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

package com.movtery.zalithlauncher.ui.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.InputDevice
import android.view.KeyEvent
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.Keep
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.jakewharton.processphoenix.ProcessPhoenix
import com.movtery.inputmap.keycodes.LwjglGlfwKeycode
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.bridge.CURSOR_DISABLED
import com.movtery.zalithlauncher.bridge.LoggerBridge
import com.movtery.zalithlauncher.bridge.ZLBridge
import com.movtery.zalithlauncher.bridge.ZLBridgeStates
import com.movtery.zalithlauncher.coroutine.DataBridge
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.input.AWTCharSender
import com.movtery.zalithlauncher.game.input.CharacterSenderStrategy
import com.movtery.zalithlauncher.game.input.LWJGLCharSender
import com.movtery.zalithlauncher.game.launch.GameLauncher
import com.movtery.zalithlauncher.game.launch.GameService
import com.movtery.zalithlauncher.game.launch.JvmLaunchInfo
import com.movtery.zalithlauncher.game.launch.JvmLauncher
import com.movtery.zalithlauncher.game.launch.LaunchConfig
import com.movtery.zalithlauncher.game.launch.Launcher
import com.movtery.zalithlauncher.game.launch.handler.AbstractHandler
import com.movtery.zalithlauncher.game.launch.handler.GameHandler
import com.movtery.zalithlauncher.game.launch.handler.HandlerType
import com.movtery.zalithlauncher.game.launch.handler.JVMHandler
import com.movtery.zalithlauncher.game.multirt.RuntimesManager
import com.movtery.zalithlauncher.game.plugin.PluginLoader
import com.movtery.zalithlauncher.game.renderer.Renderers
import com.movtery.zalithlauncher.game.sdl.SdlBridge
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.terracotta.TerracottaVPNService
import com.movtery.zalithlauncher.ui.base.BaseAppCompatActivity
import com.movtery.zalithlauncher.ui.base.ObserveFullScreenSetting
import com.movtery.zalithlauncher.ui.components.rememberBoxSize
import com.movtery.zalithlauncher.ui.control.input.HidableInputLayout
import com.movtery.zalithlauncher.ui.control.input.TextInputMode
import com.movtery.zalithlauncher.ui.screens.game.elements.OpenFolderLayer
import com.movtery.zalithlauncher.ui.screens.game.elements.OpenFolderOperation
import com.movtery.zalithlauncher.ui.theme.ZalithLauncherTheme
import com.movtery.zalithlauncher.ui.toAndroidString
import com.movtery.zalithlauncher.utils.device.PhysicalMouseChecker
import com.movtery.zalithlauncher.utils.getDisplayFriendlyRes
import com.movtery.zalithlauncher.utils.getParcelableSafely
import com.movtery.zalithlauncher.viewmodel.ErrorViewModel
import com.movtery.zalithlauncher.viewmodel.EventViewModel
import com.movtery.zalithlauncher.viewmodel.GamepadViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.libsdl.app.SDLActivity
import org.libsdl.app.SDLSurface
import org.lwjgl.glfw.CallbackBridge
import java.io.File
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds
import android.graphics.Color as NativeColor


private const val INTENT_RUN_GAME = "BUNDLE_RUN_GAME"
private const val INTENT_RUN_JAR = "INTENT_RUN_JAR"
private const val INTENT_GAME_CONFIG = "INTENT_GAME_CONFIG"
private const val INTENT_JAR_INFO = "INTENT_JAR_INFO"

data class LaunchSession(
    val activityTitle: String,
    val launcher: Launcher,
    val handler: AbstractHandler,
    val inputSender: CharacterSenderStrategy
)

/**
 * 一些关键状态须在此存放
 */
class VMViewModel : ViewModel() {
    var isRunning = false

    /**
     * 是否允许VMActivity处理按键
     */
    var keyHandle = true

    val screenSizeBridge = DataBridge<IntSize>()
    var screenSize: IntSize = IntSize.Zero

    private val _onConfigurationChanged = MutableStateFlow(false)
    val onConfigurationChanged = _onConfigurationChanged.asStateFlow()

    fun onConfigurationChanged(value: Boolean = true) {
        _onConfigurationChanged.update { value }
    }

    var sender: CharacterSenderStrategy = LWJGLCharSender
        private set

    private val _openFolderOperation = MutableStateFlow<OpenFolderOperation>(OpenFolderOperation.None)
    /** 启动器内浏览目录（将文件导入该目录） */
    val openFolderOperation = _openFolderOperation.asStateFlow()

    /** 关闭浏览目录 */
    fun clearFolder() {
        _openFolderOperation.update {
            OpenFolderOperation.None
        }
    }

    private var _session: LaunchSession? = null
    val session: LaunchSession
        get() = _session ?: error("LaunchSession not initialized")

    fun initSession(
        activity: VMActivity,
        bundle: Bundle,
        errorViewModel: ErrorViewModel,
        eventViewModel: EventViewModel,
        gamepadViewModel: GamepadViewModel,
        exitListener: (Int, Boolean) -> Unit,
    ) {
        if (_session != null) return

        _session = when {
            bundle.getBoolean(INTENT_RUN_GAME) -> {
                val config: LaunchConfig = bundle.getParcelableSafely(INTENT_GAME_CONFIG, LaunchConfig::class.java)
                    ?: throw IllegalStateException("No launch config has been set.")

                val launcher = GameLauncher(
                    activity = activity,
                    config = config,
                    onExit = { code, isSignal ->
                        if (code == 0) {
                            val finishedCount = AllSettings.finishedGame.getValue()
                            if (finishedCount < Int.MAX_VALUE)  {
                                AllSettings.finishedGame.save(finishedCount + 1)
                            }
                        }
                        exitListener(code, isSignal)
                    },
                    openPath = { folder ->
                        _openFolderOperation.update {
                            OpenFolderOperation.OpenFolder(folder)
                        }
                    }
                )

                sender = LWJGLCharSender

                LaunchSession(
                    activityTitle = config.version.getVersionName(),
                    launcher = launcher,
                    handler = GameHandler(
                        activity = activity,
                        config = config,
                        errorViewModel = errorViewModel,
                        eventViewModel = eventViewModel,
                        gamepadViewModel = gamepadViewModel,
                        gameLauncher = launcher
                    ) { code ->
                        exitListener(code, false)
                    },
                    inputSender = LWJGLCharSender
                )
            }
            bundle.getBoolean(INTENT_RUN_JAR) -> {
                val jvmLaunchInfo: JvmLaunchInfo = bundle.getParcelableSafely(INTENT_JAR_INFO, JvmLaunchInfo::class.java)
                    ?: throw IllegalStateException("No launch jar info has been set.")

                val launcher = JvmLauncher(
                    context = activity,
                    jvmLaunchInfo = jvmLaunchInfo,
                    onExit = exitListener,
                    openPath = { folder ->
                        _openFolderOperation.update {
                            OpenFolderOperation.OpenFolder(folder)
                        }
                    }
                )

                sender = AWTCharSender

                LaunchSession(
                    activityTitle = activity.getString(R.string.execute_jar_title),
                    launcher = launcher,
                    handler = JVMHandler(
                        jvmLauncher = launcher,
                        errorViewModel = errorViewModel,
                        eventViewModel = eventViewModel,
                    ) { code ->
                        exitListener(code, false)
                    },
                    inputSender = AWTCharSender
                )
            }
            else -> error("Unknown VM launch mode")
        }
    }

    /**
     * 当前输入法开启状态
     */
    var textInputMode by mutableStateOf(TextInputMode.DISABLE)

    fun disableInputMode() {
        if (textInputMode == TextInputMode.ENABLE) textInputMode = TextInputMode.DISABLE
    }


    /**
     * 直接发送文本到游戏
     */
    private fun String.sendText() {
        forEach { char ->
            sender.sendChar(char)
        }
    }

    fun sendInputText(text: String) {
        text.sendText()
    }

    fun sendBackspace() {
        sender.sendBackspace()
    }

    fun sendEnder() {
        sender.sendEnter()
    }

    /**
     * 仅处理特殊按键
     */
    fun handleSpecialKey(keyEvent: KeyEvent) {
        when (keyEvent.keyCode) {
            KeyEvent.KEYCODE_DEL, KeyEvent.KEYCODE_ENTER -> {
                //忽略掉删除事件，避免状态不同步
            }

            KeyEvent.KEYCODE_DPAD_LEFT -> sender.sendLeft()
            KeyEvent.KEYCODE_DPAD_RIGHT -> sender.sendRight()
            KeyEvent.KEYCODE_DPAD_UP -> sender.sendUp()
            KeyEvent.KEYCODE_DPAD_DOWN -> sender.sendDown()

            KeyEvent.KEYCODE_TAB -> sender.sendTab()

            else -> sender.sendOther(keyEvent)
        }
    }

    /**
     * 返回这个按键事件是否允许被处理
     */
    fun keyCanHandle(keyEvent: KeyEvent): Boolean {
        val keyCode = keyEvent.keyCode
        //因为输入法选区时会发出Shift键的事件，但同步为游戏内的文本进行选区会比较复杂
        //比如选区时没法拿到当前输入框选择了哪些文本，极容易导致输入框与游戏内的文本出现状态差异
        //这类比较打破预期的情况应该尽量避免，所以应该忽略Shift
        val isShift = keyCode == KeyEvent.KEYCODE_SHIFT_LEFT || keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT
        //避免处理Ctrl，大部分输入法不支持处理这个，而在游戏内可能会影响到指针位置
        val isCtrl = keyCode == KeyEvent.KEYCODE_CTRL_LEFT || keyCode == KeyEvent.KEYCODE_CTRL_RIGHT
        return !isShift && !isCtrl
    }
}

class VMActivity : BaseAppCompatActivity(), SurfaceTextureListener, SurfaceHolder.Callback {
    override fun isIgnoreNotch(): Boolean = AllSettings.gameFullScreen.getValue()

    private val errorViewModel: ErrorViewModel by viewModels()

    private val eventViewModel: EventViewModel by viewModels()
    /**
     * 手柄状态存储 ViewModel
     */
    private val gamepadViewModel: GamepadViewModel by viewModels()

    private val vmViewModel: VMViewModel by viewModels()

    /** View used to locate the layout hosting SDL's text input bridge. */
    private var gameSurfaceView: View? = null

    private var applySizeToSurface: ((width: Int, height: Int) -> Unit)? = null

    /**
     * 会话心跳：`:game` 进程存活期间每 [GAME_SESSION_HEARTBEAT_MS] 刷一次标记文件的时间戳。
     *
     * 只靠「标记文件 + 进程存活」判断不够稳：游戏退出后 `:game` 进程会以 cached 状态
     * 在后台停留很久，胶囊会残留、点进去是空壳（闪退）。心跳是「VMActivity 实例还活着」的
     * 直接证据：Activity 结束 / 进程被杀后心跳停止，启动器最多 [GAME_SESSION_TIMEOUT_MS] 后收起入口。
     * 游戏切后台时 Activity 仍在、主线程 Handler 继续跑，所以不会误隐藏。
     */
    private val gameSessionHandler = Handler(Looper.getMainLooper())
    private val gameSessionHeartbeat = object : Runnable {
        override fun run() {
            markGameSession(this@VMActivity)
            gameSessionHandler.postDelayed(this, GAME_SESSION_HEARTBEAT_MS)
        }
    }

    private inline fun <T> withHandler(block: AbstractHandler.() -> T): T {
        return vmViewModel.session.handler.block()
    }

    private inline fun <T> withLauncher(block: Launcher.() -> T): T {
        return vmViewModel.session.launcher.block()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //⚠️ 兜底：只有带齐启动参数的 Intent 才能拉起游戏界面。
        //游戏结束后如果还有残留的「返回游戏」入口被点到，系统会用**不带任何 extras** 的 Intent
        //新建本 Activity（旧实例已经销毁时，REORDER_TO_FRONT 会退化成一次普通启动）。
        //以前这里会直接抛 "Unknown VM launch state!"，或走到 initSession 再抛
        //"No launch config has been set."，表现就是点一下直接闪退。
        //所以先校验启动参数：缺了就安静收尾，**不初始化游戏、也不写会话标记**（否则会留下僵尸标记）。
        //校验本身也全程兜异常：Intent 里的 extras 反序列化失败同样不能把进程崩掉。
        val launchExtras = runCatching { intent.extras }.getOrNull()
        val hasLaunchParam = launchExtras != null &&
            (
                (launchExtras.getBoolean(INTENT_RUN_GAME, false) &&
                    runCatching {
                        launchExtras.getParcelableSafely(INTENT_GAME_CONFIG, LaunchConfig::class.java)
                    }.getOrNull() != null) ||
                (launchExtras.getBoolean(INTENT_RUN_JAR, false) &&
                    runCatching {
                        launchExtras.getParcelableSafely(INTENT_JAR_INFO, JvmLaunchInfo::class.java)
                    }.getOrNull() != null)
            )
        if (!hasLaunchParam) {
            finish()
            return
        }

        //进入对局就记一笔：启动器靠它判断「返回游戏」这个入口要不要显示。
        //之后用心跳定时刷新，游戏结束 / 进程被杀后心跳停止，入口会自动消失
        if (intent.getBooleanExtra(INTENT_RUN_GAME, false)) {
            markGameSession(this)
            gameSessionHandler.post(gameSessionHeartbeat)
        }
        //加载渲染器
        Renderers.init()
        //加载插件
        PluginLoader.loadAllPlugins(this, false)
        refreshData()

        //初始化物理鼠标连接检查器
        PhysicalMouseChecker.initChecker(this)

        //启动前台服务，防止后台网络中断
        startForegroundService(Intent(this, GameService::class.java))

        //上面的启动参数校验已经拦住空壳；这里只做防御性兜底，绝不再抛异常闪退
        val bundle = intent.extras ?: run {
            finish()
            return
        }

        vmViewModel.initSession(
            activity = this,
            bundle = bundle,
            errorViewModel = errorViewModel,
            eventViewModel = eventViewModel,
            gamepadViewModel = gamepadViewModel,
            exitListener = { exitCode: Int, isSignal: Boolean ->
                stopAllService()
                if (exitCode != 0) {
                    val logPath = withLauncher {
                        getLogFile().absolutePath
                    }
                    showExitMessage(this@VMActivity, exitCode, isSignal, logPath)
                } else {
                    //重启启动器
                    ProcessPhoenix.triggerRebirth(this@VMActivity)
                }
            }
        )

        //设置画面渲染输出回调
        CallbackBridge.setGraphicOutputListener {
            withHandler { onGraphicOutput() }
        }

        window?.apply {
            setBackgroundDrawable(NativeColor.BLACK.toDrawable())
            if (AllSettings.sustainedPerformance.getValue()) {
                setSustainedPerformanceMode(true)
            }
            addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // 防止系统息屏
        }

        val logFile = withLauncher { getLogFile() }
        logFile.parentFile?.mkdirs() // 换过一次日志文件路径，此处创建父目录是必要的
        if (!logFile.exists() && !logFile.createNewFile()) throw IOException("Failed to create a new log file")
        LoggerBridge.start(logFile.absolutePath)

        //错误信息展示
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                errorViewModel.errorEvents.collect { tm ->
                    errorViewModel.showErrorDialog(
                        context = this@VMActivity,
                        tm = tm
                    )
                }
            }
        }

        lifecycleScope.launch {
            //开始接收事件
            eventViewModel.events.collect { event ->
                when (event) {
                    is EventViewModel.Event.Game.RefreshSize -> {
                        vmViewModel.onConfigurationChanged()
                    }
                    is EventViewModel.Event.Game.SwitchIme -> {
                        vmViewModel.textInputMode = event.mode ?: vmViewModel.textInputMode.switch()
                    }
                    is EventViewModel.Event.Game.KeyHandle -> {
                        vmViewModel.keyHandle = event.handle
                    }
                    is EventViewModel.Event.ShowToast -> {
                        Toast.makeText(
                            this@VMActivity,
                            event.text.toAndroidString(this@VMActivity),
                            event.duration
                        ).show()
                    }
                    else -> { /* Ignore */ }
                }
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (vmViewModel.textInputMode == TextInputMode.ENABLE) {
                    //那应该是想退出输入框了
                    vmViewModel.disableInputMode()
                    return
                }
                //⚠️ 以前这里 `if (!keyHandle) return`：编辑控制布局时 keyHandle 为 false，
                //返回键被**静默吞掉**，用户卡在编辑界面里出不来。
                //现在一律把返回事件交给 GameScreen 判断（它会先处理编辑模式再处理菜单）。
                eventViewModel.sendEvent(EventViewModel.Event.Game.OnBack)
            }
        })

        //关闭菜单之后，每次启动游戏都提醒，防止部分人误触了不知道怎么解决 >:(
        if (!AllSettings.showMenuBall.getValue()) {
            Toast.makeText(
                this@VMActivity,
                getString(R.string.game_menu_option_show_menu_hided),
                Toast.LENGTH_LONG
            ).show()
        }

        setContent {
            ZalithLauncherTheme {
                ObserveFullScreenSetting(AllSettings.gameFullScreen.state)
                Screen {
                    withHandler {
                        ComposableLayout(vmViewModel.textInputMode)
                    }

                    if (vmViewModel.textInputMode == TextInputMode.ENABLE) {
                        //输入栏控制区域
                        HidableInputLayout(
                            onSend = { text ->
                                vmViewModel.sendInputText(text)
                            },
                            onBackspace = {
                                vmViewModel.sendBackspace()
                            },
                            onEnter = {
                                vmViewModel.sendEnder()
                            },
                            onClose = {
                                vmViewModel.textInputMode = TextInputMode.DISABLE
                            }
                        )
                    }

                    //鼠标变更为抓获模式时，应该关闭输入框
                    val cursorMode by ZLBridgeStates.cursorMode.collectAsStateWithLifecycle()
                    LaunchedEffect(cursorMode) {
                        if (cursorMode == CURSOR_DISABLED) vmViewModel.disableInputMode()
                    }

                    val operation by vmViewModel.openFolderOperation.collectAsStateWithLifecycle()
                    OpenFolderLayer(
                        modifier = Modifier.fillMaxSize(),
                        operation = operation,
                        requestClose = {
                            vmViewModel.clearFolder()
                        },
                        lifecycleScope = lifecycleScope
                    )
                }
            }
        }
    }

    override fun getTaskDescriptionTitle(): String? {
        return runCatching {
            vmViewModel.session.activityTitle
        }.getOrNull()
    }

    override fun onResume() {
        super.onResume()
        //回到游戏时顺手刷一次心跳：避免从后台切回来时刚好卡在心跳间隔里，
        //让启动器误判会话即将过期（游戏仍在跑时必须继续显示「返回游戏」）
        if (intent.getBooleanExtra(INTENT_RUN_GAME, false)) markGameSession(this)
        withHandler { onResume() }
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_FOCUSED, 1)
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_HOVERED, 1)
    }

    override fun onPause() {
        super.onPause()
        withHandler { onPause() }
        CallbackBridge.resetInputState()
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_FOCUSED, 0)
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_HOVERED, 0)
    }

    override fun onStart() {
        super.onStart()
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_HOVERED, 1)
    }

    override fun onStop() {
        super.onStop()
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_HOVERED, 0)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            CallbackBridge.resetInputState()
        }
        CallbackBridge.nativeSetWindowAttrib(LwjglGlfwKeycode.GLFW_FOCUSED, if (hasFocus) 1 else 0)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        vmViewModel.onConfigurationChanged()
    }

    /**
     * SDL 会在窗口创建等时机按窗口宽高动态请求方向，导致有可能被设置为竖屏,
     * 在此处重写强制锁定为 sensorLandscape 即可
     */
    override fun setRequestedOrientation(requestedOrientation: Int) {
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE)
    }

    override fun onPostResume() {
        super.onPostResume()
        lifecycleScope.launch {
            if (vmViewModel.isRunning) {
                delay(50L.milliseconds)
                withContext(Dispatchers.Main) {
                    refreshWindowSize(screenSize = vmViewModel.screenSize)
                }
            }
        }
    }

    private fun refreshWindowSize(
        screenSize: IntSize
    ): IntSize {
        fun getDisplayPixels(pixels: Int): Int {
            return withHandler {
                when (type) {
                    HandlerType.GAME -> getDisplayFriendlyRes(pixels, AllSettings.resolutionRatio.getValue().toFloat() / 100f)
                    HandlerType.JVM -> getDisplayFriendlyRes(pixels, 0.8f)
                }
            }
        }

        val windowWidth = getDisplayPixels(screenSize.width)
        val windowHeight = getDisplayPixels(screenSize.height)
        applySizeToSurface?.invoke(windowWidth, windowHeight)
        ZLBridgeStates.onWindowChange()
        CallbackBridge.sendUpdateWindowSize(windowWidth, windowHeight)
        if (SdlBridge.sdlEnabled) {
            SDLActivity.getSDLSurface()?.let { surface ->
                surface.surfaceChanged()
                surface.nativeResize(windowWidth, windowHeight)
            }
        }

        return IntSize(windowWidth, windowHeight)
    }

    override fun onDestroy() {
        //先停心跳再清标记：否则 Handler 可能在清掉之后又写回来，留下僵尸标记
        gameSessionHandler.removeCallbacks(gameSessionHeartbeat)
        clearGameSession(this)
        stopAllService()
        withHandler { onDestroy() }
        SdlBridge.reset()
        super.onDestroy()
    }

    private fun stopAllService() {
        stopService(Intent(this, GameService::class.java))
        if (TerracottaVPNService.isRunning()) {
            val vpnIntent = Intent(this, TerracottaVPNService::class.java).apply {
                action = TerracottaVPNService.ACTION_STOP
            }
            startForegroundService(vpnIntent)
        }
    }

    @SuppressLint("RestrictedApi")
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (!vmViewModel.keyHandle) return super.dispatchKeyEvent(event)

        val isPressed = event.action == KeyEvent.ACTION_DOWN

        val code = AllSettings.physicalKeyImeCode.state
        if (isPressed && code != null && event.keyCode == code) {
            //用户按下了绑定呼出输入法的按键
            //开启或关闭输入法
            vmViewModel.textInputMode = vmViewModel.textInputMode.switch()
            return true
        }
        if (vmViewModel.textInputMode == TextInputMode.ENABLE) {
            if (isPressed && !vmViewModel.keyCanHandle(event)) {
                return super.dispatchKeyEvent(event)
            }

            if (isPressed) {
                vmViewModel.handleSpecialKey(event)
            }

            if (event.keyCode == KeyEvent.KEYCODE_TAB) {
                //对于Tab键，为了避免选中其他的组件，这里应该直接拦截
                return true
            }
            //在输入文本的时候，应该避免继续处理按键事件
            //否则输入法的一些功能键会失效
            return super.dispatchKeyEvent(event)
        }
        event.device?.let {
            val source = event.source
            if (source and InputDevice.SOURCE_MOUSE_RELATIVE == InputDevice.SOURCE_MOUSE_RELATIVE ||
                source and InputDevice.SOURCE_MOUSE == InputDevice.SOURCE_MOUSE) {

                if (event.keyCode == KeyEvent.KEYCODE_BACK) {
                    //一些系统会将鼠标右键当成KEYCODE_BACK来处理，需要在这里进行拦截
                    //然后发送真实的鼠标右键
                    withHandler { sendMouseRight(isPressed) }
                    return false
                }
            }
        }
        withHandler {
            if (shouldIgnoreKeyEvent(event)) {
                return super.dispatchKeyEvent(event)
            }
        }
        return true
    }

    @Keep
    fun messageboxShowMessageBox(
        flags: Int,
        title: String?,
        message: String?,
        buttonFlags: IntArray,
        buttonIds: IntArray,
        buttonTexts: Array<String?>,
        colors: IntArray?
    ): Int = SDLActivity.messageboxShowMessageBox(
        this, flags, title, message, buttonFlags, buttonIds, buttonTexts, colors
    )

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        val nativeSurface = Surface(surface)
        SdlBridge.prepareSurface(this, nativeSurface, gameSurfaceView?.parent as? ViewGroup, surface)
        //游戏请求 GLFW direct gamepad 时的通知接收方
        CallbackBridge.setDirectGamepadEnableHandler {
            LoggerBridge.append("ZalithLauncher: Direct gamepad handler enabled")
        }
        if (vmViewModel.isRunning) {
            ZLBridge.setupBridgeWindow(nativeSurface)
            //surface 被重建（旋转 / 分屏 / 小窗 / 回到前台）后必须恢复渲染状态：
            //1）清掉销毁标记，否则下面的 onSurfaceTextureSizeChanged / surfaceChanged
            //   会被那个 `if (mIsSurfaceDestroyed) return` 全部挡掉，尺寸再也回不来；
            //2）主动按当前屏幕尺寸重新刷新一次，让新 SurfaceTexture 的 buffer
            //   与视图尺寸重新对齐（否则画面保持错位、无法自愈）。
            withHandler { mIsSurfaceDestroyed = false }
            val runningSize = vmViewModel.screenSize
            if (runningSize.width > 0 && runningSize.height > 0) {
                refreshWindowSize(screenSize = runningSize)
            }
            return
        }
        vmViewModel.isRunning = true

        withHandler { mIsSurfaceDestroyed = false }
        lifecycleScope.launch(Dispatchers.Default) {
            val screenSize = vmViewModel.screenSizeBridge.awaitData()
            val currentSize = refreshWindowSize(screenSize = screenSize)
            withHandler {
                execute(
                    surface = Surface(surface),
                    screenSize = currentSize,
                    scope = lifecycleScope
                )
            }
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        if (withHandler { mIsSurfaceDestroyed }) return
        refreshWindowSize(screenSize = IntSize(width, height))
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        val nativeSurface = SDLSurface.getNativeSurface()
        if (SdlBridge.beginSurfaceDestroy(surface, nativeSurface)) {
            if (SdlBridge.sdlEnabled) {
                SDLActivity.getSDLSurface()?.surfaceDestroyed()
            }
            SdlBridge.unregisterSurface(nativeSurface)
        }
        withHandler { mIsSurfaceDestroyed = true }
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (withHandler { mIsSurfaceDestroyed }) return
        refreshWindowSize(screenSize = IntSize(width, height))
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        val surface = holder.surface
        SdlBridge.prepareSurface(this, surface, gameSurfaceView?.parent as? ViewGroup, holder)
        if (vmViewModel.isRunning) {
            ZLBridge.setupBridgeWindow(surface)
            //同 onSurfaceTextureAvailable：SurfaceView 路径也要清掉销毁标记并重新对齐尺寸，
            //否则窗口复用后画面错位不能自愈（原因见上面的注释）。
            withHandler { mIsSurfaceDestroyed = false }
            val runningSize = vmViewModel.screenSize
            if (runningSize.width > 0 && runningSize.height > 0) {
                refreshWindowSize(screenSize = runningSize)
            }
            return
        }
        vmViewModel.isRunning = true

        withHandler { mIsSurfaceDestroyed = false }
        lifecycleScope.launch(Dispatchers.Default) {
            val screenSize = vmViewModel.screenSizeBridge.awaitData()
            val currentSize = refreshWindowSize(screenSize = screenSize)
            withHandler {
                execute(
                    surface = surface,
                    screenSize = currentSize,
                    scope = lifecycleScope
                )
            }
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        val nativeSurface = holder.surface
        if (SdlBridge.beginSurfaceDestroy(holder, nativeSurface)) {
            if (SdlBridge.sdlEnabled) {
                SDLActivity.getSDLSurface()?.surfaceDestroyed(holder)
            }
            SdlBridge.unregisterSurface(nativeSurface)
        }
        withHandler { mIsSurfaceDestroyed = true }
    }

    @Composable
    private fun Screen(
        content: @Composable () -> Unit = {}
    ) {
        val imeInsets = WindowInsets.ime
        val inputArea by withHandler { inputArea }.collectAsStateWithLifecycle()

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val screenSize = rememberBoxSize()

            val changed by vmViewModel.onConfigurationChanged.collectAsStateWithLifecycle()
            LaunchedEffect(screenSize, changed) {
                vmViewModel.screenSize = screenSize
                vmViewModel.screenSizeBridge.provideData(screenSize)
                if (changed) {
                    refreshWindowSize(screenSize = screenSize)
                    vmViewModel.onConfigurationChanged(false)
                }
            }

            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .absoluteOffset {
                        val area = inputArea ?: return@absoluteOffset IntOffset.Zero
                        val imeHeight = imeInsets.getBottom(this@absoluteOffset)
                        val bottomDistance = screenSize.height - area.bottom
                        val bottomPadding = (imeHeight - bottomDistance).coerceAtLeast(0)
                        IntOffset(0, -bottomPadding)
                    },
                factory = { context ->
                    val view = if (AllSettings.useSurfaceView.getValue()) {
                        //使用 SurfaceView 渲染
                        SurfaceView(context).apply {
                            holder.addCallback(this@VMActivity)
                            // SDL 模式需要父 ViewGroup（输入法 EditText 附加用）
                            gameSurfaceView = this
                        }.also { surface ->
                            applySizeToSurface = { width, height ->
                                surface.holder.setFixedSize(width, height)
                            }
                        }
                    } else {
                        TextureView(context).apply {
                            isOpaque = true
                            alpha = 1.0f

                            surfaceTextureListener = this@VMActivity
                        }.also { texture ->
                            gameSurfaceView = texture
                            applySizeToSurface = { width, height ->
                                texture.surfaceTexture?.setDefaultBufferSize(width, height)
                            }
                        }
                    }
                    view.setOnApplyWindowInsetsListener { v, insets ->
                        if (SdlBridge.sdlEnabled && android.os.Build.VERSION.SDK_INT >= 30) {
                            SDLActivity.notifyImeVisibilityChanged(
                                insets.isVisible(android.view.WindowInsets.Type.ime())
                            )
                        }
                        v.onApplyWindowInsets(insets)
                    }
                    view
                }
            )

            content()
        }
    }
}

/**
 * 让VMActivity进入运行游戏模式
 * @param version 指定版本
 */
fun runGame(
    context: Context,
    version: Version,
    account: Account,
) {
    val intent = Intent(context, VMActivity::class.java).apply {
        putExtra(INTENT_RUN_GAME, true)
        putExtra(INTENT_GAME_CONFIG, LaunchConfig(version, account))
    }
    context.startActivity(intent)
}

/**
 * 让VMActivity进入运行Jar模式
 * @param jarFile 指定 jar 文件
 * @param jreName 指定使用的 Java 环境，null 则为自动选择
 * @param customArgs 指定 jvm 参数
 */
fun runJar(
    context: Context,
    jarFile: File,
    jreName: String? = null,
    customArgs: String? = null
) {
    RuntimesManager.getExactJreName(8) ?: run {
        Toast.makeText(context, R.string.multirt_no_java_8, Toast.LENGTH_SHORT).show()
        return
    }

    val jvmArgsPrefix = customArgs?.let { "$it " } ?: ""
    val jvmArgs = "$jvmArgsPrefix-jar ${jarFile.absolutePath}"

    val jvmLaunchInfo = JvmLaunchInfo(
        jvmArgs = jvmArgs,
        jreName = jreName
    )

    val intent = Intent(context, VMActivity::class.java).apply {
        putExtra(INTENT_RUN_JAR, true)
        putExtra(INTENT_JAR_INFO, jvmLaunchInfo)
    }
    context.startActivity(intent)
}
/** 会话心跳间隔：`:game` 进程每 25s 刷一次标记（20–30s 之间，兼顾及时性与 IO 次数） */
private const val GAME_SESSION_HEARTBEAT_MS = 25_000L

/** 心跳有效期：超过 90s 没刷新就认为会话已经结束（约 3 个心跳周期，容忍系统调度抖动） */
private const val GAME_SESSION_TIMEOUT_MS = 90_000L

/**
 * 本次「对局」的心跳标记文件。
 *
 * 只用 `:game` 进程是否存活判断是不够的：游戏退出后 Android 会把这个进程留在 cached 状态
 * 一段时间（个别 OEM 甚至上报不准确的 importance），界面就会一直显示「返回游戏」，
 * 点进去却是一个空壳（闪退）。所以这里改成**心跳**：
 * [VMActivity] 进入对局后用主线程 Handler 每 [GAME_SESSION_HEARTBEAT_MS] 刷新一次时间戳，
 * [VMActivity.onDestroy] 时停止并删除；主进程据此判断「游戏界面实例是否还活着」。
 */
private fun gameSessionFlag(context: Context): File = File(context.filesDir, "game_session.running")

/**
 * 刷新一次会话心跳（由 `:game` 进程写入，主进程读同一个目录）。
 *
 * 内容用 [SystemClock.elapsedRealtime]（开机后的毫秒数，跨进程一致、单调递增）：
 * 系统时间被改动不会影响判活；设备重启后新值一定小于旧值，旧标记会被当作过期心跳自愈清理。
 */
fun markGameSession(context: Context) {
    runCatching {
        gameSessionFlag(context).writeText(SystemClock.elapsedRealtime().toString())
    }
}

/** 对局结束 / 界面销毁时清掉标记 */
fun clearGameSession(context: Context) {
    runCatching { gameSessionFlag(context).delete() }
}

/**
 * 游戏是否还在后台跑（决定启动器要不要显示「返回游戏」）。
 *
 * 判据：**心跳新鲜** 且 `:game` 进程仍存活：
 * - 标记文件由 [VMActivity] 持续刷新时间戳；Activity 结束 / 进程被杀后心跳停止；
 * - 超过 [GAME_SESSION_TIMEOUT_MS] 没刷新 = 会话已经结束，残留标记直接删掉（自愈）；
 * - 进程检查作为更快的兜底：进程都没了就不用等心跳过期。
 *
 * ⚠️ 不再用 `RunningAppProcessInfo.importance` 判断：cached 进程能停留很久，个别 OEM
 * 对该值的上报也不可靠。心跳时间戳是「VMActivity 实例还活着」的直接证据，
 * 游戏切后台（Activity 仍在、Handler 继续跑）时心跳正常，不会误隐藏。
 */
fun isGameProcessAlive(context: Context): Boolean {
    val flag = gameSessionFlag(context)
    if (!flag.isFile) return false

    //心跳时间戳解析失败 / 过期：上一次会话留下的残标，删掉（自愈）
    val heartbeat = runCatching { flag.readText().trim().toLong() }.getOrNull() ?: run {
        clearGameSession(context)
        return false
    }
    val age = SystemClock.elapsedRealtime() - heartbeat
    if (age !in 0..GAME_SESSION_TIMEOUT_MS) {
        clearGameSession(context)
        return false
    }

    //读不到进程列表（权限 / 系统限制）时以心跳为准，避免把还在跑的游戏误隐藏
    val processes = runCatching {
        (context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager)
            ?.runningAppProcesses
    }.getOrNull() ?: return true

    val alive = processes.any { it.processName == context.packageName + ":game" }
    if (!alive) {
        //进程已经没了：标记是残留，删掉（自愈）
        clearGameSession(context)
    }
    return alive
}

/**
 * 游戏内 → 启动器主界面，**不停止游戏**。
 *
 * 只把 [MainActivity] 重新排到任务栈最前面（`REORDER_TO_FRONT`），
 * `VMActivity` 退到它后面继续存活：JVM 不退出、联机不断、日志不丢，
 * 用户从「返回游戏」回来时还是原来那一局。
 */
fun backToLauncher(context: Context) {
    val intent = Intent(context, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        if (context !is android.app.Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(intent) }
}

/**
 * 启动器 → 正在运行的游戏，**不重新启动**。
 *
 * 与 [backToLauncher] 对称：只把已有的 [VMActivity] 实例排到最前面。
 *
 * ⚠️ 这里必须**先校验会话还在**再拉起：游戏已经退出、旧实例已经销毁时，
 * `REORDER_TO_FRONT` 会退化成一次普通启动，系统会新建一个**不带启动参数的 VMActivity 空壳**，
 * 以前点「返回游戏」直接闪退就是这么来的。
 *
 * @return 是否已把游戏切回前台；false = 会话已经结束，调用方应立即收起入口并提示用户
 */
fun resumeGame(context: Context): Boolean {
    //轮询有 2s 间隔，点击的这一刻可能刚好游戏已经退出，所以这里当场再校验一次
    if (!isGameProcessAlive(context)) return false
    val intent = Intent(context, VMActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        if (context !is android.app.Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    return runCatching { context.startActivity(intent) }.isSuccess
}
