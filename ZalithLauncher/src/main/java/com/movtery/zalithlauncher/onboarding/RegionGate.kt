/*
 * ZyNova Launcher — 地区可用性判定
 * GPL-3.0-or-later
 *
 * 仅放行：中国、美国、韩国、日本、俄罗斯。
 * 判定依据（三者任一明确落在白名单即放行，全部不匹配才拦截）：
 *   1. 系统时区（TimeZone ID）
 *   2. 系统国家/地区（Locale country）
 * 说明：这是**尽力而为**的判定，可被用户改时区绕过；真正的地区限制应同时依赖商店分发布局。
 * 这里的目的只是「不给未覆盖地区的用户误用」，不追求对抗性。
 */
package com.movtery.zalithlauncher.onboarding

import java.util.Locale
import java.util.TimeZone

object RegionGate {

    /** 放行的国家/地区代码 */
    private val ALLOWED_COUNTRIES = setOf("CN", "US", "KR", "JP", "RU")

    /**
     * 放行的时区（**逐个列出**，不要用 `America/` 这种大前缀）。
     *
     * ⚠️ 之前的写法用了 `America/`，结果加拿大、墨西哥、巴西、阿根廷的用户全都被放行，
     * 与「只开中美韩日俄」的要求不符。现在只保留美国本土 + 夏威夷 + 阿拉斯加，
     * 以及中/韩/日/俄的具体时区。
     */
    private val ALLOWED_TIMEZONES = setOf(
        // 中国
        "Asia/Shanghai", "Asia/Chongqing", "Asia/Harbin", "Asia/Urumqi", "PRC",
        // 日本
        "Asia/Tokyo", "Japan",
        // 韩国
        "Asia/Seoul", "ROK",
        // 美国（本土 + 阿拉斯加 + 夏威夷 + 关岛）
        "America/New_York", "America/Detroit", "America/Chicago",
        "America/Denver", "America/Phoenix", "America/Los_Angeles",
        "America/Anchorage", "America/Indiana/Indianapolis",
        "America/Kentucky/Louisville", "America/Boise",
        "America/Juneau", "America/Sitka", "America/Nome", "America/Adak",
        "US/Eastern", "US/Central", "US/Mountain", "US/Pacific",
        "US/Alaska", "US/Hawaii", "Pacific/Honolulu", "Pacific/Guam",
        // 俄罗斯
        "Europe/Moscow", "Europe/Kaliningrad", "Europe/Samara", "Europe/Volgograd",
        "Europe/Kirov", "Europe/Astrakhan", "Europe/Saratov", "Europe/Ulyanovsk",
        "Asia/Yekaterinburg", "Asia/Omsk", "Asia/Novosibirsk", "Asia/Barnaul",
        "Asia/Tomsk", "Asia/Novokuznetsk", "Asia/Krasnoyarsk", "Asia/Irkutsk",
        "Asia/Chita", "Asia/Yakutsk", "Asia/Khandyga", "Asia/Vladivostok",
        "Asia/Ust-Nera", "Asia/Magadan", "Asia/Sakhalin", "Asia/Srednekolymsk",
        "Asia/Kamchatka", "Asia/Anadyr"
    )

    private fun country(): String = Locale.getDefault().country.uppercase(Locale.US)

    private fun timeZoneId(): String = TimeZone.getDefault().id

    fun isAllowed(): Boolean {
        if (country() in ALLOWED_COUNTRIES) return true
        val tz = timeZoneId()
        //先精确匹配；再容忍少数系统给出的 `Area/City/…` 变体（前缀 + 斜杠边界）
        if (tz in ALLOWED_TIMEZONES) return true
        return ALLOWED_TIMEZONES.any { allowed -> tz.startsWith("$allowed/") }
    }

    /** 给用户看的地区描述（用于阻断页与日志） */
    fun describe(): String {
        val c = country().ifBlank { "??" }
        return "$c / ${timeZoneId()} / ${Locale.getDefault().toLanguageTag()}"
    }

    /** 系统语言 + 时区签名：变化时要求重新同意条款 */
    fun signature(): String = "${Locale.getDefault().toLanguageTag()}|${TimeZone.getDefault().id}"
}
