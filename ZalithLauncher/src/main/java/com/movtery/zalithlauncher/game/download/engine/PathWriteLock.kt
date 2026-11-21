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

package com.movtery.zalithlauncher.game.download.engine

import java.io.File
import kotlinx.coroutines.sync.Mutex

/**
 * 进程级「按目标路径分条」的写锁（议题 #78）
 *
 * 为什么需要：并行安装多个游戏本体时，两个安装会共用目标游戏目录里的
 * assets / libraries / versions/<MC版本> 这些文件：
 * - 下载阶段：同一目标文件的 `.part` 是确定性的，两个任务同时写会互相截断；
 * - 复制/合并阶段：两个安装同时把内容相同的文件写到同一个目标路径，
 *   一个任务可能在另一个任务写到一半时读到/覆盖，最坏会留下截断文件。
 *
 * 这里用固定数量的条带锁（不是每个路径一个锁，避免锁对象随资源数量无限增长），
 * 按目标路径 hash 到条带；同一路径必然同一把锁，不同路径大概率不冲突。
 * 需要同时锁多个路径时按条带序号升序获取，避免互相等待形成死锁。
 */
object PathWriteLock {
    private const val STRIPE_COUNT = 128
    private val stripes = Array(STRIPE_COUNT) { Mutex() }

    private fun stripeOf(path: String): Int =
        (path.hashCode() and Int.MAX_VALUE) % STRIPE_COUNT

    /**
     * 只锁一个目标路径（文件或目录都可以，用它作为「这一组写入」的粒度）
     */
    suspend fun <T> withWrite(target: File, block: suspend () -> T): T =
        withWrites(listOf(target), block)

    /**
     * 同时锁多个目标路径；按条带序号升序获取，避免交叉等待
     */
    suspend fun <T> withWrites(targets: Collection<File>, block: suspend () -> T): T {
        val stripeIndexes = targets
            .map { stripeOf(it.absolutePath) }
            .distinct()
            .sorted()
        val acquired = ArrayList<Mutex>(stripeIndexes.size)
        try {
            stripeIndexes.forEach { index ->
                val mutex = stripes[index]
                mutex.lock()
                acquired.add(mutex)
            }
            return block()
        } finally {
            for (i in acquired.indices.reversed()) {
                acquired[i].unlock()
            }
        }
    }
}
