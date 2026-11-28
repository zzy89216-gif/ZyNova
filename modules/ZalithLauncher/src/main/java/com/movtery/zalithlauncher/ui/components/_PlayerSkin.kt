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

package com.movtery.zalithlauncher.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.util.Base64
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.toArgb
import androidx.webkit.WebViewAssetLoader
import com.movtery.zalithlauncher.game.account.wardrobe.EmptyCape
import com.movtery.zalithlauncher.game.account.wardrobe.SkinModelType
import com.movtery.zalithlauncher.game.account.yggdrasil.PlayerProfile
import com.movtery.zalithlauncher.path.PathManager
import java.io.File
import java.io.InputStream

@SuppressLint("SetJavaScriptEnabled")
class PlayerSkin(
    context: Context,
    localSkinsDir: File = PathManager.DIR_ACCOUNT_SKIN,
    localCapeDir: File = PathManager.DIR_ACCOUNT_CAPE,
) {
    /**
     * 皮肤 / 披风的**真实**位置（公共目录 `/ZyNova/.Minecraft/account_skins`）。
     */
    private val sourceSkinsDir: File = localSkinsDir
    private val sourceCapesDir: File = localCapeDir

    /**
     * WebView 实际读取的位置：应用**内部缓存**。
     *
     * ⚠️ 这里绝对不能直接注册公共目录：`WebViewAssetLoader.InternalStoragePathHandler`
     * 只接受「应用内部存储」下的目录，传公共目录会抛
     * `IllegalArgumentException: The given directory ... doesn't exist under an allowed
     * app internal storage directory`，表现就是**一进账号管理就崩溃**。
     * 所以先注册内部缓存目录，加载哪张皮肤时再把那张图镜像过来（64×64 的 PNG，代价可以忽略）。
     */
    private val servedSkinsDir: File = File(PathManager.DIR_CACHE, "skinview/skins").apply { mkdirs() }
    private val servedCapesDir: File = File(PathManager.DIR_CACHE, "skinview/capes").apply { mkdirs() }

    private val assetLoader = WebViewAssetLoader.Builder()
        .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
        .addPathHandler(
            "/skins/",
            WebViewAssetLoader.InternalStoragePathHandler(context, servedSkinsDir)
        )
        .addPathHandler(
            "/capes/",
            WebViewAssetLoader.InternalStoragePathHandler(context, servedCapesDir)
        )
        .build()

    /**
     * 把源文件镜像到可被 WebView 读取的目录，成功返回文件名（不带路径）。
     *
     * 源文件不存在就直接返回 null，调用方会回退到默认皮肤。
     */
    private fun mirror(source: File, targetDir: File, name: String): String? {
        return runCatching {
            if (!source.isFile) return null
            val target = File(targetDir, name)
            val outdated = !target.exists() ||
                target.length() != source.length() ||
                target.lastModified() < source.lastModified()
            if (outdated) {
                source.copyTo(target, overwrite = true)
                target.setLastModified(source.lastModified())
            }
            name
        }.getOrNull()
    }

    /** 镜像账号皮肤；返回可在 `/skins/` 下访问的文件名 */
    private fun mirrorSkin(id: String): String? =
        mirror(File(sourceSkinsDir, "$id.png"), servedSkinsDir, "$id.png")

    /** 镜像披风；返回可在 `/capes/` 下访问的文件名 */
    private fun mirrorCape(id: String): String? =
        mirror(File(sourceCapesDir, "$id.png"), servedCapesDir, "$id.png")

    private var webview: WebView? = null

    private val skinView = AssetsUrlBuilder()
        .append("assets")
        .append("skinview")
        .append("skinview.html")
        .toString()

    private val defaultSkin = AssetsUrlBuilder()
        .append("assets")
        .append("steve.png")
        .toString()

    fun loadWebView(
        context: Context,
        onPageFinished: () -> Unit = {}
    ): WebView {
        val view = WebView(context).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                loadWithOverviewMode = true
                useWideViewPort = true
            }
            setBackgroundColor(Transparent.toArgb())
            overScrollMode = WebView.OVER_SCROLL_NEVER
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            webViewClient = object : WebViewClient() {
                override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                    return assetLoader.shouldInterceptRequest(request.url)
                }

                override fun onPageFinished(view: WebView, url: String) {
                    super.onPageFinished(view, url)
                    onPageFinished()
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                    Log.d(
                        "WebViewConsole", (consoleMessage.message()
                                + " (line " + consoleMessage.lineNumber() + ")")
                    )
                    return true
                }

                override fun onJsAlert(view: WebView?, url: String?, message: String, result: JsResult): Boolean {
                    Log.d("WebViewAlert", message)
                    result.confirm()
                    return true
                }
            }

            loadUrl(skinView)
        }
        this.webview = view
        return view
    }

    fun loadSkin(skinId: String?, model: SkinModelType?) {
        val modelString = model?.takeIf { it != SkinModelType.NONE }?.modelType ?: "auto-detect"
        val jsUrl = skinId?.let { id ->
            mirrorSkin(id)?.let { fileName ->
                AssetsUrlBuilder()
                    .append("skins")
                    .append(fileName)
                    .toString()
            }
        } ?: defaultSkin
        webview?.evaluateJavascript("loadSkin('$jsUrl', '$modelString')", null)
    }

    fun loadSkin(inputStream: InputStream?, model: SkinModelType?) {
        inputStream?.asBase64Image()?.let { dataUrl ->
            val modelString = model.takeIf { it != SkinModelType.NONE }?.modelType ?: "auto-detect"
            webview?.evaluateJavascript("loadSkin('$dataUrl', '$modelString')", null)
        } ?: run {
            loadSkin(skinId = null, model)
        }
    }

    fun loadCape(cape: PlayerProfile.Cape?) {
        val path = cape?.takeIf { it != EmptyCape }?.id?.let { id ->
            mirrorCape(id)?.let { fileName ->
                AssetsUrlBuilder()
                    .append("capes")
                    .append(fileName)
                    .toString()
            }
        }
        val jsUrl = path?.let { "'$it'" } ?: "null"
        webview?.evaluateJavascript("loadCape($jsUrl)", null)
    }

    fun loadCape(inputStream: InputStream?) {
        inputStream?.asBase64Image()?.let { dataUrl ->
            webview?.evaluateJavascript("loadCape('$dataUrl')", null)
        } ?: run {
            loadCape(cape = null)
        }
    }

    fun resetSkin() {
        loadSkin(skinId = null, SkinModelType.NONE)
        loadCape(cape = null)
    }

    fun startAnim(
        animation: ModelAnimation,
        speed: Float? = null
    ) {
        webview?.evaluateJavascript(
            "startAnim('${animation.name}', $speed)",
            null
        )
    }

    fun setAzimuthAndPitch(azimuthDeg: Int, pitchDeg: Int, distance: Int = 60) {
        webview?.evaluateJavascript(
            "setAzimuthAndPitch($azimuthDeg, $pitchDeg, $distance)",
            null
        )
    }

    /**
     * 销毁 WebView 并释放资源
     * 应在包含该组件的 Composable 离开组合树时调用
     */
    fun destroy() {
        webview?.apply {
            stopLoading()
            loadUrl("about:blank")
            clearHistory()
            removeAllViews()
            destroy()
        }
        webview = null
    }

    private fun InputStream.asBase64Image(): String {
        return readBytes().let { bytes ->
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            "data:image/png;base64,$base64"
        }
    }
}

enum class ModelAnimation {
    DefaultIdle,
    NewIdle,
    Walking,
    Running,
    Flying,
    Wave,
    Crouch,
    Hit
}

private class AssetsUrlBuilder {
    private val builder = StringBuilder()

    init {
        builder.append("https://appassets.androidplatform.net")
    }

    fun append(path: String): AssetsUrlBuilder {
        builder.append("/")
        builder.append(path)
        return this
    }

    override fun toString(): String {
        return builder.toString()
    }
}