# TFC Foundations — Current Project Status

Updated 2026-10-09 UTC (October 8 in America/Los_Angeles).

**Version 0.0.0 is an incomplete source port, not a playable release. Neither target has passed full compilation, client/server startup, or survival regression tests. No playable JAR or release was produced in this continuation.**

## Authorized direction

Continue public development in Shnewbs/TFC-Foundations, preserving upstream history, the EUPL license, credits and `tfc` resource IDs. Prioritize a playable Minecraft 26.1.2 build. Maintain Minecraft 26.3 on `26.x` as the foundation for a separately verified 26.4 port. Do not assume a version-number change establishes compatibility.

| Track | Pinned target | Latest source/validation checkpoint |
| --- | --- | --- |
| Priority playable track, `26.1.2` | Minecraft 26.1.2; NeoForge 26.1.2.114; JEI 29.43.0.107; Patchouli 26.1-94 | `8d27f34a1d76535f6b359b5151295ff8fc16f0e6`: common API checkpoint and CI probes published; 55 isolated checks pass; full compilation fails with 3,438 reported errors. |
| Forward-port track, `26.x` | Minecraft 26.3; NeoForge 26.3.0.58-beta; JEI 31.9.0.61 | `6d3180c130b9a907be5f31511fb34e17039d5210`: exact target API and compiler evidence captured; full compilation fails. Required Patchouli target remains unresolved. |

Both use Java 25 and Gradle 9.2.1. Optional EMI/Jade/TOP adapters remain isolated and incomplete. Required dependency and publication gates have not been bypassed.

## Earlier loot checkpoint

On 26.1.2, loot registries now hold their direct MapCodec values, custom context parameters use ContextKey, and the loot package uses JSpecify defaults. All 11 registered loot IDs and three custom context IDs are unchanged. Source audits confirm the animal/crop yield calculations are unchanged apart from target API accessor names. Nested min/max providers now receive validation. These source checks do not prove in-game drops.

[Validation run 37872611533](https://github.com/Shnewbs/TFC-Foundations/actions/runs/37872611533) tested source commit `cc04e4f`:

- PASS: actual production AlwaysTrueCondition codec identity, builder singleton, empty-object encoding, and codec round trip (four checks).
- PASS: actual production MinMaxProvider and loot package metadata compile against the resolved Minecraft/NeoForge classpath in an isolated javac invocation.
- PASS: existing resource validation, including language, model parents, textures and blockstates, reports zero errors in its checked categories.
- FAIL: the full Gradle compilation/build still reaches the 1,000-error display cap. The diagnostic report groups repeated messages; neither this cap nor its grouped count is the total migration backlog. No gameplay, performance or complete loot-package test has passed.

Both branches now preserve build logs, structured compiler reports, resolved API signatures, and exact source snapshots as short-lived CI artifacts. Build failures remain failures; successful diagnostic or resource steps do not make the job green.

## Common API checkpoint — 2026-10-09 UTC

The next playable milestone remains **a fully compiled 26.1.2 build**, not merely passing isolated probes. The common-source checkpoint updates package defaults, facing properties, inventory input, teleport entry points, model-data packages, level accessors, chunk-position records and NBT reads. It preserves serialized IDs and leaves core gameplay, guide and rendering sources enabled.

- Exact-target local tooling is now available: the resolved 84-JAR classpath and Java 25 compiler from successful run `37873949671` were downloaded and SHA-256 verified. This supersedes the earlier local-toolchain limitation. These development inputs are not a playable mod.
- **PASS:** all 113 main/data/test package-info files compile independently against the exact classpath. Existing nullability imports elsewhere have not all been standardized; this is not a complete nullness analysis.
- **PASS:** 51 standalone checks for the actual new NbtHelpers and Unchecked utilities, NBT defaults/type preservation, facing property values and inventory input actions. Four existing actual loot codec checks also still pass.
- **COMPILE ONLY / NOT RUN:** ChunkPos, component-patch and integer-provider runtime probes require the real NeoForge bootstrap. A plain-Java attempt exposed that requirement; no fake loader or Minecraft stub was substituted.
- **FAIL:** the completed diagnostic main-source pass reports 3,438 errors, compared with 3,979 at the earlier full traversal in this session. Earlier 1,341/846 counts ended prematurely and are not comparable backlog totals. These numbers are diagnostics, not a completion percentage. Full Gradle/mixin/data/test validation and gameplay remain separate gates.

NBT list reads retain all-or-nothing element-type validation, including mixed-list rejection. Missing numeric values retain explicit legacy defaults; old strict tag checks do not silently become numeric coercions. Reflective exception helpers no longer rely on the removed transitive noexception library and preserve the original thrown object. These changes do not finish entity/block-entity ValueInput/ValueOutput migration.

The 26.3 source is unchanged by this checkpoint. Remaining major areas include render-state/model and JEI/Patchouli migration, save lifecycle hooks, registration/holder APIs, tool/equipment changes, fluid/inventory capability APIs and world generation. No client/server launch, world creation, save/reload, survival test, performance result, playable JAR or release is claimed.

## Published checkpoint and CI confirmation

Source commit `fd86c879b08723d8253d95b1304a67d266268dde` applied the 345-file reviewed checkpoint with full preimage/postimage checks and removed its temporary transfer files. Follow-up `8d27f34a1d76535f6b359b5151295ff8fc16f0e6` connected the new common API suite to CI after artifact review caught that its invocation was missing from the initial transfer. The combined migration patch changes 346 files. Public history was preserved; no force push was used.

[Validation run 37878610233](https://github.com/Shnewbs/TFC-Foundations/actions/runs/37878610233), testing `8d27f34a`:

- **FAIL:** full Gradle build / compileJava, exit 1; javac reports 3,438 errors. The summary groups these into 3,199 path/line/message entries; neither number is a complete project-work estimate. Downstream compilation and mod runtime tests remain blocked.
- **PASS:** 51 common utility/API checks and four production loot codec checks, now independently confirmed in GitHub Actions as well as locally. All 113 package-info files compile in the separate probe.
- **PASS:** main/data/test license checks and the existing resource validation. Resource validation reports zero errors in its checked categories, but does not establish rendering or gameplay correctness.
- **NOT RUN:** the bootstrap-dependent chunk/component/provider runtime probes, client/server launch, world creation, save/reload, multiplayer, survival progression and performance measurements.

Artifact `11593477212` (`tfc-port-diagnostics-37878610233-1`) retains the logs, reports and exact source snapshot for seven days. Its downloaded SHA-256 was verified and the source snapshot matches the reviewed local files. The portable local main-source runner also completed: 1,631 source files, exit 1, 3,438 diagnostics, no timeout. The normal build remains failed; no playable JAR or release exists.

## Important 26.3 divergence

Compared the exact resolved APIs from both branches. On 26.3, LootContext uses `getOptional`, conditional loot functions use holder-based conditions, and the old NumberProvider/NumberProviders pair has been replaced by separate context integer/float provider families. **The 26.1.2 loot patch has not been copied to 26.x.** It requires a dedicated source and data-format migration, including preserving integer drop-count behavior.

## Next playability gates

1. Finish common API blockers: removed/relocated gameplay classes, ValueInput/ValueOutput save hooks, capabilities, equipment, serialization and registry APIs. Preserve behavior and validate each slice against the exact target.
2. Complete rendering/model, JEI and Patchouli adapters; do not remove core visuals or the guide merely to achieve compilation.
3. Pass main/data/test compilation, licenses, resource generation and packaging. Then test client and dedicated-server launch, new-world creation, save/reload, multiplayer and survival progression.
4. Validate 26.3's separate loot, holder and world-generation migrations before sharing branch changes. Publish target-specific GitHub releases only after their stated gates pass.

## Earth option

The requested optional natural Earth mode remains in scope: nominal one-block-per-meter horizontal scale, no manmade structures. Existing coordinate/elevation utilities and eight previously recorded isolated tests are groundwork only. There is no implemented Earth world preset, licensed terrain dataset pipeline or complete Earth chunk generator yet. Preserve normal TFC generation as the default. World height, projection distortion, dataset licensing, offline caching, geology/climate integration and chunk performance remain explicit design/implementation requirements.

## History and evidence

See [WORK_LOG](docs/foundations/WORK_LOG.md) for commits, commands and validation limits. The complete previous status is retained unchanged in [the historical snapshot](docs/foundations/history/STATUS_BEFORE_LOOT_MIGRATION.md); historical privacy and target recommendations there are superseded by the authorized direction above.
