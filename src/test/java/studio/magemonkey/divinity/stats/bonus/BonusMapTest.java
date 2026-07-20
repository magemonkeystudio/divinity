package studio.magemonkey.divinity.stats.bonus;

import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.jupiter.api.Test;
import studio.magemonkey.codex.config.api.JYML;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.stats.items.attributes.DefenseAttribute;
import studio.magemonkey.divinity.testutil.MockedTest;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression tests for defense-ignoring bonuses (e.g. Divinity custom arrows), which use
 * {@link BonusMap#loadDefenses(JYML, String, double)} with a negative sign to reduce a victim's
 * defense on hit rather than grant defense. A prior implementation negated the loaded function's
 * entire output via {@code andThen(result -> -result)}, which also flipped the sign of the
 * passthrough branch used by {@link BonusCalculator#SIMPLE_FULL} to carry forward the victim's
 * already-accumulated armor, causing that armor to be dropped from the defense map entirely.
 */
class BonusMapTest extends MockedTest {

    private JYML newConfig() throws IOException, InvalidConfigurationException {
        return new JYML(Files.createTempFile("bonus-map-test", ".yml").toFile());
    }

    @Test
    void loadDefenses_negativeSign_reducesFlatDefenseWithoutErasingExistingArmor() throws Exception {
        DefenseAttribute defense = ItemStats.getDefenses().iterator().next();

        JYML cfg = newConfig();
        cfg.set("defense-ignoring." + defense.getId(), "10");

        BonusMap arrowBonus = new BonusMap();
        arrowBonus.loadDefenses(cfg, "defense-ignoring", -1D);

        // Simulate a victim who already has 50 flat armor of this type equipped.
        BiFunction<Boolean, Double, Double> existingArmor = (isPercent, apply) -> isPercent ? apply : apply + 50D;

        double result = BonusCalculator.SIMPLE_FULL.apply(
                0D,
                List.of(existingArmor, arrowBonus.getBonus(defense))
        );

        // Existing armor (50) minus the arrow's piercing value (10), not wiped out entirely.
        assertEquals(40D, result, 0.001);
    }

    @Test
    void loadDefenses_zeroPercentPiercing_isNoOpAgainstExistingArmor() throws Exception {
        DefenseAttribute defense = ItemStats.getDefenses().iterator().next();

        JYML cfg = newConfig();
        cfg.set("defense-ignoring." + defense.getId(), "0%");

        BonusMap arrowBonus = new BonusMap();
        arrowBonus.loadDefenses(cfg, "defense-ignoring", -1D);

        BiFunction<Boolean, Double, Double> existingArmor = (isPercent, apply) -> isPercent ? apply : apply + 50D;

        double result = BonusCalculator.SIMPLE_FULL.apply(
                0D,
                List.of(existingArmor, arrowBonus.getBonus(defense))
        );

        // 0% piercing should leave the victim's existing armor fully intact.
        assertEquals(50D, result, 0.001);
    }

    @Test
    void loadDefenses_positiveSign_stillGrantsDefense() throws Exception {
        DefenseAttribute defense = ItemStats.getDefenses().iterator().next();

        JYML cfg = newConfig();
        cfg.set("defense-types." + defense.getId(), "15");

        BonusMap grantBonus = new BonusMap();
        grantBonus.loadDefenses(cfg, "defense-types");

        double result = BonusCalculator.SIMPLE_FULL.apply(0D, List.of(grantBonus.getBonus(defense)));

        assertEquals(15D, result, 0.001);
    }
}
