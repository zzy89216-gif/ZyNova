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
 * 液态玻璃（Liquid Glass / Glass UI）效果
 *
 * 26.2.2 起简化为**两档**：
 *
 * - [Off]：关闭，不叠加任何玻璃高光层（默认，性能优先）
 * - [On]：启用动态玻璃，在毛玻璃之上叠加缓慢流动的高光与折射光晕
 *
 * 原先的「标准 / 增强 / ⚠️极致」三档已移除。
 * 移除原因：「极致」档使用 `RuntimeShader` 对整个元素做多重采样模糊与折射扭曲，
 * 它作用在承载文字的图层上，会导致**字体明显模糊、文字渲染异常**（见 issue #2）；
 * 同时该档位的动态模糊半径、动态光照、多层视差、噪点纹理等效果开销极高。
 *
 * 旧配置兼容：升级前保存的 `Standard` / `Enhanced` / `Extreme` 会在
 * [com.movtery.zalithlauncher.setting.loadAllSettings] 中一次性迁移为 [On]，
 * 旧用户升级后依旧保持「玻璃效果开启」，不会因为枚举名变化而回退成关闭。
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
     * 启用动态玻璃：高光缓慢流动，效果最好，但会产生持续动画开销
     */
    On(R.string.settings_launcher_glass_level_on);

    companion object {
        /**
         * 旧版本使用过的档位名称，全部迁移为 [On]
         */
        val LEGACY_ENABLED_NAMES: Set<String> = setOf("Standard", "Enhanced", "Extreme")

        /**
         * 把旧配置中的档位名称解析为当前档位
         *
         * @return 无法识别时返回 null，由调用方决定是否写回默认值
         */
        fun fromLegacyName(name: String?): GlassLevel? = when (name) {
            null -> null
            Off.name -> Off
            On.name -> On
            in LEGACY_ENABLED_NAMES -> On
            else -> null
        }
    }
}
