# Foundations Cave-Ins

Standalone **Minecraft 26.1.2 / NeoForge 26.1.2.114** alpha. Put only this JAR in
`mods/`; TFC, Patchouli, JEI, a weight mod, and other Foundations systems are not
required. This artifact is not for Minecraft 1.21.1 or 26.3.

Mining stone/ores can trigger unsupported roofs to fall. Vanilla logs/fences
act as supports. Explosions also trigger checks. Falling blocks retain their
material/state, use native falling-entity rendering and land through vanilla
rules. Block entities and unbreakable/immune blocks cannot collapse. Work is
bounded per level/tick and pending collapse queues are saved with the level.
Unloaded support regions conservatively prevent a fall; probes never force-load
chunks. No TFC world type, food, terrain or player-survival replacement is installed.

## Pack configuration

Server settings are in `world/serverconfig/foundations_caveins-server.toml`.
Configure mining chance, creative behavior, explosions, radius, propagation
height, support ranges, falling damage and per-tick/queue limits. The read budget
covers explicit probes/revalidation, not vanilla neighbor-update callback work.
Reducing queue limits can discard excess pending work on restore.

Datapack block tags live under `data/foundations_caveins/tags/block/`:

| Tag | Purpose |
| --- | --- |
| `collapsible` | Materials eligible to fall; defaults include ordinary stone. |
| `triggers_collapse` | Mined/exploded blocks that initiate a search; includes `#foundations_caveins:collapsible` and vanilla ore families. |
| `supports` | Blocks providing structural support; defaults include logs and fences. |
| `immune` | Blocks which never collapse. Block entities and unbreakable blocks are also excluded in code. |
| `heavy_blocks` / `light_blocks` | Optional impact-damage multipliers (2× / 0.5×). Heavy wins if both match. |

For example, add another mod's rock in your datapack:

```json
{"replace": false, "values": [{"id": "othermod:rock", "required": false}]}
```

Place that file at `data/foundations_caveins/tags/block/collapsible.json` and
reload the datapack. The default trigger tag includes those new materials.
A support's upward/downward ranges describe the area it protects relative to
its own position. Item inventory weight is separate from structural collapse;
no item weights need to be supplied for this mod to function.

Operators can use `/fcaveins status` and `/fcaveins trigger <x> <y> <z>`.
The trigger command starts a bounded scan around an already-loaded position;
it does not bypass support, immunity, block-entity or chunk rules.

## Build and validate

From the repository root, with Java 25 and the pinned wrapper:

```sh
./gradlew -p standalone/cave-ins build
./gradlew -p standalone/cave-ins exportValidationServerLaunch
python3 -m pip install nbtlib
python3 tools/porting/validate_caveins_server.py --launch standalone/cave-ins/build/validation/server-launch.json --jar standalone/cave-ins/build/libs/Foundations-Cave-Ins-26.1.2-0.1.0-alpha.1.jar --workdir standalone/cave-ins/run/native-validation
```

Use `gradlew.bat` on Windows and `python` if your Python executable has that name.
The native checker creates its own disposable flat world, loads the packaged
JAR instead of source output, and refuses an existing unmarked world directory.
Its JSON and logs are written under the fixture's `evidence` directory.
It uses no TFC or sibling mod. Test helpers/classes are excluded from the mod JAR.
The export task uses the pinned ModDevGradle run configuration; normal users
install only the JAR in a matching NeoForge instance.

See the parent repository's dated standalone validation record for exact
results and host-only NeoForm/FML launch accommodations. Core algorithm tests
and native checks do not certify every modpack or a complete survival playthrough.

## Scope and remaining work

This is the first independently built cave-in candidate, not a claim that every
system branch has been extracted. Compared with TFC, this initial standalone
implementation uses vanilla landing/collision rules and retains source block
states; TFC rock-to-rubble conversion recipes, fake warning effects and crushing
through partial blocks are not ported. Supports remain effective during
propagation. It adds no custom support-beam block or TFC textures.

Graphical-client/multiplayer play, real-player mining, third-party protection
integrations, native cancelled-entity-spawn restoration and performance profiling
remain to be exercised. The packaged server checks cover falling/landing,
supports, exclusions, tag reloads, explosions and pending-work persistence.
The full TFC source tree is retained separately and is not compiled or bundled
into this JAR. Other version targets require separate ports and verification.

EUPL 1.2; upstream attribution is preserved in [NOTICE.md](NOTICE.md) and the
packaged license. The standalone `foundations_caveins` namespace is independent
of TFC save/registry IDs; no automatic TFC-save migration is claimed.
