<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <b>繁體中文</b> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **ZyNova 28.1.0 正式發佈（全球化商業合規版）。**

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.1.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.1.0)

---

面向 Android 的 Minecraft: Java Edition 啟動器，基於 ZalithLauncher2 原始碼開發，獨立維護與發佈。

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- X（官方主帳號）：<https://x.com/ZyNovaProject>
- 信箱：國內生態 <ZyNovaLauncher@163.com> ｜ 全球生態 <ZyNovaLauncher@outlook.com>
- 官方 X：<https://x.com/ZyNovaProject>
- 國內生態信箱：<ZyNovaLauncher@163.com>
- 全球生態信箱：<ZyNovaLauncher@outlook.com>

## ✨ 28.1.0 更新重點

- **首次啟動合規流程**：3 份合併文件（①EULA + 隱私政策 ②第三方開源合規聲明 + 補充免責與風險告知 ③使用教學 + 適用地區與法律管轄），每份**必須滑到底部並單獨勾選**，全部完成後再選年齡（舊用戶升級也會走一次）；**未滿 18 歲僅能在週五、週六、週日與法定節假日 20:00–21:00 啟動遊戲**

## 🛠️ 主要功能

- **AI Agent**：預設 DeepSeek，裝遊戲本體 / 裝整合包 / 裝資源 / 改設定 / 讀日誌 / 啟動遊戲，直接動手
- **渲染器**：自動 / MobileGlues / GL4ES / LTW（實驗性）
- **資源核心**：Modrinth / CurseForge 搜尋、依賴解析、下載、校驗、安裝
- **收藏與封存**：線上收藏 + 私有目錄封存，SHA-256 去重，離線可用
- **前置 API 防呆**：Fabric API / Quilted Fabric API / Legacy Fabric API 由安裝流程自動加入，
  不可重複安裝、搜不到、刪不掉、也禁不住

## 📦 下載與建置

- 下載：<https://github.com/zzy89216-gif/ZyNova/releases>
- 建置：JDK 17 + Android SDK / NDK，執行 `./gradlew ZalithLauncher:assembleRelease`

## 📚 文件

> 📚 **完整文件中心（77 篇，英文）**：[docs/en/README.md](../../docs/en/README.md)

| 內容 | 檔案 |
|---|---|
| 使用者文件中心 | [docs/en/README.md](../../docs/en/README.md) |
| 安裝 / 使用 / 匯出 | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| 渲染器 / 登入 / 收藏 / AI | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/favorites.md](../../docs/en/favorites.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| 疑難排解 / FAQ / 資料 | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| 建置 | [docs/en/build.md](../../docs/en/build.md) |
| 更新日誌 | [CHANGELOG.md](../../CHANGELOG.md) |
| 交接 / 架構 / 開發流程 | [HANDOFF.md](../../HANDOFF.md) |
| 接手入口（狀態 / 發版流程 / 坑 / 文件規則） | [AGENTS.md](../../AGENTS.md) |
| 第三方授權 | [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) |
| 授權 | [LICENSE](../../LICENSE) |

## 💖 支持

- 回報問題：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- 贊助：<https://afdian.com/a/hizzy>

## ⚖️ 授權

GPL-3.0，必須保持開源；第三方元件見 [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md)。

---

**ZyNova — Context First. Less Steps.**
