package ru.obukhov.trader.common.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestData;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

class ExponentialMovingAveragerUnitTest {

    private final ExponentialMovingAverager averager = new ExponentialMovingAverager();

    // region getAverages without order tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetAverages_withoutOrder_throwsIllegalArgumentException() {
        return Stream.of(
                Arguments.of(Collections.emptyList(), -1, "window must be positive"),
                Arguments.of(Collections.emptyList(), 0, "window must be positive")
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverages_withoutOrder_throwsIllegalArgumentException")
    void getAverages_withoutOrder_throwsIllegalArgumentException(final List<Double> values, final int window, final String expectedMessage) {
        final List<BigDecimal> bigDecimalValues = TestData.createBigDecimalsList(values);

        final Executable executable = () -> averager.getAverages(bigDecimalValues, window);
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetAverages_withoutOrder() {
        return Stream.of(
                Arguments.of(
                        List.of(),
                        2,
                        List.of()
                ),
                Arguments.of(
                        List.of(1000.0),
                        2,
                        List.of(1000.0)
                ),
                Arguments.of(
                        List.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0),
                        1,
                        List.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0)
                ),
                Arguments.of(
                        List.of(1000.0, 1000.0, 1000.0, 1000.0, 1000.0),
                        4,
                        List.of(1000.0, 1000.0, 1000.0, 1000.0, 1000.0)
                ),
                Arguments.of(
                        List.of(
                                1000.0, 2000.0, 3000.0, 4000.0, 5000.0,
                                6000.0, 7000.0, 8000.0, 9000.0, 10000.0
                        ),
                        4,
                        List.of(
                                1000.00000, 1400.00000, 2040.00000, 2824.00000, 3694.400000,
                                4616.64000, 5569.98400, 6541.99040, 7525.19424, 8515.116544
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
                                9912.000000, 9906.400000, 9894.240000, 9895.344000, 9896.006400,
                                9896.803840, 9892.082304, 9893.649382, 9891.389629, 9890.033777,
                                9886.420266, 9883.052160, 9884.631296, 9881.978778, 9880.387267,
                                9881.432360, 9880.059416, 9872.435650, 9868.261390, 9865.756834
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverages_withoutOrder")
    void getAverages_withoutOrder(final List<Double> values, final int window, final List<Double> expectedValues) {
        final List<BigDecimal> bigDecimalValues = TestData.createBigDecimalsList(values);

        final List<BigDecimal> movingAverages = averager.getAverages(bigDecimalValues, window);

        final List<BigDecimal> bigDecimalExpectedValues = TestData.createBigDecimalsList(expectedValues);
        AssertUtils.assertBigDecimalListsAreEqual(bigDecimalExpectedValues, movingAverages);

        for (final BigDecimal average : movingAverages) {
            Assertions.assertTrue(
                    DecimalUtils.DEFAULT_SCALE >= average.scale(),
                    "expected default scale for all averages"
            );
        }
    }

    // endregion

    // region getAverages with order tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetAverages_withOrder_throwsIllegalArgumentException() {
        return Stream.of(
                Arguments.of(Collections.emptyList(), -1, 3, "window must be positive"),
                Arguments.of(Collections.emptyList(), 0, 3, "window must be positive"),
                Arguments.of(Collections.emptyList(), 4, -1, "order must be positive"),
                Arguments.of(Collections.emptyList(), 4, 0, "order must be positive")
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverages_withOrder_throwsIllegalArgumentException")
    void getAverages_withOrder_throwsIllegalArgumentException(
            final List<Double> values,
            final int window,
            final int order,
            final String expectedMessage
    ) {
        final List<BigDecimal> bigDecimalValues = TestData.createBigDecimalsList(values);

        final Executable executable = () -> averager.getAverages(bigDecimalValues, window, order);
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetAverages_withOrder() {
        return Stream.of(
                Arguments.of(
                        List.of(),
                        2,
                        3,
                        List.of()
                ),
                Arguments.of(
                        List.of(1000.0),
                        2,
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
                        2,
                        2,
                        List.of(1000.0, 1000.0, 1000.0, 1000.0, 1000.0)
                ),
                Arguments.of(
                        List.of(
                                1000.0, 2000.0, 3000.0, 4000.0, 5000.0,
                                6000.0, 7000.0, 8000.0, 9000.0, 10000.0
                        ),
                        4,
                        2,
                        List.of(
                                1000.00000, 1160.00000, 1512.00000, 2036.800000, 2699.84000,
                                3466.56000, 4307.92960, 5201.55392, 6131.010048, 7084.652647
                        )
                ),
                Arguments.of(
                        List.of(
                                1000.0, 2000.0, 3000.0, 4000.0, 5000.0,
                                6000.0, 7000.0, 8000.0, 9000.0, 10000.0
                        ),
                        4,
                        3,
                        List.of(
                                1000.000, 1064.00000, 1243.200000, 1560.640000, 2016.320000,
                                2596.416, 3281.02144, 4049.234432, 4881.944678, 5763.027866
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
                        2,
                        List.of(
                                9912.000000, 9909.760000, 9903.552000, 9900.268800, 9898.563840,
                                9897.859840, 9895.548826, 9894.789049, 9893.429281, 9892.071080,
                                9889.810754, 9887.107316, 9886.116908, 9884.461656, 9882.831901,
                                9882.272085, 9881.387017, 9877.806470, 9873.988438, 9870.695797
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
                                9912.000000, 9911.104000, 9908.083200, 9904.957440, 9902.400000,
                                9900.583936, 9898.569892, 9897.057555, 9895.606245, 9894.192179,
                                9892.439609, 9890.306691, 9888.630778, 9886.963129, 9885.310637,
                                9884.095216, 9883.011937, 9880.929750, 9878.153225, 9875.170254
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverages_withoutOrder")
    void getAverages_withOrderOne(final List<Double> values, final int window, final List<Double> expectedValues) {
        getAverages_withOrder(values, window, 1, expectedValues);
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverages_withOrder")
    void getAverages_withOrder(final List<Double> values, final int window, final int order, final List<Double> expectedValues) {
        final List<BigDecimal> bigDecimalValues = TestData.createBigDecimalsList(values);

        final List<BigDecimal> movingAverages = averager.getAverages(bigDecimalValues, window, order);

        final List<BigDecimal> bigDecimalExpectedValues = TestData.createBigDecimalsList(expectedValues);
        AssertUtils.assertBigDecimalListsAreEqual(bigDecimalExpectedValues, movingAverages);

        for (final BigDecimal average : movingAverages) {
            Assertions.assertTrue(
                    DecimalUtils.DEFAULT_SCALE >= average.scale(),
                    "expected default scale for all averages"
            );
        }
    }

    // endregion

}