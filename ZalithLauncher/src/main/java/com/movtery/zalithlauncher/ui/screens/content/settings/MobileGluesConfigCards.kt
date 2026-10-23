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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.renderer.mobileglues.MobileGluesGlVersion
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.ListSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SwitchSettingsCard

/**
 * MobileGlues 的配置面板
 *
 * 只在渲染器列表里选中 MobileGlues 时展示，位于渲染器选择卡片的下方 ——
 * 与 Ironized Zink 选中后展开预设面板是同一个位置、同一个交互。
 *
 * 面板提供两个选项：
 * 1. **OpenGL 版本**下拉；
 * 2. **ANGLE 渲染后端**开关（默认关闭）。
 *
 * ⚠️ 这里没有用环境变量，而是走 MobileGlues 自己的 `config.json`
 * （原因见 `game/renderer/mobileglues/MobileGluesConfig.kt` 的类注释）：
 * MobileGlues 根本不读 `MESA_GL_VERSION_OVERRIDE`，它只认自己的 `customGLVersion`。
 */
@Composable
fun MobileGluesConfigCards(modifier: Modifier = Modifier) {
    SettingsCardColumn(modifier = modifier.fillMaxWidth()) {
        ListSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Top,
            unit = AllSettings.mobileGluesGlVersion,
            items = MobileGluesGlVersion.entries,
            title = stringResource(R.string.settings_renderer_mobileglues_gl_version_title),
            summary = stringResource(R.string.settings_renderer_mobileglues_gl_version_summary),
            getItemText = { it.displayName },
            getItemId = { it.name }
        )

        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Bottom,
            unit = AllSettings.mobileGluesUseAngle,
            title = stringResource(R.string.settings_renderer_mobileglues_angle_title),
            summary = stringResource(R.string.settings_renderer_mobileglues_angle_summary)
        )
    }
}
