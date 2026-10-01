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
import com.movtery.zalithlauncher.context.GlobalContext
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.ai.AISettings
import com.movtery.zalithlauncher.ai.agent.AIAgent
import com.movtery.zalithlauncher.ai.agent.AIToolContext
import com.movtery.zalithlauncher.ai.agent.AIToolRegistry
import com.movtery.zalithlauncher.ai.agent.AgentEvent
import com.movtery.zalithlauncher.ai.conversation.AIConversation
import com.movtery.zalithlauncher.ai.conversation.AIConversationMeta
import com.movtery.zalithlauncher.ai.conversation.AIConversationStore
import com.movtery.zalithlauncher.ai.model.AIMessage
import com.movtery.zalithlauncher.ai.model.AIRole
import com.movtery.zalithlauncher.ai.model.AIToolResult
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    // ── 历史对话（侧边栏用）────────────────────────────────────────
    private val _conversations = MutableStateFlow<List<AIConversationMeta>>(emptyList())
    val conversations: StateFlow<List<AIConversationMeta>> = _conversations.asStateFlow()

    /** 当前正在看的对话 id */
    private val _currentConversationId = MutableStateFlow<String?>(null)
    val currentConversationId: StateFlow<String?> = _currentConversationId.asStateFlow()

    /** 当前对话（含标题与时间戳）。只在 IO 线程写，@Volatile 保证可见性 */
    @Volatile
    private var currentConversation: AIConversation? = null

    /** 串行化落盘，避免多次保存互相覆盖 */
    private val persistMutex = Mutex()

    /**
     * 落盘专用的单并发派发器。
     *
     * ⚠️ 必须声明在 `init` 之前：Kotlin 的属性按声明顺序初始化，
     * 如果 `init` 里的代码先碰到它，读到的会是未初始化的 null 而直接 NPE。
     * 单并发还能保证多次保存**按调用顺序**执行。
     */
    private val persistDispatcher = Dispatchers.IO.limitedParallelism(1)

    // ── 「操作确认」模式下的待确认请求 ────────────────────────────
    private val _pendingConfirm = MutableStateFlow<PendingConfirm?>(null)
    val pendingConfirm: StateFlow<PendingConfirm?> = _pendingConfirm.asStateFlow()

    private var runJob: Job? = null
    private var confirmDeferred: CompletableDeferred<Boolean>? = null

    init {
        // 打开最近一次对话；如果没有就新建一个。
        // 读文件放到 IO 上，别在 ViewModel 构造时卡主线程。
        viewModelScope.launch(Dispatchers.IO) {
            val latest = AIConversationStore.listMetas().firstOrNull()
            val conv = latest?.let { AIConversationStore.load(it.id) }
            withContext(Dispatchers.Main) {
                if (conv != null) {
                    currentConversation = conv
                    _currentConversationId.value = conv.id
                    _messages.value = conv.messages.map { it.copy(streaming = false) }
                    _conversations.value = AIConversationStore.listMetas()
                } else {
                    newConversation()
                }
            }
        }
    }


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
            _error.value = GlobalContext.getString(R.string.ai_error_no_api_key, providerType.displayName)
            return
        }
        if (!AISettings.hasModel()) {
            _error.value = GlobalContext.getString(R.string.ai_error_no_model)
            return
        }

        _error.value = null

        // 用户消息立刻上屏，并马上落盘（万一后面崩了，用户的话不会丢）
        val userMessage = AIMessage(role = AIRole.USER, text = text)
        val conversation = _messages.value + userMessage
        _messages.value = conversation
        _busy.value = true
        persistAsync()

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
                                //逐 token 更新：只动这一条，避免每个 token 都遍历并重建整个列表
                                replaceMessage(id) { it.copy(text = it.text + event.text) }
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
                                replaceMessage(id) {
                                    it.copy(
                                        text = finished.text.ifBlank { it.text },
                                        toolCalls = finished.toolCalls,
                                        streaming = false,
                                    )
                                }
                            }
                            currentAssistantId = null
                            //一轮结束：落盘
                            persistAsync()
                        }

                        is AgentEvent.ToolFinished -> {
                            if (AISettings.showToolCalls.getValue()) {
                                replaceMessage(event.result.toolCallId) { old ->
                                    old.copy(toolResults = listOf(event.result))
                                }
                            }
                            // 工具跑完是一个安全的落盘点
                            persistAsync()
                        }

                        is AgentEvent.Failed -> {
                            _messages.value = _messages.value + AIMessage(
                                role = AIRole.ASSISTANT,
                                error = event.message,
                            )
                        }

                        // 工具开始执行：先放一条「运行中」的消息，
                        // 否则安装模组这类耗时操作期间界面完全没有反馈，看起来像卡死
                        is AgentEvent.ToolStarted -> {
                            if (AISettings.showToolCalls.getValue()) {
                                _messages.value = _messages.value + AIMessage(
                                    id = event.call.id,
                                    role = AIRole.TOOL,
                                    toolResults = listOf(
                                        AIToolResult(
                                            toolCallId = event.call.id,
                                            name = event.call.name,
                                            content = "",
                                            running = true,
                                        )
                                    ),
                                )
                            }
                        }

                        AgentEvent.Done -> Unit
                    }
                }
            } catch (e: Exception) {
                _messages.value = _messages.value + AIMessage(
                    role = AIRole.ASSISTANT,
                    error = e.message ?: e::class.simpleName ?: GlobalContext.getString(R.string.ai_error_unknown),
                )
            } finally {
                _busy.value = false
                _pendingConfirm.value = null
                persistAsync()
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
        persistAsync()
    }

    // ─────────────────────────────────────────────────────────────
    //  历史对话（侧边栏）
    // ─────────────────────────────────────────────────────────────

    /** 新建一段对话 */
    fun newConversation() {
        stop()
        val conv = AIConversation()
        currentConversation = conv
        _currentConversationId.value = conv.id
        _messages.value = emptyList()
        _error.value = null
        refreshConversations()
    }

    /** 切换到指定的历史对话 */
    fun openConversation(id: String) {
        if (_currentConversationId.value == id) return
        // 先把当前对话存好，再切走（否则旧消息会丢）
        stop()
        viewModelScope.launch(Dispatchers.IO) {
            val conv = AIConversationStore.load(id)
            withContext(Dispatchers.Main) {
                if (conv == null) {
                    // 文件不在了（被清理/损坏）→ 刷新列表并保持当前对话
                    _error.value = GlobalContext.getString(R.string.ai_error_conversation_missing)
                    refreshConversations()
                    return@withContext
                }
                currentConversation = conv
                _currentConversationId.value = conv.id
                // 载入时把 streaming 清掉，避免重新打开后还显示「正在输入」
                _messages.value = conv.messages.map { it.copy(streaming = false) }
                _error.value = null
            }
        }
    }

    /** 删除一段历史对话 */
    fun deleteConversation(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            AIConversationStore.delete(id)
            if (_currentConversationId.value == id) {
                val next = AIConversationStore.listMetas().firstOrNull()
                withContext(Dispatchers.Main) {
                    if (next != null) openConversation(next.id) else newConversation()
                }
            } else {
                withContext(Dispatchers.Main) { refreshConversations() }
            }
        }
    }

    /** 清空当前对话的消息（对话本身保留在侧边栏里） */
    fun clear() {
        stop()
        _messages.value = emptyList()
        _error.value = null
        persistAsync()
    }

    private fun refreshConversations() {
        viewModelScope.launch(Dispatchers.IO) {
            val metas = AIConversationStore.listMetas()
            withContext(Dispatchers.Main) { _conversations.value = metas }
        }
    }

    /**
     * 把当前对话异步落盘。
     *
     * 只在「安全点」调用（用户发送、一轮结束、工具跑完、停止/清空），
     * **不会每个 token 都写文件**。
     * 快照在锁内重新读取，所以即使多次保存排队，最后写入的也一定是最新状态。
     */
    private fun persistAsync() {
        //⚠️ 关键：快照必须在**调用时**就捕获。
        //   如果等协程真正跑起来再去读 currentConversation，
        //   中间发生「切换对话」就会把旧消息写进新对话的 id 下。
        val conv = currentConversation ?: return
        val messages = _messages.value
        if (messages.isEmpty()) return

        viewModelScope.launch(persistDispatcher) {
            persistMutex.withLock {
                val title = conv.title.ifBlank {
                    messages.firstOrNull { it.role == AIRole.USER }
                        ?.text
                        ?.let { AIConversation.titleFrom(it) }
                        .orEmpty()
                }
                val updated = conv.copy(
                    title = title,
                    updatedAt = System.currentTimeMillis(),
                    //落盘时清掉「正在输入」与「工具执行中」这两个界面中间态，
                    //否则重新打开对话会看到永远转不完的圈
                    messages = messages.map { msg ->
                        msg.copy(
                            streaming = false,
                            toolResults = msg.toolResults.map { r ->
                                if (r.running) r.copy(running = false) else r
                            },
                        )
                    }.filterNot { it.isEmpty },
                )
                AIConversationStore.save(updated)
                //只有当前还停在这段对话时才刷新侧边栏，避免切走后被覆盖
                if (currentConversation?.id == updated.id) {
                    currentConversation = updated
                }
                _conversations.value = AIConversationStore.listMetas()
            }
        }
    }

    /** 按 id 就地替换一条消息（流式/工具更新用，避免整表复制） */
    private fun replaceMessage(id: String, transform: (AIMessage) -> AIMessage) {
        val list = _messages.value
        val index = list.indexOfFirst { it.id == id }
        if (index < 0) return
        val newList = list.toMutableList()
        newList[index] = transform(newList[index])
        _messages.value = newList
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
            appendLine("5. **用与用户提问相同的语言回答**（用户用中文就问中文，用英文就回英文），简洁直接，不要复述工具返回的原始内容。")
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
