package studio.magemonkey.divinity.manager.damage;

import org.jetbrains.annotations.Nullable;
import studio.magemonkey.divinity.manager.damage.DamageCalculator.Modifiers;
import studio.magemonkey.divinity.manager.damage.DamageCalculator.Result;

/**
 * The damage formula exactly as {@link DamageCalculator} had it in Java before the formulas moved to
 * {@code combat.yml}: the arithmetic is copied unchanged from that version. It is the oracle for the equivalence
 * tests and the "before" of the benchmark. Do not edit the arithmetic; if the real formulas change on purpose,
 * this class stays and the tests that compare against it change with a stated reason.
 */
public final class ReferenceDamageCalculator {

    private final Modifiers modifiers;

    public ReferenceDamageCalculator(Modifiers modifiers) {
        this.modifiers = modifiers;
    }

    public Result calculate(double damage,
                            double entityTypeModifier,
                            double factionModifier,
                            @Nullable Double defense,
                            double protectionFactor) {
        double dmgType = damage;

        dmgType *= modifiers.getPveDamage();
        dmgType *= modifiers.getCritical();
        dmgType *= entityTypeModifier;
        dmgType *= factionModifier;
        dmgType *= modifiers.getEnchantProtection();
        if (modifiers.isApplyAttackPower()) dmgType *= modifiers.getAttackPower();
        dmgType *= modifiers.getBlock();
        double directType = dmgType * modifiers.getDirect(); // Get direct value for this damage type
        dmgType = Math.max(0, dmgType - directType); // Deduct this value from damage

        boolean defenseApplied = false;
        double  defended       = 0;
        if (dmgType > 0 && defense != null) {
            double def = Math.max(0, defense * modifiers.getPveDefense() * modifiers.getPenetrate());

            double defCalced;
            if (modifiers.isLegacyCombat()) {
                defCalced = Math.max(0, dmgType * (1 - (def * protectionFactor * 0.01)));
            } else {
                defCalced = Math.max(0,
                        dmgType * (1 - Math.max(def / 5, def - 4 * dmgType / Math.max(1, modifiers.getToughness() + 8))
                                * protectionFactor * 0.05));
            }
            defenseApplied = true;
            defended = dmgType - defCalced;
            dmgType = defCalced;
        }
        return new Result(dmgType + directType, defenseApplied, defended);
    }
}
