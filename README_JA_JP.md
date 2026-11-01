<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-27.8.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.8.1)

[简体中文](README.md) | [English](README_EN.md) | [繁體中文](README_ZH_TW.md) | **日本語** | [한국어](README_KO.md) | [Русский](README_RU.md)

---

Android 向けの Minecraft: Java Edition ランチャー。ZalithLauncher2 のソースを基に独自に開発・配布しています。

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ 27.8.0 の更新内容

- **AI はエージェント**：ゲーム本体・MOD パック・MOD の導入、インスタンス設定の変更まで話しかけるだけで実行（既定は「完全制御」）
- **ダウンロードは完全に静か**：ダイアログなし、進捗は上部の細いバーのみ。タップで「タイトル / 手順 / 進捗 / 速度 / キャンセル」
- **NeoForge を修正**：ミラーが maven.neoforged リポジトリ全体をカバーし、失敗や停止が解消
- **細部**：入力欄のヒントは入力すると消える。お気に入り・リソース・MOD パックは同じタスクキュー

## 🛠️ 主な機能

- **AI Agent**：既定は DeepSeek。ゲーム本体・MOD パック・MOD の導入、設定変更、ログ閲覧、起動まで実行
- **レンダラー**：自動 / MobileGlues / Ironized Zink / GL4ES
- **リソース基盤**：Modrinth / CurseForge 検索、依存解決、ダウンロード、検証、導入
- **お気に入りと保管**：オンラインのお気に入り + アプリ専用領域の保管（SHA-256 で重複排除）
- **Fabric API の保護**：Fabric の導入手順でのみ追加され、手動の重複導入はできません

## 📦 ダウンロードとビルド

- ダウンロード：<https://github.com/zzy89216-gif/ZyNova/releases>
- ビルド：JDK 17 と Android SDK / NDK を用意し `./gradlew ZalithLauncher:assembleRelease`

## 📚 ドキュメント

| 内容 | ファイル |
|---|---|
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
