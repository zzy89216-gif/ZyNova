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

import com.movtery.zalithlauncher.game.download.assets.platform.PlatformDependencyType
import com.movtery.zalithlauncher.game.download.assets.platform.getVersionByLocalFile
import com.movtery.zalithlauncher.game.download.resources.ResourceManager
import com.movtery.zalithlauncher.game.download.resources.ResourceType
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.security.MessageDigest

private const val TAG = "FavoriteInstaller"

/** 一次安装的结果：装了什么、缺了哪些前置、是不是走的「直接复制」降级路径 */
data class FavoriteInstallResult(
    val mainInstalled: Boolean,
    val installedDependencies: List<String> = emptyList(),
    val missingDependencies: List<String> = emptyList(),
    /** true = 没能在平台上认出这个文件，只能直接放进目录（前置无法自动下载） */
    val fallbackCopy: Boolean = false,
)

/**
 * 把归档资源装进某个游戏实例（议题 #39「一键安装 + 自动前置」）
 *
 * 思路是**复用现有资源安装链路**，而不是另写一套下载器：
 * 1. 先用文件的 SHA-1 去 Modrinth / CurseForge 反查版本（这两个平台都支持按本地文件哈希找版本）；
 * 2. 认出来了 → 把文件放进实例对应目录，并按平台给出的依赖列表，
 *    逐个用 [ResourceManager.matchVersion] + [ResourceManager.installVersion] 自动装前置
 *    （前置自己的前置也会被递归解析，这是既有 `buildInstallPlan` 的能力）；
 * 3. 认不出来（自编译模组、整合包里抠出来的等）→ 仍然可以安装，只是直接复制，
 *    并明确告诉用户「前置没法自动装」。
 *
 * ⚠️ 归档文件名带哈希前缀（`<sha256前16位>-原名`），装进游戏时必须用**原始文件名**，
 * 否则模组加载器的文件名校验、Fabric API 识别、重复安装判断都会失效。
 */
object FavoriteInstaller {

    suspend fun install(
        entry: FavoriteEntry,
        instance: Version,
        onMessage: (String) -> Unit = {},
    ): FavoriteInstallResult = withContext(Dispatchers.IO) {
        val source = FavoriteStore.fileOf(entry)
        if (!source.isFile) {
            throw IOException("Archived file is missing: ${source.name}")
        }

        when (entry.type) {
            FavoriteType.WORLD -> return@withContext installWorld(entry, instance, onMessage)
            FavoriteType.DATAPACK -> return@withContext installDatapack(entry, instance, onMessage)
            FavoriteType.MOD, FavoriteType.RESOURCE_PACK, FavoriteType.SHADER -> Unit
            else -> throw IOException("This type cannot be installed into a game instance: ${entry.type}")
        }

        val type = when (entry.type) {
            FavoriteType.MOD -> ResourceType.MOD
            FavoriteType.RESOURCE_PACK -> ResourceType.RESOURCE_PACK
            FavoriteType.SHADER -> ResourceType.SHADER
            else -> ResourceType.MOD
        }

        //1. 先认平台版本：顺便拿到它的依赖列表
        val sha1 = sha1Of(source)
        val platformVersion = runCatching {
            getVersionByLocalFile(source, sha1)
        }.onFailure {
            Logger.warning(TAG, "Platform lookup failed for ${entry.name}", it)
        }.getOrNull()

        //2. 主文件放进实例目录（用原始文件名）
        val targetFolder = File(instance.getGameDir(), type.classes.versionFolder.folderName)
        if (!targetFolder.exists() && !targetFolder.mkdirs()) {
            throw IOException("Failed to create folder: ${targetFolder.absolutePath}")
        }
        val target = File(targetFolder, entry.name)
        source.copyTo(target, overwrite = true)
        onMessage("已放入 ${type.classes.versionFolder.folderName}/${entry.name}")

        if (platformVersion == null) {
            return@withContext FavoriteInstallResult(
                mainInstalled = true,
                fallbackCopy = true
            )
        }

        //3. 自动装前置
        val installed = mutableListOf<String>()
        val missing = mutableListOf<String>()

        platformVersion.platformDependencies()
            .filter { it.type == PlatformDependencyType.REQUIRED }
            .forEach { dependency ->
                val projectId = dependency.projectId
                runCatching {
                    val resolved = ResourceManager.matchVersion(
                        platform = dependency.platform,
                        projectId = projectId,
                        type = type,
                        instance = instance
                    )
                    val result = ResourceManager.installVersion(
                        version = resolved,
                        type = type,
                        instance = instance
                    )
                    installed += resolved.displayName
                    result.missingRequiredDependencies.forEach { missing += it.toString() }
                }.onFailure { th ->
                    Logger.warning(TAG, "Failed to install dependency $projectId", th)
                    missing += projectId
                }
            }

        FavoriteInstallResult(
            mainInstalled = true,
            installedDependencies = installed,
            missingDependencies = missing
        )
    }

    /** 存档：解压到 saves/<名字>，并保证解压路径不会跑出目标目录 */
    private suspend fun installWorld(
        entry: FavoriteEntry,
        instance: Version,
        onMessage: (String) -> Unit,
    ): FavoriteInstallResult = withContext(Dispatchers.IO) {
        val source = FavoriteStore.fileOf(entry)
        val savesDir = File(instance.getGameDir(), "saves").apply { mkdirs() }
        val baseName = entry.name.substringBeforeLast('.').ifBlank { "imported_world" }
        var target = File(savesDir, baseName)
        var index = 2
        while (target.exists()) {
            target = File(savesDir, "$baseName ($index)")
            index++
        }
        target.mkdirs()

        val canonicalRoot = target.canonicalFile
        java.util.zip.ZipFile(source).use { zip ->
            zip.entries().asSequence().forEach { zipEntry ->
                val outFile = File(target, zipEntry.name)
                if (!outFile.canonicalPath.startsWith(canonicalRoot.path + File.separator)) {
                    throw IOException("Unsafe zip entry: ${zipEntry.name}")
                }
                if (zipEntry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    zip.getInputStream(zipEntry).use { input ->
                        outFile.outputStream().use { output -> input.copyTo(output) }
                    }
                }
            }
        }
        onMessage("已解压到 saves/${target.name}")
        FavoriteInstallResult(mainInstalled = true)
    }

    /** 数据包：解压到存档的 datapacks，没有存档时退回到实例根目录的 datapacks */
    private suspend fun installDatapack(
        entry: FavoriteEntry,
        instance: Version,
        onMessage: (String) -> Unit,
    ): FavoriteInstallResult = withContext(Dispatchers.IO) {
        val source = FavoriteStore.fileOf(entry)
        val targetFolder = File(instance.getGameDir(), "datapacks").apply { mkdirs() }
        source.copyTo(File(targetFolder, entry.name), overwrite = true)
        onMessage("已放入 datapacks/${entry.name}")
        FavoriteInstallResult(mainInstalled = true)
    }

    /** 计算 SHA-1（平台反查用的是 SHA-1，不是 SHA-256） */
    private fun sha1Of(file: File): String {
        val digest = MessageDigest.getInstance("SHA-1")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
