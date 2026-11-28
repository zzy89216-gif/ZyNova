# Custom home page

The card-based home screen has a news area at the very bottom (below the world section) called
**Custom home**: preset cards show Minecraft news, version updates and community activity.
It is **on by default**, so there is content without any setup.

Only the **card home** shows this area; switch the home type under
Settings → Launcher → Home → Home type (Blank / Card Home / Load from Local / Load from URL).
The feed is decorative and never affects launching or downloading.

## Preset cards

| Card | Content |
|---|---|
| Minecraft news | News and version announcements from Mojang |
| Java version updates | Release notes for the Java edition |
| Modrinth trending | Popular / new content on Modrinth |
| CurseForge picks | Curated content from CurseForge |
| Minecraft community | Official and community events |

Each card shows up to three entries; tapping one opens the source page in your browser.

## Switch and management

- **Master switch**: Settings → Launcher → **Custom home feed**. When off, the home screen does not load
  the feed at all (no network calls, no cache reads).
- **Manage**: **Manage custom home cards** in the same place opens the management dialog:
  - long-press the handle on the left to drag a card into a new order;
  - the switch on the right of each card **hides** it — it can be shown again later;
  - **Remove** moves a card down to "Available cards", from where it can be added back at any time;
  - **Reset to default** restores the initial order with everything visible.

## Offline and failed loads

- The feed is decorative: no network, a timeout or a changed source format only affects that one card;
- When a cache exists it is shown first (the same source is not re-requested over and over); with no cache a
  single line says "Nothing to show right now. It will refresh automatically later.";
- Instance cards, launching and downloads are never affected — **the game still launches with no network at all**.

## Privacy

The feed comes from public endpoints. It needs no sign-in and never uses your Minecraft or third-party account data.

See also: [Daily usage](https://github.com/zzy89216-gif/ZyNova/blob/main/docs/en/usage.md) ·
[Installing or importing worlds](https://github.com/zzy89216-gif/ZyNova/blob/main/docs/en/install-world.md)
