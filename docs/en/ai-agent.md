# AI Agent

Not a chatbot: it can call launcher tools to install games, modpacks and resources, edit instance settings, read/write `options.txt`, manage favorites and launch the game.

## Message rendering

Assistant replies are rendered as **Markdown** — headings, lists, tables, code blocks,
quotes and links all display properly. Code blocks scroll horizontally instead of
stretching the page. User messages are right-aligned and adapt their width (up to ~82%
of the screen). Streaming updates do not re-parse or re-layout the whole message.

Configure it in Settings → AI. Pick a model and a permission mode.
Safety nets: permission mode, loop detection, stop button and write audit log.
