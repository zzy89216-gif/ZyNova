# 更新日志 (Changelog)

本项目所有值得注意的变更都会记录在此文件中。格式遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/)。

## [2.5.1] - 2026-09-06

### 修复
- 修复一键安装时不自动下载前置依赖（前置模组）的问题
  - 原因是忽略了 Modrinth 依赖中作者指定的精确版本 `version_id`
  - 现在会优先使用作者指定的精确前置版本，确保前置依赖正确下载安装

## [2.5] - 2026-09-06

### 新增
- 液态玻璃效果（毛玻璃之上叠加动态流动高光，可在设置里开关）
- 一键安装模组/光影（自动选适配版本 + 自动装 REQUIRED 前置依赖）
- 应用改名 ZyNova Launcher（包名 com.zynova.launcher）

### 合规
- README 声明基于 ZalithLauncher2 且为非官方修改版
- 应用内「关于」页面补充非官方声明 + GPL-3.0 许可证入口
- 项目/社区链接指向 ZyNova 仓库

---

## 说明

- 本项目基于 [ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2)，遵循 **GPL-3.0** 许可证
- ZyNova 是 ZalithLauncher2 的非官方修改版本，并非官方发布
