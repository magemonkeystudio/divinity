package studio.magemonkey.divinity.manager.damage.formula;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * The compiled expressions of one preset plus the overrides from {@code combat.yml}. Immutable once built, and
 * safe to use from any thread.
 * <p>
 * An expression that cannot be used is replaced by the preset's own when this is built, and a result that is NaN
 * or infinite at hit time is replaced by the preset's result for that hit. {@link #eval} never throws.
 */
public final class CombatFormulas {

    private static final Consumer<String> SILENT = message -> {
    };

    private final CombatPreset     preset;
    private final Formula[]        active;
    private final Formula[]        fallback;
    private final boolean[]        overridden;
    private final AtomicBoolean[]  warned;
    private final Consumer<String> warn;

    private CombatFormulas(@NotNull CombatPreset preset,
                           @NotNull Formula[] active,
                           @NotNull Formula[] fallback,
                           @NotNull boolean[] overridden,
                           @NotNull Consumer<String> warn) {
        this.preset = preset;
        this.active = active;
        this.fallback = fallback;
        this.overridden = overridden;
        this.warn = warn;
        this.warned = new AtomicBoolean[active.length];
        for (int i = 0; i < warned.length; i++) {
            warned[i] = new AtomicBoolean();
        }
    }

    /** The preset as shipped, with no overrides. */
    @NotNull
    public static CombatFormulas of(@NotNull CombatPreset preset) {
        return build(preset, null, null);
    }

    /**
     * Compiles the preset's expressions, then the overrides on top of them.
     *
     * @param overrides expression key to expression (a string, or a number), or null for none. A value that is
     *                  not usable is reported to {@code warn} once, here, and that expression keeps the
     *                  preset's.
     * @param warn      receives one message per problem found now, and one per expression that later gives NaN
     *                  or infinity during a hit. Null to stay silent.
     */
    @NotNull
    public static CombatFormulas build(@NotNull CombatPreset preset,
                                       @Nullable Map<String, ?> overrides,
                                       @Nullable Consumer<String> warn) {
        Consumer<String> sink = warn == null ? SILENT : warn;
        CombatExpression[] all = CombatExpression.values();
        Formula[] active     = new Formula[all.length];
        Formula[] fallback   = new Formula[all.length];
        boolean[] overridden = new boolean[all.length];

        for (CombatExpression expression : all) {
            int    i          = expression.ordinal();
            String presetText = preset.expressionFor(expression);
            try {
                fallback[i] = compile(expression, presetText);
            } catch (IllegalArgumentException e) {
                // The shipped expressions are checked by a test, so this is a programming error
                throw new IllegalStateException("The " + preset.key() + " preset's " + expression.key()
                        + " does not compile: " + e.getMessage(), e);
            }
            active[i] = fallback[i];

            Object value = overrides == null ? null : overrides.get(expression.key());
            if (value == null) continue;

            String text = value instanceof Number || value instanceof String ? String.valueOf(value).trim() : null;
            try {
                if (text == null) throw new IllegalArgumentException("it must be text or a number");
                active[i] = compile(expression, text);
                overridden[i] = true;
            } catch (IllegalArgumentException e) {
                sink.accept("combat.yml: '" + expression.key() + "' is ignored (" + e.getMessage()
                        + "). Using the " + preset.key() + " preset's: " + presetText);
            }
        }
        return new CombatFormulas(preset, active, fallback, overridden, sink);
    }

    /**
     * Compiles {@code text} for {@code expression} and checks it against the expression's sample values.
     *
     * @throws IllegalArgumentException with the reason when it cannot be used
     */
    @NotNull
    private static Formula compile(@NotNull CombatExpression expression, @NotNull String text) {
        Formula formula = Formulas.compile(text, expression.variables());
        double  result;
        try {
            result = formula.eval(expression.sample());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("it fails to evaluate: " + e.getMessage(), e);
        }
        if (Double.isNaN(result)) {
            throw new IllegalArgumentException("it gives NaN for ordinary values");
        }
        if (Double.isInfinite(result)) {
            throw new IllegalArgumentException("it gives infinity for ordinary values");
        }
        return formula;
    }

    @NotNull
    public CombatPreset preset() {
        return preset;
    }

    /** Whether {@code expression} is the config's own and not the preset's. */
    public boolean isOverridden(@NotNull CombatExpression expression) {
        return overridden[expression.ordinal()];
    }

    /**
     * Evaluates one expression. Never throws. If the expression gives NaN or infinity, the preset's expression
     * answers instead.
     *
     * @param values the variables, in the order {@link CombatExpression#variables()} lists them
     */
    public double eval(@NotNull CombatExpression expression, double... values) {
        int    i = expression.ordinal();
        double result;
        try {
            result = active[i].eval(values);
        } catch (RuntimeException e) {
            result = Double.NaN;
        }
        if (Double.isFinite(result) || !overridden[i]) {
            // A preset expression is the reference: its own NaN or infinity is what the hit gets, as it always did
            return result;
        }

        if (warned[i].compareAndSet(false, true)) {
            warn.accept("combat.yml: '" + expression.key() + "' gave " + result + " during a hit. The "
                    + preset.key() + " preset's expression is used for such hits. (Reported once.)");
        }
        return fallback[i].eval(values);
    }
}
