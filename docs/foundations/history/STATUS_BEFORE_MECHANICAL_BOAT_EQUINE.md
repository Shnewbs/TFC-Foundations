# TFC Foundations — Current Project Status

Updated 2026-10-09 UTC and America/Los_Angeles.

**Version 0.0.0 remains an incomplete source port. Full compilation, client/server startup and survival testing have not passed. No playable JAR or release was produced by this checkpoint.**

## Direction and targets

Continue public development in `Shnewbs/TFC-Foundations`, preserving Git history, EUPL notices, credits and `tfc` identifiers. First-playable priority: Minecraft 26.1.2, NeoForge 26.1.2.114, JEI 29.43.0.107 and Patchouli 26.1-94. Both tracks use Java 25 and Gradle 9.2.1. The separate `26.x` branch remains the Minecraft 26.3 forward track toward a verified 26.4 port; no changes were made to it here. Core gameplay, visuals, required guide and publication gates remain enabled.

## Livestock, cat and native-renderer checkpoint

Public source **`354bb9ec206f612489172cf2100b69b30f6d14d4`** follows staging `124c68f7bd109271c08fb3c094773be45f7d3c6e`. Apply run `37926602783` verified all preimages/postimages for exactly **35 files**, removed the one-time transport and used a normal push. Target/dependency pins were unchanged.

Ten livestock model implementations and the cat/collar path now consume detached render state. Juvenile transforms preserve the reviewed head/body scaling order; sex, horn, udder, wool and rooster visibility remain implemented. Bird wing phase is captured separately from native animation age. Genetic size keeps the existing clamped multiplier. Native pose resets prevent baby/visibility/water state leaking into subsequent poses.

Eight custom livestock geometry factories remain byte-identical. Pig/goat/native feline factories use their exact target APIs; the cat intentionally retains an adult-layout texture with custom juvenile proportions and native feline sitting/sleeping animation. The deformed collar uses the same pose state. This is not a pixel-identical in-game appearance claim.

Four native fish renderer adapters preserve the field-guide origin exemption through captured state based on the actual entity position, not interpolated render coordinates. Squid/glowing-squid adapters retain their adult-layout textures; glow-arrow rendering uses the target arrow state. Live extraction and renderer construction remain untested.

## Validation

[GitHub validation 37926650392](https://github.com/Shnewbs/TFC-Foundations/actions/runs/37926650392), source `354bb9ec`, completed with overall **failure** because main compilation is still broken.

| Check | Observed result |
| --- | --- |
| Full Gradle/main compilation | FAIL: exit 1, 2,193 javac errors; summary groups them into 2,095 path/line/message entries. |
| New headless model suite | PASS: 11 models, 336 scenarios, 3,157 assertions, 103,680 finite CPU vertices. |
| Existing headless model suite | PASS: 48 models, 384 scenarios, 1,724 assertions, 152,544 CPU vertices. |
| Earlier standalone suites and source/API contracts | PASS. |
| Main/data/test license tasks | PASS. |
| Existing resource-validation step | PASS; not in-game visual validation. |
| Downloaded CI source preservation | PASS: all 35 reviewed postimages match; test invocation is tracked and temporary transfer files are absent. |

Artifact **11614218054**, `tfc-port-diagnostics-37926650392-1`, preserves logs, reports and the exact source until October 16, 2026 UTC. Downloaded ZIP SHA-256 **81ce5b3255179136d3e26b677746dc308c3f4a1469f1bae2ce95993661cca6e8** was verified.

The edited local source and a clean Git patch replay each complete the diagnostic main pass with **1,640 source files, exit 1, 2,193 errors and no timeout**. This uses `-proc:none`, not the full Gradle/mixin pipeline. Compared with the previous completed 2,361 pass, there are **168 fewer diagnostics**, not necessarily 168 individual fixes or a completion percentage. Changed model/renderer/state paths have no diagnostics in this traversal except the minimally adjusted, still-incomplete ClientEventHandler.

The new headless suite exercises **11 real production models across 336 scenarios**, with **3,157 assertions and 103,680 finite CPU vertices**. The existing 48-model suite still passes: 384 scenarios, 1,724 assertions and 152,544 CPU vertices. Across both suites that is 59 concrete models and 720 model/scenario combinations; the new deformed cat collar is additional geometry, not counted as another animal model.

Tests cover baby/adult pose reuse, visibility, geometric transform order, independent roots, cat/collar alignment, wing timing, genetic size and guide-origin state reuse. Eight geometry source contracts, four native texture layouts and separate compilation of four actual native fish renderers also pass locally. Existing 173 standalone Java checks, six Python guards, save/brain contracts and 114 package-info compilations remain passing. A fresh complete local `inspect_api.py` run records exit 0; the clean replay records probe exit 0 and diagnostic compiler exit 1. No game or TFC stubs were introduced.

NOT RUN: live TFC entity extraction, renderer construction, GPU drawing, actual appearance, client/server startup, world creation/save/reload, multiplayer, survival progression and performance measurements. Earlier registry-backed round trips and bootstrap-dependent probes also remain unexecuted. Headless model tests do not establish playability or replace downstream full data/test compilation and packaging.

## Next compiler/playability gates

1. Complete horse/chest and remaining native/mechanical models, boats, block/item/block-entity rendering, ClientEventHandler/overlays and JEI/Patchouli adapters.
2. Finish recipes, equipment/materials, capabilities, TFCBrain schedules, registry/holder and generation interfaces; pass full main/data/test compilation, licenses, resource generation and packaging.
3. Run genuine client/dedicated-server startup, new-world generation, save/reload/reconnect, calendar, inventory/fluid, guide/recipe and survival tests before calling an artifact playable.
4. Verify shared work separately on 26.3; do not blindly copy migrations across its divergent loot/provider/holder/worldgen APIs.

## Earth and history

Normal TFC generation remains the default. The optional natural Earth request remains nominally one block per horizontal meter without generated manmade structures. Coordinate/elevation groundwork is not a selectable Earth preset, licensed dataset pipeline or complete chunk generator. Projection, height, geology/climate, caching and performance requirements remain open. No Earth code changed here.

See [WORK_LOG](docs/foundations/WORK_LOG.md) for exact commands, commits and evidence. The complete preceding status and log are retained byte-for-byte in [STATUS_BEFORE_LIVESTOCK_RENDER_STATE](docs/foundations/history/STATUS_BEFORE_LIVESTOCK_RENDER_STATE.md) and [WORK_LOG_BEFORE_LIVESTOCK_RENDER_STATE](docs/foundations/history/WORK_LOG_BEFORE_LIVESTOCK_RENDER_STATE.md).
