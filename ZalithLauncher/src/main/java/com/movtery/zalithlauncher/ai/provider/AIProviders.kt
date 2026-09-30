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

/**
 * Provider 注册表。
 *
 * 新增 Provider：在 [AIProviderType] 加枚举 → 实现 [AIProvider] → 在 [builtin] 里加一行。
 */
object AIProviders {

    private val builtin: List<AIProvider> = listOf(
        OpenAIProvider(),
        AnthropicProvider(),
    )

    private val registry: Map<AIProviderType, AIProvider> = builtin.associateBy { it.type }

    /** 界面上可选的 Provider（按枚举顺序）*/
    val available: List<AIProviderType> = AIProviderType.entries.toList()

    /** 取 Provider 实现 */
    fun get(type: AIProviderType): AIProvider =
        registry[type] ?: throw IllegalStateException("未注册的 AI Provider: $type")

    private val builtinList = builtin
    val implementations: List<AIProvider> get() = builtinList
}
