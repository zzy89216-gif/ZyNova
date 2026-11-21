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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.screens.content.download

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.notification.NotificationManager
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.assets.download.DownloadAssetsScreen
import com.movtery.zalithlauncher.ui.screens.content.download.assets.search.SearchModPackScreen
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.ui.screens.onBack
import com.movtery.zalithlauncher.ui.screens.rememberTransitionSpec
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.ModpackImportViewModel

/**
 * 整合包下载页
 *
 * ⚠️ 安装状态（「安装成功」、兼容性警告、输入版本名、移动网络确认）不再挂在这个页面里：
 * 页面只负责触发，状态在 Activity 级的 [ModPackViewModel] 里，
 * 弹窗由 MainActivity 的 [ModPackDownloadInstallDialogs] 渲染（议题 #72）。
 */
@Composable
fun DownloadModPackScreen(
    key: NestedNavKey.DownloadModPack,
    mainScreenKey: TitledNavKey?,
    downloadScreenKey: TitledNavKey?,
    downloadModPackScreenKey: TitledNavKey?,
    onCurrentKeyChange: (TitledNavKey?) -> Unit,
    importerViewModel: ModpackImportViewModel,
    eventViewModel: EventViewModel,
    viewModel: ModPackViewModel
) {
    val context = LocalContext.current
    val backStack = key.backStack
    val stackTopKey = backStack.lastOrNull()
    LaunchedEffect(stackTopKey) {
        onCurrentKeyChange(stackTopKey)
    }

    if (backStack.isNotEmpty()) {
        NavDisplay(
            backStack = backStack,
            modifier = Modifier.fillMaxSize(),
            onBack = {
                onBack(key)
            },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            transitionSpec = rememberTransitionSpec(),
            popTransitionSpec = rememberTransitionSpec(),
            entryProvider = entryProvider {
                entry<NormalNavKey.SearchModPack> {
                    SearchModPackScreen(
                        mainScreenKey = mainScreenKey,
                        downloadScreenKey = downloadScreenKey,
                        downloadModPackScreenKey = key,
                        downloadModPackScreenCurrentKey = downloadModPackScreenKey,
                        viewModel = importerViewModel,
                        eventViewModel = eventViewModel,
                        //一键下载：主页资源中心与实例资源管理都直接可用（不需要实例上下文）
                        onQuickInstall = { platform, projectId, iconUrl ->
                            viewModel.quickInstall(
                                context = context,
                                platform = platform,
                                projectId = projectId,
                                iconUrl = iconUrl
                            )
                        }
                    ) { platform, projectId, iconUrl ->
                        backStack.navigateTo(
                            NormalNavKey.DownloadAssets(platform, projectId, PlatformClasses.MOD_PACK, iconUrl)
                        )
                    }
                }
                entry<NormalNavKey.DownloadAssets> { assetsKey ->
                    DownloadAssetsScreen(
                        mainScreenKey = mainScreenKey,
                        parentScreenKey = key,
                        parentCurrentKey = downloadScreenKey,
                        currentKey = downloadModPackScreenKey,
                        key = assetsKey,
                        eventViewModel = eventViewModel,
                        //一键安装（列表每行一个按钮，与模组页同款 UI）：
                        //点一下直接装这一版，不先进详情页。
                        onQuickInstall = { _, version ->
                            if (viewModel.installOperation is ModPackInstallOperation.None) {
                                viewModel.installOperation =
                                    if (!NotificationManager.checkNotificationEnabled(context)) {
                                        ModPackInstallOperation.WarningForNotification(version, null)
                                    } else {
                                        ModPackInstallOperation.Warning(version, null)
                                    }
                            }
                        },
                        onItemClicked = { _, version, iconUrl, _ ->
                            if (viewModel.installOperation !is ModPackInstallOperation.None) {
                                //不是待安装状态，拒绝此次安装
                                return@DownloadAssetsScreen
                            }
                            viewModel.installOperation = if (!NotificationManager.checkNotificationEnabled(context)) {
                                //警告通知权限
                                ModPackInstallOperation.WarningForNotification(version, iconUrl)
                            } else {
                                ModPackInstallOperation.Warning(version, iconUrl)
                            }
                        }
                    )
                }
            }
        )
    } else {
        Box(Modifier.fillMaxSize())
    }
}
