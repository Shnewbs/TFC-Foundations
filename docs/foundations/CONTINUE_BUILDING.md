# Continue development in another environment

**Checked:** 2026-10-16. **Repository:** [Shnewbs/TFC-Foundations](https://github.com/Shnewbs/TFC-Foundations).
This file is the handoff entry point for another computer, operating system, IDE, or ChatGPT/Codex session. **Always consult the live branch and its latest CI run before treating the checkpoint below as current.** Do not rely on local or chat-only work being available elsewhere.

## Branches and pinned targets

| Branch | Purpose | Minecraft | NeoForge | JEI | Patchouli |
| --- | --- | --- | --- | --- | --- |
| `26.1.2` | **First playable priority** | `26.1.2` | `26.1.2.114` | `29.43.0.107` | `26.1-94` |
| `26.x` | Independent experimental track to eventually move to a released/verified `26.4` | `26.3` | `26.3.0.58-beta` | `31.9.0.61` | **Unresolved for this target** |

Both tracks use **Java 25**, Gradle wrapper **9.2.1**, and ModDevGradle **2.0.148**. Branch-specific `gradle.properties` is authoritative if a dependency is changed after this handoff. Current candidate version is `0.0.0`, **not** a tested release. Preserve upstream TFC gameplay, the EUPL notices, credits, identifiers, and full feature set; never hide missing APIs behind dummy implementations or disable subsystems to manufacture a green build.

## Latest verified continuation — 2026-10-16 UTC

Work directly on `26.1.2`; no task branches. Published parent `f994598f` independently reproduces **359 diagnostic errors**. The native item-layer continuation reports **261 main errors** in both diagnostic javac and Gradle (1,667 sources, exit 1, no timeout in the diagnostic pass). All 32 API runners pass, including 584 new native item-layer CPU assertions and the existing 9,074 display assertions. Resource validation, generated textures, main/data/test licenses, dependency verification and asset gate pass. See `validation/2026-10-16-item-layers-api.json` and `2026-10-16-item-layers-errors.json` plus the first project-status/work-log entry.

Fluid containers and armor trims now use native ItemModel codecs, per-reload caches and cuboid/composite layers. Fluid inspection does not drain solid molds; offsets, texture IDs, parent transforms, translucent passes and luminous emission remain. Mask edges are retextured along with front/back faces. The generator emits 5,369 static, 66 fixed-fluid and 60 native TFC fluid/trim selectors; 102 special models remain pending. Eight vanilla armor override selectors are authored, with leather dye applied only to the base. Native grass/foliage color registration and the remaining property/special-item paths still need porting. CPU codec/quad checks do not initialize pipelines, bake/reload a real atlas, resolve live items or prove appearance/gameplay.

Full build still fails main compilation and blocks data/test compilation and packaging. **No JAR or runtime success.** Largest remaining diagnostic clusters: sky/weather 82, client registration 32, placed-item rendering 13, pan rendering 9 and transfer capabilities. Preserve and finish these systems. The previous machinery, chest/bell and sign migrations remain; hanging text has a registered `AbstractSignRendererAccessor` awaiting actual Mixin startup. The untracked host NeoForm fallback remains necessary here; the asset gate passes with this host's trusted proxy CA in local Java 25/21 stores. No dependency changes, new branch, Actions run or `26.x` change.

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
