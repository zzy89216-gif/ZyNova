/*
 * ZyNova Launcher — 三份声明的正文（按语言，续）
 * GPL-3.0-or-later
 *
 * ② 第三方开源合规声明 + 补充免责与风险告知
 * ③ 使用教程 + 适用地区与法律管辖
 */
package com.movtery.zalithlauncher.onboarding

// ─────────────────────────────────────────────────────────────────
//  ② 第三方开源合规声明 + 补充免责与风险告知
// ─────────────────────────────────────────────────────────────────

internal val LICENSES_ZH = """
《第三方开源合规声明 + 补充免责与风险告知》

一、本软件自身
GPL-3.0，完整文本见项目仓库 LICENSE；上游 ZalithLauncher2 / Fold Craft Launcher / PojavLauncher 的版权声明完整保留。

二、义务最重的组件
Terracotta（陶瓦联机）= AGPL-3.0，原样分发、未修改二进制。AGPL 比 GPL 更严格：修改后再分发，或以网络服务形式提供其功能，必须公开对应源码。

三、弱著佐权组件
MobileGlues（LGPL-2.1）、sora-editor / language-textmate（LGPL-2.1）、LTW-Turbo（LGPL-3.0）、TouchController（LGPL-3.0），均以独立动态库或独立模块分发，可自行替换。

四、宽松许可组件
MIT：GL4ES、ByteHook、OpenNBT、skinview3d；BSD-3-Clause：ANGLE、LWJGL、MMKV；Zlib：SDL3、sdl2-compat；EPL-2.0：tm4e；0BSD：XZ for Java；Apache-2.0：frp / frpc、AndroidX、Ktor、OkHttp 等。逐项清单与全文见下方「第三方组件声明」与应用内「设置 → 关于 → 开源许可」。

五、补充免责与风险告知
1. 存档、世界、截图、配置、模组可能因崩溃、断电、存储损坏或第三方软件冲突而丢失或损坏；
2. 设备可能发热、耗电加快、卡顿，或被系统终止后台进程；
3. 使用第三方账号、服务器或服务可能导致封号、限流、收费或数据被处理；
4. 来源不明的模组 / 整合包可能包含恶意代码；
5. 内网穿透会把本机端口暴露到公网，可能被扫描或滥用。
以上风险由你自行评估并承担；本软件不承诺备份、恢复或赔偿。

六、你能做什么
自用与修改自用：允许；分发给他人：允许，但须附带许可证与版权声明、公开源码、保留上游署名；闭源商用：不允许；修改 AGPL 组件后再分发：必须公开修改源码。
""".trimIndent()

internal val LICENSES_EN = """
《Third-Party Open-Source Notices + Additional Risk Disclosure》

1. The Software itself
GPL-3.0; the full text is in the repository's LICENSE. Copyright notices of the upstream projects ZalithLauncher2 / Fold Craft Launcher / PojavLauncher are preserved in full.

2. Heaviest obligation
Terracotta (multiplayer) = AGPL-3.0, redistributed unmodified. AGPL is stricter than GPL: if you modify and redistribute it, or offer its functionality as a network service, you must publish the corresponding source.

3. Weak-copyleft components
MobileGlues (LGPL-2.1), sora-editor / language-textmate (LGPL-2.1), LTW-Turbo (LGPL-3.0), TouchController (LGPL-3.0) — shipped as separate dynamic libraries or modules and replaceable by you.

4. Permissive components
MIT: GL4ES, ByteHook, OpenNBT, skinview3d; BSD-3-Clause: ANGLE, LWJGL, MMKV; Zlib: SDL3, sdl2-compat; EPL-2.0: tm4e; 0BSD: XZ for Java; Apache-2.0: frp / frpc, AndroidX, Ktor, OkHttp and others. The itemised list and full texts are in the notices below and in Settings → About → Open-source licences.

5. Additional risk disclosure
1. Saves, worlds, screenshots, settings and mods may be lost or corrupted by crashes, power loss, storage failure or conflicts with third-party software;
2. The device may heat up, drain faster, stutter, or have background processes killed by the system;
3. Third-party accounts, servers or services may lead to bans, rate limits, charges or data processing;
4. Mods or modpacks from unknown sources may contain malicious code;
5. Tunnelling exposes local ports to the internet and may be scanned or abused.
You assess and bear these risks yourself; the Software promises no backup, recovery or compensation.

6. What you may do
Personal use and personal modification: allowed. Redistribution: allowed with licence and copyright notices, published source and upstream attribution. Closed-source commercial use: not allowed. Redistributing modified AGPL components: you must publish your changes.
""".trimIndent()

internal val LICENSES_ZH_TW = """
《第三方開源合規聲明 + 補充免責與風險告知》

一、本軟體自身
GPL-3.0，完整文本見專案倉庫 LICENSE；上游 ZalithLauncher2 / Fold Craft Launcher / PojavLauncher 的版權聲明完整保留。

二、義務最重的元件
Terracotta（陶瓦聯機）= AGPL-3.0，原樣發佈、未修改二進位檔。AGPL 比 GPL 更嚴格：修改後再發佈，或以網路服務形式提供其功能，必須公開對應原始碼。

三、弱著佐權元件
MobileGlues（LGPL-2.1）、sora-editor / language-textmate（LGPL-2.1）、LTW-Turbo（LGPL-3.0）、TouchController（LGPL-3.0），均以獨立動態函式庫或獨立模組發佈，可自行替換。

四、寬鬆授權元件
MIT：GL4ES、ByteHook、OpenNBT、skinview3d；BSD-3-Clause：ANGLE、LWJGL、MMKV；Zlib：SDL3、sdl2-compat；EPL-2.0：tm4e；0BSD：XZ for Java；Apache-2.0：frp / frpc、AndroidX、Ktor、OkHttp 等。逐項清單與全文見下方「第三方元件聲明」與應用內「設定 → 關於 → 開源授權」。

五、補充免責與風險告知
1. 存檔、世界、截圖、設定、模組可能因當機、斷電、儲存損壞或第三方軟體衝突而遺失或損壞；
2. 裝置可能發熱、耗電加快、卡頓，或被系統終止背景程序；
3. 使用第三方帳號、伺服器或服務可能導致封鎖、限流、收費或資料被處理；
4. 來源不明的模組 / 整合包可能包含惡意程式碼；
5. 內網穿透會把本機連接埠暴露到公網，可能被掃描或濫用。
以上風險由你自行評估並承擔；本軟體不承諾備份、還原或賠償。

六、你能做什麼
自用與修改自用：允許；散佈給他人：允許，但須附上授權條款與版權聲明、公開原始碼、保留上游署名；閉源商用：不允許；修改 AGPL 元件後再發佈：必須公開修改後的原始碼。
""".trimIndent()

internal val LICENSES_JA = """
《第三者オープンソース遵守声明 + 追加免責・リスク告知》

一、本ソフトウェア自身
GPL-3.0。全文はリポジトリの LICENSE を参照。上流の ZalithLauncher2 / Fold Craft Launcher / PojavLauncher の著作権表示はそのまま保持しています。

二、義務が最も重いコンポーネント
Terracotta（マルチプレイ）= AGPL-3.0。バイナリは改変せずそのまま配布しています。AGPL は GPL より厳格で、改変して再配布する場合、またはその機能をネットワークサービスとして提供する場合は、対応するソースコードの公開が必要です。

三、弱いコピーレフトのコンポーネント
MobileGlues（LGPL-2.1）、sora-editor / language-textmate（LGPL-2.1）、LTW-Turbo（LGPL-3.0）、TouchController（LGPL-3.0）。いずれも独立した動的ライブラリまたはモジュールとして配布され、差し替え可能です。

四、寛容なライセンスのコンポーネント
MIT：GL4ES、ByteHook、OpenNBT、skinview3d。BSD-3-Clause：ANGLE、LWJGL、MMKV。Zlib：SDL3、sdl2-compat。EPL-2.0：tm4e。0BSD：XZ for Java。Apache-2.0：frp / frpc、AndroidX、Ktor、OkHttp など。個別の一覧と全文は下記の「第三者コンポーネント声明」および「設定 → このアプリについて → オープンソースライセンス」で確認できます。

五、追加の免責・リスク告知
1. セーブデータ、ワールド、スクリーンショット、設定、Mod は、クラッシュ、停電、ストレージ故障、第三者ソフトとの競合により失われる・壊れることがあります。
2. 端末が発熱したり、電池消費が増えたり、動作が重くなったり、バックグラウンドプロセスがシステムに終了させられることがあります。
3. 第三者アカウント、サーバー、サービスを利用すると、BAN、レート制限、課金、データ処理が発生する場合があります。
4. 出所不明の Mod / Modpack には悪意あるコードが含まれる可能性があります。
5. トンネル機能はローカルポートをインターネットに公開するため、スキャンや悪用を受ける可能性があります。
これらのリスクは利用者が評価し負担するものとし、本ソフトウェアはバックアップ、復旧、補償を約束しません。

六、できること
私的利用・私的な改変：可能。再配布：ライセンスと著作権表示の添付、ソースの公開、上流のクレジット保持が条件で可能。クローズドソースでの商用利用：不可。AGPL コンポーネントを改変して再配布する場合：改変部分のソース公開が必須。
""".trimIndent()

internal val LICENSES_KO = """
《제3자 오픈소스 준수 선언 + 추가 면책 및 위험 고지》

一、본 소프트웨어 자체
GPL-3.0이며 전문은 저장소의 LICENSE를 참고하십시오. 상위 프로젝트 ZalithLauncher2 / Fold Craft Launcher / PojavLauncher의 저작권 고지는 그대로 유지됩니다.

二、의무가 가장 무거운 구성 요소
Terracotta(멀티플레이) = AGPL-3.0, 바이너리를 수정하지 않고 그대로 배포합니다. AGPL은 GPL보다 엄격하여 수정 후 재배포하거나 그 기능을 네트워크 서비스로 제공할 경우 해당 소스 코드를 공개해야 합니다.

三、약한 카피레프트 구성 요소
MobileGlues(LGPL-2.1), sora-editor / language-textmate(LGPL-2.1), LTW-Turbo(LGPL-3.0), TouchController(LGPL-3.0) — 모두 별도 동적 라이브러리 또는 모듈로 배포되어 교체할 수 있습니다.

四、관대한 라이선스 구성 요소
MIT: GL4ES, ByteHook, OpenNBT, skinview3d. BSD-3-Clause: ANGLE, LWJGL, MMKV. Zlib: SDL3, sdl2-compat. EPL-2.0: tm4e. 0BSD: XZ for Java. Apache-2.0: frp / frpc, AndroidX, Ktor, OkHttp 등. 항목별 목록과 전문은 아래 "제3자 구성 요소 고지"와 앱의 "설정 → 정보 → 오픈소스 라이선스"에서 확인할 수 있습니다.

五、추가 면책 및 위험 고지
1. 저장 파일, 월드, 스크린샷, 설정, 모드는 충돌, 정전, 저장 장치 손상, 서드파티 소프트웨어 충돌로 손실되거나 손상될 수 있습니다.
2. 기기가 발열하거나, 전력 소모가 늘거나, 버벅이거나, 백그라운드 프로세스가 시스템에 의해 종료될 수 있습니다.
3. 서드파티 계정, 서버, 서비스를 사용하면 계정 정지, 속도 제한, 과금, 데이터 처리가 발생할 수 있습니다.
4. 출처가 불분명한 모드/모드팩에는 악성 코드가 포함될 수 있습니다.
5. 터널 기능은 로컬 포트를 인터넷에 노출하므로 스캔이나 악용의 대상이 될 수 있습니다.
이러한 위험은 이용자가 직접 평가하고 부담하며, 본 소프트웨어는 백업, 복구, 보상을 보장하지 않습니다.

六、할 수 있는 일
개인적 사용과 수정: 허용. 재배포: 라이선스와 저작권 고지 첨부, 소스 공개, 상위 프로젝트 출처 표시를 조건으로 허용. 비공개 소스 상업적 사용: 불가. AGPL 구성 요소를 수정해 재배포: 수정한 소스 공개 필수.
""".trimIndent()

internal val LICENSES_RU = """
《Уведомление о сторонних компонентах + Дополнительные риски》

1. Сама Программа
GPL-3.0, полный текст — в файле LICENSE репозитория. Уведомления об авторских правах проектов ZalithLauncher2 / Fold Craft Launcher / PojavLauncher сохранены полностью.

2. Компонент с самыми строгими обязательствами
Terracotta (сетевая игра) = AGPL-3.0, распространяется без изменений. AGPL строже GPL: при изменении и распространении либо при предоставлении её функций как сетевого сервиса необходимо опубликовать соответствующий исходный код.

3. Компоненты со слабым копилефтом
MobileGlues (LGPL-2.1), sora-editor / language-textmate (LGPL-2.1), LTW-Turbo (LGPL-3.0), TouchController (LGPL-3.0) — поставляются отдельными динамическими библиотеками или модулями и могут быть заменены.

4. Компоненты с разрешительными лицензиями
MIT: GL4ES, ByteHook, OpenNBT, skinview3d; BSD-3-Clause: ANGLE, LWJGL, MMKV; Zlib: SDL3, sdl2-compat; EPL-2.0: tm4e; 0BSD: XZ for Java; Apache-2.0: frp / frpc, AndroidX, Ktor, OkHttp и другие. Перечень и полные тексты — в уведомлении ниже и в разделе «Настройки → О программе → Лицензии».

5. Дополнительное раскрытие рисков
1. Сохранения, миры, скриншоты, настройки и моды могут быть утрачены или повреждены из-за сбоев, отключения питания, сбоя накопителя или конфликтов со сторонним ПО;
2. Устройство может нагреваться, быстрее разряжаться, подтормаживать, а фоновые процессы — завершаться системой;
3. Сторонние аккаунты, серверы и сервисы могут привести к блокировкам, ограничениям, платежам или обработке данных;
4. Моды и сборки из неизвестных источников могут содержать вредоносный код;
5. Туннель открывает локальные порты в интернет и может подвергаться сканированию и злоупотреблению.
Вы оцениваете и несёте эти риски сами; Программа не обещает резервное копирование, восстановление или компенсацию.

6. Что разрешено
Личное использование и изменение: разрешено. Распространение: разрешено при наличии лицензии и уведомлений об авторских правах, публикации исходного кода и сохранении указания авторов. Закрытое коммерческое использование: запрещено. Распространение изменённых AGPL-компонентов: необходимо опубликовать изменения.
""".trimIndent()

// ─────────────────────────────────────────────────────────────────
//  ③ 使用教程 + 适用地区与法律管辖
// ─────────────────────────────────────────────────────────────────

internal val GUIDE_ZH = """
《使用教程 + 适用地区与法律管辖》

一、第一次启动
同意三份文档并选择年龄段后进入首页：没有实例就点「下载一个实例」，选版本（可加 Fabric / NeoForge / Quilt）→ 等待下载完成即可启动。

二、装模组 / 资源包 / 光影 / 整合包
「下载」页可选资源核心（Modrinth / CurseForge），会自动解析并安装前置依赖；也可以把文件放进对应目录，或用「分享到 ZyNova」自动导入。

三、联机
陶瓦联机：双方都装本启动器，进同一房间即可（P2P，不用开端口）。樱花内网穿透：需要樱花账号与访问密钥，隧道固定 TCP，本机端口默认 25565 可改。

四、AI 助手
可以让它装游戏、装模组、读日志、改配置、生成 Word / PPT。联网搜索可选 DeepSeek 或 DuckDuckGo；删除文件的操作会走回收站。

五、出问题了
「设置 → 关于 → 日志」可查看与导出；崩溃后可查看崩溃报告或让 AI 分析；反馈请附设备型号、Android 版本、启动器版本与日志文件。

六、性能建议
内存不超过设备可用内存的一半；渲染器优先 MobileGlues / LTW-Turbo，兼容性差再换 GL4ES；大型整合包建议放内部存储并留足空间。

七、卸载与备份
卸载应用不会删除外部存储上的游戏文件；删除实例请在应用内操作；升级前建议先导出一份整合包备份。
""".trimIndent()

internal val GUIDE_EN = """
《Getting Started + Regions and Governing Law》

1. First launch
Agree to the three documents and choose your age group. On the home screen, tap "Download an instance", pick a version (optionally with Fabric / NeoForge / Quilt) and wait for the download to finish.

2. Mods, resource packs, shaders, modpacks
The Download page supports Modrinth / CurseForge as resource sources and resolves dependencies automatically. You can also drop files into the right folder or use "Share to ZyNova" to import them.

3. Multiplayer
Terracotta: both players install this launcher and join the same room (P2P, no port forwarding). Sakura tunnel: requires a Sakura account and access key; the tunnel is TCP-only, local port defaults to 25565 and can be changed.

4. AI assistant
It can install the game or mods, read logs, change settings, and produce Word / PPT files. Web search can use DeepSeek or DuckDuckGo; file deletions go to the recycle bin.

5. Troubleshooting
Settings → About → Logs to view and export logs; after a crash, open the crash report or let the AI analyse it. When reporting, include device model, Android version, launcher version and the log file.

6. Performance tips
Keep memory below half of the device's available RAM; try MobileGlues / LTW-Turbo first and fall back to GL4ES on compatibility issues; keep large modpacks on internal storage with enough free space.

7. Uninstall and backup
Uninstalling the app does not delete game files on external storage; delete instances from inside the app; export a modpack backup before upgrading.
""".trimIndent()

internal val GUIDE_ZH_TW = """
《使用教學 + 適用地區與法律管轄》

一、第一次啟動
同意三份文件並選擇年齡層後進入首頁：沒有實例就點「下載一個實例」，選版本（可加 Fabric / NeoForge / Quilt）→ 等待下載完成即可啟動。

二、安裝模組 / 資源包 / 光影 / 整合包
「下載」頁可選資源核心（Modrinth / CurseForge），會自動解析並安裝前置依賴；也可以把檔案放進對應目錄，或用「分享到 ZyNova」自動匯入。

三、聯機
陶瓦聯機：雙方都裝本啟動器，進同一房間即可（P2P，不用開連接埠）。櫻花內網穿透：需要櫻花帳號與存取金鑰，通道固定 TCP，本機連接埠預設 25565 可改。

四、AI 助手
可以讓它裝遊戲、裝模組、讀日誌、改設定、產生 Word / PPT。聯網搜尋可選 DeepSeek 或 DuckDuckGo；刪除檔案的操作會進資源回收筒。

五、出問題了
「設定 → 關於 → 日誌」可檢視與匯出；當機後可看當機報告或讓 AI 分析；回報請附裝置型號、Android 版本、啟動器版本與日誌檔。

六、效能建議
記憶體不超過裝置可用記憶體的一半；渲染器優先 MobileGlues / LTW-Turbo，相容性差再換 GL4ES；大型整合包建議放內部儲存並留足空間。

七、解除安裝與備份
解除安裝不會刪除外部儲存上的遊戲檔案；刪除實例請在應用內操作；升級前建議先匯出一份整合包備份。
""".trimIndent()

internal val GUIDE_JA = """
《使い方 + 対象地域と準拠法》

一、初回起動
3 つの文書に同意し年齢層を選ぶとホーム画面に入ります。インスタンスがなければ「インスタンスをダウンロード」を押し、バージョン（Fabric / NeoForge / Quilt も可）を選んで完了まで待ちます。

二、Mod・リソースパック・シェーダー・Modpack
「ダウンロード」画面で Modrinth / CurseForge を選べ、依存関係は自動で解決・導入されます。ファイルを所定のフォルダに置くか、「ZyNova に共有」で自動取り込みもできます。

三、マルチプレイ
Terracotta：双方が本ランチャーを入れ、同じルームに入るだけ（P2P、ポート開放不要）。Sakura トンネル：Sakura アカウントとアクセスキーが必要。トンネルは TCP 固定、ローカルポートは既定 25565（変更可）。

四、AI アシスタント
ゲームや Mod の導入、ログの読み取り、設定変更、Word / PPT の作成ができます。ウェブ検索は DeepSeek か DuckDuckGo を選択可能。ファイル削除はゴミ箱を経由します。

五、困ったとき
「設定 → このアプリについて → ログ」で確認・書き出しができます。クラッシュ後はクラッシュレポートを見るか AI に解析させてください。報告時は端末名、Android バージョン、ランチャーのバージョン、ログを添えてください。

六、パフォーマンスのヒント
メモリは端末の空きメモリの半分以下に。レンダラーは MobileGlues / LTW-Turbo を優先し、互換性の問題があれば GL4ES に切り替えてください。大型 Modpack は内部ストレージに十分な空きを確保して置いてください。

七、アンインストールとバックアップ
アンインストールしても外部ストレージのゲームファイルは消えません。インスタンスの削除はアプリ内で行ってください。アップデート前に Modpack のバックアップを書き出すことをおすすめします。
""".trimIndent()

internal val GUIDE_KO = """
《사용 안내 + 적용 지역 및 준거법》

一、첫 실행
세 문서에 동의하고 연령대를 선택하면 홈 화면으로 들어갑니다. 인스턴스가 없으면 "인스턴스 다운로드"를 눌러 버전(Fabric / NeoForge / Quilt 추가 가능)을 고르고 완료될 때까지 기다립니다.

二、모드 / 리소스팩 / 셰이더 / 모드팩
"다운로드" 화면에서 Modrinth / CurseForge를 선택할 수 있고 의존성은 자동으로 해석·설치됩니다. 파일을 해당 폴더에 넣거나 "ZyNova로 공유"로 자동 가져오기도 가능합니다.

三、멀티플레이
Terracotta: 양쪽 모두 이 런처를 설치하고 같은 방에 입장하면 됩니다(P2P, 포트 개방 불필요). Sakura 터널: Sakura 계정과 액세스 키가 필요하며 터널은 TCP 고정, 로컬 포트 기본값 25565(변경 가능).

四、AI 어시스턴트
게임과 모드 설치, 로그 읽기, 설정 변경, Word / PPT 생성이 가능합니다. 웹 검색은 DeepSeek 또는 DuckDuckGo를 선택할 수 있고, 파일 삭제는 휴지통을 거칩니다.

五、문제가 생겼을 때
"설정 → 정보 → 로그"에서 확인·내보내기가 가능합니다. 충돌 후에는 충돌 보고서를 보거나 AI에게 분석을 요청하십시오. 제보 시 기기 모델, Android 버전, 런처 버전, 로그 파일을 첨부해 주세요.

六、성능 팁
메모리는 기기 사용 가능 메모리의 절반 이하로. 렌더러는 MobileGlues / LTW-Turbo를 먼저 시도하고 호환성 문제가 있으면 GL4ES로 전환하십시오. 대형 모드팩은 내부 저장소에 여유 공간을 두고 두는 것이 좋습니다.

七、제거와 백업
앱을 제거해도 외부 저장소의 게임 파일은 삭제되지 않습니다. 인스턴스 삭제는 앱 안에서 하십시오. 업데이트 전에 모드팩 백업을 내보내는 것을 권장합니다.
""".trimIndent()

internal val GUIDE_RU = """
《Руководство + Регионы и применимое право》

1. Первый запуск
Примите три документа и выберите возрастную группу — откроется главный экран. Если экземпляров нет, нажмите «Скачать экземпляр», выберите версию (можно с Fabric / NeoForge / Quilt) и дождитесь загрузки.

2. Моды, ресурспаки, шейдеры, сборки
На странице «Загрузки» доступны Modrinth / CurseForge, зависимости разрешаются и устанавливаются автоматически. Можно также положить файлы в нужную папку или использовать «Поделиться в ZyNova».

3. Сетевая игра
Terracotta: обоим игрокам нужен этот лаунчер, затем вход в одну комнату (P2P, проброс портов не нужен). Туннель Sakura: нужны аккаунт Sakura и ключ доступа; туннель только TCP, локальный порт по умолчанию 25565 (можно изменить).

4. ИИ-ассистент
Может установить игру или моды, прочитать логи, изменить настройки, создать Word / PPT. Веб-поиск — DeepSeek или DuckDuckGo; удаление файлов идёт через корзину.

5. Если что-то пошло не так
«Настройки → О программе → Журналы» — просмотр и экспорт. После сбоя откройте отчёт или попросите ИИ проанализировать. В обращении укажите модель устройства, версию Android, версию лаунчера и файл журнала.

6. Производительность
Память — не более половины доступной. Сначала попробуйте MobileGlues / LTW-Turbo, при проблемах совместимости — GL4ES. Крупные сборки держите во внутреннем хранилище с запасом места.

7. Удаление и резервные копии
Удаление приложения не стирает игровые файлы во внешнем хранилище; экземпляры удаляйте из приложения. Перед обновлением экспортируйте резервную копию сборки.
""".trimIndent()
