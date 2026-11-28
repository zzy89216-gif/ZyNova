<p align="center"><sub>
<a href="../../README_EN.md">English</a> · <a href="../../README.md">简体中文</a> · <a href="../zh-Hant/README.md">繁體中文</a> · <a href="../ja/README.md">日本語</a> · <a href="../ko/README.md">한국어</a> · <a href="../tr/README.md">Türkçe</a> · <a href="../ru/README.md">Русский</a> · <a href="../vi/README.md">Tiếng Việt</a> · <a href="../th/README.md">ไทย</a> · <a href="../de/README.md">Deutsch</a> · <b>Español</b> · <a href="../fr/README.md">Français</a>
<br>
<a href="../uk/README.md">Українська</a> · <a href="../pl/README.md">Polski</a> · <a href="../pt-BR/README.md">Português (Brasil)</a> · <a href="../ar/README.md">العربية</a> · <a href="../fa/README.md">فارسی</a> · <a href="../id/README.md">Bahasa Indonesia</a> · <a href="../it/README.md">Italiano</a> · <a href="../el/README.md">Ελληνικά</a> · <a href="../ml/README.md">മലയാളം</a> · <a href="../ur-Latn/README.md">Roman Urdu</a> · <a href="../ro/README.md">Română</a> · <a href="../bg/README.md">Български</a> · <a href="../lzh/README.md">文言</a>
</sub></p>

<p align="center">
  <img src="../../docs/assets/icon.png" width="160" alt="ZyNova">
</p>

# ZyNova

> 📌 **Current release: 28.4.0** · [Changelog](../../meta/CHANGELOG.md) · [Releases](https://github.com/zzy89216-gif/ZyNova/releases/latest)

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Release](https://img.shields.io/badge/Release-28.4.0-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v28.4.0)

---

Un lanzador de **Minecraft: Java Edition** para Android, mantenido de forma independiente a partir del código de ZalithLauncher2. No es una compilación oficial.

La idea: **menos toques**. En lugar de soltarte veinte ajustes, el lanzador decide primero y solo pregunta cuando de verdad no puede decidir.

## ✨ Cambios recientes

**28.4.0** (última): cambios principales:

- Inicio rediseñado: mundos recientes + lista completa de mundos + contenido personalizado.
- Se elimina la página «Personalización», se corrige el título de detalles del renderizador y se actualiza el enlace de Discord.
- Eliminar una versión ahora muestra un progreso determinado a pantalla completa.
- Se eliminan las tarjetas antiguas de mundos agrupadas por versión del centro del inicio (#100).
- Corregida la detección errónea del estado vacío: con todas las versiones ocultas ya no se sugiere descargar.
- Revertidos los cambios de 28.3.2 que provocaban cierres en algunas pantallas de gestión de recursos (caché de listas / caché global de iconos / carga asíncrona de plugins); solo se mantiene la corrección de pantalla completa al eliminar una versión.

> La apariencia sigue igual que en 28.3.0: sin vidrio dinámico, refracción ni efectos magnéticos.

Novedades principales de la versión **28.3.0** (versión estable):

Esta versión estable reúne todo el contenido de las versiones preliminares anteriores (esas versiones se han retirado).

- **Varias descargas e instalaciones a la vez**: las versiones del juego, los modpacks y los recursos pueden iniciarse en paralelo; cada tarea tiene su propio progreso, velocidad y cancelación. Si vuelve a pulsar el mismo objetivo, ahora aparece un aviso en lugar de no hacer nada.
- **Reordenar las tarjetas de inicio con más estabilidad**: arrastrar con pulsación larga ya no parpadea ni provoca cierres; al arrastrar rápido las tarjetas se intercambian una a una y también se admite el arrastre horizontal.
- **Reproductor de música mejorado**: carga automática de letras `.lrc` con el mismo nombre e importación desde la página; la notificación muestra la línea anterior y la actual. Se añade una mini barra en la barra superior (título + reproducir/pausar + cambiar de pista). La posición y la duración se muestran correctamente en pausa, y la interfaz ya no se desborda en horizontal ni en pantallas bajas.
- **«Volver al juego» corregido**: la entrada desaparece en cuanto se cierra el juego y pulsarla justo antes ya no provoca el cierre.
- **IA ampliada**: se pueden instalar varias versiones del juego a la vez; nuevas herramientas de control de música (53 herramientas en total).
- **Nuevo acceso «Multijugador» en el menú del juego**: el menú se cierra, el launcher abre directamente la página Multijugador y el juego sigue ejecutándose en segundo plano.

Cambios completos: [CHANGELOG.md](../../meta/CHANGELOG.md).

## 🛠️ Funciones

- **Agente de IA**: DeepSeek por defecto — instala el juego, modpacks y mods, cambia ajustes, lee registros y lanza el juego
- **Renderizadores**: Auto / MobileGlues / Ironized Zink / GL4ES / LTW (experimental)
- **Núcleo de recursos**: búsqueda en Modrinth / CurseForge, resolución de dependencias, descarga, verificación e instalación
- **Favoritos y archivo**: favoritos en línea más archivo privado con deduplicación SHA-256
- **Protección de la API de Fabric**: Fabric API / Quilted Fabric API / Legacy Fabric API los añade el instalador; no se pueden duplicar, buscar ni borrar

## 📖 Glosario rápido

- **Modpack**: colección de mods que se instala como un bloque
- **Shader**: iluminación y agua mejoradas; se activan en los ajustes gráficos
- **Instancia**: carpeta propia con su versión, sus mods y sus ajustes
- **Renderizador**: capa que dibuja el juego (MobileGlues / Ironized Zink / GL4ES / LTW)

## 📦 Descarga y compilación

- Descarga: <https://github.com/zzy89216-gif/ZyNova/releases> (siempre la última versión)
- Compilar por tu cuenta: JDK 17 + Android SDK / NDK y luego `./gradlew ZalithLauncher:assembleRelease`
- Aviso: el flujo de cumplimiento aparece en el primer arranque; detalles en [docs/en/licenses.md](../../docs/en/licenses.md)

## 📚 Documentación

> 📚 La documentación existe en chino e inglés (78 artículos cada uno); esta página es la introducción en español.

| Contenido | Archivo |
|---|---|
| Índice de la documentación | [docs/en/README.md](../../docs/en/README.md) |
| Instalación / uso / exportación | [docs/en/install.md](../../docs/en/install.md) · [docs/en/usage.md](../../docs/en/usage.md) |
| Renderizado / inicio de sesión / IA | [docs/en/renderers.md](../../docs/en/renderers.md) · [docs/en/login.md](../../docs/en/login.md) · [docs/en/ai-agent.md](../../docs/en/ai-agent.md) |
| Problemas / FAQ / datos y privacidad | [docs/en/troubleshooting.md](../../docs/en/troubleshooting.md) · [docs/en/faq.md](../../docs/en/faq.md) · [docs/en/data-and-privacy.md](../../docs/en/data-and-privacy.md) |
| Compilación / registro de cambios | [docs/en/build.md](../../docs/en/build.md) · [CHANGELOG.md](../../meta/CHANGELOG.md) |
| Traspaso / licencias de terceros / licencia | [HANDOFF.md](../../meta/HANDOFF.md) · [THIRD_PARTY_NOTICES.md](../../meta/THIRD_PARTY_NOTICES.md) · [LICENSE](../../LICENSE) |

## 💖 Apoyo

- Reportar fallos：<https://github.com/zzy89216-gif/ZyNova/issues>
- Discord：<https://discord.gg/dbHTApYNYJ>
- Correo：<ZyNovaLauncher@outlook.com>
- X oficial：<https://x.com/ZyNovaProject>
- Apoyar el proyecto：<https://afdian.com/a/hizzy>

## ⚖️ Licencia

GPL-3.0: el proyecto sigue siendo código abierto. Los componentes de terceros y sus licencias están en
[THIRD_PARTY_NOTICES.md](../../meta/THIRD_PARTY_NOTICES.md) y [docs/en/licenses.md](../../docs/en/licenses.md).

> ⚠️ **Terracotta (multijugador) usa AGPL-3.0**, más estricta que el resto: se distribuye sin modificar.
> Si lo modificas y lo redistribuyes, la sección 13 de la AGPL-3.0 te obliga a publicar el código fuente
> correspondiente.

---

**ZyNova — Context First. Less Steps.**
