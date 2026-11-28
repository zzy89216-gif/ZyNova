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

package com.movtery.zalithlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.setting.enums.isLauncherInDarkTheme

/**
 * Codex 风格的「液态玻璃」表面。
 *
 * 与旧实现的关键区别：
 * - **不做逐元素实时模糊**。背景已经由 [com.movtery.zalithlauncher.ui.screens.content.elements.Background]
 *   统一模糊过一次，这里的玻璃只负责「半透明渐变 + 1px 亮边」，滚动/拖动时没有额外 GPU 开销；
 * - 深色主题用白色低透明度叠加，浅色主题用黑色低透明度叠加，保证单色观感；
 * - 圆角默认 28dp（面板级），按钮/胶囊可传更小的 [shape]。
 *
 * 注意：27.10.2 起「动态玻璃」概念完全移除，玻璃质感是全局统一的基础材质，
 * 不再是可开关的特效，也不再有任何常驻动画。强度由「玻璃强度」设置项驱动。
 */
@Composable
fun Modifier.codexGlass(
    shape: Shape = RoundedCornerShape(28.dp),
    strength: Float = 0.08f,
    borderWidth: Dp = 1.dp,
): Modifier {
    val dark = isLauncherInDarkTheme()
    val base = if (dark) Color.White else Color.Black

    val top = base.copy(alpha = strength)
    val bottom = base.copy(alpha = strength * 0.45f)
    val borderColor = base.copy(alpha = if (dark) 0.10f else 0.08f)

    return this
        .clip(shape)
        .background(Brush.verticalGradient(listOf(top, bottom)))
        .border(borderWidth, borderColor, shape)
}
