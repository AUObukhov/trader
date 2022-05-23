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
                                9912.000000000, 9906.400000000, 9894.240000000, 9895.344000000, 9896.006400000,
                                9896.803840000, 9892.082304000, 9893.649382400, 9891.389629440, 9890.033777664,
                                9886.420266598, 9883.052159959, 9884.631295975, 9881.978777585, 9880.387266551,
                                9881.432359931, 9880.059415959, 9872.435649575, 9868.261389745, 9865.756833847
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
        AssertUtils.assertEquals(bigDecimalExpectedValues, movingAverages);

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
                                1000.00000, 1160.00000, 1512.00000, 2036.800000, 2699.8400000,
                                3466.56000, 4307.92960, 5201.55392, 6131.010048, 7084.6526464
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
                                1000.000, 1064.00000, 1243.200000, 1560.6400000, 2016.3200000,
                                2596.416, 3281.02144, 4049.234432, 4881.9446784, 5763.0278656
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
                                9912.000000000, 9909.760000000, 9903.552000000, 9900.268800000, 9898.563840000,
                                9897.859840000, 9895.548825600, 9894.789048320, 9893.429280768, 9892.071079527,
                                9889.810754355, 9887.107316597, 9886.116908348, 9884.461656043, 9882.831900246,
                                9882.272084120, 9881.387016856, 9877.806469944, 9873.988437864, 9870.695796257
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
                                9912.000000000, 9911.104000000, 9908.083200000, 9904.957440000, 9902.400000000,
                                9900.583936000, 9898.569891840, 9897.057554432, 9895.606244966, 9894.192178791,
                                9892.439609017, 9890.306692049, 9888.630778568, 9886.963129558, 9885.310637833,
                                9884.095216348, 9883.011936551, 9880.929749909, 9878.153225091, 9875.170253558
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
        AssertUtils.assertEquals(bigDecimalExpectedValues, movingAverages);

        for (final BigDecimal average : movingAverages) {
            Assertions.assertTrue(
                    DecimalUtils.DEFAULT_SCALE >= average.scale(),
                    "expected default scale for all averages"
            );
        }
    }

    // endregion

}