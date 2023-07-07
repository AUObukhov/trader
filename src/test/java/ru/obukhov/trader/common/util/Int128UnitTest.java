package ru.obukhov.trader.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.obukhov.trader.test.utils.AssertUtils;

class Int128UnitTest {

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0",
            "14, 0",
            "-15, -1"
    })
    void lowConstructor(final long low, final long expectedHigh) {
        final Int128 actualResult = new Int128(low);
        Assertions.assertEquals(low, actualResult.getLow());
        Assertions.assertEquals(expectedHigh, actualResult.getHigh());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, true",
            "15, 0, true",
            "-100, -1, true",
            Long.MAX_VALUE + ", 0, true",
            Long.MIN_VALUE + ", -1, true",
            Long.MIN_VALUE + ", 0, false",
            "0, 10, false",
            "1, 1, false",
            "0, -11, false",
            "-100, 0, false"
    })
    void isLong_static(final long low, final long high, final boolean expectedResult) {
        final boolean actualResult = Int128.isLong(low, high);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, true",
            "15, 0, true",
            "-100, -1, true",
            Long.MAX_VALUE + ", 0, true",
            Long.MIN_VALUE + ", -1, true",
            Long.MIN_VALUE + ", 0, false",
            "0, 10, false",
            "1, 1, false",
            "0, -11, false",
            "-100, 0, false"
    })
    void isLong(final long low, final long high, final boolean expectedResult) {
        final boolean actualResult = new Int128(low, high).isLong();
        Assertions.assertEquals(expectedResult, actualResult);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, true",
            "15, 0, true",
            "-100, -1, true",
            Integer.MAX_VALUE + ", 0, true",
            Integer.MIN_VALUE + ", -1, true",
            Integer.MIN_VALUE + ", 0, false",
            (Integer.MAX_VALUE + 1L) + ", 0, false",
            (Integer.MIN_VALUE - 1L) + ", 0, false",
            (Integer.MIN_VALUE - 1L) + ", -1, false",
            "0, 10, false",
            "1, 1, false",
            "0, -11, false",
            "-100, 0, false"
    })
    void isInt(final long low, final long high, final boolean expectedResult) {
        final boolean actualResult = new Int128(low, high).isInt();
        Assertions.assertEquals(expectedResult, actualResult);
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = {
            "0; 0; (low = 0, high = 0)",
            "15; 0; (low = 15, high = 0)",
            "-100; -1; (low = -100, high = -1)",
    })
    void toString(final long low, final long high, final String expectedResult) {
        final String actualResult = new Int128(low, high).toString();
        Assertions.assertEquals(expectedResult, actualResult);
    }

    // region toLongExact tests

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 0",
            "15, 0, 15",
            "-100, -1, -100",
            Long.MAX_VALUE + ", 0, " + Long.MAX_VALUE,
            Long.MIN_VALUE + ", -1, " + Long.MIN_VALUE,
    })
    void toLongExact(final long low, final long high, final long expectedResult) {
        final long actualResult = new Int128(low, high).toLongExact();
        Assertions.assertEquals(expectedResult, actualResult);
    }

    @ParameterizedTest
    @CsvSource(value = {
            Long.MIN_VALUE + ", 0",
            "0, 10",
            "1, 1",
            "0, -11",
            "-100, 0"
    })
    void toLongExact_throwException_whenValueIsNotLong(final long low, final long high) {
        final Int128 value = new Int128(low, high);
        final String expectedMessage = "value too big for a long: " + value;
        AssertUtils.assertThrowsWithMessage(ArithmeticException.class, value::toLongExact, expectedMessage);
    }

    // endregion

    // region toIntExact tests

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 0",
            "15, 0, 15",
            "-100, -1, -100",
            Integer.MAX_VALUE + ", 0, " + Integer.MAX_VALUE,
            Integer.MIN_VALUE + ", -1, " + Integer.MIN_VALUE,
    })
    void toIntExact(final long low, final long high, final int expectedResult) {
        final int actualResult = new Int128(low, high).toIntExact();
        Assertions.assertEquals(expectedResult, actualResult);
    }


    @ParameterizedTest
    @CsvSource(value = {
            Integer.MIN_VALUE + ", 0",
            (Integer.MAX_VALUE + 1L) + ", 0",
            (Integer.MIN_VALUE - 1L) + ", 0",
            (Integer.MIN_VALUE - 1L) + ", -1",
            "0, 10",
            "1, 1",
            "0, -11",
            "-100, 0"
    })
    void toIntExact_throwException_whenValueIsNotInt(final long low, final long high) {
        final Int128 value = new Int128(low, high);
        final String expectedMessage = "value too big for a int: " + value;
        AssertUtils.assertThrowsWithMessage(ArithmeticException.class, value::toIntExact, expectedMessage);
    }

    // endregion

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 0, 0, 0",
            "1, 0, 1, 0, 0",
            "2, 3, 2, 3, 0",
            "0, 4, 0, 4, 0",
            "-5, 0, -5, 0, 0",
            "-6, -7, -6, -7, 0",

            "9, 0, 8, 0, 1",
            "0, 11, 0, 10, 1",
            "13, 14, 12, 14, 1",
            "15, 17, 15, 16, 1",
            "0, 19, 0, -20, 1",
            "21, 22, -23, -24, 1",
            "25, 26, -25, -27, 1",

            "17, 0, -18, 0, -1",
            "-29, 0, -28, 0, -1",
            "0, -31, 0, -30, -1",
            "-33, -34, -32, -34, -1",
            "-35, -37, -35, -36, -1",
    })
    void compare_withInt128(final long lowLeft, final long highLeft, final long lowRight, final long highRight, final int expectedResult) {
        final Int128 left = new Int128(lowLeft, highLeft);
        final Int128 right = new Int128(lowRight, highRight);
        Assertions.assertEquals(expectedResult, left.compare(right));
        Assertions.assertEquals(-expectedResult, right.compare(left));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 0, 0",
            "1, 0, 1, 0",
            "-5, -1, -5, 0",

            "9, 0, 8, 1",

            "11, 0, 12, -1",
            "-17, -1, 18, -1",
            "-29, -1, -28, -1",
    })
    void compare_withLong(final long lowLeft, final long highLeft, final long right, final int expectedResult) {
        final Int128 left = new Int128(lowLeft, highLeft);
        Assertions.assertEquals(expectedResult, left.compare(right));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, true",
            "1, 0, false",
            "0, 2, false",
            "3, 3, false",
            "-4, 0, false",
            "0, -5, false",
            "-6, -6, false",
    })
    void isZero(final long low, final long high, final boolean expectedResult) {
        final Int128 value = new Int128(low, high);
        Assertions.assertEquals(expectedResult, value.isZero());
    }

    // region selfIncrementExact tests

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 1, 0",
            "2, 0, 3, 0",
            Long.MAX_VALUE + ", 0, " + Long.MIN_VALUE + ", 0",
            Long.MAX_VALUE + ", 4, " + Long.MIN_VALUE + ", 4",
            Long.MAX_VALUE + ", -5, " + Long.MIN_VALUE + ", -5",
            "-1, 6, 0, 7",
            "-1, -9, 0, -8",
    })
    void selfIncrementExact(final long low, final long high, final long expectedLow, final long expectedHigh) {
        final Int128 value = new Int128(low, high);
        value.selfIncrementExact();
        Assertions.assertEquals(expectedLow, value.getLow());
        Assertions.assertEquals(expectedHigh, value.getHigh());
    }

    @Test
    void selfIncrementExact_throwsArithmeticException_inCaseOfOverflow() {
        final Int128 value = new Int128(-1, Long.MAX_VALUE);
        AssertUtils.assertThrowsWithMessage(ArithmeticException.class, value::selfIncrementExact, "long overflow");
    }

    // endregion

    // region selfDecrementExact tests

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, -1, -1",
            "3, 0, 2, 0",
            Long.MIN_VALUE + ", 0, " + Long.MAX_VALUE + ", 0",
            Long.MIN_VALUE + ", 4, " + Long.MAX_VALUE + ", 4",
            Long.MIN_VALUE + ", -5, " + Long.MAX_VALUE + ", -5",
            "0, 7, -1, 6",
            "0, -8, -1, -9",
    })
    void selfDecrementExact(final long low, final long high, final long expectedLow, final long expectedHigh) {
        final Int128 value = new Int128(low, high);
        value.selfDecrementExact();
        Assertions.assertEquals(expectedLow, value.getLow());
        Assertions.assertEquals(expectedHigh, value.getHigh());
    }

    @Test
    void selfDecrementExact_throwsArithmeticException_inCaseOfOverflow() {
        final Int128 value = new Int128(0, Long.MIN_VALUE);
        AssertUtils.assertThrowsWithMessage(ArithmeticException.class, value::selfDecrementExact, "long overflow");
    }

    // endregion

    // region selfAddExact tests

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 0, 0, 0",
            "5, 0, -5, 0, 0",
            "-1, " + Long.MAX_VALUE + ", 0, -1," + Long.MAX_VALUE,
            "1, 0, 2, 3, 0",
            "-10, 0, -20, -30, 0",
            "-1, 0, 1, 0, 1",
            "3, -12, 4, 7, -12",
    })
    void selfAddExact(final long low, final long high, final int term, final long expectedLow, final long expectedHigh) {
        final Int128 value = new Int128(low, high);
        value.selfAddExact(term);
        Assertions.assertEquals(expectedLow, value.getLow());
        Assertions.assertEquals(expectedHigh, value.getHigh());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "-1, " + Long.MAX_VALUE + ", 1",
            "0, " + Long.MIN_VALUE + ", -1",
    })
    void selfAddExact_throwsArithmeticException_inCaseOfOverflow(final long low, final long high, final int term) {
        final Int128 value = new Int128(low, high);
        AssertUtils.assertThrowsWithMessage(ArithmeticException.class, () -> value.selfAddExact(term), "Int128 overflow");
    }

    // endregion

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 0, 0, 0, 0",
            "15, 16, 15, 16, 0, 0",
            "-15, -16, -15, -16, 0, 0",

            "1, 2, 0, 0, 1, 2",
            "4, 5, 3, 0, 1, 5",
            "8, 9, 6, 7, 2, 2",
            "10, 0, 11, 0, -1, -1",
            "12, 13, 14, 13, -2, -1",
            "-12, -13, -14, -13, 2, 0",
    })
    void selfSubtract(
            final long minuendLow, final long minuendHigh,
            final long subtrahendLow, final long subtrahendHigh,
            final long expectedLow, final long expectedHigh
    ) {
        final Int128 minuend = new Int128(minuendLow, minuendHigh);
        final Int128 subtrahend = new Int128(subtrahendLow, subtrahendHigh);
        minuend.selfSubtract(subtrahend);

        Assertions.assertEquals(subtrahendLow, subtrahend.getLow());
        Assertions.assertEquals(subtrahendHigh, subtrahend.getHigh());
        Assertions.assertEquals(expectedLow, minuend.getLow());
        Assertions.assertEquals(expectedHigh, minuend.getHigh());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 0, 0, 0",
            "0, 0, 99, 0, 0",
            "2, 3, 1, 2, 3",
            "4, 0, 5, 20, 0",
            "6, 7, 8, 48, 56",
            "1000000000000000000, 0, 1000000000, -6930898827444486144, 54210108",
            "1000000000000000, 1000, 1000000000, 2003764205206896640, 1000000054210",

            "-2, -3, 1, -2, -3",
            "-4, 0, 5, -20, 4",
            "-6, -7, 8, -48, -49",
            "-1000000000000000000, 0, 1000000000, 6930898827444486144, 945789891",
            "-1000000000000000, -1000, 1000000000, -2003764205206896640, -999000054211",

            "2, 3, -1, -2, -4",
            "4, 0, -5, -20, -1",
            "6, 7, -8, -48, -57",
            "1000000000000000000, 0, -1000000000, 6930898827444486144, -54210109",
            "1000000000000000, 1000, -1000000000, -2003764205206896640, -1000000054211",
    })
    void multiplyExact(
            final long low, final long high,
            final int multiplier,
            final long expectedLow, final long expectedHigh
    ) {
        final Int128 number = new Int128(low, high);
        final Int128 result = number.multiplyExact(multiplier);

        Assertions.assertEquals(low, number.getLow());
        Assertions.assertEquals(high, number.getHigh());
        Assertions.assertEquals(expectedLow, result.getLow());
        Assertions.assertEquals(expectedHigh, result.getHigh());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 0, 0",
            "1, 0, 0, 0",
            "0, 1, 0, 0",
            "-1, 0, 0, 0",
            "0, -1, 0, 0",

            "2, 1, 2, 0",
            "1, 3, 3, 0",
            "44, 55, 2420, 0",
            "1000000000000000000, 1000000000, -6930898827444486144, 54210108",
    })
    void multiplyPositive(
            final long multiplier1, final int multiplier2,
            final long expectedLow, final long expectedHigh
    ) {
        final Int128 result = Int128.multiplyPositive(multiplier1, multiplier2);

        Assertions.assertEquals(expectedLow, result.getLow());
        Assertions.assertEquals(expectedHigh, result.getHigh());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 0, 0, 0, 0",
            "1, 0, 0, 0, 0, 0",
            "2, 3, 0, 0, 0, 0",
            "4, 5, 1, 0, 4, 5",
            "6, 7, 8, 0, 48, 56",
            "1000000000000000000, 0, 1000000000, 0, -6930898827444486144, 54210108",
    })
    void multiplyPositive(
            final long multiplier1Low, final long multiplier1High,
            final int multiplier2Low, final long multiplier2High,
            final long expectedLow, final long expectedHigh
    ) {
        final Int128 multiplier1 = new Int128(multiplier1Low, multiplier1High);
        final Int128 multiplier2 = new Int128(multiplier2Low, multiplier2High);
        final Int128 result1 = multiplier1.multiplyPositive(multiplier2);
        final Int128 result2 = multiplier2.multiplyPositive(multiplier1);

        Assertions.assertEquals(expectedLow, result1.getLow());
        Assertions.assertEquals(expectedHigh, result1.getHigh());
        Assertions.assertEquals(expectedLow, result2.getLow());
        Assertions.assertEquals(expectedHigh, result2.getHigh());
    }

    // region selfDividePositive_byInt128 tests

    @ParameterizedTest
    @CsvSource(value = {
            // division by greater number
            "10, 0, 11, 0, 0, 0, 10, 0",
            "0, 12, 0, 13, 0, 0, 0, 12",
            "14, 15, 15, 15, 0, 0, 14, 15",
            "16, 17, 16, 18, 0, 0, 16, 17",

            // division by equal number
            "6, 0, 6, 0, 1, 0, 0, 0",
            "0, 7, 0, 7, 1, 0, 0, 0",
            "8, 9, 8, 9, 1, 0, 0, 0",

            // division by lower number, dividend and divisor fit in an unsigned
            "20, 0, 19, 0, 1, 0, 1, 0",
            "21, 0, 7, 0, 3, 0, 0, 0",
            "4611686018427387904, 0, 1152921504606846976, 0, 4, 0, 0, 0",
            "-1, 0, 4294967296, 0, 4294967295, 0, 4294967295, 0",

            // divisor is 1
            "2, 0, 1, 0, 2, 0, 0, 0",
            "0, 3, 1, 0, 0, 3, 0, 0",
            "4, 5, 1, 0, 4, 5, 0, 0",

            // divisor is power of 2
            "123, 456, 0, 1, 456, 0, 123, 0",

            // many orders of magnitude difference
            "123, 2, 3, 0, -6148914691236517165, 0, 2, 0",
            "123, 457, 3, 0, 6148914691236517246, 152, 1, 0",
            "123, 9223372036854775807, 1, 140737488355327, 65536, 0, -65413, 65534",

            // not many orders of magnitude difference
            "123, 456, 123, 123, 3, 0, -246, 86",
    })
    void selfDividePositive_byInt128(
            final long dividendLow, final long dividendHigh,
            final long divisorLow, final long divisorHigh,
            final long expectedQuotientLow, final long expectedQuotientHigh,
            final long expectedRemainderLow, final long expectedRemainderHigh
    ) {
        final Int128 dividend = new Int128(dividendLow, dividendHigh);
        final Int128 divisor = new Int128(divisorLow, divisorHigh);

        final Int128 result = dividend.selfDividePositive(divisor);

        Assertions.assertEquals(expectedQuotientLow, result.getLow());
        Assertions.assertEquals(expectedQuotientHigh, result.getHigh());

        Assertions.assertEquals(expectedRemainderLow, dividend.getLow());
        Assertions.assertEquals(expectedRemainderHigh, dividend.getHigh());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0",
            "1, 0",
            "0, 2",
            "3, 4",
    })
    void selfDividePositive_byInt128_throwsArithmeticException_whenDivisorIsZero(final long dividendLow, final long dividendHigh) {
        final Int128 dividend = new Int128(dividendLow, dividendHigh);
        final Int128 divisor = new Int128(0, 0);

        AssertUtils.assertThrowsWithMessage(ArithmeticException.class, () -> dividend.selfDividePositive(divisor), "Divide by zero");
    }

    // endregion

    // region selfDividePositive_byLong tests

    @ParameterizedTest
    @CsvSource(value = {
            // division by greater number
            "10, 0, 11, 0, 0, 10, 0",

            // division by equal number
            "6, 0, 6, 1, 0, 0, 0",

            // division by lower number, dividend and divisor fit in an unsigned
            "20, 0, 19, 1, 0, 1, 0",
            "21, 0, 7, 3, 0, 0, 0",
            "4611686018427387904, 0, 1152921504606846976, 4, 0, 0, 0",
            "-1, 0, 4294967296, 4294967295, 0, 4294967295, 0",

            // divisor is 1
            "2, 0, 1, 2, 0, 0, 0",
            "0, 3, 1, 0, 3, 0, 0",
            "4, 5, 1, 4, 5, 0, 0",

            // divisor is power of 2
            "123, 456, 1024, 8214565720323784704, 0, 123, 0",

            // many orders of magnitude difference
            "123, 2, 3, -6148914691236517165, 0, 2, 0",
            "123, 457, 3, 6148914691236517246, 152, 1, 0",

            // not many orders of magnitude difference
            "123, 1, 9223372036854775807, 2, 0, 125, 0",
    })
    void selfDividePositive_byLong(
            final long dividendLow, final long dividendHigh,
            final long divisor,
            final long expectedQuotientLow, final long expectedQuotientHigh,
            final long expectedRemainderLow, final long expectedRemainderHigh
    ) {
        final Int128 dividend = new Int128(dividendLow, dividendHigh);

        final Int128 result = dividend.selfDividePositive(divisor);

        Assertions.assertEquals(expectedQuotientLow, result.getLow());
        Assertions.assertEquals(expectedQuotientHigh, result.getHigh());

        Assertions.assertEquals(expectedRemainderLow, dividend.getLow());
        Assertions.assertEquals(expectedRemainderHigh, dividend.getHigh());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0",
            "1, 0",
            "0, 2",
            "3, 4",
    })
    void selfDividePositive_byLong_throwsArithmeticException_whenDivisorIsZero(final long dividendLow, final long dividendHigh) {
        final Int128 dividend = new Int128(dividendLow, dividendHigh);

        AssertUtils.assertThrowsWithMessage(ArithmeticException.class, () -> dividend.selfDividePositive(0), "Divide by zero");
    }

    // endregion

}