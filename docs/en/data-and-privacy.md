# Data and privacy

## Where data lives

| Item | Location | After uninstall |
|---|---|---|
| Instances / game files / worlds / mods | public `/ZyNova/.Minecraft/` | kept |
| Launcher logs | public `/ZyNova/logs/` | kept |
| Java runtimes / JNA | private app storage | removed |
| Favorites archive | private app storage `files/favorites/` | removed |
| Control layouts | app external dir `control_layouts/` | removed |
| Settings / account tokens | MMKV (private app storage) | removed |
| MobileGlues config | app external dir `mobileglues/config.json` | removed |

"Private app storage" is under `Android/data/com.zynova.launcher/`, which most file managers hide.
Public storage is the plain `/storage/emulated/0/ZyNova/` folder you can open directly.

## Permissions

- "All files access" (Android 11+) is required, otherwise `/ZyNova` cannot be read or written and the
  launcher blocks on the startup page.
- Uninstalling does **not** delete game data under `/ZyNova`; accounts, settings and runtimes are removed.

## Log upload

- Nothing is uploaded to a public log platform unless you tap "Share link" yourself;
- Check that the log contains no tokens, cookies or server passwords before uploading (the launcher
  redacts common key formats, but do not rely on it);
- Crash analysis happens on-device and uploads nothing.

## API keys

- The AI API key is stored in private app settings;
- The CurseForge API key is injected into the APK at build time (upstream design) and is never published in the repo docs;
- Never put any key into issues, screenshots or chat logs.
