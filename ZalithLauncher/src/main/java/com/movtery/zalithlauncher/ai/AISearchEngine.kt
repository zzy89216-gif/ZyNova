/*
 * ZyNova Launcher — AI 联网搜索使用的引擎
 * GPL-3.0-or-later
 */
package com.movtery.zalithlauncher.ai

import com.movtery.zalithlauncher.R

/**
 * 联网搜索的引擎。
 *
 * - [DUCKDUCKGO]：抓网页版结果，免 Key，默认；
 * - [DEEPSEEK]：调用 DeepSeek 官方 Search API（`POST https://api.deepseek.com/v1/tools/search`），
 *   结果带 summary/content，中文语境更准；按成功请求计费（约 $0.75 / 1000 次）。
 *
 * DeepSeek 失败（无 Key、欠费、限流、超时）时会**静默降级**回 DuckDuckGo，任务不会卡死。
 */
enum class AISearchEngine(val labelRes: Int) {
    DUCKDUCKGO(R.string.ai_search_engine_duckduckgo),
    DEEPSEEK(R.string.ai_search_engine_deepseek);

    companion object {
        fun fromName(name: String?): AISearchEngine =
            entries.firstOrNull { it.name == name } ?: DUCKDUCKGO
    }
}
