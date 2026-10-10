# Upstream tracking

Upstream: https://github.com/TerraFirmaCraft/TerraFirmaCraft

Observed 2026-10-10. Rediscover branches each scan. Previous observation: 2026-10-08; initial 1.21.x baseline remains e9d9a88a187d5a33064e2d86b2803f54238cadd6.

| Branch | Observed tip |
| --- | --- |
| 1.12.x | 23e6a438e5627da1203e0b199e1556db8ed5234f |
| 1.16.x | 1280317be9164bbf370312e34f0ede7e676973b0 |
| 1.17.x | afddbb2f477b0a7c6dee2f3f98e9fc81851f1f9e |
| 1.18.x | caf1ec2f527d79d15bda64871014b690b6484a59 |
| 1.20.x | 8bcb23fc2afb30fc9c111ae45c57f401007de253 |
| 1.21.x | 26c93da28f21bb6931ae9dd2d8a780a1831e664a |

These are observation cursors, not claims of integrated historical fixes. Daily upstream/version monitoring is active.

Process:

1. Compare branch tips with cursors; inspect commits, merged PRs, new/updated/reopened issues and releases. Detect new/deleted branches and rewritten history.
2. Track by upstream SHA and issue/PR, with versions, reproducer, severity and relevance. Open PRs are proposals; closed issues do not alone establish merged fixes.
3. Use explicit states: observed, investigate, already inherited, not applicable, port planned, implemented, verified. Separate observation cursors from integration cursors.
4. Prioritize duplication/crash/data loss. Port the smallest applicable change, preserving upstream SHA/attribution; do not blindly merge legacy branches.
5. Record downstream commit, regression results and containing release. Update the work log.

## Initial triage sample (October 8; superseded where noted below)

| Reference | Finding | Status / follow-up |
| --- | --- | --- |
| e9d9a88a1; #3719/#3737 | Enclosed-atoll surface water | Inherited in source; test fresh atoll worlds after port |
| 6e77733cb; #3688 | Waterlogged-block bucket duplication | Inherited; test pickup/placement and counts |
| 6e77733cb; #3713 | Pot/crucible capability fill sync | Inherited; test automation and two clients |
| 6e77733cb; #3391 | Animal growth crash/future birthdays | Inherited; test calendar changes and entity loading |
| bcbaa8bb3; #3700 | Falling blocks and boats | Inherited; collision/block recovery checks |
| 1.20.x e818b1f27493; PR #3742 | Tall crop support | Legacy candidate; inspect applicability |
| #3743; open PRs #3744/#3745 | Farmland tooltip at/below 0 C | Unmerged; evaluate overlapping proposals |
| #3293 | Kiln heating behavior | Open; reproduce |
| #3721 | Flattened stratovolcano | Open; collect seed/settings |
| #3747 | Rosewood on ocean floor in volcanic arc | Open; seed-based test candidate |
| Open PR #3746 | Underground bushes | Review branch, intent and merge state |
| Open PR #3741 | 1.20.1 berry bushes | Review applicability |

Links: `https://github.com/TerraFirmaCraft/TerraFirmaCraft/issues/NUMBER` or `/pull/NUMBER`. This sample is not a complete historical bug audit.

Version baseline: Mojang release 26.3, snapshot 26.4-snapshot-3; latest observed NeoForge 26.3.0.58-beta, no 26.4 entry. Recheck official manifest/Maven metadata before changing build pins.

## Watch report — 2026-10-10

Scope: rediscovered all upstream branches; compared both changed tips against the October 8 observation cursors; inspected the nine issues/PRs returned by the updated-since October 8 feed and the latest ten releases. Six upstream branches remain: no new or deleted branch. 1.12.x, 1.16.x, 1.17.x and 1.18.x are unchanged. The two changed histories are forward additions, not rewrites.

- [1.21.x comparison](https://github.com/TerraFirmaCraft/TerraFirmaCraft/compare/e9d9a88a187d5a33064e2d86b2803f54238cadd6...26c93da28f21bb6931ae9dd2d8a780a1831e664a): 11 newly reachable commits, including merge/refactor/style commits. One merged branch commit is dated October 4; reachability, not commit date alone, identifies it as newly observed.
- [1.20.x comparison](https://github.com/TerraFirmaCraft/TerraFirmaCraft/compare/e818b1f27493e87e3be6db1172b0392e0752eedb...8bcb23fc2afb30fc9c111ae45c57f401007de253): three new commits.
- Fork source snapshots: 26.1.2 at `1c4a9fffcca6b7918239c2444ec5a2418bcf91f3`; 26.x at `c40c4fdf8a9b973fabe7588e00f625b84871f7c0`. Both comparisons have merge base `e9d9a88a187d5a33064e2d86b2803f54238cadd6` and lack the 11 new upstream commits in ancestry. Targeted source inspection below checks for equivalent changes independently.

### Relevant fixes and required regression coverage

All tests below are REQUIRED, NOT RUN. No gameplay source was changed, no upstream merge performed, and no playable artifact published in this scan.

| Upstream reference | Affected system and finding | Status in both fork snapshots | Regression tests needed |
| --- | --- | --- | --- |
| [4f05f12](https://github.com/TerraFirmaCraft/TerraFirmaCraft/commit/4f05f12edf5867e242668fae3ff6c0fc83b37a9c), [#3293](https://github.com/TerraFirmaCraft/TerraFirmaCraft/issues/3293), October 10 | Kiln/firebox: catch-up heating after unloading, fuel exhaustion/cooling, initial interior discovery; also placed-item temperature display and Jade server data | Investigate/port candidate; core catch-up code absent. Both FireboxBlockEntity versions consume skipped fuel without performSkippedHeating. Issue closure is backed by an actual commit; does not establish all reported symptoms are solved | Compare loaded versus unloaded kiln results; fuel ending before/after heating threshold; paused calendar; save/reload; place items on grate during heat-up; inventory counts; two-client temperature display when Jade is available |
| [3501f17](https://github.com/TerraFirmaCraft/TerraFirmaCraft/commit/3501f1753a050f15bf0bcca3eb090d57e0ce9844), October 10 | Banana scheduled tick reads LIFECYCLE after the block may have changed | Investigate/port candidate; block-identity guard absent from BananaPlantBlock in both | Remove/replace/die banana before scheduled update; calendar skip; normal growth/fruit cycles; no crash or phantom growth |
| [6b9749b](https://github.com/TerraFirmaCraft/TerraFirmaCraft/commit/6b9749b4b9d4e165c1cc88c9d21bc29928713a78), October 10 | Defensive property guards for stale firepit, firebox, pot, bloomery, forge and blast-furnace block entities | Not inherited as a complete fix; inspected firepit and firebox still read missing properties unguarded. Upstream itself calls this a possible fix, not a proven diagnosis | Replace heated device while retaining/queuing its calendar callback, then update/reload; no missing-property crash; ordinary heat/fuel behavior unchanged; extend coverage to all six device classes |
| [d3b7616](https://github.com/TerraFirmaCraft/TerraFirmaCraft/commit/d3b76169c9d7326c05714bdf863201863f070188), [#3749](https://github.com/TerraFirmaCraft/TerraFirmaCraft/pull/3749), [d676f97](https://github.com/TerraFirmaCraft/TerraFirmaCraft/commit/d676f9776ec4c4203ea30f65c59bc8d6b85bcc9d), October 10 | Squid/octopoteuthis spawn cancellation instead of discarding before insertion | Investigate/port candidate; both TFCSquid files still discard(). Verify target NeoForge cancellation API before porting | Constrained water volume rejects minimum-size spawn without removed-entity warning; open-water spawns still succeed; check both species and entity counts |
| [#3746](https://github.com/TerraFirmaCraft/TerraFirmaCraft/pull/3746), [26c93da](https://github.com/TerraFirmaCraft/TerraFirmaCraft/commit/26c93da28f21bb6931ae9dd2d8a780a1831e664a), [ab18e7e](https://github.com/TerraFirmaCraft/TerraFirmaCraft/commit/ab18e7e4a19850714bd43e3b8a6da0e8ae351c14) | Underground berry spreading samples near the bush, rather than projecting onto the ocean-floor heightmap; cranberry substrate position correction also present in 4f05f12 | Investigate/port candidate; both StationaryBerryBushBlock files still use OCEAN_FLOOR and both WaterloggedBerryBushBlock files still inspect pos.below() | Underground and surface growth in active season; blocked destinations; slope/height variation; cranberry valid substrate directly below bush versus invalid substrate; no unwanted replacement |
| [#3741](https://github.com/TerraFirmaCraft/TerraFirmaCraft/pull/3741), [59118f1](https://github.com/TerraFirmaCraft/TerraFirmaCraft/commit/59118f1acfc341b84beb4981876be5204c71d560), [8bcb23f](https://github.com/TerraFirmaCraft/TerraFirmaCraft/commit/8bcb23fc2afb30fc9c111ae45c57f401007de253), October 10 | 1.20.x bush lifecycle/placement corrections and cranberry calendar server ticker | Mixed: active-lifecycle gating already equivalent in newer fork code; cranberry registration still lacks this server ticker. Legacy propagation code differs substantially; not a direct cherry-pick | Cranberry growth after unload/calendar advance; active versus dormant season; spreading validates destination rather than parent bush; determine whether newer lifecycle architecture needs same ticker |
| [#3742](https://github.com/TerraFirmaCraft/TerraFirmaCraft/pull/3742), [e818b1f](https://github.com/TerraFirmaCraft/TerraFirmaCraft/commit/e818b1f27493e87e3be6db1172b0392e0752eedb), now released | 1.20.x climbing crops could destroy unrelated blocks/bedrock above when dying | Exact legacy patch not inherited; equivalent bedrock protection already present in both ClimbingCropBlock.die methods, which modify only a matching crop above. Other supported-crop behavior still needs testing | Crop death under bedrock/stone; supported versus unsupported crop; top/bottom drops and no duplication |
| [3fa1023](https://github.com/TerraFirmaCraft/TerraFirmaCraft/commit/3fa102323f128d6d7c340b29bc91c076a525c280), October 10 | New recipe: beach groundcover seaweed produces three soda ash at 500 degrees, with heat capability data | Not inherited; from_groundcover_seaweed recipe absent from both HeatRecipes generators | Datagen output/load; heat below/at threshold; exact input/output counts and recipe viewer |

The 1.21.x refactor/style commits 3927f57, 37de6b0 and 5fc5322 introduce helper abstractions or rearrange code; no separate gameplay fix is established. Do not import old ResourceLocation/ChunkPos APIs into the port wholesale. The temporary underground-temperature behavior in ab18e7e is NOT retained in the final comparison: final getAverageTemperature remains behaviorally equivalent to the previous elevation calculation. Do not advertise it as a new climate feature.

### Issues, proposals and release

- New release [TerraFirmaCraft 1.20.1 v3.2.27](https://github.com/TerraFirmaCraft/TerraFirmaCraft/releases/tag/v3.2.27), published **2026-10-10 14:05:22 UTC**: climbing-crop/bedrock, squid-spawn warning and berry/cranberry fixes. This is an upstream legacy release, not a Foundations 26.x build. Latest observed 1.21.1 release remains v4.2.11 (September 27).
- New/open [#3748](https://github.com/TerraFirmaCraft/TerraFirmaCraft/issues/3748), updated October 10: armor-trim ingredient tags and missing textures. No merged fix identified and no downstream fix verified. Test TFC copper/gold/iron as trim ingredients and all supported material/armor combinations in inventory, worn rendering and resource reload. Relevant to the active item-model/armor migration.
- New/open [#3750](https://github.com/TerraFirmaCraft/TerraFirmaCraft/issues/3750), October 10: 1.20.1 loom Jade progress tooltip off by one/desynchronized. Downstream applicability unconfirmed, especially while Jade is isolated. Test every click including final completion with two clients when compatible Jade is available.
- [#3558](https://github.com/TerraFirmaCraft/TerraFirmaCraft/issues/3558) closed October 10 with a tentative maintainer comment that it is fixed in 1.21; timeline has no closing commit. Treat as investigate, not verified inherited. Check sealed-barrel JEI/EMI duration against actual calendar progress under default and changed day lengths, including indefinite (-1) recipes.
- [#3740](https://github.com/TerraFirmaCraft/TerraFirmaCraft/pull/3740), Simplified Chinese guide translation, updated October 10 but still open; no merged update to inherit. Validate macros/placeholders and generated guide pages if later accepted.
- [#3744](https://github.com/TerraFirmaCraft/TerraFirmaCraft/pull/3744), farmland tooltip at/below zero, remains open. It was already tracked; do not treat it as a newly merged fix. Retain tests at negative/zero/positive temperatures in all units.

### Version gate

Official sources checked 2026-10-10:
- [Mojang version manifest](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json): release **26.3**; snapshot **26.4-snapshot-3** (released October 6). No released 26.4 entry.
- [NeoForge Maven metadata](https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml), lastUpdated **20261010213803**: newest listed 26.3 build **26.3.0.69-beta**, up from observed 26.3.0.58-beta. No 26.4 version.
- 26.4 migration gate remains CLOSED. No dependency pin changed. Assess the newer 26.3 loader separately before upgrading; no compatibility/build/runtime validation performed here. Preserve 26.1.2 playable-build priority and the 26.x / 26.3 development track.

This report advances observation cursors only. Integration cursor remains the initial 1.21.x base for upstream ancestry; independent port changes are not evidence that these new upstream fixes are implemented.
