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

import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ai.agent.AITool
import com.movtery.zalithlauncher.ai.agent.AIToolRisk
import com.movtery.zalithlauncher.ai.agent.AIToolSchema
import com.movtery.zalithlauncher.context.GlobalContext
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskSystem
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.addons.modloader.cleanroom.CleanroomVersions
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.fabric.FabricAPIVersions
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.fabric.FabricVersions
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.legacyfabric.LegacyFabricAPIVersions
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.legacyfabric.LegacyFabricVersions
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.quilt.QuiltAPIVersions
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.quilt.QuiltVersions
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.forge.ForgeVersions
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge.NeoForgeVersions
import com.movtery.zalithlauncher.game.addons.modloader.optifine.OptiFineVersion
import com.movtery.zalithlauncher.game.addons.modloader.optifine.OptiFineVersions
import com.movtery.zalithlauncher.game.download.game.GameDownloadInfo
import com.movtery.zalithlauncher.game.download.game.GameInstaller
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersion
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersions
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.ui.toAndroidString
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

private const val TAG = "GameInstallTools"

/** 拉取加载器版本列表的超时（网络差时不要让工具一直挂着）*/
private const val LOADER_FETCH_TIMEOUT_MS = 90_000L

/**
 * 游戏本体安装工具。
 *
 * ⚠️ 真正的安装逻辑**完全复用** `GameInstaller`（与「下载 → 游戏」页面同一条路径），
 * 这里只负责：
 * 1. 把模型给的自然语言参数翻译成 [GameDownloadInfo]；
 * 2. 自动挑选加载器版本（调用方不指定就选最新的稳定版）；
 * 3. 把安装转成**静默后台任务**，进度汇入启动器顶部的细进度条。
 *
 * 安装一旦开始就不再阻塞模型 —— 模型可以立刻回复「已在后台开始」，
 * 之后用 [GameInstallRunner.describeStatus]（`install_status` 工具）查询真实进度与结果。
 *
 * 议题 #78 起安装会话是**多份**的：可以同时发起多个本体安装（各用独立实例名 / 独立临时目录），
 * 不再有「已经有一个安装任务在进行中」这种全局单槽门闩。
 */
internal fun gameInstallTools(): List<AITool> = listOf(
    installGameTool(),
    installStatusTool(),
    listDownloadableVersionsTool(),
)

// ─────────────────────────────────────────────────────────────
//  install_game
// ─────────────────────────────────────────────────────────────

private fun installGameTool() = aiTool(
    name = "install_game",
    description = """
        下载并安装一个 Minecraft 实例（游戏本体 + 所选加载器）。这是**真的安装**，会真实下载上百 MB 文件。
        与「下载 → 游戏」页面走完全相同的安装流程：原版核心、库文件、资源文件、加载器一起装好，
        装完自动出现在实例列表里，可以直接 launch_game。

        参数说明：
        - game_version：必填，例如 "1.20.1"、"1.21.4"。不确定有哪些版本时先调 list_downloadable_versions。
        - loader：可选，"vanilla"（默认，纯原版）/ "fabric" / "quilt" / "legacyfabric" / "forge" / "neoforge" / "optifine" / "cleanroom"。
        - loader_version：可选，指定加载器版本号；不传则自动选最新的稳定版。
        - name：可选，实例名称；不传会自动生成（例如 "1.20.1 Fabric 0.15.11"）。
        - overwrite：可选，同名实例已存在时是否覆盖重装，默认 false。

        ⚠️ 安装是**静默后台任务**：调用后立即返回「已开始」，不要谎称「已经装好」。
        需要汇报真实进度或结果时用 install_status；装完用 list_instances 确认。
        ⚠️ Fabric / Quilt / Legacy Fabric 会**自动**把对应的 API 模组装上，不要另外再装一遍。
        ⚠️ 可以**同时并行安装多个**：每次调用都是一个独立的后台任务（顶部任务条各占一条）。
        唯一限制是**实例名不能重复**：同名实例已经在装 / 已经存在时会明确拒绝，
        想同时装同一个游戏版本的多个实例，用 name 参数给它们不同的名字。
    """.trimIndent(),
    parameters = AIToolSchema.objectSchema(
        properties = mapOf(
            "game_version" to AIToolSchema.string("Minecraft 版本号，例如 1.20.1"),
            "loader" to AIToolSchema.string(
                "模组加载器；不传即纯原版",
                enumValues = listOf(
                    "vanilla", "fabric", "quilt", "legacyfabric",
                    "forge", "neoforge", "optifine", "cleanroom",
                ),
            ),
            "loader_version" to AIToolSchema.string("加载器版本号；不传自动选最新稳定版"),
            "name" to AIToolSchema.string("实例名称；不传自动生成"),
            "overwrite" to AIToolSchema.bool("同名实例已存在时是否覆盖重装", default = false),
        ),
        required = listOf("game_version"),
    ),
    risk = AIToolRisk.WRITE,
) { args, _ ->
    val gameVersion = args.str("game_version")
        ?: throw IllegalArgumentException("缺少 game_version（例如 1.20.1）")
    val loader = args.strOr("loader", "vanilla").lowercase().replace("-", "").replace("_", "")
    val loaderVersion = args.str("loader_version")
    val overwrite = args.bool("overwrite") ?: false

    // 版本号合法性：拿得到清单就顺手校验一次，拿不到就交给安装器去报错
    val known = downloadableVersions()
    if (known.isNotEmpty() && known.none { it.version.id == gameVersion }) {
        val similar = known.filter { it.version.id.startsWith(gameVersion.substringBefore('.')) }
            .take(8)
            .joinToString("、") { it.version.id }
        return@aiTool "清单里没有 Minecraft「$gameVersion」这个版本。" +
            if (similar.isNotEmpty()) "你是想装这些吗：$similar？" else "请用 list_downloadable_versions 先确认版本号。"
    }

    val info = buildInstallInfo(gameVersion, loader, loaderVersion, args.str("name"), overwrite)
    val outcome = GameInstallRunner.start(info, gameVersion = gameVersion, loader = loader)
    outcome.message
}

/**
 * 根据模型给的参数组装 [GameDownloadInfo]。
 * 加载器版本不指定时自动挑：优先稳定版，其次最新版。
 */
private suspend fun buildInstallInfo(
    gameVersion: String,
    loader: String,
    loaderVersion: String?,
    customName: String?,
    overwrite: Boolean,
): GameDownloadInfo {
    fun name(suffix: String) = customName ?: "$gameVersion $suffix"

    return when (loader) {
        "vanilla", "" -> GameDownloadInfo(
            gameVersion = gameVersion,
            customVersionName = name("").trim(),
            overwrite = overwrite,
        )

        "fabric" -> {
            val list = fetch("Fabric Loader") { FabricVersions.fetchFabricLoaderList(gameVersion) }
                ?: throw IllegalStateException("拉取 Fabric Loader 版本列表失败（网络或镜像问题）。稍后再试，或换个加载器。")
            val pick = pickFabricLike(list.map { it.version to it.stable }, loaderVersion)
                ?: throw IllegalStateException("Fabric 没有适用于 Minecraft $gameVersion 的加载器版本。")
            val api = fetch("Fabric API") { FabricAPIVersions.fetchVersionList(gameVersion) }?.firstOrNull()
            GameDownloadInfo(
                gameVersion = gameVersion,
                customVersionName = name("${ModLoader.FABRIC.displayName} $pick"),
                overwrite = overwrite,
                fabric = list.first { it.version == pick },
                fabricAPI = api,
            )
        }

        "quilt" -> {
            val list = fetch("Quilt Loader") { QuiltVersions.fetchQuiltLoaderList(gameVersion) }
                ?: throw IllegalStateException("拉取 Quilt Loader 版本列表失败（网络或镜像问题）。")
            val pick = pickFabricLike(list.map { it.version to true }, loaderVersion)
                ?: throw IllegalStateException("Quilt 没有适用于 Minecraft $gameVersion 的加载器版本。")
            val api = fetch("Quilted Fabric API") { QuiltAPIVersions.fetchVersionList(gameVersion) }?.firstOrNull()
            GameDownloadInfo(
                gameVersion = gameVersion,
                customVersionName = name("${ModLoader.QUILT.displayName} $pick"),
                overwrite = overwrite,
                quilt = list.first { it.version == pick },
                quiltAPI = api,
            )
        }

        "legacyfabric" -> {
            val list = fetch("Legacy Fabric Loader") { LegacyFabricVersions.fetchFabricLoaderList(gameVersion) }
                ?: throw IllegalStateException("拉取 Legacy Fabric 版本列表失败（网络或镜像问题）。")
            val pick = pickFabricLike(list.map { it.version to it.stable }, loaderVersion)
                ?: throw IllegalStateException("Legacy Fabric 没有适用于 Minecraft $gameVersion 的加载器版本。")
            val api = fetch("Legacy Fabric API") { LegacyFabricAPIVersions.fetchVersionList(gameVersion) }?.firstOrNull()
            GameDownloadInfo(
                gameVersion = gameVersion,
                customVersionName = name("${ModLoader.LEGACY_FABRIC.displayName} $pick"),
                overwrite = overwrite,
                legacyFabric = list.first { it.version == pick },
                legacyFabricAPI = api,
            )
        }

        "forge" -> {
            val list = fetch("Forge") { ForgeVersions.fetchForgeList(gameVersion) }
                ?: throw IllegalStateException("拉取 Forge 版本列表失败（网络或镜像问题）。Forge 老版本的列表经常抽风，可以稍后再试。")
            if (list.isEmpty()) throw IllegalStateException("Forge 没有适用于 Minecraft $gameVersion 的版本。")
            val pick = list.firstOrNull { loaderVersion != null && it.versionName == loaderVersion }
                ?: list.first()
            GameDownloadInfo(
                gameVersion = gameVersion,
                customVersionName = name("${ModLoader.FORGE.displayName} ${pick.versionName}"),
                overwrite = overwrite,
                forge = pick,
            )
        }

        "neoforge" -> {
            val list = fetch("NeoForge") { NeoForgeVersions.fetchNeoForgeList(gameVersion = gameVersion) }
                ?: throw IllegalStateException("拉取 NeoForge 版本列表失败（网络或镜像问题）。")
            if (list.isEmpty()) throw IllegalStateException("NeoForge 没有适用于 Minecraft $gameVersion 的版本。")
            val pick = list.firstOrNull { loaderVersion != null && it.versionName == loaderVersion }
                ?: list.first()
            GameDownloadInfo(
                gameVersion = gameVersion,
                customVersionName = name("${ModLoader.NEOFORGE.displayName} ${pick.versionName}"),
                overwrite = overwrite,
                neoforge = pick,
            )
        }

        "optifine" -> {
            val list = fetch("OptiFine") { OptiFineVersions.fetchOptiFineList(gameVersion = gameVersion) }
                ?: throw IllegalStateException("拉取 OptiFine 列表失败（网络或镜像问题）。")
            if (list.isEmpty()) throw IllegalStateException("OptiFine 没有适用于 Minecraft $gameVersion 的版本。")
            val pick: OptiFineVersion = list.firstOrNull { loaderVersion != null && it.fileName == loaderVersion }
                ?: list.firstOrNull { !it.isPreview }
                ?: list.first()
            GameDownloadInfo(
                gameVersion = gameVersion,
                customVersionName = name("${ModLoader.OPTIFINE.displayName} ${pick.realVersion}"),
                overwrite = overwrite,
                optifine = pick,
            )
        }

        "cleanroom" -> {
            if (gameVersion != "1.12.2") {
                throw IllegalStateException("Cleanroom 只支持 Minecraft 1.12.2，不能用于 $gameVersion。")
            }
            val list = fetch("Cleanroom") { CleanroomVersions.fetchLoaderList(gameVersion) }
                ?: throw IllegalStateException("拉取 Cleanroom 版本列表失败（网络或镜像问题）。")
            if (list.isEmpty()) throw IllegalStateException("没有可用的 Cleanroom 版本。")
            val pick = list.firstOrNull { loaderVersion != null && it.version == loaderVersion } ?: list.first()
            GameDownloadInfo(
                gameVersion = gameVersion,
                customVersionName = name("${ModLoader.CLEANROOM.displayName} ${pick.version}"),
                overwrite = overwrite,
                cleanroom = pick,
            )
        }

        else -> throw IllegalArgumentException(
            "不认识的加载器「$loader」。可用值：vanilla / fabric / quilt / legacyfabric / forge / neoforge / optifine / cleanroom。"
        )
    }
}

/** 从「版本号 → 是否稳定」的列表里挑一个 */
private fun pickFabricLike(list: List<Pair<String, Boolean>>, wanted: String?): String? {
    if (list.isEmpty()) return null
    if (wanted != null) {
        return list.firstOrNull { it.first == wanted }?.first
            ?: throw IllegalArgumentException(
                "指定的加载器版本「$wanted」不存在。可用版本（前 10 个）：" +
                    list.take(10).joinToString("、") { it.first }
            )
    }
    return list.firstOrNull { it.second }?.first ?: list.first().first
}

/**
 * 网络拉取 + 超时 + 异常收敛成 null。
 *
 * ⚠️ 不能用裸 `runCatching`：它会把协程取消（CancellationException）一起吞掉，
 * 用户点「停止」之后请求还在后台跑。所以取消异常单独放行。
 */
private suspend fun <T> fetch(what: String, block: suspend () -> T?): T? = withContext(Dispatchers.IO) {
    try {
        withTimeoutOrNull(LOADER_FETCH_TIMEOUT_MS) { block() }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Logger.warning(TAG, "Failed to fetch $what", e)
        null
    }
}

/** 可下载的 Minecraft 版本（拿不到就返回空列表，不做硬性校验）*/
private suspend fun downloadableVersions(): List<MinecraftVersion> = withContext(Dispatchers.IO) {
    try {
        withTimeoutOrNull(30_000) { MinecraftVersions.refreshVersions() }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Logger.warning(TAG, "Failed to refresh Minecraft versions", e)
    }
    MinecraftVersions.allVersions.value
}

// ─────────────────────────────────────────────────────────────
//  install_status
// ─────────────────────────────────────────────────────────────

private fun installStatusTool() = aiTool(
    name = "install_status",
    description = """
        查询游戏安装任务的**真实**状态：列出**所有**正在进行的安装（每个的目标、总进度、当前步骤、已用时），
        以及最近一次安装的结果、当前已有的实例。
        用 install_game 之后，用户问「装好了没」、或者你要确认能不能 launch_game 时调用它。
        同时装了多个时，这里会一条条列出来。
        它不会启动任何新的下载。
    """.trimIndent(),
    parameters = AIToolSchema.noArgs(),
) { _, _ -> GameInstallRunner.describeStatus() }

// ─────────────────────────────────────────────────────────────
//  list_downloadable_versions
// ─────────────────────────────────────────────────────────────

private fun listDownloadableVersionsTool() = aiTool(
    name = "list_downloadable_versions",
    description = """
        列出可以下载安装的 Minecraft 版本号（正式版在最前面，后面附带一部分快照）。
        在 install_game 之前用它确认版本号写法，尤其是「最新的正式版是哪个」这种问题。
    """.trimIndent(),
    parameters = AIToolSchema.objectSchema(
        properties = mapOf(
            "include_snapshots" to AIToolSchema.bool("是否包含快照版，默认 true", default = true),
        ),
    ),
) { args, _ ->
    val includeSnapshots = args.bool("include_snapshots") ?: true
    val all = downloadableVersions()
    if (all.isEmpty()) {
        return@aiTool "暂时拿不到 Minecraft 版本清单（网络问题）。可以直接指定版本号尝试安装，例如 1.20.1。"
    }

    val releases = all.filter { it.type == MinecraftVersion.Type.Release }
    val snapshots = all.filter { it.type == MinecraftVersion.Type.Snapshot }
    buildString {
        append("正式版共 ").append(releases.size).append(" 个，最近的 20 个：\n")
        append(releases.take(20).joinToString("、") { it.version.id })
        if (includeSnapshots && snapshots.isNotEmpty()) {
            append("\n\n快照版（最近 10 个）：\n")
            append(snapshots.take(10).joinToString("、") { it.version.id })
        }
        append("\n\n（安装时直接把这些版本号传给 install_game 的 game_version）")
    }
}

// ─────────────────────────────────────────────────────────────
//  安装执行器：静默后台 + 进度汇入顶部细进度条
// ─────────────────────────────────────────────────────────────

/**
 * 让 Agent 能发起真实安装的**唯一入口**。
 *
 * 与 `GameDownloadScreen` 里的做法保持一致：
 * - 安装本身跑在 [TaskSystem.appScope]，离开 AI 页面也会继续；
 * - 另外提交一个「镜像任务」，把 `GameInstaller` 的各子任务进度平均后写进全局任务队列，
 *   顶部那条细进度条就是用户能看到的唯一反馈（多条并行安装各占一条）；
 * - 结果（成功 / 失败原因）记在这里，供 `install_status` 回答模型。
 *
 * 议题 #78：这里原来是 `private var active: Active?` **单槽**，第二个 install_game 会被
 * 「已经有一个安装任务在进行中」挡掉。现在改成**按任务 id 索引的多会话表**：
 * 每个安装有自己的 [GameInstaller]（#78 已让它用独占临时目录）、自己的镜像任务、自己的取消链路；
 * 只有「同一个实例名」才拒绝 —— 那是真的会写同一个目标目录，不能并行。
 */
internal object GameInstallRunner {

    /** 一次进行中的安装（一个实例一个会话；在 [actives] 里的 key 就是镜像任务 id） */
    private class Active(
        val versionName: String,
        val gameVersion: String,
        val loader: String,
        val installer: GameInstaller,
        val startedAt: Long = System.currentTimeMillis(),
    )

    /**
     * 并行安装会话表。
     *
     * 用 [ConcurrentHashMap] 是因为写入来自两个地方：`install_game` 工具（主线程）与
     * 安装器的完成回调（TaskSystem 的后台线程）；读取也在工具线程上。
     * 用并发容器而不是 `synchronized(LinkedHashMap)`，避免回调线程拿锁时卡住工具。
     */
    private val actives = ConcurrentHashMap<String, Active>()

    /**
     * 镜像任务 id 的序号。
     *
     * id 用「前缀 + 序号 + 时间戳」：UI 的安装用 `install_game_ui_`，AI 用 `install_game_ai_`，
     * 两边前缀不同；同一进程内连续的 AI 安装也靠序号保证 id 唯一，
     * 否则 TaskSystem 的 `containsTask` 会**静默丢弃**同 id 的第二个任务。
     */
    private val sequence = AtomicInteger(0)

    @Volatile
    private var lastReport: String? = null

    @Volatile
    private var lastSuccess: Boolean? = null

    class Outcome(val ok: Boolean, val message: String)

    fun isRunning(): Boolean = actives.isNotEmpty()

    /**
     * 发起安装。返回给模型看的文本（已经开始的会说明「在后台继续」）。
     *
     * 可以同时发起多个：只有「同一个实例名」会被拒绝（会写同一个目标目录），
     * 其它情况（不同游戏版本 / 不同加载器 / 不同 name）都各自并行。
     */
    fun start(info: GameDownloadInfo, gameVersion: String, loader: String): Outcome {
        val name = info.customVersionName

        //同名实例已经在装：明确拒绝，不静默丢请求（模型可以换 name 再发起）
        actives.values.firstOrNull { it.versionName == name }?.let { running ->
            return Outcome(
                ok = false,
                message = "实例「$name」已经在安装中了（已用时 ${elapsedSeconds(running)} 秒），" +
                    "同一个实例不能同时装两遍。\n" +
                    "想同时装同一个游戏版本的多个实例，请用 name 参数给它们不同的名字。" +
                    "当前状态：\n" + describeStatus(),
            )
        }

        if (VersionsManager.isVersionExists(name) && !info.overwrite) {
            return Outcome(
                ok = false,
                message = "实例「$name」已经存在了。如果确实要覆盖重装，请再调用一次 install_game 并带上 overwrite=true；" +
                    "否则换一个 name。",
            )
        }

        val taskId = "install_game_ai_${sequence.incrementAndGet()}_${System.currentTimeMillis()}"
        val installer = GameInstaller(GlobalContext, info, TaskSystem.appScope)
        val item = Active(
            versionName = name,
            gameVersion = gameVersion,
            loader = loader,
            installer = installer,
        )
        actives[taskId] = item
        //⚠️ 这里**不要**清 lastReport / lastSuccess：多任务并行时，清掉会让「最近一次结果」
        //在发起另一个安装的瞬间凭空消失（用户刚装完 A 又让装 B，A 的结果还想查）。
        //新会话结束时 finish() 会把它覆盖成最新的那条。

        TaskSystem.submitTask(
            Task.runTask(
                id = taskId,
                task = { task ->
                    task.updateProgress(0f)
                    task.updateTitle(androidText(R.string.main_task_install_game, name))
                    //镜像「本会话」自己的安装器进度；会话结束时（完成/失败/取消）退出循环
                    while (true) {
                        val current = actives[taskId] ?: break
                        val list = current.installer.tasksFlow.value
                        if (list.isNotEmpty()) {
                            val average = list
                                .map { it.task.progress.value.coerceIn(0f, 1f) }
                                .average()
                                .toFloat()
                            task.updateProgress(average)
                            list.firstOrNull { it.task.stage.value.name == "RUNNING" }
                                ?.let { task.updateMessage(it.title) }
                        }
                        delay(400)
                    }
                    task.updateProgress(1f)
                },
                onCancel = {
                    //用户从顶部进度条上取消：必须把真正的安装也停掉，
                    //否则「界面说取消了、后台还在下」。取消会清掉半成品目录。
                    runCatching { item.installer.cancelInstall() }
                    finish(taskId, "安装「$name」被取消了（半成品目录已清理）。", false)
                },
                onFinally = {
                    //兜底：镜像任务结束但会话还没结算时必须收掉。
                    //「取消全部任务」走的是 TaskSystem.cancelChildren()，**不会**调用任务的 onCancel
                    //（只有单条 cancelTask 才会走）；安装器自己的 onCancelled 也不保证一定先到。
                    //漏掉就会让 install_status 永远列出「进行中」、isRunning() 永远 true。
                    //
                    //⚠️ 这里只结算会话，**不**再调 cancelInstall()：真实的取消链路
                    //（onCancel / 安装器 onCancelled）已经负责清理半成品；在这里补一刀反而可能
                    //在「安装刚成功、会话刚结算」的竞态里把装好的版本目录当成半成品删掉。
                    if (actives.containsKey(taskId)) {
                        finish(
                            taskId,
                            "安装「$name」的后台任务已结束（没收到完成回调，可能已被取消）。",
                            false,
                        )
                    }
                }
            )
        )

        try {
            installer.installGame(
                isRunning = {
                    //安装器自己已经在跑了（极少见：同一个 GameInstaller 被启动了两次）
                    finish(taskId, "实例「$name」的安装请求没有开始：这个安装器已经在跑别的任务了。", false)
                },
                onInstalled = { version ->
                    finish(taskId, "实例「$version」安装完成，已经可以直接启动。", true)
                    VersionsManager.refresh("[GameInstallRunner] onInstalled", version)
                },
                onError = { th ->
                    finish(taskId, "实例「$name」安装失败：" + describeError(th), false)
                },
                onGameAlreadyInstalled = {
                    finish(taskId, "实例「$name」已经存在，安装被跳过（不是出错，是本来就有）。", false)
                },
                onCancelled = {
                    //「取消全部任务」这类整体取消不会走镜像任务的 onCancel，
                    //这里必须把会话收干净，否则 install_status 会永远显示「正在安装」
                    finish(taskId, "安装「$name」被取消了（半成品目录已清理）。", false)
                }
            )
        } catch (th: Throwable) {
            //安装器在「启动阶段」直接抛异常（极端情况）：会话也必须结算，
            //否则镜像任务会一直挂着、这个实例名再也发不起新安装
            finish(taskId, "实例「$name」安装启动失败：" + describeError(th), false)
            throw th
        }

        val parallel = actives.size
        val message = buildString {
            append("已在后台开始安装「").append(name).append("」（Minecraft ")
                .append(gameVersion).append(" / ").append(loader).append("）。")
            if (parallel > 1) append("现在共有 ").append(parallel).append(" 个安装同时进行。")
            append("\n下载和安装是静默的，进度只显示在启动器顶部的那条细进度条上，不需要用户操作。\n")
            append("装完会自动出现在实例列表里。要查真实进度或结果，调用 install_status。")
            if (parallel > 1) {
                append("\n（若还要同时装别的版本，直接再调用 install_game、用不同的 name 即可。）")
            }
        }
        return Outcome(ok = true, message = message)
    }

    /**
     * 结束一个安装会话；同一个会话只结算一次。
     *
     * 完成 / 失败 / 取消（含「取消全部任务」）都可能触发，先到的那条生效；
     * 后到的调用直接返回，不会把已经写好的成功结果覆盖成「取消」。
     */
    private fun finish(taskId: String, report: String, success: Boolean) {
        if (actives.remove(taskId) == null) return
        lastReport = report
        lastSuccess = success
    }

    /** 给模型看的瘦状态文本 */
    fun describeStatus(): String = buildString {
        //按开始时间排序：先发起的排在前面，模型转述时顺序稳定
        val running = actives.values.sortedBy { it.startedAt }
        if (running.isEmpty()) {
            append(lastReport ?: "当前没有进行中的安装任务，也没有最近一次安装的记录。")
            when (lastSuccess) {
                true -> append("\n（这次安装是成功的）")
                false -> append("\n（这次安装没有成功）")
                null -> Unit
            }
        } else {
            append("进行中的安装共 ").append(running.size).append(" 个：\n")
            running.forEachIndexed { index, runningItem ->
                val list = runningItem.installer.tasksFlow.value
                val progress = if (list.isEmpty()) 0f
                else list.map { it.task.progress.value.coerceIn(0f, 1f) }.average().toFloat()
                val current = list.firstOrNull { it.task.stage.value.name == "RUNNING" }
                append(index + 1).append(") 「").append(runningItem.versionName)
                append("」（Minecraft ").append(runningItem.gameVersion)
                append(" / ").append(runningItem.loader).append("）")
                append(" 总进度 ").append((progress * 100).toInt()).append("%")
                if (current != null) {
                    append("，当前步骤：")
                    append(runCatching { current.title.toAndroidString(GlobalContext) }.getOrElse { "（未知）" })
                }
                append("，已用时 ").append(elapsedSeconds(runningItem)).append(" 秒\n")
            }
            lastReport?.let { append("最近一次结果：").append(it).append('\n') }
        }

        val installed = VersionsManager.versions.value.map { it.getVersionName() }
        append("\n当前已有实例：").append(if (installed.isEmpty()) "（无）" else installed.joinToString("、"))
    }

    /** 安装会话已用时（秒）*/
    private fun elapsedSeconds(item: Active): Long =
        (System.currentTimeMillis() - item.startedAt) / 1000

    private fun describeError(th: Throwable): String =
        th.message ?: th::class.simpleName ?: "未知错误"
}
