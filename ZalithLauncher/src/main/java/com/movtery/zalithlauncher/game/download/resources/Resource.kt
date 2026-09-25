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

import androidx.annotation.StringRes
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDependencyType
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformProject
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformReleaseType
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.assets.platform.mcim.mapMCIMMirrorUrls

/**
 * ZyNova 统一资源类型
 *
 * 把底层平台分类 [PlatformClasses] 归一化成"用户真正关心的资源种类"，
 * Mod、资源包、光影等资源统一使用这一套类型。
 *
 * @param classes 对应的平台分类
 * @param displayNameRes 展示名称
 */
enum class ResourceType(
    val classes: PlatformClasses,
    @StringRes val displayNameRes: Int
) {
    MOD(PlatformClasses.MOD, R.string.download_category_mod),
    RESOURCE_PACK(PlatformClasses.RESOURCE_PACK, R.string.download_category_resource_pack),
    SHADER(PlatformClasses.SHADERS, R.string.download_category_shaders),
    SAVE(PlatformClasses.SAVES, R.string.download_category_saves),
    MOD_PACK(PlatformClasses.MOD_PACK, R.string.download_category_modpack);

    companion object {
        private val BY_CLASSES = entries.associateBy { it.classes }

        /**
         * 由平台分类反查统一资源类型
         */
        fun of(classes: PlatformClasses): ResourceType = BY_CLASSES[classes] ?: MOD
    }
}

/**
 * 统一资源（搜索结果 / 资源详情）
 *
 * @param provider 资源来源
 * @param id 在来源内的资源 ID
 * @param slug 资源短标识
 * @param type 统一资源类型
 * @param title 标题
 * @param summary 简介
 * @param author 作者
 * @param iconUrl 图标
 * @param downloadCount 下载量
 */
data class Resource(
    val provider: Platform,
    val id: String,
    val slug: String,
    val type: ResourceType,
    val title: String,
    val summary: String?,
    val author: String?,
    val iconUrl: String?,
    val downloadCount: Long
)

/**
 * 统一的可安装文件
 *
 * @param fileName 文件名（安装时直接使用该名称）
 * @param downloadUrls 可用的下载地址（含镜像，按优先级排列）
 * @param sha1 校验值
 * @param size 文件大小（bytes），未知时为 0
 */
data class ResourceFile(
    val fileName: String,
    val downloadUrls: List<String>,
    val sha1: String?,
    val size: Long
)

/**
 * 统一的资源依赖
 *
 * @param provider 依赖所在的来源
 * @param projectId 依赖的项目 ID
 * @param versionId 作者指定的精确版本 ID，可能为空
 * @param type 依赖类型（必要 / 可选）
 */
data class ResourceDependency(
    val provider: Platform,
    val projectId: String,
    val versionId: String?,
    val type: PlatformDependencyType
) {
    /** 是否必须安装 */
    val isRequired: Boolean get() = type == PlatformDependencyType.REQUIRED
}

/**
 * 统一的资源版本
 *
 * @param provider 资源来源
 * @param projectId 所属资源 ID
 * @param versionId 版本 ID
 * @param displayName 版本显示名
 * @param gameVersions 适配的 Minecraft 版本
 * @param loaders 适配的加载器显示名
 * @param releaseType 发布类型
 * @param publishedAt 发布时间戳（毫秒）
 * @param file 主文件
 * @param dependencies 该版本声明的依赖
 */
data class ResourceVersion(
    val provider: Platform,
    val projectId: String,
    val versionId: String,
    val displayName: String,
    val gameVersions: List<String>,
    val loaders: List<String>,
    val releaseType: PlatformReleaseType,
    val publishedAt: Long,
    val file: ResourceFile?,
    val dependencies: List<ResourceDependency>
) {
    /** 适配的 Minecraft 版本是否包含指定版本 */
    fun supportsGameVersion(minecraftVersion: String): Boolean =
        gameVersions.isEmpty() || gameVersions.contains(minecraftVersion)

    /**
     * 是否适配指定加载器
     * 资源未标注加载器时视为通用资源
     */
    fun supportsLoader(loaderName: String?): Boolean {
        if (loaders.isEmpty()) return true
        if (loaderName.isNullOrBlank()) return false
        return loaders.any { it.equals(loaderName, ignoreCase = true) }
    }
}

/**
 * 把平台项目转换为统一资源
 */
fun PlatformProject.toResource(type: ResourceType): Resource {
    return Resource(
        provider = platform(),
        id = platformId(),
        slug = platformSlug(),
        type = type,
        title = platformTitle(),
        summary = platformSummary(),
        author = platformAuthor(),
        iconUrl = platformIconUrl(),
        downloadCount = platformDownloadCount()
    )
}

/**
 * 把平台版本转换为统一资源版本
 *
 * @param projectId 该版本所属的项目 ID（平台版本对象本身不携带项目 ID）
 */
fun PlatformVersion.toResourceVersion(projectId: String): ResourceVersion {
    val file = ResourceFile(
        fileName = platformFileName(),
        downloadUrls = platformDownloadUrl().mapMCIMMirrorUrls(),
        sha1 = platformSha1(),
        size = platformFileSize()
    )
    return ResourceVersion(
        provider = platform(),
        projectId = projectId,
        versionId = platformId(),
        displayName = platformDisplayName(),
        gameVersions = platformGameVersion().toList(),
        loaders = platformLoaders().map { it.getDisplayName() },
        releaseType = platformReleaseType(),
        publishedAt = platformDatePublished().toEpochMilli(),
        file = file,
        dependencies = platformDependencies().map { dependency ->
            ResourceDependency(
                provider = platform(),
                projectId = dependency.projectId,
                versionId = dependency.versionId,
                type = dependency.type
            )
        }
    )
}
