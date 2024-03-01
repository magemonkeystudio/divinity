package su.nightexpress.quantumrpg.stats.items.attributes.stats;

import mc.promcteam.engine.utils.DataUT;
import mc.promcteam.engine.utils.random.Rnd;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.api.event.RPGItemDamageEvent;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.stats.items.api.ItemLoreStat;
import su.nightexpress.quantumrpg.stats.items.attributes.api.SimpleStat;
import su.nightexpress.quantumrpg.stats.items.attributes.api.TypedStat;

public class DurabilityStat extends ItemLoreStat<double[]> implements TypedStat {
    private   double          cap;

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
        if (!EngineCfg.ATTRIBUTES_DURABILITY_BREAK_ITEMS) return false;
        double[] durability = this.getRaw(item);
        return durability != null && durability[0] == 0;
    }

    public boolean reduceDurability(
            @NotNull LivingEntity li, @NotNull ItemStack item, int amount) {

        if (!(li instanceof Player) && !EngineCfg.ATTRIBUTES_DURABILITY_REDUCE_FOR_MOBS) return false;
        if (this.isUnbreakable(item)) return false;

        ItemMeta meta = item.getItemMeta();

        // Vanilla unbreaking formula
        if (meta != null && meta.hasEnchant(Enchantment.DURABILITY)) {
            double lvl    = meta.getEnchantLevel(Enchantment.DURABILITY);
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
        RPGItemDamageEvent eve = new RPGItemDamageEvent(item, li);
        QuantumRPG.getInstance().getPluginManager().callEvent(eve);
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

        return this.add(item, new double[]{lose, max}, -1);
    }

    @Override
    @NotNull
    public String formatValue(@NotNull ItemStack item, double[] values) {
        return EngineCfg.getDurabilityFormat((int) values[0], (int) values[1]);
    }
}
