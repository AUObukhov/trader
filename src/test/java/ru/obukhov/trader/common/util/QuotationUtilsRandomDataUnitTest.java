package ru.obukhov.trader.common.util;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.model.ExecutionResult;
import ru.obukhov.trader.market.model.transform.QuotationMapper;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

class QuotationUtilsRandomDataUnitTest {

    private static final QuotationMapper QUOTATION_MAPPER = Mappers.getMapper(QuotationMapper.class);

    @Test
    void testRandomQuotationsMultiple_add() {
        final long origin = Long.MIN_VALUE / 2;
        final long bound = Long.MAX_VALUE / 2;
        testRandomQuotationsMultiple(5000000, origin, bound, QuotationUtils::add, BigDecimal::add);
    }

    @Test
    void testRandomQuotationsMultiple_addLong() {
        final long origin = Long.MIN_VALUE / 2;
        final long bound = Long.MAX_VALUE / 2;
        testRandomQuotationsMultipleLong(5000000, origin, bound, QuotationUtils::add, BigDecimal::add);
    }

    @Test
    void testRandomQuotationsMultiple_subtract() {
        final long origin = Long.MIN_VALUE / 2;
        final long bound = Long.MAX_VALUE / 2;
        testRandomQuotationsMultiple(5000000, origin, bound, QuotationUtils::subtract, BigDecimal::subtract);
    }

    @Test
    void testRandomQuotationsMultiple_subtractLong() {
        final long origin = Long.MIN_VALUE / 2;
        final long bound = Long.MAX_VALUE / 2;
        testRandomQuotationsMultipleLong(5000000, origin, bound, QuotationUtils::subtract, BigDecimal::subtract);
    }

    @Test
    void testRandomQuotationsMultiple_multiply() {
        final int origin = Integer.MIN_VALUE;
        final int bound = Integer.MAX_VALUE;
        final BinaryOperator<BigDecimal> bigDecimalOperator = (d1, d2) -> DecimalUtils.setDefaultScale(d1.multiply(d2));
        testRandomQuotationsMultiple(10000000, origin, bound, QuotationUtils::multiply, bigDecimalOperator);
    }

    @Test
    void testRandomQuotationsMultiple_multiplyLong() {
        final int origin = Integer.MIN_VALUE;
        final int bound = Integer.MAX_VALUE;
        final BinaryOperator<BigDecimal> bigDecimalOperator = (d1, d2) -> DecimalUtils.setDefaultScale(d1.multiply(d2));
        testRandomQuotationsMultipleLong(10000000, origin, bound, QuotationUtils::multiply, bigDecimalOperator);
    }

    @Test
    void testRandomQuotationsMultiple_divide() {
        final long origin = Long.MIN_VALUE / QuotationUtils.NANOS_LIMIT;
        final long bound = Long.MAX_VALUE / QuotationUtils.NANOS_LIMIT;
        testRandomQuotationsMultiple(10000000, origin, bound, QuotationUtils::divide, DecimalUtils::divide);
    }

    @Test
    void testRandomQuotationsMultiple_divideLong() {
        final long origin = Long.MIN_VALUE / QuotationUtils.NANOS_LIMIT / 2;
        final long bound = Long.MAX_VALUE / QuotationUtils.NANOS_LIMIT / 2;
        testRandomQuotationsMultipleLong(10000000, origin, bound, QuotationUtils::divide, DecimalUtils::divide);
    }

    private void testRandomQuotationsMultiple(
            final int size,
            final long origin,
            final long bound,
            final BinaryOperator<Quotation> quotationOperator,
            final BinaryOperator<BigDecimal> bigDecimalOperator
    ) {
        final List<Quotation> quotations1 = TestData.createRandomQuotations(size, origin, bound);
        final List<Quotation> quotations2 = TestData.createRandomQuotations(size, origin, bound);
        final List<BigDecimal> bigDecimals1 = quotations1.stream().map(QUOTATION_MAPPER::toBigDecimal).toList();
        final List<BigDecimal> bigDecimals2 = quotations2.stream().map(QUOTATION_MAPPER::toBigDecimal).toList();

        final Pair<Duration, Duration> durations =
                testRandomQuotations(size, quotations1, quotations2, bigDecimals1, bigDecimals2, quotationOperator, bigDecimalOperator);

        System.out.println("quotationsTime = " + durations.getLeft().toMillis() + " ms");
        System.out.println("decimalsTime = " + durations.getRight().toMillis() + " ms");
    }

    private void testRandomQuotationsMultipleLong(
            final int size,
            final long origin,
            final long bound,
            final BiFunction<Quotation, Long, Quotation> quotationLongOperator,
            final BinaryOperator<BigDecimal> bigDecimalOperator
    ) {
        final List<Quotation> quotations = TestData.createRandomQuotations(size, origin, bound);
        final List<Long> longs = TestData.createRandomLongs(size, origin, bound);
        final List<BigDecimal> bigDecimals1 = quotations.stream().map(QUOTATION_MAPPER::toBigDecimal).toList();
        final List<BigDecimal> bigDecimals2 = longs.stream().map(DecimalUtils::setDefaultScale).toList();

        final Pair<Duration, Duration> durations =
                testRandomQuotations(size, quotations, longs, bigDecimals1, bigDecimals2, quotationLongOperator, bigDecimalOperator);

        System.out.println("quotationsTime = " + durations.getLeft().toMillis() + " ms");
        System.out.println("decimalsTime = " + durations.getRight().toMillis() + " ms");
    }

    private Pair<Duration, Duration> testRandomQuotations(
            final int size,
            final List<Quotation> quotations1,
            final List<Quotation> quotations2,
            final List<BigDecimal> bigDecimals1,
            final List<BigDecimal> bigDecimals2,
            final BinaryOperator<Quotation> quotationDoubleOperator,
            final BinaryOperator<BigDecimal> bigDecimalOperator
    ) {
        final ExecutionResult<List<Quotation>> quotationResult =
                ExecutionUtils.get(() -> processLists(quotations1, quotations2, quotationDoubleOperator));
        Assertions.assertNotNull(quotationResult.result());

        final ExecutionResult<List<BigDecimal>> bigDecimalResult =
                ExecutionUtils.get(() -> processLists(bigDecimals1, bigDecimals2, bigDecimalOperator));
        Assertions.assertNotNull(bigDecimalResult.result());

        for (int i = 0; i < size; i++) {
            final BigDecimal expected = bigDecimalResult.result().get(i);
            final BigDecimal actual = QUOTATION_MAPPER.toBigDecimal(quotationResult.result().get(i));
            if (!DecimalUtils.numbersEqual(actual, expected)) {
                final String message = String.format(
                        "expected: <%s> but was: <%s> for <%s> and <%s>",
                        expected, actual, quotations1.get(i), quotations2.get(i)
                );
                Assertions.fail(message);
            }
        }

        return Pair.of(quotationResult.duration(), bigDecimalResult.duration());
    }

    private Pair<Duration, Duration> testRandomQuotations(
            final int size,
            final List<Quotation> quotations,
            final List<Long> longs,
            final List<BigDecimal> bigDecimals1,
            final List<BigDecimal> bigDecimals2,
            final BiFunction<Quotation, Long, Quotation> quotationOperator,
            final BinaryOperator<BigDecimal> bigDecimalOperator
    ) {
        final ExecutionResult<List<Quotation>> quotationResult =
                ExecutionUtils.get(() -> processLists(quotations, longs, quotationOperator));
        Assertions.assertNotNull(quotationResult.result());

        final ExecutionResult<List<BigDecimal>> bigDecimalResult =
                ExecutionUtils.get(() -> processLists(bigDecimals1, bigDecimals2, bigDecimalOperator));
        Assertions.assertNotNull(bigDecimalResult.result());

        for (int i = 0; i < size; i++) {
            final BigDecimal expected = bigDecimalResult.result().get(i);
            final BigDecimal actual = QUOTATION_MAPPER.toBigDecimal(quotationResult.result().get(i));
            if (!DecimalUtils.numbersEqual(actual, expected)) {
                final String message = String.format(
                        "expected: <%s> but was: <%s> for <%s> and <%s>",
                        expected, actual, quotations.get(i), longs.get(i)
                );
                Assertions.fail(message);
            }
        }

        return Pair.of(quotationResult.duration(), bigDecimalResult.duration());
    }

    private <T> List<T> processLists(final List<T> list1, final List<T> list2, final BinaryOperator<T> operator) {
        final List<T> result = new ArrayList<>(list1.size());
        for (int i = 0; i < list1.size(); i++) {
            result.add(operator.apply(list1.get(i), list2.get(i)));
        }
        return result;
    }

    private <T1, T2> List<T1> processLists(final List<T1> list1, final List<T2> list2, final BiFunction<T1, T2, T1> operator) {
        final List<T1> result = new ArrayList<>(list1.size());
        for (int i = 0; i < list1.size(); i++) {
            result.add(operator.apply(list1.get(i), list2.get(i)));
        }
        return result;
    }

}