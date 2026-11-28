# Third-Party Notices / 第三方组件声明

本文件列出 ZyNova Launcher（以下简称「本项目」）分发时随附的第三方代码与二进制，
以及各自的许可证与版权归属。

**每个组件都以其上游仓库中的实际 LICENSE 为准。本项目不把它们合并成同一个许可证，
也不改变任何上游声明。**

一句话结论：

- **本项目自身：GPL-3.0**（见仓库根目录 `LICENSE`）
- **内置渲染器**：Ironized Zink = **GPL-3.0**、MobileGlues = **LGPL-2.1**、GL4ES = **MIT**、LTW-Turbo = **LGPL-3.0**
- **Ironized Zink 承载的渲染引擎**：Mesa（其中的 **Zink** 驱动与 **Kopper** 窗口系统层）——
  主 Mesa / Gallium 代码为 **MIT**，GLX 客户端代码为 **SGI Free Software License B**，
  GL / GLX 头文件为 **Khronos**
- **内网穿透客户端**：frp / frpc（樱花定制构建）= **Apache-2.0**
- **陶瓦联机 Terracotta（含 `libterracotta.so`）= AGPL-3.0** ⚠️
  AGPL-3.0 比 GPL-3.0 更严格：**重新分发时（含修改版）需要按 AGPL-3.0 §13 提供完整对应源码**，
  即使只通过网络提供服务也一样。完整文本见 `modules/Terracotta/src/main/res/raw/terracotta_license.txt`，
  也可在应用内「设置 → 关于 → 开源许可 → Terracotta」里查看。

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
| 许可证 | **GNU LGPL-2.1**（含上游版权头 `Copyright (c) 2025-2026 MobileGL-Dev`）——完整文本见 `modules/ZalithLauncher/src/main/res/raw/mobileglues_license.txt` |
| 随附形式 | 未经修改的预编译共享库 `libmobileglues.so`（arm64-v8a / armeabi-v7a / x86 / x86_64） |
| 二进制来源 | 上游 ZalithLauncher2 2.8.1 的 `modules/ZalithLauncher/libs/MobileGlues-release.aar`（`jni/<abi>/libmobileglues.so`） |

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
| 许可证 | **MIT** — 文本见 `modules/ZalithLauncher/src/main/res/raw/gl4es_license.txt` |
| 随附形式 | 未经修改的预编译共享库 `libgl4es_114.so`（四个 ABI） |

### 2.3 LTW-Turbo（实验性）

| | |
|---|---|
| 上游 | https://github.com/gaowan-u/LTW-Turbo |
| 作者 / 维护者 | MathCode（gaowan-u） |
| 版本 | v1.0.3-preview.1（commit `8df9045`，`fix/render-and-minor-bugs` 分支） |
| 许可证 | **GNU LGPL-3.0** — 完整文本见 `modules/ZalithLauncher/src/main/res/raw/ltw_turbo_license.txt` |
| 随附声明 | `modules/ZalithLauncher/src/main/assets/licenses/ltw-turbo-CREDITS.md` |
| 随附形式 | 未经修改的预编译共享库 `libltw_turbo.so`（**仅 arm64-v8a**），取自上游 release 资产 `LTW.Turbo.v1.0.3-preview.1.apk` |

LTW-Turbo 是 LTW（Large Thin Wrapper）的维护分支：一个把桌面 OpenGL 实时翻译到
宿主 OpenGL ES 的兼容层，包含固定管线模拟与 GLSL 转换。原 LTW 团队为
artDev、SerpentSpirale、CADIndie；LTW-Turbo 由 MathCode 等维护。

**对应源码（LGPL-3.0 §4/§6 要求）**：本项目未修改 `libltw_turbo.so`；
对应源码即上述 GitHub 仓库的 `v1.0.3-preview.1` 对应提交。该库以独立动态库形式
随 APK 分发，用户可替换为自己编译的同名库。

### 2.4 Ironized Zink（内置）

| | |
|---|---|
| 上游 | https://github.com/GoyDevv/IronizedZink |
| 作者 | GoyDevv |
| 版本 | 1.1.0（commit `88f7c2028defe513fd05f591e42a2253e842e4fe`） |
| 许可证 | **GNU GPL-3.0** — 完整文本见 `modules/ZalithLauncher/src/main/res/raw/ironized_zink_license.txt` |
| 随附声明 | `modules/ZalithLauncher/src/main/assets/licenses/ironized-zink-CREDITS.md`（上游 `CREDITS.md` 原文，未改动） |
| 随附形式 | 配置 / 预设代码移植进启动器（`game/renderer/ironizedzink/`），渲染引擎二进制由 `modules/ZalithLauncher/libs/kopper-zink-release.aar` 提供 |

Ironized Zink 自身是一个「打包 + 配置」项目，把 Mesa 的 Zink Gallium 驱动与
Kopper 窗口系统层包装成一个渲染器；上游许可证全文与署名文件随 APK 一同分发。
本项目只把它的配置模型（环境变量 / 四个官方预设）接入启动器设置，
**未修改**上述二进制。

### 2.5 Ironized Zink 承载的渲染引擎：Mesa（含 Zink / Kopper）

| | |
|---|---|
| 项目 | Mesa 3-D Graphics Library — https://mesa3d.org / https://gitlab.freedesktop.org/mesa/mesa |
| 版权 | Copyright © 1999-2007 Brian Paul and the Mesa contributors，以及各组件各自作者 |
| 组件许可证 | **主 Mesa 代码 / Gallium 代码：MIT**；**GLX 客户端代码：SGI Free Software License B**；**扩展头文件（`include/GL/glext.h`、`include/GL/glxext.h`）：Khronos**；C11 线程模拟：Boost（宽松许可） |
| 随附形式 | 未经修改的预编译共享库：`libEGL_mesa.so`、`libglxshim.so`、`libglapi.so`、`libzink_dri.so`、`libcutils.so`（Mesa 23.0.4，四个 ABI，由 `libs/kopper-zink-release.aar` 打包） |
| 完整文本 | 仓库内：`modules/ZalithLauncher/src/main/res/raw/mesa_licenses.txt`（随 APK 分发）；<br>APK 内另有一份由 `libs/kopper-zink-release.aar` 提供的 `assets/licenses/mesa-licenses.rst`（该文件只存在于 AAR 与成品 APK 中，**不在仓库工作树里**） |

> 依 Mesa 项目的要求：本软件不得被称作 “MesaGL”，它是 *Mesa* 或
> *The Mesa 3-D Graphics Library*。

这些 Android 预编译二进制来自 **AngelAuraMC 的 `mesa_zink_kopper` 构建**，
其来源与版权说明见上游 Ironized Zink 的 `CREDITS.md`（已随 APK 分发）。
本项目对这些二进制**未做任何修改**。

同一 Mesa 代码库产出的 **Turnip**（`libvulkan_freedreno.so`，Mesa 的 Freedreno Vulkan
驱动）同样以 MIT 许可随本项目分发，用于 Vulkan 驱动选择。

---

## 3. 客户端组件

### 3.1 frp（樱花内网穿透的 frpc 客户端）

| | |
|---|---|
| 上游 | https://github.com/fatedier/frp |
| 分发者 | SakuraFrp（https://www.natfrp.com/），使用其定制构建 |
| 版本 | `0.51.0-sakura-15.1`（构建于 2026-10-24，commit `cbfb558`） |
| 许可证 | **Apache License 2.0** — 完整文本见 `modules/ZalithLauncher/src/main/res/raw/apache_license_2.txt` |
| 随附形式 | 未经修改的预编译可执行文件 `libfrpc.so`（arm64-v8a / armeabi-v7a / x86 / x86_64），由启动器在本地启动 |
| 二进制来源 | SakuraFrp 官方客户端清单 `https://api.natfrp.com/v4/system/clients` 的 `frpc.archs` 对应架构下载地址 |

`frpc` 仅作为「把本机端口映射到公网」的客户端使用：不修改其二进制，不改动其协议，
不内置任何账号或密钥——访问密钥由用户自行填写，并以 Android Keystore 加密保存在本机。

**对应源码**：SakuraFrp 分发的 frpc 基于 frp 定制构建（加入其节点协议与 DKC 加密等），
二进制本身未做修改；frp 上游源码见上述仓库，SakuraFrp 的客户端下载与说明见
<https://doc.natfrp.com/frpc/usage.html>。

---

---

## 4. 陶瓦联机 Terracotta（AGPL-3.0）

| | |
|---|---|
| 上游 | https://github.com/burningtnt/Terracotta |
| 版权 | Copyright © 2025 Burning_TNT |
| 许可证 | **GNU AGPL-3.0** — 完整文本见 `modules/Terracotta/src/main/res/raw/terracotta_license.txt` |
| 随附形式 | 独立的 Gradle 模块 `Terracotta`：预编译动态库 `libterracotta.so`（arm64-v8a / armeabi-v7a / x86 / x86_64）+ 该库自带的 VPN 前台服务与 JNI 绑定代码 |
| 二进制是否修改 | **未修改**（直接使用上游构件） |

**为什么单独列出来**：AGPL-3.0 是本项目内义务最重的许可证。它要求：

1. 分发时必须附带许可证全文与版权声明 —— 已内置在上述路径并在应用内「开源许可」中可读；
2. **修改后再分发**（或通过网络向他人提供服务）时，必须提供**对应源码**；
3. 不允许附加额外限制。

本项目的做法是：`libterracotta.so` 原样分发、不做任何二进制修改，
`Terracotta` 模块中只有上游自带的接口代码；因此「对应源码」就是上述上游仓库。
如果你要修改它，请同时修改上游仓库并公开你的修改。

> 该原生库内部若还包含上游自己在发布时声明的组网等第三方代码，
> 其署名与许可以 Terracotta 上游发布内容为准；本项目未删除、未替换任何上游声明。

---

## 5. 本项目其余第三方依赖

其余依赖（ANGLE、LWJGL、SDL3、MMKV、sora-editor、Terracotta、tm4e、XZ for Java …）
的许可证与版权信息可在应用内
**设置 → 关于 → 开源许可** 中逐项查看，各自文本均直接取自对应上游项目。
它们与第 2、3、4 节的组件是相互独立的部分，适用各自的许可证。

---

## 6. 修改说明

本仓库相对上游所做的、与第三方组件相关的改动：

1. **移除渲染器**：删除了 Krypton Wrapper（NG-GL4ES）、Kopper Zink（已被 Ironized Zink
   接管同一套 UUID 与二进制）、VirGL、Freedreno、Panfrost，以及它们专属的 AAR、`.so`
   与许可资源。被移除组件的许可证与署名不再随包分发，故不再列入应用内许可列表。
   **上游贡献者的署名信息没有从任何保留文件中被删除。**
   Ironized Zink 曾于 27.9.0 被一并移除，**现已恢复内置**（渲染器实现、四个官方预设、
   完整参数页、`kopper-zink-release.aar`、Mesa 许可与 CREDITS 全部回到包内）。
2. **新增内置渲染器**：MobileGlues 由启动器直接内置，不再需要单独安装插件 APK；
   LTW-Turbo 以实验性渲染器形式内置（仅 arm64-v8a）。
3. **MobileGlues 配置接入**：只写入上游 native 真正读取的 `config.json` 键
   （OpenGL 版本、ANGLE、multidraw 顺序等），未修改其二进制。
4. **LTW 配置接入**：只注入上游 native 真正读取的环境变量，未修改其二进制。
5. **GL4ES 保持原样**，未做二进制层面的修改。

---

## 7. 商标

Minecraft 是 Mojang Synergies AB 的商标。本项目与 Mojang、Microsoft、Mesa 项目、
Fold Craft Launcher 团队、ZalithLauncher 团队、MobileGlues 团队、LTW-Turbo 团队
均无隶属关系，也未获得其背书。
