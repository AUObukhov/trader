package ru.obukhov.trader.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

class DecimalUtilsUnitTest {

    // region newBigDecimal tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forCreateBigDecimal() {
        return Stream.of(
                Arguments.of(0, 0, 0),
                Arguments.of(0, 120_000_000, 0.12),
                Arguments.of(14, 0, 14.0),
                Arguments.of(13, 160_000_000, 13.16),
                Arguments.of(7, 10_000, 7.00001),
                Arguments.of(15, 666_666_666, 15.666_666_666)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forCreateBigDecimal")
    void createBigDecimal_fromUnitsAndNanos(final long units, final int nanos, final double expectedResult) {
        final BigDecimal result = DecimalUtils.newBigDecimal(units, nanos);

        AssertUtils.assertEquals(expectedResult, result);
    }

    @ParameterizedTest
    @MethodSource("getData_forCreateBigDecimal")
    void createBigDecimal_fromMoneyValue(final long units, final int nanos, final double expectedResult) {
        final MoneyValue moneyValue = TestData.newMoneyValue(units, nanos, Currencies.RUB);

        final BigDecimal result = DecimalUtils.newBigDecimal(moneyValue);

        AssertUtils.assertEquals(expectedResult, result);
    }

    @Test
    void createBigDecimal_fromMoneyValue_whenMoneyValueIsNull() {
        Assertions.assertNull(DecimalUtils.newBigDecimal(null));
    }

    // endregion

    // region getNano tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetNano() {
        return Stream.of(
                Arguments.of(0, 0),
                Arguments.of(0.12, 120_000_000),
                Arguments.of(14.0, 0),
                Arguments.of(13.000_000_001, 1),
                Arguments.of(13.666_666_666, 666_666_666)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetNano")
    void getNano(final double value, final int expectedNanos) {
        final int nanos = DecimalUtils.getNano(BigDecimal.valueOf(value));

        Assertions.assertEquals(expectedNanos, nanos);
    }

    // endregion

    // region add tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forToQuotation() {
        return Stream.of(
                Arguments.of(0, 0, 0),
                Arguments.of(0.12, 0, 120_000_000),
                Arguments.of(14.0, 14, 0),
                Arguments.of(13.000_000_001, 13, 1),
                Arguments.of(10.666_666_666, 10, 666_666_666)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forToQuotation")
    void toQuotation(final double value, final int expectedUnits, final int expectedNano) {
        final Quotation quotation = DecimalUtils.toQuotation(BigDecimal.valueOf(value));

        Assertions.assertEquals(expectedUnits, quotation.getUnits());
        Assertions.assertEquals(expectedNano, quotation.getNano());
    }

    // endregion

    // region add tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forAdd() {
        return Stream.of(
                Arguments.of(100.1, 5, 105.1),
                Arguments.of(100.0000000055, 7, 107.000000006)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forAdd")
    void subtract(final double addend1, final long addend2, final double expectedResult) {
        final BigDecimal result = DecimalUtils.add(BigDecimal.valueOf(addend1), addend2);

        AssertUtils.assertEquals(expectedResult, result);
    }

    // endregion

    // region subtract tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forSubtract() {
        return Stream.of(
                Arguments.of(100.1, 1.5, 98.6),
                Arguments.of(100.0000000055, 1.5, 98.500000006)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forSubtract")
    void subtract(final double minuend, final double subtrahend, final double expectedResult) {
        final BigDecimal result = DecimalUtils.subtract(BigDecimal.valueOf(minuend), subtrahend);

        AssertUtils.assertEquals(expectedResult, result);
    }

    // endregion

    // region multiply tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forMultiplyByDouble() {
        return Stream.of(
                Arguments.of(100.1, 1.5, 150.15),
                Arguments.of(100.0000055, 5.9, 590.000032450)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forMultiplyByDouble")
    void multiplyByBigDecimal(final double multiplier, final double multiplicand, final double expectedResult) {
        final BigDecimal result = DecimalUtils.multiply(BigDecimal.valueOf(multiplier), BigDecimal.valueOf(multiplicand));

        AssertUtils.assertEquals(expectedResult, result);
    }

    @ParameterizedTest
    @MethodSource("getData_forMultiplyByDouble")
    void multiplyByDouble(final double multiplier, final double multiplicand, final double expectedResult) {
        final BigDecimal result = DecimalUtils.multiply(BigDecimal.valueOf(multiplier), multiplicand);

        AssertUtils.assertEquals(expectedResult, result);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forMultiplyByInt() {
        return Stream.of(
                Arguments.of(100.1, 2, 200.2),
                Arguments.of(100.0000000015, 3, 300.000000005)
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forMultiplyByInt")
    void multiplyByInteger(final double multiplier, final int multiplicand, final double expectedResult) {
        final BigDecimal result = DecimalUtils.multiply(BigDecimal.valueOf(multiplier), multiplicand);

        AssertUtils.assertEquals(expectedResult, result);
    }

    // endregion

    // region divide tests

    @Test
    void divideBigDecimalByInteger() {
        final BigDecimal result = DecimalUtils.divide(BigDecimal.valueOf(100), 3);

        AssertUtils.assertEquals(33.333333333, result);
    }

    @Test
    void divideLongByBigDecimal() {
        final BigDecimal result = DecimalUtils.divide(100, BigDecimal.valueOf(3));

        AssertUtils.assertEquals(33.333333333, result);
    }

    @Test
    void divide() {
        final double dividend = 100.0000055;
        final double divisor = 0.0099;
        final double expectedResult = 10101.010656566;

        final BigDecimal divideBigDecimalByDoubleResult = DecimalUtils.divide(BigDecimal.valueOf(dividend), divisor);
        final BigDecimal divideDoubleByDoubleResult = DecimalUtils.divide(dividend, divisor);
        final BigDecimal divideBigDecimalByBigDecimal = DecimalUtils.divide(BigDecimal.valueOf(dividend), BigDecimal.valueOf(divisor));

        AssertUtils.assertEquals(expectedResult, divideBigDecimalByDoubleResult);
        AssertUtils.assertEquals(expectedResult, divideDoubleByDoubleResult);
        AssertUtils.assertEquals(expectedResult, divideBigDecimalByBigDecimal);
    }

    // endregion

    // region divideAccurate tests

    @Test
    void divideAccurateLongByBigDecimal() {
        final BigDecimal result = DecimalUtils.divideAccurate(100, BigDecimal.valueOf(3));

        AssertUtils.assertEquals(new BigDecimal("33.333333333333333"), result);
    }

    @Test
    void divideAccurate() {
        final double dividend = 100.0000055;
        final double divisor = 0.0099;
        final BigDecimal expectedResult = new BigDecimal("10101.010656565656566");

        final BigDecimal result = DecimalUtils.divideAccurate(BigDecimal.valueOf(dividend), BigDecimal.valueOf(divisor));

        AssertUtils.assertEquals(expectedResult, result);
    }

    // endregion

    @ParameterizedTest
    @CsvSource({
            "10, 5, 7.5",
            "-10, 10, 0",
            "-15, -10, -12.5",
            "10, 10, 10",
            "1.234567, 2.345678, 1.790122500",
            "1.234567, 9.87654321, 5.555555105"
    })
    void getAverage_withTwoNumbers(final BigDecimal value1, final BigDecimal value2, final BigDecimal expectedAverage) {
        final BigDecimal average = DecimalUtils.getAverage(value1, value2);

        AssertUtils.assertEquals(expectedAverage, average);
    }

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forGetAverage_withList() {
        return Stream.of(
                Arguments.of(List.of(1.234567), List.of(1), 1.234567),

                Arguments.of(List.of(10.0, 5.0), List.of(1, 0), 10),
                Arguments.of(List.of(-10.0, 10.0), List.of(1, 0), -10),
                Arguments.of(List.of(1.234567, 2.345678), List.of(1, 0), 1.234567),

                Arguments.of(List.of(10.0, 5.0), List.of(0, 1), 5),
                Arguments.of(List.of(-10.0, 10.0), List.of(0, 1), 10),
                Arguments.of(List.of(1.234567, 2.345678), List.of(0, 1), 2.345678),

                Arguments.of(List.of(10.0, 5.0), List.of(1, 1), 7.5),
                Arguments.of(List.of(-10.0, 10.0), List.of(1, 1), 0),
                Arguments.of(List.of(-15.0, -10.0), List.of(1, 1), -12.5),
                Arguments.of(List.of(10.0, 10.0), List.of(1, 1), 10),
                Arguments.of(List.of(1.234567, 2.345678), List.of(1, 1), 1.790122500),
                Arguments.of(List.of(1.234567, 9.87654321), List.of(1, 1), 5.555555105),

                Arguments.of(List.of(10.0, 5.0), List.of(14, 14), 7.5),
                Arguments.of(List.of(-10.0, 10.0), List.of(14, 14), 0),
                Arguments.of(List.of(-15.0, -10.0), List.of(14, 14), -12.5),
                Arguments.of(List.of(10.0, 10.0), List.of(14, 14), 10),
                Arguments.of(List.of(1.234567, 2.345678), List.of(14, 14), 1.790122500),
                Arguments.of(List.of(1.234567, 9.87654321), List.of(14, 14), 5.555555105),

                Arguments.of(List.of(10.0, 5.0), List.of(14, 3), 9.117647059),
                Arguments.of(List.of(-10.0, 10.0), List.of(14, 3), -6.470588235),
                Arguments.of(List.of(-15.0, -10.0), List.of(14, 3), -14.117647059),
                Arguments.of(List.of(10.0, 10.0), List.of(14, 3), 10),
                Arguments.of(List.of(1.234567, 2.345678), List.of(14, 3), 1.430645412),

                Arguments.of(
                        List.of(65.75, 58.51, -96.37, -57.45, -63.09, -28.95, -92.69, 41.09, 55.55, -18.76),
                        List.of(12, 14, 47, 14, 54, 30, 39, 66, 90, 30),
                        -11.280757576
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forGetAverage_withList")
    void getAverage_withList(final List<Double> values, final List<Integer> weights, final double expectedAverage) {
        final List<BigDecimal> bigDecimalValues = values.stream().map(DecimalUtils::setDefaultScale).toList();
        final BigDecimal average = DecimalUtils.getAverage(bigDecimalValues, weights);

        AssertUtils.assertEquals(expectedAverage, average);
    }

    // region addFraction tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forAddFraction() {
        return Stream.of(
                Arguments.of(DecimalUtils.setDefaultScale(765), DecimalUtils.setDefaultScale(0.003), DecimalUtils.setDefaultScale(767.295)),
                Arguments.of(DecimalUtils.setDefaultScale(765), DecimalUtils.ONE, DecimalUtils.setDefaultScale(1530)),
                Arguments.of(DecimalUtils.setDefaultScale(0.00000001), DecimalUtils.setDefaultScale(0.5), DecimalUtils.setDefaultScale(0.000000015)),
                Arguments.of(DecimalUtils.setDefaultScale(0.000000001), DecimalUtils.setDefaultScale(-0.6), DecimalUtils.ZERO),
                Arguments.of(DecimalUtils.setDefaultScale(0.000000001), DecimalUtils.setDefaultScale(0.4), DecimalUtils.setDefaultScale(0.000000001))
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forAddFraction")
    void addFraction(final BigDecimal number, final BigDecimal fraction, final BigDecimal expectedResult) {
        final BigDecimal actualResult = DecimalUtils.addFraction(number, fraction);

        AssertUtils.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region subtractFraction tests

    @SuppressWarnings("unused")
    static Stream<Arguments> getData_forSubtractFraction() {
        return Stream.of(
                Arguments.of(DecimalUtils.setDefaultScale(765), DecimalUtils.setDefaultScale(0.003), DecimalUtils.setDefaultScale(762.705)),
                Arguments.of(DecimalUtils.setDefaultScale(765), DecimalUtils.ONE, DecimalUtils.ZERO),
                Arguments.of(DecimalUtils.setDefaultScale(0.00000001), DecimalUtils.setDefaultScale(0.5), DecimalUtils.setDefaultScale(0.000000005)),
                Arguments.of(DecimalUtils.setDefaultScale(0.000000001), DecimalUtils.setDefaultScale(0.6), DecimalUtils.ZERO),
                Arguments.of(DecimalUtils.setDefaultScale(0.000000001), DecimalUtils.setDefaultScale(0.5), DecimalUtils.setDefaultScale(0.000000001))
        );
    }

    @ParameterizedTest
    @MethodSource("getData_forSubtractFraction")
    void subtractFraction(final BigDecimal number, final BigDecimal fraction, final BigDecimal expectedResult) {
        final BigDecimal actualResult = DecimalUtils.subtractFraction(number, fraction);

        AssertUtils.assertEquals(expectedResult, actualResult);
    }

    // endregion

    @Test
    void getFractionDifference() {
        final BigDecimal result = DecimalUtils.getFractionDifference(BigDecimal.valueOf(765), BigDecimal.valueOf(762.705));

        AssertUtils.assertEquals(0.003009027, result);
    }

    // region setDefaultScale with BigDecimal tests

    @Test
    @SuppressWarnings("ConstantConditions")
    void setDefaultScale_withBigDecimal_returnsNull_whenNumberIsNull() {
        final BigDecimal number = null;

        Assertions.assertNull(DecimalUtils.setDefaultScale(number));
    }

    @ParameterizedTest
    @CsvSource({
            "10, -1",
            "10, 2",
            "10, 6"
    })
    void setDefaultScale_withBigDecimal(final long unscaledVal, final int scale) {
        final BigDecimal number = BigDecimal.valueOf(unscaledVal, scale);

        final BigDecimal result = DecimalUtils.setDefaultScale(number);

        Assertions.assertEquals(DecimalUtils.DEFAULT_SCALE, result.scale());
    }

    // endregion

    // region setDefaultScale with double tests

    @Test
    void setDefaultScale_withDouble_returnsNull_whenNumberIsNull() {
        Assertions.assertNull(DecimalUtils.setDefaultScale((Double) null));
    }

    @ParameterizedTest
    @CsvSource({
            "10.01, 10.01",
            "10.0000000001, 10",
            "10.0000000005, 10.000000001"
    })
    void setDefaultScale_withDouble(final Double number, final double expectedValue) {
        final BigDecimal result = DecimalUtils.setDefaultScale(number);

        Assertions.assertEquals(DecimalUtils.DEFAULT_SCALE, result.scale());
        AssertUtils.assertEquals(expectedValue, result);
    }

    // endregion

    // region setDefaultScale with long tests

    @Test
    void setDefaultScale_withLong_returnsNull_whenNumberIsNull() {
        Assertions.assertNull(DecimalUtils.setDefaultScale((Long) null));
    }

    @Test
    void setDefaultScale_withLong_setsDefaultScale() {
        final Long number = 10L;

        final BigDecimal result = DecimalUtils.setDefaultScale(number);

        Assertions.assertEquals(DecimalUtils.DEFAULT_SCALE, result.scale());
        AssertUtils.assertEquals(10, result);
    }

    // endregion

    // region setDefaultScale with Integer tests

    @Test
    void setDefaultScale_withInteger_returnsNull_whenNumberIsNull() {
        Assertions.assertNull(DecimalUtils.setDefaultScale((Integer) null));
    }

    @Test
    void setDefaultScale_withInteger_setsDefaultScale() {
        final Integer number = 10;

        final BigDecimal result = DecimalUtils.setDefaultScale(number);

        Assertions.assertEquals(DecimalUtils.DEFAULT_SCALE, result.scale());
        AssertUtils.assertEquals(10, result);
    }

    // endregion


    // region numbersEqual tests

    @ParameterizedTest
    @CsvSource({
            ", , true",
            ", 100, false",
            "100, , false",
            "100, 100, true",
            "11, 100, false"
    })
    void numbersEqual_withBigDecimal(final BigDecimal value1, final BigDecimal value2, final boolean expectedResult) {
        final boolean result = DecimalUtils.numbersEqual(value1, value2);

        Assertions.assertEquals(expectedResult, result);
    }

    @ParameterizedTest
    @CsvSource({
            "100, 100, true",
            "11, 100, false",
    })
    void numbersEqual_withInt(final BigDecimal value1, final int value2, final boolean expectedResult) {
        final boolean result = DecimalUtils.numbersEqual(value1, value2);

        Assertions.assertEquals(expectedResult, result);
    }

    @ParameterizedTest
    @CsvSource({
            "100, 100, true",
            "11, 100, false",
    })
    void numbersEqual_withDouble(final BigDecimal value1, final double value2, final boolean expectedResult) {
        final boolean result = DecimalUtils.numbersEqual(value1, value2);

        Assertions.assertEquals(expectedResult, result);
    }

    // endregion

    @ParameterizedTest
    @CsvSource(value = {
            "null, null",
            "0, 0",
            "100, 1E+2",
            "12.340000000, 12.34"
    },
            nullValues = "null")
    void stripTrailingZerosSafe(final BigDecimal value, final BigDecimal expectedResult) {
        final BigDecimal result = DecimalUtils.stripTrailingZerosSafe(value);

        Assertions.assertEquals(expectedResult, result);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "null, null",
            "0, 0",
            "100.0, 100",
            "12.340000000, 12.34"
    },
            nullValues = "null")
    void toPrettyStringSafe(final BigDecimal value, final String expectedResult) {
        final String result = DecimalUtils.toPrettyStringSafe(value);

        Assertions.assertEquals(expectedResult, result);
    }

}