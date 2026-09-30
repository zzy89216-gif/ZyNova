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
import com.movtery.zalithlauncher.ai.agent.AIToolRisk
import com.movtery.zalithlauncher.ai.agent.AIToolSchema
import com.movtery.zalithlauncher.path.PathManager
import java.io.File

/**
 * 文件管理工具。
 *
 * ⚠️ 全部路径都经过 [AIToolSupport.resolveInside] 校验：
 * 只能访问游戏目录、启动器数据目录与日志目录，
 * 模型无法用 `../../` 越界读写系统文件。
 */
internal fun fileTools(): List<AITool> = listOf(

    aiTool(
        name = "list_files",
        description = """
            列出目录内容。
            path 可以是绝对路径，也可以是相对路径（相对游戏根目录）。
            查配置目录、看实例目录结构时使用。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "path" to AIToolSchema.string("目录路径；'.' 表示游戏根目录"),
                "recursive" to AIToolSchema.bool("是否递归列出子目录（最多 3 层）", default = false),
            ),
            required = listOf("path"),
        ),
    ) { args, _ ->
        val dir = AIToolSupport.resolveInside(args.strOr("path", "."), PathManager.DIR_GAME)
        AIToolSupport.listDirectory(dir, args.bool("recursive") ?: false)
    },

    aiTool(
        name = "read_file",
        description = """
            读取文本文件内容。
            典型用途：读 options.txt、各模组的 config/*.json、server.properties、日志。
            文件很大时建议配合 tail=true 只看结尾（日志通常只看最后一段）。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "path" to AIToolSchema.string("文件路径（绝对或相对游戏根目录）"),
                "tail" to AIToolSchema.bool("只看文件结尾（适合日志）", default = false),
                "max_chars" to AIToolSchema.integer("最多读取多少字符，默认 40000", default = 40000, minimum = 500, maximum = 200000),
            ),
            required = listOf("path"),
        ),
    ) { args, _ ->
        val file = AIToolSupport.resolveInside(args.str("path")!!, PathManager.DIR_GAME)
        AIToolSupport.readText(
            file = file,
            maxChars = args.int("max_chars") ?: 40_000,
            tailMode = args.bool("tail") ?: false,
        )
    },

    aiTool(
        name = "search_in_file",
        description = """
            在文件里按关键字查找，并返回命中行及其上下文。
            修改配置前**先用它确认当前值**，避免盲改：
            例如在 options.txt 里搜 "fov"、在某个模组 config 里搜 "enabled"。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "path" to AIToolSchema.string("文件路径"),
                "keyword" to AIToolSchema.string("要查找的关键字（不区分大小写）"),
                "context_lines" to AIToolSchema.integer("每个命中项附带的前后行数", default = 2, minimum = 0, maximum = 20),
            ),
            required = listOf("path", "keyword"),
        ),
    ) { args, _ ->
        val file = AIToolSupport.resolveInside(args.str("path")!!, PathManager.DIR_GAME)
        if (!file.exists()) return@aiTool "文件不存在：${file.absolutePath}"
        if (file.isDirectory) return@aiTool "这是目录：${file.absolutePath}"

        val keyword = args.str("keyword")!!
        val ctx = args.int("context_lines") ?: 2
        val lines = file.readLines()
        val hits = lines.indices.filter { lines[it].contains(keyword, ignoreCase = true) }
        if (hits.isEmpty()) return@aiTool "在 ${file.name} 里没有找到「$keyword」"

        buildString {
            append("在 ").append(file.name).append(" 中找到 ")
                .append(hits.size).append(" 处「").append(keyword).append("」：\n\n")
            hits.take(50).forEach { idx ->
                val from = (idx - ctx).coerceAtLeast(0)
                val to = (idx + ctx).coerceAtMost(lines.lastIndex)
                append("── 第 ").append(idx + 1).append(" 行\n")
                (from..to).forEach { i ->
                    append(if (i == idx) "> " else "  ").append(lines[i]).append('\n')
                }
                append('\n')
            }
            if (hits.size > 50) append("…（命中过多，仅显示前 50 处）")
        }
    },

    aiTool(
        name = "write_file",
        description = """
            写入（覆盖）文本文件内容。父目录不存在会自动创建。
            **修改配置前必须先 read_file / search_in_file 确认原内容**，
            并且只做最小必要改动，不要整份重写你不了解的文件。
            写入后建议再 read_file 一次确认结果。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "path" to AIToolSchema.string("文件路径"),
                "content" to AIToolSchema.string("要写入的完整文本内容"),
            ),
            required = listOf("path", "content"),
        ),
        risk = AIToolRisk.WRITE,
    ) { args, _ ->
        val file = AIToolSupport.resolveInside(args.str("path")!!, PathManager.DIR_GAME)
        val content = args.str("content") ?: ""
        file.parentFile?.mkdirs()
        file.writeText(content)
        "已写入 ${file.absolutePath}（${content.length} 字符）"
    },

    aiTool(
        name = "delete_file",
        description = """
            删除一个文件或空目录（不可恢复）。
            删除模组请优先用 delete_mod；这里主要用于清理损坏的配置文件、.part 残留等。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "path" to AIToolSchema.string("要删除的文件路径"),
            ),
            required = listOf("path"),
        ),
        risk = AIToolRisk.DANGEROUS,
    ) { args, _ ->
        val file = AIToolSupport.resolveInside(args.str("path")!!, PathManager.DIR_GAME)
        require(file.exists()) { "文件不存在：${file.absolutePath}" }
        require(!file.isDirectory) { "拒绝删除目录（避免误删整个实例）：${file.absolutePath}" }
        if (!file.delete()) throw IllegalStateException("删除失败：${file.absolutePath}")
        "已删除：${file.absolutePath}"
    },

    aiTool(
        name = "copy_file",
        description = """
            复制文件（常用于备份配置：改配置前先复制一份 .bak）。
            两个路径都必须在允许目录内。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "source" to AIToolSchema.string("源文件路径"),
                "target" to AIToolSchema.string("目标文件路径"),
            ),
            required = listOf("source", "target"),
        ),
        risk = AIToolRisk.WRITE,
    ) { args, _ ->
        val src = AIToolSupport.resolveInside(args.str("source")!!, PathManager.DIR_GAME)
        val dst = File(AIToolSupport.resolveInside(args.str("target")!!, PathManager.DIR_GAME).absolutePath)
        require(src.exists()) { "源文件不存在：${src.absolutePath}" }
        require(!src.isDirectory) { "暂不支持复制目录" }
        dst.parentFile?.mkdirs()
        src.copyTo(dst, overwrite = true)
        "已复制 ${src.name} → ${dst.absolutePath}"
    },
)
