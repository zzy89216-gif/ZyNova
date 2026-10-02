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

package com.movtery.zalithlauncher.ai

import com.movtery.zalithlauncher.ai.provider.AIProviderType
import com.movtery.zalithlauncher.setting.SettingsRegistry

/**
 * AI Agent 的独立配置
 *
 * ⚠️ 刻意**不并入** [com.movtery.zalithlauncher.setting.AllSettings]：
 * AI 配置有独立入口（聊天界面右上角），不出现在启动器通用设置里。
 * 这里只是复用同一套 [SettingsRegistry] 存储机制，键名统一加 `ai` 前缀避免冲突。
 *
 * API Key **只保存在本地 MMKV**，不会上传到 ZyNova 的任何自有服务器。
 */
object AISettings : SettingsRegistry() {

    // ── 1. AI Provider ─────────────────────────────────────────────
    /** 当前使用的 Provider 类型（可扩展） */
    val provider = enumSetting("aiProvider", AIProviderType.OPENAI)

    // ── 2. API Key（用户自备，本地保存）────────────────────────────
    /** OpenAI（及任何 OpenAI 兼容服务）的 API Key */
    val openAIKey = stringSetting("aiOpenAIKey", "")

    /** Anthropic 的 API Key */
    val anthropicKey = stringSetting("aiAnthropicKey", "")

    // ── 3. Model（不硬编码；从 Provider 动态获取后由用户选择）──────
    /** 当前选中的模型 ID；为空表示尚未选择 */
    val model = stringSetting("aiModel", "")

    // ── 4. Base URL（有默认值，可自定义）──────────────────────────
    /** OpenAI 的 API 地址（可换成任何 OpenAI 兼容服务）*/
    val openAIBaseUrl = stringSetting("aiOpenAIBaseUrl", AIProviderType.OPENAI.defaultBaseUrl)

    /** Anthropic 的 API 地址 */
    val anthropicBaseUrl = stringSetting("aiAnthropicBaseUrl", AIProviderType.ANTHROPIC.defaultBaseUrl)

    // ── 5. Agent 权限模式 ─────────────────────────────────────────
    /**
     * 工具调用权限：完全控制 / 操作确认
     *
     * ⚠️ 默认值是 [AIPermissionMode.CONFIRM]（操作确认）。
     * 理由：Agent 能改 118 项设置、删模组、写文件、装资源，
     * 而「完全控制 + 无轮数上限」的组合下，模型若陷入循环用户可能来不及注意。
     * 每次写操作弹一次确认是最便宜的保险；用户信任建立后可自行切到「完全控制」。
     */
    val permissionMode = enumSetting("aiPermissionMode", AIPermissionMode.CONFIRM)

    // ── 其它 ──────────────────────────────────────────────────────
    /** 采样温度 */
    val temperature = floatSetting("aiTemperature", 0.7f, 0f..2f)

    /** 是否在聊天流里显示工具调用过程 */
    val showToolCalls = boolSetting("aiShowToolCalls", true)

    /**
     * 是否已经向用户展示过「数据会发往你配置的 AI 服务商」这段说明。
     *
     * 只在 AI 配置页**首次**打开时弹一次，之后不再打扰。
     */
    val privacyNoticeShown = boolSetting("aiPrivacyNoticeShown", false)

    // ── 便捷读取 ──────────────────────────────────────────────────

    /** 取指定 Provider 的 API Key */
    fun getKey(type: AIProviderType = provider.getValue()): String = when (type) {
        AIProviderType.OPENAI -> openAIKey.getValue()
        AIProviderType.ANTHROPIC -> anthropicKey.getValue()
    }

    /** 取指定 Provider 的 Base URL（去掉尾部斜杠）*/
    fun getBaseUrl(type: AIProviderType = provider.getValue()): String =
        when (type) {
            AIProviderType.OPENAI -> openAIBaseUrl.getValue()
            AIProviderType.ANTHROPIC -> anthropicBaseUrl.getValue()
        }.trim().trimEnd('/').ifEmpty { type.defaultBaseUrl }

    /** 保存指定 Provider 的 API Key */
    fun saveKey(type: AIProviderType, value: String) {
        val v = value.trim()
        when (type) {
            AIProviderType.OPENAI -> openAIKey.save(v)
            AIProviderType.ANTHROPIC -> anthropicKey.save(v)
        }
    }

    /** 保存指定 Provider 的 Base URL（留空则回退到默认值）*/
    fun saveBaseUrl(type: AIProviderType, value: String) {
        val v = value.trim().trimEnd('/').ifEmpty { type.defaultBaseUrl }
        when (type) {
            AIProviderType.OPENAI -> openAIBaseUrl.save(v)
            AIProviderType.ANTHROPIC -> anthropicBaseUrl.save(v)
        }
    }

    /** 当前 Provider 是否已填 API Key */
    fun hasKey(type: AIProviderType = provider.getValue()): Boolean = getKey(type).isNotBlank()

    /** 当前 Provider 是否已选好模型 */
    fun hasModel(): Boolean = model.getValue().isNotBlank()

    /** 是否已经可以发起对话 */
    fun isReady(type: AIProviderType = provider.getValue()): Boolean = hasKey(type) && hasModel()

    /** 切换 Provider 时清空已选模型，避免把 A 家的模型名带到 B 家 */
    fun switchProvider(type: AIProviderType) {
        if (provider.getValue() != type) {
            provider.save(type)
            model.save("")
        }
    }
}
