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

package com.movtery.zalithlauncher.game.version.saves

import com.github.steveice10.opennbt.NBTIO
import com.github.steveice10.opennbt.tag.builtin.ByteTag
import com.github.steveice10.opennbt.tag.builtin.CompoundTag
import com.github.steveice10.opennbt.tag.builtin.IntTag
import com.github.steveice10.opennbt.tag.builtin.LongTag
import com.github.steveice10.opennbt.tag.builtin.StringTag
import com.movtery.zalithlauncher.game.version.installed.Version
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ThreadLocalRandom

/**
 * 在实例的 `saves/` 目录下创建一个可以直接游玩的新世界（议题 #33）
 *
 * 设计取舍（重要）：
 * - **不直接写当前版本的 `WorldGenSettings`**。它要求完整的 `dimensions` 结构，
 *   只写 `seed` 会被原版判定为 `No key dimensions`，世界直接打不开。
 * - 这里写的是 **1.12.2 兼容层** 的 `level.dat`（`LevelName` / `RandomSeed` /
 *   `GameType` / `Difficulty` / `GameRules.keepInventory` 等），
 *   让 Minecraft 自带的 DataFixer 在首次打开时升级到当前版本。
 *   这样 1.12.2 及之后（含 26.x 的新存档格式）都能正常读取，也不需要我们手写
 *   `dimensions`、`DataPacks` 这些容易随版本变化的字段。
 * - `Data/Version` 固定写 `1.12.2`（Id 1343）正是为了触发 DataFixer；
 *   玩家首次进入并保存后，游戏会把版本标记改写为当前版本。
 *
 * ⚠️ 世界文件夹名与 `LevelName` 是两个概念：
 * `--quickPlaySingleplayer` 需要的是**文件夹名**，不是世界显示名。
 */
object WorldCreator {

    /**
     * 创建一个新世界
     *
     * @return 实际创建的世界文件夹名（用于 quickPlay）
     */
    suspend fun createWorld(
        version: Version,
        levelName: String,
        seed: Long,
        gameMode: GameMode,
        hardcore: Boolean,
        difficulty: Difficulty,
        keepInventory: Boolean,
    ): String = withContext(Dispatchers.IO) {
        val savesDir = File(version.getGameDir(), "saves").apply { mkdirs() }
        val folderName = uniqueFolderName(savesDir, sanitizeFolderName(levelName))
        val worldDir = File(savesDir, folderName).apply { mkdirs() }

        val data = CompoundTag("Data").apply {
            put(StringTag("LevelName", levelName))
            //1.12.2 的字段：GameType 0 生存 / 1 创造；极限模式是 GameType=0 + hardcore=1
            put(IntTag("GameType", gameMode.levelCode))
            put(ByteTag("hardcore", (if (hardcore) 1 else 0).toByte()))
            put(ByteTag("Difficulty", difficulty.levelCode.toByte()))
            put(ByteTag("DifficultyLocked", (if (hardcore) 1 else 0).toByte()))
            put(ByteTag("allowCommands", 0.toByte()))
            put(ByteTag("initialized", 1.toByte()))
            put(ByteTag("MapFeatures", 1.toByte()))
            //1.12.2 的 Anvil 存档格式版本（与 Data/Version/Id 配套）
            put(IntTag("version", 19133))
            //同时写 1.9+ 的 generatorName 与更老的 Generator 整数，
            //保证 DataFixer 从 1.12.2 升级时能拿到完整的“默认世界类型”
            put(StringTag("generatorName", "default"))
            put(StringTag("generatorOptions", ""))
            put(IntTag("generatorVersion", 0))
            put(IntTag("Generator", 0))
            put(LongTag("RandomSeed", seed))
            put(LongTag("LastPlayed", System.currentTimeMillis()))
            put(LongTag("SizeOnDisk", 0L))
            put(LongTag("Time", 0L))
            put(LongTag("DayTime", 0L))
            put(IntTag("SpawnX", 0))
            put(IntTag("SpawnY", 64))
            put(IntTag("SpawnZ", 0))
            put(CompoundTag("GameRules").apply {
                put(StringTag("keepInventory", keepInventory.toString()))
            })
            //1.12.2 标记：交给原版 DataFixer 升级，而不是让当前版本直接解析不完整的现代字段
            put(CompoundTag("Version").apply {
                put(IntTag("Id", 1343))
                put(StringTag("Name", "1.12.2"))
                put(ByteTag("Snapshot", 0.toByte()))
            })
        }

        val root = CompoundTag("").apply { put(data) }
        try {
            //level.dat 必须是 gzip 压缩的 NBT（Minecraft 与 SaveUtils 都按压缩格式读取）
            NBTIO.writeFile(root, File(worldDir, "level.dat"), true, false)
        } catch (e: Exception) {
            //写入失败时不要留下一个半成品存档目录，避免主页/存档页出现坏档
            worldDir.deleteRecursively()
            throw e
        }
        folderName
    }

    /**
     * 解析种子输入：
     * - 纯数字直接使用
     * - 其它文本按 Minecraft 的方式取字符串哈希
     * - 留空返回随机种子
     */
    fun parseSeed(input: String): Long {
        val text = input.trim()
        if (text.isEmpty()) return ThreadLocalRandom.current().nextLong()
        return text.toLongOrNull() ?: text.hashCode().toLong()
    }

    /**
     * 让世界名可以安全地作为文件夹名。
     *
     * 只替换文件系统不允许的字符，保留中文与空格；`LevelName` 仍然是玩家输入的原文。
     */
    private fun sanitizeFolderName(name: String): String {
        val cleaned = name.trim()
            .replace(Regex("""[\\/:*?"<>|]"""), "_")
            .trim('.', ' ')
        return cleaned.ifBlank { "New World" }.take(64)
    }

    /** 同名文件夹已存在时追加 (2)、(3)…，避免覆盖玩家的旧存档 */
    private fun uniqueFolderName(parent: File, base: String): String {
        var candidate = base
        var index = 2
        while (File(parent, candidate).exists()) {
            candidate = "$base ($index)"
            index++
        }
        return candidate
    }
}
