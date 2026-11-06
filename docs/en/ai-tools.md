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

It never gains permissions that were not granted.
