# TFC Foundations 26.1.2 — compiler error checklist

**Source:** user-provided `problems-report.html` from Windows Gradle `compileJava` failure (October 2026).
**Baseline:** 190 reported Java errors across 96 files. This is a Gradle Problems Report subset, **not** a complete count of compiler diagnostic occurrences or the entire migration backlog.

Legend: `- [x] ~~...~~` = source-level fix committed or verified in the current branch, **not** yet confirmed by a successful fresh Gradle build. `- [ ]` = outstanding. Diagnostics refer to line numbers in the user's older source checkout and may move after patches.
**Checklist:** 9 source-addressed, 181 still open from these 190 entries. No successful post-fix compilation has been claimed.


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
- [ ] Line 504 — reported Java compilation error
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
- [ ] Line 18 — reported Java compilation error
- [ ] Line 19 — reported Java compilation error
- [ ] Line 20 — reported Java compilation error

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
- [ ] Line 126 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/blocks/TFCLightBlock.java`**
- [ ] Line 129 — reported Java compilation error

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
- [ ] Line 138 — reported Java compilation error
- [ ] Line 160 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/blocks/devices/ThermometerBlock.java`**
- [ ] Line 62 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/blocks/devices/VaneBlock.java`**
- [ ] Line 78 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/blocks/plant/TFCBushBlock.java`**
- [x] ~~Line 51~~ — source-addressed (verified source already public); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/blocks/plant/TFCKelpBlock.java`**
- [ ] Line 74 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/blocks/plant/fruit/FruitTreeSaplingBlock.java`**
- [x] ~~Line 256~~ — source-addressed (verified source already public); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/blocks/plant/fruit/SeasonalPlantBlock.java`**
- [x] ~~Line 324~~ — source-addressed (649c395); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/blocks/wood/TFCChestBlock.java`**
- [ ] Line 95 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/container/ScribingTableContainer.java`**
- [ ] Line 78 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/entities/ai/livestock/DelegatingTemptingSensor.java`**
- [x] ~~Line 43~~ — source-addressed (44d88da); pending clean-build verification

**`src/main/java/net/dries007/tfc/common/entities/ai/pet/TamableAi.java`**
- [ ] Line 166 — reported Java compilation error

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
- [ ] Line 166 — reported Java compilation error
- [ ] Line 176 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/fluids/FluidHelpers.java`**
- [ ] Line 79 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/recipes/ingredients/AndIngredient.java`**
- [ ] Line 29 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/recipes/ingredients/FluidContentIngredient.java`**
- [ ] Line 30 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/recipes/ingredients/HasTraitIngredient.java`**
- [ ] Line 19 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/recipes/ingredients/HeatIngredient.java`**
- [ ] Line 26 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/recipes/ingredients/LacksTraitIngredient.java`**
- [ ] Line 19 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/recipes/ingredients/NotRottenIngredient.java`**
- [ ] Line 17 — reported Java compilation error

**`src/main/java/net/dries007/tfc/common/recipes/ingredients/RottenIngredient.java`**
- [ ] Line 17 — reported Java compilation error

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
- [ ] Line 324 — reported Java compilation error
- [ ] Line 324 — reported Java compilation error
- [ ] Line 345 — reported Java compilation error
- [ ] Line 345 — reported Java compilation error

## Other changes not represented by one of these 190 report entries

- [x] ~~Bowl inventory mutations use the item handler extraction API~~ (commit `a3743be`) — gameplay source change; still needs runtime testing.
- [x] ~~Python executable selection on Windows~~ (commit `d3e597f`) — the uploaded build report confirms execution reached Java compilation.

## Maintenance

Update this checklist only with concrete source commits and new compiler evidence. Mark each source-addressed row as build-verified in a later revision after an actual successful compiler pass; do not equate fewer displayed errors with playability.
