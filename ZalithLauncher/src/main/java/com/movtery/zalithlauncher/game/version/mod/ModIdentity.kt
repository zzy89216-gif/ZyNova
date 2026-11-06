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
 * 模组的稳定身份与「受保护前置 API」判定
 *
 * 资源来源里的「项目 ID」、模组元数据里的 `modId` 与界面上显示的模组名称
 * 是三套不同的东西，**不能互相代替**：
 * - Fabric API 在 Modrinth 的项目 ID 是 `P7dR8mSH`
 * - Fabric API 在 CurseForge 的项目 ID 是 `306612`
 * - Fabric API 在 `fabric.mod.json` 里的模组 ID 是 `fabric-api`
 *
 * 因此判断「这是不是同一个模组」「这是不是受保护的前置 API」时，
 * 必须使用这里的稳定标识，而不是 [LocalMod.name] 这类展示名称。
 *
 * 27.10.2（议题 #42 · 任务 4）：受保护的组件从「只有 Fabric API」扩展成
 * **内置模组加载器对应的整套前置 API**。这样 Quilt / Legacy Fabric 玩家
 * 也会得到和 Fabric 玩家完全一致的待遇：搜不到、装不上、删不掉、禁不掉、
 * AI 也拦得住，同一个实例里只会存在一份。
 */
object ModIdentity {

    /**
     * 受保护的「前置 API」组件
     *
     * 这些模组是整条模组加载器链路的地基：玩家手动删掉 / 禁用其中一个，
     * 依赖它的成百上千个模组在启动时就会连锁崩溃，而且玩家很难从崩溃日志
     * 里看出「原来是我删了 API」。所以启动器层面直接锁死。
     *
     * @param modId 模组元数据里 `id` 的规范值
     * @param modIdAliases 同一个组件在历史版本 / 分支里出现过的其它 modId
     * @param displayName 展示名称（只用于提示文案，判定一律用 ID）
     * @param modrinthId Modrinth 项目 ID
     * @param curseForgeId CurseForge 项目 ID；**为 null 表示该来源尚未核实**，
     *        此时只锁 Modrinth，绝不按名称瞎猜
     */
    enum class ProtectedApi(
        val modId: String,
        val modIdAliases: Set<String>,
        val displayName: String,
        val modrinthId: String,
        val curseForgeId: String?
    ) {
        /** Fabric 官方的前置 API */
        FABRIC_API(
            modId = "fabric-api",
            modIdAliases = setOf("fabric", "fabric_api"),
            displayName = "Fabric API",
            modrinthId = "P7dR8mSH",
            curseForgeId = "306612"
        ),

        /**
         * Quilt 的 Fabric API 兼容层（Quilted Fabric API）
         *
         * CurseForge 上的项目 ID 尚未核实（见议题 #42 任务 4 的备注），
         * 因此这里只登记 Modrinth ID；CurseForge 侧暂时不做拦截，
         * 宁可少拦一个来源，也不按显示名称误伤别的模组。
         */
        QUILTED_FABRIC_API(
            modId = "quilted_fabric_api",
            modIdAliases = setOf("quilted_fabric_api_base", "quilted-fabric-api"),
            displayName = "Quilted Fabric API",
            modrinthId = "qvIfYCYJ",
            curseForgeId = null
        ),

        /**
         * Legacy Fabric API（1.13 以下 Fabric 的前置 API）
         *
         * 同样只有 Modrinth ID 已核实。
         */
        LEGACY_FABRIC_API(
            modId = "legacy-fabric-api",
            modIdAliases = setOf("legacy_fabric_api", "legacyfabric-api"),
            displayName = "Legacy Fabric API",
            modrinthId = "9CJED7xi",
            curseForgeId = null
        )
    }

    /** Fabric API 在 fabric.mod.json 里的模组 ID（保留给旧调用点） */
    const val FABRIC_API_MOD_ID = "fabric-api"

    /** 所有受保护组件的规范 modId */
    val protectedModIds: Set<String> = ProtectedApi.entries.map { it.modId }.toSet()

    /**
     * 按平台项目 ID 找到受保护的组件
     *
     * 只认各来源固定的项目 ID，不按显示名称判断；
     * 未核实该来源 ID 的组件返回 null（宁缺勿错）。
     */
    fun protectedApiOfProject(platform: Platform, projectId: String): ProtectedApi? =
        ProtectedApi.entries.firstOrNull { api ->
            when (platform) {
                Platform.MODRINTH -> projectId.equals(api.modrinthId, ignoreCase = true)
                Platform.CURSEFORGE -> api.curseForgeId?.let { projectId == it } == true
            }
        }

    /** 判断某个平台项目是否为受保护的前置 API */
    fun isProtectedApiProject(platform: Platform, projectId: String): Boolean =
        protectedApiOfProject(platform, projectId) != null

    /**
     * 按本地模组元数据找到受保护的组件
     *
     * 使用解析出来的模组元数据 ID（禁用状态会去掉 `.disabled` 后再解析），
     * 不依赖文件名或显示名称。
     */
    fun protectedApiOfMod(localMod: LocalMod): ProtectedApi? {
        val id = localMod.id.trim()
        if (id.isEmpty()) return null
        return ProtectedApi.entries.firstOrNull { api ->
            id.equals(api.modId, ignoreCase = true) ||
                    api.modIdAliases.any { alias -> id.equals(alias, ignoreCase = true) }
        }
    }

    /** 判断本地模组文件是否为受保护的前置 API */
    fun isProtectedApi(localMod: LocalMod): Boolean = protectedApiOfMod(localMod) != null

    /** 本地模组的展示名称，用于「这是前置 API，不能删」这类提示 */
    fun protectedApiName(localMod: LocalMod): String =
        protectedApiOfMod(localMod)?.displayName ?: localMod.name

    /**
     * 是否禁止在模组管理界面直接删除
     *
     * 前置 API 是大量模组的必需依赖，被单独删掉后游戏会在启动时崩溃，
     * 因此模组管理界面不允许直接删除它；删除整个实例仍然走实例管理流程，不受此限制。
     */
    fun isDeleteProtected(localMod: LocalMod): Boolean = isProtectedApi(localMod)

    /**
     * 是否禁止在模组管理界面直接禁用
     *
     * 禁用前置 API 与删除它的后果完全一样（游戏启动即崩），所以一并锁死。
     */
    fun isDisableProtected(localMod: LocalMod): Boolean = isProtectedApi(localMod)

    /**
     * 去重时使用的规范项目键
     *
     * 同一个前置 API 在不同来源上的项目 ID 不同，但它们是同一个模组：
     * 只有把它们映射到同一个键，才能避免同一次安装里装出两份 Fabric API（或两份 QFAPI）。
     */
    fun canonicalProjectKey(platform: Platform, projectId: String): String =
        protectedApiOfProject(platform, projectId)?.modId ?: "${platform.name}:$projectId"
}
