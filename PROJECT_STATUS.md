# TFC Foundations — project status

Updated: 2026-10-08. Read this file first when resuming work.

## Current state

- Destination: `Shnewbs/TFC-Foundations`, default branch `26.x`.
- Publication policy (2026-10-08): the user explicitly authorized public source pushes to this existing fork. This supersedes the earlier private-repository requirement. Continue using `Shnewbs/TFC-Foundations`; no detachment/new repository is needed.
- Source baseline: `e9d9a88a187d5a33064e2d86b2803f54238cadd6`, identical to upstream `1.21.x`. The branch name does not indicate a completed port.
- First candidate version: **0.0.0**, expected filename `TFC-Foundations-26.3-0.0.0.jar`.
- Build configuration now targets Minecraft 26.3, NeoForge 26.3.0.58-beta, Java 25, Gradle 9.2.1 and ModDevGradle 2.0.148. Gameplay source is still the upstream 1.21.1 baseline and has NOT passed 26.3 compilation.
- Requested target: Minecraft 26.3, then released 26.4 with a usable NeoForge toolchain. Official metadata currently lists release 26.3, snapshot 26.4-snapshot-3 and NeoForge 26.3.0.58-beta. These are observed versions, not a tested dependency set.
- No playable Foundations build, 26.3 compilation, benchmark result, KubeJS/CraftTweaker adapter or Conquest integration exists yet.
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

1. Push source checkpoints publicly to the existing 26.x branch; preserve upstream history and notices.
2. Run the 26.x validation workflow on GitHub to get compilation diagnostics outside this host.
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
- Public checkpoint `990bda3df4a2d09ca2dfbcf91d500439ab0ceef5` contains the 0.0.0 preparation. GitHub reports the validation workflow active, but no Actions run has started as of this checkpoint. The connector cannot manually dispatch workflows; CI results are pending.

## References

- [Source audit and integration design](docs/foundations/AUDIT.md)
- [Milestones and release gates](docs/foundations/ROADMAP.md)
- [Upstream triage](docs/foundations/UPSTREAM.md)
- [Work log](docs/foundations/WORK_LOG.md)

- [26.3 dependency checkpoint](docs/foundations/DEPENDENCIES.md)
