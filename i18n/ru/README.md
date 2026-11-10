<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <b>Русский</b> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <a href="../es/README.md">Español</a> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **Выпущен ZyNova 28.1.0 (версия с глобальным соответствием).**


[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.1.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.1.0)

---

Лаунчер **Minecraft: Java Edition** для Android на основе исходников ZalithLauncher2. Поддерживается независимо, это не официальная сборка.

Цель — **меньше нажатий**. Всё, что можно автоматизировать (установка игры, подбор модов и сборок, правка настроек, чтение логов), берёт на себя AI-агент.

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- X (официальный основной аккаунт): <https://x.com/ZyNovaProject>
- Для Китая: <ZyNovaLauncher@163.com>
- Для остального мира: <ZyNovaLauncher@outlook.com>

## ✨ Что нового в 28.1.0

- **Процедура при первом запуске**: 3 объединённых документа — ① EULA + политика конфиденциальности, ② уведомление об открытом коде + дисклеймеры и риски, ③ руководство + регионы и юрисдикция. Каждый документ **нужно прокрутить до конца и отдельно отметить галочкой**, иначе дальше не пройти; затем выбирается возрастная группа (существующие пользователи тоже проходят это один раз после обновления).
- **Ограничение по времени для несовершеннолетних**: до 18 лет игра доступна только по пятницам, субботам, воскресеньям и в официальные праздники с **20:00 до 21:00**. Праздники 2026 года перечислены по датам согласно распоряжению Госсовета КНР (国办发明电〔2025〕7号); рабочие дни-отработки считаются обычными рабочими.
- **Дисклеймеры прописаны полностью**: потеря сохранений, нагрев устройства, блокировки третьими лицами, прекращение работы сервиса и подобные риски указаны прямо.
- **Кроме того**: карточки на главной больше не зависают на загрузке, веб-поиск AI работает, импорт идёт в папку инстанса, автопочинка после сбоя удалена.

## 🛠️ Возможности

- **AI Agent**: по умолчанию DeepSeek — ставит игру, модпаки и моды, меняет настройки, читает логи, запускает игру
- **Рендереры**: Авто / MobileGlues / GL4ES / LTW (экспериментальный)
- **Ядро ресурсов**: поиск в Modrinth / CurseForge, разрешение зависимостей, загрузка, проверка, установка
- **Избранное и архив**: онлайн-избранное + приватный архив с дедупликацией по SHA-256
- **Защита Fabric API**: Fabric API / Quilted Fabric API / Legacy Fabric API добавляются только процессом установки — их нельзя продублировать, найти поиском или удалить

## 📦 Загрузка и сборка

- Загрузка: <https://github.com/zzy89216-gif/ZyNova/releases> (берите последнюю версию)
- Своя сборка: JDK 17 + Android SDK / NDK, затем `./gradlew ZalithLauncher:assembleRelease`
- Обратите внимание: при первом запуске показывается процедура соответствия; подробности — в [docs/en/licenses.md](../../docs/en/licenses.md).

## 📚 Документация

> 📚 Документация пока существует на китайском и английском (по 77 статей); эта страница — вступление на русском.

| Содержание | Файл |
|---|---|
| Указатель документации | [docs/en/README.md](../../docs/en/README.md) |
| Установка / использование / экспорт | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Рендереры / вход / избранное / AI | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/favorites.md](../../docs/en/favorites.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Решение проблем / FAQ / данные и приватность | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Сборка | [docs/en/build.md](../../docs/en/build.md) |
| Журнал изменений | [CHANGELOG.md](../../CHANGELOG.md) |
| Передача / архитектура / процесс | [HANDOFF.md](../../HANDOFF.md) |
| Вход для принимающего (состояние / релиз / грабли / правила документации) | [AGENTS.md](../../AGENTS.md) |
| Лицензии третьих сторон | [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) |
| Лицензия | [LICENSE](../../LICENSE) |

## 💖 Поддержка

- Сообщить об ошибке：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- Почта: глобально <ZyNovaLauncher@outlook.com> ｜ Китай <ZyNovaLauncher@163.com>
- Официальный X：<https://x.com/ZyNovaProject>
- Поддержать проект：<https://afdian.com/a/hizzy>

## ⚖️ Лицензия

GPL-3.0 — проект остаётся открытым. Компоненты третьих сторон и их лицензии перечислены в
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) и [docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **Terracotta (мультиплеер) распространяется по AGPL-3.0**, более строгой, чем остальные:
> поставляется без изменений. Если вы его измените и будете распространять дальше, §13 AGPL-3.0 требует
> опубликовать соответствующий исходный код.

---

**ZyNova — Context First. Less Steps.**
