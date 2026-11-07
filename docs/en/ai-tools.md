# What the AI can do

- Install the game itself / modpacks / mods / resource packs / shaders / worlds
- Change instance settings (renderer, Java, memory, JVM arguments)
- Read and write `options.txt`
- List or remove favourites
- Read logs and launch the game (subject to the tool permissions)

Since 28.0.1 it can also **bring up multiplayer end to end**:

- `multiplayer_status` — Terracotta toggle, custom nodes, SakuraFrp login state
- `frp_status` — SakuraFrp account (group / quota / traffic), tunnel list, frpc state
- `frp_create_tunnel` — creates a tunnel with **the type fixed to TCP, the local port auto-detected and a node picked automatically**
- `frp_start_tunnel` / `frp_stop_tunnel` — start and stop the tunnel

So a request like "get multiplayer working for me" can be completed without you ever telling it a port number.

It can also browse the web: `web_search`, `fetch_webpage`, `search_mcmod` (toggle in AI settings; internal addresses blocked)

It never gains permissions that were not granted.


## File capabilities (28.0.5)

| Tool | Purpose |
|---|---|
| `list_files` / `read_file` / `write_file` / `delete_file` / `copy_file` | Inside the app (game directory) |
| `public_list` / `public_read` / `public_write` / `public_mkdir` | Public storage such as Download (needs "All files access") |
| `public_move_to_recycle_bin` | Cleanup by **moving to the recycle bin** (app-private `.recycle_bin/`), never a real delete |
| `import_from_public` | Copy a jar / modpack from Download into the game directory |

Limits: only under `/storage/emulated/0/`; `Android/data` and `Android/obb` are blocked.


## Document generation (28.0.6)

| Tool | Purpose |
|---|---|
| `create_docx` | Creates a Word document (title + body/bullets), saved to `Download/` by default |
| `create_pptx` | Creates a PPT; `slides` takes a JSON array `[{"title":"…","points":["…"]}]`, saved to `Download/` |

Both are **hand-written OOXML + zip** (no Apache POI, so the APK does not grow) and require
"All files access"; without it the tool tells you which switch to enable.
Just ask "make me a slide deck about X" and it will do the outline first, then generate the file.

## Real option keys (28.0.6)

These are not plain strings in `options.txt`; the tools map them for you:

| Human wording | Real key | Real values |
|---|---|---|
| Graphics mode | `graphicsMode` | 0=fancy, 1=fast, 2=fabulous |
| Particles | `particles` | 0=all, 1=decreased, 2=minimal |
| FOV | `fov` | Normalised float, 1.0 = default 70° |


## Web access (28.0.7)

- One switch only: Settings → AI settings → Web access
- Always **DuckDuckGo** (no key) — no engine or API key to configure
- If parsing fails the AI falls back to `fetch_webpage`
