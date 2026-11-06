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
import com.movtery.zalithlauncher.utils.device.Architecture
import com.movtery.zalithlauncher.utils.platform.bytesToMB
import com.movtery.zalithlauncher.utils.platform.getTotalMemory
import com.movtery.zalithlauncher.utils.string.splitPreservingQuotes

private const val LWJGL_LIB_NAME_ARG = "-Dorg.lwjgl.opengl.libname="

/**
 * 初始化处理所有设置项
 * @param reloadAll 是否重新加载全部设置项
 */
fun loadAllSettings(context: Context, reloadAll: Boolean = false) {
    if (reloadAll) AllSettings.reloadAll()

    //AI Agent 的配置独立于启动器通用设置，但同样需要初始化
    if (reloadAll) com.movtery.zalithlauncher.ai.AISettings.reloadAll()

    //旧配置兼容：27.10.2 起「动态玻璃」整体移除，玻璃变成全局统一的基础材质，
    //旧的档位键与模糊类型键直接清掉。留着孤儿值会带来隐患：
    //以后若再出现同名、不同类型的设置，读取时会直接抛类型异常（启动崩溃）。
    removeLegacyGlassKeys()
    migrateTaskMenuCollapsed()

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
 * 把 27.8.0 之前「默认展开」的任务面板一次性改为收起。
 *
 * 只在没有迁移标记时执行一次；之后用户自己点开 / 收起都会被尊重。
 */
private fun migrateTaskMenuCollapsed() {
    val mmkv = launcherMMKV()
    if (mmkv.getBoolean(KEY_TASK_MENU_COLLAPSED_MIGRATED, false)) return
    AllSettings.launcherTaskMenuExpanded.save(false)
    mmkv.putBoolean(KEY_TASK_MENU_COLLAPSED_MIGRATED, true)
}

private const val KEY_TASK_MENU_COLLAPSED_MIGRATED = "taskMenuCollapsedMigrated"

/**
 * 清理已废弃的玻璃配置键
 *
 * - `glassLevel`：动态玻璃档位（Off / On）
 * - `backgroundBlurType`：背景模糊策略（Background / Foreground）
 *
 * 幂等：键不存在时删除是空操作，可以每次启动都跑。
 */
private fun removeLegacyGlassKeys() {
    val mmkv = launcherMMKV()
    mmkv.removeValueForKey(KEY_LEGACY_GLASS_LEVEL)
    mmkv.removeValueForKey(KEY_LEGACY_BACKGROUND_BLUR_TYPE)
}

private const val KEY_LEGACY_GLASS_LEVEL = "glassLevel"
private const val KEY_LEGACY_BACKGROUND_BLUR_TYPE = "backgroundBlurType"

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