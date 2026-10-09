# TFC Foundations — Work Log

The complete previous work log is preserved byte-for-byte in [WORK_LOG_BEFORE_VISUAL_SAVE_API](history/WORK_LOG_BEFORE_VISUAL_SAVE_API.md). It records the public-source decision, dual targets, loot migration, exact-target compiler setup and the 346-file common API checkpoint. The earlier initial history is also retained in [WORK_LOG_BEFORE_LOOT_MIGRATION](history/WORK_LOG_BEFORE_LOOT_MIGRATION.md). This log continues those records; it does not replace or upgrade their validation claims.

## 2026-10-09 UTC — Particle and block-entity save API continuation

### Source and publication

Resumed public branch `26.1.2` at `47839e6fbba714ce9c5ac5e9bb1222636a3b28cc`. The last code/test checkpoint was `8d27f34a1d76535f6b359b5151295ff8fc16f0e6`; its full Gradle compilation reported 3,438 errors. The user's public-push authorization remains in force. Target and dependency versions did not change.

- `f6e34d0c7fe831059d2b3afa09986e7c97b7823b`: staged a checksum-guarded, one-time source transfer on the existing branch.
- `46401c030a6d2a6e8d9721147837237d8b03605d`: applied the reviewed 72-file particle/save checkpoint, removed the temporary transfer files and workflow, and dispatched normal validation.
- No force push, branch deletion, version increment, release or CurseForge upload was performed. The 26.x/26.3 source and Earth work were not changed.

All 72 file preimages and postimages were checked. The exact patch passed `git apply --check --index` and a clean replay with whitespace errors rejected. The downloaded CI source archive was then checked against the final manifest and local source; every reviewed file matched and all temporary transfer paths were absent.

### Production changes

**Particles:** 14 production source files were migrated from removed TextureSheetParticle/render-type APIs to the exact target SingleQuadParticle, sprite construction, provider RandomSource and layer interfaces. The patch keeps motion and tint logic where the APIs permit, rather than replacing particles with empty implementations. Falling leaves use the target vanilla falling-leaf constructor verified from the resolved target bytecode; the old vanilla motion is not assumed identical. Fluid-drip light behavior keeps its existing glowing light value while adopting the target method name. Actual client appearance and rendering are not tested.

**Block-entity persistence:** 89 old save/load hooks across 45 classes now use ValueInput/ValueOutput. Related base/interface files, nested inventory serializers, pot-output codecs, component readers and synchronization hooks were updated. Inventory constraints now use ValueIOSerializable; native ItemStackHandler serialization and custom composite inventories keep the relevant nested item layout. Pot outputs and inventory names clear stale state when those fields are absent on a later synchronization.

The TFCBlockEntity base now calls superclass saveAdditional/loadAdditional, preserving NeoForge's data/attachment hooks. Its onDataPacket/handleUpdateTag signatures match the target and continue delegating through the superclass. The bytecode probe checks these four delegations. This is not a live attachment round-trip test.

**Strict and legacy reads:** ValueInput overloads in NbtHelpers preserve explicit numeric tag kinds and reject mixed lists where the older format required homogeneity. ValueIoHelpers keeps list positions when an individual codec value is invalid, handles optional item stacks, and accepts legacy JSON custom names alongside native component representations. Source checks found no unexplained scalar save-key loss across the migrated hooks. Crucible alloy decoding retains its codec field layout and now defaults invalid/missing decoded alloy data to empty; compatibility with corrupt or modded worlds still needs runtime validation.

**Fluid layout correction before publication:** review found an initially flattened tank writer would not preserve NeoForge's 1.21.1 layout. The primary source at `neoforged/NeoForge`, commit `a2d6402a3c1eec093aef7e7d10ac5145906c199e`, `src/main/java/net/neoforged/neoforge/fluids/capability/templates/FluidTank.java`, confirms writeToNBT writes a nested `Fluid` child. Exact target bytecode also stores `Fluid`. The published writer therefore delegates to `tank.serialize(output.child(key))`. The reader accepts the canonical wrapped format and an additional flat-stack fallback. The test fixture and emitted-bytecode assertion were corrected before the final patch was generated. The tests do not execute a registry-backed fluid round trip.

Core gameplay, visual systems, guide sources, identifiers and release gates remain enabled/preserved. FluidTank's deprecation warnings remain visible; the wider capability port is still required.

### Exact tools and commands

Local checks use the previously verified Java 25 compiler and all 84 JARs from the exact pinned Minecraft 26.1.2 / NeoForge 26.1.2.114 / JEI 29.43.0.107 / Patchouli 26.1-94 classpath. No Minecraft or TFC stubs were introduced.

```sh
# JAVA_HOME and PATH must both select the resolved Java 25 JDK.
python tools/porting/compile_main_diagnostics.py
python tools/porting/inspect_api.py
```

The main-source runner records exit status and timeouts and uses `-proc:none`; it remains diagnostic-only. Local completed passes reported 2,995 errors after particle migration and 2,920 after the save migration. The final corrected tank writer was retested with the same completed 2,920 result across 1,632 main Java files. There were no particle-package diagnostics; 33 raw block-entity diagnostics remain, mostly unrelated inventory/holder/particle-constructor APIs. Counts are not gameplay progress or independent bugs.

One standalone loot invocation initially used the host Java executable and failed because it could not target release 25. Rerunning with both JAVA_HOME and PATH pointing to the resolved JDK passed. The final complete inspect_api invocation passed all standalone suites; the failed environment attempt is not treated as a source regression or a successful test.

Normal GitHub validation continues to run:

```sh
./gradlew -I tools/porting/diagnostics.gradle writePortClasspath compileJava compileDataJava compileTestJava build --continue --no-daemon --no-configuration-cache --console=plain
```

`inspect_api.py` invokes the existing common and loot suites plus the new `run_visual_save_smoke.py`, propagating any failure. The new script compiles actual production classes against the exact classpath, executes SaveIoSmoke, checks emitted bytecode delegation contracts and rejects remaining legacy APIs in the migrated particle/save paths. The normal build still fails when compilation fails; passing probes do not override that outcome.

### Independent GitHub validation

[Run 37881633084](https://github.com/Shnewbs/TFC-Foundations/actions/runs/37881633084), testing source `46401c03`, completed with overall **failure**:

- **FAIL:** full Gradle build/main compilation, exit 1; javac reports 2,920 errors, independently matching the final local diagnostic pass. The summary groups them into 2,795 path/line/message entries, including 32 grouped block-entity entries. Do not conflate these grouped entries with 33 raw block-entity messages or the full javac count.
- **PASS:** 51 existing common utility/API checks, four actual production loot codec checks, and 49 new real NBT/ValueIO checks: **104 standalone checks total**.
- **PASS:** separate compilation of seven actual production particle classes, TFCBlockEntity, NbtHelpers and ValueIoHelpers. All 113 main/data/test package-info files also compile in the common probe.
- **PASS:** four superclass save/sync delegations and delegation to the native tank serializer verified in emitted bytecode.
- **PASS:** main/data/test license tasks and the existing resource-validation step.
- **NOT RUN:** client rendering, actual block-entity/world save/reload, registry-backed item/fluid/name round trips, bootstrap-dependent chunk/component/provider execution, multiplayer, survival progression or performance measurements. Downstream full data/test compilation and packaging remain blocked by main compilation.

The 49 new executed checks test NBT/ValueIO utilities, type/list handling, defaults and generic nested fixtures. The inventory/tank fixture structure checks are not real inventory/tank registry round trips. The successful separate particle/base compilation is not full-mod compilation or mod loading.

Artifact **11594941066**, `tfc-port-diagnostics-37881633084-1`, contains build/probe logs, reports and the exact source snapshot, with seven-day retention ending October 16, 2026 UTC. Downloaded SHA-256 **381b4560e4bccc60b09fa733d5ac9898eef33d1803e8d721e995ec1b47fb3169** was verified. The public source snapshot reproduces every reviewed changed file byte-for-byte.

The delivered source patch `TFC-Foundations-26.1.2-visual-save-api.patch` is 202,115 bytes, with SHA-256 **8194497a201bbe22bc6b83fdea09ac8c69826c72c512bfab46e63e2fbdecf1ca**. It contains the 72-file code/test checkpoint; it is not a runnable JAR or release.

### Remaining milestone

The completed compiler count decreased from 3,438 to 2,920, or 518 fewer diagnostics. Rendering/model and ClientEventHandler migration, entity save hooks, equipment, capabilities, registry/holder and world-generation APIs, and JEI/Patchouli integration remain major blockers. No error count is a completion percentage. The next milestone is still a fully compiled 26.1.2 build, followed by actual client and server playability testing.

The prior common API and earlier loot patches are not blindly copied to 26.3: the exact target APIs diverge. No 26.3 source or Earth generator was implemented during this checkpoint. No playable JAR, successful client/server startup, performance improvement or release is claimed. Current direction and validation limits are summarized in [PROJECT_STATUS](../../PROJECT_STATUS.md).
