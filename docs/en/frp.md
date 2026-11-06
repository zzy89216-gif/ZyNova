# SakuraFrp (playing over the internet)

ZyNova offers two ways to play with friends. Pick one — they do not interfere with each other:

| Option | How it works | Requirements | Best for |
|---|---|---|---|
| [Terracotta](https://github.com/zzy89216-gif/ZyNova/blob/main/docs/en/terracotta.md) | P2P mesh (EasyTier); both sides join the same virtual LAN | Both players use ZyNova | Friends, no port forwarding |
| SakuraFrp | Maps a local port to a public node (frp) | A SakuraFrp account + access key | The other side uses a PC client, or you don't want Terracotta |

Entry: **Multiplayer → SakuraFrp**.

## Prerequisites

1. Sign up at [SakuraFrp](https://www.natfrp.com/);
2. Copy your access key from the panel (User info → View access key);
3. Have the game in a world with "Open to LAN" already enabled (you no longer need to remember the port).

> Quotas, real-name requirements and node restrictions follow your SakuraFrp account tier.
> ZyNova is only a client and never manages your account.

## First run

1. Open **SakuraFrp**, paste the access key and sign in.
   After signing in the header shows your **user group (Free / Silver / Gold …), tunnel quota and today's traffic**;
2. In-game choose "Open to LAN", then tap **New tunnel** in the launcher:
   - The type is **fixed to TCP** — nothing to choose;
   - **The local port is auto-detected**: the launcher reads the ports your own app is listening on,
     which is exactly the LAN port the game just opened;
   - The node list only contains nodes **your tier can use** (a free account never sees Silver/Gold-only nodes);
   - Leave the remote port empty and let SakuraFrp assign one;
3. Save, then tap **Start** on that tunnel;
4. Once it is running, a `node:remote-port` line appears **at the top of the page** with a **Copy** button —
   send that to your friend, who can use "Direct connect" in Minecraft.

## About the port (since 28.0.3)

The **local port is always an editable field, defaulting to 25565**:

- Clear it and type the port shown in the game chat;
- Once you edit it manually, *Detect again* will not overwrite your value;
- *Detect again* reads the port from the **game log** (see below).

Why the game log: since Android 10 normal apps may not read `/proc/net/tcp`
(real devices report `EACCES: Permission denied`). But when a world is opened to LAN, the game always
writes a line — and **that line is localized**:

| Client language | Log line |
|---|---|
| English | `Local game hosted on port 25565` |
| Chinese (simplified) | `本地游戏已在端口[25565]上开启` |

The launcher tries English → Chinese → a generic "digits after `port` / `端口`" fallback, and only reads the
**last 128 KB** of `logs/latest.log` under each game directory.

> Changing worlds or ports needs no tunnel edit — just tap *Detect again*.
> Starting with no listener on the port fails early with a clear message.

## What you can edit (since 28.0.2)

- Editable: **local port** and **note**;
- **Renaming is not possible**: the official `/tunnel/edit` endpoint accepts no `name` (and no `remote`), so
  previous builds silently dropped it and looked like "the rename did nothing". To rename, delete and recreate;
- If deletion succeeds but the server could not kick the tunnel offline, the UI now tells you to check the panel
  instead of pretending it worked.

## ⚠️ Public servers must use a "multi-line" node

SakuraFrp's hard rule (spelled out in its official OpenAPI):

- If the server is advertised anywhere public — **Douyin / Bilibili / Xiaohongshu / forums / large QQ groups** —
  it counts as a *public game server* and **must** use a "multi-line" node (node `flag & 2048`, name contains "多线");
- Using a regular node gets the **account banned with no refund**;
- In this launcher, such nodes are marked **"多线"** in the dropdown, and any warning returned by the API on
  creation is shown in a dialog.

## Background & screen off

- Starting a tunnel runs a **foreground service + notification**, which is how Android allows long-lived tunnels;
- On first use, tap **Allow background running** to whitelist ZyNova from battery optimisation,
  otherwise the tunnel may be killed when the screen turns off;
- Some OEM ROMs (vivo / iQOO / Xiaomi / OPPO …) additionally require allowing "background high power usage / unrestricted".

## Refreshed on every visit

Tunnels on the SakuraFrp side change over time (quota, node availability, bans), so **the page re-fetches
your account and tunnel list every time you open it**. Pull-to-refresh / the refresh button forces a reload,
so what you see is never stale cache.

## Limits

- Free accounts have tunnel-count, traffic and bandwidth limits — see the SakuraFrp panel;
- **Public game servers must use the dedicated "multi-line" nodes**; private friend sessions are not affected;
- A single tunnel **cannot be used by two clients at the same time** (they kick each other);
- SakuraFrp only forwards a port; the host still has to keep the game running.

## Security

- The access key is encrypted with **Android Keystore** and stored in the app's private directory;
- The key is only used as the `frpc` launch argument in the official form `frpc -f <key>:<tunnelId>`;
  it is never written to logs and never uploaded anywhere;
- ZyNova bundles no accounts or shared keys — every tunnel belongs to your own account.

## Troubleshooting

| Symptom | Fix |
|---|---|
| "No process is listening on this port" | Enable "Open to LAN" in-game, then tap **Detect again** or type the port |
| "frpc component not found" | Reinstall the APK (`libfrpc.so` ships with it) |
| Tunnel online but friends cannot join | Check the address/port and that the local port matches the game |
| Refresh fails with a format error | Fixed in 28.0.1 (arrays / `{data}` / BOM are tolerated); if it still happens, send the `FrpApi` log lines |
| Disconnects when the screen turns off | Whitelist battery optimisation and allow background running |

> Creating a tunnel validates the name / local port / node and highlights problems inline
> (previously tapping Save did nothing at all).
