# Continue development in another environment

## 2026-10-17 UTC — Uploaded build report resolved; full build succeeds

The uploaded Windows log contains 44 main compiler errors from published
checkpoint `7b5715acd23da8b97ed012c1b66d666e50c0de96`. All 44 are resolved in
this source: 39 retired item/fluid capability references, two block removal
callbacks, recipe result access, native item component defaults and the wolf
howl sound. Native transactions retain TFC inventory, fluid, temperature,
container replacement and rollback behavior.

Verification of the repaired tree: diagnostic javac compiles 1,699 sources
with zero errors (exit 0, no timeout); pinned full Gradle build succeeds
(47 seconds); 226 JUnit tests are recorded, **214 passed and 12 skipped**,
with zero failures/errors. All 36 API runners pass. Native data generation
also succeeds (29 seconds). The regenerated recipe corpus uses NeoForge's
registered `neoforge:never` condition to keep vanilla recipes disabled.
Generated-resource audit records 2,679 syntax-equivalent changes, one guide
config formatting difference and the required new vanilla override/tag files.
The per-error audit is in
`docs/foundations/validation/2026-10-17-uploaded-build-error-audit.json`.

An actual `TFC-Foundations-26.1.2-0.0.0.jar` is produced (69,055,991 bytes;
SHA-256 `c0d05d2fc648b6bd2155a205658582a7e20893f2c4e8a8e6ecdd5ae8858d940d`).
It is an unverified development build, **not a playable release**. Client and
dedicated-server startup, actual GPU/resource reload, world generation,
save/reload, multiplayer and survival progression remain unverified. A native
runtime launch attempt stopped during loader/display initialization; it did
not establish startup or gameplay success. Dependencies and candidate 0.0.0
remain pinned. No new branch is created; the independent 26.x track is unchanged.

## 2026-10-17 UTC — Native item models, predicates and color layers

Continued directly on `26.1.2` from `d6ed9c94556e8ce15d41d27aee965cdab3a916f3`, whose published checkpoint records 93 main compiler errors. Current pinned Java 25 full Gradle and diagnostic javac report **44 main errors**, diagnostic source count **1,680**, exit 1 with no timeout. This is a diagnostic count, not a completion percentage. Main compilation still blocks data/test compilation and packaging; **no playable JAR**. Dependency pins, candidate `0.0.0`, gameplay/resource IDs, branch policy and upstream licenses remain unchanged.

Replaced removed item property registrations with native numeric codecs for TFC rod casting, player shield blocking, active/aggressive javelin throwing, the handstone's strict last-ten-durability threshold, sealed contents/nonempty barrels and clamped blowpipe heat. The generator converts the inspected separate-transform and ascending single-property override models into native context/range selectors. It retains the original model references, threshold equality and fallback paths, all nine ordinary display contexts, held/throwing javelin geometry/materials, normal/trapped wood chest textures and filled-pan stage selection. Unknown loaders/properties and author-maintained selectors remain protected. The obsolete BEWLR extension/chest classes are removed after replacing their behavior with native model selection and the native chest renderer.

Native item definitions now cover **5,317 static + 66 fixed-fluid + 60 TFC fluid/trim + 170 special/predicate** models, **5,613 of 5,614** legacy item-model files. These are model-file counts, including helpers, not registered-item counts. The one explicit remaining migration is the authored `grass_inv` tint helper. The pan model owns its stage cache per resource reload and emits native baked quad layers during extraction. Javelins use TFC's actual javelin model and native submission, foil and extent handling. No live world/entity is captured for deferred drawing.

Block tint registration now supplies both authored face indices as native sources, including default/world/terrain-particle paths and the snowy state property for native color caching. Original grass, tall grass, seasonal/conifer foliage, krummholz snow suppression, fallen leaves, crop and water callbacks remain. Legacy item vegetation colors enter native item-state layers only when no authored native tint is present; their two index values and model identity are retained, with the existing rotten-food fallback after registered colors. The dromedary adapter transfers captured camel movement, head, jump cooldown and all five animation timelines to the native model while retaining the existing simple renderer/baby scale. Both minecart registrations use the current non-generic renderer.

Validation: **14,544 original/native selector decision comparisons** pass, including boundary values and nine display contexts; exact special textures and generated-file determinism are checked. **90 native tint-layer/default/world/particle/identity/reuse assertions** and 10 hook descriptors/anchors pass; all authored TFC face tint indices are confirmed to be 0 or 1. The livestock suite passes **32 additional native/TFC camel pose/reuse/CPU geometry scenarios**, with 3,285 aggregate assertions and 112,896 finite CPU vertices. Resource/generated-texture validation, all three license checks, dependency verification and selector generation pass; assets are up-to-date in the full build. The final aggregate passes **all 35 API runners (exit 0)**; results, source/JAR hashes and limits are recorded in `validation/2026-10-17-item-model-api.json` and the 44 remaining diagnostics in `validation/2026-10-17-item-model-errors.json`.

The first aggregate run encountered the new tint fixture's use of `BlockStateProperties.SNOWY`, which initializes unrelated sound registries without a game bootstrap. The corrected CPU fixture uses a real native BooleanProperty and separately checks the production snowy-property bindings; no native class is stubbed. The final aggregate is rerun after the fixture correction. Host-only NeoForm Java-command fallback and trusted proxy CA remain in use as documented in the preceding checkpoint.

Remaining main errors are in transfer capability registration/lookups, component defaults, food recipe outputs, the wolf howl sound and two block-removal callbacks. NOT RUN: game/model reload bootstrap, transformed Mixins, actual GPU drawing, tint/foil/chest/pan appearance, client/server launch, world generation, save/reload, multiplayer or survival. The independent `26.x` track is unchanged by this target-specific checkpoint.


## 2026-10-17 UTC — Native environment rendering and JEI callback checkpoint

**Final verification:** pinned full Gradle and diagnostic javac both report 93 main errors; all **35 API runners pass (aggregate exit 0)**, including the 15,611 new sky CPU assertions. Resource validation/generated textures, all three license checks and dependency verification pass. Asset retry succeeds; the final full build reuses those assets and fails only main compilation. Data/test compilation and JAR packaging remain blocked. Exact remaining diagnostics, runner exits, source/JAR hashes and test limits are recorded in `docs/foundations/validation/2026-10-17-environment-api.json` and `2026-10-17-environment-errors.json`.

Continued directly on `26.1.2` from `7017226aeef4a63d447729a8b9e8c30666b480d4`. The exact parent reproduces **203 main errors** in pinned Java 25 Gradle and diagnostic javac. Final source reports **93 main errors** (1,678 sources; diagnostic exit 1, no timeout). Error counts include cascading diagnostics and are not a playability percentage. Dependency pins, candidate `0.0.0` and all gameplay/resource IDs remain unchanged.

Sky/weather now use NeoForge's native custom-environment interfaces and render-state extraction. Rain/snow meshes freeze vertex/UV/color/light values before GPU submission; drawing does not retain a level or camera. The registered native pipelines retain colored catalog stars and opaque celestial covers. Catalog reload publishes immutable CPU geometry; native star buffers are replaced and closed on the render thread, including empty-catalog reloads. Sun/moon use the current celestial atlas and all eight moon phase sprites, retaining TFC's independent solar/lunar transforms and original sizes. Rain intensity, wind, temperature, textures, particle and sound calculations remain; the two full precipitation vertex bodies and the original star/rotation bodies have frozen preservation contracts.

Climate colormaps enter the native attribute system after biome layers and before timeline/weather modifiers. The custom sky/weather identifiers replace the old overworld-effects registration. Obsolete `getDayTime`/`dayTime` hooks move to the inspected native clock callback, with a guard against client fallback recursion. Precipitation wrapping targets the native method and sea-level argument; the world-selection warning and WorldGenContext callbacks retain their current parameters, including the unsaved-chunk listener. The obsolete LevelAccessor mixin is removed after rehoming its clock behavior.

JEI uses synchronized recipe-map enumeration, native ingredient streams, crafting-station roles, the current recipe-type interface and client packet distributor. The trim command reads the native material component and resolves patterns from data-driven smithing-trim recipes. Vivecraft's side check uses the current loader method. An unused Helpers import left by upstream integration was removed from TFCBlockEntity so its isolated save/particle probe can compile.

The new production-geometry suite passes **15,611 CPU assertions**, including native buffer layouts, finite immutable star geometry and transform checks. Native callback/bytecode anchors, attribute-layer order and frozen complete rain/snow vertex bodies pass. These are not rendered frames or transformed Mixin tests. The suite is registered with the existing API runners, which now write individual exit codes to a machine-readable summary.

Host setup: official OpenJDK 25 build 25+36, Gradle 9.2.1 with verified distribution hash, unchanged pinned target libraries. The existing opt-in local NeoForm 2.0.31 Java-command fallback and this host's trusted proxy CA are required. Initial native-buffer tests lacked host native JARs; the final runner loads the real matching resolved LWJGL natives. The initial geometry radius tolerance assumed exact trigonometry; the final tolerance accounts for Minecraft's native approximation. The first aggregate API pass found the unused import above. The first full build also failed the separate Java 21 asset task's local certificate check; the local trust store was corrected and that task retried without weakening TLS or build gates.

**No playable JAR.** Main compilation still blocks data/test compilation and packaging. Remaining clusters include native item predicates/special renderers, block/item tint registration, inventory/fluid capabilities, component defaults, recipe output handling and remaining removal callbacks. NOT RUN: live GPU pipelines, visual/color and native timeline timing, transformed Mixins, resource reload in a running world, startup, world/save/reload, multiplayer, survival or performance measurements. The separate `26.x` upstream integration is published as `64bc0554bf820464cbefb57c551b3007b6298a1f`; its full compilation remains unverified and Patchouli unresolved.


**Checked:** 2026-10-16. **Repository:** [Shnewbs/TFC-Foundations](https://github.com/Shnewbs/TFC-Foundations).
This file is the handoff entry point for another computer, operating system, IDE, or ChatGPT/Codex session. **Always consult the live branch and its latest CI run before treating the checkpoint below as current.** Do not rely on local or chat-only work being available elsewhere.

## Branches and pinned targets

| Branch | Purpose | Minecraft | NeoForge | JEI | Patchouli |
| --- | --- | --- | --- | --- | --- |
| `26.1.2` | **First playable priority** | `26.1.2` | `26.1.2.114` | `29.43.0.107` | `26.1-94` |
| `26.x` | Independent experimental track to eventually move to a released/verified `26.4` | `26.3` | `26.3.0.58-beta` | `31.9.0.61` | **Unresolved for this target** |

Both tracks use **Java 25**, Gradle wrapper **9.2.1**, and ModDevGradle **2.0.148**. Branch-specific `gradle.properties` is authoritative if a dependency is changed after this handoff. Current candidate version is `0.0.0`, **not** a tested release. Preserve upstream TFC gameplay, the EUPL notices, credits, identifiers, and full feature set; never hide missing APIs behind dummy implementations or disable subsystems to manufacture a green build.

## 2026-10-16 UTC — Upstream integration through c3b4d953

Ported the complete 42-commit `TerraFirmaCraft/TerraFirmaCraft` 1.21.x delta from initial base `e9d9a88a` through `c3b4d9536b82e5a9746dd793c586720a74108057` onto **both existing branches**, preserving upstream EUPL notices and authors. Ten commits became reachable since the October 12 observation. This is a source port with explicit per-file audit, not a Git ancestry merge. `validation/2026-10-16-upstream-integration.json` records all 348 diff paths, SHA/author/subject provenance and native equivalents. Upstream AGENTS.md/.gitignore do not replace our project controls. No dependency pin, version candidate or public branch changed.

Included: buoy/lobster-trap content, all 16 colors and their recipes/textures/loot/tags/guide pages; kiln catch-up heating and stale-device guards; banana and berry corrections; squid spawn cancellation; scraping progress/durability and texture safety; default soup ordering; rope-anchor orientation; fluid-loggable tool racks and offhand interactions; pet home/sitting AI; ten vanilla armor trim palettes, thatch spawn prevention, soda ash from beach seaweed and translations. The newly observed 1.20.x tip `e39a6531` adds five reachable commits affecting scraping/farmland; their behavior is covered by the newer 1.21.x changes. Legacy branch-specific architecture is not blindly merged.

Both branches pass resource/generated-texture validation and the new upstream regression runner: **2,097,152 production scraping-mask CPU checks**, source guards/fallback ordering, all JSON parsing, buoy resources, thatch tag and trim palettes. These are not world/AI/rendering tests.

On **26.1.2**, continued from `f49876a1`: main diagnostics **220 → 203**, independently confirmed by full pinned Gradle and diagnostic javac (1,677 sources, exit 1, no timeout). New fishing renderers use native extraction/captured geometry/items; new block entities use ValueInput/ValueOutput and native removal. Rope lighting/bounds, buoy wind animation and catch placements remain. Existing reload-local mold and null-safe scraping snapshot implementations provide the upstream equivalents. Also migrated placed-item/shelf/pit-kiln rendering to the native snapshot framework and supplied JEI's required native ingredient displays. Custom placed models retain cutout/translucent sheets, original slot transforms/rotations and pit-kiln stage cutoff; native usesBlockLight identifies ordinary three-dimensional item placement, with the existing shield/javelin/trident exceptions. Actual appearance still needs client verification.

All **34 registered API runners pass individually** after updating four upstream-sensitive source/fixture contracts (new item count, tool-rack hand handling, accepted interaction body and boolean scraping callback). The aggregate rerun had one stale scraping-signature failure; that runner subsequently passed after correction; no production change was needed. Existing 9,074 display/surface and 584 item-layer CPU assertions remain passing. Native selectors now cover 5,386 static + 66 fixed-fluid + 60 TFC fluid/trim definitions; 102 special models remain pending. All three license checks, dependency verification and resource/asset preparation pass in the pinned full Gradle attempt. Host NeoForm Java-path fallback and local proxy trust remain in use.

**No playable JAR.** Main compilation blocks data/test compilation and packaging. Remaining largest clusters include sky/weather (82), client registration (29), pan special rendering (9), other special item renderers and transfer capabilities. No transformed Mixin, GPU, resource reload, client/server launch, world/save/reload, multiplayer, survival, trap movement/drops, pet AI or kiln loaded/unloaded runtime verification. Exact main errors are in `validation/2026-10-16-upstream-errors.json`. `26.x` independently remains an incomplete 26.3 port.

## Latest verified continuation — 2026-10-16 UTC

Work directly on `26.1.2`; no task branches. Published parent `7a63ac8a` independently reproduces **261 diagnostic errors**. Final client-hook source was validated from an isolated staged-tree archive: **220 main errors** in both diagnostic javac and full pinned Gradle (1,669 sources, diagnostic exit 1, no timeout). This continuation's two checkpoints reduced main diagnostics **359 → 261 → 220**. All 33 API runners pass, including 78 tint-layer CPU assertions, 10 native method/anchor checks and the prior 584 item-layer / 9,074 display assertions. Resource/generated-texture validation, main/data/test licenses, dependency verification and asset gate pass. See `validation/2026-10-16-client-hooks-api.json` and `2026-10-16-client-hooks-errors.json` plus the first project-status/work-log entry.

Native item state now carries rotten-food tint and its model identity; item-break particles consume native tint layers. The obsolete ItemColors/Sodium hook is replaced by that common path. Bait, pan hand drawing and third-person glass blowing use native extraction/submission. Rain and mixing-fluid Mixin targets are current, and skeletons retain their overhead javelin pose through native arm state. Native input and cutout-leaves options replace removed helpers. Method/field/bytecode/CPU checks do not establish transformed Mixin or live rendering success. Optional Sodium behavior, hand/bait/tint appearance and graphics/input still need in-game tests.

The preceding native fluid-container/trim migration remains: 5,369 static, 66 fixed-fluid and 60 TFC fluid/trim selectors; eight vanilla armor overrides; 102 special models still pending. Full build fails main compilation and blocks data/test compilation and packaging. **No JAR or runtime success.** Largest remaining clusters: sky/weather 82, client registration 32, placed-item renderer 13, pan item renderer 9, other special items and transfer capabilities. Preserve and finish these systems. The first-person pan hook does not complete PanItemRenderer. The untracked host NeoForm fallback and local Java 25/21 proxy trust remain necessary here. No dependency changes, new branch, Actions run or `26.x` change.

## Current local checkpoint (2026-10-12)

Existing `26.1.2` continued from `c920472d` with native instrument/projectile rendering, Patchouli component extraction, fluid model registration and thirst/exhaustion callback changes. Same-mode diagnostic javac: **957 → 860 errors**, 1,658 final sources, exit 1, no timeout. Four unchanged geometry factories and the instrument/javelin CPU model suite pass. The historical 190-entry checklist is 75 source-addressed / 115 open. These checks do not establish playability. Full details and limitations are in the newest [work-log entry](WORK_LOG.md) and [validation JSON](validation/2026-10-12-render-guide.json).

Actions quota is suspended for this checkpoint: publish reviewed work on the existing branch with `[skip ci]` and use local validation. Do not request a new Actions run merely to rediscover that quota failure. Inspect live source before continuing; older CI remains historical evidence. Prioritize remaining client/world rendering, contained-fluid/trimmed-item models and overlays; held-item javelins and other Patchouli integration code are still unfinished.

### Optional restricted-container NeoForm workaround

Use a normal JDK 25 installation first. Only if NeoForm Runtime **2.0.31** fails because `ProcessHandle.current().info().command()` is empty, the opt-in helper can build a local tool copy with a `java.home/bin/java` fallback. Obtain the exact matching official `neoform-runtime-2.0.31-all.jar` and `neoform-runtime-2.0.31-sources.jar` from NeoForged Maven, plus the resolved JetBrains annotations JAR; keep generated tools outside tracked source.

```sh
python tools/porting/prepare_neoform_java_fallback.py --runtime /path/to/neoform-runtime-2.0.31-all.jar --sources /path/to/neoform-runtime-2.0.31-sources.jar --annotations /path/to/annotations.jar --output /path/to/local-neoform-fallback
./gradlew -I /path/to/local-neoform-fallback/neoform-java-fallback.gradle -I tools/porting/diagnostics.gradle writePortClasspath build --continue --no-daemon --no-configuration-cache --console=plain
```

The helper changes only two host-executable lookups; dependency pins, game patches, source sets and failure gates remain active. Do not commit the generated JAR or describe this as stock-toolchain validation. Earlier host attempts could not provision the separate **Java 21 toolchain requested by `downloadAssets`**. The latest attempt provisions it and completes asset download after installing the host's trusted proxy CA in the local build JDKs; main compilation still uses Java 25; the original restored-source attempt failed with 650 errors and the latest result is recorded above. A normal environment still needs a full unmodified build and runtime tests.

## Historical public checkpoint (2026-10-10)

- Latest examined `26.1.2` **source** commit: [`659b70ed`](https://github.com/Shnewbs/TFC-Foundations/commit/659b70ed82b0e7fd2ab54914fd37ad20839b53e0). It follows the ported tool-material, recipe, animal/sound, worldgen, model/renderer, registry, and ingredient checkpoints and corrects the tall-grass placement height API.
- [GitHub validation **38070987803**](https://github.com/Shnewbs/TFC-Foundations/actions/runs/38070987803) **FAILED full compilation**: 1,384 Java diagnostic occurrences, with 1,326 distinct displayed file/line/message entries. Its existing API/standalone inspections and resource-validation steps **PASSED**. These counts are not independent bugs or completion percentages, and later phases can reveal new errors.
- The branch generates **5,369 static and 66 native fixed-fluid item selectors**, with **162 custom/override/tinted/variable-fluid item models still needing reviewed target-specific conversion**. Successful generator tests are not proof of in-game rendering.
- **NO playable JAR, completed client or dedicated-server launch, world generation, save/reload, or survival verification.** The `26.x` branch is not known to be playable either.
- CI archives build logs, structured compiler errors, summaries, and an exact `source.tar.gz` snapshot in a **seven-day** Actions artifact. Artifacts expire; obtain the latest or rerun validation rather than expecting an old ZIP to remain downloadable.

These values belong to that exact source and CI run. Do not update or reuse them as “latest” without rechecking GitHub.

## Set up on Linux/macOS or Windows

Install Git, **JDK 25** (`java -version`, `javac -version`), and Python 3 (CI uses **3.13** for resource validation). Use the checked-in Gradle wrapper rather than a system-installed Gradle. The Gradle project is configured with a 6 GiB heap; allow sufficient system RAM/disk space for Minecraft dependencies.

**Linux/macOS shell:**
```bash
git clone https://github.com/Shnewbs/TFC-Foundations.git
cd TFC-Foundations
git switch 26.1.2
git pull --ff-only origin 26.1.2
java -version
./gradlew -I tools/porting/diagnostics.gradle writePortClasspath compileJava compileDataJava compileTestJava build --continue --no-daemon --no-configuration-cache --console=plain
python3 tools/porting/inspect_api.py
python3 tools/porting/compile_main_diagnostics.py
python3 -m pip install -r resources/requirements.txt
python3 resources validate
```

**Windows PowerShell:**
```powershell
git clone https://github.com/Shnewbs/TFC-Foundations.git
Set-Location TFC-Foundations
git switch 26.1.2
git pull --ff-only origin 26.1.2
java -version
.\gradlew.bat -I tools/porting/diagnostics.gradle writePortClasspath compileJava compileDataJava compileTestJava build --continue --no-daemon --no-configuration-cache --console=plain
python tools/porting/inspect_api.py
python tools/porting/compile_main_diagnostics.py
python -m pip install -r resources/requirements.txt
python resources validate
```

A failing build is currently expected. Check `port-diagnostics/build.log`, `port-diagnostics/compiler-errors.json`, and `port-diagnostics/main-javac-summary.json`. `writePortClasspath` must run before local API inspections or the diagnostic-only compiler. The `compile_main_diagnostics.py` runner uses `-proc:none`: it **cannot** replace Gradle's annotation/mixin/data/test compilation. The full build command above mirrors [`.github/workflows/test.yml`](../../.github/workflows/test.yml) and CI evidence is the final source of truth.

To inspect the **26.3** track, first `git switch 26.x` and `git pull --ff-only origin 26.x`; separately resolve its dependencies and API differences. Do not copy a 26.1.2 jar, Patchouli dependency, or source migration into 26.3 without target-specific tests.

## Resume order and source-of-truth files

1. Fetch the **actual branch HEAD** (`git rev-parse HEAD`) and latest [26.1.2 GitHub Actions](https://github.com/Shnewbs/TFC-Foundations/actions/workflows/test.yml?query=branch%3A26.1.2). Read the failed Gradle task and current compiler diagnostics before editing.
2. Read [ROADMAP.md](ROADMAP.md), [PROJECT_STATUS.md](../../PROJECT_STATUS.md), [WORK_LOG.md](WORK_LOG.md), [DEPENDENCIES.md](DEPENDENCIES.md), [VERSION_STRATEGY.md](VERSION_STRATEGY.md), and [TFC_EARTH.md](TFC_EARTH.md). Older status sections and work-log entries are historical, not automatically the newest checkpoint.
3. Choose one coherent source/API cluster; inspect the **resolved target classes** with `javap`, not guessed signatures. Preserve existing game behavior, save formats, render geometry and recipe/registry IDs. Add focused runnable regression checks under `tools/porting/` and register them in `inspect_api.py`.
4. Run focused tests, then full pinned Gradle build and resource checks. Compare error metrics **for the same compilation mode** and keep failed attempts explicit. Do not equate falling diagnostic counts with runtime playability.
5. Publish reviewed source **publicly to the correct branch**, via an ordinary fast-forward commit/push with checks. Keep source and exact validation evidence in GitHub; do not leave the only changes in a local workspace, an expiring Actions artifact, or a chat attachment.
6. Update the current status and dated work log with commit SHA, CI run, failed and passed checks, pending blockers, and any intended behavior differences. Only declare a release after the complete gates below pass. Never commit tokens, secret files, proprietary game binaries, or dataset licenses without authorization.

**Priority engineering backlog:** finish main/data/test compilation (client registration, block/item/entity and fluid rendering, overlays, JEI/Patchouli recipe/guide, capabilities, registry/holder, equipment/recipes and worldgen remain areas requiring review), then native item/resource reload and those 162 nontrivial item models. Do not disable the guide, missing tool APIs, vanilla survival mechanics, entities or geology merely to satisfy the compiler.

## Playable JAR and release gates

After a **successful full Gradle build with a real output JAR**, run `./gradlew runClient` and `./gradlew runServer` (or the `.bat` equivalents on Windows), verify the launched environments have their pinned NeoForge/MC/mod dependencies, then test:

- New TFC world and realistic terrain/biomes, generation at chunk boundaries, spawn and climate/calendar.
- Actual block/item/entity textures, item selectors, GUI/JEI/Patchouli guide, resource reload and recipes.
- Stone/firepit/pottery/copper/bronze/bloomery/iron survival progression; food, decay, thirst, crops and animals.
- Inventory and fluid roundtrips, removal drops, transactions, saves/restarts/rejoins, and two-client dedicated-server sync.
- License/resource verification, repeatable startup and performance regressions. Record failures and unsupported older saves honestly.

A green standalone API probe, a generated asset directory, or an intermediate Gradle output is **not** a playable release. First validated builds remain alpha with explicit limitations, then matching source/JAR/checksums can be published on GitHub Releases (and CurseForge only with correct project credentials/permissions).

## Paste this instruction into a new development environment

> Continue porting public `Shnewbs/TFC-Foundations`, branch `26.1.2`, toward a genuinely playable JAR. Read `docs/foundations/CONTINUE_BUILDING.md`, `docs/foundations/ROADMAP.md`, `PROJECT_STATUS.md`, and the latest GitHub Actions logs first. Check current HEAD and toolchain, preserve all gameplay/IDs and the EUPL license, work through actual compiler errors with exact 26.1.2 APIs, run focused plus full validation, push reviewed work publicly, update documentation, and do not call it playable until client/server/world/survival tests pass. Keep 26.3 on `26.x` separate and independently verified.
