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

import com.movtery.zalithlauncher.ai.model.AIMessage
import com.movtery.zalithlauncher.ai.model.AIModelInfo
import com.movtery.zalithlauncher.ai.model.AIRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonArrayBuilder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/**
 * OpenAI Chat Completions 协议实现。
 *
 * 由于大量第三方服务（DeepSeek、Moonshot、Ollama、各类中转）都兼容该协议，
 * 用户只需要改 Base URL 就能直接用，因此这里也照顾了常见的字段差异。
 */
class OpenAIProvider : AIProvider {

    override val type: AIProviderType = AIProviderType.OPENAI

    override suspend fun listModels(apiKey: String, baseUrl: String): List<AIModelInfo> {
        val text = getForText(
            url = "${baseUrl.trimEnd('/')}/models",
            headers = authHeaders(apiKey),
        )
        val root = AI_JSON.parseToJsonElement(text).jsonObject

        // 兼容三种常见返回：
        // OpenAI / 大多数中转：{"data":[{"id":..,"created":..}]}
        // Ollama：            {"models":[{"name":..},{"model":..}]}
        val array: JsonArray = (root["data"] as? JsonArray)
            ?: (root["models"] as? JsonArray)
            ?: JsonArray(emptyList())

        return array.mapNotNull { el ->
            val obj = el as? JsonObject ?: return@mapNotNull null
            val id = obj["id"]?.jsonPrimitive?.contentOrNull
                ?: obj["name"]?.jsonPrimitive?.contentOrNull
                ?: obj["model"]?.jsonPrimitive?.contentOrNull
                ?: return@mapNotNull null
            if (id.isBlank()) return@mapNotNull null
            AIModelInfo(
                id = id,
                displayName = id,
                contextLength = obj["context_length"]?.jsonPrimitive?.intOrNull,
                createdAt = obj["created"]?.jsonPrimitive?.longOrNull,
            )
        }.distinctBy { it.id }
            .sortedWith(compareByDescending<AIModelInfo> { it.sortKey }.thenBy { it.id })
    }

    override fun streamChat(request: AIChatRequest): Flow<AIStreamEvent> = flow {
        val body = buildJsonObject {
            put("model", request.model)
            put("stream", true)
            if (request.temperature > 0f) put("temperature", request.temperature)
            putJsonArray("messages") {
                request.systemPrompt?.takeIf { it.isNotBlank() }?.let { sys ->
                    addJsonObject {
                        put("role", "system")
                        put("content", sys)
                    }
                }
                request.messages.forEach { msg -> addOpenAIMessage(msg) }
            }
            if (request.tools.isNotEmpty()) {
                putJsonArray("tools") {
                    request.tools.forEach { tool ->
                        addJsonObject {
                            put("type", "function")
                            putJsonObject("function") {
                                put("name", tool.name)
                                put("description", tool.description)
                                put("parameters", tool.parameters)
                            }
                        }
                    }
                }
                put("tool_choice", "auto")
            }
        }

        var stopReason: String? = null

        postJsonSse(
            url = "${request.baseUrl.trimEnd('/')}/chat/completions",
            headers = authHeaders(request.apiKey),
            bodyJson = body,
        ).collect { payload ->
            // 结束标记
            if (payload == "[DONE]") return@collect

            val obj = runCatching { AI_JSON.parseToJsonElement(payload).jsonObject }.getOrNull()
                ?: return@collect

            // 流中错误事件
            obj["error"]?.let { err ->
                emit(AIStreamEvent.Failed(err.toString().take(500)))
                return@collect
            }

            val choice = (obj["choices"] as? JsonArray)?.firstOrNull()?.jsonObject ?: return@collect
            choice["finish_reason"]?.jsonPrimitive?.contentOrNull?.let { stopReason = it }

            val delta = choice["delta"]?.jsonObject ?: return@collect

            delta["content"]?.jsonPrimitive?.contentOrNull
                ?.takeIf { it.isNotEmpty() }
                ?.let { emit(AIStreamEvent.TextDelta(it)) }

            (delta["tool_calls"] as? JsonArray)?.forEach { el ->
                val tc = el as? JsonObject ?: return@forEach
                val index = tc["index"]?.jsonPrimitive?.intOrNull ?: 0
                val fn = tc["function"] as? JsonObject
                emit(
                    AIStreamEvent.ToolCallDelta(
                        index = index,
                        id = tc["id"]?.jsonPrimitive?.contentOrNull,
                        name = fn?.get("name")?.jsonPrimitive?.contentOrNull,
                        argumentsDelta = fn?.get("arguments")?.jsonPrimitive?.contentOrNull,
                    )
                )
            }
        }

        emit(AIStreamEvent.Completed(stopReason))
    }

    // ── 内部工具 ──────────────────────────────────────────────────

    private fun authHeaders(apiKey: String): Map<String, String> = mapOf(
        "Authorization" to "Bearer $apiKey",
    )

    /**
     * 把统一的 [AIMessage] 映射成 OpenAI 的 messages 元素。
     *
     * 注意：一条带多个工具结果的 TOOL 消息在 OpenAI 协议里要**拆成多条** tool 消息，
     * 所以这里是往数组里追加，可能追加多条。
     */
    private fun JsonArrayBuilder.addOpenAIMessage(msg: AIMessage) {
        when (msg.role) {
            AIRole.SYSTEM -> addJsonObject {
                put("role", "system")
                put("content", msg.text)
            }

            AIRole.USER -> addJsonObject {
                put("role", "user")
                put("content", msg.text)
            }

            AIRole.ASSISTANT -> addJsonObject {
                put("role", "assistant")
                // 只有工具调用、没有正文时，content 必须是 null
                if (msg.text.isNotBlank()) put("content", msg.text) else put("content", JsonNull)
                if (msg.toolCalls.isNotEmpty()) {
                    putJsonArray("tool_calls") {
                        msg.toolCalls.forEach { call ->
                            addJsonObject {
                                put("id", call.id)
                                put("type", "function")
                                putJsonObject("function") {
                                    put("name", call.name)
                                    put("arguments", call.arguments)
                                }
                            }
                        }
                    }
                }
            }

            AIRole.TOOL -> {
                // 一条消息里可能带多个工具结果 → 拆成多条 tool 消息
                msg.toolResults.forEach { result ->
                    addJsonObject {
                        put("role", "tool")
                        put("tool_call_id", result.toolCallId)
                        put("content", result.content)
                    }
                }
                // 兼容：没有结构化结果时，退化成普通用户消息
                if (msg.toolResults.isEmpty() && msg.text.isNotBlank()) {
                    addJsonObject {
                        put("role", "user")
                        put("content", msg.text)
                    }
                }
            }
        }
    }
}
