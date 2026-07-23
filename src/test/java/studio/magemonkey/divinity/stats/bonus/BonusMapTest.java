package studio.magemonkey.divinity.stats.bonus;

import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.jupiter.api.Test;
import studio.magemonkey.codex.config.api.JYML;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.stats.items.attributes.DefenseAttribute;
import studio.magemonkey.divinity.stats.items.attributes.stats.DynamicBuffStat;
import studio.magemonkey.divinity.stats.items.attributes.stats.PenetrationStat;
import studio.magemonkey.divinity.testutil.MockedTest;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    void loadDamageBuffs_flatValue_appliesToRegisteredBuff() throws Exception {
        DynamicBuffStat buff = new DynamicBuffStat(
                DynamicBuffStat.BuffTarget.DAMAGE,
                "bonusmap_test_damage_buff",
                "Test Damage Buff",
                "&3▸ %name%: &f%value%%condition%",
                Set.of("physical"),
                -1D
        );

        JYML cfg = newConfig();
        cfg.set("damage-buffs." + buff.getBuffId(), "12");

        BonusMap bMap = new BonusMap();
        bMap.loadDamageBuffs(cfg, "damage-buffs");

        double result = BonusCalculator.SIMPLE_FULL.apply(0D, List.of(bMap.getBonus(buff)));

        assertEquals(12D, result, 0.001);
    }

    @Test
    void loadDefenseBuffs_percentValue_appliesToRegisteredBuff() throws Exception {
        DynamicBuffStat buff = new DynamicBuffStat(
                DynamicBuffStat.BuffTarget.DEFENSE,
                "bonusmap_test_defense_buff",
                "Test Defense Buff",
                "&9▸ %name%: &f%value%%condition%",
                Set.of("physical"),
                -1D
        );

        JYML cfg = newConfig();
        cfg.set("defense-buffs." + buff.getBuffId(), "20%");

        BonusMap bMap = new BonusMap();
        bMap.loadDefenseBuffs(cfg, "defense-buffs");

        BiFunction<Boolean, Double, Double> bonus = bMap.getBonus(buff);
        double flatResult    = BonusCalculator.SIMPLE_FULL.apply(0D, List.of(bonus));
        double percentResult = bonus.apply(true, 0D);

        assertEquals(0D, flatResult, 0.001, "A percent-only entry should not contribute a flat bonus");
        assertEquals(20D, percentResult, 0.001);
    }

    @Test
    void loadPenetrations_flatValue_appliesToRegisteredPenetration() throws Exception {
        PenetrationStat pen = new PenetrationStat(
                "bonusmap_test_pen",
                "Test Penetration",
                "&c▸ %name%: &f%value%%condition%",
                Set.of("physical"),
                false,
                -1D
        );

        JYML cfg = newConfig();
        cfg.set("penetrations." + pen.getPenId(), "8");

        BonusMap bMap = new BonusMap();
        bMap.loadPenetrations(cfg, "penetrations");

        double result = BonusCalculator.SIMPLE_FULL.apply(0D, List.of(bMap.getBonus(pen)));

        assertEquals(8D, result, 0.001);
    }

    @Test
    void loadDamageBuffs_unknownId_isIgnoredWithoutError() throws Exception {
        DynamicBuffStat knownBuff = new DynamicBuffStat(
                DynamicBuffStat.BuffTarget.DAMAGE,
                "bonusmap_test_unknown_id_control",
                "Control Buff",
                "&3▸ %name%: &f%value%%condition%",
                Set.of("physical"),
                -1D
        );

        JYML cfg = newConfig();
        cfg.set("damage-buffs.nonexistent_buff_id", "50");

        BonusMap bMap = new BonusMap();
        // Should not throw despite the unresolvable id, and should leave unrelated stats untouched.
        bMap.loadDamageBuffs(cfg, "damage-buffs");

        assertNull(bMap.getBonus(knownBuff));
    }
}
