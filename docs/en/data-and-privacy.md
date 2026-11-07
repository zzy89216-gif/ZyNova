# Data and privacy

## Where data lives

| Item | Location |
|---|---|
| Instances / game files | Private app storage |
| Java runtimes | Private app storage |
| Favorites archive | Private app storage `files/favorites/` |
| Settings / account tokens | MMKV (private app storage) |
| MobileGlues config | Private app storage `mobileglues/config.json` |

## Uninstalling

Uninstalling removes the whole private directory, including instances, saves, the favorites archive, accounts and settings.
Export anything you want to keep first (saves / modpacks / logs).

## Log upload

- Nothing is uploaded to a public log platform unless you tap "Share link" yourself;
- Check that the log contains no tokens, cookies or server passwords before uploading;
- Crash analysis happens on-device and uploads nothing.

## API keys

- The AI API key is stored in private app settings;
- The CurseForge API key is injected into the APK at build time (upstream design) and is never published in the repo docs;
- Never put any key into issues, screenshots or chat logs.
