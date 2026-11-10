<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **ZyNova 28.1.0 è disponibile (versione conforme a livello globale).**


[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.1.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.1.0)

<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <b>Italiano</b> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

---

Un launcher per **Minecraft: Java Edition** su Android, mantenuto e distribuito in modo indipendente a partire dai sorgenti di ZalithLauncher2. Non è una build ufficiale.

L'obiettivo: **meno tocchi**. Tutto ciò che si può automatizzare — installare il gioco, raccogliere mod e modpack, cambiare impostazioni, leggere i log — lo fa l'agente IA.

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Segnalazioni e proposte：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- X (account principale ufficiale)：<https://x.com/ZyNovaProject>
- Casella ecosistema cinese：<ZyNovaLauncher@163.com>
- Casella globale：<ZyNovaLauncher@outlook.com>

## ✨ Novità della 28.1.0

- **Procedura di conformità al primo avvio**：tre documenti uniti — ① EULA + informativa sulla privacy, ② avvisi open source di terzi + esoneri e rischi, ③ guida all'uso + aree supportate e giurisdizione. Ogni documento va **scorso fino in fondo e spuntato singolarmente**, poi si sceglie la fascia d'età (anche chi ha già l'app la rivede una volta dopo l'aggiornamento).
- **Fascia oraria per i minori**：sotto i 18 anni l'avvio è consentito solo venerdì, sabato, domenica e nei giorni festivi, dalle **20:00 alle 21:00**. Le festività 2026 seguono il calendario del Consiglio di Stato cinese (国办发明电〔2025〕7号); i giorni di recupero valgono come lavorativi.
- **Esoneri espliciti**：perdita dei salvataggi, surriscaldamento del dispositivo, ban da parte di terzi, chiusura di un servizio e rischi simili sono dichiarati senza giri di parole.
- **Inoltre**：la home non resta più bloccata sul caricamento, la ricerca web dell'IA funziona, le importazioni finiscono nella cartella dell'istanza, rimossa la riparazione automatica dei crash.

> 🌍 **Aree disponibili nella 28.1.0**：Cina, Stati Uniti, Corea del Sud, Giappone e Russia. Altrove al primo avvio compare una pagina informativa e l'app si chiude in modo sicuro.

## 🛠️ Funzioni

- **Agente IA**：DeepSeek per impostazione predefinita — installa gioco, modpack e mod, modifica le impostazioni, legge i log, avvia il gioco
- **Renderer**：Auto / MobileGlues / GL4ES / LTW (sperimentale)
- **Nucleo risorse**：ricerca su Modrinth / CurseForge, risoluzione delle dipendenze, download, verifica, installazione
- **Preferiti e archivio**：preferiti online + archivio privato con deduplica SHA-256
- **Protezione delle API Fabric**：Fabric API / Quilted Fabric API / Legacy Fabric API vengono aggiunte dalla procedura di installazione — niente doppioni, non cercabili, non eliminabili

## 📦 Download e compilazione

- Download：<https://github.com/zzy89216-gif/ZyNova/releases> (usa sempre l'ultima versione)
- Compilare da soli：JDK 17 + Android SDK / NDK, poi `./gradlew ZalithLauncher:assembleRelease`
- Nota：al primo avvio compare la procedura di conformità; aree abilitate e limiti d'età sono in [docs/en/licenses.md](../../docs/en/licenses.md).

## 📚 Documentazione

> 📚 La documentazione è per ora in cinese e inglese (oltre 70 articoli); questa pagina è l'introduzione in italiano.

| Contenuto | File |
|---|---|
| Indice della documentazione | [docs/en/README.md](../../docs/en/README.md) |
| Installazione / uso / esportazione | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Rendering / accesso / preferiti / IA | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/favorites.md](../../docs/en/favorites.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Risoluzione problemi / FAQ / dati e privacy | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Compilazione | [docs/en/build.md](../../docs/en/build.md) |
| Registro delle modifiche | [CHANGELOG.md](../../CHANGELOG.md) |
| Passaggio di consegne / architettura / flusso | [HANDOFF.md](../../HANDOFF.md) |
| Ingresso per chi contribuisce (stato / release / trappole / regole dei doc) | [AGENTS.md](../../AGENTS.md) |
| Licenze di terzi | [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) |
| Licenza | [LICENSE](../../LICENSE) |

## 💖 Supporto

- Segnala un problema：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- E-mail：globale <ZyNovaLauncher@outlook.com> ｜ Cina <ZyNovaLauncher@163.com>
- X ufficiale：<https://x.com/ZyNovaProject>
- Sostieni il progetto：<https://afdian.com/a/hizzy>

## ⚖️ Licenza

GPL-3.0: il progetto resta open source. I componenti di terzi e le rispettive licenze sono elencati in
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) e [docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **Terracotta (multigiocatore) è sotto AGPL-3.0**, più restrittiva del resto: viene distribuita senza
> modifiche. Se la modifichi e la ridistribuisci, l'art. 13 della AGPL-3.0 ti obbliga a pubblicare il codice
> sorgente corrispondente.

---

**ZyNova — Context First. Less Steps.**
