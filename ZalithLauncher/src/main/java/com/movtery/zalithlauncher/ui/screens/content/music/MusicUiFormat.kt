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

package com.movtery.zalithlauncher.ui.screens.content.music

import android.content.Context
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.music.MusicLibrary
import java.util.Locale

/**
 * 音乐播放器两个界面（启动器页面 / 游戏内远程面板）共用的纯文本工具。
 *
 * 放在这里而不是各写一份：两边的时长格式、导入结果提示必须完全一致，
 * 否则同一个文件在启动器和游戏里会显示成两种时长写法。
 */

/** 毫秒 → 可读时长；未知（0）显示 `--:--`，小于 1 小时不显示小时位 */
internal fun formatMusicDuration(durationMs: Long): String {
    if (durationMs <= 0L) return "--:--"
    val totalSeconds = durationMs / 1000L
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.ROOT, "%d:%02d", minutes, seconds)
    }
}

/** 把一次导入的结果整理成给用户看的几行提示 */
internal fun buildMusicImportMessage(context: Context, result: MusicLibrary.ImportResult): String {
    val lines = mutableListOf<String>()
    if (result.importedCount > 0) {
        lines += context.getString(R.string.music_import_success, result.importedCount)
    }
    if (result.nonAudioNames.isNotEmpty()) {
        lines += context.getString(
            R.string.music_import_not_audio,
            result.nonAudioNames.joinToString("、")
        )
    }
    if (result.failedNames.isNotEmpty()) {
        lines += context.getString(
            R.string.music_import_failed,
            result.failedNames.joinToString("、")
        )
    }
    if (lines.isEmpty()) {
        lines += context.getString(R.string.music_import_no_files)
    }
    return lines.joinToString("\n")
}
