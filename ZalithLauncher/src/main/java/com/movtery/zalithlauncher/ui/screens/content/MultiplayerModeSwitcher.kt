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

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor

/**
 * 联机页的两种模式。
 *
 * 以前是两张并排卡片（点「樱花」直接跳到另一个页面），
 * 维护者要求改成**屏幕中下方悬浮的小菜单**：两个选项，就地切换内容，不再跳页。
 */
enum class MultiplayerMode(
    @param:StringRes val titleRes: Int
) {
    /** 陶瓦联机（点对点，双方都装启动器） */
    TERRACOTTA(R.string.multiplayer_mode_terracotta_title),

    /** 樱花内网穿透（需要自己的隧道与访问密钥） */
    FRP(R.string.frp_entry_title)
}

/**
 * 联机方式切换菜单。
 *
 * 悬浮在屏幕中下方，样式跟「返回游戏」那颗胶囊一致
 * （`Surface` + 超大圆角 + 轻微阴影），选中项用主色胶囊底衬。
 * 放这么低是为了不挡内容：两种模式的操作区都在上方，手指够得到选项、
 * 又不会盖住按钮。
 */
@Composable
fun MultiplayerModeSwitcher(
    mode: MultiplayerMode,
    onModeChange: (MultiplayerMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(MaterialTheme.shapes.extraLarge),
        shape = MaterialTheme.shapes.extraLarge,
        color = cardColor(false),
        contentColor = onCardColor(),
        tonalElevation = 3.dp,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(all = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MultiplayerMode.entries.forEach { item ->
                MultiplayerModeOption(
                    text = stringResource(item.titleRes),
                    selected = item == mode,
                    onClick = {
                        if (item != mode) onModeChange(item)
                    }
                )
            }
        }
    }
}

@Composable
private fun MultiplayerModeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.extraLarge,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
