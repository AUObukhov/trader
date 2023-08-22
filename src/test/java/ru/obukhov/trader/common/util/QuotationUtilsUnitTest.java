package ru.obukhov.trader.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.math.BigDecimal;
import java.math.RoundingMode;

class QuotationUtilsUnitTest {

    // region newQuotation tests

    @ParameterizedTest
    @ValueSource(longs = {0L, 1L, -1L})
    void newQuotation_withUnitsOnly(final long units) {
        final Quotation actualResult = QuotationUtils.newQuotation(units);

        Assertions.assertEquals(units, actualResult.getUnits());
        Assertions.assertEquals(0, actualResult.getNano());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0",
            "1, 0",
            "0, 2",
            "3, 4",
            "-5, 0",
            "0, -6",
            "-7, -8",
    })
    void newQuotation_withUnitsAndNano(final long units, final int nano) {
        final Quotation actualResult = QuotationUtils.newQuotation(units, nano);

        Assertions.assertEquals(units, actualResult.getUnits());
        Assertions.assertEquals(nano, actualResult.getNano());
    }

    // region newQuotation from Double tests

    @Test
    void newQuotation_fromDouble_whenNull() {
        final Double value = null;
        Assertions.assertNull(QuotationUtils.newQuotation(value));
    }

    @Test
    void newQuotation_fromDouble_whenZero() {
        final Quotation quotation = QuotationUtils.newQuotation(0.0);

        Assertions.assertEquals(0, quotation.getUnits());
        Assertions.assertEquals(0, quotation.getNano());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "100.000000100, 100, 100",
            "123.0000000004, 123, 0",
            "123.0000000005, 123, 1",
            "-100.000000100, -100, -100",
            "-123.0000000004, -123, 0",
            "-123.0000000005, -123, -1",
    })
    void newQuotation_fromDouble(final double value, final long expectedUnits, final int expectedNano) {
        final Quotation quotation = QuotationUtils.newQuotation(value);

        Assertions.assertEquals(expectedUnits, quotation.getUnits());
        Assertions.assertEquals(expectedNano, quotation.getNano());
    }

    // endregion

    // region newQuotation from BigDecimal tests

    @Test
    void newQuotation_fromBigDecimal_whenNull() {
        final BigDecimal value = null;
        Assertions.assertNull(QuotationUtils.newQuotation(value));
    }

    @Test
    void newQuotation_fromBigDecimal_whenZero() {
        final Quotation quotation = QuotationUtils.newQuotation(DecimalUtils.setDefaultScale(0));

        Assertions.assertEquals(0, quotation.getUnits());
        Assertions.assertEquals(0, quotation.getNano());
    }

    @Test
    void newQuotation_fromBigDecimal() {
        final BigDecimal bigDecimal = DecimalUtils.setDefaultScale(100.000000100);

        final Quotation quotation = QuotationUtils.newQuotation(bigDecimal);

        Assertions.assertEquals(100, quotation.getUnits());
        Assertions.assertEquals(100, quotation.getNano());
    }

    // endregion

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 0, 0",
            "1, 0, 1, 0",
            "0, 2, 0, 2",
            "3, 4, 3, 4",
            "-5, 0, -5, 0",
            "0, -6, 0, -6",
            "-7, -8, -7, -8",
            "-9, 10, -8, -999999990",
            "11, -12, 10, 999999988",
    })
    void newNormalizedQuotation(final long units, final int nano, final long expectedUnits, final int expectedNano) {
        final Quotation actualResult = QuotationUtils.newNormalizedQuotation(units, nano);

        Assertions.assertEquals(expectedUnits, actualResult.getUnits());

        Assertions.assertEquals(expectedNano, actualResult.getNano());
    }

    // region toBigDecimal tests

    @Test
    void toBigDecimal_whenNull() {
        Assertions.assertNull(QuotationUtils.toBigDecimal(null));
    }

    @Test
    void toBigDecimal_whenZero() {
        final Quotation quotation = QuotationUtils.ZERO;

        final BigDecimal bigDecimal = QuotationUtils.toBigDecimal(quotation);

        Assertions.assertEquals(DecimalUtils.setDefaultScale(0), bigDecimal);
    }

    @Test
    void toBigDecimal() {
        final Quotation quotation = QuotationUtils.newQuotation(100L, 100);

        final BigDecimal bigDecimal = QuotationUtils.toBigDecimal(quotation);

        AssertUtils.assertEquals(100.000000100, bigDecimal);
        AssertUtils.assertEquals(DecimalUtils.DEFAULT_SCALE, bigDecimal.scale());
    }

    // endregion

    // region toDouble tests

    @Test
    void toDouble_whenNull() {
        Assertions.assertNull(QuotationUtils.toDouble(null));
    }

    @Test
    void toDouble_whenZero() {
        final Quotation quotation = QuotationUtils.ZERO;

        final Double actualResult = QuotationUtils.toDouble(quotation);

        Assertions.assertEquals(0, actualResult);
    }

    @Test
    void toDouble() {
        final Quotation quotation = QuotationUtils.newQuotation(100L, 100);

        final Double actualResult = QuotationUtils.toDouble(quotation);

        Assertions.assertEquals(100.000000100, actualResult);
    }

    // endregion

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, [0; 0]",
            "1000, 0, [1000; 0]",
            "0, 2000, [0; 2000]",
            "3000, 4000, [3000; 4000]",
            "5000, 100000000, [5000; 100000000]",
            "6000, 7, [6000; 7]",
            "-8000, 0, [-8000; 0]",
            "0, -9000, [0; -9000]",
            "-1100, -1200, [-1100; -1200]",
            "-1300, -140000000, [-1300; -140000000]",
            "-1500, -1, [-1500; -1]",
    })
    void toString(final long units, final int nano, final String expectedResult) {
        final Quotation quotation = QuotationUtils.newQuotation(units, nano);

        final String actualResult = QuotationUtils.toString(quotation);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 0",
            "1000, 0, 1000",
            "0, 2000, 0.000002",
            "3000, 4000, 3000.000004",
            "5000, 100000000, 5000.1",
            "6000, 7, 6000.000000007",
            "-8000, 0,-8000",
            "0, -9000, -0.000009",
            "-1100, -1200, -1100.0000012",
            "-1300, -140000000, -1300.14",
            "-1500, -1, -1500.000000001",
    })
    void toPrettyString(final long units, final int nano, final String expectedResult) {
        final Quotation quotation = QuotationUtils.newQuotation(units, nano);

        final String actualResult = QuotationUtils.toPrettyString(quotation);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 0",
            "1000, 0, 1",
            "0, 1000, 1",
            "1000, 1000, 1",
            "-1000, 0, -1",
            "0, -1000, -1",
            "-1000, -1000, -1",
    })
    void getSign(final long units, final int nano, final int expectedResult) {
        final Quotation quotation = QuotationUtils.newQuotation(units, nano);

        final int actualResult = QuotationUtils.getSign(quotation);

        Assertions.assertEquals(expectedResult, actualResult);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 0, 0, 0",
            "1000, 0, 1000, 0, 0",
            "0, 2000, 0, 2000, 0",
            "3000, 4000, 3000, 4000, 0",
            "5000, 100000000, 5000, 100000000, 0",
            "6000, 7, 6000, 7, 0",
            "-8000, 0, -8000, 0, 0",
            "0, -9000, 0, -9000, 0",
            "-1100, -1200, -1100, -1200, 0",
            "-1300, -140000000, -1300, -140000000, 0",
            "-1500, -1, -1500, -1, 0",

            "10, 0, 9, 0, 1",
            "0, 20, 0, 5, 1",
            "30, 4, 30, 3, 1",
            "50, 60, 30, 40, 1",

            "9, 0, -10, 0, 1",
            "0, 5, 0, -20, 1",
            "30, 3, -30, -3, 1",
            "30, 40, -50, -60, 1",

            "-10, 0, -9, 0, -1",
            "0, -20, 0, -5, -1",
            "-30, -4, -30, -3, -1",
            "-50, -60, -30, -40, -1",

            "51, 0, 50, 999999999, 1",
    })
    void compare(final long leftUnits, final int leftNano, final long rightUnits, final int rightNano, int expectedResult) {
        final Quotation left = QuotationUtils.newQuotation(leftUnits, leftNano);
        final Quotation right = QuotationUtils.newQuotation(rightUnits, rightNano);

        Assertions.assertEquals(expectedResult, QuotationUtils.compare(left, right));
        Assertions.assertEquals(-expectedResult, QuotationUtils.compare(right, left));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 0.0, 0",
            "1000, 0, 1000.00, 0",
            "0, 2000, 0.000002, 0",
            "3000, 4000, 3000.000004, 0",
            "5000, 100000000, 5000.100000000, 0",
            "6000, 7, 6000.000000007, 0",
            "-8000, 0, -8000.0, 0",
            "0, -9000, -0.000009000, 0",
            "-1100, -1200, -1100.000001200, 0",
            "-1300, -140000000, -1300.140000000, 0",
            "-1500, -1, -1500.000000001, 0",

            "10, 0, 9.0, 1",
            "0, 20, 0.000000005, 1",
            "30, 4, 30.000000003, 1",
            "50, 60, 30.00000004, 1",

            "9, 0, -10.00, 1",
            "0, 5, -0.000000020, 1",
            "30, 3, -30.000000003, 1",
            "30, 40, -50.000000060, 1",

            "-10, 0, -9.0, -1",
            "0, -20, -0.000000005, -1",
            "-30, -4, -30.000000003, -1",
            "-50, -60, -30.000000040, -1",

            "51, 0, 50.999999999, 1",
    })
    void compare(final long leftUnits, final int leftNano, final double right, int expectedResult) {
        final Quotation left = QuotationUtils.newQuotation(leftUnits, leftNano);

        Assertions.assertEquals(expectedResult, QuotationUtils.compare(left, right));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 0, true",
            "1000, 0, 1000, true",
            "0, 2000, 0, false",
            "3000, 4000, 3000, false",
            "-1000, 0, -1000, true",
            "0, -2000, 0, false",
            "-3000, -4000, -3000, false",
    })
    void equals(final long leftUnits, final int leftNano, final long right, boolean expectedResult) {
        final Quotation left = QuotationUtils.newQuotation(leftUnits, leftNano);

        Assertions.assertEquals(expectedResult, QuotationUtils.equals(left, right));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 0, 0",
            "1000, 0, 1000, 0",
            "0, 2000, 0, 2000",
            "3000, 4000, 3000, 4000",
            "5000, 100000000, 5000, 100000000",
            "6000, 7, 6000, 7",
            "-8000, 0, -8000, 0",
            "0, -9000, 0, -9000",
            "-1100, -1200, -1100, -1200",
            "-1300, -140000000, -1300, -140000000",
            "-1500, -1, -1500, -1",

            "10, 0, 9, 0",
            "0, 20, 0, 5",
            "30, 4, 30, 3",
            "50, 60, 30, 40",
            "50, 60, 30, 90",
            "51, 0, 50, 999999999",

            "9, 0, -10, 0",
            "0, 5, 0, -20",
            "30, 3, -30, -3",
            "30, 40, -50, -60",

            "-9, 0, -10, 0",
            "0, -5, 0, -20",
            "-30, -3, -30, -4",
            "-30, -40, -50, -60",
    })
    void max(final long units1, final int nano1, final long units2, final int nano2) {
        final Quotation quotation1 = QuotationUtils.newQuotation(units1, nano1);
        final Quotation quotation2 = QuotationUtils.newQuotation(units2, nano2);

        final Quotation straightResult = QuotationUtils.max(quotation1, quotation2);
        final Quotation reverseResult = QuotationUtils.max(quotation2, quotation1);

        Assertions.assertEquals(units1, straightResult.getUnits());
        Assertions.assertEquals(nano1, straightResult.getNano());
        Assertions.assertEquals(units1, reverseResult.getUnits());
        Assertions.assertEquals(nano1, reverseResult.getNano());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 0, 0",
            "1000, 0, 1000, 0",
            "0, 2000, 0, 2000",
            "3000, 4000, 3000, 4000",
            "5000, 100000000, 5000, 100000000",
            "6000, 7, 6000, 7",
            "-8000, 0, -8000, 0",
            "0, -9000, 0, -9000",
            "-1100, -1200, -1100, -1200",
            "-1300, -140000000, -1300, -140000000",
            "-1500, -1, -1500, -1",

            "10, 0, 9, 0",
            "0, 20, 0, 5",
            "30, 4, 30, 3",
            "50, 60, 30, 40",

            "9, 0, -10, 0",
            "0, 5, 0, -20",
            "30, 3, -30, -3",
            "30, 40, -50, -60",

            "-9, 0, -10, 0",
            "0, -5, 0, -20",
            "-30, -3, -30, -4",
            "-30, -40, -50, -60",
    })
    void min(final long units1, final int nano1, final long units2, final int nano2) {
        final Quotation quotation1 = QuotationUtils.newQuotation(units1, nano1);
        final Quotation quotation2 = QuotationUtils.newQuotation(units2, nano2);

        final Quotation straightResult = QuotationUtils.min(quotation1, quotation2);
        final Quotation reverseResult = QuotationUtils.min(quotation2, quotation1);

        Assertions.assertEquals(units2, straightResult.getUnits());
        Assertions.assertEquals(nano2, straightResult.getNano());
        Assertions.assertEquals(units2, reverseResult.getUnits());
        Assertions.assertEquals(nano2, reverseResult.getNano());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 0, 0",
            "0, 1, 0, -1",
            "2, 0, -2, 0",
            "3, 4, -3, -4",
    })
    void negate(final long units, final int nano, final long expectedUnits, final int expectedNano) {
        final Quotation quotation = QuotationUtils.newQuotation(units, nano);

        final Quotation actualResult = QuotationUtils.negate(quotation);
        final Quotation doubleNegationResult = QuotationUtils.negate(actualResult);

        Assertions.assertEquals(expectedUnits, actualResult.getUnits());
        Assertions.assertEquals(expectedNano, actualResult.getNano());
        Assertions.assertEquals(units, doubleNegationResult.getUnits());
        Assertions.assertEquals(nano, doubleNegationResult.getNano());
    }

    // region add tests

    @ParameterizedTest
    @CsvSource(value = {
            "2, 500000000, 3, 500000000, 6, 0",
            "-5, 0, 3, 0, -2, 0",
            "0, 0, 0, 0, 0, 0",
            "-5, 0, 3, 750000000, -1, -250000000",
            "-5, 0, -3, -750000000, -8, -750000000",
            "0, -50000000, 3, 0, 2, 950000000",
            "0, 0, 0, -750000000, 0, -750000000",
            "0, 0, 0, 750000000, 0, 750000000",
            "10, 600000000, 11, 750000000, 22, 350000000",
            "-10, -600000000, -11, -750000000, -22, -350000000",
    })
    void addQuotation(final long units1, final int nano1, final long units2, final int nano2, final long expectedUnits, final int expectedNano) {
        final Quotation term1 = QuotationUtils.newQuotation(units1, nano1);
        final Quotation term2 = QuotationUtils.newQuotation(units2, nano2);

        final Quotation actualResult = QuotationUtils.add(term1, term2);

        Assertions.assertEquals(expectedUnits, actualResult.getUnits());
        Assertions.assertEquals(expectedNano, actualResult.getNano());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "1480266388955167775, 529438291, 0.6174392196280002, 1480266388955167776, 146877511",
            "2, 500000000, 3.5, 6, 0",
            "-5, 0, 3.0, -2, 0",
            "0, 0, 0.0, 0, 0",
            "-5, 0, 3.75, -1, -250000000",
            "0, -50000000, 3.0,2, 950000000",
            "0, 0, -0.75, 0, -750000000",
            "0, 0, 0.75, 0, 750000000",
    })
    void addBigDecimal(final long units, final int nano, final double term2, final long expectedUnits, final int expectedNano) {
        final Quotation term1 = QuotationUtils.newQuotation(units, nano);
        final BigDecimal term2BigDecimal = DecimalUtils.setDefaultScale(term2);

        final Quotation actualResult = QuotationUtils.add(term1, term2BigDecimal);

        Assertions.assertEquals(expectedUnits, actualResult.getUnits());
        Assertions.assertEquals(expectedNano, actualResult.getNano());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "1480266388955167775, 529438291, 0.6174392196280002, 1480266388955167776, 146877511",
            "2, 500000000, 3.5, 6, 0",
            "-5, 0, 3.0, -2, 0",
            "0, 0, 0.0, 0, 0",
            "-5, 0, 3.75, -1, -250000000",
            "0, -50000000, 3.0,2, 950000000",
            "0, 0, -0.75, 0, -750000000",
            "0, 0, 0.75, 0, 750000000",
    })
    void addDouble(final long units, final int nano, final double term2, final long expectedUnits, final int expectedNano) {
        final Quotation term1 = QuotationUtils.newQuotation(units, nano);

        final Quotation actualResult = QuotationUtils.add(term1, term2);

        Assertions.assertEquals(expectedUnits, actualResult.getUnits());
        Assertions.assertEquals(expectedNano, actualResult.getNano());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "2, 500000000, 3, 5, 500000000",
            "-5, 0, 3, -2, 0",
            "0, 0, 0, 0, 0",
    })
    void addLong(final long units, final int nano, final long term2, final long expectedUnits, final int expectedNano) {
        final Quotation term1 = QuotationUtils.newQuotation(units, nano);

        final Quotation actualResult = QuotationUtils.add(term1, term2);

        Assertions.assertEquals(expectedUnits, actualResult.getUnits());
        Assertions.assertEquals(expectedNano, actualResult.getNano());
    }

    // endregion

    // region subtract tests

    @ParameterizedTest
    @CsvSource(value = {
            "5, 0, 3, 0, 2, 0",
            "-8, 0, -3, 0, -5, 0",
            "0, 0, 0, 0, 0, 0",
            "5, 0, 3, 250000000, 1, 750000000",
            "-8, 0, -3, -500000000, -4, -500000000",
    })
    void subtractQuotation(
            final long minuendUnits, final int minuendNano,
            final long subtrahendUnits, final int subtrahendNano,
            final long expectedUnits, final int expectedNano
    ) {
        final Quotation minuend = QuotationUtils.newQuotation(minuendUnits, minuendNano);
        final Quotation subtrahend = QuotationUtils.newQuotation(subtrahendUnits, subtrahendNano);

        final Quotation actualResult = QuotationUtils.subtract(minuend, subtrahend);

        Assertions.assertEquals(expectedUnits, actualResult.getUnits());
        Assertions.assertEquals(expectedNano, actualResult.getNano());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 0.0, 0, 0",
            "4413245233893295410 , 459141691, 0.9843820997815861, 4413245233893295409 , 474759591",
            "5, 0, 3.25, 1, 750000000",
            "-8, 0, -3.5, -4, -500000000",
    })
    void subtractBigDecimal(
            final long minuendUnits, final int minuendNano,
            final double subtrahend,
            final long expectedUnits, final int expectedNano
    ) {
        final Quotation minuend = QuotationUtils.newQuotation(minuendUnits, minuendNano);
        final BigDecimal subtrahendBigDecimal = DecimalUtils.setDefaultScale(subtrahend);

        final Quotation actualResult = QuotationUtils.subtract(minuend, subtrahendBigDecimal);

        Assertions.assertEquals(expectedUnits, actualResult.getUnits());
        Assertions.assertEquals(expectedNano, actualResult.getNano());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 0.0, 0, 0",
            "4413245233893295410 , 459141691, 0.9843820997815861, 4413245233893295409 , 474759591",
            "5, 0, 3.25, 1, 750000000",
            "-8, 0, -3.5, -4, -500000000",
    })
    void subtractDouble(
            final long minuendUnits, final int minuendNano,
            final double subtrahend,
            final long expectedUnits, final int expectedNano
    ) {
        final Quotation minuend = QuotationUtils.newQuotation(minuendUnits, minuendNano);

        final Quotation actualResult = QuotationUtils.subtract(minuend, subtrahend);

        Assertions.assertEquals(expectedUnits, actualResult.getUnits());
        Assertions.assertEquals(expectedNano, actualResult.getNano());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 0, 0, 0",
            "5, 0, 3, 2, 0",
            "-8, 0, -3, -5, 0",
            "0, 0, 0, 0, 0",
    })
    void subtractLong(
            final long minuendUnits, final int minuendNano,
            final long subtrahend,
            final long expectedUnits, final int expectedNano
    ) {
        final Quotation minuend = QuotationUtils.newQuotation(minuendUnits, minuendNano);

        final Quotation actualResult = QuotationUtils.subtract(minuend, subtrahend);

        Assertions.assertEquals(expectedUnits, actualResult.getUnits());
        Assertions.assertEquals(expectedNano, actualResult.getNano());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 0, 0, 0",
            "5, 5, 0, 0, 0",
            "6, 0, 0, 6, 0",
            "6, 0, 100, 5, 999999900",
            "7, 1, 500000000, 5, 500000000",

            "8, -9, 0, 17, 0",
            "10, 0, -200, 10, 200",
            "-11, 5, 400000, -16, -400000",
            "-11, -3, -300000000, -7, -700000000",
    })
    void subtractFromLong(
            final long minuend,
            final long subtrahendUnits, final int subtrahendNano,
            final long expectedUnits, final int expectedNano
    ) {
        final Quotation subtrahend = QuotationUtils.newQuotation(subtrahendUnits, subtrahendNano);

        final Quotation actualResult = QuotationUtils.subtract(minuend, subtrahend);

        Assertions.assertEquals(expectedUnits, actualResult.getUnits());
        Assertions.assertEquals(expectedNano, actualResult.getNano());
    }

    // endregion

    // region multiply tests

    @ParameterizedTest
    @CsvSource(value = {
            "180822, 925065783, 883877, 850863825, 159825378394, 54749851",
            "420426, 863182675, -7859, -397921222, -3304302014, -523802102",
            "3, 500000000, 2, 0, 7, 0",
            "-5, 0, 2, 0, -10, 0",
            "0, 0, 10, 0, 0, 0",
            "2, 0, 0, -500000000, -1, 0",
            "2, 0, 0, -50000000, 0, -100000000",
    })
    void multiplyByQuotation(
            final long units1, final int nano1,
            final long units2, final int nano2,
            final long expectedUnits, final int expectedNano
    ) {
        final Quotation term1 = QuotationUtils.newQuotation(units1, nano1);
        final Quotation term2 = QuotationUtils.newQuotation(units2, nano2);

        final Quotation actualResult = QuotationUtils.multiply(term1, term2);

        Assertions.assertEquals(expectedUnits, actualResult.getUnits());
        Assertions.assertEquals(expectedNano, actualResult.getNano());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "180822, 925065783, 883877, 850863825, 159825378394, 54749851",
            "420426, 863182675, -7859, -397921222, -3304302014, -523802102",
            "3, 500000000, 2, 0, 7, 0",
            "-5, 0, 2, 0, -10, 0",
            "0, 0, 10, 0, 0, 0",
            "2, 0, 0, -500000000, -1, 0",
            "2, 0, 0, -50000000, 0, -100000000",
    })
    void multiplyByBigDecimal(
            final long units1, final int nano1,
            final long units2, final int nano2,
            final long expectedUnits, final int expectedNano
    ) {
        final Quotation term1 = QuotationUtils.newQuotation(units1, nano1);
        final BigDecimal term2 = DecimalUtils.createBigDecimal(units2, nano2);

        final Quotation actualResult = QuotationUtils.multiply(term1, term2);

        Assertions.assertEquals(expectedUnits, actualResult.getUnits());
        Assertions.assertEquals(expectedNano, actualResult.getNano());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "180822, 925065783, 883877, 850863825, 159825378394, 54749851",
            "420426, 863182675, -7859, -397921222, -3304302014, -523802102",
            "3, 500000000, 2, 0, 7, 0",
            "-5, 0, 2, 0, -10, 0",
            "0, 0, 10, 0, 0, 0",
            "2, 0, 0, -500000000, -1, 0",
            "2, 0, 0, -50000000, 0, -100000000",
    })
    void multiplyByUnitsAndNano(
            final long units1, final int nano1,
            final long multiplicand2Units, final int multiplicand2Nano,
            final long expectedUnits, final int expectedNano
    ) {
        final Quotation term1 = QuotationUtils.newQuotation(units1, nano1);

        final Quotation actualResult = QuotationUtils.multiply(term1, multiplicand2Units, multiplicand2Nano);

        Assertions.assertEquals(expectedUnits, actualResult.getUnits());
        Assertions.assertEquals(expectedNano, actualResult.getNano());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "3, 500000000, 2.5, 8, 750000000",
            "-5, 0, -2.25, 11, 250000000",
            "0, 0, 10.0, 0, 0",
    })
    void multiplyByDouble(final long units, final int nano, final double term2, final long expectedUnits, final int expectedNano) {
        final Quotation term1 = QuotationUtils.newQuotation(units, nano);

        final Quotation actualResult = QuotationUtils.multiply(term1, term2);

        Assertions.assertEquals(expectedUnits, actualResult.getUnits());
        Assertions.assertEquals(expectedNano, actualResult.getNano());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "3, 500000000, 2, 7, 0",
            "-5, 0, 2, -10, 0",
            "0, 0, 10, 0, 0",
    })
    void multiplyByLong(final long units, final int nano, final long term2, final long expectedUnits, final int expectedNano) {
        final Quotation term1 = QuotationUtils.newQuotation(units, nano);

        final Quotation actualResult = QuotationUtils.multiply(term1, term2);

        Assertions.assertEquals(expectedUnits, actualResult.getUnits());
        Assertions.assertEquals(expectedNano, actualResult.getNano());
    }

    // endregion


    @ParameterizedTest
    @CsvSource(value = {
            "765, 0, 0, 3000000, 767, 295000000",
            "765, 0, 1, 0, 1530, 0",
    })
    void addFraction(
            final long units, final int nano,
            final long fractionUnits, final int fractionNano,
            final long expectedUnits, final int expectedNano
    ) {
        final Quotation quotation = QuotationUtils.newQuotation(units, nano);
        final Quotation fraction = QuotationUtils.newQuotation(fractionUnits, fractionNano);

        final Quotation actualResult = QuotationUtils.addFraction(quotation, fraction);

        Assertions.assertEquals(expectedUnits, actualResult.getUnits());
        Assertions.assertEquals(expectedNano, actualResult.getNano());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "765, 0, 0, 3000000, 762, 705000000",
            "765, 0, 1, 0, 0, 0",
    })
    void subtractFraction(
            final long units, final int nano,
            final long fractionUnits, final int fractionNano,
            final long expectedUnits, final int expectedNano
    ) {
        final Quotation quotation = QuotationUtils.newQuotation(units, nano);
        final Quotation fraction = QuotationUtils.newQuotation(fractionUnits, fractionNano);

        final Quotation actualResult = QuotationUtils.subtractFraction(quotation, fraction);

        Assertions.assertEquals(expectedUnits, actualResult.getUnits());
        Assertions.assertEquals(expectedNano, actualResult.getNano());
    }

    // region getFractionDifference tests

    @ParameterizedTest
    @CsvSource(value = {
            "123, 456, 123, 456, 0, 0",
            "123, 456, 1, 0, 122, 456",
            "765, 0, 762, 705000000, 0, 3009027",
    })
    void getFractionDifference(
            final long units1, final int nano1,
            final long units2, final int nano2,
            final long expectedUnits, final int expectedNano
    ) {
        final Quotation quotation1 = QuotationUtils.newQuotation(units1, nano1);
        final Quotation quotation2 = QuotationUtils.newQuotation(units2, nano2);

        final Quotation actualResult = QuotationUtils.getFractionDifference(quotation1, quotation2);

        final Quotation expectedResult = QuotationUtils.newQuotation(expectedUnits, expectedNano);
        AssertUtils.assertEquals(expectedResult, actualResult);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "10, 500000000",
            "-8, 0",
            "0, 0",
    })
    void divideByZeroQuotation_throwsArithmeticException_whenDivisorIsZero(final long dividendUnits, final int dividendNano) {
        final Quotation dividend = QuotationUtils.newQuotation(dividendUnits, dividendNano);
        final Quotation divisor = QuotationUtils.ZERO;
        Assertions.assertThrows(ArithmeticException.class, () -> QuotationUtils.divide(dividend, divisor, RoundingMode.HALF_UP));
    }

    // endregion

    // region divide tests

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 2, 34, 0, 0, HALF_UP",
            "0, 0, -2, -34, 0, 0, HALF_UP",
            "10, 500000000, 2, 0, 5, 250000000, HALF_UP",
            "-8, -10000000, -2, 0, 4, 5000000, HALF_UP",
            "-643462, -717162213, 526692, 440876624, -1, -221704865, HALF_UP",
            "-643462, -717162213, 526692, 440876624, -1, -221704864, DOWN",
    })
    void divideByQuotation(
            final long dividendUnits, final int dividendNano,
            final long divisorUnits, final int divisorNano,
            final long expectedUnits, final int expectedNano,
            final RoundingMode roundingMode
    ) {
        final Quotation dividend = QuotationUtils.newQuotation(dividendUnits, dividendNano);
        final Quotation divisor = QuotationUtils.newQuotation(divisorUnits, divisorNano);

        final Quotation actualResult = QuotationUtils.divide(dividend, divisor, roundingMode);

        Assertions.assertEquals(expectedUnits, actualResult.getUnits());
        Assertions.assertEquals(expectedNano, actualResult.getNano());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "10, 500000000",
            "-8, 0",
            "0, 0",
    })
    void divideByZeroQuotation(final long dividendUnits, final int dividendNano) {
        final Quotation dividend = QuotationUtils.newQuotation(dividendUnits, dividendNano);
        final Quotation divisor = QuotationUtils.ZERO;
        Assertions.assertThrows(ArithmeticException.class, () -> QuotationUtils.divide(dividend, divisor, RoundingMode.HALF_UP));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 2, 34, UP",
            "0, 0, -2, -34, CEILING",
            "10, 500000000, 2, 0, FLOOR",
            "-8, -10000000, -2, 0, HALF_DOWN",
            "-643462, -717162213, 526692, 440876624, HALF_EVEN",
            "-643462, -717162213, 526692, 440876624, UNNECESSARY",
    })
    void divideByQuotationWithUnexpectedRoundingMode(
            final long dividendUnits, final int dividendNano,
            final long divisorUnits, final int divisorNano,
            final RoundingMode roundingMode
    ) {
        final Quotation dividend = QuotationUtils.newQuotation(dividendUnits, dividendNano);
        final Quotation divisor = QuotationUtils.newQuotation(divisorUnits, divisorNano);
        final Executable executable = () -> QuotationUtils.divide(dividend, divisor, roundingMode);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "Unexpected rounding mode " + roundingMode);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "10, 500000000, 2.5, 4, 200000000, HALF_UP",
            "-8, -10000000, -2.0, 4, 5000000, HALF_UP",
            "0, 0, 5.0, 0, 0, HALF_UP",
            "-643462, -717162213, 526692.440876624, -1, -221704865, HALF_UP",
            "-643462, -717162213, 526692.440876624, -1, -221704864, DOWN",
    })
    void divideByDouble(
            final long dividendUnits, final int dividendNano,
            final double divisor,
            final long expectedUnits, final int expectedNano,
            final RoundingMode roundingMode
    ) {
        final Quotation dividend = QuotationUtils.newQuotation(dividendUnits, dividendNano);

        final Quotation actualResult = QuotationUtils.divide(dividend, divisor, roundingMode);

        Assertions.assertEquals(expectedUnits, actualResult.getUnits());
        Assertions.assertEquals(expectedNano, actualResult.getNano());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "10, 500000000",
            "-8, 0",
            "0, 0",
    })
    void divideByZeroDouble(final long dividendUnits, final int dividendNano) {
        final Quotation dividend = QuotationUtils.newQuotation(dividendUnits, dividendNano);
        Assertions.assertThrows(ArithmeticException.class, () -> QuotationUtils.divide(dividend, 0.0, RoundingMode.HALF_UP));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 2.34, UP",
            "0, 0, -2.34, CEILING",
            "10, 500000000, 2.0, FLOOR",
            "-8, -10000000, -2.0, HALF_DOWN",
            "-643462, -717162213, 526692.440876624, HALF_EVEN",
            "-643462, -717162213, 526692.440876624, UNNECESSARY",
    })
    void divideByDoubleWithUnexpectedRoundingMode(
            final long dividendUnits, final int dividendNano,
            final double divisor,
            final RoundingMode roundingMode
    ) {
        final Quotation dividend = QuotationUtils.newQuotation(dividendUnits, dividendNano);
        final Executable executable = () -> QuotationUtils.divide(dividend, divisor, roundingMode);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "Unexpected rounding mode " + roundingMode);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "10, 500000000, 2, 5, 250000000, HALF_UP",
            "-8, 0, -2, 4, 0, HALF_UP",
            "0, 0, 5, 0, 0, HALF_UP",
            "100, 123456789, 2, 50, 61728395, HALF_UP",
            "100, 123456789, 2, 50, 61728394, DOWN",
    })
    void divideByLong(
            final long dividendUnits, final int dividendNano,
            final long divisor,
            final long expectedUnits, final int expectedNano,
            final RoundingMode roundingMode
    ) {
        final Quotation dividend = QuotationUtils.newQuotation(dividendUnits, dividendNano);

        final Quotation actualResult = QuotationUtils.divide(dividend, divisor, roundingMode);

        Assertions.assertEquals(expectedUnits, actualResult.getUnits());
        Assertions.assertEquals(expectedNano, actualResult.getNano());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "10, 500000000",
            "-8, 0",
            "0, 0",
    })
    void divideByZeroLong(final long dividendUnits, final int dividendNano) {
        final Quotation dividend = QuotationUtils.newQuotation(dividendUnits, dividendNano);
        Assertions.assertThrows(ArithmeticException.class, () -> QuotationUtils.divide(dividend, 0L, RoundingMode.HALF_UP));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 2, UP",
            "0, 0, -2, CEILING",
            "10, 500000000, 2, FLOOR",
            "-8, -10000000, -2, HALF_DOWN",
            "-643462, -717162213, 526692, HALF_EVEN",
            "-643462, -717162213, 526692, UNNECESSARY",
    })
    void divideByLongWithUnexpectedRoundingMode(
            final long dividendUnits, final int dividendNano,
            final long divisor,
            final RoundingMode roundingMode
    ) {
        final Quotation dividend = QuotationUtils.newQuotation(dividendUnits, dividendNano);
        final Executable executable = () -> QuotationUtils.divide(dividend, divisor, roundingMode);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "Unexpected rounding mode " + roundingMode);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 213, 456, 0, 0, HALF_UP",
            "2, 10, 500000000, 0, 190476190, HALF_UP",
            "-2, -8, 0, 0, 250000000, HALF_UP",
            "-20, 3, 500000000, -5, -714285714, HALF_UP",
            "100, 6, 600000000, 15, 151515152, HALF_UP",
            "100, 6, 600000000, 15, 151515151, DOWN",
    })
    void divideLong(
            final long dividend,
            final long divisorUnits, final int divisorNano,
            final long expectedUnits, final int expectedNano,
            final RoundingMode roundingMode
    ) {
        final Quotation divisor = QuotationUtils.newQuotation(divisorUnits, divisorNano);

        final Quotation actualResult = QuotationUtils.divide(dividend, divisor, roundingMode);

        Assertions.assertEquals(expectedUnits, actualResult.getUnits());
        Assertions.assertEquals(expectedNano, actualResult.getNano());
    }

    @ParameterizedTest
    @ValueSource(longs = {10L, -8L, 0L})
    void divideLongByZero(final long dividend) {
        final Quotation divisor = QuotationUtils.ZERO;
        Assertions.assertThrows(ArithmeticException.class, () -> QuotationUtils.divide(dividend, divisor, RoundingMode.HALF_UP));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 2, UP",
            "0, 0, -2, CEILING",
            "10, 500000000, 2, FLOOR",
            "-8, -10000000, -2, HALF_DOWN",
            "-643462, -717162213, 526692, HALF_EVEN",
            "-643462, -717162213, 526692, UNNECESSARY",
    })
    void divideLongWithUnexpectedRoundingMode(
            final long dividend,
            final long divisorUnits, final int divisorNano,
            final RoundingMode roundingMode
    ) {
        final Quotation divisor = QuotationUtils.newQuotation(divisorUnits, divisorNano);
        final Executable executable = () -> QuotationUtils.divide(dividend, divisor, roundingMode);
        AssertUtils.assertThrowsWithMessage(IllegalArgumentException.class, executable, "Unexpected rounding mode " + roundingMode);
    }

    // endregion

}