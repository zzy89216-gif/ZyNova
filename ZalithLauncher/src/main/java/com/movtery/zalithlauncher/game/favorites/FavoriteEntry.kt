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

import com.movtery.zalithlauncher.R
import kotlinx.serialization.Serializable

/**
 * 收藏资源的类型（议题 #39）
 *
 * 识别顺序是「先看文件结构，再看扩展名」：
 * 只靠 `.jar` / `.zip` 后缀判断会把随便一个压缩包当成模组，
 * 所以真正的类型由 [FavoriteImporter] 打开文件结构后决定。
 */
@Serializable
enum class FavoriteType(val textRes: Int) {
    MOD(R.string.favorites_type_mod),
    MODPACK(R.string.favorites_type_modpack),
    RESOURCE_PACK(R.string.favorites_type_resource_pack),
    SHADER(R.string.favorites_type_shader),
    DATAPACK(R.string.favorites_type_datapack),
    WORLD(R.string.favorites_type_world),
    GAME_CORE(R.string.favorites_type_game_core),
    UNKNOWN(R.string.favorites_type_unknown)
}

/**
 * 一条「本地归档」收藏记录。
 *
 * 与「在线收藏」的区别：
 * - 在线收藏只需要 projectId + 平台，不要求本地有文件；
 * - 本地归档必须真的有一份文件存在 ZyNova 的私有目录里（[fileName] 就是它）。
 *
 * 两者可以同时存在，也可以各自独立删除（见 [FavoriteStore]）。
 *
 * @param fileName 相对 `favorites/files/` 的文件名，**不要存绝对路径** ——
 *                 应用被移动到别的存储位置后绝对路径会失效
 */
@Serializable
data class FavoriteEntry(
    val id: String,
    val name: String,
    val type: FavoriteType,
    /** 来源：local（从手机导入）/ 资源平台名称 */
    val source: String,
    val importedAt: Long,
    val fileName: String,
    val size: Long,
    val sha256: String,
    /** 在线收藏信息（可空：本地归档不要求在线来源） */
    val onlineProjectId: String? = null,
    val onlinePlatform: String? = null,
    val note: String? = null
)
