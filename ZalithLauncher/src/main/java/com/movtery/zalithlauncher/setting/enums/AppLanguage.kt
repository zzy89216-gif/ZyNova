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

package com.movtery.zalithlauncher.setting.enums

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.movtery.zalithlauncher.R

enum class AppLanguage(
    val tag: String,
    @param:StringRes val textRes: Int,
    /**
     * 是否在设置的语言列表里显示。
     *
     * 维护者要求：**翻译没做完的语言先不要露出来**，否则用户切过去看到一半英文、一半机翻，
     * 比不给选更糟。判定口径：该 locale 的 `strings.xml` 覆盖率达到 85% 且占位符零错位，
     * 才把这里改成 `true`（覆盖率用仓库里那段 python 脚本量）。
     */
    val selectable: Boolean = true
) {
    //按语言代码字母顺序排序
    FOLLOW_SYSTEM("", R.string.generic_follow_system),
    ARABIC("ar", R.string.language_arabic, selectable = false),
    BULGARIAN("bg", R.string.language_bulgarian, selectable = false),
    ENGLISH("en", R.string.language_english),
    SPANISH("es", R.string.language_spanish, selectable = false),
    PERSIAN("fa", R.string.language_persian, selectable = false),
    FILIPINO("fil", R.string.language_filipino, selectable = false),
    FRENCH("fr", R.string.language_french, selectable = false),
    GREEK("el", R.string.language_greek, selectable = false),
    INDONESIAN("id", R.string.language_indonesian),
    ITALIAN("it", R.string.language_italian, selectable = false),
    JAPANESE("ja", R.string.language_japanese),
    KOREAN("ko", R.string.language_korean),
    MALAYALAM("ml", R.string.language_malayalam, selectable = false),
    GERMAN("de", R.string.language_german, selectable = false),
    POLISH("pl", R.string.language_polish, selectable = false),
    PORTUGUESE("pt", R.string.language_portuguese),
    BRAZILIAN_PORTUGUESE("pt-BR", R.string.language_brazilian_portuguese),
    ROMANIAN("ro", R.string.language_romanian, selectable = false),
    RUSSIAN("ru", R.string.language_russian),
    THAI("th", R.string.language_thai, selectable = false),
    TURKISH("tr", R.string.language_turkish),
    UYGHUR("ug", R.string.language_uyghur, selectable = false),
    UKRAINIAN("uk", R.string.language_ukrainian, selectable = false),
    URDU_ROMAN("ur", R.string.language_urdu_roman, selectable = false),
    VIETNAMESE("vi", R.string.language_vietnamese),
    SIMPLIFIED_CHINESE("zh-CN", R.string.language_simplified_chinese),
    TRADITIONAL_CHINESE("zh-TW", R.string.language_traditional_chinese),
}

fun applyLanguage(language: AppLanguage) {
    val appLocale = if (language != AppLanguage.FOLLOW_SYSTEM) {
        LocaleListCompat.forLanguageTags(language.tag)
    } else {
        LocaleListCompat.getEmptyLocaleList()
    }
    AppCompatDelegate.setApplicationLocales(appLocale)
}
