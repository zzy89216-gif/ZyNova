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

/**
 * ZyNova 内置的全部 Agent 工具。
 *
 * 设计原则：**把现有能力暴露给 Agent，而不是让 AI 写教程**。
 * 每一个工具背后调用的都是启动器现有的实现：
 *
 * | 工具组 | 复用的现有系统 |
 * |---|---|
 * | 实例管理 | `VersionsManager` / `Version` / `VersionFolders` |
 * | 内容管理 | `AllModReader`（真实解析模组元数据） |
 * | 文件管理 | `PathManager`（并限制在允许目录内） |
 * | 日志与崩溃 | `PathManager.DIR_LAUNCHER_LOGS` / 实例 crash-reports |
 * | 设置读写 | `AllSettings` / `SettingsRegistry` |
 * | 启动游戏 | 现有启动事件链路（由 ViewModel 注入） |
 * | 搜索与安装 | `ResourceManager` 统一资源核心（含前置依赖递归安装） |
 */
internal fun builtinTools(): List<AITool> = buildList {
    addAll(instanceTools())
    addAll(contentTools())
    addAll(fileTools())
    addAll(logTools())
    addAll(settingTools())
    addAll(launchTools())
    addAll(resourceTools())
}
