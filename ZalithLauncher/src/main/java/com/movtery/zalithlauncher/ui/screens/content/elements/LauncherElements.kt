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

package com.movtery.zalithlauncher.ui.screens.content.elements

import android.app.Activity
import android.net.Uri
import android.os.Parcelable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.gif.GifDecoder
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.accountErrorText
import com.movtery.zalithlauncher.game.account.auth_server.AuthServerHelper
import com.movtery.zalithlauncher.game.account.isMicrosoftAccount
import com.movtery.zalithlauncher.game.account.microsoftLogin
import com.movtery.zalithlauncher.game.plugin.ApkPlugin
import com.movtery.zalithlauncher.game.plugin.natives.NativePluginManager
import com.movtery.zalithlauncher.game.plugin.renderer.RendererPluginManager
import com.movtery.zalithlauncher.game.renderer.RendererInterface
import com.movtery.zalithlauncher.game.renderer.Renderers
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.hasVulkanBackend
import com.movtery.zalithlauncher.game.version.installed.utils.isBiggerVer
import com.movtery.zalithlauncher.game.version.installed.utils.isLowerVer
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.isLauncherInDarkTheme
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.VideoPlayer
import com.movtery.zalithlauncher.ui.components.rememberDialogMaxHeight
import com.movtery.zalithlauncher.ui.screens.content.FirstLoginMenu
import com.movtery.zalithlauncher.ui.theme.backgroundColor
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.canHandlePermission
import com.movtery.zalithlauncher.utils.checkStoragePermissions
import com.movtery.zalithlauncher.utils.file.InvalidFilenameException
import com.movtery.zalithlauncher.utils.file.checkFilenameValidity
import com.movtery.zalithlauncher.utils.hasStoragePermission
import com.movtery.zalithlauncher.viewmodel.BackgroundViewModel
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.LaunchGameViewModel
import com.movtery.zalithlauncher.viewmodel.LocalBackgroundViewModel
import com.movtery.zalithlauncher.viewmodel.sendToast
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
sealed interface QuickPlay : Parcelable {
    /** 快速启动游玩存档  仅支持 1.20+ 23w14a+ */
    @Parcelize
    data class Save(val saveName: String): QuickPlay

    /** 快速启动游玩服务器 */
    @Parcelize
    data class Server(val serverAddress: String): QuickPlay
}

sealed interface LaunchGameOperation {
    data object None : LaunchGameOperation
    /** 没有安装版本/没有选中有效版本 */
    data object NoVersion : LaunchGameOperation
    /** 版本名称非法时 */
    data class InvalidVersionName(val th: InvalidFilenameException) : LaunchGameOperation
    /** 没有可用账号 */
    data object NoAccount : LaunchGameOperation

    /** 渲染器可配置，但需要用到文件管理权限 */
    data class RendererNoStoragePermission(
        val renderer: RendererInterface,
        val version: Version,
        val quickPlay: QuickPlay?
    ) : LaunchGameOperation

    /** 当前渲染器不支持选中版本 */
    data class UnsupportedRenderer(
        val renderer: RendererInterface,
        val version: Version,
        val quickPlay: QuickPlay?
    ): LaunchGameOperation

    /** 当前已加载的插件不支持选中的版本 */
    data class UnsupportedPlugins(
        val plugins: List<ApkPlugin>,
        val version: Version,
        val quickPlay: QuickPlay?
    ): LaunchGameOperation

    /** 尝试启动：启动前检查一些东西 */
    data class TryLaunch(
        val version: Version?,
        val quickPlay: QuickPlay? = null
    ) : LaunchGameOperation

    /** 账号凭据已被服务端拒绝，需要重新登录 */
    data class AccountRelogin(
        val account: Account,
        val version: Version,
        val quickPlay: QuickPlay?,
        val logging: Boolean = false,
        val error: Throwable? = null
    ) : LaunchGameOperation

    /** 账号刷新失败，可选择跳过刷新继续启动 */
    data class AccountRefreshFailed(
        val account: Account,
        val error: Throwable,
        val version: Version,
        val quickPlay: QuickPlay?
    ) : LaunchGameOperation

    /** 正式启动 */
    data class RealLaunch(
        val version: Version,
        val quickPlay: QuickPlay?,
        val skipAccountRefresh: Boolean = false
    ) : LaunchGameOperation
}

@Composable
fun LaunchGameOperation(
    activity: Activity,
    eventViewModel: EventViewModel,
    launchGameViewModel: LaunchGameViewModel,
    exitActivity: () -> Unit,
    ensureVulkanSupported: suspend (Version) -> Boolean,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    toAccountManageScreen: (FirstLoginMenu) -> Unit = {},
    toVersionManageScreen: () -> Unit = {},
    navigateToWeb: (String) -> Unit = {},
    backToMain: () -> Unit = {},
    checkIfInWebScreen: () -> Boolean = { false }
) {
    val launchGameOperation by launchGameViewModel.launchGameOperation.collectAsStateWithLifecycle()

    when (val operation = launchGameOperation) {
        is LaunchGameOperation.None -> {}
        is LaunchGameOperation.NoVersion -> {
            LaunchedEffect(Unit) {
                eventViewModel.sendToast(androidText(R.string.game_launch_no_version))
                toVersionManageScreen()
                launchGameViewModel.updateOperation(LaunchGameOperation.None)
            }
        }
        is LaunchGameOperation.InvalidVersionName -> {
            val th = operation.th
            SimpleAlertDialog(
                title = stringResource(R.string.versions_manage_invalid),
                text = th.getInvalidSummary(),
                confirmText = stringResource(R.string.generic_cancel),
                onDismiss = {
                    launchGameViewModel.updateOperation(LaunchGameOperation.None)
                }
            )
        }
        is LaunchGameOperation.NoAccount -> {
            LaunchedEffect(Unit) {
                eventViewModel.sendToast(androidText(R.string.game_launch_no_account))
                //直接进入账号管理界面，打开离线登录 / 认证服务器菜单
                toAccountManageScreen(FirstLoginMenu.NORMAL)
                launchGameViewModel.updateOperation(LaunchGameOperation.None)
            }
        }
        is LaunchGameOperation.RendererNoStoragePermission -> {
            LaunchedEffect(Unit) {
                val renderer = operation.renderer
                val version = operation.version
                val quickPlay = operation.quickPlay
                withContext(Dispatchers.Main) {
                    checkStoragePermissions(
                        activity = activity,
                        message = activity.getString(R.string.renderer_version_storage_permissions, renderer.getRendererName()),
                        messageSdk30 = activity.getString(R.string.renderer_version_storage_permissions_sdk30, renderer.getRendererName()),
                        onDialogCancel = {
                            //用户拒绝授权，但仍然允许启动（不过这会导致配置无法读取）
                            launchGameViewModel.updateOperation(LaunchGameOperation.RealLaunch(version, quickPlay))
                        }
                    )
                }
                launchGameViewModel.updateOperation(LaunchGameOperation.None)
            }
        }
        is LaunchGameOperation.UnsupportedRenderer -> {
            val renderer = operation.renderer
            val version = operation.version
            val quickPlay = operation.quickPlay
            SimpleAlertDialog(
                title = stringResource(R.string.generic_warning),
                text = stringResource(R.string.renderer_version_unsupported_warning, renderer.getRendererName()),
                confirmText = stringResource(R.string.generic_anyway),
                onConfirm = {
                    launchGameViewModel.updateOperation(LaunchGameOperation.RealLaunch(version, quickPlay))
                },
                onDismiss = {
                    launchGameViewModel.updateOperation(LaunchGameOperation.None)
                }
            )
        }
        is LaunchGameOperation.UnsupportedPlugins -> {
            val plugins = operation.plugins
            val version = operation.version
            val quickPlay = operation.quickPlay
            SimpleAlertDialog(
                title = stringResource(R.string.generic_warning),
                text = stringResource(R.string.plugin_unsupported_warning, plugins.joinToString(", ") { it.appName }),
                confirmText = stringResource(R.string.generic_anyway),
                onConfirm = {
                    launchGameViewModel.updateOperation(LaunchGameOperation.RealLaunch(version, quickPlay))
                },
                onDismiss = {
                    launchGameViewModel.updateOperation(LaunchGameOperation.None)
                }
            )
        }
        is LaunchGameOperation.TryLaunch -> {
            LaunchedEffect(Unit) {
                val version = operation.version ?: run {
                    launchGameViewModel.updateOperation(LaunchGameOperation.NoVersion)
                    return@LaunchedEffect
                }

                try {
                    checkFilenameValidity(version.getVersionName())
                } catch (th: InvalidFilenameException) {
                    launchGameViewModel.updateOperation(LaunchGameOperation.InvalidVersionName(th))
                    return@LaunchedEffect
                }

                val quickPlay = operation.quickPlay

                AccountsManager.currentAccountFlow.value ?: run {
                    launchGameViewModel.updateOperation(LaunchGameOperation.NoAccount)
                    return@LaunchedEffect
                }

                Renderers.setCurrentRenderer(version.getRenderer())
                val currentRenderer = Renderers.getCurrentRenderer()

                val mcVer = version.getVersionInfo()!!.minecraftVersion

                // 设备完全支持 Vulkan 时跳过渲染器的版本支持检查
                if (!version.hasVulkanBackend() || !ensureVulkanSupported(version)) {
                    val isRendererUnsupported =
                        (currentRenderer.getMinMCVersion()?.let { mcVer.isLowerVer(it) } ?: false) ||
                                (currentRenderer.getMaxMCVersion()?.let { mcVer.isBiggerVer(it) } ?: false)

                    if (isRendererUnsupported) {
                        launchGameViewModel.updateOperation(LaunchGameOperation.UnsupportedRenderer(currentRenderer, version, quickPlay))
                        return@LaunchedEffect
                    }
                }

                val unsupportedPlugins = NativePluginManager.getCheckedPlugins().filter { plugin ->
                    (plugin.minMCVer?.let { mcVer.isLowerVer(it) } ?: false) ||
                            (plugin.maxMCVer?.let { mcVer.isBiggerVer(it) } ?: false)
                }
                if (unsupportedPlugins.isNotEmpty()) {
                    launchGameViewModel.updateOperation(LaunchGameOperation.UnsupportedPlugins(unsupportedPlugins, version, quickPlay))
                    return@LaunchedEffect
                }

                //为可配置的渲染器检查文件管理权限
                //前提：系统支持这个设置
                if (
                    canHandlePermission &&  !hasStoragePermission &&
                    RendererPluginManager.isConfigurablePlugin(version.getRenderer())
                ) {
                    launchGameViewModel.updateOperation(LaunchGameOperation.RendererNoStoragePermission(currentRenderer, version, quickPlay))
                    return@LaunchedEffect
                }

                //正式启动游戏
                launchGameViewModel.updateOperation(LaunchGameOperation.RealLaunch(version, quickPlay))
            }
        }
        is LaunchGameOperation.AccountRelogin -> {
            if (operation.account.isMicrosoftAccount()) {
                MicrosoftReloginDialog(
                    onDismissRequest = {
                        launchGameViewModel.updateOperation(LaunchGameOperation.None)
                    },
                    onConfirm = {
                        launchGameViewModel.updateOperation(LaunchGameOperation.None)
                        microsoftLogin(
                            context = activity,
                            toWeb = navigateToWeb,
                            backToMain = backToMain,
                            checkIfInWebScreen = checkIfInWebScreen,
                            showToast = { text, duration -> eventViewModel.sendToast(text, duration) },
                            submitError = submitError
                        ) {
                            activity.runOnUiThread {
                                launchGameViewModel.updateOperation(
                                    LaunchGameOperation.RealLaunch(
                                        operation.version,
                                        operation.quickPlay
                                    )
                                )
                            }
                        }
                    }
                )
            } else {
                OtherAccountReloginDialog(
                    account = operation.account,
                    logging = operation.logging,
                    error = operation.error,
                    onDismissRequest = {
                        launchGameViewModel.updateOperation(LaunchGameOperation.None)
                    },
                    onConfirm = { password ->
                        launchGameViewModel.updateOperation(operation.copy(logging = true, error = null))
                        AuthServerHelper(
                            baseUrl = operation.account.otherBaseUrl!!,
                            serverName = operation.account.accountType!!,
                            email = operation.account.otherAccount!!,
                            password = password,
                            onSuccess = { acc, _ ->
                                AccountsManager.markSessionValidated(acc)
                                AccountsManager.suspendSaveAccount(acc)
                                activity.runOnUiThread {
                                    launchGameViewModel.updateOperation(
                                        LaunchGameOperation.RealLaunch(
                                            operation.version,
                                            operation.quickPlay)
                                    )
                                }
                            },
                            onFailed = { th ->
                                activity.runOnUiThread {
                                    launchGameViewModel.updateOperation(
                                        operation.copy(
                                            logging = false,
                                            error = th
                                        ))
                                }
                            }
                        ).let { helper ->
                            TaskSystem.submitTask(helper.justLogin(activity, operation.account))
                        }
                    }
                )
            }
        }
        is LaunchGameOperation.AccountRefreshFailed -> {
            val state = operation
            AccountRefreshFailedDialog(
                error = state.error,
                onSkip = {
                    launchGameViewModel.updateOperation(
                        LaunchGameOperation.RealLaunch(
                            state.version,
                            state.quickPlay,
                            skipAccountRefresh = true
                        )
                    )
                },
                onRetry = {
                    launchGameViewModel.updateOperation(LaunchGameOperation.RealLaunch(state.version, state.quickPlay))
                },
                onCancel = {
                    launchGameViewModel.updateOperation(LaunchGameOperation.None)
                }
            )
        }
        is LaunchGameOperation.RealLaunch -> {
            LaunchedEffect(Unit) {
                val version = operation.version
                val quickPlay = operation.quickPlay
                version.apply {
                    offlineAccountLogin = false
                    quickPlaySingle = quickPlay
                }
                launchGameViewModel.start(
                    activity = activity,
                    version = version,
                    exitActivity = exitActivity,
                    submitError = submitError,
                    quickPlay = quickPlay,
                    skipAccountRefresh = operation.skipAccountRefresh
                )
                launchGameViewModel.updateOperation(LaunchGameOperation.None)
            }
        }
    }
}

@Composable
private fun AccountRefreshFailedDialog(
    error: Throwable,
    onSkip: () -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(
        onDismissRequest = onCancel
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .heightIn(max = rememberDialogMaxHeight())
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(all = 6.dp)
                    .heightIn(max = (maxHeight - 12.dp).coerceAtMost(rememberDialogMaxHeight()))
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.extraLarge,
                color = cardColor(false),
                contentColor = onCardColor(),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.account_refresh_failed_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.size(12.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .fillMaxWidth()
                    ) {
                        AndroidStringText(
                            text = accountErrorText(error),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.account_refresh_failed_skip_message),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.size(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilledTonalButton(
                            modifier = Modifier.weight(1f),
                            onClick = onCancel
                        ) {
                            MarqueeText(text = stringResource(R.string.generic_cancel))
                        }
                        FilledTonalButton(
                            modifier = Modifier.weight(1f),
                            onClick = onRetry
                        ) {
                            MarqueeText(text = stringResource(R.string.account_refresh_failed_retry))
                        }
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = onSkip
                        ) {
                            MarqueeText(text = stringResource(R.string.account_refresh_failed_skip))
                        }
                    }
                }
            }
        }
    }
}

/**
 * 启动器背景图片/视频层
 */
@Composable
fun Background(
    viewModel: BackgroundViewModel,
    modifier: Modifier = Modifier,
    allowVideo: Boolean = true
) {
    Box(
        modifier = modifier
            //议题 #38：恒定的一层底色。视频首帧出来之前、以及首页半透明背景下，
            //这一层始终与 Compose 的背景色一致，不会再透出窗口的白底/黑底。
            .background(backgroundColor())
            .backgroundBlur(AllSettings.backgroundBlur.state)
    ) {
        if (viewModel.isValid) {
            when {
                viewModel.isVideo && allowVideo -> {
                    VideoPlayer(
                        videoUri = Uri.fromFile(viewModel.backgroundFile),
                        modifier = Modifier.fillMaxSize(),
                        refreshTrigger = viewModel.refreshTrigger,
                        volume = AllSettings.videoBackgroundVolume.state / 100f
                    )
                }
                viewModel.isImage -> {
                    BackgroundImage(
                        modifier = Modifier.fillMaxSize(),
                        imageFile = viewModel.backgroundFile,
                        refreshTrigger = viewModel.refreshTrigger
                    )
                }
            }
        }
    }
}

/**
 * 背景层模糊 —— 全应用**唯一**的一层模糊
 *
 * 27.10.2 起「动态玻璃」与「逐元素背景模糊」两套机制都已移除：
 * 背景只在这里模糊一次，卡片/按钮统一用 [backgroundGlass]（Codex 单色玻璃）表达质感。
 * 因此滚动、拖动、翻页时都不会触发背景重建，也没有任何常驻动画。
 *
 * 背景不透明度拉满（100%）时整层完全不透明，模糊根本看不见 → 直接跳过，省一次全屏模糊。
 */
@Composable
private fun Modifier.backgroundBlur(blur: Int): Modifier {
    if (blur <= 0 || AllSettings.launcherBackgroundOpacity.state >= 100) return this
    val radius = remember(blur) { effectiveBlurRadius(blur) }
    return this.hazeBlur(
        input = HazeInput.Content,
        style = HazeBlurStyle {
            blurEnabled(true)
            blurRadius(radius.dp)
        }
    )
}

/**
 * 背景模糊半径上限：默认 24dp，低端设备（核心数 ≤ 4）自动降到 12dp
 */
private fun effectiveBlurRadius(requested: Int): Int {
    val cap = if (Runtime.getRuntime().availableProcessors() <= 4) 12 else 24
    return requested.coerceAtMost(cap)
}

/**
 * 卡片 / 面板的玻璃材质（沿用旧函数名，内部已换成 Codex 单色玻璃）
 *
 * 不再做逐元素模糊：调用点都已经在 `Surface` / `Card`（或显式 `clip(shape)`）内部，
 * 所以这里只叠加「竖直低透明度渐变 + 1px 亮边」，滚动时零额外开销。
 * 深色主题用白色系、浅色主题用黑色系，保证任何壁纸下都是单色、低信息密度。
 *
 * @param blur 已废弃：卡片不再独立模糊，参数仅为兼容旧调用点保留
 * @param color 已废弃：底色由调用方的 `Surface` / `Card` 提供
 * @param enabled 是否启用玻璃材质（跟随「受背景影响」开关）
 */
@Composable
@Suppress("UNUSED_PARAMETER")
fun Modifier.backgroundGlass(
    blur: Int,
    color: Color,
    enabled: Boolean = true,
): Modifier {
    if (!enabled) return this

    val strength = AllSettings.codexGlassStrength.state / 100f
    if (strength <= 0.0001f) return this

    val dark = isLauncherInDarkTheme()
    val base = if (dark) Color.White else Color.Black
    val edge = base.copy(alpha = if (dark) 0.10f else 0.08f)

    return this.drawBehind {
        drawRect(
            brush = Brush.verticalGradient(
                listOf(base.copy(alpha = strength), base.copy(alpha = strength * 0.45f))
            )
        )
        drawRect(color = edge, style = Stroke(width = 1.dp.toPx()))
    }
}

@Composable
private fun BackgroundImage(
    refreshTrigger: Any,
    imageFile: File,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val imageLoader = remember(refreshTrigger) {
        ImageLoader.Builder(context)
            .components { add(GifDecoder.Factory()) }
            .build()
    }
    val request = remember(refreshTrigger) {
        ImageRequest.Builder(context)
            .data(imageFile)
            .allowHardware(false)
            .crossfade(false)
            .build()
    }

    AsyncImage(
        modifier = modifier,
        model = request,
        imageLoader = imageLoader,
        contentDescription = null,
        contentScale = ContentScale.Crop
    )
}
