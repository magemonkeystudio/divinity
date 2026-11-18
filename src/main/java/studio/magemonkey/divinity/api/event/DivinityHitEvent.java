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
 * Listen to {@link Prepare} or {@link Result}. This base class has no handler list of its own.
 * <h2>Order</h2>
 * Both events are fired from Divinity's {@code EntityDamageEvent} handler, at priority {@code HIGHEST}, on the
 * server thread:
 * <ol>
 *     <li>{@link Prepare}: the damage and defense per type are known, Codex's attributes and buffs have been
 *     applied, and nothing has been calculated yet. It fires just before the deprecated
 *     {@code DivinityDamageEvent.Start}.</li>
 *     <li>The dodge roll (a dodged hit ends here), critical hit, block, PvP or PvE modifiers, and the formula
 *     for each damage type, defenses included.</li>
 *     <li>{@link Result}: Divinity's final numbers per type, before anything is written to the Bukkit event.
 *     What listeners leave here is what the target takes and what vampirism, bleeding, thornmail, area
 *     damage and the deprecated {@code DivinityDamageEvent.Exit} see.</li>
 * </ol>
 * Neither event fires for a hit Divinity leaves alone: a request with {@link WeaponStatsMode#RAW},
 * vanilla-only damage handling, a target that is not a living entity, or a hit another listener cancelled
 * before Divinity's handler ran.
 * <h2>Cancelling</h2>
 * Cancelling either event cancels the Bukkit event, and un-cancelling it un-cancels the Bukkit event, as the
 * deprecated events do. After a cancelled {@link Prepare} nothing more is calculated. After a cancelled
 * {@link Result} nothing is written to the Bukkit event and no post-hit effect runs.
 */
public abstract class DivinityHitEvent extends Event implements Cancellable {

    private final LivingEntity        victim;
    private final LivingEntity        attacker;
    private final Projectile          projectile;
    private final EntityDamageEvent   original;
    private final DamageMeta          meta;
    private final DamageOrigin        origin;
    private final WeaponStatsMode     weaponStats;
    private final Map<String, Double> requestedAmounts;
    private       boolean             cancelled;

    protected DivinityHitEvent(@NotNull LivingEntity victim,
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
        this.origin = meta.getOrigin() != null ? meta.getOrigin() : inferOrigin(projectile, attacker, false);
        this.weaponStats = meta.getWeaponStatsMode() != null ? meta.getWeaponStatsMode() : WeaponStatsMode.ADD;
        this.requestedAmounts = Collections.unmodifiableMap(new LinkedHashMap<>(meta.getRequestedAmounts()));
    }

    @NotNull
    public LivingEntity getVictim() {
        return victim;
    }

    /**
     * @return the living entity the hit is attributed to: the swinging entity, the projectile's shooter, or
     * the source of a hit dealt through Divinity. Null if there is none
     */
    @Nullable
    public LivingEntity getAttacker() {
        return attacker;
    }

    @Nullable
    public Projectile getProjectile() {
        return projectile;
    }

    /**
     * @return the weapon whose stats took part in the hit: the item in the attacker's main hand, or the item a
     * projectile was launched from. Null if there is none
     */
    @Nullable
    public ItemStack getWeapon() {
        return meta.getWeapon();
    }

    @NotNull
    public DamageCause getCause() {
        return original.getCause();
    }

    /**
     * @return the Bukkit event of the hit. Do not change its damage directly: Divinity writes it after
     * {@link Result}
     */
    @NotNull
    public EntityDamageEvent getOriginalEvent() {
        return original;
    }

    /**
     * @return how the dealer described this hit through Codex, or null if it did not. For a
     * {@code DIVINITY_<type>} hit dealt through Codex's {@code DamageRegistry} while the dealer's own request
     * was open on the target, this is the dealer's request
     */
    @Nullable
    public DamageRequest getRequest() {
        return meta.getRequest();
    }

    /**
     * @return whether the hit was described by a request, so that {@link #getOrigin()} is the dealer's word
     * rather than Divinity's guess
     */
    public boolean isDescribed() {
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
    public DamageOrigin getOrigin() {
        return origin;
    }

    /**
     * How Divinity handled the attacker's equipment for this hit. This is what Divinity did, which can differ
     * from what the request asked for: {@link WeaponStatsMode#ADD} without a request,
     * {@link WeaponStatsMode#IGNORE} for Fabled's ignore-divinity setting, and {@link WeaponStatsMode#REPLACE}
     * (or {@link WeaponStatsMode#IGNORE} when the request asked for that) for a {@code DIVINITY_<type>} hit.
     * Never {@link WeaponStatsMode#RAW}, since no event is fired for such a hit.
     */
    @NotNull
    public WeaponStatsMode getWeaponStatsMode() {
        return weaponStats;
    }

    /**
     * @return the typed amounts the hit was described with, by damage type as the dealer wrote it
     * ({@code DIVINITY_fire}, {@code default}, ...). For a {@code DIVINITY_<type>} hit, that type and its
     * amount. Empty if there are none. Cannot be changed
     */
    @NotNull
    public Map<String, Double> getRequestedAmounts() {
        return requestedAmounts;
    }

    @NotNull
    protected DamageMeta getMeta() {
        return meta;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        this.original.setCancelled(cancelled);
    }

    /**
     * Divinity's guess at the origin of a hit no request describes. See {@link #getOrigin()}.
     */
    @NotNull
    public static DamageOrigin inferOrigin(@Nullable Projectile projectile,
                                           @Nullable LivingEntity attacker,
                                           boolean vanillaThorns) {
        if (vanillaThorns) return DamageOrigin.EFFECT;
        if (projectile != null) return DamageOrigin.WEAPON_PROJECTILE;
        if (attacker != null) return DamageOrigin.WEAPON_MELEE;
        return DamageOrigin.ENVIRONMENT;
    }

    @Nullable
    static DamageAttribute damageType(@NotNull String id) {
        return ItemStats.getDamageById(id);
    }

    @Nullable
    static DefenseAttribute defenseType(@NotNull String id) {
        return ItemStats.getDefenseById(id);
    }

    private static double amount(double value) {
        return Double.isNaN(value) ? 0D : Math.max(0D, value);
    }

    /**
     * The inputs of a hit, before Divinity calculates anything. The damage and defense per type can be
     * changed. A damage type set to 0 is removed.
     */
    public static class Prepare extends DivinityHitEvent {
        private static final HandlerList HANDLERS = new HandlerList();

        private final Map<DamageAttribute, Double>  damages;
        private final Map<DefenseAttribute, Double> defenses;

        public Prepare(@NotNull LivingEntity victim,
                       @Nullable LivingEntity attacker,
                       @Nullable Projectile projectile,
                       @NotNull EntityDamageEvent original,
                       @NotNull DamageMeta meta,
                       @NotNull Map<DamageAttribute, Double> damages,
                       @NotNull Map<DefenseAttribute, Double> defenses) {
            super(victim, attacker, projectile, original, meta);
            this.damages = damages;
            this.defenses = defenses;
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
     */
    public static class Result extends DivinityHitEvent {
        private static final HandlerList HANDLERS = new HandlerList();

        private final Map<DamageAttribute, Double> incoming;

        public Result(@NotNull LivingEntity victim,
                      @Nullable LivingEntity attacker,
                      @Nullable Projectile projectile,
                      @NotNull EntityDamageEvent original,
                      @NotNull DamageMeta meta,
                      @NotNull Map<DamageAttribute, Double> incoming) {
            super(victim, attacker, projectile, original, meta);
            this.incoming = Collections.unmodifiableMap(new LinkedHashMap<>(incoming));
        }

        /**
         * @return the damage per type that went into the formula, before defenses. Cannot be changed
         */
        @NotNull
        public Map<DamageAttribute, Double> getIncomingDamages() {
            return incoming;
        }

        /**
         * @return the final damage per type, which cannot be changed through this map. Use
         * {@link #setDamage(DamageAttribute, double)}
         */
        @NotNull
        public Map<DamageAttribute, Double> getDamages() {
            return Collections.unmodifiableMap(getMeta().getDamages());
        }

        /**
         * @return a copy of the final damage per type, by damage type id
         */
        @NotNull
        public Map<String, Double> getDamagesById() {
            Map<String, Double> byId = new LinkedHashMap<>();
            getMeta().getDamages().forEach((type, amount) -> byId.put(type.getId(), amount));
            return byId;
        }

        public double getDamage(@NotNull DamageAttribute type) {
            return getMeta().getDamage(type);
        }

        public double getDamage(@NotNull String type) {
            DamageAttribute attribute = damageType(type);
            return attribute == null ? 0D : getDamage(attribute);
        }

        /**
         * Sets the final damage of one type. Negative values count as 0. A type the hit did not have is added.
         */
        public void setDamage(@NotNull DamageAttribute type, double amount) {
            getMeta().setDamage(type, amount(amount));
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
         * @return the sum of the final damage of every type, not rounded
         */
        public double getTotalDamage() {
            double total = 0D;
            for (double amount : getMeta().getDamages().values()) total += amount;
            return total;
        }

        /**
         * Sets the total damage, scaling every type by the same factor so their shares stay the same. If every
         * type is at 0, the whole amount goes to the default damage type. Negative values count as 0.
         */
        public void setTotalDamage(double total) {
            double target  = amount(total);
            double current = getTotalDamage();
            Map<DamageAttribute, Double> damages = getMeta().getDamages();
            if (current > 0D) {
                double factor = target / current;
                damages.replaceAll((type, amount) -> amount * factor);
                return;
            }
            if (target == 0D) return;
            DamageAttribute fallback = ItemStats.getDamageByDefault();
            damages.replaceAll((type, amount) -> 0D);
            getMeta().setDamage(fallback, target);
        }

        /**
         * @return how much of the type the victim's defense absorbed. 0 if no defense applied
         */
        public double getDefended(@NotNull DamageAttribute type) {
            return getMeta().getDefendedDamage(type);
        }

        public boolean isCritical() {
            return getMeta().isCritical();
        }

        public boolean isBlocked() {
            return getMeta().isBlocked();
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
}
