# Port milestones and release gates

Pending unless PROJECT_STATUS.md explicitly records completion. Prioritize a playable 26.1.2 alpha while maintaining a separate 26.3 foundation for released 26.4 with a usable matching toolchain. Never broaden version ranges to claim untested compatibility.

| Milestone | Deliverable | Exit evidence |
| --- | --- | --- |
| 0 — Baseline | Public fork, preserved history, safe publishing, audit and reproducible tests | Public source publication authorized, upstream SHA recorded, baseline build/data validation results |
| 1 — Target compilation | Verified toolchain; registration, components, recipes, networking, worldgen, entities and rendering ported | Clean build/test/resource reports, no no-op gameplay substitutes |
| 2 — Playable alpha | Core survival on client and dedicated server, usable guide | New world, save/restart/reconnect, stone tools, firepit, pottery, copper/bronze, bloomery/iron, food/thirst, seasons, crops and animals tested |
| 3 — Extensions | Public API and datapack examples; scripting adapters where target releases exist | Same operations verified through each supported route, reload errors and multiplayer sync tested |
| 4 — Building/art | Optional Conquest compatibility and coherent material palette | Permissions/versions recorded, tool/recipe/support balance checked, dense-build profiling |
| 5 — Release | Verified candidate with matching source | GitHub and Foundations CurseForge artifacts/checksums match; gates below pass |
| 6 — 26.4 | Port from a known good checkpoint | Build/runtime/performance suite, migration notes and dependency verification |

## Version policy

Both tracks start at `0.0.0`: `TFC-Foundations-26.1.2-0.0.0.jar` and `TFC-Foundations-26.3-0.0.0.jar`.
This is a candidate identifier, not evidence of a working port or a published release.
Use exact Minecraft compatibility until each later target is validated.

Candidate gates:

- Existing tests adapted and passing; no disabled tests to manufacture success. Datagen/resource validation and example loading pass.
- Dedicated server starts without client-class loading; two clients join and synchronize correctly.
- Fresh worlds and save/load work. Older TFC world migration remains unsupported until tested on backups.
- Check duplication, inventory persistence, fluid/container sync, food decay, paused calendar, animals and terrain regressions.
- Optional dependencies can be absent; supported combinations have smoke-test evidence.
- Benchmarks include reproducible methodology and before/after results; optimization claims have evidence.
- Matching source, notices and modification records accompany distribution. No unlicensed Conquest bundles.
- Confirm Foundations CurseForge project ID and scoped credentials; use CURSEFORGE_API_TOKEN if configured. Never upstream project IDs. No credentials in files/logs.
- Explicit version/channel/MC/loader metadata. First incomplete playable builds are alpha. Document missing integrations and unsupported saves.

Compilation alone does not authorize release publication: runtime and distribution gates must also pass.

## Continue in another environment

The cross-platform, cross-session [developer handoff](CONTINUE_BUILDING.md) includes exact branch pins, commands for PowerShell and Unix shells, current verified CI evidence, the safe source-publication procedure, known gameplay/resource blockers, and the playable-JAR acceptance checklist. **Read GitHub's live HEAD and current CI before relying on any dated checkpoint.** Update `PROJECT_STATUS.md` and `WORK_LOG.md` after each substantive source checkpoint so every environment has the same source of truth.
