/*
 * ZyNova Launcher
 * Copyright (C) 2026 zzy89216-gif and contributors
 * GPL-3.0-or-later
 */

package com.movtery.zalithlauncher.ai.agent.tools

import android.os.Environment
import com.movtery.zalithlauncher.ai.agent.AITool
import com.movtery.zalithlauncher.ai.agent.AIToolRisk
import com.movtery.zalithlauncher.ai.agent.AIToolSchema
import com.movtery.zalithlauncher.path.PathManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * AI 的「公有目录」文件能力（议题 #45）。
 *
 * 边界（写死在代码里，不靠提示词约束）：
 * - 只能动 `/storage/emulated/0/` 下、**且不在 `Android/data`、`Android/obb`** 里的内容；
 * - 需要「所有文件访问权限」（`MANAGE_EXTERNAL_STORAGE`），没授权时直接给出可执行的提示；
 * - **删除不真删**：一律先移到 App 私有目录的 `.recycle_bin/<时间戳>/`，用户能自己捞回来。
 */
private const val EXTERNAL_ROOT = "/storage/emulated/0"
private val BLOCKED_SEGMENTS = listOf("/Android/data", "/Android/obb")

private fun isGranted(): Boolean = Environment.isExternalStorageManager()

private const val PERMISSION_HINT =
    "还没有「所有文件访问权限」。请到：设置 → AI 设置 → 所有文件访问权限 → 打开后再试。"

/** 把用户给的路径规范化成 File，并做边界检查 */
private fun resolvePublic(raw: String): File {
    val path = raw.trim().ifBlank { error("路径不能为空") }
    val absolute = if (path.startsWith("/")) path else "$EXTERNAL_ROOT/$path"
    val file = File(absolute).canonicalFile
    val root = File(EXTERNAL_ROOT).canonicalFile
    require(file.path == root.path || file.path.startsWith(root.path + "/")) {
        "只允许访问 $EXTERNAL_ROOT 下的文件"
    }
    BLOCKED_SEGMENTS.forEach { blocked ->
        require(!file.path.contains(blocked)) { "禁止访问 $blocked（系统保护区）" }
    }
    return file
}

private fun recycleBin(target: File): File {
    val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
    val bin = File(PathManager.DIR_FILES_EXTERNAL, ".recycle_bin/$stamp")
    bin.mkdirs()
    return File(bin, target.name)
}

internal fun publicFileTools(): List<AITool> = listOf(
    aiTool(
        name = "public_list",
        description = """
            列出公有存储（如 Download）里的文件和文件夹。用于找用户手动下载的模组/整合包/资源包。
            需要「所有文件访问权限」；Android/data 与 Android/obb 不可访问。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf("path" to AIToolSchema.string("目录路径，默认 Download")),
        ),
        risk = AIToolRisk.READ,
    ) { args, _ ->
        if (!isGranted()) return@aiTool PERMISSION_HINT
        val dir = resolvePublic(args.strOr("path", "Download"))
        require(dir.isDirectory) { "不是文件夹：${dir.absolutePath}" }
        val items = dir.listFiles()?.sortedBy { it.name }.orEmpty()
        if (items.isEmpty()) "空文件夹：${dir.absolutePath}"
        else buildString {
            append("目录：").append(dir.absolutePath).append('\n')
            items.take(200).forEach { f ->
                append(if (f.isDirectory) "[目录] " else "[文件] ")
                append(f.name)
                if (f.isFile) append("  (").append(f.length() / 1024).append(" KB)")
                append('\n')
            }
            if (items.size > 200) append("…共 ").append(items.size).append(" 项")
        }
    },

    aiTool(
        name = "public_read",
        description = "读取公有存储里的文本文件（最多 64KB）。用来检查用户下载的配置文件 / 日志。",
        parameters = AIToolSchema.objectSchema(
            properties = mapOf("path" to AIToolSchema.string("文件路径")),
            required = listOf("path"),
        ),
        risk = AIToolRisk.READ,
    ) { args, _ ->
        if (!isGranted()) return@aiTool PERMISSION_HINT
        val file = resolvePublic(args.str("path")!!)
        require(file.isFile) { "文件不存在：${file.absolutePath}" }
        require(file.length() <= 64 * 1024) { "文件太大（${file.length()} 字节），请改用别的方式查看" }
        file.readText()
    },

    aiTool(
        name = "public_write",
        description = """
            在公有存储里写文本文件（父目录会自动创建）。
            ⚠️ 只能写字面量文本；写二进制（jar/apk）请让用户自己下载。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "path" to AIToolSchema.string("文件路径"),
                "content" to AIToolSchema.string("要写入的文本"),
            ),
            required = listOf("path", "content"),
        ),
        risk = AIToolRisk.WRITE,
    ) { args, _ ->
        if (!isGranted()) return@aiTool PERMISSION_HINT
        val file = resolvePublic(args.str("path")!!)
        require(!file.isDirectory) { "目标是文件夹：${file.absolutePath}" }
        file.parentFile?.mkdirs()
        file.writeText(args.str("content").orEmpty())
        "已写入 ${file.absolutePath}"
    },

    aiTool(
        name = "public_mkdir",
        description = "在公有存储里创建文件夹（已存在则忽略）。",
        parameters = AIToolSchema.objectSchema(
            properties = mapOf("path" to AIToolSchema.string("文件夹路径")),
            required = listOf("path"),
        ),
        risk = AIToolRisk.WRITE,
    ) { args, _ ->
        if (!isGranted()) return@aiTool PERMISSION_HINT
        val dir = resolvePublic(args.str("path")!!)
        dir.mkdirs()
        "已就绪：${dir.absolutePath}"
    },

    aiTool(
        name = "public_move_to_recycle_bin",
        description = """
            把公有存储里的文件/文件夹移到回收站（App 私有目录 .recycle_bin/，可恢复），
            **不会真正删除**。清理 Download 里的重复整合包用它。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf("path" to AIToolSchema.string("要移走的路径")),
            required = listOf("path"),
        ),
        risk = AIToolRisk.WRITE,
    ) { args, _ ->
        if (!isGranted()) return@aiTool PERMISSION_HINT
        val src = resolvePublic(args.str("path")!!)
        require(src.exists()) { "不存在：${src.absolutePath}" }
        val target = recycleBin(src)
        val ok = src.renameTo(target) || runCatching {
            src.copyRecursively(target, overwrite = true).also { src.deleteRecursively() }
        }.isSuccess
        if (!ok) error("移动失败：${src.absolutePath}")
        "已移到回收站：${target.absolutePath}（需要的话可以自己移回去）"
    },

    aiTool(
        name = "import_from_public",
        description = """
            把公有存储里的文件/文件夹**复制进 App 私有目录**（自动导入）。
            例如把 Download 里的整合包 jar 放进游戏目录的 mods。
            目标路径相对游戏目录，例如 `mods/xxx.jar` 或 `modpacks/xxx`。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "source" to AIToolSchema.string("公有存储里的源路径"),
                "target" to AIToolSchema.string("App 内的目标路径（相对游戏目录）"),
            ),
            required = listOf("source", "target"),
        ),
        risk = AIToolRisk.WRITE,
    ) { args, _ ->
        if (!isGranted()) return@aiTool PERMISSION_HINT
        val src = resolvePublic(args.str("source")!!)
        require(src.exists()) { "源不存在：${src.absolutePath}" }
        val dst = AIToolSupport.resolveInside(args.str("target")!!, PathManager.DIR_GAME)
        dst.parentFile?.mkdirs()
        if (src.isDirectory) src.copyRecursively(dst, overwrite = true)
        else src.copyTo(dst, overwrite = true)
        "已导入：${src.absolutePath} → ${dst.absolutePath}"
    },
)
