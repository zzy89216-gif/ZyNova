# ZyNova

> **Minecraft: Java Edition · Android Launcher**
>
> An independent, unofficial project based on the open-source code of ZalithLauncher2

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Primary-Kotlin-blue)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-GPL--3.0-orange)](https://www.gnu.org/licenses/gpl-3.0.html)
[![Release](https://img.shields.io/badge/Release-26.2.5-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v26.2.5)

[简体中文](README.md) | [繁體中文](README_ZH_TW.md) | English

---

## ✦ About

**ZyNova** is an **unofficial** Minecraft: Java Edition launcher for Android, built on the
open-source code of [ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2).

> **ZyNova is NOT an official version of ZalithLauncher2.**
>
> ZyNova is an unofficial modified project based on ZalithLauncher2's open-source code.

- GitHub: <https://github.com/zzy89216-gif/ZyNova>
- Issues: <https://github.com/zzy89216-gif/ZyNova/issues>
- Discord: <https://discord.gg/QwPpZQHrTa> (permanent invite)

---

## ✨ Highlights of 26.2.5

- **Fixed** the visible frame around cards on the card home page: the card used to stack
  several layers (rounded fill + scaled graphics layer + shadow), which made the padding ring
  look darker than the content area — the card now has a single background layer, no shadow
- **New**: long-press **drag to reorder** on the card home page —
  version cards, the worlds / servers inside a card, and the three blocks of the right menu
  (account avatar / version row / launch button) can be dragged into any order,
  and the order is remembered

## ✨ Highlights of 26.2.4

Two new reports were handled in this release:

- **Fixed** the card home page ignoring the *Background Element Opacity* setting:
  the card colour was hard-coded with `cardColor(false)`, which opts out of being influenced
  by the custom background, so the cards stayed fully opaque while every other page followed the setting
- **New**: an adjustable **Card Size** for the card home page
  (Settings → Launcher → Home Page → Card Size, 70%–140%, default 100% which keeps the original look)

## ✨ Highlights of 26.2.3

This release fixes the resource download flow for **everything except mods**
(modpacks, resource packs, worlds and shaders):

- **Fixed** resource pack / shader / world searches returning almost nothing:
  the mod-loader filter was being applied to resource types that are not categorised by loader
- **Fixed** the aggregated search total page count collapsing to 0 (the UI showed "1 / 0" and could not page)
- **Fixed** the *All* platform option querying sources that do not support the selected resource type
  (worlds do not exist on Modrinth)
- **Fixed** CurseForge being completely unreachable when no API key is configured
  (the MCIM mirror is now kept as a source in that case)
- **Fixed** the dead category filter on worlds, and leftover `.zip` files when unpacking a world fails

## ✨ Highlights of 26.2.2

This release focuses on fixing issues reported by users:

- **Removed** the *⚠️ Extreme* glass level and simplified the glass effect into
  **Off / Enable Dynamic Glass** — the extreme level blurred the text layer along with the
  background, making the UI font look blurry
- **Fixed** the one-tap install occasionally failing with
  `No compatible version found for this instance`: after switching the search platform, the
  mod-loader filter was silently dropped, so results included assets for other loaders
- **Improved** install failures: messages are now localized and include the target instance's
  Minecraft version and mod loader
- **Fixed** required dependencies no longer being dropped silently — unresolved prerequisites
  are logged and reported
- **Fixed** the Discord invite links: the old temporary invite expired and was replaced with a
  permanent one

## ✨ Highlights of 26.2.1

- **Reworked**: the *⚠️ Extreme* glass level now uses a real GPU shader
  (multi-sample blur + wave-based refraction/distortion) instead of barely visible gradients
- **Fixed**: search-result cards now provide a one-tap install button
- **Fixed**: the mod loader is now selected automatically from the current instance
- **Home screen**: reorganized into **per-version modules**, each listing its own worlds and servers
- **New**: an **All** platform option that merges CurseForge and Modrinth results into one list
- **Changed**: default sort order is now **Total Downloads**

### Highlights of 26.2.0

- **Fixed**: resource installation context being lost when entering the download center
  from *Version Settings → resource management* (the `@Transient` field on a NavKey was
  silently dropped by Navigation3's saveable serialization)
- **Improved**: resource search now automatically filters by the current instance's
  Minecraft version — no need to pick the version manually
- **New**: a **⚠️ Extreme** glass level (with a performance warning before enabling)

### Highlights of 26.1.0

The goal of this release is to further move away from the legacy logic of ZalithLauncher2,
and to establish ZyNova's own resource management, download, home screen and UI foundation.

> **Context First. Less Steps.**

| Feature | Status |
|---|:---:|
| Card-style home screen (recent versions / local worlds / servers) | ✅ |
| Unified Resource Management Core | ✅ |
| Resource Providers (Modrinth / CurseForge) | ✅ |
| Unified Download Manager (queue / concurrency / resume / retry / verification) | ✅ |
| Minimal resource installation (context-aware) | ✅ |
| Glass UI with two levels (Off / Enable Dynamic Glass; the former Standard / Enhanced / Extreme levels were removed in 26.2.2) | ✅ |
| Vulkan detection and compatibility for Minecraft 26.4 Snapshot 1 | ✅ |
| Launcher updates via ZyNova's own GitHub Releases | ✅ |
| On-demand loading and performance strategy | ✅ |

### Card-style home screen

The home screen is organized into **per-version modules**: each installed version is a module
card that directly lists that version's own local worlds and saved servers.
Tapping the module card itself launches that version.

The card home screen also supports:

- **Long-press drag to reorder**: version cards, the worlds / servers inside a card,
  and the three blocks of the right menu can all be dragged into any order, and the order is remembered
- **Card size**: Settings → Launcher → Home Page → Card Size (70%–140%, default 100%)
- A single-layer card background, so no extra frame or shadow is drawn

Data is loaded on demand — the launcher does not scan everything at startup.
The default / card / custom home screen can be selected in settings.

### Unified Resource Management Core

Mods, resource packs, shaders and worlds all share a single pipeline:

> Search → Details → Version matching → File selection → Download → Verification → Install

### Resource Providers

Resource sources are decoupled from the UI. Upper layers only depend on a unified interface.
Adding another source later only requires implementing a provider — no rewrite needed.

### Unified Download Manager

One download entry point for everything: download queue, concurrency control, progress,
resume, retry, cancellation, checksum verification, temporary file cleanup, and
post-download install triggering.

### Minimal resource installation

When entering a resource page from *Version Settings → Mods*, the launcher already knows the
current instance, Minecraft version, loader and resource directory. Tapping download installs
directly, without repeatedly asking for the Minecraft version, instance or installation path.
The resource list shows an **Installed** state afterwards.

### Vulkan detection

Detection results are clearly split into **available / unavailable / check failed**, with
concrete reasons, GPU and driver information, and a manual re-check button. Detection is based
on the capabilities actually enumerated from the device, not on what the device claims to support.

---

## 📦 Build Instructions (For Developers)

> The following section is for developers who wish to contribute or build the project locally.

### Requirements

* Android Studio that supports **AGP 9.3.0** (recent stable release) —— older versions cannot open this project
* Android SDK:
  * **Minimum API level**: 26
  * **Target API level**: 34
  * **Compile SDK**: 37
* JDK 17
* Gradle **9.5.0** (the wrapper handles this automatically)

### Build Steps

```bash
git clone https://github.com/zzy89216-gif/ZyNova.git
# Open the project in Android Studio and build
```

---

## 📜 License

This project is licensed under the **[GPL-3.0 license](LICENSE)**.

### Additional Terms (Pursuant to Section 7 of the GPLv3 License)

1. When distributing a modified version of this program, you must reasonably modify the program's name or version number to distinguish it from the original version. (According to [GPLv3, 7(c)](https://github.com/ZalithLauncher/ZalithLauncher2/blob/969827b/LICENSE#L372-L374))
    - Modified versions **must not include the original program name "ZalithLauncher" or its abbreviation "ZL" in their name, nor use any name that is similar enough to cause confusion with the official name**.
    - All modified versions **must clearly indicate that they are “Unofficial Modified Versions” on the program’s startup screen or main interface**.
    - The application name of the program can be modified in [gradle.properties](./ZalithLauncher/gradle.properties).

2. You must not remove the copyright notices displayed by the program. (According to [GPLv3, 7(b)](https://github.com/ZalithLauncher/ZalithLauncher2/blob/969827b/LICENSE#L368-L370))

## Open Source Libraries and Licenses

This software uses the following open source libraries:

| Library                               | Copyright                                                                                                     | License              | Official Link                                                                      |
|---------------------------------------|---------------------------------------------------------------------------------------------------------------|----------------------|------------------------------------------------------------------------------------|
| androidx-appcompat                    | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Link↗](https://developer.android.com/jetpack/androidx/releases/appcompat)         |
| androidx-constraintlayout-compose     | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Link↗](https://developer.android.com/develop/ui/compose/layouts/constraintlayout) |
| androidx-webkit                       | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Link↗](https://developer.android.com/jetpack/androidx/releases/webkit)            |
| ANGLE                                 | Copyright 2018 The ANGLE Project Authors                                                                      | BSD 3-Clause License | [Link↗](http://angleproject.org/)                                                  |
| Apache Commons Codec                  | -                                                                                                             | Apache 2.0           | [Link↗](https://commons.apache.org/proper/commons-codec)                           |
| Apache Commons Compress               | -                                                                                                             | Apache 2.0           | [Link↗](https://commons.apache.org/proper/commons-compress)                        |
| Apache Commons IO                     | -                                                                                                             | Apache 2.0           | [Link↗](https://commons.apache.org/proper/commons-io)                              |
| ByteHook                              | Copyright © 2020-2024 ByteDance, Inc.                                                                         | MIT License          | [Link↗](https://github.com/bytedance/bhook)                                        |
| BuildKeys                             | Copyright © 2026 MovTery                                                                                      | Aoache 2.0           | [Link↗](https://github.com/MovTery/BuildKeys)                                      |
| Coil Compose                          | Copyright © 2025 Coil Contributors                                                                            | Apache 2.0           | [Link↗](https://github.com/coil-kt/coil)                                           |
| Coil Gifs                             | Copyright © 2025 Coil Contributors                                                                            | Apache 2.0           | [Link↗](https://github.com/coil-kt/coil)                                           |
| Coil SVG                              | Copyright © 2025 Coil Contributors                                                                            | Apache 2.0           | [Link↗](https://github.com/coil-kt/coil)                                           |
| Fishnet                               | Copyright © 2025 Kyant                                                                                        | Apache 2.0           | [Link↗](https://github.com/Kyant0/Fishnet)                                         |
| gl4es_extra_extra                     | Copyright © 2016-2018 Sebastien Chevalier; Copyright (c) 2013-2016 Ryan Hileman                               | MIT License          | [Link↗](https://github.com/PojavLauncherTeam/gl4es_extra_extra)                    |
| Gson                                  | Copyright © 2008 Google Inc.                                                                                  | Apache 2.0           | [Link↗](https://github.com/google/gson)                                            |
| kotlinx.coroutines                    | Copyright © 2000-2020 JetBrains s.r.o.                                                                        | Apache 2.0           | [Link↗](https://github.com/Kotlin/kotlinx.coroutines)                              |
| ktor-client-content-negotiation       | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Link↗](https://ktor.io)                                                           |
| ktor-client-core                      | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Link↗](https://ktor.io)                                                           |
| ktor-client-okhttp                    | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Link↗](https://ktor.io)                                                           |
| ktor-http                             | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Link↗](https://ktor.io)                                                           |
| ktor-serialization-kotlinx-json       | Copyright © 2000-2023 JetBrains s.r.o.                                                                        | Apache 2.0           | [Link↗](https://ktor.io)                                                           |
| LWJGL - Lightweight Java Game Library | Copyright © 2012-present Lightweight Java Game Library All rights reserved.                                   | BSD 3-Clause License | [Link↗](https://github.com/LWJGL/lwjgl3)                                           |
| material-color-utilities              | Copyright 2021 Google LLC                                                                                     | Apache 2.0           | [Link↗](https://github.com/material-foundation/material-color-utilities)           |
| Maven Artifact                        | Copyright © The Apache Software Foundation                                                                    | Apache 2.0           | [Link↗](https://github.com/apache/maven/tree/maven-3.9.9/maven-artifact)           |
| Media3                                | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Link↗](https://developer.android.com/jetpack/androidx/releases/media3)            |
| Mesa                                  | Copyright © The Mesa Authors                                                                                  | MIT License          | [Link↗](https://mesa3d.org/)                                                       |
| MMKV                                  | Copyright © 2018 THL A29 Limited, a Tencent company.                                                          | BSD 3-Clause License | [Link↗](https://github.com/Tencent/MMKV)                                           |
| Navigation 3                          | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Link↗](https://developer.android.com/jetpack/androidx/releases/navigation3)       |
| NG-GL4ES                              | Copyright © 2016-2018 Sebastien Chevalier; Copyright © 2013-2016 Ryan Hileman; Copyright (c) 2025-2026 BZLZHH | MIT License          | [Link↗](https://github.com/BZLZHH/NG-GL4ES)                                        |
| OkHttp                                | Copyright © 2019 Square, Inc.                                                                                 | Apache 2.0           | [Link↗](https://github.com/square/okhttp)                                          |
| Okio                                  | Copyright © 2013 Square, Inc.                                                                                 | Apache 2.0           | [Link↗](https://square.github.io/okio/)                                            |
| OpenNBT                               | Copyright © 2013-2021 Steveice10.                                                                             | MIT License          | [Link↗](https://github.com/GeyserMC/OpenNBT)                                       |
| Process Phoenix                       | Copyright © 2015 Jake Wharton                                                                                 | Apache 2.0           | [Link↗](https://github.com/JakeWharton/ProcessPhoenix)                             |
| proxy-client-android                  | -                                                                                                             | LGPL-3.0 License     | [Link↗](https://github.com/TouchController/TouchController)                        |
| Reorderable                           | Copyright © 2023 Calvin Liang                                                                                 | Apache 2.0           | [Link↗](https://github.com/Calvin-LL/Reorderable)                                  |
| sdl2-compat                           | Copyright (C) 2026 Sam Lantinga <slouken@libsdl.org>                                                          | Zlib License         | [Link↗](https://github.com/libsdl-org/sdl2-compat)                                 |
| SDL3                                  | Copyright (C) 1997-2026 Sam Lantinga <slouken@libsdl.org>                                                     | Zlib License         | [Link↗](https://github.com/libsdl-org/SDL)                                         |
| skinview3d                            | Copyright © 2014-2018 Kent Rasmussen; Copyright © 2017-2022 Haowei Wen, Sean Boult and contributors           | MIT License          | [Link↗](https://github.com/bs-community/skinview3d)                                |
| sora-editor                           | Copyright (C) 2020-2026  Rosemoe                                                                              | LGPL-2.1 License     | [Link↗](https://github.com/Rosemoe/sora-editor)                                    |
| StringFog                             | Copyright © 2016-2023, Megatron King                                                                          | Apache 2.0           | [Link↗](https://github.com/MegatronKing/StringFog)                                 |
| tm4e (TextMate for Eclipse)           | Copyright © Eclipse Foundation                                                                                | EPL-2.0 License      | [Link↗](https://github.com/eclipse-tm4e/tm4e)                                      |
| XZ for Java                           | Copyright © The XZ for Java authors and contributors                                                          | 0BSD License         | [Link↗](https://tukaani.org/xz/java.html)                                          |
