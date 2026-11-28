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

import com.movtery.zalithlauncher.setting.AllSettings

/**
 * 「自定义主页」卡片配置的持久化读写
 *
 * 只存**卡片 id 的列表**，不存下标：
 * - [AllSettings.homeFeedCardOrder]：已经添加到主页的卡片顺序；空列表 = 全部预设（默认状态）
 * - [AllSettings.homeFeedHiddenCards]：已添加但暂时隐藏的卡片
 *
 * 「添加」= 追加到顺序列表末尾；「删除」= 从顺序列表移除；「隐藏」= 进隐藏集合，
 * 之后还能在管理页重新打开。这样以后新增预设卡片时不会打乱老用户的排序。
 *
 * 读取方：主页资讯区块（`HomeFeedSection`）与设置里的管理弹窗
 * （`HomeFeedManageDialog`）。
 */
object HomeFeedStore {
    /**
     * 当前用户添加的所有卡片（含隐藏），按用户排序
     *
     * 用户还没配置过时（[AllSettings.homeFeedCardsInitialized] 为 false）返回全部预设，
     * 这样后续版本新增的预设会自动出现在主页；一旦用户改动过排序 / 增删，
     * 就严格按保存的顺序列表展示（空列表 = 用户把卡片删光了，不再自动复活）。
     */
    fun orderedSources(): List<HomeFeedSource> {
        if (!AllSettings.homeFeedCardsInitialized.state) return HomeFeedSource.entries
        val order = AllSettings.homeFeedCardOrder.state
        val index = order.withIndex().associate { (i, id) -> id to i }
        return HomeFeedSource.entries
            .filter { it.id in index }
            .sortedBy { index[it.id] ?: Int.MAX_VALUE }
    }

    /** 已从主页移除（不在顺序列表里）的预设卡片 */
    fun removedSources(): List<HomeFeedSource> {
        val added = orderedSources().toSet()
        return HomeFeedSource.entries.filterNot { it in added }
    }

    /** 当前会在主页上展示的卡片（添加且未隐藏，且区块开关开着） */
    fun visibleSources(): List<HomeFeedSource> {
        if (!AllSettings.homeFeedEnabled.state) return emptyList()
        val hidden = AllSettings.homeFeedHiddenCards.state.toSet()
        return orderedSources().filterNot { it.id in hidden }
    }

    fun isHidden(source: HomeFeedSource): Boolean =
        source.id in AllSettings.homeFeedHiddenCards.state

    /** 隐藏 / 显示一张卡片（只影响这一张，不改排序） */
    fun setHidden(source: HomeFeedSource, hidden: Boolean) {
        val current = AllSettings.homeFeedHiddenCards.state
        val updated = if (hidden) {
            //保持其它卡片原有的隐藏顺序，避免每次开关都把集合重排
            current + source.id
        } else {
            current - source.id
        }
        AllSettings.homeFeedHiddenCards.save(updated.distinct())
    }

    /** 把一张被删除的预设卡片加回主页（追加在最后，并取消隐藏） */
    fun addSource(source: HomeFeedSource) {
        val current = orderedSources().map { it.id }
        if (source.id in current) return
        AllSettings.homeFeedCardOrder.save(current + source.id)
        AllSettings.homeFeedCardsInitialized.save(true)
        setHidden(source, false)
    }

    /** 从主页删除一张卡片（可以从管理页再加回） */
    fun removeSource(source: HomeFeedSource) {
        val current = orderedSources().map { it.id }
        if (source.id !in current) return
        AllSettings.homeFeedCardOrder.save(current - source.id)
        AllSettings.homeFeedCardsInitialized.save(true)
        setHidden(source, false)
    }

    /** 直接保存一整份顺序（管理页拖动结束后调用一次） */
    fun saveOrder(sources: List<HomeFeedSource>) {
        AllSettings.homeFeedCardOrder.save(sources.map { it.id })
        AllSettings.homeFeedCardsInitialized.save(true)
    }

    /**
     * 拖动排序：把 [from] 移到 [to]（按下标），并落盘
     *
     * ⚠️ 只在拖动结束时调用一次（与卡片式主页的拖动排序约定一致），
     * 拖动过程中改内存状态即可，否则每帧写一次 MMKV 会闪屏。
     */
    fun move(from: Int, to: Int) {
        val items = orderedSources().toMutableList()
        if (from !in items.indices || to !in items.indices || from == to) return
        items.add(to, items.removeAt(from))
        saveOrder(items)
    }

    /** 恢复默认：全部预设按默认顺序展示，且全部可见 */
    fun reset() {
        AllSettings.homeFeedCardOrder.save(emptyList())
        AllSettings.homeFeedHiddenCards.save(emptyList())
        AllSettings.homeFeedCardsInitialized.save(false)
    }
}
