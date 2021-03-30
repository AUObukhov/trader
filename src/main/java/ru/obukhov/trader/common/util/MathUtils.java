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
import java.util.function.Function;
import java.util.stream.Collectors;

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
     * @return average value of passed {@code numbers} with scale = {@link DecimalUtils#DEFAULT_SCALE}
     * and rounding mode = Half Up
     */
    public static BigDecimal getAverage(BigDecimal... numbers) {
        return getAverage(Arrays.asList(numbers));
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

    // endregion

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

    // region getSimpleMovingAverages

    /**
     * Calculates simple moving averages of given {@code elements}
     *
     * @param elements       elements containing values, for which averages are calculated for
     * @param valueExtractor function to get value from current element
     * @param window         count of values, used for calculation of each average, must be positive
     * @return list of calculated averages
     */
    public static <T> List<BigDecimal> getSimpleMovingAverages(
            List<T> elements,
            Function<T, BigDecimal> valueExtractor,
            int window
    ) {
        List<BigDecimal> values = elements.stream().map(valueExtractor).collect(Collectors.toList());
        return getSimpleMovingAverages(values, window);
    }

    /**
     * Calculates simple moving averages of given {@code values}
     *
     * @param values values, for which averages are calculated for
     * @param window count of values, used for calculation of each average, must be positive
     * @return list of calculated averages
     */
    public static List<BigDecimal> getSimpleMovingAverages(List<BigDecimal> values, int window) {
        Assert.isTrue(window > 0, "window must be positive");

        // filling of first {window} averages
        List<BigDecimal> movingAverages = new ArrayList<>(values.size());
        int count = Math.min(window, values.size());
        for (int i = 0; i < count; i++) {
            BigDecimal sum = BigDecimal.ZERO;
            for (int j = 0; j <= i; j++) {
                BigDecimal value = values.get(j);
                sum = sum.add(value);
            }

            movingAverages.add(DecimalUtils.divide(sum, i + 1));
        }

        // filling of the rest averages
        count = values.size();
        for (int i = window; i < count; i++) {
            BigDecimal excludedValue = DecimalUtils.divide(values.get(i - window), window);
            BigDecimal addedValue = DecimalUtils.divide(values.get(i), window);
            BigDecimal currentAverage = movingAverages.get(i - 1).subtract(excludedValue).add(addedValue);
            movingAverages.add(currentAverage);
        }

        return movingAverages;
    }

    // endregion

    // region getLinearWeightedMovingAverages

    /**
     * Calculates linear weighted moving averages of given {@code elements} by given {@code window}
     *
     * @param elements       elements containing values, for which averages are calculated for
     * @param valueExtractor function to get value from current element
     * @param window         count of values, used for calculation of each average, must be positive
     * @return list of calculated averages
     */
    public static <T> List<BigDecimal> getLinearWeightedMovingAverages(
            List<T> elements,
            Function<T, BigDecimal> valueExtractor,
            int window
    ) {
        List<BigDecimal> values = elements.stream().map(valueExtractor).collect(Collectors.toList());
        return getLinearWeightedMovingAverages(values, window);
    }

    /**
     * Calculates linear weighted moving averages of given {@code values} by given {@code window}
     *
     * @param values values, for which averages are calculated for
     * @param window count of values, used for calculation of each average, must be positive
     * @return list of calculated averages
     */
    public static List<BigDecimal> getLinearWeightedMovingAverages(List<BigDecimal> values, int window) {
        Assert.isTrue(window > 0, "window must be positive");

        List<BigDecimal> weightedMovingAverages = new ArrayList<>(values.size());
        for (int i = 0; i < values.size(); i++) {
            weightedMovingAverages.add(getLinearWeightedMovingAverage(values, i, window));
        }

        return weightedMovingAverages;
    }

    private static BigDecimal getLinearWeightedMovingAverage(
            List<BigDecimal> values,
            final int index,
            final int window
    ) {
        final int maxPeriod = index + 1;
        final int normalizedPeriod = Math.min(window, maxPeriod);
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

    // endregion

    // region getExponentialWeightedMovingAverages

    /**
     * Calculates exponential weighted moving averages of given {@code elements} with given {@code weightDecrease}
     *
     * @param elements       elements containing values, for which averages are calculated for
     * @param valueExtractor function to get value from current element
     * @param weightDecrease degree of weighting decrease, a constant smoothing factor. Must be in range (0; 1]
     * @return list of calculated averages
     */
    public static <T> List<BigDecimal> getExponentialWeightedMovingAverages(
            List<T> elements,
            Function<T, BigDecimal> valueExtractor,
            double weightDecrease
    ) {
        List<BigDecimal> values = elements.stream().map(valueExtractor).collect(Collectors.toList());
        return getExponentialWeightedMovingAverages(values, weightDecrease, 1);
    }

    /**
     * Calculates exponential weighted moving averages of given {@code values} with given {@code weightDecrease}
     *
     * @param values         values, for which averages are calculated for
     * @param weightDecrease degree of weighting decrease, a constant smoothing factor. Must be in range (0; 1]
     * @return list of calculated averages
     */
    public static List<BigDecimal> getExponentialWeightedMovingAverages(
            List<BigDecimal> values,
            double weightDecrease
    ) {
        return getExponentialWeightedMovingAverages(values, weightDecrease, 1);
    }

    /**
     * Calculates exponential weighted moving averages of given {@code elements} with given {@code weightDecrease}
     * and {@code order}
     *
     * @param elements       elements containing values, for which averages are calculated for
     * @param valueExtractor function to get value from current element
     * @param weightDecrease degree of weighting decrease, a constant smoothing factor. Must be in range (0; 1]
     * @param order          order of calculated averages. Must be positive.
     * @return list of calculated averages
     */
    public static <T> List<BigDecimal> getExponentialWeightedMovingAverages(
            List<T> elements,
            Function<T, BigDecimal> valueExtractor,
            double weightDecrease,
            int order
    ) {
        List<BigDecimal> values = elements.stream().map(valueExtractor).collect(Collectors.toList());
        return getExponentialWeightedMovingAverages(values, weightDecrease, order);
    }

    /**
     * Calculates exponential weighted moving averages of given {@code values} with given {@code weightDecrease}
     * and {@code order}
     *
     * @param values         values containing values, for which averages are calculated for
     * @param weightDecrease degree of weighting decrease, a constant smoothing factor. Must be in range (0; 1]
     * @param order          order of calculated averages. Must be positive.
     * @return list of calculated averages
     */
    public static List<BigDecimal> getExponentialWeightedMovingAverages(
            List<BigDecimal> values,
            double weightDecrease,
            int order
    ) {
        Assert.isTrue(order > 0, "order must be positive");
        Assert.isTrue(weightDecrease > 0 && weightDecrease <= 1,
                "weightDecrease must be in range (0; 1]");

        double revertedWeightDecrease = 1 - weightDecrease;
        List<BigDecimal> averagesNatural = new ArrayList<>(values);
        if (averagesNatural.isEmpty()) {
            return averagesNatural;
        }
        List<BigDecimal> averagesReverse = new ArrayList<>(values);

        for (int i = 0; i < order; i++) {
            updateExponentialMovingAveragesNatural(averagesNatural, weightDecrease, revertedWeightDecrease);
            updateExponentialMovingAveragesReverse(averagesReverse, weightDecrease, revertedWeightDecrease);
        }

        return getAverageValues(averagesNatural, averagesReverse);
    }

    private static void updateExponentialMovingAveragesNatural(
            List<BigDecimal> averages, double weightDecrease, double revertedWeightDecrease
    ) {
        BigDecimal average = averages.get(0);
        final int size = averages.size();
        for (int i = 1; i < size; i++) {
            average = updateExponentialMovingAverage(averages, average, i, weightDecrease, revertedWeightDecrease);
        }
    }

    private static void updateExponentialMovingAveragesReverse(
            List<BigDecimal> averages, double weightDecrease, double revertedWeightDecrease
    ) {
        final int size = averages.size();
        BigDecimal average = averages.get(size - 1);
        for (int i = size - 2; i >= 0; i--) {
            average = updateExponentialMovingAverage(averages, average, i, weightDecrease, revertedWeightDecrease);
        }
    }

    private static BigDecimal updateExponentialMovingAverage(List<BigDecimal> averages,
                                                             BigDecimal average,
                                                             int index,
                                                             double weightDecrease,
                                                             double revertedWeightDecrease) {
        average = DecimalUtils.multiply(averages.get(index), weightDecrease)
                .add(DecimalUtils.multiply(average, revertedWeightDecrease));
        averages.set(index, average);
        return average;
    }

    private static List<BigDecimal> getAverageValues(List<BigDecimal> values1, List<BigDecimal> values2) {
        List<BigDecimal> values = new ArrayList<>(values1.size());
        for (int i = 0; i < values1.size(); i++) {
            values.add(DecimalUtils.divide(values1.get(i).add(values2.get(i)), 2));
        }
        return values;
    }

    // endregion

    // region getLocalExtremes

    /**
     * Calculates indices of local extremes.
     * If several consecutive elements are equal, then the last one is considered the extremum
     *
     * @param elements       extended elements among which values extremes are sought for
     * @param valueExtractor function to get elements to compare from element
     * @param comparator     comparator of elements, defining character of extremes
     * @return calculated extremes
     */
    public static <T> List<Integer> getLocalExtremes(
            List<T> elements,
            Function<T, BigDecimal> valueExtractor,
            Comparator<BigDecimal> comparator
    ) {
        List<BigDecimal> values = elements.stream().map(valueExtractor).collect(Collectors.toList());
        return getLocalExtremes(values, comparator);
    }

    /**
     * Calculates indices of local extremes.
     * If several consecutive elements are equal, then the last one is considered the extremum
     *
     * @param values     extended values among which extremes are sought for
     * @param comparator comparator of elements, defining character of extremes
     * @return calculated extremes
     */
    public static List<Integer> getLocalExtremes(List<BigDecimal> values, Comparator<BigDecimal> comparator) {
        List<Integer> extremes = new ArrayList<>(values.size());
        if (values.isEmpty()) {
            return extremes;
        }

        boolean isGrowing = true;
        BigDecimal previousValue = values.get(0);
        for (int i = 0; i < values.size(); i++) {
            BigDecimal currentValue = values.get(i);
            if (comparator.compare(currentValue, previousValue) >= 0) {
                isGrowing = true;
            } else if (isGrowing) {
                extremes.add(i - 1);
                isGrowing = false;
            }
            previousValue = currentValue;
        }

        if (isGrowing) {
            extremes.add(values.size() - 1);
        }

        return extremes;
    }

    // endregion

}