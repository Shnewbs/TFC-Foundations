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
