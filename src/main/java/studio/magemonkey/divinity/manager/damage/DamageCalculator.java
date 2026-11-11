package studio.magemonkey.divinity.manager.damage;

import lombok.Value;
import org.jetbrains.annotations.Nullable;

/**
 * The damage formula, on plain numbers. It needs no Bukkit and no plugin instance.
 * <p>
 * {@link DamageManager} does the lookups (stats, attributes, hooks, enchantments) and passes the results in.
 * The order of the floating-point operations is deliberately the same as it was when the formula lived in
 * {@code DamageManager#onDamageRPGStart}; do not reorder them.
 */
public final class DamageCalculator {

    private final Modifiers modifiers;

    public DamageCalculator(Modifiers modifiers) {
        this.modifiers = modifiers;
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
        double dmgType = damage;

        dmgType *= modifiers.pveDamage;
        dmgType *= modifiers.critical;
        dmgType *= entityTypeModifier;
        dmgType *= factionModifier;
        dmgType *= modifiers.enchantProtection;
        if (modifiers.applyAttackPower) dmgType *= modifiers.attackPower;
        dmgType *= modifiers.block;
        double directType = dmgType * modifiers.direct; // Get direct value for this damage type
        dmgType = Math.max(0, dmgType - directType); // Deduct this value from damage

        boolean defenseApplied = false;
        double  defended       = 0;
        if (dmgType > 0 && defense != null) {
            double def = Math.max(0, defense * modifiers.pveDefense * modifiers.penetrate);

            double defCalced;
            if (modifiers.legacyCombat) {
                defCalced = Math.max(0, dmgType * (1 - (def * protectionFactor * 0.01)));
            } else {
                defCalced = Math.max(0,
                        dmgType * (1 - Math.max(def / 5, def - 4 * dmgType / Math.max(1, modifiers.toughness + 8))
                                * protectionFactor * 0.05));
            }
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
        /** The victim's armor toughness. Only the current formula uses it. */
        double  toughness;
        /** Selects the old defense formula ({@code EngineCfg.LEGACY_COMBAT}). */
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
