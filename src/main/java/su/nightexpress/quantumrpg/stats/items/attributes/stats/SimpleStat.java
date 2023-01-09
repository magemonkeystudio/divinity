package su.nightexpress.quantumrpg.stats.items.attributes.stats;

import mc.promcteam.engine.utils.NumberUT;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quantumrpg.config.EngineCfg;
import su.nightexpress.quantumrpg.stats.items.attributes.api.AbstractStat;

public class SimpleStat extends AbstractStat<Double> {

    public SimpleStat(@NotNull Type statType, @NotNull String name, @NotNull String format, double cap) {
        super(statType, name, format, cap, PersistentDataType.DOUBLE);
    }

    @Override
    @NotNull
    public String formatValue(@NotNull ItemStack item, Double values) {
        double val = this.fineValue(values);
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
}
