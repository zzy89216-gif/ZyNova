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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

/**
 * 启动器内的「音乐」页面（议题 #77）。
 *
 * 内容全部来自 [MusicPlayerPanel]：页面本身只有导航壳，
 * 这样游戏内弹层可以复用同一份 UI，不必维护两套播放界面。
 */
@Composable
fun MusicScreen(backStackViewModel: ScreenBackStackViewModel) {
    BaseScreen(
        screenKey = NormalNavKey.Music,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) {
        MusicPlayerPanel(modifier = Modifier.fillMaxSize())
    }
}
