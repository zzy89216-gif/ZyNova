<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **ZyNova 28.1.0 a fost lansat (versiune conformă la nivel global).**


[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.1.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.1.0)

<p align="center"><sub>
<a href="../en/README.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <b>Română</b> · <a href="../bg/README.md">Български</a>
</sub></p>

---

Un launcher **Minecraft: Java Edition** pentru Android, întreținut și publicat independent pe baza codului sursă ZalithLauncher2. Nu este o versiune oficială.

Scopul: **mai puține atingeri**. Tot ce se poate automatiza — instalarea jocului, strângerea modurilor și modpack-urilor, modificarea setărilor, citirea logurilor — preia agentul AI.

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Erori și sugestii：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- X (cont principal oficial)：<https://x.com/ZyNovaProject>
- Căsuță pentru ecosistemul chinez：<ZyNovaLauncher@163.com>
- Căsuță globală：<ZyNovaLauncher@outlook.com>

## ✨ Noutăți în 28.1.0

- **Fluxul de conformitate la prima pornire**：trei documente unite — ① EULA + politica de confidențialitate, ② notificare open source pentru terți + limitări de răspundere și riscuri, ③ tutorial + regiunile acceptate și jurisdicția. Fiecare document trebuie **derulat până la capăt și bifat separat**, apoi alegi grupa de vârstă (utilizatorii existenți trec și ei o dată prin asta după actualizare).
- **Interval orar pentru minori**：sub 18 ani jocul poate fi pornit doar vineri, sâmbătă, duminică și în zilele de sărbătoare legală, între **20:00 și 21:00**. Sărbătorile din 2026 urmează calendarul Consiliului de Stat al Chinei (国办发明电〔2025〕7号); zilele de recuperare se consideră zile lucrătoare.
- **Limitările de răspundere sunt scrise explicit**：pierderea salvarilor, încălzirea dispozitivului, blocările aplicate de terți, oprirea serviciului și riscuri similare sunt anunțate clar.
- **De asemenea**：cardurile din pagina principală nu mai rămân blocate la încărcare, căutarea web a AI funcționează, importurile merg în folderul instanței, repararea automată după crash a fost eliminată.

> 🌍 **Regiuni disponibile în 28.1.0**：China, SUA, Coreea de Sud, Japonia și Rusia. În alte regiuni, la prima pornire apare o pagină informativă, iar aplicația se închide în siguranță.

## 🛠️ Ce poate face

- **Agent AI**：DeepSeek implicit — instalează jocul, modpack-urile și modurile, modifică setările, citește logurile, pornește jocul
- **Randare**：Automat / MobileGlues / GL4ES / LTW (experimental)
- **Nucleul de resurse**：căutare Modrinth / CurseForge, rezolvarea dependențelor, descărcare, verificare, instalare
- **Favorite și arhivă**：favorite online + arhivă privată, deduplicare SHA-256
- **Protecția API Fabric**：Fabric API / Quilted Fabric API / Legacy Fabric API sunt adăugate de fluxul de instalare — nu pot fi duplicate, căutate sau șterse

## 📦 Descărcare și compilare

- Descărcare：<https://github.com/zzy89216-gif/ZyNova/releases> (folosește întotdeauna ultima versiune)
- Compilare proprie：JDK 17 + Android SDK / NDK, apoi `./gradlew ZalithLauncher:assembleRelease`
- Notă：la prima pornire apare fluxul de conformitate; detaliile despre regiuni și limitele de vârstă sunt în [docs/en/licenses.md](../../docs/en/licenses.md).

## 📚 Documentație

> 📚 Documentația există momentan în chineză și engleză (peste 70 de articole); această pagină este introducerea în română.

| Conținut | Fișier |
|---|---|
| Indexul documentației | [docs/en/README.md](../../docs/en/README.md) |
| Instalare / utilizare / export | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Randare / autentificare / favorite / AI | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/favorites.md](../../docs/en/favorites.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Depanare / FAQ / date și confidențialitate | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Compilare | [docs/en/build.md](../../docs/en/build.md) |
| Jurnal de modificări | [CHANGELOG.md](../../CHANGELOG.md) |
| Predare / arhitectură / flux de lucru | [HANDOFF.md](../../HANDOFF.md) |
| Punct de intrare pentru contribuitori (stare / lansare / capcane / reguli de documentație) | [AGENTS.md](../../AGENTS.md) |
| Licențe terțe | [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) |
| Licență | [LICENSE](../../LICENSE) |

## 💖 Sprijin

- Raportează o problemă：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- E-mail：global <ZyNovaLauncher@outlook.com> ｜ China <ZyNovaLauncher@163.com>
- X oficial：<https://x.com/ZyNovaProject>
- Susține proiectul：<https://afdian.com/a/hizzy>

## ⚖️ Licență

GPL-3.0 — proiectul rămâne open source. Componentele terțe și licențele lor sunt listate în
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) și [docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **Terracotta (multiplayer) este sub AGPL-3.0**, mai strictă decât restul: este distribuită nemodificată.
> Dacă o modifici și o redistribui, art. 13 din AGPL-3.0 te obligă să publici codul sursă corespunzător.

---

**ZyNova — Context First. Less Steps.**
