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

import com.movtery.zalithlauncher.game.download.assets.favorites.FavoriteEntry as OnlineFavoriteEntry
import com.movtery.zalithlauncher.game.download.resources.ResourceType
import com.movtery.zalithlauncher.game.download.resources.ResourceManager
import com.movtery.zalithlauncher.game.download.resources.ResourceMatchException
import com.movtery.zalithlauncher.game.download.assets.mapExceptionToMessage
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.favorites.FavoriteProjectsRepository
import com.movtery.zalithlauncher.game.download.assets.favorites.FavoriteInstallOutcome
import com.movtery.zalithlauncher.game.download.assets.favorites.FavoriteQuickInstaller
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.material3.TabRow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Tab
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
import com.movtery.zalithlauncher.game.favorites.FavoriteInstallPrecheck
import com.movtery.zalithlauncher.game.favorites.FavoriteStore
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.TitleAndSummary
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.toAndroidString
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import androidx.compose.material3.Surface
import androidx.compose.ui.text.style.TextOverflow
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.AssetsIcon
import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.getTextRes
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor

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
private fun LocalArchiveContent(
    modifier: Modifier = Modifier,
    /** 安装上下文：有目标实例时直接装进该实例，不再弹实例选择 */
    installTargetVersion: String? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val versions by VersionsManager.versions.collectAsStateWithLifecycle()
    val targetVersion = remember(versions, installTargetVersion) {
        installTargetVersion?.let { name -> versions.firstOrNull { it.getVersionName() == name } }
    }

    var entries by remember { mutableStateOf<List<FavoriteEntry>>(emptyList()) }
    var totalSize by remember { mutableStateOf(0L) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var pendingDelete by remember { mutableStateOf<FavoriteEntry?>(null) }
    var pendingLargeImport by remember { mutableStateOf<Uri?>(null) }
    var detailEntry by remember { mutableStateOf<FavoriteEntry?>(null) }
    var installEntry by remember { mutableStateOf<FavoriteEntry?>(null) }
    var exportEntry by remember { mutableStateOf<FavoriteEntry?>(null) }
    /** 文件与目标实例版本不匹配时的待确认安装 */
    var pendingMismatchInstall by remember { mutableStateOf<LocalMismatchInfo?>(null) }

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

    /** 真正执行安装：把结果整理成一行可读提示 */
    fun performInstall(entry: FavoriteEntry, version: Version) {
        busy = true
        message = null
        scope.launch {
            message = runCatching {
                val result = FavoriteInstaller.install(entry, version)
                buildString {
                    append(
                        context.getString(
                            R.string.favorites_install_done,
                            version.getVersionName()
                        )
                    )
                    if (result.metadataIncomplete) {
                        append("\n")
                        append(context.getString(R.string.favorites_install_metadata_incomplete))
                    }
                    if (result.installedDependencies.isNotEmpty()) {
                        append("\n")
                        append(
                            context.getString(
                                R.string.favorites_install_dependencies,
                                result.installedDependencies.joinToString(", ")
                            )
                        )
                    }
                    if (result.missingDependencies.isNotEmpty()) {
                        append("\n")
                        append(
                            context.getString(
                                R.string.favorites_install_missing,
                                result.missingDependencies.joinToString(", ")
                            )
                        )
                    }
                }
            }.getOrElse { th ->
                //走到这里已经是真正的 IO / 目录错误；平台元数据的问题在安装器内部降级了
                context.getString(
                    R.string.favorites_import_failed,
                    th.message ?: th.javaClass.simpleName
                )
            }
            busy = false
        }
    }

    /**
     * 有安装上下文时的安装入口
     *
     * 先检查「这个文件标注支持的 Minecraft 版本」是否包含目标实例的版本：
     * 不含时先问一句（可以从「安装」按钮换实例，或仍然安装），
     * 而不是装完后在游戏里加载失败。
     */
    fun installIntoInstance(entry: FavoriteEntry, version: Version) {
        if (busy) return
        busy = true
        message = null
        scope.launch {
            val precheck = runCatching { FavoriteInstaller.precheck(entry, version) }.getOrNull()
            busy = false
            if (precheck != null && precheck.compatibleWithInstance == false) {
                pendingMismatchInstall = LocalMismatchInfo(entry, version, precheck)
            } else {
                performInstall(entry, version)
            }
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

    Column(
        modifier = modifier
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

                    //有安装上下文时，先说明「安装」会装到哪个实例
                    targetVersion?.let { target ->
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(
                                R.string.favorites_install_target_hint,
                                target.getVersionName()
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

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
                            .padding(all = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            //和资源下载卡片一致的「左图标 + 中信息」结构
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    modifier = Modifier.size(26.dp),
                                    painter = painterResource(R.drawable.ic_star_filled),
                                    tint = MaterialTheme.colorScheme.primary,
                                    contentDescription = null
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = entry.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1
                                )
                                Text(
                                    text = entrySummary(context, entry),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                Text(
                                    text = entry.source,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    maxLines = 1
                                )
                            }

                            //删除按钮做小，避免和右侧信息抢空间
                            IconButton(
                                modifier = Modifier.size(32.dp),
                                onClick = { pendingDelete = entry }
                            ) {
                                Icon(
                                    modifier = Modifier.size(18.dp),
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
                                onClick = {
                                    //上下文优先：从实例的资源管理进来时，先检查版本是否匹配，
                                    //匹配就直接装进该实例；不匹配先问一句，不再让用户重复选实例
                                    val target = targetVersion
                                    if (target != null) {
                                        installIntoInstance(entry, target)
                                    } else {
                                        installEntry = entry
                                    }
                                }
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

    //本地文件的标注版本与目标实例不匹配：先确认再装
    pendingMismatchInstall?.let { info ->
        val supported = info.precheck.gameVersions.joinToString(" / ").ifBlank { "?" }
        SimpleAlertDialog(
            title = stringResource(R.string.favorites_install_no_version_title),
            text = stringResource(
                R.string.favorites_install_local_mismatch,
                info.precheck.displayName ?: info.entry.name,
                supported,
                info.precheck.instanceMinecraftVersion ?: info.instance.getVersionName()
            ),
            confirmText = stringResource(R.string.favorites_install_anyway),
            onConfirm = {
                val entry = info.entry
                val instance = info.instance
                pendingMismatchInstall = null
                performInstall(entry, instance)
            },
            onDismiss = { pendingMismatchInstall = null }
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

/**
 * 收藏（合并版，议题 #39 + 上游 ZL2 的资源收藏）
 *
 * 两个分段：
 * - **在线收藏**：沿搬上游 ZalithLauncher2 的实现，收藏的是「资源项目」（Modrinth / CurseForge），
 *   点进去可看详情并安装（走现有安装链路，含自动前置）；
 * - **本地归档**：ZyNova 自己的实现，保存实际文件（私有目录 + SHA-256 去重），离线可用。
 *
 * ⚠️ 两段完全独立：删除本地副本不会取消在线收藏，取消在线收藏也不会动本地文件。
 */
@Composable
fun FavoritesScreen(
    screenKey: TitledNavKey,
    currentKey: TitledNavKey?,
    /** 资源安装上下文（目标实例名称）；为 null 表示资源中心里的纯浏览模式 */
    installTargetVersion: String? = null,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit = {},
    /** 打开资源详情页（查看/切换全部版本）；为 null 时不提供该入口，改用可读提示 */
    onBrowseVersions: ((Platform, String, PlatformClasses, String?) -> Unit)? = null,
) {
    var tab by remember { mutableStateOf(0) }

    BaseScreen(screenKey = screenKey, currentKey = currentKey) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TabRow(selectedTabIndex = tab, modifier = Modifier.fillMaxWidth()) {
                Tab(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    text = { Text(text = stringResource(R.string.favorites_tab_online)) }
                )
                Tab(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    text = { Text(text = stringResource(R.string.favorites_tab_local)) }
                )
            }

            when (tab) {
                0 -> OnlineFavoritesContent(
                    installTargetVersion = installTargetVersion,
                    submitError = submitError,
                    onBrowseVersions = onBrowseVersions
                )
                else -> LocalArchiveContent(installTargetVersion = installTargetVersion)
            }
        }
    }
}

/** 在线收藏：搬自上游 ZalithLauncher2 的 FavoriteProjectsRepository */
@Composable
private fun OnlineFavoritesContent(
    installTargetVersion: String? = null,
    submitError: (ErrorViewModel.ThrowableMessage) -> Unit = {},
    /** 打开资源详情页（可查看/切换全部版本）；为 null 时不提供该入口，改用可读提示 */
    onBrowseVersions: ((Platform, String, PlatformClasses, String?) -> Unit)? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        FavoriteProjectsRepository.ensureLoaded()
    }

    val entries = FavoriteProjectsRepository.projects.values
        .sortedByDescending { it.project.followTime }
    val versions by VersionsManager.versions.collectAsStateWithLifecycle()

    //安装上下文提示：从「版本设置 → 资源管理」进来时，告诉用户这些下载会装到哪个实例
    val installContext = remember(installTargetVersion, versions) {
        if (installTargetVersion == null) {
            null
        } else {
            val info = versions
                .firstOrNull { it.getVersionName() == installTargetVersion }
                ?.getVersionInfo()
            listOfNotNull(
                installTargetVersion,
                info?.minecraftVersion,
                info?.loaderInfo?.loader?.displayName
            ).joinToString(" · ")
        }
    }

    var detail by remember { mutableStateOf<OnlineFavoriteEntry?>(null) }
    var installTarget by remember { mutableStateOf<OnlineFavoriteEntry?>(null) }
    /** 正在安装的收藏键（防重复点击）；null = 空闲 */
    var installingKey by remember { mutableStateOf<String?>(null) }
    /** 操作结果提示（安装完成 / 已取消收藏等） */
    var message by remember { mutableStateOf<String?>(null) }
    /** 已知「没有当前实例版本」的资源：收藏键 → 目标实例的 Minecraft 版本 */
    var missingVersions by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    /** 待确认取消的在线收藏 */
    var pendingUnfavorite by remember { mutableStateOf<OnlineFavoriteEntry?>(null) }
    /** 没有兼容版本时弹出的可操作提示 */
    var noVersionDialog by remember { mutableStateOf<OnlineNoVersionInfo?>(null) }

    //切换目标实例后，之前记下的「无 XX 版本」结论作废
    LaunchedEffect(installTargetVersion) {
        missingVersions = emptyMap()
    }

    //一键安装：失败时给出可操作的下一步，而不是只弹一个错误
    fun startInstall(entry: OnlineFavoriteEntry) {
        if (installingKey != null) return
        //整合包有单独的安装流程（解压 + 输入版本名），直接去详情页选版本安装
        if (entry.project.classes == PlatformClasses.MOD_PACK) {
            val browse = onBrowseVersions
            if (browse != null) {
                browse(
                    entry.platform,
                    entry.project.projectId,
                    entry.project.classes,
                    entry.remote?.platformIconUrl() ?: entry.project.iconUrl
                )
            } else {
                message = context.getString(R.string.favorites_online_modpack_open_downloads)
            }
            return
        }

        val key = onlineFavoriteKey(entry.platform, entry.project.projectId)
        installingKey = key
        message = null

        val accepted = FavoriteQuickInstaller.install(
            platform = entry.platform,
            projectId = entry.project.projectId,
            classes = entry.project.classes,
            targetVersionName = installTargetVersion
        ) { outcome ->
            installingKey = null
            when (outcome) {
                is FavoriteInstallOutcome.Installed -> {
                    missingVersions = missingVersions - key
                    message = buildString {
                        append(
                            context.getString(
                                R.string.favorites_install_done,
                                outcome.instanceName
                            )
                        )
                        outcome.missingDependencies?.takeIf { it.isNotBlank() }?.let { missing ->
                            append("\n")
                            append(context.getString(R.string.favorites_install_missing, missing))
                        }
                    }
                }
                is FavoriteInstallOutcome.NoCompatibleVersion -> {
                    //记下结论：卡片上补一条「无 1.20.1 版本」的轻量提示
                    missingVersions = missingVersions + (key to outcome.minecraftVersion)
                    noVersionDialog = OnlineNoVersionInfo(entry, outcome)
                }
                is FavoriteInstallOutcome.NoInstance -> {
                    val text = context.getString(R.string.download_assets_no_installed_versions)
                    message = text
                    submitError(
                        ErrorViewModel.ThrowableMessage(
                            title = androidText(R.string.download_assets_install_failed),
                            message = androidText(text)
                        )
                    )
                }
                is FavoriteInstallOutcome.Cancelled -> Unit
                is FavoriteInstallOutcome.Failed -> {
                    val text = installFailureText(
                        context = context,
                        error = outcome.error,
                        instanceName = installTargetVersion
                    )
                    message = text
                    submitError(
                        ErrorViewModel.ThrowableMessage(
                            title = androidText(R.string.download_assets_install_failed),
                            message = androidText(text)
                        )
                    )
                }
            }
        }
        if (!accepted) installingKey = null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(all = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.favorites_online_count, entries.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilledTonalButton(
                enabled = !refreshing && entries.isNotEmpty(),
                onClick = {
                    refreshing = true
                    scope.launch {
                        runCatching { FavoriteProjectsRepository.refreshRemote() }
                        refreshing = false
                    }
                }
            ) {
                Text(text = stringResource(R.string.favorites_online_refresh))
            }
        }

        //目标实例提示：让用户点下载之前就知道会装进哪个实例
        installContext?.let { target ->
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.favorites_online_install_target, target),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        message?.let {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (entries.isEmpty()) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(R.string.favorites_online_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        entries.forEach { entry ->
            val remote = entry.remote
            //简化卡片：只有图标 / 标题 / 作者与类别，右侧两个按钮——取消收藏（小）与下载（大）
            val title = remember(entry) {
                remote?.platformTitle()?.takeIf { it.isNotBlank() } ?: entry.project.title
            }
            val subtitle = remember(entry) {
                buildString {
                    val author = remote?.platformAuthor()?.takeIf { it.isNotBlank() }
                        ?: entry.project.authors.joinToString(", ").takeIf { it.isNotBlank() }
                    if (author != null) append(author).append(" · ")
                    append(context.getString(entry.project.classes.getTextRes()))
                    if (entry.invalid) {
                        append(" · ")
                        append(context.getString(R.string.favorites_online_invalid))
                    }
                }
            }
            val icon = remote?.platformIconUrl() ?: entry.project.iconUrl

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = cardColor(),
                contentColor = onCardColor(),
                onClick = { detail = entry }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AssetsIcon(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                        size = 44.dp,
                        iconUrl = icon
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        //已知这个资源没有目标实例的版本时，卡片上直接标出来，
                        //省得用户点一遍下载才发现装不上
                        missingVersions[onlineFavoriteKey(entry.platform, entry.project.projectId)]
                            ?.let { missingVersion ->
                                Text(
                                    text = stringResource(
                                        R.string.favorites_online_no_version,
                                        missingVersion
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error,
                                    maxLines = 1
                                )
                            }
                    }

                    //取消收藏：刻意做小，别抢标题和下载按钮的位置；
                    //和「删除归档」一样先确认一次，避免误触
                    IconButton(
                        modifier = Modifier.size(30.dp),
                        onClick = { pendingUnfavorite = entry }
                    ) {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            painter = painterResource(R.drawable.ic_favorite_outlined),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = stringResource(R.string.favorites_online_remove)
                        )
                    }

                    //下载：做大的那个——点一下直接装进实例（含必需前置）
                    val favoriteKey = onlineFavoriteKey(entry.platform, entry.project.projectId)
                    val installing = installingKey == favoriteKey
                    IconButton(
                        modifier = Modifier.size(46.dp),
                        enabled = !installing && installingKey == null,
                        onClick = { startInstall(entry) }
                    ) {
                        if (installing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                modifier = Modifier.size(28.dp),
                                painter = painterResource(R.drawable.ic_download_2_filled),
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = stringResource(R.string.download_assets_quick_install)
                            )
                        }
                    }
                }
            }
        }

        detail?.let { entry ->
            OnlineFavoriteDetailDialog(
                entry = entry,
                onDismiss = { detail = null },
                onInstall = {
                    detail = null
                    installTarget = entry
                }
            )
        }

        installTarget?.let { entry ->
            OnlineInstallDialog(
                entry = entry,
                installTargetVersion = installTargetVersion,
                onDismiss = { installTarget = null },
                onBrowseVersions = onBrowseVersions
            )
        }

        //没有当前实例版本：给出「查看全部版本」的可操作入口
        noVersionDialog?.let { info ->
            val entry = info.entry
            val outcome = info.outcome
            val target = listOfNotNull(
                outcome.minecraftVersion,
                outcome.loaderName
            ).joinToString(" · ")
            AlertDialog(
                onDismissRequest = { noVersionDialog = null },
                title = { Text(stringResource(R.string.favorites_install_no_version_title)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(
                                R.string.favorites_install_no_version_message,
                                target.ifBlank { outcome.instanceName }
                            )
                        )
                        Text(
                            text = stringResource(
                                R.string.favorites_install_no_version_hint,
                                outcome.instanceName
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    val browse = onBrowseVersions
                    if (browse != null) {
                        TextButton(
                            onClick = {
                                noVersionDialog = null
                                browse(
                                    entry.platform,
                                    entry.project.projectId,
                                    entry.project.classes,
                                    entry.remote?.platformIconUrl() ?: entry.project.iconUrl
                                )
                            }
                        ) {
                            Text(stringResource(R.string.favorites_install_view_versions))
                        }
                    } else {
                        TextButton(onClick = { noVersionDialog = null }) {
                            Text(stringResource(R.string.generic_confirm))
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { noVersionDialog = null }) {
                        Text(stringResource(R.string.generic_cancel))
                    }
                }
            )
        }

        //取消在线收藏确认：和「删除归档」保持一致的二次确认
        pendingUnfavorite?.let { entry ->
            SimpleAlertDialog(
                title = stringResource(R.string.favorites_online_remove),
                text = stringResource(R.string.favorites_online_remove_confirm),
                onConfirm = {
                    pendingUnfavorite = null
                    FavoriteProjectsRepository.unfavorite(
                        entry.platform,
                        entry.project.projectId
                    )
                    message = context.getString(
                        R.string.favorites_online_cancelled,
                        entry.project.title
                    )
                },
                onDismiss = { pendingUnfavorite = null }
            )
        }
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
                                                if (r.metadataIncomplete) {
                                                    append("\n")
                                                    append(context.getString(R.string.favorites_install_metadata_incomplete))
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

/** 本地归档「文件版本与目标实例不匹配」的确认数据 */
private data class LocalMismatchInfo(
    val entry: FavoriteEntry,
    val instance: Version,
    val precheck: FavoriteInstallPrecheck,
)

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

/** 在线收藏详情：显示项目信息，并提供「安装到实例」（走统一安装链路，含自动前置） */
@Composable
private fun OnlineFavoriteDetailDialog(
    entry: OnlineFavoriteEntry,
    onDismiss: () -> Unit,
    onInstall: () -> Unit,
) {
    val remote = entry.remote
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = remote?.platformTitle() ?: entry.project.title, maxLines = 2) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = entry.project.description.ifBlank { "—" },
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = stringResource(
                        R.string.favorites_online_classes,
                        entry.project.classes.name
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (entry.invalid) {
                    Text(
                        text = stringResource(R.string.favorites_online_invalid),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            if (!entry.invalid) {
                TextButton(onClick = onInstall) {
                    Text(stringResource(R.string.favorites_action_install))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.generic_cancel))
            }
        }
    )
}

/** 在线收藏 → 选实例 → 走 ResourceManager（matchVersion + installVersion，自动含必需前置） */
@Composable
private fun OnlineInstallDialog(
    entry: OnlineFavoriteEntry,
    installTargetVersion: String? = null,
    onDismiss: () -> Unit,
    onBrowseVersions: ((Platform, String, PlatformClasses, String?) -> Unit)? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val versions by VersionsManager.versions.collectAsStateWithLifecycle()
    var running by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }
    /** 失败原因是「确实没有兼容版本」时才显示「查看全部版本」 */
    var noCompatible by remember { mutableStateOf(false) }

    //上下文优先：从「版本设置 → 资源管理」进来时，把目标实例排在第一个
    val orderedVersions = remember(versions, installTargetVersion) {
        val target = versions.firstOrNull { it.getVersionName() == installTargetVersion }
        if (target == null) versions else listOf(target) + versions.filter { it != target }
    }

    AlertDialog(
        onDismissRequest = { if (!running) onDismiss() },
        title = { Text(stringResource(R.string.favorites_install_title, entry.project.title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                result?.let { Text(text = it, style = MaterialTheme.typography.bodySmall) }
                if (result == null) {
                    if (versions.isEmpty()) {
                        Text(text = stringResource(R.string.favorites_install_no_instance))
                    }
                    orderedVersions.forEach { version ->
                        TextButton(
                            enabled = !running,
                            onClick = {
                                running = true
                                scope.launch {
                                    val outcome = runCatching {
                                        val type = ResourceType.of(entry.project.classes)
                                        val resolved = ResourceManager.matchVersion(
                                            platform = entry.platform,
                                            projectId = entry.project.projectId,
                                            type = type,
                                            instance = version
                                        )
                                        val installResult = ResourceManager.installVersion(
                                            version = resolved,
                                            type = type,
                                            instance = version
                                        )
                                        buildString {
                                            append(
                                                context.getString(
                                                    R.string.favorites_install_done,
                                                    version.getVersionName()
                                                )
                                            )
                                            if (installResult.missingRequiredDependencies.isNotEmpty()) {
                                                append("\n")
                                                append(
                                                    context.getString(
                                                        R.string.favorites_install_missing,
                                                        installResult.missingDependenciesText()
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    outcome.fold(
                                        onSuccess = { result = it },
                                        onFailure = { th ->
                                            //版本不匹配要给出「去看全部版本」的入口，
                                            //而不是把内部异常原文丢给用户
                                            noCompatible = th is ResourceMatchException.NoCompatibleVersion
                                            result = installFailureText(
                                                context = context,
                                                error = th,
                                                instanceName = version.getVersionName()
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
            val browse = onBrowseVersions
            if (noCompatible && browse != null) {
                TextButton(
                    onClick = {
                        onDismiss()
                        browse(
                            entry.platform,
                            entry.project.projectId,
                            entry.project.classes,
                            entry.remote?.platformIconUrl() ?: entry.project.iconUrl
                        )
                    }
                ) {
                    Text(stringResource(R.string.favorites_install_view_versions))
                }
            } else if (noCompatible) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.generic_confirm))
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text(
                        stringResource(
                            if (result == null) R.string.generic_cancel else R.string.generic_confirm
                        )
                    )
                }
            }
        },
        dismissButton = {
            //仅在「没有兼容版本」且能跳详情页时提供「关闭」，其余情况保持单按钮布局
            if (noCompatible && onBrowseVersions != null) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.generic_cancel))
                }
            }
        }
    )
}

/**
 * 安装 / 版本匹配失败 → 用户能读懂的说明
 *
 * 复用资源下载页已有的文案：实例信息读不出来 / 查询失败 / 确实没有兼容版本。
 * 其它异常交给统一的网络错误映射，**绝不把 lateinit 之类的内部异常原文丢到界面上**。
 */
private fun installFailureText(
    context: Context,
    error: Throwable,
    instanceName: String? = null,
): String {
    return when (error) {
        is ResourceMatchException.NoCompatibleVersion -> context.getString(
            R.string.download_assets_install_no_compatible_version,
            listOfNotNull(error.minecraftVersion, error.loaderName).joinToString(" · ")
        )
        is ResourceMatchException.QueryFailed -> context.getString(
            R.string.download_assets_install_query_failed,
            error.cause?.localizedMessage ?: error.message.orEmpty()
        )
        is ResourceMatchException.InstanceInfoUnavailable -> context.getString(
            R.string.download_assets_install_instance_unavailable,
            instanceName ?: "?"
        )
        is ResourceMatchException.Blacklisted ->
            context.getString(R.string.download_assets_install_fabric_api_blacklisted)
        else -> mapExceptionToMessage(error).toAndroidString(context)
    }
}

/** 「没有当前实例版本」提示携带的数据 */
private data class OnlineNoVersionInfo(
    val entry: OnlineFavoriteEntry,
    val outcome: FavoriteInstallOutcome.NoCompatibleVersion,
)

/** 在线收藏的唯一键：平台 + 项目 ID（与卡片状态一一对应） */
private fun onlineFavoriteKey(platform: Platform, projectId: String): String =
    "${platform.name}_$projectId"
