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
import com.movtery.zalithlauncher.game.renderer.ironizedzink.IronizedZinkOptions
import com.movtery.zalithlauncher.game.renderer.ironizedzink.ironizedZinkOptions
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SwitchSettingsCard
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.ListSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn

/**
 * Ironized Zink 的配置面板
 *
 * 只在渲染器列表里选中 Ironized Zink 时展示，位于渲染器选择卡片的下方。
 *
 * 面板提供三块内容：
 * 1. **4 个官方预设**（Potato / Performance / Default / Max Compatibility）——
 *    选择预设 = 一次性写入性能/兼容性开关；
 * 2. **OpenGL 版本**（3.3 – 4.6）—— 与预设**互相独立**的单独设置（议题 #13）；
 * 3. **高级参数**折叠区（默认收起）—— 12 个底层开关，懂行的用户可逐项调。
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
            //#13：预设命中判定**忽略 OpenGL 版本** —— 预设只写其它 12 个参数，
            //用户把 GL 版本降到 3.3 后仍然显示命中同一个预设，而不是「已自定义」。
            summary = stringResource(
                if (IronizedZinkOptions.matchingPresetIgnoringGlVersion(options) ==
                    AllSettings.ironizedZinkPreset.state
                ) {
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

        // #13：OpenGL 版本下拉 —— 与预设**完全解耦**的独立设置。
        //
        // 4 个官方预设的 glVersion 都是 4.6。某些 GPU 驱动对 Vulkan→Zink 的 4.6 转译并不完整，
        // 此时用户需要一个把 OpenGL 版本降到 4.5 / 4.3 / 3.3 的「逃生口」。
        //
        // 旧实现的 bug：选预设会把整组参数（含 glVersion = 4.6）写回设置，
        // 用户刚降下去的 GL 版本立刻被重置回 4.6 —— 老设备永远降不下来。
        // 现在：
        // - `applyIronizedZinkPreset()` **不写** ironizedZinkGlVersion；
        // - 本下拉直接写 ironizedZinkGlVersion（`MESA_GL_VERSION_OVERRIDE` 取这里的值）；
        // - 预设命中判定忽略 GL 版本，避免「降了版本就显示已自定义」。
        ListSettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Single,
            unit = AllSettings.ironizedZinkGlVersion,
            items = Zink.GL_VERSIONS,
            title = stringResource(R.string.settings_renderer_ironized_gl_version_title),
            summary = stringResource(R.string.settings_renderer_ironized_gl_version_summary),
            getItemText = { it },
            getItemId = { it }
        )

        //把「预设 ↔ GL 版本」的关系写在界面上，避免用户以为切换预设会把版本重置
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(0.7f),
            text = stringResource(
                R.string.settings_renderer_ironized_gl_version_independent,
                AllSettings.ironizedZinkPreset.state.displayName,
                AllSettings.ironizedZinkGlVersion.state
            ),
            style = MaterialTheme.typography.labelSmall
        )

        // ── 高级参数（议题 #40：全部迁移进来，但默认收起）──────────
        //
        //26.4.0 曾经把这 12 个开关从面板上撤掉、只留预设；#40 要求
        //「渲染器现有全部配置项都要能改」，所以现在以「高级参数」折叠区放回来：
        //默认不打扰普通用户，懂行的人展开就能逐项调。
        //⚠️ 键名必须保持 ironizedZink*，预设写入链路（applyIronizedZinkPreset）也不要动。
        var advancedExpanded by remember { mutableStateOf(false) }

        SettingsCard(
            modifier = Modifier.fillMaxWidth(),
            position = CardPosition.Single,
            title = stringResource(R.string.settings_renderer_advanced_title),
            summary = stringResource(R.string.settings_renderer_advanced_summary),
            onClick = { advancedExpanded = !advancedExpanded },
            trailingIcon = {
                Text(
                    text = stringResource(
                        if (advancedExpanded) R.string.settings_renderer_advanced_hide
                        else R.string.settings_renderer_advanced_show
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        )

        AnimatedVisibility(visible = advancedExpanded) {
            SettingsCardColumn(modifier = Modifier.fillMaxWidth()) {
                ZinkAdvancedSwitch(AllSettings.ironizedZinkThreadedGl, R.string.settings_zink_threaded_gl_title, R.string.settings_zink_threaded_gl_summary, CardPosition.Top)
                ZinkAdvancedSwitch(AllSettings.ironizedZinkBigCoreAffinity, R.string.settings_zink_big_core_title, R.string.settings_zink_big_core_summary, CardPosition.Middle)
                ZinkAdvancedSwitch(AllSettings.ironizedZinkOutOfOrder, R.string.settings_zink_out_of_order_title, R.string.settings_zink_out_of_order_summary, CardPosition.Middle)
                ZinkAdvancedSwitch(AllSettings.ironizedZinkNoError, R.string.settings_zink_no_error_title, R.string.settings_zink_no_error_summary, CardPosition.Middle)
                ZinkAdvancedSwitch(AllSettings.ironizedZinkVsync, R.string.settings_zink_vsync_title, R.string.settings_zink_vsync_summary, CardPosition.Middle)
                ZinkAdvancedSwitch(AllSettings.ironizedZinkRelaxGlsl, R.string.settings_zink_relax_glsl_title, R.string.settings_zink_relax_glsl_summary, CardPosition.Middle)
                ZinkAdvancedSwitch(AllSettings.ironizedZinkAllExtensions, R.string.settings_zink_all_extensions_title, R.string.settings_zink_all_extensions_summary, CardPosition.Middle)
                ZinkAdvancedSwitch(AllSettings.ironizedZinkShaderCache, R.string.settings_zink_shader_cache_title, R.string.settings_zink_shader_cache_summary, CardPosition.Middle)
                ZinkAdvancedSwitch(AllSettings.ironizedZinkSingleFileCache, R.string.settings_zink_single_file_cache_title, R.string.settings_zink_single_file_cache_summary, CardPosition.Middle)
                ZinkAdvancedSwitch(AllSettings.ironizedZinkLazyDescriptors, R.string.settings_zink_lazy_descriptors_title, R.string.settings_zink_lazy_descriptors_summary, CardPosition.Middle)
                ZinkAdvancedSwitch(AllSettings.ironizedZinkInlineUniforms, R.string.settings_zink_inline_uniforms_title, R.string.settings_zink_inline_uniforms_summary, CardPosition.Middle)
                ZinkAdvancedSwitch(AllSettings.ironizedZinkForceSoftware, R.string.settings_zink_force_software_title, R.string.settings_zink_force_software_summary, CardPosition.Bottom)
            }
        }

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

/** 高级参数里的单个开关：标题与说明都用大白话，避免「mesa_glthread」这类术语直接怼到用户脸上 */
@Composable
private fun ZinkAdvancedSwitch(
    unit: com.movtery.zalithlauncher.setting.unit.BooleanSettingUnit,
    @androidx.annotation.StringRes title: Int,
    @androidx.annotation.StringRes summary: Int,
    position: CardPosition,
) {
    SwitchSettingsCard(
        modifier = Modifier.fillMaxWidth(),
        position = position,
        unit = unit,
        title = stringResource(title),
        summary = stringResource(summary)
    )
}
