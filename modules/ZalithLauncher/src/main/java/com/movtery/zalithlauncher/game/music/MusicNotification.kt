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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.movtery.zalithlauncher.R

/**
 * 音乐播放通知（议题 #77）。
 *
 * 刻意**不引入 media3-session**：项目依赖里没有它，而 MVP 需要的
 * 「播放/暂停、上一首、下一首、点回启动器」用 `NotificationCompat` 手写完全够用，
 * 少一个依赖就少一份需要同步 `THIRD_PARTY_NOTICES` 的维护成本。
 */
object MusicNotification {
    const val NOTIFICATION_ID = 10077

    private const val CHANNEL_ID = "music.playback"

    private const val REQUEST_CODE_CONTENT = 100
    private const val REQUEST_CODE_PREVIOUS = 101
    private const val REQUEST_CODE_TOGGLE = 102
    private const val REQUEST_CODE_NEXT = 103
    //通知栏右上角划掉通知 / 系统清理通知时触发「停止播放」
    private const val REQUEST_CODE_STOP = 104

    private const val PENDING_INTENT_FLAGS =
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    /** 构建当前状态对应的通知（播放中显示暂停按钮，暂停时显示播放按钮） */
    fun build(context: Context): Notification {
        ensureChannel(context)

        val playing = MusicPlayerController.playRequested
        val track = MusicPlayerController.currentTrack
        val title = track?.title ?: context.getString(R.string.music_title)
        //有歌词且要播放时优先显示歌词行（暂停时保持「已暂停」，避免让用户以为还在唱）
        val lyricLine = if (playing) buildLyricContentText() else null
        val text = when {
            MusicPlayerController.lastError != null -> MusicPlayerController.lastError!!
            lyricLine != null -> lyricLine
            playing -> context.getString(R.string.music_notification_playing)
            else -> context.getString(R.string.music_notification_paused)
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(contentIntent(context))
            //用户把通知划掉 = 停止播放（否则会留下一个无法关闭的常驻通知）
            .setDeleteIntent(serviceIntent(context, MusicService.ACTION_STOP, REQUEST_CODE_STOP))
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setOngoing(playing)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (lyricLine != null) {
            //歌词可能是「上一行 + 当前行」两行：用 BigTextStyle 让展开后的通知能显示两行
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(lyricLine))
        }

        builder
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_skip_previous_filled,
                    context.getString(R.string.music_previous),
                    serviceIntent(context, MusicService.ACTION_PREVIOUS, REQUEST_CODE_PREVIOUS)
                ).build()
            )
            .addAction(
                NotificationCompat.Action.Builder(
                    if (playing) R.drawable.ic_pause_filled else R.drawable.ic_play_arrow_filled,
                    context.getString(if (playing) R.string.music_pause else R.string.music_play),
                    serviceIntent(context, MusicService.ACTION_TOGGLE, REQUEST_CODE_TOGGLE)
                ).build()
            )
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.ic_skip_next_filled,
                    context.getString(R.string.music_next),
                    serviceIntent(context, MusicService.ACTION_NEXT, REQUEST_CODE_NEXT)
                ).build()
            )
        return builder.build()
    }

    /**
     * 通知栏歌词文本（议题 #80）：有歌词且正在播放时显示「上一行 + 当前行」。
     *
     * 只在当前曲目 / 歌词行发生变化时才会被调用（由 [MusicPlayerController.onPlaybackStateChanged]
     * 这条现有链路驱动），不参与 500ms 进度刷新，所以不会每半秒重建一次通知。
     */
    private fun buildLyricContentText(): String? {
        val lines = MusicPlayerController.lyrics
        val index = MusicPlayerController.currentLyricIndex
        if (index < 0) return null

        val current = lines.getOrNull(index)?.text?.trim().orEmpty()
        if (current.isEmpty()) return null
        //index = 0 时 getOrNull(-1) 返回 null（Kotlin 不越界），只显示当前行——自审已核对
        val previous = lines.getOrNull(index - 1)?.text?.trim().orEmpty()
        return if (previous.isEmpty()) current else "$previous\n$current"
    }

    /** 播放通知渠道，只需要创建一次（服务每次刷新通知前调用，内部已做存在判断） */
    private fun ensureChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.music_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.music_notification_channel_description)
            setShowBadge(false)
            setSound(null, null)
        }
        manager.createNotificationChannel(channel)
    }

    /** 点击通知回到启动器（复用系统桌面入口，不手动构造 MainActivity，避免导航状态不完整） */
    private fun contentIntent(context: Context): PendingIntent {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent(Intent.ACTION_MAIN).setPackage(context.packageName)
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return PendingIntent.getActivity(
            context,
            REQUEST_CODE_CONTENT,
            launchIntent,
            PENDING_INTENT_FLAGS
        )
    }

    private fun serviceIntent(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, MusicService::class.java).setAction(action)
        return PendingIntent.getService(context, requestCode, intent, PENDING_INTENT_FLAGS)
    }
}
