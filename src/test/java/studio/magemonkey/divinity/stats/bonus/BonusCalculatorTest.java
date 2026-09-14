package studio.magemonkey.divinity.stats.bonus;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BonusCalculatorTest {

    private static BiFunction<Boolean, Double, Double> flat(double base) {
        return (isPercent, input) -> isPercent ? input : input + base;
    }

    private static BiFunction<Boolean, Double, Double> percent(double percent) {
        return (isPercent, input) -> isPercent ? input + percent : input;
    }

    @Test
    void simpleFull_percentOnlyBonus_evaluatesToZero() {
        // Documents SIMPLE_FULL's multiplicative semantics: percent modifies a flat base,
        // so a percent-only bonus on a zero base has nothing to multiply and yields 0.
        double result = BonusCalculator.SIMPLE_FULL.apply(0D, List.of(flat(0D), percent(25D)));

        assertEquals(0D, result, 0.001);
    }

    @Test
    void simpleAdditive_percentOnlyBonus_returnsThePercentValue() {
        double result = BonusCalculator.SIMPLE_ADDITIVE.apply(0D, List.of(flat(0D), percent(25D)));

        assertEquals(25D, result, 0.001);
    }

    @Test
    void simpleAdditive_flatAndPercentBonuses_areSummed() {
        double result = BonusCalculator.SIMPLE_ADDITIVE.apply(0D, List.of(flat(10D), percent(15D)));

        assertEquals(25D, result, 0.001);
    }

    @Test
    void simpleAdditive_flatOnlyBonus_returnsTheFlatValue() {
        double result = BonusCalculator.SIMPLE_ADDITIVE.apply(0D, List.of(flat(12D), percent(0D)));

        assertEquals(12D, result, 0.001);
    }
}
