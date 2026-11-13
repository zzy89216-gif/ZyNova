# Renderer overview

| Renderer | Status | Best for |
|---|---|---|
| Auto | default | You do not want to choose: picked per Minecraft version |
| MobileGlues | recommended | Most devices (1.17 and newer) |
| GL4ES | fallback | Older versions (1.16 and older) / unusual devices |
| LTW | experimental | arm64 only, if you accept the risk |

- **"Auto" is not a real renderer**: it picks by **Minecraft version** (not by GPU performance), preferring
  MobileGlues and then GL4ES; the first one that declares support for that version wins.
- Switch under **Settings → Renderer**. After selecting a concrete renderer, **tap its status card** to open
  that renderer's detailed settings page.
- Do not stack multiple renderer plugins.
- Ironized Zink was removed; use a renderer plugin if you need Zink.
