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

package com.movtery.zalithlauncher.ui.screens.content

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.text.format.Formatter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.favorites.FavoriteAlreadyImportedException
import com.movtery.zalithlauncher.game.favorites.FavoriteEntry
import com.movtery.zalithlauncher.game.favorites.FavoriteImportException
import com.movtery.zalithlauncher.game.favorites.FavoriteImporter
import com.movtery.zalithlauncher.game.favorites.FavoriteInstaller
import com.movtery.zalithlauncher.game.favorites.FavoriteStore
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.TitleAndSummary
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 超过这个大小先问一句再导入（本地归档会真的占掉这么多空间） */
private const val LARGE_FILE_THRESHOLD = 512L * 1024L * 1024L

/**
 * 收藏与本地归档（议题 #39）
 *
 * 这里做的是「把手机里的资源文件复制一份到 ZyNova 私有目录」：
 * 原文件删了、下载目录清理了，收藏里的东西仍然能用。
 *
 * 每一条归档都支持：详情、完整性校验、安装到实例（自动下载前置）、导出到手机、删除。
 *
 * ⚠️ 两条必须保持的独立性：
 * - 取消在线收藏**不会**删除这里的归档文件；
 * - 删除归档文件也**不会**取消在线收藏。
 */
@Composable
fun FavoritesScreen(
    key: NormalNavKey.Favorites,
    backStackViewModel: ScreenBackStackViewModel,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var entries by remember { mutableStateOf<List<FavoriteEntry>>(emptyList()) }
    var totalSize by remember { mutableStateOf(0L) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<FavoriteEntry?>(null) }
    var pendingLargeImport by remember { mutableStateOf<Uri?>(null) }
    var detailEntry by remember { mutableStateOf<FavoriteEntry?>(null) }
    var installEntry by remember { mutableStateOf<FavoriteEntry?>(null) }
    var exportEntry by remember { mutableStateOf<FavoriteEntry?>(null) }

    suspend fun refresh() {
        entries = FavoriteStore.list().sortedByDescending { it.importedAt }
        totalSize = FavoriteStore.totalSize()
    }

    LaunchedEffect(Unit) { refresh() }

    fun startImport(uri: Uri) {
        busy = true
        message = null
        scope.launch {
            runCatching { FavoriteImporter.import(context, uri) }
                .onSuccess { entry ->
                    message = context.getString(R.string.favorites_import_success, entry.name)
                    refresh()
                }
                .onFailure { th ->
                    message = if (th is FavoriteAlreadyImportedException) {
                        context.getString(R.string.favorites_import_duplicate, th.existing.name)
                    } else {
                        context.getString(
                            R.string.favorites_import_failed,
                            th.message ?: th.javaClass.simpleName
                        )
                    }
                }
            busy = false
        }
    }

    //系统文件选择器：不申请任何存储权限，只拿用户选中的那一个文件的读权限
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val size = queryFileSize(context, uri)
        if (size != null && size > LARGE_FILE_THRESHOLD) {
            pendingLargeImport = uri
        } else {
            startImport(uri)
        }
    }

    //导出到用户指定位置
    val exporter = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { target ->
        val entry = exportEntry
        exportEntry = null
        if (target == null || entry == null) return@rememberLauncherForActivityResult
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(target)?.use { output ->
                        FavoriteStore.fileOf(entry).inputStream().use { input ->
                            input.copyTo(output)
                        }
                    } ?: throw IllegalStateException("Cannot write the selected file")
                }
            }
            message = if (result.isSuccess) {
                context.getString(R.string.favorites_export_success, entry.name)
            } else {
                context.getString(
                    R.string.favorites_import_failed,
                    result.exceptionOrNull()?.message ?: "export failed"
                )
            }
        }
    }

    BaseScreen(
        screenKey = key,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(all = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BackgroundCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TitleAndSummary(
                        title = stringResource(R.string.favorites_local_archive),
                        summary = stringResource(
                            R.string.favorites_total_size,
                            Formatter.formatShortFileSize(context, totalSize)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            enabled = !busy,
                            onClick = { picker.launch(arrayOf("*/*")) }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_star_filled),
                                contentDescription = null
                            )
                            Text(
                                modifier = Modifier.padding(start = 6.dp),
                                text = stringResource(R.string.favorites_import)
                            )
                        }
                        if (busy) {
                            Text(
                                text = stringResource(R.string.favorites_importing),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    message?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = stringResource(R.string.favorites_uninstall_note),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (entries.isEmpty() && !busy) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = stringResource(R.string.favorites_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            entries.forEach { entry ->
                BackgroundCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(all = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1
                                )
                                Text(
                                    text = entrySummary(context, entry),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { pendingDelete = entry }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_delete_outlined),
                                    contentDescription = stringResource(R.string.generic_delete)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TextButton(
                                enabled = !busy && entry.type.canInstall,
                                onClick = { installEntry = entry }
                            ) {
                                Text(text = stringResource(R.string.favorites_action_install))
                            }
                            TextButton(onClick = { detailEntry = entry }) {
                                Text(text = stringResource(R.string.favorites_action_detail))
                            }
                            TextButton(onClick = {
                                exportEntry = entry
                                exporter.launch(entry.name)
                            }) {
                                Text(text = stringResource(R.string.favorites_action_export))
                            }
                            if (entry.onlineProjectId != null) {
                                TextButton(enabled = !busy, onClick = {
                                    scope.launch {
                                        withContext(Dispatchers.IO) {
                                            FavoriteStore.detachOnlineInfo(entry.id)
                                        }
                                        refresh()
                                        message = context.getString(
                                            R.string.favorites_online_cancelled,
                                            entry.name
                                        )
                                    }
                                }) {
                                    Text(text = stringResource(R.string.favorites_action_cancel_online))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //大文件导入确认
    pendingLargeImport?.let { uri ->
        SimpleAlertDialog(
            title = stringResource(R.string.favorites_large_file_title),
            text = stringResource(R.string.favorites_large_file_message),
            onConfirm = {
                pendingLargeImport = null
                startImport(uri)
            },
            onDismiss = { pendingLargeImport = null }
        )
    }

    //删除确认
    pendingDelete?.let { entry ->
        SimpleAlertDialog(
            title = stringResource(R.string.generic_delete),
            text = stringResource(R.string.favorites_delete_confirm),
            onConfirm = {
                pendingDelete = null
                scope.launch {
                    withContext(Dispatchers.IO) { FavoriteStore.delete(entry.id) }
                    refresh()
                }
            },
            onDismiss = { pendingDelete = null }
        )
    }

    //详情 + 完整性校验
    detailEntry?.let { entry ->
        FavoriteDetailDialog(
            entry = entry,
            onDismiss = { detailEntry = null }
        )
    }

    //选择实例 → 一键安装（含自动前置）
    installEntry?.let { entry ->
        InstallToInstanceDialog(
            entry = entry,
            onDismiss = { installEntry = null },
            onInstalled = { text ->
                installEntry = null
                message = text
            }
        )
    }
}

/** 详情对话框：元数据 + 现场重算 SHA-256 校验完整性 */
@Composable
private fun FavoriteDetailDialog(
    entry: FavoriteEntry,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var verifying by remember { mutableStateOf(true) }
    var intact by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(entry.id) {
        val result = withContext(Dispatchers.IO) {
            runCatching {
                val file = FavoriteStore.fileOf(entry)
                if (!file.isFile) return@runCatching false
                sha256Of(file).equals(entry.sha256, ignoreCase = true) && file.length() == entry.size
            }.getOrDefault(false)
        }
        intact = result
        verifying = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = entry.name, maxLines = 2) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                DetailLine(stringResource(R.string.favorites_detail_type), stringResource(entry.type.textRes))
                DetailLine(
                    stringResource(R.string.favorites_detail_size),
                    Formatter.formatShortFileSize(context, entry.size)
                )
                DetailLine(
                    stringResource(R.string.favorites_detail_time),
                    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(entry.importedAt))
                )
                DetailLine(stringResource(R.string.favorites_detail_source), entry.source)
                DetailLine(
                    stringResource(R.string.favorites_detail_verify),
                    when {
                        verifying -> stringResource(R.string.favorites_verifying)
                        intact == true -> stringResource(R.string.favorites_intact)
                        else -> stringResource(R.string.favorites_corrupted)
                    }
                )
                if (entry.onlineProjectId != null) {
                    DetailLine(
                        stringResource(R.string.favorites_online_only),
                        "${entry.onlinePlatform ?: "?"} / ${entry.onlineProjectId}"
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.generic_confirm))
            }
        }
    )
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodySmall)
    }
}

/** 选择目标实例 → 调用 FavoriteInstaller（自动解析并下载必需前置） */
@Composable
private fun InstallToInstanceDialog(
    entry: FavoriteEntry,
    onDismiss: () -> Unit,
    onInstalled: (String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val versions by VersionsManager.versions.collectAsStateWithLifecycle()
    var running by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!running) onDismiss() },
        title = { Text(stringResource(R.string.favorites_install_title, entry.name)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                resultText?.let {
                    Text(text = it, style = MaterialTheme.typography.bodySmall)
                }
                if (resultText == null) {
                    if (versions.isEmpty()) {
                        Text(text = stringResource(R.string.favorites_install_no_instance))
                    }
                    versions.forEach { version ->
                        TextButton(
                            enabled = !running,
                            onClick = {
                                running = true
                                scope.launch {
                                    val outcome = runCatching {
                                        FavoriteInstaller.install(entry, version)
                                    }
                                    resultText = outcome.fold(
                                        onSuccess = { r ->
                                            buildString {
                                                append(context.getString(R.string.favorites_install_done, version.getVersionName()))
                                                if (r.fallbackCopy) {
                                                    append("\n")
                                                    append(context.getString(R.string.favorites_install_fallback))
                                                }
                                                if (r.installedDependencies.isNotEmpty()) {
                                                    append("\n")
                                                    append(
                                                        context.getString(
                                                            R.string.favorites_install_dependencies,
                                                            r.installedDependencies.joinToString(", ")
                                                        )
                                                    )
                                                }
                                                if (r.missingDependencies.isNotEmpty()) {
                                                    append("\n")
                                                    append(
                                                        context.getString(
                                                            R.string.favorites_install_missing,
                                                            r.missingDependencies.joinToString(", ")
                                                        )
                                                    )
                                                }
                                            }
                                        },
                                        onFailure = { th ->
                                            context.getString(
                                                R.string.favorites_import_failed,
                                                th.message ?: th.javaClass.simpleName
                                            )
                                        }
                                    )
                                    running = false
                                }
                            }
                        ) {
                            Text(text = version.getVersionName())
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    resultText?.let { onInstalled(it) } ?: onDismiss()
                }
            ) {
                Text(stringResource(if (resultText == null) R.string.generic_cancel else R.string.generic_confirm))
            }
        }
    )
}

/** 能力判断：这些类型可以装进游戏实例 */
private val com.movtery.zalithlauncher.game.favorites.FavoriteType.canInstall: Boolean
    get() = when (this) {
        com.movtery.zalithlauncher.game.favorites.FavoriteType.MOD,
        com.movtery.zalithlauncher.game.favorites.FavoriteType.RESOURCE_PACK,
        com.movtery.zalithlauncher.game.favorites.FavoriteType.SHADER,
        com.movtery.zalithlauncher.game.favorites.FavoriteType.WORLD,
        com.movtery.zalithlauncher.game.favorites.FavoriteType.DATAPACK -> true
        else -> false
    }

private fun queryFileSize(context: Context, uri: Uri): Long? = runCatching {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)
        ?.use { cursor -> if (cursor.moveToFirst()) cursor.getLong(0) else null }
}.getOrNull()

private fun sha256Of(file: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    file.inputStream().use { input ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val read = input.read(buffer)
            if (read <= 0) break
            digest.update(buffer, 0, read)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}

/** 一行小字：类型 · 大小 · 导入时间 */
@Composable
private fun entrySummary(context: Context, entry: FavoriteEntry): String {
    val type = stringResource(entry.type.textRes)
    val size = Formatter.formatShortFileSize(context, entry.size)
    val time = remember(entry.importedAt) {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(entry.importedAt))
    }
    return "$type · $size · $time"
}
