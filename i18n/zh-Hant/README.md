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

Android 上的 **Minecraft: Java Edition** 啟動器，以 ZalithLauncher2 原始碼自行開發與發佈，並非官方版本。

目標是「**少點幾下**」：安裝遊戲、整理模組與整合包、調整設定、讀取日誌這類能自動化的事，都交給 AI 代理處理。

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- X（官方主帳號）：<https://x.com/ZyNovaProject>
- 國內生態信箱（QQ 信箱也可寄這裡）：<ZyNovaLauncher@163.com>
- 全球生態信箱：<ZyNovaLauncher@outlook.com>

## ✨ 28.1.0 更新重點

- **首次啟動合規流程**：3 份合併文件（① EULA + 隱私政策、② 第三方開源合規聲明 + 補充免責與風險告知、③ 使用教學 + 適用地區與法律管轄），每份都**必須滑到最底部並單獨勾選**才能繼續，全部完成後再選年齡層（舊用戶升級後同樣會走一次）。
- **未成年人時段限制**：未滿 18 歲僅能在週五、週六、週日與法定假日 **20:00–21:00** 啟動遊戲；2026 年假日依中國國務院通知（国办发明电〔2025〕7号）逐日列出，補班日以工作日處理。
- **免責聲明寫完整**：存檔遺失、裝置發熱、第三方封鎖帳號、服務停止等風險都明確告知。
- **同時包含**：首頁卡片不再卡在載入、AI 聯網搜尋可用、匯入改走實例目錄、已移除當機自動修復。

## 🛠️ 主要功能

- **AI 代理**：預設 DeepSeek — 安裝遊戲本體、整合包與模組，修改設定，讀取日誌，啟動遊戲
- **渲染器**：自動 / MobileGlues / GL4ES / LTW（實驗性）
- **資源核心**：Modrinth / CurseForge 搜尋、依賴解析、下載、校驗、安裝
- **收藏與封存**：線上收藏 + 私有目錄封存，以 SHA-256 去重
- **前置 API 防呆**：Fabric API / Quilted Fabric API / Legacy Fabric API 由安裝流程自動加入，無法重複安裝、搜尋不到、也刪不掉

## 📦 下載與建置

- 下載：<https://github.com/zzy89216-gif/ZyNova/releases>（請認明最新版）
- 自行建置：JDK 17 + Android SDK / NDK，執行 `./gradlew ZalithLauncher:assembleRelease`
- 注意：首次啟動會顯示合規流程；條款細節見 [docs/en/licenses.md](../../docs/en/licenses.md)。

## 📚 文件

> 📚 文件目前只有中文與英文兩套（各 77 篇）；本頁是繁體中文介紹。

| 內容 | 檔案 |
|---|---|
| 文件總覽 | [docs/en/README.md](../../docs/en/README.md) |
| 安裝 / 使用 / 匯出 | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| 渲染器 / 登入 / 收藏 / AI | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/favorites.md](../../docs/en/favorites.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| 疑難排解 / 常見問題 / 資料與隱私 | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| 建置 | [docs/en/build.md](../../docs/en/build.md) |
| 更新日誌 | [CHANGELOG.md](../../CHANGELOG.md) |
| 交接 / 架構 / 開發流程 | [HANDOFF.md](../../HANDOFF.md) |
| 接手入口（現況 / 發版流程 / 注意事項 / 文件規則） | [AGENTS.md](../../AGENTS.md) |
| 第三方授權 | [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) |
| 授權 | [LICENSE](../../LICENSE) |

## 💖 支持

- 回報問題：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- 信箱：全球 <ZyNovaLauncher@outlook.com> ｜ 國內 <ZyNovaLauncher@163.com>
- 官方 X：<https://x.com/ZyNovaProject>
- 贊助：<https://afdian.com/a/hizzy>

## ⚖️ 授權

GPL-3.0，必須保持開源。第三方元件與各自的授權見
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) 與 [docs/en/licenses.md](../../docs/en/licenses.md)。

> ⚠️ **陶瓦聯機 Terracotta（多人連線）為 AGPL-3.0**，比其他元件更嚴格：本專案原樣散布、未修改其
> 二進位檔。若你修改後再散布，需依 AGPL-3.0 §13 公開對應原始碼。

---

**ZyNova — Context First. Less Steps.**
