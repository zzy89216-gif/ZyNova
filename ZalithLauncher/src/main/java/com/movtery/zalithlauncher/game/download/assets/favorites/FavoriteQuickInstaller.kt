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

package com.movtery.zalithlauncher.game.download.assets.favorites

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.resources.ResourceManager
import com.movtery.zalithlauncher.game.download.resources.ResourceMatchException
import com.movtery.zalithlauncher.game.download.resources.ResourceType
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "FavoriteQuickInstaller"

/**
 * 收藏页「一键下载」的结果
 *
 * 与资源下载页（[_Download.QuickInstall.kt]）的区别：
 * 那边失败只弹一个通用错误框，用户没有下一步；收藏页需要能给出
 * **可操作**的下一步，所以这里把「为什么没装成」拆成了独立状态。
 */
sealed interface FavoriteInstallOutcome {
    /** 装好了（可能带没装上的必需前置） */
    data class Installed(
        val instanceName: String,
        val versionName: String,
        /** 没装上的必需前置；null = 全装上了 */
        val missingDependencies: String? = null,
    ) : FavoriteInstallOutcome

    /**
     * 该资源没有任何与目标实例兼容的版本
     *
     * 这是用户报障的场景：从版本设置进入下载页后再点收藏，
     * 收藏里的模组可能根本没有当前实例的版本。界面必须据此
     * 给出「查看全部版本 / 换实例」的入口，而不是只弹一句错误。
     */
    data class NoCompatibleVersion(
        val instanceName: String,
        val minecraftVersion: String,
        val loaderName: String? = null,
    ) : FavoriteInstallOutcome

    /** 没有可用的游戏实例（还没建实例，或指定实例已不存在） */
    data class NoInstance(val targetVersionName: String?) : FavoriteInstallOutcome

    /** 用户从任务面板取消了安装 */
    data object Cancelled : FavoriteInstallOutcome

    /** 其它失败（网络、来源不支持、实例信息损坏等），带原始异常便于复用既有错误文案 */
    data class Failed(val error: Throwable) : FavoriteInstallOutcome
}

/**
 * 在线收藏的一键安装（含必需前置）
 *
 * 为什么不在收藏页直接抄 `quickInstallResource`：
 * 那个入口把错误写进全局错误框，发不出「这个资源没有当前版本 → 去看全部版本」
 * 这种可操作结果。这里走同一条 [ResourceManager.installToInstance] 安装链路
 * （任务面板照旧显示进度、可取消），只是把结果分类后回调给界面。
 *
 * ⚠️ 整合包（[PlatformClasses.MOD_PACK]）有单独的安装流程（解压 + 输版本名），
 * 不要走这里；调用方应先跳转到整合包详情页。
 */
object FavoriteQuickInstaller {

    /**
     * 提交安装任务
     *
     * @param targetVersionName 目标实例名称；为空表示当前实例（与资源下载页规则一致）
     * @param onOutcome 结果回调，**保证在主线程**调用
     * @return false = 没有实例 / 同一个资源已经在安装中（界面据此复位按钮）
     */
    fun install(
        platform: Platform,
        projectId: String,
        classes: PlatformClasses,
        targetVersionName: String?,
        onOutcome: (FavoriteInstallOutcome) -> Unit,
    ): Boolean {
        val instance = resolveInstance(targetVersionName)
        if (instance == null || !instance.isValid()) {
            TaskSystem.appScope.launch(Dispatchers.Main.immediate) {
                onOutcome(FavoriteInstallOutcome.NoInstance(targetVersionName))
            }
            return false
        }

        val taskId = "favorite_install_${instance.getVersionName()}_${platform.name}_$projectId"
        //防抖：连点、或者同一资源重复进入时只保留一个安装任务
        if (TaskSystem.containsTask(taskId)) return false

        TaskSystem.submitTask(
            Task.runTask(
                id = taskId,
                task = { task ->
                    task.updateProgress(-1f)
                    task.updateTitle(androidText(R.string.main_task_install_resource, projectId))
                    task.updateMessage(androidText(R.string.download_assets_quick_install_resolving))

                    val outcome = ResourceManager.installToInstance(
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

                    task.updateMessage(
                        androidText(
                            R.string.download_assets_quick_install_done,
                            outcome.version.displayName
                        )
                    )
                    deliver(onOutcome) {
                        FavoriteInstallOutcome.Installed(
                            instanceName = instance.getVersionName(),
                            versionName = outcome.version.displayName,
                            missingDependencies = outcome.result.takeIf {
                                it.hasMissingRequiredDependencies
                            }?.missingDependenciesText()
                        )
                    }
                },
                onError = { error ->
                    Logger.warning(TAG, "Favorite quick install failed.", error)
                    deliver(onOutcome) { error.toOutcome(instance) }
                },
                onCancel = {
                    TaskSystem.appScope.launch(Dispatchers.Main.immediate) {
                        onOutcome(FavoriteInstallOutcome.Cancelled)
                    }
                }
            )
        )
        return true
    }

    /** 与资源下载页一致的实例解析：优先进入页面时的上下文，其次当前实例 */
    private fun resolveInstance(targetVersionName: String?): Version? {
        return targetVersionName
            ?.let { name -> VersionsManager.versions.value.firstOrNull { it.getVersionName() == name } }
            ?: VersionsManager.currentVersion.value
    }

    /** 统一在主线程回调，界面里可以直接改 Compose 状态 */
    private suspend fun deliver(
        onOutcome: (FavoriteInstallOutcome) -> Unit,
        outcome: () -> FavoriteInstallOutcome,
    ) {
        withContext(Dispatchers.Main.immediate) {
            onOutcome(outcome())
        }
    }

    /**
     * 把安装异常分类成界面能处理的结果
     *
     * 版本不匹配单独拎出来（用户报障的核心）：主资源都没装，
     * 不需要让用户去猜是网络问题还是资源不支持。
     */
    private fun Throwable.toOutcome(instance: Version): FavoriteInstallOutcome {
        if (this !is ResourceMatchException) {
            return FavoriteInstallOutcome.Failed(this)
        }
        return when (this) {
            is ResourceMatchException.NoCompatibleVersion ->
                FavoriteInstallOutcome.NoCompatibleVersion(
                    instanceName = instance.getVersionName(),
                    minecraftVersion = minecraftVersion,
                    loaderName = loaderName
                )
            //保留 QueryFailed 包装：界面会把它转成「查询该资源可用版本失败：<原因>」
            is ResourceMatchException.QueryFailed ->
                FavoriteInstallOutcome.Failed(this)
            is ResourceMatchException.InstanceInfoUnavailable,
            is ResourceMatchException.Blacklisted ->
                FavoriteInstallOutcome.Failed(this)
        }
    }
}
