package ru.obukhov.trader.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.math.BigDecimal;

class DecimalUtilsUnitTest {

    @Test
    void subtract() {
        BigDecimal result = DecimalUtils.subtract(BigDecimal.valueOf(100.1), 1.5);

        AssertUtils.assertEquals(BigDecimal.valueOf(98.6), result);
    }

    // region multiply tests

    @Test
    void multiplyByDouble() {
        BigDecimal result = DecimalUtils.multiply(BigDecimal.valueOf(100.1), 1.5);

        AssertUtils.assertEquals(BigDecimal.valueOf(150.15), result);
    }

    @Test
    void multiplyByInteger() {
        BigDecimal result = DecimalUtils.multiply(BigDecimal.valueOf(100.1), 2);

        AssertUtils.assertEquals(BigDecimal.valueOf(200.2), result);
    }

    // endregion

    // region divide tests

    @Test
    void divideBigDecimalByInteger() {
        BigDecimal result = DecimalUtils.divide(BigDecimal.valueOf(100), 3);

        AssertUtils.assertEquals(BigDecimal.valueOf(33.33333), result);
    }

    @Test
    void divideBigDecimalByDouble() {
        BigDecimal result = DecimalUtils.divide(BigDecimal.valueOf(100), 3.5);

        AssertUtils.assertEquals(BigDecimal.valueOf(28.57143), result);
    }

    @Test
    void divideDoubleByDouble() {
        BigDecimal result = DecimalUtils.divide(100., 3.5);

        AssertUtils.assertEquals(BigDecimal.valueOf(28.57143), result);
    }

    @Test
    void divideBigDecimalByBigDecimal() {
        BigDecimal result = DecimalUtils.divide(BigDecimal.valueOf(100), BigDecimal.valueOf(3.5));

        AssertUtils.assertEquals(BigDecimal.valueOf(28.57143), result);
    }

    // endregion

    @ParameterizedTest
    @CsvSource({
            "10, 5, 7.5",
            "-10, 10, 0",
            "10, 10, 10",
            "1.234567, 2.345678, 1.79012",
            "1.234567, 9.87654321, 5.55556"
    })
    void getAverage(BigDecimal value1, BigDecimal value2, BigDecimal expectedAverage) {
        BigDecimal average = DecimalUtils.getAverage(value1, value2);

        AssertUtils.assertEquals(expectedAverage, average);
    }

    @ParameterizedTest
    @CsvSource({"7.8,2.6,3", "7.9, 2.6, 3", "10.3, 2.6, 3"})
    void getIntegerQuotient(BigDecimal dividend, BigDecimal divisor, int expectedQuotient) {
        int result = DecimalUtils.getIntegerQuotient(dividend, divisor);

        Assertions.assertEquals(expectedQuotient, result);
    }

    @Test
    void getFraction() {
        BigDecimal result = DecimalUtils.getFraction(BigDecimal.valueOf(765), 0.003);

        AssertUtils.assertEquals(BigDecimal.valueOf(2.295), result);
    }

    @Test
    void addFraction() {
        BigDecimal result = DecimalUtils.addFraction(BigDecimal.valueOf(765), 0.003);

        AssertUtils.assertEquals(BigDecimal.valueOf(767.295), result);
    }

    @Test
    void subtractFraction() {
        BigDecimal result = DecimalUtils.subtractFraction(BigDecimal.valueOf(765), 0.003);

        AssertUtils.assertEquals(BigDecimal.valueOf(762.705), result);
    }

    @Test
    void getFractionDifference() {
        BigDecimal result = DecimalUtils.getFractionDifference(BigDecimal.valueOf(765), BigDecimal.valueOf(762.705));

        AssertUtils.assertEquals(BigDecimal.valueOf(0.00301), result);
    }

    // region setDefaultScale with BigDecimal tests

    @Test
    void setDefaultScale_withBigDecimal_returnsNull_whenNumberIsNull() {
        BigDecimal number = null;

        Assertions.assertNull(DecimalUtils.setDefaultScale(number));
    }

    @ParameterizedTest
    @CsvSource({
            "10, -1, 0",
            "10, 2, 2",
            "10, 6, 5"
    })
    void setDefaultScale_withBigDecimal(long unscaledVal, int scale, int expectedScale) {
        BigDecimal number = BigDecimal.valueOf(unscaledVal, scale);

        BigDecimal result = DecimalUtils.setDefaultScale(number);

        Assertions.assertEquals(expectedScale, result.scale());
    }

    // endregion

    // region setDefaultScale with double tests

    @Test
    void setDefaultScale_withDouble_returnsNull_whenNumberIsNull() {
        Double number = null;

        Assertions.assertNull(DecimalUtils.setDefaultScale(number));
    }

    @ParameterizedTest
    @CsvSource({
            "10.01, 2, 10.01",
            "10.000001, 5, 10",
            "10.000005, 5, 10.00001"
    })
    void setDefaultScale_withDouble(Double number, int expectedScale, double expectedValue) {
        BigDecimal result = DecimalUtils.setDefaultScale(number);

        Assertions.assertEquals(expectedScale, result.scale());
        AssertUtils.assertEquals(BigDecimal.valueOf(expectedValue), result);
    }

    // endregion

    // region setDefaultScale with long tests

    @Test
    void setDefaultScale_withLong_returnsNull_whenNumberIsNull() {
        Long number = null;

        Assertions.assertNull(DecimalUtils.setDefaultScale(number));
    }

    @Test
    void setDefaultScale_withLong_notChangesScale() {
        Long number = 10L;

        final BigDecimal result = DecimalUtils.setDefaultScale(number);

        Assertions.assertEquals(0, result.scale());
        AssertUtils.assertEquals(BigDecimal.valueOf(10), result);
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
        boolean result = DecimalUtils.numbersEqual(value1, value2);

        Assertions.assertEquals(expectedResult, result);
    }

    @ParameterizedTest
    @CsvSource({
            "100, 100, true",
            "11, 100, false",
    })
    void numbersEqual_withInt(BigDecimal value1, int value2, boolean expectedResult) {
        boolean result = DecimalUtils.numbersEqual(value1, value2);

        Assertions.assertEquals(expectedResult, result);
    }

    @ParameterizedTest
    @CsvSource({
            "100, 100, true",
            "11, 100, false",
    })
    void numbersEqual_withDouble(BigDecimal value1, double value2, boolean expectedResult) {
        boolean result = DecimalUtils.numbersEqual(value1, value2);

        Assertions.assertEquals(expectedResult, result);
    }

    // endregion

    @ParameterizedTest
    @CsvSource({
            "150, 151, false",
            "150, 150, false",
            "150, 149, true"
    })
    void isGreater(BigDecimal value1, long value2, boolean expectedResult) {
        boolean result = DecimalUtils.isGreater(value1, value2);

        Assertions.assertEquals(expectedResult, result);
    }

    @ParameterizedTest
    @CsvSource({
            "150, 149, false",
            "150, 150, false",
            "150, 151, true"
    })
    void isLower(BigDecimal value1, long value2, boolean expectedResult) {
        boolean result = DecimalUtils.isLower(value1, value2);

        Assertions.assertEquals(expectedResult, result);
    }

}