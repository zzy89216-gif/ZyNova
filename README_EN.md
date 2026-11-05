<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-27.10.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.10.1)

[简体中文](README.md) | **English** | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md) | [한국어](README_KO.md) | [Русский](README_RU.md)

---

A Minecraft: Java Edition launcher for Android, built on ZalithLauncher2 source. Independently maintained; not an official build.

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ What's new in 27.10.1

- **SakuraFrp tunnels**: paste an access token on the Multiplayer screen and go — create, edit, delete, start/stop tunnels and copy the address. The token is encrypted with Android Keystore and a foreground service keeps the tunnel alive.
- **New top bar**: a centered liquid-glass capsule (Home / AI / Multiplayer / Downloads / Files / Personalization / Settings); the title stays on the left.
- **AI chat now renders Markdown**: headings, lists, tables, code blocks and links. Assistant messages are full-width, user messages adapt up to 82%.
- **Terracotta is unchanged** in this build.
- **Pre-release**: this build exists to verify the features above.

## 🛠️ Features

- **AI Agent**: DeepSeek by default — installs the game, modpacks and mods, edits configs, reads logs, launches the game
- **Renderers**: Auto / MobileGlues / GL4ES / LTW (experimental)
- **Resource core**: Modrinth / CurseForge search, dependency resolution, download, verification, install
- **Favorites & archive**: online favorites plus a private archive with SHA-256 dedupe
- **Fabric API guard**: only added by the Fabric flow; no manual duplicate install

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

GPL-3.0, must stay open source. Third-party notices: [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

---

**ZyNova — Context First. Less Steps.**
