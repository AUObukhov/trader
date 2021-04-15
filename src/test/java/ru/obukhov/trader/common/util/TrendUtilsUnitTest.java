package ru.obukhov.trader.common.util;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.common.model.Point;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

class TrendUtilsUnitTest {

    // region getSimpleMovingAverages tests

    @SuppressWarnings("unused")
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
        List<BigDecimal> bigDecimalValues = TestDataHelper.getBigDecimalValues(values);

        AssertUtils.assertThrowsWithMessage(
                () -> TrendUtils.getSimpleMovingAverages(bigDecimalValues, window),
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
        List<Optional<BigDecimal>> elements = TestDataHelper.getOptionalBigDecimalValues(values);

        AssertUtils.assertThrowsWithMessage(
                () -> TrendUtils.getSimpleMovingAverages(elements, Optional::get, window),
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
                        ImmutableList.of(1000.0, 1500.0)
                ),
                Arguments.of(
                        Collections.singletonList(1000.0),
                        1,
                        Collections.singletonList(1000.0)
                ),
                Arguments.of(
                        ImmutableList.of(1000.0, 2000.0, 3000.0, 4000.0),
                        4,
                        ImmutableList.of(1000.0, 1500.0, 2000.0, 2500.0)
                ),
                Arguments.of(
                        ImmutableList.of(1000.0, 2000.0, 3000.0),
                        5,
                        ImmutableList.of(1000.0, 1500.0, 2000.0)
                ),
                Arguments.of(
                        ImmutableList.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0),
                        1,
                        ImmutableList.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0)
                ),
                Arguments.of(
                        ImmutableList.of(
                                1000.0, 2000.0, 3000.0, 4000.0, 5000.0,
                                6000.0, 7000.0, 8000.0, 9000.0, 10000.0
                        ),
                        4,
                        ImmutableList.of(
                                1000.0, 1500.0, 2000.0, 2500.0, 3500.0,
                                4500.0, 5500.0, 6500.0, 7500.0, 8500.0
                        )
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
                                9912.00000, 9905.00000, 9895.33333, 9895.75000, 9892.00000,
                                9892.00000, 9894.25000, 9894.00000, 9891.75000, 9889.25000,
                                9888.25000, 9883.75000, 9883.50000, 9881.00000, 9880.25000,
                                9881.50000, 9879.25000, 9875.00000, 9871.00000, 9865.75000
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
        List<BigDecimal> bigDecimalValues = TestDataHelper.getBigDecimalValues(values);

        List<BigDecimal> movingAverages = TrendUtils.getSimpleMovingAverages(bigDecimalValues, window);

        List<BigDecimal> bigDecimalExpectedValues = TestDataHelper.getBigDecimalValues(expectedValues);
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
        List<Optional<BigDecimal>> elements = TestDataHelper.getOptionalBigDecimalValues(values);

        List<BigDecimal> movingAverages = TrendUtils.getSimpleMovingAverages(elements, Optional::get, window);

        List<BigDecimal> bigDecimalExpectedValues = TestDataHelper.getBigDecimalValues(expectedValues);
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

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetLinearWeightedMovingAverages_withoutOrder_throwsIllegalArgumentException() {
        return Stream.of(
                Arguments.of(Collections.emptyList(), -1, "window must be positive"),
                Arguments.of(Collections.emptyList(), 0, "window must be positive")
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetLinearWeightedMovingAverages_withoutOrder_throwsIllegalArgumentException")
    void getLinearWeightedMovingAverages_withoutOrder_withoutValueExtractor_throwsIllegalArgumentException(
            List<Double> values,
            int window,
            String expectedMessage
    ) {
        List<BigDecimal> bigDecimalValues = TestDataHelper.getBigDecimalValues(values);

        AssertUtils.assertThrowsWithMessage(
                () -> TrendUtils.getLinearWeightedMovingAverages(bigDecimalValues, window),
                IllegalArgumentException.class,
                expectedMessage
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetLinearWeightedMovingAverages_withoutOrder_throwsIllegalArgumentException")
    void getLinearWeightedMovingAverages_withoutOrder_withValueExtractor_throwsIllegalArgumentException(
            List<Double> values,
            int window,
            String expectedMessage
    ) {
        List<Optional<BigDecimal>> elements = TestDataHelper.getOptionalBigDecimalValues(values);

        AssertUtils.assertThrowsWithMessage(
                () -> TrendUtils.getLinearWeightedMovingAverages(elements, Optional::get, window),
                IllegalArgumentException.class,
                expectedMessage
        );
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetLinearWeightedMovingAverages_withOrder_throwsIllegalArgumentException() {
        return Stream.of(
                Arguments.of(Collections.emptyList(), -1, "order must be positive"),
                Arguments.of(Collections.emptyList(), 0, "order must be positive")
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetLinearWeightedMovingAverages_withOrder_throwsIllegalArgumentException")
    void getLinearWeightedMovingAverages_withOrder_withoutValueExtractor_throwsIllegalArgumentException(
            List<Double> values,
            int order,
            String expectedMessage
    ) {
        List<BigDecimal> bigDecimalValues = TestDataHelper.getBigDecimalValues(values);

        AssertUtils.assertThrowsWithMessage(
                () -> TrendUtils.getLinearWeightedMovingAverages(bigDecimalValues, 1, order),
                IllegalArgumentException.class,
                expectedMessage
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetLinearWeightedMovingAverages_withOrder_throwsIllegalArgumentException")
    void getLinearWeightedMovingAverages_withOrder_withValueExtractor_throwsIllegalArgumentException(
            List<Double> values,
            int order,
            String expectedMessage
    ) {
        List<Optional<BigDecimal>> bigDecimalValues = TestDataHelper.getOptionalBigDecimalValues(values);

        AssertUtils.assertThrowsWithMessage(
                () -> TrendUtils.getLinearWeightedMovingAverages(bigDecimalValues, Optional::get, 1, order),
                IllegalArgumentException.class,
                expectedMessage
        );
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetLinearWeightedMovingAverages_withoutOrder() {
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
    @MethodSource("getData_forGetLinearWeightedMovingAverages_withoutOrder")
    void getLinearWeightedMovingAverages_withoutOrder_withoutValueExtractor(
            List<Double> values,
            int window,
            List<Double> expectedValues
    ) {
        List<BigDecimal> bigDecimalValues = TestDataHelper.getBigDecimalValues(values);

        List<BigDecimal> movingAverages = TrendUtils.getLinearWeightedMovingAverages(bigDecimalValues, window);

        List<BigDecimal> bigDecimalExpectedValues = TestDataHelper.getBigDecimalValues(expectedValues);
        AssertUtils.assertBigDecimalListsAreEqual(bigDecimalExpectedValues, movingAverages);

        for (BigDecimal average : movingAverages) {
            Assertions.assertTrue(
                    DecimalUtils.DEFAULT_SCALE >= average.scale(),
                    "expected default scale for all averages"
            );
        }
    }

    @ParameterizedTest
    @MethodSource("getData_forGetLinearWeightedMovingAverages_withoutOrder")
    void getLinearWeightedMovingAverages_withoutOrder_withValueExtractor(
            List<Double> values,
            int window,
            List<Double> expectedValues
    ) {
        List<Optional<BigDecimal>> elements = TestDataHelper.getOptionalBigDecimalValues(values);

        List<BigDecimal> movingAverages = TrendUtils.getLinearWeightedMovingAverages(elements, Optional::get, window);

        List<BigDecimal> bigDecimalExpectedValues = TestDataHelper.getBigDecimalValues(expectedValues);
        AssertUtils.assertBigDecimalListsAreEqual(bigDecimalExpectedValues, movingAverages);

        for (BigDecimal average : movingAverages) {
            Assertions.assertTrue(
                    DecimalUtils.DEFAULT_SCALE >= average.scale(),
                    "expected default scale for all averages"
            );
        }
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetLinearWeightedMovingAverages_withOrder() {
        return Stream.of(
                Arguments.of(Collections.emptyList(), 4, 1, Collections.emptyList()),
                Arguments.of(Collections.emptyList(), 4, 2, Collections.emptyList()),
                Arguments.of(Collections.emptyList(), 4, 10, Collections.emptyList()),

                Arguments.of(Collections.singletonList(1000.0), 4, 1, Collections.singletonList(1000.0)),
                Arguments.of(Collections.singletonList(1000.0), 4, 2, Collections.singletonList(1000.0)),
                Arguments.of(Collections.singletonList(1000.0), 4, 10, Collections.singletonList(1000.0)),

                Arguments.of(ImmutableList.of(1000.0, 2000.0), 4, 1, ImmutableList.of(1000.0, 2000.0)),
                Arguments.of(ImmutableList.of(1000.0, 2000.0), 4, 2, ImmutableList.of(1000.0, 2000.0)),
                Arguments.of(ImmutableList.of(1000.0, 2000.0), 4, 10, ImmutableList.of(1000.0, 2000.0)),

                Arguments.of(Collections.singletonList(1000.0), 1, 1, Collections.singletonList(1000.0)),
                Arguments.of(Collections.singletonList(1000.0), 1, 2, Collections.singletonList(1000.0)),
                Arguments.of(Collections.singletonList(1000.0), 1, 10, Collections.singletonList(1000.0)),

                Arguments.of(
                        ImmutableList.of(1000.0, 3000.0, 2000.0, 4000.0),
                        4,
                        1,
                        ImmutableList.of(1000.0, 2250.0, 2750.0, 4000.0)
                ),
                Arguments.of(
                        ImmutableList.of(1000.0, 3000.0, 2000.0, 4000.0),
                        4,
                        2,
                        ImmutableList.of(1000.0, 2062.5, 2937.5, 4000.0)
                ),
                Arguments.of(
                        ImmutableList.of(1000.0, 3000.0, 2000.0, 4000.0),
                        4,
                        3,
                        ImmutableList.of(1000.0, 2015.625, 2984.375, 4000.0)
                ),

                Arguments.of(
                        ImmutableList.of(1000.0, 3000.0, 2000.0),
                        4,
                        1,
                        ImmutableList.of(1000.0, 2250.0, 2000.0)
                ),
                Arguments.of(
                        ImmutableList.of(1000.0, 3000.0, 2000.0),
                        4,
                        2,
                        ImmutableList.of(1000.0, 1875.0, 2000.0)
                ),
                Arguments.of(
                        ImmutableList.of(1000.0, 3000.0, 2000.0),
                        4,
                        3,
                        ImmutableList.of(1000.0, 1687.5, 2000.0)
                ),

                Arguments.of(
                        ImmutableList.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0),
                        1,
                        1,
                        ImmutableList.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0)
                ),
                Arguments.of(
                        ImmutableList.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0),
                        1,
                        2,
                        ImmutableList.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0)
                ),
                Arguments.of(
                        ImmutableList.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0),
                        1,
                        10,
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
                        1,
                        ImmutableList.of(
                                9912.00000, 9896.00000, 9891.88889, 9893.50000, 9893.44000,
                                9892.44000, 9891.32000, 9890.44000, 9888.40000, 9886.32000,
                                9884.20000, 9882.84000, 9881.76000, 9880.00000, 9878.00000,
                                9875.76000, 9872.75000, 9867.55556, 9861.75000, 9862.00000
                        )
                ),
                Arguments.of(
                        ImmutableList.of(
                                9912.0, 9898.0, 9876.0, 9897.0, 9897.0,
                                9898.0, 9885.0, 9896.0, 9888.0, 9888.0,
                                9881.0, 9878.0, 9887.0, 9878.0, 9878.0,
                                9883.0, 9878.0, 9861.0, 9862.0, 9862.0
                        ),
                        4,
                        2,
                        ImmutableList.of(
                                9912.00000, 9898.97222, 9895.56741, 9894.38667, 9893.35467,
                                9891.83831, 9890.77476, 9889.55920, 9888.05440, 9886.41760,
                                9884.73280, 9883.04000, 9881.28280, 9879.30462, 9876.93244,
                                9874.25307, 9871.40292, 9867.71408, 9863.26389, 9862.00000
                        )
                ),
                Arguments.of(
                        ImmutableList.of(
                                9912.0, 9898.0, 9876.0, 9897.0, 9897.0,
                                9898.0, 9885.0, 9896.0, 9888.0, 9888.0,
                                9881.0, 9878.0, 9887.0, 9878.0, 9878.0,
                                9883.0, 9878.0, 9861.0, 9862.0, 9862.0
                        ),
                        4,
                        3,
                        ImmutableList.of(
                                9912.00000, 9901.37796, 9897.64163, 9895.54430, 9893.91268,
                                9892.00721, 9890.62399, 9889.25928, 9887.80384, 9886.25170,
                                9884.59763, 9882.80860, 9880.86347, 9878.70666, 9876.25820,
                                9873.59185, 9870.82565, 9867.63655, 9864.06047, 9862.00000
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetLinearWeightedMovingAverages_withOrder")
    void getLinearWeightedMovingAverages_withOrder_withoutValueExtractor(
            List<Double> values,
            int window,
            int order,
            List<Double> expectedValues
    ) {
        List<BigDecimal> bigDecimalValues = TestDataHelper.getBigDecimalValues(values);

        List<BigDecimal> movingAverages = TrendUtils.getLinearWeightedMovingAverages(bigDecimalValues, window, order);

        List<BigDecimal> bigDecimalExpectedValues = TestDataHelper.getBigDecimalValues(expectedValues);
        AssertUtils.assertBigDecimalListsAreEqual(bigDecimalExpectedValues, movingAverages);

        for (BigDecimal average : movingAverages) {
            Assertions.assertTrue(
                    DecimalUtils.DEFAULT_SCALE >= average.scale(),
                    "expected default scale for all averages"
            );
        }
    }

    @ParameterizedTest
    @MethodSource("getData_forGetLinearWeightedMovingAverages_withOrder")
    void getLinearWeightedMovingAverages_withOrder_withValueExtractor(
            List<Double> values,
            int window,
            int order,
            List<Double> expectedValues
    ) {
        List<Optional<BigDecimal>> elements = TestDataHelper.getOptionalBigDecimalValues(values);

        List<BigDecimal> movingAverages =
                TrendUtils.getLinearWeightedMovingAverages(elements, Optional::get, window, order);

        List<BigDecimal> bigDecimalExpectedValues = TestDataHelper.getBigDecimalValues(expectedValues);
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

    @SuppressWarnings("unused")
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
        List<BigDecimal> bigDecimalValues = TestDataHelper.getBigDecimalValues(values);

        AssertUtils.assertThrowsWithMessage(
                () -> TrendUtils.getExponentialWeightedMovingAverages(bigDecimalValues, weightDecrease),
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
        List<Optional<BigDecimal>> elements = TestDataHelper.getOptionalBigDecimalValues(values);

        AssertUtils.assertThrowsWithMessage(
                () -> TrendUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease),
                IllegalArgumentException.class,
                expectedMessage
        );
    }

    @SuppressWarnings("unused")
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
        List<BigDecimal> bigDecimalValues = TestDataHelper.getBigDecimalValues(values);

        List<BigDecimal> movingAverages =
                TrendUtils.getExponentialWeightedMovingAverages(bigDecimalValues, weightDecrease);

        List<BigDecimal> bigDecimalExpectedValues = TestDataHelper.getBigDecimalValues(expectedValues);
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
        List<Optional<BigDecimal>> elements = TestDataHelper.getOptionalBigDecimalValues(values);

        List<BigDecimal> movingAverages =
                TrendUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease);

        List<BigDecimal> bigDecimalExpectedValues = TestDataHelper.getBigDecimalValues(expectedValues);
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

    @SuppressWarnings("unused")
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
        List<BigDecimal> bigDecimalValues = TestDataHelper.getBigDecimalValues(values);

        AssertUtils.assertThrowsWithMessage(
                () -> TrendUtils.getExponentialWeightedMovingAverages(bigDecimalValues, weightDecrease, order),
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
        List<Optional<BigDecimal>> elements = TestDataHelper.getOptionalBigDecimalValues(values);

        AssertUtils.assertThrowsWithMessage(
                () -> TrendUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease, order),
                IllegalArgumentException.class,
                expectedMessage
        );
    }

    @SuppressWarnings("unused")
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
        List<BigDecimal> bigDecimalValues = TestDataHelper.getBigDecimalValues(values);

        List<BigDecimal> movingAverages =
                TrendUtils.getExponentialWeightedMovingAverages(bigDecimalValues, weightDecrease, order);

        List<BigDecimal> bigDecimalExpectedValues = TestDataHelper.getBigDecimalValues(expectedValues);
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
        List<Optional<BigDecimal>> elements = TestDataHelper.getOptionalBigDecimalValues(values);

        List<BigDecimal> movingAverages =
                TrendUtils.getExponentialWeightedMovingAverages(elements, Optional::get, weightDecrease, order);

        List<BigDecimal> bigDecimalExpectedValues = TestDataHelper.getBigDecimalValues(expectedValues);
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

    @SuppressWarnings("unused")
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
        List<BigDecimal> bigDecimalValues = TestDataHelper.getBigDecimalValues(values);

        List<Integer> extremes = TrendUtils.getLocalExtremes(bigDecimalValues, comparator);

        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @ParameterizedTest
    @MethodSource("getData_forGetLocalExtremes")
    void getLocalExtremes_withValueExtractor(
            List<Double> values,
            List<Integer> expectedExtremes,
            Comparator<BigDecimal> comparator
    ) {
        List<Optional<BigDecimal>> elements = TestDataHelper.getOptionalBigDecimalValues(values);

        List<Integer> extremes = TrendUtils.getLocalExtremes(elements, Optional::get, comparator);

        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    // endregion

    // region getLocalExtremes tests

    @Test
    void getLocalExtremes() {
        List<BigDecimal> values = TestDataHelper.createBigDecimalsList(10, 20, 15, 30);
        OffsetDateTime now = OffsetDateTime.now();
        List<OffsetDateTime> times = ImmutableList.of(now, now.plusMinutes(1), now.plusMinutes(2), now.plusMinutes(2));
        List<Integer> localExtremesIndices = ImmutableList.of(0, 2);

        List<Point> localExtremes = TrendUtils.getLocalExtremes(values, times, localExtremesIndices);

        List<Point> expectedLocalExtremes = ImmutableList.of(
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
        List<BigDecimal> bigDecimalValues = TestDataHelper.getBigDecimalValues(values);

        List<Integer> extremes = TrendUtils.getSortedLocalExtremes(bigDecimalValues, comparator);

        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    @ParameterizedTest
    @MethodSource("getData_forGetSortedLocalExtremes")
    void getSortedLocalExtremes_withValueExtractor(
            List<Double> values,
            List<Integer> expectedExtremes,
            Comparator<BigDecimal> comparator
    ) {
        List<Optional<BigDecimal>> elements = TestDataHelper.getOptionalBigDecimalValues(values);

        List<Integer> extremes = TrendUtils.getSortedLocalExtremes(elements, Optional::get, comparator);

        AssertUtils.assertListsAreEqual(expectedExtremes, extremes);
    }

    // endregion

    // region getRestraintLines tests

    @Test
    void getRestraintLines_throwsIllegalArgumentException_whenTimesIsLongerThanValues() {
        final OffsetDateTime startTime = OffsetDateTime.now();
        final List<OffsetDateTime> times = ImmutableList.of(startTime, startTime.plusMinutes(1));
        final List<BigDecimal> values = TestDataHelper.createBigDecimalsList(10.0);
        final List<Integer> localExtremes = ImmutableList.of(0, 1);

        AssertUtils.assertThrowsWithMessage(
                () -> TrendUtils.getRestraintLines(times, values, localExtremes),
                IllegalArgumentException.class,
                "times and values must have same size"
        );
    }

    @Test
    void getRestraintLines_throwsIllegalArgumentException_whenValuesIsLongerThanTimes() {
        final List<OffsetDateTime> times = ImmutableList.of(OffsetDateTime.now());
        final List<BigDecimal> values = TestDataHelper.createBigDecimalsList(10.0, 11.0);
        final List<Integer> localExtremes = ImmutableList.of(0, 1);

        AssertUtils.assertThrowsWithMessage(
                () -> TrendUtils.getRestraintLines(times, values, localExtremes),
                IllegalArgumentException.class,
                "times and values must have same size"
        );
    }

    @Test
    void getRestraintLines_throwsIllegalArgumentException_whenLocalExtremesIsLongerThanTimes() {
        final List<OffsetDateTime> times = ImmutableList.of(OffsetDateTime.now());
        final List<BigDecimal> values = TestDataHelper.createBigDecimalsList(10.0);
        final List<Integer> localExtremes = ImmutableList.of(0, 1);

        AssertUtils.assertThrowsWithMessage(
                () -> TrendUtils.getRestraintLines(times, values, localExtremes),
                IllegalArgumentException.class,
                "localExtremes can't be longer than times and values"
        );
    }

    @Test
    void getRestraintLines_returnsEmptyList_whenLocalExtremesIsEmpty() {
        final OffsetDateTime startTime = OffsetDateTime.now();
        final List<OffsetDateTime> times = ImmutableList.of(startTime, startTime.plusMinutes(1));
        final List<BigDecimal> values = TestDataHelper.createBigDecimalsList(10.0, 11.0);
        final List<Integer> localExtremes = ImmutableList.of();

        List<List<Point>> restraintLines = TrendUtils.getRestraintLines(times, values, localExtremes);

        Assertions.assertTrue(restraintLines.isEmpty());
    }

    @Test
    void getRestraintLines_returnsEmptyList_whenThereIsSingleLocalExtremum() {
        final OffsetDateTime startTime = OffsetDateTime.now();
        final List<OffsetDateTime> times = ImmutableList.of(startTime, startTime.plusMinutes(1));
        final List<BigDecimal> values = TestDataHelper.createBigDecimalsList(10.0, 11.0);
        final List<Integer> localExtremes = ImmutableList.of(0);

        List<List<Point>> restraintLines = TrendUtils.getRestraintLines(times, values, localExtremes);

        Assertions.assertTrue(restraintLines.isEmpty());
    }

    @Test
    void getRestraintLines_returnsLines() {
        final OffsetDateTime startTime = OffsetDateTime.now();
        final List<OffsetDateTime> times = ImmutableList.of(
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
        final List<BigDecimal> values = TestDataHelper.createBigDecimalsList(
                10.0, 15.0, 14.0, 11.0, 12.0,
                13.0, 14.0, 14.0, 12.0, 16.0
        );
        final List<Integer> localExtremes = ImmutableList.of(0, 3, 8);

        List<List<Point>> restraintLines = TrendUtils.getRestraintLines(times, values, localExtremes);

        Assertions.assertEquals(2, restraintLines.size());
        List<Point> expectedRestraintLine1 = ImmutableList.of(
                Point.of(times.get(0), 10.00000),
                Point.of(times.get(1), 10.33333),
                Point.of(times.get(2), 10.66667),
                Point.of(times.get(3), 11.00000),
                Point.of(times.get(4), 11.33333),
                Point.of(times.get(5), 11.66667),
                Point.of(times.get(6), 12.00000)
        );
        AssertUtils.assertListsAreEqual(expectedRestraintLine1, restraintLines.get(0));

        List<Point> expectedRestraintLine2 = ImmutableList.of(
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
        List<BigDecimal> values1 = TestDataHelper.createBigDecimalsList(10.0, 20.0);
        List<BigDecimal> values2 = TestDataHelper.createBigDecimalsList(10.0);

        AssertUtils.assertThrowsWithMessage(
                () -> TrendUtils.getCrossovers(values1, values2),
                IllegalArgumentException.class,
                "values1 and values2 must have same size"
        );
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetCrossovers() {
        return Stream.of(
                Arguments.of(
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList()
                ),
                Arguments.of(
                        TestDataHelper.createBigDecimalsList(10, 20, 30),
                        TestDataHelper.createBigDecimalsList(10, 20, 30),
                        Collections.emptyList()
                ),
                Arguments.of(
                        TestDataHelper.createBigDecimalsList(10, 20, 30),
                        TestDataHelper.createBigDecimalsList(10, 20, 31),
                        Collections.emptyList()
                ),
                Arguments.of(
                        TestDataHelper.createBigDecimalsList(10, 12, 13),
                        TestDataHelper.createBigDecimalsList(11, 10, 11),
                        ImmutableList.of(1)
                ),
                Arguments.of(
                        TestDataHelper.createBigDecimalsList(10, 11, 12, 14, 15),
                        TestDataHelper.createBigDecimalsList(10, 12, 13, 12, 11),
                        ImmutableList.of(3)
                ),
                Arguments.of(
                        TestDataHelper.createBigDecimalsList(10, 11, 12, 14, 15),
                        TestDataHelper.createBigDecimalsList(11, 12, 13, 12, 11),
                        ImmutableList.of(3)
                ),
                Arguments.of(
                        TestDataHelper.createBigDecimalsList(10, 11, 12, 14, 15, 15, 16, 16),
                        TestDataHelper.createBigDecimalsList(11, 12, 13, 12, 11, 11, 11, 20),
                        ImmutableList.of(3, 7)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetCrossovers")
    void getCrossovers(
            List<BigDecimal> values1,
            List<BigDecimal> values2,
            List<Integer> expectedCrossovers
    ) {
        List<Integer> crossovers = TrendUtils.getCrossovers(values1, values2);

        AssertUtils.assertListsAreEqual(expectedCrossovers, crossovers);
    }

    @Test
    void getCrossovers_commonAssertions_forRandomValues() {
        List<BigDecimal> values1 = TestDataHelper.createRandomBigDecimalsList(1000);
        List<BigDecimal> values2 = TestDataHelper.createRandomBigDecimalsList(1000);

        List<Integer> crossovers = TrendUtils.getCrossovers(values1, values2);

        if (!crossovers.isEmpty()) {
            if (crossovers.get(0) <= 0) {
                String message = String.format(
                        "First crossover is %s for [%s] and [%s]",
                        crossovers.get(0),
                        StringUtils.join(values1, ", "),
                        StringUtils.join(values2, ", ")
                );
                Assertions.fail(message);
            }
        }

        for (int i = 0; i < crossovers.size() - 1; i++) {
            int currentCrossover = crossovers.get(i);
            int nextCrossover = crossovers.get(i + 1);
            if (currentCrossover >= nextCrossover) {
                String message = String.format(
                        "Not ascending crossovers for [%s] and [%s]",
                        StringUtils.join(values1, ", "),
                        StringUtils.join(values2, ", ")
                );
                Assertions.fail(message);
            }
        }
    }

    // endregion
}