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
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchFilter
import com.movtery.zalithlauncher.game.download.resources.ResourceManager
import com.movtery.zalithlauncher.game.download.resources.ResourceType
import com.movtery.zalithlauncher.utils.logging.Logger

private const val TAG = "AIResourceTools"

/**
 * 资源搜索与安装工具。
 *
 * 全部复用 ZyNova 的统一资源核心（[ResourceManager]）：
 * 搜索、版本匹配、前置依赖解析、下载、校验、安装都由那一套完成，
 * 这里**不重新实现任何下载/解析逻辑**。
 */
internal fun resourceTools(): List<AITool> = listOf(

    aiTool(
        name = "search_resources",
        description = """
            在 Modrinth / CurseForge 上搜索模组、资源包、光影或存档。
            返回每一项的**平台、项目 ID、标题、作者、简介、下载量**。
            拿到 project_id 后就可以用 install_resource 真正安装。
            用户说「帮我找个 XX 模组」时用它。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "query" to AIToolSchema.string("搜索关键词"),
                "type" to AIToolSchema.string(
                    "资源类型",
                    enumValues = listOf("mod", "resourcepack", "shader", "save"),
                    default = "mod",
                ),
                "platform" to AIToolSchema.string(
                    "平台；不传则两个平台都搜",
                    enumValues = listOf("modrinth", "curseforge"),
                ),
                "limit" to AIToolSchema.integer("每个平台返回多少条", default = 10, minimum = 1, maximum = 30),
                "instance" to AIToolSchema.string("按该实例的游戏版本过滤搜索结果；不传则使用当前实例"),
            ),
            required = listOf("query"),
        ),
    ) { args, ctx ->
        val type = parseResourceType(args.strOr("type", "mod"))
        val limit = args.int("limit") ?: 10
        val query = args.str("query")!!

        //尽量带上目标实例的游戏版本，减少装上不兼容资源的情况
        val gameVersion = runCatching {
            AIToolSupport.resolveVersion(args.str("instance"), ctx)
                .getVersionInfo()?.minecraftVersion
        }.getOrNull()

        val platforms = args.str("platform")?.let { listOf(parsePlatform(it)) }
            ?: listOf(Platform.MODRINTH, Platform.CURSEFORGE)

        val sb = StringBuilder()
        sb.append("搜索「").append(query).append("」（类型=").append(type.name)
        if (!gameVersion.isNullOrBlank()) sb.append("，游戏版本=").append(gameVersion)
        sb.append("）：\n\n")

        platforms.forEach { platform ->
            sb.append("── ").append(platform.displayName).append('\n')
            val page = runCatching {
                ResourceManager.search(
                    platform = platform,
                    query = query,
                    type = type,
                    filter = PlatformSearchFilter(
                        searchName = query,
                        gameVersion = gameVersion.orEmpty(),
                        limit = limit,
                    )
                ).getAssetsPage(type.classes)
            }.onFailure { e ->
                Logger.warning(TAG, "Search on ${platform.name} failed.", e)
            }.getOrNull()

            if (page == null) {
                sb.append("   （搜索失败，可能是网络问题）\n\n")
                return@forEach
            }
            if (page.data.isEmpty()) {
                sb.append("   （没有结果）\n\n")
                return@forEach
            }
            page.data.forEach { (data, translation) ->
                sb.append("   · ").append(translation?.name?.takeIf { it.isNotBlank() } ?: data.platformTitle())
                    .append('\n')
                sb.append("     project_id=").append(data.platformId())
                    .append(" | 平台=").append(data.platform().name)
                    .append(" | 作者=").append(data.platformAuthor())
                    .append(" | 下载量=").append(data.platformDownloadCount())
                    .append('\n')
                data.platformDescription().take(160).takeIf { it.isNotBlank() }?.let {
                    sb.append("     ").append(it).append('\n')
                }
            }
            sb.append('\n')
        }
        sb.toString()
    },

    aiTool(
        name = "install_resource",
        description = """
            从 Modrinth / CurseForge 安装一个资源到指定实例。
            它会自动完成：**版本匹配 → 选择兼容文件 → 递归安装必需前置依赖 → 下载 → 校验 → 安装**。
            返回内容会说明实际安装了哪个版本，以及是否有必需前置没装上。
            用户说「帮我装上这个模组」时用它；不要只给链接让用户自己装。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "platform" to AIToolSchema.string("平台", enumValues = listOf("modrinth", "curseforge")),
                "project_id" to AIToolSchema.string("项目 ID（通常来自 search_resources 的结果）"),
                "type" to AIToolSchema.string(
                    "资源类型",
                    enumValues = listOf("mod", "resourcepack", "shader", "save"),
                    default = "mod",
                ),
                "instance" to AIToolSchema.string("目标实例名称；不传则使用当前选中的实例"),
            ),
            required = listOf("platform", "project_id"),
        ),
        risk = AIToolRisk.WRITE,
    ) { args, ctx ->
        val platform = parsePlatform(args.str("platform")!!)
        val projectId = args.str("project_id")!!
        val type = parseResourceType(args.strOr("type", "mod"))
        val instance = AIToolSupport.resolveVersion(args.str("instance"), ctx)

        val outcome = ResourceManager.installToInstance(
            platform = platform,
            projectId = projectId,
            type = type,
            instance = instance,
        )

        buildString {
            append("已安装到实例「").append(instance.getVersionName()).append("」：")
            append(outcome.version.displayName)
            append("（").append(platform.displayName).append(" / ").append(projectId).append("）\n")
            append("实际安装文件数：").append(outcome.result.installedCount).append('\n')
            if (outcome.result.hasMissingRequiredDependencies) {
                append("\n⚠️ 以下必需前置**没有装上**，游戏可能因此崩溃：\n")
                outcome.result.missingRequiredDependencies.forEach {
                    append("   · ").append(it.toString()).append('\n')
                }
                append("请尝试用 search_resources 找到它们并单独安装。")
            } else {
                append("前置依赖均已满足。")
            }
        }
    },
)

private fun parsePlatform(raw: String): Platform = when (raw.trim().lowercase()) {
    "modrinth", "mr" -> Platform.MODRINTH
    "curseforge", "cf" -> Platform.CURSEFORGE
    else -> throw IllegalArgumentException("未知平台「$raw」，只能是 modrinth 或 curseforge")
}

private fun parseResourceType(raw: String): ResourceType = when (raw.trim().lowercase()) {
    "mod", "mods" -> ResourceType.MOD
    "resourcepack", "resource_pack", "resourcepacks", "rp" -> ResourceType.RESOURCE_PACK
    "shader", "shaders", "shaderpack", "shaderpacks" -> ResourceType.SHADER
    "save", "saves", "world" -> ResourceType.SAVE
    else -> throw IllegalArgumentException(
        "未知资源类型「$raw」，只能是 mod / resourcepack / shader / save"
    )
}
