# 2.0.0

Full port to Minecraft 1.21.1, now available for Fabric and NeoForge.

- The HUD works like before: cardinal directions, the world spawn (compass),
  lodestone targets (lodestone compasses), and your last death position
  (recovery compass).
- Lodestone targets are read from the new item data components.
- The mod is properly client-side only on both loaders; installing it on a
  server does nothing and vanilla clients can still join.
- New: buried treasure maps and explorer maps (woodland mansion, ocean
  monument, trial chambers) now show their targets on the HUD.
- New client settings (config screen in the mod list; via Mod Menu on Fabric):
  each HUD component (cardinal directions, world spawn marker, last death
  marker) can be shown always or enabled by specific compass kinds (regular,
  lodestone-bound, recovery, or a per-component list of custom compass items
  from other mods), lodestone, treasure map, and explorer map markers each
  have an on/off toggle, and the slots searched for compasses are
  configurable (main hand, off hand, hotbar, inventory, equipment).
- The Fabric version now requires Forge Config API Port.
