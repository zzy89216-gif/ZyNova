# ZyNova

> **Minecraft: Java Edition · Android Launcher**
>
> 基于 ZalithLauncher2 开源代码开发的独立非官方项目  
> **全流程使用 Android 手机开发、构建、测试与发布**

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](#)
[![Language](https://img.shields.io/badge/Primary-Kotlin-blue)](#)
[![License](https://img.shields.io/badge/License-GPL--3.0-orange)](#)
[![Release](https://img.shields.io/badge/Release-2.5.1-purple)](#)
[![Architecture](https://img.shields.io/badge/Architecture-Multi--ABI-red)](#)

---

## ✦ 项目简介

**ZyNova** 是一个基于 [ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2) 开源代码开发的 Minecraft: Java Edition Android 启动器。

本项目在上游项目代码的基础上进行了大量修改、重构、功能开发、界面调整以及 Android 平台相关适配，并由 ZyNova 项目维护者独立进行后续开发与维护。

> **ZyNova 并非 ZalithLauncher2 官方版本。**
>
> ZyNova 是一个基于 ZalithLauncher2 开源代码进行开发的非官方修改项目。

---

## 🌟 项目定位

ZyNova 是一个面向 Android 平台的 Minecraft: Java Edition 启动器项目。

项目最初源于对启动器界面与使用体验的进一步探索，随后逐渐扩展到功能开发、代码重构、安装管理、依赖处理、兼容性调整以及 Android 平台相关功能。

随着开发不断进行，项目逐渐形成了独立的：

- 项目名称
- 应用身份
- GitHub 仓库
- Release
- 开发流程
- 项目文档
- 维护体系

ZyNova 不以简单复制上游项目为目标，而是在已有开源代码基础上，根据实际需求进行进一步开发。

---

## ✨ 主要功能与修改

目前项目涉及的开发内容包括：

| 项目 | 状态 |
|---|:---:|
| Android Minecraft Java Edition 启动器 | ✅ |
| 用户界面重新设计 | ✅ |
| 交互逻辑调整 | ✅ |
| 动态毛玻璃效果 | ✅ |
| 启动器功能修改 | ✅ |
| Minecraft 启动相关功能 | ✅ |
| 游戏实例管理 | ✅ |
| 安装与管理功能 | ✅ |
| 前置依赖自动下载与安装 | ✅ |
| Android 平台功能调整 | ✅ |
| 原有代码结构调整 | ✅ |
| 新功能开发 | ✅ |
| 性能与兼容性调整 | ✅ |
| ZalithLauncher2 扩展兼容 | ✅ |
| 多 ABI APK 构建 | ✅ |
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

### 📦 游戏实例管理

支持 Minecraft 游戏实例相关的管理和操作。

### 📥 安装与依赖

对游戏、组件以及相关文件的安装和管理流程进行了调整。

ZyNova 支持部分前置依赖的自动下载与安装。

在开发过程中，自动下载前置依赖功能曾出现 Bug，并在后续版本中进行了修复。

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

## 📱 多架构支持

为了适配不同 Android CPU 架构，ZyNova 提供多个 Release 构建版本。

| 架构 | 主要适用环境 |
|---|---|
| `arm64-v8a` | 当前绝大多数现代 Android 手机 |
| `armeabi-v7a` | 部分较老的 32 位 ARM 设备 |
| `x86` | 部分模拟器 / x86 Android 环境 |
| `x86_64` | 64 位 x86 Android 环境 |
| Universal | 多架构通用版本 |

Release 中还可能包含：

- mapping 文件
- 构建相关文件
- 调试相关文件
- 其他开发配套文件

---

## 🚀 Release

当前版本：

**ZyNova 2.5.1**

2.5.1 版本主要修复了此前自动下载前置依赖过程中发现的问题。

Release 根据版本情况提供：

- ARM64 APK
- ARM32 APK
- x86 APK
- x86_64 APK
- Universal APK
- mapping 文件
- 其他必要的构建文件

---

## 🛠️ 开发与测试流程

ZyNova 的开发并不是只在源码层面完成。

实际流程包括：

**需求**

↓

**源码分析**

↓

**代码修改 / 功能开发**

↓

**工程构建**

↓

**APK 打包**

↓

**Android 真机安装**

↓

**实际测试**

↓

**发现 Bug**

↓

**修复 Bug**

↓

**重新构建**

↓

**再次测试**

↓

**Release 发布**

项目中的部分功能是在实际 Android 设备使用过程中进行测试和调整的。

---

## 📱 全流程手机开发

ZyNova 项目具有一个非常特殊的开发特点：

> **本项目从开发开始到目前的整个开发流程均使用 Android 手机完成，没有使用电脑进行开发。**

本项目不是：

> 电脑开发 → 电脑构建 → 手机测试

而是：

> **Android 手机 → 开发 → 构建 → 测试 → 修复 → 发布**

整个项目从开发、构建、测试到 Release 发布，均直接在 Android 手机上完成。

包括：

| 工作内容 | Android 手机完成 |
|---|:---:|
| 源代码编写 | ✅ |
| 源码修改 | ✅ |
| 项目文件管理 | ✅ |
| Gradle 工程处理 | ✅ |
| Android 工程构建 | ✅ |
| Native / C 相关处理 | ✅ |
| APK 编译 | ✅ |
| 多架构构建 | ✅ |
| Android 真机测试 | ✅ |
| Bug 修复 | ✅ |
| Git 操作 | ✅ |
| GitHub 仓库管理 | ✅ |
| Release 发布 | ✅ |
| 文档编写与维护 | ✅ |

因此：

**ZyNova 是一个从开发到发布全流程直接在 Android 手机上完成的项目。**

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
- 多架构打包
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
- README
- LICENSE
- 更新记录
- Release
- mapping 文件
- 构建相关文件
- 维护及交接资料

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

## 🎯 项目定位

ZyNova 定位为：

> **一个基于 ZalithLauncher2 开源代码进行深度修改、独立维护和独立发布的 Minecraft: Java Edition Android 启动器。**

ZyNova 不以简单复制上游项目为目标。

项目希望在已有优秀开源项目提供的基础上，根据实际使用需求继续进行：

- 功能开发
- UI 调整
- 代码重构
- Android 平台适配
- 兼容性调整
- 性能优化
- 使用体验改进

并逐渐形成属于 ZyNova 自身的功能、界面和使用体验。

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

## ⚠️ 与 ZalithLauncher2 的关系

**ZyNova 并非 ZalithLauncher2 官方版本。**

ZyNova 是基于 ZalithLauncher2 开源代码进行开发的非官方修改项目。

本项目与 ZalithLauncher2 官方项目不存在官方合作、授权或隶属关系，除非另有明确说明。

ZyNova 的具体代码、功能、界面以及后续发展由 ZyNova 项目维护者负责。

如需了解 ZalithLauncher2 官方项目，请以其官方仓库中的信息为准。

---

## ⚖️ 开源与许可证

ZalithLauncher2 项目使用：

**GNU General Public License v3.0 (GPL-3.0)**

由于 ZyNova 基于 ZalithLauncher2 的开源代码进行开发，因此 ZyNova 在涉及上游代码的部分遵循适用的 GPL-3.0 许可证要求。

使用、修改、再发布或分发 ZyNova 时，请同时注意：

- ZalithLauncher2 所适用的许可证要求
- ZyNova 自身适用的许可证要求
- 项目中其他第三方开源组件各自适用的许可证
- 相关版权声明
- 各许可证文本中的其他要求

请在使用、修改或再发布本项目之前仔细阅读相关许可证及版权信息。

---

## ❤️ 致谢

感谢：

- ZalithLauncher2 项目及其所有贡献者
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
- 多 ABI 构建
- GitHub Release
- 正式 APK
- ZalithLauncher2 扩展兼容
- 功能与 UI 修改
- 实际 Android 真机测试
- Bug 修复与版本迭代
- 项目维护与交接文档

并且整个项目从开发、构建、测试到发布，均使用 **Android 手机完成，没有使用电脑进行开发**。

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

**一个完全使用 Android 手机进行开发、构建、测试和发布的独立项目。**
