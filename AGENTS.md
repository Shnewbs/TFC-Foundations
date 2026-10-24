# Repository work rules

- Keep `26.1.2` as the playable integration target and `26.x` as the independent 26.3/future 26.4 integration target.
- Develop each system on `feature/<target>/<system>`, as listed in `docs/foundations/FEATURE_BRANCHES.json`. Every branch contains the complete mod and starts from its matching integration branch. The user's 2026-10-24 instruction replaces the former no-feature-branches policy.
- Feature changes must build and merge independently against that integration branch. Do not stack feature branches or depend on unmerged sibling changes. Land shared prerequisites in the integration branch first, then refresh affected features. Shared files are coordinated, not copied into separate implementations.
- Preserve all gameplay, resource and registry IDs, saves, upstream credits, and EUPL notices. Do not stub out or exclude gameplay systems to obtain a passing build.
- Use the pinned libraries and a real Java 25 compiler. Read `docs/foundations/CONTINUE_BUILDING.md` and record exact validation results in the project status and work log.
- Publish scoped work on its feature branch; integrate verified work into the matching version branch. Actions quota is suspended; use `[skip ci]` until the user restores it. Run the branch-history guard plus the appropriate full build/runtime gates before integration; a history check alone does not prove source independence or playability.
- Read `docs/foundations/BRANCH_POLICY.md` for the current workflow and the historical consolidation audit. Do not rewrite existing integration history or strip other systems from feature branches.
