<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.0.1-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.0.1)

[简体中文](README.md) | [English](README_EN.md) | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md) | [한국어](README_KO.md) | **Русский**

---

Лаунчер Minecraft: Java Edition для Android на основе исходников ZalithLauncher2. Поддерживается независимо.

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ Что нового в 28.0.1

- **SakuraFrp теперь действительно подключается**: `frpc` запускается в официальном виде `frpc -f <ключ доступа>:<ID туннеля>` (прежний способ через переменные окружения frpc от Sakura игнорирует и сразу завершается)
- **Обновление списка больше не падает с ошибкой**: поддерживаются ответы с BOM, массивы и обёртка `{data}`; адрес подключения извлекается из лога frpc, **показывается сверху и копируется одним нажатием**
- **Кнопка «назад» больше не закрывает весь экран**: верхняя «назад» не идёт через системную (без обработчика экран просто завершался), отступает ровно на один уровень и не опустошает стек
- **Из игры можно вернуться в лаунчер, не останавливая игру**: первый пункт игрового меню «Вернуться в лаунчер» **не останавливает игру**; внизу лаунчера появляется кнопка «Вернуться в игру»
- **Не нужно разбираться в портах и типах**: тип всегда TCP, локальный порт определяется автоматически из `/proc/net/tcp`, узлы фильтруются по уровню аккаунта
- **ИИ полностью автоматизирует сетевую игру**: `frp_status` / `frp_create_tunnel` / `frp_start_tunnel` / `frp_stop_tunnel` / `multiplayer_status`

## 🛠️ Возможности

- **AI Agent**: по умолчанию DeepSeek — ставит игру, модпаки и моды, меняет настройки, читает логи, запускает игру
- **Рендереры**: Авто / MobileGlues / GL4ES / LTW (экспериментальный)
- **Ресурсы**: поиск Modrinth / CurseForge, разрешение зависимостей, загрузка, проверка, установка
- **Избранное и архив**: онлайн-избранное плюс приватный архив с дедупликацией SHA-256
- **Защита Fabric API**: добавляется только процессом Fabric, вручную дублировать нельзя

## 📦 Загрузка и сборка

- Загрузка: <https://github.com/zzy89216-gif/ZyNova/releases>
- Сборка: JDK 17 и Android SDK / NDK, затем `./gradlew ZalithLauncher:assembleRelease`

## 📚 Документация

> 📚 **Полная документация (72 статьи, английский)**：[docs/en/README.md](docs/en/README.md)



| Содержание | Файл |
|---|---|
| Документация | [docs/en/README.md](docs/en/README.md) |
| Установка / использование / экспорт | [docs/en/install.md](docs/en/install.md) · [docs/en/usage.md](docs/en/usage.md) |
| Рендереры / вход / избранное / AI | [docs/en/renderers.md](docs/en/renderers.md) · [docs/en/login.md](docs/en/login.md) · [docs/en/favorites.md](docs/en/favorites.md) · [docs/en/ai-agent.md](docs/en/ai-agent.md) |
| Диагностика / FAQ / данные | [docs/en/troubleshooting.md](docs/en/troubleshooting.md) · [docs/en/faq.md](docs/en/faq.md) · [docs/en/data-and-privacy.md](docs/en/data-and-privacy.md) |
| Сборка | [docs/en/build.md](docs/en/build.md) |
| История изменений | [CHANGELOG.md](CHANGELOG.md) |
| Передача / архитектура / процесс | [HANDOFF.md](HANDOFF.md) |
| Сторонние лицензии | [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) |
| Лицензия | [LICENSE](LICENSE) |



## 💖 Поддержка

- Проблемы: <https://github.com/zzy89216-gif/ZyNova/issues>
- Discord: <https://discord.gg/QwPpZQHrTa>
- Поддержать: <https://afdian.com/a/hizzy>

## ⚖️ Лицензия

GPL-3.0, проект остаётся открытым. Сторонние компоненты: [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

---

**ZyNova — Context First. Less Steps.**
