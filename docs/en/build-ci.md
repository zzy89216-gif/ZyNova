# CI build

- Push to main: `build_apk.yml` builds the **universal** APK (the only flavor).
- Docs-only pushes **do not trigger a build**: `paths-ignore` covers `**.md`, `docs/**`, `LICENSE` and the issue templates;
  run it by hand via Actions → **Build ZyNova APK** → *Run workflow*.
- Release: `release_ci.yml` calls `build.yml` and uploads `ZyNova-<version>.apk` and `mapping.universal.zip`.
- Artifact path is `apk/<flavor>/release/`, not the old `apk/release/`.
