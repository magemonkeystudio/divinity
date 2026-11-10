package studio.magemonkey.divinity.stats.items.attributes.api;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TypedStat {
    String getId();

    String getPlaceholder();

    boolean hasPlaceholder(@NotNull ItemStack item);

    boolean isApplied(@NotNull ItemStack item);

    @NotNull
    SimpleStat.Type getType();

    double getCapability();

    void setCapability(double cap);

    SimpleStat.ItemType getItemType();

    boolean isPercent();

    boolean canBeNegative();

    @Deprecated
    boolean isMainItem(@NotNull ItemStack item);

    @Nullable
    SimpleStat.Type getDependStat();

    default double fineValue(double value) {
        if (this.getType() == Type.DURABILITY) return value;

        if (this.getCapability() >= 0 && value > this.getCapability()) {
            value = this.getCapability();
        }
        if (value < 0 && !this.canBeNegative()) {
            value = 0;
        }
        return value;
    }

    enum Type {

        //        DIRECT_DAMAGE(ItemType.WEAPON, true, false, true),
        // Generic vanilla types
        ARMOR(SimpleStat.ItemType.BOTH, false, true, true),
        ARMOR_TOUGHNESS(SimpleStat.ItemType.BOTH, false, true, true),
        ATTACK_SPEED(SimpleStat.ItemType.BOTH, true, true, true),
        BASE_ATTACK_SPEED(SimpleStat.ItemType.BOTH, false, true, true),
        KNOCKBACK_RESISTANCE(SimpleStat.ItemType.BOTH, false, true, true),
        MAX_HEALTH(SimpleStat.ItemType.BOTH, false, true, true),
        MOVEMENT_SPEED(SimpleStat.ItemType.BOTH, true, true, true),

        // All the other types
        AOE_DAMAGE(SimpleStat.ItemType.BOTH, true, false, true),
        PVP_DAMAGE(SimpleStat.ItemType.BOTH, true, true, true),
        PVE_DAMAGE(SimpleStat.ItemType.BOTH, true, true, true),
        DODGE_RATE(SimpleStat.ItemType.BOTH, true, true, true),
        ACCURACY_RATE(SimpleStat.ItemType.BOTH, true, true, true),
        BLOCK_RATE(SimpleStat.ItemType.BOTH, true, true, true),
        BLOCK_DAMAGE(SimpleStat.ItemType.BOTH, true, true, true),
        LOOT_RATE(SimpleStat.ItemType.BOTH, true, true, true),
        BURN_RATE(SimpleStat.ItemType.BOTH, true, true, true),
        PVP_DEFENSE(SimpleStat.ItemType.BOTH, true, false, true),
        PVE_DEFENSE(SimpleStat.ItemType.BOTH, true, true, true),
        CRITICAL_RATE(SimpleStat.ItemType.BOTH, true, true, true),
        CRITICAL_DAMAGE(SimpleStat.ItemType.BOTH, false, false, true),
        DURABILITY(SimpleStat.ItemType.BOTH, false, true, false),
        PENETRATION(SimpleStat.ItemType.BOTH, true, true, true),
        VAMPIRISM(SimpleStat.ItemType.BOTH, true, true, true),
        BLEED_RATE(SimpleStat.ItemType.BOTH, true, true, true),
        DISARM_RATE(SimpleStat.ItemType.BOTH, true, true, true),
        SALE_PRICE(SimpleStat.ItemType.BOTH, true, true, false),
        THORNMAIL(SimpleStat.ItemType.BOTH, true, false, true),
        HEALTH_REGEN(SimpleStat.ItemType.BOTH, true, true, true),
        MANA_REGEN(SimpleStat.ItemType.BOTH, true, true, true),
        // Entity size (maps to generic.scale Bukkit attribute)
        SCALE(SimpleStat.ItemType.BOTH, false, true, true),
        // MC 1.21+ vanilla attributes
        WATER_MOVEMENT_EFFICIENCY(SimpleStat.ItemType.BOTH, true, false, true),
        MOVEMENT_EFFICIENCY(SimpleStat.ItemType.BOTH, true, false, true),
        SNEAKING_SPEED(SimpleStat.ItemType.BOTH, true, false, true),
        // MC 1.20.5+ vanilla attributes
        BLOCK_BREAK_SPEED(SimpleStat.ItemType.BOTH, true, false, true),
        BLOCK_INTERACTION_RANGE(SimpleStat.ItemType.BOTH, false, true, true),
        ENTITY_INTERACTION_RANGE(SimpleStat.ItemType.BOTH, false, true, true),
        EXPLOSION_KNOCKBACK_RESISTANCE(SimpleStat.ItemType.BOTH, true, false, true),
        FALL_DAMAGE_MULTIPLIER(SimpleStat.ItemType.BOTH, true, true, true),
        FLYING_SPEED(SimpleStat.ItemType.BOTH, false, false, true),
        GRAVITY(SimpleStat.ItemType.BOTH, false, true, true),
        JUMP_STRENGTH(SimpleStat.ItemType.BOTH, false, false, true),
        MAX_ABSORPTION(SimpleStat.ItemType.BOTH, false, false, true),
        MINING_EFFICIENCY(SimpleStat.ItemType.BOTH, false, false, true),
        OXYGEN_BONUS(SimpleStat.ItemType.BOTH, false, false, true),
        SAFE_FALL_DISTANCE(SimpleStat.ItemType.BOTH, false, false, true),
        STEP_HEIGHT(SimpleStat.ItemType.BOTH, false, false, true),
        SUBMERGED_MINING_SPEED(SimpleStat.ItemType.BOTH, true, false, true),
        MAX_MANA(SimpleStat.ItemType.BOTH, false, false, true),
        CC_RESISTANCE(SimpleStat.ItemType.BOTH, true, false, true),
        CC_DURATION(SimpleStat.ItemType.BOTH, true, false, true),
        HEALING_CAST(SimpleStat.ItemType.BOTH, true, false, true),
        HEALING_RECEIVED(SimpleStat.ItemType.BOTH, true, false, true),
        //PLACEHOLDERS
            //MAGIC AND SUMMONS
        SKILL_EFFECTIVNESS(SimpleStat.ItemType.BOTH, true, true, true),
        SUMMON_POWER(SimpleStat.ItemType.BOTH, true, true, true),
        SUMMON_HP(SimpleStat.ItemType.BOTH, true, true, true),
        SUMMON_DURATION(SimpleStat.ItemType.BOTH, true, true, true),
         //projectiles
        PROJECTILE_COUNT(SimpleStat.ItemType.BOTH, false, true, true),
        PROJECTILE_SPEED(SimpleStat.ItemType.BOTH, true, true, true),
            //BLEED AND STUN
        BLEED_STACKS(SimpleStat.ItemType.BOTH, false, true, true),
        BLEED_DURATION(SimpleStat.ItemType.BOTH, false, true, true),
        BLEED_DAMAGEBUFF(SimpleStat.ItemType.BOTH, true, true, true),
        STUN_STACKS(SimpleStat.ItemType.BOTH, false, true, true),
        STUN_DURATION(SimpleStat.ItemType.BOTH, true, true, true),
            //
        ;

        private final SimpleStat.ItemType type;
        private final boolean             perc;
        private final boolean             canNegate;
        private final boolean             isGlobal;

        Type(@NotNull SimpleStat.ItemType type, boolean perc, boolean nega, boolean isGlobal) {
            this.type = type;
            this.perc = perc;
            this.canNegate = nega;
            this.isGlobal = isGlobal;
        }

        @Nullable
        public static Type getByName(@NotNull String s) {
            try {
                return valueOf(s.toUpperCase());
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }

        @NotNull
        public SimpleStat.ItemType getItemType() {
            return this.type;
        }

        public boolean isPercent() {
            return this.perc;
        }

        public boolean canBeNegative() {
            return this.canNegate;
        }

        /**
         * Defines is this stat is entity-global (true) or per-item based (false).
         *
         * @return
         */
        public boolean isGlobal() {
            return isGlobal;
        }
    }
}
