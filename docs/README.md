# ZyNova 文档中心

这里放**所有用户向的说明、常见问题、故障排查与维护约定**。
根目录的 `README.md` 只负责介绍与下载入口；细节都收在这里，避免首页越来越长。

## 目录

| 文档 | 内容 |
|---|---|
| [install.md](install.md) | 安装哪个 APK、首次启动、存储与权限 |
| [usage.md](usage.md) | 下载游戏 / 模组 / 整合包、实例管理、导出 |
| [renderers.md](renderers.md) | 自动 / MobileGlues / GL4ES / LTW，以及怎么选 |
| [login.md](login.md) | 微软正版、离线、第三方认证与常见登录错误 |
| [favorites.md](favorites.md) | 在线收藏、本地归档、安装 / 导出 / 删除 |
| [ai-agent.md](ai-agent.md) | AI Agent 配置、权限模式、能做什么、安全边界 |
| [troubleshooting.md](troubleshooting.md) | 崩溃分析、日志位置、常见故障处理 |
| [faq.md](faq.md) | 高频问题速查 |
| [data-and-privacy.md](data-and-privacy.md) | 数据存放位置、卸载影响、日志上传 |
| [build.md](build.md) | 本地构建、两个 APK、CI 与发布流程入口 |

## 维护约定（给下一个接手的人 / AI）

1. **新增功能或修 Bug 时，必须同步 `docs/`**：
   - 用户能感知的行为变化 → 改对应文档；
   - 新入口 / 新设置项 → 至少写进 `usage.md` 或对应专题；
   - 新的常见故障 → 写进 `troubleshooting.md` 与 `faq.md`。
2. **`docs/README.md` 的目录表必须同步**；新增文件不登记等于没写。
3. **不要在这里写密钥、Token、内网地址、个人联系方式**。
4. **版本号、下载地址、UI 路径必须可核对**；不确定就写「以当前版本界面为准」，不要编。
5. 六语言 `README*.md` 是另一套东西：中文可玩梗，其他语言必须严肃、逐段对齐；详细历史仍以 `HANDOFF.md` / `CHANGELOG.md` 为准。

> 本目录是 27.9.0 起建立的；之前只散落在 HANDOFF / README / CHANGELOG 里。
