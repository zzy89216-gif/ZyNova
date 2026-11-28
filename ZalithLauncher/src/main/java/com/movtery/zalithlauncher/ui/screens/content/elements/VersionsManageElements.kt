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
import android.os.Build
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.imageLoader
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.path.GamePath
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.game.version.installed.DeleteVersionPhase
import com.movtery.zalithlauncher.game.version.installed.DeleteVersionResult
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.game.version.installed.cleanup.CleanFailedException
import com.movtery.zalithlauncher.game.version.installed.cleanup.GameAssetCleaner
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.components.LittleTextLabel
import com.movtery.zalithlauncher.ui.components.MarqueeText
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.SimpleCheckEditDialog
import com.movtery.zalithlauncher.ui.components.SimpleEditDialog
import com.movtery.zalithlauncher.ui.components.SimpleTaskDialog
import com.movtery.zalithlauncher.ui.components.TextRailItem
import com.movtery.zalithlauncher.ui.components.fadeEdge
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.animation.getAnimateTween
import com.movtery.zalithlauncher.utils.file.formatFileSize
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.string.getMessageOrToString
import com.movtery.zalithlauncher.utils.string.isNotEmptyOrBlank
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

private const val TAG = "VersionsManageElements"

sealed interface GamePathOperation {
    data object None: GamePathOperation
    data object PathExists: GamePathOperation
    data class AddNewPath(val path: String): GamePathOperation
    data class RenamePath(val item: GamePath): GamePathOperation
    data class DeletePath(val item: GamePath): GamePathOperation
}

sealed interface VersionsOperation {
    data object None: VersionsOperation
    data class Rename(val version: Version): VersionsOperation
    data class Copy(val version: Version): VersionsOperation
    data class Delete(val version: Version, val text: String? = null): VersionsOperation
    /** 已确认删除，进入全屏进度界面 */
    data class DeleteInProgress(val version: Version): VersionsOperation
    data class InvalidDelete(val version: Version): VersionsOperation
    data class RunTask(val title: Int, val task: suspend () -> Unit): VersionsOperation
}

sealed interface CleanupOperation {
    data object None : CleanupOperation
    /** 清理前的提醒 */
    data object Tip : CleanupOperation
    /** 开始清理 */
    data object Clean : CleanupOperation
    /** 清理失败 */
    data class Error(val error: Throwable) : CleanupOperation
    /** 成功清除 */
    data class Success(val count: Int, val size: String) : CleanupOperation
}

enum class VersionCategory(val textRes: Int) {
    /** 全部 */
    ALL(R.string.generic_all),
    /** 原版 */
    VANILLA(R.string.versions_manage_category_vanilla),
    /** 带有模组加载器 */
    MODLOADER(R.string.versions_manage_category_modloader)
}

@Composable
fun GamePathItemLayout(
    item: GamePath,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onRename: () -> Unit = {},
    onDelete: () -> Unit = {},
    enabled: Boolean = true,
) {
    val notDefault = item.id != GamePathManager.DEFAULT_ID

    NavigationDrawerItem(
        modifier = modifier,
        colors = NavigationDrawerItemDefaults.colors(),
        label = {
            Column(
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 4.dp)
                    .alpha(if (enabled) 1f else DisabledAlpha)
            ) {
                Text(
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    text = if (notDefault) item.title else stringResource(R.string.versions_manage_game_path_default),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )
                Text(
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    text = item.path,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
        },
        badge = {
            var menuExpanded by remember { mutableStateOf(false) }

            Row {
                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = { menuExpanded = !menuExpanded }
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(R.drawable.ic_more_horiz),
                        contentDescription = stringResource(R.string.generic_more),
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    shape = MaterialTheme.shapes.large,
                    shadowElevation = 3.dp,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        enabled = notDefault,
                        text = { Text(text = stringResource(R.string.generic_rename)) },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(R.drawable.ic_edit_filled),
                                contentDescription = stringResource(R.string.generic_rename)
                            )
                        },
                        onClick = {
                            onRename()
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        enabled = notDefault,
                        text = { Text(text = stringResource(R.string.generic_delete)) },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(R.drawable.ic_delete_filled),
                                contentDescription = stringResource(R.string.generic_delete)
                            )
                        },
                        onClick = {
                            onDelete()
                            menuExpanded = false
                        }
                    )
                }
            }
        },
        selected = selected,
        onClick = {
            if (enabled) onClick()
        }
    )
}

@Composable
fun GamePathOperation(
    gamePathOperation: GamePathOperation,
    changeState: (GamePathOperation) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    fun doRunCatching(block: () -> Unit) {
        runCatching {
            block()
        }.onFailure { e ->
            submitError(
                ErrorViewModel.ThrowableMessage(
                    title = androidText(R.string.versions_manage_game_path_error_title),
                    message = androidText(e.getMessageOrToString())
                )
            )
        }
    }
    when (gamePathOperation) {
        is GamePathOperation.None -> {}
        is GamePathOperation.AddNewPath -> {
            NameEditPathDialog(
                onDismissRequest = { changeState(GamePathOperation.None) },
                onConfirm = { value ->
                    if (GamePathManager.containsPath(gamePathOperation.path)) {
                        changeState(GamePathOperation.PathExists)
                    } else {
                        doRunCatching {
                            GamePathManager.addNewPath(title = value, path = gamePathOperation.path)
                        }
                        changeState(GamePathOperation.None)
                    }
                }
            )
        }
        is GamePathOperation.RenamePath -> {
            NameEditPathDialog(
                initValue = gamePathOperation.item.title,
                onDismissRequest = { changeState(GamePathOperation.None) },
                onConfirm = { value ->
                    doRunCatching {
                        GamePathManager.modifyTitle(gamePathOperation.item, value)
                    }
                    changeState(GamePathOperation.None)
                }
            )
        }
        is GamePathOperation.DeletePath -> {
            SimpleAlertDialog(
                title = stringResource(R.string.versions_manage_game_path_delete_title),
                text = stringResource(R.string.versions_manage_game_path_delete_message),
                onDismiss = { changeState(GamePathOperation.None) },
                onConfirm = {
                    doRunCatching {
                        GamePathManager.removePath(gamePathOperation.item)
                    }
                    changeState(GamePathOperation.None)
                }
            )
        }
        is GamePathOperation.PathExists -> {
            SimpleAlertDialog(
                title = stringResource(R.string.versions_manage_game_path_exists_title),
                text = stringResource(R.string.versions_manage_game_path_exists_message),
                onDismiss = { changeState(GamePathOperation.None) }
            )
        }
    }
}

@Composable
fun VersionCategoryItem(
    modifier: Modifier = Modifier,
    value: VersionCategory,
    versionsCount: Int,
    selected: Boolean,
    shape: Shape = MaterialTheme.shapes.large,
    selectedContentColor: Color = MaterialTheme.colorScheme.onSurface,
    unselectedContentColor: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = MaterialTheme.typography.labelMedium,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    TextRailItem(
        modifier = modifier,
        text = {
            Text(
                text = stringResource(value.textRes),
                style = style
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "($versionsCount)",
                style = style
            )
        },
        onClick = onClick,
        selected = selected,
        shape = shape,
        selectedContentColor = selectedContentColor,
        unselectedContentColor = unselectedContentColor,
        enabled = enabled
    )
}

@Composable
private fun NameEditPathDialog(
    initValue: String = "",
    onDismissRequest: () -> Unit = {},
    onConfirm: (value: String) -> Unit = {}
) {
    var value by remember { mutableStateOf(initValue) }
    SimpleEditDialog(
        title = stringResource(R.string.versions_manage_game_path_add_new),
        value = value,
        onValueChange = { value = it },
        label = { Text(text = stringResource(R.string.versions_manage_game_path_edit_title)) },
        isError = value.isEmpty(),
        supportingText = {
            if (value.isEmpty()) Text(text = stringResource(R.string.generic_cannot_empty))
        },
        singleLine = true,
        onDismissRequest = onDismissRequest,
        onConfirm = {
            if (value.isNotEmptyOrBlank()) {
                onConfirm(value.trim())
            }
        }
    )
}

@Composable
fun VersionsOperation(
    versionsOperation: VersionsOperation,
    updateVersionsOperation: (VersionsOperation) -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    when(versionsOperation) {
        is VersionsOperation.None -> {}
        is VersionsOperation.Rename -> {
            RenameVersionDialog(
                version = versionsOperation.version,
                onDismissRequest = { updateVersionsOperation(VersionsOperation.None) },
                onConfirm = {
                    updateVersionsOperation(
                        VersionsOperation.RunTask(
                            title = R.string.versions_manage_rename_version,
                            task = {
                                VersionsManager.renameVersion(versionsOperation.version, it)
                            }
                        )
                    )
                }
            )
        }
        is VersionsOperation.Copy -> {
            CopyVersionDialog(
                onDismissRequest = { updateVersionsOperation(VersionsOperation.None) },
                onConfirm = { name, copyAll ->
                    updateVersionsOperation(
                        VersionsOperation.RunTask(
                            title = R.string.versions_manage_copy_version,
                            task = { VersionsManager.copyVersion(versionsOperation.version, name, copyAll) }
                        )
                    )
                }
            )
        }
        is VersionsOperation.InvalidDelete -> {
            updateVersionsOperation(
                VersionsOperation.Delete(
                    versionsOperation.version,
                    stringResource(R.string.versions_manage_delete_version_tip_invalid)
                )
            )
        }
        is VersionsOperation.Delete -> {
            val version = versionsOperation.version
            DeleteVersionDialog(
                version = version,
                message = versionsOperation.text,
                onDismissRequest = { updateVersionsOperation(VersionsOperation.None) },
                onConfirm = {
                    updateVersionsOperation(VersionsOperation.DeleteInProgress(version))
                }
            )
        }
        is VersionsOperation.DeleteInProgress -> {
            DeleteVersionProgressDialog(
                version = versionsOperation.version,
                onDismiss = {
                    VersionsManager.clearDeleteResult()
                    updateVersionsOperation(VersionsOperation.None)
                },
                onError = { e ->
                    Logger.error(TAG, "Failed to delete the version.", e)
                    submitError(
                        ErrorViewModel.ThrowableMessage(
                            title = androidText(R.string.versions_manage_task_error),
                            message = androidText(e.getMessageOrToString())
                        )
                    )
                }
            )
        }
        is VersionsOperation.RunTask -> {
            SimpleTaskDialog(
                title = stringResource(versionsOperation.title),
                task = versionsOperation.task,
                context = Dispatchers.IO,
                onDismiss = { updateVersionsOperation(VersionsOperation.None) },
                onError = { e ->
                    Logger.error(TAG, "Failed to run task.", e)
                    submitError(
                        ErrorViewModel.ThrowableMessage(
                            title = androidText(R.string.versions_manage_task_error),
                            message = androidText(e.getMessageOrToString())
                        )
                    )
                }
            )
        }
    }
}

@Composable
fun RenameVersionDialog(
    version: Version,
    onDismissRequest: () -> Unit = {},
    onConfirm: (value: String) -> Unit = {}
) {
    var name by remember { mutableStateOf(version.getVersionName()) }

    val filenameInvalidMessage = key(name) {
        isFilenameInvalid(name)
    }

    val isVersionExists = remember(name) {
        VersionsManager.isVersionExists(name, true)
    }
    val isError = name.isEmpty() || filenameInvalidMessage != null || isVersionExists

    SimpleEditDialog(
        title = stringResource(R.string.versions_manage_rename_version),
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
        onDismissRequest = onDismissRequest,
        onConfirm = {
            if (!isError) {
                onConfirm(name)
            }
        }
    )
}

@Composable
fun CopyVersionDialog(
    onDismissRequest: () -> Unit = {},
    onConfirm: (value: String, copyAll: Boolean) -> Unit = { _, _ -> }
) {
    var copyAll by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }

    val filenameInvalidMessage = key(name) {
        isFilenameInvalid(name)
    }

    val isVersionExists = remember(name) {
        VersionsManager.isVersionExists(name, true)
    }
    val isError = name.isEmpty() || filenameInvalidMessage != null || isVersionExists

    SimpleCheckEditDialog(
        title = stringResource(R.string.versions_manage_copy_version),
        text = stringResource(R.string.versions_manage_copy_version_tip),
        checkBoxText = stringResource(R.string.versions_manage_copy_version_all),
        checked = copyAll,
        value = name,
        onCheckedChange = { copyAll = it },
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
        onDismissRequest = onDismissRequest,
        onConfirm = {
            if (!isError) {
                onConfirm(name, copyAll)
            }
        }
    )
}

@Composable
fun DeleteVersionDialog(
    version: Version,
    message: String? = null,
    onDismissRequest: () -> Unit = {},
    onConfirm: () -> Unit = {}
) {
    if (message != null) {
        SimpleAlertDialog(
            title = stringResource(R.string.versions_manage_delete_version),
            text = message,
            onDismiss = onDismissRequest,
            onConfirm = onConfirm
        )
    } else {
        SimpleAlertDialog(
            title = stringResource(R.string.versions_manage_delete_version),
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = stringResource(R.string.versions_manage_delete_version_tip_hint1, version.getVersionName()))
                    Text(text = stringResource(R.string.versions_manage_delete_version_tip_hint2))
                    Text(text = stringResource(R.string.versions_manage_delete_version_tip_hint3))
                    Text(
                        text = stringResource(R.string.versions_manage_delete_version_tip_hint4),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            onConfirm = onConfirm,
            onCancel = onDismissRequest,
            onDismissRequest = onDismissRequest
        )
    }
}

/**
 * 让「删除版本」的全屏进度界面真正铺满整屏（议题 #74 用户反馈的「不是真全屏」）。
 *
 * 成因：Compose 的 [Dialog] 用的是**独立 Window**，默认 `decorFitsSystemWindows = true`，
 * 而且窗口尺寸默认不是 MATCH_PARENT。只设置 `usePlatformDefaultWidth = false` 时，
 * 内容仍然只铺到系统栏以内，状态栏 / 导航栏那两条露出底下的 Activity 页面 —— 看着就没盖住全屏。
 *
 * 这里对 Dialog 自己的 Window 补四件事（都只用 WindowCompat / WindowInsetsControllerCompat，API 26–36 一致）：
 * 1. `setDecorFitsSystemWindows(false)` + `setLayout(MATCH_PARENT, MATCH_PARENT)`：
 *    内容延伸到系统栏下面，并强制窗口铺满（个别 ROM 只认窗口尺寸，不认 Compose 的布局参数）；
 * 2. 系统栏透明：让卡片底色一直透到状态栏 / 导航栏，而不是系统栏自己画一条黑/白条；
 * 3. 系统栏图标按卡片背景亮度反色：浅色背景用深色图标、深色背景用浅色图标，
 *    避免状态栏时间 / 导航栏按键在浅色卡片上看不清；
 * 4. 沿用启动器的沉浸式策略（启动器平时就是全屏的，Dialog 窗口默认会把系统栏唤回来）：
 *    隐藏系统栏、上滑临时唤出，删除过程中的观感和其它页面一致。
 *
 * 只作用于 Dialog 窗口；对话框关闭后窗口销毁，不影响 Activity 自己的全屏设置。
 */
@Suppress("DEPRECATION")
@Composable
private fun FullScreenDialogWindowEffect() {
    val view = LocalView.current
    //浅色卡片背景要用深色系统栏图标（反之亦然），用卡片颜色亮度判断，跟随深浅色主题
    val isLightBackground = cardColor(false).luminance() > 0.5f

    DisposableEffect(view, isLightBackground) {
        val window = (view.parent as? DialogWindowProvider)?.window
        if (window != null) {
            //1. 窗口铺满整屏（DialogProperties.decorFitsSystemWindows 已经设置过一次，这里再显式收口）
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )

            //2. 系统栏透明：窗口自己的卡片底色直接透到系统栏区域
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            )
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT

            //3. 允许画进刘海 / 挖孔区域（API 28+ 才有这个属性；API 26/27 没有挖孔屏，跳过即可）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes = window.attributes.apply {
                    layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }

            //4. 系统栏图标反色 + 沉浸式（多窗口下系统栏由系统并排显示，不去抢它的显隐）
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = isLightBackground
                isAppearanceLightNavigationBars = isLightBackground
                val activity = view.context as? Activity
                if (activity?.isInMultiWindowMode != true) {
                    hide(WindowInsetsCompat.Type.systemBars())
                    systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
        }
        onDispose {}
    }
}

/**
 * 删除版本的全屏进度界面（议题 #74）。
 *
 * 旧实现用 [SimpleTaskDialog] 直接跑 `FileUtils.deleteQuietly`：
 * 进度条只能是不确定状态、没有取消按钮、失败还被静默吞掉，大版本一删就是几分钟，看起来就像卡死。
 * 现在：
 * 1. 先扫描统计文件数 / 总大小，删除阶段显示**确定**进度（百分比 + 文件数 + 已删/总大小）；
 * 2. 扫描阶段也有明确阶段文案与已扫描的文件数，不再是空转圈；
 * 3. 删除任务挂在 [VersionsManager] 自己的作用域上，随时可以取消；
 *    实例在开始删除前会先被挪到隐藏临时目录，取消/失败都不会在版本列表里留下「删了一半」的实例。
 */
@Composable
fun DeleteVersionProgressDialog(
    version: Version,
    onDismiss: () -> Unit,
    onError: (Throwable) -> Unit
) {
    //性能：删除进度在删除阶段大约每 120ms 变一次，这里只收集「结果」这种低频状态；
    //高频进度交给下方 DeleteVersionProgressDetails 自己读，避免每次进度刷新都把整个全屏 Dialog 子树重启一遍。
    val result by VersionsManager.deleteResult.collectAsStateWithLifecycle()

    val versionName = remember(version) { version.getVersionName() }

    //界面一出现就启动删除；真正的删除不绑定组合生命周期（见 VersionsManager.startDeleteVersion）
    LaunchedEffect(Unit) {
        if (!VersionsManager.startDeleteVersion(version)) {
            onDismiss()
        }
    }

    //成功 / 取消短暂展示结果后自动关闭，不额外增加一次点击；失败交给统一的错误提示
    LaunchedEffect(result) {
        when (val current = result) {
            is DeleteVersionResult.Success -> {
                delay(800L)
                onDismiss()
            }
            is DeleteVersionResult.Cancelled -> {
                delay(1600L)
                onDismiss()
            }
            is DeleteVersionResult.Failed -> {
                onError(current.throwable)
                onDismiss()
            }
            null -> Unit
        }
    }

    Dialog(
        //删除过程中不允许点外部 / 返回键关闭：避免变成没有界面的静默删除
        onDismissRequest = {},
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            //⚠️ 全屏关键：Compose Dialog 默认「窗口贴合系统栏」（decorFitsSystemWindows = true），
            //只设置 usePlatformDefaultWidth = false 时，内容仍被收在状态栏 / 导航栏之内，
            //系统栏那两条会露出底下的页面 —— 看起来就不是真全屏（用户反馈的 Bug 就是这个）。
            decorFitsSystemWindows = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        )
    ) {
        //把 Dialog 自己的 Window 真正铺满整屏，并处理系统栏图标反色（见下方注释）
        FullScreenDialogWindowEffect()

        //⚠️ dismissOnBackPress=false 只是不触发弹窗自己的关闭回调，系统返回键仍可能被导航层吃掉：
        //那样用户能退页、删除却在后台继续，结果也没人清理。这里显式吞掉返回键（进度/取消都只走界面按钮）。
        BackHandler(enabled = true) {}

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = cardColor(false),
            contentColor = onCardColor()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    //内容避开状态栏 / 导航栏 / 刘海挖孔：多窗口、系统栏被临时唤出时按钮也不会被挡
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val failed = result is DeleteVersionResult.Failed
                val iconRes = when (result) {
                    is DeleteVersionResult.Failed -> R.drawable.ic_warning_outlined
                    is DeleteVersionResult.Success -> R.drawable.ic_check
                    is DeleteVersionResult.Cancelled -> R.drawable.ic_close
                    null -> R.drawable.ic_delete_filled
                }

                Icon(
                    modifier = Modifier.size(56.dp),
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = if (failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (result) {
                        is DeleteVersionResult.Success -> stringResource(R.string.versions_delete_progress_done)
                        is DeleteVersionResult.Cancelled -> stringResource(R.string.versions_delete_progress_cancelled)
                        is DeleteVersionResult.Failed -> stringResource(R.string.versions_delete_progress_failed)
                        null -> stringResource(R.string.versions_delete_progress_title, versionName)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                if (result == null) {
                    //性能：进度（约 120ms 一次）单独拆成子 composable，进度刷新只重组这一小块
                    DeleteVersionProgressDetails()
                } else {
                    val current = result
                    if (current is DeleteVersionResult.Cancelled) {
                        //能恢复就明确告诉用户「实例还在」；恢复不了 / 已经删了一部分才提示残留
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when {
                                !current.restored ->
                                    stringResource(R.string.versions_delete_progress_cancel_hint)
                                current.deletedFiles > 0 ->
                                    stringResource(
                                        R.string.versions_delete_progress_cancel_partial_hint,
                                        current.deletedFiles
                                    )
                                else ->
                                    stringResource(R.string.versions_delete_progress_cancel_restored_hint)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}


/**
 * 删除进度详情：阶段文案、进度条、文件数 / 大小、当前文件与取消按钮。
 *
 * 性能：删除阶段进度大约每 120ms 更新一次；单独拆成 composable 后，
 * 进度刷新只重组这一小块，不会连带重启整个全屏 Dialog（Surface / 标题 / BackHandler / 窗口效果层）。
 */
@Composable
private fun DeleteVersionProgressDetails() {
    val currentProgress by VersionsManager.deleteProgress.collectAsStateWithLifecycle()

    val phase = currentProgress?.phase ?: DeleteVersionPhase.SCANNING

    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = when (phase) {
            DeleteVersionPhase.SCANNING -> stringResource(R.string.versions_delete_phase_scanning)
            DeleteVersionPhase.DELETING -> stringResource(R.string.versions_delete_phase_deleting)
        },
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(20.dp))

    val fraction = currentProgress?.fraction
    if (fraction == null) {
        //扫描阶段总数还不知道，只能给不确定进度；下面的实时数字说明扫描还在推进
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    } else {
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth()
        )
    }

    currentProgress?.let { data ->
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = if (data.phase == DeleteVersionPhase.SCANNING) {
                stringResource(
                    R.string.versions_delete_phase_scanning_detail,
                    data.totalFiles,
                    formatFileSize(data.totalBytes)
                )
            } else {
                stringResource(
                    R.string.versions_delete_progress_detail,
                    ((data.fraction ?: 0f) * 100).toInt(),
                    data.doneFiles,
                    data.totalFiles,
                    formatFileSize(data.doneBytes),
                    formatFileSize(data.totalBytes)
                )
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        data.currentEntry.takeIf { it.isNotBlank() }?.let { entry ->
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.versions_delete_progress_current, entry),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    Spacer(modifier = Modifier.height(28.dp))
    Button(onClick = { VersionsManager.cancelDeleteVersion() }) {
        Text(text = stringResource(R.string.generic_cancel))
    }
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = stringResource(R.string.versions_delete_progress_keep_open),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}

@Composable
fun CleanupOperation(
    operation: CleanupOperation,
    changeOperation: (CleanupOperation) -> Unit,
    cleaner: GameAssetCleaner?,
    onClean: () -> Unit,
    onCancel: () -> Unit,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit
) {
    when(operation) {
        is CleanupOperation.None -> {}
        is CleanupOperation.Tip -> {
            SimpleAlertDialog(
                title = stringResource(R.string.versions_manage_cleanup),
                text = {
                    Text(stringResource(R.string.versions_manage_cleanup_tip))
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.versions_manage_cleanup_warning))
                    Text("../assets/..")
                    //不再清理依赖库，文件并不会太大，也有可能导致其他问题：#617
//                    Text("../libraries/..")
                },
                onConfirm = onClean,
                onCancel = { changeOperation(CleanupOperation.None) }
            )
        }
        is CleanupOperation.Clean -> {
            if (cleaner != null) {
                val tasks = cleaner.tasksFlow.collectAsStateWithLifecycle()
                if (tasks.value.isNotEmpty()) {
                    //清理无用游戏文件流程对话框
                    TitleTaskFlowDialog(
                        title = stringResource(R.string.versions_manage_cleanup),
                        tasks = tasks.value,
                        onCancel = {
                            onCancel()
                            changeOperation(CleanupOperation.None)
                        }
                    )
                }
            }
        }
        is CleanupOperation.Error -> {
            val error = operation.error
            if (error is CleanFailedException) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Text(
                            text = stringResource(R.string.generic_warning),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fadeEdge(state = scrollState)
                                .verticalScrollWithBar(state = scrollState)
                        ) {
                            Text(stringResource(R.string.versions_manage_cleanup_failed_files))
                            error.files.forEach { file ->
                                Text(file.absolutePath)
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = { changeOperation(CleanupOperation.None) }) {
                            MarqueeText(text = stringResource(R.string.generic_cancel))
                        }
                    }
                )
            } else {
                changeOperation(CleanupOperation.None)
                submitError(
                    ErrorViewModel.ThrowableMessage(
                        title = androidText(R.string.versions_manage_cleanup_failed),
                        message = androidText(error.getMessageOrToString())
                    )
                )
            }
        }
        is CleanupOperation.Success -> {
            SimpleAlertDialog(
                title = stringResource(R.string.versions_manage_cleanup),
                text = stringResource(R.string.versions_manage_cleanup_success, operation.count, operation.size)
            ) {
                changeOperation(CleanupOperation.None)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VersionItemLayout(
    version: Version,
    selected: Boolean,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit,
    modifier: Modifier = Modifier,
    influencedByBackground: Boolean = true,
    color: Color = cardColor(influencedByBackground),
    contentColor: Color = onCardColor(),
    blur: Int = AllSettings.backgroundBlur.state,
    onSelected: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onRenameClick: () -> Unit = {},
    onCopyClick: () -> Unit = {},
    onExportClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onPinned: () -> Unit = {}
) {
    val scale = remember { Animatable(initialValue = 0.95f) }
    LaunchedEffect(Unit) {
        scale.animateTo(targetValue = 1f, animationSpec = getAnimateTween())
    }
    Surface(
        modifier = modifier.graphicsLayer(scaleY = scale.value, scaleX = scale.value),
        color = color,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large,
        onClick = {
            if (selected) return@Surface
            onSelected()
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = MaterialTheme.shapes.large)
                .backgroundGlass(blur, color, influencedByBackground)
                .padding(all = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = {
                    if (selected) return@RadioButton
                    onSelected()
                }
            )
            CommonVersionInfoLayout(
                modifier = Modifier.weight(1f),
                version = version
            )

            IconButton(
                onClick = {
                    val currentValue = version.pinnedState
                    runCatching {
                        version.setPinnedAndSave(!currentValue)
                    }.onFailure { e ->
                        Logger.error(TAG, "Failed to save version config!", e)
                        submitError(
                            ErrorViewModel.ThrowableMessage(
                                title = androidText(R.string.versions_config_failed_to_save),
                                message = androidText(e.getMessageOrToString())
                            )
                        )
                    }.onSuccess {
                        onPinned()
                    }
                },
                enabled = version.isValid()
            ) {
                Crossfade(
                    targetState = version.pinnedState
                ) { pinned ->
                    val icon = if (pinned) {
                        painterResource(R.drawable.ic_pinned_filled)
                    } else {
                        painterResource(R.drawable.ic_pinned_outlined)
                    }
                    Icon(
                        modifier = Modifier.rotate(45.0f),
                        painter = icon,
                        contentDescription = stringResource(R.string.versions_manage_pin)
                    )
                }
            }

            IconButton(
                onClick = onSettingsClick,
                enabled = version.isValid()
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.ic_settings_filled),
                    contentDescription = stringResource(R.string.versions_manage_settings)
                )
            }

            Row {
                var menuExpanded by remember { mutableStateOf(false) }

                IconButton(onClick = { menuExpanded = !menuExpanded }) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_more_horiz),
                        contentDescription = stringResource(R.string.generic_more)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    shape = MaterialTheme.shapes.large,
                    shadowElevation = 3.dp,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.generic_rename)) },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(R.drawable.ic_edit_filled),
                                contentDescription = stringResource(R.string.generic_rename)
                            )
                        },
                        onClick = {
                            onRenameClick()
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.generic_copy)) },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(R.drawable.ic_file_copy_filled),
                                contentDescription = stringResource(R.string.generic_copy)
                            )
                        },
                        onClick = {
                            onCopyClick()
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.versions_export)) },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(R.drawable.ic_folder_zip_filled),
                                contentDescription = stringResource(R.string.versions_export)
                            )
                        },
                        onClick = {
                            onExportClick()
                            menuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(text = stringResource(R.string.generic_delete)) },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(R.drawable.ic_delete_filled),
                                contentDescription = stringResource(R.string.generic_delete)
                            )
                        },
                        onClick = {
                            onDeleteClick()
                            menuExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CommonVersionInfoLayout(
    version: Version,
    modifier: Modifier = Modifier,
    iconSize: Dp = 34.dp,
) {
    val isValid = remember(version) { version.isValid() }
    val versionName = remember(version) { version.getVersionName() }
    val isSummaryValid = remember(version) { version.isSummaryValid() }
    val versionInfo = remember(version) { version.getVersionInfo() }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VersionIconImage(
            modifier = Modifier.size(iconSize),
            version = version
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            //版本名称
            Text(
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                maxLines = 1,
                text = versionName,
                style = MaterialTheme.typography.labelLarge
            )
            //版本描述
            if (isValid && isSummaryValid) {
                val versionSummary = remember(version) {
                    version.getVersionSummary()
                }

                Text(
                    modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    maxLines = 1,
                    text = versionSummary,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            //版本详细信息
            FlowRow(
                modifier = Modifier.alpha(0.7f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!isValid) {
                    LittleTextLabel(
                        text = stringResource(R.string.versions_manage_invalid),
                        color = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        textStyle = MaterialTheme.typography.labelSmall
                    )
                } else {
                    versionInfo?.let { versionInfo ->
                        Text(
                            text = versionInfo.minecraftVersion,
                            style = MaterialTheme.typography.labelSmall,
                        )
                        versionInfo.loaderInfo?.let { loaderInfo ->
                            Text(
                                text = loaderInfo.loader.displayName,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = loaderInfo.version,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VersionIconImage(
    version: Version?,
    modifier: Modifier = Modifier,
    refreshKey: Any? = null
) {
    val defaultIconRes = remember(version) {
        version?.let { getLoaderIconRes(it.getVersionInfo()?.loaderInfo?.loader) } ?: R.drawable.img_minecraft
    }
    val defaultIcon = painterResource(defaultIconRes)

    val context = LocalContext.current
    //性能：不要为每个列表项新建 ImageLoader —— 每次 new 都会各自建一套内存缓存 / 磁盘缓存，
    //等于同一张图标反复解码、还和全局缓存互相不命中，版本列表 / 主页卡片滚动时会明显掉帧。
    //统一用 Application 里配置好的单例 ImageLoader（自带 Gif / Svg 解码器、20MB 内存缓存、512MB 磁盘缓存）。
    val loader = context.imageLoader

    val model = remember(version, refreshKey) {
        version?.let {
            val iconFile = VersionsManager.getVersionIconFile(it)
            if (iconFile.exists()) iconFile
            else null
        } ?: defaultIcon
    }

    when (model) {
        is Painter -> {
            Image(
                painter = model,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = modifier
            )
        }
        else -> {
            AsyncImage(
                model = model,
                imageLoader = loader,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = modifier
            )
        }
    }
}

/**
 * 模组加载器图标展示组件，包装 [Image]
 */
@Composable
fun ModLoaderIcon(
    modloader: ModLoader,
    @DrawableRes
    defaultIcon: Int,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
) {
    val icon = getLoaderIconRes(modloader, defaultIcon)
    Image(
        modifier = modifier,
        painter = painterResource(icon),
        contentDescription = null,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
    )
}

private fun getLoaderIconRes(
    loader: ModLoader?,
    @DrawableRes
    defaultIcon: Int = R.drawable.img_minecraft,
): Int {
    return when (loader) {
        ModLoader.FABRIC,
        ModLoader.BABRIC -> R.drawable.img_loader_fabric
        ModLoader.LEGACY_FABRIC -> R.drawable.img_loader_legacy_fabric

        ModLoader.FORGE -> R.drawable.img_anvil
        ModLoader.QUILT -> R.drawable.img_loader_quilt
        ModLoader.NEOFORGE -> R.drawable.img_loader_neoforge
        ModLoader.OPTIFINE -> R.drawable.img_loader_optifine
        ModLoader.LITE_LOADER -> R.drawable.img_chicken_old
        ModLoader.CLEANROOM -> R.drawable.img_loader_cleanroom
        else -> defaultIcon
    }
}