/*
 * ZyNova Launcher
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

package com.movtery.zalithlauncher.game.download.assets

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDependencyType
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.assets.platform.getVersions
import com.movtery.zalithlauncher.game.download.assets.platform.mcim.mapMCIMMirrorUrls
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionInfo
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.game.version.saves.unpackSaveZip
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.file.ensureParentDirectory
import com.movtery.zalithlauncher.utils.file.formatFileSize
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.downloadFileFromSources
import com.movtery.zalithlauncher.utils.network.withSpeedReport
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException

private const val TAG = "QuickInstall"

/**
 * 一键安装资源（仿 Modrinth App）：
 * 1. 自动解析并安装 REQUIRED 前置依赖（递归）
 * 2. 自动选择与当前游戏版本/加载器适配的最新版本
 * 3. 全部文件直接下载安装到当前选中的游戏版本，全程无弹窗
 *
 * @param version 用户选中的主资源版本
 * @param classes 资源类别（决定安装到 mods / shaderpacks 等目录）
 */
fun quickInstallAsset(
    version: PlatformVersion,
    classes: PlatformClasses,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val currentVersion = VersionsManager.currentVersion.value
    if (currentVersion == null || !currentVersion.isValid()) {
        submitError(
            ErrorViewModel.ThrowableMessage(
                title = androidText(R.string.download_assets_install_failed),
                message = androidText(R.string.download_assets_no_installed_versions)
            )
        )
        return
    }

    val taskId = "quick_install_${version.platformSha1() ?: version.platformFileName()}"

    TaskSystem.submitTask(
        Task.runTask(
            id = taskId,
            task = { task ->
                task.updateProgress(-1f)
                task.updateMessage(androidText(R.string.download_assets_quick_install_resolving))

                // 递归解析所有需要安装的文件（主文件 + REQUIRED 前置依赖）
                val toInstall = mutableListOf<PlatformVersion>()
                val visited = mutableSetOf<String>()
                collectDependencies(
                    version = version,
                    currentVersion = currentVersion,
                    visited = visited,
                    out = toInstall
                )

                val targetFolder = File(currentVersion.getGameDir(), classes.versionFolder.folderName)
                if (!targetFolder.exists() && !targetFolder.mkdirs()) {
                    throw IOException("Failed to create target folder: ${targetFolder.absolutePath}")
                }

                toInstall.forEachIndexed { index, v ->
                    val fileName = v.platformFileName()
                    task.updateMessage(
                        androidText(
                            R.string.download_assets_quick_install_progress,
                            index + 1,
                            toInstall.size,
                            fileName
                        )
                    )
                    downloadAssetFile(v, targetFolder, task)

                    //存档资源下载后需要解压
                    if (classes == PlatformClasses.SAVES) {
                        task.updateMessage(androidText(R.string.download_assets_install_progress_installing, fileName))
                        unpackSaveZip(File(targetFolder, fileName), targetFolder)
                    }
                }
            },
            onError = { e ->
                Logger.warning(TAG, "Quick install failed.", e)
                submitError(
                    ErrorViewModel.ThrowableMessage(
                        title = androidText(R.string.download_assets_install_failed),
                        message = mapExceptionToMessage(e)
                    )
                )
            }
        )
    )
}

/**
 * 递归收集主文件及其 REQUIRED 前置依赖
 * 使用 [visited] 防止循环依赖与重复添加
 */
private suspend fun collectDependencies(
    version: PlatformVersion,
    currentVersion: Version,
    visited: MutableSet<String>,
    out: MutableList<PlatformVersion>
) {
    val key = "${version.platform()}:${version.platformId()}"
    if (!visited.add(key)) return
    out.add(version)

    val requiredDeps = version.platformDependencies().filter {
        it.type == PlatformDependencyType.REQUIRED
    }

    requiredDeps.forEach { dep ->
        runCatching {
            val depVersions = resolveAdaptVersions(dep.projectId, dep.platform, currentVersion)
            if (depVersions != null) {
                collectDependencies(depVersions, currentVersion, visited, out)
            }
        }.onFailure { e ->
            Logger.warning(TAG, "Failed to resolve dependency ${dep.projectId}: ${e.message}")
        }
    }
}

/**
 * 获取某个依赖项目与当前游戏版本适配的最新版本
 * @return 适配的最新版本；若无适配版本或获取失败则返回 null
 */
private suspend fun resolveAdaptVersions(
    projectId: String,
    platform: Platform,
    currentVersion: Version
): PlatformVersion? = withContext(Dispatchers.IO) {
    runCatching {
        val versions = getVersions(projectId, platform)
            .mapNotNull { v ->
                if (v.initFile(projectId)) v else null
            }
            .sortedByDescending { it.platformDatePublished() }

        selectAdaptVersion(versions, currentVersion)
    }.getOrElse { e ->
        Logger.warning(TAG, "Failed to load versions of dependency $projectId: ${e.message}")
        null
    }
}

/**
 * 从已按发布日期降序排序的版本列表中，选择与当前游戏版本/加载器适配的第一个（即最新）版本
 */
private fun selectAdaptVersion(
    versions: List<PlatformVersion>,
    currentVersion: Version
): PlatformVersion? {
    val info = currentVersion.getVersionInfo() ?: return null
    val mcVersion = info.minecraftVersion
    val loaderInfo = info.loaderInfo

    return versions.firstOrNull { v ->
        v.platformGameVersion().contains(mcVersion) && isLoaderAdapt(v, loaderInfo)
    }
}

/**
 * 判断资源的加载器是否与当前游戏版本的加载器匹配
 */
private fun isLoaderAdapt(
    version: PlatformVersion,
    loaderInfo: VersionInfo.LoaderInfo?
): Boolean {
    val loaders = version.platformLoaders()
    return when {
        loaders.isEmpty() -> true // 资源未标注加载器，视为通用
        loaderInfo == null -> false // 资源有加载器，但当前版本无加载器信息
        else -> loaders.any { it.getDisplayName().equals(loaderInfo.loader.displayName, true) }
    }
}

/**
 * 下载单个资源文件到目标文件夹
 */
private suspend fun downloadAssetFile(
    version: PlatformVersion,
    targetFolder: File,
    task: Task
) {
    val cacheFile = File(
        File(PathManager.DIR_CACHE, "assets"),
        version.platformSha1() ?: version.platformFileName()
    )

    val totalFileSize = version.platformFileSize()
    var downloadedSize = 0L

    withSpeedReport(
        onSpeedReport = { bytes -> task.updateSpeed(bytes) },
        onClear = { task.clearSpeed() }
    ) { report ->
        downloadFileFromSources(
            urls = version.platformDownloadUrl().mapMCIMMirrorUrls(),
            sha1 = version.platformSha1(),
            outputFile = cacheFile.ensureParentDirectory(),
            sizeCallback = { size ->
                downloadedSize += size
                task.updateProgress(
                    if (totalFileSize > 0) (downloadedSize.toDouble() / totalFileSize.toDouble()).toFloat() else -1f
                )
                task.updateMessage(
                    androidText(
                        R.string.download_assets_install_progress_downloading,
                        version.platformFileName(),
                        formatFileSize(downloadedSize),
                        formatFileSize(totalFileSize)
                    )
                )
                report(size)
            }
        )
    }

    // 下载完成，复制到目标文件夹
    val targetFile = File(targetFolder, version.platformFileName())
    if (targetFile.exists() && !targetFile.delete()) {
        throw IOException("Failed to delete existing target file: ${targetFile.absolutePath}")
    }
    cacheFile.copyTo(targetFile)
    FileUtils.deleteQuietly(cacheFile)
}
