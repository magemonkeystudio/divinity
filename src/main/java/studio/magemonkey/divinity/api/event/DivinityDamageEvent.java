package studio.magemonkey.divinity.api.event;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.api.events.ICancellableEvent;
import studio.magemonkey.divinity.manager.damage.DamageMeta;
import studio.magemonkey.divinity.modules.list.arrows.ArrowManager.QArrow;
import studio.magemonkey.divinity.stats.EntityStats;
import studio.magemonkey.divinity.stats.items.attributes.DamageAttribute;
import studio.magemonkey.divinity.stats.items.attributes.DefenseAttribute;
import studio.magemonkey.divinity.stats.items.attributes.api.SimpleStat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

/**
 * The damage events Divinity fired before 2.0.
 *
 * @deprecated Use {@link DivinityHitEvent.Prepare} and {@link DivinityHitEvent.Result}. These events still fire at
 * the same points with the same behaviour, and will be removed in a later 2.x release (not before
 * {@link Dodge} has a replacement). See each subclass for the closest replacement.
 */
@Deprecated
public abstract class DivinityDamageEvent extends ICancellableEvent {

    protected LivingEntity victim;
    protected LivingEntity damager;
    protected Projectile   projectile;

    protected Map<DamageAttribute, Double>  damageMap;
    protected Map<DefenseAttribute, Double> defenseMap;
    protected Map<SimpleStat.Type, Double>  damagerItemStatsMap;

    protected DamageMeta        meta;
    protected EntityDamageEvent eventOrig;

    protected boolean cancelled = false;

    @Getter
    @Setter
    protected boolean exempt = false;

    public DivinityDamageEvent(
            @NotNull LivingEntity zertva,
            @NotNull EntityDamageEvent eventOrig,
            @NotNull DamageMeta meta
    ) {
        this(zertva, null, eventOrig, meta);
    }

    public DivinityDamageEvent(
            @NotNull LivingEntity zertva,
            @Nullable LivingEntity damager,
            @NotNull EntityDamageEvent eventOrig,
            @NotNull DamageMeta meta
    ) {
        this(zertva, damager, null, eventOrig, meta);
    }

    public DivinityDamageEvent(
            @NotNull LivingEntity zertva,
            @Nullable LivingEntity damager,
            @Nullable Projectile projectile,
            @NotNull EntityDamageEvent eventOrig,
            @NotNull DamageMeta meta
    ) {
        this.victim = zertva;
        this.damager = damager;
        this.projectile = projectile;

        this.damageMap = new HashMap<>();
        this.defenseMap = new HashMap<>();
        this.damagerItemStatsMap = new HashMap<>();

        this.meta = meta;
        this.eventOrig = eventOrig;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        this.eventOrig.setCancelled(this.isCancelled());
    }

    @NotNull
    public final DamageCause getCause() {
        return this.getOriginalEvent().getCause();
    }

    @NotNull
    public EntityDamageEvent getOriginalEvent() {
        return this.eventOrig;
    }

    @NotNull
    public LivingEntity getVictim() {
        return this.victim;
    }

    @Nullable
    public LivingEntity getDamager() {
        return this.damager;
    }

    @NotNull
    public DamageMeta getDamageMeta() {
        return this.meta;
    }

    @NotNull
    public Map<DamageAttribute, Double> getDamageMap() {
        return damageMap;
    }

    public void computeDamage(@NotNull DoubleUnaryOperator operator) {
        this.computeDamage(this.damageMap.keySet(), operator);
    }

    public void computeDamage(@NotNull DamageAttribute damage, @NotNull DoubleUnaryOperator operator) {
        this.computeDamage(Sets.newHashSet(damage), operator);
    }

    public void computeDamage(@NotNull Collection<DamageAttribute> damages, @NotNull DoubleUnaryOperator operator) {
        damages.forEach(dmgApp -> this.damageMap.compute(dmgApp,
                (dmgApp2, dmg) -> operator.applyAsDouble(this.damageMap.computeIfAbsent(dmgApp, dmg2 -> 0D))));
        this.damageMap.values().removeIf(dmg -> dmg == 0D);
    }

    @NotNull
    public Map<DefenseAttribute, Double> getDefenseMap() {
        return defenseMap;
    }

    public void computeDefense(@NotNull DoubleUnaryOperator operator) {
        this.computeDefense(this.defenseMap.keySet(), operator);
    }

    public void computeDefense(@NotNull DefenseAttribute defense, @NotNull DoubleUnaryOperator operator) {
        this.computeDefense(Sets.newHashSet(defense), operator);
    }

    public void computeDefense(@NotNull Collection<DefenseAttribute> defenses, @NotNull DoubleUnaryOperator operator) {
        defenses.forEach(defApp -> this.defenseMap.compute(defApp,
                (defKey, defVal) -> operator.applyAsDouble(this.defenseMap.computeIfAbsent(defApp, defVal2 -> 0D))));
        this.defenseMap.values().removeIf(dmg -> dmg == 0D);
    }

    @NotNull
    public Map<SimpleStat.Type, Double> getDamagerItemStatsMap() {
        return damagerItemStatsMap;
    }

    public double getDamagerItemStat(@NotNull SimpleStat.Type type) {
        return this.getDamagerItemStatsMap().getOrDefault(type, 0D);
    }

    @Nullable
    public ItemStack getWeapon() {
        return this.getDamageMeta().getWeapon();
    }

    @Nullable
    public Projectile getProjectile() {
        return this.projectile;
    }

    @Nullable
    public QArrow getArrow() {
        return this.getDamageMeta().getArrow();
    }

    public final boolean isProjectile() {
        return this.getProjectile() != null;
    }

    @NotNull
    public final EntityStats getVictimStats() {
        return EntityStats.get(this.getVictim());
    }

    @Nullable
    public final EntityStats getDamagerStats() {
        if (this.getDamager() == null) return null;

        return EntityStats.get(this.damager);
    }

    /**
     * Called after Codex scaling, before any calculation. Divinity's own calculation listens to this event at
     * {@code HIGHEST}.
     *
     * @deprecated Use {@link DivinityHitEvent.Prepare}, which fires just before this event with the same damage
     * and defense maps.
     */
    @Deprecated
    public static class Start extends DivinityDamageEvent {

        public Start(
                @NotNull LivingEntity zertva,
                @Nullable LivingEntity damager,
                @Nullable Projectile projectile,
                @NotNull Map<DamageAttribute, Double> damageMap,
                @NotNull Map<DefenseAttribute, Double> defenseMap,
                @NotNull Map<SimpleStat.Type, Double> statsMap,
                @NotNull EntityDamageEvent eventOrig,
                @NotNull DamageMeta meta,
                boolean exempt
        ) {
            super(zertva, damager, projectile, eventOrig, meta);
            this.damageMap = damageMap;
            this.defenseMap = defenseMap;
            this.damagerItemStatsMap = statsMap;
            this.exempt = exempt;
        }
    }

    /**
     * Called before Codex's attributes and buffs scale the damage and defense maps.
     *
     * @deprecated Use {@link DivinityHitEvent.Prepare}. It fires after the scaling; nothing replaces the point
     * before it.
     */
    @Deprecated
    public static class BeforeScale extends DivinityDamageEvent {

        public BeforeScale(
                @NotNull LivingEntity zertva,
                @Nullable LivingEntity damager,
                @Nullable Projectile projectile,
                @NotNull Map<DamageAttribute, Double> damageMap,
                @NotNull Map<DefenseAttribute, Double> defenseMap,
                @NotNull Map<SimpleStat.Type, Double> statsMap,
                @NotNull EntityDamageEvent eventOrig,
                @NotNull DamageMeta meta,
                boolean exempt
        ) {
            super(zertva, damager, projectile, eventOrig, meta);
            this.damageMap = damageMap;
            this.defenseMap = defenseMap;
            this.damagerItemStatsMap = statsMap;
            this.exempt = exempt;
        }
    }

    /**
     * Called after the dodge roll, critical hit, block and PvP or PvE modifiers, before the formula.
     *
     * @deprecated Use {@link DivinityHitEvent.Prepare} to change the inputs, or {@link DivinityHitEvent.Result}
     * to cancel the hit once critical hit and block are known.
     */
    @Deprecated
    public static class Pre extends DivinityDamageEvent {

        public Pre(
                @NotNull LivingEntity zertva,
                @Nullable LivingEntity damager,
                @Nullable Projectile projectile,
                @NotNull EntityDamageEvent eventOrig,
                @NotNull DamageMeta meta
        ) {
            super(zertva, damager, projectile, eventOrig, meta);
        }
    }

    /**
     * Called when the victim dodges. Cancelling it makes the hit land.
     *
     * @deprecated No replacement yet. It keeps firing and will not be removed before one exists. A dodged hit
     * fires {@link DivinityHitEvent.Prepare} but no {@link DivinityHitEvent.Result}.
     */
    @Deprecated
    public static class Dodge extends DivinityDamageEvent {

        public Dodge(
                @NotNull LivingEntity zertva,
                @Nullable LivingEntity damager,
                @Nullable Projectile projectile,
                @NotNull EntityDamageEvent eventOrig,
                @NotNull DamageMeta meta
        ) {
            super(zertva, damager, projectile, eventOrig, meta);
        }

        // Override to avoid cancel original event
        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }

    /**
     * Called at the end of the calculation, after the damage is written and the post-hit effects have run. Not
     * called for hits that ignore Divinity's weapon stats.
     *
     * @deprecated Use {@link DivinityHitEvent.Result} for the final numbers (it fires for every calculated hit,
     * those ignoring weapon stats included), or the Bukkit damage event at {@code MONITOR} for "after the hit
     * landed".
     */
    @Deprecated
    public static class Exit extends DivinityDamageEvent {

        public Exit(
                @NotNull LivingEntity zertva,
                @Nullable LivingEntity damager,
                @Nullable Projectile projectile,
                @NotNull EntityDamageEvent eventOrig,
                @NotNull DamageMeta meta
        ) {
            super(zertva, damager, projectile, eventOrig, meta);
        }
    }
}
