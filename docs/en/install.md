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
- Overwriting an installation keeps instances, accounts and settings; **uninstalling wipes the private app directory** (see `data-and-privacy.md`).
- Root, Shizuku or ADB are not required to launch the game; they only affect a few advanced device features.

## 3. First launch

1. Choose language and theme (Settings → Basics).
2. Open the Download page, pick a Minecraft version and loader, and install the game.
3. Check Settings → Renderers: keep "Auto" unless you hit graphics problems.
4. Sign in on the Accounts page (Microsoft / offline / third-party).
5. Go back home, pick the instance and start it.

## 4. Storage and permissions

- Games, instances and Java runtimes all live in private app storage; uninstalling removes them together.
- External storage is only reached through the system file picker when exporting modpacks / logs / screenshots, so "all files access" is not needed.
- If you use custom backgrounds or modpack import, grant the directory the system dialog asks for.

> Tip: when no instance is installed yet, the home screen shows a "Download an instance" button that jumps straight into Download → Game.
