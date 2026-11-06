# Licenses & third-party notices

ZyNova is open source. **You, your friends and anyone who gets the APK may read, modify and redistribute it** —
which is exactly why this page has to be explicit about whose code is whose, under which licence,
and what you are allowed to do with it.

## 1. The project itself

| | |
|---|---|
| Licence | **GNU GPL-3.0** |
| Full text | [`LICENSE`](https://github.com/zzy89216-gif/ZyNova/blob/main/LICENSE) in the repo root, also readable in-app under Settings → About → Open-source licences |
| Origin | Maintained from [ZalithLauncher2](https://github.com/ZalithLauncher/ZalithLauncher2); ZyNova is an **unofficial modified build** |
| Upstream credits | The copyright notices of Zalith Launcher 2 / Fold Craft Launcher / PojavLauncher are **kept intact** in `LICENSE` and in each source file header |

What GPL-3.0 means in plain words:

- Use it, change it, hand it to others — freely;
- But **whatever you distribute must also be open source** (GPL-3.0) and must keep the licence and copyright notices;
- You **cannot** turn it into a closed-source product and sell it.

## 2. Third-party components

The complete list with per-component copyright is in
[THIRD_PARTY_NOTICES.md](https://github.com/zzy89216-gif/ZyNova/blob/main/THIRD_PARTY_NOTICES.md);
in-app, Settings → About → Open-source licences lets you open each licence text.

The ones that matter most:

| Component | Licence | Notes |
|---|---|---|
| **MobileGlues** (renderer) | LGPL-2.1 | Shipped as a separate `.so`, unmodified; replaceable with your own build |
| **GL4ES** (renderer) | MIT | Unmodified |
| **LTW-Turbo** (renderer, experimental) | LGPL-3.0 | Unmodified; arm64-v8a only |
| **frpc** (SakuraFrp client) | Apache-2.0 | SakuraFrp build, unmodified; bundles no accounts or keys |
| **Terracotta** (multiplayer) | **AGPL-3.0** ⚠️ | See the next section — the strictest licence here |

## 3. Terracotta is AGPL-3.0 (the one to be careful about)

`libterracotta.so` is licensed under **AGPL-3.0**, which goes further than GPL-3.0:

- Distribution (including after modification) must include the licence text and copyright notice — already bundled and readable in-app;
- **If you modify it**, redistribution requires **publishing your modified source**;
- Even **providing its functionality over a network** triggers the source-offer obligation.

How this project handles it: the binary is **shipped as-is, with no modification**, together with
the upstream interface code only. So "the corresponding source" is simply the upstream repository:
<https://github.com/burningtnt/Terracotta>. You do not need anything from the ZyNova team — just comply with AGPL-3.0.

## 4. What you may do

| What you want | Allowed? |
|---|---|
| Install and play it yourself | ✅ Yes, freely |
| Modify it for yourself | ✅ Yes, freely |
| Send your modified build to friends | ✅ Yes, but ship the source and licence with it |
| Bundle it into your own modpack / distribution | ✅ Yes, same obligations: keep it open and keep the credits |
| Charge for it / add ads | ⚠️ You may charge, but **the code must stay open** and users must keep the right to redistribute |
| Closed-source commercial use | ❌ Not allowed |

## 5. Credits and trademarks

- Minecraft is a trademark of Mojang Synergies AB. This project is **not affiliated** with Mojang or Microsoft.
- This project is **not affiliated** with, and not endorsed by, ZalithLauncher, Fold Craft Launcher,
  PojavLauncher, MobileGlues, LTW-Turbo, Terracotta or SakuraFrp.
- All third-party credits are preserved; none were removed or replaced.

## 6. Reporting a licensing problem

If you believe a component here is handled incorrectly (missing credit, licence mismatch, unclear binary
provenance), please open an issue: <https://github.com/zzy89216-gif/ZyNova/issues>.
Such reports take priority over feature requests.
