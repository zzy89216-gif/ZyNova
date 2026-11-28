<p align="center"><sub>
<a href="README_EN.md">English</a> · <b>简体中文</b> · <a href="i18n/zh-Hant/README.md">繁體中文</a> · <a href="i18n/ja/README.md">日本語</a> · <a href="i18n/ko/README.md">한국어</a> · <a href="i18n/tr/README.md">Türkçe</a> · <a href="i18n/ru/README.md">Русский</a> · <a href="i18n/vi/README.md">Tiếng Việt</a> · <a href="i18n/th/README.md">ไทย</a> · <a href="i18n/de/README.md">Deutsch</a> · <a href="i18n/es/README.md">Español</a> · <a href="i18n/fr/README.md">Français</a><br>
<a href="i18n/uk/README.md">Українська</a> · <a href="i18n/pl/README.md">Polski</a> · <a href="i18n/pt-BR/README.md">Português (Brasil)</a> · <a href="i18n/ar/README.md">العربية</a> · <a href="i18n/fa/README.md">فارسی</a> · <a href="i18n/id/README.md">Bahasa Indonesia</a> · <a href="i18n/it/README.md">Italiano</a> · <a href="i18n/el/README.md">Ελληνικά</a> · <a href="i18n/ml/README.md">മലയാളം</a> · <a href="i18n/ur-Latn/README.md">Roman Urdu</a> · <a href="i18n/ro/README.md">Română</a> · <a href="i18n/bg/README.md">Български</a> · <a href="i18n/lzh/README.md">文言</a>
</sub></p>

<p align="center"><img src="assets/icon.png" width="120" alt="ZyNova"></p>

# ZyNova

**Android 上的 Minecraft: Java Edition 启动器** · 基于 ZalithLauncher2 自行维护发布 · GPL-3.0

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.4.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.4.0)
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
| **主页** | 最近游玩的 4 个世界 + 全部世界列表；自定义主页资讯卡片可增删、隐藏、拖动排序，断网不影响启动 |
| **联机** | 陶瓦（P2P）/ 樱花内网穿透，一个页面切换，教程按真实流程写 |
| **数据** | `/storage/emulated/0/ZyNova/.Minecraft`（实例 / 存档 / 模组），日志在 `/ZyNova/logs` |

国内网络：按需走镜像，镜像缺文件自动回退官方；CurseForge 可能需镜像，Modrinth 通常直连。出错先换网络、关代理再试。

## 28.4.0 更新要点

**28.4.0 正式版**：在 28.3.0 基础上改主页、删除「个性化」页并修一批问题；**界面视觉与 28.3.0 一致**，本版未包含动态玻璃 / 真折射 / 磁吸等视觉改动。

- **主页旧卡片移除**（#100）：删除主页中间「按实例分组」的旧世界卡片，世界统一走顶部「最近游玩 + 查看全部」。
- **主页改版**（#91）：顶部显示最近游玩的 4 个世界，点「查看全部」进入全部世界列表（搜索 / 排序 / 按实例与游戏版本筛选，可直接启动）；世界区下方新增「自定义主页」资讯卡片（Minecraft 新闻 / Java 版本更新 / Modrinth / CurseForge / Minecraft 社区活动），可添加、删除、隐藏、拖动排序，断网不影响启动。
- **移除「个性化」**：主菜单「个性化」页与顶栏调色盘入口一并删除；外观相关设置（主题、壁纸、圆角、玻璃、背景模糊等）仍在 **设置 → 启动器**。
- **修复删除版本**：删除进度改为真全屏（铺满屏幕、返回键不误触、可随时取消）。
- **修复渲染器文案**：高级设置里「渲染器详细参数」标题数量与实际不符（三个 → 四个）。
- **回退不稳定优化**：28.3.2 的列表缓存 / 插件异步加载等改动导致部分资源管理界面崩溃，本版已全部回退；卡顿优化后续另做。

旧版本内容（28.3.0 汇总的 28.2.2–28.2.4、音乐播放器、渲染器自动选择、AI 53 工具、#65–#83 修复）已全部包含，详见 [CHANGELOG.md](CHANGELOG.md)。

## 文档与反馈

用户文档 [中文](docs/zh/README.md) · [English](docs/en/README.md) ｜ [更新日志](CHANGELOG.md) ｜ [第三方声明](THIRD_PARTY_NOTICES.md) ｜ [许可证](docs/zh/licenses.md) ｜ 开发与发版 [AGENTS.md](AGENTS.md) · [HANDOFF.md](HANDOFF.md)

反馈：[Issues](https://github.com/zzy89216-gif/ZyNova/issues) · [Discord](https://discord.gg/dbHTApYNYJ) · [X](https://x.com/ZyNovaProject) ｜ 邮箱：<ZyNovaLauncher@outlook.com> ｜ 赞助：<https://afdian.com/a/hizzy>

## 许可证

GPL-3.0。第三方组件的许可证与使用边界见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。
其中**陶瓦联机 Terracotta 是 AGPL-3.0**（更严格）：本项目原样分发、未改其二进制；若你修改后分发，需按 AGPL-3.0 §13 公开对应源码。

**ZyNova — Context First. Less Steps.**
