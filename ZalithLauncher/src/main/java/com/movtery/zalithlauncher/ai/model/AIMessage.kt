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

package com.movtery.zalithlauncher.ai.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * 对话角色。
 *
 * 这是**与 Provider 无关**的统一模型：各 Provider 在发送前自行映射成
 * OpenAI 的 `user/assistant/tool` 或 Anthropic 的 `content block`。
 */
@Serializable
enum class AIRole {
    SYSTEM,
    USER,
    ASSISTANT,
    /** 工具执行结果（OpenAI 用独立的 tool 消息，Anthropic 包在 user 里）*/
    TOOL,
}

/**
 * 一次工具调用（模型请求执行某个 [com.movtery.zalithlauncher.ai.agent.AITool]）
 */
@Serializable
data class AIToolCall(
    /** Provider 给出的调用 ID，回灌结果时必须原样带上 */
    val id: String,
    /** 工具名 */
    val name: String,
    /** 模型给出的参数（原始 JSON 字符串，可能不完整/非法，执行前必须校验）*/
    val arguments: String = "",
)

/**
 * 一次工具执行结果
 */
@Serializable
data class AIToolResult(
    val toolCallId: String,
    val name: String,
    val content: String,
    /** 是否执行失败（失败也会回灌给模型，让它自行调整）*/
    val isError: Boolean = false,
    /** 该工具是否属于写操作（用于 UI 标记与审计）*/
    val writeOperation: Boolean = false,
    /**
     * 是否仍在执行中。
     *
     * 这是**只在界面上存在的中间态**：Agent 会在工具开始时先发一条 running 的消息，
     * 执行完再就地替换成真正的结果。落盘前会被清掉。
     */
    val running: Boolean = false,
)

/**
 * 一条对话消息
 *
 * 同时承载「纯文本」「模型要求调用工具」「工具执行结果」三种形态，
 * 这样 UI 只需要渲染 [AIMessage] 列表，不必关心 Provider 差异。
 */
@Serializable
data class AIMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: AIRole,
    /** 文本内容（助手回答、用户输入、系统提示）*/
    val text: String = "",
    /** 助手要求调用的工具 */
    val toolCalls: List<AIToolCall> = emptyList(),
    /** 工具执行结果（role = TOOL 时使用）*/
    val toolResults: List<AIToolResult> = emptyList(),
    /** 是否正在流式接收中 */
    val streaming: Boolean = false,
    /** 本条消息出错时的提示（展示给用户）*/
    val error: String? = null,
) {
    val isEmpty: Boolean
        get() = text.isBlank() && toolCalls.isEmpty() && toolResults.isEmpty() && error == null
}
