package ru.obukhov.trader.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
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
     * Calculates simple moving averages of values of given {@code elements}.<br/>
     * Each average (except first and last {@code <window>} averages) is
     * arithmetic average of {@code <window>} previous values,
     * corresponding value and {@code <window>} next values.
     * <p>
     * Window for each of the first and the last {@code <window>} averages is
     * particular and equal to distance to nearest side of {@code elements} list.
     *
     * @param elements       elements containing values, for which averages are calculated for
     * @param valueExtractor function to get value from current element
     * @param window         count of values at both sides from each value, used for calculation of average.
     *                       Must be positive.
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
     * Calculates simple moving averages of given {@code values}.<br/>
     * Each average (except first and last {@code <window>} averages) is
     * arithmetic average of {@code <window>} previous values,
     * corresponding value and {@code <window>} next values.
     * <p>
     * Window for each of the first and the last {@code <window>} averages is
     * particular and equal to distance to nearest side of {@code values} list.
     *
     * @param values values, for which averages are calculated for
     * @param window count of values at both sides from each value, used for calculation of average.
     *               Must be positive.
     * @return list of calculated averages
     */
    public static List<BigDecimal> getSimpleMovingAverages(List<BigDecimal> values, int window) {
        Assert.isTrue(window > 0, "window must be positive");

        final int size = values.size();
        List<BigDecimal> averages = new ArrayList<>(size);

        if (size == 0) {
            return averages;
        }

        final int lastIndex = size - 1;
        final int actualWindow = Math.min(window, lastIndex / 2);

        // calculation of first {window} averages
        for (int i = 0; i < actualWindow; i++) {
            int length = Math.min(i * 2 + 1, size);
            BigDecimal sum = sumValues(values, 0, length);
            averages.add(DecimalUtils.divide(sum, length));
        }

        // calculation of the middle averages
        int commonLength = 2 * actualWindow + 1;
        int to = size - actualWindow;
        for (int i = actualWindow; i < to; i++) {
            BigDecimal sum = sumValues(values, i - actualWindow, i + actualWindow + 1);
            averages.add(DecimalUtils.divide(sum, commonLength));
        }

        // calculation of last {window} averages
        int from = size - actualWindow;
        for (int i = from; i < size; i++) {
            BigDecimal sum = sumValues(values, i * 2 - lastIndex, size);
            int length = (size - i) * 2 - 1;
            averages.add(DecimalUtils.divide(sum, length));
        }

        return averages;
    }

    private static BigDecimal sumValues(List<BigDecimal> values, int leftIndex, int rightIndex) {
        BigDecimal sum = BigDecimal.ZERO;
        for (int j = leftIndex; j < rightIndex; j++) {
            sum = sum.add(values.get(j));
        }
        return sum;
    }

    // endregion

    // region getLinearWeightedMovingAverages

    /**
     * Calculates linear weighted moving averages of values of given {@code elements} by given {@code window}
     * Each average (except first and last {@code <window>} averages) is
     * weighted average of {@code <window>} previous values,
     * corresponding value and {@code <window>} next values.
     * Weights are decreasing linearly beginning from {@code window} to 1.
     *
     * @param elements       elements containing values, for which averages are calculated for
     * @param valueExtractor function to get value from current element
     * @param window         count of values at both sides from each value, used for calculation of average.
     *                       Must be positive.
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
     * Each average (except first and last {@code <window>} averages) is
     * weighted average of {@code <window>} previous values,
     * corresponding value and {@code <window>} next values.
     * Weights are decreasing linearly beginning from {@code window} to 1.
     *
     * @param values values, for which averages are calculated for
     * @param window count of values at both sides from each value, used for calculation of average.
     *               Must be positive.
     * @return list of calculated averages
     */
    public static List<BigDecimal> getLinearWeightedMovingAverages(List<BigDecimal> values, int window) {
        Assert.isTrue(window > 0, "window must be positive");

        final int size = values.size();
        List<BigDecimal> weightedMovingAverages = new ArrayList<>(size);

        // calculation of the first {window} averages
        int index;
        int weight;
        double divisor;
        final int actualWindow = Math.min(window, (size - 1) / 2);
        for (index = 0; index < actualWindow; index++) {
            weight = index + 1;
            BigDecimal sum = getLinearWeightedSum(values, index, index, weight);
            divisor = (double) weight * weight;
            weightedMovingAverages.add(DecimalUtils.divide(sum, divisor));
        }

        // calculation of the middle averages
        weight = actualWindow + 1;
        divisor = (double) weight * weight;
        final int count = size - actualWindow;
        for (; index < count; index++) {
            BigDecimal sum = getLinearWeightedSum(values, index, actualWindow, weight);
            weightedMovingAverages.add(DecimalUtils.divide(sum, divisor));
        }

        // calculation of the last {window} averages
        for (; index < size; index++) {
            weight = size - index;
            BigDecimal sum = getLinearWeightedSum(values, index, weight - 1, weight);
            divisor = (double) weight * weight;
            weightedMovingAverages.add(DecimalUtils.divide(sum, divisor));
        }

        return weightedMovingAverages;
    }

    private static BigDecimal getLinearWeightedSum(List<BigDecimal> values, int index, int window, int weight) {
        BigDecimal sum = DecimalUtils.multiply(values.get(index), weight);
        int currentWeight = weight;

        for (int i = 1; i <= window; i++) {
            currentWeight--;
            sum = sum.add(DecimalUtils.multiply(values.get(index - i).add(values.get(index + i)), currentWeight));
        }

        return sum;
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
        int size = values.size();
        List<Integer> extremes = new ArrayList<>(size);
        if (values.isEmpty()) {
            return extremes;
        }

        boolean isGrowing = true;
        BigDecimal previousValue = values.get(0);
        for (int i = 0; i < size; i++) {
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
            extremes.add(size - 1);
        }

        return extremes;
    }

    // endregion

    // region getSortedLocalExtremes

    /**
     * Calculates indices of local extremes.
     * If several consecutive elements are equal, then the last one is considered the extremum
     *
     * @param elements       extended elements among which values extremes are sought for
     * @param valueExtractor function to get elements to compare from element
     * @param comparator     comparator of elements, defining character of extremes
     * @return calculated extremes in order, opposite to {@code comparator} order
     */
    public static <T> List<Integer> getSortedLocalExtremes(
            List<T> elements,
            Function<T, BigDecimal> valueExtractor,
            Comparator<BigDecimal> comparator
    ) {
        List<BigDecimal> values = elements.stream().map(valueExtractor).collect(Collectors.toList());
        return getSortedLocalExtremes(values, comparator);
    }

    /**
     * Calculates indices of local extremes.
     * If several consecutive elements are equal, then the last one is considered the extremum
     *
     * @param values     extended values among which extremes are sought for
     * @param comparator comparator of elements, defining character of extremes
     * @return calculated extremes in order, opposite to {@code comparator} order
     */
    public static List<Integer> getSortedLocalExtremes(List<BigDecimal> values, Comparator<BigDecimal> comparator) {
        if (values.isEmpty()) {
            return new ArrayList<>();
        }

        int size = values.size();
        List<Pair<Integer, BigDecimal>> extremes = new ArrayList<>(size);

        boolean isGrowing = true;
        BigDecimal previousValue = values.get(0);
        for (int i = 0; i < size; i++) {
            BigDecimal currentValue = values.get(i);
            if (comparator.compare(currentValue, previousValue) >= 0) {
                isGrowing = true;
            } else if (isGrowing) {
                extremes.add(Pair.of(i - 1, previousValue));
                isGrowing = false;
            }
            previousValue = currentValue;
        }

        if (isGrowing) {
            extremes.add(Pair.of(size - 1, previousValue));
        }

        return extremes.stream()
                .sorted(Comparator.comparing(Pair::getRight, comparator.reversed()))
                .map(Pair::getLeft)
                .collect(Collectors.toList());
    }

    // endregion

}