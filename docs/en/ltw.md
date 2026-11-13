# LTW (experimental)

- arm64-v8a only; it never shows up on other devices.
- Upstream LTW-Turbo is still early; crashes and broken frames are possible.
- Auto mode never picks it; use it for manual testing only.
- **Its options are not expanded on the renderer list page**: after selecting LTW, tap the renderer
  card to open the dedicated "LTW advanced settings" page. The list page keeps only status and the jump,
  so the same switches are not laid out twice.
- The page only exposes environment variables the native side actually reads.
- If it breaks, switch back to MobileGlues / GL4ES first.
