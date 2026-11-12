# Data locations

Game data lives in **public storage**, visible in any file manager:

| Content | Location |
|---|---|
| Instances / games | `/storage/emulated/0/ZyNova/` |
| Saves | `ZyNova/<instance>/saves/` |
| Mods / resource packs / shaders | `ZyNova/<instance>/mods`, `resourcepacks`, `shaderpacks` |
| Java runtimes | `ZyNova/runtimes/` |
| Favorites archive | `ZyNova/favorites/` (older versions used the private `files/favorites/`) |
| Launcher logs | app external dir `Android/data/com.zynova.launcher/files/logs/` |
| Settings / tokens | MMKV (private app storage) |
| MobileGlues config | `mobileglues/config.json` |

- Upgrading from an older version: if game data is still in the private app folder, the launcher asks once whether to move it to `/ZyNova`. Choosing "Later" deletes nothing; you can also copy `files/games/` into `ZyNova/` manually.
- "All files access" is required to read and write `/ZyNova`. Without it the launcher falls back to private storage and reminds you before launching a game.
- Uninstalling the app does **not** delete game data under `/ZyNova`; remove it there yourself.
