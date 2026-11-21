# Auto renderer

- Behaviour: on every launch it picks a renderer by "can this device run it → preference order → Minecraft version compatibility".
- Preference order: **MobileGlues → GL4ES → Ironized Zink**.
  - MobileGlues: requires OpenGL ES 3.2 or newer; skipped automatically otherwise.
  - GL4ES: preferred whenever the game version is in its range (the safest choice on old devices).
  - Ironized Zink: requires a working Vulkan driver; mainly covers versions newer than 1.21.4.
- It only checks "can it run", never benchmarks performance, and never writes back to your settings — each instance keeps its own configuration.
- If nothing matches (e.g. a version outside every candidate's range), it falls back to the first version-compatible renderer and logs a warning instead of failing the launch.
- LTW is experimental and excluded from auto selection.
- To see what the last launch used, check the description under "Auto" in Settings → Renderer, or look for `Auto renderer picked ...` in the log.
