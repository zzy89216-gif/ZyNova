# 发布 Release

完整步骤以 `AGENTS.md` 第四节（发版流程与坑）为准：

1. 只改 `ZalithLauncher/gradle.properties`：`launcher_version_name`（APK 内版本号）+ `launcher_version_code`；
2. 同步文档：`CHANGELOG.md`、`AGENTS.md`、`HANDOFF.md`、`docs/`（中英成对）、`README.md` / `README_EN.md`；
3. 等 push 触发的构建变绿（只改文档的 push 不会触发编译）；
4. 创建 Release（`target_commitish` 用**完整 40 位 SHA**，勾 `prerelease` 表示测试版），
   这会触发发布构建并自动上传资产；
5. 确认两个资产都 `uploaded`：`ZyNova-<版本>.apk`（通用版，唯一 APK）+ `mapping.universal.zip`。

- **发行名可以带说明，APK 文件名只取数字部分**：版本名写成不带后缀的 `X.Y.Z`，发行名写作「X.Y.Z（测试版）」；
- 已发布的 tag 不覆盖、不改写；修 Bug 就升版本号重发；
- 删除旧 Release 前必须先等它的构建结束，否则发布工作流会把 Release 自动重建。
