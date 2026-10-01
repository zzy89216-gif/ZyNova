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

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ai.AISettings
import com.movtery.zalithlauncher.ai.conversation.AIConversationMeta
import com.movtery.zalithlauncher.ai.model.AIMessage
import com.movtery.zalithlauncher.ai.model.AIRole
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.components.SmallOutlinedEditField
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.theme.backgroundColor
import com.movtery.zalithlauncher.ui.theme.onBackgroundColor
import com.movtery.zalithlauncher.viewmodel.AIChatViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel

/**
 * AI 聊天界面（同时就是 Agent 界面）
 *
 * 聊天与 Agent 是**同一个入口**：
 * 用户可以先问「为什么进不去」，再直接说「帮我修」，
 * Agent 会接着上文继续调用工具处理，而不是重新要求用户手动操作。
 *
 * 左上角可以拉开**历史对话侧边栏**，之前的对话都会留在本机。
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
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val currentId by viewModel.currentConversationId.collectAsStateWithLifecycle()

    // 把「启动游戏」注入给 Agent：和用户点启动走同一条链路
    LaunchedEffect(onLaunchGame) {
        viewModel.launchGame = { version ->
            onLaunchGame(version)
            true
        }
    }

    var input by remember { mutableStateOf("") }
    var drawerOpen by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var deletingId by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()

    // 是否已经贴底。用户往上翻看历史时不要把他拽回底部。
    val atBottom by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf true
            last.index >= listState.layoutInfo.totalItemsCount - 1
        }
    }

    // 新消息进来时：只有本来就在底部才自动跟随。
    // 用 scrollToItem（瞬时）而不是 animateScrollToItem —— 流式输出时每来一段就做一次动画会又卡又晕。
    LaunchedEffect(messages.size, messages.lastOrNull()?.text?.length, busy) {
        if (messages.isNotEmpty() && atBottom) {
            listState.scrollToItem(messages.lastIndex)
        }
    }

    BaseScreen(
        screenKey = key,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {

            Column(modifier = Modifier.fillMaxSize()) {

                // ── 顶部：侧边栏 + 模型信息 + 配置入口 ───────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { drawerOpen = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_menu),
                            contentDescription = stringResource(R.string.ai_conversations)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AISettings.provider.state.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = AISettings.model.state
                                .ifBlank { stringResource(R.string.ai_model_not_selected) },
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = { showClearConfirm = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_autorenew),
                            contentDescription = stringResource(R.string.ai_clear)
                        )
                    }

                    IconButton(onClick = toAIConfig) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings_filled),
                            contentDescription = stringResource(R.string.ai_config_title)
                        )
                    }
                }

                // ── 消息列表 ─────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
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
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            val text = input
                            if (text.isNotBlank() && !busy) {
                                input = ""
                                viewModel.send(text)
                            }
                        }),
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

            // ── 历史对话侧边栏（盖在内容上）──────────────────────────
            if (drawerOpen) {
                // 遮罩：点一下就关
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                        .pointerInput(Unit) { detectTapGestures { drawerOpen = false } }
                )
            }

            AnimatedVisibility(
                visible = drawerOpen,
                enter = slideInHorizontally { -it } + fadeIn(),
                exit = slideOutHorizontally { -it } + fadeOut(),
            ) {
                ConversationSidebar(
                    modifier = Modifier
                        .width(292.dp)
                        .fillMaxHeight(),
                    conversations = conversations,
                    currentId = currentId,
                    onNew = {
                        viewModel.newConversation()
                        drawerOpen = false
                    },
                    onOpen = { id ->
                        viewModel.openConversation(id)
                        drawerOpen = false
                    },
                    onDelete = { deletingId = it },
                    onClose = { drawerOpen = false },
                )
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

    // ── 清空当前对话（二次确认，避免误触丢掉上下文）────────────
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(stringResource(R.string.ai_clear_confirm_title)) },
            text = { Text(stringResource(R.string.ai_clear_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showClearConfirm = false
                    viewModel.clear()
                }) { Text(stringResource(R.string.ai_clear)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(stringResource(R.string.generic_cancel))
                }
            }
        )
    }

    // ── 删除历史对话确认 ──────────────────────────────────────
    deletingId?.let { id ->
        AlertDialog(
            onDismissRequest = { deletingId = null },
            title = { Text(stringResource(R.string.ai_delete_conversation)) },
            text = { Text(stringResource(R.string.ai_delete_conversation_message)) },
            confirmButton = {
                TextButton(onClick = {
                    deletingId = null
                    viewModel.deleteConversation(id)
                }) {
                    Text(
                        text = stringResource(R.string.ai_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingId = null }) {
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

// ─────────────────────────────────────────────────────────────────
//  历史对话侧边栏
// ─────────────────────────────────────────────────────────────────

@Composable
private fun ConversationSidebar(
    conversations: List<AIConversationMeta>,
    currentId: String?,
    onNew: () -> Unit,
    onOpen: (String) -> Unit,
    onDelete: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = backgroundColor(),
        contentColor = onBackgroundColor(),
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.ai_conversations),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.generic_cancel)
                    )
                }
            }

            // 新建对话
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clip(MaterialTheme.shapes.large)
                    .clickable(onClick = onNew)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.ai_new_conversation),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(6.dp))

            if (conversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.ai_no_conversations),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(conversations, key = { it.id }) { meta ->
                        ConversationRow(
                            meta = meta,
                            selected = meta.id == currentId,
                            onClick = { onOpen(meta.id) },
                            onDelete = { onDelete(meta.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(
    meta: AIConversationMeta,
    selected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val background = if (selected) MaterialTheme.colorScheme.primaryContainer
    else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(background)
            .clickable(onClick = onClick)
            .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = meta.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = relativeTime(meta.updatedAt) +
                        if (meta.messageCount > 0) " · ${meta.messageCount}" else "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(R.drawable.ic_delete_outlined),
                contentDescription = stringResource(R.string.ai_delete_conversation),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** 相对时间（刚刚 / 5 分钟前 / 昨天 / 2026-09-30） */
private fun relativeTime(timestamp: Long): String =
    DateUtils.getRelativeTimeSpanString(
        timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.FORMAT_ABBREV_RELATIVE
    ).toString()

// ─────────────────────────────────────────────────────────────────
//  消息渲染
// ─────────────────────────────────────────────────────────────────

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
                ThinkingRow()
            }
        }

        AIRole.TOOL -> message.toolResults.forEach { result ->
            Bubble(
                color = when {
                    result.running -> MaterialTheme.colorScheme.surfaceVariant
                    result.isError -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.secondaryContainer
                },
                alignEnd = false
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (result.running) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = "🔧 ${result.name}" +
                                if (result.writeOperation) " · " + stringResource(R.string.ai_tool_write) else "",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (result.running) {
                    Text(
                        modifier = Modifier.padding(top = 2.dp),
                        text = stringResource(R.string.ai_tool_running),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        modifier = Modifier.padding(top = 2.dp),
                        text = result.content.take(1500),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        AIRole.SYSTEM -> Unit
    }
}

@Composable
private fun ThinkingRow() {
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

@Composable
private fun Bubble(
    color: Color,
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
