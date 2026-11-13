/*
 * Zalith Launcher 2
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

package com.movtery.zalithlauncher.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.download.jvm_server.JvmCrashException
import com.movtery.zalithlauncher.game.download.jvm_server.isProcessStartRefused
import com.movtery.zalithlauncher.game.download.modpack.install.ModpackImporter
import com.movtery.zalithlauncher.game.download.modpack.install.PackNotSupportedException
import com.movtery.zalithlauncher.game.download.modpack.install.UnsupportedPackReason
import com.movtery.zalithlauncher.game.download.modpack.platform.PackPlatform
import com.movtery.zalithlauncher.game.version.download.DownloadFailedException
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.content.download.ModpackVersionNameDialog
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.PackIdentifier
import com.movtery.zalithlauncher.ui.screens.content.elements.TitleTaskFlowDialog
import com.movtery.zalithlauncher.utils.logging.Logger
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.delay
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import java.util.concurrent.TimeoutException
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

private const val TAG = "ModpackImportVM"

/** 导入整合包相关操作 */
sealed interface ModpackImportOperation {
    data object None : ModpackImportOperation
    /** 开始导入整合包 */
    data object Import : ModpackImportOperation
    /** 不支持的整合包或格式无效 */
    data class NotSupport(val reason: UnsupportedPackReason) : ModpackImportOperation
    /** 整合包导入完成 */
    data object Finished : ModpackImportOperation
    /** 导入整合包时出现异常 */
    data class Error(val th: Throwable) : ModpackImportOperation
}

/** 整合包版本名称自定义状态操作 */
sealed interface VersionNameOperation {
    data object None : VersionNameOperation
    /** 等待用户输入版本名称 */
    data class Waiting(val name: String) : VersionNameOperation
}

/** 整合包安装确认使用移动网络状态操作 */
sealed interface ConfirmMobileDataOperation {
    data object None : ConfirmMobileDataOperation
    /** 等待用户确认使用移动网络 */
    data object Waiting : ConfirmMobileDataOperation
}

/**
 * 导入整合包ViewModel
 */
class ModpackImportViewModel : ViewModel() {
    var importOperation by mutableStateOf<ModpackImportOperation>(ModpackImportOperation.None)
    var versionNameOperation by mutableStateOf<VersionNameOperation>(VersionNameOperation.None)
    var confirmMobileDataOperation by mutableStateOf<ConfirmMobileDataOperation>(ConfirmMobileDataOperation.None)

    //等待用户输入版本名称相关
    private var versionNameContinuation: CancellableContinuation<String>? = null
    /** 导入器建议的实例名：ViewModel 被清掉时用它兜底 */
    private var pendingSuggestedName: String? = null

    suspend fun waitForVersionName(name: String): String {
        pendingSuggestedName = name
        return suspendCancellableCoroutine { cont ->
            versionNameContinuation = cont
            versionNameOperation = VersionNameOperation.Waiting(name)
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
    private var confirmMobileData: CancellableContinuation<Boolean>? = null
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
     * 整合包导入器
     */
    var importer by mutableStateOf<ModpackImporter?>(null)

    /**
     * 开始导入整合包
     */
    fun import(
        context: Context,
        uri: Uri,
        onStart: () -> Unit = {},
        onStop: () -> Unit = {}
    ) {
        if (importOperation != ModpackImportOperation.None) {
            //当前有别的导入任务，拒绝这次导入
            return
        }
        importOperation = ModpackImportOperation.Import

        //⚠️ 用**应用级作用域**跑导入：
        //维护者要求「后台静默」——离开页面、切后台、甚至 Activity 重建（旋转 / 分屏）都不能中断，
        //否则用户挑完整合包随手切个微信，回来发现又得重来。
        val newImporter = ModpackImporter(
            context = context,
            uri = uri,
            scope = TaskSystem.appScope,
            waitForVersionName = ::waitForVersionName,
            waitForConfirmMobileData = ::waitForConfirmMobileData
        )
        importer = newImporter

        //进度不再用弹窗挡住用户：交给全局任务队列，去任务面板里看进度、也能在那里取消。
        //（与 `ModPackTools.installModPackWithProgress` 同一套做法：AI 发起的安装和手动导入行为一致）
        val done = java.util.concurrent.atomic.AtomicBoolean(false)
        val cancelled = java.util.concurrent.atomic.AtomicBoolean(false)
        val lastVersion = java.util.concurrent.atomic.AtomicReference<String?>(null)
        val failure = java.util.concurrent.atomic.AtomicReference<Throwable?>(null)

        TaskSystem.submitTask(
            Task.runTask(
                id = "import_modpack_${System.currentTimeMillis()}",
                task = { task ->
                    task.updateProgress(-1f)
                    task.updateTitle(androidText(R.string.import_modpack))
                    task.updateMessage(androidText(R.string.import_modpack))

                    newImporter.startImport(
                        isRunning = {},
                        onFinished = { version ->
                            lastVersion.set(version)
                            done.set(true)
                        },
                        onCancelled = {
                            cancelled.set(true)
                            done.set(true)
                        },
                        onError = { th ->
                            failure.set(th)
                            done.set(true)
                        }
                    )

                    //镜像导入器内部各阶段的进度：任务面板里那条进度就是用户能看到的全部反馈
                    while (!done.get()) {
                        val list = newImporter.taskFlow.value
                        if (list.isNotEmpty()) {
                            val average = list
                                .map { it.task.progress.value.coerceIn(0f, 1f) }
                                .average()
                                .toFloat()
                            task.updateProgress(average)
                            list.firstOrNull { it.task.stage.value.name == "RUNNING" }
                                ?.let { task.updateMessage(it.title) }
                        }
                        delay(500)
                    }
                },
                onCancel = {
                    //从任务面板取消：必须把真正的导入器也停掉，
                    //否则会变成「界面说取消了、后台还在写文件」
                    runCatching { newImporter.cancel() }
                },
                onFinally = {
                    //被取消时不会走 onError，这里兜底
                    if (!done.get() && failure.get() == null && !cancelled.get()) {
                        cancelled.set(true)
                    }
                    importer = null
                    if (cancelled.get()) {
                        importOperation = ModpackImportOperation.None
                    } else {
                        val version = lastVersion.get()
                        val th = failure.get()
                        if (th != null) {
                            importOperation = if (th is PackNotSupportedException) {
                                //整合包不受支持，无法导入
                                ModpackImportOperation.NotSupport(th.reason)
                            } else {
                                ModpackImportOperation.Error(th)
                            }
                        } else if (version != null) {
                            VersionsManager.refresh("[Modpack] ModpackImporter.onFinished", version)
                            importOperation = ModpackImportOperation.Finished
                        } else {
                            importOperation = ModpackImportOperation.None
                        }
                    }
                    onStop()
                }
            )
        )
        onStart()
    }

    fun cancel() {
        importer?.cancel()
        importer = null
        importOperation = ModpackImportOperation.None
        versionNameOperation = VersionNameOperation.None
        confirmMobileDataOperation = ConfirmMobileDataOperation.None
    }

    /**
     * ⚠️ 这里**不再 cancel()**。
     *
     * 导入跑在 [TaskSystem.appScope] 上，Activity 重建（旋转、分屏、改语言）时
     * ViewModel 会被清掉，但任务应当继续；顺手取消会导致「什么都没干，导入自己没了」。
     * 只把「等用户输入」的挂起收个尾，免得协程永远挂着：
     * - 版本名：直接用导入器建议的名字（用户本来也只是想改个名）
     * - 移动网络：按同意处理（用户是主动开始导入的）
     */
    override fun onCleared() {
        versionNameContinuation?.takeIf { it.isActive }?.resume(pendingSuggestedName ?: "")
        versionNameContinuation = null
        confirmMobileData?.takeIf { it.isActive }?.resume(true)
        confirmMobileData = null
        versionNameOperation = VersionNameOperation.None
        confirmMobileDataOperation = ConfirmMobileDataOperation.None
    }
}

@Composable
fun ModpackImportOperation(
    operation: ModpackImportOperation,
    changeOperation: (ModpackImportOperation) -> Unit,
    importer: ModpackImporter?,
    onCancel: () -> Unit
) {
    when (operation) {
        is ModpackImportOperation.None -> {}
        is ModpackImportOperation.Import -> {
            //⚠️ 这里**故意不弹进度窗**：
            //导入已经交给全局任务队列（见 `import()`），用户可以在任务面板里看进度、取消，
            //同时继续挑别的整合包或干别的事——「后台静默」要的就是这个。
            //`importer` 参数保留是为了兼容调用点与后续可能的「点任务跳到导入页」。
        }
        is ModpackImportOperation.NotSupport -> {
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Text(text = stringResource(R.string.import_modpack_not_supported_title))
                },
                text = {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fadeEdge(state = scrollState)
                            .verticalScrollWithBar(state = scrollState),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        when (operation.reason) {
                            UnsupportedPackReason.CorruptedArchive -> {
                                //因文件无法解压导致的无法导入
                                Text(text = stringResource(R.string.import_modpack_not_supported_text1))

                                Text(text = stringResource(R.string.import_modpack_not_supported_text2))
                                Text(text = stringResource(R.string.import_modpack_not_supported_text3))

                                Text(text = stringResource(R.string.import_modpack_not_supported_text4))
                            }
                            UnsupportedPackReason.UnsupportedFormat -> {
                                //启动器确实不支持这个格式
                                Text(text = stringResource(R.string.import_modpack_not_supported_formats))
                                AllSupportPackDisplay(modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            changeOperation(ModpackImportOperation.None)
                        }
                    ) {
                        MarqueeText(text = stringResource(R.string.generic_confirm))
                    }
                }
            )
        }
        is ModpackImportOperation.Finished -> {
            SimpleAlertDialog(
                title = stringResource(R.string.import_modpack_finished_title),
                text = stringResource(R.string.import_modpack_finished_text)
            ) {
                changeOperation(ModpackImportOperation.None)
            }
        }
        is ModpackImportOperation.Error -> {
            val th = operation.th
            Logger.error(TAG, "Failed to download the game!", th)
            val message = when (th) {
                is HttpRequestTimeoutException, is SocketTimeoutException, is TimeoutException -> stringResource(R.string.error_timeout)
                is UnknownHostException, is UnresolvedAddressException -> stringResource(R.string.error_network_unreachable)
                is ConnectException -> stringResource(R.string.error_connection_failed)
                is SerializationException, is JsonSyntaxException -> stringResource(R.string.error_parse_failed)
                is JvmCrashException -> stringResource(R.string.download_install_error_jvm_crash, th.code)
                is DownloadFailedException -> stringResource(R.string.download_install_error_download_failed)
                else -> when {
                    th.isProcessStartRefused() -> stringResource(R.string.download_install_error_process_start)
                    else -> th.localizedMessage ?: th.message ?: th::class.qualifiedName ?: "Unknown error"
                }
            }
            val dismiss = {
                changeOperation(ModpackImportOperation.None)
            }
            AlertDialog(
                onDismissRequest = dismiss,
                title = {
                    Text(text = stringResource(R.string.import_modpack_failed_title))
                },
                text = {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fadeEdge(state = scrollState)
                            .verticalScrollWithBar(state = scrollState),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = stringResource(R.string.import_modpack_failed_text))
                        Text(text = message)
                    }
                },
                confirmButton = {
                    Button(onClick = dismiss) {
                        MarqueeText(text = stringResource(R.string.generic_confirm))
                    }
                }
            )
        }
    }
}

/**
 * 所有支持的整合包格式展示
 */
@Composable
fun AllSupportPackDisplay(
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PackPlatform.entries.forEach { platform ->
            PackIdentifier(
                platform = platform
            )
        }
    }
}

@Composable
fun ModpackVersionNameOperation(
    operation: VersionNameOperation,
    onConfirmVersionName: (String) -> Unit,
    onCancel: () -> Unit
) {
    when (operation) {
        is VersionNameOperation.None -> {}
        is VersionNameOperation.Waiting -> {
            ModpackVersionNameDialog(
                name = operation.name,
                onConfirmVersionName = onConfirmVersionName,
                onCancel = onCancel
            )
        }
    }
}

@Composable
fun ModpackConfirmUseMobileDataOperation(
    operation: ConfirmMobileDataOperation,
    onConfirmUse: (Boolean) -> Unit
) {
    when (operation) {
        is ConfirmMobileDataOperation.None -> {}
        is ConfirmMobileDataOperation.Waiting -> {
            SimpleAlertDialog(
                title = stringResource(R.string.generic_warning),
                text = stringResource(R.string.download_install_warning_mobile_data),
                confirmText = stringResource(R.string.generic_anyway),
                onDismiss = {
                    onConfirmUse(false)
                },
                onConfirm = {
                    //用户坚持使用移动网络
                    onConfirmUse(true)
                }
            )
        }
    }
}