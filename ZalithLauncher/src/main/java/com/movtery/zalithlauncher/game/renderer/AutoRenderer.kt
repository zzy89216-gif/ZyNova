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

package com.movtery.zalithlauncher.game.renderer

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.GlobalContext
import com.movtery.zalithlauncher.game.renderer.ironizedzink.IRONIZED_ZINK_UNIQUE_IDENTIFIER
import com.movtery.zalithlauncher.game.renderer.mobileglues.MOBILE_GLUES_UNIQUE_IDENTIFIER
import com.movtery.zalithlauncher.game.renderer.renderers.GL4ESRenderer

/**
 * 「自动」渲染器选项。
 *
 * 它**不是**一个真正的渲染器 —— 不会创建 GL 上下文，也不会被 `setCurrentRenderer()` 采用。
 * 它只是渲染器列表里的一个**选择**：选了它就表示「每次启动自己挑一个合适的」。
 * 真正做事的是 [Renderers.resolveIdentifier]，本对象只负责在设置界面里显示。
 *
 * 为什么需要它：内置的三个渲染器并不同时支持所有 Minecraft 版本
 * （MobileGlues 声明 `minMCVer = 1.17`；只有 Ironized Zink 不限制版本）。
 * 若只能手动指定一个，「默认值」必然对一部分版本是错的 ——
 * 旧版本实例每次启动都要先弹一次「渲染器可能不兼容」的警告。
 * 自动模式把这个选择交给启动器。
 */
object AutoRenderer : RendererInterface {
    /** 存进用户配置的标识符。改动会让老用户的选择失效，**不要改** */
    const val IDENTIFIER = "auto"

    /**
     * 自动模式的偏好顺序：**从上到下第一个支持该 Minecraft 版本的**会被采用。
     *
     * 顺序理由：
     * 1. MobileGlues —— 直接跑在设备自身的 OpenGL ES 上，不需要 Vulkan，开箱可用性最好；
     * 2. Ironized Zink —— 不限制 Minecraft 版本，是三个里唯一覆盖老版本（1.16 及以前）的；
     * 3. GL4ES —— 最后的兜底。
     *
     * ⚠️ 这个顺序会实际影响游戏表现，调整前请想清楚；
     * 以后新增内置渲染器时，也要考虑是否加进这里。
     */
    private val AUTO_PREFERENCE = listOf(
        MOBILE_GLUES_UNIQUE_IDENTIFIER,
        IRONIZED_ZINK_UNIQUE_IDENTIFIER,
        GL4ESRenderer.getUniqueIdentifier()
    )

    /**
     * 按偏好顺序找出第一个「声明支持 [mcVersion]」的渲染器。
     *
     * 只依据渲染器自己声明的版本区间判断，不做任何设备 / GPU 探测 ——
     * 自动模式的作用是「不选错」，不是「选出最快的」。
     * 返回 `null` 表示三个都不支持该版本（实际上不会发生，因为末位不限制版本）。
     */
    fun pick(mcVersion: String?): RendererInterface? {
        val available = Renderers.getRenderers()
        return AUTO_PREFERENCE
            .mapNotNull { id -> available.find { it.getUniqueIdentifier() == id } }
            .firstOrNull { Renderers.isCompatible(it, mcVersion) }
    }

    override fun getRendererId(): String = IDENTIFIER

    override fun getUniqueIdentifier(): String = IDENTIFIER

    override fun getRendererName(): String =
        GlobalContext.getString(R.string.settings_renderer_auto_title)

    override fun getRendererSummary(): String =
        GlobalContext.getString(R.string.settings_renderer_auto_summary)

    /**
     * 刻意**不**覆写 `getMinMCVersion()` / `getMaxMCVersion()`：
     * 「自动」对所有版本都成立，如果声明了区间，渲染器列表里会显示一个
     * 对自动模式毫无意义的「>= x」，也会被启动路径的兼容性检查误判。
     */

    /** 自动模式从不直接启动，下面的实现只是为了满足接口 */
    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy { emptyMap() }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String = ""
}
