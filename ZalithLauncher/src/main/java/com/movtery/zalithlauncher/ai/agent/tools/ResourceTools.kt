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
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchFilter
import com.movtery.zalithlauncher.game.download.resources.ResourceInstallOutcome
import com.movtery.zalithlauncher.game.download.resources.ResourceManager
import com.movtery.zalithlauncher.game.download.resources.ResourceType
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope

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

            资源类型（模组 / 资源包 / 光影 / 存档）**可以不传**：
            工具会先问平台确认这个项目到底是什么类型，再决定装进哪个目录，装错目录是看不到的。
            如果项目其实是**整合包**，这里会明确拒绝并告诉你去用整合包安装流程。

            安装过程会作为后台任务出现在启动器顶部的细进度条里（可取消），
            返回值会说明实际装了哪个版本、装了几个文件、有没有必需前置没装上。
            已经装过的文件不会重复下载。

            用户说「帮我装上这个模组」时用它；不要只给链接让用户自己装。
        """.trimIndent(),
        parameters = AIToolSchema.objectSchema(
            properties = mapOf(
                "platform" to AIToolSchema.string("平台", enumValues = listOf("modrinth", "curseforge")),
                "project_id" to AIToolSchema.string("项目 ID（通常来自 search_resources 的结果）"),
                "type" to AIToolSchema.string(
                    "资源类型；不传则自动判断（猜错会装进错误的目录）",
                    enumValues = listOf("mod", "resourcepack", "shader", "save"),
                ),
                "instance" to AIToolSchema.string("目标实例名称；不传则使用当前选中的实例"),
            ),
            required = listOf("platform", "project_id"),
        ),
        risk = AIToolRisk.WRITE,
    ) { args, ctx ->
        val platform = parsePlatform(args.str("platform")!!)
        val projectId = args.str("project_id")!!
        val hint = args.str("type")?.let { parseResourceType(it) }
        val instance = AIToolSupport.resolveVersion(args.str("instance"), ctx)

        //① 先确认项目真实类型：类型决定安装目录，猜错等于「装好了但游戏里看不到」
        val actualType = ResourceManager.detectType(
            platform = platform,
            projectId = projectId,
            fallback = hint ?: ResourceType.MOD
        )
        if (actualType == ResourceType.MOD_PACK) {
            return@aiTool "这个项目其实是**整合包**（modpack），不是普通资源。" +
                "整合包有自己的安装流程（会创建独立实例），当前 install_resource 装不了；" +
                "请让用户到「下载 → 整合包」页面安装。"
        }

        //② 真正安装：交给 TaskSystem，进度出现在顶部细进度条，可以取消
        val outcome = installResourceWithProgress(
            type = actualType,
            platform = platform,
            projectId = projectId,
            instance = instance
        )

        val corrected = hint != null && hint != actualType
        buildString {
            if (outcome.result.installedCount == 0 && !outcome.result.hasMissingRequiredDependencies) {
                append("实例「").append(instance.getVersionName()).append("」里已经装过这个资源")
                append("（").append(outcome.version.displayName).append("），没有重复下载。")
                return@buildString
            }

            append("已安装到实例「").append(instance.getVersionName()).append("」：")
            append(outcome.version.displayName)
            append("（").append(platform.displayName).append(" / ").append(projectId).append("）\n")
            append("资源类型：").append(actualType.name)
            if (corrected) {
                append("（你传的是 ").append(hint!!.name).append("，实际是 ").append(actualType.name).append("，已按实际类型安装）")
            }
            append('\n')
            append("实际安装文件数：").append(outcome.result.installedCount).append('\n')
            if (outcome.result.hasMissingRequiredDependencies) {
                append("\n⚠️ 以下必需前置**没有装上**，游戏可能因此崩溃：\n")
                outcome.result.missingRequiredDependencies.forEach {
                    append("   · ").append(it.toString()).append('\n')
                }
                append("请用 search_resources 找到它们并单独安装。")
            } else {
                append("前置依赖均已满足。")
            }
            append("\n（装完如果游戏正在运行，重启游戏才会加载新内容。）")
        }
    },
)

/**
 * 把资源安装放进全局任务队列，进度显示在顶部细进度条上。
 *
 * 之所以不直接调用 [ResourceManager.installToInstance]：
 * - 资源安装可能要下载几百 MB，AI 页面被切走后仍需继续；
 * - 用户需要能看到进度、能取消 —— 这两件事都由 [TaskSystem] 统一提供。
 */
private suspend fun installResourceWithProgress(
    type: ResourceType,
    platform: Platform,
    projectId: String,
    instance: Version
): ResourceInstallOutcome = coroutineScope {
    val result = CompletableDeferred<Result<ResourceInstallOutcome>>()
    //id 带上时间戳：同一个资源重复安装时不会被 TaskSystem 的去重逻辑吞掉
    val taskId = "ai_install_resource_${instance.getVersionName()}_${platform.name}_${projectId}_${System.currentTimeMillis()}"

    TaskSystem.submitTask(
        Task.runTask(
            id = taskId,
            task = { task ->
                task.updateProgress(-1f)
                task.updateMessage(androidText("正在解析 ${projectId} 的兼容版本…"))
                val outcome = ResourceManager.installToInstance(
                    platform = platform,
                    projectId = projectId,
                    type = type,
                    instance = instance,
                    onProgress = { progress ->
                        task.updateProgress(progress.fraction)
                        task.updateMessage(
                            androidText(
                                "${progress.finishedCount}/${progress.totalCount}  ${progress.fileName}"
                            )
                        )
                    }
                )
                task.updateProgress(1f)
                result.complete(Result.success(outcome))
            },
            onError = { e ->
                result.complete(Result.failure(e))
            },
            onFinally = {
                //被用户从进度条上取消时，onError 不会触发，这里兜底
                result.complete(
                    Result.failure(IllegalStateException("资源安装任务已被取消"))
                )
            }
        )
    )

    result.await().getOrThrow()
}

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
