# TFC Foundations — Current Project Status

**Continuation in another environment:** [CONTINUE_BUILDING.md](docs/foundations/CONTINUE_BUILDING.md) is the shared checkout/build/CI handoff. This page records the separate 26.3 migration track as of its stated date; the playable-first 26.1.2 branch has newer source checkpoints and must be checked independently.

Updated 2026-10-09 UTC (October 8 in America/Los_Angeles).

**Version 0.0.0 is an incomplete source port, not a playable release. Neither target has passed full compilation, client/server startup, or survival regression tests. No playable JAR or release was produced in this continuation.**

## Authorized direction

Continue public development in Shnewbs/TFC-Foundations, preserving upstream history, the EUPL license, credits and `tfc` resource IDs. Prioritize a playable Minecraft 26.1.2 build. Maintain Minecraft 26.3 on `26.x` as the foundation for a separately verified 26.4 port. Do not assume a version-number change establishes compatibility.

| Track | Pinned target | Latest source/validation checkpoint |
| --- | --- | --- |
| Priority playable track, `26.1.2` | Minecraft 26.1.2; NeoForge 26.1.2.114; JEI 29.43.0.107; Patchouli 26.1-94 | `cc04e4f1f37fa62f59f30ca5f8d23d301dbc56d9`: 14 loot source files migrated; isolated checks pass; full compilation fails. |
| Forward-port track, `26.x` | Minecraft 26.3; NeoForge 26.3.0.58-beta; JEI 31.9.0.61 | `6d3180c130b9a907be5f31511fb34e17039d5210`: exact target API and compiler evidence captured; full compilation fails. Required Patchouli target remains unresolved. |

Both use Java 25 and Gradle 9.2.1. Optional EMI/Jade/TOP adapters remain isolated and incomplete. Required dependency and publication gates have not been bypassed.

## Verified in this continuation

On 26.1.2, loot registries now hold their direct MapCodec values, custom context parameters use ContextKey, and the loot package uses JSpecify defaults. All 11 registered loot IDs and three custom context IDs are unchanged. Source audits confirm the animal/crop yield calculations are unchanged apart from target API accessor names. Nested min/max providers now receive validation. These source checks do not prove in-game drops.

[Validation run 37872611533](https://github.com/Shnewbs/TFC-Foundations/actions/runs/37872611533) tested source commit `cc04e4f`:

- PASS: actual production AlwaysTrueCondition codec identity, builder singleton, empty-object encoding, and codec round trip (four checks).
- PASS: actual production MinMaxProvider and loot package metadata compile against the resolved Minecraft/NeoForge classpath in an isolated javac invocation.
- PASS: existing resource validation, including language, model parents, textures and blockstates, reports zero errors in its checked categories.
- FAIL: the full Gradle compilation/build still reaches the 1,000-error display cap. The diagnostic report groups repeated messages; neither this cap nor its grouped count is the total migration backlog. No gameplay, performance or complete loot-package test has passed.

Both branches now preserve build logs, structured compiler reports, resolved API signatures, and exact source snapshots as short-lived CI artifacts. Build failures remain failures; successful diagnostic or resource steps do not make the job green.

## Important 26.3 divergence

Compared the exact resolved APIs from both branches. On 26.3, LootContext uses `getOptional`, conditional loot functions use holder-based conditions, and the old NumberProvider/NumberProviders pair has been replaced by separate context integer/float provider families. **The 26.1.2 loot patch has not been copied to 26.x.** It requires a dedicated source and data-format migration, including preserving integer drop-count behavior.

## World-generation direction and 26.3 blocker

The requested goal is a polished, distinctive TFC terrain experience that combines TFC's geology, climate, rivers, shores, volcanoes, aquifers, and survival progression with carefully selected modern Mojang/NeoForge APIs and high-level terrain-design inspiration from projects such as Terralith. Terralith is inspiration only; do not copy its code, data, names, or assets. Major terrain behavior should be versioned or opt-in until seed/save compatibility is understood. Track the measurable acceptance gates in [ROADMAP](docs/foundations/ROADMAP.md); do not claim comparative superiority without visual, determinism, and performance evidence.

The local 26.3 target API inspection confirms worldgen needs a structural migration, not import substitutions: density functions moved to `levelgen.densityfunction` and replaced the prior compute/context/visitor API with compiled samplers and rewrite rules; `NormalNoise.Parameters` is private and noise creation now uses builders/registry-backed `Noise` values. Related migration reaches custom density functions, noise registry/data generation, aquifers, and carver integration. These changes were deliberately not applied incompletely. The local Gradle compilation on this machine reached javac's 1,000-error display cap; this is not a passing build or a trustworthy total backlog count. The installed runtime is Java 21 rather than the documented Java 25 target. No terrain output, seed compatibility, gameplay, or runtime improvement is claimed.

## Next playability gates

1. Finish common API blockers: remaining legacy nullability package defaults, removed/relocated gameplay classes, serialization and registry APIs. Preserve behavior and validate each slice against the exact target.
2. Complete rendering/model, JEI and Patchouli adapters; do not remove core visuals or the guide merely to achieve compilation.
3. Port 26.3's density-function/noise/carver worldgen APIs as one behavior-preserving cluster with focused target-backed tests, then prototype terrain improvements against the documented determinism, seam, gameplay, visual, and performance gates.
4. Pass main/data/test compilation, licenses, resource generation and packaging. Then test client and dedicated-server launch, new-world creation, save/reload, multiplayer and survival progression.
5. Validate 26.3's separate loot, holder and world-generation migrations before sharing branch changes. Publish target-specific GitHub releases only after their stated gates pass.

## Earth option

The requested optional natural Earth mode remains in scope: nominal one-block-per-meter horizontal scale, no manmade structures. Existing coordinate/elevation utilities and eight previously recorded isolated tests are groundwork only. There is no implemented Earth world preset, licensed terrain dataset pipeline or complete Earth chunk generator yet. Preserve normal TFC generation as the default. World height, projection distortion, dataset licensing, offline caching, geology/climate integration and chunk performance remain explicit design/implementation requirements.

## History and evidence

See [WORK_LOG](docs/foundations/WORK_LOG.md) for commits, commands and validation limits. The complete previous status is retained unchanged in [the historical snapshot](docs/foundations/history/STATUS_BEFORE_LOOT_MIGRATION.md); historical privacy and target recommendations there are superseded by the authorized direction above.
