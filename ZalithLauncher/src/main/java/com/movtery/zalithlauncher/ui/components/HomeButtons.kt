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

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import com.movtery.zalithlauncher.setting.enums.BackgroundBlur
import com.movtery.zalithlauncher.setting.enums.GlassLevel
import com.movtery.zalithlauncher.setting.enums.HomeButtonMaterial
import com.movtery.zalithlauncher.setting.enums.isLauncherInDarkTheme
import com.movtery.zalithlauncher.ui.screens.content.elements.backgroundGlass
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor

/**
 * 解析当前实际生效的首页按钮材质。
 *
 * 降级规则（议题 #33）：
 * - 开启「减少动态效果」→ 强制透明，且不执行实时模糊
 * - 全局动态玻璃 [GlassLevel] 关闭 → 强制透明，不执行实时模糊
 *
 * 注意：这里必须读取 `AllSettings` 的 Compose 状态，设置页切换后才能实时生效。
 */
@Composable
fun rememberHomeButtonMaterial(): HomeButtonMaterial {
    val selected = AllSettings.homeButtonMaterial.state
    val reduceMotion = AllSettings.launcherReduceMotion.state
    val glassLevel = AllSettings.glassLevel.state
    return if (
        selected == HomeButtonMaterial.LiquidGlass &&
        (reduceMotion || glassLevel == GlassLevel.Off)
    ) {
        HomeButtonMaterial.Transparent
    } else {
        selected
    }
}

/**
 * 幽灵按钮（Ghost Button）底色：
 * 与当前卡片/背景只保留约 1.5% ~ 2% 的明暗差，不提高透明度、不加描边。
 * 这样叠在动态玻璃上时不会出现「按钮上又盖一层玻璃」的冲突。
 */
@Composable
fun homeButtonGhostColor(): Color {
    val base = cardColor()
    //浅色主题稍微压暗，深色主题稍微提亮；幅度刻意很小
    val factor = if (isLauncherInDarkTheme()) 1.02f else 0.985f
    return Color(
        red = (base.red * factor).coerceIn(0f, 1f),
        green = (base.green * factor).coerceIn(0f, 1f),
        blue = (base.blue * factor).coerceIn(0f, 1f),
        //保留一点透明度：背景是图片 / 视频时，按钮会随画面实时变化，而不是锁成一块死色
        alpha = base.alpha.coerceAtMost(0.90f)
    )
}

/**
 * 首页按钮材质背景。
 *
 * ⚠️ 液态玻璃只使用 `backgroundGlass`（Haze）+ `drawBehind` 渐变高光，
 * **不碰 renderEffect**，文字层始终清晰。
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

    val clipped = this.clip(shape)
    return when (material) {
        //默认档也只是略实一点，仍然保留透明度，跟着背景动
        HomeButtonMaterial.Default -> clipped.background(
            containerColor.copy(alpha = (containerColor.alpha + 0.10f).coerceAtMost(0.92f))
        )

        HomeButtonMaterial.Transparent -> clipped.background(containerColor)

        HomeButtonMaterial.LiquidGlass -> {
            //按钮通常比整页背景小，模糊半径取一个下限，避免用户把全局模糊设为 0 时毫无质感
            val blur = maxOf(AllSettings.backgroundBlur.state, 18)
            var glass = clipped
                .background(containerColor)
                .backgroundGlass(blur, containerColor.copy(alpha = 0.12f))

            //背景模糊模式下 backgroundGlass 不会叠加动态高光，这里补一层；
            //前景模糊模式下 backgroundGlass 已经带流动高光，不再重复叠加
            if (AllSettings.backgroundBlurType.state == BackgroundBlur.Background) {
                glass = glass.homeButtonSheen()
            }
            glass
        }
    }
}

/**
 * 液态玻璃的缓慢流光（仅用于背景模糊模式下的补充）。
 *
 * 与 `backgroundGlass` 里的动态高光保持同一性质：只有缓慢移动，没有明暗脉动。
 */
@Composable
private fun Modifier.homeButtonSheen(): Modifier {
    val transition = rememberInfiniteTransition(label = "homeButtonSheen")
    val shift by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "homeButtonSheenShift"
    )

    return this.drawBehind {
        val diagonal = size.width + size.height
        val start = Offset(shift * diagonal - size.width, -size.height)
        val end = Offset(shift * diagonal + size.width, size.height)
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.30f),
                    Color.White.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                start = start,
                end = end
            )
        )
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
