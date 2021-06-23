package su.nightexpress.quantumrpg.stats.items.attributes.api;

import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.utils.DataUT;

public abstract class DoubleStat extends AbstractStat<double[]> {

	public DoubleStat(@NotNull Type statType, @NotNull String name, @NotNull String format, double cap) {
		super(statType, name, format, cap, DataUT.DOUBLE_ARRAY);
	}
}
