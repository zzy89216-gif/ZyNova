<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <b>Deutsch</b> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **ZyNova 28.1.0 veröffentlicht (global konforme Version).**
>
> 📌 **Current release: 28.2.0** · [Changelog](../../CHANGELOG.md) · [Releases](https://github.com/zzy89216-gif/ZyNova/releases/latest)


[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.2.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.2.0)

---

Ein Launcher für **Minecraft: Java Edition** unter Android, unabhängig gepflegt auf Basis des ZalithLauncher2-Quellcodes. Kein offizieller Build.

Das Prinzip: **weniger tippen**. Erst entscheiden, dann fragen — und nur dort fragen, wo sich nichts entscheiden lässt. Installation, Mod-Abhängigkeiten und Absturz-Logs übernimmt der KI-Agent.

## ✨ Neu in 28.1.0

- **Compliance-Ablauf beim ersten Start**: drei zusammengefasste Dokumente (① EULA + Datenschutzerklärung, ② Open-Source-Hinweise Dritter + Haftungsausschlüsse und Risiken, ③ Anleitung + Geltungsbereich und Gerichtsstand). Jedes muss **bis zum Ende gescrollt und einzeln bestätigt** werden; danach die Altersgruppe wählen (Bestandsnutzer sehen das nach dem Update einmal).
- **Zeitfenster für Minderjährige**: unter 18 nur freitags, samstags, sonntags und an gesetzlichen Feiertagen, **20:00–21:00**. Die Feiertage 2026 folgen dem Kalender des chinesischen Staatsrats (国办发明电〔2025〕7号); Nachholarbeitstage zählen als Arbeitstage.
- **Haftungsausschlüsse ausformuliert**: Spielstandverlust, Hitzeentwicklung, Sperren durch Drittanbieter, Einstellung von Diensten — ausdrücklich benannt.
- **Außerdem**: Startseite lädt nicht mehr endlos, KI-Websuche funktioniert, Importe landen im Instanzordner, automatische Crash-Reparatur entfernt.

## 🛠️ Funktionen

- **KI-Agent**: standardmäßig DeepSeek — installiert Spiel, Modpacks und Mods, ändert Einstellungen, liest Logs, startet das Spiel
- **Renderer**: Auto / MobileGlues / GL4ES / LTW (experimentell)
- **Ressourcen-Kern**: Suche bei Modrinth / CurseForge, Abhängigkeiten auflösen, herunterladen, prüfen, installieren
- **Favoriten & Archiv**: Online-Favoriten plus privates Archiv mit SHA-256-Deduplizierung
- **Fabric-API-Schutz**: Fabric API / Quilted Fabric API / Legacy Fabric API kommen nur über den Installationsablauf — kein Duplikat, nicht auffindbar, nicht löschbar

## 🔧 Technik und Begriffe

- Android 8.0 oder neuer (`minSdk 26`), gebaut mit JDK 17 + Android SDK / NDK
- **Modpack** = Sammlung von Mods, **Shader** = erweiterte Beleuchtung und Wasser, **Instanz** = eigener Ordner mit Version, Mods und Einstellungen
- Renderer wird passend zum Gerät gewählt; bei Grafikfehlern lohnt der manuelle Wechsel

## 📦 Download und Build

- Download: <https://github.com/zzy89216-gif/ZyNova/releases> (immer die neueste Version)
- Selbst bauen: `./gradlew ZalithLauncher:assembleRelease`
- Hinweis: Beim ersten Start läuft der Compliance-Ablauf; Details in [docs/en/licenses.md](../../docs/en/licenses.md)

## 📚 Dokumentation

> 📚 Die Dokumentation gibt es auf Chinesisch und Englisch (je 77 Artikel); diese Seite ist die deutsche Einführung.

| Inhalt | Datei |
|---|---|
| Dokumentationsübersicht | [docs/en/README.md](../../docs/en/README.md) |
| Installation / Nutzung / Export | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Renderer / Login / Favoriten / KI | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Fehlersuche / FAQ / Daten & Datenschutz | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Bauen / Änderungsprotokoll | [docs/en/build.md](../../docs/en/build.md) · [CHANGELOG.md](../../CHANGELOG.md) |
| Übergabe / Lizenzen Dritter / Lizenz | [HANDOFF.md](../../HANDOFF.md) · [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) · [LICENSE](../../LICENSE) |

## 💖 Unterstützung

- Fehler melden：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- E-Mail：<ZyNovaLauncher@outlook.com>
- Offizielles X：<https://x.com/ZyNovaProject>
- Unterstützen：<https://afdian.com/a/hizzy>

## ⚖️ Lizenz

GPL-3.0 — bleibt Open Source. Komponenten Dritter und ihre Lizenzen stehen in
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) und [docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **Terracotta (Multiplayer) steht unter AGPL-3.0** — strenger als der Rest und wird unverändert
> ausgeliefert. Wer es verändert und weiterverbreitet, muss nach AGPL-3.0 §13 den passenden Quellcode
> veröffentlichen.

---

**ZyNova — Context First. Less Steps.**
