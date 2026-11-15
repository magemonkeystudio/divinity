package studio.magemonkey.divinity.manager.damage.formula;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A named set of default expressions.
 */
public enum CombatPreset {
    /** The combat of current versions. */
    DEFAULT("default"),
    /** As {@link #DEFAULT}, with the old defense formula. What {@code legacy-combat: true} used to select. */
    LEGACY("legacy");

    private final String key;

    CombatPreset(@NotNull String key) {
        this.key = key;
    }

    /** The name used in {@code combat.yml}. */
    @NotNull
    public String key() {
        return key;
    }

    /** The preset's expression for {@code expression}. */
    @NotNull
    public String expressionFor(@NotNull CombatExpression expression) {
        return this == LEGACY && expression.legacyDefault() != null
                ? expression.legacyDefault()
                : expression.defaultExpression();
    }

    @Nullable
    public static CombatPreset byKey(@Nullable String key) {
        if (key == null) return null;
        for (CombatPreset preset : values()) {
            if (preset.key.equalsIgnoreCase(key.trim())) return preset;
        }
        return null;
    }
}
