package ru.obukhov.trader.common.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class MathUtils {

    /**
     * @return average value of passed collection with scale = {@link DecimalUtils#DEFAULT_SCALE}
     * and rounding mode = Half Up
     */
    public static BigDecimal getAverage(final Collection<BigDecimal> numbers) {
        return numbers.stream()
                .reduce(BigDecimal::add)
                .map(sum -> DecimalUtils.divide(sum, numbers.size()))
                .orElse(DecimalUtils.setDefaultScale(0));
    }

    /**
     * @return average value of passed {@code numbers} with scale = {@link DecimalUtils#DEFAULT_SCALE}
     * and rounding mode = Half Up
     */
    public static BigDecimal getAverage(final BigDecimal... numbers) {
        return getAverage(List.of(numbers));
    }

    // region getWeightedAverage

    /**
     * Calculates weighted average of given amounts.<br/>
     * Weight is count of milliseconds every amount was actual.
     * Every amount is actual from its begin dateTime inclusive to begin dateTime of next amount exclusive.
     * The last amount is actual until given {@code endDateTime}
     *
     * @param dateTimesToAmounts begin dateTimes and corresponding amounts
     * @param endDateTime        end dateTime of last amount from given {@code dateTimesToAmounts}.
     *                           Can't be before any of dateTime in {@code dateTimesToAmounts}
     * @return weighted average or zero if given {@code dateTimesToAmounts} is empty
     */
    public static BigDecimal getWeightedAverage(final Map<OffsetDateTime, BigDecimal> dateTimesToAmounts, final OffsetDateTime endDateTime) {
        final Map<Long, BigDecimal> weightedAmounts = dateTimesToAmounts.entrySet().stream()
                .collect(Collectors.toMap(entry -> getWeight(entry.getKey(), endDateTime), Map.Entry::getValue));

        return getWeightedAverage(weightedAmounts);
    }

    private static long getWeight(final OffsetDateTime dateTime, final OffsetDateTime endDateTime) {
        Assert.isTrue(!endDateTime.isBefore(dateTime), "All dateTimes must be before endDateTime");

        return Duration.between(dateTime, endDateTime).toMillis();
    }

    private static BigDecimal getWeightedAverage(final Map<Long, BigDecimal> weightedAmounts) {
        final BigDecimal weightsSum = DecimalUtils.setDefaultScale(weightedAmounts.keySet().stream().reduce(0L, Long::sum));

        BigDecimal weightedAverage = DecimalUtils.setDefaultScale(0);
        for (final Map.Entry<Long, BigDecimal> entry : weightedAmounts.entrySet()) {
            final BigDecimal normalizedWeight = DecimalUtils.divideAccurate(entry.getKey(), weightsSum);
            final BigDecimal weightedAmount = entry.getValue().multiply(normalizedWeight);
            weightedAverage = weightedAverage.add(weightedAmount);
        }

        return DecimalUtils.setDefaultScale(weightedAverage);
    }

    // endregion

    /**
     * @return maximum of given {@code values}, or null if {@code values} is empty
     */
    @Nullable
    public static Double max(final List<Double> values) {
        return values.stream()
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    /**
     * @return minimum of given {@code values}, or null if {@code values} is empty
     */
    @Nullable
    public static Double min(final List<Double> values) {
        return values.stream()
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    /**
     * @return dividend / divisor rounded up
     */
    public static long divideRoundUp(final long dividend, final long divisor) {
        long quotient = dividend / divisor;
        final long remainder = dividend % divisor;

        if (remainder != 0 && 2 * Math.abs(remainder) >= Math.abs(divisor)) {
            quotient += (dividend < 0) != (divisor < 0) ? -1 : 1;
        }

        return quotient;
    }

}