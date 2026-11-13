<p align="center"><sub>
<a href="README.md">简体中文</a> · <b>English</b> · <a href="i18n/zh-Hant/README.md">繁體中文</a> · <a href="i18n/ja/README.md">日本語</a> · <a href="i18n/ko/README.md">한국어</a> · <a href="i18n/tr/README.md">Türkçe</a> · <a href="i18n/ru/README.md">Русский</a> · <a href="i18n/vi/README.md">Tiếng Việt</a> · <a href="i18n/th/README.md">ไทย</a> · <a href="i18n/de/README.md">Deutsch</a> · <a href="i18n/es/README.md">Español</a> · <a href="i18n/fr/README.md">Français</a><br>
<a href="i18n/uk/README.md">Українська</a> · <a href="i18n/pl/README.md">Polski</a> · <a href="i18n/pt-BR/README.md">Português (Brasil)</a> · <a href="i18n/ar/README.md">العربية</a> · <a href="i18n/fa/README.md">فارسی</a> · <a href="i18n/id/README.md">Bahasa Indonesia</a> · <a href="i18n/it/README.md">Italiano</a> · <a href="i18n/el/README.md">Ελληνικά</a> · <a href="i18n/ml/README.md">മലയാളം</a> · <a href="i18n/ur-Latn/README.md">Roman Urdu</a> · <a href="i18n/ro/README.md">Română</a> · <a href="i18n/bg/README.md">Български</a>
</sub></p>

<p align="center"><img src="assets/icon.png" width="120" alt="ZyNova"></p>

# ZyNova

**A Minecraft: Java Edition launcher for Android** · maintained from ZalithLauncher2 · GPL-3.0

[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.2.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.2.0)
[![Docs](https://img.shields.io/badge/Docs-77%20articles-blue)](docs/en/README.md)

One rule: **fewer taps**. Decide for you first, ask only when it cannot; installing games, modpacks and mods, resolving dependencies and reading crash logs is delegated to the AI wherever possible.

## Quick start

1. Grab the APK from [Releases](https://github.com/zzy89216-gif/ZyNova/releases) (Android 8.0+).
2. First launch: region check → **allow "All files access"** (game data lives in the public `/ZyNova` folder) → scroll each of the 3 legal documents to the end and tick it → pick an age group.
3. Sign in (Microsoft / LittleSkin / offline) → pick a version, or let the AI install one → play.

> Upgrading from an older build runs a one-time data migration: full-screen progress, tap Next when it finishes. Data stays in `/ZyNova/.Minecraft` and is never lost.

## What it does

| | |
|---|---|
| **AI agent** | DeepSeek by default: installs games / modpacks / mods, changes settings, reads logs, launches — 51 tools |
| **One-tap install** | Resources and modpacks install in one step from the list, required dependencies included |
| **Renderers** | Auto / MobileGlues / GL4ES / LTW (experimental, arm64 only) |
| **Favorites** | Online favorites + local archive (SHA-256 dedupe), one-tap download right on the card |
| **Multiplayer** | Terracotta (P2P) / SakuraFrp, one screen, tutorials written against the real flow |
| **Data** | `/storage/emulated/0/ZyNova/.Minecraft` (instances / worlds / mods), logs in `/ZyNova/logs` |

Networks in China: mirrors are used on demand and fall back to the official source; CurseForge may need a mirror, Modrinth usually works directly. On errors, try another network or turn off proxies first.

## What's new in 28.2.0

- **Simpler favorites**: no description, no download counts; the download button is big, the unfavorite button small, one tap installs with required dependencies.
- **One-tap modpacks**: a download button next to the ♥ on search results, and one per version row.
- **Smoother corners**: card radius is 1.2× the configured value, so the curve eases in instead of turning abruptly.
- Includes the 28.1.x fixes: revoking storage permission no longer makes data look gone (P0), account page crash, silent background modpack installs, no SakuraFrp refresh errors, per-instance home card hiding, flicker-free dragging.

## Docs and feedback

User docs: [English](docs/en/README.md) · [中文](docs/zh/README.md) ｜ [Changelog](CHANGELOG.md) ｜ [Third-party notices](THIRD_PARTY_NOTICES.md) ｜ [Licenses](docs/en/licenses.md) ｜ Development and releases: [AGENTS.md](AGENTS.md) · [HANDOFF.md](HANDOFF.md)

Feedback: [Issues](https://github.com/zzy89216-gif/ZyNova/issues) · [Discord](https://discord.gg/QwPpZQHrTa) · [X](https://x.com/ZyNovaProject) ｜ Mail: <ZyNovaLauncher@163.com> · <ZyNovaLauncher@outlook.com> ｜ Sponsor: <https://afdian.com/a/hizzy>

## License

GPL-3.0. Component licenses and usage boundaries are listed in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).
**Terracotta (multiplayer) is AGPL-3.0** and stricter: it ships unmodified; if you modify and redistribute it, AGPL-3.0 §13 requires publishing the corresponding source.

**ZyNova — Context First. Less Steps.**
