<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <b>日本語</b> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **ZyNova 28.1.0 リリース（グローバルコンプライアンス版）。**
>
> 📌 **Current release: 28.2.1** · [Changelog](../../CHANGELOG.md) · [Releases](https://github.com/zzy89216-gif/ZyNova/releases/latest)


[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.2.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.2.1)

---

Android 向けの **Minecraft: Java Edition** ランチャー。ZalithLauncher2 のソースから独立して開発・配布しています（公式ビルドではありません）。

方針は「**タップを減らす**」。設定を並べて選ばせるのではなく、まずこちらで判断し、判断できないときだけ一度聞く。ゲームの導入、MOD の依存解決、クラッシュログの読解までを AI に任せられます。

## ✨ 28.1.0 の新機能

- **未成年の時間制限**：18 歳未満は金・土・日と法定祝日の **20:00–21:00** のみ起動可能。2026 年の祝日は中国国務院の通知（国办发明电〔2025〕7号）に沿って日付ごとに列挙され、振替出勤日は平日扱いです。
- **初回起動のコンプライアンス手続き**：3 つの統合文書（①EULA + プライバシーポリシー、②第三者オープンソース告知 + 免責・リスク告知、③チュートリアル + 適用地域と法的管轄）。各文書は**最後までスクロールして個別にチェック**しないと進めず、完了後に年齢区分を選びます（既存ユーザーも更新後に一度だけ表示）。
- **免責事項を明記**：セーブデータの消失、端末の発熱、第三者による BAN、サービスの終了などのリスクをはっきり告知します。
- **あわせて**：ホームのカードが読み込みで止まらず、AI の Web 検索が利用可能に、インポートはインスタンスフォルダへ、クラッシュ時の自動修復は削除。

## 🛠️ 主な機能

- **AI Agent**：既定は DeepSeek。ゲーム本体・MOD パック・MOD の導入、設定変更、ログ閲覧、起動まで実行
- **レンダラー**：自動 / MobileGlues / GL4ES / LTW（実験的）
- **リソース基盤**：Modrinth / CurseForge 検索、依存解決、ダウンロード、検証、導入
- **お気に入りと保管**：オンラインのお気に入り + アプリ専用領域の保管（SHA-256 で重複排除）
- **Fabric API の保護**：導入手順からのみ追加され、手動での重複導入・検索・削除はできません

## 📱 動作環境と用語メモ

- Android 8.0 以上（minSdk 26）。ビルドは JDK 17 + Android SDK / NDK
- **MOD**＝ゲームに追加する改造、**MOD パック**＝その詰め合わせ、**影 MOD／シェーダー**＝光影表現
- レンダラーは端末に合わせて自動選択。描画が乱れるときは手動で切り替えを

## 📦 ダウンロードとビルド

- ダウンロード：<https://github.com/zzy89216-gif/ZyNova/releases>（常に最新版を）
- ビルド：`./gradlew ZalithLauncher:assembleRelease`
- 注意：初回起動時にコンプライアンス手続きが表示されます。詳細は [docs/en/licenses.md](../../docs/en/licenses.md)

## 📚 ドキュメント

> 📚 ドキュメントは中国語と英語の 2 系統（各 77 記事）。このページは日本語の紹介です。

| 内容 | ファイル |
|---|---|
| ドキュメント一覧 | [docs/en/README.md](../../docs/en/README.md) |
| インストール / 使い方 / エクスポート | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| レンダラー / ログイン / お気に入り / AI | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| トラブルシューティング / FAQ / データとプライバシー | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| ビルド / 更新履歴 | [docs/en/build.md](../../docs/en/build.md) · [CHANGELOG.md](../../CHANGELOG.md) |
| 引き継ぎ / 第三者ライセンス / ライセンス | [HANDOFF.md](../../HANDOFF.md) · [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) · [LICENSE](../../LICENSE) |

## 💖 サポート

- 不具合報告：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- メール：<ZyNovaLauncher@outlook.com>
- 公式 X：<https://x.com/ZyNovaProject>
- 支援：<https://afdian.com/a/hizzy>

## ⚖️ ライセンス

GPL-3.0。プロジェクトはオープンソースのままです。第三者コンポーネントと各ライセンスは
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) と [docs/en/licenses.md](../../docs/en/licenses.md) に記載。

> ⚠️ **Terracotta（マルチプレイ）は AGPL-3.0** で他より厳しい条件です。本プロジェクトはバイナリを
> 未改変のまま配布しています。改変して再配布する場合は、AGPL-3.0 §13 に従って対応するソースコードを
> 公開する必要があります。

---

**ZyNova — Context First. Less Steps.**
