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

package com.movtery.zalithlauncher.setting

import com.movtery.zalithlauncher.BuildKeys

/**
 * 标题栏左上角实际显示的启动器名称（议题 #18）。
 *
 * 规则：用户在「设置 → 启动器 → 启动器名称」里填了就用它，
 * **留空 / 只填空白**则回退到默认的 [BuildKeys.LAUNCHER_IDENTIFIER]
 * （即 `gradle.properties` 的 `launcher_name`）。
 *
 * ⚠️ 界面上一律用这个值，**不要**直接读 `AllSettings.launcherTitle.state` ——
 * 那会让「留空」在标题栏上显示成一片空白。
 *
 * ⚠️ 这里改的只是**界面上显示的名字**，不会改动：
 * - 应用名（`launcher_app_name` / 桌面图标下的名称）
 * - 参与认证与游戏启动的 `BuildKeys.LAUNCHER_NAME` 与 `LAUNCHER_IDENTIFIER`
 *   （它们会写进 `clientToken`、`-Dminecraft.launcher.brand`、`launcher_name` 等）
 * 因此这个开关**不会**影响正版登录、多人游戏识别或游戏内显示的品牌名。
 */
val AllSettings.launcherDisplayTitle: String
    get() = launcherTitle.state.trim().takeIf { it.isNotEmpty() }
        ?: BuildKeys.LAUNCHER_IDENTIFIER
