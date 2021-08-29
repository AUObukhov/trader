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
                                1000.00000, 1400.00000, 2040.00000, 2824.00000, 3694.40000,
                                4616.64000, 5569.98400, 6541.99040, 7525.19424, 8515.11654
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
                                9912.00000, 9906.40000, 9894.24000, 9895.34400, 9896.00640,
                                9896.80384, 9892.08230, 9893.64938, 9891.38963, 9890.03378,
                                9886.42027, 9883.05216, 9884.63130, 9881.97878, 9880.38727,
                                9881.43236, 9880.05942, 9872.43565, 9868.26139, 9865.75683
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
                                1000.00000, 1160.00000, 1512.00000, 2036.80000, 2699.84000,
                                3466.56000, 4307.92960, 5201.55392, 6131.01005, 7084.65265
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
                                1000.00000, 1064.00000, 1243.20000, 1560.64000, 2016.32000,
                                2596.41600, 3281.02144, 4049.23443, 4881.94468, 5763.02787
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
                                9912.00000, 9909.76000, 9903.55200, 9900.26880, 9898.56384,
                                9897.85984, 9895.54883, 9894.78905, 9893.42928, 9892.07108,
                                9889.81075, 9887.10732, 9886.11691, 9884.46166, 9882.83190,
                                9882.27208, 9881.38702, 9877.80647, 9873.98844, 9870.69580
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
                                9912.00000, 9911.10400, 9908.08320, 9904.95744, 9902.40000,
                                9900.58394, 9898.56989, 9897.05755, 9895.60624, 9894.19218,
                                9892.43961, 9890.30669, 9888.63078, 9886.96313, 9885.31064,
                                9884.09522, 9883.01194, 9880.92975, 9878.15323, 9875.17025
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