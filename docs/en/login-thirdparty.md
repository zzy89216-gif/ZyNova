# Third-party authentication

- You need an Authlib-Injector server address (usually `https://...`).
- The address must be reachable and have a valid certificate.
- If sign-in fails, check the server first.

## LittleSkin quick sign-in

Accounts, then Add account, then **LittleSkin quick sign-in** in the login menu:

- One tap opens the username/password dialog; no need to look up the server address
  (it is built in as `https://littleskin.cn/api/yggdrasil`);
- The first use also adds LittleSkin to the auth server list, so you can pick it there later;
- The register button opens the LittleSkin registration page;
- After signing in you can change skins and capes exactly like any third-party account.

> Offline, third-party and LittleSkin entries only appear on a Chinese UI; other languages keep Microsoft
> sign-in only. Switching the language only hides entries and **never deletes accounts**.
