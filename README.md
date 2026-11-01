<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-27.7.2-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.7.2)

**简体中文** | [English](README_EN.md) | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md) | [한국어](README_KO.md) | [Русский](README_RU.md)

---

一个面向 Android 的 Minecraft: Java Edition 启动器，基于 ZalithLauncher2 源码开发，独立维护、独立发布。

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ 27.7.2 更新要点

- **设置中心七大分类**：基础 / 游戏 / 渲染器 / 网络与下载 / AI / 高级 / 关于
- **渲染器参数独立页**：MobileGlues / Ironized Zink / GL4ES 各一页，参数全部可调
- **收藏合并**：在线收藏（可安装、自动装前置）+ 本地归档，同页切换
- **下载体验**：顶部进度条可看明细；安装静默后台执行；视频背景恢复循环

## 🛠️ 主要功能

- **AI Agent**：默认 DeepSeek，能读日志、管模组、改配置、装资源、启动游戏
- **渲染器**：自动 / MobileGlues / Ironized Zink / GL4ES
- **资源核心**：Modrinth / CurseForge 搜索、依赖解析、下载、校验、安装
- **收藏与归档**：在线收藏 + 私有目录归档，SHA-256 去重，离线可用
- **Fabric API 防呆**：只由 Fabric 流程自动加入，不可重复安装

## 📦 下载与构建

- 下载：<https://github.com/zzy89216-gif/ZyNova/releases>
- 构建：JDK 17 + Android SDK / NDK，执行 `./gradlew ZalithLauncher:assembleRelease`

## 📚 文档

[CHANGELOG.md](CHANGELOG.md) · [HANDOFF.md](HANDOFF.md) · [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) · [LICENSE](LICENSE)

## ⚖️ 许可证

GPL-3.0，必须保持开源；第三方组件见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。

---

**ZyNova — Context First. Less Steps.**
