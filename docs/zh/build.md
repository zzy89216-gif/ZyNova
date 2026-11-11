# 构建与发布

> 完整发布步骤、签名、Secrets、Release 流程以根目录 `HANDOFF.md` 为准；
> 这里只给日常构建入口。

## 本地构建

环境：JDK 17 + Android SDK / NDK。

```bash
./gradlew ZalithLauncher:assembleRelease
```

现在有两个 flavor：

- `universal`：4 个 ABI（唯一 flavor，不再有 `arm64` 版）。

产物路径：

```
ZalithLauncher/build/outputs/apk/universal/release/ZyNova-<版本>.apk
```

## CI

- push 到 `main`：`.github/workflows/build_apk.yml` 构建通用版 APK；
- 发布 Release：`.github/workflows/release_ci.yml` 调用 `build.yml` 上传 APK 与 mapping。

## 发布前

1. 改 `ZalithLauncher/gradle.properties` 的版本号（name + code）；
2. 同步 `CHANGELOG.md` / `HANDOFF.md` / `README*.md` / `docs/`；
3. 隐私扫描；
4. 等 CI 绿；
5. 按 HANDOFF 第十二节创建 Release。

> 已发布的 tag 不覆盖；修 Bug 必须升版本号。
