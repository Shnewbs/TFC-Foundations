# TFC Foundations — Work Log

The preceding complete log is retained byte-for-byte in [WORK_LOG_BEFORE_SEASONAL_BLOCK_MODELS](history/WORK_LOG_BEFORE_SEASONAL_BLOCK_MODELS.md), linking to the mechanical, livestock, model, entity, save and common-API checkpoints. Earlier validation limits remain unchanged.

## 2026-10-09 — Seasonal block-state models and mold geometry

### Source and publication

Resumed public `26.1.2` at `11c52d1ddaa4d5ca518e45dd3ad47e2a3ba90f0b`, following source `5fbcf6623b81fd06d7e4c250772b1880684d673e`. The preceding artifact `11638282576` was downloaded and verified against SHA-256 `5f4bf550b2c30fc5ae2c346f5ac24bb962e94110c8d0238e209d1fa64956beaf`. Its source supplied the local code baseline; remote documentation changes were preserved. The exact Java 25 toolchain and resolved 84-JAR classpath were reused after checksum verification. Target and dependency versions did not change.

Staging **`1e613ee837c8a3d9e3616c09d4a5e0392933068d`** transported the reviewed patch. Apply run **`37994496413`** checked the exact staging parent, all 131 source preimages/postimages, path membership, clean Git state, bounded decoding and traversal/symlink restrictions. Applied source **`bf289ca22c3ca3146fe5623cb66c253fb7917044`** removed the temporary transfer files/workflow and dispatched normal validation. No force push, branch deletion, version increment, core-system exclusion or release-gate relaxation was used.

Patch **`TFC-Foundations-26.1.2-seasonal-block-models.patch`**: **131 files; 2,435 insertions; 1,177 deletions; 225,076 bytes**. SHA-256: **`92058efe355bfedac737e7252091725272927a335304f328b138b0a5de4e268a`**. Packed XZ SHA-256: **`b02ec7ac936ca3795fb1b848a7b083ca0bfdf10792f40771b9f19e653250549b`**. A transport transcription mismatch was corrected before staging; final source application checked every digest. The delivered patch contains code/resources/tests, not the later documentation update or a runnable mod.

### Production changes

**Dynamic world-model dispatch:** PlantBlockModel and LeavesBlockModel implement the target DynamicBlockStateModel path through SeasonalBlockStateModel. Seasonal alternatives are immutable. Cache keys distinguish both the model instance and selected baked part. The selected particle material and per-context flags follow the stage; context-free flags conservatively combine all alternatives. The leaf context-free particle fallback remains blooming stage 3, while fallback world geometry remains dense stage 0.

DynamicBlockModel.Unbaked wraps the native Variant codec, retains rotations/UV lock and resolves dependencies. It locates dynamic geometry through model ancestry. Ordinary resource-pack cuboid replacements fall back to SingleVariant instead of requiring a dynamic implementation. The native variant model-state conversion was corrected to `asModelState()` after exact-target compilation exposed the type mismatch. SeasonalUnbakedModel resolves each existing inline stage using the target model baker; generic static consumers receive the documented fallback. This does not implement a general dynamic ItemModel adapter.

**Seasonal behavior:** SeasonalModelMath separates the existing plant lifecycle/daytime and leaf climate/season calculations for comparison against a frozen pre-port oracle. World-facing methods retain position hashes, wet-season and hemisphere inputs, fast-graphics/conifer behavior, calendar inputs and lazy solar/average-rainfall queries. Missing client level falls back safely for plant selection. No live world or climate-cache behavior was exercised, and no performance gain is claimed.

**Resources and registration:** nine Java files, 115 blockstate JSON files, resources/assets.py and six test/tool files make up the checkpoint. The new BlockModelRegistration listeners register the plant, leaf and mold model loaders plus `tfc:dynamic`. Other legacy registrations in ClientEventHandler remain blockers. Adding only Java loaders would not route seasonal models through the new block-state API: 245 references now carry the dispatch type. Weighted chestnut choices, rotations and multipart conditions remain structurally identical after removing the new type marker. The generator was updated for relevant single plants, leaves and weighted chestnut definitions. All 178 seasonal and 16 mold model definitions retain their exact original bytes. Current item definitions do not inherit seasonal loaders; the new test rejects future inheritance until a dedicated item adapter is supplied.

**Mold geometry:** MoldsModelLoader now builds native CuboidModelElement/CuboidFace/UnbakedCuboidGeometry. The 14-by-14 cell bounds, original face orientation/UV formulas, shading, no-tint and unculled faces remain implemented. Invalid pattern dimensions fail explicitly. No fully atlas-baked mold or GPU draw was tested.

### Local validation and clean replay

```sh
# JAVA_HOME and PATH select the verified target Java 25 JDK.
python tools/porting/compile_main_diagnostics.py
python tools/porting/run_seasonal_block_smoke.py
python tools/porting/inspect_api.py
```

The edited source and an independent clean Git replay both complete the main diagnostic pass with **1,649 sources, 1,997 errors, exit 1, timed_out=false**. These use `-proc:none`, not the complete Gradle/mixin pipeline. The clean replay uses `git apply --check --index --whitespace=error`, verifies every reviewed postimage and records **probes=0, compiler=1**. Main diagnostics do not occur in the migrated model classes; the narrowly changed ClientEventHandler remains incomplete.

The new Java suite passes **200,225 scalar parity comparisons** against the frozen reference within **208,269 total contract assertions**, along with **392 native unbaked mold cells** and **194 asset-loader checks**. Do not add the parity count to the assertion total. Source checks cover 178 seasonal model definitions, 245 dynamic references, 115 preserved blockstate structures, registration and forbidden legacy APIs. Six new Python mutation tests deliberately break dispatch, weighting, item inheritance, registration and the frozen oracle; all are rejected as expected.

The Java suite uses actual scalar code, native Variant codecs and native unbaked model/geometry APIs. Its cache/dispatch/material cases use synthetic BlockStateModelPart fixtures, marker materials and a collecting resolver. Those are not a genuine fully baked level model or resource reload. The existing suites retain their previous real-model construction and CPU-geometry coverage; their successful execution does not extend to live rendering or gameplay.

All prior suites pass locally: 173 standalone Java checks, six earlier Python guards, save/brain contracts, 114 package-info compilations, the 48-model suite, 11 livestock/cat suite and mechanical/boat/equine suite. Both new and old probes are tracked and invoked by inspect_api.py. Local complete resource validation was not run because its Python dependency set was unavailable in the offline container; the GitHub resource step below is the independent check.

### Independent GitHub validation

[Run **37994534146**](https://github.com/Shnewbs/TFC-Foundations/actions/runs/37994534146), source **`bf289ca22c3ca3146fe5623cb66c253fb7917044`**, completed with overall **failure**.

| Check | Observed result |
| --- | --- |
| Full Gradle/main compilation | FAIL: exit 1; **1,997 javac errors**, grouped into **1,908 path/line/message entries**. |
| New seasonal/mold suite | PASS: **208,269 contract assertions**, including **200,225 scalar parity comparisons**; **392 native mold cells** and **194 asset-loader checks**. |
| New Python mutation guards | PASS: all **six** new tests. |
| Earlier utility, save, brain and headless model suites | PASS; independently reproduced against the published source. |
| Main/data/test license tasks | PASS. |
| Existing resource-validation step | PASS; this is not a registered in-game reload or visual test. |
| Downloaded CI source preservation | PASS: all **131 reviewed postimages** match; the new test invocation is tracked and temporary transfer paths are absent. |

Artifact **11646452667**, `tfc-port-diagnostics-37994534146-1`, retains logs, reports and exact source until **October 16, 2026 UTC**. Downloaded ZIP SHA-256 **3aeda006199a2819512e15e1aca41ac825c3251153f7d1f48cc76b4db7e6a65f** was verified. Every one of the 131 source/resource/test postimages matches the archived source and all one-time transport paths are absent. The source includes the new inspection-suite invocation.

Normal validation continues to run:

```sh
./gradlew -I tools/porting/diagnostics.gradle writePortClasspath compileJava compileDataJava compileTestJava build --continue --no-daemon --no-configuration-cache --console=plain
```

Passing standalone probes or resource validation does not override a failed full build. Raw javac totals and grouped path/line/message summaries are distinct measures.

### Remaining compiler and playability boundary

Completed local diagnostics move **2,103 to 1,997 (106 fewer)**; counts include cascading errors and are not independent bug counts or a completion percentage. The full GitHub build independently reproduces the final 1,997 raw javac count; its 1,908 grouped entries are a different reporting measure.

NOT RUN: registered resource reload, atlas baking, live climate/world selection, GPU rendering, in-game appearance, client/dedicated-server startup, world generation/save/reload, multiplayer, survival progression or performance measurements. Prior registry-backed round trips and bootstrap-dependent probes remain unexecuted. Downstream full data/test compilation and packaging remain blocked.

Contained-fluid, trimmed-item, pile and mold-table model migration remains open, as do other block/item/block-entity rendering, ClientEventHandler/world rendering/overlays, JEI/Patchouli, recipes, equipment/materials, capabilities, schedules, registry/holder and world-generation APIs. No 26.3 or Earth code changed. No playable JAR, release or CurseForge upload is claimed. Continue using [PROJECT_STATUS](../../PROJECT_STATUS.md) and its compiler/playability gates.
