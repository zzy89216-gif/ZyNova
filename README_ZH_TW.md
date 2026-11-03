<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-27.9.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.9.0)

[简体中文](README.md) | [English](README_EN.md) | **繁體中文** | [日本語](README_JA_JP.md) | [한국어](README_KO.md) | [Русский](README_RU.md)

---

面向 Android 的 Minecraft: Java Edition 啟動器，基於 ZalithLauncher2 原始碼開發，獨立維護與發佈。

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ 27.9.0 更新重點

- **移除 Ironized Zink**：維護成本過高；內建清單改為 MobileGlues、GL4ES，以及明確標示為實驗性的 LTW。
- **MobileGlues 2.0.1**：multidraw 順序可依 draw call 調整，遇到驅動問題時有地方可調。
- **收藏按鈕補齊**：資源列表與專案詳情頁都能直接收藏，不再只有收藏頁。
- **崩潰頁會說明怎麼處理**：缺少前置、模組衝突、Java 版本、記憶體不足、渲染器失敗都會給出具體建議。
- **匯出只保留 Modrinth**：其他格式連程式碼一併移除；發佈改為 universal + arm64-v8a 兩個 APK。

## 🛠️ 主要功能

- **AI Agent**：預設 DeepSeek，裝遊戲本體 / 裝整合包 / 裝資源 / 改設定 / 讀日誌 / 啟動遊戲，直接動手
- **渲染器**：自動 / MobileGlues / GL4ES / LTW（實驗性）
- **資源核心**：Modrinth / CurseForge 搜尋、依賴解析、下載、校驗、安裝
- **收藏與封存**：線上收藏 + 私有目錄封存，SHA-256 去重，離線可用
- **Fabric API 防呆**：只由 Fabric 流程自動加入，不可重複安裝

## 📦 下載與建置

- 下載：<https://github.com/zzy89216-gif/ZyNova/releases>
- 建置：JDK 17 + Android SDK / NDK，執行 `./gradlew ZalithLauncher:assembleRelease`

## 📚 文件

> 📚 **完整文件中心（72 篇，英文）**：[docs/en/README.md](docs/en/README.md)



| 內容 | 檔案 |
|---|---|
| 使用者文件中心 | [docs/en/README.md](docs/en/README.md) |
| 安裝 / 使用 / 匯出 | [docs/en/install.md](docs/en/install.md) · [docs/en/usage.md](docs/en/usage.md) |
| 渲染器 / 登入 / 收藏 / AI | [docs/en/renderers.md](docs/en/renderers.md) · [docs/en/login.md](docs/en/login.md) · [docs/en/favorites.md](docs/en/favorites.md) · [docs/en/ai-agent.md](docs/en/ai-agent.md) |
| 疑難排解 / FAQ / 資料 | [docs/en/troubleshooting.md](docs/en/troubleshooting.md) · [docs/en/faq.md](docs/en/faq.md) · [docs/en/data-and-privacy.md](docs/en/data-and-privacy.md) |
| 建置 | [docs/en/build.md](docs/en/build.md) |
| 更新日誌 | [CHANGELOG.md](CHANGELOG.md) |
| 交接 / 架構 / 開發流程 | [HANDOFF.md](HANDOFF.md) |
| 第三方授權 | [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) |
| 授權 | [LICENSE](LICENSE) |



## 💖 支持

- 回報問題：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- 贊助：<https://afdian.com/a/hizzy>

## ⚖️ 授權

GPL-3.0，必須保持開源；第三方元件見 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。

---

**ZyNova — Context First. Less Steps.**
