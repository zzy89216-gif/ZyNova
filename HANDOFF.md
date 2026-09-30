# ZyNova 项目交接文档（Handoff）

> 本文档供任何接手者（人类或 AI）快速理解项目现状并继续开发。请先完整阅读再动手。

---

## 一、项目概述

- **项目名**：ZyNova（全名 ZyNova Launcher）
- **性质**：基于 [ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2) 开源代码开发的 **非官方修改版** Minecraft: Java Edition Android 启动器
- **许可证**：GPL-3.0（上游也是 GPL-3.0，ZyNova 必须保持开源）
- **GitHub 仓库**：<https://github.com/zzy89216-gif/ZyNova>（分支 `main`）
- **Discord**：<https://discord.gg/QwPpZQHrTa>（**永久邀请**，Discord API 校验 `expires_at = null`）
- **包名**：`com.zynova.launcher`
- **namespace**：仍为 `com.movtery.zalithlauncher`，**不要改 namespace**，否则要改几百个文件的 package 声明
- **核心原则**：**Context First. Less Steps.**
  - 能自动判断，就不要让用户选择
  - 能一步完成，就不要拆成两步
  - 已有上下文，就直接使用上下文
  - 没有必要的按钮直接删除

---

## 二、当前版本与进度

**当前版本：27.1.0**（`launcher_version_code=270100`）

27.x 系列的核心目标是：**在自有的资源管理与 UI 基础之上，把 ZyNova 的能力开放给 AI Agent——
让 AI 不只是「告诉你怎么操作」，而是能直接动手完成。**

### 27.1.0 全局 AI Agent ✅

**一、本版本做了什么（两件事）**

1. **新增全局 AI Agent**（聊天即 Agent，25 个工具可真正执行操作）
2. **修复自动安装前置依赖的 5 处静默失败**

**1）入口与定位**

- 主界面顶部「文件」**旁边**新增 **AI** 按钮，点击**直接进入聊天界面**，**不设独立的 AI 首页**
- **聊天与 Agent 是同一个入口**：
  - 用户：「为什么这个实例进不去？」→ Agent 调 `read_log` / `summarize_log` 读真实日志
  - 用户：「那帮我修」→ Agent 直接调工具改，**不重新要求用户手动操作**

**2）AI 配置（独立于启动器通用设置）**

- 入口在**聊天界面右上角**，**不出现在「设置」页面**，配置存在独立的 `AISettings`
- `ai/AISettings.kt`：复用 `SettingsRegistry` + MMKV，键名统一 `ai` 前缀
  - `aiProvider` / `aiOpenAIKey` / `aiAnthropicKey` / `aiModel`
  - `aiOpenAIBaseUrl` / `aiAnthropicBaseUrl`
  - `aiPermissionMode` / `aiTemperature` / `aiMaxAgentSteps` / `aiShowToolCalls`
- **API Key 只保存在本机 MMKV**，不上传任何自有服务器
- **模型不硬编码**：`GET {baseUrl}/models` 动态拉取后由用户选择

**3）Provider 层（可扩展）**

| 文件 | 作用 |
|---|---|
| `ai/provider/AIProvider.kt` | 接口：`listModels()` + `streamChat()`，含统一的 `AIStreamEvent` |
| `ai/provider/AIHttp.kt` | AI 专用 OkHttp 客户端 + SSE 逐行解析；**`callTimeout(0)` 防止长回答被掐断** |
| `ai/provider/OpenAIProvider.kt` | Chat Completions；兼容 `data` / `models` 两种模型列表返回 |
| `ai/provider/AnthropicProvider.kt` | Messages API；content block / `tool_use` / `tool_result` |
| `ai/provider/AIProviders.kt` | 注册表：新增 Provider = 一个枚举值 + 一个实现 |

**4）Agent 工具架构**

- `ai/agent/AITool.kt`：工具接口 + `AIToolRisk`（READ / WRITE / DANGEROUS）+ `AIToolContext`
- `ai/agent/AIToolRegistry.kt`：注册表，`builtinTools()` 汇总 25 个工具
- `ai/agent/AIAgent.kt`：主循环（请求 → 工具调用 → 执行 → 结果回灌 → 继续）
  - 权限裁决：**「操作确认」模式下若上层没有确认器，一律拒绝写操作**
  - 工具异常会转成可读文本回灌给模型，让它自行调整
  - 步数上限 `aiMaxAgentSteps`，避免模型死循环

**5）25 个工具（全部复用现有系统，不重复实现）**

| 工具组 | 数量 | 复用的现有能力 |
|---|---|---|
| 实例管理 | 3 | `VersionsManager` / `Version` / `VersionFolders` / `PathManager` |
| 内容管理 | 5 | `AllModReader`（真实解析模组元数据）、`VersionFolders`、模组启停与删除 |
| 文件管理 | 6 | `PathManager`，**全部路径经 `resolveInside()` 允许目录校验** |
| 日志与崩溃 | 5 | 启动器日志 / 原生日志 / 实例 `crash-reports` / 错误行聚合 |
| 设置读写 | 3 | `AllSettings` + `SettingsRegistry.units()/findUnit()`（含敏感键拒绝名单） |
| 启动游戏 | 1 | 现有启动事件链路（由 `MainScreen` 注入 `AIToolContext.launchGame`） |
| 搜索与安装 | 2 | `ResourceManager` 统一资源核心（含递归安装必需前置） |

**6）UI**

- `ai/ui/AIChatScreen.kt`：流式输出、工具调用与结果可视化、停止 / 清空、「操作确认」弹窗
- `ai/ui/AIConfigScreen.kt`：Provider / Key / Base URL / 拉取模型列表 / 权限模式
- `viewmodel/AIChatViewModel.kt`：驱动 Agent 循环 + 注入启动与确认回调
- 新增图标 `ic_ai_filled` / `ic_send` / `ic_stop`；新增 31 条字符串 × 4 语言

**7）修复：自动安装前置依赖的静默失败**

统一资源核心中定位到 5 处缺陷，共同表现为「界面显示安装成功、进游戏却缺前置崩溃」：

| # | 缺陷 | 修复 |
|---|---|---|
| 1 | 依赖解析成功但无下载文件 → 静默丢弃 | 记录为缺失并说明原因 |
| 2 | 必需前置下载失败被当成「可选」跳过 | 新增 `ResourceDownloadFailure` / `downloadAllDetailed()`，如实上报 |
| 3 | 作者钉死的依赖版本不校验兼容性 | 必须先通过 `isCompatibleWith()`，否则回退最新兼容版本 |
| 4 | 同一前置解析成两个版本 → 装两份 | 按 `provider:projectId` 去重 |
| 5 | 缺失信息传不到 UI | 一路传到 `_Download.QuickInstall` 并提示 + 4 语言字符串 |

**8）兼容性**

- 未改动认证、渲染器、下载、实例管理内部实现
- 未改动解压界面等既有界面
- 设置单元新增方法均为默认实现，不影响现有调用路径

### 26.4.2 正版登录改用自有应用注册 + 更换应用图标 ✅

**一、本版本做了什么（两件事）**

1. **正版登录改用 ZyNova 自己的 Microsoft 应用注册**（只改 Secret，不改代码）
2. **更换应用图标**（纯资源改动，**未改动任何代码**）

**1）正版登录的 Client ID 切换**

| | 显示名称 | Client ID | 26.4.0 / 26.4.1 | 26.4.2 |
|---|---|---|---|---|
| **ZyNova 自己的应用注册** | ZyNova Launcher | `7b66e168-f8cd-43fc-a52d-2e78dba189b0` | ❌ 未使用 | ✅ **正在使用** |
| **ZalithLauncher2 的应用注册** | ZalithLauncher（上游） | `（已移除）` | ✅ 曾使用 | ❌ 不再使用 |

**2）应用图标更换（涉及文件）**

| 资源 | 说明 |
|---|---|
| `mipmap-{m,h,xh,xxh,xxx}dpi/ic_launcher.webp` | 传统方图标，5 个密度（48/72/96/144/192） |
| `mipmap-{m,h,xh,xxh,xxx}dpi/ic_launcher_round.webp` | 圆形图标，5 个密度 |
| `drawable-nodpi/ic_launcher_bg.png` | 自适应图标**背景层**：画作放大铺满 + 高斯模糊扩边，蒙版任意形状都不露接缝 |
| `drawable-nodpi/ic_launcher_foreground.png` | 自适应图标**前景层**：864px 画布，画作占 **624px（78dp / 108dp）** |
| `drawable-nodpi/ic_launcher_monochrome.png` | 自适应图标**单色层**：按亮度阈值 80% 提亮部做剪影 |
| `mipmap-anydpi-v26/ic_launcher{,_round}.xml` | 引用上面三层 |
| `values/ic_launcher_background.xml` | 兜底底色改为 `#1B2450` |
| `ic_launcher-playstore.png` | Google Play 512×512 |
| `drawable/splash_launcher.xml` | Android 12+ 系统启动画面图标，引用同一前景资源，**自动同步**（文件本身未改） |

⚠️ **应用内的解压界面（`ui/screens/splash/SplashScreen.kt`，首次启动 / 更新后出现的那一屏）
本次未做任何改动**，与 26.4.1 逐字节一致。

**二、切换方式（零代码改动）**

- 只改了**仓库 Secret `OAUTH_CLIENT_ID`** 的取值，**没有改任何代码**，
  认证流程（`MicrosoftAuthenticator.kt` 等）与 26.4.1 **完全一致**
- 取值优先级 **环境变量（CI Secret）> `.oauth_client_id.txt` > `ZalithLauncher/gradle.properties`**，
  因此 `gradle.properties` 里仍是原来的兜底值，源码中**不包含任何实际生效的 Client ID**
- 版本号提升为 26.4.2（`launcher_version_code=260402`）

**三、⚠️ 关键前提：白名单（改这块前必读）**

**只有处于 Minecraft 应用程序允许名单中的应用注册，才能完成正版登录。**

- 否则 `POST https://api.minecraftservices.com/authentication/login_with_xbox` 会返回
  `403 Invalid app registration, see https://aka.ms/AppRegInfo`
- 该错误与 IP、网络、请求频率、账号档案、OAuth 配置**都无关**；
  Microsoft OAuth / Xbox Live / XSTS 三步会全部成功，只在最后一步被服务端拒绝
- 申请入口（**Java Edition Game Service API Review / Application Process**）：
  <https://help.minecraft.net/hc/en-us/articles/16254801392141>
- ⚠️ 如果换成**尚未获批**的 Client ID，正版登录会**立刻失效**；
  这种情况下不要发布正式版本，或把 Secret 换回已获批准的那个

**四、图标设计的两个要点（改图标前必读）**

1. **自适应图标不能让画作铺满 108dp**
   自适应图标的可见区是画布中心 **72dp**；画作若铺满 108dp，
   系统蒙版会切掉边缘（`ZyNova` 字样会被切）。
   因此前景画作按 **78dp / 108dp** 居中放置——**完整可见，且不露背景**。
2. **背景层不要用纯色**
   画作四角颜色差异很大（深蓝 → 亮蓝），纯色背景在圆角处一定会露馅。
   现在的做法是**把画作放大铺满画布再做高斯模糊**，任何蒙版形状下都无缝融合。

**五、文档与代码一致性**

- 四语言 README、CHANGELOG、THIRD_PARTY_NOTICES（含 `assets/licenses/` 镜像）
  均已同步为「使用 ZyNova 自己的应用注册」与「图标已更换」
- 26.4.1 的相关章节**作为历史保留**（当时的叙述是正确的），并在开头标注当前状态

### 26.4.1 正版登录修复（Client ID 允许名单）与失败归因修正 ✅

> ℹ️ 本节描述的是 **26.4.1 发布当时**的状态：当时使用的是
> **上游 ZalithLauncher2 已获批准的应用注册**。
> **26.4.2 起已切换为 ZyNova 自己的应用注册**，见上一节。

**一、背景：26.4.0 的正版登录卡在 Minecraft Services**

现象是：Microsoft OAuth、Xbox Live（XBL）、XSTS **三步全部成功**，
随后停在 `authenticateMinecraft()`，日志里只有一行

```
[NetWorks/DEBUG] MicrosoftAuthenticator: Attempt 1 failed: il6: null
```

`il6` = `com.movtery.zalithlauncher.game.account.microsoft.MinecraftProfileException`。
`null` 不是错误内容——`MinecraftProfileException` 继承 `RuntimeException()` 时**不传 message**，
所以 `message` 恒为 `null`；它携带的 `FREQUENT`(429) / `BLOCKED_IP`(403) /
`PROFILE_NOT_EXISTS`(404) 状态**从来没有被写进日志**。

**二、定位过程（可复用）**

1. 设备上的日志目录可以直接读：
   `/storage/emulated/0/Android/data/com.zynova.launcher/files/logs/`
   （`adb-shell` 可读；DSHA 设备桥对 `Android/data` 是禁止的，必须走设备 shell）
2. 用 `withRetry` 的 `logTag` 反推抛出点：`"MicrosoftAuthenticator"` 这个 tag
   **只在 `MicrosoftAuthenticator.kt` 的私有 `withRetry` 包装里使用**，
   而该文件 5 个 `withRetry` 调用点中，只有 `authenticateMinecraft()` 能抛出
   `MinecraftProfileException`；且日志里没有出现 `Verifying Minecraft ownership`，
   因此可以确定失败在 `login_with_xbox`，即 **403 或 429**
3. 补诊断日志后拿到真实响应（见下）

**三、真实原因：Mojang 侧的 Client ID 允许名单**

```
POST https://api.minecraftservices.com/authentication/login_with_xbox
HTTP 403
{"path":"/authentication/login_with_xbox",
 "errorMessage":"Invalid app registration, see https://aka.ms/AppRegInfo for more information"}
```

- **不是** IP 被封、**不是** 429 频率限制、**不是** 账号没有 Minecraft 档案
- 是启动器的 **OAuth Client ID 不在 Mojang 的允许名单**里
- 已排除的干扰项：从同一台手机的中国移动直连出口 IP 探测
  `api.minecraftservices.com` 返回 **401（正常应答）**，说明该 IP 并未被封
- 该仓库的 **Actions Secrets 为空**，因此 `secrets.OAUTH_CLIENT_ID` 不存在，
  构建回落到 `gradle.properties`，所以打包进 APK 的就是 ZyNova 自己注册的
  Client ID（`7b66e168-f8cd-43fc-a52d-2e78dba189b0`）
- 上游 ZalithLauncher2 的仓库里同样是注释掉的 `#oauth_client_id=xxx`，
  说明**上游是在构建时注入一个已获批准的 Client ID**，该 ID 不在仓库内

**四、已确认的正解：只能使用「已获 Mojang 批准」的应用注册**

- 向 Minecraft 官方提交应用审核：
  **Java Edition Game Service API Review / Application Process**，
  <https://help.minecraft.net/hc/en-us/articles/16254801392141>
  审批通过、Client ID 进入允许名单后，**认证代码无需任何改动**即可登录
- 构建脚本**已经支持**注入外部 Client ID：
  `getKeyFromLocal("OAUTH_CLIENT_ID", ".oauth_client_id.txt", defaultOAuthClientID)`，
  优先级 **环境变量（CI Secret）> `.oauth_client_id.txt` > `gradle.properties`**
- ⚠️ 不要为了绕过它去创建 Client Secret、改 Redirect URI 或改认证架构：
  该错误与这些配置无关

**五、⚠️⚠️ 正版登录使用的 Microsoft 应用注册（最容易搞混的地方，务必分清）**

> **这是本节最重要的一节。** ZyNova 与 ZalithLauncher2 是两个不同项目，
> 但**正版登录所用的应用注册属于 ZalithLauncher2**。请不要把二者混为一谈。

| | 显示名称 | Client ID | 所属项目 | Mojang 允许名单 | 当前状态 |
|---|---|---|---|---|---|
| **上游应用注册** | ZalithLauncher | `（已移除）` | **ZalithLauncher2（上游）** | ✅ 已获批准 | ✅ **26.4.1 正在使用它构建** |
| **本项目的应用注册** | ZyNova Launcher | `7b66e168-f8cd-43fc-a52d-2e78dba189b0` | ZyNova | ❌ 尚未批准 | ❌ 已保留但**不使用** |

- **ZyNova 自己的 Entra 应用（当前未生效）**：支持账户类型「所有 Microsoft 帐户用户」，
  已开启公共客户端流，**未创建 Client Secret**。
  其租户 ID / 对象 ID 属于项目维护者的目录标识，**不写入本文档**，
  需要时请到维护者的 Entra 后台查看
- **注入方式**：仓库 Secret **`OAUTH_CLIENT_ID`**，**构建时注入**，
  **不写进源码**；因此 `ZalithLauncher/gradle.properties` 里保留的是 ZyNova 自己的
  那个（尚未获批），它只在 Secret 缺失时才会生效
- **上游 ZalithLauncher2 的做法完全一致**：其仓库里同样是注释掉的
  `#oauth_client_id=xxx`，真实 ID 通过 CI Secret / `.oauth_client_id.txt` 在构建时注入。
  上面这个 ID 是从 **ZalithLauncher2 2.6.1 的正式 APK** 中解出的
  （`buildKeys` 的「Base64 → 字符码整数数组」混淆可以直接还原，
  参见本文件第「附：如何从 APK 中还原 BuildKeys 字符串」一节）
- **可预期的现象**：由于认证使用 ZalithLauncher2 的应用注册，
  用户微软账号的「已连接的应用 / 应用与设备」中会显示 **ZalithLauncher** 而非 ZyNova，
  **这是预期行为**，不是 bug
- **后续切换（零代码）**：**26.4.2 起已改用 ZyNova 自己的 Client ID**
  （`7b66e168-f8cd-43fc-a52d-2e78dba189b0`），切换方式只是更换仓库 Secret
  `OAUTH_CLIENT_ID`，**未改动任何代码**；详见本节上方的 26.4.2 章节
- ⚠️ **风险提示（维护者须知）**：使用上游的应用注册意味着 ZyNova 在
  Microsoft / Minecraft 侧**以 ZalithLauncher2 的应用身份完成认证**。
  这是分支项目的常见做法，但该应用注册**属于 ZalithLauncher2 项目，并非 ZyNova 所有**；
  应尽快换成自己获批的 ID

**六、26.4.1 的代码改动（只碰失败路径，认证流程一行未改）**

| 文件 | 改动 |
|---|---|
| `game/account/microsoft/MicrosoftAuthenticator.kt` | `authenticateMinecraft()` 失败时记录 URL / 真实 HTTP 状态码 / 响应体 / 完整堆栈；成功时记录 `expiresIn`；403 按响应体区分 `APP_NOT_REGISTERED` 与 `BLOCKED_IP` |
| `game/account/microsoft/MinecraftProfileException.kt` | 构造函数增加可选 `message`；新增 `APP_NOT_REGISTERED` 状态及 `toLocal()` 分支 |
| `game/account/yggdrasil/YggdrasilApi.kt` | `getPlayerProfile()` 失败时记录状态码与响应体 |
| `res/values/strings.xml`、`res/values-zh-rCN/strings.xml` | 新增 `account_logging_app_not_registered` 文案（默认英文 + 简体中文） |
| `ZalithLauncher/gradle.properties` | 版本号 26.4.1（`launcher_version_code=260401`） |
| 仓库 Secret `OAUTH_CLIENT_ID` | 设为上游已获批准的 Client ID（**不在仓库文件中，不是代码改动**） |

⚠️ **日志安全**：`login_with_xbox` 只在**非 2xx** 时读取响应体，
拿到的是服务端的错误 JSON，**不含 `access_token`**。

### 附：如何从 APK 中还原 BuildKeys 字符串

`com.movtery.buildkeys` 插件的 `string(name, value, true)` 只是把字符串
**Base64 编码后转成字符码整数数组**（见插件源码 `DefaultStringObfuscator`），
运行期再用 `java.util.Base64` 解回。因此任何 ZyNova / ZalithLauncher2 的 APK
都能还原出其中内嵌的 Client ID：

1. 从 APK 中取出 `classes*.dex`
2. 用正则找 `\x00\x03\x04\x00`（`fill-array-data-payload`，元素宽度 4），
   读出随后的 int32 数组
3. 把每个 int 当作 `char` 拼成字符串，再做 Base64 解码

> 注意：这是**只读的取证手段**，仅用于确认某个 APK 实际使用哪个 Client ID；
> 它同样说明**内嵌在 APK 里的 Client ID 并不是秘密**，任何人都能还原。

### 26.4.0 正版登录回归与议题修复 ✅

**一、恢复 Microsoft（正版）登录入口（Issue #8）**

26.1.0 只是**切断了 UI 与 ViewModel 的接线**，认证后端从未被删。
本次按 v2.5.1 的原实现还原，涉及 5 个文件：

| 文件 | 改动 |
|---|---|
| `ui/screens/content/elements/AccountElements.kt` | 加回 `MicrosoftLoginOperation` 状态机（`None` / `Tip`）+ `MicrosoftLoginTipDialog()` 添加说明弹窗；`LoginMenuDialog()` 增加 `onMicrosoftLogin` 参数并在左侧列「离线登录」**之上**插回「微软登录」项 |
| `ui/screens/content/AccountManageScreen.kt` | `AccountManageContent` 加回 `MicrosoftLoginOperation(loginUiState.microsoftOp, actions)` 渲染；`LoginMenuDialog` 接上 `onMicrosoftLogin`（`isMicrosoftLogging()` 为真时不再重复发起）；新增私有 `MicrosoftLoginOperation` 组合函数，确认后发 `PerformMicrosoftLogin` |
| `viewmodel/AccountManageViewModel.kt` | 加回 `UpdateMicrosoftLoginOp` intent、`_microsoftLoginOp`、`LoginUiState.microsoftOp`（`loginUiState` 的 `combine` 恢复 4 路）；新增 `PerformMicrosoftLogin`；「添加账号」与「会话续期」共用私有 `startMicrosoftLogin()` |
| `game/account/AccountUtils.kt` | 加回 `isMicrosoftLogging()`（`TaskSystem.containsTask(MICROSOFT_LOGGING_TASK)`），防止重复发起设备代码流 |
| `res/values/strings.xml`、`res/values-zh-rCN/strings.xml` | 补回 11 条 `account_supporting_microsoft_tip_*` 文案（默认英文 + 简体中文）；弹窗共用到 12 条，`link_purchase` 一直保留 |

⚠️ **UI 与 ViewModel 存在同名类型**：`AccountElements.kt` 的 `MicrosoftLoginOperation`（sealed interface）
与 `AccountManageScreen.kt` 里同名的私有 `@Composable fun MicrosoftLoginOperation(...)` 是两回事，
这是 v2.5.1 的原有写法（Kotlin 允许函数与类型同名），**不要"顺手"给其中一个改名**。

**二、登录实现的关键事实（改这块前必读）**

- 认证方式是 **OAuth 2.0 设备代码流（device code flow）**，**不是重定向流**：
  `POST /consumers/oauth2/v2.0/devicecode` → 应用内 WebView 打开 `verificationUrl`
  → 轮询 `/consumers/oauth2/v2.0/token`
- 因此 **不需要 Redirect URI，也不需要 SHA-1 指纹**
- Azure 应用注册只需满足一条：**「允许公共客户端流 / Allow public client flows」= 是**
- 也**绝不能创建 Client Secret**：设备代码流属于公共客户端，带 Secret 反而会破坏该流程
- Client ID 是**公共标识**，必须随包分发（`buildKeys.OAUTH_CLIENT_ID`），它本来就会被编译进 APK；
  真正的机密是 Client Secret，本项目不创建、不使用、不保存

**三、签名（未改动）**

- Release 与 Debug 都使用 `ZalithLauncher/zalith_launcher_debug.jks`，别名 `movtery_zalith_debug`，
  口令取 `gradle.properties` 里的上游公开默认值（`default_store_password` / `default_key_password`）
- 该证书 SHA-1：`0D:FD:FB:0D:DF:B4:D0:3F:73:1C:F4:7F:0F:40:FF:D7:7A:CB:02:DC`
  （CN=MovTery，有效期至 2050-05-17）
- `zalith_launcher.jks` 未被任何构建配置引用，但**维护者刻意保留**，不要删

**四、修复下载资源页「类别」为空（Issue #6）**

- 根因：26.2.1 新增并默认启用「所有平台」后，类别候选列表只在「实际只查一个来源」时才有内容；
  26.2.3 把判据改成「实际请求的来源数量」，只救回了只有 CurseForge 一个来源的「存档」
- 修复：`SearchAssetsScreen` 的 `allCategories` 改为**始终取参照来源的类别列表**；
  `buildFilter()` 里类别条件改为 `filter.categories.takeIf { platform == referencePlatform } ?: emptyList()`，
  即**只下发给参照来源**，其余来源清空，保住「绝不跨来源传类别 ID」的不变式
- 多来源时 `SearchFilter` 的 `categorySourceName` 非空，标题显示为「类别（仅 CurseForge）」
- 新增字符串 `download_assets_filter_category_with_source`

**五、修复列表卡片缺少「资源的类别」徽章（Issue #6）**

- 根因：`_Search.Result.kt` 的 `ResultList` 调用 `ResultProjectLayout` 时**漏传 `classes` 实参**，
  形参默认 `null` → `ClassesIdentifier` 分支恒为假；详情页 `SearchIdScreen` 传了该实参，
  所以表现为「详情页有徽章、列表页没有」
- 修复：补上 `classes = classes`（一行）

**六、Ironized Zink 只保留 4 个官方预设（Issue #7）**

- `IronizedZinkConfigCards.kt` 删掉 13 个单独参数控件（1 个 OpenGL 版本下拉 + 12 个开关），
  面板只剩预设卡，`CardPosition` 由 `Top` 改为 `Single`
- `AllSettings` 的 14 个 `ironizedZink*` 定义**全部保留**（见第五节原则），
  `IronizedZinkSettings.kt` / `IronizedZinkConfig.kt` / `GameLauncher.setRendererEnv()` **一律不动**
- ⚠️ **已知取舍**：4 个预设的 `glVersion` 全部是 **4.6**，
  删掉版本下拉后**新用户无法再把 OpenGL 版本降到 4.5 / 4.3 / 3.3**；
  26.3.0 期间手改过参数的老用户其旧值仍会继续生效
- 「已自定义」与「不安全参数组合」两处提示已改写为「重新选择任意预设即可恢复」

### 26.3.0 渲染器体系重做与发布形态收敛 ✅

**一、内置渲染器只保留三个，其余全部删除**

| 渲染器 | 状态 | 配置 |
|---|---|---|
| **Ironized Zink** | 新增内置，**默认渲染器** | 完整原生配置 + 4 个官方预设 + 13 个可调参数（**26.4.0 起面板只保留 4 个预设**，见第二节） |
| **GL4ES** | 保留 | 保持默认，不追加任何环境变量 |
| **MobileGlues** | 新增内置 | 保持上游默认配置 |

删除：NG-GL4ES（Krypton Wrapper）、Kopper Zink、VirGL、Freedreno、Panfrost。

**二、关键实现位置（改渲染器前必读）**

| 关注点 | 位置 |
|---|---|
| 渲染器注册表 | `game/renderer/Renderers.kt` → `addRenderers(IronizedZinkRenderer, GL4ESRenderer, MobileGluesRenderer)` |
| 三个内置渲染器 | `game/renderer/renderers/{IronizedZink,GL4ES,MobileGlues}Renderer.kt` |
| Ironized Zink 配置模型（移植自上游） | `game/renderer/ironizedzink/IronizedZinkConfig.kt` |
| Ironized Zink 设置读写 | `game/renderer/ironizedzink/IronizedZinkSettings.kt` |
| Ironized Zink 配置 UI | `ui/screens/content/settings/IronizedZinkConfigCards.kt` |
| 环境变量注入（**最容易出错的地方**） | `game/launch/GameLauncher.kt` → `setRendererEnv()` |
| 设置项定义 | `setting/AllSettings.kt` → `renderer` 与 `ironizedZink*` 共 15 项 |

**三、环境变量注入的三条铁律（`setRendererEnv`）**

1. `renderer.getRendererEnv()` 在通用兜底块**之前**合并；
2. 通用兜底块（`MESA_LOADER_DRIVER_OVERRIDE` / `MESA_GL_VERSION_OVERRIDE` /
   `force_glsl_extensions_warn` …）**必须把三个内置渲染器全部排除**，
   否则会覆盖用户自己选的 OpenGL 版本等设置；
3. 插件渲染器走 `selectedRendererPlugin != null` 提前返回，不受此块影响。

> 当前排除条件写在 `GameLauncher.kt`：
> `renderer != GL4ESRenderer && renderer != MobileGluesRenderer && renderer != IronizedZinkRenderer`。
> **以后新增内置渲染器时，务必同步加到这个判断里。**

**四、默认渲染器 = Ironized Zink / Default**

- `AllSettings.renderer` 默认值 = `IRONIZED_ZINK_UNIQUE_IDENTIFIER`
- 该唯一标识符**沿用原「Kopper Zink」的 UUID**（`0fa435e2-…`），
  这样老用户保存过的 Kopper Zink 选择会自动落到 Ironized Zink，而不是被重置
- 常量放在 `IronizedZinkConfig.kt` 顶层，**故意不挂在渲染器对象上**，
  避免 `AllSettings` ↔ 渲染器对象初始化成环

**五、选中后自动展开配置面板**

`ui/screens/content/settings/RendererSettingsScreen.kt`：
当 `AllSettings.renderer.state == IRONIZED_ZINK_UNIQUE_IDENTIFIER` 时，
在渲染器列表卡片**下方**插入一个 `AnimatedItem`，渲染 `IronizedZinkConfigCards()`。
（**26.4.0 起该面板只剩预设卡**，不再包含 OpenGL 版本下拉与 12 个参数开关。）
（只有全局渲染器设置里有该面板；`VersionConfigScreen` 的**按版本**渲染器选择不显示它 ——
Ironized Zink 的参数是全局的，不按版本区分。）

**六、发布形态：一个通用版本 + 代码混淆**

- `ZalithLauncher/build.gradle.kts`：`splits { … }` 已删除，`projectArch` 固定为 `"all"`，
  不再读取 `-Darch`，也不再按架构裁剪 JRE / LWJGL 资源
- Release 保持 `isMinifyEnabled = true` + `isShrinkResources = true`
- 产物名：`ZyNova-<版本>.apk`（不再有 `-arm64-v8a` 之类的后缀）

**七、修复主页滚动时卡片异常移动**

`ui/components/Reorder.kt` → `ReorderState.onBounds()`：
拖动排序用 `boundsInRoot()` 记录项的屏幕范围，而**滚动会让所有项的 root 坐标一起变化**，
这段变化被误判成「布局位移」→ 每一项都播放让位动画 → 卡片乱跑。
修复：**只有在 `draggingKey != null`（正在拖动）时才处理位置变化**，其余情况直接忽略。

**八、许可证与合规**

- 根目录新增 `THIRD_PARTY_NOTICES.md`，并复制到 `assets/licenses/` 随 APK 分发
- 随包附带：Ironized Zink 的 GPL-3.0 全文、MobileGlues 的 LGPL-2.1 全文、
  Mesa 完整许可集、上游 Ironized Zink 的 `CREDITS.md` 原文
- 应用内「关于 → 开源许可」同步：移除 NG-GL4ES 条目，
  Mesa 条目改用完整许可集，新增 Ironized Zink 与 MobileGlues
- **绝不删除上游版权声明**；**绝不把不同项目的许可证混为一谈**

**九、GitHub API Token 约定**

- 只允许**本地环境变量**或**CI Secret**注入，**不写入源码 / 提交 / APK**
- CI：`GITHUB_TOKEN: ${{ secrets.GH_TOKEN || github.token }}`（未配置 Secret 时用内置 token）
- 本地：`export GITHUB_TOKEN=...`，用完 `unset`，并删除本地副本
- 推送前必须跑隐私扫描（见第十节）

### 26.2.6 修复与变更 ✅

1. **修复拖动排序后卡片内容被顶到错误位置**
   - 位移修饰符原本在 `clip` / `background` 内侧 → 只有内容动、背景不动
   - 让位动画被后续布局变化打断时 `Animatable` 停在中间值 → 残留偏移一直留着
   - 修复：位移修饰符放到**最外层**；无新位移时归零、拖动结束也归零
2. **右侧菜单恢复上游排布**（26.2.5 改成 Column 后版本行跑到中间）
3. **操作菜单改为长按拖动换边**：直接采用上游 ZalithLauncher2 的
   `ui/screens/content/home/ActionMenuDrag.kt` + `setting/enums/ActionMenuSide.kt`
   - 长按整块提起并跟手，越过中线预览停泊侧，松手平滑停泊到左/右，内容区一起让位
   - 停泊侧持久化到 `AllSettings.launcherActionMenuSide`
   - 26.2.5 给右侧菜单内部三块做的排序已移除

### 26.2.5 修复与新增 ✅

1. **彻底移除卡片式主页的卡片边框**
   - 根因：卡片由多层叠加（`Surface` 圆角填充 + `graphicsLayer` 缩放层 + `shadowElevation`），
     内边距区与内容区的叠加层数不同 → 内容区偏亮、边缘一圈偏暗 = 视觉上的「边框」
   - 修复：卡片只有**一层**背景 `clip(shape).background(cardColor())`，
     去掉阴影与缩放图层，点击反馈用默认按压动画
2. **卡片式主页支持长按拖动排序**
   - 版本卡片之间、卡片内的世界 / 服务器、以及**右侧菜单的三块**
     （账号头像 / 版本行+设置按键 / 启动按钮）都可以长按拖动排序，顺序自动记住
   - 新增基础设施：`ui/components/Reorder.kt`（`ReorderState` / `rememberReorderState` / `Modifier.reorderItem`）
     与 `game/home/HomeLayoutStore.kt`（MMKV 顺序持久化）
   - 实现要点：用**项的屏幕范围 + 指针位置**判断落点，因此纵向列表、横向列表、
     `FlowRow` 都能用，不依赖 `LazyColumn`（卡片式主页本身不能嵌套滚动容器）
   - 顺序按**标识符**保存而不是下标，增删条目后不会错位

### 26.2.4 修复与新增 ✅（2 个新议题）

1. **修复卡片式主页的卡片不透明度不跟随「背景元素不透明度」**（Issue #4）
   - 根因：卡片颜色写死成 `cardColor(false)`，即显式关闭「颜色受背景内容影响」；
     自定义主页卡片（`CustomHomeCard`）默认是 `cardColor(true)`，只有卡片式主页漏了
   - 修复：改为 `cardColor()`；未设置自定义背景时行为不变
     （`influencedByBackground` 在背景无效时本来就返回原色）
2. **卡片式主页支持调整卡片大小**（Issue #5，新功能）
   - 新增设置 `homeCardSize`（70~140%，默认 100%）
   - 位置：设置 → 启动器 → 主页 → 卡片大小，**仅在主页类型为「卡片主页」时显示**
   - 作用于卡片内边距、卡片间距、标题/副标题字号、分组间距与条目宽度
   - 默认 100% 与既有外观一致，老用户升级观感不变

### 26.2.3 修复 ✅（模组以外的资源下载链路）

1. **资源包 / 光影包 / 存档搜索结果几乎为空**
   - 根因：加载器过滤器是**来源特有**的，却被当成了所有资源类型的通用条件。
     只要实例装了加载器（如 Fabric），就会把该加载器作为搜索条件下发，
     而资源包 / 光影 / 存档**不按加载器分类**：
     Modrinth 光影 `categories:fabric` → **0 条**；
     Modrinth 资源包 `categories:fabric` → 只剩 4 条（不带过滤是 19275 条）
   - 修复：加载器过滤**只在启用它的资源类型（Mod / 整合包）上附加**
     （`SearchScreenViewModel.buildFilter()` 按 `enableModLoader` 判断）
2. **聚合搜索总页数变 0（界面 1 / 0 且无法翻页）**
   - `AggregatedSearchResult` 原来取各来源总页数的 **min**，任一来源为空就变 0
   - 改为 **max 且至少为 1**
3. **「所有平台」会请求不支持该类型的来源**
   - 存档在 Modrinth 无对应项目类型，却仍发请求并在 `platformClasses.modrinth!!` 处空指针
   - 现在只并发请求**真正支持该类型**的来源（`ResourceProviders.of(p).supports(type)`），
     并把 `!!` 换成 `UnsupportedClassesException`
4. **未配置 CurseForge API Key 时 CurseForge 侧完全不可用**
   - 未配置的 CI Secrets 会以**空字符串**注入，`getKeyFromLocal` 只判断 `null` →
     本地 `curseforge_api.txt` / gradle 属性里的兜底 Key 永远不生效
   - CurseForge 官方接口缺 Key 必然 403，而 MCIM 镜像此前**只在大陆启用**
   - 修复：空字符串视为未配置；**无 Key 时始终保留 MCIM 镜像源**
5. **存档「类别」过滤器永久不可用**：判断依据从「是否选了所有平台」改为**实际请求的来源数量**
   - ⚠️ **26.4.0 已再次改写**（见第二节 26.4.0 第四项）：该判据只救回了「存档」一个类型，
     模组 / 整合包 / 资源包 / 光影在默认「所有平台」下依然没有类别可选
     （同一面板的「模组加载器」却一直可用）。现在类别列表**始终**以参照来源为准，
     类别条件**只下发给参照来源**，其余来源清空
6. **存档解压失败残留 `.zip`**：无论成功失败都兜底清理解压包

### 26.2.2 修复 ✅（对应仓库中 3 个 Issue）

1. **移除「⚠️极致」玻璃档，玻璃效果简化为两档：关闭 / 启用动态玻璃**（Issue #2）
   - 「极致」档用 `RuntimeShader`（`RenderEffect`）对整个元素做多重采样模糊 + 波纹折射扭曲，
     而 Compose 的 `renderEffect` 作用在**整个图层**上，会把承载文字的图层一起模糊，
     导致字体明显模糊、文字渲染异常 → 直接移除该档位及全部与之绑定的高开销效果
     （动态模糊半径、动态光照、多层视差、卡片吸附与动态阴影、噪点纹理、折射着色器）
   - 旧配置 `Standard` / `Enhanced` / `Extreme` 在 `loadAllSettings` 中**一次性迁移为「启用动态玻璃」**
2. **修复一键安装偶发 `安装资源失败！No compatible version found for this instance`**（Issue #3）
   - 主因：加载器过滤条件是**来源特有**的，但此前只有「所有平台」聚合搜索会按来源重新解析；
     切换搜索平台时又把加载器清空，导致单一来源搜索**完全不按加载器过滤**，
     结果里混进其他加载器的资源，点安装才在版本匹配阶段失败
   - 现在单一来源与聚合搜索统一走 `buildFilter(platform)`，每次都按目标来源重新解析加载器
   - 版本匹配失败拆成 **实例信息不可用 / 查询失败 / 确实没有兼容版本** 三种并本地化提示
     （提示中带目标实例的 MC 版本 + 加载器）；查询异常不再被吞成「没有兼容版本」
   - 必需前置依赖解析失败不再静默丢弃，会记录日志并收集进安装计划
   - 加载器名称改为归一化比较；只有 Mod / 整合包强制校验加载器
3. **修复 Discord 邀请链接全部失效**（Issue #1）
   - 旧链接是**临时邀请**（`Tbn8Bqg2Yp`，Discord API 返回 `50270 Invite is expired`）
   - 已换成**永久邀请** `QwPpZQHrTa`，并同步到 README（当时为 `README.md` / `README_EN_US.md` /
     HANDOFF / 应用内 `UrlManager`（`URL_COMMUNITY`、`URL_DISCORD`）

### 26.2.1 修复与新增 ✅

- **极致玻璃效果重做**：此前用 `drawBehind` 画渐变，视觉上"等于没做"；
  现改为 `RuntimeShader`（AGSL）实现 6 点环形多重采样模糊 + 波纹折射扭曲
- **搜索结果卡片增加快捷安装按钮**（此前只加在版本列表，用户实际浏览的是搜索结果）
- **模组加载器按实例自动选中**
- **主页改为以「版本」为模块**，模块内直接展示该版本自己的世界与服务器
- **新增「所有平台」聚合搜索**（CurseForge + Modrinth 合并，默认）
- **排序默认改为「总下载量」**

### 26.2.0 修复与新增 ✅

- **修复安装上下文丢失**：上下文原本放在 NavKey 的 `@Transient` 字段上，
  被 Navigation3 的 saveable 序列化丢弃；已改由 `ScreenBackStackViewModel` 持有
- **资源搜索自动按当前实例 MC 版本过滤**（Context First）
- **玻璃效果新增「⚠️极致」档**：切换前弹出性能警告，包含 10 项高开销视觉效果
- 详见 `CHANGELOG.md`

### 26.1.1 修复 ✅

- **修复卡片式主页导致的启动器崩溃**
  - 卡片主页内部使用了 `verticalScroll`，而它被放在 `LauncherScreen` 的 `LazyColumn` 的 `item` 中，
    造成滚动容器嵌套、高度约束无界，触发 `IllegalStateException` 崩溃
  - 修复方式：卡片主页不再自带滚动，统一由外层 `LazyColumn` 负责

### 26.1.0 已完成 ✅

1. **移除正版登录入口**（**⚠️ 该决定已于 26.4.0 撤销，见第二节 26.4.0 第一项**）
   - 当年的改动：添加账号界面不再提供微软（正版）登录入口，只保留离线登录与第三方认证服务器
   - 移除了对应的 UI、点击逻辑、状态、无用资源与依赖
   - **保留已有微软账号的会话续期能力**（`AccountManageIntent.ReloginMicrosoft`），确保旧用户升级后仍可正常启动游戏
   - 26.4.0 恢复了登录菜单里的「微软账号」入口；`microsoftLogin` 认证实现当年从未被删，
     所以恢复成本很低

2. **彻底移除 ZalithLauncher2 更新链**
   - 移除了 ZL2 更新检查、更新提示、更新弹窗、更新 URL、对应 UI 与状态
   - 移除了上游版本信息文件（GitHub Contents）解析与 Base64 解码
   - 移除了网盘分发链路
   - ZyNova 只维护自己的更新体系：使用本项目自己的 GitHub Releases
   - 安装包按设备实际支持的 ABI 自动挑选（arm64-v8a / armeabi-v7a / x86_64 / x86 / universal）

3. **统一资源管理核心（Resource Management Core）**
   - 统一资源模型：`Resource`、`ResourceVersion`、`ResourceFile`、`ResourceDependency`、`ResourceType`
   - 统一流程：**搜索 → 资源详情 → 版本匹配 → 文件选择 → 下载 → 校验 → 安装**
   - 入口：`ResourceManager`

4. **资源来源 Provider**
   - `ResourceProvider` 接口 + `ResourceProviders` 注册表
   - Modrinth、CurseForge 作为独立来源实现，与 UI 解耦
   - 未加入 BBSMC

5. **统一下载管理器**
   - `ResourceDownloadManager`：队列、并发、进度、断点续传、重试、取消、校验（sha1）、临时文件清理、安装触发

6. **极简资源安装**
   - 从「版本设置 → Mods / 资源包 / 光影 / 存档」进入资源页面时携带实例上下文
   - 点击下载直接安装，不再重复要求选择 Minecraft 版本、实例、安装位置
   - 资源列表显示「已安装」状态
   - 资源中心（主界面入口）只负责浏览与管理，**不再提供一键安装入口**

7. **卡片式主页**
   - 新增卡片主页：最近版本 / 本地世界 / 已保存服务器，点击即启动 / 进入 / 加入
     （26.2.1 起改为**以版本为模块**，模块内直接展示该版本的世界与服务器）
   - 设置中可选：默认主页 / 卡片主页 / 自定义主页
   - `HomeDataProvider` 作为主页统一数据访问接口，按需加载

8. **玻璃效果（Glass UI）两档**（26.2.2 起）
   - `GlassLevel`：关闭 / 启用动态玻璃，**默认关闭**
   - 关闭档不叠加任何玻璃高光层；启用档在毛玻璃之上叠加缓慢流动的高光与折射光晕
   - 历史上曾有「标准 / 增强 / ⚠️极致」，26.2.2 因「极致」导致字体模糊而整体简化为两档
   - 标准档为静态高光（零持续动画），增强档才启用流动动画

9. **Minecraft 26.4 Snapshot 1 Vulkan 适配**
   - `VulkanRequirement` 要求档案，把「Minecraft 需要什么」与「怎样检测」分离
   - `VulkanCheckResult`：**可用 / 不可用 / 检测失败**三态 + 6 种具体原因
   - 采集 GPU、设备型号、驱动路径，支持主动重新检测
   - 检测依据是设备**实际枚举出的能力**，不是设备声明的支持情况
   - 检测失败不会写入缓存，避免以后不再重新检测

10. **按需加载与性能策略**
    - 启动时只加载账号与路径，不扫描 Mod / 世界 / 服务器
    - 各资源页面在进入时才扫描
    - 主页扫描限量（最多解析 10 个存档目录）
    - 更新检查 1 小时限频

11. **关于页面改造**
    - 开发者署名 zzy，补 GitHub 与 Discord 入口
    - 修复署名错误（原「ZalithLauncher2 作者」卡片被错误显示为 ZyNova 的作者）
    - ZalithLauncher2 作者卡片下移至致谢区，保留赞助入口

### 后续待办 ⬜

1. **Boat 后端双端**：用户曾想接入 Boat 后端（像老版 FCL 一样），目前未开始。
2. **AI 助手**：接入 OpenAI V1 接口，自动加模组写配置，未开始。
3. **自定义主页数据接口**：`HomeDataProvider` 已可作为统一数据入口，可继续开放给自定义主页。
4. **键位优化、渲染优化**：用户提过，未深入做。
5. **继续收敛 ZL2 遗留逻辑**：资源系统已统一，其他模块仍可能残留上游耦合。
5.1 **Ironized Zink 的 OpenGL 版本不可在界面调整（26.4.0 的已知取舍）**：
   - Issue #7 要求删除面板上全部单独参数控件，已照办；但 4 个官方预设的 `glVersion` **全都是 4.6**，
     因此新用户再也无法把 OpenGL 版本降到 4.5 / 4.3 / 3.3
   - 若日后收到「老设备 / 老光影需要更低 GL 版本」的反馈，可选做法：
     恢复**仅** OpenGL 版本这一个下拉（其余 12 个开关保持删除），
     或与上游确认后再决定是否给某个预设换更低的 GL 档（**不要擅自改上游预设取值**）
6. **配置 `CURSEFORGE_API_KEY`（已知遗留，26.2.3 起已有兜底，维护者决定暂不处理）**：
   - 仓库的 Actions Secrets 里**没有配置** `CURSEFORGE_API_KEY`，
     所以打包出来的 APK 调 CurseForge 官方接口**必然返回 403**；
     26.2.3 之前，「所有平台」聚合搜索里的 CurseForge 侧实际上一直是**空的**
     （当时没人注意到，因为 Mod/整合包/资源包/光影在 Modrinth 上也有，
     唯独「存档」只有 CurseForge 提供，于是固定显示空列表）
   - **26.2.3 的兜底**：没有 API Key 时**始终保留 MCIM 镜像源**
     （`mod.mcimirror.top`，不需要 Key），CurseForge 侧恢复可用；
     不需要等 Secret 配置好
   - 如果以后想让官方接口也生效，到
     `Settings → Secrets and variables → Actions` 添加同名 Secret 即可，
     **不需要改代码**
   - ⚠️ **密钥值只存在于 GitHub Secrets / 本地密钥文件**（`.curseforge_api.txt` 已被 `.gitignore` 忽略）。
     **绝对不要写进本文件、CHANGELOG、README、Issue、Release 说明或任何提交**
   - 另需知道：CurseForge API Key 会被**打进 APK**（上游设计如此，客户端必须自带），
     但这**不是**可以在文档里公开它的理由

---

## 三、代码位置

| 位置 | 说明 |
|---|---|
| GitHub `main` | 唯一权威源码，所有改动以仓库为准 |
| 本地工作副本（ext4） | 因 sdcard 是 noexec/fuse 分区，git 与编译必须放在 ext4 分区执行 |

⚠️ **重要**：若在 `/sdcard`（fuse 分区）上执行 `git add` 或编译，会出现 SIGSEGV 崩溃。
**所有 git 操作与编译请在 ext4 分区的工作副本中进行。**

> 📌 本地副本只是临时工作区，权威源码只有 GitHub。
> 继续开发时把仓库 `git clone` 到 ext4 分区的任意工作目录即可，收尾时再删掉。
> **文档里不要记录某台机器上的具体绝对路径**，避免把个人环境信息带进仓库。

---

## 四、关键文件清单

### 新增文件

| 文件 | 作用 |
|---|---|
| `ai/AISettings.kt` | AI 独立配置（Provider / Key / Model / BaseUrl / 权限模式） |
| `ai/AIPermissionMode.kt` | 完全控制 / 操作确认 |
| `ai/model/AIMessage.kt` | 与 Provider 无关的统一消息模型 |
| `ai/model/AIModelInfo.kt` | 动态获取到的模型信息 |
| `ai/provider/AIProvider.kt` | Provider 接口 + 统一流式事件 |
| `ai/provider/AIHttp.kt` | AI 专用 OkHttp + SSE 解析 |
| `ai/provider/OpenAIProvider.kt` | OpenAI Chat Completions 实现 |
| `ai/provider/AnthropicProvider.kt` | Anthropic Messages API 实现 |
| `ai/provider/AIProviders.kt` | Provider 注册表 |
| `ai/agent/AITool.kt` | 工具接口 / 风险等级 / 执行上下文 |
| `ai/agent/AIToolRegistry.kt` | 工具注册表 |
| `ai/agent/AIToolSchema.kt` | JSON Schema 辅助 |
| `ai/agent/AIAgent.kt` | Agent 主循环 + 权限裁决 |
| `ai/agent/tools/*.kt` | 25 个内置工具（实例 / 内容 / 文件 / 日志 / 设置 / 启动 / 搜索安装） |
| `ai/ui/AIChatScreen.kt` | AI 聊天界面（同时就是 Agent 界面） |
| `ai/ui/AIConfigScreen.kt` | AI 配置界面（独立于启动器通用设置） |
| `viewmodel/AIChatViewModel.kt` | 驱动 Agent 循环 + 注入启动/确认回调 |
| `res/drawable/ic_ai_filled.xml` | AI 入口图标 |
| `res/drawable/ic_send.xml` | 发送图标 |
| `res/drawable/ic_stop.xml` | 停止图标 |
| `game/download/resources/Resource.kt` | 统一资源模型 + 平台模型适配 |
| `game/download/resources/ResourceProvider.kt` | Provider 接口、Modrinth/CurseForge 实现、注册表 |
| `game/download/resources/ResourceDownloadManager.kt` | 统一下载管理器 |
| `game/download/resources/ResourceInstallManager.kt` | 统一安装与安装状态判定 |
| `game/download/resources/ResourceManager.kt` | 资源管理核心统一流程入口 |
| `game/home/HomeDataProvider.kt` | 主页统一数据访问接口 |
| `ui/screens/main/card_home/CardHomePage.kt` | 卡片式主页 |
| `setting/enums/GlassLevel.kt` | 玻璃效果两档枚举（26.2.2 由三档简化） |
| `upgrade/ZyNovaRelease.kt` | ZyNova 自有更新体系数据模型 + ABI 自动挑选 |
| `utils/device/VulkanRequirement.kt` | Minecraft 的 Vulkan 要求档案 |
| `utils/device/VulkanCheckResult.kt` | Vulkan 三态检测结果模型 |

### 主要修改文件

| 文件 | 改动 |
|---|---|
| `ui/screens/content/elements/AccountElements.kt` | 移除微软登录入口 UI 与状态（**26.4.0 已还原**） |
| `ui/screens/content/AccountManageScreen.kt` | 移除首登录菜单的微软分支与入口逻辑（**26.4.0 已还原**） |
| `viewmodel/AccountManageViewModel.kt` | 移除添加账号相关 intent，保留会期续期（**26.4.0 已还原**） |
| `game/account/AccountUtils.kt` | `microsoftLogin` 移除已删除的状态参数（**26.4.0 补回 `isMicrosoftLogging`**） |
| `viewmodel/LauncherUpgradeViewModel.kt` | 更新体系改为 ZyNova GitHub Releases |
| `ui/upgrade/LauncherUpgradeDialog.kt` | 更新弹窗重写，自动匹配架构 |
| `path/UrlManager.kt` | 移除 ZL2 更新 URL，新增 ZyNova Releases / Discord |
| `setting/AllSettings.kt` | 新增 `glassLevel`、`lastIgnoredVersionName`；旧项保留兼容 |
| `setting/SettingsInitializer.kt` | 旧的液态玻璃开关一次性迁移 |
| `setting/enums/HomePageType.kt` | 新增 `Cards` |
| `viewmodel/HomePageViewModel.kt` | 新增 `HomePageState.Cards` |
| `ui/screens/content/LauncherScreen.kt` | 渲染卡片主页 + 事件接线 |
| `ui/screens/main/MainScreen.kt` | 卡片主页事件接线 |
| `ui/screens/content/elements/LauncherElements.kt` | 玻璃效果按档位应用（静态/动态绘制分离） |
| `ui/screens/content/settings/LauncherSettingsScreen.kt` | 玻璃效果改为两档单选（26.2.2 移除极致档的性能警告弹窗） |
| `ui/screens/content/settings/AboutInfoScreen.kt` | 署名 / GitHub / Discord / 上游作者下移 |
| `ui/screens/content/settings/RendererSettingsScreen.kt` | 移除基于设备声明的 Vulkan 判断 |
| `utils/device/VulkanCapabilities.kt` | 检测结果改为三态，按档案判定 |
| `viewmodel/VulkanCheckerViewModel.kt` | 检测失败不缓存 |
| `ui/vulkan_checker/VulkanChecker.kt` | 三态展示 + 重新检测 |
| `ui/vulkan_checker/VCOperation.kt` | 携带三态结果与版本 |
| `game/download/assets/_Download.QuickInstall.kt` | 改为调用统一资源核心 |
| `ui/screens/content/download/Download*Screen.kt` | 安装上下文传递 |
| `ui/screens/content/download/assets/**` | 快捷安装入口按上下文显隐 + 已安装状态 |
| `ui/screens/BackStackNavKey.kt` | 新增资源安装上下文 |
| `ZalithLauncher/gradle.properties` | 版本 26.1.0，主页链接指向 ZyNova |

### 26.2.x 涉及文件

| 文件 | 改动 |
|---|---|
| `viewmodel/ScreenBackStackViewModel.kt` | 新增 `resourceInstallTarget`，持有资源安装上下文 |
| `ui/screens/BackStackNavKey.kt` | 移除失效的 `@Transient installTargetVersion` 字段 |
| `ui/screens/content/DownloadScreen.kt` | `navigateToDownload` 写入上下文；4 个入口透传给页面 |
| `ui/screens/content/download/Download*Screen.kt` | 新增 `installTargetVersion` 参数；透传给搜索页 |
| `ui/screens/content/download/assets/search/Search*.kt` | 支持初始安装上下文；按实例 MC 版本初始化过滤条件 |
| `ui/screens/main/card_home/CardHomePage.kt` | 极致档下的卡片吸附与动态阴影 |
| `setting/enums/GlassLevel.kt` | 新增 `Extreme` 档位 |
| `ui/screens/content/settings/LauncherSettingsScreen.kt` | 极致档切换前的性能警告弹窗 |
| `game/download/assets/platform/SearchPlatform.kt` | 新增「所有」平台选项（聚合搜索） |
| `game/download/assets/platform/AggregatedSearchResult.kt` | 新增聚合搜索结果（合并多个来源） |
| `ui/screens/content/elements/LauncherElements.kt` | 极致档效果：`extremeGlassEffects()`、动态模糊半径，并使用 `RuntimeShader` 实现多重采样模糊与折射扭曲 |
| `ui/screens/content/download/assets/elements/_Search.Result.kt` | 搜索结果卡片增加快捷安装按钮 |
| `ui/screens/content/download/assets/elements/_Search.Filter.kt` | 平台选择支持「所有」 |
| `game/download/assets/_Download.QuickInstall.kt` | 新增 `quickInstallResource()`（从搜索结果快捷安装） |
| `game/home/HomeDataProvider.kt` | 改为按「版本模块」组织主页数据（`instances()`） |

### 26.2.2 涉及文件

| 文件 | 改动 |
|---|---|
| `setting/enums/GlassLevel.kt` | 由 `Off/Standard/Enhanced/Extreme` 简化为 `Off/On`，新增 `fromLegacyName()` |
| `setting/SettingsInitializer.kt` | 新增 `migrateLegacyGlassLevel()`：直接读原始字符串并把旧档位迁移为 `On` |
| `ui/screens/content/elements/LauncherElements.kt` | 删除 `extremeGlassEffects()` / `EXTREME_GLASS_SHADER` / 动态模糊半径 / 静态高光常量，只保留动态玻璃 |
| `ui/screens/content/settings/LauncherSettingsScreen.kt` | 移除极致档的性能警告弹窗 |
| `ui/screens/main/card_home/CardHomePage.kt` | 卡片吸附与阴影不再与「极致档」绑定，统一为轻量按压反馈 |
| `game/download/resources/ResourceInstallManager.kt` | 新增 `ResourceMatchException`（三态原因）与 `ResourceInstallPlan`；匹配失败改为抛异常；前置依赖解析失败会记录并收集 |
| `game/download/resources/ResourceManager.kt` | `matchVersion` / `installToInstance` 返回非空；安装前记录未解析的必需前置 |
| `game/download/resources/Resource.kt` | `supportsLoader` 归一化比较；`supportsGameVersion` 忽略空白与大小写；新增 `ResourceType.requiresLoader`、`normalizeLoaderName()` |
| `ui/screens/content/download/assets/search/SearchAssetsScreen.kt` | 新增 `buildFilter()`：单一来源与聚合搜索都按来源重新解析加载器；新增 `updatePlatform()` / `updateModloader()` / `currentModloader` |
| `game/download/assets/_Download.QuickInstall.kt` | 移除裸 `IllegalStateException`，改为本地化的 `toInstallMessage()`；成功后提示已安装的资源名 |
| `path/UrlManager.kt` | Discord 链接换成永久邀请，并补充维护说明 |

### 26.2.3 涉及文件

| 文件 | 改动 |
|---|---|
| `ui/screens/content/download/assets/search/SearchAssetsScreen.kt` | VM 新增 `enableModLoader` 参数（非模组类型不再附加加载器过滤）；新增 `supportedPlatforms` / `effectivePlatforms` / `categoryFilterAvailable`；「所有平台」只查询支持该类型的来源 |
| `game/download/assets/platform/AggregatedSearchResult.kt` | `totalPage` 由 `min` 改为 `max` 且至少为 1 |
| `game/download/assets/platform/modrinth/ModrinthAPI.kt` | `platformClasses.modrinth!!` 改为明确的 `UnsupportedClassesException` |
| `game/download/assets/platform/_PlatformSearch.kt` | 无 CurseForge API Key 时也启用 MCIM 镜像源（否则官方接口必然 403） |
| `game/download/resources/ResourceInstallManager.kt` | 存档解压后兜底清理残留的 `.zip` |
| `ZalithLauncher/build.gradle.kts` | `getKeyFromLocal` 把空字符串环境变量视为未配置，可回退到本地文件 / gradle 属性 |

### 26.2.4 涉及文件

| 文件 | 改动 |
|---|---|
| `ui/screens/main/card_home/CardHomePage.kt` | 卡片颜色 `cardColor(false)` → `cardColor()`（跟随「背景元素不透明度」）；新增 `scale` 参数链路（`InstanceModule` / `HomeGroup` / `HomeEntryChip`），按卡片大小缩放内边距、间距、字号与条目宽度 |
| `setting/AllSettings.kt` | 新增 `homeCardSize = intSetting("homeCardSize", 100, 70..140)` |
| `ui/screens/content/settings/LauncherSettingsScreen.kt` | 主页设置区新增「卡片大小」滑条，仅在主页类型为「卡片主页」时显示 |
| `res/values/strings.xml`、`res/values-zh-rCN/strings.xml` | 新增 `settings_launcher_home_card_size_title` / `_summary` |

### 26.2.5 涉及文件

| 文件 | 改动 |
|---|---|
| `ui/components/Reorder.kt` | **新增**：拖动排序基础设施（`ReorderState` / `rememberReorderState` / `Modifier.reorderItem`），基于「项屏幕范围 + 指针位置」，不依赖 LazyColumn |
| `game/home/HomeLayoutStore.kt` | **新增**：拖动后的顺序持久化（MMKV，按标识符保存） |
| `ui/screens/main/card_home/CardHomePage.kt` | 卡片重建为单层背景（去阴影 / 去缩放图层，点击用默认按压动画）；版本卡片与卡片内世界 / 服务器接入拖动排序 |
| `ui/screens/content/LauncherScreen.kt` | 右侧菜单由 `ConstraintLayout` 改为可拖动的 `Column`（`Arrangement.SpaceBetween` 保持原观感），三块各自接入拖动排序；版本下拉菜单的锚点逻辑保持不变 |
| `ZalithLauncher/gradle.properties` | 版本号 26.2.5 |

### 26.4.0 涉及文件

| 文件 | 改动 |
|---|---|
| `ui/screens/content/elements/AccountElements.kt` | **还原**：`MicrosoftLoginOperation` 状态机、`MicrosoftLoginTipDialog()`、`LoginMenuDialog` 的 `onMicrosoftLogin` 与「微软登录」项 |
| `ui/screens/content/AccountManageScreen.kt` | **还原**：`MicrosoftLoginOperation(...)` 渲染、私有 `MicrosoftLoginOperation` 组合函数、`onMicrosoftLogin` 接线 |
| `viewmodel/AccountManageViewModel.kt` | **还原**：`UpdateMicrosoftLoginOp`、`_microsoftLoginOp`、`LoginUiState.microsoftOp`、`PerformMicrosoftLogin`、共用私有 `startMicrosoftLogin()` |
| `game/account/AccountUtils.kt` | **还原**：`isMicrosoftLogging()` |
| `ui/screens/content/download/assets/search/SearchAssetsScreen.kt` | Issue #6：`allCategories` 始终取参照来源列表；`buildFilter()` 的类别条件只下发给参照来源；新增 `categorySourceName` 实参 |
| `ui/screens/content/download/assets/elements/_Search.Filter.kt` | Issue #6：新增可选参数 `categorySourceName`，标题按来源标注 |
| `ui/screens/content/download/assets/elements/_Search.Result.kt` | Issue #6：`ResultList` → `ResultProjectLayout` 补上漏传的 `classes = classes` |
| `ui/screens/content/settings/IronizedZinkConfigCards.kt` | Issue #7：删除 13 个单独参数控件，面板只剩预设卡（`CardPosition.Single`） |
| `res/values/strings.xml`、`res/values-zh-rCN/strings.xml` | 补回 11 条微软提示文案；新增 `download_assets_filter_category_with_source`；改写 3 条 Ironized Zink 文案 |
| `ZalithLauncher/gradle.properties` | 配置 `oauth_client_id`；版本号 26.4.0（`launcher_version_code=260400`） |

### 26.3.0 涉及文件

| 文件 | 改动 |
|---|---|
| `game/renderer/ironizedzink/IronizedZinkConfig.kt` | **新增**：Ironized Zink 配置模型（13 个参数 + 4 个官方预设 + `buildIronizedZinkEnv()`），移植自上游 `Presets.kt`，GPL-3.0 |
| `game/renderer/ironizedzink/IronizedZinkSettings.kt` | **新增**：从 `AllSettings` 读取参数、把预设整组写回 |
| `game/renderer/renderers/IronizedZinkRenderer.kt` | **新增**：Ironized Zink 内置渲染器 |
| `game/renderer/renderers/MobileGluesRenderer.kt` | **新增**：MobileGlues 内置渲染器 |
| `ui/screens/content/settings/IronizedZinkConfigCards.kt` | **新增**：选中 Ironized Zink 后展开的配置面板（**26.4.0 起只剩 4 个预设卡**） |
| `game/renderer/renderers/GL4ESRenderer.kt` | 仅补注释（保持默认配置不变） |
| `game/renderer/Renderers.kt` | 注册表改为只注册三个内置渲染器 |
| `game/launch/GameLauncher.kt` | `setRendererEnv()` 的通用兜底块排除 IRONIZED/MOBILEGLUES |
| `setting/AllSettings.kt` | `renderer` 默认值改为 Ironized Zink；新增 14 个 `ironizedZink*` 设置 |
| `ui/screens/content/settings/RendererSettingsScreen.kt` | 选中 Ironized Zink 时在下方插入配置面板 |
| `ui/components/Reorder.kt` | `onBounds()` 仅在拖动中处理位移（修复滚动时卡片异常移动） |
| `library/_Libraries.kt` | 移除 NG-GL4ES，Mesa 改完整许可集，新增 Ironized Zink / MobileGlues |
| `ZalithLauncher/build.gradle.kts` | 删除 ABI 拆分与 `-Darch` 资源裁剪，固定通用版本 |
| `.github/workflows/build.yml` | 去掉 ABI 矩阵，只构建通用包；注入 `GITHUB_TOKEN` |
| `.github/workflows/build_apk.yml` | 改为通用版本；注入 `GITHUB_TOKEN` |
| `.github/workflows/release_ci.yml` | 传递 `GH_TOKEN`；按通用包整理产物 |
| `THIRD_PARTY_NOTICES.md` | **新增**：逐组件许可证与版权声明 |
| `assets/licenses/` | **新增**：`THIRD_PARTY_NOTICES.md`、`ironized-zink-CREDITS.md` |
| `res/raw/` | **新增**：`ironized_zink_license.txt`、`mobileglues_license.txt`、`mesa_licenses.txt` |
| `jniLibs/*/libmobileglues.so` | **新增**：MobileGlues 2.0.0 官方 release 的未经修改二进制（4 ABI） |

### 26.2.6 涉及文件

| 文件 | 改动 |
|---|---|
| `setting/enums/ActionMenuSide.kt` | **新增**（搬自上游）：操作菜单停泊侧枚举 |
| `ui/screens/content/home/ActionMenuDrag.kt` | **新增**（搬自上游）：操作菜单长按拖动换边的状态与修饰符（动画规格改为显式 spring/tween） |
| `setting/AllSettings.kt` | 新增 `launcherActionMenuSide` |
| `ui/screens/content/LauncherScreen.kt` | 内容区与操作菜单改用 `BoxWithConstraints` 布局（停泊槽 + 绝对定位的操作菜单 + 让位位移）；右侧菜单恢复 `ConstraintLayout` 并加拖拽锚点 / 版本行排除区；移除内部三块排序 |
| `ui/screens/main/card_home/CardHomePage.kt` | 位移修饰符移到最外层（与背景一起移动） |
| `ui/components/Reorder.kt` | 让位动画自愈：无新位移时归零、拖动结束归零 |

### 已删除文件

| 文件 | 原因 |
|---|---|
| `upgrade/GithubContentApi.kt` | ZL2 更新链（GitHub Contents 解析） |
| `upgrade/RemoteData.kt` | ZL2 版本信息格式 |
| `upgrade/_RemoteData.LangTag.kt` | ZL2 多语言更新日志 / 网盘匹配 |
| `ui/upgrade/UpgradeFilesDialog.kt` | ZL2 多文件安装包选择对话框 |
| `utils/device/DeviceUtils.kt` | 基于设备声明的 Vulkan 判断（无使用者） |
| `game/renderer/renderers/NGGL4ESRenderer.kt` | 26.3.0 渲染器裁剪（Krypton Wrapper） |
| `game/renderer/renderers/KopperZinkRenderer.kt` | 26.3.0 渲染器裁剪（被 Ironized Zink 取代，UUID 复用） |
| `game/renderer/renderers/VirGLRenderer.kt` | 26.3.0 渲染器裁剪 |
| `game/renderer/renderers/FreedrenoRenderer.kt` | 26.3.0 渲染器裁剪 |
| `game/renderer/renderers/PanfrostRenderer.kt` | 26.3.0 渲染器裁剪 |
| `libs/NG-GL4ES-release.aar` | 仅 NG-GL4ES 使用，随渲染器一起移除 |
| `jniLibs/{arm64-v8a,armeabi-v7a,x86_64}/libOSMesa_8.so` | 仅被 Freedreno 渲染器使用（`x86` 本来就没有） |
| `jniLibs/{arm64-v8a,armeabi-v7a,x86_64}/libOSMesa_2121.so` | 仅被 VirGL 渲染器使用 |
| `jniLibs/{arm64-v8a,armeabi-v7a,x86_64}/libOSMesa_2300d.so` | 仅被 Panfrost 渲染器使用 |
| `jniLibs/{arm64-v8a,armeabi-v7a,x86_64}/libvirgl_test_server.so` | 仅被 VirGL 渲染器使用 |
| `jniLibs/{armeabi-v7a,x86_64}/libvirglrenderer_1.so` | 仅被 VirGL 渲染器使用 |
| `res/raw/ng_gl4es_license.txt` | 对应组件已不再分发 |
| `res/raw/mesa_license.txt` | 由 `mesa_licenses.txt`（Mesa 完整许可集）取代 |

> ⚠️ **注意**：`jniLibs/*/libvulkan_freedreno.so`（Turnip）**不是** Freedreno 渲染器专属，
> 它是「Vulkan 驱动器」的默认内置驱动（`driver_helper.c` 里作为兜底），**必须保留**。
> 同理 `libEGL_mesa.so` / `libglxshim.so` / `libglapi.so` / `libzink_dri.so` / `libcutils.so`
> 由 `libs/kopper-zink-release.aar` 提供，Ironized Zink 正在使用，**不要删**。

---

## 五、旧配置兼容（重要）

改动涉及用户配置时，必须保证**旧用户升级后仍能正常启动**。目前累计的处理方式：

| 旧配置 | 处理方式 |
|---|---|
| 数据库中已有的微软账号 | **保留** `AccountType.MICROSOFT` 与 `microsoftLogin`；26.1.0 只移除了「添加账号」入口，**26.4.0 已把该入口还原** |
| `liquidGlass`（布尔开关） | 保留定义，并在 `loadAllSettings` 中一次性迁移为 `glassLevel = On` |
| `glassLevel` 旧档位名（`Standard` / `Enhanced` / `Extreme`） | 26.2.2 起枚举只剩 `Off` / `On`；`loadAllSettings` 里的 `migrateLegacyGlassLevel()` **直接读原始字符串**并迁移为 `On`（**不能**先经过 `AllSettings.glassLevel` 读取，那样只会拿到默认值，用户原本的选择会丢失） |
| `lastIgnoredVersion`（整数） | 保留定义不再写入，新逻辑使用 `lastIgnoredVersionName`（字符串），避免存储类型冲突 |
| `homePageType` | 新增 `Cards` 枚举值，旧值 `Blank / FromLocal / FromURL` 语义不变 |
| `searchModPlatform` 等搜索平台 | 26.2.1 起新增 `SearchPlatform.ALL`，枚举名与旧 `Platform` 保持一致，旧值可直接反序列化 |
| `renderer`（全局渲染器） | 26.3.0 起默认值改为 Ironized Zink 的 UUID，**沿用原 Kopper Zink 的 UUID**：老用户存过 Kopper Zink 会自动落到 Ironized Zink；存过已删除渲染器（如 NG-GL4ES）的 UUID 时 `Renderers.setCurrentRenderer` 会回退到**列表首个**（即 Ironized Zink）并打日志，不会卡死 |
| `versionConfig.renderer`（按版本渲染器） | 同上：空字符串 / 已删除 UUID 都会回退到全局默认或列表首个 |
| `ironizedZink*`（14 个新设置） | 26.3.0 新增，默认值 = 上游 **Default** 预设。旧用户没有这些键 → 全部取默认值，行为与「Default 预设」一致；**不需要迁移** |

> **原则**：删除功能时可以不再写入旧配置，但要保留其定义，避免 MMKV 读取类型不匹配导致启动异常。
>
> **迁移枚举值时**：一定要**直接读底层存储的原始字符串**再做映射
> （`launcherMMKV().getString(key, null)`），不要用已经改过的枚举去读——
> 旧名字在新枚举里不存在，读取会静默回退成默认值，用户会以为自己的设置被重置了。

---

## 六、编译环境

### ⚠️ 本机（arm64 手机容器）无法完整编译 APK

- 本地缺少 Android SDK / NDK，且 NDK 的 clang 只有 x86_64 版（Google 不提供 arm64 Linux 版）
- **正确做法是用 GitHub Actions 编译**（云端是 x86_64，可正常编译）

### 本地能做的

- 改代码
- 静态检查（import 解析、字符串引用、残留引用、隐私扫描等）
- `git add` + `git commit` + `git push`

### 构建形态（26.3.0 起）

- **只产出一个通用版本 APK**（含 `arm64-v8a` / `armeabi-v7a` / `x86` / `x86_64`）
- **不再**传 `-Darch`：`projectArch` 固定为 `"all"`，按架构裁剪 JRE / LWJGL 的逻辑已移除
- Release 构建开启代码混淆：`isMinifyEnabled = true` + `isShrinkResources = true`，
  产物同时提供 `mapping.txt`（混淆映射）用于排查崩溃堆栈
- 本地命令：`./gradlew ZalithLauncher:assembleRelease`

### 本地 git 推送命令（低内存配置 + 不落盘 Token）

```bash
cd <ext4 工作副本>
git config pack.windowMemory 128m
git config pack.deltaCacheSize 64m
git config pack.threads 1
git config core.bigFileThreshold 1m

# 用一次性凭据助手推送：Token 只存在于当前这一条命令的环境变量里，
# 既不会进 .git/config，也不会出现在 remote URL 中
export GITHUB_TOKEN="<TOKEN>"
git -c credential.helper='!f(){ echo username=<用户名>; echo password="$GITHUB_TOKEN"; };f' push origin main
unset GITHUB_TOKEN
```

> - **推荐上面的方式**：它不会把 Token 写进 `.git/config`，收尾无需额外清理。
> - 另一种常见做法是把 Token 拼进 remote URL（`https://<user>:<token>@github.com/...`），
>   虽然**只会留在本地 `.git/config`、不会被提交**，但收尾时必须手动清掉：
>   `git remote set-url origin https://github.com/zzy89216-gif/ZyNova.git`
> - 无论用哪种方式，**Token 都不要写进任何文件或提交**。

---

## 七、GitHub Actions 编译流程

仓库中共有 3 个 workflow：

| 文件 | 触发 | 作用 |
|---|---|---|
| `.github/workflows/build_apk.yml` | push 到 `main` + 手动 `workflow_dispatch` | 通用版本验证编译（push 后最快的编译闸门） |
| `.github/workflows/build.yml` | 手动 / 被 `release_ci.yml` 调用 | 通用版本 Release 构建，上传 APK 与 `mapping.txt` |
| `.github/workflows/release_ci.yml` | Release 发布（`release: published`） | 调用 `build.yml`，打包 mapping 并自动上传全部产物 |

- 编译命令：`./gradlew ZalithLauncher:assembleRelease`（**不再传 `-Darch`**）
- 产物：`ZyNova-<版本>.apk`（通用）+ `mapping.universal.zip`

### CI 需要的 Secrets

都在 `Settings → Secrets and variables → Actions` 配置，workflow 里通过 `${{ secrets.XXX }}` 读取：

| Secret | 缺失后的影响 |
|---|---|
| `GH_TOKEN` | **GitHub API Token（可选）**。用于 CI 里调用 GitHub API；未配置时 workflow 回退到 Actions 内置 token（`github.token`）。**只允许 Secret / 环境变量注入，绝不写进源码、提交或 APK** |
| `KEY_PASSWORD` / `STORE_PASSWORD` | 签名口令，缺失会回退到 `gradle.properties` 里的上游公开默认值 |
| `OAUTH_CLIENT_ID` | **微软登录的 Client ID 覆盖项（可选）**。`gradle.properties` 里已有默认值，配置同名 Secret 即可覆盖（例如换一个 Azure 应用注册）。**注意这**不是**密钥**：公共客户端的 Client ID 本来就会打进 APK；**绝不要**创建 Client Secret |
| `CURSEFORGE_API_KEY` | **CurseForge 官方接口固定 403**，见第二节「后续待办 6」；26.2.3 起由 MCIM 镜像兜底 |

> ⚠️ 三个注意点：
> 1. **未配置的 Secret 会以空字符串注入环境变量**（不是 null），
>    构建脚本 `getKeyFromLocal` 必须把空字符串当作「未配置」处理，否则本地密钥文件不会生效；
>    `build.yml` 里的 `GITHUB_TOKEN: ${{ secrets.GH_TOKEN || github.token }}` 也依赖这一点
>    （空字符串在 GitHub 表达式里为假值，会回退到内置 token）；
> 2. **密钥值绝不能写进仓库里的任何文件**（包括本文档）；
> 3. 缺失 Secret **不会**让编译失败，只会让对应功能不可用 —— 排查功能问题时先确认这一点。
>
> 📌 **Token 命名约定**：`GH_TOKEN` 是仓库 Secret 名，`GITHUB_TOKEN` 是它在 job 里注入的环境变量名。
> 本地开发同样用 `GITHUB_TOKEN` 环境变量（见第六节推送命令与第十二节发布流程）。
> **不要**把 Token 加到 `buildKeys` / `BuildConfig` —— 那会被打进 APK。

### 查看编译结果（需要 Token）

```bash
TOKEN="<TOKEN>"
REPO="zzy89216-gif/ZyNova"

# 最近的运行
curl -s -H "Authorization: Bearer $TOKEN" \
  "https://api.github.com/repos/$REPO/actions/runs?per_page=5" \
  | python3 -c "import sys,json;d=json.load(sys.stdin);[print(r['id'],r['status'],r['conclusion'],r['head_sha'][:8]) for r in d['workflow_runs']]"

# 下载日志（zip）
curl -sL -H "Authorization: Bearer $TOKEN" \
  "https://api.github.com/repos/$REPO/actions/runs/<RUN_ID>/logs" -o runlogs.zip
```

编译错误形如 `e: file:///.../Xxx.kt:行:列 Unresolved reference 'Foo'`，可直接定位。

---

## 八、⚠️ 红线（绝对禁止）

1. **不要改 Pojav 后端、SDL、LWJGL 等核心渲染 / 运行库**（用户明确说过，改了启动器就废了）。
2. **不要硬编码任何隐私信息**：GitHub Token、签名密码、OAuth client id、CurseForge API key 等，**一律不能进代码或 git 历史**。
   - Token 只通过环境变量 / 凭据助手 / GitHub Secrets 传入，**不要写进 `git remote` 以外的任何文件**，
     更不要写进源码、文档、workflow（workflow 里必须用 `${{ secrets.XXX }}`）
   - 克隆 / 推送时即使把 Token 放进 `git remote` 的 URL，也只会留在本地 `.git/config`（不会被提交），
     但收尾时应当把它清掉：`git remote set-url origin https://github.com/zzy89216-gif/ZyNova.git`
   - `ZalithLauncher/gradle.properties` 里的 `default_store_password` / `default_key_password`
     是**上游公开**的默认签名口令（官方 debug 密钥本来就公开），属于刻意保留的项目资产，不要删；
     CI 会用 `KEY_PASSWORD` / `STORE_PASSWORD` Secrets 覆盖它们
   - **文档同样不能写密钥**：HANDOFF（本文）、CHANGELOG、README、Issue、Release 说明里
     只能描述「哪个 Secret 没配置」「应该去哪里配置」，
     **不能出现任何真实的 Token / API Key / 口令值** —— 即使是已经过期的也不行，
     因为对方很可能忘了吊销
   - **不要把 Token 直接发在对话 / Issue / 聊天记录里**：一旦发出就应当视为已经泄露。
     正确的做法是——只在本地用环境变量或凭据助手传一次，用完把本地副本删掉；
     如果确实发出去过，处理完请到 GitHub **吊销并重新生成**，不要继续沿用
   - 推送前做一次隐私扫描（见第十节）
3. **不要加阿里云镜像到 `settings.gradle.kts`**（会导致 GitHub 海外服务器编译失败，必须使用官方源）。
4. **不要改 namespace**（`com.movtery.zalithlauncher`），只改 `applicationId`（`com.zynova.launcher`）。
5. **GPL-3.0 合规**：
   - 保持开源
   - 保留上游版权声明（文件头的 `Copyright (C) 2025 MovTery`）
   - 新增文件使用 ZyNova 版权头
   - 分发时附 GPL-3.0 文本（`res/raw/gpl_3_license.txt`）
5.1 **第三方组件许可证合规（26.3.0 起加严）**：
   - **每个组件以其上游仓库里的实际 LICENSE 为准**，不要凭印象，也不要「因为某个依赖是 MIT 就认为整个项目是 MIT」：
     - 本项目自身 = **GPL-3.0**
     - Ironized Zink（GoyDevv）= **GPL-3.0**
     - Ironized Zink 承载的 Mesa / Zink / Kopper = **MIT**（GLX 部分 **SGI Free Software License B**，GL 头文件 **Khronos**）
     - MobileGlues = **LGPL-2.1**
     - GL4ES（gl4es_extra_extra）= **MIT**
   - **不删除上游作者信息**：任何保留文件的版权头、`CREDITS.md`、`NOTICE` 都不能删
   - **不把不同项目的许可证混为一谈**：`THIRD_PARTY_NOTICES.md` 里逐组件分开列出，
     应用内「关于 → 开源许可」也逐条分开
   - **重分发二进制要满足对应许可证**：
     - MobileGlues（LGPL-2.1）随包的是**未经修改**的 `libmobileglues.so`，
       必须在 `THIRD_PARTY_NOTICES.md` 里给出**对应源码的获取方式**，并说明可以替换该动态库
     - Mesa 栈（MIT）+ Ironized Zink（GPL-3.0）同样要保留许可证文本与来源说明
   - **移除组件时同步移除其声明与许可资源**（例如 26.3.0 删除 NG-GL4ES 后，
     `_Libraries.kt` 条目、`res/raw/ng_gl4es_license.txt`、AAR 一起删）
   - 新增 / 更换任何第三方库或预编译二进制时，**必须同步更新**：
     `THIRD_PARTY_NOTICES.md`、`assets/licenses/`、`res/raw/`、`library/_Libraries.kt`
6. **签名**：release 与 debug 都使用官方公开的 `zalith_launcher_debug.jks`（密码在 gradle.properties，官方本来就公开），不要生成新密钥硬编码密码。
7. **⛔ 绝对不要删除或修改仓库中的签名密钥文件**（维护者刻意保留的项目资产）：
   - `ZalithLauncher/zalith_launcher_debug.jks`
   - `ZalithLauncher/zalith_launcher.jks`
   - 后者虽然当前构建配置未引用，但**维护者是刻意保留它的**，
     **不要因为「未被引用」就把它当作无用文件清理掉**。
8. **不要移除正版（微软）登录入口**（**2026-09-28 由维护者明确要求，取代此前的相反规定**）：
   - 旧版本此处曾写「不要恢复正版登录入口」，该规定**已作废**。
     26.4.0 已把 `MicrosoftLoginOperation` 状态机、`MicrosoftLoginTipDialog`、
     `LoginMenuDialog` 的「微软账号」项与对应 ViewModel 接线**全部还原**；
     `microsoftLogin` / `MicrosoftAuthenticator` 是设备代码流的完整实现，**请勿删除或裁剪**。
   - 仍然保留的禁令：**不要恢复 ZL2 更新链、资源中心的一键安装入口、BBSMC**。
   - **不要创建 Client Secret**：设备代码流是公共客户端，带 Secret 反而会破坏登录。
     只会用到公开的 `OAUTH_CLIENT_ID`，它必须随包分发。
9. **不要为了「让预设成为唯一事实来源」而删除 `AllSettings` 里的 `ironizedZink*` 定义**：
   26.4.0 移除了面板上的 13 个参数控件，但 14 个设置项定义**必须保留**——
   `IronizedZinkSettings.kt` 仍在读写它们，删定义会导致 MMKV 读取类型不匹配。

---

## 九、常见坑（已踩过）

1. **git 在 sdcard 上崩溃**（SIGSEGV）→ 所有 git 操作在 ext4 工作副本中做。
2. **git push pack-objects 崩溃**（signal 11）→ 使用低内存配置（见第六节）。
3. **中文路径问题** → 必须设置 `LANG=C.UTF-8 LC_ALL=C.UTF-8`，否则 Java 报 `InvalidPathException`。
4. **GitHub Actions KSP 插件解析失败** → 是镜像源导致的，`settings.gradle.kts` 必须用官方源。
5. **fine-grained token 不能 push** → 要用 classic token（`ghp_` 开头），勾选 repo 权限。
6. **Kotlin 编译期易错点**（26.1.0 实际踩到）：
   - 修改函数签名后，**所有调用点的 lambda 参数个数都要同步**（如 `(A, B) -> Unit` 改成 `(A, B, C) -> Unit`）
   - 类内的**成员扩展函数无法从类外以 `obj.ext()` 形式调用**，要改成顶层扩展函数
   - `stringResource()` 是 `@Composable` 调用，**不能放在 `remember {}` 的 lambda 里**
   - 删除状态类型（如 `MicrosoftLoginOperation`）前，先全局搜索所有使用点
   - **函数可以和类型同名**：`AccountManageScreen.kt` 的私有 `MicrosoftLoginOperation(...)`
     与 `AccountElements.kt` 的 `MicrosoftLoginOperation` sealed interface 是两回事，
     类型位置解析到类型、调用位置解析到函数，这是 v2.5.1 的原有写法，**不要改动其中一个的名字**
7. **native 反射约束**：`VulkanCapabilities` 由 native 通过反射构造，**不能修改其构造参数列表**，只能增加方法 / 属性。
8. **不要把导航上下文放在 NavKey 上**（26.2.0 实际踩到并修复）：
   - NavKey 是 `@Serializable`，Navigation3 的 saveable 机制会序列化 / 反序列化 key
   - 因此 `@Transient` 字段在导航过程中会**静默丢失**（不报错，但读到 null）
   - 正确做法：把这类跨页面上下文放在 **ViewModel**（如 `ScreenBackStackViewModel`）上
9. **聚合搜索要处理「平台特有」的过滤条件**（26.2.1 实际踩到）：
   - `PlatformFilterCode`（资源类别）与加载器过滤器都是**来源特有**的，
     CurseForge 的类别 ID 传给 Modrinth 会匹配失败
   - 聚合多个来源时，必须为每个来源**重新解析**加载器，
     并且**绝不把某个来源的类别 ID 传给另一个来源**
   - ⚠️ **26.4.0 起的具体做法**：加载器按每个来源分别解析后照常下发；
     类别则**只下发给「参照来源」**（`referencePlatform`，「所有」时 = CurseForge），
     其余来源清空。界面上的类别列表也始终取参照来源的列表，
     并在标题标注来源（`categorySourceName`）。
     **不要**退回成「多来源时整个类别过滤器都清空」——那会让默认「所有平台」下没有类别可选
10. **新增函数参数不要加在 lambda 参数之后**（26.2.1 实际踩到）：
   - 若函数的最后一个参数是 lambda，调用方常用尾随 lambda 语法，
     把新参数追加到末尾会导致尾随 lambda 被解析成新参数，报 `Too many arguments`
   - 正确做法：把新参数插在 lambda 参数**之前**
11. **XML 中 `&` 必须转义为 `&amp;`**（26.2.1 实际踩到，会导致资源打包失败）：
   - 报错：`The entity name must immediately follow the '&' in the entity reference`
   - 新增字符串后建议用 XML 解析器批量校验全部资源文件
12. **Compose 滚动容器不可嵌套**（26.1.1 实际踩到并修复）：
   - `LazyColumn` 的 `item` 中**不能**再放 `Column(Modifier.verticalScroll(...))`
   - 会触发 `IllegalStateException: Vertically scrollable component was measured with an infinity maximum height constraints`
   - 放进 `LazyColumn` item 的组件应让外层负责滚动，自身只做 `fillMaxWidth()`；
     若确实需要滚动，应把滚动放在 `Dialog` / 固定高度容器等**有界约束**中
13. **`Modifier.graphicsLayer { renderEffect = ... }` 会把子节点（文字）一起模糊**（26.2.2 实际踩到并修复）：
   - 「⚠️极致」玻璃档用 `RenderEffect.createRuntimeShaderEffect(...)` 做多重采样模糊 + 折射，
     但 `renderEffect` 作用在整个图层上，**承载文字的图层被一起模糊**，
     用户看到的就是「字体明显模糊、文字渲染异常」
   - 结论：**不要在包含文字的容器上挂 `renderEffect`**；需要模糊背景时用 Haze 的
     `hazeBlur`（作用于背景源）或把模糊层单独放在文字下方
14. **过滤条件要区分「来源特有」**（26.2.2 实际踩到并修复）：
   - 加载器（`PlatformDisplayLabel`）与类别（`PlatformFilterCode`）是**来源特有**的类型，
     CurseForge 的枚举不能直接传给 Modrinth
   - 保存到 `PlatformSearchFilter` 里的加载器，必须在**每次搜索前**按目标来源重新解析；
     只在聚合搜索里解析、单一来源直接用原值，会让过滤条件退化成「不过滤」
   - 更稳的做法：**只保存加载器的名称**（String），需要时再按来源解析成对应的过滤器对象
15. **不要把「失败」统一收敛成 null**（26.2.2 实际踩到并修复）：
   - 版本匹配曾经把「网络查询失败 / 来源不支持该类型 / 实例信息读不出来 / 确实无兼容版本」
     全部变成 `null`，界面只能显示同一句未本地化的英文，无法定位问题
   - 应当用 `sealed class` 把失败原因拆开并抛出，UI 再按类型给出本地化提示
16. **Discord 必须使用永久邀请**（26.2.2 实际踩到并修复）：
   - 临时邀请会过期，Discord API 会返回 `{"message": "Invite is expired.", "code": 50270}`
   - 校验方式：`curl https://discord.com/api/v10/invites/<code>?with_counts=true`，
     返回中 `expires_at` 为 `null` 才是永久邀请
   - 更换链接时必须同步：`path/UrlManager.kt`、`README.md`、`README_ZH_CN.md`、
     `README_ZH_TW.md`、`README_JA_JP.md`、`HANDOFF.md`
17. **过滤条件还要区分「资源类型」**（26.2.3 实际踩到并修复）：
   - 加载器过滤器只对 **Mod / 整合包**有意义，资源包 / 光影 / 存档**不按加载器分类**
   - 把实例的加载器（如 Fabric）当作搜索条件下发，会让 Modrinth 光影变成 **0 条**、
     资源包只剩极少数被作者打了加载器标签的项目（实测 19275 → 4）
   - 结论：过滤条件要同时判断「来源是否支持该类型」与「该类型是否需要这个过滤条件」
18. **聚合分页不能取各来源的较小值**（26.2.3 实际踩到并修复）：
   - `totalPage` 取 `min` 时，只要有一个来源没有结果就会变成 0，
     界面显示「1 / 0」且**完全无法翻页**；应取 `max` 并至少为 1
19. **CI 未配置的 Secrets 是空字符串，不是 null**（26.2.3 实际踩到并修复）：
   - `System.getenv(KEY)` 会返回 `""`，只判断 `null` 会把它当成有效值，
     本地密钥文件 / gradle 属性里的兜底配置永远不会生效
   - 判断应写成 `System.getenv(KEY)?.takeIf { it.isNotBlank() }`
20. **CurseForge 官方接口缺 API Key 必然 403**（26.2.3 实际踩到并修复）：
   - MCIM 镜像（`mod.mcimirror.top`）不需要 Key，但此前只在中国大陆启用；
     一旦不在大陆且没有 Key，CurseForge 侧**永远搜不到任何资源**，
     而「存档」只有 CurseForge 提供 → 固定空白
   - 现在没有 Key 时始终保留镜像源；排查这类问题时先确认 Key 是否真的被打进包里
21. **想让 UI 跟随「背景元素不透明度」时，不要写死 `cardColor(false)`**（26.2.4 实际踩到并修复）：
   - `cardColor(influencedByBackground = true)`（默认）会在**设置了自定义背景**时把颜色替换成
     按「背景元素不透明度」降低 alpha 的版本；`false` 则永远返回原色
   - 卡片式主页当时写的是 `cardColor(false)`，于是自定义背景下卡片完全不透明，
     与其它页面（自定义主页卡片默认 `true`）表现不一致
   - 注意 `influencedByBackground(...)` 在**背景无效时会自动返回原色**，
     所以直接用默认值不会影响没有设置背景的用户
22. **新增 UI 缩放类设置时，默认值必须是「原样」**（26.2.4 的做法）：
   - 卡片大小用百分比（70~140，默认 100），100% 时所有 `dp` / 字号乘以 1f，
     观感与升级前完全一致，老用户不会被强制改变界面
   - 缩放要同时作用于**内边距、间距、字号、条目宽度**，
     只改其中一个会得到「卡片变大了但文字没变」的割裂效果

23. **卡片的多层叠加会被看成「边框」**（26.2.5 实际踩到并修复）：
   - 卡片同时使用 `Surface` 圆角填充 + `Modifier.graphicsLayer{scale}` + `shadowElevation` 时，
     内边距区与内容区叠加的层数不同，会出现「内容区偏亮、边缘一圈偏暗」的假边框
   - 想做出干净的卡片：**只保留一层** `clip(shape).background(color)`，
     不要叠加阴影/缩放图层；需要点击反馈就用默认的 `clickable` 指示
24. **在 `onGloballyPositioned` 里写 state 要判等**（26.2.5 实际踩到）：
   - 拖动排序需要在布局回调里记录每项的屏幕范围；
     如果无条件写入 `SnapshotStateMap`，会反复触发重组甚至形成布局循环
   - 正确写法：`if (old == rect) return` 再写入

25. **位移类修饰符必须放在最外层**（26.2.6 实际踩到并修复）：
   - `Modifier.clip().background().offset{}` 时，位移只作用于**内容**，背景留在原地，
     表现为「卡片背景不动、里面文字被顶下去」
   - 正确写法：`Modifier.offset{}`（或自定义的位移修饰符）放在 `clip` / `background` **之前**
26. **Animatable 的让位动画被打断会残留偏移**（26.2.6 实际踩到并修复）：
   - `LaunchedEffect(version)` 里 `snapTo(位移); animateTo(0f)`，一旦被新的布局变化取消，
     值会停在中间，并且不会再回到 0 → 界面永久错位
   - 解法：每次布局变化在「没有新位移」时强制 `snapTo(0f)`，动画结束与拖动结束也各归零一次
27. **上游已有的能力优先「搬」而不是自研**（26.2.6）：
   - 「操作菜单长按拖动换边」上游 ZalithLauncher2 已经实现得很完整
     （`ui/screens/content/home/ActionMenuDrag.kt`：提起跟手 + 中线预览 + 停泊让位 + 持久化）
   - 自研一套不仅费时，还容易像 26.2.5 那样引入新问题；确认上游有对应实现时直接搬过来适配

28. **`boundsInRoot()` 会把「滚动」算成「布局位移」**（26.3.0 实际踩到并修复）：
   - 拖动排序用 `onGloballyPositioned { it.boundsInRoot() }` 记录每一项的屏幕范围，
     再按 `old.top - rect.top` 判断「是否有项让位了」
   - 但**页面滚动会让所有项的 root 坐标一起变化**，于是每一项都被记上一笔让位位移，
     卡片在滚动时自己乱跑、位置对不上
   - 正确做法：**只有 `draggingKey != null`（正在拖动）时才处理位置变化**，
     非拖动状态下 `bounds` 照常更新，但**不计算也不播放位移**
   - 推论：任何用「root 坐标差」推断相对位移的逻辑，都要先确认「容器本身是否在滚动」

29. **渲染器的环境变量注入顺序**（26.3.0）：
   - `GameLauncher.setRendererEnv()` 的顺序是：
     `SDL_OPENGL_LIBRARY` → `getRendererEnv()` → `POJAVEXEC_EGL`/`SDL_EGL_LIBRARY`
     → `POJAV_RENDERER` → **通用兜底块** → `LIBGL_ES` 探测
   - 通用兜底块会写死 `MESA_GL_VERSION_OVERRIDE = 4.6` 等值，
     因此**凡是自带完整 Mesa 环境的内置渲染器都必须被排除**，
     否则用户在设置里选的 OpenGL 版本会被悄悄覆盖（表现为「改了没反应」）
   - 插件渲染器在兜底块之前就 `return` 了，所以历史上没暴露这个问题；内置渲染器则会踩到

30. **渲染器的「唯一标识符」是用户配置里的持久值**（26.3.0）：
   - `AllSettings.renderer`、`VersionConfig.renderer` 存的都是 `getUniqueIdentifier()`
   - **重命名渲染器时不要顺手改它的 UUID**，否则所有老用户的选择会失效并回退到列表首个
   - Ironized Zink 刻意沿用了 Kopper Zink 的 UUID，就是为了让老用户平滑过渡
   - 删除渲染器时不必做数据迁移：`Renderers.setCurrentRenderer()` 会回退到列表首个并打警告日志

31. **改动渲染器后要一起看的地方（自查清单）**：
   - `Renderers.kt` 的注册顺序（**列表首个就是所有回退路径的落点**）
   - `GameLauncher.setRendererEnv()` 的排除条件
   - `sdl_hook.c` 的 `sdlGlesCompatEnabled()` / `isMobileGluesEgl()`（按 `POJAV_RENDERER` 与
     `POJAVEXEC_EGL` 判断，改 ID 或 EGL 名要同步确认）
   - `jni/egl_bridge.c` 的 `pojavInitOpenGL()`（按 `POJAV_RENDERER` 前缀选 bridge）
   - `library/_Libraries.kt` 的许可条目
   - `VersionConfigScreen`（按版本渲染器列表）与 `RendererSettingsScreen`（全局）
   - 预编译库里该渲染器需要的 `.so` 是否真的在包内（`jniLibs/` 或 AAR）

32. **「UI 门控」和「请求条件」必须成对修改**（26.4.0 实际踩到并修复）：
   - 类别筛选器的可用性由两处共同决定：`SearchAssetsScreen` 传给 `SearchFilter` 的
     `allCategories`（列表内容）与 `buildFilter()` 里的 `categories`（实际下发的条件）
   - 只放开其中一处，就会得到「能选但不生效」的假筛选器，比原本的空白更糟
   - 判断「某个过滤器能不能用」时，要同时看**界面入口**和**请求侧过滤**两条路径

33. **不要把「某个门控让功能看起来坏掉了」当成设计**（26.4.0）：
   - 「所有平台」下类别为空，是因为 26.2.1 引入聚合搜索时顺手清空、26.2.3 只补了「存档」一个类型；
     而同一个面板的「模组加载器」一直可用 —— **同一屏内同类能力表现不一致，几乎总是疏漏**
   - 项目里已有 `referencePlatform`（「所有」时以 CurseForge 作为界面参照）这一现成机制，
     新功能应当复用它，而不是各自加一套门控

34. **设备代码流（device code flow）不需要 Redirect URI / SHA-1 / Client Secret**（26.4.0）：
   - 它属于 **OAuth 2.0 公共客户端**：Azure 应用注册只要开启
     「允许公共客户端流 / Allow public client flows」即可
   - 因此排查「微软登录失败」时**不要**去查重定向 URI 或签名指纹，先确认
     `BuildKeys.OAUTH_CLIENT_ID` 是否真的非空（未配置时是空字符串，请求会被直接拒绝）
   - Client ID 会随包分发，这是设计使然；**绝不要**为了「安全」而引入 Client Secret

---

## 十、给接手者的建议操作顺序

1. 读本文件 + `README.md`（英文默认；另有 `README_ZH_CN.md` / `README_ZH_TW.md` / `README_JA_JP.md`）
   + `CHANGELOG.md` + `LICENSE` + `THIRD_PARTY_NOTICES.md`。
2. 确认 GitHub 仓库状态（看 Actions 最新编译结果）。
3. 在 ext4 工作副本中改代码 → 静态检查 → commit → push。
4. 等 `build_apk.yml`（通用版本）验证编译通过 → 按第十二节发布 Release。
5. 开发新功能前，先读第八节「红线」和第二节「后续待办」。

### 推荐的静态检查（无本地编译时）

由于本机无法编译，推送前建议做以下静态检查：

- **import 符号解析**：确认没有引用已删除的符号
- **字符串引用完整性**：`R.string.xxx` 是否都在 XML 中定义（**默认语言与 `zh-rCN` 都要有**）
- **`R.raw` 引用完整性**：`R.raw.xxx` 是否都有对应文件（注意跨模块的 `net.burningtnt.terracotta.R.raw`）
- **未使用的 import**：清理
- **残留引用搜索**：搜索已删除功能的符号名
- **XML 良构性**：用 XML 解析器批量校验 `res/**/*.xml`（能提前发现漏转义的 `&`）
- **workflow YAML 可解析**：用 YAML 解析器过一遍 `.github/workflows/*.yml`
- **花括号平衡**：改大段代码后粗略核对 `{` / `}` 数量
- **渲染器自查**：见第九节第 31 条清单
- **隐私扫描**：确认没有 Token / 密钥 / 本地绝对路径（见下）

```bash
# 渲染器残留引用（应为空）
grep -rn "NGGL4ESRenderer\|KopperZinkRenderer\|VirGLRenderer\|FreedrenoRenderer\|PanfrostRenderer" \
  --include=*.kt --include=*.kts .

# 渲染器 libOSMesa 残留引用（应为空）
grep -rn "libOSMesa" --include=*.kt .
```

可直接复用的隐私扫描（在仓库根目录执行，排除 `.git`）：

```bash
# 常见密钥 / Token 模式
grep -rInaE "ghp_[A-Za-z0-9]{20,}|gho_[A-Za-z0-9]{20,}|ghu_[A-Za-z0-9]{20,}|ghs_[A-Za-z0-9]{20,}|github_pat_[A-Za-z0-9_]{20,}|sk-[A-Za-z0-9]{20,}|sk-ant-[A-Za-z0-9_-]{20,}|AKIA[0-9A-Z]{16}|xox[baprs]-|AIza[0-9A-Za-z_-]{30,}|-----BEGIN [A-Z ]*PRIVATE KEY-----" . --exclude-dir=.git

# 明文口令赋值
grep -rInE "(token|api[_-]?key|secret|password|passwd)\s*[:=]\s*[\"'][^\"']{8,}[\"']" . --exclude-dir=.git

# 本地绝对路径 / 设备路径（不允许写进仓库文档与源码）
grep -rInE "/root/|/home/[a-z]+/|/data/user/0/com\.|/data/data/com\.|[A-Z]:\\\\\\\\Users" \
  --include=*.kt --include=*.kts --include=*.md --include=*.yml --include=*.xml --include=*.properties . --exclude-dir=.git

# 手机号 / QQ 等个人信息
grep -rInE "\b1[3-9][0-9]{9}\b|\bQQ[:：]\s*[0-9]{5,12}\b" . --exclude-dir=.git

# 确认 workflow 只用 Secrets，没有明文
grep -rnE "secrets\.|password|api_key" .github/workflows/*.yml
```

**共享前必须确认**：仓库中没有 GitHub Token / 签名口令明文 /
OAuth client id / CurseForge API key / Cookie / Session / 本地绝对路径 / 个人联系方式；
发布 Release 前同样要对**产物清单**再核对一次（不要把日志、临时文件、凭据一起传上去）。
另外记得确认**没有把 Token 加进 `buildKeys` / `BuildConfig`** —— 那等于把它打进 APK。

---

## 十一、当前仓库信息

- 仓库：`zzy89216-gif/ZyNova`（public）
- 分支：`main`
- 最新版本：**26.4.0**
- 历史版本：26.3.0、26.2.6、26.2.5、26.2.4、26.2.3、26.2.2、26.2.1、26.2.0、26.1.1、26.1.0、v2.5.1、v2.5
- 更新日志：`CHANGELOG.md`
- 第三方声明：`THIRD_PARTY_NOTICES.md`
- 编译 workflow：
  - `build_apk.yml` —— push 到 `main` 时验证编译**通用版本**（已代码混淆）
  - `build.yml` —— 通用版本 Release 构建，上传 APK 与 `mapping.txt`
  - `release_ci.yml` —— 发布 Release 时自动调用 `build.yml`，打包 mapping 并上传全部产物

---

## 十二、发布 Release 的完整步骤

APK 编译与产物上传全部由 GitHub Actions 自动完成，**不需要手工下载 artifact 再上传**。

⚠️ **`<TOKEN>` 由用户临时提供、`<VERSION>` 替换为实际版本号，二者都不要写入任何文件**。
Token 只放在环境变量或临时凭据助手里，用完即弃（见第八节红线 2）。

### 推荐流程（26.2.2 / 26.2.3 / 26.3.0 都是这么发的）

```bash
TOKEN="$GITHUB_TOKEN"   # 只从环境变量读，不要把值写进任何文件
REPO="zzy89216-gif/ZyNova"

# 1. 改代码 + 版本号（ZalithLauncher/gradle.properties 的 launcher_version_name / _code）
#    并同步文档（CHANGELOG / HANDOFF / README ×3 / THIRD_PARTY_NOTICES），然后 push 到 main

# 2. 等 push 触发的通用版本验证编译跑完（这是最快的编译闸门，约 10~25 分钟）
curl -s -H "Authorization: Bearer $TOKEN" \
  "https://api.github.com/repos/$REPO/actions/runs?per_page=5" \
  | python3 -c "import sys,json;d=json.load(sys.stdin);[print(r['id'],r['name'],r['status'],r['conclusion'],r['head_sha'][:8]) for r in d['workflow_runs']]"

# 3. 用 Python 生成发布说明的 JSON（正文里有换行/引号/中文，必须走文件，不要塞进 -d）
python3 - <<'PY'
import json
body = open("release_notes.md", encoding="utf-8").read()   # 发布说明写在仓库外的临时文件
json.dump({"tag_name":"v<VERSION>","name":"ZyNova <VERSION>","body":body,
           "draft":False,"prerelease":False,"target_commitish":"main"},
          open("rel_payload.json","w",encoding="utf-8"))
PY

# 4. 创建 Release（published，非 draft）——这一步就会触发 release_ci.yml
curl -s -X POST -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  --data @rel_payload.json \
  "https://api.github.com/repos/$REPO/releases" \
  | python3 -c "import sys,json;d=json.load(sys.stdin);print(d['id'],d['html_url'])"

# 5. 等 release_ci：跑一次通用版本 Release 构建 + 一个 Upload-to-Release，约 10~25 分钟
#    完成后确认 Release 资产是 2 个（1 个 APK + 1 个 mapping.zip）
curl -s -H "Authorization: Bearer $TOKEN" "https://api.github.com/repos/$REPO/releases/tags/v<VERSION>" \
  | python3 -c "import sys,json;d=json.load(sys.stdin);a=d['assets'];print(len(a));[print(' -',x['name']) for x in sorted(a,key=lambda y:y['name'])]"

# 6. 收尾：删除本地工作副本（`.git/config` 里若有过带 Token 的 URL 也会一并消失）
```

> 之所以能自动上传：`release_ci.yml` 由 `release: published` 触发 →
> 调用 `build.yml` 构建**通用版本** → `Upload-to-Release` 用 `softprops/action-gh-release`
> 把 `./apks/*.apk` 与 `./mappings/*.zip` 一并附到该 Release 上。

### 备用流程（只在自动上传失败时用）

```bash
# 找最近一次成功的 run → 下载 artifact（zip）→ 解压得到 .apk
RUN_ID="<run id>"
curl -s -H "Authorization: Bearer $TOKEN" \
  "https://api.github.com/repos/$REPO/actions/runs/$RUN_ID/artifacts" \
  | python3 -c "import sys,json;d=json.load(sys.stdin);[print(a['id'],a['name']) for a in d['artifacts']]"
ARTIFACT_ID="<artifact id>"
curl -sL -H "Authorization: Bearer $TOKEN" \
  "https://api.github.com/repos/$REPO/actions/artifacts/$ARTIFACT_ID/zip" -o apk.zip
unzip apk.zip -d apk_dir/

# 手工上传到已有 Release（去掉 upload_url 里的 {?name,label}，加 ?name=xxx）
RELEASE_ID="<release id>"
curl -sL -X POST -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/vnd.android.package-archive" \
  --data-binary @apk_dir/ZyNova-<VERSION>.apk \
  "https://uploads.github.com/repos/$REPO/releases/$RELEASE_ID/assets?name=ZyNova-<VERSION>.apk"
```

### Release 应包含的产物

26.3.0 起**只有一个通用版本**：

- `ZyNova-<VERSION>.apk`（通用：`arm64-v8a` + `armeabi-v7a` + `x86` + `x86_64`，已代码混淆）
- `mapping.universal.zip`（混淆映射，用于还原崩溃堆栈）
  > 注意：workflow 里 artifact 名是 `mapping (universal)`、打包出的文件名是
  > `mapping (universal).zip`，但 GitHub 上传 Release 资产时会把空格与括号**净化为点号**，
  > 所以线上资产名固定是 `mapping.universal.zip`。

> 不再有 `-arm64-v8a` / `-armeabi-v7a` / `-x86` / `-x86_64` 之类的架构后缀产物。
> ⚠️ 大文件上传 / 下载可能中断，APK 下载可用 `curl -C -` 断点续传。

### ⚠️ 发布前的隐私复检（每次发布都要做）

1. **仓库侧**：按第十节的扫描命令过一遍工作副本，确认没有 Token / 密钥 / 个人信息。
2. **产物侧**：上传前先列出待上传清单 `ls -l`，确认里面**只有** APK 与 mapping，
   不要把编译日志、`.env`、凭据文件、临时脚本一起传上去。
3. **Token 侧**：Token 只在 shell 变量或环境变量里用，**不要写进任何文件**；
   收尾时把本地工作副本删掉即可让 `.git/config` 里的带 Token 的 remote URL 一并消失，
   也可以用 `git remote set-url origin https://github.com/zzy89216-gif/ZyNova.git` 清掉。
4. **Release 说明**：更新日志可以详细，但不要写入任何仅内部可见的信息（内网地址、密钥提示等）。
5. **第三方合规复检**：
   - 确认本版本新增 / 移除的组件，`THIRD_PARTY_NOTICES.md`、`assets/licenses/`、
     `res/raw/`、`library/_Libraries.kt` 四处**已同步**
   - 确认没有删除任何上游版权声明
   - 确认预编译二进制的来源与版本号写在 `THIRD_PARTY_NOTICES.md` 里（含源码获取方式）

---

**最后更新**：2026-09-28（26.4.0 已开发完成：恢复正版登录入口 + 修复 Issue #6 / #7）
