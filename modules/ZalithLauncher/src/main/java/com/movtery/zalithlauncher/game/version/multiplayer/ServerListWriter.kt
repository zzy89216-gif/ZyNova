/*
 * ZyNova Launcher
 * Copyright (C) 2025 ZyNova Contributors
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

package com.movtery.zalithlauncher.game.version.multiplayer

import com.movtery.zalithlauncher.game.version.installed.Version
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * 向实例的 `servers.dat` 追加一条服务器记录（议题 #33）
 *
 * 复用 [AllServers] 的读写实现，保证与服务器管理界面完全一致：
 * 读取现有列表 → 追加 → 按原版格式整体写回（含 `servers.dat_old` 备份）。
 */
object ServerListWriter {

    /**
     * @return 实际写入的服务器数据
     */
    suspend fun addServer(
        version: Version,
        name: String,
        address: String,
    ): ServerData = withContext(Dispatchers.IO) {
        val gameDir = version.getGameDir().apply { mkdirs() }
        val dataFile = File(gameDir, "servers.dat")

        val allServers = AllServers()
        //文件不存在时 loadServers 会直接返回，等价于空列表
        allServers.loadServers(dataFile)

        val data = ServerData(
            name = name.trim().ifBlank { address.trim() },
            originIp = address.trim()
        )
        allServers.addServer(data)
        allServers.save(gameDir)

        //AllServers.save 内部会吞掉写入异常；这里回读一次确认真的落盘，
        //否则会出现「提示添加成功、进游戏却没有这个服务器」。
        val verify = AllServers()
        verify.loadServers(dataFile)
        val persisted = verify.serverList.any {
            it.originIp == data.originIp && it.name == data.name
        }
        if (!persisted) {
            throw IOException("Failed to persist server ${data.originIp} into ${dataFile.absolutePath}")
        }
        data
    }
}
