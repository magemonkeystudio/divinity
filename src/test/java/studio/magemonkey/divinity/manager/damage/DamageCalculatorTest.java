package studio.magemonkey.divinity.manager.damage;

import org.junit.jupiter.api.Test;
import studio.magemonkey.divinity.manager.damage.DamageCalculator.Modifiers;
import studio.magemonkey.divinity.manager.damage.DamageCalculator.Result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Plain unit tests of the damage formula: no server, no plugin. Every expected value is worked out by hand in
 * the comment above it.
 */
class DamageCalculatorTest {
    private static final double TOLERANCE = 1e-9;

    private static Modifiers mods(double pveDamage, double critical, double enchant, boolean applyPower,
                                  double power, double block, double direct, double pveDefense,
                                  double penetrate, double toughness, boolean legacy) {
        return new Modifiers(pveDamage, critical, enchant, applyPower, power, block, direct, pveDefense, penetrate,
                toughness, legacy);
    }

    /** All modifiers neutral, current formula, no toughness. */
    private static Modifiers neutralMods() {
        return mods(1, 1, 1, false, 1, 1, 0, 1, 1, 0, false);
    }

    private static Result noDefense(Modifiers m, double damage) {
        return new DamageCalculator(m).calculate(damage, 1, 1, null, 0);
    }

    @Test
    void multiplierChain() {
        // 10 * 1.5 (pve) = 15; * 2 (crit) = 30; * 2 (entity type) = 60; * 0.5 (faction) = 30;
        // * 0.8 (enchant) = 24; * 1.25 (attack power) = 30; * 0.5 (block) = 15
        Modifiers m = mods(1.5, 2, 0.8, true, 1.25, 0.5, 0, 1, 1, 0, false);
        Result r = new DamageCalculator(m).calculate(10, 2, 0.5, null, 0);
        assertEquals(15, r.getDamage(), TOLERANCE);
        assertFalse(r.isDefenseApplied());
        assertEquals(0, r.getDefended(), TOLERANCE);
    }

    @Test
    void attackPowerSkippedWhenNotApplicable() {
        // Attack power 3 is ignored: 10 * 0.5 (block) = 5. Were it applied it would be 15.
        assertEquals(5, noDefense(mods(1, 1, 1, false, 3, 0.5, 0, 1, 1, 0, false), 10).getDamage(), TOLERANCE);
        // Applied: 10 * 3 * 0.5 = 15
        assertEquals(15, noDefense(mods(1, 1, 1, true, 3, 0.5, 0, 1, 1, 0, false), 10).getDamage(), TOLERANCE);
    }

    @Test
    void directDamageIsNotDefended() {
        // direct = 20 * 0.25 = 5; remaining = 20 - 5 = 15.
        // Legacy defense: def = 50 * 1 * 1 = 50; 15 * (1 - 50 * 1 * 0.01) = 7.5; defended = 15 - 7.5 = 7.5.
        // Result = 7.5 + 5 (direct) = 12.5
        Modifiers m = mods(1, 1, 1, false, 1, 1, 0.25, 1, 1, 0, true);
        Result r = new DamageCalculator(m).calculate(20, 1, 1, 50D, 1);
        assertEquals(12.5, r.getDamage(), TOLERANCE);
        assertTrue(r.isDefenseApplied());
        assertEquals(7.5, r.getDefended(), TOLERANCE);
    }

    @Test
    void legacyDefenseFormula() {
        // def = 40 * 1.5 (pve defense) * 0.5 (penetration) = 30
        // 100 * (1 - 30 * 2 (protection factor) * 0.01) = 100 * 0.4 = 40; defended = 60
        Modifiers m = mods(1, 1, 1, false, 1, 1, 0, 1.5, 0.5, 0, true);
        Result r = new DamageCalculator(m).calculate(100, 1, 1, 40D, 2);
        assertEquals(40, r.getDamage(), TOLERANCE);
        assertEquals(60, r.getDefended(), TOLERANCE);
    }

    @Test
    void legacyFormulaIgnoresToughness() {
        // Same as above with toughness 100: legacy formula does not use it. 10 * (1 - 20 * 1 * 0.01) = 8
        Modifiers m = mods(1, 1, 1, false, 1, 1, 0, 1, 1, 100, true);
        assertEquals(8, new DamageCalculator(m).calculate(10, 1, 1, 20D, 1).getDamage(), TOLERANCE);
    }

    @Test
    void currentDefenseFormulaWithToughness() {
        // def = 10; def / 5 = 2; def - 4 * 10 / max(1, 2 + 8) = 10 - 4 = 6; max(2, 6) = 6
        // 10 * (1 - 6 * 1 * 0.05) = 10 * 0.7 = 7; defended = 3
        Modifiers m = mods(1, 1, 1, false, 1, 1, 0, 1, 1, 2, false);
        Result r = new DamageCalculator(m).calculate(10, 1, 1, 10D, 1);
        assertEquals(7, r.getDamage(), TOLERANCE);
        assertEquals(3, r.getDefended(), TOLERANCE);
    }

    @Test
    void currentDefenseFormulaToughnessFloor() {
        // toughness -20: max(1, -20 + 8) = 1. def = 10; def / 5 = 2; def - 4 * 10 / 1 = -30; max(2, -30) = 2
        // 10 * (1 - 2 * 1 * 0.05) = 9; defended = 1
        Modifiers m = mods(1, 1, 1, false, 1, 1, 0, 1, 1, -20, false);
        Result r = new DamageCalculator(m).calculate(10, 1, 1, 10D, 1);
        assertEquals(9, r.getDamage(), TOLERANCE);
        assertEquals(1, r.getDefended(), TOLERANCE);
    }

    @Test
    void currentDefenseFormulaWithPenetrationAndPveDefense() {
        // def = 20 * 1.5 * 0.8 = 24; def / 5 = 4.8; toughness 2: 24 - 4 * 10 / 10 = 20; max(4.8, 20) = 20
        // 10 * (1 - 20 * 0.5 (protection factor) * 0.05) = 10 * 0.5 = 5; defended = 5
        Modifiers m = mods(1, 1, 1, false, 1, 1, 0, 1.5, 0.8, 2, false);
        Result r = new DamageCalculator(m).calculate(10, 1, 1, 20D, 0.5);
        assertEquals(5, r.getDamage(), TOLERANCE);
        assertEquals(5, r.getDefended(), TOLERANCE);
    }

    @Test
    void defenseSkippedWhenNoDamageRemains() {
        // Zero damage: nothing to defend.
        Result zero = new DamageCalculator(neutralMods()).calculate(0, 1, 1, 50D, 1);
        assertEquals(0, zero.getDamage(), TOLERANCE);
        assertFalse(zero.isDefenseApplied());

        // Block modifier 0: 10 * 0 = 0.
        Result blocked = new DamageCalculator(mods(1, 1, 1, false, 1, 0, 0, 1, 1, 0, true))
                .calculate(10, 1, 1, 50D, 1);
        assertEquals(0, blocked.getDamage(), TOLERANCE);
        assertFalse(blocked.isDefenseApplied());

        // All direct: direct = 10 * 1 = 10; remaining = 0, so the defense is skipped and direct passes through.
        Result direct = new DamageCalculator(mods(1, 1, 1, false, 1, 1, 1, 1, 1, 0, true))
                .calculate(10, 1, 1, 50D, 1);
        assertEquals(10, direct.getDamage(), TOLERANCE);
        assertFalse(direct.isDefenseApplied());
    }

    @Test
    void noDefenseGivenMeansNoDefenseApplied() {
        Result r = new DamageCalculator(neutralMods()).calculate(10, 1, 1, null, 5);
        assertEquals(10, r.getDamage(), TOLERANCE);
        assertFalse(r.isDefenseApplied());
        assertEquals(0, r.getDefended(), TOLERANCE);
    }

    @Test
    void defenseLargerThanDamageGivesZeroNotNegative() {
        // Legacy: def = 200; 10 * (1 - 200 * 1 * 0.01) = 10 * -1 = -10 -> clamped to 0; defended = 10
        Result legacy = new DamageCalculator(mods(1, 1, 1, false, 1, 1, 0, 1, 1, 0, true))
                .calculate(10, 1, 1, 200D, 1);
        assertEquals(0, legacy.getDamage(), TOLERANCE);
        assertEquals(10, legacy.getDefended(), TOLERANCE);

        // Current: def = 100; def / 5 = 20; 100 - 4 * 10 / max(1, 0 + 8) = 95; max = 95
        // 10 * (1 - 95 * 1 * 0.05) = 10 * -3.75 -> clamped to 0; defended = 10
        Result current = new DamageCalculator(neutralMods()).calculate(10, 1, 1, 100D, 1);
        assertEquals(0, current.getDamage(), TOLERANCE);
        assertEquals(10, current.getDefended(), TOLERANCE);
    }

    @Test
    void negativeDefenseIsClampedToZero() {
        // def = max(0, -10 * 1 * 1) = 0, so nothing is absorbed, but the defense still counts as applied.
        Result r = new DamageCalculator(mods(1, 1, 1, false, 1, 1, 0, 1, 1, 0, true))
                .calculate(10, 1, 1, -10D, 1);
        assertEquals(10, r.getDamage(), TOLERANCE);
        assertTrue(r.isDefenseApplied());
        assertEquals(0, r.getDefended(), TOLERANCE);
    }

    @Test
    void directLargerThanDamageClampsRemainingToZero() {
        // direct = 10 * 1.5 = 15; remaining = max(0, 10 - 15) = 0; result = 0 + 15
        Result r = noDefense(mods(1, 1, 1, false, 1, 1, 1.5, 1, 1, 0, false), 10);
        assertEquals(15, r.getDamage(), TOLERANCE);
    }
}
