package ru.obukhov.trader.common.util;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.model.ExecutionResult;
import ru.obukhov.trader.market.model.transform.QuotationMapper;
import ru.obukhov.trader.test.utils.AssertUtils;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

class QuotationUtilsRandomDataUnitTest {

    private static final QuotationMapper QUOTATION_MAPPER = Mappers.getMapper(QuotationMapper.class);

    @Test
    void compare() {
        final long origin = Long.MIN_VALUE / 2;
        final long bound = Long.MAX_VALUE / 2;
        testRandomQuotationsMultiple(5000000, origin, bound, QuotationUtils::compare, BigDecimal::compareTo);
    }

    @Test
    void equalsLong() {
        final long origin = Long.MIN_VALUE / 2;
        final long bound = Long.MAX_VALUE / 2;
        testRandomQuotationsMultipleLong(5000000, origin, bound, QuotationUtils::equals, BigDecimal::equals);
    }

    @Test
    void max() {
        final long origin = Long.MIN_VALUE / 2;
        final long bound = Long.MAX_VALUE / 2;
        testRandomQuotationsMultiple(5000000, origin, bound, QuotationUtils::max, BigDecimal::max);
    }

    @Test
    void min() {
        final long origin = Long.MIN_VALUE / 2;
        final long bound = Long.MAX_VALUE / 2;
        testRandomQuotationsMultiple(5000000, origin, bound, QuotationUtils::min, BigDecimal::min);
    }

    @Test
    void add() {
        final long origin = Long.MIN_VALUE / 2;
        final long bound = Long.MAX_VALUE / 2;
        testRandomQuotationsMultiple(5000000, origin, bound, QuotationUtils::add, BigDecimal::add);
    }

    @Test
    void addLong() {
        final long origin = Long.MIN_VALUE / 2;
        final long bound = Long.MAX_VALUE / 2;
        testRandomQuotationsMultipleLong(5000000, origin, bound, QuotationUtils::add, BigDecimal::add);
    }

    @Test
    void subtract() {
        final long origin = Long.MIN_VALUE / 2;
        final long bound = Long.MAX_VALUE / 2;
        testRandomQuotationsMultiple(5000000, origin, bound, QuotationUtils::subtract, BigDecimal::subtract);
    }

    @Test
    void subtractLong() {
        final long origin = Long.MIN_VALUE / 2;
        final long bound = Long.MAX_VALUE / 2;
        testRandomQuotationsMultipleLong(5000000, origin, bound, QuotationUtils::subtract, BigDecimal::subtract);
    }

    @Test
    void subtractFromLong() {
        final long origin = Long.MIN_VALUE / 2;
        final long bound = Long.MAX_VALUE / 2;
        final BiFunction<Quotation, Long, Object> quotationLongOperator = (subtrahend, minuend) -> QuotationUtils.subtract(minuend, subtrahend);
        final BiFunction<BigDecimal, BigDecimal, Object> bigDecimalOperator = (subtrahend, minuend) -> minuend.subtract(subtrahend);
        testRandomQuotationsMultipleLong(5000000, origin, bound, quotationLongOperator, bigDecimalOperator);
    }

    @Test
    void multiply() {
        final int origin = Integer.MIN_VALUE;
        final int bound = Integer.MAX_VALUE;
        final BiFunction<Quotation, Quotation, Object> quotationFunction = QuotationUtils::multiply;
        final BiFunction<BigDecimal, BigDecimal, Object> bigDecimalFunction =
                (multiplicand1, multiplicand2) -> DecimalUtils.setDefaultScale(multiplicand1.multiply(multiplicand2));
        testRandomQuotationsMultiple(10000000, origin, bound, quotationFunction, bigDecimalFunction);
    }

    @Test
    void multiplyLong() {
        final int origin = Integer.MIN_VALUE;
        final int bound = Integer.MAX_VALUE;
        final BiFunction<BigDecimal, BigDecimal, Object> bigDecimalOperator =
                (multiplicand1, multiplicand2) -> DecimalUtils.setDefaultScale(multiplicand1.multiply(multiplicand2));
        testRandomQuotationsMultipleLong(10000000, origin, bound, QuotationUtils::multiply, bigDecimalOperator);
    }

    @Test
    void multiplyBigDecimal() {
        final int origin = Integer.MIN_VALUE;
        final int bound = Integer.MAX_VALUE;
        final BiFunction<BigDecimal, BigDecimal, Object> bigDecimalFunction =
                (multiplicand1, multiplicand2) -> DecimalUtils.setDefaultScale(multiplicand1.multiply(multiplicand2));
        testRandomQuotationsMultiple(10000000, origin, bound, QuotationUtils::multiply, bigDecimalFunction);
    }

    @Test
    void multiplyUnitsAndNano() {
        final int origin = Integer.MIN_VALUE;
        final int bound = Integer.MAX_VALUE;
        final BiFunction<Quotation, Quotation, Object> quotationFunction =
                (multiplicand1, multiplicand2) -> QuotationUtils.multiply(multiplicand1, multiplicand2.getUnits(), multiplicand2.getNano());
        final BiFunction<BigDecimal, BigDecimal, Object> bigDecimalFunction =
                (multiplicand1, multiplicand2) -> DecimalUtils.setDefaultScale(multiplicand1.multiply(multiplicand2));
        testRandomQuotationsMultiple(10000000, origin, bound, quotationFunction, bigDecimalFunction);
    }

    @Test
    void divide() {
        final long origin = Long.MIN_VALUE / QuotationUtils.NANOS_LIMIT;
        final long bound = Long.MAX_VALUE / QuotationUtils.NANOS_LIMIT;
        testRandomQuotationsMultiple(10000000, origin, bound, QuotationUtils::divide, DecimalUtils::divide);
    }

    @Test
    void divideLong() {
        final long origin = Long.MIN_VALUE / QuotationUtils.NANOS_LIMIT / 2;
        final long bound = Long.MAX_VALUE / QuotationUtils.NANOS_LIMIT / 2;
        testRandomQuotationsMultipleLong(10000000, origin, bound, QuotationUtils::divide, DecimalUtils::divide);
    }

    @Test
    void addFraction() {
        final int origin = Integer.MIN_VALUE;
        final int bound = Integer.MAX_VALUE;
        testRandomQuotationsMultiple(1000000, origin, bound, QuotationUtils::addFraction, DecimalUtils::addFraction);
    }

    @Test
    void subtractFraction() {
        final int origin = Integer.MIN_VALUE;
        final int bound = Integer.MAX_VALUE;
        testRandomQuotationsMultiple(1000000, origin, bound, QuotationUtils::subtractFraction, DecimalUtils::subtractFraction);
    }

    private <T> void testRandomQuotationsMultiple(
            final int size,
            final long origin,
            final long bound,
            final BiFunction<Quotation, Quotation, T> quotationFunction,
            final BiFunction<BigDecimal, BigDecimal, T> bigDecimalFunction
    ) {
        final List<Quotation> quotations1 = TestData.createRandomQuotations(size, origin, bound);
        final List<Quotation> quotations2 = TestData.createRandomQuotations(size, origin, bound);
        final List<BigDecimal> bigDecimals1 = quotations1.stream().map(QUOTATION_MAPPER::toBigDecimal).toList();
        final List<BigDecimal> bigDecimals2 = quotations2.stream().map(QUOTATION_MAPPER::toBigDecimal).toList();

        final Pair<Duration, Duration> durations =
                testRandomQuotations(quotations1, quotations2, bigDecimals1, bigDecimals2, quotationFunction, bigDecimalFunction);

        System.out.println("quotationsTime = " + durations.getLeft().toMillis() + " ms");
        System.out.println("decimalsTime = " + durations.getRight().toMillis() + " ms");
    }

    private <T> void testRandomQuotationsMultipleLong(
            final int size,
            final long origin,
            final long bound,
            final BiFunction<Quotation, Long, T> quotationLongFunction,
            final BiFunction<BigDecimal, BigDecimal, T> bigDecimalFunction
    ) {
        final List<Quotation> quotations = TestData.createRandomQuotations(size, origin, bound);
        final List<Long> longs = TestData.createRandomLongs(size, origin, bound);
        final List<BigDecimal> bigDecimals1 = quotations.stream().map(QUOTATION_MAPPER::toBigDecimal).toList();
        final List<BigDecimal> bigDecimals2 = longs.stream().map(DecimalUtils::setDefaultScale).toList();

        final Pair<Duration, Duration> durations =
                testRandomQuotationsLong(quotations, longs, bigDecimals1, bigDecimals2, quotationLongFunction, bigDecimalFunction);

        System.out.println("quotationsTime = " + durations.getLeft().toMillis() + " ms");
        System.out.println("decimalsTime = " + durations.getRight().toMillis() + " ms");
    }

    private <T> Pair<Duration, Duration> testRandomQuotations(
            final List<Quotation> quotations1,
            final List<Quotation> quotations2,
            final List<BigDecimal> bigDecimals1,
            final List<BigDecimal> bigDecimals2,
            final BiFunction<Quotation, Quotation, T> quotationFunction,
            final BiFunction<BigDecimal, BigDecimal, T> bigDecimalFunction
    ) {
        final ExecutionResult<List<T>> quotationResult =
                ExecutionUtils.get(() -> processLists(quotations1, quotations2, quotationFunction));
        Assertions.assertNotNull(quotationResult.result());

        final ExecutionResult<List<T>> bigDecimalResult =
                ExecutionUtils.get(() -> processLists(bigDecimals1, bigDecimals2, bigDecimalFunction));
        Assertions.assertNotNull(bigDecimalResult.result());

        AssertUtils.assertEquals(bigDecimalResult.result(), quotationResult.result());

        return Pair.of(quotationResult.duration(), bigDecimalResult.duration());
    }

    private <T> Pair<Duration, Duration> testRandomQuotationsLong(
            final List<Quotation> quotations,
            final List<Long> longs,
            final List<BigDecimal> bigDecimals1,
            final List<BigDecimal> bigDecimals2,
            final BiFunction<Quotation, Long, T> quotationFunction,
            final BiFunction<BigDecimal, BigDecimal, T> bigDecimalFunction
    ) {
        final ExecutionResult<List<T>> quotationResult =
                ExecutionUtils.get(() -> processLists(quotations, longs, quotationFunction));
        Assertions.assertNotNull(quotationResult.result());

        final ExecutionResult<List<T>> bigDecimalResult =
                ExecutionUtils.get(() -> processLists(bigDecimals1, bigDecimals2, bigDecimalFunction));
        Assertions.assertNotNull(bigDecimalResult.result());

        AssertUtils.assertEquals(bigDecimalResult.result(), quotationResult.result());

        return Pair.of(quotationResult.duration(), bigDecimalResult.duration());
    }

    private <T1, T2, T3> List<T3> processLists(final List<T1> list1, final List<T2> list2, final BiFunction<T1, T2, T3> function) {
        final List<T3> result = new ArrayList<>(list1.size());
        for (int i = 0; i < list1.size(); i++) {
            result.add(function.apply(list1.get(i), list2.get(i)));
        }
        return result;
    }

}