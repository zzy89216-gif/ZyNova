<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <b>Roman Urdu</b> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> 📌 **Current release: 28.2.1** · [Changelog](../../CHANGELOG.md) · [Releases](https://github.com/zzy89216-gif/ZyNova/releases/latest)

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.2.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.2.1)

---

Android ke liye **Minecraft: Java Edition** launcher. ZalithLauncher2 ke code se mustaqil tor par banaya aur release kiya jata hai; ye official build nahi hai.

Soch sirf ek hai: **kam taps**. Bees settings samne rakhne ke bajaye launcher pehle khud faisla karta hai, aur sirf tab poochta hai jab waqai faisla na ho sake.

## ✨ Haal ki tabdeeliyan

Tam tabdeeliyan: [CHANGELOG.md](../../CHANGELOG.md).

## 🛠️ Kya kar sakta hai

- **AI Agent**: default DeepSeek — game, modpacks aur mods install karta hai, settings badalta hai, logs parhta hai, game launch karta hai
- **Renderers**: Auto / MobileGlues / GL4ES / LTW (experimental)
- **Resource core**: Modrinth / CurseForge search, dependency resolution, download, verify, install
- **Favourites aur archive**: online favourites + private archive, SHA-256 dedupe
- **Fabric API guard**: sirf install flow se add hoti hain — duplicate, search ya delete nahi ho sakti

## 🚀 3 qadam

1. [Releases](https://github.com/zzy89216-gif/ZyNova/releases) se APK le kar install karein (Android 8.0+)
2. Pehli baar: teen documents aakhir tak parhein, har ek tick karein, age group chunein
3. Microsoft account se login karein, version chunein ya install karein, game chalayein

## 📖 Istilah

- **Modpack**: mods ka ek bundle jo ek hi baar mein install hota hai
- **Shader**: behtar roshni aur paani, graphics settings mein on hota hai
- **Instance**: alag folder jismein version, mods aur settings hoti hain

## 📦 Download aur build

- Download: <https://github.com/zzy89216-gif/ZyNova/releases> (hamesha latest)
- Khud build: JDK 17 + Android SDK / NDK, phir `./gradlew ZalithLauncher:assembleRelease`
- Note: pehli baar compliance flow aata hai; tafseel [docs/en/licenses.md](../../docs/en/licenses.md) mein

## 📚 Documentation

> 📚 Documentation filhaal Chinese aur English mein hai (har ek 77 articles); ye page Roman Urdu taaruf hai.

| Content | File |
|---|---|
| Documentation index | [docs/en/README.md](../../docs/en/README.md) |
| Install / usage / export | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Rendering / login / AI | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Troubleshooting / FAQ / privacy | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Build / changelog | [docs/en/build.md](../../docs/en/build.md) · [CHANGELOG.md](../../CHANGELOG.md) |
| Handoff / third-party licences / licence | [HANDOFF.md](../../HANDOFF.md) · [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) · [LICENSE](../../LICENSE) |

## 💖 Support

- Masla report karein：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- Email：<ZyNovaLauncher@outlook.com>
- Official X：<https://x.com/ZyNovaProject>
- Project ko support karein：<https://afdian.com/a/hizzy>

## ⚖️ Licence

GPL-3.0 — project open source rehta hai. Third-party components aur unke licences
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) aur [docs/en/licenses.md](../../docs/en/licenses.md) mein hain.

> ⚠️ **Terracotta (multiplayer) AGPL-3.0 par hai**, baaki sab se sakht: bila tabdeeli distribute hota hai.
> Agar ise modify karke dobara distribute karein, to AGPL-3.0 §13 ke tehat mutabiq source code publish
> karna hoga.

---

**ZyNova — Context First. Less Steps.**
