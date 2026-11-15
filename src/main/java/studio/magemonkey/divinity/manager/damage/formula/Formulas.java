package studio.magemonkey.divinity.manager.damage.formula;

import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.util.eval.javaluator.DoubleEvaluator;
import studio.magemonkey.codex.util.eval.javaluator.StaticVariableSet;

/**
 * The one place that turns an expression string into a {@link Formula}.
 * <p>
 * <b>TEMPORARY.</b> Codex {@code 1.3.0-SNAPSHOT} has no compile step: its evaluator tokenizes and parses the string
 * on every call, which is roughly 100 times slower than the hardcoded formulas. This class wraps that string
 * evaluation, with a variable set, so that everything around it (presets, loading, migration, failure handling)
 * can be written and tested now. It is to be replaced by {@code Evaluator.compile(expression, variableNames)} from
 * Codex once that is published; nothing else in the package may depend on how this class works.
 */
final class Formulas {

    /** Shared. The evaluator keeps no state between calls, so it is safe from any thread. */
    private static final DoubleEvaluator EVALUATOR = new DoubleEvaluator();

    /** What every variable is bound to while checking that an expression is well formed. */
    private static final double PROBE = 2D;

    private Formulas() {
    }

    /**
     * @throws IllegalArgumentException for a syntax error or a variable that is not in {@code variableNames}
     */
    @NotNull
    static Formula compile(@NotNull String expression, @NotNull String... variableNames) {
        if (expression.trim().isEmpty()) {
            throw new IllegalArgumentException("the expression is empty");
        }
        String[] names = variableNames.clone();

        // Codex's real compile throws for these at compile time. Evaluating once gives the same answer here.
        double[] probe = new double[names.length];
        java.util.Arrays.fill(probe, PROBE);
        try {
            evaluate(expression, names, probe);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(firstLine(e.getMessage()), e);
        }

        return values -> evaluate(expression, names, values);
    }

    private static double evaluate(String expression, String[] names, double[] values) {
        StaticVariableSet<Double> variables = new StaticVariableSet<>();
        for (int i = 0; i < names.length; i++) {
            variables.set(names[i], values[i]);
        }
        return EVALUATOR.evaluate(expression, variables);
    }

    private static String firstLine(String message) {
        if (message == null) return "invalid expression";
        int end = message.indexOf('\n');
        return end < 0 ? message : message.substring(0, end);
    }
}
