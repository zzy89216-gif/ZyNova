/*
 * ZyNova Launcher
 * Copyright (C) 2026 ZyNova Contributors
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

import com.movtery.zalithlauncher.game.download.assets.platform.Platform

/**
 * 模组的稳定身份与受保护依赖判定
 *
 * 资源来源里的「项目 ID」、模组元数据里的 `modId` 与界面上显示的模组名称
 * 是三套不同的东西，**不能互相代替**：
 * - Fabric API 在 Modrinth 的项目 ID 是 `P7dR8mSH`
 * - Fabric API 在 CurseForge 的项目 ID 是 `306612`
 * - Fabric API 在 `fabric.mod.json` 里的模组 ID 是 `fabric-api`
 *
 * 因此判断「这是不是同一个模组」「这是不是受保护的 Fabric API」时，
 * 必须使用这里的稳定标识，而不是 [LocalMod.name] 这类展示名称。
 */
object ModIdentity {
    /** Fabric API 在 fabric.mod.json 里的模组 ID */
    const val FABRIC_API_MOD_ID = "fabric-api"

    /** Fabric API 在 Modrinth 上的项目 ID */
    const val FABRIC_API_MODRINTH_ID = "P7dR8mSH"

    /** Fabric API 在 CurseForge 上的项目 ID */
    const val FABRIC_API_CURSEFORGE_ID = "306612"

    /**
     * 判断某个平台项目是否为 Fabric API
     *
     * 只认各来源固定的项目 ID，不按显示名称判断。
     */
    fun isFabricApiProject(platform: Platform, projectId: String): Boolean = when (platform) {
        Platform.MODRINTH -> projectId.equals(FABRIC_API_MODRINTH_ID, ignoreCase = true)
        Platform.CURSEFORGE -> projectId == FABRIC_API_CURSEFORGE_ID
    }

    /**
     * 判断本地模组文件是否为 Fabric API
     *
     * 使用解析出来的模组元数据 ID（禁用状态会去掉 `.disabled` 后再解析），
     * 不依赖文件名或显示名称。
     */
    fun isFabricApi(localMod: LocalMod): Boolean =
        localMod.id.equals(FABRIC_API_MOD_ID, ignoreCase = true)

    /**
     * 是否禁止在模组管理界面直接删除
     *
     * Fabric API 是大量 Fabric 模组的必需前置，被单独删掉后游戏会在启动时崩溃，
     * 因此模组管理界面不允许直接删除它；删除整个实例仍然走实例管理流程，不受此限制。
     */
    fun isDeleteProtected(localMod: LocalMod): Boolean = isFabricApi(localMod)

    /**
     * 去重时使用的规范项目键
     *
     * Fabric API 在两个来源上的项目 ID 不同，但它们是同一个模组：
     * 只有把它们映射到同一个键，才能避免同一次安装里装出两份 Fabric API。
     */
    fun canonicalProjectKey(platform: Platform, projectId: String): String =
        if (isFabricApiProject(platform, projectId)) {
            FABRIC_API_MOD_ID
        } else {
            "${platform.name}:$projectId"
        }
}
