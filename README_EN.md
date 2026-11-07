<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.0.7-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.0.7)

[简体中文](README.md) | **English** | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md) | [한국어](README_KO.md) | [Русский](README_RU.md)

---

A Minecraft: Java Edition launcher for Android, built on ZalithLauncher2 source. Independently maintained; not an official build.

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ What's new in 28.0.7

- **Removed the crash auto-fix feature** entirely — code, dialog, setting and strings
- **Web access needs no configuration now**: the engine picker / API key / "Create API" are gone; it always uses DuckDuckGo (no key), with a single on/off switch
- **Search fixed**: `web_search` (three-layer DuckDuckGo parsing) and `search_mcmod` (link formats + rate-limit detection) return results again
- **Import fixed**: `import_from_public` now resolves against the current instance's game directory, so files are actually visible in game
- **Modpacks**: the installed game version is echoed, with a clear warning when it differs from the current instance
- **Default home page is now the card layout**, and cards no longer fail to load (IO-thread loading + refresh on resume)

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
