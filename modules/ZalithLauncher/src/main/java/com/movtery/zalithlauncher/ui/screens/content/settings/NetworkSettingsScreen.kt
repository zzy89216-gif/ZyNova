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

import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.path.DownloadUrls
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.MirrorSourceType
import com.movtery.zalithlauncher.setting.unit.StringSettingUnit
import com.movtery.zalithlauncher.setting.unit.floatRange
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedColumn
import com.movtery.zalithlauncher.ui.components.OwnOutlinedTextField
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.TitleAndSummary
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.IntSliderSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.ListSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils

/**
 * 设置中心 → 网络与下载（议题 #37 / #41）
 *
 * 这里集中管理「下载走哪条路、走多快、断了怎么办」：
 * 1. 下载源（官方 / 镜像 / 自动）；
 * 2. 下载引擎参数（连接数、超时、重试）；
 * 3. 下载源修改器（逐服务换地址，填错自动回退官方）；
 * 4. 下载缓存与任务管理。
 *
 * ⚠️ 镜像源卡片以前只在「中国大陆」显示，这里取消了这个限制：
 * 换源本身就是给网络不好的人用的，不该由启动器替用户判断他需不需要。
 */
@Composable
fun NetworkSettingsScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.Network, settingsScreenKey, false)
    ) { isVisible ->
        AnimatedColumn(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScrollWithBar(state = rememberScrollState())
                .padding(all = 12.dp),
            isVisible = isVisible
        ) { scope ->
            // ── 下载源 ────────────────────────────────────────────
            AnimatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    ListSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        unit = AllSettings.gameDownloadSource,
                        items = MirrorSourceType.entries,
                        title = stringResource(R.string.settings_launcher_mirror_game_source_title),
                        summary = stringResource(R.string.settings_network_sources_summary),
                        getItemText = { stringResource(it.textRes) }
                    )

                    ListSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        unit = AllSettings.assetPlatformSource,
                        items = MirrorSourceType.entries,
                        title = stringResource(R.string.settings_launcher_mirror_asset_platform_source_title),
                        getItemText = { stringResource(it.textRes) }
                    )
                }
            }

            // ── 下载引擎参数 ──────────────────────────────────────
            AnimatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        unit = AllSettings.downloadConnections,
                        title = stringResource(R.string.settings_network_connections_title),
                        summary = stringResource(R.string.settings_network_connections_summary),
                        valueRange = AllSettings.downloadConnections.floatRange
                    )

                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.downloadConnectTimeout,
                        title = stringResource(R.string.settings_network_connect_timeout_title),
                        summary = stringResource(R.string.settings_network_connect_timeout_summary),
                        valueRange = AllSettings.downloadConnectTimeout.floatRange,
                        suffix = "s"
                    )

                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        unit = AllSettings.downloadReadTimeout,
                        title = stringResource(R.string.settings_network_read_timeout_title),
                        summary = stringResource(R.string.settings_network_read_timeout_summary),
                        valueRange = AllSettings.downloadReadTimeout.floatRange,
                        suffix = "s"
                    )

                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        unit = AllSettings.downloadRetryCount,
                        title = stringResource(R.string.settings_network_retry_title),
                        summary = stringResource(R.string.settings_network_retry_summary),
                        valueRange = AllSettings.downloadRetryCount.floatRange
                    )
                }
            }

            // ── 下载源修改器 ──────────────────────────────────────
            AnimatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    UrlOverrideCard(
                        position = CardPosition.Top,
                        unit = AllSettings.customVersionManifestUrl,
                        title = stringResource(R.string.settings_network_modifier_manifest_title),
                        summary = stringResource(R.string.settings_network_modifier_manifest_summary),
                        hint = DownloadUrls.DEFAULT_VERSION_MANIFEST
                    )

                    UrlOverrideCard(
                        position = CardPosition.Middle,
                        unit = AllSettings.customModrinthApiUrl,
                        title = stringResource(R.string.settings_network_modifier_modrinth_title),
                        summary = stringResource(R.string.settings_network_modifier_modrinth_summary),
                        hint = DownloadUrls.DEFAULT_MODRINTH_API
                    )

                    UrlOverrideCard(
                        position = CardPosition.Middle,
                        unit = AllSettings.customCurseForgeApiUrl,
                        title = stringResource(R.string.settings_network_modifier_curseforge_title),
                        summary = stringResource(R.string.settings_network_modifier_curseforge_summary),
                        hint = DownloadUrls.DEFAULT_CURSEFORGE_API
                    )

                    UrlOverrideCard(
                        position = CardPosition.Bottom,
                        unit = AllSettings.customMcimMirrorUrl,
                        title = stringResource(R.string.settings_network_modifier_mcim_title),
                        summary = stringResource(R.string.settings_network_modifier_mcim_summary),
                        hint = DownloadUrls.DEFAULT_MCIM_ROOT
                    )
                }
            }

            // ── 缓存与任务 ────────────────────────────────────────
            AnimatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    var cacheSize by remember { mutableStateOf<Long?>(null) }
                    var confirmClear by remember { mutableStateOf(false) }
                    var confirmCancelAll by remember { mutableStateOf(false) }
                    val tasks by TaskSystem.tasksFlow.collectAsStateWithLifecycle()

                    val sizeText = cacheSize?.let {
                        Formatter.formatShortFileSize(context, it)
                    } ?: stringResource(R.string.generic_loading)

                    fun refreshCacheSize() {
                        coroutineScope.launch {
                            cacheSize = withContext(Dispatchers.IO) {
                                PathManager.DIR_CACHE.takeIf { it.exists() }
                                    ?.let { FileUtils.sizeOfDirectory(it) } ?: 0L
                            }
                        }
                    }

                    LaunchedEffect(Unit) { refreshCacheSize() }

                    SettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        title = stringResource(R.string.settings_network_cache_title),
                        summary = stringResource(R.string.settings_network_cache_summary, sizeText),
                        onClick = { confirmClear = true },
                        trailingIcon = {
                            TextButton(onClick = { confirmClear = true }) {
                                Text(text = stringResource(R.string.settings_network_cache_clear))
                            }
                        }
                    )

                    SettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        title = stringResource(R.string.settings_network_tasks_title),
                        summary = stringResource(R.string.settings_network_tasks_summary, tasks.size),
                        onClick = {
                            if (tasks.isNotEmpty()) confirmCancelAll = true
                        },
                        trailingIcon = {
                            TextButton(
                                onClick = { confirmCancelAll = true },
                                enabled = tasks.isNotEmpty()
                            ) {
                                Text(text = stringResource(R.string.settings_network_tasks_cancel_all))
                            }
                        }
                    )

                    if (confirmClear) {
                        SimpleAlertDialog(
                            title = stringResource(R.string.settings_network_cache_title),
                            text = stringResource(R.string.settings_network_cache_summary, sizeText),
                            onConfirm = {
                                confirmClear = false
                                coroutineScope.launch {
                                    withContext(Dispatchers.IO) {
                                        PathManager.DIR_CACHE.takeIf { it.exists() }?.listFiles()
                                            ?.forEach { FileUtils.deleteQuietly(it) }
                                    }
                                    refreshCacheSize()
                                }
                            },
                            onDismiss = { confirmClear = false }
                        )
                    }

                    if (confirmCancelAll) {
                        SimpleAlertDialog(
                            title = stringResource(R.string.settings_network_tasks_cancel_all),
                            text = stringResource(R.string.settings_network_tasks_cancel_message),
                            onConfirm = {
                                confirmCancelAll = false
                                TaskSystem.stopAll()
                            },
                            onDismiss = { confirmCancelAll = false }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 单个「下载源地址」输入卡。
 *
 * 设计上的三条约束（对应议题 #37「避免错误配置导致下载任务异常」）：
 * - 留空 = 官方默认，不写任何东西；
 * - 填了但不合法 → 立刻在下面标红提示，**读取时也会回退官方**，不会把错的地址带进下载；
 * - 提供「恢复默认」，一键清空。
 */
@Composable
private fun UrlOverrideCard(
    position: CardPosition,
    unit: StringSettingUnit,
    title: String,
    summary: String,
    hint: String,
) {
    val value = unit.state
    val invalid = value.isNotBlank() && !DownloadUrls.isValid(value)

    SettingsCard(
        modifier = Modifier.fillMaxWidth(),
        position = position
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TitleAndSummary(
                title = title,
                summary = summary
            )

            OwnOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = { unit.save(it) },
                placeholder = { Text(text = hint) },
                singleLine = true,
                isError = invalid,
                shape = MaterialTheme.shapes.large
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = if (invalid) {
                        stringResource(R.string.settings_network_modifier_invalid)
                    } else hint,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (invalid) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                if (value.isNotBlank()) {
                    TextButton(onClick = { unit.save("") }) {
                        Text(text = stringResource(R.string.settings_network_modifier_restore))
                    }
                }
            }
        }
    }
}
