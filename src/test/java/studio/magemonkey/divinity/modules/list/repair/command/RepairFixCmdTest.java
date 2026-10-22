package studio.magemonkey.divinity.modules.list.repair.command;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.command.ConsoleCommandSenderMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import studio.magemonkey.divinity.modules.list.repair.RepairManager;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.stats.items.attributes.stats.DurabilityStat;
import studio.magemonkey.divinity.testutil.MockedTest;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers the /repair fix command added for issue #200.
 */
class RepairFixCmdTest extends MockedTest {
    private RepairManager  repair;
    private RepairFixCmd   cmd;
    private DurabilityStat durability;
    private PlayerMock     player;
    private int            playerCount;

    @BeforeAll
    void setupAll() {
        useRealisticColorFixing();
    }

    @BeforeEach
    void setup() {
        repair = plugin.getModuleCache().getRepairManager();
        assertNotNull(repair, "Repair module should be loaded");
        durability = ItemStats.getStat(DurabilityStat.class);
        assertNotNull(durability, "Durability stat should be registered");
        cmd = new RepairFixCmd(repair);
        // The command resolves its target by name, so each test needs a uniquely named player
        player = genPlayer("RepairTester" + playerCount++);
    }

    private ItemStack createItem(Material material, double current, double max) {
        ItemStack item = new ItemStack(material);
        ItemMeta  meta = item.getItemMeta();
        meta.setLore(new ArrayList<>());
        item.setItemMeta(meta);
        durability.add(item, new double[]{current, max}, -1);
        return item;
    }

    private double currentDurability(ItemStack item) {
        double[] raw = durability.getRaw(item);
        assertNotNull(raw, "Item should have durability");
        return raw[0];
    }

    @Test
    void repairItem_damagedItem_restoresMaxDurability() {
        ItemStack item = createItem(Material.DIAMOND_SWORD, 40, 100);

        assertTrue(repair.repairItem(item));
        assertEquals(100, currentDurability(item));
    }

    @Test
    void repairItem_undamagedItem_returnsFalse() {
        ItemStack item = createItem(Material.DIAMOND_SWORD, 100, 100);

        assertFalse(repair.repairItem(item));
    }

    @Test
    void repairItem_itemWithoutDurability_returnsFalse() {
        assertFalse(repair.repairItem(new ItemStack(Material.DIAMOND_SWORD)));
    }

    @Test
    void perform_noArgs_repairsMainHand() {
        player.getInventory().setItemInMainHand(createItem(Material.DIAMOND_SWORD, 10, 100));

        cmd.perform(player, "repair", new String[]{"fix"});

        assertEquals(100, currentDurability(player.getInventory().getItemInMainHand()));
    }

    @Test
    void perform_hand_doesNotTouchArmor() {
        player.getInventory().setItemInMainHand(createItem(Material.DIAMOND_SWORD, 10, 100));
        player.getInventory().setHelmet(createItem(Material.DIAMOND_HELMET, 10, 100));

        cmd.perform(player, "repair", new String[]{"fix", player.getName(), "hand"});

        assertEquals(100, currentDurability(player.getInventory().getItemInMainHand()));
        assertEquals(10, currentDurability(player.getInventory().getHelmet()));
    }

    @Test
    void perform_all_repairsHandsAndArmor() {
        player.getInventory().setItemInMainHand(createItem(Material.DIAMOND_SWORD, 10, 100));
        player.getInventory().setItemInOffHand(createItem(Material.SHIELD, 20, 100));
        player.getInventory().setHelmet(createItem(Material.DIAMOND_HELMET, 30, 100));
        player.getInventory().setBoots(createItem(Material.DIAMOND_BOOTS, 40, 100));

        cmd.perform(player, "repair", new String[]{"fix", player.getName(), "all"});

        assertEquals(100, currentDurability(player.getInventory().getItemInMainHand()));
        assertEquals(100, currentDurability(player.getInventory().getItemInOffHand()));
        assertEquals(100, currentDurability(player.getInventory().getHelmet()));
        assertEquals(100, currentDurability(player.getInventory().getBoots()));
    }

    @Test
    void perform_fromConsoleForOtherPlayer_repairsTheirItem() {
        player.getInventory().setItemInMainHand(createItem(Material.DIAMOND_SWORD, 10, 100));
        ConsoleCommandSenderMock console = (ConsoleCommandSenderMock) server.getConsoleSender();

        cmd.perform(console, "repair", new String[]{"fix", player.getName()});

        assertEquals(100, currentDurability(player.getInventory().getItemInMainHand()));
        assertNotNull(console.nextMessage(), "Console should be told the repair happened");
    }

    @Test
    void perform_fromConsoleWithoutPlayer_doesNothing() {
        ConsoleCommandSenderMock console = (ConsoleCommandSenderMock) server.getConsoleSender();

        assertDoesNotThrow(() -> cmd.perform(console, "repair", new String[]{"fix"}));
    }
}
