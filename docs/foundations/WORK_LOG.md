# TFC Foundations — Work Log

The complete preceding log is preserved byte-for-byte in [WORK_LOG_BEFORE_ENTITY_AI_API](history/WORK_LOG_BEFORE_ENTITY_AI_API.md), which links to the earlier common API and loot history. This record does not upgrade earlier test results.

## 2026-10-09 UTC — Entity persistence, brain factories and server lifecycle

### Source and publication

Resumed public 26.1.2 at `c350b69e1300c128e3db53af947a896aa37dbca5` (code baseline `46401c030a6d2a6e8d9721147837237d8b03605d`, 2,920 reported compilation errors). Target pins and the 26.3 branch were not changed.

Public staging `798d1d2beba0238a68d3286c42600d7f05e6912a` applied in successful run `37885605570`. Applied source commit: **`4ea0a94b771091573f461610cf6613378d47b398`**. Normal validation run: **`37885637990`**. The source application validated every preimage/postimage for **79 files**, removed its one-time transfer files, and used a normal push. Source application success is not a successful mod build. No force push, version increment, release, CurseForge publication or Earth change was made.

The compressed transport contained a bounded edit manifest with SHA-256 `13d7d7ac516cdf8ed9c466356db4b1dd5ad0c1123a0569ed010baa0442b3dcc1`. Every source preimage/postimage was checked before any file was written, with duplicate/traversal/symlink protection. The standard Git patch was separately replayed against clean preimages and reproduced all 79 final files byte-for-byte.

### Production changes

**Entity persistence:** 62 entity/helper save hooks across 31 Java files now use ValueInput/ValueOutput. Existing literal save keys, strict numeric/default handling, nested items/genes and horse chest-before-superclass inventory load order are retained. EntityHelpers delegates strict CompoundTag and ValueInput reads to the actual production NbtHelpers utility. Optional item codecs retain existing saved field names for horse chests, held minecart items, chest boats, chest carts and thrown javelins.

OviparousAnimal and WingedPrey now assign the saved plucking cooldown that the earlier loader read and discarded. Missing owner/genes values clear stale state. These repairs require later real entity/world round trips; source tests alone do not prove compatibility with existing saves.

**Pets and variants:** pet owners use native EntityReference synchronization; existing UUID-facing helpers and Owner/owner save keys remain. Native UUID codecs retain the legacy four-int representation. Cat variants use the data-driven target registry/spawn selector, with witch-hut handling retained. The existing scaled TFC kitten model keeps its adult-layout texture lookup; no new baby model is claimed. Pet collar dye and cat sound lookup use target components/registries. Live spawning, sound, synchronization and visual behavior are untested.

**Brains and activities:** 52 activity initializers across 14 AI families were adapted to ActivityData/provider factories. Activity registration precedes saved-memory restoration. Priorities, conditions, erased-memory sets and ordering are retained by the source migration. Frog copies native activity definitions and replaces only idle, retaining native non-idle definitions. Camel/armadillo generic bridges are explicit rather than pretending the native superclass exposes a different generic brain type. The native Brain constructor was inspected to verify activity registration, packed-memory restoration and core/default initialization order. TFCBrain schedules and related APIs still require migration; no AI gameplay result is claimed.

**Server lifecycle:** server damage, attack, AI-step and fall hooks adopt exact target signatures. Forty player-message calls preserve chat versus overlay routing. The seat entity implements the required server damage hook with its previous non-health behavior. Core gameplay, renderers, guide sources and packaging gates remain enabled.

**Tests and tracking:** four new entity test source files are tracked and invoked by inspect_api.py. Explicit tools/porting ignore-rule exceptions prevent Python/Java/JSON/Gradle test sources from being silently omitted; binary/cache outputs remain excluded. This addresses source tracking, not a claim that the unfinished main source now compiles.

### Local verification

The actual pinned Java 25 JDK and all 84 JARs from the resolved Minecraft 26.1.2 / NeoForge 26.1.2.114 / JEI 29.43.0.107 / Patchouli 26.1-94 classpath were used; no game/TFC stubs were introduced.

Completed main-source passes moved from 2,920 to 2,820 after the first save slice, then 2,727, 2,674, 2,611 and finally **2,583** after the reviewed brain/lifecycle/pet changes. The final portable runner covers **1,632 sources**, exit 1, timed_out=false. Its `-proc:none` mode remains diagnostic-only, not a substitute for Gradle/mixin/data/test/package validation.

All **173 standalone Java checks** pass: 51 common, four actual production loot, 49 previous NBT/ValueIO and **69 new entity-save/UUID/activity API checks**. The new suite compiles actual NbtHelpers and exercises strict saved numeric types/defaults, cooldown sentinels and long precision, legacy/native UUID codec shapes and round trips, nested generic data and ActivityData priority order/identity. Six Python regression-guard tests pass, including deliberate cooldown/key/owner/genes regressions and balanced source scanning. All 62 source save-hook signature/key contracts pass. Native Brain bytecode order/default-activity contracts pass.

Existing separate actual production particle/base/save-utility compilation and all 113 package-info checks continue to pass. These are not complete mod compilation or game execution. An additional clean replay extracted the previous source archive, applied the standard Git patch, and ran the standalone probes successfully from that clean source directory; this checks for missing/local-only source.

`git diff --cached --check` passes. The standard patch passes clean `git apply --check` and reproduces every reviewed postimage. Patch **TFC-Foundations-26.1.2-entity-save-ai.patch**: 259,662 bytes, 79 files, 1,682 insertions / 655 deletions, SHA-256 **ef950d3a871980031272ac3334d4bf1064b3b3e67ff9b1a875fad1d196c23e80**. It is a source patch, not a playable JAR.

### Independent GitHub verification

[Run 37885637990](https://github.com/Shnewbs/TFC-Foundations/actions/runs/37885637990), source `4ea0a94b771091573f461610cf6613378d47b398`, completed with overall **failure**. Full Gradle/main compilation reports **2,583 errors**, exit 1, matching the final local diagnostic pass. Its summary groups these into **2,471 path/line/message entries**; grouped entries and raw javac totals are different measures, neither a project completion estimate.

- PASS: all 173 standalone Java checks, six Python regression-guard tests, 62 source-hook contracts and native Brain initialization-order/default-activity checks.
- PASS: existing separate production particle/base/utility and 113 package-info compilation checks.
- PASS: main/data/test license tasks and the existing resource-validation step.
- FAIL: full main compilation; downstream full data/test compilation and packaging remain blocked.
- NOT RUN: entity construction/ticking, AI gameplay, synchronized ownership, cat variants/audio/rendering, registry-backed item/fluid/name round trips, world save/reload, client/server launch, multiplayer, survival progression and performance. Earlier bootstrap-dependent probes remain unexecuted.

The new runtime suite compiles the actual production NbtHelpers. UUID and ActivityData checks exercise native target APIs. Source contracts and negative fixtures do not instantiate TFC entities or prove behavior. No bootstrap-dependent check was relabeled as a runtime pass.

Artifact **11595744632**, `tfc-port-diagnostics-37885637990-1`, retains exact logs, reports and source until October 16, 2026 UTC. Downloaded ZIP SHA-256 **765c1118ef9ce071a6f963f79e6a616be1bb16191dc2d1cccfdeace02770633c** was verified. All 79 source postimages match the tested local tree and downloaded CI archive; no temporary entity transfer files remain.

### Reproduction and next work

With JAVA_HOME and PATH selecting the exact Java 25 JDK and the pinned classpath resolved:

```sh
python tools/porting/compile_main_diagnostics.py
python tools/porting/inspect_api.py
```

The first command is a `-proc:none` diagnostic pass. Normal CI remains:

```sh
./gradlew -I tools/porting/diagnostics.gradle writePortClasspath compileJava compileDataJava compileTestJava build --continue --no-daemon --no-configuration-cache --console=plain
```

The completed diagnostic difference is **2,920 to 2,583 (337 fewer)**. Counts include cascades and are not individual bugs or a completion percentage. Remaining clusters include ClientEventHandler, entity and block/item models, LevelRendererExtension/overlays, equipment/materials, capabilities, TFCBrain schedules, registry/holder and world-generation interfaces, and JEI/Patchouli.

The compiler/playability milestone stays open. The repository remains an incomplete source port, not a playable release. No client/server/world/AI/performance success, GitHub release or CurseForge publication is claimed. Current targets and next work are in [PROJECT_STATUS](../../PROJECT_STATUS.md).
