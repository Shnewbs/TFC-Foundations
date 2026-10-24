# Independent system branches — 2026-10-24

The user replaced the earlier no-feature-branches rule and clarified that the
intended model is **independent Git changes with the complete mod on every
branch**. Systems are not separate installable mods. Existing gameplay is not
removed from the integration base or reconstructed by merging feature branches.
Existing integrated fixes and history remain intact.

## Version targets and branch names

- `26.1.2`: first playable integration target.
- `26.x`: independent 26.3/future 26.4 integration target; it does not inherit
  26.1.2 code or validation automatically.
- `feature/<target>/<system>`: one development lane for each system below.

Both target families have the same system names. Each initial feature ref points
to its matching version's policy checkpoint. Creating these refs does not claim
that new system rewrites have been implemented or that the 26.x port builds.
The machine-readable registry is [FEATURE_BRANCHES.json](FEATURE_BRANCHES.json).
Navigation entry points are examples, not restrictive directory ownership.

## Systems

| System suffix | Responsibility |
| --- | --- |
| `core` | Registries, configuration, lifecycle, network, persistence and common API contracts. |
| `cave-ins` | Collapse propagation, support beams, collapse recipes and events. |
| `gravity` | Falling blocks, landslide rules, settling, collision and falling entity saves. |
| `food` | Food components, spoilage, preservation, nutrients, meals and food synchronization. |
| `animals` | Livestock, breeding, domestication, predators, prey, aquatic animals and behavior. |
| `player-survival` | Thirst, health, exhaustion, effects and player survival state. |
| `farming` | Crops, farmland, nutrients, fertilizer, compost and crop harvests. |
| `ecology` | Wild plants, trees, forests, groundcover, plant growth and regeneration. |
| `climate-calendar` | Seasons, temperature, rainfall models, weather and calendar persistence. |
| `worldgen` | Terrain blending, biomes, caves, rivers, coasts, aquifers, structures and presets. |
| `geology-ores` | Rock layers, ore veins, deposits, prospecting and mineral distribution. |
| `volcanoes` | Volcanic terrain, cinder cones, crater fluids and geothermal placement. |
| `terralith` | Optional compatible terrain detail; retain TFC geology, climate, ecology and progression. |
| `earth` | Earth coordinate/elevation groundwork and future original Earth generation. |
| `metallurgy` | Alloys, casting, forging, welding, bloomery and blast furnace progression. |
| `heat-fire` | Heat components, fuels, firepits, charcoal, kilns and heating recipes. |
| `glassworking` | Glass operations, blowpipes, glass basins and glassworking recipes. |
| `crafting` | Recipe infrastructure, knapping, pottery, textiles, quern, scraping and output providers. |
| `fluids` | Fluid definitions, water behavior, fluid components, tanks and fluid transactions. |
| `storage` | Inventories, containers, vessels, chests, item sizes, encumbrance and item transactions. |
| `machines` | Rotational networks, shafts, gears, water wheels, windmills and powered devices. |
| `tools-combat` | Tools, weapons, armor, damage, fishing, hunting and equipment behavior. |
| `building` | Construction blocks, woodwork, furniture, ropes, lights and decorative behavior. |
| `transport` | Boats, minecarts, seats, vehicle inventories and movement. |
| `rendering` | Models, atlases, materials, entities, block entities, particles, sky and weather visuals. |
| `interface-guide` | Screens, controls, HUD, accessibility, translations, creative tabs and Patchouli pages. |
| `integrations` | JEI, EMI, Jade, One Probe, Vivecraft and other optional adapters. |
| `build-validation` | Build tools, dependency checks, generators, test harnesses and validation records. |

## Independence rules

1. Start and refresh a feature from its matching integration branch. Do not
   branch from or merge an unmerged sibling feature. A cave-in change must build
   with the gravity implementation already in the integration base, without
   needing the gravity branch. The same applies to food/animals and other pairs.
2. If several systems need a new interface, codec or shared helper, make that
   a small core change, validate it and integrate it first. Then refresh the
   feature branches. Do not duplicate shared infrastructure to avoid a dependency.
3. Keep a feature's behavior, resources, recipe/tag changes, datagen inputs,
   generated outputs, tests and relevant guide pages together. Rendering changes
   specific to an animal can stay in the animal change; generic renderer changes
   belong to the rendering lane. Shared registration/configuration/event files
   remain one implementation and need coordinated edits.
4. Validate each branch against the integration base plus **only its own delta**.
   Run the pinned full build and relevant native client/server, resource reload,
   save and gameplay gates. Record exact results; history checks cannot discover
   a semantic dependency introduced by a cherry-pick or copied source.
5. Merge verified features into their matching integration target. After a merge,
   refresh affected siblings from the integration branch. Prefer merging the
   target into published feature branches; do not rewrite published history or
   force-push to keep a lane current. Do not cherry-pick across version families
   without adapting and independently validating the destination.
6. The old fixes are already integrated. They are retained as baseline behavior,
   rather than undone to manufacture feature diffs. No separate system rewrite
   is part of the branch-setup checkpoint.

## Local branch-history check

Fetch current remote refs first; this checker is read-only and never fetches,
creates branches, merges, or pushes:

```sh
git fetch origin
python3 tools/porting/check_feature_branches.py --remote origin
python3 tools/porting/check_feature_branches.py --remote origin --branch feature/26.1.2/food
python3 -m unittest discover -s tools/porting -p 'test_feature_branches.py'
```

The guard validates the registry and expected refs, matching version ancestry,
refresh against the current integration target, and absence of commits shared
with an unmerged sibling. After a shared change lands in the target it is allowed
on all siblings. Missing refs, unknown system names and stale branches fail.
A pass is a Git-history check, not a successful Gradle build or gameplay test.
Use a full-history fetch; shallow clones cannot establish these relationships.

Actions quota remains suspended: use `[skip ci]` for publication until restored.
The current documentation-only setup needs no replacement JAR; unchanged source
retains its prior validation and limitations. Future code changes must pass their
own gates before integration.

## Historical consolidation audit (superseded policy)

The following record explains the earlier cleanup; its branch restrictions were
superseded on 2026-10-24 and must not be applied to current development.

### Branch cleanup — 2026-10-13 UTC

Requested policy: develop directly on `26.1.2` and `26.x`. Do not create task, repair, validation, or transport branches. A new Minecraft target is the only reason to add a long-lived branch.

Audited source head: `c1d13325d61a501f639f8752eab5103bc38ab7f5`. Sixteen `port/26.1.2-*` refs are redundant. Ten tips are ancestors of the source head. The remaining six refs share five unique commits whose changed files were compared with the current source. Their bush codecs, height bounds, item-tag holder enumeration, fluid tags, iron chain state, recipe keys, ash color and sloping rail placement are retained. Later camera, command permission, pathfinding, identifier and shared rail helper migrations are retained as well.

The consolidation commit preserves the three divergent tip histories (`c02eb940adb93ba39ed7804be3b57c899f63e915`, `d37330e2f51722c2d6b24754bbf2eb4cd38494e5`, and `3417ccc3657d99eb6c5e496c3c1ae0aafd5863e4`) as additional parents. No gameplay source is reverted or altered by consolidation. All sixteen old branch tips are then reachable from `26.1.2`; deleting the names does not discard their commits.

Only `26.1.2` and `26.x` should remain after cleanup. No open pull requests were present at the audit. Cleanup is not a compiler or runtime validation.
