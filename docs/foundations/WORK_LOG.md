# TFC Foundations — Work Log

## 2026-10-24 — First standalone cave-in alpha

`feature/26.1.2/cave-ins` now has an independent `standalone/cave-ins` build.
Its 31,026-byte JAR requires only matching Minecraft/NeoForge: no
TFC, shared Foundations core, gravity mod, Patchouli, JEI or item-weight mod.
The full TFC source and IDs remain unchanged. Supports/collapsible materials
are datapack tags; pending queues are level attachments and work has explicit
per-level probe/fall/queue bounds. Native falling entities use vanilla landing
and rendering; block entities, unbreakable blocks and immune tags are excluded.

Pinned Java 25/NeoForge 26.1.2.114 build passes: 16 tests, zero failures/errors/
skips. Native packaged-JAR server validation passes nine checks: roof falling/
landing, beam support, tagged chest exclusion, tagged immune-block exclusion,
untagged-material exclusion, tag reload enabling dirt, real explosion-triggered
falls, queued collapse surviving save/full restart, and supported roof retention.
The FML catalog contains only cave-ins, Minecraft and NeoForge. Saved-chunk NBT
confirms the queued roof was still stone before restart and its landed material
after restart. All 11 production classes match the packaged compiled bytes;
no TFC/test classes, bundled dependencies or additional mod requirements exist.

SHA-256: `3c7bcfe21a99ec7493e1ed66550e785bdf4194cb718dcdee7dfd6a540d6a8abf`.
Exact results, source/class hashes, host NeoForm/FML accommodations, scope
changes and untested gates are in `validation/2026-10-24-standalone-caveins.json`.
This is an alpha candidate, not full TFC cave-in parity or release certification.
Real-player mining, graphical/multiplayer play, third-party cancellation/claim
behavior and profiling remain open. Other system branches and 26.x are still
unextracted. This change is published on the cave-in branch, not merged into
the full-mod version targets. Actions remain suspended; `[skip ci]` is used.

## 2026-10-24 — Standalone-mod clarification

The user clarified that each gameplay branch must produce an independently
installable mod: a modpack can install cave-ins without TFC or other systems.
The earlier complete-mod Git-only interpretation was incorrect and is superseded.
The 56 refs remain development lanes. Extraction is not completed by creating
refs or scaffolds. Independent projects live under `standalone/<system>`; local
falling/support mechanics belong inside cave-ins, with optional integrations.
Core/build-validation are infrastructure lanes. Existing full TFC source, IDs,
assets, history and prior validation are preserved. No standalone JAR or native
standalone startup is claimed by this policy checkpoint. Cave-ins is the first
implementation target, followed by remaining gameplay systems and separate
per-version validation. Actions quota remains suspended; use `[skip ci]`.

## 2026-10-24 — Independent system feature branches

The user replaced the old no-feature-branches policy and explicitly selected
independent Git changes with the full mod retained on each branch. The registry
maps 28 systems to `feature/26.1.2/<system>` and `feature/26.x/<system>`: cave-ins,
gravity, food, animals, player survival, farming, ecology, climate/calendar,
worldgen, geology/ores, volcanoes, Terralith, Earth, metallurgy, heat/fire,
glassworking, crafting, fluids, storage, machines, tools/combat, building,
transport, rendering, interface/guide, integrations, core and build/validation.
Initial refs share their matching version's documentation/tooling checkpoint;
there are no new gameplay changes or separate installable modules.

Feature changes must build and merge against the version baseline without
unmerged sibling work. Shared prerequisites are integrated first, then feature
branches refresh from the version branch. Existing integrated fixes and history
are preserved; no code is removed to manufacture feature diffs. The policy,
agent instructions and continuation entry now describe this workflow.

Validation: eight real-Git regression tests pass for independent changes,
stacked sibling rejection, accepted integrated shared changes, stale bases,
unknown versions/systems, missing refs, forbidden declared dependencies and
shallow-history rejection. All 98 navigation anchors resolve on 26.1.2.
The branch guard is a history check; it cannot prove semantic independence or
replace build/runtime tests. This checkpoint changes documentation and Python
tooling only. No Gradle build or new JAR is claimed; the 26.1.2 source baseline
is `fa586e95149db8356f1d3c6438d3c1ccd8575a46`, and the 26.x source baseline is
`64bc0554bf820464cbefb57c551b3007b6298a1f`. Existing per-version build/runtime
limits remain. Publication uses `[skip ci]`.

## 2026-10-17 UTC — Natural landscapes and live-world loading

The optional **TerraFirmaCraft — Natural** preset (`tfc:natural`) adds smooth,
convex biome blending and small weathered rock arches while retaining TFC
geology, climate, rivers, aquifers, vegetation and progression. Older settings
and `tfc:overworld` retain ordinary generation. The customization screen keeps
the profile, and existing chunks are not regenerated. Arches stay inside their
chunk, use local hardened rock and reject water, steep foundations and structure
reference chunks. Optional Terralith arch density changes their weathering;
Terralith's generator, biomes, ores and loot are not merged into TFC.

Real world creation exposed recipe stacks being decoded before default
components bind. Outputs now use immutable native item/fluid templates and
materialize stacks when used. Existing JSON and network representations and
native stack constructors/getters are retained. Both item and fluid pre-binding
regressions, byte-identical network checks and food display/crafted decay tests
pass. All 7,481 recipes load in the dedicated runtime. Two advancement icons now
reference existing items. Explicit native no-loot declarations are accepted while
all declared loot tables remain validated; no self-tests are disabled.

Client joining exposed 3,720 missing names: block items now inherit their block
names, with explicit item names retained for seeds and appropriate standalone
block items. A native registry test checks every TFC item. Duplicate creative-tab
entries from shared mappings and flowing/source fluids are coalesced without
losing component variants or parent/search visibility. All ten tabs build through
the native duplicate-checking output. This repairs JEI category omissions.
Exploration also reproduced predator construction failures. Brain builders no
longer access the owner's unassigned brain; schedules apply after construction
and after saved brain restoration. Solitary, pack and amphibious native brain
initialization tests pass, with live wolf/bear/crocodile/lion spawn checks.

The pinned Java 25/NeoForge 26.1.2.114 full build passes: **253 tests passed,
12 existing skips, zero failures/errors**. Native data generation covers 8,501
files and a repeat writes zero changes. Authored assets/resource validation
passes. Regional rainfall/geology parity covers 289 positions; normalized biome
weights and overlapping samples cover positive/negative chunk boundaries.
Geometry tests cover connected stone, grounded feet, an open walkway and gentle
versus steep foundations. Actual graphical worlds display terrain and foliage;
resource/item/data self-tests remain enabled and fatal. Optional Terralith 2.6.2
with Lithostitched 1.8.1 is exercised separately from standalone TFC.

A forced full source compile verifies record/lambda body updates after a stale
incremental class was detected. Compilation is nonincremental, and JARs exclude
datagen `.cache` bookkeeping.

Fresh standalone and Terralith worlds generate arches at block -8120, 84, -8264.
A live reload succeeds. After save, full server restart and client reconnect,
all three sampled full-chunk block hashes are unchanged and all three tagged
wildlife types are preserved.

JAR SHA-256: `9ebdeaeeba4fe8302cd03ce404b1a19a77aa56af31a75ddb97c81198d76cd490` (68,863,301 bytes).

Exact final fresh-world, reload, persistence and artifact evidence is in
`docs/foundations/validation/2026-10-17-natural-worlds.json`. The comparison with
an earlier ordinary seed-42 world retains biome/substrate data but its groundcover
and vegetation differ; do not claim whole-world byte-identical decoration or
universal generation-order determinism. Legacy unused item-model loader errors
and a non-fatal development-server client Button class probe remain. Two-client
multiplayer, complete survival progression, old-world migrations and the user's
reported NeoForge 26.1.2.122 remain outside this validation. This is a tested
candidate, not a claim of perfect terrain or completed release certification.
Publish directly to existing `26.1.2` with `[skip ci]`; `26.x` is unchanged.

## 2026-10-17 UTC — Cinder-cone lava placement and flooded crater replacement

The uploaded `2026-10-17_11.55.22.png` shows water in a small basalt cone with
magma decorations near block -1627, 161, -912. No seed was supplied, so this
checkpoint does not claim a matching-world visual reproduction. Source review
and before/after native regressions confirm two defects consistent with it.

Cinder-cone clearance was sampling near world origin rather than the cone's own
center. This could skip lava decoration on isolated cones and accept crowded
ones based on unrelated terrain. It now uses the existing center-relative
clearance helper. The cutoff, biome/rarity policies, cone shape, height and
distribution stay unchanged. Disabled frequencies still yield no centers.

The shared flood-fill feature also stopped its floor search on water it was
configured to replace. It then tried to start above the contained pool, where
the next layer can spill beyond its bounds and abort. It now finds the floor
through the existing permitted-fill predicate. The configured fluid, 3x3-chunk
boundary, minimum size and fluid-update scheduling remain. Ordinary water
features and intentional volcanic crater lakes/springs are retained.

Before repair, **4 of 9 new tests fail**: two flooded-crater cases and two local
clearance cases. After repair, **all 9 pass**. Full pinned Java 25/NeoForge
26.1.2.114 build passes in 1m 2s: **236 passed, 12 existing skips, zero failures
or errors**. Native feature fixtures verify a complete 363-block replacement,
dry lava/water configurations, negative chunk boundaries and no writes/ticks
for an uncontained basin. Deterministic cinder-noise checks cover local versus
origin clearance and disabled frequency. These are actual production feature
and noise calls in native fixtures, not a completed in-game world test.

JAR SHA-256: `1056e8d0be2479c550a912699092a9dc5b26a20a8e59b83b7ba2d1e34162f175` (68,844,062 bytes).
The two changed production classes match their tested compiled bytes in the
JAR, and eight authored lake configurations match the packaged resources.
Details: `validation/2026-10-17-volcano-crater-fluids.json`.
No resource IDs, assets, dependency pins or candidate version change. Existing
chunks are untouched; check newly generated cones in a separate fresh world.
Exact-world appearance, full new-world/save/restart/survival and the earlier
live server reload limits remain open. The user reported NeoForge 26.1.2.122;
this build tests pinned 26.1.2.114. TFC + compatible Terralith additions remain
required and separate from this repair. Publish on existing `26.1.2` with
`[skip ci]`; the independent `26.x` branch is unchanged.

## 2026-10-17 UTC — Seasonal foliage, groundcover atlases and terrain density

The three uploaded screenshots now supply the missing visual evidence: seasonal
foliage/ground plants show missing-model cubes, and terrain has abrupt walls.
Inline seasonal alternatives now explicitly declare their parent dependencies;
the native default dependency hook does not do this for CuboidModel children.
All original model IDs, seasonal geometry, tint rules and blockstates remain.
Native discovery resolves all four white-cedar parents and their geometry and
particle materials. A real client reload also exposed 17 item sprites used by
placed ores, groundcover and a fern. TFC-owned sprites now resolve exclusively
to the blocks atlas; three vanilla images use runtime aliases with unchanged artwork. Generators and native
resource-manager regression tests cover the atlas policy.

River density composition was retaining the full incoming value and adding a
weighted replacement, counting it twice. The production helper now starts at
zero. Three regression tests fail before and pass after, including no-river
identity and complete/partial river replacement. This repairs a confirmed
generation defect; it does not establish that every photographed wall is fixed.
Existing chunks are unchanged. Compare fresh worlds and new-chunk borders.

Pinned NeoForge 26.1.2.114/Java 25 full build passes: **227 tests passed,
12 existing skips, zero failures/errors**. Resource validation and the focused
seasonal API runner pass (178 model definitions; 245 dynamic references).
The real graphical client now completes resource reload and fatal model
self-tests: TFC block-model atlas rejections fall from 256 to zero. All 413
mixed-atlas item failures per reload also disappear. Namespace-restricted
TFC item discovery and an items-atlas filter keep materials together without
collecting other mods' item textures; three vanilla groundcover sprite aliases
preserve vanilla's native item atlas. Resource-pack priority and namespace tests
pass. Legacy model/reference warnings remain; this is not a zero-warning client.
The resource check no longer creates item stacks before components bind. The retained creative/translation check runs on
native client component binding and has not yet been exercised after world join.

JAR SHA-256: `2bd97d82fab9442f41de022d79ed55bfe166306e23b588e56c5fa50d9057cce3` (68,844,200 bytes).
Exact evidence and limits: `validation/2026-10-17-foliage-terrain-fix.json`.
This host uses Eclipse Temurin 25.0.4.1+1, Gradle 9.2.1, the documented host-only
NeoForm/FML adaptations and Mesa software OpenGL under Xvfb. Dependency pins and
candidate 0.0.0 remain unchanged. The report was on NeoForge 26.1.2.122; this
does not claim that loader combination was tested. Live world appearance,
terrain traversal, dedicated-server data reload, save/restart and survival
remain open. The bounded client is intentionally stopped after resource checks.

**TFC + Terralith requirement retained:** keep TFC geology/ores, climate,
hydrology, ecology and progression; add features TFC lacks through an original
compatibility layer. The separate Terralith 2.6.2 artifact has been audited for
collisions, generator resources and ore palettes. No third-party pack is copied
into TFC, no dependency is added, and no hybrid features are enabled by this
repair. See `TFC_TERRALITH.md` and the audit JSON for feature ownership and runtime
gates. Publish directly on existing `26.1.2` with `[skip ci]`; `26.x` is unchanged.

## 2026-10-17 UTC — Rabbit temptation crash and native asset references repaired

The uploaded integrated-server report reaches a world and then crashes in
`TemptGoal.canUse` because `tfc:rabbit` lacks `minecraft:tempt_range`.
Its supplier now starts with native `Rabbit.createAttributes()` and retains
TFC's health 12, movement speed 0.3 and attack damage 3. A regression reproduces
the exact exception before the fix; registered rabbit, horse, donkey, mule and
ocelot suppliers now pass the native attribute lookup. Other custom animals
use TFC's fixed-range temptation sensor and do not share that failing lookup.

Repaired confirmed asset migration gaps: nine worn/horse armor equipment
assets now generate during `processResources`, with 27 byte-identical aliases
of the original artwork. Existing texture files and resource/gameplay IDs stay
intact. All 36 armor-trim inputs now use native `trims/entity/humanoid` and
`humanoid_leggings` paths, restoring 504 TFC palette variants. Nine metal chain
models use the native iron-chain parent; ice uses the native block model.
Ten wall-fan item models share vanilla's existing floor-fan sprites and sugar
cane uses the current item sprite. Resource generators retain these fixes.

Full pinned Java 25/NeoForge 26.1.2.114 Gradle build succeeds in 72 seconds:
**219 tests passed, 12 skipped, zero failures/errors**. New native equipment
codec tests verify all nine definitions and the exact original bytes of every
armor texture alias; the trim test verifies every native input exists.
`python3 resources validate` passes. A CPU check using the native atlas-source
loader loads 4,052 block, 2,421 item and 1,081 armor-trim sprites without missing
inputs. Reference audit of 14,399 TFC models finds no missing parents or literal
textures across 19,072 texture references. The packaged JAR contains the new
equipment definitions and all 27 original-image aliases.

JAR SHA-256: `8248af60adc8b2b28ce4c49fd7b6b95c957e438b0ebd891fae5388a0a13f94f9` (69,109,378 bytes).
Details: `validation/2026-10-17-rabbit-native-assets-fix.json`.
These checks establish native resource resolution, not GPU appearance or
completed gameplay. The user's broad texture report has no screenshot or
client resource-reload log yet; any additional terrain/rendering issue remains
to be identified from that evidence. Prior world-data/component-binding runtime
limits remain open. Pins, candidate 0.0.0 and the independent `26.x` track stay
unchanged. Publish directly on existing `26.1.2`; no new branch.

## 2026-10-17 UTC — Required world-creation client Mixin repaired

Reproduced the uploaded `CreateWorldScreenMixin.changeDefaultWorldType` crash
using the real FML **CLIENT** transforming classloader. The unqualified
`openFresh` selector chose the overload that does not read `WorldPresets.NORMAL`.
The redirect now specifies the complete three-argument method descriptor.
The required injection and TFC default world preset behavior remain intact;
no `require=0`, hook removal or dummy implementation is used.

The real client transformation check fails with the exact reported exception
before the change and passes after it. A broader pass loads **23 installed
client Mixin target classes**, including the world screen and Patchouli book;
optional Sodium is not tested because it is absent. Full pinned Java 25
Gradle build passes in 39 seconds: **215 tests passed, 12 skipped**, no failures/errors.
The previous server-side JUnit environment did not apply client Mixins; the
new `tools/porting/run_client_mixin_smoke.py` exercises that separate gate.
Run it after building classes and generating `port-diagnostics/classpath.txt`;
use `--all-client-targets` and, on this restricted host, the documented
`--fml-agent` argument. It opens no game window and creates no world.

Rebuilt JAR SHA-256: `ffa0b4fb916eb2c487e602def8a88754c6ea3bab2414273ec936c6e88a0c8e38`.
Detailed results: `validation/2026-10-17-client-world-screen-mixin-fix.json`.
Reported on NeoForge 26.1.2.122; compiled and transformed against pinned
26.1.2.114. Full graphical startup and world/gameplay validation remain open,
including the preceding checkpoint's live component-binding/world-data issues.
Dependency pins, candidate 0.0.0 and branch policy remain unchanged.

## 2026-10-17 UTC — Client colormap reload registration crash fixed

Fixed the client crash reported on NeoForge 26.1.2.122: normal and summer
foliage intentionally read `tfc:textures/colormap/foliage.png`, but were also
registered under that same reload-listener key. Summer now registers as
`tfc:colormap/foliage_summer`. Both texture lookups and both cache consumers
remain unchanged; no asset, save or gameplay ID is migrated.

A native-event regression test reproduces the exact exception before the fix
and passes afterwards. It registers all nine TFC color maps and the star
catalog alongside a vanilla listener and executes NeoForge's listener sort
in two fresh resource managers. Full pinned Java 25/NeoForge 26.1.2.114 Gradle
build succeeds in 37 seconds: **215 tests passed, 12 skipped, no failures/errors**.
The rebuilt JAR contains the new key; SHA-256 `4704bb8ffeaab4022a02bd42397bdc773e6a338a7b9335d30c33ed4041bcfae7`.
Exact results are in `validation/2026-10-17-client-colormap-reload-fix.json`.

This fixes the reported registration failure. Full graphical client startup
and world loading remain unverified; the preceding checkpoint's live
component-binding/world-reload issues remain open. Dependencies and candidate
version 0.0.0 are unchanged. No new branch is created.

## 2026-10-17 UTC — Native transfer, complete build and first server reload

Continued directly on `26.1.2` from `7b5715acd23da8b97ed012c1b66d666e50c0de96`. **Main compilation: 44 → 0 errors**, 1,699 diagnostic sources, Java 25 exit 0 without timeout. Main/data/test compilation, packaging, all three license checks, pinned dependency verification and the full Gradle build pass. JUnit reports **226 tests: 214 passed, 12 existing skips, zero failures/errors**. All **36 API runners** pass. Candidate version `0.0.0` and all pinned dependencies remain unchanged. **The packaged JAR is not playable or a release.** Normal dedicated-server startup reaches world-data reload and overworld creation, but live component binding still invalidates recipe decoding and fatal self-tests stop the server. A successful Gradle `runServer` exit is not startup success.

Native item/fluid capabilities now use NeoForge resource handlers and transactions. The adapters retain TFC's owning inventories, tank/alloy/mold/heat/food components, sided rules, chest halves, limits and manual container behavior. Shared snapshot journals restore original stack identity, counts and component patches on abort, including child commits followed by parent rollback. Notifications run after root commit. Seven native container/storage tests pass, including bucket nesting, mold temperature/drain locks, vessel inventory/alloy/food rollback and coupled item/tank changes. The separate production-journal probe passes 24,580 assertions over 2,048 scenarios; it does not replace world automation/duplication tests.

Advanced crafting uses `NormalCraftingRecipe` with native matching, placement and displays instead of an invalid AIR output template. Shaped mirroring/primary-input translation, custom shapeless matching, provider outputs, remainders and registered serializers remain. Crafting context clears on exceptions. JEI receives shaped and shapeless previews with actual crafting inputs and matching grid/focus positions. Native fixed-output previews remain available to the guide. Empty-slot ingredients have an explicit registered codec. Legacy TFC ingredient/count/tag/custom schemas are retained while native vanilla recipes use current syntax. All **6,243 active generated recipes decode through native registries**, and **212 disabled vanilla recipes** decode as valid never-conditions, without dummy recipes. The fixture binds components before parsing; this does not establish the live reload ordering.

The real data loader completes every provider and writes **8,500 managed resources**; a subsequent run writes zero files and removes none. Required biome references also check the authored existing pack, replacing the removed existing-file helper without optionalizing entries or inventing biome objects. Native vanilla stew successors are disabled explicitly while retaining the legacy disabled ID. Old snow/bamboo/dyeable/tall-flower IDs remain. Relative to the parent, 2,680 generated JSON files change and 21 are added; none are deleted. Of the changed files, 2,679 match after documented native syntax/default/condition normalization. The remaining guide-config file reflects the current temperature text formatting. Native nonliving entity types explicitly retain no loot table, matching their existing manual drop behavior.

All **539 tree templates** now use native world version 4790. 478 change only that header; 61 also replace 126 obsolete, previously ignored `natural` properties with their existing decoded `branch_direction=none`. Geometry, positions, axes and all other NBT values remain unchanged. Native palette/property and load/save tests pass for every template. Of these, 472 templates are generated and 67 retained authored templates; six of the authored templates require only the header change.

Real FML loading also exposed obsolete Mixin signatures and registration timing. Required hooks now target the actual native entity/food/snow/heightmap/biome/world-preset/tag/JSON/chunk-removal/creative-mining methods. Block/item properties receive their real registry IDs before native constructors consume them. Component refresh and Patchouli multiblocks wait for native component binding. Client selection and ghost rendering live in the client helper; common block interfaces retain only common state/geometry types. The precipitation hook is in the client Mixin list because `OnlyIn` no longer strips common members. The original wolf howl event identity is restored, but the original audio is absent from the target assets and audible behavior remains unresolved.

**Runtime limits:** the dedicated development server gets beyond registration and Mixin application into actual data reload. It logs **2,937 shared JSON codec errors**: 2,935 involve item/fluid components not yet bound, and two reference invalid advancement item IDs; advancement errors and 30 missing block loot tables also remain. Fatal self-tests stay enabled and stop overworld loading. Client launch is blocked by `glfwInit failed` on this headless host; the bounded attempt is terminated, not counted as passing. No completed world generation, gameplay, save/restart, multiplayer, GPU appearance or survival validation. Next: migrate live resource outputs to the native template/component lifecycle, address the remaining recipe/advancement/loot errors, then repeat startup/world tests. Do not bypass errors or turn off self-tests.

Validation used the exact staged game/build inputs in a snapshot outside the host's synchronized workspace, avoiding transient resource-hash races without disabling Gradle tracking. Official OpenJDK 25+36 and Gradle 9.2.1 were used with the documented opt-in NeoForm Java-command fallback, trusted proxy CA and an opt-in agent containing FML's exact unmodified bundled `DevAgent` class. See `validation/2026-10-17-native-transfer-build.json` for counts, hashes, commands and limits. Source publication uses the existing branch and `[skip ci]` while Actions quota is suspended. The independent `26.x` worktree is unchanged.

## 2026-10-17 UTC — Uploaded build report resolved; full build succeeds

The uploaded Windows log contains 44 main compiler errors from published
checkpoint `7b5715acd23da8b97ed012c1b66d666e50c0de96`. All 44 are resolved in
this source: 39 retired item/fluid capability references, two block removal
callbacks, recipe result access, native item component defaults and the wolf
howl sound. Native transactions retain TFC inventory, fluid, temperature,
container replacement and rollback behavior.

Verification of the repaired tree: diagnostic javac compiles 1,699 sources
with zero errors (exit 0, no timeout); pinned full Gradle build succeeds
(47 seconds); 226 JUnit tests are recorded, **214 passed and 12 skipped**,
with zero failures/errors. All 36 API runners pass. Native data generation
also succeeds (29 seconds). The regenerated recipe corpus uses NeoForge's
registered `neoforge:never` condition to keep vanilla recipes disabled.
Generated-resource audit records 2,679 syntax-equivalent changes, one guide
config formatting difference and the required new vanilla override/tag files.
The per-error audit is in
`docs/foundations/validation/2026-10-17-uploaded-build-error-audit.json`.

An actual `TFC-Foundations-26.1.2-0.0.0.jar` is produced (69,055,991 bytes;
SHA-256 `c0d05d2fc648b6bd2155a205658582a7e20893f2c4e8a8e6ecdd5ae8858d940d`).
It is an unverified development build, **not a playable release**. Client and
dedicated-server startup, actual GPU/resource reload, world generation,
save/reload, multiplayer and survival progression remain unverified. A native
runtime launch attempt stopped during loader/display initialization; it did
not establish startup or gameplay success. Dependencies and candidate 0.0.0
remain pinned. No new branch is created; the independent 26.x track is unchanged.

## 2026-10-17 UTC — Native item models, predicates and color layers

Continued directly on `26.1.2` from `d6ed9c94556e8ce15d41d27aee965cdab3a916f3`, whose published checkpoint records 93 main compiler errors. Current pinned Java 25 full Gradle and diagnostic javac report **44 main errors**, diagnostic source count **1,680**, exit 1 with no timeout. This is a diagnostic count, not a completion percentage. Main compilation still blocks data/test compilation and packaging; **no playable JAR**. Dependency pins, candidate `0.0.0`, gameplay/resource IDs, branch policy and upstream licenses remain unchanged.

Replaced removed item property registrations with native numeric codecs for TFC rod casting, player shield blocking, active/aggressive javelin throwing, the handstone's strict last-ten-durability threshold, sealed contents/nonempty barrels and clamped blowpipe heat. The generator converts the inspected separate-transform and ascending single-property override models into native context/range selectors. It retains the original model references, threshold equality and fallback paths, all nine ordinary display contexts, held/throwing javelin geometry/materials, normal/trapped wood chest textures and filled-pan stage selection. Unknown loaders/properties and author-maintained selectors remain protected. The obsolete BEWLR extension/chest classes are removed after replacing their behavior with native model selection and the native chest renderer.

Native item definitions now cover **5,317 static + 66 fixed-fluid + 60 TFC fluid/trim + 170 special/predicate** models, **5,613 of 5,614** legacy item-model files. These are model-file counts, including helpers, not registered-item counts. The one explicit remaining migration is the authored `grass_inv` tint helper. The pan model owns its stage cache per resource reload and emits native baked quad layers during extraction. Javelins use TFC's actual javelin model and native submission, foil and extent handling. No live world/entity is captured for deferred drawing.

Block tint registration now supplies both authored face indices as native sources, including default/world/terrain-particle paths and the snowy state property for native color caching. Original grass, tall grass, seasonal/conifer foliage, krummholz snow suppression, fallen leaves, crop and water callbacks remain. Legacy item vegetation colors enter native item-state layers only when no authored native tint is present; their two index values and model identity are retained, with the existing rotten-food fallback after registered colors. The dromedary adapter transfers captured camel movement, head, jump cooldown and all five animation timelines to the native model while retaining the existing simple renderer/baby scale. Both minecart registrations use the current non-generic renderer.

Validation: **14,544 original/native selector decision comparisons** pass, including boundary values and nine display contexts; exact special textures and generated-file determinism are checked. **90 native tint-layer/default/world/particle/identity/reuse assertions** and 10 hook descriptors/anchors pass; all authored TFC face tint indices are confirmed to be 0 or 1. The livestock suite passes **32 additional native/TFC camel pose/reuse/CPU geometry scenarios**, with 3,285 aggregate assertions and 112,896 finite CPU vertices. Resource/generated-texture validation, all three license checks, dependency verification and selector generation pass; assets are up-to-date in the full build. The final aggregate passes **all 35 API runners (exit 0)**; results, source/JAR hashes and limits are recorded in `validation/2026-10-17-item-model-api.json` and the 44 remaining diagnostics in `validation/2026-10-17-item-model-errors.json`.

The first aggregate run encountered the new tint fixture's use of `BlockStateProperties.SNOWY`, which initializes unrelated sound registries without a game bootstrap. The corrected CPU fixture uses a real native BooleanProperty and separately checks the production snowy-property bindings; no native class is stubbed. The final aggregate is rerun after the fixture correction. Host-only NeoForm Java-command fallback and trusted proxy CA remain in use as documented in the preceding checkpoint.

Remaining main errors are in transfer capability registration/lookups, component defaults, food recipe outputs, the wolf howl sound and two block-removal callbacks. NOT RUN: game/model reload bootstrap, transformed Mixins, actual GPU drawing, tint/foil/chest/pan appearance, client/server launch, world generation, save/reload, multiplayer or survival. The independent `26.x` track is unchanged by this target-specific checkpoint.


## 2026-10-17 UTC — Native environment rendering and JEI callback checkpoint

**Final verification:** pinned full Gradle and diagnostic javac both report 93 main errors; all **35 API runners pass (aggregate exit 0)**, including the 15,611 new sky CPU assertions. Resource validation/generated textures, all three license checks and dependency verification pass. Asset retry succeeds; the final full build reuses those assets and fails only main compilation. Data/test compilation and JAR packaging remain blocked. Exact remaining diagnostics, runner exits, source/JAR hashes and test limits are recorded in `docs/foundations/validation/2026-10-17-environment-api.json` and `2026-10-17-environment-errors.json`.

Continued directly on `26.1.2` from `7017226aeef4a63d447729a8b9e8c30666b480d4`. The exact parent reproduces **203 main errors** in pinned Java 25 Gradle and diagnostic javac. Final source reports **93 main errors** (1,678 sources; diagnostic exit 1, no timeout). Error counts include cascading diagnostics and are not a playability percentage. Dependency pins, candidate `0.0.0` and all gameplay/resource IDs remain unchanged.

Sky/weather now use NeoForge's native custom-environment interfaces and render-state extraction. Rain/snow meshes freeze vertex/UV/color/light values before GPU submission; drawing does not retain a level or camera. The registered native pipelines retain colored catalog stars and opaque celestial covers. Catalog reload publishes immutable CPU geometry; native star buffers are replaced and closed on the render thread, including empty-catalog reloads. Sun/moon use the current celestial atlas and all eight moon phase sprites, retaining TFC's independent solar/lunar transforms and original sizes. Rain intensity, wind, temperature, textures, particle and sound calculations remain; the two full precipitation vertex bodies and the original star/rotation bodies have frozen preservation contracts.

Climate colormaps enter the native attribute system after biome layers and before timeline/weather modifiers. The custom sky/weather identifiers replace the old overworld-effects registration. Obsolete `getDayTime`/`dayTime` hooks move to the inspected native clock callback, with a guard against client fallback recursion. Precipitation wrapping targets the native method and sea-level argument; the world-selection warning and WorldGenContext callbacks retain their current parameters, including the unsaved-chunk listener. The obsolete LevelAccessor mixin is removed after rehoming its clock behavior.

JEI uses synchronized recipe-map enumeration, native ingredient streams, crafting-station roles, the current recipe-type interface and client packet distributor. The trim command reads the native material component and resolves patterns from data-driven smithing-trim recipes. Vivecraft's side check uses the current loader method. An unused Helpers import left by upstream integration was removed from TFCBlockEntity so its isolated save/particle probe can compile.

The new production-geometry suite passes **15,611 CPU assertions**, including native buffer layouts, finite immutable star geometry and transform checks. Native callback/bytecode anchors, attribute-layer order and frozen complete rain/snow vertex bodies pass. These are not rendered frames or transformed Mixin tests. The suite is registered with the existing API runners, which now write individual exit codes to a machine-readable summary.

Host setup: official OpenJDK 25 build 25+36, Gradle 9.2.1 with verified distribution hash, unchanged pinned target libraries. The existing opt-in local NeoForm 2.0.31 Java-command fallback and this host's trusted proxy CA are required. Initial native-buffer tests lacked host native JARs; the final runner loads the real matching resolved LWJGL natives. The initial geometry radius tolerance assumed exact trigonometry; the final tolerance accounts for Minecraft's native approximation. The first aggregate API pass found the unused import above. The first full build also failed the separate Java 21 asset task's local certificate check; the local trust store was corrected and that task retried without weakening TLS or build gates.

**No playable JAR.** Main compilation still blocks data/test compilation and packaging. Remaining clusters include native item predicates/special renderers, block/item tint registration, inventory/fluid capabilities, component defaults, recipe output handling and remaining removal callbacks. NOT RUN: live GPU pipelines, visual/color and native timeline timing, transformed Mixins, resource reload in a running world, startup, world/save/reload, multiplayer, survival or performance measurements. The separate `26.x` upstream integration is published as `64bc0554bf820464cbefb57c551b3007b6298a1f`; its full compilation remains unverified and Patchouli unresolved.


## 2026-10-16 UTC — Upstream integration through c3b4d953

Ported the complete 42-commit `TerraFirmaCraft/TerraFirmaCraft` 1.21.x delta from initial base `e9d9a88a` through `c3b4d9536b82e5a9746dd793c586720a74108057` onto **both existing branches**, preserving upstream EUPL notices and authors. Ten commits became reachable since the October 12 observation. This is a source port with explicit per-file audit, not a Git ancestry merge. `validation/2026-10-16-upstream-integration.json` records all 348 diff paths, SHA/author/subject provenance and native equivalents. Upstream AGENTS.md/.gitignore do not replace our project controls. No dependency pin, version candidate or public branch changed.

Included: buoy/lobster-trap content, all 16 colors and their recipes/textures/loot/tags/guide pages; kiln catch-up heating and stale-device guards; banana and berry corrections; squid spawn cancellation; scraping progress/durability and texture safety; default soup ordering; rope-anchor orientation; fluid-loggable tool racks and offhand interactions; pet home/sitting AI; ten vanilla armor trim palettes, thatch spawn prevention, soda ash from beach seaweed and translations. The newly observed 1.20.x tip `e39a6531` adds five reachable commits affecting scraping/farmland; their behavior is covered by the newer 1.21.x changes. Legacy branch-specific architecture is not blindly merged.

Both branches pass resource/generated-texture validation and the new upstream regression runner: **2,097,152 production scraping-mask CPU checks**, source guards/fallback ordering, all JSON parsing, buoy resources, thatch tag and trim palettes. These are not world/AI/rendering tests.

On **26.1.2**, continued from `f49876a1`: main diagnostics **220 → 203**, independently confirmed by full pinned Gradle and diagnostic javac (1,677 sources, exit 1, no timeout). New fishing renderers use native extraction/captured geometry/items; new block entities use ValueInput/ValueOutput and native removal. Rope lighting/bounds, buoy wind animation and catch placements remain. Existing reload-local mold and null-safe scraping snapshot implementations provide the upstream equivalents. Also migrated placed-item/shelf/pit-kiln rendering to the native snapshot framework and supplied JEI's required native ingredient displays. Custom placed models retain cutout/translucent sheets, original slot transforms/rotations and pit-kiln stage cutoff; native usesBlockLight identifies ordinary three-dimensional item placement, with the existing shield/javelin/trident exceptions. Actual appearance still needs client verification.

All **34 registered API runners pass individually** after updating four upstream-sensitive source/fixture contracts (new item count, tool-rack hand handling, accepted interaction body and boolean scraping callback). The aggregate rerun had one stale scraping-signature failure; that runner subsequently passed after correction; no production change was needed. Existing 9,074 display/surface and 584 item-layer CPU assertions remain passing. Native selectors now cover 5,386 static + 66 fixed-fluid + 60 TFC fluid/trim definitions; 102 special models remain pending. All three license checks, dependency verification and resource/asset preparation pass in the pinned full Gradle attempt. Host NeoForm Java-path fallback and local proxy trust remain in use.

**No playable JAR.** Main compilation blocks data/test compilation and packaging. Remaining largest clusters include sky/weather (82), client registration (29), pan special rendering (9), other special item renderers and transfer capabilities. No transformed Mixin, GPU, resource reload, client/server launch, world/save/reload, multiplayer, survival, trap movement/drops, pet AI or kiln loaded/unloaded runtime verification. Exact main errors are in `validation/2026-10-16-upstream-errors.json`. `26.x` independently remains an incomplete 26.3 port.

## 2026-10-16 UTC — Native client tint, particle and held-item hooks

Continued on existing `26.1.2` from `7a63ac8a86b13ff19c0c52c7d3f8f61b5b5ad8f2` (the 261-error native item-layer checkpoint). That exact parent independently reproduces **261 diagnostic errors**. Final source, checked from an isolated archive of the staged tree, reports **220 main errors** in both Java 25 diagnostic javac and pinned Gradle `compileJava` (1,669 sources, diagnostic exit 1, no timeout). Across both continuations from `f994598f`, main errors fall **359 → 261 → 220**. These are displayed diagnostics, not independent bugs or a completion percentage.

Changes:
- Rotten-food coloring enters native cuboid item layers only when no explicit tint source exists. The tint is included in model identity, retaining correct cache invalidation. A shared helper preserves explicit colors and handles reused/empty states. Item-breaking particles resolve those same native tint layers through the current factory callback; they retain their RGB coloration. Replaced the removed ItemColors lookup and obsolete Sodium-only color-provider hook with this common native state path. Optional Sodium runtime compatibility has not been tested.
- Fishing bait is extracted into a reusable native item render state, cleared before each update, and submitted with its original offset, half scale and camera-facing rotation. The native bobber and line renderer still runs. Submission retains no hook, inventory or level in the bait state.
- Rain-drop collision wrapping targets the current single-quad particle callback and keeps the `IBlockRain` full-block collision substitution. Fluid mixing face/height hooks target native FluidRenderer. Skeleton javelins set the correct arm's native THROW_TRIDENT pose and bypass only the skeleton melee override, preserving humanoid animation and the throwing posture.
- First-person pan drawing uses the native submission collector and avatar hand renderer, retaining the original sifting/tilt/arm formulas and using native skin/sleeve parameters. Third-person glass blowing captures a HEAD item state during avatar extraction, then applies the existing head clamp and ±2.3/16, -0.1625 offsets during submission. Native root transforms are included. Shift state uses the native Minecraft input query; the legacy fancy-graphics helper follows the native cutout-leaves option.

**Passed:** all **33 API runners**, including **78 new CPU tint-layer assertions**, **10 native method/bytecode-anchor contracts**, actual accessor-field descriptors and all registered Mixin source paths. Existing 584 item-layer and 9,074 display assertions still pass. Resource/generated-texture validation, all three license checks, pinned-dependency verification, asset gate and diff checks pass. Intermediate compilations reported 227/225 errors; final source removes the obsolete Sodium class after moving its behavior to the native path. Final javac and full Gradle validation use the archived intended tree, including that deletion.

**Not playable:** full `compileJava compileDataJava compileTestJava build --continue` exits 1 at main compilation; data/test compilation and packaging remain blocked. No transformed Mixin startup, actual particle/food tint, hand/bait rendering, graphics/input, model/atlas reload, GPU, optional-Sodium, client/server/world or survival test passed. Tests use real native item states and target bytecode but do not apply Mixins or bootstrap the game. Remaining largest clusters are sky/weather (82), client registration (32), placed-item rendering (13), pan item rendering (9), other special items and transfer capabilities. The native first-person pan hook does not complete the separate PanItemRenderer migration. Exact evidence: `docs/foundations/validation/2026-10-16-client-hooks-api.json` and `2026-10-16-client-hooks-errors.json`. Existing untracked NeoForm host Java-path fallback and trusted local proxy CA remain in use. Dependency pins and candidate `0.0.0` are unchanged; no new branches/Actions runs; `26.x` unchanged.

## 2026-10-16 UTC — Native fluid-container and armor-trim item layers

Continued directly on `26.1.2` from `f994598fcdb3551e64da826c5bbf66ad14cd84ec`. An isolated archive of that exact source reproduces **359 diagnostic errors**. Current diagnostic javac and pinned Gradle `compileJava` both report **261 main errors** (98 fewer displayed diagnostics). Final diagnostic traversal: 1,667 sources, exit 1, no timeout, annotation processing disabled. This is not a count of independent bugs or a completion percentage.

`ContainedFluidModel` and `TrimmedItemModel` now implement native `ItemModel` with registered codecs, native cuboid/composite layers and per-reload caches. Original texture IDs, parent display transforms, fluid XY/depth offsets, cover/trim depth offsets, optional overlays, translucent passes and luminous-fluid emission remain. Fluid selection/tint uses native content inspection rather than a simulated drain, keeping solid mold contents inspectable. The new quad remapper also retextures mask edge faces and remaps atlas UV coordinates, preventing mask-colored edges when the mask and fluid use different sprites/atlases. Trim material suffixes use the native material asset group, and trim selection no longer depends on a non-null client world. Vanilla leather selectors tint only the armor base, leaving the overlay and trim neutral.

The selector generator now emits **5,369 static + 66 fixed-fluid + 60 TFC fluid/trim definitions**; its unsupported-model list falls **162 → 102**. All 24 TFC variable-fluid and 36 TFC trim definitions use their native type, retaining IDs, parent and texture slots. Eight authored vanilla armor override selectors use the same trim codec, with leather dye sources. Unknown fields, texture references, predicates and unrelated loaders remain explicitly pending rather than being flattened. Legacy assets are unchanged. Removed fluid-only legacy item-color registration because the native fluid layers and NeoForge fixed-fluid model now own their tint; grass/foliage color registration remains an open compiler cluster.

**Passed:** all **32 standalone API runners**, including the new **584 CPU item-layer assertions**; all three license checks, pinned-dependency verification, asset gate, resource validation and generated-texture comparison; `git diff --check`. The new runner compiles the three actual production classes against the pinned target. It parses all **24 fluid and 44 trim schemas**, checks native material suffixes, and exercises native images/sprites/quads for all six face directions, cross-atlas UV remapping, tint indices, alpha-pass flags, emission, shade/AO and non-mutation. The pure quad test supplies null render types; actual pipeline selection is compiled, not initialized. The previous 9,074 display assertions still pass. No game class is replaced by a stub.

**Still blocked:** full `compileJava compileDataJava compileTestJava build --continue` exits 1 at main compilation. No data/test compilation, JAR packaging, model/atlas reload, actual item resolution, Mixin startup, GPU rendering, fluid capability interoperability, dye/trim appearance or client/server/world/survival validation passed. Remaining largest clusters: sky/weather 82, client registration 32, placed-item renderer 13, pan renderer 9 and transfer capabilities. Exact reports: `docs/foundations/validation/2026-10-16-item-layers-api.json` and `2026-10-16-item-layers-errors.json`. Existing untracked NeoForm host Java-path fallback and trusted proxy CA remain in use. Dependency pins and candidate `0.0.0` are unchanged; no new branches or Actions runs; `26.x` unchanged.

## 2026-10-16 UTC — Machinery, firepit and sign render-state migration

Continued directly on `26.1.2` from `d50d6fa5d98d714374175eaad6fd86e9834f2152`. Restored the cleared toolchain and independently reproduced that exact source at **491 diagnostic errors**. Current Java 25 diagnostic javac and pinned Gradle `compileJava` both report **359 main errors** (132 fewer displayed diagnostics). Diagnostic traversal: 1,666 sources, exit 1, no timeout, annotation processing disabled. Counts include cascading errors and are not independent bugs or a completion percentage.

Changes:
- Firepit logs, stove/pot/grill contents, crankshaft wheel/rod/pump, loom/power loom, quern, trip hammer, bellows, hot poured glass, sluice, pouring channels and mold-table displays now extract native render states. Their existing mesh coordinates, animation/fill formulas, item transforms and visibility conditions remain. The shared CPU vertex collector freezes position, normal, atlas UV, ARGB, light and overlay values before submission; submitted mesh callbacks do not retain a level, block entity or mutable collector.
- Standalone models use native identity keys and the registration/dependency API, preserving resource IDs. Keys survive reloads, while baked models remain owned by the native model manager. Firepit/crankshaft model extraction uses native ambient lighting, tint and neighbor culling. The guide-level adapter delegates block/fluid/entity and light lookups to the preview level; it does not substitute the live world's block data.
- Chest wood/trapped/double textures use the native per-state sprite hook. Bells use the native animated model. Standing/wall signs use native models for the already registered TFC wood types. Hanging signs retain metal/wood world and editor texture paths, native attachment transforms and both text faces; a registered invoker targets the native private text submission method. Removed the obsolete SignRenderer accessor. Runtime self-tests use current loot/model/sound and ingredient APIs without disabling their checks.

**Validation:** all **31 API probe runners pass**. The expanded display suite performs **9,074 CPU assertions**, including all three axle axes over 24 angles, captured/replayed vertex equivalence, immutable/reused snapshots, standalone model identity/dependencies and native sign bridge/editor signatures. This does not test actual model baking, atlas reload, Mixin application, GPU submission, sign text appearance or block-entity gameplay. Resource validation passes with zero language/model-parent/texture/blockstate/unused-model errors and no generated-texture differences. All three license checks, pinned-dependency verification and asset download pass; Mixin JSON/source presence and `git diff --check` pass.

Full `compileJava compileDataJava compileTestJava build --continue` exits 1 at main compilation; data/test compilation and packaging remain blocked. **No playable JAR or client/server/world/survival test passed.** Remaining errors are largest in sky/weather (82), contained-fluid models (52), trimmed-item models (43), client registration (35), placed-item rendering and native transfer capabilities. Exact evidence: `docs/foundations/validation/2026-10-16-machinery-sign-api.json` and `2026-10-16-machinery-sign-errors.json`. This host still uses the opt-in, untracked NeoForm Runtime 2.0.31 Java-path fallback and its trusted proxy CA in the local Java 25/21 stores; no TLS validation was disabled. Dependency pins and candidate `0.0.0` remain unchanged. No new branches or Actions runs; `26.x` is unchanged.

## 2026-10-16 UTC — Native fill-surface render states

Continued directly on `26.1.2` from `a53d28c949e2445334c26e4744bf9f06dd4e7d36`, the published 507-error checkpoint. Native render-state extraction now covers barrel contents, crucible alloy, bowl powder, powder-keg contents and the glass-basin fill animation. Their original sealed/facing visibility conditions, fill heights, barrel low-fill inset, amount/capacity math and barrel item transforms remain. Fluid sprites/tints are resolved during extraction; immutable surface records carry geometry/UV/color/light data to submission without retaining a block entity, inventory, level or FluidStack. Solid white texture faces use explicit opaque alpha; fluid alpha remains intact.

Both diagnostic javac and Gradle `compileJava` now report **491 main errors**, down from 507 and the restored 650 baseline. Diagnostic pass: 1,663 sources, exit 1, no timeout, annotation processing disabled. Full pinned `compileJava compileDataJava compileTestJava build --continue` exits 1 at main compilation; downstream compilation/packaging remain blocked. License checks for main/data/test, dependency verification and the restored asset-download gate pass. The previous resource validation passed with zero errors and no resources changed in this continuation.

All **31 API probe runners pass again**. The display suite now executes **138 CPU checks**, covering the native item snapshot list plus surface winding, transforms, atlas UV coordinates, normals, ARGB alpha, packed light and overlay across six fill levels. It uses native PoseStack/VertexConsumer APIs and a recording ItemStackRenderState subclass fixture. It does not resolve actual item models, bake/reload an atlas, submit on a GPU or construct game block entities. Source/API success is not visual or gameplay success.

Exact results: `validation/2026-10-16-fill-surfaces-api.json` and `2026-10-16-fill-surfaces-errors.json`. Existing local NeoForm Java-path fallback remains in use; the Java 21 asset trust issue was corrected and all 4,750 asset files downloaded successfully. No dependencies, gameplay exclusions or branches changed; `26.x` is unchanged. **No playable JAR, Mixin startup, client/server/world or survival test passed.** Remaining errors are largest in sky/weather, native item models, client registration and transfer capabilities.

## 2026-10-16 UTC — Verified display, foliage and entity API repairs

Continued directly on `26.1.2` from `e6f527060b68f676bf736a7a34ff029e6f6819ca`. Rebuilt the cleared host toolchain and dependencies. An isolated archive of that exact source reproduces **650 diagnostic errors**. Current source reports **507 errors** in both Java 25 diagnostic javac and Gradle `compileJava`; main compilation still fails. Diagnostic traversal covers 1,662 sources, exit 1, no timeout. Dependency pins and candidate `0.0.0` are unchanged. No new branches; `26.x` remains separate.

Changes:
- Anvil, charcoal-forge, nest-box and tool-rack displays extract native item render states and immutable local transforms. Original placement, rotation, slot selection and heated-item light values are retained. Submission reads no live inventory or level.
- Removed obsolete render-layer registrations: 26.1.2 classifies baked quads by texture transparency. Fluids continue through the existing native fluid-model registry. Replaced the removed `ItemBlockRenderTypes` mixin with `ModelBlockRenderer.forceOpaque`, retaining fast/fancy opacity for TFC leaf interfaces, fallen leaves and krummholz in chunks and moving blocks.
- Migrated egg entity serialization/hatching and falling-block block-entity restoration to registry-aware ValueIO. Camel ownership uses entity references, native eating-sound visibility is widened, feeding sounds use consumable components, dolphin boat following uses the native replacement goal, avoidance uses the native server entity lookup and tame-fight callbacks accept the server context. Manatee flops and the existing movement-sync reset use `needsSync`.
- Ram shield knockback uses the native blocking result, retaining TFC's damage and force multipliers. Minecart display-state rendering uses the retained native override without the removed flag. Grass light attenuation/postprocess, coral deferred loot-table selection, projectile interaction context, configured-feature lookup, bonus-chest options, crate text extraction and the pan loot-list import use target APIs.

Validation: **all 31 `inspect_api.py` probe runners pass**, including the new **12 CPU item-display snapshot/pose/light/reuse checks**. Resource validation reports zero errors; all three license tasks and pinned-dependency verification pass. `git diff --check` passes. Exact summaries and remaining diagnostics are in `validation/2026-10-16-display-entity-api.json` and `2026-10-16-display-entity-errors.json`. The existing opt-in local NeoForm Java-path fallback was needed in this container; this is not an unmodified-toolchain release test. The first full-build attempt also failed Java 21 asset HTTPS trust; its local trust store was corrected for a retry.

**Not playable:** full build remains blocked by the 507 main errors; data/test compilation and packaging cannot complete. No JAR, actual Mixin application, client/server launch, model/atlas reload, egg hatch, entity save/reload, grass spread, coral drop, shield-ram gameplay, multiplayer or survival test passed. Display tests use native classes with a recording item-state fixture, not actual model resolution/GPU rendering. Remaining failures center on native capability/transaction migration and client/item/world rendering.

## 2026-10-13 UTC — Explosion, fluid and block callback repair checkpoint

Continued directly on `26.1.2` from `9caa884b6df3b538d1b983d321ac49828048a6ed`. No branches were created; the live refs remain `26.1.2` and `26.x`, and `26.x` source was not changed. Dependency pins and candidate version `0.0.0` are unchanged.

Both pinned Gradle `compileJava` and same-mode diagnostic javac reproduce **713 → 650 errors** (63 fewer displayed diagnostics). The final diagnostic pass covers 1,661 sources, exits 1, and does not time out. The full `compileJava compileDataJava compileTestJava build --continue` attempt **fails main compilation with 650 errors**; data/test compilation and packaging remain blocked. All three license tasks, pinned-dependency verification and asset download pass. Java 25.0.4.1 and Gradle 9.2.1 were used with the existing opt-in, untracked NeoForm 2.0.31 host Java-path fallback. The separate asset-download Java 21 trust-store issue was corrected using this host's trusted proxy CA; no TLS checks were disabled. This is not stock-toolchain or release validation.

Changes:
- Powder-keg explosions extend native `ServerExplosion`. Two registered private-method invokers retain native blast-ray calculation, entity damage, knockback and the NeoForge detonation hook while keeping TFC's separate filtered block/drop phase. Natural-only/cannot-break tags, igniter loot context, drop merging and the 64-block player notification radius are retained. Both block and minecart callers require `ServerLevel`; chain-reaction callbacks and explosion packets use the current API.
- Mixing fluids use native server tick/spread/get-new-liquid signatures. The fluid mixin handler matches them. Wall passability uses a static invoker, and water-hole/pass-through invokers match the current native signatures. Existing mixing calculations, source thresholds, tick scheduling and molten-fluid lava random ticks remain.
- Block collision, comparator, bucket pickup/fluid placement, harvesting, fall-distance and cached light callbacks use the inspected target signatures. Banana/fruit-leaf subclasses were updated alongside their parents so their specialized collision behavior still overrides the native callback. Thatch retains full light attenuation (15); leaf attenuation remains 1.
- Native `Block` no longer receives text tooltip callbacks. `TooltipBlockItem` now delegates to `TooltipBlock` through the native item callback, retaining sealed-device inventory summaries and barrel/crucible item counts, fluid text and image-tooltip settings.

**Validation:** `inspect_api.py` passes all 30 standalone probe runners and its diagnostic-report tests. The new runner compiles the two real accessor interfaces and verifies **19 private bridge/native callback contracts**, including staticness. Existing checks include 49 real NBT/ValueIO checks, 51 common utility/API checks, 16 flat-ingredient codec checks and CPU model/asset suites. `python resources validate` passes with zero language/model-parent/texture/blockstate/unused-model errors, including generated-texture comparison. Mixin JSON and registered source presence validate; `git diff --check` passes. Exact summary and remaining diagnostics are in `docs/foundations/validation/2026-10-13-fluid-block-api.json` and `2026-10-13-fluid-block-errors.json`.

**Still open:** 650 main compiler errors, principally client registration, block/item/world rendering and resource-handler capability migration. No JAR, transformed-mixin startup, client/server launch, world save/reload, powder-keg/fluid/comparator regression or survival test passed. API/source/CPU checks are not live gameplay validation; 26.1.2 is not yet playable.

## 2026-10-13 UTC — Branch cleanup and verified common API repairs

Continued the existing `26.1.2` branch at `558427da0a32ee416d6f0e5d26e127b567bd288f`. Sixteen historical `port/` refs were removed after source comparison and history consolidation (`67c57fe3`). GitHub now has only `26.1.2` and `26.x`; all deleted tips remain reachable from `26.1.2`. Both targets have an `AGENTS.md` rule prohibiting task/repair/validation/transport branches. The 26.1.2 workflow no longer names deleted branches.

Restored JDK 25.0.4.1, Gradle 9.2.1 and the exact 84-file compile classpath with unchanged Minecraft 26.1.2 / NeoForge 26.1.2.114 / JEI 29.43.0.107 / Patchouli 26.1-94 pins. Both Gradle `compileJava` and diagnostic javac reproduce **759 errors** before source changes and **713 errors** afterward (46 fewer displayed diagnostics, not 46 independent bugs). Final diagnostic traversal: 1,660 sources, exit 1, no timeout, annotation processing disabled. Gradle compilation separately fails with the same 713 errors. Main, test and data license tasks pass. The opt-in, untracked NeoForm 2.0.31 host Java-path workaround was required; this is not stock-toolchain release validation.

Source changes:
- Seven tooltip hooks use the native display/consumer API. Contents, capacities and molten/solid text are retained; temporary lists are allocated only for legacy inventory-summary helpers.
- Block picking uses the current native callback, including inventory snapshots and sealed-device contents. Placed-item and pit-kiln selection reconstruct their hit from the player's authoritative raycast. Typed block-entity data and collected components replace the removed native item-save helper.
- Fox variants use vanilla's component/synchronization/save path; frog variants use the dynamic registry and component hook. Climate thresholds remain unchanged. Squid movement writes the native movement vector through a registered accessor; horse eating sounds keep their existing sound providers.
- Calendar storage uses a `SavedDataType` and codec while preserving the `calendar` compound, always-dirty behavior, and legacy identifier path `tfc_calendar` under the native default namespace. The native engine now resolves namespaced saved-data paths; conversion of older worlds is not verified.
- Pot and mold-table recipes use the existing synchronized recipe-map helpers. Crafting remainders use nullable native templates, preserving filled-container and barrel behavior. Knapping retains the legacy flat ingredient schema. Optional item-stack NBT uses the current registry-aware codec, preserving list positions and empty defaults.
- Reload callbacks and the ChunkMap constructor mixin match the inspected target signatures. Milk's config-controlled replacement hooks native `Item.finishUsingItem` only for `minecraft:milk_bucket`, preserving other items' consumption behavior.

Focused existing suites pass: **16 flat ingredient codec checks** and **49 real NBT/ValueIO checks**, seven standalone particle classes/base block entity compilation, four superclass save/sync bytecode delegations and the native tank serializer contract. These suites do not test the newly migrated calendar in a world, registry-backed inventories, actual mixin application, block picking, milk use, variants, guide appearance or gameplay. No new source exclusions, missing-system stubs, dependency changes, JAR or runtime success.

Remaining: 713 main Java errors, dominated by client registration, block/item/world rendering and resource-handler capability migration. Data/test compilation, full packaging and client/server/world/survival verification remain open. Candidate version stays `0.0.0`; the independent 26.3 source was not given these gameplay changes.


## 2026-10-12 — JEI category extraction and ingredient enumeration (source-only)

Continued existing `26.1.2` from `0c1d87de5fa635cded8bf13eb2d42e5ecbea7b2f`. Twenty recipe-category files now use `GuiGraphicsExtractor`; category and drawable callbacks retain JEI's `draw` name. Text uses `text`/`textWithWordWrap` with opaque white where the previous API accepted RGB. Lamp animation uses an explicit textured pipeline, preserving its crop/position arguments. No recipe IDs, slot coordinates, arrow/fire durations or registration lists were changed.

Shared ingredient helpers enumerate current item/fluid holders through the existing `RecipeHelpers`. Sized fluids retain `ingredient.amount()`; sized items retain their counts and transient non-decaying food copies. Block ingredients map blocks to items and exclude air before constructing the native ingredient. Casting, heating, simple item and pot displays use the same current enumeration. Lamp fuel uses the native bucket-volume constant for representative unsized fluid stacks.

**Checks:** all 20 Java sources pass parser-only syntax checking. Cached Minecraft 26.1.2 and NeoForge 26.1.2.114 class/source inspection confirms holder enumeration and stack constructors. JEI's upstream `26.1` API source confirms `draw(... GuiGraphicsExtractor ...)` in both recipe categories and drawables; that branch currently identifies specification 29.47.0, so it is not a substitute for compiling against this project's pinned 29.43.0.107 binary. No dependency pins were changed.

**Not verified:** full Java 25 compilation, exact pinned JEI binary linkage, visual layout/animation, custom ingredient display behavior, or client/server startup. Casting's legacy fluid capability remains an explicit separate migration task. Workspace maintenance removed the local checkout; source was recovered from GitHub and these edits were published directly to the existing branch. Available runtime is still Java 17. The latest recorded main compile remains **860 errors at `cdc66669`**, before subsequent guide/HUD/JEI source edits. Do not subtract source edits from that number or call this playable. The historical 190-entry checklist stays unchanged at 77 source-addressed / 113 open pending a new diagnostic pass.

## 2026-10-12 — HUD extraction and contextual-bar source migration

Continued `26.1.2` after `271d84a1`. Migrated health, mounted health, food, thirst/heat, ink, chisel, fishing, mount-jump and familiarity overlays from removed `LayeredDraw.Layer`/`GuiGraphics` and global shader state to NeoForge `GuiLayer`, `GuiGraphicsExtractor`, 2D poses and explicit textured draws. Existing identifiers, sprite placement/crop expressions, text content and survival formulas remain. Heat and ink alpha are submitted per draw.

Vanilla experience and jump layers are now a contextual background/foreground pair that also serves the locator. A client-only `GuiAccessor`, registered in `tfc.mixins.json`, reads the selected native renderer without naming its inaccessible enum. TFC preserves the locator when experience is disabled unless the horizontal fishing meter occupies its space. The vanilla pair is suppressed while that meter draws; the vertical fishing/jump option remains. HUD spacing is no longer reduced when the experience bar or preserved locator still occupies the space.

**Checked:** both Java sources parse; native Minecraft 26.1.2 `javap` signatures and NeoForge 26.1.2.114 source were inspected for extraction, layer registration, contextual renderer, field descriptor, pose/text/texture methods and ARGB conversion. All **19 sprite draw argument sets** (texture, placement, crop and size) match the prior source; all four text draws remain. Mixin JSON parses. These are syntax and source/API checks, not target compilation, a transformed-mixin test or visual gameplay validation.

**Still blocked:** this session has Java 17, no Java 25 runtime, and the Gradle dependency download is network-blocked as recorded in the preceding checkpoint. No full build, JAR or client/server test was produced. The latest measured main compile remains **860 errors at `cdc66669`**, before the subsequent guide/HUD edits; no new total is inferred. Historical report checklist: **77 source-addressed / 113 open**, with these two newly crossed items explicitly awaiting compilation. Validate resource reload, all overlay options, locator/fishing transitions, riding, ink/heat transparency, screen scaling and mixin application before release. No new branch, Actions run or 26.3 change.

## 2026-10-12 — Guide quick-lookup and book hook continuation (source-only)

Continued existing `26.1.2` from `cdc6666928155bf3a271da6b24855392f493763f`. The book background mixin now targets Patchouli's `extractRenderState` with `GuiGraphicsExtractor` and the current 2D pose API. Inventory tabs, the field-guide ID and background scale are retained. Quick-lookup tooltips submit their progress fan through Patchouli's native GUI submission bridge, capture the pose/timing values before deferred rendering, and use current item/text/input/platform/network APIs. Screen-edge placement and server-authoritative opening remain; no selected-slot changes were introduced. Missing books and suppressed tooltips reset lookup timing. Progress geometry is bounded to one revolution.

**Validation limits:** Java parser syntax checks passed for both edited sources. Direct `javap` inspection of cached Minecraft 26.1.2 classes confirms the GUI extraction methods and render-state contracts; cached NeoForge 26.1.2.114 source confirms the tooltip event graphics type. Patchouli's published `release-26.1-94-beta` source confirms the mixin target and GUI submission bridge. This is source/API inspection, not compilation against the pinned final Patchouli binary or an in-game test. This session has Java 17, not Java 25; its Gradle wrapper download fails with `Network is unreachable` before compilation. No new compiler total or playable JAR is claimed. The latest recorded full main compile remains **860 errors at the preceding checkpoint**. The historical checklist stays **75 source-addressed / 115 open**, since these two files are outside that report subset. No Actions run, new branch or 26.3 change was made.

## 2026-10-12 — Local native renderer/guide/fluid checkpoint

Starting branch HEAD: `c920472da99c7c039f68aaa3b82114824e1a74fa`, existing public `26.1.2`. Resolved the actual TFC-patched Minecraft 26.1.2 / NeoForge 26.1.2.114 artifacts and pinned Patchouli 26.1-94 / JEI 29.43.0.107, using JDK 25 and Gradle 9.2.1. No new branch or Actions run was requested. The older CI diagnostic count is not used as this patch's baseline.

### Changes and preservation

Three instrument models carry immutable pose records; their block-entity renderers read world state during extraction and submit native model work with light/breaking overlay data. Vane visibility resets per pose. Calendar orientation/month texture, instrument offsets, all original geometry factory bodies and texture IDs are retained. Javelin geometry uses native `Model.Simple`; projectile orientation/material/foil are copied into its render state. Held-item javelins remain an independent unfinished renderer.

Patchouli custom components implement current extraction and matrix APIs, explicit GUI texture pipelines, modern text codecs, item/fluid holder ingredients and synchronized recipe lookup. The multiblock wrapper calls Patchouli's extraction method. Empty knapping ingredient lists no longer cause modulo-by-zero. Page layout, tooltips and actual multiblock rendering need live validation; Patchouli event/font integration has separate unresolved errors.

New `FluidModelRegistration` shares appearance definitions with underwater extensions, registers every existing source/flowing holder pair, and separately registers river water with vanilla water tint. `FluidRendererExtension` implements the native fluid tint interface. Salt-water climate tint and all existing texture/overlay identifiers remain; RGB dye colors receive explicit opaque alpha. `RenderHelpers` and guide fluid draws use the model set's material/tint. Thirst/exhaustion adopt the server-level callback parameter without changing their effect bodies.

### Reproducible local checks

The container's NeoForm Runtime failed at two `ProcessHandle.Info.command().orElseThrow()` expressions before compiling TFC. `tools/porting/prepare_neoform_java_fallback.py` creates a separate opt-in local runtime JAR from matching official 2.0.31 binary/source JARs. It requires exactly one replacement in each named class and uses `java.home/bin/java` only if process metadata is absent. It does not overwrite dependency caches, substitute game classes, change dependencies/access transformers, exclude sources or suppress build failures. The normal unmodified toolchain remains the release reference. Full target artifact recompilation then completed.

Commands after resolving the exact classpath (`JAVA_HOME` set to JDK 25):

```sh
./gradlew -I /path/to/local/neoform-java-fallback.gradle -I tools/porting/diagnostics.gradle writePortClasspath --no-daemon --no-configuration-cache --console=plain
python tools/porting/compile_main_diagnostics.py
python tools/porting/run_instrument_model_smoke.py
./gradlew -I /path/to/local/neoform-java-fallback.gradle -I tools/porting/diagnostics.gradle build --continue --no-daemon --no-configuration-cache --console=plain
```

Baseline diagnostic compilation used the untouched starting main sources, the same resolved TFC-patched classpath and the runner's javac flags (`--release 25 -parameters -proc:none -Xmaxerrs 10000`). Baseline: 1,657 sources, 957 errors, exit 1, no timeout. Final: 1,658 sources, **860 errors**, exit 1, no timeout. These are displayed compiler diagnostics, including cascades; they do not count independent defects or prove runtime correctness. The new source is fluid model registration.

`run_instrument_model_smoke.py` PASS: three instrument models across 25 pose/visibility scenarios and the javelin model. It compares native root versus previous per-part CPU rendering positions, UVs, normals and vertex multiplicity, including successive vane base toggles. Original `createBodyLayer()` bodies match the starting commit byte-for-byte. The suite is included in `inspect_api.py`; the complete older suite was not rerun for this checkpoint. No replacement Minecraft classes or platform stubs are used by the new test. This is not a GPU or live extraction test.

Machine-readable evidence includes source hashes, unchanged geometry hashes, baseline/final modes and the remaining error counts by file: [2026-10-12-render-guide.json](validation/2026-10-12-render-guide.json). The original user-report checklist advances 21 entries to 75 source-addressed / 115 open, without claiming full compilation.

Full `build --continue` exited 1. Main javac emitted **860 errors**; all three license tasks passed, resource processing was up-to-date and dependency verification ran. `downloadAssets` also failed while provisioning its separate Java 21 toolchain and the temporary disk filled, truncating the final Gradle failure report. An earlier full-build attempt had reached main compilation too but failed to provision Java 21 before these final fluid changes. Only this task's failed download/extraction and temporary problem-report files were removed. The follow-up command is `compileJava checkLicenseMain checkLicenseTest checkLicenseData --continue`, with the same optional local NeoForm init and ordinary failure semantics. That follow-up completes in 49 seconds, exits 1 with 860 compiler errors and passes all three license tasks; its failure report is complete. No build failure is converted to success.

### Remaining gates

No runnable mod JAR, client/server startup, registered resource reload, guide appearance, new-world/save/reload or survival gameplay was verified. The largest remaining diagnostic groups are client registration, world rendering, contained-fluid and trimmed-item models, and overlays. Other block/entity/item renderers, guide event/font code, JEI and common APIs remain. Version `0.0.0`, full gameplay, EUPL notices and branch/dependency policy are unchanged; 26.3 on `26.x` requires separate work.


The preceding complete log is retained byte-for-byte in [WORK_LOG_BEFORE_SNAPSHOT_BLOCK_MODELS](history/WORK_LOG_BEFORE_SNAPSHOT_BLOCK_MODELS.md), linking to the seasonal, mechanical, livestock, model, entity, save and common-API checkpoints. Earlier validation limits remain unchanged.

## 2026-10-09 — Immutable pile, scraping and mold-table models

### Source and publication

Resumed public `26.1.2` at documentation commit `5435d1118a6141f0b67544fbbcd3f37d105d344b`, following source `bf289ca22c3ca3146fe5623cb66c253fb7917044`. The preceding source artifact's SHA-256, `3aeda006199a2819512e15e1aca41ac825c3251153f7d1f48cc76b4db7e6a65f`, was verified. Its exact source supplied the local baseline; the later remote documentation was preserved. The Java 25 toolchain and all 84 resolved classpath JAR hashes were verified. Target and dependency pins did not change.

Staging **04691f1d8b5e9742885b9a30bfd0739c3b692e69** transported the reviewed source with bounded decoding, exact-parent/preimage/postimage checks, path/symlink restrictions and clean-index application. Apply run **37998280708** succeeded. Source **5024736b274f1ad3d764c5bb5572ead6f71a6032** applied all 28 reviewed file changes, removed the one-time transport/workflow and dispatched normal validation. Publication used a normal push, never a force push or concurrent-source overwrite.

Patch `TFC-Foundations-26.1.2-snapshot-block-models.patch`: **28 files, 1,399 insertions, 575 deletions, 126,023 bytes**. SHA-256 **520f01fbc269e99608bd2df20014af0193530e26516b3b18c83233f2916aa109**. XZ transport: **26,228 bytes**, SHA-256 **0f53597cc9259f8819765dc04118800a80e43e4b36b8ee7538740ea5e2f5edbf**. The patch includes two obsolete-interface deletions; it is source/tests/resources, not a runnable JAR or the later documentation checkpoint.

No version increment, gameplay feature removal, dependency substitution, release-gate relaxation, 26.3 change or Earth implementation was performed.

### Production changes

**Snapshot publication:** BlockEntityModelData is client-class-free and contains only immutable values. Pile defensively copies its texture-identifier list and carries a fallback identifier. Scraping captures input/output identifiers, the 16-bit tile mask and opaque colors. Mold-table data contains only a selected item identifier. IngotPileBlockEntity and ScrapingBlockEntity refresh model data after relevant changes and client loads. MoldTableBlockEntity replaces its client-model dependency with an identifier and keeps its existing slot-update path, with a load refresh. No mutable inventory or client baked-model object is sent to geometry workers.

**Native dynamic models:** SnapshotBlockStateModel reads ModelData through the supplied level interface, never getBlockEntity or inventory contents. It caches parts per immutable key with a 4,096-quad weight limit and model-identity geometry keys. Context-free calls return an empty fallback; contextual particle/material flags follow the selected part. Conservative global flags include translucent and animated possibilities. The cache is per baked model, not global. Tests cover concurrent same-key loading, eviction and separation between reload/model identities using synthetic parts. No real chunk scheduling or measured throughput is claimed.

**Pile/scraping geometry:** StaticBlockMesh represents immutable faces, vertices and texture identifiers. The single/double ingot placement formulas, alternating 90-degree layers, fractional bounds, and three legacy trapezoid face bodies are retained. Frozen reference execution compares every vertex position and UV for all valid pile counts, including empty counts. Scraping retains all 16 cells, input/output pass order, tile height and dye routing; explicit opaque alpha prevents RGB dye colors becoming transparent vertex colors. Native face transforms use the block/UV center conventions and inverse face transformation from ModelState.

StaticBlockMeshBaker converts that mesh to the exact target BakedQuad/MaterialInfo API. Material resolution is synchronized around the retained baker because missing-material collection may be mutable; local texture lookup avoids repeated resolution within one bake. **Lighting is intentionally adapted:** white pile vertices allow native shading/light application rather than applying legacy shade a second time. Appearance under actual lighting remains unverified. The baker compiles, but real atlas/material baking is not executed in the standalone suite.

**Mold tables and resource reload:** MoldTableBlockModel registers standalone dependencies from `models/block/mold/` resources, preserving namespaces and exact path-prefix handling. Discovery replaces a volatile immutable identifier/key catalog, including when the result is empty. Each unbaked table captures the current catalog during dependency discovery; each baked table owns immutable base and mold parts and does not look up a global baked-model map. Inspection of the resolved native ModelManager showed standalone-loading completion before model dependency discovery. This inspection is not a registered in-game resource reload.

The loader deep-copies JSON before removing its custom loader field and parses the remainder through native CuboidModel. Dispatch emits the base once and the selected mold once; absent, unknown or removed mold data emits only the base. The registration callback has an unambiguous one-argument method, with the collection-taking helper separately named. A compile-only IEventBus listener probe protects that signature.

**Assets and saved fields:** four blockstates (single/double ingot pile, scraping, mold table) now use the existing `tfc:dynamic` variant route. Their generator reproduces the change. Canonical guards retain identifiers, rotations and multipart conditions. All 20 relevant model assets remain byte-identical. Unsupported item-model inheritance through these world-only routes is explicitly rejected by the source guard. IBakedGeometry and IStaticBakedModel were removed after replacing their users; the guard detects accidental restoration. Three exact block-entity save-writer bodies retain their prior hashes. That is source preservation, not world save/load validation.

### Local checks and corrected attempts

Both JAVA_HOME and PATH select the verified Java 25 JDK and exact 84-JAR target classpath.

```sh
python tools/porting/run_snapshot_block_smoke.py
python tools/porting/inspect_api.py
python tools/porting/compile_main_diagnostics.py
```

The main-source command uses `-proc:none` and explicitly records timeouts/exit codes; it is diagnostic-only. Baseline reports 1,997 errors. The first snapshot-model pass reports 1,927. Adding an overloaded listener helper exposed one ambiguous ClientEventHandler method reference (1,928); renaming that helper restored the completed final **1,927 errors across 1,651 sources, exit 1, no timeout**. The model classes have no diagnostics; ClientEventHandler and MoldTableBlockEntity retain unrelated unfinished APIs.

A first combined standalone run exceeded the host's command-time limit and is not counted as an overall pass. Another overlapping local test invocation raced on a shared temporary class-output directory; its missing-class failure was not treated as a source test success. Final validation runs serially in a separate clean Git worktree and records **PROBES=0**, **COMPILER=1**, 1,927 errors, timed_out=false. All reviewed postimages match. No Minecraft/TFC replacement classes or loader stubs were supplied.

The initial patch replay also caught omission of two staged interface deletions from a worktree-only diff. The frozen final patch was regenerated from `git diff HEAD`, cleanly applied using `--check --index --whitespace=error`, and all 28 changes—including the deletions—were verified. A new deliberate restored-interface mutation test protects this case. The final patch and transport checksums above refer only to that corrected version.

A local `python resources validate` attempt did not execute validation because `mcresources` was absent. Only the independent GitHub resource step, which installs the pinned requirements, can establish that resource-validation result for this checkpoint.

### Standalone coverage and boundaries

The new runner compiles the actual production dynamic models, snapshot values, geometry and baker classes against the resolved target libraries. It executes **102 pile-count scenarios**, compares **65,904 vertices' position/UV data** with PileLegacyReference, covers **all 65,536 scraping masks**, and reports **428,808 counted assertions**, including the vertex comparisons. These measures overlap and must not be summed. It checks immutable lists, color alpha, texture/particle selection, transform order, cache concurrency/eviction, stale-state clearing and model-identity reload separation.

Synthetic model-part and level-interface fixtures are explicitly used for cache/dispatch tests; no real game level is constructed. Native unbaked JSON parsing and standalone registration/dependency objects are exercised, including all 16 TFC mold definitions and an addon fixture. Those checks do not bake an atlas or execute the live ModelManager reload. Texture/material objects and native GPU submission remain outside this suite.

**12 Python mutation-guard tests** pass. They deliberately alter dispatch, multipart rotation, callback registration, defensive copying, forbidden live-world reads, load refresh, the frozen oracle, saved fields, unsupported item inheritance, stale discovery catalogs and obsolete-interface deletion. Four blockstate structures, 20 model assets, three original geometry face bodies and three save-writer bodies have preservation contracts. Source contracts do not instantiate the block entities or prove network/save behavior.

The complete clean replay also passes earlier suites: 173 standalone Java checks, existing Python guards and save/brain contracts, package metadata, the 48-model suite, livestock/cat suite, mechanical/boat/equine suite and seasonal-model suite. Deprecated JOML/target API warnings remain visible. The new test runner and its inspect_api invocation are tracked in the final source.

### Independent GitHub verification

[GitHub run **37998292416**](https://github.com/Shnewbs/TFC-Foundations/actions/runs/37998292416), source `5024736b`, completed with overall **failure** because main compilation remains broken.

| Check | Observed result |
| --- | --- |
| Full Gradle/main compilation | FAIL: exit 1, **1,927 javac errors**; summary groups them into **1,845 path/line/message entries**. |
| New snapshot-model suite | PASS: **102 pile-count scenarios**, **65,904 position/UV vertices**, **all 65,536 scraping masks**, **428,808 counted assertions**. |
| New Python mutation guards | PASS: **12 tests**. |
| Earlier model, utility, save, brain and seasonal suites | PASS; independently reproduced against the published source. |
| Main/data/test license tasks | PASS. |
| Existing resource-validation step | PASS; not a registered game reload or visual test. |
| Published source preservation | PASS: all **28 reviewed changes**, including two deletions, match the downloaded source; the test invocation is tracked and temporary transport files are absent. |

Artifact **11648720875**, `tfc-port-diagnostics-37998292416-1`, retains logs, reports and exact source until **October 16, 2026 UTC**. Downloaded ZIP SHA-256 **88ec609996c45a0bcedbe7ee695904f030a6b63d4ef99b73e152190ced9f7617** was verified. The entire API-inspection/probe step passed, independently matching the local final result. These passing steps do not override the failed full build.

Normal CI still runs the full command without overriding its failure status:

```sh
./gradlew -I tools/porting/diagnostics.gradle writePortClasspath compileJava compileDataJava compileTestJava build --continue --no-daemon --no-configuration-cache --console=plain
```

### Remaining compiler and playability boundary

The locally and independently CI-confirmed reduction is **1,997 to 1,927 (70 diagnostics)**. Counts include cascading failures and are not independent bugs or a completion percentage. ContainedFluidModel, TrimmedItemModel, ClientEventHandler, LevelRendererExtension and overlays remain prominent rendering clusters. Recipes, equipment/materials, capabilities, schedules, registry/holder/worldgen and JEI/Patchouli work remain outstanding.

NOT RUN: live block-entity extraction, real chunk workers, atlas/material baking, registered whole-resource reload, GPU rendering, in-game appearance, client/server startup, world generation/save/reload, multiplayer, survival progression or performance measurements. Earlier registry-backed round trips and bootstrap-dependent probes remain unexecuted. Full downstream data/test compilation and packaging remain blocked; no playable JAR or release is claimed.

Continue with the explicit target and release gates in [PROJECT_STATUS](../../PROJECT_STATUS.md).

## 2026-10-12 — Upstream and version watch

Updated [UPSTREAM.md](UPSTREAM.md#watch-report--2026-10-12) with the six-branch inventory, 17 newly reachable 1.21.x commits, four merged fix investigations, buoys/lobster traps and Chinese guide additions, issue-state changes, source inheritance evidence, and required (not executed) regressions.

The block-entity parent save/load fix is independently equivalent in 26.1.2 through October 9 commit 46401c0; it remains absent in 26.x. JEI compact-grid focus, repeated-cell scraping and freezing-temperature tooltip fixes remain absent from both inspected source snapshots. Equivalent source is not runtime verification.

Official manifest still reports release 26.3 / snapshot 26.4-snapshot-3; NeoForge metadata remains 26.3.0.76 with no 26.4 artifact. Preserve separate 26.1.2 playable priority and 26.x/26.3 track.

Documentation-only watch; no source implementation, upstream merge, dependency pin change, compile/gameplay test or artifact publication. Tracking document commits: 26.x d6d9a47d2dd1a61c03b85e561010289ea5a9d724; 26.1.2 06b8fff7eb0b37db6a0b5808e8716d57bef52a39. Readback of both tracking files matched intended text.
