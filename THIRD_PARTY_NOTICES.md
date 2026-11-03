# Third-Party Notices / 第三方组件声明

本文件列出 ZyNova Launcher（以下简称「本项目」）分发时随附的第三方代码与二进制，
以及各自的许可证与版权归属。

**每个组件都以其上游仓库中的实际 LICENSE 为准。本项目不把它们合并成同一个许可证，
也不改变任何上游声明。**

一句话结论：**本项目自身是 GPL-3.0**；内置渲染器分别属于
**MobileGlues = LGPL-2.1**、**GL4ES = MIT**、**LTW-Turbo = LGPL-3.0**。

---

## 1. 本项目自身

| 项目 | 许可证 | 版权 |
|---|---|---|
| ZyNova Launcher | GNU GPL-3.0（见仓库根目录 `LICENSE`） | 见 `LICENSE` 与各源文件头部 |

本项目的 GPL-3.0 文本中保留了上游项目的署名信息，未做删除或替换。
项目由 Zalith Launcher 2 / Fold Craft Launcher / PojavLauncher 一脉演化而来，
上游作者与贡献者的版权声明完整保留在源码头部与 `LICENSE` 中。

---

## 2. 内置渲染器

### 2.1 MobileGlues

| | |
|---|---|
| 上游 | https://github.com/MobileGL-Dev/MobileGlues |
| 发行版 | https://github.com/MobileGL-Dev/MobileGlues-release |
| 版本 | 2.0.1-dev（上游 ZalithLauncher2 2.8.1 内置的预发布构建） |
| 许可证 | **GNU LGPL-2.1**（含上游版权头 `Copyright (c) 2025-2026 MobileGL-Dev`）——完整文本见 `ZalithLauncher/src/main/res/raw/mobileglues_license.txt` |
| 随附形式 | 未经修改的预编译共享库 `libmobileglues.so`（arm64-v8a / armeabi-v7a / x86 / x86_64） |
| 二进制来源 | 上游 ZalithLauncher2 2.8.1 的 `ZalithLauncher/libs/MobileGlues-release.aar`（`jni/<abi>/libmobileglues.so`） |

**对应源码（LGPL-2.1 §6 要求）**：所分发的 `libmobileglues.so` 取自上述 AAR，
本项目**未做任何修改**。上游构建对应的 MobileGlues 源码为：

- 核心库：https://github.com/MobileGL-Dev/MobileGlues
- 对应提交：`97558a6915`（合并 `0f1e10b5` 的 multidraw grow-only 修复，2026-09-22）
- 分支：`main`（即 2.0.0 之后、尚未打正式 tag 的 2.0.1-dev 版本）

该库以独立动态库形式随 APK 分发（`lib/<abi>/libmobileglues.so`），
具备相应能力的用户可将其替换为自己编译的同名库，从而实现 LGPL 意义上的重新链接。
获取上述源码后，使用 Android NDK + CMake 构建 `MobileGlues-cpp` 即可得到同名库。

### 2.2 GL4ES

| | |
|---|---|
| 上游 | https://github.com/PojavLauncherTeam/gl4es_extra_extra |
| 版权 | Copyright © 2016-2018 Sebastien Chevalier；Copyright © 2013-2016 Ryan Hileman |
| 许可证 | **MIT** — 文本见 `ZalithLauncher/src/main/res/raw/gl4es_license.txt` |
| 随附形式 | 未经修改的预编译共享库 `libgl4es_114.so`（四个 ABI） |

### 2.3 LTW-Turbo（实验性）

| | |
|---|---|
| 上游 | https://github.com/gaowan-u/LTW-Turbo |
| 作者 / 维护者 | MathCode（gaowan-u） |
| 版本 | v1.0.3-preview.1（commit `8df9045`，`fix/render-and-minor-bugs` 分支） |
| 许可证 | **GNU LGPL-3.0** — 完整文本见 `ZalithLauncher/src/main/res/raw/ltw_turbo_license.txt` |
| 随附声明 | `ZalithLauncher/src/main/assets/licenses/ltw-turbo-CREDITS.md` |
| 随附形式 | 未经修改的预编译共享库 `libltw_turbo.so`（**仅 arm64-v8a**），取自上游 release 资产 `LTW.Turbo.v1.0.3-preview.1.apk` |

LTW-Turbo 是 LTW（Large Thin Wrapper）的维护分支：一个把桌面 OpenGL 实时翻译到
宿主 OpenGL ES 的兼容层，包含固定管线模拟与 GLSL 转换。原 LTW 团队为
artDev、SerpentSpirale、CADIndie；LTW-Turbo 由 MathCode 等维护。

**对应源码（LGPL-3.0 §4/§6 要求）**：本项目未修改 `libltw_turbo.so`；
对应源码即上述 GitHub 仓库的 `v1.0.3-preview.1` 对应提交。该库以独立动态库形式
随 APK 分发，用户可替换为自己编译的同名库。

---

## 3. 本项目其余第三方依赖

其余依赖（ANGLE、LWJGL、SDL3、MMKV、sora-editor、Terracotta、tm4e、XZ for Java …）
的许可证与版权信息可在应用内
**设置 → 关于 → 开源许可** 中逐项查看，各自文本均直接取自对应上游项目。
它们与第 2 节的渲染器组件是相互独立的部分，适用各自的许可证。

---

## 4. 修改说明

本仓库相对上游所做的、与第三方组件相关的改动：

1. **移除渲染器**：删除了内置的 Ironized Zink（及其承载的 Mesa / Zink / Kopper
   二进制）、Krypton Wrapper（NG-GL4ES）、Kopper Zink、VirGL、Freedreno、Panfrost，
   以及它们专属的 AAR、`.so` 与许可资源。被移除组件的许可证与署名不再随包分发，
   故不再列入应用内许可列表。**上游贡献者的署名信息没有从任何保留文件中被删除。**
2. **新增内置渲染器**：MobileGlues 由启动器直接内置，不再需要单独安装插件 APK；
   LTW-Turbo 以实验性渲染器形式内置（仅 arm64-v8a）。
3. **MobileGlues 配置接入**：只写入上游 native 真正读取的 `config.json` 键
   （OpenGL 版本、ANGLE、multidraw 顺序等），未修改其二进制。
4. **LTW 配置接入**：只注入上游 native 真正读取的环境变量，未修改其二进制。
5. **GL4ES 保持原样**，未做二进制层面的修改。

---

## 5. 商标

Minecraft 是 Mojang Synergies AB 的商标。本项目与 Mojang、Microsoft、Mesa 项目、
Fold Craft Launcher 团队、ZalithLauncher 团队、MobileGlues 团队、LTW-Turbo 团队
均无隶属关系，也未获得其背书。
