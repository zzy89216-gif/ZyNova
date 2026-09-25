# ZyNova 项目交接文档（Handoff）

> 本文档供任何接手者（人类或 AI）快速理解项目现状并继续开发。请先完整阅读再动手。

---

## 一、项目概述

- **项目名**：ZyNova（全名 ZyNova Launcher）
- **性质**：基于 [ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2) 开源代码开发的 **非官方修改版** Minecraft: Java Edition Android 启动器
- **许可证**：GPL-3.0（上游也是 GPL-3.0，ZyNova 必须保持开源）
- **GitHub 仓库**：<https://github.com/zzy89216-gif/ZyNova>（分支 `main`）
- **Discord**：<https://discord.gg/Tbn8Bqg2Yp>
- **包名**：`com.zynova.launcher`
- **namespace**：仍为 `com.movtery.zalithlauncher`，**不要改 namespace**，否则要改几百个文件的 package 声明
- **核心原则**：**Context First. Less Steps.**
  - 能自动判断，就不要让用户选择
  - 能一步完成，就不要拆成两步
  - 已有上下文，就直接使用上下文
  - 没有必要的按钮直接删除

---

## 二、当前版本与进度

**当前版本：26.1.1**（`launcher_version_code=260011`）

26.1.0 的核心目标是：**进一步脱离 ZalithLauncher2 的遗留逻辑，建立 ZyNova 自己的资源管理、下载、主页与 UI 基础。**

### 26.1.0 已完成 ✅

1. **移除正版登录入口**
   - 添加账号界面不再提供微软（正版）登录入口，只保留离线登录与第三方认证服务器
   - 移除了对应的 UI、点击逻辑、状态、无用资源与依赖
   - **保留已有微软账号的会话续期能力**（`AccountManageIntent.ReloginMicrosoft`），确保旧用户升级后仍可正常启动游戏

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
   - 设置中可选：默认主页 / 卡片主页 / 自定义主页
   - `HomeDataProvider` 作为主页统一数据访问接口，按需加载

8. **玻璃效果（Glass UI）三档**
   - `GlassLevel`：关闭 / 标准 / 增强，**默认关闭**
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

### 26.1.1 修复 ✅

- **修复卡片式主页导致的启动器崩溃**
  - 卡片主页内部使用了 `verticalScroll`，而它被放在 `LauncherScreen` 的 `LazyColumn` 的 `item` 中，
    造成滚动容器嵌套、高度约束无界，触发 `IllegalStateException` 崩溃
  - 修复方式：卡片主页不再自带滚动，统一由外层 `LazyColumn` 负责

### 后续待办 ⬜

1. **Boat 后端双端**：用户曾想接入 Boat 后端（像老版 FCL 一样），目前未开始。
2. **AI 助手**：接入 OpenAI V1 接口，自动加模组写配置，未开始。
3. **自定义主页数据接口**：`HomeDataProvider` 已可作为统一数据入口，可继续开放给自定义主页。
4. **键位优化、渲染优化**：用户提过，未深入做。
5. **继续收敛 ZL2 遗留逻辑**：资源系统已统一，其他模块仍可能残留上游耦合。

---

## 三、代码位置

| 位置 | 说明 |
|---|---|
| GitHub `main` | 唯一权威源码，所有改动以仓库为准 |
| 本地工作副本（ext4） | 因 sdcard 是 noexec/fuse 分区，git 与编译必须放在 ext4 分区执行 |

⚠️ **重要**：若在 `/sdcard`（fuse 分区）上执行 `git add` 或编译，会出现 SIGSEGV 崩溃。
**所有 git 操作与编译请在 ext4 分区的工作副本中进行。**

---

## 四、26.1.0 关键文件清单

### 新增文件

| 文件 | 作用 |
|---|---|
| `game/download/resources/Resource.kt` | 统一资源模型 + 平台模型适配 |
| `game/download/resources/ResourceProvider.kt` | Provider 接口、Modrinth/CurseForge 实现、注册表 |
| `game/download/resources/ResourceDownloadManager.kt` | 统一下载管理器 |
| `game/download/resources/ResourceInstallManager.kt` | 统一安装与安装状态判定 |
| `game/download/resources/ResourceManager.kt` | 资源管理核心统一流程入口 |
| `game/home/HomeDataProvider.kt` | 主页统一数据访问接口 |
| `ui/screens/main/card_home/CardHomePage.kt` | 卡片式主页 |
| `setting/enums/GlassLevel.kt` | 玻璃效果三档枚举 |
| `upgrade/ZyNovaRelease.kt` | ZyNova 自有更新体系数据模型 + ABI 自动挑选 |
| `utils/device/VulkanRequirement.kt` | Minecraft 的 Vulkan 要求档案 |
| `utils/device/VulkanCheckResult.kt` | Vulkan 三态检测结果模型 |

### 主要修改文件

| 文件 | 改动 |
|---|---|
| `ui/screens/content/elements/AccountElements.kt` | 移除微软登录入口 UI 与状态 |
| `ui/screens/content/AccountManageScreen.kt` | 移除首登录菜单的微软分支与入口逻辑 |
| `viewmodel/AccountManageViewModel.kt` | 移除添加账号相关 intent，保留会期续期 |
| `game/account/AccountUtils.kt` | `microsoftLogin` 移除已删除的状态参数 |
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
| `ui/screens/content/settings/LauncherSettingsScreen.kt` | 玻璃效果改为三档单选 |
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

### 已删除文件

| 文件 | 原因 |
|---|---|
| `upgrade/GithubContentApi.kt` | ZL2 更新链（GitHub Contents 解析） |
| `upgrade/RemoteData.kt` | ZL2 版本信息格式 |
| `upgrade/_RemoteData.LangTag.kt` | ZL2 多语言更新日志 / 网盘匹配 |
| `ui/upgrade/UpgradeFilesDialog.kt` | ZL2 多文件安装包选择对话框 |
| `utils/device/DeviceUtils.kt` | 基于设备声明的 Vulkan 判断（无使用者） |

---

## 五、旧配置兼容（重要）

改动涉及用户配置时，必须保证**旧用户升级后仍能正常启动**。26.1.0 的处理方式：

| 旧配置 | 处理方式 |
|---|---|
| 数据库中已有的微软账号 | **保留** `AccountType.MICROSOFT` 与 `microsoftLogin`，仅移除「添加账号」入口 |
| `liquidGlass`（布尔开关） | 保留定义，并在 `loadAllSettings` 中一次性迁移为 `glassLevel = Standard` |
| `lastIgnoredVersion`（整数） | 保留定义不再写入，新逻辑使用 `lastIgnoredVersionName`（字符串），避免存储类型冲突 |
| `homePageType` | 新增 `Cards` 枚举值，旧值 `Blank / FromLocal / FromURL` 语义不变 |

> **原则**：删除功能时可以不再写入旧配置，但要保留其定义，避免 MMKV 读取类型不匹配导致启动异常。

---

## 六、编译环境

### ⚠️ 本机（arm64 手机容器）无法完整编译 APK

- 本地缺少 Android SDK / NDK，且 NDK 的 clang 只有 x86_64 版（Google 不提供 arm64 Linux 版）
- **正确做法是用 GitHub Actions 编译**（云端是 x86_64，可正常编译）

### 本地能做的

- 改代码
- 静态检查（import 解析、字符串引用、残留引用、隐私扫描等）
- `git add` + `git commit` + `git push`

### 本地 git 推送命令（低内存配置，防止崩溃）

```bash
cd <ext4 工作副本>
git config pack.windowMemory 128m
git config pack.deltaCacheSize 64m
git config pack.threads 1
git config core.bigFileThreshold 1m
git push origin main
```

> 推送凭据请通过环境变量或凭据助手传入，**不要把 Token 写进任何文件或提交**。

---

## 七、GitHub Actions 编译流程

- Workflow 文件：`.github/workflows/build_apk.yml`
- 触发：push 到 `main` 分支 + 手动 `workflow_dispatch`
- 编译：`./gradlew ZalithLauncher:assembleRelease -Darch=arm64`
- 产物：arm64 Release APK

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
3. **不要加阿里云镜像到 `settings.gradle.kts`**（会导致 GitHub 海外服务器编译失败，必须使用官方源）。
4. **不要改 namespace**（`com.movtery.zalithlauncher`），只改 `applicationId`（`com.zynova.launcher`）。
5. **GPL-3.0 合规**：
   - 保持开源
   - 保留上游版权声明（文件头的 `Copyright (C) 2025 MovTery`）
   - 新增文件使用 ZyNova 版权头
   - 分发时附 GPL-3.0 文本（`res/raw/gpl_3_license.txt`）
6. **签名**：release 与 debug 都使用官方公开的 `zalith_launcher_debug.jks`（密码在 gradle.properties，官方本来就公开），不要生成新密钥硬编码密码。
7. **⛔ 绝对不要删除或修改仓库中的签名密钥文件**（维护者刻意保留的项目资产）：
   - `ZalithLauncher/zalith_launcher_debug.jks`
   - `ZalithLauncher/zalith_launcher.jks`
   - 后者虽然当前构建配置未引用，但**维护者是刻意保留它的**，
     **不要因为「未被引用」就把它当作无用文件清理掉**。
8. **不要恢复**：正版登录入口、ZL2 更新链、资源中心的一键安装入口、BBSMC。

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
7. **native 反射约束**：`VulkanCapabilities` 由 native 通过反射构造，**不能修改其构造参数列表**，只能增加方法 / 属性。
8. **Compose 滚动容器不可嵌套**（26.1.1 实际踩到并修复）：
   - `LazyColumn` 的 `item` 中**不能**再放 `Column(Modifier.verticalScroll(...))`
   - 会触发 `IllegalStateException: Vertically scrollable component was measured with an infinity maximum height constraints`
   - 放进 `LazyColumn` item 的组件应让外层负责滚动，自身只做 `fillMaxWidth()`；
     若确实需要滚动，应把滚动放在 `Dialog` / 固定高度容器等**有界约束**中

---

## 十、给接手者的建议操作顺序

1. 读本文件 + `README.md` + `CHANGELOG.md` + `LICENSE`。
2. 确认 GitHub 仓库状态（看 Actions 最新编译结果）。
3. 在 ext4 工作副本中改代码 → 静态检查 → commit → push。
4. 等 GitHub Actions 编译 → 下载 APK → 创建 Release。
5. 开发新功能前，先读第八节「红线」和第二节「后续待办」。

### 推荐的静态检查（无本地编译时）

由于本机无法编译，推送前建议做以下静态检查：

- **import 符号解析**：确认没有引用已删除的符号
- **字符串引用完整性**：`R.string.xxx` 是否都在 XML 中定义
- **未使用的 import**：清理
- **残留引用搜索**：搜索已删除功能的符号名
- **隐私扫描**：确认没有 Token / 密钥

---

## 十一、当前仓库信息

- 仓库：`zzy89216-gif/ZyNova`（public）
- 分支：`main`
- 最新版本：**26.1.0**
- 历史版本：v2.5.1、v2.5
- 更新日志：`CHANGELOG.md`
- 编译 workflow：`build_apk.yml`

---

## 十二、发布 Release 的完整步骤

APK 编译由 GitHub Actions 自动完成，发布 Release 使用 GitHub API。
（`<TOKEN>` 需用户提供，**不要写入任何文件**）

```bash
TOKEN="<TOKEN>"
REPO="zzy89216-gif/ZyNova"

# 1. 等编译完成后，找最新成功的 run id
curl -s -H "Authorization: Bearer $TOKEN" \
  "https://api.github.com/repos/$REPO/actions/runs?status=success&per_page=5" \
  | python3 -c "import sys,json;d=json.load(sys.stdin);[print(r['id'],r['name']) for r in d['workflow_runs']]"

# 2. 下载 APK artifact（zip 格式，需解压得到 .apk）
RUN_ID="<上面的 run id>"
curl -s -H "Authorization: Bearer $TOKEN" \
  "https://api.github.com/repos/$REPO/actions/runs/$RUN_ID/artifacts" \
  | python3 -c "import sys,json;d=json.load(sys.stdin);[print(a['id'],a['name']) for a in d['artifacts']]"
ARTIFACT_ID="<上面的 artifact id>"
curl -sL -H "Authorization: Bearer $TOKEN" \
  "https://api.github.com/repos/$REPO/actions/artifacts/$ARTIFACT_ID/zip" -o apk.zip
unzip apk.zip -d apk_dir/

# 3. 创建 Release（更新日志要写详细）
curl -s -X POST -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  "https://api.github.com/repos/$REPO/releases" \
  -d '{"tag_name":"v26.1.0","name":"ZyNova 26.1.0","body":"<发布说明>","draft":false,"prerelease":false}' \
  | python3 -c "import sys,json;d=json.load(sys.stdin);print(d['id'],d['upload_url'])"

# 4. 上传产物（去掉 upload_url 里的 {?name,label}，加 ?name=xxx）
RELEASE_ID="<上面的 release id>"
curl -sL -X POST -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/vnd.android.package-archive" \
  --data-binary @apk_dir/ZyNova-26.1.0-arm64-v8a.apk \
  "https://uploads.github.com/repos/$REPO/releases/$RELEASE_ID/assets?name=ZyNova-26.1.0-arm64-v8a.apk"
```

### Release 应包含的产物

- `ZyNova-26.1.0-arm64-v8a.apk`
- `ZyNova-26.1.0-armeabi-v7a.apk`
- `ZyNova-26.1.0-x86_64.apk`
- `ZyNova-26.1.0-x86.apk`
- `ZyNova-26.1.0.apk`（universal）
- mapping（防代码混淆）文件

> ⚠️ 大文件上传 / 下载可能中断，APK 下载可用 `curl -C -` 断点续传。

---

**最后更新**：2026-09-25（26.1.0 开发完成，等待编译与发布）
