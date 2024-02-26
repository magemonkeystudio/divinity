package su.nightexpress.quantumrpg.stats.items.attributes.api;

import mc.promcteam.engine.utils.NumberUT;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quantumrpg.QuantumRPG;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.modules.list.gems.GemManager;
import su.nightexpress.quantumrpg.modules.list.refine.RefineManager;
import su.nightexpress.quantumrpg.stats.bonus.BonusCalculator;
import su.nightexpress.quantumrpg.stats.bonus.BonusMap;
import su.nightexpress.quantumrpg.stats.items.ItemTags;
import su.nightexpress.quantumrpg.stats.items.api.DuplicableItemLoreStat;
import su.nightexpress.quantumrpg.utils.ItemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class SimpleStat extends DuplicableItemLoreStat<StatBonus> implements TypedStat {

    protected Type   statType;
    private   double cap;

    public SimpleStat(
            @NotNull Type statType,
            @NotNull String name,
            @NotNull String format,
            double cap
    ) {
        super(
                statType.name(),
                name,
                format,
                "%ITEM_STAT_" + statType.name() + "%",
                ItemTags.TAG_ITEM_STAT,
                StatBonus.DATA_TYPE
        );
        this.statType = statType;
        this.cap = cap;
    }

    public double getTotal(@NotNull ItemStack item, @Nullable Player player) {
        return BonusCalculator.SIMPLE_FULL.apply(0D, get(item, player));
    }

    @NotNull
    public List<BiFunction<Boolean, Double, Double>> get(@NotNull ItemStack item, @Nullable Player player) {
        List<BiFunction<Boolean, Double, Double>> bonuses = new ArrayList<>();
        double  base    = 0;
        double  percent = 0;
        boolean has     = false;

        // Get from old format
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            for (NamespacedKey key : this.keys) {
                if (container.has(key, PersistentDataType.DOUBLE)) {
                    Double value = container.get(key, PersistentDataType.DOUBLE);
                    if (value != null) {
                        base += value;
                        has = true;
                        break;
                    }
                }
            }
        }

        for (StatBonus bonus : this.getAllRaw(item)) {
            if (!bonus.meetsRequirements(player)) continue;
            double[] value = bonus.getValue();
            if (value.length == 1) {
                if (bonus.isPercent()) percent += value[0];
            } else {
                base += value[0];
                has = true;
            }
        }

        {
            double finalBase = base;
            bonuses.add((isPercent, input) -> isPercent ? input : input + finalBase);
            double finalPercent = percent;
            bonuses.add((isPercent, input) -> isPercent ? input + finalPercent : input);
        }

        // Support for Gems adding values.
        GemManager gems = QuantumRPG.getInstance().getModuleCache().getGemManager();
        if (gems != null) {
            for (Map.Entry<GemManager.Gem, Integer> e : gems.getItemSockets(item)) {
                BonusMap bMap = e.getKey().getBonusMap(e.getValue());
                if (bMap == null) continue;

                bonuses.add(bMap.getBonus(this));
            }
        }

        // Support for Refined attributes.
        RefineManager refine = QuantumRPG.getInstance().getModuleCache().getRefineManager();
        if (refine != null && has) {
            bonuses.add(refine.getRefinedBonus(item, this));
        }

        return bonuses;
    }

    public static double getDefaultAttackSpeed(@NotNull ItemStack item) {
        return QuantumRPG.getInstance().getPMS().getDefaultSpeed(item);
    }

    @Override
    @NotNull
    public SimpleStat.Type getType() {
        return this.statType;
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
    public ItemType getItemType() {
        return this.statType.getItemType();
    }

    @Override
    public boolean isPercent() {
        return this.statType.isPercent();
    }

    @Override
    public boolean canBeNegative() {
        return this.statType.canBeNegative();
    }

    @Override
    @Deprecated
    public boolean isMainItem(@NotNull ItemStack item) {
        if (this.isPercent() || this.getItemType() == ItemType.BOTH) return true;

        if (this.getItemType() == ItemType.ARMOR && ItemUtils.isArmor(item)) return true;
        return this.getItemType() == ItemType.WEAPON && ItemUtils.isWeapon(item);
    }

    @Override
    @Nullable
    public Type getDependStat() {
        switch (this.statType) {
            case BLOCK_RATE: {
                return Type.BLOCK_DAMAGE;
            }
            case BLOCK_DAMAGE: {
                return Type.BLOCK_RATE;
            }
            case CRITICAL_RATE: {
                return Type.CRITICAL_DAMAGE;
            }
            case CRITICAL_DAMAGE: {
                return Type.CRITICAL_RATE;
            }
            default: {
                return null;
            }
        }
    }

    @Override
    @NotNull
    public String formatValue(@NotNull ItemStack item, StatBonus statBonus) {
        double val = this.fineValue(statBonus.getValue()[0]);
        if (val == 0) {
            return "";
        }

        boolean bonus = !this.isMainItem(item);
        String  sVal  = NumberUT.format(val);

        if (this.canBeNegative() || bonus) {
            sVal = (val > 0 ? EngineCfg.LORE_CHAR_POSITIVE : EngineCfg.LORE_CHAR_NEGATIVE) + sVal;
        }
        if (this.isPercent() || bonus) {
            sVal += EngineCfg.LORE_CHAR_PERCENT;
        }
        if (this.statType == Type.CRITICAL_DAMAGE && !bonus) {
            sVal += EngineCfg.LORE_CHAR_MULTIPLIER;
        }

        return sVal;
    }

    public enum ItemType {
        ARMOR,
        WEAPON,
        BOTH,
    }
}
