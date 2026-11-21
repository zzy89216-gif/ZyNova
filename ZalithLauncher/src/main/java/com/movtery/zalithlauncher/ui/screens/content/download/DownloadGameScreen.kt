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
import com.movtery.zalithlauncher.notification.NotificationManager
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.download.game.DownloadGameWithAddonScreen
import com.movtery.zalithlauncher.ui.screens.content.download.game.SelectGameVersionScreen
import com.movtery.zalithlauncher.ui.screens.navigateTo
import com.movtery.zalithlauncher.ui.screens.onBack
import com.movtery.zalithlauncher.ui.screens.rememberTransitionSpec
import com.movtery.zalithlauncher.utils.network.isUsingMobileData
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.sendKeepScreen

/**
 * 游戏版本下载页
 *
 * ⚠️ 安装状态（包括「安装成功」弹窗、移动网络/通知权限警告）不再挂在这个页面里：
 * 页面只是「触发者」，状态在 Activity 级的 [GameDownloadViewModel] 里，
 * 弹窗由 MainActivity 的 [GameDownloadInstallDialogs] 渲染（议题 #72）。
 */
@Composable
fun DownloadGameScreen(
    key: NestedNavKey.DownloadGame,
    mainScreenKey: TitledNavKey?,
    downloadScreenKey: TitledNavKey?,
    downloadGameScreenKey: TitledNavKey?,
    onCurrentKeyChange: (TitledNavKey?) -> Unit,
    eventViewModel: EventViewModel,
    viewModel: GameDownloadViewModel
) {
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
                entry<NormalNavKey.DownloadGame.SelectGameVersion> {
                    SelectGameVersionScreen(
                        mainScreenKey = mainScreenKey,
                        downloadScreenKey = downloadScreenKey,
                        downloadGameScreenKey = downloadGameScreenKey,
                        eventViewModel = eventViewModel,
                    ) { versionString ->
                        backStack.navigateTo(
                            NormalNavKey.DownloadGame.Addons(versionString)
                        )
                    }
                }
                entry<NormalNavKey.DownloadGame.Addons> { key ->
                    val context = LocalContext.current
                    DownloadGameWithAddonScreen(
                        mainScreenKey = mainScreenKey,
                        downloadScreenKey = downloadScreenKey,
                        downloadGameScreenKey = downloadGameScreenKey,
                        key = key,
                        refreshErrorCheck = viewModel.versionNameErrorCheck
                    ) { info ->
                        if (viewModel.installOperation !is GameInstallOperation.None) {
                            //不是待安装状态，拒绝此次安装
                            return@DownloadGameWithAddonScreen
                        }
                        if (!NotificationManager.checkNotificationEnabled(context)) {
                            //警告通知权限
                            viewModel.installOperation = GameInstallOperation.WarningForNotification(info)
                        } else {
                            if (isUsingMobileData(context)) {
                                //警告正在使用流量
                                viewModel.installOperation = GameInstallOperation.WarningForMobileData(info)
                            } else {
                                viewModel.install(
                                    context = context,
                                    info = info,
                                    onStart = {
                                        eventViewModel.sendKeepScreen(true)
                                    },
                                    onStop = {
                                        eventViewModel.sendKeepScreen(false)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        )
    } else {
        Box(Modifier.fillMaxSize())
    }
}
