pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.8.0")
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ZalithLauncher"
// 源码模块统一放在 modules/ 下，根目录只保留工程入口与文档。
include(":ZalithLauncher")
project(":ZalithLauncher").projectDir = file("modules/ZalithLauncher")
include(":LWJGL")
project(":LWJGL").projectDir = file("modules/LWJGL")
include(":LWJGL:patches")
project(":LWJGL:patches").projectDir = file("modules/LWJGL/patches")
include(":LWJGL:lwjgl-3.3.3")
project(":LWJGL:lwjgl-3.3.3").projectDir = file("modules/LWJGL/3.3.3")
include(":LWJGL:lwjgl-3.4.1")
project(":LWJGL:lwjgl-3.4.1").projectDir = file("modules/LWJGL/3.4.1")
include(":LayerController")
project(":LayerController").projectDir = file("modules/LayerController")
include(":ColorPicker")
project(":ColorPicker").projectDir = file("modules/ColorPicker")
include(":Terracotta")
project(":Terracotta").projectDir = file("modules/Terracotta")
include(":InputMap")
project(":InputMap").projectDir = file("modules/InputMap")
