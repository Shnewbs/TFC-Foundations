# Version strategy investigation — 2026-10-08

Recommendation, not an applied target change: prioritize Minecraft 26.1.2 for the first playable 0.0.0 candidate, preserve the 26.3 migration branch, and retain 26.4 as a conditional future target. The active build configuration remains 26.3. A 26.1.2 target would still require substantial TFC API migration and runtime testing; it is not a quick configuration fix.

## Patchouli binary findings

Inspected the official July 10 release `release-26.1-94-beta`, asset `patchouli-neoforge-26.1-94.jar`, version 26.1-94. Download SHA-256 verified against the GitHub release asset digest:

`00f9e611930614eacd565916c906a4341363e4a43e271c1a01bda8227707dd49`

Its actual embedded metadata requires Minecraft `[26.1,26.2)` and NeoForge `[26.1.0.1-beta,26.2)`. Thus 26.1.2 is included, but 26.3 is excluded.

A constant-pool class-reference scan against the official, SHA-1-verified Minecraft 26.3 client JAR found seven missing class references, also absent from the selected NeoForge 26.3.0.58-beta universal JAR:

- `net.minecraft.advancements.criterion.EntityPredicate`
- `net.minecraft.advancements.criterion.MinMaxBounds`
- `net.minecraft.advancements.criterion.MinMaxBounds$Ints`
- `net.minecraft.advancements.criterion.SimpleCriterionTrigger`
- `net.minecraft.advancements.criterion.SimpleCriterionTrigger$SimpleInstance`
- `net.minecraft.client.renderer.MultiBufferSource`
- `net.minecraft.client.renderer.MultiBufferSource$BufferSource`

Affected Patchouli classes include BookOpenTrigger/TriggerInstance and MultiblockVisualizationHandler/GhostBuffers. This establishes concrete source-port work beyond widening metadata. It is a partial static check, not a launch test or exhaustive method/field/mixin compatibility audit. No Patchouli binary was modified or installed into the project.

Regression checks after any port: book opening and advancement triggers; recipe lookup/display; TFC custom pages; multiblock preview and placement; resource reload; dedicated-server startup; multiplayer guide sync. Upstream issue #868 reports broken recipe templates on Fabric 26.1; this is not proof of the same failure on NeoForge, but motivates checking recipe pages explicitly.

## Public ecosystem evidence

| Project | Documented evidence | Interpretation |
| --- | --- | --- |
| Create | Official development-status page lists 26.1 in progress, 1.21.2–1.21.11 skipped, and 1.21.1 continued support | Explicit 26.1 plan, no delivery date; not a 26.4 commitment |
| Twelve Iterations | FAQ recommends 26.1.x with long-time support through March 2028; 26.2.x/26.3.x experimental | Maintainer-specific support policy, not an ecosystem-wide LTS standard |
| Iron431 | June 24 roadmap targets 26.1.2, says no 26.2.x plans at that time, and favors the first drop of each year; expressly subject to change | Dated plan, not guaranteed current policy for every mod |
| Patchouli | July 10 NeoForge 26.1-94 beta release; repository's 26.x branch is 26.1 | Released guide dependency for 26.1.x; no 26.3/26.4 commitment found in inspected releases/branches/recent issues |
| Integrated Dynamics | Official project lists a 26.1.2 release and a 26.3 beta, both October 3 | Counterexample to universal skipping; newer drops do have adopters |
| TerraFirmaCraft | Public branch inventory through 1.21.x; official files list 1.21.1 and 1.20.1 releases September 27 | No public 26.4 timetable found in the sources checked; absence of a public branch does not prove no private work |

The evidence supports a concentration around selected versions, but does not establish the numerical claim that most mods skipped all later 1.21 releases. NeoForge's January 1 retrospective identifies 1.21.1 as its most popular version at that time. Create explicitly documents the skipping pattern.

## Why 26.3 costs more

Fabric's September 15 vanilla migration summary documents changes to recipes/advancements as reloadable registries, configured features, material rules, rendering and SDL input. These affect TFC's worldgen and UI and Patchouli's recipe/rendering code. Fabric-specific APIs are not being proposed for this NeoForge project; the relevant evidence is the underlying vanilla changes.

Official metadata rechecked October 8: latest Minecraft release 26.3, latest snapshot 26.4-snapshot-3; no released 26.4 and no NeoForge 26.4 artifact listed. Keep 26.4 as a destination after release, usable loader, critical dependencies and runtime tests. No reviewed source establishes 26.4 as the next shared modding target.

## Sources

- https://github.com/VazkiiMods/Patchouli/releases/tag/release-26.1-94-beta
- https://github.com/VazkiiMods/Patchouli/blob/release-26.1-94-beta/gradle.properties
- https://github.com/VazkiiMods/Patchouli/issues/868
- https://wiki.createmod.net/users/development-status
- https://mods.twelveiterations.com/faq
- https://www.patreon.com/Iron431/posts/june-2026-update-161961851
- https://www.curseforge.com/minecraft/mc-mods/integrated-dynamics
- https://github.com/TerraFirmaCraft/TerraFirmaCraft/branches
- https://www.curseforge.com/minecraft/mc-mods/terrafirmacraft
- https://neoforged.net/news/2025-retrospection/
- https://www.fabricmc.net/2026/09/15/263.html
- https://piston-meta.mojang.com/mc/game/version_manifest_v2.json
- https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml
