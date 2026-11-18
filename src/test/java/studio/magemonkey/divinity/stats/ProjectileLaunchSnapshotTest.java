package studio.magemonkey.divinity.stats;

import com.google.common.base.Function;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.MockedStatic;
import studio.magemonkey.codex.compat.VersionManager;
import studio.magemonkey.codex.util.EffectUT;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.api.event.DivinityProjectileLaunchEvent;
import studio.magemonkey.divinity.config.EngineCfg;
import studio.magemonkey.divinity.manager.damage.DamageMeta;
import studio.magemonkey.divinity.manager.effects.main.AdjustStatEffect;
import studio.magemonkey.divinity.stats.bonus.StatBonus;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.stats.items.attributes.DamageAttribute;
import studio.magemonkey.divinity.testutil.MockedTest;

import java.io.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.withSettings;

/**
 * What a projectile carries from its launch to its impact (review of #385): whose stats they are, which
 * projectiles get any, when their effects are charged, how long they are held, and what a reload does to them.
 */
@SuppressWarnings("deprecation")
class ProjectileLaunchSnapshotTest extends MockedTest {
    private static final double DELTA = 1e-6;

    private TickSafePlayer attacker;
    private TickSafePlayer victim;
    private TickSafePlayer other;
    private int            originalMaxAge;

    // The mock plugin jar has no loose resource files, so the shipped arrow is copied into the plugin folder
    @BeforeAll
    void copyShippedArrowConfig() throws IOException {
        File file = new File(server.getPluginsFolder().getAbsoluteFile() + File.separator +
                "Divinity-" + System.getProperty("DIVINITY_VERSION") + File.separator +
                "modules" + File.separator + "arrows" + File.separator + "items" + File.separator
                + "arrow_pierce.yml");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        try (BufferedReader in = new BufferedReader(new InputStreamReader(Objects.requireNonNull(this.getClass()
                .getClassLoader()
                .getResourceAsStream("modules/arrows/items/arrow_pierce.yml"))));
             FileWriter out = new FileWriter(file)) {
            String str;
            while ((str = in.readLine()) != null) {
                out.write(str + "\n");
            }
        }
        Divinity.getInstance().reload();
    }

    @BeforeEach
    void setup() {
        attacker = player("Shooter");
        victim = player("Victim");
        other = player("Other");
        originalMaxAge = EngineCfg.PERFORMANCE_STAT_SNAPSHOT_MAX_AGE;

        EngineCfg.VANILLA_ONLY_ITEM_STATS = true;
        doAnswer(ans -> 0D).when(VersionManager.getNms()).getDefaultDamage(any());
        doAnswer(ans -> false).when(VersionManager.getNms()).isWeapon(any());
        doAnswer(ans -> Registry.ATTRIBUTE.get(NamespacedKey.minecraft(((String) ans.getArgument(0)).toLowerCase(
                Locale.US)))).when(VersionManager.getNms()).getAttribute(anyString());
        tick(); // Let the refreshes that joining asks for run now
    }

    @AfterEach
    void tearDown() {
        ProjectileStats.resetClock();
        ProjectileStats.clear();
        EngineCfg.PERFORMANCE_STAT_SNAPSHOT_MAX_AGE = originalMaxAge;
        EngineCfg.VANILLA_ONLY_ITEM_STATS = false;
        int offset = 0;
        for (PlayerMock p : players) {
            p.teleport(p.getLocation().add(10_000 + 100 * offset++, 0, 0));
        }
        tick();
        players.forEach(EntityStats::purge);
    }

    // ------------------------------------------------------------------ //
    // Whose stats they are
    // ------------------------------------------------------------------ //

    @Test
    void aProjectileThatChangedHands_isNotReadFromTheOriginalShootersSnapshot() {
        attacker.getInventory().setItemInMainHand(bow(15D));
        attacker.getInventory().setChestplate(damageGear(5D));
        refresh();
        Arrow arrow = launch(attacker);
        assertEquals(20D, arrowHit(arrow), DELTA, "Test setup: the snapshot is 15 from the bow and 5 from the armor");

        // Reflected, or taken over by a plugin: the new owner wears nothing
        arrow.setShooter(other);
        refresh();

        assertEquals(15D, arrowHit(arrow), DELTA, "Read live from the new shooter, as before snapshots");
    }

    @Test
    void twoProjectilesFromOneShooter_eachKeepTheirOwnSnapshot() {
        attacker.getInventory().setItemInMainHand(bow(15D));
        attacker.getInventory().setChestplate(damageGear(5D));
        refresh();
        Arrow first = launch(attacker);

        attacker.getInventory().setChestplate(damageGear(25D));
        EntityStats.get(attacker).updateAll();
        Arrow second = launch(attacker);

        attacker.getInventory().setChestplate(damageGear(100D));
        EntityStats.get(attacker).updateAll();

        assertEquals(20D, arrowHit(first), DELTA);
        assertEquals(40D, arrowHit(second), DELTA);
    }

    @Test
    void aCustomArrowsBonus_isCarriedFromLaunchToImpact() {
        ArrowManagerHolder.requireShippedArrow(plugin);
        attacker.getInventory().setItemInMainHand(bow(20D));
        refresh();

        Arrow plain = launch(attacker);
        assertEquals(20D, arrowHit(plain), DELTA);

        Arrow piercing = victim.getWorld().spawn(victim.getLocation(), Arrow.class);
        piercing.setShooter(attacker);
        piercing.setMetadata("QRPG_ARROW_ID", new FixedMetadataValue(plugin, "arrow_pierce"));
        piercing.setMetadata("QRPG_ARROW_LEVEL", new FixedMetadataValue(plugin, 1));
        launchEvent(piercing, attacker);
        assertEquals(18D, arrowHit(piercing), DELTA, "Piercing Arrow: 10% less damage");

        // Gone from the projectile by the time it lands, and still in what it was fired with
        piercing.removeMetadata("QRPG_ARROW_ID", plugin);
        piercing.removeMetadata("QRPG_ARROW_LEVEL", plugin);
        assertEquals(18D, arrowHit(piercing), DELTA);
    }

    // ------------------------------------------------------------------ //
    // Which projectiles get a snapshot
    // ------------------------------------------------------------------ //

    @Test
    void arrowsGetASnapshot_pearlsAndSnowballsDoNot() {
        attacker.getInventory().setItemInMainHand(bow(15D));
        refresh();

        Arrow arrow = victim.getWorld().spawn(victim.getLocation(), Arrow.class);
        EnderPearl pearl = victim.getWorld().spawn(victim.getLocation(), EnderPearl.class);
        Snowball snowball = victim.getWorld().spawn(victim.getLocation(), Snowball.class);
        for (Projectile p : List.of(arrow, pearl, snowball)) {
            p.setShooter(attacker);
            launchEvent(p, attacker);
        }

        assertNotNull(ProjectileStats.getSnapshot(arrow));
        assertNull(ProjectileStats.getSnapshot(pearl));
        assertNull(ProjectileStats.getSnapshot(snowball));
        assertTrue(ProjectileStats.canDealDamage(arrow));
        assertFalse(ProjectileStats.canDealDamage(pearl));
        assertFalse(ProjectileStats.canDealDamage(snowball));
    }

    // ------------------------------------------------------------------ //
    // Charges are used at impact
    // ------------------------------------------------------------------ //

    @Test
    void aPearlOrASnowball_spendsNoCharge() {
        attacker.getInventory().setItemInMainHand(bow(15D));
        refresh();
        AdjustStatEffect effect = oneChargeBonus(10D);
        EntityStats.get(attacker).addEffect(effect);

        for (Projectile p : List.<Projectile>of(victim.getWorld().spawn(victim.getLocation(), EnderPearl.class),
                victim.getWorld().spawn(victim.getLocation(), Snowball.class))) {
            p.setShooter(attacker);
            launchEvent(p, attacker);
        }

        assertTrue(effect.isAdjusted(physical()), "Throwing something that deals no damage must keep the charge");
    }

    @Test
    void anArrowThatIsLaunched_butNeverHits_spendsNoCharge() {
        attacker.getInventory().setItemInMainHand(bow(15D));
        refresh();
        AdjustStatEffect effect = oneChargeBonus(10D);
        EntityStats.get(attacker).addEffect(effect);

        launch(attacker);

        assertTrue(effect.isAdjusted(physical()), "A shot that has not landed has not used its charge");
    }

    @Test
    void anArrowThatHits_spendsExactlyOneCharge_andHasTheAdjustment() {
        attacker.getInventory().setItemInMainHand(bow(15D));
        refresh();
        AdjustStatEffect effect = oneChargeBonus(10D);
        EntityStats.get(attacker).addEffect(effect);
        Arrow arrow = launch(attacker);

        assertEquals(25D, arrowHit(arrow), DELTA, "The adjustment it was fired with");
        assertFalse(effect.isAdjusted(physical()), "One charge, and the hit used it");

        assertEquals(15D, arrowHit(launch(attacker)), DELTA, "Nothing left for the next arrow");
    }

    @Test
    void anEffectThatIsGoneAtImpact_isNotChargedAndDoesNotFail() {
        attacker.getInventory().setItemInMainHand(bow(15D));
        refresh();
        AdjustStatEffect effect = oneChargeBonus(10D);
        EntityStats.get(attacker).addEffect(effect);
        Arrow arrow = launch(attacker);

        EntityStats.get(attacker).removeEffect(effect); // Clears it

        assertEquals(25D, arrowHit(arrow), DELTA);
    }

    // ------------------------------------------------------------------ //
    // How long they are held
    // ------------------------------------------------------------------ //

    @Test
    void aProjectileInAnUnloadedChunk_keepsItsSnapshot() {
        UUID gone = UUID.randomUUID();
        UUID world = victim.getWorld().getUID();
        assertFalse(victim.getWorld().isChunkLoaded(100_000, 100_000), "Test setup: this chunk is not loaded");
        ProjectileStats.hold(gone, frozen(), world, 100_000, 100_000);
        int before = ProjectileStats.snapshotCount();

        ProjectileStats.sweep();

        assertEquals(before, ProjectileStats.snapshotCount(), "It may come back when the chunk loads");
    }

    @Test
    void aProjectileThatCannotBeFoundInALoadedChunk_isReleased() {
        UUID gone = UUID.randomUUID();
        UUID world = victim.getWorld().getUID();
        int  cx = victim.getLocation().getBlockX() >> 4;
        int  cz = victim.getLocation().getBlockZ() >> 4;
        victim.getWorld().loadChunk(cx, cz);
        assertTrue(victim.getWorld().isChunkLoaded(cx, cz), "Test setup: this chunk is loaded");
        ProjectileStats.hold(gone, frozen(), world, cx, cz);
        int before = ProjectileStats.snapshotCount();

        ProjectileStats.sweep();

        assertEquals(before - 1, ProjectileStats.snapshotCount());
    }

    @Test
    void aSnapshotIsNotHeldPastTheUpperAgeBound_whateverTheChunk() {
        AtomicLong now = new AtomicLong(5_000_000L);
        ProjectileStats.setClock(now::get);
        ProjectileStats.hold(UUID.randomUUID(), frozen(), victim.getWorld().getUID(), 100_000, 100_000);
        int held = ProjectileStats.snapshotCount();

        now.addAndGet(ProjectileStats.MAX_HELD_MILLIS - 1);
        ProjectileStats.sweep();
        assertEquals(held, ProjectileStats.snapshotCount());

        now.addAndGet(2);
        ProjectileStats.sweep();
        assertEquals(held - 1, ProjectileStats.snapshotCount());
    }

    // ------------------------------------------------------------------ //
    // Reload and the off switch
    // ------------------------------------------------------------------ //

    @Test
    void aReload_dropsLaunchSnapshotsAndLiveSnapshotsAlike() {
        attacker.getInventory().setItemInMainHand(bow(15D));
        attacker.getInventory().setChestplate(damageGear(5D));
        refresh();
        Arrow arrow = launch(attacker);
        arrowHit(arrow); // Fills the victim's and the shooter's live snapshots as well
        assertTrue(EntityStats.get(victim).hasSnapshot());
        assertNotNull(ProjectileStats.getSnapshot(arrow));

        plugin.reload();

        assertNull(ProjectileStats.getSnapshot(arrow), "A launch snapshot holds stat objects that reload replaces");
        assertFalse(EntityStats.isTracked(attacker), "Live snapshots go with the stats they sit on");
        assertFalse(EntityStats.isTracked(victim));
    }

    @Test
    void aMaximumAgeOfZero_copiesNoItems() {
        attacker.getInventory().setItemInMainHand(bow(15D));
        refresh();
        EngineCfg.PERFORMANCE_STAT_SNAPSHOT_MAX_AGE = 0;

        try (MockedStatic<StatSnapshot> snapshots = mockStatic(StatSnapshot.class, CALLS_REAL_METHODS)) {
            EntityStats.get(attacker).getDamageTypes(false);
            EntityStats.get(attacker).getItemStat(studio.magemonkey.divinity.stats.items.attributes.api.TypedStat.Type.PVP_DAMAGE, false);
            snapshots.verify(() -> StatSnapshot.copyOf(any()), never());
        }

        EngineCfg.PERFORMANCE_STAT_SNAPSHOT_MAX_AGE = 20;
        try (MockedStatic<StatSnapshot> snapshots = mockStatic(StatSnapshot.class, CALLS_REAL_METHODS)) {
            EntityStats.get(attacker).getDamageTypes(false);
            snapshots.verify(() -> StatSnapshot.copyOf(any()), times(1));
        }
    }

    // ------------------------------------------------------------------ //
    // Helpers
    // ------------------------------------------------------------------ //

    /** A frozen snapshot with nothing in it, for the tests that are about how long one is held. */
    private StatSnapshot frozen() {
        return new StatSnapshot(UUID.randomUUID(), null, new HashMap<>());
    }

    private TickSafePlayer player(String name) {
        TickSafePlayer p = new TickSafePlayer(server, name);
        server.addPlayer(p);
        players.add(p);
        return p;
    }

    private DamageAttribute physical() {
        return Objects.requireNonNull(ItemStats.getDamageById("physical"));
    }

    private AdjustStatEffect oneChargeBonus(double flat) {
        return new AdjustStatEffect.Builder(-1).withCharges(1).withAdjust(physical(), v -> v + flat).build();
    }

    private void refresh() {
        for (PlayerMock p : List.of(attacker, victim, other)) {
            EntityStats.purge(p);
            EntityStats.get(p).updateAll();
        }
    }

    private void tick() {
        try (MockedStatic<EffectUT> ignored = mockStatic(EffectUT.class)) {
            server.getScheduler().performOneTick();
        }
    }

    /** A bow shot by the shooter: the projectile, its weapon and power, and the launch event. */
    private Arrow launch(PlayerMock shooter) {
        Arrow arrow = victim.getWorld().spawn(victim.getLocation(), Arrow.class);
        arrow.setShooter(shooter);
        launchEvent(arrow, shooter);
        assertNotNull(ProjectileStats.getSnapshot(arrow), "Test setup: the launch should have taken a snapshot");
        return arrow;
    }

    /**
     * The real event cannot be built here (its constructor sets properties MockBukkit refuses to set), so it is
     * stubbed, and what its constructor records on the projectile is recorded by hand.
     */
    private void launchEvent(Projectile projectile, PlayerMock shooter) {
        ItemStack bow = shooter.getInventory().getItemInMainHand();
        ProjectileStats.setSrcWeapon(projectile, bow);
        ProjectileStats.setPower(projectile, 1D);
        DivinityProjectileLaunchEvent event = mock(DivinityProjectileLaunchEvent.class,
                withSettings().defaultAnswer(CALLS_REAL_METHODS));
        doReturn(projectile).when(event).getProjectile();
        doReturn(shooter).when(event).getShooter();
        doReturn(bow).when(event).getWeapon();
        doReturn(1D).when(event).getPower();
        doReturn(true).when(event).isBowEvent();
        server.getPluginManager().callEvent(event);
    }

    private double arrowHit(Arrow arrow) {
        server.getPluginManager().callEvent(new EntityDamageByEntityEvent(arrow,
                victim,
                DamageCause.PROJECTILE,
                modifiers(4),
                functions(modifiers(4))));
        DamageMeta meta = Objects.requireNonNull(EntityStats.get(victim).getLastDamageMeta());
        Map<String, Double> damages = new HashMap<>();
        meta.getDamages().forEach((attribute, amount) -> damages.put(attribute.getId(), amount));
        return damages.getOrDefault("physical", 0D);
    }

    private ItemStack bow(double physical) {
        ItemStack item = new ItemStack(Material.BOW);
        physical().add(item, new StatBonus(new double[]{physical}, false, null), -1);
        return item;
    }

    private ItemStack damageGear(double physical) {
        ItemStack item = new ItemStack(Material.IRON_CHESTPLATE);
        physical().add(item, new StatBonus(new double[]{physical}, false, null), -1);
        return item;
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

    private static final class ArrowManagerHolder {
        static void requireShippedArrow(Divinity plugin) {
            assertNotNull(plugin.getModuleCache().getArrowManager(), "ArrowManager module should be enabled");
            assertNotNull(plugin.getModuleCache().getArrowManager().getItemById("arrow_pierce"),
                    "Shipped 'arrow_pierce' item should have loaded");
        }
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
