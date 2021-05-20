package ru.obukhov.trader.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.model.Line;
import ru.obukhov.trader.common.model.Point;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class with util methods to transform or analyse with trends
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TrendUtils {

    /**
     * Factor, that representing the duration of the restriction line
     * in compare to distance between extremes used to build this line
     */
    static final double RESTRAINT_DURATION_FACTOR = 2.0;

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

        int size = values.size();

        // filling of first {window} averages
        List<BigDecimal> movingAverages = new ArrayList<>(size);
        int count = Math.min(window, size);
        for (int i = 0; i < count; i++) {
            BigDecimal sum = BigDecimal.ZERO;
            for (int j = 0; j <= i; j++) {
                BigDecimal value = values.get(j);
                sum = sum.add(value);
            }

            movingAverages.add(DecimalUtils.divide(sum, i + 1));
        }

        // filling of the rest averages
        for (int i = window; i < size; i++) {
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
     *                       * @param order          order of calculated averages. Must be positive.
     * @return list of calculated averages
     */
    public static <T> List<BigDecimal> getLinearWeightedMovingAverages(
            List<T> elements,
            Function<T, BigDecimal> valueExtractor,
            int window,
            int order
    ) {
        List<BigDecimal> values = elements.stream().map(valueExtractor).collect(Collectors.toList());

        return getLinearWeightedMovingAverages(values, window, order);
    }

    /**
     * Calculates linear weighted moving averages of given {@code values} by given {@code window}
     *
     * @param values values, for which averages are calculated for
     * @param window count of values, used for calculation of each average, must be positive
     * @param order  order of calculated averages. Must be positive.
     * @return list of calculated averages
     */
    public static List<BigDecimal> getLinearWeightedMovingAverages(List<BigDecimal> values, int window, int order) {
        Assert.isTrue(order > 0, "order must be positive");

        List<BigDecimal> averages = new ArrayList<>(values);
        for (int i = 0; i < order; i++) {
            averages = getLinearWeightedMovingAverages(averages, window);
        }

        return averages;
    }

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
        List<BigDecimal> averages = new ArrayList<>(values);
        if (averages.isEmpty()) {
            return averages;
        }

        for (int i = 0; i < order; i++) {
            updateExponentialMovingAverages(averages, weightDecrease, revertedWeightDecrease);
        }

        return averages.stream().map(DecimalUtils::setDefaultScale).collect(Collectors.toList());
    }

    private static void updateExponentialMovingAverages(
            List<BigDecimal> averages,
            double weightDecrease,
            double revertedWeightDecrease
    ) {
        BigDecimal average = averages.get(0);
        final int size = averages.size();
        for (int i = 1; i < size; i++) {
            average = updateExponentialMovingAverage(averages, average, i, weightDecrease, revertedWeightDecrease);
        }
    }

    private static BigDecimal updateExponentialMovingAverage(
            List<BigDecimal> averages,
            BigDecimal average,
            int index,
            double weightDecrease,
            double revertedWeightDecrease
    ) {
        average = DecimalUtils.multiply(averages.get(index), weightDecrease)
                .add(DecimalUtils.multiply(average, revertedWeightDecrease));
        averages.set(index, average);
        return average;
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

    public static List<Point> getLocalExtremes(
            List<BigDecimal> values,
            List<OffsetDateTime> times,
            List<Integer> localExtremesIndices
    ) {
        return localExtremesIndices.stream()
                .map(extremum -> Point.of(times.get(extremum), values.get(extremum)))
                .collect(Collectors.toList());
    }

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

    // region getRestraintLines

    /**
     * Calculates restraint lines for every consecutive pair of local extremes.
     * Length of each line is distance between from extremes multiplicated to {@value RESTRAINT_DURATION_FACTOR}
     *
     * @param times         X values
     * @param values        Y values
     * @param localExtremes indices of local extremes of values
     * @return list of calculated lines, each line is list of points
     */
    public static List<List<Point>> getRestraintLines(
            List<OffsetDateTime> times,
            List<BigDecimal> values,
            List<Integer> localExtremes
    ) {
        Assert.isTrue(times.size() == values.size(), "times and values must have same size");
        Assert.isTrue(times.size() >= localExtremes.size(),
                "localExtremes can't be longer than times and values");

        List<List<Point>> lines = new ArrayList<>();
        if (localExtremes.isEmpty()) {
            return lines;
        }

        Integer currentExtremum;
        Integer nextExtremum = localExtremes.get(0);
        final int count = localExtremes.size() - 1;
        for (int i = 0; i < count; i++) {
            currentExtremum = nextExtremum;
            nextExtremum = localExtremes.get(i + 1);
            Line line = getLine(values, currentExtremum, nextExtremum);
            List<Point> points = getPoints(times, currentExtremum, nextExtremum, line);
            lines.add(points);
        }
        return lines;
    }

    private static Line getLine(List<BigDecimal> values, Integer x1, Integer x2) {
        BigDecimal y1 = values.get(x1);
        BigDecimal y2 = values.get(x2);

        return new Line(x1, y1, x2, y2);
    }

    private static List<Point> getPoints(List<OffsetDateTime> times, Integer x1, Integer x2, Line line) {
        List<Point> points = new ArrayList<>();
        int futureX = Math.min(times.size() - 1, x1 + (int) ((x2 - x1) * RESTRAINT_DURATION_FACTOR));
        for (int x = x1; x <= futureX; x++) {
            OffsetDateTime time = times.get(x);
            BigDecimal value = line.getValue(x);
            points.add(Point.of(time, value));
        }
        return points;
    }

    // endregion

    // region getCrossovers

    /**
     * Calculates indices of crossovers of given lists.
     * Crossover is situation, when preponderance of value from one list
     * is changed to preponderance of value from another list within step to next pair of corresponding values
     *
     * @return indices of crossovers
     */
    public static List<Integer> getCrossovers(List<BigDecimal> values1, List<BigDecimal> values2) {
        final int size = values1.size();
        Assert.isTrue(size == values2.size(), "values1 and values2 must have same size");

        List<Integer> crossovers = new ArrayList<>();
        if (values1.isEmpty()) {
            return crossovers;
        }

        int index = getFirstDifferenceIndex(values1, values2);
        if (index == -1) {
            return crossovers;
        }

        // searching for crossovers
        int previousDifference;
        int currentDifference = values1.get(index).compareTo(values2.get(index));
        for (index++; index < size; index++) {
            previousDifference = currentDifference;
            currentDifference = values1.get(index).compareTo(values2.get(index));
            if (isCrossover(previousDifference, currentDifference)) {
                crossovers.add(index);
            }
        }

        return crossovers;
    }

    private static int getFirstDifferenceIndex(List<BigDecimal> values1, List<BigDecimal> values2) {
        for (int i = 0; i < values1.size(); i++) {
            if (values1.get(i).compareTo(values2.get(i)) != 0) {
                return i;
            }
        }

        return -1;
    }

    private static boolean isCrossover(int previousDifference, int currentDifference) {
        return previousDifference != currentDifference && currentDifference != 0;
    }

    // endregion

    /**
     * @return 0 if not enough values too calculate crossovers or if there is no crossover<br/>
     * 1 if {@code values1} crosses {@code values2} from below at given {@code index} and
     * there are no crossovers or touches after {@code index}<br/>
     * -1 if {@code values1} crosses {@code values2} from above at given {@code index} and
     * there are no crossovers or touches after {@code index}<br/>
     * @throws IllegalArgumentException if {@code values1} and {@code values2} have different sizes
     */
    public static int getCrossoverIfLast(List<BigDecimal> values1, List<BigDecimal> values2, int index) {
        final int size = values1.size();

        Assert.isTrue(size == values2.size(), "Collections must has same size");

        if (size < 2) {
            return 0;
        }

        final int previousIndex1 = index - 1;

        final BigDecimal previousValue1 = values1.get(previousIndex1);
        final BigDecimal previousValue2 = values2.get(previousIndex1);

        final BigDecimal currentValue1 = values1.get(index);
        final BigDecimal currentValue2 = values2.get(index);

        final int previousComparisonResult = previousValue1.compareTo(previousValue2);
        final int currentComparisonResult = currentValue1.compareTo(currentValue2);

        if (previousComparisonResult > 0 && currentComparisonResult < 0) {
            if (relationIsKept(values1, values2, index + 1, currentComparisonResult)) {
                return -1;
            }
        } else if (
                previousComparisonResult < 0 && currentComparisonResult > 0 &&
                        relationIsKept(values1, values2, index + 1, currentComparisonResult)
        ) {
            return 1;
        }

        return 0;
    }

    private static boolean relationIsKept(
            List<BigDecimal> values1,
            List<BigDecimal> values2,
            int index,
            int relation
    ) {
        for (int i = index; i < values1.size(); i++) {
            if (values1.get(i).compareTo(values2.get(i)) != relation) {
                return false;
            }
        }

        return true;
    }

}