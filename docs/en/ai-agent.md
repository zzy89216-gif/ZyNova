# AI Agent

## What it is

Not a chat box: it is an agent that calls launcher tools directly —
installing the game itself / modpacks / resources, editing instance settings, reading and writing `options.txt`, querying favorites, launching the game and more.

## Configuration

1. Settings → AI settings.
2. Enter a DeepSeek API key (the default provider; the base URL can be changed).
3. Tap "Fetch model list" and pick a model.
4. Choose a permission mode:
   - **Full control** (default): tools run directly, no step-by-step prompts;
   - **Confirm operations**: asks before every write.

## Message rendering

- Assistant replies render **Markdown**: headings, lists, tables, code blocks, quotes and links all display properly;
- Code blocks get their own block and scroll horizontally, so long commands do not stretch the page;
- User messages are right-aligned and adapt their width (up to ~82% of the screen), never a card across the full width;
- During streaming the body keeps updating without re-parse jank while scrolling.

## What it can do

- Install games / modpacks / mods / resource packs / shaders / worlds;
- Change an instance's renderer, Java, memory, JVM arguments and so on;
- Read instance config and `options.txt`;
- Query favorites and remove them;
- Launch the game and read logs (within tool limits).

## Safety boundaries

- Permission mode, loop detection, the stop button and the write audit log are always kept.
- Do not let it run instructions from unknown sources; never paste your API key into the chat.
- When it deletes or overwrites something, double-check the target it reported.
