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

package com.movtery.zalithlauncher.game.download.engine

import java.io.File
import java.io.IOException

/**
 * 一个待下载文件的任务规格。urls 为按优先级排列的候选源列表，
 * 引擎会在失败或受限时自动沿列表换源。
 */
class DownloadRequest(
    val urls: List<String>,
    val targetFile: File,
    val sha1: String? = null,
    /** 已知的文件大小，未知时传 -1；仅用于预分配与进度统计，最终以实际响应为准 */
    val expectedSize: Long = -1L,
    /** 调用方附带的上下文对象，在进度与成功回调中原样带回 */
    val tag: Any? = null
) {
    init {
        require(urls.isNotEmpty()) { "Download requires at least one url" }
    }

    override fun toString(): String = targetFile.name
}

/** 沿因果链查找第一个 HTTP 状态异常，用于调用方识别 404 等语义 */
fun Throwable.findHttpCode(): Int? =
    generateSequence(this) { it.cause }
        .filterIsInstance<HttpResultException>()
        .firstOrNull()
        ?.code

/**
 * 下载失败的归类。
 *
 * 引擎只负责「把失败类型说清楚」，具体文案由 UI 层按 [DownloadFailureReason] 映射，
 * 避免在下载层硬编码面向用户的提示。
 */
enum class DownloadFailureReason {
    /** 网络超时 */
    TIMEOUT,

    /** HTTP 403：没有权限；CurseForge 侧通常意味着 API Key 未配置或已失效 */
    FORBIDDEN,

    /** HTTP 404/410：资源文件已被删除、下架或链接失效 */
    NOT_FOUND,

    /** 其他 4xx：请求本身有问题，重试无意义 */
    CLIENT_ERROR,

    /** 5xx：服务器内部错误 */
    SERVER_ERROR,

    /** 502/503/504：镜像或官方源暂时不可用 */
    SOURCE_UNAVAILABLE,

    /** DNS 解析失败、连接被重置等网络错误 */
    NETWORK,

    /** 文件完整性（sha1）校验失败 */
    INTEGRITY,

    /** 其他未知错误 */
    UNKNOWN
}

/**
 * 下载失败异常：既保留可读的 message（含各源失败原因），
 * 又带结构化 [reason]，供上层给出准确的本地化提示。
 */
class DownloadFailureException(
    val reason: DownloadFailureReason,
    message: String,
    cause: Throwable? = null
) : IOException(message, cause)

/** 沿因果链查找下载失败归类 */
val Throwable.downloadFailureReason: DownloadFailureReason?
    get() = generateSequence(this) { it.cause }
        .filterIsInstance<DownloadFailureException>()
        .firstOrNull()
        ?.reason

/** 全部源尝试完毕仍然失败时抛出，message 内含每个源的失败原因 */
class AllSourcesFailedException(
    summary: String,
    cause: Throwable?
) : IOException(summary, cause)

class HttpResultException(
    val code: Int,
    message: String
) : IOException(message)
