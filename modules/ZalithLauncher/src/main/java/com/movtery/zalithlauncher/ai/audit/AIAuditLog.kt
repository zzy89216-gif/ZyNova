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

package com.movtery.zalithlauncher.ai.audit

import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "AIAuditLog"

/** 审计记录的执行结果 */
enum class AIAuditStatus {
    /** 成功 */
    SUCCESS,

    /** 执行失败 */
    ERROR,

    /** 被用户拒绝（操作确认模式） */
    DENIED,
}

/**
 * 一条写操作审计记录
 *
 * 只在 **写操作类工具**（风险等级 WRITE / DANGEROUS）执行后追加。
 * 只读工具不记录，避免把日志塞满。
 */
@Serializable
data class AIAuditRecord(
    /** 发生时间（毫秒时间戳） */
    val time: Long,
    /** 工具名 */
    val tool: String,
    /** 风险等级（WRITE / DANGEROUS） */
    val risk: String,
    /** 关键参数（JSON，已截断） */
    val args: String,
    /** 结果状态 */
    val status: String,
    /** 结果摘要（已截断） */
    val detail: String,
) {
    /** 导出成一行可读文本 */
    fun toText(): String {
        val ts = TIME_FORMAT.format(Date(time))
        val statusText = when (status) {
            AIAuditStatus.SUCCESS.name -> "成功"
            AIAuditStatus.ERROR.name -> "失败"
            AIAuditStatus.DENIED.name -> "被拒绝"
            else -> status
        }
        return "[$ts] $tool ($risk) → $statusText\n    参数: $args\n    结果: $detail"
    }

    //⚠️ 这里**不能**写 `private companion object`：
    //   kotlinx.serialization 会把 `serializer()` 生成到 companion 上，
    //   companion 一旦是 private，外面的 AIAuditLog 就调不到 AIAuditRecord.serializer()
    companion object {
        private val TIME_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    }
}

/**
 * Agent 写操作审计日志
 *
 * 存储方式：`ai_conversations/audit.jsonl`，**一行一条 JSON**
 * （追加写很便宜，也不需要在内存里维护整份列表）。
 *
 * 与对话一样放在 `ai_conversations/` 下，但文件名是 `.jsonl`，
 * 不会被 [com.movtery.zalithlauncher.ai.conversation.AIConversationStore] 的索引重建扫到。
 */
object AIAuditLog {

    private const val DIR_NAME = "ai_conversations"
    private const val FILE_NAME = "audit.jsonl"

    /** 超过这个大小就裁剪，只保留最近的记录，避免无限增长 */
    private const val MAX_FILE_BYTES = 2L * 1024 * 1024
    private const val KEEP_AFTER_TRIM = 2000

    /** 单条记录里参数 / 结果的截断长度 */
    private const val MAX_ARGS = 300
    private const val MAX_DETAIL = 300

    /** 导出文件里用的完整时间格式 */
    private val EXPORT_TIME_FORMAT = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val file: File
        get() = File(File(PathManager.DIR_FILES_PRIVATE, DIR_NAME).apply { if (!exists()) mkdirs() }, FILE_NAME)

    /** 追加一条记录（失败不影响主线流程） */
    @Synchronized
    fun append(
        tool: String,
        risk: String,
        args: String,
        status: AIAuditStatus,
        detail: String,
    ) {
        runCatching {
            val record = AIAuditRecord(
                time = System.currentTimeMillis(),
                tool = tool,
                risk = risk,
                args = args.take(MAX_ARGS),
                status = status.name,
                detail = detail.take(MAX_DETAIL),
            )
            file.appendText(json.encodeToString(AIAuditRecord.serializer(), record) + "\n")
            trimIfNeeded()
        }.onFailure { e ->
            Logger.warning(TAG, "Failed to append an audit record.", e)
        }
    }

    /** 读取全部记录（从旧到新） */
    @Synchronized
    fun readAll(): List<AIAuditRecord> {
        if (!file.exists()) return emptyList()
        return runCatching {
            file.readLines()
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    runCatching {
                        json.decodeFromString(AIAuditRecord.serializer(), line)
                    }.getOrNull()
                }
        }.onFailure { e ->
            Logger.warning(TAG, "Failed to read the audit log.", e)
        }.getOrElse { emptyList() }
    }

    @Synchronized
    fun clear() {
        runCatching { file.delete() }
    }

    /**
     * 导出为可读文本文件。
     *
     * ⚠️ 写到**外部私有目录**（`getExternalFilesDir`）而不是 `filesDir`：
     * `provider_paths.xml` 只声明了 external-files / cache 等路径，
     * 放 `filesDir` 的话 FileProvider 取不到 URI，分享会直接失败。
     */
    @Synchronized
    fun exportToFile(): File {
        val records = readAll()
        val target = File(PathManager.DIR_FILES_EXTERNAL, "zynova_ai_audit.txt")

        val header = buildString {
            appendLine("ZyNova AI Agent 写操作审计日志")
            appendLine("导出时间：${EXPORT_TIME_FORMAT.format(Date(System.currentTimeMillis()))}")
            appendLine("记录条数：${records.size}")
            appendLine("说明：仅记录写操作类工具（WRITE / DANGEROUS）的调用")
            appendLine("=".repeat(60))
        }
        val body = if (records.isEmpty()) "（暂无记录）\n"
        else records.joinToString("\n") { it.toText() + "\n" }

        target.writeText(header + body)
        return target
    }

    /** 当前记录条数（给界面显示用） */
    fun count(): Int = readAll().size

    /** 文件超过上限时，只保留最近的若干条 */
    private fun trimIfNeeded() {
        if (!file.exists() || file.length() <= MAX_FILE_BYTES) return
        val recent = file.readLines().filter { it.isNotBlank() }.takeLast(KEEP_AFTER_TRIM)
        file.writeText(recent.joinToString("\n") + "\n")
    }
}
