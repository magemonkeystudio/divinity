package studio.magemonkey.divinity.modules.list.itemgenerator.generators;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import studio.magemonkey.divinity.testutil.MockedTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypedStatGeneratorTest extends MockedTest {

    @Test
    void aoeDamageIsAllowedOnlyForConfiguredWeaponTypes() {
        assertTrue(TypedStatGenerator.isAoeDamageAllowed(Material.NETHERITE_SWORD));
        assertTrue(TypedStatGenerator.isAoeDamageAllowed(Material.FISHING_ROD));
        assertTrue(TypedStatGenerator.isAoeDamageAllowed(Material.TRIDENT));
        assertTrue(TypedStatGenerator.isAoeDamageAllowed(Material.DIAMOND_HOE));

        assertFalse(TypedStatGenerator.isAoeDamageAllowed(Material.IRON_AXE));
        assertFalse(TypedStatGenerator.isAoeDamageAllowed(Material.BOW));
        assertFalse(TypedStatGenerator.isAoeDamageAllowed(Material.IRON_CHESTPLATE));
    }

    @Test
    void itemGroupsRestrictGeneratedStatsToConfiguredItems() {
        List<String> groups = List.of("sword", "axe");

        assertTrue(TypedStatGenerator.isAllowedByItemGroups(new ItemStack(Material.IRON_SWORD), groups));
        assertTrue(TypedStatGenerator.isAllowedByItemGroups(new ItemStack(Material.IRON_AXE), groups));
        assertFalse(TypedStatGenerator.isAllowedByItemGroups(new ItemStack(Material.IRON_CHESTPLATE), groups));
    }

    @Test
    void missingGroupsAreUnrestrictedButExplicitEmptyGroupsAreNot() {
        assertTrue(TypedStatGenerator.isAllowedByItemGroups(new ItemStack(Material.IRON_CHESTPLATE), null));
        assertFalse(TypedStatGenerator.isAllowedByItemGroups(new ItemStack(Material.IRON_CHESTPLATE), List.of()));
    }
}
