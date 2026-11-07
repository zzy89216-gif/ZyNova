# Accounts and login

## Three entry points

1. **Microsoft account**: device-code flow; no client secret and no redirect URI needed.
2. **Offline account**: only a username; used for offline play and some servers.
3. **Third-party auth**: Authlib-Injector server URL + account.

## Microsoft login flow

1. Accounts page → add account → Microsoft.
2. The launcher shows a device code and a verification link.
3. Open the link in a browser, enter the code, finish authorization.
4. Return to the launcher and wait for polling to fetch the token.

> Polling is no longer aborted by 400 responses;
> `authorization_pending` / `slow_down` keep waiting, only a real 4xx fails immediately.

## Common errors

| Symptom | What to do |
|---|---|
| Spinner never ends | Make sure authorization finished in the browser; Microsoft must be reachable; try another network |
| `invalid_grant` | Refresh token expired or revoked; delete the account and sign in again |
| `XErr 2148916233` | The Microsoft account has no Xbox profile; create one on the Xbox site first |
| Region / age restriction | Follow Microsoft's instructions; the launcher cannot bypass it |
| Third-party auth fails | Check the server URL starts with `https://` and is reachable |

## Security

- The launcher stores tokens only, never your Microsoft password.
- Never share account tokens or the Authorization header from logs.
