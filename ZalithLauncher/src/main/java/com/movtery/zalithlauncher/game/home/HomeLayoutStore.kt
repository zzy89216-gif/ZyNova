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

package com.movtery.zalithlauncher.game.home

import com.movtery.zalithlauncher.setting.launcherMMKV

/**
 * 主页自定义排序（拖动排序）的持久化存储
 *
 * 直接把「顺序」存成一行行的标识符：
 * - 卡片式主页的版本卡片顺序：`cards`
 * - 某个实例的世界顺序：`worlds_<实例名>`
 * - 某个实例的服务器顺序：`servers_<实例名>`
 * - 右侧菜单各块顺序：`right_menu`
 *
 * 存的是标识符而不是下标，因此之后新增/删除条目时不会错位；
 * 读取时只负责「按记录的顺序排」，没有记录过的条目依旧按原来的顺序追加在末尾。
 */
object HomeLayoutStore {
    private const val PREFIX = "home_layout_order_"

    /** 版本卡片顺序 */
    const val KEY_CARDS = "cards"

    /** 右侧菜单（启动按钮 / 账号 / 版本设置）顺序 */
    const val KEY_RIGHT_MENU = "right_menu"

    fun keyWorlds(instanceName: String): String = "worlds_$instanceName"

    fun keyServers(instanceName: String): String = "servers_$instanceName"

    /** 读取保存的顺序；没有记录时返回空列表 */
    fun load(key: String): List<String> {
        val raw = launcherMMKV().getString(PREFIX + key, null) ?: return emptyList()
        return raw.split('\n').filter { it.isNotBlank() }
    }

    /** 保存顺序 */
    fun save(key: String, order: List<String>) {
        launcherMMKV().putString(PREFIX + key, order.joinToString("\n")).apply()
    }

    /**
     * 按保存的顺序重排列表
     *
     * 未记录的条目追加在末尾，且保持它们原来的相对顺序。
     */
    fun <T> sort(items: List<T>, order: List<String>, keyOf: (T) -> String): List<T> {
        if (order.isEmpty()) return items
        val index = order.withIndex().associate { (i, k) -> k to i }
        return items.sortedBy { index[keyOf(it)] ?: Int.MAX_VALUE }
    }

    /**
     * 把一项从 [from] 移到 [to]（按下标），返回新列表
     *
     * 直接用 `to` 作为插入位置，拖动时「移到谁的位置就占谁的位置」，符合直觉。
     */
    fun <T> move(items: List<T>, from: Int, to: Int): List<T> {
        if (from == to || from !in items.indices || to !in items.indices) return items
        return items.toMutableList().apply { add(to, removeAt(from)) }
    }
}
