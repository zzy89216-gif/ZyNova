<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <b>Български</b>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> 📌 **Current release: 28.2.1** · [Changelog](../../CHANGELOG.md) · [Releases](https://github.com/zzy89216-gif/ZyNova/releases/latest)

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.2.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.2.1)

---

Лаунчер за **Minecraft: Java Edition** на Android, поддържан независимо на базата на кода на ZalithLauncher2. Не е официална сборка.

Идеята е една: **по-малко докосвания**. Вместо да поднесе двайсет настройки, лаунчерът първо решава сам и пита само когато наистина не може.

## ✨ Последни промени

Пълен списък с промени: [CHANGELOG.md](../../CHANGELOG.md).

## 🛠️ Какво прави

- **AI агент**: по подразбиране DeepSeek — инсталира играта, модпакове и модове, променя настройки, чете логове, стартира играта
- **Рендерери**: Авто / MobileGlues / GL4ES / LTW (експериментален)
- **Ядро за ресурси**: търсене в Modrinth / CurseForge, решаване на зависимости, изтегляне, проверка, инсталиране
- **Любими и архив**: онлайн любими + частен архив с дедупликация по SHA-256
- **Защита на Fabric API**: добавят се само от процеса на инсталиране — без дублиране, без търсене, без изтриване

## 🚀 Бърз старт

1. Изтегли APK от [Releases](https://github.com/zzy89216-gif/ZyNova/releases) и го инсталирай (Android 8.0+)
2. При първо стартиране прочети трите документа до края, отбележи всеки и избери възрастова група
3. Влез с акаунт на Microsoft, избери или инсталирай версия, стартирай

## 📦 Изтегляне и компилация

- Изтегляне: <https://github.com/zzy89216-gif/ZyNova/releases> (винаги най-новата версия)
- Собствена компилация: JDK 17 + Android SDK / NDK, после `./gradlew ZalithLauncher:assembleRelease`
- Забележка: при първо стартиране се показва процедурата за съответствие; подробности в [docs/en/licenses.md](../../docs/en/licenses.md)

## 📚 Документация

> 📚 Документацията е на китайски и английски (по 77 статии); тази страница е българското въведение.

| Съдържание | Файл |
|---|---|
| Показалец | [docs/en/README.md](../../docs/en/README.md) |
| Инсталиране / използване / експорт | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Рендериране / вход / AI | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Проблеми / FAQ / данни и поверителност | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Компилация / дневник на промените | [docs/en/build.md](../../docs/en/build.md) · [CHANGELOG.md](../../CHANGELOG.md) |
| Предаване / лицензи на трети страни / лиценз | [HANDOFF.md](../../HANDOFF.md) · [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) · [LICENSE](../../LICENSE) |

## 💖 Подкрепа

- Съобщи за проблем：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- Имейл：<ZyNovaLauncher@outlook.com>
- Официален X：<https://x.com/ZyNovaProject>
- Подкрепи проекта：<https://afdian.com/a/hizzy>

## ⚖️ Лиценз

GPL-3.0 — проектът остава с отворен код. Компонентите на трети страни и лицензите им са в
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) и [docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **Terracotta (мултиплейър) е под AGPL-3.0**, по-строг от останалите: разпространява се без
> промени. Ако го промените и разпространите, §13 на AGPL-3.0 изисква да публикувате съответния
> изходен код.

---

**ZyNova — Context First. Less Steps.**
