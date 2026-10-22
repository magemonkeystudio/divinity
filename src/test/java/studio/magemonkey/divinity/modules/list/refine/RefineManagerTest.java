package studio.magemonkey.divinity.modules.list.refine;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.stats.bonus.StatBonus;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.stats.items.attributes.DamageAttribute;
import studio.magemonkey.divinity.testutil.MockedTest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for issue #346: the stat lore of a refined item must reflect
 * the refine level it was just given, not the previous one.
 */
class RefineManagerTest extends MockedTest {
    private RefineManager            refine;
    private RefineManager.RefineItem stone;
    private DamageAttribute          physical;

    @BeforeAll
    void setupAll() throws IOException {
        useRealisticColorFixing();

        // Default module items are not extracted in the mocked environment, so provide the sample stone
        File file = new File(server.getPluginsFolder().getAbsoluteFile() + File.separator
                + "Divinity-" + System.getProperty("DIVINITY_VERSION") + File.separator
                + "modules" + File.separator + "refine" + File.separator
                + "items" + File.separator + "sample_stone.yml");
        file.getParentFile().mkdirs();
        try (InputStream in = Objects.requireNonNull(this.getClass()
                .getClassLoader()
                .getResourceAsStream("modules/refine/items/sample_stone.yml"))) {
            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        Divinity.getInstance().reload();
    }

    @BeforeEach
    void setup() {
        refine = plugin.getModuleCache().getRefineManager();
        assertNotNull(refine, "Refine module should be loaded");
        stone = refine.getItemById("sample_stone");
        assertNotNull(stone, "sample_stone should be loaded");
        physical = ItemStats.getDamageById("physical");
        assertNotNull(physical, "physical damage type should be registered");
    }

    private ItemStack createWeapon() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta  meta = item.getItemMeta();
        meta.setDisplayName("Test Sword");
        meta.setLore(new ArrayList<>());
        item.setItemMeta(meta);
        assertTrue(physical.add(item, new StatBonus(new double[]{100, 100}, false, null), -1));
        return item;
    }

    private String damageLine(ItemStack item) {
        List<String> lore = item.getItemMeta().getLore();
        assertNotNull(lore);
        return lore.stream()
                .map(ChatColor::stripColor)
                .filter(line -> line.contains(physical.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Damage line should be present in lore: " + lore));
    }

    @Test
    void refineItem_firstRefine_showsLevelOneBonus() {
        ItemStack item = createWeapon();
        assertFalse(damageLine(item).contains("(+"), "Unrefined item should show no bonus");

        refine.refineItem(item, stone);

        assertEquals(1, refine.getRefineLevel(item));
        // sample_stone level 1: physical +1% of 100
        assertTrue(damageLine(item).contains("(+1"),
                "Lore should show the level 1 bonus right away, was: " + damageLine(item));
    }

    @Test
    void refineItem_secondRefine_showsLevelTwoBonus() {
        ItemStack item = createWeapon();

        refine.refineItem(item, stone);
        refine.refineItem(item, stone);

        assertEquals(2, refine.getRefineLevel(item));
        // sample_stone level 2: physical +1.5% of 100
        assertTrue(damageLine(item).contains("(+1.5"),
                "Lore should show the level 2 bonus, was: " + damageLine(item));
        List<String> lore = item.getItemMeta().getLore();
        assertEquals(1, lore.stream().filter(line -> line.contains("Refined with")).count(),
                "Previous refine lore should be replaced, was: " + lore);
    }

    @Test
    void downgradeItem_toZero_removesBonusFromLore() {
        ItemStack item = createWeapon();

        refine.refineItem(item, stone);
        refine.downgradeItem(item, true);

        assertEquals(0, refine.getRefineLevel(item));
        assertFalse(damageLine(item).contains("(+"),
                "Downgraded item should no longer show a bonus, was: " + damageLine(item));
    }
}
