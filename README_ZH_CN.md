# ZyNova

> **Minecraft: Java Edition · Android Launcher**
>
> 基于 ZalithLauncher2 开源代码开发的独立非官方项目  
> **只用一部 Android 手机完成开发、测试与发布（编译交由 GitHub Actions 云端完成）**

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Primary-Kotlin-blue)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-GPL--3.0-orange)](https://www.gnu.org/licenses/gpl-3.0.html)
[![Release](https://img.shields.io/badge/Release-26.4.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v26.4.0)
[![Architecture](https://img.shields.io/badge/Architecture-Universal%20APK-red)](https://github.com/zzy89216-gif/ZyNova)

[English](README.md) | **[简体中文](README_ZH_CN.md)** | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md)

---

## ✦ 项目简介

**ZyNova** 是一个基于 [ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2) 开源代码深度开发的 **Minecraft: Java Edition Android 启动器**，由 ZyNova 项目维护者独立开发与维护。

> **目标很简单：打爆同行 100 年。（玩梗）**

> **ZyNova 并非 ZalithLauncher2 官方版本。**
>
> 它是一个基于上游开源代码进行深度修改、独立维护、独立发布的非官方项目。

当前版本 **26.4.0**。这一阶段的核心目标是：

> **进一步脱离 ZalithLauncher2 的遗留逻辑，建立 ZyNova 自己的资源管理、下载、主页与 UI 基础。**

围绕这一目标，项目已经建立了自己的：

- **渲染器体系**（内置 Ironized Zink / GL4ES / MobileGlues，默认 Ironized Zink）
- **Microsoft（正版）账号登录**（OAuth 2.0 设备代码流，无需 Client Secret）
- 统一资源管理核心（Resource Management Core）
- 资源来源 Provider 体系（Modrinth / CurseForge）
- 统一下载管理器（队列 / 并发 / 续传 / 重试 / 校验 / 清理）
- 极简资源安装流程（上下文直装）
- 卡片式主页（以版本为模块，模块内展示世界与服务器）
- 玻璃效果（Glass UI）两档
- 启动器自有更新体系（GitHub Releases）
- Minecraft 26.4 Snapshot 1 的 Vulkan 检测
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
| **Microsoft（正版）账号登录**（设备代码流，支持添加账号与会话续期） | ✅ |
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
| **Minecraft 26.4 Snapshot 1 Vulkan 检测与兼容判断** | ✅ |
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

## 🎨 UI 与视觉体验

ZyNova 对原有启动器的用户界面和交互体验进行了较大范围的调整。

项目并不是简单修改应用名称或者替换少量界面元素，而是根据实际 Android 手机使用场景，对多个界面和交互部分进行了重新设计。

主要包括：

- 全新的界面布局调整
- 交互逻辑调整
- 动态毛玻璃效果
- 半透明与模糊相关视觉效果
- Android 手机端适配
- 启动器操作流程调整
- 其他使用体验相关优化

---

## ⚙️ 功能开发

### 🎮 Minecraft 启动

针对 Minecraft: Java Edition 启动流程进行修改和调整。

### 🧩 渲染器

26.3.0 起重做渲染器体系。**启动器内置的渲染器只保留三个**：

| 渲染器 | 说明 | 配置 |
|---|---|---|
| **Ironized Zink**（默认） | 桌面 OpenGL 经 Vulkan 转译（Mesa Zink + Kopper），作者 GoyDevv；默认 OpenGL 4.6 | **4 个官方预设**（26.4.0 起面板只提供预设，见下） |
| **GL4ES** | 经典 OpenGL 转译层 | 保持默认 |
| **MobileGlues** | 把桌面 OpenGL 转译到设备的 OpenGL ES 3.x | 保持上游默认 |

**Ironized Zink 的 4 个官方预设**（参数取值与上游一致）：

| 预设 | 定位 | Minecraft | 光影 |
|---|---|---|---|
| **Potato** | 绝对最高帧率 | 1.8 → 最新（含 26.x） | 不推荐 |
| **Performance** | 高帧率 + Sodium | 1.20.x → 最新 | 轻量 |
| **Default** | 均衡 Zink + 光影（**默认**） | 1.16.x → 最新 | 完整（Iris / OptiFine） |
| **Max Compatibility** | 什么都能跑 | 全部版本 | 完整 + 重度光影包 |

**26.4.0 起，面板只提供上面 4 个预设**：OpenGL 版本下拉与 12 个单独参数开关已被移除，
普通用户不再需要手动调整底层参数。选择预设仍会一次性写入整组参数。

> ⚠️ 4 个预设的 OpenGL 版本都是 **4.6**。移除版本下拉后，**新用户无法再把 OpenGL 版本
> 降到 4.5 / 4.3 / 3.3**；而在 26.3.0 期间手动改过参数的老用户，其已保存的取值仍会继续
> 生效（依旧会被注入为环境变量），但已无法在界面上修改 —— 重新选择任意预设即可把全部
> 参数一次性写回受支持的组合。

在 **设置 → 渲染器** 里选中 Ironized Zink 后，**下方会立刻展开预设面板**。

#### 官方内置 与 外部扩展 的区别

26.3.0 删除的是**启动器官方内置**的其余 Renderer，**没有**移除外部 Renderer Plugin 机制：

| 类别 | 内容 | 26.3.0 状态 |
|---|---|---|
| **官方内置** | Ironized Zink（默认）、GL4ES、MobileGlues | 只保留这 3 个；其余内置 Renderer（Krypton Wrapper / Kopper Zink / VirGL / Freedreno / Panfrost）已删除 |
| **外部扩展** | FCL / ZalithLauncher 渲染器插件、新一代 `fclPlugin_V2` 渲染器插件、Vulkan 驱动插件、原生库插件 | **完全保留**，安装后仍会正常出现在渲染器列表中 |

外置插件自带自己的原生库，不受本次内置裁剪影响；
设置页的「下载渲染器插件」入口也仍然保留。

### 🏠 卡片式主页

ZyNova 提供卡片式主页，**以游戏版本为模块**组织内容：

每个已安装的版本是一个模块卡片，卡片内部**直接列出该版本自己的**：

| 内容 | 操作 |
|---|---|
| 本地世界 | 点击直接进入 |
| 已保存的服务器 | 点击直接加入 |

点击模块卡片本身即可**启动该版本**。

卡片式主页还支持：

- **长按拖动排序**：版本卡片之间、卡片内的世界 / 服务器，
  以及右侧菜单的三块（账号头像 / 版本行 / 启动按钮）都能拖动调整顺序，顺序会被记住
- **卡片大小**：设置 → 启动器 → 卡片大小（70% ~ 140%，默认 100%）
- 卡片样式为**单层背景**，不会出现多余的边框或阴影

主页数据**按需加载**：只有真正进入主页时才读取，并且限量扫描，
启动器启动时不会进行全盘扫描。

设置中可以选择主页类型，共 **4 种**：

| 主页类型 | 说明 |
|---|---|
| **空白** | 默认值，不显示任何内容 |
| **卡片主页** | 以版本为模块的卡片式主页（见上） |
| **从本地加载** | 加载启动器本地的主页文件（扩展 Markdown 语法），可在设置里一键生成官方主页文档 |
| **从网络加载** | 从指定链接获取主页文件并缓存，定时刷新重载 |

> 自定义主页属于第三方内容，启动器不为其内容做担保。

### 📦 统一资源管理核心

ZyNova 建立了自己的资源管理核心（Resource Management Core），
统一处理 Mod、资源包、光影、存档等 Minecraft 资源：

> **搜索 → 资源详情 → 版本匹配 → 文件选择 → 下载 → 校验 → 安装**

所有资源类型走同一条流程，界面不再分别实现自己的下载与安装逻辑。

### 🔌 资源来源（Resource Provider）

资源来源与界面完全解耦：

```
Resource Provider
├── Modrinth
└── CurseForge
```

上层只依赖统一接口。以后增加其他资源来源时，通过 Provider 扩展即可，
不需要重写整个资源系统。

### 📥 统一下载管理

Mod、资源包、光影、存档以及它们的前置依赖，全部使用统一下载管理器：

- 下载队列
- 并发控制
- 下载进度
- 断点续传
- 失败重试
- 取消下载
- 文件校验
- 临时文件清理
- 下载完成后的安装触发

### ⚡ 极简资源安装

当用户从「版本设置 → Mods / 资源包 / 光影 / 存档」进入资源页面时，
系统已经知道当前实例、Minecraft 版本、加载器和资源目录。

因此点击资源卡片上的下载按钮后会直接：

> **检查兼容性 → 选择兼容文件 → 下载 → 校验 → 安装**

不会再重复要求用户选择 Minecraft 版本、实例或安装位置。
安装完成后资源列表会根据状态显示「已安装」。

从主界面进入的资源中心则只负责浏览与管理，不提供一键安装入口。

### 🖥️ Vulkan 检测与兼容性

ZyNova 提供面向 Minecraft 26.4 Snapshot 1 的 Vulkan 检测：

- 检测结果明确区分：**可用 / 不可用 / 检测失败**
- 检测内容包括：Vulkan 是否可用、Vulkan API 版本、驱动是否正常、
  必要扩展与功能、GPU / 渲染器信息，以及检测失败的具体原因
- 不可用时给出具体原因，并支持用户主动重新检测
- 判断依据是设备**实际枚举出来的 Vulkan 能力**，
  而不是设备对外声明的 Vulkan 支持情况

### 🎨 界面与视觉效果

- 玻璃效果（Glass UI）提供两档：**关闭 / 启用动态玻璃**，默认关闭
  - 关闭档不叠加任何玻璃高光层，性能优先
  - 启用动态玻璃会在毛玻璃之上叠加缓慢流动的高光与折射光晕
  - ⚠️ 此前的「标准 / 增强 / ⚠️极致」三档已在 26.2.2 移除：
    「极致」档使用 GPU 着色器对整个元素做多重采样模糊与折射扭曲，
    会连带把承载文字的图层一起模糊，导致**字体明显模糊、文字渲染异常**，
    因此在 26.2.2 中简化掉
- 在性能与视觉效果之间优先保证移动设备的流畅度，
  避免高开销实时模糊、大量透明层叠加与持续动画

### 🔄 启动器更新

ZyNova 只维护自己的更新体系：

- 版本信息与更新日志来自 ZyNova 自己的 GitHub Releases
- Release 只提供一个**通用版本**安装包（已包含全部 4 个 ABI），用户不需要选择架构

### 🔌 扩展兼容

ZyNova 在独立开发的同时，继续保留对 ZalithLauncher2 相关扩展生态的兼容。

项目会尽可能在独立开发新功能的同时保持原有扩展的兼容性。

---

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

---

## 🧬 技术栈

ZyNova 是一个完整的 Android 软件工程，而不仅仅是一个 APK 文件。

项目涉及：

| 技术 | 用途 |
|---|---|
| Kotlin | Android 主要开发语言 |
| Java | Android / 项目相关代码 |
| C | Native 相关部分 |
| NDK | Native 构建及相关功能 |
| Gradle | 项目构建 |
| Git | 版本管理 |
| GitHub | 源码与项目管理 |
| GitHub Actions | 自动化相关流程 |
| Android | 主要目标平台 |

当前 GitHub 语言统计曾显示：

| Language | Percentage |
|---|---:|
| Kotlin | **64%** |
| Java | **22.9%** |
| C | **13%** |
| JavaScript | **0.1%** |
| Makefile | **0%** |
| C++ | **0%** |

由于项目仍在持续开发和修改过程中，实际比例可能随版本变化。

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

**ZyNova 26.4.0**

26.4.0 恢复正版登录入口，并处理 3 个议题：

- **恢复 Microsoft（正版）账号登录入口**：登录菜单重新提供「微软账号」，
  走 OAuth 2.0 **设备代码流**（自动复制设备码 → 打开验证网页 → 轮询取令牌）。
  底层认证实现一直都在，本次还原的是 26.1.0 移除的 UI 与 ViewModel 接线；
  **不需要 Client Secret，也不需要 Redirect URI / SHA-1**
- **修复下载资源页「类别」筛选器内容为空**（Issue #6）：
  默认「所有平台」下类别列表以参照来源（CurseForge）为准，
  且类别条件只下发给该来源，多来源时标题会标注「（仅 CurseForge）」
- **修复资源列表卡片缺少「资源的类别」徽章**（Issue #6）：
  列表调用漏传 `classes` 实参，导致 模组 / 资源包 / 光影包 / 存档 / 整合包 徽章永不渲染
- **Ironized Zink 设置面板只保留 4 个官方预设**（Issue #7）：
  移除 OpenGL 版本下拉与 12 个参数开关，普通用户无需再手动调底层参数
  （⚠️ 4 个预设的 OpenGL 版本均为 4.6，因此新用户无法再降到 4.5 / 4.3 / 3.3）

**ZyNova 26.3.0**

26.3.0 重做渲染器体系，并收敛发布形态：

- **内置渲染器只保留 Ironized Zink（默认）、GL4ES、MobileGlues**，
  删除 Krypton Wrapper（NG-GL4ES）、Kopper Zink、VirGL、Freedreno、Panfrost
- Ironized Zink **完整集成其原生配置**：4 个官方预设（Potato / Performance / Default / Max Compatibility）
  + 全部 13 个可调参数；在设置里选中后会**自动在下方展开配置面板**
- 默认渲染器改为 **Ironized Zink 的 Default 预设**
- **修复主页向下滚动时卡片异常移动**（滚动的整体位移被误判成让位动画）
- **只发布一个通用版本 APK**（含全部 4 个 ABI）并保持代码混淆
- 第三方组件许可证逐组件声明（`THIRD_PARTY_NOTICES.md`）

26.2.6 修复拖动排序引入的两个显示问题，并把「操作菜单长按拖动换边」从上游搬了过来：

- 修复拖动后卡片内容被顶到错误位置（位移修饰符层级 + 让位动画残留）
- 操作菜单：**长按整块可以拖到屏幕左侧或右侧**，内容区一起让位，停泊侧会被记住
- 右侧菜单恢复上游的排布

26.2.5 处理卡片式主页的边框显示问题，并加入长按拖动排序：

- 彻底移除卡片式主页卡片的边框（卡片只保留一层背景，去掉阴影与缩放图层）
- 卡片式主页支持**长按拖动排序**：版本卡片之间、卡片内的世界 / 服务器、
  以及右侧菜单的三块（账号头像 / 版本行 / 启动按钮）都可以拖动调整顺序，顺序会自动记住

26.2.4 处理两个新反馈：一个卡片式主页的 Bug 与一个卡片大小的功能请求：

- 修复卡片式主页的卡片不透明度不跟随「背景元素不透明度」设置
- 卡片式主页新增「卡片大小」设置（70% ~ 140%，默认 100% 保持原有外观）

26.2.3 集中修复**模组以外**的资源下载链路（整合包 / 资源包 / 存档 / 光影包）：

- 修复资源包 / 光影包 / 存档的搜索结果几乎为空
  （加载器过滤条件被错误地用在了并不按加载器分类的资源类型上）
- 修复聚合搜索的总页数会变成 0（界面显示「1 / 0」并且完全无法翻页）
- 修复「所有平台」会去请求并不支持该资源类型的来源（例如存档在 Modrinth 上并不存在）
- 修复未配置 CurseForge API Key 时 CurseForge 侧完全搜不到资源（无 Key 时保留 MCIM 镜像源）
- 修复存档「类别」过滤器永久不可用、存档解压失败后残留 `.zip`

26.2.2 是一次以「修复实际反馈」为主的版本：

- 移除了会让字体模糊的「⚠️极致」玻璃档，玻璃效果简化为 **关闭 / 启用动态玻璃** 两档
- 修复了一键安装偶尔报 `No compatible version found for this instance` 的问题
  （搜索在切换平台后丢失加载器过滤条件，导致结果里混进其他加载器的资源）
- 失败提示改为本地化，并带上目标实例的 Minecraft 版本与模组加载器
- 前置依赖解析失败不再被静默丢弃
- 修复 Discord 服务器邀请链接全部失效的问题（更换为永久邀请）

26.2.1 重做了「⚠️极致」玻璃效果、修复了搜索结果缺少快捷安装按钮与模组加载器未自动选中的问题，
并把主页改为以「版本」为模块、新增「所有平台」聚合搜索。

26.1.0 版本的核心目标是：进一步脱离 ZalithLauncher2 的遗留逻辑，
建立 ZyNova 自己的资源管理、下载、主页与 UI 基础。

主要变化包括：

- 统一资源管理核心与资源来源 Provider
- 统一下载管理器与极简资源安装
- 卡片式主页与玻璃效果（26.2.2 起简化为两档）
- Minecraft 26.4 Snapshot 1 的 Vulkan 检测与兼容判断
- 恢复添加账号界面的**正版（微软）登录入口**，并修复下载资源页的类别显示与 Ironized Zink 预设面板

完整的变更记录见 [CHANGELOG](CHANGELOG.md)。

Release 提供：

- 通用版本 APK（含 `arm64-v8a` / `armeabi-v7a` / `x86` / `x86_64`，已代码混淆）
- `mapping.universal.zip`（混淆映射）

---

## 🛠️ 开发与测试流程

ZyNova 的开发并不是只在源码层面完成。

实际流程包括：

**需求**

↓

**源码分析**

↓

**代码修改 / 功能开发**（在 Android 手机上）

↓

**工程构建 / APK 打包**（GitHub Actions 云端）

↓

**Android 真机安装**

↓

**实际测试**

↓

**发现 Bug**

↓

**修复 Bug**

↓

**重新构建**（云端）

↓

**再次测试**

↓

**Release 发布**（通过 GitHub）

项目中的部分功能是在实际 Android 设备使用过程中进行测试和调整的。

---

## 📱 用手机开发的项目

ZyNova 项目有一个比较特殊的工作方式：

> **从写代码、测试到发布，全部只使用一部 Android 手机操作，没有使用电脑。**

不过有一点需要说明清楚：**APK 的编译并不在手机上完成，而是在 GitHub Actions 的云端服务器上完成。**

| 环节 | 在哪里完成 |
|---|---|
| 源代码编写 / 修改 | Android 手机 |
| 项目文件管理 | Android 手机 |
| Gradle / 工程配置处理 | Android 手机 |
| Git 操作、GitHub 仓库管理 | Android 手机 |
| 查阅编译日志、修复编译错误 | Android 手机 |
| **APK 编译（通用版本 + 代码混淆）** | **GitHub Actions 云端服务器** |
| Android 真机安装测试 | Android 手机 |
| Release 发布 | Android 手机（通过 GitHub API） |
| 文档编写与维护 | Android 手机 |

原因很直接：Android 手机环境缺少完整的 Android SDK / NDK 工具链
（NDK 的 clang 只有 x86_64 版本，Google 不提供 arm64 Linux 版），
无法在手机本地完成 APK 编译，因此编译环节交由 GitHub Actions 的云端服务器执行。

所以更准确的说法是：

> **整个项目的开发、测试与发布流程只用一部 Android 手机操作完成，编译则借助 GitHub Actions 在云端完成。**

本项目不是：

> 电脑开发 → 电脑构建 → 手机测试

而是：

> **手机开发 → 云端构建 → 手机测试 → 手机发布**

---

## 🤖 AI Agent 辅助开发

ZyNova 的开发过程中大量使用 AI Agent 辅助实际工程工作。

AI Agent 参与的工作包括：

- 源码分析
- 工程结构分析
- 代码修改
- 功能开发
- 构建错误分析
- Bug 修复
- APK 构建
- 通用版本打包（含代码混淆）
- 文件整理
- GitHub 操作
- Release 文件整理
- 项目文档编写
- 项目交接文档编写

AI Agent 是项目开发过程中使用的辅助工具之一。

项目的需求、开发方向、测试结果以及最终发布由项目维护者决定。

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

ZyNova 是一个完整的 Android 工程。

仓库中包括：

- Android 项目源码
- Kotlin / Java 源码
- Native / C 相关代码
- Gradle 构建文件
- 项目模块
- GitHub 配置
- README（`README.md` 英文 / `README_ZH_CN.md` 简体中文 / `README_ZH_TW.md` 繁體中文 / `README_JA_JP.md` 日本語）
- LICENSE（`LICENSE`，GPL-3.0）
- 更新记录（`CHANGELOG.md`）
- 第三方许可证与版权声明（`THIRD_PARTY_NOTICES.md`）
- 维护及交接资料（`HANDOFF.md`）
- Release
- mapping 文件（混淆映射）
- 构建相关文件

因此，该仓库同时承担：

**源码仓库 + 开发仓库 + 文档仓库 + Release 发布仓库**

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

项目目前采用相对自由的长期维护方式：

| 情况 | 处理方式 |
|---|---|
| 🐛 发现 Bug | 修复 |
| 💡 有新的实际需求 | 视情况增加功能 |
| 🔧 有值得改进的地方 | 进行调整 |
| 😴 没有新的需求 | 保持当前状态 |

项目不会为了制造版本号而强行加入大量功能。

让 ZyNova 按照实际需求自然发展。

---

## 🗺️ Roadmap

### 26.4.0（当前版本）

- [x] 恢复 Microsoft（正版）账号登录入口（设备代码流；UI + ViewModel 接线还原）
- [x] 配置并随包分发 OAuth Client ID（不使用 Client Secret）
- [x] 修复下载资源页「类别」筛选器内容为空（Issue #6）
- [x] 修复资源列表卡片缺少「资源的类别」徽章（Issue #6）
- [x] Ironized Zink 设置面板只保留 4 个官方预设（Issue #7）

### 26.3.0

- [x] 内置渲染器只保留 Ironized Zink / GL4ES / MobileGlues，删除其余 5 个
- [x] Ironized Zink 完整集成原生配置（4 个官方预设 + 13 个可调参数）
- [x] 默认渲染器改为 Ironized Zink 的 Default 预设
- [x] 选中 Ironized Zink 后自动在下方展开配置面板
- [x] 修复主页向下滚动时卡片异常移动
- [x] 只发布一个通用版本 APK（含全部 4 个 ABI）+ 代码混淆
- [x] 第三方组件许可证逐组件声明（`THIRD_PARTY_NOTICES.md`）

### 26.2.6

- [x] 修复拖动排序后卡片内容被顶到错误位置
- [x] 操作菜单支持长按拖动换边（搬自上游 ZalithLauncher2）

### 26.2.5

- [x] 彻底移除卡片式主页的卡片边框
- [x] 版本卡片、世界、服务器支持长按拖动排序（顺序自动记住）

### 26.2.4

- [x] 修复卡片式主页的卡片不透明度不跟随「背景元素不透明度」设置
- [x] 卡片式主页新增「卡片大小」设置（70% ~ 140%，默认 100%）

### 26.2.3

- [x] 修复资源包 / 光影包 / 存档搜索结果几乎为空（加载器过滤用错了资源类型）
- [x] 修复聚合搜索总页数变成 0（界面 1 / 0、无法翻页）
- [x] 「所有平台」只请求真正支持该资源类型的来源
- [x] 无 CurseForge API Key 时保留 MCIM 镜像源，CurseForge 侧恢复可用
- [x] 修复存档「类别」过滤器永久不可用（**26.4.0 已扩展为：默认「所有平台」下所有资源类型都能选类别**）
- [x] 修复存档解压失败残留 `.zip`

### 26.2.2

- [x] 移除「⚠️极致」玻璃档，简化为「关闭 / 启用动态玻璃」两档（修复字体模糊）
- [x] 修复一键安装偶发 `No compatible version found for this instance`
- [x] 安装失败提示本地化，并显示目标实例的版本与加载器
- [x] 前置依赖解析失败不再静默丢弃，改为记录日志与告警
- [x] 修复 Discord 邀请链接失效（更换为永久邀请）

### 26.2.1

- [x] 极致玻璃效果重做（真实 GPU 多重采样模糊 + 波纹折射扭曲）
- [x] 搜索结果卡片增加快捷安装按钮
- [x] 模组加载器按当前实例自动选中
- [x] 主页改为以「版本」为模块，模块内展示该版本的世界与服务器
- [x] 新增「所有平台」聚合搜索（CurseForge + Modrinth 合并到同一列表）
- [x] 排序方式默认改为「总下载量」

### 26.2.0

- [x] 修复资源安装上下文丢失（导航键的 `@Transient` 字段会被序列化机制丢弃）
- [x] 资源搜索自动按当前实例的 Minecraft 版本过滤
- [x] 玻璃效果新增「⚠️极致」档位（切换前弹出性能警告）

### 26.1.x

- [x] 统一资源管理核心（Resource Management Core）
- [x] 资源来源 Provider 化（Modrinth / CurseForge）
- [x] 统一下载管理器
- [x] 极简资源安装（上下文直装）
- [x] 卡片式主页
- [x] 玻璃效果（关闭 / 标准 / 增强 / ⚠️极致，26.2.2 已简化为两档）
- [x] Minecraft 26.4 Snapshot 1 Vulkan 检测适配
- [x] 移除 ZalithLauncher2 更新链
- [x] ~~移除添加账号界面的正版登录入口~~ → **26.4.0 已恢复**
- [x] 按需加载与性能策略
- [x] 修复卡片式主页导致的启动器崩溃（26.1.1）

### 后续方向

- [ ] 为自定义主页开放更完整的统一数据接口
- [ ] 让统一资源管理核心覆盖更多资源来源
- [ ] 继续收敛 ZalithLauncher2 的遗留逻辑
- [ ] 渲染与键位相关的进一步优化

---

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
| ZalithLauncher2（上游，ZyNova 基于其开发） | **GPL-3.0**（上游另有 GPLv3 第 7 条附加条款，见 `README.md` / `README_ZH_TW.md` / `README_JA_JP.md` 的「附加条款」小节） |
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
  「非官方修改版本」，并在 `README.md` / `README_ZH_TW.md` / `README_JA_JP.md` 中完整转录该附加条款
- 完整声明（含各预编译二进制的来源与对应源码获取方式）：
  [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)
- 应用内「关于 → 开源许可」逐条列出各组件及其许可文本
- **上游作者的版权与署名信息一律保留，不会被删除**
- **MobileGlues** 以 LGPL-2.1 分发的是**未经修改**的预编译动态库：
  已与上游官方 release 内的同名二进制做 **SHA-256 比对，四个 ABI 全部一致**；
  其对应源码获取方式已在 `THIRD_PARTY_NOTICES.md` 中给出

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
