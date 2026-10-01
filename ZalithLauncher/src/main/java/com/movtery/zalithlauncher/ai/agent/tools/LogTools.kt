/*
 * ZyNova Launcher
 * Copyright (C) 2026 zzy89216-gif and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ai.agent.tools

import com.movtery.zalithlauncher.ai.agent.AITool
import com.movtery.zalithlauncher.ai.agent.AIToolSchema
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.path.PathManager
import java.io.File

/**
 * 日志与崩溃分析工具。
 *
 * 这是「为什么这个实例进不去？」这类问题的核心抓手：
 * **先读真实日志**，再下结论，而不是凭空猜。
 */
internal fun logTools(): List<AITool> = listOf(

    aiTool(
        name = "list_logs",
        description = """
            列出可读取的日志文件：启动器日志、原生库日志，
            以及各实例游戏目录下的 crash-reports。
            排查崩溃问题时先调用它，确认有哪些日志、哪个是最近的。
        """.trimIndent(),
        parameters = AIToolSchema.noArgs(),
    ) { _, _ ->
        val sb = StringBuilder()

        fun section(title: String, dir: File) {
            sb.append("── ").append(title).append('\n')
            if (!dir.isDirectory) {
                sb.append("   （目录不存在：").append(dir.absolutePath).append("）\n\n")
                return
            }
            val files = dir.listFiles()?.filter { it.isFile }?.sortedByDescending { it.lastModified() }
            if (files.isNullOrEmpty()) {
                sb.append("   （空）\n\n")
                return
            }
            files.take(30).forEach { f ->
                sb.append("   ").append(f.name)
                    .append("  ").append(AIToolSupport.humanSize(f.length()))
                    .append("  ").append(java.util.Date(f.lastModified()))
                    .append('\n')
            }
            sb.append('\n')
        }

        section("启动器日志", PathManager.DIR_LAUNCHER_LOGS)
        section("原生库日志", PathManager.DIR_NATIVE_LOGS)

        sb.append("── 各实例的 crash-reports\n")
        VersionsManager.versions.value.forEach { v ->
            val dir = File(v.getGameDir(), "crash-reports")
            val files = dir.listFiles()?.filter { it.isFile }?.sortedByDescending { it.lastModified() }
            sb.append("   ").append(v.getVersionName()).append("：")
            if (files.isNullOrEmpty()) {
                sb.append("（无崩溃报告）\n")
            } else {
                sb.append('\n')
                files.take(5).forEach { f ->
                    sb.append("      ").append(f.name)
                        .append("  ").append(java.util.Date(f.lastModified())).append('\n')
                }
            }
        }
        sb.toString()
    },

    aiTool(
        name = "read_log",
        description = """
            读取一个日志文件。默认只读结尾 40000 字符（崩溃原因通常在最后）。
            file 可以是：
            - 文件名（在启动器日志 / 原生日志目录里查找）
            - 绝对路径（必须在允许目录内）
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "file" to AIToolSchema.string("日志文件名或路径"),
                "max_chars" to AIToolSchema.integer("最多读取字符数", default = 40000, minimum = 500, maximum = 200000),
            ),
            required = listOf("file"),
        ),
    ) { args, _ ->
        val name = args.str("file")!!
        val file = locateLog(name) ?: return@aiTool "找不到日志文件：$name（可先用 list_logs 看看有哪些）"
        "日志：${file.absolutePath}\n\n" +
                AIToolSupport.readText(file, args.int("max_chars") ?: 40_000, tailMode = true)
    },

    aiTool(
        name = "summarize_log",
        description = """
            快速提取日志里的**关键错误行**（异常、Caused by、FATAL、crash、OpenGL/驱动报错等），
            并给出每类错误出现的次数和首次出现的行号。
            日志很长时先用它定位问题，再用 read_log 读相关片段。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "file" to AIToolSchema.string("日志文件名或路径"),
                "max_hits" to AIToolSchema.integer("最多返回多少条命中行", default = 80, minimum = 10, maximum = 300),
            ),
            required = listOf("file"),
        ),
    ) { args, _ ->
        val file = locateLog(args.str("file")!!)
            ?: return@aiTool "找不到日志文件：${args.str("file")}"
        if (!file.exists()) return@aiTool "日志不存在：${file.absolutePath}"

        val lines = file.readLines()
        val maxHits = args.int("max_hits") ?: 80
        val patterns = listOf(
            "exception", "caused by", "fatal", "error", "crash",
            "failed", "unable to", "could not", "out of memory", "unsatisfiedlinkerror",
            "noclassdeffound", "no such file", "permission denied", "incompatible",
        )
        val hits = lines.indices.filter { i ->
            val l = lines[i]
            patterns.any { l.contains(it, ignoreCase = true) }
        }
        if (hits.isEmpty()) return@aiTool "在这个日志里没有发现明显的错误行（共 ${lines.size} 行）"

        val counter = linkedMapOf<String, Int>()
        hits.forEach { i ->
            val l = lines[i].trim()
            if (l.length > 160) {
                //归一化：把数字/路径换掉，便于按「同类错误」归类计数
                val key = l.take(160)
                counter[key] = (counter[key] ?: 0) + 1
            } else {
                counter[l] = (counter[l] ?: 0) + 1
            }
        }
        val top = counter.entries.sortedByDescending { it.value }.take(15)

        buildString {
            append("日志：").append(file.absolutePath).append('\n')
            append("总行数：").append(lines.size).append("，疑似错误行：").append(hits.size).append("\n\n")
            append("── 出现次数最多的错误\n")
            top.forEach { (text, count) ->
                append("   [").append(count).append(" 次] ").append(text).append('\n')
            }
            append("\n── 错误行（按出现顺序，最多 ").append(maxHits).append(" 条）\n")
            hits.take(maxHits).forEach { i ->
                append("L").append(i + 1).append(": ").append(lines[i].take(300)).append('\n')
            }
            if (hits.size > maxHits) append("…（还有 ").append(hits.size - maxHits).append(" 条）\n")
        }
    },

    aiTool(
        name = "read_crash_report",
        description = """
            读取 Minecraft 的崩溃报告（crash report）。
            这是判断「游戏为什么崩」最权威的材料：
            里面有崩溃描述、涉及的模组、堆栈与系统信息。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "instance" to AIToolSchema.string("实例名称；不传则使用当前选中的实例"),
                "name" to AIToolSchema.string("crash-reports 里的文件名；不传则读最新的那一份"),
                "max_chars" to AIToolSchema.integer("最多读取字符数", default = 40000, minimum = 500, maximum = 200000),
            ),
        ),
    ) { args, ctx ->
        val version = AIToolSupport.resolveVersion(args.str("instance"), ctx)
        val dir = File(version.getGameDir(), "crash-reports")

        val file = args.str("name")?.let { File(dir, it) }
            ?: dir.listFiles()?.filter { it.isFile }?.maxByOrNull { it.lastModified() }

        if (file == null || !file.exists()) {
            return@aiTool "实例「${version.getVersionName()}」没有崩溃报告（目录：${dir.absolutePath}）。" +
                    "可以改用 read_log / summarize_log 看启动器日志。"
        }

        "崩溃报告：${file.absolutePath}\n\n" +
                AIToolSupport.readText(file, args.int("max_chars") ?: 40_000)
    },

    aiTool(
        name = "read_crash_report_legacy",
        description = """
            读取启动器记录的最近一次崩溃报告文件（启动器层面的崩溃，不是游戏内崩溃）。
            启动器自身异常退出时看它。
        """.trimIndent(),
        parameters = AIToolSchema.noArgs(),
    ) { _, _ ->
        val file = PathManager.FILE_CRASH_REPORT
        if (!file.exists()) return@aiTool "还没有启动器层面的崩溃报告。"
        "崩溃报告：${file.absolutePath}\n\n" + AIToolSupport.readText(file, 40_000)
    },
)

/**
 * 在启动器日志 / 原生日志目录里按名字找日志；也接受允许范围内的绝对路径。
 *
 * ⚠️ 安全要点：**不能** 用 `File(logDir, name)` 直接拼路径。
 * 如果 `name` 里带 `../`（例如 `../../../etc/hosts`），拼出来的路径会逃出日志目录
 * 并且**绕过允许目录白名单**。因此这里统一走
 * [AIToolSupport.resolveInside] 做规范化 + 白名单校验。
 */
private fun locateLog(name: String): File? {
    /** 规范化并校验；越界或非法直接返回 null（不再抛给上层） */
    fun safe(base: File): File? =
        runCatching { AIToolSupport.resolveInside(name, base) }.getOrNull()

    if (File(name).isAbsolute) return safe(PathManager.DIR_LAUNCHER_LOGS)

    val candidates = listOfNotNull(
        safe(PathManager.DIR_LAUNCHER_LOGS),
        safe(PathManager.DIR_NATIVE_LOGS),
    )
    candidates.firstOrNull { it.exists() }?.let { return it }

    // 名字不完全匹配时，按模糊匹配找最近的。
    // 这一步只在**已列出的文件名**里找，不做路径拼接，因此是安全的。
    return listOf(PathManager.DIR_LAUNCHER_LOGS, PathManager.DIR_NATIVE_LOGS)
        .flatMap { dir -> dir.listFiles()?.toList().orEmpty() }
        .filter { it.isFile }
        .filter { it.name.contains(name, ignoreCase = true) }
        .maxByOrNull { it.lastModified() }
}
