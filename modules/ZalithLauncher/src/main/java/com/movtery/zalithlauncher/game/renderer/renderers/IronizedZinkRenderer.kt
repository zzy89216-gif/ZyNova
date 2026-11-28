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

import com.movtery.zalithlauncher.game.renderer.RendererInterface
import com.movtery.zalithlauncher.game.renderer.ironizedzink.IRONIZED_ZINK_UNIQUE_IDENTIFIER
import com.movtery.zalithlauncher.game.renderer.ironizedzink.Zink
import com.movtery.zalithlauncher.game.renderer.ironizedzink.buildIronizedZinkEnv
import com.movtery.zalithlauncher.game.renderer.ironizedzink.ironizedZinkOptions
import com.movtery.zalithlauncher.setting.AllSettings

/**
 * Ironized Zink
 *
 * 桌面 OpenGL 4.6（Mesa Zink + Kopper）经 Vulkan 转译，由 GoyDevv 打包与配置。
 * 上游：https://github.com/GoyDevv/IronizedZink （GPL-3.0）
 *
 * 渲染引擎为 Mesa 23.0.4 的 Zink Gallium 驱动与 Kopper 窗口系统层，
 * 版权归 Mesa 作者所有，MIT 许可（GLX 部分为 SGI Free Software License B，
 * GL 头文件为 Khronos 许可）；完整文本见 assets/licenses/mesa-licenses.rst。
 *
 * 与「插件版 Ironized Zink」的差异：不再通过 `libironized_zink.so` 读取
 * `/sdcard/IronizedZink/ironized.env`，而是由启动器直接把同一组环境变量
 * 注入游戏进程（见 [getRendererEnv]），因此不需要任何存储权限。
 */
object IronizedZinkRenderer : RendererInterface {
    override fun getRendererId(): String = Zink.RENDERER_ID

    override fun getUniqueIdentifier(): String = IRONIZED_ZINK_UNIQUE_IDENTIFIER

    override fun getRendererName(): String = "Ironized Zink"

    /**
     * 渲染器列表里直接显示当前生效的官方预设，选完就能看到结果
     */
    override fun getRendererSummary(): String? =
        AllSettings.ironizedZinkPreset.state.displayName

    /**
     * 完整注入 Ironized Zink 的原生配置（含 4 个官方预设与全部可调参数）
     *
     * 注意：这里返回的映射是完整的，`GameLauncher.setRendererEnv` 不会再对
     * 本渲染器追加任何通用兜底值，因此用户选的 OpenGL 版本等一定会生效。
     */
    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
        buildIronizedZinkEnv(AllSettings.ironizedZinkOptions)
    }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = Zink.GL_NAME

    override fun getRendererEGL(): String = Zink.EGL_NAME

    /*
     * 刻意不覆写 getMinMCVersion() / getMaxMCVersion()：
     * 上游 Ironized Zink 的渲染器插件 manifest 没有声明 minMCVer / maxMCVer，
     * 内置后保持同样的「不限制版本」行为，免得在新版本 Minecraft 上
     * 弹出无谓的「渲染器可能不兼容」提示。
     */
}
