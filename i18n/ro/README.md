<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <b>Română</b> · <a href="../bg/README.md">Български</a>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> 📌 **Current release: 28.2.1** · [Changelog](../../CHANGELOG.md) · [Releases](https://github.com/zzy89216-gif/ZyNova/releases/latest)

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.2.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.2.1)

---

Un launcher **Minecraft: Java Edition** pentru Android, întreținut independent pe baza codului ZalithLauncher2. Nu este o versiune oficială.

Ideea: **mai puține atingeri**. În loc să-ți arunce în față douăzeci de setări, launcherul decide întâi și întreabă doar când chiar nu se poate decide.

## ✨ Modificări recente

Toate modificările: [CHANGELOG.md](../../CHANGELOG.md).

## 🛠️ Ce poate face

- **Agent AI**: DeepSeek implicit — instalează jocul, modpack-urile și modurile, modifică setările, citește logurile, pornește jocul
- **Randare**: Automat / MobileGlues / GL4ES / LTW (experimental)
- **Nucleul de resurse**: căutare Modrinth / CurseForge, rezolvarea dependențelor, descărcare, verificare, instalare
- **Favorite și arhivă**: favorite online + arhivă privată, deduplicare SHA-256
- **Protecția API Fabric**: Fabric API / Quilted Fabric API / Legacy Fabric API sunt adăugate de instalare — nu pot fi duplicate, căutate sau șterse

## 🚀 Primii pași

1. Descarcă APK-ul din [Releases](https://github.com/zzy89216-gif/ZyNova/releases) și instalează-l (Android 8.0+)
2. La prima pornire citește cele trei documente până la capăt, bifează fiecare și alege grupa de vârstă
3. Autentifică-te cu contul Microsoft, alege sau instalează o versiune, pornește

## 📦 Descărcare și compilare

- Descărcare: <https://github.com/zzy89216-gif/ZyNova/releases> (întotdeauna ultima versiune)
- Compilare proprie: JDK 17 + Android SDK / NDK, apoi `./gradlew ZalithLauncher:assembleRelease`
- Notă: la prima pornire apare fluxul de conformitate; detalii în [docs/en/licenses.md](../../docs/en/licenses.md)

## 📚 Documentație

> 📚 Documentația există în chineză și engleză (câte 77 de articole); această pagină este introducerea în română.

| Conținut | Fișier |
|---|---|
| Indexul documentației | [docs/en/README.md](../../docs/en/README.md) |
| Instalare / utilizare / export | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Randare / autentificare / AI | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Depanare / FAQ / date și confidențialitate | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Compilare / jurnal de modificări | [docs/en/build.md](../../docs/en/build.md) · [CHANGELOG.md](../../CHANGELOG.md) |
| Predare / licențe terțe / licență | [HANDOFF.md](../../HANDOFF.md) · [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) · [LICENSE](../../LICENSE) |

## 💖 Sprijin

- Raportează o problemă：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- E-mail：<ZyNovaLauncher@outlook.com>
- X oficial：<https://x.com/ZyNovaProject>
- Susține proiectul：<https://afdian.com/a/hizzy>

## ⚖️ Licență

GPL-3.0 — proiectul rămâne open source. Componentele terțe și licențele lor sunt în
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) și [docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **Terracotta (multiplayer) este sub AGPL-3.0**, mai strictă decât restul: este distribuită
> nemodificată. Dacă o modifici și o redistribui, art. 13 din AGPL-3.0 te obligă să publici codul sursă
> corespunzător.

---

**ZyNova — Context First. Less Steps.**
