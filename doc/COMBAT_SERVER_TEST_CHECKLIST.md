# Combat: real-server test checklist

A manual test plan for a real Paper or Spigot server, to run before releasing **Codex 1.3.0, Divinity 2.0.0 and
Fabled**. The automated tests run on MockBukkit, which cannot model a real server's events, timing, entities or
chunk loading (the gaps are listed in [`COMBAT_RESTRUCTURE_NOTES.md`](COMBAT_RESTRUCTURE_NOTES.md)). This list is
for what they cannot show. Design: [`COMBAT_RESTRUCTURE_PLAN.md`](COMBAT_RESTRUCTURE_PLAN.md). Open questions this
list helps answer: [`COMBAT_OPEN_QUESTIONS.md`](COMBAT_OPEN_QUESTIONS.md) (section Q below).

## How to use it

Each item has steps, the expected result and a coverage mark that says where to spend your time:

| Mark | Meaning | What to do |
|---|---|---|
| ✅ | The automated tests cover the numbers well | One quick smoke check that the feature is live |
| ◐ | The tests cover the logic but not the real trigger, timing or version | Run it fully |
| ⬜ | Not covered | Run it fully, and run it first |

Anything that behaves differently from "Expected" is a finding even if it looks harmless: the release promise is
that the default configuration reproduces today's numbers.

## Setup

- **Servers:** the newest Paper 1.21.x you support, and Spigot or Paper 1.16.5. Run every item on both unless it says
  otherwise. Divinity 2.0.0, Codex 1.3.0 and the Fabled build under test on both.
- **Players and mobs:** two players, A (attacker) and B (victim), both survival, with permission to use the items;
  a zombie and a skeleton; one test-only plugin (see item 11).
- **Items:** Divinity items you can read the numbers off: a sword with a single fixed physical damage (no range), a
  bow with the same, armor with a single physical defense and a single fire defense, a shield, an item with each of
  critical rate 100%, vampirism, bleed, dodge 100%, block rate 100%. Make a copy of everything with the stat
  set so the numbers are not luck.
- **Reading the numbers:** turn on the combat log module and read its breakdown, or run with the debug option you
  normally use. Note the damage per type and the victim's health lost.
- **Before each item** put A and B at full health, remove potion effects, and stand them on flat ground away from
  other mobs. Set chance stats to 0% or 100% by lifting the cap in `engine.yml` where it caps them (dodge 45%,
  accuracy 30% by default) and put the cap back afterwards.
- Record the server version, the three plugin versions and any config you changed for every run.

## 1. Melee with Divinity items

1. A holds the single-damage sword (damage D, vanilla damage V). B wears no armor.
2. A hits B once with a full cooldown, then once at the start of the cooldown.
3. Repeat with B wearing the physical-defense armor.

**Expected:** a full-cooldown hit deals D; a rushed hit deals D times the attack-power modifier. With defense, the
damage drops by the default formula's amount (see `CombatCharacterizationTest#defense_currentFormula`). The sword
loses durability; B's armor loses durability. A sweep attack behaves as set in `engine.yml`.
**Coverage:** ✅ numbers and formulas (`CombatCharacterizationTest`, `DamageCalculatorTest`). ◐ attack cooldown and
sweep on a real server.

## 2. Projectiles with Divinity items

1. A shoots B with the Divinity bow, fully drawn; then half drawn.
2. Shoot B with a vanilla bow and with a Divinity crossbow. Throw a trident (Divinity item) at B.
3. Shoot with a custom arrow if the arrow module has one.
4. On 1.20.4 to 1.21.3 specifically, compare the fully drawn damage with 1.21.4+ and with 1.16.5.

**Expected:** a full draw deals the bow's damage; a half draw scales with the launch power. The custom arrow's
bonus applies to the victim and to the shooter for that hit only. **The full-draw damage is the same on every
version** (`VanillaWrapperListener` divides the force by 3 on 1.20.4 to 1.21.3 only). The bow loses durability once
per shot, not per hit.
**Coverage:** ◐ scaling and snapshots (`CombatCharacterizationTest#projectile_*`, `ProjectileLaunchSnapshotTest`).
⬜ real force values per version, crossbow, trident, custom arrows.

## 3. Skills: weapon-stats modes and `DIVINITY_<type>`

Make one Fabled damage skill and run it with each setting. A holds the Divinity sword (damage D). The skill's raw
damage is S. Fabled's "Ignore Divinity" option is the old name for `IGNORE`, and Fabled 3.2 turns it into a
weapon-stats choice. If your editor build only has the old checkbox, test three of the modes as unchecked (ADD),
checked (IGNORE) and a `DIVINITY_<type>` classifier (REPLACE), and write down how `RAW` is reached in your build.

| Mode | Expected damage | On-hit effects (vampirism, bleed) and durability |
|---|---|---|
| `ADD` | The sword's types plus whatever S exceeds the sword's vanilla damage by (as for any plugin's hit) | Apply |
| `REPLACE` / classifier `DIVINITY_<type>` | Exactly S, as that type, defended by that type's defense | Apply, as for a swing |
| `IGNORE` | S, typed by cause, no sword damage; crit and defenses still apply | Do **not** apply, and the hit costs no durability |
| `RAW` | Divinity does not touch the event: vanilla armor and rules apply | Divinity does nothing, so none |

Then: use `DIVINITY_fire` on a victim with fire defense; use a classifier inside another skill's damage (a skill
that deals `DIVINITY_<type>` while a `IGNORE` or `RAW` skill is the outer hit); cancel a skill hit (dodge or an
ally check) and then **swing with the sword**: the swing must deal full damage; cast an AoE skill with several
targets.
**Expected for the cancelled hit:** the next swing is a normal swing (a Phase 0 fix).
**Coverage:** ✅ the numbers for each mode (`request_*`, `damageRegistry_*`, `ignoreDivinity_*`,
`TypedDamageInsideRequestTest`). ⬜ Fabled's real call path (`DIVINITY_<type>` inside an open scope was never
checked against Fabled itself), the editor mapping, the Physical Damage trigger and immediate buffs ordering
(`PhysicalDamageEvent` before Divinity's calculation is inferred, not tested), AoE with real targets.

## 4. Shields

1. B blocks with a vanilla shield; A hits from the front. Then from behind.
2. B has a block-rate-100% item and blocks; then with block rate 0%.
3. B holds a Fabled shield mechanic and a mob (zombie) hits B; then A hits B.
4. B is hit by fire and fall damage while blocking.

**Expected:** a frontal block uses the configured shield bonus rate and damage modifier, puts the shield on
cooldown and costs shield durability; from behind it does not block. Block rate 100% reduces by the block-damage stat
only while blocking. The Fabled shield absorbs a mob's hit as well as A's. Environmental damage is not offered to the
shield.
**Coverage:** ✅ logic (`block_*`, `codexScaling_shield*`). ◐ real facing, cooldown and the shield item.

## 5. Vanilla Thorns

1. B wears armor with Thorns III; A wears Divinity gear with a physical damage, 100% critical and vampirism.
2. A hits B until Thorns triggers. Repeat on 1.16.5.
3. Give B Divinity's Thornmail stat and hit it.

**Expected:** the reflected damage is the vanilla 1 to 4, typed by cause, **not** A's weapon damage, not critical, and
A is not healed. Divinity's Thornmail returns a share of the hit with no attacker stats involved. 1.16.5 relies on
the `THORNS` cause alone and 1.20.4+ also on the damage type; both must behave the same.
**Coverage:** ✅ `thorns_*`. ◐ the real Thorns trigger on both versions.

## 6. Stat snapshots

1. **Arrow, then swap.** A shoots a long arrow flight at B with a strong bow; before it lands A swaps to a weak
   item and takes off the armor with the damage stats. **Expected:** the hit uses the bow and the gear A had when
   the arrow left.
2. **Unloaded chunk.** A shoots an arrow far enough that it passes through chunks that unload (or move A away
   and let chunks unload while it flies), then lets it land. **Expected:** the arrow still hits with its launch
   stats; nothing logs an error. An arrow that despawns frees its snapshot (no growth in memory over a few
   hundred arrows).
3. **Equipment change.** A swings, swaps the sword, swings again. **Expected:** the second swing uses the new sword
   at once, with no stale damage. Do the same with armor, a Fabled class change and a profile buff.
4. **Effects.** Give A a stat-adjusting effect with one charge; hit twice. **Expected:** the charge is used on one
   hit only. A launched arrow that misses spends no charge.
5. **Cache off.** Set `performance.entity-stats.snapshot-max-age: 0`, reload, and repeat 1 and 3. **Expected:** the
   same results. If a bug appears only with the cache on, it is the cache.
6. **Reload.** Reload the plugin with arrows in flight. **Expected:** no errors; those arrows are read at impact as
   before.

**Coverage:** ✅ the cache logic (`EntityStatSnapshotTest`, 39 tests). ◐ projectile snapshots
(`ProjectileLaunchSnapshotTest`). ⬜ real chunk unloading and long flights (the tests simulate the chunk check),
equipment changes by other plugins (Fabled levels have no event: they count from the next refresh).

## 7. `combat.yml`

1. **Default.** Fresh install: `combat.yml` is written with `preset: default`. Repeat items 1 and 3 and compare
   with the 1.x numbers you recorded. **Expected:** identical.
2. **Legacy migration.** Start a server whose `engine.yml` has `legacy-combat: true`. **Expected:** `combat.yml` is
   created with `preset: legacy`, the old key is ignored afterwards, and the numbers match the old legacy formula.
3. **Legacy preset.** Set `preset: legacy`, reload. **Expected:** the old defense formula's numbers.
4. **An override.** Uncomment `damage-scaling` and remove `critical` from it; reload. **Expected:** critical hits
   no longer multiply, nothing else changes.
5. **A broken override.** Write an expression with an unknown variable, one with a syntax error, and one that
   divides by zero on some hit. **Expected:** one console message at load for each of the first two, and that
   expression falls back to the preset's. An expression with `2^-1` is not broken: it halves, as `2^(-1)` does. The division by zero cannot be seen at load: on the hit where it happens the preset's result is used.
   Combat never stops.
6. **Reload.** Edit the file and run Divinity's reload (not a restart); then break the file and reload.
   **Expected:** edits apply at once; a broken file never leaves combat without formulas.
7. **Hand edit survives.** Restart: `combat.yml` is not overwritten.

**Coverage:** ✅ parsing, fallbacks, migration (`CombatCfgTest`, `CombatFormulasTest`, `CombatReloadTest`). ◐ the
real reload command and console output. ⬜ the performance cost with real load (about 15 times slower per hit
than the old Java in the calculator alone; read the PR notes before judging).

## 8. Dodge, critical, block, PvP and PvE

1. Dodge 100% on B, accuracy 0: A hits B. Then give A accuracy 100%.
2. Critical rate 100%, critical damage 2 on A; then 0%.
3. A hits B (PvP), then a zombie (PvE), with and without a PvP damage / PvE damage stat on A and PvP / PvE
   defense on B.
4. Hit with a dodge-100% victim using a skill (Fabled) as well.

**Expected:** a dodge cancels the hit entirely (no damage, no knockback) and the next hit is normal; accuracy
cancels dodge. A critical hit multiplies by 1 + critical damage. PvP and PvE modifiers apply only to their kind of
fight.
**Coverage:** ✅ `dodge_*`, `accuracy_*`, `criticalHit_*`, `pvpDamage_*`, `pveDamage_*`. ◐ real knockback and hurt
animation on a dodge.

## 9. Durability

1. A swings a Divinity sword 10 times. **Expected:** it loses 10.
2. A shoots 10 arrows. **Expected:** the bow loses 10 (per shot, not per hit); the sword is untouched.
3. B wears Divinity armor and is hit 10 times by any attacker. **Expected:** each armor piece loses 10.
4. A uses an `IGNORE` skill on B 10 times, then a `RAW` skill, then a `REPLACE` skill. **Expected:** `IGNORE` and
   `RAW` cost no durability at all (no weapon, no armor: `Exit` does not fire for them). `REPLACE` costs the sword
   and B's armor 10 each, as a swing does.
5. B blocks 10 hits. **Expected:** the shield loses 10.
6. Hit a target dummy or armor stand.

**Coverage:** ◐ which hits reach `Exit` is covered (`CombatCharacterizationTest`, `DivinityHitEventTest`); the
durability amounts themselves are ⬜ (no test reads item wear).

## 10. A third-party plugin's damage

1. With a plugin that calls `entity.damage(x, player)` (a simple command plugin, an ability plugin or MythicMobs
   skill damage), damage B "from" A, holding the Divinity sword.
2. Repeat with the plugin damaging a mob.
3. With a plugin that damages with no source.

**Expected:** with a source, Divinity manages it like a swing: the sword's damage types plus the amount's excess
over the sword's vanilla damage, with on-hit effects (stays so by decision). With no source, the amount is typed by
cause and defended. No Codex request is involved, so `DivinityHitEvent#getOrigin` says `WEAPON_MELEE` for the first
and `ENVIRONMENT` for the last.
**Coverage:** ✅ `pluginDamageAttributedToAPlayer_*`. ⬜ any real plugin, including a plugin that sets
`damageCause` metadata or relies on `custom-cooldown`.

## 11. `DivinityHitEvent` listeners

Write a throwaway plugin that logs `DivinityHitEvent.Prepare` and `.Result` (origin, weapon-stats mode, attacker,
projectile, damages), and the deprecated `DivinityDamageEvent` ones. Then:

1. Melee, projectile, skill (each mode), environment (fall), Thorns, a bleed tick.
2. Cancel `Prepare`; cancel `Result`; change a damage in each.
3. Hit with a dodge-100% victim; with a `RAW` skill.

**Expected:** order is `BeforeScale`, `Prepare`, `Start`, `Pre`, `Result`, `Exit`; origins match the hit (melee
`WEAPON_MELEE`, arrow `WEAPON_PROJECTILE`, fall `ENVIRONMENT`, Thorns `EFFECT`, a skill `SKILL`); a change in
`Result` is what the victim takes and what vampirism sees; cancelling stops the hit. A dodge fires `Prepare` and no
`Result`. A `RAW` skill fires none. The deprecated events still fire, and `Exit` does not fire for `IGNORE`.
**Coverage:** ✅ `DivinityHitEventTest` (19 tests). ◐ a listener in a separate plugin (class loading, priority).

## 12. Old version (1.16.5) against 1.21

Run items 1 to 11 on both. Specifically expect and check these differences:

| Area | 1.16.5 | 1.21 |
|---|---|---|
| Vanilla damage types | None: `attached-damage-types` does nothing | Read from the event; finer than the cause |
| Thorns | Detected by the `THORNS` cause only | Cause and `minecraft:thorns` type |
| Skill damage | Dealt without a real damage source (cause and death message by metadata) | Real damage source where Codex's adapter supports it |
| Bow force | Used as is | Divided by 3 on 1.20.4 to 1.21.3 (`VanillaWrapperListener` line 81) |
| Wind charge, breeze | Do not exist | Present: see section Q |
| Death message of a skill kill | Read it: it should name the skill's caster | Read it: it should name the skill's caster |

**Expected:** the same numbers for the same Divinity setup; only the items in the table differ. Anything else is a
finding.
**Coverage:** ⬜ (MockBukkit runs 1.21 only).

## Q. Open-question cases (observe and record)

These are the hits in [`COMBAT_OPEN_QUESTIONS.md`](COMBAT_OPEN_QUESTIONS.md). The tests pin what Divinity does with
the event it is given; this section checks that the server gives that event. Record the result for each rather than
pass or fail.

| # | Do this | Record |
|---|---|---|
| Q1a | A (no damage stats on any item) throws a splash potion of Harming at B. Then A puts on armor with one damage stat and throws again | Cause, damager, damage type, and the damage per type each time. Expected today: magical 6, then a stat-scaled physical hit with critical and vampirism |
| Q1b | A throws a lingering potion of Harming; B stands in the cloud | Same. Expected: the damager is the cloud, damage physical, nothing of A's applies |
| Q2 | A throws a wind charge at B (1.21 only), with and without the stat armor. A breeze shoots B | Damage, whether A is healed or critical. Expected: 1 physical, then 15 with the stat armor |
| Q3a | A plugin launches a fireball from A. Ghast and blaze fire at B | The two hits (direct and explosion) and their numbers |
| Q3b | A dispenser fires a fireball and a fire charge at B | `DivinityHitEvent#getOrigin` (expected today: `WEAPON_PROJECTILE`, no attacker) |
| Q4a | A (no stats) throws a snowball and an egg at B, a zombie and a blaze | **Does the target get pushed?** Expected today: no push, since Divinity cancels the hit. This is the one to confirm |
| Q4b | The same with the stat armor | Damage, critical, vampirism |
| Q4c | Llama spit, a shulker bullet, an evoker's fangs (with an owner, and from a spawned-in fang with none), a thrown Divinity trident | Cause and numbers |
| Q5 | Void, starvation, drowning on B wearing 50 physical defense; then a skill with `DIVINITY__vanilla_generic` | Whether defense reduces them. Expected: it does not |

Also record, on 1.16.5, the cause and damager of each of the above; the open-questions note only verified 1.21.11.
