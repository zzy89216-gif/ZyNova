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

import java.io.File

/**
 * 曲库中的一首本地音乐（议题 #77）。
 *
 * 音频文件**只放在应用私有目录**（`filesDir/music`）：
 * - 公共目录 `/ZyNova` 的根目录会被 `PathManager` 在每次启动时搬进 `.Minecraft`，
 *   用户放进 `/ZyNova/music` 的歌下次启动就会「消失」（实际是被搬走了）；
 * - 私有目录不需要任何存储权限，卸载启动器时随应用一起清理。
 *
 * 导入时保留用户看到的显示名（把显示名直接作为文件名），
 * 所以 [title] 就是「去掉扩展名的文件名」，不额外依赖音乐标签解析。
 */
data class MusicTrack(
    /** 私有目录下的音频文件 */
    val file: File,
    /** 展示名（不带扩展名） */
    val title: String,
    /** 时长（毫秒；0 表示读取失败，界面上按未知处理） */
    val durationMs: Long,
    /** 文件大小（字节） */
    val sizeBytes: Long
) {
    /** 文件名在曲库内唯一，用作曲目 id（持久化上次播放也用这个名字） */
    val fileName: String get() = file.name

    /** 绝对路径（交给 ExoPlayer 播放） */
    val path: String get() = file.absolutePath
}
