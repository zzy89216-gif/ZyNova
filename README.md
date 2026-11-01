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
[![Release](https://img.shields.io/badge/Release-27.7.2-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.7.2)

**[简体中文](README.md)** | [English](README_EN.md) | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md) | [한국어](README_KO.md) | [Русский](README_RU.md)

---

## ✦ 简介

**ZyNova** 是一个面向 Android 的 **Minecraft: Java Edition** 启动器，基于
[ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2) 开源代码开发。
它**不是** ZalithLauncher2 官方版本，而是独立维护、独立发布的非官方修改版。

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ 27.7.2 更新要点

- **收藏合并**：在线收藏（Modrinth / CurseForge，含一键安装与自动前置）+ 本地归档，同页两段切换；下载页左侧栏可直接进入
- **渲染器参数拆页**：MobileGlues / Ironized Zink / GL4ES 各自独立页面，不再堆在高级设置里
- **设置中心重构为七大分类**：基础设置 / 游戏设置 / 渲染器设置 / 网络与下载 / AI 设置 / 高级设置 / 关于与支持
- **网络与下载**：连接数、连接超时、读超时、失败重试可调；支持按服务分别修改下载源（填错自动回退官方）；
  可查看并清理下载缓存、管理下载任务
- **渲染器配置整合**：按当前选中的渲染器动态显示专属配置；三个渲染器的全部可调参数集中在「高级设置」
- **AI 设置并入设置中心**：聊天页独立入口移除，保留首次配置引导；已有 API Key / 模型 / 权限模式不受影响
- **设置中心七大分类**：基础 / 游戏 / 渲染器 / 网络与下载 / AI / 高级 / 关于与支持；
  渲染器参数与逐服务下载源都能在这里调整
- **收藏与本地归档**：把手机里的资源复制进应用私有目录，SHA-256 去重；可一键安装到实例并自动下载必需前置，
  支持详情、完整性校验、导出与独立删除
- **视频背景启动优化**：播放器不再在界面初始化时创建，首帧出画后淡入，避免白屏与闪烁
- **下载与安装转后台**：离开下载页不会中断安装，进度框可「后台运行」
- **创建世界**：极限模式下「允许作弊」「死亡不掉落」强制关闭并置灰
- **NeoForge 下载**：进度按真实字节计算，整批停摆会主动报错，失败原因可见，重试不再从零下载

> 完整历史见 [CHANGELOG.md](CHANGELOG.md)。

## 🛠️ 主要功能

- **AI Agent**：默认 DeepSeek，也能读取日志、管理模组、改配置、装资源、启动游戏，而不只是聊天
- **渲染器**：自动 / MobileGlues / Ironized Zink / GL4ES
- **卡片式主页**：按实例展示世界与服务器，可一键创建世界 / 添加服务器并 quickPlay 进入
- **统一资源管理**：Modrinth / CurseForge 搜索、依赖解析、下载、SHA1 校验、安装一条链路
- **收藏与本地归档**：把手机里的模组 / 材质包 / 光影 / 存档复制进私有目录，SHA-256 去重，离线可用
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
