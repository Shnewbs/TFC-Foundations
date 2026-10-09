# TFC Earth: design and release gates

Status: coordinate/elevation math foundation implemented and independently unit-tested; the optional world preset remains proposed. No Earth generator or dataset is included in this checkpoint. Source and engine checks performed 2026-10-08 (America/Los_Angeles).

The accepted build priorities remain a playable Minecraft 26.1.2 alpha at version 0.0.0, and a separate 26.3 foundation for 26.4. Earth must not delay repairing the existing TFC survival loop. This document records the requested feature and a concrete implementation path, not a claim that an Earth world can currently be created.

## Implemented foundation in this checkpoint

`world/earth/GeographicPoint`, `EquirectangularProjection` and `ElevationTransform` are independent of Minecraft classes. They validate finite coordinate/scale inputs, keep the date line half-open rather than duplicating it, preserve north/south axis conventions, expose east-west projection distortion, and reject elevations outside an explicitly supplied surface interval instead of clipping mountains or ocean floors. The projection is a declared spherical approximation, not a regional transverse Mercator implementation.

Eight JUnit tests passed using Java 25 and JUnit Platform Console 1.10.3 with only these classes and `test/earth` compiled. They check geographic reference points and round trips, boundary rejection, distortion, sea-level/bathymetry mapping, quantization and invalid data. This isolated test pass does **not** demonstrate that the full mod compiles or that Earth generation works. Nothing registers or calls an Earth generator yet; terrain packs, biome/climate integration and runtime gates remain outstanding.

## Player experience

Add **TFC Earth** alongside the normal TFC world option after its creation, data validation and gameplay gates pass. Select a geographic starting location and an installed region pack; show its terrain resolution, horizontal projection, vertical scale, download size and naturalization limitations before creation. The first playable Earth milestone should cover a bounded real region. Global coverage follows the same contracts later.

Use measured geography for coastlines, relief and climate. Generate TFC trees, soils, wildlife, caves, rocks and resources on that geography. Do not import roads, buildings, cities, farmland layouts or other constructed features, and disable generated villages, monuments, ruins, mineshafts and other artificial structures in the Earth overworld. Normal player building remains available. Audit mod-provided structure placement and TFC decoration separately; a structure toggle does not filter manmade shapes already present in elevation data.

Modern elevation and land-cover observations cannot reconstruct an exact prehuman Earth. Dams, quarries, road cuts, reclaimed land and cultivated terrain can remain in bare-earth data. The naturalization pass must document this limit and identify areas with unresolved artifacts. It must not be described as a historically accurate reconstruction.

## What “1:1” can mean

A block grid can use one projected metre per horizontal block. It cannot preserve every real-world distance across an entire curved Earth on one flat plane. Source resolution is separate: interpolating a 30 m terrain raster onto one-metre blocks does not create measured one-metre detail.

| Mode | Horizontal interpretation | Vertical interpretation | Initial status |
| --- | --- | --- | --- |
| Earth region | A local metric projection, one projected metre per block; publish maximum ground-distance distortion over the selected boundary | One metre per block only where the complete regional elevation range plus headroom fits; otherwise explicitly compressed | Preferred pilot |
| Earth global | A documented global projection, nominal one projected metre per block; distances and shapes distort | Explicitly compressed to a tested dimension height | Later milestone |
| Uncompressed full Earth | Cannot preserve all global distances on a flat grid | Full ocean-to-mountain relief exceeds ordinary engine limits | Separate engine research; not promised |

A simple global engineering baseline is spherical equidistant cylindrical coordinates, with radius 6,378,137 m: `x = R * longitudeRadians`, `z = -R * latitudeRadians`. The approximately 40.1-million by 20.0-million-block rectangle fits the ordinary horizontal world bounds when centered. East-west ground distances are stretched by `1 / cos(latitude)` in this particular projection: twofold at 60 degrees. This is a calculation for the stated spherical model, not an assertion of exact WGS84 survey distances. The poles and antimeridian require explicit edge handling. Do not silently wrap blocks or duplicate longitude cells. Regional transverse Mercator is a better initial choice for recognizable local terrain; its numerical accuracy is not the same as zero map distortion. [Projection references](#primary-source-register).

Engine inspection of both official 26.1.2 and 26.3 client jars found:

- Their vanilla overworld definitions use `min_y = -64`, `height = 384`, and sea level 63: playable block Y values -64 through 319.
- `WorldBorder.MAX_SIZE` is 59,999,968 and its maximum center-coordinate constant is 29,999,984.
- `BlockPos` packing reserves 26 bits each for X and Z, and 12 for Y. `DimensionType` derives a 4,064-block span, minimum Y -2,032 and maximum Y 2,031. Its codecs and constructor also enforce height/coordinate bounds and section alignment. A JSON height edit alone cannot provide full uncompressed Earth relief.

These are inspected engine limits, not proof that TFC or other mods support the maximum custom dimension height. Initial Earth generation should retain the normal 384-block dimension, with a documented elevation transform and reserved bedrock, caves, ocean depth and construction headroom. Before considering a taller profile, audit TFC climate altitude adjustment, veins, aquifers, lighting, section allocation, packets, entities, rendering, saves and integrations. Never flatten high terrain by silently clipping it at a ceiling.

## Data candidates

Keep source licenses and notices in each separately distributed data pack. Code licensing does not replace dataset licensing. Pin exact releases, checksums, projection/datum metadata, processing versions and no-data conventions; do not track a moving “latest” source inside an existing world.

| Layer | Candidate and measured resolution | Use and constraints |
| --- | --- | --- |
| Global elevation/bathymetry | GEBCO 2026, 15 arc-second grid, approximately 464 m north-south | Coarse global coverage and ocean depth. Its source-identification layer can expose measured versus estimated coverage. Public-domain distribution under GEBCO's terms includes attribution and no implied endorsement. Resolution varies in the underlying observations. |
| Global land elevation | Copernicus GLO-30, approximately 30 m | Higher-detail land candidate. It is a **surface model containing vegetation, buildings and infrastructure**, so it needs filtering or a better terrain source before satisfying the natural-terrain goal. The provider publishes a free license and required attribution notices; preserve those notices in derived packs. |
| Regional bare-earth elevation | USGS 3DEP, 1 m where available | Strong U.S. pilot candidate. Public-domain bare-earth products, with coverage and per-tile datum/projection requirements. Bare earth removes surface objects, not all human earthworks. Transform NAD83/UTM and NAVD88 data properly before blending with global sources. |
| Land cover | ESA WorldCover 2021 v200, approximately 10 m | CC BY 4.0. Use forest, shrub, grass, wetland and water classes as inputs. Replace built-up and cultivated classes with explicitly modeled potential vegetation rather than generating present-day cities or crop grids. A land-cover map does not uniquely identify tree species. |
| Climate | CHELSA climatologies 2.1, kilometre scale; choose a fixed historical baseline | Monthly temperature and precipitation inputs. The current provider page lists CC0 1.0. Confirm the selected package metadata matches that license and record its citation. Convert temperature units and precipitation rates deliberately, and calibrate TFC's rainfall scale rather than injecting raw values into its 0–500 range. |

WorldClim is not a default redistribution source: its own page requires permission for redistribution or commercial use. FABDEM is potentially useful for removing forests/buildings, but the inspected provider license is CC BY-NC-SA 4.0, so do not bundle it in the standard openly distributed pack without resolving those restrictions. These are concrete dataset-selection constraints, not a reason to block the ordinary mod build.

No large dataset belongs in the mod JAR or source repository. A 1 m grid of a 16 km square already contains 256 million samples; a single 16-bit elevation channel is about 512 MB before compression. Start with smaller test regions, measure pack sizes, and provide separately installable, bounded packs. Generate Minecraft chunks only when visited; do not pregenerate the whole Earth.

## Fit to the current code

The current preset is `src/generated/resources/data/tfc/worldgen/world_preset/overworld.json`. `TFCWorldGen` registers the chunk-generator and biome-source codecs. `TFCChunkGenerator` creates `RegionGenerator`, receives its `RegionChunkDataGenerator`, samples height/biomes, fills terrain, runs aquifers and builds surfaces. `ChunkData` carries climate, forest, groundwater and rock information used well beyond rendering. A heightmap replacement alone would leave the wrong climate, spawn and survival resources.

Introduce new, versioned `tfc:earth` identifiers without renaming `tfc:overworld` or existing resource IDs. Keep the default preset unchanged. Proposed components are:

1. **Earth metadata and coordinate layer:** immutable projection, extent, horizontal metres per block, elevation transform, source-pack hashes and generator format version. Geographic coordinates use double precision and a defined pixel-center convention. Store the complete definition in the world's generator codec; never consult mutable global defaults while generating an existing save.
2. **Offline tile pack and sampler:** preprocess geographic rasters into a documented tiled format. Include edge samples for continuous interpolation, no-data masks, land/water masks, physical elevation and source-quality flags. Verify checksums and bounds before world creation. Use a bounded, thread-safe read-only cache; no synchronous network calls from terrain generation or the server tick.
3. **Earth chunk data and biome selection:** populate TFC's `ChunkData` from geographic climate and land-cover data. Keep physical elevation separately from compressed block Y so alpine climate does not become sea-level climate. Correct for the elevation already represented in climate data rather than applying the lapse-rate adjustment twice. Implement an Earth `ClimateModel` selected through `SelectClimateModelEvent`, including southern-hemisphere seasons and geographic latitude for day-length behavior.
4. **Target-specific terrain adapters:** share data sampling and deterministic contracts, but implement separate adapters for Minecraft 26.1.2 and 26.3. The 26.3 terrain pipeline differs substantially from the older `fillFromNoise`/carver path. Both need matching spawn heights, heightmaps, base columns, surface placement, aquifers and chunk-status behavior.
5. **Natural survival layers:** preserve measured coastlines and major relief. Add bounded, documented procedural detail below source resolution, with reproducible seeds. Procedural caves, strata and ores remain an interpretation unless licensed geological data is added; do not label them measured deposits. Ensure fresh water, stones, sticks, food, clay and progression ores can be reached from supported survival starts.

A missing or corrupt tile must produce a clear data-pack error, not a fabricated procedural continent or zero-height sea. The bounded pilot prevalidates complete coverage and places its world boundary inside that coverage. Global mode later needs an asynchronous preparation workflow and a persistent, pinned fallback dataset, chosen before any affected chunks are generated. Dataset upgrades require an explicit new-world or migration procedure because changing unseen chunks creates seams against old terrain.

## Realism improvements worth pursuing

Keep improvements to normal procedural TFC worldgen separately configurable and versioned so existing seeds and saves do not change unexpectedly. Prioritize measurable behavior rather than adding expensive noise layers:

- Watershed-consistent rivers, connected confluences, lakes with defined outlets, and fresh/salt-water transitions. For Earth, reconcile source water masks and elevation before terrain placement; sea level is not every lake's surface elevation.
- Slope- and substrate-dependent soil thickness, talus at exposed cliffs, sediment deposition and floodplain material. Avoid running large erosion simulations during chunk generation; precompute regional fields or use bounded local calculations with shared edge data.
- Climate-driven vegetation and snow lines, with physical altitude and hemisphere-correct seasons. Use real climate for Earth; evaluate rain shadows and prevailing-wind models separately for procedural worlds.
- Surface-relative ore/rock access and survivable geographic spawn selection. A realistic large landscape must still support the intended progression without quietly moving the actual terrain.

## Milestones and acceptance gates

| Milestone | Deliverable | Gate |
| --- | --- | --- |
| P0 | Playable ordinary TFC 26.1.2 alpha; continuing 26.3 port | Both tracked honestly. For the playable branch: build, client boot, dedicated-server boot, new world, survival progression, save/reload and multiplayer smoke tests. |
| E0 | Coordinate/data contracts, pack manifest validator and small fixtures | Projection round trips, seam/no-data behavior, bounds and deterministic generation tests. Synthetic fixtures are labeled synthetic and are never advertised as Earth coverage. |
| E1 | Bounded real-region terrain pilot, initially developer-only | Source-license/metadata validation; sampled elevations and coastlines agree within the declared source/quantization tolerances; measured chunk throughput, memory and disk usage. |
| E2 | Optional playable TFC Earth region preset | Climate, water, soils, vegetation, spawn, geology/resources, structure exclusions, offline operation, multiplayer and save/reload gates pass. Only then expose the player-facing option. |
| E3 | Global coverage packs and additional region quality tiers | Poles, antimeridian, projection limits, missing-tile recovery, large-coordinate precision, bounded caches and cross-version parity validated. |
| E4 | Taller dimensions or near-uncompressed regional relief | Independent performance and compatibility evidence; no full-global uncompressed 1:1 promise. |

Required regression cases include adjacent chunks generated in opposite orders, parallel generation versus single-thread fixtures, chunk/tile boundary interpolation, negative coordinates, coastline cells, inland lakes above sea level, polar climates, the date line, duplicate/corrupt pack entries, interrupted installation, offline startup, world reload with identical hashes, explicit rejection of changed packs, and client/server climate agreement. Report both peak memory and chunk-generation latency on a defined machine; set budgets from measurements rather than inventing performance claims.

## Primary source register

Verified 2026-10-08; candidate dataset metadata must be checked again when a distributable pack is prepared.

- [GEBCO 2026 grid specification](https://www.gebco.net/data-products-gridded-bathymetry-data/gebco2026-grid) and [GEBCO terms](https://www.gebco.net/data-products/gridded-bathymetry/terms-of-use).
- [Copernicus DEM description, surface-model limits and required notices](https://dataspace.copernicus.eu/explore-data/data-collections/copernicus-contributing-missions/collections-description/COP-DEM).
- [USGS 3DEP one-metre bare-earth dataset metadata](https://data.usgs.gov/datacatalog/data/USGS%3A77ae0551-c61e-4979-aedd-d797abdcde0e) and [USGS product overview](https://www.usgs.gov/3d-elevation-program/about-3dep-products-services).
- [ESA WorldCover dataset access and licensing](https://esa-worldcover.org/en/data-access).
- [CHELSA climatologies 2.1 dataset, variables and current license](https://www.chelsa-climate.org/datasets/chelsa_climatologies).
- [WorldClim's redistribution restriction](https://www.worldclim.org/about.html) and [FABDEM provider license](https://data.bris.ac.uk/datasets/s5hqmjcdj8yo2ibzi9b4ew3sn/license.txt).
- [PROJ equidistant cylindrical projection](https://proj.org/en/stable/operations/projections/eqc.html) and [transverse Mercator](https://proj.org/en/stable/operations/projections/tmerc.html).
- Engine evidence: official clients selected through [Mojang's version manifest](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json), inspected using `javap -constants -c -p` for `WorldBorder`, `DimensionType` and `BlockPos`, plus bundled overworld JSON. Local SHA-1: 26.1.2 `4e618f09a0c649dde3fdf829df443ce0b8831e65`; 26.3 `e877b6a07acd633fb3bb475002175cec036e7b87`. No proprietary client binaries or decompiled source are added to this repository.
