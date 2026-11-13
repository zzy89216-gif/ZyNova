/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.plugin.driver.Driver
import com.movtery.zalithlauncher.game.plugin.driver.DriverPluginManager
import com.movtery.zalithlauncher.game.plugin.renderer_v2.RendererV2Data
import com.movtery.zalithlauncher.game.renderer.AutoRenderer
import com.movtery.zalithlauncher.game.renderer.RendererInterface
import com.movtery.zalithlauncher.game.renderer.RendererVersions
import com.movtery.zalithlauncher.game.renderer.Renderers
import com.movtery.zalithlauncher.game.renderer.mobileglues.MOBILE_GLUES_UNIQUE_IDENTIFIER
import com.movtery.zalithlauncher.game.renderer.renderers.LTWRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.GL4ESRenderer
import com.movtery.zalithlauncher.path.URL_CLOUD_DRIVE_DRIVER_PLUGINS
import com.movtery.zalithlauncher.path.URL_CLOUD_RENDERER_PLUGINS
import com.movtery.zalithlauncher.path.URL_GITHUB_DRIVER_PLUGINS
import com.movtery.zalithlauncher.path.URL_GITHUB_RENDERER_PLUGINS
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
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.ListSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SwitchSettingsCard
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.sendDLPlugin

/**
 * 设置中心 → 渲染器设置（议题 #40 / #41）
 *
 * 结构固定为三层：
 * 1. **通用**：选渲染器 + 版本与状态（选谁就显示谁的版本、支持哪些游戏版本）；
 * 2. **当前渲染器的专属配置**：按选中项动态显示，不混在一起、也不互相重置
 *    （所有配置都是全局扁平 MMKV 键，切换渲染器不会写别的渲染器的键）；
 * 3. **显示与高级**：分辨率、全屏、SurfaceView、持续性能、驱动与调试项。
 *
 * ⚠️ 三个渲染器的 UUID 是用户配置里的持久值，**不要改**；
 * 「自动」只是设置界面的选项，不能混进 `Renderers.getRenderers()`。
 */
@Composable
fun RendererSettingsScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?,
    eventViewModel: EventViewModel,
    toSettingsScreen: (TitledNavKey) -> Unit,
) {
    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.Renderer, settingsScreenKey, false)
    ) { isVisible ->
        AnimatedColumn(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScrollWithBar(state = rememberScrollState())
                .padding(all = 12.dp),
            isVisible = isVisible
        ) { scope ->
            val currentRendererId = AllSettings.renderer.state
            val isAuto = currentRendererId == AutoRenderer.IDENTIFIER
            val isMobileGlues = currentRendererId == MOBILE_GLUES_UNIQUE_IDENTIFIER
            val isGL4ES = currentRendererId == GL4ESRenderer.getUniqueIdentifier()
            val isLTW = currentRendererId == LTWRenderer.getUniqueIdentifier()

            val currentRenderer = remember(currentRendererId) {
                Renderers.getRenderersForSelection()
                    .firstOrNull { it.getUniqueIdentifier() == currentRendererId }
            }
            val isPluginRenderer = currentRenderer is RendererV2Data
            //走 Vulkan 的才需要驱动：只剩插件渲染器（内置渲染器都不再走 Vulkan）
            val usesVulkan = isPluginRenderer

            AnimatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    val v2PluginEnvUnits = remember(currentRendererId) {
                        Renderers.getRenderers()
                            .filterIsInstance<RendererV2Data>()
                            .find { it.getUniqueIdentifier() == currentRendererId }
                            ?.env?.getConfigurableUnits()?.takeIf { it.isNotEmpty() }
                    }
                    var showV2ConfigDialog by remember { mutableStateOf(false) }

                    ListSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        unit = AllSettings.renderer,
                        //「自动」在最前，其余内置渲染器依次在后（见 Renderers.getRenderersForSelection）
                        items = Renderers.getRenderersForSelection(),
                        title = stringResource(R.string.settings_renderer_global_renderer_title),
                        summary = stringResource(R.string.settings_renderer_global_renderer_summary),
                        getItemText = { it.getRendererName() },
                        getItemId = { it.getUniqueIdentifier() },
                        getItemSummary = {
                            RendererSummaryLayout(it)
                        },
                        trailingIcon = {
                            //选中新一代渲染器插件且存在可配置项时，提供配置入口
                            if (v2PluginEnvUnits != null) {
                                IconButton(
                                    onClick = { showV2ConfigDialog = true }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_settings_filled),
                                        contentDescription = stringResource(R.string.settings_renderer_config_title)
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    eventViewModel.sendDLPlugin(
                                        githubLink = URL_GITHUB_RENDERER_PLUGINS,
                                        cloudDrives = listOf(
                                            EventViewModel.Event.DownloadPlugins.CloudDrive(
                                                language = "zh",
                                                link = URL_CLOUD_RENDERER_PLUGINS
                                            )
                                        )
                                    )
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_download_2_filled),
                                    contentDescription = stringResource(R.string.generic_download)
                                )
                            }
                        }
                    )

                    if (showV2ConfigDialog && v2PluginEnvUnits != null) {
                        RendererV2ConfigDialog(
                            units = v2PluginEnvUnits,
                            onDismissRequest = { showV2ConfigDialog = false }
                        )
                    }

                    RendererStatusCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        renderer = currentRenderer,
                        identifier = currentRendererId,
                        isAuto = isAuto,
                        //反馈：选「自动」时点它没意义；选了具体渲染器就跳到它自己的详细参数页
                        onClick = when {
                            isMobileGlues -> {
                                { toSettingsScreen(NormalNavKey.Settings.RendererAdvancedMobileGlues) }
                            }
                            isGL4ES -> {
                                { toSettingsScreen(NormalNavKey.Settings.RendererAdvancedGl4es) }
                            }
                            isLTW -> {
                                { toSettingsScreen(NormalNavKey.Settings.RendererAdvancedLtw) }
                            }
                            else -> null
                        },
                    )
                }
            }

            // ── 当前渲染器的专属配置（按选中动态显示）────────────────
            if (isMobileGlues) {
                AnimatedItem(scope) { yOffset ->
                    MobileGluesConfigCards(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                    )
                }
            }

            //⚠️ LTW 的参数**不在这里展开**：
            //上面的渲染器状态卡片点一下就会跳到「LTW 详细设置」独立页面，
            //把同一堆开关在下面再铺一遍只会让这一页又长又重复（维护者明确要求去掉）。
            //MobileGlues 保持原样——它的常用开关在这页直接调更方便。

            // ── 显示与性能（所有渲染器通用）────────────────────────
            AnimatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        unit = AllSettings.resolutionRatio,
                        title = stringResource(R.string.settings_renderer_resolution_scale_title),
                        summary = stringResource(R.string.settings_renderer_resolution_scale_summary),
                        valueRange = AllSettings.resolutionRatio.floatRange,
                        suffix = "%",
                        fineTuningControl = true
                    )

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.gameFullScreen,
                        title = stringResource(R.string.settings_renderer_full_screen_title),
                        summary = stringResource(R.string.settings_renderer_full_screen_summary)
                    )

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.useSurfaceView,
                        title = stringResource(R.string.settings_renderer_surface_title),
                        summary = stringResource(R.string.settings_renderer_surface_summary)
                    )

                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.sustainedPerformance,
                        title = stringResource(R.string.settings_renderer_sustained_performance_title),
                        summary = stringResource(R.string.settings_renderer_sustained_performance_summary)
                    )

                    //驱动只对走 Vulkan 的渲染器有意义：GL4ES / MobileGlues 下不显示，避免给出无效选项
                    if (usesVulkan) {
                        ListSettingsCard(
                            modifier = Modifier.fillMaxWidth(),
                            position = CardPosition.Middle,
                            unit = AllSettings.vulkanDriver,
                            items = DriverPluginManager.getDriverList(),
                            title = stringResource(R.string.settings_renderer_global_vulkan_driver_title),
                            getItemText = { it.name },
                            getItemId = { it.id },
                            getItemSummary = {
                                DriverSummaryLayout(it)
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        eventViewModel.sendDLPlugin(
                                            githubLink = URL_GITHUB_DRIVER_PLUGINS,
                                            cloudDrives = listOf(
                                                EventViewModel.Event.DownloadPlugins.CloudDrive(
                                                    language = "zh",
                                                    link = URL_CLOUD_DRIVE_DRIVER_PLUGINS
                                                )
                                            )
                                        )
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_download_2_filled),
                                        contentDescription = stringResource(R.string.generic_download)
                                    )
                                }
                            }
                        )
                    }

                    //调试用：默认收在最后，平时不用管
                    SwitchSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        unit = AllSettings.dumpShaders,
                        title = stringResource(R.string.settings_renderer_shader_dump_title),
                        summary = stringResource(R.string.settings_renderer_shader_dump_summary)
                    )
                }
            }
        }
    }
}

/**
 * 渲染器状态卡：名称 + 实际内置版本 + 支持的游戏版本范围。
 *
 * 议题 #36 要求「不能只改界面版本号，必须能对得上实际核心文件」——
 * 版本号现在从 [RendererVersions] 这个唯一事实来源读取，
 * 与 `THIRD_PARTY_NOTICES.md` 里记录的官方发布版保持一致。
 */
@Composable
private fun RendererStatusCard(
    modifier: Modifier,
    position: CardPosition,
    renderer: RendererInterface?,
    identifier: String,
    isAuto: Boolean,
    onClick: (() -> Unit)?,
) {
    val version = RendererVersions.of(identifier)

    SettingsCard(
        modifier = modifier,
        position = position,
        onClick = { onClick?.invoke() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = renderer?.getRendererName()
                    ?: stringResource(R.string.settings_renderer_version_unknown),
                style = MaterialTheme.typography.titleSmall
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_renderer_version_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = when {
                        version != null -> version
                        isAuto -> "-"
                        else -> stringResource(R.string.settings_renderer_version_unknown)
                    },
                    style = MaterialTheme.typography.labelSmall
                )
            }

            //支持的游戏版本范围
            renderer?.let { RendererSummaryLayout(it) }

            //选「自动」时说明会按什么顺序挑渲染器（这段说明只在这里出现一次）
            if (isAuto) {
                Text(
                    text = stringResource(R.string.settings_renderer_priority_summary),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (onClick != null) {
                Text(
                    text = stringResource(R.string.settings_renderer_status_open_advanced),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun RendererSummaryLayout(renderer: RendererInterface) {
    FlowRow(
        modifier = Modifier.alpha(0.7f),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        with(renderer) {
            getRendererSummary()?.let { summary ->
                Text(text = summary, style = MaterialTheme.typography.labelSmall)
            }

            val minVer = getDisplayMinMCVersion()
            val maxVer = getDisplayMaxMCVersion()

            if (minVer != null || maxVer != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = stringResource(R.string.renderer_version_support), style = MaterialTheme.typography.labelSmall)

                    minVer?.let {
                        Text(text = ">= $it", style = MaterialTheme.typography.labelSmall)
                    }

                    maxVer?.let {
                        Text(text = "<= $it", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun DriverSummaryLayout(driver: Driver) {
    with(driver) {
        summary?.let { text ->
            Text(
                modifier = Modifier.alpha(0.7f),
                text = text, style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
