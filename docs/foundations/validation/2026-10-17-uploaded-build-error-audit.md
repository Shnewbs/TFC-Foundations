# Uploaded Windows build report — 2026-10-17 UTC

The supplied `Pasted text.txt` records **44 main Java compiler errors** from the
earlier published item-model checkpoint `7b5715acd23da8b97ed012c1b66d666e50c0de96`.
The accompanying Gradle HTML groups and truncates repeated diagnostics; its
problem count is not the complete javac error count. Deprecation warnings are
separate from the compilation failures.

Diagnostic compilation of this repaired source succeeds: **1,699 sources, zero errors,
exit 0, no timeout**. This does not certify startup or playability. This checkpoint includes the fixes; source ZIPs downloaded before it do not.

| Status | Reported failures | Repair |
| --- | --- | --- |
| Fixed | 36 missing `Capabilities.FluidHandler` references | Native `Capabilities.Fluid` registration and item-access lookups, with transaction-aware adapters retaining existing TFC storage rules. |
| Fixed | 3 missing `Capabilities.ItemHandler` references | Native `Capabilities.Item` registration and lookups. |
| Fixed | 2 removed block callback overrides | Placed-item and aqueduct state-removal behavior is bridged through the current chunk removal lifecycle. |
| Fixed | 1 removed recipe result accessor | Mutable TFC results retain their food hook; native recipe display results receive non-decaying copies through the slot-display hook. |
| Fixed | 1 removed default-component mutator | Rebind the native item holder's component map and invalidate its cached default resource. |
| Fixed | 1 removed wolf howl event | Register and use the retained wolf howl sound resource through TFC's sound registry. |

The JSON audit beside this document enumerates every reported source path and
original line number and records both uploaded-file hashes. Original line
numbers refer to the uploaded source, not the repaired files.

Native data generation additionally exposed obsolete recipe conditions:
`neoforge:false` is retired in the pinned NeoForge version, whose registered
equivalent is `neoforge:never`. The generated files have been regenerated and
the real recipe decoding suite rerun, retaining TFC's disabled vanilla recipes.

Final validation: the pinned full Gradle build succeeds (47 seconds), native
data generation succeeds (29 seconds), and all 36 API probe runners pass.
The updated JSON audit records the JUnit counts from that full build.

Still required: client/server startup,
world generation, resource reload, save/reload,
multiplayer and core survival progression. The delivered development JAR is
not a verified playable release.
