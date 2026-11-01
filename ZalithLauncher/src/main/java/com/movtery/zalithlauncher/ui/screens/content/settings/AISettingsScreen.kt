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

package com.movtery.zalithlauncher.ui.screens.content.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.movtery.zalithlauncher.ai.ui.AIConfigContent
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey

/**
 * 设置中心 → AI 设置（议题 #37 / #41）
 *
 * 把原来独立的「AI 配置页」搬进设置中心：
 * - 聊天界面右上角那个齿轮入口已经去掉，不再有第二个配置入口；
 * - **首次配置的引导入口保留**：还没填 API Key 时，聊天页仍会提示去哪里配置；
 * - 内容体复用 [AIConfigContent]，MMKV 键名一个都没改，老用户的 Key 不会丢。
 */
@Composable
fun AISettingsScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?,
) {
    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.AI, settingsScreenKey, false)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AIConfigContent(modifier = Modifier.fillMaxSize())
        }
    }
}
