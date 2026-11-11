package studio.magemonkey.divinity.manager.damage;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.jetbrains.annotations.NotNull;

/**
 * The one place that reads and writes the vanilla {@link DamageModifier}s of an {@link EntityDamageEvent}.
 *
 * <p>This class only wraps the access with named methods. It makes no decisions: when to call which
 * method, with which value, and under which condition is up to the caller. The order of calls matters
 * and is kept exactly as it was before the bridge existed.</p>
 *
 * <p>The {@link DamageModifier} API is deprecated by Bukkit. Keeping every use in this class is what
 * makes it replaceable later.</p>
 */
@SuppressWarnings("deprecation")
public final class VanillaDamageBridge {

    private final EntityDamageEvent event;

    public VanillaDamageBridge(@NotNull EntityDamageEvent event) {
        this.event = event;
    }

    /**
     * Sets the vanilla armor modifier to zero, if the event has one.
     */
    public void disableVanillaArmor() {
        if (event.isApplicable(DamageModifier.ARMOR)) {
            event.setDamage(DamageModifier.ARMOR, 0D);
        }
    }

    /**
     * Reads the vanilla blocking modifier. It is negative when vanilla let a shield block the hit.
     */
    public double getBlockingDamage() {
        return event.getDamage(DamageModifier.BLOCKING);
    }

    /**
     * Overwrites the vanilla blocking modifier with the given value, if the event has one.
     */
    public void overrideBlocking(double value) {
        if (event.isApplicable(DamageModifier.BLOCKING)) {
            event.setDamage(DamageModifier.BLOCKING, value);
        }
    }

    /**
     * Reads the invulnerability reduction modifier. Returns 0 where the modifier does not exist (it is
     * missing on some versions) or is not applicable to this event.
     */
    public double getInvulnerabilityReduction() {
        double reduction = 0;
        try {
            reduction = event.getDamage(EntityDamageEvent.DamageModifier.valueOf("INVULNERABILITY_REDUCTION"));
        } catch (Exception ignored) {
        }
        return reduction;
    }

    /**
     * Writes the BASE modifier.
     */
    public void setBaseDamage(double damage) {
        event.setDamage(DamageModifier.BASE, damage);
    }

    /**
     * Makes the vanilla event's final damage follow Divinity's value in BASE, after the calculation.
     *
     * <p>Does nothing when the final damage already equals BASE. Otherwise every applicable modifier is
     * rewritten: BASE keeps its value, ABSORPTION is set to what the victim's absorption hearts can
     * take, INVULNERABILITY_REDUCTION and RESISTANCE are left alone, and all others are zeroed.</p>
     *
     * @param victim the victim, whose absorption amount is read once the final damage is known to differ.
     */
    public void settleFinalDamage(@NotNull LivingEntity victim) {
        if (event.getFinalDamage() != event.getDamage()) {
            double damageBeforeAbsorption = event.getDamage();
            if (event.isApplicable(DamageModifier.RESISTANCE)) {
                damageBeforeAbsorption += event.getDamage(DamageModifier.RESISTANCE);
            }
            double absorption = Math.min(Math.max(0D, damageBeforeAbsorption), victim.getAbsorptionAmount());
            for (DamageModifier dmgModifier : DamageModifier.values()) {
                if (event.isApplicable(dmgModifier)) {
                    if (dmgModifier == DamageModifier.BASE) {
                        event.setDamage(dmgModifier, event.getDamage());
                    } else if (dmgModifier == DamageModifier.ABSORPTION) {
                        // Bukkit represents absorption as a negative damage modifier.
                        // Keep it in the final damage calculation instead of subtracting
                        // it from BASE (which caused the absorption modifier to be applied
                        // twice or not at all depending on the damage source).
                        event.setDamage(dmgModifier, -absorption);
                    } else if (!dmgModifier.name().equals("INVULNERABILITY_REDUCTION") && !dmgModifier.name()
                            .equals("RESISTANCE"))
                        event.setDamage(dmgModifier, 0); // Fix
                }
            }
        }
    }
}
