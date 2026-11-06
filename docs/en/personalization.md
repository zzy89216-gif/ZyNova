# Personalization and the glass look

Entry: **the palette button in the top capsule**. The five most-used options used to live inside Settings → General.

| Option | Range | Default |
|---|---|---|
| Wallpaper | image / video | none |
| Background opacity | 20%–100% | 80% |
| Background blur | 0–40dp (capped at 24dp) | 0 |
| Card corner radius | 12–36dp | 28dp |
| Glass strength | 2%–16% | 8% |

“More settings” at the bottom opens Settings → General; both edit the same configuration.

## How the glass is drawn

- **One blur only**: the background is blurred once; cards, buttons and dialogs never blur anything;
- **No looping animations**, so scrolling and page transitions stay smooth on low-end devices;
- **Card material** = a vertical low-alpha gradient plus a 1px edge (white on dark, black on light).

## What to tweak

- Too flat → glass strength 12%–16%;
- Cards blend together → glass strength 2%–4%, or a smaller radius;
- Stutters on an old phone → blur 0–8dp and opacity 100% (blur is then skipped).
