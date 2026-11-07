# Renderers

## Built-in list

| Renderer | Status | Best for |
|---|---|---|
| Auto | Default | Users who do not want to choose; picks MobileGlues → GL4ES per version |
| MobileGlues | Recommended | Most devices; translates desktop GL to host GLES |
| GL4ES | Compatibility fallback | Old versions / devices where MobileGlues fails |
| LTW (experimental) | **Not recommended** | Risk-tolerant users who want a classic-version compatibility layer |

> Ironized Zink was removed. Use a renderer plugin if you need Zink; Mesa/Kopper are no longer bundled.

## How to choose

1. Keep "Auto" first.
2. Black screen / artifacts / crash after entering the game:
   - switch to MobileGlues;
   - still failing: switch to GL4ES;
   - old versions (1.16 and earlier): prefer GL4ES.
3. Shaders / large modpacks: prefer MobileGlues and lower the OpenGL version if needed.
4. Do not install several renderer plugins at once; whichever is selected in the list is used.

## MobileGlues

- Settings → Renderers → MobileGlues:
  - **OpenGL version**: 4.0 by default; lower it when the driver does not support it.
  - **ANGLE backend**: off by default; keep it off if clouds/water/particle order look wrong.
  - **multidraw order**: only change it when block/entity drawing is broken; the default order comes from upstream.
- Config is written to the app-private `mobileglues/config.json`; `/sdcard/MG` is not touched.

## LTW (experimental)

- arm64-v8a only; other devices do not see it.
- Only pick it manually when you know the risk; Auto never selects it.
- The parameters page only exposes environment variables the LTW native code actually reads (buffer flush, coherent storage, debug log, etc.).
- If it crashes, switch back to MobileGlues / GL4ES first, then judge whether LTW was the cause.

## Drivers

- The "Vulkan driver" option appears only when a renderer plugin uses Vulkan.
- The built-in MobileGlues / GL4ES / LTW do not need a driver choice.
