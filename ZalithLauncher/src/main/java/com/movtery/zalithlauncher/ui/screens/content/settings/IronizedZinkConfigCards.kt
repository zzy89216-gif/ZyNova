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
import com.movtery.zalithlauncher.game.renderer.ironizedzink.ZinkPreset
import com.movtery.zalithlauncher.game.renderer.ironizedzink.applyIronizedZinkPreset
import com.movtery.zalithlauncher.game.renderer.ironizedzink.ironizedZinkOptions
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.ListSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn

/**
 * Ironized Zink 的配置面板
 *
 * 只在渲染器列表里选中 Ironized Zink 时展示，位于渲染器选择卡片的下方。
 *
 * 面板**只提供上游 Ironized Zink（作者 GoyDevv，GPL-3.0）的 4 个官方预设**：
 * Potato / Performance / Default / Max Compatibility。
 * 选择预设 = 一次性写入整组底层参数，**不再暴露任何单独参数开关**，
 * 避免普通用户需要手动调整底层参数。
 *
 * 参数模型与读写链路仍完整保留在 `IronizedZinkConfig.kt` / `IronizedZinkSettings.kt`：
 * `applyIronizedZinkPreset()` 写入的键与旧版本完全一致，
 * 因此老用户已保存的取值不受影响，环境变量注入行为也不变。
 */
@Composable
fun IronizedZinkConfigCards(modifier: Modifier = Modifier) {
    val options = AllSettings.ironizedZinkOptions

    SettingsCardColumn(modifier = modifier.fillMaxWidth()) {
        ListSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Single,
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
                //选择预设 = 一次性写入整组参数（面板不再提供单独参数调整）
                AllSettings.applyIronizedZinkPreset(preset)
            }
        )

        //仅当老用户残留了已知有问题的参数组合时提示；重新选择任意预设即可恢复
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
