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

import com.movtery.zalithlauncher.ai.agent.AIToolRisk
import com.movtery.zalithlauncher.ai.agent.AIToolSchema
import com.movtery.zalithlauncher.game.download.assets.favorites.FavoriteProjectsRepository
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.favorites.FavoriteStore

/**
 * 收藏相关工具（27.8.0）
 *
 * 启动器的收藏现在分两类，**必须分开说清楚**，不要混：
 * - **在线收藏**：收藏的是平台上的资源项目（Modrinth / CurseForge），不占本地空间，
 *   可以用 `install_resource` 直接装进实例；
 * - **本地归档**：真正把文件复制进了应用私有目录，离线可用，删除只删副本。
 */
internal fun favoriteTools() = listOf(
    aiTool(
        name = "list_favorites",
        description = """
            列出用户的收藏，分两组返回：
            - online：在线收藏的资源项目（平台 + 项目 ID + 标题 + 类型 + 是否失效）
            - archive：本地归档（名称 + 类型 + 大小 + 导入时间 + 是否完整可读）
            用户问「我收藏了什么」「帮我看看收藏里有没有 XX」时用它。
            想安装在线收藏，把返回的 platform + project_id 交给 install_resource。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(properties = emptyMap(), required = emptyList()),
        risk = AIToolRisk.READ,
    ) { _, _ ->
        FavoriteProjectsRepository.ensureLoaded()
        val sb = StringBuilder()

        val online = FavoriteProjectsRepository.projects.values
            .sortedByDescending { it.project.followTime }
        sb.appendLine("online（${online.size}）")
        if (online.isEmpty()) {
            sb.appendLine("  （空）")
        } else {
            online.forEach { entry ->
                sb.appendLine(
                    "  - platform=${entry.platform.name} project_id=${entry.project.projectId} " +
                            "title=${entry.project.title} type=${entry.project.classes.name}" +
                            if (entry.invalid) "（已失效）" else ""
                )
            }
        }

        val archive = FavoriteStore.list()
        sb.appendLine("archive（${archive.size}）")
        if (archive.isEmpty()) {
            sb.appendLine("  （空）")
        } else {
            archive.forEach { item ->
                sb.appendLine(
                    "  - name=${item.name} type=${item.type.name} size=${item.size} " +
                            "imported_at=${item.importedAt} file=${item.fileName}"
                )
            }
        }
        sb.toString()
    },

    aiTool(
        name = "remove_favorite",
        description = """
            取消一条收藏。
            - target=online：只取消在线收藏标记，**不会**删除任何本地文件；
            - target=archive：只删除本地归档副本（可按 id 或 name 指定），**不会**取消在线收藏。
            删除前请先向用户确认，尤其是 archive。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "target" to AIToolSchema.string("取消哪一类", enumValues = listOf("online", "archive"), default = "online"),
                "platform" to AIToolSchema.string("target=online 时必填", enumValues = listOf("modrinth", "curseforge")),
                "project_id" to AIToolSchema.string("target=online 时的项目 ID"),
                "id" to AIToolSchema.string("target=archive 时的收藏 id"),
                "name" to AIToolSchema.string("target=archive 时也可用名称指定"),
            ),
            required = listOf("target"),
        ),
        risk = AIToolRisk.WRITE,
    ) { args, _ ->
        when (args.strOr("target", "online")) {
            "online" -> {
                val platform = args.str("platform")
                    ?: return@aiTool "缺少 platform（modrinth / curseforge）"
                val projectId = args.str("project_id") ?: return@aiTool "缺少 project_id"
                val matched = FavoriteProjectsRepository.projects.values.any {
                    it.platform.name.equals(platform, ignoreCase = true) &&
                            it.project.projectId == projectId
                }
                if (!matched) return@aiTool "没有找到这条在线收藏：$platform / $projectId"
                FavoriteProjectsRepository.unfavorite(
                    platform = Platform.valueOf(platform.uppercase()),
                    projectId = projectId
                )
                "已取消在线收藏（本地文件未改动）：$projectId"
            }
            else -> {
                val list = FavoriteStore.list()
                val target = args.str("id")?.let { id -> list.firstOrNull { it.id == id } }
                    ?: args.str("name")?.let { name -> list.firstOrNull { it.name == name } }
                    ?: return@aiTool "请提供 id 或 name（可先调用 list_favorites 查看）"
                val ok = FavoriteStore.delete(target.id)
                if (ok) "已删除本地归档副本：${target.name}（原文件未改动）"
                else "删除失败：没有找到该归档"
            }
        }
    },
)
