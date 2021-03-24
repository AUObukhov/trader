package ru.obukhov.trader.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MathUtils {

    /**
     * @return average value of passed collection with scale = {@link DecimalUtils#DEFAULT_SCALE}
     * and rounding mode = Half Up
     */
    public static BigDecimal getAverage(Collection<BigDecimal> numbers) {
        return numbers.stream()
                .reduce(BigDecimal::add)
                .map(sum -> DecimalUtils.divide(sum, numbers.size()))
                .orElse(BigDecimal.ZERO);
    }

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
    public static BigDecimal getWeightedAverage(SortedMap<OffsetDateTime, BigDecimal> dateTimesToAmounts,
                                                OffsetDateTime endDateTime) {
        if (dateTimesToAmounts.isEmpty()) {
            return BigDecimal.ZERO;
        }

        Map<BigDecimal, Double> weightedAmounts = getWeightedAmounts(dateTimesToAmounts, endDateTime);

        return getWeightedAverage(weightedAmounts);
    }

    private static Map<BigDecimal, Double> getWeightedAmounts(SortedMap<OffsetDateTime, BigDecimal> dateTimesToAmounts,
                                                              OffsetDateTime endDateTime) {

        Map<BigDecimal, Double> weightedInvestments = new HashMap<>();
        Iterator<Map.Entry<OffsetDateTime, BigDecimal>> iterator = dateTimesToAmounts.entrySet().iterator();
        Map.Entry<OffsetDateTime, BigDecimal> currentEntry = iterator.next();
        while (iterator.hasNext()) {
            Map.Entry<OffsetDateTime, BigDecimal> nextEntry = iterator.next();
            addWeightedAmount(weightedInvestments, currentEntry.getValue(), currentEntry.getKey(), nextEntry.getKey());

            currentEntry = nextEntry;
        }
        addWeightedAmount(weightedInvestments, currentEntry.getValue(), currentEntry.getKey(), endDateTime);

        return weightedInvestments;
    }

    private static void addWeightedAmount(Map<BigDecimal, Double> weightedAmounts,
                                          BigDecimal amount,
                                          OffsetDateTime from,
                                          OffsetDateTime to) {
        Double weight = (double) Duration.between(from, to).toMillis();
        weightedAmounts.put(amount, weight);
    }

    private static BigDecimal getWeightedAverage(Map<BigDecimal, Double> weightedAmounts) {
        double weightsSum = weightedAmounts.values().stream().reduce(0.0, Double::sum);

        BigDecimal weightedAverage = BigDecimal.ZERO;
        for (Map.Entry<BigDecimal, Double> entry : weightedAmounts.entrySet()) {
            double weight = entry.getValue() / weightsSum;
            BigDecimal weightedAmount = DecimalUtils.multiply(entry.getKey(), weight);
            weightedAverage = weightedAverage.add(weightedAmount);
        }
        return DecimalUtils.setDefaultScale(weightedAverage);
    }

    /**
     * @return average value of passed {@code numbers} with scale = {@link DecimalUtils#DEFAULT_SCALE}
     * and rounding mode = Half Up
     */
    public static BigDecimal getAverage(BigDecimal... numbers) {
        return getAverage(Arrays.asList(numbers));
    }

    /**
     * @return maximum of given {@code values}, or null if {@code values} is empty
     */
    @Nullable
    public static Double max(List<Double> values) {
        return values.stream()
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    /**
     * @return minimum of given {@code values}, or null if {@code values} is empty
     */
    @Nullable
    public static Double min(List<Double> values) {
        return values.stream()
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    /**
     * Calculates simple moving averages of given {@code values}
     *
     * @param values list of values for which averages are calculated for, must be greater than zero
     * @param period count of values, used for calculation of each average, must be greater than zero
     * @return list of calculated averages
     */
    public static List<BigDecimal> getSimpleMovingAverages(List<BigDecimal> values, int period) {
        Assert.isTrue(period > 0, "period must be greater than zero");

        // filling of first {period} averages
        List<BigDecimal> movingAverages = new ArrayList<>(values.size());
        for (int i = 0; i < period && i < values.size(); i++) {
            BigDecimal sum = BigDecimal.ZERO;
            for (int j = 0; j <= i; j++) {
                sum = sum.add(values.get(j));
            }

            movingAverages.add(DecimalUtils.divide(sum, i + 1));
        }

        // filling of the rest averages
        for (int i = period; i < values.size(); i++) {
            BigDecimal excludedValue = DecimalUtils.divide(values.get(i - period), period);
            BigDecimal addedValue = DecimalUtils.divide(values.get(i), period);
            BigDecimal currentAverage = movingAverages.get(i - 1).subtract(excludedValue).add(addedValue);
            movingAverages.add(currentAverage);
        }

        return movingAverages;
    }

    /**
     * Calculates weighted moving averages of given {@code values} by given {@code period}
     *
     * @param values values for which averages are calculated for, must be greater than zero
     * @param period count of values, used for calculation of each average, must be greater than zero
     * @return list of calculated averages
     */
    public static List<BigDecimal> getWeightedMovingAverages(List<BigDecimal> values, int period) {
        Assert.isTrue(period > 0, "period must be greater than zero");

        List<BigDecimal> weightedMovingAverages = new ArrayList<>(values.size());
        for (int i = 0; i < values.size(); i++) {
            weightedMovingAverages.add(getWeightedMovingAverage(values, i, period));
        }

        return weightedMovingAverages;
    }

    private static BigDecimal getWeightedMovingAverage(List<BigDecimal> values,
                                                       final int index,
                                                       final int period) {
        final int maxPeriod = index + 1;
        final int normalizedPeriod = Math.min(period, maxPeriod);
        BigDecimal sum = DecimalUtils.multiply(values.get(index), normalizedPeriod);
        BigDecimal weightsSum = BigDecimal.valueOf(normalizedPeriod);

        for (int i = index - 1; i > index - normalizedPeriod; i--) {
            int weight = normalizedPeriod - (index - i);
            sum = sum.add(DecimalUtils.multiply(values.get(i), weight));
            weightsSum = weightsSum.add(BigDecimal.valueOf(weight));
        }

        double divisor = normalizedPeriod * (normalizedPeriod + 1) / 2.0;
        return DecimalUtils.divide(sum, divisor);
    }

}