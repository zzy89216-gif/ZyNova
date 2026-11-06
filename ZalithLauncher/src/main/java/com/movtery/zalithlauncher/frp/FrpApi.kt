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

package com.movtery.zalithlauncher.frp

import com.movtery.zalithlauncher.path.GLOBAL_JSON
import com.movtery.zalithlauncher.path.TIME_OUT
import com.movtery.zalithlauncher.path.createOkHttpClientBuilder
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.decodeFromString

/**
 * 樱花内网穿透 OpenAPI v4 客户端。
 *
 * 接口文档：<https://api.natfrp.com/docs/openapi.yaml>
 * 鉴权：`Authorization: Bearer <访问密钥>`
 *
 * 说明：
 * - 单独建了一 [HttpClient]，并把 `expectSuccess` 关掉，目的是一次性拿到
 *   `{ code, msg }` 形式的错误体，好把可读的错误提示直接展示给用户；
 * - 不缓存任何结果，调用方自己决定刷新时机；
 * - 访问密钥只在请求头里出现，不写日志、不进 URL。
 */
object FrpApi {
    private const val BASE_URL = "https://api.natfrp.com/v4"

    /** 隧道列表接口的错误提示兜底 */
    private const val UNKNOWN_ERROR = "未知错误"

    private val client = HttpClient(OkHttp) {
        expectSuccess = false

        install(HttpTimeout) {
            requestTimeoutMillis = TIME_OUT
        }

        install(ContentNegotiation) {
            json(GLOBAL_JSON)
        }

        engine {
            preconfigured = createOkHttpClientBuilder().build()
        }
    }

    /**
     * 获取账号信息，同时用作访问密钥校验。
     */
    suspend fun userInfo(token: String): FrpUser {
        return client.get("$BASE_URL/user/info") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.requireJson()
    }

    /**
     * 获取全部隧道。
     */
    suspend fun tunnels(token: String): List<FrpTunnel> {
        return client.get("$BASE_URL/tunnels") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.requireJson()
    }

    /**
     * 获取全部节点（键为节点 ID）。
     */
    suspend fun nodes(token: String): Map<String, FrpNode> {
        return client.get("$BASE_URL/nodes") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.requireJson()
    }

    /**
     * 创建隧道。
     *
     * @param type 隧道类型，Minecraft 联机用 `tcp`
     * @param node 节点 ID
     * @param remote 远程端口，留空由服务端分配
     */
    suspend fun createTunnel(
        token: String,
        name: String,
        type: String,
        node: Int,
        localIp: String,
        localPort: Int,
        remote: String?
    ): FrpCreateResult {
        return client.submitForm(
            url = "$BASE_URL/tunnels",
            formParameters = Parameters.build {
                append("name", name)
                append("type", type)
                append("node", node.toString())
                append("local_ip", localIp)
                append("local_port", localPort.toString())
                if (!remote.isNullOrBlank()) append("remote", remote)
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.requireJson()
    }

    /**
     * 修改隧道（只提交需要变更的字段）。
     */
    suspend fun editTunnel(
        token: String,
        id: Int,
        name: String? = null,
        localIp: String? = null,
        localPort: Int? = null,
        remote: String? = null
    ): FrpCreateResult {
        return client.submitForm(
            url = "$BASE_URL/tunnel/edit",
            formParameters = Parameters.build {
                append("id", id.toString())
                name?.let { append("name", it) }
                localIp?.let { append("local_ip", it) }
                localPort?.let { append("local_port", it.toString()) }
                remote?.let { append("remote", it) }
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.requireJson()
    }

    /**
     * 删除隧道。
     */
    suspend fun deleteTunnel(token: String, id: Int): Boolean {
        val response = client.submitForm(
            url = "$BASE_URL/tunnel/delete",
            formParameters = Parameters.build {
                append("ids", id.toString())
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        if (response.status.value !in 200..299) {
            throw response.toApiException()
        }
        return true
    }

    /**
     * 读取 JSON 响应；非 2xx 时把 `{ code, msg }` 转成可读异常。
     *
     * 解析失败也要转成 [FrpApiException]：否则界面上会出现
     * `Field 'realname' ...` 这种用户完全看不懂的原始异常文本。
     */
    private suspend inline fun <reified T> HttpResponse.requireJson(): T {
        if (status.value !in 200..299) {
            throw toApiException()
        }
        val text = bodyAsText()
        return runCatching { GLOBAL_JSON.decodeFromString<T>(text) }
            .getOrElse { e ->
                throw FrpApiException(
                    "接口返回格式与预期不符（HTTP ${status.value}）：" +
                            (e.message ?: e::class.simpleName.orEmpty()).take(160)
                )
            }
    }

    private suspend fun HttpResponse.toApiException(): FrpApiException {
        val text = runCatching { bodyAsText() }.getOrDefault("")
        val message = runCatching {
            GLOBAL_JSON.decodeFromString<FrpApiError>(text).msg
        }.getOrNull()?.takeIf { it.isNotBlank() }
        return FrpApiException(message ?: "HTTP ${status.value} $UNKNOWN_ERROR".trim())
    }
}

/**
 * 樱花内网穿透接口异常，`message` 可直接展示给用户。
 */
class FrpApiException(message: String) : Exception(message)
