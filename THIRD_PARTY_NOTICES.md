# Third-Party Notices / 第三方组件声明

本文件列出 ZyNova Launcher（以下简称「本项目」）分发时随附的第三方代码与二进制，
以及各自的许可证与版权归属。

**每个组件都以其上游仓库中的实际 LICENSE 为准，本项目不把它们合并成同一个许可证，也不改变任何上游声明。**

若你只想要一句话结论：**本项目自身是 GPL-3.0**；内置渲染器分别属于 **Ironized Zink = GPL-3.0**、**MobileGlues = LGPL-2.1**、**GL4ES = MIT**；而 Ironized Zink 所承载的渲染引擎来自 **Mesa**（其中的 **Zink** 驱动与 **Kopper** 窗口系统层也是 Mesa 的一部分）——主 Mesa / Gallium 代码为 **MIT**，GLX 客户端代码为 **SGI Free Software License B**，GL / GLX 头文件为 **Khronos**。

---

## 1. 本项目自身

| 项目 | 许可证 | 版权 |
|---|---|---|
| ZyNova Launcher | GNU GPL-3.0（见仓库根目录 `LICENSE`） | 见 `LICENSE` 与各源文件头部 |

本项目的 GPL-3.0 文本中保留了上游项目的署名信息，未做删除或替换。
项目由 Zalith Launcher 2 / Fold Craft Launcher / PojavLauncher 一脉演化而来，
上游作者与贡献者的版权声明完整保留在源码头部与 `LICENSE` 中。

### 1.1 Microsoft（正版）登录所使用的应用注册

正版登录使用 **ZyNova 自己的 Microsoft 应用注册**。作为背景说明：
**ZyNova 是 ZalithLauncher2 的非官方分支（fork），两者是不同项目、独立维护**；
26.4.0 / 26.4.1 曾借用上游 ZalithLauncher2 的应用注册。

| | 显示名称 | Client ID | 当前是否使用 |
|---|---|---|---|
| **ZyNova 自己的应用注册** | ZyNova Launcher | `7b66e168-f8cd-43fc-a52d-2e78dba189b0` | ✅ **当前使用** |
| **ZalithLauncher2 的应用注册（上游）** | ZalithLauncher | `（已移除）` | ❌ 不使用（26.4.0 / 26.4.1 曾使用） |

原因：**只有进入 Minecraft 应用程序允许名单的应用注册才能访问 Minecraft Services**，
否则会在 `login_with_xbox` 收到 `HTTP 403 Invalid app registration`。
Client ID 由仓库 Secret `OAUTH_CLIENT_ID` **在构建时注入**，
因此源码中不包含实际生效的 Client ID。

- Client ID 属于**公共标识**，会随 APK 一同分发，本身不是密钥
- 本项目**不创建、不使用、不保存任何 Client Secret**
- 用户微软账号的「已连接的应用」中会以上表中的**显示名称**出现
- 更换应用注册只需更换该 Secret 并重新构建，**无需改动任何代码**
- 仍然**只需要「公共客户端 + Client ID」**：不需要 Client Secret、不需要 Redirect URI、不需要 SHA-1

---

## 2. 内置渲染器

### 2.1 Ironized Zink

| | |
|---|---|
| 上游 | https://github.com/GoyDevv/IronizedZink |
| 作者 | GoyDevv |
| 版本 | 1.1.0（commit `88f7c2028defe513fd05f591e42a2253e842e4fe`） |
| 许可证 | **GNU GPL-3.0** — 完整文本见 `ZalithLauncher/src/main/res/raw/ironized_zink_license.txt` |
| 随附声明 | `ZalithLauncher/src/main/assets/licenses/ironized-zink-CREDITS.md`（上游 `CREDITS.md` 原文，未改动） |

Ironized Zink 自身是一个「打包 + 配置」项目，它把 Mesa 的 Zink Gallium 驱动与
Kopper 窗口系统层包装成一个渲染器。上游许可证全文与署名文件随 APK 一同分发。

### 2.2 MobileGlues

| | |
|---|---|
| 上游 | https://github.com/MobileGL-Dev/MobileGlues |
| 发行版 | https://github.com/MobileGL-Dev/MobileGlues-release |
| 版本 | 2.0.0（release tag `V2.0.0`，2026-08-09 发布；APK 资产 `MobileGlues_2.0.0.apk`） |
| 许可证 | **GNU LGPL-2.1**（含上游版权头 `Copyright (c) 2025-2026 MobileGL-Dev`）——完整文本见 `ZalithLauncher/src/main/res/raw/mobileglues_license.txt` |
| 随附形式 | 未经修改的预编译共享库 `libmobileglues.so`（四个 ABI） |
| 完整性核验 | 已与官方 release 内 `lib/*/libmobileglues.so` 做 **SHA-256 逐字节比对，四个 ABI 全部一致** |

**对应源码（LGPL-2.1 §6 要求）**：所分发的 `libmobileglues.so` 直接取自上游官方
release 的 `MobileGlues_2.0.0.apk`，**未经任何修改**。其对应源码与构建配方如下：

- 核心库源码：https://github.com/MobileGL-Dev/MobileGlues
- 渲染器插件打包工程（含固定核心库版本的 submodule）：https://github.com/MobileGL-Dev/MobileGlues-plugin
- 二进制来源（release）：https://github.com/MobileGL-Dev/MobileGlues-release/releases/tag/V2.0.0

若需要与该二进制逐位对应的源码，请按上述插件工程在 `plugin` 分支上的
`MobileGlues` 子模块提交检出（该子模块指向 MobileGlues 核心库的对应修订）。

本项目对该库**未做任何修改**（四 ABI 的 `libmobileglues.so` 与官方 release 内的同名文件
SHA-256 完全一致，可自行复核），因此不存在需要额外提供的修改内容。
该库以独立的动态库形式随 APK 分发（`lib/<abi>/libmobileglues.so`），
可由具备相应能力的用户替换为其自行编译的同名库，从而实现 LGPL 意义上的重新链接。
获取上述源码后，使用 Android NDK + CMake 构建 `MobileGlues-cpp` 即可得到同名库。

### 2.3 GL4ES

| | |
|---|---|
| 上游 | https://github.com/PojavLauncherTeam/gl4es_extra_extra |
| 版权 | Copyright © 2016-2018 Sebastien Chevalier；Copyright © 2013-2016 Ryan Hileman |
| 许可证 | **MIT** — 文本见 `ZalithLauncher/src/main/res/raw/gl4es_license.txt` |
| 随附形式 | 未经修改的预编译共享库 `libgl4es_114.so`（四个 ABI） |

> 说明：原内置的 NG-GL4ES（BZLZHH，MIT）渲染器已随本次裁剪被移除，
> 其 AAR、原生库与许可资源不再随本项目分发，因此其条目也不再出现在应用内许可列表中。

---

## 3. Ironized Zink 承载的渲染引擎：Mesa（含 Zink / Kopper）

| | |
|---|---|
| 项目 | Mesa 3-D Graphics Library — https://mesa3d.org / https://gitlab.freedesktop.org/mesa/mesa |
| 版权 | Copyright © 1999-2007 Brian Paul and the Mesa contributors，以及各组件各自作者 |
| 组件许可证 | **主 Mesa 代码 / Gallium 代码：MIT**；**GLX 客户端代码：SGI Free Software License B**；**扩展头文件（`include/GL/glext.h`、`include/GL/glxext.h`）：Khronos**；C11 线程模拟：Boost（宽松许可） |
| 随附形式 | 未经修改的预编译共享库：`libEGL_mesa.so`、`libglxshim.so`、`libglapi.so`、`libzink_dri.so`、`libcutils.so`（Mesa 23.0.4，四个 ABI） |
| 完整文本 | 仓库内：`ZalithLauncher/src/main/res/raw/mesa_licenses.txt`（随 APK 分发）；<br>APK 内另有一份由 `libs/kopper-zink-release.aar` 提供的 `assets/licenses/mesa-licenses.rst`（该文件只存在于 AAR 与成品 APK 中，**不在仓库工作树里**） |

> 依 Mesa 项目的要求：本软件不得被称作 “MesaGL”，它是 *Mesa* 或
> *The Mesa 3-D Graphics Library*。

这些 Android 预编译二进制来自 **AngelAuraMC 的 `mesa_zink_kopper` 构建**，
其来源与版权说明见上游 Ironized Zink 的 `CREDITS.md`（已随 APK 分发）。
本项目对这些二进制**未做任何修改**。

上述 Zink/Kopper 二进制亦为原内置「Kopper Zink」渲染器所用；本项目保留了它们，
并将其接入 Ironized Zink 的完整配置体系。

同一 Mesa 代码库产出的 **Turnip**（`libvulkan_freedreno.so`，Mesa 的 Freedreno Vulkan
驱动）同样以 MIT 许可随本项目分发，用于 Vulkan 驱动选择。

---

## 4. MobileGlues 的第三方组件

以下内容摘自上游 MobileGlues README 的 “Third-party components” 一节，逐一列出，
不做合并：

| 组件 | 许可证 | 上游 |
|---|---|---|
| SPIRV-Cross (KhronosGroup) | Apache License 2.0 | https://github.com/KhronosGroup/SPIRV-Cross |
| glslang (KhronosGroup) | Various Licenses | https://github.com/KhronosGroup/glslang |
| cJSON (DaveGamble) | MIT License | https://github.com/DaveGamble/cJSON |
| FidelityFX-FSR (AMD) | MIT License | https://github.com/GPUOpen-Effects/FidelityFX-FSR |
| Perfetto (Google) | Apache License 2.0 | https://github.com/google/perfetto |
| xxHash (Yann Collet) | BSD 2-Clause License | https://github.com/Cyan4973/xxHash |
| flat_hash_map (Malte Skarupke) | Boost Software License 1.0 | https://github.com/MobileGL-Dev/flat_hash_map |

---

## 5. 本项目其余第三方依赖

其余依赖（ANGLE、LWJGL、SDL3、MMKV、sora-editor、Terracotta、tm4e、XZ for Java …）
的许可证与版权信息可在应用内
**设置 → 关于 → 开源许可** 中逐项查看，各自文本均直接取自对应上游项目。
它们与第 2、3 节的渲染器组件是相互独立的部分，适用各自的许可证。

---

## 6. 修改说明

本仓库相对上游所做的、与第三方组件相关的改动：

1. **移除渲染器**：删除了内置的 Krypton Wrapper（NG-GL4ES）、Kopper Zink、
   VirGL、Freedreno、Panfrost 五个渲染器及其专属原生库
   （`libOSMesa_*.so`、`libvirgl*.so`、`NG-GL4ES-release.aar` 等）。
   被移除组件的许可证与署名不再随包分发，故不再列入应用内许可列表。
   **上游贡献者的署名信息没有从任何保留文件中被删除。**
2. **新增内置渲染器**：Ironized Zink 与 MobileGlues 现在由启动器直接内置，
   不再需要单独安装插件 APK。
3. **Ironized Zink 配置模型移植**：上游 `Presets.kt` 的参数集合、默认值与
   4 个官方预设被移植进本项目（GPL-3.0 → GPL-3.0），被修改的文件均在
   文件头注明来源与修改说明。上游把配置写入 `/sdcard/IronizedZink/ironized.env`
   并由 `libironized_zink.so` 在游戏进程内 `setenv()`；本项目改为由启动器
   直接把同一组环境变量注入游戏进程，因此不再内置该 shim，也不需要存储权限。
4. **MobileGlues 与 Mesa/Zink/Kopper 的原生库保持原样**，未做二进制层面的修改。
   - MobileGlues：四 ABI 的 `libmobileglues.so` 与上游官方 release 内同名文件 SHA-256 一致
   - Mesa/Zink/Kopper：`libEGL_mesa.so` / `libglxshim.so` / `libglapi.so` / `libzink_dri.so` /
     `libcutils.so` 与 `libs/kopper-zink-release.aar` 内的二进制一致（未重新编译、未打补丁）

---

## 7. 商标

Minecraft 是 Mojang Synergies AB 的商标。本项目与 Mojang、Microsoft、Mesa 项目、
Fold Craft Launcher 团队、ZalithLauncher 团队均无隶属关系，也未获得其背书。
