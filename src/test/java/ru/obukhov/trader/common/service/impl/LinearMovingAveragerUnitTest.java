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

class LinearMovingAveragerUnitTest {

    private final LinearMovingAverager averager = new LinearMovingAverager();

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
    static Stream<Arguments> getData_forGetAverages_withOrder_throwsIllegalArgumentException() {
        return Stream.of(
                Arguments.of(Collections.emptyList(), -1, "order must be positive"),
                Arguments.of(Collections.emptyList(), 0, "order must be positive")
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverages_withOrder_throwsIllegalArgumentException")
    void getAverages_withOrder_throwsIllegalArgumentException(final List<Double> values, final int order, final String expectedMessage) {
        List<BigDecimal> bigDecimalValues = TestData.createBigDecimalsList(values);

        final Executable executable = () -> averager.getAverages(bigDecimalValues, 1, order);
        Assertions.assertThrows(IllegalArgumentException.class, executable, expectedMessage);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetAverages_withoutOrder() {
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
    @MethodSource("getData_forGetAverages_withoutOrder")
    void getAverages_withoutOrder(final List<Double> values, final int window, final List<Double> expectedValues) {
        final List<BigDecimal> bigDecimalValues = TestData.createBigDecimalsList(values);

        final List<BigDecimal> movingAverages = averager.getAverages(bigDecimalValues, window);

        final List<BigDecimal> bigDecimalExpectedValues = TestData.createBigDecimalsList(expectedValues);
        AssertUtils.assertBigDecimalListsAreEqual(bigDecimalExpectedValues, movingAverages);

        for (final BigDecimal average : movingAverages) {
            Assertions.assertTrue(DecimalUtils.DEFAULT_SCALE >= average.scale(), "expected default scale for all averages");
        }
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetAverages_withOrder() {
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
            Assertions.assertTrue(DecimalUtils.DEFAULT_SCALE >= average.scale(), "expected default scale for all averages");
        }
    }

}