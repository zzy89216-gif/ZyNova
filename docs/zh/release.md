# 发布 Release

完整步骤以 `HANDOFF.md` 第十二节为准：

1. 升版本号；
2. 同步文档；
3. CI 绿；
4. 创建 Release（触发自动构建 / 上传）；
5. 确认资产全部为 `uploaded`：`ZyNova-<版本>.apk`（通用）+ `ZyNova-<版本>-arm64-v8a.apk` + `mapping.universal.zip`。

已发布 tag 不覆盖。
