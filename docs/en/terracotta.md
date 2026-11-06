# Terracotta multiplayer (P2P)

ZyNova has two ways to play together; Terracotta is the one that opens no ports:

| Way | How it works | Requirements | Good for |
|---|---|---|---|
| Terracotta | P2P mesh (EasyTier); both sides join the same virtual LAN | Both players run ZyNova | Friends, no port forwarding |
| [SakuraFrp](https://github.com/zzy89216-gif/ZyNova/blob/main/docs/en/frp.md) | Maps your local port to a public node (frp) | A SakuraFrp account + access key | The other side uses a desktop client, or you don't want Terracotta |

Entry: **Multiplayer**.

## Prerequisites

1. Both sides install ZyNova (same version recommended);
2. First run asks you to accept the user notice and requests **notification permission** (the session is kept alive by a foreground notification);
3. Hosting or joining asks for **VPN permission** — that is how EasyTier builds the mesh, so it is expected.

> The core (`libterracotta.so`) and the VPN service are upstream components; their protocol is not modified.

## Host: create a room

1. In game, `Esc` → **Open to LAN**, note the port shown in chat;
2. Back to the launcher (or the in-game overlay) → Multiplayer → **Create room**;
3. Wait for node scanning (the node list is cached for **10 minutes**, so repeated attempts don't refetch);
4. On success a **room code** is shown; tap to copy and send it to your friend.

Room code format:

```
U/XXXX-XXXX-XXXX-XXXX
```

- 34-character alphabet (the easily confused `I` / `O` are excluded);
- the number is normalized with `value -= value mod 7`, so "divisible by 7" is a valid checksum;
- copy-paste instead of retyping: `I`/`1` and `O`/`0` look alike.

## Guest: join a room

1. Multiplayer → **Join room**;
2. Paste the room code → confirm;
3. Once you are in the virtual LAN, a **server address** is shown; tap to copy;
4. In game: Multiplayer → Direct Connect → paste.

### Paste format is normalized automatically

These all work:

| You paste | What is used |
|---|---|
| `u/abcd-efgh-ijkl-mnop` | `U/ABCD-EFGH-IJKL-MNOP` |
| `U/ABCD EFGH IJKL MNOP` (with spaces) | spaces removed |
| `【U/ABCD-EFGH-IJKL-MNOP】` | brackets removed |
| full-width characters, `I`→`1`, `O`→`0` | corrected |
| `abcd...` (no `U/` prefix) | prefix added |

When the input was rewritten the dialog tells you “normalized to `U/XXXX-XXXX-XXXX-XXXX`”.
“Invalid room code” means the string itself is not a valid code (a character was probably lost while copying).

## Troubleshooting

| Symptom | Fix |
|---|---|
| Hosting fails: "open the world to LAN first" | Re-open the world to LAN in game, check the port, retry |
| Joining fails | Make sure the code is fresh and the host is still in the room; try switching Wi-Fi ↔ mobile data |
| Stuck scanning nodes | Retry; the node list is cached for 10 minutes — restarting the app forces a refetch |
| Connected to the mesh but the game won't connect | The host's game must not be paused; both sides need matching versions and mods |
| Want to use your own EasyTier node | Multiplayer → enable → Custom server node |

Logs: Multiplayer → **Share core log** (note it contains network addresses).

## Versus SakuraFrp

- Terracotta: **both players need ZyNova**, P2P, no ports, no account;
- SakuraFrp: **only the host needs an account**, the port is published publicly, so a vanilla client can join.

They are independent; pick per scenario.
