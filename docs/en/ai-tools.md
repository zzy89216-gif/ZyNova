# What the AI can do

- Install the game itself / modpacks / mods / resource packs / shaders / worlds (several game installs can run **at the same time**, see "Installs and music" below)
- Change instance settings (renderer, Java, memory, JVM arguments)
- Read and write `options.txt`
- List or remove favourites
- Control the music player (what is playing, play / pause, switch tracks, play by title)
- Read logs and launch the game (subject to the tool permissions)

It can also **bring up multiplayer end to end**:

- `multiplayer_status` — Terracotta toggle, custom nodes, SakuraFrp login state
- `frp_status` — SakuraFrp account (group / quota / traffic), tunnel list, frpc state
- `frp_create_tunnel` — creates a tunnel with **the type fixed to TCP, the local port auto-detected and a node picked automatically**
- `frp_start_tunnel` / `frp_stop_tunnel` — start and stop the tunnel

So a request like "get multiplayer working for me" can be completed without you ever telling it a port number.

It can also browse the web: `web_search`, `fetch_webpage`, `search_mcmod` (DuckDuckGo or the official DeepSeek
search; toggle in AI settings; internal addresses blocked)

It never gains permissions that were not granted.


## File capabilities

| Tool | Purpose |
|---|---|
| `list_files` / `read_file` / `write_file` / `delete_file` / `copy_file` | Inside the app (game directory); `delete_file` **deletes permanently, no recycle bin** |
| `public_list` / `public_read` / `public_write` / `public_mkdir` | Public storage such as Download (needs "All files access") |
| `public_move_to_recycle_bin` | Cleanup by **moving to the recycle bin** (app-private `.recycle_bin/`), never a real delete |
| `import_from_public` | Copy a jar / modpack from Download into the game directory |

Limits: only under `/storage/emulated/0/`; `Android/data` and `Android/obb` are blocked.


## Document generation

| Tool | Purpose |
|---|---|
| `create_docx` | Creates a Word document (title + body/bullets), saved to `Download/` by default |
| `create_pptx` | Creates a PPT; `slides` takes a JSON array `[{"title":"…","points":["…"]}]`, saved to `Download/` |

Both are **hand-written OOXML + zip** (no Apache POI, so the APK does not grow) and require
"All files access"; without it the tool tells you which switch to enable.
Just ask "make me a slide deck about X" and it will do the outline first, then generate the file.

## Real option keys

These are not plain strings in `options.txt`; the tools map them for you:

| Human wording | Real key | Real values |
|---|---|---|
| Graphics mode | `graphicsMode` | 0=fancy, 1=fast, 2=fabulous |
| Particles | `particles` | 0=all, 1=decreased, 2=minimal |
| FOV | `fov` | Normalised float, 1.0 = default 70° |


## Web access

- Switch: Settings → AI settings → Web access
- Two engines to choose from:
  - **DuckDuckGo** (default, no key);
  - **DeepSeek Search** (needs a search key in AI settings and bills per successful request) — reuse the same
    key when your main model is DeepSeek.
- When DeepSeek Search is unavailable (no key, invalid key, out of credit, rate limited, timeout, network error),
  the result starts with a **clear reason** and automatically falls back to DuckDuckGo: the turn keeps working,
  and the AI never pretends the results came from DeepSeek.
- DeepSeek returns long summaries only; when a page body is needed, the AI fetches that page separately.
- You can also let the AI change this itself: say "switch web search to DeepSeek" and it updates the search engine
  and search key (leave the key empty to reuse the main model's key). The same works for other AI settings
  (model / base URL / key / web access toggle).
- If parsing fails the AI falls back to `fetch_webpage`


## Installs and music

- `install_game` can **start several game installations at the same time**: installing a few versions in a row does
  not need to wait for the previous one, and every install is an independent background task with its own row in
  the top task bar. The only limit is that **instance names must be unique**: an instance that is already being
  installed or already exists is rejected with a clear reason, so to install several copies of the same game
  version, give each one a different name;
- `install_status` lists **every installation in progress** (target, progress, current step, elapsed time), plus the
  result of the latest one and the instances you already have (no longer a single task);
- `music_status` (read-only): the current track, playing / buffering / paused state, position and total duration
  (`--:--` when unknown), library size, and whether the current track has lyrics (the current lyric line is
  included when it does);
- `music_control`: `play` / `pause` / `toggle` / `next` / `previous` / `stop` / `seek`; `play` can start a
  specific track by title or file name, and `seek` takes a target in seconds. It only controls **the local
  library you imported yourself** — it never searches for or downloads music; when the library is empty it
  tells you to import audio on the Music page first.

## All 53 tools

| Category | Tools |
|---|---|
| Install and launch | `list_downloadable_versions` `install_game` `install_status` `launch_game` `get_device_info` |
| Resources and modpacks | `search_resources` `install_resource` `install_modpack` `list_mods` `set_mod_enabled` `delete_mod` `delete_resource` `list_folder` |
| Instances | `list_instances` `get_instance_detail` `get_instance_config` `set_instance_config` `set_game_option` |
| Files | `list_files` `read_file` `search_in_file` `write_file` `delete_file` `copy_file` |
| Public storage | `public_list` `public_read` `public_write` `public_mkdir` `public_move_to_recycle_bin` `import_from_public` |
| Documents | `create_docx` `create_pptx` |
| Logs and crashes | `list_logs` `read_log` `summarize_log` `read_crash_report` `read_crash_report_legacy` |
| Web | `web_search` `fetch_webpage` `search_mcmod` |
| Multiplayer | `multiplayer_status` `frp_status` `frp_create_tunnel` `frp_start_tunnel` `frp_stop_tunnel` |
| Music | `music_status` `music_control` |
| Favorites | `list_favorites` `remove_favorite` |
| Settings | `list_settings` `get_setting` `set_setting` `update_ai_setting` |
