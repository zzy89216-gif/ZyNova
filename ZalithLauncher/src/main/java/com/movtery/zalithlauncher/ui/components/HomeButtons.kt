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

package com.movtery.zalithlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.HomeButtonMaterial
import com.movtery.zalithlauncher.setting.enums.isLauncherInDarkTheme
import com.movtery.zalithlauncher.ui.screens.content.elements.backgroundGlass
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor

/**
 * 解析当前实际生效的首页按钮材质。
 *
 * 降级规则（议题 #33）：
 * - 开启「减少动态效果」→ 液态玻璃降级为透明，不再有任何持续动画
 *
 * 注意：这里必须读取 `AllSettings` 的 Compose 状态，设置页切换后才能实时生效。
 */
@Composable
fun rememberHomeButtonMaterial(): HomeButtonMaterial {
    val selected = AllSettings.homeButtonMaterial.state
    val reduceMotion = AllSettings.launcherReduceMotion.state
    return if (selected == HomeButtonMaterial.LiquidGlass && reduceMotion) {
        HomeButtonMaterial.Transparent
    } else {
        selected
    }
}

/**
 * 幽灵按钮（Ghost Button）底色 —— 按钮层级里的「次级操作」。
 *
 * 与卡片底色保持 **2% ~ 3%** 的明暗差（深色主题 ×1.05，浅色主题 ×0.96）：
 * 主操作 / 次级操作相邻摆放时不会糊成一片，同时也不会抢走主按钮的注意力。
 * 不靠阴影区分（低端机阴影开销大，还会在纯色主题下发灰）。
 */
@Composable
fun homeButtonGhostColor(): Color {
    val base = cardColor()
    val factor = if (isLauncherInDarkTheme()) 1.05f else 0.96f
    return Color(
        red = (base.red * factor).coerceIn(0f, 1f),
        green = (base.green * factor).coerceIn(0f, 1f),
        blue = (base.blue * factor).coerceIn(0f, 1f),
        //保留一点透明度：背景是图片 / 视频时，按钮会随画面实时变化，而不是锁成一块死色
        alpha = base.alpha.coerceAtMost(0.92f)
    )
}

/**
 * 首页按钮材质背景。
 *
 * 26.10.2 起不再有常驻流光：液态玻璃完全由 [backgroundGlass] 的
 * 「竖直渐变 + 1px 亮边」表达，滚动、拖动时没有额外绘制开销。
 * 三档材质都补一层 1px 描边，保证相邻按钮之间有清晰的边界。
 *
 * @param drawDefault 默认档位是否也绘制实心底色。
 *        顶部导航图标本身没有实心底色，传 false 可保持原样。
 */
@Composable
fun Modifier.homeButtonMaterial(
    material: HomeButtonMaterial,
    shape: Shape,
    containerColor: Color,
    drawDefault: Boolean = true,
): Modifier {
    if (material == HomeButtonMaterial.Default && !drawDefault) return this

    val dark = isLauncherInDarkTheme()
    val base = if (dark) Color.White else Color.Black
    val edge = base.copy(alpha = if (dark) 0.10f else 0.08f)
    val clipped = this.clip(shape)

    return when (material) {
        //默认档也只是略实一点，仍然保留透明度，跟着背景动
        HomeButtonMaterial.Default -> clipped.background(
            containerColor.copy(alpha = (containerColor.alpha + 0.10f).coerceAtMost(0.92f))
        )

        HomeButtonMaterial.Transparent -> clipped
            .background(containerColor)
            .border(1.dp, edge, shape)

        //玻璃档已经自带 1px 亮边，不再重复描边
        HomeButtonMaterial.LiquidGlass -> clipped
            .background(containerColor)
            .backgroundGlass(AllSettings.backgroundBlur.state, containerColor)
    }
}

/**
 * 非默认材质下的文字阴影（极淡，保证任何壁纸下均清晰）。
 */
@Composable
fun homeButtonTextStyle(
    style: TextStyle,
    material: HomeButtonMaterial,
): TextStyle {
    if (material == HomeButtonMaterial.Default) return style
    val blur = with(LocalDensity.current) { 2.dp.toPx() }
    return style.copy(
        shadow = Shadow(
            color = Color.Black.copy(alpha = 0.45f),
            offset = Offset.Zero,
            blurRadius = blur
        )
    )
}

/**
 * 首页主操作按钮（启动、快捷创建等）
 */
@Composable
fun HomeMaterialButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    material: HomeButtonMaterial,
    containerColor: Color = homeButtonGhostColor(),
    contentColor: Color = onCardColor(),
    shape: Shape = MaterialTheme.shapes.large,
    enabled: Boolean = true,
    minHeight: Dp = 40.dp,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .heightIn(min = minHeight)
            .homeButtonMaterial(material, shape, containerColor)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            leadingIcon?.invoke()
            Text(
                text = text,
                style = homeButtonTextStyle(textStyle, material),
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 首页世界 / 服务器条目按钮（内容左对齐，支持两行）
 */
@Composable
fun HomeMaterialChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    material: HomeButtonMaterial,
    containerColor: Color = homeButtonGhostColor(),
    contentColor: Color = onCardColor(),
    shape: Shape = MaterialTheme.shapes.medium,
    content: @Composable ColumnScope.() -> Unit,
) {
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Column(
            modifier = modifier
                .homeButtonMaterial(material, shape, containerColor)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp),
            horizontalAlignment = Alignment.Start,
            content = content
        )
    }
}
