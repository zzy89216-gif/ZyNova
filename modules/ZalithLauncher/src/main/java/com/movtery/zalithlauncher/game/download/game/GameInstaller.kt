/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.game.download.game

import android.content.Context
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.download.InstallJournal
import com.movtery.zalithlauncher.game.download.InstallTargetRegistry
import com.movtery.zalithlauncher.game.download.engine.PathWriteLock
import com.movtery.zalithlauncher.coroutine.Task
import com.movtery.zalithlauncher.coroutine.TaskFlowExecutor
import com.movtery.zalithlauncher.coroutine.TitledTask
import com.movtery.zalithlauncher.coroutine.addTask
import com.movtery.zalithlauncher.coroutine.buildPhase
import com.movtery.zalithlauncher.game.addons.mirror.mapBMCLMirrorUrls
import com.movtery.zalithlauncher.game.addons.modloader.ModLoader
import com.movtery.zalithlauncher.game.addons.modloader.cleanroom.CleanroomVersion
import com.movtery.zalithlauncher.game.addons.modloader.fabriclike.FabricLikeVersion
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.ForgeLikeVersion
import com.movtery.zalithlauncher.game.addons.modloader.forgelike.neoforge.NeoForgeVersion
import com.movtery.zalithlauncher.game.addons.modloader.modlike.ModVersion
import com.movtery.zalithlauncher.game.download.assets.platform.mcim.mapMCIMMirrorUrls
import com.movtery.zalithlauncher.game.download.game.cleanroom.getCleanroomDownloadTask
import com.movtery.zalithlauncher.game.download.game.cleanroom.targetTempCleanroomInstaller
import com.movtery.zalithlauncher.game.download.game.fabric.getFabricLikeCompleterTask
import com.movtery.zalithlauncher.game.download.game.fabric.getFabricLikeDownloadTask
import com.movtery.zalithlauncher.game.download.game.forge.getForgeLikeAnalyseTask
import com.movtery.zalithlauncher.game.download.game.forge.getForgeLikeDownloadTask
import com.movtery.zalithlauncher.game.download.game.forge.getForgeLikeInstallTask
import com.movtery.zalithlauncher.game.download.game.forge.isNeoForge
import com.movtery.zalithlauncher.game.download.game.forge.targetTempForgeLikeInstaller
import com.movtery.zalithlauncher.game.download.game.optifine.getOptiFineDownloadTask
import com.movtery.zalithlauncher.game.download.game.optifine.getOptiFineInstallTask
import com.movtery.zalithlauncher.game.download.game.optifine.getOptiFineModsDownloadTask
import com.movtery.zalithlauncher.game.download.game.optifine.targetTempOptiFineInstaller
import com.movtery.zalithlauncher.game.path.getGameHome
import com.movtery.zalithlauncher.game.version.download.BaseMinecraftDownloader
import com.movtery.zalithlauncher.game.version.download.MinecraftDownloader
import com.movtery.zalithlauncher.game.version.installed.VersionConfig
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.ui.AndroidStringText
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.file.copyDirectoryContents
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.downloadFileFromSources
import com.movtery.zalithlauncher.utils.network.withSpeedReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File

private const val TAG = "GameInstaller"

/**
 * 在安装游戏前发现存在冲突的已安装版本，抛出这个异常
 */
private class GameAlreadyInstalledException : RuntimeException()

/**
 * 游戏安装器
 * @param context 用于获取任务描述信息
 * @param info 安装游戏所需要的信息，包括 Minecraft id、自定义版本名称、Addon 列表
 * @param scope 在有生命周期管理的scope中执行安装任务
 */
class GameInstaller(
    private val context: Context,
    private val info: GameDownloadInfo,
    private val scope: CoroutineScope
) {
    private val taskExecutor = TaskFlowExecutor(scope)
    val tasksFlow: StateFlow<List<TitledTask>> = taskExecutor.tasksFlow

    /**
     * 基础下载器
     */
    private val downloader = BaseMinecraftDownloader()

    /** 安装失败/取消的收尾只做一次（cancelInstall 与任务流取消可能都会触发） */
    private val interruptedInstallHandled = java.util.concurrent.atomic.AtomicBoolean(false)

    /** 是否已占用目标版本名（议题 #78：跨入口同名安装互斥，所有收尾路径都要释放） */
    private val targetReserved = java.util.concurrent.atomic.AtomicBoolean(false)

    /**
     * 占用目标版本名。
     * UI 游戏安装 / 整合包安装 / AI install_game 共用 [InstallTargetRegistry]，
     * 同名已有一个安装在进行时返回 false，由调用方给出可读错误，绝不静默继续。
     */
    private fun reserveTarget(): Boolean {
        if (targetReserved.get()) return true
        val ok = InstallTargetRegistry.reserve(
            targetVersionName = info.customVersionName,
            owner = "game:${info.gameVersion}:${info.customVersionName}"
        )
        if (ok) targetReserved.set(true)
        return ok
    }

    /** 释放目标版本名（幂等；成功/失败/取消/中断都必须走到） */
    private fun releaseTarget() {
        if (targetReserved.compareAndSet(true, false)) {
            InstallTargetRegistry.release(info.customVersionName)
        }
    }

    /**
     * 目标游戏客户端目录（缓存）
     * versions/<client-name>/...
     */
    private var targetClientDir: File? = null
    private val overrideClientJar: File get() = File(PathManager.DIR_CACHE, "override_${info.customVersionName}_jar")
    private val overrideClientJson: File get() = File(PathManager.DIR_CACHE, "override_${info.customVersionName}_json")

    /**
     * 目标游戏目录
     */
    private val targetGameFolder: File = File(getGameHome())

    /**
     * 本次安装独占的临时游戏目录（议题 #78）
     *
     * 以前所有安装共用 [PathManager.DIR_CACHE_GAME_DOWNLOADER] 一个目录，
     * 每次安装开头的清理会把另一个**并行安装**正在用的临时文件一起删掉，
     * 表现就是同时装两个本体时其中一个总是失败/文件校验不过。
     * 现在每个安装实例在 root 下用独立子目录，清理时只动自己的。
     */
    private val tempGameRoot: File = File(
        PathManager.DIR_CACHE_GAME_DOWNLOADER,
        buildTempDirName()
    )

    private fun buildTempDirName(): String {
        //⚠️ 不能只用随机名：议题 #35 的「失败重试保留 libraries 不重下」依赖同一个安装每次用同一目录。
        //版本名本身就会作为目标版本目录名使用，所以可以直接当目录名；再拼 hash 兜底同名碰撞。
        //并行安装不同版本 → 版本名不同 → 目录不同，仍然互不影响。
        val safeName = info.customVersionName
            .replace(Regex("[\\\\/:*?\"<>|\\s]"), "_")
            .take(32)
            .ifBlank { "install" }
        return "${safeName}_${Integer.toHexString(info.customVersionName.hashCode())}"
    }

    /**
     * 安装 Minecraft 游戏
     * @param isRunning 正在运行中，阻止此次安装时
     * @param onInstalled 游戏已完成安装
     * @param onError 游戏安装失败
     * @param onGameAlreadyInstalled 在安装游戏前发现存在冲突的已安装版本
     */
    fun installGame(
        isRunning: () -> Unit = {},
        onInstalled: (version: String) -> Unit,
        onError: (th: Throwable) -> Unit,
        onGameAlreadyInstalled: () -> Unit,
        /**
         * 安装被取消（用户从任务面板取消 / 「取消全部任务」）时回调。
         * 界面必须靠它把「安装中」状态复位，否则按钮会永远停在安装中（议题 #72/#73）。
         */
        onCancelled: () -> Unit = {}
    ) {
        if (taskExecutor.isRunning()) {
            //正在安装中，阻止这次安装请求
            isRunning()
            return
        }

        //议题 #78：先占目标版本名，再检查「是否已安装」。
        //顺序不能反：两个入口同时装同一个新版本时，若都先通过「未安装」检查再各自开始，
        //就会一起写 versions/<同名>/。占用失败要给可读错误，不能静默丢弃。
        if (!reserveTarget()) {
            onError(IllegalStateException(context.getString(R.string.download_install_target_busy, info.customVersionName)))
            return
        }

        //目标版本已经存在：安装不会真正发生，绝不能继续走 onComplete（否则会误报安装成功）
        val alreadyInstalled = java.util.concurrent.atomic.AtomicBoolean(false)
        taskExecutor.executePhasesAsync(
            onStart = {
                val tasks = try {
                    getTaskPhase()
                } catch (_: GameAlreadyInstalledException) {
                    alreadyInstalled.set(true)
                    onGameAlreadyInstalled()
                    return@executePhasesAsync
                }
                taskExecutor.addPhases(tasks)
            },
            onComplete = {
                //先释放目标版本名：成功路径也不能一直占着，否则同名词后续安装永远被拒
                releaseTarget()
                if (alreadyInstalled.get()) return@executePhasesAsync
                if (info.overwrite) {
                    clearBackupFiles()
                }
                InstallJournal.clearGameInstall(info.customVersionName)
                onInstalled(info.customVersionName)
            },
            onError = {
                releaseTarget()
                //安装失败也要把半成品收拾掉：新装删掉半成品版本目录，
                //覆盖/更新则尽量把旧 json/jar 放回去（议题 #73）
                finishInterruptedInstall(clearTarget = true)
                onError(it)
            },
            onCancel = {
                releaseTarget()
                //「取消全部任务」这类整体取消不会经过 cancelInstall()：这里也要做同样的清理
                finishInterruptedInstall(clearTarget = true)
                onCancelled()
            }
        )
    }

    /**
     * 更新加载器
     * @param isRunning 正在运行中，阻止此次安装时
     * @param onInstalled 加载器已完成安装
     * @param onError 加载器安装失败
     */
    fun updateLoader(
        isRunning: () -> Unit = {},
        onInstalled: () -> Unit,
        onError: (th: Throwable) -> Unit
    ) {
        if (taskExecutor.isRunning()) {
            //正在安装中，阻止这次安装请求
            isRunning()
            return
        }
        //议题 #78：更新加载器同样写 versions/<同名>，必须和别的入口互斥
        if (!reserveTarget()) {
            onError(IllegalStateException(context.getString(R.string.download_install_target_busy, info.customVersionName)))
            return
        }

        taskExecutor.executePhasesAsync(
            onStart = {
                val tasks = getUpdateLoaderTaskPhase()
                taskExecutor.addPhases(tasks)
            },
            onComplete = {
                releaseTarget()
                if (info.overwrite) {
                    clearBackupFiles()
                }
                InstallJournal.clearGameInstall(info.customVersionName)
                onInstalled()
            },
            onError = {
                releaseTarget()
                //更新加载器失败：把旧 json/jar 恢复回去，避免出现读不出来的版本
                finishInterruptedInstall(clearTarget = false)
                onError(it)
            },
            onCancel = {
                releaseTarget()
                finishInterruptedInstall(clearTarget = false)
            }
        )
    }

    /**
     * 安装过程中所需的所有文件路径配置
     */
    private class InstallationPathConfig(
        val targetClientDir: File,
        val tempGameDir: File,
        val tempMinecraftDir: File,
        val tempGameVersionsDir: File,
        val tempClientDir: File,
        val tempModsDir: File,
        val optifineDir: File?,
        val forgeDir: File?,
        val neoforgeDir: File?,
        val fabricDir: File?,
        val legacyFabricDir: File?,
        val quiltDir: File?,
        val cleanroomDir: File?
    )

    /**
     * 构建安装过程中使用的所有路径配置
     */
    private fun createPathConfig(checkTargetVersion: Boolean): InstallationPathConfig {
        //目标版本目录
        val targetClientDir1 = VersionsManager.getVersionPath(info.customVersionName)
        targetClientDir = targetClientDir1
        val targetVersionJson = File(targetClientDir1, "${info.customVersionName}.json")
        val targetVersionJar = File(targetClientDir1, "${info.customVersionName}.jar")

        //目标版本已经安装的情况，非覆盖模式将退出
        if (!info.overwrite && checkTargetVersion && targetVersionJson.exists()) {
            Logger.debug(TAG, "The game has already been installed!")
            throw GameAlreadyInstalledException()
        }

        //议题 #73：安装开始前把关键状态落盘，进程被杀后下次启动才能判断「上次没装完」。
        //新装：目标目录是本次新建的，恢复时直接删掉半成品；
        //覆盖安装 / 更新加载器：旧 json/jar 先备份到私有目录，恢复时放回去。
        //目标版本原来不存在（没有 json）→ 目录是这次安装新建的，恢复时可以直接删；
        //原来存在（覆盖安装 / 更新加载器）→ 只能恢复旧 json/jar 备份，不能删目录里用户自己的文件
        val targetVersionAlreadyExists = targetVersionJson.exists()
        val backupPaths: Pair<String?, String?> = if (!targetVersionAlreadyExists) {
            null to null
        } else {
            InstallJournal.backupVersionFiles(
                targetJson = targetVersionJson,
                targetJar = targetVersionJar
            )
        }
        InstallJournal.beginGameInstall(
            targetVersionName = info.customVersionName,
            overwrite = info.overwrite,
            deleteTargetOnRecovery = !targetVersionAlreadyExists,
            backupJsonPath = backupPaths.first,
            backupJarPath = backupPaths.second
        )

        //如果是覆盖安装，将清除目标版本Json和Jar
        if (info.overwrite) {
            runCatching {
                targetVersionJson.takeIf { it.exists() }?.let {
                    overrideClientJson.delete()
                    it.copyTo(overrideClientJson)
                    it.delete()
                }
                targetVersionJar.takeIf { it.exists() }?.let {
                    overrideClientJar.delete()
                    it.copyTo(overrideClientJar)
                    it.delete()
                }
            }.onFailure {
                //无法正常备份，只能硬着头皮干！
                FileUtils.deleteQuietly(overrideClientJson)
                FileUtils.deleteQuietly(overrideClientJar)
                Logger.warning(TAG, "Backup failed, will proceed with overwrite installation directly!", it)
            }
        }

        //议题 #78：用本次安装独占的临时目录，避免并行安装互相清理
        val tempGameDir = tempGameRoot
        val tempMinecraftDir = File(tempGameDir, ".minecraft")
        val tempGameVersionsDir = File(tempMinecraftDir, "versions")
        val tempClientDir = File(tempGameVersionsDir, info.gameVersion)

        //ModLoader临时目录
        val optifineDir = info.optifine?.let { File(tempGameVersionsDir, it.version) }
        val forgeDir = info.forge?.let { File(tempGameVersionsDir, "forge-${it.versionName}") }
        val neoforgeDir = info.neoforge?.let { File(tempGameVersionsDir, "neoforge-${it.versionName}") }
        val fabricDir = info.fabric?.let { File(tempGameVersionsDir, "fabric-loader-${it.version}-${info.gameVersion}") }
        val legacyFabricDir = info.legacyFabric?.let { File(tempGameVersionsDir, "legacy-fabric-loader-${it.version}-${info.gameVersion}") }
        val quiltDir = info.quilt?.let { File(tempGameVersionsDir, "quilt-loader-${it.version}-${info.gameVersion}") }
        val cleanroomDir = info.cleanroom?.let { File(tempGameVersionsDir, "cleanroom-${it.version}-${info.gameVersion}") }

        //Mods临时目录
        val tempModsDir = File(tempGameDir, ".temp_mods")

        return InstallationPathConfig(
            targetClientDir = targetClientDir1,
            tempGameDir = tempGameDir,
            tempMinecraftDir = tempMinecraftDir,
            tempGameVersionsDir = tempGameVersionsDir,
            tempClientDir = tempClientDir,
            tempModsDir = tempModsDir,
            optifineDir = optifineDir,
            forgeDir = forgeDir,
            neoforgeDir = neoforgeDir,
            fabricDir = fabricDir,
            legacyFabricDir = legacyFabricDir,
            quiltDir = quiltDir,
            cleanroomDir = cleanroomDir
        )
    }

    /**
     * 获取安装 Minecraft 游戏的任务流阶段
     * @param onInstalled 游戏已完成安装
     */
    suspend fun getTaskPhase(
        createIsolation: Boolean = true,
        onInstalled: suspend (targetClientDir: File) -> Unit = {},
    ): List<TaskFlowExecutor.TaskPhase> = withContext(Dispatchers.IO) {
        val pathConfig = createPathConfig(checkTargetVersion = true)

        listOf(
            buildPhase {
                //开始之前，应该先清理一次临时游戏目录，否则可能会影响安装结果
                addTask(
                    id = "Download.Game.ClearTemp",
                    title = androidText(R.string.download_install_clear_temp),
                    icon = R.drawable.ic_auto_delete_outlined,
                ) {
                    clearTempGameDir()
                    //清理完成缓存目录后，创建新的缓存目录
                    pathConfig.tempClientDir.createDirAndLog()
                    pathConfig.optifineDir?.createDirAndLog()
                    pathConfig.forgeDir?.createDirAndLog()
                    pathConfig.neoforgeDir?.createDirAndLog()
                    pathConfig.fabricDir?.createDirAndLog()
                    pathConfig.legacyFabricDir?.createDirAndLog()
                    pathConfig.quiltDir?.createDirAndLog()
                    pathConfig.cleanroomDir?.createDirAndLog()
                    pathConfig.tempModsDir.createDirAndLog()
                }

                //下载安装原版
                addTask(
                    title = androidText(R.string.download_game_install_vanilla, info.gameVersion),
                    task = createMinecraftDownloadTask(info.gameVersion, pathConfig.tempGameVersionsDir)
                )

                //下载加载器/模组
                addLoaderTasks(
                    tempGameDir = pathConfig.tempGameDir,
                    tempMinecraftDir = pathConfig.tempMinecraftDir,
                    forgeDir = pathConfig.forgeDir,
                    neoforgeDir = pathConfig.neoforgeDir,
                    fabricDir = pathConfig.fabricDir,
                    legacyFabricDir = pathConfig.legacyFabricDir,
                    quiltDir = pathConfig.quiltDir,
                    cleanroomDir = pathConfig.cleanroomDir,
                    tempModsDir = pathConfig.tempModsDir
                )

                //最终游戏安装任务
                addTask(
                    title = androidText(R.string.download_game_install_game_files_progress),
                    icon = R.drawable.ic_build_outlined,
                    //如果有非原版以外的任务，则需要进行处理安装（合并版本Json、迁移文件等）
                    task = if (
                        pathConfig.optifineDir != null ||
                        pathConfig.forgeDir != null ||
                        pathConfig.neoforgeDir != null ||
                        pathConfig.fabricDir != null ||
                        pathConfig.legacyFabricDir != null ||
                        pathConfig.quiltDir != null ||
                        pathConfig.cleanroomDir != null ||
                        pathConfig.tempModsDir.listFiles()?.isNotEmpty() == true
                    ) {
                        createGameInstalledTask(
                            tempMinecraftDir = pathConfig.tempMinecraftDir,
                            targetMinecraftDir = targetGameFolder,
                            targetClientDir = pathConfig.targetClientDir,
                            tempClientDir = pathConfig.tempClientDir,
                            tempModsDir = pathConfig.tempModsDir,
                            createIsolation = createIsolation,
                            optiFineFolder = pathConfig.optifineDir,
                            forgeFolder = pathConfig.forgeDir,
                            neoForgeFolder = pathConfig.neoforgeDir,
                            fabricFolder = pathConfig.fabricDir,
                            legacyFabricFolder = pathConfig.legacyFabricDir,
                            quiltFolder = pathConfig.quiltDir,
                            cleanroomFolder = pathConfig.cleanroomDir,
                            onComplete = {
                                onInstalled(pathConfig.targetClientDir)
                                //全部文件已经落到目标版本目录：本次安装成功，清掉落盘记录
                                InstallJournal.clearGameInstall(info.customVersionName)
                                targetClientDir = null
                            }
                        )
                    } else {
                        //仅仅下载了原版，只复制版本client文件
                        createVanillaFilesCopyTask(
                            tempMinecraftDir = pathConfig.tempMinecraftDir,
                            onComplete = {
                                onInstalled(pathConfig.targetClientDir)
                                InstallJournal.clearGameInstall(info.customVersionName)
                                targetClientDir = null
                            }
                        )
                    }
                )
            }
        )
    }

    /**
     * 获取安装加载器更新的任务流阶段
     */
    private suspend fun getUpdateLoaderTaskPhase(
        onInstalled: suspend () -> Unit = {},
    ): List<TaskFlowExecutor.TaskPhase> = withContext(Dispatchers.IO) {
        val pathConfig = createPathConfig(checkTargetVersion = false)

        listOf(
            buildPhase {
                //开始之前，应该先清理一次临时游戏目录，否则可能会影响安装结果
                addTask(
                    id = "UpdateLoader.ClearTemp",
                    title = androidText(R.string.download_install_clear_temp),
                    icon = R.drawable.ic_auto_delete_outlined,
                ) {
                    clearTempGameDir()
                    //清理完成缓存目录后，创建新的缓存目录
                    pathConfig.tempClientDir.createDirAndLog()
                    pathConfig.optifineDir?.createDirAndLog()
                    pathConfig.forgeDir?.createDirAndLog()
                    pathConfig.neoforgeDir?.createDirAndLog()
                    pathConfig.fabricDir?.createDirAndLog()
                    pathConfig.legacyFabricDir?.createDirAndLog()
                    pathConfig.quiltDir?.createDirAndLog()
                    pathConfig.cleanroomDir?.createDirAndLog()
                    pathConfig.tempModsDir.createDirAndLog()
                }

                //下载原版的 Json/Jar，后续需要基于这个进行合并
                addTask(
                    id = "UpdateLoader.DownloadVanilla",
                    title = androidText(R.string.download_game_install_base_download_file2, info.gameVersion)
                ) { task ->
                    val clientVersion = info.gameVersion
                    val mcFolder = pathConfig.tempGameVersionsDir

                    //下载原版 Json
                    task.updateProgress(-1f)
                    val manifest = downloader.findVersion(clientVersion)?.let {
                        downloader.createVersionJson(it, clientVersion, mcFolder)
                    } ?: error("Version not found: $clientVersion")

                    //下载原版 Jar
                    val tempJarFile = downloader.getVersionJarPath(clientVersion, mcFolder)
                    manifest.downloads?.client?.let { client ->
                        val urls = client.url.mapBMCLMirrorUrls()
                        val sizeConfig = object {
                            val totalSize = client.size
                            var downloadedSize: Long = 0L
                        }
                        //开始下载
                        withSpeedReport(
                            onSpeedReport = { bytes ->
                                task.updateSpeed(bytes)
                            },
                            onClear = {
                                task.clearSpeed()
                            }
                        ) { report ->
                            downloadFileFromSources(
                                urls = urls,
                                outputFile = tempJarFile,
                                sizeCallback = { downloaded ->
                                    sizeConfig.downloadedSize += downloaded
                                    task.updateProgress(
                                        (sizeConfig.downloadedSize.toFloat() / sizeConfig.totalSize.toFloat())
                                            .coerceIn(0f, 1f)
                                    )
                                    report(downloaded)
                                }
                            )
                        }
                    } ?: run {
                        //如果未提供下载方式，则很可能是需要复制原版的Jar文件
                        val clientFile = downloader.getVersionJarPath(clientVersion, downloader.versionsTarget)
                        if (clientFile.exists()) {
                            clientFile.copyTo(tempJarFile)
                        } else {
                            error("Unable to cache the vanilla Jar file: $clientVersion")
                        }
                    }

                    task.updateProgress(1f)
                }

                //下载加载器/模组
                addLoaderTasks(
                    tempGameDir = pathConfig.tempGameDir,
                    tempMinecraftDir = pathConfig.tempMinecraftDir,
                    forgeDir = pathConfig.forgeDir,
                    neoforgeDir = pathConfig.neoforgeDir,
                    fabricDir = pathConfig.fabricDir,
                    legacyFabricDir = pathConfig.legacyFabricDir,
                    quiltDir = pathConfig.quiltDir,
                    cleanroomDir = pathConfig.cleanroomDir,
                    tempModsDir = pathConfig.tempModsDir
                )

                //最终游戏安装任务
                addTask(
                    title = androidText(R.string.download_game_install_game_files_progress),
                    icon = R.drawable.ic_build_outlined,
                    task = createGameInstalledTask(
                        tempMinecraftDir = pathConfig.tempMinecraftDir,
                        targetMinecraftDir = targetGameFolder,
                        targetClientDir = pathConfig.targetClientDir,
                        tempClientDir = pathConfig.tempClientDir,
                        tempModsDir = pathConfig.tempModsDir,
                        createIsolation = false, //这个安装流程针对的是已有的版本，所以不应该重新创建
                        optiFineFolder = pathConfig.optifineDir,
                        forgeFolder = pathConfig.forgeDir,
                        neoForgeFolder = pathConfig.neoforgeDir,
                        fabricFolder = pathConfig.fabricDir,
                        legacyFabricFolder = pathConfig.legacyFabricDir,
                        quiltFolder = pathConfig.quiltDir,
                        cleanroomFolder = pathConfig.cleanroomDir,
                        onComplete = {
                            onInstalled()
                            InstallJournal.clearGameInstall(info.customVersionName)
                            targetClientDir = null
                        }
                    )
                )
            }
        )
    }

    private fun MutableList<TitledTask>.addLoaderTasks(
        tempGameDir: File,
        tempMinecraftDir: File,
        forgeDir: File?,
        neoforgeDir: File?,
        fabricDir: File?,
        legacyFabricDir: File?,
        quiltDir: File?,
        cleanroomDir: File?,
        tempModsDir: File
    ) {
        // OptiFine 安装
        info.optifine?.let { optifineVersion ->
            if (forgeDir == null && fabricDir == null) {
                val isNewVersion: Boolean = optifineVersion.inherit.contains("w") || optifineVersion.inherit.split(".")[1].toInt() >= 14
                val targetInstaller: File = targetTempOptiFineInstaller(tempGameDir, tempMinecraftDir, optifineVersion.fileName, isNewVersion)

                //将OptiFine作为版本下载，其余情况则作为Mod下载
                addTask(
                    title = androidText(
                        R.string.download_game_install_base_download_file,
                        ModLoader.OPTIFINE.displayName,
                        info.optifine.displayName
                    ),
                    task = getOptiFineDownloadTask(
                        targetTempInstaller = targetInstaller,
                        optifine = optifineVersion
                    )
                )

                //安装 OptiFine
                addTask(
                    title = androidText(
                        R.string.download_game_install_base_install,
                        ModLoader.OPTIFINE.displayName
                    ),
                    icon = R.drawable.ic_build_outlined,
                    task = getOptiFineInstallTask(
                        tempGameDir = tempGameDir,
                        tempMinecraftDir = tempMinecraftDir,
                        tempInstallerJar = targetInstaller,
                        isNewVersion = isNewVersion,
                        optifineVersion = optifineVersion
                    )
                )
            } else {
                //仅作为Mod进行下载
                addTask(
                    title = androidText(
                        R.string.download_game_install_base_download_file,
                        ModLoader.OPTIFINE.displayName,
                        info.optifine.displayName
                    ),
                    task = getOptiFineModsDownloadTask(
                        optifine = optifineVersion,
                        tempModsDir = tempModsDir
                    )
                )
            }
        }

        // Forge 安装
        info.forge?.let { forgeVersion ->
            createForgeLikeTask(
                forgeLikeVersion = forgeVersion,
                tempGameDir = tempGameDir,
                tempMinecraftDir = tempMinecraftDir,
                tempFolderName = forgeDir!!.name,
                addTask = { title, icon, task ->
                    addTask(title = title, icon = icon, task = task)
                }
            )
        }

        // NeoForge 安装
        info.neoforge?.let { neoforgeVersion ->
            createForgeLikeTask(
                forgeLikeVersion = neoforgeVersion,
                tempGameDir = tempGameDir,
                tempMinecraftDir = tempMinecraftDir,
                tempFolderName = neoforgeDir!!.name,
                addTask = { title, icon, task ->
                    addTask(title = title, icon = icon, task = task)
                }
            )
        }

        fun addFabricLike(
            version: FabricLikeVersion,
            dirName: String
        ) {
            createFabricLikeTask(
                fabricLikeVersion = version,
                tempMinecraftDir = tempMinecraftDir,
                tempFolderName = dirName,
                addTask = { title, icon, task ->
                    addTask(title = title, icon = icon, task = task)
                }
            )
        }

        fun addMod(
            mod: ModVersion,
            modName: String,
            modVer: String,
            isFabricApi: Boolean = false,
        ) {
            //同一实例里已经装有同名文件时不再重复下载。
            //例如更新加载器时又一次自动选中了 Fabric API，最终复制阶段本来也会跳过已有文件，
            //以前却会先把它完整下载一遍。
            val targetModsDir = targetClientDir?.let { VersionFolders.MOD.getDir(it) }
            if (targetModsDir != null && File(targetModsDir, mod.file.fileName).exists()) {
                Logger.info(TAG, "$modName $modVer is already installed; skip downloading")
                return
            }

            //Fabric API 防呆：实例里只要已经存在任意版本的 Fabric API（文件名以 fabric-api 开头），
            //就不再安装第二份，哪怕这次自动选中的是另一个版本。
            if (isFabricApi && targetModsDir != null) {
                val installed = targetModsDir.listFiles()?.firstOrNull { file ->
                    file.isFile && file.name.lowercase().startsWith("fabric-api")
                }
                if (installed != null) {
                    Logger.info(
                        TAG,
                        "$modName is already installed as ${installed.name}; skip downloading $modVer"
                    )
                    return
                }
            }

            addTask(
                title = androidText(
                    R.string.download_game_install_base_download_file,
                    modName, modVer
                ),
                task = createModLikeDownloadTask(
                    tempModsDir = tempModsDir,
                    modVersion = mod
                )
            )
        }

        // Fabric 安装
        info.fabric?.let { fabricVersion ->
            addFabricLike(fabricVersion, fabricDir!!.name)
        }
        info.fabricAPI?.let { apiVersion ->
            addMod(
                mod = apiVersion,
                modName = ModLoader.FABRIC_API.displayName,
                modVer = apiVersion.displayName,
                isFabricApi = true
            )
        }

        // Legacy Fabric 安装
        info.legacyFabric?.let { fabricVersion ->
            addFabricLike(fabricVersion, legacyFabricDir!!.name)
        }
        info.legacyFabricAPI?.let { apiVersion ->
            addMod(
                mod = apiVersion,
                modName = ModLoader.LEGACY_FABRIC_API.displayName,
                modVer = apiVersion.displayName
            )
        }

        // Quilt 安装
        info.quilt?.let { quiltVersion ->
            addFabricLike(quiltVersion, quiltDir!!.name)
        }
        info.quiltAPI?.let { apiVersion ->
            addMod(
                mod = apiVersion,
                modName = ModLoader.QUILT_API.displayName,
                modVer = apiVersion.displayName
            )
        }

        // Cleanroom 安装
        info.cleanroom?.let { cleanroomVersion ->
            createCleanroomTask(
                cleanroomVersion = cleanroomVersion,
                tempGameDir = tempGameDir,
                tempMinecraftDir = tempMinecraftDir,
                tempFolderName = cleanroomDir!!.name,
                addTask = { title, icon, task ->
                    addTask(title = title, icon = icon, task = task)
                }
            )
        }
    }

    fun cancelInstall(
        clearTarget: Boolean = true
    ) {
        taskExecutor.cancel()
        //取消通常来自界面线程：清理/回滚可能涉及删除整个版本目录，必须放到 IO，
        //否则大目录会在主线程删到卡死（长按取消 → ANR）。记录最后才清，中途被杀下次启动还能恢复。
        CoroutineScope(Dispatchers.IO).launch {
            finishInterruptedInstall(clearTarget)
        }

        //⚠️ 这里不再全局 stopService / 停 JVMSocketServer（议题 #78）：
        //JvmService / JVMSocketServer 是进程级单例，并行安装时一个取消会把另一个正在跑的
        //JVM 安装阶段一起打断。现在 JVM 服务生命周期收在 `runJvmRetryRuntimes` 的
        //InstallerJvmGate 临界区里（见 GameJVMRunner.startJvmServiceAndWaitExit 的 finally），
        //取消本安装会通过协程取消传播到该阶段，只收掉自己手里的服务。
    }

    /**
     * 安装失败/被取消后的统一收尾（幂等，只执行一次）
     *
     * - 覆盖安装：把旧 json/jar 恢复回去（私有目录备份优先，cache 备份兜底）
     * - 新装：删掉半成品版本目录
     * - 更新加载器（clearTarget=false）：只恢复旧文件，不删整个版本目录
     *
     * 清理完成后才清落魄盘记录，避免「记录没了、目录没删完」时再被杀一次没人接着收拾。
     */
    private fun finishInterruptedInstall(clearTarget: Boolean) {
        if (!interruptedInstallHandled.compareAndSet(false, true)) return
        if (!info.overwrite) {
            //新装：rollback 会删掉半成品目录；更新加载器：只会恢复备份
            val result = InstallJournal.rollbackInterruptedGameInstall(info.customVersionName)
            if (clearTarget && result == null) {
                //没有落盘记录（例如记录写入失败）时按目标目录兜底删除
                clearTargetClient()
            }
        } else {
            //覆盖安装：先按持久备份恢复，revertClientDir 再用 cache 备份兜底
            InstallJournal.rollbackInterruptedGameInstall(info.customVersionName)
            revertClientDir()
        }
    }

    /**
     * 清除临时游戏目录
     */
    private suspend fun clearTempGameDir() = withContext(Dispatchers.IO) {
        //议题 #78：只清理本次安装自己的临时目录，不能碰其它并行安装的
        val folder = tempGameRoot.takeIf { it.exists() } ?: return@withContext
        //议题 #35：开始安装时不再把整目录删掉。
        //NeoForge / Forge 的支持库就下在 temp_game/.minecraft/libraries，
        //以前一失败重试就全部重下；现在保留 libraries（下次会先做 sha1 校验再复用），
        //只清理其余临时文件。
        val minecraftDir = File(folder, ".minecraft")
        if (!minecraftDir.isDirectory) {
            FileUtils.deleteQuietly(folder)
            Logger.info(TAG, "Temporary game directory cleared.")
            return@withContext
        }
        minecraftDir.listFiles()?.forEach { child ->
            if (!child.name.equals("libraries", ignoreCase = true)) {
                FileUtils.deleteQuietly(child)
            }
        }
        folder.listFiles()?.forEach { child ->
            if (child != minecraftDir) FileUtils.deleteQuietly(child)
        }
        Logger.info(TAG, "Temporary game directory cleared (libraries kept for resume).")
    }

    /**
     * 安装失败、取消安装时，都应该清除目标客户端版本文件夹
     */
    private fun clearTargetClient() {
        val dirToDelete = targetClientDir //临时变量
        targetClientDir = null

        CoroutineScope(Dispatchers.IO).launch {
//            clearTempGameDir() 考虑到用户可能操作快，双线程清理同一个文件夹可能导致一些问题
            dirToDelete?.let {
                //直接清除上一次安装的目标目录
                FileUtils.deleteQuietly(it)
                Logger.info(TAG, "Successfully deleted version directory: ${it.name} at path: ${it.absolutePath}")
            }
            //目录已经删掉，记录可以安全清除
            InstallJournal.clearGameInstall(info.customVersionName)
        }
    }

    private fun clearBackupFiles() {
        CoroutineScope(Dispatchers.IO).launch {
            FileUtils.deleteQuietly(overrideClientJson)
            FileUtils.deleteQuietly(overrideClientJar)
        }
    }

    private fun revertClientDir() {
        val targetDir = targetClientDir ?: return

        val targetJson = File(targetDir, "${info.customVersionName}.json")
        val targetJar = File(targetDir, "${info.customVersionName}.jar")

        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                if (overrideClientJson.exists()) {
                    FileUtils.deleteQuietly(targetJson)
                    FileUtils.moveFile(overrideClientJson, targetJson)
                }
                if (overrideClientJar.exists()) {
                    FileUtils.deleteQuietly(targetJar)
                    FileUtils.moveFile(overrideClientJar, targetJar)
                }
            }.onFailure { e ->
                Logger.error(TAG, "Failed to revert client files: ${e.message}", e)
            }
            //旧文件已经放回（或确认放不回去），记录可以清除
            InstallJournal.clearGameInstall(info.customVersionName)
        }
    }

    /**
     * 获取下载原版 Task
     */
    private fun createMinecraftDownloadTask(
        tempClientName: String,
        tempVersionsDir: File
    ): Task {
        val mcDownloader = MinecraftDownloader(
            context = context,
            version = info.gameVersion,
            customName = info.customVersionName,
            downloader = downloader,
            onThrowable = { throw it }
        )

        return mcDownloader.getDownloadTask(tempClientName, tempVersionsDir)
    }

    /**
     * @param tempFolderName 临时ModLoader版本文件夹名称
     */
    private fun createForgeLikeTask(
        forgeLikeVersion: ForgeLikeVersion,
        loaderVersion: String = forgeLikeVersion.versionName,
        tempGameDir: File,
        tempMinecraftDir: File,
        tempFolderName: String,
        addTask: (title: AndroidStringText, icon: Int?, task: Task) -> Unit
    ) {
        //类似 1.19.3-41.2.8 格式，优先使用 Version 中要求的版本而非 Inherit（例如 1.19.3 却使用了 1.19 的 Forge）
        val (processedInherit, processedLoaderVersion) =
            if (
                !forgeLikeVersion.isNeoForge && loaderVersion.startsWith("1.") && loaderVersion.contains("-")
            ) {
                loaderVersion.substringBefore("-") to loaderVersion.substringAfter("-")
            } else {
                forgeLikeVersion.inherit to loaderVersion
            }

        val tempInstaller = targetTempForgeLikeInstaller(tempGameDir)
        //下载安装器
        addTask(
            androidText(
                R.string.download_game_install_base_download_file,
                forgeLikeVersion.loaderName,
                processedLoaderVersion
            ),
            null,
            getForgeLikeDownloadTask(tempInstaller, forgeLikeVersion)
        )
        //分析与安装
        val isNew = forgeLikeVersion is NeoForgeVersion || !forgeLikeVersion.isLegacy

        if (isNew) {
            addTask(
                androidText(
                    R.string.download_game_install_forgelike_analyse,
                    forgeLikeVersion.loaderName
                ),
                R.drawable.ic_build_outlined,
                getForgeLikeAnalyseTask(
                    downloader = downloader,
                    targetTempInstaller = tempInstaller,
                    removeFromDownload = "${forgeLikeVersion.loaderName.lowercase()}-$processedInherit-$loaderVersion",
                    tempMinecraftFolder = tempMinecraftDir,
                    sourceInherit = info.gameVersion,
                    processedInherit = processedInherit,
                )
            )
        }

        addTask(
            androidText(
                R.string.download_game_install_base_install,
                forgeLikeVersion.loaderName
            ),
            R.drawable.ic_build_outlined,
            getForgeLikeInstallTask(
                isNew = isNew,
                downloader = downloader,
                loaderName = forgeLikeVersion.loaderName,
                tempFolderName = tempFolderName,
                tempInstaller = tempInstaller,
                tempGameFolder = tempGameDir,
                tempMinecraftDir = tempMinecraftDir,
                inherit = processedInherit
            )
        )
    }

    private fun createFabricLikeTask(
        fabricLikeVersion: FabricLikeVersion,
        tempMinecraftDir: File,
        tempFolderName: String,
        addTask: (title: AndroidStringText, icon: Int?, task: Task) -> Unit
    ) {
        val tempVersionJson = File(tempMinecraftDir, "versions/$tempFolderName/$tempFolderName.json")

        //下载 Json
        addTask(
            androidText(
                R.string.download_game_install_base_download_file,
                fabricLikeVersion.loaderName,
                fabricLikeVersion.version
            ),
            null,
            getFabricLikeDownloadTask(
                fabricLikeVersion = fabricLikeVersion,
                tempVersionJson = tempVersionJson
            )
        )

        //补全游戏库
        addTask(
            androidText(
                R.string.download_game_install_forgelike_analyse,
                fabricLikeVersion.loaderName
            ),
            null,
            getFabricLikeCompleterTask(
                downloader = downloader,
                tempMinecraftDir = tempMinecraftDir,
                tempVersionJson = tempVersionJson
            )
        )
    }

    private fun createCleanroomTask(
        cleanroomVersion: CleanroomVersion,
        tempGameDir: File,
        tempMinecraftDir: File,
        tempFolderName: String,
        addTask: (title: AndroidStringText, icon: Int?, task: Task) -> Unit
    ) {
        val tempInstaller = targetTempCleanroomInstaller(tempGameDir)
        //下载安装器
        addTask(
            androidText(
                R.string.download_game_install_base_download_file,
                ModLoader.CLEANROOM.displayName,
                cleanroomVersion.version
            ),
            null,
            getCleanroomDownloadTask(tempInstaller, cleanroomVersion)
        )

        //以新Forge安装器的方式进行安装
        addTask(
            androidText(
                R.string.download_game_install_forgelike_analyse,
                cleanroomVersion.version
            ),
            R.drawable.ic_build_outlined,
            getForgeLikeAnalyseTask(
                downloader = downloader,
                targetTempInstaller = tempInstaller,
                removeFromDownload = "cleanroom-${cleanroomVersion.version}",
                tempMinecraftFolder = tempMinecraftDir,
                sourceInherit = info.gameVersion,
                processedInherit = "1.12.2"
            )
        )

        addTask(
            androidText(
                R.string.download_game_install_base_install,
                cleanroomVersion.version
            ),
            R.drawable.ic_build_outlined,
            getForgeLikeInstallTask(
                isNew = true,
                downloader = downloader,
                loaderName = cleanroomVersion.version,
                tempFolderName = tempFolderName,
                tempInstaller = tempInstaller,
                tempGameFolder = tempGameDir,
                tempMinecraftDir = tempMinecraftDir,
                inherit = "1.12.2"
            )
        )
    }

    private fun createModLikeDownloadTask(
        tempModsDir: File,
        modVersion: ModVersion
    ) = Task.runTask(
        id = "Download.Mods",
        task = { task ->
            withSpeedReport(
                onSpeedReport = { bytes ->
                    task.updateSpeed(bytes)
                },
                onClear = {
                    task.clearSpeed()
                }
            ) { report ->
                downloadFileFromSources(
                    urls = modVersion.file.url.mapMCIMMirrorUrls(),
                    sha1 = modVersion.file.hashes.sha1,
                    outputFile = File(tempModsDir, modVersion.file.fileName),
                    sizeCallback = report
                )
            }
        }
    )

    /**
     * 游戏带附加内容安装完成，合并版本Json、迁移游戏文件
     * @param createIsolation 是否新创建启用版本隔离的版本配置
     */
    private fun createGameInstalledTask(
        tempMinecraftDir: File,
        targetMinecraftDir: File,
        targetClientDir: File,
        tempClientDir: File,
        tempModsDir: File,
        createIsolation: Boolean = true,
        optiFineFolder: File? = null,
        forgeFolder: File? = null,
        neoForgeFolder: File? = null,
        fabricFolder: File? = null,
        legacyFabricFolder: File? = null,
        quiltFolder: File? = null,
        cleanroomFolder: File? = null,
        onComplete: suspend () -> Unit = {}
    ) = Task.runTask(
        id = GAME_JSON_MERGER_ID,
        dispatcher = Dispatchers.IO,
        task = { task ->
            //合并版本 Json
            task.updateProgress(0.1f)
            mergeGameJson(
                info = info,
                outputFolder = targetClientDir,
                clientFolder = tempClientDir,
                optiFineFolder = optiFineFolder,
                forgeFolder = forgeFolder,
                neoForgeFolder = neoForgeFolder,
                fabricFolder = fabricFolder,
                legacyFabricFolder = legacyFabricFolder,
                quiltFolder = quiltFolder,
                cleanroomFolder = cleanroomFolder
            )

            //迁移游戏文件
            //议题 #78：并行安装同一个 MC 版本时两个安装会同时写目标 libraries，
            //按目标目录加路径锁串行（内容相同，但要避免一边写一边被另一边截断）
            PathWriteLock.withWrite(File(targetMinecraftDir, "libraries")) {
                copyDirectoryContents(
                    File(tempMinecraftDir, "libraries"),
                    File(targetMinecraftDir, "libraries"),
                    onProgress = { percentage ->
                        task.updateProgress(percentage)
                    }
                )
            }

            //复制客户端文件
            copyVanillaFiles(
                sourceGameFolder = tempMinecraftDir,
                sourceVersion = info.gameVersion,
                destinationGameFolder = targetGameFolder,
                targetVersion = info.customVersionName
            )

            //复制Mods
            tempModsDir.listFiles()?.let {
                val targetModsDir = VersionFolders.MOD.getDir(targetClientDir)
                it.forEach { modFile ->
                    val targetMod = File(targetModsDir, modFile.name)
                    if (!targetMod.exists()) {
                        //如果已经安装了，那就不覆盖
                        //用户可能是覆盖安装，所以检查这个很有必要
                        modFile.copyTo(targetMod)
                    }
                }
                if (createIsolation) {
                    //开启版本隔离
                    VersionConfig.createIsolation(targetClientDir).save()
                }
            }

            //清除临时游戏目录
            task.updateProgress(-1f)
            task.updateMessage(androidText(R.string.download_install_clear_temp))
            clearTempGameDir()

            onComplete()
        }
    )

    /**
     * 仅原本客户端文件复制任务 json、jar
     */
    private fun createVanillaFilesCopyTask(
        tempMinecraftDir: File,
        onComplete: suspend () -> Unit = {}
    ): Task {
        return Task.runTask(
            id = "VanillaFilesCopy",
            task = { task ->
                //复制客户端文件
                copyVanillaFiles(
                    sourceGameFolder = tempMinecraftDir,
                    sourceVersion = info.gameVersion,
                    destinationGameFolder = targetGameFolder,
                    targetVersion = info.customVersionName
                )

                //清除临时游戏目录
                task.updateProgress(-1f)
                task.updateMessage(androidText(R.string.download_install_clear_temp))
                clearTempGameDir()

                onComplete()
            }
        )
    }

    private fun File.createDirAndLog(): File {
        this.mkdirs()
        Logger.debug(TAG, "Created directory: $this")
        return this
    }
}