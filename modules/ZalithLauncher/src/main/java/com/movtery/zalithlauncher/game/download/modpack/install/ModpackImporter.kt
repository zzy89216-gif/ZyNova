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

package com.movtery.zalithlauncher.game.download.modpack.install

import android.content.Context
import android.net.Uri
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.context.copyLocalFile
import com.movtery.zalithlauncher.coroutine.TaskFlowExecutor
import com.movtery.zalithlauncher.coroutine.TitledTask
import com.movtery.zalithlauncher.coroutine.addTask
import com.movtery.zalithlauncher.coroutine.buildPhase
import com.movtery.zalithlauncher.game.download.InstallJournal
import com.movtery.zalithlauncher.game.download.InstallTargetRegistry
import com.movtery.zalithlauncher.game.download.modpack.platform.ALL_PACK_PARSER
import com.movtery.zalithlauncher.game.download.modpack.platform.AbstractPack
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.game.version.installed.VersionFolders
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.ui.androidText
import com.movtery.zalithlauncher.utils.file.extractFromZip
import com.movtery.zalithlauncher.utils.logging.Logger
import com.movtery.zalithlauncher.utils.network.isUsingMobileData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.UUID
import org.apache.commons.compress.archivers.zip.ZipFile as ApacheZipFile
import java.util.zip.ZipFile as JDKZipFile

private const val TAG = "ModpackImporter"

/**
 * 本地整合包导入器
 * @param uri 本地选择的文件链接
 * @param scope 在有生命周期管理的scope中执行安装任务
 * @param waitForVersionName 等待用户输入预期的版本名
 * @param waitForConfirmMobileData 等待用户确认使用移动网络
 */
class ModpackImporter(
    private val context: Context,
    private val uri: Uri,
    private val scope: CoroutineScope,
    private val waitForVersionName: suspend (String) -> String,
    private val waitForConfirmMobileData: suspend () -> Boolean,
) {
    private val taskExecutor = TaskFlowExecutor(scope)
    val taskFlow: StateFlow<List<TitledTask>> = taskExecutor.tasksFlow

    /**
     * 当前导入的整合包的任务构建器
     */
    private lateinit var modpack: AbstractPack

    /**
     * 本次导入独占的临时整合包目录（议题 #78）
     * 本地导入和在线下载可能同时进行，共用一个目录会互相清理。
     */
    private val tempModPackRoot: File = File(
        PathManager.DIR_CACHE_MODPACK_DOWNLOADER,
        "import_${UUID.randomUUID().toString().take(8)}"
    )

    /** 已占用的目标版本名（议题 #78：本地导入同样与 UI/AI/整合包跨入口互斥） */
    private val targetReserved = java.util.concurrent.atomic.AtomicBoolean(false)
    private var reservedTargetName: String? = null

    private fun releaseTarget() {
        if (targetReserved.compareAndSet(true, false)) {
            reservedTargetName?.let { InstallTargetRegistry.release(it) }
        }
    }

    /**
     * 开始导入整合包
     * @param isRunning 正在运行中，被拒绝导入时
     * @param onFinished 导入完成时
     * @param onCancelled 内部取消时
     * @param onError 导入过程中出现异常
     */
    fun startImport(
        isRunning: () -> Unit = {},
        onFinished: (version: String) -> Unit,
        onCancelled: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        if (taskExecutor.isRunning()) {
            isRunning()
            return //正在运行中，拒绝导入
        }

        taskExecutor.executePhasesAsync(
            onStart = {
                val tasks = getTaskPhases()
                taskExecutor.addPhases(tasks)
            },
            onComplete = {
                releaseTarget()
                //导入成功：只清本次导入的落盘记录（嵌套的游戏安装记录由嵌套安装器自己清）
                //⚠️ 不能 clearGameInstall() 清全部：会把并行安装的其它游戏记录一起清掉
                InstallJournal.clearModpackInstall(tempModPackRoot.absolutePath)
                onFinished(modpack.getFinalClientName())
            },
            onError = { e ->
                releaseTarget()
                //议题 #73：整合包导入失败/取消时干净回滚，避免留下半成品实例
                InstallJournal.rollbackInterruptedModpackInstall(tempModPackRoot.absolutePath)
                if (e is UsingMobileDataException) {
                    //用户不希望使用移动网络
                    onCancelled()
                    return@executePhasesAsync
                }
                onError(e)
            },
            onCancel = {
                releaseTarget()
                InstallJournal.rollbackInterruptedModpackInstall(tempModPackRoot.absolutePath)
                onCancelled()
            }
        )
    }

    private suspend fun getTaskPhases() = withContext(Dispatchers.IO) {
        //临时游戏环境目录（本次导入独占，见 [tempModPackRoot]）
        val tempModPackDir = tempModPackRoot
        //议题 #73：整合包导入同样从开始就落盘记录，进程被杀后下次启动才能判断「没装完」
        InstallJournal.beginModpackInstall(tempModPackDir.absolutePath)
        val tempVersionsDir = File(tempModPackDir, "fkVersion")
        //整合包安装包文件
        val installerDir = File(tempModPackDir, "installer")
        val installerFile = File(installerDir, ".temp_installer.zip")
        //已解压的整合包缓存目录
        val packDir = File(installerDir, ".temp_pack")

        listOf(
            buildPhase {
                //清除上一次安装的缓存（如果有的话，可能会影响这次的安装结果）
                addTask(
                    id = "ImportModpack.Cleanup",
                    title = androidText(R.string.download_install_clear_temp),
                    icon = R.drawable.ic_auto_delete_outlined
                ) { _ ->
                    GamePathManager.waitForRefresh()
                    VersionsManager.waitForRefresh()
                    clearTempModPackDir()
                    //清理完成缓存目录后，创建新的缓存目录
                    tempModPackDir.createDirAndLog()
                    tempVersionsDir.createDirAndLog()
                    installerDir.createDirAndLog()
                    packDir.createDirAndLog()
                    VersionFolders.MOD.getDir(tempVersionsDir).createDirAndLog() //创建临时模组目录
                }

                //先导入文件
                addTask(
                    id = "ImportModpack.ImportFile",
                    title = androidText(R.string.import_modpack_task_unpack),
                    dispatcher = Dispatchers.IO,
                    icon = R.drawable.ic_unarchive_outlined
                ) { task ->
                    task.updateProgress(-1f)
                    context.copyLocalFile(uri, installerFile)
                    //尝试解压压缩包
                    try {
                        JDKZipFile(installerFile).use { zip ->
                            zip.extractFromZip("", packDir)
                        }
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        Logger.warning(TAG, "JDK ZipFile failed to unpack, fallback to Apache ZipFile.", e)
                        try {
                            ApacheZipFile.builder().setFile(installerFile).get().use { zip ->
                                zip.extractFromZip("", packDir)
                            }
                        } catch (e: Exception) {
                            if (e is CancellationException) throw e
                            //如果兜底解压也失败了，则说明这可能不是一个压缩包
                            //或者压缩包已损坏，抛出不支持的异常终止任务流
                            Logger.error(TAG, "Unable to extract the installer file. Is it really a compressed archive?", e)
                            throw PackNotSupportedException(
                                reason = UnsupportedPackReason.CorruptedArchive
                            )
                        }
                    }

                    //在这个阶段开始检查是否使用移动网络
                    if (isUsingMobileData(context)) {
                        val use = waitForConfirmMobileData()
                        if (!use) {
                            //用户不决定使用移动网络安装，取消导入
                            throw UsingMobileDataException()
                        }
                    }
                }

                //解析整合包
                addTask(
                    id = "ImportModpack.ParsePack",
                    title = androidText(R.string.import_modpack_task_parse),
                    icon = R.drawable.ic_build_outlined
                ) { task ->
                    task.updateProgress(-1f)

                    modpack = run {
                        //尝试所有整合包格式 进行解析
                        for (parser in ALL_PACK_PARSER) {
                            ensureActive()

                            val result = runCatching {
                                parser.parse(packFolder = packDir)
                            }.onFailure { th ->
                                Logger.debug(TAG, "${parser.getIdentifier()} parser does not recognize this format", th)
                            }.getOrNull()

                            if (result != null) {
                                //成功识别到这个整合包格式
                                Logger.info(TAG, "Successfully detected the modpack format: ${result.platform.identifier}")
                                return@run result
                            } else {
                                Logger.debug(TAG, "Skipped the ${parser.getIdentifier()} parser")
                            }
                        }
                        //整合包不受支持，或格式有误未能匹配
                        null
                    } ?: throw PackNotSupportedException(UnsupportedPackReason.UnsupportedFormat)

                    //添加下一导入阶段的任务流
                    taskExecutor.addPhases(
                        modpack.buildTaskPhases(
                            context = context,
                            scope = scope,
                            versionFolder = tempVersionsDir,
                            //记录用户确认的目标版本名：进程被杀后恢复时才能定位半成品版本
                            waitForVersionName = { suggested ->
                                val name = waitForVersionName(suggested)
                                //议题 #78：名字确定后全局占用，避免与 UI/AI/其它整合包撞同名目标目录
                                if (!InstallTargetRegistry.reserve(name, "import:${uri}")) {
                                    throw IllegalStateException(
                                        context.getString(R.string.download_install_target_busy, name)
                                    )
                                }
                                reservedTargetName = name
                                targetReserved.set(true)
                                InstallJournal.updateModpackTargetVersion(tempModPackDir.absolutePath, name)
                                name
                            },
                            addPhases = { phases ->
                                taskExecutor.addPhases(phases)
                            },
                            onClearTemp = {
                                clearTempModPackDir()
                            }
                        )
                    )
                }
            }
        )
    }

    /**
     * 清理临时整合包版本目录
     */
    private suspend fun clearTempModPackDir() = withContext(Dispatchers.IO) {
        //议题 #78：只清本次导入自己的临时目录
        tempModPackRoot.takeIf { it.exists() }?.let { folder ->
            FileUtils.deleteQuietly(folder)
            Logger.info(TAG, "Temporary modpack directory cleared.")
        }
    }

    /**
     * 取消整合包导入
     */
    fun cancel() {
        taskExecutor.cancel()
    }

    private fun File.createDirAndLog(): File {
        this.mkdirs()
        Logger.debug(TAG, "Created directory: $this")
        return this
    }
}