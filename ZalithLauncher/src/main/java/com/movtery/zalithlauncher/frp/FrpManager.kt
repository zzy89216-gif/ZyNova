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

package com.movtery.zalithlauncher.frp

import android.content.Context
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.path.PathManager
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.utils.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.RandomAccessFile
import java.net.InetSocketAddress
import java.net.ServerSocket

private const val TAG = "FrpManager"

/**
 * 「对局域网开放」时写进日志的端口行。
 *
 * ⚠️ 这一行是**跟着游戏语言变**的（同一个 `commands.publish.started` 翻译键）：
 *
 * - 英文：`Local game hosted on port 25565`
 * - 简体中文（真机实测）：`本地游戏已在端口[25565]上开启`
 *
 * 28.0.2 只认英文，所以中文客户端永远识别不到端口。现在按优先级依次尝试，
 * 最后一条是「只认 端口/port 后面跟的数字」的兜底，能覆盖大部分其它语言。
 */
private val LAN_PORT_REGEXES = listOf(
    Regex("Local game hosted on port (\\d{1,5})"),
    Regex("本地游戏已在端口\\[(\\d{1,5})\\]"),
    Regex("(?i)(?:端口|port)\\s*\\[?(\\d{2,5})\\]?")
)

/**
 * 樱花内网穿透的运行核心。
 *
 * 职责：
 * - 访问密钥的加密保存 / 读取；
 * - 调 [FrpApi] 拉取账号、节点、隧道，并提供增删改；
 * - 托管 `frpc` 子进程（生命周期、日志、退出状态）；
 * - 维护前台服务，避免息屏后隧道被系统回收；
 * - 本机端口占用检测。
 *
 * ⚠️ 访问密钥只会以环境变量 `NATFRP_TOKEN` 传给子进程：
 * - 不出现在命令行参数里（`ps` 看不到）；
 * - 不写进任何日志；
 * - 磁盘上只保存 Keystore 加密后的密文。
 */
object FrpManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** frpc 在 jniLibs 中的文件名（见 `ZalithLauncher/src/main/jniLibs`） */
    private const val FRPC_LIB_NAME = "libfrpc.so"

    private const val MAX_LOG_LINES = 200

    /** `/proc/net/tcp*` 的字段分隔符（可能有多个连续空格）*/
    private val WHITESPACE = Regex("\\s+")

    private val _tunnels = MutableStateFlow<List<FrpTunnel>>(emptyList())
    val tunnels: StateFlow<List<FrpTunnel>> = _tunnels.asStateFlow()

    private val _nodes = MutableStateFlow<Map<String, FrpNode>>(emptyMap())
    val nodes: StateFlow<Map<String, FrpNode>> = _nodes.asStateFlow()

    private val _user = MutableStateFlow<FrpUser?>(null)
    val user: StateFlow<FrpUser?> = _user.asStateFlow()

    private val _runState = MutableStateFlow<FrpRunState>(FrpRunState.Idle)
    val runState: StateFlow<FrpRunState> = _runState.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())

    /** /proc 读取被系统拒绝时只提示一次，避免日志刷屏 */
    private var procReadDenied = false
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    @Volatile
    private var process: Process? = null

    @Volatile
    private var currentTunnelId: Int = -1

    @Volatile
    private var appContext: Context? = null

    private var logJob: Job? = null

    /**
     * 当前访问密钥（明文只存在于内存与 Keystore 加密结果中）。
     */
    var token: String
        get() = FrpSecret.decrypt(AllSettings.frpToken.getValue()).orEmpty()
        set(value) {
            val trimmed = value.trim()
            if (trimmed.isEmpty()) {
                AllSettings.frpToken.save("")
            } else {
                val encrypted = FrpSecret.encrypt(trimmed)
                if (encrypted == null) {
                    Logger.warning(TAG, "Unable to encrypt SakuraFrp token with Android Keystore")
                } else {
                    AllSettings.frpToken.save(encrypted)
                }
            }
        }

    val isLoggedIn: Boolean
        get() = token.isNotBlank()

    /**
     * 登录：校验访问密钥并拉取账号与隧道。
     */
    suspend fun login(accessToken: String) {
        val validated = FrpApi.userInfo(accessToken.trim())
        token = accessToken
        if (token.isBlank()) {
            // Keystore 不可用时不接受「看似登录成功、实际没存下密钥」的状态
            throw FrpApiException("无法在本机安全保存访问密钥（Android Keystore 不可用）")
        }
        _user.value = validated
        _tunnels.value = FrpApi.tunnels(accessToken.trim())
    }

    /**
     * 刷新账号信息与隧道列表。
     */
    suspend fun refresh() {
        val current = token
        if (current.isBlank()) return
        _user.value = FrpApi.userInfo(current)
        _tunnels.value = FrpApi.tunnels(current)
    }

    /**
     * 拉取节点列表。
     */
    suspend fun loadNodes() {
        val current = token
        if (current.isBlank()) return
        _nodes.value = FrpApi.nodes(current)
    }

    suspend fun createTunnel(
        name: String,
        type: String,
        node: Int,
        localIp: String,
        localPort: Int,
        remote: String?
    ): FrpCreateResult {
        val result = FrpApi.createTunnel(
            token = token,
            name = name,
            type = type,
            node = node,
            localIp = localIp,
            localPort = localPort,
            remote = remote
        )
        refresh()
        return result
    }

    /**
     * 修改隧道：只支持本机端口与备注。
     *
     * ⚠️ 官方 `/tunnel/edit` 不接受 `name` 与 `remote`，改名必须删了重建，
     * 所以界面上也不再给「改名」入口（给了也只会静默失败）。
     */
    suspend fun editTunnel(
        id: Int,
        note: String? = null,
        localIp: String? = null,
        localPort: Int? = null
    ) {
        FrpApi.editTunnel(
            token = token,
            id = id,
            note = note,
            localIp = localIp,
            localPort = localPort
        )
        refresh()
    }

    suspend fun deleteTunnel(id: Int) {
        FrpApi.deleteTunnel(token, id)
        refresh()
    }

    /**
     * 退出登录：停止隧道并清空本机保存的密钥与列表。
     */
    fun logout() {
        stop()
        token = ""
        _user.value = null
        _tunnels.value = emptyList()
        _nodes.value = emptyMap()
        currentTunnelId = -1
    }

    /**
     * 启动指定隧道的 frpc。
     *
     * 约束：
     * - 同一时间只允许一个隧道（同一隧道被两个客户端连接会互踢）；
     * - 本机端口被占用时直接拒绝，避免「隧道连上了但 502」。
     */
    fun start(context: Context, tunnel: FrpTunnel) {
        appContext = context.applicationContext

        val accessToken = token
        if (accessToken.isBlank()) {
            _runState.value = FrpRunState.Failed("尚未填写访问密钥")
            return
        }

        if (tunnel.localPort in 1..65535 && !isLocalPortInUse(tunnel.localPort)) {
            // 本机端口没有程序监听：说明游戏还没「对局域网开放」，或端口填错了
            _runState.value = FrpRunState.Failed(
                "本机端口 ${tunnel.localPort} 没有程序在监听，请先在游戏里选择「对局域网开放」"
            )
            return
        }

        stopProcessOnly()

        val binary = File(PathManager.DIR_NATIVE_LIB, FRPC_LIB_NAME)
        if (!binary.isFile) {
            _runState.value = FrpRunState.Failed("未找到 frpc 组件（libfrpc.so），请重新安装应用")
            return
        }

        _runState.value = FrpRunState.Starting
        appendLog("正在启动 frpc，隧道 ID：${tunnel.id}")

        //⚠️ 这里必须按樱花官方文档的形式传参：`frpc -f <访问密钥>:<隧道ID>`
        //（<https://doc.natfrp.com/frpc/usage.html>「启动参数的格式」）。
        //27.10.1 / 28.0.0 用的是 `--disable_log_color` + 环境变量 NATFRP_TOKEN/NATFRP_TARGET，
        //而樱花定制的 frpc 根本不读这两个环境变量 —— 结果就是 frpc 启动后立刻退出，
        //界面显示「未运行」，看起来像「点了启动没反应」。
        val started = runCatching {
            val builder = ProcessBuilder(
                binary.absolutePath,
                "-f",
                "$accessToken:${tunnel.id}"
            )
            builder.redirectErrorStream(true)
            builder.start()
        }.getOrElse { e ->
            Logger.warning(TAG, "Unable to start frpc", e)
            _runState.value = FrpRunState.Failed(e.message ?: "frpc 启动失败")
            return
        }
        Logger.info(TAG, "frpc started, tunnel=${tunnel.id}, localPort=${tunnel.localPort}")

        process = started
        currentTunnelId = tunnel.id
        _runState.value = FrpRunState.Running(tunnel.id)
        FrpService.start(context)

        logJob = scope.launch {
            readProcessOutput(started)
            val exitCode = runCatching { started.waitFor() }.getOrDefault(-1)
            if (process === started) {
                process = null
                _runState.value = FrpRunState.Stopped(exitCode)
                appendLog("frpc 已退出，退出码：$exitCode")
                Logger.warning(TAG, "frpc exited, code=$exitCode")
                appContext?.let { FrpService.stop(it) }
            }
        }
    }

    /**
     * 停止 frpc 与前台服务。
     */
    fun stop() {
        stopProcessOnly()
        _runState.value = FrpRunState.Idle
        appContext?.let { FrpService.stop(it) }
    }

    /**
     * 前台服务被系统回收时，同步结束子进程，避免留下孤儿进程。
     */
    fun onServiceDestroyed() {
        stopProcessOnly()
        if (_runState.value is FrpRunState.Running) {
            _runState.value = FrpRunState.Idle
        }
    }

    private fun stopProcessOnly() {
        logJob?.cancel()
        logJob = null
        process?.let { running ->
            runCatching { running.destroy() }
            runCatching { running.waitFor() }
        }
        process = null
        currentTunnelId = -1
    }

    private fun readProcessOutput(running: Process) {
        runCatching {
            running.inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    if (line.isNotBlank()) appendLog(line)
                }
            }
        }.onFailure {
            Logger.warning(TAG, "Unable to read frpc output", it)
        }
    }

    private fun appendLog(line: String) {
        _logs.value = (_logs.value + line).takeLast(MAX_LOG_LINES)
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    /**
     * 隧道公网连接地址：`节点地址:远程端口`。
     *
     * - 远程字段本身带冒号（绑定域名）时直接使用；
     * - 拿不到节点地址时返回 null，由界面提示用户到面板查看。
     */
    fun tunnelAddress(tunnel: FrpTunnel): String? {
        val remote = tunnel.remote?.trim().orEmpty()
        if (remote.isEmpty()) return null
        if (remote.contains(':')) return remote
        val host = _nodes.value[tunnel.node.toString()]?.host?.trim().orEmpty()
        if (host.isEmpty()) return null
        return "$host:$remote"
    }

    /**
     * 本机端口是否已被占用。
     *
     * 用通配地址尝试绑定：能绑上说明当前没有程序监听该端口。
     */
    fun isLocalPortInUse(port: Int): Boolean {
        if (port !in 1..65535) return true
        return runCatching {
            ServerSocket().use { socket ->
                socket.reuseAddress = false
                socket.bind(InetSocketAddress(port))
            }
            false
        }.getOrDefault(true)
    }

    /**
     * 自动检测 Minecraft「对局域网开放」后监听的端口。
     *
     * 原理：MC 服务端和启动器是**同一个进程 / 同一个 uid**，
     * 它监听的 socket 会出现在 `/proc/net/tcp` 与 `/proc/net/tcp6` 里（world-readable，不需要 root）。
     * 按 `st == 0A`（LISTEN）筛出监听项，再只保留 uid 属于本应用的端口，
     * 就得到「游戏正在监听的端口」——也就是玩家在游戏里点「对局域网开放」时聊天栏显示的那个端口。
     *
     * 返回按端口号从大到小排序（MC 的临时端口通常落在高位区间）。
     */
    /**
     * 隧道名称清洗。
     *
     * ⚠️ 樱花对隧道名有字符限制：**不能带空格 / 中文**（真实行为：提交 `Minecraft 联机`
     * 会直接被服务端拒绝，创建失败）。这里统一把非法字符换成 `-`，
     * 并把结果限制在 32 个字符内，保证「用户随便输入也能建成功」。
     */
    fun sanitizeTunnelName(raw: String): String {
        val cleaned = raw
            .replace(WHITESPACE, "-")
            .map { ch ->
                if (ch.isLetterOrDigit() && ch.code < 128 || ch == '-' || ch == '_') ch else '-'
            }
            .joinToString("")
            .replace(Regex("-{2,}"), "-")
            .trim('-', '_')
            .take(32)
        return cleaned.ifBlank { "Minecraft-LAN" }
    }

    fun detectLanPorts(): List<Int> {
        val ports = readListeningPortsFromProc()
        if (ports.isEmpty()) {
            //Android 10 起 /proc/net/tcp 对普通应用不开放（EACCES），
            //这里用「游戏日志」兜底：原版服务端「对局域网开放」时一定会写一行
            //`[Server thread/INFO]: Local game hosted on port 5xxxx`
            detectPortFromGameLog()?.let { ports += it }
        }
        return ports.sortedDescending()
    }

    /** 读 `/proc/net/tcp{,6}` 里属于本应用 uid 的 LISTEN 端口（部分 ROM 允许）*/
    private fun readListeningPortsFromProc(): MutableSet<Int> {
        val uid = android.os.Process.myUid()
        val ports = linkedSetOf<Int>()
        var failed = false
        listOf("/proc/net/tcp", "/proc/net/tcp6").forEach { path ->
            runCatching {
                File(path).forEachLine { line ->
                    if (line.isBlank() || line.startsWith("sl")) return@forEachLine
                    val parts = line.trim().split(WHITESPACE)
                    if (parts.size < 8) return@forEachLine
                    // st: 0A = LISTEN
                    if (!parts[3].equals("0A", ignoreCase = true)) return@forEachLine
                    val port = parts[1].substringAfterLast(':').toIntOrNull(16) ?: return@forEachLine
                    val owner = parts[7].toIntOrNull() ?: return@forEachLine
                    if (owner == uid && port in 1..65535) ports += port
                }
            }.onFailure { failed = true }
        }
        if (failed && !procReadDenied) {
            procReadDenied = true
            //只提示一次：真机上这条会刷屏（每个对话框打开都读一次）
            Logger.info(
                TAG,
                "Unable to read /proc/net/tcp (blocked on Android 10+); falling back to game log detection"
            )
        }
        return ports
    }

    /**
     * 从游戏日志里找「对局域网开放」的端口。
     *
     * 原版/大部分加载器在成功开放局域网时会输出：
     * `[Server thread/INFO]: Local game hosted on port 51234`
     * 这是最可靠、且不需要任何系统权限的来源。
     *
     * 只看最近修改的 3 个 `latest.log` 的**尾部 128KB**，避免读取整份大日志。
     */
    private fun detectPortFromGameLog(): Int? {
        val roots = runCatching {
            GamePathManager.gamePathData.value.map { File(it.path) }
        }.getOrDefault(emptyList()).ifEmpty {
            //兜底：直接用当前游戏根目录（公共目录），别再去私有目录找
            listOf(PathManager.DIR_GAME)
        }

        val logs = roots
            .mapNotNull { root ->
                buildList {
                    //共享 gameDir：<gameDir>/logs/latest.log
                    add(File(root, "logs/latest.log"))
                    //每个版本独立目录：<gameDir>/versions/<ver>/logs/latest.log
                    File(root, "versions").listFiles()?.forEach { add(File(it, "logs/latest.log")) }
                }
            }
            .flatten()
            .filter { it.isFile && it.length() > 0L }
            .sortedByDescending { it.lastModified() }

        for (log in logs.take(3)) {
            val port = runCatching {
                val tail = readTail(log, 128 * 1024)
                LAN_PORT_REGEXES.firstNotNullOfOrNull { regex ->
                    regex.findAll(tail).lastOrNull()?.groupValues?.get(1)?.toIntOrNull()
                }
            }.getOrNull()
            if (port != null && port in 1..65535) {
                Logger.info(TAG, "Detected LAN port $port from ${log.absolutePath}")
                return port
            }
        }
        return null
    }

    /** 读文件尾部（日志可能很大，只取最后 [maxBytes] 字节）*/
    private fun readTail(file: File, maxBytes: Int): String {
        RandomAccessFile(file, "r").use { raf ->
            val length = raf.length()
            val start = (length - maxBytes).coerceAtLeast(0L)
            raf.seek(start)
            val bytes = ByteArray((length - start).toInt())
            raf.readFully(bytes)
            return String(bytes, Charsets.UTF_8)
        }
    }

    /**
     * 当前正在运行的隧道 ID。
     */
    fun runningTunnelId(): Int = currentTunnelId
}
