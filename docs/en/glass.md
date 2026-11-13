# Glass material

> The “dynamic glass” toggle is gone: glass is now a global base material, not an optional effect.
> For the full set of options, see [Personalization and the glass look](https://github.com/zzy89216-gif/ZyNova/blob/main/docs/en/personalization.md).

- There is exactly **one** background blur in the whole app: the background layer is blurred once, cards / buttons / dialogs never blur anything themselves.
- Card material = a vertical low-alpha gradient plus a 1px edge; white on dark themes, black on light themes.
- No looping animations: scrolling, dragging and page transitions never rebuild the background.
- *Glass strength* lives on the Personalization page (or Settings → General), 2%–16%, default 8%.
- If it stutters or heats up: drop *Background blur* to 0–8dp, or set *Background opacity* to 100% (blur is then skipped entirely).
