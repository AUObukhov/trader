package ru.obukhov.investor.util;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MathUtilsTest {

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
}