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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.frp.FrpManager
import com.movtery.zalithlauncher.frp.FrpNode
import com.movtery.zalithlauncher.frp.FrpRunState
import com.movtery.zalithlauncher.frp.FrpTunnel
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.CheckChip
import com.movtery.zalithlauncher.ui.components.OwnOutlinedTextField
import com.movtery.zalithlauncher.ui.components.SimpleAlertDialog
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import kotlinx.coroutines.launch

/**
 * 樱花内网穿透（SakuraFrp）管理页。
 *
 * 设计目标：
 * - **填完访问密钥就能用**：登录后直接列出隧道，点「启动」即可；
 * - **能力对齐官方启动器**：新建 / 编辑 / 删除隧道、查看在线状态与地址；
 * - **原生 UI**：复用启动器既有的卡片、输入框与对话框，不引入 WebView。
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

    LaunchedEffect(loggedIn) {
        if (loggedIn) {
            runCatching {
                FrpManager.refresh()
                FrpManager.loadNodes()
            }
        }
    }

    BaseScreen(
        screenKey = NormalNavKey.Frp,
        currentKey = backScreenViewModel.mainScreen.currentKey
    ) { isVisible ->
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
                        name = user?.name?.takeIf { it.isNotBlank() }
                            ?: context.getString(R.string.frp_account_unnamed),
                        onLogout = {
                            FrpManager.logout()
                            loggedIn = false
                        }
                    )

                    BatteryCard()

                    RunStateCard(
                        state = runState,
                        tunnels = tunnels,
                        onStop = { FrpManager.stop() }
                    )

                    Text(
                        text = stringResource(R.string.frp_tunnels_title),
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (tunnels.isEmpty()) {
                        Text(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            text = stringResource(R.string.frp_tunnels_empty),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        tunnels.forEach { tunnel ->
                            TunnelCard(
                                tunnel = tunnel,
                                nodeName = nodes[tunnel.node.toString()]?.name,
                                address = FrpManager.tunnelAddress(tunnel),
                                running = (runState as? FrpRunState.Running)?.tunnelId == tunnel.id,
                                onStart = {
                                    if (FrpManager.tunnelAddress(tunnel) == null) {
                                        // 地址拿不到也能启动，只是无法在这里展示连接地址
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
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

                        Button(
                            onClick = { showCreateDialog = true },
                            enabled = !loading
                        ) {
                            Text(stringResource(R.string.frp_tunnel_create))
                        }
                    }

                    LogsCard(
                        logs = logs,
                        onClear = { FrpManager.clearLogs() }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (showCreateDialog) {
                TunnelDialog(
                    tunnel = null,
                    nodes = nodes,
                    onDismiss = { showCreateDialog = false },
                    onConfirm = { name, type, node, localPort, remote ->
                        showCreateDialog = false
                        scope.launch {
                            loading = true
                            runCatching {
                                FrpManager.createTunnel(
                                    name = name,
                                    type = type,
                                    node = node,
                                    localIp = "127.0.0.1",
                                    localPort = localPort,
                                    remote = remote
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
                    onDismiss = { editingTunnel = null },
                    onConfirm = { name, type, _, localPort, remote ->
                        editingTunnel = null
                        scope.launch {
                            loading = true
                            runCatching {
                                FrpManager.editTunnel(
                                    id = tunnel.id,
                                    name = name,
                                    localPort = localPort,
                                    remote = remote
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
            modifier = Modifier.padding(all = 16.dp),
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

@Composable
private fun AccountCard(
    name: String,
    onLogout: () -> Unit
) {
    BackgroundCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.frp_account, name),
                style = MaterialTheme.typography.titleSmall
            )
            TextButton(onClick = onLogout) {
                Text(stringResource(R.string.frp_logout))
            }
        }
    }
}

@Composable
private fun BatteryCard() {
    val context = LocalContext.current
    var allowed by remember { mutableStateOf(isIgnoringBatteryOptimizations(context)) }

    LaunchedEffect(Unit) {
        allowed = isIgnoringBatteryOptimizations(context)
    }

    BackgroundCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.frp_battery_title),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = stringResource(
                    if (allowed) R.string.frp_battery_done else R.string.frp_battery_summary
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!allowed) {
                TextButton(onClick = { requestIgnoreBatteryOptimizations(context) }) {
                    Text(stringResource(R.string.frp_battery_action))
                }
            }
        }
    }
}

@Composable
private fun RunStateCard(
    state: FrpRunState,
    tunnels: List<FrpTunnel>,
    onStop: () -> Unit
) {
    BackgroundCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = runStateText(state, tunnels),
                style = MaterialTheme.typography.bodyMedium
            )
            if (state is FrpRunState.Running || state is FrpRunState.Starting) {
                TextButton(onClick = onStop) {
                    Text(stringResource(R.string.frp_action_stop))
                }
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
            val address = tunnel?.let { FrpManager.tunnelAddress(it) }
            if (address.isNullOrBlank()) {
                stringResource(R.string.frp_address_unknown)
            } else {
                stringResource(R.string.frp_status_running, address)
            }
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
    BackgroundCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = tunnel.name.ifBlank { "#${tunnel.id}" },
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = if (tunnel.isBanned) {
                        stringResource(R.string.frp_tunnel_banned)
                    } else {
                        stringResource(if (tunnel.online) R.string.frp_tunnel_online else R.string.frp_tunnel_offline)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (tunnel.online) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${tunnel.type.uppercase()} · " +
                    stringResource(R.string.frp_tunnel_local_port, tunnel.localPort) +
                    (nodeName?.let { " · " + stringResource(R.string.frp_tunnel_node) + " " + it } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = address ?: stringResource(R.string.frp_address_unknown),
                style = MaterialTheme.typography.bodyMedium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
private fun LogsCard(
    logs: List<String>,
    onClear: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    BackgroundCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                Column {
                    if (logs.isEmpty()) {
                        Text(
                            text = stringResource(R.string.frp_logs_empty),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        logs.takeLast(50).forEach { line ->
                            Text(
                                text = line,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TunnelDialog(
    tunnel: FrpTunnel?,
    nodes: Map<String, FrpNode>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String, node: Int, localPort: Int, remote: String?) -> Unit
) {
    var name by remember { mutableStateOf(tunnel?.name.orEmpty()) }
    var type by remember { mutableStateOf(tunnel?.type ?: "tcp") }
    var nodeId by remember { mutableStateOf(tunnel?.node ?: 0) }
    var localPort by remember { mutableStateOf(tunnel?.localPort?.toString().orEmpty()) }
    var remote by remember { mutableStateOf(tunnel?.remote.orEmpty()) }
    var nodeMenuExpanded by remember { mutableStateOf(false) }
    var portStatus by remember { mutableStateOf<String?>(null) }

    val availableNodes = remember(nodes) {
        nodes.entries
            .filter { it.value.allowCreate }
            .sortedBy { it.value.name }
    }

    val selectedNodeName = availableNodes.firstOrNull { it.key == nodeId.toString() }?.value?.name
        ?: stringResource(R.string.frp_field_node)

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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OwnOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.frp_field_name)) },
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("tcp", "udp").forEach { candidate ->
                        CheckChip(
                            selected = type == candidate,
                            label = { Text(candidate.uppercase()) },
                            onClick = { type = candidate }
                        )
                    }
                }

                if (tunnel == null) {
                    if (availableNodes.isEmpty()) {
                        Text(
                            text = stringResource(R.string.frp_no_nodes),
                            style = MaterialTheme.typography.bodySmall,
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
                                    text = { Text(node.name) },
                                    onClick = {
                                        nodeId = id.toIntOrNull() ?: 0
                                        nodeMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OwnOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = localPort,
                    onValueChange = { input ->
                        localPort = input.filter { it.isDigit() }
                        portStatus = null
                    },
                    label = { Text(stringResource(R.string.frp_field_local_port)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OwnOutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = remote,
                    onValueChange = { remote = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.frp_field_remote_port)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            val port = localPort.toIntOrNull()
                            portStatus = if (port == null || port !in 1..65535) {
                                null
                            } else if (FrpManager.isLocalPortInUse(port)) {
                                "in_use"
                            } else {
                                "free"
                            }
                        }
                    ) {
                        Text(stringResource(R.string.frp_port_check))
                    }
                    portStatus?.let { status ->
                        val port = localPort.toIntOrNull() ?: 0
                        Text(
                            text = if (status == "in_use") {
                                stringResource(R.string.frp_port_in_use, port)
                            } else {
                                stringResource(R.string.frp_port_free, port)
                            },
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val port = localPort.toIntOrNull()
                    if (name.isBlank() || port == null || port !in 1..65535) return@TextButton
                    if (tunnel == null && nodeId == 0) return@TextButton
                    onConfirm(name.trim(), type, nodeId, port, remote.ifBlank { null })
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

private fun copyText(context: Context, text: String) {
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    manager?.setPrimaryClip(ClipData.newPlainText("SakuraFrp", text))
}

private fun isIgnoringBatteryOptimizations(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        ?: return false
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}

private fun requestIgnoreBatteryOptimizations(context: Context) {
    val direct = Intent(
        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
        Uri.parse("package:${context.packageName}")
    )
    runCatching { context.startActivity(direct) }
        .onFailure {
            runCatching {
                context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        }
}
