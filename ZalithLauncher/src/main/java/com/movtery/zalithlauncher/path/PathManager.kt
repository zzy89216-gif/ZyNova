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

package com.movtery.zalithlauncher.path

import android.content.Context
import android.os.Environment
import com.movtery.zalithlauncher.game.launch.LogName
import org.apache.commons.io.FileUtils
import java.io.File

class PathManager {
    companion object {
        /**
         * 公共存储下对外可见的根目录名。
         *
         * 维护者要求：游戏文件放到**文件管理器一打开就能看到**的地方，目录名用启动器的名字。
         * 所以是 `/storage/emulated/0/ZyNova`，不是 `Android/data/...`。
         *
         * ⚠️ 名字是**固定的**，不要跟着 `launcher_name` 变——用户改个显示名就把数据目录改了，
         * 等于把已有实例弄丢。
         */
        const val PUBLIC_GAME_DIR_NAME = "ZyNova"

        /**
         * 根目录里真正装游戏数据的那一层：`/storage/emulated/0/ZyNova/.Minecraft`。
         *
         * 维护者要求（28.1.3）：**公共目录里只留少量该露出来的东西**——
         * 日志（`logs`）和游戏数据（`.Minecraft`）。
         * 点开头的名字在文件管理器里默认不显示，用户不会一不小心把 `versions` 当成垃圾删掉，
         * 但真要去翻实例、改配置时又能进去。
         */
        const val PUBLIC_GAME_DIR_INNER = ".Minecraft"

        /** 根目录里放启动日志的那一层 */
        const val PUBLIC_LOG_DIR_NAME = "logs"

        lateinit var DIR_FILES_PRIVATE: File
        lateinit var DIR_FILES_EXTERNAL: File
        lateinit var DIR_CACHE: File
        lateinit var DIR_NATIVE_LIB: String
        var DIR_RUNTIME_MOD: File? = null

        lateinit var DIR_GAME: File
        lateinit var DIR_DATA_BASES: File
        lateinit var DIR_ACCOUNT_SKIN: File
        lateinit var DIR_ACCOUNT_CAPE: File
        lateinit var DIR_MULTIRT: File
        lateinit var DIR_JNA: File
        lateinit var DIR_COMPONENTS: File
        lateinit var DIR_MOUSE_POINTER: File
        lateinit var DIR_BACKGROUND: File
        lateinit var DIR_CACHE_GAME_DOWNLOADER: File
        lateinit var DIR_CACHE_MODPACK_DOWNLOADER: File
        lateinit var DIR_CACHE_MODPACK_EXPORTER: File
        lateinit var DIR_CACHE_MOD_UPDATER: File
        lateinit var DIR_CACHE_APP_ICON: File
        lateinit var DIR_CACHE_HOME_PAGE: File
        lateinit var DIR_LAUNCHER_LOGS: File
        lateinit var DIR_NATIVE_LOGS: File
        lateinit var DIR_IMAGE_CACHE: File
        lateinit var DIR_CONTROL_LAYOUTS: File
        lateinit var DIR_TERRACOTTA: File
        lateinit var DIR_STYLES: File

        lateinit var FILE_CRASH_REPORT: File
        lateinit var FILE_SETTINGS: File
        lateinit var FILE_MINECRAFT_VERSIONS: File
        lateinit var FILE_LAUNCHER_BACKGROUND: File
        lateinit var FILE_TERRACOTTA_LOG: File

        fun refreshPaths(context: Context) {
            DIR_FILES_PRIVATE = context.filesDir
            DIR_FILES_EXTERNAL = context.getExternalFilesDir(null) ?: run {
                //from FCL (commit 744156a)
                File(Environment.getExternalStorageDirectory(), "Android/data/${context.packageName}/files")
            }
            DIR_CACHE = context.cacheDir
            DIR_NATIVE_LIB = context.applicationInfo.nativeLibraryDir
            DIR_RUNTIME_MOD = context.getDir("runtime_mod", 0)

            DIR_DATA_BASES = File(DIR_FILES_PRIVATE.parentFile, "databases")
            //游戏数据优先放公共存储（文件管理器可见）；权限不足或迁移未完成时回退到私有目录
            DIR_GAME = resolveGameDir()
            DIR_ACCOUNT_SKIN = File(DIR_GAME, "account_skins")
            DIR_ACCOUNT_CAPE = File(DIR_GAME, "account_capes")
            //⚠️ Java 运行时与 JNA 必须留在**应用私有目录**：
            //公共存储（/storage/emulated/0）是 noexec 挂载，放那里的 .so 无法加载，
            //游戏会直接起不来。数据可以放公共目录，可执行文件不行。
            DIR_MULTIRT = File(DIR_FILES_PRIVATE, "runtimes")
            DIR_JNA = File(DIR_FILES_PRIVATE, "jna_dir")
            DIR_COMPONENTS = File(DIR_FILES_PRIVATE, "components")
            DIR_MOUSE_POINTER = File(DIR_FILES_PRIVATE, "mouse_pointer")
            DIR_BACKGROUND = File(DIR_FILES_PRIVATE, "background")
            DIR_CACHE_GAME_DOWNLOADER = File(DIR_CACHE, "temp_game")
            DIR_CACHE_MODPACK_DOWNLOADER = File(DIR_CACHE, "temp_modpack")
            DIR_CACHE_MODPACK_EXPORTER = File(DIR_CACHE, "temp_modpack_exporter")
            DIR_CACHE_MOD_UPDATER = File(DIR_CACHE, "temp_mod_updater")
            DIR_CACHE_APP_ICON = File(DIR_CACHE, "app_icons")
            DIR_CACHE_HOME_PAGE = File(DIR_CACHE, "remote_homepage")
            DIR_LAUNCHER_LOGS = resolveLauncherLogsDir()
            DIR_NATIVE_LOGS = File(DIR_LAUNCHER_LOGS, "native")
            DIR_IMAGE_CACHE = File(DIR_CACHE, "images")
            DIR_CONTROL_LAYOUTS = File(DIR_FILES_EXTERNAL, "control_layouts")
            DIR_TERRACOTTA = File(DIR_FILES_PRIVATE, "net.burningtnt.terracotta")
            DIR_STYLES = File(DIR_FILES_PRIVATE, "special_styles")

            FILE_CRASH_REPORT = File(DIR_LAUNCHER_LOGS, "launcher_crash.log")
            FILE_SETTINGS = File(DIR_FILES_PRIVATE, "settings.json")
            FILE_MINECRAFT_VERSIONS = File(DIR_GAME, "minecraft_versions.json")
            FILE_LAUNCHER_BACKGROUND = File(DIR_BACKGROUND, "background01.file")
            FILE_TERRACOTTA_LOG = File(DIR_FILES_EXTERNAL, "terracotta.log")

            //先搬运行时（此时目标目录还没被 createDirs 建出来），再处理公共目录的旧布局，
            //最后创建其余目录
            migrateRuntimesToPrivate()
            migratePublicFlatLayout()
            createDirs()
            handleLegacy()
        }

        /**
         * 旧的 Java 运行时 / JNA 曾经放在 `games/runtimes`、`games/jna_dir`。
         * 现在它们固定在私有目录，这里把旧位置的搬过来（同一分区，rename 很快）。
         */
        private fun migrateRuntimesToPrivate() {
            runCatching {
                val legacyGame = legacyGameDir()
                listOf(
                    File(legacyGame, "runtimes") to DIR_MULTIRT,
                    File(legacyGame, "jna_dir") to DIR_JNA,
                ).forEach { (from, to) ->
                    val targetEmpty = !to.exists() || (to.list()?.isEmpty() != false)
                    if (from.exists() && targetEmpty) {
                        to.parentFile?.mkdirs()
                        if (!from.renameTo(to)) {
                            FileUtils.copyDirectory(from, to)
                            FileUtils.deleteQuietly(from)
                        }
                    }
                }
            }.onFailure {
                com.movtery.zalithlauncher.utils.logging.Logger.warning(
                    "PathManager", "Migrate runtimes failed: ${it.message}"
                )
            }
        }

        private fun createDirs() {
            DIR_RUNTIME_MOD?.mkdirs()
            DIR_GAME.mkdirs()
            DIR_ACCOUNT_SKIN.mkdirs()
            DIR_ACCOUNT_CAPE.mkdirs()
            DIR_MULTIRT.mkdirs()
            DIR_JNA.mkdirs()
            DIR_COMPONENTS.mkdirs()
            DIR_MOUSE_POINTER.mkdirs()
            DIR_BACKGROUND.mkdirs()
            DIR_CACHE_GAME_DOWNLOADER.mkdirs()
            DIR_CACHE_MODPACK_DOWNLOADER.mkdirs()
            DIR_CACHE_MODPACK_EXPORTER.mkdirs()
            DIR_CACHE_MOD_UPDATER.mkdirs()
            DIR_CACHE_APP_ICON.mkdirs()
            DIR_CACHE_HOME_PAGE.mkdirs()
            DIR_LAUNCHER_LOGS.mkdirs()
            DIR_NATIVE_LOGS.mkdirs()
            DIR_IMAGE_CACHE.mkdirs()
            DIR_CONTROL_LAYOUTS.mkdirs()
            DIR_TERRACOTTA.mkdirs()
            DIR_STYLES.mkdirs()
        }

        /** 公共存储下的可见根目录：`/storage/emulated/0/ZyNova` */
        fun publicRootDir(): File =
            File(Environment.getExternalStorageDirectory(), PUBLIC_GAME_DIR_NAME)

        /** 公共存储下的游戏根目录：`/storage/emulated/0/ZyNova/.Minecraft` */
        fun publicGameDir(): File =
            File(publicRootDir(), PUBLIC_GAME_DIR_INNER)

        /**
         * 启动日志放哪。
         *
         * 用公共目录时放 `/ZyNova/logs`（用户反馈问题时直接在文件管理器里能找到、能发出来）；
         * 公共目录不可写就退回 `Android/data/<包名>/files/logs`，**日志永远有地方写**，
         * 不能因为目录建不出来把启动流程搞崩。
         */
        private fun resolveLauncherLogsDir(): File {
            val external = File(DIR_FILES_EXTERNAL, "logs")
            if (DIR_GAME.absolutePath != publicGameDir().absolutePath) return external
            val publicLogs = File(publicRootDir(), PUBLIC_LOG_DIR_NAME)
            val usable = runCatching { publicLogs.isDirectory || publicLogs.mkdirs() }
                .getOrDefault(false)
            return if (usable) publicLogs else external
        }

        /**
         * 28.1.3 之前公共目录是「平铺」的：`ZyNova/versions`、`ZyNova/account_skins`……
         * 现在这些全部收进 `ZyNova/.Minecraft`，根目录只留 `logs` 与 `.Minecraft`。
         *
         * 同一分区 rename 是瞬时的，所以这里不需要进度条；rename 失败才退回复制 + 删除。
         * 目标已存在同名条目时**保持原样**（大概率是上次只搬了一半，交给实例列表各自校验）。
         */
        private fun migratePublicFlatLayout() {
            runCatching {
                val public = publicGameDir()
                val root = publicRootDir()
                if (DIR_GAME.absolutePath != public.absolutePath) return@runCatching
                if (!root.isDirectory) return@runCatching
                public.mkdirs()
                root.listFiles()?.forEach { child ->
                    val name = child.name
                    //logs 与 .Minecraft 本来就该留在根目录
                    if (name == PUBLIC_GAME_DIR_INNER || name == PUBLIC_LOG_DIR_NAME) return@forEach
                    val target = File(public, name)
                    if (target.exists()) return@forEach
                    if (!child.renameTo(target)) {
                        runCatching {
                            if (child.isDirectory) {
                                FileUtils.copyDirectory(child, target)
                            } else {
                                FileUtils.copyFile(child, target)
                            }
                            FileUtils.deleteQuietly(child)
                        }
                    }
                }
            }.onFailure {
                com.movtery.zalithlauncher.utils.logging.Logger.warning(
                    "PathManager", "Migrate public dir layout failed: ${it.message}"
                )
            }
        }

        /** 私有目录下的旧游戏根目录（28.2.0 之前的位置） */
        fun legacyGameDir(): File = File(DIR_FILES_PRIVATE, "games")

        fun hasContent(dir: File): Boolean =
            dir.isDirectory && (dir.list()?.isNotEmpty() == true)

        /**
         * 是否有权写公共存储。
         *
         * Android 11 起必须拿到「所有文件访问」；更早的版本只要有存储权限即可。
         * 拿不到就回退私有目录，**绝不因为权限问题让启动器起不来**。
         */
        private fun canWritePublicStorage(): Boolean {
            val allowed = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                runCatching { Environment.isExternalStorageManager() }.getOrDefault(false)
            } else true
            if (!allowed) return false
            return runCatching { publicGameDir().let { it.exists() || it.mkdirs() } }.getOrDefault(false)
        }

        /**
         * 决定这次用哪个游戏根目录。
         *
         * 优先级：
         * 1. 公共目录已经有内容 → 直接用；
         * 2. 只有旧的私有目录有内容 → **先继续用旧目录**，同时在后台把数据搬到公共目录，
         *    搬完并校验成功后才删除旧目录（下次启动自动用新目录）。
         *    这样迁移中途退出、失败都不会丢实例；
         * 3. 全新安装 → 直接用公共目录（没权限就回退私有）。
         */
        private fun resolveGameDir(): File {
            val public = publicGameDir()
            val legacy = legacyGameDir()
            //有权限就**始终**用公共目录：旧数据由 GameDirMigration 在进主界面前搬过去。
            //⚠️ 这里以前写成「私有目录有数据就继续用私有」，结果 needsMigration 永远为 false，
            //迁移界面根本不出现，老用户永远搬不过去。
            if (canWritePublicStorage()) return public
            return legacy
        }

        /**
         * 迁移失败时回退到私有目录：改回路径并重建依赖它的文件常量。
         * 宁可继续用私有目录，也不能让用户的实例"消失"。
         */
        fun useLegacyGameDir() {
            runCatching {
                DIR_GAME = legacyGameDir()
                FILE_MINECRAFT_VERSIONS = File(DIR_GAME, "minecraft_versions.json")
                DIR_ACCOUNT_SKIN = File(DIR_GAME, "account_skins")
                DIR_ACCOUNT_CAPE = File(DIR_GAME, "account_capes")
                DIR_GAME.mkdirs()
                DIR_ACCOUNT_SKIN.mkdirs()
                DIR_ACCOUNT_CAPE.mkdirs()
            }
        }

        /**
         * 处理历史遗留的旧文件
         */
        private fun handleLegacy() {
            //不再支持的共用游戏运行日志
            File(DIR_FILES_EXTERNAL, LogName.GAME.fileName).takeIf { it.exists() }?.let {
                FileUtils.deleteQuietly(it)
            }
        }
    }
}