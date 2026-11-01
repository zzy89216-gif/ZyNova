<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-27.8.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.8.1)

**简体中文** | [English](README_EN.md) | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md) | [한국어](README_KO.md) | [Русский](README_RU.md)

---

一个面向 Android 的 Minecraft: Java Edition 启动器，基于 ZalithLauncher2 源码开发，独立维护、独立发布。

> 说人话版：这是你手机上的 MC 摆渡车。票不用买，行李不用自己搬（模组、整合包、光影它替你搬），
> 司机（AI）还会自己看路况、自己修车——你只要说一句「走」。
> 车上没有方向盘，因为它不打算让你动手；也没有刹车，因为它相信自己。
> 唯一的使用说明就是：想干什么，用中文说出来，然后等着。

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ 27.8.0 更新要点

- **AI 是 Agent，不是聊天机器人**：说一句就能装游戏本体 / 装整合包 / 装资源 / 改实例配置，默认「完全控制」
- **下载全部静默**：安装不再弹窗，进度只在顶部细进度条；点开是「标题 / 步骤 / 进度 / 速度 / 取消」的明细
- **NeoForge 修好了**：镜像覆盖整个 maven.neoforged 仓库，不再动不动下载失败、卡住
- **细节**：聊天输入框提示语输入即隐；收藏 / 资源 / 整合包共用同一个任务队列

## 🛠️ 主要功能

- **AI Agent**：默认 DeepSeek，装游戏本体 / 装整合包 / 装资源 / 改配置 / 读日志 / 启动游戏，直接动手
- **渲染器**：自动 / MobileGlues / Ironized Zink / GL4ES
- **资源核心**：Modrinth / CurseForge 搜索、依赖解析、下载、校验、安装
- **收藏与归档**：在线收藏 + 私有目录归档，SHA-256 去重，离线可用
- **Fabric API 防呆**：只由 Fabric 流程自动加入，不可重复安装

## 📦 下载与构建

- 下载：<https://github.com/zzy89216-gif/ZyNova/releases>
- 构建：JDK 17 + Android SDK / NDK，执行 `./gradlew ZalithLauncher:assembleRelease`

## 📚 文档

| 内容 | 文件 |
|---|---|
| 更新日志 | [CHANGELOG.md](CHANGELOG.md) |
| 交接 / 架构 / 开发流程 | [HANDOFF.md](HANDOFF.md) |
| 第三方许可证 | [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) |
| 许可证 | [LICENSE](LICENSE) |



## 💖 支持

- 反馈问题：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- 赞助：<https://afdian.com/a/hizzy>

## ⚖️ 许可证

GPL-3.0，必须保持开源；第三方组件见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。

---

**ZyNova — Context First. Less Steps.**
