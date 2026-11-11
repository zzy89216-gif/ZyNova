# Which APK to download

There is only one package now — no choice to make:

| File | Devices | Notes |
|---|---|---|
| `ZyNova-<version>.apk` | All Android devices | Universal, all ABIs (arm64-v8a / armeabi-v7a / x86 / x86_64) |

The old `-arm64-v8a` slim build has been dropped: it added no features, only saved a little size,
while forcing everyone to check their device first. Releases now ship a single universal APK.

- The LTW-Turbo renderer ships arm64-only libraries and **still works in the universal build on arm64 phones**;
  other architectures simply do not see the option.
- Each release also ships one obfuscation mapping, `mapping.universal.zip`, for de-obfuscating crash stacks. Regular users do not need it.
