package ru.obukhov.trader.common.util;

import com.google.protobuf.Timestamp;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class MathUtils {

    /**
     * @return average value of passed collection with scale = {@link DecimalUtils#DEFAULT_SCALE}
     * and rounding mode = Half Up
     */
    public static BigDecimal getAverage(final Collection<BigDecimal> numbers) {
        return numbers.stream()
                .reduce(BigDecimal::add)
                .map(sum -> DecimalUtils.divide(sum, numbers.size()))
                .orElse(DecimalUtils.setDefaultScale(0));
    }

    /**
     * @return average value of passed {@code numbers} with scale = {@link DecimalUtils#DEFAULT_SCALE}
     * and rounding mode = Half Up
     */
    public static BigDecimal getAverage(final BigDecimal... numbers) {
        return getAverage(List.of(numbers));
    }

    // region getWeightedAverage

    /**
     * Calculates weighted average of given amounts.<br/>
     * Weight is count of milliseconds every amount was actual.
     * Every amount is actual since its timestamp inclusive until timestamp of next amount exclusive.
     * The last amount is actual until given {@code endTimestamp}
     *
     * @param timestampsToAmounts begin timestamps and corresponding amounts
     * @param endTimestamp        end timestamp of last amount from given {@code timestampsToAmounts}.
     *                            Can't be before any of timestamp in {@code timestampsToAmounts}
     * @return weighted average or zero if given {@code timestampsToAmounts} is empty
     */
    public static Quotation getWeightedAverage(final Map<Timestamp, Quotation> timestampsToAmounts, final Timestamp endTimestamp) {
        final Map<Long, Quotation> weightedAmounts = timestampsToAmounts.entrySet().stream()
                .collect(Collectors.toMap(entry -> getWeight(entry.getKey(), endTimestamp), Map.Entry::getValue));

        return getWeightedAverage(weightedAmounts);
    }

    private static long getWeight(final Timestamp timestamp, final Timestamp endTimestamp) {
        Assert.isTrue(!TimestampUtils.isBefore(endTimestamp, timestamp), "All timestamps must be before endTimestamp");

        return TimestampUtils.toDuration(timestamp, endTimestamp).toMillis();
    }

    private static Quotation getWeightedAverage(final Map<Long, Quotation> weightedAmounts) {
        final Quotation weightsSum = QuotationUtils.newQuotation(weightedAmounts.keySet().stream().reduce(0L, Math::addExact));

        Quotation weightedAverage = QuotationUtils.ZERO;
        for (final Map.Entry<Long, Quotation> entry : weightedAmounts.entrySet()) {
            final Quotation weightedAmount = QuotationUtils.multiply(entry.getValue(), entry.getKey());
            final Quotation normalizedWeightedAmount = QuotationUtils.divide(weightedAmount, weightsSum, RoundingMode.HALF_UP);
            weightedAverage = QuotationUtils.add(weightedAverage, normalizedWeightedAmount);
        }
        return weightedAverage;
    }

    // endregion

    /**
     * @return maximum of given {@code values}, or null if {@code values} is empty
     */
    @Nullable
    public static Double max(final List<Double> values) {
        return values.stream()
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    /**
     * @return minimum of given {@code values}, or null if {@code values} is empty
     */
    @Nullable
    public static Double min(final List<Double> values) {
        return values.stream()
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    /**
     * @return dividend / divisor rounded up
     */
    public static long divideRoundUp(final long dividend, final long divisor) {
        long quotient = dividend / divisor;
        final long remainder = dividend % divisor;

        if (remainder != 0 && 2 * Math.abs(remainder) >= Math.abs(divisor)) {
            quotient += (dividend < 0) != (divisor < 0) ? -1 : 1;
        }

        return quotient;
    }

}