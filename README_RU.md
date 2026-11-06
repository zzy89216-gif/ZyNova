<p align="center">
  <img src="assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-27.10.2-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.10.2)

[简体中文](README.md) | [English](README_EN.md) | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md) | [한국어](README_KO.md) | **Русский**

---

Лаунчер Minecraft: Java Edition для Android на основе исходников ZalithLauncher2. Поддерживается независимо.

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Issues：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>

## ✨ Что нового в 27.10.2

- **Страница персонализации**: обои, прозрачность фона, размытие, радиус углов и сила «стекла» в одном месте (кнопка-палитра в верхней капсуле)
- **Переработанная основа «стекла»**: во всём приложении только одно размытие фона, все постоянные анимации убраны; карточки — плоский градиент + рамка 1px
- **AI-чат**: вызовы инструментов показываются одной строкой `Выполнение команды · описание`, исходный вывод свёрнут; перед запуском инструмента модель обязана написать краткое описание
- **Защита Quilt**: Quilted / Legacy Fabric API теперь так же «не находятся, не ставятся, не удаляются и не отключаются», как Fabric API
- **Исправления**: кнопка «назад» не работала / мерцание экрана, SakuraFrp падал сразу после ввода ключа доступа, диалоги накладывали второе размытие
- Это **тестовая сборка (pre-release)**

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
