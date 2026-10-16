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
import com.movtery.zalithlauncher.game.renderer.mobileglues.MOBILE_GLUES_UNIQUE_IDENTIFIER
import com.movtery.zalithlauncher.game.renderer.mobileglues.MobileGluesConfig
import com.movtery.zalithlauncher.game.renderer.mobileglues.MobileGluesGlVersion
import com.movtery.zalithlauncher.setting.AllSettings

/**
 * MobileGlues
 *
 * 「(on) Mobile, GL uses ES」——把桌面 OpenGL 转译到宿主 OpenGL ES 3.x 的实现。
 * 上游：https://github.com/MobileGL-Dev/MobileGlues
 * 许可：GNU LGPL-2.1（详见仓库根目录 THIRD_PARTY_NOTICES.md 与 assets/licenses/）
 *
 * 其余配置保持上游默认：环境变量与上游渲染器插件 manifest 中声明的 `pojavEnv` 一致，
 * 只额外接入用户可选的 OpenGL 版本（见 [getRendererEnv]）。
 */
object MobileGluesRenderer : RendererInterface {
    override fun getRendererId(): String = "opengles3"

    override fun getUniqueIdentifier(): String = MOBILE_GLUES_UNIQUE_IDENTIFIER

    override fun getRendererName(): String = "MobileGlues"

    /**
     * 渲染器列表里的说明。
     *
     * 前半句是渲染器本身的定位，后半句显示当前生效的 OpenGL 版本
     * （与 Ironized Zink 显示预设名是同一个思路：选完就能在列表里看到结果）。
     *
     * `GlobalContext` 缓存的是 application context，不受应用内语言切换影响，
     * 因此在渲染器对象里取字符串资源是安全的。
     */
    override fun getRendererSummary(): String {
        val description = GlobalContext.getString(R.string.settings_renderer_mobileglues_summary)
        val glVersion = MobileGluesGlVersion.of(AllSettings.mobileGluesGlVersion.state).displayName
        return "$description · $glVersion"
    }

    /**
     * 与上游渲染器插件 manifest 声明的 `minMCVer` 保持一致（1.17）
     */
    override fun getMinMCVersion(): String = "1.17"

    /**
     * MobileGlues 的环境变量。
     *
     * ⚠️ 这里**不能**像 Ironized Zink 那样用 `MESA_GL_VERSION_OVERRIDE` 表达 OpenGL 版本：
     * MobileGlues 不读那个变量（它的源码里只认自己的 `config.json`）。
     * 因此 OpenGL 版本改为在启动前写进 config.json，
     * 并用 [MobileGluesConfig.ENV_DIR_PATH] 把该目录告诉 native 端。
     */
    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
        //写配置放在这里：这个方法只在真正启动游戏时求值，
        //因此每次启动都会用用户当前选择的版本刷新一次 config.json
        val glVersion = MobileGluesGlVersion.of(AllSettings.mobileGluesGlVersion.state)
        MobileGluesConfig.writeGlVersion(glVersion)

        linkedMapOf(
            "LIBGL_ES" to "3",
            "POJAV_RENDERER" to getRendererId(),
            "POJAVEXEC_EGL" to getRendererLibrary(),
            "LIBGL_EGL" to getRendererLibrary(),
            "MG_COUNT_LAUNCH" to "1",
            //让 MobileGlues 从启动器自己的目录读 config.json（含上面写入的 customGLVersion）
            MobileGluesConfig.ENV_DIR_PATH to MobileGluesConfig.directory.absolutePath
        )
    }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = "libmobileglues.so"

    /** MobileGlues 自带 EGL 实现（与上游渲染器插件声明的 `POJAVEXEC_EGL` 一致） */
    override fun getRendererEGL(): String = getRendererLibrary()
}
