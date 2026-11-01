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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.unit.floatRange
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedColumn
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.IntSliderSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SwitchSettingsCard

/**
 * 渲染器详细参数（三个独立页面）
 *
 * 反馈：参数不要一股脑堆在「高级设置」里，也不要三个渲染器挤在同一页。
 * 所以拆成三页，每页只放这个渲染器自己的东西；
 * 版式沿用设置中心的原生骨架（BaseScreen + AnimatedColumn + SettingsCardColumn），
 * 不另造一套视觉。
 *
 * ⚠️ 所有参数都是真实的（GL4ES 的环境变量、MobileGlues 的 config.json、
 * Ironized Zink 的 MMKV 键），默认值等于历史写死值，升级后行为不变。
 */

/** MobileGlues 详细参数 */
@Composable
fun MobileGluesAdvancedSettingsScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?,
) {
    RendererAdvancedPage(
        normalKey = NormalNavKey.Settings.RendererAdvancedMobileGlues,
        settingsKey = key,
        settingsScreenKey = settingsScreenKey,
        mainScreenKey = mainScreenKey
    ) {
        MobileGluesConfigCards(modifier = Modifier.fillMaxWidth())

        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Top,
            unit = AllSettings.mobileGluesNoError,
            title = stringResource(R.string.settings_mg_no_error_title),
            summary = stringResource(R.string.settings_mg_no_error_summary)
        )
        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            unit = AllSettings.mobileGluesExtTimerQuery,
            title = stringResource(R.string.settings_mg_timer_query_title),
            summary = stringResource(R.string.settings_mg_timer_query_summary)
        )
        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            unit = AllSettings.mobileGluesExtComputeShader,
            title = stringResource(R.string.settings_mg_compute_shader_title),
            summary = stringResource(R.string.settings_mg_compute_shader_summary)
        )
        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            unit = AllSettings.mobileGluesExtDirectStateAccess,
            title = stringResource(R.string.settings_mg_dsa_title),
            summary = stringResource(R.string.settings_mg_dsa_summary)
        )
        IntSliderSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            unit = AllSettings.mobileGluesMaxGlslCacheSize,
            title = stringResource(R.string.settings_mg_glsl_cache_title),
            summary = stringResource(R.string.settings_mg_glsl_cache_summary),
            valueRange = AllSettings.mobileGluesMaxGlslCacheSize.floatRange,
            suffix = "MiB"
        )
        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            unit = AllSettings.mobileGluesAngleDepthClearFix,
            title = stringResource(R.string.settings_mg_angle_depth_title),
            summary = stringResource(R.string.settings_mg_angle_depth_summary)
        )
        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Bottom,
            unit = AllSettings.mobileGluesFsr1,
            title = stringResource(R.string.settings_mg_fsr1_title),
            summary = stringResource(R.string.settings_mg_fsr1_summary)
        )
    }
}

/** Ironized Zink 详细参数 */
@Composable
fun IronizedZinkAdvancedSettingsScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?,
) {
    RendererAdvancedPage(
        normalKey = NormalNavKey.Settings.RendererAdvancedZink,
        settingsKey = key,
        settingsScreenKey = settingsScreenKey,
        mainScreenKey = mainScreenKey
    ) {
        IronizedZinkConfigCards(modifier = Modifier.fillMaxWidth())
    }
}

/** GL4ES 详细参数 */
@Composable
fun Gl4esAdvancedSettingsScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?,
) {
    RendererAdvancedPage(
        normalKey = NormalNavKey.Settings.RendererAdvancedGl4es,
        settingsKey = key,
        settingsScreenKey = settingsScreenKey,
        mainScreenKey = mainScreenKey
    ) {
        IntSliderSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Top,
            unit = AllSettings.gl4esMipmap,
            title = stringResource(R.string.settings_gl4es_mipmap_title),
            summary = stringResource(R.string.settings_gl4es_mipmap_summary),
            valueRange = AllSettings.gl4esMipmap.floatRange
        )
        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            unit = AllSettings.gl4esNoError,
            title = stringResource(R.string.settings_gl4es_no_error_title),
            summary = stringResource(R.string.settings_gl4es_no_error_summary)
        )
        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Middle,
            unit = AllSettings.gl4esNoIntOverflowHack,
            title = stringResource(R.string.settings_gl4es_no_overflow_title),
            summary = stringResource(R.string.settings_gl4es_no_overflow_summary)
        )
        SwitchSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Bottom,
            unit = AllSettings.gl4esNormalize,
            title = stringResource(R.string.settings_gl4es_normalize_title),
            summary = stringResource(R.string.settings_gl4es_normalize_summary)
        )
    }
}

/**
 * 三个页面共用的骨架：与其它设置页完全一致（BaseScreen → AnimatedColumn → 卡片列）。
 * 不单独发明布局，避免点进来像换了个应用。
 */
@Composable
private fun RendererAdvancedPage(
    normalKey: NormalNavKey.Settings,
    settingsKey: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?,
    content: @Composable () -> Unit,
) {
    BaseScreen(
        Triple(settingsKey, mainScreenKey, false),
        Triple(normalKey, settingsScreenKey, false)
    ) { isVisible ->
        AnimatedColumn(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScrollWithBar(state = rememberScrollState())
                .padding(all = 12.dp),
            isVisible = isVisible
        ) { scope ->
            AnimatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    content()
                }
            }
        }
    }
}
