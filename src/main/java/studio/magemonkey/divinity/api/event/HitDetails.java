package studio.magemonkey.divinity.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.api.damage.DamageOrigin;
import studio.magemonkey.codex.api.damage.WeaponStatsMode;
import studio.magemonkey.divinity.manager.damage.DamageMeta;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What both {@link DivinityHitEvent} events report about a hit, worked out once from the damage meta.
 * {@link DivinityHitEvent.Prepare} and {@link DivinityHitEvent.Result} have different superclasses, so they
 * share this instead of a base class.
 */
final class HitDetails {
    final LivingEntity        victim;
    final LivingEntity        attacker;
    final Projectile          projectile;
    final EntityDamageEvent   original;
    final DamageMeta          meta;
    final DamageOrigin        origin;
    final WeaponStatsMode     weaponStats;
    final Map<String, Double> requestedAmounts;

    HitDetails(@NotNull LivingEntity victim,
               @Nullable LivingEntity attacker,
               @Nullable Projectile projectile,
               @NotNull EntityDamageEvent original,
               @NotNull DamageMeta meta) {
        this.victim = victim;
        this.attacker = attacker;
        this.projectile = projectile;
        this.original = original;
        this.meta = meta;
        // A damage meta that did not come from Divinity's own listener (a plugin firing a deprecated event by
        // hand) has neither, so they are worked out here the same way
        this.origin = meta.getOrigin() != null
                ? meta.getOrigin()
                : DivinityHitEvent.inferOrigin(projectile, attacker, false);
        this.weaponStats = meta.getWeaponStatsMode() != null ? meta.getWeaponStatsMode() : WeaponStatsMode.ADD;
        this.requestedAmounts = Collections.unmodifiableMap(new LinkedHashMap<>(meta.getRequestedAmounts()));
    }
}
