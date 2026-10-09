# TFC Foundations — Current Project Status

Updated 2026-10-09 UTC (October 8 in America/Los_Angeles).

**Version 0.0.0 remains an incomplete source port. Full compilation, client/server startup and survival testing have not passed. No playable JAR or release was produced by this checkpoint.**

## Direction and targets

Continue public development in `Shnewbs/TFC-Foundations`, preserving Git history, EUPL notices, credits and `tfc` resource IDs. First-playable priority: Minecraft 26.1.2, NeoForge 26.1.2.114, JEI 29.43.0.107 and Patchouli 26.1-94. Forward branch `26.x` remains the separately verified Minecraft 26.3 track toward 26.4; it was unchanged in this continuation. Both tracks use Java 25 and Gradle 9.2.1. Do not copy target-specific migrations blindly or remove core systems to obtain compilation.

## Hierarchical model/render-state checkpoint

Public source **`bcbdf950ff4280199adcfeaa15122cf6bf85e186`** applies a reviewed **68-file checkpoint**, following staging `80fd233bc22be98e8ce52cc73c0ab6337f464494`. It adopts native EntityModel/render-state and keyframe APIs for the shared hierarchical animal/fish/critter path and associated renderers. It does not finish all entity models or renderers.

Model setup consumes a detached TFCAnimalRenderState instead of reading live entities, brains, calendars or levels. Extraction captures conditional state, texture selection and independently copied animation timelines; custom state is reset between captures. Baked keyframes are cached per definition identity and model instance, never shared between model roots. No measured performance improvement is claimed.

Existing geometry and declared keyframes are protected by 48 source contracts. The dog intentionally adopts the verified native adult-wolf mesh factory while retaining the adult/collar texture layout. Dog sitting/standing/head-roll behavior and camel baby transform order are covered by actual model tests. Headless testing also caught strict native binding failures where old shared animations referenced absent bones. The compatibility path permits only explicitly reviewed model/bone omissions; unexpected names and wholly incompatible animations still fail. It preserves legacy omissions rather than inventing geometry or silently accepting new mistakes.

## Independent validation

[GitHub run 37889572978](https://github.com/Shnewbs/TFC-Foundations/actions/runs/37889572978), source `bcbdf950`, completed with overall **failure** because main compilation is still broken.

| Check | Observed result |
| --- | --- |
| Full Gradle/main compilation | FAIL: exit 1, 2,361 javac errors; summary groups them into 2,250 path/line/message entries. |
| New headless production-model tests | PASS: 48 models, 384 model/scenario combinations, 1,720 assertions, 152,544 finite CPU vertices checked. |
| Model preservation checks | PASS: 48 geometry/keyframe contracts, explicit missing-bone rules and two native texture presence/layout checks. |
| Existing standalone suites | PASS: 173 Java checks, six Python guards and the prior save/brain contracts. |
| Separate production compilation | PASS: migrated models/state, prior particle/base/save utilities and all 114 package-info files. This is not full-mod compilation. |
| Main/data/test license tasks | PASS. |
| Existing resource-validation step | PASS; it does not establish in-game visual correctness. |
| Published source preservation | PASS: all 68 reviewed postimages match the downloaded CI source; temporary transport files are absent. |

Both the edited local source and a clean Git patch replay reproduce **2,361 errors** across 1,635 main sources, exit 1, without timeout. This diagnostic command uses `-proc:none`, not the complete Gradle/mixin pipeline. Compared with the previous completed 2,583 pass, there are **222 fewer diagnostics**, not necessarily 222 individual fixes or a completion percentage. No diagnostics appear in the migrated model/state/renderer files; the minimally adjusted ClientEventHandler still has unrelated errors.

The headless suite constructs real production models, applies poses and emits native CPU geometry. It does **not** execute live entity extraction, GPU drawing, a game client or gameplay. Client/server launch, actual appearance, world save/reload, multiplayer, survival progression, registry-backed round trips and performance measurements remain untested. Downstream complete data/test compilation and packaging remain blocked.

Artifact **11598051560**, `tfc-port-diagnostics-37889572978-1`, retains logs, reports and exact source until October 16, 2026 UTC. Downloaded SHA-256 **615759954bed4906c6ac5d5a8d7fbf615380ffc5616f2464e30dca512ab4cec3** was verified. The source patch passes a clean Git application and reproduces all reviewed files.

## Next compiler/playability gates

1. Continue the remaining livestock/native entity models and renderers, block/item/block-entity rendering, ClientEventHandler/overlays and JEI/Patchouli integration.
2. Finish remaining recipes, equipment/materials, capabilities, TFCBrain schedules, registry/holder and world-generation APIs; then pass complete main/data/test compilation, licenses, generated resources and packaging.
3. Run genuine client/dedicated-server launch, new-world generation, save/reload, reconnect, calendar, inventory/fluid, guide/recipe and survival-progression checks before calling an artifact playable.
4. Independently verify shared work on 26.3. No 26.3 or Earth code changed in this checkpoint; release gates remain intact.

## Earth and history

Normal TFC generation remains the default. The optional natural Earth request remains nominally one block per horizontal meter without generated manmade structures. Existing coordinate/elevation groundwork is not an implemented Earth preset, licensed dataset pipeline or complete chunk generator. Projection, height, geology/climate, caching and performance requirements remain open.

See [WORK_LOG](docs/foundations/WORK_LOG.md) for commits, commands and validation limits. The complete preceding status and log are preserved byte-for-byte in [STATUS_BEFORE_MODEL_RENDER_STATE](docs/foundations/history/STATUS_BEFORE_MODEL_RENDER_STATE.md) and [WORK_LOG_BEFORE_MODEL_RENDER_STATE](docs/foundations/history/WORK_LOG_BEFORE_MODEL_RENDER_STATE.md).
