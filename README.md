# TFC Foundations

A public development fork of [TerraFirmaCraft](https://github.com/TerraFirmaCraft/TerraFirmaCraft), targeting Minecraft **26.1.2** and **26.3** on NeoForge.

**Current candidate: 0.0.0 — port in progress. No playable build is available yet.** The branch contains upstream gameplay code that still needs porting; changing the build target does not establish runtime compatibility.

## Development goals

- Prioritize a playable 26.1.2 build on branch `26.1.2`; develop 26.3 on `26.x` as the base for a later 26.4 port.
- Preserve TFC survival progression, climate, geology, food and agriculture.
- Prioritize correctness and measured performance.
- Extend datapack and addon APIs, with optional KubeJS/CraftTweaker adapters when compatible dependencies are available.
- Support optional Conquest-compatible presentation without bundling unlicensed assets.
- Track fixes across upstream branches; move to released 26.4 once Minecraft and a usable matching NeoForge toolchain are available.

These are development goals, not completed features.

## Build status

Both branches use Java 25, Gradle 9.2.1 and ModDevGradle 2.0.148. Exact Minecraft, NeoForge and integration pins are in each branch’s `gradle.properties`. GitHub Actions compiles each branch independently. The 26.3 build reaches Java compilation but still fails on API changes; neither branch has passed runtime testing.

```sh
./gradlew build --no-daemon
```

On Windows, use `gradlew.bat build --no-daemon`. A fresh build downloads its required toolchain and dependencies. Do not supply old 1.21.1 integration jars to satisfy the target-version dependency checks.

## Project records

- [Current status and next actions](PROJECT_STATUS.md)
- [Roadmap and release gates](docs/foundations/ROADMAP.md)
- [Resume building on another computer or agent](docs/foundations/CONTINUE_BUILDING.md)
- [Dependency checkpoint](docs/foundations/DEPENDENCIES.md)
- [Source audit](docs/foundations/AUDIT.md)
- [Upstream tracking](docs/foundations/UPSTREAM.md)
- [Work log](docs/foundations/WORK_LOG.md)

Source checkpoints are published separately on `26.1.2` and `26.x`, with `26.1.2` prioritized for a playable alpha. Playable releases require successful compilation, tests and client/dedicated-server survival validation. Publishing to upstream distribution destinations is disabled.

Upstream's [Field Guide](https://terrafirmacraft.github.io/Field-Guide/en_us/) and [API documentation](https://terrafirmacraft.github.io/Documentation/) describe the original project and are reference material, not guarantees of compatibility with this port.

### Legal

Licensed under the [EUPL, Version 1.2](LICENSE.txt) unless otherwise noted.

- `FastNoiseLite.java` is licensed under the [MIT License](https://github.com/Auburn/FastNoiseLite/blob/72d212e005e62c886c06f55f740571116f361571/LICENSE) and is modified from [FastNoiseLite](https://github.com/Auburn/FastNoiseLite)
- `resources/bsc5p_radec_min.json` is a catalog and is used under the [CC-BY 4.0](https://creativecommons.org/licenses/by/4.0/deed.en) unmodified from [BSC5P-JSON-XYZ](https://github.com/frostoven/BSC5P-JSON-XYZ)
- Sounds are used under [CC0](https://creativecommons.org/publicdomain/zero/1.0/) unless otherwise noted.
- `rock_slide_long_3` is licensed under [CC-BY-4.0](https://creativecommons.org/licenses/by/4.0/), and has been modified from the [original work](https://freesound.org/people/Benboncan/sounds/60085/).

### Acknowledgments

Based on original work by Robert "Bioxx" Anthony, Amanda "Kittychanley" Halek and others.

Music by Mike "Menoch" Pelaez

Parts of this project are edited source code from the original TerraFirmaCraft for 1.7.10 mod. They are used under a different license with permission from the original author (Bioxx).

Thanks to [AlcatrazEscapee](https://github.com/alcatrazEscapee) for invaluable contributions, project guidance and leadership from the Minecraft 1.12 era in 2018, all the way until 1.20 in 2025.
