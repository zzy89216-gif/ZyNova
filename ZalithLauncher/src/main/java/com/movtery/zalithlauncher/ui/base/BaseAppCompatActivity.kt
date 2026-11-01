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

package com.movtery.zalithlauncher.ui.base

import android.app.ActivityManager
import android.content.res.Configuration
import android.graphics.ColorDrawable
import android.os.Bundle
import androidx.annotation.CallSuper
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.context.refreshContext
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.setting.enums.DarkMode
import com.movtery.zalithlauncher.setting.loadAllSettings
import com.movtery.zalithlauncher.utils.checkStoragePermissionsForInit
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class BaseAppCompatActivity : FullScreenAppCompatActivity() {
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applyWindowBackground()
        refreshContext(this)
        checkStoragePermissions()
    }

    /**
     * 议题 #38：窗口底色与「启动器深色设置」对齐。
     *
     * 主题只按系统分 values / values-night，用户选「强制深色」时 Activity 用的仍是浅色主题，
     * 首帧之前窗口会先刷一层白底（视频背景用户看到的「白屏闪一下」就是这个）。
     * 这里只在「设置里的深色状态」与「系统主题」不一致时补一层纯色窗口背景，
     * 一致时沿用主题自带的 `?android:attr/colorBackground`，不改变原有观感。
     */
    private fun applyWindowBackground() {
        val systemDark =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val launcherDark = when (AllSettings.launcherDarkMode.getValue()) {
            DarkMode.Enable -> true
            DarkMode.Disable -> false
            DarkMode.FollowSystem -> systemDark
        }
        if (launcherDark == systemDark) return
        window.setBackgroundDrawable(
            ColorDrawable(if (launcherDark) 0xFF141218.toInt() else 0xFFFFFBFE.toInt())
        )
    }

    @CallSuper
    override fun onResume() {
        super.onResume()
        loadAllSettings(this, true)
        checkStoragePermissions()
        refreshTaskDescription()
    }

    /**
     * @return 该 Activity 在最近任务中显示的标题
     */
    protected open fun getTaskDescriptionTitle(): String? = null

    @Suppress("DEPRECATION")
    private fun refreshTaskDescription() {
        val title = getTaskDescriptionTitle() ?: BuildKeys.LAUNCHER_NAME
        setTaskDescription(ActivityManager.TaskDescription(title))
    }

    protected fun refreshData() {
        AccountsManager.reloadAccounts()
        AccountsManager.reloadAuthServers()
        GamePathManager.reloadPath()
    }

    private fun checkStoragePermissions() {
        //检查所有文件管理权限
        checkStoragePermissionsForInit(this)
    }

    protected fun runFinish() = run { finish() }
}