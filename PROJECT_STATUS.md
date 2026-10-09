# TFC Foundations — project status

Updated: 2026-10-08. Read this file first when resuming work.

## Current state

- Destination: `Shnewbs/TFC-Foundations`, default branch `26.x`.
- Publication policy (2026-10-08): the user explicitly authorized public source pushes to this existing fork. This supersedes the earlier private-repository requirement. Continue using `Shnewbs/TFC-Foundations`; no detachment/new repository is needed.
- Source baseline: `e9d9a88a187d5a33064e2d86b2803f54238cadd6`, originally identical to upstream `1.21.x`. The branch name does not indicate a completed port.
- First candidate version: **0.0.0** on both tracks; neither is playable yet.
- Playable-alpha priority: branch `26.1.2`, Minecraft 26.1.2, NeoForge 26.1.2.114, JEI 29.43.0.107, Patchouli 26.1-94.
- Forward-port foundation: branch `26.x`, Minecraft 26.3, NeoForge 26.3.0.58-beta, JEI 31.9.0.61. Continue toward 26.4 when its release and matching dependencies are available.
- Shared toolchain: Java 25, Gradle 9.2.1, ModDevGradle 2.0.148. Both GitHub builds reach Java compilation and fail on remaining source migration errors. No gameplay or full Java tests have passed.
- Daily upstream/version monitoring was enabled on 2026-10-08. It reports meaningful changes, not automatic merges of untested fixes.

## Decisions

1. Preserve upstream history, EUPL notices and credits. Keep `tfc` resource IDs during the initial port to avoid unnecessary save/datapack migration. This fork replaces upstream TFC rather than loading alongside it.
2. Port gameplay before broad restructuring. Isolate optional scripting and presentation dependencies behind narrow interfaces. Add Gradle subprojects where they provide useful dependency isolation.
3. Performance and correctness are release requirements. Record measured results rather than claiming optimization from inspection.
4. Use optional Conquest-compatible presentation/integration. Do not bundle its assets or code without applicable permission. Resource packs alone do not provide Conquest's blocks or gameplay features.
5. Publish verified playable candidates to this project's GitHub releases and its own CurseForge project. The CurseForge project ID has not been supplied. Never reuse upstream's IDs.
6. Keep source comments and release notes professional and task-focused. Preserve factual provenance and required notices; do not invent human authorship or test results.
7. Update this file and WORK_LOG.md after meaningful checkpoints and before ending sessions. Include exact commits, tests, blockers and next actions.

## Next actions

1. Push reviewed checkpoints to both target branches, prioritizing playable 26.1.2; preserve history and notices.
2. Use each branch’s GitHub validation run for target-specific compilation diagnostics.
3. Run the prepared 26.3 build on a host where NeoFormRuntime can identify its Java executable. Current host fails in `createMinecraftArtifacts`, before TFC compilation. Then port/isolate unavailable integration adapters and work through source compile errors. See DEPENDENCIES.md.
4. Port registration, components/codecs, recipes, networking, worldgen, entities and rendering in reviewable slices. Re-audit every mixin/access transformer.
5. Complete dedicated-server/client survival checks, then extension adapters and presentation integration.

## Validation

- Inspected architecture and selected source paths documented in AUDIT.md; this is not an exhaustive line-by-line correctness audit.
- `./gradlew test --no-daemon` failed before compilation: Gradle wrapper download raised `java.net.SocketException: Network is unreachable`. Default shell JDK is 17; the baseline requires a Java 21 toolchain. No Java tests ran.
- 26.3 toolchain configuration: Gradle `help` passed; wrapper regeneration and `generateModMetadata` passed. Generated metadata parses and identifies `tfc`, version `0.0.0`, Minecraft `[26.3]`, and NeoForge `[26.3.0.58-beta,)`.
- Gradle automatically installed Temurin 25.0.4.1+1. `compileJava` failed in `createMinecraftArtifacts`: NeoFormEngine.java:120 throws `NoSuchElementException` because `ProcessHandle.current().info().command()` is empty on this host. An independent Java probe reproduced the empty Optional. TFC Java compilation and tests did not run; no JAR exists.
- `verifyPortDependencies` fails as intended: Patchouli/EMI/Jade/The One Probe target pins are missing. Removed old runtime dependencies rather than loading 1.21.1 binaries into 26.3. Adapters remain in source for a proper port/isolation pass.
- The 26.x validation workflow now allows public-fork runs. Runtime release gates still apply: an incomplete port must not be published as a playable release.
- Actions were disabled at repository level for this fork; enabled after user approval. Manually launched run #1 (`37860026420`) at commit `995c2433a287e0d21a45fc41dcb1e45fee7900db`. It failed configuring license checks because Licenser 0.7.2 references Gradle's removed ConfigureUtil. Upgraded to 0.7.5; all local main/test/data license checks now pass. CI run #3 (`37860477602`) now resolves JEI/MezzConfig and successfully creates Minecraft artifacts. It stops at the deliberate missing-integration packaging gate before Java compilation. CI now explicitly requests main/data/test compilation with `--continue` to collect independent migration errors while keeping packaging blocked.

## References

- [Source audit and integration design](docs/foundations/AUDIT.md)
- [Milestones and release gates](docs/foundations/ROADMAP.md)
- [Upstream triage](docs/foundations/UPSTREAM.md)
- [Work log](docs/foundations/WORK_LOG.md)

- [26.3 dependency checkpoint](docs/foundations/DEPENDENCIES.md)

## First source migration checkpoint

- CI run [37860739449](https://github.com/Shnewbs/TFC-Foundations/actions/runs/37860739449) reached `compileJava` and reported the first 100 errors. This is the compiler display limit, not the total number of migration errors. Missing types include ResourceLocation, moved entity classes, GuiGraphics, reload events and interaction APIs.
- Replaced the exact ResourceLocation token with Identifier in 179 main/data/test Java files, including codecs, packets, registries, assets and integration adapters. All string IDs and gameplay logic remain unchanged by this migration.
- Verified the replacement against Mojang's SHA-1-verified 26.3 client JAR, using `javap` and an isolated executable probe: explicit/default namespaces, serialization round trip and invalid-ID rejection all passed. Local main/data/test license checks pass. This is not a full TFC compile or runtime test.
- Next: inspect the post-migration CI diagnostics, migrate entity class locations and event APIs, then adapt rendering and unavailable integrations. Packaging remains blocked.

## Entity migration checkpoint

- Run [37861223935](https://github.com/Shnewbs/TFC-Foundations/actions/runs/37861223935) still fails Java compilation; the reported errors now expose entity package moves, interaction APIs, rendering and reload-event changes.
- Moved references to 36 vanilla entity types to their actual 26.3 packages across 63 Java files, and renamed MobSpawnType to EntitySpawnReason. Verified all referenced spawn-reason enum constants exist in the official 26.3 JAR.
- Updated the AbstractSkeleton mixin invocation descriptor to the moved class. Bytecode inspection confirms reassessWeaponGoal still invokes getItemInHand with the expected descriptor; application of the mixin still requires a runtime test.
- These changes do not complete entity behavior migration. Constructor/method signatures, synchronization, AI and spawning need compiler and runtime verification.

## Version investigation

[Version strategy investigation](docs/foundations/VERSION_STRATEGY.md) records Patchouli binary incompatibilities and published ecosystem plans as of 2026-10-08. Recommendation: first playable candidate on 26.1.2, preserve 26.3 work and keep 26.4 conditional. This is a recommendation only; the user has not selected a new target and the current build remains 26.3.

## Accepted dual-target policy — 2026-10-08

The user authorized both tracks: `26.1.2` is the priority for the first playable 0.0.0 candidate; `26.x` retains Minecraft 26.3 as the development base for 26.4. This supersedes the recommendation-only text above. Share only changes verified against each branch; keep exact target ranges and separate artifacts. Neither branch is playable yet.

### Active branch target

This branch targets Minecraft 26.1.2, NeoForge 26.1.2.114, JEI 29.43.0.107 and Patchouli 26.1-94. Published Maven POMs/metadata were verified. Expected artifact: TFC-Foundations-26.1.2-0.0.0.jar. Earlier 26.3 entries record the shared port history. EMI/Jade/TOP adapters remain pending.
