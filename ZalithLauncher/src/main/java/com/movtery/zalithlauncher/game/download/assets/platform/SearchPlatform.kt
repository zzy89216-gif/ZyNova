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

package com.movtery.zalithlauncher.game.download.assets.platform

import androidx.annotation.StringRes
import com.movtery.zalithlauncher.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 资源搜索的目标平台选项
 *
 * 在原有的 CurseForge / Modrinth 之外，额外提供「所有」：
 * 选择「所有」时会**同时搜索全部来源并把结果合并到一起**，
 * 用户不需要在两个平台之间来回切换。
 *
 * 注意：[ALL] 的 [platform] 为 null，表示不是单一来源。
 *
 * 旧配置兼容：枚举名与 [Platform] 保持一致（CURSEFORGE / MODRINTH），
 * 因此升级前保存的平台设置可以直接反序列化。
 */
@Serializable
enum class SearchPlatform(
    /** 对应的具体来源；「所有」为 null */
    val platform: Platform?,
    val displayName: String,
    @field:StringRes
    val textRes: Int
) {
    @SerialName("ALL")
    ALL(
        platform = null,
        displayName = "All",
        textRes = R.string.download_assets_platform_all
    ),

    @SerialName("CURSEFORGE")
    CURSEFORGE(
        platform = Platform.CURSEFORGE,
        displayName = Platform.CURSEFORGE.displayName,
        textRes = R.string.download_assets_platform_curseforge
    ),

    @SerialName("MODRINTH")
    MODRINTH(
        platform = Platform.MODRINTH,
        displayName = Platform.MODRINTH.displayName,
        textRes = R.string.download_assets_platform_modrinth
    );

    /** 是否为「同时搜索全部来源」 */
    val isAll: Boolean get() = platform == null

    /** 是否包含 CurseForge 来源（CurseForge 的版本过滤只能使用正式版） */
    val selectsCurseForge: Boolean
        get() = this == ALL || platform == Platform.CURSEFORGE

    companion object {
        /** 需要聚合搜索的全部具体来源 */
        val ALL_PLATFORMS: List<Platform> = Platform.entries.toList()

        /**
         * 由具体来源反查选项
         */
        fun of(platform: Platform): SearchPlatform = when (platform) {
            Platform.CURSEFORGE -> CURSEFORGE
            Platform.MODRINTH -> MODRINTH
        }
    }
}
