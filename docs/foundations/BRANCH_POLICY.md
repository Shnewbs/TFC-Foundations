# Standalone system mods — 2026-10-24 clarification

The user clarified the goal with a concrete example: install only the cave-in
mod in a modpack. Each gameplay branch must therefore produce an independently
installable system JAR, without requiring TFC or another extracted system.
The previous interpretation (complete-mod branches with independent Git changes)
was incorrect. The 56 existing refs are development lanes, not 56 completed mods.

`26.1.2` and `26.x` remain separate version integration tracks. Work lives on
`feature/<target>/<system>`. Keep the upstream full-mod source and its history;
create each independent build in `standalone/<system>` with its own settings,
source set, mod ID, resources, tests and runtime metadata. Do not compile against
or bundle the complete TFC source set. Core/build-validation are infrastructure
lanes; they are not marketed as separate gameplay mods.

## Systems to extract

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

## Runtime independence

- A system JAR requires only its matching Minecraft/NeoForge runtime, unless
  the user expressly approves an additional mandatory dependency.
- Include essential local mechanics in the system itself: cave-ins contains
  falling/support behavior and must not require the separate gravity mod.
- Connect separate systems through optional adapters or small API contracts;
  absence of food, animals, gravity or any other sibling must not crash startup.
- Configure ordinary and modded blocks/items through datapacks/configuration.
  Cave-ins starts with working vanilla rules. Inventory item weight is not a
  prerequisite for collapsible blocks and structural supports.
- Preserve EUPL notices and upstream attribution. Standalone namespaces must
  avoid collisions with the full mod. Do not promise automatic old-save migration.
- Native startup with TFC absent, resource reload, actual gameplay, bounded work,
  saves/restarts and dependency/JAR-content audits are required per system.
  An empty scaffold, branch ref, copied source or algorithm-only test is incomplete.

## Git workflow

Start each branch from its matching version baseline; do not stack feature
branches. Shared build/policy prerequisites land in the baseline first. Merge
only verified changes into the matching version; never infer 26.x success from
a 26.1.2 result. The branch-history guard remains a useful additional check:

```sh
git fetch origin
python3 tools/porting/check_feature_branches.py --remote origin
python3 -m unittest discover -s tools/porting -p 'test_feature_branches.py'
```

It checks ancestry, not runtime dependency isolation. Publish with `[skip ci]`
while Actions quota is suspended. Record extraction state honestly in the
registry and per-system README/evidence; do not mark other branches complete
when only cave-ins has been extracted.
