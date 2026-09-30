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
[![Release](https://img.shields.io/badge/Release-26.4.2-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v26.4.2)

[English](README.md) | [简体中文](README_ZH_CN.md) | [繁體中文](README_ZH_TW.md) | **[日本語](README_JA_JP.md)**

---

## ✦ 概要

**ZyNova** は、[ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2) のオープンソースコードを
ベースに構築された、Android 向けの**非公式** Minecraft: Java Edition ランチャーです。

> **ZyNova は ZalithLauncher2 の公式版ではありません。**
>
> ZyNova は、ZalithLauncher2 のオープンソースコードをベースにした非公式の改変プロジェクトです。

- GitHub: <https://github.com/zzy89216-gif/ZyNova>
- Issues: <https://github.com/zzy89216-gif/ZyNova/issues>
- Discord: <https://discord.gg/QwPpZQHrTa>（永続招待リンク）

---

## ✨ 26.4.2 のハイライト

- **正規サインインが ZyNova 自身の Microsoft アプリケーション登録に切り替わりました**：
  26.4.0 / 26.4.1 は、Minecraft のアプリケーション許可リストに載っている登録でなければ
  Minecraft Services にアクセスできないため、**上流の ZalithLauncher2 プロジェクト**の
  アプリ登録を借用していました。26.4.2 からは **ZyNova 自身の登録**でビルドします
- **アプリアイコンを刷新しました**：アダプティブアイコン（背景 / 前景 / モノクロの 3 層）、
  従来のスクエアアイコンと丸アイコン（各 5 密度）、Google Play 用アイコン（512×512）を
  **すべて新しい ZyNova のアートワークに差し替え**ました。
  アートワークはアダプティブアイコンの表示領域内に収めてあるため、
  **キャラクターと `ZyNova` の文字は欠けることなく全体が見えます**。
  **アプリ内の解凍画面（初回起動 / 更新後に表示される画面）は変更していません**
- それ以外に**機能の変更はありません**：認証フロー・レンダラー・リソース管理などは
  26.4.1 と完全に同じです

**ℹ️ 正規サインインに使われる Microsoft アプリケーション（プロジェクトの帰属と混同しないでください）**

**ZyNova は ZalithLauncher2 の非公式フォークであり、両者は別プロジェクトで、独立して
保守されています。** 関係する 2 つのアプリ登録は次のとおりです：

| | 表示名 | Client ID | 本ビルドでの使用 |
|---|---|---|---|
| **ZyNova 自身のアプリ登録** | ZyNova Launcher | `7b66e168-f8cd-43fc-a52d-2e78dba189b0` | ✅ **使用中** |
| **ZalithLauncher2 のアプリ登録** | ZalithLauncher（上流） | `（已移除）` | ❌ 未使用（26.4.0 / 26.4.1 で使用） |

- **Minecraft のアプリケーション許可リストに載っているアプリ登録だけ**が正規サインインを
  完了できます。そうでない場合、`POST /authentication/login_with_xbox` は
  `403 Invalid app registration, see https://aka.ms/AppRegInfo` を返します
  （OAuth / Xbox Live / XSTS の成否とは無関係です）
- Client ID はリポジトリ Secret `OAUTH_CLIENT_ID` により**ビルド時に注入**され、
  ソースにハードコードされません。優先順位は
  **環境変数（CI Secret）> `.oauth_client_id.txt` > `ZalithLauncher/gradle.properties`**
- **表示される現象**：Microsoft アカウントの「接続済みのアプリ / アプリとデバイス」には
  上の表の**表示名**で表示されます
- 必要なものは依然として**「パブリッククライアント + Client ID」のみ**です。
  Client Secret・Redirect URI・SHA-1 は不要です

## ✨ 26.4.1 のハイライト

- **正規サインインが使えるようになりました（中心的な修正）**：26.4.0 のサインインは
  Microsoft OAuth・Xbox Live・XSTS の**3 ステップすべてが成功**したあとに
  Minecraft Services で停止していました。本当の原因は **Mojang 側のアプリケーション
  許可リスト**で、Minecraft Services は **HTTP 403 `Invalid app registration`** を返していました
  （コード・Entra 設定・IP・ネットワーク・リクエスト頻度・アカウントのプロフィールの
  いずれとも無関係です）。26.4.1 は **Mojang の承認済み Microsoft アプリケーション登録**を
  使ってビルドするため、正規サインインが完了します（26.4.2 で ZyNova 自身の登録に切り替え済み）
- **サインイン失敗時に本当のエラーが残るようになりました**：以前はログに
  `message` が常に `null` の例外（`il6: null`）しか出ておらず、実際の HTTP ステータスコードも
  サーバーの応答本文も記録されていませんでした。現在は `login_with_xbox` と
  `getPlayerProfile` の失敗時に**リクエスト URL・実際の HTTP ステータスコード・応答本文**・
  完全なスタックトレースを記録します（記録されるのは非 2xx のエラー JSON で、
  `access_token` は含まれません）
- **403 の誤った原因表示を修正**：Minecraft Services の 403 には
  `BLOCKED_IP`（IP がブロック）と `Invalid app registration`（Client ID が Mojang の
  許可リストにない）という**まったく異なる 2 つの意味**があります。以前はすべて前者として
  表示され、ネットワークや IP の問題だと誤解させていました。現在は応答本文で区別し、
  後者には専用のメッセージを表示します

## ✨ 26.4.0 のハイライト

- **Microsoft（正規）サインインが復活しました**：アカウントログインメニューに再び **Microsoft** が表示されます。
  OAuth 2.0 の**デバイスコードフロー**を使用し、ランチャーがデバイスコードを取得してクリップボードにコピーし、
  認証ページを開いたうえでトークンをポーリングします。
  認証バックエンドが削除されたことは一度もありません。26.1.0 で削除されたのは UI と ViewModel の接続部分だけで、
  本リリースでそれを復元しました。**Client Secret、リダイレクト URI、SHA-1 は不要**であり、
  公開 OAuth Client ID は設計どおり APK 内に同梱されています。
- **ダウンロードページの「カテゴリ」フィルターが空になる問題を修正**（Issue #6）：
  既定の「すべてのプラットフォーム」モードで、カテゴリ一覧がクリアされずに参照元（CurseForge）に従うようになり、
  カテゴリ条件はその参照元にのみ送信されるため、あるカテゴリ ID が属さない参照元に渡されることはありません。
  複数の参照元を問い合わせる場合、フィルターのタイトルにその旨が表示されます（例：「カテゴリ（CurseForge のみ）」）。
- **検索結果カードにリソース種別バッジが表示されない問題を修正**（Issue #6）：
  検索結果リストが `classes` 引数をカードレイアウトに渡していなかったため、
  mod / リソースパック / シェーダー / ワールド / modpack のバッジがまったく描画されませんでした。
  一方、この引数を正しく渡していた詳細ページではバッジが正しく表示されていました。
- **Ironized Zink は公式の 4 プリセットのみを公開するようになりました**（Issue #7）：
  OpenGL バージョンのドロップダウンと 12 個のパラメータースイッチが削除され、
  一般ユーザーが低レベルのパラメーターを調整する必要がなくなりました。プリセットを選択すると、
  これまでどおりパラメーターセット全体が書き込まれます。
  ⚠️ 4 つのプリセットはすべて OpenGL バージョンを 4.6 に固定しているため、新規ユーザーは
  4.5 / 4.3 / 3.3 に下げることができなくなりました。26.3.0 でパラメーターを調整したユーザーが既に保存した値は引き続き機能します。

## ✨ 26.3.0 のハイライト

- **レンダラーの大規模な刷新 —— 内蔵レンダラーは 3 種類だけになりました**：
  **Ironized Zink**（現在は既定）、**GL4ES**、**MobileGlues** です。
  削除されたもの：Krypton Wrapper (NG-GL4ES)、Kopper Zink、VirGL、Freedreno、Panfrost、
  およびそれらだけが使用していたネイティブライブラリ。
- **Ironized Zink を完全統合**：**13 個の調整可能なパラメーター**と
  **4 つの公式プリセット**（Potato / Performance / Default / Max Compatibility）を
  アップストリーム（GoyDevv 氏作、GPL-3.0）から移植しました。**Settings → Renderer** で選択すると、
  そのすぐ下に構成パネルが即座に展開されます。
  *（26.4.0 でこのパネルは 4 つのプリセットのみに縮小されました —— 上記のハイライトを参照してください。）*
- **既定のレンダラーが Ironized Zink（Default プリセット）になりました。**
- **ホームページをスクロール中にカードがずれる問題を修正** —— ページのスクロールが並べ替えの
  「場所を空ける」移動と誤認され、すべてのカードが本来の位置からアニメーションで外れていました。
- **単一のユニバーサル APK のみ**（4 つの ABI すべてを 1 つのビルドに含め、コード難読化も維持）。
- **コンポーネントごとのライセンス表記** —— [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) を参照してください。
- **外部レンダラープラグインは影響を受けません**：FCL / Zalith のレンダラープラグインおよび
  `fclPlugin_V2` プラグインは、引き続き一覧にレンダラーを追加できます。

## ✨ 26.2.6 のハイライト

- ドラッグで並べ替えた後にカードが誤った位置に押し出される問題を**修正**
  （オフセット修飾子が `clip` / `background` の内部で適用されており、中断された
  「場所を空ける」アニメーションが永続的なオフセットを残す可能性がありました）
- **変更**：アクションメニューを**長押しして画面の左右どちら側へもドラッグ**できるようになりました
  （アップストリームの ZalithLauncher2 から移植：メニューが持ち上がり、指に追従し、ドックする側をプレビューして
  そこに収まり、コンテンツ領域が場所を空けます。選択した側は記憶されます）
- 右側メニューのレイアウトがアップストリームの配置に戻っていた問題を**修正**

## ✨ 26.2.5 のハイライト

- カード式ホームページでカードの周囲に枠が見える問題を**修正**：カードが複数のレイヤー
  （角丸の塗り + 拡大されたグラフィックスレイヤー + 影）を重ねていたため、余白のリングが
  コンテンツ領域より暗く見えていました —— カードは単一の背景レイヤーになり、影はありません
- **新機能**：カード式ホームページでの長押しによる**ドラッグ並べ替え** ——
  バージョンカード、カード内のワールド / サーバー、右側メニューの 3 つのブロック
  （アカウントのアバター / バージョン行 / 起動ボタン）を任意の順序にドラッグでき、
  その順序は記憶されます

## ✨ 26.2.4 のハイライト

本リリースでは 2 件の新しい報告に対応しました：

- カード式ホームページが*背景要素の不透明度*設定を無視する問題を**修正**：
  カードの色が `cardColor(false)` でハードコードされており、カスタム背景の影響を受けない設定になっていたため、
  他のすべてのページが設定に従う一方でカードは完全に不透明のままでした
- **新機能**：カード式ホームページ向けの調整可能な**カードサイズ**
  （Settings → Launcher → Home Page → Card Size、70%〜140%、既定は 100% で元の見た目を維持）

## ✨ 26.2.3 のハイライト

本リリースでは、**mod 以外のすべて**（modpack、リソースパック、ワールド、シェーダー）に
ついてリソースのダウンロードフローを修正しました：

- リソースパック / シェーダー / ワールドの検索結果がほとんど返らない問題を**修正**：
  mod ローダーのフィルターが、ローダーで分類されないリソース種別に適用されていました
- 集約検索の総ページ数が 0 に潰れる問題を**修正**（UI が「1 / 0」と表示され、ページ送りができませんでした）
- *すべて*のプラットフォームオプションが、選択したリソース種別をサポートしない参照元に問い合わせる問題を**修正**
  （ワールドは Modrinth に存在しません）
- API キーが構成されていないときに CurseForge がまったく到達不能になる問題を**修正**
  （この場合、MCIM ミラーが参照元として維持されるようになりました）
- ワールドのカテゴリフィルターが機能しない問題、およびワールドの展開に失敗したときに残る `.zip` ファイルを**修正**

## ✨ 26.2.2 のハイライト

本リリースは、ユーザーから報告された問題の修正に焦点を当てています：

- *⚠️ Extreme* のガラスレベルを**削除**し、ガラス効果を
  **オフ / 動的ガラスを有効化**に簡素化しました —— Extreme レベルは背景とともにテキストレイヤーまでぼかし、
  UI のフォントがぼやけて見えていました
- ワンタップインストールが `No compatible version found for this instance` で
  ときどき失敗する問題を**修正**：検索プラットフォームを切り替えた後、
  mod ローダーのフィルターが黙って破棄され、他のローダー向けのアセットが結果に含まれていました
- インストール失敗時の表示を**改善**：メッセージがローカライズされ、
  対象インスタンスの Minecraft バージョンと mod ローダーが含まれるようになりました
- 必須の依存関係が黙って破棄されなくなったことを**修正** —— 解決できなかった前提条件は
  ログに記録され、報告されます
- Discord の招待リンクを**修正**：古い一時招待が期限切れになったため、永続的なものに置き換えました

## ✨ 26.2.1 のハイライト

- **作り直し**：*⚠️ Extreme* のガラスレベルが、ほとんど見えないグラデーションではなく
  実際の GPU シェーダー（マルチサンプルブラー + 波ベースの屈折 / 歪み）を使用するようになりました
- **修正**：検索結果カードにワンタップインストールボタンが用意されました
- **修正**：現在のインスタンスから mod ローダーが自動的に選択されるようになりました
- **ホーム画面**：**バージョンごとのモジュール**に再編成され、各モジュールが自身のワールドとサーバーを一覧表示します
- **新機能**：CurseForge と Modrinth の結果を 1 つのリストに統合する **All** プラットフォームオプション
- **変更**：既定の並べ替え順が **Total Downloads** になりました

### 26.2.0 のハイライト

- **修正**：*Version Settings → resource management* からダウンロードセンターに入ったときに
  リソースのインストールコンテキストが失われる問題（NavKey の `@Transient` フィールドが
  Navigation3 の保存可能シリアライゼーションによって黙って破棄されていました）
- **改善**：リソース検索が現在のインスタンスの Minecraft バージョンで自動的に絞り込まれるようになりました
  —— バージョンを手動で選ぶ必要はありません
- **新機能**：**⚠️ Extreme** のガラスレベル（有効化前にパフォーマンスの警告が表示されます）

### 26.1.0 のハイライト

本リリースの目的は、ZalithLauncher2 のレガシーなロジックからさらに離れ、
ZyNova 独自のリソース管理、ダウンロード、ホーム画面、UI の基盤を確立することです。

> **Context First. Less Steps.**

| 機能 | 状態 |
|---|:---:|
| カード式ホーム画面（最近のバージョン / ローカルワールド / サーバー） | ✅ |
| 統合リソース管理コア | ✅ |
| リソースプロバイダー（Modrinth / CurseForge） | ✅ |
| 統合ダウンロードマネージャー（キュー / 同時実行 / 再開 / 再試行 / 検証） | ✅ |
| 最小限のリソースインストール（コンテキスト対応） | ✅ |
| 2 つのレベルを持つガラス UI（オフ / 動的ガラスを有効化。以前の Standard / Enhanced / Extreme レベルは 26.2.2 で削除されました） | ✅ |
| Minecraft 26.4 Snapshot 1 の Vulkan 検出と互換性 | ✅ |
| ZyNova 独自の GitHub Releases によるランチャー更新 | ✅ |
| オンデマンド読み込みとパフォーマンス戦略 | ✅ |

### レンダラー

26.3.0 以降、ランチャーには**ちょうど 3 つの内蔵レンダラー**が同梱されています：

| レンダラー | 内容 | 構成 |
|---|---|---|
| **Ironized Zink**（既定） | デスクトップ OpenGL 4.6 を Vulkan に変換（Mesa Zink + Kopper）、GoyDevv 氏作 | **4 つの公式プリセット**（26.4.0 で個別パラメーター設定は削除） |
| **GL4ES** | クラシックな OpenGL 変換レイヤー | 既定値のまま |
| **MobileGlues** | デスクトップ OpenGL を端末の OpenGL ES 3.x 上に変換 | アップストリームの既定値のまま |

**Ironized Zink の 4 つの公式プリセット**（値はアップストリームと同一）：

| プリセット | 最適な用途 | Minecraft | シェーダー |
|---|---|---|---|
| **Potato** | 絶対的な最大 FPS | 1.8 → 最新（26.x を含む） | 非推奨 |
| **Performance** | Sodium を使った高 FPS | 1.20.x → 最新 | 軽量 |
| **Default** | Zink + シェーダーのバランス（**既定**） | 1.16.x → 最新 | フル（Iris / OptiFine） |
| **Max Compatibility** | すべてを動作させる | すべてのバージョン | フル + 重量級パック |

**26.4.0 以降、パネルに表示されるのは上記 4 つのプリセットのみ**です。
OpenGL バージョンのドロップダウンと 12 個の個別パラメータースイッチは削除され、
一般ユーザーが低レベルパラメーターを手動調整する必要はなくなりました。
プリセットを選ぶと、これまでどおりパラメーターセット全体が一括で書き込まれます。

> ⚠️ 4 つのプリセットはいずれも OpenGL バージョンを **4.6** に固定しています。ドロップダウンがなく
> なったため **新規ユーザーは 4.5 / 4.3 / 3.3 に下げられません**。26.3.0 の期間中にパラメーターを
> 手動調整した既存ユーザーの保存値は引き続き有効ですが（環境変数として注入され続けます）、UI からは
> 編集できません —— 任意のプリセットを選び直すと、サポート対象の組み合わせに戻ります。

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
