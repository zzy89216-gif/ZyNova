# Known issues and limits

- Ironized Zink removed; use a plugin for Zink.
- LTW is arm64-only and experimental.
- Background blur still costs something on very low-end devices (capped at 24dp, 12dp on <=4-core devices).
- Export supports Modrinth only.
- Uninstalling clears private app storage (runtimes, favourites archive, accounts, settings); game data under
  `/ZyNova` is kept.
- Under 18, launching is limited to Friday / Saturday / Sunday and public holidays, 20:00-21:00
  (based on the age chosen on first launch).
