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

import com.movtery.zalithlauncher.setting.AllSettings

/**
 * 从启动器设置里读取 Ironized Zink 当前的全部参数。
 *
 * 上游 Ironized Zink 把参数存在自己的 SharedPreferences 并写出 `/sdcard/...env`；
 * 内置到启动器后直接复用启动器的设置存储，不再需要任何外部文件或存储权限。
 */
val AllSettings.ironizedZinkOptions: IronizedZinkOptions
    get() = IronizedZinkOptions(
        glVersion = ironizedZinkGlVersion.state,
        threadedGl = ironizedZinkThreadedGl.state,
        bigCoreAffinity = ironizedZinkBigCoreAffinity.state,
        outOfOrder = ironizedZinkOutOfOrder.state,
        noError = ironizedZinkNoError.state,
        vsync = ironizedZinkVsync.state,
        relaxGlsl = ironizedZinkRelaxGlsl.state,
        allExtensions = ironizedZinkAllExtensions.state,
        shaderCache = ironizedZinkShaderCache.state,
        singleFileCache = ironizedZinkSingleFileCache.state,
        lazyDescriptors = ironizedZinkLazyDescriptors.state,
        inlineUniforms = ironizedZinkInlineUniforms.state,
        forceSoftware = ironizedZinkForceSoftware.state,
    )

/**
 * 应用一个官方预设：把该预设的全部参数写回设置
 *
 * 与上游行为一致——预设只是「一次性写入整组参数」的快捷方式，
 * 写入后用户仍然可以逐项修改任何一个参数。
 */
fun AllSettings.applyIronizedZinkPreset(preset: ZinkPreset) {
    val options = IronizedZinkOptions.forPreset(preset)
    ironizedZinkPreset.save(preset)
    ironizedZinkGlVersion.save(options.glVersion)
    ironizedZinkThreadedGl.save(options.threadedGl)
    ironizedZinkBigCoreAffinity.save(options.bigCoreAffinity)
    ironizedZinkOutOfOrder.save(options.outOfOrder)
    ironizedZinkNoError.save(options.noError)
    ironizedZinkVsync.save(options.vsync)
    ironizedZinkRelaxGlsl.save(options.relaxGlsl)
    ironizedZinkAllExtensions.save(options.allExtensions)
    ironizedZinkShaderCache.save(options.shaderCache)
    ironizedZinkSingleFileCache.save(options.singleFileCache)
    ironizedZinkLazyDescriptors.save(options.lazyDescriptors)
    ironizedZinkInlineUniforms.save(options.inlineUniforms)
    ironizedZinkForceSoftware.save(options.forceSoftware)
}
