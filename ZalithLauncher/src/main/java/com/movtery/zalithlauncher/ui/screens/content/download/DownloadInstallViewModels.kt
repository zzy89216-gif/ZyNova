/*
 * ZyNova Launcher
 * Copyright (C) 2025-2026 ZyNova Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.screens.content.download

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.assets.platform.getVersions
import com.movtery.zalithlauncher.game.download.game.GameDownloadInfo
import com.movtery.zalithlauncher.game.download.game.GameInstaller
import com.movtery.zalithlauncher.game.download.modpack.install.ModPackInfo
import com.movtery.zalithlauncher.game.download.modpack.install.ModPackInstaller
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.notification.NotificationManager
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.initAll
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.viewmodel.ConfirmMobileDataOperation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

/**
 * 下载 / 安装操作状态（Activity 级）
 *
 * 议题 #72：以前这套状态挂在下载页的导航条目上（`viewModel(key = ...)` 在 NavDisplay entry 里），
 * 用户离开下载页 → 条目销毁 → 安装被取消，或者后台装完了却只有回到下载页才能看到弹窗；
 * 整合包「输入版本名」更会因为没人回答而卡死。
 *
 * 现在这两个 ViewModel 由 MainActivity 用 `by viewModels()` 持有（与 [com.movtery.zalithlauncher.viewmodel.ModpackImportViewModel]
 * 同一套范式），弹窗统一由 MainActivity 的全局宿主渲染，任何界面都能正常下载、正常弹窗。
 */

/** 游戏安装状态操作 */
sealed interface GameInstallOperation {
    data object None : GameInstallOperation
    /** 开始安装 */
    data object Install : GameInstallOperation
    /** 警告通知权限，可以无视，并直接开始安装 */
    data class WarningForNotification(val info: GameDownloadInfo) : GameInstallOperation
    /** 警告正在使用流量 */
    data class WarningForMobileData(val info: GameDownloadInfo) : GameInstallOperation
    /** 游戏安装出现异常 */
    data class Error(val th: Throwable) : GameInstallOperation
    /** 游戏已成功安装 */
    data object Success : GameInstallOperation
}

/** 整合包安装状态操作 */
sealed interface ModPackInstallOperation {
    data object None : ModPackInstallOperation
    /** 警告整合包的兼容性，同意后将进行安装 */
    data class Warning(val version: PlatformVersion, val iconUrl: String?) : ModPackInstallOperation
    /** 开始安装 */
    data object Install : ModPackInstallOperation
    /** 警告通知权限，可以无视，并直接开始安装 */
    data class WarningForNotification(val version: PlatformVersion, val iconUrl: String?) : ModPackInstallOperation
    /** 整合包安装出现异常 */
    data class Error(val th: Throwable) : ModPackInstallOperation
    /** 整合包已成功安装 */
    data object Success : ModPackInstallOperation
}

/** 整合包版本名称自定义状态操作 */
sealed interface VersionNameOperation {
    data object None : VersionNameOperation
    /** 等待用户输入版本名称 */
    data class Waiting(val info: ModPackInfo) : VersionNameOperation
}

/** 等待游戏安装器挂到 ViewModel 上的最长时间：超过就认为这次安装没起来 */
private const val INSTALLER_ATTACH_TIMEOUT_MS = 10_000L

/**
 * 游戏版本下载安装（Activity 级）
 */
class GameDownloadViewModel : ViewModel() {
    /**
     * 用于刷新游戏下载页面版本名称的检查
     */
    var versionNameErrorCheck by mutableStateOf(false)

    var installOperation by mutableStateOf<GameInstallOperation>(GameInstallOperation.None)

    /**
     * 游戏安装器
     */
    var installer by mutableStateOf<GameInstaller?>(null)

    /**
     * 刷新游戏下载页面内的版本名称检查
     */
    private fun refreshVersionNameCheck() {
        versionNameErrorCheck = !versionNameErrorCheck
    }

    fun install(
        context: Context,
        info: GameDownloadInfo,
        onStart: () -> Unit = {},
        onStop: () -> Unit = {}
    ) {
        //下载 / 安装跑在应用级作用域，离开下载页也不会被取消
        installer?.let { running ->
            //已经有一个安装在进行：把对话框重新显示出来，而不是再开一份
            installOperation = GameInstallOperation.Install
            onStart()
            return
        }
        installOperation = GameInstallOperation.Install
        //完全静默：页面里不再弹任何进度界面，只把进度写进全局任务队列，
        //顶部那条小进度条就是唯一的反馈来源
        val mirrorId = "install_game_${info.gameVersion}_${info.customVersionName}"
        TaskSystem.submitTask(
            Task.runTask(
                id = mirrorId,
                task = { task ->
                    task.updateProgress(0f)
                    task.updateTitle(
                        androidText(R.string.main_task_install_game, info.customVersionName)
                    )
                    //⚠️ installer 是在 submitTask **之后**才赋值的。
                    //   这里如果不等它挂上，第一次读到的就是 null，会被当成「已经装完」，
                    //   任务瞬间结束 —— 表现就是「点了下载，上面什么都不出现」。
                    var waited = 0
                    while (installer == null && waited < INSTALLER_ATTACH_TIMEOUT_MS) {
                        delay(100)
                        waited += 100
                    }
                    while (true) {
                        val current = installer
                        val list = current?.tasksFlow?.value.orEmpty()
                        if (list.isNotEmpty()) {
                            val average = list
                                .map { it.task.progress.value.coerceIn(0f, 1f) }
                                .average()
                                .toFloat()
                            task.updateProgress(average)
                            list.firstOrNull { it.task.stage.value.name == "RUNNING" }
                                ?.let { task.updateMessage(it.title) }
                        }
                        if (current == null) {
                            task.updateProgress(1f)
                            break
                        }
                        delay(400)
                    }
                },
                onCancel = {
                    //从顶部任务面板取消：必须把真正的安装也停掉，否则「界面说取消了、后台还在写文件」
                    runCatching { installer?.cancelInstall() }
                },
                onFinally = {
                    //兜底：任务被整体取消（例如设置里「取消全部任务」）时安装器不会回调，
                    //不在这里复位的话界面会永远停在「安装中」，按钮全部失效
                    if (installOperation is GameInstallOperation.Install) {
                        runCatching { installer?.cancelInstall() }
                        installer = null
                        installOperation = GameInstallOperation.None
                        refreshVersionNameCheck()
                        onStop()
                    }
                }
            )
        )

        installer = GameInstaller(context, info, TaskSystem.appScope).also {
            it.installGame(
                onInstalled = { version ->
                    installer = null
                    VersionsManager.refresh("[DownloadGame] GameInstaller.onInstalled", version)
                    installOperation = GameInstallOperation.Success
                    refreshVersionNameCheck()
                    onStop()
                },
                onError = { th ->
                    installer = null
                    installOperation = GameInstallOperation.Error(th)
                    refreshVersionNameCheck()
                    onStop()
                },
                onGameAlreadyInstalled = {
                    //很有可能发生在刚安装完成，再次点击安装按钮时。
                    //必须把 installer 置空并复位状态，否则下一次安装会被当成「已有安装在进行」永远拒绝
                    installer = null
                    installOperation = GameInstallOperation.None
                    //保险起见，再次刷新版本名称错误检查
                    refreshVersionNameCheck()
                    onStop()
                },
                onCancelled = {
                    installer = null
                    installOperation = GameInstallOperation.None
                    refreshVersionNameCheck()
                    onStop()
                }
            )
        }
        onStart()
    }

    fun cancel() {
        installer?.cancelInstall()
        installer = null
        installOperation = GameInstallOperation.None
        refreshVersionNameCheck()
    }

    override fun onCleared() {
        //⚠️ Activity 级 ViewModel：只有 Activity 真正销毁才会走到这里（旋转/分屏不会）。
        //不在里取消安装：安装跑在 TaskSystem.appScope 上，是「后台静默」的，
        //用户离开下载页只是关掉对话框，不该把已经下载一半的安装废掉。
    }
}

/**
 * 整合包（在线下载）安装（Activity 级）
 */
class ModPackViewModel : ViewModel() {
    var installOperation by mutableStateOf<ModPackInstallOperation>(ModPackInstallOperation.None)
    var versionNameOperation by mutableStateOf<VersionNameOperation>(VersionNameOperation.None)
    var confirmMobileDataOperation by mutableStateOf<ConfirmMobileDataOperation>(ConfirmMobileDataOperation.None)

    //等待用户输入版本名称相关
    private var versionNameContinuation: Continuation<String>? = null
    suspend fun waitForVersionName(modPackInfo: ModPackInfo): String {
        return suspendCancellableCoroutine { cont ->
            versionNameContinuation = cont
            versionNameOperation = VersionNameOperation.Waiting(modPackInfo)
        }
    }

    /**
     * 用户确认输入版本名称
     */
    fun confirmVersionName(name: String) {
        //恢复continuation
        versionNameContinuation?.resume(name)
        versionNameContinuation = null
        versionNameOperation = VersionNameOperation.None
    }

    //警告使用移动网络相关
    private var confirmMobileData: Continuation<Boolean>? = null
    suspend fun waitForConfirmMobileData(): Boolean {
        return suspendCancellableCoroutine { cont ->
            confirmMobileData = cont
            confirmMobileDataOperation = ConfirmMobileDataOperation.Waiting
        }
    }

    /**
     * 用户是否确认使用移动网络
     */
    fun confirmUseMobileData(use: Boolean) {
        //恢复continuation
        confirmMobileData?.resume(use)
        confirmMobileData = null
        confirmMobileDataOperation = ConfirmMobileDataOperation.None
    }

    /**
     * 搜索结果卡片的一键下载
     *
     * 结果卡片上只有一个项目，没有「选版本」这一步，所以先取该项目的最新版本，
     * 再交给现有链路（通知权限 → 兼容性警告 → 后台安装）。
     * 主页与「版本设置 → 资源管理」两个入口都可以用。
     */
    fun quickInstall(
        context: Context,
        platform: Platform,
        projectId: String,
        iconUrl: String?
    ) {
        if (installOperation != ModPackInstallOperation.None) return
        viewModelScope.launch {
            runCatching {
                //⚠️ getVersions() 返回的是**原始平台版本**，没有初始化主要文件；
                //直接拿去安装会在 ModPackInstaller 里读 platformDisplayName()/platformFileName()
                //时抛 `lateinit property thisPrimaryFile has not been initialized`，
                //在后台协程里就是未捕获异常，整个启动器会崩（28.2.0 用户崩溃日志）。
                //initAll 会在返回前逐项 initFile，并过滤掉没有可用下载文件的版本。
                getVersions(projectID = projectId, platform = platform)
                    .initAll(projectId)
            }
                .onSuccess { versions ->
                    //initAll 已按发布时间倒序，第一个就是最新版本
                    val version = versions.firstOrNull()
                    if (version == null) {
                        //不要给用户一句英文的 "No downloadable version"：
                        //指引他换一个整合包 / 去详情页选版本
                        installOperation = ModPackInstallOperation.Error(
                            IllegalStateException(
                                context.getString(R.string.download_modpack_no_version)
                            )
                        )
                        return@onSuccess
                    }
                    installOperation = if (!NotificationManager.checkNotificationEnabled(context)) {
                        ModPackInstallOperation.WarningForNotification(version, iconUrl)
                    } else {
                        ModPackInstallOperation.Warning(version, iconUrl)
                    }
                }
                .onFailure { th ->
                    //切换整合包导致的协程取消不是「取版本失败」，不要弹错误框
                    if (th is kotlinx.coroutines.CancellationException) throw th
                    Logger.warning("InstallModpack", "Failed to fetch modpack versions.", th)
                    installOperation = ModPackInstallOperation.Error(th)
                }
        }
    }

    /**
     * 整合包安装器
     */
    var installer by mutableStateOf<ModPackInstaller?>(null)

    fun install(
        context: Context,
        version: PlatformVersion,
        iconUrl: String?,
        onStart: () -> Unit = {},
        onStop: () -> Unit = {},
    ) {
        installOperation = ModPackInstallOperation.Install

        //把安装过程镜像到全局任务队列：这样任务面板里有进度、能取消，
        //不需要一个挡住整个界面的进度弹窗（维护者要的是后台静默下载）。
        val doneFlag = java.util.concurrent.atomic.AtomicBoolean(false)
        TaskSystem.submitTask(
            Task.runTask(
                id = "install_modpack_${System.currentTimeMillis()}",
                task = { task ->
                    task.updateProgress(-1f)
                    task.updateTitle(androidText(R.string.download_modpack_install_title))
                    while (!doneFlag.get()) {
                        val list = installer?.tasksFlow?.value.orEmpty()
                        if (list.isNotEmpty()) {
                            val average = list
                                .map { it.task.progress.value.coerceIn(0f, 1f) }
                                .average()
                                .toFloat()
                            task.updateProgress(average)
                            list.firstOrNull { it.task.stage.value.name == "RUNNING" }
                                ?.let { task.updateMessage(it.title) }
                        }
                        delay(400)
                    }
                },
                onCancel = {
                    doneFlag.set(true)
                    runCatching { installer?.cancelInstall() }
                },
                onFinally = {
                    doneFlag.set(true)
                    //兜底复位：任务被整体取消时安装器不一定回调，避免界面永远停在「安装中」
                    if (installOperation is ModPackInstallOperation.Install) {
                        runCatching { installer?.cancelInstall() }
                        installer = null
                        installOperation = ModPackInstallOperation.None
                        onStop()
                    }
                }
            )
        )

        installer = ModPackInstaller(
            context = context,
            version = version,
            iconUrl = iconUrl,
            //安装跑在 App 级作用域：退出下载页、切后台都继续，不要绑在页面 ViewModel 上
            scope = TaskSystem.appScope,
            waitForVersionName = ::waitForVersionName,
            waitForConfirmMobileData = ::waitForConfirmMobileData
        ).also {
            it.installModPack(
                onInstalled = { version ->
                    doneFlag.set(true)
                    installer = null
                    VersionsManager.refresh("[Modpack] ModPackInstaller.onInstalled", version)
                    installOperation = ModPackInstallOperation.Success
                    onStop()
                },
                onCancelled = {
                    doneFlag.set(true)
                    installer = null
                    installOperation = ModPackInstallOperation.None
                    onStop()
                },
                onError = { th ->
                    doneFlag.set(true)
                    installer = null
                    installOperation = ModPackInstallOperation.Error(th)
                    onStop()
                }
            )
        }
        onStart()
    }

    fun cancel() {
        installer?.cancelInstall()
        installer = null
        installOperation = ModPackInstallOperation.None
        versionNameOperation = VersionNameOperation.None
        confirmMobileDataOperation = ConfirmMobileDataOperation.None
    }

    override fun onCleared() {
        //⚠️ Activity 级 ViewModel：只有 Activity 真正销毁才会走到这里。
        //离开下载页不再销毁它，所以「输入版本名 / 确认移动网络」可以一直等到用户在主界面回答。
        //真的销毁时没有界面能回答对话框，只能收尾，避免协程永远挂起。
        val waitingForInput =
            versionNameOperation is VersionNameOperation.Waiting ||
                confirmMobileDataOperation is ConfirmMobileDataOperation.Waiting
        if (waitingForInput) {
            cancel()
        } else {
            installer = null
        }
    }
}
