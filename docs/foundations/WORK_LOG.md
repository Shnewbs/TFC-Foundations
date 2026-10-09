# TFC Foundations — Work Log

The complete earlier work log is retained byte-for-byte in [WORK_LOG_BEFORE_LOOT_MIGRATION](history/WORK_LOG_BEFORE_LOOT_MIGRATION.md). It records the initial source publication, toolchain/dependency work, dual-target decision, gameplay API migrations and Earth groundwork. This file continues that history; it does not replace the recorded validation limits.

## 2026-10-09 UTC — Loot migration and reproducible target evidence

### Starting points and public commits

The continuation resumed `26.1.2` from `1811814ee1e2bd7bbe1d5b1fdf6067bf4fe0d3f9` and `26.x` from `2d45ee32ceed4d5c8341cc0ed33dcafad3e095a3`. The user's prior public-push authorization remains in effect. No force push, branch deletion, release or version increment was performed.

- `5f2800b86d44f7a4e694928b755c3065637f7a8f` on 26.1.2: bounded CI summaries with full logs and exact source archives.
- `e8928686036cd13b137e129ec7b9dde25a3288b9` on 26.1.2: deduplicated compiler reports and exact resolved API inspection.
- `6d3180c130b9a907be5f31511fb34e17039d5210` on 26.x: the same diagnostic infrastructure, independently resolved against 26.3.
- `cc04e4f1f37fa62f59f30ca5f8d23d301dbc56d9` on 26.1.2: 14 production loot Java files ported to direct codecs/context keys, with a focused actual-source validation probe.

### Source changes and preservation checks

26.1.2's loot condition, number-provider and function registries now register MapCodec instances directly rather than the removed wrapper types. Each implementation returns the same static codec instance that is registered. Custom context keys preserve `tfc:isolated`, `tfc:burnt_out` and `tfc:sluice`. All 11 loot registry identifiers are preserved. The presence-based flag semantics are unchanged; a present Boolean false is not silently reinterpreted as an absent flag.

Animal/crop yield formulas are unchanged apart from the required context accessor renames. Fluid-copy behavior still uses the original SIMULATE/EXECUTE flow. MinMaxProvider now validates each nested provider with a field-specific context. Loot package defaults and CopyFluidFunction's explicit nullable declarations use JSpecify. The repository-wide nullability migration is not complete.

Local source audits passed for all 11 registered IDs, all three context IDs, both yield calculations and absence of removed loot wrapper/context accessor names in the migrated package. These were structural source checks, not game-runtime tests.

### Exact commands and observed results

CI runs the full build without hiding its exit status:

```sh
./gradlew -I tools/porting/diagnostics.gradle writePortClasspath compileJava compileDataJava compileTestJava build --continue --no-daemon --no-configuration-cache --console=plain
```

Build output is retained in `port-diagnostics/build.log`. The init script only records the resolved classpath; it does not remove production source sets or relax packaging/release gates. `inspect_api.py` scans actual resolved JARs and records public javap signatures, rather than assuming API compatibility from class names. Diagnostic artifacts contain signatures, reports and this project's source, not copies of Minecraft binaries.

[26.1.2 run 37872611533](https://github.com/Shnewbs/TFC-Foundations/actions/runs/37872611533), source commit `cc04e4f`:

- Full build: FAIL, still reaches javac's configured 1,000-error display cap. Grouped/repeated diagnostics are not a reliable total-backlog or progress percentage.
- `python3 tools/porting/inspect_api.py`: PASS, inspected 23 exact target API types and ran the focused source probe.
- Focused Java 25 compilation: PASS for the actual production AlwaysTrueCondition, MinMaxProvider and their package metadata. The empty source path prevents an accidental whole-project compile; no fake game/TFC implementations are supplied.
- Focused runtime probe: PASS, four checks on the actual AlwaysTrueCondition codec: stable identity, singleton builder, encoding and round trip. This is not mod loading or validation of all 11 registered codecs.
- `python resources validate`: PASS. Reported zero errors for the checked language, model-parent, texture, blockstate and unused-model categories. This does not prove target rendering compatibility.
- Artifact: `11590039374`, named `tfc-port-diagnostics-37872611533-1`, seven-day retention. Earlier exact-API artifacts: 26.1.2 `11590293535` from run `37871566668`; 26.3 `11589869160` from run `37871626426`.

The local container only had Java 21 and no resolved target dependency cache, so these Java 25 results came from GitHub Actions, not an alleged local full build.

### Branch divergence discovered before sharing the patch

The 14 original loot files were byte-identical on both branch checkpoints, but their resolved target APIs were not. 26.3 LootContext exposes `getOptional` instead of 26.1.2's getParameter/getOptionalParameter pair; conditional function builders have holder-based conditions; NumberProvider and NumberProviders no longer exist. The target class inventory contains separate ContextIntProvider and ContextFloatProvider families. Minecraft's official 26.3 notes independently document the integer/float registry split.

Consequently, the 26.1.2 loot patch was NOT blindly copied to 26.x. The 26.3 source/data migration must select appropriate provider registries and preserve integer drop counts, codec references, optional context handling and conditional-function behavior. The 26.3 full build remains failed; its diagnostic infrastructure is usable.

### Outstanding work and release boundary

Remaining displayed failures include obsolete rendering/model classes, JEI/Patchouli adapters, legacy package nullability defaults, gameplay type relocations, old tool/armor APIs and serialization/dependency changes. The next source pass should work through common API blockers and then rendering/integration adapters. After compilation, validate all inherited interaction, recipe reload/sync, calendar, worldgen, save/restart, multiplayer and survival-progression regressions recorded in the earlier log.

Earth's existing coordinate/elevation groundwork remains preserved but untouched in this continuation. No Earth preset, dataset integration or complete world generator was added. No playable JAR, client/server startup result, gameplay regression result, performance claim, GitHub release or CurseForge upload is claimed.

Current decision/status is in [PROJECT_STATUS](../../PROJECT_STATUS.md). Continue appending exact commit/run outcomes here; never mark planned work as tested.


## 2026-10-09 UTC — Common API continuation toward the compiler milestone

Resumed source `2854e31b81b3f159dcabaf74f0805ac70a762321` on 26.1.2. Public tooling commit `c52eed5fceeca8083b012993719cac07333d2ceb` produced a bounded exact-target compiler bundle in successful workflow `37873949671`. All 84 classpath JAR hashes were checked after download. Local compilation uses that Java 25 runtime and the exact Minecraft 26.1.2 / NeoForge 26.1.2.114 / JEI 29.43.0.107 / Patchouli 26.1-94 inputs, not guessed libraries.

Production changes update legacy package defaults to JSpecify, DirectionProperty to EnumProperty<Direction>, ContainerInput and teleport APIs, moved model data/advancement packages and ARGB calls. The tooltip debug path uses DataComponentPatch.getPatch so absent patches, explicit removal and explicit values remain distinct. Level field reads, ChunkPos record accessors/factories and NBT reads were planned with javac-resolved receiver types, not broad field-name replacement. Source preimages and final contents are verified before public application.

The new NbtHelpers implements strict saved-tag checks and validates every element of a formerly homogeneous list, returning a fresh empty list on invalid/missing input. Existing numeric/string reads now state their old fallback values explicitly. The independently testable Unchecked utility replaces reliance on a removed transitive library while keeping Helpers as the calling facade and preserving throwable identity.

Validation:

- Diagnostic Java main pass initially terminated with 1,341 reported errors; the first source slice reported 846 before the same early stop. Those are not full counts.
- After repairing the debug component-patch accessor, javac completed a larger diagnostic traversal with 3,979 errors. The typed common pass then completed with 3,490; the utility/type-check follow-up completed with 3,438. Error counts are not an estimate of project completion, nor proof that all downstream errors are exposed.
- Some local full-compile attempts were interrupted by execution timeouts. Only logs ending with javac's final error/warning totals are treated as completed diagnostic passes. The normal Gradle release gate is not replaced by this manual `-proc:none` pass.
- `JAVA_HOME=<resolved Java 25> python tools/porting/run_common_smoke.py`: PASS, 51 standalone actual-utility/API checks; independently compiled all 113 package-info files across main/data/test. The first run caught remaining stale annotation imports; those were corrected and retested.
- Chunk-position, component-patch and integer-provider runtime probes: NOT RUN successfully. A plain-Java attempt failed because Minecraft/NeoForge bootstrap was absent. Their code compiles but they are excluded from the standalone runtime count, explicitly reported as not run, and must be executed in a genuine bootstrapped harness later.
- `python tools/porting/run_loot_smoke.py` with the resolved JDK on PATH: PASS, the existing four production AlwaysTrueCondition checks; MinMaxProvider also compiles.
- `git diff --check`: PASS. No registry/resource/save-key rename or release gate relaxation is intended. The compiler error display limit is raised to 10,000 to expose diagnostics, not to suppress failures.

The standalone probes are integrated into `inspect_api.py`; normal CI still fails when the Gradle build fails. `compile_main_diagnostics.py` provides the separate reproducible main-source diagnostic command and records timeouts/exit codes explicitly. The 26.x/26.3 branch, Earth terrain work, gameplay behavior redesign, feature exclusions and dependency/version pins were not changed. The compiler milestone, playable JAR and release remain outstanding.
