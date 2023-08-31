package ru.obukhov.trader.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

class MathUtilsUnitTest {

    // region getAverage with collection tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetAverage_withCollection() {
        return Stream.of(
                Arguments.of(List.of(), 0.0),
                Arguments.of(List.of(1000.0), 1000.0),
                Arguments.of(List.of(100.0, 200.0, 1000.0), 433.333333333)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverage_withCollection")
    void getAverage_withCollection(final List<Double> values, final Double expectedAverage) {
        final List<BigDecimal> bigDecimalValues = TestData.createBigDecimals(values);

        final BigDecimal average = MathUtils.getAverage(bigDecimalValues);

        AssertUtils.assertEquals(expectedAverage, average);
    }

    // endregion

    // region getAverage with VarArgs tests

    @Test
    void getAverage_withVarArgs_returnsZero_whenNoArguments() {
        final BigDecimal average = MathUtils.getAverage();

        AssertUtils.assertEquals(0, average);
    }

    @Test
    void getAverage_withVarArgs_returnsNumber_whenSingleArguments() {
        final BigDecimal number = DecimalUtils.setDefaultScale(10);

        final BigDecimal average = MathUtils.getAverage(number);

        AssertUtils.assertEquals(number, average);
    }

    @Test
    void getAverage_withVarArgs_returnsAverage_whenMultipleNumbersInCollection() {
        final BigDecimal average = MathUtils.getAverage(
                DecimalUtils.setDefaultScale(100),
                DecimalUtils.setDefaultScale(200),
                DecimalUtils.setDefaultScale(1000)
        );

        AssertUtils.assertEquals(433.333333333, average);
    }

    // endregion

    // region getWeightedAverage tests

    @Test
    void getWeightedAverage_throwsIllegalArgumentException_whenThereIsDateTimeAfterEndDateTime() {
        final Map<OffsetDateTime, Quotation> timestampsToAmounts = new HashMap<>();
        OffsetDateTime dateTime = DateTimeTestData.createDateTime(2021, 1, 1);
        Quotation amount = QuotationUtils.newQuotation(10000L);
        timestampsToAmounts.put(dateTime, amount);
        final Quotation increment = QuotationUtils.newQuotation(1000L);
        for (int i = 0; i < 24; i++) {
            dateTime = dateTime.plusDays(1);
            amount = QuotationUtils.add(amount, increment);
            timestampsToAmounts.put(dateTime, amount);
        }

        final OffsetDateTime endDateTime = DateTimeTestData.createDateTime(2021, 1, 24);

        final Executable executable = () -> MathUtils.getWeightedAverage(timestampsToAmounts, endDateTime);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "All dateTimes must be before endDateTime");
    }

    @Test
    void getWeightedAverage_returnsZero_whenCollectionIsEmpty() {
        final Map<OffsetDateTime, Quotation> dateTimesToAmounts = new HashMap<>();
        final OffsetDateTime endDateTime = DateTimeTestData.createDateTime(2021, 3, 10, 11, 12, 13);

        final Quotation weightedAverage = MathUtils.getWeightedAverage(dateTimesToAmounts, endDateTime);

        AssertUtils.assertEquals(0, weightedAverage);
    }

    @Test
    void getWeightedAverage_returnsProperValue_whenCollectionIsNotEmpty() {
        final Map<OffsetDateTime, Quotation> dateTimesToAmounts = new LinkedHashMap<>();
        OffsetDateTime dateTime = DateTimeTestData.createDateTime(2021, 1, 1);
        Quotation amount = QuotationUtils.newQuotation(10000L);
        dateTimesToAmounts.put(dateTime, amount);
        final Quotation increment = QuotationUtils.newQuotation(1000L);
        for (int i = 0; i < 24; i++) {
            dateTime = dateTime.plusDays(1);
            amount = QuotationUtils.add(amount, increment);
            dateTimesToAmounts.put(dateTime, amount);
        }

        final OffsetDateTime endDateTime = DateTimeTestData.createDateTime(2021, 1, 25);
        final Quotation weightedAverage = MathUtils.getWeightedAverage(dateTimesToAmounts, endDateTime);

        AssertUtils.assertEquals(17666.666666664, weightedAverage);
    }

    // endregion

    // region max tests

    @Test
    void max_returnsNull_whenValuesIsEmpty() {
        final Double max = MathUtils.max(Collections.emptyList());

        Assertions.assertNull(max);
    }

    @Test
    void max_returnsMaxValue_whenValuesIsNotEmpty() {
        final List<Double> values = List.of(-100d, 21d, 10d, 20d);

        Double max = MathUtils.max(values);

        Assertions.assertEquals(max, Double.valueOf(21));
    }

    // endregion

    // region min tests

    @Test
    void min_returnsNull_whenValuesIsEmpty() {
        final Double min = MathUtils.min(Collections.emptyList());

        Assertions.assertNull(min);
    }

    @Test
    void min_returnsMaxValue_whenValuesIsNotEmpty() {
        final List<Double> values = List.of(100d, -21d, 10d, 20d);

        final Double min = MathUtils.min(values);

        Assertions.assertEquals(min, Double.valueOf(-21));
    }

    // endregion

    private static Stream<Arguments> getData_for_divideRoundUp() {
        return Stream.of(
                Arguments.of(0L, 3L, 0L),
                Arguments.of(10L, 3L, 3L),
                Arguments.of(10L, 4L, 3L),
                Arguments.of(10L, 5L, 2L),
                Arguments.of(10L, 6L, 2L),
                Arguments.of(10L, 7L, 1L),
                Arguments.of(-10L, 3L, -3L),
                Arguments.of(-10L, 4L, -3L),
                Arguments.of(-10L, 5L, -2L),
                Arguments.of(-10L, 6L, -2L),
                Arguments.of(-10L, 7L, -1L),
                Arguments.of(-10L, -3L, 3L),
                Arguments.of(-10L, -4L, 3L),
                Arguments.of(-10L, -5L, 2L),
                Arguments.of(-10L, -6L, 2L),
                Arguments.of(-10L, -7L, 1L)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_for_divideRoundUp")
    void divideRoundUp(final long dividend, final long divisor, final long expectedResult) {
        final long actualResult = MathUtils.divideRoundUp(dividend, divisor);
        Assertions.assertEquals(expectedResult, actualResult);
    }

}