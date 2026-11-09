# Structure Terrain Shaper

Fabric mod project for Minecraft 26.3. It includes a Mod Menu configuration screen that reads the active world's structure registry, filters IDs/namespaces, supports multi-selection, per-structure override toggles, and bulk Base align / clear actions. The blend reach defaults to 8 blocks and can be set from 2 to 24.

Base align runs after a structure start is placed in a chunk. For each piece it scans placed blocks to find the lowest solid block in each column, identifies the perimeter, then fills air below and just outward from that perimeter toward existing terrain. A seeded smooth relief adds small variation. This intentionally may spoil floating structures; only structures explicitly selected by the user are affected.

## Important limitation

Terrain generation is server-side. In single-player the integrated server reads the local config. On multiplayer, the server's `config/structure_terrain_shaper.json` controls generation; changing the client config alone does not configure a remote server. The current implementation is an initial functional version, and should be tested in a disposable world before use on an important save.

## Build with GitHub

Push this folder to a repository. GitHub Actions installs Java 25 and Gradle 9.7.1, runs `gradle build`, and uploads the jar from `build/libs/`.

Mod Menu is optional at runtime and compile-only. YACL is not required because the mod supplies its own list screen.
