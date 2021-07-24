package ru.obukhov.trader.common.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Candle;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.TestDataHelper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

class SimpleMovingAveragerUnitTest {

    private final SimpleMovingAverager averager = new SimpleMovingAverager();

    @Test
    void getType_returnsSimple() {
        Assertions.assertEquals(MovingAverageType.SIMPLE, averager.getType());
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetAverages_withoutOrder_throwsIllegalArgumentException() {
        return Stream.of(
                Arguments.of(Collections.emptyList(), -1, "window must be positive"),
                Arguments.of(Collections.emptyList(), 0, "window must be positive")
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverages_withoutOrder_throwsIllegalArgumentException")
    void getAverages_withoutOrder_withoutExtractors_throwsIllegalArgumentException(
            final List<Double> values,
            final int window,
            final String expectedMessage
    ) {
        final List<BigDecimal> bigDecimalValues = TestDataHelper.getBigDecimalValues(values);

        AssertUtils.assertThrowsWithMessage(
                () -> averager.getAverages(bigDecimalValues, window),
                IllegalArgumentException.class,
                expectedMessage
        );
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
                                9912.00000, 9905.00000, 9895.33333, 9895.75000, 9892.00000,
                                9892.00000, 9894.25000, 9894.00000, 9891.75000, 9889.25000,
                                9888.25000, 9883.75000, 9883.50000, 9881.00000, 9880.25000,
                                9881.50000, 9879.25000, 9875.00000, 9871.00000, 9865.75000
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverages_withoutOrder")
    void getAverages_withoutOrder_withoutExtractors(
            final List<Double> values,
            final int window,
            final List<Double> expectedValues
    ) {
        final List<BigDecimal> bigDecimalValues = TestDataHelper.getBigDecimalValues(values);

        final List<BigDecimal> movingAverages = averager.getAverages(bigDecimalValues, window);

        final List<BigDecimal> bigDecimalExpectedValues = TestDataHelper.getBigDecimalValues(expectedValues);
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
                Arguments.of(Collections.emptyList(), -1, 1, "window must be positive"),
                Arguments.of(Collections.emptyList(), 0, 1, "window must be positive"),
                Arguments.of(Collections.emptyList(), 1, -1, "order must be positive"),
                Arguments.of(Collections.emptyList(), 1, 0, "order must be positive")
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverages_withOrder_throwsIllegalArgumentException")
    void getAverages_withOrder_withValueExtractor_throwsIllegalArgumentException(
            final List<Double> values,
            final int window,
            final int order,
            final String expectedMessage
    ) {
        final List<Optional<BigDecimal>> elements = TestDataHelper.getOptionalBigDecimalValues(values);

        AssertUtils.assertThrowsWithMessage(
                () -> averager.getAverages(elements, Optional::get, window, order),
                IllegalArgumentException.class,
                expectedMessage
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverages_withOrder_throwsIllegalArgumentException")
    void getAverages_withOrder_withValueExtractors_throwsIllegalArgumentException(
            final List<Double> values,
            final int window,
            final int order,
            final String expectedMessage
    ) {
        final List<Candle> candles = new ArrayList<>(values.size());
        final OffsetDateTime now = OffsetDateTime.now();
        for (int i = 0; i < values.size(); i++) {
            final Candle candle = TestDataHelper.createCandleWithOpenPriceAndTime(values.get(i), now.plusMinutes(i));
            candles.add(candle);
        }

        AssertUtils.assertThrowsWithMessage(
                () -> averager.getAverages(candles, Candle::getOpenPrice, window, order),
                IllegalArgumentException.class,
                expectedMessage
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverages_withOrder_throwsIllegalArgumentException")
    void getAverages_withOrder_withoutExtractors_throwsIllegalArgumentException(
            final List<Double> values,
            final int window,
            final int order,
            final String expectedMessage
    ) {
        final List<BigDecimal> bigDecimalValues = TestDataHelper.getBigDecimalValues(values);

        AssertUtils.assertThrowsWithMessage(
                () -> averager.getAverages(bigDecimalValues, window, order),
                IllegalArgumentException.class,
                expectedMessage
        );
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
                                9912.00000, 9908.50000, 9904.11111, 9902.02083, 9897.02083,
                                9893.77083, 9893.50000, 9893.06250, 9893.00000, 9892.31250,
                                9890.81250, 9888.25000, 9886.18750, 9884.12500, 9882.12500,
                                9881.56250, 9880.50000, 9879.00000, 9876.68750, 9872.75000
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
                                9912.00000, 9910.25000, 9908.20370, 9906.65799, 9902.91320,
                                9899.23091, 9896.57813, 9894.33855, 9893.33334, 9892.96876,
                                9892.29689, 9891.09376, 9889.39064, 9887.34376, 9885.17188,
                                9883.50001, 9882.07813, 9880.79688, 9879.43751, 9877.23438
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverages_withoutOrder")
    void getAverages_withOrderOne_withValueExtractor(
            final List<Double> values,
            final int window,
            final List<Double> expectedValues
    ) {
        final List<Optional<BigDecimal>> elements = TestDataHelper.getOptionalBigDecimalValues(values);

        final List<BigDecimal> movingAverages =
                averager.getAverages(elements, Optional::get, window, 1);

        final List<BigDecimal> bigDecimalExpectedValues = TestDataHelper.getBigDecimalValues(expectedValues);
        AssertUtils.assertBigDecimalListsAreEqual(bigDecimalExpectedValues, movingAverages);

        for (final BigDecimal average : movingAverages) {
            Assertions.assertTrue(
                    DecimalUtils.DEFAULT_SCALE >= average.scale(),
                    "expected default scale for all averages"
            );
        }
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverages_withOrder")
    void getAverages_withOrder_withValueExtractor(
            final List<Double> values,
            final int window,
            final int order,
            final List<Double> expectedValues
    ) {
        final List<Optional<BigDecimal>> elements = TestDataHelper.getOptionalBigDecimalValues(values);

        final List<BigDecimal> movingAverages =
                averager.getAverages(elements, Optional::get, window, order);

        final List<BigDecimal> bigDecimalExpectedValues = TestDataHelper.getBigDecimalValues(expectedValues);
        AssertUtils.assertBigDecimalListsAreEqual(bigDecimalExpectedValues, movingAverages);

        for (final BigDecimal average : movingAverages) {
            Assertions.assertTrue(
                    DecimalUtils.DEFAULT_SCALE >= average.scale(),
                    "expected default scale for all averages"
            );
        }
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverages_withoutOrder")
    void getAverages_withOrderOne_withoutValueExtractor(
            final List<Double> values,
            final int window,
            final List<Double> expectedValues
    ) {
        final List<BigDecimal> bigDecimalValues = TestDataHelper.getBigDecimalValues(values);

        final List<BigDecimal> movingAverages = averager.getAverages(bigDecimalValues, window, 1);

        final List<BigDecimal> bigDecimalExpectedValues = TestDataHelper.getBigDecimalValues(expectedValues);
        AssertUtils.assertBigDecimalListsAreEqual(bigDecimalExpectedValues, movingAverages);

        for (final BigDecimal average : movingAverages) {
            Assertions.assertTrue(
                    DecimalUtils.DEFAULT_SCALE >= average.scale(),
                    "expected default scale for all averages"
            );
        }
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverages_withOrder")
    void getAverages_withOrder_withoutValueExtractor(
            final List<Double> values,
            final int window,
            final int order,
            final List<Double> expectedValues
    ) {
        final List<BigDecimal> bigDecimalValues = TestDataHelper.getBigDecimalValues(values);

        final List<BigDecimal> movingAverages = averager.getAverages(bigDecimalValues, window, order);

        final List<BigDecimal> bigDecimalExpectedValues = TestDataHelper.getBigDecimalValues(expectedValues);
        AssertUtils.assertBigDecimalListsAreEqual(bigDecimalExpectedValues, movingAverages);

        for (final BigDecimal average : movingAverages) {
            Assertions.assertTrue(
                    DecimalUtils.DEFAULT_SCALE >= average.scale(),
                    "expected default scale for all averages"
            );
        }
    }

}