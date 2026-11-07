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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */

package com.movtery.zalithlauncher.ai.agent.tools

import com.movtery.zalithlauncher.ai.AISettings
import com.movtery.zalithlauncher.ai.agent.AITool
import com.movtery.zalithlauncher.ai.agent.AIToolRisk
import com.movtery.zalithlauncher.ai.agent.AIToolSchema
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * AI 的网络能力（议题 #45 第一批）。
 *
 * 设计原则：
 * - **只读**：这三个工具都是 [AIToolRisk.READ]，不会改任何本地文件；
 * - **可控**：设置里可以整体关掉「网络搜索」，关掉后工具直接返回「用户已关闭」；
 * - **防 SSRF**：`fetch_webpage` 只允许公网 http/https，禁止 localhost / 内网 / 链路本地地址；
 * - **有上限**：单次返回内容截断到 8KB，避免把上下文撑爆。
 */
private const val TAG = "AIToolWeb"
private const val MAX_BODY_CHARS = 8_000
private const val USER_AGENT =
    "Mozilla/5.0 (Linux; Android 15) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Mobile Safari/537.36"

private val httpClient: OkHttpClient by lazy {
    OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(18, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()
}

internal fun webTools(): List<AITool> = listOf(
    aiTool(
        name = "web_search",
        description = """
            联网搜索（DuckDuckGo 网页版）。用于查模组报错、崩溃信息、版本兼容性等本机资料里没有的内容。
            返回前若干条结果的标题 + 链接 + 摘要。需要看正文时再用 fetch_webpage。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "query" to AIToolSchema.string("搜索关键词，中英文都行"),
                "limit" to AIToolSchema.integer("返回条数，默认 5，最大 10"),
            ),
            required = listOf("query")
        ),
        risk = AIToolRisk.READ,
    ) { args, _ ->
        if (!AISettings.webSearch.state) return@aiTool "用户已关闭网络搜索（设置 → AI 设置 → 网络搜索）"
        val query = args.strOr("query", "").trim()
        if (query.isEmpty()) return@aiTool "query 不能为空"
        val limit = (args.int("limit") ?: 5).coerceIn(1, 10)
        runCatching { searchWeb(query, limit) }
            .getOrElse { e ->
                Logger.warning(TAG, "web_search failed", e)
                "搜索失败：${e.message ?: e::class.simpleName}"
            }
    },

    aiTool(
        name = "fetch_webpage",
        description = """
            抓取一个网页并转成纯文本（去掉脚本、样式、HTML 标签），最多返回 8000 字。
            只允许公网 http/https 地址，禁止内网与 localhost（防 SSRF）。
            用来读搜索结果的具体页面、模组文档、崩溃解决方案。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf("url" to AIToolSchema.string("要抓取的网页地址，http/https")),
            required = listOf("url")
        ),
        risk = AIToolRisk.READ,
    ) { args, _ ->
        if (!AISettings.webSearch.state) return@aiTool "用户已关闭网络搜索（设置 → AI 设置 → 网络搜索）"
        val raw = args.strOr("url", "").trim()
        if (raw.isEmpty()) return@aiTool "url 不能为空"
        val blocked = ssrfReason(raw)
        if (blocked != null) return@aiTool blocked
        runCatching { fetchAsText(raw) }
            .getOrElse { e ->
                Logger.warning(TAG, "fetch_webpage failed", e)
                "抓取失败：${e.message ?: e::class.simpleName}"
            }
    },

    aiTool(
        name = "search_mcmod",
        description = "在 MC 百科（mcmod.cn）里搜索模组 / 整合包，返回标题、链接与摘要。查中文模组资料优先用它。",
        parameters = AIToolSchema.objectSchema(
            properties = mapOf("query" to AIToolSchema.string("模组或整合包名字")),
            required = listOf("query")
        ),
        risk = AIToolRisk.READ,
    ) { args, _ ->
        if (!AISettings.webSearch.state) return@aiTool "用户已关闭网络搜索（设置 → AI 设置 → 网络搜索）"
        val query = args.strOr("query", "").trim()
        if (query.isEmpty()) return@aiTool "query 不能为空"
        val url = "https://search.mcmod.cn/s?key=" + java.net.URLEncoder.encode(query, "UTF-8")
        runCatching {
            val html = get(url)
            val items = Regex("<a[^>]+href=\"(//www\\.mcmod\\.cn/class/[^\"]+)\"[^>]*>(.*?)</a>", RegexOption.DOT_MATCHES_ALL)
                .findAll(html)
                .map { m -> cleanText(m.groupValues[2]) to "https:" + m.groupValues[1] }
                .filter { it.first.isNotBlank() }
                .distinctBy { it.second }
                .take(8)
                .toList()
            if (items.isEmpty()) "MC 百科没有搜到结果，或者页面结构变了：$url"
            else buildString {
                append("MC 百科搜索：").append(query).append('\n')
                items.forEach { (title, link) -> append("- ").append(title).append('\n').append("  ").append(link).append('\n') }
            }
        }.getOrElse { e ->
            Logger.warning(TAG, "search_mcmod failed", e)
            "MC 百科搜索失败：${e.message ?: e::class.simpleName}"
        }
    },
)

/**
 * 按用户选择的引擎搜索。
 *
 * - `duckduckgo`：免 Key，抓网页版结果；
 * - `baidu`：百度千帆 AI 搜索（需要 API Key，控制台地址见设置页的「创建 API」）；
 * - `bing`：Azure Bing Web Search（需要订阅 Key）；
 * - `custom`：自建兼容接口，返回 JSON 里带标题/链接/摘要即可。
 *
 * 任何一家失败都会**回退到 DuckDuckGo**，保证「搜得到」比「用得对」更重要。
 */
private fun searchWeb(query: String, limit: Int): String {
    val engine = AISettings.searchEngine.state
    val key = AISettings.searchApiKey.state.trim()
    val baseUrl = AISettings.searchBaseUrl.state.trim()

    val result = when (engine) {
        "baidu" -> runCatching { searchBaidu(query, limit, key) }.getOrNull()
        "bing" -> runCatching { searchBing(query, limit, key) }.getOrNull()
        "custom" -> runCatching { searchCustom(query, limit, baseUrl, key) }.getOrNull()
        else -> null
    }
    if (!result.isNullOrBlank()) return result

    //兜底：免 Key 的 DuckDuckGo
    return searchDuckDuckGo(query, limit)
}

private fun searchDuckDuckGo(query: String, limit: Int): String {
    val url = "https://html.duckduckgo.com/html/?q=" + java.net.URLEncoder.encode(query, "UTF-8")
    val html = get(url)
    val items = Regex(
        "<a[^>]+class=\"result__a\"[^>]+href=\"([^\"]+)\"[^>]*>(.*?)</a>",
        setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)
    ).findAll(html).map { m ->
        val link = m.groupValues[1].replace("&amp;", "&")
        cleanText(m.groupValues[2]) to link
    }.filter { it.first.isNotBlank() && it.second.startsWith("http") }
        .distinctBy { it.second }
        .take(limit)
        .toList()

    if (items.isEmpty()) return "没有搜到结果（可能被搜索源限流）：$query"
    return buildString {
        append("搜索（DuckDuckGo）：").append(query).append('\n')
        items.forEach { (title, link) -> append("- ").append(title).append('\n').append("  ").append(link).append('\n') }
    }
}

/** 百度千帆 AI 搜索 */
private fun searchBaidu(query: String, limit: Int, apiKey: String): String? {
    if (apiKey.isBlank()) return null
    val body = """{"messages":[{"content":"$query","role":"user"}],"search_source":"baidu_search_v2","resource_type_filter":[{"type":"web","top_k":$limit}]}"""
    val json = postJson(
        url = "https://qianfan.baidubce.com/v2/ai_search/web_search",
        body = body,
        headers = mapOf("Authorization" to "Bearer $apiKey")
    )
    return formatJsonResults("百度千帆", query, json)?.takeIf { it.isNotBlank() }
}

/** Azure Bing Web Search */
private fun searchBing(query: String, limit: Int, apiKey: String): String? {
    if (apiKey.isBlank()) return null
    val url = "https://api.bing.microsoft.com/v7.0/search?count=$limit&q=" +
            java.net.URLEncoder.encode(query, "UTF-8")
    val json = get(url, mapOf("Ocp-Apim-Subscription-Key" to apiKey))
    return formatJsonResults("Bing", query, json)
}

/** 自建兼容接口：GET {base}?q=xxx，返回 JSON（含 title/url/snippet 即可）*/
private fun searchCustom(query: String, limit: Int, baseUrl: String, apiKey: String): String? {
    if (baseUrl.isBlank()) return null
    val sep = if (baseUrl.contains('?')) '&' else '?'
    val url = baseUrl + sep + "q=" + java.net.URLEncoder.encode(query, "UTF-8") + "&limit=$limit"
    val json = get(url, if (apiKey.isBlank()) emptyMap() else mapOf("Authorization" to "Bearer $apiKey"))
    return formatJsonResults("自定义", query, json)
}

/**
 * 把各家搜索结果统一成「标题 + 链接」列表。
 *
 * 三家返回字段名不一样（title/name、url/link、snippet/description），
 * 这里直接从 JSON 文本里按字段名抓，避免为每家建一套数据类。
 */
private fun formatJsonResults(label: String, query: String, json: String): String? {
    val titles = Regex("\"(?:title|name)\"\\s*:\\s*\"([^\"]{2,120})\"").findAll(json).map { it.groupValues[1] }.toList()
    val links = Regex("\"(?:url|link)\"\\s*:\\s*\"(https?://[^\"]+)\"").findAll(json).map { it.groupValues[1] }.toList()
    val snippets = Regex("\"(?:snippet|description|content)\"\\s*:\\s*\"([^\"]{2,200})\"").findAll(json).map { it.groupValues[1] }.toList()
    if (titles.isEmpty() && links.isEmpty()) return null
    return buildString {
        append("搜索（").append(label).append("）：").append(query).append('\n')
        links.forEachIndexed { index, link ->
            append("- ").append(titles.getOrElse(index) { link }).append('\n')
            snippets.getOrNull(index)?.let { append("  ").append(it).append('\n') }
            append("  ").append(link).append('\n')
        }
    }
}

private fun postJson(url: String, body: String, headers: Map<String, String>): String {
    val builder = Request.Builder()
        .url(url)
        .post(okhttp3.RequestBody.create("application/json".toMediaTypeOrNull(), body))
        .header("User-Agent", USER_AGENT)
    headers.forEach { (k, v) -> builder.header(k, v) }
    httpClient.newCall(builder.build()).execute().use { response ->
        if (!response.isSuccessful) error("HTTP ${response.code}")
        return response.body?.string().orEmpty()
    }
}

private fun get(url: String, headers: Map<String, String> = emptyMap()): String {
    val builder = Request.Builder()
        .url(url)
        .header("User-Agent", USER_AGENT)
        .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
    headers.forEach { (k, v) -> builder.header(k, v) }
    val request = builder.build()
    httpClient.newCall(request).execute().use { response ->
        if (!response.isSuccessful) error("HTTP ${response.code}")
        return response.body?.string().orEmpty()
    }
}

private suspend fun fetchAsText(url: String): String = withContext(Dispatchers.IO) {
    val html = get(url)
    val text = htmlToText(html)
    if (text.isBlank()) "页面里没有可读文本（可能是纯前端渲染）"
    else "来源：$url\n\n" + text.take(MAX_BODY_CHARS)
}

/** 粗略的 HTML → 纯文本：去脚本样式、去标签、解实体、压空行 */
private fun htmlToText(html: String): String {
    var s = html
    s = s.replace(Regex("(?is)<script[^>]*>.*?</script>"), " ")
    s = s.replace(Regex("(?is)<style[^>]*>.*?</style>"), " ")
    s = s.replace(Regex("(?is)<noscript[^>]*>.*?</noscript>"), " ")
    s = s.replace(Regex("(?i)<br\\s*/?>"), "\n")
    s = s.replace(Regex("(?i)</(p|div|li|h[1-6]|tr)>"), "\n")
    s = s.replace(Regex("(?s)<[^>]+>"), " ")
    s = unescapeEntities(s)
    s = s.lines().map { it.replace(Regex("[ \\t\\u00A0]+"), " ").trim() }
        .filter { it.isNotEmpty() }
        .joinToString("\n")
    return s
}

private fun cleanText(html: String): String = unescapeEntities(html.replace(Regex("(?s)<[^>]+>"), " ")).trim()

private fun unescapeEntities(s: String): String = s
    .replace("&nbsp;", " ")
    .replace("&amp;", "&")
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&quot;", "\"")
    .replace("&#39;", "'")
    .replace("&apos;", "'")

/**
 * SSRF 防护：只放行公网 http/https。
 *
 * @return 被拒绝的原因；`null` 表示可以抓
 */
private fun ssrfReason(raw: String): String? {
    val url = runCatching { URL(raw) }.getOrNull() ?: return "地址格式不对：$raw"
    if (url.protocol !in listOf("http", "https")) return "只支持 http / https"
    val host = url.host.lowercase()
    if (host.isBlank()) return "地址里没有主机名"
    if (host == "localhost" || host.endsWith(".local") || host.endsWith(".internal")) {
        return "出于安全考虑，禁止访问本机或内网地址：$host"
    }
    // IP 字面量：拒绝内网 / 回环 / 链路本地
    val ip = host.trim('[', ']')
    if (ip.matches(Regex("\\d{1,3}(\\.\\d{1,3}){3}"))) {
        val parts = ip.split('.').mapNotNull { it.toIntOrNull() }
        if (parts.size == 4) {
            val (a, b) = parts[0] to parts[1]
            val private = a == 10 || a == 127 || a == 0 ||
                    (a == 172 && b in 16..31) || (a == 192 && b == 168) || (a == 169 && b == 254)
            if (private) return "出于安全考虑，禁止访问内网地址：$ip"
        }
    }
    if (ip.startsWith("fe80") || ip.startsWith("fc") || ip.startsWith("fd") || ip == "::1") {
        return "出于安全考虑，禁止访问内网地址：$ip"
    }
    return null
}
