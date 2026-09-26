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

**当前版本：26.2.4**（`launcher_version_code=260240`）

26.x 系列的核心目标是：**进一步脱离 ZalithLauncher2 的遗留逻辑，建立 ZyNova 自己的资源管理、下载、主页与 UI 基础。**

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
   - 已换成**永久邀请** `QwPpZQHrTa`，并同步到 README / README_EN_US / README_ZH_TW /
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

> 📌 **当前状态（2026-09-26 维护结束后）**：设备上**已不存在**本地工作副本
> （`/root/work/ZyNova`、`/root/ZyNova`、`/sdcard/Download/dsha工作区/ZyNova` 均已清理），
> 这是刻意的：本地副本只是临时工作区，权威源码只有 GitHub。
> 继续开发时重新 `git clone` 到 ext4 分区即可，收尾时再删掉。

---

## 四、关键文件清单

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
| `setting/enums/GlassLevel.kt` | 玻璃效果两档枚举（26.2.2 由三档简化） |
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

改动涉及用户配置时，必须保证**旧用户升级后仍能正常启动**。目前累计的处理方式：

| 旧配置 | 处理方式 |
|---|---|
| 数据库中已有的微软账号 | **保留** `AccountType.MICROSOFT` 与 `microsoftLogin`，仅移除「添加账号」入口 |
| `liquidGlass`（布尔开关） | 保留定义，并在 `loadAllSettings` 中一次性迁移为 `glassLevel = On` |
| `glassLevel` 旧档位名（`Standard` / `Enhanced` / `Extreme`） | 26.2.2 起枚举只剩 `Off` / `On`；`loadAllSettings` 里的 `migrateLegacyGlassLevel()` **直接读原始字符串**并迁移为 `On`（**不能**先经过 `AllSettings.glassLevel` 读取，那样只会拿到默认值，用户原本的选择会丢失） |
| `lastIgnoredVersion`（整数） | 保留定义不再写入，新逻辑使用 `lastIgnoredVersionName`（字符串），避免存储类型冲突 |
| `homePageType` | 新增 `Cards` 枚举值，旧值 `Blank / FromLocal / FromURL` 语义不变 |
| `searchModPlatform` 等搜索平台 | 26.2.1 起新增 `SearchPlatform.ALL`，枚举名与旧 `Platform` 保持一致，旧值可直接反序列化 |

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

### 本地 git 推送命令（低内存配置 + 不落盘 Token）

```bash
cd <ext4 工作副本>
git config pack.windowMemory 128m
git config pack.deltaCacheSize 64m
git config pack.threads 1
git config core.bigFileThreshold 1m

# 用一次性凭据助手推送：Token 只存在于当前这一条命令的环境变量里，
# 既不会进 .git/config，也不会出现在 remote URL 中
export GH_TOKEN="<TOKEN>"
git -c credential.helper='!f(){ echo username=<用户名>; echo password="$GH_TOKEN"; };f' push origin main
unset GH_TOKEN
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
| `.github/workflows/build_apk.yml` | push 到 `main` + 手动 `workflow_dispatch` | 单 ABI（arm64-v8a）验证编译 |
| `.github/workflows/build.yml` | 手动 / 被 `release_ci.yml` 调用 | 多 ABI 矩阵 `all`/`arm`/`arm64`/`x86`/`x86_64`，上传 `mapping.txt` |
| `.github/workflows/release_ci.yml` | Release 发布（`release: published`） | 调用 `build.yml`，打包 mapping 并自动上传全部产物 |

- 编译命令：`./gradlew ZalithLauncher:assembleRelease -Darch=<架构>`
- 产物：Release APK（按 `-Darch` 决定架构）+ 对应 `mapping.<架构>.zip`

### CI 需要的 Secrets

都在 `Settings → Secrets and variables → Actions` 配置，workflow 里通过 `${{ secrets.XXX }}` 读取：

| Secret | 缺失后的影响 |
|---|---|
| `KEY_PASSWORD` / `STORE_PASSWORD` | 签名口令，缺失会回退到 `gradle.properties` 里的上游公开默认值 |
| `OAUTH_CLIENT_ID` | 微软登录相关（本项目已移除正版登录入口，基本不影响） |
| `CURSEFORGE_API_KEY` | **CurseForge 官方接口固定 403**，见第二节「后续待办 6」；26.2.3 起由 MCIM 镜像兜底 |

> ⚠️ 三个注意点：
> 1. **未配置的 Secret 会以空字符串注入环境变量**（不是 null），
>    构建脚本 `getKeyFromLocal` 必须把空字符串当作「未配置」处理，否则本地密钥文件不会生效；
> 2. **密钥值绝不能写进仓库里的任何文件**（包括本文档）；
> 3. 缺失 Secret **不会**让编译失败，只会让对应功能不可用 —— 排查功能问题时先确认这一点。

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
8. **不要把导航上下文放在 NavKey 上**（26.2.0 实际踩到并修复）：
   - NavKey 是 `@Serializable`，Navigation3 的 saveable 机制会序列化 / 反序列化 key
   - 因此 `@Transient` 字段在导航过程中会**静默丢失**（不报错，但读到 null）
   - 正确做法：把这类跨页面上下文放在 **ViewModel**（如 `ScreenBackStackViewModel`）上
9. **聚合搜索要处理「平台特有」的过滤条件**（26.2.1 实际踩到）：
   - `PlatformFilterCode`（资源类别）与加载器过滤器都是**来源特有**的，
     CurseForge 的类别 ID 传给 Modrinth 会匹配失败
   - 聚合多个来源时，必须为每个来源**重新解析**加载器，并**清空**平台特有的类别条件
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
   - 更换链接时必须同步：`path/UrlManager.kt`、`README.md`、`README_EN_US.md`、
     `README_ZH_TW.md`、`HANDOFF.md`
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

---

## 十、给接手者的建议操作顺序

1. 读本文件 + `README.md` + `CHANGELOG.md` + `LICENSE`。
2. 确认 GitHub 仓库状态（看 Actions 最新编译结果）。
3. 在 ext4 工作副本中改代码 → 静态检查 → commit → push。
4. 等 `build_apk.yml`（arm64）验证编译通过 → 按第十二节发布 Release。
5. 开发新功能前，先读第八节「红线」和第二节「后续待办」。

### 推荐的静态检查（无本地编译时）

由于本机无法编译，推送前建议做以下静态检查：

- **import 符号解析**：确认没有引用已删除的符号
- **字符串引用完整性**：`R.string.xxx` 是否都在 XML 中定义
- **未使用的 import**：清理
- **残留引用搜索**：搜索已删除功能的符号名
- **XML 良构性**：用 XML 解析器批量校验 `res/**/*.xml`（能提前发现漏转义的 `&`）
- **花括号平衡**：改大段代码后粗略核对 `{` / `}` 数量
- **隐私扫描**：确认没有 Token / 密钥（见下）

可直接复用的隐私扫描（在仓库根目录执行，排除 `.git`）：

```bash
# 常见密钥 / Token 模式
grep -rInE "ghp_[A-Za-z0-9]{20,}|gho_[A-Za-z0-9]{20,}|github_pat_[A-Za-z0-9_]{20,}|sk-[A-Za-z0-9]{20,}|xox[baprs]-|AIza[0-9A-Za-z_-]{30,}|-----BEGIN [A-Z ]*PRIVATE KEY-----" . --exclude-dir=.git

# 明文口令赋值
grep -rInE "(token|api[_-]?key|secret|password|passwd)\s*[:=]\s*[\"'][^\"']{8,}[\"']" . --exclude-dir=.git

# 手机号 / QQ 等个人信息
grep -rInE "\b1[3-9][0-9]{9}\b|\bQQ[:：]\s*[0-9]{5,12}\b" . --exclude-dir=.git

# 确认 workflow 只用 Secrets，没有明文
grep -rnE "secrets\.|password|api_key" .github/workflows/*.yml
```

**共享前必须确认**：仓库中没有 GitHub Token / 签名口令明文 /
OAuth client id / CurseForge API key / 个人联系方式；
发布 Release 前同样要对**产物清单**再核对一次（不要把日志、临时文件、凭据一起传上去）。

---

## 十一、当前仓库信息

- 仓库：`zzy89216-gif/ZyNova`（public）
- 分支：`main`
- 最新版本：**26.2.4**
- 历史版本：26.2.3、26.2.2、26.2.1、26.2.0、26.1.1、26.1.0、v2.5.1、v2.5
- 更新日志：`CHANGELOG.md`
- 编译 workflow：
  - `build_apk.yml` —— push 到 `main` 时单 ABI（arm64-v8a）验证编译
  - `build.yml` —— 多 ABI 矩阵（`all`/`arm`/`arm64`/`x86`/`x86_64`），上传 `mapping.txt`
  - `release_ci.yml` —— 发布 Release 时自动调用 `build.yml`，打包 mapping 并上传全部产物

---

## 十二、发布 Release 的完整步骤

APK 编译与产物上传全部由 GitHub Actions 自动完成，**不需要手工下载 artifact 再上传**。

⚠️ **`<TOKEN>` 由用户临时提供、`<VERSION>` 替换为实际版本号，二者都不要写入任何文件**。
Token 只放在环境变量或临时凭据助手里，用完即弃（见第八节红线 2）。

### 推荐流程（26.2.2 / 26.2.3 都是这么发的）

```bash
TOKEN="<TOKEN>"
REPO="zzy89216-gif/ZyNova"

# 1. 改代码 + 版本号（ZalithLauncher/gradle.properties 的 launcher_version_name / _code）
#    并同步文档（CHANGELOG / HANDOFF / README），然后 push 到 main

# 2. 等 push 触发的 arm64 验证编译跑完（这是最快的编译闸门，约 10~25 分钟）
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

# 5. 等 release_ci：会跑 5 个架构 + 一个 Upload-to-Release，约 15~30 分钟
#    完成后确认 Release 资产是 10 个（5 个 APK + 5 个 mapping.<架构>.zip）
curl -s -H "Authorization: Bearer $TOKEN" "https://api.github.com/repos/$REPO/releases/tags/v<VERSION>" \
  | python3 -c "import sys,json;d=json.load(sys.stdin);a=d['assets'];print(len(a));[print(' -',x['name']) for x in sorted(a,key=lambda y:y['name'])]"

# 6. 收尾：删除本地工作副本（`.git/config` 里若有过带 Token 的 URL 也会一并消失）
```

> 之所以能自动上传：`release_ci.yml` 由 `release: published` 触发 →
> 调用 `build.yml` 跑 5 个架构 → `Upload-to-Release` 用 `softprops/action-gh-release`
> 把 `./apks/*.apk` 与 `./mappings/*.zip` 一并附到该 Release 上。

### 备用流程（只在自动上传失败时用）

```bash
# 找最近一次成功的多架构 run → 下载 artifact（zip）→ 解压得到 .apk
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
  --data-binary @apk_dir/ZyNova-<VERSION>-arm64-v8a.apk \
  "https://uploads.github.com/repos/$REPO/releases/$RELEASE_ID/assets?name=ZyNova-<VERSION>-arm64-v8a.apk"
```

### Release 应包含的产物

- `ZyNova-<VERSION>-arm64-v8a.apk`
- `ZyNova-<VERSION>-armeabi-v7a.apk`
- `ZyNova-<VERSION>-x86_64.apk`
- `ZyNova-<VERSION>-x86.apk`
- `ZyNova-<VERSION>.apk`（universal）
- mapping（防代码混淆）文件

> ⚠️ 大文件上传 / 下载可能中断，APK 下载可用 `curl -C -` 断点续传。

### ⚠️ 发布前的隐私复检（每次发布都要做）

1. **仓库侧**：按第十节的扫描命令过一遍工作副本，确认没有 Token / 密钥 / 个人信息。
2. **产物侧**：上传前先列出待上传清单 `ls -l`，确认里面**只有** APK 与 mapping，
   不要把编译日志、`.env`、凭据文件、临时脚本一起传上去。
3. **Token 侧**：Token 只在 shell 变量或环境变量里用，**不要写进任何文件**；
   收尾时把本地工作副本删掉即可让 `.git/config` 里的带 Token 的 remote URL 一并消失，
   也可以用 `git remote set-url origin https://github.com/zzy89216-gif/ZyNova.git` 清掉。
4. **Release 说明**：更新日志可以详细，但不要写入任何仅内部可见的信息（内网地址、密钥提示等）。

---

**最后更新**：2026-09-26（26.2.4 已发布）
