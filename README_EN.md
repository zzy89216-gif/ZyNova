<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.0.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.0.1)

[简体中文](README.md) | **English** | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md) | [한국어](README_KO.md) | [Русский](README_RU.md)

---

A Minecraft: Java Edition launcher for Android, built on ZalithLauncher2 source. Independently maintained; not an official build.

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ What's new in 28.0.1

- **SakuraFrp actually connects now**: `frpc -f <accessKey>:<tunnelId>` (the official form; the previous env-var approach is ignored by Sakura's frpc). Refresh / create-tunnel no longer dies on `Unexpected JSON token`, and the connection address is extracted from the frpc log, pinned to the top with a one-tap copy
- **Back button stops ejecting you**: the top-bar back no longer borrows the system back press (which had no callback and simply finished the screen — restart required). It now steps back exactly one level and never empties the stack
- **Leave the game without killing it**: the in-game menu's first entry returns to the launcher while the game keeps running; a "Back to game" pill appears at the bottom of the launcher to jump straight back
- **No port/type knowledge needed**: tunnel type is fixed to TCP, the local port is auto-detected from `/proc/net/tcp` (the LAN port Minecraft opens), and nodes are filtered by account tier
- **AI can bring up multiplayer end-to-end**: `frp_status` / `frp_create_tunnel` / `frp_start_tunnel` / `frp_stop_tunnel` / `multiplayer_status`

## 🛠️ Features

- **AI Agent**: DeepSeek by default — installs the game, modpacks and mods, edits configs, reads logs, launches the game
- **Renderers**: Auto / MobileGlues / GL4ES / LTW (experimental)
- **Resource core**: Modrinth / CurseForge search, dependency resolution, download, verification, install
- **Favorites & archive**: online favorites plus a private archive with SHA-256 dedupe
- **Preload API guard**: Fabric API / Quilted Fabric API / Legacy Fabric API are added by the install flow and can't be duplicated, searched, deleted or disabled manually

## 📦 Download and build

- Download: <https://github.com/zzy89216-gif/ZyNova/releases>
- Build: JDK 17 plus Android SDK / NDK, then `./gradlew ZalithLauncher:assembleRelease`

## 📚 Docs

> 📚 **Full documentation (72 articles, English)**：[docs/en/README.md](docs/en/README.md)



| Content | File |
|---|---|
| User docs hub | [docs/en/README.md](docs/en/README.md) |
| Install / usage / export | [docs/en/install.md](docs/en/install.md) · [docs/en/usage.md](docs/en/usage.md) |
| Renderers / login / favorites / AI | [docs/en/renderers.md](docs/en/renderers.md) · [docs/en/login.md](docs/en/login.md) · [docs/en/favorites.md](docs/en/favorites.md) · [docs/en/ai-agent.md](docs/en/ai-agent.md) |
| Troubleshooting / FAQ / data | [docs/en/troubleshooting.md](docs/en/troubleshooting.md) · [docs/en/faq.md](docs/en/faq.md) · [docs/en/data-and-privacy.md](docs/en/data-and-privacy.md) |
| Building | [docs/en/build.md](docs/en/build.md) |
| Changelog | [CHANGELOG.md](CHANGELOG.md) |
| Handoff / architecture / workflow | [HANDOFF.md](HANDOFF.md) |
| Third-party licenses | [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) |
| License | [LICENSE](LICENSE) |



## 💖 Support

- Issues: <https://github.com/zzy89216-gif/ZyNova/issues>
- Discord: <https://discord.gg/QwPpZQHrTa>
- Sponsor: <https://afdian.com/a/hizzy>

## ⚖️ License

GPL-3.0, must stay open source. Third-party components and the licence of each are listed in
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) and [docs/en/licenses.md](docs/en/licenses.md).

> ⚠️ **Terracotta is AGPL-3.0** (stricter than the rest): it is shipped unmodified. If you modify it and
> redistribute, AGPL-3.0 §13 requires you to publish the corresponding source.

---

**ZyNova — Context First. Less Steps.**
