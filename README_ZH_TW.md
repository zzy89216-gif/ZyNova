<p align="center">
  <img src="assets/icon.png" width="180" alt="ZyNova">
</p>

# ZyNova

> **Minecraft: Java Edition · Android Launcher**
>
> 基於 ZalithLauncher2 開源程式碼的獨立非官方專案

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Primary-Kotlin-blue)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-GPL--3.0-orange)](https://www.gnu.org/licenses/gpl-3.0.html)
[![Release](https://img.shields.io/badge/Release-27.6.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.6.1)

[简体中文](README.md) | [English](README_EN.md) | **[繁體中文](README_ZH_TW.md)** | [日本語](README_JA_JP.md) | [한국어](README_KO.md) | [Русский](README_RU.md)

---

## ✦ 簡介

**ZyNova** 是面向 Android 的 **Minecraft: Java Edition** 啟動器，基於
[ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2) 開源程式碼開發。
它**不是** ZalithLauncher2 官方版本，而是獨立維護、獨立發佈的非官方修改版。

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ 27.6.1 更新重點

- **頂部 5 個按鈕回檔**：資料夾 / AI / 多人 / 下載 / 設定恢復原樣，不再套用首頁按鈕材質
- **幽靈按鈕**：啟動 / 建立世界 / 新增伺服器 / 條目只保留約 1.5%~2% 明暗差，無描邊、保留透明度，
  圖片 / 影片背景即時透出；快捷按鈕縮小後並排放在啟動按鈕旁
- **建立世界依版本自適應**：26.1+ 直接寫新存檔結構；1.12.2~26.0 走 DataFixer；
  低於 1.12.2 隱藏入口；寫完回讀驗證
- **允許作弊**：建立世界新增開關，依 Minecraft 版本自適應
- **Fabric API 載入狀態**：載入中只顯示「正在載入中」，載入成功才允許安裝，失敗 / 為空才拒絕
- **保留 27.6.0 內容**：Fabric API 防呆、圖示修正、一鍵建立世界 / 伺服器

> 完整歷史見 [CHANGELOG.md](CHANGELOG.md)。

## 🛠️ 主要功能

- **AI Agent**：預設 DeepSeek，能讀日誌、管理模組、改設定、裝資源、啟動遊戲
- **渲染器**：自動 / MobileGlues / Ironized Zink / GL4ES
- **卡片式首頁**：依實例展示世界與伺服器，可一鍵建立 / 新增並 quickPlay 進入
- **統一資源管理**：Modrinth / CurseForge 搜尋、依賴解析、下載、SHA1 校驗、安裝
- **Fabric API 防呆**：只由 Fabric 安裝流程自動加入；不可手動安裝、刪除、停用或重複安裝

## 📦 下載與建置

- **下載**：<https://github.com/zzy89216-gif/ZyNova/releases>
  - 通用 APK（arm64-v8a / armeabi-v7a / x86 / x86_64）
  - `mapping.universal.zip`（還原混淆堆疊）
- **本機構建**：需要 JDK 17、Android SDK / NDK，然後執行：
  ```bash
  ./gradlew ZalithLauncher:assembleRelease
  ```
- 正式建置由 GitHub Actions 完成，Release 建置開啟程式碼混淆與資源壓縮。

## ⚠️ 注意

- 首頁「建立世界」在 **26.1+** 使用新存檔格式；**1.12.2~26.0** 使用相容層；
  **低於 1.12.2** 不提供此入口。
- Fabric API 由安裝流程自動處理；手動安裝會被拒絕，模組管理中也不能刪除 / 停用。
- 舊版本不支援 quickPlay 時，仍會正常啟動到遊戲主選單。
- 舊設定、既有存檔與伺服器清單不受升級影響。

## 📚 文件

| 內容 | 檔案 |
|---|---|
| 更新日誌 | [CHANGELOG.md](CHANGELOG.md) |
| 交接 / 架構 / 開發流程 | [HANDOFF.md](HANDOFF.md) |
| 第三方授權 | [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) |
| 授權條款 | [LICENSE](LICENSE) |

## ⚖️ 授權

ZyNova 以 **GPL-3.0** 發佈，必須保持開源。上游 ZalithLauncher2 的版權與授權聲明已保留；
附加條款與完整文本見 [LICENSE](LICENSE)，第三方元件授權見
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。

## 💖 支援

- 回報問題：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- 贊助：見 [assets/donate](assets/donate) 與關於頁

## 👥 維護者

ZyNova 由 **zzy** 獨立開發與維護。

---

**ZyNova — Context First. Less Steps.**
