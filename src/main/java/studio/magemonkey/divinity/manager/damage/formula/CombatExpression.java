package studio.magemonkey.divinity.manager.damage.formula;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Every formula of the damage calculator that can be changed in {@code combat.yml}, with the variables it is
 * given and the default expression of each preset. {@code combat.yml} documents the same variables; keep the two
 * in step ({@code CombatFormulasTest} checks the defaults and the variables against the shipped file).
 * <p>
 * The order of the variables is part of the contract with the compiled expression: the calculator passes values
 * by position.
 */
public enum CombatExpression {

    /** The damage of one type after every multiplier. */
    DAMAGE_SCALING("damage-scaling",
            new String[]{"damage", "pve_damage", "critical", "entity_type", "faction", "enchant_protection",
                    "attack_power", "block"},
            new double[]{10D, 1.2D, 1.5D, 0.9D, 1.1D, 0.8D, 1D, 0.7D},
            "damage * pve_damage * critical * entity_type * faction * enchant_protection * attack_power * block",
            null),

    /** The part of the scaled damage that skips the defense. */
    DIRECT_DAMAGE("direct-damage",
            new String[]{"damage", "direct"},
            new double[]{10D, 0.2D},
            "damage * direct",
            null),

    /** The victim's defense value, before the mitigation formula uses it. */
    DEFENSE_VALUE("defense-value",
            new String[]{"defense", "pve_defense", "penetration"},
            new double[]{30D, 1.1D, 0.9D},
            "max(0, defense * pve_defense * penetration)",
            null),

    /** The damage left after the defense. */
    DEFENSE_MITIGATION("defense-mitigation",
            new String[]{"damage", "defense", "protection", "toughness"},
            new double[]{20D, 30D, 0.8D, 4D},
            "max(0, damage * (1 - max(defense / 5, defense - 4 * damage / max(1, toughness + 8)) * protection * 0.05))",
            "max(0, damage * (1 - (defense * protection * 0.01)))");

    private final String   key;
    private final String[] variables;
    private final double[] sample;
    private final String   defaultExpression;
    private final String   legacyDefault;

    CombatExpression(@NotNull String key,
                     @NotNull String[] variables,
                     @NotNull double[] sample,
                     @NotNull String defaultExpression,
                     @Nullable String legacyDefault) {
        this.key = key;
        this.variables = variables;
        this.sample = sample;
        this.defaultExpression = defaultExpression;
        this.legacyDefault = legacyDefault;
    }

    /** The key under {@code expressions:} in {@code combat.yml}. */
    @NotNull
    public String key() {
        return key;
    }

    /** The variable names, in the order the values are passed. */
    @NotNull
    public String[] variables() {
        return variables.clone();
    }

    /**
     * Ordinary values for every variable, used to check a formula at load time. A formula that is NaN or
     * infinite for these values is rejected.
     */
    @NotNull
    double[] sample() {
        return sample.clone();
    }

    @NotNull
    public String defaultExpression() {
        return defaultExpression;
    }

    /** The legacy preset's expression, or null when it is the same as the default. */
    @Nullable
    public String legacyDefault() {
        return legacyDefault;
    }

    @Nullable
    public static CombatExpression byKey(@NotNull String key) {
        for (CombatExpression expression : values()) {
            if (expression.key.equals(key)) return expression;
        }
        return null;
    }
}
