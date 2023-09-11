package ru.obukhov.trader.common.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.Assert;
import ru.obukhov.trader.common.model.Line;
import ru.obukhov.trader.common.model.Point;
import ru.obukhov.trader.trading.model.Crossover;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Class with util methods to transform or analyse trends
 */
@UtilityClass
public class TrendUtils {

    /**
     * Factor, that representing the duration of the restriction line
     * in compare to distance between extremes used to build this line
     */
    private static final double RESTRAINT_DURATION_FACTOR = 2.0;

    // region local extremes

    /**
     * Calculates indices of local extremes.
     * If several consecutive elements are equal, then the last one is considered the extremum
     *
     * @param values     extended values among which extremes are sought for
     * @param comparator comparator of elements, defining character of extremes
     * @return calculated extremes
     */
    public static List<Integer> getLocalExtremes(final List<BigDecimal> values, final Comparator<BigDecimal> comparator) {
        final int size = values.size();
        final List<Integer> extremes = new ArrayList<>(size);
        if (values.isEmpty()) {
            return extremes;
        }

        boolean isGrowing = true;
        BigDecimal previousValue = values.get(0);
        for (int i = 0; i < size; i++) {
            final BigDecimal currentValue = values.get(i);
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

    public static List<Point> getLocalExtremes(
            final List<BigDecimal> values,
            final List<OffsetDateTime> times,
            final List<Integer> localExtremesIndices
    ) {
        return localExtremesIndices.stream()
                .map(extremum -> Point.of(times.get(extremum), values.get(extremum)))
                .toList();
    }

    /**
     * Calculates indices of local extremes.
     * If several consecutive elements are equal, then the last one is considered the extremum
     *
     * @param values     extended values among which extremes are sought for
     * @param comparator comparator of elements, defining character of extremes
     * @return calculated extremes in order, opposite to {@code comparator} order
     */
    public static List<Integer> getSortedLocalExtremes(final List<BigDecimal> values, final Comparator<BigDecimal> comparator) {
        if (values.isEmpty()) {
            return new ArrayList<>();
        }

        final int size = values.size();
        final List<Pair<Integer, BigDecimal>> extremes = new ArrayList<>(size);

        boolean isGrowing = true;
        BigDecimal previousValue = values.get(0);
        for (int i = 0; i < size; i++) {
            final BigDecimal currentValue = values.get(i);
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
                .toList();
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
            final List<OffsetDateTime> times,
            final List<BigDecimal> values,
            final List<Integer> localExtremes
    ) {
        Assert.isTrue(times.size() == values.size(), "times and values must have same size");
        Assert.isTrue(times.size() >= localExtremes.size(),
                "localExtremes can't be longer than times and values");

        final List<List<Point>> lines = new ArrayList<>();
        if (localExtremes.isEmpty()) {
            return lines;
        }

        Integer currentExtremum;
        Integer nextExtremum = localExtremes.get(0);
        final int count = localExtremes.size() - 1;
        for (int i = 0; i < count; i++) {
            currentExtremum = nextExtremum;
            nextExtremum = localExtremes.get(i + 1);
            final Line line = getLine(values, currentExtremum, nextExtremum);
            final List<Point> points = getPoints(times, currentExtremum, nextExtremum, line);
            lines.add(points);
        }
        return lines;
    }

    private static Line getLine(final List<BigDecimal> values, final Integer x1, final Integer x2) {
        final BigDecimal y1 = values.get(x1);
        final BigDecimal y2 = values.get(x2);

        return new Line(x1, y1, x2, y2);
    }

    private static List<Point> getPoints(
            final List<OffsetDateTime> times,
            final Integer x1,
            final Integer x2,
            final Line line
    ) {
        final List<Point> points = new ArrayList<>();
        final int futureX = Math.min(times.size() - 1, x1 + (int) ((x2 - x1) * RESTRAINT_DURATION_FACTOR));
        for (int x = x1; x <= futureX; x++) {
            final OffsetDateTime time = times.get(x);
            final BigDecimal value = line.getValue(x);
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
    public static List<Integer> getCrossovers(final List<BigDecimal> values1, final List<BigDecimal> values2) {
        final int size = values1.size();
        Assert.isTrue(size == values2.size(), "values1 and values2 must have same size");

        final List<Integer> crossovers = new ArrayList<>();
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

    private static int getFirstDifferenceIndex(final List<BigDecimal> values1, final List<BigDecimal> values2) {
        for (int i = 0; i < values1.size(); i++) {
            if (values1.get(i).compareTo(values2.get(i)) != 0) {
                return i;
            }
        }

        return -1;
    }

    private static boolean isCrossover(final int previousDifference, final int currentDifference) {
        return previousDifference != currentDifference && currentDifference != 0;
    }

    // endregion

    /**
     * @return - {@link Crossover#NONE} if not enough values to calculate crossovers or if there is no crossover<br/>
     * - {@link Crossover#ABOVE} if {@code values1} crosses {@code values2} from below at given {@code index} and
     * there are no crossovers or touches after {@code index}<br/>
     * - {@link Crossover#BELOW} if {@code values1} crosses {@code values2} from above at given {@code index} and
     * there are no crossovers or touches after {@code index}<br/>
     * @throws IllegalArgumentException if {@code values1} and {@code values2} have different sizes
     */
    public static Crossover getCrossoverIfLast(final List<BigDecimal> values1, final List<BigDecimal> values2, final int index) {
        final int size = values1.size();

        Assert.isTrue(size == values2.size(), "Collections must has same size");

        if (size < 2) {
            return Crossover.NONE;
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
                return Crossover.ABOVE;
            }
        } else if (
                previousComparisonResult < 0 && currentComparisonResult > 0 &&
                        relationIsKept(values1, values2, index + 1, currentComparisonResult)
        ) {
            return Crossover.BELOW;
        }

        return Crossover.NONE;
    }

    private static boolean relationIsKept(
            final List<BigDecimal> values1,
            final List<BigDecimal> values2,
            final int index,
            final int relation
    ) {
        for (int i = index; i < values1.size(); i++) {
            if (values1.get(i).compareTo(values2.get(i)) != relation) {
                return false;
            }
        }

        return true;
    }

}