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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.frp.FrpManager
import com.movtery.zalithlauncher.frp.FrpNode
import com.movtery.zalithlauncher.frp.FrpRunState
import com.movtery.zalithlauncher.frp.FrpTunnel
import com.movtery.zalithlauncher.frp.FrpUser
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.OwnOutlinedTextField
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive

/** 樱花面板地址（实名认证 / 额度管理都在这里） */
private const val SAKURA_PANEL_URL = "https://www.natfrp.com/user/"

/**
 * 樱花内网穿透（SakuraFrp）管理页。
 *
 * 交互原则：
 * - **只让用户做必要的事**：类型固定 TCP，本机端口自动从当前进程的监听 socket 里识别；
 * - **失败要说人话**：接口错误、frpc 退出码、账号等级不足都有对应的可读提示；
 * - **按账号等级适配**：免费 / 白银 / 黄金等用户组能用的节点不同，列表只显示当前等级可用的节点。
 */
@Composable
fun FrpScreen(
    backScreenViewModel: ScreenBackStackViewModel,
    eventViewModel: EventViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val tunnels by FrpManager.tunnels.collectAsState()
    val nodes by FrpManager.nodes.collectAsState()
    val user by FrpManager.user.collectAsState()
    val runState by FrpManager.runState.collectAsState()
    val logs by FrpManager.logs.collectAsState()

    var loggedIn by remember { mutableStateOf(FrpManager.isLoggedIn) }
    var tokenInput by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingTunnel by remember { mutableStateOf<FrpTunnel?>(null) }
    var deletingTunnel by remember { mutableStateOf<FrpTunnel?>(null) }

    fun toast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    val quota = user?.tunnelQuota ?: 0
    val quotaFull = quota > 0 && tunnels.size >= quota
    val groupLevel = user?.groupLevel ?: 0

    BaseScreen(
        screenKey = NormalNavKey.Frp,
        currentKey = backScreenViewModel.mainScreen.currentKey
    ) { isVisible ->
        LaunchedEffect(isVisible, loggedIn) {
            if (isVisible && loggedIn) {
                runCatching {
                    FrpManager.refresh()
                    FrpManager.loadNodes()
                }.onFailure {
                    toast(context.getString(R.string.frp_error, it.message ?: ""))
                }
            }
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScrollWithBar(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!loggedIn) {
                    LoginCard(
                        token = tokenInput,
                        loading = loading,
                        onTokenChange = { tokenInput = it },
                        onLogin = {
                            val input = tokenInput.trim()
                            if (input.isEmpty()) {
                                toast(context.getString(R.string.frp_need_login))
                            } else {
                                scope.launch {
                                    loading = true
                                    runCatching { FrpManager.login(input) }
                                        .onSuccess {
                                            loggedIn = true
                                            tokenInput = ""
                                        }
                                        .onFailure {
                                            toast(context.getString(R.string.frp_error, it.message ?: ""))
                                        }
                                    loading = false
                                }
                            }
                        }
                    )
                } else {
                    AccountCard(
                        user = user,
                        tunnelCount = tunnels.size,
                        onLogout = {
                            FrpManager.logout()
                            loggedIn = false
                        }
                    )

                    if (user?.isRealName == false) {
                        NoticeCard(
                            title = stringResource(R.string.frp_realname_title),
                            summary = stringResource(R.string.frp_realname_summary),
                            action = stringResource(R.string.frp_realname_action),
                            onAction = { openUrl(context, SAKURA_PANEL_URL) }
                        )
                    }

                    if (quotaFull) {
                        NoticeCard(
                            title = stringResource(R.string.frp_quota_full, quota),
                            summary = stringResource(R.string.frp_quota_full_summary),
                            action = stringResource(R.string.frp_realname_action),
                            onAction = { openUrl(context, SAKURA_PANEL_URL) }
                        )
                    }

                    StatusCard(
                        state = runState,
                        tunnels = tunnels,
                        logs = logs,
                        onStop = { FrpManager.stop() }
                    )

                    //frpc 输出里的节点地址：直接摆在最上面，旁边一键复制
                    val logAddress = remember(logs) {
                        logs.asReversed().firstNotNullOfOrNull { extractAddress(it) }
                    }
                    logAddress?.let { address ->
                        BackgroundCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.frp_log_address_title),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = address,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        copyText(context, address)
                                        toast(context.getString(R.string.frp_copied))
                                    }
                                ) {
                                    Text(stringResource(R.string.frp_action_copy))
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.frp_tunnels_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        TextButton(
                            onClick = {
                                scope.launch {
                                    loading = true
                                    runCatching {
                                        FrpManager.refresh()
                                        FrpManager.loadNodes()
                                    }.onFailure {
                                        toast(context.getString(R.string.frp_error, it.message ?: ""))
                                    }
                                    loading = false
                                }
                            },
                            enabled = !loading
                        ) {
                            Text(stringResource(R.string.frp_tunnel_refresh))
                        }
                    }

                    if (tunnels.isEmpty()) {
                        BackgroundCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.frp_tunnels_empty),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = stringResource(R.string.frp_tunnels_empty_hint),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        tunnels.forEach { tunnel ->
                            TunnelCard(
                                tunnel = tunnel,
                                nodeName = nodes[tunnel.node.toString()]?.name,
                                address = FrpManager.tunnelAddress(tunnel),
                                running = (runState as? FrpRunState.Running)?.tunnelId == tunnel.id,
                                onStart = {
                                    if (FrpManager.tunnelAddress(tunnel) == null) {
                                        toast(context.getString(R.string.frp_address_unknown))
                                    }
                                    FrpManager.start(context, tunnel)
                                },
                                onStop = { FrpManager.stop() },
                                onEdit = { editingTunnel = tunnel },
                                onDelete = { deletingTunnel = tunnel },
                                onCopy = {
                                    val address = FrpManager.tunnelAddress(tunnel)
                                    if (address == null) {
                                        toast(context.getString(R.string.frp_address_unknown))
                                    } else {
                                        copyText(context, address)
                                        toast(context.getString(R.string.frp_copied))
                                    }
                                }
                            )
                        }
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (quotaFull) {
                                toast(context.getString(R.string.frp_quota_full, quota))
                            } else {
                                showCreateDialog = true
                                if (nodes.isEmpty()) {
                                    scope.launch {
                                        runCatching { FrpManager.loadNodes() }
                                            .onFailure {
                                                toast(context.getString(R.string.frp_error, it.message ?: ""))
                                            }
                                    }
                                }
                            }
                        },
                        enabled = !loading && !quotaFull
                    ) {
                        Text(stringResource(R.string.frp_tunnel_create))
                    }

                    BatteryCard()

                    LogsCard(
                        logs = logs,
                        onClear = { FrpManager.clearLogs() }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showCreateDialog) {
        TunnelDialog(
            tunnel = null,
            nodes = nodes,
            groupLevel = groupLevel,
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, node, localPort ->
                showCreateDialog = false
                scope.launch {
                    loading = true
                    runCatching {
                        //类型固定 TCP：Minecraft 联机只需要 TCP
                        FrpManager.createTunnel(
                            name = name,
                            type = "tcp",
                            node = node,
                            localIp = "127.0.0.1",
                            localPort = localPort,
                            remote = null
                        )
                    }.onFailure {
                        toast(context.getString(R.string.frp_error, it.message ?: ""))
                    }
                    loading = false
                }
            }
        )
    }

    editingTunnel?.let { tunnel ->
        TunnelDialog(
            tunnel = tunnel,
            nodes = nodes,
            groupLevel = groupLevel,
            onDismiss = { editingTunnel = null },
            onConfirm = { name, _, localPort ->
                editingTunnel = null
                scope.launch {
                    loading = true
                    runCatching {
                        FrpManager.editTunnel(
                            id = tunnel.id,
                            name = name,
                            localPort = localPort
                        )
                    }.onFailure {
                        toast(context.getString(R.string.frp_error, it.message ?: ""))
                    }
                    loading = false
                }
            }
        )
    }

    deletingTunnel?.let { tunnel ->
        SimpleAlertDialog(
            title = stringResource(R.string.frp_delete_title),
            text = stringResource(R.string.frp_delete_message, tunnel.name),
            onConfirm = {
                deletingTunnel = null
                scope.launch {
                    loading = true
                    runCatching { FrpManager.deleteTunnel(tunnel.id) }
                        .onFailure {
                            toast(context.getString(R.string.frp_error, it.message ?: ""))
                        }
                    loading = false
                }
            },
            onDismiss = { deletingTunnel = null }
        )
    }
}

@Composable
private fun LoginCard(
    token: String,
    loading: Boolean,
    onTokenChange: (String) -> Unit,
    onLogin: () -> Unit
) {
    BackgroundCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.frp_login_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.frp_login_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OwnOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = token,
                onValueChange = onTokenChange,
                label = { Text(stringResource(R.string.frp_token_hint)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                enabled = !loading
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onLogin,
                enabled = !loading
            ) {
                Text(
                    stringResource(
                        if (loading) R.string.frp_logging_in else R.string.frp_login_button
                    )
                )
            }
        }
    }
}

/**
 * 账号卡片：头像 + 名称 + 用户组 + 隧道额度 + 退出
 */
@Composable
private fun AccountCard(
    user: FrpUser?,
    tunnelCount: Int,
    onLogout: () -> Unit
) {
    val name = user?.name?.takeIf { it.isNotBlank() }
    BackgroundCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name?.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = name ?: stringResource(R.string.frp_account_unnamed),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = buildString {
                        val group = user?.groupName.orEmpty()
                        if (group.isNotBlank()) {
                            append(stringResource(R.string.frp_account_group, group))
                            append(" · ")
                        }
                        val quota = user?.tunnelQuota ?: 0
                        append(
                            if (quota > 0) {
                                stringResource(R.string.frp_account_quota, tunnelCount, quota)
                            } else {
                                stringResource(R.string.frp_account_tunnels, tunnelCount)
                            }
                        )
                        trafficText(user)?.let {
                            append(" · ")
                            append(it)
                        }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(onClick = onLogout) {
                Text(stringResource(R.string.frp_logout))
            }
        }
    }
}

/** 提示条：实名认证 / 额度已满 */
@Composable
private fun NoticeCard(
    title: String,
    summary: String,
    action: String,
    onAction: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            TextButton(
                modifier = Modifier.align(Alignment.End),
                onClick = onAction
            ) {
                Text(action)
            }
        }
    }
}

/**
 * 运行状态卡：状态点 + 状态文本；异常退出时把 frpc 最后一行输出带出来
 */
@Composable
private fun StatusCard(
    state: FrpRunState,
    tunnels: List<FrpTunnel>,
    logs: List<String>,
    onStop: () -> Unit
) {
    val dotColor = when (state) {
        is FrpRunState.Running -> Color(0xFF4CAF50)
        is FrpRunState.Starting -> Color(0xFFFFB300)
        is FrpRunState.Failed -> MaterialTheme.colorScheme.error
        is FrpRunState.Stopped -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }
    val running = state is FrpRunState.Running || state is FrpRunState.Starting
    val lastLine = logs.lastOrNull { it.isNotBlank() }

    BackgroundCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Text(
                    modifier = Modifier.weight(1f),
                    text = runStateText(state, tunnels),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                if (running) {
                    TextButton(onClick = onStop) {
                        Text(stringResource(R.string.frp_action_stop))
                    }
                }
            }

            val hint = when (state) {
                is FrpRunState.Failed -> state.message
                is FrpRunState.Stopped -> if (state.exitCode != 0) {
                    lastLine ?: stringResource(R.string.frp_status_stopped_hint)
                } else {
                    null
                }
                else -> null
            }
            hint?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun runStateText(state: FrpRunState, tunnels: List<FrpTunnel>): String {
    return when (state) {
        is FrpRunState.Idle -> stringResource(R.string.frp_status_idle)
        is FrpRunState.Starting -> stringResource(R.string.frp_status_starting)
        is FrpRunState.Running -> {
            val tunnel = tunnels.firstOrNull { it.id == state.tunnelId }
            val name = tunnel?.name?.takeIf { it.isNotBlank() }
                ?: tunnel?.let { "#${it.id}" }
                ?: "#${state.tunnelId}"
            stringResource(R.string.frp_status_running_tunnel, name)
        }
        is FrpRunState.Stopped -> stringResource(R.string.frp_status_stopped, state.exitCode)
        is FrpRunState.Failed -> stringResource(R.string.frp_status_failed, state.message)
    }
}

@Composable
private fun TunnelCard(
    tunnel: FrpTunnel,
    nodeName: String?,
    address: String?,
    running: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit
) {
    BackgroundCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onCopy
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = tunnel.name.ifBlank { "#${tunnel.id}" },
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                when {
                    tunnel.isBanned -> StatusChip(
                        text = stringResource(R.string.frp_tunnel_banned),
                        color = MaterialTheme.colorScheme.error
                    )
                    running -> StatusChip(
                        text = stringResource(R.string.frp_tunnel_running_chip),
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = address ?: stringResource(R.string.frp_address_unknown),
                style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                color = if (address != null) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = buildString {
                    append("TCP")
                    append(" · ")
                    append(stringResource(R.string.frp_tunnel_local_port, tunnel.localPort))
                    if (!nodeName.isNullOrBlank()) {
                        append(" · ")
                        append(nodeName)
                    }
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (tunnel.isBanned && !tunnel.statusReason.isNullOrBlank()) {
                Text(
                    text = tunnel.statusReason,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (running) {
                    TextButton(onClick = onStop) {
                        Text(stringResource(R.string.frp_action_stop))
                    }
                } else {
                    TextButton(onClick = onStart, enabled = !tunnel.isBanned) {
                        Text(stringResource(R.string.frp_action_start))
                    }
                }
                TextButton(onClick = onCopy) {
                    Text(stringResource(R.string.frp_action_copy))
                }
                TextButton(onClick = onEdit) {
                    Text(stringResource(R.string.frp_action_edit))
                }
                TextButton(onClick = onDelete) {
                    Text(stringResource(R.string.frp_action_delete))
                }
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun LogsCard(
    logs: List<String>,
    onClear: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    BackgroundCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(stringResource(R.string.frp_logs_title))
                }
                TextButton(onClick = onClear) {
                    Text(stringResource(R.string.frp_logs_clear))
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    if (logs.isEmpty()) {
                        Text(
                            text = stringResource(R.string.frp_logs_empty),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val context = LocalContext.current
                        val copiedTip = stringResource(R.string.frp_copied)
                        logs.takeLast(60).forEach { line ->
                            //日志里会出现节点地址（frpc 输出形如 `start proxy success` + `节点:端口`），
                            //点一下就能把其中的地址复制走，不用手抄
                            Text(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        copyText(context, extractAddress(line) ?: line)
                                        Toast.makeText(context, copiedTip, Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 1.dp),
                                text = line,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 电池优化：已允许时只留一行小字，未允许时才展示完整说明与按钮
 */
@Composable
private fun BatteryCard() {
    val context = LocalContext.current
    var allowed by remember { mutableStateOf(isIgnoringBatteryOptimizations(context)) }

    LaunchedEffect(Unit) {
        allowed = isIgnoringBatteryOptimizations(context)
    }

    if (allowed) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF4CAF50)
            )
            Text(
                text = stringResource(R.string.frp_battery_ok),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    BackgroundCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.frp_battery_title),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = stringResource(R.string.frp_battery_summary),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                modifier = Modifier.align(Alignment.End),
                onClick = { requestIgnoreBatteryOptimizations(context) }
            ) {
                Text(stringResource(R.string.frp_battery_action))
            }
        }
    }
}

/**
 * 新建 / 编辑隧道对话框。
 *
 * 28.0.1 起：类型固定 TCP（不再让用户选），本机端口自动检测（不再手填），
 * 节点只列当前账号等级可用的，保存前给出可读校验。
 */
@Composable
private fun TunnelDialog(
    tunnel: FrpTunnel?,
    nodes: Map<String, FrpNode>,
    groupLevel: Int,
    onDismiss: () -> Unit,
    onConfirm: (name: String, node: Int, localPort: Int) -> Unit
) {
    val defaultTunnelName = stringResource(R.string.frp_default_tunnel_name)
    var name by remember { mutableStateOf(tunnel?.name ?: defaultTunnelName) }
    var nodeId by remember { mutableStateOf(tunnel?.node ?: 0) }
    var nodeMenuExpanded by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var manualPort by remember { mutableStateOf(tunnel?.localPort?.takeIf { it in 1..65535 }?.toString().orEmpty()) }
    var detected by remember { mutableStateOf<List<Int>>(emptyList()) }
    var selectedPort by remember { mutableStateOf<Int?>(tunnel?.localPort?.takeIf { it in 1..65535 }) }
    var detectToken by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    //端口自动检测：MC 服务端与本应用同 uid，读 /proc/net/tcp* 即可拿到监听端口
    LaunchedEffect(detectToken) {
        val ports = FrpManager.detectLanPorts()
        detected = ports
        if (selectedPort == null || selectedPort !in ports) {
            selectedPort = ports.firstOrNull() ?: selectedPort
        }
    }

    //按账号等级过滤节点；过滤后为空时退回「所有可创建节点」，避免把用户彻底卡住
    val levelNodes = remember(nodes, groupLevel) {
        nodes.entries
            .filter { it.value.allowCreate && (groupLevel <= 0 || it.value.requiredLevel <= groupLevel) }
            .sortedBy { it.value.name }
    }
    val availableNodes = remember(nodes, levelNodes) {
        levelNodes.ifEmpty {
            nodes.entries.filter { it.value.allowCreate }.sortedBy { it.value.name }
        }
    }
    val showLevelWarning = levelNodes.isEmpty() && availableNodes.isNotEmpty()

    val selectedNodeName = availableNodes.firstOrNull { it.key == nodeId.toString() }?.value?.name
        ?: stringResource(R.string.frp_field_node)

    LaunchedEffect(availableNodes) {
        if (tunnel == null && nodeId == 0 && availableNodes.isNotEmpty()) {
            nodeId = availableNodes.first().key.toIntOrNull() ?: 0
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (tunnel == null) R.string.frp_create_title else R.string.frp_edit_title
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OwnOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = name,
                    onValueChange = {
                        name = it
                        errorText = null
                    },
                    label = { Text(stringResource(R.string.frp_field_name)) },
                    singleLine = true
                )

                if (tunnel == null) {
                    if (availableNodes.isEmpty()) {
                        Text(
                            text = stringResource(R.string.frp_no_nodes),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (showLevelWarning) {
                        Text(
                            text = stringResource(R.string.frp_nodes_level_warning),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Box {
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { nodeMenuExpanded = true }
                        ) {
                            Text(selectedNodeName)
                        }
                        DropdownMenu(
                            expanded = nodeMenuExpanded,
                            onDismissRequest = { nodeMenuExpanded = false }
                        ) {
                            availableNodes.forEach { (id, node) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (node.requiredLevel > 0) {
                                                "${node.name} · ${node.requiredLevel} 级"
                                            } else {
                                                node.name
                                            }
                                        )
                                    },
                                    onClick = {
                                        nodeId = id.toIntOrNull() ?: 0
                                        nodeMenuExpanded = false
                                        errorText = null
                                    }
                                )
                            }
                        }
                    }
                }

                //本机端口：自动识别，拿不到才让用户手填
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                detected.isNotEmpty() -> stringResource(
                                    R.string.frp_port_auto,
                                    selectedPort ?: detected.first()
                                )
                                else -> stringResource(R.string.frp_port_none)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (detected.isNotEmpty()) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                    TextButton(onClick = { detectToken++ }) {
                        Text(stringResource(R.string.frp_port_retry))
                    }
                }

                if (detected.isEmpty()) {
                    OwnOutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = manualPort,
                        onValueChange = { input ->
                            manualPort = input.filter { it.isDigit() }
                            errorText = null
                        },
                        label = { Text(stringResource(R.string.frp_field_local_port)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                errorText?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val port = selectedPort ?: manualPort.toIntOrNull()
                    errorText = when {
                        name.isBlank() -> context.getString(R.string.frp_error_name_required)
                        port == null || port !in 1..65535 -> context.getString(R.string.frp_error_port_invalid)
                        tunnel == null && nodeId == 0 -> context.getString(R.string.frp_error_node_required)
                        else -> null
                    }
                    if (errorText == null) {
                        onConfirm(name.trim(), nodeId, port!!)
                    }
                }
            ) {
                Text(stringResource(R.string.frp_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.generic_cancel))
            }
        }
    )
}

/** 流量展示：`[今日消耗, 总剩余]`（字节）*/
private fun trafficText(user: FrpUser?): String? {
    val array = user?.traffic as? JsonArray ?: return null
    if (array.size < 2) return null
    val total = (array[1] as? JsonPrimitive)?.content?.toLongOrNull() ?: return null
    if (total <= 0) return null
    return formatBytes(total)
}

private fun formatBytes(bytes: Long): String {
    val gb = bytes / 1024.0 / 1024.0 / 1024.0
    return if (gb >= 1) String.format("%.1f GB", gb)
    else String.format("%.0f MB", bytes / 1024.0 / 1024.0)
}


/** 从 frpc 日志里抽出可复制的地址（`host:port`），抽不到就返回 null �� 由调用方复制整行 */
private val ADDRESS_REGEX = Regex("([A-Za-z0-9._-]+\\.(?:natfrp\\.(?:io|com|net)|[A-Za-z0-9.-]+)):(\\d{2,5})")

private fun extractAddress(line: String): String? =
    ADDRESS_REGEX.find(line)?.value

private fun openUrl(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}

private fun copyText(context: Context, text: String) {
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    manager?.setPrimaryClip(ClipData.newPlainText("SakuraFrp", text))
}

private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    val manager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
    return manager.isIgnoringBatteryOptimizations(context.packageName)
}

private fun requestIgnoreBatteryOptimizations(context: Context) {
    runCatching {
        context.startActivity(
            Intent(
                Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                Uri.parse("package:${context.packageName}")
            )
        )
    }.onFailure {
        runCatching {
            context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
        }
    }
}
