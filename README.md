<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.0.5-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.0.5)

**简体中文** | [English](README_EN.md) | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md) | [한국어](README_KO.md) | [Русский](README_RU.md)

---

Android 上的 Minecraft: Java Edition 启动器，基于 ZalithLauncher2 源码自行维护发布，GPL-3.0。
目标是「少点几下」：玩家的操作尽量交给 AI 和自动化流程。

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ 28.0.5 更新要点

- **AI 能选搜索引擎**：DuckDuckGo / 百度千帆 / Bing / 自建，需要 Key 的引擎旁边直接有「创建 API」直达按钮，失败自动回退
- **AI 能读写公有目录**：列目录、读文件、建文件夹、写文件、自动导入到游戏目录；删除一律进回收站（可恢复），禁止碰 Android/data 与 obb
- **任意文件自动导入**：分享或用 ZyNova 打开文件，会复制到 App 的 `imported/` 并提示路径
- **崩溃静默自动修复**：开启后游戏异常退出不弹框，AI 直接读日志动手修（默认关闭，会消耗额度）

## 🛠️ 能做什么

- **AI Agent**：默认 DeepSeek，能装游戏 / 装整合包 / 装模组 / 改设置 / 读日志 / 启动游戏
- **渲染器**：自动 / MobileGlues / GL4ES / LTW（实验性）
- **资源核心**：Modrinth / CurseForge 搜索 → 依赖解析 → 下载 → 校验 → 安装
- **收藏与归档**：在线收藏 + 私有目录归档，SHA-256 去重
- **前置 API 防呆**：Fabric / Quilted / Legacy Fabric API 由安装流程自动加入，不可重复安装、搜不到、删不掉

## 📦 下载与构建

- 下载：<https://github.com/zzy89216-gif/ZyNova/releases>（认准最新版）
- 自行构建：JDK 17 + Android SDK / NDK，然后 `./gradlew ZalithLauncher:assembleRelease`

## 📚 文档

- 用户文档中心（76 篇）：[docs/zh/README.md](docs/zh/README.md)
- 更新日志：[CHANGELOG.md](CHANGELOG.md)
- 交接 / 架构 / 开发流程：[HANDOFF.md](HANDOFF.md)
- 第三方许可：[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) · 许可证：[LICENSE](LICENSE)

## 💖 支持

- 反馈：<https://github.com/zzy89216-gif/ZyNova/issues> · Discord：<https://discord.gg/QwPpZQHrTa>
- 赞助：<https://afdian.com/a/hizzy>

## ⚖️ 许可证

GPL-3.0，保持开源。第三方组件、各组件许可证与「你能怎么用」见
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) 与 [docs/zh/licenses.md](docs/zh/licenses.md)。

> ⚠️ 其中**陶瓦联机 Terracotta 是 AGPL-3.0**（比其他组件更严格）：本项目原样分发、未修改其二进制；
> 若你要修改后再分发，需按 AGPL-3.0 §13 公开对应源码。

---

**ZyNova — Context First. Less Steps.**
