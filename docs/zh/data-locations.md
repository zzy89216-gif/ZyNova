# 数据位置

游戏相关的东西放在**公共存储**里，打开文件管理器就能看到：

| 内容 | 位置 |
|---|---|
| 启动器根目录 | `/storage/emulated/0/ZyNova/` |
| 实例 / 游戏数据 | `ZyNova/.Minecraft/`（点开头，文件管理器默认隐藏） |
| 存档 | `ZyNova/.Minecraft/<实例>/saves/` |
| 模组 / 资源包 / 光影 | `ZyNova/.Minecraft/<实例>/mods`、`resourcepacks`、`shaderpacks` |
| 皮肤 / 披风缓存 | `ZyNova/.Minecraft/account_skins`、`account_capes` |
| 启动器日志 | `ZyNova/logs/` |
| Java 运行时 / JNA | 应用私有目录（公共存储是 noexec，放那里游戏起不来） |
| 设置 / 令牌 | MMKV（应用私有） |
| MobileGlues 配置 | `mobileglues/config.json` |

根目录只放两样东西：`logs`（日志，反馈问题时要能找到）和 `.Minecraft`（游戏数据）。
实例、存档、模组都在 `.Minecraft` 里，这样文件管理器里不会看到一堆散落的目录，
但真要进去改配置、加模组也照样能进。

- **新装用户**不需要迁移：第一次启动就直接在 `/ZyNova/.Minecraft`。
- **从旧版本升级**：如果游戏数据还在应用私有目录里，启动器会在进主界面之前**强制迁移**，
  并显示全屏进度；搬完后要点「下一步」才会继续。迁移会先复制、再校验，成功后把旧目录改名成
  `games.migrated-<时间戳>` 保留（不删除），确认没问题可以自己删掉；失败会清理半成品并回退到私有目录，
  不会丢数据。中途退出也没关系，下次打开会接着搬完。
- 需要「所有文件访问」权限才能读写 `/ZyNova`；没有授权时启动器无法使用（会在启动页拦住并引导授权）。
- 卸载应用**不会**删除 `/ZyNova` 里的游戏数据；要彻底清理请自己在该目录里删。
