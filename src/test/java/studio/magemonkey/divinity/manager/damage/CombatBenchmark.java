package studio.magemonkey.divinity.manager.damage;

import com.google.common.base.Function;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockito.MockedStatic;
import studio.magemonkey.codex.compat.VersionManager;
import studio.magemonkey.codex.util.EffectUT;
import studio.magemonkey.divinity.config.EngineCfg;
import studio.magemonkey.divinity.stats.EntityStats;
import studio.magemonkey.divinity.stats.bonus.StatBonus;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.stats.items.attributes.api.SimpleStat;
import studio.magemonkey.divinity.stats.items.attributes.api.TypedStat;
import studio.magemonkey.divinity.testutil.MockedTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;

/**
 * Hits per second through the damage pipeline (the listener, which reads stats, and the calculator), for the
 * same inputs before and after a change to how stats are read. It measures nothing the suite asserts, so it
 * only runs when asked for:
 * <pre>
 *   mvn -Dtest=CombatBenchmark -Dcombat.benchmark=true [-Dcombat.benchmark.out=/path/file] test
 * </pre>
 * Two situations are timed, because they differ in what a stat cache can save:
 * <ul>
 *   <li><b>burst</b>: hits that follow each other with nothing in between, as with area damage or several
 *   projectiles landing in one tick.</li>
 *   <li><b>steady</b>: one hit per tick, with the server tick in between. A hit asks for the stats of both
 *   fighters to be refreshed on the next tick, so this is what ordinary combat looks like.</li>
 * </ul>
 * The fighters carry a weapon with three damage types and four armor pieces with three defenses each, plus
 * item stats on all of them, which is about what a geared player has.
 */
@EnabledIfSystemProperty(named = "combat.benchmark", matches = "true")
class CombatBenchmark extends MockedTest {
    private static final int WARMUP_HITS = 1500;
    private static final int BURST_HITS  = 4000;
    private static final int STEADY_HITS = 1500;
    private static final int ROUNDS      = 5;

    private TickSafePlayer attacker;
    private TickSafePlayer victim;

    @BeforeEach
    void setup() {
        useRealAttributes();
        doAnswer(ans -> Registry.ATTRIBUTE.get(NamespacedKey.minecraft(((String) ans.getArgument(0)).toLowerCase(
                Locale.US)))).when(VersionManager.getNms()).getAttribute(anyString());
        doAnswer(ans -> 0D).when(VersionManager.getNms()).getDefaultDamage(any());
        doAnswer(ans -> false).when(VersionManager.getNms()).isWeapon(any());

        EngineCfg.VANILLA_ONLY_ITEM_STATS = true;

        attacker = new TickSafePlayer(server, "BenchAttacker");
        server.addPlayer(attacker);
        players.add(attacker);
        victim = new TickSafePlayer(server, "BenchVictim");
        server.addPlayer(victim);
        players.add(victim);

        attacker.getInventory().setItemInMainHand(weapon());
        attacker.getInventory().setHelmet(armor(Material.IRON_HELMET));
        attacker.getInventory().setChestplate(armor(Material.IRON_CHESTPLATE));
        victim.getInventory().setHelmet(armor(Material.IRON_HELMET));
        victim.getInventory().setChestplate(armor(Material.IRON_CHESTPLATE));
        victim.getInventory().setLeggings(armor(Material.IRON_LEGGINGS));
        victim.getInventory().setBoots(armor(Material.IRON_BOOTS));
        refresh();
    }

    @AfterEach
    void tearDown() {
        EngineCfg.VANILLA_ONLY_ITEM_STATS = false;
        try (MockedStatic<EffectUT> ignored = mockStatic(EffectUT.class)) {
            attacker.teleport(attacker.getLocation().add(10_000, 0, 0));
            victim.teleport(victim.getLocation().add(10_100, 0, 0));
            server.getScheduler().performOneTick();
        }
        players.forEach(EntityStats::purge);
    }

    @Test
    void hitsPerSecond() throws IOException {
        List<Double> burst  = new ArrayList<>();
        List<Double> steady = new ArrayList<>();

        try (MockedStatic<EffectUT> ignored = mockStatic(EffectUT.class)) {
            for (int i = 0; i < WARMUP_HITS; i++) {
                hit();
                if (i % 10 == 0) server.getScheduler().performOneTick();
            }
            for (int round = 0; round < ROUNDS; round++) {
                server.getScheduler().performOneTick();
                long start = System.nanoTime();
                for (int i = 0; i < BURST_HITS; i++) hit();
                burst.add(BURST_HITS / ((System.nanoTime() - start) / 1e9));

                server.getScheduler().performOneTick();
                start = System.nanoTime();
                for (int i = 0; i < STEADY_HITS; i++) {
                    hit();
                    server.getScheduler().performOneTick();
                }
                steady.add(STEADY_HITS / ((System.nanoTime() - start) / 1e9));
            }
        }

        String report = String.format(Locale.US,
                "BENCH burst median=%.0f hits/s (rounds: %s) | steady median=%.0f hits/s (rounds: %s)",
                median(burst), format(burst), median(steady), format(steady));
        System.out.println(report);
        String out = System.getProperty("combat.benchmark.out");
        if (out != null) {
            Path file = Paths.get(out);
            Files.write(file,
                    (report + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        }
        assertTrue(median(burst) > 0 && median(steady) > 0);
    }

    private void hit() {
        Map<DamageModifier, Double> modifiers = new EnumMap<>(DamageModifier.class);
        modifiers.put(DamageModifier.BASE, 6D);
        Map<DamageModifier, Function<? super Double, Double>> functions = new EnumMap<>(DamageModifier.class);
        functions.put(DamageModifier.BASE, d -> 6D);
        server.getPluginManager()
                .callEvent(new EntityDamageByEntityEvent((Entity) attacker,
                        victim,
                        DamageCause.ENTITY_ATTACK,
                        modifiers,
                        functions));
    }

    private void refresh() {
        for (PlayerMock player : List.of(attacker, victim)) {
            EntityStats.purge(player);
            EntityStats.get(player).updateAll();
        }
    }

    private ItemStack weapon() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        damage(item, "physical", new double[]{10D, 20D});
        damage(item, "fire", new double[]{4D});
        damage(item, "magical", new double[]{2D, 6D});
        stat(item, TypedStat.Type.PVP_DAMAGE, 10D);
        stat(item, TypedStat.Type.PENETRATION, 5D);
        stat(item, TypedStat.Type.CRITICAL_DAMAGE, 1.5D);
        return item;
    }

    private ItemStack armor(Material material) {
        ItemStack item = new ItemStack(material);
        defense(item, "physical", 6D);
        defense(item, "fire", 3D);
        defense(item, "magical", 2D);
        stat(item, TypedStat.Type.PVP_DEFENSE, 5D);
        stat(item, TypedStat.Type.HEALTH_REGEN, 2D);
        return item;
    }

    private static void damage(ItemStack item, String id, double[] value) {
        Objects.requireNonNull(ItemStats.getDamageById(id), id + " damage type is not enabled")
                .add(item, new StatBonus(value, false, null), -1);
    }

    private static void defense(ItemStack item, String id, double value) {
        Objects.requireNonNull(ItemStats.getDefenseById(id), id + " defense type is not enabled")
                .add(item, new StatBonus(new double[]{value}, false, null), -1);
    }

    private static void stat(ItemStack item, TypedStat.Type type, double value) {
        ((SimpleStat) Objects.requireNonNull(ItemStats.getStat(type), type.name() + " is not enabled"))
                .add(item, new StatBonus(new double[]{value}, false, null), -1);
    }

    private static double median(List<Double> values) {
        List<Double> sorted = new ArrayList<>(values);
        sorted.sort(null);
        return sorted.get(sorted.size() / 2);
    }

    private static String format(List<Double> values) {
        StringBuilder sb = new StringBuilder();
        values.forEach(v -> sb.append(String.format(Locale.US, "%.0f ", v)));
        return sb.toString().trim();
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
