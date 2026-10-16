/*
 * ZyNova Launcher
 * Copyright (C) 2026 zzy89216-gif and contributors
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

package com.movtery.zalithlauncher.game.renderer.mobileglues

/**
 * MobileGlues 的唯一标识符。
 *
 * 这个值会存进用户配置（`AllSettings.renderer` / `VersionConfig.renderer`），
 * **永远不要改**，否则老用户已保存的渲染器选择会失效并回退到渲染器列表首个。
 *
 * 与 Ironized Zink 的 `IRONIZED_ZINK_UNIQUE_IDENTIFIER` 同理，**刻意放在这里**
 * 而不是挂在 `MobileGluesRenderer` 对象上：
 * `AllSettings` 需要引用它作为默认值，若它挂在渲染器对象上就会形成
 * `AllSettings` → 渲染器 → `AllSettings` 的初始化成环
 * （`MobileGluesRenderer.getRendererEnv()` 要读 `AllSettings.mobileGluesGlVersion`）。
 */
const val MOBILE_GLUES_UNIQUE_IDENTIFIER = "c0c6a2f6-3a5b-4c1c-9e6f-5b3b9e37d531"

/**
 * MobileGlues 暴露给用户的 OpenGL 目标版本。
 *
 * 取值集合与上游插件（`com.fcl.plugin.mobileglues.settings.GlVersion`）一致，
 * [wire] 就是写进 `config.json` 的 `customGLVersion` 整数，
 * 必须与 MobileGlues native 端 `config/settings.cpp` 的夹取规则保持一致。
 *
 * ⚠️ 这里**刻意不提供**上游那个 `Default(0)` 档：native 端 `DEFAULT_GL_VERSION = 40`，
 * 也就是说 `0` 与 `40` 在游戏里是同一件事（都是 OpenGL 4.0）。
 * 既然结果完全相同，让用户在「默认」和「4.0」之间做一次没有差别的选择，
 * 只会增加困惑（项目原则：Context First, Less Steps）。
 * 解析旧配置时遇到 `0` 仍然会正确落到 [GL_4_0]。
 */
enum class MobileGluesGlVersion(
    /** 写进 `config.json` 的 `customGLVersion` */
    val wire: Int,
    /** 界面与日志里展示的名字 */
    val displayName: String,
) {
    GL_4_6(46, "OpenGL 4.6"),
    GL_4_5(45, "OpenGL 4.5"),
    GL_4_4(44, "OpenGL 4.4"),
    GL_4_3(43, "OpenGL 4.3"),
    GL_4_2(42, "OpenGL 4.2"),
    GL_4_1(41, "OpenGL 4.1"),
    GL_4_0(40, "OpenGL 4.0"),
    GL_3_3(33, "OpenGL 3.3"),
    GL_3_2(32, "OpenGL 3.2");

    companion object {
        /**
         * MobileGlues 官方默认值。
         *
         * 上游插件 `customGLVersion` 的默认是 `Default(0)`，而 native 端把 `0`
         * 解析成 `DEFAULT_GL_VERSION = 40`，因此这里直接用 [GL_4_0]：
         * 行为与「没写过配置」完全一致，同时用户能在界面上看到真实生效的版本。
         */
        val default: MobileGluesGlVersion = GL_4_0

        /**
         * 把设置里保存的字符串解析成枚举。
         *
         * 兼容枚举名（`GL_4_0`，当前存储格式）、裸整数（`40`）与展示名（`OpenGL 4.0`）；
         * 认不出来时回退到 [default]，保证任何情况下都能给游戏一个合法值。
         */
        fun of(raw: String?): MobileGluesGlVersion {
            if (raw.isNullOrBlank()) return default
            entries.firstOrNull { it.name == raw }?.let { return it }
            raw.trim().toIntOrNull()?.let { wire -> ofWire(wire)?.let { return it } }
            entries.firstOrNull { it.displayName.equals(raw.trim(), ignoreCase = true) }?.let { return it }
            return default
        }

        /**
         * 整数 → 枚举，照抄 native 的夹取规则（`config/settings.cpp`）。
         *
         * 不能「不认识就当默认」：native 把 47 当 4.6 用、把 38 当 3.3 用，
         * 这里若读成默认值，界面显示的档位就会与游戏里实际生效的不一致。
         */
        fun ofWire(wire: Int?): MobileGluesGlVersion? {
            if (wire == null) return null
            entries.firstOrNull { it.wire == wire }?.let { return it }
            return when {
                wire > 46 -> GL_4_6
                //34..39：native 一律夹到 3.3
                wire in 34..39 -> GL_3_3
                //1..31：native 一律夹到 3.2
                wire in 1..31 -> GL_3_2
                //0 与负数都表示「没配过」，走默认
                else -> null
            }
        }
    }
}
