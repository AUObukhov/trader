package ru.obukhov.trader.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.math.BigDecimal;
import java.util.stream.Stream;

class DecimalUtilsUnitTest {

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
            "10, 10, 10",
            "1.234567, 2.345678, 1.790122500",
            "1.234567, 9.87654321, 5.555555105"
    })
    void getAverage(BigDecimal value1, BigDecimal value2, BigDecimal expectedAverage) {
        final BigDecimal average = DecimalUtils.getAverage(value1, value2);

        AssertUtils.assertEquals(expectedAverage, average);
    }

    @ParameterizedTest
    @CsvSource({"7.8,2.6,3", "7.9, 2.6, 3", "10.3, 2.6, 3"})
    void getIntegerQuotient(BigDecimal dividend, BigDecimal divisor, int expectedQuotient) {
        final int result = DecimalUtils.getIntegerQuotient(dividend, divisor);

        Assertions.assertEquals(expectedQuotient, result);
    }

    @Test
    void getFraction() {
        final BigDecimal result = DecimalUtils.getFraction(BigDecimal.valueOf(765), 0.003);

        AssertUtils.assertEquals(2.295, result);
    }

    @Test
    void addFraction() {
        final BigDecimal result = DecimalUtils.addFraction(BigDecimal.valueOf(765), 0.003);

        AssertUtils.assertEquals(767.295, result);
    }

    @Test
    void subtractFraction() {
        final BigDecimal result = DecimalUtils.subtractFraction(BigDecimal.valueOf(765), 0.003);

        AssertUtils.assertEquals(762.705, result);
    }

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
    void setDefaultScale_withBigDecimal(long unscaledVal, int scale) {
        final BigDecimal number = BigDecimal.valueOf(unscaledVal, scale);

        final BigDecimal result = DecimalUtils.setDefaultScale(number);

        Assertions.assertEquals(DecimalUtils.DEFAULT_SCALE, result.scale());
    }

    // endregion

    // region setDefaultScale with double tests

    @Test
    void setDefaultScale_withDouble_returnsNull_whenNumberIsNull() {
        final Double number = null;

        Assertions.assertNull(DecimalUtils.setDefaultScale(number));
    }

    @ParameterizedTest
    @CsvSource({
            "10.01, 10.01",
            "10.0000000001, 10",
            "10.0000000005, 10.000000001"
    })
    void setDefaultScale_withDouble(Double number, double expectedValue) {
        final BigDecimal result = DecimalUtils.setDefaultScale(number);

        Assertions.assertEquals(DecimalUtils.DEFAULT_SCALE, result.scale());
        AssertUtils.assertEquals(BigDecimal.valueOf(expectedValue), result);
    }

    // endregion

    // region setDefaultScale with long tests

    @Test
    void setDefaultScale_withLong_returnsNull_whenNumberIsNull() {
        final Long number = null;

        Assertions.assertNull(DecimalUtils.setDefaultScale(number));
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
        final Integer number = null;

        Assertions.assertNull(DecimalUtils.setDefaultScale(number));
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
    void numbersEqual_withBigDecimal(BigDecimal value1, BigDecimal value2, boolean expectedResult) {
        final boolean result = DecimalUtils.numbersEqual(value1, value2);

        Assertions.assertEquals(expectedResult, result);
    }

    @ParameterizedTest
    @CsvSource({
            "100, 100, true",
            "11, 100, false",
    })
    void numbersEqual_withInt(BigDecimal value1, int value2, boolean expectedResult) {
        final boolean result = DecimalUtils.numbersEqual(value1, value2);

        Assertions.assertEquals(expectedResult, result);
    }

    @ParameterizedTest
    @CsvSource({
            "100, 100, true",
            "11, 100, false",
    })
    void numbersEqual_withDouble(BigDecimal value1, double value2, boolean expectedResult) {
        final boolean result = DecimalUtils.numbersEqual(value1, value2);

        Assertions.assertEquals(expectedResult, result);
    }

    // endregion

}