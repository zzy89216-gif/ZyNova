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

package com.movtery.zalithlauncher.ui.screens.main.card_home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.home.HomeWorld
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.HomeButtonMaterial
import com.movtery.zalithlauncher.ui.components.HomeMaterialChip
import com.movtery.zalithlauncher.ui.components.homeButtonTextStyle
import com.movtery.zalithlauncher.ui.screens.content.elements.backgroundGlass
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor

/**
 * 主页顶部的「最近游玩的 4 个世界」区块
 *
 * - 数据由 `CardHomePage` 从已加载的实例世界数据聚合（多实例、按最后游玩时间倒序），
 *   这里只负责展示，不自己扫描文件系统；
 * - 第 5 个卡片固定是「更多」，点进独立的世界列表页（[com.movtery.zalithlauncher.ui.screens.content.WorldListScreen]）；
 * - 视觉复用实例卡片：同样的单层背景 + 玻璃层 + 世界条目 chip（[HomeEntryChip]），不另造一套。
 *
 * 没有世界时整个区块不显示（由调用方判断列表是否为空）。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecentWorldsSection(
    worlds: List<HomeWorld>,
    /** 卡片大小比例（1f = 100%） */
    scale: Float,
    material: HomeButtonMaterial,
    onPlayWorld: (Version, String) -> Unit,
    onOpenWorldList: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(cardColor())
            .backgroundGlass(AllSettings.backgroundBlur.state, cardColor())
            .padding(
                horizontal = (14 * scale).dp,
                vertical = (12 * scale).dp
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp * scale)
    ) {
        Text(
            text = stringResource(R.string.home_recent_worlds_title),
            style = MaterialTheme.typography.labelLarge,
            color = onCardColor()
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp * scale),
            verticalArrangement = Arrangement.spacedBy(8.dp * scale)
        ) {
            worlds.forEach { world ->
                HomeEntryChip(
                    title = world.name,
                    //跨实例聚合时，实例名比存档的 MC 版本更能说明「这是哪个世界的存档」
                    subtitle = world.instance.getVersionName(),
                    scale = scale,
                    reorderModifier = Modifier,
                    material = material,
                    onClick = { onPlayWorld(world.instance, world.save.saveFile.name) }
                )
            }

            //第 5 张卡片：进入全部世界列表
            MoreWorldsChip(
                scale = scale,
                material = material,
                onClick = onOpenWorldList
            )
        }
    }
}

/** 「更多」卡片：与其它世界条目同尺寸，避免网格里出现一个突兀的按钮 */
@Composable
private fun MoreWorldsChip(
    scale: Float,
    material: HomeButtonMaterial,
    onClick: () -> Unit,
) {
    HomeMaterialChip(
        modifier = Modifier.widthIn(
            min = (120 * scale).dp,
            max = (240 * scale).dp
        ),
        material = material,
        onClick = onClick
    ) {
        Text(
            text = stringResource(R.string.home_recent_worlds_more),
            style = homeButtonTextStyle(MaterialTheme.typography.labelLarge, material),
            maxLines = 1
        )
        Text(
            text = stringResource(R.string.world_list_title),
            style = homeButtonTextStyle(MaterialTheme.typography.labelSmall, material),
            maxLines = 1
        )
    }
}
