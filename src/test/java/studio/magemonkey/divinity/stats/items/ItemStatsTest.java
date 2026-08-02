package studio.magemonkey.divinity.stats.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import studio.magemonkey.divinity.config.EngineCfg;
import studio.magemonkey.divinity.testutil.MockedTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
