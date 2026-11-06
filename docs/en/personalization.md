# Personalization and the glass look

Since 27.10.2 the visual options moved out of *Settings → General* into a single page:
**palette button in the top capsule → Personalization**.

The page only holds the five most-used options; everything else stays in Settings → General.
Both places read and write the **same configuration**, so they can never disagree.

## What is on the page

| Option | Range | Default | Notes |
|---|---|---|---|
| Wallpaper | image / video | none | The picker itself still lives in Settings → General (it needs the system picker) |
| Background opacity | 20%–100% | 80% | At 100% the background is fully opaque, so **blur is skipped** to save performance |
| Background blur | 0–40dp | 0 | Effective radius is capped at 24dp; devices with ≤ 4 cores drop to 12dp |
| Card corner radius | 12–36dp | 28dp | Drives every rounded corner (panels ×1.36, input fields ×0.54, …) |
| Glass strength | 2%–16% | 8% | Density of the single-color gradient on cards; lower means flatter |

At the bottom, **More settings →** jumps to Settings → General.

## How the glass is drawn

The concept of “dynamic glass” is gone as of 27.10.2:

- **One blur only**: the background is blurred once in `Background()`. Cards, buttons and dialogs never blur anything themselves.
- **No looping animations**: the diagonal highlight sweep and the home-button sheen are removed.
- **Card material** = a vertical low-alpha gradient plus a 1px edge. Dark theme overlays white, light theme overlays black, so the look stays monochrome on any wallpaper.

As a result, scrolling, dragging and page transitions **never rebuild the background**, and dialogs no longer stack a second blur on low-end devices.

## Which option to tweak

- Interface feels too flat → raise *Glass strength* to 12%–16%.
- Cards blend into each other → lower *Glass strength* to 2%–4%, or reduce *Corner radius*.
- Old phone stutters → set *Background blur* to 0–8dp and opacity to 100% (blur is then skipped entirely).
- Pure, minimal look → glass strength 2% + radius 12dp.

## Related

- Button hierarchy: primary buttons sit 2–3% deeper than the card (dark theme ×1.05 / light theme ×0.96) with a 1px edge, so “Launch game” and “Create world” stay visually separate.
- Settings → General still has wallpaper picking, background video volume, theme colour, dark mode, language and full screen.
