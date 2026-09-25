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

package com.movtery.zalithlauncher.utils.device

import androidx.annotation.Keep
import com.movtery.zalithlauncher.utils.logging.Logger
import org.apache.commons.io.FileUtils
import java.io.File

private const val TAG = "VulkanCapabilities"

/**
 * 设备实际枚举出来的 Vulkan 能力
 *
 * 注意：构造器签名由 native 层反射调用，不能修改参数列表。
 */
@Keep
data class VulkanCapabilities(
    val apiVersionMajor: Int,
    val apiVersionMinor: Int,
    val apiVersionPatch: Int,
    val extensions: List<String>,
    val features: Map<String, Boolean>
) {
    /** Vulkan 版本字符串 */
    val versionString: String
        get() = "$apiVersionMajor.$apiVersionMinor.$apiVersionPatch"

    /**
     * 在指定要求档案下，Vulkan 版本是否达标
     */
    fun isVersionSupportedFor(requirement: VulkanRequirement): Boolean =
        apiVersionMajor > requirement.minApiMajor ||
            (apiVersionMajor == requirement.minApiMajor && apiVersionMinor >= requirement.minApiMinor)

    /**
     * 在指定要求档案下缺失的必要扩展
     */
    fun missingExtensionsFor(requirement: VulkanRequirement): List<String> =
        requirement.requiredExtensions.filter { it !in extensions }

    /**
     * 在指定要求档案下缺失的必要功能
     */
    fun missingFeaturesFor(requirement: VulkanRequirement): List<String> =
        requirement.requiredFeatures.filter { features[it] != true }

    /**
     * 是否满足指定要求档案
     */
    fun isAllSupportedFor(requirement: VulkanRequirement): Boolean =
        isVersionSupportedFor(requirement) &&
            missingExtensionsFor(requirement).isEmpty() &&
            missingFeaturesFor(requirement).isEmpty()

    // ===== 以下为使用当前要求档案的便捷属性 =====

    /** 检查 Vulkan 版本是否满足当前要求 */
    val isVersionSupported: Boolean
        get() = isVersionSupportedFor(VulkanRequirements.CURRENT)

    /** 返回设备缺失的必要扩展 */
    val missingExtensions: List<String>
        get() = missingExtensionsFor(VulkanRequirements.CURRENT)

    /** 返回设备不支持的必要功能 */
    val missingFeatures: List<String>
        get() = missingFeaturesFor(VulkanRequirements.CURRENT)

    /** 设备是否满足当前所有要求 */
    val isAllSupported: Boolean
        get() = isAllSupportedFor(VulkanRequirements.CURRENT)

    companion object {
        /** 当前要求的必要扩展 */
        val REQUIRED_EXTENSIONS: List<String>
            get() = VulkanRequirements.CURRENT.requiredExtensions

        /** 当前要求的必要功能 */
        val REQUIRED_FEATURES: List<String>
            get() = VulkanRequirements.CURRENT.requiredFeatures
    }
}

@Keep
fun interface VulkanLogCallback {
    fun log(level: String, message: String)
}

/**
 * Vulkan 检测器
 *
 * 检测结果明确区分「可用 / 不可用 / 检测失败」三种状态，
 * 并且给出失败的具体原因，而不是笼统地返回"不支持"。
 */
@Keep
object VulkanChecker {
    init {
        try {
            System.loadLibrary("vulkan_check")
            nativeSetLogCallback { level, msg ->
                when (level) {
                    "INFO" -> Logger.info(TAG, msg)
                    "WARN" -> Logger.warning(TAG, msg)
                    "ERROR" -> Logger.error(TAG, msg)
                    else -> Logger.debug(TAG, msg)
                }
            }
        } catch (e: UnsatisfiedLinkError) {
            Logger.error(TAG, "Failed to load vulkan_check library", e)
        }
    }

    /**
     * 检测设备的 Vulkan 能力
     *
     * @param driverPath 自定义驱动路径
     * @param nativeDir 自定义驱动所在目录（为空时使用系统 Vulkan 驱动）
     * @param cacheDir 驱动解压等操作使用的临时目录
     */
    fun checkCapabilities(
        driverPath: String?,
        nativeDir: String?,
        cacheDir: String?
    ): VulkanCheckResult {
        val requirement = VulkanRequirements.CURRENT
        val customDriver = !nativeDir.isNullOrBlank()
        val useTurnip = customDriver
        val deviceInfo = VulkanDeviceInfo.collect(nativeDir)

        return try {
            val caps = nativeCheckVulkan(
                driverPath = driverPath,
                nativeDir = nativeDir,
                cacheDir = cacheDir,
            )

            if (caps == null) {
                //检测顺利完成，但没能拿到可用的 Vulkan 设备
                Logger.warning(TAG, "No usable Vulkan device found (custom driver: $customDriver)")
                VulkanCheckResult.Unavailable(
                    capabilities = null,
                    issues = listOf(
                        if (customDriver) VulkanIssue.DRIVER_ABNORMAL else VulkanIssue.NO_VULKAN_DEVICE
                    ),
                    missingExtensions = requirement.requiredExtensions,
                    missingFeatures = requirement.requiredFeatures,
                    requiredApiVersion = requirement.minApiVersionString,
                    deviceInfo = deviceInfo,
                    useTurnip = useTurnip
                )
            } else {
                logCapabilities(caps, requirement)

                val issues = buildList {
                    if (!caps.isVersionSupportedFor(requirement)) add(VulkanIssue.API_VERSION_TOO_LOW)
                    if (caps.missingExtensionsFor(requirement).isNotEmpty()) add(VulkanIssue.MISSING_EXTENSIONS)
                    if (caps.missingFeaturesFor(requirement).isNotEmpty()) add(VulkanIssue.MISSING_FEATURES)
                }

                if (issues.isEmpty()) {
                    VulkanCheckResult.Available(
                        capabilities = caps,
                        deviceInfo = deviceInfo,
                        useTurnip = useTurnip
                    )
                } else {
                    VulkanCheckResult.Unavailable(
                        capabilities = caps,
                        issues = issues,
                        missingExtensions = caps.missingExtensionsFor(requirement),
                        missingFeatures = caps.missingFeaturesFor(requirement),
                        requiredApiVersion = requirement.minApiVersionString,
                        deviceInfo = deviceInfo,
                        useTurnip = useTurnip
                    )
                }
            }
        } catch (e: UnsatisfiedLinkError) {
            //检测库本身不可用：无法得出任何结论
            Logger.error(TAG, "Native library or method not found", e)
            VulkanCheckResult.Failed(
                message = e.message ?: e::class.qualifiedName,
                deviceInfo = deviceInfo,
                useTurnip = useTurnip
            )
        } catch (e: Exception) {
            Logger.error(TAG, "Native check failed", e)
            VulkanCheckResult.Failed(
                message = e.message ?: e::class.qualifiedName,
                deviceInfo = deviceInfo,
                useTurnip = useTurnip
            )
        } finally {
            if (nativeDir != null && cacheDir != null) {
                FileUtils.deleteQuietly(File(cacheDir))
            }
        }
    }

    /**
     * 输出检测日志，便于排查设备兼容问题
     */
    private fun logCapabilities(caps: VulkanCapabilities, requirement: VulkanRequirement) {
        Logger.info(TAG, "Target Minecraft: ${requirement.minecraftVersion}")
        Logger.info(TAG, "Vulkan version: ${caps.versionString}")
        Logger.info(TAG, "Version >= ${requirement.minApiVersionString}: ${caps.isVersionSupportedFor(requirement)}")
        if (caps.missingExtensionsFor(requirement).isNotEmpty()) {
            Logger.warning(TAG, "Missing required extensions: ${caps.missingExtensionsFor(requirement)}")
        }
        if (caps.missingFeaturesFor(requirement).isNotEmpty()) {
            Logger.warning(TAG, "Missing required features: ${caps.missingFeaturesFor(requirement)}")
        }
        Logger.info(TAG, "All requirements satisfied: ${caps.isAllSupportedFor(requirement)}")
    }

    @Keep
    @JvmStatic
    private external fun nativeSetLogCallback(callback: VulkanLogCallback)

    @Keep
    @JvmStatic
    private external fun nativeCheckVulkan(
        driverPath: String?,
        nativeDir: String?,
        cacheDir: String?
    ): VulkanCapabilities?
}
