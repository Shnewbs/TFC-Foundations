# TFC Foundations — Current Project Status

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
Exact evidence and limits: `docs/foundations/validation/2026-10-17-foliage-terrain-fix.json`.
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
repair. See `docs/foundations/TFC_TERRALITH.md` and the audit JSON for feature ownership and runtime
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
Details: `docs/foundations/validation/2026-10-17-rabbit-native-assets-fix.json`.
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

## 2026-10-12 — Instrument, projectile, guide and fluid rendering checkpoint

Continued existing `26.1.2` from `c920472da99c7c039f68aaa3b82114824e1a74fa` using the pinned Java 25 / Minecraft 26.1.2 / NeoForge 26.1.2.114 classpath. A fresh same-mode diagnostic main compilation fell from **957 to 860 errors** (97 fewer displayed diagnostics, not independent bugs or a completion percentage). The final pass covers 1,658 sources, exits 1, and does not time out. The original 190-entry checklist now has **75 source-addressed / 115 open**; it is only a historical report subset.

- Anemometer, calendar clock and vane rendering now extract immutable model values and submit through the native renderer. Calendar facing/month texture, vane attachment offset/base visibility and all four instrument/javelin geometry factories are preserved.
- Thrown javelins use native render-state extraction, material textures and a separate foil submission. The held-item javelin renderer remains unfinished.
- Patchouli custom components use `GuiGraphicsExtractor`, explicit textures/tints, current ingredient holders and TFC's synchronized client recipe map. Knapping pages guard empty matching inputs. Other guide integration errors and actual book appearance still need work.
- Fluid appearance is registered through `RegisterFluidModelsEvent` for source/flowing pairs and river water. Existing underwater overlays and climate-dependent salt-water tint are retained. Dye fluids explicitly gain opaque alpha; fluid GUI helpers now resolve native models.
- Thirst/exhaustion tick callbacks accept the new `ServerLevel` argument; their effect logic is unchanged.

**Validation:** instrument model probes PASS (25 pose/visibility scenarios plus javelin geometry, against real target libraries); all four geometry factory bodies match the starting commit. Main javac has no diagnostics in the migrated models/renderers, custom guide component package, fluid extension/registration, or effect classes. `ClientEventHandler` and `RenderHelpers` still have unrelated errors. Full Gradle `build --continue` FAILS: main compilation emits 860 errors, while `downloadAssets` also fails during separate Java 21 provisioning/disk exhaustion. Main/data/test license tasks PASS. The subsequent compile/license-only run completes in 49 seconds with the same 860 compiler errors and all three license tasks passing. No successful package or runtime test is claimed.

**Environment qualification:** stock NeoForm Runtime 2.0.31 could not obtain its Java executable from `ProcessHandle.Info.command()` in this container. An explicitly opt-in local helper supplies `java.home/bin/java` at those two tool call sites; target pins, game patches, source sets and build gates stay unchanged. This enables compilation evidence but is not a stock-toolchain release validation. See [continuation instructions](docs/foundations/CONTINUE_BUILDING.md) and [machine-readable evidence](docs/foundations/validation/2026-10-12-render-guide.json).

**Still incomplete:** full main/data/test build, item/contained-fluid models, client event and world rendering APIs, remaining guide/JEI/capability APIs, resource reload, client/server startup, world/save/survival tests. Version stays `0.0.0`. No gameplay system was disabled, dependency pin changed, or `26.x` migration attempted. Publication uses `[skip ci]` while Actions quota is suspended.


## 2026-10-12 — Deferred block loot-table API migration (source only)

On existing `26.1.2`, `ExtendedProperties.dropsLike(Supplier<Block>)` now uses the supplier's `IdHolder.getId()` to construct the same default `<namespace>:blocks/<path>` loot-table key via `Properties.overrideLootTable(Optional<ResourceKey<LootTable>>)`. This replaces removed `Properties.lootFrom(Supplier)` without calling `Supplier.get()` during block registration. Audited call sites: standing/wall torch pairs in `TFCBlocks` and standing/wall wood signs in `Wood`; their suppliers resolve to `TFCBlocks.Id` registry holders. Suppliers without IDs now fail explicitly rather than forcing unsafe early block construction. Full block drops, registrations, resource data and game runtime remain untested. EUPL license and all block/loot identifiers are unchanged.

The source-addressed compiler checklist subset is now **54 / 190**, with **136 still open**. These counts record source changes, **not a verified compilation pass**. The latest available Gradle failure predates recent source commits, so it does not validate this fix. Current local runtime lacks Java 25 and the resolved NeoForge dependency classpath; no Gradle build, JAR, client/server startup, or gameplay testing was possible. Historical port branches were checked: the wholly ancestor branches need no merge; divergent branches require careful per-file reconciliation and were not blindly merged. Minecraft 26.3 `26.x` stays separate. No GitHub Actions run was triggered.


## 2026-10-11 — Animal conversion API continuation (source only)

On `26.1.2`, commits `860c9ee` and `9c899d9` replace the removed boolean `Mob.convertTo` overload in wolf-to-dog and ocelot-to-cat taming with `ConversionParams.single` and native after-conversion callbacks. The callbacks preserve spawn initialization and adult age; wolf conversion also preserves gender. The source was reread after publication. The 190-entry report checklist is now **53 source-addressed / 137 open**; these two entries still require a clean build. Local runtime is Java 17. The checked-in Gradle 9.2.1 wrapper could not download its distribution because network sockets are blocked, so no javac task started; Java 25 and the dependency cache are also absent. There is no full compile, runnable JAR, or gameplay test from this checkpoint. The `26.x` forward track and Minecraft 1.21.1 were not changed.

## 2026-10-11 — Ten additional javac API corrections (not compiled)

The source-only checklist now shows **51 source-addressed / 139 open** of its original 190-entry subset. Newly committed direct-branch fixes: clock removal hook, POI holder lookup, penguin water/bubble ambient check, horse listener superclass/override cleanup, direct block loot table transfer and postprocess callback, barrel rack release, bloomery inventory ejection in block entity pre-remove effects, and inventory tooltip `GuiGraphicsExtractor` conversion (height + image API).

**Known incomplete behavior:** `ExtendedProperties.dropsLike(Supplier<Block>)` still uses removed `Properties.lootFrom` and requires a deferred-safe loot-table strategy; the horse inventory-change callback was removed from the superclass, so TFC encumbrance invalidation must be hooked into the new inventory model. Rendering and capability refactors remain outstanding. There is no passing build or client/server test yet.

## 2026-10-11 — Ten further source repairs (not compiled)

Ten direct-branch Java changes were committed for five removed-block lifecycle hooks (anemometer, thermometer, vane, nest box, sluice), three removed `RenderShape.ENTITYBLOCK_ANIMATED` values (glass basin, hot-poured glass, water wheel), the kelp-tree fluid tick access, and the predator swimming fluid predicate. The source blobs were reread and the intended edits were verified. Relevant NeoForge 26.1 documentation confirms `affectNeighborsAfterRemoval` is the replacement for old post-removal neighbor updates; inventory cleanup remains a separate `BlockEntity#preRemoveSideEffects` responsibility. See the compiler checklist for commit IDs and per-error tracking. Rendering, block drops, save/reload, AI fluid behavior, and the overall build are **not** verified.

## 2026-10-11 — Local Gradle Java errors and active source repairs

The user's Windows Gradle build moved past Python item-selector generation and reached `:compileJava`, which **failed** with Minecraft 26.1.2/NeoForge API errors. The uploaded Gradle Problems Report has **190 top-level error entries** (across 96 files) plus warnings/advice. These are a report subset, not the complete number of javac errors emitted by the build. The checklist is at [COMPILER_ERROR_CHECKLIST_26.1.2.md](docs/foundations/COMPILER_ERROR_CHECKLIST_26.1.2.md): **51 source-addressed, 139 still unchecked**. Crossed-out items have **not** yet been proven clean by a follow-up Gradle run.

Most recent source fixes, committed **directly** to `26.1.2` without creating branches: glassworking item tooltip callback (`a39cd36`), biome precipitation/sea-level argument (`f55759d`), tamable stop-attack predicate (`fc29c56`), holding minecart interaction/spawn/destroy method signatures (`91c27f9`), spring/salt fluid entity checks (`dd86ac8`), bubble-column client-level accessor (`3d63e55`), rejecting invalid minecart chest slots (`4fff220`), client camera block-position accessor (`a8b6ede`), and block-entity type constructor registration (`8210c9e`). The source and checklist were reread after pushes; there was no local Java 25 or full NeoForge classpath available for actual compilation. Rendering state extraction, capabilities' resource-handler migration, GUI/JEI/Patchouli and other errors still require substantial implementation.

**Do not mark this playable.** `compileJava`, full `build`, client/dedicated server startup, worldgen, save/reload, and survival scenarios all remain unverified or failed in the latest available evidence.

## Branch consolidation audit — 2026-10-11 UTC

The only intended long-lived branches are `26.1.2` (first playable) and `26.x` (26.3/future 26.4). Three temporary `port/` branches remain visible pending deletion; **do not create more**.

- `port/26.1.2-horse-rope-api`: identical commit to `26.1.2` at audit; zero ahead/behind.
- `port/26.1.2-cooldown-api`: historical unique commit, but all three affected Java file blobs match `26.1.2` byte-for-byte. Changes are already present; no new merge is needed.
- `port/26.1.2-entity-spawn-20261011`: historical unique commit, but all seven affected Java file blobs match `26.1.2` byte-for-byte. Changes are already present; no new merge is needed.
- No open pull requests were found at audit. Deleting the redundant refs requires a GitHub branch-delete action, which is unavailable through the currently connected GitHub integration; **do not force-push or reset refs just to make a branch disappear**.
- This audit is source-content verification, not a successful compile. There is no newly verified Java 25 build, client startup or playable JAR as a result of this check.

Continue new source work directly on `26.1.2`; carry applicable verified migrations independently into `26.x`.

Updated 2026-10-10 UTC. Latest verified build summary is first; later sections preserve a historical checkpoint.

**Version 0.0.0 remains an incomplete source port. Full compilation, client/server startup and survival testing have not passed. No playable JAR or release was produced by this checkpoint.**

## Direction and targets

Continue public development in `Shnewbs/TFC-Foundations`, preserving history, EUPL notices, credits and `tfc` identifiers. First-playable priority remains Minecraft 26.1.2, NeoForge 26.1.2.114, JEI 29.43.0.107 and Patchouli 26.1-94. Java 25 and Gradle 9.2.1 are unchanged. The separate `26.x` branch remains the Minecraft 26.3 forward track toward a separately verified 26.4 port. No 26.3 or Earth code changed here. Core gameplay, visuals, the required guide and publication gates remain enabled.

## Live branch handoff — 2026-10-10

**Branch `26.1.2`** at source [`659b70ed82b0e7fd2ab54914fd37ad20839b53e0`](https://github.com/Shnewbs/TFC-Foundations/commit/659b70ed82b0e7fd2ab54914fd37ad20839b53e0). [CI run `38070987803`](https://github.com/Shnewbs/TFC-Foundations/actions/runs/38070987803) **failed full Java/Gradle compilation**: **1,384 diagnostic occurrences** and **1,326 unique displayed file/line/message entries**. API/standalone inspection and resource validation **passed**, but those checks do not make a runnable mod. No playable JAR or successful client/server or world save/reload test has been produced.

The 26.1.2 branch has since added tool-material, recipe and ingredient API migrations, worldgen and registration fixes, and new-format item selector generation to the October 9 model checkpoint below. The item-selector generator covers **5,369 static and 66 fixed-fluid** models; **162 special models** remain unported. The separate `26.x`/26.3 track remains an independent, unverified foundation toward 26.4.

**The latest cross-platform instructions live in [CONTINUE_BUILDING.md](docs/foundations/CONTINUE_BUILDING.md).** Always inspect the live branch and CI before a continuation, and update this status after each substantive source push.

## Historical pile/scraping checkpoint (2026-10-09)

### Original checkpoint narrative

Source **`5024736b274f1ad3d764c5bb5572ead6f71a6032`** applies a **28-file checkpoint**, following staging `04691f1d8b5e9742885b9a30bfd0739c3b692e69`. Apply run `37998280708` verified every source preimage/postimage, removed the temporary transport and used a normal push. Two obsolete baked-model interfaces were replaced by the native pipeline and deleted; no gameplay feature was removed.

Ingot piles, double-ingot piles and scraping models now use immutable model-data snapshots instead of reading mutable inventories from geometry-generation workers. The snapshot payload contains texture identifiers, colors and scraping progress, not live block entities or item stacks. Relevant mutations and client loads request fresh model data. Per-model geometry caches are bounded and reload-local; their keys distinguish both snapshot contents and model identity. Concurrency tests use fixtures, not live chunk workers.

Pile positions, layered rotations and UV coordinates retain the frozen legacy geometry. Scraping retains its 4-by-4 tile layout, input/output passes and dye colors; RGB colors gain explicit opaque alpha. **Lighting adaptation:** pile geometry uses white vertex colors with native shading/light application instead of pre-applying the legacy shade. Actual lit appearance is not verified pixel-identical.

Mold tables carry the selected item identifier rather than a cached client model in their block entity. Resource discovery uses the native standalone-model registration/dependency path. Each baked table owns its model map; missing or removed mold state falls back to the base table, and a later resource discovery replaces rather than accumulates the catalog. Base table and selected mold are emitted once each in dispatch fixtures. Actual atlas baking and a registered whole-resource reload are not tested.

Four blockstate definitions and their generator now route through `tfc:dynamic`. Existing model identifiers, rotations and multipart conditions are guarded. All 20 relevant model JSON assets and the three block-entity save-writer bodies remain unchanged. These source contracts are not whole-world save-compatibility tests. Contained-fluid and trimmed-item models were not changed in this checkpoint.

## Validation

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

The clean extracted-source patch replay completed all standalone suites with **exit 0** and diagnostic main compilation with **1,651 source files, exit 1, 1,927 errors and no timeout**. The edited source reports the same compiler result. These main passes use `-proc:none`, not the complete Gradle/mixin/data/test pipeline. Compared with the previous 1,997 pass, the reduction is **70 diagnostics**, not an independent bug count or completion percentage. Changed model classes have no diagnostics in this traversal; ClientEventHandler and MoldTableBlockEntity still have separate unfinished APIs.

The new suite checks **102 pile-count scenarios**, compares **65,904 vertices' positions/UVs** against the frozen legacy path, covers **all 65,536 scraping masks**, and records **428,808 counted assertions**, including the vertex comparisons. These overlapping measures must not be added into a single total. **12 Python regression-guard tests** pass. Snapshot immutability, concurrent cache loading/eviction, model-data routing, stale-state clearing and reload isolation are exercised using explicit synthetic model-part/interface fixtures. Native unbaked loader parsing and standalone dependency registration are exercised against the exact target libraries; atlas/material baking is not.

Earlier utility/save/brain and headless model suites remain passing in the clean replay, including 173 prior standalone Java checks, previous Python guards, package metadata and prior seasonal/geometry checks. The new runner is tracked and called from `inspect_api.py`. Three save-writer source hashes and the deleted-interface guard protect against accidental source omissions.

**Not run:** live block-entity extraction or chunk-worker scheduling, atlas/material baking, registered whole-resource reload, GPU rendering, in-game appearance, client/server startup, new-world generation, save/reload/reconnect, multiplayer, survival progression or performance measurements. Earlier registry-backed round trips and bootstrap-dependent probes remain unexecuted. Full downstream data/test compilation and packaging remain blocked by main compilation.

## Next compiler and playability gates

1. Continue contained-fluid and trimmed-item models, other block/item/block-entity rendering, ClientEventHandler/world rendering/overlays and JEI/Patchouli adapters without removing these systems to compile.
2. Finish recipes, equipment/materials, capabilities, schedules, registry/holder and world-generation APIs; pass complete main/data/test compilation, licenses, resource generation and packaging.
3. Test actual client/server startup, resource reload and appearance, world creation/save/reload, calendar, inventories/fluids, guide/recipes and survival before calling an artifact playable.
4. Independently verify shared work on 26.3 rather than copying divergent target APIs blindly.

Normal TFC generation remains the default. The optional natural Earth request remains nominally one block per horizontal meter without generated manmade structures. Existing coordinate/elevation groundwork is not a selectable Earth preset, licensed dataset pipeline or complete chunk generator. Projection, height, geology/climate, caching and performance requirements remain open.

See [WORK_LOG](docs/foundations/WORK_LOG.md) for commands, commits and limits. The preceding complete status/log are preserved byte-for-byte in [STATUS_BEFORE_SNAPSHOT_BLOCK_MODELS](docs/foundations/history/STATUS_BEFORE_SNAPSHOT_BLOCK_MODELS.md) and [WORK_LOG_BEFORE_SNAPSHOT_BLOCK_MODELS](docs/foundations/history/WORK_LOG_BEFORE_SNAPSHOT_BLOCK_MODELS.md).
