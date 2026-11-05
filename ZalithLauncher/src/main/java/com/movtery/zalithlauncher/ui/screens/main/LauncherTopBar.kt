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

package com.movtery.zalithlauncher.ui.screens.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.components.codexGlass
import com.movtery.zalithlauncher.ui.screens.BackStackNavKey
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.theme.feativals.FestivalTitleText
import com.movtery.zalithlauncher.utils.festival.LocalFestivals

/**
 * Codex / Bubble Launcher 风格的顶部栏。
 *
 * 单行布局：
 * - 左侧：标题（保持原内容与样式；子页面时前面多一个返回键）；
 * - 正中：液态玻璃胶囊，顺序为
 *   文件管理 → 主菜单 → 多人联机 → 下载 → 个性化 → 设置 → AI；
 * - 右端：有下载任务且任务面板收起时，显示一个小圆环。
 *
 * 与旧版的区别：不再把功能入口排在右侧，也不再使用「…」溢出菜单——
 * 七个入口全部平铺在居中胶囊里。
 */
@Composable
fun LauncherTopBar(
    mainScreenKey: TitledNavKey?,
    inLauncherScreen: Boolean,
    taskRunning: Boolean,
    isTasksExpanded: Boolean,
    modifier: Modifier = Modifier,
    contentColor: Color,
    onScreenBack: () -> Unit,
    toMainScreen: () -> Unit,
    toSettingsScreen: () -> Unit,
    toPersonalization: () -> Unit,
    toDownloadScreen: () -> Unit,
    toMultiplayerScreen: () -> Unit,
    openFileManager: () -> Unit,
    toAIChat: () -> Unit,
    changeExpandedState: () -> Unit,
) {
    val festivals = LocalFestivals.current

    val inMultiplayerScreen = mainScreenKey is NormalNavKey.Multiplayer
    val inDownloadScreen = mainScreenKey is NestedNavKey.Download
    val inSettingsScreen = mainScreenKey is NestedNavKey.Settings
    val inAIChatScreen = mainScreenKey is NormalNavKey.AIChat

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // ── 左侧：返回 + 标题 ───────────────────────────────────────
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .widthIn(max = 180.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!inLauncherScreen) {
                    IconButton(
                        modifier = Modifier.size(36.dp),
                        onClick = onScreenBack
                    ) {
                        Icon(
                            modifier = Modifier.size(22.dp),
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.generic_back)
                        )
                    }
                }

                val parentRes = mainScreenKey?.title
                val childRes = (mainScreenKey as? BackStackNavKey<*>)?.currentKey?.title

                Crossfade(
                    targetState = parentRes to childRes
                ) { (parent, child) ->
                    val style = MaterialTheme.typography.titleMedium

                    if (parent == null) {
                        if (festivals.isEmpty()) {
                            Text(
                                text = AllSettings.launcherDisplayTitle,
                                style = style,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            FestivalTitleText(
                                festivals = festivals,
                                style = style,
                                maxLines = 1
                            )
                        }
                    } else {
                        AndroidStringText(
                            text = if (child != null) {
                                androidText(parent, androidText(" - "), child)
                            } else {
                                parent
                            },
                            style = style,
                            softWrap = false,
                            maxLines = 1
                        )
                    }
                }
            }

            // ── 正中：玻璃胶囊 ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .codexGlass(shape = RoundedCornerShape(26.dp))
                    .height(48.dp)
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 顺序：主菜单 → AI → 多人 → 下载 → 文件 → 个性化 → 设置
                // 主菜单放最左（选中态最稳定），AI 是核心入口放第二，
                // 高频的联机/下载居中，文件与两个「配置类」入口靠右。
                CapsuleItem(
                    selected = inLauncherScreen,
                    painter = painterResource(R.drawable.ic_home_filled),
                    label = stringResource(R.string.generic_main_menu),
                    onClick = { if (!inLauncherScreen) toMainScreen() }
                )
                CapsuleItem(
                    selected = inAIChatScreen,
                    painter = painterResource(R.drawable.ic_ai_filled),
                    label = stringResource(R.string.ai_title),
                    onClick = { if (!inAIChatScreen) toAIChat() }
                )
                CapsuleItem(
                    selected = inMultiplayerScreen,
                    painter = painterResource(R.drawable.ic_group_filled),
                    label = stringResource(R.string.terracotta),
                    onClick = { if (!inMultiplayerScreen) toMultiplayerScreen() }
                )
                CapsuleItem(
                    selected = inDownloadScreen,
                    painter = painterResource(R.drawable.ic_download_2_filled),
                    label = stringResource(R.string.generic_download),
                    onClick = { if (!inDownloadScreen) toDownloadScreen() }
                )
                CapsuleItem(
                    selected = false,
                    painter = painterResource(R.drawable.ic_folder_filled),
                    label = stringResource(R.string.fm_activity_name),
                    onClick = openFileManager
                )
                CapsuleItem(
                    selected = false,
                    painter = painterResource(R.drawable.ic_format_paint_outlined),
                    label = stringResource(R.string.settings_tab_launcher),
                    onClick = toPersonalization
                )
                CapsuleItem(
                    selected = inSettingsScreen,
                    painter = painterResource(R.drawable.ic_settings_filled),
                    label = stringResource(R.string.generic_setting),
                    onClick = { if (!inSettingsScreen) toSettingsScreen() }
                )

                // 任务小圆环：有任务且面板收起时出现
                AnimatedVisibility(visible = !(isTasksExpanded || taskRunning)) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = changeExpandedState)
                            .padding(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 胶囊里的单个入口：未选中只显示图标，选中时展开文字。
 */
@Composable
private fun CapsuleItem(
    selected: Boolean,
    painter: Painter,
    label: String,
    onClick: () -> Unit,
) {
    val horizontalPadding by animateDpAsState(
        targetValue = if (selected) 12.dp else 10.dp,
        label = "capsuleItemPadding"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = horizontalPadding, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painter,
            contentDescription = label
        )
        AnimatedVisibility(visible = selected) {
            Row {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )
            }
        }
    }
}
