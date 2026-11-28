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

import android.content.Context
import androidx.annotation.StringRes
import com.movtery.zalithlauncher.context.GlobalContext

/**
 * 在**没有 Context** 的地方（Provider、Agent 核心、工具层）取本地化字符串。
 *
 * 这些层的错误信息既要回灌给模型，也会显示给用户，
 * 所以不能直接写死中文——否则 4 语言版本里只有中文用户看得懂。
 *
 * 取不到 Context（理论上只在 Application 初始化完成前发生）时返回空串，
 * 由调用方决定怎么兜底，绝不因此抛异常。
 */
internal fun aiString(@StringRes id: Int, vararg args: Any): String {
    val context: Context? = runCatching { GlobalContext }.getOrNull()
    return context?.getString(id, *args).orEmpty()
}
