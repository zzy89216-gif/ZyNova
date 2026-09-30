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

package com.movtery.zalithlauncher.ai.agent

import com.movtery.zalithlauncher.game.version.installed.Version
import kotlinx.serialization.json.JsonObject

/**
 * 工具对系统的影响程度。
 * 权限模式为「操作确认」时，[WRITE] / [DANGEROUS] 需要用户点头。
 */
enum class AIToolRisk {
    /** 只读：查看实例、列模组、读日志、搜资料 */
    READ,

    /** 写操作：安装 / 删除 / 改配置 ------------------------------------ */
    WRITE,

    /** 高风险：启动游戏、删除世界等不可逆或影响较大的操作 */
    DANGEROUS,
}

/**
 * 暴露给模型的工具定义（会序列化进请求的 `tools` 字段）
 */
data class AIToolSpec(
    /** 工具名，模型靠它调用；统一 snake_case */
    val name: String,
    /** 给模型看的说明：**要写清楚什么时候用、参数含义、返回什么** */
    val description: String,
    /** 参数的 JSON Schema（type = object）*/
    val parameters: JsonObject,
)

/**
 * 工具执行上下文
 *
 * 这是工具与「启动器运行时」之间的唯一桥梁：
 * 需要 UI / ViewModel 才能完成的能力（例如启动游戏），通过回调注入，
 * 而不是让工具自己去 new 一堆东西。
 */
class AIToolContext(
    /** 当前选中的实例，作为未显式指定 instance 时的默认值 */
    val currentVersion: Version? = null,
    /**
     * 请求用户确认（仅「操作确认」模式会调用）。
     * 默认实现一律返回 false —— 上层没接入确认 UI 时**安全地拒绝写操作**。
     */
    val confirm: suspend (toolName: String, detail: String) -> Boolean = { _, _ -> false },
    /**
     * 真正启动游戏。由 ViewModel 注入（走启动器现有的启动事件链路）。
     * 未注入时，`launch_game` 工具会明确告知模型「当前无法启动」而不是假装成功。
     */
    val launchGame: (suspend (Version) -> Boolean)? = null,
)

/**
 * 一个可被 AI Agent 调用的工具。
 *
 * 实现要求：
 * - [execute] 必须是**幂等安全**的：出现异常要抛出可读错误，由上层回灌给模型
 * - 返回值必须是**人类与模型都能读**的简短文本（必要时用 JSON 字符串）
 * - **优先复用 ZyNova 现有能力**，不要在本层重复实现下载 / 安装 / 解析逻辑
 */
interface AITool {
    val spec: AIToolSpec

    /** 默认只读 */
    val risk: AIToolRisk get() = AIToolRisk.READ

    /**
     * 执行工具
     * @param args 模型给出的参数（已在 [AIToolRegistry] 校验过 JSON 合法性）
     * @return 回灌给模型的文本结果
     */
    suspend fun execute(args: JsonObject, context: AIToolContext): String
}
