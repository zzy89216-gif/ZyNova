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

package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.home.HomeDataProvider
import com.movtery.zalithlauncher.game.home.HomeWorld
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.HomeMaterialButton
import com.movtery.zalithlauncher.ui.components.SimpleTextInputField
import com.movtery.zalithlauncher.ui.components.rememberHomeButtonMaterial
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.SortByDropdownMenu
import com.movtery.zalithlauncher.ui.screens.content.elements.SortByEnum
import com.movtery.zalithlauncher.ui.screens.content.elements.backgroundGlass
import com.movtery.zalithlauncher.ui.theme.cardColor
import com.movtery.zalithlauncher.ui.theme.itemColor
import com.movtery.zalithlauncher.ui.theme.onCardColor
import com.movtery.zalithlauncher.ui.theme.onItemColor
import com.movtery.zalithlauncher.utils.formatDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 「更多世界」独立列表页（议题 #91）
 *
 * 从主页「最近游玩的 4 个世界」区块的第 5 张卡片进入，
 * 展示**全部实例下的全部本地世界**，并支持：
 * - 搜索（世界名 / 存档文件夹名 / 实例名）
 * - 排序（最后游玩 / 名称，可切换升序降序）
 * - 筛选（按游戏版本 / 按实例）
 *
 * 卡片样式复用卡片式主页的世界条目（同样的卡片背景、玻璃层与文字层级），
 * 不另造一套视觉。
 */
@Composable
fun WorldListScreen(
    screenKey: TitledNavKey,
    currentKey: TitledNavKey?,
    onPlayWorld: (Version, String) -> Unit,
) {
    BaseScreen(screenKey = screenKey, currentKey = currentKey) {
        WorldListContent(onPlayWorld = onPlayWorld)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun WorldListContent(
    onPlayWorld: (Version, String) -> Unit,
) {
    var worlds by remember { mutableStateOf<List<HomeWorld>?>(null) }

    //按需加载：只有真正进入这个页面才扫描实例与存档
    LaunchedEffect(Unit) {
        worlds = withContext(Dispatchers.IO) {
            HomeDataProvider.allWorlds()
        }
    }

    var search by remember { mutableStateOf("") }
    var sortByEnum by remember { mutableStateOf(SortByEnum.LastPlayed) }
    var isAscending by remember { mutableStateOf(false) }
    var instanceFilter by remember { mutableStateOf<String?>(null) }
    var versionFilter by remember { mutableStateOf<String?>(null) }

    val allWorlds = worlds

    val instanceNames = remember(allWorlds) {
        allWorlds.orEmpty()
            .map { it.instance.getVersionName() }
            .distinct()
            .sorted()
    }
    val versionNames = remember(allWorlds) {
        allWorlds.orEmpty()
            .mapNotNull { it.save.levelMCVersion?.takeIf(String::isNotBlank) }
            .distinct()
            .sorted()
    }

    val filteredWorlds = remember(
        allWorlds, search, sortByEnum, isAscending, instanceFilter, versionFilter
    ) {
        allWorlds.orEmpty()
            .filter { world ->
                val keyword = search.trim()
                val matchesSearch = keyword.isEmpty() ||
                        world.name.contains(keyword, ignoreCase = true) ||
                        world.save.saveFile.name.contains(keyword, ignoreCase = true) ||
                        world.instance.getVersionName().contains(keyword, ignoreCase = true)
                val matchesInstance = instanceFilter == null ||
                        world.instance.getVersionName() == instanceFilter
                val matchesVersion = versionFilter == null ||
                        world.save.levelMCVersion == versionFilter
                matchesSearch && matchesInstance && matchesVersion
            }
            .sortedWith { left, right ->
                //基准比较器统一按「升序」写，最后再按用户选择的顺序取反，避免两种排序方向互相打结
                val value = when (sortByEnum) {
                    SortByEnum.Name -> left.name.compareTo(right.name, ignoreCase = true)
                    else -> left.lastPlayedMillis.compareTo(right.lastPlayedMillis)
                }
                if (isAscending) value else -value
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        //搜索 + 排序
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                var expanded by remember { mutableStateOf(false) }
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_sort),
                        contentDescription = stringResource(R.string.sort_by)
                    )
                }
                SortByDropdownMenu(
                    expanded = expanded,
                    onClose = { expanded = false },
                    enums = listOf(SortByEnum.LastPlayed, SortByEnum.Name),
                    currentEnum = sortByEnum,
                    onEnumChanged = { sortByEnum = it },
                    isAscending = isAscending,
                    onToggleSortOrder = { isAscending = !isAscending }
                )
            }

            SimpleTextInputField(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
                value = search,
                onValueChange = { search = it },
                hint = {
                    Text(
                        text = stringResource(R.string.generic_search),
                        style = TextStyle(color = onItemColor()).copy(fontSize = 12.sp)
                    )
                },
                color = itemColor(),
                contentColor = onItemColor(),
                singleLine = true
            )
        }

        //筛选：实例 / 游戏版本
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            //两个筛选项均分宽度：实例名可能很长，靠 weight + 单行省略避免撑破一行
            Box(modifier = Modifier.weight(1f)) {
                FilterDropdown(
                    label = instanceFilter
                        ?: stringResource(R.string.world_list_all_instances),
                    options = instanceNames,
                    allLabel = stringResource(R.string.world_list_all_instances),
                    onSelected = { instanceFilter = it }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                FilterDropdown(
                    label = versionFilter
                        ?: stringResource(R.string.world_list_all_versions),
                    options = versionNames,
                    allLabel = stringResource(R.string.world_list_all_versions),
                    onSelected = { versionFilter = it }
                )
            }
        }

        when {
            allWorlds == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
            filteredWorlds.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.world_list_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredWorlds,
                        //存档目录名在实例内唯一，实例名 + 目录名全局唯一，作为列表 key
                        key = { world ->
                            world.instance.getVersionName() + "#" + world.save.saveFile.name
                        }
                    ) { world ->
                        WorldListRow(
                            world = world,
                            onClick = {
                                onPlayWorld(world.instance, world.save.saveFile.name)
                            }
                        )
                    }
                }
            }
        }
    }
}

/** 单个世界卡片：样式与主页世界条目一致，整行可点直接进入游戏 */
@Composable
private fun WorldListRow(
    world: HomeWorld,
    onClick: () -> Unit,
) {
    val material = rememberHomeButtonMaterial()
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(cardColor())
            .backgroundGlass(
                blur = com.movtery.zalithlauncher.setting.AllSettings.backgroundBlur.state,
                color = cardColor()
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = world.name,
                style = MaterialTheme.typography.titleSmall,
                color = onCardColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = buildString {
                    append(world.instance.getVersionName())
                    world.save.levelMCVersion?.takeIf { it.isNotBlank() }?.let {
                        append(" · ").append(it)
                    }
                    val lastPlayed = world.lastPlayedMillis
                    append(" · ")
                    append(
                        if (lastPlayed > 0L) {
                            //本地时区的「最后游玩」，和存档管理页口径一致
                            formatDate(lastPlayed, "yyyy-MM-dd HH:mm")
                        } else {
                            context.getString(R.string.world_list_never_played)
                        }
                    )
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        HomeMaterialButton(
            onClick = onClick,
            text = stringResource(R.string.home_instance_launch),
            material = material,
            shape = MaterialTheme.shapes.medium,
            minHeight = 32.dp,
            textStyle = MaterialTheme.typography.labelMedium,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

/** 筛选下拉：显示当前选中项，点开列出全部选项（第一项永远是「全部」） */
@Composable
private fun FilterDropdown(
    label: String,
    options: List<String>,
    allLabel: String,
    onSelected: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(itemColor())
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = onItemColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(R.drawable.ic_arrow_drop_down_rounded),
                contentDescription = null,
                tint = onItemColor()
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = MaterialTheme.shapes.large
        ) {
            DropdownMenuItem(
                text = { Text(allLabel) },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
