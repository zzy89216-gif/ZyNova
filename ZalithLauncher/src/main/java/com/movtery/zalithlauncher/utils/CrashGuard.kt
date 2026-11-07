/*
 * ZyNova Launcher
 * Copyright (C) 2026 zzy89216-gif and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.movtery.zalithlauncher.utils

import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/**
 * 游戏异常退出记录（议题 #44「崩溃自愈」第一批）。
 *
 * 现在做的是**可靠地记住发生了什么**，而不是偷偷替用户改文件：
 * 1. 游戏以非 0 退出码结束时，写一条记录（版本 / 退出码 / 时间）；
 * 2. 用户回到启动器时弹一次提示，可以直接跳「日志」页，或用 AI 分析这次崩溃；
 * 3. 用户处理过后清除记录，不会反复打扰。
 *
 * 为什么不在后台自动改文件：AI 自动禁用模组 / 删文件属于不可逆操作，
 * 必须由用户点一下确认；等「崩溃自愈」成熟后再做成可选的自动模式。
 */
@Serializable
data class CrashRecord(
    val versionName: String = "",
    val exitCode: Int = 0,
    val isSignal: Boolean = false,
    val timestamp: Long = 0L
)

object CrashGuard {
    private const val TAG = "CrashGuard"
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private fun file(): File = File(PathManager.DIR_LAUNCHER_LOGS, "last_game_exit.json")

    /** 游戏异常退出时调用 */
    fun markCrash(versionName: String, exitCode: Int, isSignal: Boolean) {
        runCatching {
            val record = CrashRecord(
                versionName = versionName,
                exitCode = exitCode,
                isSignal = isSignal,
                timestamp = System.currentTimeMillis()
            )
            val target = file()
            target.parentFile?.mkdirs()
            target.writeText(json.encodeToString(CrashRecord.serializer(), record))
            Logger.info(TAG, "Game exited abnormally: code=$exitCode, signal=$isSignal, version=$versionName")
        }.onFailure { Logger.warning(TAG, "Unable to write crash record", it) }
    }

    /** 读取（不删除）最近一次异常退出记录 */
    fun peek(): CrashRecord? = runCatching {
        val target = file()
        if (!target.isFile) return null
        if (System.currentTimeMillis() - target.lastModified() > 7L * 24 * 3600 * 1000) {
            //超过一周的记录没有意义，直接清掉
            target.delete()
            return null
        }
        json.decodeFromString(CrashRecord.serializer(), target.readText())
    }.getOrNull()

    /** 用户处理过之后清除 */
    fun clear() {
        runCatching { file().delete() }
    }
}
