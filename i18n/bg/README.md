<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **ZyNova 28.1.0 е публикуван (издание със спазване на глобалните изисквания).**


[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.1.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.1.0)

<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <b>Български</b>
</sub></p>

---

Лаунчер за **Minecraft: Java Edition** на Android, поддържан и публикуван независимо на базата на кода на ZalithLauncher2. Това не е официална сборка.

Целта: **по-малко докосвания**. Всичко, което може да се автоматизира — инсталиране на играта, събиране на модове и модпакове, промяна на настройки, четене на логове — поема AI агентът.

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Грешки и предложения：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- X (официален основен акаунт)：<https://x.com/ZyNovaProject>
- Поща за китайската екосистема：<ZyNovaLauncher@163.com>
- Глобална поща：<ZyNovaLauncher@outlook.com>

## ✨ Ново в 28.1.0

- **Процедура за съответствие при първо стартиране**：три обединени документа — ① EULA + политика за поверителност, ② уведомление за open source на трети страни + отказ от отговорност и рискове, ③ ръководство + приложими региони и юрисдикция. Всеки документ трябва да се **превърти до края и да се отметне отделно**, след което се избира възрастова група (съществуващите потребители също минават през това веднъж след обновяване).
- **Времеви прозорец за непълнолетни**：под 18 години играта се стартира само в петък, събота, неделя и в официални празници от **20:00 до 21:00**. Празниците за 2026 г. са изброени по календара на Държавния съвет на КНР (国办发明电〔2025〕7号); дните за отработване се считат за работни.
- **Отказът от отговорност е изписан докрай**：загуба на запазени игри, нагряване на устройството, блокиране от трети страни, спиране на услугата и подобни рискове са посочени пряко.
- **Освен това**：картите на началния екран вече не засичат на зареждане, уеб търсенето на AI работи, импортът отива в папката на инстанцията, автоматичното възстановяване след срив е премахнато.

> 🌍 **Достъпни региони в 28.1.0**：Китай, САЩ, Южна Корея, Япония и Русия. В други региони при първо стартиране се показва информационна страница и приложението се затваря безопасно.

## 🛠️ Какво може

- **AI агент**：по подразбиране DeepSeek — инсталира играта, модпакове и модове, променя настройки, чете логове, стартира играта
- **Рендерери**：Авто / MobileGlues / GL4ES / LTW (експериментален)
- **Ядро за ресурси**：търсене в Modrinth / CurseForge, решаване на зависимости, изтегляне, проверка, инсталиране
- **Любими и архив**：онлайн любими + частен архив с дедупликация по SHA-256
- **Защита на Fabric API**：Fabric API / Quilted Fabric API / Legacy Fabric API се добавят от процеса на инсталиране — не могат да се дублират, търсят или изтриват

## 📦 Изтегляне и собствена компилация

- Изтегляне：<https://github.com/zzy89216-gif/ZyNova/releases> (винаги най-новата версия)
- Собствена компилация：JDK 17 + Android SDK / NDK, после `./gradlew ZalithLauncher:assembleRelease`
- Забележка：при първо стартиране се показва процедурата за съответствие; подробности за регионите и възрастовите ограничения има в [docs/en/licenses.md](../../docs/en/licenses.md).

## 📚 Документация

> 📚 Документацията засега е на китайски и английски (над 70 статии); тази страница е българското въведение.

| Съдържание | Файл |
|---|---|
| Показалец на документацията | [docs/en/README.md](../../docs/en/README.md) |
| Инсталиране / използване / експорт | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Рендериране / вход / любими / AI | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/favorites.md](../../docs/en/favorites.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Отстраняване на проблеми / FAQ / данни и поверителност | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Компилация | [docs/en/build.md](../../docs/en/build.md) |
| Дневник на промените | [CHANGELOG.md](../../CHANGELOG.md) |
| Предаване / архитектура / работен процес | [HANDOFF.md](../../HANDOFF.md) |
| Вход за поемащите (състояние / издание / капани / правила за документация) | [AGENTS.md](../../AGENTS.md) |
| Лицензи на трети страни | [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) |
| Лиценз | [LICENSE](../../LICENSE) |

## 💖 Подкрепа

- Съобщи за проблем：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- Имейл：глобално <ZyNovaLauncher@outlook.com> ｜ Китай <ZyNovaLauncher@163.com>
- Официален X：<https://x.com/ZyNovaProject>
- Подкрепи проекта：<https://afdian.com/a/hizzy>

## ⚖️ Лиценз

GPL-3.0 — проектът остава с отворен код. Компонентите на трети страни и техните лицензи са изброени в
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) и [docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **Terracotta (мултиплейър) е под AGPL-3.0**, по-строг от останалите: разпространява се без промени.
> Ако го промените и разпространите, §13 на AGPL-3.0 изисква да публикувате съответния изходен код.

---

**ZyNova — Context First. Less Steps.**
