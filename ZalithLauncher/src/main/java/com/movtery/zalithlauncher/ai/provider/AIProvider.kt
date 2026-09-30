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

package com.movtery.zalithlauncher.ai.provider

import com.movtery.zalithlauncher.ai.agent.AIToolSpec
import com.movtery.zalithlauncher.ai.model.AIMessage

/**
 * 一次对话请求（**与 Provider 无关**）
 */
data class AIChatRequest(
    val apiKey: String,
    val baseUrl: String,
    val model: String,
    /** 除系统提示外的历史消息 */
    val messages: List<AIMessage>,
    /** 本轮可用的工具 */
    val tools: List<AIToolSpec> = emptyList(),
    val temperature: Float = 0.7f,
    /** 系统提示（由 Agent 组装，包含工具使用规范与当前环境信息）*/
    val systemPrompt: String? = null,
)

/**
 * 流式响应事件。
 *
 * 各 Provider 把自家的 SSE 解析成这套统一事件，UI 与 Agent 只认这一套。
 */
sealed interface AIStreamEvent {
    /** 一段正文增量 */
    data class TextDelta(val text: String) : AIStreamEvent

    /**
     * 工具调用增量。
     * OpenAI 会把一次调用拆成多块（id/name 只在第一块出现，参数分片到达），
     * 因此这里都设计成可空增量，由调用方按 [index] 累积。
     */
    data class ToolCallDelta(
        val index: Int,
        val id: String? = null,
        val name: String? = null,
        val argumentsDelta: String? = null,
    ) : AIStreamEvent

    /** 本轮结束 */
    data class Completed(val stopReason: String? = null) : AIStreamEvent

    /** 出错（网络、鉴权、限流、模型不支持工具等）*/
    data class Failed(val message: String, val cause: Throwable? = null) : AIStreamEvent
}

/**
 * AI 服务商抽象。
 *
 * 新增 Provider 的步骤：
 * 1. 在 [AIProviderType] 加枚举（含默认 Base URL）
 * 2. 实现本接口
 * 3. 到 [AIProviders] 注册
 */
interface AIProvider {

    val type: AIProviderType

    /**
     * 动态获取可用模型列表。
     *
     * **不要在这里返回硬编码的模型名**：实在拿不到就抛出可读异常，
     * 由 UI 提示用户检查 Key / Base URL。
     */
    suspend fun listModels(apiKey: String, baseUrl: String): List<com.movtery.zalithlauncher.ai.model.AIModelInfo>

    /**
     * 发起一次流式对话。
     * 返回的 Flow 会依次发出 [AIStreamEvent.TextDelta] / [AIStreamEvent.ToolCallDelta]，
     * 并以 [AIStreamEvent.Completed] 或 [AIStreamEvent.Failed] 收尾。
     */
    fun streamChat(request: AIChatRequest): kotlinx.coroutines.flow.Flow<AIStreamEvent>
}
