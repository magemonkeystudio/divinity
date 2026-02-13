package studio.magemonkey.divinity.stats.items.attributes.stats;


import org.bukkit.inventory.meta.Damageable;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.util.DataUT;
import studio.magemonkey.codex.util.NamespaceResolver;
import studio.magemonkey.codex.util.random.Rnd;
import studio.magemonkey.divinity.Divinity;
import studio.magemonkey.divinity.api.event.DivinityItemDamageEvent;
import studio.magemonkey.divinity.config.EngineCfg;
import studio.magemonkey.divinity.stats.items.ItemTags;
import studio.magemonkey.divinity.stats.items.api.ItemLoreStat;
import studio.magemonkey.divinity.stats.items.attributes.api.SimpleStat;
import studio.magemonkey.divinity.stats.items.attributes.api.TypedStat;

public class DurabilityStat extends ItemLoreStat<double[]> implements TypedStat {
    private double cap;

    public DurabilityStat(
            @NotNull String name,
            @NotNull String format,
            double cap) {
        super(
                TypedStat.Type.DURABILITY.name(),
                name,
                format,
                "%ITEM_STAT_" + TypedStat.Type.DURABILITY.name() + "%",
                ItemTags.TAG_ITEM_STAT,
                DataUT.DOUBLE_ARRAY);
        this.cap = cap;

        // Legacy keys
        this.keys.add(NamespacedKey.fromString("prorpgitems:item_stat_durability"));
        this.keys.add(NamespacedKey.fromString("prorpgitems:qrpg_item_stat_durability"));
        this.keys.add(NamespacedKey.fromString("quantumrpg:qrpg_item_stat_durability"));
    }

    @Override
    @NotNull
    public Class<double[]> getParameterClass() {
        return double[].class;
    }

    @Override
    @NotNull
    public SimpleStat.Type getType() {
        return TypedStat.Type.DURABILITY;
    }

    @Override
    public double getCapability() {
        return this.cap;
    }

    @Override
    public void setCapability(double cap) {
        this.cap = cap;
    }

    @Override
    @NotNull
    public SimpleStat.ItemType getItemType() {
        return TypedStat.Type.DURABILITY.getItemType();
    }

    @Override
    public boolean isPercent() {
        return TypedStat.Type.DURABILITY.isPercent();
    }

    @Override
    public boolean canBeNegative() {
        return TypedStat.Type.DURABILITY.canBeNegative();
    }

    @Override
    @Deprecated
    public boolean isMainItem(@NotNull ItemStack item) {
        return true;
    }

    @Override
    @Nullable
    public SimpleStat.Type getDependStat() {
        return null;
    }

    public boolean isUnbreakable(@NotNull ItemStack item) {
        double[] arr = this.getRaw(item);
        if (arr != null) {
            return arr[1] == -1;
        } else {
            return false;
        }
    }

    public boolean isDamaged(@NotNull ItemStack item) {
        if (this.isUnbreakable(item)) {
            return false;
        }
        double[] durability = this.getRaw(item);
        return durability != null && durability[0] < durability[1];
    }

    public boolean isBroken(@NotNull ItemStack item) {
        double[] durability = this.getRaw(item);
        return durability != null && durability[0] == 0 && !EngineCfg.ATTRIBUTES_DURABILITY_BREAK_ITEMS;
    }

    public boolean reduceDurability(
            @NotNull LivingEntity li, @NotNull ItemStack item, int amount) {

        if (!(li instanceof Player) && !EngineCfg.ATTRIBUTES_DURABILITY_REDUCE_FOR_MOBS) return false;
        if (this.isUnbreakable(item)) return false;

        ItemMeta meta = item.getItemMeta();

        // Vanilla unbreaking formula
        final Enchantment unbreaking =
                NamespaceResolver.getEnchantment("UNBREAKING", "DURABILITY"); // UNBREAKING/DURABILITY
        if (meta != null && meta.hasEnchant(unbreaking)) {
            double lvl    = meta.getEnchantLevel(unbreaking);
            double chance = (100D / (lvl + 1D));
            if (Rnd.get(true) < chance) {
                return false;
            }
        }

        double[] durability = this.getRaw(item);
        if (durability == null) return false;
        double current = durability[0];
        // Stop if item is already broken
        if (current == 0) return false;

        // Custom item damage event
        DivinityItemDamageEvent eve = new DivinityItemDamageEvent(item, li);
        Divinity.getInstance().getPluginManager().callEvent(eve);
        if (eve.isCancelled()) return false;

        double max  = durability[1];
        double lose = current - Math.min(amount, current);

        if (lose <= 0) {
            if (EngineCfg.ATTRIBUTES_DURABILITY_BREAK_ITEMS) {
                item.setAmount(0);
                li.getWorld().playSound(li.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.8f, 0.8f);
                return false;
            }
        }

        boolean result = this.add(item, new double[]{lose, max}, -1);

        if (result) {
            syncVanillaBar(item, lose, max);
        }

        return result;

    }

    @Override
    @NotNull
    public String formatValue(@NotNull ItemStack item, double[] values) {
        return EngineCfg.getDurabilityFormat((int) values[0], (int) values[1]);
    }
    public void syncVanillaBar(@NotNull ItemStack item, double current, double maxCustom) {

    ItemMeta meta = item.getItemMeta();
    if (!(meta instanceof org.bukkit.inventory.meta.Damageable)) return;

    org.bukkit.inventory.meta.Damageable damageable =
            (org.bukkit.inventory.meta.Damageable) meta;

    int maxVanilla = item.getType().getMaxDurability();
    if (maxVanilla <= 0) return;

    double percent = current / maxCustom;
    int vanillaDamage = (int) ((1.0 - percent) * maxVanilla);

    damageable.setDamage(vanillaDamage);
    item.setItemMeta((ItemMeta) damageable);
}

}