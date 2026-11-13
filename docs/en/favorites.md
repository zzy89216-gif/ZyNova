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
- **Entered from "Version settings → Resources"**, the favorites page carries that instance's context: the download button on the right of a card installs the resource (plus its required dependencies) straight into that instance. Entered from the home screen resource center, it stays browse-only.

## Card appearance

Favorites cards use the same layout as the resource download list (mods, resource packs and so on): icon on the left; title, author, description and download / follow counts on the right; loader and category labels at the bottom.
