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

package com.movtery.zalithlauncher.ai.agent.tools

import com.movtery.zalithlauncher.ai.agent.AITool
import com.movtery.zalithlauncher.ai.agent.AIToolContext
import com.movtery.zalithlauncher.ai.agent.AIToolRisk
import com.movtery.zalithlauncher.ai.agent.AIToolSpec
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

/** 构造一个工具；把样板代码收敛在这里 */
internal fun aiTool(
    name: String,
    description: String,
    parameters: JsonObject,
    risk: AIToolRisk = AIToolRisk.READ,
    execute: suspend (args: JsonObject, context: AIToolContext) -> String,
): AITool = object : AITool {
    override val spec: AIToolSpec = AIToolSpec(name, description, parameters)
    override val risk: AIToolRisk = risk
    override suspend fun execute(args: JsonObject, context: AIToolContext): String =
        execute(args, context)
}

// ── 参数读取辅助（模型经常给错类型，这里统一容错）────────────────────

internal fun JsonObject.str(key: String): String? =
    this[key]?.jsonPrimitive?.contentOrNull?.trim()?.takeIf { it.isNotEmpty() }

internal fun JsonObject.strOr(key: String, fallback: String): String = str(key) ?: fallback

internal fun JsonObject.bool(key: String): Boolean? =
    this[key]?.jsonPrimitive?.booleanOrNull
        ?: this[key]?.jsonPrimitive?.contentOrNull?.let {
            when (it.trim().lowercase()) {
                "true", "1", "yes", "on" -> true
                "false", "0", "no", "off" -> false
                else -> null
            }
        }

internal fun JsonObject.int(key: String): Int? =
    this[key]?.jsonPrimitive?.intOrNull
        ?: this[key]?.jsonPrimitive?.contentOrNull?.trim()?.toIntOrNull()
