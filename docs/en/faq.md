# FAQ

**Root/Shizuku?** Not needed to play.
**Microsoft login?** Yes, device-code flow; offline and third-party are also available.
**Best renderer?** Auto by default; MobileGlues for most devices, GL4ES as fallback.
**Where is Ironized Zink?** Removed; use a renderer plugin.
**Why is there only one APK now?** The old "arm64 slim" build had identical features and only saved a little size while making everyone check their device first. It has been dropped; the universal build is the only package, and LTW still works on arm64 phones.
**Does uninstalling delete games?** **No.** Instances, worlds and mods live in `/ZyNova/.Minecraft` and stay.
Only private app storage is removed: Java runtimes, the favourites archive, accounts and settings.
Copying `.Minecraft` elsewhere is still wise before switching devices.
**Why is "All files access" mandatory?** Game data lives where a file manager can reach it (`/ZyNova`), and
Android 11+ only allows that with the permission granted. Without it the startup page blocks you: not a
workaround, the launcher simply cannot work.
**Where exactly is my data?** Public `/storage/emulated/0/ZyNova/`: `logs/` for logs, `.Minecraft/` for game data.
Private storage (`Android/data/com.zynova.launcher/`) only holds runtimes, the favourites archive, accounts and settings.
**Where do I sign in with LittleSkin?** Accounts, then Add account, then **LittleSkin quick sign-in** in the
login menu; one tap opens the username/password dialog.
**I revoked the permission by accident — is my data gone?** Nothing is lost. The launcher immediately returns
to the permission page and does **not** switch the game directory to private storage; allow "All files access"
again and the data is back (the files never left `/ZyNova/.Minecraft`).
**The UI stutters?** Glass has a single background blur and no always-on animations. If it is still heavy,
turn "background blur" down or set "background opacity" to 100% (blur is skipped then).
**Does the AI delete things?** In full-control mode it can execute writes; use confirm mode if unsure.
**Logs private?** They may contain IDs and paths; review before sharing.

**How can I reach you outside GitHub and Discord?**
Two mailboxes — pick the one that matches your ecosystem:

| Mailbox | For |
|---|---|
| <ZyNovaLauncher@163.com> | China ecosystem (Chinese mailboxes incl. QQ Mail) |
| <ZyNovaLauncher@outlook.com> | Global ecosystem (overseas users, English) |
| <https://x.com/ZyNovaProject> | Official X account (announcements, release news) |

Please include the launcher version (Settings → About), game version + loader, reproduction steps and a log link.
