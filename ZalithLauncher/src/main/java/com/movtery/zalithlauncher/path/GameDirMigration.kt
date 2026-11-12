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
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.logging.Logger
import org.apache.commons.io.FileUtils
import java.io.File

object GameDirMigration {

    private const val TAG = "GameDirMigration"

    /** 本次启动是否需要弹「是否迁移」 */
    fun needsPrompt(): Boolean {
        if (AllSettings.gameDirMigrationAsked.getValue()) return false
        val legacy = PathManager.legacyGameDir()
        val public = PathManager.publicGameDir()
        //旧目录有数据，且公共目录还没有（否则就是已经迁过了）
        return PathManager.hasContent(legacy) && !PathManager.hasContent(public)
    }

    /** 用户选了「不迁移」：记一笔，别再问 */
    fun markAsked() {
        AllSettings.gameDirMigrationAsked.save(true)
    }

    /**
     * 后台迁移。成功后旧目录会被删除，下次启动直接使用公共目录。
     * 回调在主线程以外，调用方自己切线程。
     */
    fun migrate(onFinished: (Boolean) -> Unit) {
        markAsked()
        val from = PathManager.legacyGameDir()
        val to = PathManager.publicGameDir()
        Thread({
            val ok = runCatching {
                Logger.info(TAG, "Migrating: ${from.absolutePath} -> ${to.absolutePath}")
                to.mkdirs()
                FileUtils.copyDirectory(from, to)
                val oldCount = from.walkTopDown().count()
                val newCount = to.walkTopDown().count()
                if (newCount >= oldCount) {
                    FileUtils.deleteQuietly(from)
                    Logger.info(TAG, "Migration finished ($newCount files)")
                    true
                } else {
                    Logger.warning(TAG, "Migration incomplete ($oldCount -> $newCount), kept old dir")
                    false
                }
            }.onFailure {
                Logger.warning(TAG, "Migration failed: ${it.message}")
            }.getOrDefault(false)
            onFinished(ok)
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
