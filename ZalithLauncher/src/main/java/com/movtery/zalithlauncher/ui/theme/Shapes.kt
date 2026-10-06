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
import androidx.compose.ui.unit.dp

/**
 * ZyNova 新版形状体系（界面世代 = 新版）
 *
 * 目标是「极简苹果风」：**更大的圆角 + 更少的层级**。
 * Material 默认圆角偏小（4 / 8 / 12 / 16 / 28），在卡片化布局里会显得「方、硬、密」；
 * 这里整体上移一档，让界面更松弛：
 *
 * | 槽位 | Material 默认 | ZyNova 新版 | 主要用在哪 |
 * |---|---|---|---|
 * | extraSmall | 4dp | 8dp | 徽章、小块标签 |
 * | small | 8dp | 12dp | 输入框、小按钮 |
 * | medium | 12dp | 16dp | 中等容器 |
 * | large | 16dp | **22dp** | **卡片 / 列表项（全局 113 处引用，影响最大）** |
 * | extraLarge | 28dp | 30dp | 对话框 / 大面板（全局 61 处引用） |
 *
 * ⚠️ 这是一次**全局**改动：`MaterialTheme.shapes.large` 在项目里有 113 处引用、
 * `extraLarge` 有 61 处，改这里等于同时改了绝大部分卡片与对话框的圆角。
 * 之所以敢这么做，是因为项目对圆角的使用高度集中（硬编码 `RoundedCornerShape(...)`
 * 只剩 43 处），而不是散落各处 —— 改这里不会出现「一半圆了一半没圆」的割裂感。
 *
 * 如果某个组件必须保持小圆角，请在使用处显式传 `RoundedCornerShape(...)`，
 * **不要**为了个别组件把这里的槽位调回去。
 */
val ModernShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

/**
 * ZyNova 经典形状体系（界面世代 = 旧版）
 *
 * 与 27.3.0 之前完全一致：Material 默认圆角。
 * 用户切到「旧版」界面时圆角会回到这里，**可以随时切回新版**。
 */
val ClassicShapes = Shapes()
