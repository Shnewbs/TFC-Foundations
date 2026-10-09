# TFC Foundations — Work Log

The complete preceding log is preserved byte-for-byte in [WORK_LOG_BEFORE_MODEL_RENDER_STATE](history/WORK_LOG_BEFORE_MODEL_RENDER_STATE.md), which links to the entity, particle/save, common API and loot history. This checkpoint does not upgrade those earlier validation claims.

## 2026-10-09 UTC — Hierarchical animal models and detached render state

### Source and publication

Resumed `26.1.2` at `fb5741b1c1b9918435baef726b395f1acc0756c3` (code `4ea0a94b771091573f461610cf6613378d47b398`, 2,583 reported compiler errors). Public source authorization remains in effect. Minecraft 26.1.2, NeoForge 26.1.2.114, JEI 29.43.0.107, Patchouli 26.1-94, Java 25 and Gradle 9.2.1 pins did not change.

Staging commit **80fd233bc22be98e8ce52cc73c0ab6337f464494** transported the reviewed 68-file patch with SHA-256 verification, all source preimage/postimage checks, bounded decoding, exact path membership and symlink/traversal protection. It also required the exact staging parent and a clean index. Source commit **bcbdf950ff4280199adcfeaa15122cf6bf85e186** applied the patch and removed its one-time transport files/workflow with a normal push. No force push or concurrent-source overwrite was used. This source application was not a successful mod build.

Patch **TFC-Foundations-26.1.2-model-render-state.patch**: 68 files, 211,826 bytes, SHA-256 **645072a44c2b9cbecb92c28def8377e4831a155bf5c2c2d21a8ab230fb6cc53a**. XZ transport SHA-256: **6a69f63db5667883e4f149fda87d7e4fab9664ee4c2f0e00e7797a6cffd19a42**. Clean Git application with whitespace errors rejected reproduces all 68 postimages. The delivered patch contains code/tests, not the later documentation checkpoint or a runnable JAR.

### Model and renderer implementation

The shared HierarchicalAnimatedModel now extends native EntityModel<TFCAnimalRenderState>. Native setup resets the pose. Per-model IdentityHashMap caching bakes each static/constructor-owned animation definition against that model's root, then uses native walk/timeline application. There is no cross-model mutable root cache.

The migrated concrete models consume detached snapshot fields rather than live TFC entities, calendars, brain memories or world access. TFCAnimalRenderStateExtractor captures those values after the native superclass extraction, copies each custom animation timeline into independently owned state, and preserves texture callback ordering. Custom state clears between captures. Seasonal flags, sex/product appearance, movement, owner/collar, sleeping/sitting/climbing, aggression and camel saddle/animation values are captured for the appropriate species. The field-guide origin exemption uses the actual entity position during extraction rather than interpolated render coordinates. Native water-state semantics replace the removed legacy predicate; live-world water/bubble behavior is not independently tested.

SimpleMobRenderer and associated dog/collar, camel, penguin, jellyfish and pest renderers adopt target render-state hooks. Four seasonal bird builder calls in ClientEventHandler receive explicit generic parameters; the rest of that handler remains incomplete. No item-in-mouth layer or unfinished climbing behavior is newly claimed. The dog collar submission retains native outline handling; its integer submission-order argument was checked against target bytecode.

Geometry factories and declared keyframe definitions remain guarded by 48 source contracts. The dog is an explicit reviewed factory migration to AdultWolfModel.createBodyLayer(CubeDeformation.NONE), retaining a 64x32 LayerDefinition and the verified native adult/collar textures. Its old prepare/setup animation phases are combined after pose reset. The camel baby transform preserves the old scale-then-translation matrix and returns to the adult pose after reset. Redundant jellyfish/manatee/orca draw overrides are replaced by native root rendering; native supplied tint is retained. These are source/CPU geometry checks, not a rendered screenshot comparison.

### Missing animation bones found by execution

The expanded real-model test initially failed when the target's strict keyframe binder encountered Bongo's missing tail1 bone. Other models sharing definitions had analogous legacy omissions. A temporary diagnostic filter was used to enumerate the cases, then replaced before publication with explicit per-model allowlists. The final code does not silently skip arbitrary missing names or accept an entirely incompatible nonempty animation.

Reviewed omissions: Bongo tail1; Caribou right_hind_leg; Deer tail1; Gazelle tail1; Grouse neck1/snood; HorseshoeCrab antena1/antena2; Hyena tail2; Jerboa 1/2; Peafowl snood; Wildebeest tail1. They are recorded in the contract manifest and model overrides. This retains the old lookup's behavior for those known cases, not a claim that the absent bones have been added or animated. Tests deliberately exercise rejection of unknown names and wholly incompatible animations.

### Local tests and limitations

Used the previously resolved exact Java 25 JDK and 84-JAR target classpath; the offline archive and JAR hashes were verified. No Minecraft/TFC stubs, placeholder model substitutions or excluded core systems were introduced.

```sh
# JAVA_HOME and PATH select the exact target's Java 25 JDK.
python tools/porting/compile_main_diagnostics.py
python tools/porting/run_model_render_smoke.py
python tools/porting/inspect_api.py
```

The first is diagnostic-only (-proc:none). Completed main passes moved from 2,583 to 2,365, then **2,361** after the reviewed seasonal-renderer fixes. The final edited source and a separate clean Git replay each cover **1,635 main sources**, exit 1, timed_out=false, 2,361 errors. The minimally changed ClientEventHandler remains erroneous; the migrated model/state/renderer files have no diagnostics in this traversal. Counts include cascades and are not fixed-bug counts, completion percentages or guarantees that every downstream error is visible.

The new standalone runner compiles and constructs **48 actual production models**, applies **384 model/scenario combinations**, evaluates **1,720 assertions** and checks **152,544 finite CPU vertices** using the native model path and a counting VertexConsumer. It checks pose reuse/reset, independent model roots and baked animations, bison charge, speed clamps, baby/adult camel transform order, dog sitting/head-roll, and strict missing-bone validation. The runner verifies 48 geometry/keyframe source contracts, explicit omission lists, extraction/reset/copy ordering, and two actual native texture dimensions. It is tracked and called by inspect_api.py.

The 173 prior standalone Java checks and six Python guards passed locally. One combined local inspect_api invocation was stopped by the host's 45-second command limit after the prior suites passed, before the model suite completed; it is NOT counted as a successful overall invocation. The model suite ran separately to completion and passed, then passed again after clean Git replay with all 68 source hashes verified. The clean replay also repeated the entity suite successfully. GitHub independently executed the entire inspection/probe step successfully below.

### Independent GitHub validation and preservation

[Run **37889572978**](https://github.com/Shnewbs/TFC-Foundations/actions/runs/37889572978), source **bcbdf950ff4280199adcfeaa15122cf6bf85e186**, finished with overall **failure**:

- FAIL: full Gradle/main compilation, exit 1, **2,361 javac errors**. The summary groups them into **2,250 path/line/message entries**. This matches the completed local raw count; these are different diagnostic measures.
- PASS: the complete API-inspection/probe step, including all 173 prior Java checks, six Python guards, 62 save-hook contracts, native Brain order checks, and prior particle/base/save utility compilation.
- PASS: all 48 new concrete models, 384 scenarios, 1,720 assertions and 152,544 CPU vertices; model/keyframe and native texture checks independently reproduce the local result.
- PASS: all 114 package-info files compile separately; main/data/test license tasks pass.
- PASS: the existing resource-validation step. This is not GPU or in-game visual validation.
- PASS: downloaded CI source matches every one of the 68 reviewed postimages, and no temporary model transfer files/workflow remain.

Normal CI command remains unchanged:

```sh
./gradlew -I tools/porting/diagnostics.gradle writePortClasspath compileJava compileDataJava compileTestJava build --continue --no-daemon --no-configuration-cache --console=plain
```

Artifact **11598051560**, `tfc-port-diagnostics-37889572978-1`, retains logs, reports and exact source until October 16, 2026 UTC. Downloaded ZIP SHA-256 **615759954bed4906c6ac5d5a8d7fbf615380ffc5616f2464e30dca512ab4cec3** was verified. The archived source includes the tracked new test invocation and all reviewed source changes. Passing diagnostic/model tests do not override the failed build.

### Remaining work and release boundary

NOT RUN: live TFC entity extraction, GPU rendering, in-game model/texture appearance, client/server launch, actual entity ticking, world save/reload, multiplayer, survival progression or performance measurements. Registry-backed item/fluid/name round trips and earlier bootstrap-dependent runtime probes remain unexecuted. Full downstream data/test compilation and packaging remain blocked.

Remaining work includes other livestock/native entity models and renderers; block/item/block-entity rendering; ClientEventHandler/overlays; recipes, equipment/materials, capabilities, TFCBrain schedules, registry/holder/world-generation APIs; and JEI/Patchouli integration. No model test substitutes for those systems or for the compiler/playability gate.

The completed raw diagnostic reduction is **2,583 to 2,361 (222 fewer)**. No 26.3 source, Earth generation, version increment, playable JAR, GitHub release, CurseForge publication or measured performance improvement is claimed. Continue with the targets and release gates in [PROJECT_STATUS](../../PROJECT_STATUS.md).
