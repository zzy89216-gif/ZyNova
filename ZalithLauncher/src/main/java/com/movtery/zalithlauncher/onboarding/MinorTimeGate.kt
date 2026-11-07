/*
 * ZyNova Launcher — 未成年人游玩时段限制
 * GPL-3.0-or-later
 */
package com.movtery.zalithlauncher.onboarding

import java.util.Calendar

/**
 * 未成年人游戏时段限制。
 *
 * 依据：国家新闻出版署《关于进一步严格管理切实防止未成年人沉迷网络游戏的通知》
 * （2021-08-30，<https://www.gov.cn/zhengce/zhengceku/2021-09/01/content_5634661.htm>）：
 * **未成年人仅可在周五、周六、周日和法定节假日每日 20:00–21:00 游玩 1 小时**。
 *
 * 法定节假日按国务院办公厅《关于2026年部分节假日安排的通知》
 * （国办发明电〔2025〕7号）逐日列出，**调休上班的周末按上班日处理（不允许游玩）**。
 * 跨年后请同步更新 [HOLIDAYS_2026] 与 [MAKEUP_WORKDAYS_2026]。
 */
object MinorTimeGate {

    private const val START_HOUR = 20
    private const val END_HOUR = 21

    /** 2026 年法定节假日（含放假调休日），格式 MMdd */
    private val HOLIDAYS_2026: Set<Int> = buildSet {
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

    /** 2026 年调休上班的周末（这些日子按工作日算，不允许游玩） */
    private val MAKEUP_WORKDAYS_2026: Set<Int> = setOf(
        104,   // 1/4 周日上班
        214,   // 2/14 周六上班
        228,   // 2/28 周六上班
        509,   // 5/9 周六上班
        920,   // 9/20 周日上班
        1010   // 10/10 周六上班
    )

    private fun key(calendar: Calendar): Int =
        (calendar.get(Calendar.MONTH) + 1) * 100 + calendar.get(Calendar.DAY_OF_MONTH)

    private fun isHoliday(calendar: Calendar): Boolean = key(calendar) in HOLIDAYS_2026

    private fun isMakeupWorkday(calendar: Calendar): Boolean = key(calendar) in MAKEUP_WORKDAYS_2026

    /** 是否处于允许未成年游玩的时刻 */
    fun isAllowedNow(calendar: Calendar = Calendar.getInstance()): Boolean {
        if (isMakeupWorkday(calendar)) return false
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        if (hour !in START_HOUR until END_HOUR) return false
        val day = calendar.get(Calendar.DAY_OF_WEEK)
        val weekendOrFriday = day == Calendar.FRIDAY || day == Calendar.SATURDAY || day == Calendar.SUNDAY
        return weekendOrFriday || isHoliday(calendar)
    }

    /** 当前为什么不能玩（给用户看的一句话） */
    fun blockReason(calendar: Calendar = Calendar.getInstance()): String {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val day = calendar.get(Calendar.DAY_OF_WEEK)
        val weekendOrFriday = day == Calendar.FRIDAY || day == Calendar.SATURDAY || day == Calendar.SUNDAY
        val openDay = (weekendOrFriday || isHoliday(calendar)) && !isMakeupWorkday(calendar)
        return when {
            isMakeupWorkday(calendar) ->
                "今天是调休上班日，未成年人不可游玩（仅限周五、周六、周日和法定节假日的 20:00–21:00）。"
            !openDay ->
                "未成年人仅可在周五、周六、周日和法定节假日的 20:00–21:00 游玩，今天不是开放日。"
            hour < START_HOUR ->
                "未成年人仅可在 20:00–21:00 游玩，现在还没到开放时段。"
            else ->
                "未成年人仅可在 20:00–21:00 游玩，今天的开放时段已经结束。"
        }
    }
}
