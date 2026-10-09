# TFC Foundations — Current Project Status

Updated 2026-10-09 UTC and America/Los_Angeles.

**Version 0.0.0 remains an incomplete source port. Full compilation, client/server startup and survival testing have not passed. No playable JAR or release was produced by this checkpoint.**

## Direction and targets

Continue public development in `Shnewbs/TFC-Foundations`, preserving history, EUPL notices, credits and `tfc` identifiers. First-playable priority remains Minecraft 26.1.2, NeoForge 26.1.2.114, JEI 29.43.0.107 and Patchouli 26.1-94. Java 25 and Gradle 9.2.1 are unchanged. The separate `26.x` branch remains the Minecraft 26.3 forward track toward a separately verified 26.4 port. No 26.3 or Earth code changed here. Core gameplay, visuals, the required guide and publication gates remain enabled.

## Pile, scraping and mold-table snapshot checkpoint

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
