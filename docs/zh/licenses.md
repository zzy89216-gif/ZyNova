# 开源许可与第三方声明

ZyNova 是开源软件。**你自己、你的朋友、任何拿到 APK 的人，都有权查看、修改、再分发它**——
这也是为什么这一页必须写清楚：哪些代码是谁的、按什么许可分发、你能拿它做什么。

## 一、本项目自身

| 项 | 内容 |
|---|---|
| 许可证 | **GNU GPL-3.0** |
| 全文 | 仓库根目录 [`LICENSE`](https://github.com/zzy89216-gif/ZyNova/blob/main/LICENSE)，应用内「设置 → 关于 → 开源许可」也可看 |
| 来源 | 基于 [ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2) 源码维护，ZyNova 是**非官方修改版** |
| 上游署名 | Zalith Launcher 2 / Fold Craft Launcher / PojavLauncher 一脉的版权声明**完整保留**在 `LICENSE` 与各源文件头部 |

GPL-3.0 的含义（说人话）：

- 你可以随便用、随便改、随便发给别人；
- 但你**发出去的版本也必须开源**（GPL-3.0），并且要带上许可证与版权声明；
- 你**不能**把它改成闭源再卖。

## 二、第三方组件：谁、什么许可

完整清单与逐项版权见仓库 [THIRD_PARTY_NOTICES.md](https://github.com/zzy89216-gif/ZyNova/blob/main/THIRD_PARTY_NOTICES.md)，
应用内「设置 → 关于 → 开源许可」里每一项都能点开看到许可证原文。

最需要你知道的四个：

| 组件 | 许可证 | 说明 |
|---|---|---|
| **MobileGlues**（渲染器） | LGPL-2.1 | 以独立 `.so` 分发、未修改；可替换为你自己编译的同名库 |
| **GL4ES**（渲染器） | MIT | 未修改 |
| **LTW-Turbo**（渲染器，实验性） | LGPL-3.0 | 未修改；仅 arm64-v8a |
| **frpc**（樱花穿透客户端） | Apache-2.0 | 樱花定制构建，未修改；不内置任何账号或密钥 |
| **Terracotta**（陶瓦联机） | **AGPL-3.0** ⚠️ | 见下一节，义务最重 |

## 三、陶瓦联机 = AGPL-3.0（唯一需要特别注意的）

`libterracotta.so` 遵循 **AGPL-3.0**，比 GPL-3.0 更严格：

- 分发（包括改过之后分发）必须附带许可证全文与版权声明 → 已内置，应用内可读；
- **如果你修改了它**，再分发时必须**公开你的修改源码**；
- 即使你只是**通过网络向别人提供**它的功能，也同样要提供源码。

本项目对它的处理：**原样打包、未做任何二进制修改**，只保留上游自带的接口代码。
所以「对应源码」就是上游仓库：<https://github.com/burningtnt/Terracotta>。
你不需要向 ZyNova 申请任何东西，直接按 AGPL-3.0 处理即可。

## 四、你可以怎么用

| 你想做的事 | 允许吗 |
|---|---|
| 自己装、自己玩 | ✅ 随便 |
| 改代码自己用 | ✅ 随便 |
| 把改过的版本发给朋友 | ✅ 可以，但要一起给源码和许可证 |
| 打包进自己的整合包 / 发行版 | ✅ 可以，同样要开源并保留署名 |
| 加广告 / 收费分发 | ⚠️ 可以收，但**代码仍必须开源**，且不能限制用户再分发 |
| 闭源商用 | ❌ 不允许 |

## 五、署名与商标

- Minecraft 是 Mojang Synergies AB 的商标；本项目与 Mojang、Microsoft **无隶属关系**。
- 与 ZalithLauncher、Fold Craft Launcher、PojavLauncher、MobileGlues、LTW-Turbo、
  Terracotta、樱花内网穿透 的团队**均无隶属关系**，也未获得其背书。
- 第三方组件的署名均已保留，未做删除或替换。

## 六、发现许可问题怎么办

如果你认为本项目某个组件的许可处理有问题（漏署名、许可不匹配、二进制来源不明），
请直接开议题：<https://github.com/zzy89216-gif/ZyNova/issues>。
这类问题优先级高于功能需求。
