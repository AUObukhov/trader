package ru.obukhov.trader.common.service.impl;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;

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
        final List<BigDecimal> quotationValues = TestData.createBigDecimals(values);

        final Executable executable = () -> averager.getAverages(quotationValues, window);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
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
        final List<BigDecimal> decimalValues = TestData.createBigDecimals(values);

        final Executable executable = () -> averager.getAverages(decimalValues, 1, order);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
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
                        List.of(1000.0, 1666.666666667)
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
                                9912.000000, 9902.666666667, 9889.333333333, 9892.4, 9892.9,
                                9895.300000, 9892.500000000, 9893.200000000, 9890.8, 9889.3,
                                9886.000000, 9881.900000000, 9883.200000000, 9881.0, 9879.8,
                                9880.900000, 9879.500000000, 9872.200000000, 9867.0, 9863.4
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverages_withoutOrder")
    void getAverages_withoutOrder(final List<Double> values, final int window, final List<Double> expectedValues) {
        final List<BigDecimal> decimalValues = TestData.createBigDecimals(values);

        final List<BigDecimal> movingAverages = averager.getAverages(decimalValues, window);

        final List<BigDecimal> quotationExpectedValues = TestData.createBigDecimals(expectedValues);
        AssertUtils.assertEquals(quotationExpectedValues, movingAverages);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetAverages_withOrder() {
        return Stream.of(
                Arguments.of(List.of(), 4, 2, List.of()),
                Arguments.of(List.of(), 4, 10, List.of()),

                Arguments.of(List.of(1000.0), 4, 2, List.of(1000.0)),
                Arguments.of(List.of(1000.0), 4, 10, List.of(1000.0)),

                Arguments.of(List.of(1000.0, 2000.0), 4, 2, List.of(1000.0, 1444.444444445)),
                Arguments.of(List.of(1000.0, 2000.0), 4, 10, List.of(1000.0, 1017.341529916)),

                Arguments.of(List.of(1000.0), 1, 1, List.of(1000.0)),
                Arguments.of(List.of(1000.0), 1, 2, List.of(1000.0)),
                Arguments.of(List.of(1000.0), 1, 10, List.of(1000.0)),

                Arguments.of(
                        List.of(1000.0, 3000.0, 2000.0, 4000.0),
                        4,
                        2,
                        List.of(1000.0, 1888.888888889, 2027.777777778, 2376.666666667)
                ),
                Arguments.of(
                        List.of(1000.0, 3000.0, 2000.0, 4000.0),
                        4,
                        3,
                        List.of(1000.0, 1592.592592593, 1810.185185185, 2036.777777778)
                ),

                Arguments.of(
                        List.of(1000.0, 3000.0, 2000.0),
                        4,
                        2,
                        List.of(1000.0, 1888.888888889, 2027.777777778)
                ),
                Arguments.of(
                        List.of(1000.0, 3000.0, 2000.0),
                        4,
                        3,
                        List.of(1000.0, 1592.592592593, 1810.185185185)
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
                                9912.000000000, 9905.777777778, 9897.555555556, 9895.493333333, 9893.013333333,
                                9893.403333333, 9893.410000000, 9893.380000000, 9892.310000000, 9890.850000000,
                                9888.670000000, 9885.500000000, 9883.980000000, 9882.340000000, 9881.050000000,
                                9880.820000000, 9880.130000000, 9876.890000000, 9872.450000000, 9867.850000000
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
                                9912.000000000, 9907.851851852, 9902.703703704, 9899.819555556, 9895.942222222,
                                9894.119555555, 9893.537000000, 9893.357000000, 9892.960333333, 9892.050000000,
                                9890.523000000, 9888.202000000, 9886.061000000, 9884.097000000, 9882.468000000,
                                9881.509000000, 9880.742000000, 9879.064000000, 9876.155000000, 9872.266000000
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
        final List<BigDecimal> decimalValues = TestData.createBigDecimals(values);

        final List<BigDecimal> movingAverages = averager.getAverages(decimalValues, window, order);

        final List<BigDecimal> quotationExpectedValues = TestData.createBigDecimals(expectedValues);
        AssertUtils.assertEquals(quotationExpectedValues, movingAverages);
    }

}