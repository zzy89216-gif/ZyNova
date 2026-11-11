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
            LegalLanguage.VI -> "《Thỏa thuận cấp phép người dùng cuối + Chính sách quyền riêng tư》"
            LegalLanguage.TH -> "《ข้อตกลงอนุญาตให้ใช้สิทธิผู้ใช้ปลายทาง + นโยบายความเป็นส่วนตัว》"
            LegalLanguage.ID -> "《Perjanjian Lisensi Pengguna Akhir + Kebijakan Privasi》"
            LegalLanguage.PL -> "《Umowa licencyjna użytkownika końcowego + Polityka prywatności》"
            LegalLanguage.UK -> "《Ліцензійна угода кінцевого користувача + Політика конфіденційності》"
            LegalLanguage.RO -> "《Acord de licență pentru utilizatorul final + Politica de confidențialitate》"
            LegalLanguage.BG -> "《Лицензионно споразумение за краен потребител + Политика за поверителност》"
            LegalLanguage.EL -> "《Άδεια τελικού χρήστη + Πολιτική απορρήτου》"
            LegalLanguage.AR -> "《اتفاقية ترخيص المستخدم النهائي + سياسة الخصوصية》"
            LegalLanguage.FA -> "《توافق‌نامه مجوز کاربر نهایی + سیاست حفظ حریم خصوصی》"
            LegalLanguage.ML -> "《അന്തിമ ഉപയോക്തൃ ലൈസൻസ് കരാർ + സ്വകാര്യതാ നയം》"
            LegalLanguage.UR_LATN -> "《End User License Agreement + Privacy Policy》"
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
            LegalLanguage.VI -> "《Thông báo thành phần mã nguồn mở + Cảnh báo rủi ro bổ sung》"
            LegalLanguage.TH -> "《ประกาศคอมโพเนนต์โอเพนซอร์ส + คำเตือนความเสี่ยงเพิ่มเติม》"
            LegalLanguage.ID -> "《Pemberitahuan Komponen Sumber Terbuka + Pemberitahuan Risiko Tambahan》"
            LegalLanguage.PL -> "《Informacje o komponentach open source + Dodatkowe ostrzeżenia o ryzyku》"
            LegalLanguage.UK -> "《Повідомлення про компоненти з відкритим кодом + Додаткові попередження про ризики》"
            LegalLanguage.RO -> "《Notificări privind componentele open source + Avertismente suplimentare de risc》"
            LegalLanguage.BG -> "《Уведомления за компоненти с отворен код + Допълнителни предупреждения за рискове》"
            LegalLanguage.EL -> "《Σημειώσεις στοιχείων ανοιχτού κώδικα + Πρόσθετες προειδοποιήσεις κινδύνου》"
            LegalLanguage.AR -> "《إشعارات مكوّنات المصدر المفتوح + إفصاحات مخاطر إضافية》"
            LegalLanguage.FA -> "《اعلان اجزای متن‌باز + هشدارهای ریسک تکمیلی》"
            LegalLanguage.ML -> "《ഓപ്പൺ സോഴ്സ് ഘടകങ്ങളുടെ അറിയിപ്പ് + അധിക അപകടസൂചനകൾ》"
            LegalLanguage.UR_LATN -> "《Open-source Components Notice + Izafi Risk Disclosure》"
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
            LegalLanguage.VI -> "《Bắt đầu + Khu vực và pháp luật áp dụng》"
            LegalLanguage.TH -> "《เริ่มต้นใช้งาน + ภูมิภาคและกฎหมายที่ใช้บังคับ》"
            LegalLanguage.ID -> "《Memulai + Wilayah dan Hukum yang Berlaku》"
            LegalLanguage.PL -> "《Pierwsze kroki + Regiony i prawo właściwe》"
            LegalLanguage.UK -> "《Початок роботи + Регіони та застосовне право》"
            LegalLanguage.RO -> "《Primii pași + Regiuni și legea aplicabilă》"
            LegalLanguage.BG -> "《Първи стъпки + Региони и приложимо право》"
            LegalLanguage.EL -> "《Πρώτα βήματα + Περιοχές και εφαρμοστέο δίκαιο》"
            LegalLanguage.AR -> "《البدء + المناطق والقانون الواجب التطبيق》"
            LegalLanguage.FA -> "《شروع کار + منطقه‌ها و قانون حاکم》"
            LegalLanguage.ML -> "《തുടക്കം + പ്രദേശങ്ങളും ബാധകമായ നിയമവും》"
            LegalLanguage.UR_LATN -> "《Shuruat + Regions aur Laagu Qanoon》"
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
            LegalLanguage.VI -> TERMS_VI
            LegalLanguage.TH -> TERMS_TH
            LegalLanguage.ID -> TERMS_ID
            LegalLanguage.PL -> TERMS_PL
            LegalLanguage.UK -> TERMS_UK
            LegalLanguage.RO -> TERMS_RO
            LegalLanguage.BG -> TERMS_BG
            LegalLanguage.EL -> TERMS_EL
            LegalLanguage.AR -> TERMS_AR
            LegalLanguage.FA -> TERMS_FA
            LegalLanguage.ML -> TERMS_ML
            LegalLanguage.UR_LATN -> TERMS_UR_LATN
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
            LegalLanguage.VI -> LICENSES_VI
            LegalLanguage.TH -> LICENSES_TH
            LegalLanguage.ID -> LICENSES_ID
            LegalLanguage.PL -> LICENSES_PL
            LegalLanguage.UK -> LICENSES_UK
            LegalLanguage.RO -> LICENSES_RO
            LegalLanguage.BG -> LICENSES_BG
            LegalLanguage.EL -> LICENSES_EL
            LegalLanguage.AR -> LICENSES_AR
            LegalLanguage.FA -> LICENSES_FA
            LegalLanguage.ML -> LICENSES_ML
            LegalLanguage.UR_LATN -> LICENSES_UR_LATN
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
            LegalLanguage.VI -> GUIDE_VI
            LegalLanguage.TH -> GUIDE_TH
            LegalLanguage.ID -> GUIDE_ID
            LegalLanguage.PL -> GUIDE_PL
            LegalLanguage.UK -> GUIDE_UK
            LegalLanguage.RO -> GUIDE_RO
            LegalLanguage.BG -> GUIDE_BG
            LegalLanguage.EL -> GUIDE_EL
            LegalLanguage.AR -> GUIDE_AR
            LegalLanguage.FA -> GUIDE_FA
            LegalLanguage.ML -> GUIDE_ML
            LegalLanguage.UR_LATN -> GUIDE_UR_LATN
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
        LegalLanguage.VI -> JurisText(
            JURIS_TERMS_VI, JURIS_LICENSES_VI, JURIS_GUIDE_VI,
            window = " Người dưới {age} tuổi chỉ có thể khởi chạy trò chơi vào thứ Sáu, thứ Bảy, Chủ nhật và ngày lễ, từ 20:00 đến 21:00.",
            noWindow = " Người dưới độ tuổi đó cần sự đồng ý của người giám hộ."
        )
        LegalLanguage.TH -> JurisText(
            JURIS_TERMS_TH, JURIS_LICENSES_TH, JURIS_GUIDE_TH,
            window = " ผู้ที่มีอายุต่ำกว่า {age} ปีเริ่มเกมได้เฉพาะวันศุกร์ เสาร์ อาทิตย์ และวันหยุดนักขัตฤกษ์ เวลา 20:00–21:00",
            noWindow = " ผู้ที่อายุต่ำกว่านั้นต้องได้รับความยินยอมจากผู้ปกครอง"
        )
        LegalLanguage.ID -> JurisText(
            JURIS_TERMS_ID, JURIS_LICENSES_ID, JURIS_GUIDE_ID,
            window = " Pengguna di bawah {age} tahun hanya dapat memulai permainan pada Jumat, Sabtu, Minggu, dan hari libur resmi pukul 20.00–21.00.",
            noWindow = " Pengguna di bawah usia tersebut memerlukan persetujuan wali."
        )
        LegalLanguage.PL -> JurisText(
            JURIS_TERMS_PL, JURIS_LICENSES_PL, JURIS_GUIDE_PL,
            window = " Osoby poniżej {age} lat mogą uruchamiać grę tylko w piątki, soboty, niedziele i dni ustawowo wolne w godzinach 20:00–21:00.",
            noWindow = " Osoby poniżej tego wieku potrzebują zgody opiekuna."
        )
        LegalLanguage.UK -> JurisText(
            JURIS_TERMS_UK, JURIS_LICENSES_UK, JURIS_GUIDE_UK,
            window = " Особи до {age} років можуть запускати гру лише в п’ятницю, суботу, неділю та у святкові дні з 20:00 до 21:00.",
            noWindow = " Особам, молодшим за цей вік, потрібна згода опікуна."
        )
        LegalLanguage.RO -> JurisText(
            JURIS_TERMS_RO, JURIS_LICENSES_RO, JURIS_GUIDE_RO,
            window = " Persoanele sub {age} ani pot porni jocul doar vinerea, sâmbăta, duminica și în zilele de sărbătoare legală, între 20:00 și 21:00.",
            noWindow = " Persoanele sub această vârstă au nevoie de acordul unui tutore."
        )
        LegalLanguage.BG -> JurisText(
            JURIS_TERMS_BG, JURIS_LICENSES_BG, JURIS_GUIDE_BG,
            window = " Лица под {age} години могат да стартират играта само в петък, събота, неделя и в официалните празници от 20:00 до 21:00.",
            noWindow = " Лицата под тази възраст се нуждаят от съгласието на настойник."
        )
        LegalLanguage.EL -> JurisText(
            JURIS_TERMS_EL, JURIS_LICENSES_EL, JURIS_GUIDE_EL,
            window = " Τα άτομα κάτω των {age} ετών μπορούν να ξεκινούν το παιχνίδι μόνο Παρασκευή, Σάββατο, Κυριακή και στις επίσημες αργίες, 20:00–21:00.",
            noWindow = " Τα άτομα κάτω από αυτή την ηλικία χρειάζονται συγκατάθεση κηδεμόνα."
        )
        LegalLanguage.AR -> JurisText(
            JURIS_TERMS_AR, JURIS_LICENSES_AR, JURIS_GUIDE_AR,
            window = " لا يجوز لمن هم دون {age} سنة تشغيل اللعبة إلا أيام الجمعة والسبت والأحد والعطلات الرسمية من 20:00 إلى 21:00.",
            noWindow = " يحتاج من هم دون هذا السن إلى موافقة وليّ الأمر."
        )
        LegalLanguage.FA -> JurisText(
            JURIS_TERMS_FA, JURIS_LICENSES_FA, JURIS_GUIDE_FA,
            window = " افراد زیر {age} سال تنها جمعه، شنبه، یکشنبه و تعطیلات رسمی از ساعت ۲۰:۰۰ تا ۲۱:۰۰ می‌توانند بازی را اجرا کنند.",
            noWindow = " افراد زیر این سن به رضایت سرپرست نیاز دارند."
        )
        LegalLanguage.ML -> JurisText(
            JURIS_TERMS_ML, JURIS_LICENSES_ML, JURIS_GUIDE_ML,
            window = " {age} വയസ്സിൽ താഴെയുള്ളവർക്ക് വെള്ളി, ശനി, ഞായർ ദിവസങ്ങളിലും സർക്കാർ അവധിദിനങ്ങളിലും 20:00 മുതൽ 21:00 വരെ മാത്രമേ ഗെയിം തുടങ്ങാൻ കഴിയൂ.",
            noWindow = " ആ പ്രായത്തിൽ താഴെയുള്ളവർക്ക് രക്ഷിതാവിന്റെ സമ്മതം ആവശ്യമാണ്."
        )
        LegalLanguage.UR_LATN -> JurisText(
            JURIS_TERMS_UR_LATN, JURIS_LICENSES_UR_LATN, JURIS_GUIDE_UR_LATN,
            window = " {age} saal se kam umar ke log game sirf Juma, Hafta aur Itwar aur sarkari chhuttiyon mein 20:00 se 21:00 tak shuru kar sakte hain.",
            noWindow = " Is umar se kam walon ko sarparast ki razamandi chahiye."
        )
        LegalLanguage.JA -> JurisText(
            JURIS_TERMS_JA, JURIS_LICENSES_JA, JURIS_GUIDE_JA,
            window = " {age}歳未満は金曜・土曜・日曜および祝日の 20:00〜21:00 のみゲームを起動できます。",
            noWindow = " この年齢未満は保護者の同意が必要です。"
        )
        LegalLanguage.KO -> JurisText(
            JURIS_TERMS_KO, JURIS_LICENSES_KO, JURIS_GUIDE_KO,
            window = " {age}세 미만은 금요일, 토요일, 일요일 및 공휴일에 20:00~21:00에만 게임을 시작할 수 있습니다.",
            noWindow = " 이 연령 미만은 보호자의 동의가 필요합니다."
        )
        LegalLanguage.RU -> JurisText(
            JURIS_TERMS_RU, JURIS_LICENSES_RU, JURIS_GUIDE_RU,
            window = " Лица младше {age} лет могут запускать игру только по пятницам, субботам, воскресеньям и в праздничные дни с 20:00 до 21:00.",
            noWindow = " Лицам младше этого возраста нужно согласие опекуна."
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
