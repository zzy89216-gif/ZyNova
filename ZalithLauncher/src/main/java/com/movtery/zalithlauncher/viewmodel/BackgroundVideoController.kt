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

package com.movtery.zalithlauncher.viewmodel

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import com.movtery.zalithlauncher.utils.logging.Logger

private const val TAG = "BackgroundVideo"

/**
 * 首页视频背景的播放器持有者（议题 #38）
 *
 * 背景视频在旧版本里是「组合期同步 new ExoPlayer + 立刻挂 PlayerView」，
 * 由此带来三个问题：启动瞬间卡一下、首帧出来之前先闪一下、切前后台反复重建解码器。
 * 这里把播放器从 Composition 里拆出来，统一按下面几条规则管理：
 *
 * 1. **不在组合期创建**：第一次请求时 `post` 到主线程队列末尾再建，
 *    避免和首帧布局抢同一帧预算；
 * 2. **首帧门控**：[firstFrameRendered] 只在真正出画后置 true，
 *    界面据此做淡入，出画前显示稳定的占位底色；
 * 3. **同一实例复用**：换视频只 `setMediaItem + prepare`，不重建播放器；
 * 4. **前后台只暂停不销毁**：回到前台继续播，真正释放交给 ViewModel 清理。
 *
 * ⚠️ ExoPlayer 必须在同一个 Looper 上访问，所以构建与所有操作都留在主线程，
 * 只是**错开**了组合期，不是搬到子线程。
 */
@OptIn(UnstableApi::class)
object BackgroundVideoController {
    private val mainHandler = Handler(Looper.getMainLooper())

    /** 当前播放器；为 null 表示还没建好（界面此时显示占位底色） */
    var player: ExoPlayer? by mutableStateOf(null)
        private set

    /** 当前这次准备是否已经出画（用于首帧淡入） */
    var firstFrameRendered: Boolean by mutableStateOf(false)
        private set

    /** 播放器报错（解码失败等），界面用占位底色兜底，不影响其它 UI */
    var lastError: Throwable? by mutableStateOf(null)
        private set

    private var pendingUri: Uri? = null
    private var pendingRestart: Boolean = false
    private var building: Boolean = false

    /**
     * 请求播放某个视频。
     *
     * @param restart 内容变了（重新导入 / 删除后重建）时传 true，
     *                会强制重新 `setMediaItem`；只是界面重组则复用当前媒体项，
     *                避免反复 prepare 造成的重复解码。
     */
    fun play(context: Context, uri: Uri, restart: Boolean) {
        pendingUri = uri
        pendingRestart = restart

        val current = player
        if (current != null) {
            applyMediaIfNeeded(current, uri, restart)
            return
        }
        if (building) return

        building = true
        // 延后到当前组合 / 首帧之后再建播放器
        mainHandler.post {
            building = false
            if (player != null) return@post
            val appContext = context.applicationContext
            runCatching {
                ExoPlayer.Builder(appContext)
                    .setRenderersFactory(
                        DefaultRenderersFactory(appContext)
                            //开启解码器 fallback：某些机器默认硬解不可用时自动换软解
                            .setEnableDecoderFallback(true)
                    )
                    .build()
            }.onSuccess { created ->
                created.addListener(object : Player.Listener {
                    override fun onRenderedFirstFrame() {
                        firstFrameRendered = true
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        lastError = error
                        Logger.warning(TAG, "Background video playback failed", error)
                    }
                })
                player = created
                pendingUri?.let { applyMediaIfNeeded(created, it, restart = true) }
            }.onFailure { th ->
                lastError = th
                Logger.warning(TAG, "Failed to create background video player", th)
            }
        }
    }

    /** 暂停（切后台 / 首页不可见） */
    fun pause() {
        player?.pause()
    }

    /** 继续播放（回到前台） */
    fun resume() {
        //不判断首帧：如果用户恰好在准备阶段切到后台，回到前台仍然要继续播，
        //否则会永远停在暂停状态（首帧门控只负责「看不看得见」，不负责「播不播」）
        player?.play()
    }

    /** 设置音量为 0..1；背景视频默认静音 */
    fun setVolume(volume: Float) {
        player?.volume = volume.coerceIn(0f, 1f)
    }

    /** 设置是否循环 */
    fun setLoop(loop: Boolean) {
        player?.repeatMode = if (loop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }

    /** 设置是否自动播放 */
    fun setPlayWhenReady(play: Boolean) {
        player?.playWhenReady = play
    }

    /**
     * 真正释放播放器。
     *
     * 只在背景 ViewModel 被清理（退出启动器 / 切换账号等真正结束的场景）时调用；
     * Activity 重建不要调用，否则每次重建都会重新建解码器。
     */
    fun release() {
        val current = player ?: return
        player = null
        firstFrameRendered = false
        pendingUri = null
        runCatching { current.release() }
    }

    private fun applyMediaIfNeeded(target: ExoPlayer, uri: Uri, restart: Boolean) {
        val currentItem = target.currentMediaItem
        val sameMedia = currentItem?.localConfiguration?.uri == uri
        if (sameMedia && !restart && target.playbackState != Player.STATE_IDLE) return

        firstFrameRendered = false
        lastError = null
        target.setMediaItem(MediaItem.fromUri(uri))
        target.prepare()
        target.playWhenReady = true
    }
}
