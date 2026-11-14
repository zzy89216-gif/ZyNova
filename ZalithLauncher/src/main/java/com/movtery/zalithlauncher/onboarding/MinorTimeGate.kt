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
 * 法定节假日逐日数据放在 [MinorTimeGateData]（带年份标识，当前是 2026 年），
 * **调休上班的周末按上班日处理（不允许游玩）**。
 *
 * 跨年 fail-open：数据只覆盖 [MinorTimeGateData.DATA_YEAR] 一年，系统年份晚于该年时不再用
 * 节假日/调休数据判定开放日（数据缺席不能变成拦人理由），只保留与数据无关的
 * 20:00–21:00 时段限制，并在 [blockReason] 里给出「数据已过期」的说明。
 * 更新数据只需要改 [MinorTimeGateData]，判定逻辑不用动。
 */
object MinorTimeGate {

    private const val START_HOUR = 20
    private const val END_HOUR = 21

    /** 节假日数据过期时给用户看的说明（中文硬编码：时段限制只在中国启用） */
    const val DATA_EXPIRED_NOTICE: String =
        "未成年人节假日数据已过期，已按普通周末规则放行（仅保留每日 20:00–21:00 时段限制）。"

    private fun key(calendar: Calendar): Int =
        (calendar.get(Calendar.MONTH) + 1) * 100 + calendar.get(Calendar.DAY_OF_MONTH)

    /** 节假日数据是否已过期：系统年份晚于数据年份 */
    fun isDataExpired(calendar: Calendar = Calendar.getInstance()): Boolean =
        calendar.get(Calendar.YEAR) > MinorTimeGateData.DATA_YEAR

    private fun isHoliday(calendar: Calendar): Boolean = key(calendar) in MinorTimeGateData.HOLIDAYS

    private fun isMakeupWorkday(calendar: Calendar): Boolean = key(calendar) in MinorTimeGateData.MAKEUP_WORKDAYS

    private fun inTimeWindow(calendar: Calendar): Boolean {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        return hour in START_HOUR until END_HOUR
    }

    private fun isOpenDay(calendar: Calendar): Boolean {
        val day = calendar.get(Calendar.DAY_OF_WEEK)
        val weekendOrFriday = day == Calendar.FRIDAY || day == Calendar.SATURDAY || day == Calendar.SUNDAY
        return weekendOrFriday || isHoliday(calendar)
    }

    /** 是否处于允许未成年游玩的时刻 */
    fun isAllowedNow(calendar: Calendar = Calendar.getInstance()): Boolean {
        //数据过期：fail-open —— 节假日/调休无法判定时不能反过来拦住用户，只保留时段限制
        if (isDataExpired(calendar)) return inTimeWindow(calendar)
        if (isMakeupWorkday(calendar)) return false
        if (!inTimeWindow(calendar)) return false
        return isOpenDay(calendar)
    }

    /** 当前为什么不能玩（给用户看的一句话） */
    fun blockReason(calendar: Calendar = Calendar.getInstance()): String {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        //数据过期：说明数据已过期（fail-open 后只有时段限制会拦人）
        if (isDataExpired(calendar)) {
            if (!inTimeWindow(calendar)) {
                val timeReason = if (hour < START_HOUR) {
                    "未成年人仅可在 20:00–21:00 游玩，现在还没到开放时段。"
                } else {
                    "未成年人仅可在 20:00–21:00 游玩，今天的开放时段已经结束。"
                }
                return DATA_EXPIRED_NOTICE + timeReason
            }
            //时段内已放行，正常不会走到这里；保留可读文案兜底
            return DATA_EXPIRED_NOTICE
        }
        val openDay = isOpenDay(calendar) && !isMakeupWorkday(calendar)
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
