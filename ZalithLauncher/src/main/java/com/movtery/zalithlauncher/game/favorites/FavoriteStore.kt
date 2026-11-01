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

package com.movtery.zalithlauncher.game.favorites

import com.movtery.zalithlauncher.path.PathManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * 本地归档收藏的存储（议题 #39）
 *
 * 存储布局（全部在应用私有目录里，系统卸载应用时一起清理）：
 * ```
 * files/favorites/
 *   ├── index.json          # 收藏记录（本文件）
 *   └── files/<sha256>-<名字> # 真正归档的文件副本
 * ```
 *
 * 三条设计约束：
 * 1. **只存相对文件名**，不存绝对路径 —— 应用数据被迁移时绝对路径会失效；
 * 2. **在线收藏与本地归档分开存**：删归档不动在线收藏，取消在线收藏也不删文件；
 * 3. **写入先写临时文件再改名**，避免进程被杀时留下半个 index.json。
 */
object FavoriteStore {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }
    private val mutex = Mutex()

    /** 收藏根目录 */
    fun rootDir(): File = File(PathManager.DIR_FILES_PRIVATE, "favorites").apply { mkdirs() }

    /** 归档文件目录 */
    fun filesDir(): File = File(rootDir(), "files").apply { mkdirs() }

    private fun indexFile(): File = File(rootDir(), "index.json")

    /** 某条归档文件对应的实际文件 */
    fun fileOf(entry: FavoriteEntry): File = File(filesDir(), entry.fileName)

    suspend fun list(): List<FavoriteEntry> = withContext(Dispatchers.IO) {
        mutex.withLock { readLocked() }
    }

    /** 总占用（字节），用于「存储空间」展示 */
    suspend fun totalSize(): Long = withContext(Dispatchers.IO) {
        mutex.withLock { readLocked().sumOf { it.size } }
    }

    /** 按 SHA-256 找已有归档；用于导入去重 */
    suspend fun findBySha256(sha256: String): FavoriteEntry? = withContext(Dispatchers.IO) {
        mutex.withLock { readLocked().firstOrNull { it.sha256 == sha256 } }
    }

    suspend fun add(entry: FavoriteEntry) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val all = readLocked().toMutableList()
            all.removeAll { it.id == entry.id }
            all.add(entry)
            writeLocked(all)
        }
    }

    /** 只删除归档记录与文件，不触碰任何在线收藏信息 */
    suspend fun delete(id: String): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            val all = readLocked().toMutableList()
            val target = all.firstOrNull { it.id == id } ?: return@withLock false
            all.remove(target)
            writeLocked(all)
            runCatching { fileOf(target).delete() }
            true
        }
    }

    /** 记录「同时存在在线收藏」的信息；不会因为取消在线收藏而删本地文件 */
    suspend fun attachOnlineInfo(id: String, projectId: String, platform: String) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val all = readLocked().toMutableList()
            val index = all.indexOfFirst { it.id == id }
            if (index < 0) return@withLock
            all[index] = all[index].copy(onlineProjectId = projectId, onlinePlatform = platform)
            writeLocked(all)
        }
    }

    private fun readLocked(): List<FavoriteEntry> {
        val file = indexFile()
        if (!file.isFile) return emptyList()
        return runCatching {
            json.decodeFromString<List<FavoriteEntry>>(file.readText())
        }.getOrElse { emptyList() }
    }

    private fun writeLocked(entries: List<FavoriteEntry>) {
        val target = indexFile()
        val temp = File(target.parentFile, "${target.name}.tmp")
        runCatching {
            temp.writeText(json.encodeToString(entries))
            if (target.exists()) target.delete()
            temp.renameTo(target)
        }
    }
}
