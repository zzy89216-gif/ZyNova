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

package com.movtery.zalithlauncher.ai.agent.tools

import com.movtery.zalithlauncher.ai.agent.AITool
import com.movtery.zalithlauncher.ai.agent.AIToolRisk
import com.movtery.zalithlauncher.ai.agent.AIToolSchema
import com.movtery.zalithlauncher.setting.AllSettings

/**
 * 启动器设置读写工具。
 *
 * 覆盖 Java/JVM（`jvmArgs`、`ramAllocation`）、渲染器（`renderer`、各 zink 开关）、
 * 游戏行为（`versionIsolation`、`resolutionRatio` …）等 118 项设置。
 *
 * ⚠️ 安全约定：
 * - 值以字符串读写，底层再按各自类型解析；解析失败会明确报错，不会写入脏值
 * - 命中 [SENSITIVE_KEY_HINTS] 的键**拒绝读写**（当前 AllSettings 里没有这类键，
 *   但留一层防御，避免以后加了令牌类设置被 Agent 顺手读走）
 */
internal fun settingTools(): List<AITool> = listOf(

    aiTool(
        name = "list_settings",
        description = """
            列出启动器的全部可配置项（键名 + 当前值）。
            支持用 keyword 过滤键名，例如 keyword="jvm" 只看 Java 相关、
            keyword="renderer" 只看渲染器相关。
            改设置之前**先用它确认键名**，不要凭猜测调用 set_setting。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "keyword" to AIToolSchema.string("按键名过滤（不区分大小写），不传则列出全部"),
            ),
        ),
    ) { args, _ ->
        val keyword = args.str("keyword")
        val units = AllSettings.units()
            .filter { !isSensitive(it.key) }
            .filter { keyword == null || it.key.contains(keyword, ignoreCase = true) }

        if (units.isEmpty()) return@aiTool "没有匹配「$keyword」的设置项。"

        buildString {
            append("共 ").append(units.size).append(" 项")
            if (keyword != null) append("（过滤：").append(keyword).append("）")
            append("：\n\n")
            units.sortedBy { it.key }.forEach { unit ->
                val value = runCatching { unit.valueAsString() }.getOrElse { "<读取失败>" }
                append(unit.key).append(" = ").append(value).append('\n')
            }
        }
    },

    aiTool(
        name = "get_setting",
        description = "读取单个设置项的值。",
        parameters = AIToolSchema.objectSchema(
            properties = mapOf("key" to AIToolSchema.string("设置键名")),
            required = listOf("key"),
        ),
    ) { args, _ ->
        val key = args.str("key")!!
        if (isSensitive(key)) return@aiTool "拒绝读取该设置项（敏感键名）。"
        val unit = AllSettings.findUnit(key)
            ?: return@aiTool "没有名为「$key」的设置项（可用 list_settings 查看全部键名）。"
        "$key = ${runCatching { unit.valueAsString() }.getOrElse { "<读取失败>" }}"
    },

    aiTool(
        name = "set_setting",
        description = """
            修改一个启动器设置项。
            value 用字符串表示：布尔用 true/false，数字直接写，枚举写枚举名（如 GraphicsApi 的值）。
            修改后**务必再 get_setting 一次确认**。
            典型场景：调大内存分配、追加 JVM 参数、切换渲染器。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "key" to AIToolSchema.string("设置键名"),
                "value" to AIToolSchema.string("新的值（字符串形式）"),
            ),
            required = listOf("key", "value"),
        ),
        risk = AIToolRisk.WRITE,
    ) { args, _ ->
        val key = args.str("key")!!
        val value = args.str("value") ?: ""
        if (isSensitive(key)) return@aiTool "拒绝修改该设置项（敏感键名）。"

        val unit = AllSettings.findUnit(key)
            ?: return@aiTool "没有名为「$key」的设置项（可用 list_settings 查看全部键名）。"

        val before = runCatching { unit.valueAsString() }.getOrElse { "?" }
        val ok = runCatching { unit.setFromString(value) }.getOrDefault(false)
        if (!ok) {
            return@aiTool "修改失败：值「$value」无法解析成 $key 所需要的类型（它现在支持的类型请先用 get_setting 看当前值）。"
        }
        val after = runCatching { unit.valueAsString() }.getOrElse { "?" }
        "已修改 $key：$before → $after"
    },
)

/**
 * 敏感键名黑名单
 *
 * 当前 AllSettings 中没有此类设置项，但保留这层防御：
 * 以后若加入令牌 / 账号类设置，Agent 也不会顺手读走。
 */
private val SENSITIVE_KEY_HINTS = listOf(
    "token", "password", "passwd", "secret", "apikey", "api_key",
    "credential", "cookie", "session", "refresh", "clientsecret",
)

private fun isSensitive(key: String): Boolean =
    SENSITIVE_KEY_HINTS.any { key.contains(it, ignoreCase = true) }
