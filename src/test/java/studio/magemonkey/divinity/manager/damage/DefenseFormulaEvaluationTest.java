package studio.magemonkey.divinity.manager.damage;

import org.junit.jupiter.api.Test;
import studio.magemonkey.divinity.testutil.MockedTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Covers the CUSTOM defense-formula string evaluation (DamageManager#evaluateDefenseFormula /
 * #evaluateOverflowFormula), package-visible specifically so these pure-logic pieces can be
 * exercised directly without a full combat event simulation.
 */
class DefenseFormulaEvaluationTest extends MockedTest {

    @Test
    void evaluateDefenseFormula_defaultFormula_appliesFactorStyleReduction() {
        // Default engine.yml formula: damage*(25/(25+defense))
        double result = DamageManager.evaluateDefenseFormula(
                "damage*(25/(25+defense))", 100D, 25D, 0D, Map.of());

        assertEquals(50D, result, 0.001);
    }

    @Test
    void evaluateDefenseFormula_zeroDefense_dealsFullDamage() {
        double result = DamageManager.evaluateDefenseFormula(
                "damage*(25/(25+defense))", 100D, 0D, 0D, Map.of());

        assertEquals(100D, result, 0.001);
    }

    @Test
    void evaluateDefenseFormula_individualDefensePlaceholder_substitutedBeforeSumPlaceholder() {
        // "defense" is a prefix of "defense_physical", so the individual placeholder must be
        // substituted first or this formula would break.
        double result = DamageManager.evaluateDefenseFormula(
                "damage - defense_physical", 100D, 999D, 0D, Map.of("physical", 30D));

        assertEquals(70D, result, 0.001);
    }

    @Test
    void evaluateOverflowFormula_defaultFormula_scalesOverflowByDamage() {
        // Default engine.yml formula: damage*(overflow/100)
        double result = DamageManager.evaluateOverflowFormula(
                "damage*(overflow/100)", 50D, 20D, 10D);

        assertEquals(10D, result, 0.001);
    }

    @Test
    void evaluateOverflowFormula_negativeResult_clampsToZero() {
        double result = DamageManager.evaluateOverflowFormula(
                "damage - 1000", 10D, 20D, 5D);

        assertEquals(0D, result, 0.001);
    }
}
