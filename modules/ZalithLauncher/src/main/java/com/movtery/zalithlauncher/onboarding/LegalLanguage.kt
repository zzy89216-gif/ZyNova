/*
 * ZyNova Launcher — 条款语言
 * GPL-3.0-or-later
 */
package com.movtery.zalithlauncher.onboarding

import java.util.Locale

/**
 * 条款文本支持的语言。
 *
 * 与介绍页/仓库里那 24 种语言**一一对应**（`i18n/README.md` 的索引）。
 * 选哪一种是「系统语言」决定的，不是地区决定的：
 * 一个在美国用日语的用户，看到的是**日文正文 + 美国地区条款**。
 */
enum class LegalLanguage(val tag: String, val label: String, val rtl: Boolean = false) {
    EN("en", "English"),
    ZH_CN("zh-CN", "简体中文"),
    ZH_TW("zh-TW", "繁體中文"),
    JA("ja", "日本語"),
    KO("ko", "한국어"),
    TR("tr", "Türkçe"),
    RU("ru", "Русский"),
    VI("vi", "Tiếng Việt"),
    TH("th", "ไทย"),
    DE("de", "Deutsch"),
    ES("es", "Español"),
    FR("fr", "Français"),
    UK("uk", "Українська"),
    PL("pl", "Polski"),
    PT_BR("pt-BR", "Português (Brasil)"),
    AR("ar", "العربية", rtl = true),
    FA("fa", "فارسی", rtl = true),
    ID("id", "Bahasa Indonesia"),
    IT("it", "Italiano"),
    EL("el", "Ελληνικά"),
    ML("ml", "മലയാളം"),
    UR_LATN("ur-Latn", "Roman Urdu"),
    RO("ro", "Română"),
    BG("bg", "Български");

    companion object {
        private val BY_LANGUAGE: Map<String, LegalLanguage> = entries.associateBy { it.tag.substringBefore('-') }

        fun of(locale: Locale = Locale.getDefault()): LegalLanguage {
            val lang = locale.language.lowercase(Locale.ROOT)
            val country = locale.country.uppercase(Locale.ROOT)
            val script = locale.script
            return when (lang) {
                // 中文：繁体优先看文字，其次看地区
                "zh" -> if (script == "Hant" || country in setOf("TW", "HK", "MO")) ZH_TW else ZH_CN
                // 印尼语的历史代码是 in
                "in", "id" -> ID
                // 葡萄牙语只提供巴西版（欧洲葡语用户回退到 pt-BR，差异可读）
                "pt" -> PT_BR
                // 乌尔都语只提供拉丁转写版
                "ur" -> UR_LATN
                else -> BY_LANGUAGE[lang] ?: EN
            }
        }
    }
}
