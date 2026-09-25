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

package com.movtery.zalithlauncher.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey

class ScreenBackStackViewModel : ViewModel() {
    /**
     * 资源安装上下文：从「版本设置 → 资源管理」进入下载中心时，
     * 记录目标游戏实例名称。为 null 表示从主界面进入的资源中心（纯浏览）。
     *
     * ⚠️ 该状态必须放在 ViewModel 上，而不能放在 NavKey 上：
     * NavKey 是 @Serializable，Navigation3 的 saveable 机制会序列化 / 反序列化 key，
     * @Transient 字段会在这一过程中丢失，导致页面读不到上下文。
     */
    var resourceInstallTarget by mutableStateOf<String?>(null)

    /** 主屏幕 */
    val mainScreen = NestedNavKey.Main()
    /** 设置屏幕 */
    val settingsScreen = NestedNavKey.Settings()
    /** 下载屏幕 */
    val downloadScreen = NestedNavKey.Download()

    /** 下载游戏屏幕 */
    val downloadGameScreen = NestedNavKey.DownloadGame()
    /** 下载整合包屏幕 */
    val downloadModPackScreen = NestedNavKey.DownloadModPack()
    /** 下载模组屏幕 */
    val downloadModScreen = NestedNavKey.DownloadMod()
    /** 下载资源包屏幕 */
    val downloadResourcePackScreen = NestedNavKey.DownloadResourcePack()
    /** 下载存档屏幕 */
    val downloadSavesScreen = NestedNavKey.DownloadSaves()
    /** 下载光影包屏幕 */
    val downloadShadersScreen = NestedNavKey.DownloadShaders()

    /**
     * 在跳转前，先将导航栈中所有属于 [clearBeforeNavKeys] 的页面全部移除
     * 这样可以避免用户在这几个页面间产生叠加栈或多层返回的情况
     */
    val clearBeforeNavKeys = listOf(
        settingsScreen::class,
        downloadScreen::class,
        NormalNavKey.Multiplayer::class
    )
}