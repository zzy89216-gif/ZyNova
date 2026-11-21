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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.gson.JsonSyntaxException
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.game.GameDownloadInfo
import com.movtery.zalithlauncher.game.download.game.optifine.CantFetchingOptiFineUrlException
import com.movtery.zalithlauncher.game.download.jvm_server.JvmCrashException
import com.movtery.zalithlauncher.game.download.jvm_server.isProcessStartRefused
import com.movtery.zalithlauncher.game.version.download.DownloadFailedException
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.NotificationCheck
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleEditDialog
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.content.elements.isFilenameInvalid
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.ModpackConfirmUseMobileDataOperation
import com.movtery.zalithlauncher.viewmodel.sendKeepScreen
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.serialization.SerializationException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.channels.UnresolvedAddressException
import java.util.concurrent.TimeoutException

/**
 * 下载 / 安装的全局弹窗宿主（议题 #72）
 *
 * 以前这些弹窗写在各下载页的 composable 里，用户离开下载页就看不到了：
 * 安装成功的提示要回到下载页才弹，整合包「输入版本名」在主界面时永远等不到答案。
 * 现在统一由 MainActivity 在顶层渲染（与整合包导入弹窗同一套做法），
 * 状态则放在 Activity 级的 ViewModel 里，任何界面都不影响下载与弹窗。
 *
 * 议题 #78：多个安装可以同时进行，所以 ViewModel 里是一个**对话框队列**。
 * 这里每次只渲染队首那一个操作，处理完（关闭 / 确认）再从队列里移除，
 * 两个安装同时完成时提示会逐个出现，不会互相覆盖。
 */

// ------------------------------------------------------------------ 游戏版本安装

@Composable
fun GameDownloadInstallDialogs(
    viewModel: GameDownloadViewModel,
    eventViewModel: EventViewModel
) {
    val context = LocalContext.current

    fun startInstall(info: GameDownloadInfo) {
        viewModel.startInstall(
            context = context,
            info = info,
            onStart = {
                eventViewModel.sendKeepScreen(true)
            },
            onStop = {
                eventViewModel.sendKeepScreen(false)
            }
        )
    }

    when (val operation = viewModel.installOperation) {
        is GameInstallOperation.None -> {}
        is GameInstallOperation.WarningForNotification -> {
            NotificationCheck(
                text = stringResource(R.string.notification_data_jvm_service_message),
                onGranted = {
                    //权限被授予，开始安装
                    viewModel.dismissOperation(operation)
                    startInstall(operation.info)
                },
                onIgnore = {
                    //用户不想授权，但是支持继续进行安装
                    viewModel.dismissOperation(operation)
                    startInstall(operation.info)
                },
                onDismiss = {
                    viewModel.dismissOperation(operation)
                }
            )
        }
        is GameInstallOperation.WarningForMobileData -> {
            SimpleAlertDialog(
                title = stringResource(R.string.generic_warning),
                text = stringResource(R.string.download_install_warning_mobile_data),
                confirmText = stringResource(R.string.generic_anyway),
                onDismiss = {
                    viewModel.dismissOperation(operation)
                },
                onConfirm = {
                    //用户坚持使用移动网络
                    viewModel.dismissOperation(operation)
                    startInstall(operation.info)
                }
            )
        }
        is GameInstallOperation.Install -> {
            //安装时不再弹「安装游戏中」对话框，进度统一在顶部任务进度条里
        }
        is GameInstallOperation.Error -> {
            val th = operation.th
            Logger.error("InstallGame", "Failed to download the game!", th)
            val message = when (th) {
                is HttpRequestTimeoutException, is SocketTimeoutException, is TimeoutException -> stringResource(R.string.error_timeout)
                is UnknownHostException, is UnresolvedAddressException -> stringResource(R.string.error_network_unreachable)
                is ConnectException -> stringResource(R.string.error_connection_failed)
                is SerializationException, is JsonSyntaxException -> stringResource(R.string.error_parse_failed)
                is CantFetchingOptiFineUrlException -> stringResource(R.string.download_install_error_cant_fetch_optifine_download_url)
                is JvmCrashException -> stringResource(R.string.download_install_error_jvm_crash, th.code)
                is DownloadFailedException -> stringResource(R.string.download_install_error_download_failed)
                else -> when {
                    th.isProcessStartRefused() -> stringResource(R.string.download_install_error_process_start)
                    else -> th.localizedMessage ?: th.message ?: th::class.qualifiedName ?: "Unknown error"
                }
            }
            val dismiss = {
                viewModel.dismissOperation(operation)
            }
            AlertDialog(
                onDismissRequest = dismiss,
                title = {
                    Text(text = stringResource(R.string.download_install_error_title))
                },
                text = {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fadeEdge(state = scrollState)
                            .verticalScrollWithBar(state = scrollState),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = stringResource(R.string.download_install_error_message))
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
        is GameInstallOperation.Success -> {
            SimpleAlertDialog(
                title = stringResource(R.string.download_install_success_title),
                text = stringResource(R.string.download_install_success_message)
            ) {
                viewModel.dismissOperation(operation)
            }
        }
    }
}

// ------------------------------------------------------------------ 整合包安装

@Composable
fun ModPackDownloadInstallDialogs(
    viewModel: ModPackViewModel,
    eventViewModel: EventViewModel
) {
    val context = LocalContext.current

    ModPackInstallOperation(
        operation = viewModel.installOperation,
        dismissOperation = { viewModel.dismissOperation(it) },
        onWarn = { version, iconUrl -> viewModel.requestCompatibilityWarning(version, iconUrl) },
        onInstall = { version, iconUrl ->
            viewModel.startInstall(
                context = context,
                version = version,
                iconUrl = iconUrl,
                onStart = {
                    eventViewModel.sendKeepScreen(true)
                },
                onStop = {
                    eventViewModel.sendKeepScreen(false)
                }
            )
        }
    )

    //用户确认版本名称 操作流程
    VersionNameOperation(
        operation = viewModel.versionNameOperation,
        onConfirmVersionName = { name ->
            viewModel.confirmVersionName(name)
        },
        onCancel = {
            //只取消正在等名字的那个整合包安装，不影响其它并行安装
            viewModel.cancelVersionName()
        }
    )

    //用户确认使用移动网络 操作流程
    ModpackConfirmUseMobileDataOperation(
        operation = viewModel.confirmMobileDataOperation,
        onConfirmUse = { use ->
            viewModel.confirmUseMobileData(use)
        }
    )
}

@Composable
private fun ModPackInstallOperation(
    operation: ModPackInstallOperation,
    dismissOperation: (ModPackInstallOperation) -> Unit,
    onWarn: (PlatformVersion, iconUrl: String?) -> Unit,
    onInstall: (PlatformVersion, iconUrl: String?) -> Unit
) {
    when (operation) {
        is ModPackInstallOperation.None -> {}
        is ModPackInstallOperation.WarningForNotification -> {
            NotificationCheck(
                text = stringResource(R.string.notification_data_jvm_service_message),
                onGranted = {
                    //权限被授予：把这条通知权限提醒处理掉，接着排入兼容性确认
                    dismissOperation(operation)
                    onWarn(operation.version, operation.iconUrl)
                },
                onIgnore = {
                    //用户不想授权，但是支持继续进行安装
                    dismissOperation(operation)
                    onWarn(operation.version, operation.iconUrl)
                },
                onDismiss = {
                    dismissOperation(operation)
                }
            )
        }
        is ModPackInstallOperation.Warning -> {
            //警告整合包的兼容性（免责声明）
            SimpleAlertDialog(
                title = stringResource(R.string.generic_warning),
                text = {
                    Text(text = stringResource(R.string.download_modpack_warning1))
                    Text(text = stringResource(R.string.download_modpack_warning2))

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.download_modpack_warning3),
                        fontWeight = FontWeight.Bold
                    )
                },
                confirmText = stringResource(R.string.download_install),
                onCancel = {
                    dismissOperation(operation)
                },
                onConfirm = {
                    dismissOperation(operation)
                    onInstall(operation.version, operation.iconUrl)
                }
            )
        }
        is ModPackInstallOperation.Install -> {
            //⚠️ 这里**故意不再弹进度弹窗**（维护者要求「后台静默」）：
            //安装已经提交到全局任务队列，用户可以在任务面板里看进度 / 取消
        }
        is ModPackInstallOperation.Error -> {
            val th = operation.th
            Logger.error("InstallModpack", "Failed to download the game!", th)
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
                dismissOperation(operation)
            }
            AlertDialog(
                onDismissRequest = dismiss,
                title = {
                    Text(text = stringResource(R.string.download_install_error_title))
                },
                text = {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fadeEdge(state = scrollState)
                            .verticalScrollWithBar(state = scrollState),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = stringResource(R.string.download_install_error_message))
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
        is ModPackInstallOperation.Success -> {
            SimpleAlertDialog(
                title = stringResource(R.string.download_install_success_title),
                text = stringResource(R.string.download_install_success_message)
            ) {
                dismissOperation(operation)
            }
        }
    }
}

@Composable
private fun VersionNameOperation(
    operation: VersionNameOperation,
    onConfirmVersionName: (String) -> Unit,
    onCancel: () -> Unit
) {
    when (operation) {
        is VersionNameOperation.None -> {}
        is VersionNameOperation.Waiting -> {
            val modpackInfo = operation.info
            ModpackVersionNameDialog(
                name = modpackInfo.name,
                onConfirmVersionName = onConfirmVersionName,
                onCancel = onCancel
            )
        }
    }
}

/**
 * 将要安装的整合包版本名称
 * @param name 预填写的整合包版本名称
 * @param onConfirmVersionName 用户输入并确认了版本名称
 * @param onCancel 用户取消了导入
 */
@Composable
fun ModpackVersionNameDialog(
    name: String,
    onConfirmVersionName: (String) -> Unit,
    onCancel: () -> Unit,
) {
    var name by remember { mutableStateOf(name) }

    val filenameInvalidMessage = key(name) {
        isFilenameInvalid(name)
    }

    val isVersionExists = remember(name) {
        VersionsManager.isVersionExists(name, true)
    }
    val isError = name.isEmpty() || filenameInvalidMessage != null || isVersionExists

    SimpleEditDialog(
        title = stringResource(R.string.download_install_input_version_name_title),
        value = name,
        onValueChange = { name = it },
        isError = isError,
        supportingText = {
            when {
                name.isEmpty() -> Text(stringResource(R.string.generic_cannot_empty))
                filenameInvalidMessage != null -> Text(filenameInvalidMessage)
                isVersionExists -> Text(stringResource(R.string.versions_manage_install_exists))
            }
        },
        singleLine = true,
        onDismissRequest = {},
        onCancel = onCancel,
        onConfirm = {
            if (!isError) {
                onConfirmVersionName(name)
            }
        }
    )
}
