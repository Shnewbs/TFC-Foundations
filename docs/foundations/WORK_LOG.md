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
