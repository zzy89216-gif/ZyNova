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

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.music.MusicLibrary
import com.movtery.zalithlauncher.game.music.MusicPreferences
import com.movtery.zalithlauncher.game.music.MusicService
import com.movtery.zalithlauncher.game.music.MusicTrack
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.utils.file.formatFileSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 远程面板轮询播放快照的间隔：500ms 在「进度看起来连续」和「跨进程读 MMKV 的开销」之间取平衡 */
private const val SNAPSHOT_POLL_INTERVAL_MS = 500L

private const val MESSAGE_VISIBLE_DURATION_MS = 5000L

/**
 * 游戏内音乐控制面板（议题 #77）。
 *
 * ⚠️ 为什么和启动器里的 [MusicPlayerPanel] 不是同一份实现：
 * 游戏界面 `VMActivity` 跑在 `:game` **独立进程**（manifest 里写死的 `android:process=":game"`），
 * 如果在游戏进程里直接 new 一个 ExoPlayer，就会变成「两个播放器同时出声、状态互相不同步」。
 *
 * 所以这里的定位是**远程遥控器**：
 * - 播放内核与曲库状态始终只在主进程（[com.movtery.zalithlauncher.game.music.MusicPlayerController]）；
 * - 主进程把播放快照写进 MMKV（`MULTI_PROCESS_MODE`），本面板只**读**快照显示；
 * - 所有操作（播放某首 / 播放暂停 / 上下一首 / 进度 / 停止）都通过 Intent 发给主进程的
 *   [MusicService] 执行，由主进程的同一个播放器响应。
 *
 * 这样「游戏内打开/关闭面板」只是销毁 UI，播放状态不会被重建，也不会双声。
 */
@Composable
fun MusicRemotePanel(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var snapshot by remember { mutableStateOf(MusicPreferences.readPlaybackSnapshot()) }
    var tracks by remember { mutableStateOf<List<MusicTrack>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var importing by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var draggingPosition by remember { mutableStateOf<Float?>(null) }

    /** 曲库直接读文件系统：两个进程共享同一个私有目录，扫描结果一致 */
    suspend fun refreshLibrary() {
        tracks = MusicLibrary.list(context)
        loading = false
    }

    LaunchedEffect(Unit) {
        refreshLibrary()
    }

    //跨进程轮询播放状态（游戏进程没有播放器实例，状态只能从主进程写下的快照里取）
    LaunchedEffect(Unit) {
        while (true) {
            snapshot = MusicPreferences.readPlaybackSnapshot()
            delay(SNAPSHOT_POLL_INTERVAL_MS)
        }
    }

    LaunchedEffect(message) {
        if (message != null) {
            delay(MESSAGE_VISIBLE_DURATION_MS)
            message = null
        }
    }

    /** 把命令发给主进程的前台服务；跨进程只传「做什么」，播放状态始终由主进程维护 */
    fun sendCommand(action: String, fileName: String? = null, positionMs: Long? = null) {
        val intent = Intent(context, MusicService::class.java).setAction(action)
        fileName?.let { intent.putExtra(MusicService.EXTRA_FILE_NAME, it) }
        positionMs?.let { intent.putExtra(MusicService.EXTRA_POSITION_MS, it) }
        runCatching {
            context.startService(intent)
        }.onFailure {
            message = context.getString(R.string.music_remote_unavailable)
        }
    }

    val importer = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isEmpty()) {
            message = context.getString(R.string.music_import_cancelled)
            return@rememberLauncherForActivityResult
        }
        importing = true
        scope.launch {
            val result = MusicLibrary.import(context, uris)
            importing = false
            refreshLibrary()
            message = buildMusicImportMessage(context, result)
        }
    }

    val durationMs = snapshot.durationMs
    val sliderMax = durationMs.coerceAtLeast(1L).toFloat()
    val sliderValue = (draggingPosition ?: snapshot.positionMs.toFloat()).coerceIn(0f, sliderMax)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── 顶部：标题 / 说明 / 导入 ─────────────────────────────────
        BackgroundCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.music_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(R.string.music_remote_subtitle),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(
                        enabled = !importing,
                        onClick = { importer.launch(arrayOf(MusicLibrary.AUDIO_MIME)) }
                    ) {
                        if (importing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                modifier = Modifier.size(18.dp),
                                painter = painterResource(R.drawable.ic_add),
                                contentDescription = null
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(
                                if (importing) R.string.music_importing else R.string.music_import
                            )
                        )
                    }
                }
                message?.let { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // ── 中部：曲库列表 / 空状态 ──────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                tracks.isEmpty() -> {
                    BackgroundCard(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                modifier = Modifier.size(48.dp),
                                painter = painterResource(R.drawable.ic_audio_file_filled),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.music_empty_title),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.music_remote_hint),
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            FilledTonalButton(
                                onClick = { importer.launch(arrayOf(MusicLibrary.AUDIO_MIME)) }
                            ) {
                                Text(text = stringResource(R.string.music_import))
                            }
                        }
                    }
                }
                else -> {
                    val listState = rememberLazyListState()
                    BackgroundCard(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = listState,
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(items = tracks, key = { it.fileName }) { track ->
                                RemoteTrackRow(
                                    track = track,
                                    isCurrent = snapshot.fileName == track.fileName,
                                    isPlaying = snapshot.fileName == track.fileName && snapshot.isPlaying,
                                    onClick = {
                                        sendCommand(MusicService.ACTION_PLAY_FILE, fileName = track.fileName)
                                    }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── 底部：当前曲目 / 进度 / 控制（全部是远程指令）───────────
        BackgroundCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(R.drawable.ic_audio_file_filled),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = snapshot.title.ifBlank {
                                stringResource(R.string.music_nothing_playing)
                            },
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(
                                if (snapshot.isPlaying) {
                                    R.string.music_notification_playing
                                } else {
                                    R.string.music_notification_paused
                                }
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Slider(
                    modifier = Modifier.fillMaxWidth(),
                    value = sliderValue,
                    onValueChange = { draggingPosition = it },
                    onValueChangeFinished = {
                        draggingPosition?.let { target ->
                            sendCommand(
                                MusicService.ACTION_SEEK,
                                positionMs = target.toLong()
                            )
                        }
                        draggingPosition = null
                    },
                    valueRange = 0f..sliderMax,
                    enabled = snapshot.hasSession && durationMs > 0L
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatMusicDuration(snapshot.positionMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatMusicDuration(durationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        enabled = snapshot.hasSession,
                        onClick = { sendCommand(MusicService.ACTION_PREVIOUS) }
                    ) {
                        Icon(
                            modifier = Modifier.size(28.dp),
                            painter = painterResource(R.drawable.ic_skip_previous_filled),
                            contentDescription = stringResource(R.string.music_previous)
                        )
                    }
                    FilledIconButton(
                        modifier = Modifier.size(52.dp),
                        enabled = snapshot.hasSession || tracks.isNotEmpty(),
                        onClick = {
                            if (snapshot.hasSession) {
                                sendCommand(MusicService.ACTION_TOGGLE)
                            } else {
                                //还没有会话：直接让主进程从头播放曲库第一首
                                tracks.firstOrNull()?.let { first ->
                                    sendCommand(MusicService.ACTION_PLAY_FILE, fileName = first.fileName)
                                }
                            }
                        }
                    ) {
                        Icon(
                            modifier = Modifier.size(28.dp),
                            painter = painterResource(
                                if (snapshot.isPlaying) {
                                    R.drawable.ic_pause_filled
                                } else {
                                    R.drawable.ic_play_arrow_filled
                                }
                            ),
                            contentDescription = stringResource(
                                if (snapshot.isPlaying) R.string.music_pause else R.string.music_play
                            )
                        )
                    }
                    IconButton(
                        enabled = snapshot.hasSession,
                        onClick = { sendCommand(MusicService.ACTION_NEXT) }
                    ) {
                        Icon(
                            modifier = Modifier.size(28.dp),
                            painter = painterResource(R.drawable.ic_skip_next_filled),
                            contentDescription = stringResource(R.string.music_next)
                        )
                    }
                    IconButton(
                        enabled = snapshot.hasSession,
                        onClick = { sendCommand(MusicService.ACTION_STOP) }
                    ) {
                        Icon(
                            modifier = Modifier.size(22.dp),
                            painter = painterResource(R.drawable.ic_stop),
                            contentDescription = stringResource(R.string.music_stop)
                        )
                    }
                }

                snapshot.error?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/** 游戏内曲目行：点一下让主进程播放这一首 */
@Composable
private fun RemoteTrackRow(
    track: MusicTrack,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(
                if (isCurrent) R.drawable.ic_play_arrow_filled else R.drawable.ic_audio_file_filled
            ),
            contentDescription = null,
            tint = if (isCurrent) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${formatFileSize(track.sizeBytes)} · ${formatMusicDuration(track.durationMs)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        if (isCurrent && isPlaying) {
            Text(
                text = stringResource(R.string.music_now_playing),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
