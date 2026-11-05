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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * 樱花内网穿透（SakuraFrp）相关的数据模型。
 *
 * 字段名与官方 OpenAPI v4 保持一致（snake_case 通过 @SerialName 映射），
 * 所有字段都给了默认值，避免服务端新增字段导致解析失败。
 */

/**
 * 用户信息（`GET /v4/user/info`）。
 *
 * 只取界面真正需要的字段；`traffic` 的结构在不同账号组之间不完全一致，
 * 因此保留为 [JsonElement]，由上层按需解析，避免强类型导致整体解析失败。
 */
@Serializable
data class FrpUser(
    val id: Int = 0,
    val name: String = "",
    val realname: Boolean = false,
    val group: String = "",
    val sign: Boolean = false,
    val traffic: JsonElement? = null
)

/**
 * 隧道信息（`GET /v4/tunnels`）。
 */
@Serializable
data class FrpTunnel(
    val id: Int = 0,
    val name: String = "",
    /** 节点 ID，对应 `/v4/nodes` 返回的对象键 */
    val node: Int = 0,
    /** tcp / udp / http / https / wol / etcp / eudp */
    val type: String = "tcp",
    /** 0 正常，2 封禁 */
    val status: Int = 0,
    @SerialName("status_reason") val statusReason: String? = null,
    val online: Boolean = false,
    val note: String? = null,
    val extra: String? = null,
    @SerialName("local_ip") val localIp: String = "127.0.0.1",
    @SerialName("local_port") val localPort: Int = 0,
    /** 远程端口或绑定域名 */
    val remote: String? = null,
    /** 端口导出状态：`类型|IP 地址|端口` */
    val export: String? = null
) {
    val isBanned: Boolean get() = status == 2

    val isTcp: Boolean get() = type.equals("tcp", ignoreCase = true)
}

/**
 * 节点信息（`GET /v4/nodes`，返回 `{ 节点ID: 节点信息 }`）。
 */
@Serializable
data class FrpNode(
    val name: String = "",
    val host: String = "",
    val description: String = "",
    val vip: Int = 0,
    val flag: Int = 0
) {
    /** 是否允许创建隧道（1 << 2），且节点未离线（1 << 9） */
    val allowCreate: Boolean
        get() = (flag and (1 shl 2)) != 0 && (flag and (1 shl 9)) == 0

    /** 是否允许穿透公开游戏服务器（1 << 11） */
    val allowPublicGame: Boolean
        get() = (flag and (1 shl 11)) != 0

    /** 节点是否离线 */
    val offline: Boolean
        get() = (flag and (1 shl 9)) != 0
}

/**
 * 创建隧道的返回结果（`POST /v4/tunnels`）。
 */
@Serializable
data class FrpCreateResult(
    val id: Int = 0,
    val name: String = "",
    val remote: String? = null,
    @SerialName("important_warning_for_ai") val warning: String? = null
)

/**
 * 接口错误返回：`{ "code": 123, "msg": "..." }`
 */
@Serializable
data class FrpApiError(
    val code: Int = 0,
    val msg: String = ""
)

/**
 * 隧道运行状态。
 */
sealed interface FrpRunState {
    /** 未启动 */
    data object Idle : FrpRunState

    /** 正在拉起 frpc */
    data object Starting : FrpRunState

    /** 运行中 */
    data class Running(val tunnelId: Int) : FrpRunState

    /** 已退出（正常退出或异常退出） */
    data class Stopped(val exitCode: Int) : FrpRunState

    /** 启动失败 */
    data class Failed(val message: String) : FrpRunState
}
