package ru.obukhov.trader.common.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@UtilityClass
public class MathUtils {

    public static BigDecimal getAverage(final Collection<BigDecimal> numbers) {
        return numbers.stream()
                .reduce(BigDecimal::add)
                .map(sum -> DecimalUtils.divide(sum, numbers.size()))
                .orElse(DecimalUtils.ZERO);
    }

    public static BigDecimal getAverage(final BigDecimal... numbers) {
        return getAverage(List.of(numbers));
    }

    // region getWeightedAverage

    public static BigDecimal getWeightedAverage(final Map<OffsetDateTime, BigDecimal> dateTimesToAmounts, final OffsetDateTime endDateTime) {
        final Map<Long, BigDecimal> weightedAmounts = dateTimesToAmounts.entrySet().stream()
                .collect(Collectors.toMap(entry -> getWeight(entry.getKey(), endDateTime), Map.Entry::getValue));

        return getWeightedAverage(weightedAmounts);
    }

    private static long getWeight(final OffsetDateTime dateTime, final OffsetDateTime endDateTime) {
        Assert.isTrue(!endDateTime.isBefore(dateTime), "All dateTimes must be before endDateTime");
        return Duration.between(dateTime, endDateTime).toMillis();
    }

    private static BigDecimal getWeightedAverage(final Map<Long, BigDecimal> weightedAmounts) {
        final BigDecimal weightsSum = DecimalUtils.setDefaultScale(weightedAmounts.keySet().stream().reduce(0L, Long::sum));

        BigDecimal weightedAverage = DecimalUtils.ZERO;
        for (final Map.Entry<Long, BigDecimal> entry : weightedAmounts.entrySet()) {
            final BigDecimal normalizedWeight = DecimalUtils.divideAccurate(entry.getKey(), weightsSum);
            final BigDecimal weightedAmount = entry.getValue().multiply(normalizedWeight);
            weightedAverage = weightedAverage.add(weightedAmount);
        }

        return DecimalUtils.setDefaultScale(weightedAverage);
    }

    // endregion

    @Nullable
    public static Double max(final List<Double> values) {
        return values.stream()
                .max(Comparator.naturalOrder())
                .orElse(null);
    }

    @Nullable
    public static Double min(final List<Double> values) {
        return values.stream()
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    public static long divideRoundUp(final long dividend, final long divisor) {
        long quotient = dividend / divisor;
        final long remainder = dividend % divisor;

        if (remainder != 0 && 2 * Math.abs(remainder) >= Math.abs(divisor)) {
            quotient += (dividend < 0) != (divisor < 0) ? -1 : 1;
        }

        return quotient;
    }

}