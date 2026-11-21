# Downloads and tasks

- Every install task appears in the thin top progress bar; tap it for title, step, progress, speed and cancel.
- Games, modpacks and resources share one queue and can run together.
- Cancel also cancels the real installer, not just the UI; the task state resets afterwards instead of
  getting stuck on "installing".
- These tasks **keep running in the background**: leaving the download page, switching apps or rotating the
  screen does not interrupt them, and the task panel still shows progress when you come back.
- **Completion and input prompts pop up on any screen**: install-success notices, the modpack version-name prompt
  and the mobile-data / notification permission reminders appear wherever you are, and leaving the download page
  does not cancel or suspend an install.
- **An interrupted install recovers safely**: installs keep a recovery record, so if the launcher is killed by
  the system the next start cleans up a half-finished new install, restores the old json / jar for an overwrite
  install, or cleans up modpack leftovers and asks you to reinstall. Resource files are written to `.part` and
  then renamed, so no half-written jars are left behind.
- **Importing a local modpack** also runs in this queue; see [install-modpack.md](install-modpack.md) for the steps.
- **One-tap download / install**: when you open the resource pages from "Version settings → Resources", mods, resource
  packs, shaders and worlds get a download button on their card; one tap installs that version plus its **required
  dependencies** into the current instance, with no detour through the details page. Modpack version lists work the
  same way — one button per row installs that version directly.
- **One-tap download on favorites and modpack search results needs no instance context**: the big button on a favorites
  card and the button next to the ♥ on a modpack result work straight from the home screen (installing into the current
  instance), without visiting an instance's resource manager first.
- The "installed" marker on resource details **refreshes after an install** — no need to leave and re-enter. When filters
  match no version you get an empty state with "Show all versions"; an empty search can be retried directly.
- When every download source fails, the app lists what went wrong for each source and suggests checking the network or
  switching the mirror on/off.
