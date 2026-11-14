/*
 * ZyNova Launcher — 地区可用性判定
 * GPL-3.0-or-later
 *
 * 开放范围 = [LegalRegions] 里那 59 个国家/地区（与「已提供语言」对齐）。
 * 判定依据（两者任一命中即放行）：
 *   1. 系统国家/地区代码（Locale country）
 *   2. 系统时区 → 反查国家（用系统时区库，不写死大前缀）
 *
 * 结论分三态（见 [RegionDecision]）：
 *   1. 命中开放地区 → 放行；
 *   2. 能确定国家但不在 [LegalRegions.ALL] → **阻断**（维持原设计）；
 *   3. 判定失败（国家码为空 + 时区反查失败/异常）→ **默认放行** + 一条 WARN 日志（便于排查）。
 *   第 3 种以前也进阻断页，会把「系统语言/时区没配好」的用户直接挡在门外，属于误拦，已改为 fail-open。
 *
 * 说明：这是**尽力而为**的判定，可被用户改时区绕过；真正的地区限制应同时依赖商店分发布局。
 * 这里的目的只是「不给未覆盖地区的用户误用」，不追求对抗性。
 */
package com.movtery.zalithlauncher.onboarding

import com.movtery.zalithlauncher.utils.logging.Logger
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 地区判定的三态结论。
 *
 * 只有「能确定国家、但该国不在开放列表」才是真正的阻断；「判定失败」按 fail-open 放行。
 */
sealed interface RegionDecision {

    /** 是否放行：只有 [Unsupported] 为 false */
    val isAllowed: Boolean

    /** 命中开放地区 */
    data class Covered(
        val region: LegalRegion,
        /** 命中来源：true = 时区反查命中，false = 系统国家码命中 */
        val matchedByTimeZone: Boolean,
    ) : RegionDecision {
        override val isAllowed: Boolean get() = true
    }

    /** 能确定国家，但该国/地区不在 [LegalRegions.ALL] 里 → 阻断（维持原设计） */
    data class Unsupported(
        val country: String,
        val timeZoneCountry: String?,
    ) : RegionDecision {
        override val isAllowed: Boolean get() = false
    }

    /** 判定失败：国家码为空且时区反查失败/异常 → fail-open 放行，避免误拦 */
    data class Undetermined(
        val country: String,
        val timeZone: String,
        /** 时区反查抛出的异常摘要；没有异常（反查只是没结果）时为 null */
        val error: String?,
    ) : RegionDecision {
        override val isAllowed: Boolean get() = true
    }
}

object RegionGate {

    /** ICU 表示「未知地区」的代码 */
    private const val UNKNOWN_REGION_CODE = "001"

    /** 判定失败的 WARN 只记一次，避免 Compose 重组合/多次调用时刷屏 */
    private val undeterminedWarned = AtomicBoolean(false)

    private fun country(): String = Locale.getDefault().country.uppercase(Locale.ROOT)

    private fun timeZoneId(): String = TimeZone.getDefault().id

    /**
     * 当前地区的三态判定结论。
     *
     * 判定顺序：系统国家码 → 时区反查国家 → 两者都拿不到才算「判定失败」。
     * 这一步不写日志，日志只在 [RegionDecision.Undetermined] 时由内部记一次。
     */
    fun decision(): RegionDecision {
        val countryCode = country()

        // 1. 系统国家码命中开放地区 → 放行
        LegalRegions.byCountry(countryCode)?.let {
            return RegionDecision.Covered(region = it, matchedByTimeZone = false)
        }

        // 2. 系统时区 → 国家反查；能反查出国家就按「确定国家」处理
        val zoneId = timeZoneId()
        val zoneResult = runCatching { countryOfTimeZone(zoneId) }
        val zoneCountry = zoneResult.getOrNull()
        if (zoneCountry != null) {
            LegalRegions.byCountry(zoneCountry)?.let {
                return RegionDecision.Covered(region = it, matchedByTimeZone = true)
            }
            // 时区能反查出国家，但不在开放列表 → 阻断
            return RegionDecision.Unsupported(country = countryCode, timeZoneCountry = zoneCountry)
        }

        // 3. 时区反查失败：国家码非空仍然算「能确定国家，只是不在开放列表」→ 阻断
        if (countryCode.isNotBlank()) {
            return RegionDecision.Unsupported(country = countryCode, timeZoneCountry = null)
        }

        // 4. 判定失败（国家码为空 + 时区反查失败/异常）→ fail-open 放行，并记一条 WARN 便于排查
        val undetermined = RegionDecision.Undetermined(
            country = countryCode,
            timeZone = zoneId,
            error = zoneResult.exceptionOrNull()?.let { "${it.javaClass.simpleName}: ${it.message}" },
        )
        warnUndetermined(undetermined)
        return undetermined
    }

    private fun warnUndetermined(decision: RegionDecision.Undetermined) {
        if (!undeterminedWarned.compareAndSet(false, true)) return
        Logger.warning(
            "RegionGate",
            "地区判定失败，已按 fail-open 放行：国家码=\"${decision.country}\"，时区=\"${decision.timeZone}\"，" +
                "反查异常=${decision.error ?: "无"}。请检查系统语言 / 时区设置。"
        )
    }

    /**
     * 当前设备命中的开放地区；不在开放范围内（含判定失败）返回 null。
     *
     * ⚠️ 以前这里维护了一张手写时区白名单，只有中美韩日俄。
     * 开放范围扩到 59 个地区后，手写时区表既容易漏（德国只有 Europe/Berlin，
     * 忘了 Europe/Busingen 就会误拦），也没法维护。
     * 现在改为「时区 → 国家」反查，判断逻辑收敛到一个地方。
     */
    fun region(): LegalRegion? = (decision() as? RegionDecision.Covered)?.region

    /** 是否放行：命中开放地区或判定失败都放行，只有「确定国家但不在开放列表」才阻断 */
    fun isAllowed(): Boolean = decision().isAllowed

    /**
     * 用系统时区库反查时区所属国家（ICU）。
     * 失败、未知时区、或返回 `001`（未知地区）时返回 null，交给 [decision] 兜底。
     */
    private fun countryOfTimeZone(zoneId: String): String? {
        val code: String = android.icu.util.TimeZone.getRegion(zoneId) ?: return null
        if (code.isBlank() || code == UNKNOWN_REGION_CODE) return null
        return code.uppercase(Locale.ROOT)
    }

    /** 给用户看的地区描述（用于阻断页与日志） */
    fun describe(): String {
        val c = country().ifBlank { "??" }
        return "$c / ${timeZoneId()} / ${Locale.getDefault().toLanguageTag()}"
    }

    /** 系统语言 + 时区签名：只做记录，用于下次展示时选文本，不再是「重新同意」的触发条件 */
    fun signature(): String = "${Locale.getDefault().toLanguageTag()}|${TimeZone.getDefault().id}"
}
