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

package com.movtery.zalithlauncher.game.account

import com.movtery.zalithlauncher.game.account.auth_server.data.AuthServer

/**
 * LittleSkin 快捷登录。
 *
 * LittleSkin 是国内的皮肤站，同时提供 **Yggdrasil 认证**（authlib-injector 兼容），
 * 所以它其实就是一个普通的「第三方验证服务器」——只不过地址、注册链接都是固定的，
 * 没必要让用户自己去查、去手填。
 *
 * 这里做两件事：
 * 1. 把 LittleSkin 作为**内置服务器**直接写进认证服务器表（不联网、不走 `addOtherServer` 的探测流程，
 *    因为地址是写死的，探测只会多等几秒）；
 * 2. 写入后直接打开账号密码对话框，用户输完就能进游戏。
 *
 * ⚠️ 地址不要随手改：`/api/yggdrasil` 是 authlib-injector 的 API 根，
 * 登录会拼成 `<root>/authserver/authenticate`，写成站点首页会 404。
 */
object LittleSkin {
    /** 显示名 */
    const val NAME = "LittleSkin"

    /** authlib-injector API 根地址 */
    const val BASE_URL = "https://littleskin.cn/api/yggdrasil"

    /** 注册页（登录对话框里的「注册」按钮会打开它） */
    const val REGISTER_URL = "https://littleskin.cn/auth/register"

    /** 内置服务器条目 */
    val SERVER: AuthServer = AuthServer(
        baseUrl = BASE_URL,
        serverName = NAME,
        register = REGISTER_URL
    )
}
