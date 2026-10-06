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

package com.movtery.zalithlauncher.setting.enums

import androidx.annotation.StringRes
import com.movtery.zalithlauncher.R

/**
 * 界面世代（UI Generation）
 *
 * ZyNova 从 27.3.0 起提供两套并行的界面观感，用户可以在「设置 → 启动器」里**随时双向切换**：
 *
 * - [Modern]：新版界面（**默认**）。重新设计的字阶与形状体系，整体更松弛、更克制，
 *   是 ZyNova 后续持续打磨的方向。
 * - [Classic]：旧版界面。与 27.3.0 之前**完全一致**的字阶与圆角，
 *   供不习惯新观感的用户随时退回。
 *
 * ⚠️ **两个方向都必须一直可切**：
 * 这是一个普通的枚举单选设置，不写「已切换」之类的单向状态 ——
 * 用户切到 [Classic] 之后必须还能切回 [Modern]，不能出现「切过去就回不来」的死角。
 * 新增界面世代时，同样只需要在枚举里加一项，UI 会自动多出一个单选项。
 *
 * 实现位置：字阶与形状分别见
 * [com.movtery.zalithlauncher.ui.theme.ModernTypography] /
 * [com.movtery.zalithlauncher.ui.theme.ClassicTypography] 与
 * [com.movtery.zalithlauncher.ui.theme.ModernShapes] /
 * [com.movtery.zalithlauncher.ui.theme.ClassicShapes]。
 */
enum class UiGeneration(
    @field:StringRes
    val textRes: Int
) {
    /**
     * 新版界面（默认）：重新设计的字阶与形状体系
     */
    Modern(R.string.settings_launcher_ui_generation_modern),

    /**
     * 旧版界面：与 27.3.0 之前完全一致的观感，可随时切回新版
     */
    Classic(R.string.settings_launcher_ui_generation_classic)
}
