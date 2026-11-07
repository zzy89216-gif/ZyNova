<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **ZyNova 28.1.0 released (global compliance build).**


[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.1.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.1.0)

[简体中文](README.md) | **English** | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md) | [한국어](README_KO.md) | [Русский](README_RU.md)

---

A Minecraft: Java Edition launcher for Android, built on ZalithLauncher2 source. Independently maintained; not an official build.

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ What's new in 28.1.0

- **First-launch compliance flow**: 3 merged documents — ① EULA + Privacy Policy, ② third-party open-source notice + disclaimers and risks, ③ tutorial + supported regions and legal jurisdiction. Each one **must be scrolled to the bottom and ticked separately** before you can continue, then pick your age group (existing users see it once after updating too)
- **Under-18 time limit**: minors may only launch the game on Friday, Saturday, Sunday and statutory holidays, **20:00–21:00**; the 2026 holidays follow the State Council schedule (国办发明电〔2025〕7号), and make-up working days are treated as workdays
- **Disclaimers are explicit**: save loss, device heat, third-party bans, service shutdown and similar risks are all stated
- Also includes: card home no longer hangs on loading, AI web search works, imports go to the instance directory, crash auto-fix removed

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
| Onboarding entry (state / release flow / traps / doc rules) | [AGENTS.md](AGENTS.md) |
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
