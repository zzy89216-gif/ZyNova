<p align="center">
  <img src="assets/icon.png" width="180" alt="ZyNova">
</p>

# ZyNova

> **Minecraft: Java Edition · Android Launcher**
>
> An independent, unofficial project based on the open-source code of ZalithLauncher2

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Primary-Kotlin-blue)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-GPL--3.0-orange)](https://www.gnu.org/licenses/gpl-3.0.html)
[![Release](https://img.shields.io/badge/Release-27.1.2-purple)](https://github.com/zzy89216-gif/ZyNova/releases/tag/v27.1.2)

**[English](README.md)** | [简体中文](README_ZH_CN.md) | [繁體中文](README_ZH_TW.md) | [日本語](README_JA_JP.md)

---

## ✦ About

**ZyNova** is an **unofficial** Minecraft: Java Edition launcher for Android, built on the
open-source code of [ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2).

> **ZyNova is NOT an official version of ZalithLauncher2.**
>
> ZyNova is an unofficial modified project based on ZalithLauncher2's open-source code.
>
> It ships with a **built-in AI Agent** that can really read your logs, manage mods, edit
> configs, install resources and launch the game for you — not just give advice.

- GitHub: <https://github.com/zzy89216-gif/ZyNova>
- Issues: <https://github.com/zzy89216-gif/ZyNova/issues>
- Discord: <https://discord.gg/QwPpZQHrTa> (permanent invite)

---

## ✨ Highlights of 27.1.2

- **The upstream Client ID is gone — including from the Git history.** At the upstream
  ZalithLauncher2 project's request, their Microsoft application registration ID no longer appears
  anywhere in this repository, and the history has been rewritten to purge it (3 commits removed;
  a full object scan now returns zero hits). **All commit hashes and tags have therefore changed**,
  so old clones must be fetched again.
- **AI: you can now see where your data goes.** The first time you open the AI settings, the app
  explains that game logs, mod lists, configuration contents and the text you type are sent to the
  provider you configured, that ZyNova neither stores nor relays them, and that your API key stays
  on this device.
- **AI: operation audit log.** Every write operation the agent performs is recorded locally
  (time, tool, risk, arguments, result) and can be reviewed or exported from the AI settings.
- **The agent permission mode now defaults to "confirm each operation"**, so a brand-new user is
  asked before the AI changes settings, deletes mods, writes files or installs resources.
- **The OpenGL version selector is back** for Ironized Zink (4.6 / 4.5 / 4.3 / 3.3). All four
  official presets use 4.6, and devices whose drivers translate Vulkan to Zink 4.6 imperfectly had
  no way to lower it. The other twelve low-level parameters stay hidden.

> 📜 Full version history: [CHANGELOG.md](CHANGELOG.md)

## ℹ️ Which Microsoft application is used for sign-in
**ZyNova is an unofficial fork of ZalithLauncher2 — they are different projects, maintained
independently.**

| | Display name | Client ID | Mojang allow list | Used by this build |
|---|---|---|---|---|
| **ZyNova's own** | ZyNova Launcher | `7b66e168-f8cd-43fc-a52d-2e78dba189b0` | ✅ **approved (2026-09-30)** | ✅ yes |
| **ZalithLauncher2's** | ZalithLauncher (upstream) | (removed) | ✅ approved | ❌ no (used by the 26.4.0 / 26.4.1 builds) |

- Only a registration that is on **Minecraft's application allow list** can complete premium
  sign-in; otherwise `POST /authentication/login_with_xbox` answers
  `403 Invalid app registration`, no matter how OAuth, Xbox Live and XSTS went.
- ✅ **ZyNova's own registration is on that allow list** — the Mojang Enforcement AppID review was
  completed on **2026-09-30**. Premium sign-in works with the current builds, and ZyNova no longer
  borrows the upstream project's registration.
- Still required: **public client + Client ID only** — no Client Secret, no redirect URI, no SHA-1.
  The ID is injected at build time through the repository secret `OAUTH_CLIENT_ID`; it is never
  hard-coded in the source.

## 🛠️ Features

### AI Agent

A new **AI** button sits next to the file entry in the top bar and opens the chat screen
directly — there is no separate AI home page. Chat and Agent are the **same entry point**:
ask *"why won't this instance launch?"* and it goes and reads the logs; then say *"fix it for me"*
and it **calls tools and actually does it**, instead of writing you a tutorial.

- **25 tools**, all built on the launcher's existing systems: instances, real mod metadata,
  enabling / disabling / deleting mods, resource packs, shaders, saves, files, logs, crash reports,
  **117 launcher settings**, resource search and install (including recursive installation of
  required dependencies), and **actually launching the game**
- **Conversations are saved on this device**, and a **sidebar** (top-left) lets you review, switch,
  delete or start a new conversation
- **Operation audit log**: every write operation is recorded locally and can be reviewed or
  exported from the AI settings
- **No tool-call limit** — the agent keeps working until it decides it is done; you can stop it at
  any time
- **Its own settings screen** (top-right of the chat), separate from the launcher settings:
  provider (OpenAI / Anthropic, extensible), your own API key (stored on this device only),
  a **dynamically fetched model list — no model name is hard-coded**, a customisable base URL, and
  a permission mode (**confirm each operation** by default, or full control)
- **Safety**: file tools are confined to the game and launcher data directories; sensitive setting
  keys are refused; if the confirmation UI is unavailable, write operations are denied

### Renderers

Since 26.3.0 the launcher ships **exactly three built-in renderers**:

| Renderer | What it is | Configuration |
|---|---|---|
| **Ironized Zink** (default) | Desktop OpenGL 4.6 translated to Vulkan (Mesa Zink + Kopper), by GoyDevv | **4 official presets** + OpenGL version |
| **GL4ES** | Classic OpenGL translation layer | Kept at its defaults |
| **MobileGlues** | Translates desktop OpenGL onto the device's OpenGL ES 3.x | Kept at upstream defaults |

**The 4 official Ironized Zink presets** (values identical to upstream):

| Preset | Best for | Minecraft | Shaders |
|---|---|---|---|
| **Potato** | Absolute maximum FPS | 1.8 → latest (incl. 26.x) | Not recommended |
| **Performance** | High FPS with Sodium | 1.20.x → latest | Light |
| **Default** | Balanced Zink + shaders (**default**) | 1.16.x → latest | Full (Iris / OptiFine) |
| **Max Compatibility** | Run everything | All versions | Full + heavy packs |

Since **26.4.0** the panel exposes **the 4 presets** plus an **OpenGL version** dropdown below
them. The 12 individual parameter switches were removed so that ordinary users do not have to
tune low-level parameters. Picking a preset still writes the whole parameter set at once.

> ℹ️ All four presets pin the OpenGL version to **4.6**. If your driver does not translate
> Vulkan to Zink 4.6 correctly (broken picture / refuses to start), lower it to 4.5 / 4.3 / 3.3
> in the **OpenGL version** dropdown below the presets — that dropdown was removed in 26.4.0 and
> **restored in 27.1.2**. Changing it only affects the OpenGL version, not the other presets.

Selecting Ironized Zink in **Settings → Renderer** immediately expands the preset panel right below it.

> **External renderers are unaffected**: renderer plugins (FCL / Zalith renderer plugins
> and the newer `fclPlugin_V2` plugins) can still add more renderers to the list.
> Only three are *built in* — you can still install as many plugins as you like.

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

> **Licenses differ per component.** Do not assume the whole project is MIT just because one
> dependency is: ZyNova itself is GPL-3.0, Ironized Zink is GPL-3.0, MobileGlues is LGPL-2.1,
> and GL4ES is MIT. The rendering engine Ironized Zink ships is **Mesa** — its core and Gallium
> code (including the **Zink** driver) are MIT, the GLX client code is under the
> SGI Free Software License B, and the GL / GLX extension headers are under the Khronos license.
> **Kopper** is also part of the Mesa code base, so the Mesa terms apply to it.
> See **[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)** for the full per-component breakdown,
> copyright notices and the source availability statement for the redistributed binaries.

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
| BuildKeys                             | Copyright © 2026 MovTery                                                                                      | Apache 2.0           | [Link↗](https://github.com/MovTery/BuildKeys)                                      |
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
| Ironized Zink                         | Copyright © GoyDevv (rendering engine: Mesa Zink / Kopper, © The Mesa Authors)                                 | GPL-3.0              | [Link↗](https://github.com/GoyDevv/IronizedZink)                                   |
| Mesa core / Gallium (incl. Zink)      | Copyright © 1999-2007 Brian Paul and the Mesa contributors                                                    | MIT                  | [Link↗](https://mesa3d.org/)                                                       |
| Mesa GLX client code                  | Copyright © 1999-2007 Brian Paul and the Mesa contributors                                                    | SGI Free Software License B | [Link↗](https://mesa3d.org/)                                                 |
| Mesa GL / GLX extension headers       | Copyright © The Khronos Group                                                                                 | Khronos              | [Link↗](https://www.khronos.org/)                                                  |
| Kopper                                | Part of the Mesa code base (Vulkan WSI layer)                                                                 | Covered by the Mesa terms above | [Link↗](https://mesa3d.org/)                                            |
| MMKV                                  | Copyright © 2018 THL A29 Limited, a Tencent company.                                                          | BSD 3-Clause License | [Link↗](https://github.com/Tencent/MMKV)                                           |
| Navigation 3                          | Copyright © The Android Open Source Project                                                                   | Apache 2.0           | [Link↗](https://developer.android.com/jetpack/androidx/releases/navigation3)       |
| MobileGlues                           | Copyright (c) 2025-2026 MobileGL-Dev                                                                          | LGPL-2.1             | [Link↗](https://github.com/MobileGL-Dev/MobileGlues)                               |
| OkHttp                                | Copyright © 2019 Square, Inc.                                                                                 | Apache 2.0           | [Link↗](https://github.com/square/okhttp)                                          |
| Okio                                  | Copyright © 2013 Square, Inc.                                                                                 | Apache 2.0           | [Link↗](https://github.com/square/okio)                                            |
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
