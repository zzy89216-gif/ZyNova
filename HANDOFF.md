# ZyNova 项目交接文档（Handoff）

> 本文档供任何接手者（人类或 AI）快速理解项目现状并继续开发。请先完整阅读再动手。

---

## 一、项目概述

- **项目名**：ZyNova（全名 ZyNova Launcher）
- **性质**：基于 [ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2) 开源代码开发的 **非官方修改版** Minecraft: Java Edition Android 启动器
- **许可证**：GPL-3.0（上游也是 GPL-3.0，ZyNova 必须保持开源）
- **GitHub 仓库**：https://github.com/zzy89216-gif/ZyNova （分支 `main`）
- **包名**：`com.zynova.launcher`（namespace 仍为 `com.movtery.zalithlauncher`，不要改 namespace，否则要改几百个文件的 package 声明）
- **目标用户**：Android 手机玩 Minecraft Java 版的玩家

---

## 二、当前进度总览

### 已完成 ✅
1. **液态玻璃效果**：毛玻璃之上叠加两条流动高光带，模拟 iOS 26 液态玻璃。
   - 实现：`LauncherElements.kt` 的 `liquidGlassHighlights()` 函数
   - 设置项：`AllSettings.liquidGlass`（布尔开关）
   - 设置界面：`LauncherSettingsScreen.kt`「启动器背景」板块的「液态玻璃效果」开关
2. **一键安装模组/光影**：版本列表项加「一键安装」按钮，自动选最新适配版本 + 递归装 REQUIRED 前置依赖 + 直接装进游戏。
   - 核心实现：`game/download/assets/_Download.QuickInstall.kt` 的 `quickInstallAsset()` 函数
   - UI：`DownloadAssetsElements.kt` 版本列表项的下载图标按钮
   - 入口：Mod/Shaders/ResourcePack/Saves 四个 Screen 已接线
3. **应用改名**：ZyNova / ZyNova Launcher / com.zynova.launcher
4. **合规整改**：README 声明、About 页非官方声明、GPL-3.0 全文进 APK、项目链接指向 ZyNova 仓库
5. **隐私清除**：token、密钥、硬编码密码已全部清除（详见「红线」章节）

### 进行中 ⏳
- GitHub Actions 编译 Release arm64 APK（首次编译成功后，后续会更快）

### 待办 ⬜
1. **Boat 后端双端**：用户想接入 Boat 后端（像老版 FCL 一样），目前**未开始**，是最大的待办。
2. **AI 助手**：接入 OpenAI V1 接口，自动加模组写配置，**未开始**。
3. **更新检查源**：`UrlManager.kt` 的 `URL_PROJECT_INFO` 仍指向上游 Zalith-Info，需要 ZyNova 自己的版本信息服务器才能改（详见「注意事项」第 5 条）。
4. **键位优化、渲染优化**：用户提过，未深入做。

---

## 三、代码位置

| 位置 | 说明 |
|---|---|
| `/sdcard/Download/DSHA/工作区/ZalithLauncher2` | 源码工作区（用户可见，但 git 历史已重建过） |
| `/root/zyNova_git` | **推送副本**（ext4 分区，git 干净，是推送到 GitHub 的版本，以后在这里改+提交+推送） |
| `/root/zyNova` | 旧的本地构建副本（可能过期，不再用） |

⚠️ **重要**：因为手机 sdcard 是 noexec/fuse 分区，git add/编译在 sdcard 上会崩溃（SIGSEGV）。**所有 git 操作和编译都在 `/root/zyNova_git`（ext4）进行**。工作区只是备份参考。

---

## 四、关键文件清单

### 我改过/新增的文件
| 文件 | 改动内容 |
|---|---|
| `ZalithLauncher/src/main/java/.../ui/screens/content/elements/LauncherElements.kt` | 液态玻璃高光动效 |
| `ZalithLauncher/src/main/java/.../setting/AllSettings.kt` | 新增 `liquidGlass` 设置项 |
| `ZalithLauncher/src/main/java/.../ui/screens/content/settings/LauncherSettingsScreen.kt` | 液态玻璃开关 UI |
| `ZalithLauncher/src/main/java/.../game/download/assets/_Download.QuickInstall.kt` | **新增**，一键安装核心逻辑 |
| `ZalithLauncher/src/main/java/.../download/assets/elements/_Download.Single.kt` | 新增 `QuickInstall` 状态 |
| `ZalithLauncher/src/main/java/.../download/assets/elements/DownloadAssetsElements.kt` | 版本项加一键安装按钮 |
| `ZalithLauncher/src/main/java/.../download/assets/download/DownloadAssetsScreen.kt` | 透传 onQuickInstall |
| `.../download/DownloadModScreen.kt` / `DownloadShadersScreen.kt` / `DownloadResourcePackScreen.kt` / `DownloadSavesScreen.kt` | 一键安装接线 |
| `.../assetinfo/AssetInfoScreen.kt` | 一键安装接线 |
| `.../path/UrlManager.kt` | URL_PROJECT/URL_COMMUNITY 指向 ZyNova 仓库 |
| `.../ui/screens/content/settings/AboutInfoScreen.kt` | 非官方声明 + 许可证按钮 |
| `.../library/`、`.../res/raw/gpl_3_license.txt` | GPL-3.0 全文进 APK |
| `ZalithLauncher/build.gradle.kts` | 包名 com.zynova.launcher、release 签名用 debug 密钥 |
| `ZalithLauncher/gradle.properties` | 名称 ZyNova |
| `.github/workflows/build_apk.yml` | **新增**，GitHub Actions 编译 arm64 Release |
| `settings.gradle.kts` | 恢复官方仓库源（不要加阿里云镜像到 GitHub！） |

---

## 五、编译环境（本地手机容器）

已配置好的环境：
- JDK 17：`/usr/lib/jvm/java-17-openjdk-arm64`
- Android SDK：`/opt/android-sdk`（platform 37 + build-tools 37 + NDK 25.2）
- 环境变量：`LANG=C.UTF-8 LC_ALL=C.UTF-8 JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8" ANDROID_HOME=/opt/android-sdk`

⚠️ **本机（arm64 手机容器）无法完整编译 APK**：NDK 的 clang 只有 x86_64 版，Google 不提供 arm64 Linux 版。所以**本地编译会卡在 native 编译**。**正确做法是用 GitHub Actions 编译**（云端是 x86_64，能正常编译）。

### 本地能做的：
- 改代码
- `git add` + `git commit` + `git push`（在 `/root/zyNova_git`，用低内存配置）

### 本地 git 推送命令（重要，防止崩溃）：
```bash
cd /root/zyNova_git
git config pack.windowMemory 128m
git config pack.deltaCacheSize 64m
git config pack.threads 1
git config core.bigFileThreshold 1m
git push "https://zzy89216-gif:<TOKEN>@github.com/zzy89216-gif/ZyNova.git" main:main
```
（`<TOKEN>` 需要用户提供，不要硬编码到任何文件！）

---

## 六、GitHub Actions 编译流程

- Workflow 文件：`.github/workflows/build_apk.yml`
- 触发：push 到 main 分支 + 手动 workflow_dispatch
- 编译：`./gradlew ZalithLauncher:assembleRelease -Darch=arm64`
- 产物：`ZyNova-2.5-arm64-v8a.apk`（约 180MB），在 Actions 页面的 Artifacts 下载

### 发布 Release 步骤：
1. 等编译成功（约 10-20 分钟首次，后续更快）
2. 下载 APK artifact（用 GitHub API）
3. 创建 GitHub Release 并上传 APK

---

## 七、⚠️ 红线（绝对禁止，接手者必读）

1. **不要改 Pojav 后端、SDL、LWJGL 等核心渲染/运行库**（用户明确说过，改了启动器就废了）。
2. **不要硬编码任何隐私信息**：GitHub token、签名密码、OAuth client id、CurseForge API key 等，**一律不能进代码或 git 历史**。
3. **不要加阿里云镜像到 settings.gradle.kts**（那是我之前在本地为解决 dl.google.com 不可达临时加的，会导致 GitHub 海外服务器编译失败，已恢复官方源）。
4. **不要改 namespace**（`com.movtery.zalithlauncher`），只改 applicationId（`com.zynova.launcher`）。
5. **更新检查源**：`URL_PROJECT_INFO` 仍指向上游，改它需要 ZyNova 有自己的版本信息文件（`latest_version_md.json` 格式见 `upgrade/RemoteData.kt`）。
6. **GPL-3.0 合规**：保持开源、保留上游版权声明（文件头的 `Copyright (C) 2025 MovTery`）、分发时附 GPL-3.0 文本。
7. **签名**：release 用官方公开的 debug 密钥 `zalith_launcher_debug.jks`（密码在 gradle.properties，官方本来就公开的），不要生成新密钥硬编码密码。

---

## 八、常见坑（已踩过）

1. **git 在 sdcard 上崩溃**（SIGSEGV）→ 所有 git 操作在 `/root/zyNova_git`（ext4）做。
2. **git push pack-objects 崩溃**（signal 11）→ 用低内存配置（见「编译环境」）。
3. **中文路径问题** → 必须设置 `LANG=C.UTF-8 LC_ALL=C.UTF-8`，否则 Java 报 InvalidPathException。
4. **sdkmanager SSL 报错**（trustAnchors）→ 已通过 `update-ca-certificates -f` 修复。
5. **GitHub Actions KSP 插件解析失败** → 是阿里云镜像导致的，settings.gradle.kts 必须用官方源。
6. **GitHub fine-grained token 不能 push** → 要用 classic token（ghp_ 开头），勾选 repo 权限。
7. **一键安装不自动下载前置依赖**（已在 v2.5.1 修复）→ 原因是 `_Download.QuickInstall.kt` 忽略了 Modrinth 依赖的 `version_id`（作者指定的精确版本），改成重新查项目版本+选适配导致选不到。修复方法：`PlatformDependency` 增加 `versionId` 字段，`collectDependencies` 优先用 `getVersionById()` 获取精确版本。

---

## 九、给接手者的建议操作顺序

1. 读本文件 + README.md + LICENSE。
2. 确认当前 GitHub 仓库状态（看 Actions 最新编译结果）。
3. 在 `/root/zyNova_git` 改代码 → commit → push（用上面的命令）。
4. 等 GitHub Actions 编译 → 下载 APK → 创建 Release。
5. 开发新功能前，先读「红线」和「待办」。

---

## 十、当前仓库 GitHub 信息

- 仓库：`zzy89216-gif/ZyNova`（public）
- 分支：`main`
- 已发布版本：**v2.5.1**（最新，Release 链接：https://github.com/zzy89216-gif/ZyNova/releases/tag/v2.5.1）
- 历史版本：v2.5（有前置依赖 bug，已修复于 v2.5.1）
- 最新提交：合规整改（About 页声明、GPL-3.0、链接指向 ZyNova）+ 交接文档
- 编译 workflow：`build_apk.yml`（Release arm64）

---

## 十一、发布 Release 的完整步骤（以后重复用）

APK 编译由 GitHub Actions 自动完成，发布 Release 用 GitHub API。下面是完整命令（`<TOKEN>` 需用户提供）：

```bash
TOKEN="<TOKEN>"
REPO="zzy89216-gif/ZyNova"

# 1. 等编译完成后，找最新成功的 run id 和 artifact id
curl -s -H "Authorization: Bearer $TOKEN" "https://api.github.com/repos/$REPO/actions/runs?status=success" | python3 -c "import sys,json; d=json.load(sys.stdin); [print(r['id'], r['name']) for r in d['workflow_runs'][:5]]"

# 2. 下载 APK artifact（zip 格式，需解压得到 .apk）
RUN_ID="<上面的 run id>"
curl -s -H "Authorization: Bearer $TOKEN" "https://api.github.com/repos/$REPO/actions/runs/$RUN_ID/artifacts" | python3 -c "import sys,json; d=json.load(sys.stdin); [print(a['id'], a['name']) for a in d['artifacts']]"
ARTIFACT_ID="<上面的 artifact id>"
curl -sL -H "Authorization: Bearer $TOKEN" "https://api.github.com/repos/$REPO/actions/artifacts/$ARTIFACT_ID/zip" -o apk.zip
unzip apk.zip -d apk_dir/

# 3. 创建 Release
curl -s -X POST -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  "https://api.github.com/repos/$REPO/releases" \
  -d '{"tag_name":"v2.5","name":"ZyNova v2.5","body":"发布说明","draft":false,"prerelease":false}' \
  | python3 -c "import sys,json; d=json.load(sys.stdin); print(d['id'], d['upload_url'])"

# 4. 上传 APK（把 upload_url 里的 {?name,label} 去掉，加 ?name=xxx.apk）
RELEASE_ID="<上面的 release id>"
curl -sL -X POST -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/vnd.android.package-archive" \
  --data-binary @apk_dir/ZyNova-2.5-arm64-v8a.apk \
  "https://uploads.github.com/repos/$REPO/releases/$RELEASE_ID/assets?name=ZyNova-2.5-arm64-v8a.apk"
```

> ⚠️ 大文件上传/下载可能中断，APK 下载可用 `curl -C -` 断点续传。

---

**最后更新**：2026-09-06（v2.5.1 已发布，修复前置依赖自动下载）

