/*
 * ZyNova Launcher
 * Copyright (C) 2026 zzy89216-gif and contributors
 * GPL-3.0-or-later
 */

package com.movtery.zalithlauncher.ai.agent.tools

import com.movtery.zalithlauncher.ai.AISearchEngine
import com.movtery.zalithlauncher.ai.AISettings
import com.movtery.zalithlauncher.ai.agent.AITool
import com.movtery.zalithlauncher.ai.agent.AIToolRisk
import com.movtery.zalithlauncher.ai.agent.AIToolSchema
import com.movtery.zalithlauncher.ai.aiString
import java.util.Locale

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
            - `searchEngine`：联网搜索用哪个引擎（`deepseek` / `duckduckgo`，大小写与空格不敏感）
            - `searchKey`：DeepSeek 官方搜索专用 Key；留空表示清空、改为复用主模型 Key
            ⚠️ 写入后要告诉用户「已保存」，**不要在回复里重复密钥原文**。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "field" to AIToolSchema.string(
                    "要修改的字段",
                    enumValues = listOf(
                        "apiKey", "baseUrl", "model", "webSearch", "searchEngine", "searchKey"
                    )
                ),
                "value" to AIToolSchema.string("字段的新值（布尔写 true/false）"),
            ),
            required = listOf("field", "value"),
        ),
        risk = AIToolRisk.WRITE,
    ) { args, _ ->
        val rawField = args.strOr("field", "").trim()
        if (rawField.isBlank()) return@aiTool "field 不能为空"
        //字段名容错：忽略大小写与下划线 / 连字符（模型可能写成 search_engine、web_search）
        val field = rawField.lowercase(Locale.ROOT).replace("_", "").replace("-", "")
        val value = args.str("value").orEmpty().trim()

        fun mask(v: String): String = when {
            v.length <= 6 -> "***"
            else -> v.take(3) + "***" + v.takeLast(2)
        }

        when (field) {
            "apikey" -> {
                //saveKey 会 trim；空值表示清空
                AISettings.saveKey(value)
                if (value.isBlank()) "已清空主模型 API Key"
                else "已保存主模型 API Key（${mask(value)}）"
            }
            "baseurl" -> {
                //saveBaseUrl 会去掉尾部斜杠，留空回退官方地址
                AISettings.saveBaseUrl(value)
                "已保存接口地址：${AISettings.getBaseUrl()}"
            }
            "model" -> { AISettings.model.save(value); "已切换模型：$value" }
            "websearch" -> {
                val on = value.lowercase(Locale.ROOT) in setOf("true", "1", "on", "yes", "开", "开启")
                AISettings.webSearch.save(on); "联网已${if (on) "开启" else "关闭"}"
            }
            "searchengine" -> {
                if (value.isBlank()) return@aiTool "searchEngine 需要指定 deepseek 或 duckduckgo"
                //容错：忽略大小写与空格，兼容 deepseek / ds / duckduckgo / ddg 等口语写法；
                //不认识的值要明确报错，不能像 fromName() 那样默默回退成 DuckDuckGo
                val engine = when (value.uppercase(Locale.ROOT).filterNot { it.isWhitespace() }) {
                    "DEEPSEEK", "DS", "DEEPSEEKSEARCH" -> AISearchEngine.DEEPSEEK
                    "DUCKDUCKGO", "DDG" -> AISearchEngine.DUCKDUCKGO
                    else -> null
                } ?: return@aiTool "不支持的搜索引擎：$value（可选 deepseek / duckduckgo）"
                AISettings.searchEngine.save(engine)
                "搜索引擎已切换为${aiString(engine.labelRes)}"
            }
            "searchkey" -> {
                AISettings.searchApiKey.save(value)
                if (value.isBlank()) "已清空 DeepSeek 搜索 Key（将复用主模型 Key）"
                else "已保存 DeepSeek 搜索 Key（${mask(value)}）"
            }
            else -> "不支持的字段：$rawField"
        }
    },
)
