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

package com.movtery.zalithlauncher.setting.enums

import androidx.annotation.StringRes
import com.movtery.zalithlauncher.R

/**
 * 首页按钮材质（议题 #33）
 *
 * - [Default]：传统实心材质，性能最好
 * - [Transparent]：半透明底色 + 轻微描边，**默认值**
 * - [LiquidGlass]：毛玻璃 + 边缘折射高光 + 动态流光
 *
 * ⚠️ 液态玻璃必须满足以下约束：
 * - 只能使用 `Haze` / `drawBehind` + `clip`，**禁止 `renderEffect`**，
 *   否则会把文字一起模糊（issue #2 的旧坑）
 * - 文字统一加极淡阴影，保证任何壁纸下清晰
 * - 用户开启「减少动态效果」或关闭全局 [GlassLevel] 时，强制降级为 [Transparent]
 *   （降级逻辑见 `ui/components/HomeButtons.kt` 的 `rememberHomeButtonMaterial`）
 */
enum class HomeButtonMaterial(
    @field:StringRes
    val textRes: Int
) {
    /** 默认：传统实心材质，无透明度 */
    Default(R.string.settings_launcher_home_button_material_default),

    /** 透明：无模糊的半透明底色 + 轻微描边（默认选项） */
    Transparent(R.string.settings_launcher_home_button_material_transparent),

    /** 液态玻璃：毛玻璃 + 边缘折射高光 + 动态流光 */
    LiquidGlass(R.string.settings_launcher_home_button_material_liquid_glass)
}
