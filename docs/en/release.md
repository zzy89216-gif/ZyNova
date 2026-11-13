# Publishing a release

The full procedure (and its pitfalls) lives in `AGENTS.md`, section 4:

1. Only edit `ZalithLauncher/gradle.properties`: `launcher_version_name` (the in-APK version) and
   `launcher_version_code`;
2. Sync the docs: `CHANGELOG.md`, `AGENTS.md`, `HANDOFF.md`, `docs/` (zh and en pairs),
   `README.md` / `README_EN.md`;
3. Wait for the push build to go green (docs-only pushes do not trigger a build);
4. Create the Release (`target_commitish` must be the **full 40-character SHA**; tick `prerelease` for a
   beta), which triggers the release build and uploads the assets automatically;
5. Verify both assets are `uploaded`: `ZyNova-<version>.apk` (universal, the only APK) and
   `mapping.universal.zip`.

- **The release name may carry a suffix; the APK filename only takes the numeric part**: version name
  `X.Y.Z` without a suffix, release title `X.Y.Z (beta)`;
- A published tag is never overwritten or rewritten; ship a bug fix by bumping the version;
- Always wait for the running build of an old release to finish before deleting it, otherwise the release
  workflow recreates it;
- **Turning a pre-release into the stable release**: wait for the new push build to go green → delete the old
  pre-release (both the Release and the tag) → create the stable Release from the new build's full SHA with
  `prerelease` unticked. Do not delete the old one first, or there is a window with no installable release.
