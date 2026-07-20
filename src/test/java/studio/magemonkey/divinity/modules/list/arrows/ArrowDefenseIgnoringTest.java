package studio.magemonkey.divinity.modules.list.arrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.modules.list.arrows.ArrowManager.QArrow;
import studio.magemonkey.divinity.stats.bonus.BonusCalculator;
import studio.magemonkey.divinity.stats.bonus.BonusMap;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.stats.items.attributes.DefenseAttribute;
import studio.magemonkey.divinity.testutil.MockedTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Reproduces the Discord report "[Divinity] Custom Arrows Cause Armour to be Ignored":
 * shooting the shipped "Piercing Arrow" (modules/arrows/items/arrow_pierce.yml, which ships
 * with {@code defense-ignoring.physical: 50.0%}) was wiping out the victim's entire physical
 * defense instead of only piercing 50% of it, regardless of the configured percentage.
 * <p>
 * This loads the real bundled arrow item through {@link ArrowManager} (exercising the actual
 * production config and parsing code) and combines its bonus with a stand-in for a victim's
 * existing armor the same way {@code EntityStats#getDefenseTypes} does - item-based defense
 * bonuses first, arrow bonus appended after - via {@link BonusCalculator#SIMPLE_FULL}.
 */
class ArrowDefenseIgnoringTest extends MockedTest {

    // MockBukkit's generated plugin jar doesn't carry loose main/resources files, so the module's
    // usual jar-resource extraction (ArrowManager -> QModuleDrop#loadItems) finds nothing on disk.
    // Copy the real shipped item config into place ourselves, same as ItemGeneratorManagerTest does.
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

    @Test
    void piercingArrow_halvesDefense_insteadOfErasingIt() {
        ArrowManager arrowManager = plugin.getModuleCache().getArrowManager();
        assertNotNull(arrowManager, "ArrowManager module should be enabled");

        QArrow arrow = arrowManager.getItemById("arrow_pierce");
        assertNotNull(arrow, "Shipped 'arrow_pierce' item should have loaded from modules/arrows/items");

        BonusMap bonusMap = arrow.getBonusMap(1);
        assertNotNull(bonusMap, "Piercing Arrow should have a level-1 bonus map");

        DefenseAttribute physical = ItemStats.getDefenseById("physical");
        assertNotNull(physical, "'physical' defense type should be registered by default");

        BiFunction<Boolean, Double, Double> arrowBonus = bonusMap.getBonus(physical);
        assertNotNull(arrowBonus, "Piercing Arrow should carry a defense-ignoring bonus for physical defense");

        // Victim already has 20 flat physical armor equipped.
        BiFunction<Boolean, Double, Double> existingArmor = (isPercent, apply) -> isPercent ? apply : apply + 20D;

        double defenseAfterHit = BonusCalculator.SIMPLE_FULL.apply(0D, List.of(existingArmor, arrowBonus));

        // 50% defense-ignoring should halve the victim's armor (20 -> 10), not erase it entirely.
        assertEquals(10D, defenseAfterHit, 0.001);
    }
}
