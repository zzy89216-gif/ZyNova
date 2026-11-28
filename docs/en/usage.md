# Daily usage

## Interface

- The top bar is a **centered liquid-glass capsule**, left to right:
  **Home / AI / Multiplayer / Downloads / Music / Files / Settings**.
- The title stays in the top-left corner; a back button appears on sub-pages.
- While a task is running, a small ring appears at the right end of the capsule — tap it to open the task panel.
- Outside the music page, a compact "now playing" mini bar sits right below the top bar: song title on the left,
  play / pause and previous / next on the right; tap the bar to open the music page. It also shows while paused,
  and hides itself while nothing has been played yet or when there is no current track.
- Appearance options (theme, wallpaper, corner radius, glass, background blur and more) live under **Settings → Launcher**; the top bar no longer has a separate **Personalization** entry.

## Downloading and installing the game

1. Top capsule → **Downloads**.
2. Choose **Game**, pick a Minecraft version, then a loader (Vanilla / Fabric / Quilt / Forge / NeoForge ...).
3. Tap install; progress shows in the thin top bar, tap it for the task panel (title / step / progress / speed / cancel).
4. When it finishes the instance shows up on the home screen.

> You can **run several downloads / installs at once**: start the next one without waiting for the previous,
> each with its own row in the task panel and its own progress, speed and cancel. Modpacks, mods, resource packs,
> shaders and worlds are all under the same **Downloads** entry, switch with the left categories.

## Instances

- To open the instance page: the home screen no longer shows the old instance cards; open it from the **Versions** list (tap an instance entry).
- **Version settings** changes Java, memory, renderer, resolution and JVM arguments.
- **Mod management** enables, disables or deletes mods; Fabric API is added by the Fabric flow and must not be installed twice by hand.
- **Run log** lives on the version overview page; when something crashes, share the log from there first.

### Deleting an installed version

- Entry: **Version list** → the menu next to a version → **Delete** → confirm **Delete Version**.
- Deletion shows a **full-screen progress**: it first counts the files (count + size), then deletes them one by one,
  showing the percentage, file count, deleted / total size and the current file — so it no longer looks stuck.
  You can cancel at any time.
- Cancelling **restores the version when possible**: files that were not deleted yet are kept, and the final
  notice tells you how many files had already been deleted if the cancel came too late.
- Deletion covers everything under `./versions/<version-name>`; with version isolation on, that includes this
  version's worlds, resource packs and mods. **Back up anything you want to keep first.**

## Home

The home screen is **card-based** by default (switch between Blank / Card Home / Load from Local / Load from URL under Settings → Launcher → Home → Home type). From top to bottom:

1. **Recently played**: worlds you played last, aggregated across instances, up to four of them. Tap one to launch it; the fifth card, **View all**, opens the **All worlds** list with search, sorting and filtering by instance / game version — see [Installing or importing worlds](https://github.com/zzy89216-gif/ZyNova/blob/main/docs/en/install-world.md).
2. **Custom home**: news cards below the worlds section (Minecraft news / Java version updates / Modrinth / CurseForge / Minecraft community), **on by default**. The switch (Custom home feed) is under Settings → Launcher, and cards can be added, removed, hidden or dragged into a new order. When offline or when a request fails it uses the local cache and never blocks the rest of the home page — see [Custom home page](https://github.com/zzy89216-gif/ZyNova/blob/main/docs/en/custom-home.md).

## Export

- Instance page → Export → currently **Modrinth modpack format only**.
- You can choose whether to write Modrinth / CurseForge remote resources into the index; the launcher asks first.
- The resulting `.mrpack` can be imported by other common launchers.

## Favorites

- Tap ♥ on a search result card or project page to save it.
- The favorites page has "online favorites" (links) and "local archive" (copies stored in private app storage).
- A favorites card has a big download button on the right — one tap installs it (required dependencies included);
  the small ♥ is the unfavorite button.
- See `favorites.md`.

## Common entries

| I want to... | Go to |
|---|---|
| Change the renderer | Settings → Renderer |
| Change memory / Java | Instance → Version settings |
| See why it crashed | Crash screen → "Crash analysis — what to do" |
| Share a log | Crash screen top-right / Version overview → Run log |
| Configure AI | Settings → AI settings |
| Find a particular world | Home → "Recently played" → "View all" |
| Turn the home feed off | Settings → Launcher |

## Back key and ESC in game

- **The back key only toggles the launcher menu**: inside the game it opens the launcher's control menu
  (mouse / gamepad / gestures / gyroscope, logs, exit) and never jumps around inside the game;
- **To reach Minecraft's own pause menu**: open the launcher menu → tap "Send ESC (game pause menu)";
- The menu also has "Back to the launcher", which **does not stop the game** — you return to the same session;
- The menu's **Multiplayer** entry collapses the menu, returns to the launcher and **opens the Multiplayer page
  directly** (both Terracotta and SakuraFrp live there) — **the game does not quit; it keeps running in the background**;
- Changing controls, checking logs or switching input methods all happen in this menu — no need to quit the game.
