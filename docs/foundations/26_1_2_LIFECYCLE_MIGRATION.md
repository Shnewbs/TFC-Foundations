# Minecraft 26.1.2 — Lifecycle and rabbit variant migration

This changeset preserves inventory, ingot pile and rabbit genetics behavior while removing several calls to Minecraft APIs that changed since TFC's original target.

## Block removal
- DeviceBlock's previous use of `affectNeighborsAfterRemoval` was functionally wrong: by this time the old block entity has normally been removed. It is replaced with `InventoryBlockEntity.preRemoveSideEffects`, which delegates back into DeviceBlock's existing DROP/SAVE/NOOP policy.
- `ejectInventory` clears its slots after spawning the items to avoid repeated ejection. SAVE and NOOP do not call it.
- IngotPileBlockEntity now ejects ingots in `preRemoveSideEffects`. The existing creative-mode void step in IngotPileBlock is preserved.
- These paths require live verification: survival drops, creative no-drops, preserved-item components, chunk load/unload, multiblock destruction, and structure/piston/command removal.

## Rabbits
- Vanilla Rabbit#setVariant is private in 26.1. TFCRabbit now owns a synchronized `TFC_VARIANT` integer accessor, overrides `getVariant()`, and persists a new `TFCVariant` key.
- Existing saves lacking the new key fall back to the vanilla Rabbit variant on load.
- New rabbit offspring genes use integer IDs; older string-form genes remain readable. In-game reproduction and rendering are not yet validated.

## Validation status
Source regression guards: `python3 -m unittest discover -s tools/porting -p 'test_26_1_2_lifecycle.py'`.
These source-level checks **are not** Gradle compilation or Minecraft gameplay tests. A Java 25 Gradle build, dedicated/client startup, worldgen, survival, save/reload and item drop checks are still mandatory before a playable release.
