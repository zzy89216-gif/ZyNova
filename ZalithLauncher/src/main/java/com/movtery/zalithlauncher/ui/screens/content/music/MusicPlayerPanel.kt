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
import com.movtery.zalithlauncher.game.music.MusicPlayerController
import com.movtery.zalithlauncher.game.music.MusicTrack
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.utils.file.formatFileSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 提示信息在界面上停留的时间（导入/删除结果） */
private const val MESSAGE_VISIBLE_DURATION_MS = 5000L

/**
 * 音乐播放器界面（议题 #77）。
 *
 * 启动器「音乐」页与游戏内弹层**复用同一份 UI**：两边只是外壳不同
 * （页面 / Dialog），曲库、播放状态、控制按钮全部来自
 * [MusicPlayerController]，所以运行中切换入口不会重建播放器、不会断音乐。
 */
@Composable
fun MusicPlayerPanel(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var tracks by remember { mutableStateOf<List<MusicTrack>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var importing by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<MusicTrack?>(null) }

    /** 重新扫描曲库并同步给播放器（导入 / 删除后调用） */
    suspend fun refreshLibrary() {
        val loaded = MusicLibrary.list(context)
        MusicPlayerController.setPlaylist(loaded)
        tracks = loaded
        loading = false
    }

    LaunchedEffect(Unit) {
        //先按顺序初始化播放内核（必须在主线程），再扫描曲库；
        //扫描在 IO 线程，不会卡住页面首帧
        MusicPlayerController.ensureInitialized(context)
        refreshLibrary()
    }

    //提示信息几秒后自动消失，避免一直挂在页面顶部
    LaunchedEffect(message) {
        if (message != null) {
            delay(MESSAGE_VISIBLE_DURATION_MS)
            message = null
        }
    }

    //系统文件选择器多选音频：不申请任何存储权限，只拿用户选中文件的读权限
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

    val deleteTarget = pendingDelete
    if (deleteTarget != null) {
        SimpleAlertDialog(
            title = stringResource(R.string.music_delete_title),
            text = stringResource(R.string.music_delete_message, deleteTarget.title),
            confirmText = stringResource(R.string.generic_delete),
            dismissText = stringResource(R.string.generic_cancel),
            onConfirm = {
                pendingDelete = null
                scope.launch {
                    val deleted = MusicLibrary.delete(deleteTarget)
                    //播放器里也要同步摘掉（正在播这首时先停掉，避免读已删除的文件）
                    MusicPlayerController.removeTrack(deleteTarget)
                    refreshLibrary()
                    message = if (deleted) {
                        context.getString(R.string.music_delete_success, deleteTarget.title)
                    } else {
                        context.getString(R.string.music_delete_failed, deleteTarget.title)
                    }
                }
            },
            onDismiss = { pendingDelete = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── 顶部：标题 / 数量 / 导入按钮 ───────────────────────────────
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
                            text = stringResource(R.string.music_library_summary, tracks.size),
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
                //播放/焦点相关的错误（格式不支持、被别的 App 抢走焦点等）
                MusicPlayerController.lastError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // ── 中部：曲库列表 / 空状态 ───────────────────────────────────
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
                                text = stringResource(R.string.music_empty_summary),
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
                                MusicTrackRow(
                                    track = track,
                                    isCurrent = MusicPlayerController.currentTrack?.fileName == track.fileName,
                                    isPlaying = MusicPlayerController.playRequested &&
                                        MusicPlayerController.isPlaying,
                                    onClick = { MusicPlayerController.playTrack(track) },
                                    onDelete = { pendingDelete = track }
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

        // ── 底部：当前曲目 + 进度 + 控制 ─────────────────────────────
        NowPlayingCard()
    }
}

/** 当前播放控制区：曲名 / 进度条（可拖动）/ 上一首 / 播放暂停 / 下一首 / 停止 */
@Composable
private fun NowPlayingCard() {
    val current = MusicPlayerController.currentTrack
    val durationMs = MusicPlayerController.durationMs
    val duration = durationMs.coerceAtLeast(1L)
    var draggingPosition by remember { mutableStateOf<Float?>(null) }
    val sliderValue = (draggingPosition ?: MusicPlayerController.positionMs.toFloat())
        .coerceIn(0f, duration.toFloat())

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
                        text = current?.title ?: stringResource(R.string.music_nothing_playing),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(
                            if (MusicPlayerController.playRequested) {
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
                    draggingPosition?.let { target -> MusicPlayerController.seekTo(target.toLong()) }
                    draggingPosition = null
                },
                valueRange = 0f..duration.toFloat(),
                enabled = current != null && durationMs > 0L
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatMusicDuration(MusicPlayerController.positionMs),
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
                    enabled = MusicPlayerController.playlist.isNotEmpty(),
                    onClick = { MusicPlayerController.previous() }
                ) {
                    Icon(
                        modifier = Modifier.size(28.dp),
                        painter = painterResource(R.drawable.ic_skip_previous_filled),
                        contentDescription = stringResource(R.string.music_previous)
                    )
                }
                FilledIconButton(
                    modifier = Modifier.size(52.dp),
                    enabled = MusicPlayerController.playlist.isNotEmpty(),
                    onClick = { MusicPlayerController.toggle() }
                ) {
                    Icon(
                        modifier = Modifier.size(28.dp),
                        painter = painterResource(
                            if (MusicPlayerController.playRequested || MusicPlayerController.isPlaying) {
                                R.drawable.ic_pause_filled
                            } else {
                                R.drawable.ic_play_arrow_filled
                            }
                        ),
                        contentDescription = stringResource(
                            if (MusicPlayerController.playRequested || MusicPlayerController.isPlaying) {
                                R.string.music_pause
                            } else {
                                R.string.music_play
                            }
                        )
                    )
                }
                IconButton(
                    enabled = MusicPlayerController.playlist.isNotEmpty(),
                    onClick = { MusicPlayerController.next() }
                ) {
                    Icon(
                        modifier = Modifier.size(28.dp),
                        painter = painterResource(R.drawable.ic_skip_next_filled),
                        contentDescription = stringResource(R.string.music_next)
                    )
                }
                IconButton(
                    enabled = current != null,
                    onClick = { MusicPlayerController.stop() }
                ) {
                    Icon(
                        modifier = Modifier.size(22.dp),
                        painter = painterResource(R.drawable.ic_stop),
                        contentDescription = stringResource(R.string.music_stop)
                    )
                }
            }
        }
    }
}

/** 曲库中的一行：曲名 / 大小 · 时长 / 当前播放标记 / 删除 */
@Composable
private fun MusicTrackRow(
    track: MusicTrack,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
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
        IconButton(onClick = onDelete) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(R.drawable.ic_delete_outlined),
                contentDescription = stringResource(R.string.generic_delete)
            )
        }
    }
}
