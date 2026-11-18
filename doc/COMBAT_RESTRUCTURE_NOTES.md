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
| 2. The shared contract | Done with 2.2c, which adds the `attached-damage-types` key |
| 3. Fabled migration | Done (Fabled #1919, #1920, #1921, #1922) |
| 4.1 Formulas in `combat.yml` | Done (#387) |
| 4.2 Stat snapshots | Done (#385) |
| 4.2b Vanilla Thorns | Done (#384) |
| 4.3a New event surface, `DIVINITY_<type>` inside a request | Done (#389) |
| 4.3b Fabled: Physical Damage trigger and immediate buffs on `DivinityHitEvent.Result` | Next |
| 5. Clean-up | Not started |

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

- When a pull request starts using something new from Codex, raise two things together: `codex.version`
  in `pom.xml`, which is what Divinity compiles against, and `DependencyRequirement.MIN_CORE_VERSION`,
  which is what Divinity refuses to start below. A dev build reports itself as `X.Y.Z-dev.N` at runtime
  even though Maven calls it `X.Y.Z-SNAPSHOT`.

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
- **Divinity's own dealers describe their hits with a Codex `DamageRequest`** and deal them through
  `DamageManager.deal`. The listener reads the request with `DamageContexts.claim` and keeps it on the
  `DamageMeta`. A new dealer should do the same, not invent a flag or a temporary stat change. The old
  one-charge `AdjustStatEffect` trick could outlive its hit and change the attacker's next swing.
- **In `ADD` mode the request's typed amounts are ignored.** The amount is the Bukkit damage call's. See
  the plan for why.
- **Fabled's `*-defense` attribute stats describe damage taken,** not a defense value. Applying one to
  Divinity's defense value lowers defense where a server expects it to rise. `melee-defense` and
  `projectile-defense` are already applied that way; do not extend the pattern without a decision.

- **Vanilla Thorns is not a swing (4.2b).** A request-less hit with cause `THORNS` (any version) or vanilla
  damage type `thorns` (1.20.4+) takes the non-swing path in `VanillaWrapperListener`, whatever the damager;
  a hit with a request is decided by the request's origin. The damage type is read once per event.

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

## The new event surface (4.3a)

- **`DivinityHitEvent.Prepare` and `.Result`** are the supported events. `Prepare` is fired by
  `VanillaWrapperListener` after Codex scaling and before `Start`. `Result` is fired by `DamageManager` after
  the per-type loop and before the fully-mitigated check, so a listener that changes the numbers changes the
  `BASE` written, the post-hit effects, `Exit` and the combat log. `DivinityHitEventTest` pins the order and
  what each change does.
- **They extend Bukkit's `Event`, not Codex's `IEvent`.** `IEvent` has one static `HandlerList` shared by every
  Codex event, so every listener of every such event is offered every one of them. Give any new event its own
  `HandlerList`.
- **`DamageMeta` carries what the events report:** `origin`, `weaponStatsMode` (what Divinity did, not what the
  request asked) and `requestedAmounts`. `VanillaWrapperListener#describe` sets them before the first event
  fires. A meta built elsewhere has none; `DivinityHitEvent` then falls back to the inferred origin and `ADD`.
- **The old events are deprecated but still drive Divinity.** The calculation is a `Start` listener, and the
  durability and stats-refresh listeners are on `Exit`. Do not move them to the new events without a decision:
  `Exit` skips `IGNORE` hits and `Result` does not.
- **`DIVINITY_<type>` inside an open request.** `DamageManager#dealDamage` used to open its own `REPLACE` request,
  which Codex's `claim` (newest unclaimed first) handed out in place of the dealer's. The dealer's request then
  stayed open and unclaimed, so the next hit on the target inside the same scope claimed it. Now, when
  `DamageContexts.isAnnounced(target)`, `dealDamage` opens a `TypedDeals.Deal` instead of a request, and the
  listener claims both. `RAW` leaves the hit alone, `IGNORE` uses only the typed amount and is exempt, anything
  else is `REPLACE` as before. Codex has no way to read an open request without claiming it; if it gains one,
  `TypedDeals` could go. `TypedDamageInsideRequestTest` pins all of this; four of its tests fail without the fix.
- **Fabled's real call path for a `DIVINITY_<type>` skill was not checked** (Fabled is outside this repository's
  scope and the published snapshots predate fabled#1919). The fix assumes what the task described: Fabled's
  scope is open on the target while it calls `DamageRegistry.dealDamage`. Check this in 4.3b.

## Formulas in `combat.yml` (4.1)

- **Where things are.** `config/CombatCfg` loads and migrates the file and holds what is in force.
  `manager/damage/formula/` holds `CombatExpression` (the four expressions, their variables and their default
  text), `CombatPreset`, `CombatFormulas` (compiled set plus overrides) and `Formulas`.
  `DamageCalculator` keeps the stage order and the bookkeeping between stages; the numbers come from the expressions.
- **Expressions are compiled once with Codex's `Evaluator.compile`** (needs Codex `1.3.0-dev.2`, which is why
  `MIN_CORE_VERSION` was raised). A `CompiledExpression` is immutable and safe from any thread, binds variables by
  position and returns NaN and infinity as they are, so `CombatFormulas` checks every expression against the sample
  values in `CombatExpression` at load. Syntax follows Javaluator: `^` is left associative. **Javaluator, which
  Codex bundles, computes `x * 2^-1` wrongly without complaining** (the old string evaluator does too), so
  `CombatFormulas` rejects any override with `^` followed by a minus and tells the admin to write `2^(-1)`. Remove
  that check only once Codex has fixed the parser (tracked in magemonkeystudio/codex#197).
- **`DamageCalculator` keeps no state between calls** and builds a fresh argument array for each evaluation, so one
  calculator can be shared between threads and kept across hits (the stat snapshots in 4.2 may want that). It used
  to reuse arrays to avoid the allocation; measured on the benchmark that saved nothing noticeable, and it made
  sharing a calculator unsafe. `CombatFormulasTest` has a test that shares one calculator between eight threads and
  one that reuses it for many calls; keep both if the calculator changes.
- **`ReferenceDamageCalculator` (test sources) is the formula as it was written in Java.** The equivalence test
  compares bit for bit against it for both presets. Never edit its arithmetic to make a test pass.
- **`EngineCfg.LEGACY_COMBAT` is a deprecated override and nothing sets it.** `CombatCharacterizationTest` and
  `DamageCalculatorTest` still set it (or `Modifiers.legacyCombat`) to run the legacy preset, which is why the
  field and `Modifiers.legacyCombat` still exist. The old `engine.yml` key is read into
  `EngineCfg.LEGACY_COMBAT_OLD_KEY` only so migration can pick a preset, and is ignored once `combat.yml` exists.
  Do not make production code write `LEGACY_COMBAT` again: a stale `legacy-combat: true` in an old `engine.yml`
  would then override the preset in `combat.yml`.
- **Reload.** `Divinity#reload` (Codex's `CodexPlugin#reload`, what a config reload runs) goes through
  `ConfigManager.setup`, which calls `Divinity#setConfig`; `CombatCfg.load` runs there after `EngineCfg`, and the
  order matters because migration reads the old `legacy-combat` key `EngineCfg` has just read. `CombatReloadTest`
  uses the real `plugin.reload()` under MockBukkit and fails if the order changes or `CombatCfg.load` leaves
  that path.
- **Variable names use underscores** (`pvp_pve_damage`), because a hyphen is the minus operator. `pvp_pve_damage` and
  `pvp_pve_defense` are the PvP values when both sides are players, which is why the names say so.
- **Presets that give NaN or infinity keep doing so.** Only a formula the config overrides is replaced by the
  preset's result at hit time; a preset's own non-finite result is passed on as the Java formula always did.
- **Benchmark.** `DamageCalculatorBenchmark` (test sources, not a test, has a `main`) compares the replaced Java
  formulas with the configured ones on the same inputs. Run it after changing the evaluator. With Codex
  `1.3.0-dev.2` the configured formulas are about 15 times slower on this calculator-only benchmark (roughly 400 ns
  against 27 ns per hit, ten evaluations at about 35 ns); that is over the 1.5x the pull request aimed for and is
  discussed in its description.

## Stat snapshots (4.2)

Read this before you touch `EntityStats`, `StatSnapshot`, `ProjectileStats` or the equipment handlers in
`EntityManager`.

- **What is cached and what is not.** `EntityStats` always cached the equipment list (`inventory`) and the set
  and class bonus functions (`bonuses`), and refreshed both in `updateAll()`. A `StatSnapshot` sits on top of
  those and is filled in on demand: each damage type's minimum and maximum, each defense, each item stat, each
  enchantment protection factor. It stores the value *before* the things that belong to the moment of the hit,
  which are applied on every read: the roll (`Rnd.getDouble(min, max)`), the biome, active effects
  (`AdjustStatEffect`, which has charges and runs out by itself), and an item stat's cap. The Codex registries
  (`scaleValuesWithCore`), attack power, the `custom-cooldown` metadata and the attacker's `ATTACK_DAMAGE`
  attribute are read at the hit too. Do not move any of these into the snapshot: effects and rolls would then
  need invalidation that cannot be done reliably.
- **Ranges are summed over items, then rolled once.** `RANGE_FULL` adds every item's minimum and maximum before
  anything is rolled, so a snapshot holding `{min, max}` per type gives exactly the old distribution. The same
  seed gives the same rolls (`EntityStatSnapshotTest#rolls_*`).
- **Anything new that stats read needs an invalidation or a check.** If you add an input to what
  `DamageAttribute.get`, `DefenseAttribute.get`, `SimpleStat.get` or `getBonuses` read, either make the code that
  changes it call `EntityStats.invalidateSnapshot()` / `updateAll()`, or compare it on each read the way the
  vanilla armor and toughness attributes are (`EntityStats#getDefenseValues`, `#getItemStatValue`). The table of
  triggers is in the description of the pull request that introduced this.
- **`updateAll()` forces, `refreshAfterHit()` compares.** Every completed hit asks for both fighters' stats to be
  read again on the next tick (`EntityManager#onStatsDamagePacketDuplicator`). If that dropped the snapshot, it
  would never survive until the next hit. `refreshAfterHit()` drops it only when the equipment or the bonus maps
  differ from what the snapshot was built from; wear (vanilla damage and Divinity's durability) does not count.
  Callers that change something that is not equipment (a class, a profile, a set) call `updateAll()`.
- **A snapshot copies the items it was built from,** so a stack changed in place is still seen as a change.
- **Profile buffs.** `UserProfile` counts every change to its buffs (`getBuffRevision`) and the snapshot compares
  it. The buff sets are handed out for editing: code that edits them directly must call `markBuffsChanged()`,
  as `BuffCommand` does.
- **Projectiles.** `VanillaWrapperListener#onProjectileLaunchSnapshot` runs last (MONITOR) on
  `DivinityProjectileLaunchEvent`. For a projectile that can deal damage (`ProjectileStats#canDealDamage`:
  `AbstractArrow` plus the damaging kinds in `AmmoAttribute.Type`) it stores a frozen snapshot with the weapon in
  place of the main hand, the custom arrow's bonus, the active effects, the shooter's biome and the shooter's
  UUID. At impact it is ignored if the projectile's shooter is someone else. Effects are captured *without*
  using their charges; the hit that lands uses them (`StatSnapshot#rollDamage`, `#itemStats`), so a miss spends
  none. The snapshots are kept in `ProjectileStats` by projectile UUID, not as entity metadata: metadata is only
  cleared when someone removes it. `EntityStatsTask` sweeps every 100 ticks: an entry goes when the projectile is
  dead or cannot be found in a chunk that is loaded, never because its chunk is unloaded, and in any case ten
  minutes after launch. A plugin reload clears them (`Divinity#disable`). A projectile with no snapshot is read
  at impact, as before.
- **Config.** `performance.entity-stats.snapshot-max-age` (ticks, default 20) bounds how long a snapshot is
  reused when nothing announced a change. 0 turns the cache off (nothing is kept and no items are copied), which
  is the quickest way to tell whether a stat bug is the cache.
- **Benchmark.** `CombatBenchmark` is off unless asked for:
  `mvn -Dtest=CombatBenchmark -Dcombat.benchmark=true -Dcombat.benchmark.out=<file> test`. MockBukkit parses items
  slowly, so a run takes about eleven minutes without the cache. To get "before" numbers for a change, check out
  the base commit in a worktree and copy the benchmark in.
- **Testing traps found on the way.**
  - Joining a player queues a forced refresh for the next tick. In a test that ticks the scheduler, that refresh
    drops the snapshot you are looking at. Tick once in `@BeforeEach`, after adding the players.
  - `DivinityProjectileLaunchEvent` cannot be constructed under MockBukkit: its constructor calls
    `Projectile#setBounce`, which MockBukkit refuses. Build it with Mockito (`CALLS_REAL_METHODS` and stubs for
    the getters) and set the weapon and power on the projectile by hand, as the constructor would.
  - `PlayerMock` has no armor or toughness attribute until you call `registerAttribute`.
  - A seeded test needs `Rnd.rnd.setSeed(...)`. `Rnd` has one shared generator, so re-seed before each sequence.

## Still open

- Whether Fabled's `PhysicalDamageEvent` runs before Divinity's calculation is inferred from plugin load
  order and has not been tested. It needs the real Fabled plugin loaded alongside Divinity.
- The exact Bukkit API for damage sources on 1.20.4 and newer has not been checked against a server jar.
- Burn and disarm are not covered by the characterization tests. Neither deals damage through the
  pipeline, so they are not in the way of Phase 2.
- Whether a hit is one of Fabled's throwaway events is only checked when the Fabled plugin is enabled,
  which it never is in the tests. So "a throwaway event does not take a damage request away from the
  real hit" is true by the order of the code in `VanillaWrapperListener`, not by a test.
- Environmental damage is not offered to Fabled shields.
- Level changes made by another plugin (Fabled, for example) have no event. An item's level requirement is
  decided when the equipment list is rebuilt, as it was before snapshots, so such an item starts counting at the
  next refresh. Nothing about that changed in 4.2.
- A projectile hit uses the charges of effects the shooter had at launch, if the effect still exists. An effect
  that has gone by impact still adjusts the hit (it was fired with it) and is not charged.
