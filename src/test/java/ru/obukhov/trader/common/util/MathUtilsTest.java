package ru.obukhov.trader.common.util;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.AssertUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MathUtilsTest {

    // region getAverage with collection tests

    @Test
    void getAverage_withCollection_returnsZero_whenCollectionIsEmpty() {

        List<BigDecimal> numbers = ImmutableList.of();

        BigDecimal average = MathUtils.getAverage(numbers);

        AssertUtils.assertEquals(BigDecimal.ZERO, average);

    }

    @Test
    void getAverage_withCollection_returnsNumber_whenItIsSingleInCollection() {

        List<BigDecimal> numbers = ImmutableList.of(BigDecimal.TEN);

        BigDecimal average = MathUtils.getAverage(numbers);

        AssertUtils.assertEquals(numbers.get(0), average);

    }

    @Test
    void getAverage_withCollection_returnsAverage_whenMultipleNumbersInCollection() {

        List<BigDecimal> numbers = ImmutableList.of(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(1000)
        );

        BigDecimal average = MathUtils.getAverage(numbers);

        AssertUtils.assertEquals(BigDecimal.valueOf(433.33333), average);

    }

    // endregion

    // region getAverage with VarArgs tests

    @Test
    void getAverage_withVarArgs_returnsZero_whenNoArguments() {

        BigDecimal average = MathUtils.getAverage();

        AssertUtils.assertEquals(BigDecimal.ZERO, average);

    }

    @Test
    void getAverage_withVarArgs_returnsNumber_whenSingleArguments() {

        BigDecimal number = BigDecimal.TEN;

        BigDecimal average = MathUtils.getAverage(number);

        AssertUtils.assertEquals(number, average);

    }

    @Test
    void getAverage_withVarArgs_returnsAverage_whenMultipleNumbersInCollection() {


        BigDecimal average = MathUtils.getAverage(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(1000));

        AssertUtils.assertEquals(BigDecimal.valueOf(433.33333), average);

    }

    // endregion

    @Test
    void multiply() {
        BigDecimal result = MathUtils.multiply(BigDecimal.valueOf(100.1), 1.5);

        AssertUtils.assertEquals(BigDecimal.valueOf(150.15), result);
    }

    @Test
    void divideByInteger() {
        BigDecimal result = MathUtils.divide(BigDecimal.valueOf(100), 3);

        AssertUtils.assertEquals(BigDecimal.valueOf(33.33333), result);
    }

    @Test
    void divideByBigDecimal() {
        BigDecimal result = MathUtils.divide(BigDecimal.valueOf(100), BigDecimal.valueOf(3));

        AssertUtils.assertEquals(BigDecimal.valueOf(33.33333), result);
    }

    // region getIntegerQuotient

    @Test
    void getIntegerQuotient1() {
        int result = MathUtils.getIntegerQuotient(BigDecimal.valueOf(7.8), BigDecimal.valueOf(2.6));

        assertEquals(3, result);
    }

    @Test
    void getIntegerQuotient2() {
        int result = MathUtils.getIntegerQuotient(BigDecimal.valueOf(7.9), BigDecimal.valueOf(2.6));

        assertEquals(3, result);
    }

    @Test
    void getIntegerQuotient3() {
        int result = MathUtils.getIntegerQuotient(BigDecimal.valueOf(10.3), BigDecimal.valueOf(2.6));

        assertEquals(3, result);
    }

    // endregion

    @Test
    void getFraction() {
        BigDecimal result = MathUtils.getFraction(BigDecimal.valueOf(765), 0.003);

        AssertUtils.assertEquals(BigDecimal.valueOf(2.295), result);
    }

    @Test
    void addFraction() {
        BigDecimal result = MathUtils.addFraction(BigDecimal.valueOf(765), 0.003);

        AssertUtils.assertEquals(BigDecimal.valueOf(767.295), result);
    }

    @Test
    void subtractFraction() {
        BigDecimal result = MathUtils.subtractFraction(BigDecimal.valueOf(765), 0.003);

        AssertUtils.assertEquals(BigDecimal.valueOf(762.705), result);
    }

    @Test
    void getFractionDifference() {
        BigDecimal result = MathUtils.getFractionDifference(BigDecimal.valueOf(765), BigDecimal.valueOf(762.705));

        AssertUtils.assertEquals(BigDecimal.valueOf(0.00301), result);
    }

    // region setDefaultScale tests

    @Test
    void setDefaultScale_returnsNull_whenNumberIsNull() {
        assertNull(MathUtils.setDefaultScale(null));
    }

    @Test
    void setDefaultScale_setZeroScale_whenNumberScaleIsNegative() {
        BigDecimal number = BigDecimal.valueOf(10, -1);
        final BigDecimal result = MathUtils.setDefaultScale(number);

        assertEquals(0, result.scale());
    }

    @Test
    void setDefaultScale_notChangesScale_whenNumberScaleIsLowerThanDefault() {
        int scale = 2;
        BigDecimal number = BigDecimal.valueOf(10, scale);
        final BigDecimal result = MathUtils.setDefaultScale(number);

        assertEquals(scale, result.scale());
    }

    @Test
    void setDefaultScale_setDefaultScaleValue_whenNumberScaleIsGreaterThanDefault() {
        int scale = 6;
        BigDecimal number = BigDecimal.valueOf(10, scale);
        final BigDecimal result = MathUtils.setDefaultScale(number);

        assertEquals(MathUtils.DEFAULT_SCALE, result.scale());
    }

    // endregion

    // region numbersEqual tests

    @Test
    void numbersEqual_returnTrue_whenEqualsBigDecimal() {
        boolean result = MathUtils.numbersEqual(BigDecimal.valueOf(100.00), BigDecimal.valueOf(100));

        assertTrue(result);
    }

    @Test
    void numbersEqual_returnFalse_whenNotEqualsBigDecimal() {
        boolean result = MathUtils.numbersEqual(BigDecimal.valueOf(11.00), BigDecimal.valueOf(100));

        assertFalse(result);
    }

    @Test
    void numbersEqual_returnTrue_whenEqualsInt() {
        boolean result = MathUtils.numbersEqual(BigDecimal.valueOf(100.00), 100);

        assertTrue(result);
    }

    @Test
    void numbersEqual_returnFalse_whenNotEqualsInt() {
        boolean result = MathUtils.numbersEqual(BigDecimal.valueOf(11.00), 100);

        assertFalse(result);
    }

    @Test
    void numbersEqual_returnTrue_whenEqualsDouble() {
        boolean result = MathUtils.numbersEqual(BigDecimal.valueOf(100.00), 100.00);

        assertTrue(result);
    }

    @Test
    void numbersEqual_returnFalse_whenNotEqualsDouble() {
        boolean result = MathUtils.numbersEqual(BigDecimal.valueOf(11.00), 100.00);

        assertFalse(result);
    }

    // endregion

    // region isGreater tests

    @Test
    void isGreater_returnsFalse_whenLower() {
        BigDecimal value1 = BigDecimal.valueOf(150);
        long value2 = 151L;

        boolean result = MathUtils.isGreater(value1, value2);

        assertFalse(result);
    }

    @Test
    void isGreater_returnsFalse_whenEquals() {
        BigDecimal value1 = BigDecimal.valueOf(150);
        long value2 = 150L;

        boolean result = MathUtils.isGreater(value1, value2);

        assertFalse(result);
    }

    @Test
    void isGreater_returnsTrue_whenGreater() {
        BigDecimal value1 = BigDecimal.valueOf(150);
        long value2 = 149L;

        boolean result = MathUtils.isGreater(value1, value2);

        assertTrue(result);
    }

    // endregion

    // region isLower tests

    @Test
    void isLower_returnsFalse_whenGreater() {
        BigDecimal value1 = BigDecimal.valueOf(150);
        long value2 = 149L;

        boolean result = MathUtils.isLower(value1, value2);

        assertFalse(result);
    }

    @Test
    void isLower_returnsFalse_whenEquals() {
        BigDecimal value1 = BigDecimal.valueOf(150);
        long value2 = 150L;

        boolean result = MathUtils.isLower(value1, value2);

        assertFalse(result);
    }

    @Test
    void isLower_returnsTrue_whenGreater() {
        BigDecimal value1 = BigDecimal.valueOf(150);
        long value2 = 151L;

        boolean result = MathUtils.isLower(value1, value2);

        assertTrue(result);
    }

    // endregion

    // region max tests

    @Test
    void max_returnsNull_whenValuesIsEmpty() {
        Double max = MathUtils.max(Collections.emptyList());

        assertNull(max);
    }

    @Test
    void max_returnsMaxValue_whenValuesIsNotEmpty() {
        List<Double> values = Arrays.asList(-100d, 21d, 10d, 20d);

        Double max = MathUtils.max(values);

        assertEquals(max, Double.valueOf(21));
    }

    // endregion

    // region min tests

    @Test
    void min_returnsNull_whenValuesIsEmpty() {
        Double min = MathUtils.min(Collections.emptyList());

        assertNull(min);
    }

    @Test
    void min_returnsMaxValue_whenValuesIsNotEmpty() {
        List<Double> values = Arrays.asList(100d, -21d, 10d, 20d);

        Double min = MathUtils.min(values);

        assertEquals(min, Double.valueOf(-21));
    }

    // endregion

}