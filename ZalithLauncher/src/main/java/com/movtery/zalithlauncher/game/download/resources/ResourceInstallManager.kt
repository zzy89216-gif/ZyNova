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

import com.movtery.zalithlauncher.game.download.assets.platform.getVersionByLocalFile
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.saves.unpackSaveZip
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.logging.Logger
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException

private const val TAG = "ResourceInstall"

/**
 * 资源安装状态
 */
enum class ResourceInstallState {
    /** 未安装 */
    NOT_INSTALLED,

    /** 已安装 */
    INSTALLED,

    /** 已安装，但存在可用的新版本 */
    UPDATE_AVAILABLE
}

/**
 * 待安装的一个资源版本
 */
data class ResourceInstallEntry(
    val version: ResourceVersion,
    /** 是否为前置依赖 */
    val isDependency: Boolean
) {
    val fileName: String? get() = version.file?.fileName
}

/**
 * ZyNova 统一资源安装管理器
 *
 * 统一负责「版本匹配 → 文件选择 → 下载 → 校验 → 安装」，
 * 资源页面不再各自实现下载与安装逻辑。
 */
object ResourceInstallManager {

    /**
     * 判断资源在当前实例中的安装状态（按文件名匹配）
     *
     * @param installedFileNames 当前实例该资源类别下已经安装的文件名
     * @param targetFileName 准备安装的文件名
     */
    fun installState(
        installedFileNames: Collection<String>,
        targetFileName: String?
    ): ResourceInstallState {
        if (targetFileName.isNullOrBlank()) return ResourceInstallState.NOT_INSTALLED
        return if (installedFileNames.any { it.equals(targetFileName, ignoreCase = true) }) {
            ResourceInstallState.INSTALLED
        } else {
            ResourceInstallState.NOT_INSTALLED
        }
    }

    /**
     * 判断资源在当前实例中的安装状态（按版本识别结果）
     *
     * 用于区分「已安装」与「可更新」：
     * 本地识别到的版本与最新兼容版本一致时是「已安装」，
     * 不一致时说明存在新版本，可以更新。
     *
     * @param installedVersionId 通过本地文件识别出的、已安装的版本 ID
     * @param latestVersionId 最新兼容版本的版本 ID
     */
    fun installState(
        installedVersionId: String?,
        latestVersionId: String?
    ): ResourceInstallState {
        if (latestVersionId.isNullOrBlank()) return ResourceInstallState.NOT_INSTALLED
        if (installedVersionId.isNullOrBlank()) return ResourceInstallState.NOT_INSTALLED
        return if (installedVersionId == latestVersionId) {
            ResourceInstallState.INSTALLED
        } else {
            ResourceInstallState.UPDATE_AVAILABLE
        }
    }

    /**
     * 通过本地已安装文件识别它对应的资源版本 ID
     *
     * @return 识别出的版本 ID；无法识别时返回 null
     */
    suspend fun identifyInstalledVersionId(file: File, sha1: String): String? {
        return runCatching {
            getVersionByLocalFile(file, sha1)?.platformId()
        }.getOrElse { e ->
            Logger.warning(TAG, "Failed to identify an installed resource file: ${file.name}", e)
            null
        }
    }

    /**
     * 读取某个资源类别下当前实例已经安装的文件名
     */
    fun installedFileNames(instance: Version, type: ResourceType): List<String> {
        val folder = File(instance.getGameDir(), type.classes.versionFolder.folderName)
        if (!folder.isDirectory) return emptyList()
        return folder.listFiles()?.filter { it.isFile }?.map { it.name }.orEmpty()
    }

    /**
     * 版本匹配：在资源的所有版本中挑选与当前实例兼容的最新版本
     *
     * 能自动判断，就不让用户选择。
     *
     * @param instance 目标游戏实例
     * @return 适配的版本；若没有兼容版本则返回 null
     */
    suspend fun resolveCompatibleVersion(
        provider: ResourceProvider,
        projectId: String,
        type: ResourceType,
        instance: Version
    ): ResourceVersion? {
        val info = instance.getVersionInfo() ?: return null
        return runCatching {
            provider.getVersions(projectId, type)
                .sortedByDescending { it.publishedAt }
                .firstOrNull { version ->
                    version.file != null &&
                        version.supportsGameVersion(info.minecraftVersion) &&
                        version.supportsLoader(info.loaderInfo?.loader?.displayName)
                }
        }.getOrElse { e ->
            Logger.warning(TAG, "Failed to resolve a compatible version for $projectId: ${e.message}")
            null
        }
    }

    /**
     * 构建安装计划：主资源 + 必需的（递归）前置依赖
     *
     * @param includeOptionalDependencies 是否同时安装可选依赖
     */
    suspend fun buildInstallPlan(
        version: ResourceVersion,
        instance: Version,
        includeOptionalDependencies: Boolean = false
    ): List<ResourceInstallEntry> {
        val plan = mutableListOf<ResourceInstallEntry>()
        val visited = mutableSetOf<String>()

        suspend fun collect(current: ResourceVersion, isDependency: Boolean) {
            val key = "${current.provider}:${current.projectId}:${current.versionId}"
            if (!visited.add(key)) return
            if (current.file == null) return

            plan.add(ResourceInstallEntry(current, isDependency))

            current.dependencies
                .filter { it.isRequired || includeOptionalDependencies }
                .forEach { dependency ->
                    val resolved = resolveDependency(dependency, instance) ?: return@forEach
                    collect(resolved, true)
                }
        }

        collect(version, false)
        return plan
    }

    /**
     * 解析单个依赖对应的可用版本
     *
     * 优先使用作者指定的精确版本，失败时回退为「选择适配当前实例的最新版本」。
     */
    private suspend fun resolveDependency(
        dependency: ResourceDependency,
        instance: Version
    ): ResourceVersion? {
        val provider = runCatching { ResourceProviders.of(dependency.provider) }.getOrNull() ?: return null
        val type = ResourceType.MOD

        dependency.versionId?.let { versionId ->
            runCatching {
                provider.getVersionById(versionId, dependency.projectId, type)
            }.getOrNull()?.let { return it }
        }

        return resolveCompatibleVersion(provider, dependency.projectId, type, instance)
    }

    /**
     * 执行安装：下载 → 校验 → 安装
     *
     * 严格按统一流程执行，调用方只需要提供安装计划与目标实例。
     *
     * @param plan 安装计划
     * @param type 资源类型（决定安装目录）
     * @param instance 目标游戏实例
     * @param onProgress 下载进度回调
     * @return 实际安装的文件数量
     */
    suspend fun install(
        plan: List<ResourceInstallEntry>,
        type: ResourceType,
        instance: Version,
        onProgress: (ResourceDownloadProgress) -> Unit = {}
    ): Int {
        if (plan.isEmpty()) return 0

        val targetFolder = File(instance.getGameDir(), type.classes.versionFolder.folderName)
        if (!targetFolder.exists() && !targetFolder.mkdirs()) {
            throw IOException("Failed to create target folder: ${targetFolder.absolutePath}")
        }

        //1. 统一交给下载管理器（并发、重试、续传、校验都在这里完成）
        val cacheDir = File(PathManager.DIR_CACHE, "resources")
        val requests = plan.mapNotNull { entry ->
            entry.version.file?.let { file ->
                ResourceDownloadRequest(
                    fileName = file.fileName,
                    downloadUrls = file.downloadUrls,
                    sha1 = file.sha1,
                    size = file.size,
                    //前置依赖失败不应该让主资源也失败
                    required = !entry.isDependency
                )
            }
        }

        val downloaded = ResourceDownloadManager.downloadAll(
            cacheDir = cacheDir,
            requests = requests,
            onProgress = onProgress
        )

        //2. 安装到目标实例
        var installed = 0
        downloaded.forEach { result ->
            val targetFile = File(targetFolder, result.file.name)
            if (targetFile.exists() && !targetFile.delete()) {
                throw IOException("Failed to delete the existing resource file: ${targetFile.absolutePath}")
            }
            result.file.copyTo(targetFile, overwrite = true)

            //存档资源需要解压后才能被游戏识别
            if (type == ResourceType.SAVE) {
                runCatching {
                    unpackSaveZip(targetFile, targetFolder)
                }.onFailure { e ->
                    Logger.warning(TAG, "Failed to unpack save file ${targetFile.name}: ${e.message}")
                }
            }
            installed++
        }

        //3. 清理本次的缓存文件
        downloaded.forEach { FileUtils.deleteQuietly(it.file) }
        return installed
    }
}
