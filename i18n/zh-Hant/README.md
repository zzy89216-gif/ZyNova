<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <b>繁體中文</b> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> 📌 **Current release: 28.2.1** · [Changelog](../../CHANGELOG.md) · [Releases](https://github.com/zzy89216-gif/ZyNova/releases/latest)

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.2.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.2.1)

---

Android 上的 **Minecraft: Java Edition** 啟動器，以 ZalithLauncher2 原始碼獨立開發與發佈，不是官方版本。

一句話講完設計：**少點幾下**。多數啟動器把設定攤開讓你挑，ZyNova 反過來——先幫你判斷，判斷不了才問一次。

## 🧭 跟其他啟動器哪裡不一樣

- **不讓你在設定裡迷路**：安裝、版本對齊、模組依賴，能自動就自動
- **模組地獄有人幫扛**：整合包幾十個模組、依賴一長串，交給流程去解
- **當機不用猜**：把日誌交給 AI 讀，比在論壇翻十頁快

## ✨ 最近變更

完整變更：[CHANGELOG.md](../../CHANGELOG.md)。

## 🛠️ 主要功能

- **AI 代理**：預設 DeepSeek — 安裝遊戲本體、整合包與模組，修改設定，讀取日誌，啟動遊戲
- **渲染器**：自動 / MobileGlues / GL4ES / LTW（實驗性）
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

> 📚 官方文件目前只有簡體中文與英文兩套（各 77 篇）；以下連結為簡體中文版，用語略有差異。

| 內容 | 檔案 |
|---|---|
| 文件中心 | [docs/zh/README.md](../../docs/zh/README.md) |
| 安裝 / 使用 / 匯出 | [docs/zh/install.md](../../docs/zh/install.md) · [docs/zh/usage.md](../../docs/zh/usage.md) |
| 渲染器 / 登入 / 收藏 / AI | [docs/zh/renderers.md](../../docs/zh/renderers.md) · [docs/zh/login.md](../../docs/zh/login.md) · [docs/zh/favorites.md](../../docs/zh/favorites.md) · [docs/zh/ai-agent.md](../../docs/zh/ai-agent.md) |
| 疑難排解 / 常見問題 / 資料與隱私 | [docs/zh/troubleshooting.md](../../docs/zh/troubleshooting.md) · [docs/zh/faq.md](../../docs/zh/faq.md) · [docs/zh/data-and-privacy.md](../../docs/zh/data-and-privacy.md) |
| 更新日誌 | [CHANGELOG.md](../../CHANGELOG.md) |
| 第三方授權 | [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) |
| 授權 | [LICENSE](../../LICENSE) |

## 💖 支持

- 回報問題：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- 信箱：<ZyNovaLauncher@outlook.com>
- 官方 X：<https://x.com/ZyNovaProject>
- 贊助：<https://afdian.com/a/hizzy>

## ⚖️ 授權

GPL-3.0，必須保持開源。第三方元件與各自的授權見
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) 與 [docs/zh/licenses.md](../../docs/zh/licenses.md)。

> ⚠️ **陶瓦聯機 Terracotta（多人連線）為 AGPL-3.0**，比其他元件更嚴格：本專案原樣散布、未修改其
> 二進位檔。若你修改後再散布，需依 AGPL-3.0 §13 公開對應原始碼。

---

**ZyNova — Context First. Less Steps.**
