package ru.obukhov.trader.common.util;

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
                        List.of(),
                        4,
                        List.of()
                ),
                Arguments.of(
                        List.of(1000.0),
                        4,
                        List.of(1000.0)
                ),
                Arguments.of(
                        List.of(1000.0, 2000.0),
                        4,
                        List.of(1000.0, 1500.0)
                ),
                Arguments.of(
                        List.of(1000.0),
                        1,
                        List.of(1000.0)
                ),
                Arguments.of(
                        List.of(1000.0, 2000.0, 3000.0, 4000.0),
                        4,
                        List.of(1000.0, 1500.0, 2000.0, 2500.0)
                ),
                Arguments.of(
                        List.of(1000.0, 2000.0, 3000.0),
                        5,
                        List.of(1000.0, 1500.0, 2000.0)
                ),
                Arguments.of(
                        List.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0),
                        1,
                        List.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0)
                ),
                Arguments.of(
                        List.of(
                                1000.0, 2000.0, 3000.0, 4000.0, 5000.0,
                                6000.0, 7000.0, 8000.0, 9000.0, 10000.0
                        ),
                        4,
                        List.of(
                                1000.0, 1500.0, 2000.0, 2500.0, 3500.0,
                                4500.0, 5500.0, 6500.0, 7500.0, 8500.0
                        )
                ),
                Arguments.of(
                        List.of(
                                9912.0, 9898.0, 9876.0, 9897.0, 9897.0,
                                9898.0, 9885.0, 9896.0, 9888.0, 9888.0,
                                9881.0, 9878.0, 9887.0, 9878.0, 9878.0,
                                9883.0, 9878.0, 9861.0, 9862.0, 9862.0
                        ),
                        4,
                        List.of(
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
                        List.of(),
                        4,
                        List.of()
                ),
                Arguments.of(
                        List.of(1000.0),
                        4,
                        List.of(1000.0)
                ),
                Arguments.of(
                        List.of(1000.0, 2000.0),
                        4,
                        List.of(1000.0, 1666.66667)
                ),
                Arguments.of(
                        List.of(1000.0),
                        1,
                        List.of(1000.0)
                ),
                Arguments.of(
                        List.of(1000.0, 2000.0, 3000.0, 4000.0),
                        4,
                        List.of(1000.0, 5000.0 / 3, 14000.0 / 6, 3000.0)
                ),
                Arguments.of(
                        List.of(1000.0, 2000.0, 3000.0),
                        4,
                        List.of(1000.0, 5000.0 / 3, 14000.0 / 6)
                ),
                Arguments.of(
                        List.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0),
                        1,
                        List.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0)
                ),
                Arguments.of(
                        List.of(
                                1000.0, 2000.0, 3000.0, 4000.0, 5000.0,
                                6000.0, 7000.0, 8000.0, 9000.0, 10000.0
                        ),
                        4,
                        List.of(
                                1000.0, 5000.0 / 3, 14000.0 / 6, 3000.0, 4000.0,
                                5000.0, 6000.0, 7000.0, 8000.0, 9000.0
                        )
                ),
                Arguments.of(
                        List.of(
                                9912.0, 9898.0, 9876.0, 9897.0, 9897.0,
                                9898.0, 9885.0, 9896.0, 9888.0, 9888.0,
                                9881.0, 9878.0, 9887.0, 9878.0, 9878.0,
                                9883.0, 9878.0, 9861.0, 9862.0, 9862.0
                        ),
                        4,
                        List.of(
                                9912.00000, 9902.66667, 9889.33333, 9892.40000, 9892.90000,
                                9895.30000, 9892.50000, 9893.20000, 9890.80000, 9889.30000,
                                9886.00000, 9881.90000, 9883.20000, 9881.00000, 9879.80000,
                                9880.90000, 9879.50000, 9872.20000, 9867.00000, 9863.40000
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
                Arguments.of(List.of(), 4, 2, List.of()),
                Arguments.of(List.of(), 4, 10, List.of()),

                Arguments.of(List.of(1000.0), 4, 2, List.of(1000.0)),
                Arguments.of(List.of(1000.0), 4, 10, List.of(1000.0)),

                Arguments.of(List.of(1000.0, 2000.0), 4, 2, List.of(1000.0, 1444.44445)),
                Arguments.of(List.of(1000.0, 2000.0), 4, 10, List.of(1000.0, 1017.34153)),

                Arguments.of(List.of(1000.0), 1, 1, List.of(1000.0)),
                Arguments.of(List.of(1000.0), 1, 2, List.of(1000.0)),
                Arguments.of(List.of(1000.0), 1, 10, List.of(1000.0)),

                Arguments.of(
                        List.of(1000.0, 3000.0, 2000.0, 4000.0),
                        4,
                        2,
                        List.of(1000.0, 1888.88889, 2027.77778, 2376.66667)
                ),
                Arguments.of(
                        List.of(1000.0, 3000.0, 2000.0, 4000.0),
                        4,
                        3,
                        List.of(1000.0, 1592.59259, 1810.18519, 2036.77778)
                ),

                Arguments.of(
                        List.of(1000.0, 3000.0, 2000.0),
                        4,
                        2,
                        List.of(1000.0, 1888.88889, 2027.77778)
                ),
                Arguments.of(
                        List.of(1000.0, 3000.0, 2000.0),
                        4,
                        3,
                        List.of(1000.0, 1592.59259, 1810.18519)
                ),

                Arguments.of(
                        List.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0),
                        1,
                        2,
                        List.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0)
                ),
                Arguments.of(
                        List.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0),
                        1,
                        10,
                        List.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0)
                ),

                Arguments.of(
                        List.of(
                                9912.0, 9898.0, 9876.0, 9897.0, 9897.0,
                                9898.0, 9885.0, 9896.0, 9888.0, 9888.0,
                                9881.0, 9878.0, 9887.0, 9878.0, 9878.0,
                                9883.0, 9878.0, 9861.0, 9862.0, 9862.0
                        ),
                        4,
                        2,
                        List.of(
                                9912.00000, 9905.77778, 9897.55556, 9895.49333, 9893.01333,
                                9893.40333, 9893.41000, 9893.38000, 9892.31000, 9890.85000,
                                9888.67000, 9885.50000, 9883.98000, 9882.34000, 9881.05000,
                                9880.82000, 9880.13000, 9876.89000, 9872.45000, 9867.85000
                        )
                ),
                Arguments.of(
                        List.of(
                                9912.0, 9898.0, 9876.0, 9897.0, 9897.0,
                                9898.0, 9885.0, 9896.0, 9888.0, 9888.0,
                                9881.0, 9878.0, 9887.0, 9878.0, 9878.0,
                                9883.0, 9878.0, 9861.0, 9862.0, 9862.0
                        ),
                        4,
                        3,
                        List.of(
                                9912.00000, 9907.85185, 9902.70371, 9899.81956, 9895.94222,
                                9894.11955, 9893.53700, 9893.35700, 9892.96033, 9892.05000,
                                9890.52300, 9888.20200, 9886.06100, 9884.09700, 9882.46800,
                                9881.50900, 9880.74200, 9879.06400, 9876.15500, 9872.26600
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetLinearWeightedMovingAverages_withoutOrder")
    void getLinearWeightedMovingAverages_withOrderOne_withoutValueExtractor(
            List<Double> values,
            int window,
            List<Double> expectedValues
    ) {
        getLinearWeightedMovingAverages_withOrder_withoutValueExtractor(values, window, 1, expectedValues);
    }

    @ParameterizedTest
    @MethodSource("getData_forGetLinearWeightedMovingAverages_withoutOrder")
    void getLinearWeightedMovingAverages_withOrderOne_withValueExtractor(
            List<Double> values,
            int window,
            List<Double> expectedValues
    ) {
        getLinearWeightedMovingAverages_withOrder_withValueExtractor(values, window, 1, expectedValues);
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
                        List.of(),
                        0.8,
                        List.of()
                ),
                Arguments.of(
                        List.of(1000.0),
                        0.8,
                        List.of(1000.0)
                ),
                Arguments.of(
                        List.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0),
                        1.0,
                        List.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0)
                ),
                Arguments.of(
                        List.of(1000.0, 1000.0, 1000.0, 1000.0, 1000.0),
                        0.8,
                        List.of(1000.0, 1000.0, 1000.0, 1000.0, 1000.0)
                ),
                Arguments.of(
                        List.of(
                                1000.0, 2000.0, 3000.0, 4000.0, 5000.0,
                                6000.0, 7000.0, 8000.0, 9000.0, 10000.0
                        ),
                        0.8,
                        List.of(
                                1000.00000, 1800.00000, 2760.00000, 3752.00000, 4750.40000,
                                5750.08000, 6750.01600, 7750.00320, 8750.00064, 9750.00013
                        )
                ),
                Arguments.of(
                        List.of(
                                9912.0, 9898.0, 9876.0, 9897.0, 9897.0,
                                9898.0, 9885.0, 9896.0, 9888.0, 9888.0,
                                9881.0, 9878.0, 9887.0, 9878.0, 9878.0,
                                9883.0, 9878.0, 9861.0, 9862.0, 9862.0
                        ),
                        0.5,
                        List.of(
                                9912.00000, 9905.00000, 9890.50000, 9893.75000, 9895.37500,
                                9896.68750, 9890.84375, 9893.42188, 9890.71094, 9889.35547,
                                9885.17773, 9881.58887, 9884.29443, 9881.14722, 9879.57361,
                                9881.28680, 9879.64340, 9870.32170, 9866.16085, 9864.08043
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
                        List.of(),
                        0.8,
                        3,
                        List.of()
                ),
                Arguments.of(
                        List.of(1000.0),
                        0.8,
                        3,
                        List.of(1000.0)
                ),
                Arguments.of(
                        List.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0),
                        1,
                        3,
                        List.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0)
                ),
                Arguments.of(
                        List.of(1000.0, 1000.0, 1000.0, 1000.0, 1000.0),
                        0.8,
                        2,
                        List.of(1000.0, 1000.0, 1000.0, 1000.0, 1000.0)
                ),
                Arguments.of(
                        List.of(
                                1000.0, 2000.0, 3000.0, 4000.0, 5000.0,
                                6000.0, 7000.0, 8000.0, 9000.0, 10000.0
                        ),
                        0.8,
                        2,
                        List.of(
                                1000.00000, 1640.00000, 2536.00000, 3508.80000, 4502.08000,
                                5500.48000, 6500.10880, 7500.02432, 8500.00538, 9500.00118
                        )
                ),
                Arguments.of(
                        List.of(
                                1000.0, 2000.0, 3000.0, 4000.0, 5000.0,
                                6000.0, 7000.0, 8000.0, 9000.0, 10000.0
                        ),
                        0.8,
                        3,
                        List.of(
                                1000.00000, 1512.00000, 2331.20000, 3273.28000, 4256.32000,
                                5251.64800, 6250.41664, 7250.10278, 8250.02486, 9250.00591
                        )
                ),
                Arguments.of(
                        List.of(
                                9912.0, 9898.0, 9876.0, 9897.0, 9897.0,
                                9898.0, 9885.0, 9896.0, 9888.0, 9888.0,
                                9881.0, 9878.0, 9887.0, 9878.0, 9878.0,
                                9883.0, 9878.0, 9861.0, 9862.0, 9862.0
                        ),
                        0.5,
                        2,
                        List.of(
                                9912.00000, 9908.50000, 9899.50000, 9896.62500, 9896.00000,
                                9896.34375, 9893.59375, 9893.50781, 9892.10938, 9890.73242,
                                9887.95508, 9884.77197, 9884.53320, 9882.84021, 9881.20691,
                                9881.24686, 9880.44513, 9875.38342, 9870.77213, 9867.42628
                        )
                ),
                Arguments.of(
                        List.of(
                                9912.0, 9898.0, 9876.0, 9897.0, 9897.0,
                                9898.0, 9885.0, 9896.0, 9888.0, 9888.0,
                                9881.0, 9878.0, 9887.0, 9878.0, 9878.0,
                                9883.0, 9878.0, 9861.0, 9862.0, 9862.0
                        ),
                        0.5,
                        3,
                        List.of(
                                9912.00000, 9910.25000, 9904.87500, 9900.75000, 9898.37500,
                                9897.35938, 9895.47656, 9894.49219, 9893.30078, 9892.01660,
                                9889.98584, 9887.37891, 9885.95605, 9884.39813, 9882.80252,
                                9882.02469, 9881.23491, 9878.30916, 9874.54065, 9870.98346
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetExponentialWeightedMovingAverages_withoutOrder")
    void getExponentialWeightedMovingAverages_withOrderOne_andWithoutValueExtractor(
            List<Double> values,
            double weightDecrease,
            List<Double> expectedValues
    ) {
        getExponentialWeightedMovingAverages_withOrder_andWithoutValueExtractor(
                values,
                weightDecrease,
                1,
                expectedValues
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
    @MethodSource("getData_forGetExponentialWeightedMovingAverages_withoutOrder")
    void getExponentialWeightedMovingAverages_withOrderOne_andWithValueExtractor(
            List<Double> values,
            double weightDecrease,
            List<Double> expectedValues
    ) {
        getExponentialWeightedMovingAverages_withOrder_andWithValueExtractor(
                values,
                weightDecrease,
                1,
                expectedValues
        );
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
        List<OffsetDateTime> times = List.of(now, now.plusMinutes(1), now.plusMinutes(2), now.plusMinutes(2));
        List<Integer> localExtremesIndices = List.of(0, 2);

        List<Point> localExtremes = TrendUtils.getLocalExtremes(values, times, localExtremesIndices);

        List<Point> expectedLocalExtremes = List.of(
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
        final List<OffsetDateTime> times = List.of(startTime, startTime.plusMinutes(1));
        final List<BigDecimal> values = TestDataHelper.createBigDecimalsList(10.0);
        final List<Integer> localExtremes = List.of(0, 1);

        AssertUtils.assertThrowsWithMessage(
                () -> TrendUtils.getRestraintLines(times, values, localExtremes),
                IllegalArgumentException.class,
                "times and values must have same size"
        );
    }

    @Test
    void getRestraintLines_throwsIllegalArgumentException_whenValuesIsLongerThanTimes() {
        final List<OffsetDateTime> times = List.of(OffsetDateTime.now());
        final List<BigDecimal> values = TestDataHelper.createBigDecimalsList(10.0, 11.0);
        final List<Integer> localExtremes = List.of(0, 1);

        AssertUtils.assertThrowsWithMessage(
                () -> TrendUtils.getRestraintLines(times, values, localExtremes),
                IllegalArgumentException.class,
                "times and values must have same size"
        );
    }

    @Test
    void getRestraintLines_throwsIllegalArgumentException_whenLocalExtremesIsLongerThanTimes() {
        final List<OffsetDateTime> times = List.of(OffsetDateTime.now());
        final List<BigDecimal> values = TestDataHelper.createBigDecimalsList(10.0);
        final List<Integer> localExtremes = List.of(0, 1);

        AssertUtils.assertThrowsWithMessage(
                () -> TrendUtils.getRestraintLines(times, values, localExtremes),
                IllegalArgumentException.class,
                "localExtremes can't be longer than times and values"
        );
    }

    @Test
    void getRestraintLines_returnsEmptyList_whenLocalExtremesIsEmpty() {
        final OffsetDateTime startTime = OffsetDateTime.now();
        final List<OffsetDateTime> times = List.of(startTime, startTime.plusMinutes(1));
        final List<BigDecimal> values = TestDataHelper.createBigDecimalsList(10.0, 11.0);
        final List<Integer> localExtremes = List.of();

        List<List<Point>> restraintLines = TrendUtils.getRestraintLines(times, values, localExtremes);

        Assertions.assertTrue(restraintLines.isEmpty());
    }

    @Test
    void getRestraintLines_returnsEmptyList_whenThereIsSingleLocalExtremum() {
        final OffsetDateTime startTime = OffsetDateTime.now();
        final List<OffsetDateTime> times = List.of(startTime, startTime.plusMinutes(1));
        final List<BigDecimal> values = TestDataHelper.createBigDecimalsList(10.0, 11.0);
        final List<Integer> localExtremes = List.of(0);

        List<List<Point>> restraintLines = TrendUtils.getRestraintLines(times, values, localExtremes);

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
        final List<BigDecimal> values = TestDataHelper.createBigDecimalsList(
                10.0, 15.0, 14.0, 11.0, 12.0,
                13.0, 14.0, 14.0, 12.0, 16.0
        );
        final List<Integer> localExtremes = List.of(0, 3, 8);

        List<List<Point>> restraintLines = TrendUtils.getRestraintLines(times, values, localExtremes);

        Assertions.assertEquals(2, restraintLines.size());
        List<Point> expectedRestraintLine1 = List.of(
                Point.of(times.get(0), 10.00000),
                Point.of(times.get(1), 10.33333),
                Point.of(times.get(2), 10.66667),
                Point.of(times.get(3), 11.00000),
                Point.of(times.get(4), 11.33333),
                Point.of(times.get(5), 11.66667),
                Point.of(times.get(6), 12.00000)
        );
        AssertUtils.assertListsAreEqual(expectedRestraintLine1, restraintLines.get(0));

        List<Point> expectedRestraintLine2 = List.of(
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
                        List.of(),
                        List.of(),
                        List.of()
                ),
                Arguments.of(
                        TestDataHelper.createBigDecimalsList(10, 20, 30),
                        TestDataHelper.createBigDecimalsList(10, 20, 30),
                        List.of()
                ),
                Arguments.of(
                        TestDataHelper.createBigDecimalsList(10, 20, 30),
                        TestDataHelper.createBigDecimalsList(10, 20, 31),
                        List.of()
                ),
                Arguments.of(
                        TestDataHelper.createBigDecimalsList(10, 12, 13),
                        TestDataHelper.createBigDecimalsList(11, 10, 11),
                        List.of(1)
                ),
                Arguments.of(
                        TestDataHelper.createBigDecimalsList(10, 11, 12, 14, 15),
                        TestDataHelper.createBigDecimalsList(10, 12, 13, 12, 11),
                        List.of(3)
                ),
                Arguments.of(
                        TestDataHelper.createBigDecimalsList(10, 11, 12, 14, 15),
                        TestDataHelper.createBigDecimalsList(11, 12, 13, 12, 11),
                        List.of(3)
                ),
                Arguments.of(
                        TestDataHelper.createBigDecimalsList(10, 11, 12, 14, 15, 15, 16, 16),
                        TestDataHelper.createBigDecimalsList(11, 12, 13, 12, 11, 11, 11, 20),
                        List.of(3, 7)
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