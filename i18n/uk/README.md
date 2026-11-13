<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<b>Українська</b> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **Випущено ZyNova 28.1.0 (версія з глобальною відповідністю).**
>
> 📌 **Current release: 28.2.0** · [Changelog](../../CHANGELOG.md) · [Releases](https://github.com/zzy89216-gif/ZyNova/releases/latest)


[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.2.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.2.0)

---

Лаунчер **Minecraft: Java Edition** для Android на коді ZalithLauncher2. Незалежний проєкт, не офіційна збірка.

Ідея проста: **менше натискань**. Спочатку лаунчер вирішує сам — і питає лише тоді, коли справді не може вирішити.

## 🚀 Швидкий старт

1. Завантажте APK з [Releases](https://github.com/zzy89216-gif/ZyNova/releases) і встановіть (Android 8.0+)
2. Перший запуск: три документи — прокрутити до кінця, позначити кожен, вибрати вікову групу
3. Вхід у Microsoft → вибір версії (або хай AI встановить) → запуск

## ✨ Що нового в 28.1.0

- **Процедура відповідності під час першого запуску**: три об'єднані документи (①EULA + політика конфіденційності, ②повідомлення про відкритий код третіх сторін + відмови й ризики, ③посібник + регіони та юрисдикція). Кожен треба **прокрутити до кінця й окремо позначити**; далі обирається вікова група (наявні користувачі проходять це раз після оновлення).
- **Часове вікно для неповнолітніх**: до 18 років гра доступна лише в п'ятницю, суботу, неділю та в офіційні свята, **20:00–21:00**. Свята 2026 року перелічені за календарем Держради КНР; дні відпрацювання вважаються робочими.
- **Відмови від відповідальності**: втрата збережень, нагрів пристрою, блокування третьою стороною, припинення сервісу — усе зазначено прямо.
- **Також**: картки на головній не зависають, вебпошук AI працює, імпорт іде в каталог інстансу, автовиправлення після збою прибрано.

## 🛠️ Що вміє

- **AI-агент**: типово DeepSeek — ставить гру, модпаки й моди, змінює налаштування, читає логи, запускає гру
- **Рендерери**: Авто / MobileGlues / GL4ES / LTW (експериментальний)
- **Ядро ресурсів**: пошук у Modrinth / CurseForge, розв'язання залежностей, завантаження, перевірка, встановлення
- **Обране й архів**: онлайн-обране + приватний архів із дедуплікацією SHA-256
- **Захист Fabric API**: додається лише процесом встановлення — продублювати, знайти пошуком чи видалити не вийде

## 🧩 Словник

- **Збірка (модпак)** — набір модів, який ставиться однією дією
- **Шейдери** — покращене освітлення й вода, вмикаються в налаштуваннях графіки
- **Інстанс** — окрема тека з версією, модами та налаштуваннями

## 📦 Завантаження та збірка

- Завантаження: <https://github.com/zzy89216-gif/ZyNova/releases> (беріть найновішу версію)
- Потрібен Android 8.0+; власна збірка — JDK 17 + Android SDK / NDK і `./gradlew ZalithLauncher:assembleRelease`
- Під час першого запуску буде процедура відповідності; подробиці — [docs/en/licenses.md](../../docs/en/licenses.md)

## 📚 Документація

> 📚 Документація поки китайською та англійською (по 77 статей). Ця сторінка — український вступ.

| Зміст | Файл |
|---|---|
| Покажчик документації | [docs/en/README.md](../../docs/en/README.md) |
| Встановлення / користування / експорт | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Рендерери / вхід / обране / AI | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Проблеми / FAQ / дані та приватність | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Збірка / журнал змін | [docs/en/build.md](../../docs/en/build.md) · [CHANGELOG.md](../../CHANGELOG.md) |
| Передача / ліцензії третіх сторін / ліцензія | [HANDOFF.md](../../HANDOFF.md) · [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) · [LICENSE](../../LICENSE) |

## 💖 Підтримка

- Повідомити про помилку：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- Пошта：<ZyNovaLauncher@outlook.com>
- Офіційний X：<https://x.com/ZyNovaProject>
- Підтримати проєкт：<https://afdian.com/a/hizzy>

## ⚖️ Ліцензія

GPL-3.0 — проєкт лишається відкритим. Компоненти третіх сторін та їхні ліцензії перелічені в
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) і [docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **Terracotta (мультиплеєр) поширюється за AGPL-3.0** — суворіше за решту: постачається без змін.
> Якщо ви його зміните й розповсюдите далі, §13 AGPL-3.0 вимагає опублікувати відповідний код.

---

**ZyNova — Context First. Less Steps.**
