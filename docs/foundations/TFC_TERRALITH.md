# TFC and Terralith world-generation integration

User requirement, reaffirmed 2026-10-17: retain all TFC features and add Terralith
features that TFC does not offer wherever they can coexist. The goal is more
natural terrain, coherent ecosystems and varied exploration. Existing TFC
world generation must remain available. Finish the runnable port and repair
generation defects before enabling a broad terrain expansion.

## Ownership and feature selection

| System | Required behavior |
| --- | --- |
| Geology and ores | TFC owns rock strata, exposed rock, mineral deposits, veins and loose rocks. New formations use their actual local TFC geology. |
| Climate and ecosystems | TFC owns temperature, rainfall, seasons, soils, crops, forests and wildlife placement. Terrain additions must satisfy those conditions. |
| Water | Retain TFC river drainage, fresh/salt/river water distinctions, aquifers and coast transitions. New landmarks cannot introduce arbitrary water shelves. |
| Survival | Retain TFC recipes, resource availability, tools and progression. Imported structures and loot need a separate progression review. |
| Added landscapes | Review arches, distinctive erosion formations, cave appearance and biome transitions individually. Use TFC materials and placements. |
| Existing overlaps | TFC already has volcanoes, karst, caves, boulders, glaciers and erosion. Compare the actual implementations before adding competing versions. |

TFC already implements its own generator, biome sampling, density composition,
rock layers, hydrology and decoration. Extend that pipeline with a compatibility
layer. Do not replace it with Terralith's Overworld generator or simply merge
every biome/resource. Full generator replacement would bypass the systems above.

Keep the externally installed Terralith distribution separate. The compatibility
implementation and any overrides must be original; preserve Stardust Labs credit
and follow the distribution's own license. Do not vendor Terralith's pack/code
into this EUPL repository. Upstream references:

- https://www.stardustlabs.net/terralith
- https://github.com/Stardust-Labs-MC/Terralith/blob/1.20/license.txt
- https://modrinth.com/datapack/terralith

## Implemented Natural preset

Choose **TerraFirmaCraft — Natural** for a new world, or set
`level-type=tfc:natural` on a new dedicated world. Ordinary `tfc:overworld`
remains available and missing `natural_landscapes` settings decode to `false`.
The original settings constructor remains compatible, and the customization
screen preserves the selected landscape profile. Existing chunks are not
rewritten; switching an established world's generator is not a supported migration.

The Natural preset blends regional and local biome weights with convex cubic
B-splines. It smooths changes in terrain derivatives without negative weights or
height overshoot. Shores and rivers retain their separate TFC blend groups.
Regional climate and geology are unchanged, checked at 289 seed-42 positions
including negative coordinates. Ordinary generation retains its original linear
blending and decoration seeds.

Original, small weathered stone arches fill a gap in TFC's formations. They use
local TFC hardened rock, require dry uplands, reject steep or flooded foundations,
keep a clear opening, and never write outside local coordinates 1–14 of their
chunk. Foundations span at most three blocks of height difference. Structure
reference chunks are excluded. Arches run before vegetation with a separate
world/chunk seed, without inserting or renumbering ordinary biome features.
Wildlife, plants, soil, ores, aquifers and survival progression remain TFC systems.

When installed, the external `terralith:overworld/arch/thickness` density function
is wired to the world seed and sampled to vary arch thickness. Its advertised
range is normalized and bounded; no external definition is copied. Without it,
the original built-in weathering still generates arches. This is a selected
Terralith integration, not replacement by its Overworld generator or import of
all its biomes, ores, structures or loot. No additional engine or hard dependency
is required. Startup logs report which detail source is active; the optional
`tfc.debug.natural_landmarks` JVM property logs successful landmark coordinates.

## Audit and acceptance gates

The inspected 26.1.2 NeoForge artifact is Terralith 2.6.2, Modrinth version
`4xxRkKvw`, `Terralith_26.1_v2.6.2_Neoforge.jar`. Its metadata requires
Lithostitched 1.6.3 or later. The selected integration is exercised with
Lithostitched 1.8.1, Minecraft 26.1.2 and pinned NeoForge 26.1.2.114. These
optional test artifacts are not new TFC dependency pins or bundled distributions.

`tools/porting/audit_terralith_compat.py` reports exact resource collisions,
generator replacements, configured-feature types and literal block palettes
without copying or modifying the external pack. The audit does not resolve
every transitive dependency or conditional overlay and does not prove that
features are safe to enable. Do not equate an optional mod being present with
Terralith landscape features being integrated into a TFC world.

For each addition, require:

1. A concrete gap in TFC's existing feature set and an explicit placement policy.
2. Preserved TFC block materials, geology, climate, water and progression.
3. Native registration/codec tests, deterministic seed output and chunk-boundary
   checks, including negative coordinates and different generation orders.
4. Actual new-world screenshots, traversable terrain, water behavior and
   generation-time/memory measurements against the unmodified TFC baseline.
5. Save/restart and dedicated-server tests. Features remain optional and absence
   of Terralith must preserve ordinary TFC generation.

Existing generated chunks are not rewritten. Terrain corrections and additions
must be reviewed for borders with newly generated chunks. Test new terrain on a
separate fresh world; never delete or silently regenerate a user's saved chunks.

**Status:** the optional preset, smooth terrain blending and original arch layer
are implemented. Actual fresh-world generation is checked with and without the
external pack, including native graphical client joins, data reload, dedicated
startup and save/restart. See `validation/2026-10-17-natural-worlds.json` for exact
results, packaged hashes and limits. These checks do not establish perfect terrain,
matching the user's unknown original seed, every Terralith biome's behavior, or
complete survival/multiplayer release certification. Larger formations and other
candidate features need their own placement, progression and runtime reviews.
