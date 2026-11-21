# Renderers

## Built-in list

| Renderer | Status | Best for |
|---|---|---|
| Auto | Default | Users who do not want to choose; picks by device capability + version: MobileGlues → GL4ES → Ironized Zink |
| MobileGlues | Recommended | Most devices; translates desktop GL to host GLES |
| Ironized Zink | Optional | Desktop-level OpenGL 4.x / shaders on devices with a working Vulkan driver |
| GL4ES | Compatibility fallback | Old versions / devices where MobileGlues fails |
| LTW (experimental) | **Not recommended** | Risk-tolerant users who want a classic-version compatibility layer |

> Ironized Zink is a built-in renderer (Mesa Zink + Kopper over Vulkan); no renderer plugin is needed.
> Its Mesa/Kopper binaries and GPL-3.0 license are bundled with the APK.

## How to choose

1. Keep "Auto" first.
2. Black screen / artifacts / crash after entering the game:
   - switch to MobileGlues;
   - still failing: switch to GL4ES;
   - old versions (1.16 and earlier): prefer GL4ES; pick Ironized Zink when you want desktop OpenGL 4.x / shaders and the device has a working Vulkan driver.
3. Shaders / large modpacks: prefer MobileGlues and lower the OpenGL version if needed.
4. Do not install several renderer plugins at once; whichever is selected in the list is used.

## MobileGlues

- Settings → Renderers → MobileGlues:
  - **OpenGL version**: 4.0 by default; lower it when the driver does not support it.
  - **ANGLE backend**: off by default; keep it off if clouds/water/particle order look wrong.
  - **multidraw order**: only change it when block/entity drawing is broken; the default order comes from upstream.
- Config is written to the app-private `mobileglues/config.json`; `/sdcard/MG` is not touched.

## Ironized Zink

- Settings → Renderers → Ironized Zink (the config shows on this page, or tap its status card):
  - **Presets**: Potato / Performance / Default / Max Compatibility — they write the
    performance and compatibility switches only; the OpenGL version below is never touched.
  - **OpenGL version**: 3.3 – 4.6. All presets are tuned for 4.6; if the driver cannot
    translate Vulkan to Zink 4.6, lower it here and it applies on the next launch.
    Independent of the preset — switching presets will not reset it back to 4.6.
  - **Advanced options** (collapsed by default): threaded GL, big-core affinity,
    out-of-order drawing, VSync, relaxed GLSL, shader cache and more; saved immediately.
- Drivers: Zink uses Vulkan, so this page also shows the Vulkan driver and the
  "use system Vulkan driver" switch (a warning is shown on Adreno devices). Auto may pick
  Zink as well, so the driver options appear for Auto too.

## LTW (experimental)

- arm64-v8a only; other devices do not see it.
- Only pick it manually when you know the risk; Auto never selects it.
- The parameters page only exposes environment variables the LTW native code actually reads (buffer flush, coherent storage, debug log, etc.).
- If it crashes, switch back to MobileGlues / GL4ES first, then judge whether LTW was the cause.

## Drivers

- The "Vulkan driver" option appears for Ironized Zink (and Auto, which may pick it) and when a renderer plugin uses Vulkan.
- The built-in MobileGlues / GL4ES / LTW do not need a driver choice.
