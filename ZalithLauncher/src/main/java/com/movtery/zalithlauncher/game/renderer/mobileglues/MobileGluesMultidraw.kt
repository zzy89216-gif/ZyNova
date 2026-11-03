/*
 * ZyNova Launcher
 * Copyright (C) 2026 zzy89216-gif and contributors
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

package com.movtery.zalithlauncher.game.renderer.mobileglues

/**
 * MobileGlues 2.0.1 的 multidraw 实现策略。
 *
 * native 按 `multidrawOrder` 的**逗号分隔顺序**依次尝试实现；
 * 某个函数（glMultiDrawArrays / glMultiDrawElements / ...）可以单独覆盖顺序。
 * 这些键在 MobileGlues 2.0.0 已存在，2.0.1 起才在启动器侧完整暴露。
 */
enum class MultidrawAlgorithm(
    /** 写进 config.json 的值 */
    val wire: String,
    /** 界面展示名（与上游保持一致的英文算法名） */
    val displayName: String,
) {
    NATIVE("native", "Native"),
    MULTI_INDIRECT("multiindirect", "MultiIndirect"),
    MULTI_BASE_VERTEX("multibasevertex", "MultiBaseVertex"),
    MULTI_ARRAYS("multiarrays", "MultiArrays"),
    INDIRECT("indirect", "Indirect"),
    BASE_VERTEX("basevertex", "BaseVertex"),
    UNROLL("unroll", "Unroll"),
    COMPUTE("compute", "Compute");

    companion object {
        fun ofWire(wire: String): MultidrawAlgorithm? =
            entries.firstOrNull { it.wire == wire }
    }
}

/**
 * 一个可按函数覆盖的 multidraw 目标。
 *
 * @param key config.json 的键名
 * @param nativeValue 当全局顺序里出现 `native` 时，这个函数用它替换
 * @param options 这个函数支持的策略
 * @param defaultOrder 默认顺序（与上游 MobileGlues 一致）
 */
data class MultidrawOverride(
    val key: String,
    val title: String,
    val nativeValue: MultidrawAlgorithm,
    val options: List<MultidrawAlgorithm>,
    val defaultOrder: List<MultidrawAlgorithm>,
)

object MobileGluesMultidraw {
    /** 全局默认顺序（与上游 MobileGlues 2.0.1 一致） */
    val DEFAULT_ORDER: List<MultidrawAlgorithm> = listOf(
        MultidrawAlgorithm.NATIVE,
        MultidrawAlgorithm.MULTI_INDIRECT,
        MultidrawAlgorithm.MULTI_BASE_VERTEX,
        MultidrawAlgorithm.MULTI_ARRAYS,
        MultidrawAlgorithm.INDIRECT,
        MultidrawAlgorithm.BASE_VERTEX,
        MultidrawAlgorithm.UNROLL,
        MultidrawAlgorithm.COMPUTE,
    )

    val OVERRIDES: List<MultidrawOverride> = listOf(
        MultidrawOverride(
            key = "multidrawOrderArrays",
            title = "glMultiDrawArrays",
            nativeValue = MultidrawAlgorithm.MULTI_ARRAYS,
            options = listOf(
                MultidrawAlgorithm.MULTI_ARRAYS,
                MultidrawAlgorithm.MULTI_INDIRECT,
                MultidrawAlgorithm.UNROLL,
            ),
            defaultOrder = listOf(
                MultidrawAlgorithm.MULTI_ARRAYS,
                MultidrawAlgorithm.MULTI_INDIRECT,
                MultidrawAlgorithm.UNROLL,
            )
        ),
        MultidrawOverride(
            key = "multidrawOrderElements",
            title = "glMultiDrawElements",
            nativeValue = MultidrawAlgorithm.MULTI_ARRAYS,
            options = listOf(
                MultidrawAlgorithm.MULTI_ARRAYS,
                MultidrawAlgorithm.MULTI_INDIRECT,
                MultidrawAlgorithm.MULTI_BASE_VERTEX,
                MultidrawAlgorithm.INDIRECT,
                MultidrawAlgorithm.UNROLL,
            ),
            defaultOrder = listOf(
                MultidrawAlgorithm.MULTI_ARRAYS,
                MultidrawAlgorithm.MULTI_INDIRECT,
                MultidrawAlgorithm.MULTI_BASE_VERTEX,
                MultidrawAlgorithm.INDIRECT,
                MultidrawAlgorithm.UNROLL,
            )
        ),
        MultidrawOverride(
            key = "multidrawOrderElementsBaseVertex",
            title = "glMultiDrawElementsBaseVertex",
            nativeValue = MultidrawAlgorithm.MULTI_BASE_VERTEX,
            options = listOf(
                MultidrawAlgorithm.MULTI_BASE_VERTEX,
                MultidrawAlgorithm.MULTI_INDIRECT,
                MultidrawAlgorithm.INDIRECT,
                MultidrawAlgorithm.BASE_VERTEX,
                MultidrawAlgorithm.UNROLL,
                MultidrawAlgorithm.COMPUTE,
            ),
            defaultOrder = listOf(
                MultidrawAlgorithm.MULTI_BASE_VERTEX,
                MultidrawAlgorithm.MULTI_INDIRECT,
                MultidrawAlgorithm.INDIRECT,
                MultidrawAlgorithm.BASE_VERTEX,
                MultidrawAlgorithm.UNROLL,
                MultidrawAlgorithm.COMPUTE,
            )
        ),
        MultidrawOverride(
            key = "multidrawOrderArraysIndirect",
            title = "glMultiDrawArraysIndirect",
            nativeValue = MultidrawAlgorithm.MULTI_INDIRECT,
            options = listOf(
                MultidrawAlgorithm.MULTI_INDIRECT,
                MultidrawAlgorithm.INDIRECT,
            ),
            defaultOrder = listOf(
                MultidrawAlgorithm.MULTI_INDIRECT,
                MultidrawAlgorithm.INDIRECT,
            )
        ),
        MultidrawOverride(
            key = "multidrawOrderElementsIndirect",
            title = "glMultiDrawElementsIndirect",
            nativeValue = MultidrawAlgorithm.MULTI_INDIRECT,
            options = listOf(
                MultidrawAlgorithm.MULTI_INDIRECT,
                MultidrawAlgorithm.INDIRECT,
            ),
            defaultOrder = listOf(
                MultidrawAlgorithm.MULTI_INDIRECT,
                MultidrawAlgorithm.INDIRECT,
            )
        ),
    )

    /** 默认全局顺序的存储值 */
    val DEFAULT_ORDER_VALUE: String = serialize(DEFAULT_ORDER)

    fun serialize(order: List<MultidrawAlgorithm>): String =
        order.joinToString(",") { it.wire }

    /**
     * 解析存储值：只保留 [options] 里的合法项，去重；
     * 缺失项按 [fallback] 补齐（与上游 `coerce` 的行为一致）。
     */
    fun parse(
        raw: String?,
        options: List<MultidrawAlgorithm> = MultidrawAlgorithm.entries,
        fallback: List<MultidrawAlgorithm> = DEFAULT_ORDER,
    ): List<MultidrawAlgorithm> {
        val values = raw
            ?.split(',')
            ?.mapNotNull { MultidrawAlgorithm.ofWire(it.trim()) }
            ?.filter { it in options }
            .orEmpty()
        val defaults = fallback.filter { it in options }
        return (values + defaults).distinct()
    }

    /**
     * 按全局顺序计算某个函数的默认覆盖顺序：
     * 全局里的 `native` 替换成该函数的 nativeValue，再过滤掉它不支持的策略。
     */
    fun inheritOrder(
        globalOrder: String?,
        override: MultidrawOverride,
    ): List<MultidrawAlgorithm> {
        val expanded = parse(globalOrder).map {
            if (it == MultidrawAlgorithm.NATIVE) override.nativeValue else it
        }
        return (expanded + override.options)
            .filter { it in override.options }
            .distinct()
    }
}
