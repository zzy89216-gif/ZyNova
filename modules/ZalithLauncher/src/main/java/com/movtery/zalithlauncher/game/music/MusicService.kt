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
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

private const val TAG = "MusicService"

/**
 * 音乐播放前台服务（议题 #77）。
 *
 * 为什么需要它：进程切到后台/息屏后系统会回收普通进程，音乐就会「突然没了」；
 * 前台服务 + 常驻通知是系统允许的后台播放方式。
 *
 * 服务**不持有播放状态**，真正管理播放器的是 [MusicPlayerController]：
 * 服务只负责「让进程活着」「把通知栏动作 / 游戏内远程指令转发给 Controller」「把状态变化刷进通知」。
 *
 * 跨进程说明：游戏界面（`VMActivity`）在 `:game` 进程，它发来的 Intent 会落到这个服务
 * （服务在应用主进程），所以游戏内远程面板也能控制同一个播放器，不会出现第二个 ExoPlayer。
 *
 * 生命周期：
 * - 第一次播放时由 Controller 拉起并进入前台；
 * - 暂停时保持前台（用户要能从通知栏 / 游戏内面板恢复播放）；
 * - 用户停止播放 / 没有播放内容 / 通知被划掉 → 服务自停，通知一起消失。
 */
class MusicService : Service() {
    companion object {
        const val ACTION_START = "com.movtery.zalithlauncher.music.action.START"
        const val ACTION_TOGGLE = "com.movtery.zalithlauncher.music.action.TOGGLE"
        const val ACTION_NEXT = "com.movtery.zalithlauncher.music.action.NEXT"
        const val ACTION_PREVIOUS = "com.movtery.zalithlauncher.music.action.PREVIOUS"
        const val ACTION_STOP = "com.movtery.zalithlauncher.music.action.STOP"
        const val ACTION_SEEK = "com.movtery.zalithlauncher.music.action.SEEK"

        /** 游戏内远程面板：播放指定文件名的曲目 */
        const val ACTION_PLAY_FILE = "com.movtery.zalithlauncher.music.action.PLAY_FILE"

        const val EXTRA_FILE_NAME = "com.movtery.zalithlauncher.music.extra.FILE_NAME"
        const val EXTRA_POSITION_MS = "com.movtery.zalithlauncher.music.extra.POSITION_MS"

        /** 由 Controller 在「真正开始播放」时调用；失败不抛异常（例如后台启动被系统限制） */
        fun start(context: Context) {
            val intent = Intent(context, MusicService::class.java).setAction(ACTION_START)
            runCatching {
                ContextCompat.startForegroundService(context, intent)
            }.onFailure { throwable ->
                Logger.warning(TAG, "Failed to start the music service", throwable)
            }
        }

        /** 停止服务（通知与前台状态会一起移除） */
        fun stop(context: Context) {
            runCatching {
                context.stopService(Intent(context, MusicService::class.java))
            }.onFailure { throwable ->
                Logger.warning(TAG, "Failed to stop the music service", throwable)
            }
        }
    }

    /** 远程播放曲目时需要在 IO 上扫描曲库，这里用主线程作用域，扫描完回到主线程操作播放器 */
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /** Controller 状态变化时刷新通知；用固定引用便于 onDestroy 时精确解绑 */
    private val notificationUpdater: () -> Unit = {
        refreshNotification()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        MusicPlayerController.ensureInitialized(this)
        MusicPlayerController.onPlaybackStateChanged = notificationUpdater
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //通知栏动作也可能在服务已被回收后到达（系统重建服务），这里保证播放内核可用
        MusicPlayerController.ensureInitialized(this)
        MusicPlayerController.onPlaybackStateChanged = notificationUpdater

        val action = intent?.action

        //游戏内面板点某首歌曲：主进程重新扫描曲库（游戏进程导入的新文件也能立刻播到），
        //再交给播放器。扫描是异步的，所以这里直接返回、不自停，播放成功后会自己进前台。
        if (action == ACTION_PLAY_FILE) {
            val fileName = intent.getStringExtra(EXTRA_FILE_NAME)
            if (!fileName.isNullOrBlank()) {
                playFileByName(fileName)
            } else {
                stopSelf()
            }
            return START_NOT_STICKY
        }

        when (action) {
            ACTION_TOGGLE -> MusicPlayerController.toggle()
            ACTION_NEXT -> MusicPlayerController.next()
            ACTION_PREVIOUS -> MusicPlayerController.previous()
            ACTION_STOP -> MusicPlayerController.stop()
            ACTION_SEEK -> MusicPlayerController.seekTo(
                intent.getLongExtra(EXTRA_POSITION_MS, 0L)
            )
            else -> Unit
        }

        //「明确停止」或者「服务被拉起却没有可播放的内容」→ 不留空通知，直接自停。
        //注意：从通知栏按暂停（ACTION_TOGGLE 导致暂停）时 hasSession 仍然为 true，
        //所以服务与通知会继续保持，用户才能从通知栏恢复播放。
        if (action == ACTION_STOP || !MusicPlayerController.hasSession) {
            stopForeground(Service.STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        refreshNotification()
        return START_NOT_STICKY
    }

    /**
     * 主进程扫描曲库并播放指定文件。
     *
     * 每次都重新扫描而不是复用旧列表：曲库文件可能刚被游戏内面板导入，
     * 复用旧列表会「点了没反应」。扫描在 IO 线程，完成后回到主线程操作播放器。
     */
    private fun playFileByName(fileName: String) {
        serviceScope.launch {
            val tracks = MusicLibrary.list(this@MusicService)
            MusicPlayerController.setPlaylist(tracks)
            val track = tracks.firstOrNull { it.fileName == fileName }
            if (track == null) {
                Logger.warning(TAG, "Music file not found: $fileName")
                stopSelf()
                return@launch
            }
            MusicPlayerController.playTrack(track)
        }
    }

    private fun refreshNotification() {
        startForegroundCompat(MusicNotification.build(this))
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                MusicNotification.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(MusicNotification.NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        if (MusicPlayerController.onPlaybackStateChanged === notificationUpdater) {
            MusicPlayerController.onPlaybackStateChanged = null
        }
        stopForeground(Service.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
}
