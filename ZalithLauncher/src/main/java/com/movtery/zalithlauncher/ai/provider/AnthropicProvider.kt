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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

/**
 * Anthropic Claude（Messages API）实现。
 *
 * 与 OpenAI 的主要差异：
 * - 鉴权用 `x-api-key` + 必需的 `anthropic-version` 头
 * - 系统提示是**顶层 `system` 字段**，不是一条 message
 * - 工具调用/结果放在 content block 数组里（`tool_use` / `tool_result`）
 * - `max_tokens` 必填
 * - 流式事件是具名事件（`content_block_delta` 等），不再用 `[DONE]`
 */
class AnthropicProvider : AIProvider {

    override val type: AIProviderType = AIProviderType.ANTHROPIC

    override suspend fun listModels(apiKey: String, baseUrl: String): List<AIModelInfo> {
        val text = getForText(
            url = "${baseUrl.trimEnd('/')}/models",
            headers = authHeaders(apiKey),
        )
        val root = AI_JSON.parseToJsonElement(text).jsonObject
        val array = (root["data"] as? JsonArray) ?: JsonArray(emptyList())

        return array.mapNotNull { el ->
            val obj = el as? JsonObject ?: return@mapNotNull null
            val id = obj["id"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
            if (id.isBlank()) return@mapNotNull null
            AIModelInfo(
                id = id,
                displayName = obj["display_name"]?.jsonPrimitive?.contentOrNull ?: id,
            )
        }.distinctBy { it.id }.sortedBy { it.id }
    }

    override fun streamChat(request: AIChatRequest): Flow<AIStreamEvent> = flow {
        val body = buildJsonObject {
            put("model", request.model)
            put("stream", true)
            // Anthropic 必填；给一个足够大的默认值，避免回答被截断
            put("max_tokens", DEFAULT_MAX_TOKENS)
            if (request.temperature > 0f) put("temperature", request.temperature)
            request.systemPrompt?.takeIf { it.isNotBlank() }?.let { put("system", it) }

            putJsonArray("messages") {
                request.messages.forEach { msg -> addAnthropicMessage(msg) }
            }

            if (request.tools.isNotEmpty()) {
                putJsonArray("tools") {
                    request.tools.forEach { tool ->
                        addJsonObject {
                            put("name", tool.name)
                            put("description", tool.description)
                            put("input_schema", tool.parameters)
                        }
                    }
                }
            }
        }

        var stopReason: String? = null

        postJsonSse(
            url = "${request.baseUrl.trimEnd('/')}/messages",
            headers = authHeaders(request.apiKey),
            bodyJson = body,
        ).collect { payload ->
            val obj = runCatching { AI_JSON.parseToJsonElement(payload).jsonObject }.getOrNull()
                ?: return@collect

            when (obj["type"]?.jsonPrimitive?.contentOrNull) {
                // 错误事件
                "error" -> {
                    val message = obj["error"]?.jsonObject?.get("message")?.jsonPrimitive?.contentOrNull
                    emit(AIStreamEvent.Failed(message ?: obj["error"].toString().take(500)))
                    return@collect
                }

                // 模型开始吐工具名（id / name 在这里给全，参数在后续 delta 里分片）
                "content_block_start" -> {
                    val index = obj["index"]?.jsonPrimitive?.intOrNull ?: 0
                    val block = obj["content_block"]?.jsonObject
                    if (block?.get("type")?.jsonPrimitive?.contentOrNull == "tool_use") {
                        emit(
                            AIStreamEvent.ToolCallDelta(
                                index = index,
                                id = block["id"]?.jsonPrimitive?.contentOrNull,
                                name = block["name"]?.jsonPrimitive?.contentOrNull,
                            )
                        )
                    }
                }

                // 正文 / 工具参数增量
                "content_block_delta" -> {
                    val index = obj["index"]?.jsonPrimitive?.intOrNull ?: 0
                    val delta = obj["delta"]?.jsonObject
                    when (delta?.get("type")?.jsonPrimitive?.contentOrNull) {
                        "text_delta" -> delta["text"]?.jsonPrimitive?.contentOrNull
                            ?.takeIf { it.isNotEmpty() }
                            ?.let { emit(AIStreamEvent.TextDelta(it)) }

                        "input_json_delta" -> emit(
                            AIStreamEvent.ToolCallDelta(
                                index = index,
                                argumentsDelta = delta["partial_json"]?.jsonPrimitive?.contentOrNull,
                            )
                        )
                    }
                }

                // 结束原因
                "message_delta" -> {
                    obj["delta"]?.jsonObject?.get("stop_reason")?.jsonPrimitive?.contentOrNull
                        ?.let { stopReason = it }
                }

                "message_stop" -> return@collect
            }
        }

        emit(AIStreamEvent.Completed(stopReason))
    }

    // ── 内部工具 ──────────────────────────────────────────────────

    private fun authHeaders(apiKey: String): Map<String, String> = mapOf(
        "x-api-key" to apiKey,
        "anthropic-version" to ANTHROPIC_VERSION,
    )

    private fun JsonArrayBuilder.addAnthropicMessage(msg: AIMessage) {
        when (msg.role) {
            // 系统提示已经放到顶层 system 字段，这里跳过
            AIRole.SYSTEM -> Unit

            AIRole.USER -> addJsonObject {
                put("role", "user")
                putJsonArray("content") {
                    addJsonObject {
                        put("type", "text")
                        put("text", msg.text)
                    }
                }
            }

            AIRole.ASSISTANT -> addJsonObject {
                put("role", "assistant")
                putJsonArray("content") {
                    if (msg.text.isNotBlank()) {
                        addJsonObject {
                            put("type", "text")
                            put("text", msg.text)
                        }
                    }
                    msg.toolCalls.forEach { call ->
                        addJsonObject {
                            put("type", "tool_use")
                            put("id", call.id)
                            put("name", call.name)
                            // Anthropic 要求 input 是对象；参数非法时退化成空对象
                            put(
                                "input",
                                runCatching { AI_JSON.parseToJsonElement(call.arguments).jsonObject }
                                    .getOrElse { JsonObject(emptyMap()) }
                            )
                        }
                    }
                }
            }

            // 工具结果在 Anthropic 里是 user 角色的 tool_result block
            AIRole.TOOL -> addJsonObject {
                put("role", "user")
                putJsonArray("content") {
                    msg.toolResults.forEach { result ->
                        addJsonObject {
                            put("type", "tool_result")
                            put("tool_use_id", result.toolCallId)
                            put("content", result.content)
                            if (result.isError) put("is_error", true)
                        }
                    }
                    if (msg.toolResults.isEmpty() && msg.text.isNotBlank()) {
                        addJsonObject {
                            put("type", "text")
                            put("text", msg.text)
                        }
                    }
                }
            }
        }
    }

    private companion object {
        const val ANTHROPIC_VERSION = "2023-06-01"
        const val DEFAULT_MAX_TOKENS = 8192
    }
}
