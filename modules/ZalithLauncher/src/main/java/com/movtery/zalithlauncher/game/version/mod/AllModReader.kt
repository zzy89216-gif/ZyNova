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

package com.movtery.zalithlauncher.game.version.mod

import android.util.Log
import com.movtery.zalithlauncher.utils.file.UnpackZipException
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "AllModReader"

const val READER_PARALLELISM = 8

/**
 * 扩展名有匹配的解析器、但所有解析器都解析失败。
 *
 * 必须与「没有匹配的解析器」区分开：文件本身是模组格式（jar/zip/litemod），
 * 只是内容或元数据有问题。message 里带每个解析器的失败原因，cause 保留首个
 * 「预期之外」的真实异常，避免以前那样统一伪报成
 * `IllegalArgumentException: No matching reader for extension: jar`。
 */
class AllReadersFailedException(
    file: File,
    extension: String,
    failures: List<String>,
    cause: Throwable?
) : RuntimeException(
    buildString {
        append("All ").append(failures.size).append(" readers failed for ")
        append(file.name).append(" (extension: ").append(extension).append(")")
        if (failures.isNotEmpty()) {
            append(": ").append(failures.joinToString("; "))
        }
    },
    cause
)

/** 异常的一行摘要：优先自己的 message，其次 cause 的，最后退回类名 */
private fun Throwable.readerFailureDetail(): String {
    val text = message ?: cause?.let { it.message ?: it.javaClass.simpleName } ?: javaClass.simpleName
    return text.lineSequence().firstOrNull()?.trim().orEmpty().ifBlank { javaClass.simpleName }
}

/**
 * 「读不出来」告警去重（文件路径 + 大小 + 最后修改时间）。
 *
 * 模组列表每次刷新 / 进游戏前都会重新扫描一遍 mods 目录，
 * 同一个坏掉的 jar 会一遍遍走同一套解析器、报同一条 WARNING。
 * 文件内容变了（重新下载好了、换了版本）指纹就会变，自然重新报。
 */
private object ReadFailureLogGuard {
    private val reported = ConcurrentHashMap<String, Boolean>()

    fun shouldReport(file: File): Boolean {
        val fingerprint = "${file.absolutePath}|${file.length()}|${file.lastModified()}"
        return reported.putIfAbsent(fingerprint, true) == null
    }
}

class AllModReader(val modsDir: File) {
    val resultsMutex = Mutex()

    private fun <T> scanFiles(
        pack: (LocalMod) -> T
    ): List<ReadTask<T>> {
        val files = modsDir.listFiles()?.filter { !it.isDirectory } ?: return emptyList()
        return files.map { file ->
            ReadTask(file, pack)
        }
    }

    /**
     * 异步读取所有模组文件，将其包装为支持同步远端项目数据的对象
     */
    suspend fun readAllForRemote(): List<RemoteMod> = withContext(Dispatchers.IO) {
        //扫描文件，封装任务
        val results = readAllMods { RemoteMod(localMod = it) }
        return@withContext results.sortedBy { it.localMod.file.name }
    }

    /**
     * 异步读取所有模组文件，获取所有本地模组信息
     */
    suspend fun readAllLocals(): List<LocalMod> = withContext(Dispatchers.IO) {
        readAllMods { it }
    }

    private suspend fun <T> readAllMods(
        pack: (LocalMod) -> T
    ): List<T> = withContext(Dispatchers.IO) {
        //扫描文件，封装任务
        val tasks = scanFiles(pack)

        buildList {
            val taskChannel = Channel<ReadTask<T>>(Channel.UNLIMITED)

            val workers = List(READER_PARALLELISM) {
                launch(Dispatchers.IO) {
                    for (task in taskChannel) {
                        val mod = task.execute()
                        resultsMutex.withLock {
                            add(mod)
                        }
                    }
                }
            }

            tasks.forEach { taskChannel.send(it) }
            taskChannel.close()

            workers.joinAll()
        }
    }

    private class ReadTask<T>(
        private val file: File,
        private val pack: (LocalMod) -> T
    ) {
        suspend fun execute(): T {
            try {
                currentCoroutineContext().ensureActive()

                val extension = if (file.isDisabled()) {
                    File(file.nameWithoutExtension).extension
                } else {
                    file.extension
                }

                //「没有匹配的解析器」= 这个扩展名根本没人认领（.txt、.toml 等）。
                //它只是非模组文件，安静地归为 notMod 即可；不要和「解析器存在但解析失败」混为一谈。
                val readers = MOD_READERS[extension]
                if (readers.isNullOrEmpty()) {
                    if (ReadFailureLogGuard.shouldReport(file)) {
                        Logger.debug(
                            TAG,
                            "No reader registered for extension: $extension, treated as a non-mod file: $file" +
                                    " (supported: ${MOD_READERS.keys})"
                        )
                    }
                    return pack(createNotMod(file))
                }

                //逐个尝试解析器：成功即返回；失败的真实原因全部收集起来
                val failures = mutableListOf<String>()
                var unexpected: Throwable? = null
                for (reader in readers) {
                    try {
                        return pack(reader.fromLocal(file))
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        failures += "${reader.javaClass.simpleName}: ${e.readerFailureDetail()}"
                        //「不是本格式」用 UnpackZipException 表达，属正常探测；其它异常才值得进 logcat
                        if (e !is UnpackZipException) {
                            if (unexpected == null) unexpected = e
                            Log.d(TAG, "Exception encountered while parsing the mod with ${reader.javaClass.simpleName}", e)
                        }
                    }
                }

                //解析器存在但全部失败：把每个解析器的真实原因与首个异常带出去
                throw AllReadersFailedException(file, extension, failures, unexpected)
            } catch (e: Exception) {
                when (e) {
                    is CancellationException -> throw e
                    else -> {
                        //同一个文件（路径 + 大小 + 修改时间）只报一次 WARNING：
                        //模组列表每次刷新都会重读，重复上报会把日志刷爆、把真故障淹掉。
                        if (ReadFailureLogGuard.shouldReport(file)) {
                            Logger.warning(TAG, "Failed to read mod: $file", e)
                        }
                        return pack(createNotMod(file))
                    }
                }
            }
        }
    }
}