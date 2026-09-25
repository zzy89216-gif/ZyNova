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

package com.movtery.zalithlauncher.setting

import android.content.Context
import com.movtery.zalithlauncher.setting.enums.GlassLevel
import com.movtery.zalithlauncher.utils.device.Architecture
import com.movtery.zalithlauncher.utils.platform.bytesToMB
import com.movtery.zalithlauncher.utils.platform.getTotalMemory
import com.movtery.zalithlauncher.utils.string.splitPreservingQuotes

private const val LWJGL_LIB_NAME_ARG = "-Dorg.lwjgl.opengl.libname="

/** 玻璃效果档位在配置中的键名，需要与 [AllSettings.glassLevel] 保持一致 */
private const val GLASS_LEVEL_KEY = "glassLevel"

/**
 * 初始化处理所有设置项
 * @param reloadAll 是否重新加载全部设置项
 */
fun loadAllSettings(context: Context, reloadAll: Boolean = false) {
    if (reloadAll) AllSettings.reloadAll()

    //旧配置兼容：把旧版「液态玻璃」布尔开关迁移为新的玻璃效果
    if (AllSettings.liquidGlass.getValue()) {
        AllSettings.glassLevel.save(GlassLevel.On)
        AllSettings.liquidGlass.save(false)
    }

    //旧配置兼容：26.2.2 起玻璃效果只保留「关闭 / 启用动态玻璃」两档，
    //升级前保存的 Standard / Enhanced / Extreme 统一迁移为「启用动态玻璃」。
    //必须直接读取原始字符串：旧档位名称已经不在枚举里，
    //经过 [AllSettings.glassLevel] 读取会直接回退成默认值，从而丢失用户原本的选择。
    migrateLegacyGlassLevel()

    if (AllSettings.ramAllocation.getValue() == null) {
        val ram = findBestRAMAllocation(context)
        AllSettings.ramAllocation.save(ram)
    }
    val jvmArgs = AllSettings.jvmArgs.getValue()
    jvmArgs.splitPreservingQuotes().find { it.startsWith(LWJGL_LIB_NAME_ARG) }?.let { arg ->
        AllSettings.jvmArgs.save(jvmArgs.replace(arg, ""))
    }
}

/**
 * 把旧版玻璃效果档位迁移为当前的「关闭 / 启用动态玻璃」
 *
 * 幂等：当前档位名称（Off / On）会被解析为自身，重复执行不会改变配置。
 */
private fun migrateLegacyGlassLevel() {
    val raw = launcherMMKV().getString(GLASS_LEVEL_KEY, null) ?: return
    val migrated = GlassLevel.fromLegacyName(raw) ?: return
    if (migrated.name != raw) {
        AllSettings.glassLevel.save(migrated)
    }
}

/**
 * This functions aims at finding the best default RAM amount,
 * according to the RAM amount of the physical device.
 * Put not enough RAM ? Minecraft will lag and crash.
 * Put too much RAM ?
 * The GC will lag, android won't be able to breathe properly.
 * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/5de6822/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/prefs/LauncherPreferences.java#L142-L154)
 * @param context Context needed to get the total memory of the device.
 * @return The best default value found.
 */
fun findBestRAMAllocation(context: Context): Int {
    if (Architecture.is32BitsDevice) return 696

    val deviceRam = getTotalMemory(context).bytesToMB()
    return when {
        deviceRam < 1024 -> 296
        deviceRam < 1536 -> 448
        deviceRam < 2048 -> 656
        deviceRam < 3064 -> 936
        deviceRam < 4096 -> 1144
        deviceRam < 6144 -> 1536
        else -> 2048 //Default RAM allocation for 64 bits
    }
}