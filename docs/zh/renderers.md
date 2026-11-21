# 渲染器

## 内置列表

| 渲染器 | 状态 | 适合 |
|---|---|---|
| 自动 | 默认 | 不想选；按设备能力 + 版本在 MobileGlues → GL4ES → Ironized Zink 之间挑 |
| MobileGlues | 推荐 | 大多数设备；直接把桌面 GL 翻译到宿主 GLES |
| Ironized Zink | 可选 | 想要桌面级 OpenGL 4.x / 光影，且设备 Vulkan 驱动可用 |
| GL4ES | 兼容兜底 | 老版本 / MobileGlues 跑不起来的设备 |
| LTW（实验性） | **不推荐** | 愿意承担崩溃风险、想试经典版本兼容层 |

> Ironized Zink 是内置渲染器（Mesa Zink + Kopper，经 Vulkan 转译），不需要安装渲染器插件。
> 它自带 Mesa/Kopper 二进制与 GPL-3.0 许可，随安装包分发。

## 怎么选

1. 先保持「自动」。
2. 进游戏黑屏 / 花屏 / 闪退：
   - 换 MobileGlues；
   - 仍不行换 GL4ES；
   - 老版本（1.16 及以前）优先 GL4ES；想要桌面级 OpenGL 4.x / 光影、且设备 Vulkan 可用时选 Ironized Zink。
3. 用光影 / 大型模组：优先 MobileGlues，必要时调低 OpenGL 版本。
4. 不要同时装多个渲染器插件；列表里选中哪个就用哪个。

## MobileGlues

- 设置 → 渲染器 → MobileGlues：
  - **OpenGL 版本**：默认 4.0；驱动不支持更高版本时往下调。
  - **ANGLE 后端**：默认关闭；云层穿透、水/粒子顺序异常时保持关闭。
  - **multidraw 顺序**：只有方块/实体绘制异常时才需要改；默认顺序由上游决定。
- 配置写入应用私有目录的 `mobileglues/config.json`，不修改 `/sdcard/MG`。

## Ironized Zink

- 设置 → 渲染器 → Ironized Zink（选中后本页会直接显示配置，也可在状态卡片进详细页）：
  - **预设**：Potato / Performance / Default / Max Compatibility 四个官方预设，
    一次性写入性能与兼容性开关；**不会**改动下面的 OpenGL 版本。
  - **OpenGL 版本**：3.3 – 4.6。四个预设都是按 4.6 调的；驱动转译不了 4.6 时
    在这里单独降级，下次启动生效。预设与它互相独立，切预设不会把版本重置回 4.6。
  - **高级参数**（默认收起）：线程化 GL、绑定大核、乱序绘制、垂直同步、放宽 GLSL、
    着色器缓存等，改动即时保存。
- 驱动：Zink 走 Vulkan，本页会出现 Vulkan 驱动与「使用系统 Vulkan 驱动」开关
  （Adreno 设备开启前会弹警告）。**自动**模式可能挑到 Zink，因此也会显示驱动选项。

## LTW（实验性）

- 仅 arm64-v8a；其他设备不会显示。
- 只建议在明确知道风险时手动选择；自动模式不会选它。
- 参数页只暴露 LTW native 真正读取的环境变量（buffer flush、coherent storage、调试日志等）。
- 崩溃时请先换回 MobileGlues / GL4ES，再判断是不是 LTW 的问题。

## 驱动

- Ironized Zink（以及可能挑到它的「自动」）与渲染器插件走 Vulkan 时才显示「Vulkan 驱动」选项。
- 内置的 MobileGlues / GL4ES / LTW 不需要选驱动。
