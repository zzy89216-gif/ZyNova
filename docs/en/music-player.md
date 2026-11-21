# Built-in music player

The launcher ships a small music player: import local audio, play it as a list, and switch tracks from inside the game without a second app.

## Where to find it

- The **Music** entry in the launcher's top bar capsule;
- In game: open the launcher menu (the floating menu on screen) → **Music** in the right column; the panel controls playback without leaving the game.

## Importing music

- Tap **Import** → the system file picker; you can select several files at once;
- Common formats work: MP3, AAC (M4A), FLAC, OGG / Opus, WAV. Files that are not audio are skipped with a notice;
- Imported files are **copied into the launcher's private storage** (`files/music/`), so deleting the originals will not break playback;
- Files with the same name are renamed automatically instead of overwriting each other.

## Playback

- Play / pause, previous / next, drag the progress bar, see the current time;
- When a track ends the next one starts automatically (the list loops);
- The last track and position are remembered, so reopening the player continues where you left off;
- The delete icon on the right of each row removes that track — only the launcher's copy, never your original file.

## Background playback

- While playing, the notification shows controls: play / pause, previous / next; swiping the notification away stops playback;
- Playback continues when you switch to another app or go back to the home screen;
- On Android 13 and newer the notification controls need notification permission. If it is denied, foreground playback still works — there are just no notification buttons.

## Audio focus and headphones

- When another app takes audio focus, the player pauses and yields; it never talks over the game or other apps;
- Unplugging headphones pauses playback.

## Limits

- APE, WMA and DSD are not supported;
- No lyrics, no cover art, no online streaming;
- Lock-screen and Bluetooth media keys are not supported.

## Moving to a new device / uninstalling

- The library lives in private app storage at `files/music/`, which most file managers hide;
- **Uninstalling the launcher deletes the library** — copy the files elsewhere if you want to keep them.
