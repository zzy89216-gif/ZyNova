/*
 * ZyNova Launcher — 公共存储权限门禁
 * GPL-3.0-or-later
 *
 * 28.1.2 起游戏数据放在 `/storage/emulated/0/ZyNova`，没有「所有文件访问」就没法读写。
 * 所以进主界面之前必须过这一关：
 *   1. 第一次：弹窗说明用途，给「去授权 / 退出」；
 *   2. 用户从系统设置返回后还没授权：改成硬提示——"必须允许管理所有文件，否则无法使用本软件"，
 *      只留「去授权」和「退出」，不给绕过。
 *
 * 维护者原话："如果用户拒绝的话，就给一个弹窗，使用此软件必须要允许管理所有文件，不然将无法使用本软件。"
 */
package com.movtery.zalithlauncher.ui.screens.splash

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.path.GameDirMigration
import com.movtery.zalithlauncher.utils.isStorageReady
import com.movtery.zalithlauncher.utils.logging.Logger

@Composable
fun StoragePermissionScreen(
    onGranted: () -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var granted by remember { mutableStateOf(isStorageReady(context)) }
    /** 是否已经从设置页返回过一次还没授权——是的话升级成“必须允许”的硬提示 */
    var returnedWithoutGrant by remember { mutableStateOf(false) }

    /**
     * Android 8–10 没有「所有文件访问」，用的是运行时读写存储权限。
     * 这里直接请求；给了就继续，拒绝就落到下面的硬提示（只能再去授权或退出）。
     */
    val legacyPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { it } && isStorageReady(context)) {
            granted = true
        } else {
            returnedWithoutGrant = true
        }
    }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && !isStorageReady(context)) {
            legacyPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    // 每次回到前台都重新检查：用户可能在系统设置里刚打开开关
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val now = isStorageReady(context)
                if (now) granted = true
                else if (!granted) returnedWithoutGrant = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(granted) {
        if (granted) onGranted()
    }

    if (granted) return

    AlertDialog(
        //硬提示阶段不允许点外部关闭，只能去授权或退出
        onDismissRequest = { if (!returnedWithoutGrant) onExit() },
        title = {
            Text(
                stringResource(
                    if (returnedWithoutGrant) R.string.storage_gate_blocked_title
                    else R.string.storage_gate_title
                )
            )
        },
        text = {
            Text(
                stringResource(
                    if (returnedWithoutGrant) R.string.storage_gate_blocked_message
                    else R.string.storage_gate_message
                )
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    runCatching {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                        )
                    }.onFailure {
                        Logger.warning("StorageGate", "Cannot open settings: ${it.message}")
                    }
                } else {
                    legacyPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    )
                }
            }) { Text(stringResource(R.string.storage_gate_grant)) }
        },
        dismissButton = {
            TextButton(onClick = onExit) {
                Text(stringResource(R.string.storage_gate_exit))
            }
        }
    )
}
