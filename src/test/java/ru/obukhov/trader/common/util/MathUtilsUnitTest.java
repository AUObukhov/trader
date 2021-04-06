package ru.obukhov.trader.common.util;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class MathUtilsUnitTest {

    // region getAverage with collection tests

    static Stream<Arguments> getData_forGetAverage_withCollection() {
        return Stream.of(
                Arguments.of(ImmutableList.of(), 0.0),
                Arguments.of(ImmutableList.of(1000.0), 1000.0),
                Arguments.of(ImmutableList.of(100.0, 200.0, 1000.0), 433.33333)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverage_withCollection")
    void getAverage_withCollection(List<Double> values, Double expectedAverage) {
        List<BigDecimal> bigDecimalValues = getBigDecimalValues(values);

        BigDecimal average = MathUtils.getAverage(bigDecimalValues);

        AssertUtils.assertEquals(expectedAverage, average);
    }

    // endregion

    // region getAverage with VarArgs tests

    @Test
    void getAverage_withVarArgs_returnsZero_whenNoArguments() {

        BigDecimal average = MathUtils.getAverage();

        AssertUtils.assertEquals(BigDecimal.ZERO, average);

    }

    @Test
    void getAverage_withVarArgs_returnsNumber_whenSingleArguments() {

        BigDecimal number = BigDecimal.TEN;

        BigDecimal average = MathUtils.getAverage(number);

        AssertUtils.assertEquals(number, average);

    }

    @Test
    void getAverage_withVarArgs_returnsAverage_whenMultipleNumbersInCollection() {

        BigDecimal average = MathUtils.getAverage(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(1000)
        );

        AssertUtils.assertEquals(BigDecimal.valueOf(433.33333), average);

    }

    // endregion

    // region getWeightedAverage tests

    @Test
    void getWeightedAverage_returnsZero_whenCollectionIsEmpty() {
        SortedMap<OffsetDateTime, BigDecimal> dateTimesToAmounts = new TreeMap<>();
        OffsetDateTime endTime = DateUtils.getDateTime(2021, 3, 10, 11, 12, 13);

        BigDecimal weightedAverage = MathUtils.getWeightedAverage(dateTimesToAmounts, endTime);

        AssertUtils.assertEquals(BigDecimal.ZERO, weightedAverage);
    }

    @Test
    void getWeightedAverage_returnsProperValue_whenCollectionIsNotEmpty() {
        SortedMap<OffsetDateTime, BigDecimal> dateTimesToAmounts = new TreeMap<>();
        dateTimesToAmounts.put(
                DateUtils.getDateTime(2021, 1, 1, 10, 0, 0),
                BigDecimal.valueOf(100000)
        );
        dateTimesToAmounts.put(
                DateUtils.getDateTime(2021, 2, 1, 10, 0, 0),
                BigDecimal.valueOf(110000)
        );
        dateTimesToAmounts.put(
                DateUtils.getDateTime(2021, 3, 1, 10, 0, 0),
                BigDecimal.valueOf(120000)
        );
        OffsetDateTime endTime = DateUtils.getDateTime(2021, 3, 10, 10, 0, 0);

        BigDecimal weightedAverage = MathUtils.getWeightedAverage(dateTimesToAmounts, endTime);

        AssertUtils.assertEquals(BigDecimal.valueOf(106764.70588), weightedAverage);
    }

    // endregion

    // region max tests

    @Test
    void max_returnsNull_whenValuesIsEmpty() {
        Double max = MathUtils.max(Collections.emptyList());

        Assertions.assertNull(max);
    }

    @Test
    void max_returnsMaxValue_whenValuesIsNotEmpty() {
        List<Double> values = ImmutableList.of(-100d, 21d, 10d, 20d);

        Double max = MathUtils.max(values);

        Assertions.assertEquals(max, Double.valueOf(21));
    }

    // endregion

    // region min tests

    @Test
    void min_returnsNull_whenValuesIsEmpty() {
        Double min = MathUtils.min(Collections.emptyList());

        Assertions.assertNull(min);
    }

    @Test
    void min_returnsMaxValue_whenValuesIsNotEmpty() {
        List<Double> values = ImmutableList.of(100d, -21d, 10d, 20d);

        Double min = MathUtils.min(values);

        Assertions.assertEquals(min, Double.valueOf(-21));
    }

    // endregion

    // region getSimpleMovingAverages tests

    static Stream<Arguments> getData_forGetSimpleMovingAverages_withoutValueExtractor_throwsIllegalArgumentException() {
        return Stream.of(
                Arguments.of(Collections.emptyList(), -1, "window must be positive"),
                Arguments.of(Collections.emptyList(), 0, "window must be positive")
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetSimpleMovingAverages_withoutValueExtractor_throwsIllegalArgumentException")
    void getSimpleMovingAverages_withoutValueExtractor_throwsIllegalArgumentException(
            List<Double> values,
            int window,
            String expectedMessage
    ) {
        List<BigDecimal> bigDecimalValues = getBigDecimalValues(values);

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getSimpleMovingAverages(bigDecimalValues, window),
                IllegalArgumentException.class,
                expectedMessage
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetSimpleMovingAverages_withoutValueExtractor_throwsIllegalArgumentException")
    void getSimpleMovingAverages_withValueExtractor_throwsIllegalArgumentException(
            List<Double> values,
            int window,
            String expectedMessage
    ) {
        List<Optional<BigDecimal>> elements = getOptionalBigDecimalValues(values);

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getSimpleMovingAverages(elements, Optional::get, window),
                IllegalArgumentException.class,
                expectedMessage
        );
    }

    static Stream<Arguments> getData_forGetSimpleMovingAverages() {
        return Stream.of(
                Arguments.of(
                        Collections.emptyList(),
                        4,
                        Collections.emptyList()
                ),
                Arguments.of(
                        Collections.singletonList(1000.0),
                        4,
                        Collections.singletonList(1000.0)
                ),
                Arguments.of(
                        ImmutableList.of(1000.0, 2000.0),
                        4,
                        ImmutableList.of(1000.0, 2000.0)
                ),
                Arguments.of(
                        Collections.singletonList(1000.0),
                        1,
                        Collections.singletonList(1000.0)
                ),
                Arguments.of(
                        ImmutableList.of(1000.0, 3000.0, 2000.0, 4000.0),
                        4,
                        ImmutableList.of(1000.0, 2000.0, 3000.0, 4000.0)
                ),
                Arguments.of(
                        ImmutableList.of(1000.0, 3000.0, 2000.0),
                        5,
                        ImmutableList.of(1000.0, 2000.0, 2000.0)
                ),
                Arguments.of(
                        ImmutableList.of(1000.0, 3000.0, 2000.0, 4000.0, 6000.0),
                        1,
                        ImmutableList.of(1000.0, 2000.0, 3000.0, 4000.0, 6000.0)
                ),
                Arguments.of(
                        ImmutableList.of(
                                9912.0, 9898.0, 9876.0, 9897.0, 9897.0,
                                9898.0, 9885.0, 9896.0, 9888.0, 9888.0,
                                9881.0, 9878.0, 9887.0, 9878.0, 9878.0,
                                9883.0, 9878.0, 9861.0, 9862.0, 9862.0
                        ),
                        4,
                        ImmutableList.of(
                                9912.00000, 9895.33333, 9896.00000, 9894.71429, 9894.11111,
                                9891.44444, 9889.55556, 9889.77778, 9888.66667, 9886.55556,
                                9884.33333, 9884.11111, 9882.11111, 9879.11111, 9876.22222,
                                9874.11111, 9871.71429, 9869.20000, 9861.66667, 9862.00000
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetSimpleMovingAverages")
    void getSimpleMovingAverages_withoutValueExtractor(
            List<Double> values,
            int window,
            List<Double> expectedValues
    ) {
        List<BigDecimal> bigDecimalValues = getBigDecimalValues(values);

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(bigDecimalValues, window);

        List<BigDecimal> bigDecimalExpectedValues = getBigDecimalValues(expectedValues);
        AssertUtils.assertBigDecimalListsAreEqual(bigDecimalExpectedValues, movingAverages);

        for (BigDecimal average : movingAverages) {
            Assertions.assertTrue(
                    DecimalUtils.DEFAULT_SCALE >= average.scale(),
                    "expected default scale for all averages"
            );
        }
    }

    @ParameterizedTest
    @MethodSource("getData_forGetSimpleMovingAverages")
    void getSimpleMovingAverages_withValueExtractor(
            List<Double> values,
            int window,
            List<Double> expectedValues
    ) {
        List<Optional<BigDecimal>> elements = getOptionalBigDecimalValues(values);

        List<BigDecimal> movingAverages = MathUtils.getSimpleMovingAverages(elements, Optional::get, window);

        List<BigDecimal> bigDecimalExpectedValues = getBigDecimalValues(expectedValues);
        AssertUtils.assertBigDecimalListsAreEqual(bigDecimalExpectedValues, movingAverages);

        for (BigDecimal average : movingAverages) {
            Assertions.assertTrue(
                    DecimalUtils.DEFAULT_SCALE >= average.scale(),
                    "expected default scale for all averages"
            );
        }
    }

    // endregion

    // region getLinearWeightedMovingAverages tests

    static Stream<Arguments> getData_forGetLinearWeightedMovingAverages_throwsIllegalArgumentException() {
        return Stream.of(
                Arguments.of(Collections.emptyList(), -1, "window must be positive"),
                Arguments.of(Collections.emptyList(), 0, "window must be positive")
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetLinearWeightedMovingAverages_throwsIllegalArgumentException")
    void getLinearWeightedMovingAverages_withoutValueExtractor_throwsIllegalArgumentException(
            List<Double> values,
            int window,
            String expectedMessage
    ) {
        List<BigDecimal> bigDecimalValues = getBigDecimalValues(values);

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getLinearWeightedMovingAverages(bigDecimalValues, window),
                IllegalArgumentException.class,
                expectedMessage
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetLinearWeightedMovingAverages_throwsIllegalArgumentException")
    void getLinearWeightedMovingAverages_withValueExtractor_throwsIllegalArgumentException(
            List<Double> values,
            int window,
            String expectedMessage
    ) {
        List<Optional<BigDecimal>> elements = getOptionalBigDecimalValues(values);

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getLinearWeightedMovingAverages(elements, Optional::get, window),
                IllegalArgumentException.class,
                expectedMessage
        );
    }

    static Stream<Arguments> getData_forGetLinearWeightedMovingAverages() {
        return Stream.of(
                Arguments.of(
                        Collections.emptyList(),
                        4,
                        Collections.emptyList()
                ),
                Arguments.of(
                        Collections.singletonList(1000.0),
                        4,
                        Collections.singletonList(1000.0)
                ),
                Arguments.of(
                        ImmutableList.of(1000.0, 2000.0),
                        4,
                        ImmutableList.of(1000.0, 2000.0)
                ),
                Arguments.of(
                        Collections.singletonList(1000.0),
                        1,
                        Collections.singletonList(1000.0)
                ),
                Arguments.of(
                        ImmutableList.of(1000.0, 3000.0, 2000.0, 4000.0),
                        4,
                        ImmutableList.of(1000.0, 2250.0, 2750.0, 4000.0)
                ),
                Arguments.of(
                        ImmutableList.of(1000.0, 3000.0, 2000.0),
                        4,
                        ImmutableList.of(1000.0, 2250.0, 2000.0)
                ),
                Arguments.of(
                        ImmutableList.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0),
                        1,
                        ImmutableList.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0)
                ),
                Arguments.of(
                        ImmutableList.of(
                                9912.0, 9898.0, 9876.0, 9897.0, 9897.0,
                                9898.0, 9885.0, 9896.0, 9888.0, 9888.0,
                                9881.0, 9878.0, 9887.0, 9878.0, 9878.0,
                                9883.0, 9878.0, 9861.0, 9862.0, 9862.0
                        ),
                        4,
                        ImmutableList.of(
                                9912.00000, 9896.00000, 9891.88889, 9893.50000, 9893.44000,
                                9892.44000, 9891.32000, 9890.44000, 9888.40000, 9886.32000,
                                9884.20000, 9882.84000, 9881.76000, 9880.00000, 9878.00000,
                                9875.76000, 9872.75000, 9867.55556, 9861.75000, 9862.00000
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetLinearWeightedMovingAverages")
    void getLinearWeightedMovingAverages_withoutValueExtractor(
            List<Double> values,
            int window,
            List<Double> expectedValues
    ) {
        List<BigDecimal> bigDecimalValues = getBigDecimalValues(values);

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(bigDecimalValues, window);

        List<BigDecimal> bigDecimalExpectedValues = getBigDecimalValues(expectedValues);
        AssertUtils.assertBigDecimalListsAreEqual(bigDecimalExpectedValues, movingAverages);

        for (BigDecimal average : movingAverages) {
            Assertions.assertTrue(
                    DecimalUtils.DEFAULT_SCALE >= average.scale(),
                    "expected default scale for all averages"
            );
        }
    }

    @ParameterizedTest
    @MethodSource("getData_forGetLinearWeightedMovingAverages")
    void getLinearWeightedMovingAverages_withValueExtractor(
            List<Double> values,
            int window,
            List<Double> expectedValues
    ) {
        List<Optional<BigDecimal>> elements = getOptionalBigDecimalValues(values);

        List<BigDecimal> movingAverages = MathUtils.getLinearWeightedMovingAverages(elements, Optional::get, window);

        List<BigDecimal> bigDecimalExpectedValues = getBigDecimalValues(expectedValues);
        AssertUtils.assertBigDecimalListsAreEqual(bigDecimalExpectedValues, movingAverages);

        for (BigDecimal average : movingAverages) {
            Assertions.assertTrue(
                    DecimalUtils.DEFAULT_SCALE >= average.scale(),
                    "expected default scale for all averages"
            );
        }
    }

    // endregion

    // region getExponentialWeightedMovingAverages without order tests

    static Stream<Arguments> getData_forGetExponentialWeightedMovingAverages_withoutOrder_throwsIllegalArgumentException() {
        return Stream.of(
                Arguments.of(Collections.emptyList(), -0.1, "weightDecrease must be in range (0; 1]"),
                Arguments.of(Collections.emptyList(), 0.0, "weightDecrease must be in range (0; 1]"),
                Arguments.of(Collections.emptyList(), 1.1, "weightDecrease must be in range (0; 1]")
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetExponentialWeightedMovingAverages_withoutOrder_throwsIllegalArgumentException")
    void getExponentialWeightedMovingAverages_withoutOrder_andWithoutValueExtractor_throwsIllegalArgumentException(
            List<Double> values,
            double weightDecrease,
            String expectedMessage
    ) {
        List<BigDecimal> bigDecimalValues = getBigDecimalValues(values);

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(bigDecimalValues, weightDecrease),
                IllegalArgumentException.class,
                expectedMessage
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetExponentialWeightedMovingAverages_withoutOrder_throwsIllegalArgumentException")
    void getExponentialWeightedMovingAverages_withoutOrder_andWithValueExtractor_throwsIllegalArgumentException(
            List<Double> values,
            double weightDecrease,
            String expectedMessage
    ) {
        List<Optional<BigDecimal>> elements = getOptionalBigDecimalValues(values);

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease),
                IllegalArgumentException.class,
                expectedMessage
        );
    }

    static Stream<Arguments> getData_forGetExponentialWeightedMovingAverages_withoutOrder() {
        return Stream.of(
                Arguments.of(
                        Collections.emptyList(),
                        0.8,
                        Collections.emptyList()
                ),
                Arguments.of(
                        Collections.singletonList(1000.0),
                        0.8,
                        Collections.singletonList(1000.0)
                ),
                Arguments.of(
                        ImmutableList.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0),
                        1.0,
                        ImmutableList.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0)
                ),
                Arguments.of(
                        ImmutableList.of(1000.0, 1000.0, 1000.0, 1000.0, 1000.0),
                        0.8,
                        ImmutableList.of(1000.0, 1000.0, 1000.0, 1000.0, 1000.0)
                ),
                Arguments.of(
                        ImmutableList.of(
                                1000.0, 2000.0, 3000.0, 4000.0, 5000.0,
                                6000.0, 7000.0, 8000.0, 9000.0, 10000.0
                        ),
                        0.8,
                        ImmutableList.of(
                                1124.99994, 2024.99968, 3004.99840, 4000.99200, 5000.16000,
                                5999.84000, 6999.00800, 7995.00160, 8975.00032, 9875.00006
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetExponentialWeightedMovingAverages_withoutOrder")
    void getExponentialWeightedMovingAverages_withoutOrder_andWithoutValueExtractor(
            List<Double> values,
            double weightDecrease,
            List<Double> expectedValues
    ) {
        List<BigDecimal> bigDecimalValues = getBigDecimalValues(values);

        List<BigDecimal> movingAverages =
                MathUtils.getExponentialWeightedMovingAverages(bigDecimalValues, weightDecrease);

        List<BigDecimal> bigDecimalExpectedValues = getBigDecimalValues(expectedValues);
        AssertUtils.assertBigDecimalListsAreEqual(bigDecimalExpectedValues, movingAverages);

        for (BigDecimal average : movingAverages) {
            Assertions.assertTrue(
                    DecimalUtils.DEFAULT_SCALE >= average.scale(),
                    "expected default scale for all averages"
            );
        }
    }

    @ParameterizedTest
    @MethodSource("getData_forGetExponentialWeightedMovingAverages_withoutOrder")
    void getExponentialWeightedMovingAverages_withoutOrder_andWithValueExtractor(
            List<Double> values,
            double weightDecrease,
            List<Double> expectedValues
    ) {
        List<Optional<BigDecimal>> elements = getOptionalBigDecimalValues(values);

        List<BigDecimal> movingAverages =
                MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease);

        List<BigDecimal> bigDecimalExpectedValues = getBigDecimalValues(expectedValues);
        AssertUtils.assertBigDecimalListsAreEqual(bigDecimalExpectedValues, movingAverages);

        for (BigDecimal average : movingAverages) {
            Assertions.assertTrue(
                    DecimalUtils.DEFAULT_SCALE >= average.scale(),
                    "expected default scale for all averages"
            );
        }
    }

    // endregion

    // region getExponentialWeightedMovingAverages with order tests

    static Stream<Arguments> getData_forGetExponentialWeightedMovingAverages_withOrder_throwsIllegalArgumentException() {
        return Stream.of(
                Arguments.of(Collections.emptyList(), -0.1, 3, "weightDecrease must be in range (0; 1]"),
                Arguments.of(Collections.emptyList(), 0.0, 3, "weightDecrease must be in range (0; 1]"),
                Arguments.of(Collections.emptyList(), 1.1, 3, "weightDecrease must be in range (0; 1]"),
                Arguments.of(Collections.emptyList(), 0.5, -1, "order must be positive"),
                Arguments.of(Collections.emptyList(), 0.5, 0, "order must be positive")
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetExponentialWeightedMovingAverages_withOrder_throwsIllegalArgumentException")
    void getExponentialWeightedMovingAverages_withOrder_andWithoutValueExtractor_throwsIllegalArgumentException(
            List<Double> values,
            double weightDecrease,
            int order,
            String expectedMessage
    ) {
        List<BigDecimal> bigDecimalValues = getBigDecimalValues(values);

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(bigDecimalValues, weightDecrease, order),
                IllegalArgumentException.class,
                expectedMessage
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetExponentialWeightedMovingAverages_withOrder_throwsIllegalArgumentException")
    void getExponentialWeightedMovingAverages_withOrder_andWithValueExtractor_throwsIllegalArgumentException(
            List<Double> values,
            double weightDecrease,
            int order,
            String expectedMessage
    ) {
        List<Optional<BigDecimal>> elements = getOptionalBigDecimalValues(values);

        AssertUtils.assertThrowsWithMessage(
                () -> MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease, order),
                IllegalArgumentException.class,
                expectedMessage
        );
    }

    static Stream<Arguments> getData_forGetExponentialWeightedMovingAverages_withOrder() {
        return Stream.of(
                Arguments.of(
                        Collections.emptyList(),
                        0.8,
                        3,
                        Collections.emptyList()
                ),
                Arguments.of(
                        Collections.singletonList(1000.0),
                        0.8,
                        3,
                        Collections.singletonList(1000.0)
                ),
                Arguments.of(
                        ImmutableList.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0),
                        1,
                        3,
                        ImmutableList.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0)
                ),
                Arguments.of(
                        ImmutableList.of(1000.0, 1000.0, 1000.0, 1000.0, 1000.0),
                        0.8,
                        3,
                        ImmutableList.of(1000.0, 1000.0, 1000.0, 1000.0, 1000.0)
                ),
                Arguments.of(
                        ImmutableList.of(
                                1000.0, 2000.0, 3000.0, 4000.0, 5000.0,
                                6000.0, 7000.0, 8000.0, 9000.0, 10000.0
                        ),
                        0.8,
                        1,
                        ImmutableList.of(
                                1124.99994, 2024.99968, 3004.99840, 4000.99200, 5000.16000,
                                5999.84000, 6999.00800, 7995.00160, 8975.00032, 9875.00006
                        )
                ),
                Arguments.of(
                        ImmutableList.of(
                                1000.0, 2000.0, 3000.0, 4000.0, 5000.0,
                                6000.0, 7000.0, 8000.0, 9000.0, 10000.0
                        ),
                        0.8,
                        2,
                        ImmutableList.of(
                                1249.99941, 2069.99731, 3017.98784, 4004.34560, 5000.80000,
                                5999.20000, 6995.65440, 7982.01216, 8930.00269, 9750.00059
                        )
                ),
                Arguments.of(
                        ImmutableList.of(
                                1000.0, 2000.0, 3000.0, 4000.0, 5000.0,
                                6000.0, 7000.0, 8000.0, 9000.0, 10000.0
                        ),
                        0.8,
                        3,
                        ImmutableList.of(
                                1374.99704, 2130.98757, 3040.54861, 4011.43168, 5002.33600,
                                5997.66400, 6988.56832, 7959.45139, 8869.01243, 9625.00296
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetExponentialWeightedMovingAverages_withOrder")
    void getExponentialWeightedMovingAverages_withOrder_andWithoutValueExtractor(
            List<Double> values,
            double weightDecrease,
            int order,
            List<Double> expectedValues
    ) {
        List<BigDecimal> bigDecimalValues = getBigDecimalValues(values);

        List<BigDecimal> movingAverages =
                MathUtils.getExponentialWeightedMovingAverages(bigDecimalValues, weightDecrease, order);

        List<BigDecimal> bigDecimalExpectedValues = getBigDecimalValues(expectedValues);
        AssertUtils.assertBigDecimalListsAreEqual(bigDecimalExpectedValues, movingAverages);

        for (BigDecimal average : movingAverages) {
            Assertions.assertTrue(
                    DecimalUtils.DEFAULT_SCALE >= average.scale(),
                    "expected default scale for all averages"
            );
        }
    }

    @ParameterizedTest
    @MethodSource("getData_forGetExponentialWeightedMovingAverages_withOrder")
    void getExponentialWeightedMovingAverages_withOrder_andWithValueExtractor(
            List<Double> values,
            double weightDecrease,
            int order,
            List<Double> expectedValues
    ) {
        List<Optional<BigDecimal>> elements = getOptionalBigDecimalValues(values);

        List<BigDecimal> movingAverages =
                MathUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease, order);

        List<BigDecimal> bigDecimalExpectedValues = getBigDecimalValues(expectedValues);
        AssertUtils.assertBigDecimalListsAreEqual(bigDecimalExpectedValues, movingAverages);

        for (BigDecimal average : movingAverages) {
            Assertions.assertTrue(
                    DecimalUtils.DEFAULT_SCALE >= average.scale(),
                    "expected default scale for all averages"
            );
        }
    }

    // endregion

    // region getLocalExtremes tests

    static Stream<Arguments> getData_forGetLocalExtremes() {
        return Stream.of(
                Arguments.of(
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        Collections.singletonList(100.0),
                        Collections.singletonList(0),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        ImmutableList.of(100.0, 90.0),
                        Collections.singletonList(0),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        ImmutableList.of(100.0, 100.0),
                        Collections.singletonList(1),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        ImmutableList.of(90.0, 100.0),
                        Collections.singletonList(1),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        ImmutableList.of(100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0),
                        Collections.singletonList(9),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        ImmutableList.of(10.0, 30.0, 30.0, 29.9, 20.0, 25.0, 21.0, 15.0, 50.0, 30.0),
                        ImmutableList.of(2, 5, 8),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        ImmutableList.of(10.0, 30.0, 30.0, 29.9, 20.0, 25.0, 21.0, 50.0, 50.0, 30.0),
                        ImmutableList.of(2, 5, 8),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        ImmutableList.of(10.0, 30.0, 30.0, 29.9, 20.0, 25.0, 21.0, 50.0, 50.0, 50.0),
                        ImmutableList.of(2, 5, 9),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        ImmutableList.of(100.0, 30.0, 30.0, 29.9, 20.0, 25.0, 21.0, 15.0, 50.0, 50.1),
                        ImmutableList.of(0, 2, 5, 9),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        ImmutableList.of(10.0, 5.0, 5.0, 5.1, 4.0, 3.5, 5.0, 70.0, 50.0, 80.0),
                        ImmutableList.of(0, 3, 7, 9),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        ImmutableList.of(10.0, 5.0, 5.0, 5.1, 4.0, 3.5, 5.0, 70.0, 50.0, 80.0),
                        ImmutableList.of(2, 5, 8),
                        Comparator.reverseOrder()
                ),
                Arguments.of(
                        ImmutableList.of(20.0, 15.0, 15.0, 15.1, 20.0, 19.0, 21.0, 10.0, 10.0, 30.0),
                        ImmutableList.of(2, 5, 8),
                        Comparator.reverseOrder()
                ),
                Arguments.of(
                        ImmutableList.of(20.0, 15.0, 15.0, 15.1, 20.0, 19.0, 21.0, 10.0, 10.0, 10.0),
                        ImmutableList.of(2, 5, 9),
                        Comparator.reverseOrder()
                ),
                Arguments.of(
                        ImmutableList.of(10.0, 30.0, 30.0, 30.1, 20.0, 19.0, 21.0, 60.0, 50.0, 49.9),
                        ImmutableList.of(0, 2, 5, 9),
                        Comparator.reverseOrder()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetLocalExtremes")
    void getLocalExtremes_withoutValueExtractor(
            List<Double> values,
            List<Integer> expectedExtremes,
            Comparator<BigDecimal> comparator
    ) {
        List<BigDecimal> bigDecimalValues = getBigDecimalValues(values);

        List<Integer> extremes = MathUtils.getLocalExtremes(bigDecimalValues, comparator);

        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @ParameterizedTest
    @MethodSource("getData_forGetLocalExtremes")
    void getLocalExtremes_withValueExtractor(
            List<Double> values,
            List<Integer> expectedExtremes,
            Comparator<BigDecimal> comparator
    ) {
        List<Optional<BigDecimal>> elements = getOptionalBigDecimalValues(values);

        List<Integer> extremes = MathUtils.getLocalExtremes(elements, Optional::get, comparator);

        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    // endregion

    // region getSortedLocalExtremes tests

    static Stream<Arguments> getData_forGetSortedLocalExtremes() {
        return Stream.of(
                Arguments.of(
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        Collections.singletonList(100.0),
                        Collections.singletonList(0),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        ImmutableList.of(100.0, 90.0),
                        Collections.singletonList(0),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        ImmutableList.of(100.0, 100.0),
                        Collections.singletonList(1),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        ImmutableList.of(90.0, 100.0),
                        Collections.singletonList(1),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        ImmutableList.of(100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0),
                        Collections.singletonList(9),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        ImmutableList.of(10.0, 30.0, 30.0, 29.9, 20.0, 25.0, 21.0, 15.0, 50.0, 30.0),
                        ImmutableList.of(8, 2, 5),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        ImmutableList.of(10.0, 30.0, 30.0, 29.9, 20.0, 25.0, 21.0, 50.0, 50.0, 30.0),
                        ImmutableList.of(8, 2, 5),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        ImmutableList.of(10.0, 30.0, 30.0, 29.9, 20.0, 25.0, 21.0, 50.0, 50.0, 50.0),
                        ImmutableList.of(9, 2, 5),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        ImmutableList.of(100.0, 30.0, 30.0, 29.9, 20.0, 25.0, 21.0, 15.0, 50.0, 50.1),
                        ImmutableList.of(0, 9, 2, 5),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        ImmutableList.of(10.0, 5.0, 5.0, 5.1, 4.0, 3.5, 5.0, 70.0, 50.0, 80.0),
                        ImmutableList.of(9, 7, 0, 3),
                        Comparator.naturalOrder()
                ),
                Arguments.of(
                        ImmutableList.of(10.0, 5.0, 5.0, 5.1, 4.0, 3.5, 5.0, 70.0, 50.0, 80.0),
                        ImmutableList.of(5, 2, 8),
                        Comparator.reverseOrder()
                ),
                Arguments.of(
                        ImmutableList.of(20.0, 15.0, 15.0, 15.1, 20.0, 19.0, 21.0, 10.0, 10.0, 30.0),
                        ImmutableList.of(8, 2, 5),
                        Comparator.reverseOrder()
                ),
                Arguments.of(
                        ImmutableList.of(20.0, 15.0, 15.0, 15.1, 20.0, 19.0, 21.0, 10.0, 10.0, 10.0),
                        ImmutableList.of(9, 2, 5),
                        Comparator.reverseOrder()
                ),
                Arguments.of(
                        ImmutableList.of(10.0, 30.0, 30.0, 30.1, 20.0, 19.0, 21.0, 60.0, 50.0, 49.9),
                        ImmutableList.of(0, 5, 2, 9),
                        Comparator.reverseOrder()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetSortedLocalExtremes")
    void getSortedLocalExtremes_withoutValueExtractor(
            List<Double> values,
            List<Integer> expectedExtremes,
            Comparator<BigDecimal> comparator
    ) {
        List<BigDecimal> bigDecimalValues = getBigDecimalValues(values);

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(bigDecimalValues, comparator);

        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @ParameterizedTest
    @MethodSource("getData_forGetSortedLocalExtremes")
    void getSortedLocalExtremes_withValueExtractor(
            List<Double> values,
            List<Integer> expectedExtremes,
            Comparator<BigDecimal> comparator
    ) {
        List<Optional<BigDecimal>> elements = getOptionalBigDecimalValues(values);

        List<Integer> extremes = MathUtils.getSortedLocalExtremes(elements, Optional::get, comparator);

        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    // endregion

    private static List<BigDecimal> getBigDecimalValues(List<Double> values) {
        return values.stream().map(DecimalUtils::setDefaultScale).collect(Collectors.toList());
    }

    private static List<Optional<BigDecimal>> getOptionalBigDecimalValues(List<Double> values) {
        return values.stream()
                .map(DecimalUtils::setDefaultScale)
                .map(Optional::of)
                .collect(Collectors.toList());
    }

}