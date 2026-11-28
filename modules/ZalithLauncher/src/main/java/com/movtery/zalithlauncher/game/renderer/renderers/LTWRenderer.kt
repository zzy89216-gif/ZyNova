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

package com.movtery.zalithlauncher.game.renderer.renderers

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.GlobalContext
import com.movtery.zalithlauncher.game.renderer.RendererInterface
import com.movtery.zalithlauncher.setting.AllSettings

/**
 * LTW-Turbo（实验性）
 *
 * 上游：https://github.com/gaowan-u/LTW-Turbo
 * 许可：LGPL-3.0（随包的是**未经修改**的预编译 `libltw_turbo.so`，仅 arm64-v8a）
 *
 * LTW 通过 `eglGetProcAddress` 拦截 GL 调用，把桌面 OpenGL 翻译到宿主 GLES，
 * 并为 1.17 及更早版本补齐固定管线。它同时导出 EGL 与 GL 符号，
 * 因此 `getRendererLibrary()` 与 `getRendererEGL()` 都指向同一个库。
 *
 * ⚠️ **实验性 / 不推荐**：LTW-Turbo 仍处于早期开发阶段，项目状态自述为
 * 「非稳定正式版」，只建议愿意承担崩溃风险的用户手动选择；「自动」不会选它。
 */
object LTWRenderer : RendererInterface {
    /**
     * 随包只有 arm64-v8a 的 `libltw_turbo.so`。
     * 其他架构（armeabi-v7a / x86 / x86_64）上直接判定不可用，
     * 于是设置里的渲染器列表**不会出现 LTW**，而不是让用户选中后启动崩溃。
     */
    override fun isAvailableOnDevice(): Boolean =
        android.os.Build.SUPPORTED_ABIS.any { it.equals("arm64-v8a", ignoreCase = true) }

    override fun getRendererId(): String = "opengles3_ltw"

    override fun getUniqueIdentifier(): String = "b3f1c8e2-7a4d-4f9a-9c2e-1d5a6b8c0e71"

    override fun getRendererName(): String =
        GlobalContext.getString(R.string.settings_renderer_ltw_name)

    override fun getRendererSummary(): String =
        GlobalContext.getString(R.string.settings_renderer_ltw_summary)

    /** LTW 主要面向 1.17 及更早版本，但也能跑新版本；不设上限 */
    override fun getMinMCVersion(): String = "1.8"

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
        val env = linkedMapOf(
            "LIBGL_ES" to "3",
            "POJAV_RENDERER" to getRendererId(),
            // native 端 env_istrue_d 的默认值是 true，所以这里必须显式写 0/1
            "LTW_NEVER_FLUSH_BUFFERS" to if (AllSettings.ltwNeverFlushBuffers.state) "1" else "0",
            "LTW_COHERENT_DYNAMIC_STORAGE" to if (AllSettings.ltwCoherentDynamicStorage.state) "1" else "0",
        )
        if (AllSettings.ltwHideBufferStorage.state) env["LTW_HIDE_BUFFER_STORAGE"] = "1"
        if (AllSettings.ltwNoError.state) env["LIBGL_NOERROR"] = "1"
        if (AllSettings.ltwShaderConvCheckNan.state) env["LTW_SHADERCONV_CHECKNAN"] = "1"
        if (AllSettings.ltwDebug.state) env["LTW_DEBUG"] = "1"
        if (AllSettings.ltwGlErrorTrace.state) env["LTW_GLERR_TRACE"] = "1"
        env
    }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = "libltw_turbo.so"

    override fun getRendererEGL(): String = getRendererLibrary()
}
