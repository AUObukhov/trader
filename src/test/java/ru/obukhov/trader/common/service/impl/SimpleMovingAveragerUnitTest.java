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

class SimpleMovingAveragerUnitTest {

    private final SimpleMovingAverager averager = new SimpleMovingAverager();

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
        final List<BigDecimal> decimalValues = TestData.newBigDecimalList(values);

        final Executable executable = () -> averager.getAverages(decimalValues, window);
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
                                9912.00, 9905.00, 9895.333333333, 9895.75, 9892.00,
                                9892.00, 9894.25, 9894.000000000, 9891.75, 9889.25,
                                9888.25, 9883.75, 9883.500000000, 9881.00, 9880.25,
                                9881.50, 9879.25, 9875.000000000, 9871.00, 9865.75
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverages_withoutOrder")
    void getAverages_withoutOrder(final List<Double> values, final int window, final List<Double> expectedValues) {
        final List<BigDecimal> decimalValues = TestData.newBigDecimalList(values);

        final List<BigDecimal> movingAverages = averager.getAverages(decimalValues, window);

        final List<BigDecimal> quotationExpectedValues = TestData.newBigDecimalList(expectedValues);
        AssertUtils.assertEquals(quotationExpectedValues, movingAverages);
    }

    // endregion

    // region getAverages with order tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetAverages_withOrder_throwsIllegalArgumentException() {
        return Stream.of(
                Arguments.of(Collections.emptyList(), -1, 1, "window must be positive"),
                Arguments.of(Collections.emptyList(), 0, 1, "window must be positive"),
                Arguments.of(Collections.emptyList(), 1, -1, "order must be positive"),
                Arguments.of(Collections.emptyList(), 1, 0, "order must be positive")
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
        final List<BigDecimal> decimalValues = TestData.newBigDecimalList(values);

        final Executable executable = () -> averager.getAverages(decimalValues, window, order);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, expectedMessage);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetAverages_withOrder() {
        return Stream.of(
                Arguments.of(
                        List.of(),
                        4,
                        2,
                        List.of()
                ),
                Arguments.of(
                        List.of(),
                        4,
                        3,
                        List.of()
                ),

                Arguments.of(
                        List.of(1000.0),
                        4,
                        2,
                        List.of(1000.0)
                ),
                Arguments.of(
                        List.of(1000.0),
                        4,
                        3,
                        List.of(1000.0)
                ),

                Arguments.of(
                        List.of(1000.0, 2000.0),
                        4,
                        2,
                        List.of(1000.0, 1250.0)
                ),
                Arguments.of(
                        List.of(1000.0, 2000.0),
                        4,
                        3,
                        List.of(1000.0, 1125.0)
                ),

                Arguments.of(
                        List.of(1000.0),
                        1,
                        2,
                        List.of(1000.0)
                ),
                Arguments.of(
                        List.of(1000.0),
                        1,
                        3,
                        List.of(1000.0)
                ),
                Arguments.of(
                        List.of(1000.0, 2000.0, 3000.0, 4000.0),
                        4,
                        2,
                        List.of(1000.0, 1250.0, 1500.0, 1750.0)
                ),
                Arguments.of(
                        List.of(1000.0, 2000.0, 3000.0, 4000.0),
                        4,
                        3,
                        List.of(1000.0, 1125.0, 1250.0, 1375.0)
                ),

                Arguments.of(
                        List.of(1000.0, 2000.0, 3000.0),
                        5,
                        2,
                        List.of(1000.0, 1250.0, 1500.0)
                ),
                Arguments.of(
                        List.of(1000.0, 2000.0, 3000.0),
                        5,
                        3,
                        List.of(1000.0, 1125.0, 1250.0)
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
                        2,
                        List.of(1000.0, 2000.0, 3000.0, 4000.0, 5000.0)
                ),

                Arguments.of(
                        List.of(
                                1000.0, 2000.0, 3000.0, 4000.0, 5000.0,
                                6000.0, 7000.0, 8000.0, 9000.0, 10000.0
                        ),
                        4,
                        2,
                        List.of(
                                1000.00000, 1250.00000, 1500.00000, 1750.00000, 2375.00000,
                                3125.00000, 4000.00000, 5000.00000, 6000.00000, 7000.00000
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
                                1000.00000, 1125.00000, 1250.00000, 1375.00000, 1718.75000,
                                2187.50000, 2812.50000, 3625.00000, 4531.25000, 5500.00000
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
                                9912.000000000, 9908.50, 9904.111111111, 9902.020833333, 9897.020833333,
                                9893.770833333, 9893.50, 9893.062500000, 9893.000000000, 9892.312500000,
                                9890.812500000, 9888.25, 9886.187500000, 9884.125000000, 9882.125000000,
                                9881.562500000, 9880.50, 9879.000000000, 9876.687500000, 9872.750000000
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
                                9912.000000000, 9910.250000000, 9908.203703704, 9906.657986111, 9902.913194444,
                                9899.230902777, 9896.578124999, 9894.338541666, 9893.333333333, 9892.968750000,
                                9892.296875000, 9891.093750000, 9889.390625000, 9887.343750000, 9885.171875000,
                                9883.500000000, 9882.078125000, 9880.796875000, 9879.437500000, 9877.234375000
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverages_withoutOrder")
    void getAverages_withOrderOne(final List<Double> values, final int window, final List<Double> expectedValues) {
        final List<BigDecimal> decimalValues = TestData.newBigDecimalList(values);

        final List<BigDecimal> movingAverages = averager.getAverages(decimalValues, window, 1);

        final List<BigDecimal> quotationExpectedValues = TestData.newBigDecimalList(expectedValues);
        AssertUtils.assertEquals(quotationExpectedValues, movingAverages);
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverages_withOrder")
    void getAverages_withOrder(final List<Double> values, final int window, final int order, final List<Double> expectedValues) {
        final List<BigDecimal> decimalValues = TestData.newBigDecimalList(values);

        final List<BigDecimal> movingAverages = averager.getAverages(decimalValues, window, order);

        final List<BigDecimal> quotationExpectedValues = TestData.newBigDecimalList(expectedValues);
        AssertUtils.assertEquals(quotationExpectedValues, movingAverages);
    }

}