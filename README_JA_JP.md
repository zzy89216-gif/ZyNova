<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.0.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.0.1)

[简体中文](README.md) | [English](README_EN.md) | [繁體中文](README_ZH_TW.md) | **日本語** | [한국어](README_KO.md) | [Русский](README_RU.md)

---

Android 向けの Minecraft: Java Edition ランチャー。ZalithLauncher2 のソースを基に独自に開発・配布しています。

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ 28.0.1 の新機能

- **SakuraFrp が実際につながるようになりました**：`frpc` は公式形式 `frpc -f <アクセスキー>:<トンネルID>` で起動します（以前の環境変数方式は Sakura 版 frpc に無視され、すぐ終了していました）
- **更新してもエラーにならない**：BOM / 配列 / `{data}` ラップの応答に対応。接続アドレスは frpc ログから抽出し、**最上部に表示 + ワンタップコピー**
- **戻るキーで画面全体が終了しない**：上部の戻るはシステムの戻るキーを経由しなくなり（コールバックが無く画面が終了していました）、1 階層だけ戻り、スタックを空にしません
- **ゲームを止めずにランチャーへ戻れます**：ゲーム内メニューの最初の項目「ランチャーに戻る」は**ゲームを停止しません**。ランチャー下部に「ゲームに戻る」ピルが出ます
- **ポートや種類を理解する必要はありません**：種類は TCP 固定、ローカルポートは `/proc/net/tcp` から自動検出、ノードはアカウント階層で絞り込み
- **AI がマルチプレイを全自動で開始**：`frp_status` / `frp_create_tunnel` / `frp_start_tunnel` / `frp_stop_tunnel` / `multiplayer_status`

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
