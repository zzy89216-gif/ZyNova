/*
 * ZyNova Launcher
 * Copyright (C) 2025 ZyNova Contributors
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

package com.movtery.zalithlauncher.game.download.resources

import com.movtery.zalithlauncher.utils.file.ensureParentDirectory
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.downloadFileFromSources
import com.movtery.zalithlauncher.utils.network.withRetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

private const val TAG = "ResourceDownload"

/**
 * 单次资源下载请求
 *
 * @param fileName 保存的文件名
 * @param downloadUrls 可用下载地址（含镜像，按优先级排列）
 * @param sha1 文件校验值
 * @param size 期望大小，未知时为 0
 * @param required 是否为必需文件；非必需文件失败时不会中断整批下载
 */
data class ResourceDownloadRequest(
    val fileName: String,
    val downloadUrls: List<String>,
    val sha1: String? = null,
    val size: Long = 0L,
    val required: Boolean = true
)

/**
 * 下载进度
 *
 * @param fileName 当前正在下载的文件名
 * @param finishedCount 已完成文件数
 * @param totalCount 总文件数
 * @param downloadedBytes 已接收字节数（整批累计）
 * @param totalBytes 预计总字节数（未知时为 0）
 */
data class ResourceDownloadProgress(
    val fileName: String,
    val finishedCount: Int,
    val totalCount: Int,
    val downloadedBytes: Long,
    val totalBytes: Long
) {
    /** 整体进度比例；无法估算时返回 -1 表示不确定进度 */
    val fraction: Float
        get() = if (totalBytes > 0) {
            (downloadedBytes.toDouble() / totalBytes.toDouble()).toFloat().coerceIn(0f, 1f)
        } else {
            -1f
        }
}

/**
 * 单个文件下载结果
 */
data class ResourceDownloadResult(
    val request: ResourceDownloadRequest,
    val file: File
)

/**
 * ZyNova 统一下载管理器
 *
 * Mod、资源包、光影、存档以及它们的前置依赖全部使用这里的同一个入口，
 * 各个资源页面不再自己实现下载器。
 *
 * 能力：
 * - 下载队列与并发控制
 * - 下载进度聚合
 * - 断点续传与失败重试（由底层下载引擎提供）
 * - 取消下载（协程取消后自动清理临时文件）
 * - 文件校验（sha1，由底层下载引擎完成）
 * - 临时文件清理
 * - 下载完成后的安装触发（由 [ResourceInstallManager] 负责）
 */
object ResourceDownloadManager {
    /** 默认并发下载数，避免移动端同时打开过多连接 */
    const val DEFAULT_CONCURRENCY = 3

    /** 下载失败后的重试次数 */
    private const val MAX_RETRIES = 2

    /**
     * 批量下载资源文件
     *
     * @param cacheDir 临时缓存目录
     * @param requests 下载请求列表
     * @param concurrency 并发数
     * @param onProgress 进度回调
     * @return 成功下载的文件列表（顺序与请求顺序一致）
     */
    suspend fun downloadAll(
        cacheDir: File,
        requests: List<ResourceDownloadRequest>,
        concurrency: Int = DEFAULT_CONCURRENCY,
        onProgress: (ResourceDownloadProgress) -> Unit = {}
    ): List<ResourceDownloadResult> = withContext(Dispatchers.IO) {
        if (requests.isEmpty()) return@withContext emptyList()

        val totalCount = requests.size
        val totalBytes = requests.sumOf { it.size }
        val finishedCount = AtomicInteger(0)
        val downloadedBytes = AtomicLong(0L)
        val semaphore = Semaphore(concurrency.coerceAtLeast(1))

        try {
            coroutineScope {
                requests.map { request ->
                    async {
                        semaphore.withPermit {
                            downloadOne(
                                cacheDir = cacheDir,
                                request = request,
                                totalCount = totalCount,
                                totalBytes = totalBytes,
                                finishedCount = finishedCount,
                                downloadedBytes = downloadedBytes,
                                onProgress = onProgress
                            )
                        }
                    }
                }.awaitAll()
            }.filterNotNull()
        } finally {
            //只清理本次下载产生的半成品，避免影响其它并发下载任务
            requests.forEach { request ->
                FileUtils.deleteQuietly(File(cacheDir, request.fileName + TEMP_FILE_SUFFIX))
            }
        }
    }

    /**
     * 下载单个文件
     *
     * 非必需文件失败时返回 null，不中断整批下载。
     */
    private suspend fun downloadOne(
        cacheDir: File,
        request: ResourceDownloadRequest,
        totalCount: Int,
        totalBytes: Long,
        finishedCount: AtomicInteger,
        downloadedBytes: AtomicLong,
        onProgress: (ResourceDownloadProgress) -> Unit
    ): ResourceDownloadResult? {
        if (request.downloadUrls.isEmpty()) {
            if (request.required) {
                throw ResourceProviderException("No download url available for ${request.fileName}")
            }
            return null
        }

        val target = File(cacheDir, request.fileName).ensureParentDirectory()

        return runCatching {
            withRetry(logTag = TAG, maxRetries = MAX_RETRIES) {
                downloadFileFromSources(
                    urls = request.downloadUrls,
                    outputFile = target,
                    sha1 = request.sha1,
                    sizeCallback = { delta ->
                        val current = downloadedBytes.addAndGet(delta)
                        onProgress(
                            ResourceDownloadProgress(
                                fileName = request.fileName,
                                finishedCount = finishedCount.get(),
                                totalCount = totalCount,
                                downloadedBytes = current,
                                totalBytes = totalBytes
                            )
                        )
                    }
                )
            }
            ResourceDownloadResult(request = request, file = target)
        }.getOrElse { e ->
            if (request.required) throw e
            Logger.warning(TAG, "Optional resource failed to download: ${request.fileName}", e)
            null
        }.also {
            finishedCount.incrementAndGet()
        }
    }

    /**
     * 清理下载缓存中的临时文件
     *
     * 只删除下载过程中产生、且已经没有任何用途的半成品文件，
     * 不会影响已经完成校验的正常缓存。
     */
    fun cleanupTempFiles(cacheDir: File) {
        runCatching {
            if (!cacheDir.isDirectory) return
            cacheDir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.endsWith(TEMP_FILE_SUFFIX)) {
                    FileUtils.deleteQuietly(file)
                }
            }
        }.onFailure { e ->
            Logger.warning(TAG, "Failed to clean temporary download files.", e)
        }
    }

    /** 下载过程中产生的临时文件后缀 */
    private const val TEMP_FILE_SUFFIX = ".part"
}
