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

package com.movtery.zalithlauncher.game.addons.mirror

import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.isChinaMainland

private const val ROOT = "https://bmclapi2.bangbang93.com"

enum class BMCLAPI(val url: String) {
    BASE_URL(ROOT),
    MAVEN("$ROOT/maven"),
    ASSETS("$ROOT/assets"),
    LIBRARIES("$ROOT/libraries")
}

/**
 * [Modified from HMCL](https://github.com/HMCL-dev/HMCL/blob/9aa1367/HMCLCore/src/main/java/org/jackhuang/hmcl/download/BMCLAPIDownloadProvider.java#L64-L83)
 */
private val REPLACE_MIRROR_HOLDERS = mapOf(
    Pair(BMCLAPI.BASE_URL.url, BMCLAPI.BASE_URL.url),
    Pair("https://launchermeta.mojang.com", BMCLAPI.BASE_URL.url),
    Pair("https://piston-meta.mojang.com", BMCLAPI.BASE_URL.url),
    Pair("https://piston-data.mojang.com", BMCLAPI.BASE_URL.url),
    Pair("https://launcher.mojang.com", BMCLAPI.BASE_URL.url),
    Pair("https://libraries.minecraft.net", BMCLAPI.LIBRARIES.url),
    Pair("https://resources.download.minecraft.net", BMCLAPI.ASSETS.url),
    Pair("http://files.minecraftforge.net/maven", BMCLAPI.MAVEN.url),
    Pair("https://files.minecraftforge.net/maven", BMCLAPI.MAVEN.url),
    Pair("https://maven.minecraftforge.net", BMCLAPI.MAVEN.url),
    //⚠️ NeoForge 的库**不只在 net/neoforged 下面**：
    //   securejarhandler（cpw/mods）、asm（org/ow2）、sponge-mixin（net/fabricmc）、
    //   night-config（com/electronwill）、terminalconsoleappender（net/minecrell）
    //   全都挂在同一个 maven.neoforged.net/releases 仓库里。
    //   以前只映射 net/neoforged 子树，其余库只能直连国外源 —— 这就是
    //   「NeoForge 动不动下载失败、一直卡」的主因（47 个库里三分之二没走镜像）。
    //   BMCLAPI 的 /maven 代理的是**整个 maven.neoforged.net 仓库**（实测
    //   /maven/cpw/mods/... 与 /maven/org/ow2/asm/... 都能 200），所以直接映射整段前缀。
    //   镜像缺的制品会快速 404，由换源机制回退官方，不会让原本能下的文件失败。
    Pair("https://maven.neoforged.net/releases", BMCLAPI.MAVEN.url),
    Pair("http://dl.liteloader.com/versions/versions.json", BMCLAPI.MAVEN.url + "/com/mumfrey/liteloader/versions.json"),
    Pair("http://dl.liteloader.com/versions", BMCLAPI.MAVEN.url),
    Pair("https://meta.fabricmc.net", BMCLAPI.BASE_URL.url + "/fabric-meta"),
    Pair("https://maven.fabricmc.net", BMCLAPI.MAVEN.url),
    Pair("https://authlib-injector.yushi.moe", BMCLAPI.BASE_URL.url + "/mirrors/authlib-injector"),
    Pair("https://repo1.maven.org/maven2", "https://mirrors.cloud.tencent.com/nexus/repository/maven-public"),
    Pair("https://repo.maven.apache.org/maven2", "https://mirrors.cloud.tencent.com/nexus/repository/maven-public"),
    Pair("https://hmcl-dev.github.io/metadata/cleanroom", "https://alist.8mi.tech/d/mirror/HMCL-Metadata/Auto/cleanroom")
)

/**
 * 替换为镜像源链接并按用户偏好排序；非中国大陆不注入任何镜像。
 */
fun String.mapBMCLMirrorUrls(): List<String> {
    if (!isChinaMainland()) return listOf(this)

    val mirrorUrl = REPLACE_MIRROR_HOLDERS.entries.firstOrNull { (origin, _) ->
        this.startsWith(origin)
    }?.let { (origin, mirror) ->
        replaceFirst(origin, mirror)
    }

    return orderCandidates(
        official = this,
        mirror = mirrorUrl,
        priority = resolveMirrorPriority(
            source = AllSettings.gameDownloadSource.getValue(),
            mainland = true
        )
    )
}