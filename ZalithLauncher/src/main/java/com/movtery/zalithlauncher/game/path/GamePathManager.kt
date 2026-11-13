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

package com.movtery.zalithlauncher.game.path

import android.content.Context
import com.movtery.zalithlauncher.database.AppDatabase
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings.currentGamePathId
import com.movtery.zalithlauncher.utils.canHandlePermission
import com.movtery.zalithlauncher.utils.hasStoragePermission
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID
import kotlin.io.path.exists

private const val TAG = "GamePathManager"

/**
 * 游戏目录管理，为支持将游戏文件保存至不同的路径
 */
object GamePathManager {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val mutex = Mutex()
    /**
     * 默认游戏目录 = **公共存储里的 `/ZyNova/.Minecraft`**（`PathManager.DIR_GAME`）。
     *
     * ⚠️ 这里以前是 `Android/data/<包名>/files/.minecraft`（私有目录），
     * 直接写死会让「游戏数据搬到公共目录」只搬了运行时和皮肤缓存、**实例还留在私有目录**，
     * 用户在文件管理器里永远看不到自己的存档。所以默认目录必须跟着 `PathManager` 走：
     * 有「所有文件访问」时是公共目录，没有时自动回退旧的私有目录（保证还能用）。
     */
    private val defaultGamePath: String
        get() = PathManager.DIR_GAME.absolutePath
    /**
     * 默认游戏目录的ID
     */
    const val DEFAULT_ID = "default"

    private val _gamePathData = MutableStateFlow<List<GamePath>>(listOf())
    val gamePathData = _gamePathData.asStateFlow()

    private val _currentPath = MutableStateFlow(defaultGamePath)
    /** 当前选择的路径 */
    val currentPath = _currentPath.asStateFlow()

    /**
     * 是否是「旧的私有目录」路径。
     *
     * 两类都算：以前的实例根 `files/.minecraft`、以前的运行时/缓存根 `files/games`。
     * 这些记录在「选择目录」里对用户没有任何用处（普通文件管理器打不开），
     * 数据也已经搬到公共目录，所以加载时直接丢掉。
     */
    private fun isLegacyPrivatePath(path: String): Boolean {
        if (path.isBlank()) return false
        val legacyInstance = PathManager.legacyInstanceDir().absolutePath
        val legacyGame = PathManager.legacyGameDir().absolutePath
        return path == legacyInstance || path.startsWith("$legacyInstance/") ||
            path == legacyGame || path.startsWith("$legacyGame/")
    }

    /**
     * 当前游戏路径
     */
    fun getCurrentPath(): String = File(_currentPath.value).absolutePath

    /**
     * 当前用户路径
     */
    fun getUserPath(): String = File(_currentPath.value).parentFile!!.absolutePath

    private lateinit var database: AppDatabase
    private lateinit var gamePathDao: GamePathDao

    fun initialize(context: Context) {
        database = AppDatabase.getInstance(context)
        gamePathDao = database.gamePathDao()
    }

    fun reloadPath() {
        scope.launch {
            mutex.withLock {
                _gamePathData.update { emptyList() }

                val newValue = mutableListOf<GamePath>()
                //添加默认游戏目录
                newValue.add(0, GamePath(DEFAULT_ID, "", defaultGamePath))

                run parseConfig@{
                    //从数据库中加载游戏目录
                    val paths = gamePathDao.getAllPaths()
                    //⚠️ 旧版本默认目录是私有目录（`Android/data/<包名>/files/.minecraft`）。
                    //现在数据全部在公共目录，这些指向私有目录的记录已经没意义：
                    //留着只会在「选择目录」里出现一个用户根本打不开、还会误导人的路径。
                    //所以这里直接把它们从数据库里删掉（不动磁盘上的数据，迁移由 GameDirMigration 负责）。
                    val legacy = paths.filter { isLegacyPrivatePath(it.path) }
                    if (legacy.isNotEmpty()) {
                        legacy.forEach { path ->
                            runCatching { gamePathDao.deletePath(path) }
                                .onFailure { e ->
                                    Logger.warning(TAG, "Failed to drop legacy game path ${path.path}: ${e.message}")
                                }
                        }
                        Logger.info(TAG, "Dropped ${legacy.size} legacy private game paths")
                    }
                    newValue.addAll(paths.filterNot { isLegacyPrivatePath(it.path) }.sortedBy { it.title })
                }

                _gamePathData.update { newValue }

                if (canHandlePermission &&  !hasStoragePermission) {
                    _currentPath.update { defaultGamePath }
                    saveDefaultPath(false)
                } else {
                    refreshCurrentPath(false)
                }

                VersionsManager.refresh("GamePathManager.reloadPath")
                Logger.info(TAG, "Loaded ${_gamePathData.value.size} game paths")
            }
        }
    }

    /**
     * 执行在路径列表刷新完成后可执行的任务
     */
    suspend fun waitForRefresh() {
        mutex.withLock {}
    }

    private fun String.createNoMediaFile() {
        runCatching {
            val parent = Paths.get(this)
            Files.createDirectories(parent)
            val noMediaFile = parent.resolve(".nomedia")
            if (!noMediaFile.exists()) Files.createFile(noMediaFile)
        }.onFailure { e ->
            Logger.error(TAG, "Failed to create .nomedia file in $this", e)
        }
    }

    /**
     * 查找是否存在指定id的项
     */
    fun containsId(id: String): Boolean = _gamePathData.value.any { it.id == id }

    /**
     * 查找是否存在指定path的项
     */
    fun containsPath(path: String): Boolean = _gamePathData.value.any { it.path == path }

    /**
     * 修改并保存指定目录的标题
     * @throws IllegalArgumentException 未找到匹配项
     */
    fun modifyTitle(path: GamePath, modifiedTitle: String) {
        if (!containsId(path.id)) throw IllegalArgumentException("Item with ID ${path.id} not found, unable to rename.")
        path.title = modifiedTitle
        savePath(path)
    }

    /**
     * 添加新的路径并保存
     * @throws IllegalArgumentException 当前添加的路径与现有项冲突
     */
    fun addNewPath(title: String, path: String) {
        if (containsPath(path)) throw IllegalArgumentException("The path conflicts with an existing item!")
        savePath(
            GamePath(id = generateUUID(), title = title, path = path)
        )
    }

    /**
     * 删除路径并保存
     */
    fun removePath(path: GamePath) {
        if (!containsId(path.id)) return
        deletePath(path)
    }

    /**
     * 保存为默认的游戏目录
     */
    fun saveDefaultPath(reloadVersions: Boolean = true) {
        saveCurrentPathUncheck(DEFAULT_ID, reloadVersions)
    }

    /**
     * 保存当前选择的路径
     * @throws IllegalStateException 未授予存储/管理所有文件权限
     * @throws IllegalArgumentException 未找到匹配项
     */
    fun saveCurrentPath(id: String, reloadVersions: Boolean = true) {
        if (canHandlePermission && !hasStoragePermission) throw IllegalStateException("Storage permissions are not granted")
        if (!containsId(id)) throw IllegalArgumentException("No match found!")
        saveCurrentPathUncheck(id, reloadVersions)
    }

    private fun saveCurrentPathUncheck(id: String, reloadVersions: Boolean) {
        if (currentGamePathId.getValue() == id) return
        currentGamePathId.save(id)
        refreshCurrentPath(reloadVersions)
    }

    private fun refreshCurrentPath(reloadVersions: Boolean) {
        val id = currentGamePathId.getValue()
        _gamePathData.value.find { it.id == id }?.let { item ->
            if (_currentPath.value == item.path) return //避免重复刷新
            val path = item.path
            _currentPath.update { path }
            path.createNoMediaFile()
            if (reloadVersions) {
                VersionsManager.refresh("GamePathManager.refreshCurrentPath")
            }
        } ?: saveCurrentPath(DEFAULT_ID, reloadVersions)
    }

    private fun generateUUID(): String {
        val uuid = UUID.randomUUID().toString()
        return if (containsId(uuid)) generateUUID()
        else uuid
    }

    private fun savePath(path: GamePath) {
        scope.launch {
            runCatching {
                gamePathDao.savePath(path)
                Logger.info(TAG, "Saved game path: ${path.path}")
            }.onFailure { e ->
                Logger.error(TAG, "Failed to save game path config!", e)
            }
            reloadPath()
        }
    }

    private fun deletePath(path: GamePath) {
        scope.launch {
            gamePathDao.deletePath(path)
            reloadPath()
        }
    }
}