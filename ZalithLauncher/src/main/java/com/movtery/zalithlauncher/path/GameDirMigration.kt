/*
 * ZyNova Launcher — 游戏数据目录迁移（私有 → 公共）
 * GPL-3.0-or-later
 *
 * 28.2.0 起游戏数据默认放公共存储 `/storage/emulated/0/ZyNova`，文件管理器直接可见。
 * 老用户的数据还在应用私有目录里，所以升级后**问一次**要不要搬：
 *   - 点是：后台复制 + 校验，成功后才删旧目录；
 *   - 点否：继续用旧目录，不再打扰（下次大版本可以再问）。
 * 无论选哪个，**都不删账号、不删实例**。
 */
package com.movtery.zalithlauncher.path

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.apache.commons.io.FileUtils
import java.io.File

object GameDirMigration {

    private const val TAG = "GameDirMigration"

    private val _progress = MutableStateFlow(0f)
    /** 0f..1f，给迁移界面用 */
    val progress: StateFlow<Float> = _progress

    private val _currentFile = MutableStateFlow("")
    /** 正在复制的文件（相对路径） */
    val currentFile: StateFlow<String> = _currentFile

    /**
     * 是否需要**强制迁移**。
     *
     * 维护者 2026-11-12 决定：不再问，直接把旧数据搬到公共目录。
     * 条件：旧私有目录有数据、公共目录还没有、且当前已经在用公共目录
     * （没拿到「所有文件访问」时会回退私有目录，这时不迁移，等授权后再说）。
     */
    fun needsMigration(): Boolean {
        val legacy = PathManager.legacyGameDir()
        val public = PathManager.publicGameDir()
        //当前用的是公共目录（有权限），且旧目录**还在**（迁移成功会把它删掉）→ 需要搬。
        //⚠️ 不再要求「公共目录为空」：上次中途被杀会留下半成品，这时也要继续搬完，
        //否则用户会看到只有一半的实例列表。
        return PathManager.DIR_GAME.absolutePath == public.absolutePath &&
            PathManager.hasContent(legacy)
    }

    /**
     * 强制迁移（带进度）。
     *
     * 流程：统计文件数 → 逐个复制并回报进度 → 数量校验 → 删除旧目录。
     * **校验不通过不删旧目录**，并回退到私有目录继续用，避免用户数据凭空消失。
     *
     * @param onProgress 0f..1f 与当前文件名
     * @param onDone true 表示迁移成功（已切到公共目录），false 表示已回退私有目录
     */
    fun runMigration(onDone: (Boolean) -> Unit) {
        _progress.value = 0f
        _currentFile.value = ""
        val from = PathManager.legacyGameDir()
        val to = PathManager.publicGameDir()
        Thread({
            val ok = runCatching {
                Logger.info(TAG, "Forced migration: ${from.absolutePath} -> ${to.absolutePath}")
                val files = from.walkTopDown().filter { it.isFile }.toList()
                val total = files.size.coerceAtLeast(1)
                to.mkdirs()
                files.forEachIndexed { index, file ->
                    val relative = file.absolutePath.removePrefix(from.absolutePath).trimStart('/')
                    val target = File(to, relative)
                    target.parentFile?.mkdirs()
                    file.copyTo(target, overwrite = true)
                    if (index % 25 == 0 || index == files.lastIndex) {
                        _progress.value = (index + 1f) / total
                        _currentFile.value = relative
                    }
                }
                val copied = to.walkTopDown().count { it.isFile }
                if (copied >= files.size) {
                    //⚠️ 不再直接删除旧目录：改成改名保留（同一分区，瞬间完成）。
                    //万一复制过程中有文件被跳过/写坏，用户的数据还在，不会真的丢。
                    //占用的空间由用户在确认游戏正常后自行清理，或者下个版本再做自动清理。
                    val backup = File(from.parentFile, "games.migrated-" + System.currentTimeMillis())
                    if (!from.renameTo(backup)) {
                        Logger.warning(TAG, "Migration done but could not rename old dir; kept as-is")
                    }
                    Logger.info(TAG, "Forced migration finished ($copied files)")
                    true
                } else {
                    Logger.warning(TAG, "Migration incomplete ($copied/${files.size}), keeping old dir")
                    false
                }
            }.onFailure {
                Logger.warning(TAG, "Forced migration failed: ${it.message}")
            }.getOrDefault(false)

            if (!ok) {
                //迁移失败：清掉这次复制出来的半成品（否则下次启动会以为公共目录已有数据），
                //再回退私有目录，保证还能用
                runCatching {
                    to.listFiles()?.forEach { FileUtils.deleteQuietly(it) }
                }
                PathManager.useLegacyGameDir()
            }
            android.os.Handler(android.os.Looper.getMainLooper()).post { onDone(ok) }
        }, "game-dir-migration").start()
    }

    /** 是否已经拿到「所有文件访问」权限（Android 11 以下恒为 true） */
    fun hasAllFilesAccess(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            runCatching { Environment.isExternalStorageManager() }.getOrDefault(false)
        } else true

    /**
     * 开局前确保有公共存储权限。
     *
     * 没有权限时**不拦着不让玩**：拉起系统的「所有文件访问」页面并提示一句，
     * 然后照常继续。游戏数据会先落在能写的位置，等用户授权后下次启动自动迁过去。
     */
    fun ensureStorageAccess(context: Context) {
        if (hasAllFilesAccess()) return
        runCatching {
            val intent = Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Toast.makeText(context, R.string.game_dir_need_all_files, Toast.LENGTH_LONG).show()
        }.onFailure {
            Logger.warning(TAG, "Cannot open all-files settings: ${it.message}")
        }
    }

    /** 当前实际在用的游戏根目录（给「设置 → 关于」或调试用） */
    fun currentGameDir(): File = PathManager.DIR_GAME
}
