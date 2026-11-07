<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **ZyNova 28.1.0 LTS 正式发布（全球化商业合规版）。**


[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.1.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.1.0)

**简体中文** | [English](README_EN.md) | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md) | [한국어](README_KO.md) | [Русский](README_RU.md)

---

Android 上的 Minecraft: Java Edition 启动器，基于 ZalithLauncher2 源码自行维护发布，GPL-3.0。
目标是「少点几下」：玩家的操作尽量交给 AI 和自动化流程。

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ 28.1.0 更新要点

- **首次启动合规流程**：3 份合并文档（①EULA + 隐私政策 ②第三方开源合规声明 + 补充免责与风险告知 ③使用教程 + 适用地区与法律管辖），每份**必须滑到底部并单独勾选**，全部完成后选择年龄段（老用户升级后同样会走一次）
- **未成年人时段限制**：未满 18 周岁仅可在周五、周六、周日和法定节假日 **20:00–21:00** 启动游戏；法定节假日按国办发明电〔2025〕7号逐日列出，调休上班日按工作日处理
- **免责声明写全了**：存档丢失、设备发热、第三方封号、服务停服等风险均已明确告知
- 同时包含：主页卡片不再卡加载、AI 联网搜索可用、导入按实例目录、崩溃自动修复已移除

## 🛠️ 能做什么

- **AI Agent**：默认 DeepSeek，能装游戏 / 装整合包 / 装模组 / 改设置 / 读日志 / 启动游戏
- **渲染器**：自动 / MobileGlues / GL4ES / LTW（实验性）
- **资源核心**：Modrinth / CurseForge 搜索 → 依赖解析 → 下载 → 校验 → 安装
- **收藏与归档**：在线收藏 + 私有目录归档，SHA-256 去重
- **前置 API 防呆**：Fabric / Quilted / Legacy Fabric API 由安装流程自动加入，不可重复安装、搜不到、删不掉

## 📦 下载与构建

- 下载：<https://github.com/zzy89216-gif/ZyNova/releases>（认准最新版）
- 自行构建：JDK 17 + Android SDK / NDK，然后 `./gradlew ZalithLauncher:assembleRelease`

## 📚 文档

- 用户文档中心（77 篇）：[docs/zh/README.md](docs/zh/README.md)
- 更新日志：[CHANGELOG.md](CHANGELOG.md)
- 交接 / 架构 / 开发流程：[HANDOFF.md](HANDOFF.md)
- 第三方许可：[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) · 许可证：[LICENSE](LICENSE)

## 💖 支持

- 反馈：<https://github.com/zzy89216-gif/ZyNova/issues> · Discord：<https://discord.gg/QwPpZQHrTa>
- 赞助：<https://afdian.com/a/hizzy>

## ⚖️ 许可证

GPL-3.0，保持开源。第三方组件、各组件许可证与「你能怎么用」见
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) 与 [docs/zh/licenses.md](docs/zh/licenses.md)。

> ⚠️ 其中**陶瓦联机 Terracotta 是 AGPL-3.0**（比其他组件更严格）：本项目原样分发、未修改其二进制；
> 若你要修改后再分发，需按 AGPL-3.0 §13 公开对应源码。

---

**ZyNova — Context First. Less Steps.**
