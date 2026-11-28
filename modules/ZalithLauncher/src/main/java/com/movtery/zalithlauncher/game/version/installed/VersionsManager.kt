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

package com.movtery.zalithlauncher.game.version.installed

import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.game.launch.LogName
import com.movtery.zalithlauncher.game.path.getGameHome
import com.movtery.zalithlauncher.game.path.getVersionsHome
import com.movtery.zalithlauncher.game.version.installed.utils.parseJsonToVersionInfo
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException

private const val TAG = "VersionsManager"

/**
 * 删除版本时，实例会被先改名挪进这个隐藏目录（游戏目录下）。
 * 必须与 `versions` 同分区，rename 才是 O(1) 且原子的。
 */
private const val DELETE_TRASH_DIR_NAME = ".zn_delete_trash"

/**
 * 删除临时目录至少闲置这么久才会被顺手清理。
 * 用户刚点「取消删除」时不能立刻又在后台把剩余文件删掉（那就等于没取消）。
 */
private const val DELETE_TRASH_MIN_AGE_MS = 5 * 60 * 1000L

/**
 * 删除版本时的进度阶段
 */
enum class DeleteVersionPhase {
    /** 扫描：统计版本目录内的文件数量与总大小（只读，可安全取消） */
    SCANNING,
    /** 删除：实例已挪到临时目录，按文件推进，进度可确定 */
    DELETING
}

/**
 * 删除版本的实时进度，供全屏进度界面展示
 */
data class DeleteVersionProgress(
    val versionName: String,
    val phase: DeleteVersionPhase,
    val totalFiles: Int = 0,
    val doneFiles: Int = 0,
    val totalBytes: Long = 0L,
    val doneBytes: Long = 0L,
    val currentEntry: String = "",
) {
    /**
     * 确定的 0f..1f 进度；扫描阶段总数未知，返回 null（界面显示为不确定进度）。
     * 优先按字节算，实例里全是 0 字节文件时退回按文件数量算。
     */
    val fraction: Float?
        get() = when {
            phase != DeleteVersionPhase.DELETING -> null
            totalBytes > 0L -> (doneBytes.toDouble() / totalBytes.toDouble()).toFloat().coerceIn(0f, 1f)
            totalFiles > 0 -> (doneFiles.toFloat() / totalFiles.toFloat()).coerceIn(0f, 1f)
            else -> null
        }
}

/**
 * 一次删除版本操作的结果
 */
sealed interface DeleteVersionResult {
    val versionName: String

    data class Success(override val versionName: String): DeleteVersionResult
    data class Failed(override val versionName: String, val throwable: Throwable): DeleteVersionResult
    /**
     * 用户取消删除。
     * @param restored 实例目录是否已经恢复回原位置（恢复后仍可能少了几个取消前已删掉的文件）
     * @param deletedFiles 取消前已经被删除的文件数量（0 = 一个文件都没删）
     */
    data class Cancelled(
        override val versionName: String,
        val restored: Boolean,
        val deletedFiles: Int = 0
    ): DeleteVersionResult
}

object VersionsManager {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val mutex = Mutex()
    private val listeners: MutableList<suspend () -> Unit> = mutableListOf()

    /**
     * 注册版本列表刷新监听器
     */
    fun registerListener(listener: suspend () -> Unit) {
        listeners.add(listener)
    }

    /**
     * 移除版本列表刷新监听器
     */
    fun unregisterListener(listener: suspend () -> Unit) {
        listeners.remove(listener)
    }

    private val _versions = MutableStateFlow<List<Version>>(emptyList())
    /** 当前所有的游戏版本 */
    val versions = _versions.asStateFlow()

    /**
     * 当前的游戏信息
     */
    var gameInfo: CurrentGameInfo? = null
        private set

    private val _currentVersion = MutableStateFlow<Version?>(null)
    val currentVersion = _currentVersion.asStateFlow()

    private var currentJob: Job? = null

    private val deleteMutex = Mutex()
    private var deleteJob: Job? = null

    private val _deleteProgress = MutableStateFlow<DeleteVersionProgress?>(null)
    /** 正在进行的删除版本进度；null 表示当前没有删除在进行 */
    val deleteProgress = _deleteProgress.asStateFlow()

    private val _deleteResult = MutableStateFlow<DeleteVersionResult?>(null)
    /** 最近一次删除版本的结果，界面展示完提示后调用 [clearDeleteResult] 清掉 */
    val deleteResult = _deleteResult.asStateFlow()

    /** 是否有删除版本的任务正在运行 */
    val isDeleting: Boolean get() = deleteJob?.isActive == true

    private val _isRefreshing = MutableStateFlow(false)
    /** 是否正在刷新版本 */
    val isRefreshing = _isRefreshing.asStateFlow()

    /**
     * 检查版本是否已经存在
     */
    fun isVersionExists(versionName: String, checkJson: Boolean = false): Boolean {
        val folder = File(getVersionsHome(), versionName)
        //保证版本文件夹存在的同时，也应保证其版本json文件存在
        return if (checkJson) File(folder, "${folder.name}.json").exists()
        else folder.exists()
    }

    /**
     * 刷新所有版本
     * @param tag 是由谁发起的刷新，输出到日志方便定位
     * @param trySetVersion 在刷新完成后尝试设置当前版本
     */
    fun refresh(tag: String, trySetVersion: String? = null) {
        currentJob?.cancel()
        currentJob = scope.launch {
            mutex.withLock {
                _isRefreshing.update { true }
                Logger.debug(TAG, "Initiated by $tag: starting to refresh the version list.")

                if (trySetVersion != null) {
                    saveCurrentVersion(trySetVersion, refresh = false)
                    Logger.debug(TAG, "Has attempted to save the current version: $trySetVersion")
                }

                _versions.update { emptyList() }

                val newVersions = mutableListOf<Version>()
                File(getVersionsHome()).listFiles()?.forEach { versionFile ->
                    runCatching {
                        processVersionFile(versionFile)
                    }.getOrNull()?.let {
                        newVersions.add(it)
                    }
                }

                _versions.update { newVersions.sortedWith(VersionComparator) }

                gameInfo = refreshCurrentInfo()
                Logger.debug(TAG, "Version list refreshed, refreshing the current version now.")
                refreshCurrentVersion()

                listeners.forEach { it() }

                _isRefreshing.update { false }
            }

            //刷新结束后顺手处理「上次删除被取消 / 进程被杀」留下的临时目录（删除进行中时不动它）
            tryCleanupDeleteTrash()
        }
    }

    /**
     * 执行在版本列表刷新完成后可执行的任务
     */
    suspend fun waitForRefresh() {
        mutex.withLock {}
    }

    private fun processVersionFile(versionFile: File): Version? {
        if (versionFile.exists() && versionFile.isDirectory) {
            var isVersion = false

            //通过判断是否存在版本的.json文件，来确定其是否为一个版本
            val jsonFile = File(versionFile, "${versionFile.name}.json")
            val versionInfo = if (jsonFile.exists() && jsonFile.isFile) {
                parseJsonToVersionInfo(jsonFile)?.also {
                    //如果解析失败了，可能不是标准版本
                    //保险起见，只有解析成功了的版本，才会被判定为有效版本
                    isVersion = true
                }
            } else {
                null
            }

            val versionConfig = VersionConfig.parseConfig(versionFile)

            val version = Version(
                versionFile.name,
                versionConfig,
                versionInfo,
                isVersion,
                versionInfo.getVersionType()
            )

            Logger.info(TAG, 
                "Identified and added version: ${version.getVersionName()}, " +
                        "Path: (${version.getVersionPath()}), " +
                        "Info: ${version.getVersionInfo()?.getInfoString()}"
            )

            return version
        }
        return null
    }

    private fun refreshCurrentVersion() {
        val version = run {
            val currentList = _versions.value
            if (currentList.isEmpty()) return@run null

            fun getVersionByFirst(): Version? {
                return currentList.find { it.isValid() }?.apply {
                    //确保版本有效
                    saveCurrentVersion(getVersionName(), refresh = false)
                }
            }

            runCatching {
                val versionString = gameInfo!!.version
                currentList.getVersion(versionString) ?: run {
                    Logger.debug(TAG, "Stored version $versionString not found, using the first available version instead.")
                    getVersionByFirst()
                }
            }.onFailure { e ->
                Logger.warning(TAG, "The current version information has not been initialized yet.", e)
            }.getOrElse {
                getVersionByFirst()
            }
        }.also { version ->
            Logger.debug(TAG, "The current version is: ${version?.getVersionName()}")
        }

        _currentVersion.update { version }
    }

    private fun List<Version>.getVersion(name: String?): Version? {
        name?.let { versionName ->
            return find { it.getVersionName() == versionName }?.takeIf { it.isValid() }
        }
        return null
    }

    fun getVersion(name: String?): Version? {
        return _versions.value.getVersion(name)
    }

    /**
     * @return 通过版本名，判断其版本是否存在
     */
    fun checkVersionExistsByName(versionName: String?) =
        versionName?.let { name -> _versions.value.any { it.getVersionName() == name } } ?: false

    /**
     * @return 获取 Zalith 启动器版本标识文件夹
     */
    fun getZalithVersionPath(version: Version) = File(version.getVersionPath(), BuildKeys.LAUNCHER_IDENTIFIER)

    /**
     * @return 通过目录获取 Zalith 启动器版本标识文件夹
     */
    fun getZalithVersionPath(folder: File) = File(folder, BuildKeys.LAUNCHER_IDENTIFIER)

    /**
     * @return 通过名称获取 Zalith 启动器版本标识文件夹
     */
    fun getZalithVersionPath(name: String) = File(getVersionPath(name), BuildKeys.LAUNCHER_IDENTIFIER)

    /**
     * @return 游戏的上一次运行日志
     */
    fun getLatestLog(version: Version) = File(getZalithVersionPath(version), LogName.GAME.fileName)

    /**
     * @return 获取当前版本设置的图标
     */
    fun getVersionIconFile(version: Version) = File(getZalithVersionPath(version), "VersionIcon.png")

    /**
     * @return 通过目录获取 Zalith 启动器版本标识文件夹
     */
    fun getVersionIconFile(folder: File) = File(getZalithVersionPath(folder), "VersionIcon.png")

    /**
     * @return 通过名称获取当前版本设置的图标
     */
    fun getVersionIconFile(name: String) = File(getZalithVersionPath(name), "VersionIcon.png")

    /**
     * @return 通过名称获取版本的文件夹路径
     */
    fun getVersionPath(name: String) = File(getVersionsHome(), name)

    /**
     * 保存当前选择的版本
     * @return 是否执行保存
     */
    fun saveVersion(version: Version, refresh: Boolean = true): Boolean {
        if (!version.isValid()) return false
        saveCurrentVersion(version.getVersionName(), refresh)
        return true
    }

    /**
     * 保存当前选择的版本
     */
    fun saveCurrentVersion(versionName: String, refresh: Boolean = true) {
        runCatching {
            gameInfo!!.apply {
                version = versionName
                saveCurrentInfo()
            }
            if (refresh) {
                Logger.debug(TAG, "Current game info file saved, refreshing the current version now.")
                refreshCurrentVersion()
            }
        }.onFailure { e ->
            Logger.error(TAG, "An exception occurred while saving the currently selected version information.", e)
        }
    }

    /**
     * 重命名当前版本，但并不会在这里对即将重命名的名称，进行非法性判断
     */
    fun renameVersion(version: Version, name: String) {
        val currentVersionName = _currentVersion.value?.getVersionName()
        //如果当前的版本是即将被重命名的版本，那么就把将要重命名的名字设置为当前版本
        val saveToCurrent = version.getVersionName() == currentVersionName

        val versionFolder = version.getVersionPath()
        val renameFolder = File(getVersionsHome(), name)

        //不管重命名之后的文件夹是什么，只要这个文件夹存在，那么就必须删除
        //否则将出现问题
        FileUtils.deleteQuietly(renameFolder)

        val originalName = versionFolder.name

        versionFolder.renameTo(renameFolder)

        val versionJsonFile = File(renameFolder, "$originalName.json")
        val versionJarFile = File(renameFolder, "$originalName.jar")
        val renameJsonFile = File(renameFolder, "$name.json")
        val renameJarFile = File(renameFolder, "$name.jar")

        versionJsonFile.renameTo(renameJsonFile)
        versionJarFile.renameTo(renameJarFile)

        FileUtils.deleteQuietly(versionFolder)

        if (saveToCurrent) {
            //设置并刷新当前版本
            saveCurrentVersion(name, refresh = false)
        }

        refresh("VersionsManager.renameVersion")
    }

    /**
     * 将选中的版本复制为一个新的版本
     * @param version 选中的版本
     * @param name 新的版本的名称
     * @param copyAllFile 是否复制全部文件
     */
    fun copyVersion(version: Version, name: String, copyAllFile: Boolean) {
        val versionsFolder = version.getVersionsFolder()
        val newVersion = File(versionsFolder, name)

        val originalName = version.getVersionName()

        //新版本的json与jar文件
        val newJsonFile = File(newVersion, "$name.json")
        val newJarFile = File(newVersion, "$name.jar")

        val originalVersionFolder = version.getVersionPath()
        if (copyAllFile) {
            //启用复制所有文件时，直接将原文件夹整体复制到新版本
            FileUtils.copyDirectory(originalVersionFolder, newVersion)
            //重命名json、jar文件
            val jsonFile = File(newVersion, "$originalName.json")
            val jarFile = File(newVersion, "$originalName.jar")
            if (jsonFile.exists()) jsonFile.renameTo(newJsonFile)
            if (jarFile.exists()) jarFile.renameTo(newJarFile)
        } else {
            //不复制所有文件时，仅复制并重命名json、jar文件
            val originalJsonFile = File(originalVersionFolder, "$originalName.json")
            val originalJarFile = File(originalVersionFolder, "$originalName.jar")
            newVersion.mkdirs()
            // versions/1.21.3/1.21.3.json -> versions/name/name.json
            if (originalJsonFile.exists()) originalJsonFile.copyTo(newJsonFile)
            // versions/1.21.3/1.21.3.jar -> versions/name/name.jar
            if (originalJarFile.exists()) originalJarFile.copyTo(newJarFile)
        }

        //保存版本配置文件
        version.getVersionConfig().copy().let { config ->
            config.setVersionPath(newVersion)
            config.isolationType = SettingState.ENABLE
            config.saveWithThrowable()
        }

        refresh("VersionsManager.copyVersion")
    }

    /**
     * 清除最近一次删除版本的结果（界面展示完「完成 / 已取消 / 失败」提示后调用）
     */
    fun clearDeleteResult() {
        _deleteResult.update { null }
    }

    /**
     * 启动一次删除版本任务。
     *
     * 任务挂在 [scope] 上，而不是界面的组合作用域：
     * 1. 删除过程不会随页面销毁被截断，避免「界面一关就变成静默删除」；
     * 2. 进度与结果通过 [deleteProgress] / [deleteResult] 暴露，全屏进度界面只负责展示与「取消」，
     *    不再直接执行阻塞的目录删除（旧实现是 `FileUtils.deleteQuietly` 一把梭：没有进度、没法取消、
     *    失败还会被静默吞掉）。
     *
     * @return 是否真的启动了删除（已经有一个删除在跑时返回 false）
     */
    fun startDeleteVersion(version: Version): Boolean {
        if (isDeleting) {
            Logger.warning(TAG, "A version deletion is already running, ignored: ${version.getVersionName()}")
            return false
        }
        val versionName = version.getVersionName()
        _deleteResult.update { null }
        _deleteProgress.update {
            DeleteVersionProgress(versionName = versionName, phase = DeleteVersionPhase.SCANNING)
        }
        deleteJob = scope.launch { deleteVersion(version) }
        return true
    }

    /**
     * 取消正在进行的删除版本任务。
     *
     * 若还在扫描阶段，磁盘没有任何改动；若已经开始删除，实例已先被原子地挪进隐藏临时目录，
     * 取消时会在 [deleteVersion] 里把它 rename 回原位置，真正把实例恢复出来。
     */
    fun cancelDeleteVersion() {
        deleteJob?.cancel()
    }

    /**
     * 删除版本（由 [startDeleteVersion] 启动）
     *
     * 全流程在 IO 线程执行、按文件检查协程取消，主线程不碰磁盘 IO。
     */
    private suspend fun deleteVersion(version: Version) {
        val versionName = version.getVersionName()
        val versionFolder = version.getVersionPath()

        deleteMutex.withLock {
            var trashFolder: File? = null
            var deletingStarted = false
            var deletedFiles = 0

            try {
                withContext(Dispatchers.IO) {
                    //只清理「以前遗留且已闲置一段时间」的临时目录：
                    //刚取消的目录要留着恢复实例，不能立刻被清掉
                    cleanupDeleteTrash(minAgeMillis = DELETE_TRASH_MIN_AGE_MS)

                    //1. 扫描：先统计文件数量与总大小，后面才能给出确定进度
                    val (totalFiles, totalBytes) = scanForDeletion(versionFolder) { files, bytes, entry ->
                        _deleteProgress.update {
                            DeleteVersionProgress(
                                versionName = versionName,
                                phase = DeleteVersionPhase.SCANNING,
                                totalFiles = files,
                                doneFiles = files,
                                totalBytes = bytes,
                                doneBytes = bytes,
                                currentEntry = entry
                            )
                        }
                    }

                    //2. 原子地挪进临时目录：版本列表立刻看不到它，中途取消 / 进程被杀也不会留下半删实例
                    val movedTrash = moveVersionToTrash(versionFolder)
                    trashFolder = movedTrash
                    if (movedTrash != null) {
                        Logger.info(TAG, "Version $versionName moved to delete temp folder: ${movedTrash.name}")
                    } else {
                        Logger.warning(TAG, "Unable to move $versionName to delete temp folder, deleting in place.")
                    }
                    val deleteTarget = movedTrash ?: versionFolder

                    //3. 按文件删除并上报确定进度
                    deletingStarted = true
                    val failedFiles = deleteFolder(
                        folder = deleteTarget,
                        versionName = versionName,
                        totalFiles = totalFiles,
                        totalBytes = totalBytes,
                        onFileDeleted = { deletedFiles++ }
                    )

                    when {
                        failedFiles.isEmpty() -> Unit
                        //已经挪走：失败的文件藏在隐藏临时目录里，不影响使用，留给后续自动清理
                        movedTrash != null -> Logger.warning(
                            TAG, "${failedFiles.size} file(s) of $versionName could not be deleted, will be cleaned up later."
                        )
                        //原地删除失败会留下残缺实例，必须抛给界面提示并允许用户重试
                        else -> throw IOException("Failed to delete ${failedFiles.size} file(s) of $versionName")
                    }
                }

                _deleteResult.update { DeleteVersionResult.Success(versionName) }
            } catch (e: CancellationException) {
                //取消不是「延迟删除」：能恢复就一定把实例 rename 回原位置，否则用户会以为保住了、结果丢了
                val detached = trashFolder
                val restored = if (detached != null) {
                    //删除阶段取消：实例还在临时目录里，同分区 rename 回来是 O(1)
                    restoreFromTrash(detached, versionFolder)
                } else if (!deletingStarted) {
                    //扫描阶段取消：磁盘没有任何改动
                    true
                } else {
                    //原地删除阶段取消：已经删掉一部分文件，没法恢复成完整实例
                    false
                }
                Logger.info(TAG, "Version deletion cancelled: $versionName, restored=$restored, deleted=$deletedFiles")
                _deleteResult.update { DeleteVersionResult.Cancelled(versionName, restored, deletedFiles) }
                throw e
            } catch (e: Throwable) {
                Logger.error(TAG, "Failed to delete version: $versionName", e)
                _deleteResult.update { DeleteVersionResult.Failed(versionName, e) }
            } finally {
                _deleteProgress.update { null }
                //无论成功 / 取消 / 失败都刷新一次，保证版本列表与磁盘一致
                refresh("VersionsManager.deleteVersion")
            }
        }
    }

    /**
     * 扫描待删除目录，统计文件数量与总大小（只读、可取消）
     * @return 文件数量 to 总字节数
     */
    private suspend fun scanForDeletion(
        folder: File,
        onProgress: (files: Int, bytes: Long, currentEntry: String) -> Unit
    ): Pair<Int, Long> {
        var files = 0
        var bytes = 0L
        var lastNotifyTime = 0L

        //用显式 for 遍历 Sequence：不在 Sequence.forEach 的内联 lambda 里调 suspend 函数，规避编译细节
        for (entry in folder.walkTopDown()) {
            currentCoroutineContext().ensureActive()
            if (entry.isFile) {
                files++
                bytes += entry.length()
            }

            val now = System.currentTimeMillis()
            //节流：每 128 个文件或 120ms 上报一次，避免海量小文件把界面刷爆
            if ((files > 0 && files % 128 == 0) || now - lastNotifyTime >= 120L) {
                lastNotifyTime = now
                onProgress(files, bytes, entry.name)
            }
        }

        onProgress(files, bytes, "")
        return files to bytes
    }

    /**
     * 删除目录内的文件与子目录，并上报确定进度
     * @return 删除失败的条目（空集合 = 全部删除成功）
     */
    private suspend fun deleteFolder(
        folder: File,
        versionName: String,
        totalFiles: Int,
        totalBytes: Long,
        onFileDeleted: () -> Unit = {}
    ): Set<File> {
        val failed = mutableSetOf<File>()
        var doneFiles = 0
        var doneBytes = 0L
        var lastNotifyTime = 0L

        _deleteProgress.update {
            DeleteVersionProgress(
                versionName = versionName,
                phase = DeleteVersionPhase.DELETING,
                totalFiles = totalFiles,
                totalBytes = totalBytes
            )
        }

        //先删文件：先删目录会让 walkTopDown 的遍历提前断掉
        for (entry in folder.walkTopDown()) {
            currentCoroutineContext().ensureActive()
            if (entry.isFile) {
                val size = entry.length()
                if (runCatching { entry.delete() }.getOrDefault(false)) {
                    doneFiles++
                    doneBytes += size
                    onFileDeleted()
                }

                val now = System.currentTimeMillis()
                //节流：每 64 个文件或 120ms 上报一次
                if ((doneFiles > 0 && doneFiles % 64 == 0) || now - lastNotifyTime >= 120L) {
                    lastNotifyTime = now
                    _deleteProgress.update {
                        DeleteVersionProgress(
                            versionName = versionName,
                            phase = DeleteVersionPhase.DELETING,
                            totalFiles = totalFiles,
                            doneFiles = doneFiles,
                            totalBytes = totalBytes,
                            doneBytes = doneBytes,
                            currentEntry = entry.name
                        )
                    }
                }
            }
        }

        //自底向上删空目录；第一轮没删掉的文件也会在这里再试一次，仍然失败的才记录下来
        for (entry in folder.walkBottomUp()) {
            currentCoroutineContext().ensureActive()
            if (entry.exists() && !runCatching { entry.delete() }.getOrDefault(false)) {
                failed.add(entry)
            }
        }

        //全部删除成功时把进度补满，避免界面停在 99%
        if (failed.isEmpty()) {
            _deleteProgress.update {
                DeleteVersionProgress(
                    versionName = versionName,
                    phase = DeleteVersionPhase.DELETING,
                    totalFiles = totalFiles,
                    doneFiles = totalFiles,
                    totalBytes = totalBytes,
                    doneBytes = totalBytes
                )
            }
        }

        return failed
    }

    /**
     * 取消删除时把实例从临时目录 rename 回原位置。
     *
     * 删除期间实例一直完整待在同一个文件系统里（只是改了名字），所以这是 O(1) 且无损的恢复；
     * 只有「原路径被别的任务重新创建」这种异常情况才放弃恢复，让临时目录留给后续清理。
     *
     * @return 是否完整恢复
     */
    private fun restoreFromTrash(trashFolder: File, versionFolder: File): Boolean {
        if (!trashFolder.exists()) return false
        //原路径已经有东西了就不能覆盖，否则会和正在进行的安装等任务互相破坏
        if (versionFolder.exists()) return false
        return runCatching { trashFolder.renameTo(versionFolder) }.getOrDefault(false)
    }

    /**
     * 把版本目录改名到隐藏的删除临时目录（同分区 rename，O(1)）
     * @return 临时目录；目录已不存在或改名失败时返回 null（调用方回退为原地删除）
     */
    private fun moveVersionToTrash(versionFolder: File): File? {
        if (!versionFolder.exists()) return null
        val trashRoot = getDeleteTrashFolder() ?: return null
        val target = File(trashRoot, "${versionFolder.name}_${System.currentTimeMillis()}")
        val moved = runCatching {
            trashRoot.mkdirs()
            versionFolder.renameTo(target)
        }.getOrDefault(false)
        return if (moved) target else null
    }

    /**
     * @return 删除临时目录（放在游戏目录下，与 versions 同分区才能直接 rename）
     */
    private fun getDeleteTrashFolder(): File? {
        val gameHome = getGameHome().takeIf { it.isNotBlank() } ?: return null
        return File(gameHome, DELETE_TRASH_DIR_NAME)
    }

    /**
     * 清理删除临时目录里遗留的文件
     * @param minAgeMillis 只清理闲置超过这么久的目录，避免用户刚取消就又被后台删掉
     */
    private suspend fun cleanupDeleteTrash(minAgeMillis: Long) = withContext(Dispatchers.IO) {
        val trashRoot = getDeleteTrashFolder() ?: return@withContext
        val entries = trashRoot.listFiles() ?: return@withContext
        val now = System.currentTimeMillis()
        entries.forEach { entry ->
            ensureActive()
            if (minAgeMillis > 0L && now - entry.lastModified() < minAgeMillis) return@forEach
            Logger.info(TAG, "Cleaning leftover delete temp folder: ${entry.name}")
            FileUtils.deleteQuietly(entry)
            if (entry.exists()) {
                Logger.warning(TAG, "Failed to clean leftover delete temp folder: ${entry.name}")
            }
        }
        //临时目录空了就一起删掉，不在游戏目录里留一个空文件夹
        if (trashRoot.listFiles().isNullOrEmpty()) {
            trashRoot.delete()
        }
    }

    /**
     * 空闲时顺手清理删除临时目录（不阻塞任何界面，失败忽略）
     */
    private fun tryCleanupDeleteTrash() {
        //删除进行中时不动临时目录
        if (isDeleting) return
        scope.launch {
            try {
                cleanupDeleteTrash(minAgeMillis = DELETE_TRASH_MIN_AGE_MS)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                //清理只是顺手做的事，失败不能影响刷新或把异常抛到协程外
                Logger.warning(TAG, "Failed to clean leftover delete temp folders.", e)
            }
        }
    }
}