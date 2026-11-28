/*
 * ZyNova Launcher
 * Copyright (C) 2025 ZyNova Contributors
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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.components.reorderItem
import com.movtery.zalithlauncher.ui.components.rememberReorderState
import com.movtery.zalithlauncher.ui.screens.main.home_feed.HomeFeedSource
import com.movtery.zalithlauncher.ui.screens.main.home_feed.HomeFeedStore
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor

/**
 * 「自定义主页」卡片管理弹窗（设置 → 基础设置 → 主页）
 *
 * 支持：
 * - **拖动排序**：长按左侧手柄上下拖，顺序持久化到 `homeFeedCardOrder`；
 * - **隐藏 / 显示**：开关只影响这一张卡片，之后还能再打开（`homeFeedHiddenCards`）；
 * - **删除 / 添加**：删除只是从主页移除，可以在下方「可添加的卡片」里加回来。
 *
 * 之所以做成弹窗而不是独立页面：入口就在设置页里，管理完要立刻回到设置继续调，
 * 弹窗比多一层页面返回更少步骤（Context First. Less Steps.）。
 */
@Composable
fun HomeFeedManageDialog(
    visible: Boolean,
    onDismissRequest: () -> Unit,
) {
    if (!visible) return

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.82f),
            shape = MaterialTheme.shapes.extraLarge,
            color = cardColor()
        ) {
            HomeFeedManageContent(onDismissRequest = onDismissRequest)
        }
    }
}

@Composable
private fun HomeFeedManageContent(
    onDismissRequest: () -> Unit,
) {
    //本地状态：拖动过程中只改这里，拖动结束才落盘（避免每帧写存储）
    var added by remember { mutableStateOf(HomeFeedStore.orderedSources()) }
    var hidden by remember { mutableStateOf(AllSettings.homeFeedHiddenCards.state.toSet()) }
    var removed by remember { mutableStateOf(HomeFeedStore.removedSources()) }

    //打开时按当前配置同步一次（弹窗可能被反复打开）
    LaunchedEffect(Unit) {
        added = HomeFeedStore.orderedSources()
        hidden = AllSettings.homeFeedHiddenCards.state.toSet()
        removed = HomeFeedStore.removedSources()
    }

    fun refreshLocalState() {
        added = HomeFeedStore.orderedSources()
        hidden = AllSettings.homeFeedHiddenCards.state.toSet()
        removed = HomeFeedStore.removedSources()
    }

    val reorderState = rememberReorderState(
        onMove = { dragged, target ->
            val from = added.indexOfFirst { it.id == dragged }
            val to = added.indexOfFirst { it.id == target }
            if (from >= 0 && to >= 0 && from != to) {
                added = added.toMutableList().apply { add(to, removeAt(from)) }
            }
        },
        onEnd = {
            HomeFeedStore.saveOrder(added)
        }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        //标题栏
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.home_feed_manage_title),
                style = MaterialTheme.typography.titleMedium,
                color = onCardColor()
            )
            IconButton(onClick = onDismissRequest) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.generic_close)
                )
            }
        }
        Text(
            text = stringResource(R.string.home_feed_manage_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            added.forEach { source ->
                ManagedCardRow(
                    source = source,
                    hidden = source.id in hidden,
                    reorderState = reorderState,
                    onHiddenChange = { isHidden ->
                        HomeFeedStore.setHidden(source, isHidden)
                        hidden = AllSettings.homeFeedHiddenCards.state.toSet()
                    },
                    onRemove = {
                        HomeFeedStore.removeSource(source)
                        refreshLocalState()
                    }
                )
            }

            if (added.isEmpty()) {
                Text(
                    text = stringResource(R.string.home_feed_manage_none_added),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (removed.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = stringResource(R.string.home_feed_manage_available),
                    style = MaterialTheme.typography.labelLarge,
                    color = onCardColor()
                )
                removed.forEach { source ->
                    RemovedCardRow(
                        source = source,
                        onAdd = {
                            HomeFeedStore.addSource(source)
                            refreshLocalState()
                        }
                    )
                }
            }
        }

        //底部操作
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {
                    HomeFeedStore.reset()
                    refreshLocalState()
                }
            ) {
                Text(text = stringResource(R.string.home_feed_manage_reset))
            }
            Spacer(modifier = Modifier.weight(1f))
            FilledTonalButton(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.generic_done))
            }
        }
    }
}

/** 已添加的卡片：拖动排序 + 隐藏开关 + 删除 */
@Composable
private fun ManagedCardRow(
    source: HomeFeedSource,
    hidden: Boolean,
    reorderState: com.movtery.zalithlauncher.ui.components.ReorderState,
    onHiddenChange: (Boolean) -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .reorderItem(key = source.id, state = reorderState)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        //长按手柄拖动排序（与卡片式主页的拖动交互一致）
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(R.drawable.ic_drag_handle),
            contentDescription = stringResource(R.string.home_feed_manage_drag_hint),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp),
            text = stringResource(source.titleRes),
            style = MaterialTheme.typography.labelLarge,
            color = onCardColor(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painterResource(
                if (hidden) R.drawable.ic_visibility_off_outlined
                else R.drawable.ic_visibility_outlined
            ),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Switch(
            checked = !hidden,
            onCheckedChange = { visible -> onHiddenChange(!visible) }
        )
        IconButton(onClick = onRemove) {
            Icon(
                painter = painterResource(R.drawable.ic_delete_outlined),
                contentDescription = stringResource(R.string.home_feed_manage_remove),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

/** 已删除的卡片：一键加回主页 */
@Composable
private fun RemovedCardRow(
    source: HomeFeedSource,
    onAdd: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(start = 12.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(source.titleRes),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(onClick = onAdd) {
            Icon(
                painter = painterResource(R.drawable.ic_add_box_outlined),
                contentDescription = stringResource(R.string.home_feed_manage_add),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
