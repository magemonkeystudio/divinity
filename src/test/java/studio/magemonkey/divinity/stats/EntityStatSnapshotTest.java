package studio.magemonkey.divinity.stats;

import com.google.common.base.Function;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.entity.ZombieMock;
import org.mockito.MockedStatic;
import studio.magemonkey.codex.compat.VersionManager;
import studio.magemonkey.codex.util.EffectUT;
import studio.magemonkey.codex.util.EnumUT;
import studio.magemonkey.codex.util.random.Rnd;
import studio.magemonkey.divinity.api.event.DivinityProjectileLaunchEvent;
import studio.magemonkey.divinity.api.event.EntityEquipmentChangeEvent;
import studio.magemonkey.divinity.config.EngineCfg;
import studio.magemonkey.divinity.data.api.DivinityUser;
import studio.magemonkey.divinity.data.api.UserProfile;
import studio.magemonkey.divinity.manager.damage.DamageMeta;
import studio.magemonkey.divinity.manager.effects.buffs.SavedBuff;
import studio.magemonkey.divinity.manager.effects.main.AdjustStatEffect;
import studio.magemonkey.divinity.modules.ModuleCache;
import studio.magemonkey.divinity.modules.list.sets.SetManager;
import studio.magemonkey.divinity.stats.bonus.BonusMap;
import studio.magemonkey.divinity.stats.bonus.StatBonus;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.stats.items.attributes.DamageAttribute;
import studio.magemonkey.divinity.stats.items.attributes.api.SimpleStat;
import studio.magemonkey.divinity.stats.items.attributes.api.TypedStat;
import studio.magemonkey.divinity.testutil.MockedTest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Entity stat snapshots (issue #372). A snapshot keeps what was worked out from an entity's gear so a hit does
 * not read every item again. What matters is that it never outlives a change: for each way stats can change
 * there is a test that warms the cache with a hit, makes the change the way the server would, and checks the
 * very next hit.
 * <p>
 * Every damage value is a single number so the results can be asserted exactly. The tests that are about
 * rolls seed the generator.
 */
@SuppressWarnings("deprecation")
class EntityStatSnapshotTest extends MockedTest {
    private static final double DELTA                = 1e-6;
    private static final double VANILLA_SWORD_DAMAGE = 6D;

    private TickSafePlayer attacker;
    private TickSafePlayer victim;
    private int            originalMaxAge;

    @BeforeEach
    void setup() {
        attacker = new TickSafePlayer(server, "Attacker");
        server.addPlayer(attacker);
        players.add(attacker);
        victim = new TickSafePlayer(server, "Victim");
        server.addPlayer(victim);
        players.add(victim);

        originalMaxAge = EngineCfg.PERFORMANCE_STAT_SNAPSHOT_MAX_AGE;

        // Adding a stat to an item also syncs vanilla attribute modifiers onto it, which the mocked NMS layer
        // cannot do
        EngineCfg.VANILLA_ONLY_ITEM_STATS = true;
        doAnswer(ans -> isIronSword(ans.getArgument(0)) ? VANILLA_SWORD_DAMAGE : 0D)
                .when(VersionManager.getNms()).getDefaultDamage(any());
        doAnswer(ans -> isIronSword(ans.getArgument(0))).when(VersionManager.getNms()).isWeapon(any());
        // Without this an entity's max health reads as 0 and refreshing its stats kills it
        doAnswer(ans -> Registry.ATTRIBUTE.get(NamespacedKey.minecraft(((String) ans.getArgument(0)).toLowerCase(
                Locale.US)))).when(VersionManager.getNms()).getAttribute(anyString());

        // Joining asks for a forced refresh on the next tick. Let it run now, or it would run in the middle of a
        // test and drop the snapshot the test is looking at
        tick();
    }

    @AfterEach
    void tearDown() {
        EntityStats.resetClock();
        EngineCfg.PERFORMANCE_STAT_SNAPSHOT_MAX_AGE = originalMaxAge;
        EngineCfg.VANILLA_ONLY_ITEM_STATS = false;
        EngineCfg.LEGACY_COMBAT = false;

        // The server is shared by every test in this class and entity stats are static: take the players out
        // of reach of anything still queued, and let it run
        int offset = 0;
        for (PlayerMock player : players) {
            player.teleport(player.getLocation().add(10_000 + 100 * offset++, 0, 0));
        }
        try (MockedStatic<EffectUT> ignored = mockStatic(EffectUT.class)) {
            server.getScheduler().performOneTick();
        }
        players.forEach(EntityStats::purge);
    }

    // ------------------------------------------------------------------ //
    // The cache is used, and a hit does not throw it away
    // ------------------------------------------------------------------ //

    @Test
    void ahitFillsTheSnapshotsOfBothFighters() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh();
        assertFalse(EntityStats.get(attacker).hasSnapshot());

        hit();

        assertTrue(EntityStats.get(attacker).hasSnapshot());
        assertTrue(EntityStats.get(victim).hasSnapshot());
    }

    @Test
    void theRefreshAfterAHit_keepsTheSnapshotOfUnchangedGear() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        victim.getInventory().setChestplate(armor(Map.of("physical", 30D)));
        refresh();
        hit();

        tick(); // The hit asked for both fighters' stats to be read again on the next tick

        assertTrue(EntityStats.get(attacker).hasSnapshot(), "A hit changes nothing about the attacker's gear");
        assertTrue(EntityStats.get(victim).hasSnapshot(), "A hit changes nothing about the victim's gear");
    }

    @Test
    void wear_isNotAChange() {
        ItemStack sword = weapon(Map.of("physical", 20D));
        attacker.getInventory().setItemInMainHand(sword);
        refresh();
        hit();
        tick();

        // Every hit wears the weapon down a little
        ItemStack worn = attacker.getInventory().getItemInMainHand();
        ItemMeta  meta = worn.getItemMeta();
        ((Damageable) meta).setDamage(5);
        worn.setItemMeta(meta);
        attacker.getInventory().setItemInMainHand(worn);
        hit();
        tick();

        assertTrue(EntityStats.get(attacker).hasSnapshot(), "Wear should not cost the attacker their snapshot");
    }

    @Test
    void anythingElseAboutAnItem_isAChange() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh();
        hit();
        tick();

        ItemStack enchanted = attacker.getInventory().getItemInMainHand();
        enchanted.addUnsafeEnchantment(Enchantment.SHARPNESS, 1);
        attacker.getInventory().setItemInMainHand(enchanted);
        hit();
        tick();

        assertFalse(EntityStats.get(attacker).hasSnapshot());
    }

    // ------------------------------------------------------------------ //
    // Invalidation: one test for each way stats change
    // ------------------------------------------------------------------ //

    @Test
    void heldItemChange_isSeenByTheNextHit() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh();
        assertEquals(20D, physicalHit(), DELTA);
        tick();

        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 30D)));
        server.getPluginManager().callEvent(new PlayerItemHeldEvent(attacker, 0, 1));
        tick();

        assertEquals(30D, physicalHit(), DELTA);
    }

    @Test
    void equipmentChange_isSeenByTheNextHit() {
        EngineCfg.LEGACY_COMBAT = true;
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        victim.getInventory().setChestplate(armor(Map.of("physical", 30D)));
        refresh();
        assertEquals(15.2D, physicalHit(), DELTA);
        tick();

        victim.getInventory().setChestplate(armor(Map.of("physical", 50D)));
        server.getPluginManager().callEvent(new EntityEquipmentChangeEvent(victim));
        tick();

        assertEquals(12D, physicalHit(), DELTA); // 20 * (1 - 50 * 0.8 * 0.01)
    }

    @Test
    void anItemChangedInPlace_gemsAndSocketsIncluded_isSeenByTheNextHit() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh();
        assertEquals(20D, physicalHit(), DELTA);
        tick();

        // What socketing a gem does to the stack a player is holding: the same item, with different stats on it
        ItemStack held = attacker.getInventory().getItemInMainHand();
        Objects.requireNonNull(ItemStats.getDamageById("physical"))
                .add(held, new StatBonus(new double[]{30D}, false, null), -1);
        attacker.getInventory().setItemInMainHand(held);
        server.getPluginManager().callEvent(new EntityEquipmentChangeEvent(attacker));
        tick();

        assertEquals(30D, physicalHit(), DELTA);
    }

    @Test
    void gearChangedWithoutAnyEvent_isSeenOnceTheHitsRefreshHasRun() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh();
        assertEquals(20D, physicalHit(), DELTA);

        // A plugin sets the weapon and tells no one. This is how it has always been: the hit that follows still
        // uses the gear that was read last, and the refresh the hit asks for puts it right
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 30D)));
        assertEquals(20D, physicalHit(), DELTA);
        tick();

        assertEquals(30D, physicalHit(), DELTA);
    }

    @Test
    void aForcedRefresh_dropsTheSnapshotEvenIfNothingLooksDifferent() {
        // What classes, profiles and sets ask for when they change something that is not equipment
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh();
        hit();
        assertTrue(EntityStats.get(attacker).hasSnapshot());

        EntityStats.get(attacker).updateAll();

        assertFalse(EntityStats.get(attacker).hasSnapshot());
    }

    @Test
    void setBonus_isSeenByTheNextHit_whenTheRefreshIsForced() throws Exception {
        BonusMap setBonus = physicalDamageBonus(10D);
        List<BonusMap> active = new ArrayList<>();
        try (SetManagerInstalled ignored = installSetManager(active)) {
            attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
            refresh();
            assertEquals(20D, physicalHit(), DELTA);
            tick();

            active.add(setBonus); // The last piece of the set goes on
            EntityStats.get(attacker).updateAll(); // What SetManager asks for when equipment changes

            assertEquals(30D, physicalHit(), DELTA);
        }
    }

    @Test
    void setBonus_changeIsSeenAfterAHit_withoutAForcedRefresh() throws Exception {
        BonusMap setBonus = physicalDamageBonus(10D);
        List<BonusMap> active = new ArrayList<>(List.of(setBonus));
        try (SetManagerInstalled ignored = installSetManager(active)) {
            attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
            refresh();
            assertEquals(30D, physicalHit(), DELTA);

            active.clear(); // A set piece is gone, and the equipment itself looks the same
            hit();
            tick();

            assertEquals(20D, physicalHit(), DELTA);
        }
    }

    @Test
    void effectAdded_isSeenByTheNextHit_andNoLongerOnceRemoved() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh();
        assertEquals(20D, physicalHit(), DELTA);

        AdjustStatEffect effect = new AdjustStatEffect.Builder(-1)
                .withAdjust(Objects.requireNonNull(ItemStats.getDamageById("physical")), v -> v + 10D)
                .build();
        EntityStats.get(attacker).addEffect(effect);
        assertEquals(30D, physicalHit(), DELTA);

        EntityStats.get(attacker).removeEffect(effect);
        assertEquals(20D, physicalHit(), DELTA);
    }

    @Test
    void effectThatRunsOut_isNoLongerSeenByTheNextHit() throws Exception {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh();
        AdjustStatEffect effect = new AdjustStatEffect.Builder(0.5)
                .withAdjust(Objects.requireNonNull(ItemStats.getDamageById("physical")), v -> v + 10D)
                .build();
        EntityStats.get(attacker).addEffect(effect);
        assertEquals(30D, physicalHit(), DELTA);

        Thread.sleep(700);

        assertEquals(20D, physicalHit(), DELTA);
    }

    @Test
    void effectWithOneCharge_isUsedUpByTheFirstHit() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh();
        assertEquals(20D, physicalHit(), DELTA);

        EntityStats.get(attacker).addEffect(new AdjustStatEffect.Builder(-1)
                .withCharges(1)
                .withAdjust(Objects.requireNonNull(ItemStats.getDamageById("physical")), v -> v + 10D)
                .build());

        assertEquals(30D, physicalHit(), DELTA);
        assertEquals(20D, physicalHit(), DELTA);
    }

    @Test
    void profileBuffAdded_isSeenByTheNextHit_andNoLongerOnceRemoved() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh();
        assertEquals(20D, physicalHit(), DELTA);
        UserProfile profile = profileOf(attacker);

        profile.addDamageBuff(new SavedBuff("physical", 10D, false, System.currentTimeMillis() + 60_000L));
        assertEquals(30D, physicalHit(), DELTA);

        profile.removeDamageBuff("physical");
        assertEquals(20D, physicalHit(), DELTA);
    }

    @Test
    void profileBuffThatRunsOut_isNoLongerSeenByTheNextHit() throws Exception {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh();
        // Long enough that the first hit lands while it is still active, short enough to wait for
        profileOf(attacker).addDamageBuff(new SavedBuff("physical", 10D, false, System.currentTimeMillis() + 600L));
        assertEquals(30D, physicalHit(), DELTA);

        Thread.sleep(800);

        assertEquals(20D, physicalHit(), DELTA);
    }

    @Test
    void profileBuffsEditedInPlace_areSeenOnceTheyAreMarkedChanged() {
        // What the buff command does: it edits the sets it is handed
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh();
        assertEquals(20D, physicalHit(), DELTA);
        UserProfile profile = profileOf(attacker);

        profile.getDamageBuffs().add(new SavedBuff("physical", 10D, false, System.currentTimeMillis() + 60_000L));
        profile.markBuffsChanged();

        assertEquals(30D, physicalHit(), DELTA);
    }

    @Test
    void profileSwitch_isSeenByTheNextHit() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh();
        UserProfile first = profileOf(attacker);
        first.addDamageBuff(new SavedBuff("physical", 10D, false, System.currentTimeMillis() + 60_000L));
        assertEquals(30D, physicalHit(), DELTA);

        DivinityUser user = Objects.requireNonNull(plugin.getUserManager().getOrLoadUser(attacker));
        assertTrue(user.addProfile("other"));
        assertTrue(user.switchProfile("other"));
        try {
            assertEquals(20D, physicalHit(), DELTA, "The other profile has no buff");
        } finally {
            user.switchProfile(first.getIdName());
        }
    }

    @Test
    void biome_isReadAtTheHit_notCached() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh();
        assertEquals(20D, physicalHit(), DELTA);
        tick();
        assertTrue(EntityStats.get(attacker).hasSnapshot());

        String              biome     = EnumUT.getName(attacker.getLocation().getBlock().getBiome());
        Map<String, Double> modifiers = ItemStats.getDamageById("physical").getBiomeDamageModifiers();
        Double              previous  = modifiers.put(biome, 1.5D);
        try {
            assertEquals(30D, physicalHit(), DELTA);
        } finally {
            if (previous == null) modifiers.remove(biome);
            else modifiers.put(biome, previous);
        }
        assertEquals(20D, physicalHit(), DELTA);
    }

    @Test
    void vanillaArmorAttribute_isSeenByTheNextHit() {
        EngineCfg.LEGACY_COMBAT = true;
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh();
        assertEquals(20D, physicalHit(), DELTA);

        // Potions and other plugins change attributes and tell no one
        victim.registerAttribute(Attribute.ARMOR);
        Objects.requireNonNull(victim.getAttribute(Attribute.ARMOR)).setBaseValue(30D);

        assertEquals(15.2D, physicalHit(), DELTA);
    }

    @Test
    void vanillaToughnessAttribute_isSeenByTheNextHit() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        victim.getInventory().setChestplate(armor(Map.of("physical", 30D)));
        refresh();
        // 1 - max(30 / 5, 30 - 4 * 20 / (toughness 0 + 8)) * protection factor 0.8 * 0.05 = 0.2
        assertEquals(4D, physicalHit(), DELTA);

        victim.registerAttribute(Attribute.ARMOR_TOUGHNESS);
        Objects.requireNonNull(victim.getAttribute(Attribute.ARMOR_TOUGHNESS)).setBaseValue(2D);

        // 1 - max(6, 30 - 4 * 20 / (2 + 8)) * 0.04 = 0.12
        assertEquals(2.4D, physicalHit(), DELTA);
    }

    @Test
    void death_dropsAPlayersSnapshot() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh();
        hit();
        assertTrue(EntityStats.get(attacker).hasSnapshot());
        assertEquals(20D, EntityStats.get(attacker).getDamageByType(physical()), DELTA);

        attacker.getInventory().clear(); // What dying does to the inventory
        EntityStats.get(attacker).handleDeath();

        assertFalse(EntityStats.get(attacker).hasSnapshot());
        assertEquals(0D, EntityStats.get(attacker).getDamageByType(physical()), DELTA);
    }

    @Test
    void death_releasesAMobsStats() {
        ZombieMock zombie = new ZombieMock(server, UUID.randomUUID());
        EntityStats.get(zombie).getEnchantProtectFactor(Enchantment.PROTECTION); // Fills a snapshot
        assertTrue(EntityStats.isTracked(zombie));

        server.getPluginManager()
                .callEvent(new EntityDeathEvent(zombie,
                        DamageSource.builder(DamageType.GENERIC).build(),
                        new ArrayList<>()));

        assertFalse(EntityStats.isTracked(zombie));
    }

    @Test
    void quit_releasesAPlayersStats() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh();
        hit();
        assertTrue(EntityStats.isTracked(attacker));

        server.getPluginManager().callEvent(new PlayerQuitEvent(attacker, "left"));

        assertFalse(EntityStats.isTracked(attacker));
    }

    @Test
    void anEntityThatIsGone_isReleasedBySweep() {
        ZombieMock zombie = new ZombieMock(server, UUID.randomUUID());
        EntityStats.get(zombie).getEnchantProtectFactor(Enchantment.PROTECTION);
        assertTrue(EntityStats.isTracked(zombie));

        zombie.remove(); // What unloading or despawning does
        EntityStats.getAll(); // What the stats task does every tick

        assertFalse(EntityStats.isTracked(zombie));
    }

    @Test
    void invalidatingEveryone_dropsEveryoneSnapshot() {
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh();
        hit();
        assertTrue(EntityStats.get(attacker).hasSnapshot());
        assertTrue(EntityStats.get(victim).hasSnapshot());

        EntityStats.invalidateAllSnapshots(); // What a reload of the stat config does

        assertFalse(EntityStats.get(attacker).hasSnapshot());
        assertFalse(EntityStats.get(victim).hasSnapshot());
    }

    // ------------------------------------------------------------------ //
    // The fallback for changes nothing announces
    // ------------------------------------------------------------------ //

    @Test
    void aSnapshot_isNotUsedPastItsMaximumAge() {
        AtomicLong now = new AtomicLong(1_000_000L);
        EntityStats.setClock(now::get);
        EngineCfg.PERFORMANCE_STAT_SNAPSHOT_MAX_AGE = 20; // 1 second

        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh();
        assertEquals(20D, physicalHit(), DELTA);
        UserProfile profile = profileOf(attacker);
        // Edited without a word to anyone: nothing here can tell the snapshot
        profile.getDamageBuffs().add(new SavedBuff("physical", 10D, false, System.currentTimeMillis() + 3_600_000L));

        now.addAndGet(900);
        assertEquals(20D, physicalHit(), DELTA, "Still inside the maximum age: the snapshot is used");

        now.addAndGet(200);
        assertEquals(30D, physicalHit(), DELTA, "Past the maximum age: read again");
    }

    @Test
    void aMaximumAgeOfZero_turnsTheCacheOff() {
        EngineCfg.PERFORMANCE_STAT_SNAPSHOT_MAX_AGE = 0;
        attacker.getInventory().setItemInMainHand(weapon(Map.of("physical", 20D)));
        refresh();
        assertEquals(20D, physicalHit(), DELTA);
        assertFalse(EntityStats.get(attacker).hasSnapshot());

        profileOf(attacker).getDamageBuffs()
                .add(new SavedBuff("physical", 10D, false, System.currentTimeMillis() + 60_000L));

        assertEquals(30D, physicalHit(), DELTA);
    }

    // ------------------------------------------------------------------ //
    // Rolls: the range is cached, the roll is not
    // ------------------------------------------------------------------ //

    @Test
    void aRange_isRolledAtEveryHit_notWhenTheSnapshotIsTaken() {
        attacker.getInventory().setItemInMainHand(rangedWeapon(10D, 20D));
        refresh();

        Set<Double> rolls = new HashSet<>();
        for (int i = 0; i < 40; i++) {
            double damage = physicalHit(); // No refresh in between: one snapshot serves them all
            assertTrue(damage >= 10D && damage <= 20D, "Rolled outside the range: " + damage);
            rolls.add(damage);
        }
        assertTrue(EntityStats.get(attacker).hasSnapshot());
        assertTrue(rolls.size() > 20, "Every hit should roll for itself, but only got " + rolls.size() + " values");
    }

    @Test
    void rolls_followTheSameSequenceWhetherOrNotTheSnapshotIsUsed() {
        attacker.getInventory().setItemInMainHand(rangedWeapon(10D, 20D));
        refresh();

        EngineCfg.PERFORMANCE_STAT_SNAPSHOT_MAX_AGE = 20;
        List<Double> cached = rolls(200, 1234L);
        EngineCfg.PERFORMANCE_STAT_SNAPSHOT_MAX_AGE = 0;
        List<Double> uncached = rolls(200, 1234L);

        assertEquals(uncached, cached, "The same seed should give the same rolls with or without the cache");
    }

    @Test
    void rolls_areUniformOverTheRange() {
        attacker.getInventory().setItemInMainHand(rangedWeapon(10D, 20D));
        refresh();

        int          n     = 20_000;
        List<Double> rolls = rolls(n, 99L);

        double mean = rolls.stream().mapToDouble(d -> d).average().orElseThrow();
        assertEquals(15D, mean, 0.15D, "The mean of a uniform roll between 10 and 20 is 15");
        int[] bins = new int[10];
        for (double roll : rolls) {
            assertTrue(roll >= 10D && roll <= 20D);
            bins[Math.min(9, (int) (roll - 10D))]++;
        }
        double expected = n / 10D;
        double chi      = 0;
        for (int count : bins) chi += (count - expected) * (count - expected) / expected;
        assertTrue(chi < 40D, "Rolls are not uniform, chi-square " + chi + " over bins " + java.util.Arrays.toString(bins));
    }

    @Test
    void rolls_matchWhatTheCalculatorGaveBeforeSnapshotsExisted() {
        // These numbers were produced by the code before snapshots existed, from the same item and seed
        attacker.getInventory().setItemInMainHand(rangedWeapon(10D, 20D));
        refresh();

        List<Double> rolls = rolls(5, 1234L);

        double[] before = BEFORE_SNAPSHOTS_SEED_1234;
        for (int i = 0; i < before.length; i++) {
            assertEquals(before[i], rolls.get(i), 1e-9, "Roll " + i);
        }
    }

    // Rolled by the code of the base commit (aed8234) for the same item and seed
    private static final double[] BEFORE_SNAPSHOTS_SEED_1234 = {
            16.05520825575022D, 17.93004448505456D, 17.417024356698136D, 12.687599526733104D, 17.451704997376304D};

    // ------------------------------------------------------------------ //
    // Projectiles: the shooter's stats at launch
    // ------------------------------------------------------------------ //

    @Test
    void projectile_usesTheGearItWasFiredWith_notTheGearAtImpact() {
        attacker.getInventory().setItemInMainHand(bow(15D));
        attacker.getInventory().setChestplate(damageGear(5D));
        refresh();

        Arrow arrow = launch();
        // The shooter swaps armor while the arrow is in the air
        attacker.getInventory().setChestplate(damageGear(25D));
        EntityStats.get(attacker).updateAll();

        assertEquals(20D, arrowPhysical(arrow), DELTA, "15 from the bow and 5 from the armor worn at launch");
    }

    @Test
    void projectile_notLaunchedThroughDivinity_isReadAtImpactAsItAlwaysWas() {
        attacker.getInventory().setItemInMainHand(bow(15D));
        attacker.getInventory().setChestplate(damageGear(5D));
        refresh();

        Arrow arrow = victim.getWorld().spawn(victim.getLocation(), Arrow.class);
        arrow.setShooter(attacker);
        ProjectileStats.setSrcWeapon(arrow, bow(15D));
        ProjectileStats.setPower(arrow, 1D);
        assertNull(ProjectileStats.getSnapshot(arrow));

        attacker.getInventory().setChestplate(damageGear(25D));
        EntityStats.get(attacker).updateAll();

        assertEquals(40D, arrowPhysical(arrow), DELTA);
    }

    @Test
    void projectile_ignoresABuffGainedAfterItWasFired() {
        attacker.getInventory().setItemInMainHand(bow(15D));
        refresh();
        Arrow arrow = launch();

        profileOf(attacker).addDamageBuff(new SavedBuff("physical", 10D, false, System.currentTimeMillis() + 60_000L));

        assertEquals(15D, arrowPhysical(arrow), DELTA);
        assertEquals(25D, arrowPhysical(launch()), DELTA, "An arrow fired now has the buff");
    }

    @Test
    void projectile_ignoresAnEffectGainedAfterItWasFired() {
        attacker.getInventory().setItemInMainHand(bow(15D));
        refresh();
        Arrow arrow = launch();

        EntityStats.get(attacker).addEffect(new AdjustStatEffect.Builder(-1)
                .withAdjust(physical(), v -> v + 10D)
                .build());

        assertEquals(15D, arrowPhysical(arrow), DELTA);
    }

    @Test
    void projectile_keepsAnEffectItWasFiredWith_evenIfItIsGoneAtImpact() {
        attacker.getInventory().setItemInMainHand(bow(15D));
        refresh();
        AdjustStatEffect effect = new AdjustStatEffect.Builder(-1).withAdjust(physical(), v -> v + 10D).build();
        EntityStats.get(attacker).addEffect(effect);
        Arrow arrow = launch();

        EntityStats.get(attacker).removeEffect(effect);

        assertEquals(25D, arrowPhysical(arrow), DELTA);
    }

    @Test
    void projectile_takesTheShootersItemStatsAtLaunchToo() {
        ItemStack bow = bow(15D);
        stat(bow, TypedStat.Type.PVP_DAMAGE, 50D); // +50% against players
        attacker.getInventory().setItemInMainHand(bow);
        refresh();
        Arrow arrow = launch();

        // Gear with a higher PvP bonus goes on while the arrow flies
        ItemStack ring = damageGear(0D);
        stat(ring, TypedStat.Type.PVP_DAMAGE, 500D);
        attacker.getInventory().setChestplate(ring);
        EntityStats.get(attacker).updateAll();

        assertEquals(22.5D, arrowPhysical(arrow), DELTA);
    }

    @Test
    void projectile_snapshotIsReleasedWhenTheProjectileIsGone() {
        attacker.getInventory().setItemInMainHand(bow(15D));
        refresh();
        Arrow arrow = launch();
        ProjectileStats.sweep();
        assertNotNull(ProjectileStats.getSnapshot(arrow), "A projectile in the air keeps its snapshot");
        int before = ProjectileStats.snapshotCount();

        arrow.remove();
        ProjectileStats.sweep();

        assertNull(ProjectileStats.getSnapshot(arrow));
        assertEquals(before - 1, ProjectileStats.snapshotCount());
    }

    // ------------------------------------------------------------------ //
    // Helpers
    // ------------------------------------------------------------------ //

    private static boolean isIronSword(ItemStack item) {
        return item != null && item.getType() == Material.IRON_SWORD;
    }

    private DamageAttribute physical() {
        return Objects.requireNonNull(ItemStats.getDamageById("physical"));
    }

    private void refresh() {
        for (PlayerMock player : List.of(attacker, victim)) {
            EntityStats.purge(player);
            EntityStats.get(player).updateAll();
        }
    }

    /** Lets the server run a tick, which is when the refreshes that a hit or an equipment change asks for run. */
    private void tick() {
        try (MockedStatic<EffectUT> ignored = mockStatic(EffectUT.class)) {
            server.getScheduler().performOneTick();
        }
    }

    /** Fires a melee hit the way the server would and returns what it did, by damage type. */
    private Map<String, Double> hit() {
        server.getPluginManager().callEvent(new EntityDamageByEntityEvent(attacker,
                victim,
                DamageCause.ENTITY_ATTACK,
                modifiers(VANILLA_SWORD_DAMAGE),
                functions(modifiers(VANILLA_SWORD_DAMAGE))));
        return damagesOf(EntityStats.get(victim).getLastDamageMeta());
    }

    private double physicalHit() {
        return hit().getOrDefault("physical", 0D);
    }

    private double arrowPhysical(Arrow arrow) {
        return arrowHit(arrow).getOrDefault("physical", 0D);
    }

    private Map<String, Double> arrowHit(Arrow arrow) {
        server.getPluginManager().callEvent(new EntityDamageByEntityEvent(arrow,
                victim,
                DamageCause.PROJECTILE,
                modifiers(4),
                functions(modifiers(4))));
        return damagesOf(EntityStats.get(victim).getLastDamageMeta());
    }

    /** What the server does when the attacker shoots: the launch event, with the bow in hand. */
    private Arrow launch() {
        ItemStack bow   = attacker.getInventory().getItemInMainHand();
        Arrow     arrow = victim.getWorld().spawn(victim.getLocation(), Arrow.class);
        arrow.setShooter(attacker);
        // The real event cannot be built here: its constructor sets properties MockBukkit refuses to set. What
        // it records on the projectile is recorded by hand, as the constructor would.
        ProjectileStats.setSrcWeapon(arrow, bow);
        ProjectileStats.setPower(arrow, 1D);
        DivinityProjectileLaunchEvent event = mock(DivinityProjectileLaunchEvent.class,
                withSettings().defaultAnswer(CALLS_REAL_METHODS));
        doReturn(arrow).when(event).getProjectile();
        doReturn(attacker).when(event).getShooter();
        doReturn(bow).when(event).getWeapon();
        doReturn(1D).when(event).getPower();
        doReturn(true).when(event).isBowEvent();
        server.getPluginManager().callEvent(event);
        assertNotNull(ProjectileStats.getSnapshot(arrow), "Test setup: the launch should have taken a snapshot");
        return arrow;
    }

    private List<Double> rolls(int count, long seed) {
        Rnd.rnd.setSeed(seed);
        EntityStats stats = EntityStats.get(attacker);
        List<Double> rolls = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            rolls.add(stats.getDamageByType(physical()));
        }
        return rolls;
    }

    private UserProfile profileOf(PlayerMock player) {
        DivinityUser user = Objects.requireNonNull(plugin.getUserManager().getOrLoadUser(player));
        return user.getActiveProfile();
    }

    private static Map<String, Double> damagesOf(DamageMeta meta) {
        Map<String, Double> damages = new HashMap<>();
        Objects.requireNonNull(meta).getDamages().forEach((attribute, amount) -> damages.put(attribute.getId(), amount));
        return damages;
    }

    private BonusMap physicalDamageBonus(double flat) {
        BonusMap map = new BonusMap();
        map.getBonuses().put(physical(), (isPercent, input) -> isPercent ? input : input + flat);
        return map;
    }

    /** Stands in for the sets module, which the tests do not configure. */
    private SetManagerInstalled installSetManager(List<BonusMap> active) throws Exception {
        Field field = ModuleCache.class.getDeclaredField("setManager");
        field.setAccessible(true);
        ModuleCache cache    = plugin.getModuleCache();
        Object      previous = field.get(cache);

        SetManager sets = mock(SetManager.class);
        when(sets.getActiveSetBonuses(any())).thenAnswer(ans -> new ArrayList<>(active));
        field.set(cache, sets);
        return () -> {
            try {
                field.set(cache, previous);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        };
    }

    private interface SetManagerInstalled extends AutoCloseable {
        @Override
        void close();
    }

    private ItemStack weapon(Map<String, Double> damages) {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        damages.forEach((id, value) -> Objects.requireNonNull(ItemStats.getDamageById(id))
                .add(item, new StatBonus(new double[]{value}, false, null), -1));
        return item;
    }

    private ItemStack rangedWeapon(double min, double max) {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        physical().add(item, new StatBonus(new double[]{min, max}, false, null), -1);
        return item;
    }

    private ItemStack bow(double physical) {
        ItemStack item = new ItemStack(Material.BOW);
        physical().add(item, new StatBonus(new double[]{physical}, false, null), -1);
        return item;
    }

    /** A chestplate that adds damage, as a piece of gear with a damage stat does. */
    private ItemStack damageGear(double physical) {
        ItemStack item = new ItemStack(Material.IRON_CHESTPLATE);
        if (physical > 0) physical().add(item, new StatBonus(new double[]{physical}, false, null), -1);
        return item;
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

    /** MockBukkit does not implement the attack cooldown, which the server tick reads for every player. */
    private static class TickSafePlayer extends PlayerMock {
        TickSafePlayer(@NotNull ServerMock server, @NotNull String name) {
            super(server, name, UUID.randomUUID());
        }

        @Override
        public float getAttackCooldown() {
            return 1F;
        }
    }
}
