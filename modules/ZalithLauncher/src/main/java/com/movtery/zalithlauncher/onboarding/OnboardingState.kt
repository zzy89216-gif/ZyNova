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
 * 2. **条款内容版本变化**（[CURRENT_LEGAL_VERSION]，只有法律文本实质变更时才提升）；
 * 3. 从未确认过年龄段。
 *
 * ⚠️ 与 28.1.0 的行为差异（维护者 2026-11 明确要求）：
 * - 以前 `CURRENT_LEGAL_VERSION` 直接取 `BuildConfig.VERSION_NAME`，**每次发版**都强制所有用户
 *   重读三份条款。用户反馈这是纯打扰：应用升级 ≠ 政策更新。
 * - 现在条款版本**独立于应用版本**，只在法律文本真的改了才手动提升；
 * - 系统语言 / 时区变化**不再**触发重新同意（只记录签名，用于选择哪一种语言/地区的文本）。
 */
object OnboardingState {

    /**
     * 当前生效的条款版本。
     *
     * 规则：
     * - **不要**改回 `BuildConfig.VERSION_NAME`；
     * - 只有 EULA / 隐私政策 / 第三方声明 / 地区条款发生**实质变更**时才提升这里；
     * - 提升后所有用户会被强制重新阅读并同意三份文档（这是预期行为）。
     *
     * `28.1.0` 是 2026 年 11 月那版条款的版本号，保持不动即可让老用户升级后**不再重读**。
     */
    const val CURRENT_LEGAL_VERSION: String = "28.1.0"

    fun needsOnboarding(): Boolean {
        if (!AllSettings.onboardingAccepted.getValue()) return true
        if (!AllSettings.onboardingAgeConfirmed.getValue()) return true
        if (AllSettings.onboardingLegalVersion.getValue() != CURRENT_LEGAL_VERSION) return true
        return false
    }

    fun needsTimeLimit(): Boolean = AllSettings.onboardingIsMinor.getValue()

    /** 用户点击进入时调用：把当前条款版本与语言/时区签名落盘 */
    fun recordAcceptance(isMinor: Boolean) {
        AllSettings.onboardingAccepted.save(true)
        AllSettings.onboardingIsMinor.save(isMinor)
        AllSettings.onboardingAgeConfirmed.save(true)
        AllSettings.onboardingLegalVersion.save(CURRENT_LEGAL_VERSION)
        // 只做记录：用于下次展示时选对应语言/地区的文本，不再作为「重新同意」的触发条件
        AllSettings.onboardingLocaleSignature.save(RegionGate.signature())
    }
}
