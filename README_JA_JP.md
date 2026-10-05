<p align="center">
  <img src="assets/icon.png" width="180" alt="ZyNova">
</p>

# ZyNova

> **Minecraft: Java Edition · Android Launcher**
>
> An independent, unofficial project based on the open-source code of ZalithLauncher2

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Primary-Kotlin-blue)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-GPL--3.0-orange)](https://www.gnu.org/licenses/gpl-3.0.html)
[![Release](https://img.shields.io/badge/Release-27.2.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.2.0)

[English](README.md) | [简体中文](README_ZH_CN.md) | [繁體中文](README_ZH_TW.md) | **[日本語](README_JA_JP.md)**

---

## ✦ 概要

**ZyNova** は、[ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2) のオープンソースコードを
ベースに構築された、Android 向けの**非公式** Minecraft: Java Edition ランチャーです。

> **ZyNova は ZalithLauncher2 の公式版ではありません。**
>
> ZyNova は、ZalithLauncher2 のオープンソースコードをベースにした非公式の改変プロジェクトです。
>
> **内蔵の AI Agent** を搭載し、ログの確認・MOD 管理・設定変更・リソース導入・ゲーム起動まで
> **実際に実行**できます（助言だけではありません）。

- GitHub: <https://github.com/zzy89216-gif/ZyNova>
- Issues: <https://github.com/zzy89216-gif/ZyNova/issues>
- Discord: <https://discord.gg/QwPpZQHrTa>（永続招待リンク）

---

## ✨ 27.2.0 のハイライト

- **ページトランジションが実際に機能するようになりました。** トランジション設定には
  *ゼリーバウンス / バウンス / スライスイン*がありましたが、いずれも黙って単なる
  クロスフェードとして描画されていました。各オプションに本物のトランジションが入り、
  15 か所すべてのナビゲーション画面が 1 つの実装を共有します（これまで
  トランジションがなかったファイルマネージャーも含みます）。
- **モーションを減らす** — すべてを一括でオフにする新しいアクセシビリティスイッチです。
  ページトランジション、連続する入場アニメーション、動的ガラスとシマーのスケルトンの
  流れるハイライトが対象となり、Material のモーションスキームを expressive から
  standard へ引き下げます。単に時間を短くするのではなく、本当のオフスイッチです。
- **⚠️ 強烈な動的ガラスレベルを新設、光過敏症への警告付き。** ガラス効果は
  オフ / 動的ガラス / **強烈（光過敏症のリスク）** の 3 段階になりました。強烈レベルは
  より速く流れ、より明るく、ゆっくりとした明るさの明滅を加えます。以前の「極端」レベルと
  異なり、グラデーションのハイライトだけを描画し、テキストを載せたレイヤーをぼかすことは
  ないため、テキストは鮮明なままです。このレベルには実際のリスクがあるため、
  使用中はランチャーが**起動のたびに光過敏症の警告を表示**します。この警告は
  *設定 → ランチャー*でオフにできます。
- **カードがタッチに反応するようになりました** — 控えめな押下・解放のスケールで、
  共有カードコンポーネントを通じて一貫して適用されます（「モーションを減らす」では
  完全にスキップされます）。
- **グラフィックス API は完全に自動です。** 手動の OpenGL / Vulkan セレクターは
  なくなりました。インスタンスは初回起動時に OpenGL を使用し、それ以降ランチャーは
  バックエンドに手を加えないため、ゲーム自身の設定が優先されます。あるバージョンに
  このオプションが必要かどうかは、ハードコードされたバージョン番号ではなく
  **リリース日**で判断します。最初の Vulkan 対応バージョンは
  Minecraft 26.2-snapshot-1（2026-04-07）です。バージョンメタデータは起動のたびに
  （最新リリースとスナップショットを含めて）確認されますが、ゲームをブロックすることは
  決してありません。
- **スプラッシュ画面がメイン画面へフェードするようになりました** — 既定のアクティビティ
  アニメーションを使う代わりになります。

> 📜 過去のバージョン履歴は [CHANGELOG.md](CHANGELOG.md) をご覧ください

## ℹ️ 正規サインインに使うアプリ登録
**ZyNova は ZalithLauncher2 の非公式フォークであり、両者は独立して保守されている別プロジェクトです。**

| | 表示名 | Client ID | Mojang 許可リスト | 本ビルドでの使用 |
|---|---|---|---|---|
| **ZyNova 自身** | ZyNova Launcher | `7b66e168-f8cd-43fc-a52d-2e78dba189b0` | ✅ **承認済み（2026-09-30）** | ✅ **使用中** |
| **ZalithLauncher2 の** | ZalithLauncher（上流） | （削除済み） | ✅ 承認済み | ❌ 未使用（26.4.0 / 26.4.1 で使用） |

- **Minecraft のアプリケーション許可リストに載っている登録だけ**が正規サインインを完了できます。
  そうでない場合、`POST /authentication/login_with_xbox` は `403 Invalid app registration` を返します
- ✅ **ZyNova 自身の登録はこの許可リストに掲載されています**（Mojang Enforcement が
  **2026-09-30** に AppID レビューを完了）。現在のビルドで正規サインインが完了し、
  上流プロジェクトの登録を借りる必要はありません
- 必要なものは依然として**「パブリッククライアント + Client ID」のみ**：
  Client Secret・Redirect URI・SHA-1 は不要です。Client ID はリポジトリ Secret
  `OAUTH_CLIENT_ID` により**ビルド時に注入**され、ソースにはハードコードされません

## 🛠️ 機能

### AI Agent

上部バーの「ファイル」の隣に **AI** ボタンを追加しました。タップすると**直接チャット画面**に
入ります（独立した AI ホームはありません）。チャットと Agent は**同じ入口**です。
「このインスタンスが起動しないのはなぜ？」と聞けばログを読みに行き、
「直して」と言えば**実際にツールを呼んで処理します**。操作手順を書いて渡すだけではありません。

- **25 個のツール**はすべてランチャーの既存機能の上に構築されています：インスタンス、
  MOD の実メタデータ、MOD の有効化／無効化／削除、リソースパック、シェーダー、ワールド、
  ファイル、ログ、クラッシュレポート、**117 項目のランチャー設定**、リソースの検索と
  インストール（必須前提の再帰インストールを含む）、そして**実際のゲーム起動**
- **会話は端末内に自動保存**され、左上の**サイドバー**から確認・切り替え・削除・新規作成ができます
- **書き込み操作の監査ログ**：すべての書き込み操作をローカルに記録し、AI 設定から確認・
  エクスポートできます
- **ツール呼び出しの回数制限なし** —— Agent は完了と判断するまで動き続け、いつでも停止できます
- **設定はランチャー設定とは別**（チャット画面の右上）：プロバイダー（OpenAI / Anthropic、
  拡張可能）、自分の API キー（端末内のみ）、**モデル一覧は動的に取得しモデル名を
  ハードコードしません**、Base URL のカスタム、権限モード（既定は**操作確認**／完全制御）
- **安全性**：ファイル系ツールはゲームとランチャーのデータディレクトリに限定。
  機密キーは読み書き拒否。確認 UI が使えない場合は書き込み操作を拒否します

### レンダラー

26.3.0 以降、ランチャーには**ちょうど 3 つの内蔵レンダラー**が同梱されています：

| レンダラー | 内容 | 構成 |
|---|---|---|
| **Ironized Zink**（既定） | デスクトップ OpenGL 4.6 を Vulkan に変換（Mesa Zink + Kopper）、GoyDevv 氏作 | **4 つの公式プリセット** + OpenGL バージョン |
| **GL4ES** | クラシックな OpenGL 変換レイヤー | 既定値のまま |
| **MobileGlues** | デスクトップ OpenGL を端末の OpenGL ES 3.x 上に変換 | アップストリームの既定値のまま |

**Ironized Zink の 4 つの公式プリセット**（値はアップストリームと同一）：

| プリセット | 最適な用途 | Minecraft | シェーダー |
|---|---|---|---|
| **Potato** | 絶対的な最大 FPS | 1.8 → 最新（26.x を含む） | 非推奨 |
| **Performance** | Sodium を使った高 FPS | 1.20.x → 最新 | 軽量 |
| **Default** | Zink + シェーダーのバランス（**既定**） | 1.16.x → 最新 | フル（Iris / OptiFine） |
| **Max Compatibility** | すべてを動作させる | すべてのバージョン | フル + 重量級パック |

**26.4.0 以降、パネルに表示されるのは上記 4 つのプリセット**と、
その下の **OpenGL バージョン** ドロップダウンです。12 個の個別パラメータースイッチは削除され、
一般ユーザーが低レベルパラメーターを手動調整する必要はなくなりました。
プリセットを選ぶと、これまでどおりパラメーターセット全体が一括で書き込まれます。

> ℹ️ 4 つのプリセットはいずれも OpenGL バージョンを **4.6** に固定しています。
> ドライバーが Vulkan→Zink 4.6 を正しく変換できない場合（描画の乱れ／起動しない）は、
> プリセットの下にある「**OpenGL バージョン**」で 4.5 / 4.3 / 3.3 に下げられます
> —— このドロップダウンは 26.4.0 で削除され、**27.1.2 で復活**しました。
> この項目だけを変更しても他のプリセットパラメータには影響しません。

**Settings → Renderer** で Ironized Zink を選択すると、そのすぐ下にプリセットパネルが即座に展開されます。

> **外部レンダラーは影響を受けません**：レンダラープラグイン（FCL / Zalith のレンダラープラグイン
> および新しい `fclPlugin_V2` プラグイン）は、引き続き一覧にレンダラーを追加できます。
> *内蔵*されているのは 3 つだけです —— プラグインはいくつでもインストールできます。

### カード式ホーム画面

ホーム画面は**バージョンごとのモジュール**で構成されています。インストール済みの各バージョンがモジュール
カードとなり、そのバージョン自身のローカルワールドと保存済みサーバーを直接一覧表示します。
モジュールカード自体をタップすると、そのバージョンが起動します。

カード式ホーム画面は次のこともサポートしています：

- **長押しドラッグによる並べ替え**：バージョンカード、カード内のワールド / サーバー、
  右側メニューの 3 つのブロックを任意の順序にドラッグでき、その順序は記憶されます
- **カードサイズ**：Settings → Launcher → Home Page → Card Size（70%〜140%、既定は 100%）
- 単一レイヤーのカード背景のため、余分な枠や影は描画されません

データはオンデマンドで読み込まれます —— ランチャーは起動時にすべてをスキャンしません。
既定 / カード式 / カスタムのホーム画面は設定で選択できます。

### 統合リソース管理コア

mod、リソースパック、シェーダー、ワールドはすべて単一のパイプラインを共有します：

> 検索 → 詳細 → バージョン照合 → ファイル選択 → ダウンロード → 検証 → インストール

### リソースプロバイダー

リソースの参照元は UI から分離されています。上位レイヤーは統合されたインターフェースのみに依存します。
後から別の参照元を追加する場合も、プロバイダーを実装するだけでよく、書き直しは不要です。

### 統合ダウンロードマネージャー

すべてに対する単一のダウンロード入口：ダウンロードキュー、同時実行制御、進捗、
再開、再試行、キャンセル、チェックサム検証、一時ファイルのクリーンアップ、
ダウンロード後のインストール起動。

### 最小限のリソースインストール

*Version Settings → Mods* からリソースページに入ると、ランチャーはすでに
現在のインスタンス、Minecraft バージョン、ローダー、リソースディレクトリを把握しています。ダウンロードをタップすると
直接インストールされ、Minecraft バージョン、インスタンス、インストール先を繰り返し尋ねることはありません。
その後、リソース一覧に**インストール済み**の状態が表示されます。

### Vulkan 検出

検出結果は**利用可能 / 利用不可 / チェック失敗**に明確に分けられ、具体的な理由、GPU とドライバーの情報、
手動で再チェックするボタンが示されます。検出は、端末が対応を主張する内容ではなく、
端末から実際に列挙された機能に基づいて行われます。

---

## 📦 ビルド手順（開発者向け）

> 以下のセクションは、プロジェクトに貢献したい、またはローカルでビルドしたい開発者向けです。

### 要件

* **AGP 9.3.0** をサポートする Android Studio（最近の安定版）—— 古いバージョンではこのプロジェクトを開けません
* Android SDK:
  * **最小 API レベル**：26
  * **ターゲット API レベル**：34
  * **Compile SDK**：37
* JDK 17
* Gradle **9.5.0**（wrapper が自動的に処理します）

### ビルド手順

```bash
git clone https://github.com/zzy89216-gif/ZyNova.git
# Open the project in Android Studio and build
```

---

## 📜 ライセンス

本プロジェクトは **[GPL-3.0 license](LICENSE)** の下でライセンスされています。

> **ライセンスはコンポーネントごとに異なります。** ある依存関係が MIT だからといって、
> プロジェクト全体が MIT だと想定しないでください。ZyNova 自体は GPL-3.0、Ironized Zink は GPL-3.0、
> MobileGlues は LGPL-2.1、GL4ES は MIT です。Ironized Zink が同梱するレンダリングエンジンは **Mesa** です ——
> そのコアと Gallium のコード（**Zink** ドライバーを含む）は MIT、GLX クライアントコードは
> SGI Free Software License B、GL / GLX 拡張ヘッダーは Khronos ライセンスの下にあります。
> **Kopper** も Mesa コードベースの一部であるため、Mesa の条件が適用されます。
> コンポーネントごとの完全な内訳、著作権表示、再配布されるバイナリのソース入手可能性に関する声明については、
> **[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)** を参照してください。

### 追加条項（GPLv3 ライセンス第 7 条に基づく）

1. 本プログラムの改変版を配布する場合、原本と区別するためにプログラム名またはバージョン番号を合理的に変更する必要があります。（[GPLv3, 7(c)](https://github.com/ZalithLauncher/ZalithLauncher2/blob/969827b/LICENSE#L372-L374) による）
    - 改変版は、**その名称に元のプログラム名「ZalithLauncher」またはその略称「ZL」を含めてはならず、公式名称と混同を引き起こすほど類似した名称を使用してはなりません**。
    - すべての改変版は、**プログラムの起動画面またはメイン画面で「非公式改変版」であることを明確に示さなければなりません**。
    - プログラムのアプリケーション名は [gradle.properties](./ZalithLauncher/gradle.properties) で変更できます。

2. プログラムが表示する著作権表示を削除してはなりません。（[GPLv3, 7(b)](https://github.com/ZalithLauncher/ZalithLauncher2/blob/969827b/LICENSE#L368-L370) による）

## オープンソースライブラリとライセンス

本ソフトウェアは以下のオープンソースライブラリを使用しています：

| Library                               | Copyright                                                                                                     | License              | Official Link                                                                      |
|---------------------------------------|---------------------------------------------------------------------------------------------------------------|----------------------|------------------------------------------------------------------------------------|
| androidx-appcompat                    | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Link↗](https://developer.android.com/jetpack/androidx/releases/appcompat)         |
| androidx-constraintlayout-compose     | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Link↗](https://developer.android.com/develop/ui/compose/layouts/constraintlayout) |
| androidx-webkit                       | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Link↗](https://developer.android.com/jetpack/androidx/releases/webkit)            |
| ANGLE                                 | Copyright 2018 The ANGLE Project Authors                                                                      | BSD 3-Clause License | [Link↗](http://angleproject.org/)                                                  |
| Apache Commons Codec                  | -                                                                                                             | Apache 2.0           | [Link↗](https://commons.apache.org/proper/commons-codec)                           |
| Apache Commons Compress               | -                                                                                                             | Apache 2.0           | [Link↗](https://commons.apache.org/proper/commons-compress)                        |
| Apache Commons IO                     | -                                                                                                             | Apache 2.0           | [Link↗](https://commons.apache.org/proper/commons-io)                              |
| ByteHook                              | Copyright © 2020-2024 ByteDance, Inc.                                                                         | MIT License          | [Link↗](https://github.com/bytedance/bhook)                                        |
| BuildKeys                             | Copyright © 2026 MovTery                                                                                      | Apache 2.0           | [Link↗](https://github.com/MovTery/BuildKeys)                                      |
| Coil Compose                          | Copyright © 2025 Coil Contributors                                                                            | Apache 2.0           | [Link↗](https://github.com/coil-kt/coil)                                           |
| Coil Gifs                             | Copyright © 2025 Coil Contributors                                                                            | Apache 2.0           | [Link↗](https://github.com/coil-kt/coil)                                           |
| Coil SVG                              | Copyright © 2025 Coil Contributors                                                                            | Apache 2.0           | [Link↗](https://github.com/coil-kt/coil)                                           |
| Fishnet                               | Copyright © 2025 Kyant                                                                                        | Apache 2.0           | [Link↗](https://github.com/Kyant0/Fishnet)                                         |
| gl4es_extra_extra                     | Copyright © 2016-2018 Sebastien Chevalier; Copyright (c) 2013-2016 Ryan Hileman                               | MIT License          | [Link↗](https://github.com/PojavLauncherTeam/gl4es_extra_extra)                    |
| Gson                                  | Copyright © 2008 Google Inc.                                                                                  | Apache 2.0           | [Link↗](https://github.com/google/gson)                                            |
| kotlinx.coroutines                    | Copyright © 2000-2020 JetBrains s.r.o.                                                                        | Apache 2.0           | [Link↗](https://github.com/Kotlin/kotlinx.coroutines)                              |
| ktor-client-content-negotiation       | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Link↗](https://ktor.io)                                                           |
| ktor-client-core                      | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Link↗](https://ktor.io)                                                           |
| ktor-client-okhttp                    | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Link↗](https://ktor.io)                                                           |
| ktor-http                             | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Link↗](https://ktor.io)                                                           |
| ktor-serialization-kotlinx-json       | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Link↗](https://ktor.io)                                                           |
| LWJGL - Lightweight Java Game Library | Copyright © 2012-present Lightweight Java Game Library All rights reserved.                                   | BSD 3-Clause License | [Link↗](https://github.com/LWJGL/lwjgl3)                                           |
| material-color-utilities              | Copyright 2021 Google LLC                                                                                     | Apache 2.0           | [Link↗](https://github.com/material-foundation/material-color-utilities)           |
| Maven Artifact                        | Copyright © The Apache Software Foundation                                                                    | Apache 2.0           | [Link↗](https://github.com/apache/maven/tree/maven-3.9.9/maven-artifact)           |
| Media3                                | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Link↗](https://developer.android.com/jetpack/androidx/releases/media3)            |
| Ironized Zink                         | Copyright © GoyDevv (rendering engine: Mesa Zink / Kopper, © The Mesa Authors)                                 | GPL-3.0              | [Link↗](https://github.com/GoyDevv/IronizedZink)                                   |
| Mesa core / Gallium (incl. Zink)      | Copyright © 1999-2007 Brian Paul and the Mesa contributors                                                    | MIT                  | [Link↗](https://mesa3d.org/)                                                       |
| Mesa GLX client code                  | Copyright © 1999-2007 Brian Paul and the Mesa contributors                                                    | SGI Free Software License B | [Link↗](https://mesa3d.org/)                                                 |
| Mesa GL / GLX extension headers       | Copyright © The Khronos Group                                                                                 | Khronos              | [Link↗](https://www.khronos.org/)                                                  |
| Kopper                                | Part of the Mesa code base (Vulkan WSI layer)                                                                 | Covered by the Mesa terms above | [Link↗](https://mesa3d.org/)                                            |
| MMKV                                  | Copyright © 2018 THL A29 Limited, a Tencent company.                                                          | BSD 3-Clause License | [Link↗](https://github.com/Tencent/MMKV)                                           |
| Navigation 3                          | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Link↗](https://developer.android.com/jetpack/androidx/releases/navigation3)       |
| MobileGlues                           | Copyright (c) 2025-2026 MobileGL-Dev                                                                          | LGPL-2.1             | [Link↗](https://github.com/MobileGL-Dev/MobileGlues)                               |
| OkHttp                                | Copyright © 2019 Square, Inc.                                                                                 | Apache 2.0           | [Link↗](https://github.com/square/okhttp)                                          |
| Okio                                  | Copyright © 2013 Square, Inc.                                                                                 | Apache 2.0           | [Link↗](https://github.com/square/okio)                                            |
| OpenNBT                               | Copyright © 2013-2021 Steveice10.                                                                             | MIT License          | [Link↗](https://github.com/GeyserMC/OpenNBT)                                       |
| Process Phoenix                       | Copyright © 2015 Jake Wharton                                                                                 | Apache 2.0           | [Link↗](https://github.com/JakeWharton/ProcessPhoenix)                             |
| proxy-client-android                  | -                                                                                                             | LGPL-3.0 License     | [Link↗](https://github.com/TouchController/TouchController)                        |
| Reorderable                           | Copyright © 2023 Calvin Liang                                                                                 | Apache 2.0           | [Link↗](https://github.com/Calvin-LL/Reorderable)                                  |
| sdl2-compat                           | Copyright (C) 2026 Sam Lantinga <slouken@libsdl.org>                                                          | Zlib License         | [Link↗](https://github.com/libsdl-org/sdl2-compat)                                 |
| SDL3                                  | Copyright (C) 1997-2026 Sam Lantinga <slouken@libsdl.org>                                                     | Zlib License         | [Link↗](https://github.com/libsdl-org/SDL)                                         |
| skinview3d                            | Copyright © 2014-2018 Kent Rasmussen; Copyright © 2017-2022 Haowei Wen, Sean Boult and contributors           | MIT License          | [Link↗](https://github.com/bs-community/skinview3d)                                |
| sora-editor                           | Copyright (C) 2020-2026  Rosemoe                                                                              | LGPL-2.1 License     | [Link↗](https://github.com/Rosemoe/sora-editor)                                    |
| StringFog                             | Copyright © 2016-2023, Megatron King                                                                          | Apache 2.0           | [Link↗](https://github.com/MegatronKing/StringFog)                                 |
| tm4e (TextMate for Eclipse)           | Copyright © Eclipse Foundation                                                                                | EPL-2.0 License      | [Link↗](https://github.com/eclipse-tm4e/tm4e)                                      |
| XZ for Java                           | Copyright © The XZ for Java authors and contributors                                                          | 0BSD License         | [Link↗](https://tukaani.org/xz/java.html)                                          |
