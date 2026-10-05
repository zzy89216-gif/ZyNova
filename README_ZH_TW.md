<p align="center">
  <img src="assets/icon.png" width="180" alt="ZyNova">
</p>

# ZyNova

> **Minecraft: Java Edition · Android 啟動器**
>
> 基於 ZalithLauncher2 開源程式碼開發的獨立非官方專案

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Primary-Kotlin-blue)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-GPL--3.0-orange)](https://www.gnu.org/licenses/gpl-3.0.html)
[![Release](https://img.shields.io/badge/Release-27.2.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.2.0)

[English](README.md) | [简体中文](README_ZH_CN.md) | **[繁體中文](README_ZH_TW.md)** | [日本語](README_JA_JP.md)

---

## ✦ 專案簡介

**ZyNova** 是一個基於 [ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2)
開源程式碼開發的 **非官方** Minecraft: Java Edition Android 啟動器。

> **ZyNova 並非 ZalithLauncher2 官方版本。**
>
> ZyNova 是基於 ZalithLauncher2 開源程式碼進行開發的非官方修改專案。
>
> 內建 **全域 AI Agent**：不只是聊天，而是能**直接讀日誌、管模組、改設定、裝資源、啟動遊戲**。

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>（永久邀請）

---

## ✨ 27.2.0 主要內容

- **頁面轉場終於真的起作用了。** 轉場設定提供 *果凍彈跳 / 彈跳 / 切入*，
  但每一個都被默默渲染成普通的交叉淡入淡出。現在每個選項都有真正的轉場，
  而且十五個導覽介面共用同一套實作 —— 包括先前完全沒有轉場的檔案管理員。
- **減少動效** —— 全新的無障礙開關，一次關閉所有動效：頁面轉場、階梯式進場動畫、
  動態玻璃的流動高光與微光骨架，並把 Material 動效方案從 expressive 降級為 standard。
  它是真正的關閉開關，而不只是縮短時長。
- **全新的 ⚠️ 強烈動態玻璃等級，附光敏性警告。** 玻璃效果現在有三個等級：
  關閉 / 動態玻璃 / **強烈（光敏性風險）**。強烈等級流動更快、更亮，並加入緩慢的亮度脈動。
  與舊的「極致」等級不同，它只繪製漸層高光 —— 絕不模糊承載文字的圖層，因此文字保持清晰。
  由於該等級確實帶有真實風險，啟動器**在使用期間每次啟動都會顯示光敏性警告**，
  你可以在 *設定 → 啟動器* 中關閉該警告。
- **卡片現在會回應觸控** —— 細緻的按下與放開縮放，透過共用的卡片元件一致套用
  （在減少動效下完全略過）。
- **圖形 API 全面自動化。** 手動的 OpenGL / Vulkan 選擇器已移除。實例首次啟動時使用 OpenGL，
  之後啟動器就不再干預後端，讓遊戲自身的設定決定。某個版本是否需要這個選項，
  取決於它的**發佈日期**，而非硬編碼的版本號 —— 第一個支援 Vulkan 的版本是
  Minecraft 26.2-snapshot-1（2026-04-07）。版本中介資料會在每次啟動時檢查
  （包括最新正式版與快照），且絕不會阻擋遊戲啟動。
- **啟動畫面現在會淡入主畫面**，而不再使用預設的 Activity 動畫。

> 📜 完整版本歷史請見 [CHANGELOG.md](CHANGELOG.md)

## ℹ️ 正版登入用的是哪個應用
**ZyNova 是 ZalithLauncher2 的非官方分支，兩者是各自獨立維護的不同專案。**

| | 顯示名稱 | Client ID | Mojang 允許名單 | 本版本是否使用 |
|---|---|---|---|---|
| **ZyNova 自己的** | ZyNova Launcher | `7b66e168-f8cd-43fc-a52d-2e78dba189b0` | ✅ **已獲批准（2026-09-30）** | ✅ **正在使用** |
| **ZalithLauncher2 的** | ZalithLauncher（上游） | （已移除） | ✅ 已獲批准 | ❌ 不使用（26.4.0 / 26.4.1 曾使用） |

- **只有處於 Minecraft 應用程式允許名單中的應用註冊**才能完成正版登入；
  否則 `POST /authentication/login_with_xbox` 會回傳 `403 Invalid app registration`
- ✅ **ZyNova 自己的註冊已在該名單中**：Mojang Enforcement 已於 **2026-09-30** 完成 AppID 審核，
  目前版本的正版登入可以正常完成，也不再借用上游專案的註冊
- 仍然**只需要「公開客戶端 + Client ID」**：不需要 Client Secret、不需要 Redirect URI、不需要 SHA-1；
  Client ID 透過倉庫 Secret `OAUTH_CLIENT_ID` **在建置時注入**，不寫死在原始碼中

## 🛠️ 功能

### AI Agent

頂部工具列「檔案」旁新增 **AI** 按鈕，點擊**直接進入聊天介面**，不設獨立的 AI 首頁。
聊天與 Agent 是**同一個入口**：先問「為什麼這個實例進不去？」，它會去讀日誌；
接著說「幫我修」，它會**直接呼叫工具動手處理**，而不是寫一段教學給你。

- **25 個工具**，全部建立在啟動器現有系統之上：實例、模組真實中繼資料、
  啟用／停用／刪除模組、資源包、光影、存檔、檔案、日誌、崩潰報告、
  **117 項啟動器設定**、資源搜尋與安裝（含遞迴安裝必需前置），以及**真正啟動遊戲**
- **對話自動存在本機**，左上角的**側邊欄**可以查看、切換、刪除或新增對話
- **寫入操作稽核記錄**：每一次寫入操作都會在本地留痕，可在 AI 設定中查看或匯出
- **不限制工具呼叫次數** —— Agent 會一直做到它認為完成為止，你隨時可以停止
- **設定獨立於啟動器設定**（聊天介面右上角）：服務商（OpenAI / Anthropic，可擴充）、
  自備 API Key（僅存本機）、**模型清單動態取得、不硬編碼任何模型名稱**、
  可自訂 Base URL、權限模式（預設**操作確認**／完全控制）
- **安全**：檔案類工具只能存取遊戲與啟動器資料目錄；敏感設定鍵拒絕讀寫；
  確認介面不可用時一律拒絕寫入操作

### 渲染器

26.3.0 起，啟動器**內建的渲染器只有三個**：

| 渲染器 | 說明 | 設定 |
|---|---|---|
| **Ironized Zink**（預設） | 桌面 OpenGL 4.6 經 Vulkan 轉譯（Mesa Zink + Kopper），作者 GoyDevv | **4 個官方預設** + OpenGL 版本下拉 |
| **GL4ES** | 經典 OpenGL 轉譯層 | 保持預設 |
| **MobileGlues** | 把桌面 OpenGL 轉譯到裝置的 OpenGL ES 3.x | 保持上游預設 |

**Ironized Zink 的 4 個官方預設**（參數值與上游一致）：

| 預設 | 定位 | Minecraft | 光影 |
|---|---|---|---|
| **Potato** | 絕對最高幀率 | 1.8 → 最新（含 26.x） | 不推薦 |
| **Performance** | 高幀率 + Sodium | 1.20.x → 最新 | 輕量 |
| **Default** | 均衡 Zink + 光影（**預設**） | 1.16.x → 最新 | 完整（Iris / OptiFine） |
| **Max Compatibility** | 什麼都能跑 | 全部版本 | 完整 + 重度光影包 |

**26.4.0 起，面板只提供上面 4 個預設**：12 個單獨參數開關已被移除，
一般使用者不再需要手動調整底層參數。選擇預設仍會一次寫入整組參數。

> ℹ️ 4 個預設的 OpenGL 版本都是 **4.6**。如果驅動對 Vulkan→Zink 的 4.6 轉譯不完整
> （畫面異常或進不去），可以在面板下方的「**OpenGL 版本**」下拉降到 4.5 / 4.3 / 3.3
> —— 該下拉在 26.4.0 曾被移除，**27.1.2 起已恢復**。
> 改動此項只影響 OpenGL 版本，不動其它預設參數。

在 **設定 → 渲染器** 選中 Ironized Zink 後，**下方會立刻展開預設面板**。

> **外掛渲染器不受影響**：仍然可以透過安裝渲染器外掛（FCL / Zalith 渲染器外掛、
> 新一代 `fclPlugin_V2` 外掛）把更多渲染器加進清單 ——
> 內建只有 3 個，外掛想裝幾個還是幾個。

### 卡片式首頁

首頁以**版本為模組**組織內容：每個已安裝的版本是一張模組卡片，
卡片內直接列出該版本自己的本機世界與已儲存伺服器。
點擊模組卡片本身即可**啟動該版本**。

卡片式首頁還支援：

- **長按拖動排序**：版本卡片之間、卡片內的世界 / 伺服器，
  以及右側選單的三塊（帳號頭像 / 版本列 / 啟動按鈕）都能拖動調整順序，順序會被記住
- **卡片大小**：設定 → 啟動器 → 首頁 → 卡片大小（70% ~ 140%，預設 100%）
- 卡片為**單層背景**，不會有多餘的邊框或陰影

資料按需載入，啟動器啟動時不會全盤掃描。
設定中可選擇預設 / 卡片 / 自訂首頁。

### 統一資源管理核心

Mod、資源包、光影與存檔共用同一條流程：

> 搜尋 → 資源詳情 → 版本匹配 → 檔案選擇 → 下載 → 校驗 → 安裝

### 資源來源 Provider

資源來源與介面完全解耦，上層只依賴統一介面。
日後新增其他來源只需實作 Provider，不需要重寫整個資源系統。

### 統一下載管理器

所有下載共用同一個入口：下載佇列、併發控制、下載進度、斷點續傳、失敗重試、
取消下載、檔案校驗、臨時檔案清理，以及下載完成後的安裝觸發。

### 極簡資源安裝

從「版本設定 → Mods」進入資源頁面時，系統已知目前實例、Minecraft 版本、
載入器與資源目錄，點擊下載即可直接安裝，不會重複要求選擇版本、實例或安裝位置。
安裝完成後資源列表會顯示「已安裝」狀態。

### Vulkan 偵測

偵測結果明確區分 **可用 / 不可用 / 偵測失敗**，並提供具體原因、GPU 與驅動資訊，
以及主動重新偵測按鈕。判斷依據是裝置**實際列舉出來的 Vulkan 能力**，
而不是裝置對外宣稱的支援情況。

---

## 📦 構建方式（開發者）

> 以下內容供希望參與開發或在本機構建專案的開發者參考。

### 環境要求

* 支援 **AGP 9.3.0** 的 Android Studio（近期穩定版）—— 舊版本無法開啟本專案
* Android SDK：
  * **最低 API 等級**：26
  * **目標 API 等級**：34
  * **Compile SDK**：37
* JDK 17
* Gradle **9.5.0**（Wrapper 會自動處理）

### 構建步驟

```bash
git clone https://github.com/zzy89216-gif/ZyNova.git
# 使用 Android Studio 開啟專案並進行構建
```

---

## 📜 License

本專案程式碼遵循 **[GPL-3.0 license](LICENSE)** 開源協議。

### 附加條款（依據 GPLv3 開源授權條款第七條）

1. 當你分發本程式的修改版本時，必須以合理方式修改該程式的名稱或版本號，以區別於原始版本。（依據 [GPLv3, 7(c)](https://github.com/ZalithLauncher/ZalithLauncher2/blob/969827b/LICENSE#L372-L374)）
    - 修改版本 **不得在名稱中包含原程式名稱「ZalithLauncher」或其縮寫「ZL」，亦不得使用與官方名稱相近、可能造成混淆的名稱**。
    - 所有修改版本 **必須在程式啟動畫面或主介面中以明顯方式標示其為「非官方修改版」**。
    - 程式的應用名稱可於 [gradle.properties](./ZalithLauncher/gradle.properties) 中進行修改。

2. 你不得移除本程式所顯示的版權聲明。（依據 [GPLv3, 7(b)](https://github.com/ZalithLauncher/ZalithLauncher2/blob/969827b/LICENSE#L368-L370)）

> **各元件的授權並不相同。** 不要因為某個相依套件是 MIT，就認為整個專案都是 MIT：
> ZyNova 本身是 GPL-3.0，Ironized Zink 是 GPL-3.0，MobileGlues 是 LGPL-2.1，GL4ES 是 MIT。
> Ironized Zink 隨附的渲染引擎來自 **Mesa**：主程式碼與 Gallium（含 **Zink** 驅動）為 MIT，
> GLX 客戶端程式碼為 SGI Free Software License B，GL / GLX 擴充標頭檔為 Khronos 授權；
> **Kopper** 同樣屬於 Mesa 程式碼庫，適用 Mesa 的上述條款。
> 完整的逐元件清單、版權聲明，以及重散布二進位檔的原始碼取得方式，
> 見 **[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)**。

## 引用開源專案
  
本軟體使用以下開源函式庫:

| Library                               | Copyright                                                                                                     | License              | Official Link                                                                     |
|---------------------------------------|---------------------------------------------------------------------------------------------------------------|----------------------|-----------------------------------------------------------------------------------|
| androidx-appcompat                    | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [链接↗](https://developer.android.com/jetpack/androidx/releases/appcompat)         |
| androidx-constraintlayout-compose     | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [链接↗](https://developer.android.com/develop/ui/compose/layouts/constraintlayout) |
| androidx-webkit                       | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [链接↗](https://developer.android.com/jetpack/androidx/releases/webkit)            |
| ANGLE                                 | Copyright 2018 The ANGLE Project Authors                                                                      | BSD 3-Clause License | [链接↗](http://angleproject.org/)                                                  |
| Apache Commons Codec                  | -                                                                                                             | Apache 2.0           | [链接↗](https://commons.apache.org/proper/commons-codec)                           |
| Apache Commons Compress               | -                                                                                                             | Apache 2.0           | [链接↗](https://commons.apache.org/proper/commons-compress)                        |
| Apache Commons IO                     | -                                                                                                             | Apache 2.0           | [链接↗](https://commons.apache.org/proper/commons-io)                              |
| ByteHook                              | Copyright © 2020-2024 ByteDance, Inc.                                                                         | MIT License          | [链接↗](https://github.com/bytedance/bhook)                                        |
| BuildKeys                             | Copyright © 2026 MovTery                                                                                      | Apache 2.0           | [链接↗](https://github.com/MovTery/BuildKeys)                                      |
| Coil Compose                          | Copyright © 2025 Coil Contributors                                                                            | Apache 2.0           | [链接↗](https://github.com/coil-kt/coil)                                           |
| Coil Gifs                             | Copyright © 2025 Coil Contributors                                                                            | Apache 2.0           | [链接↗](https://github.com/coil-kt/coil)                                           |
| Coil SVG                              | Copyright © 2025 Coil Contributors                                                                            | Apache 2.0           | [链接↗](https://github.com/coil-kt/coil)                                           |
| Fishnet                               | Copyright © 2025 Kyant                                                                                        | Apache 2.0           | [链接↗](https://github.com/Kyant0/Fishnet)                                         |
| gl4es_extra_extra                     | Copyright © 2016-2018 Sebastien Chevalier; Copyright (c) 2013-2016 Ryan Hileman                               | MIT License          | [链接↗](https://github.com/PojavLauncherTeam/gl4es_extra_extra)                    |
| Gson                                  | Copyright © 2008 Google Inc.                                                                                  | Apache 2.0           | [链接↗](https://github.com/google/gson)                                            |
| kotlinx.coroutines                    | Copyright © 2000-2020 JetBrains s.r.o.                                                                        | Apache 2.0           | [链接↗](https://github.com/Kotlin/kotlinx.coroutines)                              |
| ktor-client-content-negotiation       | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [链接↗](https://ktor.io)                                                           |
| ktor-client-core                      | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [链接↗](https://ktor.io)                                                           |
| ktor-client-okhttp                    | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [链接↗](https://ktor.io)                                                           |
| ktor-http                             | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [链接↗](https://ktor.io)                                                           |
| ktor-serialization-kotlinx-json       | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [链接↗](https://ktor.io)                                                           |
| LWJGL - Lightweight Java Game Library | Copyright © 2012-present Lightweight Java Game Library All rights reserved.                                   | BSD 3-Clause License | [链接↗](https://github.com/LWJGL/lwjgl3)                                           |
| material-color-utilities              | Copyright 2021 Google LLC                                                                                     | Apache 2.0           | [链接↗](https://github.com/material-foundation/material-color-utilities)           |
| Maven Artifact                        | Copyright © The Apache Software Foundation                                                                    | Apache 2.0           | [链接↗](https://github.com/apache/maven/tree/maven-3.9.9/maven-artifact)           |
| Media3                                | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [链接↗](https://developer.android.com/jetpack/androidx/releases/media3)            |
| Ironized Zink                         | Copyright © GoyDevv（渲染引擎：Mesa Zink / Kopper，版權歸 Mesa 作者所有）                                     | GPL-3.0              | [連結↗](https://github.com/GoyDevv/IronizedZink)                                   |
| Mesa 主程式碼 / Gallium（含 Zink）    | Copyright © 1999-2007 Brian Paul and the Mesa contributors                                                    | MIT                  | [連結↗](https://mesa3d.org/)                                                       |
| Mesa GLX 客戶端程式碼                 | Copyright © 1999-2007 Brian Paul and the Mesa contributors                                                    | SGI Free Software License B | [連結↗](https://mesa3d.org/)                                                 |
| Mesa GL / GLX 擴充標頭檔              | Copyright © The Khronos Group                                                                                 | Khronos              | [連結↗](https://www.khronos.org/)                                                  |
| Kopper                                | 屬於 Mesa 程式碼庫（Vulkan WSI 層）                                                                           | 隨 Mesa 適用上述條款 | [連結↗](https://mesa3d.org/)                                                      |
| MMKV                                  | Copyright © 2018 THL A29 Limited, a Tencent company.                                                          | BSD 3-Clause License | [链接↗](https://github.com/Tencent/MMKV)                                           |
| Navigation 3                          | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [链接↗](https://developer.android.com/jetpack/androidx/releases/navigation3)       |
| MobileGlues                           | Copyright (c) 2025-2026 MobileGL-Dev                                                                          | LGPL-2.1             | [連結↗](https://github.com/MobileGL-Dev/MobileGlues)                               |
| OkHttp                                | Copyright © 2019 Square, Inc.                                                                                 | Apache 2.0           | [链接↗](https://github.com/square/okhttp)                                          |
| Okio                                  | Copyright © 2013 Square, Inc.                                                                                 | Apache 2.0           | [链接↗](https://github.com/square/okio)                                            |
| OpenNBT                               | Copyright © 2013-2021 Steveice10.                                                                             | MIT License          | [链接↗](https://github.com/GeyserMC/OpenNBT)                                       |
| Process Phoenix                       | Copyright © 2015 Jake Wharton                                                                                 | Apache 2.0           | [链接↗](https://github.com/JakeWharton/ProcessPhoenix)                             |
| proxy-client-android                  | -                                                                                                             | LGPL-3.0 License     | [链接↗](https://github.com/TouchController/TouchController)                        |
| Reorderable                           | Copyright © 2023 Calvin Liang                                                                                 | Apache 2.0           | [链接↗](https://github.com/Calvin-LL/Reorderable)                                  |
| sdl2-compat                           | Copyright (C) 2026 Sam Lantinga <slouken@libsdl.org>                                                          | Zlib License         | [链接↗](https://github.com/libsdl-org/sdl2-compat)                                 |
| SDL3                                  | Copyright (C) 1997-2026 Sam Lantinga <slouken@libsdl.org>                                                     | Zlib License         | [链接↗](https://github.com/libsdl-org/SDL)                                         |
| skinview3d                            | Copyright © 2014-2018 Kent Rasmussen; Copyright © 2017-2022 Haowei Wen, Sean Boult and contributors           | MIT License          | [链接↗](https://github.com/bs-community/skinview3d)                                |
| sora-editor                           | Copyright (C) 2020-2026  Rosemoe                                                                              | LGPL-2.1 License     | [链接↗](https://github.com/Rosemoe/sora-editor)                                    |
| StringFog                             | Copyright © 2016-2023, Megatron King                                                                          | Apache 2.0           | [链接↗](https://github.com/MegatronKing/StringFog)                                 |
| tm4e (TextMate for Eclipse)           | Copyright © Eclipse Foundation                                                                                | EPL-2.0 License      | [链接↗](https://github.com/eclipse-tm4e/tm4e)                                      |
| XZ for Java                           | Copyright © The XZ for Java authors and contributors                                                          | 0BSD License         | [链接↗](https://tukaani.org/xz/java.html)                                          |