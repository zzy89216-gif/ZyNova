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

package com.movtery.zalithlauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.toAndroidString

/**
 * 资源下载进度条（模组 / 整合包 / 材质包 / 光影等）
 *
 * 反馈：下载资源时看不到任何进度，只能干等。
 * 这里把全局任务队列里「正在跑的任务」摊开显示：
 * - 常驻一条细进度条 + 当前在做什么（文件名 / 校验 / 安装）；
 * - 点一下弹出明细，能看到每个任务的进度与阶段，也能单个取消。
 *
 * ⚠️ 数据源是 `TaskSystem.tasksFlow`（和启动器顶部任务菜单同一个来源），
 * 所以不会另起一套状态，也不会和已有任务重复显示。
 */
@Composable
fun DownloadTasksIndicator(
    modifier: Modifier = Modifier,
    /** 只显示 id 满足该前缀的任务；为空表示显示全部 */
    idPrefix: String? = null,
) {
    val context = LocalContext.current
    val allTasks by TaskSystem.tasksFlow.collectAsStateWithLifecycle()
    val tasks = remember(allTasks, idPrefix) {
        if (idPrefix == null) allTasks else allTasks.filter { it.id.startsWith(idPrefix) }
    }

    if (tasks.isEmpty()) return

    var showDetails by remember { mutableStateOf(false) }
    val top = tasks.first()
    val progress by top.progress.collectAsStateWithLifecycle()
    val message by top.message.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(cardColor())
            .clickable { showDetails = true }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = message?.toAndroidString(context)
                ?: stringResource(R.string.download_tasks_running, tasks.size),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 2
        )
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = stringResource(R.string.download_tasks_tap_for_details),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showDetails) {
        AlertDialog(
            onDismissRequest = { showDetails = false },
            title = { Text(stringResource(R.string.download_tasks_title)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    tasks.forEach { task ->
                        TaskDetailRow(task)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDetails = false }) {
                    Text(stringResource(R.string.generic_confirm))
                }
            }
        )
    }
}

@Composable
private fun TaskDetailRow(task: Task) {
    val context = LocalContext.current
    val progress by task.progress.collectAsStateWithLifecycle()
    val message by task.message.collectAsStateWithLifecycle()
    val stage by task.stage.collectAsStateWithLifecycle()

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = message?.toAndroidString(context) ?: task.id,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${(progress.coerceIn(0f, 1f) * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall
            )
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stage.name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = { TaskSystem.cancelTask(task.id) }) {
                Text(stringResource(R.string.generic_cancel))
            }
        }
    }
}
