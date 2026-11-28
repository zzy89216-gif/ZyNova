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

package com.movtery.zalithlauncher.upgrade

import android.os.Build
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 已知的 CPU 架构标记
 *
 * 按长度降序匹配，避免短标记（"x86"）错误命中长标记（"x86_64"）。
 */
private val KNOWN_ABIS = listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
    .sortedByDescending { it.length }

/**
 * 该构建产物所属的 CPU 架构；不包含任何已知架构标记时返回 null（通用包）
 */
fun ZyNovaRelease.Asset.abiOrNull(): String? =
    KNOWN_ABIS.firstOrNull { name.contains(it, ignoreCase = true) }

/**
 * ZyNova 自有更新体系的数据模型。
 *
 * ZyNova 只维护自己的更新体系：直接以 ZyNova 仓库的 GitHub Releases
 * 作为唯一的版本信息来源，不再依赖任何外部版本信息文件、
 * Base64 内容接口以及网盘分发链路。
 *
 * @param tagName 发布标签，例如 v26.1.0
 * @param name 发布标题
 * @param body 发布说明（Markdown），即该版本的更新日志
 * @param publishedAt 发布时间（ISO-8601）
 * @param htmlUrl 发布页面链接
 * @param prerelease 是否为预发布版本
 * @param assets 该版本附带的构建产物
 */
@Serializable
data class ZyNovaRelease(
    @SerialName("tag_name")
    val tagName: String,
    @SerialName("html_url")
    val htmlUrl: String = "",
    @SerialName("name")
    val name: String? = null,
    @SerialName("body")
    val body: String? = null,
    @SerialName("published_at")
    val publishedAt: String? = null,
    @SerialName("prerelease")
    val prerelease: Boolean = false,
    @SerialName("assets")
    val assets: List<Asset> = emptyList()
) {
    /**
     * 附带的构建产物
     * @param name 文件名
     * @param downloadUrl 可直接下载的链接
     * @param size 文件大小（bytes）
     */
    @Serializable
    data class Asset(
        @SerialName("name")
        val name: String,
        @SerialName("browser_download_url")
        val downloadUrl: String,
        @SerialName("size")
        val size: Long = 0L
    )

    /** 归一化后的版本号（去掉前缀 v / V），例如 26.1.0 */
    val version: String
        get() = tagName.trim().removePrefix("v").removePrefix("V").trim()

    /**
     * 根据当前设备实际支持的 ABI 自动选择最合适的安装包。
     *
     * 用户不需要自己挑选架构：能自动判断，就不让用户选择。
     */
    fun pickAsset(): Asset? {
        val apks = assets.filter { it.name.endsWith(".apk", ignoreCase = true) }
        if (apks.isEmpty()) return null

        //按设备实际支持顺序匹配精确架构
        Build.SUPPORTED_ABIS.forEach { abi ->
            apks.firstOrNull { it.abiOrNull() == abi }?.let { return it }
        }
        //回退到通用包（文件名不含任何架构标记）
        apks.firstOrNull { it.abiOrNull() == null }?.let { return it }
        //最后回退到任意安装包
        return apks.firstOrNull()
    }

    companion object {
        /**
         * 比较两个启动器版本号（形如 26.1.0）
         *
         * @return 若 [a] 比 [b] 新则返回正数，更旧返回负数，相同返回 0
         */
        fun compareVersion(a: String, b: String): Int {
            val pa = a.toVersionParts()
            val pb = b.toVersionParts()
            for (i in pa.indices) {
                val result = pa[i].compareTo(pb[i])
                if (result != 0) return result
            }
            return 0
        }

        /**
         * 将版本号字符串拆分为可比较的数字段，最多比较 4 段
         */
        private fun String.toVersionParts(): List<Int> {
            val numbers = split('.', '-', '+', '_')
                .mapNotNull { it.trim().toIntOrNull() }
            return List(4) { index -> numbers.getOrElse(index) { 0 } }
        }
    }
}
