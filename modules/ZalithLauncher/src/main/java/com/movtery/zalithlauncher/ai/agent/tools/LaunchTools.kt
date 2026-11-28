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

/**
 * 游戏启动工具。
 *
 * ⚠️ 真正的启动动作**不在这里实现**：
 * 启动走的是 ZyNova 现有的启动事件链路（`EventViewModel.Event.Launch.Game`），
 * 由 ViewModel 通过 [com.movtery.zalithlauncher.ai.agent.AIToolContext.launchGame] 注入。
 * 这样 Agent 启动游戏与用户点「启动」走的是**完全同一条路径**，
 * 不会出现「AI 用另一套方式启动、行为和正常启动不一致」的问题。
 */
internal fun launchTools(): List<AITool> = listOf(

    aiTool(
        name = "launch_game",
        description = """
            启动一个 Minecraft 实例（走启动器正常的启动流程，与用户点「启动」完全相同）。
            这是一次**真实启动**，会占用设备资源，只在用户明确要求「帮我启动」「进游戏试试」时调用。
            修复类操作完成后，用户常会让你启动验证，这时用它。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "instance" to AIToolSchema.string("要启动的实例名称；不传则启动当前选中的实例"),
            ),
        ),
        risk = AIToolRisk.DANGEROUS,
    ) { args, ctx ->
        val version = AIToolSupport.resolveVersion(args.str("instance"), ctx)

        if (!version.isValid()) {
            return@aiTool "实例「${version.getVersionName()}」当前无效（版本文件缺失或损坏），" +
                    "无法启动。可以先用 get_instance_detail 查看哪里缺失。"
        }

        val launch = ctx.launchGame
            ?: return@aiTool "当前上下文不允许启动游戏（Agent 未接入启动链路）。" +
                    "请告知用户手动点击启动。"

        val ok = launch(version)
        if (ok) {
            "已发出启动请求：${version.getVersionName()}。启动过程需要一些时间，" +
                    "如果随后崩溃，可以用 list_logs + read_crash_report 查看原因。"
        } else {
            "启动请求未能发出（可能已经在启动中，或启动器当前状态不允许）。"
        }
    },
)
