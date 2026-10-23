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

    // ── 1. API Key / Base URL（固定 DeepSeek）─────────────────────
    /**
     * DeepSeek 的 API Key（用户自备，只存本机 MMKV）。
     *
     * 27.5.1 起不再提供「AI 服务商」选择：启动器只接 DeepSeek，
     * 接口地址固定为 [DEFAULT_BASE_URL]，用户可以自己改（兼容同类中转）。
     */
    val apiKey = stringSetting("aiDeepSeekKey", "")

    /** 当前选中的模型 ID；为空表示尚未选择（模型仍然动态获取） */
    val model = stringSetting("aiModel", "")

    /** DeepSeek 的 API 地址（可换成兼容 OpenAI 协议的中转地址） */
    val baseUrl = stringSetting("aiDeepSeekBaseUrl", DEFAULT_BASE_URL)

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

    /** DeepSeek 官方接口地址 */
    const val DEFAULT_BASE_URL = "https://api.deepseek.com/v1"

    /** 取 API Key */
    fun getKey(): String = apiKey.getValue().trim()

    /** 取 Base URL（去掉尾部斜杠；留空回退到默认地址） */
    fun getBaseUrl(): String =
        baseUrl.getValue().trim().trimEnd('/').ifEmpty { DEFAULT_BASE_URL }

    /** 保存 API Key */
    fun saveKey(value: String) {
        apiKey.save(value.trim())
    }

    /** 保存 Base URL（留空则回退到默认值） */
    fun saveBaseUrl(value: String) {
        baseUrl.save(value.trim().trimEnd('/').ifEmpty { DEFAULT_BASE_URL })
    }

    /** 是否已填 API Key */
    fun hasKey(): Boolean = getKey().isNotBlank()

    /** 是否已选好模型 */
    fun hasModel(): Boolean = model.getValue().isNotBlank()

    /** 是否已经可以发起对话 */
    fun isReady(): Boolean = hasKey() && hasModel()
}
