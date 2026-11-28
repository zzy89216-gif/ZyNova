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

package com.movtery.zalithlauncher.terracotta

import java.math.BigInteger

/**
 * 陶瓦联机房间码的输入归一化与格式校验
 *
 * 算法来源：`docs/reference/pcl-fe/terracotta-flow.md`（PCL-FE 的 `TcFormat`）。
 *
 * 房间码 = 34 进制 16 位，展示格式 `U/XXXX-XXXX-XXXX-XXXX`，
 * 字符表刻意去掉了容易看错的 `I` / `O`：
 *
 * ```
 * 0123456789ABCDEFGHJKLMNPQRSTUVWXYZ
 * ```
 *
 * 生成时数字会被 `value -= value mod 7` 处理，因此「能被 7 整除」是格式校验的关键；
 * 早期 PCL-FE 还用过低位在前的写法，所以校验要两个方向都试。
 *
 * ⚠️ **这里只做「让用户少出错」的活**：真正的解析与握手仍然交给原生
 * [Terracotta.parseRoomCode] / `setGuesting`，本文件不改变任何协议行为，
 * 只负责把用户粘贴进来的乱七八糟的格式整理干净。
 */
object RoomCode {
    /** 34 进制字符表（无 I、O）*/
    private const val ALPHABET = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ"

    private val BASE = BigInteger.valueOf(ALPHABET.length.toLong())

    /** 房间码位数 */
    private const val DIGITS = 16

    /** 展示前缀 */
    private const val PREFIX = "U/"

    /** 输入框占位符 */
    const val PLACEHOLDER = "U/XXXX-XXXX-XXXX-XXXX"

    /**
     * 把用户粘进来的文本整理成标准房间码
     *
     * 处理的脏数据：全角字符、大小写、空格 / 换行、各种连字符、
     * `【】` / `[]` 包裹、`U/` 前缀、把 `I` 看成 `1`、把 `O` 看成 `0`。
     *
     * - 整理后正好 16 位合法字符 → 返回 `U/XXXX-XXXX-XXXX-XXXX`
     * - 否则原样返回清理后的字符（交给上层判定是否是别的房间类型）
     */
    fun normalizeInput(raw: String): String {
        val cleaned = buildString {
            for (char in raw) {
                val half = toHalfWidth(char)
                when {
                    half.isWhitespace() -> Unit
                    half == '-' || half == '－' || half == '—' || half == '–' -> Unit
                    half == '【' || half == '】' || half == '[' || half == ']' -> Unit
                    else -> {
                        val upper = half.uppercaseChar()
                        //字符表里没有 I / O，用户看到的多半是 1 / 0
                        val fixed = when (upper) {
                            'I' -> '1'
                            'O' -> '0'
                            else -> upper
                        }
                        if (fixed in ALPHABET) append(fixed)
                    }
                }
            }
        }

        return if (cleaned.length == DIGITS && looksLikeRoomCode(cleaned)) {
            format(cleaned)
        } else {
            //不是标准 16 位房间码（可能是别的格式），保留清理结果给原生解析判断
            if (cleaned.isNotEmpty()) cleaned else raw.trim()
        }
    }

    /**
     * 是否为「格式正确」的 16 位房间码（不含前缀）
     *
     * 判定依据：34 进制数值能被 7 整除。先按高位在前计算，
     * 不成立再按低位在前计算一次（兼容旧版 PCL-FE 生成的房间码）。
     */
    fun looksLikeRoomCode(digits: String): Boolean {
        if (digits.length != DIGITS) return false
        if (digits.any { it !in ALPHABET }) return false

        val highFirst = digits.fold(BigInteger.ZERO) { acc, char ->
            acc.multiply(BASE).add(BigInteger.valueOf(ALPHABET.indexOf(char).toLong()))
        }
        if (highFirst.mod(SEVEN) == BigInteger.ZERO) return true

        val lowFirst = digits.reversed().fold(BigInteger.ZERO) { acc, char ->
            acc.multiply(BASE).add(BigInteger.valueOf(ALPHABET.indexOf(char).toLong()))
        }
        return lowFirst.mod(SEVEN) == BigInteger.ZERO
    }

    /** 把 16 位数字格式化成 `U/XXXX-XXXX-XXXX-XXXX` */
    fun format(digits: String): String {
        if (digits.length != DIGITS) return digits
        return buildString {
            append(PREFIX)
            digits.forEachIndexed { index, char ->
                if (index > 0 && index % 4 == 0) append('-')
                append(char)
            }
        }
    }

    /** 去掉前缀与连字符，只留 16 位数字；不是标准房间码时返回 null */
    fun digitsOf(code: String): String? {
        val digits = code.filter { it in ALPHABET }
        return digits.takeIf { it.length == DIGITS && looksLikeRoomCode(it) }
    }

    /** 全角 → 半角（数字与字母）*/
    private fun toHalfWidth(char: Char): Char = when (char) {
        in '０'..'９' -> '0' + (char - '０')
        in 'Ａ'..'Ｚ' -> 'A' + (char - 'Ａ')
        in 'ａ'..'ｚ' -> 'a' + (char - 'ａ')
        else -> char
    }

    private val SEVEN = BigInteger.valueOf(7L)
}
