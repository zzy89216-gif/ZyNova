/*
 * ZyNova Launcher — 未成年人游玩时段限制：纯 JVM 单测
 * GPL-3.0-or-later
 *
 * 只用 java.util.Calendar，不碰 Android API（不需要 Robolectric）。
 */
package com.movtery.zalithlauncher.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class MinorTimeGateTest {

    /** 固定日期/时刻的 Calendar（月份从 1 开始，方便对照节假日表） */
    private fun calendarOf(year: Int, month: Int, day: Int, hour: Int, minute: Int = 0): Calendar =
        Calendar.getInstance().apply {
            clear()
            set(year, month - 1, day, hour, minute, 0)
        }

    private fun allowedAt(year: Int, month: Int, day: Int, hour: Int, minute: Int = 0): Boolean =
        MinorTimeGate.isAllowedNow(calendarOf(year, month, day, hour, minute))

    // ── 数据年份 ────────────────────────────────────────────────

    @Test
    fun dataYearIs2026AndNotExpired() {
        assertEquals(2026, MinorTimeGateData.DATA_YEAR)
        assertFalse(MinorTimeGate.isDataExpired(calendarOf(2026, 1, 1, 20)))
        assertTrue(MinorTimeGate.isDataExpired(calendarOf(2027, 1, 1, 20)))
    }

    // ── 2026 法定节假日（工作日也开放） ─────────────────────────

    @Test
    fun holidays2026OpenEveningWindowOnWeekdays() {
        // 2/15–2/23 春节：2/18 是周三
        assertTrue(allowedAt(2026, 2, 18, 20, 30))
        // 4/4–4/6 清明：4/6 是周一
        assertTrue(allowedAt(2026, 4, 6, 20, 30))
        // 10/1–10/7 国庆：10/1 是周四
        assertTrue(allowedAt(2026, 10, 1, 20, 30))
        // 元旦 1/1 是周四
        assertTrue(allowedAt(2026, 1, 1, 20, 30))
    }

    @Test
    fun ordinaryWeekday2026IsBlocked() {
        // 3/4 周三，不是节假日
        assertFalse(allowedAt(2026, 3, 4, 20, 30))
    }

    // ── 周五 / 周六 / 周日 ─────────────────────────────────────

    @Test
    fun fridaySaturdaySundayAreOpenDays() {
        assertTrue(allowedAt(2026, 3, 6, 20, 30))   // 周五
        assertTrue(allowedAt(2026, 3, 7, 20, 30))   // 周六
        assertTrue(allowedAt(2026, 3, 8, 20, 30))   // 周日
        assertFalse(allowedAt(2026, 3, 9, 20, 30))  // 周一
    }

    // ── 调休上班日 ─────────────────────────────────────────────

    @Test
    fun makeupWorkdays2026AreBlocked() {
        assertFalse(allowedAt(2026, 1, 4, 20, 30))   // 1/4 周日上班
        assertFalse(allowedAt(2026, 2, 14, 20, 30))  // 2/14 周六上班
        assertFalse(allowedAt(2026, 2, 28, 20, 30))  // 2/28 周六上班
        assertFalse(allowedAt(2026, 5, 9, 20, 30))   // 5/9 周六上班
        assertFalse(allowedAt(2026, 9, 20, 20, 30))  // 9/20 周日上班
        assertFalse(allowedAt(2026, 10, 10, 20, 30)) // 10/10 周六上班
    }

    // ── 时段边界 ───────────────────────────────────────────────

    @Test
    fun timeWindowBoundaries2026() {
        // 周五 3/6：[20:00, 21:00) 才开放
        assertFalse(allowedAt(2026, 3, 6, 19, 59))
        assertTrue(allowedAt(2026, 3, 6, 20, 0))
        assertTrue(allowedAt(2026, 3, 6, 20, 59))
        assertFalse(allowedAt(2026, 3, 6, 21, 0))
        // 节假日同样适用时段边界（春节 2/18 周三）
        assertFalse(allowedAt(2026, 2, 18, 19, 59))
        assertTrue(allowedAt(2026, 2, 18, 20, 0))
        assertTrue(allowedAt(2026, 2, 18, 20, 59))
        assertFalse(allowedAt(2026, 2, 18, 21, 0))
    }

    // ── 跨年 fail-open ─────────────────────────────────────────

    @Test
    fun year2027FailOpenDropsHolidayDayCheck() {
        // 2/17 是周三：旧逻辑（isHoliday 恒 false）会误拦，跨年后应放行
        assertTrue(allowedAt(2027, 2, 17, 20, 30))
        // 元旦 1/1 是周五，本来就开放
        assertTrue(allowedAt(2027, 1, 1, 20, 30))
        // 周六开放
        assertTrue(allowedAt(2027, 2, 20, 20, 30))
    }

    @Test
    fun year2027FailOpenKeepsTimeWindow() {
        // 数据过期后不再用调休/节假日判定，但 20:00–21:00 的时段限制与数据无关，仍然保留
        assertFalse(allowedAt(2027, 2, 17, 19, 59))
        assertTrue(allowedAt(2027, 2, 17, 20, 0))
        assertTrue(allowedAt(2027, 2, 17, 20, 59))
        assertFalse(allowedAt(2027, 2, 17, 21, 0))
    }

    @Test
    fun year2027BlockReasonExplainsExpiredData() {
        val reason = MinorTimeGate.blockReason(calendarOf(2027, 2, 17, 19, 0))
        assertTrue(reason.contains("节假日数据已过期"))
        assertTrue(reason.contains(MinorTimeGate.DATA_EXPIRED_NOTICE))
    }

    // ── 2026 拦截原因文案 ──────────────────────────────────────

    @Test
    fun blockReasonDistinguishesCases2026() {
        assertTrue(MinorTimeGate.blockReason(calendarOf(2026, 2, 14, 20, 30)).contains("调休上班日"))
        assertTrue(MinorTimeGate.blockReason(calendarOf(2026, 3, 4, 20, 30)).contains("今天不是开放日"))
        assertTrue(MinorTimeGate.blockReason(calendarOf(2026, 3, 6, 19, 0)).contains("还没到开放时段"))
        assertTrue(MinorTimeGate.blockReason(calendarOf(2026, 3, 6, 21, 30)).contains("已经结束"))
    }
}
