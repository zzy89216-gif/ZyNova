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

import android.os.Build
import com.movtery.zalithlauncher.ai.agent.AITool
import com.movtery.zalithlauncher.ai.agent.AIToolSchema
import com.movtery.zalithlauncher.ai.agent.AIToolRisk
import com.movtery.zalithlauncher.context.GlobalContext
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.device.Architecture
import com.movtery.zalithlauncher.utils.platform.getTotalMemory
import java.io.File

/**
 * 实例（Minecraft 版本）相关工具。
 *
 * 数据全部来自现有的 [VersionsManager]，不自己扫描目录。
 */
internal fun instanceTools(): List<AITool> = listOf(

    aiTool(
        name = "list_instances",
        description = """
            列出当前启动器里全部 Minecraft 实例（版本）。
            每个实例会给出：名称、类型（原版 / Fabric / Forge 等）、是否有效、
            以及 mods / resourcepacks / shaderpacks / saves 的文件数量。
            当用户说「这个实例」「那个版本」而你没把握指哪个时，**先调用它**。
        """.trimIndent(),
        parameters = AIToolSchema.noArgs(),
    ) { _, _ ->
        val versions = VersionsManager.versions.value
        if (versions.isEmpty()) return@aiTool "当前没有任何 Minecraft 实例。"

        val current = VersionsManager.currentVersion.value?.getVersionName()
        buildString {
            append("共 ").append(versions.size).append(" 个实例：\n\n")
            versions.forEach { v ->
                val gameDir = v.getGameDir()
                append(if (v.getVersionName() == current) "★ " else "  ")
                append(v.getVersionName())
                append("（").append(v.versionType.name).append("）")
                append(" 有效=").append(v.isValid())
                append(" 模组=").append(AIToolSupport.folderFileCount(VersionFolders.MOD.getDir(gameDir)))
                append(" 资源包=").append(AIToolSupport.folderFileCount(VersionFolders.RESOURCE_PACK.getDir(gameDir)))
                append(" 光影=").append(AIToolSupport.folderFileCount(VersionFolders.SHADERS.getDir(gameDir)))
                append(" 存档=").append(AIToolSupport.folderFileCount(VersionFolders.SAVES.getDir(gameDir)))
                append('\n')
                append("     ").append(gameDir.absolutePath).append('\n')
            }
            append("\n★ = 启动器当前选中的实例")
        }
    },

    aiTool(
        name = "get_instance_detail",
        description = """
            查看某个实例的详细信息：游戏目录、版本 JSON 是否存在、核心 jar、
            各资源目录的绝对路径与文件清单概览。
            排查「进不去」「崩了」之前，先用它确认实例本身是否完整。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "instance" to AIToolSchema.string("实例名称；不传则使用当前选中的实例"),
            ),
        ),
    ) { args, ctx ->
        val version = AIToolSupport.resolveVersion(args.str("instance"), ctx)
        val gameDir = version.getGameDir()
        val versionPath = version.getVersionPath()

        buildString {
            append(AIToolSupport.describeVersion(version)).append("\n\n")
            append("版本目录：").append(versionPath.absolutePath)
            append("  存在=").append(versionPath.exists()).append('\n')
            append("游戏目录：").append(gameDir.absolutePath)
            append("  存在=").append(gameDir.exists()).append('\n')
            append("版本 JSON：").append(File(versionPath, "${version.getVersionName()}.json").exists()).append('\n')
            VersionFolders.entries.filter { it != VersionFolders.NONE }.forEach { folder ->
                val dir = folder.getDir(gameDir)
                append("  ").append(folder.folderName)
                    .append("：存在=").append(dir.exists())
                    .append(" 文件数=").append(AIToolSupport.folderFileCount(dir))
                    .append('\n')
            }
        }
    },

    aiTool(
        name = "get_device_info",
        description = """
            读取运行环境信息：设备型号、Android 版本、CPU 架构、可用/总内存、
            启动器数据目录与游戏根目录。
            判断「内存是否够」「架构对不对」以及写崩溃分析结论时使用。
        """.trimIndent(),
        parameters = AIToolSchema.noArgs(),
    ) { _, _ ->
        val ctx = runCatching { GlobalContext }.getOrNull()
        buildString {
            append("设备：").append(Build.MANUFACTURER).append(' ').append(Build.MODEL).append('\n')
            append("Android：").append(Build.VERSION.RELEASE)
            append("（SDK ").append(Build.VERSION.SDK_INT).append("）\n")
            append("CPU 架构：").append(Build.SUPPORTED_ABIS.joinToString(", ")).append('\n')
            append("是否 32 位设备：").append(Architecture.is32BitsDevice).append('\n')
            if (ctx != null) {
                runCatching {
                    append("物理内存：").append(AIToolSupport.humanSize(getTotalMemory(ctx))).append('\n')
                }
            }
            append("JVM 上限：").append(AIToolSupport.humanSize(Runtime.getRuntime().maxMemory())).append('\n')
            append("启动器数据目录：").append(PathManager.DIR_FILES_PRIVATE.absolutePath).append('\n')
            append("外部数据目录：").append(PathManager.DIR_FILES_EXTERNAL.absolutePath).append('\n')
            append("游戏根目录：").append(PathManager.DIR_GAME.absolutePath).append('\n')
            append("日志目录：").append(PathManager.DIR_LAUNCHER_LOGS.absolutePath).append('\n')
        }
    },
)
