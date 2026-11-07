package studio.magemonkey.divinity.stats.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import studio.magemonkey.divinity.config.EngineCfg;
import studio.magemonkey.divinity.stats.bonus.StatBonus;
import studio.magemonkey.divinity.stats.items.attributes.api.SimpleStat;
import studio.magemonkey.divinity.stats.items.attributes.api.TypedStat;
import studio.magemonkey.divinity.stats.items.attributes.stats.DurabilityStat;
import studio.magemonkey.divinity.testutil.MockedTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemStatsTest extends MockedTest {
    private PlayerMock player;

    @BeforeEach
    void setup() {
        player = genPlayer("Travja");
    }

    @AfterEach
    void resetFlag() {
        EngineCfg.VANILLA_ONLY_ITEM_STATS = false;
    }

    @Test
    void legacyVanillaItemStatsLeavesItemCompletelyUntouched() {
        EngineCfg.VANILLA_ONLY_ITEM_STATS = true;

        ItemStack item   = new ItemStack(Material.DIAMOND_SWORD);
        ItemStack before = item.clone();

        ItemStats.updateVanillaAttributes(item, player);

        assertEquals(before,
                item,
                "Item should not be modified at all while legacy vanilla item stats is enabled");
    }

    @Test
    void hiddenStatRemainsStoredAndAppliedWithoutLore() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        SimpleStat stat = (SimpleStat) ItemStats.getStat(TypedStat.Type.CRITICAL_RATE);
        assertNotNull(stat);

        assertTrue(stat.addWithoutLore(item, new StatBonus(new double[]{25}, false, null)));

        assertEquals(25, stat.getTotal(item, player));
        assertTrue(stat.isApplied(item));
        assertNull(item.getItemMeta().getLore());
    }

    @Test
    void reducingHiddenDurabilityDoesNotAddLore() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        DurabilityStat stat = ItemStats.getStat(DurabilityStat.class);
        assertNotNull(stat);
        assertTrue(stat.addWithoutLore(item, new double[]{100, 100}));

        assertTrue(stat.reduceDurability(player, item, 1));

        assertEquals(99, stat.getRaw(item)[0]);
        assertTrue(stat.isApplied(item));
        assertFalse(item.getItemMeta().hasLore());
    }
}
