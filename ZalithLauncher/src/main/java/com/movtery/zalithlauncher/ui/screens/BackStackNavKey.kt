/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import com.movtery.zalithlauncher.ui.AndroidStringText
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.reflect.KClass

@Serializable
abstract class BackStackNavKey<E: TitledNavKey>(
    @Transient private val initTitle: AndroidStringText? = null
) : TitledNavKey {
    override var title by mutableStateOf(initTitle)

    /** 当前屏幕正在使用的堆栈 */
    @Contextual
    val backStack: NavBackStack<E> = NavBackStack()
    /** 当前屏幕的Key */
    var currentKey by mutableStateOf<E?>(null)

    /**
     * 资源安装上下文：从「版本设置 → 资源管理」进入时，记录目标游戏实例名称。
     *
     * 为 null 表示从资源中心进入，只浏览与管理，不提供快捷安装。
     * 该状态不参与序列化，仅用于同一次导航会话。
     */
    @Transient
    var installTargetVersion: String? = null
        private set

    /**
     * 设置资源安装上下文
     */
    fun withInstallTarget(versionName: String?) {
        installTargetVersion = versionName
    }

    @Suppress("unused")
    fun navigateOnce(key: E) {
        backStack.navigateOnce(key)
    }

    fun navigateTo(screenKey: E, useClassEquality: Boolean = false) {
        backStack.navigateTo(screenKey, useClassEquality)
    }

    fun removeAndNavigateTo(remove: KClass<*>, screenKey: E, useClassEquality: Boolean = false) {
        backStack.removeAndNavigateTo(remove, screenKey, useClassEquality)
    }

    fun removeAndNavigateTo(removes: List<KClass<*>>, screenKey: E, useClassEquality: Boolean = false) {
        backStack.removeAndNavigateTo(removes, screenKey, useClassEquality)
    }

    fun clearWith(navKey: E) {
        backStack.clearWith(navKey)
    }
}