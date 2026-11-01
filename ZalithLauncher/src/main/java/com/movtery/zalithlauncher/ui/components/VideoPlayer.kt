/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.ui.components

import android.net.Uri
import androidx.annotation.FloatRange
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.movtery.zalithlauncher.viewmodel.BackgroundVideoController

/**
 * 沉浸式视频背景播放层（议题 #38 重写）
 *
 * 与旧实现的区别：
 * - **不在组合期创建播放器**：播放器由 [BackgroundVideoController] 统一持有，
 *   这里只负责把画面挂到界面上，首帧布局不会再被创建解码器拖慢；
 * - **首帧门控 + 淡入**：真正出画之前这一层是完全透明的（露出下层稳定的背景底色），
 *   出画后再用 220ms 淡入，不会再出现「先白/黑一下、视频突然跳出来」；
 * - **复用同一个播放器**：换视频只换媒体项，Activity 重建也不会重新建解码器。
 *
 * @param autoPlay 准备好后，是否立即播放视频
 * @param loop 是否循环播放视频
 * @param volume 音量（0..1），背景视频默认静音
 * @param refreshTrigger 背景内容是否变化；变化时才重新加载媒体项
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUri: Uri,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true,
    loop: Boolean = true,
    @FloatRange(from = 0.0, to = 1.0) volume: Float = 0f,
    resizeMode: Int = AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
    refreshTrigger: Any? = null
) {
    val context = LocalContext.current
    val controller = BackgroundVideoController
    val player = controller.player
    val firstFrameRendered = controller.firstFrameRendered

    //请求播放：控制器内部会把「创建播放器」延后到当前组合之后
    LaunchedEffect(videoUri, refreshTrigger) {
        controller.play(context, videoUri, restart = true)
    }

    LaunchedEffect(autoPlay) { controller.setPlayWhenReady(autoPlay) }
    LaunchedEffect(loop) { controller.setLoop(loop) }
    LaunchedEffect(volume) { controller.setVolume(volume) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                //后台只暂停，不释放解码器；回前台继续播，首帧门控会自然复用
                Lifecycle.Event.ON_STOP -> controller.pause()
                Lifecycle.Event.ON_START -> if (autoPlay) controller.resume()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var playerView by remember { mutableStateOf<PlayerView?>(null) }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            PlayerView(ctx).apply {
                useController = false
                this.resizeMode = resizeMode
                //首帧之前整层透明：让下层稳定的背景底色可见，避免黑屏/白屏
                alpha = 0f
            }.also { playerView = it }
        },
        update = { view ->
            //播放器实例变了才重新绑定，避免每次重组都重置画面
            if (player != null && view.player !== player) {
                view.player = player
            }
        }
    )

    //出画之后再做淡入：用 View 自己的 alpha 动画，不再额外套一层 Compose 图层
    LaunchedEffect(firstFrameRendered, player) {
        val view = playerView ?: return@LaunchedEffect
        view.animate()
            .alpha(if (firstFrameRendered) 1f else 0f)
            .setDuration(if (firstFrameRendered) 220L else 0L)
            .start()
    }
}
