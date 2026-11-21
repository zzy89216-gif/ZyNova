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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.utils.logging.Logger
import java.io.File

private const val TAG = "MusicPlayer"

/** 播放进度刷新间隔：500ms 足够让进度条看起来是连续的，又不至于每帧都读播放器 */
private const val PROGRESS_UPDATE_INTERVAL_MS = 500L

/** 每刷新 4 次进度（约 2 秒）把播放位置落一次盘，进程被杀时最多丢 2 秒进度 */
private const val PROGRESS_SAVE_TICK = 4

/** 被短暂压低时的音量（`AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK`） */
private const val DUCK_VOLUME = 0.2f

/**
 * 内置音乐播放器的进程级单例（议题 #77）。
 *
 * 为什么是单例而不是 ViewModel：
 * - 播放器必须比任意一个页面活得久：启动器页面被销毁、退出游戏页回到启动器、
 *   游戏内弹层关掉，音乐都不能断；
 * - 前台服务（[MusicService]）与界面都只操作这一个实例，两边状态永远一致。
 *
 * 线程约定：ExoPlayer 只能在创建它的 Looper 上访问，所以本对象的**所有公开方法都必须在主线程调用**
 * （Compose 点击回调、`LaunchedEffect`、`Service.onStartCommand` 都是主线程）。
 *
 * 音频焦点策略（维护者明确要求「边玩边听」不被压低）：
 * - 自己请求 `AUDIOFOCUS_GAIN`，走媒体流，**不会**把游戏或别的 App 的声音压小；
 * - 只有当别的 App 真的拿走焦点时才让位：永久丢失 → 暂停；短暂丢失 → 暂停并等焦点回来恢复；
 *   「可以 duck」→ 只降低自己的音量，绝不去动别人；
 * - 拔耳机（`ACTION_AUDIO_BECOMING_NOISY`）→ 立刻暂停。
 */
@OptIn(UnstableApi::class)
object MusicPlayerController {
    private val mainHandler = Handler(Looper.getMainLooper())

    /** 当前曲库（由界面扫描后同步进来，播放器的上一首/下一首以它为准） */
    var playlist: List<MusicTrack> by mutableStateOf(emptyList())
        private set

    /** 当前选中的曲目（可能还没开始播） */
    var currentTrack: MusicTrack? by mutableStateOf(null)
        private set

    /** 用户是否「要它播放」：正在缓冲时也是 true，界面据此显示暂停按钮 */
    var playRequested: Boolean by mutableStateOf(false)
        private set

    /** 播放器是否真的在出声（由 ExoPlayer 回调更新） */
    var isPlaying: Boolean by mutableStateOf(false)
        private set

    /** 当前媒体项是否已经装载进播放器（决定按播放时要不要重新 setMediaItem） */
    var isPrepared: Boolean by mutableStateOf(false)
        private set

    /** 当前进度（毫秒） */
    var positionMs: Long by mutableStateOf(0L)
        private set

    /** 当前曲目时长（毫秒；未知时为 0） */
    var durationMs: Long by mutableStateOf(0L)
        private set

    /** 需要展示给用户的错误（已本地化）；null 表示没有错误 */
    var lastError: String? by mutableStateOf(null)
        private set

    /** 播放状态变化回调：前台服务用它刷新通知栏（服务未启动时为 null） */
    var onPlaybackStateChanged: (() -> Unit)? = null

    /**
     * 当前是否存在播放会话（曲库已装载 / 有选中曲目）。
     *
     * 前台服务据此判断「暂停时保持」还是「没有内容时自停」；
     * 游戏内远程面板不读这个属性（跨进程），读的是 MMKV 快照。
     */
    val hasSession: Boolean
        get() = currentTrack != null || playlist.isNotEmpty()

    private var appContext: Context? = null
    private var player: ExoPlayer? = null

    /** 上次播放的曲目/进度：第一次拿到曲库时用来恢复，恢复成功后清空 */
    private var pendingRestoreFileName: String? = null
    private var pendingRestorePositionMs: Long = 0L

    /** 本轮「短暂丢失焦点」是否是播放中发生的；焦点恢复后据此决定要不要续播 */
    private var resumeAfterFocusGain = false

    /** 焦点请求对象缓存起来，避免每次都新建（也方便放弃焦点时一一对应） */
    private var audioFocusRequest: AudioFocusRequest? = null

    private var positionSaveTick = 0

    private val mediaAudioAttributes: AudioAttributes by lazy {
        AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
    }

    private val platformAudioAttributes: android.media.AudioAttributes by lazy {
        android.media.AudioAttributes.Builder()
            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
    }

    private val audioFocusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                player?.volume = 1f
                if (resumeAfterFocusGain) {
                    resumeAfterFocusGain = false
                    playCurrent()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                //永久失去焦点（电话 / 别的音乐 App 开始播放）：暂停并主动放弃焦点，
                //等用户自己再点播放时重新申请，避免跟对方来回抢
                resumeAfterFocusGain = false
                pauseInternal(abandonFocus = false)
                abandonAudioFocus()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                //短暂丢失（来电等）：先暂停，焦点回来后自动续播
                resumeAfterFocusGain = playRequested || isPlaying
                pauseInternal(abandonFocus = false)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                //只压低自己的音量；游戏/别的 App 的音量交给系统，不在这里处理
                player?.volume = DUCK_VOLUME
            }
        }
    }

    /** 拔耳机 / 断开蓝牙时系统会发这个广播：必须暂停，否则会突然外放 */
    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                pause()
            }
        }
    }

    private val progressRunnable = object : Runnable {
        override fun run() {
            val exo = player ?: return
            if (!exo.isPlaying) {
                positionMs = exo.currentPosition.coerceAtLeast(0L)
                return
            }
            positionMs = exo.currentPosition.coerceAtLeast(0L)
            val duration = exo.duration
            if (duration > 0L) durationMs = duration

            positionSaveTick++
            if (positionSaveTick >= PROGRESS_SAVE_TICK) {
                positionSaveTick = 0
                saveLastPlayed()
            }
            //游戏内远程面板（:game 进程）靠这个快照显示进度，每 500ms 同步一次
            savePlaybackSnapshot()
            mainHandler.postDelayed(this, PROGRESS_UPDATE_INTERVAL_MS)
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            this@MusicPlayerController.isPlaying = isPlaying
            if (!isPlaying) {
                positionMs = player?.currentPosition?.coerceAtLeast(0L) ?: positionMs
            }
            notifyPlaybackChanged()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    val duration = player?.duration ?: 0L
                    if (duration > 0L) durationMs = duration
                }
                Player.STATE_ENDED -> {
                    //列表循环：一首放完自动下一首（单曲循环类似行为，只是会从第一首重新开始）
                    next()
                }
                else -> Unit
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            val message = appContext?.getString(
                R.string.music_playback_failed,
                error.message ?: error.javaClass.simpleName
            ) ?: error.message
            lastError = message
            Logger.warning(TAG, "Music playback failed", error)
            playRequested = false
            isPlaying = false
            abandonAudioFocus()
            //播放失败后没有声音了，前台服务没有继续存在的理由：停掉服务，
            //错误信息留给界面展示（通知栏里挂一条「播放失败」没有意义）
            appContext?.let { MusicService.stop(it) }
            notifyPlaybackChanged()
        }
    }

    /**
     * 初始化播放内核（幂等，必须在主线程）。
     *
     * 界面第一次进来 / 前台服务第一次启动时都会调用；已经初始化过就直接返回。
     * 这里只创建播放器、注册耳机拔出广播并读取「上次播放」信息，
     * **不会自动开始播放**（不能让用户打开页面就突然有声音）。
     */
    fun ensureInitialized(context: Context) {
        if (player != null) return
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Logger.warning(TAG, "MusicPlayerController must be initialized on the main thread")
            return
        }

        val applicationContext = context.applicationContext
        appContext = applicationContext
        pendingRestoreFileName = MusicPreferences.lastTrackFileName
        pendingRestorePositionMs = MusicPreferences.lastPositionMs

        try {
            val created = ExoPlayer.Builder(applicationContext)
                //自己管理音频焦点（见文件头注释），所以这里传 false，避免两套机制互相打架
                .setAudioAttributes(mediaAudioAttributes, false)
                .setRenderersFactory(
                    DefaultRenderersFactory(applicationContext)
                        //与首页视频背景一致：硬解不可用时自动换软解
                        .setEnableDecoderFallback(true)
                )
                .build()
            created.addListener(playerListener)
            player = created
            registerBecomingNoisyReceiver(applicationContext)
            //进程重启后先清掉上一个进程遗留的播报快照：
            //否则游戏内远程面板会显示一个已经不存在的播放会话
            savePlaybackSnapshot()
        } catch (throwable: Throwable) {
            lastError = throwable.message ?: throwable.javaClass.simpleName
            Logger.error(TAG, "Failed to create the music player", throwable)
        }
    }

    /**
     * 同步曲库（界面扫描完成后调用）。
     *
     * 如果当前正在播的曲目还在新列表里，保持播放器不动（避免刷新列表把音乐打断）；
     * 否则按「上次播放 → 第一首」的顺序重新选中，并把上次的进度恢复出来。
     */
    fun setPlaylist(tracks: List<MusicTrack>) {
        playlist = tracks

        val current = currentTrack
        if (current != null) {
            val stillExists = tracks.firstOrNull { it.fileName == current.fileName }
            if (stillExists != null) {
                currentTrack = stillExists
                if (!isPrepared) durationMs = stillExists.durationMs
                //快照要跟着更新：游戏内远程面板靠它显示「当前曲目」，否则打开面板会看到空
                notifyPlaybackChanged()
                return
            }
            //当前曲目已经不在曲库里（被删了）：停掉播放与前台服务，下面重新选中
            player?.stop()
            isPrepared = false
            playRequested = false
            isPlaying = false
            abandonAudioFocus()
            appContext?.let { MusicService.stop(it) }
        }

        val restoredName = pendingRestoreFileName
        val restored = tracks.firstOrNull { it.fileName == restoredName } ?: tracks.firstOrNull()
        currentTrack = restored
        isPrepared = false
        durationMs = restored?.durationMs ?: 0L
        positionMs = if (restored != null && restored.fileName == restoredName) {
            pendingRestorePositionMs
        } else {
            0L
        }
        pendingRestoreFileName = null
        pendingRestorePositionMs = 0L
        notifyPlaybackChanged()
    }

    /** 播放指定曲目（从头开始） */
    fun playTrack(track: MusicTrack) {
        val context = appContext ?: return
        if (!prepareTrack(track, 0L)) return
        startPlayback(context)
    }

    /** 播放/继续当前选中的曲目 */
    fun playCurrent() {
        val context = appContext ?: return
        val exo = player ?: return
        val track = currentTrack ?: playlist.firstOrNull() ?: return
        currentTrack = track

        if (!isPrepared || exo.mediaItemCount == 0) {
            //还没装载过（刚打开页面 / 上次停止过）：按记录的进度继续
            if (!prepareTrack(track, positionMs)) return
        }
        startPlayback(context)
    }

    /** 播放/暂停切换 */
    fun toggle() {
        if (playRequested || isPlaying) {
            pause()
        } else {
            playCurrent()
        }
    }

    /** 用户主动暂停 */
    fun pause() {
        pauseInternal(abandonFocus = true)
    }

    /** 下一首（播放中会接着播，暂停中只切换不自动开播） */
    fun next() {
        skip(1)
    }

    /** 上一首 */
    fun previous() {
        skip(-1)
    }

    /** 停止播放并让前台服务自停；当前曲目保留在列表里，下次按播放从头开始 */
    fun stop() {
        val context = appContext
        mainHandler.removeCallbacks(progressRunnable)
        player?.stop()
        playRequested = false
        isPlaying = false
        isPrepared = false
        positionMs = 0L
        abandonAudioFocus()
        currentTrack?.let { MusicPreferences.saveLastPlayed(it.fileName, 0L) }
        if (context != null) {
            MusicService.stop(context)
        }
        notifyPlaybackChanged()
    }

    /** 进度条拖动：装载前也能记住目标进度，等真正 prepare 时生效 */
    fun seekTo(targetPositionMs: Long) {
        val target = targetPositionMs.coerceAtLeast(0L)
        val exo = player
        if (exo != null && exo.mediaItemCount > 0) {
            runCatching { exo.seekTo(target) }
        }
        positionMs = target
        saveLastPlayed()
    }

    /**
     * 曲库里的文件被删除后同步到播放器。
     *
     * 删的正是当前曲目时：先停掉播放（不能继续读一个已经删掉的文件），再清掉选中状态。
     */
    fun removeTrack(track: MusicTrack) {
        val removingCurrent = currentTrack?.fileName == track.fileName
        if (removingCurrent) {
            stop()
            currentTrack = null
            isPrepared = false
            durationMs = 0L
            positionMs = 0L
            MusicPreferences.clearLastPlayed()
        }
        playlist = playlist.filterNot { it.fileName == track.fileName }
        if (playlist.isEmpty()) {
            player?.stop()
            isPrepared = false
            playRequested = false
            isPlaying = false
            durationMs = 0L
            positionMs = 0L
        }
        notifyPlaybackChanged()
    }

    /** 列表刷新/删除后，把「当前曲目已不在曲库」的显示状态一起收干净 */
    fun clearIfMissing() {
        val current = currentTrack ?: return
        if (playlist.none { it.fileName == current.fileName }) {
            removeTrack(current)
        }
    }

    private fun skip(delta: Int) {
        val context = appContext ?: return
        if (playlist.isEmpty()) return

        val current = currentTrack
        val currentIndex = if (current == null) {
            -1
        } else {
            playlist.indexOfFirst { it.fileName == current.fileName }
        }
        val size = playlist.size
        val targetIndex = (((currentIndex + delta) % size) + size) % size
        val target = playlist[targetIndex]

        val shouldContinuePlaying = playRequested || isPlaying
        if (!prepareTrack(target, 0L)) return
        if (shouldContinuePlaying) {
            startPlayback(context)
        }
    }

    private fun pauseInternal(abandonFocus: Boolean) {
        mainHandler.removeCallbacks(progressRunnable)
        playRequested = false
        player?.pause()
        positionMs = player?.currentPosition?.coerceAtLeast(0L) ?: positionMs
        isPlaying = false
        if (abandonFocus) {
            abandonAudioFocus()
        }
        saveLastPlayed()
        notifyPlaybackChanged()
    }

    /** 装载曲目；成功返回 true。装载失败会把错误写进 [lastError] 并让界面显示 */
    private fun prepareTrack(track: MusicTrack, startPositionMs: Long): Boolean {
        val exo = player ?: return false
        currentTrack = track
        durationMs = track.durationMs
        val startPosition = startPositionMs.coerceAtLeast(0L)
        positionMs = startPosition

        try {
            exo.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(track.path))))
            if (startPosition > 0L) {
                exo.seekTo(startPosition)
            }
            exo.prepare()
        } catch (throwable: Throwable) {
            lastError = throwable.message ?: throwable.javaClass.simpleName
            Logger.warning(TAG, "Failed to prepare ${track.fileName}", throwable)
            return false
        }

        isPrepared = true
        lastError = null
        saveLastPlayed()
        notifyPlaybackChanged()
        return true
    }

    private fun startPlayback(context: Context) {
        val exo = player ?: return
        if (!requestAudioFocus(context)) {
            lastError = context.getString(R.string.music_focus_denied)
            playRequested = false
            isPlaying = false
            notifyPlaybackChanged()
            return
        }
        lastError = null
        playRequested = true
        player?.volume = 1f
        exo.play()
        scheduleProgressUpdates()
        //拿到焦点、真正开始播放后才拉起前台服务：让通知栏与「后台可播放」同时成立
        MusicService.start(context)
        notifyPlaybackChanged()
    }

    private fun scheduleProgressUpdates() {
        mainHandler.removeCallbacks(progressRunnable)
        mainHandler.post(progressRunnable)
    }

    private fun saveLastPlayed() {
        val track = currentTrack ?: return
        MusicPreferences.saveLastPlayed(track.fileName, positionMs)
    }

    /**
     * 把当前播放状态写进 MMKV，供 `:game` 进程的游戏内远程面板读取。
     *
     * 游戏界面不能自己创建播放器（会变成两个 ExoPlayer 同时出声），
     * 所以游戏内看到的一切都由这份快照驱动。
     */
    private fun savePlaybackSnapshot() {
        MusicPreferences.savePlaybackSnapshot(
            MusicPreferences.PlaybackSnapshot(
                hasSession = hasSession,
                fileName = currentTrack?.fileName.orEmpty(),
                title = currentTrack?.title.orEmpty(),
                isPlaying = playRequested,
                positionMs = positionMs,
                durationMs = durationMs,
                playlistSize = playlist.size,
                error = lastError
            )
        )
    }

    private fun requestAudioFocus(context: Context): Boolean {
        val manager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return true
        return try {
            val request = audioFocusRequest
                ?: AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(platformAudioAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .setOnAudioFocusChangeListener(audioFocusListener)
                    .build()
                    .also { audioFocusRequest = it }
            manager.requestAudioFocus(request) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } catch (throwable: Throwable) {
            Logger.warning(TAG, "Failed to request audio focus", throwable)
            false
        }
    }

    private fun abandonAudioFocus() {
        val context = appContext ?: return
        val request = audioFocusRequest ?: return
        runCatching {
            (context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager)
                ?.abandonAudioFocusRequest(request)
        }
    }

    private fun registerBecomingNoisyReceiver(context: Context) {
        runCatching {
            ContextCompat.registerReceiver(
                context,
                becomingNoisyReceiver,
                IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }.onFailure { throwable ->
            Logger.warning(TAG, "Failed to register the becoming-noisy receiver", throwable)
        }
    }

    private fun notifyPlaybackChanged() {
        savePlaybackSnapshot()
        runCatching { onPlaybackStateChanged?.invoke() }
            .onFailure { Logger.warning(TAG, "Failed to refresh the music notification", it) }
    }
}
