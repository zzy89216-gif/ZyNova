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

package com.movtery.zalithlauncher.ai.agent.tools

import com.movtery.zalithlauncher.ai.agent.AITool
import com.movtery.zalithlauncher.ai.agent.AIToolRisk
import com.movtery.zalithlauncher.ai.agent.AIToolSchema
import com.movtery.zalithlauncher.context.GlobalContext
import com.movtery.zalithlauncher.game.music.MusicLibrary
import com.movtery.zalithlauncher.game.music.MusicPlayerController
import com.movtery.zalithlauncher.game.music.MusicTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 给模型列「可用曲名」时最多列几首，避免把整张曲库倒进上下文 */
private const val MAX_TRACK_HINTS = 10

/**
 * 内置音乐播放器的 AI 工具（议题 #80 / #81）。
 *
 * 只**读用** `game/music` 目录的现有能力，不自己造播放器、不碰歌词解析：
 * - [MusicLibrary.list] 负责扫描曲库（IO 线程）；
 * - [MusicPlayerController] 是进程级单例，**所有公开方法都必须在主线程调用**（ExoPlayer 的线程约束）。
 *
 * 所以这里的模式统一是：曲库在 [Dispatchers.IO] 扫描 → 回到主线程操作控制器 → 回一段短文本。
 * 之所以要先 `setPlaylist`：用户可能从没打开过「音乐」页就直接让 AI 放歌，
 * 播放器的上一首/下一首依赖曲库，控制器里没有曲库时这些操作是空转。
 */
internal fun musicTools(): List<AITool> = listOf(
    musicStatusTool(),
    musicControlTool(),
)

// ─────────────────────────────────────────────────────────────
//  music_status
// ─────────────────────────────────────────────────────────────

private fun musicStatusTool() = aiTool(
    name = "music_status",
    description = "查看启动器内置音乐播放器的当前状态：\n" +
        "- 当前曲目、是播放中 / 缓冲中 / 已暂停；\n" +
        "- 已播放到的位置与总时长（未知时显示 --:--）；\n" +
        "- 曲库一共有多少首；\n" +
        "- 当前曲目有没有歌词（有就顺带给出当前歌词行）。\n\n" +
        "用户问「在听什么」「放到哪了」「有没有歌词」时用它。只读，不会改变播放状态。\n" +
        "要放歌 / 暂停 / 切歌时用 music_control。",
    parameters = AIToolSchema.noArgs(),
    risk = AIToolRisk.READ,
) { _, _ -> musicStatusText() }

/**
 * 状态文本。
 *
 * 曲库数量以「真实扫描结果」为准（用户可能刚删掉歌）：
 * 播放器里的曲库是上次扫描的快照，文件被删后它可能还留着旧曲目，
 * 所以这里要拿扫描结果做对比，当前曲目已经不在曲库里时明确提示（否则用户会以为 AI 看错了）。
 */
private suspend fun musicStatusText(): String {
    val library = withContext(Dispatchers.IO) { MusicLibrary.list(GlobalContext) }
    return withContext(Dispatchers.Main.immediate) {
        buildString {
            val track = MusicPlayerController.currentTrack

            if (track == null) {
                append("当前没有选中的曲目，")
                if (library.isNotEmpty()) {
                    append("曲库有 ").append(library.size).append(" 首（还没开始播放）。")
                } else {
                    append("曲库是空的（还没有导入音乐）。")
                }
            } else {
                append("当前曲目：").append(track.title).append('\n')
                append("状态：").append(playbackLabel()).append('\n')
                //位置 0 是合法值（00:00），只有「时长未知」才显示 --:--
                append("进度：").append(formatPosition(MusicPlayerController.positionMs))
                    .append(" / ").append(formatDuration(MusicPlayerController.durationMs)).append('\n')
                append("歌词：").append(lyricsLabel()).append('\n')
                append("曲库：").append(library.size).append(" 首")
                if (library.none { it.fileName == track.fileName }) {
                    append("（当前曲目已不在曲库中，可能已被删除或移动）")
                }
            }

            MusicPlayerController.lastError?.let { append("\n最近错误：").append(it) }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  music_control
// ─────────────────────────────────────────────────────────────

private fun musicControlTool() = aiTool(
    name = "music_control",
    description = "控制启动器内置音乐播放器（音源是用户在「音乐」页导入的本地曲库）。\n\n" +
        "action 可选值：\n" +
        "- play：播放。可选 track 指定曲名或文件名；不传则继续播放当前曲目（没有就从头播第一首）\n" +
        "- pause：暂停\n" +
        "- toggle：播放 / 暂停切换\n" +
        "- next / previous：下一首 / 上一首（播放中会接着播，暂停中只切换不开播）\n" +
        "- stop：停止播放（进度归零，下次播放从头开始）\n" +
        "- seek：跳到 position_seconds 秒（需要 position_seconds）\n\n" +
        "⚠️ 曲库是用户自己导入的本地音乐，不要用这个工具搜歌 / 下载歌；曲库空时请让用户先去「音乐」页导入。\n" +
        "⚠️ 曲名找不到时会返回可用的曲名，照着再调一次，不要瞎猜。\n" +
        "⚠️ 用户只是问「在听什么」时用 music_status，不要用这个工具。",
    parameters = AIToolSchema.objectSchema(
        properties = mapOf(
            "action" to AIToolSchema.string(
                "要执行的操作",
                enumValues = listOf("play", "pause", "toggle", "next", "previous", "stop", "seek"),
            ),
            "track" to AIToolSchema.string("action=play 时可选：曲名或文件名"),
            "position_seconds" to AIToolSchema.integer(
                "action=seek 时必填：目标位置（秒）",
                minimum = 0,
                maximum = 86_400,
            ),
        ),
        required = listOf("action"),
    ),
    risk = AIToolRisk.WRITE,
) { args, _ ->
    val action = args.strOr("action", "toggle").trim().lowercase()
    controlMusic(
        action = action,
        wantedTrack = args.str("track"),
        positionSeconds = args.int("position_seconds"),
    )
}

/**
 * 执行一次播放控制，返回给模型看的短文本。
 *
 * ⚠️ 控制器读写的都是 Compose state / ExoPlayer，必须在主线程；曲库扫描必须在 IO。
 */
private suspend fun controlMusic(
    action: String,
    wantedTrack: String?,
    positionSeconds: Int?,
): String {
    val scanned = withContext(Dispatchers.IO) { MusicLibrary.list(GlobalContext) }

    return withContext(Dispatchers.Main.immediate) {
        MusicPlayerController.ensureInitialized(GlobalContext)

        //把最新曲库同步给播放器：正在播的曲目还在列表里就保持不动，
        //已经被删掉的曲目会被清掉（否则 next / play 会去读一个不存在的文件）。
        //曲库为空也必须同步 ——「歌都被删了」时播放器里的旧快照不能继续冒充曲库。
        MusicPlayerController.setPlaylist(scanned)
        val tracks = MusicPlayerController.playlist

        when (action) {
            "play" -> {
                if (tracks.isEmpty()) {
                    emptyLibraryHint()
                } else if (wantedTrack.isNullOrBlank()) {
                    //没有当前曲目时 playCurrent() 自己会播列表第一首；但它可能因为
                    //音频焦点被占用 / 播放内核没建起来而静默失败，必须核对真实状态再回话
                    MusicPlayerController.playCurrent()
                    playbackStartedMessage("已开始/继续播放。")
                } else {
                    val track = findTrack(tracks, wantedTrack)
                    if (track == null) {
                        trackNotFoundHint(wantedTrack, tracks)
                    } else {
                        MusicPlayerController.playTrack(track)
                        playbackStartedMessage("已开始播放。")
                    }
                }
            }

            "pause" -> {
                //本来就没在播时直接回「已暂停」会让人以为刚才有声音，这里如实说明
                val wasActive = MusicPlayerController.playRequested || MusicPlayerController.isPlaying
                MusicPlayerController.pause()
                withError((if (wasActive) "已暂停。" else "当前没有在播放，已保持暂停。") + "\n" + stateLine())
            }

            "toggle" -> {
                if (tracks.isEmpty() && MusicPlayerController.currentTrack == null) {
                    emptyLibraryHint()
                } else {
                    MusicPlayerController.toggle()
                    withError("已切换为" + playbackLabel() + "。\n" + stateLine())
                }
            }

            "next" -> {
                if (tracks.isEmpty()) {
                    emptyLibraryHint()
                } else {
                    MusicPlayerController.next()
                    withError("已切到下一首。\n" + stateLine())
                }
            }

            "previous" -> {
                if (tracks.isEmpty()) {
                    emptyLibraryHint()
                } else {
                    MusicPlayerController.previous()
                    withError("已切到上一首。\n" + stateLine())
                }
            }

            "stop" -> {
                MusicPlayerController.stop()
                "已停止播放（进度归零，下次播放从头开始）。"
            }

            "seek" -> {
                val seconds = positionSeconds
                if (seconds == null) {
                    "seek 需要 position_seconds（目标秒数，≥0）。"
                } else if (MusicPlayerController.currentTrack == null) {
                    "当前没有可跳转的曲目，先 play 一首再 seek。"
                } else {
                    val targetMs = seconds.toLong() * 1000L
                    val knownDuration = MusicPlayerController.durationMs
                    //控制器内部会把目标夹进已知时长；这里提前记下是否被夹，
                    //否则回给模型的位置和用户输入对不上（「跳到 90 秒」但歌只有 60 秒）
                    val clamped = knownDuration > 0L && targetMs > knownDuration
                    MusicPlayerController.seekTo(targetMs)
                    val actual = MusicPlayerController.positionMs
                    if (!MusicPlayerController.isPrepared) {
                        //还没装载播放器（还没 play 过）：只有起始位置被记下来，
                        //真正 seek 要等装载媒体项时由 prepareTrack 生效
                        "已把播放起点设为 " + formatPosition(actual) + "（还没开始播放，下次播放从这里开始）。"
                    } else {
                        val prefix = if (clamped) {
                            "已跳到 " + formatPosition(actual) + "（目标超出时长，已夹到结尾）。"
                        } else {
                            "已跳到 " + formatPosition(actual) + "。"
                        }
                        withError(prefix + "\n" + stateLine())
                    }
                }
            }

            else -> "不认识的 action「$action」。可用值：play / pause / toggle / next / previous / stop / seek。"
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  小工具
// ─────────────────────────────────────────────────────────────

/** 播放状态的人话标签（缓冲中也算「要播」，和界面按钮的语义一致） */
private fun playbackLabel(): String = when {
    MusicPlayerController.isPlaying -> "播放中"
    MusicPlayerController.playRequested -> "缓冲中"
    MusicPlayerController.isPrepared -> "已暂停"
    else -> "未播放"
}

/** 有没有歌词 + 当前行 */
private fun lyricsLabel(): String {
    if (!MusicPlayerController.hasLyrics) return "无"
    val line = MusicPlayerController.lyrics
        .getOrNull(MusicPlayerController.currentLyricIndex)
        ?.text
        ?.takeIf { it.isNotBlank() }
    return if (line == null) "有（还没到第一句）" else "有（当前行：$line）"
}

/** 当前播放状态的紧凑一行（操作结果里带上，模型转述时不用再调 music_status） */
private fun stateLine(): String = buildString {
    val track = MusicPlayerController.currentTrack
    append("当前曲目：").append(track?.title ?: "无")
    append(" | ").append(playbackLabel())
    append(" | ").append(formatPosition(MusicPlayerController.positionMs))
    append(" / ").append(formatDuration(MusicPlayerController.durationMs))
}

/**
 * 播放类操作的返回文本：先核对播放器**真的**开始要播了再报成功。
 *
 * `playCurrent()` / `playTrack()` 在音频焦点申请失败、播放器没建成、媒体项装载失败时
 * 都是**静默返回**的；不核对就会回「已开始播放」而实际一点声音都没有。
 */
private fun playbackStartedMessage(prefix: String): String {
    if (MusicPlayerController.playRequested || MusicPlayerController.isPlaying) {
        return withError(prefix + "\n" + stateLine())
    }
    val error = MusicPlayerController.lastError
    return "播放没有启动" +
        if (error != null) "：$error" else "（音频焦点可能被其他应用占用，或播放内核未就绪）。"
}

/** 操作完成后把最近一次播放错误也带上（例如音频焦点被占用、解码失败） */
private fun withError(line: String): String {
    val error = MusicPlayerController.lastError ?: return line
    return line + "\n最近错误：" + error
}

/**
 * 位置：0 是**合法值**（曲目开头 → 00:00），只有负数/未知才显示 `--:--`。
 *
 * 旧实现把位置和时长都交给同一个「<=0 就 --:--」的函数，导致刚开始播放 / 暂停在开头时
 * 进度显示成 `--:--`，看起来像读不到进度（议题 #82 的观感问题在 AI 侧复现）。
 */
private fun formatPosition(ms: Long): String = if (ms < 0L) "--:--" else formatClock(ms)

/** 时长：0 表示未知（还没 READY / 容器没给出）→ `--:--` */
private fun formatDuration(ms: Long): String = if (ms <= 0L) "--:--" else formatClock(ms)

/**
 * 毫秒 → `mm:ss`。
 *
 * 超过 1 小时的曲目分钟数会自然超过 59（例如 75:30），这是有意为之：
 * 只在前面补零、不截断，也不做进位成小时（短音频用不上，且保持一行紧凑）。
 */
private fun formatClock(ms: Long): String {
    val totalSeconds = ms.coerceAtLeast(0L) / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return minutes.toString().padStart(2, '0') + ":" + seconds.toString().padStart(2, '0')
}

/** 按「曲名 / 文件名 / 去扩展名 / 包含」的顺序找曲目；找不到返回 null */
private fun findTrack(tracks: List<MusicTrack>, query: String): MusicTrack? {
    val raw = query.trim()
    if (raw.isEmpty()) return null
    val nameWithoutExtension = raw.substringBeforeLast('.', raw)
    return tracks.firstOrNull { it.title.equals(raw, ignoreCase = true) }
        ?: tracks.firstOrNull { it.fileName.equals(raw, ignoreCase = true) }
        ?: tracks.firstOrNull { it.title.equals(nameWithoutExtension, ignoreCase = true) }
        ?: tracks.firstOrNull { it.fileName.substringBeforeLast('.').equals(raw, ignoreCase = true) }
        ?: tracks.firstOrNull { it.title.contains(raw, ignoreCase = true) }
        ?: tracks.firstOrNull { it.fileName.contains(raw, ignoreCase = true) }
}

/** 找不到曲目时给可用曲名，让模型能立刻改正而不是继续瞎猜 */
private fun trackNotFoundHint(query: String, tracks: List<MusicTrack>): String {
    val scope = if (tracks.size > MAX_TRACK_HINTS) {
        "可用曲目（前 $MAX_TRACK_HINTS 首，共 ${tracks.size} 首）"
    } else {
        "可用曲目"
    }
    return "曲库里没有叫「$query」的曲目。$scope：" +
        tracks.take(MAX_TRACK_HINTS).joinToString("、") { it.title }
}

/** 曲库为空时的统一提示 */
private fun emptyLibraryHint(): String =
    "曲库是空的，没有可以播放的音乐。请用户先在「音乐」页导入音频。"
