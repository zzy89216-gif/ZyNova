/*
 * ZyNova Launcher
 * Copyright (C) 2026 zzy89216-gif and contributors
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

package com.movtery.zalithlauncher.ai.model

/**
 * 从 Provider 动态获取到的可用模型
 *
 * ⚠️ 刻意只保留接口真实返回的字段：**不在代码里写任何固定的模型名**。
 */
data class AIModelInfo(
    /** 调用时要传的模型 ID */
    val id: String,
    /** 展示名；部分 Provider 只返回 id，此时与 [id] 相同 */
    val displayName: String = id,
    /** 上下文长度（部分 Provider 会返回，拿不到就是 null）*/
    val contextLength: Int? = null,
    /** 创建时间戳（拿不到就是 null）*/
    val createdAt: Long? = null,
) {
    /** 列表里的排序键：优先按创建时间倒序，没有就按名字 */
    val sortKey: Long
        get() = createdAt ?: 0L
}
