package studio.magemonkey.divinity.manager.damage;

import lombok.Value;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.divinity.config.CombatCfg;
import studio.magemonkey.divinity.manager.damage.formula.CombatExpression;
import studio.magemonkey.divinity.manager.damage.formula.CombatFormulas;

/**
 * The damage calculation, on plain numbers. It needs no Bukkit and no plugin instance.
 * <p>
 * {@link DamageManager} does the lookups (stats, attributes, hooks, enchantments) and passes the results in.
 * The numbers come from the expressions in {@code combat.yml} ({@link CombatExpression}); what stays here is the
 * order of the stages and the bookkeeping between them. A calculator is not thread-safe: it reuses argument arrays. With the shipped default the results are bit for bit what
 * the formulas gave when they were written in Java, and the same holds for the legacy preset.
 */
public final class DamageCalculator {

    private final Modifiers       modifiers;
    private final CombatFormulas  formulas;

    // One argument array per expression, reused for every damage type, so that a hit allocates none per evaluation.
    // This is why a calculator is for one thread and one hit (DamageManager makes one per hit).
    private final double[] scalingArgs    = new double[CombatExpression.DAMAGE_SCALING.variables().length];
    private final double[] directArgs     = new double[CombatExpression.DIRECT_DAMAGE.variables().length];
    private final double[] defenseArgs    = new double[CombatExpression.DEFENSE_VALUE.variables().length];
    private final double[] mitigationArgs = new double[CombatExpression.DEFENSE_MITIGATION.variables().length];

    /** Uses the formulas of {@code combat.yml} (or the legacy preset's when {@link Modifiers#legacyCombat}). */
    public DamageCalculator(Modifiers modifiers) {
        this(modifiers, CombatCfg.formulas(modifiers.legacyCombat));
    }

    public DamageCalculator(Modifiers modifiers, CombatFormulas formulas) {
        this.modifiers = modifiers;
        this.formulas = formulas;
    }

    /**
     * Calculates one damage type of the hit.
     *
     * @param damage             the damage of this type before any modifier
     * @param entityTypeModifier the damage attribute's modifier for the victim's entity type (1 if none)
     * @param factionModifier    the damage attribute's modifier for the victim's MythicMobs faction (1 if none)
     * @param defense            the victim's value of the attached defense, or null if no defense applies
     * @param protectionFactor   the attached defense's protection factor (ignored when {@code defense} is null)
     */
    public Result calculate(double damage,
                            double entityTypeModifier,
                            double factionModifier,
                            @Nullable Double defense,
                            double protectionFactor) {
        // The arrays are filled in the order CombatExpression lists the variables
        double[] scaling = scalingArgs;
        scaling[0] = damage;
        scaling[1] = modifiers.pveDamage;
        scaling[2] = modifiers.critical;
        scaling[3] = entityTypeModifier;
        scaling[4] = factionModifier;
        scaling[5] = modifiers.enchantProtection;
        // Attack power is not applied to exempt events; 1 is the neutral value of the product
        scaling[6] = modifiers.applyAttackPower ? modifiers.attackPower : 1D;
        scaling[7] = modifiers.block;
        double dmgType = formulas.eval(CombatExpression.DAMAGE_SCALING, scaling);

        double[] direct = directArgs;
        direct[0] = dmgType;
        direct[1] = modifiers.direct;
        double directType = formulas.eval(CombatExpression.DIRECT_DAMAGE, direct);
        dmgType = Math.max(0, dmgType - directType); // Deduct this value from damage

        boolean defenseApplied = false;
        double  defended       = 0;
        if (dmgType > 0 && defense != null) {
            double[] defenseValue = defenseArgs;
            defenseValue[0] = defense;
            defenseValue[1] = modifiers.pveDefense;
            defenseValue[2] = modifiers.penetrate;
            double def = formulas.eval(CombatExpression.DEFENSE_VALUE, defenseValue);

            double[] mitigation = mitigationArgs;
            mitigation[0] = dmgType;
            mitigation[1] = def;
            mitigation[2] = protectionFactor;
            mitigation[3] = modifiers.toughness;
            double defCalced = formulas.eval(CombatExpression.DEFENSE_MITIGATION, mitigation);
            defenseApplied = true;
            defended = dmgType - defCalced;
            dmgType = defCalced;
        }
        return new Result(dmgType + directType, defenseApplied, defended);
    }

    /**
     * The modifiers that are the same for every damage type of one hit.
     */
    @Value
    public static class Modifiers {
        double  pveDamage;
        double  critical;
        double  enchantProtection;
        /** Whether {@link #attackPower} is applied at all. It is not when the event is exempt. */
        boolean applyAttackPower;
        double  attackPower;
        double  block;
        double  direct;
        double  pveDefense;
        double  penetrate;
        /** The victim's armor toughness. Only the default preset's defense formula uses it. */
        double  toughness;
        /**
         * Uses the legacy preset's formulas for this hit, whatever {@code combat.yml} selects
         * ({@code EngineCfg.LEGACY_COMBAT}, deprecated: false unless something sets it).
         */
        boolean legacyCombat;
    }

    /**
     * The result for one damage type.
     */
    @Value
    public static class Result {
        /** What is written to the damage meta: the damage after defense, plus the direct part. */
        double  damage;
        /** Whether a defense was applied. The damage meta must be told even when {@link #defended} is 0. */
        boolean defenseApplied;
        /** The amount the defense absorbed. 0 when no defense was applied. */
        double  defended;
    }
}
