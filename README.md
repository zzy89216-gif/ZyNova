<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-27.8.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.8.1)

**简体中文** | [English](README_EN.md) | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md) | [한국어](README_KO.md) | [Русский](README_RU.md)

---

一个面向 Android 的 Minecraft: Java Edition 启动器，基于 ZalithLauncher2 源码开发，独立维护、独立发布。

> **说人话版（本段是全文唯一可以玩梗的地方）**
> 这是你手机上的 MC 摆渡车：票不要钱，行李不用你搬（模组、整合包、光影它自己扛），
> 司机是个 AI，会看路况也会修车——你只要说一句「走」。
> 车上没有方向盘，因为它不打算让你动手；也没有刹车，因为它相信自己。
> 它偶尔也会走神，某个功能可能先它一步离开这个世界；别慌，这不叫崩，叫版本迭代的代价 ( ˘•ω•˘ )
> 开摆之前先点一下「下载」，它会自己动的 (๑•̀ㅂ•́)و✧

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ 27.8.1 更新要点

- **点下载后有进度反馈**：安装任务启动前，顶部细进度条不再瞬间结束，会跟随真实进度显示
- **详细任务面板默认收起**：只有点击顶部进度条才展开；任务结束后自动收起
- **动画节奏统一**：列表入场错峰改为 30ms 递增、总量封顶 180ms
- **27.8.0 能力延续**：AI 可安装游戏本体 / 整合包 / 资源、修改实例配置；NeoForge 镜像已修

## 🛠️ 主要功能

- **AI Agent**：默认 DeepSeek，可安装游戏本体 / 整合包 / 资源、修改配置、读取日志、启动游戏
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
