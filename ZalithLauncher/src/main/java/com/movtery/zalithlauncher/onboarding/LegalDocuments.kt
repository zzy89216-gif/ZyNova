/*
 * ZyNova Launcher — 三份声明的正文与「本地区补充条款」
 * GPL-3.0-or-later
 *
 * 组成方式：
 *   某一语言的正文本  +  某一国家/地区的补充条款
 * 所以「同一份声明，中国用户和美国用户看到的不一样」，
 * 而「同一个地区，用日语系统和用英语系统看到的正文语言不一样」。
 *
 * 新增语言时：
 *   1. 在 LegalBodies*.kt 里补三份正文与三份地区补充条款；
 *   2. 在下面的 body()/titleOf()/jurisText() 里加一个分支；
 *   3. 没有对应文本的语言会回退英文（不会崩，也不会是空白）。
 */
package com.movtery.zalithlauncher.onboarding

import java.util.Locale

enum class LegalDocId { TERMS, LICENSES, GUIDE }

data class LegalDoc(val title: String, val body: String)

object LegalDocuments {

    fun build(id: LegalDocId, region: LegalRegion, lang: LegalLanguage): LegalDoc =
        LegalDoc(titleOf(id, lang), body(id, lang).trimEnd() + "\n\n" + jurisdiction(id, region, lang))

    // ─────────────────────────────────────────────────────────────
    //  标题
    // ─────────────────────────────────────────────────────────────

    private fun titleOf(id: LegalDocId, lang: LegalLanguage): String = when (id) {
        LegalDocId.TERMS -> when (lang) {
            LegalLanguage.ZH_CN -> "《最终用户许可协议 + 隐私政策》"
            LegalLanguage.ZH_TW -> "《最終用戶授權協議 + 隱私政策》"
            LegalLanguage.JA -> "《エンドユーザーライセンス契約 + プライバシーポリシー》"
            LegalLanguage.KO -> "《최종 사용자 라이선스 계약 + 개인정보 처리방침》"
            LegalLanguage.RU -> "《Лицензионное соглашение + Политика конфиденциальности》"
            LegalLanguage.DE -> "《Endnutzer-Lizenzvereinbarung + Datenschutzerklärung》"
            LegalLanguage.FR -> "《Contrat de licence utilisateur final + Politique de confidentialité》"
            LegalLanguage.ES -> "《Contrato de licencia de usuario final + Política de privacidad》"
            LegalLanguage.PT_BR -> "《Contrato de licença de usuário final + Política de privacidade》"
            LegalLanguage.IT -> "《Contratto di licenza con l'utente finale + Informativa sulla privacy》"
            LegalLanguage.TR -> "《Son Kullanıcı Lisans Sözleşmesi + Gizlilik Politikası》"
            else -> "《End User License Agreement + Privacy Policy》"
        }
        LegalDocId.LICENSES -> when (lang) {
            LegalLanguage.ZH_CN -> "《第三方开源合规声明 + 补充免责与风险告知》"
            LegalLanguage.ZH_TW -> "《第三方開源合規聲明 + 補充免責與風險告知》"
            LegalLanguage.JA -> "《第三者オープンソース遵守声明 + 追加免責・リスク告知》"
            LegalLanguage.KO -> "《제3자 오픈소스 준수 선언 + 추가 면책 및 위험 고지》"
            LegalLanguage.RU -> "《Уведомление о сторонних компонентах + Дополнительные риски》"
            LegalLanguage.DE -> "《Hinweise zu Open-Source-Komponenten + Ergänzende Risikohinweise》"
            LegalLanguage.FR -> "《Mentions open source tierces + Avertissements de risque complémentaires》"
            LegalLanguage.ES -> "《Avisos de componentes de código abierto + Avisos de riesgo adicionales》"
            LegalLanguage.PT_BR -> "《Avisos de componentes de código aberto + Avisos de risco adicionais》"
            LegalLanguage.IT -> "《Note sui componenti open source + Ulteriori avvisi di rischio》"
            LegalLanguage.TR -> "《Açık kaynak bileşen bildirimleri + Ek risk bildirimleri》"
            else -> "《Third-Party Open-Source Notices + Additional Risk Disclosure》"
        }
        LegalDocId.GUIDE -> when (lang) {
            LegalLanguage.ZH_CN -> "《使用教程 + 适用地区与法律管辖》"
            LegalLanguage.ZH_TW -> "《使用教學 + 適用地區與法律管轄》"
            LegalLanguage.JA -> "《使い方 + 対象地域と準拠法》"
            LegalLanguage.KO -> "《사용 안내 + 적용 지역 및 준거법》"
            LegalLanguage.RU -> "《Руководство + Регионы и применимое право》"
            LegalLanguage.DE -> "《Erste Schritte + Regionen und anwendbares Recht》"
            LegalLanguage.FR -> "《Premiers pas + Régions et droit applicable》"
            LegalLanguage.ES -> "《Primeros pasos + Regiones y ley aplicable》"
            LegalLanguage.PT_BR -> "《Primeiros passos + Regiões e lei aplicável》"
            LegalLanguage.IT -> "《Primi passi + Regioni e legge applicabile》"
            LegalLanguage.TR -> "《Başlangıç + Bölgeler ve geçerli hukuk》"
            else -> "《Getting Started + Regions and Governing Law》"
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  正文
    // ─────────────────────────────────────────────────────────────

    private fun body(id: LegalDocId, lang: LegalLanguage): String = when (id) {
        LegalDocId.TERMS -> when (lang) {
            LegalLanguage.ZH_CN -> TERMS_ZH
            LegalLanguage.ZH_TW -> TERMS_ZH_TW
            LegalLanguage.JA -> TERMS_JA
            LegalLanguage.KO -> TERMS_KO
            LegalLanguage.RU -> TERMS_RU
            LegalLanguage.DE -> TERMS_DE
            LegalLanguage.FR -> TERMS_FR
            LegalLanguage.ES -> TERMS_ES
            LegalLanguage.PT_BR -> TERMS_PT_BR
            LegalLanguage.IT -> TERMS_IT
            LegalLanguage.TR -> TERMS_TR
            else -> TERMS_EN
        }
        LegalDocId.LICENSES -> when (lang) {
            LegalLanguage.ZH_CN -> LICENSES_ZH
            LegalLanguage.ZH_TW -> LICENSES_ZH_TW
            LegalLanguage.JA -> LICENSES_JA
            LegalLanguage.KO -> LICENSES_KO
            LegalLanguage.RU -> LICENSES_RU
            LegalLanguage.DE -> LICENSES_DE
            LegalLanguage.FR -> LICENSES_FR
            LegalLanguage.ES -> LICENSES_ES
            LegalLanguage.PT_BR -> LICENSES_PT_BR
            LegalLanguage.IT -> LICENSES_IT
            LegalLanguage.TR -> LICENSES_TR
            else -> LICENSES_EN
        }
        LegalDocId.GUIDE -> when (lang) {
            LegalLanguage.ZH_CN -> GUIDE_ZH
            LegalLanguage.ZH_TW -> GUIDE_ZH_TW
            LegalLanguage.JA -> GUIDE_JA
            LegalLanguage.KO -> GUIDE_KO
            LegalLanguage.RU -> GUIDE_RU
            LegalLanguage.DE -> GUIDE_DE
            LegalLanguage.FR -> GUIDE_FR
            LegalLanguage.ES -> GUIDE_ES
            LegalLanguage.PT_BR -> GUIDE_PT_BR
            LegalLanguage.IT -> GUIDE_IT
            LegalLanguage.TR -> GUIDE_TR
            else -> GUIDE_EN
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  本地区补充条款
    // ─────────────────────────────────────────────────────────────

    /** 一个语言下的地区条款模板；`window` / `noWindow` 是「是否启用时段限制」两种说法 */
    private class JurisText(
        val terms: String,
        val licenses: String,
        val guide: String,
        val window: String,
        val noWindow: String,
    )

    private fun jurisText(lang: LegalLanguage): JurisText = when (lang) {
        LegalLanguage.ZH_CN, LegalLanguage.ZH_TW -> JurisText(
            JURIS_TERMS_ZH, JURIS_LICENSES_ZH, JURIS_GUIDE_ZH,
            window = "；未满 {age} 周岁仅可在周五、周六、周日及法定节假日 20:00–21:00 启动游戏",
            noWindow = "；未满该年龄需取得监护人同意"
        )
        LegalLanguage.DE -> JurisText(
            JURIS_TERMS_DE, JURIS_LICENSES_DE, JURIS_GUIDE_DE,
            window = " Unter-{age}-Jährige dürfen das Spiel nur freitags, samstags, sonntags und an gesetzlichen Feiertagen von 20:00 bis 21:00 starten.",
            noWindow = " Minderjährige benötigen die Zustimmung einer erziehungsberechtigten Person."
        )
        LegalLanguage.FR -> JurisText(
            JURIS_TERMS_FR, JURIS_LICENSES_FR, JURIS_GUIDE_FR,
            window = " Les moins de {age} ans ne peuvent lancer le jeu que le vendredi, le samedi, le dimanche et les jours fériés, de 20 h à 21 h.",
            noWindow = " Les mineurs doivent obtenir l'accord d'un représentant légal."
        )
        LegalLanguage.ES -> JurisText(
            JURIS_TERMS_ES, JURIS_LICENSES_ES, JURIS_GUIDE_ES,
            window = " Los menores de {age} años solo pueden iniciar el juego los viernes, sábados, domingos y días festivos, de 20:00 a 21:00.",
            noWindow = " Los menores necesitan la autorización de su representante legal."
        )
        LegalLanguage.PT_BR -> JurisText(
            JURIS_TERMS_PT_BR, JURIS_LICENSES_PT_BR, JURIS_GUIDE_PT_BR,
            window = " Menores de {age} anos só podem iniciar o jogo às sextas, sábados, domingos e feriados, das 20h às 21h.",
            noWindow = " Menores precisam da autorização de um responsável legal."
        )
        LegalLanguage.IT -> JurisText(
            JURIS_TERMS_IT, JURIS_LICENSES_IT, JURIS_GUIDE_IT,
            window = " I minori di {age} anni possono avviare il gioco solo il venerdì, il sabato, la domenica e nei giorni festivi, dalle 20:00 alle 21:00.",
            noWindow = " I minori necessitano dell'autorizzazione di un genitore o tutore."
        )
        LegalLanguage.TR -> JurisText(
            JURIS_TERMS_TR, JURIS_LICENSES_TR, JURIS_GUIDE_TR,
            window = " {age} yaş altındakiler oyunu yalnızca cuma, cumartesi, pazar ve resmî tatillerde 20:00–21:00 arasında başlatabilir.",
            noWindow = " Bu yaşın altındakiler için veli onayı gerekir."
        )
        //其余语言暂用英文地区条款，正文仍是各自语言（不会出现空白）
        else -> JurisText(
            JURIS_TERMS_EN, JURIS_LICENSES_EN, JURIS_GUIDE_EN,
            window = " Under-{age}s may only launch the game on Fridays, Saturdays, Sundays and statutory holidays, 20:00–21:00.",
            noWindow = " Users below that age need guardian consent."
        )
    }

    private fun jurisdiction(id: LegalDocId, region: LegalRegion, lang: LegalLanguage): String {
        val text = jurisText(lang)
        val template = when (id) {
            LegalDocId.TERMS -> text.terms
            LegalDocId.LICENSES -> text.licenses
            LegalDocId.GUIDE -> text.guide
        }
        val window = if (region.playTimeWindow) text.window else text.noWindow
        return template
            // {timeWindow} 自带 {age}，必须排在 {age} 之前替换
            .replace("{timeWindow}", window)
            .replace("{region}", regionName(region, lang))
            .replace("{law}", region.dataLaw)
            .replace("{regulator}", region.regulator)
            .replace("{age}", region.minorAge.toString())
    }

    private fun regionName(region: LegalRegion, lang: LegalLanguage): String {
        val locale = Locale.forLanguageTag(lang.tag)
        return Locale("", region.code).getDisplayCountry(locale).ifBlank { region.code }
    }
}
