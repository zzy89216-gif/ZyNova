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
 * along with this program. If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Material 默认字阶
 *
 * 所有「新版」字阶都以它为基准做 `copy(...)`，
 * 这样以后 Material 调整了字号 / 行高，我们也跟着走，不会写死一份会和上游脱节的副本。
 */
private val materialBase = Typography()

/**
 * ZyNova 新版字阶（界面世代 = 新版）
 *
 * 目标观感：**极简、克制、层级清晰**，接近 iOS 的系统字体层级。
 * 与 Material 默认字阶的三点区别：
 *
 * 1. **标题改用 SemiBold(600)**：Material 默认标题是 Regular，
 *    在 ZyNova 这种卡片化布局里层级不明显，只能靠加大字号区分。
 *    改用 SemiBold 后「标题 / 正文」一眼可分，反而可以少用分隔线和强调色 —— 这正是极简的前提。
 * 2. **大字号收紧字距**（负 letterSpacing）：iOS 的大标题就是靠负字距显得紧凑精神。
 * 3. **小字号保持 0 字距**：中文不像拉丁字母，负字距会把字挤在一起，所以 body 一律不加负字距。
 *
 * ⚠️ 刻意**只改字重与字距，不改字号、不改行高**：
 * 改字号会让既有布局在长文案下换行 / 溢出（对话框高度、卡片宽度都是按现字号调的），
 * 收益小、风险大。需要更大的字请在使用处单独指定 `fontSize`。
 */
val ModernTypography: Typography = Typography(
    // 超大标题：只在实际会用到的大字上收紧字距
    displayLarge = materialBase.displayLarge.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = materialBase.displayMedium.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.4).sp
    ),
    displaySmall = materialBase.displaySmall.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.3).sp
    ),

    // 标题层级：SemiBold + 轻微负字距
    headlineLarge = materialBase.headlineLarge.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.4).sp
    ),
    headlineMedium = materialBase.headlineMedium.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.3).sp
    ),
    headlineSmall = materialBase.headlineSmall.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.2).sp
    ),

    // 卡片 / 设置项标题：这是全局用得最多的层级（titleSmall 遍布设置页）
    titleLarge = materialBase.titleLarge.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.2).sp
    ),
    titleMedium = materialBase.titleMedium.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),
    titleSmall = materialBase.titleSmall.copy(
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.sp
    ),

    // 正文：不动字重与字距，中文正文保持 Material 的舒适值
    bodyLarge = materialBase.bodyLarge,
    bodyMedium = materialBase.bodyMedium,
    bodySmall = materialBase.bodySmall,

    // 标签 / 按钮：SemiBold 让按钮文字更「实」，接近 iOS 的按钮观感
    labelLarge = materialBase.labelLarge.copy(fontWeight = FontWeight.SemiBold),
    labelMedium = materialBase.labelMedium.copy(fontWeight = FontWeight.SemiBold),
    labelSmall = materialBase.labelSmall.copy(fontWeight = FontWeight.Medium)
)

/**
 * ZyNova 经典字阶（界面世代 = 旧版）
 *
 * 与 27.3.0 之前完全一致：Material 默认字阶。
 * 用户切到「旧版」界面时字阶会回到这里，**可以随时切回新版**。
 */
val ClassicTypography: Typography = Typography()
