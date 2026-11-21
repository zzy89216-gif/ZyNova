<p align="center"><sub>
<a href="README.md">简体中文</a> · <b>English</b> · <a href="i18n/zh-Hant/README.md">繁體中文</a> · <a href="i18n/ja/README.md">日本語</a> · <a href="i18n/ko/README.md">한국어</a> · <a href="i18n/tr/README.md">Türkçe</a> · <a href="i18n/ru/README.md">Русский</a> · <a href="i18n/vi/README.md">Tiếng Việt</a> · <a href="i18n/th/README.md">ไทย</a> · <a href="i18n/de/README.md">Deutsch</a> · <a href="i18n/es/README.md">Español</a> · <a href="i18n/fr/README.md">Français</a><br>
<a href="i18n/uk/README.md">Українська</a> · <a href="i18n/pl/README.md">Polski</a> · <a href="i18n/pt-BR/README.md">Português (Brasil)</a> · <a href="i18n/ar/README.md">العربية</a> · <a href="i18n/fa/README.md">فارسی</a> · <a href="i18n/id/README.md">Bahasa Indonesia</a> · <a href="i18n/it/README.md">Italiano</a> · <a href="i18n/el/README.md">Ελληνικά</a> · <a href="i18n/ml/README.md">മലയാളം</a> · <a href="i18n/ur-Latn/README.md">Roman Urdu</a> · <a href="i18n/ro/README.md">Română</a> · <a href="i18n/bg/README.md">Български</a> · <a href="i18n/lzh/README.md">文言</a>
</sub></p>

<p align="center"><img src="assets/icon.png" width="120" alt="ZyNova"></p>

# ZyNova

**A Minecraft: Java Edition launcher for Android** · maintained from ZalithLauncher2 · GPL-3.0

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.3.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.3.0)
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
| **Multiplayer** | Terracotta (P2P) / SakuraFrp, one screen, tutorials written against the real flow |
| **Data** | `/storage/emulated/0/ZyNova/.Minecraft` (instances / worlds / mods), logs in `/ZyNova/logs` |

Networks in China: mirrors are used on demand and fall back to the official source; CurseForge may need a mirror, Modrinth usually works directly. On errors, try another network or turn off proxies first.

## What's new in 28.3.0

**Stable release.** This version rolls up everything from the 28.2.2 / 28.2.3 / 28.2.4 pre-releases
(those pre-release Releases have been removed) and ships it on the stable line: the built-in music player,
device-aware renderer selection, 51 → 53 AI tools, and fixes for 19 issues.

### Fixes and improvements (#65–#83)

- **Download / install several things at once** (#78): game versions, modpacks and resources can run in parallel,
  each with its own progress / speed / cancel; tapping the same target again gives a clear notice. Forge / NeoForge
  JVM phases run one at a time, shared-file writes are locked, cross-entry duplicate names are reported, and exports
  use their own temp directory.
- **Card reordering on the home page is stable** (#79): no flicker, no crash, fast drags swap one card at a time,
  horizontal dragging supported.
- **Music progress no longer freezes** (#82): pausing / buffering no longer stops position and duration forever;
  the position stays visible while paused and unknown durations show `--:--`.
- **No stale "Return to game"** (#83): the entry disappears as soon as the game exits; tapping it validates first
  and never launches an empty `VMActivity` that crashes.
- **Deleting a version no longer looks stuck** (#74): full-screen determinate progress (files / size / percentage /
  current file), cancellable, and cancelling restores the original path when possible.
- **Download / install prompts are global** (#72): success, modpack naming and permission prompts appear on any screen;
  leaving the download page no longer cancels or suspends an install.
- **Interrupted installs recover** (#73): a recovery record is written so the next start cleans up or rolls back
  safely — no half-written jars and no permanent "installing" state.
- **"About & Support" → "About & Plugins"** (#71), **multiplayer page title fixed** (#68), **about-page buttons
  aligned** (#69 #70), **favorites top bar rework** (#76), **in-game notice card no longer full-width** (#66),
  **extra mailbox removed** (#67), **no duplicate "follow global" renderer entry** (#65).
- Task / cancel chain: "cancel all tasks" no longer breaks the whole background task system; cancelling is no longer
  recorded as a failure; the "update loader" button mis-detection is fixed.

### 🎵 Music player (new + improved)

- Import local music (MP3 / AAC(M4A) / FLAC / OGG / Opus / WAV), list playback, previous / next, seek, remember last track and position.
- Background playback: foreground service + notification controls; yields when another app takes audio focus, pauses on
  headphone unplug; the in-game panel can control playback across processes.
- **Lyrics**: same-name `.lrc` loaded automatically (UTF-8 / UTF-16 / GBK), with an in-page "Import lyrics"; current line
  highlighted and auto-scrolled; the notification shows "previous line + current line"; lyrics are stored per full audio
  file name (`Sunny.mp3.lrc`), so same-name different-extension tracks no longer overwrite each other.
- **Top-bar mini player**: on non-music pages with a current track, a compact bar below the top bar (title + play/pause +
  skip, tap to open the music page).
- **Denser UI**: tighter library rows, highlighted current track, auto-scroll on track change; position and duration always
  visible; landscape / short screens no longer overflow.

### 🤖 AI

- **AI web search**: the official DeepSeek search path is fixed; when unavailable it no longer fails silently — the result
  states the reason and falls back to DuckDuckGo; the AI can also switch search engines and configure a key.
- **Installs**: `install_game` can start several game installs at once and `install_status` lists all running installs plus
  the latest result.
- **Music tools**: new `music_status` / `music_control` (status, play / pause / skip / seek / play by title);
  total tools **51 → 53**.

### 🎨 Renderers

- **Ironized Zink is back in full** (4 official presets + 12 advanced switches, OpenGL version independently selectable
  3.3–4.6).
- **Auto selection is device-aware**: checks whether the device can run it (MobileGlues needs GLES 3.2+, Ironized Zink needs
  Vulkan), then picks the first version-compatible one in MobileGlues → GL4ES → Ironized Zink order; manual fallback uses
  the same logic.
- Renderer plugin docs completed (source, duplicate same-name entries are normal, identifier conflicts are hidden).

### 🧩 In-game entry

- A new **Multiplayer** shortcut in the in-game menu: collapses the menu, returns to the launcher and opens the Multiplayer
  page directly (Terracotta and Sakura both live there) — **the game keeps running**.

### ⬆️ Upstream sync

- Android 8.x clipboard crash from a game thread, and the auto-JRE-selection crash.

### 📄 Docs

- User docs 78+78 articles and 25-language intro pages (Classical Chinese added) are all synced to 28.3.0 stable.

## Docs and feedback

User docs: [English](docs/en/README.md) · [中文](docs/zh/README.md) ｜ [Changelog](CHANGELOG.md) ｜ [Third-party notices](THIRD_PARTY_NOTICES.md) ｜ [Licenses](docs/en/licenses.md) ｜ Development and releases: [AGENTS.md](AGENTS.md) · [HANDOFF.md](HANDOFF.md)

Feedback: [Issues](https://github.com/zzy89216-gif/ZyNova/issues) · [Discord](https://discord.gg/QwPpZQHrTa) · [X](https://x.com/ZyNovaProject) ｜ Mail: <ZyNovaLauncher@outlook.com> ｜ Sponsor: <https://afdian.com/a/hizzy>

## License

GPL-3.0. Component licenses and usage boundaries are listed in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
**Terracotta (multiplayer) is AGPL-3.0** and stricter: it ships unmodified; if you modify and redistribute it, AGPL-3.0 §13 requires publishing the corresponding source.

**ZyNova — Context First. Less Steps.**
