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

package com.movtery.zalithlauncher.game.download.assets.platform

import com.movtery.zalithlauncher.ui.screens.content.download.assets.elements.AssetsPage

/**
 * 聚合搜索结果：把多个来源的搜索结果合并成一个结果
 *
 * 用于「目标平台 = 所有」的场景：CurseForge 与 Modrinth 的资源
 * 会出现在同一个列表里，统一按总下载量排序。
 */
class AggregatedSearchResult(
    private val results: List<PlatformSearchResult>
) : PlatformSearchResult {

    override fun getAssetsPage(classes: PlatformClasses): AssetsPage {
        val pages = results.map { it.getAssetsPage(classes) }

        //合并各来源的条目，统一按总下载量降序，保证列表只有一个稳定顺序
        val mergedData = pages
            .flatMap { it.data }
            .sortedByDescending { (item, _) -> item.platformDownloadCount() }

        return AssetsPage(
            pageNumber = pages.minOfOrNull { it.pageNumber } ?: 1,
            pageIndex = pages.minOfOrNull { it.pageIndex } ?: 0,
            //取各来源中较小的总页数，避免翻到某一来源已经结束的页码
            totalPage = pages.minOfOrNull { it.totalPage } ?: 1,
            isLastPage = pages.all { it.isLastPage },
            data = mergedData
        )
    }

    override fun processChineseSearchResults(
        searchFilter: String,
        classes: PlatformClasses
    ): PlatformSearchResult = AggregatedSearchResult(
        results.map { it.processChineseSearchResults(searchFilter, classes) }
    )
}
