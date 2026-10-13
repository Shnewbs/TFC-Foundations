# Branch cleanup — 2026-10-13 UTC

Requested policy: develop directly on `26.1.2` and `26.x`. Do not create task, repair, validation, or transport branches. A new Minecraft target is the only reason to add a long-lived branch.

Audited source head: `c1d13325d61a501f639f8752eab5103bc38ab7f5`. Sixteen `port/26.1.2-*` refs are redundant. Ten tips are ancestors of the source head. The remaining six refs share five unique commits whose changed files were compared with the current source. Their bush codecs, height bounds, item-tag holder enumeration, fluid tags, iron chain state, recipe keys, ash color and sloping rail placement are retained. Later camera, command permission, pathfinding, identifier and shared rail helper migrations are retained as well.

The consolidation commit preserves the three divergent tip histories (`c02eb940adb93ba39ed7804be3b57c899f63e915`, `d37330e2f51722c2d6b24754bbf2eb4cd38494e5`, and `3417ccc3657d99eb6c5e496c3c1ae0aafd5863e4`) as additional parents. No gameplay source is reverted or altered by consolidation. All sixteen old branch tips are then reachable from `26.1.2`; deleting the names does not discard their commits.

Only `26.1.2` and `26.x` should remain after cleanup. No open pull requests were present at the audit. Cleanup is not a compiler or runtime validation.
