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
import java.net.InetSocketAddress
import java.net.ServerSocket

private const val TAG = "FrpManager"

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

    suspend fun editTunnel(
        id: Int,
        name: String? = null,
        localIp: String? = null,
        localPort: Int? = null,
        remote: String? = null
    ) {
        FrpApi.editTunnel(
            token = token,
            id = id,
            name = name,
            localIp = localIp,
            localPort = localPort,
            remote = remote
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
    fun detectLanPorts(): List<Int> {
        val uid = android.os.Process.myUid()
        val ports = linkedSetOf<Int>()
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
            }.onFailure {
                Logger.warning(TAG, "Unable to read $path for LAN port detection", it)
            }
        }
        return ports.sortedDescending()
    }

    /**
     * 当前正在运行的隧道 ID。
     */
    fun runningTunnelId(): Int = currentTunnelId
}
