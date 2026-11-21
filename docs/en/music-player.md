# Built-in music player

The launcher ships a small music player: import local audio, play it as a list, read lyrics, and switch tracks
from inside the game without a second app.

## Where to find it

- The **Music** entry in the launcher's top bar capsule;
- The "now playing" mini bar right below the top bar: whenever you are not on the music page and a current track
  exists (also while paused) it shows the song title on the left and play / pause, previous / next on the right.
  Tap the bar to open the music page;
- In game: open the launcher menu (the floating menu on screen) → **Music** in the right column; the panel controls
  playback without leaving the game.

## Importing music

- Tap **Import** → the system file picker; you can select several files at once;
- Common formats work: MP3, AAC (M4A), FLAC, OGG / Opus, WAV. Files that are not audio are skipped with a notice;
- Imported files are **copied into the launcher's private storage** (`files/music/`), so deleting the originals will not break playback;
- Files with the same name are renamed automatically instead of overwriting each other;
- Lyrics do **not** have to be picked together with the audio; add them per track later (see "Lyrics").

## Picking a track and playback

- Tap any row in the library to switch to that track; the row that is playing is highlighted;
- Play / pause, previous / next;
- Progress bar: while you drag it shows where you are dragging to, and releasing jumps there. Elapsed time and
  total duration are always visible — pausing no longer freezes or hides the position. While the duration is
  still unknown it shows `--:--`;
- When a track ends the next one starts automatically (the list loops);
- The last track and position are remembered, so reopening the player continues where you left off;
- The delete icon on the right of each row removes that track — only the launcher's copy, never your original file.

## Lyrics

- To add lyrics to a track: tap **Import lyrics** on the music page and pick any `.lrc` file (only `.lrc` is
  accepted, other formats are rejected with a notice) — **it does not have to share the track's name**. The
  current track shows the lyrics **immediately**, no need to restart playback;
- Importing copies the lyrics to `files/music/<full audio file name>.lrc` (for example `Sunny.mp3.lrc`) in the
  launcher's private storage, so `a.mp3` and `a.m4a` each keep their own lyrics instead of overwriting each other;
  lyrics imported earlier still show up;
- Switching tracks or importing lyrics takes a moment to parse: the lyrics area shows "Loading lyrics…" first,
  then switches to the lyrics (or the "no lyrics" hint);
- During playback the current line is highlighted, the neighbouring lines stay visible, and the view scrolls to
  the current line automatically;
- Common LRC timestamps (`[mm:ss]` / `[mm:ss.xx]`) are supported: a line with several timestamps is shown at
  each of them. Lines without a timestamp (credit lines such as "Lyrics:" for example) are skipped and never
  break the rest of the file;
- You do not need to convert the file: UTF-8, UTF-16 (with BOM) and GBK are detected automatically;
- The lyrics file is limited to 1 MiB; above that, or when no timestamped line can be read from the file, the app
  says it could not read any lyrics;
- A track without lyrics is not an error — the lyrics area just says there are none yet.

## Background playback

- While playing, the notification shows controls: play / pause, previous / next; swiping the notification away stops playback;
- When the track has lyrics, the notification shows the **previous line + the current one**, and expanding the
  notification reveals both (updated only when the line changes, not once a second);
- While paused the notification shows "paused" instead of a lyric line;
- Playback continues when you switch to another app or go back to the home screen;
- On Android 13 and newer the notification controls need notification permission. If it is denied, foreground playback still works — there are just no notification buttons.

## Audio focus and headphones

- When another app takes audio focus, the player pauses and yields; it never talks over the game or other apps;
- Unplugging headphones pauses playback.

## Limits

- APE, WMA and DSD are not supported;
- Lyrics support common LRC timestamps only — **word-by-word lyrics** (krc and similar) are not supported;
- No cover art and no online streaming;
- Lock-screen and Bluetooth media keys are not supported.

## Moving to a new device / uninstalling

- The library lives in private app storage at `files/music/`, which most file managers hide; lyrics live there too,
  named after the full audio file name (for example `Sunny.mp3.lrc`);
- **Uninstalling the launcher deletes the library (lyrics included)** — copy the files elsewhere if you want to keep them.
