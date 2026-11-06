# SakuraFrp tunnel (multiplayer)

ZyNova offers two ways to play together. They are independent — pick whichever fits:

| Mode | How it works | Requires | Good for |
|---|---|---|---|
| [Terracotta](https://github.com/zzy89216-gif/ZyNova/blob/main/docs/en/terracotta.md) | P2P overlay network (EasyTier); both sides join the same virtual LAN | Both players use ZyNova | Friends who do not want to expose a port |
| SakuraFrp tunnel | Maps a local port to a public node (frp) | A SakuraFrp account + access token | Friends on the desktop client, or anyone not using Terracotta |

Entry: **Multiplayer → SakuraFrp tunnel**.

## Before you start

1. Register at [SakuraFrp](https://www.natfrp.com/);
2. Copy the access token from the dashboard (Profile → View access token);
3. Open your world to LAN in Minecraft and note the port shown in chat.

> Free-tier quotas, real-name requirements and node restrictions follow the SakuraFrp
> dashboard. ZyNova only acts as a client and never manages your account.

## First run

1. Open **SakuraFrp tunnel**, paste the access token and sign in;
2. Tap **New tunnel**:
   - type **TCP**;
   - pick a node marked as available;
   - set **local port** to the LAN port shown in game (not necessarily 25565);
   - leave the remote port empty so SakuraFrp assigns one;
3. Save, then tap **Start** on the tunnel;
4. Tap **Copy address** and send `node:remote-port` to your friend — they join via
   **Direct Connect** in Minecraft.

## About ports

- The LAN port may change every time you open a world to LAN; update the tunnel's local port
  after switching worlds;
- **Check** next to the port field tells you whether anything is listening;
- Starting with a port nobody listens on is refused, so you do not end up with a tunnel
  that connects but cannot be joined.

## Background and screen-off

- Starting a tunnel runs a **foreground service with a persistent notification**, the only
  reliable way to survive backgrounding on Android;
- Tap **Allow background** once to whitelist the launcher from battery optimization,
  otherwise the tunnel may be killed when the screen turns off;
- Some OEM ROMs (vivo / iQOO / Xiaomi / OPPO …) also need "unrestricted" background
  permission in system settings.

## Limits

- Free accounts have tunnel-count, traffic and bandwidth limits — see the SakuraFrp dashboard;
- **Public game servers may only use dedicated "multi-line" nodes**; private friend groups are fine;
- One tunnel cannot be used by two clients at the same time (they will kick each other);
- This is plain port mapping: the host must keep the game running.

## Security

- The access token is encrypted with **Android Keystore** and stored in the app-private
  directory; other apps cannot read the plaintext;
- The token is passed to the `frpc` child process through an environment variable only —
  never in command-line arguments or logs;
- ZyNova ships no account or shared key; every tunnel belongs to your own SakuraFrp account.

## Troubleshooting

| Symptom | Fix |
|---|---|
| "Port is not listening" | Open the world to LAN first, then use that port |
| "frpc component not found" | Reinstall the APK (`libfrpc.so` is bundled) |
| Tunnel online but friends cannot join | Check the address/port and that the local port matches the game |
| Disconnects when the screen turns off | Whitelist battery optimization and allow background running |
