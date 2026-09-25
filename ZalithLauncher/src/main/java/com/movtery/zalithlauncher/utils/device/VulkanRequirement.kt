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

/**
 * Minecraft 对 Vulkan 的能力要求档案
 *
 * 把「Minecraft 需要什么」与「怎样检测」分开：
 * Minecraft 更新渲染要求时，只需要新增或调整档案，
 * 不需要改动 Vulkan 检测逻辑本身。
 *
 * @param minecraftVersion 该要求对应的 Minecraft 版本标识
 * @param minApiMajor 最低 Vulkan API 主版本
 * @param minApiMinor 最低 Vulkan API 次版本
 * @param requiredExtensions 必要的 Vulkan 扩展
 * @param requiredFeatures 必要的 Vulkan 功能
 */
data class VulkanRequirement(
    val minecraftVersion: String,
    val minApiMajor: Int,
    val minApiMinor: Int,
    val requiredExtensions: List<String>,
    val requiredFeatures: List<String>
) {
    /** 最低 API 版本字符串，例如 1.2 */
    val minApiVersionString: String get() = "$minApiMajor.$minApiMinor"
}

/**
 * Minecraft 各版本的 Vulkan 要求
 *
 * 判断依据始终是设备**实际枚举出来的** Vulkan 能力，
 * 而不是设备在 PackageManager 中声明的 Vulkan 支持情况。
 */
object VulkanRequirements {
    /**
     * Minecraft 26.4 Snapshot 1 的 Vulkan 要求
     */
    val MC_26_4_SNAPSHOT_1 = VulkanRequirement(
        minecraftVersion = "26.4 Snapshot 1",
        minApiMajor = 1,
        minApiMinor = 2,
        requiredExtensions = listOf(
            "VK_KHR_dynamic_rendering",
            "VK_KHR_push_descriptor",
            "VK_KHR_synchronization2",
            "VK_EXT_vertex_attribute_divisor",
            "VK_KHR_swapchain"
        ),
        requiredFeatures = listOf(
            "multiDrawIndirect",
            "fillModeNonSolid",
            "samplerAnisotropy",
            "shaderDrawParameters",
            "timelineSemaphore",
            "hostQueryReset",
            "synchronization2",
            "dynamicRendering",
            "vertexAttributeInstanceRateDivisor"
        )
    )

    /**
     * 当前 Minecraft 的 Vulkan 要求
     */
    val CURRENT: VulkanRequirement get() = MC_26_4_SNAPSHOT_1
}
