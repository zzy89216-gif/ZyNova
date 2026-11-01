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

package com.movtery.zalithlauncher.game.version.download

import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.game.download.engine.BatchDownloader
import com.movtery.zalithlauncher.game.download.engine.BatchProgress
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

private const val TAG = "TaskBatch"

/** 本地已存在文件校验的并发度 */
private const val LOCAL_VERIFY_PARALLELISM = 4

/**
 * 把一批 [DownloadTask] 交给下载引擎执行的统一入口：
 * 先挑出本地已可复用的文件（直接计入完成并执行挂后任务），
 * 剩余的交给引擎分块并发下载；仍有失败时抛出 [DownloadFailedException]，
 * message 内含失败文件路径。
 *
 * @param onSnapshot 每 100ms 收到一次引擎统计快照
 */
suspend fun Task.runBatchDownloads(
    tasks: List<DownloadTask>,
    maxConnections: Int,
    retryRounds: Int = 1,
    onSnapshot: suspend (BatchProgress) -> Unit = {},
    acceptFailure: ((task: DownloadTask, error: Throwable) -> Boolean)? = null,
    /** 把本批次的 0..1 映射到调用方的进度区间（例如只占整个分析阶段的 0.2..0.85） */
    progressMapper: (Float) -> Float = { it },
) {
    val verifyStarted = System.currentTimeMillis()
    val (reusable, pending) = verifyExistingFilesConcurrently(tasks)
    Logger.info(TAG, "Local file check done: reusable=${reusable.size} pending=${pending.size}, took=${System.currentTimeMillis() - verifyStarted}ms")

    //议题 #35：进度分母不再「全有或全无」。
    //
    //NeoForge / Forge 的 install_profile 里，很多库只有 url 没有 size；
    //旧逻辑只要发现一条没有 size，就把整批进度退化成「按文件个数」，
    //于是「12/21 = 57.1%」这种数字会长时间不动（剩下的都是大文件），
    //看起来就像卡死在 57%。
    //
    //现在的口径：已知大小求和 + 未知大小按同批平均大小估算（下限 64KiB），
    //这样字节在动、百分比就在动；并且在真正下载完成之前最多显示到 99%。
    val knownSizes = tasks.map { it.size }.filter { it > 0 }
    val unknownCount = tasks.count { it.size <= 0 }
    val estimatePerUnknown = knownSizes.takeIf { it.isNotEmpty() }
        ?.let { it.sum() / it.size }
        ?.coerceAtLeast(UNKNOWN_SIZE_FALLBACK_BYTES) ?: 0L
    val progressTotalBytes = knownSizes.sum() + unknownCount * estimatePerUnknown

    val batch = BatchDownloader(
        requests = pending.map { it.toRequest() },
        maxConnections = maxConnections,
        retryRounds = retryRounds
    )

    reusable.forEach {
        batch.stats.registerFile(it.size)
        batch.stats.markFileFinished()
        it.runFileDownloadedTask()
    }

    //已复用文件的字节并入"已下载"口径，进度条才能从已完成部分起步
    if (reusable.isNotEmpty()) {
        batch.stats.addBytes(reusable.sumOf { maxOf(it.size, 0L) })
        //随后重置测速基线，避免一次性并入的字节被当作瞬时速率报出
        batch.stats.resetSpeedBaseline()
    }

    batch.onUpdate = { snapshot ->
        updateProgress(progressMapper(progressFor(snapshot, progressTotalBytes)))
        onSnapshot(snapshot)
    }
    batch.onFileSuccess = { request ->
        (request.tag as? DownloadTask)?.runFileDownloadedTask()
    }
    acceptFailure?.let { judge ->
        batch.onFailureFilter = { request, error ->
            (request.tag as? DownloadTask)?.let { judge(it, error) } ?: false
        }
    }

    try {
        batch.run()
        updateProgress(1f)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        //议题 #35：失败详情必须带原因，否则界面只能说「一些文件下载失败」，
        //用户和开发者都看不出到底是 404、超时还是校验不过
        val failedDetail = batch.lastRunFailures.entries.joinToString("\n") { (path, error) ->
            val reason = error.message?.lineSequence()?.firstOrNull() ?: error::class.simpleName
            "$path -> $reason"
        }
        throw DownloadFailedException("Failed downloads:\n$failedDetail", e)
    } finally {
        clearSpeed()
    }
}

/**
 * 并行校验本地已存在的文件是否可复用
 */
private suspend fun verifyExistingFilesConcurrently(
    tasks: List<DownloadTask>
): Pair<List<DownloadTask>, List<DownloadTask>> =
    withContext(Dispatchers.IO.limitedParallelism(LOCAL_VERIFY_PARALLELISM)) {
        coroutineScope {
            tasks.map { task ->
                async { task to task.existingFileValid() }
            }.awaitAll()
        }.partition { it.second }
            .let { (reusable, pending) -> reusable.map { it.first } to pending.map { it.first } }
    }

/** 未知大小文件的估算下限：太小会让百分比虚高，64KiB 是个够用的保守值 */
private const val UNKNOWN_SIZE_FALLBACK_BYTES = 64L * 1024

/** 还没全部下完时最多显示到 99%，避免「显示 100% 却还在下」 */
private const val MAX_RATIO_BEFORE_DONE = 0.99f

/**
 * 进度条的显示策略（议题 #35 起）：
 * - 有可用分母时按字节比，**未完成前钳到 99%**；
 * - 连分母都构不出来（空清单）时才显示为不确定进度。
 */
private fun progressFor(snapshot: BatchProgress, totalBytes: Long): Float {
    if (snapshot.totalFiles <= 0) return -1f
    if (totalBytes <= 0L || snapshot.totalBytes <= 0) {
        return (snapshot.downloadedFiles.toFloat() / snapshot.totalFiles).coerceIn(0f, 1f)
    }
    val ratio = snapshot.downloadedBytes.toFloat() / totalBytes
    return if (snapshot.downloadedFiles < snapshot.totalFiles) {
        ratio.coerceIn(0f, MAX_RATIO_BEFORE_DONE)
    } else {
        ratio.coerceIn(0f, 1f)
    }
}

