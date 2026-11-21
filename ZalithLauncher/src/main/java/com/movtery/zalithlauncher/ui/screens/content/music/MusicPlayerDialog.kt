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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.theme.cardColor

/**
 * 游戏内音乐弹层（议题 #77）。
 *
 * 用 [Dialog] 而不是往游戏画面里叠 Compose 层：
 * - Dialog 是**独立窗口**，触摸事件不会穿透到游戏的控制层（`ControlBoxLayout` / 快捷栏），
 *   不影响输入映射；
 * - 返回键由 Dialog 自己处理（默认 `dismissOnBackPress = true`），
 *   不需要改 `GameScreen` 的返回键分发；
 * - 关闭弹层只销毁 UI，播放器在主进程的
 *   [com.movtery.zalithlauncher.game.music.MusicPlayerController] 单例里继续播，重新打开不会重建播放状态。
 *
 * ⚠️ 游戏界面在 `:game` 独立进程，这里的内容是 [MusicRemotePanel]（远程遥控：
 * 状态读主进程写下的 MMKV 快照，操作通过 Intent 交给 [com.movtery.zalithlauncher.game.music.MusicService]），
 * **不会**在游戏进程里再建一个播放器。
 */
@Composable
fun MusicPlayerDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            //全屏显示：音乐列表与控制区需要比系统默认对话框宽得多的空间
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = cardColor(influencedByBackground = false)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.music_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = stringResource(R.string.generic_close)
                        )
                    }
                }
                MusicRemotePanel(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
