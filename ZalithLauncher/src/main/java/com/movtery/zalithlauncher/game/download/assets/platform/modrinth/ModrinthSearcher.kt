/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.game.download.assets.platform.modrinth

import com.movtery.zalithlauncher.game.download.assets.platform.AbstractPlatformSearcher
import com.movtery.zalithlauncher.game.download.assets.platform.Platform
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformClasses
import com.movtery.zalithlauncher.game.download.assets.platform.PlatformSearchFilter
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthSingleProject
import com.movtery.zalithlauncher.game.download.assets.platform.modrinth.models.ModrinthVersion
import com.movtery.zalithlauncher.path.DownloadUrls
import com.movtery.zalithlauncher.utils.network.httpGetJson
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.Parameters
import java.io.File

class ModrinthSearcher(
    //默认地址在每次构造时从「下载源修改器」读取，用户改了设置就能立刻生效
    val api: String = DownloadUrls.modrinthApi(),
    source: String = "Official Modrinth"
): AbstractPlatformSearcher(
    platform = Platform.MODRINTH,
    source = source
) {
    override suspend fun searchAssets(
        query: String,
        searchFilter: PlatformSearchFilter,
        platformClasses: PlatformClasses
    ): ModrinthSearchResult {
        return httpGetJson(
            url = "$api/search",
            parameters = searchFilter.toModrinthRequest(
                query = query,
                platformClasses = platformClasses
            ).toParameters()
        )
    }

    override suspend fun getProject(projectID: String): ModrinthSingleProject {
        val project = httpGetJson<ModrinthSingleProject>(
            url = "$api/project/$projectID"
        )
        // 默认不做处理，不可访问的话，Modrinth 那边自己就会返回 404
//        if (!project.isPublic()) throw NotFoundException("The project {$projectID} is not in a publicly available state.")
        return project
    }

    /**
     * 获取 Modrinth 项目的版本列表（可设置区间）
     * @param pageSize 每页请求数量，null则为获取所有版本
     * @param offset 开始处，null则为获取所有版本
     */
    suspend fun getVersionsChunk(
        projectID: String,
        pageSize: Int? = null,
        offset: Int? = null,
    ): List<ModrinthVersion> {
        return httpGetJson(
            url = "$api/project/$projectID/version",
            parameters = if (pageSize != null && offset != null) {
                Parameters.build {
                    append("limit", pageSize.toString())
                    append("offset", offset.toString())
                    append("include_changelog", "false")
                }
            } else null
        )
    }

    override suspend fun getVersions(
        projectID: String,
        pageCallback: (chunk: Int, page: Int) -> Unit
    ): List<ModrinthVersion> {
        return getVersionsChunk(
            projectID = projectID
        )
    }

    override suspend fun getVersionByLocalFile(
        file: File,
        sha1: String
    ): ModrinthVersion? {
        val version = try {
            httpGetJson<ModrinthVersion>(
                url = "$api/version_file/$sha1",
                parameters = Parameters.build {
                    append("algorithm", "sha1")
                }
            )
        } catch (_: ClientRequestException) {
            null
        } ?: return null

        //⚠️ 反查回来的对象默认没有初始化主要文件，直接读 platformFileName() 等会抛
        //`lateinit property thisPrimaryFile has not been initialized`。这里补一次初始化，
        //失败就当作「认不出这个文件」（收藏安装 / 更新检查会走降级路径）。
        return if (version.initFile(version.projectId)) version else null
    }

    override suspend fun getVersionById(
        versionID: String
    ): ModrinthVersion? {
        val version = try {
            httpGetJson<ModrinthVersion>(url = "$api/version/$versionID")
        } catch (_: ClientRequestException) {
            null
        } ?: return null
        //同上：给调用方一个「文件信息已就绪」的版本对象
        return if (version.initFile(version.projectId)) version else null
    }
}