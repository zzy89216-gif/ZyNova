# CI 构建

- push 到 main：`build_apk.yml` 构建 universal + arm64。
- Release：`release_ci.yml` 调用 `build.yml`，上传 APK 与 mapping。
- 路径是 `apk/<flavor>/release/`，不是旧的 `apk/release/`。
