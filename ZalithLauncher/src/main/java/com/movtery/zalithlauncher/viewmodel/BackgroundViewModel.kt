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

package com.movtery.zalithlauncher.viewmodel

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movtery.zalithlauncher.context.copyLocalFile
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.image.isImageFile
import com.movtery.zalithlauncher.utils.video.isVideoFile
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File

/**
 * 启动器背景管理
 */
class BackgroundViewModel: ViewModel() {
    // HazeState 默认使用 Auto 位置策略
    val hazeState = HazeState()

    val backgroundFile: File = PathManager.FILE_LAUNCHER_BACKGROUND

    /**
     * 背景文件是否有效（存在，且是有效的视频或图片）
     */
    var isValid by mutableStateOf(false)
        private set

    /**
     * 背景文件是一个视频
     */
    var isVideo by mutableStateOf(false)
        private set

    /**
     * 背景文件是一个图片
     */
    var isImage by mutableStateOf(false)
        private set

    var refreshTrigger by mutableStateOf(false)
        private set

    /**
     * 当前壁纸的平均亮度（0 = 全黑，1 = 全白）；视频或算不出来时为 null。
     *
     * 用途：自适应遮罩（议题 #46）。亮壁纸 + 浅色文字 = 根本看不清写的是什么，
     * 有了这个值就能决定要不要在文字底下压一层深的 scrim、以及压多重。
     */
    var backgroundLuminance by mutableStateOf<Float?>(null)
        private set

    /**
     * 背景是否「已经可以看」。
     *
     * 图片：文件有效即可见；
     * 视频：要等播放器**真正画出第一帧**才算可见（议题 #38）。
     *
     * 首页背景透明度、卡片配色都跟着这个值走：
     * 视频还没出画时整屏保持不透明，出画后再一起变透明，
     * 就不会出现「先变透明、视频过一会儿才出来」的二次跳变。
     */
    val isBackgroundVisible: Boolean
        get() = isValid && (isImage || (isVideo && BackgroundVideoController.firstFrameRendered))

    /**
     * 取壁纸平均亮度：把图片解码成极小缩略图再平均，成本可以忽略（一次性）。
     */
    private fun computeLuminance(): Float? = runCatching {
        val options = BitmapFactory.Options().apply { inSampleSize = 16 }
        val bitmap = BitmapFactory.decodeFile(backgroundFile.absolutePath, options) ?: return null
        var sum = 0.0
        var count = 0
        // 最多采样 24x24 个点，避免大图遍历
        val stepX = (bitmap.width / 24).coerceAtLeast(1)
        val stepY = (bitmap.height / 24).coerceAtLeast(1)
        var x = 0
        while (x < bitmap.width) {
            var y = 0
            while (y < bitmap.height) {
                val pixel = bitmap.getPixel(x, y)
                val r = (pixel shr 16 and 0xFF) / 255.0
                val g = (pixel shr 8 and 0xFF) / 255.0
                val b = (pixel and 0xFF) / 255.0
                // 感知亮度（ITU-R BT.601）
                sum += 0.299 * r + 0.587 * g + 0.114 * b
                count++
                y += stepY
            }
            x += stepX
        }
        bitmap.recycle()
        if (count == 0) null else (sum / count).toFloat()
    }.getOrNull()

    private suspend fun updateState() {
        val (isImage0, isVideo0) = withContext(Dispatchers.IO) {
            val isImage = backgroundFile.isImageFile()
            //如果文件是图片，则不检查是否为视频
            val isVideo = if (!isImage) backgroundFile.isVideoFile() else false
            isImage to isVideo
        }
        //图片壁纸：顺便算一次亮度（视频不参与，返回 null 走默认策略）
        val luminance = withContext(Dispatchers.IO) {
            if (isImage0 && backgroundFile.exists()) computeLuminance() else null
        }
        //更新状态
        withContext(Dispatchers.Main) {
            backgroundLuminance = luminance
            isImage = isImage0
            isVideo = isVideo0
            isValid = backgroundFile.exists() && (isImage0 || isVideo0)
            refreshTrigger = !refreshTrigger
        }
    }

    init {
        viewModelScope.launch(Dispatchers.Main) {
            updateState()
        }
    }

    suspend fun delete() {
        withContext(Dispatchers.IO) {
            FileUtils.deleteQuietly(backgroundFile)
            updateState()
        }
    }

    suspend fun import(context: Context, result: Uri) {
        withContext(Dispatchers.IO) {
            FileUtils.deleteQuietly(backgroundFile)
            context.copyLocalFile(result, backgroundFile)
            if (!backgroundFile.isImageFile() && !backgroundFile.isVideoFile()) {
                //不是媒体类文件
                FileUtils.deleteQuietly(backgroundFile)
                error("The selected file is not an image or a video!")
            }
            updateState()
        }
    }

    /**
     * 退出启动器（ViewModel 被清理）时才真正释放播放器。
     *
     * ⚠️ 不要放到 Activity.onDestroy：切深色模式 / 改字体大小会重建 Activity，
     * 那样每次重建都要重新建一次解码器，正是议题 #38 里「卡顿、闪屏」的来源之一。
     */
    override fun onCleared() {
        BackgroundVideoController.release()
        super.onCleared()
    }
}

/**
 * 本地随时可以使用的背景图片管理 ViewModel
 * 由 MainActivity 的主题提供
 */
val LocalBackgroundViewModel = compositionLocalOf<BackgroundViewModel?> {
    error("No BackgroundViewModel provided")
}

@Composable
fun <E> influencedByBackground(
    value: E,
    influenced: E,
    enabled: Boolean = true
): E {
    return if (enabled) {
        //议题 #38：视频要等首帧出画后才算「有背景」，卡片配色与画面同时变化
        if (LocalBackgroundViewModel.current?.isBackgroundVisible == true) {
            influenced
        } else {
            value
        }
    } else {
        value
    }
}