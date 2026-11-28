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

package com.movtery.zalithlauncher.game.download.jvm_server

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 安装器 JVM 阶段的进程级互斥门（议题 #78）
 *
 * 背景：Forge / NeoForge / OptiFine 等安装器需要一个临时的 JVM 来跑安装 jar，
 * 而 [JvmService] 是 Android 前台服务、[JVMSocketServer] 是固定端口的单例对象——
 * **整个进程只能有一个 JVM 安装阶段**。两个本体并行安装、又都带 Forge 时，
 * 第二个会抢同一个服务/端口，甚至取消其中一个时把服务 stop 掉，
 * 把另一个正在跑的安装一起打断。
 *
 * 修法：所有使用 JVM 的安装阶段统一经过这个 Mutex：
 * - 进入前排队（[kotlinx.coroutines.sync.Mutex] 的等待是可取消的：取消一个等待中的安装不影响别的）；
 * - 阶段内启动 / 等待 / 停止服务（"停止"放在 `startJvmServiceAndWaitExit` 的 finally 里，
 *   所以取消当前持有者也一定会把手里的服务收掉）；
 * - 出锁后下一个安装可以正常重新启动服务。
 *
 * 不在 JVM 阶段的安装（原版 / Fabric / 整合包下载 / 资源安装）完全不受影响。
 */
object InstallerJvmGate {
    private val mutex = Mutex()

    suspend fun <T> withJvm(block: suspend () -> T): T = mutex.withLock {
        block()
    }
}
