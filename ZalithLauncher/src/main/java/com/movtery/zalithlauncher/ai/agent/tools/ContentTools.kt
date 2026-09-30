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
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.game.version.mod.AllModReader
import java.io.File

/**
 * 实例内容（模组 / 资源包 / 光影 / 存档）相关工具。
 *
 * 全部复用现有能力：
 * - 模组元数据解析用 [AllModReader]（它已经会读 fabric.mod.json / mods.toml 等）
 * - 目录位置用 [VersionFolders]，不自己拼路径字符串
 */
internal fun contentTools(): List<AITool> = listOf(

    aiTool(
        name = "list_mods",
        description = """
            列出某个实例已安装的全部模组，并解析出每个模组的真实元数据：
            名称、版本、加载器、作者、是否已禁用、是否不是模组文件。
            依赖冲突、前置缺失、模组崩溃排查的**第一步就是调用它**。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "instance" to AIToolSchema.string("实例名称；不传则使用当前选中的实例"),
            ),
        ),
    ) { args, ctx ->
        val version = AIToolSupport.resolveVersion(args.str("instance"), ctx)
        val modsDir = VersionFolders.MOD.getDir(version.getGameDir())
        if (!modsDir.exists()) return@aiTool "该实例还没有 mods 目录：${modsDir.absolutePath}"

        val mods = AllModReader(modsDir).readAllLocals()
        if (mods.isEmpty()) return@aiTool "mods 目录是空的：${modsDir.absolutePath}"

        buildString {
            append("实例「").append(version.getVersionName()).append("」共 ")
                .append(mods.size).append(" 个模组文件：\n\n")
            mods.sortedBy { it.file.name }.forEach { mod ->
                append("· ").append(mod.file.name).append('\n')
                append("    名称=").append(mod.name.ifBlank { "（无法解析）" })
                append(" | 版本=").append(mod.version ?: "?")
                append(" | 加载器=").append(mod.loader.name)
                append(" | 已禁用=").append(mod.file.name.endsWith(".disabled"))
                if (mod.notMod) append(" | ⚠️不是模组文件")
                if (mod.authors.isNotEmpty()) append(" | 作者=").append(mod.authors.joinToString(","))
                append('\n')
            }
        }
    },

    aiTool(
        name = "set_mod_enabled",
        description = """
            启用或禁用一个模组（通过把文件重命名为 .disabled 实现，可随时恢复）。
            这是**排查模组冲突最安全的手段**：先禁用一半模组看是否还崩，
            而不是直接删除。用户说「帮我找出是哪个模组导致的」时优先用它。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "instance" to AIToolSchema.string("实例名称；不传则使用当前选中的实例"),
                "mod" to AIToolSchema.string("模组文件名（含或不含 .jar / .jar.disabled 都可以）"),
                "enabled" to AIToolSchema.bool("true = 启用，false = 禁用"),
            ),
            required = listOf("mod", "enabled"),
        ),
        risk = AIToolRisk.WRITE,
    ) { args, ctx ->
        val version = AIToolSupport.resolveVersion(args.str("instance"), ctx)
        val enabled = args.bool("enabled") ?: throw IllegalArgumentException("缺少参数 enabled")
        val modsDir = VersionFolders.MOD.getDir(version.getGameDir())
        val target = findModFile(modsDir, args.str("mod")!!)
            ?: throw IllegalStateException("在 mods 目录里找不到模组：${args.str("mod")}")

        val nowDisabled = target.name.endsWith(DISABLED_SUFFIX)
        if (enabled == !nowDisabled) {
            return@aiTool "模组 ${target.name} 已经是${if (enabled) "启用" else "禁用"}状态，无需改动。"
        }

        val newName = if (enabled) target.name.removeSuffix(DISABLED_SUFFIX) else target.name + DISABLED_SUFFIX
        val newFile = File(modsDir, newName)
        if (!target.renameTo(newFile)) {
            throw IllegalStateException("重命名失败：${target.name} → $newName")
        }
        "已${if (enabled) "启用" else "禁用"}模组：$newName"
    },

    aiTool(
        name = "delete_mod",
        description = """
            删除一个模组文件（**不可恢复**，请先考虑用 set_mod_enabled 禁用）。
            只在用户明确要求「删掉这个模组」时使用。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "instance" to AIToolSchema.string("实例名称；不传则使用当前选中的实例"),
                "mod" to AIToolSchema.string("模组文件名"),
            ),
            required = listOf("mod"),
        ),
        risk = AIToolRisk.WRITE,
    ) { args, ctx ->
        val version = AIToolSupport.resolveVersion(args.str("instance"), ctx)
        val modsDir = VersionFolders.MOD.getDir(version.getGameDir())
        val target = findModFile(modsDir, args.str("mod")!!)
            ?: throw IllegalStateException("在 mods 目录里找不到模组：${args.str("mod")}")
        if (!target.delete()) throw IllegalStateException("删除失败：${target.name}")
        "已删除模组：${target.name}"
    },

    aiTool(
        name = "list_folder",
        description = """
            列出某个实例下指定资源目录的文件清单。
            folder 取值：mods / resourcepacks / shaderpacks / saves / screenshots。
            想确认「资源包装上了没」「光影放对地方没」时用它。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "instance" to AIToolSchema.string("实例名称；不传则使用当前选中的实例"),
                "folder" to AIToolSchema.string(
                    "要列出的子目录",
                    enumValues = listOf("mods", "resourcepacks", "shaderpacks", "saves", "screenshots"),
                    default = "mods",
                ),
            ),
            required = listOf("folder"),
        ),
    ) { args, ctx ->
        val version = AIToolSupport.resolveVersion(args.str("instance"), ctx)
        val folder = folderOf(args.strOr("folder", "mods"))
        AIToolSupport.listDirectory(VersionFolders.valueOf(folder).getDir(version.getGameDir()))
    },

    aiTool(
        name = "delete_resource",
        description = """
            删除一个资源文件或整个存档目录。
            folder 取值同上；对存档（saves）会删除**整个世界文件夹**，务必谨慎。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "instance" to AIToolSchema.string("实例名称；不传则使用当前选中的实例"),
                "folder" to AIToolSchema.string(
                    "目标子目录",
                    enumValues = listOf("resourcepacks", "shaderpacks", "saves"),
                ),
                "name" to AIToolSchema.string("文件名或存档文件夹名"),
            ),
            required = listOf("folder", "name"),
        ),
        risk = AIToolRisk.DANGEROUS,
    ) { args, ctx ->
        val version = AIToolSupport.resolveVersion(args.str("instance"), ctx)
        val folder = VersionFolders.valueOf(folderOf(args.strOr("folder", "resourcepacks")))
        val dir = folder.getDir(version.getGameDir())
        val name = args.str("name")!!
        val target = File(dir, name)
        require(target.exists()) { "不存在：${target.absolutePath}" }
        // 防止通过 ../ 跳出该目录
        require(target.canonicalFile.parentFile == dir.canonicalFile) { "非法路径：$name" }

        val deleted = if (target.isDirectory) target.deleteRecursively() else target.delete()
        if (!deleted) throw IllegalStateException("删除失败：${target.absolutePath}")
        "已删除：${target.name}（${folder.folderName}）"
    },
)

private const val DISABLED_SUFFIX = ".disabled"

/** 在 mods 目录里按名字找模组文件（容忍带不带 .disabled 后缀） */
private fun findModFile(modsDir: File, name: String): File? {
    val files = modsDir.listFiles()?.filter { !it.isDirectory } ?: return null
    return files.firstOrNull { it.name == name }
        ?: files.firstOrNull { it.name == name + DISABLED_SUFFIX }
        ?: files.firstOrNull { it.name == name.removeSuffix(DISABLED_SUFFIX) }
        ?: files.firstOrNull { it.name.equals(name, ignoreCase = true) }
}

/** 把 folder 参数解析成枚举 */
private fun folderOf(raw: String): String = when (raw.trim().lowercase()) {
    "mods", "mod" -> VersionFolders.MOD.name
    "resourcepacks", "resourcepack", "resource_packs" -> VersionFolders.RESOURCE_PACK.name
    "shaderpacks", "shaderpack", "shaders" -> VersionFolders.SHADERS.name
    "saves", "save" -> VersionFolders.SAVES.name
    "screenshots" -> VersionFolders.SCREENSHOTS.name
    else -> throw IllegalArgumentException(
        "不支持的目录「$raw」，只能是 mods / resourcepacks / shaderpacks / saves / screenshots"
    )
}
