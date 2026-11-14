/*
 * ZyNova Launcher — 未成年人游玩时段：法定节假日 / 调休上班日数据
 * GPL-3.0-or-later
 */
package com.movtery.zalithlauncher.onboarding

/**
 * 未成年人时段限制用到的法定节假日数据（按年更新，单独放一个文件）。
 *
 * 依据国务院办公厅《关于2026年部分节假日安排的通知》
 * （国办发明电〔2025〕7号）逐日列出放假日与调休上班日。
 *
 * ⚠️ 数据只覆盖 [DATA_YEAR] 这一年：
 * - 未跨年前：按 [HOLIDAYS] / [MAKEUP_WORKDAYS] 判定；
 * - **系统年份晚于 [DATA_YEAR]**：[MinorTimeGate] 会 **fail-open**（不再用这份数据判定开放日/调休，
 *   只保留与数据无关的 20:00–21:00 时段限制），避免「数据没更新」被用户误认为「被无故拦住」。
 *
 * 更新方式：把 [DATA_YEAR] 与下面两张表一起替换成新一年的安排即可，不需要动判定逻辑。
 * 这里刻意用纯 `object`（不读 assets、不需要 Context），这样能直接跑 JVM 单测。
 */
object MinorTimeGateData {

    /** 当前数据适用的年份。国务院办公厅通常在上一年底公布下一年安排，公布后更新本常量与两张表。 */
    const val DATA_YEAR: Int = 2026

    /** [DATA_YEAR] 年法定节假日（含放假调休日），格式 MMdd */
    val HOLIDAYS: Set<Int> = buildSet {
        // 元旦：1/1–1/3
        add(101); add(102); add(103)
        // 春节：2/15–2/23
        add(215); add(216); add(217); add(218); add(219); add(220); add(221); add(222); add(223)
        // 清明：4/4–4/6
        add(404); add(405); add(406)
        // 劳动节：5/1–5/5
        (1..5).forEach { add(500 + it) }
        // 端午：6/19–6/21
        add(619); add(620); add(621)
        // 中秋：9/25–9/27
        add(925); add(926); add(927)
        // 国庆：10/1–10/7
        (1..7).forEach { add(1000 + it) }
    }

    /** [DATA_YEAR] 年调休上班的周末（这些日子按工作日算，不允许游玩），格式 MMdd */
    val MAKEUP_WORKDAYS: Set<Int> = setOf(
        104,   // 1/4 周日上班
        214,   // 2/14 周六上班
        228,   // 2/28 周六上班
        509,   // 5/9 周六上班
        920,   // 9/20 周日上班
        1010   // 10/10 周六上班
    )
}
