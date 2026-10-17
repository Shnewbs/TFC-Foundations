# Upstream tracking

## 2026-10-17 UTC — Publish outstanding upstream source integration

Imported the TerraFirmaCraft `1.21.x` delta from `e9d9a88a187d5a33064e2d86b2803f54238cadd6` through `c3b4d9536b82e5a9746dd793c586720a74108057` (42 commits) directly onto the existing `26.x` branch. This completes publication that was previously reported as prepared locally. It is a source port, not an ancestry merge. Upstream authors and EUPL notices are retained in the integration audit. Minecraft 26.3, NeoForge 26.3.0.58-beta and all dependency pins remain unchanged; Patchouli remains unresolved.

Includes the upstream kiln, banana/berry, squid, scraping, rope, offhand/tool-rack and pet fixes; soup fallback ordering; buoys/lobster traps; recipes, translations, guide pages, trim palettes and thatch spawn tag. Resolved conflicts retain this port's Identifier/InteractionResult/highlight APIs and adapt the new ChunkPos helpers to the inspected 26.3 record API. Removed the obsolete tool-rack main-hand interaction method so upstream's hand-aware block interaction is used.

Validation in this session: all 87 changed Java files pass syntax parsing; `python resources validate` passes language, parent, texture, blockstate, unused-model and generated-texture checks; the upstream runner passes 2,097,152 production scraping-mask checks plus source/resource guards. The ChunkPos helper signatures were inspected in the actual pinned Minecraft 26.3 client. These checks do not establish target-wide API compatibility.

**No full 26.3 compilation, transformed Mixins, client/server startup, world/save/reload, gameplay or JAR packaging was verified in this continuation.** New content still needs target-specific API/runtime validation as part of this incomplete port. The 203-error result belongs exclusively to the separate 26.1.2 checkpoint; it must not be attributed to this branch. No task branch or Actions run was created.


Upstream: https://github.com/TerraFirmaCraft/TerraFirmaCraft

Observed 2026-10-12. Rediscover branches each scan. Previous observations: 2026-10-08 through 2026-10-11; initial 1.21.x baseline remains e9d9a88a187d5a33064e2d86b2803f54238cadd6.

| Branch | Observed tip |
| --- | --- |
| 1.12.x | 23e6a438e5627da1203e0b199e1556db8ed5234f |
| 1.16.x | 1280317be9164bbf370312e34f0ede7e676973b0 |
| 1.17.x | afddbb2f477b0a7c6dee2f3f98e9fc81851f1f9e |
| 1.18.x | caf1ec2f527d79d15bda64871014b690b6484a59 |
| 1.20.x | 8bcb23fc2afb30fc9c111ae45c57f401007de253 |
| 1.21.x | 5e15971edbf03c82c1bfb32fae590f4b5c443009 |

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

## Watch report — 2026-10-11

Scope: rediscovered every upstream branch, compared the 1.21.x tip with the 2026-10-10 observation cursor, inspected all issues/PRs updated since the previous scan, checked releases, and rechecked the official Mojang and NeoForge version sources.

- Branch inventory remains six branches: 1.12.x, 1.16.x, 1.17.x, 1.18.x, 1.20.x and 1.21.x. No branch was added or removed.
- 1.12.x, 1.16.x, 1.17.x, 1.18.x and 1.20.x are unchanged.
- [1.21.x comparison](https://github.com/TerraFirmaCraft/TerraFirmaCraft/compare/26c93da28f21bb6931ae9dd2d8a780a1831e664a...f67a160166b7c49b44b601a7e02457cae72d1f8d): four forward commits; no history rewrite.
- Fork snapshots checked: 26.x at `b519cc9484489ec67bf85628de382ecff4eb67c6`; 26.1.2 at `0ae943330a49ae395d4dab94569a3cfa08dc7d2f`. Both remain based on the initial `e9d9a88a187d5a33064e2d86b2803f54238cadd6` ancestry cursor and are 15 upstream commits behind by ancestry. Targeted file inspection below distinguishes independently equivalent code from missing changes.
- No new upstream release. Latest observed releases remain 1.20.1 v3.2.27 (October 10) and 1.21.1 v4.2.11 (September 27).

### Relevant merged fixes

All regression tests below are REQUIRED, NOT RUN. No upstream commit was merged, no gameplay source or version pin was changed, and no artifact was published in this scan.

| Upstream reference | Affected system and finding | Status in Foundations 26.x and 26.1.2 | Regression tests needed |
| --- | --- | --- | --- |
| [ab42a68](https://github.com/TerraFirmaCraft/TerraFirmaCraft/commit/ab42a68683a68167c755e273de2c13540f563664), [#3682](https://github.com/TerraFirmaCraft/TerraFirmaCraft/issues/3682), merged October 11 | Mob spawning: adds TFC thatch to vanilla's `prevent_mob_spawning_inside` block tag, preventing hostile mobs from materializing inside a thatch block | Port candidate, not inherited. Neither branch has the generated tag file or corresponding BuiltinBlockTags entry. Adapt to the target-version tag API instead of blindly cherry-picking | Dark-room natural-spawn test with a thatch floor/volume; hostile mobs cannot spawn inside thatch; valid nearby solid blocks still spawn normally; passive spawning and player placement remain unchanged; datapack reload preserves the tag |
| [76dd9bb](https://github.com/TerraFirmaCraft/TerraFirmaCraft/commit/76dd9bbe0df2cca77162e25257fa2f4ce4e8f747), [#3748](https://github.com/TerraFirmaCraft/TerraFirmaCraft/issues/3748), merged October 11 | Armor rendering/resources: adds vanilla trim-material palette permutations for TFC armor textures. Upstream explicitly did not add TFC copper/iron to trim-material ingredient tags; the confirmed bug was missing textures | Port candidate, not inherited. Both branches' `assets/minecraft/atlases/blocks.json` end with TFC-specific `*_tfc` permutations and omit vanilla palettes | Apply all ten vanilla trim materials to every supported TFC armor material/piece; verify inventory, worn third-person and armor-stand rendering; no black/red missing texture; resource reload and multiplayer client display |
| [c1daeac](https://github.com/TerraFirmaCraft/TerraFirmaCraft/commit/c1daeac00973ab60aee17e90a153b75eca6f532a) and [f67a160](https://github.com/TerraFirmaCraft/TerraFirmaCraft/commit/f67a160166b7c49b44b601a7e02457cae72d1f8d) | Datagen refreshes accompanying the two fixes; also normalizes generated food-tag ordering | Not independent gameplay fixes. Do not import generated outputs without porting their generators and regenerating under the target toolchain | Datagen is clean/reproducible; generated tag and atlas match generators; food tag membership unchanged, including cooked armadillo |

### New or materially updated investigations

- [#3751](https://github.com/TerraFirmaCraft/TerraFirmaCraft/issues/3751), opened October 11: Prospector's Pick always reports nothing in a modpack. Current evidence points to another mod adding an invalid entry to `tfc:prospectable`; it remains an unconfirmed conflict with no fix. Both port branches still depend on that tag. Before calling the port's propick working, test direct ore, nearby ore at radius boundaries, an empty area, the generated tag, and malformed/optional compatibility entries. Verify the target NeoForge tag-inspection command and ensure a bad external datapack produces a diagnosable reload error rather than silent false negatives.
- [#3752](https://github.com/TerraFirmaCraft/TerraFirmaCraft/pull/3752), opened October 11 against 1.20.x: proposed guard prevents `promotePartial` from replacing initialized level-chunk data with null when cached partial data is missing, which otherwise can break capability serialization. It is open and unmerged. The current 1.21-derived port trees do not contain the legacy `ChunkDataProvider` or `promotePartial` path, so this exact patch is not applicable. Still add an analogous world-data regression test: generate a chunk, repeat any proto-to-level promotion path available in the target, save/reload, and verify TFC rock/climate/forest data serializes and remains stable. Include this in TFC Earth/worldgen testing.
- [#3716](https://github.com/TerraFirmaCraft/TerraFirmaCraft/issues/3716) was only retitled; no new crash evidence or fix. [#3747](https://github.com/TerraFirmaCraft/TerraFirmaCraft/issues/3747) and [#3750](https://github.com/TerraFirmaCraft/TerraFirmaCraft/issues/3750) only received classification labels. Retain their existing worldgen and loom/Jade tests; no new implementation claim.

### Version gate

Official sources checked 2026-10-11:
- [Mojang version manifest](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json): latest release **26.3**; latest snapshot **26.4-snapshot-3**. There is still no released 26.4 entry.
- [NeoForge Maven metadata](https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml), lastUpdated **20261011194634**: NeoForge 26.3 has advanced to stable **26.3.0.76**; there is no 26.4 artifact.
- 26.4 migration gate remains CLOSED. The 26.x branch remains pinned to `26.3.0.58-beta`; treat 26.3.0.76 as an upgrade candidate only after compile, client/server startup, worldgen, save/reload and multiplayer regression tests. No pin was changed in this watch run.

This report advances observation cursors only. It does not advance integration or verification status.

## Watch report — 2026-10-12

Scope: rediscovered all upstream branches, compared their exact tips with the October 11 cursors, inspected issues/PRs updated since that report and recent releases, and fetched the official version manifest and Maven XML directly.

- Six branches remain: 1.12.x, 1.16.x, 1.17.x, 1.18.x, 1.20.x and 1.21.x. No added/deleted branch. The five branches other than 1.21.x retain their recorded SHAs.
- [1.21.x comparison](https://github.com/TerraFirmaCraft/TerraFirmaCraft/compare/f67a160166b7c49b44b601a7e02457cae72d1f8d...5e15971edbf03c82c1bfb32fae590f4b5c443009): 17 newly reachable commits, forward history, zero behind. Some merged branch commits have earlier dates; reachability is the observation criterion.
- Fork source snapshots inspected: 26.x at `778f7af3e991fdfb81af019fa5934db908ddd1ed`; 26.1.2 at `c330bc326eb3c5ae88ea4cdbb8002df9634ac7ac`. Both have upstream merge base `e9d9a88a187d5a33064e2d86b2803f54238cadd6` and lack 32 upstream commits by ancestry. Independently equivalent code is distinguished below.
- No new upstream release: latest remain [1.20.1 v3.2.27](https://github.com/TerraFirmaCraft/TerraFirmaCraft/releases/tag/v3.2.27), October 10, and [1.21.1 v4.2.11](https://github.com/TerraFirmaCraft/TerraFirmaCraft/releases/tag/v4.2.11), September 27. Today's changes are branch changes, not a new released build.

### Relevant merged fixes

All dates below are October 12, 2026, UTC. Tests are REQUIRED, NOT RUN. Final upstream-tip files were checked in addition to individual patches.

| Upstream commit / issue | Affected system | Foundations source status | Regression tests needed |
| --- | --- | --- | --- |
| [0ea63b5](https://github.com/TerraFirmaCraft/TerraFirmaCraft/commit/0ea63b503a82b61ee967ab07868b66d0b35b1417), [#3754](https://github.com/TerraFirmaCraft/TerraFirmaCraft/issues/3754), 13:29 UTC | DATA LOSS: empty TFCBlockEntity parent overrides skip NeoForge persistent data and serializable attachments when saving/loading; Firmalife wooden hive reproducer | 26.x: not inherited, overrides remain empty. 26.1.2: independently equivalent parent calls already present through ValueOutput/ValueInput, introduced by [46401c0](https://github.com/Shnewbs/TFC-Foundations/commit/46401c030a6d2a6e8d9721147837237d8b03605d) on October 9. This is source equivalence, not upstream ancestry or runtime verification | Put persistent marker and serializable attachment on a derived BE; set changed; save, fully unload chunk, load a NEW instance, and restart server. Verify marker, attachment and inventory survive. Same-object round trips can hide the bug. Exercise Firmalife only with target-compatible builds; audit derived save/load super calls |
| [51f95d4](https://github.com/TerraFirmaCraft/TerraFirmaCraft/commit/51f95d47699c65911bbd91b18f6be018a148b872), [#3755](https://github.com/TerraFirmaCraft/TerraFirmaCraft/issues/3755), 13:26 UTC | JEI advanced shapeless recipes: focus links used ingredient indices against padded nine-slot grid; can throw or focus the wrong ingredient, e.g. Firmalife spoon | Not inherited in either branch; both TFCCraftingExtensions files still use inputSlots.get(primaryIndex). Port candidate; inspect actual target JEI layout API before applying | Recipes with 1, 2, 3, 4 and 5–9 inputs; primary ingredient in every position; matching/mismatching candidate counts; focus cycles input/output correctly; tool remainders preserved; spoon reproducer when compatible dependency exists |
| [1b8cd11](https://github.com/TerraFirmaCraft/TerraFirmaCraft/commit/1b8cd1174440c5a5464233e79a37c2cdec8ba9c2), [#3753](https://github.com/TerraFirmaCraft/TerraFirmaCraft/issues/3753), 13:50 UTC | Hide scraping: repeat clicks on a completed cell no longer consume knife durability or show scraping particles; adds scraping recipe tooltip | Not inherited in either branch: onClicked remains void and callers damage knife/do particles unconditionally. 1.20.x backport is not merged; maintainer requests a signature-preserving PR for that legacy branch | Newly scraped cell costs durability once; repeated cell click costs none and gives no scraping feedback; all 16 cells, edges, final conversion and extra drop exactly once; save/unload/reload preserves progress, dye and textures; multiplayer sync; tooltip only for incomplete valid recipe |
| [ea17ff4](https://github.com/TerraFirmaCraft/TerraFirmaCraft/commit/ea17ff4e6743b5b66cde7721ed2f45788e8ea9d0), [#3744](https://github.com/TerraFirmaCraft/TerraFirmaCraft/pull/3744), 13:52 UTC | Farmland/hoe/Jade climate temperatures at or below zero rendered null; splits item heat and climate display enums to prevent invalid COLOR climate config; device thermometer uses item heat mode | Not inherited in either branch: FarmlandBlock still calls heat-only format() for current and threshold temperatures. Port candidate; prior open-proposal status is superseded by this merge. Overlapping legacy PR #3745 remains open | Negative, zero and positive temperatures in Celsius/Fahrenheit/Kelvin/Rankine; crop min/max thresholds and climate/inventory/guide screens; old COLOR climate config reload/migration; thermometer in device versus ambient mode; preserve item heat colors and decimal precision |

### New features and other investigations

- [85fbb4a](https://github.com/TerraFirmaCraft/TerraFirmaCraft/commit/85fbb4a42e867686227105ba67d3278361e0ad16), 22:12 UTC: buoys and lobster traps, rope lowering/recall, bait/catch inventories, aquatic critter AI, loot/recipes/tags and renderers. [7c275fb](https://github.com/TerraFirmaCraft/TerraFirmaCraft/commit/7c275fb59190e1f3d5d658a5f56ba5501b9a3443), 22:13 UTC, supplies the trap renderer. Neither Foundations snapshot registers BUOYS or LOBSTER_TRAP; feature is not inherited. Optional future port, after compilation priority. Required coverage: supported water/source placement, blocked lowering path, rope count/refunds and disconnected line, moving traps preserves inventory/attachments without duplication, bait consumption and full-catch behavior, tagged species/loot, ordinary fishing bait and biome aquatic spawn regressions, chunk unload/restart, removal drops, all colors/orientations, rendering and multiplayer interaction/drinking dispatch.
- [#3740](https://github.com/TerraFirmaCraft/TerraFirmaCraft/pull/3740) merged 13:53 UTC via [04a8c1f](https://github.com/TerraFirmaCraft/TerraFirmaCraft/commit/04a8c1f879db5782424d54e1357d4a052a731ba2): complete Simplified Chinese guide translation and generated pages. Not inherited in ancestry; sampled lighting page differs in both forks, so complete generated update is absent. Validate macros/placeholders, links, recipe pages and reproducible datagen when porting.
- [#3751](https://github.com/TerraFirmaCraft/TerraFirmaCraft/issues/3751) closed at 06:10 UTC. [Maintainer diagnosis](https://github.com/TerraFirmaCraft/TerraFirmaCraft/issues/3751#issuecomment-6115097117) identifies TFC Metallum Overhaul (`tfcm`) referencing missing blocks through `#c:ores`, breaking `tfc:prospectable`; reporter acknowledged and planned to try removing it. No TFC fix commit or verified successful retest is established by closure. Retain generated prospectable-tag/invalid external tag and direct/nearby/empty propick regression tests.
- [#3752](https://github.com/TerraFirmaCraft/TerraFirmaCraft/pull/3752) remains OPEN and UNMERGED against 1.20.x; updated 13:30 UTC. No new implementation evidence; retain previous world-data serialization/promotion test plan rather than importing the legacy capability patch.
- #3640 received an update but no new comment evidence or merged fix was identified. Existing armor-trim closure is already covered by the October 11 report; do not duplicate it as today's fix.
- Merge, AGENTS documentation and language-conflict cleanup commits do not establish additional tested gameplay fixes. Upstream attribution/licenses remain required for any future port.

### Version gate

Official metadata fetched directly on 2026-10-12:
- [Mojang manifest](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json): latest release **26.3** (September 15); latest snapshot **26.4-snapshot-3** (October 6). No released 26.4 entry.
- [NeoForge Maven metadata](https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml): latest **26.3.0.76**, lastUpdated **20261011194634**; no 26.4 version. Unchanged since previous report.
- 26.4 migration remains unavailable. The 26.x branch still targets Minecraft 26.3 / NeoForge 26.3.0.58-beta; 26.1.2 remains a separate playable-build priority. No target/pin changed.

Only documentation/observation cursors were updated by this watch. No upstream code merge, fix implementation, compile/runtime verification or artifact publication occurred. Integration and verified-release status do not advance.
