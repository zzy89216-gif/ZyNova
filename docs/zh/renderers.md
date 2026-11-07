# 渲染器

## 内置列表

| 渲染器 | 状态 | 适合 |
|---|---|---|
| 自动 | 默认 | 不想选；按版本在 MobileGlues → GL4ES 之间挑 |
| MobileGlues | 推荐 | 大多数设备；直接把桌面 GL 翻译到宿主 GLES |
| GL4ES | 兼容兜底 | 老版本 / MobileGlues 跑不起来的设备 |
| LTW（实验性） | **不推荐** | 愿意承担崩溃风险、想试经典版本兼容层 |

> Ironized Zink 已移除；需要 Zink 请使用渲染器插件，不再内置 Mesa/Kopper。

## 怎么选

1. 先保持「自动」。
2. 进游戏黑屏 / 花屏 / 闪退：
   - 换 MobileGlues；
   - 仍不行换 GL4ES；
   - 老版本（1.16 及以前）优先 GL4ES。
3. 用光影 / 大型模组：优先 MobileGlues，必要时调低 OpenGL 版本。
4. 不要同时装多个渲染器插件；列表里选中哪个就用哪个。

## MobileGlues

- 设置 → 渲染器 → MobileGlues：
  - **OpenGL 版本**：默认 4.0；驱动不支持更高版本时往下调。
  - **ANGLE 后端**：默认关闭；云层穿透、水/粒子顺序异常时保持关闭。
  - **multidraw 顺序**：只有方块/实体绘制异常时才需要改；默认顺序由上游决定。
- 配置写入应用私有目录的 `mobileglues/config.json`，不修改 `/sdcard/MG`。

## LTW（实验性）

- 仅 arm64-v8a；其他设备不会显示。
- 只建议在明确知道风险时手动选择；自动模式不会选它。
- 参数页只暴露 LTW native 真正读取的环境变量（buffer flush、coherent storage、调试日志等）。
- 崩溃时请先换回 MobileGlues / GL4ES，再判断是不是 LTW 的问题。

## 驱动

- 只有渲染器插件走 Vulkan 时才显示「Vulkan 驱动」选项。
- 内置的 MobileGlues / GL4ES / LTW 不需要选驱动。
