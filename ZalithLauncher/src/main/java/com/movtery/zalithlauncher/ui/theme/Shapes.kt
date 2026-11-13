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
 * along with this program. If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.setting.AllSettings

/**
 * 按「卡片圆角」设置生成新版形状体系
 *
 * 27.10.2 起圆角不再是写死的常量，而是个性化页暴露给用户的参数
 * （[AllSettings.launcherCornerRadius]，12~36dp，默认 28dp）。
 * 其余槽位按固定比例跟随主圆角，保证「卡片圆了、对话框还是方的」不会出现：
 *
 * | 槽位 | 与主圆角的关系 |
 * |---|---|
 * | extraSmall | ×0.60 |
 * | small | ×0.85 |
 * | medium（小图标/徽章）| ×1.00 |
 * | large（卡片）| ×1.20 |
 * | extraLarge（面板/对话框）| ×1.45 |
 *
 * ⚠️ 卡片槽位刻意比主圆角更大：圆角从「主圆角」直接等于卡片圆角时，
 * 转弯来得太急（直线突然拐进圆弧）。放大到 1.2 倍后，曲线更早开始、
 * 进入直角的过程更平缓，卡片看起来像是「顺着弯道并进主路」而不是原地急打方向。
 */
@Composable
fun modernShapes(radius: Int = AllSettings.launcherCornerRadius.state): Shapes {
    val card = radius.dp
    return Shapes(
        extraSmall = RoundedCornerShape(card * 0.60f),
        small = RoundedCornerShape(card * 0.85f),
        medium = RoundedCornerShape(card),
        large = RoundedCornerShape(card * 1.20f),
        extraLarge = RoundedCornerShape(card * 1.45f)
    )
}

/**
 * ZyNova 经典形状体系（界面世代 = 旧版）
 *
 * 与 27.3.0 之前完全一致：Material 默认圆角。
 * 用户切到「旧版」界面时圆角会回到这里，**可以随时切回新版**。
 */
val ClassicShapes = Shapes()
