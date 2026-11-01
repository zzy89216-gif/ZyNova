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

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.util.UUID
import java.util.zip.ZipFile

/** 导入失败的原因（UI 据此给本地化提示，而不是一句「导入失败」） */
class FavoriteImportException(message: String, cause: Throwable? = null) : IOException(message, cause)

/** 文件已经归档过（同一份内容），[existing] 是已有记录 */
class FavoriteAlreadyImportedException(val existing: FavoriteEntry) :
    IOException("This file has already been archived")

/**
 * 把手机里的资源文件导入 ZyNova 私有目录（议题 #39）
 *
 * 流程：临时文件 → 计算 SHA-256 → 结构识别类型 → 查重 → 移动到归档目录 → 写记录。
 *
 * 几条硬性保证：
 * - **原文件不动**：只读打开 SAF 的输入流，复制一份，不移动、不删除用户原文件；
 * - **先校验再入库**：只有完整复制并算完哈希的文件才会被改名进归档目录，
 *   中途失败会删掉临时文件，不会留下「看起来导入成功、其实是半个文件」的记录；
 * - **结构优先于扩展名**：判断类型时先解开压缩包看里面有什么，
 *   只看 `.jar` / `.zip` 会把任意压缩包误判成模组。
 */
object FavoriteImporter {

    suspend fun import(context: Context, uri: Uri): FavoriteEntry = withContext(Dispatchers.IO) {
        val displayName = queryDisplayName(context, uri) ?: "imported-${System.currentTimeMillis()}"
        val temp = File(FavoriteStore.rootDir(), ".import-${UUID.randomUUID()}.tmp")

        try {
            // 1. 复制到临时文件，同时算哈希
            val digest = MessageDigest.getInstance("SHA-256")
            var size = 0L
            context.contentResolver.openInputStream(uri)?.use { input ->
                temp.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val read = input.read(buffer)
                        if (read <= 0) break
                        digest.update(buffer, 0, read)
                        output.write(buffer, 0, read)
                        size += read
                    }
                }
            } ?: throw FavoriteImportException("Cannot open the selected file")

            if (size <= 0L || !temp.isFile) {
                throw FavoriteImportException("The selected file is empty or unreadable")
            }

            val sha256 = digest.digest().joinToString("") { "%02x".format(it) }

            // 2. 同一份内容已经归档过 → 不再复制第二份
            FavoriteStore.findBySha256(sha256)?.let { existing ->
                throw FavoriteAlreadyImportedException(existing)
            }

            // 3. 结构识别类型
            val type = detectType(temp, displayName)

            // 4. 移动到归档目录：<sha256 前 16 位>-<清洗后的名字>
            val safeName = sanitizeFileName(displayName)
            val targetName = "${sha256.take(16)}-$safeName"
            val target = File(FavoriteStore.filesDir(), targetName)
            if (target.exists()) target.delete()
            if (!temp.renameTo(target)) {
                temp.copyTo(target, overwrite = true)
                temp.delete()
            }

            val entry = FavoriteEntry(
                id = UUID.randomUUID().toString(),
                name = displayName,
                type = type,
                source = "local",
                importedAt = System.currentTimeMillis(),
                fileName = targetName,
                size = size,
                sha256 = sha256
            )
            FavoriteStore.add(entry)
            entry
        } catch (e: FavoriteAlreadyImportedException) {
            temp.delete()
            throw e
        } catch (e: Exception) {
            // 任何失败都不留下半成品
            temp.delete()
            if (e is FavoriteImportException) throw e
            throw FavoriteImportException(e.message ?: "Import failed", e)
        }
    }

    /**
     * 通过文件结构判断资源类型。
     *
     * 判定顺序刻意是「先看里面有什么」：
     * 1. `level.dat` → 存档
     * 2. `modrinth.index.json` / `manifest.json` + `mods/` → 整合包
     * 3. `fabric.mod.json` / `META-INF/mods.toml` / `mcmod.info` → 模组
     * 4. 只有 `shaders/` 或常见光影文件 → 光影
     * 5. 带 `pack.mcmeta` 且有 `assets/` → 材质包；有 `data/` 且无 `assets/` → 数据包
     * 6. 其余按扩展名兜底（`.jar` 视为模组，`.zip` 视为未知）
     */
    private fun detectType(file: File, displayName: String): FavoriteType {
        val lowerName = displayName.lowercase()

        //jar 只可能是模组或加载器插件；这里统一按模组归档，具体能不能装由安装流程再判断
        if (lowerName.endsWith(".jar")) return FavoriteType.MOD

        if (!lowerName.endsWith(".zip") && !lowerName.endsWith(".mrpack")) {
            return FavoriteType.UNKNOWN
        }

        return runCatching {
            ZipFile(file).use { zip ->
                val names = zip.entries().asSequence().map { it.name }.toList()
                when {
                    names.any { it == "level.dat" } -> FavoriteType.WORLD
                    names.any { it == "modrinth.index.json" } -> FavoriteType.MODPACK
                    names.any { it == "manifest.json" } && names.any { it.startsWith("mods/") } -> FavoriteType.MODPACK
                    names.any { it == "pack.mcmeta" } && names.any { it.startsWith("data/") } &&
                            names.none { it.startsWith("assets/") } -> FavoriteType.DATAPACK
                    names.any { it == "pack.mcmeta" } -> FavoriteType.RESOURCE_PACK
                    names.any { it.startsWith("shaders/") } -> FavoriteType.SHADER
                    else -> FavoriteType.UNKNOWN
                }
            }
        }.getOrDefault(FavoriteType.UNKNOWN)
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                }
        }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    /** 只做文件系统层面的清洗：保留可读名字，替换掉非法字符 */
    private fun sanitizeFileName(name: String): String {
        val cleaned = name.replace(Regex("""[\\/:*?"<>|]"""), "_").trim()
        return cleaned.ifEmpty { "resource" }.take(64)
    }
}
