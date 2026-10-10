# TFC Foundations — Work Log

The complete earlier work log is retained byte-for-byte in [WORK_LOG_BEFORE_LOOT_MIGRATION](history/WORK_LOG_BEFORE_LOOT_MIGRATION.md). It records the initial source publication, toolchain/dependency work, dual-target decision, gameplay API migrations and Earth groundwork. This file continues that history; it does not replace the recorded validation limits.

## 2026-10-10 — 26.3 worldgen direction and API assessment

Fast-forwarded the existing work branch to the documented 26.x handoff commit `25c3c1ebc40fc064fa97c725d030351dc215a10a`; no new branch was created. Recorded the requested hybrid worldgen quality direction and acceptance gates in `ROADMAP.md`, and added the exact 26.3 migration findings to `PROJECT_STATUS.md`.

Inspected the resolved Minecraft 26.3 patched JAR APIs and the latest public 26.x diagnostic artifact (run `37873172113`, source `88e0f3f`). Density functions are now under `net.minecraft.world.level.levelgen.densityfunction`; the prior `compute`/function-context/visitor model has been replaced by compiled samplers and rewrite rules. `NormalNoise.Parameters` is not accessible to mod source, and target noise APIs use `Noise` values, registry-backed keys, and builders. This means a safe port must coordinate custom density functions, datagen/registry lookups, noise samplers, aquifers, and carver integration. Simple package renames would leave an incomplete or behavior-changing generator, so no source migration was retained in this pass.

Local command `.\gradlew.bat -I tools/porting/diagnostics.gradle writePortClasspath compileJava compileDataJava --continue --no-daemon --no-configuration-cache --console=plain` resolved/downloaded the 26.3 artifacts and failed at `compileJava` after javac's 1,000-error display cap. The diagnostics include target worldgen API errors and pre-existing broad migration errors; the cap is not a total count or progress measure. This machine has Java 21, below the Java 25 branch requirement. `git diff --check` and Python syntax checking passed, but the full build did not. No compile, datagen, runtime, seed-compatibility, visual-quality, or performance pass is claimed. This checkpoint changes roadmap/status documentation only; no source output, gameplay result, or release was produced.

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
