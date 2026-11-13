# First launch

## 0. Region check and storage permission

1. **Region check**: unsupported regions get a block screen with an exit button only.
2. **Storage permission**: "All files access" (Android 11+) is required, otherwise public `/ZyNova`
   cannot be read or written. Without it you cannot reach the main screen; the page has a button that
   opens the system switch.

## 1. The three documents (new installs)

On first launch you get three documents in order. **Each one must be scrolled to the bottom and accepted separately** before you can continue:

1. End User License Agreement + Privacy Policy
2. Third-Party Open-Source Notices + Additional Risk Disclosure
3. Getting Started + Regions and Governing Law

- **The language follows your system language; the terms follow your region.** The same document reads differently for a user in China and a user in the United States (data-protection law, regulator, age of majority, dispute venue).
- **Upgrading does not show them again.** Re-acceptance is required only when the terms change substantively; changing the system language or time zone no longer triggers it.
- If your region is not supported yet, a block screen appears with an exit button and shows the detection basis (country / time zone / language).

## 2. Upgrading: data migration

If game data is still in private app storage, a **full-screen migration page** first moves instances and
launcher data into `/ZyNova/.Minecraft`:

- the progress bar shows the percentage and the file being copied;
- **it does not continue on its own**; tap Next when it finishes;
- closing the app mid-way is fine, it resumes next time; on failure the partial copy is cleaned up and the
  old folder is kept.

## 3. After you are in

1. Choose language and theme in Settings → Basic.
2. Install a game version from Download.
3. Keep Settings → Renderer on Auto. The list only shows renderers your device can actually run.
4. Sign in from the account page.
5. Start the instance from Home.

The first launch extracts runtimes and may take a while. Do not kill the app mid-way.
