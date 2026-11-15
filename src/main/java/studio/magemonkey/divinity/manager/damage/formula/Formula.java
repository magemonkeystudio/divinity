package studio.magemonkey.divinity.manager.damage.formula;

/**
 * A compiled combat expression. Variables are bound by position, in the order the expression's
 * {@link CombatExpression} declares them. Implementations must be immutable and safe to call from any thread.
 */
@FunctionalInterface
public interface Formula {

    /**
     * @param values the variable values, in declaration order
     * @return the result. May be NaN or infinite; the caller checks.
     */
    double eval(double... values);
}
