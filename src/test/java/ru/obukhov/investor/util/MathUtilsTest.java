package ru.obukhov.investor.util;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MathUtilsTest {

    // region getAverage with collection tests

    @Test
    public void getAverage_withCollection_returnsZero_whenCollectionIsEmpty() {

        List<BigDecimal> numbers = ImmutableList.of();

        BigDecimal average = MathUtils.getAverage(numbers);

        assertTrue(MathUtils.numbersEqual(BigDecimal.ZERO, average));

    }

    @Test
    public void getAverage_withCollection_returnsNumber_whenItIsSingleInCollection() {

        List<BigDecimal> numbers = ImmutableList.of(BigDecimal.TEN);

        BigDecimal average = MathUtils.getAverage(numbers);

        assertTrue(MathUtils.numbersEqual(numbers.get(0), average));

    }

    @Test
    public void getAverage_withCollection_returnsAverage_whenMultipleNumbersInCollection() {

        List<BigDecimal> numbers = ImmutableList.of(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(1000)
        );

        BigDecimal average = MathUtils.getAverage(numbers);

        assertTrue(MathUtils.numbersEqual(BigDecimal.valueOf(433.3333), average));

    }

    // endregion

    // region getAverage with VarArgs tests

    @Test
    public void getAverage_withVarArgs_returnsZero_whenNoArguments() {

        BigDecimal average = MathUtils.getAverage();

        assertTrue(MathUtils.numbersEqual(BigDecimal.ZERO, average));

    }

    @Test
    public void getAverage_withVarArgs_returnsNumber_whenSingleArguments() {

        BigDecimal number = BigDecimal.TEN;

        BigDecimal average = MathUtils.getAverage(number);

        assertTrue(MathUtils.numbersEqual(number, average));

    }

    @Test
    public void getAverage_withVarArgs_returnsAverage_whenMultipleNumbersInCollection() {


        BigDecimal average = MathUtils.getAverage(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(1000));

        assertTrue(MathUtils.numbersEqual(BigDecimal.valueOf(433.3333), average));

    }

    // endregion

    @Test
    public void multiply() {
        BigDecimal result = MathUtils.multiply(BigDecimal.valueOf(100.1), 1.5);

        assertTrue(MathUtils.numbersEqual(BigDecimal.valueOf(150.15), result));
    }

    @Test
    public void divide() {
        BigDecimal result = MathUtils.divide(BigDecimal.valueOf(100), 3);

        assertTrue(MathUtils.numbersEqual(BigDecimal.valueOf(33.3333), result));
    }

    @Test
    public void getFraction() {
        BigDecimal result = MathUtils.getFraction(BigDecimal.valueOf(765), 0.003);

        assertTrue(MathUtils.numbersEqual(BigDecimal.valueOf(2.295), result));
    }

    @Test
    public void addFraction() {
        BigDecimal result = MathUtils.addFraction(BigDecimal.valueOf(765), 0.003);

        assertTrue(MathUtils.numbersEqual(BigDecimal.valueOf(767.295), result));
    }

    @Test
    public void subtractFraction() {
        BigDecimal result = MathUtils.subtractFraction(BigDecimal.valueOf(765), 0.003);

        assertTrue(MathUtils.numbersEqual(BigDecimal.valueOf(762.705), result));
    }

    @Test
    public void getFractionDifference() {
        BigDecimal result = MathUtils.getFractionDifference(BigDecimal.valueOf(765), BigDecimal.valueOf(762.705));

        assertTrue(MathUtils.numbersEqual(BigDecimal.valueOf(0.003), result));
    }

    // region numbersEqual tests

    @Test
    public void numbersEqual_returnTrue_whenEqualsBigDecimal() {
        boolean result = MathUtils.numbersEqual(BigDecimal.valueOf(100.00), BigDecimal.valueOf(100));

        assertTrue(result);
    }

    @Test
    public void numbersEqual_returnFalse_whenNotEqualsBigDecimal() {
        boolean result = MathUtils.numbersEqual(BigDecimal.valueOf(11.00), BigDecimal.valueOf(100));

        assertFalse(result);
    }

    @Test
    public void numbersEqual_returnTrue_whenEqualsInt() {
        boolean result = MathUtils.numbersEqual(BigDecimal.valueOf(100.00), 100);

        assertTrue(result);
    }

    @Test
    public void numbersEqual_returnFalse_whenNotEqualsInt() {
        boolean result = MathUtils.numbersEqual(BigDecimal.valueOf(11.00), 100);

        assertFalse(result);
    }

    @Test
    public void numbersEqual_returnTrue_whenEqualsDouble() {
        boolean result = MathUtils.numbersEqual(BigDecimal.valueOf(100.00), 100.00);

        assertTrue(result);
    }

    @Test
    public void numbersEqual_returnFalse_whenNotEqualsDouble() {
        boolean result = MathUtils.numbersEqual(BigDecimal.valueOf(11.00), 100.00);

        assertFalse(result);
    }

    // endregion

    // region isGreater tests

    @Test
    public void isGreater_returnsFalse_whenLower() {
        BigDecimal value1 = BigDecimal.valueOf(150);
        long value2 = 151L;

        boolean result = MathUtils.isGreater(value1, value2);

        assertFalse(result);
    }

    @Test
    public void isGreater_returnsFalse_whenEquals() {
        BigDecimal value1 = BigDecimal.valueOf(150);
        long value2 = 150L;

        boolean result = MathUtils.isGreater(value1, value2);

        assertFalse(result);
    }

    @Test
    public void isGreater_returnsTrue_whenGreater() {
        BigDecimal value1 = BigDecimal.valueOf(150);
        long value2 = 149L;

        boolean result = MathUtils.isGreater(value1, value2);

        assertTrue(result);
    }

    // endregion

    // region isLower tests

    @Test
    public void isLower_returnsFalse_whenGreater() {
        BigDecimal value1 = BigDecimal.valueOf(150);
        long value2 = 149L;

        boolean result = MathUtils.isLower(value1, value2);

        assertFalse(result);
    }

    @Test
    public void isLower_returnsFalse_whenEquals() {
        BigDecimal value1 = BigDecimal.valueOf(150);
        long value2 = 150L;

        boolean result = MathUtils.isLower(value1, value2);

        assertFalse(result);
    }

    @Test
    public void isLower_returnsTrue_whenGreater() {
        BigDecimal value1 = BigDecimal.valueOf(150);
        long value2 = 151L;

        boolean result = MathUtils.isLower(value1, value2);

        assertTrue(result);
    }

    // endregion

    // region max tests

    @Test
    public void max_returnsNull_whenValuesIsEmpty() {
        Double max = MathUtils.max(Collections.emptyList());

        assertNull(max);
    }

    @Test
    public void max_returnsMaxValue_whenValuesIsNotEmpty() {
        List<Double> values = Arrays.asList(-100d, 21d, 10d, 20d);

        Double max = MathUtils.max(values);

        assertEquals(max, Double.valueOf(21));
    }

    // endregion

    // region min tests

    @Test
    public void min_returnsNull_whenValuesIsEmpty() {
        Double min = MathUtils.min(Collections.emptyList());

        assertNull(min);
    }

    @Test
    public void min_returnsMaxValue_whenValuesIsNotEmpty() {
        List<Double> values = Arrays.asList(100d, -21d, 10d, 20d);

        Double min = MathUtils.min(values);

        assertEquals(min, Double.valueOf(-21));
    }

    // endregion

}