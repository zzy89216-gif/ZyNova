/*
 * ZyNova Launcher
 * Copyright (C) 2026 zzy89216-gif and contributors
 *
 * GPL-3.0-or-later
 */

package com.movtery.zalithlauncher.ai

/**
 * 「进入 AI 对话后自动发送的第一句话」。
 *
 * 崩溃自愈（议题 #44）用它把「帮我修复一下崩溃」这句话交给对话页：
 * 主界面检测到异常退出 → 用户点「自动修复」→ 写入这里 → 跳转 AI 页 →
 * AIChatScreen 首次组合时取走并 `send()`，用户看起来就是「AI 已经开始干活了」。
 */
object AIPendingPrompt {
    @Volatile
    private var prompt: String? = null

    fun put(text: String) {
        prompt = text
    }

    /** 取走（一次性），没有则返回 null */
    fun consume(): String? {
        val value = prompt
        prompt = null
        return value
    }
}
