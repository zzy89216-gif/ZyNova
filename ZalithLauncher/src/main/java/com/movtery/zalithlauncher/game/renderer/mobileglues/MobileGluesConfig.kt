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

package com.movtery.zalithlauncher.game.renderer.mobileglues

import com.google.gson.JsonObject
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.utils.GSON
import com.movtery.zalithlauncher.utils.file.ensureDirectorySilently
import com.movtery.zalithlauncher.utils.logging.Logger
import java.io.File

private const val TAG = "MobileGluesConfig"

/**
 * MobileGlues 的 OpenGL 版本配置。
 *
 * ## 为什么必须落到文件
 *
 * MobileGlues **不读取 `MESA_GL_VERSION_OVERRIDE`**（那是 Mesa / Zink 用的环境变量）——
 * 它的 OpenGL 版本取自 native 端 `config/settings.cpp` 的：
 *
 * ```c
 * int customGLVersionInt = config_get_int("customGLVersion");
 * ```
 *
 * 而 `config.json` 的位置由 `config/config.cpp` 的 `check_path()` 决定：
 * `MG_DIR_PATH` 环境变量存在就用它，否则退回默认的 `/sdcard/MG`。
 *
 * 所以「让用户选 MobileGlues 的 OpenGL 版本」只有一条路：
 * **启动游戏前把 `customGLVersion` 写进一份 config.json，并把目录注入给游戏进程。**
 *
 * ## 为什么不直接写 /sdcard/MG
 *
 * 那个目录是官方 MobileGlues 插件的配置目录。用户如果在别的启动器里用官方插件
 * 调过参数，启动器再去改写它，就等于替用户改了另一个应用的设置。
 * 因此这里用启动器自己的目录（外部私有目录下的 `mobileglues/`）并通过
 * [ENV_DIR_PATH] 注入，与项目里 Ironized Zink「不再依赖 /sdcard 配置文件」
 * 的做法保持同一个方向。
 */
object MobileGluesConfig {
    /** 注入给游戏进程的目录环境变量（MobileGlues native 端读取这个键） */
    const val ENV_DIR_PATH = "MG_DIR_PATH"

    /** native 端读取的 OpenGL 版本键名（整数，如 46 = 4.6） */
    private const val KEY_GL_VERSION = "customGLVersion"

    /**
     * MobileGlues 目录。
     *
     * 用 `by lazy` 是因为 [PathManager] 的字段要等 `PathManager.init()` 之后才可用，
     * 而渲染器对象可能在那之前就被引用。
     */
    val directory: File by lazy { File(PathManager.DIR_FILES_EXTERNAL, "mobileglues") }

    /** `config.json` 的路径 */
    val configFile: File get() = File(directory, "config.json")

    /**
     * 把用户选择的 OpenGL 版本写进 `config.json`。
     *
     * - 文件已存在 → **只覆盖 `customGLVersion` 一个键**，其余键（含启动器不认识的键）
     *   原样保留。这与上游插件 `MGConfigCodec.encode()` 的做法一致：
     *   不认识的键要写回去，否则每保存一次就等于在删别人的设置。
     * - 文件不存在 → 先写一份与上游插件 `MGConfig.Default` 等价的完整配置。
     *   **不能只写一个键**：native 对缺失的键是「回落到自己的硬编码默认值」，
     *   而它与插件默认值并不相同（例如缺 `enableANGLE` 会被解析成 `DisableIfPossible`，
     *   而插件默认是 `EnableIfPossible`），只写一个键会悄悄改掉别的行为。
     *
     * @return 是否写入成功。失败只记日志，**不会**阻塞游戏启动
     */
    fun writeGlVersion(glVersion: MobileGluesGlVersion): Boolean = runCatching {
        if (!directory.ensureDirectorySilently()) {
            Logger.warning(TAG, "Cannot create MobileGlues directory: ${directory.absolutePath}")
            return false
        }

        val root = readConfig() ?: JsonObject()
        root.addProperty(KEY_GL_VERSION, glVersion.wire)

        val target = configFile
        //先写临时文件再改名：中途失败也不会留下半截 JSON 让游戏读到损坏的配置
        val temp = File(directory, "${target.name}.tmp")
        temp.writeText(GSON.toJson(root), Charsets.UTF_8)
        target.delete()
        if (!temp.renameTo(target)) {
            //改名失败时退回直接写，至少保证内容是对的
            target.writeText(GSON.toJson(root), Charsets.UTF_8)
            temp.delete()
        }
        true
    }.getOrElse { e ->
        Logger.error(TAG, "Failed to write MobileGlues config", e)
        false
    }

    /**
     * 读取现有 `config.json` 并补齐缺失的键。
     *
     * 解析失败（损坏 / 不是 JSON 对象）时返回 `null`，由调用方重建一份默认配置 ——
     * 宁可恢复默认，也不要让游戏读到坏文件。
     */
    private fun readConfig(): JsonObject? {
        val file = configFile
        if (!file.isFile) return JsonObject()

        val parsed = runCatching {
            GSON.fromJson(file.readText(Charsets.UTF_8), JsonObject::class.java)
        }.getOrNull()

        if (parsed == null) {
            Logger.warning(TAG, "Broken MobileGlues config, rebuilding with defaults: ${file.absolutePath}")
            return null
        }

        //与上游插件一致：缺键时按「插件默认值」补齐，
        //而不是留给 native 回落到它自己的硬编码默认值（两者并不相同）
        DEFAULTS.forEach { (key, value) ->
            if (!parsed.has(key)) parsed.addProperty(key, value)
        }
        return parsed
    }

    /**
     * 与上游插件 `MGConfig.Default` 等价的默认值。
     *
     * 刻意用整数写布尔，与上游 `MGConfigCodec.encode()` 的产物一致
     * （native 一律按 `> 0` 判断布尔）。
     */
    private val DEFAULTS: Map<String, Int> = linkedMapOf(
        // enableANGLE：EnableIfPossible
        "enableANGLE" to 1,
        // enableNoError：Auto
        "enableNoError" to 0,
        // enableExtTimerQuery：启用
        "enableExtTimerQuery" to 1,
        // enableExtComputeShader：关闭
        "enableExtComputeShader" to 0,
        // enableExtDirectStateAccess：关闭
        "enableExtDirectStateAccess" to 0,
        // maxGlslCacheSize：32 MiB
        "maxGlslCacheSize" to 32,
        // angleDepthClearFixMode：关闭
        "angleDepthClearFixMode" to 0,
        // customGLVersion：默认（native DEFAULT_GL_VERSION = 40）
        KEY_GL_VERSION to MobileGluesGlVersion.default.wire,
        // fsr1Setting：关闭
        "fsr1Setting" to 0,
    )
}
