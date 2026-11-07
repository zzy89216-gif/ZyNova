/*
 * ZyNova Launcher — 首次启动引导状态
 * GPL-3.0-or-later
 */
package com.movtery.zalithlauncher.onboarding

import com.movtery.zalithlauncher.setting.AllSettings

/** 首次启动引导（EULA / 隐私政策 / 第三方声明 / 教程 / 年龄确认）状态 */
object OnboardingState {
    fun needsOnboarding(): Boolean =
        !AllSettings.onboardingAccepted.getValue() || !AllSettings.onboardingAgeConfirmed.getValue()

    fun needsTimeLimit(): Boolean = AllSettings.onboardingIsMinor.getValue()
}
