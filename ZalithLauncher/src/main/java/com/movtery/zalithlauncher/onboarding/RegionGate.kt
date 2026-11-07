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
     * 放行的时区前缀。俄罗斯跨 11 个时区，按常见城市 ID 与 Europe/Moscow 兼容处理。
     */
    private val ALLOWED_TIMEZONE_PREFIXES = listOf(
        "Asia/Shanghai", "Asia/Chongqing", "Asia/Harbin", "Asia/Urumqi", "PRC",
        "Asia/Tokyo", "Japan",
        "Asia/Seoul", "ROK",
        "America/", "US/", "Pacific/Honolulu", "Pacific/Guam",
        "Europe/Moscow", "Europe/Kaliningrad", "Europe/Samara", "Europe/Volgograd",
        "Asia/Yekaterinburg", "Asia/Novosibirsk", "Asia/Krasnoyarsk", "Asia/Irkutsk",
        "Asia/Yakutsk", "Asia/Vladivostok", "Asia/Magadan", "Asia/Kamchatka"
    )

    private fun country(): String = Locale.getDefault().country.uppercase(Locale.US)

    private fun timeZoneId(): String = TimeZone.getDefault().id

    fun isAllowed(): Boolean {
        if (country() in ALLOWED_COUNTRIES) return true
        val tz = timeZoneId()
        return ALLOWED_TIMEZONE_PREFIXES.any { tz.startsWith(it) }
    }

    /** 给用户看的地区描述（用于阻断页与日志） */
    fun describe(): String {
        val c = country().ifBlank { "??" }
        return "$c / ${timeZoneId()} / ${Locale.getDefault().toLanguageTag()}"
    }

    /** 系统语言 + 时区签名：变化时要求重新同意条款 */
    fun signature(): String = "${Locale.getDefault().toLanguageTag()}|${TimeZone.getDefault().id}"
}
