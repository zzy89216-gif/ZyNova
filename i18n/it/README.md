<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <b>Italiano</b> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> 📌 **Current release: 28.2.1** · [Changelog](../../CHANGELOG.md) · [Releases](https://github.com/zzy89216-gif/ZyNova/releases/latest)

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.2.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.2.1)

---

Un launcher per **Minecraft: Java Edition** su Android, mantenuto in modo indipendente dai sorgenti di ZalithLauncher2. Non è una build ufficiale.

L'idea è una sola: **meno tocchi**. Invece di mostrare venti impostazioni, il launcher decide prima e chiede solo quando non può fare altrimenti.

## 🚀 In tre passi

1. Scarica l'APK da [Releases](https://github.com/zzy89216-gif/ZyNova/releases) e installalo (Android 8.0+)
2. Al primo avvio leggi i tre documenti fino in fondo, spunta ognuno e scegli la fascia d'età
3. Accedi con l'account Microsoft, scegli o installa una versione, avvia

## ✨ Modifiche recenti

Modifiche complete: [CHANGELOG.md](../../CHANGELOG.md).

## 🛠️ Funzioni

- **Agente IA**: DeepSeek per impostazione predefinita — installa gioco, modpack e mod, modifica le impostazioni, legge i log, avvia il gioco
- **Renderer**: Auto / MobileGlues / GL4ES / LTW (sperimentale)
- **Nucleo risorse**: ricerca su Modrinth / CurseForge, risoluzione delle dipendenze, download, verifica, installazione
- **Preferiti e archivio**: preferiti online + archivio privato con deduplica SHA-256
- **Protezione delle API Fabric**: Fabric API / Quilted Fabric API / Legacy Fabric API arrivano solo dall'installazione — niente doppioni, non cercabili, non eliminabili

## 📦 Download e compilazione

- Download: <https://github.com/zzy89216-gif/ZyNova/releases> (sempre l'ultima versione)
- Compilare da soli: JDK 17 + Android SDK / NDK, poi `./gradlew ZalithLauncher:assembleRelease`
- Nota: al primo avvio compare la procedura di conformità; i dettagli sono in [docs/en/licenses.md](../../docs/en/licenses.md)

## 📚 Documentazione

> 📚 La documentazione è in cinese e inglese (77 articoli ciascuna); questa pagina è l'introduzione in italiano.

| Contenuto | File |
|---|---|
| Indice | [docs/en/README.md](../../docs/en/README.md) |
| Installazione / uso / esportazione | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Rendering / accesso / IA | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Problemi / FAQ / dati e privacy | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Compilazione / registro modifiche | [docs/en/build.md](../../docs/en/build.md) · [CHANGELOG.md](../../CHANGELOG.md) |
| Passaggio di consegne / licenze di terzi / licenza | [HANDOFF.md](../../HANDOFF.md) · [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) · [LICENSE](../../LICENSE) |

## 💖 Supporto

- Segnala un problema：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- E-mail：<ZyNovaLauncher@outlook.com>
- X ufficiale：<https://x.com/ZyNovaProject>
- Sostieni il progetto：<https://afdian.com/a/hizzy>

## ⚖️ Licenza

GPL-3.0: il progetto resta open source. I componenti di terzi e le rispettive licenze sono in
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) e [docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **Terracotta (multigiocatore) è sotto AGPL-3.0**, più restrittiva del resto: viene distribuita senza
> modifiche. Se la modifichi e la ridistribuisci, l'art. 13 della AGPL-3.0 ti obbliga a pubblicare il
> codice sorgente corrispondente.

---

**ZyNova — Context First. Less Steps.**
