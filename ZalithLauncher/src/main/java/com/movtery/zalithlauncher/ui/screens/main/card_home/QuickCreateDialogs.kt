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

@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.movtery.zalithlauncher.ui.screens.main.card_home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.multiplayer.ServerListWriter
import com.movtery.zalithlauncher.game.version.saves.Difficulty
import com.movtery.zalithlauncher.game.version.saves.GameMode
import com.movtery.zalithlauncher.game.version.saves.WorldCreator
import com.movtery.zalithlauncher.ui.components.ImePanContainer
import com.movtery.zalithlauncher.ui.components.OwnOutlinedTextField
import com.movtery.zalithlauncher.ui.components.rememberDialogMaxHeight
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.utils.network.ServerAddress
import kotlinx.coroutines.launch

/** 创建世界时可选的三档模式（极限模式在 NBT 里是生存 + hardcore） */
private enum class CreateWorldMode(
    @field:StringRes val labelRes: Int
) {
    Survival(R.string.saves_manage_gamemode_survival),
    Creative(R.string.saves_manage_gamemode_creative),
    Hardcore(R.string.saves_manage_hardcore)
}

/**
 * 首页「+ 创建世界」对话框（议题 #33）
 *
 * 确认后写入 `saves/<文件夹名>/level.dat`，并把文件夹名交给 quickPlay。
 * 世界名与文件夹名分开：世界名可以包含任意文字，文件夹名只做最小限度的文件系统清洗。
 */
@Composable
fun CreateWorldDialog(
    instance: Version,
    onDismiss: () -> Unit,
    onCreated: (folderName: String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var seed by remember { mutableStateOf("") }
    var keepInventory by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf(CreateWorldMode.Survival) }
    var difficulty by remember { mutableStateOf(Difficulty.NORMAL) }
    var error by remember { mutableStateOf<String?>(null) }
    var working by remember { mutableStateOf(false) }

    QuickCreateDialogFrame(
        title = stringResource(R.string.home_create_world_title),
        working = working,
        error = error,
        confirmText = stringResource(R.string.home_create_world_action),
        onDismiss = onDismiss,
        onConfirm = {
            val levelName = name.trim()
            if (levelName.isEmpty()) {
                error = context.getString(R.string.home_quick_name_required)
            } else {
                working = true
                error = null
                scope.launch {
                    runCatching {
                        WorldCreator.createWorld(
                            version = instance,
                            levelName = levelName,
                            seed = WorldCreator.parseSeed(seed),
                            gameMode = when (mode) {
                                CreateWorldMode.Creative -> GameMode.CREATIVE
                                else -> GameMode.SURVIVAL
                            },
                            hardcore = mode == CreateWorldMode.Hardcore,
                            //极限模式在游戏内固定为困难
                            difficulty = if (mode == CreateWorldMode.Hardcore) Difficulty.HARD else difficulty,
                            keepInventory = keepInventory
                        )
                    }.onSuccess { folderName ->
                        working = false
                        onCreated(folderName)
                    }.onFailure { th ->
                        working = false
                        error = context.getString(
                            R.string.home_quick_create_failed,
                            th.message ?: th.javaClass.simpleName
                        )
                    }
                }
            }
        }
    ) {
        OwnOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.home_create_world_name)) },
            singleLine = true
        )

        OwnOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = seed,
            onValueChange = { seed = it },
            label = { Text(stringResource(R.string.home_create_world_seed)) },
            placeholder = { Text(stringResource(R.string.home_create_world_seed_hint)) },
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.home_create_world_keep_inventory),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = keepInventory,
                onCheckedChange = { keepInventory = it }
            )
        }

        Text(
            text = stringResource(R.string.home_create_world_game_mode),
            style = MaterialTheme.typography.labelLarge
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CreateWorldMode.entries.forEach { entry ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = mode == entry,
                        onClick = {
                            mode = entry
                            //极限模式在游戏内固定为困难，切换时同步一下界面
                            if (entry == CreateWorldMode.Hardcore) {
                                difficulty = Difficulty.HARD
                            }
                        }
                    )
                    Text(
                        text = stringResource(entry.labelRes),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.home_create_world_difficulty),
            style = MaterialTheme.typography.labelLarge
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Difficulty.entries.forEach { entry ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = difficulty == entry,
                        enabled = mode != CreateWorldMode.Hardcore,
                        onClick = { difficulty = entry }
                    )
                    Text(
                        text = stringResource(entry.nameRes),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

/**
 * 首页「+ 添加服务器」对话框（议题 #33）
 *
 * 直接追加到实例的 `servers.dat`，随后用 quickPlay 连接该地址。
 */
@Composable
fun AddServerDialog(
    instance: Version,
    onDismiss: () -> Unit,
    onAdded: (address: String) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var working by remember { mutableStateOf(false) }

    QuickCreateDialogFrame(
        title = stringResource(R.string.home_add_server_title),
        working = working,
        error = error,
        confirmText = stringResource(R.string.home_add_server_action),
        onDismiss = onDismiss,
        onConfirm = {
            val serverAddress = address.trim()
            val parsedAddress = runCatching { ServerAddress.parse(serverAddress) }
            if (serverAddress.isEmpty() || parsedAddress.isFailure) {
                error = context.getString(R.string.home_quick_address_invalid)
            } else {
                working = true
                error = null
                scope.launch {
                    runCatching {
                        ServerListWriter.addServer(
                            version = instance,
                            name = name,
                            address = serverAddress
                        )
                    }.onSuccess {
                        working = false
                        onAdded(serverAddress)
                    }.onFailure { th ->
                        working = false
                        error = context.getString(
                            R.string.home_quick_add_server_failed,
                            th.message ?: th.javaClass.simpleName
                        )
                    }
                }
            }
        }
    ) {
        OwnOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.home_add_server_name)) },
            singleLine = true
        )

        OwnOutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = address,
            onValueChange = { address = it },
            label = { Text(stringResource(R.string.home_add_server_address)) },
            placeholder = { Text(stringResource(R.string.home_add_server_address_hint)) },
            singleLine = true
        )
    }
}

/**
 * 两个快捷对话框共用的外壳：标题、可滚动表单、错误提示、取消/确认。
 */
@Composable
private fun QuickCreateDialogFrame(
    title: String,
    working: Boolean,
    error: String?,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = { if (!working) onDismiss() },
        properties = DialogProperties(decorFitsSystemWindows = false)
    ) {
        ImePanContainer(
            modifier = Modifier
                .heightIn(max = rememberDialogMaxHeight())
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .padding(all = 10.dp)
                    .widthIn(max = 560.dp)
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.extraLarge,
                color = cardColor(false),
                contentColor = onCardColor(),
                shadowElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(all = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        content()

                        error?.let { message ->
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilledTonalButton(
                            modifier = Modifier.weight(1f),
                            onClick = onDismiss,
                            enabled = !working
                        ) {
                            Text(stringResource(R.string.generic_cancel))
                        }
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = onConfirm,
                            enabled = !working
                        ) {
                            Text(
                                text = if (working) {
                                    stringResource(R.string.home_quick_processing)
                                } else {
                                    confirmText
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
