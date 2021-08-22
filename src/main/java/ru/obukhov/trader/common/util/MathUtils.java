package ru.obukhov.trader.common.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

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
                .orElse(BigDecimal.ZERO);
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
     * @param endDateTime        end dateTime of last amount from given {@code dateTimesToAmounts}
     * @return weighted average or zero if given {@code dateTimesToAmounts} is empty
     */
    public static BigDecimal getWeightedAverage(final SortedMap<OffsetDateTime, BigDecimal> dateTimesToAmounts, final OffsetDateTime endDateTime) {
        if (dateTimesToAmounts.isEmpty()) {
            return BigDecimal.ZERO;
        }

        final Map<BigDecimal, Double> weightedAmounts = getWeightedAmounts(dateTimesToAmounts, endDateTime);

        return getWeightedAverage(weightedAmounts);
    }

    private static Map<BigDecimal, Double> getWeightedAmounts(
            final SortedMap<OffsetDateTime, BigDecimal> dateTimesToAmounts,
            final OffsetDateTime endDateTime
    ) {
        final Map<BigDecimal, Double> weightedInvestments = new HashMap<>();
        final Iterator<Map.Entry<OffsetDateTime, BigDecimal>> iterator = dateTimesToAmounts.entrySet().iterator();
        Map.Entry<OffsetDateTime, BigDecimal> currentEntry = iterator.next();
        while (iterator.hasNext()) {
            final Map.Entry<OffsetDateTime, BigDecimal> nextEntry = iterator.next();
            addWeightedAmount(weightedInvestments, currentEntry.getValue(), currentEntry.getKey(), nextEntry.getKey());

            currentEntry = nextEntry;
        }
        addWeightedAmount(weightedInvestments, currentEntry.getValue(), currentEntry.getKey(), endDateTime);

        return weightedInvestments;
    }

    private static void addWeightedAmount(
            final Map<BigDecimal, Double> weightedAmounts,
            final BigDecimal amount,
            final OffsetDateTime from,
            final OffsetDateTime to
    ) {
        final Double weight = (double) Duration.between(from, to).toMillis();
        weightedAmounts.put(amount, weight);
    }

    private static BigDecimal getWeightedAverage(final Map<BigDecimal, Double> weightedAmounts) {
        final double weightsSum = weightedAmounts.values().stream().reduce(0.0, Double::sum);

        BigDecimal weightedAverage = BigDecimal.ZERO;
        for (final Map.Entry<BigDecimal, Double> entry : weightedAmounts.entrySet()) {
            final double weight = entry.getValue() / weightsSum;
            final BigDecimal weightedAmount = DecimalUtils.multiply(entry.getKey(), weight);
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

}