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

import com.movtery.zalithlauncher.path.createOkHttpClientBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * AI 请求专用客户端。
 *
 * ⚠️ 为什么不用全局的 [com.movtery.zalithlauncher.path.GLOBAL_CLIENT]：
 * 那个客户端 `expectSuccess = true`，且是给普通 REST 用的；
 * 流式对话需要**长时间不超时地逐行读取响应体**，用独立客户端更好控制，
 * 同时仍然复用项目已有的 `createOkHttpClientBuilder()`（自带 ResilientDns）。
 */
private val AI_CLIENT: OkHttpClient by lazy {
    createOkHttpClientBuilder {
        // 流式对话可能很久没有数据（模型思考），读超时要放宽
        readTimeout(5, TimeUnit.MINUTES)
        connectTimeout(30, TimeUnit.SECONDS)
        // SSE 不重试（重试会重复扣费），由上层决定是否重发
        retryOnConnectionFailure(false)
    }.build()
}

internal val AI_JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
internal val AI_SSE_MEDIA = "text/event-stream".toMediaType()

/**
 * 解析服务端返回的 JSON；失败时抛出可读异常
 */
internal val AI_JSON: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
    encodeDefaults = true
}

/** 把鉴权 / 限流 / 模型错误等翻译成用户看得懂的话 */
internal fun parseHttpError(code: Int, body: String): String {
    val detail = runCatching {
        val obj = AI_JSON.parseToJsonElement(body) as? JsonObject
        val msg = obj?.get("error")?.let { err ->
            when (err) {
                is JsonObject -> err["message"]?.toString()?.trim('"')
                else -> err.toString().trim('"')
            }
        } ?: obj?.get("message")?.toString()?.trim('"')
        msg
    }.getOrNull()?.takeIf { it.isNotBlank() }

    val head = when (code) {
        401, 403 -> "API Key 无效或没有权限（HTTP $code）"
        404 -> "接口地址或模型不存在（HTTP 404）"
        429 -> "请求过于频繁或额度不足（HTTP 429）"
        in 500..599 -> "服务端错误（HTTP $code）"
        else -> "请求失败（HTTP $code）"
    }
    return if (detail != null) "$head：$detail" else head
}

/**
 * 发起一次 POST JSON 请求，**返回完整响应体文本**（用于取模型列表这类一次性请求）。
 */
internal suspend fun postJsonForText(
    url: String,
    headers: Map<String, String>,
    bodyJson: JsonObject,
): String {
    return kotlinx.coroutines.withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url(url)
            .post(AI_JSON.encodeToString(JsonObject.serializer(), bodyJson).toRequestBody(AI_JSON_MEDIA))
            .apply { headers.forEach { (k, v) -> header(k, v) } }
            .build()
        AI_CLIENT.newCall(req).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) throw IOException(parseHttpError(resp.code, text))
            text
        }
    }
}

/** 发起一次 GET 请求，返回完整响应体文本 */
internal suspend fun getForText(url: String, headers: Map<String, String>): String {
    return kotlinx.coroutines.withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url(url)
            .get()
            .apply { headers.forEach { (k, v) -> header(k, v) } }
            .build()
        AI_CLIENT.newCall(req).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) throw IOException(parseHttpError(resp.code, text))
            text
        }
    }
}

/**
 * 发起一次 POST 并**逐行读取 SSE 响应**，把每个 `data:` 负载作为 Flow 元素发出。
 *
 * ⚠️ 刻意设计成「发出负载」而不是「回调里 emit」：
 * 本函数带 [flowOn]，若在回调里向外部 Flow `emit` 会触发
 * “Flow invariant is violated”。让上层 `collect` 后再 emit 才是安全的。
 *
 * Provider 负责处理 `[DONE]`、错误事件等具体协议细节。
 * 非 2xx 响应会直接抛出带可读信息的 [IOException]。
 */
internal fun postJsonSse(
    url: String,
    headers: Map<String, String>,
    bodyJson: JsonObject,
): Flow<String> = flow {
    val req = Request.Builder()
        .url(url)
        .post(AI_JSON.encodeToString(JsonObject.serializer(), bodyJson).toRequestBody(AI_JSON_MEDIA))
        .apply {
            headers.forEach { (k, v) -> header(k, v) }
            header("Accept", "text/event-stream")
        }
        .build()

    var resp: Response? = null
    try {
        resp = AI_CLIENT.newCall(req).execute()
        if (!resp.isSuccessful) {
            val errText = resp.body?.string().orEmpty()
            throw IOException(parseHttpError(resp.code, errText))
        }
        val source = resp.body?.source() ?: throw IOException("响应体为空")
        while (true) {
            val line = source.readUtf8Line() ?: break
            if (line.isEmpty()) continue
            if (!line.startsWith("data:")) continue
            val payload = line.removePrefix("data:").trim()
            if (payload.isEmpty()) continue
            emit(payload)
        }
    } finally {
        runCatching { resp?.close() }
    }
}.flowOn(Dispatchers.IO)
