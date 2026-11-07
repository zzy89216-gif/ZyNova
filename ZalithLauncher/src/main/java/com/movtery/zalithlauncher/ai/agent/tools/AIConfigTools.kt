/*
 * ZyNova Launcher
 * Copyright (C) 2026 zzy89216-gif and contributors
 * GPL-3.0-or-later
 */

package com.movtery.zalithlauncher.ai.agent.tools

import com.movtery.zalithlauncher.ai.AISettings
import com.movtery.zalithlauncher.ai.agent.AITool
import com.movtery.zalithlauncher.ai.agent.AIToolRisk
import com.movtery.zalithlauncher.ai.agent.AIToolSchema

/**
 * 「AI 自举配置」工具（议题 #45）。
 *
 * 用户在聊天框里直接说「我的 Key 是 sk-xxx，帮我配上」，AI 调这个工具写进本地加密存储，
 * **不需要用户自己去设置页翻**。返回值里会把这个值打码，避免又泄露进对话记录。
 */
internal fun aiConfigTools(): List<AITool> = listOf(
    aiTool(
        name = "update_ai_setting",
        description = """
            修改 AI 自身的设置并立即生效。用户把 API Key 直接发在聊天里时用它完成配置。
            可用字段：
            - `apiKey`：主对话模型的 API Key（DeepSeek 或兼容中转）
            - `baseUrl`：主对话模型的接口地址（兼容 OpenAI 协议）
            - `model`：模型 ID
            - `webSearch`：是否允许联网（true/false）
            ⚠️ 写入后要告诉用户「已保存」，**不要在回复里重复密钥原文**。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "field" to AIToolSchema.string(
                    "要修改的字段",
                    enumValues = listOf(
                        "apiKey", "baseUrl", "model", "webSearch"
                    )
                ),
                "value" to AIToolSchema.string("字段的新值（布尔写 true/false）"),
            ),
            required = listOf("field", "value"),
        ),
        risk = AIToolRisk.WRITE,
    ) { args, _ ->
        val field = args.strOr("field", "").trim()
        val value = args.str("value").orEmpty().trim()
        if (field.isBlank()) return@aiTool "field 不能为空"

        fun mask(v: String): String = when {
            v.length <= 6 -> "***"
            else -> v.take(3) + "***" + v.takeLast(2)
        }

        when (field) {
            "apiKey" -> { AISettings.apiKey.save(value); "已保存主模型 API Key（${mask(value)}）" }
            "baseUrl" -> { AISettings.baseUrl.save(value); "已保存接口地址：$value" }
            "model" -> { AISettings.model.save(value); "已切换模型：$value" }
            "webSearch" -> {
                val on = value.equals("true", true) || value == "1"
                AISettings.webSearch.save(on); "联网已${if (on) "开启" else "关闭"}"
            }
            else -> "不支持的字段：$field"
        }
    },
)
