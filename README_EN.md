<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.0.4-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.0.4)

[简体中文](README.md) | **English** | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md) | [한국어](README_KO.md) | [Русский](README_RU.md)

---

A Minecraft: Java Edition launcher for Android, built on ZalithLauncher2 source. Independently maintained; not an official build.

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ What's new in 28.0.4

- **Text stays readable on bright wallpapers**: the app measures the wallpaper's average luminance and applies a 3%–28% scrim (dark scrim for light text, light scrim for dark text), updating live when you change the wallpaper
- **Redesigned the AI "Agent permission" section**: two selectable cards with icon, plain-language description and a radio dot; "Full control" uses the error colour plus a warning icon so the risk is obvious
- Issues #44 (crash self-heal) and #45 (web search / global files) are scheduled next

- **AI can browse the web now**: search errors, query MC百科, read page text (internal addresses blocked); toggle it in AI settings
- **One-tap crash fix**: after an abnormal game exit the launcher offers "Auto-fix", which hands the problem to the AI

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
