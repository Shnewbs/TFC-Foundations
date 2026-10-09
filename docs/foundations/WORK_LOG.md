# Work log

## 2026-10-08 — Baseline and preparation

Completed: verified GitHub access, cloned repositories, inspected source/build/workflows and recent upstream activity. Confirmed 26.x equals upstream 1.21.x at e9d9a88a187d5a33064e2d86b2803f54238cadd6. Prepared audit, milestones, extension boundaries, performance plan, licensing notes and bug-triage sample. Enabled daily upstream/version watch. Prepared local release-workflow quarantine to remove upstream publishing destinations.

Validation: `./gradlew test --no-daemon` failed downloading Gradle before compilation (Network is unreachable). No Java tests/game tests ran. Official Mojang/NeoForge metadata requests succeeded. Source remains configured for 1.21.1; 26.3 dependency set is not validated.

Blockers: repository is a public fork; connector lacks administration actions and browser fallback needs approval. Leaving the fork network is permanent and can discard non-Git metadata. Need target toolchain/dependencies, runtime validation and Foundations CurseForge project ID. Conquest permissions/26.3 compatibility remain unverified.

Changes remain local and unpushed. Next: verify private destination, push preparation, establish baseline tests, then port the 26.3 toolchain and compilation in a separate change. Record actual outcomes and commits here.

## 2026-10-08 — First 26.x candidate configuration (0.0.0)

User selected version 0.0.0 and confirmed the public-fork privacy blocker remains unresolved. No remote writes.

Changes: configured Minecraft 26.3, NeoForge 26.3.0.58-beta, Java 25, ModDevGradle 2.0.148, Gradle 9.2.1, Foojay resolver 1.0.0 and JEI 31.9.0.61. Regenerated wrapper and pinned the verified Gradle distribution checksum. Set project/artifact identity to TFC Foundations; preserved `tfc` IDs and upstream credits. Removed the obsolete Parchment overlay and old runtime integration pins. Added a packaging prerequisite for verified integration versions. Changed inherited CI to 26.x/Java 25, read-only permissions and private repositories only; publication remains disabled. No gameplay source port or integrations claimed complete.

Validation:

- Host proxy arguments allowed Gradle dependency downloads without changing project networking settings.
- `gradle help --no-daemon`: PASS (Gradle 9.2.1).
- `gradle compileJava --no-daemon`: FAIL in `createMinecraftArtifacts`, before compilation. Temurin 25.0.4.1+1 installed automatically; NeoFormRuntime throws `NoSuchElementException` in `NeoFormEngine.java:120` when resolving the running executable. Independent Java probe: `ProcessHandle.current().info().command()` => `Optional.empty`, while `java.home` is valid. No environment access controls or build-tool binaries were modified to work around this.
- `gradle wrapper generateModMetadata verifyPortDependencies --continue --no-daemon`: wrapper and metadata PASS; dependency check FAIL as expected for unset Patchouli, EMI, Jade and The One Probe target pins.
- Generated TOML parsed successfully; candidate version and target ranges checked.
- No Java tests, runtime tests, performance measurements, playable JAR, GitHub release or CurseForge upload.

Next: run artifact generation on a compatible build host/private CI, port/isolate the legacy integrations, then compile and port gameplay APIs. Passing Gradle configuration is not a completed game port. Keep 0.0.0 unreleased until the roadmap gates pass.

## Checkpoint format

Record date, starting/ending commits, scope, changed systems, exact test commands/results, unverified assumptions, blockers, release/artifact identifiers and next action. Update PROJECT_STATUS.md before ending each meaningful session. Planned work must never be marked complete.

## 2026-10-08 — Public source publication authorized

The user explicitly requested continued work and public pushes, superseding the earlier privacy requirement. Retain the existing public fork and its full history. Removed the private-only CI condition and enabled manual validation runs. Push preparation includes the local 0.0.0 checkpoint 36e9b1fbd. Runtime/playability gates still apply to releases.

Publication result: pushed preparation and public CI configuration to `26.x` as `990bda3df4a2d09ca2dfbcf91d500439ab0ceef5` through the GitHub connector. Direct Git transport had no credential, so the connector created the equivalent tree/commit; original local commits remain on `local-pre-public-checkpoint`. Verified identical trees and aligned the local branch with the published commit. Both workflows report active, but the Actions runs endpoint reports zero runs. No CI compilation result is available. The connector has no manual-dispatch action. Updated README to identify this fork, disclose the incomplete port and preserve all upstream legal/acknowledgment notices.

## 2026-10-08 — Actions enabled; Gradle 9 license check repaired

With explicit browser approval, enabled Actions for this fork and dispatched Validate 26.x on 26.x. Run #1: https://github.com/Shnewbs/TFC-Foundations/actions/runs/37860026420, commit 995c2433a287e0d21a45fc41dcb1e45fee7900db. Checkout and Java 25 setup passed. Build failed before source compilation: Licenser 0.7.2 referenced removed Gradle class `org/gradle/util/ConfigureUtil`.

Selected published NeoForge Licenser 0.7.5 and ran `gradle checkLicenses --no-daemon` on Gradle 9.2.1 locally. PASS: checkLicenseMain, checkLicenseTest, checkLicenseData and checkLicenses. No check disabled or license removed. Push this fix for a full CI retry. Playable 0.0.0 remains unavailable.

### CI dependency resolution follow-up — 2026-10-08

Run [37860254645](https://github.com/Shnewbs/TFC-Foundations/actions/runs/37860254645) passed the original Licenser configuration error, then failed resolving JEI transitive MezzConfig 0.6.6 artifacts. Both 26.3 POMs were verified present on Maven BlameJared; the exclusive repository filter admitted only `mezz.jei`. Added `net.mezzdev.config` to that filter. Full compilation and runtime validation remain pending; this is a dependency resolution correction, not a completed gameplay port.

### First Java migration — 2026-10-08

CI now runs explicit compilation targets with `--continue`, retaining the failing packaging gate. Run 37860739449 reached the compiler and displayed 100 errors. Migrated ResourceLocation to the verified 26.3 Identifier API across 179 Java files. A token-only diff check confirmed no other Java edits; no data/resource IDs changed. A standalone Java 25 probe against the official Minecraft 26.3 client JAR passed namespace preservation, round trip, and invalid identifier checks. All local license checks pass. Full compilation, tests, client/server startup and gameplay regression checks remain incomplete.

### Entity package migration — 2026-10-08

Migrated 36 entity class locations plus MobSpawnType → EntitySpawnReason across 63 Java files using the official 26.3 class inventory. Updated the skeleton mixin bytecode target and verified the target invocation still exists. Retained all spawn-reason values used by TFC; verified each against EntitySpawnReason. Next checks: clean compilation, mixin application, natural/chunk/breeding/conversion spawns, animal AI, projectiles, boats/minecarts and save/reload. None of those gameplay checks has passed yet.

### Dual-target build setup — 2026-10-08

Accepted user direction: prioritize a playable 26.1.2 branch and continue 26.3 on 26.x for eventual 26.4. CI now listens to both branches and runs compiler, test and packaging checks independently. Modern Patchouli Maven coordinates use patchouli-neoforge; 26.1.2 pins released 26.1-94, while 26.3 remains blocked pending a compatible guide dependency. No runtime compatibility or release claimed.


## 2026-10-09 — Shared relocations and keyed reload registration

- Compared official client JAR inventories for 26.1.2 and 26.3; applied 26 shared class package relocations, including JVM descriptor paths. Class existence does not prove method compatibility. Kept Bucketable in its target-specific package on 26.1.2.
- Migrated server/client reload registration to AddServerReloadListenersEvent/AddClientReloadListenersEvent and unique namespaced listener keys, using both NeoForge source distributions. Preserved vanilla recipe-manager access after checking both target signatures.
- Initial dual-track CI runs 37863900030 (26.1.2) and 37863876836 (26.3) reached Java compilation and failed with the first 100 displayed errors; this is not a total error count. Remaining failures include model/rendering, interaction, worldgen and optional integration APIs.
- Local 26.1.2 metadata generation, dependency resolution and license checks passed. Full local compilation remains blocked by the documented host executable-discovery failure. No playable JAR or runtime verification is claimed.
- Required regression checks after compilation: initial client resource load and F3+T (all color maps/stars), server start and repeated /reload (all data managers), recipes after reload, multiplayer data sync/reconnect, dedicated-server class loading, entity/model rendering, survival progression and save/restart.


## 2026-10-09 — Spawn, potion and block-break API migration

- Replaced PlayerRespawnLogic with PlayerSpawnFinder after verifying getSpawnPosInChunk on both target JARs.
- Used AbstractThrownPotion for water dousing, preserving both splash and lingering potion handling. Verified the water-sensitive predicate and shared inheritance on both versions.
- Migrated TriState to Minecraft's enum and replaced removed helper methods with explicit enum comparisons, preserving bamboo soil override/default behavior.
- Migrated BreakBlockEvent while preserving server-only collapse/logging. The replacement fires on both sides; client callbacks and canceled events return without world changes. Server-side logging cancellation requests a client block update.
- Relocated CriteriaTriggers only on 26.3; 26.1.2 retains its original package.
- Gameplay checks still required: fresh-world spawn, protected/canceled mining, collapse, tree felling with client block synchronization, normal block breaking, water splash/lingering dousing and bamboo soil TRUE/FALSE/DEFAULT cases. These are source changes, not runtime-tested fixes.


## 2026-10-09 — Playability migration and Earth groundwork

- Migrated item/block/entity interaction results without dropping held-stack replacement or empty-hand fallback. Replaced old game-rule callback accessors with supported change events and kept calendar ownership server-side.
- Ported GUI extraction/input APIs, with branch-specific screen navigation and keyboard handling. Corrected grass-density slider initialization so accepting existing world settings does not copy continentalness into grass density.
- Ported JSON listener construction while keeping registry-aware parsing in apply. Recipe caches now consume RecipeMap; recipes are explicitly requested for client sync and cleared on logout.
- Isolated optional EMI/Jade/TOP source sets; missing target adapters are not compiled or linked, while included adapter errors remain visible. Patchouli remains required and blocks 26.3 packaging. License and source-set model checks passed for optional adapters; no unverified pins persisted.
- 26.1.2 preserves single-pass caves/aquifers and adds a maintained TFC random-patch feature. Generator output matches all 125 migrated biome carver lists and177 patch references;1907 generated resources completed without errors. New patch classes compile against the actual26.1.2 client JAR. 26.3's new terrain/density pipeline still requires a substantive port.
- Earth groundwork: nominal-scale coordinate/elevation math and explicit distortion, eight isolated Java25/JUnit tests passing. No synthetic Earth preset, dataset download, or complete generator claimed.
- Required gameplay regressions: interaction swings/offhand/container replacement, calendar/game-rule commands, protected mining/collapse/logging, recipes after reload/disconnect/reconnect, guide and GUI navigation, cave/aquifer continuity, vegetation/loose rocks, chunk persistence, and client/dedicated-server progression.
