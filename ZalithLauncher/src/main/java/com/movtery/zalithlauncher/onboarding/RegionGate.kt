/*
 * ZyNova Launcher — 地区可用性判定
 * GPL-3.0-or-later
 *
 * 开放范围 = [LegalRegions] 里那 58 个国家/地区（与「已提供语言」对齐）。
 * 判定依据（两者任一命中即放行）：
 *   1. 系统国家/地区代码（Locale country）
 *   2. 系统时区 → 反查国家（用系统时区库，不写死大前缀）
 * 说明：这是**尽力而为**的判定，可被用户改时区绕过；真正的地区限制应同时依赖商店分发布局。
 * 这里的目的只是「不给未覆盖地区的用户误用」，不追求对抗性。
 */
package com.movtery.zalithlauncher.onboarding

import java.util.Locale
import java.util.TimeZone

object RegionGate {

    private fun country(): String = Locale.getDefault().country.uppercase(Locale.ROOT)

    private fun timeZoneId(): String = TimeZone.getDefault().id

    /**
     * 当前设备命中的开放地区；不在开放范围内返回 null。
     *
     * ⚠️ 以前这里维护了一张手写时区白名单，只有中美韩日俄。
     * 开放范围扩到 58 个地区后，手写时区表既容易漏（德国只有 Europe/Berlin，
     * 忘了 Europe/Busingen 就会误拦），也没法维护。
     * 现在改为「时区 → 国家」反查，判断逻辑收敛到一个地方。
     */
    fun region(): LegalRegion? {
        LegalRegions.byCountry(country())?.let { return it }
        return LegalRegions.byCountry(countryOfTimeZone(timeZoneId()))
    }

    fun isAllowed(): Boolean = region() != null

    /**
     * 用系统时区库反查时区所属国家（ICU）。
     * 失败、未知时区、或返回 `001`（未知地区）时返回 null，交给调用方兜底。
     */
    private fun countryOfTimeZone(zoneId: String): String? = runCatching {
        val code: String = android.icu.util.TimeZone.getRegion(zoneId) ?: return null
        if (code.isBlank() || code == "001") null else code.uppercase(Locale.ROOT)
    }.getOrNull()

    /** 给用户看的地区描述（用于阻断页与日志） */
    fun describe(): String {
        val c = country().ifBlank { "??" }
        return "$c / ${timeZoneId()} / ${Locale.getDefault().toLanguageTag()}"
    }

    /** 系统语言 + 时区签名：只做记录，用于下次展示时选文本，不再是「重新同意」的触发条件 */
    fun signature(): String = "${Locale.getDefault().toLanguageTag()}|${TimeZone.getDefault().id}"
}
