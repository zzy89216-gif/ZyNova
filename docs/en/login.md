# Accounts and login

Three entries: Microsoft, offline, third-party (Authlib-Injector).

- Microsoft uses the device-code flow; no client secret or redirect URI is needed.
- Offline accounts only store a username.
- Third-party servers need a reachable HTTPS URL.

See `login-microsoft.md` and `login-errors.md`.
