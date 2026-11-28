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

package com.movtery.zalithlauncher.frp

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.notification.NotificationChannelData

/**
 * 樱花内网穿透的前台服务。
 *
 * 为什么必须要有：
 * - Android 在息屏 / 切后台后会回收普通进程，frpc 一被回收隧道就断，
 *   游戏里表现为「突然掉线、再也连不上」；
 * - 前台服务 + 常驻通知是系统允许的唯一长期存活方式（与音乐播放器同理）。
 *
 * 服务本身不持有隧道状态，真正管理进程的是 [FrpManager]；
 * 这里只负责「让进程活着」以及在被回收时把子进程一起收掉。
 */
class FrpService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 10027

        fun start(context: Context) {
            val intent = Intent(context, FrpService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            runCatching {
                context.stopService(Intent(context, FrpService::class.java))
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundCompat(buildNotification())
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val channel = NotificationChannelData.FRP_SERVICE_CHANNEL
        return NotificationCompat.Builder(this, channel.channelId)
            .setContentTitle(getString(R.string.frp_notification_title))
            .setContentText(getString(R.string.frp_notification_text))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .build()
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        // 服务被系统回收 / 用户主动停止时，同步结束 frpc，避免留下孤儿进程
        FrpManager.onServiceDestroyed()
        super.onDestroy()
    }
}
