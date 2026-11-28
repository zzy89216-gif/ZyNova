/*
 * ZyNova Launcher
 * Copyright (C) 2026 ZyNova contributors
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

import com.google.gson.JsonObject
import com.movtery.zalithlauncher.game.versioninfo.MinecraftVersions
import com.movtery.zalithlauncher.game.versioninfo.models.VersionManifest
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "AutoGraphicsApi"

/**
 * 图形 API 的自动选择（27.2.0 起不再向用户提供手动选择）
 *
 * 规则：
 * 1. 某个实例**第一次启动**时使用 OpenGL（最保守、兼容性最好）；
 * 2. 之后**完全跟随 Minecraft 游戏自身**保存的图形 API 设置，启动器不再覆盖；
 * 3. 每次启动都会检查当前版本的版本信息（只读本地，缺失时才回退到版本清单）；
 * 4. 该版本是否需要写入这个选项，由**版本发布时间**判断，**不写死任何版本号**。
 *
 * 之所以要判断「这个版本处于哪个时代」：`preferredGraphicsBackend` 是
 * Minecraft 引入可选图形后端之后才存在的选项，往更老的版本里写它没有任何意义，
 * 因此直接跳过，保持 options.txt 干净。
 */
object AutoGraphicsApi {
    /** Minecraft 选项中记录图形后端的键名 */
    const val OPTION_KEY = "preferredGraphicsBackend"

    /**
     * 首个带有 Vulkan 后端的版本发布时间：Minecraft 26.2-snapshot-1（数据版本 4883）
     *
     * 判定一律基于「版本发布时间」，而不是写死版本号 —— 这样以后 Mojang 再怎么
     * 调整版本命名（快照 / 预览 / 正式版命名规则变化）都不会影响判断。
     */
    const val VULKAN_BACKEND_SINCE_DATE = "2026-04-07"

    /**
     * 计算「首次启动」应当写入的图形 API 值。
     *
     * @return 需要写入的值；返回 `null` 表示这个版本不需要（也不应该）写入该选项
     */
    suspend fun firstLaunchOption(version: Version): String? {
        return if (version.supportsGraphicsBackendOption()) GraphicsApi.OPENGL.option else null
    }

    /**
     * 该版本所处的时代是否存在「可选的图形后端」（即是否处于 Vulkan 时代）。
     *
     * 优先按版本发布时间判断；发布时间实在拿不到时，退回读取客户端 Jar 的
     * 数据版本（[hasVulkanBackend]，同样不写死版本号，且完全离线）。
     */
    suspend fun Version.supportsGraphicsBackendOption(): Boolean {
        val releaseDate = getReleaseDate()
        if (releaseDate != null) return releaseDate >= VULKAN_BACKEND_SINCE_DATE
        return hasVulkanBackend()
    }

    /**
     * 获取该版本的发布时间（`yyyy-MM-dd`）。
     *
     * 依次尝试：已安装版本自己的版本 Json → 本地缓存的 Minecraft 版本清单。
     * **全程只读本地文件**，不会因为网络问题拖慢游戏启动。
     */
    suspend fun Version.getReleaseDate(): String? = withContext(Dispatchers.IO) {
        readLocalReleaseTime()?.toDateOnly() ?: readManifestReleaseTime()?.toDateOnly()
    }

    /**
     * 从已安装版本的版本 Json 中读取 `releaseTime`
     */
    private fun Version.readLocalReleaseTime(): String? = runCatching {
        val manifestFile = File(getVersionPath(), "${getVersionName()}.json")
        if (!manifestFile.exists() || !manifestFile.isFile) return@runCatching null
        GSON.fromJson(manifestFile.readText(), JsonObject::class.java)
            ?.get("releaseTime")?.asString
    }.onFailure {
        Logger.warning(TAG, "Failed to read releaseTime from the version Json", it)
    }.getOrNull()

    /**
     * 从 Minecraft 版本清单中读取该版本的发布时间。
     *
     * 优先使用内存中已经加载好的列表；没有加载过时直接读本地缓存文件，
     * 不会为了一个时间戳去联网。
     */
    private suspend fun Version.readManifestReleaseTime(): String? {
        val minecraftVersion = getVersionInfo()?.minecraftVersion ?: return null

        MinecraftVersions.allVersions.value
            .find { it.version.id == minecraftVersion }
            ?.let { return it.version.releaseTime }

        return runCatching {
            val manifestFile = PathManager.FILE_MINECRAFT_VERSIONS
            if (!manifestFile.exists() || !manifestFile.isFile) return@runCatching null
            GSON.fromJson(manifestFile.readText(), VersionManifest::class.java)
                ?.versions?.find { it.id == minecraftVersion }
                ?.releaseTime
        }.onFailure {
            Logger.warning(TAG, "Failed to read the local Minecraft version manifest", it)
        }.getOrNull()
    }
}

/**
 * 从 ISO-8601 时间戳（如 `2026-04-07T11:52:43+00:00`）里取出日期部分 `yyyy-MM-dd`。
 *
 * 只取日期部分是刻意的：Minecraft 各版本 Json 里的时区偏移并不统一
 * （有 `+00:00`、也有 `+08:00`），比较「哪一天发布」既足够又不受时区写法影响。
 */
private fun String.toDateOnly(): String? = take(10).takeIf { date ->
    date.length == 10 && date[4] == '-' && date[7] == '-'
}
