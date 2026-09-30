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

package com.movtery.zalithlauncher.ai

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText

/**
 * Agent 工具调用权限模式
 */
enum class AIPermissionMode {
    /**
     * 完全控制：Agent 可以直接调用工具并执行，不需要逐个确认。
     */
    FULL_CONTROL,

    /**
     * 操作确认：每次写操作（安装 / 删除 / 改配置 / 启动）前都需要用户确认。
     *
     * 当前版本 UI 尚未接入逐项确认弹窗，[com.movtery.zalithlauncher.ai.agent.AIAgent]
     * 会通过 [com.movtery.zalithlauncher.ai.agent.AIToolConfirmHandler] 挂起等待；
     * 若上层未提供确认器，则安全地**拒绝**写操作（只读工具不受影响）。
     */
    CONFIRM;

    val displayName: AndroidStringText
        get() = androidText(
            when (this) {
                FULL_CONTROL -> R.string.ai_permission_full_control
                CONFIRM -> R.string.ai_permission_confirm
            }
        )
}
