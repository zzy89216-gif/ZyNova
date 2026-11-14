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

package com.movtery.zalithlauncher.game.download.assets.platform

import android.util.Log
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.game.addons.mirror.MirrorPriority
import com.movtery.zalithlauncher.game.addons.mirror.resolveMirrorPriority
import com.movtery.zalithlauncher.game.download.assets.mapExceptionToMessage
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.CurseForgeSearcher
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.MCIM_CURSEFORGE_API
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.MCIM_MODRINTH_API
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.ModrinthSearcher
import com.movtery.zalithlauncher.game.download.assets.utils.localizedModSearchKeywords
import com.movtery.zalithlauncher.game.download.engine.AllSourcesFailedException
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.MirrorSourceType
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.DownloadAssetsState
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.SearchAssetsState
import com.movtery.zalithlauncher.utils.isChinaMainland
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.isInterruptedIOException
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

private const val TAG = "PlatformSearch"

private val modrinthSearcher = ModrinthSearcher()
private val mirrorModrinthSearcher = ModrinthSearcher(
    api = MCIM_MODRINTH_API,
    source = "MCIM Modrinth"
)

private val curseForgeSearcher = CurseForgeSearcher()
private val mirrorCurseForgeSearcher = CurseForgeSearcher(
    api = MCIM_CURSEFORGE_API,
    source = "MCIM CurseForge"
)

/**
 * 对资源平台搜索启用镜像源机制进行操作
 *
 * 某个源失败会自动换下一个；**所有源都失败**时，把每个源各自的真实原因
 * （HTTP 状态 / 超时 / DNS 等）聚合进异常 message，而不是只抛一句没有信息量的
 * 「All sources have failed to attempt」——用户和开发者都要能一眼看出是哪个源、因为什么失败。
 */
suspend fun <E: AbstractPlatformSearcher, T> mirroredPlatformSearcher(
    searchers: List<E>,
    printLog: Boolean = true,
    block: suspend (E) -> T
): T {
    require(searchers.isNotEmpty()) { "Searcher list must not be empty." }

    /** 每个源的失败记录（源名称 → 异常），全部失败时用于聚合原因 */
    val failures = mutableListOf<Pair<String, Exception>>()
    var lastException: Exception? = null

    for (searcher in searchers) {
        try {
            if (printLog) {
                Logger.debug(TAG, "Starting to attempt to perform the operation on source: {${searcher.source}}")
            }
            return block(searcher)
        } catch (e: CancellationException) {
            //⚠️ 取消不是「镜像失败」：不能记 error、不能降级到下一个源、更不能报 WARNING。
            //否则用户切页 / 换关键词 / 关页面时，日志里会堆满
            //「All sources have failed + Mirror error #N: Job was cancelled」这种假故障。
            throw e
        } catch (e: Exception) {
            Log.w("PlatformSearcher", "Failed to perform the operation on source: {${searcher.source}}", e)
            lastException = e
            failures += searcher.source to e

            if (e.isInterruptedIOException()) {
                //网络请求被中断（不是单源失败），继续换源没有意义
                throw e
            } else if (e is FileNotFoundException) {
                //源明确说「文件不存在」，别的源也找不到
                break
            }
        }
    }

    //所有源都失败：聚合每个源的真实原因（日志 + 异常 message 都带）
    val failureSummary = buildAllSourcesFailureMessage(failures)
    val aggregated = AllSourcesFailedException(failureSummary, lastException)
    if (printLog) {
        Logger.warning(TAG, "All sources failed to perform the operation.", aggregated)
    }

    //404/403 这类「语义明确」的失败保持原异常类型抛出：
    //上层会按状态码给出精确文案（例如依赖项目 404 时记住「该项目不存在」），
    //包装成 IOException 反而会让这些判断失效。
    val semanticFailure = lastException
    if (semanticFailure != null && semanticFailure.isSemanticPlatformFailure()) {
        throw semanticFailure
    }
    throw aggregated
}

/**
 * 聚合每个源的失败原因：一句能看懂、能照着操作的话。
 *
 * 例：All platform sources failed (2):
 * - Official Modrinth: timeout after 5000 ms
 * - MCIM Modrinth: HTTP 503 Service Unavailable
 *
 * ⚠️ 这里只写**技术事实**（源名 + 失败原因），不写面向用户的操作指引：
 * 用户可见文案由界面按资源字符串本地化，映射见 `mapExceptionToMessage()`。
 */
private fun buildAllSourcesFailureMessage(failures: List<Pair<String, Exception>>): String {
    if (failures.isEmpty()) return "All platform sources failed, but no specific reason was recorded."
    return buildString {
        append("All platform sources failed (").append(failures.size).append("):\n")
        failures.forEach { (source, error) ->
            append("- ").append(source).append(": ").append(error.describePlatformFailure()).append('\n')
        }
    }
}

/** 单源失败的一行摘要：HTTP 状态优先，其次异常 message，最后退回类名 */
private fun Throwable.describePlatformFailure(): String {
    if (this is ResponseException) {
        return "HTTP ${response.status.value} ${response.status.description}".trim()
    }
    val text = message ?: cause?.message ?: javaClass.simpleName
    val firstLine = text.lineSequence().firstOrNull()?.trim().orEmpty()
    return when {
        firstLine.isEmpty() -> javaClass.simpleName
        firstLine.length > 120 -> firstLine.take(117) + "..."
        else -> firstLine
    }
}

/**
 * 4xx（404/403 等确定性错误）与「文件不存在」属于语义明确的失败。
 *
 * 这类失败上层会据此给出精确提示、或跳过该项目；不能包装成通用 IOException。
 * 408/429 属于临时性错误，仍然走聚合提示。
 */
private fun Throwable.isSemanticPlatformFailure(): Boolean = when (this) {
    is FileNotFoundException -> true
    is ResponseException -> response.status.value.let { it in 400..499 && it != 408 && it != 429 }
    else -> false
}

/**
 * 是否配置了 CurseForge 官方 API Key
 *
 * 官方接口在缺少 Key 时一定返回 403，与网络环境无关。
 */
private fun hasCurseForgeApiKey(): Boolean = BuildKeys.CURSEFORGE_API.isNotBlank()

/**
 * CurseForge 的候选来源
 *
 * 规则：
 * - **没有 Key**：官方接口必然 403，只保留 MCIM 镜像（镜像不需要 Key）；
 * - **有 Key**：官方接口优先，镜像始终作为兜底，官方临时失败后仍可回退镜像；
 *   只有用户显式选择「镜像优先」时才把镜像排到前面。
 *
 * ⚠️ 没有 Key 时即便不在中国大陆也必须保留镜像源，否则 CurseForge 侧
 * 永远搜不到任何资源，而「存档」这类只有 CurseForge 提供的资源类型会直接变成固定空列表。
 */
fun mirroredCurseForgeSource(
    enabledMirror: Boolean = true
): List<CurseForgeSearcher> {
    if (!hasCurseForgeApiKey()) {
        return listOf(mirrorCurseForgeSearcher)
    }
    if (!enabledMirror) {
        return listOf(curseForgeSearcher)
    }
    val mirrorFirst = AllSettings.assetPlatformSource.getValue() == MirrorSourceType.MIRROR_FIRST
    return if (mirrorFirst) {
        listOf(mirrorCurseForgeSearcher, curseForgeSearcher)
    } else {
        listOf(curseForgeSearcher, mirrorCurseForgeSearcher)
    }
}

/**
 * 镜像源只能在中国地区使用
 */
fun mirroredModrinthSource(
    enabledMirror: Boolean = isChinaMainland()
): List<ModrinthSearcher> {
    val source = resolveMirrorPriority(AllSettings.assetPlatformSource.getValue(), mainland = enabledMirror)
    val mirrorSource = mirrorModrinthSearcher.takeIf { enabledMirror }
    return when (source) {
        MirrorPriority.OFFICIAL_FIRST ->
            listOfNotNull(modrinthSearcher, mirrorSource)
        MirrorPriority.MIRROR_FIRST ->
            listOfNotNull(mirrorSource, modrinthSearcher)
    }
}

suspend fun searchAssets(
    searchPlatform: Platform,
    searchFilter: PlatformSearchFilter,
    platformClasses: PlatformClasses,
    onSuccess: suspend (PlatformSearchResult) -> Unit,
    onError: (SearchAssetsState.Error) -> Unit
) {
    runCatching {
        val (containsChinese, englishKeywords) = searchFilter.searchName.localizedModSearchKeywords(platformClasses)
        //参考源代码：[HMCL Github](https://github.com/HMCL-dev/HMCL/blob/d295e60/HMCL/src/main/java/org/jackhuang/hmcl/game/LocalizedRemoteModRepository.java#L56-L68)
        //逐个英文短语尝试搜索，取第一个有非空结果的
        val queries = if (!englishKeywords.isNullOrEmpty()) {
            englishKeywords.toList()
        } else {
            listOf(searchFilter.searchName)
        }

        var lastResult: PlatformSearchResult? = null
        var lastException: Exception? = null
        for (query in queries) {
            try {
                val r = when (searchPlatform) {
                    Platform.CURSEFORGE -> mirroredPlatformSearcher(
                        searchers = mirroredCurseForgeSource(),
                        printLog = false
                    ) { searcher ->
                        searcher.searchAssets(
                            query = query,
                            searchFilter = searchFilter,
                            platformClasses = platformClasses
                        )
                    }
                    Platform.MODRINTH -> mirroredPlatformSearcher(
                        searchers = mirroredModrinthSource(),
                        printLog = false
                    ) { searcher ->
                        searcher.searchAssets(
                            query = query,
                            searchFilter = searchFilter,
                            platformClasses = platformClasses
                        )
                    }
                }
                lastResult = r
                if (r.getAssetsPage(platformClasses).data.isNotEmpty()) break
            } catch (e: CancellationException) {
                //取消不是「这个词搜失败了」，不要继续往下搜
                throw e
            } catch (e: Exception) {
                //当前关键词搜索失败，记录异常并继续尝试下一个
                lastException = e
            }
        }

        val result = lastResult ?: throw lastException ?: IOException("Failed to search for all queries")

        onSuccess(
            if (containsChinese) result.processChineseSearchResults(searchFilter.searchName, platformClasses)
            else result
        )
    }.onFailure { e ->
        if (e !is CancellationException) {
            Logger.error(TAG, "An exception occurred while searching for assets.", e)
            val state = SearchAssetsState.Error(mapExceptionToMessage(e))
            onError(state)
        } else {
            Logger.debug(TAG, "The search task has been cancelled.")
        }
    }
}

suspend fun getVersions(
    projectID: String,
    platform: Platform,
    pageCallback: (chunk: Int, page: Int) -> Unit = { _, _ -> },
) = when (platform) {
    Platform.CURSEFORGE -> mirroredPlatformSearcher(
        searchers = mirroredCurseForgeSource()
    ) { searcher ->
        searcher.getVersions(
            projectID = projectID,
            pageCallback = pageCallback
        )
    }
    Platform.MODRINTH -> mirroredPlatformSearcher(
        searchers = mirroredModrinthSource()
    ) { searcher ->
        searcher.getVersions(
            projectID = projectID,
            pageCallback = pageCallback
        )
    }
}

/**
 * 通过版本ID获取单个版本的完整信息
 * @return 对应版本；若该平台不支持或未找到则返回 null
 */
suspend fun getVersionById(
    versionID: String,
    platform: Platform,
): PlatformVersion? = when (platform) {
    Platform.CURSEFORGE -> mirroredPlatformSearcher(
        searchers = mirroredCurseForgeSource()
    ) { searcher ->
        searcher.getVersionById(versionID)
    }
    Platform.MODRINTH -> mirroredPlatformSearcher(
        searchers = mirroredModrinthSource()
    ) { searcher ->
        searcher.getVersionById(versionID)
    }
}


suspend fun <E> getVersions(
    projectID: String,
    platform: Platform,
    pageCallback: (chunk: Int, page: Int) -> Unit = { _, _ -> },
    onSuccess: suspend (List<PlatformVersion>) -> Unit,
    onError: (DownloadAssetsState<List<E>>) -> Unit
) {
    runCatching {
        val result = getVersions(projectID, platform, pageCallback)
        onSuccess(result)
    }.onFailure { e ->
        if (e !is CancellationException) {
            Logger.error(TAG, "An exception occurred while retrieving the project version.", e)
            val state = DownloadAssetsState.Error<List<E>>(mapExceptionToMessage(e))
            onError(state)
        } else {
            Logger.debug(TAG, "The version retrieval task has been cancelled.")
        }
    }
}

suspend fun <E> getProject(
    projectID: String,
    platform: Platform,
    onSuccess: (PlatformProject) -> Unit,
    onError: (DownloadAssetsState<E>, Throwable) -> Unit
) {
    runCatching {
        when (platform) {
            Platform.CURSEFORGE -> mirroredPlatformSearcher(
                searchers = mirroredCurseForgeSource()
            ) { searcher ->
                searcher.getProject(projectID)
            }
            Platform.MODRINTH -> mirroredPlatformSearcher(
                searchers = mirroredModrinthSource()
            ) { searcher ->
                searcher.getProject(projectID)
            }
        }
    }.fold(
        onSuccess = onSuccess,
        onFailure = { e ->
            if (e !is CancellationException) {
                Logger.error(TAG, "An exception occurred while retrieving project information.", e)
                val state = DownloadAssetsState.Error<E>(mapExceptionToMessage(e))
                onError(state, e)
            } else {
                Logger.debug(TAG, "The project retrieval task has been cancelled.")
            }
        }
    )
}

suspend fun getProjectByVersion(
    projectId: String,
    platform: Platform,
    printLog: Boolean = true
): PlatformProject = withContext(Dispatchers.IO) {
    when (platform) {
        Platform.MODRINTH -> mirroredPlatformSearcher(
            searchers = mirroredModrinthSource(),
            printLog = printLog
        ) { searcher ->
            searcher.getProject(projectId)
        }
        Platform.CURSEFORGE -> mirroredPlatformSearcher(
            searchers = mirroredCurseForgeSource(),
            printLog = printLog
        ) { searcher ->
            searcher.getProject(projectId)
        }
    }
}

suspend fun getVersionByLocalFile(file: File, sha1: String): PlatformVersion? = withContext(Dispatchers.IO) {
    coroutineScope {
        val modrinthDeferred = async(Dispatchers.IO) {
            runCatching {
                mirroredPlatformSearcher(
                    searchers = mirroredModrinthSource(),
                    printLog = false
                ) { searcher ->
                    searcher.getVersionByLocalFile(file, sha1)
                }
            }.getOrNull()
        }

        val curseForgeDeferred = async(Dispatchers.IO) {
            runCatching {
                mirroredPlatformSearcher(
                    searchers = mirroredCurseForgeSource(),
                    printLog = false
                ) { searcher ->
                    searcher.getVersionByLocalFile(file, sha1)
                }
            }.getOrNull()
        }

        val result = select {
            modrinthDeferred.onAwait { result ->
                if (result != null) {
                    curseForgeDeferred.cancel()
                    result
                } else {
                    null
                }
            }
            curseForgeDeferred.onAwait { result ->
                if (result != null) {
                    modrinthDeferred.cancel()
                    result
                } else {
                    null
                }
            }
        }

        result ?: run {
            if (!modrinthDeferred.isCompleted) modrinthDeferred.await()
            else if (!curseForgeDeferred.isCompleted) curseForgeDeferred.await()
            else null
        }
    }
}