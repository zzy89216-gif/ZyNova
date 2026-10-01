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

import com.movtery.zalithlauncher.ai.agent.AIToolContext
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.path.PathManager
import java.io.File
import java.io.RandomAccessFile

/**
 * 工具实现的公共支撑：实例解析、路径安全、文本格式化。
 *
 * ⚠️ 这里是整个 Agent 的**安全边界**所在：
 * 任何涉及文件路径的工具都必须走 [resolveInside]，
 * 否则模型可以用 `../../` 读写沙箱之外的文件。
 */
internal object AIToolSupport {

    /** 允许 Agent 访问的根目录（游戏目录 + 启动器数据目录 + 日志目录）*/
    fun allowedRoots(): List<File> = buildList {
        add(PathManager.DIR_FILES_PRIVATE)
        add(PathManager.DIR_FILES_EXTERNAL)
        add(PathManager.DIR_GAME)
        add(PathManager.DIR_LAUNCHER_LOGS)
        VersionsManager.versions.value.forEach { add(it.getGameDir()) }
    }.distinctBy { it.absolutePath }

    /**
     * 按名字解析实例；未指定时用当前选中的实例。
     * 找不到时抛出可读异常（会让模型知道要换一个名字）。
     */
    fun resolveVersion(name: String?, context: AIToolContext): Version {
        val versions = VersionsManager.versions.value
        if (versions.isEmpty()) {
            throw IllegalStateException("当前没有任何 Minecraft 实例，请先在启动器里下载一个版本。")
        }
        if (name.isNullOrBlank()) {
            val current = context.currentVersion
                ?: VersionsManager.currentVersion.value
                ?: versions.first()
            return current
        }
        return versions.firstOrNull { it.getVersionName() == name }
            ?: versions.firstOrNull { it.getVersionName().equals(name, ignoreCase = true) }
            ?: throw IllegalStateException(
                "找不到名为「$name」的实例。现有实例：" +
                    versions.joinToString("、") { it.getVersionName() }
            )
    }

    /** 实例摘要（给模型看的紧凑格式）*/
    fun describeVersion(version: Version): String = buildString {
        append("名称=").append(version.getVersionName())
        append(" | 类型=").append(version.versionType.name)
        append(" | 有效=").append(version.isValid())
        append(" | 目录=").append(version.getGameDir().absolutePath)
        runCatching { append(" | 摘要=").append(version.getVersionSummary()) }
    }

    /** 取实例下某个子文件夹（mods / resourcepacks / shaderpacks / saves）*/
    fun folderOf(version: Version, folder: VersionFolders): File =
        folder.getDir(version.getGameDir())

    /**
     * 把「相对路径」解析成一个**确定在允许根目录内**的 File。
     *
     * - 相对路径一律以 [base] 为基准
     * - 绝对路径必须落在 [allowedRoots] 之内
     * - 越界直接抛异常，不做任何「尽力而为」的尝试
     */
    fun resolveInside(path: String, base: File? = null): File {
        val raw = path.trim()
        require(raw.isNotEmpty()) { "路径不能为空" }

        val target = if (File(raw).isAbsolute) File(raw) else File(base ?: PathManager.DIR_GAME, raw)
        val canonical = runCatching { target.canonicalFile }.getOrElse { target.absoluteFile }

        val roots = allowedRoots().mapNotNull { runCatching { it.canonicalFile }.getOrNull() }
        val ok = roots.any { root ->
            canonical == root || canonical.path.startsWith(root.path + File.separator)
        }
        require(ok) {
            "拒绝访问：$raw 不在允许的目录范围内（只能访问游戏目录与启动器数据目录）"
        }
        return canonical
    }

    /** 人类可读的体积 */
    fun humanSize(bytes: Long): String = when {
        bytes >= 1024L * 1024 * 1024 -> "%.2f GB".format(bytes / 1024.0 / 1024 / 1024)
        bytes >= 1024L * 1024 -> "%.1f MB".format(bytes / 1024.0 / 1024)
        bytes >= 1024L -> "%.1f KB".format(bytes / 1024.0)
        else -> "$bytes B"
    }

    /** 列目录（默认不递归），带体积与类型标记 */
    fun listDirectory(dir: File, recursive: Boolean = false, maxEntries: Int = 300): String {
        if (!dir.exists()) return "目录不存在：${dir.absolutePath}"
        if (!dir.isDirectory) return "不是目录：${dir.absolutePath}"

        val sb = StringBuilder()
        sb.append("目录：").append(dir.absolutePath).append('\n')

        fun walk(d: File, prefix: String, depth: Int) {
            val children = d.listFiles()?.sortedBy { it.name } ?: return
            var shown = 0
            for (child in children) {
                if (shown >= maxEntries) {
                    sb.append(prefix).append("…（已截断）\n")
                    return
                }
                shown++
                if (child.isDirectory) {
                    sb.append(prefix).append(child.name).append("/\n")
                    if (recursive && depth < 3) walk(child, "$prefix  ", depth + 1)
                } else {
                    sb.append(prefix).append(child.name)
                        .append("  (").append(humanSize(child.length())).append(")\n")
                }
            }
        }
        walk(dir, "", 0)
        if (sb.lines().size <= 2) sb.append("（空目录）\n")
        return sb.toString()
    }

    /**
     * 读取文本文件。
     *
     * ⚠️ **不要**直接 `file.readText()`：日志动辄几百 MB，
     * 整份读进内存会直接把 App 干成 OOM。
     * 这里按需要只读「头部」或「尾部」的一段字节：
     * - 文件不大 → 还是整份读，行为与以前一致
     * - 文件很大 → 只读需要的那一段（tail 模式用 RandomAccessFile 定位到末尾）
     */
    fun readText(file: File, maxChars: Int = 40_000, tailMode: Boolean = false): String {
        if (!file.exists()) return "文件不存在：${file.absolutePath}"
        if (file.isDirectory) return "这是一个目录，不是文件：${file.absolutePath}"

        val length = file.length()
        //UTF-8 中文最多 4 字节/字符，留足余量，保证解码后至少能凑够 maxChars 个字符
        val maxBytes = (maxChars.toLong() * 4).coerceAtLeast(64L * 1024)

        //① 小文件：整份读，并用原有方式提示截断
        if (length <= maxBytes) {
            val text = runCatching { file.readText() }.getOrElse { e ->
                return "读取失败：${e.message ?: e::class.simpleName}"
            }
            if (text.length <= maxChars) return text
            return if (tailMode) {
                "（文件较大，仅显示最后 $maxChars 字符）\n" + text.takeLast(maxChars)
            } else {
                text.take(maxChars) + "\n…（已截断，仅显示前 $maxChars 字符）"
            }
        }

        //② 大文件：只读一段，避免 OOM
        val chunk = runCatching {
            if (tailMode) readTailBytes(file, maxBytes) else readHeadBytes(file, maxBytes)
        }.getOrElse { e ->
            return "读取失败：${e.message ?: e::class.simpleName}"
        }

        val note = if (tailMode) {
            "（文件较大：${humanSize(length)}，仅显示最后 $maxChars 字符）"
        } else {
            "（文件较大：${humanSize(length)}，仅显示前 $maxChars 字符）"
        }
        val body = if (chunk.length <= maxChars) chunk
        else if (tailMode) chunk.takeLast(maxChars) else chunk.take(maxChars)

        return "$note\n$body"
    }

    /** 读取文件开头的若干字节（不使用 readNBytes：Android 低版本没有） */
    private fun readHeadBytes(file: File, maxBytes: Long): String =
        file.inputStream().use { input ->
            val buf = ByteArray(maxBytes.toInt())
            var read = 0
            while (read < buf.size) {
                val n = input.read(buf, read, buf.size - read)
                if (n <= 0) break
                read += n
            }
            String(buf, 0, read, Charsets.UTF_8).trimStartReplacement()
        }

    /** 读取文件末尾的若干字节（定位到末尾，不读前面） */
    private fun readTailBytes(file: File, maxBytes: Long): String =
        RandomAccessFile(file, "r").use { raf ->
            val start = (raf.length() - maxBytes).coerceAtLeast(0L)
            raf.seek(start)
            val buf = ByteArray((raf.length() - start).toInt())
            raf.readFully(buf)
            String(buf, Charsets.UTF_8).trimStartReplacement()
        }

    /** 按字节截断 UTF-8 可能切在多字节字符中间，去掉开头产生的替换字符 */
    private fun String.trimStartReplacement(): String = trimStart('\uFFFD')

    /** 目录体积统计 */
    fun folderFileCount(dir: File): Int =
        dir.listFiles()?.count { !it.isDirectory } ?: 0
}
