# TFC Foundations — Current Project Status

Updated 2026-10-09 UTC and America/Los_Angeles.

**Version 0.0.0 remains an incomplete source port. Full compilation, client/server startup and survival testing have not passed. No playable JAR or release was produced by this checkpoint.**

## Direction and targets

Continue public development in `Shnewbs/TFC-Foundations`, preserving history, EUPL notices, credits and `tfc` identifiers. First-playable priority: Minecraft 26.1.2, NeoForge 26.1.2.114, JEI 29.43.0.107 and Patchouli 26.1-94. Java 25 and Gradle 9.2.1 remain pinned. The separate `26.x` branch remains the Minecraft 26.3 forward track toward a separately verified 26.4 port. No 26.3 or Earth code changed here. Core gameplay, visuals, the required guide and publication gates remain enabled.

## Seasonal block models and mold geometry

Source **`bf289ca22c3ca3146fe5623cb66c253fb7917044`** applies a **131-file checkpoint** after staging `1e613ee837c8a3d9e3616c09d4a5e0392933068d`. Apply run `37994496413` verified the source preimages/postimages, removed the temporary transport and used a normal push. The checkpoint comprises nine Java files, 115 blockstate definitions, one resource generator and six test/tool files.

Plant and leaf models now use the native dynamic block-state pipeline rather than removed baked-model APIs. A registered `tfc:dynamic` variant preserves native rotation/UV-lock handling and supports weighted/multipart definitions. Immutable baked seasonal alternatives participate in the geometry cache key, keeping different stages and model instances distinct. Particle materials and layer flags follow the selected stage; context-free material flags conservatively include all alternatives. No performance improvement is claimed.

The resource migration adds 245 dynamic references across 115 blockstates and updates the generator so regeneration retains them. Model identifiers, weights, rotations and multipart conditions are protected by structural checks. All 178 seasonal and 16 mold model JSON definitions remain byte-identical. Seasonal item-model inheritance is explicitly rejected by the guard until its separate adapter exists; current item assets do not use this new dynamic route.

Existing plant lifecycle/daytime selection and leaf climate/hemisphere/graphics selection were separated into testable scalar logic without redesigning their seasonal formulas. Position hashes and conditional climate/solar reads remain guarded. The mold loader adopts native cuboid geometry while preserving the 14-by-14 cell layout, face UVs, bounds, shading and no-tint behavior. A missing client level uses a safe plant fallback. These changes do not establish in-game appearance or live climate behavior.

## Independent validation

[GitHub run **37994534146**](https://github.com/Shnewbs/TFC-Foundations/actions/runs/37994534146), testing source `bf289ca2`, completed with overall **failure**.

| Check | Observed result |
| --- | --- |
| Full Gradle/main compilation | FAIL: exit 1; **1,997 javac errors**, grouped into **1,908 path/line/message entries**. |
| New seasonal/mold suite | PASS: **208,269 contract assertions**, including **200,225 scalar parity comparisons**; **392 native mold cells** and **194 asset-loader checks**. |
| New Python mutation guards | PASS: all **six** new tests. |
| Earlier utility, save, brain and headless model suites | PASS; independently reproduced against the published source. |
| Main/data/test license tasks | PASS. |
| Existing resource-validation step | PASS; this is not a registered in-game reload or visual test. |
| Downloaded CI source preservation | PASS: all **131 reviewed postimages** match; the new test invocation is tracked and temporary transfer paths are absent. |

The **208,269 contract assertions include 200,225 scalar comparisons** against a frozen pre-port reference; these are not additional counts to sum. The same suite checks 392 native unbaked mold cells and loads 194 existing asset definitions. Six new Python mutation tests cover missing dispatch/registration, changed weights, unsupported seasonal item inheritance and changes to the frozen reference.

The edited local source and clean Git patch replay both complete the diagnostic main pass with **1,649 source files, exit 1, 1,997 errors and no timeout**. Their standalone inspection passes with exit 0. These diagnostic compilations use `-proc:none`, not the full Gradle/mixin pipeline. Compared with the previous 2,103 pass, the reduction is **106 diagnostics**, not an independent bug count or completion percentage. The migrated model paths have no diagnostics in this traversal; the minimally changed ClientEventHandler remains incomplete.

Artifact **11646452667**, `tfc-port-diagnostics-37994534146-1`, retains logs, reports and exact source until **October 16, 2026 UTC**. Its downloaded SHA-256 **3aeda006199a2819512e15e1aca41ac825c3251153f7d1f48cc76b4db7e6a65f** was verified.

**Test boundary:** the scalar code, Variant codec and unbaked geometry/loader implementations use the resolved target libraries. Cache/dispatch/material tests also use synthetic model-part fixtures and a collecting resolver, not a fully baked level model. No registered whole-resource reload, atlas baking, live climate/world selection, GPU rendering, client/server startup, world creation/save/reload, multiplayer, survival progression or performance measurement has passed. Earlier registry-backed round trips and bootstrap-dependent probes remain unexecuted. Full downstream data/test compilation and packaging remain blocked.

## Next compiler and playability gates

1. Continue contained-fluid, trimmed-item, pile and mold-table models, other block/item/block-entity rendering, ClientEventHandler/world rendering/overlays and JEI/Patchouli adapters.
2. Finish remaining recipes, equipment/materials, capabilities, schedules, registry/holder and world-generation APIs, then pass complete main/data/test compilation, licenses, resource generation and packaging.
3. Verify genuine client/server startup, resource loading and appearance, new-world generation, save/reload/reconnect, calendar, inventories/fluids, guide/recipes and survival before calling an artifact playable.
4. Independently verify shared work on 26.3 rather than copying target-specific APIs blindly.

Normal TFC generation remains the default. The optional natural Earth request remains nominally one block per horizontal meter without generated manmade structures. Coordinate/elevation groundwork is not a selectable Earth preset, licensed dataset pipeline or complete chunk generator. Projection, height, geology/climate, caching and performance requirements remain open.

See [WORK_LOG](docs/foundations/WORK_LOG.md) for commands, commits and limits. The preceding complete status and log are preserved byte-for-byte in [STATUS_BEFORE_SEASONAL_BLOCK_MODELS](docs/foundations/history/STATUS_BEFORE_SEASONAL_BLOCK_MODELS.md) and [WORK_LOG_BEFORE_SEASONAL_BLOCK_MODELS](docs/foundations/history/WORK_LOG_BEFORE_SEASONAL_BLOCK_MODELS.md).
