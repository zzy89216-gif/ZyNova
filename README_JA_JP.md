<p align="center">
  <img src="assets/icon.png" width="180" alt="ZyNova">
</p>

# ZyNova

> **Minecraft: Java Edition · Android Launcher**
>
> ZalithLauncher2 のオープンソースコードを基にした独立非公式プロジェクト

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Primary-Kotlin-blue)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-GPL--3.0-orange)](https://www.gnu.org/licenses/gpl-3.0.html)
[![Release](https://img.shields.io/badge/Release-27.6.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.6.1)

[简体中文](README.md) | [English](README_EN.md) | [繁體中文](README_ZH_TW.md) | **[日本語](README_JA_JP.md)** | [한국어](README_KO.md) | [Русский](README_RU.md)

---

## ✦ 概要

**ZyNova** は Android 向けの **Minecraft: Java Edition** ランチャーで、
[ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2) のオープンソースコードを
基にしています。ZalithLauncher2 の公式版**ではなく**、独立して保守・配布される非公式フォークです。

- GitHub: <https://github.com/zzy89216-gif/ZyNova>
- Issues: <https://github.com/zzy89216-gif/ZyNova/issues>
- Discord: <https://discord.gg/QwPpZQHrTa>

## ✨ 27.6.1 のハイライト

- **上部 5 ボタンを復元**：フォルダー / AI / マルチプレイ / ダウンロード / 設定が 27.6.0 以前の
  見た目に戻り、ホームボタンの素材を使わなくなりました
- **ゴーストボタン**：起動 / ワールド作成 / サーバー追加 / 項目は背景との明暗差を約 1.5~2% だけにし、
  枠線なし・透明度を維持。画像・動画背景がリアルタイムに透けます。作成 / 追加ボタンは小さくして
  起動ボタンの隣に配置
- **バージョン別ワールド作成**：26.1+ は新しい分割保存形式を直接書き、1.12.2~26.0 は DataFixer、
  1.12.2 未満は入口を非表示。書き込み後に読み戻して検証
- **チートを許可**：ワールド作成にバージョン対応のスイッチを追加
- **Fabric API の読み込み状態**：読み込み中は「読み込み中」を表示し、成功後にのみインストールを許可
- **27.6.0 の内容を維持**：Fabric API 保護、アイコン修正、ワールド / サーバーのワンクリック作成

> 完全な履歴は [CHANGELOG.md](CHANGELOG.md) を参照してください。

## 🛠️ 主な機能

- **AI Agent**：既定は DeepSeek。ログ確認、MOD 管理、設定編集、リソース導入、ゲーム起動を実行
- **レンダラー**：自動 / MobileGlues / Ironized Zink / GL4ES
- **カード型ホーム**：インスタンスごとにワールドとサーバーを表示し、作成 / 追加から quickPlay で起動
- **統合リソース管理**：Modrinth / CurseForge 検索、依存解決、ダウンロード、SHA1 検証、インストール
- **Fabric API 保護**：Fabric 導入フローが自動で追加。手動インストール・削除・無効化・重複導入は不可

## 📦 ダウンロードとビルド

- **ダウンロード**：<https://github.com/zzy89216-gif/ZyNova/releases>
  - ユニバーサル APK（arm64-v8a / armeabi-v7a / x86 / x86_64）
  - `mapping.universal.zip`（難読化解除用マッピング）
- **ローカルビルド**：JDK 17、Android SDK / NDK が必要です：
  ```bash
  ./gradlew ZalithLauncher:assembleRelease
  ```
- 公式ビルドは GitHub Actions で実行され、コード難読化とリソース圧縮が有効になります。

## ⚠️ 注意

- ホームの「ワールド作成」は **26.1+** で新形式、**1.12.2~26.0** で互換パス、
  **1.12.2 未満**では非表示です。
- Fabric API は導入フローが自動処理します。手動インストールは拒否され、MOD 管理からも
  削除・無効化できません。
- quickPlay 非対応の旧バージョンでは、通常どおりメインメニューから起動します。
- 既存の設定・ワールド・サーバー一覧はアップグレード後も保持されます。

## 📚 ドキュメント

| 内容 | ファイル |
|---|---|
| 更新履歴 | [CHANGELOG.md](CHANGELOG.md) |
| 引き継ぎ / アーキテクチャ / 開発フロー | [HANDOFF.md](HANDOFF.md) |
| サードパーティライセンス | [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) |
| ライセンス | [LICENSE](LICENSE) |

## ⚖️ ライセンス

ZyNova は **GPL-3.0** で公開され、オープンソースを維持する必要があります。
上流 ZalithLauncher2 の著作権・ライセンス表示は保持されています。全文と追加条項は
[LICENSE](LICENSE)、サードパーティコンポーネントは [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)
を参照してください。

## 💖 サポート

- 問題報告：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- 支援：[assets/donate](assets/donate) とアプリ内の About ページ

## 👥 メンテナー

ZyNova は **zzy** が独立して開発・保守しています。

---

**ZyNova — Context First. Less Steps.**
