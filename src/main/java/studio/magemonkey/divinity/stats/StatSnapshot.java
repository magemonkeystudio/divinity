package studio.magemonkey.divinity.stats;

import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.util.random.Rnd;
import studio.magemonkey.divinity.data.api.UserProfile;
import studio.magemonkey.divinity.stats.bonus.BonusMap;
import studio.magemonkey.divinity.stats.items.ItemStats;
import studio.magemonkey.divinity.stats.items.api.ItemLoreStat;
import studio.magemonkey.divinity.stats.items.attributes.DamageAttribute;
import studio.magemonkey.divinity.stats.items.attributes.DefenseAttribute;
import studio.magemonkey.divinity.stats.items.attributes.api.SimpleStat;
import studio.magemonkey.divinity.stats.items.attributes.api.TypedStat;
import studio.magemonkey.divinity.stats.items.attributes.stats.DurabilityStat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.DoubleUnaryOperator;

/**
 * What {@link EntityStats} worked out from an entity's gear, kept until something that feeds into it changes.
 * <p>
 * There are two kinds, and they differ in who may change them:
 * <ul>
 *   <li>A <b>live</b> snapshot belongs to an entity and is filled in on demand, one section at a time:
 *   damage ranges, defenses, item stats and enchantment protection. It holds the part of a stat that only
 *   gear, sets, classes and profile buffs decide, <em>before</em> anything that is meant to be read at the
 *   moment of the hit: the roll of a damage range, the biome, and active effects (which carry charges and
 *   run out by themselves). Those three are applied by the caller each time. {@link EntityStats} drops the
 *   snapshot when one of its inputs changes; see its {@code invalidateSnapshot}.</li>
 *   <li>A <b>frozen</b> snapshot is taken when a projectile is launched and never changes. It also holds the
 *   shooter's effects and biome as they were, so the arrow does the damage it was fired with. It is rolled
 *   at impact like a live one.</li>
 * </ul>
 * A damage type is stored as its minimum and maximum, the sum over every equipped item. The roll happens
 * when the hit lands, with the same single call as before, so the distribution of results is unchanged.
 */
public final class StatSnapshot {
    private static final DoubleUnaryOperator IDENTITY = value -> value;

    private final long              validUntil;
    @Nullable
    private final UserProfile       profile;
    private final int               profileRevision;
    private final List<ItemStack>   inputItems;
    private final List<BonusMap>    inputBonuses;
    private final boolean           frozen;

    // Filled in on demand, under the lock of this snapshot
    Map<DamageAttribute, double[]>  damageRanges;
    Map<DefenseAttribute, Double>   defenses;
    double                          armorKey = Double.NaN;
    double                          toughnessKey = Double.NaN;
    final Map<SimpleStat.Type, Double> itemStats      = new HashMap<>();
    final Map<Enchantment, Double>     enchantFactors = new HashMap<>();

    // Frozen snapshots only
    @Nullable
    private Map<ItemLoreStat<?>, DoubleUnaryOperator> frozenEffects;
    @Nullable
    private Biome                                     frozenBiome;

    /** A live snapshot. */
    StatSnapshot(long validUntil,
                 @Nullable UserProfile profile,
                 int profileRevision,
                 @NotNull List<ItemStack> inputItems,
                 @NotNull List<BonusMap> inputBonuses) {
        this.validUntil = validUntil;
        this.profile = profile;
        this.profileRevision = profileRevision;
        this.inputItems = inputItems;
        this.inputBonuses = inputBonuses;
        this.frozen = false;
    }

    /** A frozen snapshot, for a projectile. */
    StatSnapshot(@Nullable Biome biome, @NotNull Map<ItemLoreStat<?>, DoubleUnaryOperator> effects) {
        this.validUntil = Long.MAX_VALUE;
        this.profile = null;
        this.profileRevision = 0;
        this.inputItems = List.of();
        this.inputBonuses = List.of();
        this.frozen = true;
        this.frozenBiome = biome;
        this.frozenEffects = effects;
    }

    public boolean isFrozen() {
        return this.frozen;
    }

    /**
     * Whether this snapshot can still be used at {@code now}. A snapshot goes out of date by itself when
     * it is older than the configured maximum age, when a profile buff it was built with runs out, and
     * when the player's active profile or its buffs are not the ones it was built with.
     */
    boolean isCurrent(long now, @Nullable UserProfile activeProfile) {
        if (this.frozen) return true;
        if (now >= this.validUntil) return false;
        if (this.profile != activeProfile) return false;
        return this.profile == null || this.profile.getBuffRevision() == this.profileRevision;
    }

    /**
     * Whether the equipment and bonus maps this snapshot was built from are what an entity has now. Items
     * are compared the way a player would see it: wear (vanilla damage and Divinity's durability) is not
     * a change, anything else about an item is.
     */
    boolean matches(@NotNull List<ItemStack> items, @NotNull List<BonusMap> bonuses) {
        if (this.inputItems.size() != items.size() || this.inputBonuses.size() != bonuses.size()) return false;
        for (int i = 0; i < bonuses.size(); i++) {
            if (this.inputBonuses.get(i) != bonuses.get(i)) return false;
        }
        synchronized (items) {
            for (int i = 0; i < items.size(); i++) {
                if (!sameIgnoringWear(this.inputItems.get(i), items.get(i))) return false;
            }
        }
        return true;
    }

    /**
     * Copies the items a snapshot is built from. The copies are what a later {@link #matches} compares
     * against, so a change made in place to an equipped stack is still seen as a change.
     */
    static List<ItemStack> copyOf(@NotNull List<ItemStack> items) {
        synchronized (items) {
            List<ItemStack> copy = new ArrayList<>(items.size());
            for (ItemStack item : items) copy.add(item == null ? null : item.clone());
            return copy;
        }
    }

    private static boolean sameIgnoringWear(@Nullable ItemStack a, @Nullable ItemStack b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a.isSimilar(b)) return true;
        if (a.getType() != b.getType()) return false;

        ItemStack unwornA = withoutWear(a);
        ItemStack unwornB = withoutWear(b);
        return unwornA.isSimilar(unwornB);
    }

    /** A copy of the item as it was when new. */
    private static ItemStack withoutWear(@NotNull ItemStack item) {
        ItemStack copy = item.clone();
        copy.setAmount(1);

        DurabilityStat durability = ItemStats.getStat(DurabilityStat.class);
        if (durability != null) {
            double[] raw = durability.getRaw(copy);
            if (raw != null && raw.length == 2 && raw[1] > 0) {
                if (durability.add(copy, new double[]{raw[1], raw[1]}, -1)) {
                    durability.syncVanillaBar(copy, raw[1], raw[1]);
                }
            }
        }

        ItemMeta meta = copy.getItemMeta();
        if (meta instanceof Damageable && ((Damageable) meta).hasDamage()) {
            ((Damageable) meta).setDamage(0);
            copy.setItemMeta(meta);
        }
        return copy;
    }

    // ------------------------------------------------------------------ //
    // Rolling and finishing: the part that is done at the moment of the hit
    // ------------------------------------------------------------------ //

    /**
     * Rolls every damage type's range once. One roll per type, from its summed minimum and maximum, then
     * the biome and the effects, in that order. A type that ends at 0 or below is left out.
     *
     * @param effect the adjusting function of a stat from the active effects
     */
    @NotNull
    static Map<DamageAttribute, Double> roll(@NotNull Map<DamageAttribute, double[]> ranges,
                                             @Nullable Biome biome,
                                             @NotNull Function<ItemLoreStat<?>, DoubleUnaryOperator> effect) {
        Map<DamageAttribute, Double> map = new HashMap<>();
        for (Map.Entry<DamageAttribute, double[]> entry : ranges.entrySet()) {
            DamageAttribute dmgAtt = entry.getKey();
            double[]        range  = entry.getValue();

            double value = Rnd.getDouble(range[0], range[1]);
            if (biome != null) value *= dmgAtt.getDamageModifierByBiome(biome); // Multiply by Biome
            value = effect.apply(dmgAtt).applyAsDouble(value);

            if (value > 0D) {
                map.put(dmgAtt, value);
            }
        }
        return map;
    }

    /** Applies the effects, then the stat's cap, to an item stat's value from gear. */
    static double finish(@NotNull SimpleStat stat, double value, @NotNull DoubleUnaryOperator effect) {
        value = effect.applyAsDouble(value);
        if (stat.getCapability() >= 0) {
            if (value > stat.getCapability()) {
                value = stat.getCapability();
            }
        }
        return value;
    }

    /**
     * Rolls the damage the shooter had at launch. Only for a frozen snapshot.
     */
    @NotNull
    public Map<DamageAttribute, Double> rollDamage() {
        if (!this.frozen || this.damageRanges == null) {
            throw new IllegalStateException("Only a frozen snapshot holds a launch's damage");
        }
        Map<ItemLoreStat<?>, DoubleUnaryOperator> effects = this.frozenEffects;
        return roll(this.damageRanges, this.frozenBiome, stat -> effects == null ? IDENTITY : effects.getOrDefault(
                stat,
                IDENTITY));
    }

    /**
     * The shooter's item stats at launch. Only for a frozen snapshot.
     */
    @NotNull
    public Map<SimpleStat.Type, Double> itemStats() {
        if (!this.frozen) {
            throw new IllegalStateException("Only a frozen snapshot holds a launch's item stats");
        }
        Map<ItemLoreStat<?>, DoubleUnaryOperator> effects = this.frozenEffects;
        Map<SimpleStat.Type, Double>              map     = new HashMap<>();
        for (SimpleStat.Type type : TypedStat.Type.values()) {
            Double raw = this.itemStats.get(type);
            if (raw == null) continue;
            SimpleStat stat = (SimpleStat) ItemStats.getStat(type);
            if (stat == null) continue;

            double value = finish(stat, raw, effects == null ? IDENTITY : effects.getOrDefault(stat, IDENTITY));
            if (value > 0D) {
                map.put(type, value);
            }
        }
        return map;
    }

    /** For {@link EntityStats}, which builds a frozen snapshot. */
    void setDamageRanges(@NotNull Map<DamageAttribute, double[]> ranges) {
        this.damageRanges = ranges;
    }

    static Map<DamageAttribute, double[]> newRanges() {
        return new LinkedHashMap<>();
    }
}
