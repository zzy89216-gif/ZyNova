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

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

/**
 * 写工具参数 JSON Schema 的小工具。
 *
 * 不必手写一大坨 `buildJsonObject`，让工具定义保持可读。
 */
object AIToolSchema {

    /** 构造一个 object 类型的参数 Schema */
    fun objectSchema(
        properties: Map<String, JsonObject> = emptyMap(),
        required: List<String> = emptyList(),
        description: String? = null,
    ): JsonObject = buildJsonObject {
        put("type", "object")
        description?.let { put("description", it) }
        putJsonObject("properties") {
            properties.forEach { (name, schema) -> put(name, schema) }
        }
        put("required", buildJsonArray { required.forEach { add(JsonPrimitive(it)) } })
        // 明确禁止模型塞入未声明的参数，减少乱传参数导致的执行失败
        put("additionalProperties", false)
    }

    /** 字符串参数 */
    fun string(description: String, enumValues: List<String>? = null, default: String? = null): JsonObject =
        buildJsonObject {
            put("type", "string")
            put("description", description)
            default?.let { put("default", it) }
            enumValues?.let { values ->
                put("enum", buildJsonArray { values.forEach { add(JsonPrimitive(it)) } })
            }
        }

    /** 整数参数 */
    fun integer(description: String, default: Int? = null, minimum: Int? = null, maximum: Int? = null): JsonObject =
        buildJsonObject {
            put("type", "integer")
            put("description", description)
            default?.let { put("default", it) }
            minimum?.let { put("minimum", it) }
            maximum?.let { put("maximum", it) }
        }

    /** 布尔参数 */
    fun bool(description: String, default: Boolean? = null): JsonObject =
        buildJsonObject {
            put("type", "boolean")
            put("description", description)
            default?.let { put("default", it) }
        }

    /** 字符串数组参数 */
    fun stringArray(description: String): JsonObject =
        buildJsonObject {
            put("type", "array")
            put("description", description)
            putJsonObject("items") { put("type", "string") }
        }

    /** 空参数（无参工具）*/
    fun noArgs(): JsonObject = buildJsonObject {
        put("type", "object")
        putJsonObject("properties") { }
        put("required", buildJsonArray { })
    }

    /**
     * 「这一步要做什么」的概括字段
     *
     * 27.10.2 起**每个工具都必须带**：模型在调用工具前先用一句人话概括自己的动作，
     * 界面把它渲染成 `运行命令 · 概括` 的单行提示，而不是把整段工具参数和返回值铺出来。
     * 这个字段只在本地消费，执行前会被 [stripIntentSummary] 剥掉，不会传给工具实现。
     */
    fun intentSummaryField(): JsonObject = string(
        "一句话概括你**这一次**调用要做什么，用用户能看懂的中文写动作本身" +
                "（例如「读取设备信息」「在手机里搜索 Fabric API」「把整合包装到 1.20.1」）。" +
                "不要写工具名、不要复述参数、不要写成问题。"
    )

    /** 把 [intentSummaryField] 注入任意工具的 parameters Schema（幂等）*/
    fun JsonObject.withIntentSummaryField(): JsonObject {
        val properties = this["properties"] as? JsonObject ?: JsonObject(emptyMap())
        if (properties.containsKey(SUMMARY_KEY)) return this

        val required = (this["required"] as? JsonArray)
            ?.mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
            .orEmpty()

        return buildJsonObject {
            this@withIntentSummaryField.forEach { (key, value) ->
                if (key != "properties" && key != "required") put(key, value)
            }
            putJsonObject("properties") {
                properties.forEach { (name, schema) -> put(name, schema) }
                put(SUMMARY_KEY, intentSummaryField())
            }
            put("required", buildJsonArray {
                required.forEach { add(JsonPrimitive(it)) }
                add(JsonPrimitive(SUMMARY_KEY))
            })
        }
    }

    /**
     * 从模型给的参数里取出概括
     *
     * 老模型 / 中转可能会漏掉这个字段，此时返回空串，界面退化成「工具中文名」。
     */
    fun JsonObject.intentSummary(): String =
        ((this[SUMMARY_KEY] as? JsonPrimitive)?.contentOrNull ?: "").trim()

    /** 剥掉概括字段，得到真正要交给工具实现的参数 */
    fun JsonObject.stripIntentSummary(): JsonObject =
        JsonObject(filterKeys { it != SUMMARY_KEY })

    /** 概括字段的参数名 */
    const val SUMMARY_KEY = "summary"

    /**
     * 给工具定义补上「必须先写概括」的说明与参数
     *
     * 统一在提供给模型的那一层加，工具实现本身不用关心这件事。
     */
    fun AIToolSpec.withIntentSummary(): AIToolSpec = copy(
        description = description +
                "\n\n调用本工具前必须先想清楚这一步在做什么，" +
                "并用一句中文写进必填参数 summary；这句话会直接显示给用户当进度提示。",
        parameters = parameters.withIntentSummaryField()
    )
}
