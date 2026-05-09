# Seasonal Villages

A Fabric 1.21.1 mod that makes villages react to the passing year.

![Seasonal Villages gallery art](docs/assets/seasonal-villages-gallery.png)

## Features

- Adds a `Village Almanac` item.
- Uses Minecraft world time to cycle through spring, summer, autumn, and winter.
- Detects active villages near players by nearby villager count.
- Applies seasonal villager buffs and occasional seasonal gifts.
- Places light, capped seasonal decorations near village paths and meeting spots.
- Saves per-world decoration memory so the same chunk is not re-decorated repeatedly for the same season/year.
- Generates a validated config file at `config/seasonal-villages.json`.

## Requirements

- Minecraft 1.21.1
- Fabric Loader
- Fabric API
- Java 21 for building

## Build

```powershell
.\build-local.bat build
```

The mod jar is written to `build/libs/seasonal-villages-1.0.0.jar`.

## Play-Test In Minecraft

```powershell
.\play-client.bat
```

This launches a Fabric 1.21.1 development client with Seasonal Villages loaded.

## Configuration

After the first launch, edit:

```text
config/seasonal-villages.json
```

Available settings include days per season, village radius, minimum villager count, decoration radius, decoration count, gifts, decorations, and villager buffs.

## Publishing

Release notes, public descriptions, and upload steps live in `docs/`.
