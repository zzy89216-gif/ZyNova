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

package com.movtery.zalithlauncher.game.download.assets

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.resources.ResourceManager
import com.movtery.zalithlauncher.game.download.resources.ResourceType
import com.movtery.zalithlauncher.game.download.resources.toResourceVersion
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel

private const val TAG = "QuickInstall"

/**
 * 极简安装资源
 *
 * 上下文优先：目标实例、Minecraft 版本、加载器、资源目录都已经由调用方确定，
 * 因此不再要求用户重复选择「游戏版本 / 实例 / 安装位置」。
 *
 * 实际流程完全交由 ZyNova 统一资源核心执行：
 * 兼容性检查 → 选择兼容文件 → 下载 → 校验 → 安装
 *
 * @param version 用户选中的资源版本
 * @param classes 资源类别（决定安装到 mods / shaderpacks 等目录）
 * @param submitError 错误上报
 * @param projectId 资源项目 ID；缺省时退回使用版本 ID
 * @param targetVersionName 目标游戏实例名称；缺省时安装到当前实例
 */
fun quickInstallAsset(
    version: PlatformVersion,
    classes: PlatformClasses,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    projectId: String? = null,
    targetVersionName: String? = null
) {
    //上下文优先：优先安装到进入资源页面时所在的实例
    val currentVersion = targetVersionName
        ?.let { name -> VersionsManager.versions.value.firstOrNull { it.getVersionName() == name } }
        ?: VersionsManager.currentVersion.value
    if (currentVersion == null || !currentVersion.isValid()) {
        submitError(
            ErrorViewModel.ThrowableMessage(
                title = androidText(R.string.download_assets_install_failed),
                message = androidText(R.string.download_assets_no_installed_versions)
            )
        )
        return
    }

    val type = ResourceType.of(classes)
    val taskId = "quick_install_${version.platformSha1() ?: version.platformFileName()}"

    TaskSystem.submitTask(
        Task.runTask(
            id = taskId,
            task = { task ->
                task.updateProgress(-1f)
                task.updateMessage(androidText(R.string.download_assets_quick_install_resolving))

                //1. 统一模型转换
                val resourceVersion = version.toResourceVersion(projectId ?: version.platformId())

                //2. 统一下载 → 校验 → 安装（统一走资源管理核心）
                ResourceManager.installVersion(
                    version = resourceVersion,
                    type = type,
                    instance = currentVersion,
                    onProgress = { progress ->
                        task.updateProgress(progress.fraction)
                        task.updateMessage(
                            androidText(
                                R.string.download_assets_quick_install_progress,
                                (progress.finishedCount + 1).coerceAtMost(progress.totalCount),
                                progress.totalCount,
                                progress.fileName
                            )
                        )
                    }
                )
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
 * 从资源 ID 一步安装到当前实例（极简安装）
 *
 * 用于资源**搜索结果**卡片上的快捷安装：用户只需要点一下，
 * 剩下的「版本匹配 → 选兼容文件 → 递归装必需前置 → 下载 → 校验 → 安装」
 * 全部由统一资源核心完成，不需要用户再选择 Minecraft 版本或前置依赖。
 *
 * @param platform 资源来源
 * @param projectId 资源项目 ID
 * @param classes 资源类别（决定安装目录）
 * @param targetVersionName 目标游戏实例名称；为空时使用当前实例
 */
fun quickInstallResource(
    platform: Platform,
    projectId: String,
    classes: PlatformClasses,
    targetVersionName: String?,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    val instance = targetVersionName
        ?.let { name -> VersionsManager.versions.value.firstOrNull { it.getVersionName() == name } }
        ?: VersionsManager.currentVersion.value

    if (instance == null || !instance.isValid()) {
        submitError(
            ErrorViewModel.ThrowableMessage(
                title = androidText(R.string.download_assets_install_failed),
                message = androidText(R.string.download_assets_no_installed_versions)
            )
        )
        return
    }

    TaskSystem.submitTask(
        Task.runTask(
            id = "quick_install_${platform.name}_$projectId",
            task = { task ->
                task.updateProgress(-1f)
                task.updateMessage(androidText(R.string.download_assets_quick_install_resolving))

                val installed = ResourceManager.installToInstance(
                    platform = platform,
                    projectId = projectId,
                    type = ResourceType.of(classes),
                    instance = instance,
                    onProgress = { progress ->
                        task.updateProgress(progress.fraction)
                        task.updateMessage(
                            androidText(
                                R.string.download_assets_quick_install_progress,
                                (progress.finishedCount + 1).coerceAtMost(progress.totalCount),
                                progress.totalCount,
                                progress.fileName
                            )
                        )
                    }
                )

                //没有找到与该实例兼容的版本
                if (installed == null) {
                    throw IllegalStateException(
                        "No compatible version found for this instance"
                    )
                }
            },
            onError = { e ->
                Logger.warning(TAG, "Quick install from search result failed.", e)
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
