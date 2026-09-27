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

package com.movtery.zalithlauncher.game.renderer.ironizedzink

import com.movtery.zalithlauncher.path.PathManager

/**
 * Ironized Zink 的完整原生配置模型。
 *
 * 本文件移植自 Ironized Zink（作者 GoyDevv，GPL-3.0）的 `Presets.kt`：
 * https://github.com/GoyDevv/IronizedZink
 *
 * 与上游的差异（修改说明）：
 * - 上游把配置写到 `/sdcard/IronizedZink/ironized.env`，再由 `libironized_zink.so`
 *   在游戏进程里 `setenv()`；内置到启动器后不再需要该文件与 shim，
 *   启动器直接把同一组环境变量注入到游戏进程（见 [IronizedZinkRenderer.getRendererEnv]）。
 * - 上游 `RenderOptions` 的字段与 4 个官方预设的取值**完全保留**，未做任何增删或改值。
 * - 新增 [glslFor] 与启动器侧取值入口（`IronizedZinkSettings.kt`），仅为接入启动器所需。
 *
 * 每个选项都对应一个 Mesa / Zink / Pojav 真正会读取的环境变量。
 */
object Zink {
    /** 渲染器 ID，会被写入环境变量 `POJAV_RENDERER` */
    const val RENDERER_ID = "opengles3_desktopgl_zink_kopper"

    /** 需要 dlopen 的图形库（Mesa GLX shim） */
    const val GL_NAME = "libglxshim.so"

    /** EGL 实现（Mesa EGL） */
    const val EGL_NAME = "libEGL_mesa.so"

    /** Mesa 版本（与随启动器分发的 kopper-zink 原生库一致） */
    const val MESA_VERSION = "23.0.4"

    /** F3 调试界面里显示的渲染器名 */
    const val VENDOR = "GoyDevv"

    const val UPSTREAM_URL = "https://github.com/GoyDevv/IronizedZink"

    /** 可选 OpenGL 版本（与上游一致的取值集合） */
    val GL_VERSIONS = listOf("4.6", "4.5", "4.3", "3.3")
}

/**
 * 渲染器唯一标识符。
 *
 * 沿用原「Kopper Zink」内置渲染器的 UUID：Ironized Zink 与它跑的是同一条
 * Zink + Kopper 渲染管线，沿用后老用户升级不会被重置回默认渲染器。
 *
 * 单独放在这里（而不是挂在 [IronizedZinkRenderer] 上）是为了避免
 * `AllSettings` 与渲染器对象之间的初始化成环。
 */
const val IRONIZED_ZINK_UNIQUE_IDENTIFIER = "0fa435e2-46df-45c9-906c-b29606aaef00"

/**
 * Ironized Zink 官方预设。
 *
 * 名称与 [IronizedZinkOptions] 的取值均取自上游 `Presets.kt`，未做改写；
 * 预设名保持上游原文（Potato / Performance / Default / Max Compatibility）不翻译，
 * 说明文案的本地化见 `strings.xml`。
 */
enum class ZinkPreset(
    /** 官方预设名，保持上游原文，不翻译 */
    val displayName: String,
    /** 该预设映射的环境变量组合 */
    val options: IronizedZinkOptions,
) {
    POTATO(
        displayName = "Potato",
        options = IronizedZinkOptions(
            glVersion = "4.6", threadedGl = true, bigCoreAffinity = true, outOfOrder = true,
            noError = false, vsync = true, relaxGlsl = false, allExtensions = true,
            shaderCache = true, singleFileCache = true, lazyDescriptors = true,
            inlineUniforms = false, forceSoftware = false,
        )
    ),
    PERFORMANCE(
        displayName = "Performance",
        options = IronizedZinkOptions(
            glVersion = "4.6", threadedGl = true, bigCoreAffinity = true, outOfOrder = true,
            noError = false, vsync = false, relaxGlsl = true, allExtensions = true,
            shaderCache = true, singleFileCache = true, lazyDescriptors = false,
            inlineUniforms = false, forceSoftware = false,
        )
    ),
    DEFAULT(
        displayName = "Default",
        options = IronizedZinkOptions(
            glVersion = "4.6", threadedGl = true, bigCoreAffinity = false, outOfOrder = false,
            noError = false, vsync = true, relaxGlsl = true, allExtensions = true,
            shaderCache = true, singleFileCache = true, lazyDescriptors = false,
            inlineUniforms = false, forceSoftware = false,
        )
    ),
    MAX_COMPAT(
        displayName = "Max Compatibility",
        options = IronizedZinkOptions(
            glVersion = "4.6", threadedGl = false, bigCoreAffinity = false, outOfOrder = false,
            noError = false, vsync = true, relaxGlsl = true, allExtensions = true,
            shaderCache = true, singleFileCache = false, lazyDescriptors = false,
            inlineUniforms = false, forceSoftware = false,
        )
    );

    companion object {
        val fallback: ZinkPreset = DEFAULT

        fun ofName(name: String?): ZinkPreset =
            entries.firstOrNull { it.name == name } ?: fallback
    }
}

/**
 * 全部可调参数（与上游 `RenderOptions` 一一对应，默认值也一致）。
 *
 * 安全提示（沿用上游 `Presets.kt` 的说明）：
 * Mesa 官方文档指出 `MESA_NO_ERROR` 会让非法的 API 调用进入「未定义行为」，
 * `allow_draw_out_of_order` 则允许驱动重排绘制顺序。两者叠加正是
 * 「手穿过 GUI 界面」深度排序问题的成因，因此预设中不再默认组合它们，
 * [noError] 只作为显式、带警告的选项提供给用户。
 */
data class IronizedZinkOptions(
    val glVersion: String = "4.6",
    // 性能
    val threadedGl: Boolean = true,          // mesa_glthread
    val bigCoreAffinity: Boolean = false,    // POJAV_BIG_CORE_AFFINITY
    val outOfOrder: Boolean = false,         // allow_draw_out_of_order
    val noError: Boolean = false,            // MESA_NO_ERROR（不安全，见上文）
    val vsync: Boolean = true,               // FORCE_VSYNC / 真实刷新率上限
    // 着色器与兼容性
    val relaxGlsl: Boolean = true,           // force_glsl_extensions_warn + allow_*
    val allExtensions: Boolean = true,       // MESA_EXTENSION_MAX_YEAR（关闭则限制年份）
    val shaderCache: Boolean = true,         // MESA_SHADER_CACHE_DISABLE（取反）
    val singleFileCache: Boolean = true,     // MESA_DISK_CACHE_SINGLE_FILE
    // 实验性
    val lazyDescriptors: Boolean = false,    // ZINK_DESCRIPTORS=lazy
    val inlineUniforms: Boolean = false,     // ZINK_INLINE_UNIFORMS
    val forceSoftware: Boolean = false,      // LIBGL_ALWAYS_SOFTWARE
) {
    /** 是否同时开启了「已知会出画面错乱」的 noError + outOfOrder 组合 */
    val hasUnsafeCombo: Boolean get() = noError && outOfOrder

    companion object {
        fun forPreset(preset: ZinkPreset): IronizedZinkOptions = preset.options

        /** 当前参数是否与某个预设完全一致 */
        fun matches(preset: ZinkPreset, options: IronizedZinkOptions): Boolean =
            forPreset(preset) == options

        /** 当前参数完全匹配的预设；没有则为 null（用户自定义） */
        fun matchingPreset(options: IronizedZinkOptions): ZinkPreset? =
            ZinkPreset.entries.firstOrNull { matches(it, options) }
    }
}

/**
 * 生成有序的环境变量表（只写不删）
 *
 * @param options 用户当前参数
 * @param shaderCacheDir 着色器缓存目录，`null` 时不设置
 */
fun buildIronizedZinkEnv(
    options: IronizedZinkOptions,
    shaderCacheDir: String? = PathManager.DIR_CACHE.absolutePath,
): LinkedHashMap<String, String> {
    val env = LinkedHashMap<String, String>()

    // --- 核心：Zink + Kopper ---
    env["POJAV_RENDERER"] = Zink.RENDERER_ID
    env["LIBGL_ES"] = "3"
    env["MESA_LOADER_DRIVER_OVERRIDE"] = "zink"
    env["MESA_GL_VERSION_OVERRIDE"] = options.glVersion
    env["MESA_GLSL_VERSION_OVERRIDE"] = glslFor(options.glVersion)
    shaderCacheDir?.let { env["MESA_GLSL_CACHE_DIR"] = it }

    // --- F3 界面显示的渲染器 / 厂商字符串（Mesa 从环境读取这些 driconf 项）---
    env["force_gl_renderer"] = "IronizedZink | OpenGL ${options.glVersion}"
    env["force_gl_vendor"] = Zink.VENDOR

    // --- 着色器与兼容性 ---
    val relax = if (options.relaxGlsl) "true" else "false"
    env["force_glsl_extensions_warn"] = relax
    env["allow_higher_compat_version"] = relax
    env["allow_glsl_extension_directive_midshader"] = relax
    if (!options.allExtensions) env["MESA_EXTENSION_MAX_YEAR"] = "2018"
    if (!options.shaderCache) env["MESA_SHADER_CACHE_DISABLE"] = "true"
    if (options.singleFileCache) env["MESA_DISK_CACHE_SINGLE_FILE"] = "1"

    // --- 性能 ---
    if (options.threadedGl) env["mesa_glthread"] = "true"
    if (options.bigCoreAffinity) env["POJAV_BIG_CORE_AFFINITY"] = "1"
    if (options.outOfOrder) env["allow_draw_out_of_order"] = "true"
    if (options.noError) env["MESA_NO_ERROR"] = "1"
    env["FORCE_VSYNC"] = if (options.vsync) "true" else "false"
    // vblank_mode 才是 Mesa/DRI 真正的垂直同步开关：
    // 1 = 跟随屏幕真实刷新率（60/90/120/144Hz），0 = 不限制
    env["vblank_mode"] = if (options.vsync) "1" else "0"

    // --- 实验性 ---
    if (options.lazyDescriptors) env["ZINK_DESCRIPTORS"] = "lazy"
    if (options.inlineUniforms) env["ZINK_INLINE_UNIFORMS"] = "1"
    if (options.forceSoftware) env["LIBGL_ALWAYS_SOFTWARE"] = "1"

    return env
}

/** GL 版本 → GLSL 版本（与上游一致） */
fun glslFor(glVersion: String): String = when (glVersion) {
    "4.6" -> "460"
    "4.5" -> "450"
    "4.3" -> "430"
    "3.3" -> "330"
    else -> "460"
}
