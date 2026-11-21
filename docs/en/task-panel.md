# Task panel

- The thin progress bar at the top only appears while tasks run.
- Tap it to expand: title / step / progress / percentage / speed / cancel.
- It collapses by itself when everything finishes and never pops up over the UI — completion prompts and
  text inputs are separate dialogs and can appear on any screen (see [downloads.md](downloads.md)).

## Tasks that run here

These keep running **in the background**: you can leave the page or switch apps and the progress is
still there when you come back:

- game installation;
- modpack download and install;
- **modpack import** (importing a local archive).

If the process is killed by the system, the next start runs a safe recovery (clean up a half-finished install,
roll back an overwrite install, or clean up modpack leftovers and ask you to reinstall).

Cancel from the panel to stop them; the underlying installer or importer stops too, so it never ends up
"canceled in the UI but still writing files". Cancelling resets the task state instead of leaving it stuck
on "installing".
