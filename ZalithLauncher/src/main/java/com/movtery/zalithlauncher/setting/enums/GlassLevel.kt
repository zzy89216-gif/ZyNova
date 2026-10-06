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
 * - [Off]：关闭，不叠加任何玻璃高光层（默认，性能优先）
 * - [On]：启用动态玻璃，在毛玻璃之上叠加缓慢流动的高光与折射光晕
 *
 * 历史沿革（改这块前请先读，避免又把高风险档位接回来）：
 * - 26.2.2 起由「标准 / 增强 / ⚠️极致」简化为两档 [Off] / [On]。
 *   旧「极致」档使用 `RuntimeShader` + `renderEffect` 对整个元素做多重采样模糊与折射扭曲，
 *   而 `renderEffect` 作用在承载文字的整个图层上，导致**字体明显模糊、文字渲染异常**
 *   （issue #2），且开销极高。
 * - 27.2.0 曾加入第三档「强效动态玻璃」（`Intense`）：更快的流动高光 + 整体明暗脉动。
 *   它属于**持续明暗变化**，存在光敏风险，因此当时配套了启动光敏性警告。
 * - **27.3.0 已移除该档位与配套的光敏性警告**，始终只保留 [Off] / [On] 两档；
 *   现在的实现只用 `drawBehind` 画渐变高光，**不碰 renderEffect**，所以不会重现旧「极致」档
 *   把文字一起模糊的问题。
 *
 * ⚠️ 本次移除后，应用内**不再存在**任何快速闪烁、频闪或持续明暗脉动。
 * 因此**不要**再引入「会引起持续明暗变化」的视觉档位；若将来确实需要，
 * 必须同时补回光敏性警告与「减少动态效果」的强制降级。
 *
 * 旧配置兼容：升级前保存的 `Standard` / `Enhanced` / `Extreme`（26.2.2 之前）
 * 以及 `Intense`（27.2.0 的强效档）都会在
 * [com.movtery.zalithlauncher.setting.loadAllSettings] 中一次性迁移为 [On]。
 * 这是必须的 —— 枚举按**名称**持久化，名称消失会静默回退成默认值 [Off]，
 * 用户会以为自己的玻璃效果被关掉了。
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
         * 旧版本使用过的、**代表「玻璃效果开启」**的档位名称，全部迁移为 [On]
         *
         * - `Standard` / `Enhanced` / `Extreme`：26.2.2 之前的三档
         * - `Intense`：27.2.0 引入、27.3.0 移除的「强效动态玻璃」
         *
         * ⚠️ 每移除一个「开启类」档位，都必须把它的名称加到这里，
         * 否则老用户会被静默回退成 [Off]。
         */
        val LEGACY_ENABLED_NAMES: Set<String> =
            setOf("Standard", "Enhanced", "Extreme", "Intense")

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
