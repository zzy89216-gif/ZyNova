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

import com.movtery.zalithlauncher.setting.launcherMMKV

/**
 * 音乐播放器的持久化 / 跨进程共享状态（议题 #77）。
 *
 * 直接复用项目现有的启动器 MMKV（`launcherMMKV()`，本身是 `MULTI_PROCESS_MODE`）：
 * - 「上次播放曲目/进度」是**播放状态**而不是用户可调设置，所以不往 `AllSettings` 里加设置项，
 *   免得混进设置页与 AI 的设置清单；
 * - 游戏界面跑在 `:game` 独立进程（`VMActivity` 的 `android:process=":game"`），
 *   那边**不能**自己创建播放器（会出现两个 ExoPlayer 同时出声）。
 *   所以主进程的播放器把状态写进 MMKV 快照，游戏内远程面板只读快照、通过
 *   [MusicService] 的 Intent 指令控制播放。
 *
 * 曲库列表本身不需要落盘：音频文件就放在私有目录里，扫描目录即可，
 * 文件被手动删掉时列表也会自动跟着对账（不会出现「列表里有、点开报错」）。
 */
object MusicPreferences {
    /** 上次播放的文件名（曲库内唯一，对应 [MusicTrack.fileName]） */
    private const val KEY_LAST_TRACK = "music.lastTrack"

    /** 上次播放到的进度（毫秒） */
    private const val KEY_LAST_POSITION = "music.lastPosition"

    // ── 跨进程播放快照（主进程写、:game 进程读）──────────────────────
    private const val KEY_SNAPSHOT_HAS_SESSION = "music.state.hasSession"
    private const val KEY_SNAPSHOT_FILE_NAME = "music.state.fileName"
    private const val KEY_SNAPSHOT_TITLE = "music.state.title"
    private const val KEY_SNAPSHOT_PLAYING = "music.state.playing"
    private const val KEY_SNAPSHOT_POSITION = "music.state.position"
    private const val KEY_SNAPSHOT_DURATION = "music.state.duration"
    private const val KEY_SNAPSHOT_PLAYLIST_SIZE = "music.state.playlistSize"
    private const val KEY_SNAPSHOT_ERROR = "music.state.error"

    fun saveLastPlayed(fileName: String, positionMs: Long) {
        runCatching {
            val mmkv = launcherMMKV()
            mmkv.encode(KEY_LAST_TRACK, fileName)
            mmkv.encode(KEY_LAST_POSITION, positionMs.coerceAtLeast(0L))
        }
    }

    fun clearLastPlayed() {
        runCatching {
            val mmkv = launcherMMKV()
            mmkv.removeValueForKey(KEY_LAST_TRACK)
            mmkv.removeValueForKey(KEY_LAST_POSITION)
        }
    }

    val lastTrackFileName: String?
        get() = runCatching { launcherMMKV().decodeString(KEY_LAST_TRACK) }.getOrNull()

    val lastPositionMs: Long
        get() = runCatching { launcherMMKV().decodeLong(KEY_LAST_POSITION, 0L) }.getOrDefault(0L)

    /** 主进程播放器写给游戏内远程面板看的播放快照 */
    data class PlaybackSnapshot(
        /** 是否已经装载过曲库/曲目（没有会话时远程面板只提供「播放」而不能暂停等） */
        val hasSession: Boolean,
        /** 当前曲目的文件名（曲库内唯一，游戏内列表用它标记「正在播放」） */
        val fileName: String,
        /** 当前曲目展示名，空串表示没有曲目 */
        val title: String,
        /** 用户是否要它播放（与通知栏的播放/暂停按钮一致） */
        val isPlaying: Boolean,
        val positionMs: Long,
        val durationMs: Long,
        val playlistSize: Int,
        val error: String?
    ) {
        companion object {
            val EMPTY = PlaybackSnapshot(
                hasSession = false,
                fileName = "",
                title = "",
                isPlaying = false,
                positionMs = 0L,
                durationMs = 0L,
                playlistSize = 0,
                error = null
            )
        }
    }

    fun savePlaybackSnapshot(snapshot: PlaybackSnapshot) {
        runCatching {
            val mmkv = launcherMMKV()
            mmkv.encode(KEY_SNAPSHOT_HAS_SESSION, snapshot.hasSession)
            mmkv.encode(KEY_SNAPSHOT_FILE_NAME, snapshot.fileName)
            mmkv.encode(KEY_SNAPSHOT_TITLE, snapshot.title)
            mmkv.encode(KEY_SNAPSHOT_PLAYING, snapshot.isPlaying)
            mmkv.encode(KEY_SNAPSHOT_POSITION, snapshot.positionMs)
            mmkv.encode(KEY_SNAPSHOT_DURATION, snapshot.durationMs)
            mmkv.encode(KEY_SNAPSHOT_PLAYLIST_SIZE, snapshot.playlistSize)
            mmkv.encode(KEY_SNAPSHOT_ERROR, snapshot.error.orEmpty())
        }
    }

    fun readPlaybackSnapshot(): PlaybackSnapshot {
        return runCatching {
            val mmkv = launcherMMKV()
            PlaybackSnapshot(
                hasSession = mmkv.decodeBool(KEY_SNAPSHOT_HAS_SESSION, false),
                fileName = mmkv.decodeString(KEY_SNAPSHOT_FILE_NAME).orEmpty(),
                title = mmkv.decodeString(KEY_SNAPSHOT_TITLE) ?: "",
                isPlaying = mmkv.decodeBool(KEY_SNAPSHOT_PLAYING, false),
                positionMs = mmkv.decodeLong(KEY_SNAPSHOT_POSITION, 0L),
                durationMs = mmkv.decodeLong(KEY_SNAPSHOT_DURATION, 0L),
                playlistSize = mmkv.decodeInt(KEY_SNAPSHOT_PLAYLIST_SIZE, 0),
                error = mmkv.decodeString(KEY_SNAPSHOT_ERROR)?.takeIf { it.isNotBlank() }
            )
        }.getOrDefault(PlaybackSnapshot.EMPTY)
    }
}
