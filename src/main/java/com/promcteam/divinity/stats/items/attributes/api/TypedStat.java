package com.promcteam.divinity.stats.items.attributes.api;

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
        AOE_DAMAGE(SimpleStat.ItemType.WEAPON, true, false, true),
        PVP_DAMAGE(SimpleStat.ItemType.WEAPON, true, true, true),
        PVE_DAMAGE(SimpleStat.ItemType.WEAPON, true, true, true),
        DODGE_RATE(SimpleStat.ItemType.ARMOR, true, true, true),
        ACCURACY_RATE(SimpleStat.ItemType.WEAPON, true, true, true),
        BLOCK_RATE(SimpleStat.ItemType.BOTH, true, true, true),
        BLOCK_DAMAGE(SimpleStat.ItemType.ARMOR, true, true, true),
        LOOT_RATE(SimpleStat.ItemType.BOTH, true, true, true),
        BURN_RATE(SimpleStat.ItemType.WEAPON, true, true, true),
        PVP_DEFENSE(SimpleStat.ItemType.ARMOR, true, false, true),
        PVE_DEFENSE(SimpleStat.ItemType.ARMOR, true, true, true),
        CRITICAL_RATE(SimpleStat.ItemType.WEAPON, true, true, true),
        CRITICAL_DAMAGE(SimpleStat.ItemType.WEAPON, false, false, true),
        DURABILITY(SimpleStat.ItemType.BOTH, false, true, false),
        MOVEMENT_SPEED(SimpleStat.ItemType.ARMOR, true, true, true),
        PENETRATION(SimpleStat.ItemType.WEAPON, true, true, true),
        BASE_ATTACK_SPEED(SimpleStat.ItemType.BOTH, false, true, true),
        ATTACK_SPEED(SimpleStat.ItemType.BOTH, true, true, true),
        VAMPIRISM(SimpleStat.ItemType.WEAPON, true, true, true),
        MAX_HEALTH(SimpleStat.ItemType.BOTH, false, true, true),
        BLEED_RATE(SimpleStat.ItemType.WEAPON, true, true, true),
        DISARM_RATE(SimpleStat.ItemType.WEAPON, true, true, true),
        SALE_PRICE(SimpleStat.ItemType.BOTH, true, true, false),
        THORNMAIL(SimpleStat.ItemType.ARMOR, true, false, true),
        HEALTH_REGEN(SimpleStat.ItemType.BOTH, true, true, true),
        MANA_REGEN(SimpleStat.ItemType.BOTH, true, true, true),
        ARMOR_TOUGHNESS(SimpleStat.ItemType.ARMOR, false, true, true),
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
