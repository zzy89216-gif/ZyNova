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
 * 兼容嵌套NavDisplay的返回事件处理
 */
fun <E: TitledNavKey> onBack(currentBackStack: NavBackStack<E>) {
    when (val key = currentBackStack.lastOrNull()) {
        //普通的屏幕，直接退出当前堆栈的上层
        is NormalNavKey -> currentBackStack.removeLastOrNull()
        is BackStackNavKey<*> -> {
            if (key.backStack.size <= 1) {
                //嵌套屏幕的堆栈处于最后一个屏幕的状态
                //可以退出当前堆栈的上层了
                currentBackStack.removeLastOrNull()
            } else {
                //退出子堆栈的上层屏幕
                key.backStack.removeLastOrNull()
            }
        }
    }
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