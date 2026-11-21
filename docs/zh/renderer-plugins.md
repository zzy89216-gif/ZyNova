# 渲染器插件

内置渲染器只有 MobileGlues、GL4ES、LTW 三个，覆盖不到的设备靠**渲染器插件**补。
插件以 APK 形式安装，启动器读到后会自动出现在渲染器列表里，**不需要重启启动器**。

## 从哪里装

- 设置 → 渲染器 → 下载插件（应用内给了两个入口：GitHub 镜像 Release、国内网盘）。
- 只装能对得上来源的插件；网上来路不明的渲染器 APK 可能带改过的驱动二进制，出问题很难排查。

常见上游：

| 插件 | 上游 | 说明 |
|---|---|---|
| FCL Renderer Plugin（一堆渲染器合集） | <https://github.com/ShirosakiMio/FCLRendererPlugin/releases/tag/Renderer> | 官方 `FCL-Team/FCLRendererPlugin` 仓库已被 GitHub 下架，应用内链接指向这个镜像 Release |
| Ironized Zink（Zink over Vulkan） | <https://github.com/GoyDevv/IronizedZink> | 请用 **1.1.0 及以上**：旧版把 OpenGL 版本钉死在 4.6，老设备降不下来 |
| MobileGlues 插件版 | <https://github.com/MobileGL-Dev/MobileGlues-release> | 与内置版是两份二进制，见下面「同名两条」 |
| LTW 插件版 | <https://github.com/layou233/LTWRendererPlugin> | 更新较慢，内置的 LTW-Turbo 更活跃 |

> 「OpenLTW」「Vulkan Fix」这类名字**查不到可靠的上游仓库**，本项目不提供、也不为它们背书；真要装，请自己核对作者与来源。

## 几个正常但容易误会的现象

**渲染器列表里出现两条同名项，是正常的。**
内置版和插件版是两个不同的渲染器（标识符不同），会并列显示。比如「MobileGlues」和「MobileGlues（来自插件 …）」——
选哪个都行：内置版随启动器更新，插件版跟着插件作者更新。

**装了插件，列表里却没有。**
多半是**标识符（UUID）和已有渲染器撞了**：启动器会拒绝注册重复标识符的渲染器，只在日志里留一条警告。
把冲突的那个插件卸载（或只留一个）再回来看。

**插件没有齿轮图标。**
只有声明了自己可配置的插件才会显示齿轮；没声明就说明它不需要启动器传参数。

## 使用建议

- 不要同时启用多个会改同一套 GL 环境的插件（例如两个 Zink 系），容易互相覆盖环境变量。
- 插件更新后直接生效，启动器不缓存版本号，也不校验最低版本——**渲染器能不能用，以实际进游戏为准**。
- 进游戏黑屏 / 闪退时，先换回「自动」或内置的 MobileGlues 试一次，再判断是不是插件的问题。
- 老设备想降 OpenGL 版本：用 Ironized Zink 插件自带的「OpenGL 版本 / 预设」，不要指望启动器替它改。
