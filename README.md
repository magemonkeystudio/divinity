[![Discord](https://dcbadge.limes.pink/api/server/5fz9wE5?style=flat)](https://discord.gg/5fz9wE5)
# Divinity — Midmines Fork

This is a personal fork of [Divinity](https://github.com/magemonkeystudio/divinity),
maintained by [midmines](https://github.com/midmines) for independent
experimentation and development. Changes in this repository are not official
Divinity changes and are not supported by the upstream maintainers.

This is my first fork, and I am still learning how GitHub works. The code and
changes in this fork were created with 100% AI assistance.

This is a fun, personal-use project focused on having lots of customization
features. Updates are made in my spare time, so they may be irregular and are
not guaranteed. This fork may differ from or lag behind the official project.

## Changes

### New features

- **`/stats` menu** — displays a player's active stats in a paginated GUI,
  including base, bonus, and total values with descriptions. Configure it in
  `src/main/resources/stats/settings.yml`; names, icons, formats, ordering,
  and display options can be customized.
- **Per-stat item group filters in the item generator** — restrict generated
  item stats to selected item groups or subtypes directly in the generator
  YAML. For example:

  ```yml
  critical_rate:
    chance: 20.0
    item-groups:
      - sword
      - axe
    scale-by-level: 1.025
    min: 3.0
    max: 6.25
    flat-range: false
  ```

  This option is available for item stats under
  `generator.item-stats.list.<stat>`. Remove `item-groups` to allow a stat on
  all items. Group names must match groups or subtypes recognized by Divinity.
- **Configurable soulbound chance for generated items** — use
  `generator.soulbound-chance` to set the chance from 0 to 100.
- **DecentHolograms support** for combat-log damage indicators, alongside
  HolographicDisplays.

### Fixes and adjustments

- AoE target search uses the configured combat AoE range instead of a fixed
  distance.
- Max-health attribute modifiers use Divinity-specific keys instead of a
  shared hardcoded identifier. This avoids conflicts with LevelledMobs and
  allows max-health bonuses from item sets to be applied correctly.
- Durability values can be updated even when durability lore is hidden.
- Stat-applied checks look at item data as well as lore, supporting stats that
  are stored without being displayed.
- The hand-type generator no longer adds hand lore to non-weapon items.

## Development

Build the project and run its tests with:

```bash
mvn test
```

## Download

The fork's plugin JAR is available at
[`build/1.1.1 FORK.jar`](./build/1.1.1%20FORK.jar). Back up your server and
test the JAR before using it on a live server. The filename is a custom fork
label; the plugin metadata currently identifies this build as `1.0.2-SNAPSHOT`.

## Links

- **This fork:** https://github.com/midmines/divinity_midmines_fork
- **Official upstream project:** https://github.com/magemonkeystudio/divinity

For issues or changes specific to this fork, use this repository. This fork
should not be treated as an official Divinity release or recommendation.
