# Log upload

- Logs live in public `/ZyNova/logs/` (`launcher_crash.log` holds the last crash) and are easy to find in
  any file manager.
- Nothing leaves the device unless you tap "Share link".
- Skim for tokens, cookies or server passwords before uploading: the launcher redacts common key formats
  (`sk-...`, `Bearer ...`, `password=...` and so on), but do not rely on it.
- Crash analysis happens on-device and uploads nothing.
