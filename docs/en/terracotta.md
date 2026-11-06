# Terracotta multiplayer (P2P)

No port forwarding, **both players need ZyNova**. Entry: Multiplayer.

## Host

1. In game: `Esc` → **Open to LAN**, note the port in chat;
2. Launcher → Multiplayer → Create room, wait for node scanning;
3. A room code appears — tap to copy and send it over.

## Guest

1. Multiplayer → Join room → paste the code;
2. Copy the server address that appears, then use “Direct Connect” in game.

Room codes look like `U/XXXX-XXXX-XXXX-XXXX` (base-34, `I`/`O` excluded).
The field normalizes pasted input (full-width characters, spaces, dashes, brackets, missing `U/`,
`I`→`1`, `O`→`0`) and tells you the normalized form. “Invalid room code” means the string itself is bad.

## Troubleshooting

| Symptom | Fix |
|---|---|
| Hosting fails | Re-open the world to LAN in game, check the port, retry |
| Joining fails | Make sure the code is fresh and the host is still in; try another network |
| Stuck scanning nodes | Retry; the node list is cached for 10 minutes |
| Connected but game won't connect | Host must not pause; match versions and mods |
| Custom node | Multiplayer → enable → Custom server node |

Logs: Multiplayer → Share core log (it contains network addresses).

Versus [SakuraFrp](https://github.com/zzy89216-gif/ZyNova/blob/main/docs/en/frp.md): Terracotta needs ZyNova on both sides;
SakuraFrp only needs the host to have an account, and vanilla clients can join.
