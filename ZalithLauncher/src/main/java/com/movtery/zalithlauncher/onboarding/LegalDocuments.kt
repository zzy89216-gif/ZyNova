/*
 * ZyNova Launcher — 三份声明的正文与「本地区补充条款」
 * GPL-3.0-or-later
 *
 * 组成方式：
 *   某一语言的正文本  +  某一国家/地区的补充条款
 * 所以「同一份声明，中国用户和美国用户看到的不一样」，
 * 而「同一个地区，用日语系统和用英语系统看到的正文语言不一样」。
 */
package com.movtery.zalithlauncher.onboarding

import java.util.Locale

enum class LegalDocId { TERMS, LICENSES, GUIDE }

data class LegalDoc(val title: String, val body: String)

object LegalDocuments {

    fun build(id: LegalDocId, region: LegalRegion, lang: LegalLanguage): LegalDoc {
        val base = when (id) {
            LegalDocId.TERMS -> terms(lang)
            LegalDocId.LICENSES -> licenses(lang)
            LegalDocId.GUIDE -> guide(lang)
        }
        return LegalDoc(titleOf(id, lang), base.trimEnd() + "\n\n" + jurisdiction(id, region, lang))
    }

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
            else -> "《End User License Agreement + Privacy Policy》"
        }
        LegalDocId.LICENSES -> when (lang) {
            LegalLanguage.ZH_CN -> "《第三方开源合规声明 + 补充免责与风险告知》"
            LegalLanguage.ZH_TW -> "《第三方開源合規聲明 + 補充免責與風險告知》"
            LegalLanguage.JA -> "《第三者オープンソース遵守声明 + 追加免責・リスク告知》"
            LegalLanguage.KO -> "《제3자 오픈소스 준수 선언 + 추가 면책 및 위험 고지》"
            LegalLanguage.RU -> "《Уведомление о сторонних компонентах + Дополнительные риски》"
            else -> "《Third-Party Open-Source Notices + Additional Risk Disclosure》"
        }
        LegalDocId.GUIDE -> when (lang) {
            LegalLanguage.ZH_CN -> "《使用教程 + 适用地区与法律管辖》"
            LegalLanguage.ZH_TW -> "《使用教學 + 適用地區與法律管轄》"
            LegalLanguage.JA -> "《使い方 + 対象地域と準拠法》"
            LegalLanguage.KO -> "《사용 안내 + 적용 지역 및 준거법》"
            LegalLanguage.RU -> "《Руководство + Регионы и применимое право》"
            else -> "《Getting Started + Regions and Governing Law》"
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  正文（按语言）
    // ─────────────────────────────────────────────────────────────

    private fun terms(lang: LegalLanguage): String = when (lang) {
        LegalLanguage.ZH_CN -> TERMS_ZH
        LegalLanguage.ZH_TW -> TERMS_ZH_TW
        LegalLanguage.JA -> TERMS_JA
        LegalLanguage.KO -> TERMS_KO
        LegalLanguage.RU -> TERMS_RU
        else -> TERMS_EN
    }

    private fun licenses(lang: LegalLanguage): String = when (lang) {
        LegalLanguage.ZH_CN -> LICENSES_ZH
        LegalLanguage.ZH_TW -> LICENSES_ZH_TW
        LegalLanguage.JA -> LICENSES_JA
        LegalLanguage.KO -> LICENSES_KO
        LegalLanguage.RU -> LICENSES_RU
        else -> LICENSES_EN
    }

    private fun guide(lang: LegalLanguage): String = when (lang) {
        LegalLanguage.ZH_CN -> GUIDE_ZH
        LegalLanguage.ZH_TW -> GUIDE_ZH_TW
        LegalLanguage.JA -> GUIDE_JA
        LegalLanguage.KO -> GUIDE_KO
        LegalLanguage.RU -> GUIDE_RU
        else -> GUIDE_EN
    }

    // ─────────────────────────────────────────────────────────────
    //  本地区补充条款
    // ─────────────────────────────────────────────────────────────

    private fun regionName(region: LegalRegion, lang: LegalLanguage): String =
        Locale.forLanguageTag(lang.tag).let { locale ->
            Locale("", region.code).getDisplayCountry(locale).ifBlank { region.code }
        }

    private fun jurisdiction(id: LegalDocId, region: LegalRegion, lang: LegalLanguage): String {
        val zh = lang == LegalLanguage.ZH_CN || lang == LegalLanguage.ZH_TW
        val name = regionName(region, lang)
        val template = when (id) {
            LegalDocId.TERMS -> if (zh) JURIS_TERMS_ZH else JURIS_TERMS_EN
            LegalDocId.LICENSES -> if (zh) JURIS_LICENSES_ZH else JURIS_LICENSES_EN
            LegalDocId.GUIDE -> if (zh) JURIS_GUIDE_ZH else JURIS_GUIDE_EN
        }
        // 时段限制只有中国开启：海外用户不该看到一条当地不存在的规定
        val windowText = when {
            zh && region.playTimeWindow ->
                "；未满 {age} 周岁仅可在周五、周六、周日及法定节假日 20:00–21:00 启动游戏"
            zh -> "；未满该年龄需取得监护人同意"
            region.playTimeWindow ->
                " Under-{age}s may only launch the game on Fridays, Saturdays, Sundays and statutory holidays, 20:00–21:00."
            else -> " Users below that age need guardian consent."
        }
        return template
            // {timeWindow} 自带 {age}，必须排在 {age} 之前替换
            .replace("{timeWindow}", windowText)
            .replace("{region}", name)
            .replace("{law}", region.dataLaw)
            .replace("{regulator}", region.regulator)
            .replace("{age}", region.minorAge.toString())
    }

    // ── 中文补充条款 ──────────────────────────────────────────
    private val JURIS_TERMS_ZH = """
        《本地区补充条款 · {region}》

        一、个人数据保护
        本地区适用 {law}，监管机构为 {regulator}。你对自己的个人数据享有访问、更正、删除、撤回同意、限制处理与投诉等权利（以当地法律为准）。本产品默认不在设备之外收集个人信息；数据是否离开设备，完全取决于你是否主动启用第三方功能。

        二、跨境传输
        只有你主动启用第三方服务（AI 模型、搜索引擎、联机节点、内网穿透）时，相关数据才会传输到该服务所在地，由该服务商按其隐私政策处理，本产品作者无法控制。

        三、未成年人
        本地区以 {age} 周岁为未成年界限{timeWindow}。
    """.trimIndent()

    private val JURIS_LICENSES_ZH = """
        《本地区补充条款 · {region}》

        一、许可证义务的效力
        GPL-3.0 / AGPL-3.0 / LGPL 的义务按本地区法律解释与执行；AGPL-3.0 §13「以网络服务形式提供须公开对应源码」在承认著作权的大多数司法辖区可执行。

        二、出口管制与制裁
        不得将本产品用于受制裁国家/地区、受制裁个人或实体，或用于违反出口管制与制裁规定的用途。

        三、第三方服务的可用性
        AI 模型、搜索引擎、内网穿透、联机节点在不同地区的可用性、计费规则与合规要求由各服务商自行决定，可能在你所在地区不可用或受限。
    """.trimIndent()

    private val JURIS_GUIDE_ZH = """
        《本地区补充条款 · {region}》

        一、适用法律与管辖
        你与本产品之间的争议适用{region}法律；由你所在地有管辖权的法院处理。当地消费者保护法赋予你的强制性权利不受本条款影响。

        二、未成年人保护
        本地区以 {age} 周岁为未成年界限{timeWindow}。

        三、语言与地区识别
        正文语言取系统语言，地区条款取系统国家/地区与时区；两者都在开放范围内才可进入主界面。

        四、被误拦截怎么办
        阻断页会显示判定依据（国家 / 时区 / 语言）。确认自己在开放地区却被拦截时，请把该行信息附到 GitHub Issues。
    """.trimIndent()

    // ── 英文补充条款 ──────────────────────────────────────────
    private val JURIS_TERMS_EN = """
        Regional Supplement — {region}

        1. Personal data
        {law} applies in this region; the supervisory authority is {regulator}. You have the rights to access, rectify, erase, withdraw consent, restrict processing and lodge a complaint, as provided by local law. The product collects nothing outside your device by default; whether data leaves the device depends entirely on the third-party features you enable.

        2. Cross-border transfer
        Data is transferred only when you enable a third-party feature (AI model, search engine, multiplayer relay, tunnel). It is then processed by that provider under its own privacy policy, outside the author's control.

        3. Minors
        The age of majority applied here is {age}.{timeWindow}
    """.trimIndent()

    private val JURIS_LICENSES_EN = """
        Regional Supplement — {region}

        1. Effect of licence obligations
        GPL-3.0 / AGPL-3.0 / LGPL obligations are interpreted and enforced under local law; AGPL-3.0 §13 (source disclosure when offering the software as a network service) is enforceable in most jurisdictions that recognise copyright.

        2. Export control and sanctions
        You may not use this product in sanctioned countries/regions, or by sanctioned persons or entities, or for any purpose that violates export-control or sanctions rules.

        3. Availability of third-party services
        Availability, pricing and compliance requirements of AI models, search engines, tunnels and multiplayer relays are decided by each provider and may be unavailable or restricted in your region.
    """.trimIndent()

    private val JURIS_GUIDE_EN = """
        Regional Supplement — {region}

        1. Governing law and venue
        Disputes between you and this product are governed by the laws of {region} and heard by the competent courts where you reside. Mandatory consumer-protection rights under local law are not affected.

        2. Minors
        The age of majority applied here is {age}.{timeWindow}

        3. Language and region detection
        The language of the text follows your system language; the regional terms follow your system country and time zone. Both must be in the supported set to enter the main screen.

        4. If you are blocked by mistake
        The block screen shows the detection basis (country / time zone / language). If you are in a supported region but blocked, attach that line to a GitHub Issue.
    """.trimIndent()
}
