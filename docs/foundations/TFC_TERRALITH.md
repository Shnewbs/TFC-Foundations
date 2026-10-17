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

## Current audit and acceptance gates

The inspected 26.1.2 NeoForge artifact is Terralith 2.6.2, Modrinth version
`4xxRkKvw`, `Terralith_26.1_v2.6.2_Neoforge.jar`. Its metadata requires
Lithostitched 1.6.3 or later. This identifies the audit input, not a tested
dependency combination or a new TFC dependency pin.

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

**Status:** integration requirement and audit are recorded. The hybrid feature
layer is not implemented or included merely by documenting this plan. The current
repair addresses foliage dependency discovery and erroneous density accumulation;
it does not constitute completed Terralith integration or a perfect terrain engine.
