/*
 * ZyNova Launcher
 * Copyright (C) 2025-2026 ZyNova Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.game.download

import com.google.gson.Gson
import com.movtery.zalithlauncher.game.download.resources.ResourceDownloadManager
import com.movtery.zalithlauncher.game.path.getVersionsHome
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.logging.Logger
import com.tencent.mmkv.MMKV
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.Collections

private const val TAG = "InstallJournal"

/**
 * 一条「游戏版本安装」的落盘记录
 *
 * 为什么需要它：安装流程会把最终的 `<版本名>.json` 先写进 versions 目录，
 * 之后才拷贝 libraries、jar；而覆盖安装一开始就会删掉旧的 json/jar。
 * 进程被系统杀掉时，这些中间态只存在于内存里，下次启动就会看到
 * 「版本在列表里、但一启动就崩」或者「旧版本直接消失」。
 * 所以安装开始时把关键信息写进 MMKV，成功/失败/取消时清掉；
 * 下次启动如果读到记录，就说明上次安装被中断了，按记录做回滚。
 */
data class GameInstallJournal(
    /** 本次安装的目标版本名（versions/<名称>） */
    val targetVersionName: String,
    /** 是否覆盖安装 */
    val overwrite: Boolean,
    /**
     * 目标版本目录是否为本次安装新建。
     * 为 true 时恢复可以直接删除这个半成品目录（它本来就不存在）；
     * 为 false 时（覆盖安装 / 更新加载器）只能恢复旧 json/jar 备份，不能删目录里用户自己的文件。
     */
    val deleteTargetOnRecovery: Boolean,
    /** 旧版本 json 的备份路径（仅覆盖/更新时存在） */
    val backupJsonPath: String? = null,
    /** 旧版本 jar 的备份路径（仅覆盖/更新时存在） */
    val backupJarPath: String? = null,
    val startedAt: Long = System.currentTimeMillis()
)

/**
 * 一条「整合包安装」的落盘记录
 *
 * 整合包安装分两段：先把游戏主体装成版本（这一段由 [GameInstallJournal] 记录），
 * 再把整合包临时目录里的模组/配置拷进版本目录。第二段发生在版本已经可用的**之后**，
 * 进程被杀会留下「版本能启动、但模组只拷了一半」的假状态，用户几乎不可能自己发现，
 * 只会觉得游戏莫名其妙崩溃。
 *
 * 检测方式：所有整合包格式的最终任务都会在结束时清掉临时目录，所以
 * 「记录还在 + 临时目录还在」= 没装完；「记录还在 + 临时目录没了」= 其实已经装完，只清记录。
 */
data class ModpackInstallJournal(
    /** 临时整合包目录（所有整合包格式共用） */
    val tempModpackDirPath: String,
    /** 目标版本名（用户输入名字之后才有） */
    val targetVersionName: String? = null,
    val startedAt: Long = System.currentTimeMillis()
)

/** 启动时对中断安装执行的动作，投给界面做提示 */
enum class InstallRecoveryResult {
    /** 新装的版本没装完，半成品目录已删除 */
    GAME_CLEANED,
    /** 覆盖安装没装完，旧版本 json/jar 已恢复 */
    GAME_RESTORED,
    /** 覆盖安装没装完，但旧文件备份缺失/为空，没法自动恢复，需要用户重新安装 */
    GAME_RESTORE_FAILED,
    /** 整合包没装完，半成品已清理，需要重新安装 */
    MODPACK_CLEANED
}

/**
 * 下载/安装链路的「落盘状态 + 启动恢复」
 *
 * 只负责两件事：把正在进行的安装记下来、下次启动时把上次没装完的东西收拾干净。
 * 不参与正常安装流程的成功路径（成功时只清记录），也不引入任何新的后台框架。
 *
 * ⚠️ 恢复动作只允许操作 journal 里记录、且位于已知目录内的路径：
 * versions/<记录的名字>、launcher 自己的 temp_game / temp_modpack、cache 里的下载临时文件。
 * 任何一条路径校验不通过就跳过并只清记录，绝不按记录里的字符串乱删。
 */
object InstallJournal {
    private const val MMKV_ID = "DownloadInstallJournal"
    private const val KEY_GAME = "game_install_journal"
    private const val KEY_MODPACK = "modpack_install_journal"
    private const val BACKUP_DIR_NAME = "download_install_backup"

    private val gson = Gson()

    /** 进程内只做一次启动恢复：Activity 旋转/重建时会再次进入 onResume，不能重复回滚 */
    @Volatile
    private var recovered = false

    private fun mmkv(): MMKV = MMKV.mmkvWithID(MMKV_ID, MMKV.SINGLE_PROCESS_MODE)

    // ------------------------------------------------------------ 路径白名单

    private fun File.canonicalOrNull(): File? = runCatching { canonicalFile }.getOrNull()

    /**
     * 规范化后的 versions 目录。
     *
     * 额外校验目录名必须是 `versions`：游戏目录路径还没初始化完整时
     * `getVersionsHome()` 可能返回 `/versions` 这种根路径，绝不能拿它当白名单去删东西。
     */
    private fun versionsHomeCanonical(): File? {
        val home = runCatching { File(getVersionsHome()).canonicalFile }.getOrNull() ?: return null
        if (home.name != "versions") return null
        if (home.path.length <= "/versions".length) return null
        return home
    }
    private fun backupDir(): File = File(PathManager.DIR_FILES_PRIVATE, BACKUP_DIR_NAME)

    /**
     * 只接受 versions 目录的**直接子目录**，且名字里不能有路径分隔符。
     */
    private fun safeVersionDir(versionName: String?): File? {
        if (versionName.isNullOrBlank()) return null
        if (versionName.contains('/') || versionName.contains('\\')) return null
        if (versionName == "." || versionName == "..") return null
        val versionsHome = versionsHomeCanonical() ?: return null
        val dir = File(versionsHome, versionName).canonicalOrNull() ?: return null
        return dir.takeIf { it.parentFile?.path == versionsHome.path }
    }

    /**
     * 只接受安装前备份到私有目录里的文件。
     */
    private fun safeBackupFile(path: String?): File? {
        val file = path?.let { File(it) }?.canonicalOrNull() ?: return null
        val root = backupDir().canonicalOrNull() ?: return null
        return file.takeIf { it.parentFile?.path == root.path }
    }

    /**
     * 只接受 launcher 自己的整合包临时目录。
     */
    private fun safeTempModpackDir(path: String?): File? {
        val dir = path?.let { File(it) }?.canonicalOrNull() ?: return null
        val allowed = PathManager.DIR_CACHE_MODPACK_DOWNLOADER.canonicalOrNull() ?: return null
        return dir.takeIf { it.path == allowed.path }
    }

    private fun File.isInside(parent: File?): Boolean {
        val child = canonicalOrNull() ?: return false
        val root = parent?.canonicalOrNull() ?: return false
        return child.path == root.path || child.path.startsWith(root.path + File.separator)
    }

    // ---------------------------------------------------------------- 游戏版本

    /**
     * 开始一次游戏版本安装前写入记录
     *
     * @param deleteTargetOnRecovery 目标版本目录是否由本次安装新建（新装为 true）
     */
    fun beginGameInstall(
        targetVersionName: String,
        overwrite: Boolean,
        deleteTargetOnRecovery: Boolean,
        backupJsonPath: String? = null,
        backupJarPath: String? = null
    ) {
        runCatching {
            //上一次的记录如果还留着（理论上不该出现），先把它引用的备份清掉，避免越积越多
            readGameInstall()?.let { deleteBackupFiles(it) }
            val journal = GameInstallJournal(
                targetVersionName = targetVersionName,
                overwrite = overwrite,
                deleteTargetOnRecovery = deleteTargetOnRecovery,
                backupJsonPath = backupJsonPath,
                backupJarPath = backupJarPath
            )
            mmkv().putString(KEY_GAME, gson.toJson(journal))
            Logger.info(TAG, "Game install journal written: $targetVersionName, overwrite=$overwrite")
        }.onFailure {
            //记录失败不能影响正常安装
            Logger.warning(TAG, "Failed to write the game install journal", it)
        }
    }

    /**
     * 游戏版本安装成功/已回滚，清掉记录与备份
     */
    fun clearGameInstall() {
        runCatching {
            readGameInstall()?.let { deleteBackupFiles(it) }
            mmkv().removeValueForKey(KEY_GAME)
        }.onFailure {
            Logger.warning(TAG, "Failed to clear the game install journal", it)
        }
    }

    /**
     * 安装出错/取消时，按记录回滚：
     * - 新装：删掉半成品版本目录（它本来就不该存在）
     * - 覆盖/更新：只有旧 json/jar 备份**都在且非空**时才放回去，否则不动目标目录
     *
     * 与启动恢复共用同一套判断，保证「进程内失败」和「进程被杀」结果一致。
     */
    fun rollbackInterruptedGameInstall(): InstallRecoveryResult? {
        val journal = readGameInstall() ?: return null
        val result = runCatching { applyGameRollback(journal) }.onFailure {
            Logger.warning(TAG, "Failed to roll back the interrupted game install", it)
        }.getOrNull()
        clearGameInstall()
        return result
    }

    private fun applyGameRollback(journal: GameInstallJournal): InstallRecoveryResult? {
        val targetDir = safeVersionDir(journal.targetVersionName) ?: run {
            Logger.warning(
                TAG,
                "Skipped the game rollback: ${journal.targetVersionName} is not a safe version directory"
            )
            return null
        }

        if (journal.deleteTargetOnRecovery) {
            if (!targetDir.exists()) return null
            FileUtils.deleteQuietly(targetDir)
            Logger.info(TAG, "Removed the unfinished version directory: ${targetDir.absolutePath}")
            return InstallRecoveryResult.GAME_CLEANED
        }

        //覆盖安装 / 更新加载器：只有两份备份都在且非空才恢复
        val jsonBackup = safeBackupFile(journal.backupJsonPath)
        val jarBackup = safeBackupFile(journal.backupJarPath)
        if (jsonBackup == null || !jsonBackup.exists() || jsonBackup.length() <= 0L ||
            jarBackup == null || !jarBackup.exists() || jarBackup.length() <= 0L
        ) {
            Logger.warning(
                TAG,
                "The backup files of ${journal.targetVersionName} are missing or empty; skipped restoring"
            )
            return InstallRecoveryResult.GAME_RESTORE_FAILED
        }

        targetDir.mkdirs()
        val targetJson = File(targetDir, "${journal.targetVersionName}.json")
        val targetJar = File(targetDir, "${journal.targetVersionName}.jar")
        FileUtils.deleteQuietly(targetJson)
        FileUtils.moveFile(jsonBackup, targetJson)
        FileUtils.deleteQuietly(targetJar)
        FileUtils.moveFile(jarBackup, targetJar)
        Logger.info(TAG, "Restored the previous version files of ${journal.targetVersionName}")
        return InstallRecoveryResult.GAME_RESTORED
    }

    /**
     * 把目标版本目录里的 json/jar 备份到私有目录（不会被系统当缓存清掉），返回备份路径。
     * 覆盖安装/更新加载器时，旧文件马上会被删掉或覆盖，所以必须先备份。
     */
    fun backupVersionFiles(targetJson: File?, targetJar: File?): Pair<String?, String?> {
        val dir = backupDir()
        if (!dir.exists() && !dir.mkdirs()) {
            Logger.warning(TAG, "Unable to create the install backup directory: ${dir.absolutePath}")
            return null to null
        }
        fun backup(source: File?): String? = runCatching {
            if (source == null || !source.exists() || !source.isFile) return null
            val backup = File(dir, "${source.name}.bak")
            FileUtils.deleteQuietly(backup)
            source.copyTo(backup, overwrite = true)
            backup.absolutePath
        }.onFailure {
            Logger.warning(TAG, "Failed to back up ${source?.absolutePath} before installing", it)
        }.getOrNull()

        return backup(targetJson) to backup(targetJar)
    }

    private fun deleteBackupFiles(journal: GameInstallJournal) {
        safeBackupFile(journal.backupJsonPath)?.let { FileUtils.deleteQuietly(it) }
        safeBackupFile(journal.backupJarPath)?.let { FileUtils.deleteQuietly(it) }
    }

    private fun readGameInstall(): GameInstallJournal? = runCatching {
        mmkv().getString(KEY_GAME, null)?.let { text ->
            gson.fromJson(text, GameInstallJournal::class.java)
        }
    }.onFailure {
        Logger.warning(TAG, "Failed to read the game install journal", it)
    }.getOrNull()

    // ---------------------------------------------------------------- 整合包

    /**
     * 整合包安装开始（此时还没拿到目标版本名）
     */
    fun beginModpackInstall(tempModpackDirPath: String) {
        runCatching {
            val journal = ModpackInstallJournal(tempModpackDirPath = tempModpackDirPath)
            mmkv().putString(KEY_MODPACK, gson.toJson(journal))
            Logger.info(TAG, "Modpack install journal written.")
        }.onFailure {
            Logger.warning(TAG, "Failed to write the modpack install journal", it)
        }
    }

    /**
     * 用户确认了预安装版本名：补写目标版本名，恢复时才能定位半成品版本
     */
    fun updateModpackTargetVersion(targetVersionName: String) {
        runCatching {
            val journal = readModpackInstall() ?: return
            if (journal.targetVersionName == targetVersionName) return
            mmkv().putString(
                KEY_MODPACK,
                gson.toJson(journal.copy(targetVersionName = targetVersionName))
            )
        }.onFailure {
            Logger.warning(TAG, "Failed to update the modpack install journal", it)
        }
    }

    fun clearModpackInstall() {
        runCatching {
            mmkv().removeValueForKey(KEY_MODPACK)
        }.onFailure {
            Logger.warning(TAG, "Failed to clear the modpack install journal", it)
        }
    }

    /**
     * 整合包安装出错/取消时干净回滚。
     *
     * 不做「续传」：不同整合包格式的最终配置字段（jvm 参数 / 服务器 / 内存 / 图标）各不相同，
     * 续传要按格式重建配置，风险高、收益低；宁可干净清掉、提示用户重装，也不留下半吊子的整合包。
     */
    fun rollbackInterruptedModpackInstall(): InstallRecoveryResult? {
        val journal = readModpackInstall() ?: return null
        val result = runCatching { applyModpackRollback(journal) }.onFailure {
            Logger.warning(TAG, "Failed to roll back the interrupted modpack install", it)
        }.getOrNull()
        clearModpackInstall()
        //整合包安装过程中嵌套的游戏安装也可能留下了记录
        clearGameInstall()
        return result
    }

    private fun applyModpackRollback(journal: ModpackInstallJournal): InstallRecoveryResult? {
        val tempDir = safeTempModpackDir(journal.tempModpackDirPath) ?: run {
            Logger.warning(TAG, "Skipped the modpack rollback: unsafe temp directory")
            return null
        }
        //所有整合包格式的最终任务都会在结束时清掉临时目录：
        //临时目录还在 = 安装没走完；已经没了 = 上次其实装完了，只需要清记录
        if (!tempDir.exists()) return null

        //只删「确实是这次安装创建/改写」的版本目录：
        //如果同名版本有更早的 json（说明是用户已有版本），不能删
        val versionDir = safeVersionDir(journal.targetVersionName)
        if (versionDir != null && versionDir.exists()) {
            val jsonFile = File(versionDir, "${journal.targetVersionName}.json")
            val createdByThisInstall = !jsonFile.exists() || jsonFile.lastModified() >= journal.startedAt
            if (createdByThisInstall) {
                FileUtils.deleteQuietly(versionDir)
                Logger.info(TAG, "Removed the unfinished modpack version: ${versionDir.absolutePath}")
            } else {
                Logger.warning(
                    TAG,
                    "Skipped deleting ${versionDir.absolutePath}: it looks like an existing version"
                )
            }
        }

        FileUtils.deleteQuietly(tempDir)
        Logger.info(TAG, "Removed the unfinished modpack temp directory: ${tempDir.absolutePath}")
        return InstallRecoveryResult.MODPACK_CLEANED
    }

    private fun readModpackInstall(): ModpackInstallJournal? = runCatching {
        mmkv().getString(KEY_MODPACK, null)?.let { text ->
            gson.fromJson(text, ModpackInstallJournal::class.java)
        }
    }.onFailure {
        Logger.warning(TAG, "Failed to read the modpack install journal", it)
    }.getOrNull()

    // ---------------------------------------------------------------- 启动恢复

    /**
     * 进程启动后调用一次：处理上次被杀留下的中断安装。
     *
     * 只有进程内第一次调用会真正执行（Activity 重建/旋转不会重复回滚）。
     * 需要在外部确认存储权限已就绪后再调用，否则删不掉也读不了游戏目录。
     * 内部逐项 try/catch：任何一步失败都只记日志并清记录，绝不让恢复流程影响启动。
     */
    fun recoverOncePerProcess(): List<InstallRecoveryResult> {
        if (recovered) return emptyList()
        synchronized(this) {
            if (recovered) return emptyList()
            recovered = true
        }

        return runCatching {
            Logger.info(TAG, "Checking for interrupted downloads/installs…")
            val results = Collections.synchronizedList(mutableListOf<InstallRecoveryResult>())

            //① 游戏版本安装：先处理，因为它可能已经写出了一个假版本
            runCatching { rollbackInterruptedGameInstall() }
                .onFailure { Logger.warning(TAG, "Game install recovery failed", it) }
                .getOrNull()
                ?.let { results.add(it) }

            //② 整合包安装：清理临时目录与半成品版本
            runCatching { rollbackInterruptedModpackInstall() }
                .onFailure { Logger.warning(TAG, "Modpack install recovery failed", it) }
                .getOrNull()
                ?.let { results.add(it) }

            //③ launcher 自己的临时目录与 cache 里的下载半成品
            cleanStaleTempFiles()

            results.toList()
        }.onFailure {
            Logger.warning(TAG, "Failed to recover interrupted installs", it)
        }.getOrDefault(emptyList())
    }

    /**
     * 清理上次下载中断留下的临时文件
     *
     * ⚠️ 只清 launcher 自己的目录：cache/resources 下的 `.part`、以及 temp_game / temp_modpack。
     * 不碰用户游戏目录、不碰 cache 里其它模块的文件；共享 libraries/assets 里的 `.part`
     * 下次下载会覆盖，且扫描成本高，这里不清。
     */
    private fun cleanStaleTempFiles() {
        runCatching {
            val cacheRoot = PathManager.DIR_CACHE.canonicalOrNull()
            val resourcesCache = File(PathManager.DIR_CACHE, "resources")
            if (cacheRoot != null && resourcesCache.isInside(cacheRoot)) {
                ResourceDownloadManager.cleanupTempFiles(resourcesCache)
            }
            PathManager.DIR_CACHE_GAME_DOWNLOADER
                .takeIf { cacheRoot != null && it.isInside(cacheRoot) }
                ?.let { FileUtils.deleteQuietly(it) }
            PathManager.DIR_CACHE_MODPACK_DOWNLOADER
                .takeIf { cacheRoot != null && it.isInside(cacheRoot) }
                ?.let { FileUtils.deleteQuietly(it) }
        }.onFailure {
            Logger.warning(TAG, "Failed to clean the stale temporary files", it)
        }
    }
}
