<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **ZyNova 28.1.0 — випущено (версія з глобальною відповідністю вимогам).**


[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.1.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.1.0)

<p align="center"><sub>
<a href="../en/README.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<b>Українська</b> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

---

Лаунчер **Minecraft: Java Edition** для Android, який незалежно підтримується й публікується на основі коду ZalithLauncher2. Це не офіційна збірка.

Мета — **менше натискань**. Усе, що можна автоматизувати (встановлення гри, підбір модів і збірок, зміна налаштувань, читання логів), бере на себе AI-агент.

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Помилки та пропозиції：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- X (офіційний головний акаунт)：<https://x.com/ZyNovaProject>
- Пошта для китайської екосистеми：<ZyNovaLauncher@163.com>
- Глобальна пошта：<ZyNovaLauncher@outlook.com>

## ✨ Що нового в 28.1.0

- **Процедура відповідності під час першого запуску**：три об'єднані документи — ① EULA + політика конфіденційності, ② повідомлення про відкритий код третіх сторін + відмови від відповідальності й ризики, ③ посібник + регіони застосування та юрисдикція. Кожен документ треба **прокрутити до кінця й окремо позначити**; далі вибирається вікова група (наявні користувачі теж проходять це раз після оновлення).
- **Часове вікно для неповнолітніх**：до 18 років гру можна запускати лише в п'ятницю, суботу, неділю та в офіційні свята з **20:00 до 21:00**. Свята 2026 року перелічені за календарем Держради КНР (国办发明电〔2025〕7号); дні відпрацювання вважаються робочими.
- **Відмови від відповідальності прописані повністю**：втрата збережень, нагрів пристрою, блокування третьою стороною, припинення роботи сервісу та подібні ризики зазначені прямо.
- **Крім того**：картки на головній більше не зависають на завантаженні, вебпошук AI працює, імпорт іде в каталог інстансу, автоматичне виправлення після збою прибрано.

> 🌍 **Доступні регіони в 28.1.0**：Китай, США, Південна Корея, Японія та Росія. В інших регіонах під час першого запуску з'являється сторінка з поясненням, після чого застосунок безпечно закривається.

## 🛠️ Що вміє

- **AI-агент**：типово DeepSeek — встановлює гру, модпаки й моди, змінює налаштування, читає логи, запускає гру
- **Рендерери**：Авто / MobileGlues / GL4ES / LTW (експериментальний)
- **Ядро ресурсів**：пошук у Modrinth / CurseForge, розв'язання залежностей, завантаження, перевірка, встановлення
- **Обране й архів**：онлайн-обране + приватний архів із дедуплікацією SHA-256
- **Захист API Fabric**：Fabric API / Quilted Fabric API / Legacy Fabric API додає процес встановлення — їх не можна продублювати, знайти пошуком чи видалити

## 📦 Завантаження та збірка

- Завантаження：<https://github.com/zzy89216-gif/ZyNova/releases> (беріть найновішу версію)
- Власна збірка：JDK 17 + Android SDK / NDK, далі `./gradlew ZalithLauncher:assembleRelease`
- Увага：під час першого запуску буде процедура відповідності; подробиці щодо регіонів і вікових обмежень — у [docs/en/licenses.md](../../docs/en/licenses.md).

## 📚 Документація

> 📚 Документація наразі китайською та англійською (70+ статей); ця сторінка — український вступ.

| Зміст | Файл |
|---|---|
| Покажчик документації | [docs/en/README.md](../../docs/en/README.md) |
| Встановлення / користування / експорт | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Рендерери / вхід / обране / AI | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/favorites.md](../../docs/en/favorites.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Вирішення проблем / FAQ / дані та приватність | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Збірка | [docs/en/build.md](../../docs/en/build.md) |
| Журнал змін | [CHANGELOG.md](../../CHANGELOG.md) |
| Передача / архітектура / робочий процес | [HANDOFF.md](../../HANDOFF.md) |
| Вхід для тих, хто підхоплює (стан / реліз / пастки / правила документації) | [AGENTS.md](../../AGENTS.md) |
| Ліцензії третіх сторін | [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) |
| Ліцензія | [LICENSE](../../LICENSE) |

## 💖 Підтримка

- Повідомити про помилку：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- Email：глобально <ZyNovaLauncher@outlook.com> ｜ Китай <ZyNovaLauncher@163.com>
- Офіційний X：<https://x.com/ZyNovaProject>
- Підтримати проєкт：<https://afdian.com/a/hizzy>

## ⚖️ Ліцензія

GPL-3.0 — проєкт лишається відкритим. Компоненти третіх сторін та їхні ліцензії перелічені в
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) і [docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **Terracotta (мультиплеєр) поширюється за AGPL-3.0**, суворішою за решту: постачається без змін.
> Якщо ви його зміните й розповсюдите далі, §13 AGPL-3.0 вимагає опублікувати відповідний код.

---

**ZyNova — Context First. Less Steps.**
