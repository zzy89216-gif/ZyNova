<p align="center"><sub>
<a href="README_EN.md">English</a> · <b>简体中文</b> · <a href="i18n/zh-Hant/README.md">繁體中文</a> · <a href="i18n/ja/README.md">日本語</a> · <a href="i18n/ko/README.md">한국어</a> · <a href="i18n/tr/README.md">Türkçe</a> · <a href="i18n/ru/README.md">Русский</a> · <a href="i18n/vi/README.md">Tiếng Việt</a> · <a href="i18n/th/README.md">ไทย</a> · <a href="i18n/de/README.md">Deutsch</a> · <a href="i18n/es/README.md">Español</a> · <a href="i18n/fr/README.md">Français</a><br>
<a href="i18n/uk/README.md">Українська</a> · <a href="i18n/pl/README.md">Polski</a> · <a href="i18n/pt-BR/README.md">Português (Brasil)</a> · <a href="i18n/ar/README.md">العربية</a> · <a href="i18n/fa/README.md">فارسی</a> · <a href="i18n/id/README.md">Bahasa Indonesia</a> · <a href="i18n/it/README.md">Italiano</a> · <a href="i18n/el/README.md">Ελληνικά</a> · <a href="i18n/ml/README.md">മലയാളം</a> · <a href="i18n/ur-Latn/README.md">Roman Urdu</a> · <a href="i18n/ro/README.md">Română</a> · <a href="i18n/bg/README.md">Български</a>
</sub></p>

<p align="center"><img src="assets/icon.png" width="120" alt="ZyNova"></p>

# ZyNova

**Android 上的 Minecraft: Java Edition 启动器** · 基于 ZalithLauncher2 自行维护发布 · GPL-3.0

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.2.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.2.0)
[![Docs](https://img.shields.io/badge/Docs-77%20%E7%AF%87-blue)](docs/zh/README.md)

设计只有一句：**少点几下**。先替你判断，判断不了再问一次；装游戏、装整合包、装模组、理依赖、读崩溃日志，都尽量交给 AI。

## 快速开始

1. 从 [Releases](https://github.com/zzy89216-gif/ZyNova/releases) 下载 APK（Android 8.0+）。
2. 首次启动：地区检查 → **允许「所有文件访问」**（游戏数据在公共目录 `/ZyNova`）→ 3 份条款各自滑到底并勾选 → 选年龄段。
3. 登录（微软 / LittleSkin / 离线）→ 选或让 AI 装一个版本 → 启动。

> 老用户升级会先走一次数据迁移：全屏进度，搬完点「下一步」。数据一直在 `/ZyNova/.Minecraft`，不会丢。

## 能做什么

| | |
|---|---|
| **AI Agent** | 默认 DeepSeek：装游戏 / 整合包 / 模组、改设置、读日志、启动，51 个工具 |
| **一键安装** | 资源与整合包都能在列表里一步装完，自动解析并下载**必需前置** |
| **渲染器** | 自动 / MobileGlues / GL4ES / LTW（实验性，仅 arm64） |
| **收藏与归档** | 在线收藏 + 本地归档（SHA-256 去重），卡片上直接一键下载 |
| **联机** | 陶瓦（P2P）/ 樱花内网穿透，一个页面切换，教程按真实流程写 |
| **数据** | `/storage/emulated/0/ZyNova/.Minecraft`（实例 / 存档 / 模组），日志在 `/ZyNova/logs` |

国内网络：按需走镜像，镜像缺文件自动回退官方；CurseForge 可能需镜像，Modrinth 通常直连。出错先换网络、关代理再试。

## 28.2.0 更新要点

- **收藏页简化**：删简介与下载量，下载按钮做大、取消收藏做小，点一下直接装（含必需前置）。
- **整合包一键下载**：搜索结果卡片 ♥ 旁直接下载最新版，版本列表每行也能一键装。
- **圆角更顺**：卡片圆角 = 设置值 × 1.2，不再有直线突然拐进圆弧的「急转弯」感。
- 已包含 28.1.x 期间的修复：运行中收回存储权限不再让数据「消失」（P0）、账号页崩溃、整合包后台静默安装、樱花刷新不弹错、主页卡片按实例隐藏、拖动不闪屏。

## 文档与反馈

用户文档 [中文](docs/zh/README.md) · [English](docs/en/README.md) ｜ [更新日志](CHANGELOG.md) ｜ [第三方声明](THIRD_PARTY_NOTICES.md) ｜ [许可证](docs/zh/licenses.md) ｜ 开发与发版 [AGENTS.md](AGENTS.md) · [HANDOFF.md](HANDOFF.md)

反馈：[Issues](https://github.com/zzy89216-gif/ZyNova/issues) · [Discord](https://discord.gg/QwPpZQHrTa) · [X](https://x.com/ZyNovaProject) ｜ 邮箱：<ZyNovaLauncher@163.com> · <ZyNovaLauncher@outlook.com> ｜ 赞助：<https://afdian.com/a/hizzy>

## 许可证

GPL-3.0。第三方组件的许可证与使用边界见 [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)。
其中**陶瓦联机 Terracotta 是 AGPL-3.0**（更严格）：本项目原样分发、未改其二进制；若你修改后分发，需按 AGPL-3.0 §13 公开对应源码。

**ZyNova — Context First. Less Steps.**
