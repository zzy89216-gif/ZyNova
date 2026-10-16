/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
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

import com.movtery.zalithlauncher.game.renderer.renderers.GL4ESRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.IronizedZinkRenderer
import com.movtery.zalithlauncher.game.renderer.renderers.MobileGluesRenderer
import com.movtery.zalithlauncher.game.version.installed.utils.isBiggerVer
import com.movtery.zalithlauncher.game.version.installed.utils.isLowerVer
import com.movtery.zalithlauncher.utils.logging.Logger

private const val TAG = "Renderers"

/**
 * 启动器所有渲染器总管理者，启动器内置的渲染器与渲染器插件加载的渲染器，都会加载到这里
 *
 * 内置渲染器只保留三个：**MobileGlues（默认）**、Ironized Zink、GL4ES。
 *
 * ⚠️ **列表顺序有意义**：`setCurrentRenderer()` 在找不到已保存的渲染器时
 * （例如用户存过已被删除的渲染器 UUID）会回退到**列表首个**。
 * 因此列表首个必须是那个「万一出问题也最可能跑得起来」的渲染器 ——
 * MobileGlues 直接跑在设备自身的 OpenGL ES 上、不需要 Vulkan，所以放在首位。
 */
object Renderers {
    private val renderers: MutableList<RendererInterface> = mutableListOf()
    private var currentRenderer: RendererInterface? = null
    private var isInitialized: Boolean = false

    fun init(
        reset: Boolean = false
    ) {
        if (isInitialized && !reset) return
        isInitialized = true

        if (reset) {
            renderers.clear()
            currentRenderer = null
        }

        addRenderers(
            MobileGluesRenderer,
            IronizedZinkRenderer,
            GL4ESRenderer
        )
    }

    /**
     * 获取当前的渲染器列表
     */
    fun getRenderers(): List<RendererInterface> = renderers

    /**
     * 设置界面里供用户选择的渲染器列表：**「自动」在最前，其余内置渲染器依次在后**。
     *
     * 只用于界面展示。[getRenderers] 仍然只返回真正的渲染器
     * （所以「回退到列表首个」这条路径不会被「自动」污染）。
     */
    fun getRenderersForSelection(): List<RendererInterface> = listOf(AutoRenderer) + renderers

    /**
     * 挑选一个「在指定 Minecraft 版本上可用」的渲染器唯一标识符。
     *
     * 存在两层理由：
     *
     * 1. **用户显式选择了「自动」**（[AutoRenderer.IDENTIFIER]）：按
     *    [AutoRenderer] 的偏好顺序挑第一个支持该版本的渲染器；
     * 2. **用户选了某个具体渲染器，但它跑不了这个版本**：回退到列表里第一个支持该版本的，
     *    免得每次启动都弹一次「渲染器可能不兼容」的警告 —— 用户什么都没做错，
     *    却要为默认值的选择买单。
     *
     * ⚠️ 这里**只决定本次启动实际用哪个**，绝不写回用户设置 ——
     * 用户的选择原样保留，换个实例 / 换个版本仍然按他选的那个走。
     *
     * @param preferredIdentifier 用户实际选择（版本设置里的值，或全局设置的值）
     * @param mcVersion 目标实例的 Minecraft 版本
     */
    fun resolveIdentifier(preferredIdentifier: String, mcVersion: String?): String {
        if (preferredIdentifier == AutoRenderer.IDENTIFIER) {
            return AutoRenderer.pick(mcVersion)?.getUniqueIdentifier() ?: preferredIdentifier
        }
        return findCompatibleIdentifier(preferredIdentifier, mcVersion)
    }

    /**
     * 判断某个渲染器是否支持指定的 Minecraft 版本
     *
     * 与启动路径（`LauncherElements` 的 `isRendererUnsupported`）使用**同一套判据**，
     * 避免出现「这里认为支持、启动时又弹不支持」的不一致。
     * 没有声明 min / max 的渲染器视为不限制版本。
     */
    fun isCompatible(renderer: RendererInterface, mcVersion: String?): Boolean {
        if (mcVersion.isNullOrBlank()) return true
        val min = renderer.getMinMCVersion()
        if (min != null && mcVersion.isLowerVer(min)) return false
        val max = renderer.getMaxMCVersion()
        if (max != null && mcVersion.isBiggerVer(max)) return false
        return true
    }

    /**
     * 在**具体渲染器**之间做版本兼容回退（不处理「自动」，那由 [resolveIdentifier] 负责）。
     *
     * 选择顺序：
     * 1. [preferredIdentifier] 能跑就用它；
     * 2. 否则回退到列表里**第一个声明支持该版本**的渲染器
     *    （列表首个是 MobileGlues，但兼容性过滤后通常落到不限制版本的 Ironized Zink）；
     * 3. 都不满足时保留原值，交回启动路径去弹那个警告（不做静默的错事）。
     */
    fun findCompatibleIdentifier(preferredIdentifier: String, mcVersion: String?): String {
        val preferred = renderers.find { it.getUniqueIdentifier() == preferredIdentifier }
            ?: return preferredIdentifier
        if (isCompatible(preferred, mcVersion)) return preferredIdentifier

        val fallback = renderers.firstOrNull { isCompatible(it, mcVersion) }
        if (fallback != null) {
            Logger.info(
                TAG,
                "Renderer ${preferred.getRendererName()} does not support MC $mcVersion, " +
                        "using ${fallback.getRendererName()} for this launch"
            )
            return fallback.getUniqueIdentifier()
        }
        return preferredIdentifier
    }

    /**
     * 加入一些渲染器
     */
    fun addRenderers(vararg renderers: RendererInterface) {
        renderers.forEach { renderer ->
            addRenderer(renderer)
        }
    }

    /**
     * 加入单个渲染器
     */
    fun addRenderer(renderer: RendererInterface): Boolean {
        return if (renderers.any { it.getUniqueIdentifier() == renderer.getUniqueIdentifier() }) {
            Logger.warning(TAG, "The unique identifier of this renderer (${renderer.getRendererName()} - ${renderer.getUniqueIdentifier()}) conflicts with an already loaded renderer. " +
                    "Normally, this shouldn't happen. You deliberately caused this conflict, didn't you, user?")
            false
        } else {
            renderers.add(renderer)
            Logger.info(TAG, "Renderer loaded: ${renderer.getRendererName()} (${renderer.getRendererId()} - ${renderer.getUniqueIdentifier()})")
            true
        }
    }

    /**
     * 设置当前的渲染器
     * @param uniqueIdentifier 渲染器的唯一标识符，用于找到当前想要设置的渲染器
     * @param retryToFirstOnFailure 如果未找到匹配的渲染器，是否跳回渲染器列表的首个渲染器
     */
    fun setCurrentRenderer(uniqueIdentifier: String, retryToFirstOnFailure: Boolean = true) {
        if (!isInitialized) throw IllegalStateException("Uninitialized renderer!")
        currentRenderer = renderers.find { it.getUniqueIdentifier() == uniqueIdentifier } ?: run {
            if (retryToFirstOnFailure) {
                val renderer = renderers[0]
                Logger.warning(TAG, "Incompatible renderer $uniqueIdentifier will be replaced with ${renderer.getUniqueIdentifier()} (${renderer.getRendererName()})")
                renderer
            } else null
        }
    }

    /**
     * 获取当前的渲染器
     */
    fun getCurrentRenderer(): RendererInterface {
        if (!isInitialized) throw IllegalStateException("Uninitialized renderer!")
        return currentRenderer ?: throw IllegalStateException("Current renderer not set")
    }

    /**
     * 当前是否设置了渲染器
     */
    fun isCurrentRendererValid(): Boolean = isInitialized && currentRenderer != null
}