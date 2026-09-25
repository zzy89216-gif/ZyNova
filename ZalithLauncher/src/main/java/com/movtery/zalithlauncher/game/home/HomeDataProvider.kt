/*
 * ZyNova Launcher
 * Copyright (C) 2025 ZyNova Contributors
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

package com.movtery.zalithlauncher.game.home

import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.game.version.multiplayer.AllServers
import com.movtery.zalithlauncher.game.version.multiplayer.ServerData
import com.movtery.zalithlauncher.game.version.saves.SaveData
import com.movtery.zalithlauncher.game.version.saves.parseLevelDatFile
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "HomeData"

/**
 * 主页上的一个本地世界
 */
data class HomeWorld(
    val instance: Version,
    val save: SaveData
) {
    val name: String get() = save.levelName ?: save.saveFile.name
}

/**
 * 主页上的一个已保存服务器
 */
data class HomeServer(
    val instance: Version,
    val server: ServerData
)

/**
 * 主页上的一个「游戏实例模块」
 *
 * 主页以版本为模块组织内容：每个模块就是一个已安装的游戏实例，
 * 模块内部直接展示该实例自己的本地世界与已保存服务器。
 *
 * @param instance 游戏实例
 * @param minecraftVersion 该实例的 Minecraft 版本
 * @param loaderName 该实例的模组加载器名称（原版为 null）
 * @param worlds 该实例的本地世界
 * @param servers 该实例的已保存服务器
 */
data class HomeInstance(
    val instance: Version,
    val minecraftVersion: String,
    val loaderName: String?,
    val worlds: List<HomeWorld>,
    val servers: List<HomeServer>
) {
    /** 该实例是否没有任何世界与服务器 */
    val isEmpty: Boolean get() = worlds.isEmpty() && servers.isEmpty()
}

/**
 * 主页统一数据访问接口
 *
 * 卡片式主页与自定义主页共用同一套数据入口，
 * 避免每个主页各自去扫描文件系统。
 *
 * 所有数据都在真正需要时才读取（按需加载），
 * 启动器启动时不会进行一次性的全盘扫描。
 */
object HomeDataProvider {
    /** 单个模块内最多展示的世界 / 服务器数量 */
    const val DEFAULT_LIMIT = 6

    /** 最多展示多少个实例模块，避免扫描过多实例 */
    private const val MAX_INSTANCES = 4

    /** 单个实例最多解析多少个存档目录（按修改时间取最近的） */
    private const val MAX_WORLDS_TO_SCAN = 8

    /**
     * 按「版本模块」组织的主页数据
     *
     * 以最近使用的实例为顺序，每个实例内部带上它自己的世界与服务器。
     */
    suspend fun instances(): List<HomeInstance> = withContext(Dispatchers.IO) {
        val versions = VersionsManager.versions.value
            .filter { it.isValid() }
            .sortedByDescending { version ->
                runCatching { version.getVersionPath().lastModified() }.getOrDefault(0L)
            }
            .take(MAX_INSTANCES)

        versions.map { instance ->
            val info = instance.getVersionInfo()
            HomeInstance(
                instance = instance,
                minecraftVersion = info?.minecraftVersion.orEmpty(),
                loaderName = info?.loaderInfo?.loader?.displayName,
                worlds = scanWorlds(instance),
                servers = loadServers(instance)
            )
        }
    }

    /**
     * 最近使用的 Minecraft 版本
     *
     * 以版本目录的最后修改时间近似"最近使用"。
     */
    suspend fun recentVersions(limit: Int = MAX_INSTANCES): List<Version> = withContext(Dispatchers.IO) {
        VersionsManager.versions.value
            .filter { it.isValid() }
            .sortedByDescending { version ->
                runCatching { version.getVersionPath().lastModified() }.getOrDefault(0L)
            }
            .take(limit)
    }

    /**
     * 本地世界（按上次游玩时间排序）
     */
    suspend fun recentWorlds(limit: Int = DEFAULT_LIMIT): List<HomeWorld> = withContext(Dispatchers.IO) {
        val instance = activeInstance() ?: return@withContext emptyList()
        scanWorlds(instance, limit)
    }

    /**
     * 已保存的服务器
     */
    suspend fun savedServers(limit: Int = DEFAULT_LIMIT): List<HomeServer> = withContext(Dispatchers.IO) {
        val instance = activeInstance() ?: return@withContext emptyList()
        loadServers(instance, limit)
    }

    /**
     * 当前活跃实例：优先当前选中版本，否则取第一个有效版本
     */
    private fun activeInstance(): Version? =
        VersionsManager.currentVersion.value?.takeIf { it.isValid() }
            ?: VersionsManager.versions.value.firstOrNull { it.isValid() }

    /**
     * 读取某个实例已保存的服务器
     */
    private suspend fun loadServers(
        instance: Version,
        limit: Int = DEFAULT_LIMIT
    ): List<HomeServer> {
        return runCatching {
            val dataFile = File(instance.getGameDir(), "servers.dat")
            if (!dataFile.isFile) return emptyList()

            val allServers = AllServers()
            allServers.loadServers(dataFile)
            allServers.serverList.take(limit).map { HomeServer(instance, it) }
        }.getOrElse { e ->
            Logger.warning(TAG, "Failed to read saved servers of ${instance.getVersionName()}.", e)
            emptyList()
        }
    }

    /**
     * 扫描某个实例下的本地世界
     *
     * 按目录修改时间先筛出最近改动过的若干存档，
     * 避免为了展示几张卡片而解析用户全部的存档数据。
     */
    private suspend fun scanWorlds(
        instance: Version,
        limit: Int = DEFAULT_LIMIT
    ): List<HomeWorld> {
        val savesDir = File(instance.getGameDir(), "saves")
        if (!savesDir.isDirectory) return emptyList()

        val dirs = savesDir.listFiles()
            ?.filter { it.isDirectory }
            ?.sortedByDescending { it.lastModified() }
            ?.take(MAX_WORLDS_TO_SCAN)
            ?: return emptyList()

        val worlds = mutableListOf<HomeWorld>()
        dirs.forEach { dir ->
            runCatching {
                val data = parseLevelDatFile(
                    saveFile = dir,
                    levelDatFile = File(dir, "level.dat"),
                    worldGenDatFile = File(dir, "data/minecraft/world_gen_settings.dat")
                        .takeIf { it.isFile && it.exists() }
                )
                worlds.add(HomeWorld(instance, data))
            }.onFailure { e ->
                Logger.debug(TAG, "Skipped an unreadable world: ${dir.name} (${e.message})")
            }
        }

        return worlds
            .sortedByDescending { it.save.lastPlayed ?: 0L }
            .take(limit)
    }
}
