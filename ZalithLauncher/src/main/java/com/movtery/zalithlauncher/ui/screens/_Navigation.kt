/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
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

package com.movtery.zalithlauncher.ui.screens

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.scene.Scene
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.animation.BounceEasing
import com.movtery.zalithlauncher.utils.animation.JellyBounce
import com.movtery.zalithlauncher.utils.animation.TransitionAnimationType
import com.movtery.zalithlauncher.utils.animation.getAnimateSpeed
import kotlin.reflect.KClass

/**
 * 兼容嵌套 NavDisplay 的返回事件处理：**一次只退一层**。
 *
 * 规则（与用户直觉一致，也是这次修复的核心）：
 * 1. 栈顶是嵌套屏幕、并且它自己的内部栈还有上一层 → 只弹内部栈；
 * 2. 否则外层栈还有上一层 → 弹外层栈；
 * 3. 什么都没有 → 不做事并返回 `false`，交给系统返回（退到桌面）。
 *
 * ⚠️ 绝对不能把栈弹空：`NavDisplay` 看到空栈会没有内容可画，
 * 表现就是「点了返回没反应 / 白屏 / 直接被踢回主界面还得重启启动器」。
 *
 * @return 是否消费了这次返回
 */
fun <E: TitledNavKey> onBack(owner: BackStackNavKey<E>): Boolean {
    val stack = owner.backStack
    val top = stack.lastOrNull() ?: return false

    if (top is BackStackNavKey<*>) {
        if (top.backStack.size > 1) {
            //先退嵌套屏幕自己的栈顶，外层不动
            top.backStack.removeLastOrNull()
            @Suppress("UNCHECKED_CAST")
            (top as BackStackNavKey<TitledNavKey>).currentKey = top.backStack.lastOrNull()
            return true
        }
    }

    if (stack.size <= 1) return false

    stack.removeLastOrNull()
    //同步外层 currentKey：NavBackStack 是普通 ArrayList，弹栈不会自动触发重组
    owner.currentKey = stack.lastOrNull()
    return true
}

/**
 * 当前是否还有可以返回的一层。
 *
 * 用于注册系统返回键（[onBack] 的 `enabled`）：没有上一层时必须**让出**返回键，
 * 否则用户按返回永远退不出启动器。
 */
fun <E: TitledNavKey> canGoBack(owner: BackStackNavKey<E>): Boolean {
    val stack = owner.backStack
    val top = stack.lastOrNull() ?: return false
    if (top is BackStackNavKey<*> && top.backStack.size > 1) return true
    return stack.size > 1
}

fun <E: TitledNavKey> NavBackStack<E>.navigateOnce(key: E) {
    if (key == lastOrNull()) return //防止反复加载
    clearWith(key)
}

fun <E: TitledNavKey> NavBackStack<E>.navigateTo(screenKey: E, useClassEquality: Boolean = false) {
    val current = lastOrNull()
    if (useClassEquality) {
        if (current != null && screenKey::class == current::class) return //防止反复加载
    } else {
        if (screenKey == current) return //防止反复加载
    }
    add(screenKey)
}

fun <E: TitledNavKey> NavBackStack<E>.removeAndNavigateTo(remove: KClass<*>, screenKey: E, useClassEquality: Boolean = false) {
    removeIf { key ->
        key::class == remove
    }
    navigateTo(screenKey, useClassEquality)
}

fun <E: TitledNavKey> NavBackStack<E>.removeAndNavigateTo(removes: List<KClass<*>>, screenKey: E, useClassEquality: Boolean = false) {
    removeIf { key ->
        key::class in removes
    }
    navigateTo(screenKey, useClassEquality)
}

/**
 * 清除所有栈，并加入指定的key
 */
fun <E: TitledNavKey> NavBackStack<E>.clearWith(navKey: E) {
    val targetClass = navKey::class.java
    if (none { it::class.java == targetClass }) {
        //提前加入，避免让 Nav3 看到空帧
        add(navKey)
    }
    removeIf { it::class.java != targetClass }
}

/**
 * 清除指定的key
 */
fun <E: TitledNavKey> NavBackStack<E>.clearKeys(vararg navKeys: E) {
    val classes = navKeys.map { it::class.java }
    removeIf { it::class.java in classes }
}

fun <E: TitledNavKey> NavBackStack<E>.addIfEmpty(navKey: E) {
    if (isEmpty()) {
        add(navKey)
    }
}

@Composable
fun rememberSwapTween(): FiniteAnimationSpec<Float> {
    val speed = AllSettings.launcherAnimateSpeed.state
    return remember(speed) {
        tween(durationMillis = (getAnimateSpeed() / 5) * 2)
    }
}

@Composable
fun <T : Any> rememberTransitionSpec(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform {
    val type = AllSettings.launcherSwapAnimateType.state
    val speed = AllSettings.launcherAnimateSpeed.state
    val reduceMotion = AllSettings.launcherReduceMotion.state
    return remember(type, speed, reduceMotion) {
        //与旧实现保持一致的时间换算：默认倍速下约 330ms
        val durationMillis = ((getAnimateSpeed() / 5) * 2).coerceAtLeast(1)

        //⚠️ 注意这里的写法：每个分支都必须是「以 lambda 开头」的块，
        //不要在 lambda 前面写 `val spec = tween(...)` 之类的调用 ——
        //Kotlin 会把紧跟其后的 `{ ... }` 解析成那个调用的**尾随 lambda**，
        //lambda 里的局部变量就会解析不到（曾因此导致编译失败）。
        //所以动画参数统一在 lambda 内部构造（构造 tween 只是创建一个数据类，开销可忽略）。
        val transform: AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform =
            if (reduceMotion || type == TransitionAnimationType.CLOSE) {
                //关闭页面切换动画（手动选择「关闭」档，或开启了「减少动态效果」）
                { ContentTransform(EnterTransition.None, ExitTransition.None) }
            } else when (type) {
                //切入：新页面自右侧滑入，旧页面向左滑出
                TransitionAnimationType.SLICE_IN -> {
                    {
                        ContentTransform(
                            fadeIn(animationSpec = tween(durationMillis)) +
                                    slideInHorizontally(
                                        animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
                                    ) { width -> width / 4 },
                            fadeOut(animationSpec = tween(durationMillis)) +
                                    slideOutHorizontally(
                                        animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
                                    ) { width -> -width / 4 }
                        )
                    }
                }

                //弹跳：Bounce 缓动的缩放入场
                TransitionAnimationType.BOUNCE -> {
                    {
                        ContentTransform(
                            fadeIn(animationSpec = tween(durationMillis)) +
                                    scaleIn(
                                        initialScale = 0.85f,
                                        animationSpec = tween(durationMillis, easing = BounceEasing)
                                    ),
                            fadeOut(animationSpec = tween(durationMillis)) +
                                    scaleOut(
                                        targetScale = 0.95f,
                                        animationSpec = tween(durationMillis)
                                    )
                        )
                    }
                }

                //JELLY_BOUNCE（默认）：带轻微回弹的缩放入场
                else -> {
                    {
                        ContentTransform(
                            fadeIn(animationSpec = tween(durationMillis)) +
                                    scaleIn(
                                        initialScale = 0.9f,
                                        animationSpec = tween(durationMillis, easing = JellyBounce)
                                    ),
                            fadeOut(animationSpec = tween(durationMillis)) +
                                    scaleOut(
                                        targetScale = 0.97f,
                                        animationSpec = tween(durationMillis)
                                    )
                        )
                    }
                }
            }
        transform
    }
}