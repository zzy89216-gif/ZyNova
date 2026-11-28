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

package com.movtery.zalithlauncher.ui.vulkan_checker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.utils.device.VulkanCheckResult
import com.movtery.zalithlauncher.utils.device.VulkanRequirements

@Composable
fun VulkanChecker(
    operation: VCOperation,
    onChange: (VCOperation) -> Unit,
    startCheck: (Version) -> Unit,
    confirmResult: () -> Unit,
) {
    when (operation) {
        is VCOperation.None -> {}
        is VCOperation.Tip -> {
            SimpleAlertDialog(
                title = stringResource(R.string.game_vulkan_check_title),
                text = stringResource(R.string.game_vulkan_check_text),
                dismissByDialog = false,
                onDismiss = {
                    startCheck(operation.version)
                }
            )
        }
        is VCOperation.Result -> {
            val result = operation.result
            val version = operation.version
            //用于在非 Composable 的 lambda 中解析资源文案
            val context = LocalContext.current

            AlertDialog(
                onDismissRequest = {
                    onChange(VCOperation.None)
                },
                title = {
                    Text(text = stringResource(R.string.game_vulkan_check_title))
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScrollWithBar(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CompositionLocalProvider(
                            LocalTextStyle provides MaterialTheme.typography.labelMedium
                        ) {
                            //检测状态：可用 / 不可用 / 检测失败
                            Text(
                                text = stringResource(result.statusTextRes()),
                                style = MaterialTheme.typography.titleSmall
                            )

                            //目标 Minecraft 版本
                            Text(
                                stringResource(
                                    R.string.game_vulkan_check_target,
                                    VulkanRequirements.CURRENT.minecraftVersion
                                )
                            )

                            //GPU / 渲染器信息
                            Text(
                                stringResource(
                                    R.string.game_vulkan_check_gpu,
                                    result.deviceInfo.gpuRenderer
                                )
                            )
                            Text(
                                stringResource(
                                    R.string.game_vulkan_check_device,
                                    result.deviceInfo.deviceModel
                                )
                            )
                            Text(
                                stringResource(
                                    R.string.game_vulkan_check_driver,
                                    result.deviceInfo.driverPath
                                        ?: stringResource(R.string.game_vulkan_check_driver_system)
                                )
                            )
                            Text(
                                stringResource(
                                    R.string.game_vulkan_check_turnip,
                                    result.useTurnip.toString()
                                )
                            )

                            when (result) {
                                is VulkanCheckResult.Available -> {
                                    Text(
                                        stringResource(
                                            R.string.game_vulkan_check_version,
                                            result.capabilities.versionString
                                        )
                                    )
                                    TextGroup(
                                        text = stringResource(R.string.game_vulkan_check_extensions),
                                        columns = VulkanRequirements.CURRENT.requiredExtensions
                                    )
                                    TextGroup(
                                        text = stringResource(R.string.game_vulkan_check_features),
                                        columns = VulkanRequirements.CURRENT.requiredFeatures
                                    )
                                }

                                is VulkanCheckResult.Unavailable -> {
                                    result.capabilities?.let { caps ->
                                        Text(
                                            stringResource(
                                                R.string.game_vulkan_check_version,
                                                caps.versionString
                                            )
                                        )
                                    }
                                    Text(
                                        stringResource(
                                            R.string.game_vulkan_check_required_api,
                                            result.requiredApiVersion
                                        )
                                    )
                                    //具体原因
                                    TextGroup(
                                        text = stringResource(R.string.game_vulkan_check_reasons),
                                        columns = result.issues.map { context.getString(it.textRes) }
                                    )
                                    if (result.missingExtensions.isNotEmpty()) {
                                        TextGroup(
                                            text = stringResource(R.string.game_vulkan_check_missing_extensions),
                                            columns = result.missingExtensions
                                        )
                                    }
                                    if (result.missingFeatures.isNotEmpty()) {
                                        TextGroup(
                                            text = stringResource(R.string.game_vulkan_check_missing_features),
                                            columns = result.missingFeatures
                                        )
                                    }
                                }

                                is VulkanCheckResult.Failed -> {
                                    Text(
                                        stringResource(R.string.game_vulkan_check_failed_reason)
                                    )
                                    result.message?.let { message ->
                                        Text(
                                            modifier = Modifier.padding(start = 16.dp),
                                            text = message
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                dismissButton = {
                    //支持用户主动重新检测
                    FilledTonalButton(
                        onClick = {
                            startCheck(version)
                        }
                    ) {
                        Text(text = stringResource(R.string.game_vulkan_check_retry))
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onChange(VCOperation.None)
                            confirmResult()
                        }
                    ) {
                        Text(text = stringResource(R.string.generic_confirm))
                    }
                }
            )
        }
    }
}

/**
 * 检测状态对应的文案
 */
private fun VulkanCheckResult.statusTextRes(): Int = when (this) {
    is VulkanCheckResult.Available -> R.string.game_vulkan_result_available
    is VulkanCheckResult.Unavailable -> R.string.game_vulkan_result_unavailable
    is VulkanCheckResult.Failed -> R.string.game_vulkan_result_failed
}

@Composable
private fun TextGroup(
    text: String,
    columns: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(text)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
        ) {
            columns.forEach { string ->
                Text(string)
            }
        }
    }
}
