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

package com.movtery.zalithlauncher.game.download.resources

import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchFilter
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchResult
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.utils.logging.Logger

private const val TAG = "ResourceManager"

/**
 * ZyNova 资源管理核心（Resource Management Core）
 *
 * 统一处理 Mod、资源包、光影、存档等 Minecraft 资源，
 * 并把它们收敛到同一条流程：
 *
 * 搜索 → 资源详情 → 版本匹配 → 文件选择 → 下载 → 校验 → 安装
 *
 * 上层（UI）只依赖本核心与 [ResourceProvider] 接口，
 * 不再各自实现下载和安装逻辑，也不直接接触具体资源来源。
 */
object ResourceManager {

    /**
     * 搜索资源（统一入口）
     */
    suspend fun search(
        platform: Platform,
        query: String,
        type: ResourceType,
        filter: PlatformSearchFilter = PlatformSearchFilter()
    ): PlatformSearchResult {
        return ResourceProviders.of(platform).search(query, type, filter)
    }

    /**
     * 获取资源详情
     */
    suspend fun resource(
        platform: Platform,
        projectId: String,
        type: ResourceType
    ): Resource {
        return ResourceProviders.of(platform).getResource(projectId, type)
    }

    /**
     * 获取资源的所有可用版本
     */
    suspend fun versions(
        platform: Platform,
        projectId: String,
        type: ResourceType
    ): List<ResourceVersion> {
        return ResourceProviders.of(platform).getVersions(projectId, type)
    }

    /**
     * 版本匹配 + 文件选择
     *
     * 自动挑选与目标实例的游戏版本、加载器都兼容的最新版本，
     * 并要求该版本具备可安装文件。
     *
     * @throws ResourceMatchException 匹配失败时抛出，携带具体原因（实例信息不可用 / 查询失败 / 无兼容版本）
     */
    suspend fun matchVersion(
        platform: Platform,
        projectId: String,
        type: ResourceType,
        instance: Version
    ): ResourceVersion {
        return ResourceInstallManager.resolveCompatibleVersion(
            provider = ResourceProviders.of(platform),
            projectId = projectId,
            type = type,
            instance = instance
        )
    }

    /**
     * 安装一个已确定的资源版本（下载 → 校验 → 安装）
     *
     * @return 实际安装的文件数量
     */
    suspend fun installVersion(
        version: ResourceVersion,
        type: ResourceType,
        instance: Version,
        onProgress: (ResourceDownloadProgress) -> Unit = {}
    ): Int {
        val plan = ResourceInstallManager.buildInstallPlan(
            version = version,
            type = type,
            instance = instance
        )

        //前置依赖缺失不会让安装失败，但必须留下痕迹，
        //否则用户只会看到「装好了」，进游戏却因为缺少前置而崩溃
        if (plan.hasUnresolvedRequiredDependencies) {
            Logger.warning(
                TAG,
                "Installed ${version.projectId} but ${plan.unresolvedRequiredDependencies.size} " +
                        "required dependencies could not be resolved: " +
                        plan.unresolvedRequiredDependencies.joinToString { "${it.provider}:${it.projectId}" }
            )
        }

        return ResourceInstallManager.install(
            plan = plan.entries,
            type = type,
            instance = instance,
            onProgress = onProgress
        )
    }

    /**
     * 极简资源安装：从「资源 + 目标实例」一步完成安装
     *
     * 调用方只需要提供来源、资源 ID 与目标实例，
     * 版本匹配、文件选择、下载、校验、安装全部由核心完成。
     *
     * @return 实际安装的资源版本
     * @throws ResourceMatchException 匹配失败时抛出，携带具体原因
     */
    suspend fun installToInstance(
        platform: Platform,
        projectId: String,
        type: ResourceType,
        instance: Version,
        onProgress: (ResourceDownloadProgress) -> Unit = {}
    ): ResourceVersion {
        val matched = matchVersion(platform, projectId, type, instance)
        installVersion(
            version = matched,
            type = type,
            instance = instance,
            onProgress = onProgress
        )
        return matched
    }
}
