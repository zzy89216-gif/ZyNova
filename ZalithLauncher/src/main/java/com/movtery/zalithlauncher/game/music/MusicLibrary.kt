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

package com.movtery.zalithlauncher.game.music

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

private const val TAG = "MusicLibrary"

/**
 * 音乐曲库（议题 #77）。
 *
 * 存储约定：**只用应用私有目录** `filesDir/music`。
 * 不写 `/ZyNova` 公共目录 —— 那个目录的根层会被 `PathManager.migratePublicFlatLayout()`
 * 在每次启动时搬进 `.Minecraft`，用户放进 `/ZyNova/music` 的文件会「凭空消失」。
 *
 * 曲库列表不落盘：目录本身就是唯一事实来源，扫描即可得到列表；
 * 用户手动删文件、导入失败留下半截文件都不会让列表和真实文件对不上。
 */
object MusicLibrary {
    private const val MUSIC_DIR_NAME = "music"

    /** 系统文件选择器的 MIME 过滤：只让用户看到音频（其余文件即使被选中也会被拦下） */
    const val AUDIO_MIME = "audio/*"

    /**
     * ExoPlayer 能直接解码的常见扩展名。
     *
     * 只用来「显示名里没有扩展名时推断一个」以及「MIME 也拿不到时判断像不像音频」，
     * **不作为硬性拦截**：系统给出的 MIME 属于 audio 类型时一律允许导入，
     * 真的解不了由播放器的 onPlayerError 兜底提示（而不是在导入时静默丢掉）。
     */
    private val KNOWN_EXTENSIONS = setOf(
        "mp3", "m4a", "m4b", "aac", "flac", "ogg", "oga", "opus", "wav", "weba", "3gp", "mp4"
    )

    /** 单次导入里文件名最多保留的字符数，避免超长文件名写盘失败 */
    private const val MAX_NAME_LENGTH = 80

    /** 一次导入的结果，界面据此给出「成功几首 / 哪几个失败 / 哪几个不是音频」 */
    data class ImportResult(
        val importedCount: Int,
        val failedNames: List<String>,
        val nonAudioNames: List<String>
    )

    /** 曲库目录；不存在时顺手创建 */
    fun musicDir(context: Context): File = File(context.filesDir, MUSIC_DIR_NAME)

    /**
     * 扫描曲库（IO 线程）。
     *
     * 时长用 [MediaMetadataRetriever] 读：只读文件头，不会把整首歌读进内存。
     * 某个文件读不出时长（编码不支持 / 文件损坏）按 0 处理，不影响其它曲目。
     */
    suspend fun list(context: Context): List<MusicTrack> = withContext(Dispatchers.IO) {
        val dir = musicDir(context)
        if (!dir.isDirectory && !dir.mkdirs() && !dir.isDirectory) {
            Logger.warning(TAG, "Cannot create music directory: ${dir.absolutePath}")
            return@withContext emptyList()
        }
        dir.listFiles()
            ?.filter { it.isFile && it.length() > 0L }
            ?.map { toTrack(it) }
            ?.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { track: MusicTrack -> track.title })
            ?: emptyList()
    }

    /**
     * 把系统文件选择器里选中的音频复制进私有目录（IO 线程）。
     *
     * 为什么一定要复制：SAF 给的 URI 读权限在进程重启后不保证一直可用，
     * 直接拿 URI 建曲库很容易出现「过几天点播放就报错」。复制一份是最稳的做法。
     */
    suspend fun import(context: Context, uris: List<Uri>): ImportResult = withContext(Dispatchers.IO) {
        val dir = musicDir(context)
        if (!dir.isDirectory && !dir.mkdirs() && !dir.isDirectory) {
            Logger.warning(TAG, "Cannot create music directory: ${dir.absolutePath}")
            return@withContext ImportResult(
                importedCount = 0,
                failedNames = uris.map { displayNameOf(context, it) },
                nonAudioNames = emptyList()
            )
        }

        var imported = 0
        val failed = mutableListOf<String>()
        val nonAudio = mutableListOf<String>()

        uris.forEach { uri ->
            val displayName = displayNameOf(context, uri)
            val mimeType = runCatching { context.contentResolver.getType(uri) }.getOrNull()
            val extension = resolveExtension(displayName, mimeType)
            if (!looksLikeAudio(mimeType, extension)) {
                nonAudio += displayName
                return@forEach
            }

            val copied = runCatching {
                copyIntoTrack(context, uri, dir, displayName, extension)
            }.onFailure { throwable ->
                Logger.warning(TAG, "Failed to import $displayName", throwable)
            }.getOrNull()

            if (copied != null) imported++ else failed += displayName
        }

        ImportResult(
            importedCount = imported,
            failedNames = failed,
            nonAudioNames = nonAudio
        )
    }

    /** 删除一首曲目（IO 线程）。文件已经不在了也按「删掉了」处理，避免界面卡在失败提示上 */
    suspend fun delete(track: MusicTrack): Boolean = withContext(Dispatchers.IO) {
        runCatching { !track.file.exists() || track.file.delete() }.getOrDefault(false)
    }

    private fun toTrack(file: File): MusicTrack {
        return MusicTrack(
            file = file,
            title = file.nameWithoutExtension.ifBlank { file.name },
            durationMs = readDurationMs(file),
            sizeBytes = file.length()
        )
    }

    private fun readDurationMs(file: File): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
                ?.coerceAtLeast(0L)
                ?: 0L
        } catch (throwable: Throwable) {
            Logger.warning(TAG, "Failed to read the duration of ${file.name}", throwable)
            0L
        } finally {
            runCatching { retriever.release() }
        }
    }

    /**
     * 把选中的文件复制成曲库里的正式文件。
     *
     * 先写 `*.part` 临时文件，写完再改名：中途失败/杀进程不会在曲库里留下
     * 半截音频（否则列表里会出现一首点了就报错的歌）。
     */
    private fun copyIntoTrack(
        context: Context,
        uri: Uri,
        dir: File,
        displayName: String,
        extension: String?
    ): File? {
        val targetName = uniqueFileName(dir, sanitizeBaseName(displayName), extension)
        val target = File(dir, targetName)
        val temporary = File(dir, "$targetName.part")
        try {
            val input = context.contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("Cannot open the selected file")
            input.use { source ->
                temporary.outputStream().use { sink ->
                    source.copyTo(sink)
                }
            }
            if (temporary.length() <= 0L) {
                throw IllegalStateException("The selected file is empty")
            }
            if (target.exists()) {
                target.delete()
            }
            if (!temporary.renameTo(target)) {
                temporary.copyTo(target, overwrite = true)
            }
            return target
        } finally {
            runCatching { if (temporary.exists()) temporary.delete() }
        }
    }

    /** 追加序号直到文件名不冲突；同时避开还没改名的 `.part`，防止并发导入互相覆盖 */
    private fun uniqueFileName(dir: File, baseName: String, extension: String?): String {
        val suffix = if (extension.isNullOrBlank()) "" else ".$extension"
        var index = 1
        var fileName = "$baseName$suffix"
        while (File(dir, fileName).exists() || File(dir, "$fileName.part").exists()) {
            index++
            fileName = "$baseName ($index)$suffix"
        }
        return fileName
    }

    /** 显示名里可能带路径分隔符 / 非法字符（少数第三方文件管理器会这样），写盘前先清洗 */
    private fun sanitizeBaseName(displayName: String): String {
        val rawBase = displayName.substringBeforeLast('.', displayName)
        val sanitized = rawBase
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .trim()
            .take(MAX_NAME_LENGTH)
        return sanitized.ifBlank { "track" }
    }

    /**
     * 决定复制进曲库时使用的扩展名。
     *
     * 优先用显示名里带的扩展名（ExoPlayer 也靠扩展名推断容器格式），
     * 显示名没有就按 MIME 反推。
     */
    private fun resolveExtension(displayName: String, mimeType: String?): String? {
        val fromName = displayName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        if (fromName.isNotEmpty()) return fromName

        if (mimeType != null && mimeType.startsWith("audio/", ignoreCase = true)) {
            return MimeTypeMap.getSingleton()
                .getExtensionFromMimeType(mimeType)
                ?.lowercase(Locale.ROOT)
                ?: "mp3"
        }
        return null
    }

    private fun looksLikeAudio(mimeType: String?, extension: String?): Boolean {
        if (mimeType != null && mimeType.startsWith("audio/", ignoreCase = true)) return true
        return extension != null && extension in KNOWN_EXTENSIONS
    }

    /** 查询显示名；取不到就退回 URI 最后一段，保证总有一个可用的名字 */
    private fun displayNameOf(context: Context, uri: Uri): String {
        runCatching {
            context.contentResolver
                .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1 && cursor.moveToFirst()) {
                        cursor.getString(index)?.takeIf { it.isNotBlank() }?.let { return it }
                    }
                }
        }
        return uri.lastPathSegment
            ?.substringAfterLast('/')
            ?.takeIf { it.isNotBlank() }
            ?: "audio"
    }
}
