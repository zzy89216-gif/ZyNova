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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.GlassLevel
import com.movtery.zalithlauncher.game.home.HomeDataProvider
import com.movtery.zalithlauncher.game.home.HomeServer
import com.movtery.zalithlauncher.game.home.HomeWorld
import com.movtery.zalithlauncher.game.version.installed.Version

import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor

/**
 * 卡片式主页
 *
 * 自动识别并展示：
 * - 最近使用的 Minecraft 版本 → 点击启动
 * - 本地世界 → 点击进入
 * - 已保存的服务器 → 点击加入
 *
 * 数据按需加载：只有真正进入主页时才读取版本、世界与服务器，
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
    var versions by remember { mutableStateOf<List<Version>>(emptyList()) }
    var worlds by remember { mutableStateOf<List<HomeWorld>>(emptyList()) }
    var servers by remember { mutableStateOf<List<HomeServer>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        versions = HomeDataProvider.recentVersions()
        worlds = HomeDataProvider.recentWorlds()
        servers = HomeDataProvider.savedServers()
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
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        HomeSection(
            titleRes = R.string.home_card_versions_title,
            empty = versions.isEmpty()
        ) {
            versions.forEach { version ->
                HomeCard(
                    title = version.getVersionName(),
                    subtitle = version.getVersionInfo()?.minecraftVersion,
                    onClick = { onLaunchVersion(version) }
                )
            }
        }

        HomeSection(
            titleRes = R.string.home_card_worlds_title,
            empty = worlds.isEmpty()
        ) {
            worlds.forEach { world ->
                HomeCard(
                    title = world.name,
                    subtitle = world.save.levelMCVersion,
                    onClick = { onPlayWorld(world.instance, world.name) }
                )
            }
        }

        HomeSection(
            titleRes = R.string.home_card_servers_title,
            empty = servers.isEmpty()
        ) {
            servers.forEach { server ->
                HomeCard(
                    title = server.server.name.ifBlank { server.server.originIp },
                    subtitle = server.server.originIp,
                    onClick = { onJoinServer(server.instance, server.server.originIp) }
                )
            }
        }
    }
}

/**
 * 主页分组：标题 + 卡片区域
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeSection(
    titleRes: Int,
    empty: Boolean,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleMedium
        )

        if (empty) {
            Text(
                text = stringResource(R.string.home_card_empty),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                content()
            }
        }
    }
}

/**
 * 单张主页卡片
 *
 * 在「极致」玻璃档位下额外启用：
 * - Magnetic / Snap Interaction：按下时卡片产生吸附式回弹
 * - Dynamic Shadow：阴影随交互状态实时变化
 */
@Composable
private fun HomeCard(
    title: String,
    subtitle: String?,
    onClick: () -> Unit
) {
    val extremeGlass = AllSettings.glassLevel.state == GlassLevel.Extreme

    //【Magnetic / Snap Interaction】按下时的吸附回弹
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val snapScale by animateFloatAsState(
        targetValue = if (extremeGlass && pressed) 0.965f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "homeCardSnapScale"
    )

    //【Dynamic Shadow】阴影随交互状态变化
    val shadowElevation by animateDpAsState(
        targetValue = when {
            extremeGlass && pressed -> 14.dp
            extremeGlass -> 6.dp
            else -> 1.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "homeCardShadow"
    )

    Surface(
        modifier = Modifier
            .widthIn(min = 150.dp, max = 260.dp)
            .graphicsLayer {
                scaleX = snapScale
                scaleY = snapScale
            },
        shape = MaterialTheme.shapes.large,
        color = cardColor(false),
        contentColor = onCardColor(),
        shadowElevation = shadowElevation,
        interactionSource = interactionSource,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
