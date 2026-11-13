# Data locations

Game data lives in **public storage**, visible in any file manager:

| Content | Location |
|---|---|
| Launcher root | `/storage/emulated/0/ZyNova/` |
| Instances / game data | `ZyNova/.Minecraft/` (dot-prefixed, hidden by default in file managers) |
| Saves | `ZyNova/.Minecraft/<instance>/saves/` |
| Mods / resource packs / shaders | `ZyNova/.Minecraft/<instance>/mods`, `resourcepacks`, `shaderpacks` |
| Skin / cape cache | `ZyNova/.Minecraft/account_skins`, `account_capes` |
| Launcher logs | `ZyNova/logs/` |
| Java runtimes / JNA | private app storage (public storage is mounted noexec, so game runtimes cannot start from there) |
| Settings / tokens | MMKV (private app storage) |
| MobileGlues config | `mobileglues/config.json` |

The root only holds two things: `logs` (so a log is easy to find when you report a problem) and
`.Minecraft` (the game data). Instances, saves and mods live inside `.Minecraft`, which keeps the
file manager tidy while still letting you edit configs or drop in mods.

- **New installs** need no migration: everything starts under `/ZyNova/.Minecraft`.
- **Upgrading from an older version**: if game data is still in the private app folder, the launcher
  migrates it **before the main screen** and shows a full-screen progress page; you tap Next when it
  finishes. It copies both your instances (`files/.minecraft`) and the launcher data (`files/games`:
  runtimes, skin cache, favourites) into `.Minecraft`, verifies every file, and only then removes the old
  private folders. On failure the partial copy is cleaned up and the launcher falls back to private
  storage, so nothing is lost. Closing the app mid-way is fine; migration resumes next time.
- The default entry under "Choose folder" is `/ZyNova/.Minecraft`; stale records that pointed at the
  private folder are cleaned up, so you never see a path you cannot open.
- "All files access" is required to read and write `/ZyNova`; without it the launcher cannot be used
  (a startup page blocks and guides you to grant it).
- **Revoking it while the app runs** immediately brings the permission page back and the launcher does
  **not** switch the game directory to private storage: your data stays in `/ZyNova/.Minecraft` and comes
  back the moment you re-grant, with no reinstall and no data loss.
- Uninstalling the app does **not** delete game data under `/ZyNova`; remove it there yourself.
