<p align="center"><sub>
<a href="README_EN.md">English</a> · <b>简体中文</b> · <a href="i18n/zh-Hant/README.md">繁體中文</a> · <a href="i18n/ja/README.md">日本語</a> · <a href="i18n/ko/README.md">한국어</a> · <a href="i18n/tr/README.md">Türkçe</a> · <a href="i18n/ru/README.md">Русский</a> · <a href="i18n/vi/README.md">Tiếng Việt</a> · <a href="i18n/th/README.md">ไทย</a> · <a href="i18n/de/README.md">Deutsch</a> · <a href="i18n/es/README.md">Español</a> · <a href="i18n/fr/README.md">Français</a><br>
<a href="i18n/uk/README.md">Українська</a> · <a href="i18n/pl/README.md">Polski</a> · <a href="i18n/pt-BR/README.md">Português (Brasil)</a> · <a href="i18n/ar/README.md">العربية</a> · <a href="i18n/fa/README.md">فارسی</a> · <a href="i18n/id/README.md">Bahasa Indonesia</a> · <a href="i18n/it/README.md">Italiano</a> · <a href="i18n/el/README.md">Ελληνικά</a> · <a href="i18n/ml/README.md">മലയാളം</a> · <a href="i18n/ur-Latn/README.md">Roman Urdu</a> · <a href="i18n/ro/README.md">Română</a> · <a href="i18n/bg/README.md">Български</a> · <a href="i18n/lzh/README.md">文言</a>
</sub></p>

<p align="center"><img src="assets/icon.png" width="120" alt="ZyNova"></p>

# ZyNova

**Android 上的 Minecraft: Java Edition 启动器** · 基于 ZalithLauncher2 自行维护发布 · GPL-3.0

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.3.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.3.0)
[![Docs](https://img.shields.io/badge/Docs-78%20%E7%AF%87-blue)](docs/zh/README.md)

设计只有一句：**少点几下**。先替你判断，判断不了再问一次；装游戏、装整合包、装模组、理依赖、读崩溃日志，都尽量交给 AI。

## 快速开始

1. 从 [Releases](https://github.com/zzy89216-gif/ZyNova/releases) 下载 APK（Android 8.0+）。
2. 首次启动：地区检查 → **允许「所有文件访问」**（游戏数据在公共目录 `/ZyNova`）→ 3 份条款各自滑到底并勾选 → 选年龄段。
3. 登录（微软 / LittleSkin / 离线）→ 选或让 AI 装一个版本 → 启动。

> 老用户升级会先走一次数据迁移：全屏进度，搬完点「下一步」。数据一直在 `/ZyNova/.Minecraft`，不会丢。

## 校验官方安装包

官方 Release 的 APK 由同一张证书长期签名，**证书 SHA-256 指纹跨版本不变**。想确认手里的包没被替换过，核对指纹即可：

- 装了 Android SDK build-tools（最可靠）：`apksigner verify --print-certs ZyNova-<版本>.apk`，看 `Signer #1 certificate SHA-256 digest` 一行；
- 只有 JDK：`keytool -printcert -jarfile ZyNova-<版本>.apk`，看 `SHA256:` 一行；若提示「不是已签名的 jar」，说明包只带 v2/v3 签名，改用上面的 `apksigner`；
- 官方签名证书的 SHA-256 指纹（比对这一串）：

  `8A:DA:D0:D5:A3:6A:B1:87:19:A2:33:18:46:A2:C3:37:31:23:CA:E8:B7:7C:86:B6:9F:55:6C:7A:34:2D:09:1D`

指纹对不上**就不要安装**，换回 [Releases](https://github.com/zzy89216-gif/ZyNova/releases) 重新下载。自行构建的包用自己的密钥签名，指纹自然不同。

> ⚠️ 这张证书对应上游公开的默认密钥（仓库里就能拿到），所以指纹只能帮你识别「被换成别家签名」的包，不能证明来源。下载始终以本仓库 [Releases](https://github.com/zzy89216-gif/ZyNova/releases) 为准。

## 能做什么

| | |
|---|---|
| **AI Agent** | 默认 DeepSeek：装游戏 / 整合包 / 模组、改设置、读日志、启动、控制音乐，53 个工具 |
| **一键安装** | 资源与整合包都能在列表里一步装完，自动解析并下载**必需前置** |
| **渲染器** | 自动 / MobileGlues / Ironized Zink / GL4ES / LTW（实验性，仅 arm64） |
| **收藏与归档** | 在线收藏 + 本地归档（SHA-256 去重），卡片上直接一键下载 |
| **音乐播放器** | 本地导入、列表播放，进游戏也能切歌；后台播放带通知栏控制 |
| **联机** | 陶瓦（P2P）/ 樱花内网穿透，一个页面切换，教程按真实流程写 |
| **数据** | `/storage/emulated/0/ZyNova/.Minecraft`（实例 / 存档 / 模组），日志在 `/ZyNova/logs` |

国内网络：按需走镜像，镜像缺文件自动回退官方；CurseForge 可能需镜像，Modrinth 通常直连。出错先换网络、关代理再试。

## 28.3.0 更新要点

**正式版。** 本版汇总此前 28.2.2 / 28.2.3 / 28.2.4 三个预发行版的全部内容（这些预发行 Release 已移除），
作为正式线发布；包含内置音乐播放器、渲染器自动选择、51→53 个 AI 工具，以及 19 个议题的修复。

### 修复与改进（#65–#83 汇总）

- **多任务并行下载 / 安装**（#78）：游戏本体、整合包、资源可以同时排多个，各自进度 / 速度 / 取消独立；
  重复点同一个目标会明确提示；Forge / NeoForge 的 JVM 安装阶段串行执行、共享文件写入加锁、跨入口同名安装有提示、
  导出与安装各用独立临时目录。
- **主页卡片拖动更稳**（#79）：不闪屏、不闪退，快速拖动逐张换位，支持横向拖动。
- **音乐进度条不再卡死**（#82）：「暂停 / 缓冲」不再让进度与时长永久停住，暂停也能看到位置，未知时长显示 `--:--`。
- **「返回游戏」不再残留**（#83）：游戏退出即收起；点击时当场校验，不再以空壳 `VMActivity` 崩溃。
- **删除版本不再像卡死**（#74）：全屏确定进度（文件数 / 大小 / 百分比 / 当前文件），可取消，取消尽量恢复原路径。
- **下载 / 安装弹窗全局化**（#72）：安装成功、整合包命名、权限提醒在任何页面都会弹出；离开下载页不取消、不挂起安装。
- **安装中断恢复**（#73）：安装写恢复记录；进程被杀后下次启动安全清理 / 回滚，不留半个 jar、不再永久「安装中」。
- **关于页更名「关于与插件」**（#71）、**多人联机页标题统一**（#68）、**关于页按钮等宽对齐**（#69 #70）、
  **收藏页顶部改版**（#76）、**游戏内提示卡不再拉满全屏宽**（#66）、**移除多余邮箱**（#67）。
- **渲染器列表不再出现两条「跟随全局」**（#65）。
- 任务 / 取消链路：取消全部任务不再废掉整个后台任务系统；取消不再被记成失败；「更新加载器」按钮误判修复。

### 🎵 音乐播放器（新增 + 增强）

- 导入本地音乐（支持 MP3 / AAC(M4A) / FLAC / OGG / Opus / WAV 等），列表播放、上一首 / 下一首、拖动进度、记住上次曲目与进度。
- 后台播放：前台服务 + 通知栏控制；被其他应用抢音频焦点时让位暂停，拔耳机自动暂停；游戏内面板可跨进程切歌、拖进度。
- **歌词**：同名 `.lrc` 自动加载（UTF-8 / UTF-16 / GBK），页面可「导入歌词」；当前行高亮 + 自动滚动；
  通知栏显示「上一行 + 当前行」；歌词按音频完整文件名保存（`晴天.mp3.lrc`），同名不同扩展名不再互相覆盖。
- **顶栏迷你播放器**：非音乐页且有当前曲目时，顶栏下方一条紧凑条（歌名 + 播放/暂停 + 切歌，点击进音乐页）。
- **界面更密集**：曲库行高收紧、当前曲高亮、切歌自动滚动；进度与时长常显；横屏 / 矮屏布局不再溢出。

### 🤖 AI

- **AI 联网搜索**：DeepSeek 官方搜索链路修复；不可用时不再静默——结果里写明原因并兜底 DuckDuckGo；
  也能让 AI 切换搜索引擎、配置 Key。
- **安装能力**：`install_game` 支持同时安装多个游戏本体，`install_status` 列出全部进行中的安装与最近结果。
- **音乐工具**：新增 `music_status` / `music_control`（查看播放状态、播放 / 暂停 / 切歌 / 跳进度 / 按曲名点播），工具总数 **51 → 53**。

### 🎨 渲染器

- **Ironized Zink 满血回归**（4 个官方预设 + 12 个高级开关，OpenGL 版本独立可选 3.3–4.6）。
- **自动选择按设备能力挑**：先判设备能不能跑（MobileGlues 需 GLES 3.2+、Ironized Zink 需 Vulkan），
  再按 MobileGlues → GL4ES → Ironized Zink 取第一个版本兼容的；手动选择不兼容时的回退也走同一套逻辑。
- 渲染器插件文档补全（来源、同名两条属正常、标识符冲突不显示）。

### 🧩 游戏内入口

- 菜单新增「多人联机」快捷入口：收起菜单、回启动器并直接打开「多人联机」页（陶瓦 / 樱花都在这一页），**游戏不退出**。

### ⬆️ 上游同步

- Android 8.x 游戏线程取剪贴板崩溃、自动选择 Java 运行时崩溃两条上游修复。

### 📄 文档

- 用户文档 78+78 篇、介绍页 25 种语言（新增文言）全部同步到 28.3.0 正式版。

## 文档与反馈

用户文档 [中文](docs/zh/README.md) · [English](docs/en/README.md) ｜ [更新日志](CHANGELOG.md) ｜ [第三方声明](THIRD_PARTY_NOTICES.md) ｜ [许可证](docs/zh/licenses.md) ｜ 开发与发版 [AGENTS.md](AGENTS.md) · [HANDOFF.md](HANDOFF.md)

反馈：[Issues](https://github.com/zzy89216-gif/ZyNova/issues) · [Discord](https://discord.gg/QwPpZQHrTa) · [X](https://x.com/ZyNovaProject) ｜ 邮箱：<ZyNovaLauncher@outlook.com> ｜ 赞助：<https://afdian.com/a/hizzy>

## 许可证

GPL-3.0。第三方组件的许可证与使用边界见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。
其中**陶瓦联机 Terracotta 是 AGPL-3.0**（更严格）：本项目原样分发、未改其二进制；若你修改后分发，需按 AGPL-3.0 §13 公开对应源码。

**ZyNova — Context First. Less Steps.**
