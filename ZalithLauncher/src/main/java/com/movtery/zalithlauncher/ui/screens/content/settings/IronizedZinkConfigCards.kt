/*
 * ZyNova Launcher
 * Copyright (C) 2025 ZyNova Contributors
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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.renderer.ironizedzink.Zink
import com.movtery.zalithlauncher.game.renderer.ironizedzink.ZinkPreset
import com.movtery.zalithlauncher.game.renderer.ironizedzink.applyIronizedZinkPreset
import com.movtery.zalithlauncher.game.renderer.ironizedzink.ironizedZinkOptions
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.ListSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SwitchSettingsCard

/**
 * Ironized Zink 的完整配置面板
 *
 * 只在渲染器列表里选中 Ironized Zink 时展示，位于渲染器选择卡片的下方。
 *
 * 内容与上游 Ironized Zink（作者 GoyDevv，GPL-3.0）的设置界面一一对应：
 * 4 个官方预设 + 全部可调参数，没有任何参数被省略。
 */
@Composable
fun IronizedZinkConfigCards(modifier: Modifier = Modifier) {
    val options = AllSettings.ironizedZinkOptions

    SettingsCardColumn(modifier = modifier.fillMaxWidth()) {
        ListSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Top,
            unit = AllSettings.ironizedZinkPreset,
            items = ZinkPreset.entries,
            title = stringResource(R.string.settings_renderer_ironized_preset_title),
            summary = stringResource(
                if (AllSettings.ironizedZinkPreset.state.options == options) {
                    R.string.settings_renderer_ironized_preset_summary
                } else {
                    R.string.settings_renderer_ironized_preset_custom
                }
            ),
            getItemText = { it.displayName },
            getItemSummary = { preset -> IronizedPresetSummary(preset) },
            onValueChange = { preset ->
                //选择预设 = 一次性写入整组参数；写入后每个参数仍可单独调整
                AllSettings.applyIronizedZinkPreset(preset)
            }
        )

        ListSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            unit = AllSettings.ironizedZinkGlVersion,
            items = Zink.GL_VERSIONS,
            title = stringResource(R.string.settings_renderer_ironized_gl_version_title),
            summary = stringResource(R.string.settings_renderer_ironized_gl_version_summary),
            getItemText = { it },
            getItemId = { it }
        )

        //性能
        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            unit = AllSettings.ironizedZinkThreadedGl,
            title = stringResource(R.string.settings_renderer_ironized_threaded_gl_title),
            summary = stringResource(R.string.settings_renderer_ironized_threaded_gl_summary)
        )

        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            unit = AllSettings.ironizedZinkBigCoreAffinity,
            title = stringResource(R.string.settings_renderer_ironized_big_core_title),
            summary = stringResource(R.string.settings_renderer_ironized_big_core_summary)
        )

        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            unit = AllSettings.ironizedZinkOutOfOrder,
            title = stringResource(R.string.settings_renderer_ironized_out_of_order_title),
            summary = stringResource(R.string.settings_renderer_ironized_out_of_order_summary)
        )

        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            unit = AllSettings.ironizedZinkVsync,
            title = stringResource(R.string.settings_renderer_ironized_vsync_title),
            summary = stringResource(R.string.settings_renderer_ironized_vsync_summary)
        )

        //着色器与兼容性
        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            unit = AllSettings.ironizedZinkRelaxGlsl,
            title = stringResource(R.string.settings_renderer_ironized_relax_glsl_title),
            summary = stringResource(R.string.settings_renderer_ironized_relax_glsl_summary)
        )

        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            unit = AllSettings.ironizedZinkAllExtensions,
            title = stringResource(R.string.settings_renderer_ironized_all_extensions_title),
            summary = stringResource(R.string.settings_renderer_ironized_all_extensions_summary)
        )

        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            unit = AllSettings.ironizedZinkShaderCache,
            title = stringResource(R.string.settings_renderer_ironized_shader_cache_title),
            summary = stringResource(R.string.settings_renderer_ironized_shader_cache_summary)
        )

        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            unit = AllSettings.ironizedZinkSingleFileCache,
            title = stringResource(R.string.settings_renderer_ironized_single_file_cache_title),
            summary = stringResource(R.string.settings_renderer_ironized_single_file_cache_summary)
        )

        //实验性
        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            unit = AllSettings.ironizedZinkLazyDescriptors,
            title = stringResource(R.string.settings_renderer_ironized_lazy_descriptors_title),
            summary = stringResource(R.string.settings_renderer_ironized_lazy_descriptors_summary)
        )

        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            unit = AllSettings.ironizedZinkInlineUniforms,
            title = stringResource(R.string.settings_renderer_ironized_inline_uniforms_title),
            summary = stringResource(R.string.settings_renderer_ironized_inline_uniforms_summary)
        )

        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            unit = AllSettings.ironizedZinkNoError,
            title = stringResource(R.string.settings_renderer_ironized_no_error_title),
            summary = stringResource(R.string.settings_renderer_ironized_no_error_summary)
        )

        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Bottom,
            unit = AllSettings.ironizedZinkForceSoftware,
            title = stringResource(R.string.settings_renderer_ironized_force_software_title),
            summary = stringResource(R.string.settings_renderer_ironized_force_software_summary)
        )

        if (options.hasUnsafeCombo) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(0.9f),
                text = stringResource(R.string.settings_renderer_ironized_unsafe_combo),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

/** 预设条目的说明：官方标语 + 适用 Minecraft 版本与光影建议 */
@Composable
private fun IronizedPresetSummary(preset: ZinkPreset) {
    val taglineRes: Int
    val noteRes: Int
    when (preset) {
        ZinkPreset.POTATO -> {
            taglineRes = R.string.settings_renderer_ironized_preset_potato_tagline
            noteRes = R.string.settings_renderer_ironized_preset_potato_note
        }
        ZinkPreset.PERFORMANCE -> {
            taglineRes = R.string.settings_renderer_ironized_preset_performance_tagline
            noteRes = R.string.settings_renderer_ironized_preset_performance_note
        }
        ZinkPreset.DEFAULT -> {
            taglineRes = R.string.settings_renderer_ironized_preset_default_tagline
            noteRes = R.string.settings_renderer_ironized_preset_default_note
        }
        ZinkPreset.MAX_COMPAT -> {
            taglineRes = R.string.settings_renderer_ironized_preset_max_compat_tagline
            noteRes = R.string.settings_renderer_ironized_preset_max_compat_note
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = stringResource(taglineRes),
            style = MaterialTheme.typography.labelSmall
        )
        Text(
            modifier = Modifier.alpha(0.7f),
            text = stringResource(noteRes),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
