# Vulkan 驱动

- 走 Vulkan 的渲染器才显示驱动选择：内置的 Ironized Zink（以及可能挑到它的「自动」）与插件渲染器。
- 内置的 MobileGlues / GL4ES / LTW 不需要选驱动。
- 换驱动前先确认插件要求；选错会直接黑屏。
