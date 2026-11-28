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

package com.movtery.zalithlauncher.game.account.microsoft

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.microsoft.MinecraftProfileException.ExceptionStatus.APP_NOT_REGISTERED
import com.movtery.zalithlauncher.game.account.microsoft.MinecraftProfileException.ExceptionStatus.BLOCKED_IP
import com.movtery.zalithlauncher.game.account.microsoft.MinecraftProfileException.ExceptionStatus.FREQUENT
import com.movtery.zalithlauncher.game.account.microsoft.MinecraftProfileException.ExceptionStatus.PROFILE_NOT_EXISTS
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText

/**
 * Minecraft 配置获取异常
 *
 * @param status 业务状态，界面通过 [toLocal] 转换为用户可读文案
 * @param message 诊断用信息（例如真实 HTTP 状态码与来源 URL）。
 *                仅用于日志排查，不参与业务判断。
 */
class MinecraftProfileException(
    val status: ExceptionStatus,
    message: String? = null
) : RuntimeException(message) {
    enum class ExceptionStatus {
        /**
         * 登陆过于频繁
         */
        FREQUENT,

        /**
         * IP 地址被禁止
         */
        BLOCKED_IP,

        /**
         * 未创建配置
         */
        PROFILE_NOT_EXISTS,

        /**
         * 该应用注册未被 Minecraft 服务端认可
         *
         * Minecraft Services 返回 HTTP 403 且响应体为
         * `Invalid app registration, see https://aka.ms/AppRegInfo`。
         * 这与 IP 被封（[BLOCKED_IP]）是**两件完全不同的事**：
         * 前者是启动器的 Client ID 不在 Mojang 的允许名单里，用户换 IP 也没用。
         */
        APP_NOT_REGISTERED
    }
}

fun MinecraftProfileException.toLocal(): AndroidStringText {
    return androidText(
        when (status) {
            FREQUENT -> R.string.account_logging_frequent
            BLOCKED_IP -> R.string.account_logging_blocked_ip
            PROFILE_NOT_EXISTS -> R.string.account_logging_profile_not_exists
            APP_NOT_REGISTERED -> R.string.account_logging_app_not_registered
        }
    )
}