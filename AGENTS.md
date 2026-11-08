# 接手必读（人类 / AI Agent 通用）

> **这份文件是入口。** 下一个接手的人或 AI，只看这一份就能知道：项目是什么、现在什么状态、发版怎么做、哪些坑不能踩、文档怎么维护。
>
> 读完后请按顺序读：**本文件 → [`HANDOFF.md`](HANDOFF.md)（架构与历史）→ [`CHANGELOG.md`](CHANGELOG.md)（改了什么）→ [`docs/zh/README.md`](docs/zh/README.md)（用户文档总索引）**。
>
> 维护者通常只说一句「了解一下这个项目，看文档，然后做」——所以**所有约定都必须写在这里或 HANDOFF 里，不要依赖聊天记录**。

---

## 一、项目速览

| 项 | 值 |
|---|---|
| 名称 | ZyNova（ZyNova Launcher） |
| 性质 | 基于 [ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2) 的**非官方** Minecraft: Java Edition Android 启动器 |
| 许可证 | GPL-3.0（上游同为 GPL-3.0，必须保持开源） |
| 仓库 | <https://github.com/zzy89216-gif/ZyNova>，分支 `main` |
| 包名 | `com.zynova.launcher`（namespace 仍是 `com.movtery.zalithlauncher`，**不要改**） |
| 最低系统 | Android 8.0（`minSdk 26`），`targetSdk 34`，`compileSdk 37` |
| 构建 | JDK 17 + Android SDK/NDK，`./gradlew ZalithLauncher:assembleRelease` |
| 核心原则 | **Context First. Less Steps.** 能自动判断就不让用户选；能一步就别拆两步 |
| 官方渠道 | GitHub <https://github.com/zzy89216-gif/ZyNova> · Discord <https://discord.gg/QwPpZQHrTa> · X（主账号）<https://x.com/ZyNovaProject> |
| 联系邮箱 | 国内生态 <ZyNovaLauncher@163.com> · 全球生态 <ZyNovaLauncher@outlook.com> |

## 二、当前状态（写这份文档时的事实）

| 项 | 值 |
|---|---|
| 版本名 | `28.1.0`（**不带任何后缀，不要写 LTS**） |
| versionCode | `281001` |
| 版本号唯一来源 | `ZalithLauncher/gradle.properties` 的 `launcher_version_name` / `launcher_version_code` |
| Release | 只有 `v28.1.0` 一个，tag 指向 **`65f0ba3`**（打这个包用的提交） |
| main | 可能比 tag 新（后续只改了文档），**这是正常现象** |
| 发布资产 | `ZyNova-28.1.0.apk`、`ZyNova-28.1.0-arm64-v8a.apk`、`mapping.universal.zip` |
| 用户文档 | `docs/zh` 与 `docs/en` **各 77 篇**，文件名一一对应 |

> ⚠️ 版本号一变，`legalVersion` 也跟着变（`OnboardingState.CURRENT_LEGAL_VERSION = BuildConfig.VERSION_NAME`），
> 所有用户会被强制重新同意一次条款。这是设计如此，别「优化」掉。

## 三、这个版本实现了什么（别重复造）

### 首次启动合规流程（`ZalithLauncher/.../onboarding/`）
- **3 份合并文档**，每份**必须滑到底部**（`derivedStateOf` 判断 scrollState 到底）+ **逐份勾选**才能下一步；换页重置勾选**并把滚动位置复位到顶部**；最后选年龄。
  1. 《最终用户许可协议 + 隐私政策》
  2. 《第三方开源合规声明 + 补充免责与风险告知》——直接渲染 APK 内 `assets/licenses/THIRD_PARTY_NOTICES.md`
  3. 《使用教程 + 适用地区与法律管辖》
- 关键词标红：AGPL / LGPL / 不承担任何 / 免责 / 风险 / 无法使用 / 不允许 / 必须（`highlightLegal`）
- **地区白名单**：中国 / 美国 / 韩国 / 日本 / 俄罗斯。时区**逐条列出**（美国仅本土 + 阿拉斯加 + 夏威夷 + 关岛），
  **禁止用 `America/` 这种大前缀**——以前这么写导致加拿大、墨西哥、巴西被放行。允许 `<时区>/子区域` 变体。
- 阻断页只有「退出」按钮，安全退出，不伪造崩溃。
- **未成年人时间锁**：未满 18 仅周五 / 周六 / 周日 + 法定节假日 20:00–21:00；
  2026 节假日按国办发明电〔2025〕7号（元旦 1/1-3、春节 2/15-23、清明 4/4-6、劳动 5/1-5、端午 6/19-21、中秋 9/25-27、国庆 10/1-7）；
  调休上班日禁玩：1/4、2/14、2/28、5/9、9/20、10/10。
- 启动游戏的三个入口（`Launch.Game` / `PlayServer` / `PlaySave`）都必须过 `allowLaunchUnderAgeRule()`。

### AI 能力（51 个工具）
完整清单见 [`docs/zh/ai-tools.md`](docs/zh/ai-tools.md)。分类：安装与启动、资源与整合包、实例、文件、公有存储、
文档生成（手写 OOXML，不引 POI）、日志与崩溃、联网（固定 DuckDuckGo）、联机（陶瓦 + 樱花 frp）、收藏、设置。
已移除：崩溃自动修复（`CrashGuard` / `AIPendingPrompt`）、搜索引擎配置。

### 其他
- 默认主页 = **Cards**（主页卡片），数据 IO 线程加载 + 回前台刷新
- `options.txt` 用真实键：`graphicsMode`(0/1/2)、`particles`(0/1/2)、`fov`(归一化)
- 返回导航递归下探最深层（修「下载页点返回直接回主界面」）

## 四、发版：完整流程与坑（照做即可）

### 0. 前置
- 版本号只改 `ZalithLauncher/gradle.properties`（name + code），**别处不要写死版本号**。
- 同步：`CHANGELOG.md`、`HANDOFF.md`、`README*.md`（六种语言）、`docs/`。
- 推送命令（Token 只放命令行，**绝不入库**）：
  ```bash
  git -c credential.helper= push "https://<user>:<TOKEN>@github.com/zzy89216-gif/ZyNova.git" HEAD:main
  ```
- 提交作者必须是 `ZyNova Dev <zzy89216-gif@users.noreply.github.com>`。

### 1. 等 push 构建绿
```bash
curl -s "https://api.github.com/repos/zzy89216-gif/ZyNova/actions/runs?per_page=3" -H "Authorization: Bearer $TOKEN"
```
push 到 main 会触发 `build_apk.yml`，**约 19 分钟**。**红构建不许发版。**

> 🕒 **只改文档不会触发编译**：`build_apk.yml` 加了 `paths-ignore`（`**.md`、`docs/**`、`LICENSE`、`ISSUE_TEMPLATE` 等）。
> 整次 push 只碰这些路径时，工作流不会启动——以前改个错别字也要等 19 分钟，维护者嫌慢。
> 确实想手动跑一次：Actions → **Build ZyNova APK** → *Run workflow*（`workflow_dispatch`）。
> 发版时的编译不走这个工作流，走 `release_ci.yml` → `build.yml`。

### 2. 删除旧 Release / tag（**必须在构建结束之后**）
```bash
# 找 id
curl -s ".../releases" | 取 tag_name=vX.Y.Z 的 id
curl -s -X DELETE ".../releases/<id>"      # 返回 204
curl -s -X DELETE ".../git/refs/tags/vX.Y.Z"  # 返回 204
git ls-remote --tags origin   # 确认为空
```
> ⚠️ **坑 1**：发布工作流用 `softprops/action-gh-release`。**如果 Release 被删时那轮构建还在跑，它会把 Release 自动重建**（死灰复燃）。所以一定等构建 `completed` 再删。
> ⚠️ **坑 2**：删 Release 不会删 tag，两步都要做。

### 3. 建 Release
```bash
POST /repos/zzy89216-gif/ZyNova/releases
{
  "tag_name": "vX.Y.Z",
  "target_commitish": "<完整 40 位 SHA>",
  "name": "...",
  "body": "...",
  "draft": false,
  "prerelease": false
}
```
> ⚠️ **坑 3**：`target_commitish` 必须是**完整 40 位 SHA**，短 SHA 会 **422**。

### 4. 等发布构建 / 核对资产（约 18 分钟）
`release_ci.yml` 会在 `release: published` 时跑 `build.yml` 并上传，核对：
- `ZyNova-<版本>.apk`
- `ZyNova-<版本>-arm64-v8a.apk`
- `mapping.universal.zip`
- 三个都必须是 `uploaded`；用 `curl -sIL -o /dev/null -w '%{http_code}'` 抽查下载链接应为 200。

> ⚠️ **坑 4（资产名）**：APK 文件名只取版本号的数字部分（`launcherVersionFileTag = launcher_version_name.substringBefore(' ')`）。
> 如果版本名带后缀（如 `28.1.1 beta`），空格会被 GitHub 转成点，出现 `ZyNova-28.1.1.beta.apk` 这种名字。
> 真要改回来：改 `ZalithLauncher/build.gradle.kts` 的 `launcherVersionFileTag`。
> GitHub 支持 `PATCH /releases/assets/{id}` 直接改名，不需要重传。

### 5. 修 Bug 必须升版本号
已发布的 tag **不覆盖、不改写**。修 Bug → 升 code/name → 正常发新版本。

## 五、文档规则（维护者最在意这条）

1. **中英必须成对**：`docs/zh` 与 `docs/en` 文件名一对一、篇数相同（当前各 77 篇），并都在各自的 `README.md` 索引里登记。
2. **坏链必须为 0**：相对链接、图片、文内锚点都要能解析。
3. **索引标题 = 正文 H1**，不要各写各的。
4. **用户文档里不要写内部版本号**（如「27.9.0 起」）：那些版本用户从没见过。用行为描述代替。
5. **不要写一次性表述**：类似「只提供 LTS」「只保留一个 Release」「这是第一个（或最后一个）公开版本」说法，发一次就过期。
6. **版本号只能有一个来源**（`gradle.properties`），文档里的数字必须与它一致。
7. **改完必须全量检查**：坏链、中英对齐、索引登记、版本一致、CHANGELOG 首条。
8. **`THIRD_PARTY_NOTICES.md` 有两个副本**：仓库根目录 + `ZalithLauncher/src/main/assets/licenses/`。
   应用内「第三方声明」页渲染的是**APK 里那份**，所以改动必须两边同步，**并且跟着一次真实构建走**——
   只改仓库会造成「应用内文本 ≠ 仓库文本」。

### 文档检查可以用的一段命令（不想装工具时）
```bash
python3 - <<'PY'
import os,re
skip={'.git','build','.gradle','node_modules','.idea'}
mds=[os.path.relpath(os.path.join(b,f),'.') for b,d,fs in os.walk('.') for f in fs
     if f.endswith('.md') and not (set(b.split(os.sep))&skip)]
bad=[]
for p in mds:
    t=open(p,encoding='utf-8').read(); d=os.path.dirname(p)
    for u in re.findall(r'!?\[[^\]]*\]\(([^)\s]+)\)',t):
        if u.startswith(('http','mailto:','#','tel:')): continue
        c=u.split('#')[0]
        if c and not os.path.exists(os.path.normpath(os.path.join(d,c))): bad.append((p,u))
print(len(mds),'md; bad links:',len(bad)); [print(' ',x) for x in bad]
PY
```

## 六、已知的坑（都踩过）

| 坑 | 说明 |
|---|---|
| Kotlin 编译错误 | 取日志：`GET /actions/runs/{run_id}/jobs` → `jobs[0].id` → `GET /actions/jobs/{id}/logs`，再 grep `e: file:///` |
| 删 Issue | REST 没有删除接口，用 GraphQL `deleteIssue`（先 `GET /issues/{n}` 拿 `node_id`） |
| Token | classic token，需 repo 权限；**绝不写进仓库/文档/APK**（`BuildConfig` 里也不要） |
| Git 历史 | **不要重写**（维护者明确要求） |
| 构建耗时 | 一次 push 构建 ≈ 19 分钟，一次发布构建 ≈ 18 分钟（只改文档的 push 已被 `paths-ignore` 跳过） |
| 条款文本冻结 | APK 内的条款/声明是打包时冻结的，改条款必须重新构建 |
| `gradle.properties` 位置 | 版本号在 `ZalithLauncher/gradle.properties`，不在根目录 |
| `namespace` | 不要改 `com.movtery.zalithlauncher`，否则要改几百个文件的 package |

## 七、历史决定（避免反复讨论）

- 26.x / 27.x / 28.0.x 都是内部迭代，**未单独对外发布**；对外只有 28.1.0 起。
- 曾经为了把「18 岁按钮禁用」「地区误放行」两个已修 Bug 打进包，**用最新 main 重发过同版本号的 v28.1.0**。
  以后遇到「线上包是旧代码」这种情况，同样处理：先等新构建绿 → 删旧 Release/tag → 用完整 SHA 重建。
- 曾经加过一个文档自检脚本 + `Docs Check` 工作流，后来**按维护者要求删除了**。
  现在文档检查靠本节第五部分的命令 / 人工检查，**不要擅自把它加回 CI**。
- 维护者要「正常维护、正常修 Bug、正常发版」，**不要**在标题、版本名、文档里加「LTS / 长期支持」这类标签。
- Release 说明允许玩梗（README 也允许），但**不能写不存在的功能**，也不能把没修的写成修好了。
- **发版待办不再写进文档**，统一放在 Issue 里跟踪：<https://github.com/zzy89216-gif/ZyNova/issues/50>（下一个版本要做的事）。

## 八、给你的建议顺序

1. 读本文件 → `HANDOFF.md` 对应章节 → `CHANGELOG.md` 首条 → `docs/zh/README.md`。
2. `git log --oneline -20` 看最近做了什么。
3. 要改功能：先 `grep` 相关 `docs/` 与代码，**改完同步中英文档**。
4. 要发版：严格按第四节，别跳步。
5. 拿不准就问维护者一句，不要猜着改历史/改 tag。
