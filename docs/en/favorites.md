# Favorites and local archive

## Two different things

| | Online favorites | Local archive |
|---|---|---|
| Stores | Project ID + cached metadata | File copies (SHA-256 dedupe) |
| Location | MMKV | Private storage `files/favorites/` |
| Offline | Cache only, cannot install | Usable |
| Deleting | Removes the favorite record only | Removes the copy only, online favorite stays |

## How to favorite

- The ♥ on the right of a search result card;
- The ♥ in the top-right corner of a project page;
- Tap again to unfavorite.

## Local archive

- Entry: Favorites page → "Local archive" → import.
- Pick files through the system picker; they are copied into private storage and the originals stay untouched.
- Recognized types: worlds, modpacks, resource packs, datapacks, shaders, mods.
- Deleting an archive item removes the copy only; uninstalling the app clears the whole private directory.

## Installing

- Online favorites: project page → install to instance; dependencies are resolved automatically.
- Local archive: install to instance; files that cannot be identified online are copied as-is, so add dependencies manually.
- **The card itself has the download button**: entered from "Version settings → Resources" it installs into that instance, entered from the home screen resource center it installs into the current instance; required dependencies come along.

## Card appearance

Favorites use a **simplified card**: icon on the left, title plus author / type in the middle, and on the right a big
download button next to a small unfavorite ♥. No description, no download counts — it is obvious what to tap.
