# Combat Restructure: Working Notes

Practical notes for whoever picks up the next pull request in the combat restructure, person or AI
session. Read [`COMBAT_RESTRUCTURE_PLAN.md`](COMBAT_RESTRUCTURE_PLAN.md) first: it holds the design and the
decisions. This file holds what was learned doing the work, so it does not have to be learned twice.

Update this file in the same pull request whenever something here stops being true.

## Where things are

- **Design and decisions:** `doc/COMBAT_RESTRUCTURE_PLAN.md`
- **Tracking issue:** magemonkeystudio/divinity#365. Each planned pull request is a sub-issue named
  `Combat <phase>.<n>`, in the repository it lands in. The sub-issue states the scope, what must not
  change, and how it is proven.
- **Safety net:** `CombatCharacterizationTest` in Divinity. It pins what the damage pipeline produces.

## Status

| Phase | State |
|---|---|
| 0. Safety net and bug fixes | Done |
| 1. Extract the calculator (1.1) and the vanilla modifier bridge (1.2) | Done |
| 2. The shared contract | In progress: 2.1 and 2.1b are merged in Codex. 2.2 is next and needs a Codex build that contains them |
| 3 to 5 | Not started |

## Rules for every pull request

- One repository per pull request. Merge order is Codex, then Divinity, then Fabled.
- Never mix moving code with changing behaviour.
- State in the description which characterization tests the pull request touches and why. Before
  Phase 4 the expected answer is "none". Changing an expected number in those tests to make them pass
  is not acceptable; it means behaviour changed.
- Base branch is `dev`. Each pull request needs exactly one of the labels `build:patch`, `build:minor`
  or `build:major`, or the `semver-label` check fails.
- If something in the sub-issue turns out to be wrong or riskier than described, leave it out, say so in
  the description, and update the plan. Two items were dropped from Phase 0 this way.

## Building and testing

| Repository | Command | Notes |
|---|---|---|
| Divinity | `mvn test` | Runs everywhere. About two minutes. |
| Fabled | `mvn test` | Runs everywhere. Several minutes. |
| Codex | `mvn -DskipNms test` | See below. |

Codex details:

- `-DskipNms` leaves out the per-version server modules, which need Spigot server jars that cannot be
  downloaded. CI uses the same flag.
- The `codex-plugin` module needs **Java 25**, because a WorldEdit dependency is compiled for it. On an
  older JDK it fails with "class file has wrong version". `codex-api` and `codex-core` still build and
  test on Java 21: `mvn -DskipNms -pl codex-api,codex-core -am test`.
- Codex's registries (`DamageRegistry`, `AttributeRegistry`, `BuffRegistry`) live in `codex-core`, and
  their provider interfaces in `codex-api`, so most of the restructure's Codex work can be tested
  without the plugin module.

Always check the real exit code of the test command before pushing. Piping Maven into `grep` or `tail`
hides a failure, and a failing commit was pushed once because of it. Write the output to a file and
read the exit code, or use `set -o pipefail`.

## How to trust a test

Two checks were worth their cost every time:

- **A bug-fix test must fail without the fix.** Run it against the unfixed code and see it fail.
- **Break the production code and see a test fail.** Delete or change one line of the formula and run
  the characterization tests. The first version of those tests missed three of ten such breaks, which
  is how its gaps were found. Do this for any line a refactor is about to move.

When reverting a temporary break, restore only that edit. `git checkout -- <file>` on a file that also
holds uncommitted work throws the work away too.

## The test environment's gaps

Divinity's tests run on MockBukkit with a mocked NMS layer (`MockedTest`). Things that behave
differently from a real server:

- **`LivingEntity.damage(amount, source)` fires no damage event.** It only changes health. To test
  anything that deals damage that way, use a mock entity that fires the event itself. See `FiringPlayer`
  in `CombatCharacterizationTest`.
- **Item cooldowns are not implemented.** Calling them makes the test report as *skipped*, not failed.
  The same is true of other unimplemented MockBukkit methods, so check the skipped count, not just
  failures. Two existing trident tests in `VanillaWrapperListenerTest` are skipped for this reason.
- **Attributes are missing unless stubbed.** Without `MockedTest.useRealAttributes()` an entity's max
  health reads as 0 and refreshing its `EntityStats` kills it.
- **`EntityStats` does not refresh on its own.** After changing equipment, call
  `EntityStats.purge(entity)` and then `EntityStats.get(entity).updateAll()`.
- **Adding a stat to an item needs `EngineCfg.VANILLA_ONLY_ITEM_STATS = true`** while the item is built,
  because the vanilla attribute sync cannot run on the mocked NMS layer. Reset it afterwards.
- **Vanilla item facts come from the NMS mock.** Weapon damage and "is this a weapon" return 0 and false
  unless stubbed.
- **`setBlocking(true)` does nothing unless the player holds a shield.**
- **`ItemStats` keeps the plugin instance it saw at class load.** With several test classes in one JVM
  that is a previous, disabled plugin, so module lookups such as "is this an ItemGenerator item" fail in
  a full run while passing alone. Always run the whole suite, not just the new class.
- **The server and its world are shared by every test in a class,** and `EntityStats` is static. Players
  from earlier tests are still standing at the spawn, their bleeds still tick, and a hit scheduled for
  the next tick lands in whichever test ticks next. A test class that ticks the scheduler has to clean up
  after each test; see `leaveNothingBehind` in `CombatCharacterizationTest`.
- **Ticking the scheduler runs the stats task,** which reads every tracked player's attack cooldown.
  MockBukkit does not implement it, so the test is reported as skipped. Use a player that overrides
  `getAttackCooldown` (`TickSafePlayer`).
- **Particles are not implemented.** A bleed tick plays them; mock `EffectUT` around the tick.
- **Effects live on the `EntityStats` object.** `EntityStats.purge` drops them, so refreshing an entity
  that way mid-test ends its bleed. Call `updateAll()` without purging.
- **Fabled, MythicMobs and Citizens are not loaded.** Fabled's classes are on the test classpath, so its
  events can be constructed and its statics mocked, but its listeners do not run.

Avoid randomness instead of seeding it: use single damage values rather than ranges, and 0% or 100% for
chance stats. Several stats are capped below 100% in the default config (dodge 45%, accuracy 30%), so
lift the cap in the test and restore it.

## Things about the code that are easy to get wrong

- **Fabled fires throwaway damage events** to ask other plugins whether an attack is allowed, and can do
  so from inside a real hit (its PvP ally check does). Anything that reacts to "a damage event for this
  attacker happened" must skip them: `DefaultCombatProtection.isFakeDamageEvent`.
- **`DivinityDamageEvent.Start` is where the calculation happens.** `DamageManager` listens to it at
  `HIGHEST` and fires `Dodge`, `Pre` and `Exit` from inside. A listener on `Start` below `HIGHEST` runs
  before the calculation; one at `MONITOR` runs after it.
- **`Exit` does not fire for `ignore-divinity` damage, on purpose.** Firing it would make those hits cost
  item durability.
- **The keys Divinity asks Codex's registries for are a compatibility surface.** Servers have them saved
  in skill files. Add keys, never rename or remove them. The exact keys and their order are pinned by
  the `codexScaling_*` tests.
- **Fabled's `*-defense` attribute stats describe damage taken,** not a defense value. Applying one to
  Divinity's defense value lowers defense where a server expects it to rise. `melee-defense` and
  `projectile-defense` are already applied that way; do not extend the pattern without a decision.

- **`VanillaDamageBridge` owns every `DamageModifier` call.** It only wraps access; callers decide when to
  call it, and the order relative to event firing is unchanged. Plain `getDamage()` and `setDamage(double)`
  (which are BASE, and which Bukkit may scale other modifiers for) are not modifier calls and were left
  at their call sites: the dodge in `handleDamageModifiers` and the reads in `onVanillaDamage`.
  `getFinalDamage()` is only read inside the bridge. Add any new modifier access to the bridge, not to
  callers. `VanillaDamageBridgeTest` builds events by hand; it does not fire them.
- **Two blocks in `DamageManager` behave nearly alike but are not the same.** Blocking is written with an
  `isApplicable` guard but read without one; invulnerability reduction is read without a guard inside
  `try`/`catch`. Both are kept as they were.

- **The end-to-end tests do not guard three bridge details:** the value written to BLOCKING when a shield
  roll fails or succeeds, the invulnerability reduction read, and resistance counting toward the absorption
  cap. Only `VanillaDamageBridgeTest` catches them, so keep that test when the bridge changes.

## Still open

- Whether Fabled's `PhysicalDamageEvent` runs before Divinity's calculation is inferred from plugin load
  order and has not been tested. It needs the real Fabled plugin loaded alongside Divinity.
- The exact Bukkit API for damage sources on 1.20.4 and newer has not been checked against a server jar.
- Burn and disarm are not covered by the characterization tests. Neither deals damage through the
  pipeline, so they are not in the way of Phase 2.
- The one-charge stat adjustment that area damage and the damage action use is only used up when the
  hit it was meant for reads the attacker's weapon damage. If that hit never gets that far (the target is
  protected, or the attacker holds a bow, whose damage is not used in melee by default), the adjustment
  stays and changes the attacker's next real swing. Moving these dealers onto a damage request in 2.2
  removes the adjustment altogether.
- Environmental damage is not offered to Fabled shields.
