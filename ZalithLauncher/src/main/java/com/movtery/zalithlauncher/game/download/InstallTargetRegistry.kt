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

import java.util.concurrent.ConcurrentHashMap

/**
 * 进程级「安装目标版本名」注册表（议题 #78）
 *
 * 为什么需要：游戏本体安装（下载页 / AI install_game）、整合包安装（用户输入的版本名）、
 * 本地整合包导入 是三条独立入口，各自的单槽/去重逻辑看不到对方。
 * 用户只要让其中两条撞上同一个版本名，两个安装会同时往 `versions/<同名>/` 里写，
 * 结果就是版本 json/jar 被覆盖、libraries 冲突、甚至「装好但启动崩」。
 *
 * 这里用进程级 ConcurrentHashMap 做「同名只能有一个安装」的租约：
 * - [reserve] 原子占用，失败即表示另一个安装已在用这个名字（调用方必须给出可读错误，不能静默）；
 * - [release] 在成功 / 失败 / 取消 / 任务中断的**所有**收尾路径调用（幂等）；
 * - 进程被杀后内存表自然清空；启动恢复时再主动 [clearAll] 一次兜底。
 *
 * ⚠️ 它只解决「并发同名」；「版本已经装好」仍由各安装器的
 * `GameAlreadyInstalledException` / `VersionsManager.isVersionExists` 负责。
 */
object InstallTargetRegistry {
    private val reserved = ConcurrentHashMap<String, String>()

    /**
     * 尝试占用一个目标版本名
     * @param owner 便于排查的占用者描述（例如 "game:<版本名>"）
     * @return true = 占用成功；false = 已被别的安装占用
     */
    fun reserve(targetVersionName: String, owner: String = ""): Boolean =
        reserved.putIfAbsent(targetVersionName, owner) == null

    /**
     * 释放占用（幂等：没占用也不会报错）
     */
    fun release(targetVersionName: String) {
        reserved.remove(targetVersionName)
    }

    fun isReserved(targetVersionName: String): Boolean = reserved.containsKey(targetVersionName)

    /** 启动恢复时清空进程内残留（进程刚启动时本来也是空的，这里只是语义上的兜底） */
    fun clearAll() {
        reserved.clear()
    }
}
