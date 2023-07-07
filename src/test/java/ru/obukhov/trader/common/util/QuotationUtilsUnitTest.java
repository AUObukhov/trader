package ru.obukhov.trader.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.util.stream.Stream;

class QuotationUtilsUnitTest {

    private static Stream<Arguments> testData_for_newQuotation() {
        return Stream.of(
                Arguments.of(0, 0, 0, 0),
                Arguments.of(1, 0, 1, 0),
                Arguments.of(0, 2, 0, 2),
                Arguments.of(3, 4, 3, 4),
                Arguments.of(-5, 0, -5, 0),
                Arguments.of(0, -6, 0, -6),
                Arguments.of(-7, -8, -7, -8),
                Arguments.of(-9, 10, -8, -999999990),
                Arguments.of(11, -12, 10, 999999988)
        );
    }

    @ParameterizedTest
    @MethodSource("testData_for_newQuotation")
    void newQuotation(final long units, final int nano, final long expectedUnits, final int expectedNano) {
        final Quotation actualResult = QuotationUtils.newNormalizedQuotation(units, nano);
        Assertions.assertEquals(expectedUnits, actualResult.getUnits());
        Assertions.assertEquals(expectedNano, actualResult.getNano());
    }

    private static Stream<Arguments> testData_for_toString() {
        return Stream.of(
                Arguments.of(QuotationUtils.newNormalizedQuotation(0, 0), "[0; 0]"),
                Arguments.of(QuotationUtils.newNormalizedQuotation(1000, 0), "[1000; 0]"),
                Arguments.of(QuotationUtils.newNormalizedQuotation(0, 2000), "[0; 2000]"),
                Arguments.of(QuotationUtils.newNormalizedQuotation(3000, 4000), "[3000; 4000]"),
                Arguments.of(QuotationUtils.newNormalizedQuotation(5000, 100000000), "[5000; 100000000]"),
                Arguments.of(QuotationUtils.newNormalizedQuotation(6000, 7), "[6000; 7]"),
                Arguments.of(QuotationUtils.newNormalizedQuotation(-8000, 0), "[-8000; 0]"),
                Arguments.of(QuotationUtils.newNormalizedQuotation(0, -9000), "[0; -9000]"),
                Arguments.of(QuotationUtils.newNormalizedQuotation(-1100, -1200), "[-1100; -1200]"),
                Arguments.of(QuotationUtils.newNormalizedQuotation(-1300, -140000000), "[-1300; -140000000]"),
                Arguments.of(QuotationUtils.newNormalizedQuotation(-1500, -1), "[-1500; -1]")
        );
    }

    @ParameterizedTest
    @MethodSource("testData_for_toString")
    void toString(final Quotation quotation, final String expectedResult) {
        final String actualResult = QuotationUtils.toString(quotation);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    private static Stream<Arguments> testData_for_toPrettyString() {
        return Stream.of(
                Arguments.of(QuotationUtils.newNormalizedQuotation(0, 0), "0"),
                Arguments.of(QuotationUtils.newNormalizedQuotation(1000, 0), "1000"),
                Arguments.of(QuotationUtils.newNormalizedQuotation(0, 2000), "0.000002"),
                Arguments.of(QuotationUtils.newNormalizedQuotation(3000, 4000), "3000.000004"),
                Arguments.of(QuotationUtils.newNormalizedQuotation(5000, 100000000), "5000.1"),
                Arguments.of(QuotationUtils.newNormalizedQuotation(6000, 7), "6000.000000007"),
                Arguments.of(QuotationUtils.newNormalizedQuotation(-8000, 0), "-8000"),
                Arguments.of(QuotationUtils.newNormalizedQuotation(0, -9000), "-0.000009"),
                Arguments.of(QuotationUtils.newNormalizedQuotation(-1100, -1200), "-1100.0000012"),
                Arguments.of(QuotationUtils.newNormalizedQuotation(-1300, -140000000), "-1300.14"),
                Arguments.of(QuotationUtils.newNormalizedQuotation(-1500, -1), "-1500.000000001")
        );
    }

    @ParameterizedTest
    @MethodSource("testData_for_toPrettyString")
    void toPrettyString(final Quotation quotation, final String expectedResult) {
        final String actualResult = QuotationUtils.toPrettyString(quotation);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    private static Stream<Arguments> testData_for_getSign() {
        return Stream.of(
                Arguments.of(QuotationUtils.newNormalizedQuotation(0, 0), 0),
                Arguments.of(QuotationUtils.newNormalizedQuotation(1000, 0), 1),
                Arguments.of(QuotationUtils.newNormalizedQuotation(0, 1000), 1),
                Arguments.of(QuotationUtils.newNormalizedQuotation(1000, 1000), 1),
                Arguments.of(QuotationUtils.newNormalizedQuotation(-1000, 0), -1),
                Arguments.of(QuotationUtils.newNormalizedQuotation(0, -1000), -1),
                Arguments.of(QuotationUtils.newNormalizedQuotation(-1000, -1000), -1)
        );
    }

    @ParameterizedTest
    @MethodSource("testData_for_getSign")
    void getSign(final Quotation quotation, final int expectedResult) {
        final int actualResult = QuotationUtils.getSign(quotation);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    // region compare tests

    private static Stream<Arguments> testData_for_compareEquals() {
        return Stream.of(
                Arguments.of(0, 0),
                Arguments.of(1000, 0),
                Arguments.of(0, 2000),
                Arguments.of(3000, 4000),
                Arguments.of(5000, 100000000),
                Arguments.of(6000, 7),
                Arguments.of(-8000, 0),
                Arguments.of(0, -9000),
                Arguments.of(-1100, -1200),
                Arguments.of(-1300, -140000000),
                Arguments.of(-1500, -1)
        );
    }

    @ParameterizedTest
    @MethodSource("testData_for_compareEquals")
    void compareEquals(final long units, final int nano) {
        final Quotation left = QuotationUtils.newNormalizedQuotation(units, nano);
        final Quotation right = QuotationUtils.newNormalizedQuotation(units, nano);
        Assertions.assertEquals(0, QuotationUtils.compare(left, right));
        Assertions.assertEquals(0, QuotationUtils.compare(right, left));
    }

    private static Stream<Arguments> testData_for_compare() {
        return Stream.of(
                Arguments.of(QuotationUtils.newNormalizedQuotation(10L, 0), QuotationUtils.newNormalizedQuotation(9L, 0), 1),
                Arguments.of(QuotationUtils.newNormalizedQuotation(0L, 20), QuotationUtils.newNormalizedQuotation(0L, 5), 1),
                Arguments.of(QuotationUtils.newNormalizedQuotation(30L, 4), QuotationUtils.newNormalizedQuotation(30L, 3), 1),
                Arguments.of(QuotationUtils.newNormalizedQuotation(50L, 60), QuotationUtils.newNormalizedQuotation(30L, 40), 1),

                Arguments.of(QuotationUtils.newNormalizedQuotation(9L, 0), QuotationUtils.newNormalizedQuotation(-10L, 0), 1),
                Arguments.of(QuotationUtils.newNormalizedQuotation(0L, 5), QuotationUtils.newNormalizedQuotation(0L, -20), 1),
                Arguments.of(QuotationUtils.newNormalizedQuotation(30L, 3), QuotationUtils.newNormalizedQuotation(-30L, -3), 1),
                Arguments.of(QuotationUtils.newNormalizedQuotation(30L, 40), QuotationUtils.newNormalizedQuotation(-50L, -60), 1),

                Arguments.of(QuotationUtils.newNormalizedQuotation(-10L, 0), QuotationUtils.newNormalizedQuotation(-9L, 0), -1),
                Arguments.of(QuotationUtils.newNormalizedQuotation(0L, -20), QuotationUtils.newNormalizedQuotation(0L, -5), -1),
                Arguments.of(QuotationUtils.newNormalizedQuotation(-30L, -4), QuotationUtils.newNormalizedQuotation(-30L, -3), -1),
                Arguments.of(QuotationUtils.newNormalizedQuotation(-50L, -60), QuotationUtils.newNormalizedQuotation(-30L, -40), -1)
        );
    }

    @ParameterizedTest
    @MethodSource("testData_for_compare")
    void compare(final Quotation quotation1, final Quotation quotation2, int expectedResult) {
        Assertions.assertEquals(expectedResult, QuotationUtils.compare(quotation1, quotation2));
        Assertions.assertEquals(-expectedResult, QuotationUtils.compare(quotation2, quotation1));
    }

    // endregion

    // region add tests

    private static Stream<Arguments> testData_for_addQuotation() {
        return Stream.of(
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(2, 500000000),
                        QuotationUtils.newNormalizedQuotation(3, 500000000),
                        QuotationUtils.newNormalizedQuotation(6, 0)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(-5, 0),
                        QuotationUtils.newNormalizedQuotation(3, 0),
                        QuotationUtils.newNormalizedQuotation(-2, 0)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(0, 0),
                        QuotationUtils.newNormalizedQuotation(0, 0),
                        QuotationUtils.newNormalizedQuotation(0, 0)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(-5, 0),
                        QuotationUtils.newNormalizedQuotation(3, 750000000),
                        QuotationUtils.newNormalizedQuotation(-1, -250000000)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(-5, 0),
                        QuotationUtils.newNormalizedQuotation(-3, -750000000),
                        QuotationUtils.newNormalizedQuotation(-8, -750000000)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(0, -50000000),
                        QuotationUtils.newNormalizedQuotation(3, 0),
                        QuotationUtils.newNormalizedQuotation(2, 950000000)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(0, 0),
                        QuotationUtils.newNormalizedQuotation(0, -750000000),
                        QuotationUtils.newNormalizedQuotation(0, -750000000)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(0, 0),
                        QuotationUtils.newNormalizedQuotation(0, 750000000),
                        QuotationUtils.newNormalizedQuotation(0, 750000000)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(10, 600000000),
                        QuotationUtils.newNormalizedQuotation(11, 750000000),
                        QuotationUtils.newNormalizedQuotation(22, 350000000)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(-10, -600000000),
                        QuotationUtils.newNormalizedQuotation(-11, -750000000),
                        QuotationUtils.newNormalizedQuotation(-22, -350000000)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testData_for_addQuotation")
    void addQuotation(final Quotation term1, final Quotation term2, final Quotation expectedResult) {
        final Quotation actualResult = QuotationUtils.add(term1, term2);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    private static Stream<Arguments> testData_for_addDouble() {
        return Stream.of(
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(1480266388955167775L, 529438291),
                        0.6174392196280002,
                        QuotationUtils.newNormalizedQuotation(1480266388955167776L, 146877511)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(2, 500000000),
                        3.5,
                        QuotationUtils.newNormalizedQuotation(6, 0)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(-5, 0),
                        3.0,
                        QuotationUtils.newNormalizedQuotation(-2, 0)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(0, 0),
                        0.0,
                        QuotationUtils.newNormalizedQuotation(0, 0)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(-5, 0),
                        3.75,
                        QuotationUtils.newNormalizedQuotation(-1, -250000000)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(0, -50000000),
                        3.0,
                        QuotationUtils.newNormalizedQuotation(2, 950000000)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(0, 0),
                        -0.75,
                        QuotationUtils.newNormalizedQuotation(0, -750000000)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(0, 0),
                        0.75,
                        QuotationUtils.newNormalizedQuotation(0, 750000000)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testData_for_addDouble")
    void addDouble(final Quotation term1, final double term2, final Quotation expectedResult) {
        final Quotation actualResult = QuotationUtils.add(term1, term2);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    private static Stream<Arguments> testData_for_addLong() {
        return Stream.of(
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(2, 500000000),
                        3L,
                        QuotationUtils.newNormalizedQuotation(5, 500000000)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(-5, 0),
                        3L,
                        QuotationUtils.newNormalizedQuotation(-2, 0)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(0, 0),
                        0L,
                        QuotationUtils.newNormalizedQuotation(0, 0)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testData_for_addLong")
    void addLong(final Quotation term1, final long term2, final Quotation expectedResult) {
        final Quotation actualResult = QuotationUtils.add(term1, term2);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region subtract tests

    private static Stream<Arguments> testData_for_subtractQuotation() {
        return Stream.of(
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(5, 0),
                        QuotationUtils.newNormalizedQuotation(3, 0),
                        QuotationUtils.newNormalizedQuotation(2, 0)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(-8, 0),
                        QuotationUtils.newNormalizedQuotation(-3, 0),
                        QuotationUtils.newNormalizedQuotation(-5, 0)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(0, 0),
                        QuotationUtils.newNormalizedQuotation(0, 0),
                        QuotationUtils.newNormalizedQuotation(0, 0)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(5, 0),
                        QuotationUtils.newNormalizedQuotation(3, 250000000),
                        QuotationUtils.newNormalizedQuotation(1, 750000000)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(-8, 0),
                        QuotationUtils.newNormalizedQuotation(-3, -500000000),
                        QuotationUtils.newNormalizedQuotation(-4, -500000000)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testData_for_subtractQuotation")
    void subtractQuotation(final Quotation minuend, final Quotation subtrahend, final Quotation expectedResult) {
        final Quotation actualResult = QuotationUtils.subtract(minuend, subtrahend);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    private static Stream<Arguments> testData_for_subtractDouble() {
        return Stream.of(
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(4413245233893295410L, 459141691),
                        0.9843820997815861,
                        QuotationUtils.newNormalizedQuotation(4413245233893295409L, 474759591)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(5, 0),
                        3.25,
                        QuotationUtils.newNormalizedQuotation(1, 750000000)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(-8, 0),
                        -3.5,
                        QuotationUtils.newNormalizedQuotation(-4, -500000000)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(0, 0),
                        0.0,
                        QuotationUtils.newNormalizedQuotation(0, 0)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testData_for_subtractDouble")
    void subtractDouble(final Quotation minuend, final double subtrahend, final Quotation expectedResult) {
        final Quotation actualResult = QuotationUtils.subtract(minuend, subtrahend);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    private static Stream<Arguments> testData_for_subtractLong() {
        return Stream.of(
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(5, 0),
                        3L,
                        QuotationUtils.newNormalizedQuotation(2, 0)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(-8, 0),
                        -3L,
                        QuotationUtils.newNormalizedQuotation(-5, 0)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(0, 0),
                        0L,
                        QuotationUtils.newNormalizedQuotation(0, 0)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testData_for_subtractLong")
    void subtractLong(final Quotation minuend, final long subtrahend, final Quotation expectedResult) {
        final Quotation actualResult = QuotationUtils.subtract(minuend, subtrahend);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region multiply tests

    private static Stream<Arguments> testData_for_multiplyByQuotation() {
        return Stream.of(
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(180822L, 925065783),
                        QuotationUtils.newNormalizedQuotation(883877L, 850863825),
                        QuotationUtils.newNormalizedQuotation(159825378394L, 54749851)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(420426L, 863182675),
                        QuotationUtils.newNormalizedQuotation(-7859L, -397921222),
                        QuotationUtils.newNormalizedQuotation(-3304302014L, -523802102)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(3L, 500000000),
                        QuotationUtils.newNormalizedQuotation(2L, 0),
                        QuotationUtils.newNormalizedQuotation(7L, 0)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(-5L, 0),
                        QuotationUtils.newNormalizedQuotation(2L, 0),
                        QuotationUtils.newNormalizedQuotation(-10L, 0)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(0L, 0),
                        QuotationUtils.newNormalizedQuotation(10L, 0),
                        QuotationUtils.newNormalizedQuotation(0L, 0)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(2L, 0),
                        QuotationUtils.newNormalizedQuotation(0L, -500000000),
                        QuotationUtils.newNormalizedQuotation(-1L, 0)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(2L, 0),
                        QuotationUtils.newNormalizedQuotation(0L, -50000000),
                        QuotationUtils.newNormalizedQuotation(0L, -100000000)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testData_for_multiplyByQuotation")
    void multiplyByQuotation(final Quotation multiplicand1, final Quotation multiplicand2, final Quotation expectedResult) {
        final Quotation actualResult = QuotationUtils.multiply(multiplicand1, multiplicand2);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    private static Stream<Arguments> testData_for_multiplyByDouble() {
        return Stream.of(
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(3L, 500000000),
                        2.5,
                        QuotationUtils.newNormalizedQuotation(8L, 750000000)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(-5L, 0),
                        -2.25,
                        QuotationUtils.newNormalizedQuotation(11L, 250000000)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(0L, 0),
                        10.0,
                        QuotationUtils.newNormalizedQuotation(0L, 0)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testData_for_multiplyByDouble")
    void multiplyByDouble(final Quotation multiplicand1, final double multiplicand2, final Quotation expectedResult) {
        final Quotation actualResult = QuotationUtils.multiply(multiplicand1, multiplicand2);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    private static Stream<Arguments> testData_for_multiplyByLong() {
        return Stream.of(
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(3L, 500000000),
                        2L,
                        QuotationUtils.newNormalizedQuotation(7L, 0)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(-5L, 0),
                        2L,
                        QuotationUtils.newNormalizedQuotation(-10L, 0)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(0L, 0),
                        10L,
                        QuotationUtils.newNormalizedQuotation(0L, 0)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testData_for_multiplyByLong")
    void multiplyByLong(final Quotation multiplicand1, final long multiplicand2, final Quotation expectedResult) {
        final Quotation actualResult = QuotationUtils.multiply(multiplicand1, multiplicand2);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    // endregion

    // region divide tests

    private static Stream<Arguments> testData_for_divideByQuotation() {
        return Stream.of(
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(0L, 0),
                        QuotationUtils.newNormalizedQuotation(2L, 34),
                        QuotationUtils.newNormalizedQuotation(0L, 0)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(0L, 0),
                        QuotationUtils.newNormalizedQuotation(-2L, -34),
                        QuotationUtils.newNormalizedQuotation(0L, 0)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(10L, 500000000),
                        QuotationUtils.newNormalizedQuotation(2L, 0),
                        QuotationUtils.newNormalizedQuotation(5L, 250000000)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(-8L, -10000000),
                        QuotationUtils.newNormalizedQuotation(-2L, 0),
                        QuotationUtils.newNormalizedQuotation(4L, 5000000)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(-643462L, -717162213),
                        QuotationUtils.newNormalizedQuotation(526692, 440876624),
                        QuotationUtils.newNormalizedQuotation(-1L, -221704865)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testData_for_divideByQuotation")
    void divideByQuotation(final Quotation dividend, final Quotation divisor, final Quotation expectedResult) {
        final Quotation actualResult = QuotationUtils.divide(dividend, divisor);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    private static Stream<Arguments> testData_for_divideByDouble() {
        return Stream.of(
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(10L, 500000000),
                        2.5,
                        QuotationUtils.newNormalizedQuotation(4L, 200000000)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(-8L, -10000000),
                        -2.0,
                        QuotationUtils.newNormalizedQuotation(4L, 5000000)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(0L, 0),
                        5.0,
                        QuotationUtils.newNormalizedQuotation(0L, 0)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testData_for_divideByDouble")
    void divideByDouble(final Quotation a, final double b, final Quotation expectedResult) {
        final Quotation actualResult = QuotationUtils.divide(a, b);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    private static Stream<Arguments> testData_for_divideByLong() {
        return Stream.of(
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(10L, 500000000),
                        2L,
                        QuotationUtils.newNormalizedQuotation(5L, 250000000)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(-8L, 0),
                        -2L,
                        QuotationUtils.newNormalizedQuotation(4L, 0)
                ),
                Arguments.of(
                        QuotationUtils.newNormalizedQuotation(0L, 0),
                        5L,
                        QuotationUtils.newNormalizedQuotation(0L, 0)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testData_for_divideByLong")
    void divideByLong(final Quotation dividend, final long divisor, final Quotation expectedResult) {
        final Quotation actualResult = QuotationUtils.divide(dividend, divisor);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    private static Stream<Arguments> testData_for_divideLong() {
        return Stream.of(
                Arguments.of(
                        0L,
                        QuotationUtils.newNormalizedQuotation(213L, 456),
                        QuotationUtils.newNormalizedQuotation(0L, 0)
                ),
                Arguments.of(
                        2L,
                        QuotationUtils.newNormalizedQuotation(10L, 500000000),
                        QuotationUtils.newNormalizedQuotation(0L, 190476190)
                ),
                Arguments.of(
                        -2L,
                        QuotationUtils.newNormalizedQuotation(-8L, 0),
                        QuotationUtils.newNormalizedQuotation(0L, 250000000)
                ),
                Arguments.of(
                        -20L,
                        QuotationUtils.newNormalizedQuotation(3L, 500000000),
                        QuotationUtils.newNormalizedQuotation(-5L, -714285714)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testData_for_divideLong")
    void divideLong(final long dividend, final Quotation divisor, final Quotation expectedResult) {
        final Quotation actualResult = QuotationUtils.divide(dividend, divisor);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    private static Stream<Arguments> testData_for_divideByZero() {
        return Stream.of(
                Arguments.of(QuotationUtils.newNormalizedQuotation(10L, 500000000)),
                Arguments.of(QuotationUtils.newNormalizedQuotation(-8L, 0)),
                Arguments.of(QuotationUtils.newNormalizedQuotation(0L, 0))
        );
    }

    @ParameterizedTest
    @MethodSource("testData_for_divideByZero")
    void divideByZeroQuotation(final Quotation dividend) {
        final Quotation divisor = QuotationUtils.newNormalizedQuotation(0L, 0);
        Assertions.assertThrows(ArithmeticException.class, () -> QuotationUtils.divide(dividend, divisor));
    }

    @ParameterizedTest
    @MethodSource("testData_for_divideByZero")
    void divideByZeroDouble(final Quotation dividend) {
        Assertions.assertThrows(ArithmeticException.class, () -> QuotationUtils.divide(dividend, 0.0));
    }

    @ParameterizedTest
    @MethodSource("testData_for_divideByZero")
    void divideByZeroLong(final Quotation dividend) {
        Assertions.assertThrows(ArithmeticException.class, () -> QuotationUtils.divide(dividend, 0L));
    }

    @ParameterizedTest
    @MethodSource("testData_for_divideByZero")
    void divideLongByZero(final Quotation dividend) {
        final long longDividend = dividend.getUnits();
        final Quotation divisor = QuotationUtils.newNormalizedQuotation(0L, 0);
        Assertions.assertThrows(ArithmeticException.class, () -> QuotationUtils.divide(longDividend, divisor));
    }

    // endregion

}