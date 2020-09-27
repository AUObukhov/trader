package ru.obukhov.investor.util;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MathUtilsTest {

    // region getAverageMoney tests

    @Test
    public void getAverageMoney_returnsZero_whenCollectionIsEmpty() {

        List<BigDecimal> numbers = ImmutableList.of();

        BigDecimal average = MathUtils.getAverageMoney(numbers);

        assertTrue(MathUtils.numbersEqual(BigDecimal.ZERO, average));

    }

    @Test
    public void getAverageMoney_returnsNumber_whenItIsSingleInCollection() {

        List<BigDecimal> numbers = ImmutableList.of(BigDecimal.TEN);

        BigDecimal average = MathUtils.getAverageMoney(numbers);

        assertTrue(MathUtils.numbersEqual(BigDecimal.TEN, average));

    }

    @Test
    public void getAverageMoney_returnsAverage_whenMultipleNumbersInCollection() {

        List<BigDecimal> numbers = ImmutableList.of(
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(200),
                BigDecimal.valueOf(1000)
        );

        BigDecimal average = MathUtils.getAverageMoney(numbers);

        assertTrue(MathUtils.numbersEqual(BigDecimal.valueOf(433.33), average));

    }

    // endregion

    @Test
    public void subtractMoney() {
        BigDecimal result = MathUtils.subtractMoney(BigDecimal.valueOf(100), BigDecimal.valueOf(10.555555));

        assertTrue(MathUtils.numbersEqual(BigDecimal.valueOf(89.44), result));
    }

    @Test
    public void divideMoney() {
        BigDecimal result = MathUtils.divideMoney(BigDecimal.valueOf(100), 3);

        assertTrue(MathUtils.numbersEqual(BigDecimal.valueOf(33.33), result));
    }

    @Test
    public void subtractCommission() {
        BigDecimal result = MathUtils.subtractCommission(BigDecimal.valueOf(99), 0.05);

        assertTrue(MathUtils.numbersEqual(BigDecimal.valueOf(94.05), result));
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
}