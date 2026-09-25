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

package com.movtery.zalithlauncher.utils.device

import android.os.Build
import androidx.annotation.StringRes
import com.movtery.zalithlauncher.R

/**
 * Vulkan 检测问题的具体原因
 */
enum class VulkanIssue(
    @field:StringRes
    val textRes: Int
) {
    /** 设备没有可用的 Vulkan 驱动 */
    NO_VULKAN_DEVICE(R.string.game_vulkan_issue_no_device),

    /** Vulkan API 版本低于要求 */
    API_VERSION_TOO_LOW(R.string.game_vulkan_issue_api_version),

    /** 缺少必要的 Vulkan 扩展 */
    MISSING_EXTENSIONS(R.string.game_vulkan_issue_extensions),

    /** 缺少必要的 Vulkan 功能 */
    MISSING_FEATURES(R.string.game_vulkan_issue_features),

    /** Vulkan 驱动异常 */
    DRIVER_ABNORMAL(R.string.game_vulkan_issue_driver),

    /** 检测过程本身失败 */
    CHECK_FAILED(R.string.game_vulkan_issue_check_failed)
}

/**
 * GPU / 渲染器信息
 *
 * @param gpuRenderer GPU 渲染器标识
 * @param deviceModel 设备型号
 * @param driverPath 使用的自定义 Vulkan 驱动路径；使用系统驱动时为空
 */
data class VulkanDeviceInfo(
    val gpuRenderer: String,
    val deviceModel: String,
    val driverPath: String?
) {
    companion object {
        /**
         * 收集当前设备的 GPU / 渲染器信息
         */
        fun collect(driverPath: String?): VulkanDeviceInfo {
            val renderer = listOfNotNull(
                Build.HARDWARE?.takeIf { it.isNotBlank() },
                Build.BOARD?.takeIf { it.isNotBlank() }
            ).distinct().joinToString(" / ").ifBlank { "Unknown GPU" }

            val model = listOfNotNull(
                Build.MANUFACTURER?.takeIf { it.isNotBlank() },
                Build.MODEL?.takeIf { it.isNotBlank() }
            ).joinToString(" ").ifBlank { "Unknown Device" }

            return VulkanDeviceInfo(
                gpuRenderer = renderer,
                deviceModel = model,
                driverPath = driverPath?.takeIf { it.isNotBlank() }
            )
        }
    }
}

/**
 * Vulkan 检测结果
 *
 * 明确区分三种状态：
 * - [Available] 可用
 * - [Unavailable] 不可用（检测成功完成，但设备能力不满足要求）
 * - [Failed] 检测失败（无法完成检测）
 */
sealed interface VulkanCheckResult {
    /** 本次检测收集到的设备信息 */
    val deviceInfo: VulkanDeviceInfo

    /** 是否使用了自定义（Turnip）驱动 */
    val useTurnip: Boolean

    /**
     * 可用：设备实际具备 Minecraft 所需的 Vulkan 能力
     */
    data class Available(
        val capabilities: VulkanCapabilities,
        override val deviceInfo: VulkanDeviceInfo,
        override val useTurnip: Boolean
    ) : VulkanCheckResult

    /**
     * 不可用：检测本身成功完成，但设备不满足要求
     *
     * @param capabilities 检测到的能力；设备完全无 Vulkan 支持时为 null
     * @param issues 具体问题列表
     * @param missingExtensions 缺失的必要扩展
     * @param missingFeatures 缺失的必要功能
     * @param requiredApiVersion 要求的最低 Vulkan API 版本
     */
    data class Unavailable(
        val capabilities: VulkanCapabilities?,
        val issues: List<VulkanIssue>,
        val missingExtensions: List<String>,
        val missingFeatures: List<String>,
        val requiredApiVersion: String,
        override val deviceInfo: VulkanDeviceInfo,
        override val useTurnip: Boolean
    ) : VulkanCheckResult

    /**
     * 检测失败：无法完成检测（例如检测库不可用）
     *
     * 与 [Unavailable] 的区别在于：这里无法得出"设备是否支持"的结论。
     */
    data class Failed(
        val message: String?,
        override val deviceInfo: VulkanDeviceInfo,
        override val useTurnip: Boolean
    ) : VulkanCheckResult
}
