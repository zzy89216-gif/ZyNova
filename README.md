<p align="center"><sub>
<a href="README_EN.md">English</a> · <b>简体中文</b> · <a href="i18n/zh-Hant/README.md">繁體中文</a> · <a href="i18n/ja/README.md">日本語</a> · <a href="i18n/ko/README.md">한국어</a> · <a href="i18n/tr/README.md">Türkçe</a> · <a href="i18n/ru/README.md">Русский</a> · <a href="i18n/vi/README.md">Tiếng Việt</a> · <a href="i18n/th/README.md">ไทย</a> · <a href="i18n/de/README.md">Deutsch</a> · <a href="i18n/es/README.md">Español</a> · <a href="i18n/fr/README.md">Français</a>
<br>
<a href="i18n/uk/README.md">Українська</a> · <a href="i18n/pl/README.md">Polski</a> · <a href="i18n/pt-BR/README.md">Português (Brasil)</a> · <a href="i18n/ar/README.md">العربية</a> · <a href="i18n/fa/README.md">فارسی</a> · <a href="i18n/id/README.md">Bahasa Indonesia</a> · <a href="i18n/it/README.md">Italiano</a> · <a href="i18n/el/README.md">Ελληνικά</a> · <a href="i18n/ml/README.md">മലയാളം</a> · <a href="i18n/ur-Latn/README.md">Roman Urdu</a> · <a href="i18n/ro/README.md">Română</a> · <a href="i18n/bg/README.md">Български</a>
</sub></p>

<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **ZyNova 28.1.0 正式发布（全球化商业合规版）。**

> 🧪 **28.1.5（测试版，预发行）**：修掉运行中收回「所有文件访问」后数据看起来"消失"、重新授权也恢复不了的 P0 问题；
> 上一版已做：修掉账号管理页崩溃（WebView 读公共目录皮肤）、樱花教程滑不动、
> 联机页状态不刷新、卡片宽度接缝；LTW 参数收敛到独立页面；关于页赞助入口重排。
> 上一版已做：公共目录只保留 `logs` 与 `.Minecraft`；迁移改为全屏、搬完要点「下一步」；
> 联机页改成底部悬浮菜单（陶瓦 / 樱花就地切换，并记住上次选择）；联机教程按真实流程重写；
> LittleSkin 快捷登录；主页卡片压住一半即弹起换位。
> 下载：[Releases](https://github.com/zzy89216-gif/ZyNova/releases)（预发行；日常使用建议仍选正式版 28.1.0）


[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.1.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.1.0)

---

Android 上的 **Minecraft: Java Edition** 启动器，基于 ZalithLauncher2 源码自行维护发布，GPL-3.0。

设计思路只有一句：**少点几下**。多数启动器把设置摊开让玩家自己选，ZyNova 反过来——先替你判断，判断不了再问一次。装游戏、找模组、理依赖、读崩溃日志，都尽量由 AI 代劳。

## 🎯 谁适合用

- 手机上只装了这一个启动器，想「打开就能玩」的人
- 整合包玩家：几十个模组、依赖一堆，懒得手动对齐版本
- 出了问题只会截图问人的人：把日志丢给 AI，比翻十页帖子快

## ⚡ 30 秒上手

1. 从 [Releases](https://github.com/zzy89216-gif/ZyNova/releases) 下载 APK 安装（要求 Android 8.0 及以上）
2. 首次启动先过地区检查，然后**必须允许「所有文件访问」**——游戏数据放在公共目录 `/ZyNova`，没这个权限读写不了
3. 读完 3 份条款：每份滑到底、单独勾选，再选年龄段（老用户升级会先走一次数据迁移，搬完点「下一步」）
4. 登录微软账号（或 LittleSkin / 离线）→ 选版本（或让 AI 装）→ 启动

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- X（官方主账号）：<https://x.com/ZyNovaProject>
- 国内生态邮箱（QQ 邮箱也可发到这里）：<ZyNovaLauncher@163.com>
- 全球生态邮箱：<ZyNovaLauncher@outlook.com>

## ✨ 28.1.0 更新要点

- **首次启动合规流程**：3 份合并文档（①EULA + 隐私政策 ②第三方开源合规声明 + 补充免责与风险告知 ③使用教程 + 适用地区与法律管辖），每份**必须滑到底部并单独勾选**，全部完成后选择年龄段（老用户升级后同样会走一次）
- **未成年人时段限制**：未满 18 周岁仅可在周五、周六、周日和法定节假日 **20:00–21:00** 启动游戏；法定节假日按国办发明电〔2025〕7号逐日列出，调休上班日按工作日处理
- **免责声明写全了**：存档丢失、设备发热、第三方封号、服务停服等风险均已明确告知
- 同时包含：主页卡片不再卡加载、AI 联网搜索可用、导入按实例目录、崩溃自动修复已移除

## 📂 数据在哪

| 内容 | 位置 | 卸载后 |
|---|---|---|
| 实例 / 存档 / 模组 / 资源包 / 光影 | `/storage/emulated/0/ZyNova/.Minecraft/` | 保留 |
| 启动器日志 | `/ZyNova/logs/` | 保留 |
| Java 运行时 / 收藏归档 / 账号 / 设置 | 应用私有目录 | 删除 |

根目录只有 `logs` 和 `.Minecraft` 两样东西：日志方便你反馈问题时直接发出来，
`.Minecraft` 点开头、文件管理器默认不显示，避免误删。
详见 [数据位置](https://github.com/zzy89216-gif/ZyNova/blob/main/docs/zh/data-locations.md)。

## 🛠️ 能做什么

- **AI Agent**：默认 DeepSeek，能装游戏 / 装整合包 / 装模组 / 改设置 / 读日志 / 启动游戏
- **渲染器**：自动 / MobileGlues / GL4ES / LTW（实验性）
- **资源核心**：Modrinth / CurseForge 搜索 → 依赖解析 → 下载 → 校验 → 安装
- **收藏与归档**：在线收藏 + 私有目录归档，SHA-256 去重
- **前置 API 防呆**：Fabric / Quilted / Legacy Fabric API 由安装流程自动加入，不可重复安装、搜不到、删不掉

## 🌐 国内网络这块

- 启动器会按需使用镜像；镜像缺文件时自动回退官方（见 [docs/zh/mirrors.md](docs/zh/mirrors.md)）
- CurseForge 资源可能要走镜像，Modrinth 通常可以直连
- 下载出错先换网络、关代理/加速器试试，别急着卸载重装

## 📦 下载与构建

- 下载：<https://github.com/zzy89216-gif/ZyNova/releases>（认准最新版）
- 自行构建：JDK 17 + Android SDK / NDK，然后 `./gradlew ZalithLauncher:assembleRelease`
- 提示：首次启动会走合规流程，条款细则见 [docs/zh/licenses.md](docs/zh/licenses.md)

## 📚 文档

- 用户文档中心（77 篇）：[docs/zh/README.md](docs/zh/README.md)
- 更新日志：[CHANGELOG.md](CHANGELOG.md)
- 接手入口（状态 / 发版流程 / 坑 / 文档规则）：[AGENTS.md](AGENTS.md)
- 交接 / 架构 / 开发流程：[HANDOFF.md](HANDOFF.md)
- 第三方许可：[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) · 许可证：[LICENSE](LICENSE)

## 💖 支持

- 反馈：<https://github.com/zzy89216-gif/ZyNova/issues> · Discord：<https://discord.gg/QwPpZQHrTa>
- 邮箱：<ZyNovaLauncher@163.com> ｜ <ZyNovaLauncher@outlook.com>
- 官方 X：<https://x.com/ZyNovaProject>
- 赞助：<https://afdian.com/a/hizzy>

## ⚖️ 许可证

GPL-3.0，保持开源。第三方组件、各组件许可证与「你能怎么用」见
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) 与 [docs/zh/licenses.md](docs/zh/licenses.md)。

> ⚠️ 其中**陶瓦联机 Terracotta 是 AGPL-3.0**（比其他组件更严格）：本项目原样分发、未修改其二进制；
> 若你要修改后再分发，需按 AGPL-3.0 §13 公开对应源码。

---

**ZyNova — Context First. Less Steps.**
