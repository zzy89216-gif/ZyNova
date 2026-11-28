/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.terracotta

import com.google.gson.JsonParseException
import com.movtery.zalithlauncher.path.GLOBAL_CLIENT
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.isChinaMainland
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.safeBodyAsJson
import com.movtery.zalithlauncher.utils.string.isNotEmptyOrBlank
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URI
import java.net.URISyntaxException

private const val TAG = "TerracottaNodeList"

private const val NODE_LIST_URL = "https://terracotta.glavo.site/nodes"

@Serializable
private data class TerracottaNode(
    @SerialName("url")
    val url: String? = null,
    @SerialName("region")
    val region: String? = null
) {
    fun validate() {
        requireNotNull(url, { "TerracottaNode.url cannot be null" })
        try {
            URI(url)
        } catch (e: URISyntaxException) {
            throw JsonParseException("Invalid URL: $url", e)
        }
    }
}

@Volatile
private var nodeList: List<URI>? = null

/** 缓存写入时间（毫秒）；0 表示没有缓存 */
@Volatile
private var nodeListFetchedAt: Long = 0L

/**
 * 节点列表缓存有效期
 *
 * 27.10.2（议题 #42）：原来 `fetchNodes()` 一旦成功就**永久**缓存，
 * 后果是「官方节点列表更新了 / 某节点下线了，用户必须杀进程才能拿到新列表」。
 * 现在改成 10 分钟后允许重新拉取，单次联机里依然只请求一次，不会增加开销。
 */
private const val NODE_CACHE_TTL_MS = 10 * 60 * 1000L

private val fetchMutex = Mutex()

/** 判断当前缓存是否仍然有效 */
private fun isCacheValid(): Boolean {
    val cached = nodeList ?: return false
    if (cached.isEmpty()) return false
    return System.currentTimeMillis() - nodeListFetchedAt < NODE_CACHE_TTL_MS
}

/**
 * [Reference HMCL](https://github.com/HMCL-dev/HMCL/blob/a70b8d7/HMCL/src/main/java/org/jackhuang/hmcl/terracotta/TerracottaNodeList.java)
 */
suspend fun fetchNodes(): List<URI> {
    if (isCacheValid()) return nodeList!!

    return fetchMutex.withLock {
        if (isCacheValid()) return nodeList!!

        if (AllSettings.enableTerracottaNodes.getValue()) {
            // 使用自定义的 EasyTier 服务器节点
            val url = AllSettings.terracottaNodes.getValue()
            if (url.isNotEmptyOrBlank()) {
                val tempList = listOf(URI(url))
                nodeList = tempList
                nodeListFetchedAt = System.currentTimeMillis()
                return tempList
            }
            // 为空则继续使用默认的节点逻辑
        }

        withContext(Dispatchers.IO) {
            fetchNodesFromRemote()
        }.also { result ->
            nodeList = result
            nodeListFetchedAt = System.currentTimeMillis()
        }
    }
}

private suspend fun fetchNodesFromRemote(): List<URI> {
    return runCatching {
        val nodes = GLOBAL_CLIENT
            .get(NODE_LIST_URL)
            .safeBodyAsJson<List<TerracottaNode?>?>()

        if (nodes.isNullOrEmpty()) {
            Logger.info(TAG, "No available Terracotta nodes found")
            return emptyList()
        }

        val result = nodes
            .asSequence()
            .mapNotNull { node ->
                if (node == null) return@mapNotNull null
                parseNode(node)
            }
            .toList()

        result
    }.onFailure {
        Logger.warning(TAG, "Failed to fetch terracotta node list", it)
    }.getOrDefault(emptyList())
}

private fun parseNode(
    node: TerracottaNode
): URI? {
    return try {
        node.validate()

        if (!shouldUseNode(node.region)) {
            return null
        }

        URI.create(node.url)

    } catch (e: Exception) {
        Logger.warning(TAG, "Invalid terracotta node: $node", e)
        null
    }
}

private fun shouldUseNode(
    region: String?
): Boolean {
    if (region.isNullOrBlank()) return true

    //仅限中国大陆地区使用
    val isMainLand = isChinaMainland()
    return isMainLand && region.equals("CN", ignoreCase = true)
}
