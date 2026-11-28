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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SwitchSettingsCard

/**
 * LTW（实验性）的参数面板。
 *
 * 只暴露 LTW native 真正读取的环境变量；没有把上游独立 App 的主题/强调色
 * 这类「应用外观」硬塞进启动器设置里 —— 那和渲染行为无关。
 */
@Composable
fun LTWConfigCards(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        BackgroundCard(
            modifier = Modifier.fillMaxWidth(),
            influencedByBackground = false,
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.settings_renderer_ltw_warning_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    text = stringResource(R.string.settings_renderer_ltw_warning_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        SettingsCardColumn(modifier = Modifier.fillMaxWidth()) {
            SwitchSettingsCard(
                modifier = Modifier.fillMaxWidth(),
                position = CardPosition.Top,
                unit = AllSettings.ltwNeverFlushBuffers,
                title = stringResource(R.string.settings_ltw_never_flush_title),
                summary = stringResource(R.string.settings_ltw_never_flush_summary)
            )
            SwitchSettingsCard(
                modifier = Modifier.fillMaxWidth(),
                position = CardPosition.Middle,
                unit = AllSettings.ltwCoherentDynamicStorage,
                title = stringResource(R.string.settings_ltw_coherent_title),
                summary = stringResource(R.string.settings_ltw_coherent_summary)
            )
            SwitchSettingsCard(
                modifier = Modifier.fillMaxWidth(),
                position = CardPosition.Middle,
                unit = AllSettings.ltwHideBufferStorage,
                title = stringResource(R.string.settings_ltw_hide_buffer_title),
                summary = stringResource(R.string.settings_ltw_hide_buffer_summary)
            )
            SwitchSettingsCard(
                modifier = Modifier.fillMaxWidth(),
                position = CardPosition.Middle,
                unit = AllSettings.ltwNoError,
                title = stringResource(R.string.settings_ltw_no_error_title),
                summary = stringResource(R.string.settings_ltw_no_error_summary)
            )
            SwitchSettingsCard(
                modifier = Modifier.fillMaxWidth(),
                position = CardPosition.Middle,
                unit = AllSettings.ltwShaderConvCheckNan,
                title = stringResource(R.string.settings_ltw_check_nan_title),
                summary = stringResource(R.string.settings_ltw_check_nan_summary)
            )
            SwitchSettingsCard(
                modifier = Modifier.fillMaxWidth(),
                position = CardPosition.Middle,
                unit = AllSettings.ltwDebug,
                title = stringResource(R.string.settings_ltw_debug_title),
                summary = stringResource(R.string.settings_ltw_debug_summary)
            )
            SwitchSettingsCard(
                modifier = Modifier.fillMaxWidth(),
                position = CardPosition.Bottom,
                unit = AllSettings.ltwGlErrorTrace,
                title = stringResource(R.string.settings_ltw_glerror_title),
                summary = stringResource(R.string.settings_ltw_glerror_summary)
            )
        }
    }
}
