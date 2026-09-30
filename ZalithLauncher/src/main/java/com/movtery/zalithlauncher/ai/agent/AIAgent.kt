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

package com.movtery.zalithlauncher.ai.agent

import com.movtery.zalithlauncher.ai.AIPermissionMode
import com.movtery.zalithlauncher.ai.model.AIMessage
import com.movtery.zalithlauncher.ai.model.AIRole
import com.movtery.zalithlauncher.ai.model.AIToolCall
import com.movtery.zalithlauncher.ai.model.AIToolResult
import com.movtery.zalithlauncher.ai.provider.AIChatRequest
import com.movtery.zalithlauncher.ai.provider.AI_JSON
import com.movtery.zalithlauncher.ai.provider.AIStreamEvent
import com.movtery.zalithlauncher.ai.provider.AIProviders
import com.movtery.zalithlauncher.ai.provider.AIProviderType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import java.util.UUID

/**
 * Agent 运行过程中向外发出的事件。
 *
 * UI 只需要按序消费这些事件即可把「思考 → 调工具 → 拿结果 → 继续回答」画出来。
 */
sealed interface AgentEvent {
    /** 模型正在输出的正文增量 */
    data class TextDelta(val text: String) : AgentEvent

    /** 一轮助手消息结束（可能带工具调用）*/
    data class TurnFinished(val message: AIMessage) : AgentEvent

    /** 开始执行某个工具 */
    data class ToolStarted(val call: AIToolCall) : AgentEvent

    /** 工具执行完成 */
    data class ToolFinished(val result: AIToolResult) : AgentEvent

    /** 失败（网络 / 鉴权 / 达到步数上限等）*/
    data class Failed(val message: String) : AgentEvent

    /** 模型不再调用工具，本轮对话结束 */
    data object Done : AgentEvent
}

/**
 * AI Agent 主循环。
 *
 * 流程：
 * 1. 把当前对话发给模型（带工具定义），流式接收
 * 2. 若模型返回工具调用 → 逐个执行（受权限模式约束）→ 结果作为 TOOL 消息回灌
 * 3. 回到第 1 步，直到模型不再调用工具、或达到 [maxSteps] 上限
 *
 * @param conversation 初始对话（user/assistant/tool 消息，**不含**系统提示）
 */
object AIAgent {

    fun run(
        providerType: AIProviderType,
        apiKey: String,
        baseUrl: String,
        model: String,
        temperature: Float,
        systemPrompt: String?,
        conversation: List<AIMessage>,
        context: AIToolContext,
        permissionMode: AIPermissionMode,
        maxSteps: Int,
        toolNames: List<String>? = null,
    ): Flow<AgentEvent> = flow {
        val provider = AIProviders.get(providerType)
        val tools = AIToolRegistry.all()
            .filter { toolNames == null || it.spec.name in toolNames }
            .map { it.spec }

        val history = conversation.toMutableList()
        var step = 0

        while (true) {
            if (step >= maxSteps) {
                emit(AgentEvent.Failed("已达到工具调用轮数上限（$maxSteps 轮），已停止。可以继续追问让它接着做。"))
                return@flow
            }
            step++

            val request = AIChatRequest(
                apiKey = apiKey,
                baseUrl = baseUrl,
                model = model,
                messages = history.toList(),
                tools = tools,
                temperature = temperature,
                systemPrompt = systemPrompt,
            )

            val text = StringBuilder()
            val accumulators = linkedMapOf<Int, ToolCallAccumulator>()
            var failure: String? = null

            provider.streamChat(request).collect { event ->
                when (event) {
                    is AIStreamEvent.TextDelta -> {
                        text.append(event.text)
                        emit(AgentEvent.TextDelta(event.text))
                    }

                    is AIStreamEvent.ToolCallDelta -> {
                        accumulators.getOrPut(event.index) { ToolCallAccumulator() }.apply(event)
                    }

                    is AIStreamEvent.Completed -> Unit

                    is AIStreamEvent.Failed -> failure = event.message
                }
            }

            failure?.let {
                emit(AgentEvent.Failed(it))
                return@flow
            }

            val calls = accumulators.toSortedMap().values
                .mapNotNull { it.build() }
                .filter { it.name.isNotBlank() }

            val assistant = AIMessage(
                id = UUID.randomUUID().toString(),
                role = AIRole.ASSISTANT,
                text = text.toString(),
                toolCalls = calls,
            )
            history += assistant
            emit(AgentEvent.TurnFinished(assistant))

            // 没有工具调用 → 本轮结束
            if (calls.isEmpty()) {
                emit(AgentEvent.Done)
                return@flow
            }

            val results = mutableListOf<AIToolResult>()
            for (call in calls) {
                emit(AgentEvent.ToolStarted(call))
                val result = executeTool(call, context, permissionMode)
                results += result
                emit(AgentEvent.ToolFinished(result))
            }
            history += AIMessage(
                id = UUID.randomUUID().toString(),
                role = AIRole.TOOL,
                toolResults = results,
            )
        }
    }

    /**
     * 执行单个工具调用，包含权限裁决与异常兜底。
     *
     * 关键安全点：**「操作确认」模式下若上层没有提供确认器，一律拒绝写操作**，
     * 避免出现「用户以为要确认、实际直接执行」的情况。
     */
    private suspend fun executeTool(
        call: AIToolCall,
        context: AIToolContext,
        permissionMode: AIPermissionMode,
    ): AIToolResult {
        val tool = AIToolRegistry.get(call.name)
            ?: return AIToolResult(
                toolCallId = call.id,
                name = call.name,
                content = "没有名为 `${call.name}` 的工具。可用工具请参考系统提示中的清单。",
                isError = true,
            )

        val args: JsonObject = runCatching {
            if (call.arguments.isBlank()) JsonObject(emptyMap())
            else AI_JSON.parseToJsonElement(call.arguments).jsonObject
        }.getOrElse { e ->
            return AIToolResult(
                toolCallId = call.id,
                name = call.name,
                content = "参数不是合法 JSON：${call.arguments.take(300)}（${e.message}）",
                isError = true,
            )
        }

        // 权限裁决
        if (permissionMode == AIPermissionMode.CONFIRM && tool.risk != AIToolRisk.READ) {
            val approved = runCatching {
                context.confirm(call.name, describe(call, args))
            }.getOrDefault(false)
            if (!approved) {
                return AIToolResult(
                    toolCallId = call.id,
                    name = call.name,
                    content = "用户拒绝执行该操作（权限模式：操作确认）。可以改用只读方式排查，或询问用户是否允许。",
                    isError = true,
                    writeOperation = true,
                )
            }
        }

        return try {
            val output = tool.execute(args, context)
            AIToolResult(
                toolCallId = call.id,
                name = call.name,
                content = output.ifBlank { "（执行成功，无输出）" },
                writeOperation = tool.risk != AIToolRisk.READ,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AIToolResult(
                toolCallId = call.id,
                name = call.name,
                content = "执行失败：${e.message ?: e::class.simpleName}",
                isError = true,
                writeOperation = tool.risk != AIToolRisk.READ,
            )
        }
    }

    private fun describe(call: AIToolCall, args: JsonObject): String = buildString {
        append("工具：").append(call.name).append('\n')
        append("参数：").append(args.toString().take(500))
    }
}

/**
 * 把 Provider 分片吐出来的工具调用拼成完整的 [AIToolCall]。
 * OpenAI 与 Anthropic 都会把 id / name / 参数拆成多块。
 */
private class ToolCallAccumulator {
    private var id: String? = null
    private var name: String = ""
    private val args = StringBuilder()

    fun apply(delta: AIStreamEvent.ToolCallDelta) {
        delta.id?.takeIf { it.isNotBlank() }?.let { id = it }
        delta.name?.takeIf { it.isNotBlank() }?.let { name = name.ifBlank { it } }
        delta.argumentsDelta?.let { args.append(it) }
    }

    fun build(): AIToolCall? {
        if (name.isBlank()) return null
        return AIToolCall(
            id = id ?: "call_${UUID.randomUUID()}",   // 个别中转不返回 id，兜底生成一个
            name = name,
            arguments = args.toString().ifBlank { "{}" },
        )
    }
}
