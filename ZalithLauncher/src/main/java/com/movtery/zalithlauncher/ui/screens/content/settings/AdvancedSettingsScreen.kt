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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.unit.floatRange
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.AnimatedColumn
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SwitchSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.IntSliderSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.movtery.zalithlauncher.utils.file.shareFile
import com.movtery.zalithlauncher.utils.logging.Logger
import java.io.File

private const val TAG = "AdvancedSettings"

/**
 * 设置中心 → 高级设置（议题 #37 / #41）
 *
 * 只放两类东西：
 * 1. **低频但有用**的入口（Java 管理、鼠标/手势、手柄、按键布局管理）；
 * 2. **出问题才需要**的诊断项（日志保留、日志打包）。
 *
 * 常规设置不往这里塞：这里的存在意义是「让主界面保持简单」，
 * 而不是把什么都藏进来。
 */
@Composable
fun AdvancedSettingsScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?,
    toSettingsScreen: (TitledNavKey) -> Unit,
    toFavorites: () -> Unit,
) {
    val context = LocalContext.current

    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.Advanced, settingsScreenKey, false)
    ) { isVisible ->
        AnimatedColumn(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScrollWithBar(state = rememberScrollState())
                .padding(all = 12.dp),
            isVisible = isVisible
        ) { scope ->
            //渲染器详细参数改成独立页面，这里只放入口（反馈：一长串参数堆在高级设置里太乱）
            AnimatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    SectionLabel(stringResource(R.string.settings_advanced_renderer_title))

                    SettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        title = stringResource(R.string.settings_advanced_renderer_mg),
                        summary = stringResource(R.string.settings_renderer_advanced_summary),
                        onClick = { toSettingsScreen(NormalNavKey.Settings.RendererAdvancedMobileGlues) }
                    )
                    SettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        title = stringResource(R.string.settings_advanced_renderer_ltw),
                        summary = stringResource(R.string.settings_renderer_ltw_summary),
                        onClick = { toSettingsScreen(NormalNavKey.Settings.RendererAdvancedLtw) }
                    )
                    SettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        title = stringResource(R.string.settings_advanced_renderer_gl4es),
                        summary = stringResource(R.string.settings_renderer_advanced_summary),
                        onClick = { toSettingsScreen(NormalNavKey.Settings.RendererAdvancedGl4es) }
                    )
                }
            }

            AnimatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    SettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        title = stringResource(R.string.settings_tab_java_manage),
                        summary = stringResource(R.string.settings_advanced_java_entry_summary),
                        onClick = { toSettingsScreen(NormalNavKey.Settings.JavaManager) }
                    )

                    SettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        title = stringResource(R.string.settings_tab_control),
                        summary = stringResource(R.string.settings_advanced_control_entry_summary),
                        onClick = { toSettingsScreen(NormalNavKey.Settings.Control) }
                    )

                    SettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        title = stringResource(R.string.settings_tab_gamepad),
                        summary = stringResource(R.string.settings_advanced_gamepad_entry_summary),
                        onClick = { toSettingsScreen(NormalNavKey.Settings.Gamepad) }
                    )

                    SettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Middle,
                        title = stringResource(R.string.settings_tab_control_manage),
                        summary = stringResource(R.string.settings_advanced_control_manage_entry_summary),
                        onClick = { toSettingsScreen(NormalNavKey.Settings.ControlManager) }
                    )

                    //议题 #39：收藏与本地归档（导入 / 查看 / 删除）
                    SettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        title = stringResource(R.string.favorites_title),
                        summary = stringResource(R.string.favorites_import),
                        onClick = toFavorites
                    )
                }
            }

            AnimatedItem(scope) { yOffset ->
                SettingsCardColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
                ) {
                    IntSliderSettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Top,
                        unit = AllSettings.launcherLogRetentionDays,
                        title = stringResource(R.string.settings_launcher_log_retention_days_title),
                        summary = stringResource(R.string.settings_launcher_log_retention_days_summary),
                        valueRange = AllSettings.launcherLogRetentionDays.floatRange,
                        suffix = stringResource(R.string.unit_day)
                    )

                    SettingsCard(
                        modifier = Modifier.fillMaxWidth(),
                        position = CardPosition.Bottom,
                        title = stringResource(R.string.settings_launcher_log_share_title),
                        summary = stringResource(R.string.settings_launcher_log_share_summary),
                        onClick = {
                            TaskSystem.submitTask(
                                Task.runTask(
                                    id = "ZIP_LOGS",
                                    task = { task ->
                                        task.updateProgress(-1f)
                                        task.updateMessage(androidText(R.string.settings_launcher_log_share_packing))
                                        val logsFile = File(PathManager.DIR_CACHE, "logs.zip")
                                        Logger.pack(logsFile)
                                        task.updateProgress(1f)
                                        task.updateMessage(null)
                                        shareFile(
                                            context = context,
                                            file = logsFile
                                        )
                                    },
                                    onError = { e ->
                                        Logger.error(TAG, "Failed to package log files.", e)
                                    }
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}
/** 分组标题：与设置中心其它分组保持同样的排版（小号 + 次要色 + 左缩进） */
@Composable
private fun SectionLabel(text: String) {
    Text(
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp),
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
