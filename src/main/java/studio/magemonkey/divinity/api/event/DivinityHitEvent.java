package studio.magemonkey.divinity.api.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.api.damage.DamageOrigin;
import studio.magemonkey.codex.api.damage.DamageRequest;
import studio.magemonkey.codex.api.damage.FinalDamageEvent;
import studio.magemonkey.codex.api.damage.WeaponStatsMode;
import studio.magemonkey.divinity.manager.damage.DamageMeta;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.stats.items.attributes.DamageAttribute;
import studio.magemonkey.divinity.stats.items.attributes.DefenseAttribute;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A hit Divinity calculates, at the two points where other plugins can see or change it.
 * <p>
 * Listen to {@link Prepare} or {@link Result}. This is the interface both implement, so code can handle
 * either; it is not an event of its own and cannot be listened to.
 * <h2>Order</h2>
 * Both events are fired from Divinity's {@code EntityDamageEvent} handler, at priority {@code HIGHEST}, on the
 * server thread:
 * <ol>
 *     <li>{@link Prepare}: the damage and defense per type are known, Codex's attributes and buffs have been
 *     applied, and nothing has been calculated yet. It fires just before the deprecated
 *     {@code DivinityDamageEvent.Start}.</li>
 *     <li>The dodge roll (a dodged hit ends here), critical hit, block, PvP or PvE modifiers, and the formula
 *     for each damage type, defenses included. Each damage type's hit actions run here, as that type is
 *     calculated, so they have already run when {@link Result} fires.</li>
 *     <li>{@link Result}: Divinity's final numbers per type, before anything is written to the Bukkit event.
 *     What listeners leave here is what the target takes and what vampirism, bleeding, thornmail, area
 *     damage and the deprecated {@code DivinityDamageEvent.Exit} see.</li>
 * </ol>
 * Neither event fires for a hit Divinity leaves alone: a request with {@link WeaponStatsMode#RAW},
 * vanilla-only damage handling, a target that is not a living entity, or a hit another listener cancelled
 * before Divinity's handler ran.
 * <h2>Codex's {@link FinalDamageEvent}</h2>
 * {@link Result} is a {@link FinalDamageEvent}, so a plugin that does not depend on Divinity can listen to
 * that and see the same numbers. Divinity registers itself as a provider of it; see
 * {@link FinalDamageEvent#willFire(EntityDamageEvent)} and {@code DivinityFinalDamageProvider}. A dodged hit
 * fires no {@link Result} and so no {@link FinalDamageEvent}, although {@code willFire} said it would: the
 * dodge is rolled after the question can be asked, and the Bukkit event is cancelled.
 * <h2>Cancelling</h2>
 * Cancelling either event cancels the Bukkit event, and un-cancelling it un-cancels the Bukkit event, as the
 * deprecated events do. After a cancelled {@link Prepare} nothing more is calculated. After a cancelled
 * {@link Result} nothing is written to the Bukkit event, and vampirism, bleeding, thornmail, area damage and
 * the deprecated {@code DivinityDamageEvent.Exit} do not run. The hit actions of the damage types have already
 * run by then (see Order).
 */
public interface DivinityHitEvent extends Cancellable {

    @NotNull
    LivingEntity getVictim();

    /**
     * @return the living entity the hit is attributed to: the swinging entity, the projectile's shooter, or
     * the source of a hit dealt through Divinity. Null if there is none
     */
    @Nullable
    LivingEntity getAttacker();

    @Nullable
    Projectile getProjectile();

    /**
     * @return the weapon whose stats took part in the hit: the item in the attacker's main hand, or the item a
     * projectile was launched from. Null if there is none
     */
    @Nullable
    ItemStack getWeapon();

    @NotNull
    DamageCause getCause();

    /**
     * @return the Bukkit event of the hit. Do not change its damage directly: Divinity writes it after
     * {@link Result}
     */
    @NotNull
    EntityDamageEvent getOriginalEvent();

    /**
     * @return how the dealer described this hit through Codex, or null if it did not. For a
     * {@code DIVINITY_<type>} hit dealt through Codex's {@code DamageRegistry} while the dealer's own request
     * was open on the target, this is the dealer's request
     */
    @Nullable
    DamageRequest getRequest();

    /**
     * @return whether the hit was described by a request, so that {@link #getOrigin()} is the dealer's word
     * rather than Divinity's guess
     */
    default boolean isDescribed() {
        return getRequest() != null;
    }

    /**
     * What kind of thing dealt the hit. With a request, its origin. Without one, Divinity's best guess:
     * {@link DamageOrigin#WEAPON_PROJECTILE} for a projectile, {@link DamageOrigin#EFFECT} for vanilla Thorns,
     * {@link DamageOrigin#WEAPON_MELEE} when there is a living attacker, and {@link DamageOrigin#ENVIRONMENT}
     * otherwise. A plugin's {@code damage(amount, player)} without a request cannot be told from a swing, so it
     * is guessed as {@link DamageOrigin#WEAPON_MELEE}.
     */
    @NotNull
    DamageOrigin getOrigin();

    /**
     * How Divinity handled the attacker's equipment for this hit. This is what Divinity did, which can differ
     * from what the request asked for: {@link WeaponStatsMode#ADD} without a request,
     * {@link WeaponStatsMode#IGNORE} for Fabled's ignore-divinity setting, and {@link WeaponStatsMode#REPLACE}
     * (or {@link WeaponStatsMode#IGNORE} when the request asked for that) for a {@code DIVINITY_<type>} hit.
     * Never {@link WeaponStatsMode#RAW}, since no event is fired for such a hit.
     */
    @NotNull
    WeaponStatsMode getWeaponStatsMode();

    /**
     * @return the typed amounts the hit was described with, by damage type as the dealer wrote it
     * ({@code DIVINITY_fire}, {@code default}, ...). For a {@code DIVINITY_<type>} hit, that type and its
     * amount. Empty if there are none. Cannot be changed
     */
    @NotNull
    Map<String, Double> getRequestedAmounts();

    /**
     * Divinity's guess at the origin of a hit no request describes. See {@link #getOrigin()}.
     */
    @NotNull
    static DamageOrigin inferOrigin(@Nullable Projectile projectile,
                                    @Nullable LivingEntity attacker,
                                    boolean vanillaThorns) {
        if (vanillaThorns) return DamageOrigin.EFFECT;
        if (projectile != null) return DamageOrigin.WEAPON_PROJECTILE;
        if (attacker != null) return DamageOrigin.WEAPON_MELEE;
        return DamageOrigin.ENVIRONMENT;
    }

    @Nullable
    private static DamageAttribute damageType(@NotNull String id) {
        return ItemStats.getDamageById(id);
    }

    @Nullable
    private static DefenseAttribute defenseType(@NotNull String id) {
        return ItemStats.getDefenseById(id);
    }

    private static double amount(double value) {
        return Double.isNaN(value) ? 0D : Math.max(0D, value);
    }

    /**
     * The inputs of a hit, before Divinity calculates anything. The damage and defense per type can be
     * changed. A damage type set to 0 is removed.
     * <p>
     * Only Divinity fires this; it has no counterpart in Codex.
     */
    class Prepare extends Event implements DivinityHitEvent {
        private static final HandlerList HANDLERS = new HandlerList();

        private final HitDetails                    hit;
        private final Map<DamageAttribute, Double>  damages;
        private final Map<DefenseAttribute, Double> defenses;
        private       boolean                       cancelled;

        public Prepare(@NotNull LivingEntity victim,
                       @Nullable LivingEntity attacker,
                       @Nullable Projectile projectile,
                       @NotNull EntityDamageEvent original,
                       @NotNull DamageMeta meta,
                       @NotNull Map<DamageAttribute, Double> damages,
                       @NotNull Map<DefenseAttribute, Double> defenses) {
            this.hit = new HitDetails(victim, attacker, projectile, original, meta);
            this.damages = damages;
            this.defenses = defenses;
        }

        @NotNull
        @Override
        public LivingEntity getVictim() {
            return hit.victim;
        }

        @Nullable
        @Override
        public LivingEntity getAttacker() {
            return hit.attacker;
        }

        @Nullable
        @Override
        public Projectile getProjectile() {
            return hit.projectile;
        }

        @Nullable
        @Override
        public ItemStack getWeapon() {
            return hit.meta.getWeapon();
        }

        @NotNull
        @Override
        public DamageCause getCause() {
            return hit.original.getCause();
        }

        @NotNull
        @Override
        public EntityDamageEvent getOriginalEvent() {
            return hit.original;
        }

        @Nullable
        @Override
        public DamageRequest getRequest() {
            return hit.meta.getRequest();
        }

        @NotNull
        @Override
        public DamageOrigin getOrigin() {
            return hit.origin;
        }

        @NotNull
        @Override
        public WeaponStatsMode getWeaponStatsMode() {
            return hit.weaponStats;
        }

        @NotNull
        @Override
        public Map<String, Double> getRequestedAmounts() {
            return hit.requestedAmounts;
        }

        /**
         * @return the damage per type, which cannot be changed through this map. Use
         * {@link #setDamage(DamageAttribute, double)}
         */
        @NotNull
        public Map<DamageAttribute, Double> getDamages() {
            return Collections.unmodifiableMap(damages);
        }

        public double getDamage(@NotNull DamageAttribute type) {
            return damages.getOrDefault(type, 0D);
        }

        /**
         * @return the damage of the type with this id, 0 if the hit has none or there is no such type
         */
        public double getDamage(@NotNull String type) {
            DamageAttribute attribute = damageType(type);
            return attribute == null ? 0D : getDamage(attribute);
        }

        /**
         * Sets the damage of one type, before defenses. A value of 0 or less removes the type from the hit.
         */
        public void setDamage(@NotNull DamageAttribute type, double amount) {
            double value = amount(amount);
            if (value == 0D) damages.remove(type);
            else damages.put(type, value);
        }

        /**
         * @return false if there is no damage type with this id
         */
        public boolean setDamage(@NotNull String type, double amount) {
            DamageAttribute attribute = damageType(type);
            if (attribute == null) return false;
            setDamage(attribute, amount);
            return true;
        }

        /**
         * @return the victim's defense per type, which cannot be changed through this map. Use
         * {@link #setDefense(DefenseAttribute, double)}
         */
        @NotNull
        public Map<DefenseAttribute, Double> getDefenses() {
            return Collections.unmodifiableMap(defenses);
        }

        public double getDefense(@NotNull DefenseAttribute type) {
            return defenses.getOrDefault(type, 0D);
        }

        public double getDefense(@NotNull String type) {
            DefenseAttribute attribute = defenseType(type);
            return attribute == null ? 0D : getDefense(attribute);
        }

        /**
         * Sets the victim's value of one defense for this hit. Defense applies to the damage types it is
         * attached to. Unlike damage, a defense may be negative.
         */
        public void setDefense(@NotNull DefenseAttribute type, double value) {
            defenses.put(type, Double.isNaN(value) ? 0D : value);
        }

        /**
         * @return false if there is no defense type with this id
         */
        public boolean setDefense(@NotNull String type, double value) {
            DefenseAttribute attribute = defenseType(type);
            if (attribute == null) return false;
            setDefense(attribute, value);
            return true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
            hit.original.setCancelled(cancelled);
        }

        @NotNull
        @Override
        public HandlerList getHandlers() {
            return HANDLERS;
        }

        @NotNull
        public static HandlerList getHandlerList() {
            return HANDLERS;
        }
    }

    /**
     * Divinity's final damage per type, after defenses, before it is written to the Bukkit event. The damage
     * can be changed; whatever listeners leave is what the victim takes.
     * <p>
     * Divinity writes the sum of the types, less the vanilla invulnerability reduction and rounded to two
     * decimals, as the Bukkit event's base damage. If the hit then comes to nothing, it is cancelled.
     * <h2>One set of numbers for Codex and Divinity listeners</h2>
     * This is Codex's {@link FinalDamageEvent}, and it shares Codex's handler list: a listener of either class
     * receives it (a listener of {@code Result} only receives Divinity's). Both read and change the same
     * numbers, Divinity's per-type damage, in the order Divinity lists them:
     * <ul>
     *     <li>{@link #getDamages()} is keyed by damage type id ({@code physical}, {@code fire}, ...);
     *     {@link #getDamagesByAttribute()} is the same by {@link DamageAttribute}.</li>
     *     <li>{@link #setDamage(String, double)} and {@link #setDamage(DamageAttribute, double)} change the same
     *     entry. An id that is not one of Divinity's damage types is ignored: nothing is added and
     *     {@link #getDamage(String)} stays 0 for it, so the total stays the sum of the types. Use
     *     {@link #isDamageType(String)} to check first.</li>
     *     <li>Unlike Codex's base class, a negative amount counts as 0 and NaN as 0 rather than throwing, as
     *     this event always did. Infinity is kept as Divinity's formulas would keep it.</li>
     * </ul>
     * Cancelling also cancels the Bukkit event (see {@link #setCancelled(boolean)}).
     */
    class Result extends FinalDamageEvent implements DivinityHitEvent {

        private final HitDetails                   hit;
        private final Map<DamageAttribute, Double> incoming;
        private       boolean                      cancelled;

        public Result(@NotNull LivingEntity victim,
                      @Nullable LivingEntity attacker,
                      @Nullable Projectile projectile,
                      @NotNull EntityDamageEvent original,
                      @NotNull DamageMeta meta,
                      @NotNull Map<DamageAttribute, Double> incoming) {
            this(new HitDetails(victim, attacker, projectile, original, meta), incoming);
        }

        private Result(@NotNull HitDetails hit, @NotNull Map<DamageAttribute, Double> incoming) {
            // The damage lives on the damage meta, so Codex's own map is left empty (see FinalDamageEvent)
            super(hit.victim,
                    hit.attacker,
                    hit.projectile,
                    hit.original.getCause(),
                    hit.original,
                    hit.meta.getRequest(),
                    hit.origin,
                    hit.weaponStats,
                    Collections.emptyMap());
            this.hit = hit;
            this.incoming = Collections.unmodifiableMap(new LinkedHashMap<>(incoming));
        }

        @Nullable
        @Override
        public ItemStack getWeapon() {
            return hit.meta.getWeapon();
        }

        @NotNull
        @Override
        public EntityDamageEvent getOriginalEvent() {
            return getDamageEvent();
        }

        /**
         * {@inheritDoc}
         * <p>
         * Divinity reports what the hit was described with, which for a {@code DIVINITY_<type>} hit dealt inside
         * a dealer's request is that type and amount rather than the request's own amounts.
         */
        @NotNull
        @Override
        public Map<String, Double> getRequestedAmounts() {
            return hit.requestedAmounts;
        }

        /**
         * @return the damage per type that went into the formula, before defenses. Cannot be changed
         */
        @NotNull
        public Map<DamageAttribute, Double> getIncomingDamages() {
            return incoming;
        }

        /**
         * @return the final damage per type, by damage type id, in Divinity's order. A copy, which cannot be
         * changed; use {@link #setDamage(String, double)}
         */
        @NotNull
        @Override
        public Map<String, Double> getDamages() {
            Map<String, Double> byId = new LinkedHashMap<>();
            hit.meta.getDamages().forEach((type, amount) -> {
                if (type != null) byId.put(type.getId(), amount);
            });
            return Collections.unmodifiableMap(byId);
        }

        /**
         * @return the final damage per type, which cannot be changed through this map. Use
         * {@link #setDamage(DamageAttribute, double)}
         */
        @NotNull
        public Map<DamageAttribute, Double> getDamagesByAttribute() {
            return Collections.unmodifiableMap(hit.meta.getDamages());
        }

        public double getDamage(@NotNull DamageAttribute type) {
            return hit.meta.getDamage(type);
        }

        /**
         * @return the final damage of the type with this id, 0 if the hit has none or there is no such type
         */
        @Override
        public double getDamage(@NotNull String type) {
            DamageAttribute attribute = damageType(type);
            return attribute == null ? 0D : getDamage(attribute);
        }

        /**
         * Sets the final damage of one type. Negative values and NaN count as 0. A type the hit did not have
         * is added.
         */
        public void setDamage(@NotNull DamageAttribute type, double amount) {
            hit.meta.setDamage(type, amount(amount));
        }

        /**
         * Sets the final damage of the type with this id, as {@link #setDamage(DamageAttribute, double)} does.
         * An id that is not one of Divinity's damage types is ignored; see {@link #isDamageType(String)}.
         */
        @Override
        public void setDamage(@NotNull String type, double amount) {
            DamageAttribute attribute = damageType(type);
            if (attribute != null) setDamage(attribute, amount);
        }

        /**
         * @return whether this id is one of Divinity's damage types, so that {@link #setDamage(String, double)}
         * takes it
         */
        public boolean isDamageType(@NotNull String type) {
            return damageType(type) != null;
        }

        /**
         * @return the sum of the final damage of every type, not rounded
         */
        @Override
        public double getTotalDamage() {
            double total = 0D;
            for (double amount : hit.meta.getDamages().values()) total += amount;
            return total;
        }

        /**
         * Sets the total damage, scaling every type by the same factor so their shares stay the same. If every
         * type is at 0, the whole amount goes to Divinity's default damage type (not split between the listed
         * types as Codex's base class would). Negative values and NaN count as 0.
         */
        @Override
        public void setTotalDamage(double total) {
            double target  = amount(total);
            double current = getTotalDamage();
            Map<DamageAttribute, Double> damages = hit.meta.getDamages();
            if (current > 0D) {
                double factor = target / current;
                damages.replaceAll((type, amount) -> amount * factor);
                return;
            }
            if (target == 0D) return;
            DamageAttribute fallback = ItemStats.getDamageByDefault();
            damages.replaceAll((type, amount) -> 0D);
            hit.meta.setDamage(fallback, target);
        }

        /**
         * @return how much of the type the victim's defense absorbed. 0 if no defense applied
         */
        public double getDefended(@NotNull DamageAttribute type) {
            return hit.meta.getDefendedDamage(type);
        }

        public boolean isCritical() {
            return hit.meta.isCritical();
        }

        public boolean isBlocked() {
            return hit.meta.isBlocked();
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        /**
         * Cancels the hit or lets it through again, and does the same to the Bukkit event, as Divinity's other
         * events do.
         */
        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
            hit.original.setCancelled(cancelled);
        }
    }
}
