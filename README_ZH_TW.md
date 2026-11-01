<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-27.8.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.8.1)

[简体中文](README.md) | [English](README_EN.md) | **繁體中文** | [日本語](README_JA_JP.md) | [한국어](README_KO.md) | [Русский](README_RU.md)

---

面向 Android 的 Minecraft: Java Edition 啟動器，基於 ZalithLauncher2 原始碼開發，獨立維護與發佈。

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ 27.8.0 更新重點

- **AI 是 Agent，不是聊天機器人**：說一句就能裝遊戲本體 / 裝整合包 / 裝資源 / 改實例設定，預設「完全控制」
- **下載全部靜默**：安裝不再彈窗，進度只在頂部細進度條；點開是「標題 / 步驟 / 進度 / 速度 / 取消」的明細
- **NeoForge 修好了**：鏡像涵蓋整個 maven.neoforged 倉庫，不再動不動下載失敗、卡住
- **細節**：聊天輸入框提示語輸入即隱；收藏 / 資源 / 整合包共用同一個任務佇列

## 🛠️ 主要功能

- **AI Agent**：預設 DeepSeek，裝遊戲本體 / 裝整合包 / 裝資源 / 改設定 / 讀日誌 / 啟動遊戲，直接動手
- **渲染器**：自動 / MobileGlues / Ironized Zink / GL4ES
- **資源核心**：Modrinth / CurseForge 搜尋、依賴解析、下載、校驗、安裝
- **收藏與封存**：線上收藏 + 私有目錄封存，SHA-256 去重，離線可用
- **Fabric API 防呆**：只由 Fabric 流程自動加入，不可重複安裝

## 📦 下載與建置

- 下載：<https://github.com/zzy89216-gif/ZyNova/releases>
- 建置：JDK 17 + Android SDK / NDK，執行 `./gradlew ZalithLauncher:assembleRelease`

## 📚 文件

| 內容 | 檔案 |
|---|---|
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
