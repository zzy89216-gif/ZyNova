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
 * 液态玻璃（Liquid Glass / Glass UI）效果档位
 *
 * Android 设备的 GPU 性能差异很大，因此默认关闭；
 * 并且只有最高档位才会启用持续动画，避免不必要的 GPU 负载。
 */
enum class GlassLevel(
    @field:StringRes
    val textRes: Int
) {
    /**
     * 关闭：不叠加任何额外的玻璃高光层，性能优先（默认）
     */
    Off(R.string.settings_launcher_glass_level_off),

    /**
     * 标准：叠加静态高光，不启动任何持续动画，开销很低
     */
    Standard(R.string.settings_launcher_glass_level_standard),

    /**
     * 增强：高光缓慢流动，效果最好，但会产生持续动画开销
     */
    Enhanced(R.string.settings_launcher_glass_level_enhanced)
}
