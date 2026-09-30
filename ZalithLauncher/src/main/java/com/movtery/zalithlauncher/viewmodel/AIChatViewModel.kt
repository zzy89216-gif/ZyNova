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

package com.movtery.zalithlauncher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movtery.zalithlauncher.ai.AIPermissionMode
import com.movtery.zalithlauncher.ai.AISettings
import com.movtery.zalithlauncher.ai.agent.AIAgent
import com.movtery.zalithlauncher.ai.agent.AIToolContext
import com.movtery.zalithlauncher.ai.agent.AIToolRegistry
import com.movtery.zalithlauncher.ai.agent.AgentEvent
import com.movtery.zalithlauncher.ai.model.AIMessage
import com.movtery.zalithlauncher.ai.model.AIModelInfo
import com.movtery.zalithlauncher.ai.model.AIRole
import com.movtery.zalithlauncher.ai.model.AIToolResult
import com.movtery.zalithlauncher.ai.provider.AIProviders
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AI 聊天 + Agent 的 ViewModel。
 *
 * 聊天与 Agent 是**同一个入口**：用户说「为什么进不去」，它会读日志；
 * 用户接着说「帮我修」，它会直接调用 ZyNova 工具去改，而不是再让用户手动操作。
 */
@HiltViewModel
class AIChatViewModel @Inject constructor() : ViewModel() {

    private val _messages = MutableStateFlow<List<AIMessage>>(emptyList())
    val messages: StateFlow<List<AIMessage>> = _messages.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    /** 会话级错误提示（配置不全、网络失败等）*/
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ── 模型列表 ──────────────────────────────────────────────────
    private val _models = MutableStateFlow<List<AIModelInfo>>(emptyList())
    val models: StateFlow<List<AIModelInfo>> = _models.asStateFlow()

    private val _loadingModels = MutableStateFlow(false)
    val loadingModels: StateFlow<Boolean> = _loadingModels.asStateFlow()

    private val _modelError = MutableStateFlow<String?>(null)
    val modelError: StateFlow<String?> = _modelError.asStateFlow()

    // ── 「操作确认」模式下的待确认请求 ────────────────────────────
    private val _pendingConfirm = MutableStateFlow<PendingConfirm?>(null)
    val pendingConfirm: StateFlow<PendingConfirm?> = _pendingConfirm.asStateFlow()

    private var runJob: Job? = null
    private var confirmDeferred: CompletableDeferred<Boolean>? = null

    /**
     * 真正启动游戏。
     * 由界面注入（走启动器现有的启动事件链路），
     * 保证「AI 启动」与「用户点启动」完全一致。
     */
    var launchGame: (suspend (Version) -> Boolean)? = null

    // ─────────────────────────────────────────────────────────────
    //  发送消息 → 交给 Agent 循环
    // ─────────────────────────────────────────────────────────────

    fun send(rawInput: String) {
        val text = rawInput.trim()
        if (text.isEmpty() || _busy.value) return

        val providerType = AISettings.provider.getValue()
        if (!AISettings.hasKey(providerType)) {
            _error.value = "还没有配置 ${providerType.displayName} 的 API Key，请点右上角进入 AI 设置。"
            return
        }
        if (!AISettings.hasModel()) {
            _error.value = "还没有选择模型，请点右上角进入 AI 设置并拉取模型列表。"
            return
        }

        _error.value = null

        // 用户消息立刻上屏
        val userMessage = AIMessage(role = AIRole.USER, text = text)
        val conversation = _messages.value + userMessage
        _messages.value = conversation
        _busy.value = true

        runJob = viewModelScope.launch {
            var currentAssistantId: String? = null

            try {
                AIAgent.run(
                    providerType = providerType,
                    apiKey = AISettings.getKey(providerType),
                    baseUrl = AISettings.getBaseUrl(providerType),
                    model = AISettings.model.getValue(),
                    temperature = AISettings.temperature.getValue(),
                    systemPrompt = buildSystemPrompt(),
                    conversation = conversation,
                    context = buildToolContext(),
                    permissionMode = AISettings.permissionMode.getValue(),
                    maxSteps = AISettings.maxAgentSteps.getValue(),
                ).collect { event ->
                    when (event) {
                        is AgentEvent.TextDelta -> {
                            val id = currentAssistantId
                            if (id == null) {
                                val msg = AIMessage(
                                    role = AIRole.ASSISTANT,
                                    text = event.text,
                                    streaming = true,
                                )
                                currentAssistantId = msg.id
                                _messages.value = _messages.value + msg
                            } else {
                                _messages.value = _messages.value.map {
                                    if (it.id == id) it.copy(text = it.text + event.text) else it
                                }
                            }
                        }

                        is AgentEvent.TurnFinished -> {
                            val id = currentAssistantId
                            val finished = event.message
                            if (id == null) {
                                //整轮只调用了工具、没有正文：也要让这一轮在界面上可见
                                val msg = finished.copy(streaming = false)
                                currentAssistantId = msg.id
                                _messages.value = _messages.value + msg
                            } else {
                                _messages.value = _messages.value.map {
                                    if (it.id == id) {
                                        it.copy(
                                            text = finished.text.ifBlank { it.text },
                                            toolCalls = finished.toolCalls,
                                            streaming = false,
                                        )
                                    } else it
                                }
                            }
                            currentAssistantId = null
                        }

                        is AgentEvent.ToolFinished -> {
                            if (AISettings.showToolCalls.getValue()) {
                                _messages.value = _messages.value + AIMessage(
                                    role = AIRole.TOOL,
                                    toolResults = listOf(event.result),
                                )
                            }
                        }

                        is AgentEvent.Failed -> {
                            _messages.value = _messages.value + AIMessage(
                                role = AIRole.ASSISTANT,
                                error = event.message,
                            )
                        }

                        AgentEvent.ToolStarted, AgentEvent.Done -> Unit
                    }
                }
            } catch (e: Exception) {
                _messages.value = _messages.value + AIMessage(
                    role = AIRole.ASSISTANT,
                    error = e.message ?: e::class.simpleName ?: "未知错误",
                )
            } finally {
                _busy.value = false
                _pendingConfirm.value = null
            }
        }
    }

    /** 中止当前这一轮 */
    fun stop() {
        runJob?.cancel()
        runJob = null
        confirmDeferred?.complete(false)
        confirmDeferred = null
        _pendingConfirm.value = null
        _busy.value = false
    }

    /** 清空对话 */
    fun clear() {
        stop()
        _messages.value = emptyList()
        _error.value = null
    }

    fun dismissError() {
        _error.value = null
    }

    /** 用户在「操作确认」弹窗里点了允许 / 拒绝 */
    fun resolveConfirm(approved: Boolean) {
        confirmDeferred?.complete(approved)
        confirmDeferred = null
        _pendingConfirm.value = null
    }

    // ─────────────────────────────────────────────────────────────
    //  模型列表：从 Provider 动态拉取，绝不硬编码
    // ─────────────────────────────────────────────────────────────

    fun loadModels() {
        val providerType = AISettings.provider.getValue()
        val key = AISettings.getKey(providerType)
        if (key.isBlank()) {
            _modelError.value = "请先填写 ${providerType.displayName} 的 API Key。"
            return
        }
        if (_loadingModels.value) return

        _loadingModels.value = true
        _modelError.value = null

        viewModelScope.launch {
            runCatching {
                AIProviders.get(providerType).listModels(key, AISettings.getBaseUrl(providerType))
            }.onSuccess { list ->
                _models.value = list
                if (list.isEmpty()) {
                    _modelError.value = "接口没有返回任何可用模型。"
                } else if (AISettings.model.getValue().isBlank() ||
                    list.none { it.id == AISettings.model.getValue() }
                ) {
                    //默认选中第一个，用户也可以自己换
                    AISettings.model.save(list.first().id)
                }
            }.onFailure { e ->
                _models.value = emptyList()
                _modelError.value = e.message ?: "拉取模型列表失败。"
            }
            _loadingModels.value = false
        }
    }

    fun selectModel(modelId: String) {
        AISettings.model.save(modelId)
    }

    // ─────────────────────────────────────────────────────────────
    //  工具上下文：把「启动游戏」「请求确认」注入给 Agent
    // ─────────────────────────────────────────────────────────────

    private fun buildToolContext(): AIToolContext = AIToolContext(
        currentVersion = VersionsManager.currentVersion.value,
        confirm = { toolName, detail ->
            val deferred = CompletableDeferred<Boolean>()
            confirmDeferred = deferred
            _pendingConfirm.value = PendingConfirm(toolName, detail)
            deferred.await()
        },
        launchGame = { version -> launchGame?.invoke(version) ?: false },
    )

    /**
     * 系统提示：告诉模型它**能真的动手**，并给出当前环境。
     *
     * 刻意写得直接一些，避免模型退化成「给你写一段教程」。
     */
    private fun buildSystemPrompt(): String {
        val tools = AIToolRegistry.all()
        val current = VersionsManager.currentVersion.value?.getVersionName() ?: "（未选中）"
        val instances = VersionsManager.versions.value.joinToString("、") { it.getVersionName() }

        return buildString {
            appendLine("你是 ZyNova 启动器内置的 AI 助手。")
            appendLine()
            appendLine("## 最重要的一条")
            appendLine("你不是一个只会给建议的聊天机器人。你**可以直接调用工具**，")
            appendLine("真实地读取文件、查看日志、增删模组、修改配置、安装资源、启动游戏。")
            appendLine("所以：")
            appendLine("- 遇到问题**先用工具去查**，不要凭猜测回答；")
            appendLine("- 用户让你修，就**直接调用工具去修**，不要只写一段「请你自己这样操作」；")
            appendLine("- 改完之后**再用只读工具确认一次**，然后告诉用户实际结果；")
            appendLine("- 只有确实无法通过工具完成时，才向用户说明原因。")
            appendLine()
            appendLine("## 当前环境")
            appendLine("- 当前选中的实例：$current")
            appendLine("- 已有实例：${instances.ifEmpty { "（无）" }}")
            appendLine("- 权限模式：${AISettings.permissionMode.getValue().name}")
            appendLine()
            appendLine("## 可用工具（共 ${tools.size} 个，只能使用下面列出的名字）")
            tools.forEach { appendLine("- ${it.spec.name}（${it.risk.name}）：${it.spec.description.lineSequence().first()}") }
            appendLine()
            appendLine("## 规则")
            appendLine("1. 只能调用上面列出的工具，不要编造工具名。")
            appendLine("2. 涉及「删除」这类不可逆操作前，先说清楚你要删什么；用户已明确要求时可直接执行。")
            appendLine("3. 修改配置文件前先读一遍，只做最小必要改动。")
            appendLine("4. 每次工具失败后要读错误信息并调整做法，不要重复同样的调用。")
            appendLine("5. 用中文回答，简洁直接，不要复述工具返回的原始内容。")
        }
    }
}

/**
 * 待用户确认的工具调用（仅「操作确认」模式会出现）
 */
data class PendingConfirm(
    val toolName: String,
    val detail: String,
)
