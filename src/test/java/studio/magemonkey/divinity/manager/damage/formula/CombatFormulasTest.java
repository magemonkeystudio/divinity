package studio.magemonkey.divinity.manager.damage.formula;

import org.junit.jupiter.api.Test;
import studio.magemonkey.divinity.manager.damage.DamageCalculator;
import studio.magemonkey.divinity.manager.damage.DamageCalculator.Modifiers;
import studio.magemonkey.divinity.manager.damage.DamageCalculator.Result;
import studio.magemonkey.divinity.manager.damage.ReferenceDamageCalculator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CombatFormulasTest {

    private static final double DELTA = 1e-9;

    /** A hit with a defense: 20 damage, 30 defense, protection factor 0.8, toughness 0, everything else neutral. */
    private static Modifiers neutral(boolean legacy) {
        return new Modifiers(1, 1, 1, true, 1, 1, 0, 1, 1, 0, legacy);
    }

    private static Result hit(CombatFormulas formulas, Modifiers m) {
        return new DamageCalculator(m, formulas).calculate(20, 1, 1, 30D, 0.8);
    }

    // ------------------------------------------------------------------ //
    // The shipped file and the code agree
    // ------------------------------------------------------------------ //

    @Test
    void shippedFile_documentsEveryExpressionItsVariablesAndItsDefault() throws IOException {
        String file;
        try (InputStream in = getClass().getResourceAsStream("/combat.yml")) {
            assertNotNull(in, "combat.yml is not on the classpath");
            file = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }

        for (CombatExpression expression : CombatExpression.values()) {
            String key = expression.key();
            assertTrue(file.contains("# " + key + ": \"" + expression.defaultExpression() + "\""),
                    key + ": the default expression in combat.yml differs from the code");
            if (expression.legacyDefault() != null) {
                assertTrue(file.contains("# " + key + ": \"" + expression.legacyDefault() + "\""),
                        key + ": the legacy expression in combat.yml differs from the code");
            }
            for (String variable : expression.variables()) {
                assertTrue(file.matches("(?s).*# " + key + ":.*#   " + variable + " .*"),
                        key + ": variable '" + variable + "' is not documented in combat.yml");
            }
        }
        assertTrue(file.contains("\npreset: default\n"));
    }

    @Test
    void everyPresetExpressionCompilesAndIsFiniteForItsSample() {
        for (CombatPreset preset : CombatPreset.values()) {
            List<String> warnings = new ArrayList<>();
            CombatFormulas formulas = CombatFormulas.build(preset, null, warnings::add);
            assertTrue(warnings.isEmpty(), preset + ": " + warnings);
            for (CombatExpression expression : CombatExpression.values()) {
                assertFalse(formulas.isOverridden(expression));
                assertTrue(Double.isFinite(formulas.eval(expression, expression.sample())),
                        preset + "/" + expression);
            }
        }
    }

    @Test
    void legacyPresetDiffersFromDefaultOnlyInTheMitigation() {
        for (CombatExpression expression : CombatExpression.values()) {
            boolean differs = !CombatPreset.DEFAULT.expressionFor(expression)
                    .equals(CombatPreset.LEGACY.expressionFor(expression));
            assertEquals(expression == CombatExpression.DEFENSE_MITIGATION, differs, expression.key());
        }
    }

    // ------------------------------------------------------------------ //
    // The numbers did not change
    // ------------------------------------------------------------------ //

    @Test
    void presets_giveTheSameBitsAsTheJavaFormulasTheyReplaced() {
        Random random = new Random(371);
        for (CombatPreset preset : CombatPreset.values()) {
            CombatFormulas formulas = CombatFormulas.of(preset);
            for (int i = 0; i < 20_000; i++) {
                Modifiers m = new Modifiers(
                        0.5 + random.nextDouble(), 1 + random.nextInt(3) * random.nextDouble(),
                        random.nextDouble(), random.nextBoolean(), 0.2 + random.nextDouble(),
                        random.nextBoolean() ? 1 : random.nextDouble(),
                        random.nextInt(4) == 0 ? random.nextDouble() * 0.5 : 0,
                        0.5 + random.nextDouble(), random.nextDouble(), random.nextInt(21),
                        preset == CombatPreset.LEGACY);
                double  damage  = random.nextInt(10) == 0 ? 0 : random.nextDouble() * 300;
                double  entity  = random.nextBoolean() ? 1 : 0.5 + random.nextDouble();
                double  faction = random.nextBoolean() ? 1 : 0.5 + random.nextDouble();
                Double  defense = random.nextInt(4) == 0 ? null : random.nextDouble() * 200 - 20;
                double  protect = random.nextDouble() * 3;

                Result expected = new ReferenceDamageCalculator(m).calculate(damage, entity, faction, defense, protect);
                Result actual   = new DamageCalculator(m, formulas).calculate(damage, entity, faction, defense, protect);

                assertEquals(Double.doubleToLongBits(expected.getDamage()), Double.doubleToLongBits(actual.getDamage()),
                        preset + " damage, hit " + i + ": " + expected + " vs " + actual);
                assertEquals(Double.doubleToLongBits(expected.getDefended()),
                        Double.doubleToLongBits(actual.getDefended()), preset + " defended, hit " + i);
                assertEquals(expected.isDefenseApplied(), actual.isDefenseApplied(), preset + " applied, hit " + i);
            }
        }
    }

    @Test
    void oneCalculatorForSeveralDamageTypes_doesNotLeakArgumentsBetweenCalls() {
        // A call must not see anything the one before it left behind
        Random random = new Random(4_1);
        for (CombatPreset preset : CombatPreset.values()) {
            Modifiers m = new Modifiers(1.1, 1.5, 0.9, true, 0.8, 0.7, 0.1, 1.2, 0.9, 6, preset == CombatPreset.LEGACY);
            DamageCalculator          shared    = new DamageCalculator(m, CombatFormulas.of(preset));
            ReferenceDamageCalculator reference = new ReferenceDamageCalculator(m);
            for (int i = 0; i < 5_000; i++) {
                double damage  = random.nextDouble() * 100;
                Double defense = random.nextBoolean() ? null : random.nextDouble() * 100;
                double protect = random.nextDouble() * 2;
                double entity   = 0.5 + random.nextDouble();
                double faction  = 0.5 + random.nextDouble();
                Result expected = reference.calculate(damage, entity, faction, defense, protect);
                Result actual   = shared.calculate(damage, entity, faction, defense, protect);
                assertEquals(expected, actual, preset + ", call " + i);
            }
        }
    }

    @Test
    void oneCalculatorSharedBetweenThreads_givesEveryThreadItsOwnResults() throws Exception {
        // A calculator keeps no state between calls, so threads using the same one must not disturb each other
        Modifiers m = new Modifiers(1.1, 1.5, 0.9, true, 0.8, 0.7, 0.1, 1.2, 0.9, 6, false);
        for (CombatPreset preset : CombatPreset.values()) {
            DamageCalculator          shared    = new DamageCalculator(m, CombatFormulas.of(preset));
            ReferenceDamageCalculator reference = new ReferenceDamageCalculator(
                    new Modifiers(1.1, 1.5, 0.9, true, 0.8, 0.7, 0.1, 1.2, 0.9, 6, preset == CombatPreset.LEGACY));
            int                   threads  = 8;
            java.util.concurrent.atomic.AtomicInteger wrong = new java.util.concurrent.atomic.AtomicInteger();
            java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
            List<Thread> workers = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                final int seed = t;
                Thread worker = new Thread(() -> {
                    Random random = new Random(seed);
                    try {
                        start.await();
                    } catch (InterruptedException e) {
                        return;
                    }
                    for (int i = 0; i < 20_000; i++) {
                        double damage  = 1 + random.nextDouble() * 200;
                        double entity  = 0.5 + random.nextDouble();
                        double faction = 0.5 + random.nextDouble();
                        Double defense = random.nextInt(4) == 0 ? null : random.nextDouble() * 100;
                        double protect = random.nextDouble() * 2;
                        Result expected = reference.calculate(damage, entity, faction, defense, protect);
                        Result actual   = shared.calculate(damage, entity, faction, defense, protect);
                        if (!expected.equals(actual)) wrong.incrementAndGet();
                    }
                });
                workers.add(worker);
                worker.start();
            }
            start.countDown();
            for (Thread worker : workers) worker.join();
            assertEquals(0, wrong.get(), preset + ": calls from other threads changed results");
        }
    }

    // ------------------------------------------------------------------ //
    // Each expression, overridden, changes the result as expected
    // ------------------------------------------------------------------ //

    private static CombatFormulas with(String key, String expression) {
        List<String> warnings = new ArrayList<>();
        CombatFormulas formulas = CombatFormulas.build(CombatPreset.DEFAULT, Map.of(key, expression), warnings::add);
        assertTrue(warnings.isEmpty(), warnings.toString());
        return formulas;
    }

    @Test
    void override_damageScaling() {
        // Pretend a critical hit is twice as strong again
        CombatFormulas formulas = with("damage-scaling", "damage * critical * critical");
        Modifiers      m        = new Modifiers(1, 3, 1, true, 1, 1, 0, 1, 1, 0, false);
        Result         r        = new DamageCalculator(m, formulas).calculate(10, 1, 1, null, 0);
        assertEquals(90D, r.getDamage(), DELTA); // 10 * 3 * 3, the default would give 30
        assertTrue(formulas.isOverridden(CombatExpression.DAMAGE_SCALING));
    }

    @Test
    void override_directDamage() {
        // A fifth of the scaled damage skips the defense, whatever the direct modifier says
        CombatFormulas formulas = with("direct-damage", "damage * 0.2");
        Result         r        = new DamageCalculator(neutral(false), formulas).calculate(100, 1, 1, null, 0);
        assertEquals(100D, r.getDamage(), DELTA); // 80 + 20 direct, no defense
        Result defended = hit(formulas, neutral(false));
        // 20 damage: 4 direct. 16 left, defense 30: 1 - max(6, 30 - 4 * 16 / 8 = 22) * 0.8 * 0.05 = 0.12
        assertEquals(16 * 0.12 + 4, defended.getDamage(), DELTA);
        assertEquals(16 * 0.88, defended.getDefended(), DELTA);
    }

    @Test
    void override_defenseValue() {
        // Halve every defense. The default gives 30
        CombatFormulas formulas = with("defense-value", "defense * 0.5");
        Result         r        = hit(formulas, neutral(true));
        // legacy-style check on the default preset: 1 - max(3, 15 - 4 * 20 / 8 = 5) * 0.8 * 0.05 = 0.8
        assertEquals(20 * (1 - 5 * 0.8 * 0.05), r.getDamage(), DELTA);
    }

    @Test
    void override_defenseMitigation() {
        // No defense formula at all: the defense value is a flat amount taken off
        CombatFormulas formulas = with("defense-mitigation", "max(0, damage - defense / 10)");
        Result         r        = hit(formulas, neutral(false));
        assertEquals(17D, r.getDamage(), DELTA);
        assertEquals(3D, r.getDefended(), DELTA);
    }

    @Test
    void override_numberIsAccepted() {
        // A plain number is a valid expression: no direct damage at all, written as 0
        CombatFormulas formulas = CombatFormulas.build(CombatPreset.DEFAULT, Map.of("direct-damage", 0), null);
        assertTrue(formulas.isOverridden(CombatExpression.DIRECT_DAMAGE));
        assertEquals(0D, formulas.eval(CombatExpression.DIRECT_DAMAGE, 10D, 0.5D), DELTA);
    }

    @Test
    void override_appliesToTheLegacyPresetToo() {
        List<String> warnings = new ArrayList<>();
        CombatFormulas formulas = CombatFormulas.build(CombatPreset.LEGACY,
                Map.of("defense-value", "defense * 0.5"), warnings::add);
        // legacy: 1 - 15 * 0.8 * 0.01 = 0.88
        assertEquals(20 * 0.88, hit(formulas, neutral(true)).getDamage(), DELTA);
    }

    // ------------------------------------------------------------------ //
    // A broken formula is reported once and the preset's takes over
    // ------------------------------------------------------------------ //

    private static final String DEFAULT_DEFENSE_VALUE = CombatPreset.DEFAULT.expressionFor(CombatExpression.DEFENSE_VALUE);

    private void assertFallsBack(String key, Object broken, String reasonPart) {
        List<String> warnings = new ArrayList<>();
        CombatFormulas formulas = CombatFormulas.build(CombatPreset.DEFAULT, Map.of(key, broken), warnings::add);

        assertEquals(1, warnings.size(), warnings.toString());
        assertTrue(warnings.get(0).contains("'" + key + "'"), "names the key: " + warnings.get(0));
        assertTrue(warnings.get(0).contains(reasonPart), "gives the reason '" + reasonPart + "': " + warnings.get(0));
        CombatExpression expression = CombatExpression.byKey(key);
        assertFalse(formulas.isOverridden(expression));
        assertEquals(CombatFormulas.of(CombatPreset.DEFAULT).eval(expression, expression.sample()),
                formulas.eval(expression, expression.sample()), 0D);
    }

    @Test
    void brokenFormula_syntaxError() {
        assertFallsBack("defense-value", "max(defense, ", "mismatched");
    }

    @Test
    void brokenFormula_unknownVariable() {
        assertFallsBack("defense-value", "defense * armour", "armour");
    }

    @Test
    void brokenFormula_unknownFunction() {
        assertFallsBack("defense-value", "cbrt(defense)", "cbrt");
    }

    @Test
    void brokenFormula_negativeExponentWithoutBrackets_isRejected() {
        // Codex 1.3.0-dev.2 computes 'x * 2^-1' wrongly without complaining (-900 for x = 30, not 15), so any
        // '^' followed by a minus is refused at load, with or without spaces
        assertFallsBack("defense-value", "2^-1", "bracketed");
        assertFallsBack("defense-value", "defense * 2^-1", "bracketed");
        assertFallsBack("defense-value", "defense * 2 ^ -1", "bracketed");
        assertFallsBack("defense-value", "defense * 2 ^\t- 1", "bracketed");
        assertFallsBack("defense-value", "defense * (2 ^ -defense)", "bracketed");
    }

    @Test
    void negativeExponentWithBrackets_isAccepted() {
        List<String> warnings = new ArrayList<>();
        CombatFormulas ok = CombatFormulas.build(CombatPreset.DEFAULT, Map.of("defense-value", "defense * 2^(-1)"),
                warnings::add);
        assertTrue(warnings.isEmpty(), warnings.toString());
        assertTrue(ok.isOverridden(CombatExpression.DEFENSE_VALUE));
        assertEquals(15D, ok.eval(CombatExpression.DEFENSE_VALUE, 30D, 1D, 1D), DELTA);

        // A minus that is not an exponent's sign is fine
        CombatFormulas minus = CombatFormulas.build(CombatPreset.DEFAULT,
                Map.of("defense-value", "max(0, defense - 2^2)"), warnings::add);
        assertTrue(warnings.isEmpty(), warnings.toString());
        assertEquals(26D, minus.eval(CombatExpression.DEFENSE_VALUE, 30D, 1D, 1D), DELTA);
    }

    @Test
    void brokenFormula_divisionByZero() {
        assertFallsBack("defense-mitigation", "damage / (damage - damage)", "infinity");
    }

    @Test
    void brokenFormula_notANumber() {
        assertFallsBack("damage-scaling", "0 / 0", "NaN");
    }

    @Test
    void brokenFormula_empty() {
        assertFallsBack("direct-damage", "  ", "empty");
    }

    @Test
    void brokenFormula_wrongType() {
        assertFallsBack("direct-damage", Collections.singletonList("damage"), "text or a number");
    }

    @Test
    void brokenFormula_fallsBackToTheChosenPresetNotToTheDefault() {
        List<String> warnings = new ArrayList<>();
        CombatFormulas formulas = CombatFormulas.build(CombatPreset.LEGACY,
                Map.of("defense-mitigation", "damage * nonsense"), warnings::add);

        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("legacy"), warnings.get(0));
        // 1 - 30 * 0.8 * 0.01 = 0.76, the legacy result, not the default's 0.2
        assertEquals(15.2D, hit(formulas, neutral(true)).getDamage(), DELTA);
    }

    @Test
    void oneBrokenFormulaDoesNotAffectTheOthers() {
        CombatFormulas formulas = CombatFormulas.build(CombatPreset.DEFAULT,
                Map.of("defense-value", "nonsense +", "direct-damage", "damage * 0.2"), null);
        assertFalse(formulas.isOverridden(CombatExpression.DEFENSE_VALUE));
        assertTrue(formulas.isOverridden(CombatExpression.DIRECT_DAMAGE));
    }

    @Test
    void formulaThatBreaksDuringAHit_usesThePresetsForThatHitAndReportsOnce() {
        List<String> warnings = new ArrayList<>();
        // Fine for the sample (defense 30), a division by zero when the defense is exactly 31
        CombatFormulas formulas = CombatFormulas.build(CombatPreset.DEFAULT,
                Map.of("defense-value", "defense * 10 / (defense - 31)"), warnings::add);
        assertTrue(warnings.isEmpty());
        assertTrue(formulas.isOverridden(CombatExpression.DEFENSE_VALUE));

        double expected = CombatFormulas.of(CombatPreset.DEFAULT).eval(CombatExpression.DEFENSE_VALUE, 31D, 1D, 1D);
        assertEquals(expected, formulas.eval(CombatExpression.DEFENSE_VALUE, 31D, 1D, 1D), 0D);
        assertEquals(expected, formulas.eval(CombatExpression.DEFENSE_VALUE, 31D, 1D, 1D), 0D);

        assertEquals(1, warnings.size(), warnings.toString());
        assertTrue(warnings.get(0).contains("'defense-value'"));

        // And through the calculator: it returns a number rather than throwing
        Result r = new DamageCalculator(neutral(false), formulas).calculate(20, 1, 1, 31D, 0.8);
        assertTrue(Double.isFinite(r.getDamage()));
    }

    @Test
    void presetExpressions_keepTheirOwnNonFiniteResultsAsTheyAlwaysDid() {
        // No override, so nothing to fall back from: the result is what the Java formula gave (and no message)
        List<String> warnings = new ArrayList<>();
        CombatFormulas formulas = CombatFormulas.build(CombatPreset.DEFAULT, null, warnings::add);
        Modifiers m = new Modifiers(1, 1, 1, true, 1, 1, 0, 1, 1, 0, false);
        Result expected = new ReferenceDamageCalculator(m).calculate(Double.POSITIVE_INFINITY, 1, 1, 10D, 1);
        Result actual   = new DamageCalculator(m, formulas).calculate(Double.POSITIVE_INFINITY, 1, 1, 10D, 1);
        assertEquals(expected.getDamage(), actual.getDamage(), 0D);
        assertEquals(Double.doubleToLongBits(expected.getDefended()), Double.doubleToLongBits(actual.getDefended()));
        assertTrue(warnings.isEmpty());
    }
}
