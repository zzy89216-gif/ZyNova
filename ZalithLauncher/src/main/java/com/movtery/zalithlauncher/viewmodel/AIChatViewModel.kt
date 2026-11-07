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
import com.movtery.zalithlauncher.ai.provider.AIProviderType
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
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

    /**
     * 当前这轮生成的代号。
     *
     * ⚠️ 解决「停止后立刻再发消息，被上一轮的收尾逻辑污染」的竞态（议题 #24）：
     * 旧协程的 `catch/finally` 是**异步**执行的，可能在新一轮已经开始后才跑到。
     * 每次发送/停止都自增它，收尾时只要对不上号，就说明自己已经过期，什么都不要改。
     */
    private var runGeneration = 0

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

        if (!AISettings.hasKey()) {
            _error.value = GlobalContext.getString(R.string.ai_error_no_api_key, AIProviderType.DEEPSEEK.displayName)
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

        val generation = ++runGeneration
        runJob = viewModelScope.launch {
            var currentAssistantId: String? = null

            try {
                AIAgent.run(
                    providerType = AIProviderType.DEEPSEEK,
                    apiKey = AISettings.getKey(),
                    baseUrl = AISettings.getBaseUrl(),
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
                                            summary = event.call.summary,
                                        )
                                    ),
                                )
                            }
                        }

                        AgentEvent.Done -> Unit
                    }
                }
            } catch (e: CancellationException) {
                // 用户点了「停止」：这是正常流程，**不能**当成错误往聊天里塞一条红字。
                // 正在流式的助手消息标记为「已中止」，让用户知道这轮没说完。
                if (generation == runGeneration) {
                    currentAssistantId?.let { id ->
                        replaceMessage(id) { it.copy(streaming = false, interrupted = true) }
                    }
                }
                throw e
            } catch (e: Exception) {
                // 过期协程的异常不要再写进界面，否则会盖掉新一轮的内容
                if (generation == runGeneration) {
                    _messages.value = _messages.value + AIMessage(
                        role = AIRole.ASSISTANT,
                        error = e.message ?: e::class.simpleName ?: GlobalContext.getString(R.string.ai_error_unknown),
                    )
                }
            } finally {
                // 只有「当前这一轮」才有资格收尾；旧协程晚到的 finally 不能把新一轮的
                // busy / 待确认状态清掉（那正是「停止后再发送就报错/卡住」的根因之一）
                if (generation == runGeneration) {
                    _busy.value = false
                    _pendingConfirm.value = null
                    persistAsync()
                }
            }
        }
    }

    /** 中止当前这一轮 */
    fun stop() {
        // 先作废旧代号，旧协程随后的 catch/finally 就会自动跳过
        runGeneration++
        runJob?.cancel()
        runJob = null
        confirmDeferred?.complete(false)
        confirmDeferred = null
        _pendingConfirm.value = null
        _busy.value = false
        // 界面上的「正在输入」必须立刻停掉，不能等旧协程的收尾
        _messages.value = _messages.value.map { msg ->
            if (msg.streaming) msg.copy(streaming = false, interrupted = true) else msg
        }
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
            appendLine("真实地读取文件、查看日志、增删模组、改设置、装游戏本体、装资源、装整合包、启动游戏。")
            appendLine("所以：")
            appendLine("- 遇到问题**先用工具去查**，不要凭猜测回答；")
            appendLine("- 用户让你修，就**直接调用工具去修**，不要只写一段「请你自己这样操作」；")
            appendLine("- 改完之后**再用只读工具确认一次**，然后告诉用户实际结果；")
            appendLine("- 只有确实无法通过工具完成时，才向用户说明原因。")
            appendLine()
            appendLine("## 你是 Agent：默认「替用户做完」")
            appendLine("用户说「帮我…」「我要…」，默认含义是 **你去把它做完**，不是「教我怎么弄」。")
            appendLine("一个任务需要好几步时，自己连着做完：查现状 → 动手改 → 再查一遍确认 → 用一两句话汇报结果，")
            appendLine("中间不要停下来问「需要我继续吗」，也不要每做一步就报备一次。")
            appendLine("只有两种情况才停下来问：① 操作不可逆且有歧义（例如「删哪个实例」）；② 确实超出工具能力。")
            appendLine("不要在回答里写「你可以去设置里关掉…」这种教程，除非用户明确是在问怎么做。")
            appendLine()
            appendLine("## 做文档 / PPT")
            appendLine("- 用户要「写个文档 / 报告 / 说明」→ 先组织内容，再调用 `create_docx` 生成并保存；")
            appendLine("- 用户要「做一份 PPT / 演示文稿」→ 先提炼大纲（每页一个标题 + 3~5 个要点），")
            appendLine("  再用 JSON 数组调用 `create_pptx`，例如：")
            appendLine("  `[{\"title\":\"封面\",\"points\":[\"副标题\"]},{\"title\":\"第二页\",\"points\":[\"要点1\",\"要点2\"]}]`")
            appendLine("- 生成后把**文件路径**告诉用户（默认在 Download 里）。")
            appendLine()
            appendLine("## 联网工具的兜底")
            appendLine("- 如果 `web_search` 因为网络问题（超时、限流、被墙、返回空）失败，")
            appendLine("  **不要中断服务**：立刻改用 `fetch_webpage` 去抓对应搜索引擎的结果页")
            appendLine("  （例如 `https://html.duckduckgo.com/html/?q=关键词`）再整理答案；")
            appendLine("- 两者都失败时，明确告诉用户「网络查询失败」并给出你基于已有知识的判断。")
            appendLine()
            appendLine("## 默认就能干活，不需要用户先配置")
            appendLine("用户不会改启动器设置，也不该为了让你干活去改任何东西 —— 工具默认就是可用的。")
            appendLine("所以不要回「请先去设置里开启某某功能」「请你自己到某个页面操作」，")
            appendLine("能做的直接做；确实做不到（例如要用户授权的系统权限）才说明原因和入口。")
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
            appendLine()
            appendLine("## 收藏怎么用")
            appendLine("- `list_favorites` 会同时返回 **online（在线收藏）** 与 **archive（本地归档）**，两者性质不同，回答时要说清楚是哪一类。")
            appendLine("- 想装某个在线收藏：拿它的 `platform` + `project_id` 直接调 `install_resource`（会自动解析并安装必需前置）。")
            appendLine("- `remove_favorite`：`target=online` 只取消在线标记，不动本地文件；`target=archive` 只删本地副本，不动在线收藏。**不要混为一谈，也不要说成「一起删掉」。**")
            appendLine()
            appendLine("## 实例配置（怎么把实例改成用户想要的样子）")
            appendLine("- 看一个实例现在的启动配置 / 游戏设置：`get_instance_config`。")
            appendLine("- 改启动配置：`set_instance_config`，人话键名（renderer / ram_mb / isolation / java_runtime / jvm_args …），")
            appendLine("  一次只改一个键，改完它会自己回读校验。**不要**用 write_file 去手写 version.config 的 JSON。")
            appendLine("- 改游戏内设置（视距、GUI 缩放、语言、FPS 上限等）：`set_game_option`，只动 options.txt 里指定的那一行，")
            appendLine("  会先备份成 options.txt.bak。要恢复默认就把 value 传空字符串。")
            appendLine("- 涉及渲染器时，值是标识符（`auto` 或 get_instance_config 里列出的那些），不要编造。")
            appendLine()
            appendLine("## 设置与配置")
            appendLine("- 改启动器设置用 `list_settings` → `set_setting`，先读再改，改完再读一次确认生效。")
            appendLine("- 改游戏实例里的文件（options.txt、配置文件等）用 `read_file` / `write_file`：")
            appendLine("  **先读原文 → 只改必要的行 → 写回 → 再读一次确认**；不确定的文件名先 `list_folder`。")
            appendLine("- 不要凭印象编造配置键名。")
            appendLine()
            appendLine("## 下载与安装")
            appendLine("- 搜资源用 `search_resources`，拿到 `project_id` 再用 `install_resource`。")
            appendLine("- 安装是**静默后台任务**：调用后进度显示在启动器顶部的细进度条，不要谎称「已装好」，")
            appendLine("  应以工具返回的结果为准；失败要如实说明原因（缺前置 / 版本不兼容 / 网络失败）。")
            appendLine("- 装**游戏本体**用 `install_game`（参数只要 game_version，加载器可选）：")
            appendLine("  - 不知道版本号先 `list_downloadable_versions`；不确定当前装到哪一步用 `install_status`。")
            appendLine("  - 用户说「装个 1.20.1 Fabric」时，直接 `install_game(game_version=\"1.20.1\", loader=\"fabric\")`，")
            appendLine("    加载器版本会自动选最新的稳定版，不需要问用户要版本号，也不需要他去改配置。")
            appendLine("  - 装完再 `launch_game` 就能进游戏；装失败了就照 `install_status` 给出的原因解释。")
            appendLine("- 用户要装的资源如果依赖别的模组，`install_resource` 会自动带上前置，不要自己一个个装。")
            appendLine("- `install_resource` 的 `type` 可以**不传**：它会先问平台确认项目真实类型（模组 / 资源包 / 光影 / 存档），")
            appendLine("  再决定装进哪个目录。别凭猜传 `type`，猜错就是「装好了但游戏里看不到」。")
            appendLine("- 装**整合包**用 `install_modpack`（只要 platform + project_id）：它会新建一个独立实例，")
            appendLine("  自动装好游戏版本、加载器和全部模组，装完返回新实例名，直接 `launch_game` 就能进。")
            appendLine("  - 找整合包：`search_resources(type=\"modpack\")`；只想要某个模组时别装整个整合包。")
            appendLine("  - `install_resource` 提示「这是整合包」时，改用 `install_modpack`，不要硬塞、也不要假装装好了。")
            appendLine("- 安装过程中进度会出现在顶部细进度条上（用户可以取消）；取消后如实说「被你取消了」，别说装好了。")
            appendLine()
            appendLine("## Fabric API 防呆（硬性限制）")
            appendLine("- Fabric API 由启动器的 Fabric 安装流程**自动加入**，不要手动下载 / 安装，")
            appendLine("  也不要建议用户去资源页找它；资源页已经把 Fabric API 隐藏。")
            appendLine("- 一个实例只能有一份 Fabric API。用户要求「装 Fabric API」时，先调用 list_mods 检查；")
            appendLine("  如果已经存在，就说明无需重复安装，**绝不能再装第二份**。")
            appendLine("- Fabric API **不可删除、不可禁用**：不要调用 delete_mod / set_mod_enabled 去改它；")
            appendLine("  用户坚持时，直接说明这是启动器的防呆限制。")
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
