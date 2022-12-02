package su.nightexpress.quantumrpg.stats.items.attributes.api;

import mc.promcteam.engine.utils.DataUT;
import org.jetbrains.annotations.NotNull;

public abstract class DoubleStat extends AbstractStat<double[]> {

    public DoubleStat(@NotNull Type statType, @NotNull String name, @NotNull String format, double cap) {
        super(statType, name, format, cap, DataUT.DOUBLE_ARRAY);
    }
}
