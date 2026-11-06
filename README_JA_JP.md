<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-27.10.2-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.10.2)

[简体中文](README.md) | [English](README_EN.md) | [繁體中文](README_ZH_TW.md) | **日本語** | [한국어](README_KO.md) | [Русский](README_RU.md)

---

Android 向けの Minecraft: Java Edition ランチャー。ZalithLauncher2 のソースを基に独自に開発・配布しています。

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ 27.10.2 の更新内容

- **パーソナライズ画面**：壁紙・背景の不透明度・ぼかし・角丸・ガラス強度を 1 ページで調整（上部カプセルのパレットから）
- **ガラス基盤の作り直し**：アプリ全体で背景ぼかしは 1 層のみ、常時アニメーションを全廃。カードは単色グラデーション + 1px の縁
- **AI チャット**：ツール呼び出しは `コマンド実行 · 要約` の 1 行表示、生の出力は既定で折りたたみ。ツール実行前にモデルが一言要約を書く仕様に
- **Quilt 保護**：Quilted / Legacy Fabric API も Fabric API と同じく「検索不可・インストール不可・削除不可・無効化不可」
- **修正**：戻るボタンが効かない／画面のちらつき、SakuraFrp のアクセスキー入力直後のエラー、ダイアログの二重ぼかし
- 本ビルドは**テスト版（pre-release）**です

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
