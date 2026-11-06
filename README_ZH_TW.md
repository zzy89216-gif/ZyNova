<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.0.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.0.1)

[简体中文](README.md) | [English](README_EN.md) | **繁體中文** | [日本語](README_JA_JP.md) | [한국어](README_KO.md) | [Русский](README_RU.md)

---

面向 Android 的 Minecraft: Java Edition 啟動器，基於 ZalithLauncher2 原始碼開發，獨立維護與發佈。

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ 28.0.1 更新重點

- **樱花內網穿透真的能連了**：`frpc` 改用官方參數 `frpc -f <存取金鑰>:<隧道ID>`（舊版用環境變數，樱花版 frpc 不認，一啟動就退出）
- **重新整理不再報錯**：相容 BOM / 陣列 / `{data}` 包裝的回應；連線位址會從 frpc 日誌抽出來，**置頂顯示並可一鍵複製**
- **返回鍵不會再一次退出整個介面**：頂部返回不再借系統返回鍵（原本沒有回呼接住，會直接結束介面），一次只退一層，也不會把堆疊彈空
- **遊戲內可以回啟動器了**：遊戲內選單第一項「回到啟動器主介面」**不會停止遊戲**；啟動器底部會出現「返回遊戲」膠囊，點一下回到同一局
- **建隧道不用懂連接埠與類型**：類型固定 TCP，連接埠自動從 `/proc/net/tcp` 認出來，節點依帳號等級過濾
- **AI 可以全自動開聯機**：`frp_status` / `frp_create_tunnel` / `frp_start_tunnel` / `frp_stop_tunnel` / `multiplayer_status`

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
