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

package com.movtery.zalithlauncher.ui.screens.main.card_home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.home.HomeDataProvider
import com.movtery.zalithlauncher.game.home.HomeInstance
import com.movtery.zalithlauncher.game.home.HomeLayoutStore
import com.movtery.zalithlauncher.game.home.HomeServer
import com.movtery.zalithlauncher.game.home.HomeWorld
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.components.ReorderState
import com.movtery.zalithlauncher.ui.components.reorderItem
import com.movtery.zalithlauncher.ui.components.rememberReorderState
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor

/** 实例卡片在排序中的稳定标识 */
private fun HomeInstance.orderKey(): String = instance.getVersionName()

/** 世界条目在排序中的稳定标识（同一个实例内唯一） */
private fun HomeWorld.orderKey(): String = name

/** 服务器条目在排序中的稳定标识 */
private fun HomeServer.orderKey(): String =
    "${server.name}#${server.originIp}"

/**
 * 卡片式主页
 *
 * 以「游戏版本」为模块组织内容：每个已安装的实例是一个模块，
 * 模块内部直接展示该实例自己的本地世界与已保存服务器。
 *
 * 26.2.5 起支持**长按拖动排序**：版本卡片之间、以及同一张卡片内的
 * 世界 / 服务器都可以拖动调整顺序，顺序会被记住。
 *
 * 数据按需加载：只有真正进入主页时才读取，且限量扫描，
 * 启动器启动时不会进行一次性的全盘扫描。
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun cardHomePage(
    modifier: Modifier = Modifier,
    onLaunchVersion: (Version) -> Unit = {},
    onPlayWorld: (Version, String) -> Unit = { _, _ -> },
    onJoinServer: (Version, String) -> Unit = { _, _ -> },
) {
    var instances by remember { mutableStateOf<List<HomeInstance>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }

    //卡片大小（百分比）：100% 与既有外观完全一致，
    //调小可以让更多实例卡片同屏显示，调大则更易点击
    val cardScale = AllSettings.homeCardSize.state.toFloat() / 100f

    LaunchedEffect(Unit) {
        //按用户上次拖动后的顺序恢复；没有记录过就保持默认顺序
        instances = HomeLayoutStore.sort(
            items = HomeDataProvider.instances(),
            order = HomeLayoutStore.load(HomeLayoutStore.KEY_CARDS),
            keyOf = { it.orderKey() }
        ).map { instance ->
            val name = instance.orderKey()
            instance.copy(
                worlds = HomeLayoutStore.sort(
                    items = instance.worlds,
                    order = HomeLayoutStore.load(HomeLayoutStore.keyWorlds(name)),
                    keyOf = { it.orderKey() }
                ),
                servers = HomeLayoutStore.sort(
                    items = instance.servers,
                    order = HomeLayoutStore.load(HomeLayoutStore.keyServers(name)),
                    keyOf = { it.orderKey() }
                )
            )
        }
        loaded = true
    }

    //版本卡片之间的拖动排序
    val cardReorder = rememberReorderState { dragged, target ->
        val from = instances.indexOfFirst { it.orderKey() == dragged }
        val to = instances.indexOfFirst { it.orderKey() == target }
        if (from >= 0 && to >= 0) {
            instances = HomeLayoutStore.move(instances, from, to)
            HomeLayoutStore.save(HomeLayoutStore.KEY_CARDS, instances.map { it.orderKey() })
        }
    }

    if (!loaded) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            LoadingIndicator()
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp * cardScale)
    ) {
        if (instances.isEmpty()) {
            Text(
                text = stringResource(R.string.home_instance_none),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            instances.forEach { instance ->
                val instanceKey = instance.orderKey()
                InstanceModule(
                    instance = instance,
                    scale = cardScale,
                    reorderState = cardReorder,
                    reorderKey = instanceKey,
                    onWorldsReordered = { worlds ->
                        instances = instances.map { current ->
                            if (current.orderKey() == instanceKey) current.copy(worlds = worlds) else current
                        }
                        HomeLayoutStore.save(
                            HomeLayoutStore.keyWorlds(instanceKey),
                            worlds.map { it.orderKey() }
                        )
                    },
                    onServersReordered = { servers ->
                        instances = instances.map { current ->
                            if (current.orderKey() == instanceKey) current.copy(servers = servers) else current
                        }
                        HomeLayoutStore.save(
                            HomeLayoutStore.keyServers(instanceKey),
                            servers.map { it.orderKey() }
                        )
                    },
                    onLaunch = { onLaunchVersion(instance.instance) },
                    onPlayWorld = onPlayWorld,
                    onJoinServer = onJoinServer
                )
            }
        }
    }
}

/**
 * 单个游戏实例模块
 *
 * 顶部是该实例的版本信息与启动入口，
 * 下面按「世界 / 服务器」两组直接列出属于该实例的内容。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InstanceModule(
    instance: HomeInstance,
    /** 卡片大小比例（1f = 100%） */
    scale: Float,
    reorderState: ReorderState,
    reorderKey: String,
    onWorldsReordered: (List<HomeWorld>) -> Unit,
    onServersReordered: (List<HomeServer>) -> Unit,
    onLaunch: () -> Unit,
    onPlayWorld: (Version, String) -> Unit,
    onJoinServer: (Version, String) -> Unit,
) {
    //该实例内部的世界 / 服务器各自的拖动排序
    val worldReorder = rememberReorderState { dragged, target ->
        val from = instance.worlds.indexOfFirst { it.orderKey() == dragged }
        val to = instance.worlds.indexOfFirst { it.orderKey() == target }
        if (from >= 0 && to >= 0) {
            onWorldsReordered(HomeLayoutStore.move(instance.worlds, from, to))
        }
    }
    val serverReorder = rememberReorderState { dragged, target ->
        val from = instance.servers.indexOfFirst { it.orderKey() == dragged }
        val to = instance.servers.indexOfFirst { it.orderKey() == target }
        if (from >= 0 && to >= 0) {
            onServersReordered(HomeLayoutStore.move(instance.servers, from, to))
        }
    }

    //⚠️ 卡片只有**一层**背景：圆角裁剪 + 卡片色，不叠加描边、不使用阴影。
    //之前用 Surface + shadowElevation + graphicsLayer 缩放时，
    //会在卡片边缘形成一圈可见的「边框」（内边距环与内容区明暗不一致），见反馈。
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(cardColor())
            .clickable(onClick = onLaunch)
            .reorderItem(key = reorderKey, state = reorderState)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = (14 * scale).dp,
                vertical = (12 * scale).dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp * scale)
        ) {
            //版本标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp * scale),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = instance.instance.getVersionName(),
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = MaterialTheme.typography.titleMedium.fontSize * scale,
                        color = onCardColor(),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = buildString {
                            if (instance.minecraftVersion.isNotBlank()) append(instance.minecraftVersion)
                            instance.loaderName?.takeIf { it.isNotBlank() }?.let {
                                if (isNotEmpty()) append(" · ")
                                append(it)
                            }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = MaterialTheme.typography.labelSmall.fontSize * scale,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Button(onClick = onLaunch) {
                    Text(text = stringResource(R.string.home_instance_launch))
                }
            }

            if (instance.isEmpty) {
                Text(
                    text = stringResource(R.string.home_instance_empty),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize * scale,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            //该实例的本地世界
            if (instance.worlds.isNotEmpty()) {
                HomeGroup(titleRes = R.string.home_card_worlds_title, scale = scale) {
                    instance.worlds.forEach { world ->
                        HomeEntryChip(
                            title = world.name,
                            subtitle = world.save.levelMCVersion,
                            scale = scale,
                            reorderModifier = Modifier.reorderItem(
                                key = world.orderKey(),
                                state = worldReorder
                            ),
                            onClick = { onPlayWorld(world.instance, world.name) }
                        )
                    }
                }
            }

            //该实例的服务器
            if (instance.servers.isNotEmpty()) {
                HomeGroup(titleRes = R.string.home_card_servers_title, scale = scale) {
                    instance.servers.forEach { server ->
                        HomeEntryChip(
                            title = server.server.name.ifBlank { server.server.originIp },
                            subtitle = server.server.originIp,
                            scale = scale,
                            reorderModifier = Modifier.reorderItem(
                                key = server.orderKey(),
                                state = serverReorder
                            ),
                            onClick = { onJoinServer(server.instance, server.server.originIp) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 模块内的一组内容（世界 / 服务器）
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeGroup(
    titleRes: Int,
    scale: Float,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp * scale)
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.labelLarge,
            fontSize = MaterialTheme.typography.labelLarge.fontSize * scale,
            color = onCardColor()
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp * scale),
            verticalArrangement = Arrangement.spacedBy(8.dp * scale)
        ) {
            content()
        }
    }
}

/**
 * 世界 / 服务器条目
 *
 * 长按可以拖动排序（关键字见 [reorderModifier]）。
 */
@Composable
private fun HomeEntryChip(
    title: String,
    subtitle: String?,
    scale: Float,
    reorderModifier: Modifier,
    onClick: () -> Unit
) {
    FilledTonalButton(
        modifier = Modifier
            .widthIn(min = (120 * scale).dp, max = (240 * scale).dp)
            .then(reorderModifier),
        onClick = onClick
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(1.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontSize = MaterialTheme.typography.labelLarge.fontSize * scale,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = MaterialTheme.typography.labelSmall.fontSize * scale,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
