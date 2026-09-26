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

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.home.HomeDataProvider
import com.movtery.zalithlauncher.game.home.HomeInstance
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor

/**
 * 卡片式主页
 *
 * 以「游戏版本」为模块组织内容：每个已安装的实例是一个模块，
 * 模块内部直接展示该实例自己的本地世界与已保存服务器。
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
        instances = HomeDataProvider.instances()
        loaded = true
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
                InstanceModule(
                    instance = instance,
                    scale = cardScale,
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
    onLaunch: () -> Unit,
    onPlayWorld: (Version, String) -> Unit,
    onJoinServer: (Version, String) -> Unit,
) {
    //卡片按下时的轻微吸附回弹（26.2.2 起不再区分玻璃档位，效果保持轻量）
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val snapScale by animateFloatAsState(
        targetValue = if (pressed) 0.99f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "instanceModuleSnapScale"
    )

    //阴影随交互状态变化（同样保持轻量）
    val shadowElevation by animateDpAsState(
        targetValue = if (pressed) 4.dp else 1.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "instanceModuleShadow"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = snapScale
                scaleY = snapScale
            },
        shape = MaterialTheme.shapes.large,
        //⚠️ 必须使用受背景影响的卡片颜色（默认 true）：
        //之前写死 `cardColor(false)`，导致设置了自定义背景后，
        //卡片不透明度完全不跟随「背景元素不透明度」设置（issue #4）
        color = cardColor(),
        contentColor = onCardColor(),
        shadowElevation = shadowElevation,
        interactionSource = interactionSource,
        onClick = onLaunch
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
            fontSize = MaterialTheme.typography.labelLarge.fontSize * scale
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
 */
@Composable
private fun HomeEntryChip(
    title: String,
    subtitle: String?,
    scale: Float,
    onClick: () -> Unit
) {
    FilledTonalButton(
        modifier = Modifier.widthIn(min = (120 * scale).dp, max = (240 * scale).dp),
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
