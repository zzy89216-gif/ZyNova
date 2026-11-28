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

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.movtery.zalithlauncher.context.GlobalContext
import com.movtery.zalithlauncher.utils.logging.Logger

private const val TAG = "RendererDeviceProfile"

/**
 * 「自动」渲染器用来判断**设备能不能跑某个渲染器**的能力快照。
 *
 * 只回答兼容性问题（OpenGL ES 版本 / Vulkan 支持），不回答「哪个更快」——
 * 排序仍然由 [AutoRenderer] 的偏好顺序负责。
 *
 * 几条约定：
 * - 结果只探测一次并缓存（渲染器选择很频繁，没必要每次启动都问系统）；
 * - **探测失败一律当作「未知」放行**：读不到 GLES 版本时不能把玩家挡在渲染器外面，
 *   宁可回到通用兜底，也不能因为一次系统 API 失败导致没有渲染器可选。
 */
object RendererDeviceProfile {
    /**
     * 设备声明的 OpenGL ES 版本，编码方式同 `ConfigurationInfo.reqGlEsVersion`
     * （例如 OpenGL ES 3.2 = `0x00030002`）；`0` 表示读取失败。
     */
    val glEsVersionCode: Int by lazy {
        runCatching {
            val activityManager = GlobalContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.deviceConfigurationInfo.reqGlEsVersion
        }.onFailure {
            Logger.warning(TAG, "Failed to read device OpenGL ES version", it)
        }.getOrDefault(0)
    }

    /**
     * 系统是否声明支持 Vulkan 硬件。
     *
     * Zink 系渲染器（Ironized Zink 等）底层走 Vulkan，没有这份能力就只能选别的。
     */
    val supportsVulkan: Boolean by lazy {
        runCatching {
            GlobalContext.packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_VERSION)
        }.onFailure {
            Logger.warning(TAG, "Failed to query Vulkan hardware feature", it)
        }.getOrDefault(false)
    }

    /**
     * 设备是否至少有指定版本的 OpenGL ES。
     *
     * @return 探测不到版本时返回 `true`（见类注释：未知不拦人）
     */
    fun isGlEsAtLeast(major: Int, minor: Int): Boolean {
        val code = glEsVersionCode
        if (code <= 0) return true
        return code >= (major shl 16) + minor
    }

    /** 设备主 ABI（部分渲染器只带 arm64 原生库，[RendererInterface.isAvailableOnDevice] 已经拦过一次） */
    val primaryAbi: String? = Build.SUPPORTED_ABIS.firstOrNull()

    /** 给日志 / 诊断用的一句话设备快照 */
    fun describe(): String {
        val gles = glEsVersionCode.takeIf { it > 0 }
            ?.let { "${it shr 16}.${it and 0xFFFF}" }
            ?: "unknown"
        return "GLES=$gles, Vulkan=$supportsVulkan, ABI=${primaryAbi ?: "unknown"}"
    }
}
