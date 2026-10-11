# 26.1.2 playable port — 50 gated engineering phases

These are **pending work items**, not fifty completed phases. The current branch remains an incomplete source port. Each phase needs a code diff, test evidence, and a recorded exit status before being marked complete. Never manufacture a green build by disabling gameplay or integrations.

| # | Area | Implementation and exit requirement | Status |
| --- | --- | --- | --- |
| 1 | Build | Capture branch SHA and dependency pins — record target-specific implementation and validation | Pending |
| 2 | Build | Verify Java 25 availability — record target-specific implementation and validation | Pending |
| 3 | Build | Resolve exact NeoForge classpath — record target-specific implementation and validation | Pending |
| 4 | Build | Capture uncompromised Gradle failure — record target-specific implementation and validation | Pending |
| 5 | Build | Rank first compiler-error clusters — record target-specific implementation and validation | Pending |
| 6 | Core API | Port registry holder signatures — record target-specific implementation and validation | Pending |
| 7 | Core API | Port deferred block registration — record target-specific implementation and validation | Pending |
| 8 | Core API | Port deferred item registration — record target-specific implementation and validation | Pending |
| 9 | Core API | Port component serialization — record target-specific implementation and validation | Pending |
| 10 | Core API | Port event bus registrations — record target-specific implementation and validation | Pending |
| 11 | Gameplay API | Port recipe ingredient access — record target-specific implementation and validation | Pending |
| 12 | Gameplay API | Port recipe serializer interfaces — record target-specific implementation and validation | Pending |
| 13 | Gameplay API | Port tool material/attributes — record target-specific implementation and validation | Pending |
| 14 | Gameplay API | Port fluid capability interfaces — record target-specific implementation and validation | Pending |
| 15 | Gameplay API | Port block entity save and load — record target-specific implementation and validation | Pending |
| 16 | Worldgen | Port biome lookup and registry — record target-specific implementation and validation | Pending |
| 17 | Worldgen | Port density/noise APIs — record target-specific implementation and validation | Pending |
| 18 | Worldgen | Port surface rules and strata — record target-specific implementation and validation | Pending |
| 19 | Worldgen | Port climate and rain queries — record target-specific implementation and validation | Pending |
| 20 | Worldgen | Verify seeded terrain determinism — record target-specific implementation and validation | Pending |
| 21 | Rendering | Port contained fluid model — record target-specific implementation and validation | Pending |
| 22 | Rendering | Port trimmed item model — record target-specific implementation and validation | Pending |
| 23 | Rendering | Port custom item selectors — record target-specific implementation and validation | Pending |
| 24 | Rendering | Port block entity render registration — record target-specific implementation and validation | Pending |
| 25 | Rendering | Port client overlays — record target-specific implementation and validation | Pending |
| 26 | Entities | Port entity data components — record target-specific implementation and validation | Pending |
| 27 | Entities | Port spawn registration — record target-specific implementation and validation | Pending |
| 28 | Entities | Port animal goal navigation — record target-specific implementation and validation | Pending |
| 29 | Entities | Port entity render layers — record target-specific implementation and validation | Pending |
| 30 | Entities | Port sound event holders — record target-specific implementation and validation | Pending |
| 31 | Integrations | Port JEI ingredient registration — record target-specific implementation and validation | Pending |
| 32 | Integrations | Port JEI recipe categories — record target-specific implementation and validation | Pending |
| 33 | Integrations | Port Patchouli page and book assets — record target-specific implementation and validation | Pending |
| 34 | Integrations | Verify optional addon isolation — record target-specific implementation and validation | Pending |
| 35 | Integrations | Check client/server classloading boundaries — record target-specific implementation and validation | Pending |
| 36 | Compile | Achieve zero main-source javac errors — record target-specific implementation and validation | Pending |
| 37 | Compile | Achieve zero data-source javac errors — record target-specific implementation and validation | Pending |
| 38 | Compile | Achieve zero test-source javac errors — record target-specific implementation and validation | Pending |
| 39 | Compile | Pass tests, license and resource checks — record target-specific implementation and validation | Pending |
| 40 | Compile | Produce a verified loadable JAR — record target-specific implementation and validation | Pending |
| 41 | Runtime | Launch NeoForge client with TFC — record target-specific implementation and validation | Pending |
| 42 | Runtime | Launch dedicated NeoForge server — record target-specific implementation and validation | Pending |
| 43 | Runtime | Create TFC world and cross chunk boundaries — record target-specific implementation and validation | Pending |
| 44 | Runtime | Save/reload and reconnect — record target-specific implementation and validation | Pending |
| 45 | Runtime | Verify resources, textures and UI — record target-specific implementation and validation | Pending |
| 46 | Survival | Stone tools, firepit and pottery — record target-specific implementation and validation | Pending |
| 47 | Survival | Copper, bronze, bloomery and iron — record target-specific implementation and validation | Pending |
| 48 | Survival | Food, thirst, crops and seasons — record target-specific implementation and validation | Pending |
| 49 | Survival | Animals, fluids and persistence — record target-specific implementation and validation | Pending |
| 50 | Release | Package matching source/JAR/checksums — record target-specific implementation and validation | Pending |

## Execution constraints

- First playability target: Minecraft 26.1.2 / NeoForge 26.1.2.114 / Java 25. Continue 26.3 separately on `26.x`.
- Phase 36 requires a real successful complete main compilation; a diagnostic report with zero entries but failed process exit is not a pass.
- Phases 37–40 require Gradle success and a physically produced JAR; phases 41–49 require gameplay/runtime evidence.
- A dependency failure, timeout, or missing compiler is a blocker, never zero errors.
- Record links to each relevant commit, CI run, and source artifact in PROJECT_STATUS.md.
