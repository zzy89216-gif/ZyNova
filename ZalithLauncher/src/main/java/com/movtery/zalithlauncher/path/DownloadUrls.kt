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

package com.movtery.zalithlauncher.path

import com.movtery.zalithlauncher.setting.AllSettings
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

/**
 * 下载源修改器（议题 #37 / #41）
 *
 * 用户可以在「设置 → 网络与下载 → 下载源修改器」里给几个下载服务分别换一个地址
 * （自建镜像、加速代理等）。这里负责把「用户填的值」变成「实际要用的地址」。
 *
 * 三条硬性规则：
 * 1. **留空 = 官方默认地址**，旧用户什么都不用做，行为与升级前完全一致；
 * 2. **填错不会把下载搞坏**：地址必须是完整的 `http(s)://host[/path]`，
 *    校验不通过就当作没填，静默回退默认值（界面上会同步提示格式不对）；
 * 3. **每个服务分别处理**：它们的数据格式和鉴权方式完全不同，
 *    不能把一个服务的地址套到另一个服务上。
 *
 * ⚠️ 不要把这些地址改成 `const val` 再写死到调用点：
 * 那样用户改了设置也不会生效，等于做了个假的设置项。
 */
object DownloadUrls {
    /** Minecraft 版本清单（Mojang 官方） */
    const val DEFAULT_VERSION_MANIFEST =
        "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"

    /** Modrinth 官方接口 */
    const val DEFAULT_MODRINTH_API = "https://api.modrinth.com/v2"

    /** CurseForge 官方接口 */
    const val DEFAULT_CURSEFORGE_API = "https://api.curseforge.com/v1"

    /** MCIM 镜像站根地址（国内访问 CurseForge / Modrinth 的加速源） */
    const val DEFAULT_MCIM_ROOT = "https://mod.mcimirror.top"

    /** 版本清单实际使用的地址 */
    fun versionManifest(): String = resolveOrNull(
        AllSettings.customVersionManifestUrl.getValue()
    ) ?: DEFAULT_VERSION_MANIFEST

    /** Modrinth 接口实际使用的地址（不带结尾斜杠） */
    fun modrinthApi(): String = (resolveOrNull(
        AllSettings.customModrinthApiUrl.getValue()
    ) ?: DEFAULT_MODRINTH_API).trimEnd('/')

    /** CurseForge 接口实际使用的地址（不带结尾斜杠） */
    fun curseForgeApi(): String = (resolveOrNull(
        AllSettings.customCurseForgeApiUrl.getValue()
    ) ?: DEFAULT_CURSEFORGE_API).trimEnd('/')

    /** MCIM 镜像站根地址（不带结尾斜杠） */
    fun mcimRoot(): String = (resolveOrNull(
        AllSettings.customMcimMirrorUrl.getValue()
    ) ?: DEFAULT_MCIM_ROOT).trimEnd('/')

    /** 镜像站上的 Modrinth 接口 */
    fun mcimModrinthApi(): String = "${mcimRoot()}/modrinth/v2"

    /** 镜像站上的 CurseForge 接口 */
    fun mcimCurseForgeApi(): String = "${mcimRoot()}/curseforge/v1"

    /**
     * 用户输入是否是一个「能用的」下载地址。
     *
     * 只做格式校验（协议 + 主机名），不做连通性探测：
     * 下载设置页不该因为用户手机当前没网就一直报错。
     */
    fun isValid(raw: String): Boolean = resolveOrNull(raw) != null

    /** 留空视为「没填」；填了但不合法也返回 null（调用方回退默认值） */
    private fun resolveOrNull(raw: String): String? {
        val value = raw.trim()
        if (value.isEmpty()) return null
        val url = value.toHttpUrlOrNull() ?: return null
        if (url.host.isBlank()) return null
        if (url.scheme != "http" && url.scheme != "https") return null
        return value.trimEnd('/')
    }
}
