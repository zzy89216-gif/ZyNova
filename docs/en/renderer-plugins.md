# Renderer plugins

Only three renderers ship with the launcher: MobileGlues, GL4ES and LTW. Devices those do not cover are
served by **renderer plugins**. A plugin is a normal APK; once installed, its renderers show up in the
renderer list automatically — **no launcher restart needed**.

## Where to get them

- Settings → Renderer → download plugins (the app offers two entry points: a GitHub mirror release and a
  Chinese file host).
- Only install plugins whose origin you can verify. A random renderer APK from the internet may contain a
  modified driver binary, which makes problems very hard to diagnose.

Common upstreams:

| Plugin | Upstream | Notes |
|---|---|---|
| FCL Renderer Plugin (bundle of many renderers) | <https://github.com/ShirosakiMio/FCLRendererPlugin/releases/tag/Renderer> | The official `FCL-Team/FCLRendererPlugin` repo was taken down by GitHub; the in-app link points at this mirror release |
| Ironized Zink (Zink over Vulkan) | <https://github.com/GoyDevv/IronizedZink> | Use **1.1.0 or newer**: older builds pinned OpenGL to 4.6, which old devices cannot lower |
| MobileGlues (plugin build) | <https://github.com/MobileGL-Dev/MobileGlues-release> | A different binary from the built-in one, see "two entries with the same name" below |
| LTW (plugin build) | <https://github.com/layou233/LTWRendererPlugin> | Updated less often; the bundled LTW-Turbo is more active |

> Names like "OpenLTW" and "Vulkan Fix" have **no traceable upstream repository**. The project neither
> ships nor endorses them — if you install one, verify the author and the source yourself.

## Normal but confusing behaviour

**Two entries with the same name is normal.**
The built-in and the plugin renderer are two different renderers (different identifiers) and are listed
side by side, e.g. "MobileGlues" and "MobileGlues (from plugin …)". Either works: the built-in one updates
with the launcher, the plugin one updates with the plugin.

**A plugin is installed but not listed.**
Usually its identifier (UUID) collides with a renderer that is already registered. The launcher refuses to
register a duplicate and only logs a warning. Uninstall the conflicting plugin (or keep just one) and check again.

**A plugin has no gear icon.**
Only plugins that declare themselves configurable get one; without it the launcher has no parameters to pass.

## Practical advice

- Do not enable several plugins that touch the same GL environment (e.g. two Zink-based ones); they tend to
  overwrite each other's environment variables.
- Plugin updates take effect immediately — the launcher does not cache versions or enforce a minimum version.
  Whether a renderer actually works is only proven by launching a game.
- If the game shows a black screen or crashes, switch back to "Auto" or the built-in MobileGlues first, then
  judge whether the plugin is at fault.
- To lower the OpenGL version on an old device, use Ironized Zink's own "OpenGL version / presets"; the
  launcher does not override them.
