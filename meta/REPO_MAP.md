# 仓库结构

根目录只保留「入口文件」和「工程入口」；源码、文档、维护资料都放在二级目录里。

## 根目录（一级）

| 条目 | 用途 |
|---|---|
| [`README.md`](../README.md) / [`README_EN.md`](../README_EN.md) | 项目介绍、下载与校验、快速开始（中 / 英） |
| [`AGENTS.md`](../AGENTS.md) | 接手必读：状态、发版流程、文档规则、历史决定 |
| `build.gradle.kts` / `settings.gradle.kts` | 根构建脚本与子项目声明 |
| `gradle.properties` / `gradle/` / `gradlew` / `gradlew.bat` | 根工程属性与 Gradle Wrapper |
| `LICENSE` | GPL-3.0 许可证全文 |
| `.github/` | CI 工作流与 Issue 模板（`.gitignore` 为忽略规则） |
| `docs/` | 用户文档、文档中心与图片资源（见下） |
| `i18n/` | 25 种语言的项目介绍页（见下） |
| `meta/` | 维护资料：变更日志、架构历史、第三方声明、本文件 |
| `modules/` | 全部 Gradle 源码模块（见下） |

## 维护资料 `meta/`

| 文件 | 用途 |
|---|---|
| [`CHANGELOG.md`](CHANGELOG.md) | 更新日志（按版本、分新增 / 变更 / 修复 / 说明） |
| [`HANDOFF.md`](HANDOFF.md) | 架构与历史：模块说明、每个版本做了什么 |
| [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md) | 第三方开源声明（另有 APK 内副本，两处必须同步） |
| [`REPO_MAP.md`](REPO_MAP.md) | 本文件：仓库结构与放置约定 |

## 源码模块 `modules/`（Gradle 子项目）

| 目录 | 用途 |
|---|---|
| `modules/ZalithLauncher/` | 主应用模块：启动器 UI、游戏管理、AI Agent、渲染器、联机等 |
| `modules/ColorPicker/` | 颜色选择器组件（主题自定义用） |
| `modules/InputMap/` | 输入映射组件（控制布局用） |
| `modules/LayerController/` | 图层 / 视图控制组件 |
| `modules/LWJGL/` | LWJGL 相关补丁与多版本模块 |
| `modules/Terracotta/` | 陶瓦联机（AGPL-3.0，原样分发） |

## 文档 `docs/`

| 目录 / 文件 | 用途 |
|---|---|
| `docs/zh/`、`docs/en/` | 用户文档，各 78 篇，文件名一一对应并登记在各自 `README.md` 索引 |
| `docs/README.md` | 文档中心入口（中英索引对照） |
| `docs/assets/` | README 与文档使用的图标、赞助码等图片资源 |

## 多语言 `i18n/`

| 目录 / 文件 | 用途 |
|---|---|
| `i18n/<语言>/README.md` | 各语言的项目介绍页（23 种语言目录） |
| `i18n/README.md` | 语言索引 |

## 生成物（不提交）

`build/`、`modules/*/build/`、`.gradle/`、`local.properties`、签名密钥等由 `.gitignore` 忽略；不要提交构建产物。

## 放置约定

1. 新增用户文档：`docs/zh/` 与 `docs/en/` **成对新增**，并在两边 `README.md` 索引登记。
2. 新增语言介绍页：`i18n/<语言>/README.md`，并在 `i18n/README.md` 登记。
3. 新增源码模块：放到 `modules/` 下，在 `settings.gradle.kts` 声明 `projectDir`，并在
   `meta/HANDOFF.md` 记录用途。
4. 维护资料（变更日志 / 架构历史 / 第三方声明 / 本结构说明）只放 `meta/`；根目录不再新增同类文件。
5. 图片资源只放 `docs/assets/`，文档与 README 用相对路径引用。
6. 改动任何路径后，检查 `docs/`、`README`、`meta/` 与 `.github/` 中的引用，保证坏链为 0、CI 路径同步。
