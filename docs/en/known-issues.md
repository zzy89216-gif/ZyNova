# Known issues and limits

- Ironized Zink removed; use a plugin for Zink.
- LTW is arm64-only and experimental.
- Background blur still costs something on very low-end devices (capped at 24dp, 12dp on <=4-core devices).
- Export supports Modrinth only.
- Uninstalling clears private app storage (runtimes, favourites archive, accounts, settings); game data under
  `/ZyNova` is kept.
- Under 18, launching is limited to Friday / Saturday / Sunday and public holidays, 20:00-21:00
  (based on the age chosen on first launch).
- Holiday data for the under-18 time window is updated per year. Once the system year passes the data year, the gate
  falls back to ordinary weekend rules (the 20:00-21:00 window stays) and says the holiday data has expired.
