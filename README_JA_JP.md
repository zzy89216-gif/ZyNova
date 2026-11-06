<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.0.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.0.0)

[简体中文](README.md) | [English](README_EN.md) | [繁體中文](README_ZH_TW.md) | **日本語** | [한국어](README_KO.md) | [Русский](README_RU.md)

---

Android 向けの Minecraft: Java Edition ランチャー。ZalithLauncher2 のソースを基に独自に開発・配布しています。

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ What's new in 28.0.0

- **Back navigation that makes sense**: the five capsule entries are tabs — back always returns to the main menu; inside a tab (settings sub-pages, download sub-pages, personalization → more settings) it steps back one level instead of jumping to an unrelated tab
- **Home empty state**: a "Download an instance" button that jumps straight to Downloads → Game
- **Palette entry**: real palette icon with a selected state, so tapping it gives feedback
- **AI chat layout**: fixed the ragged spacing of the "Run command" rows (empty assistant messages no longer take a list slot)
- **SakuraFrp**: new-tunnel dialog auto-selects a node, shows readable validation errors, reloads nodes when empty, and the battery card alignment is fixed
- Everything from 27.10.x (glass foundation, personalization page, Quilt protection) is now in a stable build

## 🛠️ 主な機能

- **AI Agent**：既定は DeepSeek。ゲーム本体・MOD パック・MOD の導入、設定変更、ログ閲覧、起動まで実行
- **レンダラー**：自動 / MobileGlues / GL4ES / LTW（実験的）
- **リソース基盤**：Modrinth / CurseForge 検索、依存解決、ダウンロード、検証、導入
- **お気に入りと保管**：オンラインのお気に入り + アプリ専用領域の保管（SHA-256 で重複排除）
- **Fabric API の保護**：Fabric の導入手順でのみ追加され、手動の重複導入はできません

## 📦 ダウンロードとビルド

- ダウンロード：<https://github.com/zzy89216-gif/ZyNova/releases>
- ビルド：JDK 17 と Android SDK / NDK を用意し `./gradlew ZalithLauncher:assembleRelease`

## 📚 ドキュメント

> 📚 **ドキュメント一覧（72 記事、英語）**：[docs/en/README.md](docs/en/README.md)



| 内容 | ファイル |
|---|---|
| ユーザードキュメント | [docs/en/README.md](docs/en/README.md) |
| インストール / 使い方 / エクスポート | [docs/en/install.md](docs/en/install.md) · [docs/en/usage.md](docs/en/usage.md) |
| レンダラー / ログイン / お気に入り / AI | [docs/en/renderers.md](docs/en/renderers.md) · [docs/en/login.md](docs/en/login.md) · [docs/en/favorites.md](docs/en/favorites.md) · [docs/en/ai-agent.md](docs/en/ai-agent.md) |
| トラブルシューティング / FAQ / データ | [docs/en/troubleshooting.md](docs/en/troubleshooting.md) · [docs/en/faq.md](docs/en/faq.md) · [docs/en/data-and-privacy.md](docs/en/data-and-privacy.md) |
| ビルド | [docs/en/build.md](docs/en/build.md) |
| 更新履歴 | [CHANGELOG.md](CHANGELOG.md) |
| 引き継ぎ / 構成 / 開発手順 | [HANDOFF.md](HANDOFF.md) |
| 第三者ライセンス | [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) |
| ライセンス | [LICENSE](LICENSE) |



## 💖 サポート

- 不具合報告：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- 支援：<https://afdian.com/a/hizzy>

## ⚖️ ライセンス

GPL-3.0（オープンソースを維持）。第三者ライセンスは [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。

---

**ZyNova — Context First. Less Steps.**
