# TFC Foundations 26.1.2 — compiler error checklist

**Source:** user-provided `problems-report.html` from Windows Gradle `compileJava` failure (October 2026).
**Baseline:** 190 reported Java errors across 96 files. This is a Gradle Problems Report subset, **not** a complete count of compiler diagnostic occurrences or the entire migration backlog.

Legend: `- [x] ~~...~~` = source-level fix committed or verified in the current branch, **not** yet confirmed by a successful fresh Gradle build. `- [ ]` = outstanding. Diagnostics refer to line numbers in the user's older source checkout and may move after patches.
**Checklist:** 34 source-addressed, 156 still open from these 190 entries. No successful post-fix compilation has been claimed.


## Status and release gates

- [ ] Re-run `compileJava` on the latest `26.1.2` and confirm each crossed-out item no longer fails.
- [ ] Reach zero Java errors in `compileJava`.
- [ ] Complete data/test Java compilation and all Gradle build tasks.
- [ ] Produce a JAR and test client/server launch, world generation, save/reload and TFC survival.

## Reported compiler locations

### Client / rendering

**`src/main/java/net/dries007/tfc/client/ClientEventHandler.java`**
- [ ] Line 20 — reported Java compilation error
- [ ] Line 21 — reported Java compilation error
- [ ] Line 34 — reported Java compilation error
- [ ] Line 35 — reported Java compilation error
- [ ] Line 51 — reported Java compilation error
- [ ] Line 52 — reported Java compilation error
- [ ] Line 71 — reported Java compilation error
- [ ] Line 79 — reported Java compilation error
- [ ] Line 830 — reported Java compilation error
- [ ] Line 851 — reported Java compilation error
- [ ] Line 856 — reported Java compilation error
- [ ] Line 862 — reported Java compilation error
- [ ] Line 921 — reported Java compilation error
- [ ] Line 932 — reported Java compilation error
- [ ] Line 1072 — reported Java compilation error
- [ ] Line 404 — reported Java compilation error
- [ ] Line 405 — reported Java compilation error
- [ ] Line 406 — reported Java compilation error
- [ ] Line 407 — reported Java compilation error
- [ ] Line 410 — reported Java compilation error
- [ ] Line 592 — reported Java compilation error
- [ ] Line 596 — reported Java compilation error
- [ ] Line 597 — reported Java compilation error
- [ ] Line 673 — reported Java compilation error
- [ ] Line 688 — reported Java compilation error
- [ ] Line 689 — reported Java compilation error
- [ ] Line 690 — reported Java compilation error
- [ ] Line 691 — reported Java compilation error
- [ ] Line 692 — reported Java compilation error
- [ ] Line 693 — reported Java compilation error
- [ ] Line 694 — reported Java compilation error
- [ ] Line 697 — reported Java compilation error
- [ ] Line 698 — reported Java compilation error
- [ ] Line 699 — reported Java compilation error
- [ ] Line 700 — reported Java compilation error
- [ ] Line 701 — reported Java compilation error
- [ ] Line 702 — reported Java compilation error
- [ ] Line 704 — reported Java compilation error
- [ ] Line 705 — reported Java compilation error
- [ ] Line 707 — reported Java compilation error
- [ ] Line 719 — reported Java compilation error
- [ ] Line 720 — reported Java compilation error
- [ ] Line 725 — reported Java compilation error
- [ ] Line 811 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/ClientForgeEventHandler.java`**
- [x] ~~Line 504~~ — source-addressed (`314460b`); pending clean-build verification
- [ ] Line 563 — reported Java compilation error
- [ ] Line 566 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/ClientInventoryTooltip.java`**
- [ ] Line 18 — reported Java compilation error
- [ ] Line 29 — reported Java compilation error
- [ ] Line 41 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/DocumentedVertexConsumer.java`**
- [ ] Line 37 — reported Java compilation error
- [ ] Line 43 — reported Java compilation error
- [ ] Line 55 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/IngameOverlays.java`**
- [ ] Line 60 — reported Java compilation error
- [ ] Line 62 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/RenderHelpers.java`**
- [ ] Line 513 — reported Java compilation error
- [ ] Line 530 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/TFCKeyBindings.java`**
- [x] ~~Line 18~~ — source-addressed (`efc0830`); pending clean-build verification
- [x] ~~Line 19~~ — source-addressed (`efc0830`); pending clean-build verification
- [x] ~~Line 20~~ — source-addressed (`efc0830`); pending clean-build verification

**`src/main/java/net/dries007/tfc/client/extensions/FluidRendererExtension.java`**
- [ ] Line 32 — reported Java compilation error
- [ ] Line 38 — reported Java compilation error
- [ ] Line 44 — reported Java compilation error
- [ ] Line 50 — reported Java compilation error
- [ ] Line 56 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/extensions/ItemRendererExtension.java`**
- [ ] Line 21 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/model/ContainedFluidModel.java`**
- [ ] Line 35 — reported Java compilation error
- [ ] Line 36 — reported Java compilation error
- [ ] Line 37 — reported Java compilation error
- [ ] Line 38 — reported Java compilation error
- [ ] Line 39 — reported Java compilation error
- [ ] Line 54 — reported Java compilation error
- [ ] Line 74 — reported Java compilation error
- [ ] Line 142 — reported Java compilation error
- [ ] Line 165 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/model/TrimmedItemModel.java`**
- [ ] Line 34 — reported Java compilation error
- [ ] Line 35 — reported Java compilation error
- [ ] Line 36 — reported Java compilation error
- [ ] Line 37 — reported Java compilation error
- [ ] Line 38 — reported Java compilation error
- [ ] Line 45 — reported Java compilation error
- [ ] Line 77 — reported Java compilation error
- [ ] Line 48 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/model/entity/AnemometerModel.java`**
- [ ] Line 51 — reported Java compilation error
- [ ] Line 58 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/model/entity/CalendarClockModel.java`**
- [ ] Line 33 — reported Java compilation error
- [ ] Line 66 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/model/entity/JavelinModel.java`**
- [ ] Line 44 — reported Java compilation error
- [ ] Line 49 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/model/entity/VaneModel.java`**
- [ ] Line 27 — reported Java compilation error
- [ ] Line 60 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/overworld/LevelRendererExtension.java`**
- [ ] Line 81 — reported Java compilation error
- [ ] Line 157 — reported Java compilation error
- [ ] Line 231 — reported Java compilation error
- [ ] Line 315 — reported Java compilation error
- [ ] Line 335 — reported Java compilation error
- [ ] Line 443 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/render/blockentity/AnemometerBlockEntityRenderer.java`**
- [ ] Line 22 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/render/blockentity/AnvilBlockEntityRenderer.java`**
- [ ] Line 23 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/render/blockentity/BarrelBlockEntityRenderer.java`**
- [ ] Line 25 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/render/blockentity/BellowsBlockEntityRenderer.java`**
- [ ] Line 27 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/render/blockentity/BowlBlockEntityRenderer.java`**
- [ ] Line 24 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/render/blockentity/CalendarClockBlockEntityRenderer.java`**
- [ ] Line 26 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/render/blockentity/ChannelBlockEntityRenderer.java`**
- [ ] Line 26 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/render/blockentity/CharcoalForgeBlockEntityRenderer.java`**
- [ ] Line 22 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/render/blockentity/ChestItemRenderer.java`**
- [ ] Line 24 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/render/blockentity/CrankshaftBlockEntityRenderer.java`**
- [ ] Line 40 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/render/blockentity/CrucibleBlockEntityRenderer.java`**
- [ ] Line 18 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/render/blockentity/FirepitBlockEntityRenderer.java`**
- [ ] Line 32 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/render/blockentity/GlassBasinBlockEntityRenderer.java`**
- [ ] Line 18 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/render/blockentity/HotPouredGlassBlockEntityRenderer.java`**
- [ ] Line 24 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/render/blockentity/JavelinItemRenderer.java`**
- [ ] Line 24 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/render/blockentity/LoomBlockEntityRenderer.java`**
- [ ] Line 26 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/render/blockentity/MoldBlockEntityRenderer.java`**
- [ ] Line 37 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/render/blockentity/PanItemRenderer.java`**
- [ ] Line 31 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/render/blockentity/TFCBellBlockEntityRenderer.java`**
- [ ] Line 78 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/render/blockentity/TFCChestBlockEntityRenderer.java`**
- [ ] Line 56 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/render/blockentity/TFCHangingSignBlockEntityRenderer.java`**
- [ ] Line 87 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/render/blockentity/TFCSignBlockEntityRenderer.java`**
- [ ] Line 26 — reported Java compilation error

**`src/main/java/net/dries007/tfc/client/render/entity/TFCFishingHookRenderer.java`**
- [ ] Line 49 — reported Java compilation error

### Gameplay / world / entities / recipes

**`src/main/java/net/dries007/tfc/common/blocks/ExtendedProperties.java`**
- [ ] Line 209 — reported Java compilation error
- [ ] Line 210 — reported Java compilation error
- [ ] Line 216 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/blocks/TFCBubbleColumnBlock.java`**
- [x] ~~Line 126~~ — source-addressed (`9372f0d`, corrected by `ceca573`); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/blocks/TFCLightBlock.java`**
- [x] ~~Line 129~~ — source-addressed (`2aece7f`); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/blocks/WattleBlock.java`**
- [x] ~~Line 134~~ — source-addressed (a06d734); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/blocks/devices/AnemometerBlock.java`**
- [ ] Line 76 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/blocks/devices/BarrelBlock.java`**
- [ ] Line 276 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/blocks/devices/BloomeryBlock.java`**
- [ ] Line 280 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/blocks/devices/CalendarClockBlock.java`**
- [ ] Line 64 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/blocks/devices/NestBoxBlock.java`**
- [ ] Line 51 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/blocks/devices/PotBlock.java`**
- [x] ~~Line 64~~ — source-addressed (3f03e50); pending clean-build verification
- [x] ~~Line 66~~ — source-addressed (3f03e50); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/blocks/devices/SluiceBlock.java`**
- [x] ~~Line 138~~ — source-addressed (`5bbd70a`, corrected by `a2cb3b2`); pending clean-build verification
- [ ] Line 160 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/blocks/devices/ThermometerBlock.java`**
- [ ] Line 62 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/blocks/devices/VaneBlock.java`**
- [ ] Line 78 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/blocks/plant/TFCBushBlock.java`**
- [x] ~~Line 51~~ — source-addressed (verified source already public); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/blocks/plant/TFCKelpBlock.java`**
- [x] ~~Line 74~~ — source-addressed (`df39e0c`); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/blocks/plant/fruit/FruitTreeSaplingBlock.java`**
- [x] ~~Line 256~~ — source-addressed (verified source already public); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/blocks/plant/fruit/SeasonalPlantBlock.java`**
- [x] ~~Line 324~~ — source-addressed (649c395); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/blocks/wood/TFCChestBlock.java`**
- [x] ~~Line 95~~ — source-addressed (`5a6c1cc`); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/container/ScribingTableContainer.java`**
- [x] ~~Line 78~~ — source-addressed (`3a83131`); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/entities/ai/livestock/DelegatingTemptingSensor.java`**
- [x] ~~Line 43~~ — source-addressed (44d88da); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/entities/ai/pet/TamableAi.java`**
- [x] ~~Line 166~~ — source-addressed (`fc29c56`); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/entities/ai/predator/AggressiveSwim.java`**
- [ ] Line 34 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/entities/ai/predator/PackPredator.java`**
- [ ] Line 224 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/entities/ai/prey/PrepareRamNearestTargetTFC.java`**
- [x] ~~Line 59~~ — source-addressed (de6ccb7); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/entities/ai/prey/TFCOcelot.java`**
- [ ] Line 129 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/entities/aquatic/Penguin.java`**
- [ ] Line 71 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/entities/livestock/horse/HorseProperties.java`**
- [x] ~~Line 124~~ — source-addressed (ebe4aba); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/entities/livestock/horse/TFCChestedHorse.java`**
- [ ] Line 122 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/entities/misc/HoldingMinecart.java`**
- [x] ~~Line 166~~ — source-addressed (`91c27f9`); pending clean-build verification
- [x] ~~Line 176~~ — source-addressed (`91c27f9`); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/fluids/FluidHelpers.java`**
- [x] ~~Line 79~~ — source-addressed (`dd86ac8`); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/recipes/ingredients/AndIngredient.java`**
- [x] ~~Line 29~~ — source-addressed (`b380c07`, holder-based `items()`); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/recipes/ingredients/FluidContentIngredient.java`**
- [x] ~~Line 30~~ — source-addressed (`0230532`, holder-based `items()`); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/recipes/ingredients/HasTraitIngredient.java`**
- [x] ~~Line 19~~ — source-addressed (`953efbf`, holder-based `items()`); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/recipes/ingredients/HeatIngredient.java`**
- [x] ~~Line 26~~ — source-addressed (`953efbf`, holder-based `items()`); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/recipes/ingredients/LacksTraitIngredient.java`**
- [x] ~~Line 19~~ — source-addressed (`953efbf`, holder-based `items()`); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/recipes/ingredients/NotRottenIngredient.java`**
- [x] ~~Line 17~~ — source-addressed (`953efbf`, holder-based `items()`); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/recipes/ingredients/RottenIngredient.java`**
- [x] ~~Line 17~~ — source-addressed (`953efbf`, holder-based `items()`); pending clean-build verification

### JEI / Patchouli

**`src/main/java/net/dries007/tfc/compat/jei/TFCCraftingExtensions.java`**
- [ ] Line 32 — reported Java compilation error

**`src/main/java/net/dries007/tfc/compat/patchouli/component/AnvilComponent.java`**
- [ ] Line 16 — reported Java compilation error

**`src/main/java/net/dries007/tfc/compat/patchouli/component/GlassworkingComponent.java`**
- [ ] Line 23 — reported Java compilation error

**`src/main/java/net/dries007/tfc/compat/patchouli/component/HeatingComponent.java`**
- [ ] Line 23 — reported Java compilation error

**`src/main/java/net/dries007/tfc/compat/patchouli/component/InstantBarrelComponent.java`**
- [ ] Line 14 — reported Java compilation error

**`src/main/java/net/dries007/tfc/compat/patchouli/component/KnappingComponent.java`**
- [ ] Line 24 — reported Java compilation error

**`src/main/java/net/dries007/tfc/compat/patchouli/component/LoomComponent.java`**
- [ ] Line 20 — reported Java compilation error

### Mixins

**`src/main/java/net/dries007/tfc/mixin/MilkBucketItemMixin.java`**
- [ ] Line 28 — reported Java compilation error

**`src/main/java/net/dries007/tfc/mixin/accessor/HorseAccessor.java`**
- [ ] Line 11 — reported Java compilation error

**`src/main/java/net/dries007/tfc/mixin/client/BreakingItemParticleMixin.java`**
- [ ] Line 20 — reported Java compilation error
- [ ] Line 31 — reported Java compilation error

**`src/main/java/net/dries007/tfc/mixin/client/ItemBlockRenderTypesMixin.java`**
- [ ] Line 25 — reported Java compilation error

**`src/main/java/net/dries007/tfc/mixin/client/ItemColorsMixin.java`**
- [ ] Line 26 — reported Java compilation error

**`src/main/java/net/dries007/tfc/mixin/client/LiquidBlockRendererMixin.java`**
- [ ] Line 17 — reported Java compilation error

**`src/main/java/net/dries007/tfc/mixin/client/PlayerItemInHandLayerMixin.java`**
- [ ] Line 34 — reported Java compilation error
- [ ] Line 34 — reported Java compilation error
- [ ] Line 39 — reported Java compilation error

**`src/main/java/net/dries007/tfc/mixin/client/SkeletonModelMixin.java`**
- [ ] Line 28 — reported Java compilation error

**`src/main/java/net/dries007/tfc/mixin/client/WaterDropParticleMixin.java`**
- [ ] Line 25 — reported Java compilation error

**`src/main/java/net/dries007/tfc/mixin/client/accessor/SignRendererAccessor.java`**
- [ ] Line 23 — reported Java compilation error

**`src/main/java/net/dries007/tfc/mixin/client/compat/sodium/ItemColorsMixin.java`**
- [ ] Line 29 — reported Java compilation error

### Utilities

**`src/main/java/net/dries007/tfc/util/PowderKegExplosion.java`**
- [ ] Line 34 — reported Java compilation error
- [ ] Line 68 — reported Java compilation error

**`src/main/java/net/dries007/tfc/util/tracker/WeatherHelpers.java`**
- [ ] Line 60 — reported Java compilation error
- [x] ~~Line 324~~ — source-addressed (`abfbe44`, section-height accessor); pending clean-build verification
- [x] ~~Line 324~~ — source-addressed (`abfbe44`, section-height accessor); pending clean-build verification
- [x] ~~Line 345~~ — source-addressed (`abfbe44`, section-height accessor); pending clean-build verification
- [x] ~~Line 345~~ — source-addressed (`abfbe44`, section-height accessor); pending clean-build verification

## Additional compiler fixes from the full Gradle report (outside the 190-entry subset)

### 2026-10-11 additional repair batch

- [x] ~~Glassworking item tooltip method migrated to `TooltipDisplay` and component consumer~~ — `GlassworkingItem.java`, commit `a39cd36`; pending compilation.
- [x] ~~Biome precipitation takes a sea-level argument~~ — `BiomeBasedClimateModel.java`, commit `f55759d`; pending compilation.
- [x] ~~Holding minecart interaction now receives hit position~~ — `HoldingMinecart.java`, commit `91c27f9`; pending compilation.
- [x] ~~Vanilla minecart construction uses entity type and position assignment~~ — `HoldingMinecart.java`, commit `91c27f9`; pending compilation.
- [x] ~~Holding minecart activator receives ServerLevel~~ — `HoldingMinecart.java`, commit `91c27f9`; pending compilation.
- [x] ~~Obsolete minecart type/custom-display overrides removed~~ — `HoldingMinecart.java`, commit `91c27f9`; needs gameplay/rendering verification.
- [x] ~~Client bubble-column ambience accesses player level using `level()`~~ — `TFCBubbleColumnAmbientSoundHandler.java`, commit `3d63e55`; pending compilation.
- [x] ~~Invalid chest-minecart slot rejects writes instead of referencing removed `SlotAccess.NULL`~~ — `TFCMinecartChest.java`, commit `4fff220`; pending compilation.



- [x] ~~Scribing table legacy `createInputSlotDefinitions()` override~~ — replaced with the same three-slot layout supplied at construction (`3a83131`); pending build verification. Original 190-row item tracks the constructor at line 78.


The uploaded HTML contains many more diagnostics than the original 190-row summary; the following have source patches but do **not** change the 190-entry checklist counts.

- [x] ~~Patchouli tooltip component height requires `Font`~~ — `PatchouliClientEventHandler.java`, commit `e98adfa`; pending build verification.
- [x] ~~Creative spawn-egg item wraps an `Optional<Holder<Item>>`~~ — `TFCCreativeTabs.java`, commit `03c7c8a`; pending build verification.
- [x] ~~Amphibian resistance effect constant~~ — `AmphibianPlayDeadBehavior.java`, commit `ed21ff7`; pending build verification.
- [x] ~~Ramming AI speed and slowness effect constants~~ — `RamTargetTFC.java`, commit `e0c8db7`; pending build verification.
- [x] ~~Undefined generic `E` in pinniped attacking behavior~~ — `PinnipedAI.java`, commit `fc84d18`; pending build verification.
- [x] ~~TFC key mapping category registration and translation~~ — `ClientEventHandler.java` and `en_us.json`, commits `d4bf8ce` and `d1918e7`; pending build verification.

## Other changes not represented by one of these 190 report entries

- [x] ~~GlowArrow grounded state~~ (`aa20a04`) — use arrow accessor in place of removed field; pending compilation verification.

- [x] ~~Bowl inventory mutations use the item handler extraction API~~ (commit `a3743be`) — gameplay source change; still needs runtime testing.
- [x] ~~Python executable selection on Windows~~ (commit `d3e597f`) — the uploaded build report confirms execution reached Java compilation.

## Maintenance

Update this checklist only with concrete source commits and new compiler evidence. Mark each source-addressed row as build-verified in a later revision after an actual successful compiler pass; do not equate fewer displayed errors with playability.
