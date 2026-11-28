# 仓库结构

根目录只放「入口文档」和「工程入口」，其余内容按用途分类。**不要往根目录随手丢临时文件或第二份清单**。

## 入口文档

| 文件 | 用途 |
|---|---|
| [`README.md`](README.md) / [`README_EN.md`](README_EN.md) | 项目介绍、下载与校验、快速开始（中 / 英） |
| [`AGENTS.md`](AGENTS.md) | 接手必读：状态、发版流程、文档规则、历史决定 |
| [`HANDOFF.md`](HANDOFF.md) | 架构与历史：模块说明、每个版本做了什么 |
| [`CHANGELOG.md`](CHANGELOG.md) | 更新日志（按版本、分新增 / 变更 / 修复 / 说明） |
| [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md) | 第三方开源声明（另有 APK 内副本，两处必须同步） |
| [`LICENSE`](LICENSE) | GPL-3.0 许可证全文 |

## 应用与业务模块（Gradle 子项目）

| 目录 | 用途 |
|---|---|
| `ZalithLauncher/` | 主应用模块：启动器 UI、游戏管理、AI Agent、渲染器、联机等 |
| `ColorPicker/` | 颜色选择器组件（主题自定义用） |
| `InputMap/` | 输入映射组件（控制布局用） |
| `LayerController/` | 图层 / 视图控制组件 |
| `LWJGL/` | LWJGL 相关补丁与多版本模块 |
| `Terracotta/` | 陶瓦联机（AGPL-3.0，原样分发） |

## 工程与构建

| 文件 / 目录 | 用途 |
|---|---|
| `build.gradle.kts` / `settings.gradle.kts` | 根构建脚本与子项目声明 |
| `gradle/` | Wrapper 与版本目录（`libs.versions.toml`） |
| `gradlew` / `gradlew.bat` | Gradle Wrapper |
| `gradle.properties` | 根工程属性；**版本号不在这里**（见 `ZalithLauncher/gradle.properties`） |
| `.github/` | CI 工作流与 Issue 模板 |
| `.gitignore` | 忽略规则 |

## 文档与多语言

| 目录 | 用途 |
|---|---|
| `docs/zh/`、`docs/en/` | 用户文档，各 78 篇，文件名一一对应并登记在各自 `README.md` 索引 |
| `docs/README.md` | 文档中心入口（中英索引对照） |
| `i18n/` | 25 种语言的项目介绍页（`i18n/<语言>/README.md`）与语言索引 |

## 资源

| 目录 | 用途 |
|---|---|
| `assets/` | README 用的项目图标等图片资源 |

## 生成物（不提交）

`build/`、`.gradle/`、`local.properties`、签名文件密钥等由 `.gitignore` 忽略；不要提交构建产物。

## 放置约定

1. 新增用户文档：`docs/zh/` 与 `docs/en/` **成对新增**，并在两边 `README.md` 索引登记。
2. 新增语言介绍页：`i18n/<语言>/README.md`，并在 `i18n/README.md` 登记。
3. 新增模块：根目录建目录并在 `settings.gradle.kts` 声明，同时在 HANDOFF 记录用途。
4. 根目录**只放**入口文档与工程入口；临时脚本、草稿、第二份待办清单不要放进来。
5. 改动任何路径后，检查 `docs/`、`README` 与 `.github/` 中的引用，保证坏链为 0。
