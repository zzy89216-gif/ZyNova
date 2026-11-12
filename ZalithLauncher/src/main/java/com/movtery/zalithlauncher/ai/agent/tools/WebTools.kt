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
import com.movtery.zalithlauncher.ai.AISearchEngine
import com.movtery.zalithlauncher.ai.agent.AIToolRisk
import com.movtery.zalithlauncher.ai.agent.AIToolSchema
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
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
        //⚠️ 必须在 IO 线程：OkHttp 的 socket 连接不能跑在主线程，
        //否则抛 NetworkOnMainThreadException（28.0.5 真机 100% 复现）。
        withContext(Dispatchers.IO) {
            runCatching { searchWeb(query, limit) }
                .getOrElse { e ->
                    Logger.warning(TAG, "web_search failed", e)
                    "搜索失败：${e.message ?: e::class.simpleName}"
                }
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
        withContext(Dispatchers.IO) {
            runCatching { fetchAsText(raw) }
                .getOrElse { e ->
                    Logger.warning(TAG, "fetch_webpage failed", e)
                    "抓取失败：${e.message ?: e::class.simpleName}"
                }
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
        withContext(Dispatchers.IO) {
        runCatching {
            val html = get(url)
            //mcmod 限流时返回 200 + 「搜索太频繁」，必须单独识别，别混成「没有结果」
            if (html.contains("搜索太频繁") || html.contains("请稍后再试")) {
                return@runCatching "MC 百科正在限流（提示「搜索太频繁」），过一会儿再试"
            }
            //链接可能是 //www.mcmod.cn/class/…、https://… 或 /class/…
            val items = Regex("""href="((?:https?:)?//(?:www\.)?mcmod\.cn/(?:class|modpack)/\d+\.html|/(?:class|modpack)/\d+\.html)"[^>]*>(.*?)</a>""",
                setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
                .findAll(html)
                .map { m ->
                    val link = m.groupValues[1].let {
                        when {
                            it.startsWith("//") -> "https:$it"
                            it.startsWith("/") -> "https://www.mcmod.cn$it"
                            else -> it
                        }
                    }
                    cleanText(m.groupValues[2]) to link
                }
                .filter { it.first.isNotBlank() }
                .distinctBy { it.second }
                .take(8)
                .toList()

            when {
                items.isNotEmpty() -> buildString {
                    append("MC 百科搜索：").append(query).append('\n')
                    items.forEach { (title, link) ->
                        append("- ").append(title).append('\n').append("  ").append(link).append('\n')
                    }
                }
                html.contains("找到约 0 条") || html.contains("全部 (0)") -> "MC 百科没有匹配结果：$query"
                else -> "MC 百科页面已返回，但没解析到结果（结构可能变了）。可改用 fetch_webpage 抓：$url"
            }
        }.getOrElse { e ->
            Logger.warning(TAG, "search_mcmod failed", e)
            "MC 百科搜索失败：${e.message ?: e::class.simpleName}"
        }
        }
    },
)

/**
 * 搜索：固定用 DuckDuckGo（不需要任何 Key，也就没有「配置引擎」这回事了）。
 *
 * 28.0.6 之前的实现只认 `<a class="result__a" href="...">` 这种精确结构，
 * DDG 页面结构一变就整体失效（真机实测：永远返回「没有搜到结果」）。
 * 现在改成分层解析：
 * 1. 先找带 `result__a` / `result-link` 的链接；
 * 2. 找不到就直接扫所有 `uddg=` 重定向链接（DDG 的结果链接本质都是这个）；
 * 3. 再不行扫普通外链。
 * 任何一层命中就返回，避免「页面明明有结果却说没有」。
 */
/**
 * 搜索入口：按设置选引擎。
 *
 * DeepSeek 优先（结果带 summary/content，中文更准）；
 * 未配置 Key、余额不足、限流、超时或返回异常时**静默降级**到 DuckDuckGo，
 * 保证「联网查一下」这件事不会因为引擎故障整段卡死。
 */
private fun searchWeb(query: String, limit: Int): String {
    if (AISettings.searchEngine.state == AISearchEngine.DEEPSEEK) {
        val key = AISettings.searchApiKey.state.ifBlank { AISettings.apiKey.state }
        if (key.isNotBlank()) {
            runCatching { deepSeekSearch(query, limit, key) }
                .onSuccess { return it }
                .onFailure { e ->
                    Logger.warning(TAG, "DeepSeek search failed, falling back to DuckDuckGo", e)
                }
        }
    }
    return duckDuckGoSearch(query, limit)
}

/**
 * DeepSeek 官方 Search API。
 *
 * 端点：`POST https://api.deepseek.com/v1/tools/search`
 * 请求：`query`(1~128 字符) / `count`(1~50) / `need_content`(是否返回正文)
 * 计费：按**成功请求**计，约 $0.75 / 1000 次；失败、空结果、超时不收费。
 */
private fun deepSeekSearch(query: String, limit: Int, key: String): String {
    val payload = buildString {
        append("{\"query\":")
        append(org.json.JSONObject.quote(query))
        append(",\"count\":").append(limit.coerceIn(1, 50))
        append(",\"need_content\":true}")
    }
    val request = Request.Builder()
        .url("https://api.deepseek.com/v1/tools/search")
        .header("Authorization", "Bearer $key")
        .header("Content-Type", "application/json")
        .header("User-Agent", USER_AGENT)
        .post(payload.toRequestBody("application/json".toMediaType()))
        .build()

    httpClient.newCall(request).execute().use { response ->
        if (!response.isSuccessful) {
            throw IOException("DeepSeek Search HTTP ${response.code}")
        }
        val body = response.body?.string().orEmpty()
        val root = Json.parseToJsonElement(body).jsonObject
        val results = root["results"]?.jsonArray ?: return "没有搜到结果。"
        if (results.isEmpty()) return "没有搜到结果。"

        val sb = StringBuilder()
        sb.append("搜索（DeepSeek）：").append(query).append('\n')
        results.take(limit).forEachIndexed { index, element ->
            val item = element.jsonObject
            val title = item["title"]?.jsonPrimitive?.contentOrNullSafe().orEmpty()
            val url = item["url"]?.jsonPrimitive?.contentOrNullSafe().orEmpty()
            val summary = item["summary"]?.jsonPrimitive?.contentOrNullSafe()
                ?: item["snippet"]?.jsonPrimitive?.contentOrNullSafe().orEmpty()
            val content = item["content"]?.jsonPrimitive?.contentOrNullSafe().orEmpty()
            sb.append(index + 1).append(". ").append(title).append('\n')
            sb.append("   ").append(url).append('\n')
            if (summary.isNotBlank()) sb.append("   ").append(summary).append('\n')
            if (content.isNotBlank()) {
                sb.append("   正文：").append(content.take(1200)).append('\n')
            }
        }
        return sb.toString()
    }
}

private fun kotlinx.serialization.json.JsonPrimitive.contentOrNullSafe(): String? =
    runCatching { content }.getOrNull()

private fun duckDuckGoSearch(query: String, limit: Int): String {
    val url = "https://html.duckduckgo.com/html/?q=" + java.net.URLEncoder.encode(query, "UTF-8")
    val html = get(url)

    if (html.contains("anomaly", true) || html.contains("captcha", true)) {
        return "搜索被 DuckDuckGo 拦截（人机校验）。可以改用 fetch_webpage 直接抓结果页。"
    }

    val items = linkedSetOf<Pair<String, String>>()

    //① 结构化结果
    Regex("""<a[^>]+class="[^"]*result__a[^"]*"[^>]*href="([^"]+)"[^>]*>(.*?)</a>""",
        setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
        .findAll(html).forEach { m ->
            decodeDdg(m.groupValues[1])?.let { link -> items += cleanText(m.groupValues[2]) to link }
        }

    //② 所有 uddg 重定向（DDG 结果链接的真实形态）
    if (items.size < limit) {
        Regex("""href="([^"]*uddg=[^"]+)"""", RegexOption.IGNORE_CASE).findAll(html).forEach { from ->
            decodeDdg(from.groupValues[1])?.let { link ->
                if (items.none { it.second == link }) items += link to link
            }
        }
    }

    //③ 兜底：普通外链
    if (items.size < limit) {
        Regex("""href="(https?://(?!duckduckgo\.com)[^"]+)"""", RegexOption.IGNORE_CASE).findAll(html).forEach { from ->
            val link = from.groupValues[1]
            if (items.none { it.second == link }) items += link to link
        }
    }

    val picked = items.filter { it.first.isNotBlank() }.take(limit)
    if (picked.isEmpty()) {
        return "没有解析到结果（页面结构可能又变了）。可以改用 fetch_webpage 直接抓：$url"
    }
    return buildString {
        append("搜索（DuckDuckGo）：").append(query).append('\n')
        picked.forEach { (title, link) -> append("- ").append(title).append('\n').append("  ").append(link).append('\n') }
    }
}

/** 解析 DDG 的 `//duckduckgo.com/l/?uddg=<urlencoded>` 或普通链接 */
private fun decodeDdg(href: String): String? {
    val raw = href.replace("&amp;", "&")
    val uddg = Regex("""uddg=([^&]+)""").find(raw)?.groupValues?.get(1)
    val decoded = runCatching { java.net.URLDecoder.decode(uddg ?: raw, "UTF-8") }.getOrNull() ?: return null
    return when {
        decoded.startsWith("http") -> decoded
        decoded.startsWith("//") -> "https:$decoded"
        else -> null
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
