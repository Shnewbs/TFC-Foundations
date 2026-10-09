# TFC Foundations — Current Project Status

Updated 2026-10-09 UTC (October 8 in America/Los_Angeles).

**Version 0.0.0 remains an incomplete source port. Full compilation and client/server survival testing have not passed. No playable JAR or release was produced by this checkpoint.**

## Direction and targets

Continue public source development in `Shnewbs/TFC-Foundations`, preserving Git history, EUPL notices, credits and `tfc` IDs. First-playable priority: Minecraft 26.1.2, NeoForge 26.1.2.114, JEI 29.43.0.107, Patchouli 26.1-94. Forward track `26.x` remains Minecraft 26.3, NeoForge 26.3.0.58-beta, JEI 31.9.0.61, with its required guide dependency unresolved at its previous checkpoint. Both tracks use Java 25 and Gradle 9.2.1. Exact-target verification remains required before sharing changes or moving to 26.4.

## Entity persistence and brain API checkpoint

Public source commit **`4ea0a94b771091573f461610cf6613378d47b398`** follows staging `798d1d2beba0238a68d3286c42600d7f05e6912a`. Apply run `37885605570` succeeded after checking all preimages/postimages for exactly **79 files**, then removed the temporary transfer files and used a normal push. No force push, version change, source-system exclusion or publication-gate relaxation was used.

The source migrates **62 entity/helper save hooks across 31 Java files** to ValueInput/ValueOutput, retaining literal save keys and strict numeric/default handling. Item and genetic data retain their nested fields. Horse chests are restored before superclass inventory loading. OviparousAnimal and WingedPrey now assign the saved plucking cooldown that the previous read discarded. Missing owner/genes values clear stale state. These checks do not establish whole-world save compatibility.

Pet ownership uses native EntityReference synchronization while retaining UUID-facing helper methods and the existing Owner/owner keys. Native UUID codecs retain the legacy four-int form. Cat variants use the target data-driven registry/spawn selection and retain witch-hut handling; the TFC scaled kitten model still uses its existing adult-layout texture. Live ownership synchronization, spawning, sounds and appearance are not tested.

**52 activity initializers across 14 AI families** now use target ActivityData/provider factories so activities exist before packed memories are restored. Activity priority order, conditions and memory-erasure sets are retained by the migration. Frog retains native non-idle activities. Server damage/attack/AI-step/fall hooks and 40 player-message calls adopt target APIs, preserving chat versus overlay routing. This does not complete AI migration; TFCBrain schedules and other behavior APIs remain blockers.

The new entity regression suite is tracked and invoked by inspect_api.py. Explicit tools/porting source ignore-rule exceptions prevent source tests from being silently omitted; binary/cache outputs remain excluded.

## Verified validation

[GitHub validation 37885637990](https://github.com/Shnewbs/TFC-Foundations/actions/runs/37885637990), testing source `4ea0a94b`, completed with overall **failure**:

| Check | Observed result |
| --- | --- |
| Full Gradle/main compilation | FAIL: exit 1, 2,583 javac errors; the summary groups them into 2,471 path/line/message entries. |
| Standalone suites | PASS: 173 Java checks and six Python regression-guard tests. |
| Source/API contracts | PASS: 62 save-hook signature/key contracts and native Brain initialization-order checks. |
| Separate production compilation | PASS: existing particle/base/save-utility checks and all 113 package-info files. |
| Main/data/test license tasks | PASS. |
| Existing resource-validation step | PASS; this is not visual or gameplay validation. |
| CI source preservation | PASS: all 79 reviewed files match the downloaded source snapshot; temporary transport files are absent. |

The 173 standalone Java checks comprise 51 common, four production loot, 49 prior NBT/ValueIO and 69 new entity-save/UUID/activity checks. They pass locally, in a clean extracted-source patch replay, and independently in GitHub Actions. No Minecraft/TFC stubs were used. The six Python tests include deliberate key/cooldown/stale-state regressions; these and the 62 source contracts do not instantiate or simulate the TFC entities.

The completed local main diagnostic pass covers 1,632 source files and reports **2,583 errors**, exit 1, without timeout; its `-proc:none` mode is not the full Gradle/mixin/data/test pipeline. Compared with the prior 2,920 pass, this is **337 fewer diagnostics**, not individual fixed bugs or a completion percentage. The standard patch passes a clean Git application and reproduces all 79 reviewed postimages.

Artifact `11595744632`, `tfc-port-diagnostics-37885637990-1`, retains logs/reports/source until October 16, 2026 UTC. Downloaded SHA-256 `765c1118ef9ce071a6f963f79e6a616be1bb16191dc2d1cccfdeace02770633c` was verified.

NOT RUN: actual entity construction/ticks, AI gameplay, synchronized ownership, cat variants/audio/rendering, world save/reload, client/server launch, multiplayer, survival progression and performance. Registry-backed item/fluid/name round trips and earlier bootstrap-dependent probes also remain unexecuted. Downstream complete data/test compilation and packaging remain blocked by main compilation.

## Next compiler and playability gates

1. Continue entity/block-entity rendering, block/item/entity models, ClientEventHandler/overlays and JEI/Patchouli adapters without deleting those systems to get a green build.
2. Finish equipment/materials, capabilities, TFCBrain schedules, registry/holder and world-generation interfaces; then complete main/data/test compilation, licenses, generated resources and packaging.
3. Run genuine client/dedicated-server launch, new-world generation, save/reload, reconnect, calendar, inventory/fluid, guide/recipe and survival-progression checks before calling an artifact playable.
4. Verify shared work independently on 26.3; its loot providers, conditions and generation APIs diverge. No 26.3 or Earth code changed in this checkpoint.

## Earth and history

Normal TFC generation remains the default. The optional natural Earth request remains nominally one block per horizontal meter without generated manmade structures. Existing coordinate/elevation groundwork and eight previously recorded isolated checks are not an implemented Earth preset, licensed dataset pipeline or complete chunk generator. Projection, height, geology/climate, caching and performance requirements remain open.

See [WORK_LOG](docs/foundations/WORK_LOG.md) for commits, commands and validation limits. The previous complete status and work log are preserved byte-for-byte in [STATUS_BEFORE_ENTITY_AI_API](docs/foundations/history/STATUS_BEFORE_ENTITY_AI_API.md) and [WORK_LOG_BEFORE_ENTITY_AI_API](docs/foundations/history/WORK_LOG_BEFORE_ENTITY_AI_API.md).
