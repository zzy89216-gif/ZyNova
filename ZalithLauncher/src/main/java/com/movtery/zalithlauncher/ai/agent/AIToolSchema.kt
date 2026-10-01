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

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
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
}
