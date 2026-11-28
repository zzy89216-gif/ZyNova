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

package com.movtery.zalithlauncher.setting.unit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

abstract class AbstractSettingUnit<V>(
    val key: String,
    val defaultValue: V
) {
    /**
     * @return 获取当前的设置值
     */
    abstract fun getValue(): V

    /**
     * 保存设置值
     */
    protected abstract fun saveValue(v: V): V

    /**
     * 可观察的状态
     */
    var state by mutableStateOf(defaultValue)
        protected set

    fun init() {
        getValue()
    }

    /**
     * 保存当前状态值
     */
    fun save() {
        saveValue(this.state)
    }

    /**
     * 存入值
     */
    fun save(value: V) {
        this.state = saveValue(value)
    }

    /**
     * **仅更新状态**，不保存值
     */
    open fun updateState(value: V) {
        this.state = value
    }

    /**
     * 重置当前设置单元为默认值
     */
    fun reset() {
        this.state = saveValue(defaultValue)
    }

    /**
     * 以字符串形式读取当前值。
     *
     * 供 AI Agent 与诊断工具按 key 读取设置用；
     * 不影响任何现有调用路径。
     */
    open fun valueAsString(): String = state.toString()

    /**
     * 用字符串写入设置值。
     *
     * 供 AI Agent 按 key 修改设置用。默认返回 false（表示该类型不支持字符串写入），
     * 各具体类型自行重写。
     *
     * @return 是否写入成功（解析失败或类型不支持时为 false）
     */
    open fun setFromString(value: String): Boolean = false
}