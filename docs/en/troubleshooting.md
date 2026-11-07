# Troubleshooting

## First: get the log

- Game crash: "Share logs" in the top-right corner of the crash screen;
- Launcher crash: Settings → About / the log entry;
- Run log: version overview → "Run log".

**A screenshot is worth less than a log**; the "crash analysis" on the crash screen is only a heuristic hint — the log is the source of truth.

## What the crash screen tells you

The crash screen shows a "Crash analysis · what to do next" card covering:

- missing dependency mods;
- mod load failure / Mixin injection failure;
- duplicate mods;
- Java version too old;
- out of memory;
- renderer / graphics driver init failure;
- corrupted files;
- missing classes / version mismatch.

Act on the hint and try again; if the hint looks wrong, share the log.

## Common problems

### The game crashes right after launch

1. Switch the renderer (MobileGlues ↔ GL4ES);
2. Switch Java (1.16 and older: Java 8/11; 1.17+: Java 17/21; 1.20.5+ usually 21);
3. Remove the mods / shaders you just added;
4. Look at the first `Caused by` in the log.

### Stuck on the loading screen

- Large modpacks unpack and download libraries on first launch; watch the task progress;
- Too little memory stalls it; raise the instance memory;
- Check whether `latest.log` keeps retrying the same download.

### Download fails / stuck at 0%

- Switch network / turn off the proxy;
- Read the exact error in the task panel;
- CurseForge resources may need a mirror; Modrinth is usually reachable directly.

### Login spinner never ends

See `login.md`; the polling problem is fixed — if it still fails, switch network and sign in again.

### A project I just favorited is not in the list

- Online favorites need a network refresh;
- Make sure you tapped ♥ and not the download button;
- Pull to refresh / re-enter the favorites page.

### Installing the APK fails

- Make sure the system is Android 8 or newer (minSdk 26);
- Uninstall the old build first (only when signatures differ);
- Make sure the download is complete (compare the file size).

## When reporting a problem, include

1. Device model / Android version / GPU;
2. Launcher version (Settings → About);
3. Game version + loader;
4. Reproduction steps;
5. A log link (do not send only screenshots).
