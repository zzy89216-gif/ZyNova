/*
 * ZyNova Launcher
 * Copyright (C) 2025 ZyNova Contributors
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

package com.movtery.zalithlauncher.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

/**
 * 拖动排序状态
 *
 * 用法：
 * 1. 用一个稳定的 **key** 标记每一项（不能是下标，否则重排后状态会串）；
 * 2. 容器里给每一项加上 [Modifier.reorderItem]；
 * 3. 在 [rememberReorderState] 的回调里按「被拖动项的 key → 目标项的 key」重排数据源。
 *
 * 动画沿用启动器默认的弹性手感（与点击反馈同一套 spring 参数），
 * 不额外引入花哨的自定义动画。
 *
 * 注意：这里用「项的屏幕范围 + 指针位置」判断落点，
 * 因此对纵向列表、横向列表、FlowRow 都能工作，不依赖 LazyColumn。
 */
@Stable
class ReorderState {
    /** 当前被拖动的项；null 表示没有在拖 */
    var draggingKey: Any? by mutableStateOf(null)
        private set

    /** 拖动位移（相对该项当前布局位置） */
    var dragOffset: Offset by mutableStateOf(Offset.Zero)
        private set

    /** 每次布局变化自增，用于触发各项的补间动画 */
    var layoutVersion: Int by mutableIntStateOf(0)
        private set

    /** 项的屏幕范围（root 坐标） */
    private val bounds = mutableStateMapOf<Any, Rect>()

    /** 尚未播放的「布局位移」，播放后由 [takeShift] 取走 */
    private val shifts = mutableStateMapOf<Any, Float>()

    /** 数据源重排回调：被拖动项的 key → 目标项的 key */
    var onMove: (Any, Any) -> Unit = { _, _ -> }

    fun isDragging(key: Any): Boolean = draggingKey == key

    /** 取走待播放的位移（只会被取到一次） */
    fun takeShift(key: Any): Float = shifts.remove(key) ?: 0f

    /**
     * 记录某项的屏幕范围
     *
     * 如果该项的位置相比上一次布局发生了变化：
     * - 它是被拖动项 → 抵消位移，保证卡片继续跟手
     * - 其它项 → 记录位移，由 [reorderItem] 播放成一次平滑的让位动画
     */
    fun onBounds(key: Any, rect: Rect) {
        val old = bounds[key]
        //位置没变就不要写入：否则会在布局回调里反复触发重组
        if (old == rect) return
        bounds[key] = rect
        old ?: return

        val delta = old.top - rect.top
        if (delta == 0f) return

        if (draggingKey == key) {
            dragOffset += Offset(0f, -delta)
        } else {
            shifts[key] = (shifts[key] ?: 0f) + delta
            layoutVersion++
        }
    }

    fun start(key: Any) {
        draggingKey = key
        dragOffset = Offset.Zero
    }

    /** 拖动中：更新位移，并在指针进入其它项的范围时请求换位 */
    fun drag(delta: Offset) {
        val key = draggingKey ?: return
        dragOffset += delta

        val self = bounds[key] ?: return
        val pointer = self.center + dragOffset
        val target = bounds.entries.firstOrNull { (other, rect) ->
            other != key && rect.contains(pointer)
        }?.key ?: return

        onMove(key, target)
    }

    fun end() {
        draggingKey = null
        dragOffset = Offset.Zero
    }
}

/**
 * 记住一个拖动排序状态
 *
 * @param onMove 被拖动项的 key → 目标项的 key
 */
@Composable
fun rememberReorderState(onMove: (Any, Any) -> Unit): ReorderState {
    val state = remember { ReorderState() }
    state.onMove = onMove
    return state
}

/**
 * 让一项支持「长按拖动排序」
 *
 * - 长按后拖动：该项跟随手指，其它项平滑让位
 * - 短按：不影响原有的点击/按压反馈
 *
 * @param key 该项的稳定标识
 * @param state 由 [rememberReorderState] 创建
 * @param enabled 是否允许拖动
 */
@Composable
fun Modifier.reorderItem(
    key: Any,
    state: ReorderState,
    enabled: Boolean = true
): Modifier {
    //让位动画：布局变化时先瞬时位移到旧位置，再弹回新位置
    val shift = remember { Animatable(0f) }
    val version = state.layoutVersion

    LaunchedEffect(version, key) {
        val pending = state.takeShift(key)
        if (pending != 0f) {
            shift.snapTo(pending)
            shift.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    val dragging = state.isDragging(key)

    var modifier = this
        .zIndex(if (dragging) 1f else 0f)
        .onGloballyPositioned { state.onBounds(key, it.boundsInRoot()) }

    if (enabled) {
        modifier = modifier.pointerInput(key) {
            detectDragGesturesAfterLongPress(
                onDragStart = { state.start(key) },
                onDrag = { change, delta ->
                    change.consume()
                    state.drag(delta)
                },
                onDragEnd = { state.end() },
                onDragCancel = { state.end() }
            )
        }
    }

    return modifier.offset {
        val y = shift.value + if (dragging) state.dragOffset.y else 0f
        IntOffset(0, y.roundToInt())
    }
}
