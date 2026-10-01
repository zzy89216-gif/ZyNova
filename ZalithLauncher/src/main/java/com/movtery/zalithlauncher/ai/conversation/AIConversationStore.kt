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

package com.movtery.zalithlauncher.ai.conversation

import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.serialization.json.Json
import java.io.File

private const val TAG = "AIConversationStore"

/**
 * 对话记录的本地存储。
 *
 * 方案：**一段对话一个 JSON 文件 + 一个索引文件**
 * - `index.json`：只存侧边栏需要的摘要（id / 标题 / 时间），列表页不必解析正文
 * - `<id>.json`：完整对话
 *
 * 为什么不用 Room：现有数据库里是账号 / 路径这类实体，
 * 加表需要改 schema version 并写迁移，风险远大于收益；
 * 对话是「一个整体读写」的数据，用文件更自然，也不受单条大小限制。
 *
 * 所有方法都做了异常兜底：**存储坏掉也不能让聊天界面崩**。
 */
object AIConversationStore {

    private const val DIR_NAME = "ai_conversations"
    private const val INDEX_FILE = "index.json"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    private val dir: File
        get() = File(PathManager.DIR_FILES_PRIVATE, DIR_NAME).apply { if (!exists()) mkdirs() }

    private fun fileOf(id: String) = File(dir, "$id.json")

    // ─────────────────────────────────────────────────────────────
    //  侧边栏：摘要列表
    // ─────────────────────────────────────────────────────────────

    /** 列出全部对话摘要，按最近更新倒序 */
    @Synchronized
    fun listMetas(): List<AIConversationMeta> {
        val fromIndex = readIndex()
        if (fromIndex != null) return fromIndex.sortedByDescending { it.updatedAt }

        //索引缺失/损坏 → 扫目录重建（这样即使索引丢了也不会丢对话）
        return rebuildIndex()
    }

    // ─────────────────────────────────────────────────────────────
    //  单个对话
    // ─────────────────────────────────────────────────────────────

    @Synchronized
    fun load(id: String): AIConversation? {
        val file = fileOf(id)
        if (!file.exists()) return null
        return runCatching {
            json.decodeFromString(AIConversation.serializer(), file.readText())
        }.onFailure { e ->
            Logger.warning(TAG, "Failed to load the conversation $id.", e)
        }.getOrNull()
    }

    /** 保存一段对话（正文 + 更新索引） */
    @Synchronized
    fun save(conversation: AIConversation) {
        runCatching {
            fileOf(conversation.id).writeText(
                json.encodeToString(AIConversation.serializer(), conversation)
            )
            upsertIndex(conversation.toMeta())
        }.onFailure { e ->
            Logger.warning(TAG, "Failed to save the conversation ${conversation.id}.", e)
        }
    }

    /** 删除一段对话（正文 + 索引） */
    @Synchronized
    fun delete(id: String) {
        runCatching {
            fileOf(id).delete()
            val rest = listMetas().filterNot { it.id == id }
            writeIndex(rest)
        }.onFailure { e ->
            Logger.warning(TAG, "Failed to delete the conversation $id.", e)
        }
    }

    /** 清空全部对话 */
    @Synchronized
    fun clearAll() {
        runCatching {
            dir.listFiles()?.forEach { it.delete() }
        }.onFailure { e ->
            Logger.warning(TAG, "Failed to clear the conversations.", e)
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  内部：索引读写
    // ─────────────────────────────────────────────────────────────

    private fun indexFile() = File(dir, INDEX_FILE)

    private fun readIndex(): List<AIConversationMeta>? {
        val file = indexFile()
        if (!file.exists()) return null
        return runCatching {
            json.decodeFromString(
                kotlinx.serialization.builtins.ListSerializer(AIConversationMeta.serializer()),
                file.readText()
            )
        }.getOrNull()
    }

    private fun writeIndex(metas: List<AIConversationMeta>) {
        indexFile().writeText(
            json.encodeToString(
                kotlinx.serialization.builtins.ListSerializer(AIConversationMeta.serializer()),
                metas
            )
        )
    }

    private fun upsertIndex(meta: AIConversationMeta) {
        val rest = (readIndex() ?: emptyList()).filterNot { it.id == meta.id }
        writeIndex(rest + meta)
    }

    /**
     * 扫目录重建索引。
     *
     * 解析不了的文件会被跳过，但**不会被删除**（宁可在侧边栏里看不到，
     * 也不能因为一次解析失败就把用户的对话删掉）。
     */
    private fun rebuildIndex(): List<AIConversationMeta> {
        val metas = runCatching {
            dir.listFiles()
                ?.filter { it.isFile && it.name.endsWith(".json") && it.name != INDEX_FILE }
                ?.mapNotNull { file ->
                    runCatching {
                        json.decodeFromString(AIConversation.serializer(), file.readText()).toMeta()
                    }.getOrNull()
                }
                .orEmpty()
        }.getOrElse { emptyList() }

        runCatching { writeIndex(metas) }
        return metas.sortedByDescending { it.updatedAt }
    }
}
