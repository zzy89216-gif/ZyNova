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
 * 应用一个官方预设：把该预设的参数写回设置。
 *
 * ⚠️ 议题 #13（老设备无法降级 OpenGL 版本）：**预设不写 [AllSettings.ironizedZinkGlVersion]**。
 *
 * 4 个官方预设的 `glVersion` 都是 4.6。旧实现「选预设 = 写入整组参数」，于是用户先在
 * 下拉里把 OpenGL 版本降到 3.3 / 4.5，只要再点一次预设就会被重新固定回 4.6，
 * 在不完整支持 Vulkan→Zink 4.6 转译的老 GPU 上直接黑屏/进不去，而且用户看不到原因。
 *
 * 现在的设计：
 * - **OpenGL 版本是独立设置**，只由用户在「OpenGL 版本」下拉里改；
 * - 预设只写其它 12 个参数，**绝不动 GL 版本**；
 * - 预设命中的判定也忽略 GL 版本（见 `IronizedZinkOptions.matchesIgnoringGlVersion`），
 *   所以「Default + 3.3」仍显示为 Default，UI 摘要里会说明两者各自生效。
 */
fun AllSettings.applyIronizedZinkPreset(preset: ZinkPreset) {
    val options = IronizedZinkOptions.forPreset(preset)
    ironizedZinkPreset.save(preset)
    //刻意不写 ironizedZinkGlVersion：GL 版本与预设解耦（议题 #13）
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
