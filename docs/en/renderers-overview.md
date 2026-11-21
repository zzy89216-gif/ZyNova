# Renderer overview

| Renderer | Status | Best for |
|---|---|---|
| Auto | default | You do not want to choose: picked by device capability + Minecraft version |
| MobileGlues | recommended | Most devices (1.17 and newer) |
| Ironized Zink | optional | Desktop OpenGL 4.x / shaders when the Vulkan driver works |
| GL4ES | fallback | Older versions (1.16 and older) / unusual devices |
| LTW | experimental | arm64 only, if you accept the risk |

- **"Auto" is not a real renderer**: it first checks what the device can run (GLES version / Vulkan), then takes
  the first version-compatible one in the order **MobileGlues → GL4ES → Ironized Zink**. It only checks
  "can it run" — no benchmarks.
- Switch under **Settings → Renderer**. After selecting a concrete renderer, **tap its status card** to open
  that renderer's detailed settings page.
- Do not stack multiple renderer plugins.
- Ironized Zink is built in — no renderer plugin needed; its OpenGL version and presets are independent (see `renderers.md`).
