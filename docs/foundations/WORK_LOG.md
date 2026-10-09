# TFC Foundations — Work Log

The preceding log is preserved byte-for-byte in [WORK_LOG_BEFORE_MECHANICAL_BOAT_EQUINE](history/WORK_LOG_BEFORE_MECHANICAL_BOAT_EQUINE.md). Its earlier compiler, livestock, model, entity and save checkpoints retain their recorded validation limits.

## 2026-10-09 — Mechanical, boat and equine rendering

### Source and publication

Resumed public `26.1.2` at `c51690797b4706969c7359698dfe3b13da0845af`, following source `354bb9ec206f612489172cf2100b69b30f6d14d4`. The downloaded preceding artifact `11614218054` was checked against SHA-256 `81ce5b3255179136d3e26b677746dc308c3f4a1469f1bae2ce95993661cca6e8`. Its source supplied the local baseline; the later remote documentation-only checkpoint was preserved. The Java 25 toolchain and all 84 resolved JAR hashes were reverified. No dependency pins changed.

Staging **ddc33f29f97d2fe858c28cb3a5a9f2d9ce4d6f96** transported 23 reviewed source/test files. Apply run **37973174520** verified exact ancestry, all preimages/postimages, path membership, bounded decoding and a clean Git application before a normal push. Source **5fbcf6623b81fd06d7e4c250772b1880684d673e** contains the applied patch and removes the temporary transport files/workflow. Source application is not a successful mod build.

Patch `TFC-Foundations-26.1.2-mechanical-boat-equine.patch`: **23 files, 1,124 insertions, 445 deletions, 108,861 bytes**; SHA-256 **7065205bfd8a59a81bf587445c5d254f6324933c51dddbf1c2b27cd78d044df3**. Packed transport SHA-256: **1072af58550cda7493db89b292c629b0487275492103061806ef32ee3afacd14**. No force push, branch deletion, version increment, core-system exclusion, release or publication-gate relaxation was performed. Neither 26.3 nor Earth code changed.

### Production changes

**Rotating machinery:** waterwheel and three windmill model factories retain their exact prior source bodies. Their model setup consumes angle/pass values rather than live block entities. Windmill frame, dyed blade and rustic extras use separate immutable `BladePose` submissions, with explicit white/dyed/white tints; extras remain restricted to a full identical five-blade set. The factory-identity model cache is renderer-local rather than retaining a render context in a global memoized factory. The captured blade list is immutable and cleared before each extraction. This guards against stale state and last-pose reuse in deferred rendering, but does not establish actual GPU queue behavior or performance.

Axle and bladed-axle submissions use captured sprite UV bounds and primitive light/rotation values. Three legacy face-generator method bodies and their winding remain unchanged, as do the reviewed bounds and axis transform order. Custom geometry callbacks capture their needed values instead of dereferencing a live block entity. Waterwheel/windmill offscreen behavior and infinite bounds are retained, not claimed as a culling optimization. Native extraction and model break-progress inputs are preserved where used.

**Boats and carried chests:** TFCBoatRenderer now uses the native AbstractBoatRenderer path for both ordinary and chest boats. Hull selection retains the palm raft case. The new BoatChestModel traverses the native chest boat/raft root but emits only the three chest parts; it does not draw a second hull or paddles. Chest texture is captured from the carried item and cleared before reuse, retaining the prior oak fallback. The attachment is submitted inside the native yaw/damage/bubble transform path. The water mask remains absent for rafts and underwater boats. Geometry tests check native chest shape/UV identity, not a screenshot of moving boats.

**Equines:** ordinary horses use target adult/juvenile horse models, markings and native body-armor/saddle layers with the existing scale and all seven variant texture selections. Donkeys/mules have an independently posed carried-item layer, preserving registered barrels and other supported carried items rather than narrowing visibility to vanilla wooden chests. Texture and presence are overwritten on extraction; body and carried-item passes have separate roots. Native saddle layer registration is added for donkeys/mules.

**Explicit juvenile visual adaptation:** chested donkeys/mules use a half-scale adult-layout model with a root offset, retaining the adult texture and carried chest geometry on juveniles. Native BabyDonkeyModel inspection showed missing chest geometry and mutation of its input xRot during setup; it was not substituted into the shared body/attachment path. The implemented juvenile proportion is a deliberate adaptation, **not verified identical to the legacy juvenile anatomy**. Actual in-game proportions and saddle/carried-item appearance remain a visual review gate. CPU tests verify alignment, reset behavior, geometry presence and nonmutation of the chosen production setup's state, not aesthetic fidelity.

ClientEventHandler changes are limited to these model factories, layers and constructor signatures. Its unrelated registration/rendering failures remain. New tests and their inspect_api invocation are tracked in source.

### Local validation and clean replay

```sh
# JAVA_HOME and PATH select the verified exact-target Java 25 toolchain.
python tools/porting/compile_main_diagnostics.py
python tools/porting/run_mechanical_boat_smoke.py
python tools/porting/inspect_api.py
```

Baseline diagnostic compilation completed with **1,640 sources, 2,193 errors, exit 1, no timeout**. Final edited compilation completed with **1,644 sources, 2,103 errors, exit 1, no timeout**. A separate clean Git worktree applied the source patch using `--check --index --whitespace=error`, matched all 23 postimage hashes, repeated the full standalone inspection successfully (**probes=0**) and reproduced the failed diagnostic compiler result (**compiler=1**, 2,103 errors). These main passes use `-proc:none`, not the complete Gradle/mixin/data/test pipeline.

The new suite passes **261 mechanical/boat/equine scenarios, 3,294 assertions and 37,416 finite CPU vertices**. It constructs production machinery/chest/attachment models and relevant native horse geometry, checking independent poses, resets, tint separation, chest-only emission and UVs, attachment alignment, juvenile transforms, source-state nonmutation, and axle winding/matrices. Immutable submission values are replayed into real model setup; no live renderer or GPU queue is executed. Separate javac compilation checks the production boat/horse adapters and supporting state/geometry/layer classes without game/TFC stubs.

Four retained geometry factories, three legacy face-method bodies, capture/submission contracts, 16 native equine texture dimensions and all TFC hull/carried-chest texture dimensions also pass. Earlier suites remain passing: **173 standalone Java checks, six Python guards**, save/brain contracts, 114 package-info compilations, the **48-model suite** (384 scenarios, 1,724 assertions, 152,544 CPU vertices), and the **11 livestock/cat suite** (336 scenarios, 3,157 assertions, 103,680 CPU vertices). Deprecated JOML Unsafe warnings remain visible.

### Independent GitHub evidence

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

The standard build command remains:

```sh
./gradlew -I tools/porting/diagnostics.gradle writePortClasspath compileJava compileDataJava compileTestJava build --continue --no-daemon --no-configuration-cache --console=plain
```

### Remaining compiler and playability boundary

The locally and independently CI-confirmed reduction is **2,193 to 2,103 (90 fewer diagnostics)**. Cascading compiler messages are not independent bugs or a completion percentage. Changed source paths have no diagnostics except the narrowly adjusted, still-incomplete ClientEventHandler. Client registration, world rendering, contained-fluid/plant/trim/leaves/mold block/item models, overlays and equipment/material code remain prominent clusters, with recipes, capabilities, schedules, registry/holder/worldgen and JEI/Patchouli work outstanding.

NOT RUN: renderer construction, live state extraction, native GPU submission, in-game visual/proportion checks, actual client/dedicated-server launch, world generation/save/reload, multiplayer, survival progression or performance measurements. Registry-backed item/fluid/name round trips and earlier bootstrap-dependent probes remain unexecuted. Full downstream data/test compilation and packaging are still blocked by main compilation. No playable JAR or release is claimed.

Continue with the targets and release gates in [PROJECT_STATUS](../../PROJECT_STATUS.md).
