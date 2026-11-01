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
import com.movtery.zalithlauncher.game.renderer.AutoRenderer
import com.movtery.zalithlauncher.game.renderer.Renderers
import com.movtery.zalithlauncher.game.version.installed.GraphicsApi
import com.movtery.zalithlauncher.game.version.installed.SettingState
import com.movtery.zalithlauncher.game.version.installed.VersionConfig
import java.io.File

/**
 * 实例配置工具：**结构化地**读写「这个实例怎么启动」以及「这个实例的游戏设置」。
 *
 * 为什么要单独做，而不是让模型直接 `write_file` 改 JSON：
 * 1. 直接写 JSON 极易把整份 `version.config` 写坏（少一个字段、枚举拼错），
 *    启动器读不出来就退回默认值，用户会以为「AI 乱改设置」；
 * 2. `options.txt` 是逐行的 `键:值`，让模型整份重写等于赌它不漏行；
 * 3. 这里每一处修改都是**先读 → 只改指定项 → 备份 → 写回 → 再读校验**。
 *
 * 参数一律使用「人话键名」（renderer / ram_mb / isolation …），
 * 底层仍然落到启动器现有的 [VersionConfig] 与 `options.txt`。
 */
internal fun instanceConfigTools(): List<AITool> = listOf(
    getInstanceConfigTool(),
    setInstanceConfigTool(),
    setGameOptionTool(),
)

/** 版本配置（启动方式）里允许被 Agent 修改的键 */
private val VERSION_CONFIG_KEYS = listOf(
    "renderer", "driver", "graphics_api", "java_runtime", "jvm_args",
    "ram_mb", "isolation", "skip_integrity_check", "control_layout",
    "custom_game_path", "server_ip", "summary",
)

private fun getInstanceConfigTool() = aiTool(
    name = "get_instance_config",
    description = """
        读取某个实例的**启动配置**与**游戏设置**（人话版，不是原始 JSON）：
        渲染器、驱动、图形 API、Java 运行时、JVM 参数、内存分配、版本隔离、
        完整性检查、控制布局、自定义游戏目录、自动进服地址，
        以及游戏内常用设置（options.txt 里的语言、视距、GUI 缩放、FOV、全屏等）。

        回答「我这个实例是什么设置」「为什么跑成这个样」之前先调用它；
        改设置之后也用它复查是否真的生效。
    """.trimIndent(),
    parameters = AIToolSchema.objectSchema(
        properties = mapOf(
            "instance" to AIToolSchema.string("实例名称；不传则使用当前选中的实例"),
        ),
    ),
) { args, ctx ->
    val version = AIToolSupport.resolveVersion(args.str("instance"), ctx)
    val config = VersionConfig.parseConfig(version.getVersionPath())
    val gameDir = version.getGameDir()

    buildString {
        append("实例：").append(version.getVersionName()).append('\n')
        append("版本目录：").append(version.getVersionPath().absolutePath).append('\n')
        append("游戏目录：").append(gameDir.absolutePath).append('\n')
        append('\n')
        append("【启动配置】\n")
        append("  渲染器：").append(config.renderer.ifBlank { "（跟随全局设置）" })
        append("    可用值：").append(rendererChoices().joinToString("、") { it.first }).append('\n')
        append("  驱动：").append(config.driver.ifBlank { "（跟随全局设置）" }).append('\n')
        append("  图形 API：").append(
            config.graphicsApi?.displayName?.ifBlank { "默认（游戏设定）" } ?: "（跟随全局设置）"
        ).append('\n')
        append("  Java 运行时：").append(config.javaRuntime.ifBlank { "（跟随全局设置）" }).append('\n')
        append("  JVM 参数：").append(config.jvmArgs.ifBlank { "（跟随全局设置）" }).append('\n')
        append("  内存分配：").append(if (config.ramAllocation <= 0) "（跟随全局设置）" else "${config.ramAllocation} MB")
            .append('\n')
        append("  版本隔离：").append(settingStateText(config.isolationType)).append('\n')
        append("  跳过完整性检查：").append(settingStateText(config.skipGameIntegrityCheck)).append('\n')
        append("  控制布局：").append(config.control.ifBlank { "（跟随全局设置）" }).append('\n')
        append("  自定义游戏目录：").append(config.customPath.ifBlank { "（未设置）" }).append('\n')
        append("  自动进服地址：").append(config.serverIp.ifBlank { "（未设置）" }).append('\n')
        if (config.versionSummary.isNotBlank()) {
            append("  备注：").append(config.versionSummary).append('\n')
        }
        append('\n')
        append("【游戏设置 options.txt】\n")
        append(describeOptionsFile(File(gameDir, "options.txt")))
        append('\n')
        append("要改这些值，用 set_instance_config（启动配置）或 set_game_option（游戏设置）。")
    }
}

private fun setInstanceConfigTool() = aiTool(
    name = "set_instance_config",
    description = """
        修改某个实例的**启动配置**（人话键名，只改你指定的那一项，其它项原样保留）。

        可用键：
        - renderer：渲染器标识符（"auto" = 自动；其它值见 get_instance_config 输出）
        - driver：GPU 驱动（具体渲染器支持的驱动名；不确定就别乱填）
        - graphics_api：图形 API，"default" / "opengl" / "vulkan"
        - java_runtime：Java 运行时名称（空字符串 = 跟随全局）
        - jvm_args：JVM 参数，例如 "-XX:+UseSerialGC"
        - ram_mb：给这个实例分配的内存（正整数 MB）
        - isolation：版本隔离，"follow" / "on" / "off"
        - skip_integrity_check：跳过完整性检查，"follow" / "on" / "off"
        - control_layout：控制布局文件名（空字符串 = 跟随全局）
        - custom_game_path：自定义游戏目录绝对路径（空字符串 = 取消）
        - server_ip：启动后自动进入的服务器地址（空字符串 = 取消）
        - summary：实例备注

        改完会自动保存并回读校验。不确定当前值就先 get_instance_config。
    """.trimIndent(),
    parameters = AIToolSchema.objectSchema(
        properties = mapOf(
            "instance" to AIToolSchema.string("实例名称；不传则使用当前选中的实例"),
            "key" to AIToolSchema.string("要修改的配置项", enumValues = VERSION_CONFIG_KEYS),
            "value" to AIToolSchema.string("新的值；数字 / 开关也按字符串传，例如 4096、on、auto"),
        ),
        required = listOf("key", "value"),
    ),
    risk = AIToolRisk.WRITE,
) { args, ctx ->
    val version = AIToolSupport.resolveVersion(args.str("instance"), ctx)
    val key = args.str("key") ?: throw IllegalArgumentException("缺少 key")
    //value 允许是空字符串（表示「取消该项」），所以不能用 str()（它会把空串当 null 丢掉）
    val value = args.strOrEmpty("value") ?: throw IllegalArgumentException("缺少 value")

    val versionPath = version.getVersionPath()
    val config = VersionConfig.parseConfig(versionPath)
    val before = describeKey(config, key)

    when (key) {
        "renderer" -> {
            val trimmed = value.trim()
            if (trimmed.isNotEmpty()) {
                val choices = rendererChoices()
                require(choices.any { it.first == trimmed }) {
                    "渲染器「$trimmed」不存在。可用值：${choices.joinToString("、") { "${it.first}（${it.second}）" }}"
                }
            }
            config.renderer = trimmed
        }

        "driver" -> config.driver = value.trim()

        "graphics_api" -> {
            config.graphicsApi = when (value.trim().lowercase()) {
                "", "default", "none" -> GraphicsApi.DEFAULT
                "opengl", "gl" -> GraphicsApi.OPENGL
                "default_opengl" -> GraphicsApi.DEFAULT_OPENGL
                "vulkan", "vk" -> GraphicsApi.VULKAN
                else -> throw IllegalArgumentException("图形 API 只能是 default / opengl / vulkan")
            }
        }

        "java_runtime" -> config.javaRuntime = value.trim()
        "jvm_args" -> config.jvmArgs = value.trim()

        "ram_mb" -> {
            val mb = value.trim().toIntOrNull()
                ?: throw IllegalArgumentException("ram_mb 需要整数（单位 MB），例如 4096")
            require(mb == -1 || mb in 256..65536) { "ram_mb 超出合理范围（256 ~ 65536，或 -1 表示跟随全局）" }
            config.ramAllocation = mb
        }

        "isolation" -> config.isolationType = parseSettingState(value)
        "skip_integrity_check" -> config.skipGameIntegrityCheck = parseSettingState(value)
        "control_layout" -> config.control = value.trim()
        "custom_game_path" -> config.customPath = value.trim()
        "server_ip" -> config.serverIp = value.trim()
        "summary" -> config.versionSummary = value

        else -> throw IllegalArgumentException(
            "不认识的配置项「$key」。可用：${VERSION_CONFIG_KEYS.joinToString("、")}"
        )
    }

    config.saveWithThrowable()

    //回读校验：确认真的落下去了
    val reloaded = VersionConfig.parseConfig(versionPath)
    val after = describeKey(reloaded, key)
    buildString {
        append("已修改实例「").append(version.getVersionName()).append("」的 ").append(key).append("。\n")
        append("修改前：").append(before).append('\n')
        append("修改后：").append(after)
        if (before == after && key != "summary") {
            append("\n⚠️ 回读结果与修改前相同，修改可能没有生效，请用 get_instance_config 再确认一次。")
        }
    }
}

private fun setGameOptionTool() = aiTool(
    name = "set_game_option",
    description = """
        修改某个实例的**游戏内设置**（就是 options.txt 里的那些键），只动你指定的那一行。

        常见键名（区分大小写）：
        - 语言 lang（例如 "zh_cn" / "en_us"）
        - 视距 renderDistance（2 ~ 32 个区块）
        - GUI 缩放 guiScale（0 = 自动，1 ~ 4）
        - 视角 fov（30 ~ 110；0 表示默认）
        - 帧率上限 maxFps（例如 60、120、260）
        - 全屏 fullscreen（true / false）
        - 分辨率 overrideWidth / overrideHeight
        - 按键冲突自动解决 autoJump（true / false）
        - 粒子效果 particles（"all" / "decreased" / "minimal"）
        - 图形模式 graphics（"fancy" / "fast" / "fabulous"）

        不想改值、只是想删掉某一项（恢复到游戏默认）时，把 value 传空字符串。
        修改前会自动备份为 options.txt.bak，改完会回读校验。
    """.trimIndent(),
    parameters = AIToolSchema.objectSchema(
        properties = mapOf(
            "instance" to AIToolSchema.string("实例名称；不传则使用当前选中的实例"),
            "key" to AIToolSchema.string("options.txt 里的键名，例如 renderDistance"),
            "value" to AIToolSchema.string("新的值；传空字符串表示删除该键（恢复默认）"),
        ),
        required = listOf("key", "value"),
    ),
    risk = AIToolRisk.WRITE,
) { args, ctx ->
    val version = AIToolSupport.resolveVersion(args.str("instance"), ctx)
    val key = args.str("key") ?: throw IllegalArgumentException("缺少 key")
    require(key.matches(Regex("^[A-Za-z0-9_.-]+$"))) {
        "键名只能包含字母、数字、下划线、点和短横线：$key"
    }
    val rawValue = args.strOrEmpty("value")
        ?: throw IllegalArgumentException("缺少 value（要删除请传空字符串）")

    val file = File(version.getGameDir(), "options.txt")
    val existing = if (file.exists()) file.readLines() else emptyList()
    val oldLine = existing.firstOrNull { it.substringBefore(':') == key }

    val result = if (rawValue.isEmpty()) {
        if (oldLine == null) return@aiTool "options.txt 里本来就没有 $key，不需要删除。"
        existing.filterNot { it.substringBefore(':') == key }
    } else {
        val newLine = "$key:$rawValue"
        if (oldLine == null) existing + newLine
        else existing.map { if (it.substringBefore(':') == key) newLine else it }
    }

    //备份原文件（只留一份，避免把目录塞满）
    if (file.exists()) {
        runCatching { file.copyTo(File(file.parentFile, "options.txt.bak"), overwrite = true) }
    }
    file.parentFile?.mkdirs()
    file.writeText(result.joinToString("\n").let { if (it.isEmpty()) it else "$it\n" })

    //回读校验
    val reloaded = file.readLines().firstOrNull { it.substringBefore(':') == key }
    buildString {
        append("已修改实例「").append(version.getVersionName()).append("」的 options.txt：").append(key).append('\n')
        append("修改前：").append(oldLine ?: "（不存在）").append('\n')
        append("修改后：").append(reloaded ?: "（已删除，游戏会使用默认值）").append('\n')
        append("文件：").append(file.absolutePath)
        append("\n注意：游戏运行中修改，退出游戏后可能被游戏自己的设置覆盖；建议在没进游戏时改。")
    }
}

// ─────────────────────────────────────────────────────────────

private fun rendererChoices(): List<Pair<String, String>> {
    runCatching { Renderers.init() }
    val real = runCatching {
        Renderers.getRenderers().map { it.getUniqueIdentifier() to it.getRendererName() }
    }.getOrElse { emptyList() }
    return listOf(AutoRenderer.IDENTIFIER to "自动") + real
}

private fun settingStateText(state: SettingState?): String = when (state) {
    SettingState.ENABLE -> "开启"
    SettingState.DISABLE -> "关闭"
    else -> "跟随全局设置"
}

private fun parseSettingState(value: String): SettingState = when (value.trim().lowercase()) {
    "follow", "follow_global", "global", "默认", "跟随" -> SettingState.FOLLOW_GLOBAL
    "on", "true", "enable", "enabled", "开", "开启" -> SettingState.ENABLE
    "off", "false", "disable", "disabled", "关", "关闭" -> SettingState.DISABLE
    else -> throw IllegalArgumentException("只接受 follow / on / off（也接受 跟随 / 开 / 关）")
}

/** 只读地描述某个键的当前值，用于「修改前 / 修改后」对比 */
private fun describeKey(config: VersionConfig, key: String): String = when (key) {
    "renderer" -> config.renderer.ifBlank { "（空 = 跟随全局）" }
    "driver" -> config.driver.ifBlank { "（空 = 跟随全局）" }
    "graphics_api" -> config.graphicsApi?.name ?: "（未设置）"
    "java_runtime" -> config.javaRuntime.ifBlank { "（空 = 跟随全局）" }
    "jvm_args" -> config.jvmArgs.ifBlank { "（空 = 跟随全局）" }
    "ram_mb" -> config.ramAllocation.toString()
    "isolation" -> settingStateText(config.isolationType)
    "skip_integrity_check" -> settingStateText(config.skipGameIntegrityCheck)
    "control_layout" -> config.control.ifBlank { "（空 = 跟随全局）" }
    "custom_game_path" -> config.customPath.ifBlank { "（空 = 未设置）" }
    "server_ip" -> config.serverIp.ifBlank { "（空 = 未设置）" }
    "summary" -> config.versionSummary.ifBlank { "（空）" }
    else -> "（未知）"
}

/** options.txt 的常用设置摘要 */
private fun describeOptionsFile(file: File): String {
    if (!file.exists()) return "  （还没有 options.txt：这个实例要么没进过游戏，要么游戏目录不是这个）\n"
    val lines = runCatching { file.readLines() }.getOrElse {
        return "  （读取 options.txt 失败：${it.message}）\n"
    }
    val want = listOf(
        "lang", "renderDistance", "guiScale", "fov", "maxFps", "fullscreen",
        "graphics", "particles", "overrideWidth", "overrideHeight", "autoJump",
    )
    val map = lines.mapNotNull { line ->
        val idx = line.indexOf(':')
        if (idx <= 0) null else line.substring(0, idx) to line.substring(idx + 1)
    }.toMap()

    return buildString {
        for (key in want) {
            append("  ").append(key).append(" = ").append(map[key] ?: "（默认）").append('\n')
        }
        append("  （共 ").append(lines.size).append(" 行，文件：").append(file.absolutePath).append("）")
    }
}
