import com.android.build.api.variant.impl.VariantOutputImpl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    id("com.google.devtools.ksp")
    id("kotlinx-serialization")
    id("kotlin-parcelize")
    id("com.movtery.buildkeys")
}

val zalithPackageName = "com.movtery.zalithlauncher"
val zynovaPackageName = "com.zynova.launcher"
val launcherAPPName = project.findProperty("launcher_app_name") as? String ?: error("The \"launcher_app_name\" property is not set in gradle.properties.")
val launcherName = project.findProperty("launcher_name") as? String ?: error("The \"launcher_name\" property is not set in gradle.properties.")
val launcherShortName = project.findProperty("launcher_short_name") as? String ?: error("The \"launcher_short_name\" property is not set in gradle.properties.")
val launcherUrl = project.findProperty("url_home") as? String ?: error("The \"url_home\" property is not set in gradle.properties.")

val launcherVersionCode = (project.findProperty("launcher_version_code") as? String)?.toIntOrNull() ?: error("The \"launcher_version_code\" property is not set as an integer in gradle.properties.")
val launcherVersionName = project.findProperty("launcher_version_name") as? String ?: error("The \"launcher_version_name\" property is not set in gradle.properties.")

//APK 文件名只取版本号数字部分：将来 versionName 若带后缀（例如「28.1.1 beta」），
//空格会被 GitHub 转成点，产出带后缀的名字；这里固定只取数字部分。
val launcherVersionFileTag = launcherVersionName.substringBefore(' ').trim()

val defaultOAuthClientID = project.findProperty("oauth_client_id") as? String
val defaultStorePassword = project.findProperty("default_store_password") as? String ?: error("The \"default_store_password\" property is not set in gradle.properties.")
val defaultKeyPassword = project.findProperty("default_key_password") as? String ?: error("The \"default_key_password\" property is not set in gradle.properties.")
val defaultCurseForgeApiKey = project.findProperty("curseforge_api_key") as? String

/**
 * 只发布**一个**通用版 APK：包含全部 ABI（arm64-v8a / armeabi-v7a / x86 / x86_64）。
 *
 * 维护者 2026-11 决定：取消单独的 `arm64` 体积优化版。
 * 原因：两个包对用户的区分成本高于省下的体积，且「测试渲染器」（LTW-Turbo）本来就放在
 * `src/main/jniLibs/arm64-v8a`，通用版已经包含，arm64 版并没有多出任何功能。
 * 之后发版只出 `ZyNova-<版本>.apk` + `mapping.universal.zip`。
 */
val projectArch: String = "all"

fun getKeyFromLocal(envKey: String, fileName: String? = null, default: String? = null): String {
    //⚠️ CI 中**未配置**的 Secrets 会以空字符串注入环境变量。
    //如果把空字符串当成有效值，本地密钥文件与 gradle.properties 里的兜底配置
    //就永远不会生效（例如 CurseForge API Key 缺失会让官方接口固定 403）。
    val key = System.getenv(envKey)?.takeIf { it.isNotBlank() }
    return key ?: fileName?.let {
        val file = File(rootDir, fileName)
        if (file.canRead() && file.isFile) {
            file.readText().trim().takeIf { text -> text.isNotEmpty() }
        } else null
    } ?: default?.takeIf { it.isNotBlank() } ?: run {
        logger.warn("BUILD: $envKey not set; related features may throw exceptions.")
        ""
    }
}

android {
    namespace = zalithPackageName
    compileSdk = 37

    signingConfigs {
        create("releaseBuild") {
            storeFile = file("zalith_launcher_debug.jks")
            storePassword = defaultStorePassword
            keyAlias = "movtery_zalith_debug"
            keyPassword = defaultKeyPassword
        }
        create("debugBuild") {
            storeFile = file("zalith_launcher_debug.jks")
            storePassword = defaultStorePassword
            keyAlias = "movtery_zalith_debug"
            keyPassword = defaultKeyPassword
        }
    }

    defaultConfig {
        applicationId = zynovaPackageName
        minSdk = 26
        targetSdk = 34
        versionCode = launcherVersionCode
        versionName = launcherVersionName
        manifestPlaceholders["launcher_name"] = launcherAPPName
    }

    flavorDimensions += "abi"
    productFlavors {
        create("universal") {
            dimension = "abi"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("releaseBuild")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            signingConfig = signingConfigs.getByName("debugBuild")
        }
    }

    ndkVersion = "25.2.9519653"

    externalNativeBuild {
        ndkBuild {
            path = file("src/main/jni/Android.mk")
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
            pickFirsts += listOf("**/libbytehook.so")
            // libfrpc.so 是樱花 frpc 的可执行文件（Go + UPX），不是真正的共享库；
            // 必须阻止 AGP 用 llvm-strip 处理它，否则会破坏 UPX 头部导致无法启动。
            keepDebugSymbols += listOf("**/libfrpc.so")
        }
    }

    compileOptions {
        // sora-editor language-textmate
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
        prefab = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            //让 android.util.Log 等框架方法在本地单测中返回默认值而非抛出异常
            isReturnDefaultValues = true
        }
    }

    sourceSets {
        getByName("test") {
            //单元测试跑在纯 JVM 上，默认拿不到 APK 里的 assets：
            //org.jackhuang.hmcl.util.versioning.GameVersionNumber 的静态初始化会读
            //classpath 上的 /assets/game/versions.txt 与 /assets/game/version-alias.csv，
            //没挂上就会 NPE / ExceptionInInitializerError（VersionCompareTest 曾因此全红）。
            resources.srcDirs("src/main/assets")
        }
    }
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            if (output is VariantOutputImpl) {
                val baseName = "$launcherName-${if (variant.buildType == "release") launcherVersionFileTag else "Debug-$launcherVersionFileTag"}"
                //只有一个 flavor，不再需要 ABI 后缀
                output.outputFileName = "$baseName.apk"
            }
        }
    }
}


kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        optIn.addAll(
            "androidx.compose.material3.ExperimentalMaterial3Api",
        )
    }
}

buildKeys {
    string("OAUTH_CLIENT_ID", getKeyFromLocal("OAUTH_CLIENT_ID", ".oauth_client_id.txt", defaultOAuthClientID), true)
    string("LAUNCHER_NAME", launcherAPPName, true)
    string("LAUNCHER_IDENTIFIER", launcherName, true)
    string("LAUNCHER_SHORT_NAME", launcherShortName, true)
    string("URL_HOME", launcherUrl, true)
    string("CURSEFORGE_API", getKeyFromLocal("CURSEFORGE_API_KEY", ".curseforge_api.txt", defaultCurseForgeApiKey), true)
    string("BUILD_ARCH", projectArch)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.nav3)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.webkit)
    implementation(libs.documentfile)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.coil.svg)
    implementation(libs.coil.network.ktor3)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.material)
    implementation(libs.material.color.utilities)
    implementation(libs.materialKolor)
    implementation(libs.reorderable)
    implementation(libs.richtext.commonmark)
    implementation(libs.richtext.ui)
    implementation(libs.richtext.ui.material3)
    implementation(platform(libs.editor.bom))
    implementation(libs.editor)
    implementation(libs.editor.language.textmate)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.dev.haze)
    implementation(libs.dev.haze.blur)
    //Project
    implementation(project(":LayerController"))
    implementation(project(":ColorPicker"))
    implementation(project(":Terracotta"))
    implementation(project(":InputMap"))
    //Utils
    implementation(libs.bytehook)
    implementation(libs.gson)
    implementation(libs.commons.io)
    implementation(libs.commons.codec)
    implementation(libs.commons.compress)
    implementation(libs.xz)
    implementation(libs.zip4j)
    implementation(libs.okio)
    implementation(libs.okhttp)
    implementation(libs.ktor.http)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.minidns.hla)
    implementation(libs.toml4j)
    implementation(libs.maven.artifact)
    implementation(libs.mmkv)
    implementation(libs.fishnet)
    implementation(libs.process.phoenix)
    implementation(libs.lunarcalendar)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    //Safe
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.sqlcipher.android)
    ksp(libs.androidx.room.compiler)
    //Support
    implementation(libs.proxy.client.android)
    //Hilt
    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    //Test
    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver3)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
}
