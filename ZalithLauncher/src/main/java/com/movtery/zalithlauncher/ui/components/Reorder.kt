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
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

/** 目标卡片「弹起来」的阈值：被压住自身一半以上 */
private const val LIFT_THRESHOLD = 0.5f

/** 弹起时的放大倍数 */
private const val LIFT_SCALE = 1.05f

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

    /**
     * 当前「被拖动项压住过半」的那一项（会弹起来）。
     *
     * 维护者要求：往下拖、压住下一张卡片的一半时，那张卡片要自己弹起来，
     * 让人一眼看出「松手就换到这儿」。所以这里在拖动过程中记录它，
     * 由 [reorderItem] 播放一个轻微放大的弹起动画。
     */
    var liftedKey: Any? by mutableStateOf(null)
        private set

    /** 项的屏幕范围（root 坐标） */
    private val bounds = mutableStateMapOf<Any, Rect>()

    /** 尚未播放的「布局位移」，播放后由 [takeShift] 取走 */
    private val shifts = mutableStateMapOf<Any, Float>()

    /** 数据源重排回调：被拖动项的 key → 目标项的 key */
    var onMove: (Any, Any) -> Unit = { _, _ -> }

    /**
     * 拖动结束回调。
     *
     * ⚠️ 落盘（MMKV / 数据库）必须放在这里，**不能放在 [onMove] 里**：
     * 拖动过程中每帧都可能触发 onMove，每帧写一次存储 + 让整个列表重组，
     * 表现就是「闪屏」和「另一张卡片跟着抖」。
     */
    var onEnd: () -> Unit = {}

    fun isDragging(key: Any): Boolean = draggingKey == key

    /** 取走待播放的位移（只会被取到一次） */
    fun takeShift(key: Any): Float = shifts.remove(key) ?: 0f

    /**
     * 记录某项的屏幕范围
     *
     * 只有在**拖动过程中**才需要处理位置变化：
     * - 它是被拖动项 → 抵消位移，保证卡片继续跟手
     * - 其它项 → 记录位移，由 [reorderItem] 播放成一次平滑的让位动画
     *
     * 没有在拖动时必须直接忽略：页面滚动会让所有项的 root 坐标一起变化，
     * 若继续按「布局位移」处理，每一项都会被记上一笔让位动画，
     * 卡片就会在滚动时自己乱跑（这正是「主页向下滚动时卡片异常移动」的成因）。
     */
    fun onBounds(key: Any, rect: Rect) {
        val old = bounds[key]
        //位置没变就不要写入：否则会在布局回调里反复触发重组
        if (old == rect) return
        bounds[key] = rect
        old ?: return

        val dragging = draggingKey ?: return

        val delta = old.top - rect.top
        if (delta == 0f) return

        if (dragging == key) {
            //拖动过程中被拖项的 root 坐标也会变：小范围是「让位/重排」造成的，需要补偿，
            //否则卡片会从手指下面滑走；大范围通常是页面在滚动，**不能再补偿**，
            //否则会和手势 delta 叠加成双倍，表现就是「往下滑着拖，卡片被甩出去、暂时消失」。
            if (kotlin.math.abs(delta) < 64f) {
                dragOffset += Offset(0f, -delta)
            }
            return
        }
        shifts[key] = (shifts[key] ?: 0f) + delta
        layoutVersion++
    }

    fun start(key: Any) {
        draggingKey = key
        dragOffset = Offset.Zero
    }

    /**
     * 拖动中：更新位移，并按「重叠过半」的规则请求换位。
     *
     * 判定口径从「指针进入目标范围」改成「拖动项压住目标**一半以上**」：
     * - 轻轻碰到就换位会让整页卡片抖个不停，手感很糟；
     * - 压住一半才换位，同时让目标卡片弹起来，用户能确定松手会落到哪。
     */
    fun drag(delta: Offset) {
        val key = draggingKey ?: return
        dragOffset += delta
        //兜底：任何异常累积都不允许把卡片甩到屏幕外
        val maxOffset = 4000f
        dragOffset = Offset(
            dragOffset.x.coerceIn(-maxOffset, maxOffset),
            dragOffset.y.coerceIn(-maxOffset, maxOffset)
        )

        val self = bounds[key] ?: return
        val selfRect = self.translate(dragOffset)

        val lifted = bounds.entries
            .asSequence()
            .filter { it.key != key }
            .map { (other, rect) -> other to overlapRatio(selfRect, rect) }
            .filter { it.second > 0f }
            .maxByOrNull { it.second }
            ?.takeIf { it.second >= LIFT_THRESHOLD }
            ?.first

        liftedKey = lifted

        if (lifted != null) {
            onMove(key, lifted)
        }
    }

    fun end() {
        draggingKey = null
        dragOffset = Offset.Zero
        liftedKey = null
        runCatching { onEnd() }
    }

    /**
     * 两个矩形「压住的比例」：纵向与横向各算一次，取大的那个。
     * 纵向列表看高度，横向列表/网格看宽度，同一套逻辑都能用。
     */
    private fun overlapRatio(a: Rect, b: Rect): Float {
        val overlapHeight = (minOf(a.bottom, b.bottom) - maxOf(a.top, b.top)).coerceAtLeast(0f)
        val overlapWidth = (minOf(a.right, b.right) - maxOf(a.left, b.left)).coerceAtLeast(0f)
        val vertical = if (b.height > 0f) overlapHeight / b.height else 0f
        val horizontal = if (b.width > 0f) overlapWidth / b.width else 0f
        return maxOf(vertical, horizontal)
    }
}

/**
 * 记住一个拖动排序状态
 *
 * @param onMove 被拖动项的 key → 目标项的 key
 */
@Composable
fun rememberReorderState(
    onMove: (Any, Any) -> Unit,
    onEnd: () -> Unit = {}
): ReorderState {
    val state = remember { ReorderState() }
    state.onMove = onMove
    state.onEnd = onEnd
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
        if (pending == 0f) {
            //没有新的位移就确保归零：
            //动画被新的布局变化打断时，Animatable 会停在中间值上，
            //不归零的话卡片会「卡」在错位的位置（内容被顶下去）
            shift.snapTo(0f)
            return@LaunchedEffect
        }
        shift.snapTo(shift.value + pending)
        shift.animateTo(
            targetValue = 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
        //收尾再归零一次，保证任何情况下都不会残留偏移
        shift.snapTo(0f)
    }

    //拖动结束时同样归零
    LaunchedEffect(state.draggingKey) {
        if (state.draggingKey == null) shift.snapTo(0f)
    }

    val dragging = state.isDragging(key)
    val lifted = !dragging && state.liftedKey == key

    //被压住过半时轻轻放大一下：卡片「弹起来」，用户能确定松手会落到哪
    val liftScale by animateFloatAsState(
        targetValue = if (lifted) LIFT_SCALE else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "reorderLift"
    )

    var modifier = this
        .zIndex(if (dragging) 1f else 0f)
        .graphicsLayer {
            scaleX = liftScale
            scaleY = liftScale
        }
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
