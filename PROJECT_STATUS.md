# TFC Foundations — Current Project Status

## 2026-10-13 UTC — Branch cleanup and verified common API repairs

Continued the existing `26.1.2` branch at `558427da0a32ee416d6f0e5d26e127b567bd288f`. Sixteen historical `port/` refs were removed after source comparison and history consolidation (`67c57fe3`). GitHub now has only `26.1.2` and `26.x`; all deleted tips remain reachable from `26.1.2`. Both targets have an `AGENTS.md` rule prohibiting task/repair/validation/transport branches. The 26.1.2 workflow no longer names deleted branches.

Restored JDK 25.0.4.1, Gradle 9.2.1 and the exact 84-file compile classpath with unchanged Minecraft 26.1.2 / NeoForge 26.1.2.114 / JEI 29.43.0.107 / Patchouli 26.1-94 pins. Both Gradle `compileJava` and diagnostic javac reproduce **759 errors** before source changes and **713 errors** afterward (46 fewer displayed diagnostics, not 46 independent bugs). Final diagnostic traversal: 1,660 sources, exit 1, no timeout, annotation processing disabled. Gradle compilation separately fails with the same 713 errors. Main, test and data license tasks pass. The opt-in, untracked NeoForm 2.0.31 host Java-path workaround was required; this is not stock-toolchain release validation.

Source changes:
- Seven tooltip hooks use the native display/consumer API. Contents, capacities and molten/solid text are retained; temporary lists are allocated only for legacy inventory-summary helpers.
- Block picking uses the current native callback, including inventory snapshots and sealed-device contents. Placed-item and pit-kiln selection reconstruct their hit from the player's authoritative raycast. Typed block-entity data and collected components replace the removed native item-save helper.
- Fox variants use vanilla's component/synchronization/save path; frog variants use the dynamic registry and component hook. Climate thresholds remain unchanged. Squid movement writes the native movement vector through a registered accessor; horse eating sounds keep their existing sound providers.
- Calendar storage uses a `SavedDataType` and codec while preserving the `calendar` compound, always-dirty behavior, and legacy identifier path `tfc_calendar` under the native default namespace. The native engine now resolves namespaced saved-data paths; conversion of older worlds is not verified.
- Pot and mold-table recipes use the existing synchronized recipe-map helpers. Crafting remainders use nullable native templates, preserving filled-container and barrel behavior. Knapping retains the legacy flat ingredient schema. Optional item-stack NBT uses the current registry-aware codec, preserving list positions and empty defaults.
- Reload callbacks and the ChunkMap constructor mixin match the inspected target signatures. Milk's config-controlled replacement hooks native `Item.finishUsingItem` only for `minecraft:milk_bucket`, preserving other items' consumption behavior.

Focused existing suites pass: **16 flat ingredient codec checks** and **49 real NBT/ValueIO checks**, seven standalone particle classes/base block entity compilation, four superclass save/sync bytecode delegations and the native tank serializer contract. These suites do not test the newly migrated calendar in a world, registry-backed inventories, actual mixin application, block picking, milk use, variants, guide appearance or gameplay. No new source exclusions, missing-system stubs, dependency changes, JAR or runtime success.

Remaining: 713 main Java errors, dominated by client registration, block/item/world rendering and resource-handler capability migration. Data/test compilation, full packaging and client/server/world/survival verification remain open. Candidate version stays `0.0.0`; the independent 26.3 source was not given these gameplay changes.


## 2026-10-12 — JEI category extraction and ingredient enumeration (source-only)

Continued existing `26.1.2` from `0c1d87de5fa635cded8bf13eb2d42e5ecbea7b2f`. Twenty recipe-category files now use `GuiGraphicsExtractor`; category and drawable callbacks retain JEI's `draw` name. Text uses `text`/`textWithWordWrap` with opaque white where the previous API accepted RGB. Lamp animation uses an explicit textured pipeline, preserving its crop/position arguments. No recipe IDs, slot coordinates, arrow/fire durations or registration lists were changed.

Shared ingredient helpers enumerate current item/fluid holders through the existing `RecipeHelpers`. Sized fluids retain `ingredient.amount()`; sized items retain their counts and transient non-decaying food copies. Block ingredients map blocks to items and exclude air before constructing the native ingredient. Casting, heating, simple item and pot displays use the same current enumeration. Lamp fuel uses the native bucket-volume constant for representative unsized fluid stacks.

**Checks:** all 20 Java sources pass parser-only syntax checking. Cached Minecraft 26.1.2 and NeoForge 26.1.2.114 class/source inspection confirms holder enumeration and stack constructors. JEI's upstream `26.1` API source confirms `draw(... GuiGraphicsExtractor ...)` in both recipe categories and drawables; that branch currently identifies specification 29.47.0, so it is not a substitute for compiling against this project's pinned 29.43.0.107 binary. No dependency pins were changed.

**Not verified:** full Java 25 compilation, exact pinned JEI binary linkage, visual layout/animation, custom ingredient display behavior, or client/server startup. Casting's legacy fluid capability remains an explicit separate migration task. Workspace maintenance removed the local checkout; source was recovered from GitHub and these edits were published directly to the existing branch. Available runtime is still Java 17. The latest recorded main compile remains **860 errors at `cdc66669`**, before subsequent guide/HUD/JEI source edits. Do not subtract source edits from that number or call this playable. The historical 190-entry checklist stays unchanged at 77 source-addressed / 113 open pending a new diagnostic pass.

## 2026-10-12 — HUD extraction and contextual-bar source migration

Continued `26.1.2` after `271d84a1`. Migrated health, mounted health, food, thirst/heat, ink, chisel, fishing, mount-jump and familiarity overlays from removed `LayeredDraw.Layer`/`GuiGraphics` and global shader state to NeoForge `GuiLayer`, `GuiGraphicsExtractor`, 2D poses and explicit textured draws. Existing identifiers, sprite placement/crop expressions, text content and survival formulas remain. Heat and ink alpha are submitted per draw.

Vanilla experience and jump layers are now a contextual background/foreground pair that also serves the locator. A client-only `GuiAccessor`, registered in `tfc.mixins.json`, reads the selected native renderer without naming its inaccessible enum. TFC preserves the locator when experience is disabled unless the horizontal fishing meter occupies its space. The vanilla pair is suppressed while that meter draws; the vertical fishing/jump option remains. HUD spacing is no longer reduced when the experience bar or preserved locator still occupies the space.

**Checked:** both Java sources parse; native Minecraft 26.1.2 `javap` signatures and NeoForge 26.1.2.114 source were inspected for extraction, layer registration, contextual renderer, field descriptor, pose/text/texture methods and ARGB conversion. All **19 sprite draw argument sets** (texture, placement, crop and size) match the prior source; all four text draws remain. Mixin JSON parses. These are syntax and source/API checks, not target compilation, a transformed-mixin test or visual gameplay validation.

**Still blocked:** this session has Java 17, no Java 25 runtime, and the Gradle dependency download is network-blocked as recorded in the preceding checkpoint. No full build, JAR or client/server test was produced. The latest measured main compile remains **860 errors at `cdc66669`**, before the subsequent guide/HUD edits; no new total is inferred. Historical report checklist: **77 source-addressed / 113 open**, with these two newly crossed items explicitly awaiting compilation. Validate resource reload, all overlay options, locator/fishing transitions, riding, ink/heat transparency, screen scaling and mixin application before release. No new branch, Actions run or 26.3 change.

## 2026-10-12 — Guide quick-lookup and book hook continuation (source-only)

Continued existing `26.1.2` from `cdc6666928155bf3a271da6b24855392f493763f`. The book background mixin now targets Patchouli's `extractRenderState` with `GuiGraphicsExtractor` and the current 2D pose API. Inventory tabs, the field-guide ID and background scale are retained. Quick-lookup tooltips submit their progress fan through Patchouli's native GUI submission bridge, capture the pose/timing values before deferred rendering, and use current item/text/input/platform/network APIs. Screen-edge placement and server-authoritative opening remain; no selected-slot changes were introduced. Missing books and suppressed tooltips reset lookup timing. Progress geometry is bounded to one revolution.

**Validation limits:** Java parser syntax checks passed for both edited sources. Direct `javap` inspection of cached Minecraft 26.1.2 classes confirms the GUI extraction methods and render-state contracts; cached NeoForge 26.1.2.114 source confirms the tooltip event graphics type. Patchouli's published `release-26.1-94-beta` source confirms the mixin target and GUI submission bridge. This is source/API inspection, not compilation against the pinned final Patchouli binary or an in-game test. This session has Java 17, not Java 25; its Gradle wrapper download fails with `Network is unreachable` before compilation. No new compiler total or playable JAR is claimed. The latest recorded full main compile remains **860 errors at the preceding checkpoint**. The historical checklist stays **75 source-addressed / 115 open**, since these two files are outside that report subset. No Actions run, new branch or 26.3 change was made.

## 2026-10-12 — Instrument, projectile, guide and fluid rendering checkpoint

Continued existing `26.1.2` from `c920472da99c7c039f68aaa3b82114824e1a74fa` using the pinned Java 25 / Minecraft 26.1.2 / NeoForge 26.1.2.114 classpath. A fresh same-mode diagnostic main compilation fell from **957 to 860 errors** (97 fewer displayed diagnostics, not independent bugs or a completion percentage). The final pass covers 1,658 sources, exits 1, and does not time out. The original 190-entry checklist now has **75 source-addressed / 115 open**; it is only a historical report subset.

- Anemometer, calendar clock and vane rendering now extract immutable model values and submit through the native renderer. Calendar facing/month texture, vane attachment offset/base visibility and all four instrument/javelin geometry factories are preserved.
- Thrown javelins use native render-state extraction, material textures and a separate foil submission. The held-item javelin renderer remains unfinished.
- Patchouli custom components use `GuiGraphicsExtractor`, explicit textures/tints, current ingredient holders and TFC's synchronized client recipe map. Knapping pages guard empty matching inputs. Other guide integration errors and actual book appearance still need work.
- Fluid appearance is registered through `RegisterFluidModelsEvent` for source/flowing pairs and river water. Existing underwater overlays and climate-dependent salt-water tint are retained. Dye fluids explicitly gain opaque alpha; fluid GUI helpers now resolve native models.
- Thirst/exhaustion tick callbacks accept the new `ServerLevel` argument; their effect logic is unchanged.

**Validation:** instrument model probes PASS (25 pose/visibility scenarios plus javelin geometry, against real target libraries); all four geometry factory bodies match the starting commit. Main javac has no diagnostics in the migrated models/renderers, custom guide component package, fluid extension/registration, or effect classes. `ClientEventHandler` and `RenderHelpers` still have unrelated errors. Full Gradle `build --continue` FAILS: main compilation emits 860 errors, while `downloadAssets` also fails during separate Java 21 provisioning/disk exhaustion. Main/data/test license tasks PASS. The subsequent compile/license-only run completes in 49 seconds with the same 860 compiler errors and all three license tasks passing. No successful package or runtime test is claimed.

**Environment qualification:** stock NeoForm Runtime 2.0.31 could not obtain its Java executable from `ProcessHandle.Info.command()` in this container. An explicitly opt-in local helper supplies `java.home/bin/java` at those two tool call sites; target pins, game patches, source sets and build gates stay unchanged. This enables compilation evidence but is not a stock-toolchain release validation. See [continuation instructions](docs/foundations/CONTINUE_BUILDING.md) and [machine-readable evidence](docs/foundations/validation/2026-10-12-render-guide.json).

**Still incomplete:** full main/data/test build, item/contained-fluid models, client event and world rendering APIs, remaining guide/JEI/capability APIs, resource reload, client/server startup, world/save/survival tests. Version stays `0.0.0`. No gameplay system was disabled, dependency pin changed, or `26.x` migration attempted. Publication uses `[skip ci]` while Actions quota is suspended.


## 2026-10-12 — Deferred block loot-table API migration (source only)

On existing `26.1.2`, `ExtendedProperties.dropsLike(Supplier<Block>)` now uses the supplier's `IdHolder.getId()` to construct the same default `<namespace>:blocks/<path>` loot-table key via `Properties.overrideLootTable(Optional<ResourceKey<LootTable>>)`. This replaces removed `Properties.lootFrom(Supplier)` without calling `Supplier.get()` during block registration. Audited call sites: standing/wall torch pairs in `TFCBlocks` and standing/wall wood signs in `Wood`; their suppliers resolve to `TFCBlocks.Id` registry holders. Suppliers without IDs now fail explicitly rather than forcing unsafe early block construction. Full block drops, registrations, resource data and game runtime remain untested. EUPL license and all block/loot identifiers are unchanged.

The source-addressed compiler checklist subset is now **54 / 190**, with **136 still open**. These counts record source changes, **not a verified compilation pass**. The latest available Gradle failure predates recent source commits, so it does not validate this fix. Current local runtime lacks Java 25 and the resolved NeoForge dependency classpath; no Gradle build, JAR, client/server startup, or gameplay testing was possible. Historical port branches were checked: the wholly ancestor branches need no merge; divergent branches require careful per-file reconciliation and were not blindly merged. Minecraft 26.3 `26.x` stays separate. No GitHub Actions run was triggered.


## 2026-10-11 — Animal conversion API continuation (source only)

On `26.1.2`, commits `860c9ee` and `9c899d9` replace the removed boolean `Mob.convertTo` overload in wolf-to-dog and ocelot-to-cat taming with `ConversionParams.single` and native after-conversion callbacks. The callbacks preserve spawn initialization and adult age; wolf conversion also preserves gender. The source was reread after publication. The 190-entry report checklist is now **53 source-addressed / 137 open**; these two entries still require a clean build. Local runtime is Java 17. The checked-in Gradle 9.2.1 wrapper could not download its distribution because network sockets are blocked, so no javac task started; Java 25 and the dependency cache are also absent. There is no full compile, runnable JAR, or gameplay test from this checkpoint. The `26.x` forward track and Minecraft 1.21.1 were not changed.

## 2026-10-11 — Ten additional javac API corrections (not compiled)

The source-only checklist now shows **51 source-addressed / 139 open** of its original 190-entry subset. Newly committed direct-branch fixes: clock removal hook, POI holder lookup, penguin water/bubble ambient check, horse listener superclass/override cleanup, direct block loot table transfer and postprocess callback, barrel rack release, bloomery inventory ejection in block entity pre-remove effects, and inventory tooltip `GuiGraphicsExtractor` conversion (height + image API).

**Known incomplete behavior:** `ExtendedProperties.dropsLike(Supplier<Block>)` still uses removed `Properties.lootFrom` and requires a deferred-safe loot-table strategy; the horse inventory-change callback was removed from the superclass, so TFC encumbrance invalidation must be hooked into the new inventory model. Rendering and capability refactors remain outstanding. There is no passing build or client/server test yet.

## 2026-10-11 — Ten further source repairs (not compiled)

Ten direct-branch Java changes were committed for five removed-block lifecycle hooks (anemometer, thermometer, vane, nest box, sluice), three removed `RenderShape.ENTITYBLOCK_ANIMATED` values (glass basin, hot-poured glass, water wheel), the kelp-tree fluid tick access, and the predator swimming fluid predicate. The source blobs were reread and the intended edits were verified. Relevant NeoForge 26.1 documentation confirms `affectNeighborsAfterRemoval` is the replacement for old post-removal neighbor updates; inventory cleanup remains a separate `BlockEntity#preRemoveSideEffects` responsibility. See the compiler checklist for commit IDs and per-error tracking. Rendering, block drops, save/reload, AI fluid behavior, and the overall build are **not** verified.

## 2026-10-11 — Local Gradle Java errors and active source repairs

The user's Windows Gradle build moved past Python item-selector generation and reached `:compileJava`, which **failed** with Minecraft 26.1.2/NeoForge API errors. The uploaded Gradle Problems Report has **190 top-level error entries** (across 96 files) plus warnings/advice. These are a report subset, not the complete number of javac errors emitted by the build. The checklist is at [COMPILER_ERROR_CHECKLIST_26.1.2.md](docs/foundations/COMPILER_ERROR_CHECKLIST_26.1.2.md): **51 source-addressed, 139 still unchecked**. Crossed-out items have **not** yet been proven clean by a follow-up Gradle run.

Most recent source fixes, committed **directly** to `26.1.2` without creating branches: glassworking item tooltip callback (`a39cd36`), biome precipitation/sea-level argument (`f55759d`), tamable stop-attack predicate (`fc29c56`), holding minecart interaction/spawn/destroy method signatures (`91c27f9`), spring/salt fluid entity checks (`dd86ac8`), bubble-column client-level accessor (`3d63e55`), rejecting invalid minecart chest slots (`4fff220`), client camera block-position accessor (`a8b6ede`), and block-entity type constructor registration (`8210c9e`). The source and checklist were reread after pushes; there was no local Java 25 or full NeoForge classpath available for actual compilation. Rendering state extraction, capabilities' resource-handler migration, GUI/JEI/Patchouli and other errors still require substantial implementation.

**Do not mark this playable.** `compileJava`, full `build`, client/dedicated server startup, worldgen, save/reload, and survival scenarios all remain unverified or failed in the latest available evidence.

## Branch consolidation audit — 2026-10-11 UTC

The only intended long-lived branches are `26.1.2` (first playable) and `26.x` (26.3/future 26.4). Three temporary `port/` branches remain visible pending deletion; **do not create more**.

- `port/26.1.2-horse-rope-api`: identical commit to `26.1.2` at audit; zero ahead/behind.
- `port/26.1.2-cooldown-api`: historical unique commit, but all three affected Java file blobs match `26.1.2` byte-for-byte. Changes are already present; no new merge is needed.
- `port/26.1.2-entity-spawn-20261011`: historical unique commit, but all seven affected Java file blobs match `26.1.2` byte-for-byte. Changes are already present; no new merge is needed.
- No open pull requests were found at audit. Deleting the redundant refs requires a GitHub branch-delete action, which is unavailable through the currently connected GitHub integration; **do not force-push or reset refs just to make a branch disappear**.
- This audit is source-content verification, not a successful compile. There is no newly verified Java 25 build, client startup or playable JAR as a result of this check.

Continue new source work directly on `26.1.2`; carry applicable verified migrations independently into `26.x`.

Updated 2026-10-10 UTC. Latest verified build summary is first; later sections preserve a historical checkpoint.

**Version 0.0.0 remains an incomplete source port. Full compilation, client/server startup and survival testing have not passed. No playable JAR or release was produced by this checkpoint.**

## Direction and targets

Continue public development in `Shnewbs/TFC-Foundations`, preserving history, EUPL notices, credits and `tfc` identifiers. First-playable priority remains Minecraft 26.1.2, NeoForge 26.1.2.114, JEI 29.43.0.107 and Patchouli 26.1-94. Java 25 and Gradle 9.2.1 are unchanged. The separate `26.x` branch remains the Minecraft 26.3 forward track toward a separately verified 26.4 port. No 26.3 or Earth code changed here. Core gameplay, visuals, the required guide and publication gates remain enabled.

## Live branch handoff — 2026-10-10

**Branch `26.1.2`** at source [`659b70ed82b0e7fd2ab54914fd37ad20839b53e0`](https://github.com/Shnewbs/TFC-Foundations/commit/659b70ed82b0e7fd2ab54914fd37ad20839b53e0). [CI run `38070987803`](https://github.com/Shnewbs/TFC-Foundations/actions/runs/38070987803) **failed full Java/Gradle compilation**: **1,384 diagnostic occurrences** and **1,326 unique displayed file/line/message entries**. API/standalone inspection and resource validation **passed**, but those checks do not make a runnable mod. No playable JAR or successful client/server or world save/reload test has been produced.

The 26.1.2 branch has since added tool-material, recipe and ingredient API migrations, worldgen and registration fixes, and new-format item selector generation to the October 9 model checkpoint below. The item-selector generator covers **5,369 static and 66 fixed-fluid** models; **162 special models** remain unported. The separate `26.x`/26.3 track remains an independent, unverified foundation toward 26.4.

**The latest cross-platform instructions live in [CONTINUE_BUILDING.md](docs/foundations/CONTINUE_BUILDING.md).** Always inspect the live branch and CI before a continuation, and update this status after each substantive source push.

## Historical pile/scraping checkpoint (2026-10-09)

### Original checkpoint narrative

Source **`5024736b274f1ad3d764c5bb5572ead6f71a6032`** applies a **28-file checkpoint**, following staging `04691f1d8b5e9742885b9a30bfd0739c3b692e69`. Apply run `37998280708` verified every source preimage/postimage, removed the temporary transport and used a normal push. Two obsolete baked-model interfaces were replaced by the native pipeline and deleted; no gameplay feature was removed.

Ingot piles, double-ingot piles and scraping models now use immutable model-data snapshots instead of reading mutable inventories from geometry-generation workers. The snapshot payload contains texture identifiers, colors and scraping progress, not live block entities or item stacks. Relevant mutations and client loads request fresh model data. Per-model geometry caches are bounded and reload-local; their keys distinguish both snapshot contents and model identity. Concurrency tests use fixtures, not live chunk workers.

Pile positions, layered rotations and UV coordinates retain the frozen legacy geometry. Scraping retains its 4-by-4 tile layout, input/output passes and dye colors; RGB colors gain explicit opaque alpha. **Lighting adaptation:** pile geometry uses white vertex colors with native shading/light application instead of pre-applying the legacy shade. Actual lit appearance is not verified pixel-identical.

Mold tables carry the selected item identifier rather than a cached client model in their block entity. Resource discovery uses the native standalone-model registration/dependency path. Each baked table owns its model map; missing or removed mold state falls back to the base table, and a later resource discovery replaces rather than accumulates the catalog. Base table and selected mold are emitted once each in dispatch fixtures. Actual atlas baking and a registered whole-resource reload are not tested.

Four blockstate definitions and their generator now route through `tfc:dynamic`. Existing model identifiers, rotations and multipart conditions are guarded. All 20 relevant model JSON assets and the three block-entity save-writer bodies remain unchanged. These source contracts are not whole-world save-compatibility tests. Contained-fluid and trimmed-item models were not changed in this checkpoint.

## Validation

[GitHub run **37998292416**](https://github.com/Shnewbs/TFC-Foundations/actions/runs/37998292416), source `5024736b`, completed with overall **failure** because main compilation remains broken.

| Check | Observed result |
| --- | --- |
| Full Gradle/main compilation | FAIL: exit 1, **1,927 javac errors**; summary groups them into **1,845 path/line/message entries**. |
| New snapshot-model suite | PASS: **102 pile-count scenarios**, **65,904 position/UV vertices**, **all 65,536 scraping masks**, **428,808 counted assertions**. |
| New Python mutation guards | PASS: **12 tests**. |
| Earlier model, utility, save, brain and seasonal suites | PASS; independently reproduced against the published source. |
| Main/data/test license tasks | PASS. |
| Existing resource-validation step | PASS; not a registered game reload or visual test. |
| Published source preservation | PASS: all **28 reviewed changes**, including two deletions, match the downloaded source; the test invocation is tracked and temporary transport files are absent. |

Artifact **11648720875**, `tfc-port-diagnostics-37998292416-1`, retains logs, reports and exact source until **October 16, 2026 UTC**. Downloaded ZIP SHA-256 **88ec609996c45a0bcedbe7ee695904f030a6b63d4ef99b73e152190ced9f7617** was verified. The entire API-inspection/probe step passed, independently matching the local final result. These passing steps do not override the failed full build.

The clean extracted-source patch replay completed all standalone suites with **exit 0** and diagnostic main compilation with **1,651 source files, exit 1, 1,927 errors and no timeout**. The edited source reports the same compiler result. These main passes use `-proc:none`, not the complete Gradle/mixin/data/test pipeline. Compared with the previous 1,997 pass, the reduction is **70 diagnostics**, not an independent bug count or completion percentage. Changed model classes have no diagnostics in this traversal; ClientEventHandler and MoldTableBlockEntity still have separate unfinished APIs.

The new suite checks **102 pile-count scenarios**, compares **65,904 vertices' positions/UVs** against the frozen legacy path, covers **all 65,536 scraping masks**, and records **428,808 counted assertions**, including the vertex comparisons. These overlapping measures must not be added into a single total. **12 Python regression-guard tests** pass. Snapshot immutability, concurrent cache loading/eviction, model-data routing, stale-state clearing and reload isolation are exercised using explicit synthetic model-part/interface fixtures. Native unbaked loader parsing and standalone dependency registration are exercised against the exact target libraries; atlas/material baking is not.

Earlier utility/save/brain and headless model suites remain passing in the clean replay, including 173 prior standalone Java checks, previous Python guards, package metadata and prior seasonal/geometry checks. The new runner is tracked and called from `inspect_api.py`. Three save-writer source hashes and the deleted-interface guard protect against accidental source omissions.

**Not run:** live block-entity extraction or chunk-worker scheduling, atlas/material baking, registered whole-resource reload, GPU rendering, in-game appearance, client/server startup, new-world generation, save/reload/reconnect, multiplayer, survival progression or performance measurements. Earlier registry-backed round trips and bootstrap-dependent probes remain unexecuted. Full downstream data/test compilation and packaging remain blocked by main compilation.

## Next compiler and playability gates

1. Continue contained-fluid and trimmed-item models, other block/item/block-entity rendering, ClientEventHandler/world rendering/overlays and JEI/Patchouli adapters without removing these systems to compile.
2. Finish recipes, equipment/materials, capabilities, schedules, registry/holder and world-generation APIs; pass complete main/data/test compilation, licenses, resource generation and packaging.
3. Test actual client/server startup, resource reload and appearance, world creation/save/reload, calendar, inventories/fluids, guide/recipes and survival before calling an artifact playable.
4. Independently verify shared work on 26.3 rather than copying divergent target APIs blindly.

Normal TFC generation remains the default. The optional natural Earth request remains nominally one block per horizontal meter without generated manmade structures. Existing coordinate/elevation groundwork is not a selectable Earth preset, licensed dataset pipeline or complete chunk generator. Projection, height, geology/climate, caching and performance requirements remain open.

See [WORK_LOG](docs/foundations/WORK_LOG.md) for commands, commits and limits. The preceding complete status/log are preserved byte-for-byte in [STATUS_BEFORE_SNAPSHOT_BLOCK_MODELS](docs/foundations/history/STATUS_BEFORE_SNAPSHOT_BLOCK_MODELS.md) and [WORK_LOG_BEFORE_SNAPSHOT_BLOCK_MODELS](docs/foundations/history/WORK_LOG_BEFORE_SNAPSHOT_BLOCK_MODELS.md).
