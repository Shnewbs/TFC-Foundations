# Upstream tracking

Upstream: https://github.com/TerraFirmaCraft/TerraFirmaCraft

Observed 2026-10-08. Rediscover branches each scan.

| Branch | Observed tip |
| --- | --- |
| 1.12.x | 23e6a438e5627da1203e0b199e1556db8ed5234f |
| 1.16.x | 1280317be9164bbf370312e34f0ede7e676973b0 |
| 1.17.x | afddbb2f477b0a7c6dee2f3f98e9fc81851f1f9e |
| 1.18.x | caf1ec2f527d79d15bda64871014b690b6484a59 |
| 1.20.x | e818b1f27493e87e3be6db1172b0392e0752eedb |
| 1.21.x | e9d9a88a187d5a33064e2d86b2803f54238cadd6 |

These are observation cursors, not claims of integrated historical fixes. Daily upstream/version monitoring is active.

Process:

1. Compare branch tips with cursors; inspect commits, merged PRs, new/updated/reopened issues and releases. Detect new/deleted branches and rewritten history.
2. Track by upstream SHA and issue/PR, with versions, reproducer, severity and relevance. Open PRs are proposals; closed issues do not alone establish merged fixes.
3. Use explicit states: observed, investigate, already inherited, not applicable, port planned, implemented, verified. Separate observation cursors from integration cursors.
4. Prioritize duplication/crash/data loss. Port the smallest applicable change, preserving upstream SHA/attribution; do not blindly merge legacy branches.
5. Record downstream commit, regression results and containing release. Update the work log.

## Initial triage sample

| Reference | Finding | Status / follow-up |
| --- | --- | --- |
| e9d9a88a1; #3719/#3737 | Enclosed-atoll surface water | Inherited in source; test fresh atoll worlds after port |
| 6e77733cb; #3688 | Waterlogged-block bucket duplication | Inherited; test pickup/placement and counts |
| 6e77733cb; #3713 | Pot/crucible capability fill sync | Inherited; test automation and two clients |
| 6e77733cb; #3391 | Animal growth crash/future birthdays | Inherited; test calendar changes and entity loading |
| bcbaa8bb3; #3700 | Falling blocks and boats | Inherited; collision/block recovery checks |
| 1.20.x e818b1f27493; PR #3742 | Tall crop support | Legacy candidate; inspect applicability |
| #3743; open PRs #3744/#3745 | Farmland tooltip at/below 0 C | Unmerged; evaluate overlapping proposals |
| #3293 | Kiln heating behavior | Open; reproduce |
| #3721 | Flattened stratovolcano | Open; collect seed/settings |
| #3747 | Rosewood on ocean floor in volcanic arc | Open; seed-based test candidate |
| Open PR #3746 | Underground bushes | Review branch, intent and merge state |
| Open PR #3741 | 1.20.1 berry bushes | Review applicability |

Links: `https://github.com/TerraFirmaCraft/TerraFirmaCraft/issues/NUMBER` or `/pull/NUMBER`. This sample is not a complete historical bug audit.

Version baseline: Mojang release 26.3, snapshot 26.4-snapshot-3; latest observed NeoForge 26.3.0.58-beta, no 26.4 entry. Recheck official manifest/Maven metadata before changing build pins.
