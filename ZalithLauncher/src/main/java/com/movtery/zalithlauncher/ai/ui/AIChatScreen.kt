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

package com.movtery.zalithlauncher.ai.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ai.AISettings
import com.movtery.zalithlauncher.ai.model.AIMessage
import com.movtery.zalithlauncher.ai.model.AIRole
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.SmallOutlinedEditField
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.viewmodel.AIChatViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

/**
 * AI 聊天界面（同时就是 Agent 界面）
 *
 * 聊天与 Agent 是**同一个入口**：
 * 用户可以先问「为什么进不去」，再直接说「帮我修」，
 * Agent 会接着上文继续调用工具处理，而不是重新要求用户手动操作。
 */
@Composable
fun AIChatScreen(
    key: NormalNavKey.AIChat,
    backStackViewModel: ScreenBackStackViewModel,
    toAIConfig: () -> Unit = {},
    /** 真正启动游戏：由 MainScreen 走启动器现有的启动事件链路 */
    onLaunchGame: (Version) -> Unit = {},
) {
    val viewModel: AIChatViewModel = hiltViewModel(key = "zynova_ai_chat")

    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val pendingConfirm by viewModel.pendingConfirm.collectAsStateWithLifecycle()

    // 把「启动游戏」注入给 Agent：和用户点启动走同一条链路
    LaunchedEffect(onLaunchGame) {
        viewModel.launchGame = { version ->
            onLaunchGame(version)
            true
        }
    }

    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // 自动滚到底部
    LaunchedEffect(messages.size, messages.lastOrNull()?.text) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    BaseScreen(
        screenKey = key,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { _ ->
        Column(modifier = Modifier.fillMaxSize()) {

            // ── 顶部：模型信息 + 配置入口 ─────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val model = AISettings.model.state
                    Text(
                        text = AISettings.provider.state.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = model.ifBlank { stringResource(R.string.ai_model_not_selected) },
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                TextButton(onClick = viewModel::clear) {
                    Text(stringResource(R.string.ai_clear))
                }

                IconButton(onClick = toAIConfig) {
                    Icon(
                        painter = painterResource(R.drawable.ic_settings_filled),
                        contentDescription = stringResource(R.string.ai_config_title)
                    )
                }
            }

            // ── 消息列表 ─────────────────────────────────────────
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (messages.isEmpty()) {
                    EmptyHint(
                        modifier = Modifier.align(Alignment.Center),
                        hasKey = AISettings.hasKey(),
                        onOpenConfig = toAIConfig,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages, key = { it.id }) { message ->
                            MessageBubble(message)
                        }
                    }
                }
            }

            // ── 输入区 ───────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                SmallOutlinedEditField(
                    modifier = Modifier.weight(1f),
                    value = input,
                    onValueChange = { input = it },
                    label = { Text(stringResource(R.string.ai_input_hint)) },
                    singleLine = false,
                )
                Spacer(Modifier.width(8.dp))
                if (busy) {
                    IconButton(onClick = viewModel::stop) {
                        Icon(
                            painter = painterResource(R.drawable.ic_stop),
                            contentDescription = stringResource(R.string.ai_stop),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    IconButton(
                        enabled = input.isNotBlank(),
                        onClick = {
                            val text = input
                            input = ""
                            viewModel.send(text)
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_send),
                            contentDescription = stringResource(R.string.ai_send),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    // ── 配置缺失提示 ──────────────────────────────────────────
    error?.let { text ->
        AlertDialog(
            onDismissRequest = viewModel::dismissError,
            title = { Text(stringResource(R.string.ai_error_title)) },
            text = { Text(text) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissError()
                    toAIConfig()
                }) { Text(stringResource(R.string.ai_config_title)) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissError) {
                    Text(stringResource(R.string.generic_cancel))
                }
            }
        )
    }

    // ── 「操作确认」模式的确认弹窗 ────────────────────────────
    pendingConfirm?.let { pending ->
        AlertDialog(
            onDismissRequest = { viewModel.resolveConfirm(false) },
            title = { Text(stringResource(R.string.ai_confirm_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.ai_confirm_message, pending.toolName))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = pending.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.resolveConfirm(true) }) {
                    Text(stringResource(R.string.ai_confirm_allow))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.resolveConfirm(false) }) {
                    Text(stringResource(R.string.ai_confirm_deny))
                }
            }
        )
    }
}

@Composable
private fun MessageBubble(message: AIMessage) {
    when (message.role) {
        AIRole.USER -> Bubble(
            color = MaterialTheme.colorScheme.primaryContainer,
            alignEnd = true
        ) {
            Text(message.text, style = MaterialTheme.typography.bodyMedium)
        }

        AIRole.ASSISTANT -> Bubble(
            color = MaterialTheme.colorScheme.surfaceVariant,
            alignEnd = false
        ) {
            if (message.text.isNotBlank()) {
                Text(message.text, style = MaterialTheme.typography.bodyMedium)
            }
            if (message.toolCalls.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(top = if (message.text.isBlank()) 0.dp else 6.dp),
                    text = stringResource(
                        R.string.ai_tool_calls,
                        message.toolCalls.joinToString(", ") { it.name }
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            message.error?.let {
                Text(
                    modifier = Modifier.padding(top = 6.dp),
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            if (message.streaming && message.text.isBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.ai_thinking),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        AIRole.TOOL -> message.toolResults.forEach { result ->
            Bubble(
                color = if (result.isError) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.secondaryContainer,
                alignEnd = false
            ) {
                Text(
                    text = "🔧 ${result.name}" + if (result.writeOperation) " · 写操作" else "",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    modifier = Modifier.padding(top = 2.dp),
                    text = result.content.take(1500),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        AIRole.SYSTEM -> Unit
    }
}

@Composable
private fun Bubble(
    color: androidx.compose.ui.graphics.Color,
    alignEnd: Boolean,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (alignEnd) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.86f),
            color = color,
            shape = MaterialTheme.shapes.large,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(10.dp)) { content() }
        }
    }
}

@Composable
private fun EmptyHint(
    modifier: Modifier = Modifier,
    hasKey: Boolean,
    onOpenConfig: () -> Unit,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.ai_welcome),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(
                if (hasKey) R.string.ai_welcome_hint else R.string.ai_welcome_need_key
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!hasKey) {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onOpenConfig) {
                Text(stringResource(R.string.ai_config_title))
            }
        }
    }
}
