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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.game.version.saves

import com.github.steveice10.opennbt.NBTIO
import com.github.steveice10.opennbt.tag.builtin.ByteTag
import com.github.steveice10.opennbt.tag.builtin.CompoundTag
import com.github.steveice10.opennbt.tag.builtin.FloatTag
import com.github.steveice10.opennbt.tag.builtin.IntArrayTag
import com.github.steveice10.opennbt.tag.builtin.IntTag
import com.github.steveice10.opennbt.tag.builtin.ListTag
import com.github.steveice10.opennbt.tag.builtin.LongTag
import com.github.steveice10.opennbt.tag.builtin.StringTag
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.utils.isLowerVer
import com.movtery.zalithlauncher.utils.nbt.asCompoundTag
import com.movtery.zalithlauncher.utils.nbt.asLong
import com.movtery.zalithlauncher.utils.nbt.asString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ThreadLocalRandom

/**
 * 在实例的 `saves/` 目录下创建一个可以直接游玩的新世界（议题 #33）
 *
 * 27.6.1 起按 Minecraft 版本分两条路径，避免用一份固定模板硬套所有版本：
 *
 * - **26.1 及之后**：26.1 重构了存档格式
 *   （`difficulty_settings`、独立的 `world_gen_settings.dat` / `game_rules.dat`、
 *   `version_history` 等）。这里直接写 26.x 的真实结构，不再依赖 1.12.2 DataFixer 猜测。
 * - **1.12.2 ~ 26.0**：写 1.12.2 兼容层（`LevelName` / `RandomSeed` / `GameType` /
 *   `Difficulty` / `GameRules.keepInventory` / `generatorName`），
 *   由原版 DataFixer 在首次打开时升级。
 * - **低于 1.12.2**：不提供首页创建世界入口（见 [supports]）。
 *
 * 两条路径都会在写完后再读回一次，确认 `level.dat`（以及 26.x 的
 * `world_gen_settings.dat` / `game_rules.dat`）真的可解析；失败会删除半成品目录。
 *
 * ⚠️ 世界文件夹名与 `LevelName` 是两个概念：
 * `--quickPlaySingleplayer` 需要的是**文件夹名**，不是世界显示名。
 */
object WorldCreator {

    /** 26.1 起使用新的拆分式存档格式 */
    private const val MODERN_26_START = "26.1"

    /** 是否允许在首页创建世界（低于 1.12.2 的旧版本不提供） */
    fun supports(version: Version): Boolean {
        val minecraftVersion = version.getVersionInfo()?.minecraftVersion ?: return true
        return !minecraftVersion.isLowerVer("1.12.2")
    }

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
        allowCheats: Boolean,
    ): String = withContext(Dispatchers.IO) {
        val savesDir = File(version.getGameDir(), "saves").apply { mkdirs() }
        val folderName = uniqueFolderName(savesDir, sanitizeFolderName(levelName))
        val worldDir = File(savesDir, folderName).apply { mkdirs() }

        val minecraftVersion = version.getVersionInfo()?.minecraftVersion
        val modern26 = minecraftVersion != null && !minecraftVersion.isLowerVer(MODERN_26_START)

        try {
            if (modern26) {
                writeModern26(
                    worldDir = worldDir,
                    minecraftVersion = minecraftVersion!!,
                    levelName = levelName,
                    seed = seed,
                    gameMode = gameMode,
                    hardcore = hardcore,
                    difficulty = difficulty,
                    keepInventory = keepInventory,
                    allowCheats = allowCheats
                )
            } else {
                writeLegacy(
                    worldDir = worldDir,
                    levelName = levelName,
                    seed = seed,
                    gameMode = gameMode,
                    hardcore = hardcore,
                    difficulty = difficulty,
                    keepInventory = keepInventory,
                    allowCheats = allowCheats
                )
            }
            verifyWorld(worldDir, modern26)
        } catch (e: Exception) {
            //写入失败时不要留下一个半成品存档目录，避免主页/存档页出现坏档
            worldDir.deleteRecursively()
            throw e
        }
        folderName
    }

    /**
     * 26.1+：按真实的新存档结构写入
     *
     * 结构来自 26.3 服务端实际生成的存档：
     * - `level.dat`：`difficulty_settings` / `spawn` / `Version` / `version_history` 等
     * - `data/minecraft/world_gen_settings.dat`：`data.seed` + 三个维度的生成器
     * - `data/minecraft/game_rules.dat`：`minecraft:keep_inventory`
     */
    private fun writeModern26(
        worldDir: File,
        minecraftVersion: String,
        levelName: String,
        seed: Long,
        gameMode: GameMode,
        hardcore: Boolean,
        difficulty: Difficulty,
        keepInventory: Boolean,
        allowCheats: Boolean,
    ) {
        val dataVersion = dataVersionFor(minecraftVersion)

        val levelRoot = CompoundTag("").apply {
            put(CompoundTag("Data").apply {
                put(CompoundTag("difficulty_settings").apply {
                    put(StringTag("difficulty", difficulty.name.lowercase()))
                    put(ByteTag("hardcore", (if (hardcore) 1 else 0).toByte()))
                    put(ByteTag("locked", (if (hardcore) 1 else 0).toByte()))
                })
                put(LongTag("Time", 0L))
                put(ListTag("version_history").apply {
                    add(IntTag("", dataVersion))
                })
                put(IntTag("GameType", gameMode.levelCode))
                put(ListTag("ServerBrands").apply {
                    add(StringTag("", "vanilla"))
                })
                put(IntTag("version", 19133))
                put(LongTag("LastPlayed", System.currentTimeMillis()))
                put(CompoundTag("spawn").apply {
                    put(IntArrayTag("pos", intArrayOf(0, 64, 0)))
                    put(FloatTag("pitch", 0f))
                    put(StringTag("dimension", "minecraft:overworld"))
                    put(FloatTag("yaw", 0f))
                })
                put(CompoundTag("Version").apply {
                    put(ByteTag("Snapshot", 0.toByte()))
                    put(StringTag("Series", "main"))
                    put(IntTag("Id", dataVersion))
                    put(StringTag("Name", minecraftVersion))
                })
                put(StringTag("LevelName", levelName))
                put(ByteTag("initialized", 1.toByte()))
                put(ByteTag("WasModded", 0.toByte()))
                put(IntTag("DataVersion", dataVersion))
                put(ByteTag("allowCommands", (if (allowCheats) 1 else 0).toByte()))
                put(CompoundTag("DataPacks").apply {
                    put(ListTag("Enabled").apply {
                        add(StringTag("", "vanilla"))
                    })
                    put(ListTag("Disabled").apply {
                        add(StringTag("", "minecart_improvements"))
                        add(StringTag("", "redstone_experiments"))
                        add(StringTag("", "trade_rebalance"))
                    })
                })
            })
        }
        writeGzip(levelRoot, File(worldDir, "level.dat"))

        val worldGenRoot = CompoundTag("").apply {
            put(CompoundTag("data").apply {
                put(ByteTag("bonus_chest", 0.toByte()))
                put(LongTag("seed", seed))
                put(ByteTag("generate_structures", 1.toByte()))
                put(CompoundTag("dimensions").apply {
                    put(vanillaDimension("minecraft:overworld", "minecraft:overworld", "minecraft:overworld"))
                    put(vanillaDimension("minecraft:the_nether", "minecraft:the_nether", "minecraft:nether"))
                    put(CompoundTag("minecraft:the_end").apply {
                        put(CompoundTag("generator").apply {
                            put(StringTag("settings", "minecraft:end"))
                            put(CompoundTag("biome_source").apply {
                                put(StringTag("type", "minecraft:the_end"))
                            })
                            put(StringTag("type", "minecraft:noise"))
                        })
                        put(StringTag("type", "minecraft:the_end"))
                    })
                })
            })
            put(IntTag("DataVersion", dataVersion))
        }
        writeGzip(worldGenRoot, File(worldDir, "data/minecraft/world_gen_settings.dat"))

        val gameRulesRoot = CompoundTag("").apply {
            put(CompoundTag("data").apply {
                put(ByteTag("minecraft:keep_inventory", (if (keepInventory) 1 else 0).toByte()))
            })
            put(IntTag("DataVersion", dataVersion))
        }
        writeGzip(gameRulesRoot, File(worldDir, "data/minecraft/game_rules.dat"))
    }

    private fun vanillaDimension(
        dimensionKey: String,
        dimensionType: String,
        settings: String,
    ): CompoundTag = CompoundTag(dimensionKey).apply {
        put(CompoundTag("generator").apply {
            put(StringTag("settings", "minecraft:$settings"))
            put(CompoundTag("biome_source").apply {
                put(StringTag("preset", "minecraft:$settings"))
                put(StringTag("type", "minecraft:multi_noise"))
            })
            put(StringTag("type", "minecraft:noise"))
        })
        put(StringTag("type", "minecraft:$dimensionType"))
    }

    /**
     * 1.12.2 ~ 26.0：写 1.12.2 兼容层，交给原版 DataFixer 升级
     */
    private fun writeLegacy(
        worldDir: File,
        levelName: String,
        seed: Long,
        gameMode: GameMode,
        hardcore: Boolean,
        difficulty: Difficulty,
        keepInventory: Boolean,
        allowCheats: Boolean,
    ) {
        val data = CompoundTag("Data").apply {
            put(StringTag("LevelName", levelName))
            //1.12.2 的字段：GameType 0 生存 / 1 创造；极限模式是 GameType=0 + hardcore=1
            put(IntTag("GameType", gameMode.levelCode))
            put(ByteTag("hardcore", (if (hardcore) 1 else 0).toByte()))
            put(ByteTag("Difficulty", difficulty.levelCode.toByte()))
            put(ByteTag("DifficultyLocked", (if (hardcore) 1 else 0).toByte()))
            put(ByteTag("allowCommands", (if (allowCheats) 1 else 0).toByte()))
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
        writeGzip(root, File(worldDir, "level.dat"))
    }

    /** 写完回读一次，确认不是坏档 */
    private fun verifyWorld(worldDir: File, modern26: Boolean) {
        val level = NBTIO.readFile(File(worldDir, "level.dat"))
            ?: error("The generated level.dat is not a CompoundTag")
        val data = level.asCompoundTag("Data")
            ?: error("The generated level.dat has no Data entry")
        data.asString("LevelName", null)
            ?: error("The generated level.dat has no LevelName")

        if (!modern26) return

        val worldGen = NBTIO.readFile(File(worldDir, "data/minecraft/world_gen_settings.dat"))
            ?: error("The generated world_gen_settings.dat is not a CompoundTag")
        val worldGenData = worldGen.asCompoundTag("data")
            ?: error("The generated world_gen_settings.dat has no data entry")
        worldGenData.asLong("seed", null)
            ?: error("The generated world_gen_settings.dat has no seed")

        val gameRules = NBTIO.readFile(File(worldDir, "data/minecraft/game_rules.dat"))
            ?: error("The generated game_rules.dat is not a CompoundTag")
        gameRules.asCompoundTag("data")
            ?: error("The generated game_rules.dat has no data entry")
    }

    private fun writeGzip(tag: CompoundTag, file: File) {
        file.parentFile?.mkdirs()
        //level.dat 与 data/*.dat 都是 gzip 压缩的 NBT
        NBTIO.writeFile(tag, file, true, false)
    }

    /**
     * 26.x 的 DataVersion 基线。
     *
     * 26.4 之后没有写进映射表时继续用 5023（26.3）：比目标旧会触发 DataFixer 升级，
     * 比目标新反而会被游戏拒绝，所以宁可取一个确定可用的旧基线。
     */
    private fun dataVersionFor(minecraftVersion: String): Int = when {
        minecraftVersion.startsWith("26.1") -> 4786
        minecraftVersion.startsWith("26.2") -> 4903
        else -> 5023 // 26.3 及之后
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
