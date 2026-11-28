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

package com.movtery.zalithlauncher.ui.screens.game.multiplayer

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.terracotta.RoomCode
import com.movtery.zalithlauncher.terracotta.Terracotta
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.components.SimpleEditDialog
import com.movtery.zalithlauncher.utils.string.isEmptyOrBlank
import net.burningtnt.terracotta.TerracottaAndroidAPI

/** 等待中：房客操作状态 */
sealed interface GuestWaitingOperation {
    data object None : GuestWaitingOperation
    /** 被点击，开始输入邀请码 */
    data object OnClick : GuestWaitingOperation
}

@Composable
fun GuestWaitingOperation(
    operation: GuestWaitingOperation,
    onChange: (GuestWaitingOperation) -> Unit,
    onPositive: (roomCode: String) -> Unit,
    onShowToast: (AndroidStringText) -> Unit
) {
    when (operation) {
        is GuestWaitingOperation.None -> {}
        is GuestWaitingOperation.OnClick -> {
            InviteCodeInputDialog(
                onPositive = onPositive,
                onDismiss = {
                    onChange(GuestWaitingOperation.None)
                },
                onShowToast = onShowToast
            )
        }
    }
}

/**
 * 房客输入邀请码对话框
 */
@Composable
private fun InviteCodeInputDialog(
    onPositive: (roomCode: String) -> Unit,
    onDismiss: () -> Unit,
    onShowToast: (AndroidStringText) -> Unit
) {
    var code by remember { mutableStateOf("") }

    /** 输入归一化结果：全角/空格/连字符/前缀都会被整理成标准房间码 */
    val normalized = remember(code) { RoomCode.normalizeInput(code) }

    /** 自动整理后的提示文案（stringResource 必须在 composable 作用域里取，不能塞进 remember）*/
    val normalizedHint = stringResource(R.string.terracotta_room_code_normalized, normalized)

    /**
     * 原生解析（协议的权威判定）
     *
     * 先用用户原始输入，失败再用归一化后的结果 —— 这样「u/abcd efgh-...」这类
     * 粘贴过来的脏数据也能直接加入，而不是逼用户自己删空格。
     */
    val roomType = remember(code) {
        Terracotta.parseRoomCode(code) ?: Terracotta.parseRoomCode(normalized)
    }

    /** 验证不通过时 */
    val isError = code.isNotEmpty() && roomType == null

    val supportingText: AndroidStringText? = remember(code, roomType, normalized, normalizedHint) {
        if (code.isEmpty()) {
            return@remember null
        }

        if (roomType == null) {
            return@remember androidText(R.string.terracotta_status_waiting_guest_prompt_invalid)
        }

        val typeText: AndroidStringText? = when (roomType) {
            TerracottaAndroidAPI.RoomType.TERRACOTTA_LEGACY -> androidText(R.string.terracotta_status_waiting_guest_prompt_terracotta_legacy)
            TerracottaAndroidAPI.RoomType.PCL2CE -> androidText(R.string.terracotta_status_waiting_guest_prompt_pcl2ce)
            TerracottaAndroidAPI.RoomType.SCAFFOLDING -> androidText(R.string.terracotta_status_waiting_guest_prompt_scaffolding)
            else -> null
        }

        //输入被自动整理过时，把整理结果告诉用户，避免「我明明粘的是对的」这种疑惑
        if (normalized != code.trim() && RoomCode.digitsOf(normalized) != null) {
            if (typeText != null) {
                androidText(typeText, androidText("\n"), androidText(normalizedHint))
            } else {
                androidText(normalizedHint)
            }
        } else {
            typeText
        }
    }

    SimpleEditDialog(
        title = stringResource(R.string.terracotta_status_waiting_guest_prompt_title),
        value = code,
        onValueChange = { value ->
            code = value
        },
        label = (@Composable { Text(text = RoomCode.PLACEHOLDER) }).takeIf { code.isEmpty() },
        supportingText = supportingText?.let { text ->
            {
                AndroidStringText(text = text)
            }
        },
        singleLine = true,
        isError = isError,
        onConfirm = {
            val candidate = normalized.ifBlank { code.trim() }
            if (code.isEmptyOrBlank() || roomType == null) {
                onShowToast(androidText(R.string.terracotta_status_waiting_guest_prompt_invalid))
            } else {
                onPositive(candidate)
                onDismiss()
            }
        },
        onDismissRequest = onDismiss
    )
}