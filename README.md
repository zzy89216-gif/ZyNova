<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-27.10.2-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.10.2)

**简体中文** | [English](README_EN.md) | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md) | [한국어](README_KO.md) | [Русский](README_RU.md)

---

## ⚡ 开场白（认真的部分只有这句）

一个跑在 Android 上的 Minecraft: Java Edition 启动器，基于 ZalithLauncher2 源码开发，
自己维护、自己发布，GPL-3.0。**以下内容开始起飞，但每一条事实都能核对。**

## 🚌 它到底是什么玩意

想象一下：你把 MC 塞进手机，手机说「行」，然后递给你一个方向盘。
我们把这方向盘没收了。

**ZyNova 是你手机上的 MC 摆渡车。**

- 车票不要钱，行李不用你搬——模组、整合包、光影，它自己扛
- 司机是个 AI，会看路况、会修车、会在你骂它之前先把活干完
- 车上没有方向盘（不打算让你动手），也没有刹车（它相信自己）
- 唯一使用说明：**用中文说出你想干什么，然后躺好** ( ˘•ω•˘ )

> 它偶尔会自己走神，某个功能可能先它一步离开这个世界。
> 别慌，这不叫崩，这叫**版本迭代的代价** (｡•́︿•̀｡)
>
> 开摆之前先点一下「下载」——它会自己动的 (๑•̀ㅂ•́)و✧

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ 27.10.2 更新要点

- **个性化页**：壁纸、不透明度、背景模糊、卡片圆角、玻璃强度一页调完（顶部胶囊调色盘直达）
- **玻璃地基重做**：全应用只保留一层背景模糊，删除全部常驻流光动画；卡片统一为单色渐变 + 1px 亮边
- **AI 对话界面**：工具调用改为单行 `运行命令 · 概括`，原始输出默认收起；模型必须先写一句动作概括
- **Quilt 保护对齐 Fabric API**：Quilted / Legacy Fabric API 与 Fabric API 一样「搜不到、装不上、删不掉、禁不掉」
- **修复**：返回键无法返回上一页 / 闪屏、樱花内网穿透输入密钥直接报错、弹窗叠第二层模糊
- 本版本为**测试版（pre-release）**

## 🛠️ 它到底会什么（不是 PPT，是真能干）

- **AI Agent**：默认 DeepSeek。装游戏、装整合包、装模组、改设置、读日志、启动游戏——
  它不是给你写教程，它是直接把锅端走 (๑•̀ㅂ•́)و✧
- **渲染器**：自动 / MobileGlues / GL4ES / LTW（实验性），参数各回各屋，不再挤一个抽屉
- **资源核心**：Modrinth / CurseForge 搜索 → 依赖解析 → 下载 → 校验 → 安装，一条龙，中途不喊你
- **收藏与归档**：在线收藏 + 私有目录归档，SHA-256 去重，断网也能用
- **前置 API 防呆**：Fabric API / Quilted Fabric API / Legacy Fabric API 由安装流程自动加入，
  不可重复安装、搜不到、删不掉、也禁不掉。别问，问就是它自己会装

## 📦 下载与构建（求生指南）

- 下载请认准**最新那个**，别恋旧（上一个版本的故事足够拍三集）：
  <https://github.com/zzy89216-gif/ZyNova/releases>
- 想自己编：JDK 17 + Android SDK / NDK，然后
  `./gradlew ZalithLauncher:assembleRelease`
- 编不过？重启 Gradle。还编不过？清缓存。再编不过？那不是你的问题，那是**玄学** (╯°□°）╯︵ ┻━┻

## 📚 文档（这里是正经人待的地方）

> 📚 **完整文档中心（75 篇，中文）**：[docs/zh/README.md](docs/zh/README.md)



| 内容 | 文件 |
|---|---|
| 用户文档中心 | [docs/zh/README.md](docs/zh/README.md) |
| 安装 / 使用 / 导出 | [docs/zh/install.md](docs/zh/install.md) · [docs/zh/usage.md](docs/zh/usage.md) |
| 渲染器 / 登录 / 收藏 / AI | [docs/zh/renderers.md](docs/zh/renderers.md) · [docs/zh/login.md](docs/zh/login.md) · [docs/zh/favorites.md](docs/zh/favorites.md) · [docs/zh/ai-agent.md](docs/zh/ai-agent.md) |
| 故障排查 / FAQ / 数据 | [docs/zh/troubleshooting.md](docs/zh/troubleshooting.md) · [docs/zh/faq.md](docs/zh/faq.md) · [docs/zh/data-and-privacy.md](docs/zh/data-and-privacy.md) |
| 构建 | [docs/zh/build.md](docs/zh/build.md) |
| 更新日志 | [CHANGELOG.md](CHANGELOG.md) |
| 交接 / 架构 / 开发流程 | [HANDOFF.md](HANDOFF.md) |
| 第三方许可证 | [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) |
| 许可证 | [LICENSE](LICENSE) |

> 上面四份是**严肃文档**，按规矩写、只写事实。别指望在里面看见段子——
> 沙雕浓度只在本页生效，其他地方一个字都不许玩 ( ˘•ω•˘ )

## 💖 支持（老板里边请）

- 反馈问题：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- 赞助：<https://afdian.com/a/hizzy>
  ——钱会变成电费、域名、和更多 bug 的安葬费。谢谢老板 (｡•́︿•̀｡)

## ⚖️ 许可证

GPL-3.0，必须保持开源。第三方组件见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。
想拿去商用？先看看它，再想想 ( ˘•ω•˘ )

---

**ZyNova — Context First. Less Steps.**
