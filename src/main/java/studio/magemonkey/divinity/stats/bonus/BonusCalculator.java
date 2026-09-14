package studio.magemonkey.divinity.stats.bonus;

import java.util.List;
import java.util.function.BiFunction;

public class BonusCalculator {
    public static final BiFunction<Double, List<BiFunction<Boolean, Double, Double>>, Double> SIMPLE_FULL =
            (input, bonuses) -> {
                double value   = input;
                double percent = 0D;

                for (BiFunction<Boolean, Double, Double> bif : bonuses) {
                    value = bif.apply(false, value);
                    percent = bif.apply(true, percent);
                }

                return value * (1D + percent / 100D);
            };

    /**
     * Sums the flat and percent channels directly instead of treating percent as a
     * multiplier on the flat channel. Use this for stats whose "percent" bonuses are
     * themselves the stat's value (e.g. a percent-based buff/penetration amount) rather
     * than a modifier applied to some other base value — otherwise a percent-only bonus
     * on a zero base would always evaluate to zero.
     */
    public static final BiFunction<Double, List<BiFunction<Boolean, Double, Double>>, Double> SIMPLE_ADDITIVE =
            (input, bonuses) -> {
                double value   = input;
                double percent = 0D;

                for (BiFunction<Boolean, Double, Double> bif : bonuses) {
                    value = bif.apply(false, value);
                    percent = bif.apply(true, percent);
                }

                return value + percent;
            };

    public static final BiFunction<Double, List<BiFunction<Boolean, Double, Double>>, Double> SIMPLE_BONUS =
            (input, bonuses) -> {
                double value   = 0D;
                double percent = 0D;

                for (BiFunction<Boolean, Double, Double> bif : bonuses) {
                    value = bif.apply(false, value);
                    percent = bif.apply(true, percent);
                }
                if (value == 0D && percent != 0D) value = input;
                if (percent == 0D) percent = 100D;

                return value * (percent / 100D);
            };

    public static final BiFunction<Double, List<BiFunction<Boolean, Double, Double>>, Double> SIMPLE_DEFAULT =
            (input, bonuses) -> {
                double result = SIMPLE_BONUS.apply(input, bonuses);
                return result == 0D ? input : result;
            };

    public static final BiFunction<double[], List<BiFunction<Boolean, double[], double[]>>, double[]> RANGE_FULL =
            (input, bonuses) -> {
                double[] value   = input;
                double[] percent = new double[]{0};

                for (BiFunction<Boolean, double[], double[]> bif : bonuses) {
                    value = bif.apply(false, value);
                    percent = bif.apply(true, percent);
                }

                return new double[]{value[0] * (1D + percent[0] / 100D), value[1] * (1D + percent[0] / 100D)};
            };
}
