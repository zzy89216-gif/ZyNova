# What the AI can do

- Install the game itself / modpacks / mods / resource packs / shaders / worlds
- Change instance settings (renderer, Java, memory, JVM arguments)
- Read and write `options.txt`
- List or remove favourites
- Read logs and launch the game (subject to the tool permissions)

It can also **bring up multiplayer end to end**:

- `multiplayer_status` — Terracotta toggle, custom nodes, SakuraFrp login state
- `frp_status` — SakuraFrp account (group / quota / traffic), tunnel list, frpc state
- `frp_create_tunnel` — creates a tunnel with **the type fixed to TCP, the local port auto-detected and a node picked automatically**
- `frp_start_tunnel` / `frp_stop_tunnel` — start and stop the tunnel

So a request like "get multiplayer working for me" can be completed without you ever telling it a port number.

It can also browse the web: `web_search`, `fetch_webpage`, `search_mcmod` (toggle in AI settings; internal addresses blocked)

It never gains permissions that were not granted.


## File capabilities

| Tool | Purpose |
|---|---|
| `list_files` / `read_file` / `write_file` / `delete_file` / `copy_file` | Inside the app (game directory) |
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
- If DeepSeek Search fails (no key, out of credit, rate limited, timeout) it **silently falls back** to
  DuckDuckGo instead of failing the turn
- If parsing fails the AI falls back to `fetch_webpage`


## All 51 tools

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
| Favorites | `list_favorites` `remove_favorite` |
| Settings | `list_settings` `get_setting` `set_setting` `update_ai_setting` |
