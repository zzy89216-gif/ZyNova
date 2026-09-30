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
 * 内置支持的 AI Provider 类型。
 *
 * 新增 Provider 只需要：
 * 1. 在这里加一个枚举值（带默认 Base URL）
 * 2. 实现 [AIProvider] 并在 [AIProviders] 里注册
 *
 * ⚠️ **不要**在这里硬编码模型列表：模型一律通过 Provider 的模型列表接口动态获取。
 */
enum class AIProviderType(
    /** 界面上显示的固定名称（不翻译，保持厂商原名）*/
    val displayName: String,
    /** 默认 API 地址 */
    val defaultBaseUrl: String,
) {
    /** OpenAI 官方，以及所有 OpenAI 兼容的第三方服务（自定义 Base URL 即可）*/
    OPENAI("OpenAI", "https://api.openai.com/v1"),

    /** Anthropic Claude */
    ANTHROPIC("Anthropic", "https://api.anthropic.com/v1");

    companion object {
        fun fromName(name: String?): AIProviderType =
            entries.firstOrNull { it.name == name } ?: OPENAI
    }
}
