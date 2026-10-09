# TFC Foundations — Work Log

The complete preceding log is retained byte-for-byte in [WORK_LOG_BEFORE_LIVESTOCK_RENDER_STATE](history/WORK_LOG_BEFORE_LIVESTOCK_RENDER_STATE.md), linking to the earlier model, entity, save and common API checkpoints. Earlier validation claims are not upgraded by this continuation.

## 2026-10-09 — Livestock, cat and native-renderer migration

### Source and publication

Resumed public `26.1.2` at `e6e700318b97ddd1280fa0bdf66281d32f428cad`, with previous source `bcbdf950ff4280199adcfeaa15122cf6bf85e186`. A fresh baseline diagnostic pass reproduced 2,361 errors across 1,635 main Java sources, exit 1, without timeout. The prior source artifact 11598051560 was downloaded, SHA-256 checked, and extracted as the source baseline; e6's later documentation-only changes were preserved in the remote tree.

Pins remain Minecraft 26.1.2 / NeoForge 26.1.2.114 / JEI 29.43.0.107 / Patchouli 26.1-94, Java 25 and Gradle 9.2.1. The previously resolved 84-JAR classpath and JDK were verified. No game/TFC stubs, version increment, system exclusion, force push or release-gate relaxation was introduced. Neither 26.3 nor Earth code was modified.

Staging **124c68f7bd109271c08fb3c094773be45f7d3c6e** contained only a bounded, checksum-guarded source transfer and one-time apply workflow. Apply run **37926602783** checked the exact parent, all 35 preimages/postimages, path membership, traversal/symlink restrictions and clean Git application. Source **354bb9ec206f612489172cf2100b69b30f6d14d4** committed the reviewed changes, removed the transport files/workflow and dispatched normal validation. This application was not a successful mod build.

The code/test patch `TFC-Foundations-26.1.2-livestock-render-state.patch` changes 35 files (1,029 insertions, 368 deletions), is 107,336 bytes, and has SHA-256 **6749db18cba61fae9e447b64a8aac9f751836b02719b66a88cb5b985e9219639**. Packed XZ transport SHA-256: **5de66925ac4cf2ee9a4e8f1fc58c853f8402303372534778c471c3b346dabb38**. The delivered patch does not include later status/log edits and is not a runnable mod.

### Production changes

Eight custom models (cow, yak, sheep, alpaca, musk ox, duck, quail and chicken) move off the removed AgeableListModel through a native EntityModel-based LivestockModel. Their eight geometry factories are byte-identical and protected by source hashes. Pig uses the target QuadrupedModel mesh factory with explicit non-mirrored flags; goat retains its native mesh factory. All ten consume TFCAnimalRenderState, with native pose reset before animation and juvenile transforms. Existing udder/horn/wool/rooster logic is retained. Water-state reuse no longer retains an earlier dry-land sway after reset.

AgeableModelTransforms reproduces scale-then-translation in model pixels after resetting the native pose. Tests compare its matrix against the earlier PoseStack ordering, including translated, rotated and nonuniformly scaled parts. Goat's head/body and pig's grouped-head juvenile layout remain explicit; other livestock retain their old body scale and offsets.

TFCAnimalRenderState adds female-characteristic, genetic-size and wing-phase fields with reset defaults. The extractor captures sex, wool/product and interpolated bird timing. Wing phase is not substituted for native ageInTicks. Pure production LivestockRenderStateMath preserves the clamped genetic multiplier and original wing interpolation formula. AnimalRenderer, GenderedRenderer and OviparousRenderer use the native state path, keeping texture precedence and genetic scaling.

The cat has an independent FelineRenderState-derived snapshot. Native AdultFelineModel setup handles reset, sitting and lying poses; a reviewed custom juvenile transform keeps the adult texture layout. TFCCatRenderer overwrites texture, owner/collar and all custom feline pose inputs each capture, retaining existing external scale/sleep rotation. The deformed collar consumes that same captured state through the native submit layer. Native feline animation is an intentional target adaptation, not a claim of pixel-identical legacy poses.

Cod, salmon, pufferfish and tropical-fish adapters capture the field-guide origin exemption in a native ContextKey extension. It is based on actual entity.position(), overwrites true and false, and respects native render-data reset rather than using interpolated render coordinates. Squid/glowing squid pass the existing adult-layout mesh in both native model slots and retain their matching 64x32 textures. GlowArrowRenderer supplies ArrowRenderState. ClientEventHandler changes are narrowly limited to the migrated model factories and constructors; many unrelated handler errors remain.

### Local execution and regression evidence

```sh
# JAVA_HOME and PATH select the verified exact-target Java 25 JDK.
python tools/porting/compile_main_diagnostics.py
python tools/porting/run_livestock_render_smoke.py
python tools/porting/inspect_api.py
```

The first command is diagnostic-only (`-proc:none`). The initial livestock pass reported 2,289 errors and exposed a remaining live-entity baby predicate in YakModel; it was corrected before publication. The completed final native-renderer pass reports **2,193 errors across 1,640 sources**, exit 1, timed_out=false. A separate clean Git worktree applied the patch with `--check --index --whitespace=error`, verified all 35 postimages, and independently reproduced both the successful new suite and the same final compiler result. Git whitespace checks pass.

The new tracked runner compiles and constructs **11 actual production models**, executes **336 scenarios**, evaluates **3,157 assertions**, and emits **103,680 finite CPU vertices**. Ten livestock models exercise 32 baby/sex/product/water/airborne combinations each, while the cat exercises 16 combinations and matching deformed-collar geometry. Checks cover pose resets, independent roots, juvenile transform ordering, visibility, wing phase, genetic-size clamping, guide-origin stale-state prevention and cat/collar alignment. It also verifies eight retained geometry factories, four actual fish-renderer compilations, four native 64x32 texture layouts and source capture/texture-precedence contracts.

The prior 48-model suite still passes 384 scenarios, now 1,724 assertions, and 152,544 CPU vertices; four extra reset checks reflect the added snapshot fields. Prior standalone checks (173 Java), six Python guards, 62 save-hook contracts, native Brain-order checks and all 114 package-info compilations remain passing. A fresh full local inspect_api invocation completed with explicitly recorded **exit 0**. The clean replay explicitly recorded **PROBE_EXIT=0**, **COMPILE_EXIT=1**. JOML Unsafe deprecation warnings remain visible; they were not suppressed or reported as performance results.

### Independent GitHub validation

[GitHub validation 37926650392](https://github.com/Shnewbs/TFC-Foundations/actions/runs/37926650392), source `354bb9ec`, completed with overall **failure** because main compilation is still broken.

| Check | Observed result |
| --- | --- |
| Full Gradle/main compilation | FAIL: exit 1, 2,193 javac errors; summary groups them into 2,095 path/line/message entries. |
| New headless model suite | PASS: 11 models, 336 scenarios, 3,157 assertions, 103,680 finite CPU vertices. |
| Existing headless model suite | PASS: 48 models, 384 scenarios, 1,724 assertions, 152,544 CPU vertices. |
| Earlier standalone suites and source/API contracts | PASS. |
| Main/data/test license tasks | PASS. |
| Existing resource-validation step | PASS; not in-game visual validation. |
| Downloaded CI source preservation | PASS: all 35 reviewed postimages match; test invocation is tracked and temporary transfer files are absent. |

Artifact **11614218054**, `tfc-port-diagnostics-37926650392-1`, preserves logs, reports and the exact source until October 16, 2026 UTC. Downloaded ZIP SHA-256 **81ce5b3255179136d3e26b677746dc308c3f4a1469f1bae2ce95993661cca6e8** was verified.

Normal validation continues to use the existing full build command, preserving its real failure status:

```sh
./gradlew -I tools/porting/diagnostics.gradle writePortClasspath compileJava compileDataJava compileTestJava build --continue --no-daemon --no-configuration-cache --console=plain
```

The new suite is tracked and invoked by inspect_api.py. A successful headless/API-inspection step cannot override a failed Gradle compilation.

### Remaining compiler and playability boundary

The completed diagnostic reduction is **2,361 to 2,193 (168 fewer)**. Cascading diagnostics are not independent bug counts or a completion percentage. No diagnostics remain in changed model/render-state/renderer paths in the final local traversal, excluding the minimally changed ClientEventHandler. Remaining model errors include horse/chest and mechanical models; ClientEventHandler, LevelRendererExtension, contained-fluid/plant/trim/leaves/mold models and overlays remain prominent clusters.

NOT RUN: live entity extraction, renderer construction, GPU drawing, in-game appearance, client/server startup, actual entities/AI, world creation/save/reload, multiplayer, survival progression or performance measurements. Earlier registry-backed item/fluid/name round trips and bootstrap-dependent execution remain untested. Full downstream data/test compilation and packaging are still blocked by main compilation.

Next work includes horse/chest/native/mechanical and boat models, block/item/block-entity rendering, client registration/overlays, JEI/Patchouli, recipes, equipment/materials, capabilities, schedules and registry/holder/worldgen APIs. No playable JAR, release, CurseForge upload or measured optimization is claimed. Continue using the explicit target and release gates in [PROJECT_STATUS](../../PROJECT_STATUS.md).
