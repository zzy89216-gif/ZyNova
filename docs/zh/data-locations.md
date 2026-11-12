# 数据位置

游戏相关的东西放在**公共存储**里，打开文件管理器就能看到：

| 内容 | 位置 |
|---|---|
| 实例 / 游戏 | `/storage/emulated/0/ZyNova/` |
| 存档 | `ZyNova/<实例>/saves/` |
| 模组 / 资源包 / 光影 | `ZyNova/<实例>/mods`、`resourcepacks`、`shaderpacks` |
| Java 运行时 | `ZyNova/runtimes/` |
| 收藏归档 | `ZyNova/favorites/`（旧版本在应用私有目录 `files/favorites/`） |
| 启动器日志 | 应用外部目录 `Android/data/com.zynova.launcher/files/logs/` |
| 设置 / 令牌 | MMKV（应用私有） |
| MobileGlues 配置 | `mobileglues/config.json` |

- 从旧版本升级：如果游戏数据还在应用私有目录里，启动器会**弹窗问一次**要不要迁到 `/ZyNova`；
  点「暂不迁移」不会删除任何东西，之后也可以手动把 `files/games/` 复制到 `ZyNova/`。
- 需要「所有文件访问」权限才能读写 `/ZyNova`；没有授权时启动器会回退到私有目录，开局前会提示你去开启。
- 卸载应用**不会**删除 `/ZyNova` 里的游戏数据；要彻底清理请自己在该目录里删。
