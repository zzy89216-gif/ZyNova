# Installation and first launch

## 1. Which APK

Every release ships one universal APK:

| File | Devices | Notes |
|---|---|---|
| `ZyNova-<version>.apk` | All Android devices | Universal: arm64-v8a / armeabi-v7a / x86 / x86_64 |

The old `-arm64-v8a` slim build has been dropped; the universal build has the full feature set on arm64 phones too.
The experimental LTW renderer only ships arm64 libraries, so it never shows up on 32-bit / x86 devices.

## 2. Install

- Install the APK directly; when Android warns about "unknown sources", allow the browser or file manager you used.
- Overwriting an installation keeps instances, accounts and settings; **uninstalling does not delete game data**, it only removes accounts, settings and Java runtimes (see `data-and-privacy.md`).
- Root, Shizuku or ADB are not required to launch the game; they only affect a few advanced device features.

## 3. First launch

1. Choose language and theme (Settings → Basics).
2. Open the Download page, pick a Minecraft version and loader, and install the game.
3. Check Settings → Renderers: keep "Auto" unless you hit graphics problems.
4. Sign in on the Accounts page (Microsoft / offline / third-party).
5. Go back home, pick the instance and start it.

## 4. Storage and permissions

- **Game data lives in public storage** at `/storage/emulated/0/ZyNova/.Minecraft/` (instances, worlds, mods,
  resource packs, shaders, skin cache), with launcher logs next to it in `ZyNova/logs/`, all visible in any file manager.
- **"All files access" is required** (Android 11+): without it `/ZyNova` cannot be read or written and the
  startup page blocks you and opens the permission screen. It is a hard requirement, not an option: game data
  only stays reachable if the launcher may write to public storage.
- Java runtimes and JNA stay in private app storage (public storage is mounted noexec, so runtimes cannot start
  from there); uninstalling removes them.
- First launch goes: region check, storage permission, three statements plus age, (upgrading users) data
  migration, then the main screen.

> Tip: when no instance is installed yet, the home screen shows a "Download an instance" button that jumps straight into Download → Game.
