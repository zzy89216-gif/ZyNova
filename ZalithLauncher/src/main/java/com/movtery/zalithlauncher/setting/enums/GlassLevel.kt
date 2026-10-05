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
 * 26.2.2 起简化为两档（[Off] / [On]）；
 * 27.2.0 重新加入了**强效档 [Intense]**，作为刻意保留的高强度视觉效果。
 *
 * - [Off]：关闭，不叠加任何玻璃高光层（默认，性能优先）
 * - [On]：启用动态玻璃，在毛玻璃之上叠加缓慢流动的高光与折射光晕
 * - [Intense]：强效动态玻璃，高光流速更快、更亮，并叠加整体明暗脉动
 *
 * ⚠️ [Intense] 存在**光敏风险**（持续的明暗变化），因此：
 * - 它**不是默认值**，必须由用户主动选择；
 * - 启动器启动时会显示「光敏性警告」（可在设置中关闭）；
 * - 开启「减少动态效果」后，[Intense] 会被无条件降级为静态毛玻璃。
 *
 * 与旧「极致」档的区别：旧档使用 `RuntimeShader` + `renderEffect` 对整个元素做
 * 多重采样模糊与折射扭曲，作用在承载文字的图层上，导致**字体明显模糊、文字渲染异常**
 * （见 issue #2），且开销极高；[Intense] 只用 `drawBehind` 绘制渐变高光，
 * 不触碰 `renderEffect`，因此不会模糊文字。
 *
 * 旧配置兼容：升级前保存的 `Standard` / `Enhanced` / `Extreme` 会在
 * [com.movtery.zalithlauncher.setting.loadAllSettings] 中一次性迁移为 [On]，
 * 旧用户升级后依旧保持「玻璃效果开启」，不会因为枚举名变化而回退成关闭，
 * 也不会被自动升级成带光敏风险的 [Intense]。
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
    On(R.string.settings_launcher_glass_level_on),

    /**
     * 强效动态玻璃：高光流动更快更亮，并叠加整体明暗脉动（⚠️ 有光敏风险）
     */
    Intense(R.string.settings_launcher_glass_level_intense);

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
            Intense.name -> Intense
            in LEGACY_ENABLED_NAMES -> On
            else -> null
        }
    }
}
