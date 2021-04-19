package su.nightexpress.quantumrpg.stats.bonus;

import java.util.List;
import java.util.function.BiFunction;

public class BonusCalculator {

	public static final BiFunction<Double, List<BiFunction<Boolean, Double, Double>>, Double> CALC_FULL;
	public static final BiFunction<Double, List<BiFunction<Boolean, Double, Double>>, Double> CALC_BONUS;
	public static final BiFunction<Double, List<BiFunction<Boolean, Double, Double>>, Double> CALC_DEFAULT;
	
	static {
		CALC_FULL = (input, bonuses) -> {
			double value = input;
			double bonus = 0D;
			
			for (BiFunction<Boolean, Double, Double> bif : bonuses) {
				value = bif.apply(false, value);
				bonus = bif.apply(true, bonus);
			}
			
			return value * (1D + bonus / 100D);
		};
		
		CALC_BONUS = (input, bonuses) -> {
			double value = 0D;
			double bonus = 0D;
			
			for (BiFunction<Boolean, Double, Double> bif : bonuses) {
				value = bif.apply(false, value);
				bonus = bif.apply(true, bonus);
			}
			if (value == 0D && bonus != 0D) value = input;
			if (bonus == 0D) bonus = 100D;
			
			return value * (bonus / 100D);
		};
		
		CALC_DEFAULT = (input, bonuses) -> {
			double result = CALC_BONUS.apply(input, bonuses);
			return result == 0D ? input : result;
		};
	}
}
