<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **ZyNova 28.1.0 jari ho gaya (global compliance build).**


[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.1.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.1.0)

<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <b>Roman Urdu</b> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

---

Android ke liye **Minecraft: Java Edition** launcher, jo ZalithLauncher2 ke source code par mustaqil tor par maintain aur release hota hai. Ye official build nahi hai.

Maqsad: **kam taps**. Jo kuch automate ho sakta hai — game install karna, mods aur modpacks jama karna, settings badalna, logs parhna — wo AI agent sambhalta hai.

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Bugs aur tajawizat：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- X (official main account)：<https://x.com/ZyNovaProject>
- China ecosystem mailbox：<ZyNovaLauncher@163.com>
- Global mailbox：<ZyNovaLauncher@outlook.com>

## ✨ 28.1.0 mein naya

- **Pehli baar chalane par compliance flow**：teen documents — ① EULA + privacy policy, ② third-party open source notice + disclaimers aur risks, ③ tutorial + applicable regions aur legal jurisdiction. Har document ko **aakhir tak scroll karke alag se tick** karna hoga, phir age group chuniye (purane users bhi update ke baad ek baar ye dekhenge).
- **Na-baalighon ke liye waqt ki hadd**：18 se kam umar wale sirf Juma, Hafta, Itwaar aur sarkari chhuttiyon mein **20:00–21:00** game chala sakte hain. 2026 ki chhutiyan Chinese State Council calendar (国办发明电〔2025〕7号) ke mutabiq din-ba-din di gayi hain; make-up working days ko working day mana jayega.
- **Disclaimers poori tarah likhe gaye**：save ka nuqsan, device garam hona, third-party ban, service band hona aur aise khatrat wazeh tor par bataye gaye hain.
- **Iske ilawa**：home cards loading par atakte nahi, AI web search chalta hai, import instance folder mein jata hai, crash ke baad auto-fix hata diya gaya hai.

> 🌍 **28.1.0 mein available regions**：China, USA, South Korea, Japan aur Russia. Baaki regions mein pehli baar chalane par notice page dikhta hai aur app safely band ho jati hai.

## 🛠️ Kya kar sakta hai

- **AI Agent**：default DeepSeek — game, modpacks aur mods install karta hai, settings badalta hai, logs parhta hai, game launch karta hai
- **Renderers**：Auto / MobileGlues / GL4ES / LTW (experimental)
- **Resource core**：Modrinth / CurseForge search, dependency resolution, download, verify, install
- **Favourites aur archive**：online favourites + private archive, SHA-256 dedupe
- **Fabric API guard**：Fabric API / Quilted Fabric API / Legacy Fabric API install flow se add hoti hain — duplicate, search ya delete nahi ho sakti

## 📦 Download aur build

- Download：<https://github.com/zzy89216-gif/ZyNova/releases> (hamesha latest version lein)
- Khud build karein：JDK 17 + Android SDK / NDK, phir `./gradlew ZalithLauncher:assembleRelease`
- Note：pehli baar chalane par compliance flow aata hai; regions aur age limits ki tafseel [docs/en/licenses.md](../../docs/en/licenses.md) mein hai.

## 📚 Documentation

> 📚 Documentation filhaal Chinese aur English mein hai (70+ articles); ye page Roman Urdu taaruf hai.

| Content | File |
|---|---|
| Documentation index | [docs/en/README.md](../../docs/en/README.md) |
| Install / usage / export | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Rendering / login / favourites / AI | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/favorites.md](../../docs/en/favorites.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Troubleshooting / FAQ / data aur privacy | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Build | [docs/en/build.md](../../docs/en/build.md) |
| Changelog | [CHANGELOG.md](../../CHANGELOG.md) |
| Handoff / architecture / workflow | [HANDOFF.md](../../HANDOFF.md) |
| Naye caretaker ke liye entry (state / release / traps / doc rules) | [AGENTS.md](../../AGENTS.md) |
| Third-party licences | [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) |
| Licence | [LICENSE](../../LICENSE) |

## 💖 Support

- Masla report karein：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- Email：global <ZyNovaLauncher@outlook.com> ｜ China <ZyNovaLauncher@163.com>
- Official X：<https://x.com/ZyNovaProject>
- Project ko support karein：<https://afdian.com/a/hizzy>

## ⚖️ Licence

GPL-3.0 — project open source rehta hai. Third-party components aur unke licences
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) aur [docs/en/licenses.md](../../docs/en/licenses.md) mein hain.

> ⚠️ **Terracotta (multiplayer) AGPL-3.0 par hai**, baaki sab se sakht: bila tabdeeli distribute hota hai.
> Agar aap ise modify karke dobara distribute karein, to AGPL-3.0 §13 ke tehat mutabiq source code
> publish karna hoga.

---

**ZyNova — Context First. Less Steps.**
