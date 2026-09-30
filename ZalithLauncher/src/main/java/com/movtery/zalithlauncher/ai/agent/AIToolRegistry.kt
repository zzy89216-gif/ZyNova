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

/**
 * Agent 工具注册表。
 *
 * 全局单例：新增工具只需要在 [registerBuiltinTools] 里加一行。
 */
object AIToolRegistry {

    private val tools = linkedMapOf<String, AITool>()
    private var initialized = false

    /** 幂等注册内置工具（第一次取用时自动执行）*/
    @Synchronized
    fun ensureInitialized() {
        if (initialized) return
        initialized = true
        registerBuiltinTools()
    }

    @Synchronized
    fun register(tool: AITool) {
        tools[tool.spec.name] = tool
    }

    fun get(name: String): AITool? {
        ensureInitialized()
        return tools[name]
    }

    fun all(): List<AITool> {
        ensureInitialized()
        return tools.values.toList()
    }

    /** 提供给模型的工具定义 */
    fun specs(): List<AIToolSpec> = all().map { it.spec }

    /** 供界面展示的工具清单（名称 + 风险等级）*/
    fun summary(): List<Triple<String, String, AIToolRisk>> =
        all().map { Triple(it.spec.name, it.spec.description, it.risk) }
}

/**
 * 注册 ZyNova 内置的全部 Agent 工具。
 *
 * ⚠️ 这些工具**必须复用现有系统**（实例管理 / 模组读取 / 路径 / 设置 / 启动），
 * 不要在这里重新实现下载、解析、安装等已有能力。
 */
private fun registerBuiltinTools() {
    com.movtery.zalithlauncher.ai.agent.tools.builtinTools().forEach {
        AIToolRegistry.register(it)
    }
}
