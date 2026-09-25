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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.path.GLOBAL_CLIENT
import com.movtery.zalithlauncher.path.URL_ZY_NOVA_RELEASE_LATEST
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.upgrade.UpgradeDialog
import com.movtery.zalithlauncher.upgrade.TooFrequentOperationException
import com.movtery.zalithlauncher.upgrade.ZyNovaRelease
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.safeBodyAsJson
import com.movtery.zalithlauncher.utils.network.withRetry
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

private const val TAG = "LauncherUpgradeVM"

sealed interface LauncherUpgradeOperation {
    data object None : LauncherUpgradeOperation

    /** 已检查到 ZyNova 存在新版本，展示更新信息 */
    data class Upgrade(val release: ZyNovaRelease) : LauncherUpgradeOperation
}

/**
 * 用于记录启动器更新 ViewModel
 *
 * ZyNova 只维护自己的更新体系：版本信息与安装包全部来自 ZyNova 自己的
 * GitHub Releases，不再包含任何上游 ZL2 的更新检查、更新提示、
 * 更新弹窗、更新 URL 与网盘分发逻辑。
 */
class LauncherUpgradeViewModel: ViewModel() {
    var operation by mutableStateOf<LauncherUpgradeOperation>(LauncherUpgradeOperation.None)

    private val checkMutex = Mutex()

    /**
     * 检查是否在限频时间内
     * @param time 限频时间（毫秒）
     * @param lastCheckTime 上次检查的时间戳
     */
    private fun isWithinRateLimit(
        time: Long,
        lastCheckTime: Long
    ): Boolean {
        val currentTime = System.currentTimeMillis()
        if (lastCheckTime > currentTime) {
            //用户调整到了未来的时间，无法正常判断
            //直接允许进行检查
            return false
        }
        return currentTime - lastCheckTime < time
    }

    /**
     * 更新最后一次检查的时间
     */
    private fun updateLastCheckTime() {
        AllSettings.lastUpgradeCheck.save(System.currentTimeMillis())
    }

    /**
     * 在启动时，快速完成所有的检查
     */
    fun checkOnAppStart(
        onIsLatest: suspend () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (
                isWithinRateLimit(
                    time = TimeUnit.HOURS.toMillis(1L),
                    lastCheckTime = AllSettings.lastUpgradeCheck.getValue()
                )
            ) {
                Logger.info(TAG, "App start check: Within rate limit, skipping")
                return@launch
            }

            val release = fetchLatestRelease()
            if (release != null) {
                checkForUpgrade(
                    release = release,
                    lastIgnored = AllSettings.lastIgnoredVersionName.getValue(),
                    ignoreDismissedVersions = true, //启动时检查忽略用户已忽略的版本
                    onUpgrade = { data ->
                        operation = LauncherUpgradeOperation.Upgrade(data)
                    },
                    onIsLatest = onIsLatest
                )
            }
            updateLastCheckTime()
        }
    }

    /**
     * 用户在设置内手动点击检查更新
     * @param onInProgress 准备检查更新
     * @param onIsLatest 当前启动器是最新版
     */
    suspend fun checkManually(
        onInProgress: suspend () -> Unit = {},
        onIsLatest: suspend () -> Unit = {}
    ): Boolean {
        return checkMutex.withLock {
            if (
                isWithinRateLimit(
                    time = TimeUnit.SECONDS.toMillis(5L),
                    lastCheckTime = AllSettings.lastUpgradeCheck.getValue()
                )
            ) throw TooFrequentOperationException()

            onInProgress()

            val release = fetchLatestRelease()
            if (release != null) {
                checkForUpgrade(
                    release = release,
                    lastIgnored = AllSettings.lastIgnoredVersionName.getValue(),
                    ignoreDismissedVersions = false,
                    onUpgrade = { data ->
                        operation = LauncherUpgradeOperation.Upgrade(data)
                    },
                    onIsLatest = onIsLatest
                )
            }
            updateLastCheckTime()
            release != null
        }
    }

    /**
     * 从 ZyNova 自己的 GitHub Releases 获取最新版本信息
     */
    private suspend fun fetchLatestRelease(): ZyNovaRelease? {
        return withContext(Dispatchers.IO) {
            runCatching {
                withRetry(logTag = "LauncherUpgrade", maxRetries = 2) {
                    GLOBAL_CLIENT.get(URL_ZY_NOVA_RELEASE_LATEST).safeBodyAsJson<ZyNovaRelease>()
                }
            }.getOrElse { e ->
                Logger.warning(TAG, "Failed to check for ZyNova updates!", e)
                null
            }
        }
    }

    /**
     * 检查 ZyNova 是否需要更新
     * @param lastIgnored 上次弹出更新弹窗时，用户所忽略的版本号
     * @param ignoreDismissedVersions 是否忽略用户已忽略的版本
     * @param onUpgrade 发现需要更新时调用
     * @param onIsLatest 当前已是最新版本时
     */
    private suspend fun checkForUpgrade(
        release: ZyNovaRelease,
        lastIgnored: String,
        ignoreDismissedVersions: Boolean,
        onUpgrade: suspend (ZyNovaRelease) -> Unit,
        onIsLatest: suspend () -> Unit = {}
    ) {
        val currentVersion = BuildConfig.VERSION_NAME
        if (ZyNovaRelease.compareVersion(release.version, currentVersion) > 0) {
            //启动器为旧版本
            when {
                ignoreDismissedVersions && lastIgnored == release.version -> {
                    //忽略这次更新
                    Logger.info(TAG, "ZyNova update detected: $currentVersion -> ${release.version}, but ignored by user")
                }
                else -> {
                    //弹出更新弹窗
                    Logger.info(TAG, "ZyNova update detected: $currentVersion -> ${release.version}, dialog shown to user")
                    onUpgrade(release)
                }
            }
        } else {
            Logger.info(TAG, "ZyNova is running the latest version: $currentVersion")
            onIsLatest()
        }
    }
}

@Composable
fun LauncherUpgradeOperation(
    operation: LauncherUpgradeOperation,
    onChanged: (LauncherUpgradeOperation) -> Unit,
    onIgnoredClick: (version: String) -> Unit,
    onLinkClick: (String) -> Unit
) {
    when (operation) {
        is LauncherUpgradeOperation.None -> {}
        is LauncherUpgradeOperation.Upgrade -> {
            UpgradeDialog(
                release = operation.release,
                onDismissRequest = {
                    onChanged(LauncherUpgradeOperation.None)
                },
                onIgnored = {
                    onIgnoredClick(operation.release.version)
                },
                onLinkClick = onLinkClick
            )
        }
    }
}
