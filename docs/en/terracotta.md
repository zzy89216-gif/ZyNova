# Terracotta multiplayer (P2P)

No port forwarding, no server, and **both sides install ZyNova**. Entry: **Multiplayer** in the top bar
(pick "Terracotta" in the floating switcher at the bottom of the screen).

## Preparation (both host and guest)

1. Open the Multiplayer page, turn on **Enable multiplayer**, read the notice and confirm;
2. Allow notifications: status shows in the notification bar, and Android is far more likely to kill the
   process when notifications are blocked;
3. **Turn the switch on before launching the game** — it is read when the game starts, so changing it later does nothing;
4. The first time you tap host or guest it asks for **VPN permission** (EasyTier creates a virtual adapter).
   It must be allowed, otherwise nothing connects.

## Host

1. Enter a **singleplayer world**, press `Esc`, choose "Open to LAN", note the port (25565 by default,
   write it down if you change it), then click "Start LAN world". **Skip this and hosting stays stuck on scanning**;
2. Tap the floating menu ball in game, then **Menu**, then **Multiplayer menu**, then **I want to host**
   (that entry only exists while "Enable multiplayer" is on);
3. Status goes: scanning LAN world, then starting room, then room started;
4. On success you get an invite code `U/XXXX-XXXX-XXXX-XXXX`, **already copied to the clipboard**;
   if not, tap "Copy invite code";
5. Send it to your friend and have them pick "I want to join" in the multiplayer menu.

> Do not leave the world while playing: leaving it closes the room. To close it on purpose press "Exit";
> the invite code dies with the room.

## Guest

1. Launch the game and stay on the main menu; no world needed;
2. Menu ball, then **Menu**, then **Multiplayer menu**, then **I want to join**;
3. Paste the invite code and confirm: spaces, dashes and the `U/` prefix are cleaned up, and the field tells
   you which protocol the code belongs to (Terracotta / PCL CE / Scaffolding);
4. Wait for "joining room"; a success estimate based on both networks is shown and is **only a hint**;
5. Only "joined room" means success. Then go back to the game main menu, open **Multiplayer**, refresh and
   double-click the **Terracotta lobby** entry;
6. If the list stays empty, use **Copy alternate address** in the multiplayer menu and paste it into
   Multiplayer, then **Direct Connection**.

**Game version, loader and mods must match the host**, otherwise you connect but cannot enter the world.
Offline accounts work; the alternate-address route does not support offline accounts, so prefer the lobby list.

## Invite codes

Shaped `U/XXXX-XXXX-XXXX-XXXX` (base 34, without the confusing `I` / `O`). The field normalises full-width
characters, spaces, dashes, `【】`, a missing `U/` prefix, `I` to `1` and `O` to `0`, then tells you the
normalised code. "Invalid invite code" means the characters themselves are not valid.

## Troubleshooting

| Symptom | Fix |
|---|---|
| Hosting stuck on scanning | Make sure the world is really opened to LAN, then tap host again |
| Hosting fails immediately | Same as above; also check the VPN permission was granted |
| Joining fails / invalid code | Check the code, make sure the room is still open (closing the room kills the code), retry on another network |
| Joined, but no lobby in the list | Use **Copy alternate address** with Direct Connection; verify versions and mods match |
| "Room closed" | The host left the world or closed the room; ask for a new code |
| Connects then drops / laggy | Both sides on a normal home network, no double VPN; the host should not pause the game |
| Want your own nodes | Multiplayer page, **Custom EasyTier server nodes** (empty uses the default) |

Logs: **View log** in the multiplayer menu; to report a problem, use **Share multiplayer core log** on the
Multiplayer page (it contains network addresses).

## Interoperability

Works with other Scaffolding clients such as FCL, HMCL and PCL CE; your friends do not have to use this launcher.

Difference from [SakuraFrp](https://github.com/zzy89216-gif/ZyNova/blob/main/docs/en/frp.md): Terracotta needs
ZyNova on both sides and connects peer-to-peer; SakuraFrp only needs a host account, and guests can use a
vanilla client.
