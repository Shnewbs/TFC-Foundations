# TFC Foundations — Current Project Status

Updated 2026-10-09 UTC (October 8 in America/Los_Angeles).

**Version 0.0.0 remains an incomplete source port. Neither target has passed full compilation or client/server survival testing. No playable JAR or release exists from this checkpoint.**

## Authorized direction

Continue public development in `Shnewbs/TFC-Foundations`, preserving upstream history, EUPL notices, credits and `tfc` resource IDs. Prioritize a playable Minecraft 26.1.2 build. Maintain Minecraft 26.3 on `26.x` as the foundation for a separately verified 26.4 port. Do not equate a version-number change with compatibility. Normal TFC world generation remains the default; the optional natural Earth request is still in scope.

| Track | Pinned target | Latest verified checkpoint |
| --- | --- | --- |
| Priority playable track, `26.1.2` | Minecraft 26.1.2; NeoForge 26.1.2.114; JEI 29.43.0.107; Patchouli 26.1-94 | `46401c030a6d2a6e8d9721147837237d8b03605d`: 72-file particle/save API checkpoint; 104 standalone checks pass; full compilation fails with 2,920 reported errors. |
| Forward-port track, `26.x` | Minecraft 26.3; NeoForge 26.3.0.58-beta; JEI 31.9.0.61 | Unchanged in this continuation. Earlier exact-target diagnostics confirmed compilation failures and an unresolved required Patchouli target. |

Both tracks use Java 25 and Gradle 9.2.1. EMI/Jade/TOP adapters remain isolated and incomplete. Core gameplay, rendering and the required guide have not been excluded to obtain a successful build. Publication gates remain intact.

## Particle and block-entity save checkpoint

Source commit `46401c03` follows public staging commit `f6e34d0c7fe831059d2b3afa09986e7c97b7823b`. The one-time application checked every preimage and postimage, applied exactly 72 files, removed its temporary transport files and used a normal fast-forward push. The downloaded CI source snapshot independently matches all 72 reviewed source files.

The checkpoint updates 14 particle source files to the target sprite, provider and layer APIs. Smoke, bubbles, leaves, sparks, sleep effects, glints, wind and fluid-drip behavior remain implemented rather than being removed. Falling leaves now use the target vanilla falling-leaf implementation; their appearance and motion still need an actual client test.

Block-entity persistence migrates 89 save/load hooks across 45 classes to ValueInput/ValueOutput. Related inventory interfaces, composite serializers, pot outputs and synchronization entry points are updated. The base block entity delegates to NeoForge's superclass save/load hooks so its attachment data is not omitted. Pots clear removed output on synchronization; inventories clear removed custom names.

Legacy item, fluid, timer and nested inventory fields are retained by the source migration. Strict tag/list reads remain strict. Custom names accept legacy JSON and native component representations. Fluid tanks retain the legacy nested `Fluid` child through the native tank serializer; accepting a flat stack as a read fallback does not change the canonical writer. These implementation and format checks are not proof of full-world save compatibility.

## Verified validation results

[GitHub validation 37881633084](https://github.com/Shnewbs/TFC-Foundations/actions/runs/37881633084), source `46401c03`, completed with an overall **failure**, as required while compilation is broken.

| Check | Observed result |
| --- | --- |
| Full Gradle build / main compilation | FAIL: exit 1; javac reports 2,920 errors. The summary groups them into 2,795 path/line/message entries. |
| Reproducible local main diagnostic pass | FAIL: 1,632 source files; 2,920 errors; completed without timeout. This uses `-proc:none`, not the full Gradle/mixin pipeline. |
| Standalone execution | PASS: 51 existing utility/API checks, four existing production loot codec checks, and 49 new NBT/ValueIO checks; 104 total. |
| Separate compilation | PASS: seven actual production particle classes, the base block entity and save utilities. The common probe also compiles all 113 package-info files. |
| Emitted bytecode contracts | PASS: four superclass save/sync delegations and delegation to the native fluid-tank writer. |
| Main/data/test license checks | PASS. |
| Existing resource validation | PASS. This does not establish target rendering correctness. |
| Client/server launch, world save/reload and survival | NOT RUN; full compilation still blocks them. |

The completed compiler count decreased from 3,438 to 2,920, or 518 fewer diagnostics. Earlier error caps and grouped reports are not comparable totals. Counts include cascading errors and are not individual bug counts or a completion percentage. No diagnostics remain in the particle package in this pass; this is narrower than proving its runtime correctness.

Registry-backed item/fluid/name round trips and bootstrap-dependent chunk/component/provider probes remain unexecuted. FluidTank deprecation warnings remain visible; broader capability migration is unfinished. There are no measured gameplay or performance results.

Artifact `11594941066`, `tfc-port-diagnostics-37881633084-1`, preserves logs, reports and the exact source snapshot until October 16, 2026 UTC. Its downloaded SHA-256 is `381b4560e4bccc60b09fa733d5ac9898eef33d1803e8d721e995ec1b47fb3169` and was verified.

## Next compiler and playability gates

1. Continue entity save lifecycle migration, equipment and capability APIs, registry/holder changes and the remaining world-generation interfaces.
2. Complete entity/block-entity render-state and model migration, ClientEventHandler/overlays, and JEI/Patchouli adapters. Do not remove those systems merely to compile.
3. Pass main/data/test compilation, license checks, resource generation and packaging. Then test actual client and dedicated-server launch, new-world creation, save/reload, multiplayer reconnect, calendar, inventories/fluids, recipes/guide and survival progression.
4. Share only changes independently verified against 26.3. Its loot providers, holder-based conditions and world-generation APIs diverge from 26.1.2; neither this patch nor the earlier loot patch should be copied blindly. Publish target-specific releases only after their stated gates pass.

## Earth option

The requested optional natural Earth mode remains nominally one block per meter horizontally, without generated manmade structures. Existing coordinate/elevation utilities and eight previously recorded isolated checks are groundwork only. There is still no implemented Earth preset, licensed dataset pipeline or complete Earth chunk generator. Projection distortion, world height, geology/climate integration, caching and chunk performance remain explicit requirements. No Earth code changed in this checkpoint.

## Evidence and history

See [WORK_LOG](docs/foundations/WORK_LOG.md) for exact commits, commands, validation boundaries and source-preservation checks. The complete previous status is retained byte-for-byte in [STATUS_BEFORE_VISUAL_SAVE_API](docs/foundations/history/STATUS_BEFORE_VISUAL_SAVE_API.md), which records the earlier common API and loot checkpoints. Historical target recommendations are superseded by the authorized direction above.
