# Combat Restructure Plan

Status: **in progress**. Phase 0 is done. Practical notes for doing the work are in
[`COMBAT_RESTRUCTURE_NOTES.md`](COMBAT_RESTRUCTURE_NOTES.md).

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
| `BuffRegistry.scaleDamageForDefense` passes the original value to each provider, so only the last one counts | Codex |

Documented keys that were never asked for (fixed in its own pull request). The Fabled editor documents
`DIVINITY_damage_<type>` and `DIVINITY_defense_<type>` as buff types and `DIVINITY_<type>` as a shield
classifier. `scaleValuesWithCore` did not ask for the defense buff key or the shield classifier at all,
and for physical damage asked for `DIVINITY_damage_physical-damage` instead. The documented keys are now
asked for in addition to the ones used before, which are all kept.

Shields were also only consulted when the attacker was a player, because the lookup sat inside the
damager-side block. They now absorb a mob's hit too (fixed in the same pull request).

Left for the shared contract in Phase 2, because changing them alters live behaviour:

- The attribute keys on the defense side stay as they are (`DIVINITY_defense_<type>`). Fabled's own
  `*-defense` attribute stats describe damage taken, so pointing them at Divinity's defense *value* would
  lower defense where a server expects it to rise. `melee-defense` and `projectile-defense` are already
  applied that way today.
- Shields are not consulted for environmental damage (fire, fall and so on), only for hits that have an
  attacker.

Found later and fixed in 4.2b (Phase 4, because it changes live numbers): a hit from the vanilla Thorns
enchantment used to be treated as a full weapon swing by the armour's wearer, because the wrapper used the
attacker's weapon damage for any hit with a living attacker and did not look at the cause. Now a hit with no
damage request whose cause is `THORNS` (any version), or whose vanilla damage type is `thorns` (1.20.4 and
newer), takes the non-swing path environmental damage takes: the event's own damage, typed by
`attached-damage-types`, `attached-damage-causes` or the default type, with the defender's defenses applied.
A hit that carries a request is decided by the request's origin, as before. Divinity's own Thornmail stat is
not affected; it deals damage through a request with no attacker. Other causes that may be non-swings are
still treated as swings and are left for a follow-up. That follow-up is investigated, not decided, in
[`COMBAT_OPEN_QUESTIONS.md`](COMBAT_OPEN_QUESTIONS.md): potions and area effect clouds, wind charges, fireballs,
snowballs and eggs, the other projectiles, and true damage, each with its options, a recommendation and the test
that pins today's behaviour (`NonSwingHitsCharacterizationTest`).

Not a bug after all: `DivinityDamageEvent.Exit` does not fire for `ignore-divinity` skill damage. Firing
it would make those hits start costing weapon and armor durability, which is a behaviour change. It is
left as it is and revisited with the new event surface in Phase 4. In 4.3a it stays that way;
`DivinityHitEvent.Result` does fire for those hits.

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
| `ADD` | Managed as today: weapon damage types, plus whatever the event's damage exceeds the weapon's vanilla damage by. The request's weapon scale and bonus are applied to the weapon damage types. Today's default for Fabled skills |
| `REPLACE` | Only the typed amounts in the request. Today's `DIVINITY_<type>` classification |
| `IGNORE` | Today's `ignore-divinity`: no weapon damage and no on-hit effects, but dodge, crit, block, defenses, PvP/PvE modifiers and enchant protection still apply |
| `RAW` | New, opt-in. Divinity does not touch the event |

In `ADD` mode the amount still travels in the Bukkit damage call, not in the request's typed amounts,
which Divinity ignores in that mode. Reading both would count the damage twice, and the rule that turns the
event's damage into a bonus is what Fabled skills are balanced against today. Typed amounts are what
`REPLACE` is for.

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

As built in 4.2: the snapshot is the part of a stat that gear, sets, classes and profile buffs decide. The roll,
the biome and active effects are applied at the hit and are not cached, because effects carry charges and run
out on their own. A snapshot is dropped by the existing refresh paths, compared after a hit, and bounded by
a maximum age for changes nothing announces. See the notes file for the rules.

### 6. Improvements on Minecraft 1.20.4 and newer (Codex)

Behind one Codex interface that does nothing on older versions:

| Improvement | Gain |
|---|---|
| Deal skill damage with a real damage source | Correct cause and death message without reflection |
| Map vanilla damage types to Divinity damage types | Finer than `attached-damage-causes`; the optional key `attached-damage-types` beside it |
| Use the event's direct and causing entities | Replaces manual projectile and shooter lookups |

Codex reaches these through `DamageSources.get()`, by reflection (see "Still to verify" for what was
checked against which version). As far as we know, a damage source does not
distinguish a plugin's `damage(x, player)` from a real swing, which is one reason unknown damage stays
managed.

## Compatibility contract

These must keep working unchanged:

- Skill file strings documented by the Fabled editor: `DIVINITY_<type>` as a damage classifier,
  `DIVINITY_damage_<classifier>` and `DIVINITY_defense_<classifier>` as buff types, and shield
  classifiers such as `skill_defense_default`. Any key clean-up is alias-only.
- The `ignore-divinity` skill setting, with its current behaviour.
- `DivinityDamageEvent.BeforeScale`, `Start`, `Pre`, `Dodge` and `Exit`, as deprecated shims, for at
  least one release after 2.0. Deprecated in 4.3a in favour of `DivinityHitEvent.Prepare` and `.Result`.
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
| 2.2a | Divinity | Characterization tests for Divinity's own damage dealers: area damage, the damage action, bleed and the fishing hook | None |
| 2.1c | Codex | Let a request scale or add to the weapon's damage, for every type or for one | None |
| 2.2 | Divinity | Consume the context. Move `ActionDamage`, AoE, thorns, bleed, the fishing hook and `dealDamage` onto it | None |
| 2.2c | Divinity | Add the optional damage-type mapping key beside `attached-damage-causes` | None |

### Phase 3: Fabled migration

| PR | Repo | Scope | Behaviour change |
|---|---|---|---|
| 3.1 | Fabled | `Skill.damage` builds a request and uses the adapter. Old accessors stay as deprecated shims | None |
| 3.2 | Fabled `editor` | "Ignore Divinity" becomes a weapon-stats choice; old skills still load. Tooltips corrected | Editor only |

### Phase 4: Divinity 2.0.0

| PR | Repo | Scope | Breaking |
|---|---|---|---|
| 4.1 | Divinity | Formulas move to `combat.yml` | Config layout |
| 4.2 | Divinity | Stat snapshots. Done | Projectile timing |
| 4.2b | Divinity | Stop treating hits that are not swings as swings: vanilla Thorns first (cause `THORNS` on every version, the damage type on 1.20.4+). Uses the origin from the damage request where there is one | Thorns damage |
| 4.3a | Divinity | New event and API surface (`DivinityHitEvent.Prepare` and `.Result`); the old events become deprecated shims. A `DIVINITY_<type>` hit dealt inside a dealer's open request honours that request's weapon-stats mode. Done (#389) | `DIVINITY_<type>` inside an `IGNORE` or `RAW` request |
| 4.3b | Divinity | `DivinityHitEvent.Result` extends Codex's `FinalDamageEvent` (Codex `1.3.0-dev.3`), and Divinity registers as its provider so `FinalDamageEvent.willFire` answers for it. `DivinityHitEvent` becomes an interface; `Result#getDamages()` is keyed by id. In review | `DivinityHitEvent.Result` API (unreleased) |
| 4.3b | Fabled | Fabled stops mutating `BASE` when `FinalDamageEvent.willFire` says a provider will fire. The Physical Damage trigger and immediate buffs act on the provider's final numbers through `FinalDamageEvent`, so Fabled needs no dependency on Divinity | Yes |

### Phase 5: clean-up

Remove the shims and dead paths after a release or two. Publish documentation for plugin authors.

`DivinityDamageEvent.Dodge` has no replacement in the new surface yet (4.3a). Do not remove it before one
exists. `ItemDurabilityListener` and `EntityManager` listen to `Exit`; moving them to `Result` would make
hits that ignore weapon stats cost durability, so they need their own decision before `Exit` goes.

## Before release

Codex 1.3.0, Divinity 2.0.0 and Fabled are tested by hand on a real server first, with the plan in
[`COMBAT_SERVER_TEST_CHECKLIST.md`](COMBAT_SERVER_TEST_CHECKLIST.md). It marks what the automated tests already
cover, so the time goes to the rest.

## Still to verify

- That Fabled's `PhysicalDamageEvent` really runs before Divinity's calculation in practice. This is
  inferred from the plugin load order and is checked by the Phase 0 tests.
- The exact 1.20.4+ API for damage sources. Checked against the 1.21.11 API so far:
  `Damageable.damage(double, DamageSource)`, `DamageSource.builder(DamageType)` with `withCausingEntity`,
  `withDirectEntity` and `withDamageLocation`, and `EntityDamageEvent.getDamageSource()` all exist. The
  version each one first appeared in is still to be confirmed. Codex's API module compiles against 1.16.5,
  so the adapter cannot reference these classes directly.
- Whether GitHub sub-issues can link across the three repositories.
