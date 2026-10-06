<p align="center">
  <img src="assets/icon.png" width="180" alt="ZyNova">
</p>

# ZyNova

> **Minecraft: Java Edition · Android Launcher**
>
> 基于 ZalithLauncher2 开源代码开发的独立非官方项目  
> **只用一部 Android 手机完成开发、测试与发布（编译交由 GitHub Actions 云端完成）**

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Primary-Kotlin-blue)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-GPL--3.0-orange)](https://www.gnu.org/licenses/gpl-3.0.html)
[![Release](https://img.shields.io/badge/Release-27.3.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.3.0)
[![Architecture](https://img.shields.io/badge/Architecture-Universal%20APK-red)](https://github.com/zzy89216-gif/ZyNova)

[English](README.md) | **[简体中文](README_ZH_CN.md)** | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md)

---

## ✦ 项目简介

**ZyNova** 是一个基于 [ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2) 开源代码深度开发的 **Minecraft: Java Edition Android 启动器**，由 ZyNova 项目维护者独立开发与维护。

> **目标很简单：打爆同行 100 年。（玩梗）**

> **ZyNova 并非 ZalithLauncher2 官方版本。**
>
> 它是一个基于上游开源代码进行深度修改、独立维护、独立发布的非官方项目。

当前版本 **27.3.0**。这一阶段做了三件事：
**认证入口向官方账号体系收敛、界面换代并保留可回退的旧版、修掉一个会改坏游戏 DNS 的缺陷。**

- **认证入口调整（议题 #17）**：默认隐藏「离线登录」与「第三方认证」入口，
  「添加账号」直接进入 Microsoft 正版登录；
  **只改入口，不删底层实现，也不清理任何已有账号数据**
- **「界面世代」切换（新版 / 旧版，随时双向可切）**：新版重做字阶与圆角体系，并改用标准动效
- **移除「强效动态玻璃」档与光敏性警告**：玻璃回到「关闭 / 启用动态玻璃」两档，
  原本使用强效档的用户会回退到「启用动态玻璃」，**不会被关掉**
- **修复**：切到非中文界面后，游戏的 `resolv.conf` 会误判成海外并把 DNS 换成 Cloudflare；
  现在改用与其它模块一致的时区判定

> 上一版（27.2.0）：图形 API 全自动、页面转场真正生效、无障碍选项「减少动态效果」。

> **在自有的资源管理与 UI 基础之上，把 ZyNova 的能力开放给 AI Agent——
> 让 AI 不只是「告诉你怎么操作」，而是能直接动手完成。**

围绕这一目标，项目已经建立了自己的：

- **渲染器体系**（内置 Ironized Zink / GL4ES / MobileGlues，默认 Ironized Zink）
- **Microsoft（正版）账号登录**（OAuth 2.0 设备代码流，无需 Client Secret）
- **全局 AI Agent**（Provider 可扩展 / 模型从服务商动态获取 / 流式对话 / 25 个可真正执行操作的工具 /
  **历史对话与侧边栏** / **不限制工具调用轮数**）
- 统一资源管理核心（Resource Management Core）
- 资源来源 Provider 体系（Modrinth / CurseForge）
- 统一下载管理器（队列 / 并发 / 续传 / 重试 / 校验 / 清理）
- 极简资源安装流程（上下文直装）
- 卡片式主页（以版本为模块，模块内展示世界与服务器）
- **自动图形 API 策略**（按版本发布时间判定，不写死版本号）
- **玻璃效果（Glass UI）两档**（关闭 / 启用动态玻璃）
- **「界面世代」切换**（新版 / 旧版，可随时双向切换；新版重做字阶与圆角体系）
- **无障碍动效控制**（减少动态效果：一键关闭转场、动态玻璃高光与骨架屏闪烁）
- 启动器自有更新体系（GitHub Releases）
- Minecraft **26.2-snapshot-1（数据版本 4883）** 起的 Vulkan 检测
- 单一通用版本发布（含全部 4 个 ABI + 代码混淆）

设计上遵循一条原则：

> **Context First. Less Steps.**
>
> 能自动判断，就不要让用户选择；能一步完成，就不要拆成两步；
> 已有上下文，就直接使用上下文；没有必要的按钮直接删除。

---

## 🌟 项目定位

ZyNova 面向 Android 平台。它的目标不是简单复制上游项目，而是在上游优秀开源项目的基础上，根据实际使用需求逐步形成属于 ZyNova 自身的功能、界面与使用体验。

随着开发推进，项目已经形成了独立的：

- 项目名称与应用身份
- 独立源码仓库与 Release
- 独立的资源管理与下载体系
- 独立的启动器更新体系
- 完整的 Android 工程与通用版本构建（含代码混淆）
- 开发流程、项目文档与交接体系

---

## ✨ 主要功能与修改

目前项目涉及的开发内容包括：

| 项目 | 状态 |
|---|:---:|
| Android Minecraft Java Edition 启动器 | ✅ |
| 用户界面重新设计 | ✅ |
| 交互逻辑调整 | ✅ |
| **全局 AI Agent**（主界面顶部入口，聊天即 Agent；25 个工具可**真正执行**读日志 / 管模组 / 改配置 / 装资源 / 启动游戏） | ✅ |
| **AI 配置独立于启动器设置**（Provider 可扩展 / API Key 仅存本机 / 模型动态获取不硬编码 / Base URL 可自定义 / 权限模式） | ✅ |
| **Microsoft（正版）账号登录**（设备代码流，支持添加账号与会话续期；应用注册已获 Mojang 白名单批准） | ✅ |
| **卡片式主页**（以版本为模块，模块内展示世界与服务器） | ✅ |
| **卡片式主页拖动排序**（版本卡片 / 世界 / 服务器 / 右侧菜单） | ✅ |
| **卡片大小可调**（70% ~ 140%） | ✅ |
| **渲染器体系重做**（内置只保留 Ironized Zink / GL4ES / MobileGlues） | ✅ |
| **Ironized Zink 官方预设**（Potato / Performance / Default / Max Compatibility，选中即展开） | ✅ |
| **统一资源管理核心**（Resource Management Core） | ✅ |
| **资源来源 Provider**（Modrinth / CurseForge） | ✅ |
| **统一下载管理器**（队列 / 并发 / 续传 / 重试 / 校验 / 清理） | ✅ |
| **极简资源安装**（上下文直装，不再重复选择） | ✅ |
| **玻璃效果两档**（关闭 / 启用动态玻璃） | ✅ |
| **「界面世代」切换**（新版 / 旧版，可随时双向切换） | ✅ |
| **认证入口调整**（默认隐藏离线 / 第三方认证，Microsoft 正版认证优先） | ✅ |
| **真实生效的页面转场**（回弹 / 弹跳 / 切入，全项目统一） | ✅ |
| **减少动态效果**（无障碍：一键关闭转场 / 动态玻璃高光 / 骨架屏闪烁） | ✅ |
| **图形 API 全自动**（首次启动 OpenGL，之后跟随游戏自身设置；按发布时间判定） | ✅ |
| **统一卡片按压反馈**（可点击卡片按下轻微缩放） | ✅ |
| **Minecraft 26.2-snapshot-1（数据版本 4883）Vulkan 检测与兼容判断** | ✅ |
| **按需加载与性能策略** | ✅ |
| **启动器自有更新体系**（GitHub Releases） | ✅ |
| Minecraft 启动相关功能 | ✅ |
| 游戏实例管理 | ✅ |
| Android 平台功能调整 | ✅ |
| **ZalithLauncher2 扩展生态兼容**（渲染器插件 / Vulkan 驱动插件 / 原生库插件） | ✅ |
| 单一通用版本 APK 构建（含代码混淆） | ✅ |
| GitHub Release | ✅ |
| 项目维护与交接文档 | ✅ |

具体功能会随着项目版本更新不断变化。

---

## ℹ️ 正版登录用的是哪个微软应用

**ZyNova 是 ZalithLauncher2 的非官方分支，两者是各自独立维护的不同项目。**

| | 显示名称 | Client ID | Mojang 允许名单 | 本版本是否使用 |
|---|---|---|---|---|
| **ZyNova 自己的** | ZyNova Launcher | `7b66e168-f8cd-43fc-a52d-2e78dba189b0` | ✅ **已获批准（2026-09-30）** | ✅ **正在使用** |
| **ZalithLauncher2 的** | ZalithLauncher（上游） | （已从本仓库移除） | ✅ 已获批准 | ❌ 不使用（26.4.0 / 26.4.1 曾使用） |

- **只有处于 Minecraft 应用程序允许名单中的应用注册**才能完成正版登录；
  否则 `POST /authentication/login_with_xbox` 会返回
  `403 Invalid app registration, see https://aka.ms/AppRegInfo`
- ✅ **ZyNova 自己的注册已在该名单中**：Mojang Enforcement 已于 **2026-09-30**
  完成 AppID 审核，**当前版本的正版登录可以正常完成**，也不再借用上游项目的注册
- 仍然**只需要「公共客户端 + Client ID」**：不需要 Client Secret、不需要 Redirect URI、不需要 SHA-1；
  Client ID 通过仓库 Secret `OAUTH_CLIENT_ID` **在构建时注入**，不写死在源码中

---

## 🎨 UI 与视觉体验

ZyNova 对原有启动器的界面与交互做了较大范围的重新设计，而不是简单改个名字：

- 全新界面布局与交互逻辑
- 动态毛玻璃效果、半透明与模糊
- Android 手机端适配与操作流程调整

### 动效与转场（27.2.0 起）

- **页面转场**：回弹（阻尼缩放）/ 弹跳 / 切入（横向滑入）三档**真实生效**
  （此前三档全都只是淡入淡出），全项目 15 处导航统一复用同一个实现，含文件管理器
- **交互动效**：可点击卡片按下轻微缩放并在抬起时回弹
- **启动体验**：启动页到主界面为淡入淡出，不再使用系统默认 Activity 动画

### 无障碍：减少动态效果

设置 → 启动器 → **减少动态效果**。开启后会一并关闭
页面转场、级联入场动画、动态玻璃的流动高光、骨架屏闪烁，
并把 Material 动效方案由 Expressive 降级为标准 —— 是「彻底不动」，而不是只把时长调短。

### 玻璃效果与界面世代

**玻璃效果**提供两档（27.3.0 起移除了带光敏风险的「强效」档与配套的启动警告）：

| 档位 | 说明 |
|---|---|
| **关闭**（默认） | 不叠加任何高光层，性能优先 |
| **启用动态玻璃** | 毛玻璃之上叠加缓慢流动的高光与折射光晕 |

- 动态玻璃只画渐变高光，**不碰 `renderEffect`**，所以不会像旧「极致」档那样模糊文字（issue #2）
- 开启「减少动态效果」后会退化为静态毛玻璃
- 原本使用已移除「强效」档的用户，升级后会**回退到「启用动态玻璃」，不会被关掉**

**界面世代**：设置 → 启动器 → **界面世代**，可选**新版 / 旧版**，而且**两个方向都能切**：

| | 新版（默认） | 旧版 |
|---|---|---|
| 字阶 | 标题 SemiBold、大字号收紧字距 | Material 默认 |
| 圆角 | 卡片 22dp / 对话框 30dp | 卡片 16dp / 对话框 28dp |
| 动效 | 标准动效（无回弹、无过冲） | Expressive |

### 图形 API（27.2.0 起全自动）

不再需要用户选择 OpenGL / Vulkan：

1. 某个实例**第一次启动**时使用 OpenGL（最保守、兼容性最好）
2. 之后**完全跟随 Minecraft 游戏自身**保存的设置，启动器不再覆盖
3. 每次启动游戏都会自动检查一次版本信息（含最新正式版与快照），失败不影响启动
4. 是否需要该选项由**版本发布时间**判断，**不写死版本号**
   （首个带 Vulkan 后端的版本为 Minecraft 26.2-snapshot-1，发布于 2026-04-07）

---

## 🧩 渲染器

26.3.0 起重做渲染器体系。**启动器内置的渲染器只保留三个**：

| 渲染器 | 说明 | 配置 |
|---|---|---|
| **Ironized Zink**（默认） | 桌面 OpenGL 经 Vulkan 转译（Mesa Zink + Kopper），作者 GoyDevv；默认 OpenGL 4.6 | **4 个官方预设** + OpenGL 版本下拉 |
| **GL4ES** | 经典 OpenGL 转译层 | 保持默认 |
| **MobileGlues** | 把桌面 OpenGL 转译到设备的 OpenGL ES 3.x | 保持上游默认 |

**Ironized Zink 的 4 个官方预设**（参数取值与上游一致）：

| 预设 | 定位 | Minecraft | 光影 |
|---|---|---|---|
| **Potato** | 绝对最高帧率 | 1.8 → 最新（含 26.x） | 不推荐 |
| **Performance** | 高帧率 + Sodium | 1.20.x → 最新 | 轻量 |
| **Default** | 均衡 Zink + 光影（**默认**） | 1.16.x → 最新 | 完整（Iris / OptiFine） |
| **Max Compatibility** | 什么都能跑 | 全部版本 | 完整 + 重度光影包 |

**26.4.0 起，面板只提供上面 4 个预设**：12 个单独参数开关已被移除，
普通用户不再需要手动调整底层参数。选择预设仍会一次性写入整组参数。

> ℹ️ 4 个预设的 OpenGL 版本都是 **4.6**。如果驱动对 Vulkan→Zink 的 4.6 转译不完整
> （画面异常或进不去），可以在面板下方的「**OpenGL 版本**」下拉里降到 4.5 / 4.3 / 3.3
> —— 该下拉在 26.4.0 曾被移除，**27.1.2 起已恢复**。
> 改动此项只影响 OpenGL 版本，不动其它预设参数。

在 **设置 → 渲染器** 里选中 Ironized Zink 后，**下方会立刻展开预设面板**。

#### 官方内置 与 外部扩展 的区别

26.3.0 删除的是**启动器官方内置**的其余 Renderer，**没有**移除外部 Renderer Plugin 机制：

| 类别 | 内容 | 26.3.0 状态 |
|---|---|---|
| **官方内置** | Ironized Zink（默认）、GL4ES、MobileGlues | 只保留这 3 个；其余内置 Renderer（Krypton Wrapper / Kopper Zink / VirGL / Freedreno / Panfrost）已删除 |
| **外部扩展** | FCL / ZalithLauncher 渲染器插件、新一代 `fclPlugin_V2` 渲染器插件、Vulkan 驱动插件、原生库插件 | **完全保留**，安装后仍会正常出现在渲染器列表中 |

外置插件自带自己的原生库，不受本次内置裁剪影响；
设置页的「下载渲染器插件」入口也仍然保留。

## 🔄 与 ZalithLauncher2 共存

ZyNova 使用独立的应用名称以及独立的应用签名。

因此，在 Android 系统及设备环境允许的情况下，ZyNova 可以与 ZalithLauncher2 同时安装并共存。

| 项目 | 状态 |
|---|:---:|
| ZalithLauncher2 单独安装 | ✅ |
| ZyNova 单独安装 | ✅ |
| ZalithLauncher2 + ZyNova 同时安装 | ✅ |
| 不删除 ZyNova 使用 ZalithLauncher2 | ✅ |
| 不删除 ZalithLauncher2 使用 ZyNova | ✅ |

用户可以根据自己的需求自由选择使用哪个启动器。

如果之后希望重新使用 ZalithLauncher2，也可以重新下载安装原项目。

> ℹ️ **注意：安装可以共存，而正版登录使用的是 ZyNova 自己的应用注册。**
> ZyNova 是 ZalithLauncher2 的非官方分支，两者是**不同项目**；
> 从 26.4.2 起，正版登录改用 **ZyNova 自己申请、并已获 Mojang 允许名单批准的**
> Microsoft 应用注册（见上方「正版登录用的是哪个微软应用」）。
> 因此在微软账号的「已连接的应用」中看到的是 **ZyNova Launcher**，而不是 ZalithLauncher。

---

## 🧬 技术栈

ZyNova 是一个完整的 Android 软件工程，而不仅仅是一个 APK 文件。

| 技术 | 用途 |
|---|---|
| Kotlin / Java | Android 主要开发语言 |
| C / C++ + NDK | Native 相关部分 |
| Gradle | 项目构建 |
| Git / GitHub | 版本管理与项目管理 |
| GitHub Actions | 云端构建与自动化流程 |
| Android | 主要目标平台 |

按仓库语言统计，大致为 **Kotlin 约 64% / Java 约 23% / C 约 13%**；
项目仍在持续开发，实际比例会随版本变化。

---

## 📱 通用版本

26.3.0 起，**Release 只提供一个通用版本 APK**，不再按架构拆分。

| 项 | 说明 |
|---|---|
| 覆盖架构 | `arm64-v8a`、`armeabi-v7a`、`x86`、`x86_64`（一个包全部包含） |
| 代码混淆 | Release 开启（`isMinifyEnabled` + `isShrinkResources`） |
| 产物名 | `ZyNova-<版本>.apk` |
| 混淆映射 | `mapping.universal.zip`，用于还原崩溃堆栈 |

为什么只出一个通用版本：

- 用户**不需要判断自己的设备是什么架构**，下载唯一的安装包即可
- 混淆映射只需要维护一份，排查线上崩溃更简单
- 代价是包体包含全部 4 个 ABI 的预编译库，比单架构包大

一次 Release 只上传 **2 个产物**：

| 产物 | 内容 |
|---|---|
| `ZyNova-<版本>.apk` | 通用版本安装包（已代码混淆） |
| `mapping.universal.zip` | 混淆映射，用于还原崩溃堆栈 |

> GitHub 上传 Release 资产时会把空格与括号净化为点号，
> 因此线上资产名固定是 `mapping.universal.zip`（CI 内部 artifact 名为 `mapping (universal)`）。

---

## 🚀 Release

当前版本：

**ZyNova 27.3.0**

27.3.0 处理了仓库里的开放议题 **#17**（认证入口调整），并完成了一次界面体系升级：

- **🔄 认证入口调整**（#17）：默认隐藏「离线登录」与「第三方认证」入口，
  「添加账号」直接进入 Microsoft 正版登录；
  这是**可逆的 UI 层开关** —— 不删底层实现，也不清理任何已有账号数据
- **✨ 「界面世代」切换**：新版（默认，重做字阶与圆角体系 + 标准动效）/
  旧版（与 27.3.0 之前完全一致），**两个方向都能切**
- **🗑️ 移除「强效动态玻璃」档与光敏性警告**：玻璃回到「关闭 / 启用动态玻璃」两档；
  原本使用强效档的用户会回退到「启用动态玻璃」，**不会被关掉**
- **🐛 修复**：切到非中文界面会被误判成海外，游戏的 DNS 被换成 Cloudflare；
  改用与其它模块一致的时区判定
- **✨ 关于页新增 ZyNova 自己的赞助入口**；四语言 README 末尾新增赞助版块

> 📜 更早版本的完整变更记录见 [CHANGELOG.md](CHANGELOG.md)。

## 🛠️ 开发与测试流程

ZyNova 的开发并不只在源码层面完成，实际流程是：

> 需求 → 源码分析 → 代码修改（在 Android 手机上）→ 云端构建与 APK 打包（GitHub Actions）
> → 真机安装 → 实际测试 → 发现 Bug → 修复 → 重新构建 → 再次测试 → Release 发布

项目中的部分功能是在实际 Android 设备使用过程中进行测试和调整的。

---

## 📱 用手机开发的项目

> **从写代码、测试到发布，全部只用一部 Android 手机操作，没有使用电脑。**

只有 **APK 编译不在手机上完成**：Android 环境缺少完整的 SDK / NDK 工具链
（NDK 的 clang 只有 x86_64 版本，Google 不提供 arm64 Linux 版），
因此编译交给 **GitHub Actions 云端服务器** 执行。

| 环节 | 在哪里完成 |
|---|---|
| 源码编写 / 工程配置 / Git 操作 / 文档维护 / 真机测试 / Release 发布 | Android 手机 |
| **APK 编译（通用版本 + 代码混淆）** | **GitHub Actions 云端** |

> 手机开发 → 云端构建 → 手机测试 → 手机发布

---

## 🤖 AI Agent 辅助开发

> ℹ️ 本节说的是**开发 ZyNova 时**使用的 AI Agent（工程辅助）。
> 启动器**内置的** AI Agent 功能是另一回事，见 [主要功能与修改](#-主要功能与修改)。

ZyNova 的开发过程中大量使用 AI Agent 辅助实际工程工作，包括：
源码与工程结构分析、代码修改与功能开发、构建错误分析、Bug 修复、
APK 构建与通用版本打包、文件整理、GitHub 与 Release 操作、项目文档与交接文档编写。

AI Agent 只是辅助工具：**需求、开发方向、测试结果与最终发布由项目维护者决定。**

---

## 📚 项目文档与交接

为了避免项目未来完全依赖某一次开发过程或者某一个 AI 对话，ZyNova 建立了相应的项目维护与交接资料。

相关资料用于记录：

- 项目结构
- 当前项目状态
- 构建方式
- 主要修改内容
- 已知问题
- 维护方式
- 后续开发方向
- 未来开发者或 AI 如何继续参与项目

这样即使原有开发环境或者 AI 对话上下文不存在，未来仍然可以通过公开源码和项目文档继续了解和维护 ZyNova。

---

## 🏗️ 项目结构

仓库同时承担 **源码仓库 + 开发仓库 + 文档仓库 + Release 发布仓库**：

| 内容 | 位置 |
|---|---|
| Android 工程（Kotlin / Java / Native / Gradle 模块） | 仓库根目录与 `ZalithLauncher/` |
| 四语言说明 | `README.md` / `README_ZH_CN.md` / `README_ZH_TW.md` / `README_JA_JP.md` |
| 更新记录 | `CHANGELOG.md` |
| 第三方许可证与版权声明 | `THIRD_PARTY_NOTICES.md` |
| 维护与交接资料 | `HANDOFF.md` |
| 许可证 | `LICENSE`（GPL-3.0） |
| Release 与混淆映射 | GitHub Releases |

---

## 👥 项目维护者

目前 ZyNova 的核心开发由：

**zzy**

以及：

**ChalkyDuke_pwp**

共同参与。

项目的具体代码、功能、界面、版本以及后续发展由 ZyNova 项目维护者负责。

---

## 🌱 项目维护理念

ZyNova 不追求为了保持更新而强行加入大量功能。

| 情况 | 处理方式 |
|---|---|
| 🐛 发现 Bug | 修复 |
| 💡 有新的实际需求 | 视情况增加功能 |
| 🔧 有值得改进的地方 | 进行调整 |
| 😴 没有新的需求 | 保持当前状态 |

让 ZyNova 按照实际需求自然发展。

---

## 🗺️ Roadmap

### 27.3.0（当前版本）

- [x] **认证入口调整**：默认隐藏离线 / 第三方认证入口，Microsoft 正版认证优先（#17）
- [x] **「界面世代」切换**：新版 / 旧版，可随时双向切换
- [x] **新版设计令牌层**：字阶（SemiBold 标题 + 负字距）、圆角体系（卡片 22dp）、标准动效
- [x] **移除「强效动态玻璃」档与光敏性警告**（旧配置迁移：强效 → 启用动态玻璃）
- [x] **修复启动链路用「应用语言」判断地区，导致切语言后游戏 DNS 被换掉**
- [x] **关于页新增 ZyNova 自己的赞助入口**；四语言 README 新增赞助版块
- [ ] 界面世代的后续：**逐屏重做布局与间距**、整体视觉语言统一、per-page 差异化转场、
      把剩余动画收敛到统一动效入口、读取系统级「移除动画」设置
- [ ] 评估 `Launcher.kt` 的 `user.country` 与 `LanguageHelper` 对 `Locale.getDefault()` 的依赖
      （会实际影响游戏 JVM 的 locale，需要单独验证后再改）

### 27.2.0

- [x] **页面转场真实生效**（回弹 / 弹跳 / 切入），15 处导航统一（#15）
- [x] **减少动态效果**（无障碍总开关：转场 / 级联入场 / 动态玻璃高光 / 骨架屏闪烁）（#15）
- [x] **强效动态玻璃档位 + 启动光敏性警告**（可在设置中关闭）（#15）
- [x] **统一卡片按压反馈**（#15）
- [x] **启动页 → 主界面淡入淡出**（#15）
- [x] **图形 API 全自动**（移除手动选择，按版本发布时间判定）（#16）
- [x] **修复 AI 图标点击无反馈**（#14）
- [ ] 议题 #15 的其余部分：启动流程重新设计、整体视觉语言统一、
      per-page 差异化转场、把剩余动画收敛到统一动效入口、读取系统级「移除动画」设置

### 27.1.2

- [x] **移除上游 Client ID 并改写 Git 历史**（3 个提交清除，全对象扫描 0 命中）
- [x] AI 配置页首次打开时的**数据流向说明**
- [x] Agent **写操作审计日志**（可查看 / 导出 / 清空）
- [x] Agent 权限模式**默认值改为「操作确认」**
- [x] **恢复「OpenGL 版本」下拉**（4.6 / 4.5 / 4.3 / 3.3）
- [x] 修复「操作确认」弹窗会盖在其它页面上的问题

> 📜 更早版本的路线图记录见 [CHANGELOG.md](CHANGELOG.md)。

## 💬 社区与反馈

| 渠道 | 地址 |
|---|---|
| GitHub 仓库 | <https://github.com/zzy89216-gif/ZyNova> |
| GitHub Issues（Bug / 建议） | <https://github.com/zzy89216-gif/ZyNova/issues> |
| Discord 服务器 | <https://discord.gg/QwPpZQHrTa> |
| Releases（下载） | <https://github.com/zzy89216-gif/ZyNova/releases> |

> ⚠️ Discord 使用的是**永久邀请链接**。如果该链接失效，说明服务器更换了邀请，
> 请以 GitHub 仓库首页与启动器「关于」页面中的链接为准，并到 Issues 反馈。

---

## 🔗 上游项目

ZyNova 基于：

**ZalithLauncher2**

官方项目仓库：

https://github.com/ZalithLauncher/ZalithLauncher2

感谢 ZalithLauncher2 项目以及所有贡献者提供的：

- 开源代码
- 技术基础
- 项目经验
- 相关工作

ZyNova 的开发离不开上游项目所提供的基础。

---

## 🧬 内置渲染器的上游

26.3.0 起内置的三个渲染器分别来自这些上游项目（**各自适用各自的许可证**）：

| 渲染器 | 上游项目 | 该渲染器自身代码的许可证 |
|---|---|---|
| **Ironized Zink** | https://github.com/GoyDevv/IronizedZink | **GPL-3.0** |
| **MobileGlues** | https://github.com/MobileGL-Dev/MobileGlues | **LGPL-2.1** |
| **GL4ES** | https://github.com/PojavLauncherTeam/gl4es_extra_extra | **MIT** |

Ironized Zink 本身是一个「打包 + 配置」项目；真正的渲染引擎来自
**Mesa**（https://mesa3d.org/）：

- **Zink**（Mesa 的 Gallium 驱动）与 **Kopper**（Mesa 的 Vulkan 窗口系统集成层）
  都是 **Mesa 代码库的一部分**，并不是独立的第三方项目
- Mesa 内部各组件许可证**并不相同**：主 Mesa 代码 / Gallium 代码为 **MIT**，
  GLX 客户端代码为 **SGI Free Software License B**，
  GL / GLX 扩展头文件为 **Khronos**，C11 线程模拟为 **Boost（宽松许可）**
- 随启动器分发的 Zink / Kopper 预编译二进制来自
  **AngelAuraMC 的 `mesa_zink_kopper` 构建**，以**未经修改**的形式随包分发

完整的逐组件许可证、版权归属、预编译二进制来源与对应源码获取方式，见
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。

---

## ⚠️ 与 ZalithLauncher2 的关系

**ZyNova 并非 ZalithLauncher2 官方版本。**

ZyNova 是基于 ZalithLauncher2 开源代码进行开发的非官方修改项目。

本项目与 ZalithLauncher2 官方项目不存在官方合作、授权或隶属关系，除非另有明确说明。

ZyNova 的具体代码、功能、界面以及后续发展由 ZyNova 项目维护者负责。

如需了解 ZalithLauncher2 官方项目，请以其官方仓库中的信息为准。

---

## ⚖️ 开源与许可证

ZalithLauncher2 项目使用 **GNU General Public License v3.0 (GPL-3.0)**。

ZyNova 基于 ZalithLauncher2 的开源代码开发，因此 **ZyNova 自身同样以 GPL-3.0 发布**
（仓库根目录 `LICENSE`），在涉及上游代码的部分遵循适用的 GPL-3.0 许可证要求。

使用、修改、再发布或分发 ZyNova 时，请同时注意：

- ZalithLauncher2 所适用的许可证要求
- ZyNova 自身适用的许可证要求
- 项目中其他第三方开源组件各自适用的许可证
- 相关版权声明
- 各许可证文本中的其他要求

请在使用、修改或再发布本项目之前仔细阅读相关许可证及版权信息。

### 各组件许可证并不相同

**不要因为某一个依赖是 MIT，就认为整个项目都是 MIT。** 本项目逐组件适用各自的许可证：

| 组件 | 许可证 |
|---|---|
| ZyNova Launcher（本项目自身） | **GPL-3.0** |
| ZalithLauncher2（上游，ZyNova 基于其开发） | **GPL-3.0**（上游另有 GPLv3 第 7 条附加条款，见本文件及各 README 的「附加条款」小节） |
| Ironized Zink（GoyDevv） | **GPL-3.0** |
| MobileGlues（MobileGL-Dev） | **LGPL-2.1** |
| GL4ES（gl4es_extra_extra / PojavLauncherTeam） | **MIT** |
| Mesa 主代码 / Gallium 代码（含 Zink 驱动） | **MIT** |
| Mesa GLX 客户端代码 | **SGI Free Software License B** |
| Mesa GL / GLX 扩展头文件 | **Khronos** |
| Mesa C11 线程模拟 | **Boost（宽松许可）** |
| Kopper | 属于 Mesa 代码库，随 Mesa 适用上述条款 |
| 其余第三方依赖（ANGLE / LWJGL / SDL3 / MMKV / sora-editor / Terracotta 等） | 各自上游许可证，见应用内「关于 → 开源许可」 |

- 仓库根目录 `LICENSE` 是 **GPL-3.0** 全文，其中保留了上游
  Zalith Launcher / MovTery 的版权声明 —— 这是 GPL-3.0 的要求，**不会删除**
- 上游 ZalithLauncher2 依据 GPLv3 第 7 条提出的附加条款（分发修改版时需更名以区别于原版、
  且不得移除程序显示的版权声明）同样适用；本项目已在启动器「关于」页面标注
  「非官方修改版本」，并在四份 README 中完整转录该附加条款
- 完整声明（含各预编译二进制的来源与对应源码获取方式）：
  [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)
- 应用内「关于 → 开源许可」逐条列出各组件及其许可文本
- **上游作者的版权与署名信息一律保留，不会被删除**
- **MobileGlues** 以 LGPL-2.1 分发的是**未经修改**的预编译动态库：
  已与上游官方 release 内的同名二进制做 **SHA-256 比对，四个 ABI 全部一致**；
  其对应源码获取方式已在 `THIRD_PARTY_NOTICES.md` 中给出

### 附加条款（依据 GPLv3 第 7 条）

1. **分发本程序的修改版本时，必须合理地修改程序名称或版本号，以区别于原版。**
   （依据 [GPLv3 第 7(c) 条](https://github.com/ZalithLauncher/ZalithLauncher2/blob/969827b/LICENSE#L372-L374)）
   - 修改版本的名称中**不得包含原程序名称「ZalithLauncher」或其缩写「ZL」，
     也不得使用与官方名称足够相似、可能造成混淆的名称**
   - 所有修改版本**必须在程序启动画面或主界面明确标注「非官方修改版本」**
   - 程序的应用名称可在 [gradle.properties](./ZalithLauncher/gradle.properties) 中修改
2. **不得移除程序所显示的版权声明。**
   （依据 [GPLv3 第 7(b) 条](https://github.com/ZalithLauncher/ZalithLauncher2/blob/969827b/LICENSE#L368-L370)）

---

## ❤️ 致谢

感谢：

- ZalithLauncher2 项目及其所有贡献者
- **Ironized Zink（GoyDevv）** —— 本项目的默认渲染器
- **MobileGlues（MobileGL-Dev）**、**gl4es_extra_extra（PojavLauncherTeam）** —— 另外两个内置渲染器
- **Mesa / Zink / Kopper** 的作者与贡献者 —— Ironized Zink 的渲染引擎
- 本项目使用的其他开源项目
- 第三方开源组件的作者与贡献者
- 所有参与测试的用户
- 提交 Bug 和反馈问题的用户
- 提供建议和改进意见的用户

感谢所有开源项目和社区生态为 ZyNova 提供的技术基础。

---

## 📌 总结

ZyNova 从一个最初的界面与功能需求开始，逐渐发展成为一个完整的 Android Minecraft: Java Edition 启动器项目。

目前项目已经拥有：

- 独立名称
- 独立应用身份
- 独立源码仓库
- 完整 Android 工程
- Kotlin / Java / C / NDK
- **全局 AI Agent**（Provider 可扩展 / 25 个可真正执行操作的工具 / 历史对话与侧边栏 / 写操作审计 / 数据流向说明）
- 统一资源管理核心
- 资源来源 Provider 体系
- 统一下载管理器
- 卡片式主页
- 自有更新体系
- 单一通用版本构建（含代码混淆）
- GitHub Release
- 正式 APK
- 内置渲染器体系（Ironized Zink / GL4ES / MobileGlues）
- ZalithLauncher2 扩展兼容
- 功能与 UI 修改
- 实际 Android 真机测试
- Bug 修复与版本迭代
- 项目维护与交接文档

并且整个项目的开发、测试与发布流程，均**只使用一部 Android 手机操作完成**（APK 编译交由 GitHub Actions 在云端执行）。

ZyNova 不追求一次完成所有事情，也不会为了更新而强行加入功能。

未来将按照实际需求自然发展：

> **有 Bug 就修。**
>
> **有需要就改。**
>
> **有想法就加。**
>
> **没有需要，就保持当前状态。**

---

# ZyNova

**Minecraft: Java Edition Android Launcher**

**基于开源，持续开发，独立维护。**

**一个只用一部 Android 手机操作完成开发、测试与发布的独立项目（编译借助云端 CI）。**

---

## 💖 支持 ZyNova

ZyNova 由**一个人**维护，全程只用**一部 Android 手机**开发，背后没有公司、团队，也没有资金支持。
如果这个启动器帮到了你，欢迎请作者喝杯水 —— 这些支持会直接用在让项目继续活下去、继续变好上。

<p align="center">
  <img src="assets/donate/alipay.jpg" width="220" alt="支付宝">&nbsp;&nbsp;&nbsp;&nbsp;<img src="assets/donate/wechat.png" width="220" alt="微信支付">
</p>

<p align="center"><sub>支付宝&nbsp;·&nbsp;微信支付</sub></p>

打赏**完全自愿**。提一个 Bug、改进一句翻译，或者只是点一个 Star，帮助同样大。
