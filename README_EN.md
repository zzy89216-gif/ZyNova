<p align="center"><sub>
<a href="README.md">简体中文</a> · <b>English</b> · <a href="i18n/zh-Hant/README.md">繁體中文</a> · <a href="i18n/ja/README.md">日本語</a> · <a href="i18n/ko/README.md">한국어</a> · <a href="i18n/tr/README.md">Türkçe</a> · <a href="i18n/ru/README.md">Русский</a> · <a href="i18n/vi/README.md">Tiếng Việt</a> · <a href="i18n/th/README.md">ไทย</a> · <a href="i18n/de/README.md">Deutsch</a> · <a href="i18n/es/README.md">Español</a> · <a href="i18n/fr/README.md">Français</a><br>
<a href="i18n/uk/README.md">Українська</a> · <a href="i18n/pl/README.md">Polski</a> · <a href="i18n/pt-BR/README.md">Português (Brasil)</a> · <a href="i18n/ar/README.md">العربية</a> · <a href="i18n/fa/README.md">فارسی</a> · <a href="i18n/id/README.md">Bahasa Indonesia</a> · <a href="i18n/it/README.md">Italiano</a> · <a href="i18n/el/README.md">Ελληνικά</a> · <a href="i18n/ml/README.md">മലയാളം</a> · <a href="i18n/ur-Latn/README.md">Roman Urdu</a> · <a href="i18n/ro/README.md">Română</a> · <a href="i18n/bg/README.md">Български</a> · <a href="i18n/lzh/README.md">文言</a>
</sub></p>

<p align="center"><img src="assets/icon.png" width="120" alt="ZyNova"></p>

# ZyNova

**A Minecraft: Java Edition launcher for Android** · maintained from ZalithLauncher2 · GPL-3.0

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.4.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.4.0)
[![Docs](https://img.shields.io/badge/Docs-78%20articles-blue)](docs/en/README.md)

One rule: **fewer taps**. Decide for you first, ask only when it cannot; installing games, modpacks and mods, resolving dependencies and reading crash logs is delegated to the AI wherever possible.

## Quick start

1. Grab the APK from [Releases](https://github.com/zzy89216-gif/ZyNova/releases) (Android 8.0+).
2. First launch: region check → **allow "All files access"** (game data lives in the public `/ZyNova` folder) → scroll each of the 3 legal documents to the end and tick it → pick an age group.
3. Sign in (Microsoft / LittleSkin / offline) → pick a version, or let the AI install one → play.

> Upgrading from an older build runs a one-time data migration: full-screen progress, tap Next when it finishes. Data stays in `/ZyNova/.Minecraft` and is never lost.

## Verify the official APK

APKs from official Releases are signed with the same certificate across versions, so the **certificate SHA-256 fingerprint never changes**. To check that your copy has not been swapped, compare the fingerprint:

- With Android SDK build-tools (most reliable): `apksigner verify --print-certs ZyNova-<version>.apk` and read the `Signer #1 certificate SHA-256 digest` line;
- With only a JDK: `keytool -printcert -jarfile ZyNova-<version>.apk` and read the `SHA256:` line; if it says the file is not a signed jar, the APK is v2/v3-only — use `apksigner` instead;
- SHA-256 fingerprint of the official signing certificate (compare against this):

  `8A:DA:D0:D5:A3:6A:B1:87:19:A2:33:18:46:A2:C3:37:31:23:CA:E8:B7:7C:86:B6:9F:55:6C:7A:34:2D:09:1D`

If the fingerprint does not match, **do not install it** — download again from [Releases](https://github.com/zzy89216-gif/ZyNova/releases). Builds you compile yourself are signed with your own key, so their fingerprint differs by design.

> ⚠️ This certificate is the upstream public default key (available in the repository), so a matching fingerprint only shows the APK was not re-signed with a different key; it does not prove provenance. Always download from this repository's [Releases](https://github.com/zzy89216-gif/ZyNova/releases).

## What it does

| | |
|---|---|
| **AI agent** | DeepSeek by default: installs games / modpacks / mods, changes settings, reads logs, launches, controls music — 53 tools |
| **One-tap install** | Resources and modpacks install in one step from the list, required dependencies included |
| **Renderers** | Auto / MobileGlues / Ironized Zink / GL4ES / LTW (experimental, arm64 only) |
| **Favorites** | Online favorites + local archive (SHA-256 dedupe), one-tap download right on the card |
| **Music player** | Import local tracks, play them as a list, switch tracks in game — background playback with notification controls |
| **Home** | Your four most recent worlds plus the full world list; a custom home feed you can add to, hide and reorder, offline-safe |
| **Multiplayer** | Terracotta (P2P) / SakuraFrp, one screen, tutorials written against the real flow |
| **Data** | `/storage/emulated/0/ZyNova/.Minecraft` (instances / worlds / mods), logs in `/ZyNova/logs` |

Networks in China: mirrors are used on demand and fall back to the official source; CurseForge may need a mirror, Modrinth usually works directly. On errors, try another network or turn off proxies first.

## What's new in 28.4.0

**28.4.0 stable** reworks the home screen, removes the Personalization page and fixes a batch of issues on top of the 28.3.0 stable release. **The look of the UI stays the same as 28.3.0** — this build does not include dynamic glass, real refraction, magnetic snapping or the motion system.

- **Old home cards removed** (#100): the grouped-by-instance world cards in the middle of the home page are gone; worlds are reached through "Recent worlds + View all".
- **Home redesign** (#91): the top of the home screen now shows your four most recently played worlds, and **View all** opens the full world list (search, sorting, filters by instance and game version, launch right from the list); a new **Custom home** feed below the world section (Minecraft news / Java version updates / Modrinth / CurseForge / Minecraft community) can be added to, removed, hidden and reordered, and offline never blocks launching.
- **Personalization removed**: the top-bar palette entry and the Personalization page are gone; appearance settings (theme, wallpaper, corner radius, glass, background blur and more) still live under **Settings → Launcher**.
- **Deleting a version**: the progress screen is now truly full screen — it covers the whole display, the back key no longer dismisses it by accident, and you can cancel at any time.
- **Renderer wording fix**: the "Renderer advanced settings" title in Advanced settings no longer claims the wrong number of renderers (three → four).
- **Unstable optimisation reverted**: the list caching / async plugin loading from 28.3.2 caused crashes in several resource manager screens; all of it is reverted in this build. Performance work will be revisited separately.

Everything from earlier versions (the 28.3.0 roll-up of 28.2.2–28.2.4, the music player, device-aware renderer selection, 53 AI tools and the #65–#83 fixes) is included — see [CHANGELOG.md](CHANGELOG.md).

## Docs and feedback

User docs: [English](docs/en/README.md) · [中文](docs/zh/README.md) ｜ [Changelog](CHANGELOG.md) ｜ [Third-party notices](THIRD_PARTY_NOTICES.md) ｜ [Licenses](docs/en/licenses.md) ｜ Development and releases: [AGENTS.md](AGENTS.md) · [HANDOFF.md](HANDOFF.md) ｜ [Repository structure](REPO_MAP.md)

Feedback: [Issues](https://github.com/zzy89216-gif/ZyNova/issues) · [Discord](https://discord.gg/dbHTApYNYJ) · [X](https://x.com/ZyNovaProject) ｜ Mail: <ZyNovaLauncher@outlook.com> ｜ Sponsor: <https://afdian.com/a/hizzy>

## License

GPL-3.0. Component licenses and usage boundaries are listed in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
**Terracotta (multiplayer) is AGPL-3.0** and stricter: it ships unmodified; if you modify and redistribute it, AGPL-3.0 §13 requires publishing the corresponding source.

**ZyNova — Context First. Less Steps.**
