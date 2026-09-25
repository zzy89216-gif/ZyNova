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

import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchFilter
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchResult
import com.movtery.zalithlauncher.game.download.assets.platform.getProjectByVersion
import com.movtery.zalithlauncher.game.download.assets.platform.getVersionById
import com.movtery.zalithlauncher.game.download.assets.platform.getVersions
import com.movtery.zalithlauncher.game.download.assets.platform.searchAssets
import com.movtery.zalithlauncher.utils.logging.Logger

private const val TAG = "ResourceProvider"

/**
 * 资源来源操作失败
 */
class ResourceProviderException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * 统一资源来源接口（Resource Provider）
 *
 * 上层（资源管理核心、下载管理、UI）只依赖这个接口，
 * 不直接接触 Modrinth / CurseForge 的具体实现。
 *
 * 以后增加新的资源来源时，只需要实现本接口并注册到 [ResourceProviders]，
 * 不需要重写整个资源系统。
 */
interface ResourceProvider {
    /** 来源标识 */
    val platform: Platform

    /** 来源显示名称 */
    val displayName: String

    /** 该来源支持的资源类型 */
    val supportedTypes: Set<ResourceType>

    /** 当前是否可用（例如缺少 API Key 的来源不可用） */
    val isAvailable: Boolean

    /**
     * 是否支持指定资源类型
     */
    fun supports(type: ResourceType): Boolean = type in supportedTypes

    /**
     * 搜索资源
     */
    suspend fun search(
        query: String,
        type: ResourceType,
        filter: PlatformSearchFilter
    ): PlatformSearchResult

    /**
     * 获取资源详情
     */
    suspend fun getResource(projectId: String, type: ResourceType): Resource

    /**
     * 获取资源的所有版本（已转换为统一模型）
     */
    suspend fun getVersions(projectId: String, type: ResourceType): List<ResourceVersion>

    /**
     * 通过版本 ID 获取单个版本
     */
    suspend fun getVersionById(
        versionId: String,
        projectId: String,
        type: ResourceType
    ): ResourceVersion?
}

/**
 * Provider 的通用实现骨架
 *
 * 具体来源只需要声明自己的元信息，搜索与版本请求统一委托给
 * 已有的多源/镜像调度层，避免每个来源各自实现一套网络逻辑。
 */
abstract class AbstractResourceProvider(
    override val platform: Platform,
    override val displayName: String,
    override val supportedTypes: Set<ResourceType>
) : ResourceProvider {

    protected fun requireSupported(type: ResourceType) {
        if (!supports(type)) {
            throw ResourceProviderException("$displayName does not support resource type: $type")
        }
    }

    override suspend fun search(
        query: String,
        type: ResourceType,
        filter: PlatformSearchFilter
    ): PlatformSearchResult {
        requireSupported(type)

        var result: PlatformSearchResult? = null
        var failure: Throwable? = null

        searchAssets(
            searchPlatform = platform,
            searchFilter = filter.copy(searchName = query),
            platformClasses = type.classes,
            onSuccess = { result = it },
            onError = { state ->
                failure = ResourceProviderException("$displayName search failed: $state")
            }
        )

        result?.let { return it }
        failure?.let { throw it }
        throw ResourceProviderException("$displayName returned an empty search result")
    }

    override suspend fun getResource(projectId: String, type: ResourceType): Resource {
        requireSupported(type)
        val project = runCatching {
            getProjectByVersion(projectId = projectId, platform = platform)
        }.getOrElse { e ->
            throw ResourceProviderException("$displayName failed to load resource $projectId", e)
        }
        return project.toResource(type)
    }

    override suspend fun getVersions(projectId: String, type: ResourceType): List<ResourceVersion> {
        requireSupported(type)
        val versions = runCatching {
            getVersions(projectID = projectId, platform = platform)
        }.getOrElse { e ->
            throw ResourceProviderException("$displayName failed to load versions of $projectId", e)
        }
        return versions
            .mapNotNull { version ->
                //部分版本需要额外初始化才能拿到文件信息
                runCatching {
                    if (version.initFile(projectId)) version.toResourceVersion(projectId) else null
                }.getOrElse { e ->
                    Logger.warning(TAG, "Skipped an unusable version of $projectId: ${e.message}")
                    null
                }
            }
    }

    override suspend fun getVersionById(
        versionId: String,
        projectId: String,
        type: ResourceType
    ): ResourceVersion? {
        requireSupported(type)
        val version = runCatching {
            getVersionById(versionID = versionId, platform = platform)
        }.getOrElse { e ->
            Logger.warning(TAG, "Failed to load version $versionId from $displayName: ${e.message}")
            null
        } ?: return null

        if (!version.initFile(projectId)) return null
        return version.toResourceVersion(projectId)
    }
}

/**
 * Modrinth 资源来源
 */
object ModrinthProvider : AbstractResourceProvider(
    platform = Platform.MODRINTH,
    displayName = "Modrinth",
    supportedTypes = setOf(
        ResourceType.MOD,
        ResourceType.RESOURCE_PACK,
        ResourceType.SHADER,
        ResourceType.MOD_PACK
    )
) {
    override val isAvailable: Boolean = true
}

/**
 * CurseForge 资源来源
 *
 * 需要 API Key 才可用，缺少密钥时不参与资源检索。
 */
object CurseForgeProvider : AbstractResourceProvider(
    platform = Platform.CURSEFORGE,
    displayName = "CurseForge",
    supportedTypes = ResourceType.entries.toSet()
) {
    override val isAvailable: Boolean
        get() = BuildKeys.CURSEFORGE_API.isNotBlank()
}

/**
 * 资源来源注册表
 *
 * 上层通过这里获取统一接口，新增来源时只需在此注册。
 */
object ResourceProviders {
    /** 全部已注册的来源 */
    val all: List<ResourceProvider> = listOf(ModrinthProvider, CurseForgeProvider)

    /**
     * 获取指定来源
     */
    fun of(platform: Platform): ResourceProvider =
        all.firstOrNull { it.platform == platform }
            ?: throw ResourceProviderException("Unsupported resource platform: $platform")

    /**
     * 获取支持指定资源类型、且当前可用的来源列表
     */
    fun availableFor(type: ResourceType): List<ResourceProvider> =
        all.filter { it.isAvailable && it.supports(type) }

    /**
     * 获取支持指定资源类型的所有来源（不判断可用性）
     */
    fun allFor(type: ResourceType): List<ResourceProvider> =
        all.filter { it.supports(type) }
}
