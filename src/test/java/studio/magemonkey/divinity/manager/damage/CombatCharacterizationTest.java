package studio.magemonkey.divinity.manager.damage;

import com.google.common.base.Function;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.entity.ZombieMock;
import studio.magemonkey.codex.compat.VersionManager;
import studio.magemonkey.codex.hooks.NHook;
import studio.magemonkey.codex.registry.DamageRegistry;
import studio.magemonkey.divinity.api.event.DivinityDamageEvent;
import studio.magemonkey.divinity.config.EngineCfg;
import studio.magemonkey.divinity.hooks.EHook;
import studio.magemonkey.divinity.hooks.external.FabledHook;
import studio.magemonkey.divinity.stats.EntityStats;
import studio.magemonkey.divinity.stats.ProjectileStats;
import studio.magemonkey.divinity.stats.bonus.StatBonus;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.stats.items.attributes.api.SimpleStat;
import studio.magemonkey.divinity.stats.items.attributes.api.TypedStat;
import studio.magemonkey.divinity.testutil.MockedTest;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;

/**
 * Characterization tests for the damage pipeline (issue #366).
 * <p>
 * These pin what {@code VanillaWrapperListener} and {@code DamageManager} produce <em>today</em>, so the
 * combat restructure described in {@code doc/COMBAT_RESTRUCTURE_PLAN.md} can be shown not to change
 * any number. They are not a statement that each result is desirable. Where a test records behaviour
 * that is a known bug, its assertion message says so.
 * <p>
 * Every scenario avoids randomness: damage values are single numbers rather than ranges, and chance
 * based stats are set to 0% or 100% with their configured caps lifted.
 */
@SuppressWarnings("deprecation")
class CombatCharacterizationTest extends MockedTest {
    private static final double DELTA                = 1e-6;
    private static final double VANILLA_SWORD_DAMAGE = 6D;

    private static final List<String> FULL_PIPELINE = List.of("BeforeScale", "Start", "Pre", "Exit");

    private final Map<TypedStat.Type, Double> originalCaps = new EnumMap<>(TypedStat.Type.class);

    private PlayerMock   attacker;
    private FiringPlayer victim;
    private Recorder     recorder;
    private List<String> firedEvents;

    @BeforeEach
    void setup() {
        attacker = genPlayer("Attacker");
        victim = new FiringPlayer(server, "Victim");
        server.addPlayer(victim);
        players.add(victim);

        firedEvents = new ArrayList<>();
        recorder = new Recorder();
        server.getPluginManager().registerEvents(recorder, plugin);

        // Adding a stat to an item also syncs vanilla attribute modifiers onto it, which needs real
        // attribute constants the mocked NMS layer cannot supply. That syncing is unrelated to damage.
        EngineCfg.VANILLA_ONLY_ITEM_STATS = true;

        // The mocked NMS layer knows nothing about items, so describe an iron sword to it
        doAnswer(ans -> isIronSword(ans.getArgument(0)) ? VANILLA_SWORD_DAMAGE : 0D)
                .when(VersionManager.getNms()).getDefaultDamage(any());
        doAnswer(ans -> isIronSword(ans.getArgument(0)))
                .when(VersionManager.getNms()).isWeapon(any());
        // Without this an entity's max health reads as 0 and refreshing its stats kills it
        doAnswer(ans -> Registry.ATTRIBUTE.get(NamespacedKey.minecraft(((String) ans.getArgument(0)).toLowerCase(
                Locale.US)))).when(VersionManager.getNms()).getAttribute(anyString());

        // The default config caps these below 100%, which would make the rolls random
        liftCap(TypedStat.Type.DODGE_RATE);
        liftCap(TypedStat.Type.ACCURACY_RATE);
    }

    @AfterEach
    void tearDown() {
        HandlerList.unregisterAll(recorder);
        EngineCfg.LEGACY_COMBAT = false;
        EngineCfg.VANILLA_ONLY_ITEM_STATS = false;
        originalCaps.forEach((type, cap) -> ItemStats.getStat(type).setCapability(cap));
        originalCaps.clear();
    }

    // ------------------------------------------------------------------ //
    // Melee: where the damage map comes from
    // ------------------------------------------------------------------ //

    @Test
    void bareHand_usesEventDamageAsDefaultType() {
        Hit hit = melee(5);

        assertEquals(Map.of("physical", 5D), hit.damages());
        assertEquals(5D, hit.finalDamage(), DELTA);
        assertEquals(FULL_PIPELINE, hit.events());
    }

    @Test
    void vanillaWeapon_passesEventDamageThrough() {
        attacker.getInventory().setItemInMainHand(new ItemStack(Material.IRON_SWORD));

        assertEquals(Map.of("physical", 6D), melee(VANILLA_SWORD_DAMAGE).damages());
        assertEquals(Map.of("physical", 9D), melee(VANILLA_SWORD_DAMAGE + 3).damages(),
                "Damage above the weapon's vanilla value (strength, sharpness, ...) is kept");
    }

    @Test
    void divinityWeapon_replacesVanillaWeaponDamage() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));

        Hit hit = melee(VANILLA_SWORD_DAMAGE);

        assertEquals(Map.of("physical", 20D), hit.damages());
        assertEquals(20D, hit.finalDamage(), DELTA);
        assertEquals(FULL_PIPELINE, hit.events());
    }

    @Test
    void divinityWeapon_addsDamageAboveVanillaAsFlatBonus() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));

        // 9 dealt by the event, 6 of it is the sword itself, so 3 is "extra"
        assertEquals(Map.of("physical", 23D), melee(VANILLA_SWORD_DAMAGE + 3).damages());
    }

    @Test
    void divinityWeapon_spreadsExtraDamageEvenlyAcrossDamageTypes() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D, "fire", 10D)));

        assertEquals(Map.of("physical", 20D, "fire", 10D), melee(VANILLA_SWORD_DAMAGE).damages());
        // The extra 3 is split 1.5 / 1.5 regardless of how large each type is
        assertEquals(Map.of("physical", 21.5D, "fire", 11.5D), melee(VANILLA_SWORD_DAMAGE + 3).damages());
    }

    @Test
    void divinityWeapon_appliesAgainstMobs() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        ZombieMock zombie = new ZombieMock(server, UUID.randomUUID());

        Hit hit = hit(meleeEvent(attacker, zombie, VANILLA_SWORD_DAMAGE), zombie);

        assertEquals(Map.of("physical", 20D), hit.damages());
    }

    // ------------------------------------------------------------------ //
    // Defense
    // ------------------------------------------------------------------ //

    @Test
    void defense_currentFormula() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        victim.getInventory().setChestplate(armor(Map.of("physical", 30D)));

        Hit hit = melee(VANILLA_SWORD_DAMAGE);

        // 1 - max(30 / 5, 30 - 4 * 20 / (toughness 0 + 8)) * protection factor 0.8 * 0.05 = 0.2
        assertEquals(4D, hit.damage("physical"), DELTA);
        assertEquals(16D, hit.defended(), DELTA);
        assertEquals(4D, hit.finalDamage(), DELTA);
    }

    @Test
    void defense_legacyFormula() {
        EngineCfg.LEGACY_COMBAT = true;
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        victim.getInventory().setChestplate(armor(Map.of("physical", 30D)));

        Hit hit = melee(VANILLA_SWORD_DAMAGE);

        // 1 - 30 * protection factor 0.8 * 0.01 = 0.76
        assertEquals(15.2D, hit.damage("physical"), DELTA);
        assertEquals(4.8D, hit.defended(), DELTA);
    }

    @Test
    void defense_onlyReducesItsOwnDamageType() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D, "fire", 10D)));
        victim.getInventory().setChestplate(armor(Map.of("physical", 30D)));

        Hit hit = melee(VANILLA_SWORD_DAMAGE);

        assertEquals(4D, hit.damage("physical"), DELTA);
        assertEquals(10D, hit.damage("fire"), DELTA);
        assertEquals(14D, hit.finalDamage(), DELTA);
    }

    @Test
    void penetration_reducesEffectiveDefense() {
        ItemStack sword = weapon(Map.of("physical", 20D));
        stat(sword, TypedStat.Type.PENETRATION, 50);
        attacker.getInventory().setItemInMainHand(sword);
        victim.getInventory().setChestplate(armor(Map.of("physical", 30D)));

        // Defense 30 halved to 15: 1 - max(15 / 5, 15 - 10) * 0.8 * 0.05 = 0.8
        assertEquals(16D, melee(VANILLA_SWORD_DAMAGE).damage("physical"), DELTA);
    }

    @Test
    void vanillaArmorModifier_isDiscarded() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        Map<DamageModifier, Double> modifiers = modifiers(VANILLA_SWORD_DAMAGE);
        modifiers.put(DamageModifier.ARMOR, -3D);

        Hit hit = hit(new EntityDamageByEntityEvent(attacker,
                victim,
                DamageCause.ENTITY_ATTACK,
                modifiers,
                functions(modifiers)), victim);

        assertEquals(0D, hit.event().getDamage(DamageModifier.ARMOR), DELTA);
        assertEquals(20D, hit.finalDamage(), DELTA);
    }

    @Test
    void fullyMitigatedHit_cancelsTheEvent() {
        victim.getInventory().setChestplate(armor(Map.of("fire", 30D)));

        Hit hit = hit(environmentEvent(DamageCause.FIRE, 4), victim);

        assertTrue(hit.event().isCancelled());
        assertEquals(List.of("BeforeScale", "Start", "Pre"), hit.events(), "Exit is not fired for a hit reduced to 0");
    }

    // ------------------------------------------------------------------ //
    // Critical hits, dodge and block
    // ------------------------------------------------------------------ //

    @Test
    void criticalHit_multipliesByOnePlusCriticalDamageStat() {
        ItemStack sword = weapon(Map.of("physical", 20D));
        stat(sword, TypedStat.Type.CRITICAL_RATE, 100);
        stat(sword, TypedStat.Type.CRITICAL_DAMAGE, 2);
        attacker.getInventory().setItemInMainHand(sword);

        Hit hit = melee(VANILLA_SWORD_DAMAGE);

        // The critical damage stat starts from a base of 1, so a stat of 2 triples the hit
        assertEquals(60D, hit.damage("physical"), DELTA);
        assertTrue(hit.meta().isCritical());
    }

    @Test
    void dodge_cancelsTheEventAndStopsThePipeline() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        ItemStack chestplate = armor(Map.of());
        stat(chestplate, TypedStat.Type.DODGE_RATE, 100);
        victim.getInventory().setChestplate(chestplate);

        Hit hit = melee(VANILLA_SWORD_DAMAGE);

        assertTrue(hit.event().isCancelled());
        assertTrue(hit.meta().isDodged());
        assertEquals(0D, hit.event().getDamage(), DELTA);
        assertEquals(List.of("BeforeScale", "Start", "Dodge"), hit.events());
    }

    @Test
    void accuracy_negatesDodge() {
        ItemStack sword = weapon(Map.of("physical", 20D));
        stat(sword, TypedStat.Type.ACCURACY_RATE, 100);
        attacker.getInventory().setItemInMainHand(sword);
        ItemStack chestplate = armor(Map.of());
        stat(chestplate, TypedStat.Type.DODGE_RATE, 100);
        victim.getInventory().setChestplate(chestplate);

        Hit hit = melee(VANILLA_SWORD_DAMAGE);

        assertFalse(hit.meta().isDodged());
        assertEquals(20D, hit.finalDamage(), DELTA);
        assertEquals(FULL_PIPELINE, hit.events());
    }

    @Test
    void block_fromStats_reducesDamageByBlockDamageStat() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        ItemStack chestplate = armor(Map.of());
        stat(chestplate, TypedStat.Type.BLOCK_RATE, 100);
        stat(chestplate, TypedStat.Type.BLOCK_DAMAGE, 50);
        victim.getInventory().setChestplate(chestplate);
        raiseShield(victim);

        Hit hit = melee(VANILLA_SWORD_DAMAGE);

        assertEquals(10D, hit.damage("physical"), DELTA);
        assertTrue(hit.meta().isBlocked());
    }

    @Test
    void block_statsDoNothingUnlessThePlayerIsBlocking() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        ItemStack chestplate = armor(Map.of());
        stat(chestplate, TypedStat.Type.BLOCK_RATE, 100);
        stat(chestplate, TypedStat.Type.BLOCK_DAMAGE, 50);
        victim.getInventory().setChestplate(chestplate);

        Hit hit = melee(VANILLA_SWORD_DAMAGE);

        assertEquals(20D, hit.damage("physical"), DELTA);
        assertFalse(hit.meta().isBlocked());
    }

    @Test
    void block_vanillaShieldBlock_usesConfiguredBonusInsteadOfFullBlock() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        raiseShield(victim);
        Map<DamageModifier, Double> modifiers = modifiers(VANILLA_SWORD_DAMAGE);
        modifiers.put(DamageModifier.BLOCKING, -VANILLA_SWORD_DAMAGE);

        Hit hit = hit(new EntityDamageByEntityEvent(attacker,
                victim,
                DamageCause.ENTITY_ATTACK,
                modifiers,
                functions(modifiers)), victim);

        // engine.yml combat.shield.block: rate bonus 100, damage bonus 35 -> the hit is reduced by 35%
        assertEquals(13D, hit.damage("physical"), DELTA);
        assertEquals(0D, hit.event().getDamage(DamageModifier.BLOCKING), DELTA);
        assertEquals(13D, hit.finalDamage(), DELTA);
        assertTrue(hit.meta().isBlocked());
    }

    // ------------------------------------------------------------------ //
    // PvP / PvE modifiers and on-hit effects
    // ------------------------------------------------------------------ //

    @Test
    void pvpDamage_appliesBetweenPlayersOnly() {
        ItemStack sword = weapon(Map.of("physical", 20D));
        stat(sword, TypedStat.Type.PVP_DAMAGE, 50);
        attacker.getInventory().setItemInMainHand(sword);
        ZombieMock zombie = new ZombieMock(server, UUID.randomUUID());

        assertEquals(30D, melee(VANILLA_SWORD_DAMAGE).damage("physical"), DELTA);
        assertEquals(20D,
                hit(meleeEvent(attacker, zombie, VANILLA_SWORD_DAMAGE), zombie).damage("physical"),
                DELTA);
    }

    @Test
    void pveDamage_appliesAgainstMobsOnly() {
        ItemStack sword = weapon(Map.of("physical", 20D));
        stat(sword, TypedStat.Type.PVE_DAMAGE, 50);
        attacker.getInventory().setItemInMainHand(sword);
        ZombieMock zombie = new ZombieMock(server, UUID.randomUUID());

        assertEquals(20D, melee(VANILLA_SWORD_DAMAGE).damage("physical"), DELTA);
        assertEquals(30D,
                hit(meleeEvent(attacker, zombie, VANILLA_SWORD_DAMAGE), zombie).damage("physical"),
                DELTA);
    }

    @Test
    void vampirism_healsTheAttackerByAShareOfTheDamage() {
        ItemStack sword = weapon(Map.of("physical", 20D));
        stat(sword, TypedStat.Type.VAMPIRISM, 25);
        attacker.getInventory().setItemInMainHand(sword);
        attacker.setHealth(10);

        melee(VANILLA_SWORD_DAMAGE);

        assertEquals(15D, attacker.getHealth(), DELTA);
    }

    @Test
    void thornmail_damagesTheAttackerByAShareOfTheDamage() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        ItemStack chestplate = armor(Map.of());
        stat(chestplate, TypedStat.Type.THORNMAIL, 25);
        victim.getInventory().setChestplate(chestplate);

        melee(VANILLA_SWORD_DAMAGE);

        assertEquals(15D, attacker.getHealth(), DELTA);
    }

    // ------------------------------------------------------------------ //
    // Environmental damage
    // ------------------------------------------------------------------ //

    @Test
    void environment_mapsCauseToDamageType() {
        Hit fire = hit(environmentEvent(DamageCause.FIRE, 4), victim);

        assertEquals(Map.of("fire", 4D), fire.damages());
        assertEquals(FULL_PIPELINE, fire.events());
    }

    @Test
    void environment_unmappedCauseFallsBackToDefaultType() {
        assertEquals(Map.of("physical", 4D), hit(environmentEvent(DamageCause.FALL, 4), victim).damages());
    }

    @Test
    void environment_isReducedByMatchingDefense() {
        victim.getInventory().setChestplate(armor(Map.of("fire", 10D)));

        // 1 - max(10 / 5, 10 - 4 * 4 / 8) * 0.8 * 0.05 = 0.68
        assertEquals(2.72D, hit(environmentEvent(DamageCause.FIRE, 4), victim).damage("fire"), DELTA);
    }

    // ------------------------------------------------------------------ //
    // Projectiles
    // ------------------------------------------------------------------ //

    @Test
    void projectile_usesTheSourceWeaponAndIgnoresEventDamage() {
        Arrow arrow = arrowFrom(bow(15D), 1D);

        Hit hit = hit(new EntityDamageByEntityEvent(arrow,
                victim,
                DamageCause.PROJECTILE,
                modifiers(4),
                functions(modifiers(4))), victim);

        assertEquals(Map.of("physical", 15D), hit.damages());
        assertEquals(attacker, hit.meta().getDamager());
        assertEquals(Material.BOW, hit.meta().getWeapon().getType());
    }

    @Test
    void projectile_scalesWithLaunchPower() {
        Arrow arrow = arrowFrom(bow(15D), 0.5D);

        Hit hit = hit(new EntityDamageByEntityEvent(arrow,
                victim,
                DamageCause.PROJECTILE,
                modifiers(4),
                functions(modifiers(4))), victim);

        assertEquals(7.5D, hit.damage("physical"), DELTA);
    }

    // ------------------------------------------------------------------ //
    // Damage dealt through the Bukkit API by plugins
    // ------------------------------------------------------------------ //

    @Test
    void pluginDamageAttributedToAPlayer_isManagedLikeAWeaponSwing() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh(attacker);
        refresh(victim);

        // What any third-party plugin does to deal 7 damage "from" a player
        victim.damage(7, attacker);

        // Weapon damage 20, plus the 1 by which the request exceeds the sword's vanilla damage
        assertEquals(Map.of("physical", 21D), damagesOf(victim));
        assertEquals(21D, victim.lastEvent.getFinalDamage(), DELTA);
    }

    @Test
    void damageRegistry_divinityType_replacesTheDamageMap() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh(attacker);
        refresh(victim);
        firedEvents.clear();

        // What Fabled does for a skill whose damage classifier is "DIVINITY_fire"
        boolean handled = DamageRegistry.dealDamage(victim, 7, "DIVINITY_fire", attacker);

        assertTrue(handled);
        assertEquals(Map.of("fire", 7D), damagesOf(victim));
        assertEquals(7D, victim.lastEvent.getFinalDamage(), DELTA);
        assertEquals(FULL_PIPELINE, firedEvents);
    }

    @Test
    void damageRegistry_divinityType_isReducedByMatchingDefense() {
        victim.getInventory().setChestplate(armor(Map.of("fire", 10D)));
        refresh(attacker);
        refresh(victim);

        DamageRegistry.dealDamage(victim, 4, "DIVINITY_fire", attacker);

        assertEquals(2.72D, victim.lastEvent.getFinalDamage(), DELTA);
    }

    @Test
    void damageRegistry_unknownDivinityType_dealsNothing() {
        boolean handled = DamageRegistry.dealDamage(victim, 7, "DIVINITY_not_a_type", attacker);

        assertFalse(handled);
        assertNull(victim.lastEvent, "No damage should have been attempted");
    }

    @Test
    void ignoreDivinity_skipsWeaponDamageButStillRollsCritAndAppliesDefense() throws Exception {
        ItemStack sword = weapon(Map.of("physical", 20D));
        stat(sword, TypedStat.Type.CRITICAL_RATE, 100);
        stat(sword, TypedStat.Type.CRITICAL_DAMAGE, 2);
        attacker.getInventory().setItemInMainHand(sword);
        victim.getInventory().setChestplate(armor(Map.of("physical", 10D)));

        FabledHook hook = installFabledHook();
        try {
            // What FabledHook records for a skill with "ignore-divinity: true"
            hook.ignoreDivinity(attacker, true);

            Hit hit = melee(7);

            // 7 from the skill, tripled by the crit: 21. Defense 10 against 21:
            // 1 - max(10 / 5, 10 - 4 * 21 / 8) * 0.8 * 0.05 = 0.92
            assertEquals(19.32D, hit.damage("physical"), DELTA);
            assertTrue(hit.meta().isCritical());
            assertEquals(List.of("BeforeScale", "Start", "Pre"), hit.events(),
                    "Known bug, fixed in #367: Exit is not fired for ignore-divinity damage");
        } finally {
            hook.ignoreDivinity(attacker, false);
            plugin.getHooks().getHooks(plugin).remove(hook);
        }
    }

    // ------------------------------------------------------------------ //
    // Helpers
    // ------------------------------------------------------------------ //

    private static boolean isIronSword(@Nullable ItemStack item) {
        return item != null && item.getType() == Material.IRON_SWORD;
    }

    private void raiseShield(PlayerMock player) {
        player.getInventory().setItemInOffHand(new ItemStack(Material.SHIELD));
        player.setBlocking(true);
        assertTrue(player.isBlocking(), "Test setup: the player should be blocking");
    }

    private Hit melee(double eventDamage) {
        return hit(meleeEvent(attacker, victim, eventDamage), victim);
    }

    private EntityDamageByEntityEvent meleeEvent(Entity damager, Entity target, double eventDamage) {
        return new EntityDamageByEntityEvent(damager,
                target,
                DamageCause.ENTITY_ATTACK,
                modifiers(eventDamage),
                functions(modifiers(eventDamage)));
    }

    private EntityDamageEvent environmentEvent(DamageCause cause, double eventDamage) {
        return new EntityDamageEvent(victim, cause, modifiers(eventDamage), functions(modifiers(eventDamage)));
    }

    /**
     * Fires the event the way the server would and captures what the pipeline did with it.
     */
    private Hit hit(EntityDamageEvent event, LivingEntity target) {
        refresh(attacker);
        refresh(target);
        firedEvents.clear();
        server.getPluginManager().callEvent(event);
        return new Hit(event, List.copyOf(firedEvents), EntityStats.get(target).getLastDamageMeta());
    }

    /**
     * Rebuilds an entity's cached equipment, which the plugin normally does on a timer.
     */
    private void refresh(LivingEntity entity) {
        EntityStats.purge(entity);
        EntityStats.get(entity).updateAll();
    }

    private Map<String, Double> damagesOf(LivingEntity target) {
        return ids(Objects.requireNonNull(EntityStats.get(target).getLastDamageMeta()));
    }

    private static Map<String, Double> ids(DamageMeta meta) {
        Map<String, Double> damages = new HashMap<>();
        meta.getDamages().forEach((attribute, amount) -> damages.put(attribute.getId(), amount));
        return damages;
    }

    private static Map<DamageModifier, Double> modifiers(double base) {
        Map<DamageModifier, Double> modifiers = new EnumMap<>(DamageModifier.class);
        modifiers.put(DamageModifier.BASE, base);
        return modifiers;
    }

    private static Map<DamageModifier, Function<? super Double, Double>> functions(Map<DamageModifier, Double> modifiers) {
        Map<DamageModifier, Function<? super Double, Double>> functions = new EnumMap<>(DamageModifier.class);
        modifiers.forEach((modifier, value) -> functions.put(modifier, d -> value));
        return functions;
    }

    private ItemStack weapon(Map<String, Double> damages) {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        damages.forEach((id, value) -> Objects.requireNonNull(ItemStats.getDamageById(id))
                .add(item, new StatBonus(new double[]{value}, false, null), -1));
        return item;
    }

    private ItemStack bow(double physical) {
        ItemStack item = new ItemStack(Material.BOW);
        Objects.requireNonNull(ItemStats.getDamageById("physical"))
                .add(item, new StatBonus(new double[]{physical}, false, null), -1);
        return item;
    }

    private Arrow arrowFrom(ItemStack bow, double power) {
        attacker.getInventory().setItemInMainHand(bow);
        Arrow arrow = victim.getWorld().spawn(victim.getLocation(), Arrow.class);
        arrow.setShooter(attacker);
        ProjectileStats.setSrcWeapon(arrow, bow);
        ProjectileStats.setPower(arrow, power);
        return arrow;
    }

    private ItemStack armor(Map<String, Double> defenses) {
        ItemStack item = new ItemStack(Material.IRON_CHESTPLATE);
        defenses.forEach((id, value) -> Objects.requireNonNull(ItemStats.getDefenseById(id))
                .add(item, new StatBonus(new double[]{value}, false, null), -1));
        return item;
    }

    private void stat(ItemStack item, TypedStat.Type type, double value) {
        ((SimpleStat) Objects.requireNonNull(ItemStats.getStat(type), type.name() + " is not enabled"))
                .add(item, new StatBonus(new double[]{value}, false, null), -1);
    }

    private void liftCap(TypedStat.Type type) {
        TypedStat stat = Objects.requireNonNull(ItemStats.getStat(type), type.name() + " is not enabled");
        originalCaps.put(type, stat.getCapability());
        stat.setCapability(-1);
    }

    /**
     * Fabled is not loaded in these tests, so Divinity never registers its Fabled hook. Register one by
     * hand so the "ignore Divinity" path, which only exists through that hook, can be exercised.
     */
    private FabledHook installFabledHook() throws Exception {
        FabledHook hook  = new FabledHook(plugin);
        Field      field = NHook.class.getDeclaredField("pluginName");
        field.setAccessible(true);
        field.set(hook, EHook.SKILL_API);
        plugin.getHooks().getHooks(plugin).add(hook);
        return hook;
    }

    /**
     * What the pipeline did with one damage event.
     */
    private static final class Hit {
        private final EntityDamageEvent event;
        private final List<String>      events;
        private final DamageMeta        meta;

        Hit(EntityDamageEvent event, List<String> events, DamageMeta meta) {
            this.event = event;
            this.events = events;
            this.meta = meta;
        }

        EntityDamageEvent event() {
            return event;
        }

        List<String> events() {
            return events;
        }

        DamageMeta meta() {
            return meta;
        }

        Map<String, Double> damages() {
            return ids(meta);
        }

        double damage(String type) {
            return damages().getOrDefault(type, 0D);
        }

        double defended() {
            return meta.getDefendedDamage();
        }

        double finalDamage() {
            return event.getFinalDamage();
        }
    }

    /**
     * MockBukkit's {@code damage(amount, source)} changes health without firing a damage event, so nothing
     * a plugin listens for would run. This player fires the event a real server would, and remembers it.
     */
    private static class FiringPlayer extends PlayerMock {
        private final Map<Material, Integer> cooldowns = new EnumMap<>(Material.class);
        private       EntityDamageEvent      lastEvent;

        FiringPlayer(@NotNull ServerMock server, @NotNull String name) {
            super(server, name, UUID.randomUUID());
        }

        @Override
        public void damage(double amount, @Nullable Entity source) {
            if (source == null) {
                super.damage(amount, (Entity) null);
                return;
            }
            lastEvent = new EntityDamageByEntityEvent(source,
                    this,
                    DamageCause.ENTITY_ATTACK,
                    modifiers(amount),
                    functions(modifiers(amount)));
            server.getPluginManager().callEvent(lastEvent);
        }

        // MockBukkit does not implement item cooldowns, which the shield block check reads and sets

        @Override
        public int getCooldown(@NotNull Material material) {
            return cooldowns.getOrDefault(material, 0);
        }

        @Override
        public void setCooldown(@NotNull Material material, int ticks) {
            cooldowns.put(material, ticks);
        }
    }

    /**
     * Records the order in which Divinity's own damage events fire. Listening at the lowest priority
     * sees each event as it is fired, before Divinity's own handlers nest further events inside it.
     */
    public class Recorder implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void on(DivinityDamageEvent.BeforeScale event) {
            firedEvents.add("BeforeScale");
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void on(DivinityDamageEvent.Start event) {
            firedEvents.add("Start");
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void on(DivinityDamageEvent.Pre event) {
            firedEvents.add("Pre");
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void on(DivinityDamageEvent.Dodge event) {
            firedEvents.add("Dodge");
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void on(DivinityDamageEvent.Exit event) {
            firedEvents.add("Exit");
        }
    }
}
