package studio.magemonkey.divinity.manager.damage;

import com.google.common.base.Function;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.entity.ZombieMock;
import org.mockito.MockedStatic;
import studio.magemonkey.codex.compat.VersionManager;
import studio.magemonkey.codex.hooks.Hooks;
import studio.magemonkey.codex.hooks.NHook;
import studio.magemonkey.codex.registry.AttributeRegistry;
import studio.magemonkey.codex.registry.BuffRegistry;
import studio.magemonkey.codex.registry.DamageRegistry;
import studio.magemonkey.codex.registry.provider.AttributeProvider;
import studio.magemonkey.codex.registry.provider.BuffProvider;
import studio.magemonkey.codex.util.EffectUT;
import studio.magemonkey.codex.util.EnumUT;
import studio.magemonkey.codex.util.actions.params.IParamResult;
import studio.magemonkey.codex.util.actions.params.IParamType;
import studio.magemonkey.codex.util.actions.params.IParamValue;
import studio.magemonkey.divinity.utils.actions.executors.ActionDamage;
import studio.magemonkey.divinity.api.event.DivinityDamageEvent;
import studio.magemonkey.divinity.config.EngineCfg;
import studio.magemonkey.divinity.hooks.EHook;
import studio.magemonkey.divinity.hooks.external.FabledHook;
import studio.magemonkey.divinity.modules.list.itemgenerator.ItemGeneratorManager;
import studio.magemonkey.divinity.stats.EntityStats;
import studio.magemonkey.divinity.stats.ProjectileStats;
import studio.magemonkey.divinity.stats.bonus.StatBonus;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.stats.items.attributes.DamageAttribute;
import studio.magemonkey.divinity.stats.items.attributes.api.SimpleStat;
import studio.magemonkey.divinity.stats.items.attributes.api.TypedStat;
import studio.magemonkey.divinity.testutil.MockedTest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

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
        attacker = new TickSafePlayer(server, "Attacker");
        server.addPlayer(attacker);
        players.add(attacker);
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
        leaveNothingBehind();
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
    void vanillaArmorModifier_isDiscardedBeforeDivinityEventsFire() {
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
        // Listeners on Divinity's own events already see the vanilla event without its armor reduction
        assertEquals(0D, recorder.armorSeenAtBeforeScale, DELTA);
        assertEquals(20D, recorder.finalDamageSeenAtExit, DELTA);
    }

    @Test
    void vanillaModifiers_onlyResistanceSurvives() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        Map<DamageModifier, Double> modifiers = modifiers(VANILLA_SWORD_DAMAGE);
        modifiers.put(DamageModifier.ARMOR, -3D);
        modifiers.put(DamageModifier.MAGIC, -2D);
        modifiers.put(DamageModifier.RESISTANCE, -4D);

        Hit hit = hit(new EntityDamageByEntityEvent(attacker,
                victim,
                DamageCause.ENTITY_ATTACK,
                modifiers,
                functions(modifiers)), victim);

        assertEquals(20D, hit.event().getDamage(DamageModifier.BASE), DELTA);
        assertEquals(0D, hit.event().getDamage(DamageModifier.ARMOR), DELTA);
        assertEquals(0D, hit.event().getDamage(DamageModifier.MAGIC), DELTA, "Vanilla enchantment protection");
        assertEquals(-4D, hit.event().getDamage(DamageModifier.RESISTANCE), DELTA);
        assertEquals(16D, hit.finalDamage(), DELTA);
    }

    @Test
    void armorToughness_weakensDefenseLessAgainstBigHits() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        ItemStack chestplate = armor(Map.of("physical", 20D));
        victim.getInventory().setChestplate(chestplate);

        // No toughness: 1 - max(20 / 5, 20 - 4 * 20 / 8) * 0.8 * 0.05 = 0.6
        assertEquals(12D, melee(VANILLA_SWORD_DAMAGE).damage("physical"), DELTA);

        stat(chestplate, TypedStat.Type.ARMOR_TOUGHNESS, 8);
        victim.getInventory().setChestplate(chestplate);

        // Toughness 8: 1 - max(20 / 5, 20 - 4 * 20 / 16) * 0.8 * 0.05 = 0.4
        assertEquals(8D, melee(VANILLA_SWORD_DAMAGE).damage("physical"), DELTA);
    }

    @Test
    void enchantProtection_reducesDamageByFourPercentPerLevel() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE);
        chestplate.addUnsafeEnchantment(Enchantment.PROTECTION, 4);
        victim.getInventory().setChestplate(chestplate);

        Hit hit = melee(VANILLA_SWORD_DAMAGE);

        // Protection IV is 4 points, each worth 1/25
        assertEquals(16.8D, hit.damage("physical"), DELTA);
        assertEquals(0.84D, hit.meta().getEnchantProtectionModifier(), DELTA);
    }

    @Test
    void enchantProtection_specialisedEnchantsCountDoubleForTheirCause() {
        ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE);
        chestplate.addUnsafeEnchantment(Enchantment.FIRE_PROTECTION, 4);
        victim.getInventory().setChestplate(chestplate);

        // Fire Protection IV is 8 points against fire: 10 * (1 - 8 / 25)
        assertEquals(6.8D, hit(environmentEvent(DamageCause.FIRE, 10), victim).damage("fire"), DELTA);
        // ...and nothing against anything else
        assertEquals(10D, hit(environmentEvent(DamageCause.FALL, 10), victim).damage("physical"), DELTA);
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
    // Divinity's own damage dealers
    // ------------------------------------------------------------------ //

    @Test
    void aoe_hitsBystandersWithAShareOfEveryWeaponDamageType() {
        attacker.getInventory().setItemInMainHand(aoeSword(50));
        FiringPlayer bystander = bystander();

        Hit main = melee(VANILLA_SWORD_DAMAGE);
        assertEquals(Map.of("physical", 20D, "fire", 10D), main.damages(), "The main hit is not reduced");
        assertTrue(bystander.events.isEmpty(), "The area damage is dealt on the next tick, not inside the hit");

        firedEvents.clear();
        server.getScheduler().performOneTick();

        assertEquals(1, bystander.events.size());
        assertSame(attacker, ((EntityDamageByEntityEvent) bystander.lastEvent).getDamager());
        assertEquals(Map.of("physical", 10D, "fire", 5D), damagesOf(bystander));
        assertEquals(15D, bystander.lastEvent.getFinalDamage(), DELTA);
        assertEquals(FULL_PIPELINE, firedEvents, "The area hit runs the whole pipeline");
        assertTrue(victim.events.isEmpty(), "The original victim is not hit again");
    }

    @Test
    void aoe_hitOnABystanderDoesNotSpreadFurther() {
        attacker.getInventory().setItemInMainHand(aoeSword(50));
        FiringPlayer bystander = bystander();

        melee(VANILLA_SWORD_DAMAGE);
        server.getScheduler().performTicks(3);

        assertEquals(1, bystander.events.size(), "The bystander is hit once");
        assertTrue(victim.events.isEmpty(), "The area hit on the bystander must not bounce back to the victim");
    }

    @Test
    void aoe_leavesTheNextSwingAtFullDamage() {
        attacker.getInventory().setItemInMainHand(aoeSword(50));
        bystander();

        melee(VANILLA_SWORD_DAMAGE);
        server.getScheduler().performOneTick();
        Hit next = hitWithoutRefresh(meleeEvent(attacker, victim, VANILLA_SWORD_DAMAGE), victim);

        assertEquals(Map.of("physical", 20D, "fire", 10D), next.damages());
    }

    @Test
    void aoe_withNobodyInRange_dealsNothingExtra() {
        attacker.getInventory().setItemInMainHand(aoeSword(50));
        FiringPlayer bystander = bystander();
        bystander.teleport(bystander.getLocation().add(10, 0, 0));

        melee(VANILLA_SWORD_DAMAGE);
        server.getScheduler().performOneTick();
        Hit next = hitWithoutRefresh(meleeEvent(attacker, victim, VANILLA_SWORD_DAMAGE), victim);

        assertTrue(bystander.events.isEmpty());
        assertEquals(Map.of("physical", 20D, "fire", 10D), next.damages());
    }

    @Test
    void damageAction_flatAmount_isAddedToEveryWeaponDamageType() throws Exception {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D, "fire", 10D)));
        refresh(attacker);
        refresh(victim);
        firedEvents.clear();

        runDamageAction(attacker, victim, 5, false);

        assertEquals(1, victim.events.size());
        assertSame(attacker, ((EntityDamageByEntityEvent) victim.lastEvent).getDamager());
        assertEquals(Map.of("physical", 25D, "fire", 15D), damagesOf(victim));
        assertEquals(40D, victim.lastEvent.getFinalDamage(), DELTA);
        assertEquals(FULL_PIPELINE, firedEvents);
    }

    @Test
    void damageAction_negativeFlatAmount_isTakenFromEveryWeaponDamageType() throws Exception {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D, "fire", 10D)));
        refresh(attacker);
        refresh(victim);

        runDamageAction(attacker, victim, -5, false);

        assertEquals(Map.of("physical", 15D, "fire", 5D), damagesOf(victim));
    }

    @Test
    void damageAction_percentAmount_scalesEveryWeaponDamageType() throws Exception {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D, "fire", 10D)));
        refresh(attacker);
        refresh(victim);

        runDamageAction(attacker, victim, 50, true);

        assertEquals(Map.of("physical", 10D, "fire", 5D), damagesOf(victim));
        assertEquals(15D, victim.lastEvent.getFinalDamage(), DELTA);
    }

    @Test
    void damageAction_leavesTheNextSwingAtFullDamage() throws Exception {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D, "fire", 10D)));
        refresh(attacker);
        refresh(victim);

        runDamageAction(attacker, victim, 50, true);
        Hit next = hitWithoutRefresh(meleeEvent(attacker, victim, VANILLA_SWORD_DAMAGE), victim);

        assertEquals(Map.of("physical", 20D, "fire", 10D), next.damages());
    }

    @Test
    void damageAction_zeroAmount_dealsNoDamageAtAll() throws Exception {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh(attacker);
        refresh(victim);

        runDamageAction(attacker, victim, 0, false);

        assertTrue(victim.events.isEmpty());
    }

    @Test
    void damageAction_runByAProjectile_isAMeleeHitFromItsShooter() throws Exception {
        ItemStack bow = bow(20);
        Arrow arrow = arrowFrom(bow, 1);
        refresh(attacker);
        refresh(victim);

        runDamageAction(arrow, victim, 5, false);

        assertSame(attacker, ((EntityDamageByEntityEvent) victim.lastEvent).getDamager());
        // The shooter holds a bow, and a bow in hand does not deal its damage in melee by default. So the hit
        // is the 1 damage the action asks for, and the action's amount is not applied to it.
        assertEquals(Map.of("physical", 1D), damagesOf(victim));
    }

    @Test
    void bleed_ticksForAShareOfTheHitAsUnattributedDamage() {
        ItemStack sword = weapon(Map.of("physical", 20D));
        stat(sword, TypedStat.Type.BLEED_RATE, 100);
        attacker.getInventory().setItemInMainHand(sword);
        liftCap(TypedStat.Type.BLEED_RATE);

        melee(VANILLA_SWORD_DAMAGE);
        assertTrue(victim.events.isEmpty(), "Bleeding starts on a later tick, not inside the hit");

        firedEvents.clear();
        // The bleed plays particles, which MockBukkit does not implement
        try (MockedStatic<EffectUT> ignored = mockStatic(EffectUT.class)) {
            server.getScheduler().performOneTick();
        }

        // The default formula is half of the hit's damage per tick
        assertEquals(1, victim.events.size());
        assertFalse(victim.lastEvent instanceof EntityDamageByEntityEvent, "Bleeding has no attacker");
        assertEquals(DamageCause.CUSTOM, victim.lastEvent.getCause());
        assertEquals(Map.of("physical", 10D), damagesOf(victim));
        assertEquals(10D, victim.lastEvent.getFinalDamage(), DELTA);
        assertEquals(FULL_PIPELINE, firedEvents, "A bleed tick runs the whole pipeline as environmental damage");
    }

    @Test
    void bleed_tickIsReducedByTheVictimsDefense() {
        ItemStack sword = weapon(Map.of("physical", 20D));
        stat(sword, TypedStat.Type.BLEED_RATE, 100);
        attacker.getInventory().setItemInMainHand(sword);
        liftCap(TypedStat.Type.BLEED_RATE);

        melee(VANILLA_SWORD_DAMAGE);
        // Put the armor on after the hit, so the bleed is still sized by an undefended 20
        victim.getInventory().setChestplate(armor(Map.of("physical", 10D)));
        // Not refresh(): that would drop the victim's stats, and the bleed with them
        EntityStats.get(victim).updateAll();
        try (MockedStatic<EffectUT> ignored = mockStatic(EffectUT.class)) {
            server.getScheduler().performOneTick();
        }

        // 10 bleed against 10 defense, current formula with the default protection factor of 0.8:
        // 10 * (1 - max(10 / 5, 10 - 4 * 10 / 8) * 0.8 * 0.05) = 8
        assertEquals(8D, victim.lastEvent.getFinalDamage(), DELTA);
    }

    @Test
    void fishingHook_dealsAOneDamageHitFromThePlayer_managedLikeAWeaponSwing() {
        ItemStack rod = new ItemStack(Material.FISHING_ROD);
        Objects.requireNonNull(ItemStats.getDamageById("physical"))
                .add(rod, new StatBonus(new double[]{20}, false, null), -1);
        attacker.getInventory().setItemInMainHand(rod);
        refresh(attacker);
        refresh(victim);
        assertTrue(EngineCfg.COMBAT_FISHING_HOOK_DO_DAMAGE, "Test setup: the default config enables hook damage");

        server.getPluginManager().callEvent(new PlayerFishEvent(attacker,
                victim,
                mock(FishHook.class),
                PlayerFishEvent.State.CAUGHT_ENTITY));

        assertEquals(1, victim.events.size());
        assertSame(attacker, ((EntityDamageByEntityEvent) victim.lastEvent).getDamager());
        // The rod's 20, plus the 1 the hook asks for (a rod has no vanilla damage to deduct)
        assertEquals(Map.of("physical", 21D), damagesOf(victim));
    }

    @Test
    void fishingHook_doesNothingWhenDisabled() {
        EngineCfg.COMBAT_FISHING_HOOK_DO_DAMAGE = false;
        try {
            server.getPluginManager().callEvent(new PlayerFishEvent(attacker,
                    victim,
                    mock(FishHook.class),
                    PlayerFishEvent.State.CAUGHT_ENTITY));

            assertTrue(victim.events.isEmpty());
        } finally {
            EngineCfg.COMBAT_FISHING_HOOK_DO_DAMAGE = true;
        }
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
                    "Exit is not fired for ignore-divinity damage, so the hit costs no item durability");
        } finally {
            hook.ignoreDivinity(attacker, false);
            plugin.getHooks().getHooks(plugin).remove(hook);
        }
    }

    // ------------------------------------------------------------------ //
    // Modifiers configured per damage type
    // ------------------------------------------------------------------ //

    @Test
    void entityTypeModifier_scalesDamageAgainstThatEntityType() throws Exception {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        ZombieMock          zombie    = new ZombieMock(server, UUID.randomUUID());
        Map<String, Double> modifiers = entityTypeModifiers("physical");
        Double              previous  = modifiers.put("ZOMBIE", 1.5D);
        try {
            assertEquals(30D,
                    hit(meleeEvent(attacker, zombie, VANILLA_SWORD_DAMAGE), zombie).damage("physical"),
                    DELTA);
            assertEquals(20D, melee(VANILLA_SWORD_DAMAGE).damage("physical"), DELTA, "Players are not zombies");
        } finally {
            restore(modifiers, "ZOMBIE", previous);
        }
    }

    @Test
    void biomeModifier_scalesDamageDealtInThatBiome() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        String              biome     = EnumUT.getName(attacker.getLocation().getBlock().getBiome());
        Map<String, Double> modifiers = ItemStats.getDamageById("physical").getBiomeDamageModifiers();
        Double              previous  = modifiers.put(biome, 1.5D);
        try {
            assertEquals(30D, melee(VANILLA_SWORD_DAMAGE).damage("physical"), DELTA);
        } finally {
            restore(modifiers, biome, previous);
        }
    }

    // ------------------------------------------------------------------ //
    // Attack cooldown, generated items and damage ranges
    // ------------------------------------------------------------------ //

    @Test
    void attackCooldown_scalesMeleeDamageByAttackPower() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh(attacker);
        refresh(victim);
        // What EntityStatsTask records from Player#getAttackCooldown for a half-charged swing
        EntityStats.get(attacker).setAttackPower(0.5D);

        Hit hit = hitWithoutRefresh(meleeEvent(attacker, victim, VANILLA_SWORD_DAMAGE), victim);

        assertEquals(10D, hit.damage("physical"), DELTA);
    }

    @Test
    void attackCooldown_configuredModifierReplacesAttackPower() {
        double original = EngineCfg.COMBAT_DAMAGE_MODIFIER_FOR_COOLDOWN;
        EngineCfg.COMBAT_DAMAGE_MODIFIER_FOR_COOLDOWN = 0.2D;
        try {
            attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
            refresh(attacker);
            refresh(victim);
            EntityStats.get(attacker).setAttackPower(0.5D);

            Hit hit = hitWithoutRefresh(meleeEvent(attacker, victim, VANILLA_SWORD_DAMAGE), victim);

            // Any swing that is not fully charged uses the configured modifier, however charged it is
            assertEquals(4D, hit.damage("physical"), DELTA);
        } finally {
            EngineCfg.COMBAT_DAMAGE_MODIFIER_FOR_COOLDOWN = original;
        }
    }

    @Test
    void generatedItem_alsoDeductsTheAttackersBaseAttackDamageFromExtraDamage() {
        ItemStack sword = weapon(Map.of("physical", 20D));
        attacker.getInventory().setItemInMainHand(sword);
        setBaseAttackDamage(attacker, 2D);

        // ItemStats resolves an item's module through a plugin instance it captured when the class was
        // first loaded. With several test classes in one JVM that instance is a previous, disabled plugin,
        // so the real lookup cannot recognise an ItemGenerator item here. Answer that one lookup directly;
        // what is being pinned is what the damage listener does once an item is recognised.
        ItemGeneratorManager generator = plugin.getModuleCache().getTierManager();
        try (MockedStatic<ItemStats> ignored = mockStatic(ItemStats.class, invocation ->
                invocation.getMethod().getName().equals("getModule")
                        ? (sword.isSimilar(invocation.getArgument(0)) ? generator : null)
                        : invocation.callRealMethod())) {
            // Event 9, of which 6 is the sword and 2 the attacker's own base attack damage: 1 is "extra"
            assertEquals(Map.of("physical", 21D), melee(VANILLA_SWORD_DAMAGE + 3).damages());
        }
    }

    @Test
    void handBuiltItem_doesNotDeductTheAttackersBaseAttackDamage() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        setBaseAttackDamage(attacker, 2D);

        assertEquals(Map.of("physical", 23D), melee(VANILLA_SWORD_DAMAGE + 3).damages());
    }

    @Test
    void damageRange_rollsBetweenMinimumAndMaximum() {
        ItemStack sword = new ItemStack(Material.IRON_SWORD);
        Objects.requireNonNull(ItemStats.getDamageById("physical"))
                .add(sword, new StatBonus(new double[]{10D, 20D}, false, null), -1);
        attacker.getInventory().setItemInMainHand(sword);

        Set<Double> rolls = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            double damage = melee(VANILLA_SWORD_DAMAGE).damage("physical");
            assertTrue(damage >= 10D && damage <= 20D, "Rolled outside the range: " + damage);
            rolls.add(damage);
        }
        assertTrue(rolls.size() > 1, "A range should not always roll the same value");
    }

    // ------------------------------------------------------------------ //
    // Scaling through Codex's attribute and buff registries (how Fabled feeds in)
    // ------------------------------------------------------------------ //

    @Test
    void codexScaling_physicalMelee_asksForTheseKeysInThisOrder() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        victim.getInventory().setChestplate(armor(Map.of("physical", 10D)));

        List<String> calls = recordScaling(() -> melee(VANILLA_SWORD_DAMAGE));

        assertEquals(List.of(
                "buff DIVINITY_damage_physical-damage Attacker",
                "buff DIVINITY_damage_physical Attacker",
                "attribute physical-damage Attacker",
                "buff DIVINITY_damage_melee-damage Attacker",
                "attribute melee-damage Attacker",
                "damage-for-defense physical-damage Victim",
                "damage-for-defense DIVINITY_physical Victim",
                // The first buff key and the attribute key are the wrong way round compared with the damage
                // side. They are kept for servers that rely on them; the documented buff key follows.
                "buff physical-defense Victim",
                "buff DIVINITY_defense_physical Victim",
                "attribute DIVINITY_defense_physical-defense Victim",
                "buff DIVINITY_defense_melee-defense Victim",
                "attribute melee-defense Victim"), calls);
    }

    @Test
    void codexScaling_customDamageType_asksForTheseKeysInThisOrder() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("fire", 10D)));

        List<String> calls = recordScaling(() -> melee(VANILLA_SWORD_DAMAGE));

        assertEquals(List.of(
                "buff DIVINITY_damage_fire Attacker",
                "attribute rpgdamage-fire Attacker",
                "buff DIVINITY_damage_melee-damage Attacker",
                "attribute melee-damage Attacker",
                "damage-for-defense rpgdefense-fire Victim",
                "damage-for-defense DIVINITY_fire Victim",
                "buff rpgdefense-fire Victim",
                "buff DIVINITY_defense_fire Victim",
                "attribute DIVINITY_defense_fire Victim",
                "buff DIVINITY_defense_melee-defense Victim",
                "attribute melee-defense Victim"), calls);
    }

    @Test
    void codexScaling_projectile_usesProjectileKeysInsteadOfMelee() {
        Arrow arrow = arrowFrom(bow(15D), 1D);

        List<String> calls = recordScaling(() -> hit(new EntityDamageByEntityEvent(arrow,
                victim,
                DamageCause.PROJECTILE,
                modifiers(4),
                functions(modifiers(4))), victim));

        assertEquals(List.of(
                "buff DIVINITY_damage_physical-damage Attacker",
                "buff DIVINITY_damage_physical Attacker",
                "attribute physical-damage Attacker",
                "buff DIVINITY_damage_projectile-damage Attacker",
                "attribute projectile-damage Attacker",
                "damage-for-defense physical-damage Victim",
                "damage-for-defense DIVINITY_physical Victim",
                "buff physical-defense Victim",
                "buff DIVINITY_defense_physical Victim",
                "attribute DIVINITY_defense_physical-defense Victim",
                "buff DIVINITY_defense_projectile-defense Victim",
                "attribute projectile-defense Victim"), calls);
    }

    @Test
    void codexScaling_mobAttacker_onlyShieldsAndTheDefendingPlayerAreAsked() {
        ZombieMock zombie = new ZombieMock(server, UUID.randomUUID());
        victim.getInventory().setChestplate(armor(Map.of("physical", 10D)));

        List<String> calls = recordScaling(() -> {
            refresh(victim);
            firedEvents.clear();
            server.getPluginManager().callEvent(meleeEvent(zombie, victim, 4));
        });

        assertEquals(List.of(
                "damage-for-defense physical-damage Victim",
                "damage-for-defense DIVINITY_physical Victim",
                "buff physical-defense Victim",
                "buff DIVINITY_defense_physical Victim",
                "attribute DIVINITY_defense_physical-defense Victim",
                "buff DIVINITY_defense_melee-defense Victim",
                "attribute melee-defense Victim"), calls);
    }

    @Test
    void codexScaling_citizensNpcAttacker_isNotScaled() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        victim.getInventory().setChestplate(armor(Map.of("physical", 10D)));

        // Citizens is not installed here, so answer "is this a Citizens NPC?" for the attacker by hand
        List<String> calls;
        try (MockedStatic<Hooks> ignored = mockStatic(Hooks.class, invocation ->
                invocation.getMethod().getName().equals("isNPC")
                        ? attacker.equals(invocation.getArgument(0))
                        : invocation.callRealMethod())) {
            calls = recordScaling(() -> melee(VANILLA_SWORD_DAMAGE));
        }

        assertEquals(List.of(
                "damage-for-defense physical-damage Victim",
                "damage-for-defense DIVINITY_physical Victim",
                "buff physical-defense Victim",
                "buff DIVINITY_defense_physical Victim",
                "attribute DIVINITY_defense_physical-defense Victim",
                "buff DIVINITY_defense_melee-defense Victim",
                "attribute melee-defense Victim"), calls);
    }

    @Test
    void codexScaling_documentedDefenseBuffKey_raisesThatDefense() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("fire", 20D)));
        victim.getInventory().setChestplate(armor(Map.of("fire", 10D)));

        // Fire defense 10 against 20: 1 - max(10 / 5, 10 - 4 * 20 / 8) * 0.8 * 0.05 = 0.92
        assertEquals(18.4D, melee(VANILLA_SWORD_DAMAGE).damage("fire"), DELTA);

        // What a Fabled Buff mechanic of type "DIVINITY_defense_fire" with a x2 multiplier does
        BuffProvider doubleFireDefense = (name, entity, value) ->
                name.equals("DIVINITY_defense_fire") ? value * 2 : value;
        BuffRegistry.registerProvider(doubleFireDefense);
        try {
            // Fire defense 20 against 20: 1 - max(20 / 5, 20 - 10) * 0.8 * 0.05 = 0.6
            assertEquals(12D, melee(VANILLA_SWORD_DAMAGE).damage("fire"), DELTA);
        } finally {
            BuffRegistry.unregisterProvider(doubleFireDefense);
        }
    }

    @Test
    void codexScaling_documentedPhysicalDamageBuffKey_scalesPhysicalDamage() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        BuffProvider doublePhysical = (name, entity, value) ->
                name.equals("DIVINITY_damage_physical") ? value * 2 : value;
        BuffRegistry.registerProvider(doublePhysical);
        try {
            assertEquals(40D, melee(VANILLA_SWORD_DAMAGE).damage("physical"), DELTA);
        } finally {
            BuffRegistry.unregisterProvider(doublePhysical);
        }
    }

    @Test
    void codexScaling_documentedShieldClassifier_absorbsThatDamageType() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("fire", 20D)));
        // What a Fabled Shield mechanic with classifier "DIVINITY_fire" and 5 left to absorb does
        BuffProvider fireShield = new BuffProvider() {
            @Override
            public double scaleValue(String name, @NotNull LivingEntity entity, double value) {
                return value;
            }

            @Override
            public double scaleDamageForDefense(String name, @NotNull LivingEntity entity, double damage) {
                return name.equals("DIVINITY_fire") ? Math.max(0, damage - 5) : damage;
            }
        };
        BuffRegistry.registerProvider(fireShield);
        try {
            assertEquals(15D, melee(VANILLA_SWORD_DAMAGE).damage("fire"), DELTA);
        } finally {
            BuffRegistry.unregisterProvider(fireShield);
        }
    }

    @Test
    void codexScaling_shieldAbsorbsAMobsHit() {
        ZombieMock zombie = new ZombieMock(server, UUID.randomUUID());
        BuffProvider physicalShield = new BuffProvider() {
            @Override
            public double scaleValue(String name, @NotNull LivingEntity entity, double value) {
                return value;
            }

            @Override
            public double scaleDamageForDefense(String name, @NotNull LivingEntity entity, double damage) {
                return name.equals("DIVINITY_physical") ? Math.max(0, damage - 3) : damage;
            }
        };
        BuffRegistry.registerProvider(physicalShield);
        try {
            refresh(victim);
            server.getPluginManager().callEvent(meleeEvent(zombie, victim, 8));

            assertEquals(Map.of("physical", 5D), damagesOf(victim));
        } finally {
            BuffRegistry.unregisterProvider(physicalShield);
        }
    }

    @Test
    void codexScaling_shieldsAreNotAskedAboutEnvironmentalDamage() {
        List<String> calls = recordScaling(() -> hit(environmentEvent(DamageCause.FIRE, 4), victim));

        assertFalse(calls.stream().anyMatch(call -> call.startsWith("damage-for-defense")), calls.toString());
    }

    @Test
    void codexScaling_resultsFeedIntoTheCalculation() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        AttributeProvider doubleMelee = (name, entity, value) ->
                name.equals(AttributeRegistry.MELEE_DAMAGE) ? value * 2 : value;
        AttributeRegistry.registerProvider(doubleMelee);
        try {
            assertEquals(40D, melee(VANILLA_SWORD_DAMAGE).damage("physical"), DELTA);
        } finally {
            AttributeRegistry.unregisterProvider(doubleMelee);
        }
    }

    // ------------------------------------------------------------------ //
    // Helpers
    // ------------------------------------------------------------------ //

    private static boolean isIronSword(@Nullable ItemStack item) {
        return item != null && item.getType() == Material.IRON_SWORD;
    }

    /**
     * The server and its world are shared by every test in this class, and entity stats are static. Without
     * this, a later test would find this test's players standing next to its own, their bleeds still
     * ticking, and any hit this test scheduled for the next tick still waiting to land.
     */
    private void leaveNothingBehind() {
        int offset = 0;
        for (PlayerMock player : players) {
            // Far from the spawn and from each other, so nothing still queued can reach anyone
            player.teleport(player.getLocation().add(10_000 + 100 * offset++, 0, 0));
        }
        try (MockedStatic<EffectUT> ignored = mockStatic(EffectUT.class)) {
            server.getScheduler().performOneTick();
        }
        players.forEach(EntityStats::purge);
    }

    private ItemStack aoeSword(double aoePercent) {
        ItemStack sword = weapon(Map.of("physical", 20D, "fire", 10D));
        stat(sword, TypedStat.Type.AOE_DAMAGE, aoePercent);
        return sword;
    }

    /**
     * A second player standing where the attacker stands, in reach of area damage.
     */
    private FiringPlayer bystander() {
        FiringPlayer bystander = new FiringPlayer(server, "Bystander");
        server.addPlayer(bystander);
        players.add(bystander);
        bystander.teleport(attacker.getLocation());
        refresh(bystander);
        return bystander;
    }

    /**
     * Runs Divinity's DAMAGE action the way the actions engine would.
     */
    private void runDamageAction(Entity executor, Entity target, double amount, boolean percent) throws Exception {
        IParamValue value = mock(IParamValue.class);
        when(value.getDouble(anyDouble())).thenReturn(amount);
        when(value.getBoolean()).thenReturn(percent);
        IParamResult result = mock(IParamResult.class);
        when(result.getParamValue(IParamType.AMOUNT)).thenReturn(value);

        Method execute =
                ActionDamage.class.getDeclaredMethod("execute", Entity.class, Set.class, IParamResult.class);
        execute.setAccessible(true);
        execute.invoke(new ActionDamage(plugin), executor, new HashSet<>(Set.of(target)), result);
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

    private Hit hitWithoutRefresh(EntityDamageEvent event, LivingEntity target) {
        firedEvents.clear();
        server.getPluginManager().callEvent(event);
        return new Hit(event, List.copyOf(firedEvents), EntityStats.get(target).getLastDamageMeta());
    }

    /**
     * Runs the action with providers registered on Codex's registries that change nothing but note every
     * key they are asked to scale, in order.
     */
    private List<String> recordScaling(Runnable action) {
        List<String> calls = new ArrayList<>();
        AttributeProvider attributes = (name, entity, value) -> {
            calls.add("attribute " + name + " " + entity.getName());
            return value;
        };
        BuffProvider buffs = new BuffProvider() {
            @Override
            public double scaleValue(String name, @NotNull LivingEntity entity, double value) {
                calls.add("buff " + name + " " + entity.getName());
                return value;
            }

            @Override
            public double scaleDamageForDefense(String name, @NotNull LivingEntity entity, double damage) {
                calls.add("damage-for-defense " + name + " " + entity.getName());
                return damage;
            }
        };
        AttributeRegistry.registerProvider(attributes);
        BuffRegistry.registerProvider(buffs);
        try {
            action.run();
        } finally {
            AttributeRegistry.unregisterProvider(attributes);
            BuffRegistry.unregisterProvider(buffs);
        }
        return calls;
    }

    /**
     * The per-entity-type damage modifiers of a damage type. They are only loaded from config and have no
     * accessor, so the test reaches the map directly.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Double> entityTypeModifiers(String damageType) throws Exception {
        Field field = DamageAttribute.class.getDeclaredField("entityTypeModifier");
        field.setAccessible(true);
        return (Map<String, Double>) field.get(ItemStats.getDamageById(damageType));
    }

    private void setBaseAttackDamage(PlayerMock player, double value) {
        // MockBukkit players start without this attribute
        if (player.getAttribute(Attribute.ATTACK_DAMAGE) == null) player.registerAttribute(Attribute.ATTACK_DAMAGE);
        Objects.requireNonNull(player.getAttribute(Attribute.ATTACK_DAMAGE)).setBaseValue(value);
    }

    private static void restore(Map<String, Double> map, String key, @Nullable Double previous) {
        if (previous == null) map.remove(key);
        else map.put(key, previous);
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
     * Ticking the scheduler runs Divinity's stats task, which reads every tracked player's attack cooldown.
     * MockBukkit does not implement that, and an unimplemented call makes a test report as skipped.
     */
    private static class TickSafePlayer extends PlayerMock {
        TickSafePlayer(@NotNull ServerMock server, @NotNull String name) {
            super(server, name, UUID.randomUUID());
        }

        @Override
        public float getAttackCooldown() {
            return 1F;
        }
    }

    /**
     * MockBukkit's {@code damage(amount, source)} changes health without firing a damage event, so nothing
     * a plugin listens for would run. This player fires the event a real server would, and remembers it.
     */
    private static class FiringPlayer extends TickSafePlayer {
        private final Map<Material, Integer> cooldowns = new EnumMap<>(Material.class);
        private final List<EntityDamageEvent> events    = new ArrayList<>();
        private       EntityDamageEvent      lastEvent;

        FiringPlayer(@NotNull ServerMock server, @NotNull String name) {
            super(server, name);
        }

        @Override
        public void damage(double amount, @Nullable Entity source) {
            if (source == null) {
                lastEvent = new EntityDamageEvent(this,
                        DamageCause.CUSTOM,
                        modifiers(amount),
                        functions(modifiers(amount)));
                events.add(lastEvent);
                server.getPluginManager().callEvent(lastEvent);
                return;
            }
            lastEvent = new EntityDamageByEntityEvent(source,
                    this,
                    DamageCause.ENTITY_ATTACK,
                    modifiers(amount),
                    functions(modifiers(amount)));
            events.add(lastEvent);
            server.getPluginManager().callEvent(lastEvent);
        }

        @Override
        public void damage(double amount) {
            damage(amount, (Entity) null);
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
        private double armorSeenAtBeforeScale = Double.NaN;
        private double finalDamageSeenAtExit  = Double.NaN;

        @EventHandler(priority = EventPriority.LOWEST)
        public void on(DivinityDamageEvent.BeforeScale event) {
            firedEvents.add("BeforeScale");
            EntityDamageEvent original = event.getOriginalEvent();
            if (original.isApplicable(DamageModifier.ARMOR)) {
                armorSeenAtBeforeScale = original.getDamage(DamageModifier.ARMOR);
            }
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
            finalDamageSeenAtExit = event.getOriginalEvent().getFinalDamage();
        }
    }
}
