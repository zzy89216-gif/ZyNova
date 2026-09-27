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

package com.movtery.zalithlauncher.game.renderer.renderers

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.GlobalContext
import com.movtery.zalithlauncher.game.renderer.RendererInterface

/**
 * MobileGlues
 *
 * 「(on) Mobile, GL uses ES」——把桌面 OpenGL 转译到宿主 OpenGL ES 3.x 的实现。
 * 上游：https://github.com/MobileGL-Dev/MobileGlues
 * 许可：GNU LGPL-2.1（详见仓库根目录 THIRD_PARTY_NOTICES.md 与 assets/licenses/）
 *
 * 保持上游默认配置：环境变量与上游渲染器插件 manifest 中声明的 `pojavEnv` 一致，
 * 不额外开启任何可调项。
 */
object MobileGluesRenderer : RendererInterface {
    override fun getRendererId(): String = "opengles3"

    override fun getUniqueIdentifier(): String = "c0c6a2f6-3a5b-4c1c-9e6f-5b3b9e37d531"

    override fun getRendererName(): String = "MobileGlues"

    override fun getRendererSummary(): String =
        GlobalContext.getString(R.string.settings_renderer_mobileglues_summary)

    /**
     * 与上游渲染器插件 manifest 声明的 `minMCVer` 保持一致（1.17）
     */
    override fun getMinMCVersion(): String = "1.17"

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
        mapOf(
            "LIBGL_ES" to "3",
            "POJAV_RENDERER" to getRendererId(),
            "POJAVEXEC_EGL" to getRendererLibrary(),
            "LIBGL_EGL" to getRendererLibrary(),
            "MG_COUNT_LAUNCH" to "1"
        )
    }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = "libmobileglues.so"

    /** MobileGlues 自带 EGL 实现（与上游渲染器插件声明的 `POJAVEXEC_EGL` 一致） */
    override fun getRendererEGL(): String = getRendererLibrary()
}
