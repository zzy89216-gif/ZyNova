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

package com.movtery.zalithlauncher.ai.agent.tools

import com.movtery.zalithlauncher.ai.agent.AITool
import com.movtery.zalithlauncher.ai.agent.AIToolRisk
import com.movtery.zalithlauncher.ai.agent.AIToolSchema
import com.movtery.zalithlauncher.context.GlobalContext
import com.movtery.zalithlauncher.frp.FrpManager
import com.movtery.zalithlauncher.frp.FrpRunState
import com.movtery.zalithlauncher.setting.AllSettings

/**
 * 樱花内网穿透 / 联机相关的 Agent 工具。
 *
 * 设计原则：
 * - **复用 [FrpManager]**：不在工具层重新实现登录、建隧道、起进程；
 * - 只暴露「能真正跑通」的动作：查状态、建隧道、启动、停止；
 * - 建隧道时类型固定 TCP、端口自动识别（与界面一致），模型不用关心这些细节。
 */
internal fun frpTools(): List<AITool> = listOf(
    aiTool(
        name = "frp_status",
        description = """
            查看樱花内网穿透的当前状态：是否登录、账号（用户名 / 用户组 / 隧道额度）、
            隧道列表（ID、名称、类型、本机端口、远程地址、是否封禁）、以及 frpc 运行状态。
            用户问「穿透开没开」「我有哪些隧道」「隧道地址是多少」时用它。
        """,
        parameters = AIToolSchema.noArgs(),
        risk = AIToolRisk.READ,
    ) { _, _ ->
        buildString {
            append("已登录：").append(if (FrpManager.isLoggedIn) "是" else "否").append('\n')
            FrpManager.user.value?.let { user ->
                append("账号：").append(user.name.ifBlank { "未知" })
                if (user.groupName.isNotBlank()) append("（").append(user.groupName).append("）")
                append('\n')
                if (user.tunnelQuota > 0) {
                    append("隧道额度：").append(FrpManager.tunnels.value.size)
                        .append('/').append(user.tunnelQuota).append('\n')
                }
                append("实名认证：").append(if (user.isRealName) "已认证" else "未认证").append('\n')
            }
            append("运行状态：").append(
                when (val state = FrpManager.runState.value) {
                    is FrpRunState.Idle -> "未运行"
                    is FrpRunState.Starting -> "正在启动"
                    is FrpRunState.Running -> "运行中（隧道 #${state.tunnelId}）"
                    is FrpRunState.Stopped -> "已停止（退出码 ${state.exitCode}）"
                    is FrpRunState.Failed -> "启动失败：${state.message}"
                }
            ).append('\n')

            val tunnels = FrpManager.tunnels.value
            if (tunnels.isEmpty()) {
                append("隧道：暂无")
            } else {
                append("隧道：\n")
                tunnels.forEach { tunnel ->
                    append("- #").append(tunnel.id).append(' ').append(tunnel.name.ifBlank { "未命名" })
                    append(" | ").append(tunnel.type.uppercase())
                    append(" | 本机 ").append(tunnel.localPort)
                    append(" | ")
                    append(FrpManager.tunnelAddress(tunnel) ?: "地址未知")
                    if (tunnel.isBanned) append(" | 已封禁")
                    append('\n')
                }
            }
        }
    },

    aiTool(
        name = "frp_create_tunnel",
        description = """
            新建一条樱花内网穿透隧道（类型固定为 TCP，隧道名建议用「Minecraft 联机」这类可读名字）。
            local_port 不填时会自动检测当前 Minecraft「对局域网开放」的端口；
            node 不填时自动选择账号可用节点里第一个。
            创建成功后返回新隧道 ID 与它的公网地址。
        """,
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "name" to AIToolSchema.string("隧道名称，例如 Minecraft 联机"),
                "local_port" to AIToolSchema.integer("本机端口：游戏聊天栏里「对局域网开放」后的端口；不填自动检测"),
                "node" to AIToolSchema.integer("节点 ID；不填自动选择当前账号可用的第一个节点"),
            ),
        ),
        risk = AIToolRisk.WRITE,
    ) { args, _ ->
        if (!FrpManager.isLoggedIn) return@aiTool "还没有登录樱花内网穿透，请先在界面里填入访问密钥"
        val name = args.strOr("name", "Minecraft 联机")
        val port = args.int("local_port")
            ?: FrpManager.detectLanPorts().firstOrNull()
            ?: return@aiTool "没有检测到本机端口：请先在游戏里「对局域网开放」"
        val node = args.int("node")
            ?: FrpManager.nodes.value.entries
                .filter { it.value.allowCreate }
                .sortedBy { it.value.name }
                .firstOrNull()?.key?.toIntOrNull()
            ?: return@aiTool "没有可用节点（可能是账号等级限制或节点全满），请在界面里重试"
        val result = FrpManager.createTunnel(
            name = name,
            type = "tcp",
            node = node,
            localIp = "127.0.0.1",
            localPort = port,
            remote = null
        )
        "已创建隧道 #${result.id}（$name），本机端口 $port" +
                (result.remote?.let { "，远程端口 $it" } ?: "，远程端口由樱花自动分配")
    },

    aiTool(
        name = "frp_start_tunnel",
        description = """
            启动指定隧道（同一时间只允许一条）。tunnel_id 不填时用隧道列表里的第一条。
            启动前会检查本机端口是否有程序监听；端口没监听会直接失败并说明原因。
        """,
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "tunnel_id" to AIToolSchema.integer("隧道 ID；不填用列表第一条"),
            ),
        ),
        risk = AIToolRisk.DANGEROUS,
    ) { args, _ ->
        if (!FrpManager.isLoggedIn) return@aiTool "还没有登录樱花内网穿透"
        val tunnels = FrpManager.tunnels.value
        val id = args.int("tunnel_id") ?: tunnels.firstOrNull()?.id ?: return@aiTool "隧道列表为空"
        val tunnel = tunnels.firstOrNull { it.id == id } ?: return@aiTool "没有 ID 为 $id 的隧道"
        FrpManager.start(GlobalContext, tunnel)
        val address = FrpManager.tunnelAddress(tunnel)
        "已请求启动隧道 #${tunnel.id}（${tunnel.name}）" + (address?.let { "，地址 $it" } ?: "")
    },

    aiTool(
        name = "frp_stop_tunnel",
        description = "停止当前运行的樱花内网穿透隧道（frpc 子进程会被结束）。",
        parameters = AIToolSchema.noArgs(),
        risk = AIToolRisk.WRITE,
    ) { _, _ ->
        FrpManager.stop()
        "已停止樱花内网穿透"
    },

    aiTool(
        name = "multiplayer_status",
        description = """
            查看联机相关开关：陶瓦联机是否启用、自定义节点、樱花穿透是否登录与隧道数量。
            用户问「联机怎么开」「陶瓦开着吗」时先调它，再决定下一步。
        """,
        parameters = AIToolSchema.noArgs(),
        risk = AIToolRisk.READ,
    ) { _, _ ->
        val terracotta = AllSettings.enableTerracotta.state
        val customNodes = AllSettings.enableTerracottaNodes.state
        buildString {
            append("陶瓦联机：").append(if (terracotta) "已启用" else "未启用").append('\n')
            append("自定义节点：").append(if (customNodes) AllSettings.terracottaNodes.state.ifBlank { "已开但未填写" } else "关闭").append('\n')
            append("樱花穿透：").append(if (FrpManager.isLoggedIn) "已登录" else "未登录")
            append("，隧道 ").append(FrpManager.tunnels.value.size).append(" 条")
        }
    },
)
