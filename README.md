<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.0.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.0.1)

**简体中文** | [English](README_EN.md) | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md) | [한국어](README_KO.md) | [Русский](README_RU.md)

---

Android 上的 Minecraft: Java Edition 启动器，基于 ZalithLauncher2 源码自行维护发布，GPL-3.0。
目标是「少点几下」：玩家的操作尽量交给 AI 和自动化流程。

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ 28.0.1 更新要点

- **樱花内网穿透能用了**：`frpc -f 密钥:隧道ID`（官方参数，之前的写法樱花版 frpc 不认）；
  刷新/新建隧道不再被 `Unexpected JSON token` 顶回来；连接地址从日志里抽出来**置顶 + 一键复制**
- **返回键不再「一点就退出」**：顶栏返回不再借道系统返回键（原来是直接结束界面，只能重启启动器），
  一次只退一层，栈到底也不会被弹空
- **游戏内可以回启动器了**：游戏内菜单第一项「回到启动器主界面」**不停止游戏**；
  启动器底部会出现「返回游戏」胶囊，点一下回去还是原来那一局
- **建隧道不用懂端口/类型**：类型固定 TCP，端口自动从 `/proc/net/tcp` 里认（就是游戏「对局域网开放」那个），
  节点按账号等级过滤，免费号不会看到付费节点
- **AI 能全自动开联机**：`frp_status` / `frp_create_tunnel` / `frp_start_tunnel` / `frp_stop_tunnel` / `multiplayer_status`

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

- 用户文档中心（75 篇）：[docs/zh/README.md](docs/zh/README.md)
- 更新日志：[CHANGELOG.md](CHANGELOG.md)
- 交接 / 架构 / 开发流程：[HANDOFF.md](HANDOFF.md)
- 第三方许可：[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) · 许可证：[LICENSE](LICENSE)

## 💖 支持

- 反馈：<https://github.com/zzy89216-gif/ZyNova/issues> · Discord：<https://discord.gg/QwPpZQHrTa>
- 赞助：<https://afdian.com/a/hizzy>

## ⚖️ 许可证

GPL-3.0，保持开源。第三方组件见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。

---

**ZyNova — Context First. Less Steps.**
