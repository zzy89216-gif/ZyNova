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

package com.movtery.zalithlauncher.game.music

import com.movtery.zalithlauncher.utils.logging.Logger
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.Charset
import java.nio.charset.CodingErrorAction

private const val TAG = "MusicLyrics"

/**
 * 歌词文件的硬上限（1 MiB）。
 *
 * 歌词是纯文本，正常几 KB 到几十 KB；超过这个大小多半是用户选错了文件。
 * 在读盘阶段就挡掉，避免拖慢切歌 / 白占内存（切歌是高频操作，不能为异常文件付出代价）。
 */
internal const val LYRICS_MAX_FILE_BYTES = 1024L * 1024L

/** LRC 时间标签：`[mm:ss]` / `[mm:ss.xx]` / `[mm:ss.xxx]`（个别文件用 `[mm:ss:xx]`） */
private val TIME_TAG_REGEX = Regex("\\[(\\d{1,3}):(\\d{1,2})(?:[.:](\\d{1,3}))?\\]")

/** 全局偏移标签：`[offset:+500]` / `[offset:-1200]`，单位毫秒 */
private val OFFSET_TAG_REGEX = Regex("^\\[offset:\\s*([+-]?\\d{1,6})\\]$", RegexOption.IGNORE_CASE)

/**
 * 一行已同步歌词（议题 #80）。
 *
 * 时间轴不变量：[timeMs] 单调不减（[Lyrics.parse] 会排序），这样界面可以按行序做高亮 / 自动滚动，
 * 播放器也能用二分查找当前行。
 */
data class LyricLine(
    /** 这一行开始显示的时间点（毫秒，文件里的标签经过 `[offset:]` 修正） */
    val timeMs: Long,
    /** 歌词正文（已去掉时间标签，非空） */
    val text: String
)

/**
 * `.lrc` 歌词解析（议题 #80）。
 *
 * 设计原则：**边界情况全部容错，不做文件格式的改造**。
 * - 支持一句多标签（`[00:10.00][00:30.00]副歌`）；
 * - 没有时间标签的行（`[ti:]` / `[ar:]` / 纯文本）直接跳过，它们没法同步；
 * - 非法标签 / 非法秒数（>=60）跳过，不抛异常；
 * - BOM / UTF-16 / UTF-8 / GBK(GB18030) 都能读；UTF-8 严格解码失败才回退，避免把正常中文解成乱码；
 * - 解析失败返回空列表（界面按「没有歌词」处理），**绝不让播放器崩**。
 *
 * 所有公开方法都是纯同步的：调用方负责把它放在 IO 线程（见 [MusicPlayerController] 的异步加载），
 * 这样首帧 / 切歌都不会被文件 IO 卡住。
 */
object Lyrics {
    /**
     * 解析一个 `.lrc` 文件；文件不存在 / 读不出来 / 没有可同步的行都返回空列表。
     */
    fun parseFile(file: File): List<LyricLine> {
        if (!file.isFile || file.length() <= 0L) return emptyList()
        if (file.length() > LYRICS_MAX_FILE_BYTES) {
            Logger.warning(TAG, "Lyrics file is too large, ignored: ${file.name}")
            return emptyList()
        }
        val bytes = runCatching { file.readBytes() }
            .onFailure { Logger.warning(TAG, "Failed to read the lyrics of ${file.name}", it) }
            .getOrElse { return emptyList() }
        return runCatching { parse(decodeText(bytes)) }
            .onFailure { Logger.warning(TAG, "Failed to parse the lyrics of ${file.name}", it) }
            .getOrDefault(emptyList())
    }

    /**
     * 解析歌词文本；没有任何「带时间标签」的行时返回空列表。
     */
    fun parse(raw: String): List<LyricLine> {
        val lines = ArrayList<LyricLine>()
        var offsetMs = 0L

        for (rawLine in raw.lineSequence()) {
            val line = rawLine.trimEnd('\r')
            if (line.isBlank()) continue

            val offsetMatch = OFFSET_TAG_REGEX.matchEntire(line.trim())
            if (offsetMatch != null) {
                offsetMs = offsetMatch.groupValues[1].toLongOrNull() ?: offsetMs
                continue
            }

            val tags = TIME_TAG_REGEX.findAll(line).toList()
            if (tags.isEmpty()) continue
            // 正文 = 最后一个时间标签之后的部分；这样「[00:01][00:02]副歌」两个标签共用同一句
            val content = line.substring(tags.last().range.last + 1).trim()
            if (content.isEmpty()) continue

            for (tag in tags) {
                val timeMs = parseTimeTagMs(tag) ?: continue
                lines.add(LyricLine(timeMs = timeMs, text = content))
            }
        }

        lines.sortBy { it.timeMs }
        if (offsetMs == 0L) return lines

        // offset 可能把最前面几行推到 0 之前，夹紧后需要重新排序（稳定排序，同时刻行序不变）
        return lines
            .map { LyricLine(timeMs = (it.timeMs + offsetMs).coerceAtLeast(0L), text = it.text) }
            .sortedBy { it.timeMs }
    }

    /** 把一条时间标签换算成毫秒；`[mm:ss]` 的秒数非法时返回 null（该标签跳过） */
    private fun parseTimeTagMs(tag: MatchResult): Long? {
        val minutes = tag.groupValues[1].toLongOrNull() ?: return null
        val seconds = tag.groupValues[2].toLongOrNull() ?: return null
        if (seconds >= 60L) return null

        val fraction = tag.groupValues[3]
        val fractionMs = when (fraction.length) {
            0 -> 0L
            1 -> fraction.toLong() * 100L
            2 -> fraction.toLong() * 10L
            else -> fraction.take(3).toLong()
        }
        return (minutes * 60_000L + seconds * 1_000L + fractionMs).coerceAtLeast(0L)
    }

    /**
     * 把歌词文件字节解码成文本。
     *
     * 顺序：BOM（UTF-8 / UTF-16）→ UTF-8 严格解码 → GB18030（GBK 的超集，对 GBK 文本结果一致）。
     * 先用严格解码而不是 `String(bytes, UTF_8)`：后者遇到 GBK 字节会静默产出 `�`，
     * 我们就没有机会回退到 GBK 了。
     */
    private fun decodeText(bytes: ByteArray): String {
        if (bytes.size >= 3 &&
            bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()
        ) {
            return String(bytes, 3, bytes.size - 3, Charsets.UTF_8)
        }
        if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
        }
        if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
            return String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
        }
        return try {
            Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString()
        } catch (_: CharacterCodingException) {
            runCatching { String(bytes, Charset.forName("GB18030")) }
                .getOrElse { String(bytes, Charsets.UTF_8) }
        }
    }
}
