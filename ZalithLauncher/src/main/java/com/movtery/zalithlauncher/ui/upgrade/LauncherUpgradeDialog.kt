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

package com.movtery.zalithlauncher.ui.upgrade

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.components.MarkdownView
import com.movtery.zalithlauncher.ui.components.defaultRichTextStyle
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.upgrade.ZyNovaRelease
import com.movtery.zalithlauncher.utils.file.formatFileSize
import com.movtery.zalithlauncher.utils.formatDate

/**
 * ZyNova 更新提示弹窗
 *
 * 更新日志直接来自 ZyNova 自己 GitHub Release 的发布说明；
 * 安装包会根据当前设备实际支持的 ABI 自动挑选，用户不需要选择架构。
 */
@Composable
fun UpgradeDialog(
    release: ZyNovaRelease,
    onDismissRequest: () -> Unit,
    onIgnored: () -> Unit,
    onLinkClick: (String) -> Unit
) {
    //能自动判断，就不让用户选择：直接匹配当前设备架构
    val asset = remember(release) { release.pickAsset() }
    val abiLabel = remember(asset) {
        asset?.let { release.abiOrNull(it) } ?: "universal"
    }
    val downloadUrl = remember(asset, release) {
        asset?.downloadUrl?.takeIf { it.isNotBlank() } ?: release.htmlUrl
    }

    val markdownBody = remember(release, asset) {
        buildString {
            append(stringResource(R.string.upgrade_version_change, release.version))
            release.publishedAt?.takeIf { it.isNotBlank() }?.let { publishedAt ->
                append("  \n")
                append(
                    stringResource(
                        R.string.upgrade_version_create_at,
                        formatDate(
                            input = publishedAt,
                            pattern = stringResource(R.string.date_format)
                        )
                    )
                )
            }
            asset?.let {
                append("  \n")
                append(stringResource(R.string.upgrade_version_size, formatFileSize(it.size)))
            }
            append("  \n\n")
            append(
                release.body?.takeIf { it.isNotBlank() }
                    ?: stringResource(R.string.upgrade_no_changelog)
            )
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Surface(
            modifier = Modifier.padding(all = 3.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = cardColor(false),
            contentColor = onCardColor(),
            shadowElevation = 3.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp),
                    text = stringResource(R.string.upgrade_new)
                )

                CompositionLocalProvider(
                    LocalUriHandler provides object : UriHandler {
                        override fun openUri(uri: String) {
                            onLinkClick(uri)
                        }
                    }
                ) {
                    MarkdownView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .padding(horizontal = 20.dp)
                            .verticalScrollWithBar(rememberScrollState()),
                        content = markdownBody,
                        richTextStyle = defaultRichTextStyle(),
                    )
                }

                //自动挑选的安装包架构，仅作为说明，不需要用户操作
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    text = stringResource(R.string.upgrade_files_arch, abiLabel),
                    style = MaterialTheme.typography.labelSmall
                )

                //按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(Modifier.weight(1f))

                    FilledTonalButton(
                        onClick = {
                            onIgnored()
                            onDismissRequest()
                        }
                    ) {
                        Text(text = stringResource(R.string.generic_ignore))
                    }

                    Button(
                        onClick = {
                            onLinkClick(downloadUrl)
                            onDismissRequest()
                        }
                    ) {
                        Text(text = stringResource(R.string.upgrade_download))
                    }
                }
            }
        }
    }
}
