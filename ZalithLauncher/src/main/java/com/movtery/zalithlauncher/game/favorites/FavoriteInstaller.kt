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
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformVersion
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
    /** true = 平台认出来了，但文件元数据不完整，前置列表读不到（文件本身已装好） */
    val metadataIncomplete: Boolean = false,
)

/**
 * 安装前的兼容性检查结果
 *
 * 收藏页用它回答「这个收藏文件能不能装进目标实例」：
 * 平台没认出文件、或实例信息读不出来时，各字段保持 null / 空，
 * 界面退回到「直接安装」的老行为，**绝不阻断**用户。
 *
 * @param recognized 平台是否认出了这个文件（哈希反查成功且文件信息已初始化）
 * @param displayName 平台上的版本显示名
 * @param gameVersions 该文件在平台上标注支持的 Minecraft 版本
 * @param instanceMinecraftVersion 目标实例的 Minecraft 版本
 * @param compatibleWithInstance 是否与目标实例兼容；null = 无法判断
 */
data class FavoriteInstallPrecheck(
    val recognized: Boolean,
    val displayName: String? = null,
    val gameVersions: List<String> = emptyList(),
    val instanceMinecraftVersion: String? = null,
    val compatibleWithInstance: Boolean? = null,
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
        val platformVersion = lookupInitializedPlatformVersion(entry, source)

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

        //⚠️ 依赖列表来自平台版本对象：必须放在 runCatching 里。
        //历史崩溃：CurseForge 按文件哈希反查回来的版本没有初始化主要文件，
        //直接读 platformDependencies() 会抛 `lateinit property thisPrimaryFile has not been initialized`，
        //异常一路冒到界面就是一条「安装失败：lateinit property ...」的未知错误。
        val dependencies = runCatching { platformVersion.platformDependencies() }
            .onFailure { th ->
                //文件本身已经放进去了，只是读不到前置列表：明确降级，不影响主资源
                Logger.warning(TAG, "Failed to read dependencies of ${entry.name}", th)
            }
            .getOrNull()

        if (dependencies == null) {
            return@withContext FavoriteInstallResult(
                mainInstalled = true,
                metadataIncomplete = true
            )
        }

        dependencies
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

    /**
     * 安装前检查：这个收藏文件能不能装进目标实例
     *
     * 只做「读平台元数据 + 比对游戏版本」，不写任何文件，也不下载任何东西。
     * 收藏页用它先给用户一句「该文件没有适用于 1.20.1 的版本」，
     * 而不是等安装完了才在游戏里加载失败。
     *
     * @return 无法判断时 `compatibleWithInstance` 为 null，调用方应照常安装
     */
    suspend fun precheck(
        entry: FavoriteEntry,
        instance: Version,
    ): FavoriteInstallPrecheck = withContext(Dispatchers.IO) {
        val instanceMcVersion = instance.getVersionInfo()?.minecraftVersion
        val source = FavoriteStore.fileOf(entry)
        if (!source.isFile) {
            return@withContext FavoriteInstallPrecheck(
                recognized = false,
                instanceMinecraftVersion = instanceMcVersion
            )
        }

        val platformVersion = lookupInitializedPlatformVersion(entry, source)
            ?: return@withContext FavoriteInstallPrecheck(
                recognized = false,
                instanceMinecraftVersion = instanceMcVersion
            )

        val gameVersions = runCatching { platformVersion.platformGameVersion().toList() }
            .onFailure { Logger.warning(TAG, "Failed to read game versions of ${entry.name}", it) }
            .getOrDefault(emptyList())
        val displayName = runCatching { platformVersion.platformDisplayName() }
            .getOrNull()
        val compatible = if (gameVersions.isEmpty() || instanceMcVersion.isNullOrBlank()) {
            null //平台没标游戏版本 / 实例信息读不出来：不阻断，交由安装流程判断
        } else {
            gameVersions.any { it.trim().equals(instanceMcVersion, ignoreCase = true) }
        }

        FavoriteInstallPrecheck(
            recognized = true,
            displayName = displayName,
            gameVersions = gameVersions,
            instanceMinecraftVersion = instanceMcVersion,
            compatibleWithInstance = compatible
        )
    }

    /**
     * 把归档文件反查成平台版本，并**保证文件信息已初始化**。
     *
     * 为什么必须检查：`getVersionByLocalFile()` 返回的对象在旧实现里没有初始化
     * `thisPrimaryFile`，直接读 `platformDependencies()` / `platformDisplayName()`
     * 会抛 `UninitializedPropertyAccessException`（`lateinit property thisPrimaryFile
     * has not been initialized`）。引擎层现在会先 initFile，但这里再兜一次：
     * 初始化失败就当作「平台没认出这个文件」，让调用方走「直接复制」降级路径，
     * 绝不让异常冒到界面上变成一条看不懂的未知错误。
     */
    private suspend fun lookupInitializedPlatformVersion(
        entry: FavoriteEntry,
        source: File,
    ): PlatformVersion? {
        val sha1 = runCatching { sha1Of(source) }
            .onFailure { Logger.warning(TAG, "Failed to hash ${entry.name}", it) }
            .getOrNull() ?: return null
        val version = runCatching { getVersionByLocalFile(source, sha1) }
            .onFailure { Logger.warning(TAG, "Platform lookup failed for ${entry.name}", it) }
            .getOrNull() ?: return null

        if (version.isFileInitialized) return version

        val initialized = runCatching {
            version.initFile(entry.onlineProjectId.orEmpty())
        }.onFailure {
            Logger.warning(TAG, "Failed to initialize platform file of ${entry.name}", it)
        }.getOrDefault(false)
        return version.takeIf { initialized }
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
