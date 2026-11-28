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

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ai.AISettings
import com.movtery.zalithlauncher.ai.agent.AITool
import com.movtery.zalithlauncher.ai.AISearchEngine
import com.movtery.zalithlauncher.ai.aiString
import com.movtery.zalithlauncher.ai.agent.AIToolRisk
import com.movtery.zalithlauncher.ai.agent.AIToolSchema
import com.movtery.zalithlauncher.path.createOkHttpClientBuilder
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.io.InterruptedIOException
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

/**
 * 网页抓取 / DuckDuckGo / MC 百科用的客户端。
 *
 * 同样复用项目带 ResilientDns 的构建器：系统 DNS 被污染或解析失败时自动走 DoH，
 * 否则这些抓取在部分网络上会直接 UnknownHost、白白失败。
 * 只读网页不像 AI 对话要秒级响应，连接 12 秒、读取 25 秒足够；
 * DeepSeek 搜索另用 [deepSeekHttpClient]（更长，因为服务端要检索网页）。
 */
private val httpClient: OkHttpClient by lazy {
    createOkHttpClientBuilder { builder ->
        builder.connectTimeout(12, TimeUnit.SECONDS)
        builder.readTimeout(25, TimeUnit.SECONDS)
        builder.followRedirects(true)
    }.build()
}

/** DeepSeek Search 官方限制：query 1~128 个字符，超了会直接 400 */
private const val MAX_DEEPSEEK_QUERY_CHARS = 128

/** DeepSeek Search 的整体超时；提示文案里的秒数也用它，避免两处写死后对不上 */
private const val DEEPSEEK_CALL_TIMEOUT_SECONDS = 60L

/**
 * DeepSeek Search 专用客户端。
 *
 * ⚠️ 不能复用上面的 [httpClient]：那个是给网页抓取用的（读超时 25 秒），
 * 而 Search 接口在服务端要检索网页，官方示例给的是 60 秒整体超时（curl `--max-time 60`）。
 * 旧实现只有 18 秒，经常被超时打断，还把超时当成「DeepSeek 坏了」静默退回 DuckDuckGo ——
 * 真机上的表现就是「DeepSeek 的搜索不能用 / 搜不到就退回」。
 *
 * 这里放宽读超时，并复用项目带 ResilientDns 的构建器（系统 DNS 失败时自动走 DoH）。
 */
private val deepSeekHttpClient: OkHttpClient by lazy {
    createOkHttpClientBuilder { builder ->
        builder.connectTimeout(15, TimeUnit.SECONDS)
        builder.readTimeout(45, TimeUnit.SECONDS)
        builder.callTimeout(DEEPSEEK_CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
    }.build()
}

/**
 * 与 [runCatching] 相同，但**不吞协程取消**。
 *
 * `runCatching` 会把 `CancellationException` 也接住，用户点「停止」后工具还会返回一个
 * 「失败」字符串继续往下走；这里把取消原样抛出。
 */
private inline fun <T> runCatchingCancellable(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }

internal fun webTools(): List<AITool> = listOf(
    aiTool(
        name = "web_search",
        description = """
            联网搜索（引擎由 AI 设置决定：DeepSeek 官方搜索 / DuckDuckGo 网页版）。
            用于查模组报错、崩溃信息、版本兼容性等本机资料里没有的内容。
            返回前若干条结果的标题 + 链接 + 摘要（DeepSeek 引擎给的是长摘要，不含网页正文；
            要读正文请再调用 fetch_webpage）。
            如果结果开头带「⚠️」并说明已退回 DuckDuckGo（英文界面下是 fell back to DuckDuckGo），
            说明 DeepSeek 引擎这次没成功，请把原因如实告诉用户，不要当成 DeepSeek 的结果。
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
            runCatchingCancellable { searchWeb(query, limit) }
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
            runCatchingCancellable { fetchAsText(raw) }
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
        runCatchingCancellable {
            val html = get(url)
            //mcmod 限流时返回 200 + 「搜索太频繁」，必须单独识别，别混成「没有结果」
            if (html.contains("搜索太频繁") || html.contains("请稍后再试")) {
                return@runCatchingCancellable "MC 百科正在限流（提示「搜索太频繁」），过一会儿再试"
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
 * 搜索入口：按设置选引擎。
 *
 * ⚠️ 旧实现用 `runCatching` 吞掉 DeepSeek 的一切失败（没配 Key / 欠费 / 限流 / 超时 / 解析失败），
 * 然后**静默**跑 DuckDuckGo，工具返回里只写「搜索（DuckDuckGo）」。
 * 用户完全看不到 DeepSeek 为什么没用上，表现就成了「DeepSeek 的搜索不能用、搜不到就退回」。
 *
 * 现在：兜底仍然保留（联网查询不该让整轮对话失败），但**降级必须可见** ——
 * 结果开头会写明 DeepSeek 为什么没成功，模型也会照实告诉用户。
 */
private fun searchWeb(query: String, limit: Int): String {
    if (AISettings.searchEngine.state != AISearchEngine.DEEPSEEK) {
        return duckDuckGoSearch(query, limit)
    }

    //搜索 Key 优先用单独配置的；留空则复用主模型 Key（设置页文案就是这么写的）
    val key = AISettings.searchApiKey.state.trim()
        .ifEmpty { AISettings.apiKey.state.trim() }
    if (key.isEmpty()) {
        Logger.warning(TAG, "DeepSeek search skipped: no search key configured")
        return withDuckDuckGoFallback(aiString(R.string.ai_search_ds_no_key), query, limit)
    }

    val reason = try {
        return deepSeekSearch(query, limit, key)
    } catch (e: CancellationException) {
        //⚠️ 协程取消不是「搜索失败」，不能吞掉（否则用户点了「停止」还会继续跑兜底）
        throw e
    } catch (e: DeepSeekSearchError) {
        Logger.warning(TAG, "DeepSeek search failed: ${e.message}")
        e.message ?: aiString(R.string.ai_search_ds_error_bad_response)
    } catch (e: InterruptedIOException) {
        Logger.warning(TAG, "DeepSeek search timed out after ${DEEPSEEK_CALL_TIMEOUT_SECONDS}s: ${e.message}")
        aiString(R.string.ai_search_ds_error_timeout, DEEPSEEK_CALL_TIMEOUT_SECONDS.toInt())
    } catch (e: IOException) {
        Logger.warning(TAG, "DeepSeek search network error", e)
        aiString(R.string.ai_search_ds_error_network, e.message ?: (e::class.simpleName ?: "IO"))
    } catch (e: Exception) {
        Logger.warning(TAG, "DeepSeek search unexpected error", e)
        aiString(R.string.ai_search_ds_error_network, e.message ?: (e::class.simpleName ?: "?"))
    }
    return withDuckDuckGoFallback(reason, query, limit)
}

/**
 * DeepSeek 没拿到可用结果：先写清楚原因，再附上 DuckDuckGo 兜底结果。
 *
 * 这样用户 / 模型既能拿到结果，也能知道「这次不是 DeepSeek 搜的、为什么」。
 */
private fun withDuckDuckGoFallback(reason: String, query: String, limit: Int): String {
    val fallback = try {
        duckDuckGoSearch(query, limit)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Logger.warning(TAG, "DuckDuckGo fallback failed", e)
        aiString(R.string.ai_search_ddg_failed, e.message ?: (e::class.simpleName ?: "?"))
    }
    return aiString(R.string.ai_search_fallback_notice, reason) + "\n" + fallback
}

/** DeepSeek Search 的可读失败原因（message 已本地化，直接给模型 / 用户看） */
private class DeepSeekSearchError(message: String) : Exception(message)

/**
 * DeepSeek 官方 Search API。
 *
 * 端点：`POST <Base URL>/tools/search`（官方默认 `https://api.deepseek.com/v1/tools/search`）
 * 请求：`query`(1~128 字符) / `count`(1~50) / `need_content`(是否返回网页正文)
 * 响应：`{"results":[{rerank_score,title,url,snippet,summary,content,site_name,site_icon,date,authority}]}`
 * 计费：按**成功请求**计（结果非空才计费），失败、空结果、超时不计费。
 * 文档：https://api-docs.deepseek.com/zh-cn/api/search/
 */
private fun deepSeekSearch(query: String, limit: Int, key: String): String {
    val payload = buildJsonObject {
        put("query", query.take(MAX_DEEPSEEK_QUERY_CHARS))
        put("count", limit.coerceIn(1, 50))
        //只取长摘要（summary 约 600~800 字，正好给模型读）。
        //need_content=true 会让服务端在返回前抓取每个网页正文，时延从几秒涨到几十秒，
        //旧代码的 18 秒读超时就是这么被打爆、每次搜索都掉进兜底的；
        //要看正文时让模型再调 fetch_webpage，比这里整页抓取更可控。
        put("need_content", false)
    }
    val request = Request.Builder()
        .url(deepSeekSearchUrl())
        .header("Authorization", "Bearer $key")
        .header("Content-Type", "application/json")
        .header("User-Agent", USER_AGENT)
        .post(payload.toString().toRequestBody("application/json".toMediaType()))
        .build()

    deepSeekHttpClient.newCall(request).execute().use { response ->
        val body = response.body?.string().orEmpty()
        if (!response.isSuccessful) {
            throw DeepSeekSearchError(deepSeekHttpError(response.code, body))
        }
        val root = runCatching { Json.parseToJsonElement(body).jsonObject }.getOrNull()
            ?: throw DeepSeekSearchError(aiString(R.string.ai_search_ds_error_bad_response))
        val results = root["results"] as? JsonArray
            ?: throw DeepSeekSearchError(aiString(R.string.ai_search_ds_error_bad_response))
        if (results.isEmpty()) throw DeepSeekSearchError(aiString(R.string.ai_search_ds_empty))

        val sb = StringBuilder()
        sb.append("搜索（DeepSeek）：").append(query).append('\n')
        results.take(limit).forEachIndexed { index, element ->
            val item = element as? JsonObject ?: return@forEachIndexed
            val title = item.text("title")
            val url = item.text("url")
            //summary 是官方给模型读的长摘要（600~800 字）；snippet 是旧字段，留作兜底
            val summary = item.text("summary").ifBlank { item.text("snippet") }
            val content = item.text("content")
            val site = item.text("site_name")
            val date = item.text("date").take(10)
            sb.append(index + 1).append(". ").append(title).append('\n')
            sb.append("   ").append(url).append('\n')
            if (summary.isNotBlank()) sb.append("   ").append(summary).append('\n')
            if (content.isNotBlank()) sb.append("   正文：").append(content.take(1200)).append('\n')
            val meta = listOf(site, date).filter { it.isNotBlank() }.joinToString(" · ")
            if (meta.isNotBlank()) sb.append("   （").append(meta).append("）\n")
        }
        return sb.toString()
    }
}

/** 读 JSON 对象的字符串字段；缺失或为 JSON null 时返回空串 */
private fun JsonObject.text(key: String): String =
    (this[key] as? JsonPrimitive)?.contentOrNull.orEmpty()

/**
 * Search 接口地址：跟随设置里的 Base URL。
 *
 * 官方完整地址是 `https://api.deepseek.com/v1/tools/search`（Base URL 默认值就是 `…/v1`）。
 * 旧实现把官方域名写死：用户如果在 AI 设置里填了「兼容 OpenAI 协议的中转地址」，
 * 搜索却仍然打到官方站点，中转的 Key 在官方无效 → 401 → 静默退回 DuckDuckGo。
 * 现在跟随 Base URL；只写了域名、漏掉 `/v1` 的会被补上（避免拼出 `…/v1/v1/tools/search`）。
 */
private fun deepSeekSearchUrl(): String {
    val base = AISettings.getBaseUrl()
    val versioned = if (base.endsWith("/v1")) base else "$base/v1"
    return "$versioned/tools/search"
}

/**
 * 把 Search 接口的 HTTP 错误翻译成明确原因。
 *
 * 官方错误码：400 参数不合法 / 401 Key 无效 / 402 余额不足 / 403 拒绝 /
 * 408 读请求体超时 / 429 限流 / 5xx 服务端错误。
 * 旧实现只抛 “DeepSeek Search HTTP 402” 这种码，用户根本不知道是欠费还是 Key 错了。
 */
private fun deepSeekHttpError(code: Int, body: String): String {
    val detail = runCatching {
        val obj = Json.parseToJsonElement(body).jsonObject
        when (val err = obj["error"]) {
            is JsonObject -> err["message"]
            else -> obj["message"]
        }.let { (it as? JsonPrimitive)?.contentOrNull }
    }.getOrNull()?.takeIf { it.isNotBlank() }?.take(300)
        ?: aiString(R.string.ai_search_ds_error_detail_unknown)

    return when (code) {
        400 -> aiString(R.string.ai_search_ds_error_bad_request, detail)
        401, 403 -> aiString(R.string.ai_search_ds_error_auth)
        402 -> aiString(R.string.ai_search_ds_error_balance)
        404 -> aiString(R.string.ai_search_ds_error_not_supported, deepSeekSearchUrl())
        429 -> aiString(R.string.ai_search_ds_error_rate_limited)
        in 500..599 -> aiString(R.string.ai_search_ds_error_server, code, detail)
        else -> aiString(R.string.ai_search_ds_error_http, code, detail)
    }
}

/**
 * 搜索：DuckDuckGo（不需要任何 Key）。
 *
 * 28.0.6 之前的实现只认 `<a class="result__a" href="...">` 这种精确结构，
 * DDG 页面结构一变就整体失效（真机实测：永远返回「没有搜到结果」）。
 * 现在改成分层解析：
 * 1. 先找带 `result__a` / `result-link` 的链接；
 * 2. 找不到就直接扫所有 `uddg=` 重定向链接（DDG 的结果链接本质都是这个）；
 * 3. 再不行扫普通外链。
 * 任何一层命中就返回，避免「页面明明有结果却说没有」。
 */
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
