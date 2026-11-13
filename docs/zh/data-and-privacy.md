# 数据与隐私

## 数据放在哪

| 内容 | 位置 | 卸载后 |
|---|---|---|
| 实例 / 游戏文件 / 存档 / 模组 | 公共目录 `/ZyNova/.Minecraft/` | 保留 |
| 启动器日志 | 公共目录 `/ZyNova/logs/` | 保留 |
| Java 运行时 / JNA | 应用私有目录 | 删除 |
| 收藏归档 | 应用私有目录 `files/favorites/` | 删除 |
| 控制布局 | 应用外部目录 `control_layouts/` | 删除 |
| 设置 / 账号令牌 | MMKV（应用私有） | 删除 |
| MobileGlues 配置 | 应用外部目录 `mobileglues/config.json` | 删除 |

「应用私有目录」指 `Android/data/com.zynova.launcher/` 下面，普通文件管理器通常看不到；
公共目录就是文件管理器里直接能打开的 `/storage/emulated/0/ZyNova/`。

## 权限

- 必须授予「所有文件访问」（Android 11+），否则读写不了 `/ZyNova`，启动器会在启动页拦住。
- 卸载应用**不会**删除 `/ZyNova` 里的游戏数据；账号、设置、运行时会被清掉。

## 日志上传

- 只有你主动点「分享链接」才会上传到公共日志平台；
- 上传前先确认日志里没有 Token、Cookie、服务器密码（启动器写日志时会自动打码常见密钥格式，但不要依赖它）；
- 崩溃分析在本地完成，不上传日志。

## API Key

- AI 的 API Key 存在应用私有设置里；
- CurseForge API Key 由构建时注入 APK（上游设计），不在仓库文档中公开；
- 不要把任何 Key 写进 Issue / 截图 / 聊天记录。
