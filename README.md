<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-27.8.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.8.1)

**简体中文** | [English](README_EN.md) | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md) | [한국어](README_KO.md) | [Русский](README_RU.md)

---

## 这是啥

一个跑在 Android 上的 Minecraft: Java Edition 启动器，基于 ZalithLauncher2 源码开发，
自己维护、自己发布，血缘清楚，GPL-3.0，没打算藏着。

说人话：**这是你手机上的 MC 摆渡车。**

- 票不用买，行李不用自己搬（模组、整合包、光影它替你搬）
- 司机是个 AI，会自己看路况、自己修车——你只要说一句「走」
- 车上没有方向盘，因为它不打算让你动手；也没有刹车，因为它相信自己
- 唯一的使用说明：想干什么，用中文说出来，然后等着 ( ˘•ω•˘ )

> 真出问题也别慌，多半是某个功能它自己先走了；我们这边已经习惯了。
> 开摆之前先点一下「下载」，它会自己动的 (๑•̀ㅂ•́)و✧

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ 27.8.1 更新要点

- **点下载终于有反应了**：以前那条顶部小进度条一闪都不闪——因为它比安装任务先跑，
  看到「还没开始」就理解成「已经结束」，然后心安理得地下班了。现在它得等活儿真干完才能走。
- **详细面板不再自己蹦出来**：想看点一下那条进度条，不看就收着；任务全干完它会自己缩回去。
- **动画不排队了**：设置页以前十几张卡片一个接一个入场，越往下越晚，看着零碎、滑着肉。
  现在统一节奏，快得像有人在后头催。
- **27.8.0 的大活**（这版都还有）：AI 能装游戏本体 / 装整合包 / 装资源 / 改实例配置，
  默认「完全控制」，说一句它就动手；NeoForge 的镜像也修了，不再动不动下载失败、一直卡。

## 🛠️ 能干啥

- **AI Agent**：默认 DeepSeek，装游戏本体 / 装整合包 / 装资源 / 改配置 / 读日志 / 启动游戏，
  不是给你写教程，是直接上手干活
- **渲染器**：自动 / MobileGlues / Ironized Zink / GL4ES，参数各回各屋，不再挤一个抽屉
- **资源核心**：Modrinth / CurseForge 搜索、依赖解析、下载、校验、安装，一条流水线
- **收藏与归档**：在线收藏 + 私有目录归档，SHA-256 去重，离线也能用
- **Fabric API 防呆**：只由 Fabric 流程自动加入，不可重复安装（别问，问就是它自己会装）

## 📦 下载与构建

- 下载去 Release 页面挑最新的那个，别挑上一个（上一个的故事很长）：
  <https://github.com/zzy89216-gif/ZyNova/releases>
- 想自己编译：JDK 17 + Android SDK / NDK，然后
  `./gradlew ZalithLauncher:assembleRelease`
  编不过就重启一次 Gradle，这不叫修，这叫玄学 (╯°□°）╯︵ ┻━┻

## 📚 文档

| 内容 | 文件 |
|---|---|
| 更新日志 | [CHANGELOG.md](CHANGELOG.md) |
| 交接 / 架构 / 开发流程 | [HANDOFF.md](HANDOFF.md) |
| 第三方许可证 | [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) |
| 许可证 | [LICENSE](LICENSE) |

> 上面这四份是严肃文档，按规矩写的，别指望在里面看到段子。

## 💖 支持

- 反馈问题：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- 赞助：<https://afdian.com/a/hizzy>
  （钱会变成电费、域名和更多 bug 的修复预算，谢谢老板 (｡•́︿•̀｡)）

## ⚖️ 许可证

GPL-3.0，必须保持开源。第三方组件见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。

---

**ZyNova — Context First. Less Steps.**
