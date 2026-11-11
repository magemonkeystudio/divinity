package studio.magemonkey.divinity.modules.list.arrows;

import com.google.common.base.Function;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.api.event.DivinityDamageEvent;
import studio.magemonkey.divinity.config.EngineCfg;
import studio.magemonkey.divinity.stats.EntityStats;
import studio.magemonkey.divinity.stats.bonus.StatBonus;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.stats.items.attributes.DefenseAttribute;
import studio.magemonkey.divinity.testutil.MockedTest;

import java.io.*;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * A custom arrow's bonus is applied to the shooter and the victim for the duration of one hit. If that hit
 * was cancelled part-way through, the bonus used to stay on both of them (issue #367). With the shipped
 * Piercing Arrow, which ignores 50% of physical defense, the victim kept half their defense against every
 * later attack until another custom arrow happened to land.
 */
@SuppressWarnings("deprecation")
class ArrowBonusLeakTest extends MockedTest {
    private static final double ARMOR = 20D;

    private PlayerMock shooter;
    private PlayerMock victim;
    private Listener   canceller;

    // Same approach as ArrowDefenseIgnoringTest: the mock plugin jar has no loose resource files, so the
    // shipped arrow has to be copied into the plugin folder by hand.
    @BeforeAll
    void copyShippedArrowConfig() throws IOException {
        File file = new File(server.getPluginsFolder().getAbsoluteFile() + File.separator +
                "Divinity-" + System.getProperty("DIVINITY_VERSION") + File.separator +
                "modules" + File.separator + "arrows"
                + File.separator + "items" + File.separator
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
        useRealAttributes();
        shooter = genPlayer("Shooter");
        victim = genPlayer("Victim");

        // Adding a stat to an item also syncs vanilla attributes, which the mocked NMS layer cannot do
        EngineCfg.VANILLA_ONLY_ITEM_STATS = true;
        ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE);
        physicalDefense().add(chestplate, new StatBonus(new double[]{ARMOR}, false, null), -1);
        victim.getInventory().setChestplate(chestplate);

        refresh(shooter);
        refresh(victim);
        assertEquals(ARMOR, defenseOf(victim), 0.001, "Test setup: the victim should start with full defense");
    }

    @AfterEach
    void tearDown() {
        EngineCfg.VANILLA_ONLY_ITEM_STATS = false;
        if (canceller != null) HandlerList.unregisterAll(canceller);
        canceller = null;
    }

    @Test
    void hitCancelledBeforeScaling_doesNotLeaveTheArrowBonusBehind() {
        canceller = new Listener() {
            @EventHandler
            public void on(DivinityDamageEvent.BeforeScale event) {
                event.setCancelled(true);
            }
        };
        server.getPluginManager().registerEvents(canceller, plugin);

        server.getPluginManager().callEvent(piercingArrowHit());

        assertEquals(ARMOR, defenseOf(victim), 0.001);
    }

    @Test
    void hitCancelledAtStart_doesNotLeaveTheArrowBonusBehind() {
        // This is also the path a dodged arrow takes
        canceller = new Listener() {
            @EventHandler
            public void on(DivinityDamageEvent.Start event) {
                event.setCancelled(true);
            }
        };
        server.getPluginManager().registerEvents(canceller, plugin);

        server.getPluginManager().callEvent(piercingArrowHit());

        assertEquals(ARMOR, defenseOf(victim), 0.001);
    }

    @Test
    void completedHit_doesNotLeaveTheArrowBonusBehind() {
        server.getPluginManager().callEvent(piercingArrowHit());

        assertEquals(ARMOR, defenseOf(victim), 0.001);
    }

    private EntityDamageByEntityEvent piercingArrowHit() {
        ArrowManager arrows = plugin.getModuleCache().getArrowManager();
        assertNotNull(arrows, "ArrowManager module should be enabled");
        assertNotNull(arrows.getItemById("arrow_pierce"), "Shipped 'arrow_pierce' item should have loaded");

        Arrow arrow = victim.getWorld().spawn(victim.getLocation(), Arrow.class);
        arrow.setShooter(shooter);
        // What ArrowManager records on a projectile when a custom arrow is fired
        arrow.setMetadata("QRPG_ARROW_ID", new FixedMetadataValue(plugin, "arrow_pierce"));
        arrow.setMetadata("QRPG_ARROW_LEVEL", new FixedMetadataValue(plugin, 1));
        assertNotNull(arrows.getArrow(arrow), "Test setup: the projectile should be recognised as a custom arrow");

        Map<DamageModifier, Double> modifiers = new EnumMap<>(DamageModifier.class);
        modifiers.put(DamageModifier.BASE, 5D);
        Map<DamageModifier, Function<? super Double, Double>> functions = new EnumMap<>(DamageModifier.class);
        functions.put(DamageModifier.BASE, d -> 5D);
        return new EntityDamageByEntityEvent(arrow, victim, DamageCause.PROJECTILE, modifiers, functions);
    }

    private void refresh(PlayerMock player) {
        EntityStats.purge(player);
        EntityStats.get(player).updateAll();
    }

    private double defenseOf(PlayerMock player) {
        return EntityStats.get(player).getDefenseTypes(false).getOrDefault(physicalDefense(), 0D);
    }

    private static DefenseAttribute physicalDefense() {
        return Objects.requireNonNull(ItemStats.getDefenseById("physical"));
    }
}
