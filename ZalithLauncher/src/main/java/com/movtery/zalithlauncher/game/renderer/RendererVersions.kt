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

package com.movtery.zalithlauncher.game.renderer

import com.movtery.zalithlauncher.game.renderer.ironizedzink.IRONIZED_ZINK_UNIQUE_IDENTIFIER
import com.movtery.zalithlauncher.game.renderer.mobileglues.MOBILE_GLUES_UNIQUE_IDENTIFIER
import com.movtery.zalithlauncher.game.renderer.renderers.GL4ESRenderer

/**
 * 内置渲染器「到底是哪个版本」的唯一事实来源（议题 #36）
 *
 * 为什么要单独放一个文件：
 * - 两个渲染器都是**随包内置的预编译二进制**，`.so` 里没有版本字符串、
 *   AAR 里也没有 versionName，升级时最容易出现「文档说 2.0.0、包内其实还是旧的」；
 * - 以前版本号只写在 `THIRD_PARTY_NOTICES.md` 和 HANDOFF 里，界面完全看不到。
 *
 * 现在把版本号收在这里，设置页直接显示；**以后升级渲染器时改这一处 + 换二进制 +
 * 同步 THIRD_PARTY_NOTICES**，三件事对齐即可。
 *
 * ⚠️ 这里的值必须与 `THIRD_PARTY_NOTICES.md` 记录的官方发布版一致，
 * 不要凭印象改；改完请把对应的官方 tag 一并写进提交说明。
 */
object RendererVersions {
    /** MobileGlues：官方 release tag `V2.0.0`（2026-08-09） */
    const val MOBILE_GLUES = "2.0.0"

    /** Ironized Zink：官方 release tag `v1.1.0`（2026-07-15，commit 88f7c202） */
    const val IRONIZED_ZINK = "1.1.0"

    /** GL4ES：`gl4es_extra_extra`，随包的是 `libgl4es_114.so` */
    const val GL4ES = "1.1.4"

    /**
     * 按渲染器唯一标识取版本号。
     * 插件渲染器 / 「自动」这种没有内置二进制的，返回 null（界面显示成「跟随插件」）。
     */
    fun of(identifier: String): String? = when (identifier) {
        MOBILE_GLUES_UNIQUE_IDENTIFIER -> MOBILE_GLUES
        IRONIZED_ZINK_UNIQUE_IDENTIFIER -> IRONIZED_ZINK
        //直接问渲染器自己，避免在这里再抄一份 UUID（UUID 是用户配置里的持久值，不能改）
        GL4ESRenderer.getUniqueIdentifier() -> GL4ES
        else -> null
    }
}
