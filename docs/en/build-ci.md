# CI build

- Push to main: `build_apk.yml` builds universal + arm64.
- Release: `release_ci.yml` calls `build.yml` and uploads APKs + mapping.
- Artifact path is `apk/<flavor>/release/`, not the old `apk/release/`.
