<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <b>繁體中文</b> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a> · <a href="../lzh/README.md">文言</a>
</sub></p>

<p align="center">
  <img src="../../docs/assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> 📌 **Current release: 28.4.0** · [Changelog](../../meta/CHANGELOG.md) · [Releases](https://github.com/zzy89216-gif/ZyNova/releases/latest)

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.4.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.4.0)

本版彙整先前全部預發行的內容；預發行 Release 已移除，僅保留正式版。

---

Android 上的 **Minecraft: Java Edition** 啟動器，以 ZalithLauncher2 原始碼獨立開發與發佈，不是官方版本。

一句話講完設計：**少點幾下**。多數啟動器把設定攤開讓你挑，ZyNova 反過來——先幫你判斷，判斷不了才問一次。

## 🧭 跟其他啟動器哪裡不一樣

- **不讓你在設定裡迷路**：安裝、版本對齊、模組依賴，能自動就自動
- **模組地獄有人幫扛**：整合包幾十個模組、依賴一長串，交給流程去解
- **當機不用猜**：把日誌交給 AI 讀，比在論壇翻十頁快

## ✨ 最近變更

**28.4.0**（最新）：主要變更

- 首頁改版：最近遊玩世界＋全部世界清單＋自訂首頁資訊。
- 移除「個人化」頁面、修正渲染器詳細參數標題、更新 Discord 連結。
- 刪除版本改為全螢幕確定進度。
- 移除首頁中央舊的依版本分組世界卡片（#100）。
- 修正首頁空狀態誤判：所有版本隱藏時不再提示下載。
- 回退 28.3.2 中導致部分資源管理介面崩潰的改動（清單快取／圖示全域快取／外掛非同步載入），僅保留刪除版本全螢幕修正。

> 介面視覺維持 28.3.0，未包含動態玻璃、折射與磁吸效果。

本版為 **28.3.0** 正式版（由 28.2.4 預發行版轉正），重點如下：

- **可同時下載、安裝多個項目**：遊戲本體、整合包與資源能一次排進多個，各自有進度、速度與取消；重複點同一個目標會直接提示，不再默默沒反應。
- **主頁卡片拖曳更穩**：長按拖曳換位不再閃爍、不再閃退；快速拖曳也能逐張換位，橫向拖曳同樣支援。
- **音樂播放器補齊**：同名 `.lrc` 歌詞自動載入，也能在頁面匯入；通知欄顯示上一行＋當前行；頂欄新增迷你播放條（歌名＋播放／暫停＋切歌）；暫停時進度與時長仍正確顯示；橫屏／矮螢幕版面不再溢出。
- **「返回遊戲」不再殘留**：遊戲結束後入口立刻收起，就算點在收起前一刻也不會崩潰。
- **AI 強化**：可同時安裝多個遊戲本體；新增音樂控制工具（工具總數 53）。
- **遊戲內選單新增「多人聯機」**：收起選單回到啟動器並直接開啟多人聯機頁，遊戲不會退出、繼續在背景執行。

完整變更：[CHANGELOG.md](../../meta/CHANGELOG.md)。

## 🛠️ 主要功能

- **AI 代理**：預設 DeepSeek — 安裝遊戲本體、整合包與模組，修改設定，讀取日誌，啟動遊戲
- **渲染器**：自動 / MobileGlues / Ironized Zink / GL4ES / LTW（實驗性）
- **資源核心**：Modrinth / CurseForge 搜尋、依賴解析、下載、校驗、安裝
- **收藏與封存**：線上收藏 + 私有目錄封存，以 SHA-256 去重
- **前置 API 防呆**：Fabric API / Quilted Fabric API / Legacy Fabric API 由安裝流程自動加入，無法重複安裝、搜尋不到、也刪不掉

## 🔤 用語對照（台灣／香港）

| 這裡的說法 | 你可能看到的其他寫法 |
|---|---|
| 模組 | MOD、插件（較少用） |
| 整合包 | MOD 包、模組包 |
| 光影 | Shader、著色器 |
| 存檔 | 世界存檔、地圖 |
| 啟動器 | Launcher |

## 📦 下載與建置

- 下載：<https://github.com/zzy89216-gif/ZyNova/releases>（認明最新版）
- 自行建置：JDK 17 + Android SDK / NDK，執行 `./gradlew ZalithLauncher:assembleRelease`
- 注意：首次啟動會顯示合規流程，條款細節見 [docs/zh/licenses.md](../../docs/zh/licenses.md)（簡體）

## 📚 文件

> 📚 官方文件目前只有簡體中文與英文兩套（各 78 篇）；以下連結為簡體中文版，用語略有差異。

| 內容 | 檔案 |
|---|---|
| 文件中心 | [docs/zh/README.md](../../docs/zh/README.md) |
| 安裝 / 使用 / 匯出 | [docs/zh/install.md](../../docs/zh/install.md) · [docs/zh/usage.md](../../docs/zh/usage.md) |
| 渲染器 / 登入 / 收藏 / AI | [docs/zh/renderers.md](../../docs/zh/renderers.md) · [docs/zh/login.md](../../docs/zh/login.md) · [docs/zh/favorites.md](../../docs/zh/favorites.md) · [docs/zh/ai-agent.md](../../docs/zh/ai-agent.md) |
| 疑難排解 / 常見問題 / 資料與隱私 | [docs/zh/troubleshooting.md](../../docs/zh/troubleshooting.md) · [docs/zh/faq.md](../../docs/zh/faq.md) · [docs/zh/data-and-privacy.md](../../docs/zh/data-and-privacy.md) |
| 更新日誌 | [CHANGELOG.md](../../meta/CHANGELOG.md) |
| 第三方授權 | [THIRD_PARTY_NOTICES.md](../../meta/THIRD_PARTY_NOTICES.md) |
| 授權 | [LICENSE](../../LICENSE) |

## 💖 支持

- 回報問題：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/dbHTApYNYJ>
- 信箱：<ZyNovaLauncher@outlook.com>
- 官方 X：<https://x.com/ZyNovaProject>
- 贊助：<https://afdian.com/a/hizzy>

## ⚖️ 授權

GPL-3.0，必須保持開源。第三方元件與各自的授權見
[THIRD_PARTY_NOTICES.md](../../meta/THIRD_PARTY_NOTICES.md) 與 [docs/zh/licenses.md](../../docs/zh/licenses.md)。

> ⚠️ **陶瓦聯機 Terracotta（多人連線）為 AGPL-3.0**，比其他元件更嚴格：本專案原樣散布、未修改其
> 二進位檔。若你修改後再散布，需依 AGPL-3.0 §13 公開對應原始碼。

---

**ZyNova — Context First. Less Steps.**
