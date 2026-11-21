<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a> · <b>文言</b>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> 📌 **今本：28.2.4** · [沿革](../../CHANGELOG.md) · [發佈](https://github.com/zzy89216-gif/ZyNova/releases/latest)

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.2.4-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.2.4)

---

夫 Android 上之 **Minecraft: Java Edition** 啟動器也，本 ZalithLauncher2 之源碼而別治之，非官本也。

其設計一言以蔽之：**少點幾下**。凡可預判者，先為汝決；不能決者，方問一次。

## 🧭 與諸啟動器之異

- **不使汝迷於設定**：安裝、版本相配、模組依賴，可自動者皆自動之
- **模組之困有人任之**：整合包數十模組、依賴連綿，悉付之於流水之序
- **崩潰不須妄測**：以誌授 AI 讀之，勝於論壇翻檢十頁

## ✨ 近事

詳見 [CHANGELOG.md](../../CHANGELOG.md)。

## 🛠️ 主要之能

- **AI 代事**：預設 DeepSeek —— 裝遊戲本體、整合包與模組，改設定，讀誌，啟遊戲
- **渲染器**：自動 / MobileGlues / Ironized Zink / GL4ES / LTW（試行）
- **資源之核**：Modrinth / CurseForge 搜求、依賴解析、下載、校驗、安裝
- **收藏與封存**：線上收藏 + 私有目錄封存，以 SHA-256 別其重
- **前置 API 防誤**：Fabric API / Quilted Fabric API / Legacy Fabric API 由安裝之序自附，不可重裝、不可搜得、亦不可刪

## 📦 下載與自建

- 下載：<https://github.com/zzy89216-gif/ZyNova/releases>（認其最新者）
- 自建：JDK 17 + Android SDK / NDK，行 `./gradlew ZalithLauncher:assembleRelease`
- 注意：初啟之時，示合規之序；條款之詳見 [docs/zh/licenses.md](../../docs/zh/licenses.md)

## 📚 文書

> 📚 官文惟簡體中文與英文二套（各 78 篇）；下列之文皆指簡體中文之本，用語微異。

| 內容 | 檔案 |
|---|---|
| 文書之府 | [docs/zh/README.md](../../docs/zh/README.md) |
| 安裝 / 使用 / 匯出 | [docs/zh/install.md](../../docs/zh/install.md) · [docs/zh/usage.md](../../docs/zh/usage.md) |
| 渲染器 / 登入 / 收藏 / AI | [docs/zh/renderers.md](../../docs/zh/renderers.md) · [docs/zh/login.md](../../docs/zh/login.md) · [docs/zh/favorites.md](../../docs/zh/favorites.md) · [docs/zh/ai-agent.md](../../docs/zh/ai-agent.md) |
| 疑難 / 常見 / 數據與隱私 | [docs/zh/troubleshooting.md](../../docs/zh/troubleshooting.md) · [docs/zh/faq.md](../../docs/zh/faq.md) · [docs/zh/data-and-privacy.md](../../docs/zh/data-and-privacy.md) |
| 沿革 | [CHANGELOG.md](../../CHANGELOG.md) |
| 第三方授權 | [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) |
| 授權 | [LICENSE](../../LICENSE) |

## 💖 相助

- 報問題：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- 信箱：<ZyNovaLauncher@outlook.com>
- 官方 X：<https://x.com/ZyNovaProject>
- 贊助：<https://afdian.com/a/hizzy>

## ⚖️ 授權

GPL-3.0，必守開源。第三方元件與各自授權見
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) 與 [docs/zh/licenses.md](../../docs/zh/licenses.md)。

> ⚠️ **陶瓦聯機 Terracotta（多人連線）為 AGPL-3.0**，較諸元件為嚴：本專案如其原樣而布之，未改其
> 二進位。若汝改而復布，須依 AGPL-3.0 §13 公開相應源碼。

---

**ZyNova — Context First. Less Steps.**
