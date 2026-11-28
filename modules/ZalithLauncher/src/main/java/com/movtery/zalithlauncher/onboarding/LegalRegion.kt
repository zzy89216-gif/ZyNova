/*
 * ZyNova Launcher — 开放地区与当地合规要素
 * GPL-3.0-or-later
 */
package com.movtery.zalithlauncher.onboarding

/**
 * 一个「开放地区」= 一个国家/地区代码 + 当地合规要素。
 *
 * 这些字段会被拼进三份声明的「本地区补充条款」里，所以**同一份声明在不同国家内容不同**：
 * 适用哪部数据保护法、哪个监管机构、几岁算未成年、是否启用未成年人时段限制。
 *
 * 判定口径（维护者 2026-11 确认）：开放范围跟「已提供语言」对齐，共 59 个国家/地区。
 */
data class LegalRegion(
    val code: String,
    /** 该地区的主语言：语言与地区无对应文本时用它兜底 */
    val language: LegalLanguage,
    /** 适用的个人数据保护法（保留当地官方名称，不翻译法律名） */
    val dataLaw: String,
    /** 数据保护监管机构（保留当地官方名称） */
    val regulator: String,
    /** 未成年判定年龄 */
    val minorAge: Int,
    /** 是否启用「仅周五/六/日与法定节假日 20:00–21:00」的时段限制（目前只有中国） */
    val playTimeWindow: Boolean = false,
)

/**
 * 地区表。键是 ISO 3166-1 alpha-2 国家/地区代码（大写）。
 *
 * ⚠️ 新增地区必须同时满足：
 * 1. 该地区有对应的介绍页语言（`i18n/`），否则界面与条款对不上；
 * 2. 在 [LegalDocuments] 里能给出像样的本地化条款，不要硬塞。
 */
object LegalRegions {

    private fun eu(
        code: String,
        lang: LegalLanguage,
        law: String = "GDPR (EU 2016/679)",
        regulator: String = "各国数据保护监管机构 (DPA)",
        minorAge: Int = 16,
    ) = LegalRegion(code, lang, law, regulator, minorAge)

    val ALL: Map<String, LegalRegion> = listOf(
        // ── 中文 ────────────────────────────────────────────────
        LegalRegion(
            "CN", LegalLanguage.ZH_CN,
            "《个人信息保护法》(PIPL)、《数据安全法》、《网络安全法》",
            "国家互联网信息办公室 (CAC)",
            minorAge = 18, playTimeWindow = true
        ),
        LegalRegion(
            "HK", LegalLanguage.ZH_TW,
            "《個人資料（私隱）條例》(PDPO)",
            "個人資料私隱專員公署 (PCPD)",
            minorAge = 18
        ),
        LegalRegion(
            "MO", LegalLanguage.ZH_TW,
            "《個人資料保護法》(Lei n.º 8/2005)",
            "個人資料保護辦公室 (GPDP)",
            minorAge = 18
        ),
        LegalRegion(
            "TW", LegalLanguage.ZH_TW,
            "《個人資料保護法》",
            "個人資料保護委員會",
            minorAge = 18
        ),

        // ── 英语 ────────────────────────────────────────────────
        LegalRegion(
            "US", LegalLanguage.EN,
            "CCPA/CPRA（加州）、COPPA（13 岁以下）、各州隐私法",
            "联邦贸易委员会 (FTC) 与各州总检察长",
            minorAge = 13
        ),
        LegalRegion(
            "GB", LegalLanguage.EN,
            "UK GDPR、Data Protection Act 2018",
            "信息专员办公室 (ICO)",
            minorAge = 13
        ),
        LegalRegion("CA", LegalLanguage.EN, "PIPEDA", "隐私专员办公室 (OPC)", 13),
        LegalRegion("AU", LegalLanguage.EN, "Privacy Act 1988", "澳大利亚信息专员办公室 (OAIC)", 15),
        LegalRegion("NZ", LegalLanguage.EN, "Privacy Act 2020", "隐私专员办公室 (OPC)", 16),
        eu("IE", LegalLanguage.EN, regulator = "数据保护委员会 (DPC)"),
        LegalRegion("SG", LegalLanguage.EN, "PDPA 2012", "个人数据保护委员会 (PDPC)", 13),
        LegalRegion("ZA", LegalLanguage.EN, "POPIA", "信息监管局 (Information Regulator)", 18),

        // ── 日 / 韩 ─────────────────────────────────────────────
        LegalRegion(
            "JP", LegalLanguage.JA,
            "《個人情報の保護に関する法律》(APPI)",
            "個人情報保護委員会 (PPC)",
            minorAge = 15
        ),
        LegalRegion(
            "KR", LegalLanguage.KO,
            "《개인정보 보호법》(PIPA)",
            "개인정보보호위원회 (PIPC)",
            minorAge = 14
        ),

        // ── 土耳其 / 俄语区 ──────────────────────────────────────
        LegalRegion(
            "TR", LegalLanguage.TR,
            "Kişisel Verilerin Korunması Kanunu (KVKK, 6698)",
            "Kişisel Verileri Koruma Kurumu",
            minorAge = 18
        ),
        LegalRegion("RU", LegalLanguage.RU, "152-ФЗ《个人数据法》", "Roskomnadzor", 18),
        LegalRegion("BY", LegalLanguage.RU, "《个人数据保护法》(2008)", "国家个人数据保护中心", 18),
        LegalRegion("KZ", LegalLanguage.RU, "《个人数据及其保护法》(2013)", "数字发展部", 18),

        // ── 东南亚 ──────────────────────────────────────────────
        LegalRegion("VN", LegalLanguage.VI, "Decree 13/2023/ND-CP", "公安部 (MPS)", 16),
        LegalRegion("TH", LegalLanguage.TH, "PDPA B.E. 2562 (2019)", "个人数据保护委员会 (PDPC)", 20),
        LegalRegion("ID", LegalLanguage.ID, "UU No. 27/2022 (PDP Law)", "个人数据保护机构", 18),

        // ── 德语区 ──────────────────────────────────────────────
        eu("DE", LegalLanguage.DE, "GDPR、BDSG", "各州数据保护监管机构 (DPA)"),
        eu("AT", LegalLanguage.DE, "GDPR、DSG", "数据保护局 (DSB)"),
        LegalRegion(
            "CH", LegalLanguage.DE,
            "nFADP（修订版联邦数据保护法）",
            "联邦数据保护与信息专员 (FDPIC)",
            minorAge = 16
        ),

        // ── 西语区 ──────────────────────────────────────────────
        eu("ES", LegalLanguage.ES, "GDPR、LOPDGDD 3/2018", "数据保护局 (AEPD)"),
        LegalRegion("MX", LegalLanguage.ES, "LFPDPPP", "国家透明度与信息保护研究所 (INAI)", 18),
        LegalRegion("AR", LegalLanguage.ES, "Ley 25.326", "数据保护局 (AAIP)", 18),
        LegalRegion("CL", LegalLanguage.ES, "Ley 19.628 / Ley 21.719", "个人数据保护局", 18),
        LegalRegion("CO", LegalLanguage.ES, "Ley 1581 de 2012", "工商监管局 (SIC)", 18),
        LegalRegion("PE", LegalLanguage.ES, "Ley 29733", "国家个人数据保护局 (ANPD)", 18),

        // ── 法语区 ──────────────────────────────────────────────
        eu("FR", LegalLanguage.FR, "GDPR、Loi Informatique et Libertés", "国家信息与自由委员会 (CNIL)", 15),
        eu("BE", LegalLanguage.FR, regulator = "数据保护局 (APD/GBA)"),
        eu("LU", LegalLanguage.FR, regulator = "国家数据保护委员会 (CNPD)"),
        LegalRegion("MC", LegalLanguage.FR, "Loi n° 1.165", "个人数据保护局 (APDP)", 16),

        // ── 东欧 ────────────────────────────────────────────────
        LegalRegion("UA", LegalLanguage.UK, "《个人数据保护法》(2010)", "议会人权专员", 18),
        eu("PL", LegalLanguage.PL, "GDPR、UODO 法", "个人数据保护办公室 (UODO)"),
        eu("RO", LegalLanguage.RO, "GDPR、Legea 190/2018", "国家个人数据处理监管局 (ANSPDCP)"),
        LegalRegion("MD", LegalLanguage.RO, "Legea 133/2011", "国家个人数据保护中心 (CNPDCP)", 16),
        eu("BG", LegalLanguage.BG, "GDPR、ЗЗЛД", "个人数据保护委员会 (CPDP)"),
        eu("GR", LegalLanguage.EL, "GDPR、Law 4624/2019", "个人数据保护局 (HDPA)", 15),
        eu("CY", LegalLanguage.EL, regulator = "个人数据保护专员办公室"),

        // ── 葡语区 ──────────────────────────────────────────────
        LegalRegion("BR", LegalLanguage.PT_BR, "LGPD (Lei 13.709/2018)", "国家数据保护局 (ANPD)", 18),
        eu("PT", LegalLanguage.PT_BR, "GDPR、Lei 58/2019", "国家数据保护委员会 (CNPD)"),

        // ── 中东 ────────────────────────────────────────────────
        GCC_SA(), GCC_AE(), GCC_EG(), GCC_DZ(), GCC_MA(), GCC_TN(),
        GCC_JO(), GCC_KW(), GCC_QA(), GCC_BH(), GCC_OM(),
        LegalRegion("IR", LegalLanguage.FA, "《个人数据保护法》(1402)", "国家数据与信息安全中心", 18),

        // ── 南亚 ────────────────────────────────────────────────
        LegalRegion("IN", LegalLanguage.ML, "DPDP Act 2023", "印度数据保护委员会", 18),
        LegalRegion("PK", LegalLanguage.UR_LATN, "PDPA（草案）、PECA 2016", "国家网络犯罪调查局 (NCCIA)", 18),

        // ── 意大利 / 圣马力诺 ────────────────────────────────────
        eu("IT", LegalLanguage.IT, "GDPR、Codice Privacy", "数据保护局 (Garante)"),
        LegalRegion("SM", LegalLanguage.IT, "Legge 21/2018", "数据保护局", 16),
    ).associateBy { it.code }

    // 海湾与北非：数据法各不相同，单独列，避免用「一套 GDPR 走天下」
    private fun GCC_SA() = LegalRegion("SA", LegalLanguage.AR, "PDPL（个人数据保护法）", "通信与信息技术委员会 (CITC)", 18)
    private fun GCC_AE() = LegalRegion("AE", LegalLanguage.AR, "Federal Decree-Law No. 45/2021 (PDPL)", "阿联酋数据办公室", 18)
    private fun GCC_EG() = LegalRegion("EG", LegalLanguage.AR, "Law No. 151/2020 (PDPL)", "个人数据保护中心", 18)
    private fun GCC_DZ() = LegalRegion("DZ", LegalLanguage.AR, "Loi 18-07", "国家个人数据保护局 (ANPDP)", 18)
    private fun GCC_MA() = LegalRegion("MA", LegalLanguage.AR, "Loi 09-08", "国家个人数据保护委员会 (CNDP)", 18)
    private fun GCC_TN() = LegalRegion("TN", LegalLanguage.AR, "Loi organique 2004-63", "国家个人数据保护局 (INPDP)", 18)
    private fun GCC_JO() = LegalRegion("JO", LegalLanguage.AR, "Personal Data Protection Law No. 24/2023", "个人数据保护委员会", 18)
    private fun GCC_KW() = LegalRegion("KW", LegalLanguage.AR, "Data Privacy Protection Regulation (CITRA)", "通信与信息技术监管局 (CITRA)", 18)
    private fun GCC_QA() = LegalRegion("QA", LegalLanguage.AR, "Law No. 13/2016", "国家数据保护委员会", 18)
    private fun GCC_BH() = LegalRegion("BH", LegalLanguage.AR, "Personal Data Protection Law 2018", "个人数据保护局", 18)
    private fun GCC_OM() = LegalRegion("OM", LegalLanguage.AR, "Royal Decree 6/2022 (PDPL)", "交通、通信与信息技术部", 18)

    fun byCountry(code: String?): LegalRegion? = ALL[code?.uppercase() ?: return null]
}
