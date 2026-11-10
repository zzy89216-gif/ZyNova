<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <b>Polski</b> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **ZyNova 28.1.0 — wydanie gotowe (wersja zgodna z wymogami globalnymi).**


[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.1.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.1.0)

---

Launcher **Minecraft: Java Edition** na Androida, rozwijany niezależnie na bazie kodu ZalithLauncher2. To nie jest oficjalna kompilacja.

Pomysł jest jeden: **mniej kliknięć**. Zamiast wyrzucać na ekran dwadzieścia ustawień, launcher najpierw decyduje sam, a pyta dopiero wtedy, gdy naprawdę nie może.

## ✨ Nowości w 28.1.0

- **Procedura zgodności przy pierwszym uruchomieniu**: trzy połączone dokumenty (① EULA + polityka prywatności, ② informacja o otwartym kodzie stron trzecich + wyłączenia i ryzyka, ③ samouczek + regiony i jurysdykcja). Każdy trzeba **przewinąć do końca i osobno zaznaczyć**; potem wybiera się grupę wiekową (dotychczasowi użytkownicy przechodzą to raz po aktualizacji).
- **Okno czasowe dla nieletnich**: osoby poniżej 18 lat mogą uruchomić grę tylko w piątek, sobotę, niedzielę i w dni ustawowo wolne, w godz. **20:00–21:00**. Święta 2026 są wymienione dzień po dniu według kalendarza Rady Państwowej ChRL; dni odrabiania liczą się jako robocze.
- **Wyłączenia odpowiedzialności bez owijania w bawełnę**: utrata zapisów, nagrzewanie urządzenia, bany od stron trzecich, wyłączenie usługi — wszystko wprost.
- **Poza tym**: karty na stronie głównej nie wiszą na ładowaniu, wyszukiwanie internetowe AI działa, import trafia do katalogu instancji, usunięto automatyczną naprawę po awarii.

## 🛠️ Co potrafi

- **Agent AI**: domyślnie DeepSeek — instaluje grę, modpacki i mody, zmienia ustawienia, czyta logi, uruchamia grę
- **Renderery**: Auto / MobileGlues / GL4ES / LTW (eksperymentalny)
- **Rdzeń zasobów**: wyszukiwanie Modrinth / CurseForge, rozwiązywanie zależności, pobieranie, weryfikacja, instalacja
- **Ulubione i archiwum**: ulubione online + prywatne archiwum z deduplikacją SHA-256
- **Ochrona API Fabric**: Fabric API / Quilted Fabric API / Legacy Fabric API dodaje instalator — nie da się ich zdublować, wyszukać ani usunąć

## 🚀 Szybki start

1. Pobierz APK z [Releases](https://github.com/zzy89216-gif/ZyNova/releases) i zainstaluj (Android 8.0+)
2. Przy pierwszym uruchomieniu przeczytaj trzy dokumenty do końca, zaznacz każdy i wybierz grupę wiekową
3. Zaloguj się kontem Microsoft, wybierz lub zainstaluj wersję, uruchom

## 📦 Pobieranie i kompilacja

- Pobieranie: <https://github.com/zzy89216-gif/ZyNova/releases> (zawsze najnowsza wersja)
- Samodzielna kompilacja: JDK 17 + Android SDK / NDK, potem `./gradlew ZalithLauncher:assembleRelease`
- Uwaga: przy pierwszym uruchomieniu pojawia się procedura zgodności; szczegóły w [docs/en/licenses.md](../../docs/en/licenses.md)

## 📚 Dokumentacja

> 📚 Dokumentacja jest po chińsku i angielsku (po 77 artykułów); ta strona to wprowadzenie po polsku.

| Treść | Plik |
|---|---|
| Spis dokumentacji | [docs/en/README.md](../../docs/en/README.md) |
| Instalacja / użytkowanie / eksport | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Renderery / logowanie / AI | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Problemy / FAQ / dane i prywatność | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Kompilacja / dziennik zmian | [docs/en/build.md](../../docs/en/build.md) · [CHANGELOG.md](../../CHANGELOG.md) |
| Przekazanie / licencje stron trzecich / licencja | [HANDOFF.md](../../HANDOFF.md) · [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) · [LICENSE](../../LICENSE) |

## 💖 Wsparcie

- Zgłoś błąd：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- E-mail：<ZyNovaLauncher@outlook.com>
- Oficjalny X：<https://x.com/ZyNovaProject>
- Wesprzyj projekt：<https://afdian.com/a/hizzy>

## ⚖️ Licencja

GPL-3.0 — projekt pozostaje otwartoźródłowy. Komponenty stron trzecich i ich licencje są w
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) oraz [docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **Terracotta (multiplatforma) jest na AGPL-3.0**, surowszej niż reszta: dystrybuowana bez zmian.
> Jeśli ją zmodyfikujesz i będziesz rozpowszechniać, art. 13 AGPL-3.0 nakazuje udostępnić odpowiedni kod
> źródłowy.

---

**ZyNova — Context First. Less Steps.**
