# Seasonal Villages Test Plan

## Automated

Run:

```bat
build-local.bat build
```

Expected:

- Java compilation succeeds.
- Unit tests pass.
- Checkstyle passes.
- Remapped Fabric jar is created under `build/libs/`.

## Dedicated Server Smoke Test

Run:

```bat
build-local.bat runServer --args nogui
```

Expected log lines:

```text
- seasonal_villages 1.0.0
Done (...)! For help, type "help"
```

## In-Game Client Test

Run:

```bat
play-client.bat
```

Steps:

1. Create a creative world.
2. Spawn or find at least 3 villagers close together.
3. Give yourself `seasonal_villages:village_almanac`.
4. Right-click the almanac.
5. Run `/time add 192000` to advance one season.
6. Right-click the almanac again.

Expected:

- Almanac reports the current season and villager count.
- Spring, Summer, Autumn, and Winter rotate every 8 Minecraft days by default.
- Seasonal gifts and decorations appear around active villages.
- Restarting the world does not repeat the same chunk's seasonal decoration for the same season/year.
