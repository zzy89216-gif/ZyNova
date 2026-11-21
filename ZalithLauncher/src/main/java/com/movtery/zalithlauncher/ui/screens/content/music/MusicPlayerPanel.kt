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
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.music.LyricLine
import com.movtery.zalithlauncher.game.music.MusicLibrary
import com.movtery.zalithlauncher.game.music.MusicPlayerController
import com.movtery.zalithlauncher.game.music.MusicTrack
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** 提示信息在界面上停留的时间（导入/删除结果） */
private const val MESSAGE_VISIBLE_DURATION_MS = 5000L

/** 进度条两侧时间标签的最小宽度：避免数字变化时滑条左右抖动 */
private val TIME_LABEL_MIN_WIDTH = 48.dp

/**
 * 音乐播放器界面（议题 #77 / #80 / #82）。
 *
 * 启动器「音乐」页与游戏内弹层**复用同一份内核**：曲库、播放状态、控制按钮全部来自
 * [MusicPlayerController]，所以运行中切换入口不会重建播放器、不会断音乐。
 *
 * 版面按议题 #80 的「更密集」要求排：顶部一条紧凑标题/导入、其下歌词区、
 * 中部曲库列表（单行小行高）、底部常驻进度与控制条。
 * 进度条（#82）的拖动状态用 [draggingPosition] 单独保存，位置回调不会把正在拖的滑块拽回去。
 */
@Composable
fun MusicPlayerPanel(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var tracks by remember { mutableStateOf<List<MusicTrack>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var importing by remember { mutableStateOf(false) }
    var importingLyrics by remember { mutableStateOf(false) }
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

    //议题 #80：导入歌词。系统选择器按 `*/*` 打开（.lrc 没有统一 MIME 类型，
    //用 text/* 会让部分文件管理器把 .lrc 置灰），选完这里再校验扩展名。
    val lyricsImporter = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            message = context.getString(R.string.music_lyrics_cancelled)
            return@rememberLauncherForActivityResult
        }
        if (!lyricsFileName(context, uri).endsWith(".lrc", ignoreCase = true)) {
            message = context.getString(R.string.music_lyrics_not_lrc)
            return@rememberLauncherForActivityResult
        }
        val track = MusicPlayerController.currentTrack
        if (track == null) {
            message = context.getString(R.string.music_lyrics_import_failed)
            return@rememberLauncherForActivityResult
        }
        importingLyrics = true
        scope.launch {
            //复制成 files/music/<同名>.lrc（IO 在 core 内部切），成功后让当前曲目重新加载
            val saved = MusicLibrary.saveLyrics(context, track, uri)
            importingLyrics = false
            if (saved) {
                MusicPlayerController.reloadLyrics()
                message = context.getString(R.string.music_lyrics_import_success)
            } else {
                message = context.getString(R.string.music_lyrics_import_failed)
            }
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
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── 顶部：标题 / 数量 / 导入（紧凑一行）──────────────────────
        BackgroundCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        painter = painterResource(R.drawable.ic_music_filled),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.music_title),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = stringResource(R.string.music_library_summary, tracks.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(
                        enabled = !importing,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        onClick = { importer.launch(arrayOf(MusicLibrary.AUDIO_MIME)) }
                    ) {
                        if (importing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                modifier = Modifier.size(16.dp),
                                painter = painterResource(R.drawable.ic_add),
                                contentDescription = null
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(
                                if (importing) R.string.music_importing else R.string.music_import
                            ),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                message?.let { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                //播放/焦点相关的错误（格式不支持、被别的 App 抢走焦点等）
                MusicPlayerController.lastError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // ── 中部：歌词 + 曲库并排（横屏布局）─────────────────────────
        //MainActivity 是 sensorLandscape：可用高度通常只有 330–380dp。
        //若把「歌词 / 曲库 / 控制条」竖着排，矮屏上底部进度与控制卡会被挤出屏幕，
        //所以歌词与曲库并排放进唯一的 weight 区域，底部控制条永远可见、可点。
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            //歌词区：只有存在当前曲目时才出现（没选中歌时提示没意义）
            if (MusicPlayerController.currentTrack != null) {
                LyricsPanel(
                    importing = importingLyrics,
                    onImport = { lyricsImporter.launch(arrayOf("*/*")) },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
            LibrarySection(
                loading = loading,
                tracks = tracks,
                onImport = { importer.launch(arrayOf(MusicLibrary.AUDIO_MIME)) },
                onPlay = { MusicPlayerController.playTrack(it) },
                onDelete = { pendingDelete = it },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // ── 底部：当前曲目 + 进度 + 控制（常驻；没选中曲目时没有可控制对象，不占高度）──
        if (MusicPlayerController.currentTrack != null) {
            NowPlayingCard()
        }
    }
}

/**
 * 歌词区（议题 #80）。
 *
 * 有歌词：当前行高亮、上下保留若干行，并随播放自动滚到当前行；
 * 没歌词：直接给一句解释 + 「导入歌词」入口（系统选择器选 .lrc）。
 */
@Composable
private fun LyricsPanel(
    importing: Boolean,
    onImport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lyrics = MusicPlayerController.lyrics
    val currentIndex = MusicPlayerController.currentLyricIndex
    //hasLyrics 是普通 getter（内部读 lyrics 状态），异步加载完成后会触发重组
    val hasLyrics = MusicPlayerController.hasLyrics
    //切歌 / 重新导入歌词时 core 会先把 lyrics 清空再异步解析：
    //用加载中状态区分「正在解析」和「确定没有歌词」，否则切歌瞬间会闪一下「无歌词 + 导入歌词」
    val loadingLyrics = MusicPlayerController.lyricsLoading

    BackgroundCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            when {
                hasLyrics -> LyricLines(
                    lyrics = lyrics,
                    currentIndex = currentIndex,
                    modifier = Modifier.fillMaxSize()
                )
                loadingLyrics -> LyricsLoadingHint()
                else -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    NoLyricsHint(importing = importing, onImport = onImport)
                }
            }
        }
    }
}

@Composable
private fun LyricLines(
    lyrics: List<LyricLine>,
    currentIndex: Int,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    //当前行变化 / 换歌 / 重新导入歌词都重新定位：
    //向上留两行的上下文，让高亮行大致停在中间；还没到第一句（index=-1）就回顶部。
    //合成一个 effect 是必要的：以前拆成「歌词变了回顶部」+「行变了滚到当前行」两个，
    //导入歌词（歌词列表变化、但 currentIndex 数值可能不变）时第二个不会重启，
    //会被第一个拽回顶部，直到下一句歌词才归位。
    LaunchedEffect(lyrics, currentIndex) {
        if (lyrics.isEmpty()) return@LaunchedEffect
        if (currentIndex >= 0) {
            listState.animateScrollToItem((currentIndex - 2).coerceIn(0, lyrics.lastIndex))
        } else {
            listState.scrollToItem(0)
        }
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        itemsIndexed(items = lyrics) { index, line ->
            val isCurrent = index == currentIndex
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 3.dp),
                text = line.text,
                style = if (isCurrent) {
                    MaterialTheme.typography.bodyMedium
                } else {
                    MaterialTheme.typography.labelMedium
                },
                fontWeight = if (isCurrent) FontWeight.SemiBold else null,
                color = if (isCurrent) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                },
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** 歌词正在异步解析：只显示一行「加载中」，不显示「没有歌词」与导入按钮 */
@Composable
private fun LyricsLoadingHint() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Text(
                text = stringResource(R.string.music_lyrics_loading),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoLyricsHint(
    importing: Boolean,
    onImport: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(R.drawable.ic_text_snippet_filled),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = stringResource(R.string.music_lyrics_empty),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = stringResource(R.string.music_lyrics_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        FilledTonalButton(
            enabled = !importing,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            onClick = onImport
        ) {
            if (importing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(
                    if (importing) R.string.music_lyrics_importing else R.string.music_lyrics_import
                ),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

/**
 * 当前播放控制区：曲名 / 进度条（可拖动）/ 上一首 / 播放暂停 / 下一首 / 停止。
 *
 * #82 的两个要点：
 * - 时间标签**常显**：当前位置与总时长分列滑块两侧，总时长未知时显示 `--:--`（不是 0:00）；
 * - 拖动期间用 [draggingPosition] 顶住显示，播放器每 500ms 回写的位置不会把滑块拽回去，
 *   松手才 seek，然后清空拖动状态交给位置回调。
 */
@Composable
private fun NowPlayingCard() {
    val current = MusicPlayerController.currentTrack
    val durationMs = MusicPlayerController.durationMs
    val duration = durationMs.coerceAtLeast(1L)
    var draggingPosition by remember { mutableStateOf<Float?>(null) }
    val sliderValue = (draggingPosition ?: MusicPlayerController.positionMs.toFloat())
        .coerceIn(0f, duration.toFloat())
    //拖动时时间标签跟着显示目标位置，松手前也能看到要跳到哪
    val displayedPosition = draggingPosition?.toLong() ?: MusicPlayerController.positionMs
    val playing = MusicPlayerController.playRequested || MusicPlayerController.isPlaying
    val stateText = when {
        current == null -> stringResource(R.string.music_nothing_playing)
        playing -> stringResource(R.string.music_notification_playing)
        else -> stringResource(R.string.music_notification_paused)
    }

    //切歌（含播完自动下一首）时丢掉上一首的拖动状态：
    //否则拖到一半换了歌，松手会把新歌 seek 到上一首的位置。
    LaunchedEffect(current?.fileName) {
        draggingPosition = null
    }

    BackgroundCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            //一行装下「曲名 / 状态 + 全部控制键」：横屏宽度够，把省下的高度留给歌词与曲库
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
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = current?.title ?: stringResource(R.string.music_nothing_playing),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stateText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                IconButton(
                    enabled = MusicPlayerController.playlist.isNotEmpty(),
                    onClick = { MusicPlayerController.previous() }
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_skip_previous_filled),
                        contentDescription = stringResource(R.string.music_previous)
                    )
                }
                FilledIconButton(
                    modifier = Modifier.size(44.dp),
                    enabled = MusicPlayerController.playlist.isNotEmpty(),
                    onClick = { MusicPlayerController.toggle() }
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(
                            if (playing) {
                                R.drawable.ic_pause_filled
                            } else {
                                R.drawable.ic_play_arrow_filled
                            }
                        ),
                        contentDescription = stringResource(
                            if (playing) R.string.music_pause else R.string.music_play
                        )
                    )
                }
                IconButton(
                    enabled = MusicPlayerController.playlist.isNotEmpty(),
                    onClick = { MusicPlayerController.next() }
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_skip_next_filled),
                        contentDescription = stringResource(R.string.music_next)
                    )
                }
                IconButton(
                    enabled = current != null,
                    onClick = { MusicPlayerController.stop() }
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(R.drawable.ic_stop),
                        contentDescription = stringResource(R.string.music_stop)
                    )
                }
            }

            //进度条：左当前位置 / 右总时长，两侧最小宽度固定，避免数字变化时滑条抖动
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.widthIn(min = TIME_LABEL_MIN_WIDTH),
                    text = formatMusicPosition(displayedPosition),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    modifier = Modifier.weight(1f),
                    value = sliderValue,
                    onValueChange = { draggingPosition = it },
                    onValueChangeFinished = {
                        draggingPosition?.let { target ->
                            MusicPlayerController.seekTo(target.toLong())
                        }
                        draggingPosition = null
                    },
                    valueRange = 0f..duration.toFloat(),
                    enabled = current != null && durationMs > 0L
                )
                Text(
                    modifier = Modifier.widthIn(min = TIME_LABEL_MIN_WIDTH),
                    text = formatMusicDuration(durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

/**
 * 曲库区（中层右栏）：加载中 / 空状态 / 紧凑列表。
 *
 * 从主面板抽出来，是为了让「歌词 + 曲库」能并排放进同一个 weight 区域
 * （启动器是横屏，纵向空间比横向紧张），也让列表状态只在这一层 remember。
 */
@Composable
private fun LibrarySection(
    loading: Boolean,
    tracks: List<MusicTrack>,
    onImport: () -> Unit,
    onPlay: (MusicTrack) -> Unit,
    onDelete: (MusicTrack) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
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
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(40.dp),
                            painter = painterResource(R.drawable.ic_audio_file_filled),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.music_empty_title),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.music_empty_summary),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        FilledTonalButton(onClick = onImport) {
                            Text(text = stringResource(R.string.music_import))
                        }
                    }
                }
            }
            else -> {
                BackgroundCard(modifier = Modifier.fillMaxSize()) {
                    val listState = rememberLazyListState()
                    //切歌（包括底部的上一首/下一首）后把列表滚到当前曲目，
                    //否则「正在播放」高亮可能在屏幕外，看不出选中的是哪首（#80 选歌更好找）
                    val currentFileName = MusicPlayerController.currentTrack?.fileName
                    LaunchedEffect(currentFileName) {
                        val index = tracks.indexOfFirst { it.fileName == currentFileName }
                        if (index >= 0) listState.animateScrollToItem(index)
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(items = tracks, key = { it.fileName }) { track ->
                            val isCurrent =
                                MusicPlayerController.currentTrack?.fileName == track.fileName
                            MusicTrackRow(
                                track = track,
                                isCurrent = isCurrent,
                                onClick = { onPlay(track) },
                                onDelete = { onDelete(track) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 曲库中的一行（单行紧凑版）：曲名 / 时长 / 删除。
 *
 * 当前曲目整行加一层主题色底并让文字变主题色，比原来的「正在播放」小字更好认（#80）。
 */
@Composable
private fun MusicTrackRow(
    track: MusicTrack,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 1.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isCurrent) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                } else {
                    Color.Transparent
                }
            )
            .clickable(onClick = onClick)
            .padding(start = 8.dp, end = 2.dp, top = 2.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
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
        Text(
            modifier = Modifier.weight(1f),
            text = track.title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isCurrent) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = formatMusicDuration(track.durationMs),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        CompactIconAction(
            painter = painterResource(R.drawable.ic_delete_outlined),
            contentDescription = stringResource(R.string.generic_delete),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = onDelete
        )
    }
}

/**
 * 列表里的小图标按钮：用 32dp 热区而不是 [IconButton]（后者自带 48dp 最小触控区，
 * 会把「更密集」的列表行高又撑回去）。
 */
@Composable
private fun CompactIconAction(
    painter: Painter,
    contentDescription: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painter,
            contentDescription = contentDescription,
            tint = tint
        )
    }
}

/** 取系统选择器里选中文件的显示名；拿不到就退回 URI 最后一段，用来校验 .lrc 扩展名 */
private fun lyricsFileName(context: Context, uri: Uri): String {
    runCatching {
        context.contentResolver
            .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1 && cursor.moveToFirst()) {
                    cursor.getString(index)?.takeIf { it.isNotBlank() }?.let { return it }
                }
            }
    }
    return uri.lastPathSegment?.substringAfterLast('/').orEmpty()
}
