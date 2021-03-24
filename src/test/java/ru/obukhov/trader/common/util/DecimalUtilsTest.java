package ru.obukhov.trader.common.util;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecimalUtilsTest {

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
    void divideByInteger() {
        BigDecimal result = DecimalUtils.divide(BigDecimal.valueOf(100), 3);

        AssertUtils.assertEquals(BigDecimal.valueOf(33.33333), result);
    }

    @Test
    void divideByDouble() {
        BigDecimal result = DecimalUtils.divide(BigDecimal.valueOf(100), 3.5);

        AssertUtils.assertEquals(BigDecimal.valueOf(28.57143), result);
    }

    @Test
    void divideByBigDecimal() {
        BigDecimal result = DecimalUtils.divide(BigDecimal.valueOf(100), BigDecimal.valueOf(3.5));

        AssertUtils.assertEquals(BigDecimal.valueOf(28.57143), result);
    }

    // endregion

    // region getIntegerQuotient

    @Test
    void getIntegerQuotient1() {
        int result = DecimalUtils.getIntegerQuotient(BigDecimal.valueOf(7.8), BigDecimal.valueOf(2.6));

        assertEquals(3, result);
    }

    @Test
    void getIntegerQuotient2() {
        int result = DecimalUtils.getIntegerQuotient(BigDecimal.valueOf(7.9), BigDecimal.valueOf(2.6));

        assertEquals(3, result);
    }

    @Test
    void getIntegerQuotient3() {
        int result = DecimalUtils.getIntegerQuotient(BigDecimal.valueOf(10.3), BigDecimal.valueOf(2.6));

        assertEquals(3, result);
    }

    // endregion

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

    // region setDefaultScale tests

    @Test
    void setDefaultScale_returnsNull_whenNumberIsNull() {
        assertNull(DecimalUtils.setDefaultScale(null));
    }

    @Test
    void setDefaultScale_setZeroScale_whenNumberScaleIsNegative() {
        BigDecimal number = BigDecimal.valueOf(10, -1);
        final BigDecimal result = DecimalUtils.setDefaultScale(number);

        assertEquals(0, result.scale());
    }

    @Test
    void setDefaultScale_notChangesScale_whenNumberScaleIsLowerThanDefault() {
        int scale = 2;
        BigDecimal number = BigDecimal.valueOf(10, scale);
        final BigDecimal result = DecimalUtils.setDefaultScale(number);

        assertEquals(scale, result.scale());
    }

    @Test
    void setDefaultScale_setDefaultScaleValue_whenNumberScaleIsGreaterThanDefault() {
        int scale = 6;
        BigDecimal number = BigDecimal.valueOf(10, scale);
        final BigDecimal result = DecimalUtils.setDefaultScale(number);

        assertEquals(DecimalUtils.DEFAULT_SCALE, result.scale());
    }

    // endregion

    // region numbersEqual tests

    @Test
    void numbersEqual_returnTrue_whenEqualsBigDecimal() {
        boolean result = DecimalUtils.numbersEqual(BigDecimal.valueOf(100.00), BigDecimal.valueOf(100));

        assertTrue(result);
    }

    @Test
    void numbersEqual_returnFalse_whenNotEqualsBigDecimal() {
        boolean result = DecimalUtils.numbersEqual(BigDecimal.valueOf(11.00), BigDecimal.valueOf(100));

        assertFalse(result);
    }

    @Test
    void numbersEqual_returnTrue_whenEqualsInt() {
        boolean result = DecimalUtils.numbersEqual(BigDecimal.valueOf(100.00), 100);

        assertTrue(result);
    }

    @Test
    void numbersEqual_returnFalse_whenNotEqualsInt() {
        boolean result = DecimalUtils.numbersEqual(BigDecimal.valueOf(11.00), 100);

        assertFalse(result);
    }

    @Test
    void numbersEqual_returnTrue_whenEqualsDouble() {
        boolean result = DecimalUtils.numbersEqual(BigDecimal.valueOf(100.00), 100.00);

        assertTrue(result);
    }

    @Test
    void numbersEqual_returnFalse_whenNotEqualsDouble() {
        boolean result = DecimalUtils.numbersEqual(BigDecimal.valueOf(11.00), 100.00);

        assertFalse(result);
    }

    // endregion

    // region isGreater tests

    @Test
    void isGreater_returnsFalse_whenLower() {
        BigDecimal value1 = BigDecimal.valueOf(150);
        long value2 = 151L;

        boolean result = DecimalUtils.isGreater(value1, value2);

        assertFalse(result);
    }

    @Test
    void isGreater_returnsFalse_whenEquals() {
        BigDecimal value1 = BigDecimal.valueOf(150);
        long value2 = 150L;

        boolean result = DecimalUtils.isGreater(value1, value2);

        assertFalse(result);
    }

    @Test
    void isGreater_returnsTrue_whenGreater() {
        BigDecimal value1 = BigDecimal.valueOf(150);
        long value2 = 149L;

        boolean result = DecimalUtils.isGreater(value1, value2);

        assertTrue(result);
    }

    // endregion

    // region isLower tests

    @Test
    void isLower_returnsFalse_whenGreater() {
        BigDecimal value1 = BigDecimal.valueOf(150);
        long value2 = 149L;

        boolean result = DecimalUtils.isLower(value1, value2);

        assertFalse(result);
    }

    @Test
    void isLower_returnsFalse_whenEquals() {
        BigDecimal value1 = BigDecimal.valueOf(150);
        long value2 = 150L;

        boolean result = DecimalUtils.isLower(value1, value2);

        assertFalse(result);
    }

    @Test
    void isLower_returnsTrue_whenGreater() {
        BigDecimal value1 = BigDecimal.valueOf(150);
        long value2 = 151L;

        boolean result = DecimalUtils.isLower(value1, value2);

        assertTrue(result);
    }

    // endregion

}