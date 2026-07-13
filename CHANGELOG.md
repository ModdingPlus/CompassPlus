# 2.0.0

Full port to Minecraft 1.21.1, now available for Fabric and NeoForge.

- The HUD works like before: cardinal directions, the world spawn (compass),
  lodestone targets (lodestone compasses), and your last death position
  (recovery compass).
- Lodestone targets are read from the new item data components.
- The mod is properly client-side only on both loaders; installing it on a
  server does nothing and vanilla clients can still join.
- New client settings (config screen in the mod list; via Mod Menu on Fabric):
  always show the HUD, the world spawn marker, or the last death marker, even
  without the corresponding compass in your inventory.
- The Fabric version now requires Forge Config API Port.
