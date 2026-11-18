package studio.magemonkey.divinity.manager.damage;

import org.bukkit.Material;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.BlockProjectileSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.ZombieMock;
import studio.magemonkey.codex.api.damage.DamageOrigin;
import studio.magemonkey.codex.api.damage.DamageRequest;
import studio.magemonkey.codex.api.damage.WeaponStatsMode;
import studio.magemonkey.codex.registry.DamageRegistry;
import studio.magemonkey.divinity.api.event.DivinityDamageEvent;
import studio.magemonkey.divinity.api.event.DivinityHitEvent;
import studio.magemonkey.divinity.stats.EntityStats;
import studio.magemonkey.divinity.stats.ProjectileStats;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.stats.items.attributes.DamageAttribute;
import studio.magemonkey.divinity.stats.items.attributes.api.TypedStat;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Characterization tests for hits that carry no Codex {@link DamageRequest} and are not weapon swings, but that
 * {@code VanillaWrapperListener} may still treat as one (see {@code doc/COMBAT_OPEN_QUESTIONS.md}). Vanilla Thorns
 * was the first such case and is pinned in {@code CombatCharacterizationTest}; these are the others.
 * <p>
 * They record what happens <em>today</em>. Nothing here says the result is right; where it is doubtful the
 * assertion message says so, and the open-questions note has the options. If a change makes one of these fail,
 * that change altered behaviour and needs the decision in that note first.
 * <p>
 * Each event is built the way the server builds it: the cause, the damager entity and the damage source were read
 * from Paper's {@code CraftEventFactory} and from the vanilla 1.21.11 entity classes (see the note for how). A
 * projectile is given the shooter and the source weapon that {@code VanillaWrapperListener#onVanillaProjLaunch}
 * gives it, because {@code DivinityProjectileLaunchEvent} cannot be constructed under MockBukkit.
 * <p>
 * Two probes tell a swing from anything else. The attacker's chestplate carries a stat each: 5 physical damage, a
 * certain critical hit (damage tripled) and 50% vampirism. A hit that reads the attacker's stats shows those; one
 * that does not, shows none of them. Without the chestplate the attacker has no stats at all.
 */
@SuppressWarnings("deprecation")
class NonSwingHitsCharacterizationTest extends CombatTestBase {
    private final List<Entity>  spawned = new ArrayList<>();
    private final List<Seen>    seen    = new ArrayList<>();
    private       Listener      probe;
    private       ZombieMock    mob;

    /** What the new and the old events reported for one hit. */
    private static final class Seen {
        DamageOrigin    origin;
        WeaponStatsMode mode;
        LivingEntity    attacker;
        Entity          projectile;
        boolean         exited;
        LivingEntity    exitDamager;
        boolean         exitProjectile;
    }

    @BeforeEach
    void listen() {
        mob = new ZombieMock(server, UUID.randomUUID());
        probe = new Listener() {
            @EventHandler
            public void on(DivinityHitEvent.Result e) {
                Seen s = new Seen();
                s.origin = e.getOrigin();
                s.mode = e.getWeaponStatsMode();
                s.attacker = e.getAttacker();
                s.projectile = e.getProjectile();
                seen.add(s);
            }

            @EventHandler
            public void on(DivinityDamageEvent.Exit e) {
                // ItemDurabilityListener reads exactly these two: the attacker's weapon only loses durability
                // when there is a damager and the hit is not a projectile
                Seen s = seen.get(seen.size() - 1);
                s.exited = true;
                s.exitDamager = e.getDamager();
                s.exitProjectile = e.isProjectile();
            }
        };
        server.getPluginManager().registerEvents(probe, plugin);
    }

    @AfterEach
    void cleanUp() {
        HandlerList.unregisterAll(probe);
        seen.clear();
        spawned.forEach(Entity::remove);
        spawned.clear();
        EntityStats.purge(mob);
    }

    // ------------------------------------------------------------------ //
    // Building blocks
    // ------------------------------------------------------------------ //

    private <T extends Entity> T spawn(Class<T> type) {
        T entity = victim.getWorld().spawn(victim.getLocation(), type);
        spawned.add(entity);
        return entity;
    }

    /**
     * What gives an attacker's hit away as read from the attacker's stats: 5 physical damage, a certain
     * critical hit that triples it, and 50% vampirism. All on the chestplate, so no weapon is involved.
     */
    private void wearStatChestplate() {
        liftCap(TypedStat.Type.VAMPIRISM);
        ItemStack chest = armor(Map.of());
        Objects.requireNonNull(ItemStats.getDamageById("physical"))
                .add(chest, new studio.magemonkey.divinity.stats.bonus.StatBonus(new double[]{5}, false, null), -1);
        stat(chest, TypedStat.Type.CRITICAL_RATE, 100);
        stat(chest, TypedStat.Type.CRITICAL_DAMAGE, 2);
        stat(chest, TypedStat.Type.VAMPIRISM, 50);
        attacker.getInventory().setChestplate(chest);
    }

    /** What {@code onVanillaProjLaunch} does for a living shooter: the shooter, the held item and full power. */
    private void launch(Projectile projectile, LivingEntity shooter, ItemStack held) {
        projectile.setShooter(shooter);
        if (held != null) ProjectileStats.setSrcWeapon(projectile, held);
        ProjectileStats.setPower(projectile, 1D);
    }

    private EntityDamageByEntityEvent hitBy(Entity direct,
                                            DamageCause cause,
                                            DamageType type,
                                            Entity causing,
                                            double amount) {
        DamageSource.Builder source = DamageSource.builder(type).withDirectEntity(direct);
        if (causing != null) source.withCausingEntity(causing);
        return new EntityDamageByEntityEvent(direct,
                victim,
                cause,
                source.build(),
                modifiers(amount),
                functions(modifiers(amount)));
    }

    private EntityDamageByEntityEvent fire(EntityDamageByEntityEvent event) {
        attacker.setHealth(10D);
        refresh(attacker);
        refresh(victim);
        seen.clear();
        server.getPluginManager().callEvent(event);
        return event;
    }

    private Map<String, Double> damages() {
        return rounded(damagesOf(victim));
    }

    private DamageMeta meta() {
        return Objects.requireNonNull(EntityStats.get(victim).getLastDamageMeta());
    }

    private Seen reported() {
        assertEquals(1, seen.size(), "Expected exactly one DivinityHitEvent.Result");
        return seen.get(0);
    }

    /** The probes say the hit read the attacker's stats: 5 physical tripled by a critical hit, and vampirism. */
    private void assertReadAttackerStats(EntityDamageByEntityEvent event, double expectedPhysical) {
        assertEquals(Map.of("physical", expectedPhysical), damages());
        assertSame(attacker, meta().getDamager());
        assertTrue(meta().isCritical(), "The attacker's certain critical hit applied");
        assertEquals(Math.min(20D, 10D + expectedPhysical / 2),
                attacker.getHealth(),
                1e-6,
                "50% vampirism healed the attacker (up to their 20 maximum health)");
        assertEquals(expectedPhysical, event.getFinalDamage(), 1e-6);
    }

    /** The probes say none of the attacker's stats applied: only the event's own damage, no critical, no healing. */
    private void assertNoStatsApplied(EntityDamageByEntityEvent event, Map<String, Double> expected) {
        assertEquals(expected, damages());
        assertFalse(meta().isCritical());
        assertEquals(10D, attacker.getHealth(), 1e-6, "No vampirism");
        assertEquals(expected.values().stream().mapToDouble(Double::doubleValue).sum(), event.getFinalDamage(), 1e-6);
    }

    /** As {@link #assertNoStatsApplied}, and Divinity did not even take the shooter as the hit's damager. */
    private void assertTakenAsEnvironmental(EntityDamageByEntityEvent event, Map<String, Double> expected) {
        assertNoStatsApplied(event, expected);
        assertNull(meta().getDamager(), "Same as environmental damage: no damager stats are read");
    }

    // ------------------------------------------------------------------ //
    // 1. Splash potions and area effect clouds
    // ------------------------------------------------------------------ //

    /**
     * Server: cause {@code MAGIC}, damager the {@code ThrownPotion} (damage type {@code indirect_magic}, direct
     * entity the potion, causing entity the thrower). The thrower has no stats, so the event's own damage is
     * typed by the cause, and the hit is still reported as a weapon projectile.
     */
    @Test
    void splashPotion_thrownByAnAttackerWithNoStats_isMagicalDamageReportedAsAWeaponProjectile() {
        attacker.getInventory().setItemInMainHand(new ItemStack(Material.SPLASH_POTION));
        SplashPotion potion = spawn(SplashPotion.class);
        launch(potion, attacker, new ItemStack(Material.SPLASH_POTION));

        EntityDamageByEntityEvent event =
                fire(hitBy(potion, DamageCause.MAGIC, DamageType.INDIRECT_MAGIC, attacker, 6));

        assertEquals("magical", ItemStats.getDamageByCause(DamageCause.MAGIC).getId(), "Test setup");
        assertNoStatsApplied(event, Map.of("magical", 6D));
        assertSame(attacker, meta().getDamager(), "The thrower is the damager, who happens to have no stats");
        Seen s = reported();
        assertEquals(DamageOrigin.WEAPON_PROJECTILE, s.origin, "A potion is guessed to be a weapon projectile");
        assertEquals(WeaponStatsMode.ADD, s.mode);
        assertSame(potion, s.projectile);
        assertSame(attacker, s.attacker);
        assertTrue(s.exited);
        assertSame(attacker, s.exitDamager);
        assertTrue(s.exitProjectile, "Projectile hits cost the attacker's weapon no durability");
    }

    /**
     * With a stat on the thrower's armor, the potion's 6 is no longer what counts: the armor's damage is, with the
     * thrower's critical hit and vampirism. The potion is handled as a projectile that takes its shooter's stats.
     */
    @Test
    void splashPotion_thrownByAnAttackerWithStats_takesTheAttackersStatsInPlaceOfItsOwnDamage() {
        wearStatChestplate();
        attacker.getInventory().setItemInMainHand(new ItemStack(Material.SPLASH_POTION));
        SplashPotion potion = spawn(SplashPotion.class);
        launch(potion, attacker, new ItemStack(Material.SPLASH_POTION));

        EntityDamageByEntityEvent event =
                fire(hitBy(potion, DamageCause.MAGIC, DamageType.INDIRECT_MAGIC, attacker, 6));

        // Plausibly wrong: the harming potion's 6 magical damage is gone, replaced by 5 physical x 3
        assertReadAttackerStats(event, 15D);
        assertEquals(DamageOrigin.WEAPON_PROJECTILE, reported().origin);
    }

    /**
     * Server: the cloud applies the instant effect itself, so the damager is the {@code AreaEffectCloud}, which is
     * not a projectile. The cause stays {@code ENTITY_ATTACK}: Paper only turns {@code MAGIC} into a cause for
     * a thrown potion or the plain {@code magic} damage type, not for {@code indirect_magic}. Divinity sees neither
     * a living damager nor a projectile, so it takes the non-swing path, typed by that cause: physical, whereas
     * the same potion thrown directly is magical.
     */
    @Test
    void areaEffectCloud_isNeverASwing_andIsPhysicalNotMagical() {
        for (boolean geared : new boolean[]{false, true}) {
            if (geared) wearStatChestplate();
            AreaEffectCloud cloud = spawn(AreaEffectCloud.class);
            cloud.setSource(attacker);

            // Harming I, scaled by 0.5 for a cloud: (int) (0.5 * 6 + 0.5) = 3
            EntityDamageByEntityEvent event =
                    fire(hitBy(cloud, DamageCause.ENTITY_ATTACK, DamageType.INDIRECT_MAGIC, attacker, 3));

            assertNull(ItemStats.getDamageByCause(DamageCause.ENTITY_ATTACK),
                    "Test setup: ENTITY_ATTACK is attached to no type, so the default type (physical) is used");
            assertTakenAsEnvironmental(event, Map.of("physical", 3D));
            Seen s = reported();
            assertEquals(DamageOrigin.ENVIRONMENT, s.origin, "geared=" + geared);
            assertNull(s.attacker);
            assertNull(s.projectile);
            assertNull(s.exitDamager, "Nothing reads the weapon, so no durability and no on-hit effects");
        }
    }

    // ------------------------------------------------------------------ //
    // 2. Wind charges
    // ------------------------------------------------------------------ //

    /**
     * Server (1.21): cause {@code PROJECTILE}, damager the {@code WindCharge}, damage type {@code wind_charge},
     * 1 damage. Its explosion does no damage ({@code SimpleExplosionDamageCalculator(true, false, ...)}). A player
     * throwing it holds it, so the wind charge item is the source weapon.
     */
    @Test
    void windCharge_thrownByAnAttackerWithNoStats_isOnePhysicalDamageReportedAsAWeaponProjectile() {
        attacker.getInventory().setItemInMainHand(new ItemStack(Material.WIND_CHARGE));
        WindCharge charge = spawn(WindCharge.class);
        launch(charge, attacker, new ItemStack(Material.WIND_CHARGE));

        EntityDamageByEntityEvent event =
                fire(hitBy(charge, DamageCause.PROJECTILE, DamageType.WIND_CHARGE, attacker, 1));

        assertNoStatsApplied(event, Map.of("physical", 1D));
        assertSame(attacker, meta().getDamager());
        assertEquals(DamageOrigin.WEAPON_PROJECTILE, reported().origin);
        assertTrue(reported().exitProjectile);
    }

    @Test
    void windCharge_thrownByAnAttackerWithStats_takesTheAttackersStatsInPlaceOfItsOwnDamage() {
        wearStatChestplate();
        attacker.getInventory().setItemInMainHand(new ItemStack(Material.WIND_CHARGE));
        WindCharge charge = spawn(WindCharge.class);
        launch(charge, attacker, new ItemStack(Material.WIND_CHARGE));

        EntityDamageByEntityEvent event =
                fire(hitBy(charge, DamageCause.PROJECTILE, DamageType.WIND_CHARGE, attacker, 1));

        // Plausibly wrong: a hit meant to push (1 damage) becomes 15 with a critical hit and vampirism
        assertReadAttackerStats(event, 15D);
    }

    /**
     * A breeze's charge. A zombie stands in for the breeze: Divinity only sees a {@code LivingEntity} shooter.
     * With no stats on the mob, it is the event's own 1 damage as physical, attributed to the mob.
     */
    @Test
    void windCharge_shotByAMob_isAWeaponProjectileOfTheMob() {
        BreezeWindCharge charge = spawn(BreezeWindCharge.class);
        charge.setShooter(mob);

        EntityDamageByEntityEvent event = fire(hitBy(charge, DamageCause.PROJECTILE, DamageType.WIND_CHARGE, mob, 1));

        assertEquals(Map.of("physical", 1D), damages());
        assertSame(mob, meta().getDamager());
        assertEquals(1D, event.getFinalDamage(), 1e-6);
        Seen s = reported();
        assertEquals(DamageOrigin.WEAPON_PROJECTILE, s.origin);
        assertSame(mob, s.attacker);
        assertSame(charge, s.projectile);
    }

    /** The shipped config has a wind damage type and a wind defense, but nothing is attached to them. */
    @Test
    void windDamageType_existsButNothingIsAttachedToIt() {
        DamageAttribute wind = Objects.requireNonNull(ItemStats.getDamageById("wind"));

        assertTrue(wind.getAttachedDamageTypes().isEmpty());
        assertNull(ItemStats.getDamageByVanillaType("minecraft:wind_charge"));
        assertNull(ItemStats.getDamageByVanillaType("wind_charge"));
        assertNotNull(wind.getAttachedDefense(), "Test setup: there is a wind defense to go with it");
    }

    // ------------------------------------------------------------------ //
    // 3. Fire charges and fireballs
    // ------------------------------------------------------------------ //

    /**
     * Server: a fireball that hits an entity deals {@code fireball} damage (6): cause {@code PROJECTILE}, damager
     * the fireball. The damage type is not an explosion, so Paper does not call it one. The fireball then
     * explodes, which is a second hit (below).
     */
    @Test
    void fireball_launchedByAnAttackerWithNoStats_isTheEventsDamageAsPhysical() {
        LargeFireball ball = spawn(LargeFireball.class);
        launch(ball, attacker, new ItemStack(Material.AIR));

        EntityDamageByEntityEvent event = fire(hitBy(ball, DamageCause.PROJECTILE, DamageType.FIREBALL, attacker, 6));

        assertNull(ItemStats.getDamageByCause(DamageCause.PROJECTILE), "Test setup: PROJECTILE is attached to no type");
        assertNoStatsApplied(event, Map.of("physical", 6D));
        assertSame(attacker, meta().getDamager());
        assertEquals(DamageOrigin.WEAPON_PROJECTILE, reported().origin);
    }

    /**
     * Server: the explosion of a ghast's fireball. Cause {@code ENTITY_EXPLOSION} and damager the fireball (it is
     * not TNT), damage type {@code player_explosion} when the fireball has an owner. Divinity treats it exactly like
     * the direct hit: a projectile of its shooter.
     */
    @Test
    void fireballExplosion_isTreatedLikeTheDirectHit() {
        wearStatChestplate();
        LargeFireball ball = spawn(LargeFireball.class);
        launch(ball, attacker, new ItemStack(Material.AIR));

        EntityDamageByEntityEvent direct =
                fire(hitBy(ball, DamageCause.PROJECTILE, DamageType.FIREBALL, attacker, 6));
        // (5 from the chestplate + the event's 6, which an empty hand does not deduct) x 3
        assertReadAttackerStats(direct, 33D);

        EntityDamageByEntityEvent explosion =
                fire(hitBy(ball, DamageCause.ENTITY_EXPLOSION, DamageType.PLAYER_EXPLOSION, attacker, 6));
        assertReadAttackerStats(explosion, 33D);
        assertEquals(DamageOrigin.WEAPON_PROJECTILE, reported().origin);
    }

    /**
     * A fireball from a dispenser has a block as its shooter, which is not a living entity: no damager, so it
     * is handled like environmental damage and no stats are read. {@code DivinityHitEvent} still calls it a weapon
     * projectile, because the projectile is set before the shooter is looked at: origin and handling disagree.
     * A fireball a plugin launches from a player has the player as its shooter and is the case above.
     */
    @Test
    void fireball_fromADispenser_isEnvironmentLike() {
        wearStatChestplate();
        LargeFireball ball = spawn(LargeFireball.class);
        ball.setShooter(mock(BlockProjectileSource.class));

        EntityDamageByEntityEvent event = fire(hitBy(ball, DamageCause.PROJECTILE, DamageType.FIREBALL, null, 6));

        assertTakenAsEnvironmental(event, Map.of("physical", 6D));
        Seen s = reported();
        assertEquals(DamageOrigin.WEAPON_PROJECTILE, s.origin, "Plausibly wrong: there is no weapon and no attacker");
        assertSame(ball, s.projectile);
        assertNull(s.attacker);
        assertNull(s.exitDamager);
    }

    /** A blaze's small fireball: a mob projectile, 5 damage, attributed to the blaze. */
    @Test
    void smallFireball_shotByAMob_isAWeaponProjectileOfTheMob() {
        SmallFireball ball = spawn(SmallFireball.class);
        ball.setShooter(mob);

        EntityDamageByEntityEvent event = fire(hitBy(ball, DamageCause.PROJECTILE, DamageType.FIREBALL, mob, 5));

        assertEquals(Map.of("physical", 5D), damages());
        assertSame(mob, meta().getDamager());
        assertEquals(5D, event.getFinalDamage(), 1e-6);
        assertEquals(DamageOrigin.WEAPON_PROJECTILE, reported().origin);
    }

    // ------------------------------------------------------------------ //
    // 4. Other projectiles
    // ------------------------------------------------------------------ //

    /**
     * Server: a snowball and an egg deal 0 damage (a snowball 3 to a blaze), cause {@code PROJECTILE}, damage type
     * {@code thrown}, and the event fires for a player too (Paper removes the vanilla "0 damage, no hit" return).
     * Divinity cancels a hit whose total is below 0.001, so the push the vanilla hit would give is believed to be
     * lost. That last part is not testable here and is on the server checklist.
     */
    @Test
    void snowballAndEgg_withNoStats_areCancelledAsZeroDamage() {
        attacker.getInventory().setItemInMainHand(new ItemStack(Material.SNOWBALL));
        Snowball snowball = spawn(Snowball.class);
        launch(snowball, attacker, new ItemStack(Material.SNOWBALL));
        EntityDamageByEntityEvent snow = fire(hitBy(snowball, DamageCause.PROJECTILE, DamageType.THROWN, attacker, 0));

        assertTrue(snow.isCancelled(), "Plausibly wrong: the knockback-only hit is cancelled");
        assertEquals(Map.of("physical", 0D), damages());
        Seen s = reported();
        assertEquals(DamageOrigin.WEAPON_PROJECTILE, s.origin);
        assertFalse(s.exited, "A cancelled hit does not reach Exit");

        Egg egg = spawn(Egg.class);
        launch(egg, attacker, new ItemStack(Material.EGG));
        assertTrue(fire(hitBy(egg, DamageCause.PROJECTILE, DamageType.THROWN, attacker, 0)).isCancelled());
    }

    @Test
    void snowballAndEgg_thrownByAnAttackerWithStats_dealRealDamageWithOnHitEffects() {
        wearStatChestplate();
        attacker.getInventory().setItemInMainHand(new ItemStack(Material.SNOWBALL));
        Snowball snowball = spawn(Snowball.class);
        launch(snowball, attacker, new ItemStack(Material.SNOWBALL));

        EntityDamageByEntityEvent event =
                fire(hitBy(snowball, DamageCause.PROJECTILE, DamageType.THROWN, attacker, 0));

        // Plausibly wrong: a snowball that pushes and does nothing else now hits for 15, critically, and heals
        assertReadAttackerStats(event, 15D);
        assertFalse(event.isCancelled());
        assertEquals(DamageOrigin.WEAPON_PROJECTILE, reported().origin);
    }

    /** Llama spit (1 damage) and a shulker bullet (4): projectiles of a mob shooter, as any arrow is. */
    @Test
    void llamaSpitAndShulkerBullet_shotByAMob_areWeaponProjectilesOfTheMob() {
        LlamaSpit spit = spawn(LlamaSpit.class);
        spit.setShooter(mob);
        EntityDamageByEntityEvent spat = fire(hitBy(spit, DamageCause.PROJECTILE, DamageType.SPIT, mob, 1));

        assertEquals(Map.of("physical", 1D), damages());
        assertSame(mob, meta().getDamager());
        assertEquals(1D, spat.getFinalDamage(), 1e-6);
        assertEquals(DamageOrigin.WEAPON_PROJECTILE, reported().origin);

        ShulkerBullet bullet = spawn(ShulkerBullet.class);
        bullet.setShooter(mob);
        EntityDamageByEntityEvent shot = fire(hitBy(bullet, DamageCause.PROJECTILE, DamageType.MOB_PROJECTILE, mob, 4));

        assertEquals(Map.of("physical", 4D), damages());
        assertSame(mob, meta().getDamager());
        assertEquals(4D, shot.getFinalDamage(), 1e-6);
        assertEquals(DamageOrigin.WEAPON_PROJECTILE, reported().origin);
    }

    /**
     * Server: fangs with an owner deal {@code indirect_magic} (6) with the fangs as direct entity, so the damager
     * is the {@code EvokerFangs} and the cause {@code ENTITY_ATTACK}. The fangs are neither a living entity nor a
     * projectile, so this is the non-swing path: physical, no damager, no on-hit effects. Fangs without an owner
     * deal plain {@code magic}, whose cause is {@code MAGIC}: magical.
     */
    @Test
    void evokerFangs_areNeverASwing_andTheirTypeDependsOnWhetherTheyHaveAnOwner() {
        wearStatChestplate();
        EvokerFangs fangs = spawn(EvokerFangs.class);

        EntityDamageByEntityEvent owned =
                fire(hitBy(fangs, DamageCause.ENTITY_ATTACK, DamageType.INDIRECT_MAGIC, mob, 6));
        assertTakenAsEnvironmental(owned, Map.of("physical", 6D));
        Seen s = reported();
        assertEquals(DamageOrigin.ENVIRONMENT, s.origin);
        assertNull(s.attacker, "The evoker is not Divinity's attacker");
        assertNull(s.exitDamager);

        EntityDamageByEntityEvent unowned = fire(hitBy(fangs, DamageCause.MAGIC, DamageType.MAGIC, null, 6));
        assertTakenAsEnvironmental(unowned, Map.of("magical", 6D));
        assertEquals(DamageOrigin.ENVIRONMENT, reported().origin);
    }

    /**
     * Server: a thrown trident deals {@code trident} damage (8): cause {@code PROJECTILE}, damager the trident.
     * Unlike the others it is a weapon, with Divinity stats on the item: {@code ProjectileStats} reads them from
     * the trident entity's item. This is a legitimate weapon projectile and is pinned to show what the stats do.
     * <p>
     * MockBukkit's {@code Trident} cannot be used (its item and shooter calls are not implemented), so this is a
     * Mockito stand-in that only answers what Divinity asks of a projectile.
     */
    @Test
    void trident_thrownByAnAttacker_usesTheTridentItemsStats() {
        ItemStack item = new ItemStack(Material.TRIDENT);
        Objects.requireNonNull(ItemStats.getDamageById("physical"))
                .add(item, new studio.magemonkey.divinity.stats.bonus.StatBonus(new double[]{20}, false, null), -1);
        attacker.getInventory().setItemInMainHand(item);
        Trident trident = mock(Trident.class);
        UUID    id      = UUID.randomUUID();
        when(trident.getUniqueId()).thenReturn(id);
        when(trident.getType()).thenReturn(EntityType.TRIDENT);
        when(trident.getShooter()).thenReturn(attacker);
        when(trident.getItem()).thenReturn(item);
        when(trident.getMetadata(anyString())).thenReturn(List.of());
        when(trident.getLocation()).thenReturn(victim.getLocation());
        when(trident.getWorld()).thenReturn(victim.getWorld());

        EntityDamageByEntityEvent event = fire(new EntityDamageByEntityEvent(trident,
                victim,
                DamageCause.PROJECTILE,
                modifiers(8),
                functions(modifiers(8))));

        assertEquals(Map.of("physical", 20D), damages());
        assertSame(attacker, meta().getDamager());
        assertEquals(Material.TRIDENT, meta().getWeapon().getType());
        assertEquals(20D, event.getFinalDamage(), 1e-6);
        assertEquals(DamageOrigin.WEAPON_PROJECTILE, reported().origin);
    }

    // ------------------------------------------------------------------ //
    // 5. True damage
    // ------------------------------------------------------------------ //

    /**
     * There is no flag for it on {@link DamageRequest}: nothing in its builder says to skip defenses. If Codex adds
     * one, this fails, which is the cue to decide what Divinity does with it.
     */
    @Test
    void damageRequest_hasNoTrueDamageOrBypassDefenseFlag() {
        List<String> names = new ArrayList<>();
        for (Method method : DamageRequest.Builder.class.getMethods()) names.add(method.getName().toLowerCase());
        for (Method method : DamageRequest.class.getMethods()) names.add(method.getName().toLowerCase());

        for (String name : names) {
            assertFalse(name.contains("true") || name.contains("bypass") || name.contains("pierce")
                            || name.contains("ignoredefense") || name.contains("unblockable"),
                    "DamageRequest gained '" + name + "': decide what Divinity does with it (COMBAT_OPEN_QUESTIONS.md)");
        }
    }

    /**
     * The closest thing Divinity has is a damage type with no defense attached: {@code _vanilla_generic}, which the
     * shipped config attaches to void, starvation, suffocation, drowning, suicide and Thorns. The defense formula
     * is skipped for it. It is not truly true damage: PvP and PvE modifiers, enchantment protection, the victim's
     * dodge and the vanilla invulnerability reduction still apply.
     */
    @Test
    void genericDamageType_hasNoDefenseAttached_soDefensesDoNotReduceIt() {
        victim.getInventory().setChestplate(armor(Map.of("physical", 50D)));
        refresh(victim);
        DamageAttribute generic = Objects.requireNonNull(ItemStats.getDamageById("_vanilla_generic"));
        assertNull(generic.getAttachedDefense());
        for (DamageCause cause : new DamageCause[]{DamageCause.VOID, DamageCause.STARVATION, DamageCause.SUFFOCATION,
                DamageCause.DROWNING, DamageCause.SUICIDE}) {
            assertSame(generic, ItemStats.getDamageByCause(cause), cause.name());
        }

        victim.damage(4); // CUSTOM environmental damage: the default type, physical, which 50 defense reduces
        double reduced = damagesOf(victim).get("physical");
        assertTrue(reduced < 4D, "Physical defense applies to physical damage");

        server.getPluginManager().callEvent(new org.bukkit.event.entity.EntityDamageEvent(victim,
                DamageCause.VOID,
                modifiers(4),
                functions(modifiers(4))));
        assertEquals(Map.of("_vanilla_generic", 4D), rounded(damagesOf(victim)), "Not reduced by 50 physical defense");
        assertNull(meta().getRequest(), "A vanilla void hit is not described by a request");
    }

    /**
     * Typed damage dealt through Codex ({@code DamageTypeProvider.dealDamage}, which is a request with
     * {@code REPLACE}) in the generic type is the only way to ask Divinity for "damage that defenses do not
     * reduce": described by a request, unreduced. Nothing in the request says so; it is a side effect of the
     * type's missing defense.
     */
    @Test
    void dealtGenericType_isDescribedByARequest_andNotReducedByDefense() {
        victim.getInventory().setChestplate(armor(Map.of("physical", 50D)));
        refresh(attacker);
        refresh(victim);

        boolean handled = DamageRegistry.dealDamage(victim, 4, "DIVINITY__vanilla_generic", attacker);

        assertTrue(handled);
        assertEquals(Map.of("_vanilla_generic", 4D), rounded(damagesOf(victim)));
        DamageRequest request = Objects.requireNonNull(meta().getRequest());
        assertEquals(DamageOrigin.EXTERNAL, request.getOrigin());
        assertEquals(WeaponStatsMode.REPLACE, request.getWeaponStats());
    }

    /**
     * The "direct damage" part of the formula, which was meant to skip defenses, is fixed at 0%: the stat that fed
     * it is commented out and the modifier is the constant 0.
     */
    @Test
    void directDamage_isNeverSet() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));

        fire(meleeEvent(attacker, victim, VANILLA_SWORD_DAMAGE));

        assertEquals(0D, meta().getDirectModifier());
        assertThrows(IllegalArgumentException.class, () -> TypedStat.Type.valueOf("DIRECT_DAMAGE"));
    }
}
