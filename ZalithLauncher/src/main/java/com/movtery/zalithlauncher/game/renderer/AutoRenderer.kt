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
import com.movtery.zalithlauncher.utils.logging.Logger

private const val TAG = "AutoRenderer"

/**
 * 「自动」渲染器选项。
 *
 * 它**不是**一个真正的渲染器 —— 不会创建 GL 上下文，也不会被 `setCurrentRenderer()` 采用。
 * 它只是渲染器列表里的一个**选择**：选了它就表示「每次启动自己挑一个合适的」。
 * 真正做事的是 [Renderers.resolveIdentifier]，本对象只负责在设置界面里显示。
 *
 * 为什么需要它：内置渲染器的**版本覆盖范围**和**设备要求**都不同
 * （MobileGlues 要求 GLES 3.2+ 且只支持 1.17+；GL4ES 只到 1.21.4；
 * Ironized Zink 不限版本但必须有 Vulkan）。若只能手动指定一个，
 * 「默认值」必然对一部分设备 / 版本是错的 —— 用户什么都没做错，
 * 却要每次启动先吃一次「渲染器可能不兼容」的警告，或者直接黑屏。
 * 自动模式把这个选择交给启动器。
 */
object AutoRenderer : RendererInterface {
    /** 存进用户配置的标识符。改动会让老用户的选择失效，**不要改** */
    const val IDENTIFIER = "auto"

    /**
     * 自动模式的一个候选：标识符 + 「这台设备跑不跑得动」的判断。
     *
     * `deviceSupports` 只回答兼容性问题，**不判断性能**；
     * 真正的排序由列表顺序决定。
     */
    private data class AutoChoice(
        val identifier: String,
        val deviceSupports: () -> Boolean,
    )

    /**
     * 自动模式的偏好顺序：**从上到下，第一个「设备跑得动 + 版本也兼容」的会被采用**。
     *
     * 顺序理由：
     * 1. MobileGlues —— 直接跑在设备自身的 OpenGL ES 上，不需要 Vulkan，开箱可用性最好；
     *    但它要求 GLES 3.2+，低端设备选了也起不来，所以加设备能力门槛；
     * 2. GL4ES —— 老版本（≤ 1.21.4）的稳定兜底，GLES 2.0 设备就能跑；
     * 3. Ironized Zink —— 需要 Vulkan，且比前两个重；放在 GL4ES 之后，
     *    只在 GL4ES 覆盖不到的版本（1.21.5+）或前面都跑不动时才轮到它。
     *
     * ⚠️ 这个顺序会实际影响游戏表现，调整前请想清楚；
     * 以后新增内置渲染器时，也要考虑是否加进这里并补上设备门槛。
     */
    private val AUTO_PREFERENCE = listOf(
        AutoChoice(MOBILE_GLUES_UNIQUE_IDENTIFIER) {
            RendererDeviceProfile.isGlEsAtLeast(3, 2)
        },
        AutoChoice(GL4ESRenderer.getUniqueIdentifier()) { true },
        AutoChoice(IRONIZED_ZINK_UNIQUE_IDENTIFIER) {
            RendererDeviceProfile.supportsVulkan
        },
    )

    /**
     * 按「设备能力 → 偏好顺序 → 版本兼容」挑一个渲染器。
     *
     * 1. 先按 [AUTO_PREFERENCE] 顺序找第一个**设备跑得动且声明支持 [mcVersion]** 的；
     * 2. 一个都没有时，退回「版本兼容的第一个内置渲染器」，并记一条警告 ——
     *    设备能力探测可能不准（读不到 GLES 版本、系统 feature 缺失），
     *    不能因为一次探测失败就让玩家没有渲染器可选。
     *
     * @return `null` 表示渲染器列表为空，或没有任何一个在版本上兼容。
     */
    fun pick(mcVersion: String?): RendererInterface? {
        val available = Renderers.getRenderers()

        AUTO_PREFERENCE.forEach { choice ->
            val renderer = available.find { it.getUniqueIdentifier() == choice.identifier } ?: return@forEach

            if (!choice.deviceSupports()) {
                Logger.info(TAG, "Auto: skip ${renderer.getRendererName()} (device cannot run it), MC=$mcVersion")
                return@forEach
            }
            if (!Renderers.isCompatible(renderer, mcVersion)) {
                Logger.info(TAG, "Auto: skip ${renderer.getRendererName()} (MC $mcVersion not supported)")
                return@forEach
            }

            Logger.info(
                TAG,
                "Auto renderer picked ${renderer.getRendererName()} for MC $mcVersion (${RendererDeviceProfile.describe()})"
            )
            return renderer
        }

        //兜底：设备能力探测失败 / 渲染器列表被插件改过时，至少保证「版本兼容」这一条
        val fallback = available.firstOrNull { Renderers.isCompatible(it, mcVersion) }
        if (fallback != null) {
            Logger.warning(
                TAG,
                "Auto: no device-capable renderer for MC $mcVersion, " +
                        "falling back to ${fallback.getRendererName()} (${RendererDeviceProfile.describe()})"
            )
        } else {
            Logger.warning(
                TAG,
                "Auto: no renderer compatible with MC $mcVersion " +
                        "(${available.joinToString { it.getRendererName() }})"
            )
        }
        return fallback
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
