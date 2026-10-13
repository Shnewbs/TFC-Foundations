# Repository work rules

- Work directly on the existing `26.1.2` branch for the playable target and `26.x` for the independent 26.3/future 26.4 track.
- Do not create task, repair, validation, transport, or progress branches. A new Minecraft version target is the only permitted reason to create a branch.
- Preserve all gameplay, resource and registry IDs, saves, upstream credits, and EUPL notices. Do not stub out or exclude gameplay systems to obtain a passing build.
- Use the pinned libraries and a real Java 25 compiler. Read `docs/foundations/CONTINUE_BUILDING.md` and record exact validation results in the project status and work log.
- Publish reviewed fixes directly to the correct existing public branch. Actions quota is suspended; use `[skip ci]` until the user restores it.
- Read `docs/foundations/BRANCH_POLICY.md` for the completed branch consolidation audit.
