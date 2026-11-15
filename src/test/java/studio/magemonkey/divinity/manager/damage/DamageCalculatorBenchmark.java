package studio.magemonkey.divinity.manager.damage;

import studio.magemonkey.divinity.manager.damage.DamageCalculator.Modifiers;
import studio.magemonkey.divinity.manager.damage.formula.CombatFormulas;
import studio.magemonkey.divinity.manager.damage.formula.CombatPreset;

import java.util.Arrays;
import java.util.Random;

/**
 * Scratch benchmark, not a test (no test runner picks it up). Run it from the test classpath:
 * <pre>java -cp target/test-classes:target/classes:$(cat classpath.txt) \
 *     studio.magemonkey.divinity.manager.damage.DamageCalculatorBenchmark</pre>
 * One "hit" is what {@code DamageManager} does for a hit with three damage types, one of them undefended: build a
 * calculator, then calculate each type. The same pre-generated inputs go through the Java formulas that were
 * replaced ({@link ReferenceDamageCalculator}) and through the one that reads {@code combat.yml}.
 */
public final class DamageCalculatorBenchmark {

    private static final int HITS = 4096;

    private static final Modifiers[] MODS     = new Modifiers[HITS];
    private static final double[][]  DAMAGE   = new double[HITS][3];
    private static final double[]    DEFENSE  = new double[HITS];
    private static final double[]    PROTECT  = new double[HITS];

    static {
        Random random = new Random(371);
        for (int i = 0; i < HITS; i++) {
            MODS[i] = new Modifiers(0.8 + random.nextDouble() * 0.5, random.nextBoolean() ? 1 : 1.5, 0.9, true,
                    0.5 + random.nextDouble() / 2, 1, 0, 1, 0.9, random.nextInt(10), false);
            for (int t = 0; t < 3; t++) DAMAGE[i][t] = 5 + random.nextDouble() * 60;
            DEFENSE[i] = random.nextDouble() * 80;
            PROTECT[i] = 0.5 + random.nextDouble();
        }
    }

    private static double sink;

    private static double reference(int i) {
        ReferenceDamageCalculator calc = new ReferenceDamageCalculator(MODS[i]);
        double sum = 0;
        sum += calc.calculate(DAMAGE[i][0], 1, 1, DEFENSE[i], PROTECT[i]).getDamage();
        sum += calc.calculate(DAMAGE[i][1], 1, 1, DEFENSE[i] / 2, PROTECT[i]).getDamage();
        sum += calc.calculate(DAMAGE[i][2], 1, 1, null, 0).getDamage();
        return sum;
    }

    private static double configured(int i, CombatFormulas formulas) {
        DamageCalculator calc = new DamageCalculator(MODS[i], formulas);
        double sum = 0;
        sum += calc.calculate(DAMAGE[i][0], 1, 1, DEFENSE[i], PROTECT[i]).getDamage();
        sum += calc.calculate(DAMAGE[i][1], 1, 1, DEFENSE[i] / 2, PROTECT[i]).getDamage();
        sum += calc.calculate(DAMAGE[i][2], 1, 1, null, 0).getDamage();
        return sum;
    }

    private interface Run {
        double hit(int i);
    }

    /** Hits per second, as the median of several timed rounds after a warm-up. */
    private static double measure(Run run, int hitsPerRound) {
        for (int i = 0; i < hitsPerRound; i++) sink += run.hit(i % HITS);
        double[] rates = new double[7];
        for (int round = 0; round < rates.length; round++) {
            long start = System.nanoTime();
            for (int i = 0; i < hitsPerRound; i++) sink += run.hit(i % HITS);
            rates[round] = hitsPerRound / ((System.nanoTime() - start) / 1e9);
        }
        Arrays.sort(rates);
        return rates[rates.length / 2];
    }

    public static void main(String[] args) {
        CombatFormulas formulas = CombatFormulas.of(CombatPreset.DEFAULT);

        // Same inputs, same answers, before timing anything
        for (int i = 0; i < HITS; i++) {
            if (Double.doubleToLongBits(reference(i)) != Double.doubleToLongBits(configured(i, formulas))) {
                throw new IllegalStateException("results differ at hit " + i);
            }
        }

        int    rounds = args.length > 0 ? Integer.parseInt(args[0]) : 200_000;
        double before = measure(DamageCalculatorBenchmark::reference, rounds);
        double after  = measure(i -> configured(i, formulas), Math.max(1, rounds / 20));

        System.out.printf("before (Java formulas):        %,14.0f hits/s%n", before);
        System.out.printf("after  (combat.yml formulas):  %,14.0f hits/s%n", after);
        System.out.printf("after is %.1fx %s%n", before > after ? before / after : after / before,
                before > after ? "slower" : "faster");
        if (sink == 42) System.out.println();
    }
}
