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

package com.movtery.zalithlauncher.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.music.MusicPlayerController
import com.movtery.zalithlauncher.ui.components.codexGlass

/**
 * 启动器顶栏下方的迷你播放条（议题 #81）。
 *
 * 为什么单独一条而不是塞进 64dp 顶栏里：
 * 顶栏中间是 8 个入口的玻璃胶囊，左右只剩标题与任务圆环的位置，
 * 再塞歌名与切歌按钮一定挤；所以在顶栏**下方**插一条 40dp 的紧凑条，
 * 只在「不是音乐页 + 有当前曲目（播放中或暂停）」时出现，不改变原有顶栏布局。
 *
 * 状态全部读 [MusicPlayerController] 的 Compose 状态：
 * 切歌、暂停、清空曲库都会自动重组，不需要界面再保存一份。
 * 点整条进入音乐页（完整的选歌 / 歌词 / 进度都在那里）。
 */
@Composable
fun MusicMiniPlayer(
    visible: Boolean,
    onOpenMusic: () -> Unit,
    modifier: Modifier = Modifier,
) {
    //显示条件用 currentTrack != null（有当前曲目：播放中 / 暂停 / 刚切到某首），
    //不能用 hasSession —— 那个是「曲库已装载或有选中曲目」的服务生命周期判断，
    //两者条件不同会让迷你条在「只是导入了歌、还没选中」时也冒出来。
    //currentTrack 是 Compose state，切歌/停止/清空曲库都会自动进出。
    AnimatedVisibility(
        visible = visible && MusicPlayerController.currentTrack != null,
        modifier = modifier
    ) {
        MiniPlayerBar(onOpenMusic = onOpenMusic)
    }
}

@Composable
private fun MiniPlayerBar(onOpenMusic: () -> Unit) {
    val current = MusicPlayerController.currentTrack
    val playing = MusicPlayerController.playRequested || MusicPlayerController.isPlaying
    val canSkip = MusicPlayerController.playlist.isNotEmpty()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 6.dp)
            .height(40.dp)
            .codexGlass(shape = RoundedCornerShape(20.dp))
            .clickable(onClick = onOpenMusic)
            .padding(start = 12.dp, end = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painterResource(R.drawable.ic_music_filled),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = current?.title ?: stringResource(R.string.music_nothing_playing),
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        MiniPlayerAction(
            painter = painterResource(R.drawable.ic_skip_previous_filled),
            contentDescription = stringResource(R.string.music_previous),
            enabled = canSkip,
            onClick = { MusicPlayerController.previous() }
        )
        MiniPlayerAction(
            painter = painterResource(
                if (playing) R.drawable.ic_pause_filled else R.drawable.ic_play_arrow_filled
            ),
            contentDescription = stringResource(
                if (playing) R.string.music_pause else R.string.music_play
            ),
            enabled = canSkip,
            onClick = { MusicPlayerController.toggle() }
        )
        MiniPlayerAction(
            painter = painterResource(R.drawable.ic_skip_next_filled),
            contentDescription = stringResource(R.string.music_next),
            enabled = canSkip,
            onClick = { MusicPlayerController.next() }
        )
    }
}

/**
 * 迷你条里的小按钮。
 *
 * 不用 [androidx.compose.material3.IconButton]：它自带 48dp 的最小触控区，
 * 三个按钮会把这条「紧凑条」撑回 48dp 以上；这里用 34dp 的圆形热区，
 * 配合整条可点（点空白处进音乐页），既保持 40dp 条高又不至于点不到。
 */
@Composable
private fun MiniPlayerAction(
    painter: Painter,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = LocalContentColor.current
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painter,
            contentDescription = contentDescription,
            tint = if (enabled) contentColor else contentColor.copy(alpha = 0.38f)
        )
    }
}
