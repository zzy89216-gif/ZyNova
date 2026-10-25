<p align="center">
  <img src="assets/icon.png" width="180" alt="ZyNova">
</p>

# ZyNova

> **Minecraft: Java Edition · Android Launcher**
>
> 基于 ZalithLauncher2 开源代码的独立非官方项目

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Primary-Kotlin-blue)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-GPL--3.0-orange)](https://www.gnu.org/licenses/gpl-3.0.html)
[![Release](https://img.shields.io/badge/Release-27.6.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.6.1)

**[简体中文](README.md)** | [English](README_EN.md) | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md) | [한국어](README_KO.md) | [Русский](README_RU.md)

---

## ✦ 简介

**ZyNova** 是一个面向 Android 的 **Minecraft: Java Edition** 启动器，基于
[ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2) 开源代码开发。
它**不是** ZalithLauncher2 官方版本，而是独立维护、独立发布的非官方修改版。

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ 27.6.1 更新要点

- **顶部 5 个按钮回档**：文件夹 / AI / 多人 / 下载 / 设置恢复原样，不再套用首页按钮材质
- **幽灵按钮**：启动 / 创建世界 / 添加服务器 / 条目只保留约 1.5%~2% 明暗差，无描边、保留透明度，
  图片 / 视频背景实时透出；快捷按钮缩小后并排放在启动按钮旁
- **创建世界按版本自适应**：26.1+ 直接写新存档结构；1.12.2~26.0 走 DataFixer；
  低于 1.12.2 隐藏入口；写完回读验证
- **允许作弊**：创建世界新增开关，按 Minecraft 版本自适应
- **Fabric API 加载状态**：加载中只显示「正在加载中」，加载成功才允许安装，失败 / 为空才拒绝
- **27.6.0 内容保留**：Fabric API 防呆、图标修复、一键创建世界 / 服务器

> 完整历史见 [CHANGELOG.md](CHANGELOG.md)。

## 🛠️ 主要功能

- **AI Agent**：默认 DeepSeek，也能读取日志、管理模组、改配置、装资源、启动游戏，而不只是聊天
- **渲染器**：自动 / MobileGlues / Ironized Zink / GL4ES
- **卡片式主页**：按实例展示世界与服务器，可一键创建世界 / 添加服务器并 quickPlay 进入
- **统一资源管理**：Modrinth / CurseForge 搜索、依赖解析、下载、SHA1 校验、安装一条链路
- **Fabric API 防呆**：只由 Fabric 安装流程自动加入；不可手动安装、删除、禁用或重复安装

## 📦 下载与构建

- **下载**：<https://github.com/zzy89216-gif/ZyNova/releases>
  - 通用 APK（含 arm64-v8a / armeabi-v7a / x86 / x86_64）
  - `mapping.universal.zip`（还原混淆堆栈）
- **本地构建**：需要 JDK 17、Android SDK / NDK，然后执行：
  ```bash
  ./gradlew ZalithLauncher:assembleRelease
  ```
- 正式构建由 GitHub Actions 完成，Release 构建开启代码混淆与资源压缩。

## ⚠️ 注意

- 首页「创建世界」在 **26.1+** 使用新存档格式；**1.12.2~26.0** 使用兼容层；
  **低于 1.12.2** 不提供该入口。
- Fabric API 由安装流程自动处理；手动安装会被拒绝，模组管理中也不能删除 / 禁用。
- 旧版本不支持 quickPlay 时，创建世界 / 添加服务器仍会正常启动到游戏主菜单。
- 旧配置、已有存档与服务器列表不受升级影响。

## 📚 文档

| 内容 | 文件 |
|---|---|
| 更新日志 | [CHANGELOG.md](CHANGELOG.md) |
| 交接 / 架构 / 开发流程 | [HANDOFF.md](HANDOFF.md) |
| 第三方许可证 | [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) |
| 许可证 | [LICENSE](LICENSE) |

## ⚖️ 许可证

ZyNova 以 **GPL-3.0** 发布，必须保持开源。上游 ZalithLauncher2 的版权与许可声明已保留；
附加条款与完整文本见 [LICENSE](LICENSE)，第三方组件许可见
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。

## 💖 支持

- 反馈问题：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- 赞助：见 [assets/donate](assets/donate) 与关于页

## 👥 维护者

ZyNova 由 **zzy** 独立开发与维护。

---

**ZyNova — Context First. Less Steps.**
