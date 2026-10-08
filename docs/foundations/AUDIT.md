# Source audit and integration design

Inspection: 2026-10-08, baseline `e9d9a88a187d5a33064e2d86b2803f54238cadd6`.

TFC is a survival total conversion: world generation, rock layers/ores, climate/calendar, food/nutrition, agriculture/fauna and metalworking progression are coupled. Preserve that progression for the first playable milestone. Conquest building content must not silently bypass material tiers, heating, tool requirements, support/collapse or resource acquisition.

Inventory: 1,673 main Java files and 26 test Java files including helpers, plus substantial generated data/assets. This is not a coverage measurement.

## Findings

| Area and source | Observation and action |
| --- | --- |
| `build.gradle.kts`, wrapper, templates | Still 1.21.1/Java 21. Verify the complete 26.3 toolchain and dependencies before changing build pins. |
| `.github/workflows/build.yml` | Inherited workflow publishes to CurseForge 302973 and Modrinth JaCEZUhg. Local replacement disables publication and removes upstream destinations. |
| `.github/workflows/test.yml` | Only covers 1.21.x, not default 26.x. Add target-branch coverage during build migration. |
| `TerraFirmaCraft.java` | Registration spans components, recipes, entities, worldgen, custom data, food traits and climate models. Preserve registry identities/lifecycle ordering. |
| `util/data/DataManagers.java`, `DataManager.java` | Existing codec-based managers cover food, heat, size, fauna, climate ranges, drinkables, fertilizer, fuel, fluid heat, knapping, support, lamp fuel, deposits and damage resistance. Extend these before introducing a parallel configuration system. |
| `DataManager.getReference`, `getCheckedReference`, `apply`, `updateReferences` | Shared reference-map access includes locked and unlocked paths. Review thread confinement during reload/recipe loading before declaring or fixing a race. Define adapter lifecycle and atomic data publication. |
| `common/recipes`, `RecipeHelpers.java` | Existing serializers/codecs and indexed recipe lookup should back datapacks and scripts alike. Test ingredients, outputs and reload cache invalidation. |
| `util/events` | Hooks already cover nutrition, climate selection, prospecting, logging, collapse, animal products and fire handling. Document semantics and add missing hooks driven by concrete pack-maker needs. |
| `world/region/RegionGenerator.java`, `world/FastConcurrentCache.java` | Concurrent generation already uses thread-local areas and bounded locked caches. Profile contention/duplicate computation before changing synchronization; preserve deterministic terrain without seams. |
| `util/calendar/ServerCalendar.java` | Calendar sync interval is 20 ticks; progression respects frozen ticks and logged-on players. Preserve pause, sleep, seasons and offline aging behavior. |
| `src/main/resources/tfc.mixins.json` | Hooks span chunkgen, fluids, recipes, components, entities, rendering and Patchouli/Jade/Sodium. Validate each 26.3 target/signature; do not mask failures by making required injections optional. |
| Dependencies and mod template | Patchouli is required and uses implementation internals. JEI, EMI, Jade, TOP and ModernFix dependencies are 1.21-era. Target availability remains unverified. Isolate optional integrations and retain a usable guide if Patchouli blocks the port. |

## Integration boundaries

Keep upstream packages recognizable to simplify fix integration. Establish logical boundaries before moving code into subprojects.

| Boundary | Contract |
| --- | --- |
| Core/public API | Typed, versioned access to recipes, heat/food/size, climate/calendar, agriculture/fauna, metallurgy, support/collapse and progression. Document side/thread ownership. |
| Datapack backend | Codecs, schemas, validation, reload transactions, references, derived indexes and client synchronization. |
| KubeJS adapter | Recipe builders and validated data/event bindings through the same backend; optional matching KubeJS dependency, no script runtime in core. |
| CraftTweaker adapter | Equivalent operations with source-aware errors; optional matching dependency, no scripting types in core. |
| Conquest compatibility | Explicit material/block mappings, recipes, tags, tool tiers and support/collapse rules; compatible optional dependency if available. |
| Visual packs | TFC-native presentation and selectable compatible presentation; client resources with documented redistribution rights. |

Registration remains startup-only; recipes/eligible data can reload. Worldgen changes affect new chunks and require existing-world boundary guidance. Do not promise arbitrary safe runtime changes.

Define deterministic precedence: defaults, ordered datapacks, then explicitly configured script transformations. Specify ordering or reject conflicting adapters. Validate a complete replacement snapshot before activation, invalidate derived indexes and synchronize the accepted revision to clients. Keep server authority. Test duplicate IDs, missing tags/dependencies, invalid ranges, reload failures, reconnect and unload/reload. Avoid reflection in hot loops and unconstrained per-tick script callbacks.

## Performance methodology

Use fixed seeds and identical settings; record hardware, JVM/heap, warmup and mod list. Measure median/p95/p99 tick time, chunk latency/throughput, allocations, heap after GC, reload/startup duration, frame time and network traffic. Profile before changing algorithms; no speedup is claimed yet.

Scenarios: fresh spawn/exploration; developed settlements and processing/storage; farms/livestock; simultaneous multiplayer exploration; dense decorative builds with optional presentation; repeated reloads and dimension travel. Keep generation and steady-state gameplay results separate.

## Conquest and licensing

The official Conquest FAQ allows CurseForge/Modrinth modpacks and otherwise prohibits redistribution of Conquest Reforged/assets. Dependency-based modpack use is different from bundling textures in a new mod. Neither bundling permission nor 26.3 compatibility has been verified. Start with an external optional pack/mod and our own adaptation layer. Original compatible artwork is an alternative; a texture option alone does not port Conquest blocks/features.

TFC uses EUPL 1.2 with separately noted asset/code licenses. Preserve notices and document modifications. EUPL section 3 requires matching machine-readable source alongside executable distribution, or an easily/freely accessible source repository notice. Private development is compatible with later source delivery; a private GitHub URL alone does not supply source to public CurseForge users. Review all exceptions before release.

Sources:
- https://github.com/TerraFirmaCraft/TerraFirmaCraft/tree/1.21.x
- https://github.com/TerraFirmaCraft/TerraFirmaCraft/blob/1.21.x/LICENSE.txt
- https://www.conquestreforged.com/faq
- https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml
- https://piston-meta.mojang.com/mc/game/version_manifest_v2.json
