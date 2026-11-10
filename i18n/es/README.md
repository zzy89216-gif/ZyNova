<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <b>Español</b> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a>
</sub></p>

<p align="center">
  <img src="../../assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> ⭐ **ZyNova 28.1.0 ya está disponible (versión con cumplimiento global).**

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.1.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.1.0)

---

Un lanzador de **Minecraft: Java Edition** para Android, mantenido y publicado de forma independiente a partir del código de ZalithLauncher2. No es una compilación oficial.

La idea: **menos toques**. Todo lo que se pueda automatizar —instalar el juego, reunir los mods, ajustar opciones, leer los registros— lo hace el agente de IA.

- GitHub：<https://github.com/zzy89216-gif/ZyNova>
- Incidencias y sugerencias：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- X (cuenta principal oficial)：<https://x.com/ZyNovaProject>
- Correo del ecosistema chino：<ZyNovaLauncher@163.com>
- Correo global：<ZyNovaLauncher@outlook.com>

## ✨ Novedades de la 28.1.0

- **Flujo de cumplimiento en el primer arranque**：tres documentos unificados — ① EULA + política de privacidad, ② avisos de código abierto de terceros + descargos y riesgos, ③ tutorial + regiones admitidas y jurisdicción. Cada documento hay que **desplazarlo hasta el final y aceptarlo por separado**; después se elige el grupo de edad (quien ya tenía la app también lo ve una vez tras actualizar).
- **Franja horaria para menores**：los menores de 18 años solo pueden iniciar el juego los viernes, sábados, domingos y festivos legales, de **20:00 a 21:00**. Los festivos de 2026 siguen el calendario del Consejo de Estado chino (国办发明电〔2025〕7号); los días de recuperación cuentan como laborables.
- **Descargos bien explícitos**：pérdida de partidas, calentamiento del dispositivo, bloqueos de terceros, cierre de servicios y riesgos similares se avisan sin rodeos.
- **También**：la pantalla de inicio ya no se queda cargando, la búsqueda web de la IA funciona, las importaciones van a la carpeta de la instancia y se eliminó la reparación automática de fallos.

## 🛠️ Funciones

- **Agente de IA**：DeepSeek por defecto — instala el juego, modpacks y mods, cambia ajustes, lee registros y lanza el juego
- **Renderizadores**：Auto / MobileGlues / GL4ES / LTW (experimental)
- **Núcleo de recursos**：búsqueda en Modrinth / CurseForge, resolución de dependencias, descarga, verificación e instalación
- **Favoritos y archivo**：favoritos en línea más archivo privado con deduplicación SHA-256
- **Protección de la API de Fabric**：Fabric API / Quilted Fabric API / Legacy Fabric API los añade el instalador; no se pueden duplicar, buscar ni borrar

## 📦 Descarga y compilación

- Descarga：<https://github.com/zzy89216-gif/ZyNova/releases> (usa siempre la última versión)
- Compilar por tu cuenta：JDK 17 + Android SDK / NDK, y luego `./gradlew ZalithLauncher:assembleRelease`
- Aviso：el flujo de cumplimiento aparece en el primer arranque. Las regiones habilitadas y los límites de edad están en [docs/en/licenses.md](../../docs/en/licenses.md).

## 📚 Documentación

> 📚 La documentación existe de momento en chino e inglés (más de 70 artículos); esta página es la introducción en español.

| Contenido | Archivo |
|---|---|
| Índice de la documentación | [docs/en/README.md](../../docs/en/README.md) |
| Instalación / uso / exportación | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Renderizado / inicio de sesión / favoritos / IA | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/favorites.md](../../docs/en/favorites.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Solución de problemas / FAQ / datos y privacidad | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Compilación | [docs/en/build.md](../../docs/en/build.md) |
| Registro de cambios | [CHANGELOG.md](../../CHANGELOG.md) |
| Traspaso / arquitectura / flujo de trabajo | [HANDOFF.md](../../HANDOFF.md) |
| Entrada para colaboradores (estado / publicación / trampas / normas de documentación) | [AGENTS.md](../../AGENTS.md) |
| Licencias de terceros | [THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) |
| Licencia | [LICENSE](../../LICENSE) |

## 💖 Apoyo

- Reportar fallos：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/QwPpZQHrTa>
- Correo：global <ZyNovaLauncher@outlook.com> ｜ China <ZyNovaLauncher@163.com>
- X oficial：<https://x.com/ZyNovaProject>
- Apoyar el proyecto：<https://afdian.com/a/hizzy>

## ⚖️ Licencia

GPL-3.0: el proyecto sigue siendo código abierto. Los componentes de terceros y sus licencias están en
[THIRD_PARTY_NOTICES.md](../../THIRD_PARTY_NOTICES.md) y [docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **Terracotta (multijugador) usa AGPL-3.0**, más estricta que el resto: se distribuye sin modificar.
> Si lo modificas y lo redistribuyes, la sección 13 de la AGPL-3.0 te obliga a publicar el código fuente
> correspondiente.

---

**ZyNova — Context First. Less Steps.**
