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

package com.movtery.zalithlauncher.game.crash

import androidx.annotation.StringRes
import com.movtery.zalithlauncher.R

/**
 * 一条崩溃分析结论：原因 + 怎么处理。
 *
 * 这里只做**基于日志文本的启发式判断**，不保证 100% 命中；
 * 目的是把「看日志找原因」变成「先看最可能的三五条」。
 */
data class CrashFinding(
    @StringRes val title: Int,
    @StringRes val detail: Int,
    @StringRes val action: Int,
    val args: List<String> = emptyList(),
)

/**
 * 游戏崩溃日志的轻量分析器。
 *
 * 规则来自常见启动器/整合包崩溃模式，按「越具体越靠前」的顺序匹配：
 * 缺前置 → 模组加载失败 → 重复模组 → Java 版本 → 内存 → 渲染器 → 文件损坏 → 缺类。
 */
object CrashAnalyzer {
    private const val MAX_FINDINGS = 6

    private val ignoreCase = setOf(RegexOption.IGNORE_CASE)
    private val modIdRegex = Regex("""Mod ID:\s*['"]?([A-Za-z0-9_.-]+)""", ignoreCase)
    private val dependencyRegex = Regex(
        """Mod ID:\s*['"]([^'"]+)['"]\s*,\s*Requested by:\s*['"]([^'"]+)['"]""",
        ignoreCase
    )
    private val jarRegex = Regex("""mods[\\/]([^\\/\s"']+\.jar)""", ignoreCase)
    private val mixinRegex = Regex("""Mixin apply failed[^\n]*?([A-Za-z0-9_.-]+)\.mixins""", ignoreCase)
    private val missingDependency = Regex("""requires\s+.+?\s+which is missing""", ignoreCase)
    private val modLoadFailure = Regex(
        """(Failed to load mod|has failed to load correctly|Mixin apply failed|MixinTransformerError|ModLoadingException)""",
        ignoreCase
    )
    private val rendererFailure = Regex(
        """(Failed to create window|GLFW error|EGL_BAD|Could not initialize|Pixel format not accelerated|Failed to initialize GL|OpenGL.*(?:error|failed))""",
        ignoreCase
    )
    private val corruptFile = Regex(
        """(ZipException|invalid LOC header|invalid distance|corrupt|CRC32)""",
        ignoreCase
    )
    private val classFailure = Regex("""(NoClassDefFoundError|ClassNotFoundException)""", ignoreCase)

    fun analyze(raw: String): List<CrashFinding> {
        val text = raw.takeLast(512 * 1024)
        val findings = mutableListOf<CrashFinding>()

        fun add(finding: CrashFinding) {
            if (findings.none { it.title == finding.title && it.args == finding.args }) {
                findings += finding
            }
        }

        // 1. 缺前置：Forge/NeoForge 会把缺失依赖逐条列出来
        val dependencies = dependencyRegex.findAll(text).take(3).toList()
        if (dependencies.isNotEmpty()) {
            dependencies.forEach { match ->
                val missing = match.groupValues[1]
                val requestedBy = match.groupValues[2]
                add(
                    CrashFinding(
                        title = R.string.crash_analysis_missing_title,
                        detail = R.string.crash_analysis_missing_detail,
                        action = R.string.crash_analysis_missing_action,
                        args = listOf(missing, requestedBy)
                    )
                )
            }
        } else if (text.contains("Missing or unsupported mandatory dependencies", true) ||
            text.contains("MissingModsException", true) ||
            missingDependency.containsMatchIn(text)
        ) {
            add(
                CrashFinding(
                    title = R.string.crash_analysis_missing_generic_title,
                    detail = R.string.crash_analysis_missing_generic_detail,
                    action = R.string.crash_analysis_missing_generic_action
                )
            )
        }

        // 2. 模组加载失败 / Mixin 注入失败
        if (modLoadFailure.containsMatchIn(text)) {
            val mod = modIdRegex.find(text)?.groupValues?.get(1)
                ?: mixinRegex.find(text)?.groupValues?.get(1)
                ?: jarRegex.find(text)?.groupValues?.get(1)?.removeSuffix(".jar")
                ?: text
            add(
                CrashFinding(
                    title = R.string.crash_analysis_mod_failed_title,
                    detail = R.string.crash_analysis_mod_failed_detail,
                    action = R.string.crash_analysis_mod_failed_action,
                    args = listOf(mod)
                )
            )
        }

        // 3. 重复模组
        if (text.contains("Duplicate mod", true) || text.contains("duplicate mods", true)) {
            add(
                CrashFinding(
                    title = R.string.crash_analysis_duplicate_title,
                    detail = R.string.crash_analysis_duplicate_detail,
                    action = R.string.crash_analysis_duplicate_action
                )
            )
        }

        // 4. Java 版本不匹配
        if (text.contains("UnsupportedClassVersionError", true) ||
            text.contains("compiled by a more recent version of the Java Runtime", true)
        ) {
            add(
                CrashFinding(
                    title = R.string.crash_analysis_java_title,
                    detail = R.string.crash_analysis_java_detail,
                    action = R.string.crash_analysis_java_action
                )
            )
        }

        // 5. 内存不足
        if (text.contains("OutOfMemoryError", true) ||
            text.contains("Java heap space", true) ||
            text.contains("insufficient memory", true)
        ) {
            add(
                CrashFinding(
                    title = R.string.crash_analysis_memory_title,
                    detail = R.string.crash_analysis_memory_detail,
                    action = R.string.crash_analysis_memory_action
                )
            )
        }

        // 6. 渲染器 / 图形驱动初始化失败
        if (rendererFailure.containsMatchIn(text)) {
            add(
                CrashFinding(
                    title = R.string.crash_analysis_renderer_title,
                    detail = R.string.crash_analysis_renderer_detail,
                    action = R.string.crash_analysis_renderer_action
                )
            )
        }

        // 7. 文件损坏
        if (corruptFile.containsMatchIn(text)) {
            add(
                CrashFinding(
                    title = R.string.crash_analysis_corrupt_title,
                    detail = R.string.crash_analysis_corrupt_detail,
                    action = R.string.crash_analysis_corrupt_action
                )
            )
        }

        // 8. 缺类 / 依赖版本不匹配
        if (classFailure.containsMatchIn(text)) {
            add(
                CrashFinding(
                    title = R.string.crash_analysis_class_title,
                    detail = R.string.crash_analysis_class_detail,
                    action = R.string.crash_analysis_class_action
                )
            )
        }

        if (findings.isEmpty() && text.contains("Exception", true)) {
            add(
                CrashFinding(
                    title = R.string.crash_analysis_generic_title,
                    detail = R.string.crash_analysis_generic_detail,
                    action = R.string.crash_analysis_generic_action
                )
            )
        }

        return findings.take(MAX_FINDINGS)
    }
}
