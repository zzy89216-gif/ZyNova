# Build and release

> Full release steps (signing, Secrets, Release flow) live in `HANDOFF.md` section 12;
> this page only covers the day-to-day build entry points.

## Local build

Requires JDK 17 + Android SDK/NDK.

```bash
./gradlew ZalithLauncher:assembleRelease
```

There are two flavors:

- `universal`: all 4 ABIs;
- `arm64`: arm64-v8a only.

Output paths:

```
ZalithLauncher/build/outputs/apk/universal/release/ZyNova-<version>.apk
ZalithLauncher/build/outputs/apk/arm64/release/ZyNova-<version>-arm64-v8a.apk
```

## CI

- Push to `main`: `.github/workflows/build_apk.yml` builds both APKs;
- Publishing a Release: `.github/workflows/release_ci.yml` calls `build.yml` to upload the APKs and mapping.

## Before a release

1. Bump the version in `ZalithLauncher/gradle.properties` (name + code);
2. Sync `CHANGELOG.md` / `HANDOFF.md` / `README*.md` / `docs/`;
3. Privacy scan;
4. Wait for green CI;
5. Create the Release following `HANDOFF.md` section 12.

> Published tags are never overwritten; a bug fix always bumps the version.
