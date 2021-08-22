package ru.obukhov.trader.common.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.common.model.Point;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.trading.model.Crossover;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

class TrendUtilsUnitTest {

    // region getLocalExtremes tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetLocalExtremes() {
        return Stream.of(
                Arguments.of(
                        List.of(),
                        List.of(),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        List.of(100.0),
                        List.of(0),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        List.of(100.0, 90.0),
                        List.of(0),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        List.of(100.0, 100.0),
                        List.of(1),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        List.of(90.0, 100.0),
                        List.of(1),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        List.of(100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0),
                        List.of(9),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        List.of(10.0, 30.0, 30.0, 29.9, 20.0, 25.0, 21.0, 15.0, 50.0, 30.0),
                        List.of(2, 5, 8),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        List.of(10.0, 30.0, 30.0, 29.9, 20.0, 25.0, 21.0, 50.0, 50.0, 30.0),
                        List.of(2, 5, 8),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        List.of(10.0, 30.0, 30.0, 29.9, 20.0, 25.0, 21.0, 50.0, 50.0, 50.0),
                        List.of(2, 5, 9),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        List.of(100.0, 30.0, 30.0, 29.9, 20.0, 25.0, 21.0, 15.0, 50.0, 50.1),
                        List.of(0, 2, 5, 9),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        List.of(10.0, 5.0, 5.0, 5.1, 4.0, 3.5, 5.0, 70.0, 50.0, 80.0),
                        List.of(0, 3, 7, 9),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        List.of(10.0, 5.0, 5.0, 5.1, 4.0, 3.5, 5.0, 70.0, 50.0, 80.0),
                        List.of(2, 5, 8),
                        Comparator.reverseOrder()
                ),
                Arguments.of(
                        List.of(20.0, 15.0, 15.0, 15.1, 20.0, 19.0, 21.0, 10.0, 10.0, 30.0),
                        List.of(2, 5, 8),
                        Comparator.reverseOrder()
                ),
                Arguments.of(
                        List.of(20.0, 15.0, 15.0, 15.1, 20.0, 19.0, 21.0, 10.0, 10.0, 10.0),
                        List.of(2, 5, 9),
                        Comparator.reverseOrder()
                ),
                Arguments.of(
                        List.of(10.0, 30.0, 30.0, 30.1, 20.0, 19.0, 21.0, 60.0, 50.0, 49.9),
                        List.of(0, 2, 5, 9),
                        Comparator.reverseOrder()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetLocalExtremes")
    void getLocalExtremes(final List<Double> values, final List<Integer> expectedExtremes, final Comparator<BigDecimal> comparator) {
        final List<BigDecimal> bigDecimalValues = TestData.createBigDecimalsList(values);

        final List<Integer> extremes = TrendUtils.getLocalExtremes(bigDecimalValues, comparator);

        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    // endregion

    // region getLocalExtremes tests

    @Test
    void getLocalExtremes() {
        final List<BigDecimal> values = TestData.createBigDecimalsList(10, 20, 15, 30);
        final OffsetDateTime now = OffsetDateTime.now();
        final List<OffsetDateTime> times = List.of(now, now.plusMinutes(1), now.plusMinutes(2), now.plusMinutes(2));
        final List<Integer> localExtremesIndices = List.of(0, 2);

        final List<Point> localExtremes = TrendUtils.getLocalExtremes(values, times, localExtremesIndices);

        final List<Point> expectedLocalExtremes = List.of(
                Point.of(times.get(0), values.get(0)),
                Point.of(times.get(2), values.get(2))
        );

        AssertUtils.assertListsAreEqual(expectedLocalExtremes, localExtremes);
    }

    // endregion

    // region getSortedLocalExtremes tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetSortedLocalExtremes() {
        return Stream.of(
                Arguments.of(
                        List.of(),
                        List.of(),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        List.of(100.0),
                        List.of(0),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        List.of(100.0, 90.0),
                        List.of(0),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        List.of(100.0, 100.0),
                        List.of(1),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        List.of(90.0, 100.0),
                        List.of(1),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        List.of(100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0),
                        List.of(9),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        List.of(10.0, 30.0, 30.0, 29.9, 20.0, 25.0, 21.0, 15.0, 50.0, 30.0),
                        List.of(8, 2, 5),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        List.of(10.0, 30.0, 30.0, 29.9, 20.0, 25.0, 21.0, 50.0, 50.0, 30.0),
                        List.of(8, 2, 5),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        List.of(10.0, 30.0, 30.0, 29.9, 20.0, 25.0, 21.0, 50.0, 50.0, 50.0),
                        List.of(9, 2, 5),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        List.of(100.0, 30.0, 30.0, 29.9, 20.0, 25.0, 21.0, 15.0, 50.0, 50.1),
                        List.of(0, 9, 2, 5),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        List.of(10.0, 5.0, 5.0, 5.1, 4.0, 3.5, 5.0, 70.0, 50.0, 80.0),
                        List.of(9, 7, 0, 3),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        List.of(10.0, 5.0, 5.0, 5.1, 4.0, 3.5, 5.0, 70.0, 50.0, 80.0),
                        List.of(5, 2, 8),
                        Comparator.reverseOrder()
                ),
                Arguments.of(
                        List.of(20.0, 15.0, 15.0, 15.1, 20.0, 19.0, 21.0, 10.0, 10.0, 30.0),
                        List.of(8, 2, 5),
                        Comparator.reverseOrder()
                ),
                Arguments.of(
                        List.of(20.0, 15.0, 15.0, 15.1, 20.0, 19.0, 21.0, 10.0, 10.0, 10.0),
                        List.of(9, 2, 5),
                        Comparator.reverseOrder()
                ),
                Arguments.of(
                        List.of(10.0, 30.0, 30.0, 30.1, 20.0, 19.0, 21.0, 60.0, 50.0, 49.9),
                        List.of(0, 5, 2, 9),
                        Comparator.reverseOrder()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetSortedLocalExtremes")
    void getSortedLocalExtremes(final List<Double> values, final List<Integer> expectedExtremes, final Comparator<BigDecimal> comparator) {
        final List<BigDecimal> bigDecimalValues = TestData.createBigDecimalsList(values);

        final List<Integer> extremes = TrendUtils.getSortedLocalExtremes(bigDecimalValues, comparator);

        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    // endregion

    // region getRestraintLines tests

    @Test
    void getRestraintLines_throwsIllegalArgumentException_whenTimesIsLongerThanValues() {
        final OffsetDateTime startTime = OffsetDateTime.now();
        final List<OffsetDateTime> times = List.of(startTime, startTime.plusMinutes(1));
        final List<BigDecimal> values = TestData.createBigDecimalsList(10.0);
        final List<Integer> localExtremes = List.of(0, 1);

        final Executable executable = () -> TrendUtils.getRestraintLines(times, values, localExtremes);
        Assertions.assertThrows(IllegalArgumentException.class, executable, "times and values must have same size");
    }

    @Test
    void getRestraintLines_throwsIllegalArgumentException_whenValuesIsLongerThanTimes() {
        final List<OffsetDateTime> times = List.of(OffsetDateTime.now());
        final List<BigDecimal> values = TestData.createBigDecimalsList(10.0, 11.0);
        final List<Integer> localExtremes = List.of(0, 1);

        final Executable executable = () -> TrendUtils.getRestraintLines(times, values, localExtremes);
        Assertions.assertThrows(IllegalArgumentException.class, executable, "times and values must have same size");
    }

    @Test
    void getRestraintLines_throwsIllegalArgumentException_whenLocalExtremesIsLongerThanTimes() {
        final List<OffsetDateTime> times = List.of(OffsetDateTime.now());
        final List<BigDecimal> values = TestData.createBigDecimalsList(10.0);
        final List<Integer> localExtremes = List.of(0, 1);

        final Executable executable = () -> TrendUtils.getRestraintLines(times, values, localExtremes);
        final String expectedMessage = "localExtremes can't be longer than times and values";
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
    }

    @Test
    void getRestraintLines_returnsEmptyList_whenLocalExtremesIsEmpty() {
        final OffsetDateTime startTime = OffsetDateTime.now();
        final List<OffsetDateTime> times = List.of(startTime, startTime.plusMinutes(1));
        final List<BigDecimal> values = TestData.createBigDecimalsList(10.0, 11.0);
        final List<Integer> localExtremes = List.of();

        final List<List<Point>> restraintLines = TrendUtils.getRestraintLines(times, values, localExtremes);

        Assertions.assertTrue(restraintLines.isEmpty());
    }

    @Test
    void getRestraintLines_returnsEmptyList_whenThereIsSingleLocalExtremum() {
        final OffsetDateTime startTime = OffsetDateTime.now();
        final List<OffsetDateTime> times = List.of(startTime, startTime.plusMinutes(1));
        final List<BigDecimal> values = TestData.createBigDecimalsList(10.0, 11.0);
        final List<Integer> localExtremes = List.of(0);

        final List<List<Point>> restraintLines = TrendUtils.getRestraintLines(times, values, localExtremes);

        Assertions.assertTrue(restraintLines.isEmpty());
    }

    @Test
    void getRestraintLines_returnsLines() {
        final OffsetDateTime startTime = OffsetDateTime.now();
        final List<OffsetDateTime> times = List.of(
                startTime,
                startTime.plusMinutes(1),
                startTime.plusMinutes(2),
                startTime.plusMinutes(3),
                startTime.plusMinutes(4),
                startTime.plusMinutes(5),
                startTime.plusMinutes(6),
                startTime.plusMinutes(7),
                startTime.plusMinutes(8),
                startTime.plusMinutes(9)
        );
        final List<BigDecimal> values = TestData.createBigDecimalsList(10.0, 15.0, 14.0, 11.0, 12.0, 13.0, 14.0, 14.0, 12.0, 16.0);
        final List<Integer> localExtremes = List.of(0, 3, 8);

        final List<List<Point>> restraintLines = TrendUtils.getRestraintLines(times, values, localExtremes);

        Assertions.assertEquals(2, restraintLines.size());
        final List<Point> expectedRestraintLine1 = List.of(
                Point.of(times.get(0), 10.00000),
                Point.of(times.get(1), 10.33333),
                Point.of(times.get(2), 10.66667),
                Point.of(times.get(3), 11.00000),
                Point.of(times.get(4), 11.33333),
                Point.of(times.get(5), 11.66667),
                Point.of(times.get(6), 12.00000)
        );
        AssertUtils.assertListsAreEqual(expectedRestraintLine1, restraintLines.get(0));

        final List<Point> expectedRestraintLine2 = List.of(
                Point.of(times.get(3), 11.0),
                Point.of(times.get(4), 11.2),
                Point.of(times.get(5), 11.4),
                Point.of(times.get(6), 11.6),
                Point.of(times.get(7), 11.8),
                Point.of(times.get(8), 12.0),
                Point.of(times.get(9), 12.2)
        );
        AssertUtils.assertListsAreEqual(expectedRestraintLine2, restraintLines.get(1));
    }

    // endregion

    // region getCrossovers tests

    @Test
    void getCrossovers_throwIllegalArgumentException_whenArgumentsHaveDifferentSizes() {
        final List<BigDecimal> values1 = TestData.createBigDecimalsList(10.0, 20.0);
        final List<BigDecimal> values2 = TestData.createBigDecimalsList(10.0);

        final Executable executable = () -> TrendUtils.getCrossovers(values1, values2);
        Assertions.assertThrows(IllegalArgumentException.class, executable, "values1 and values2 must have same size");
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetCrossovers() {
        return Stream.of(
                Arguments.of(
                        List.of(),
                        List.of(),
                        List.of()
                ),
                Arguments.of(
                        TestData.createBigDecimalsList(10, 20, 30),
                        TestData.createBigDecimalsList(10, 20, 30),
                        List.of()
                ),
                Arguments.of(
                        TestData.createBigDecimalsList(10, 20, 30),
                        TestData.createBigDecimalsList(10, 20, 31),
                        List.of()
                ),
                Arguments.of(
                        TestData.createBigDecimalsList(10, 12, 13),
                        TestData.createBigDecimalsList(11, 10, 11),
                        List.of(1)
                ),
                Arguments.of(
                        TestData.createBigDecimalsList(10, 11, 12, 14, 15),
                        TestData.createBigDecimalsList(10, 12, 13, 12, 11),
                        List.of(3)
                ),
                Arguments.of(
                        TestData.createBigDecimalsList(10, 11, 12, 14, 15),
                        TestData.createBigDecimalsList(11, 12, 13, 12, 11),
                        List.of(3)
                ),
                Arguments.of(
                        TestData.createBigDecimalsList(10, 11, 12, 14, 15, 15, 16, 16),
                        TestData.createBigDecimalsList(11, 12, 13, 12, 11, 11, 11, 20),
                        List.of(3, 7)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetCrossovers")
    void getCrossovers(final List<BigDecimal> values1, final List<BigDecimal> values2, final List<Integer> expectedCrossovers) {
        final List<Integer> crossovers = TrendUtils.getCrossovers(values1, values2);

        AssertUtils.assertListsAreEqual(expectedCrossovers, crossovers);
    }

    @Test
    void getCrossovers_commonAssertions_forRandomValues() {
        final List<BigDecimal> values1 = TestData.createRandomBigDecimalsList(1000);
        final List<BigDecimal> values2 = TestData.createRandomBigDecimalsList(1000);

        final List<Integer> crossovers = TrendUtils.getCrossovers(values1, values2);

        if (!crossovers.isEmpty()) {
            if (crossovers.get(0) <= 0) {
                final String message = String.format(
                        "First crossover is %s for [%s] and [%s]",
                        crossovers.get(0),
                        StringUtils.join(values1, ", "),
                        StringUtils.join(values2, ", ")
                );
                Assertions.fail(message);
            }
        }

        for (int i = 0; i < crossovers.size() - 1; i++) {
            final int currentCrossover = crossovers.get(i);
            final int nextCrossover = crossovers.get(i + 1);
            if (currentCrossover >= nextCrossover) {
                final String message = String.format(
                        "Not ascending crossovers for [%s] and [%s]",
                        StringUtils.join(values1, ", "),
                        StringUtils.join(values2, ", ")
                );
                Assertions.fail(message);
            }
        }
    }

    // endregion

    // region getCrossoverIfLast tests

    @Test
    void getCrossoverIfLast_throwIllegalArgumentException_whenDifferentSizes() {
        final List<BigDecimal> values1 = TestData.createRandomBigDecimalsList(10);
        final List<BigDecimal> values2 = TestData.createRandomBigDecimalsList(9);
        final int index = 2;

        final Executable executable = () -> TrendUtils.getCrossoverIfLast(values1, values2, index);
        Assertions.assertThrows(IllegalArgumentException.class, executable, "Collections must has same size");
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetCrossoverIfLast() {
        return Stream.of(
                Arguments.of(
                        List.of(),
                        List.of(),
                        2,
                        Crossover.NONE
                ),
                Arguments.of(
                        List.of(4.0),
                        List.of(3.0),
                        2,
                        Crossover.NONE
                ),
                // crossover from above and it is last
                Arguments.of(
                        List.of(1.0, 2.0, 3.0, 4.0, 5.0),
                        List.of(0.0, 1.0, 4.0, 5.0, 7.0),
                        2,
                        Crossover.ABOVE
                ),
                // crossover from below and it is not last because of another crossover
                Arguments.of(
                        List.of(1.0, 2.0, 3.0, 4.0, 5.0),
                        List.of(0.0, 1.0, 4.0, 5.0, 3.0),
                        2,
                        Crossover.NONE
                ),
                // crossover from below and it is not last because of touch
                Arguments.of(
                        List.of(1.0, 2.0, 3.0, 4.0, 5.0),
                        List.of(0.0, 1.0, 4.0, 4.0, 7.0),
                        2,
                        Crossover.NONE
                ),
                // crossover from below and it is last
                Arguments.of(
                        List.of(0.0, 1.0, 4.0, 5.0, 7.0),
                        List.of(1.0, 2.0, 3.0, 4.0, 5.0),
                        2,
                        Crossover.BELOW
                ),
                // crossover from above and it is not last because of another crossover
                Arguments.of(
                        List.of(0.0, 1.0, 4.0, 5.0, 3.0),
                        List.of(1.0, 2.0, 3.0, 4.0, 5.0),
                        2,
                        Crossover.NONE
                ),
                // crossover from above and it is not last because of touch
                Arguments.of(
                        List.of(0.0, 1.0, 4.0, 4.0, 7.0),
                        List.of(1.0, 2.0, 3.0, 4.0, 5.0),
                        2,
                        Crossover.NONE
                ),
                // no crossover because collections are equal
                Arguments.of(
                        List.of(1.0, 2.0, 3.0, 4.0, 5.0),
                        List.of(1.0, 2.0, 3.0, 4.0, 5.0),
                        2,
                        Crossover.NONE
                ),
                // no crossover because values1 is above values2
                Arguments.of(
                        List.of(1.0, 2.0, 3.0, 4.0, 5.0),
                        List.of(0.0, 1.0, 2.0, 3.0, 4.0),
                        2,
                        Crossover.NONE
                ),
                // no crossover because values1 is below values2
                Arguments.of(
                        List.of(1.0, 2.0, 3.0, 4.0, 5.0),
                        List.of(2.0, 3.0, 4.0, 5.0, 6.0),
                        2,
                        Crossover.NONE
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetCrossoverIfLast")
    void getCrossoverIfLast(List<Double> values1, List<Double> values2, int index, Crossover expectedCrossover) {
        final List<BigDecimal> bigDecimalValues1 = TestData.createBigDecimalsList(values1);
        final List<BigDecimal> bigDecimalValues2 = TestData.createBigDecimalsList(values2);

        final Crossover crossover = TrendUtils.getCrossoverIfLast(bigDecimalValues1, bigDecimalValues2, index);

        Assertions.assertEquals(expectedCrossover, crossover);
    }

    // endregion

}