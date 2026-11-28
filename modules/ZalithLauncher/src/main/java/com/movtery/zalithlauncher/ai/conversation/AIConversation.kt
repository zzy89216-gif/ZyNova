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

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ai.aiString
import com.movtery.zalithlauncher.ai.model.AIMessage
import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * 一次完整的 AI 对话（会持久化到本机）
 */
@Serializable
data class AIConversation(
    val id: String = UUID.randomUUID().toString(),
    /** 侧边栏里显示的名字，默认取第一条用户消息 */
    val title: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val messages: List<AIMessage> = emptyList(),
) {
    /** 侧边栏用的轻量元信息 */
    fun toMeta(): AIConversationMeta = AIConversationMeta(
        id = id,
        title = title.ifBlank { defaultTitle() },
        updatedAt = updatedAt,
        messageCount = messages.size,
    )

    companion object {
        /** 未命名对话的显示名（跟随应用语言） */
        fun defaultTitle(): String = aiString(R.string.ai_untitled_conversation)

        /** 标题最长保留多少字符（超出加省略号） */
        const val MAX_TITLE_LENGTH = 28

        /**
         * 从用户的第一句话生成标题。
         *
         * 取第一行、压缩空白，太长就截断，避免侧边栏被撑开。
         */
        fun titleFrom(text: String): String {
            val oneLine = text.lineSequence()
                .map { it.trim() }
                .firstOrNull { it.isNotEmpty() }
                .orEmpty()
                .replace(Regex("\\s+"), " ")
            if (oneLine.isBlank()) return defaultTitle()
            return if (oneLine.length <= MAX_TITLE_LENGTH) oneLine
            else oneLine.take(MAX_TITLE_LENGTH).trimEnd() + "…"
        }
    }
}

/**
 * 侧边栏用到的对话摘要
 *
 * 单独抽出来是为了**不必解析整个对话文件**就能列出侧边栏
 * （一段对话里可能有很长的工具输出，整份解析会很浪费）。
 */
@Serializable
data class AIConversationMeta(
    val id: String,
    val title: String,
    val updatedAt: Long,
    val messageCount: Int = 0,
)
