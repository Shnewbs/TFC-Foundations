# TFC Foundations — Current Project Status

Updated 2026-10-09 UTC and America/Los_Angeles.

**Version 0.0.0 remains an incomplete source port. Full compilation, client/server startup and survival testing have not passed. No playable JAR or release was produced by this checkpoint.**

## Direction and targets

Continue public development in `Shnewbs/TFC-Foundations`, preserving history, EUPL notices, credits and `tfc` identifiers. First-playable priority: Minecraft 26.1.2, NeoForge 26.1.2.114, JEI 29.43.0.107 and Patchouli 26.1-94. Java 25 and Gradle 9.2.1 remain pinned. The separate `26.x` branch remains the Minecraft 26.3 forward track toward a separately verified 26.4 port; it was unchanged here. Core gameplay, visuals, the required guide and publication gates remain enabled.

## Mechanical, boat and equine checkpoint

Public source **5fbcf6623b81fd06d7e4c250772b1880684d673e** follows staging `ddc33f29f97d2fe858c28cb3a5a9f2d9ce4d6f96`. Apply run `37973174520` verified all preimages/postimages for **23 source/test files**, removed its temporary transfer and used a normal push. Dependencies and identifiers were unchanged.

Waterwheels, windmill blades, axles and bladed axles now use captured render inputs. Windmill frame, dyed-blade and optional rustic-extra portions have separate immutable pose submissions, preserving color separation and full-set conditions. Four original mechanical model factories and three axle face-generator bodies remain unchanged. Renderer-local model caching does not retain a global rendering context; no performance improvement is claimed.

Boats use native hull transforms with an independent chest-only attachment model, avoiding a second hull/paddle submission. Wood texture selection, oak fallback, raft handling and underwater water-mask conditions remain implemented. Horse rendering uses target adult/juvenile models, markings and equipment layers; donkey/mule carried-item layers retain registered barrel/chest support and clear stale appearance state.

**Juvenile visual adaptation:** chested donkeys/mules use a half-scale adult-layout mesh with a root offset, preserving the adult texture and carried geometry. This is not verified identical to legacy juvenile proportions. Native juvenile donkey geometry lacks the carried chest parts and its setup mutates an input field; the chosen independent body/attachment path avoids those issues. Actual proportions and attachment appearance still require in-game review.

## Validation

[GitHub run **37973223478**](https://github.com/Shnewbs/TFC-Foundations/actions/runs/37973223478), testing source `5fbcf662`, completed with overall **failure** because main compilation remains broken.

| Check | Observed result |
| --- | --- |
| Full Gradle/main compilation | FAIL: exit 1, **2,103 javac errors**; summary groups them into **2,007 path/line/message entries**. |
| New mechanical/boat/equine suite | PASS: **261 scenarios, 3,294 assertions, 37,416 finite CPU vertices**. |
| Earlier model, utility, save and brain suites | PASS; independently reproduced against the published source. |
| Main/data/test license tasks | PASS. |
| Existing resource-validation step | PASS; not GPU or gameplay validation. |
| Public source preservation | PASS: all **23 reviewed postimages** match the downloaded source; the test invocation is tracked and temporary transfer files are absent. |

Artifact **11638282576**, `tfc-port-diagnostics-37973223478-1`, retains logs, reports and exact source until **October 16, 2026 UTC**. Downloaded ZIP SHA-256 **5f4bf550b2c30fc5ae2c346f5ac24bb962e94110c8d0238e209d1fa64956beaf** was verified. The complete API-inspection/probe step passed; it does not override the failed full build.

Local baseline and final diagnostic passes complete with **2,193 → 2,103 errors**, across 1,640 → 1,644 sources. A clean Git patch replay matches all 23 postimages, records **probes=0**, and reproduces **compiler=1**, 2,103 errors, without timeout. These compiler passes use `-proc:none`, not the full Gradle/mixin/data/test pipeline. The reduction of 90 diagnostics is not an independent bug count or a completion percentage.

The new headless suite passes **261 scenarios, 3,294 assertions and 37,416 finite CPU vertices**. It checks real production machinery/chest/attachment models, relevant native horse geometry, pose/tint reuse, UVs, juvenile/body attachment alignment and axle transforms. It also checks source capture contracts and texture dimensions. Existing suites pass locally and in GitHub: **173 standalone Java checks, six Python guards**, prior save/brain contracts and 114 package-info compilations, plus the 48-model and 11 livestock/cat model suites. These are not game or GPU tests.

NOT RUN: renderer construction, live extraction, native GPU submission, in-game appearance/proportions, client/dedicated-server startup, world generation/save/reload, multiplayer, survival progression or performance measurements. Registry-backed round trips and earlier bootstrap-dependent checks remain unexecuted. Full downstream data/test compilation and packaging remain blocked.

## Next compiler and playability gates

1. Complete block/item/block-entity models and rendering, ClientEventHandler/world rendering/overlays and JEI/Patchouli adapters. Contained-fluid, plant, trimmed-item, leaf and mold model APIs are prominent remaining clusters.
2. Finish recipes, equipment/materials, capabilities, schedules, registry/holder and world-generation APIs; pass complete main/data/test compilation, licenses, resource generation and packaging.
3. Test genuine client/server startup, world generation, save/reload/reconnect, calendar, inventory/fluid, guide/recipe and survival progression before calling an artifact playable. Include the juvenile visual adaptation in the client review.
4. Independently verify shared work against 26.3; do not blindly copy target-specific APIs. No 26.3 or Earth code changed here.

## Earth and history

Normal TFC generation remains the default. The optional natural Earth request remains nominally one block per horizontal meter without generated manmade structures. Existing coordinate/elevation groundwork is not a selectable Earth preset, licensed dataset pipeline or complete chunk generator. Projection, height, geology/climate, caching and performance requirements remain open.

See [WORK_LOG](docs/foundations/WORK_LOG.md) for exact evidence and limits. The preceding status/log are preserved byte-for-byte in [STATUS_BEFORE_MECHANICAL_BOAT_EQUINE](docs/foundations/history/STATUS_BEFORE_MECHANICAL_BOAT_EQUINE.md) and [WORK_LOG_BEFORE_MECHANICAL_BOAT_EQUINE](docs/foundations/history/WORK_LOG_BEFORE_MECHANICAL_BOAT_EQUINE.md).
