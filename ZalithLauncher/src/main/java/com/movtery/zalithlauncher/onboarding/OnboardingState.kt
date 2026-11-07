/*
 * ZyNova Launcher — 首次启动引导状态
 * GPL-3.0-or-later
 */
package com.movtery.zalithlauncher.onboarding

import com.movtery.zalithlauncher.setting.AllSettings

/**
 * 首次启动引导（EULA / 隐私政策 / 第三方声明 / 教程 / 年龄确认）状态。
 *
 * 需要重新同意的条件（任一）：
 * 1. 从未同意过；
 * 2. **法律法规文本版本变化**（[CURRENT_LEGAL_VERSION]）；
 * 3. **系统语言或时区变化**（用户换环境后必须重新确认）。
 */
object OnboardingState {

    /** 条款版本：四份文档实质变更时必须改这个值 */
    const val CURRENT_LEGAL_VERSION = "28.1.0-2026"

    fun needsOnboarding(): Boolean {
        if (!AllSettings.onboardingAccepted.getValue()) return true
        if (!AllSettings.onboardingAgeConfirmed.getValue()) return true
        if (AllSettings.onboardingLegalVersion.getValue() != CURRENT_LEGAL_VERSION) return true
        if (AllSettings.onboardingLocaleSignature.getValue() != RegionGate.signature()) return true
        return false
    }

    fun needsTimeLimit(): Boolean = AllSettings.onboardingIsMinor.getValue()

    /** 用户点击进入时调用：把当前版本与语言/时区签名落盘 */
    fun recordAcceptance(isMinor: Boolean) {
        AllSettings.onboardingAccepted.save(true)
        AllSettings.onboardingIsMinor.save(isMinor)
        AllSettings.onboardingAgeConfirmed.save(true)
        AllSettings.onboardingLegalVersion.save(CURRENT_LEGAL_VERSION)
        AllSettings.onboardingLocaleSignature.save(RegionGate.signature())
    }
}
