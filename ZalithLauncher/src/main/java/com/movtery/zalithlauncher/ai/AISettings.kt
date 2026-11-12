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
import com.movtery.zalithlauncher.setting.launcherMMKV

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
     * 27.8.0 起默认是 [AIPermissionMode.FULL_CONTROL]（完全控制）。
     *
     * 理由：ZyNova 的 AI 定位是 **Agent**，不是聊天机器人 —— 用户说「帮我装个 1.20.1 Fabric」，
     * 它就应该直接把游戏装上，而不是每装一步弹一次确认、弹到用户放弃。
     * 「每步都问」会让 Agent 退化成「只会给建议的聊天框」，这正好是它要解决的问题。
     *
     * 代价与控制手段（都保留着，没有删）：
     * - 仍然有「操作确认」模式，随时可以在 AI 设置里切回去；
     * - Agent 循环有死循环检测（同一工具 + 完全相同的参数反复调用会被判定为死循环并停止）；
     * - 用户随时可以点「停止」中断这一轮；
     * - 所有写操作都会写进 AI 审计日志（[com.movtery.zalithlauncher.ai.AIAuditLog]）。
     */
    val permissionMode = enumSetting("aiPermissionMode", AIPermissionMode.FULL_CONTROL)

    // ── 其它 ──────────────────────────────────────────────────────
    /** 采样温度 */
    val temperature = floatSetting("aiTemperature", 0.7f, 0f..2f)

    /** 是否在聊天流里显示工具调用过程 */
    val showToolCalls = boolSetting("aiShowToolCalls", true)

    // ── 网络能力（议题 #45）────────────────────────────────────────
    /**
     * 是否允许 AI 联网（搜索 / 抓取网页）。
     *
     * 默认开启：AI 排障时「查一下这个报错」是刚需；关闭后网络工具会直接拒绝执行。
     * 抓取只允许公网 http/https，禁止内网地址（防 SSRF）。
     */
    val webSearch = boolSetting("aiWebSearch", true)

    /** 联网搜索用哪个引擎（DeepSeek 官方 Search / DuckDuckGo 网页） */
    val searchEngine = enumSetting("aiSearchEngine", AISearchEngine.DUCKDUCKGO)

    /**
     * DeepSeek Search 专用 Key。
     *
     * 主模型就是 DeepSeek 时留空即可，会自动复用 [apiKey]；
     * 只有主模型是别家时才需要单独填。
     */
    val searchApiKey = stringSetting("aiSearchApiKey", "")


    /**
     * 是否已经向用户展示过「数据会发往你配置的 AI 服务商」这段说明。
     *
     * 只在 AI 配置页**首次**打开时弹一次，之后不再打扰。
     */
    val privacyNoticeShown = boolSetting("aiPrivacyNoticeShown", false)

    /**
     * 老用户升级上来时，把权限模式切到「完全控制」。
     *
     * **只做一次**（MMKV 里留标记）：用户之后手动切回「操作确认」不会再被覆盖。
     * 不做这一步的话，27.7.x 及更早版本装过的设备里已经存着 `CONFIRM`，
     * 改默认值对它们毫无影响，Agent 依旧每步都要人点头。
     */
    private const val KEY_PERMISSION_MIGRATED = "aiPermissionModeMigratedToFull"

    private fun migratePermissionModeOnce() {
        runCatching {
            val mmkv = launcherMMKV()
            if (mmkv.getBoolean(KEY_PERMISSION_MIGRATED, false)) return
            permissionMode.save(AIPermissionMode.FULL_CONTROL)
            mmkv.putBoolean(KEY_PERMISSION_MIGRATED, true)
        }
    }

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

    init {
        migratePermissionModeOnce()
    }
}
