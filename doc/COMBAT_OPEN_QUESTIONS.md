# Combat: hits that may wrongly be treated as weapon swings

Decision note for the combat restructure. Nothing here changes behaviour: each case is pinned by a
characterization test (`NonSwingHitsCharacterizationTest`) and the options are for the maintainers to choose from.
Background: [`COMBAT_RESTRUCTURE_PLAN.md`](COMBAT_RESTRUCTURE_PLAN.md) (vanilla Thorns, the first such case, was
fixed in 4.2b) and [`COMBAT_RESTRUCTURE_NOTES.md`](COMBAT_RESTRUCTURE_NOTES.md). Real-server checks for the
doubtful parts are in [`COMBAT_SERVER_TEST_CHECKLIST.md`](COMBAT_SERVER_TEST_CHECKLIST.md), section Q.

## The root cause

`VanillaWrapperListener.onVanillaDamage` decides "this is the attacker's attack" from **who** the damager is, not
**what** hit. A hit with no Codex request becomes a full attack (`isFullDamage`, `VanillaWrapperListener.java:354`)
when its damager is a living entity (`:293-307`) or a projectile with a living shooter (`:309-351`). Then:

- the attacker's stats are read: damage types from gear (the thrown item standing in for the main hand,
  `EntityStats.java:1082-1100`), critical, vampirism, bleed, area damage and the rest (`DamageManager.java:339-426`,
  `:452-521`);
- the event's own damage stops being the damage and is only a flat bonus on top (`VanillaWrapperListener.java:414-453`).

A hit that fails both tests takes the environment path: the event's damage, typed by vanilla damage type, then cause,
then the default type (`:384-396`), defenses applied, no attacker.

An attacker with **no stats** hides all of this, because with no damage types the event's damage is used anyway.
So every case below has two outcomes: a bare attacker (looks fine) and one with a stat on any worn item (does not).
The tests use a chestplate with 5 physical damage, a certain critical hit (tripled) and 50% vampirism.

## What was checked, and against what

The cause and damager Bukkit gives were read from **Paper 1.21.11**: `CraftEventFactory.handleEntityDamageEvent`
(from the dev bundle) and the bytecode of the vanilla 1.21.11 server jar (with Mojang's mappings) for the damage
source and amount each entity uses. Rule: the Bukkit damager is the damage source's *direct* entity; the cause is
`MAGIC` for a thrown potion, `PROJECTILE` for any other projectile, `ENTITY_EXPLOSION` for an explosion tagged
`is_explosion`, `ENTITY_ATTACK` otherwise (and `MAGIC` only for the plain `magic` type).

**Not checked: 1.16.5 and other old versions.** There, from memory of CraftBukkit, the same rule holds for potions
and projectiles, there are no damage types (so no `attached-damage-types`), no wind charge or breeze, and ownerless
evoker fangs probably fire no damager. The checklist asks for these to be confirmed.

## Summary

Attacker-with-stats column: does the hit read the attacker's gear stats, critical, vampirism and the rest?

| Case (1.21.11) | Cause / damager | Bare attacker | Attacker with stats | Origin reported | Weapon durability |
|---|---|---|---|---|---|
| Splash potion, direct hit | `MAGIC` / the potion | `magical`, event damage | **yes**, armor damage replaces the potion's | `WEAPON_PROJECTILE` | none (projectile) |
| Lingering potion / cloud | `ENTITY_ATTACK` / the cloud | `physical`, event damage | no | `ENVIRONMENT` | none |
| Wind charge, player | `PROJECTILE` / the charge | `physical` 1 | **yes** | `WEAPON_PROJECTILE` | none |
| Wind charge, breeze | `PROJECTILE` / the charge | `physical` 1, mob is damager | mob's stats | `WEAPON_PROJECTILE` | none |
| Fireball, plugin-launched by a player | `PROJECTILE` / the fireball | `physical` | **yes**, and **twice** (hit + explosion) | `WEAPON_PROJECTILE` | none |
| Fireball, dispenser | `PROJECTILE` / the fireball | `physical`, no damager | no | `WEAPON_PROJECTILE` (!) | none |
| Ghast / blaze fireball | `PROJECTILE` / the fireball | `physical`, mob is damager | mob's stats | `WEAPON_PROJECTILE` | none |
| Snowball, egg | `PROJECTILE` / the ball | **cancelled** (0 damage) | **yes**, real damage | `WEAPON_PROJECTILE` | none |
| Llama spit, shulker bullet | `PROJECTILE` / the bullet | `physical`, mob is damager | mob's stats | `WEAPON_PROJECTILE` | none |
| Evoker fangs | `ENTITY_ATTACK` (owned) or `MAGIC` / the fangs | `physical` / `magical` | no | `ENVIRONMENT` | none |
| Trident (thrown) | `PROJECTILE` / the trident | its item's stats | its item's stats plus worn gear | `WEAPON_PROJECTILE` | none |

Durability: the victim's armor wears on every hit that reaches `Exit`; the attacker's weapon only wears when there
is a damager and the hit is not a projectile (`ItemDurabilityListener.java:88`). So no case here wears a weapon, and
a cancelled hit (snowball) wears nothing.

## Q1. Splash and lingering potions, area effect clouds

**Today.**
- A splash potion that hits is a projectile with a living shooter, so it takes the full-attack path
  (`VanillaWrapperListener.java:309-351`). The launch handler stores the held potion as its source weapon
  (`:114-149`), so no weapon damage is added, but every other worn item's damage applies. With none, the event's
  6 is typed `magical` by the `MAGIC` cause (`damage.yml:36-45`).
- A lingering potion does no harm itself; its **cloud** applies harming. The cloud is an `AreaEffectCloud` damager:
  neither living nor a projectile, so it takes the environment path. Cause `ENTITY_ATTACK` is attached to no type, so
  the default type (`physical`) is used.
- `DivinityHitEvent.inferOrigin` says `WEAPON_PROJECTILE` for the splash potion and `ENVIRONMENT` for the cloud
  (`DivinityHitEvent.java:193-200`).

**Plausibly wrong?** Yes, twice. (a) With one damage stat on any worn item, a harming potion stops being 6 magical
and becomes 15 physical, critical and vampiric (5 x 3), which no potion thrown by an ungeared player does.
(b) The same effect is `magical` thrown directly and `physical` from a cloud.

**Options.**
1. Leave it, and document that gear stats apply to thrown potions.
2. Take the non-swing path for potion hits, as 4.2b did for Thorns: the event's damage typed by cause, defenses
   applied, no thrower stats. Detect a `ThrownPotion` damager (every version) or damage type `indirect_magic`
   with a potion or cloud as direct entity (1.20.4+).
3. Keep the thrower as attacker but skip weapon damage and on-hit effects (the `IGNORE` mode), so critical and
   PvP/PvE modifiers still apply.

For the type mismatch, independently: add `minecraft:indirect_magic` to `magical`'s `attached-damage-types`
(config only, 1.20.4+; older versions keep `physical`).

**Recommendation.** Option 2 (it matches vanilla expectations and the Thorns precedent), plus the config line in
the 2.0 window because it changes the default numbers. Option 3 if servers want the thrower's critical hit.

**Pinned by.** `splashPotion_thrownByAnAttackerWithNoStats_...`, `splashPotion_thrownByAnAttackerWithStats_...`,
`areaEffectCloud_isNeverASwing_andIsPhysicalNotMagical`.

## Q2. Wind charges (1.20.5+ / 1.21)

**Today.** A wind charge deals 1 damage, damage type `wind_charge`, cause `PROJECTILE`; its explosion deals none
(`SimpleExplosionDamageCalculator(true, false, ...)` in the vanilla class). A player's charge has the player as
shooter and takes the full-attack path (the same as a potion: 15 for the geared attacker). A breeze's charge is a
mob projectile and is the mob's attack. The shipped config has a `wind` damage type and a `wind` defense, but
nothing is attached to either (`damage.yml:100-110`), so no wind charge is ever `wind` damage.

**Plausibly wrong?** The player case, yes: it is a movement tool that does 1 damage, and a geared player's does 15
with a critical hit. The breeze case is fine: a mob's projectile is the mob's attack, as an arrow is.

**Options.**
1. Leave it.
2. Player-thrown charge takes the non-swing path (as Q1 option 2); a breeze's stays a mob projectile.
3. As 2, plus attach `minecraft:wind_charge` to `wind` in `damage.yml` so it is typed and defended as wind, which
   is probably why the type exists.

**Recommendation.** Option 3; the attachment goes in with the other config changes in 2.0.

**Pinned by.** `windCharge_thrownByAnAttackerWithNoStats_...`, `windCharge_thrownByAnAttackerWithStats_...`,
`windCharge_shotByAMob_...`, `windDamageType_existsButNothingIsAttachedToIt`.

## Q3. Fireballs and fire charges

**Today.**
- A fireball that hits an entity deals `fireball` damage (6; a small one 5): cause `PROJECTILE`, damager the
  fireball. It then **explodes**: a second hit with cause `ENTITY_EXPLOSION`, damager still the fireball. Both go
  through the full-attack path, so a player-owned fireball (a plugin's `launchProjectile`) hits a geared attacker's
  stats twice: 33 and 33 in the test.
- A dispenser-fired fire charge or fireball has a block as shooter. No damager is set (`:313-315`), so nothing of
  the attacker is read, yet the event reports **`WEAPON_PROJECTILE`** with no attacker, because `projectile` is set
  before the shooter is looked at (`DivinityHitEvent.java:197`).
- A ghast's or blaze's fireball is a mob projectile, the mob's attack.

**Plausibly wrong?** The dispenser origin, yes, though only what the event reports. The two-hit behaviour is
vanilla's. Plugin-launched fireballs are third-party damage, which the plan says stays managed.

**Options.**
1. Leave it.
2. Report-only: `inferOrigin` says `WEAPON_PROJECTILE` only when the projectile has a living shooter; otherwise
   `ENVIRONMENT` (or `EXTERNAL`). Changes what `DivinityHitEvent.getOrigin()` returns, nothing Divinity computes.
3. Also treat player-owned fireballs as non-swings.

**Recommendation.** Option 2 now, no change to the numbers; leave player-owned fireballs managed.

**Pinned by.** `fireball_launchedByAnAttackerWithNoStats_...`, `fireballExplosion_isTreatedLikeTheDirectHit`,
`fireball_fromADispenser_isEnvironmentLike`, `smallFireball_shotByAMob_...`.

## Q4. Other projectiles: snowballs, eggs, llama spit, shulker bullets, evoker fangs, tridents

**Today.**
- **Snowball and egg** deal 0 damage (3 to a blaze). Paper fires the event even for a player (it removes the
  vanilla "0 damage, no hit" return). With no stats the total is 0, and Divinity **cancels** a hit under 0.001
  (`DamageManager.java:311-317`), so the push is believed lost. With a stat on any worn item the same snowball
  does 15, critically, with vampirism. `ProjectileStats.canDealDamage` already says snowballs and eggs are "not
  fired for their damage" (`ProjectileStats.java:90-96`) but the wrapper does not use it.
- **Llama spit** (1) and **shulker bullet** (4) are mob projectiles, the mob's attack.
- **Evoker fangs** are the damager, neither living nor a projectile: environment path. Owned fangs deal
  `indirect_magic` with cause `ENTITY_ATTACK`, so `physical`; unowned ones deal `magic`, so `magical`.
- **Trident**: a real weapon. `getSrcWeapon` reads the entity's item (`ProjectileStats.java:63-65`). Fine.

**Plausibly wrong?** The snowball and egg: yes, on both counts. Fangs: only the type. Spit, bullet, trident: no.

**Options.**
1. Leave it.
2. Projectiles that are not `canDealDamage` take the non-swing path (as Q1 option 2), and a hit that arrived with
   0 damage is not cancelled, so vanilla's push survives.
3. Only stop cancelling a hit that arrived with 0 damage; leave gear stats on snowballs.

For fangs: attach `indirect_magic` to `magical` (config, 1.20.4+).

**Recommendation.** Option 2. The uncancel is worth doing on its own even if nothing else is, but note it also
affects 0-damage hits from other plugins, so it needs the server check (checklist Q4) first.

**Pinned by.** `snowballAndEgg_withNoStats_areCancelledAsZeroDamage`, `snowballAndEgg_thrownByAnAttackerWithStats_...`,
`llamaSpitAndShulkerBullet_shotByAMob_...`, `evokerFangs_areNeverASwing_...`, `trident_thrownByAnAttacker_...`.

## Q5. True damage

**Today.** Divinity has no true damage.
- Nothing in `src/main` is named or documented as such. `DamageRequest` has no flag for it, and the Codex builder
  offers only origin, source, cause, typed amounts, weapon scale and bonus, weapon-stats mode and knockback.
- The "direct damage" part of the formula was meant to skip the defense and is switched off: the modifier is the
  constant 0 (`DamageManager.java:386`), its stat is commented out (`TypedStat.java:49`), and the `direct-damage`
  expression in `combat.yml` multiplies by it.
- A damage type with **no defense attached** skips the defense formula (`DamageManager.java:277-278`). The shipped
  `_vanilla_generic` is that, and is attached to void, starvation, suffocation, drowning, suicide and Thorns
  (`damage.yml:7-17`). Dealing it by hand works: `DamageRegistry.dealDamage(target, 4, "DIVINITY__vanilla_generic", src)`
  is a request (origin `EXTERNAL`, mode `REPLACE`) that no defense reduces.
- `IGNORE` is not true damage: defenses, PvP/PvE modifiers and enchant protection still apply. `RAW` hands the hit
  back to vanilla, which keeps its own armor rules.

Hits **not described by a request**: all vanilla damage (void, starvation, suffocation, drowning, fire, fall, every
case above) and any third-party `damage(x, source)`. Even `_vanilla_generic` damage still has the PvP/PvE modifier,
enchant protection, the dodge roll and vanilla absorption and resistance applied to it, so it is not truly true.

**Plausibly wrong?** Not a bug: a missing feature. The risk is Fabled adding "true damage" and reaching for
`_vanilla_generic` by accident.

**Options.**
1. Document `_vanilla_generic` as the stand-in and leave it.
2. Add a `DamageRequest` flag (a Codex change) that skips the defense stage, read by the calculator.
3. Add a dedicated `true` damage type with no defense to the shipped `damage.yml`.

**Recommendation.** Option 1 now. Decide on option 2 when Fabled actually asks for it; option 3 would add a
user-visible item stat type, which is a larger decision.

**Pinned by.** `damageRequest_hasNoTrueDamageOrBypassDefenseFlag` (fails if Codex adds one),
`genericDamageType_hasNoDefenseAttached_soDefensesDoNotReduceIt`, `dealtGenericType_isDescribedByARequest_...`,
`directDamage_isNeverSet`.

## Not testable under MockBukkit

- Whether the push really is lost when Divinity cancels a snowball hit (Q4): needs a real server.
- Entities were built by hand with the cause, damager and damage source read from the 1.21.11 server; the tests
  cannot prove the server builds them that way, only that Divinity treats them so.
- The breeze is stood in for by a zombie shooter (Divinity only sees a living shooter). `Trident` is a Mockito
  stand-in because MockBukkit's throws `UnimplementedOperationException` when its item is used.
- `DivinityProjectileLaunchEvent` cannot be built under MockBukkit, so projectiles get the shooter, held item and
  power that `onVanillaProjLaunch` would give them, set by hand.
