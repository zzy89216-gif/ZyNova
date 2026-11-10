<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **ZyNova 28.1.0 — wydanie gotowe (wersja zgodna z wymogami globalnymi).**


[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.1.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.1.0)

<p align="center"><sub>
<a href="../en/README.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <b>Polski</b> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

---

Launcher **Minecraft: Java Edition** na Androida, rozwijany i publikowany niezależnie na bazie kodu ZalithLauncher2. To nie jest oficjalna kompilacja.

Cel: **mniej kliknięć**. Wszystko, co da się zautomatyzować — instalację gry, dobór modów i modpacków, zmianę ustawień, czytanie logów — przejmuje agent AI.

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Błędy i sugestie：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- X (oficjalne konto główne)：<https://x.com/ZyNovaProject>
- Skrzynka dla Chin：<ZyNovaLauncher@163.com>
- Skrzynka globalna：<ZyNovaLauncher@outlook.com>

## ✨ Nowości w 28.1.0

- **Procedura zgodności przy pierwszym uruchomieniu**：trzy połączone dokumenty — ① EULA + polityka prywatności, ② informacja o otwartym kodzie stron trzecich + wyłączenia odpowiedzialności i ryzyka, ③ samouczek + obsługiwane regiony i jurysdykcja. Każdy dokument trzeba **przewinąć do końca i osobno zaznaczyć**, dopiero potem wybiera się grupę wiekową (dotychczasowi użytkownicy też przechodzą to raz po aktualizacji).
- **Okno czasowe dla nieletnich**：osoby poniżej 18 lat mogą uruchomić grę tylko w piątek, sobotę, niedzielę i w dni ustawowo wolne, w godz. **20:00–21:00**. Święta 2026 są wymienione dzień po dniu według kalendarza Rady Państwowej ChRL (国办发明电〔2025〕7号); dni odrabiania traktowane są jak dni robocze.
- **Wyłączenia odpowiedzialności bez owijania w bawełnę**：utrata zapisów, nagrzewanie urządzenia, bany od stron trzecich, wyłączenie usługi i podobne ryzyka są wprost wymienione.
- **Poza tym**：karty na stronie głównej nie wiszą już na ładowaniu, wyszukiwanie internetowe AI działa, import trafia do katalogu instancji, usunięto automatyczną naprawę po awarii.

> 🌍 **Regiony dostępne w 28.1.0**：Chiny, USA, Korea Południowa, Japonia i Rosja. W pozostałych regionach przy pierwszym uruchomieniu pojawia się strona informacyjna, a aplikacja zamyka się bezpiecznie.

## 🛠️ Co potrafi

- **Agent AI**：domyślnie DeepSeek — instaluje grę, modpacki i mody, zmienia ustawienia, czyta logi, uruchamia grę
- **Renderery**：Auto / MobileGlues / GL4ES / LTW (eksperymentalny)
- **Rdzeń zasobów**：wyszukiwanie Modrinth / CurseForge, rozwiązywanie zależności, pobieranie, weryfikacja, instalacja
- **Ulubione i archiwum**：ulubione online + prywatne archiwum z deduplikacją SHA-256
- **Ochrona API Fabric**：Fabric API / Quilted Fabric API / Legacy Fabric API dodaje instalator — nie da się ich zdublować, wyszukać ani usunąć

## 📦 Pobieranie i kompilacja

- Pobieranie：<https://github.com/zzy89216-gif/ZyNova/releases> (zawsze najnowsza wersja)
- Samodzielna kompilacja：JDK 17 + Android SDK / NDK, potem `./gradlew ZalithLauncher:assembleRelease`
- Uwaga：przy pierwszym uruchomieniu pojawia się procedura zgodności; szczegóły regionów i limitów wieku są w [docs/en/licenses.md](../../docs/en/licenses.md).

## 📚 Dokumentacja

> 📚 Dokumentacja jest na razie po chińsku i angielsku (ponad 70 artykułów); ta strona to wprowadzenie po polsku.

| Treść | Plik |
|---|---|
| Spis dokumentacji | [docs/en/README.md](../../docs/en/README.md) |
| Instalacja / użytkowanie / eksport | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Renderery / logowanie / ulubione / AI | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/favorites.md](../../docs/en/favorites.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Rozwiązywanie problemów / FAQ / dane i prywatność | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Kompilacja | [docs/en/build.md](../../docs/en/build.md) |
| Dziennik zmian | [CHANGELOG.md](../../CHANGELOG.md) |
| Przekazanie / architektura / workflow | [HANDOFF.md](../../HANDOFF.md) |
| Wejście dla współtwórców (stan / wydanie / pułapki / zasady dokumentacji) | [AGENTS.md](../../AGENTS.md) |
| Licencje stron trzecich | [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) |
| Licencja | [LICENSE](../../LICENSE) |

## 💖 Wsparcie

- Zgłoś błąd：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- E-mail：globalnie <ZyNovaLauncher@outlook.com> ｜ Chiny <ZyNovaLauncher@163.com>
- Oficjalny X：<https://x.com/ZyNovaProject>
- Wesprzyj projekt：<https://afdian.com/a/hizzy>

## ⚖️ Licencja

GPL-3.0 — projekt pozostaje otwartoźródłowy. Komponenty stron trzecich i ich licencje są w
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) oraz [docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **Terracotta (multiplatforma) jest na AGPL-3.0**, surowszej niż reszta: dystrybuowana jest bez zmian.
> Jeśli ją zmodyfikujesz i będziesz dalej rozpowszechniać, art. 13 AGPL-3.0 nakazuje udostępnić odpowiedni
> kod źródłowy.

---

**ZyNova — Context First. Less Steps.**
