# Combat Restructure Plan

Status: **proposal, awaiting review**. Nothing described here is implemented yet.

This plan spans three repositories: Codex, Divinity and Fabled (including Fabled's `editor` branch).
Divinity owns the document because Divinity owns the damage pipeline.

## Goal

Give Divinity an explicit model of "a hit", so that damage dealt by Divinity itself, by Fabled and by
other plugins is described up front instead of being reverse-engineered from an `EntityDamageEvent`.

The combat *feel* must not change by default. Server owners keep every lever they have today, and gain
the ability to edit the formulas themselves.

### Non-goals

- Changing the default numbers. The shipped configuration reproduces today's results.
- Changing how unknown third-party damage is treated. It stays managed by Divinity (see Decisions).
- Raising the minimum Minecraft version.
- Reworking item stats, lore formats, sockets, sets or any other non-combat module.

## How damage works today

1. `VanillaWrapperListener.onVanillaDamage` (priority `HIGHEST`) zeroes the vanilla armor modifier and
   guesses a damage map. If the damager is a living entity it takes their full weapon damage types plus
   `(event damage - vanilla weapon damage)` spread evenly across those types.
2. It fires `DivinityDamageEvent.BeforeScale`, applies Codex's `AttributeRegistry` and `BuffRegistry`
   (`scaleValuesWithCore`), then fires `DivinityDamageEvent.Start`.
3. The formula itself is a listener on `Start` (`DamageManager.onDamageRPGStart`). It fires `Dodge`,
   `Pre` and `Exit` from inside, writes the `BASE` modifier, and the wrapper then zeroes most of the
   remaining vanilla modifiers.

### Problems this causes

- **Every `entity.damage(x, player)` looks like a weapon swing.** Divinity cannot tell a swing from a
  skill, a proc or another plugin, so each case needs its own workaround.
- **Intent travels through side channels.** Today there are six:
  - a one-charge `AdjustStatEffect` followed by `damage(1D)` (`ActionDamage`, AoE damage)
  - a temporary Bukkit listener registered and unregistered per `DamageManager.dealDamage` call
  - the `divinityIgnored` UUID list in `FabledHook`
  - the static `Skill.skillDamage` flag in Fabled
  - the `custom-cooldown` and `damageCause` entity metadata, the latter applied by reflection on the
    event's `cause` field
  - the `setIgnoreAOE` and `setArrowBonus` flags on `EntityStats`
- **Multipliers applied by other plugins before Divinity runs become small flat bonuses**, because
  Divinity subtracts the vanilla weapon damage and treats the remainder as a flat addition.
- **The formula is hardcoded.** The multiplier order and both defense formulas live in Java, selected by
  the `legacy-combat` boolean.
- **Stats are recomputed on every hit** by reading every damage type from every equipped item, and
  projectiles use the shooter's stats at impact rather than at launch.
- **The combat log reconciles after the fact.** `DamageMeta.addMissingDmg` spreads any late difference
  evenly across damage types.

### Known bugs (fixed in Phase 0)

| Bug | Location |
|---|---|
| Arrow bonus is never cleared when `BeforeScale` or `Start` is cancelled, which includes every dodge | `VanillaWrapperListener` |
| A cancelled skill hit leaves the damager in `divinityIgnored`, so their next melee swing skips Divinity | `FabledHook` |
| The Citizens NPC check compares a fully qualified class name against `"PlayerNPC"` and never matches | `VanillaWrapperListener.scaleValuesWithCore` |
| `DivinityDamageEvent.Exit` never fires for exempt skill damage, so durability and stat refresh skip it | `DamageManager` |
| `BuffRegistry.scaleDamageForDefense` passes the original value to each provider, so only the last one counts | Codex |

To confirm before fixing: the buff and attribute key prefixes on the defense side of
`scaleValuesWithCore` look swapped relative to the damage side.

## Target design

### 1. Damage request and scoped context (Codex)

A dealer describes the hit before calling Bukkit's `damage()`. The request carries:

- **origin**: weapon melee, weapon projectile, skill, effect (bleed, thorns, AoE), environment, external
- **typed amounts**: damage type id to amount
- **cause** and **source entity**
- **weapon-stats mode** (see below)
- **flags** such as knockback

The context wraps the Bukkit call. It does not replace it:

```java
DamageRequest request = DamageRequest.builder()
        .origin(Origin.SKILL)
        .source(caster)
        .cause(cause)
        .amount(classification, damage)
        .weaponStats(WeaponStats.ADD)
        .knockback(knockback)
        .build();

try (DamageScope scope = DamageContexts.open(target, request)) {
    target.damage(damage, caster);
}
```

Names in this sketch are illustrative and will be settled in the Codex PR.

Rules:

- The context is keyed to the target and consumed by the first damage event for that target, so a
  nested hit (thorns, bleed) cannot pick it up.
- The scope closes itself even when the event is cancelled.
- If nothing reads the context, nothing happens. Fabled without Divinity behaves as it does today.
- It is plain Java and works on every supported Minecraft version.

### 2. Weapon-stats modes

| Situation | Divinity's behaviour |
|---|---|
| No context (vanilla swing, third-party plugin) | Managed exactly as today |
| `ADD` | Weapon damage types plus the requested amount. Today's default for Fabled skills |
| `REPLACE` | Only the typed amounts in the request. Today's `DIVINITY_<type>` classification |
| `IGNORE` | Today's `ignore-divinity`: no weapon damage and no on-hit effects, but dodge, crit, block, defenses, PvP/PvE modifiers and enchant protection still apply |
| `RAW` | New, opt-in. Divinity does not touch the event |

### 3. Calculator and vanilla bridge (Divinity)

- A plain calculator class with no Bukkit events inside: build context, snapshot attacker and defender
  stats, run the stages, produce a result.
- One bridge class owns every read and write of vanilla damage modifiers.
- Events become notifications at stage boundaries. The existing `DivinityDamageEvent` subclasses are
  kept as deprecated shims fired at the same points.
- The combat log reads the result instead of reconciling afterwards.

### 4. Formulas in configuration (Divinity 2.0)

Stages become expressions in a new `combat.yml`, compiled once at load using Codex's `Evaluator`.
The shipped default reproduces today's numbers exactly. `legacy-combat` becomes a preset and is
migrated automatically.

### 5. Stat snapshots (Divinity 2.0)

Cache each entity's minimum and maximum per damage type, invalidate on equipment or effect change, and
roll at hit time. Projectiles snapshot the shooter's stats at launch.

### 6. Improvements on Minecraft 1.20.4 and newer (Codex)

Behind one Codex interface that does nothing on older versions:

| Improvement | Gain |
|---|---|
| Deal skill damage with a real damage source | Correct cause and death message without reflection |
| Map vanilla damage types to Divinity damage types | Finer than `attached-damage-causes`; a new optional key beside it |
| Use the event's direct and causing entities | Replaces manual projectile and shooter lookups |

The exact API calls are to be confirmed in the Codex PR. As far as we know, a damage source does not
distinguish a plugin's `damage(x, player)` from a real swing, which is one reason unknown damage stays
managed.

## Compatibility contract

These must keep working unchanged:

- Skill file strings documented by the Fabled editor: `DIVINITY_<type>` as a damage classifier,
  `DIVINITY_damage_<classifier>` and `DIVINITY_defense_<classifier>` as buff types, and shield
  classifiers such as `skill_defense_default`. Any key clean-up is alias-only.
- The `ignore-divinity` skill setting, with its current behaviour.
- `DivinityDamageEvent.BeforeScale`, `Start`, `Pre`, `Dodge` and `Exit`, as deprecated shims, for at
  least one release after 2.0.
- `DamageTypeProvider.dealDamage`, delegating to the new request API.
- Existing `engine.yml` combat options, migrated where they move.

## Decisions

| Decision | Outcome |
|---|---|
| Unknown third-party damage | Stays managed by Divinity, as it always has been |
| Meaning of `ignore-divinity` | Keeps current behaviour (`IGNORE`). The editor tooltip is corrected. `RAW` is added as a separate opt-in |
| Minimum Minecraft version | Unchanged. Newer versions get optional improvements only |
| Versioning | Divinity 2.0.0 at Phase 4. Codex and Fabled stay on minor releases |
| Old events | Kept as deprecated shims, removed in a later 2.x |

## Phases

Rules for every pull request:

- One repository per pull request, merged in dependency order: Codex, then Divinity, then Fabled.
- No pull request mixes moving code with changing behaviour.
- Each pull request states which Phase 0 tests it touches and why. Before Phase 4 the expected answer
  is "none".

### Phase 0: safety net

| PR | Repo | Scope | Behaviour change |
|---|---|---|---|
| 0.1 | Divinity | Characterization tests locking today's numbers: melee, projectile, skill (default, `DIVINITY_` and ignore-divinity), environment, dodge, block, crit, both defense formulas | None |
| 0.2 | Divinity | The Divinity bug fixes listed above, each with a failing test first | Fixes only |
| 0.3 | Codex | Fix `BuffRegistry.scaleDamageForDefense` | Fix only |

### Phase 1: extract the calculator

| PR | Repo | Scope | Behaviour change |
|---|---|---|---|
| 1.1 | Divinity | Move the formula out of the `Start` listener into a calculator class | None |
| 1.2 | Divinity | Move all vanilla modifier handling into one bridge class | None |

### Phase 2: the shared contract

| PR | Repo | Scope | Behaviour change |
|---|---|---|---|
| 2.1 | Codex | Add the damage request and scoped context. Additive; `dealDamage` delegates | None |
| 2.1b | Codex | Damage source adapter for 1.20.4+, with a no-op fallback | None |
| 2.2 | Divinity | Consume the context. Move `ActionDamage`, AoE, thorns, bleed, the fishing hook and `dealDamage` onto it. Add the optional damage-type mapping key | None |

### Phase 3: Fabled migration

| PR | Repo | Scope | Behaviour change |
|---|---|---|---|
| 3.1 | Fabled | `Skill.damage` builds a request and uses the adapter. Old accessors stay as deprecated shims | None |
| 3.2 | Fabled `editor` | "Ignore Divinity" becomes a weapon-stats choice; old skills still load. Tooltips corrected | Editor only |

### Phase 4: Divinity 2.0.0

| PR | Repo | Scope | Breaking |
|---|---|---|---|
| 4.1 | Divinity | Formulas move to `combat.yml` | Config layout |
| 4.2 | Divinity | Stat snapshots | Projectile timing |
| 4.3 | Divinity and Fabled | New event and API surface. Fabled stops mutating `BASE` when Divinity is present. The Physical Damage trigger and immediate buffs act on Divinity's final numbers | Yes |

### Phase 5: clean-up

Remove the shims and dead paths after a release or two. Publish documentation for plugin authors.

## Still to verify

- That Fabled's `PhysicalDamageEvent` really runs before Divinity's calculation in practice. This is
  inferred from the plugin load order and is checked by the Phase 0 tests.
- The defense key prefixes noted under Known bugs.
- The exact 1.20.4+ API for damage sources.
- Whether GitHub sub-issues can link across the three repositories.
