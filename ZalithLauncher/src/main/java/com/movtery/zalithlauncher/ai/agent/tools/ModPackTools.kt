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
import com.movtery.zalithlauncher.context.GlobalContext
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformReleaseType
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
import com.movtery.zalithlauncher.game.download.assets.platform.getProjectByVersion
import com.movtery.zalithlauncher.game.download.assets.platform.getVersions
import com.movtery.zalithlauncher.game.download.modpack.install.ModPackInstaller
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.file.INVALID_CHARACTERS_REGEX
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

private const val TAG = "AIModPackTools"

/**
 * 整合包安装工具。
 *
 * 整合包和普通资源**不是一回事**：它不是往现有实例里塞一个文件，
 * 而是「下载整合包 → 解析它需要的游戏版本与加载器 → 装一个全新的实例 → 复制模组与配置」。
 * 所以它走的是 [ModPackInstaller]，和「下载 → 整合包」页面完全同一条链路。
 *
 * 与其它安装工具一样：静默后台执行，进度只出现在顶部细进度条上，
 * 但这里会**等到装完**再把真实结果告诉模型 —— 整合包安装失败的原因
 * （解析失败 / 加载器不匹配 / 下载失败）必须如实转述，不能含糊。
 */
internal fun modPackTools(): List<AITool> = listOf(installModPackTool())

private fun installModPackTool() = aiTool(
    name = "install_modpack",
    description = """
        安装一个**整合包**（modpack）：会新建一个独立实例，自动装好它要求的
        Minecraft 版本、模组加载器、全部模组与配置。与「下载 → 整合包」页面完全同一条流程。

        参数说明：
        - platform：必填，modrinth / curseforge
        - project_id：必填，整合包项目 ID（来自 search_resources，type=modpack）
        - version_id：可选，整合包的具体文件版本；不传则自动选最新的正式版
        - name：可选，新实例的名称；不传会自动用整合包自己的名字（重名会自动加序号）

        ⚠️ 整合包会**新建一个实例**，不会装进用户现有的实例，也不会影响现有实例的模组。
        ⚠️ 安装过程较长（可能要下载几百 MB），进度在启动器顶部细进度条上；
        工具会等到真正装完才返回，所以不要在结果返回前就说「装好了」。
        用户只是想要某个模组时，别把整个整合包装一遍 —— 用 install_resource。
    """.trimIndent(),
    parameters = AIToolSchema.objectSchema(
        properties = mapOf(
            "platform" to AIToolSchema.string("平台", enumValues = listOf("modrinth", "curseforge")),
            "project_id" to AIToolSchema.string("整合包项目 ID"),
            "version_id" to AIToolSchema.string("整合包文件版本 ID；不传则选最新正式版"),
            "name" to AIToolSchema.string("新实例名称；不传自动生成"),
        ),
        required = listOf("platform", "project_id"),
    ),
    risk = AIToolRisk.WRITE,
) { args, _ ->
    val platform = when (args.str("platform")!!.trim().lowercase()) {
        "modrinth", "mr" -> Platform.MODRINTH
        "curseforge", "cf" -> Platform.CURSEFORGE
        else -> throw IllegalArgumentException("未知平台，只能是 modrinth 或 curseforge")
    }
    val projectId = args.str("project_id")!!
    val wantedVersionId = args.str("version_id")
    val requestedName = args.str("name")

    //① 拉整合包的可下载版本（整合包版本不依赖目标实例，它自带游戏版本信息）
    val versions = try {
        getVersions(projectID = projectId, platform = platform)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Logger.warning(TAG, "Failed to load modpack versions of $projectId", e)
        return@aiTool "拉取整合包版本列表失败：${e.message ?: "网络或来源异常"}。可以稍后再试。"
    }

    if (versions.isEmpty()) {
        return@aiTool "这个项目没有可安装的整合包文件（可能只有源码，或者已被作者删除）。"
    }

    //② 过滤掉拿不到文件信息的版本（部分版本需要额外初始化才有下载链接）
    val usable = versions
        .sortedByDescending { it.platformDatePublished() }
        .mapNotNull { version ->
            val ok = try {
                version.initFile(projectId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                Logger.warning(TAG, "Skipped an unusable modpack version ${version.platformId()}: ${e.message}")
                false
            }
            version.takeIf { ok }
        }

    if (usable.isEmpty()) {
        return@aiTool "整合包里没有可用的下载文件（所有版本的链接都拿不到）。"
    }

    //③ 选版本：指定优先；否则优先正式版，其次最新版
    val chosen: PlatformVersion = if (wantedVersionId != null) {
        usable.firstOrNull { it.platformId() == wantedVersionId }
            ?: return@aiTool "找不到版本 ID 为 $wantedVersionId 的整合包文件。" +
                "可用版本（前 5 个）：" +
                usable.take(5).joinToString("、") { "${it.platformId()}（${it.platformDisplayName()}）" }
    } else {
        usable.firstOrNull { it.platformReleaseType() == PlatformReleaseType.RELEASE } ?: usable.first()
    }

    val iconUrl = try {
        getProjectByVersion(projectId = projectId, platform = platform).platformIconUrl()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Logger.warning(TAG, "Failed to load the modpack icon of $projectId", e)
        null
    }

    //④ 装上（内部等安装器跑完）
    val installedName = installModPackWithProgress(
        version = chosen,
        iconUrl = iconUrl,
        requestedName = requestedName
    )

    val info = runCatching {
        VersionsManager.versions.value.firstOrNull { it.getVersionName() == installedName }
    }.getOrNull()

    buildString {
        append("整合包已安装完成，新实例名称：").append(installedName).append('\n')
        append("整合包版本：").append(chosen.platformDisplayName())
        append("（").append(platform.displayName).append(" / ").append(projectId).append("）\n")
        info?.let {
            runCatching {
                append("游戏版本与加载器：").append(it.getVersionSummary()).append('\n')
            }
        }
        append("这是一个**独立的实例**，之前已有的实例没有被动过。")
        append("可以直接用 launch_game（instance=\"").append(installedName).append("\"）启动它。")
    }
}

/**
 * 执行整合包安装，并把安装器的阶段进度映射到全局任务队列。
 *
 * @return 装好后的实例名称
 */
private suspend fun installModPackWithProgress(
    version: PlatformVersion,
    iconUrl: String?,
    requestedName: String?
): String = coroutineScope {
    val result = CompletableDeferred<Result<String>>()
    val taskId = "ai_install_modpack_${version.platformId()}_${System.currentTimeMillis()}"

    //用户从顶部进度条上取消时，必须把真正的安装器也一起停掉，
    //否则「AI 说取消了、后台还在下」——比不让取消更糟
    var runningInstaller: ModPackInstaller? = null

    TaskSystem.submitTask(
        Task.runTask(
            id = taskId,
            task = { task ->
                task.updateProgress(-1f)
                task.updateMessage(androidText("正在下载并解析整合包…"))

                val installer = ModPackInstaller(
                    context = GlobalContext,
                    version = version,
                    iconUrl = iconUrl,
                    scope = TaskSystem.appScope,
                    //安装器要求给出新实例名称：AI 就直接用整合包自己的名字（或用户指定的名字）
                    waitForVersionName = { info ->
                        resolveInstanceName(requested = requestedName, suggested = info.name)
                    },
                    //⚠️ AI 发起的安装没有「是否使用移动网络」的确认界面，
                    //   这里必须立刻给出答复，否则安装会永远卡在等待里；
                    //   用户已经在聊天里明确要求安装，就按同意处理。
                    waitForConfirmMobileData = { true }
                )

                runningInstaller = installer

                installer.installModPack(
                    onInstalled = { name ->
                        VersionsManager.refresh("[AI] install_modpack onInstalled", name)
                        result.complete(Result.success(name))
                    },
                    onCancelled = {
                        result.complete(
                            Result.failure(IllegalStateException("整合包安装被取消了"))
                        )
                    },
                    onError = { e ->
                        result.complete(Result.failure(e))
                    }
                )

                //镜像安装器内部各阶段的进度（顶部那条细进度条就是用户能看到的全部反馈）
                while (!result.isCompleted) {
                    val list = installer.tasksFlow.value
                    if (list.isNotEmpty()) {
                        val average = list
                            .map { it.task.progress.value.coerceIn(0f, 1f) }
                            .average()
                            .toFloat()
                        task.updateProgress(average)
                        list.firstOrNull { it.task.stage.value.name == "RUNNING" }
                            ?.let { task.updateMessage(it.title) }
                    }
                    delay(500)
                }
            },
            onError = { e ->
                result.complete(Result.failure(e))
            },
            onCancel = {
                runCatching { runningInstaller?.cancelInstall() }
            },
            onFinally = {
                //被用户从进度条上取消时不会走 onError，这里兜底
                result.complete(Result.failure(IllegalStateException("整合包安装任务已被取消")))
            }
        )
    )

    result.await().getOrThrow()
}

/**
 * 生成一个**合法且不重名**的实例名称。
 *
 * 整合包名字里可能有 `\\ / : * ?` 之类不能做目录名的字符，
 * 直接拿去建目录会失败；重名则会撞上「已经安装过了」。
 */
private fun resolveInstanceName(requested: String?, suggested: String): String {
    val base = (requested ?: suggested)
        .replace(Regex(INVALID_CHARACTERS_REGEX), "_")
        .replace("..", "_")
        .trim()
        .take(64)
        .ifBlank { "整合包" }

    if (!VersionsManager.isVersionExists(base)) return base

    for (index in 2..99) {
        val candidate = "$base ($index)"
        if (!VersionsManager.isVersionExists(candidate)) return candidate
    }
    return "$base (${System.currentTimeMillis() % 1000})"
}
