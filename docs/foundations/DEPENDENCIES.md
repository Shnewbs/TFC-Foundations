# 26.3 dependency checkpoint

Observed 2026-10-08. Candidate version: 0.0.0. Source baseline: e9d9a88a187d5a33064e2d86b2803f54238cadd6.

## Toolchain

| Component | Pin | Evidence |
| --- | --- | --- |
| Minecraft | 26.3 | Official Mojang version manifest |
| NeoForge | 26.3.0.58-beta | Official NeoForge Maven metadata |
| Java | 25 | Official NeoForge 26.3 MDK |
| ModDevGradle | 2.0.148 | Official NeoForge 26.3 MDK |
| Gradle | 9.2.1 | Official NeoForge 26.3 MDK; distribution SHA-256 verified |
| Foojay resolver | 1.0.0 | Official NeoForge 26.3 MDK |
| JEI | 31.9.0.61 | Published metadata for jei-26.3-neoforge; adapter not ported/tested |

## Integration blockers

The existing source directly imports all of these integrations. Removing an old runtime jar alone does not port the adapter. Compilation remains useful for diagnosing source API differences; JAR packaging also checks for missing integration version pins.

- Patchouli: no 26.3 version found in published Maven metadata. TFC hard-depends on its API and internal client classes. Preserve guide data; move guide access behind an optional adapter or port a compatible backend before a playable candidate. Do not replace gameplay with no-op implementations.
- EMI: no 26.3 artifact verified. Existing repository metadata has no 26.x artifact. Recheck the maintained repository before selecting a future version; isolate the old adapter if unavailable.
- The One Probe: published metadata contains 26.1.2 and 26.2 entries, but no 26.3 entry. Do not assume binary compatibility.
- Jade: the inherited CurseForge file targets the old baseline. No target artifact selected yet.
- ModernFix: removed the inherited 1.21.1 runtime development dependency; no performance claims made.
- Parchment: removed the 1.21.1 mappings overlay, following the 26.3 MDK.

`patchouliVersion`, `emiVersion`, `jadeVersion`, and `theOneProbeVersion` are deliberately unset. Select verified target artifacts or isolate the old adapters before packaging. Setting arbitrary values does not establish compatibility.

## Sources

- https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle/blob/main/build.gradle
- https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle/blob/main/gradle/wrapper/gradle-wrapper.properties
- https://piston-meta.mojang.com/mc/game/version_manifest_v2.json
- https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml
- https://maven.blamejared.com/mezz/jei/jei-26.3-neoforge/maven-metadata.xml
- https://maven.blamejared.com/vazkii/patchouli/Patchouli/maven-metadata.xml
- https://maven.terraformersmc.com/releases/dev/emi/emi-neoforge/maven-metadata.xml
- https://maven.k-4u.nl/mcjty/theoneprobe/theoneprobe/maven-metadata.xml
