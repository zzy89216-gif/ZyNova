# Build and release

> Full release steps (signing, Secrets, Release flow) live in `meta/HANDOFF.md` section 12;
> this page only covers the day-to-day build entry points.

## Local build

Requires JDK 17 + Android SDK/NDK.

```bash
./gradlew ZalithLauncher:assembleRelease
```

There are two flavors:

- `universal`: all 4 ABIs (the only flavor; the `arm64` variant is gone).

Output path:

```
modules/ZalithLauncher/build/outputs/apk/universal/release/ZyNova-<version>.apk
```

## CI

- Push to `main`: `.github/workflows/build_apk.yml` builds the universal APK;
- Publishing a Release: `.github/workflows/release_ci.yml` calls `build.yml` to upload the APK and mapping.

## Before a release

1. Bump the version in `modules/ZalithLauncher/gradle.properties` (name + code);
2. Sync `meta/CHANGELOG.md` / `meta/HANDOFF.md` / `README*.md` / `docs/`;
3. Privacy scan;
4. Wait for green CI;
5. Create the Release following `meta/HANDOFF.md` section 12.

> Published tags are never overwritten; a bug fix always bumps the version.
