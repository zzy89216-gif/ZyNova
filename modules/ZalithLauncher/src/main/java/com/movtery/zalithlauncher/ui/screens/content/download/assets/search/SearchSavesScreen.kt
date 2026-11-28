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

package com.movtery.zalithlauncher.ui.screens.content.download.assets.search

import androidx.compose.runtime.Composable
import com.movtery.zalithlauncher.game.download.assets.platform.SearchPlatform
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.curseforge.models.CurseForgeSavesCategory
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey

@Composable
fun SearchSavesScreen(
    mainScreenKey: TitledNavKey?,
    downloadScreenKey: TitledNavKey?,
    downloadSavesScreenKey: TitledNavKey,
    downloadSavesScreenCurrentKey: TitledNavKey?,
    /** 资源安装上下文（目标实例名称） */
    installTargetVersion: String? = null,
    /** 一键安装回调；为 null 时不显示安装按钮 */
    onQuickInstall: ((Platform, projectId: String, iconUrl: String?) -> Unit)? = null,
    swapToDownload: (Platform, projectId: String, iconUrl: String?) -> Unit = { _, _, _ -> }
) {
    SearchAssetsScreen(
        mainScreenKey = mainScreenKey,
        parentScreenKey = downloadSavesScreenKey,
        parentCurrentKey = downloadScreenKey,
        screenKey = NormalNavKey.SearchSaves,
        currentKey = downloadSavesScreenCurrentKey,
        platformClasses = PlatformClasses.SAVES,
        //存档资源只有 CurseForge 提供，这里统一使用「所有」选项
        initialPlatform = SearchPlatform.ALL,
        enablePlatform = false,
        getCategories = { CurseForgeSavesCategory.entries },
        mapCategories = { platform, string ->
            CurseForgeSavesCategory.entries.find { it.describe() == string }
        },
        swapToDownload = swapToDownload,
        installTargetVersion = installTargetVersion,
        onQuickInstall = onQuickInstall
    )
}