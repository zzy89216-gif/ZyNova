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

package com.movtery.zalithlauncher.ui.screens.main.home_feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.screens.content.elements.backgroundGlass
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/** 每张来源卡片最多展示几条资讯：太多会把主页拉得很长 */
private const val ITEMS_PER_CARD = 3

/**
 * 主页「自定义主页」资讯区块
 *
 * 位置：主页世界区（实例卡片区）下方；开关与管理入口在「设置 → 基础设置 → 主页」。
 *
 * 设计要求（议题 #91）：
 * - **不阻塞主页**：区块自己异步加载，网络慢/断网时只显示占位，实例卡片照常可用；
 * - **静默降级**：请求失败用磁盘缓存（[HomeFeedRepository]），没有缓存就显示一行空态文案；
 * - **不频繁刷新**：仓库层有 30 分钟缓存有效期，同一次启动内内存缓存兜底。
 */
@Composable
fun HomeFeedSection(
    modifier: Modifier = Modifier,
    /** 卡片大小比例（与卡片式主页的「卡片大小」设置共用，1f = 100%） */
    scale: Float = 1f,
) {
    //读这些设置项，保证在设置里改了开关 / 顺序 / 隐藏 / 恢复默认后主页立刻反映
    val enabled = AllSettings.homeFeedEnabled.state
    val cardOrder = AllSettings.homeFeedCardOrder.state
    val hiddenCards = AllSettings.homeFeedHiddenCards.state
    val cardsInitialized = AllSettings.homeFeedCardsInitialized.state
    val sources = remember(enabled, cardOrder, hiddenCards, cardsInitialized) {
        HomeFeedStore.visibleSources()
    }

    if (!enabled || sources.isEmpty()) return

    //null = 还在加载；有值（可能为空列表）= 已经有结果
    var feeds by remember { mutableStateOf<Map<String, List<HomeFeedItem>?>>(emptyMap()) }

    LaunchedEffect(sources) {
        feeds = sources.associate { it.id to null }
        val loaded = coroutineScope {
            sources.map { source ->
                async { source.id to HomeFeedRepository.load(source) }
            }.awaitAll()
        }
        feeds = loaded.toMap()
    }

    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp * scale)
    ) {
        Text(
            text = stringResource(R.string.home_feed_title),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        sources.forEach { source ->
            HomeFeedSourceCard(
                source = source,
                items = feeds[source.id],
                scale = scale,
                onItemClick = { item ->
                    if (item.url.isNotBlank()) {
                        //打开失败（没有浏览器 / 链接非法）不能崩溃，主页继续可用
                        runCatching { uriHandler.openUri(item.url) }
                    }
                }
            )
        }
    }
}

/**
 * 单个来源的资讯卡片
 *
 * 视觉与实例卡片保持一致：单层背景 + 同样的圆角 / 玻璃层 / 内边距，
 * 不额外造一套卡片样式。
 */
@Composable
private fun HomeFeedSourceCard(
    source: HomeFeedSource,
    /** null = 加载中 */
    items: List<HomeFeedItem>?,
    scale: Float,
    onItemClick: (HomeFeedItem) -> Unit,
) {
    val visibleItems = items?.take(ITEMS_PER_CARD).orEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(cardColor())
            //动态玻璃与首页其它卡片走同一层，避免观感割裂
            .backgroundGlass(AllSettings.backgroundBlur.state, cardColor())
            .padding(
                horizontal = (14 * scale).dp,
                vertical = (12 * scale).dp
            ),
        verticalArrangement = Arrangement.spacedBy(10.dp * scale)
    ) {
        //标题行：来源名 + 最新一条的日期
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp * scale)
        ) {
            Icon(
                modifier = Modifier.size((16 * scale).dp),
                painter = painterResource(R.drawable.ic_article_outlined),
                contentDescription = null,
                tint = onCardColor()
            )
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(source.titleRes),
                style = MaterialTheme.typography.titleSmall,
                color = onCardColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val date = visibleItems.firstOrNull()?.dateText
            if (!date.isNullOrBlank()) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }

        when {
            items == null -> {
                //加载中：只占一条细进度条的位置，不闪烁、不阻塞其它内容
                Text(
                    text = stringResource(R.string.generic_loading),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            visibleItems.isEmpty() -> {
                Text(
                    text = stringResource(R.string.home_feed_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                visibleItems.forEach { item ->
                    HomeFeedItemRow(
                        item = item,
                        scale = scale,
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }
    }
}

/** 一条资讯：左图右文，点整行跳来源 */
@Composable
private fun HomeFeedItemRow(
    item: HomeFeedItem,
    scale: Float,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp * scale),
        horizontalArrangement = Arrangement.spacedBy(10.dp * scale),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FeedItemImage(
            imageUrl = item.imageUrl,
            scale = scale
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelLarge,
                color = onCardColor(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (item.summary.isNotBlank()) {
                Text(
                    text = item.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** 资讯缩略图；没有图 / 加载失败时给一个静态占位，不留空洞 */
@Composable
private fun FeedItemImage(
    imageUrl: String?,
    scale: Float,
) {
    val imageModifier = Modifier
        .width((96 * scale).dp)
        .height((60 * scale).dp)
        .clip(RoundedCornerShape((8 * scale).dp))

    if (imageUrl.isNullOrBlank()) {
        Box(
            modifier = imageModifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size((20 * scale).dp),
                painter = painterResource(R.drawable.ic_article_outlined),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        AsyncImage(
            modifier = imageModifier,
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
}
