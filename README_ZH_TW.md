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
[![Release](https://img.shields.io/badge/Release-27.1.2-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.1.2)

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

## ✨ 27.1.2 主要內容

- **移除上游 Client ID，並改寫 Git 歷史。** 應上游 ZalithLauncher2 專案的要求，
  其 Microsoft 應用註冊 ID 已不再出現在本倉庫的任何位置，
  且歷史已被改寫以徹底清除（移除 3 個提交；全物件掃描 0 命中）。
  **因此所有提交雜湊與 tag 都已改變**，舊的複製需要重新拉取。
- **AI：現在能看到資料流向。** 首次開啟 AI 設定時會說明：
  遊戲日誌、模組清單、設定檔內容與你輸入的文字都會傳送給你設定的服務商，
  ZyNova 不儲存也不轉送，API Key 只留在本機。
- **AI：寫入操作稽核記錄。** Agent 的每一次寫入操作都會在本地留下記錄
  （時間、工具、風險等級、參數、結果），可在 AI 設定中查看或匯出。
- **Agent 權限模式預設改為「操作確認」**，全新使用者在 AI 改設定、刪模組、
  寫檔案或裝資源之前都會被詢問一次。
- **恢復 OpenGL 版本選擇**（Ironized Zink，4.6 / 4.5 / 4.3 / 3.3）。
  4 個官方預設都使用 4.6，而驅動對 Vulkan→Zink 4.6 轉譯不完整的裝置原本無處可調。
  其餘 12 個底層參數仍然不暴露。

## ✨ 27.1.1 主要內容

- **移除工具呼叫次數上限。** 以前 12 輪就停，現在**不設上限**，
  Agent 會一直做到模型自己認為完成為止，你隨時可以按「停止」。
  唯一保留的是**重複呼叫保護**：同一個工具用**完全相同的參數**重複超過 6 次才會判定為死迴圈並停止。
- **歷史對話 + 側邊欄。** 對話會自動存在本機，重開啟動器還在。
  從左上角拉開側邊欄，可以查看、切換、刪除或新增對話。
- **介面優化。** 工具執行期間會顯示「執行中」（安裝模組不再像卡死）、
  自動捲動只在你已經貼底時跟隨、可以用鍵盤的送出鍵、清空對話會先二次確認。
- **修復。** 按「停止」現在能立刻中斷串流；`read_log` 無法再用 `../` 讀取允許目錄之外的檔案；
  超大日誌不再有記憶體爆掉的風險；切換對話不會再把舊訊息寫進新對話。
- **在地化。** 使用者可見的 AI 錯誤訊息全部翻譯（英 / 簡 / 繁 / 日），
  而且助手會**用你提問的語言**回答。

## ✨ 27.1.0 主要內容

- **全域 AI Agent。** 頂部工具列「檔案」旁新增 **AI** 按鈕，點擊**直接進入聊天介面**，
  不設獨立的 AI 首頁。聊天與 Agent 是**同一個入口**：先問「為什麼這個實例進不去？」，
  它會去讀日誌；接著說「幫我修」，它會**直接呼叫工具動手處理**，而不是寫一段教學給你。
- **AI 真的能操作啟動器。** 共 25 個工具，全部建立在啟動器現有系統之上：
  實例、模組真實中繼資料、啟用／停用／刪除模組、資源包、光影、存檔、檔案、日誌、
  崩潰報告、**118 項啟動器設定**、資源搜尋與安裝（含遞迴安裝必需前置），
  以及**真正啟動遊戲**。
- **AI 設定與啟動器設定完全分開。** 服務商（OpenAI / Anthropic，可擴充）、
  自備 API Key（僅存本機）、模型清單**由服務商動態取得，程式碼中不硬編碼任何模型名稱**、
  可自訂 Base URL，以及 Agent 權限模式（完全控制／操作確認）。
- **修復：自動安裝前置依賴的多個靜默失敗。** 五處缺陷都表現成同一個現象——
  介面顯示「安裝成功」，進遊戲卻因為缺少前置而崩潰。

## ✨ 26.4.2 主要內容

- **正版登入改用 ZyNova 自己的 Microsoft 應用註冊**：26.4.0 / 26.4.1 因為只有處於 Minecraft
  應用程式允許名單中的註冊才能存取 Minecraft Services，借用了**上游 ZalithLauncher2 專案**的
  應用註冊；從 26.4.2 起改用 **ZyNova 自己的註冊**
- **更換應用圖示**：自適應圖示（背景 / 前景 / 單色三層）、傳統方圖示與圓形圖示（各 5 個密度）、
  Google Play 商店圖示（512×512）**全部換成新版 ZyNova 圖示**。
  畫作放在自適應圖示的系統可見區之內，**人物與 `ZyNova` 字樣完整可見、不被裁切**。
  **應用內的解壓介面（首次啟動 / 更新後出現的那一屏）未做任何改動**
- 除此之外**沒有其他功能變化**：認證流程、渲染器、資源管理等與 26.4.1 一致

**ℹ️ 正版登入用的是哪個微軟應用（請勿與專案歸屬混淆）**

**ZyNova 是 ZalithLauncher2 的非官方分支（fork），兩者是不同專案、獨立維護。**
兩個應用註冊的關係如下：

| | 顯示名稱 | Client ID | Mojang 允許名單 | 本版本是否使用 |
|---|---|---|---|---|
| **ZyNova 自己的應用註冊** | ZyNova Launcher | `7b66e168-f8cd-43fc-a52d-2e78dba189b0` | ✅ **已獲批准（2026-09-30）** | ✅ **正在使用** |
| **ZalithLauncher2 的應用註冊** | ZalithLauncher（上游） | （已從本倉庫移除） | ✅ 已獲批准 | ❌ 不使用（26.4.0 / 26.4.1 曾使用） |

- **只有處於 Minecraft 應用程式允許名單中的應用註冊**才能完成正版登入；
  否則 `POST /authentication/login_with_xbox` 會回傳
  `403 Invalid app registration, see https://aka.ms/AppRegInfo`，
  與 OAuth / Xbox Live / XSTS 是否成功無關
- ✅ **ZyNova 自己的應用註冊已經進入該允許名單。** Mojang Enforcement 於 **2026-09-30**
  完成 AppID 審核，並明確告知該批次的申請
  「**met the required criteria and have been approved for our allow list**」。
  **因此目前版本的正版登入可以正常完成——不再需要借用上游專案的應用註冊。**
- Client ID 透過倉庫 Secret `OAUTH_CLIENT_ID` **在建置時注入**，不寫死在原始碼中；
  取值優先順序：**環境變數（CI Secret）> `.oauth_client_id.txt` > `ZalithLauncher/gradle.properties`**
- **你會看到的現象**：微軟帳號的「已連接的應用程式 / 應用程式與裝置」頁面會以上表中的**顯示名稱**出現
- 仍然**只需要「公開客戶端 + Client ID」**：不需要 Client Secret、不需要 Redirect URI、不需要 SHA-1

## ✨ 26.4.1 主要內容

- **正版登入恢復可用（核心修復）**：26.4.0 的登入會在 Microsoft OAuth、Xbox Live、XSTS
  **三步全部成功**之後卡在 Minecraft Services。真實原因是 **Mojang 端的應用註冊允許名單**——
  Minecraft Services 回傳 **HTTP 403 `Invalid app registration`**
  （與程式碼、Entra 設定、IP、網路、請求頻率、帳號檔案**都無關**）。
  26.4.1 改為**使用一個已獲 Mojang 批准的 Microsoft 應用註冊**建置，登入現已可用；
  26.4.2 起已切換為 ZyNova 自己的應用註冊（見上）
- **登入失敗不再遺失真實錯誤**：此前日誌裡只有一個 `message` 恆為 `null` 的例外
  （`il6: null`），真實 HTTP 狀態碼與伺服端回傳內容完全沒有被記錄。
  現在 `login_with_xbox` 與 `getPlayerProfile` 失敗時會記錄
  **請求 URL、真實 HTTP 狀態碼、回應內容**與完整例外堆疊
  （記錄的是非 2xx 的錯誤 JSON，不含 `access_token`）
- **修正 403 的錯誤歸因**：Minecraft Services 的 403 有兩種完全不同的含義——
  `BLOCKED_IP`（IP 被禁止）與 `Invalid app registration`（Client ID 未獲 Mojang 授權）。
  此前一律顯示為「當前 IP 位址已被禁止登入」，把排查方向錯誤地引向網路與 IP；
  現在依回應內容區分，後者有獨立文案

## ✨ 26.4.0 主要內容

- **恢復 Microsoft（正版）登入**：帳號登入選單重新提供 **「微軟帳號」**，
  走 OAuth 2.0 **裝置代碼流程**（取得裝置代碼並自動複製到剪貼簿 → 開啟驗證網頁 → 輪詢換取權杖）。
  認證後端從未被移除，26.1.0 只切斷了 UI 與 ViewModel 的接線，本次原樣還原。
  **不需要 Client Secret，也不需要 Redirect URI / SHA-1**。
- **修復下載資源頁「類別」篩選器內容為空**（Issue #6）：
  預設「所有平台」下類別清單改以參照來源（CurseForge）為準，且類別條件只下發給該來源；
  多來源時標題會標註來源，例如「類別（僅 CurseForge）」。
- **修復資源列表卡片缺少「資源的類別」徽章**（Issue #6）：
  列表呼叫時漏傳 `classes` 實參，導致 模組 / 資源包 / 光影包 / 存檔 / 整合包 徽章永不渲染。
- **Ironized Zink 設定面板只保留 4 個官方預設**（Issue #7）：
  移除 OpenGL 版本下拉與 12 個參數開關。
  ⚠️ 4 個預設的 OpenGL 版本都是 4.6，因此新使用者無法再降到 4.5 / 4.3 / 3.3。

## ✨ 26.3.0 主要內容

- **渲染器體系重做 —— 內建渲染器只保留三個**：
  **Ironized Zink**（改為預設）、**GL4ES**、**MobileGlues**。
  移除：Krypton Wrapper（NG-GL4ES）、Kopper Zink、VirGL、Freedreno、Panfrost，
  以及只有它們會用到的原生函式庫。
- **Ironized Zink 完整整合**：**13 個可調參數**與 **4 個官方預設**
  （Potato / Performance / Default / Max Compatibility），移植自上游（作者 GoyDevv，GPL-3.0）。
  在 **設定 → 渲染器** 選中它之後，下方會**立刻展開完整設定面板**。
- **預設渲染器改為 Ironized Zink 的 Default 預設。**
- **修復首頁向下捲動時卡片異常移動** —— 頁面捲動被誤判成排序的「讓位」位移，
  於是每張卡片都把自己動到錯誤位置。
- **只發布一個通用版本 APK**（含全部 4 個 ABI，並保留程式碼混淆）。
- **逐元件授權聲明** —— 見 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。
- **外掛渲染器不受影響**：FCL / Zalith 渲染器外掛與 `fclPlugin_V2` 外掛
  仍然可以把更多渲染器加進清單。

## ✨ 26.2.6 主要內容

- **修復** 拖動排序後卡片內容被推到錯誤位置
  （位移修飾符套在 `clip` / `background` 內側；讓位動畫被中斷後也會殘留偏移）
- **變更**：操作選單現在可以**長按整塊拖到螢幕左側或右側**
  （移植自上游 ZalithLauncher2：提起跟手、預覽停泊側、平滑停泊，內容區一起讓位，停泊側會被記住）
- **修復** 右側選單恢復上游的排版

## ✨ 26.2.5 主要內容

- **修復** 卡片式首頁卡片外圈的「邊框」：卡片原本疊了多層
  （圓角填充 + 縮放圖層 + 陰影），導致內邊距一圈比內容區暗；
  現在卡片只保留**一層**背景，並移除陰影
- **新增** 卡片式首頁支援**長按拖動排序**：版本卡片之間、卡片內的世界 / 伺服器，
  以及右側選單的三塊（帳號頭像 / 版本列 / 啟動按鈕）都可以拖動調整順序，且順序會被記住

## ✨ 26.2.4 主要內容

本版本處理兩個新回報：

- **修復** 卡片式首頁的卡片不透明度未跟隨「背景元素不透明度」設定
  （卡片顏色被寫死為 `cardColor(false)`，等於關閉了「受背景內容影響」）
- **新增** 卡片式首頁的**卡片大小**設定
  （設定 → 啟動器 → 首頁 → 卡片大小，70% ~ 140%，預設 100% 維持原有外觀）

## ✨ 26.2.3 主要內容

本版本集中修復**模組以外**的資源下載鏈路（整合包 / 資源包 / 存檔 / 光影包）：

- **修復** 資源包 / 光影包 / 存檔搜尋結果幾乎為空
  （載入器過濾條件被誤用在並不依載入器分類的資源類型上）
- **修復** 聚合搜尋總頁數變成 0（介面顯示「1 / 0」且完全無法翻頁）
- **修復**「所有平台」會去請求並不支援該資源類型的來源（存檔在 Modrinth 上並不存在）
- **修復** 未設定 CurseForge API Key 時 CurseForge 側完全搜不到資源（此時保留 MCIM 鏡像源）
- **修復** 存檔「類別」過濾器永久不可用、存檔解壓失敗後殘留 `.zip`

## ✨ 26.2.2 主要內容

本版本以修復使用者回報的問題為主：

- **移除** **⚠️極致** 玻璃檔，玻璃效果簡化為 **關閉 / 啟用動態玻璃** 兩檔
  ——「極致」檔會連同承載文字的圖層一起模糊，導致字體明顯模糊
- **修復** 一鍵安裝偶發 `No compatible version found for this instance`：
  切換搜尋平台後模組載入器過濾條件被丟棄，結果中混入其他載入器的資源
- **改進** 安裝失敗提示：改為本地化，並顯示目標實例的 Minecraft 版本與模組載入器
- **修復** 前置依賴解析失敗不再被靜默丟棄，會記錄日誌並回報
- **修復** Discord 邀請連結全部失效（舊臨時邀請已過期，改為永久邀請）

## ✨ 26.2.1 主要內容

- **重做**：**⚠️極致** 玻璃效果改用真實 GPU 著色器（多重取樣模糊 + 波紋折射扭曲），
  此前只是幾乎看不見的漸層
- **修復**：搜尋結果卡片現在提供一鍵安裝按鈕
- **修復**：模組載入器會依目前實例自動選取
- **主頁**：改為**以版本為模組**，每個模組直接列出自己的世界與伺服器
- **新增**：**所有** 平台選項，將 CurseForge 與 Modrinth 結果合併到同一份清單
- **變更**：排序方式預設改為**總下載量**

### 26.2.0 主要內容

- **修復**：從「版本設定 → 資源管理」進入下載中心時，資源安裝上下文遺失
  （NavKey 上的 `@Transient` 欄位會被 Navigation3 的 saveable 序列化機制丟棄）
- **改進**：資源搜尋現在會自動依目前實例的 Minecraft 版本過濾，不需再手動選擇版本
- **新增**：玻璃效果 **⚠️極致** 檔（啟用前會顯示效能警告）

### 26.1.0 主要內容

本版本目標是進一步脫離 ZalithLauncher2 的遺留邏輯，
建立 ZyNova 自己的資源管理、下載、首頁與 UI 基礎。

> **Context First. Less Steps.**

| 功能 | 狀態 |
|---|:---:|
| 卡片式首頁（以版本為模組，模組內顯示世界與伺服器） | ✅ |
| 統一資源管理核心 | ✅ |
| 資源來源 Provider（Modrinth / CurseForge） | ✅ |
| 統一下載管理器（佇列 / 併發 / 續傳 / 重試 / 校驗） | ✅ |
| 極簡資源安裝（帶上下文直接安裝） | ✅ |
| 玻璃效果兩檔（關閉 / 啟用動態玻璃；原本的標準 / 增強 / 極致已於 26.2.2 移除） | ✅ |
| Minecraft 26.4 Snapshot 1 Vulkan 偵測與相容性判斷 | ✅ |
| 啟動器自有更新體系（GitHub Releases） | ✅ |
| 按需載入與效能策略 | ✅ |

### 渲染器

26.3.0 起，啟動器**內建的渲染器只有三個**：

| 渲染器 | 說明 | 設定 |
|---|---|---|
| **Ironized Zink**（預設） | 桌面 OpenGL 4.6 經 Vulkan 轉譯（Mesa Zink + Kopper），作者 GoyDevv | **4 個官方預設**（26.4.0 起面板只提供預設） |
| **GL4ES** | 經典 OpenGL 轉譯層 | 保持預設 |
| **MobileGlues** | 把桌面 OpenGL 轉譯到裝置的 OpenGL ES 3.x | 保持上游預設 |

**Ironized Zink 的 4 個官方預設**（參數值與上游一致）：

| 預設 | 定位 | Minecraft | 光影 |
|---|---|---|---|
| **Potato** | 絕對最高幀率 | 1.8 → 最新（含 26.x） | 不推薦 |
| **Performance** | 高幀率 + Sodium | 1.20.x → 最新 | 輕量 |
| **Default** | 均衡 Zink + 光影（**預設**） | 1.16.x → 最新 | 完整（Iris / OptiFine） |
| **Max Compatibility** | 什麼都能跑 | 全部版本 | 完整 + 重度光影包 |

**26.4.0 起，面板只提供上面 4 個預設**：OpenGL 版本下拉與 12 個單獨參數開關已被移除，
一般使用者不再需要手動調整底層參數。選擇預設仍會一次寫入整組參數。

> ⚠️ 4 個預設的 OpenGL 版本都是 **4.6**。移除版本下拉後，**新使用者無法再把 OpenGL 版本
> 降到 4.5 / 4.3 / 3.3**；而在 26.3.0 期間手動改過參數的老使用者，其已儲存的取值仍會繼續
> 生效（依舊會被注入為環境變數），但已無法在介面上修改 —— 重新選擇任意預設即可把全部
> 參數一次寫回受支援的組合。

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