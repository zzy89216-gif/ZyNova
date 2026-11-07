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

package com.movtery.zalithlauncher.setting

import android.os.Build
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.materialkolor.PaletteStyle
import com.movtery.layer_controller.utils.snap.SnapMode
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.game.download.assets.platform.SearchPlatform
import com.movtery.zalithlauncher.game.path.GamePathManager
import com.movtery.zalithlauncher.game.version.installed.GraphicsApi
import com.movtery.zalithlauncher.setting.enums.ActionMenuSide
import com.movtery.zalithlauncher.setting.enums.AppLanguage
import com.movtery.zalithlauncher.setting.enums.DarkMode
import com.movtery.zalithlauncher.setting.enums.GestureActionType
import com.movtery.zalithlauncher.setting.enums.GamepadInputMode
import com.movtery.zalithlauncher.setting.enums.HomeButtonMaterial
import com.movtery.zalithlauncher.setting.enums.HomePageType
import com.movtery.zalithlauncher.setting.enums.MirrorSourceType
import com.movtery.zalithlauncher.game.renderer.AutoRenderer
import com.movtery.zalithlauncher.game.renderer.mobileglues.MobileGluesGlVersion
import com.movtery.zalithlauncher.game.renderer.mobileglues.MobileGluesMultidraw
import com.movtery.zalithlauncher.setting.enums.MouseControlMode
import com.movtery.zalithlauncher.setting.enums.UiGeneration
import com.movtery.zalithlauncher.ui.control.HotbarRule
import com.movtery.zalithlauncher.ui.control.gamepad.JoystickMode
import com.movtery.zalithlauncher.ui.control.mouse.CENTER_HOTSPOT
import com.movtery.zalithlauncher.ui.control.mouse.CursorHotspot
import com.movtery.zalithlauncher.ui.control.mouse.LEFT_TOP_HOTSPOT
import com.movtery.zalithlauncher.ui.theme.ColorThemeType
import com.movtery.zalithlauncher.utils.animation.TransitionAnimationType

object AllSettings : SettingsRegistry() {
    /**
     * 标题栏左上角显示的启动器名称（议题 #18）
     *
     * 留空 = 使用默认的 `BuildKeys.LAUNCHER_IDENTIFIER`（即 gradle.properties 里的
     * `launcher_name`）。读取请统一走 `launcherDisplayTitle`，
     * 不要直接用这里的状态 —— 否则「留空」会在界面上显示成一片空白。
     */
    val launcherTitle = stringSetting("launcherTitle", "")

    //Renderer
    /**
     * 全局渲染器
     *
     * 默认 **「自动」**：每个实例启动时按偏好顺序挑一个支持该 Minecraft 版本的渲染器
     * （见 `game/renderer/AutoRenderer.kt` 与 `Renderers.resolveIdentifier`）。
     *
     * 之所以默认自动而不是直接写死某一个：内置渲染器的版本覆盖范围不同 ——
     * MobileGlues 声明 `minMCVer = 1.17`，GL4ES 声明 `maxMCVer = 1.21.4`。
     * 固定默认某一个，必然对一部分 Minecraft 版本是错的（旧版本实例每次启动都要先吃一次
     * 「渲染器可能不兼容」的警告）。自动模式把这个选择交给启动器。
     *
     * ⚠️ 这个默认值**只对新用户生效**：老用户配置里已经写死了 `renderer` 键，
     * 启动器不会去改它。
     */
    val renderer = stringSetting("renderer", AutoRenderer.IDENTIFIER)

    /**
     * Vulkan 驱动器
     */
    val vulkanDriver = stringSetting("vulkanDriver", "default turnip")

    /**
     * 图形 API（Minecraft 26.2+）
     */
    val graphicsApi = enumSetting("graphicsApi", GraphicsApi.DEFAULT_OPENGL)

    /**
     * 分辨率
     */
    val resolutionRatio = intSetting("resolutionRatio", 100, 25..300)

    /**
     * 游戏页面全屏化
     */
    val gameFullScreen = boolSetting("gameFullScreen", true)

    /**
     * 使用 SurfaceView 渲染
     */
    val useSurfaceView = boolSetting("useSurfaceView", false)

    /**
     * 持续性能模式
     */
    val sustainedPerformance = boolSetting("sustainedPerformance", false)

    /**
     * 使用系统的 Vulkan 驱动
     */
    val zinkPreferSystemDriver = boolSetting("zinkPreferSystemDriver", false)

    /**
     * Zink 垂直同步
     */
    val vsyncInZink = boolSetting("vsyncInZink", false)

    /**
     * 启用着色器日志输出
     */
    val dumpShaders = boolSetting("dumpShaders", false)

    //MobileGlues
    //MobileGlues 的 OpenGL 版本**不是**环境变量，而是它自己 config.json 里的
    //`customGLVersion` 整数；启动游戏前由 MobileGluesConfig 写进 MG_DIR_PATH 下。
    //存储的是 MobileGluesGlVersion 的枚举名。

    /**
     * MobileGlues 的 OpenGL 目标版本
     *
     * 默认 4.0，与 MobileGlues 官方默认（native `DEFAULT_GL_VERSION = 40`）一致。
     */
    val mobileGluesGlVersion = stringSetting(
        "mobileGluesGlVersion",
        MobileGluesGlVersion.default.name
    )

    /**
     * MobileGlues 是否启用 ANGLE 渲染后端（`config.json` 的 `enableANGLE`）
     *
     * 默认 **关闭**（写 `DisableIfPossible`）。
     *
     * 原因：ANGLE 是「严格 ES 宿主」，MC 26.x 的透明合成管线会把 D32F 深度纹理
     * 绑定到 `MIPMAP_LINEAR` 采样器上，而 ES 3.0 规定深度纹理在这种过滤器下
     * 是 filter-incomplete —— 采样恒为 0，透明排序整体退化
     * （云穿透地形、水/粒子/天气/火焰遮挡异常）。
     * 上游 MobileGlues 已确认该问题（issue #57），修复仍在未合并的 PR #61 里，
     * 官方二进制暂时没有。
     *
     * 因此这里把「用不用 ANGLE」交给用户：
     * - 关闭（默认）：走设备原生 GLES 驱动，避开上述严格 ES 路径；
     * - 打开：需要 ANGLE 的设备（个别驱动原生 GLES 有问题）可以自行开启。
     *
     * ⚠️ 这只是**绕过**已知的深度纹理缺陷，不是修复 MobileGlues 本身。
     */
    val mobileGluesUseAngle = boolSetting("mobileGluesUseAngle", false)

    //Game
    /**
     * 版本隔离
     */
    val versionIsolation = boolSetting("versionIsolation", true)

    /**
     * 不检查游戏完整性
     */
    val skipGameIntegrityCheck = boolSetting("skipGameIntegrityCheck", false)

    /**
     * 版本自定义信息
     */
    val versionCustomInfo = stringSetting("versionCustomInfo", "${BuildKeys.LAUNCHER_IDENTIFIER}[zl_version]")

    /**
     * 启动器的Java环境
     */
    val javaRuntime = stringSetting("javaRuntime", "")

    /**
     * 自动选择Java环境
     */
    val autoPickJavaRuntime = boolSetting("autoPickJavaRuntime", true)

    /**
     * 游戏内存分配大小
     */
    val ramAllocation = intSetting("ramAllocation", null, min = 256)

    /**
     * 自定义Jvm启动参数
     */
    val jvmArgs = stringSetting("jvmArgs", "")

    /**
     * 已禁用的原生库插件列表
     */
    val disableNativeLibPlugins = stringListSetting("nativeLibPlugins", emptyList())

    /**
     * 启动游戏时自动展示日志，直到游戏开始渲染
     */
    val showLogAutomatic = boolSetting("showLogAutomatic", false)

    /**
     * 日志字体大小
     */
    val logTextSize = intSetting("logTextSize", 15, 5..20)

    /**
     * 日志缓冲区刷新时间
     */
    val logBufferFlushInterval = intSetting("logBufferFlushInterval", 200, 100..1000)

    // ── 渲染器高级参数（设置 → 高级设置）────────────────────────────
    //
    //议题：各渲染器的「所有可改参数」都要能改，并且集中放在高级选项里。
    //下面这些以前是写死在代码里的，现在提升为设置项；
    //默认值 = 以前的写死值，所以升级后行为不变。

    //GL4ES（opengles2 分支的 5 个环境变量）
    /** LIBGL_MIPMAP：纹理 mipmap 生成级别，0 = 关闭 */
    val gl4esMipmap = intSetting("gl4esMipmap", 3, 0..3)

    /** LIBGL_NOERROR：关闭 GL 错误检查，快一点但更难排查 */
    val gl4esNoError = boolSetting("gl4esNoError", true)

    /** LIBGL_NOINTOVLHACK：避免整数溢出 hack 带来的兼容问题 */
    val gl4esNoIntOverflowHack = boolSetting("gl4esNoIntOverflowHack", true)

    /** LIBGL_NORMALIZE：自动归一化法线，老游戏更不容易黑屏 */
    val gl4esNormalize = boolSetting("gl4esNormalize", true)

    //MobileGlues（写进它自己的 config.json）
    /** enableNoError：关闭 GL 错误检查 */
    val mobileGluesNoError = boolSetting("mobileGluesNoError", false)

    /** enableExtTimerQuery：时间查询扩展 */
    val mobileGluesExtTimerQuery = boolSetting("mobileGluesExtTimerQuery", true)

    /** enableExtComputeShader：计算着色器扩展 */
    val mobileGluesExtComputeShader = boolSetting("mobileGluesExtComputeShader", false)

    /** enableExtDirectStateAccess：直接状态访问扩展 */
    val mobileGluesExtDirectStateAccess = boolSetting("mobileGluesExtDirectStateAccess", false)

    /** maxGlslCacheSize：着色器缓存上限（MiB） */
    val mobileGluesMaxGlslCacheSize = intSetting("mobileGluesMaxGlslCacheSize", 32, 4..512)

    /** angleDepthClearFixMode：ANGLE 深度清除修正（0 关闭 / 1 开启） */
    val mobileGluesAngleDepthClearFix = boolSetting("mobileGluesAngleDepthClearFix", false)

    /** fsr1Setting：FSR1 超分辨率 */
    val mobileGluesFsr1 = boolSetting("mobileGluesFsr1", false)

    /** multidrawOrder：全局 multidraw 策略顺序（逗号分隔，native 表示用驱动原生实现） */
    val mobileGluesMultidrawOrder = stringSetting(
        "mobileGluesMultidrawOrder",
        MobileGluesMultidraw.DEFAULT_ORDER_VALUE
    )

    /** glMultiDrawArrays 单独覆盖顺序 */
    val mobileGluesMultidrawArraysEnabled = boolSetting("mobileGluesMultidrawArraysEnabled", false)
    val mobileGluesMultidrawArraysOrder = stringSetting(
        "mobileGluesMultidrawArraysOrder",
        MobileGluesMultidraw.serialize(MobileGluesMultidraw.OVERRIDES[0].defaultOrder)
    )

    /** glMultiDrawElements 单独覆盖顺序 */
    val mobileGluesMultidrawElementsEnabled = boolSetting("mobileGluesMultidrawElementsEnabled", false)
    val mobileGluesMultidrawElementsOrder = stringSetting(
        "mobileGluesMultidrawElementsOrder",
        MobileGluesMultidraw.serialize(MobileGluesMultidraw.OVERRIDES[1].defaultOrder)
    )

    /** glMultiDrawElementsBaseVertex 单独覆盖顺序 */
    val mobileGluesMultidrawElementsBaseVertexEnabled = boolSetting("mobileGluesMultidrawElementsBaseVertexEnabled", false)
    val mobileGluesMultidrawElementsBaseVertexOrder = stringSetting(
        "mobileGluesMultidrawElementsBaseVertexOrder",
        MobileGluesMultidraw.serialize(MobileGluesMultidraw.OVERRIDES[2].defaultOrder)
    )

    /** glMultiDrawArraysIndirect 单独覆盖顺序 */
    val mobileGluesMultidrawArraysIndirectEnabled = boolSetting("mobileGluesMultidrawArraysIndirectEnabled", false)
    val mobileGluesMultidrawArraysIndirectOrder = stringSetting(
        "mobileGluesMultidrawArraysIndirectOrder",
        MobileGluesMultidraw.serialize(MobileGluesMultidraw.OVERRIDES[3].defaultOrder)
    )

    /** glMultiDrawElementsIndirect 单独覆盖顺序 */
    val mobileGluesMultidrawElementsIndirectEnabled = boolSetting("mobileGluesMultidrawElementsIndirectEnabled", false)
    val mobileGluesMultidrawElementsIndirectOrder = stringSetting(
        "mobileGluesMultidrawElementsIndirectOrder",
        MobileGluesMultidraw.serialize(MobileGluesMultidraw.OVERRIDES[4].defaultOrder)
    )

    //LTW（实验性）
    //LTW 原生库只读取环境变量，下面这些开关会直接注入游戏进程。

    /** 禁止显式 buffer flush（LTW 默认开启） */
    val ltwNeverFlushBuffers = boolSetting("ltwNeverFlushBuffers", true)

    /** 强制动态 storage buffer 保持一致（LTW 默认开启） */
    val ltwCoherentDynamicStorage = boolSetting("ltwCoherentDynamicStorage", true)

    /** 对游戏隐藏 buffer storage 扩展（兼容性开关） */
    val ltwHideBufferStorage = boolSetting("ltwHideBufferStorage", false)

    /** 关闭 GL 错误检查（LIBGL_NOERROR） */
    val ltwNoError = boolSetting("ltwNoError", false)

    /** 着色器转换时检查 NaN（排障用） */
    val ltwShaderConvCheckNan = boolSetting("ltwShaderConvCheckNan", false)

    /** LTW 调试日志 */
    val ltwDebug = boolSetting("ltwDebug", false)

    /** LTW GL 错误追踪日志 */
    val ltwGlErrorTrace = boolSetting("ltwGlErrorTrace", false)

    //Control
    /**
     * 实体鼠标控制
     */
    val physicalMouseMode = boolSetting("physicalMouseMode", true)

    /**
     * 按键键值，按下按键呼出输入法
     */
    val physicalKeyImeCode = intSetting("physicalKeyImeCode", null)

    /**
     * 隐藏虚拟鼠标
     */
    val hideMouse = boolSetting("hideMouse", false)

    /**
     * 虚拟鼠标大小（Dp）
     */
    val mouseSize = intSetting("mouseSize", 24, 5..50)

    /**
     * 虚拟鼠标箭头热点坐标
     */
    val arrowMouseHotspot = parcelableSetting("arrowMouseHotspot", LEFT_TOP_HOTSPOT)

    /**
     * 虚拟鼠标链接选择热点坐标
     */
    val linkMouseHotspot = parcelableSetting("linkMouseHotspot", CursorHotspot(xPercent = 23, yPercent = 0))

    /**
     * 虚拟鼠标输入选择热点坐标
     */
    val iBeamMouseHotspot = parcelableSetting("iBeamMouseHotspot", CENTER_HOTSPOT)

    /**
     * 虚拟鼠标十字热点坐标
     */
    val crossHairMouseHotspot = parcelableSetting("crossHairMouseHotspot", CENTER_HOTSPOT)

    /**
     * 虚拟鼠标调整大小（上下）热点坐标
     */
    val resizeNSMouseHotspot = parcelableSetting("resizeNSMouseHotspot", CENTER_HOTSPOT)

    /**
     * 虚拟鼠标调整大小（左右）热点坐标
     */
    val resizeEWMouseHotspot = parcelableSetting("resizeEWMouseHotspot", CENTER_HOTSPOT)

    /**
     * 虚拟鼠标调整大小（全部方向）热点坐标
     */
    val resizeAllMouseHotspot = parcelableSetting("resizeAllMouseHotspot", CENTER_HOTSPOT)

    /**
     * 虚拟鼠标禁止/无效操作热点坐标
     */
    val notAllowedMouseHotspot = parcelableSetting("notAllowedMouseHotspot", CENTER_HOTSPOT)

    /**
     * 虚拟鼠标灵敏度
     */
    val cursorSensitivity = intSetting("cursorSensitivity", 100, 25..300)

    /**
     * 被抓获指针移动灵敏度
     */
    val mouseCaptureSensitivity = intSetting("mouseCaptureSensitivity", 100, 25..300)

    /**
     * 虚拟鼠标控制模式
     */
    val mouseControlMode = enumSetting("mouseControlMode", MouseControlMode.SLIDE)

    /**
     * 鼠标控制长按延迟
     */
    val mouseLongPressDelay = intSetting("mouseLongPressDelay", 300, 100..1000)

    /**
     * 是否开启虚拟鼠标点击操作
     */
    val enableMouseClick = boolSetting("enableMouseClick", true)

    /**
     * 是否启用手柄控制
     */
    val gamepadControl = boolSetting("gamepadControl", true)

    /**
     * SDL 下是否允许自动唤起输入法
     */
    val sdlAutoShowIme = boolSetting("sdlAutoShowIme", true)

    /**
     * 手柄输入模式（映射虚拟按键 / SDL 直通）
     */
    val gamepadInputMode = enumSetting("gamepadInputMode", GamepadInputMode.Mapped)

    /**
     * 是否已完成手柄输入模式的选择询问
     */
    val gamepadInputModePrompted = boolSetting("gamepadInputModePrompted", false)

    /**
     * 摇杆死区缩放
     */
    val gamepadDeadZoneScale = intSetting("gamepadDeadZoneScale", 100, 50..200)

    /**
     * 手柄映射配置
     */
    val gamepadMappingConfig = stringSetting("gamepadMappingConfig", "default")

    /**
     * 摇杆控制模式
     */
    val joystickControlMode = enumSetting("joystickControlMode", JoystickMode.LeftMovement)

    /**
     * 手柄摇杆控制鼠标指针时的灵敏度
     */
    val gamepadCursorSensitivity = intSetting("gamepadCursorSensitivity", 100, 25..300)

    /**
     * 手柄摇杆控制游戏视角时的灵敏度
     */
    val gamepadCameraSensitivity = intSetting("gamepadCameraSensitivity", 100, 25..300)

    /**
     * 手势控制
     */
    val gestureControl = boolSetting("gestureControl", false)

    /**
     * 手势控制点击时触发的鼠标按钮
     */
    val gestureTapMouseAction = enumSetting("gestureTapMouseAction", GestureActionType.MOUSE_RIGHT)

    /**
     * 手势控制长按时触发的鼠标按钮
     */
    val gestureLongPressMouseAction = enumSetting("gestureLongPressMouseAction", GestureActionType.MOUSE_LEFT)

    /**
     * 手势控制长按延迟
     */
    val gestureLongPressDelay = intSetting("gestureLongPressDelay", 300, 100..1000)

    /**
     * 陀螺仪控制
     */
    val gyroscopeControl = boolSetting("gyroscopeControl", false)

    /**
     * 陀螺仪控制灵敏度
     */
    val gyroscopeSensitivity = intSetting("gyroscopeSensitivity", 100, 25..300)

    /**
     * 陀螺仪采样率
     */
    val gyroscopeSampleRate = intSetting("gyroscopeSampleRate", 16, 5..50)

    /**
     * 陀螺仪数值平滑
     */
    val gyroscopeSmoothing = boolSetting("gyroscopeSmoothing", true)

    /**
     * 陀螺仪平滑处理的窗口大小
     */
    val gyroscopeSmoothingWindow = intSetting("gyroscopeSmoothingWindow", 4, 2..10)

    /**
     * 反转 X 轴
     */
    val gyroscopeInvertX = boolSetting("gyroscopeInvertX", false)

    /**
     * 反转 Y 轴
     */
    val gyroscopeInvertY = boolSetting("gyroscopeInvertY", false)

    //Launcher
    /**
     * 颜色主题色
     * Android 12+ 默认动态主题色
     */
    val launcherColorTheme = enumSetting(
        "launcherColorTheme",
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) ColorThemeType.DYNAMIC
        else ColorThemeType.EMBERMIRE
    )

    /**
     * 自定义颜色主题色
     */
    val launcherCustomColor = intSetting("launcherCustomColor", Color.Blue.toArgb())

    /**
     * 自定义颜色配色风格
     */
    val launcherCustomPaletteStyle = enumSetting("launcherCustomPaletteStyle", PaletteStyle.TonalSpot)

    /**
     * 启动器UI深色主题
     */
    val launcherDarkMode = enumSetting("launcherDarkMode", DarkMode.FollowSystem)

    /**
     * 启动器语言
     */
    val launcherLanguage = enumSetting("launcherLanguage", AppLanguage.FOLLOW_SYSTEM)

    /**
     * 启动器部分屏幕全屏
     */
    val launcherFullScreen = boolSetting("launcherFullScreen", true)

    /**
     * 持续型节日彩蛋效果
     */
    val launcherFestivalEffects = boolSetting("launcherFestivalEffects", true)

    /**
     * 动画倍速
     */
    val launcherAnimateSpeed = intSetting("launcherAnimateSpeed", 5, 0..10)

    /**
     * 动画幅度
     */
    val launcherAnimateExtent = intSetting("launcherAnimateExtent", 5, 0..10)

    /**
     * 启动器页面切换动画类型
     */
    val launcherSwapAnimateType = enumSetting("launcherSwapAnimateType", TransitionAnimationType.JELLY_BOUNCE)

    /**
     * 减少动态效果（无障碍选项）
     *
     * 开启后会关闭页面切换动画、动态玻璃的流动高光、骨架屏闪烁等
     * 持续或幅度较大的动态效果，只保留必要的静态反馈：
     * 对动效敏感的用户可以彻底关掉这些效果，而不是逐个去调倍速。
     */
    val launcherReduceMotion = boolSetting("launcherReduceMotion", false)

    /**
     * 启动时显示光敏性警告
     *
     * ⚠️ **自 27.3.0 起已废弃，不再被任何代码读取或写入。**
     *
     * 27.2.0 曾引入「强效动态玻璃」档（已随 27.10.2 整体移除），它会产生持续的明暗脉动，
     * 因此配套了这个启动警告。27.3.0 已把该档位连同警告一起移除，
     * 应用内不再存在任何持续明暗变化，警告自然也不再需要。
     *
     * **定义必须保留**：这是旧配置兼容项 —— 删掉定义会让 MMKV 里的旧值成为孤儿，
     * 一旦以后重新加上同名设置，读取类型不匹配会直接导致启动异常
     * （与 [liquidGlass] 的处理方式一致，见交接文档「旧配置兼容」一节）。
     */
    val launcherPhotosensitivityWarning = boolSetting("launcherPhotosensitivityWarning", false)

    /**
     * 启动器背景元素不透明度
     */
    val launcherBackgroundOpacity = intSetting("launcherBackgroundOpacity", 80, 20..100)

    /**
     * 启动器视频背景音量
     */
    val videoBackgroundVolume = intSetting("videoBackgroundVolume", 0, 0..100)

    /**
     * 启动器背景模糊效果
     */
    val backgroundBlur = intSetting("backgroundBlur", 0, 0..40)

    /**
     * 玻璃材质强度（百分比）
     *
     * 27.10.2 起玻璃不再是「开关」，而是全局统一的单色材质：
     * 这里控制叠在卡片/面板上的低透明度渐变的强度（8 = 8%）。
     * 调低更接近纯色，调高玻璃感更强；读取方是 `Modifier.backgroundGlass()`。
     */
    val codexGlassStrength = intSetting("codexGlassStrength", 8, 2..16)

    /**
     * 启动器卡片圆角（dp）
     *
     * 统一驱动 `MaterialTheme.shapes`，个性化页与设置页读写同一个值。
     */
    val launcherCornerRadius = intSetting("launcherCornerRadius", 28, 12..36)

    /**
     * 首页按钮材质（议题 #33）
     *
     * 「默认 / 透明 / 液态玻璃」三档，默认**透明**。
     * 27.10.2 起玻璃材质是全应用统一的基础材质（不再是全局开关），
     * 只有开启「减少动态效果」时液态玻璃才会在 `rememberHomeButtonMaterial` 里降级为透明。
     */
    val homeButtonMaterial = enumSetting("homeButtonMaterial", HomeButtonMaterial.Transparent)

    /**
     * 界面世代（新版 / 旧版）
     *
     * 27.3.0 起提供两套并行的界面观感，用户可以**随时双向切换**：
     * - [UiGeneration.Modern]（默认）：重新设计的字阶与形状体系
     * - [UiGeneration.Classic]：与 27.3.0 之前完全一致的观感
     *
     * 老用户升级后默认拿到 [UiGeneration.Modern]（新观感），
     * 想回到原来样子的话在「设置 → 启动器 → 界面世代」里切一下即可，
     * 而且**切过去之后还能再切回来**。
     *
     * 读取方：`ui/theme/Theme.kt`（字阶 / 形状 / 动效方案）。
     */
    val uiGeneration = enumSetting("uiGeneration", UiGeneration.Modern)

    /**
     * 旧的液态玻璃开关（布尔值）
     *
     * 旧配置兼容项：保留定义以便旧用户升级后仍能正常读取原有配置。
     * 27.10.2 起「动态玻璃」整体移除，这个值只用于升级迁移时清理，不再参与渲染。
     */
    val liquidGlass = boolSetting("liquidGlass", false)

    /**
     * 主界面操作菜单的停泊侧（长按操作菜单可拖动换边）
     */
    val launcherActionMenuSide = enumSetting("launcherActionMenuSide", ActionMenuSide.END)

    /**
     * 启动器主页类型
     */
    // ── 首次启动引导 / 年龄确认（28.0.8）────────────────────────────
    val onboardingAccepted = boolSetting("onboardingAccepted", false)

    val onboardingAgeConfirmed = boolSetting("onboardingAgeConfirmed", false)

    /** 是否未满 18 周岁（默认按「未满」处理，保守）*/
    val onboardingIsMinor = boolSetting("onboardingIsMinor", true)

    val homePageType = enumSetting("homePageType", HomePageType.Cards)

    /**
     * 启动器网络主页下载地址
     */
    val homePageURL = stringSetting("homePageURL", "")

    /**
     * 卡片式主页的卡片大小（百分比）
     *
     * 100% 表示与既有外观完全一致；
     * 调小可以让更多实例卡片同时显示在一屏内，调大则更易点击。
     */
    val homeCardSize = intSetting("homeCardSize", 100, 70..140)

    /**
     * 启动器上次检查更新时，用户选择忽略的版本号（整数版本号）
     *
     * 旧的 ZL2 更新链路遗留配置项。
     * 保留定义是为了让旧用户升级后仍能正常读取原有配置，不再写入。
     */
    val lastIgnoredVersion = intSetting("lastIgnoredVersion", null)

    /**
     * 启动器上次检查更新时，用户选择忽略的版本号（版本名字符串，如 26.1.0）
     *
     * ZyNova 自己的更新体系使用版本名进行比较，因此单独使用一个配置键，
     * 避免与旧配置项产生存储类型冲突。
     */
    val lastIgnoredVersionName = stringSetting("lastIgnoredVersionName", "")

    /**
     * 启动器日志保留天数
     */
    val launcherLogRetentionDays = intSetting("launcherLogRetentionDays", 7, 1..14)

    /**
     * 游戏内容镜像源
     */
    val gameDownloadSource = enumSetting("gameDownloadSource", MirrorSourceType.AUTO)

    /**
     * 资源平台镜像源
     */
    val assetPlatformSource = enumSetting("assetPlatformSource", MirrorSourceType.AUTO)

    //网络与下载（议题 #37 / #41）
    //
    //⚠️ 这一组是 27.7.0 新增的配置。旧用户没有这些键 → 全部取默认值，
    //   默认值 = 旧版本的写死行为，所以升级后下载行为与升级前一模一样。

    /**
     * 同一个文件最多同时用几条连接下载
     *
     * 值越大，单个大文件下载越快，但对下载源和手机流量的压力也越大。
     */
    val downloadConnections = intSetting("downloadConnections", 4, 1..12)

    /**
     * 连接超时（秒）：连上服务器最多等多久
     */
    val downloadConnectTimeout = intSetting("downloadConnectTimeout", 15, 5..60)

    /**
     * 读超时（秒）：连上之后，多久收不到数据就判定这条连接卡死并换源
     */
    val downloadReadTimeout = intSetting("downloadReadTimeout", 15, 5..120)

    /**
     * 下载失败后自动重试的次数（0 = 不自动重试）
     */
    val downloadRetryCount = intSetting("downloadRetryCount", 3, 0..5)

    // ── 下载源修改器 ─────────────────────────────────────────────
    //
    //全部是「留空 = 用官方地址」。填写后必须是完整的 http(s) 地址，
    //读取时由 `path/DownloadUrls.kt` 统一校验，校验不通过会静默回退到官方地址，
    //不会把错误配置带进下载流程。
    //
    //⚠️ 各个下载服务是**分别**处理的：它们的数据格式与鉴权方式不同，
    //   不能把一个服务的地址套到另一个服务上。

    /**
     * Minecraft 版本清单地址（默认 Mojang 官方）
     */
    val customVersionManifestUrl = stringSetting("customVersionManifestUrl", "")

    /**
     * Modrinth 接口地址（默认官方 api.modrinth.com）
     */
    val customModrinthApiUrl = stringSetting("customModrinthApiUrl", "")

    /**
     * CurseForge 接口地址（默认官方 api.curseforge.com）
     */
    val customCurseForgeApiUrl = stringSetting("customCurseForgeApiUrl", "")

    /**
     * MCIM 镜像站地址（默认 mod.mcimirror.top）
     */
    val customMcimMirrorUrl = stringSetting("customMcimMirrorUrl", "")

    //Control
    /**
     * 全局默认控制布局文件名
     */
    val controlLayout = stringSetting("controlLayout", "")

    //Other
    /**
     * 当前选择的账号
     */
    val currentAccount = stringSetting("currentAccount", "")

    /**
     * 当前选择的游戏目录id
     */
    val currentGamePathId = stringSetting("currentGamePathId", GamePathManager.DEFAULT_ID)

    /**
     * 启动器任务菜单（顶部细进度条点开的详细任务面板）是否展开
     *
     * ⚠️ 默认 **false**：任务面板必须由用户点一下才出现。
     * 以前默认 true，导致「点了下载，右上角直接弹出大面板」，
     * 而顶部那条细进度条又被面板挡住 —— 用户既看不到进度、也被大框打断。
     */
    val launcherTaskMenuExpanded = boolSetting("launcherTaskMenuExpanded", false)

    /**
     * 在游戏菜单悬浮窗上显示帧率
     */
    val showFPS = boolSetting("showFPS", true)

    /**
     * 在游戏菜单悬浮窗上显示内存
     */
    val showMemory = boolSetting("showMemory", false)

    /**
     * 在游戏画面上展示菜单悬浮窗
     */
    val showMenuBall = boolSetting("showMenuBall", true)

    /**
     * 游戏菜单悬浮窗位置
     */
    val menuBallPos = offsetSetting("menuBallPos", Offset.Zero)

    /**
     * 游戏菜单悬浮窗不透明度
     */
    val menuBallOpacity = intSetting("menuBallOpacity", 100, 20..100)

    /**
     * 快捷栏判定箱计算规则
     */
    val hotbarRule = enumSetting("hotbarRule", HotbarRule.Auto)

    /**
     * 快捷栏宽度百分比
     */
    val hotbarWidth = intSetting("hotbarWidth", 500, 0..1000)

    /**
     * 快捷栏高度百分比
     */
    val hotbarHeight = intSetting("hotbarHeight", 100, 0..1000)

    /**
     * 快捷栏双击与副手交换物品
     */
    val hotbarDoubleClick = boolSetting("hotbarDoubleClick", true)

    /**
     * 快捷栏长按丢弃所选物品
     */
    val hotbarLongClick = boolSetting("hotbarLongClick", true)

    /**
     * 快捷栏长按快捷栏触发延迟
     */
    val hotbarLongClickDelay = intSetting("hotbarLongClickDelay", 300, 100..1000)

    /**
     * 游戏内控制布局的整体不透明度
     */
    val controlsOpacity = intSetting("controlsOpacity", 100, 0..100)

    /**
     * 控制布局编辑器：是否开启控件吸附功能
     */
    val editorEnableWidgetSnap = boolSetting("editorEnableWidgetSnap", true)

    /**
     * 控制布局编辑器：是否在所有控件层范围内吸附
     */
    val editorSnapInAllLayers = boolSetting("editorSnapInAllLayers", false)

    /**
     * 控制布局编辑器：控件吸附模式
     */
    val editorWidgetSnapMode = enumSetting("editorWidgetSnapMode", SnapMode.FullScreen)

    /**
     * 是否启用陶瓦联机
     */
    val enableTerracotta = boolSetting("enableTerracotta", false)

    /**
     * 是否使用自定义 EasyTier 服务器节点
     */
    val enableTerracottaNodes = boolSetting("enableTerracottaNodes", false)

    /**
     * 陶瓦联机：自定义 EasyTier 服务器节点
     */
    val terracottaNodes = stringSetting("terracottaNodes", "")

    /**
     * 陶瓦联机公告版本号
     */
    val terracottaNoticeVer = intSetting("terracottaNoticeVer", -1)

    /**
     * 樱花内网穿透：访问密钥（保存的是 Android Keystore 加密后的密文，不是明文）
     *
     * ⚠️ 读写请统一走 [com.movtery.zalithlauncher.frp.FrpManager.token]，
     * 不要直接拿这里的值去请求接口或写日志。
     */
    val frpToken = stringSetting("frpToken", "")

    /**
     * 上次检查更新的时间戳
     */
    val lastUpgradeCheck = longSetting("lastUpgradeCheck", 0L)

    /**
     * 玩家结束运行游戏的次数
     */
    val finishedGame = intSetting("finishedGame", 0)

    /**
     * 是否在打开启动器时，根据特定的运行游戏次数，显示赞助支持弹窗
     */
    val showSponsorship = boolSetting("showSponsorship", true)

    /**
     * 搜索模组的初始搜索平台
     */
    val searchModPlatform = enumSetting("searchModPlatform", SearchPlatform.ALL)

    /**
     * 搜索整合包的初始搜索平台
     */
    val searchModpackPlatform = enumSetting("searchModpackPlatform", SearchPlatform.ALL)

    /**
     * 搜索资源包的初始搜索平台
     */
    val searchResourcePackPlatform = enumSetting("searchResourcePackPlatform", SearchPlatform.ALL)

    /**
     * 搜索光影的初始搜索平台
     */
    val searchShadersPlatform = enumSetting("searchShadersPlatform", SearchPlatform.ALL)
}