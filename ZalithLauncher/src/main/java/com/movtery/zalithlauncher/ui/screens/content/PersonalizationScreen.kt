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

package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.unit.floatRange
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.IntSliderSettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCard
import com.movtery.zalithlauncher.viewmodel.LocalBackgroundViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

/**
 * 个性化页（议题 #42 · 任务 2）
 *
 * 设计目标：**把最高频的观感调整从「设置 → 通用」里提出来**，
 * 用户不必在长长的设置列表里翻找，只需要一个页面就能把启动器调成自己想要的样子。
 *
 * 这里只放 4 项最常用的：壁纸、背景不透明度、背景模糊、卡片圆角，
 * 外加玻璃强度与「减少动态效果」两个开关；
 * 其余低频项仍然在「设置 → 通用」里，两处读写的是同一套 [AllSettings]，
 * 不存在「个性化页改了这个值、设置页又改回去」的分叉。
 */
@Composable
fun PersonalizationScreen(
    backScreenViewModel: ScreenBackStackViewModel,
) {
    val settingsScreen = backScreenViewModel.settingsScreen

    BaseScreen(
        screenKey = NormalNavKey.Personalization,
        currentKey = backScreenViewModel.mainScreen.currentKey
    ) { isVisible ->
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScrollWithBar(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val backgroundViewModel = LocalBackgroundViewModel.current
                val hasBackground = backgroundViewModel?.isValid == true

                //壁纸：真正的选图 / 选视频流程在设置页（需要系统选择器 + 导入进度），
                //这里只做入口，避免把两套导入逻辑各自维护一份
                SettingsCard(
                    modifier = Modifier.fillMaxWidth(),
                    position = CardPosition.Top,
                    title = stringResource(R.string.personalization_wallpaper_title),
                    summary = stringResource(
                        if (hasBackground) {
                            R.string.personalization_wallpaper_summary_set
                        } else {
                            R.string.personalization_wallpaper_summary_empty
                        }
                    ),
                    onClick = {
                        //追加在栈顶：从个性化进设置后，返回会回到个性化页（上一级），而不是跳主界面
                        settingsScreen.navigateTo(NormalNavKey.Settings.Launcher)
                        backScreenViewModel.mainScreen.navigateTo(settingsScreen)
                    },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_right_rounded),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                IntSliderSettingsCard(
                    modifier = Modifier.fillMaxWidth(),
                    position = CardPosition.Middle,
                    unit = AllSettings.launcherBackgroundOpacity,
                    title = stringResource(R.string.settings_launcher_background_opacity_title),
                    summary = stringResource(R.string.settings_launcher_background_opacity_summary),
                    valueRange = AllSettings.launcherBackgroundOpacity.floatRange,
                    suffix = "%",
                    enabled = hasBackground,
                    fineTuningControl = true
                )

                IntSliderSettingsCard(
                    modifier = Modifier.fillMaxWidth(),
                    position = CardPosition.Middle,
                    unit = AllSettings.backgroundBlur,
                    title = stringResource(R.string.settings_title_blur),
                    summary = stringResource(R.string.settings_launcher_background_blur_summary),
                    valueRange = AllSettings.backgroundBlur.floatRange,
                    suffix = "Dp",
                    enabled = hasBackground,
                    fineTuningControl = true
                )

                IntSliderSettingsCard(
                    modifier = Modifier.fillMaxWidth(),
                    position = CardPosition.Middle,
                    unit = AllSettings.launcherCornerRadius,
                    title = stringResource(R.string.settings_launcher_corner_radius_title),
                    summary = stringResource(R.string.settings_launcher_corner_radius_summary),
                    valueRange = AllSettings.launcherCornerRadius.floatRange,
                    suffix = "Dp",
                    fineTuningControl = true
                )

                IntSliderSettingsCard(
                    modifier = Modifier.fillMaxWidth(),
                    position = CardPosition.Bottom,
                    unit = AllSettings.codexGlassStrength,
                    title = stringResource(R.string.settings_launcher_glass_strength_title),
                    summary = stringResource(R.string.settings_launcher_glass_strength_summary),
                    valueRange = AllSettings.codexGlassStrength.floatRange,
                    suffix = "%",
                    fineTuningControl = true
                )

                //更多设置：直接送到「设置 → 通用」，其余低频项都在那里
                SettingsCard(
                    modifier = Modifier.fillMaxWidth(),
                    position = CardPosition.Single,
                    title = stringResource(R.string.personalization_more_settings),
                    summary = stringResource(R.string.personalization_more_settings_summary),
                    onClick = {
                        //追加在栈顶：从个性化进设置后，返回会回到个性化页（上一级），而不是跳主界面
                        settingsScreen.navigateTo(NormalNavKey.Settings.Launcher)
                        backScreenViewModel.mainScreen.navigateTo(settingsScreen)
                    },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_right_rounded),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }
    }
}
