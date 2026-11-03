# MobileGlues

- 把桌面 OpenGL 翻译到宿主 GLES；多数设备首选。
- 设置 → 渲染器 → MobileGlues：
  - OpenGL 版本（默认 4.0）
  - ANGLE 后端（默认关）
  - multidraw 顺序（画面异常才动）
- 配置写在私有目录 `mobileglues/config.json`，不碰 `/sdcard/MG`。
