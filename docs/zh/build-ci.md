# CI 构建

- push 到 main：`build_apk.yml` 构建**通用版** APK（只有这一个 flavor）。
- 只改文档的 push **不会触发构建**：`paths-ignore` 覆盖了 `**.md`、`docs/**`、`LICENSE`、Issue 模板等；
  确实想跑一次就手动触发：Actions → **Build ZyNova APK** → *Run workflow*。
- Release：`release_ci.yml` 调用 `build.yml`，上传 `ZyNova-<版本>.apk` 与 `mapping.universal.zip`。
- 路径是 `apk/<flavor>/release/`，不是旧的 `apk/release/`。
