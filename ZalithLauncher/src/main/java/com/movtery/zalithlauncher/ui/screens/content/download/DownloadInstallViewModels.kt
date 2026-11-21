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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskStage
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
import com.movtery.zalithlauncher.utils.network.isUsingMobileData
import com.movtery.zalithlauncher.viewmodel.ConfirmMobileDataOperation
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
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
 *
 * 议题 #78：用户要求「一次提交并同时下载/安装多个游戏本体、整合包、资源」。
 * 所以这里**不再是单槽状态**：
 * - 每个安装是一份独立会话（各自的 installer / 任务 / 取消入口），用 key（游戏版本名 / 整合包平台版本）区分；
 * - `installOperation` 从「一个值」变成**对话框队列**：多个安装同时完成时逐个弹，不会后一个把前一个顶掉；
 * - 「输入版本名 / 确认移动网络」这类要用户回答的交互也排队，同一时间只问一个，后面的等前面回答完。
 * 资源（模组/资源包/光影/存档）的单文件下载本来就走 TaskSystem 的独立任务，天然可并发。
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

/** 等待游戏安装器挂到会话上的最长时间：超过就认为这次安装没起来 */
private const val INSTALLER_ATTACH_TIMEOUT_MS = 10_000L

/** 任务镜像的轮询间隔：只用于把安装进度同步到全局任务面板 */
private const val MIRROR_POLL_INTERVAL_MS = 400L

/**
 * 一个游戏安装会话（议题 #78）
 *
 * 以前 installer / 进度镜像任务都挂在 ViewModel 的单个字段上，
 * 第二个安装会直接顶掉第一个，第一个的镜像任务读到 `installer == null` 就提前结束。
 * 现在每个安装各持一份，任务只观察自己的 installer，互不影响。
 */
private class GameInstallSession(
    /** 去重 / 取消用的 key：目标版本名 */
    val key: String,
    val onStop: () -> Unit
) {
    @Volatile
    var installer: GameInstaller? = null

    /** 成功/失败/取消/任务中断都只能收尾一次 */
    val finished = AtomicBoolean(false)
}

/**
 * 一个整合包安装会话（议题 #78），职责与 [GameInstallSession] 相同。
 */
private class ModPackInstallSession(
    /** 去重 / 取消用的 key：平台版本 id（快捷安装用 projectId） */
    val key: String,
    val onStop: () -> Unit
) {
    @Volatile
    var installer: ModPackInstaller? = null

    val finished = AtomicBoolean(false)
}

/** 等待用户输入版本名的请求（排队处理） */
private class VersionNameRequest(
    val info: ModPackInfo,
    val continuation: CancellableContinuation<String>
)

/** 等待用户确认移动网络的请求（排队处理） */
private class MobileDataRequest(
    val continuation: CancellableContinuation<Boolean>
)

/**
 * 游戏版本下载安装（Activity 级）
 */
class GameDownloadViewModel : ViewModel() {
    /**
     * 用于刷新游戏下载页面版本名称的检查
     */
    var versionNameErrorCheck by mutableStateOf(false)

    /**
     * 待处理的对话框队列：UI 只渲染队首，处理完一个再弹下一个。
     * 多个安装同时完成时不会互相覆盖（议题 #78）。
     */
    private val operations = mutableStateListOf<GameInstallOperation>()
    val installOperation: GameInstallOperation get() = operations.firstOrNull() ?: GameInstallOperation.None

    /** 正在进行的安装会话，key = 目标版本名 */
    private val sessions = mutableStateMapOf<String, GameInstallSession>()

    /**
     * 刷新游戏下载页面内的版本名称检查
     */
    private fun refreshVersionNameCheck() {
        versionNameErrorCheck = !versionNameErrorCheck
    }

    /**
     * 请求安装一个游戏版本（议题 #78 统一入口）
     *
     * 通知权限 / 移动网络之类的提醒先进对话框队列；不同版本可以同时提交，
     * 只有「同一目标版本已经在下载/安装」时才会拒绝并给出可读提示。
     */
    fun requestInstall(
        context: Context,
        info: GameDownloadInfo,
        onStart: () -> Unit = {},
        onStop: () -> Unit = {}
    ) {
        if (isBusy(info.customVersionName)) {
            operations.add(
                GameInstallOperation.Error(
                    IllegalStateException(
                        context.getString(
                            R.string.download_install_duplicate_game,
                            info.customVersionName
                        )
                    )
                )
            )
            return
        }
        when {
            !NotificationManager.checkNotificationEnabled(context) ->
                operations.add(GameInstallOperation.WarningForNotification(info))
            isUsingMobileData(context) ->
                operations.add(GameInstallOperation.WarningForMobileData(info))
            else -> startInstall(context, info, onStart, onStop)
        }
    }

    /**
     * 开始安装（用户已经确认完警告后调用，或不需要警告时直接调用）
     */
    fun startInstall(
        context: Context,
        info: GameDownloadInfo,
        onStart: () -> Unit = {},
        onStop: () -> Unit = {}
    ) {
        val key = info.customVersionName
        if (sessions.containsKey(key)) {
            operations.add(
                GameInstallOperation.Error(
                    IllegalStateException(
                        context.getString(R.string.download_install_duplicate_game, key)
                    )
                )
            )
            return
        }

        //前缀与 AI 工具侧的 `install_game_ai_` 区分，尾缀随机保证「取消后马上重装」也不会撞 id
        //（同 id 的 submitTask 会被 TaskSystem 当成重复任务静默忽略）
        val taskId = "install_game_ui_${info.gameVersion}_${key}_${UUID.randomUUID().toString().take(8)}"
        val session = GameInstallSession(
            key = key,
            onStop = onStop
        )
        sessions[key] = session
        onStart()

        //下载 / 安装跑在应用级作用域，离开下载页也不会被取消
        TaskSystem.submitTask(
            Task.runTask(
                id = taskId,
                task = { task ->
                    task.updateProgress(0f)
                    task.updateTitle(androidText(R.string.main_task_install_game, key))
                    //⚠️ installer 是在 submitTask **之后**才赋值的。
                    //   这里如果不等它挂上，第一次读到的就是 null，会被当成「已经装完」，
                    //   任务瞬间结束 —— 表现就是「点了下载，上面什么都不出现」。
                    var waited = 0
                    while (session.installer == null && waited < INSTALLER_ATTACH_TIMEOUT_MS) {
                        delay(100)
                        waited += 100
                    }
                    //只观察**本会话**的 installer：以前读全局单槽字段，第二个安装赋值后
                    //第一个的镜像任务立刻读到 null 并退出（议题 #78）
                    while (true) {
                        val current = session.installer
                        if (current == null) {
                            task.updateProgress(1f)
                            break
                        }
                        val list = current.tasksFlow.value
                        if (list.isNotEmpty()) {
                            val average = list
                                .map { it.task.progress.value.coerceIn(0f, 1f) }
                                .average()
                                .toFloat()
                            task.updateProgress(average)
                            list.firstOrNull { it.task.stage.value == TaskStage.RUNNING }
                                ?.let { task.updateMessage(it.title) }
                            //速度：同一阶段内的子任务是顺序跑的，最多一个在报速；把它透传到任务面板，
                            //让并行安装的每个任务都有自己独立的速度显示（议题 #78）
                            val speed = list.mapNotNull { it.task.rateBytesPerSec.value }.sum()
                            if (speed > 0L) task.updateSpeed(speed) else task.clearSpeed()
                        }
                        delay(MIRROR_POLL_INTERVAL_MS)
                    }
                },
                onCancel = {
                    //从顶部任务面板取消：只停掉这次安装，别动其它并行安装
                    runCatching { session.installer?.cancelInstall() }
                },
                onFinally = {
                    //兜底：任务被整体取消（例如设置里「取消全部任务」）时安装器不会回调，
                    //不在这里复位的话界面会永远停在「安装中」，按钮全部失效
                    finishSession(session)
                }
            )
        )

        //⚠️ 先把 installer 挂到会话上再启动：GameAlreadyInstalled 这类回调可能在
        //installGame() 返回前就在别的线程触发收尾；如果之后再赋值，会把已结束的会话重新挂上一个死安装器，
        //镜像任务就永远等不到 installer==null（任务卡在 0%）
        val gameInstaller = runCatching {
            GameInstaller(context, info, TaskSystem.appScope)
        }.getOrElse { th ->
            //构造失败（例如缓存/游戏目录建不出来）：任务已经提交，必须收尾并告知用户，
            //不能让异常从点击回调里抛出去把启动器打崩
            finishSession(session)
            operations.add(GameInstallOperation.Error(th))
            return
        }
        session.installer = gameInstaller
        gameInstaller.installGame(
            onInstalled = { version ->
                //先收尾会话（置 finished、摘掉 mirror），再给用户弹成功
                completeSession(session)
                VersionsManager.refresh("[DownloadGame] GameInstaller.onInstalled", version)
                operations.add(GameInstallOperation.Success)
            },
            onError = { th ->
                completeSession(session)
                operations.add(GameInstallOperation.Error(th))
            },
            onGameAlreadyInstalled = {
                //很有可能发生在刚安装完成，再次点击安装按钮时。
                //必须把会话收尾，否则下一次安装会被当成「已有安装在进行」永远拒绝
                completeSession(session)
                operations.add(
                    GameInstallOperation.Error(
                        IllegalStateException(
                            context.getString(
                                R.string.download_install_already_installed,
                                key
                            )
                        )
                    )
                )
            },
            onCancelled = {
                completeSession(session)
            }
        )
    }

    /**
     * 安装器自然结束（成功/失败/取消/目标已存在）：只收尾自己的会话。
     * 这里**不能** cancelInstall：成功路径取消安装器会把已经装好的版本目录回滚掉。
     */
    private fun completeSession(session: GameInstallSession) {
        if (!session.finished.compareAndSet(false, true)) return
        session.installer = null
        sessions.remove(session.key)
        refreshVersionNameCheck()
        releaseKeepScreenIfIdle(session)
    }

    /**
     * 任务结束兜底（任何路径都会走到）：任务被取消 / 整体中断时才真正停掉安装器。
     */
    private fun finishSession(session: GameInstallSession) {
        if (!session.finished.compareAndSet(false, true)) return
        //走到这里说明不是安装器自己回调结束的（否则 finished 已经是 true）：
        //必须把真正的安装停掉，否则会出现「界面说取消了、后台还在写文件」
        runCatching { session.installer?.cancelInstall() }
        session.installer = null
        sessions.remove(session.key)
        refreshVersionNameCheck()
        releaseKeepScreenIfIdle(session)
    }

    /** 所有安装都结束后才能放开「保持屏幕常亮」，否则一个安装完成就会让别的安装期间熄屏 */
    private fun releaseKeepScreenIfIdle(session: GameInstallSession) {
        if (sessions.isNotEmpty()) return
        runCatching { session.onStop() }
    }

    /**
     * @return 同一目标版本是否已有安装在进行，或正在排队等用户确认警告
     */
    private fun isBusy(key: String): Boolean {
        if (sessions.containsKey(key)) return true
        return operations.any { operation ->
            when (operation) {
                is GameInstallOperation.WarningForNotification -> operation.info.customVersionName == key
                is GameInstallOperation.WarningForMobileData -> operation.info.customVersionName == key
                else -> false
            }
        }
    }

    /**
     * 处理掉队首的一个操作（弹窗关闭 / 用户确认后调用）
     */
    fun dismissOperation(operation: GameInstallOperation) {
        operations.remove(operation)
    }

    /**
     * 取消全部正在进行的游戏安装（旧入口保留：目前没有界面调用，避免外部调用点编译不过）
     */
    fun cancel() {
        sessions.values.toList().forEach { session ->
            if (session.finished.compareAndSet(false, true)) {
                runCatching { session.installer?.cancelInstall() }
                session.installer = null
                sessions.remove(session.key)
            }
        }
        operations.clear()
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
    /** 待处理的对话框队列：UI 只渲染队首，避免多个安装的提示互相覆盖（议题 #78） */
    private val operations = mutableStateListOf<ModPackInstallOperation>()
    val installOperation: ModPackInstallOperation get() = operations.firstOrNull() ?: ModPackInstallOperation.None

    /** 正在进行的整合包安装会话，key = 平台版本 id（快捷安装用 projectId） */
    private val sessions = mutableStateMapOf<String, ModPackInstallSession>()

    //等待用户输入版本名称相关：多个整合包可能要名字，排队问
    private val versionNameRequests = mutableStateListOf<VersionNameRequest>()
    val versionNameOperation: VersionNameOperation
        get() = versionNameRequests.firstOrNull()
            ?.let { VersionNameOperation.Waiting(it.info) }
            ?: VersionNameOperation.None

    //警告使用移动网络相关：同样排队
    private val mobileDataRequests = mutableStateListOf<MobileDataRequest>()
    val confirmMobileDataOperation: ConfirmMobileDataOperation
        get() = if (mobileDataRequests.isEmpty()) {
            ConfirmMobileDataOperation.None
        } else {
            ConfirmMobileDataOperation.Waiting
        }

    /**
     * 等待用户输入版本名称（每个整合包安装各一个请求，排队处理）
     */
    private suspend fun waitForVersionName(modPackInfo: ModPackInfo): String {
        return suspendCancellableCoroutine { cont ->
            val request = VersionNameRequest(modPackInfo, cont)
            versionNameRequests.add(request)
            cont.invokeOnCancellation { versionNameRequests.remove(request) }
        }
    }

    /**
     * 用户确认输入版本名称：只回答队首那一个，后面的继续排队
     */
    fun confirmVersionName(name: String) {
        val request = versionNameRequests.firstOrNull() ?: return
        versionNameRequests.remove(request)
        request.continuation.resume(name)
    }

    /**
     * 用户取消了版本名称输入：只取消队首那一个安装
     */
    fun cancelVersionName() {
        val request = versionNameRequests.firstOrNull() ?: return
        versionNameRequests.remove(request)
        request.continuation.cancel(CancellationException("User cancelled the version name input"))
    }

    private suspend fun waitForConfirmMobileData(): Boolean {
        return suspendCancellableCoroutine { cont ->
            val request = MobileDataRequest(cont)
            mobileDataRequests.add(request)
            cont.invokeOnCancellation { mobileDataRequests.remove(request) }
        }
    }

    /**
     * 用户是否确认使用移动网络：同样只回答队首那一个
     */
    fun confirmUseMobileData(use: Boolean) {
        val request = mobileDataRequests.firstOrNull() ?: return
        mobileDataRequests.remove(request)
        request.continuation.resume(use)
    }

    /**
     * 搜索结果卡片的一键下载
     *
     * 结果卡片上只有一个项目，没有「选版本」这一步，所以先取该项目的最新版本，
     * 再交给现有链路（通知权限 → 兼容性警告 → 后台安装）。
     * 主页与「版本设置 → 资源管理」两个入口都可以用。
     *
     * 议题 #78：这里以前 `if (installOperation != None) return` 会挡掉第二个整合包下载，
     * 现在只有「这个项目已经在装」才拒绝。
     */
    fun quickInstall(
        context: Context,
        platform: Platform,
        projectId: String,
        iconUrl: String?
    ) {
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
                        operations.add(
                            ModPackInstallOperation.Error(
                                IllegalStateException(
                                    context.getString(R.string.download_modpack_no_version)
                                )
                            )
                        )
                        return@onSuccess
                    }
                    requestInstall(
                        context = context,
                        version = version,
                        iconUrl = iconUrl
                    )
                }
                .onFailure { th ->
                    //切换整合包导致的协程取消不是「取版本失败」，不要弹错误框
                    if (th is kotlinx.coroutines.CancellationException) throw th
                    Logger.warning("InstallModpack", "Failed to fetch modpack versions.", th)
                    operations.add(ModPackInstallOperation.Error(th))
                }
        }
    }

    /**
     * 请求安装一个整合包版本（议题 #78 统一入口）
     *
     * 不同整合包版本可以同时提交；只有「同一个整合包版本已经在下载/安装」时拒绝。
     * 通知权限 / 兼容性提醒先进对话框队列，用户确认后由弹窗调用 [startInstall]。
     */
    fun requestInstall(
        context: Context,
        version: PlatformVersion,
        iconUrl: String?
    ) {
        if (isBusy(version.platformId())) {
            operations.add(
                ModPackInstallOperation.Error(
                    IllegalStateException(
                        context.getString(
                            R.string.download_install_duplicate_modpack,
                            version.platformDisplayName()
                        )
                    )
                )
            )
            return
        }
        operations.add(
            if (!NotificationManager.checkNotificationEnabled(context)) {
                ModPackInstallOperation.WarningForNotification(version, iconUrl)
            } else {
                ModPackInstallOperation.Warning(version, iconUrl)
            }
        )
    }

    /**
     * 用户处理完通知权限提醒后，排入整合包兼容性确认（议题 #78：队列里逐个处理）
     */
    fun requestCompatibilityWarning(version: PlatformVersion, iconUrl: String?) {
        operations.add(ModPackInstallOperation.Warning(version, iconUrl))
    }

    /**
     * 真正开始安装（用户已经确认完警告后由弹窗调用）
     */
    fun startInstall(
        context: Context,
        version: PlatformVersion,
        iconUrl: String?,
        onStart: () -> Unit = {},
        onStop: () -> Unit = {}
    ) {
        val key = version.platformId()
        if (sessions.containsKey(key)) {
            operations.add(
                ModPackInstallOperation.Error(
                    IllegalStateException(
                        context.getString(
                            R.string.download_install_duplicate_modpack,
                            version.platformDisplayName()
                        )
                    )
                )
            )
            return
        }

        //UI 任务 id 前缀与其它入口区分，避免撞 id 被 TaskSystem 静默丢弃
        val taskId = "install_modpack_ui_${key}_${UUID.randomUUID().toString().take(8)}"
        val session = ModPackInstallSession(
            key = key,
            onStop = onStop
        )
        sessions[key] = session
        onStart()

        //把安装过程镜像到全局任务队列：这样任务面板里有进度、能取消，
        //不需要一个挡住整个界面的进度弹窗（维护者要的是后台静默下载）。
        TaskSystem.submitTask(
            Task.runTask(
                id = taskId,
                task = { task ->
                    task.updateProgress(-1f)
                    task.updateTitle(androidText(R.string.download_modpack_install_title))
                    var waited = 0
                    while (session.installer == null && waited < INSTALLER_ATTACH_TIMEOUT_MS) {
                        delay(100)
                        waited += 100
                    }
                    //只观察本会话的安装器，多个整合包并行时互不干扰（议题 #78）
                    while (true) {
                        val current = session.installer
                        if (current == null) {
                            task.updateProgress(1f)
                            break
                        }
                        val list = current.tasksFlow.value
                        if (list.isNotEmpty()) {
                            val average = list
                                .map { it.task.progress.value.coerceIn(0f, 1f) }
                                .average()
                                .toFloat()
                            task.updateProgress(average)
                            list.firstOrNull { it.task.stage.value == TaskStage.RUNNING }
                                ?.let { task.updateMessage(it.title) }
                            //速度：同一阶段内的子任务是顺序跑的，最多一个在报速；把它透传到任务面板，
                            //让并行安装的每个任务都有自己独立的速度显示（议题 #78）
                            val speed = list.mapNotNull { it.task.rateBytesPerSec.value }.sum()
                            if (speed > 0L) task.updateSpeed(speed) else task.clearSpeed()
                        }
                        delay(MIRROR_POLL_INTERVAL_MS)
                    }
                },
                onCancel = {
                    //只取消这次安装
                    runCatching { session.installer?.cancelInstall() }
                },
                onFinally = {
                    finishSession(session)
                }
            )
        )

        //⚠️ 同样先挂到会话再启动，避免快速失败的回调被后面的赋值覆盖
        val modPackInstaller = ModPackInstaller(
            context = context,
            version = version,
            iconUrl = iconUrl,
            //安装跑在 App 级作用域：退出下载页、切后台都继续，不要绑在页面 ViewModel 上
            scope = TaskSystem.appScope,
            waitForVersionName = ::waitForVersionName,
            waitForConfirmMobileData = ::waitForConfirmMobileData
        )
        session.installer = modPackInstaller
        modPackInstaller.installModPack(
            onInstalled = { installedVersion ->
                completeSession(session)
                VersionsManager.refresh("[Modpack] ModPackInstaller.onInstalled", installedVersion)
                operations.add(ModPackInstallOperation.Success)
            },
            onCancelled = {
                completeSession(session)
            },
            onError = { th ->
                completeSession(session)
                operations.add(ModPackInstallOperation.Error(th))
            }
        )
    }

    /**
     * 安装器自然结束（成功/失败/取消）：只收尾自己的会话。
     * 这里**不能** cancelInstall（成功路径取消会把已装好的版本回滚掉）。
     */
    private fun completeSession(session: ModPackInstallSession) {
        if (!session.finished.compareAndSet(false, true)) return
        session.installer = null
        sessions.remove(session.key)
        releaseKeepScreenIfIdle(session)
    }

    /** 任务结束兜底：任务被取消 / 整体中断时才真正停掉安装器 */
    private fun finishSession(session: ModPackInstallSession) {
        if (!session.finished.compareAndSet(false, true)) return
        runCatching { session.installer?.cancelInstall() }
        session.installer = null
        sessions.remove(session.key)
        releaseKeepScreenIfIdle(session)
    }

    private fun releaseKeepScreenIfIdle(session: ModPackInstallSession) {
        if (sessions.isNotEmpty()) return
        runCatching { session.onStop() }
    }

    /** @return 同一个整合包版本是否已有安装在进行，或正在排队等用户确认警告 */
    private fun isBusy(key: String): Boolean {
        if (sessions.containsKey(key)) return true
        return operations.any { operation ->
            when (operation) {
                is ModPackInstallOperation.Warning -> operation.version.platformId() == key
                is ModPackInstallOperation.WarningForNotification -> operation.version.platformId() == key
                else -> false
            }
        }
    }

    /** 处理掉队首的一个操作（弹窗关闭 / 用户确认后调用） */
    fun dismissOperation(operation: ModPackInstallOperation) {
        operations.remove(operation)
    }

    /**
     * 取消全部正在进行的整合包安装（旧入口保留：避免外部调用点编译不过）
     */
    fun cancel() {
        sessions.values.toList().forEach { session ->
            if (session.finished.compareAndSet(false, true)) {
                runCatching { session.installer?.cancelInstall() }
                session.installer = null
                sessions.remove(session.key)
            }
        }
        cancelVersionName()
        mobileDataRequests.toList().forEach { request ->
            mobileDataRequests.remove(request)
            request.continuation.cancel(CancellationException("Cancelled by the user"))
        }
        operations.clear()
    }

    override fun onCleared() {
        //⚠️ Activity 级 ViewModel：只有 Activity 真正销毁才会走到这里。
        //离开下载页不再销毁它，所以「输入版本名 / 确认移动网络」可以一直等到用户在主界面回答。
        //真的销毁时没有界面能回答对话框，只能收尾这些挂起的协程，避免永远挂起。
        versionNameRequests.toList().forEach { request ->
            versionNameRequests.remove(request)
            request.continuation.cancel(CancellationException("The activity has been destroyed"))
        }
        mobileDataRequests.toList().forEach { request ->
            mobileDataRequests.remove(request)
            request.continuation.cancel(CancellationException("The activity has been destroyed"))
        }
        //不主动取消安装：安装跑在 TaskSystem.appScope 上，是「后台静默」的
    }
}
