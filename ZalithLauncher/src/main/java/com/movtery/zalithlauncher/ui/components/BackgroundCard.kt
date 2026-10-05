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

package com.movtery.zalithlauncher.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.UiComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.screens.content.elements.backgroundGlass
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.cardTitleColor
import com.movtery.zalithlauncher.ui.theme.onCardColor

/**
 * 背景卡片组件，
 * 使用方式与原本的[Card]无异，但[BackgroundCard]配置了更舒适的背景颜色
 */
@Composable
fun BackgroundCard(
    modifier: Modifier = Modifier,
    influencedByBackground: Boolean = true,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = cardColor(influencedByBackground),
        contentColor = onCardColor()
    ),
    elevation: CardElevation = CardDefaults.cardElevation(),
    blur: Int = AllSettings.backgroundBlur.state,
    border: BorderStroke? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
    ) {
        Column(
            modifier = Modifier.backgroundGlass(blur, colors.containerColor, influencedByBackground),
            content = content
        )
    }
}

/**
 * 背景卡片组件，
 * 使用方式与原本的[Card]无异，但[BackgroundCard]配置了更舒适的背景颜色
 */
@Composable
fun BackgroundCard(
    modifier: Modifier = Modifier,
    influencedByBackground: Boolean = true,
    shape: Shape = CardDefaults.shape,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = cardColor(influencedByBackground),
        contentColor = onCardColor(),
        disabledContainerColor = cardColor(influencedByBackground)
    ),
    elevation: CardElevation = CardDefaults.cardElevation(),
    blur: Int = AllSettings.backgroundBlur.state,
    border: BorderStroke? = null,
    onClick: () -> Unit,
    enabled: Boolean = true,
    content: @Composable @UiComposable ColumnScope.() -> Unit
) {
    //统一的卡片按压反馈：按下时轻微缩小，抬起后回弹，
    //让「我刚点的这一下已经生效了」有一个直观的视觉回应。
    //开启「减少动态效果」时完全不缩放（不只是把时长改短）。
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val reduceMotion = AllSettings.launcherReduceMotion.state
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled && !reduceMotion) 0.97f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "BackgroundCardPressScale"
    )

    Card(
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
    ) {
        Column(
            modifier = Modifier.backgroundGlass(blur, colors.containerColor, influencedByBackground),
            content = content
        )
    }
}

/**
 * 适合在背景卡片组件顶部使用的标题栏Layout
 * @param alpha 组件背景颜色不透明度
 */
@Composable
fun CardTitleLayout(
    modifier: Modifier = Modifier,
    influencedByBackground: Boolean = true,
    alpha: Float = 0.5f,
    color: Color = influencedByBackgroundColor(
        color = cardTitleColor(alpha),
        enabled = influencedByBackground
    ),
    contentColor: Color = onCardColor(),
    blur: Int = AllSettings.backgroundBlur.state,
    content: @Composable @UiComposable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = color,
            contentColor = contentColor,
        ) {
            Column(
                modifier = Modifier.backgroundGlass(blur, color, influencedByBackground),
                content = content
            )
        }
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
    }
}