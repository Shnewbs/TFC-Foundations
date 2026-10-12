# TFC Foundations — Work Log

## 2026-10-12 — HUD extraction and contextual-bar source migration

Continued `26.1.2` after `271d84a1`. Migrated health, mounted health, food, thirst/heat, ink, chisel, fishing, mount-jump and familiarity overlays from removed `LayeredDraw.Layer`/`GuiGraphics` and global shader state to NeoForge `GuiLayer`, `GuiGraphicsExtractor`, 2D poses and explicit textured draws. Existing identifiers, sprite placement/crop expressions, text content and survival formulas remain. Heat and ink alpha are submitted per draw.

Vanilla experience and jump layers are now a contextual background/foreground pair that also serves the locator. A client-only `GuiAccessor`, registered in `tfc.mixins.json`, reads the selected native renderer without naming its inaccessible enum. TFC preserves the locator when experience is disabled unless the horizontal fishing meter occupies its space. The vanilla pair is suppressed while that meter draws; the vertical fishing/jump option remains. HUD spacing is no longer reduced when the experience bar or preserved locator still occupies the space.

**Checked:** both Java sources parse; native Minecraft 26.1.2 `javap` signatures and NeoForge 26.1.2.114 source were inspected for extraction, layer registration, contextual renderer, field descriptor, pose/text/texture methods and ARGB conversion. All **19 sprite draw argument sets** (texture, placement, crop and size) match the prior source; all four text draws remain. Mixin JSON parses. These are syntax and source/API checks, not target compilation, a transformed-mixin test or visual gameplay validation.

**Still blocked:** this session has Java 17, no Java 25 runtime, and the Gradle dependency download is network-blocked as recorded in the preceding checkpoint. No full build, JAR or client/server test was produced. The latest measured main compile remains **860 errors at `cdc66669`**, before the subsequent guide/HUD edits; no new total is inferred. Historical report checklist: **77 source-addressed / 113 open**, with these two newly crossed items explicitly awaiting compilation. Validate resource reload, all overlay options, locator/fishing transitions, riding, ink/heat transparency, screen scaling and mixin application before release. No new branch, Actions run or 26.3 change.

## 2026-10-12 — Guide quick-lookup and book hook continuation (source-only)

Continued existing `26.1.2` from `cdc6666928155bf3a271da6b24855392f493763f`. The book background mixin now targets Patchouli's `extractRenderState` with `GuiGraphicsExtractor` and the current 2D pose API. Inventory tabs, the field-guide ID and background scale are retained. Quick-lookup tooltips submit their progress fan through Patchouli's native GUI submission bridge, capture the pose/timing values before deferred rendering, and use current item/text/input/platform/network APIs. Screen-edge placement and server-authoritative opening remain; no selected-slot changes were introduced. Missing books and suppressed tooltips reset lookup timing. Progress geometry is bounded to one revolution.

**Validation limits:** Java parser syntax checks passed for both edited sources. Direct `javap` inspection of cached Minecraft 26.1.2 classes confirms the GUI extraction methods and render-state contracts; cached NeoForge 26.1.2.114 source confirms the tooltip event graphics type. Patchouli's published `release-26.1-94-beta` source confirms the mixin target and GUI submission bridge. This is source/API inspection, not compilation against the pinned final Patchouli binary or an in-game test. This session has Java 17, not Java 25; its Gradle wrapper download fails with `Network is unreachable` before compilation. No new compiler total or playable JAR is claimed. The latest recorded full main compile remains **860 errors at the preceding checkpoint**. The historical checklist stays **75 source-addressed / 115 open**, since these two files are outside that report subset. No Actions run, new branch or 26.3 change was made.

## 2026-10-12 — Local native renderer/guide/fluid checkpoint

Starting branch HEAD: `c920472da99c7c039f68aaa3b82114824e1a74fa`, existing public `26.1.2`. Resolved the actual TFC-patched Minecraft 26.1.2 / NeoForge 26.1.2.114 artifacts and pinned Patchouli 26.1-94 / JEI 29.43.0.107, using JDK 25 and Gradle 9.2.1. No new branch or Actions run was requested. The older CI diagnostic count is not used as this patch's baseline.

### Changes and preservation

Three instrument models carry immutable pose records; their block-entity renderers read world state during extraction and submit native model work with light/breaking overlay data. Vane visibility resets per pose. Calendar orientation/month texture, instrument offsets, all original geometry factory bodies and texture IDs are retained. Javelin geometry uses native `Model.Simple`; projectile orientation/material/foil are copied into its render state. Held-item javelins remain an independent unfinished renderer.

Patchouli custom components implement current extraction and matrix APIs, explicit GUI texture pipelines, modern text codecs, item/fluid holder ingredients and synchronized recipe lookup. The multiblock wrapper calls Patchouli's extraction method. Empty knapping ingredient lists no longer cause modulo-by-zero. Page layout, tooltips and actual multiblock rendering need live validation; Patchouli event/font integration has separate unresolved errors.

New `FluidModelRegistration` shares appearance definitions with underwater extensions, registers every existing source/flowing holder pair, and separately registers river water with vanilla water tint. `FluidRendererExtension` implements the native fluid tint interface. Salt-water climate tint and all existing texture/overlay identifiers remain; RGB dye colors receive explicit opaque alpha. `RenderHelpers` and guide fluid draws use the model set's material/tint. Thirst/exhaustion adopt the server-level callback parameter without changing their effect bodies.

### Reproducible local checks

The container's NeoForm Runtime failed at two `ProcessHandle.Info.command().orElseThrow()` expressions before compiling TFC. `tools/porting/prepare_neoform_java_fallback.py` creates a separate opt-in local runtime JAR from matching official 2.0.31 binary/source JARs. It requires exactly one replacement in each named class and uses `java.home/bin/java` only if process metadata is absent. It does not overwrite dependency caches, substitute game classes, change dependencies/access transformers, exclude sources or suppress build failures. The normal unmodified toolchain remains the release reference. Full target artifact recompilation then completed.

Commands after resolving the exact classpath (`JAVA_HOME` set to JDK 25):

```sh
./gradlew -I /path/to/local/neoform-java-fallback.gradle -I tools/porting/diagnostics.gradle writePortClasspath --no-daemon --no-configuration-cache --console=plain
python tools/porting/compile_main_diagnostics.py
python tools/porting/run_instrument_model_smoke.py
./gradlew -I /path/to/local/neoform-java-fallback.gradle -I tools/porting/diagnostics.gradle build --continue --no-daemon --no-configuration-cache --console=plain
```

Baseline diagnostic compilation used the untouched starting main sources, the same resolved TFC-patched classpath and the runner's javac flags (`--release 25 -parameters -proc:none -Xmaxerrs 10000`). Baseline: 1,657 sources, 957 errors, exit 1, no timeout. Final: 1,658 sources, **860 errors**, exit 1, no timeout. These are displayed compiler diagnostics, including cascades; they do not count independent defects or prove runtime correctness. The new source is fluid model registration.

`run_instrument_model_smoke.py` PASS: three instrument models across 25 pose/visibility scenarios and the javelin model. It compares native root versus previous per-part CPU rendering positions, UVs, normals and vertex multiplicity, including successive vane base toggles. Original `createBodyLayer()` bodies match the starting commit byte-for-byte. The suite is included in `inspect_api.py`; the complete older suite was not rerun for this checkpoint. No replacement Minecraft classes or platform stubs are used by the new test. This is not a GPU or live extraction test.

Machine-readable evidence includes source hashes, unchanged geometry hashes, baseline/final modes and the remaining error counts by file: [2026-10-12-render-guide.json](validation/2026-10-12-render-guide.json). The original user-report checklist advances 21 entries to 75 source-addressed / 115 open, without claiming full compilation.

Full `build --continue` exited 1. Main javac emitted **860 errors**; all three license tasks passed, resource processing was up-to-date and dependency verification ran. `downloadAssets` also failed while provisioning its separate Java 21 toolchain and the temporary disk filled, truncating the final Gradle failure report. An earlier full-build attempt had reached main compilation too but failed to provision Java 21 before these final fluid changes. Only this task's failed download/extraction and temporary problem-report files were removed. The follow-up command is `compileJava checkLicenseMain checkLicenseTest checkLicenseData --continue`, with the same optional local NeoForm init and ordinary failure semantics. That follow-up completes in 49 seconds, exits 1 with 860 compiler errors and passes all three license tasks; its failure report is complete. No build failure is converted to success.

### Remaining gates

No runnable mod JAR, client/server startup, registered resource reload, guide appearance, new-world/save/reload or survival gameplay was verified. The largest remaining diagnostic groups are client registration, world rendering, contained-fluid and trimmed-item models, and overlays. Other block/entity/item renderers, guide event/font code, JEI and common APIs remain. Version `0.0.0`, full gameplay, EUPL notices and branch/dependency policy are unchanged; 26.3 on `26.x` requires separate work.


The preceding complete log is retained byte-for-byte in [WORK_LOG_BEFORE_SNAPSHOT_BLOCK_MODELS](history/WORK_LOG_BEFORE_SNAPSHOT_BLOCK_MODELS.md), linking to the seasonal, mechanical, livestock, model, entity, save and common-API checkpoints. Earlier validation limits remain unchanged.

## 2026-10-09 — Immutable pile, scraping and mold-table models

### Source and publication

Resumed public `26.1.2` at documentation commit `5435d1118a6141f0b67544fbbcd3f37d105d344b`, following source `bf289ca22c3ca3146fe5623cb66c253fb7917044`. The preceding source artifact's SHA-256, `3aeda006199a2819512e15e1aca41ac825c3251153f7d1f48cc76b4db7e6a65f`, was verified. Its exact source supplied the local baseline; the later remote documentation was preserved. The Java 25 toolchain and all 84 resolved classpath JAR hashes were verified. Target and dependency pins did not change.

Staging **04691f1d8b5e9742885b9a30bfd0739c3b692e69** transported the reviewed source with bounded decoding, exact-parent/preimage/postimage checks, path/symlink restrictions and clean-index application. Apply run **37998280708** succeeded. Source **5024736b274f1ad3d764c5bb5572ead6f71a6032** applied all 28 reviewed file changes, removed the one-time transport/workflow and dispatched normal validation. Publication used a normal push, never a force push or concurrent-source overwrite.

Patch `TFC-Foundations-26.1.2-snapshot-block-models.patch`: **28 files, 1,399 insertions, 575 deletions, 126,023 bytes**. SHA-256 **520f01fbc269e99608bd2df20014af0193530e26516b3b18c83233f2916aa109**. XZ transport: **26,228 bytes**, SHA-256 **0f53597cc9259f8819765dc04118800a80e43e4b36b8ee7538740ea5e2f5edbf**. The patch includes two obsolete-interface deletions; it is source/tests/resources, not a runnable JAR or the later documentation checkpoint.

No version increment, gameplay feature removal, dependency substitution, release-gate relaxation, 26.3 change or Earth implementation was performed.

### Production changes

**Snapshot publication:** BlockEntityModelData is client-class-free and contains only immutable values. Pile defensively copies its texture-identifier list and carries a fallback identifier. Scraping captures input/output identifiers, the 16-bit tile mask and opaque colors. Mold-table data contains only a selected item identifier. IngotPileBlockEntity and ScrapingBlockEntity refresh model data after relevant changes and client loads. MoldTableBlockEntity replaces its client-model dependency with an identifier and keeps its existing slot-update path, with a load refresh. No mutable inventory or client baked-model object is sent to geometry workers.

**Native dynamic models:** SnapshotBlockStateModel reads ModelData through the supplied level interface, never getBlockEntity or inventory contents. It caches parts per immutable key with a 4,096-quad weight limit and model-identity geometry keys. Context-free calls return an empty fallback; contextual particle/material flags follow the selected part. Conservative global flags include translucent and animated possibilities. The cache is per baked model, not global. Tests cover concurrent same-key loading, eviction and separation between reload/model identities using synthetic parts. No real chunk scheduling or measured throughput is claimed.

**Pile/scraping geometry:** StaticBlockMesh represents immutable faces, vertices and texture identifiers. The single/double ingot placement formulas, alternating 90-degree layers, fractional bounds, and three legacy trapezoid face bodies are retained. Frozen reference execution compares every vertex position and UV for all valid pile counts, including empty counts. Scraping retains all 16 cells, input/output pass order, tile height and dye routing; explicit opaque alpha prevents RGB dye colors becoming transparent vertex colors. Native face transforms use the block/UV center conventions and inverse face transformation from ModelState.

StaticBlockMeshBaker converts that mesh to the exact target BakedQuad/MaterialInfo API. Material resolution is synchronized around the retained baker because missing-material collection may be mutable; local texture lookup avoids repeated resolution within one bake. **Lighting is intentionally adapted:** white pile vertices allow native shading/light application rather than applying legacy shade a second time. Appearance under actual lighting remains unverified. The baker compiles, but real atlas/material baking is not executed in the standalone suite.

**Mold tables and resource reload:** MoldTableBlockModel registers standalone dependencies from `models/block/mold/` resources, preserving namespaces and exact path-prefix handling. Discovery replaces a volatile immutable identifier/key catalog, including when the result is empty. Each unbaked table captures the current catalog during dependency discovery; each baked table owns immutable base and mold parts and does not look up a global baked-model map. Inspection of the resolved native ModelManager showed standalone-loading completion before model dependency discovery. This inspection is not a registered in-game resource reload.

The loader deep-copies JSON before removing its custom loader field and parses the remainder through native CuboidModel. Dispatch emits the base once and the selected mold once; absent, unknown or removed mold data emits only the base. The registration callback has an unambiguous one-argument method, with the collection-taking helper separately named. A compile-only IEventBus listener probe protects that signature.

**Assets and saved fields:** four blockstates (single/double ingot pile, scraping, mold table) now use the existing `tfc:dynamic` variant route. Their generator reproduces the change. Canonical guards retain identifiers, rotations and multipart conditions. All 20 relevant model assets remain byte-identical. Unsupported item-model inheritance through these world-only routes is explicitly rejected by the source guard. IBakedGeometry and IStaticBakedModel were removed after replacing their users; the guard detects accidental restoration. Three exact block-entity save-writer bodies retain their prior hashes. That is source preservation, not world save/load validation.

### Local checks and corrected attempts

Both JAVA_HOME and PATH select the verified Java 25 JDK and exact 84-JAR target classpath.

```sh
python tools/porting/run_snapshot_block_smoke.py
python tools/porting/inspect_api.py
python tools/porting/compile_main_diagnostics.py
```

The main-source command uses `-proc:none` and explicitly records timeouts/exit codes; it is diagnostic-only. Baseline reports 1,997 errors. The first snapshot-model pass reports 1,927. Adding an overloaded listener helper exposed one ambiguous ClientEventHandler method reference (1,928); renaming that helper restored the completed final **1,927 errors across 1,651 sources, exit 1, no timeout**. The model classes have no diagnostics; ClientEventHandler and MoldTableBlockEntity retain unrelated unfinished APIs.

A first combined standalone run exceeded the host's command-time limit and is not counted as an overall pass. Another overlapping local test invocation raced on a shared temporary class-output directory; its missing-class failure was not treated as a source test success. Final validation runs serially in a separate clean Git worktree and records **PROBES=0**, **COMPILER=1**, 1,927 errors, timed_out=false. All reviewed postimages match. No Minecraft/TFC replacement classes or loader stubs were supplied.

The initial patch replay also caught omission of two staged interface deletions from a worktree-only diff. The frozen final patch was regenerated from `git diff HEAD`, cleanly applied using `--check --index --whitespace=error`, and all 28 changes—including the deletions—were verified. A new deliberate restored-interface mutation test protects this case. The final patch and transport checksums above refer only to that corrected version.

A local `python resources validate` attempt did not execute validation because `mcresources` was absent. Only the independent GitHub resource step, which installs the pinned requirements, can establish that resource-validation result for this checkpoint.

### Standalone coverage and boundaries

The new runner compiles the actual production dynamic models, snapshot values, geometry and baker classes against the resolved target libraries. It executes **102 pile-count scenarios**, compares **65,904 vertices' position/UV data** with PileLegacyReference, covers **all 65,536 scraping masks**, and reports **428,808 counted assertions**, including the vertex comparisons. These measures overlap and must not be summed. It checks immutable lists, color alpha, texture/particle selection, transform order, cache concurrency/eviction, stale-state clearing and model-identity reload separation.

Synthetic model-part and level-interface fixtures are explicitly used for cache/dispatch tests; no real game level is constructed. Native unbaked JSON parsing and standalone registration/dependency objects are exercised, including all 16 TFC mold definitions and an addon fixture. Those checks do not bake an atlas or execute the live ModelManager reload. Texture/material objects and native GPU submission remain outside this suite.

**12 Python mutation-guard tests** pass. They deliberately alter dispatch, multipart rotation, callback registration, defensive copying, forbidden live-world reads, load refresh, the frozen oracle, saved fields, unsupported item inheritance, stale discovery catalogs and obsolete-interface deletion. Four blockstate structures, 20 model assets, three original geometry face bodies and three save-writer bodies have preservation contracts. Source contracts do not instantiate the block entities or prove network/save behavior.

The complete clean replay also passes earlier suites: 173 standalone Java checks, existing Python guards and save/brain contracts, package metadata, the 48-model suite, livestock/cat suite, mechanical/boat/equine suite and seasonal-model suite. Deprecated JOML/target API warnings remain visible. The new test runner and its inspect_api invocation are tracked in the final source.

### Independent GitHub verification

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

Normal CI still runs the full command without overriding its failure status:

```sh
./gradlew -I tools/porting/diagnostics.gradle writePortClasspath compileJava compileDataJava compileTestJava build --continue --no-daemon --no-configuration-cache --console=plain
```

### Remaining compiler and playability boundary

The locally and independently CI-confirmed reduction is **1,997 to 1,927 (70 diagnostics)**. Counts include cascading failures and are not independent bugs or a completion percentage. ContainedFluidModel, TrimmedItemModel, ClientEventHandler, LevelRendererExtension and overlays remain prominent rendering clusters. Recipes, equipment/materials, capabilities, schedules, registry/holder/worldgen and JEI/Patchouli work remain outstanding.

NOT RUN: live block-entity extraction, real chunk workers, atlas/material baking, registered whole-resource reload, GPU rendering, in-game appearance, client/server startup, world generation/save/reload, multiplayer, survival progression or performance measurements. Earlier registry-backed round trips and bootstrap-dependent probes remain unexecuted. Full downstream data/test compilation and packaging remain blocked; no playable JAR or release is claimed.

Continue with the explicit target and release gates in [PROJECT_STATUS](../../PROJECT_STATUS.md).
