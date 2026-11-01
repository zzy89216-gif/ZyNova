<p align="center">
  <img src="assets/icon.png" width="180" alt="ZyNova">
</p>

# ZyNova

> **Minecraft: Java Edition · Android Launcher**
>
> An independent, unofficial project based on the open-source code of ZalithLauncher2

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Primary-Kotlin-blue)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-GPL--3.0-orange)](https://www.gnu.org/licenses/gpl-3.0.html)
[![Release](https://img.shields.io/badge/Release-27.7.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.7.1)

[简体中文](README.md) | **[English](README_EN.md)** | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md) | [한국어](README_KO.md) | [Русский](README_RU.md)

---

## ✦ About

**ZyNova** is a **Minecraft: Java Edition** launcher for Android, built on the open-source code of
[ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2). It is **not** an official
ZalithLauncher2 release; it is an independently maintained, unofficial fork.

- GitHub: <https://github.com/zzy89216-gif/ZyNova>
- Issues: <https://github.com/zzy89216-gif/ZyNova/issues>
- Discord: <https://discord.gg/QwPpZQHrTa>

## ✨ 27.7.1 Highlights

- **Settings reorganized into seven categories**: Basics / Game / Renderer / Network & Downloads / AI / Advanced / About & Support
- **Network & Downloads**: adjust connections, connect/read timeouts and retries; override each download service separately (invalid values fall back to the official one); inspect and clear the download cache
- **Renderer settings consolidated**: each renderer shows only its own options; every configurable parameter is available under Advanced settings
- **AI settings moved into Settings**: the separate entry in the AI chat is gone, first-time setup guidance stays, existing API key / model / permission mode are untouched
- **Favorites & local archive**: copy resources into app-private storage with SHA-256 dedupe; install into an instance with automatic required dependencies, verify integrity, export and delete independently
- **Video background startup**: the player is no longer created during first composition; the video fades in after the first frame, avoiding white flashes
- **Downloads and installs run in the background**: leaving the download page no longer cancels an install
- **World creation**: Hardcore forces cheats and keep-inventory off
- **NeoForge downloads**: progress is byte-based, stalled batches fail with a reason, retries keep finished files

> Full history: [CHANGELOG.md](CHANGELOG.md).

## 🛠️ Features

- **Settings in seven categories**: Basics / Game / Renderer / Network & Downloads / AI / Advanced /
  About & Support; renderer parameters and per-service download sources are all editable there
- **Favorites & local archive**: copy resources into app-private storage with SHA-256 dedupe; install
  into an instance with automatic required dependencies, verify integrity, export or delete independently
- **AI Agent**: DeepSeek by default; it can read logs, manage mods, edit configs, install resources
  and launch the game — not just give advice.
- **Renderers**: Auto / MobileGlues / Ironized Zink / GL4ES
- **Card home**: worlds and servers per instance, with one-step create/add and quickPlay
- **Unified resource core**: Modrinth / CurseForge search, dependency resolution, download, SHA1
  verification and installation in one flow
- **Fabric API guard**: installed automatically by the Fabric flow; no manual install, delete,
  disable or duplicate copy

## 📦 Download & Build

- **Download**: <https://github.com/zzy89216-gif/ZyNova/releases>
  - Universal APK (arm64-v8a / armeabi-v7a / x86 / x86_64)
  - `mapping.universal.zip` (deobfuscation mapping)
- **Local build**: JDK 17 + Android SDK / NDK, then:
  ```bash
  ./gradlew ZalithLauncher:assembleRelease
  ```
- Official builds run in GitHub Actions with code obfuscation and resource shrinking.

## ⚠️ Notes

- Home-screen world creation uses the new save format on **26.1+**, the compatibility path on
  **1.12.2–26.0**, and is hidden below **1.12.2**.
- Fabric API is handled automatically; manual installation is refused, and it cannot be deleted or
  disabled in the mod manager.
- If the target version does not support quickPlay, the game still starts normally to the main menu.
- Existing configs, worlds and server lists are preserved across upgrades.

## 📚 Documentation

| Content | File |
|---|---|
| Changelog | [CHANGELOG.md](CHANGELOG.md) |
| Handoff / architecture / workflow | [HANDOFF.md](HANDOFF.md) |
| Third-party licenses | [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) |
| License | [LICENSE](LICENSE) |

## ⚖️ License

ZyNova is released under **GPL-3.0** and must remain open source. Upstream ZalithLauncher2 notices
are preserved. See [LICENSE](LICENSE) for the full text and additional terms, and
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for third-party components.

## 💖 Support

- Issues: <https://github.com/zzy89216-gif/ZyNova/issues>
- Discord: <https://discord.gg/QwPpZQHrTa>
- Donations: see [assets/donate](assets/donate) and the About page

## 👥 Maintainer

ZyNova is developed and maintained independently by **zzy**.

---

**ZyNova — Context First. Less Steps.**
