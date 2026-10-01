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

package com.movtery.zalithlauncher.ai

import com.movtery.zalithlauncher.ai.model.AIModelInfo
import com.movtery.zalithlauncher.ai.provider.AIProviders
import com.movtery.zalithlauncher.ai.provider.AIProviderType

/**
 * 模型列表的统一入口。
 *
 * 把「用当前配置去问 Provider 要模型列表」这件事收敛到一处，
 * 避免聊天界面与配置界面各写一遍（Key / Base URL 的拼装很容易写漏）。
 *
 * ⚠️ 模型一律**动态获取**，这里不包含任何硬编码的模型名。
 */
object AIModelRepository {

    /**
     * 按当前保存的配置拉取模型列表。
     *
     * @throws IllegalArgumentException Key 未填时抛出，消息可直接展示给用户
     * @throws Exception 网络 / 鉴权失败时抛出（消息已是可读文本）
     */
    suspend fun fetch(type: AIProviderType = AISettings.provider.getValue()): List<AIModelInfo> {
        val key = AISettings.getKey(type)
        require(key.isNotBlank()) { "请先填写 ${type.displayName} 的 API Key。" }
        return AIProviders.get(type).listModels(key, AISettings.getBaseUrl(type))
    }

    /**
     * 拉取并确保有一个可用模型被选中。
     *
     * 列表为空或原来选的模型已不在列表里时，自动选中第一个。
     *
     * @return 拉取到的模型列表
     */
    suspend fun fetchAndEnsureSelection(
        type: AIProviderType = AISettings.provider.getValue()
    ): List<AIModelInfo> {
        val list = fetch(type)
        if (list.isNotEmpty()) {
            val current = AISettings.model.getValue()
            if (current.isBlank() || list.none { it.id == current }) {
                AISettings.model.save(list.first().id)
            }
        }
        return list
    }
}
