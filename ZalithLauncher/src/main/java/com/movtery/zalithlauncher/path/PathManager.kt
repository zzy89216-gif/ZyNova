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
         * 公共存储下的游戏目录名。
         *
         * 维护者要求：游戏文件放到**文件管理器一打开就能看到**的地方，目录名用启动器的名字。
         * 所以是 `/storage/emulated/0/ZyNova`，不是 `Android/data/...`。
         *
         * ⚠️ 名字是**固定的**，不要跟着 `launcher_name` 变——用户改个显示名就把数据目录改了，
         * 等于把已有实例弄丢。
         */
        const val PUBLIC_GAME_DIR_NAME = "ZyNova"

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
            DIR_LAUNCHER_LOGS = File(DIR_FILES_EXTERNAL, "logs")
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

            //先搬运行时（此时目标目录还没被 createDirs 建出来），再创建其余目录
            migrateRuntimesToPrivate()
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

        /** 公共存储下的游戏根目录 */
        fun publicGameDir(): File =
            File(Environment.getExternalStorageDirectory(), PUBLIC_GAME_DIR_NAME)

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
            if (hasContent(public)) return public
            //旧目录有数据：先继续用着，由 [GameDirMigration] 弹窗问用户要不要搬，
            //**不在启动时偷偷搬**（几 GB 数据放主线程/无提示地搬，体验和风险都不对）
            if (hasContent(legacy)) return legacy
            return if (canWritePublicStorage()) public else legacy
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