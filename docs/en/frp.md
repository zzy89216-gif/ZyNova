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

## About the port (auto-detected since 28.0.1)

- The launcher detects the game port by reading the LISTEN sockets of its own uid from
  `/proc/net/tcp` and `/proc/net/tcp6`, so **changing worlds or ports no longer requires editing the tunnel** —
  just tap **Detect again**;
- If detection fails (game not started, or `/proc` reads restricted by the ROM),
  fill in the port shown in the game chat manually;
- Starting with no listener on that port fails early with a clear message, so you never end up with
  "the tunnel is online but nobody can join".

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
